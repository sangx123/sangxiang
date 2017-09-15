package com.sensor.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.dialog.HubbleDialogFactory;
import com.hubble.registration.Util;
import com.hubble.registration.interfaces.IChangeNameCallBack;
import com.hubble.registration.tasks.ChangeNameTask;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubble.registration.tasks.CheckFirmwareUpdateTask;
import com.hubble.registration.tasks.RemoveDeviceTask;
import com.hubble.registration.ui.CommonDialogListener;
import com.hubble.ui.DebugFragment;
import com.hubble.util.BLEUtil;
import com.hubble.util.CommandUtils;

import com.nxcomm.blinkhd.ui.Global;
import com.nxcomm.blinkhd.ui.IUpgradeCallback;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.nxcomm.blinkhd.ui.dialog.UpgradeDialog;
import com.sensor.bluetooth.BluetoothLeService;
import com.sensor.bluetooth.GattInfo;
import com.sensor.dialog.PairSensorDialog;
import com.sensor.dialog.SensorSettingDialog;
import com.util.CommonUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import base.hubble.IAsyncTaskCommonHandler;
import base.hubble.PublicDefineGlob;
import base.hubble.database.DeviceProfile;
//import sensorupgrade.hubble.IntentAction;
//import sensorupgrade.hubble.SensorUpgradeActivity;
import com.hubbleconnected.camera.R;

public class SensorDetailsFragment extends Fragment implements IChangeNameCallBack, ISensorCommunicationListener {

  private static final String TAG = "SensorDetailsFragment";

  private static final String GET_TAG_BATTERY_LEVEL_CMD = "get_tag_battery_level";
  private static final String GET_TAG_BATTERY_LEVEL_PARAM1 = "&tag_id=";

  private static final int REQUEST_ENABLE_BT = 1;
  private Activity activity;
  private View view;
  private boolean mRemoveDialogShowing = false;
  private Device mSensorDevice;
  private DeviceProfile mSensorDeviceProfile;
  private String apiKey;
  private String mLatestFirmWare;
  private String urlStringA, urlStringB;

