package com.sensor.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.Global;
import com.sensor.bluetooth.BluetoothLeService;
import com.sensor.bluetooth.GattInfo;
import com.sensor.bluetooth.IntentAction;
import com.sensor.constants.ProfileInfo;
import com.sensor.constants.SensorConstants;

import java.util.List;

import base.hubble.Models;

public class SetupTagSensorFragment extends Fragment implements VerifyDevicesTask.IVerifyDevicesCallback {
  private static final String TAG = "SetupTagSensorFragment";
  private Activity activity;
  private BluetoothAdapter mBluetoothAdapter;
  private static final int REQUEST_ENABLE_BT = 1;
  private BluetoothLeService mBluetoothLeService;
  private String mStatus;
  private View view;
  private ProgressBar mProgressBar;
  private BluetoothDevice mBluetoothDevice;
  private String mAddress, mDiscoveredDeviceAddress;
  private ScrollView mSetupSensorLayoutView;
  private LinearLayout mPresenceLinearLayout;
  private LinearLayout mDoorMotionLinearLayout;
  private Handler mHandler;
  private VerifyDevicesTask mVerifyDevicesTask;
  private Button isLedBlinking;
  private RelativeLayout mSetupTagProgressLayout, mSelectSensorTypeLayout;
  private ImageButton mPresenceSensor, mDoorSensor;
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_setup_tag_sensor, container, false);
    isLedBlinking = (Button) findViewById(R.id.isledblinking);

    mSetupSensorLayoutView = (ScrollView) findViewById(R.id.setup_sensor__layoutview);

    mSetupTagProgressLayout = (RelativeLayout) findViewById(R.id.setup_tag_progress);
    mSelectSensorTypeLayout = (RelativeLayout) findViewById(R.id.tag_type_selection);

    mPresenceSensor = (ImageButton) findViewById(R.id.presence);
    mDoorSensor = (ImageButton) findViewById(R.id.door);


    mSetupSensorLayoutView.setVisibility(View.GONE);

    mSetupTagProgressLayout.setVisibility(View.GONE);

    //mProgressBar = (ProgressBar) findViewById(R.id.tag_sensor_progress_bar);

    /*mPresenceLinearLayout = (LinearLayout) findViewById(R.id.presence_detection_layout);
    mDoorMotionLinearLayout = (LinearLayout) findViewById(R.id.door_motion_detection_layout);*/

    mPresenceSensor.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((AddSensorActivity) activity).setSensorType(SensorConstants.PRESENCE_DETECTION);
        mSelectSensorTypeLayout.setVisibility(View.GONE);
        mSetupSensorLayoutView.setVisibility(View.VISIBLE);
        if (mBluetoothLeService != null) {
          mBluetoothLeService.writeProfile(ProfileInfo.PRESENCE);
        }
      }
    });

    mDoorSensor.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((AddSensorActivity) activity).setSensorType(SensorConstants.MOTION_DETECTION);
        mSelectSensorTypeLayout.setVisibility(View.GONE);
        mSetupSensorLayoutView.setVisibility(View.VISIBLE);
        if (mBluetoothLeService != null) {
          mBluetoothLeService.writeProfile(ProfileInfo.MOTION);
        }
      }
    });

   isLedBlinking.setOnClickListener(new View.OnClickListener(){
     @Override
     public void onClick(View v) {
       mSetupSensorLayoutView.setVisibility(View.GONE);
       mSetupTagProgressLayout.setVisibility(View.VISIBLE);
       ImageView setupRing = (ImageView) findViewById(R.id.setup_ring) ;
       Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.image_rotate);
       setupRing.startAnimation(animation);

       if (mBluetoothLeService != null) {
         mBluetoothLeService.scanLeDevice(true);
       } else {
         Log.d(TAG, "postDelayed startScanning");
         mHandler.postDelayed(startScanning, 100);
       }
     }
   });
    final BluetoothManager bluetoothManager =
        (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
      mBluetoothAdapter = bluetoothManager.getAdapter();
    } else {
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    mHandler = new Handler();
    return view;
  }

  public static IntentFilter makeGattUpdateIntentFilter() {
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(IntentAction.ACTION_GATT_CONNECTED);
    intentFilter.addAction(IntentAction.ACTION_GATT_DISCONNECTED);
    intentFilter.addAction(IntentAction.ACTION_GATT_SERVICES_DISCOVERED);
    intentFilter.addAction(IntentAction.ACTION_DEVICE_DISCOVERED);
    intentFilter.addAction(IntentAction.ACTION_DEVICE_NOT_DISCOVERED);
    intentFilter.addAction(IntentAction.ACTION_DATA_NOTIFY);
    intentFilter.addAction(IntentAction.ACTION_DATA_READ);
    intentFilter.addAction(IntentAction.ACTION_DATA_WRITE);
    return intentFilter;
  }

  private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();
      mStatus = "";
      String uuid = intent.getStringExtra(IntentAction.EXTRA_UUID);
      switch (action) {
        case IntentAction.ACTION_DEVICE_DISCOVERED:
          byte[] scanRecord = intent.getByteArrayExtra(IntentAction.EXTRA_DATA);
          int rssi = intent.getIntExtra(IntentAction.EXTRA_RSSI, 0);
          mAddress = intent.getStringExtra(IntentAction.EXTRA_ADDRESS);
          BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mAddress);

          String model = device.getName();
          if (model != null && model.length() > 5) {
            model = model.substring(0, 5);
          }
          String advtHex = SensorUtils.bytesToHex(scanRecord);
          Log.d(TAG, "advtHex " + advtHex);
          // Check for valid Gecko and then connect
          if (model != null && model.equals("FTAG2") && rssi > -85 && !TextUtils.isEmpty(advtHex) &&
              advtHex.toUpperCase().contains("FF720101")) {
            if (mBluetoothLeService != null) {
              mBluetoothLeService.scanLeDevice(false);
              mDiscoveredDeviceAddress = mAddress;
//              String macAddress = mAddress.replace(":", "");

              String registrationId = ((AddSensorActivity) activity).generateRegisterationID(mDiscoveredDeviceAddress);
              String sessionToken = Global.getApiKey(getActivity());
              Log.d(TAG, "Registration id for validation " + registrationId);
              // Validating sensor
              mVerifyDevicesTask = new VerifyDevicesTask(SetupTagSensorFragment.this);
              mVerifyDevicesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sessionToken, registrationId);
            }
          }
          break;
        case IntentAction.ACTION_GATT_CONNECTED:
          mStatus += IntentAction.ACTION_GATT_CONNECTED + "\n";
          Log.d(TAG, mStatus);
          break;
        case IntentAction.ACTION_GATT_SERVICES_DISCOVERED:
          mStatus += IntentAction.ACTION_GATT_SERVICES_DISCOVERED + "\n";
          Log.d(TAG, mStatus);
          if (mBluetoothLeService != null) {
            mHandler.postDelayed(readFirmwareVersion, 100);
          }
          break;
        case IntentAction.ACTION_GATT_DISCONNECTED:
          mStatus += IntentAction.ACTION_GATT_DISCONNECTED + "\n";
          Log.d(TAG, mStatus);
          break;
        case IntentAction.ACTION_DATA_NOTIFY:
          mStatus += IntentAction.ACTION_DATA_NOTIFY + "\n";
          Log.d(TAG, mStatus);
          break;
        case IntentAction.ACTION_DATA_READ:
          mStatus += IntentAction.ACTION_DATA_READ + "\n";
          Log.d(TAG, mStatus);
          if (GattInfo.FIRMWARE_VERSION_UUID.toString().equals(uuid)) {
            byte[] firmwareValue = intent.getByteArrayExtra(IntentAction.EXTRA_DATA);
            String firmwareRevisionString = null;
            try {
              firmwareRevisionString = new String(firmwareValue, "UTF-8").trim();
              ((AddSensorActivity) activity).setFirmwareVersion(firmwareRevisionString);
            } catch (Exception e) {
              e.printStackTrace();
            }
            mSetupSensorLayoutView.setVisibility(View.GONE);
            mSetupTagProgressLayout.setVisibility(View.GONE);

            String sensorType =  ((AddSensorActivity) activity).getSensorType();


            if (mBluetoothLeService != null) {
              if(sensorType.equalsIgnoreCase(SensorConstants.PRESENCE_DETECTION))
                 mBluetoothLeService.writeProfile(ProfileInfo.PRESENCE);
              else
                mBluetoothLeService.writeProfile(ProfileInfo.MOTION);
            }
          }
          break;
        case IntentAction.ACTION_DATA_WRITE:
          //ARUNA
          mStatus += IntentAction.ACTION_DATA_WRITE + "\n";
          Log.d(TAG, mStatus);
          int status = intent.getIntExtra(IntentAction.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);

          byte[] value = intent.getByteArrayExtra(IntentAction.EXTRA_DATA);
