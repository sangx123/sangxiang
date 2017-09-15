package com.firmware;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.NetworkStateChangeReceiver;
import com.hubble.registration.JWebClient;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubbleconnected.camera.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import base.hubble.PublicDefineGlob;

public class FirmwareUpdateActivity extends ActionBarActivity {
    private FirmwareAddDeviceFragment mAddDeviceFragment;
    private UpgradeDialogFirmware upgradeDialog;
    private ProgressDialog mProgressDialog;
    private LinearLayout viewHolder;
    private SecureConfig settings = HubbleApplication.AppConfig;
    private String modelId = "";
    private ListView listCameraModel;
    private ArrayList<CameraModel> data;
    private ListCameraAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fw_main_layout);
        setupToolbar();
        initData();

        viewHolder = (LinearLayout) findViewById(R.id.main_view_holder);
        listCameraModel = (ListView) findViewById(R.id.list_camera_model);
        listCameraModel.setAdapter(adapter);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(false);

    }

    public void switchAddDevice(Fragment frag){
        if (frag == null) {
            frag = new FirmwareAddDeviceFragment(modelId);
        }

        viewHolder.setVisibility(View.VISIBLE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_view_holder, frag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void unregisterNetworkChangeReceiver() {
        if (networkChangeRegistered) {
            unregisterReceiver(networkStateChangeReceiver);
            networkChangeRegistered = false;
        }
    }

    public void onClickUpgrade(String model){
        modelId = model;
        new DownloadLatestFirmWare().execute(model);
    }

    private NetworkStateChangeReceiver networkStateChangeReceiver = new NetworkStateChangeReceiver();

    public boolean networkChangeRegistered = false;

    public void registerNetworkChangeReceiver() {
        if (!networkChangeRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(networkStateChangeReceiver, intentFilter);
            networkChangeRegistered = true;
        }
    }

    class DownloadLatestFirmWare extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String url = String.format(PublicDefine.CHECK_LATEST_FW_OTA1, strings[0]);
                if (modelId.equals("0086")) {
                    url = String.format(PublicDefine.CHECK_LATEST_FW_OTA, strings[0]);
                }
                String latestVersion = JWebClient.downloadAsString(url);
                if (modelId.equals("0086") && latestVersion !=null)
                    latestVersion = latestVersion.substring(8,16);
                return latestVersion;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            settings.putString(modelId, result);

            if (result == null) {
                Toast.makeText(getApplicationContext(),"Error!",Toast.LENGTH_LONG).show();
            } else {
                boolean isLatest = isFirmwareBinaryCached(modelId, result);
                boolean is17 = isFirmwareBinaryCached(modelId, "01.17.00");
                if (modelId.equals("1662") || modelId.equals("0086") || modelId.equals("1854") || modelId.equals("0662") ) {
                    if ( isLatest) {
                        switchToCamConfigureActivity();
                    } else if (result != null) {
                        showUpdateDialog(result);
                    }
                } else {
                    if ( isLatest && is17) {
                        switchToCamConfigureActivity();
                    } else {
                        if (!isLatest) {
                            showUpdateDialog(result);
                        } else {
                            showUpdateDialog("01.17.00");
                        }
                    }
                }

            }
        }
    }

    private void showUpdateDialog(final String firmwareVersion) {
        upgradeDialog = new UpgradeDialogFirmware(FirmwareUpdateActivity.this, firmwareVersion, modelId, false, new IUpgradeCallbackFirmware() {
            @Override
            public void onUpgradeSucceed() {
            }

            @Override
            public void onUpgradeFail() {
            }

            @Override
            public void onDownloadSucceed() {
                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         upgradeDialog.dismiss();
                         switchToCamConfigureActivity();
                     }
                 });
            }

            @Override
            public void onDownloadFailded() {

            }
        });
        upgradeDialog.setCancelable(false);
        upgradeDialog.show(getSupportFragmentManager(), "upgrade");
    }

    private boolean isFirmwareBinaryCached(String cameraModel, String fwVersion) {
        String fwDirectory = Util.getFirmwareDirectory();
        String fwFileName = String.format("%s-%s.tar.gz", cameraModel, fwVersion);
        if (cameraModel.equals("0086")) {
            fwFileName = String.format("%s-%s.tar", cameraModel, fwVersion);
        }
        File fwFile = new File(fwDirectory + File.separator + fwFileName);

        if (fwFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (viewHolder.getVisibility() == View.VISIBLE) {
            viewHolder.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.fw_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.hubble_camera_models));
    }

    private void initData(){
        data = new ArrayList<>();
        data.add(new CameraModel(String.format(getString(R.string.camera_hd_model), "0854"),"0854"));
        data.add(new CameraModel(String.format(getString(R.string.camera_hd_model), "0066"),"0066"));
//        data.add(new CameraModel("Camera-HD 854s","1854"));
//        data.add(new CameraModel("Camera-HD 0086", "0086"));
//        data.add(new CameraModel("Camera-HD 1662", "1662"));
//        data.add(new CameraModel("Focus 662", "0662"));
//        data.add(new CameraModel("Focus 73", "0073"));

        adapter = new ListCameraAdapter(FirmwareUpdateActivity.this, data);
    }

    public void switchToCamConfigureActivity(){
        String saved_token = null;

        unregisterNetworkChangeReceiver();
        Intent cam_conf = new Intent(FirmwareUpdateActivity.this, FirmwareCamConfigureActivity.class);
        cam_conf.putExtra(FirmwareCamConfigureActivity.str_userToken, saved_token);
        cam_conf.putExtra(FirmwareCamConfigureActivity.IS_CAMERA, true);
        cam_conf.putExtra(FirmwareCamConfigureActivity.MODEL_ID, modelId);
        startActivityForResult(cam_conf, PublicDefineGlob.SETUP_CAMERA_ACTIVITY_RESULT);
    }
}
