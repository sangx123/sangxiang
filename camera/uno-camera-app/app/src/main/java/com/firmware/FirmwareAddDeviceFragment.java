package com.firmware;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hubbleconnected.camera.R;

import base.hubble.PublicDefineGlob;


public class FirmwareAddDeviceFragment extends Fragment {

    private View view;
    private Activity activity;
    private LinearLayout mCamera;
    private PackageManager mPkgManager;
    private boolean isCamera;
    private String modelId;

    public FirmwareAddDeviceFragment() {
    }

    public FirmwareAddDeviceFragment(String modelId) {
        this.modelId = modelId;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.firmware_add_device, container, false);
        setHasOptionsMenu(true);
        mCamera = (LinearLayout) findViewById(R.id.camera_layout);

        mCamera.setOnClickListener(mCameraOncliOnClickListener);

        return view;
    }

    private View.OnClickListener mCameraOncliOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isCamera = true;
            launchAddCameraActivity(isCamera);
        }
    };

    private void launchAddCameraActivity(final boolean isCamera) {
        final WifiManager w = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (w.getWifiState() == WifiManager.WIFI_STATE_ENABLED || w.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            String saved_token = null;

            ((FirmwareUpdateActivity) activity).unregisterNetworkChangeReceiver();
            Intent cam_conf = new Intent(activity, FirmwareCamConfigureActivity.class);
            cam_conf.putExtra(FirmwareCamConfigureActivity.str_userToken, saved_token);
            cam_conf.putExtra(FirmwareCamConfigureActivity.IS_CAMERA, isCamera);
            cam_conf.putExtra(FirmwareCamConfigureActivity.MODEL_ID, modelId);
            startActivityForResult(cam_conf, PublicDefineGlob.SETUP_CAMERA_ACTIVITY_RESULT);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            Spanned msg = Html.fromHtml("<big>" + getResources().getString(R.string.mobile_data_is_enabled_please_turn_on_wifi_to_add_camera) + "</big>");
            builder.setMessage(msg).setCancelable(true).setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
            ).setPositiveButton(getResources().getString(R.string.turn_on_wifi), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            turnOnWifi();

                            String saved_token = null;

                            ((FirmwareUpdateActivity) activity).unregisterNetworkChangeReceiver();
                            Intent cam_conf = new Intent(activity, FirmwareCamConfigureActivity.class);
                            cam_conf.putExtra(FirmwareCamConfigureActivity.str_userToken, saved_token);
                            cam_conf.putExtra(FirmwareCamConfigureActivity.IS_CAMERA, isCamera);
                            cam_conf.putExtra(FirmwareCamConfigureActivity.MODEL_ID, modelId);
                            startActivityForResult(cam_conf, PublicDefineGlob.SETUP_CAMERA_ACTIVITY_RESULT);
                            activity.finish();
                        }
                    }
            );

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void turnOnWifi() {
    /* re-use Vox_main, just an empty and transparent activity */
        WifiManager w = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        w.setWifiEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case PublicDefineGlob.CAMERA_NOT_FOUND_INSTALL_CAM_RESULT:
                if (requestCode == PublicDefineGlob.SETUP_SENSOR_ACTIVITY_REQUEST) {
                    launchAddCameraActivity(isCamera);
                }
                break;
        }
    }

    private View findViewById(int id) {
        return view.findViewById(id);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mPkgManager = activity.getPackageManager();
        this.activity = activity;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