  private Handler mHandler;
  private ViewHolder mHolder = new ViewHolder();
  private SecureConfig settings = HubbleApplication.AppConfig;
  private Dialog mAlertDlg = null;
  private Dialog mChangeNameDlg = null;
  private String mNewSensorName = null;
  private ProgressDialog checkFirmwareDialog;
  private PairSensorDialog mPairSensorDialog = null;
  private BluetoothLeService mBleService;
  private BluetoothAdapter mBluetoothAdapter;
  private BluetoothDevice mBluetoothDevice;
  private int mCurrSensitivityLevel = -1;
  private int mNewSensitivityLevel = -1;
  private SensorSettingDialog mSensitivitySettingDialog = null;
  private ProgressDialog mWaitingDialog = null;
  private Dialog mForceSensorUpgradeDlg = null;
  private boolean mIsFragmentPaused = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPairSensorDialog = new PairSensorDialog();
    String msg = getString(R.string.loading);
    mWaitingDialog = HubbleDialogFactory.createProgressDialog(getActivity(), msg, false, false);
    mSensitivitySettingDialog = new SensorSettingDialog();
    mSensitivitySettingDialog.setOnSeekbarChangedListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        mNewSensitivityLevel = progress;
        mSensitivitySettingDialog.setCurrentLevel(mNewSensitivityLevel);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
    mSensitivitySettingDialog.setOnImgHelpClickedListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showSensorSensitivityHelpDialog();
      }
    });
    mSensitivitySettingDialog.setCommonDialogListener(new CommonDialogListener() {
      @Override
      public void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog) {
        Log.i(TAG, "Changing sensor sensitivity, curr level: " + mCurrSensitivityLevel + ", new level: " + mNewSensitivityLevel);
        if (mNewSensitivityLevel != mCurrSensitivityLevel) {
          String msg = getString(R.string.updating);
          mWaitingDialog.setMessage(msg);
          setWaitingDialogEnabled(true);
          mBleService.writeData(GattInfo.GECKO_PROFILE, GattInfo.GECKO_PROFILE_ALERT_CONFIGUTATION, mNewSensitivityLevel);
        } else {
          Log.i(TAG, "Sensor sensitivity doesnot change, do nothing");
          dialog.dismiss();
          if (mBleService != null) {
            mBleService.disconnect();
          }
        }
      }

      @Override
      public void onDialogNegativeClick(android.support.v4.app.DialogFragment dialog) {
        dialog.dismiss();
        if (mBleService != null) {
          mBleService.disconnect();
        }
      }

      @Override
      public void onDialogNeutral(android.support.v4.app.DialogFragment dialog) {
        dialog.dismiss();
      }
    });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mBleService != null) {
      mBleService.disconnect();
    }
  }

  private void showSensorSensitivityHelpDialog() {
    if (getActivity() != null) {
      Dialog helpDialog = null;
      View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_sensor_sensitivity_setting_help, null);
      TextView mTvSubtitle = (TextView) view.findViewById(R.id.tv_subtitle);
      mTvSubtitle.setText(getSafeString(R.string.dialog_sensor_sensitivity_level_help_subtitle));
      ListView mLvLevel = (ListView) view.findViewById(R.id.lv_sensitivityLevel);
      ArrayAdapter<CharSequence> lvAdapter = ArrayAdapter.createFromResource(getActivity(),
          R.array.dialog_sensor_sensitivity_level_help_array, android.R.layout.simple_list_item_1);
      mLvLevel.setAdapter(lvAdapter);
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setView(view)
          .setCancelable(true)
          .setPositiveButton(getSafeString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              dialogInterface.dismiss();
            }
          });
      helpDialog = builder.create();
      helpDialog.setCanceledOnTouchOutside(false);
      try {
        helpDialog.show();
      } catch (Exception e) {
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    mHandler = new Handler();
    view = inflater.inflate(R.layout.fragment_sensor_details, container, false);
    setHasOptionsMenu(true);

    mLatestFirmWare = "GTAG2_P_03.00.08";
    urlStringA = "http://dev-gecko-resources.s3.amazonaws.com/device_models/GTAG2/firmware_releases/GTAG2_P_03.00.08/GTAG2_P_03.00.08_A.bin?AWSAccessKeyId=AKIAIDBFDZTAR2EB4KPQ&Expires=1435233521&Signature=bHiMfc4n7lu%2BtBBG%2F7Ms%2BOuF4RQ%3D";
    urlStringB = "http://dev-gecko-resources.s3.amazonaws.com/device_models/GTAG2/firmware_releases/GTAG2_P_03.00.08/GTAG2_P_03.00.08_B.bin?AWSAccessKeyId=AKIAIDBFDZTAR2EB4KPQ&Expires=1435233521&Signature=zoec1eLrDNXnbhOp6jeyuMOWdtQ%3D";

    mSensorDeviceProfile = mSensorDevice.getProfile();
    apiKey = Global.getApiKey(activity);
    mHolder.mSensorName = (TextView) findViewById(R.id.settings_sensor_name);
    mHolder.mLinearName = (RelativeLayout) findViewById(R.id.linear_sensor_name);
    mHolder.mSensorFirmware = (TextView) findViewById(R.id.settings_firm_ware_version);
    mHolder.mSensorModelName = (TextView) findViewById(R.id.settings_model_name);
    mHolder.mSensorRemove = (TextView) findViewById(R.id.settings_remove_sensor);
    mHolder.mMacAddress = (TextView) findViewById(R.id.settings_mac_address);
    mHolder.mUpgradeSensorVersion = (ImageView) findViewById(R.id.upgrade_sensor_version);
    mHolder.mFirmwareMtag = (RelativeLayout) findViewById(R.id.layout_firm_ware_mtag);
    mHolder.mSensorSensitivity = (TextView) findViewById(R.id.tv_sensor_sensitivity);
    mHolder.mLinearSensitivity = (LinearLayout) findViewById(R.id.layout_sensor_sensitivity);
    mHolder.mSensorSensitivityLine = (ImageView) findViewById(R.id.line_sensor_sensitivity);
    mHolder.batteryLevelValue = (TextView) findViewById(R.id.tv_battery_level_value);
    checkFirmwareDialog = new ProgressDialog(getActivity());
    checkFirmwareDialog.setCancelable(false);

    mHolder.mSensorRemove.setOnClickListener(mSensorRemoveOnClickListner);
    mHolder.mLinearName.setOnClickListener(mChangeNameOnClickListener);
    //mHolder.mUpgradeSensorVersion.setVisibility(View.INVISIBLE);
   /* mHolder.mUpgradeSensorVersion.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String bluetoothDeviceMac = getMacAddress(mSensorDeviceProfile.getMacAddress());
        Log.d(TAG, "Sensor mac address for upgrade : " + bluetoothDeviceMac);
        *//* Still does not support sensor upgrade feature *//*
        *//*
        Intent mIntent = new Intent(activity.getApplicationContext(), SensorUpgradeActivity.class);
        mIntent.putExtra(IntentAction.EXTRA_ADDRESS, bluetoothDeviceMac);
        mIntent.putExtra(IntentAction.EXTRA_ISFROMASSETS, false);
        mIntent.putExtra(IntentAction.EXTRA_LATEST_FIRMWARE, mLatestFirmWare);
        mIntent.putExtra(IntentAction.EXTRA_URLA, urlStringA);
        mIntent.putExtra(IntentAction.EXTRA_URLB, urlStringB);
        startActivity(mIntent);
        *//*
      }
    });*/

   mHolder.mFirmwareMtag.setOnClickListener(mFirmwareMtagUpgrade);

    String sensorMode = mSensorDevice.getProfile().getMode();
    if (BLEUtil.isSupportedBLE(HubbleApplication.AppContext)) {
      final BluetoothManager bluetoothManager =
          (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
        mBluetoothAdapter = bluetoothManager.getAdapter();
      } else {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      }

      if (sensorMode != null && sensorMode.equalsIgnoreCase("motion_detection")) {
        mHolder.mLinearSensitivity.setVisibility(View.VISIBLE);
       // mHolder.mSensorSensitivityLine.setVisibility(View.VISIBLE);
        mHolder.mSensorSensitivity.setOnClickListener(mOnSensorSensitivityOnClicked);
      } else {
        mHolder.mLinearSensitivity.setVisibility(View.GONE);
     //   mHolder.mSensorSensitivityLine.setVisibility(View.GONE);
      }
    } else {
      mHolder.mLinearSensitivity.setVisibility(View.GONE);
    //  mHolder.mSensorSensitivityLine.setVisibility(View.GONE);
    }
    updateViewData();

    return view;
  }

  private String getMacAddress(String deviceAddress) {
    String macAddress = "";
    int length = deviceAddress.length();
    for (int i = 0; i < deviceAddress.length(); i++) {
      macAddress += deviceAddress.charAt(i);
      if (i % 2 != 0 && i != length - 1) {
        macAddress += ":";
      }
    }
    return macAddress;
  }

  private void getTagBatteryLevel() {
    if (mHolder.batteryLevelValue != null) {
      mHolder.batteryLevelValue.setText(getSafeString(R.string.loading));
      Runnable runn = new Runnable() {
        @Override
        public void run() {
          int batteryRes = -1;
          if (mSensorDeviceProfile != null) {
            if (!TextUtils.isEmpty(mSensorDeviceProfile.getParentRegistrationId())) {
              String parentRegId = mSensorDeviceProfile.getParentRegistrationId();
              String getBatteryLevelCmd = GET_TAG_BATTERY_LEVEL_CMD + GET_TAG_BATTERY_LEVEL_PARAM1 +
                  mSensorDeviceProfile.getRegistrationId();
              int retries = 3;
              do {
                Log.i(TAG, "Sending get battery level command...");
                String res = null;
                res = CommandUtils.sendRemoteCommand(parentRegId, getBatteryLevelCmd);
                Log.i(TAG, "Send get battery level command DONE, res: " + res);
                if (res != null && res.startsWith(GET_TAG_BATTERY_LEVEL_CMD)) {
                  String batteryResStr = res.substring(GET_TAG_BATTERY_LEVEL_CMD.length() + 2);
                  if (!TextUtils.isEmpty(batteryResStr) && !batteryResStr.equalsIgnoreCase("-1")) {
                    try {
                      batteryRes = Integer.parseInt(batteryResStr);
                    } catch (NumberFormatException e) {
                      e.printStackTrace();
                    }

                    // check lower & upper bound for battery level because camera could return
                    // invalid value
                    if (batteryRes > 0) {
                      if (batteryRes > 100) {
                        batteryRes = 100;
                      }

                      break;
                    }
                  }
                }

                try {
                  Thread.sleep(2000);
                } catch (InterruptedException e) {
                }

                Log.e(TAG, "Invalid battery level, retries: " + retries);
              }
              while (--retries > 0 && mIsFragmentPaused == false);
            } else {
              Log.e(TAG, "Get battery level failed, sensor " + mSensorDeviceProfile.getRegistrationId() +
                  ", parentRegId: " + mSensorDeviceProfile.getParentRegistrationId());
            }
          } else {
            Log.e(TAG, "Sensor profile is null");
          }

          final int batteryFinal = batteryRes;
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              if (batteryFinal > 0) {
                mHolder.batteryLevelValue.setText(batteryFinal + "%");
              } else {
                Log.i(TAG, "Failed to retrieve tag battery level");
                mHolder.batteryLevelValue.setText(getSafeString(R.string.failed_to_retrieve_sensor_data));
              }
            }
          });

        }

      };
      Thread worker = new Thread(runn);
      worker.start();

    } else {
      Log.e(TAG, "Sensor battery layout doesn't exist");
    }
  }

  private void updateViewData() {
    mHolder.mSensorName.setText(mSensorDeviceProfile.getName());
    mHolder.mSensorModelName.setText(mSensorDeviceProfile.getModelId());
    mHolder.mMacAddress.setText(CommonUtil.formatMacAddress(mSensorDeviceProfile.getMacAddress(), getActivity()));
  }

  @Override
  public void onResume() {
    super.onResume();
    mIsFragmentPaused = false;

    String currentFw = mSensorDeviceProfile.getFirmwareVersion();
    String regid = mSensorDeviceProfile.getRegistrationId();
    String latestFw = settings.getString("Mtag_" + regid, "");

    if (!TextUtils.isEmpty(latestFw) && Util.isThisVersionGreaterThan(latestFw, currentFw)){
      mSensorDeviceProfile.setFirmwareVersion(latestFw);
      mHolder.mSensorFirmware.setText(latestFw);
      settings.remove("Mtag_" + regid);
    } else {
      mHolder.mSensorFirmware.setText(currentFw);
    }

    getTagBatteryLevel();

    // 20151117: hoang: AA-1184
    // Temporary comment it now because app still doesn't found a way to force
    // user to upgrade sensor firmware.
    // performCheckOtaInBackground();
  }

  @Override
  public void onPause() {
    super.onPause();
    mIsFragmentPaused = true;
  }

  private View.OnClickListener mSensorRemoveOnClickListner = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      showRemoveDialog();
    }
  };

  private View.OnClickListener mChangeNameOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      showChangeSensorNameDialog();
    }
  };

  private View.OnClickListener mFirmwareMtagUpgrade = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      if (!BLEUtil.isSupportedBLE(getActivity().getApplicationContext())) {
        showDonotSupportBLEDialog();
      } else {
        createCheckFirmwareUpdateTask();
      }
    }
  };

  private View.OnClickListener mOnSensorSensitivityOnClicked = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      mBleService = ((MainActivity) activity).getBLEService();
      if (!mBluetoothAdapter.isEnabled()) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      } else {
        if (mBleService != null) {
            mBleService.startLeScanning(true, SensorDetailsFragment.this);
            setPairSensorDialogEnabled(true);
        } else {
          Log.d(TAG, "postDelayed startScanning");
          mHandler.postDelayed(startScanning, 100);
          setPairSensorDialogEnabled(true);
        }
      }
    }
  };

  Runnable startScanning = new Runnable() {
    @Override
    public void run() {
      mBleService = ((AddSensorActivity) activity).getBLEService();
      if (mBleService != null) {
        mBleService.startLeScanning(true, SensorDetailsFragment.this);
      } else {
        mHandler.postDelayed(startScanning, 100);
      }
    }
  };

  private void setPairSensorDialogEnabled(boolean isShown) {
    if (isShown == true) {
      if (mPairSensorDialog != null) {
        try {
          mPairSensorDialog.show(getFragmentManager(), "");
        } catch (Exception e) {
        }
      }
    } else {
      if (mPairSensorDialog != null) {
        try {
          mPairSensorDialog.dismiss();
        } catch (Exception e) {
        }
      }
    }
  }

  private void setSensitivitySettingDialogEnabled(boolean isShown) {
    if (isShown) {
      if (mSensitivitySettingDialog != null && (mSensitivitySettingDialog.getDialog() == null ||
          !mSensitivitySettingDialog.getDialog().isShowing())) {
        try {
          mSensitivitySettingDialog.setCurrentLevel(mCurrSensitivityLevel);
          mSensitivitySettingDialog.show(getFragmentManager(), "");
        } catch (Exception e) {
        }
      }
    } else {
      if (mSensitivitySettingDialog != null && mSensitivitySettingDialog.getDialog() != null &&
          mSensitivitySettingDialog.getDialog().isShowing()) {
        try {
          mSensitivitySettingDialog.dismiss();
        } catch (Exception e) {
        }
      }
    }
  }

  private void setWaitingDialogEnabled(boolean isShown) {
    if (isShown) {
      if (!mWaitingDialog.isShowing()) {
        try {
          mWaitingDialog.show();
        } catch (Exception e) {
        }
      }
    } else {
      if (mWaitingDialog.isShowing()) {
        try {
          mWaitingDialog.dismiss();
        } catch (Exception e) {
        }
      }
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_sensor_details, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_delete_sensor && !mRemoveDialogShowing) {
      mRemoveDialogShowing = true;
      if (mSensorDevice != null) {
        showRemoveDialog();
      }
    }
    return super.onOptionsItemSelected(item);
  }

  private void showChangeSensorNameDialog() {
    if (getActivity() == null) {
      return;
    }

    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.dialog_edittext, (ViewGroup) getActivity().findViewById(R.id.dialog_edittext_root));

    final EditText editText = (EditText) layout.findViewById(R.id.dialog_edittext_edittext);
    // editText.setHint(getString(R.string.hint_for_camera_name));
    editText.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
    editText.setText(mSensorDevice.getProfile().getName());

    mAlertDlg = new AlertDialog.Builder(getActivity()).setTitle(getSafeString(R.string.change_sensor_name))
        .setMessage(getSafeString(R.string.enter_the_new_name_of_this_sensor))
        .setView(layout)
        .setPositiveButton(getSafeString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String newSensorName = editText.getText().toString().trim();
            if (newSensorName != null && !newSensorName.isEmpty() && !newSensorName.equals(mSensorDevice.getProfile().getName())) {
              mNewSensorName = newSensorName;
              mChangeNameDlg = ProgressDialog.show(getActivity(), null, getSafeString(R.string.changing_sensor_name), true, false);

              ChangeNameTask rename = new ChangeNameTask(getActivity(), SensorDetailsFragment.this);
              rename.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, apiKey, newSensorName, mSensorDevice.getProfile().getRegistrationId());
            }
          }
        })
        .setNegativeButton(getSafeString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        }).show();
  }

  private void showRemoveDialog() {
    if (getActivity() == null) {
      return;
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage(getSafeString(R.string.remove_sensor_confirm)).setPositiveButton(getSafeString(R.string.yes), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(final DialogInterface mDialog, int which) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getActivity().getString(R.string.removing_sensor));
        progressDialog.show();
        RemoveDeviceTask deleteDeviceTask = new RemoveDeviceTask(getActivity(), new RemoveDeviceTask.onDeleteTaskCompleted() {
          @Override
          public void onDeleteTaskCompleted(int result) {
            if (activity != null) {

              if (mDialog != null) {
                try {
                  mDialog.dismiss();
                } catch (Exception e) {
                }
                mRemoveDialogShowing = false;
              }
              if (result == RemoveDeviceTask.REMOVE_CAM_SUCCESS) {
                Log.e(TAG, "Remove sensor succeeded.");

                Handler mTimerHandler = new Handler();
                Runnable mTimerExecutor = new Runnable() {
                  @Override
                  public void run() {
                    try {
                      progressDialog.dismiss();
                    } catch (Exception e) {
                    }
                    alertDialogOkBack(R.string.sensor_removed);
                  }
                };
                mTimerHandler.postDelayed(mTimerExecutor, 2000);
              } else {
                try {
                  progressDialog.dismiss();
                } catch (Exception e) {
                }
                Log.e(TAG, "Remove sensor failed.");
                alertDialogOkBack(R.string.remove_sensor_failed);
              }
            }
          }
        });
        deleteDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mSensorDeviceProfile.getRegistrationId(), apiKey);
      }
    }).setNegativeButton(getSafeString(R.string.no), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        mRemoveDialogShowing = false;
      }
    });

    AlertDialog removeConfirmDialog = builder.create();
    removeConfirmDialog.setCancelable(true);
    removeConfirmDialog.setCanceledOnTouchOutside(false);
    removeConfirmDialog.setTitle(getSafeString(R.string.remove_sensor));
    removeConfirmDialog.show();
  }

  private void alertDialogOkBack(int stringId) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setMessage(stringId);
    builder.setCancelable(false);
    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        activity.onBackPressed();
      }
    });
    builder.setCancelable(false);
    builder.create().show();
  }

  public void setmSensorDevice(Device mSensorDevice) {
    this.mSensorDevice = mSensorDevice;
  }

  private View findViewById(int id) {
    return view.findViewById(id);
  }

  @Override
  public void update_cam_success() {
    Log.d(TAG, "Change sensor name success, new name: " + mNewSensorName);
    if (getActivity() != null) {
      if (mChangeNameDlg != null && mChangeNameDlg.isShowing()) {
        try {
          mChangeNameDlg.dismiss();
        } catch (Exception e) {
        }
      }

      mHolder.mSensorName.setText(mNewSensorName);
      mSensorDevice.getProfile().setName(mNewSensorName);
      Toast.makeText(getActivity(), getSafeString(R.string.sensor_name_changed), Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void update_cam_failed() {
    Log.d(TAG, "Change sensor name failed");
    if (getActivity() != null) {
      if (mChangeNameDlg != null && mChangeNameDlg.isShowing()) {
        try {
          mChangeNameDlg.dismiss();
        } catch (Exception e) {
        }
      }
      Toast.makeText(getActivity(), getSafeString(R.string.failed_to_change_sensor_name), Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // User chose not to enable Bluetooth.
    if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
      return;
    } else {
      if (mBleService != null) {
        mBleService.startLeScanning(true, this);
      }
      setPairSensorDialogEnabled(true);
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onDeviceDiscovered(BluetoothDevice device, int rssi, byte[] record) {
    Log.i(TAG, "onDeviceDiscovered: " + device.getAddress() + ", rssi: " + rssi + ", currentDevice: " + mSensorDevice.getProfile().getMacAddress());
    byte[] scanRecord = record;
    String mAddress = device.getAddress();
    mBluetoothDevice = device;

    if (device.getAddress().equals(PublicDefineGlob.add_colon_to_mac(mSensorDevice.getProfile().getMacAddress()))) {
      String model = device.getName();
      if (model != null && model.length() > 5) {
        model = model.substring(0, 5);
      }
      String advtHex = SensorUtils.bytesToHex(scanRecord);
      Log.d(TAG, "advtHex " + advtHex);
      // Check for valid Gecko and then connect
      if (model != null && model.equals("FTAG2") && rssi > -85 && !TextUtils.isEmpty(advtHex) &&
          advtHex.toUpperCase().contains("FF720101")) {
        if (mBleService != null) {
          mBleService.stopLeScanning();
          mBleService.connect(mAddress);
          mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mAddress);
          Log.i(TAG, "\n" + "Connecting........" + mAddress + "\n");
        }
      }
    }
  }

  @Override
  public void onGattConnected(String deviceAddress, int status) {
    Log.i(TAG, "onGattConnected: " + deviceAddress + ", status: " + status);
  }

  @Override
  public void onGattDisconnected(String deviceAddress, int status) {
    Log.i(TAG, "onGattDisconnected: " + deviceAddress + ", status: " + status);
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        setPairSensorDialogEnabled(false);
//        Toast connectFailedToast = Toast.makeText(getActivity(), getString(R.string.connect_to_sensor_failed), Toast.LENGTH_SHORT);
//        try {
//          connectFailedToast.show();
//        } catch (Exception e) {
//        }
      }
    });
  }

  @Override
  public void onGattServiceDiscovered(String deviceAddress, int status) {
    Log.i(TAG, "onGattServiceDiscovered: " + deviceAddress + ", status: " + status);
    setPairSensorDialogEnabled(false);
    mBleService.readData(GattInfo.GECKO_PROFILE, GattInfo.GECKO_PROFILE_ALERT_CONFIGUTATION);
  }

  @Override
  public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
    Log.i(TAG, "onCharacteristicChanged: " + characteristic);
  }

  @Override
  public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
    Log.i(TAG, "onCharacteristicRead: " + characteristic + ", status: " + status);
    if (GattInfo.GECKO_PROFILE_ALERT_CONFIGUTATION.equals(characteristic.getUuid())) {
      byte[] doorSensitivityValue = characteristic.getValue();
      Log.d(TAG, "current doorSensitivityValue: " + SensorUtils.bytesToHex(doorSensitivityValue));
      mCurrSensitivityLevel = doorSensitivityValue[1];
      mNewSensitivityLevel = mCurrSensitivityLevel;
    }

    if (getActivity() != null) {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          setWaitingDialogEnabled(false);

          // Update new door sensitivity value here
          mSensitivitySettingDialog.setCurrentLevel(mCurrSensitivityLevel);
          try {
            mSensitivitySettingDialog.show(getFragmentManager(), null);
          } catch (Exception e) {
          }
        }
      });

    }
  }

  @Override
  public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
    Log.i(TAG, "onCharacteristicWrite: " + characteristic + ", status: " + status);
    if (GattInfo.GECKO_PROFILE_ALERT_CONFIGUTATION.equals(characteristic.getUuid())) {
      byte[] doorSensitivityValue = characteristic.getValue();
      Log.d(TAG, "New doorSensitivityValue: " + SensorUtils.bytesToHex(doorSensitivityValue) + ", expected value: " + mNewSensitivityLevel);
      if (mNewSensitivityLevel == doorSensitivityValue[1]) {
        mCurrSensitivityLevel = doorSensitivityValue[1];

        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            Log.i(TAG, "Sensor sensitivity changed, disconnect device now");
            mBleService.disconnect();
            setSensitivitySettingDialogEnabled(false);
            setWaitingDialogEnabled(false);

            Toast sensitivityChangedToast = Toast.makeText(getActivity(), getSafeString(R.string.sensor_sensitivity_changed), Toast.LENGTH_SHORT);
            try {
              sensitivityChangedToast.show();
            } catch (Exception e) {
            }
          }
        }, 1000);
      } else {
        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            Log.i(TAG, "Sensor sensitivity change failed, disconnect device now");
            mBleService.disconnect();
            setSensitivitySettingDialogEnabled(false);
            setWaitingDialogEnabled(false);

            Toast failedToast = Toast.makeText(getActivity(), getSafeString(R.string.change_sensor_sensitivity_failed), Toast.LENGTH_SHORT);
            try {
              failedToast.show();
            } catch (Exception e) {
            }
          }
        }, 100);
      }
    }

  }

  @Override
  public void onDeviceNotDiscovered() {
    Log.i(TAG, "onDeviceNotDiscovered");
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        setPairSensorDialogEnabled(false);
        Toast connectFailedToast = Toast.makeText(getActivity(), getSafeString(R.string.cannot_find_your_mototag), Toast.LENGTH_SHORT);
        try {
          connectFailedToast.show();
        } catch (Exception e) {
        }
      }
    });
  }


  private class ViewHolder {
    private TextView mSensorName, mSensorFirmware, mSensorModelName, mSensorRemove, mMacAddress, mSensorSensitivity, batteryLevelValue;
    private ImageView mUpgradeSensorVersion, mSensorSensitivityLine;
    private RelativeLayout mLinearName,mFirmwareMtag;
    private LinearLayout mLinearSensitivity;
  }

  private void createCheckFirmwareUpdateTask() {
    showCheckFWDialog();
    String fwVersion = mSensorDevice.getProfile().getFirmwareVersion();
    String regId = mSensorDevice.getProfile().getRegistrationId();
    String modelId = mSensorDeviceProfile.getModelId();
    String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

    new CheckFirmwareUpdateTask(saved_token, regId, fwVersion, modelId, mSensorDevice, new IAsyncTaskCommonHandler() {
      @Override
      public void onPreExecute() {
      }

      @Override
      public void onPostExecute(final Object result) {
        if (result instanceof CheckFirmwareUpdateResult) {
          CheckFirmwareUpdateResult checkFirmwareUpdateResult = (CheckFirmwareUpdateResult) result;
          checkFirmwareUpdateResult.setLocalCamera(true);
          checkFirmwareUpdateResult.setInetAddress(mSensorDevice.getProfile().getDeviceLocation().getLocalIp());
          checkFirmwareUpdateResult.setApiKey(apiKey);
          checkFirmwareUpdateResult.setRegID(mSensorDevice.getProfile().getRegistrationId());
          hideCheckFWDialog();
          handleCheckFwUpdateResult(checkFirmwareUpdateResult);
        }
      }

      @Override
      public void onCancelled() {
      }
    }, settings.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false)).execute();
  }

  private void performCheckOtaInBackground() {
    String fwVersion = mSensorDevice.getProfile().getFirmwareVersion();
    String regId = mSensorDevice.getProfile().getRegistrationId();
    String modelId = mSensorDevice.getProfile().getModelId();
    String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
    boolean useDevOta = settings.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false);
    CheckFirmwareUpdateTask checkFirmwareUpdateTask = new CheckFirmwareUpdateTask(
        saved_token, regId, fwVersion, modelId, mSensorDevice, new IAsyncTaskCommonHandler() {
      @Override
      public void onPostExecute(Object result) {
        if (result instanceof CheckFirmwareUpdateResult) {
          CheckFirmwareUpdateResult checkFirmwareUpdateResult = (CheckFirmwareUpdateResult) result;
          checkFirmwareUpdateResult.setLocalCamera(true);
          checkFirmwareUpdateResult.setInetAddress(mSensorDevice.getProfile().getDeviceLocation().getLocalIp());
          checkFirmwareUpdateResult.setApiKey(apiKey);
          checkFirmwareUpdateResult.setRegID(mSensorDevice.getProfile().getRegistrationId());
          if (checkFirmwareUpdateResult.isHaveNewFirmwareVersion()) {
            showForceSensorUpgradeDialog(checkFirmwareUpdateResult);
          } else {
            Log.i(TAG, "Sensor firmware is up to date: " + mSensorDevice.getProfile().getFirmwareVersion());
          }
        }
      }

      @Override
      public void onPreExecute() {

      }

      @Override
      public void onCancelled() {

      }
    }, useDevOta);
    checkFirmwareUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  private void handleCheckFwUpdateResult(final CheckFirmwareUpdateResult result) {
    if (result.isHaveNewFirmwareVersion()) { // new firmware on OTA server
      showConfirmFWDialog(result);
    } else { // no new firmware on OTA server or OTA version < current version
      showFWDialog(String.format(getActivity().getString(R.string.mtag_no_fw_upgrade_found), result.getCurrentFirmwareVersion()));
    }
  }

  private void showFWDialog(String message) {
    AlertDialog fwDialog = null;
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    Spanned spannedMsg = Html.fromHtml("<big>" + message + "</big>");
    builder.setMessage(spannedMsg).setIcon(R.drawable.ic_launcher).setTitle(getActivity().getString(R.string.updating)).setPositiveButton(getActivity().getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(@NotNull DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            }
    );
    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        dialog.dismiss();
      }
    });
    fwDialog = builder.create();
    try {
      fwDialog.show();
    } catch (Exception e) {
    }
  }

  private void showCheckFWDialog() {
    if(checkFirmwareDialog !=null && !checkFirmwareDialog.isShowing()) {
      checkFirmwareDialog.setMessage(getString(R.string.check_fw_sensor));
      try {
        checkFirmwareDialog.show();
      } catch (Exception e) {
      }
    }
  }

  private void hideCheckFWDialog() {
    if(checkFirmwareDialog !=null && checkFirmwareDialog.isShowing()) {
      try {
        checkFirmwareDialog.dismiss();
      } catch (Exception e) {
      }
    }
  }

  private void showConfirmFWDialog(final CheckFirmwareUpdateResult result) {
    AlertDialog confirmDialog = null;
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    Spanned spannedMsg = Html.fromHtml("<big>" + getSafeString(R.string.mtag_firmware_upgrade_available) + "</big>");
    builder.setMessage(spannedMsg).setIcon(R.drawable.ic_launcher).setTitle(getSafeString(R.string.updating)).setPositiveButton(getSafeString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(@NotNull DialogInterface dialog, int which) {
                dialog.dismiss();
                result.setRequestUpgradeOnly(true);
                File fwFile = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/" + result.getNewFirmwareVersion() + ".zip");
                if (fwFile.exists()) {
                  Intent intent = new Intent(getActivity(), UpgradeSensorActivity.class);
                  intent.putExtra(UpgradeSensorActivity.REG_ID, mSensorDevice.getProfile().registrationId);
                  intent.putExtra(UpgradeSensorActivity.FW_NAME, result.getNewFirmwareVersion());

                  startActivity(intent);
                } else {
                  showUpdateDialog(result);
                }
              }
            }
    ).setNegativeButton(getSafeString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(@NotNull DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        dialog.dismiss();
      }
    });
    confirmDialog = builder.create();
    try {
      confirmDialog.show();
    } catch (Exception e) {
    }
  }

  private void showForceSensorUpgradeDialog(final CheckFirmwareUpdateResult result) {
    if (mForceSensorUpgradeDlg == null) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      Spanned spannedMsg = Html.fromHtml("<big>" + getSafeString(R.string.dialog_force_mtag_firmware_upgrade) + "</big>");
      String msg = getSafeString(R.string.dialog_force_mtag_firmware_upgrade);
      builder.setMessage(msg).setIcon(R.drawable.ic_launcher).setTitle(getSafeString(R.string.sensor_update)).setPositiveButton(getSafeString(R.string.proceed), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(@NotNull DialogInterface dialog, int which) {
              dialog.dismiss();
              result.setRequestUpgradeOnly(true);
              File fwFile = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/" + result.getNewFirmwareVersion() + ".zip");
              if (fwFile.exists()) {
                Intent intent = new Intent(getActivity(), UpgradeSensorActivity.class);
                intent.putExtra(UpgradeSensorActivity.REG_ID, mSensorDevice.getProfile().registrationId);
                intent.putExtra(UpgradeSensorActivity.FW_NAME, result.getNewFirmwareVersion());
                startActivity(intent);
              } else {
                showUpdateDialog(result);
              }
            }
          }
      ).setNegativeButton(getSafeString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(@NotNull DialogInterface dialog, int which) {
          dialog.dismiss();
          Log.i(TAG, "User doesn't want to upgrade sensor fw, back to camera list");
          if (getActivity() != null) {
            ((MainActivity)getActivity()).switchToDeviceList();
          }
        }
      });

      builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          dialog.dismiss();
        }
      });
      builder.setCancelable(false);
      mForceSensorUpgradeDlg = builder.create();
      mForceSensorUpgradeDlg.setCanceledOnTouchOutside(false);
    }

    if (mForceSensorUpgradeDlg != null && !mForceSensorUpgradeDlg.isShowing()) {
      try {
        mForceSensorUpgradeDlg.show();
      } catch (Exception e) {
      }
    }
  }

  private void showUpdateDialog(CheckFirmwareUpdateResult result) {
    UpgradeDialog upgradeDialog = new UpgradeDialog(mSensorDevice, result, new IUpgradeCallback() {
      @Override
      public void onUpgradeSucceed() {
      }

      @Override
      public void onUpgradeFail() {
      }
    });
    upgradeDialog.setCancelable(false);
    upgradeDialog.show(getActivity().getSupportFragmentManager(), "upgrade");
  }

  private void showDonotSupportBLEDialog() {
    AlertDialog dontSupportBleDlg = null;
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage(R.string.your_device_does_not_have_ble_support);
    builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
      }
    });
    dontSupportBleDlg = builder.create();
    try {
      dontSupportBleDlg.show();
    } catch (Exception e) {
    }
  }

  public static boolean setBluetooth(boolean enable) {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    boolean isEnabled = bluetoothAdapter.isEnabled();
    if (enable && !isEnabled) {
      return bluetoothAdapter.enable();
    } else if (!enable && isEnabled) {
      return bluetoothAdapter.disable();
    }

    return true;
  }

  private String getSafeString(int resourceId) {
    String str;
    if (getActivity() != null) {
      str = getActivity().getString(resourceId);
    } else {
      str = "";
    }
    return str;
  }
}
