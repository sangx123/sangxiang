package com.nest.common;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginListener;
import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginManager;
import com.hubble.framework.service.Plugins.ThirdParty.Nest.RevokeCallback;
import com.hubbleconnected.camera.R;
import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.Structure;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.util.CommonUtil;

import java.util.ArrayList;

/**
 * Created by dasari on 11/01/17.
 */

public class NestHomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = NestHomeActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;
    private NestToken mToken;
    private ArrayList<NestStructure> homeList = new ArrayList<>();
    private NestHomeListAdapter homeListAdapter;
    private RelativeLayout animationLayout;
    private ImageView animationView;
    private ImageView settings;
    private Handler handler;
    private boolean isResponseState = false;
    private ProgressDialog mProgressDialog;
    private NestPluginListener.StructureListener structureListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nest_devices_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.nest_devices_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        animationLayout = (RelativeLayout) findViewById(R.id.loading_layout);
        animationView = (ImageView) findViewById(R.id.anim_image);
        settings = (ImageView) findViewById(R.id.img_settings);
        TextView title = (TextView) findViewById(R.id.title_header);
        title.setText(R.string.homes);
        showAnimation(true);
        if (NestPluginManager.getInstance().getNestAuthListener() == null) {
            authenticate(Settings.loadAuthToken(this));
        } else {
            fetchHomes();
        }
        settings.setOnClickListener(this);
        handler = new Handler();
    }

    /**
     * Authenticate with the Nest API and start listening for updates.
     *
     * @param token the token used to authenticate.
     */
    private void authenticate(NestToken token) {
        NestPluginManager.getInstance().authWithToken(token, new NestPluginListener.AuthListener() {

            @Override
            public void onAuthSuccess() {
                Log.v(TAG, "Authentication succeeded.");
                handler.postDelayed(timedTask, 12000);
                isResponseState = false;
                fetchHomes();
            }

            @Override
            public void onAuthFailure(NestException exception) {
                Log.e(TAG, "Authentication failed with error: " + exception.getMessage());
                Settings.saveAuthToken(NestHomeActivity.this, null);
                showAnimation(false);
                finish();
                Intent intent = new Intent(NestHomeActivity.this, GetStartedActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAuthRevoked() {
                Log.e(TAG, "Auth token was revoked!");
                Settings.saveAuthToken(NestHomeActivity.this, null);
                showAnimation(false);
                Intent intent = new Intent(NestHomeActivity.this, GetStartedActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode != RESULT_OK || requestCode != AUTH_TOKEN_REQUEST_CODE) {
            Log.e(TAG, "Finished with no result.");
            return;
        }

        mToken = NestPluginManager.getAccessTokenFromIntent(intent);
        if (mToken != null) {
            Settings.saveAuthToken(this, mToken);
            Log.v(TAG, "Token received successfully");
            setContentView(R.layout.nest_authorising_layout);
            //authenticate(mToken);
            fetchHomes();
        } else {
            Log.e(TAG, "Unable to resolve access token from payload.");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (structureListener != null) {
            NestPluginManager.getInstance().removeListener(structureListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void fetchHomes() {
        structureListener = new NestPluginListener.StructureListener() {
            @Override
            public void onUpdate(@NonNull ArrayList<Structure> structures) {
                homeList.clear();
                if (!isResponseState) {
                    handler.removeCallbacks(timedTask);
                    isResponseState = true;
                }

                for (Structure structure : structures) {
                    showAnimation(false);

                    boolean isExists = false;
                    for (int k = 0; k < homeList.size(); k++) {
                        if (homeList.get(k).getHomeID().contains(structure.getStructureId())) {
                            isExists = true;
                        }
                    }
                    if (isExists)
                        continue;

                    NestStructure nestStructure = new NestStructure();
                    nestStructure.setHomeID(structure.getStructureId());
                    nestStructure.setHomeName(structure.getName());
                    nestStructure.setHomeAwayStatus(structure.getAway());
                    homeList.add(nestStructure);
                }

                homeListAdapter = new NestHomeListAdapter(NestHomeActivity.this);
                homeListAdapter.setStructureList(homeList);
                mRecyclerView.setAdapter(homeListAdapter);

            }
        };

        NestPluginManager.getInstance().addStructureListener(structureListener);

    }

    private void showAnimation(boolean enable) {
        if (enable) {
            animationLayout.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.image_rotate);
            animationView.startAnimation(animation);
        } else {
            animationView.clearAnimation();
            animationLayout.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.img_settings) {
            showSettingsDialog();
        }
    }


    public void showSettingsDialog() {

        String[] settingsItems = {getResources().getString(R.string.rule_setting_nest), getResources().getString(R.string.nest_logout)};

        final NestToken nestToken = Settings.loadAuthToken(this);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.nest_settings_dialog, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.nest_settings_item, settingsItems);
        final ListView settingsList = (ListView) dialogView.findViewById(R.id.settingsList);
        settingsList.setAdapter(adapter);
        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object listItem = settingsList.getItemAtPosition(position);
                String selectedItem = listItem.toString();
                if (selectedItem.equalsIgnoreCase(getResources().getString(R.string.nest_logout))) {
                    mProgressDialog = new ProgressDialog(NestHomeActivity.this);
                    mProgressDialog.setMessage(getResources().getString(R.string.logging_out));
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();

                    NestPluginManager.getInstance().revokeToken(nestToken, new RevokeCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(NestHomeActivity.this, R.string.nest_logout_successfull, Toast.LENGTH_SHORT).show();
                                    Settings.saveAuthToken(getApplicationContext(), null);
                                    Intent intent = new Intent(NestHomeActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    if (CommonUtil.getNestConfig(getApplicationContext())) {
                                        NestPluginManager.getInstance().removeAllListeners();
                                    }
                                    stopService(new Intent(NestHomeActivity.this, SmokeService.class));
                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing())
                                            mProgressDialog.dismiss();
                                    }
                                    finish();

                                }
                            });
                        }

                        @Override
                        public void onFailure(NestException exception) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.nest_logout_failure, Toast.LENGTH_SHORT).show();
                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing())
                                            mProgressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    });
                } else if (selectedItem.equalsIgnoreCase(getResources().getString(R.string.rule_setting_with_nest))) {
                    Intent ruleSettingActivity = new Intent(NestHomeActivity.this, NestRuleSettingsActivity.class);
                    if (homeList.size() > 0) {
                        startActivity(ruleSettingActivity);
                    } else {
                        Toast.makeText(NestHomeActivity.this, "Smoke protect should be added for setting Rule", Toast.LENGTH_SHORT).show();
                    }
                }
                alertDialog.dismiss();
            }
        });
        if (alertDialog != null && !alertDialog.isShowing()) {
            alertDialog.show();
        }
    }


    private Runnable timedTask = new Runnable() {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Please try after some time", Toast.LENGTH_SHORT).show();
                    Intent gotoMainActivity = new Intent(NestHomeActivity.this, MainActivity.class);
                    gotoMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(gotoMainActivity);
                }
            });

        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (!isResponseState) {
            handler.removeCallbacks(timedTask);
        }
    }
}