//          if (mBluetoothLeService != null && status == BluetoothGatt.GATT_SUCCESS && GattInfo.GECKO_PROFILE_ALERT_CONFIGUTATION.toString().equals(uuid)) {
//            mHandler.postDelayed(mDisconnectDevice, 1000);
//          } else {
//            //TODO:Handle the gatt error case
//          }
          
          try {
            if (mBluetoothDevice == null) {
              Log.i(TAG, "Bluetooth device is null, get it from adapter");
              mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDiscoveredDeviceAddress);
            }
            ((AddSensorActivity) activity).switchToFoundMotionSensorFragment(mBluetoothDevice);
          } catch (Exception ex) {
            ex.printStackTrace();
            ((AddSensorActivity) activity).switchToSensorErrorPage(getString(R.string.can_not_find_mototag_main), getString(R.string.can_not_find_mototag_sub));
          }

          break;
        case IntentAction.ACTION_DEVICE_NOT_DISCOVERED:
          mStatus += IntentAction.ACTION_DEVICE_NOT_DISCOVERED + "\n";
          ((AddSensorActivity) activity).switchToSensorErrorPage(getString(R.string.can_not_find_mototag_main), getString(R.string.can_not_find_mototag_sub));
          break;
      }

    }
  };
  Runnable mDisconnectDevice = new Runnable() {
    @Override
    public void run() {
      if (mBluetoothLeService != null) {
        Log.d(TAG, "postDelayed mDisconnectDevice");
        mBluetoothLeService.disconnect();
        mBluetoothLeService.close();
      }
    }
  };

  Runnable readFirmwareVersion = new Runnable() {
    @Override
    public void run() {
      if (mBluetoothLeService != null) {
        Log.d(TAG, "postDelayed readFirmwareVersion");
        mBluetoothLeService.readFirmwareVersion();
      }
    }
  };

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // User chose not to enable Bluetooth.
    if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
      activity.finish();
      return;
    } else {
      if (mBluetoothLeService != null) {
        mBluetoothLeService.scanLeDevice(true);
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private View findViewById(int id) {
    return view.findViewById(id);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    mBluetoothLeService = ((AddSensorActivity) activity).getBLEService();
    if (!mBluetoothAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    } else {
      /*if (mBluetoothLeService != null) {
        mBluetoothLeService.scanLeDevice(true);
      } else {
        Log.d(TAG, "postDelayed startScanning");
        mHandler.postDelayed(startScanning, 100);
      }*/
    }
    activity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
  }

  Runnable startScanning = new Runnable() {
    @Override
    public void run() {
      mBluetoothLeService = ((AddSensorActivity) activity).getBLEService();
      if (mBluetoothLeService != null) {
        mBluetoothLeService.scanLeDevice(true);
      } else {
        mHandler.postDelayed(startScanning, 100);
      }
    }
  };

  private void errorServerPage(String message) {
    ((AddSensorActivity) activity).switchToSensorErrorPage(null, message);
  }

  private void showAlertDialogOk(String message) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(
        new ContextThemeWrapper(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog));

    String contentTitle = getResources().getString(R.string.oops);
    builder.setCancelable(false);
    builder.setMessage(message).
        setTitle(Html.fromHtml("<font color='#000000'>" + contentTitle + "</font>"));
    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        activity.finish();
      }
    });
    builder.create();
    builder.show();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mGattUpdateReceiver != null) {
      activity.unregisterReceiver(mGattUpdateReceiver);
    }
    if (mBluetoothLeService != null) {
      mBluetoothLeService.scanLeDevice(false);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (mBluetoothLeService != null) {
      mBluetoothLeService.scanLeDevice(false);
    }
    if (mHandler != null) {
      mHandler.removeCallbacks(startScanning);
    }
  }

  @Override
  public void onVerifyDevicesCompleted(Models.ApiResponse<List<Models.VerifyDeviceData>> verifyDeviceResponse) {

    if (verifyDeviceResponse != null) {
      String message = "";
      int status = -1;
      try {
        status = Integer.parseInt(verifyDeviceResponse.getStatus());
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
      Log.d(TAG, "onVerifyDevicesCompleted status? " + status);
      if (status == 200) {
        List<Models.VerifyDeviceData> verifyDeviceDatas = verifyDeviceResponse.getData();
        if (verifyDeviceDatas != null && verifyDeviceDatas.size() > 0) {
          Models.VerifyDeviceData data = verifyDeviceDatas.get(0);
          if (data != null) {
            String registrationId = data.getRegistrationId();
            boolean isValid = data.isValid();
            Log.d(TAG, "DEVICE_VALIDATE registrationId: " + registrationId);
            Log.d(TAG, "DEVICE_VALIDATE isValid? " + isValid);
            if (isValid == true) {
              if (mBluetoothLeService != null) {
                mBluetoothLeService.scanLeDevice(false);
                mBluetoothLeService.connect(mDiscoveredDeviceAddress);
                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDiscoveredDeviceAddress);
                mStatus += IntentAction.ACTION_DEVICE_DISCOVERED + "\n" + "Connecting........" + mDiscoveredDeviceAddress + "\n";
                Log.d(TAG, mStatus);
              }
            } else {
              message = data.getMessages().toString();
              Log.d(TAG, message);
              ((AddSensorActivity) activity).switchToSensorErrorPage(getString(R.string.sensor_registration_failed),
                  getString(R.string.sensor_udid, registrationId) + "\n" + getString(R.string.device_is_not_present_in_device_master));
            }
          } else {
            Log.d(TAG, "Verify device data is null");
            errorServerPage(getString(R.string.there_are_some_problems_contacting_the_server_please_try_again_later_));
          }
        } else {
          Log.d(TAG, "Verify device data is empty");
          errorServerPage(getString(R.string.there_are_some_problems_contacting_the_server_please_try_again_later_));
        }
      } else {
        message = verifyDeviceResponse.getMessage();
        Log.d(TAG, message);
        ((AddSensorActivity) activity).switchToSensorErrorPage(null, message);
      }
    } else {
      errorServerPage(getString(R.string.there_are_some_problems_contacting_the_server_please_try_again_later_));
    }
  }
}
