package com.sensor.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.hubbleconnected.camera.R;
import com.sensor.bluetooth.BluetoothLeService;

/**
 * Add Sensor to the Camera
 */
public class UpgradeSensorActivity extends ActionBarActivity {
    private static final String TAG = "UpgradeMotoTag";
    public static final String REG_ID = "reg_id";
    public static final String FW_NAME = "fw_name";
    private boolean isActivityDestroyed;
    private boolean isOnBackPressFinish;

    private UpgradeTagSensorFragment mSetupTagSensorFragment;
    private SensorErrorMessageFragment mSensorErrorMessageFragment;
    private BluetoothLeService mBluetoothLeService;
    private String mFirmwareVersion = "";
    String fileNameFw, regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.select_sensor_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.upgrade_mtag_title));

        mSetupTagSensorFragment = new UpgradeTagSensorFragment();
        mSensorErrorMessageFragment = new SensorErrorMessageFragment();

        if(getIntent().getExtras() != null) {
            fileNameFw = getIntent().getExtras().getString(FW_NAME,"");
            regId = getIntent().getExtras().getString(REG_ID,"");
        }

        switchToSetupTagSensorFragment();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, this.BIND_AUTO_CREATE);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private void disconnectBleDevice() {
        if (mBluetoothLeService != null) {
            Log.d(TAG, "Disconnecting BLE device");
            mBluetoothLeService.disconnect();
            mBluetoothLeService.close();
            Log.d(TAG, "Disconnected BLE device");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void switchToSetupTagSensorFragment() {
        if (mSetupTagSensorFragment != null) {
            isOnBackPressFinish = false;
            switchToFragment(mSetupTagSensorFragment, false);
        }
    }

    private void switchToFragment(Fragment fragment, boolean addToBackStack) {
        if (!isActivityDestroyed && fragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putString(REG_ID,regId);
            bundle.putString(FW_NAME,fileNameFw);
            fragment.setArguments(bundle);

            fragmentTransaction.replace(R.id.main_view_sensor_holder, fragment);
            if (addToBackStack) {
                fragmentTransaction.addToBackStack("back_fragment");
            }
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    public void switchToSensorErrorPage(String mailTile, String subTitle) {
        if (mSensorErrorMessageFragment != null) {
            isOnBackPressFinish = true;
            Bundle args = new Bundle();
            if (mailTile != null)
                args.putString("error_main_title", mailTile);
            if (subTitle != null)
                args.putString("error_sub_title", subTitle);
            mSensorErrorMessageFragment.setArguments(args);
            switchToFragment(mSensorErrorMessageFragment, false);
        }
    }

    public void setFirmwareVersion(String firmwareVersion) {
        Log.d(TAG, "Firmware Version: " + mFirmwareVersion);
        mFirmwareVersion = firmwareVersion;
    }

    public BluetoothLeService getBLEService() {
        return mBluetoothLeService;
    }

    protected void showLeaveSensorSetupWarning() {
        new AlertDialog.Builder(UpgradeSensorActivity.this).setTitle(getString(R.string.leave_sensor_setup))
                .setMessage(getString(R.string.are_you_sure_cancel_sensor_setup))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                        return;
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;
        unbindService(mServiceConnection);
    }

    @Override
    public void onBackPressed() {
        if (isOnBackPressFinish)
            UpgradeSensorActivity.super.onBackPressed();
        else
            showLeaveSensorSetupWarning();
    }
}
