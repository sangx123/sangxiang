package com.sensor.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.interfaces.IChangeNameCallBack;
import com.hubbleconnected.camera.R;
import com.sensor.bluetooth.BluetoothLeService;
import com.sensor.bluetooth.IntentAction;
import com.sensor.helper.DfuService;
import com.sensor.helper.UpdateFWTask;
import com.util.CommonUtil;

import java.io.File;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

import static com.hubble.registration.Util.getRootRecordingDirectory;

public class UpgradeTagSensorFragment extends Fragment implements IChangeNameCallBack {
    private static final String TAG = "SetupTagSensorFragment";
    private Activity activity;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothLeService mBluetoothLeService;
    private String mStatus;
    private View view;
    //  private ProgressBar mProgressBar;
    private String mAddress, mDiscoveredDeviceAddress;
    private ScrollView mSetupSensorLayoutView, mSelectSensorLayoutView;
    private Handler mHandler;

    private TextView mTextPercentage;
    private ProgressBar mProgressBar;
    private RelativeLayout layoutUpgrade;
    private String userToken;
    private SecureConfig settings = HubbleApplication.AppConfig;
    private String model;
    private String regId, fwVersion;
    private Button btnCancel;
    private RelativeLayout layoutCancel, mTagTypeSelectionLayout, mSetupTagProgressLayout;
    private Button isLedBliking;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(getString(R.string.ConnectToNetworkActivity_connecting));
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            Log.e("Mtag", "Disconnect");
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            Log.e("Mtag", "Complete");
            mTextPercentage.setText(getString(R.string.up_loading_fw_mtag, 100));
            layoutCancel.setVisibility(View.INVISIBLE);

