package com.nest.common;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginListener;
import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginManager;
import com.hubbleconnected.camera.R;
import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.SmokeCOAlarm;
import com.nestlabs.sdk.Structure;
import com.nxcomm.blinkhd.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

import static com.hubbleconnected.camera.R.id.switch_smokeDetection;

/*
 * Copyright 2016 (c) Hubble Connected (HKT) Ltd. - All Rights Reserved
 *
 * Proprietary and confidential.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
public class NestRuleSettingsActivity extends Activity {

    private static final String TAG = NestRuleSettingsActivity.class.getSimpleName();
    private AppCompatSpinner smokeDetectSpinner, cameraSpinner, homeSpinner;
    private ArrayList<SmokeCOAlarm> smokeCOAlarmArrayList = new ArrayList<>();


    private ArrayList<NestStructure> homeList = new ArrayList<>();
    private List<String> mSmokeCoList = new ArrayList<>();
    private List<String> mCameraList = new ArrayList<>();
    private List<String> mHomeList = new ArrayList<>();

    private SwitchCompat smokeDetection;


    private static final int AUTH_TOKEN_REQUEST_CODE = 123;

    private Button ruleCancel, ruleSave;
    private int cameraPosition = -1, smokeProtectPosition = -1;
    private ArrayAdapter<String> smokeAdapter;
    private ArrayAdapter<String> cameraAdapter;
    private ArrayAdapter<String> homeAdapter;
    private RelativeLayout animationLayout;
    private ImageView animationView;
    private boolean isSmokeDetection = false;
    private ProgressDialog mProgressDialog;
    private String homeID, homeName;
    private int deviceList;
    private LinearLayout llSpokeSpinners;
    private NestPluginListener.GlobalListener globalListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nest_rule_settings);
        ruleCancel = (Button) findViewById(R.id.btn_ruleCancel);
        ruleSave = (Button) findViewById(R.id.btn_ruleSave);
        smokeDetection = (SwitchCompat) findViewById(switch_smokeDetection);

        smokeDetectSpinner = (AppCompatSpinner) findViewById(R.id.smokeDetectSpinner);
        cameraSpinner = (AppCompatSpinner) findViewById(R.id.cameraSpinner);
        homeSpinner = (AppCompatSpinner) findViewById(R.id.homeSpinner);
        llSpokeSpinners = (LinearLayout) findViewById(R.id.ll_smokespinners);

        animationLayout = (RelativeLayout) findViewById(R.id.loading_layout);
        animationView = (ImageView) findViewById(R.id.anim_image);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.loading_rule_settings));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        if (getIntent().getStringExtra("HOME_ID") != null && getIntent().hasExtra("HOME_ID")) {
            homeID = getIntent().getStringExtra("HOME_ID");
        }
        if (getIntent().getStringExtra("HOME_NAME") != null && getIntent().hasExtra("HOME_NAME")) {
            homeName = getIntent().getStringExtra("HOME_NAME");
        }
        if (getIntent().getStringExtra("DEVICE_LIST") != null && getIntent().hasExtra("DEVICE_LIST")) {
            deviceList = getIntent().getIntExtra("DEVICE_LIST", 0);
        }
        if(NestPluginManager.getInstance().getNestAuthListener() == null){
            authenticate(Settings.loadAuthToken(this));
        }else{
            fetchHomes();
        }
        ruleSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(smokeDetection.isChecked()) {
                    if(smokeProtectPosition == -1){
                        Toast.makeText(NestRuleSettingsActivity.this, R.string.no_smokeCo_to_set_rule, Toast.LENGTH_SHORT).show();
                        isSmokeDetection = Settings.addSmokeDetection(getApplicationContext(), homeSpinner.getSelectedItem().toString(), false);
                        smokeDetection.setChecked(false);
                        return;
                    }
                     if (cameraPosition == -1 ) {

                         Toast.makeText(NestRuleSettingsActivity.this, R.string.no_camera_available_to_set_rule, Toast.LENGTH_SHORT).show();
                       // finish();
                         smokeDetection.setChecked(false);
                         isSmokeDetection = Settings.addSmokeDetection(getApplicationContext(), homeSpinner.getSelectedItem().toString(), false);

                     } else {
                        Settings.addSmokeRule(NestRuleSettingsActivity.this, mSmokeCoList.get(smokeProtectPosition), mCameraList.get(cameraPosition));
                        Toast.makeText(NestRuleSettingsActivity.this, R.string.rule_setting_saved_successfully, Toast.LENGTH_SHORT).show();
                         isSmokeDetection = Settings.addSmokeDetection(getApplicationContext(), homeSpinner.getSelectedItem().toString(), true);
                         smokeDetection.setChecked(true);
                         Intent dashBoardIntent = new Intent(NestRuleSettingsActivity.this, MainActivity.class);
                          startActivity(dashBoardIntent);

                        finishAffinity();
                    }
                }else{
                    Toast.makeText(NestRuleSettingsActivity.this, R.string.toggle_smoke_detection_to_set_rule, Toast.LENGTH_SHORT).show();

                }
            }
        });


        ruleCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cameraAdapter = new ArrayAdapter<String>(
                NestRuleSettingsActivity.this, android.R.layout.simple_spinner_item, mCameraList);
        cameraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cameraAdapter.setNotifyOnChange(true);

        homeAdapter = new ArrayAdapter<String>(
                NestRuleSettingsActivity.this, android.R.layout.simple_spinner_item, mHomeList);
        homeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        homeAdapter.setNotifyOnChange(true);

        smokeAdapter = new ArrayAdapter<String>(
                NestRuleSettingsActivity.this, android.R.layout.simple_spinner_item, mSmokeCoList);
        smokeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        smokeAdapter.setNotifyOnChange(true);

        cameraSpinner.setAdapter(cameraAdapter);
        smokeDetectSpinner.setAdapter(smokeAdapter);
        homeSpinner.setAdapter(homeAdapter);

        cameraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cameraPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                cameraPosition = -1;
            }
        });

        smokeDetectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                smokeProtectPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                smokeProtectPosition = -1;
            }
        });

        homeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                homeID = homeList.get(position).getHomeID();
                homeName = homeList.get(position).getHomeName();
                updateSpinners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        smokeDetection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                if (isChecked) {
                    llSpokeSpinners.setVisibility(View.VISIBLE);
                    smokeDetectSpinner.setEnabled(true);
                    cameraSpinner.setEnabled(true);
                } else {
                    smokeDetectSpinner.setEnabled(false);
                    cameraSpinner.setEnabled(false);
                    llSpokeSpinners.setVisibility(View.INVISIBLE);
                }

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(globalListener != null){
            NestPluginManager.getInstance().removeListener(globalListener);
        }
    }

    private void authenticate(NestToken token) {

        NestPluginManager.getInstance().authWithToken(token, new NestPluginListener.AuthListener() {

            @Override
            public void onAuthSuccess() {
                Log.v(TAG, "Authentication succeeded.");
                fetchHomes();
            }

            @Override
            public void onAuthFailure(NestException exception) {
                Log.e(TAG, "Authentication failed with error: " + exception.getMessage());
                Settings.saveAuthToken(NestRuleSettingsActivity.this, null);
                NestPluginManager.getInstance().launchAuthFlow(NestRuleSettingsActivity.this, AUTH_TOKEN_REQUEST_CODE);
                showAnimation(false);
            }

            @Override
            public void onAuthRevoked() {
                Log.e(TAG, "Auth token was revoked!");
                Settings.saveAuthToken(NestRuleSettingsActivity.this, null);
                NestPluginManager.getInstance().launchAuthFlow(NestRuleSettingsActivity.this, AUTH_TOKEN_REQUEST_CODE);
                showAnimation(false);
            }
        });

    }


    private void fetchHomes() {

       globalListener = new NestPluginListener.GlobalListener() {
            @Override
            public void onUpdate(@NonNull GlobalUpdate update) {

                mSmokeCoList.clear();
                mHomeList.clear();
                mCameraList.clear();
                homeList.clear();
                smokeCOAlarmArrayList.clear();

                if (mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                for (Structure structure : update.getStructures()) {
                    NestStructure nestStructure = new NestStructure();
                    nestStructure.setHomeID(structure.getStructureId());

                    nestStructure.setHomeName(structure.getName());
                    nestStructure.setHomeAwayStatus(structure.getAway());
                    homeList.add(nestStructure);
                }

                for (int i = 0; i < homeList.size(); i++) {
                    mHomeList.add(homeList.get(i).getHomeName());
                }

                smokeCOAlarmArrayList.addAll(update.getSmokeCOAlarms());
                updateSpinners();
            }
        };
        NestPluginManager.getInstance().addGlobalListener(globalListener);
    }

    private void updateSpinners() {

        mCameraList.clear();
        mSmokeCoList.clear();

        for (int i = 0; i < smokeCOAlarmArrayList.size(); i++) {
            if (homeID != null) {
                if (smokeCOAlarmArrayList.get(i).getStructureId().equals(homeID)) {
                    mSmokeCoList.add(smokeCOAlarmArrayList.get(i).getNameLong());
                }
            } else {
                mSmokeCoList.add(smokeCOAlarmArrayList.get(i).getNameLong());
            }
        }

        for (Device device : DeviceSingleton.getInstance().getDevices()) {
            if (!device.getProfile().getName().equals("Thermostat")) {
                mCameraList.add(device.getProfile().getName());
            }
        }

        if (homeAdapter != null) {
            homeAdapter.notifyDataSetChanged();
        }
        if (cameraAdapter != null) {
            cameraAdapter.notifyDataSetChanged();
        }


        if (smokeAdapter != null) {
            smokeAdapter.notifyDataSetChanged();
        }
        String camID = null;
        for (int i=0; i < mSmokeCoList.size(); i++){
            if(Settings.getCamID(this, mSmokeCoList.get(i)) != null){
                smokeDetectSpinner.setSelection(i);
                camID = Settings.getCamID(this, mSmokeCoList.get(i));
            }
        }
        if(camID != null){
            for(int i =0; i < mCameraList.size(); i++){
                if(mCameraList.get(i).equals(camID)){
                    cameraSpinner.setSelection(i);
                    break;
                }
            }
        }

        if (getIntent().getStringExtra("HOME_NAME") != null && getIntent().hasExtra("HOME_NAME")) {
            int spinnerPosition = homeAdapter.getPosition(homeName);
            homeSpinner.setSelection(spinnerPosition);
        }

        Handler  handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isSmokeDetection = Settings.getSmokeDetection(getApplicationContext(), homeSpinner.getSelectedItem().toString());
                if (isSmokeDetection) {
                    llSpokeSpinners.setVisibility(View.VISIBLE);
                    smokeDetection.setChecked(true);
                    smokeDetectSpinner.setEnabled(true);
                    cameraSpinner.setEnabled(true);
                } else {
                    llSpokeSpinners.setVisibility(View.INVISIBLE);
                    smokeDetectSpinner.setEnabled(false);
                    cameraSpinner.setEnabled(false);
                    smokeDetection.setChecked(false);
                }
            }
        }, 500);


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

}
