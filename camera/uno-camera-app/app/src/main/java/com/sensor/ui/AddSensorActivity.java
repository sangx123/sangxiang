package com.sensor.ui;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.Global;
import com.sensor.bluetooth.BluetoothLeService;
import com.sensor.constants.SensorConstants;
import com.util.AppEvents;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.devices.SerializableDeviceProfile;
import base.hubble.meapi.SimpleJsonResponse;
import base.hubble.meapi.device.AddNewDeviceResponse;

/**
 * Add Sensor to the Camera
 */
public class AddSensorActivity extends ActionBarActivity {
  private static final String TAG = "HubbleMotoTag";
  private boolean isActivityDestroyed;
  private boolean isOnBackPressFinish;
  private boolean isFromCameraDetails;

  private Device mSelectedCameraDevice;
  private BluetoothDevice mBluetoothDevice;

  private ChooseCameraListFragment mChooseCameraListFragment;
  private CamerasNotFoundFragment mCamerasNotFoundFragment;
  private SetupTagSensorFragment mSetupTagSensorFragment;
  private FoundMotionSensorFragment mFoundMotionSensorFragment;
  private SettingTagAsMotionSensorFragment mSettingTagAsMotionSensorFragment;
  private InstructionStickToDoorFragment mInstructionStickToDoorFragment;
  private AfterFixingMotionTagFragment mAfterFixingMotionTagFragment;
  private SensorErrorMessageFragment mSensorErrorMessageFragment;
  private BluetoothLeService mBluetoothLeService;
  private String mSensorType = "";
  private String mFirmwareVersion = "";
  private String mSensorName;
  private String mSensorRegistrationId;
  private boolean isSensorPairingNorified;
  private Handler mHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_sensor);

    Toolbar toolbar = (Toolbar) findViewById(R.id.select_sensor_toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(getString(R.string.add_a_focus_tag));

    mChooseCameraListFragment = new ChooseCameraListFragment();
    mCamerasNotFoundFragment = new CamerasNotFoundFragment();
    mSetupTagSensorFragment = new SetupTagSensorFragment();
    mFoundMotionSensorFragment = new FoundMotionSensorFragment();
    mSettingTagAsMotionSensorFragment = new SettingTagAsMotionSensorFragment();
    mInstructionStickToDoorFragment = new InstructionStickToDoorFragment();
    mAfterFixingMotionTagFragment = new AfterFixingMotionTagFragment();
    mSensorErrorMessageFragment = new SensorErrorMessageFragment();

    if (getIntent().getBooleanExtra("isCameraDetail", false))
      isFromCameraDetails = true;

    switchToChooseCameraListFragment();

    // Register receiver to get the notifications of Sensor paired with camera, after sensor registration
//    final IntentFilter intentFilter = new IntentFilter();
//    intentFilter.addAction(SensorConstants.ACTION_SENSOR_PAIRED_EVENT);
//    intentFilter.addAction(SensorConstants.ACTION_SENSOR_NOT_PAIRED_EVENT);
//    registerReceiver(mSensorPairedReceiver, intentFilter);

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

  public void buyCamera() {
    try {
      String url = "http://hubblehome.com/hubble-products/";
      Intent i = new Intent(Intent.ACTION_VIEW);
      i.setData(Uri.parse(url));
      startActivity(i);
    } catch (Exception ex) {
      Log.e(TAG, Log.getStackTraceString(ex));
    }
  }

  private void switchToFragment(Fragment fragment, boolean addToBackStack) {
    if (!isActivityDestroyed && fragment != null) {
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.main_view_sensor_holder, fragment);
      if (addToBackStack) {
        fragmentTransaction.addToBackStack("back_fragment");
      }
      fragmentTransaction.commitAllowingStateLoss();
    }
  }

  public void switchToChooseCameraListFragment() {
    // If Add sensor functionality started from inside the Selected camera, then no need to select camera
    if (!isFromCameraDetails && mChooseCameraListFragment != null) {
      List<Device> mF86Devices = new ArrayList<>();
      Log.d(TAG, "Switching to choose camera in list");
      List<Device> mDevices = DeviceSingleton.getInstance().getDevices();
      for (Device mDevice : mDevices) {
        String uuid = mDevice.getProfile().getRegistrationId();
        if (uuid.startsWith("010086")) {
          if (mDevice.getProfile().isAvailable()) {
            mF86Devices.add(mDevice);
            Log.d(TAG, "Choose camera list " + uuid);
          } else {
            Log.d(TAG, "Dont choose camera list " + uuid);
          }
        }
      }
      if (mF86Devices.size() > 0) {
        isOnBackPressFinish = true;
        mChooseCameraListFragment.setDevices(mF86Devices);
        switchToFragment(mChooseCameraListFragment, false);
      } else {
        switchToCamerasNotFoundFragment();
      }
    } else {
      Device device = DeviceSingleton.getInstance().getSelectedDevice();
      setSelectedCameraDevice(device);
      switchToSetupTagSensorFragment();
    }
  }

  public void switchToCamerasNotFoundFragment() {
    isOnBackPressFinish = true;
    switchToFragment(mCamerasNotFoundFragment, false);
  }

  public void switchToSetupTagSensorFragment() {
    if (mSetupTagSensorFragment != null) {
      isOnBackPressFinish = false;
      switchToFragment(mSetupTagSensorFragment, false);
    }
  }

  public void switchToFoundMotionSensorFragment(BluetoothDevice mBluetoothDevice) {
    if (mFoundMotionSensorFragment != null) {
      isOnBackPressFinish = false;
      setBluetoothDevice(mBluetoothDevice);
      switchToFragment(mFoundMotionSensorFragment, false);
    }
  }

  public void switchToSettingTagAsMotionSensorFragment() {
    if (mSettingTagAsMotionSensorFragment != null) {
      isOnBackPressFinish = false;
      Bundle args = new Bundle();
      args.putString("name_sensor", mSensorName);
      mSettingTagAsMotionSensorFragment.setArguments(args);
      switchToFragment(mSettingTagAsMotionSensorFragment, false);
    }
  }

  public void switchToSensorErrorPage(String mailTile, String subTitle) {
//    if (mSensorRegistrationId != null) {
//      Log.d(TAG, "Add moto tag failed, remove " + mSensorRegistrationId);
//      String savedToken = Global.getApiKey(this);
//      RemoveCameraTask removeTagTask = new RemoveCameraTask();
//      removeTagTask.execute(savedToken, mSensorRegistrationId);
//    }

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

  public void switchToInstructionStickToDoorFragment() {
    if (mInstructionStickToDoorFragment != null) {
      isOnBackPressFinish = false;
      switchToFragment(mInstructionStickToDoorFragment, true);
    }
  }

  public void switchToAfterFixingMotionTagFragment() {
    if (mAfterFixingMotionTagFragment != null) {
      isOnBackPressFinish = false;
      switchToFragment(mAfterFixingMotionTagFragment, false);
    }
  }

  public String getSensorType() {
    return mSensorType;
  }

  public void setSensorType(String sensorType) {
    mSensorType = sensorType;
  }

  public void setFirmwareVersion(String firmwareVersion) {
    Log.d(TAG, "Firmware Version: " + firmwareVersion);
    mFirmwareVersion = firmwareVersion;
  }

  public BluetoothLeService getBLEService() {
    return mBluetoothLeService;
  }

  public void addSensorAsyncTask() {
    if (mBluetoothDevice != null) {
      String savedToken = Global.getApiKey(this);

      mSensorRegistrationId = generateRegisterationID(mBluetoothDevice.getAddress());
      Log.d(TAG, "Adding the sensor tag: " + mSensorRegistrationId);

      int offset = TimeZone.getDefault().getRawOffset();
      String timeZone = String.format("%s%02d.%02d", offset >= 0 ? "+" : "-", Math.abs(offset) / 3600000, (Math.abs(offset) / 60000) % 60);

      // Show progress screen
      switchToNextAfterRegister();

      AddCameraTask try_addCam = new AddCameraTask();
      try_addCam.execute(savedToken, mSensorName, mSensorRegistrationId, mSensorType, mFirmwareVersion, timeZone, mSelectedCameraDevice.getProfile().getRegistrationId());
    } else {
      Log.d(TAG, "skip addSensorAsyncTask. mBluetoothDevice is null.");
      disconnectBleDevice();
      switchToSensorErrorPage(getString(R.string.sensor_registration_failed), getString(R.string.sensor_udid, mSensorRegistrationId));
    }
  }

  public void removeSensorTask() {
    String savedToken = Global.getApiKey(this);
    mSensorRegistrationId = generateRegisterationID(mBluetoothDevice.getAddress());
    Log.d(TAG, "Removing the sensor tag: " + mSensorRegistrationId);
    RemoveCameraTask removeTagTask = new RemoveCameraTask();
    removeTagTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, savedToken, mSensorRegistrationId);
    try {
      removeTagTask.get(30000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }

  protected void showLeaveSensorSetupWarning() {
    new AlertDialog.Builder(AddSensorActivity.this).setTitle(getString(R.string.leave_sensor_setup))
        .setMessage(getString(R.string.are_you_sure_cancel_sensor_setup))
        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            disconnectBleDevice();
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

  public void setSelectedCameraDevice(Device mSelectedCameraDevice) {
    this.mSelectedCameraDevice = mSelectedCameraDevice;
  }

  public Device getSelectedCameraDevice() {
    return this.mSelectedCameraDevice;
  }

  public void setBluetoothDevice(BluetoothDevice mBluetoothDevice) {
    this.mBluetoothDevice = mBluetoothDevice;
  }

  // To register sensor to the server
  class AddCameraTask extends AsyncTask<String, String, Boolean> {

    private String usrToken;
    private String mSensorName;
    private String registerId;
    private String deviceAccessibilityMode;
    private String deviceFwVersion;
    private String timeZone;
    private String parentId;
    private String message;

    private static final String PROGRESS_UPDATE_RETRY = "AddSensorRetrying";
    private static final String PROGRESS_UDPATE_DISCONNECT_BLE = "BleDisconnecting";

    @Override
    protected Boolean doInBackground(String... params) {

      usrToken = params[0];
      mSensorName = params[1];
      registerId = params[2];
      deviceAccessibilityMode = params[3];
      deviceFwVersion = params[4];
      timeZone = params[5];
      parentId = params[6];

      Log.d(TAG, "Start add MotoTag: " + mSensorName + ", mode: " + deviceAccessibilityMode +
              " to parent camera: " + mSelectedCameraDevice.getProfile().getName() + ", parent id: " + parentId);
      // Don't need to remove sensor before adding anymore. Server will do that automatically.
//      Log.d(TAG, "Remove tag before add");
//      removeSensorTask();
//
//      // Sleep a bit
//      try {
//        Thread.sleep(10 * 1000);
//      } catch (InterruptedException e) {
//      }

      String regIdRes = null;
      int retries = 3;
      boolean isSucceeded = false;
      do {
        try {
          AddNewDeviceResponse reg_res = base.hubble.meapi.Device.registerSensorDevice(usrToken, mSensorName, registerId, deviceAccessibilityMode, deviceFwVersion, timeZone, parentId);
          if (reg_res != null) {
            if (reg_res.getStatus() == HttpURLConnection.HTTP_OK) {
              if (reg_res.getResponseData() != null) {
                regIdRes = reg_res.getResponseData().getRegistrationID();
                if (regIdRes != null) {
                  isSucceeded = true;
                  // clear error message when registering successfully
                  message = null;
                  break;
                }
              }

            } else {
              Log.d(TAG, "Add sensor res code: " + reg_res.getStatus());
              message = "Error code: " + reg_res.getStatus() + ": " + reg_res.getCode() + ": " + reg_res.getMessage();
            }
          } else {
            message = getString(R.string.there_are_some_problems_contacting_the_server_please_try_again_later_);
          }
        } catch (Exception se) {
          message = getString(R.string.there_are_some_problems_contacting_the_server_please_try_again_later_);
          Log.e(TAG, Log.getStackTraceString(se));
        }

        publishProgress(PROGRESS_UPDATE_RETRY);
        Log.d(TAG, "Add sensor to server failed, retries: " + retries);

        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
      } while (--retries > 0 && isSucceeded == false && !isCancelled());

      /* 20151001 HOANG: Improve MotoTag setup.
       * DON'T disconnect BLE too soon. Just disconnect after sending add query DONE.
       * When app disconnect BLE from MotoTag, it will transition state immediately.
       * The camera could fail to find the MotoTag because it misses the advertisement duration of the MotoTag.
       */
      publishProgress(PROGRESS_UDPATE_DISCONNECT_BLE);
      Log.d(TAG, "Sensor registered isSucceeded? " + isSucceeded);
      boolean isSensorAdded = false;
      if (isSucceeded == true) {
        if (registerId.equals(regIdRes)) {
         /* try {
          //  DeviceSingleton.getInstance().reload(false);// ARUNA to recheck if needed
          } catch (Exception e) {
            e.printStackTrace();
          }*/

          final long endSensorPollingTime = System.currentTimeMillis() + 3 * 60000; // 3 min
          while (isSensorAdded == false && !isCancelled() && System.currentTimeMillis() < endSensorPollingTime) {
            try {
              // Check tag status on server
              Log.d(TAG, "Checking sensor status for tag: " + registerId);
              Models.ApiResponse<SerializableDeviceProfile> sensorInfoResponse =
                Api.getInstance().getService().getDeviceProfile(usrToken, registerId);
              if (sensorInfoResponse != null && sensorInfoResponse.getData() != null) {
                SerializableDeviceProfile sensorProfile = sensorInfoResponse.getData();
                String sensorStatus = sensorProfile.getStatus();
                Log.d(TAG, "Checking sensor status res? " + sensorStatus);
                if (sensorStatus != null && sensorStatus.equalsIgnoreCase("1")) {
                  isSensorAdded = true;
                  break;
                }
              } else {
                Log.d(TAG, "Checking sensor status failed!");
              }
            } catch (Exception e) {
              /*
               * 20151208: HOANG: AA-1284
               * Root cause: uncatch exception "retrofit.RetrofitError: 404 Not Found" from retrofit.
               * Add try catch here to make sure app doesn't crash when not found tag on server.
               */
              e.printStackTrace();
            }

            try {
              Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
          }
        }

        if (isSensorAdded == false) {
          Log.d(TAG, "Sensor is not online after setup, remove it");
          removeSensorTask();
        }
      }

      return isSensorAdded;
    }

    /* UI thread */
    protected void onPostExecute(Boolean result) {

      Log.d(TAG, "Sensor has been registered. Success? " + result);
      if (result) {
//        Log.d(TAG, "Sensor has been registered successfully. Going to Next Screen.");
//        mHandler = new Handler();
//        mHandler.postDelayed(sensorNotPairedRunnable, 60000);
//        switchToNextAfterRegister();
//        mHandler.removeCallbacks(sensorNotPairedRunnable);
        isSensorPairingNorified = true;
        if (mSettingTagAsMotionSensorFragment.isVisible())
          mSettingTagAsMotionSensorFragment.enableActivateNow();

        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD, AppEvents.TAG_SETUP+" : "+AppEvents.SUCCESS, AppEvents.TAG_SETUP_SUCCESS);
        ZaiusEvent setupEvt = new ZaiusEvent(AppEvents.DASHBOARD);
        setupEvt.action(AppEvents.TAG_SETUP+" : "+AppEvents.SUCCESS);
        try {
          ZaiusEventManager.getInstance().trackCustomEvent(setupEvt);
        } catch (ZaiusException e) {
          e.printStackTrace();
        }
      } else {
        if (message != null) {
          switchToSensorErrorPage(getString(R.string.sensor_registration_failed), getString(R.string.sensor_udid, registerId) + "\n" + message);
          GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,  AppEvents.TAG_SETUP+" : "+"failure :"+message, AppEvents.TAG_SETUP_FAILURE);
          ZaiusEvent setupEvt = new ZaiusEvent(AppEvents.DASHBOARD);
          setupEvt.action(AppEvents.TAG_SETUP+" : "+"failure :"+message);
          try {
            ZaiusEventManager.getInstance().trackCustomEvent(setupEvt);
          } catch (ZaiusException e) {
            e.printStackTrace();
          }
        } else {
          switchToSensorErrorPage(getString(R.string.camera_not_able_to_find_sensor_main, mSelectedCameraDevice.getProfile().getName()),
              getString(R.string.sensor_udid, registerId) + "\n" + getString(R.string.camera_not_able_to_find_sensor_sub));
        }
      }
    }

    @Override
    protected void onProgressUpdate(String... values) {
      String msg = values[0];
      Log.d(TAG, "Add MotoTag, update progress: " + values[0]);
      if (msg != null) {
        if (msg.equalsIgnoreCase(PROGRESS_UDPATE_DISCONNECT_BLE)) {
          Log.d(TAG, "Add MotoTag to server DONE, disconnected from BLE device now");
          disconnectBleDevice();
        } else if (msg.equalsIgnoreCase(PROGRESS_UPDATE_RETRY)) {
          if (mSettingTagAsMotionSensorFragment.isVisible()) {
            mSettingTagAsMotionSensorFragment.updateSensorSetupProgress(getString(R.string.registration_failed_retry));
          }
        }
      }
    }
  }

  class RemoveCameraTask extends AsyncTask<String, String, Boolean> {

    private String userToken;
    private String deviceId;

    @Override
    protected Boolean doInBackground(String... params) {
      userToken = params[0];
      deviceId = params[1];

      try {
        SimpleJsonResponse deleteRes = base.hubble.meapi.Device.delete(userToken, deviceId);
        if (deleteRes != null && deleteRes.getStatus() == HttpURLConnection.HTTP_OK) {
          Log.d(TAG, "Remove MotoTag succeeded!");
          GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD, AppEvents.REMOVE_TAG+" : "+AppEvents.SUCCESS, AppEvents.TAG_REMOVE_SUCCESS);
          ZaiusEvent removeTagEvt = new ZaiusEvent( AppEvents.DASHBOARD);
          removeTagEvt.action(AppEvents.REMOVE_TAG+" : "+AppEvents.SUCCESS);
          try {
            ZaiusEventManager.getInstance().trackCustomEvent(removeTagEvt);
          } catch (ZaiusException e) {
            e.printStackTrace();
          }
        } else {
          Log.d(TAG, "Remove MotoTag failed!");
          GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD, AppEvents.REMOVE_TAG+" : "+"failure : "+deleteRes.getMessage(), "removeFailure");
          ZaiusEvent removeTagEvt = new ZaiusEvent(AppEvents.DASHBOARD);
          removeTagEvt.action(AppEvents.REMOVE_TAG+" : "+"failure : "+deleteRes.getMessage());
          try {
            ZaiusEventManager.getInstance().trackCustomEvent(removeTagEvt);
          } catch (ZaiusException e) {
            e.printStackTrace();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      return null;
    }
  }

  private void switchToNextAfterRegister() {

    switchToSettingTagAsMotionSensorFragment();

  }

  private BroadcastReceiver mSensorPairedReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();
      String deviceRegistrationId = intent.getStringExtra(SensorConstants.EXTRA_DEVICE_REGISTRATION_ID);
      Log.d(TAG, "Receive sensor pair event, regId: " + deviceRegistrationId + ", action: " + action);
      switch (action) {
        case SensorConstants.ACTION_SENSOR_PAIRED_EVENT:
          if (deviceRegistrationId.equals(mSensorRegistrationId)) {
            mHandler.removeCallbacks(sensorNotPairedRunnable);
            isSensorPairingNorified = true;
            if (mSettingTagAsMotionSensorFragment.isVisible())
              mSettingTagAsMotionSensorFragment.enableActivateNow();
          } else {
          }
          break;
        case SensorConstants.ACTION_SENSOR_NOT_PAIRED_EVENT:
          if (deviceRegistrationId.equals(mSensorRegistrationId)) {
            mHandler.removeCallbacks(sensorNotPairedRunnable);
            switchToSensorErrorPage(getString(R.string.camera_not_able_to_find_sensor_main, mSelectedCameraDevice.getProfile().getName()),
                getString(R.string.camera_not_able_to_find_sensor_sub));
          } else {
          }
      }
    }
  };

  private Runnable sensorNotPairedRunnable = new Runnable() {
    @Override
    public void run() {
      switchToSensorErrorPage(getString(R.string.camera_not_able_to_find_sensor_main, mSelectedCameraDevice.getProfile().getName()),
          getString(R.string.camera_not_able_to_find_sensor_sub));
    }
  };

  public void setmSensorName(String mSensorName) {
    this.mSensorName = mSensorName;
  }

  // Generates the registration id of sensor from mac address
  public String generateRegisterationID(String mMacAddress) {
    String registrationId = new String("060001");
    mMacAddress = mMacAddress.replace(":", "");
    registrationId = registrationId.concat(mMacAddress);
    String lastEightChars = "00000000";

    registrationId = registrationId.concat(lastEightChars);
    registrationId = registrationId.toUpperCase();
    return registrationId;
  }


  public boolean isSensorPairingNorified() {
    return isSensorPairingNorified;
  }

  public boolean isFromCameraDetails() {
    return isFromCameraDetails;
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    isActivityDestroyed = true;
//    if (mSensorPairedReceiver != null)
//      unregisterReceiver(mSensorPairedReceiver);
    unbindService(mServiceConnection);
  }

  @Override
  public void onBackPressed() {
    if (isOnBackPressFinish)
      AddSensorActivity.super.onBackPressed();
    else
      showLeaveSensorSetupWarning();
  }
}