            UpdateFWTask updateFW = new UpdateFWTask(getActivity(), UpgradeTagSensorFragment.this);
            updateFW.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, userToken, fwVersion, regId);

            mTextPercentage.setText(getString(R.string.LoginOrRegistrationActivity_conn_bms));
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            getActivity().finish();
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            Log.e("Mtag", "Changed");
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(percent);
            mTextPercentage.setText(getString(R.string.up_loading_fw_mtag, percent));
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            Log.e("Mtag", "Error");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((UpgradeSensorActivity) activity).switchToSensorErrorPage(getString(R.string.can_not_connect_mototag_main), getString(R.string.update_fw_version_mtag_sub));
                }
            }, 1000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setup_tag_sensor, container, false);
        mSetupSensorLayoutView = (ScrollView) findViewById(R.id.setup_sensor__layoutview);
        //mSelectSensorLayoutView = (ScrollView) findViewById(R.id.select_sensor_layoutview);
        mTagTypeSelectionLayout = (RelativeLayout) findViewById(R.id.tag_type_selection);
        mSetupTagProgressLayout = (RelativeLayout) findViewById(R.id.setup_tag_progress);

        isLedBliking = (Button) findViewById(R.id.isledblinking);

       // mSelectSensorLayoutView.setVisibility(View.GONE);
        mTagTypeSelectionLayout.setVisibility(View.GONE);
        mSetupTagProgressLayout.setVisibility(View.GONE);
        mSetupSensorLayoutView.setVisibility(View.VISIBLE);

        userToken = settings.getString(PublicDefine.PREFS_SAVED_PORTAL_TOKEN, null);

        mTextPercentage = (TextView) findViewById(R.id.textviewProgress);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_file);
        layoutUpgrade = (RelativeLayout) findViewById(R.id.layout_upload_fw);
        btnCancel = (Button) findViewById(R.id.btn_cancel_upload);
        layoutCancel = (RelativeLayout) findViewById(R.id.layout_cancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextPercentage.setText(getString(R.string.cancelling_));
                mProgressBar.setIndeterminate(true);
                uploadCancel();
            }
        });


        Bundle bundle = getArguments();
        if (bundle != null) {
            fwVersion = bundle.getString(UpgradeSensorActivity.FW_NAME);
            regId = bundle.getString(UpgradeSensorActivity.REG_ID);
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        mHandler = new Handler();

        isLedBliking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetupSensorLayoutView.setVisibility(View.GONE);
                mSetupTagProgressLayout.setVisibility(View.VISIBLE);
                ImageView setupRing = (ImageView) findViewById(R.id.setup_ring) ;
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.image_rotate);
                setupRing.startAnimation(animation);

                mBluetoothLeService = ((UpgradeSensorActivity) activity).getBLEService();
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    if (mBluetoothLeService != null) {
                        mBluetoothLeService.scanLeDevice(true);
                    } else {
                        Log.d(TAG, "postDelayed startScanning");
                        mHandler.postDelayed(startScanning, 100);
                    }
                }
                activity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            }
        });
        return view;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
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

                    model = device.getName();
                    if (model != null && model.length() > 5) {
                        model = model.substring(0, 5);
                    }
                    Device selectedFocusTag = DeviceSingleton.getInstance().getDeviceByRegId(regId);
                    String selectedTagAdd = CommonUtil.formatMacAddress(selectedFocusTag.getProfile().getMacAddress(), getActivity());
                    String tagAddress = device.getAddress();
                    String advtHex = bytesToHex(scanRecord);
                    Log.d(TAG, "advtHex " + advtHex);
                    if (model != null && model.equals("FTAG2") && selectedTagAdd.equalsIgnoreCase(tagAddress) && !TextUtils.isEmpty(advtHex) &&
                            advtHex.toUpperCase().contains("FF720101")) {
                        if (mBluetoothLeService != null) {
                            mBluetoothLeService.scanLeDevice(false);
                            uploadFirmWare();
                            layoutUpgrade.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case IntentAction.ACTION_DEVICE_NOT_DISCOVERED:
                    mStatus += IntentAction.ACTION_DEVICE_NOT_DISCOVERED + "\n";
                    ((UpgradeSensorActivity) activity).switchToSensorErrorPage(getString(R.string.can_not_find_mototag_main), getString(R.string.can_not_find_mototag_sub));
                    break;
            }

        }
    };

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

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

        DfuServiceListenerHelper.registerProgressListener(getActivity(), mDfuProgressListener);

  /*  mBluetoothLeService = ((UpgradeSensorActivity) activity).getBLEService();
    if (!mBluetoothAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    } else {
      if (mBluetoothLeService != null) {
        mBluetoothLeService.scanLeDevice(true);
      } else {
        Log.d(TAG, "postDelayed startScanning");
        mHandler.postDelayed(startScanning, 100);
      }
    }
    activity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());*/
    }

    Runnable startScanning = new Runnable() {
        @Override
        public void run() {
            mBluetoothLeService = ((UpgradeSensorActivity) activity).getBLEService();
            if (mBluetoothLeService != null) {
                mBluetoothLeService.scanLeDevice(true);
            } else {
                mHandler.postDelayed(startScanning, 100);
            }
        }
    };

    private void showAlertDialogSuccess(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog));

        String contentTitle = getString(R.string.update_to_server);
        builder.setCancelable(false);
        builder.setMessage(message).
                setTitle(Html.fromHtml("<font color='#000000'>" + contentTitle + "</font>"));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                settings.putString("Mtag_" + regId, fwVersion);
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

    public void uploadFirmWare() {
        final DfuServiceInitiator starter = new DfuServiceInitiator(mAddress)
                .setDeviceName(model)
                .setKeepBond(false);

        //  File newFile = new File(getRootRecordingDirectory() + File.separator + "fw" + File.separator + "/0001-" + fwVersion + ".zip");
        //   File tempPath = new File(getActivity().getExternalCacheDir().getAbsolutePath() + "/0001-" + fwVersion + ".zip");
        starter.setZip(null, getRootRecordingDirectory() + File.separator + "fw" + File.separator + "/0001-" + fwVersion + ".zip");//getActivity().getExternalCacheDir().getAbsolutePath() + "/0001-" + fwVersion + ".zip");
        starter.start(getActivity(), DfuService.class);
    }

    private boolean isDfuServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(getActivity().ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DfuService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void update_cam_success() {
        layoutUpgrade.setVisibility(View.GONE);
        showAlertDialogSuccess(getString(R.string.success));
    }

    @Override
    public void update_cam_failed() {
        mTextPercentage.setText(getString(R.string.error));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((UpgradeSensorActivity) activity).switchToSensorErrorPage(getString(R.string.update_fw_version_mtag_main), getString(R.string.update_fw_version_mtag_sub));
            }
        }, 1000);
    }

    private void uploadCancel() {
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
        manager.sendBroadcast(pauseAction);
    }
}
