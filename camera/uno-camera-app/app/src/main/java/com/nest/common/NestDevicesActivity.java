package com.nest.common;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.nestlabs.sdk.DeviceUpdate;
import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.SmokeCOAlarm;
import com.nestlabs.sdk.Thermostat;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.util.CommonUtil;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by dasari on 11/01/17.
 */

public class NestDevicesActivity extends AppCompatActivity {

    private static final String TAG = NestDevicesActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private NestDeviceListAdapter mNestDeviceListAdapter;
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;
    private NestToken mToken;
    private ArrayList<NestDevice> deviceList = new ArrayList<>();
    private NestDeviceListAdapter deviceListAdapter;
    private RelativeLayout animationLayout;
    private ImageView animationView;
    private String homeID, homeName;
    private TextView noDevicesText;
    private ImageView nestSettings;
    private ProgressDialog mProgressDialog;
    private HashSet<NestDevice> hashSet = new HashSet<NestDevice>();
    Intent smokeService;
    private NestPluginListener.DeviceListener deviceListener;
   // private ArrayList<SmokeCOAlarm> smokeCOAlarmArrayList = new ArrayList<>();


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nest_devices_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.nest_devices_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        animationLayout = (RelativeLayout)findViewById(R.id.loading_layout);
        animationView = (ImageView)findViewById(R.id.anim_image);
        noDevicesText = (TextView)findViewById(R.id.no_nest_devices);
        nestSettings = (ImageView) findViewById(R.id.img_settings);
        homeID = getIntent().getStringExtra("HOME_ID");
        homeName = getIntent().getStringExtra("HOME_NAME");

        TextView title = (TextView)findViewById(R.id.title_header);
        title.setText(homeName);
        if(NestPluginManager.getInstance().getNestAuthListener() == null){
            authenticate(Settings.loadAuthToken(this));
        }else{
            fetchDevices();
        }
        showAnimation(true);
        smokeService = new Intent(NestDevicesActivity.this, SmokeService.class);

        nestSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsDialog();
            }
        });
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
                fetchDevices();
            }

            @Override
            public void onAuthFailure(NestException exception) {
                Log.e(TAG, "Authentication failed with error: " + exception.getMessage());
                Settings.saveAuthToken(NestDevicesActivity.this, null);
                NestPluginManager.getInstance().launchAuthFlow(NestDevicesActivity.this, AUTH_TOKEN_REQUEST_CODE);
                showAnimation(false);
            }

            @Override
            public void onAuthRevoked() {
                Log.e(TAG, "Auth token was revoked!");
                Settings.saveAuthToken(NestDevicesActivity.this, null);
                NestPluginManager.getInstance().launchAuthFlow(NestDevicesActivity.this, AUTH_TOKEN_REQUEST_CODE);
                showAnimation(false);
            }
        });
    }

    public void showSettingsDialog()
    {

        String[] settingsItems = {getResources().getString(R.string.rule_setting_nest),getResources().getString(R.string.nest_logout)};

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
                if(selectedItem.equalsIgnoreCase(getResources().getString(R.string.nest_logout))){
                    mProgressDialog = new ProgressDialog(NestDevicesActivity.this);
                    mProgressDialog.setMessage(getResources().getString(R.string.logging_out));
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    NestPluginManager.getInstance().revokeToken(nestToken, new RevokeCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Settings.saveAuthToken(getApplicationContext(), null);
                                    Toast.makeText(getApplicationContext(),R.string.nest_logout_successfull,Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(NestDevicesActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    stopService(new Intent(NestDevicesActivity.this, SmokeService.class));
                                    if(CommonUtil.getNestConfig(getApplicationContext())) {
                                        NestPluginManager.getInstance().removeAllListeners();
                                    }
                                    if (mProgressDialog != null)
                                    {
                                        if( mProgressDialog.isShowing())
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
                                    Toast.makeText(getApplicationContext(),R.string.nest_logout_failure,Toast.LENGTH_SHORT).show();
                                    if (mProgressDialog != null)
                                    {
                                        if( mProgressDialog.isShowing())
                                            mProgressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    });
                }else if(selectedItem.equalsIgnoreCase(getResources().getString(R.string.rule_setting_with_nest))){
                    Intent ruleSettingActivity = new Intent(NestDevicesActivity.this,NestRuleSettingsActivity.class);
                      ruleSettingActivity.putExtra("HOME_ID",homeID);
                      ruleSettingActivity.putExtra("HOME_NAME",homeName);
                      ruleSettingActivity.putExtra("DEVICE_LIST",deviceList.size());
                      startActivity(ruleSettingActivity);
                               }
                alertDialog.dismiss();
            }
        });
        if(alertDialog != null && !alertDialog.isShowing()){
            alertDialog.show();
        }
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
            fetchDevices();
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
        if(deviceListener != null){
            NestPluginManager.getInstance().removeListener(deviceListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void fetchDevices(){
        Log.d(TAG,"fetchDevices");

        deviceListener = new NestPluginListener.DeviceListener() {
            @Override
            public void onUpdate(@NonNull DeviceUpdate update) {
                deviceList.clear();
                for(Thermostat thermostat : update.getThermostats()){
                    if(thermostat.getStructureId().equalsIgnoreCase(homeID)){
                        boolean isExists = false;
                        for(int k=0;k<deviceList.size();k++){
                            if(deviceList.get(k).getDeviceID().contains(thermostat.getDeviceId())){
                                isExists = true;
                            }
                        }
                        if(isExists)
                            continue;
                        NestDevice nestDevice = new NestDevice();
                        nestDevice.setDeviceType(NestDevice.DEVICE_TYPE.THERMOSTAT);
                        nestDevice.setName(thermostat.getName());
                        nestDevice.setDeviceID(thermostat.getDeviceId());
                        deviceList.add(nestDevice);
                    }
                }
                for(SmokeCOAlarm smokeCOAlarm : update.getSmokeCOAlarms()){
                    if(smokeCOAlarm.getStructureId().equalsIgnoreCase(homeID)){
                        boolean isExists = false;
                        for(int k=0;k<deviceList.size();k++){
                            if(deviceList.get(k).getDeviceID().contains(smokeCOAlarm.getDeviceId())){
                                isExists = true;
                            }
                        }
                        if(isExists)
                            continue;
                        NestDevice nestDevice = new NestDevice();
                        nestDevice.setDeviceType(NestDevice.DEVICE_TYPE.SMOKE_DETECTOR);
                        nestDevice.setName(smokeCOAlarm.getName());
                        nestDevice.setDeviceID(smokeCOAlarm.getDeviceId());
                        deviceList.add(nestDevice);
                        if (!isMyServiceRunning(SmokeService.class)) {
                            startService(smokeService);
                        }
                    }
                }
                showAnimation(false);
                if(deviceList.size() == 0){
                    Log.d(TAG, "Devices list is empty");
                    noDevicesText.setVisibility(View.VISIBLE);
                }else{
                    Log.d(TAG, "Devices list is not empty");
                    Log.d(TAG,"fetchDevices adding nest devices " + deviceList);
                    noDevicesText.setVisibility(View.GONE);
                    if(deviceListAdapter == null)
                        deviceListAdapter = new NestDeviceListAdapter(NestDevicesActivity.this);
                    deviceListAdapter.setDeviceList(deviceList,homeID,homeName);
                    mRecyclerView.setAdapter(deviceListAdapter);
                    deviceListAdapter.notifyDataSetChanged();
                }
            }
        };
        NestPluginManager.getInstance().addDeviceListener(deviceListener);
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
