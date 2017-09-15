package com.hubble.setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.BaseActivity;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.ISettings;
import com.hubble.framework.common.TransportMode;
import com.hubble.framework.common.exception.BaseException;
import com.hubble.framework.device.Configuration;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceAttribute;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceID;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceAttributeDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceDetail;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceDetailsResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceStatusDetails;
import com.hubble.framework.service.connectivity.NetworkStatusManager;
import com.hubble.framework.service.connectivity.P2PDiscovery;
import com.hubble.framework.service.connectivity.RemoteDevice;
import com.hubble.framework.service.p2p.P2pDevice;
import com.hubble.framework.service.p2p.P2pUtils;
import com.hubble.ota.OtaActivity;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.registration.interfaces.IChangeNameCallBack;
import com.hubble.registration.tasks.ChangeNameTask;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubble.registration.tasks.CheckFirmwareUpdateTask;
import com.hubble.registration.tasks.RemoveDeviceTask;
import com.hubble.subscription.ApplySubscriptionService;
import com.hubble.tls.LocalDevice;
import com.hubble.ui.DebugFragment;
import com.hubble.ui.ViewFinderActivity;
import com.hubble.util.CommandUtils;
import com.hubble.util.CommonConstants;
import com.hubble.util.ListChild;
import com.hubble.util.P2pSettingUtils;
import com.hubble.util.SubscriptionUtil;
import com.hubbleconnected.camera.BuildConfig;
import com.hubbleconnected.camera.R;

import com.msc3.registration.OnBoardingFragment;
import com.nxcomm.blinkhd.actors.ActorMessage;
import com.nxcomm.blinkhd.actors.CameraSettingsActor;
import com.nxcomm.blinkhd.ui.Global;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import base.hubble.Api;
import base.hubble.IAsyncTaskCommonHandler;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.database.DeviceStatusDetail;
import base.hubble.meapi.PublicDefines;


/**
 * Created by sonikas on 22/09/16.
 */
public class CameraConnectActivity extends BaseActivity implements NetworkStatusManager,View.OnClickListener,IChangeNameCallBack, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "CameraConnectActivity";

    public static final String REMOTE_DEVICE = "remote_device";
    public static final String TRANSPORT_MODE = "transport_mode";

    private ArrayAdapter adapter;
    private ImageView mProgressImage;
    private SecureConfig settings = HubbleApplication.AppConfig;
    private String mSelectedCameraName;
    private String regID;
    private String output;
    private String mac;
    private boolean isOnline;
    private boolean isSetUpCompleted = false;
    private ProgressDialog mProgressDialog;
    private int deviceRegistrationStatus = -1;
    private static String ssid = null;
    private static String ssidPassword = null;


    private String mImageURL = null;
    private String mDeviceModel = null;
    private ImageView mDeviceImageView;
    private EventData eventData;

    private AlertDialog mAlertDialog;
    private static final int FIRMWARE_UPGRADE_REQUEST_CODE = 0x01;
    private int mDeviceType;
    List<String> mCameraUsageStrings;
    EditText mCameraNameEdit;
    CameraSettingsActor mCameraSettingActor;
    ListChild mSoundDetection, mMotionDetection;
    SwitchCompat mSoundOnOffSwitch,mMotionOnOffSwitch;

    RadioButton mRadioSound1, mRadioSound2, mRadioSound3;
    RadioButton mRadioMotion1, mRadioMotion2,mRadioMotion3,mRadioMotion4,mRadioMotion5;
    RadioGroup mMotionRadioGroup, mSoundRadioGroup;

    private boolean mSoundSensitivityChanged=false,mMotionSensitivityChanged=false;
    private boolean mSoundSettingChanged=false,mMotionSettingChanged=false;
    private boolean mIsDefaultSound=false, mIsDefaultMotion=false;
    private boolean mSetSoundOnDefault=false;
    private boolean mSoundOnOfFSwitchChecked=false;
    private String deviceFwVersion = null;
    private static int DEFAULT_SOUND_SENSITIVITY=25;
    private static int DEFAULT_MOTION_SENSITIVITY=2;
    private Context mContext;



    private RemoteDevice mSelectRemoteDevice;
    private TransportMode mTransportMode = null;

    private boolean mIsInSoundSetting=false, mIsInMotionSetting=false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        eventData = new EventData();
        mSelectedCameraName = getIntent().getStringExtra("Camera_Name");
        regID = getIntent().getStringExtra("reg_id");
        mDeviceType = getIntent().getIntExtra("device_type", CommonConstants.DEVICE_TYPE_OTHER);
        mTransportMode = (TransportMode) getIntent().getSerializableExtra(TRANSPORT_MODE);

        if(BuildConfig.DEBUG)
            Log.d(TAG,"Transport Mode :- " + mTransportMode);

        if(mTransportMode == null)
        {

            switch (mDeviceType) {
                case CommonConstants.DEVICE_TYPE_ORBIT:
                    mTransportMode = TransportMode.WI_FI_HUBBLE;
                    break;

                case CommonConstants.DEVICE_TYPE_73:
                case CommonConstants.DEVICE_TYPE_72:
                    mTransportMode = TransportMode.LAN;
                    break;


                case CommonConstants.DEVICE_TYPE_OTHER:
                    mTransportMode = TransportMode.WI_FI_HUBBLE;
                    break;

                default:
                    mTransportMode = TransportMode.WI_FI_HUBBLE;
                    break;
            }
        }

        mSelectRemoteDevice = getIntent().getParcelableExtra(REMOTE_DEVICE);

        mCameraSettingActor = new CameraSettingsActor(this,regID,mActorInterface);

        scanWifiNetwork();

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        P2PDiscovery.getInstance().unRegisterDiscoveryCallback(mTransportMode);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        P2PDiscovery.getInstance().registerDiscoveryCallback(mTransportMode, this);
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        mContext = null;
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        mContext = getBaseContext();
    }


    @Override
    protected void onDestroy()
    {

        if (P2pSettingUtils.hasP2pFeature()) {
            startP2pService();
        }
        super.onDestroy();
    }

    private void startP2pService() {
        if (P2pSettingUtils.hasP2pFeature()) {
            // Build P2P device list
            List<P2pDevice> p2pDevices = new ArrayList<>();
            List<com.hubble.devcomm.Device> cameraDevices = DeviceSingleton.getInstance().getDevices();
            if (cameraDevices != null) {
                for (com.hubble.devcomm.Device cameraDevice : cameraDevices)
                {
                    boolean isOrbitP2PEnabled =  !cameraDevice.getProfile().isStandBySupported()
                    || settings.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);


                    if (isOrbitP2PEnabled && cameraDevice.getProfile().canUseP2p() && cameraDevice.getProfile().canUseP2pRelay() &&
                              !TextUtils.isEmpty(cameraDevice.getProfile().getRegistrationId()))
                    {
                        P2pDevice newDevice = new P2pDevice();
                        newDevice.setRegistrationId(cameraDevice.getProfile().getRegistrationId());
                        newDevice.setFwVersion(cameraDevice.getProfile().getFirmwareVersion());
                        newDevice.setMacAddress(cameraDevice.getProfile().getMacAddress());
                        newDevice.setModelId(cameraDevice.getProfile().getModelId());
                        if (cameraDevice.getProfile().getDeviceLocation() != null) {
                            newDevice.setLocalIp(cameraDevice.getProfile().getDeviceLocation().getLocalIp());
                        }

                        if (cameraDevice.getProfile().isStandBySupported())
                        {
                            DeviceStatusDetail deviceStatusDetail = cameraDevice.getProfile().getDeviceStatusDetail();
                            if(deviceStatusDetail != null && deviceStatusDetail.getDeviceStatus() != null)
                            {
                                String deviceStatus  = deviceStatusDetail.getDeviceStatus();
                                if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0)
                                {
                                    newDevice.setAvailable(true);
                                }
                                else
                                {
                                    newDevice.setAvailable(false);
                                }
                            }
                            else
                            {
                                newDevice.setAvailable(cameraDevice.getProfile().isAvailable());
                            }


                        } else {
                            newDevice.setAvailable(cameraDevice.getProfile().isAvailable());

                        }
                        p2pDevices.add(newDevice);
                    }
                }
            }
            String apiKey = Global.getApiKey(getApplicationContext());
            P2pUtils.startP2pService(this, apiKey, p2pDevices);
        }
    }
    @Override
    public void onBackPressed() {
        if (!isSetUpCompleted) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            alertDialogBuilder
                    .setMessage(getString(R.string.setup_cancel))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                           finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();

            alertDialog.show();

            Button noButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            noButton.setTextColor(getResources().getColor(R.color.text_blue));

            Button yesButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            yesButton.setTextColor(getResources().getColor(R.color.text_blue));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detect_try_again:
            case R.id.detect_wifi_again:
                scanWifiNetwork();
                break;
            case R.id.connect_try_again:
                Intent intent = new Intent(CameraConnectActivity.this, CameraSetUpActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.continue_btn:
                //save camera name
                hideKeyboard();
                if (mCameraNameEdit != null && mCameraNameEdit.getText() != null && !(mCameraNameEdit.getText().length() == 0)) {
                    String cameraName=mCameraNameEdit.getText().toString().trim();
                    if (checkNameValid(cameraName)) {
                        mProgressDialog = ProgressDialog.show(CameraConnectActivity.this, null, getString(R.string.changing_camera_name), true, false);
                        String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                        ChangeNameTask rename = new ChangeNameTask(getApplicationContext()
                                , CameraConnectActivity.this);
                        rename.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, saved_token, cameraName, regID);
                    } else {
                        showDialogValidName();
                    }
                }
                break;
            case R.id.tv_toolbar_back:
                onBackPressed();
                break;
            case R.id.get_started:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                break;
            case R.id.sound_detection_next:
            case R.id.sound_detection_skip:
                //if sound detection is not changed or sound detection is on and senstivity not chnaged
                if(!mSoundSettingChanged || (mSoundDetection.booleanValue && !mSoundSensitivityChanged))
                    setDefaultSoundSettings();
                setUpMotionDetection();
                break;
            case R.id.motion_detection_next:
            case R.id.motion_detection_skip:
                //if motion detection is not changed or motion detection is on and senstivity not chnaged
                if(!mMotionSettingChanged || (mMotionDetection.booleanValue && !mMotionSensitivityChanged))
                    setDefaultMotionSettings();

                 exitDeviceSetup();

                break;
            case R.id.radio_sound1:
                mSoundOnOfFSwitchChecked=false;
                setSoundThreshold(80);
                break;
            case R.id.radio_sound2:
                mSoundOnOfFSwitchChecked=false;
                setSoundThreshold(70);
                break;
            case R.id.radio_sound3:
                mSoundOnOfFSwitchChecked=false;
                setSoundThreshold(25);
                break;
            case R.id.radio_motion1:
                setMotionSensitivity(0);
                break;
            case R.id.radio_motion2:
                setMotionSensitivity(1);
                break;
            case R.id.radio_motion3:
                setMotionSensitivity(2);
                break;
            case R.id.radio_motion4:
                setMotionSensitivity(3);
                break;
            case R.id.radio_motion5:
                setMotionSensitivity(4);
                break;
        }

    }

    private void setActionBar() {
        TextView tv_title = (TextView) findViewById(R.id.tv_toolbar_title);
        tv_title.setText(getString(R.string.setup_device));
        ImageView tv_back = (ImageView) findViewById(R.id.tv_toolbar_back);
        tv_back.setOnClickListener(this);
    }


    private void scanWifiNetwork() {
        setContentView(R.layout.device_setup_detect_wifi);
        setActionBar();
        //starting animation
        ImageView detectCameraImage = (ImageView) findViewById(R.id.detect_wifi_img);
        detectCameraImage.setBackgroundResource(R.drawable.detecting_wifi_anim);
        AnimationDrawable animationDrawable = (AnimationDrawable) detectCameraImage.getBackground();
        animationDrawable.start();

        adapter = new ArrayAdapter(getApplicationContext(), R.layout.device_setup_devicelist_item, R.id.device_item);
        try {
            if(mTransportMode == TransportMode.LAN) {
                P2PDiscovery.getInstance().findNearbyDevices(TransportMode.LAN_WIFI_ROUTER, 10000);
            }
            else
                P2PDiscovery.getInstance().findNearbyDevices(TransportMode.WI_FI_ROUTER, 10000);
        } catch (BaseException e) {
            Log.d(TAG, "Exception while scanning for devices");
            e.printStackTrace();
        }
    }

    private void showWifiList() {
        final ListView wifilist = (ListView) findViewById(R.id.wifi_list);
        wifilist.setAdapter(adapter);
        wifilist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String itemValue = (String) wifilist.getItemAtPosition(position);
                final Configuration configuration = new Configuration();
                configuration.setSsid(itemValue);
                /*String storedPassword = CommonUtil.getStringValue(getApplicationContext(), itemValue + "-" + SettingsPrefUtils.PREFS_SSID);
                if (!TextUtils.isEmpty(storedPassword)) {
                    configuration.setPassWord(storedPassword);

                    authorizeWifi(configuration);
                }else {
                */

                final Dialog wifidialog = new Dialog(CameraConnectActivity.this);
                wifidialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                wifidialog.setContentView(R.layout.dialog_wifi_password);
                wifidialog.setCancelable(false);
                TextView wifiApName = (TextView) wifidialog.findViewById(R.id.wifi_ap_name);
                wifiApName.setText(itemValue.replace("\"", ""));
                TextView tv_cancel = (TextView) wifidialog.findViewById(R.id.tv_dialog_cancel);
                TextView tv_submit = (TextView) wifidialog.findViewById(R.id.tv_dialog_submit);
                final EditText et_password = (EditText) wifidialog.findViewById(R.id.et_dialog_passwod);
                final CheckBox cb_dialog = (CheckBox) wifidialog.findViewById(R.id.cb_dailog);

                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        wifidialog.dismiss();
                    }
                });

                tv_submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String password = et_password.getText().toString();
                        configuration.setPassWord(password);
                        //ARUNA
                           /* ssid = itemValue;
                            ssidPassword = password;*/

                        wifidialog.dismiss();
                        authorizeWifi(configuration);
                    }
                });


                cb_dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (((CheckBox) v).isChecked()) {
                            et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        } else {
                            et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        }
                    }
                });


                wifidialog.show();

            }
        });

    }


    private void authorizeWifi(Configuration configuration) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.device_setup_progress);
        setActionBar();
        mProgressImage = (ImageView) findViewById(R.id.progress_image);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_rotate);
        mProgressImage.startAnimation(animation);
        P2PDiscovery.getInstance().authorizeDevice(mTransportMode, configuration);
    }

    private void showNoNetworkFound() {
        setContentView(R.layout.device_setup_network_error);
        setActionBar();
        Button tryAgainButton = (Button) findViewById(R.id.detect_try_again);
        tryAgainButton.setOnClickListener(this);
    }

    private void showErrorScreen(){
        setContentView(R.layout.device_setup_error);
        setActionBar();
        TextView errorText=(TextView)findViewById(R.id.error_msg);
        errorText.setText(getString(R.string.setup_config_error));
        Button tryAgainButton=(Button)findViewById(R.id.connect_try_again);
        tryAgainButton.setOnClickListener(this);
    }

    private String getMacFromRegId(String regId) {
        String res = null;

        int startIdx = 6;
        int endIdx = startIdx + 12;
        try {
            res = regId.substring(startIdx, endIdx);
        } catch (Exception e) {
            Log.e("AutoConfigure", Log.getStackTraceString(e));
        }

        return res;
    }

    private void setUpSuccess()
    {
        /* remove the old snapshot and  event if camera was added before. The newly added camera should not show old info*/
        Util.deleteLatestPreview(regID);
        Util.removeDashBoardEventsFromSP(getApplicationContext(),regID);
       // CommonUtil.setSettingValue(getApplicationContext(), ssid + "-" + SettingsPrefUtils.PREFS_SSID, password);


        setContentView(R.layout.device_setup_successfull);
        setActionBar();
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,AppEvents.ADD_CAMERA+" : "+AppEvents.SUCCESS,AppEvents.ADD_CAMERA_SUCCESS);
        ZaiusEvent addCameraEvt = new ZaiusEvent(AppEvents.DASHBOARD);
        addCameraEvt.action(AppEvents.ADD_CAMERA+" : "+AppEvents.SUCCESS);
        TimeZone tz = TimeZone.getDefault();
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(addCameraEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }

        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,AppEvents.TIMEZONE+" : "+tz.getDisplayName(false, TimeZone.SHORT)+" "+tz.getDisplayName(false, TimeZone.LONG),AppEvents.TIMEZONE);
        ZaiusEvent timeZoneEvt = new ZaiusEvent(AppEvents.DASHBOARD);
        timeZoneEvt.action(AppEvents.TIMEZONE+" : "+tz.getDisplayName(false, TimeZone.SHORT)+" "+tz.getDisplayName(false, TimeZone.LONG));
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(timeZoneEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }

        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,AppEvents.CAMERA_MODEL_ID+" = "+regID.substring(2,6)+" : "+AppEvents.FW_VERSION+" = "+deviceFwVersion,AppEvents.CAMERA_MODEL_ID);
        ZaiusEvent cameraNameEvt = new ZaiusEvent(AppEvents.DASHBOARD);
        cameraNameEvt.action(AppEvents.CAMERA_MODEL_ID+" = "+regID.substring(2,6)+" : "+AppEvents.FW_VERSION+" = "+deviceFwVersion);

        try {
            ZaiusEventManager.getInstance().trackCustomEvent(cameraNameEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }

        if (mDeviceType == CommonConstants.DEVICE_TYPE_ORBIT)
        {
            ImageView cameraImage = (ImageView) findViewById(R.id.camera_image);
            cameraImage.setImageResource(R.drawable.orbit_setup);
        }
        else if(mDeviceType == CommonConstants.DEVICE_TYPE_72)
        {
            ImageView cameraImage = (ImageView) findViewById(R.id.camera_image);
            cameraImage.setImageResource(R.drawable.setup_success_72);
        }
        else if(mDeviceType == CommonConstants.DEVICE_TYPE_73)
        {
            ImageView cameraImage = (ImageView) findViewById(R.id.camera_image);
            cameraImage.setImageResource(R.drawable.setup_success_73);
        }

        ListView cameraUsageList = (ListView) findViewById(R.id.camera_usage_list);
        mCameraUsageStrings = Arrays.asList(getString(R.string.setup_usage_baby_monitor), getString(R.string.setup_usage_pet_monitor),
                getString(R.string.setup_usage_indoor_monitor), getString(R.string.setup_usage_outdoor_monitor), getString(R.string.setup_usage_other));
        CameraUsageAdapter adapter = new CameraUsageAdapter(mCameraUsageStrings, this);
        cameraUsageList.setAdapter(adapter);

        cameraUsageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setUpChangeName(mCameraUsageStrings.get(position));

            }

            });



        //set mvr off by defualt
        /*if(mDeviceType!=CommonConstants.DEVICE_TYPE_ORBIT){
            mCameraSettingActor.setRecordingParameter(PublicDefineGlob.SET_RECORDING_PARAMETER_MVR_OFF_PARAM, new CameraSettingsActor.MVRListener() {
                @Override
                public void onMVRResponse(boolean success) {
                    Log.d(TAG, "MVR Response:"+success);
                }
            });
        }*/

    }

    private void setUpChangeName(String name)
    {

        setContentView(R.layout.device_setup_camera_name);
        setActionBar();

        if (mDeviceType == CommonConstants.DEVICE_TYPE_ORBIT)
        {
            ImageView cameraImage = (ImageView) findViewById(R.id.camera_image);
            cameraImage.setImageResource(R.drawable.orbit_setup);
        }
        else if(mDeviceType == CommonConstants.DEVICE_TYPE_72)
        {
            ImageView cameraImage = (ImageView) findViewById(R.id.camera_image);
            cameraImage.setImageResource(R.drawable.setup_success_72);
        }
        else if(mDeviceType == CommonConstants.DEVICE_TYPE_73)
        {
            ImageView cameraImage = (ImageView) findViewById(R.id.camera_image);
            cameraImage.setImageResource(R.drawable.setup_success_73);
        }


        Button continueBtn = (Button) findViewById(R.id.continue_btn);
        mCameraNameEdit = (EditText) findViewById(R.id.camera_name_edit);
        if (getString(R.string.setup_usage_baby_monitor).equals(name)) {
            mCameraNameEdit.setText(getString(R.string.setup_name_baby_cam));
        } else if (getString(R.string.setup_usage_pet_monitor).equals(name)) {
            mCameraNameEdit.setText(getString(R.string.setup_name_pet_cam));
        } else if (getString(R.string.setup_usage_indoor_monitor).equals(name)) {
            mCameraNameEdit.setText(getString(R.string.setup_name_indoor_cam));
        } else if (getString(R.string.setup_usage_outdoor_monitor).equals(name)) {
            mCameraNameEdit.setText(getString(R.string.setup_name_outdoor_cam));
        }

        continueBtn.setOnClickListener(this);

        SubscriptionUtil.setShowFreeTrial(this, regID, true);
        SubscriptionUtil.setShouldCheckSubscriptionPlan(this, regID, true);


        //Start service to check subscription and enable motion detection.
        if(mDeviceType != CommonConstants.DEVICE_TYPE_ORBIT) {
            Intent intent = new Intent(this, ApplySubscriptionService.class);
            intent.putExtra(CommonConstants.REGISTRATION_ID, regID);
            startService(intent);
        }
        String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
        String userName = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, "");
        CommonUtil.setFirstCameraAddedToSP(getApplicationContext(), userName, true);


    }

    private void deviceOTAFailed()
    {
        setContentView(R.layout.device_setup_ota_failed);
        setActionBar();

        Button tryAgainButton = (Button) findViewById(R.id.connect_try_again);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CameraConnectActivity.this, CameraSetUpActivity.class);
                startActivity(intent);
                finish();

            }
        });


        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,AppEvents.ADD_CAMERA+" "+AppEvents.FAILURE+" : "+ getResources().getString(R.string.orbit_ota_failed),AppEvents.ADD_CAMERA_FAILED);
        ZaiusEvent addCamerafailedEvt = new ZaiusEvent(AppEvents.DASHBOARD);
        addCamerafailedEvt.action(AppEvents.ADD_CAMERA+" "+AppEvents.FAILURE+" : "+getResources().getString(R.string.orbit_ota_failed));
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(addCamerafailedEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }
    }

    private void connectToDeviceFailed() {
        Log.d(TAG, "setup failed:" + regID);
        Log.d(TAG, "setup failed:" + mSelectedCameraName);

        AnalyticsInterface.getInstance().trackEvent(AppEvents.ADD_CAMERA,AppEvents.CAMERA_ADD_FAILURE,eventData);
        setContentView(R.layout.device_setup_error);
        setActionBar();
        Button tryAgainButton = (Button) findViewById(R.id.connect_try_again);
        tryAgainButton.setOnClickListener(this);

        TextView errorText = (TextView) findViewById(R.id.error_msg);
        String failureReason = "null";
        switch (deviceRegistrationStatus) {
            case 0:
                errorText.setText(getString(R.string.setup_error));
                failureReason = "0";
                break;
            case 1:
                errorText.setText(getString(R.string.setup_failed_not_configured, mSelectedCameraName));
                failureReason = "camera not registered to hubble";
                break;
            case 2:
            case 5:
                errorText.setText(getString(R.string.setup_failed_retry));
                failureReason = "Registration failed";
                break;
            case 3:
                if (!isOnline)
                    errorText.setText(getString(R.string.setup_failed_retry));
                failureReason = "Unable to reach device";
                break;
            case 4:
                errorText.setText(getString(R.string.setup_failed_retry));
                failureReason = "Already registered";
                break;
            case -1:
                errorText.setText(getString(R.string.setup_failed_connect_to_server));
                failureReason = "facing difficulites in reaching server";
                break;
            default:
                errorText.setText(getString(R.string.setup_error));
        }

        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,AppEvents.ADD_CAMERA+" "+AppEvents.FAILURE+" : "+failureReason,AppEvents.ADD_CAMERA_FAILED);
        ZaiusEvent addCamerafailedEvt = new ZaiusEvent(AppEvents.DASHBOARD);
        addCamerafailedEvt.action(AppEvents.ADD_CAMERA+" "+AppEvents.FAILURE+" : "+failureReason);
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(addCamerafailedEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }

    }

    private void setUpSoundDetection() {
        setContentView(R.layout.device_setup_sound_detection);
        setActionBar();
        mSoundDetection= new ListChild(getSafeString(R.string.sound_detection), DEFAULT_SOUND_SENSITIVITY+"", true);
        mSoundOnOffSwitch = (SwitchCompat) findViewById(R.id.sound_on_off_switch);
        mSoundOnOffSwitch.setOnCheckedChangeListener(this);

        Button skipButton = (Button) findViewById(R.id.sound_detection_skip);
        Button nextButton = (Button) findViewById(R.id.sound_detection_next);
        skipButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        mSoundRadioGroup=(RadioGroup)findViewById(R.id.sound_radio_group);
        mRadioSound1 = (RadioButton) findViewById(R.id.radio_sound1);
        mRadioSound2 = (RadioButton) findViewById(R.id.radio_sound2);
        mRadioSound3 = (RadioButton) findViewById(R.id.radio_sound3);

        mRadioSound1.setOnClickListener(this);
        mRadioSound2.setOnClickListener(this);
        mRadioSound3.setOnClickListener(this);
        mIsInSoundSetting=true;

        mIsInSoundSetting=true;

    }


    private void setUpMotionDetection()
    {
        mIsInSoundSetting=false;

        setContentView(R.layout.device_setup_motion_detection);
        setActionBar();

        mMotionDetection = new ListChild(getSafeString(R.string.motion_detection), DEFAULT_MOTION_SENSITIVITY+"", true);
        mMotionOnOffSwitch = (SwitchCompat) findViewById(R.id.motion_on_off_switch);
        mMotionOnOffSwitch.setOnCheckedChangeListener(this);

        Button skipButton = (Button) findViewById(R.id.motion_detection_skip);
        Button nextButton = (Button) findViewById(R.id.motion_detection_next);
        skipButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        mMotionRadioGroup=(RadioGroup)findViewById(R.id.motion_radio_group);
        mRadioMotion1 = (RadioButton) findViewById(R.id.radio_motion1);
        mRadioMotion2 = (RadioButton) findViewById(R.id.radio_motion2);
        mRadioMotion3 = (RadioButton) findViewById(R.id.radio_motion3);
        mRadioMotion4 = (RadioButton) findViewById(R.id.radio_motion4);
        mRadioMotion5 = (RadioButton) findViewById(R.id.radio_motion5);

        mRadioMotion1.setOnClickListener(this);
        mRadioMotion2.setOnClickListener(this);
        mRadioMotion3.setOnClickListener(this);
        mRadioMotion4.setOnClickListener(this);
        mRadioMotion5.setOnClickListener(this);

        if(mDeviceType==CommonConstants.DEVICE_TYPE_ORBIT)
        {
            mRadioMotion1.setVisibility(View.GONE);
            mRadioMotion4.setVisibility(View.GONE);
            mCameraSettingActor.setIsOrbit(true);
        }
        else {
            mCameraSettingActor.setIsOrbit(false);
        }

        mIsInMotionSetting=true;
        isSetUpCompleted = true;

    }


    @Override
    public void onConnectStatus(NetworkConnectEvent networkEvent, RemoteDevice remoteDevice)
    {
        Log.d(TAG, "onConnectStatus");
        if(networkEvent==NetworkStatusManager.NetworkConnectEvent.WIFI_CONNECT_FAILURE || networkEvent == NetworkStatusManager.NetworkConnectEvent.LAN_CONNECT_FAILURE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    showErrorScreen();
                }
            });

        }
    }

    @Override
    public void onDeviceFound(List<RemoteDevice> remoteDevices)
    {
        Log.d(TAG, "onDeviceFound");
        setContentView(R.layout.device_setup_wifi_list);
        setActionBar();
        Button searchAgainButton = (Button) findViewById(R.id.detect_wifi_again);
        searchAgainButton.setOnClickListener(this);

        if (remoteDevices.size() > 0) {
            for (RemoteDevice remoteDevice : remoteDevices) {
                if (remoteDevice.getAddress() != null && !remoteDevice.getAddress().isEmpty()) {
                    if (adapter != null && adapter.getPosition(remoteDevice.getAddress()) < 0) {
                        Log.d(TAG, "found device:" + remoteDevice.getAddress());
                        //remove quotes from wifi names
                        char[] networkName = remoteDevice.getAddress().toCharArray();
                        char[] newName;
                        String wifiName;
                        if (networkName.length > 2 && networkName[0] == '"' && networkName[networkName.length - 1] == '"') {
                            newName = new char[networkName.length - 2];
                            int j = 0;
                            for (int i = 1; i < networkName.length - 1; i++, j++) {
                                newName[j] = networkName[i];
                            }
                            wifiName = String.valueOf(newName);
                        } else {
                            wifiName = String.valueOf(networkName);
                        }
                        adapter.add(wifiName);
                    }
                }
            }
            if (adapter != null && adapter.getCount() > 0) {
                showWifiList();
            } else {
                showNoNetworkFound();
            }
        } else {
            showNoNetworkFound();

        }
    }

    @Override
    public void onConfigStatus(ConfigurationEvent configurationEvent) {
        Log.d(TAG, "onConfigStatus");
       /* if(!TextUtils.isEmpty(ssid) && (!TextUtils.isEmpty(ssidPassword))) {
            CommonUtil.setSettingValue(getApplicationContext(), ssid + "-" + SettingsPrefUtils.PREFS_SSID, ssidPassword);
            ssid = null;
            ssidPassword = null;
        }*/

    }
    @Override
    public void onConfigStatus(ConfigurationEvent configurationEvent, LocalDevice localDevice) {
    }


    @Override
    public void onAuthorizationEvent(AuthorizationEvent authorizationEvent, Object result) {
        Log.d(TAG, "onAuthorizationEvent");
    }

    @Override
    public void onDataAvailable(String property, Object value) {
        Log.d(TAG, "onDataAvailable");
    }

    @Override
    public void onDeviceRegistrationOnServer() {
        Log.d(TAG, "onDeviceRegistrationOnServer");

        Handler handler = new Handler();
        //todo check delay value again
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CheckDeviceOnServerTask checkDeviceTask = new CheckDeviceOnServerTask();
                checkDeviceTask.execute();

            }
        }, 2000);

    }

    @Override
    public void update_cam_success() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        Toast.makeText(this, getString(R.string.changed_camera_name), Toast.LENGTH_SHORT).show();
        //goto next screen after change camera name
        if ( mDeviceType == CommonConstants.DEVICE_TYPE_ORBIT ||
             mDeviceType == CommonConstants.DEVICE_TYPE_73 ||
             mDeviceType == CommonConstants.DEVICE_TYPE_72) {
            //microphone not present in orbit and 73 camera
            setUpMotionDetection();
        } else {
            setUpSoundDetection();
        }

    }

    @Override
    public void update_cam_failed() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();

        //goto next screen after change camera name
        if (mDeviceType==CommonConstants.DEVICE_TYPE_ORBIT || mDeviceType==CommonConstants.DEVICE_TYPE_73
        || mDeviceType == CommonConstants.DEVICE_TYPE_72) {
            setUpMotionDetection();
        } else {
            setUpSoundDetection();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch(buttonView.getId()){
            case R.id.sound_on_off_switch:
               // mProgressDialog = ProgressDialog.show(CameraConnectActivity.this, null, getString(R.string.updating), true, false);
                mSoundDetection.setOldCopy();
                mSoundDetection.booleanValue=isChecked;
                mCameraSettingActor.send(new ActorMessage.SetSoundDetection(mSoundDetection, isChecked));
                mSoundOnOfFSwitchChecked=true;
                mSoundSettingChanged=true;

                if(!mSoundDetection.booleanValue){
                    resetSoundSensitivity();
                }else{
                    restoreSoundSensitivityLevel();
                }
                break;
            case R.id.motion_on_off_switch:
               // mProgressDialog = ProgressDialog.show(CameraConnectActivity.this, null, getString(R.string.updating), true, false);
                mMotionDetection.setOldCopy();
                mMotionDetection.booleanValue=isChecked;
                mCameraSettingActor.send(new ActorMessage.SetMotionNotification(mMotionDetection, isChecked));
                mMotionSettingChanged=true;

                if(!mMotionDetection.booleanValue)
                    resetMotionSensitivity();
                else{
                    restoreMotionSensitivityLevel();
                }
                break;
        }
    }


    public class CheckDeviceOnServerTask extends AsyncTask<Boolean, Void, Boolean> {

        boolean isContinue = true;

        private String firmwareVersion = null, localIP = null;

        public CheckDeviceOnServerTask() {
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {

            int k = 0;
            String userToken = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");

            do {
                try {
                    //check device status
                    Log.d(TAG, "check device status");
                    output = "OnGoing";


                    DeviceID deviceID = new DeviceID(userToken, regID);
                    DeviceManager.getInstance(getApplicationContext()).checkDeviceStatusRequest(deviceID, new Response.Listener<DeviceStatusDetails>() {
                        @Override
                        public void onResponse(DeviceStatusDetails response) {
                            deviceRegistrationStatus = response.getStatus();
                            Log.d(TAG, "Device registration status:" + deviceRegistrationStatus);
                            switch (deviceRegistrationStatus) {
                                case 0:
                                case 2:
                                case 4:
                                case 5:
                                    output = "OnGoing";
                                    break;
                                case 3:
                                    output = "Done";
                                    break;
                                case 1:
                                    output = "Failed";
                                    break;
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "status fail");
                            output = "OnGoing";
                            error.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    output = "OnGoing";
                }
                try {
                    if (output == null || output.equalsIgnoreCase("OnGoing")) {
                        //2000
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                //20;40 for orbit
            } while (k++ < 20 && output != null && output.equalsIgnoreCase("OnGoing") && !isCancelled());

            Log.d(TAG, "status : " + output);

            if (output != null && output.equalsIgnoreCase("Done")) {
                try {
                    Log.d(TAG, "check online");

                    DeviceID deviceID = new DeviceID(userToken, regID);
                    mac = getMacFromRegId(regID);
                    k = 0;
                    do {
                        DeviceManager.getInstance(getApplicationContext()).getDeviceDetailsRequest(deviceID, new Response.Listener<DeviceDetail>() {
                                    @Override
                                    public void onResponse(DeviceDetail response) {
                                        DeviceDetailsResponse deviceDetailsResponse = response.getDeviceDetailsResponse();

                                        if (deviceDetailsResponse != null) {
                                            //commented to enable re-registration in the same account
                                            /*final long currentTimeCheck = System.currentTimeMillis();

                                            Date registrationAt = deviceDetailsResponse.getRegistrationAt();

                                            if (registrationAt != null) {
                                                long delayTime = registrationAt.getTime() - currentTimeCheck;
                                                if (Math.abs(delayTime) < 5 * 60 * 1000) {
                                                    isOnline = true;
                                                    isContinue = false;
                                                } else {
                                                    isOnline = false;
                                                    isContinue = true;
                                                }
                                            }*/
                                            Log.d(TAG, "device response..isAvailable:"+deviceDetailsResponse.isAvailable());
                                            if(deviceDetailsResponse.isAvailable()){
                                                isOnline = true;
                                                isContinue = false;
                                            }else{
                                                isOnline = false;
                                                isContinue = true;
                                            }
                                            Log.d(TAG, "isOnline:"+isOnline+"isContinue:"+isContinue);
                                            firmwareVersion = deviceDetailsResponse.getFirmwareVersion();
                                            deviceFwVersion = firmwareVersion;
                                            localIP = deviceDetailsResponse.getDeviceLocation() != null ? deviceDetailsResponse.getDeviceLocation().getLocalIP() : null;

                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d(TAG, "onErrorResponse");
                                        error.printStackTrace();
                                        isOnline = false;
                                        isContinue = false;
                                    }
                                });
                        try {
                            if (isContinue) {
                                //1000 ; 5000 for orbit
                                Thread.sleep(5000);
                            }
                        } catch (InterruptedException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                        //10;20 for orbit
                    } while (k++ < 36 && isContinue && !isCancelled());
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    isOnline = false;
                }
                //isOnline=true;
            } else {
                isOnline = false;
            }
            Log.w(TAG, "is camera online: " + isOnline);
            fetchP2pCredentialsIfNeeded(regID);
            return isOnline;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute"+result);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            boolean isHandle = false;
            /* It is required that we should verify firmware upgrade for Orbit device */
            //if (CommonUtil.isOrbit(getApplicationContext()))
            //{
            String modelID = PublicDefine.getModelIdFromRegId(regID);

            isHandle = true;

            if (result)
            {

                Log.d(TAG, "registration successful, device online");
                if(modelID != null && modelID.compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)==0 && Util.isThisVersionGreaterThan(firmwareVersion,PublicDefine.ORBIT_SKIP_OTA_FW_VERSION))
                {
                    getBatteryMode(firmwareVersion, regID, modelID, localIP);
                }
                else
                {
                    doCheckFwUpgradeTask(firmwareVersion, regID, modelID, localIP);
                }

            } else {
                //if camera registration is successful and device is offline, delete the camera
                //this is done so that the user does not get confused by seeing offline camera in dashboard.
                //this usually occurs when camera is re-added to same account and wrong pwd is entered
                Log.d(TAG, "registration successfull, camera offline..removing camera");
                if(output!=null && output.equalsIgnoreCase("Done")){
                    RemoveDeviceTask deleteDeviceTask = new RemoveDeviceTask(getApplicationContext(), new RemoveDeviceTask.onDeleteTaskCompleted(){

                        @Override
                        public void onDeleteTaskCompleted(int result) {
                            if (mProgressImage != null) {
                                mProgressImage.clearAnimation();
                            }
                            connectToDeviceFailed();
                        }
                    });
                    deleteDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, regID, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, ""));

                }else {
                    Log.d(TAG, "Error in server response");
                    if (mProgressImage != null) {
                        mProgressImage.clearAnimation();
                    }
                    connectToDeviceFailed();
                }

            }
            //}
            // }

            if (!isHandle) {
                if (mProgressImage != null) {
                    mProgressImage.clearAnimation();
                }

                if (result) {
                    Log.d(TAG, "Device registered " + mSelectedCameraName);
                    setUpSuccess();
                } else {
                    Log.d(TAG, "Error in server response");
                    if(deviceRegistrationStatus==3 && !isOnline){
                        setUpSuccess();
                    }else
                        connectToDeviceFailed();
                }
            }


        }
    }


    boolean isAllowFirmwareUpgrade = false;
    private void getBatteryMode(final String firmwareVersion, final String regID, final String modelID, final String localIP)
    {
        Runnable runnableBatteryMode = new Runnable()
        {
            @Override
            public void run()
            {
                float deviceMode = -1;
                float batteryValue = -1;

                String res = CommandUtils.sendLocalCommand(localIP, PublicDefine.LOCAL_PORT_STR,PublicDefine.GET_DEVICE_MODE);

                if(BuildConfig.DEBUG)
                    Log.d(TAG,"battery mode:- " + res);

                if (res != null && res.startsWith(PublicDefine.GET_DEVICE_MODE))
                {
                    try
                    {
                        final Pair<String, Object> parsedResponse= CommonUtil.parseResponseBody(res);
                        if (parsedResponse != null && parsedResponse.second instanceof Float)
                        {
                            deviceMode = (Float) parsedResponse.second;
                        }
                        else if (parsedResponse != null && parsedResponse.second instanceof String)
                        {
                            try
                            {
                                deviceMode = Float.valueOf((String) parsedResponse.second);
                            }
                            catch (NumberFormatException e)
                            {
                                Log.e(TAG,e.getMessage());
                                deviceMode = -1;
                            }
                        }
                        else if(parsedResponse != null && parsedResponse.second instanceof Integer)
                        {
                            deviceMode = (Integer) parsedResponse.second;
                        }

                    }
                    catch (Exception exception)
                    {
                        Log.d(TAG, exception.getMessage());
                        exception.printStackTrace();


                    }
                }


                if (mContext != null)
                {
                    final int batteryStatus = (int)deviceMode;
                    if(batteryStatus == CameraStatusView.ORBIT_BATTERY_CHARGING)
                    {
                        isAllowFirmwareUpgrade = true;
                    }
                    else
                    {
                        isAllowFirmwareUpgrade = false;

                        String batteryResponse = CommandUtils.sendLocalCommand(localIP, PublicDefine.LOCAL_PORT_STR,PublicDefine.GET_BATTERY_VALUE);

                        if (batteryResponse != null && batteryResponse.startsWith(PublicDefine.GET_BATTERY_VALUE))
                        {

                            try
                            {
                                final Pair<String, Object> parsedResponse= CommonUtil.parseResponseBody(batteryResponse);
                                if (parsedResponse != null && parsedResponse.second instanceof Float)
                                {
                                    batteryValue = (Float) parsedResponse.second;
                                }
                                else if (parsedResponse != null && parsedResponse.second instanceof String)
                                {
                                    try
                                    {
                                        batteryValue = Float.valueOf((String) parsedResponse.second);
                                    }
                                    catch (NumberFormatException e)
                                    {
                                        Log.e(TAG,e.getMessage());
                                        batteryValue = -1;
                                    }
                                }
                                else if(parsedResponse != null && parsedResponse.second instanceof Integer)
                                {
                                    batteryValue = (Integer) parsedResponse.second;
                                }

                            }
                            catch (Exception exception)
                            {
                                Log.d(TAG, exception.getMessage());
                                exception.printStackTrace();


                            }

                            if(batteryValue <= PublicDefine.ORBIT_MINIMUM_BATTERY_LEVEL)
                            {
                                isAllowFirmwareUpgrade = false;
                            }
                            else
                            {
                                isAllowFirmwareUpgrade = true;
                            }
                        }
                    }
                    if(mContext != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (BuildConfig.DEBUG)
                                    Log.d(TAG, "batteryStatus :- " + batteryStatus);



                                if (BuildConfig.DEBUG)
                                    Log.d(TAG, "isAllowFirmwareUpgrade :- " + isAllowFirmwareUpgrade);

                                if (isAllowFirmwareUpgrade)
                                {
                                    doCheckFwUpgradeTask(firmwareVersion,regID,modelID,localIP);
                                }
                                else
                                {
                                    CommonUtil.setSettingInfo(mContext, regID+ "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, false);
                                    setUpSuccess();
                                }


                            }

                        });
                    }

                }
            }
        };
        Thread worker = new Thread(runnableBatteryMode);
        worker.start();
    }


    private void fetchP2pCredentialsIfNeeded(String regId) {
        String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
        String model = PublicDefine.getModelIdFromRegId(regId);
        if(model.equalsIgnoreCase("0068") || model.equalsIgnoreCase("0072")) {
            if (apiKey != null) {
                try {
                    ISettings                    cache       = HubbleApplication.AppContext;
                    Models.P2PCredentialResponse res         = Api.getInstance().getService().getP2PCredential(regId, apiKey);
                    Log.w(TAG, "fetch p2p credentials p2pId: " + res.p2pId + ", p2pKey: " + res.p2pKey);
                    cache.cacheP2PIdentifier(regId, res.p2pKey, res.p2pId);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doCheckFwUpgradeTask(String firmwareVersion, final String regID, String modelID, final String localIP) {


        String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

        // reinitialize interface
        mCameraSettingActor=new CameraSettingsActor(this,regID,mActorInterface);

        new CheckFirmwareUpdateTask(saved_token, regID, firmwareVersion, modelID, null, new IAsyncTaskCommonHandler() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void onPostExecute(final Object result) {
                if (result instanceof CheckFirmwareUpdateResult) {
                    CheckFirmwareUpdateResult checkFirmwareUpdateResult = (CheckFirmwareUpdateResult) result;
                    checkFirmwareUpdateResult.setLocalCamera(true);
                    checkFirmwareUpdateResult.setInetAddress(localIP);
                    checkFirmwareUpdateResult.setApiKey(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, ""));
                    checkFirmwareUpdateResult.setRegID(regID);
                    handleCheckFwUpdateResult(checkFirmwareUpdateResult);
                }
            }

            @Override
            public void onCancelled() {
            }
        }, settings.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false)).execute();
    }

    private void handleCheckFwUpdateResult(final CheckFirmwareUpdateResult result) {
        Log.d(TAG, "is new firmware version :- " + result.isHaveNewFirmwareVersion());
        Log.d(TAG, "device ip :- " + result.getInetAddress());

        if (result.isHaveNewFirmwareVersion() && result.getInetAddress() != null) {
            CommonUtil.setSettingInfo(this, result.getRegID()+ "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, true);
            showConfirmFWDialog(result);
        } else {
            CommonUtil.setSettingInfo(this, result.getRegID()+ "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, false);
            setUpSuccess();
        }
    }

    private void showConfirmFWDialog(final CheckFirmwareUpdateResult result)
    {
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_NEW_FW_AVAILABLE,AppEvents.NEW_FIRMWARE_AVAILABLE);

        ZaiusEvent cameraFirmwareEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
        cameraFirmwareEvt.action(AppEvents.CAMERA_NEW_FW_AVAILABLE);
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(cameraFirmwareEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.ota_setup_instruction);


        TextView tv_title = (TextView) findViewById(R.id.tv_toolbar_title);
        tv_title.setText(getString(R.string.firmware_upgrade));
        ImageView tv_back = (ImageView) findViewById(R.id.tv_toolbar_back);
        tv_back.setOnClickListener(this);

        TextView continueTv = (TextView) findViewById(R.id.continue_tv);
        TextView skipTv = (TextView) findViewById(R.id.skip_tv);


        if(PublicDefines.getModelIDFromRegID(regID).compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)== 0 && !Util.isThisVersionGreaterThan(result.getCurrentFirmwareVersion(),PublicDefine.ORBIT_SKIP_OTA_FW_VERSION))
        {
            skipTv.setVisibility(View.GONE);
        }


        skipTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                setUpSuccess();
            }
        });

        continueTv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                result.setRequestUpgradeOnly(true);

                // start ota activity to get result
                Intent intent = new Intent(CameraConnectActivity.this, OtaActivity.class);

                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_FW_UPGRADE_OK,AppEvents.UPGRADE_OK);
                ZaiusEvent firmwareUpgradeEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
                firmwareUpgradeEvt.action(AppEvents.CAMERA_FW_UPGRADE_OK);

                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(firmwareUpgradeEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }

                Bundle bundle = new Bundle();
                bundle.putBoolean(OtaActivity.IS_FROM_SETUP, true);
                bundle.putString(OtaActivity.DEVICE_MODEL_ID, PublicDefines.getModelIDFromRegID(regID));
                bundle.putSerializable(OtaActivity.CHECK_FIRMWARE_UPGRADE_RESULT, result);

                intent.putExtras(bundle);
                startActivityForResult(intent, FIRMWARE_UPGRADE_REQUEST_CODE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FIRMWARE_UPGRADE_REQUEST_CODE && mDeviceType==CommonConstants.DEVICE_TYPE_ORBIT)
        {
            if(resultCode == Activity.RESULT_OK) {
                setUpSuccess();
            }
            else
            {
                RemoveDeviceTask deleteDeviceTask = new RemoveDeviceTask(getApplicationContext(), new RemoveDeviceTask.onDeleteTaskCompleted(){

                    @Override
                    public void onDeleteTaskCompleted(int result) {
                        if (mProgressImage != null) {
                            mProgressImage.clearAnimation();
                        }
                        deviceOTAFailed();
                    }
                });
                deleteDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, regID, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, ""));

            }
        }
        else if(requestCode == FIRMWARE_UPGRADE_REQUEST_CODE)
        {
            setUpSuccess();;
        }
    }

    private void setSoundThreshold(int threshold){
        if(mSoundOnOffSwitch.isChecked()) {
           // mProgressDialog = ProgressDialog.show(CameraConnectActivity.this, null, getString(R.string.updating), true, false);
            mSoundDetection.setOldCopy();
            mSoundDetection.intValue = threshold;
            mCameraSettingActor.send(new ActorMessage.SetSoundThreshold(mSoundDetection, threshold));
            mSoundSensitivityChanged=true;
        }else{
            Toast.makeText(this, getString(R.string.setup_sound_detection_disabled),Toast.LENGTH_SHORT).show();
            resetSoundSensitivity();
        }
    }

    private void setMotionSensitivity(int value){
        if(mMotionOnOffSwitch.isChecked()) {

            //mProgressDialog = ProgressDialog.show(CameraConnectActivity.this, null, getString(R.string.updating), true, false);

            mMotionDetection.setOldCopy();
            mMotionDetection.intValue = value;
            mCameraSettingActor.send(new ActorMessage.SetMotionSentivity(mMotionDetection, value));
            mMotionSensitivityChanged=true;
        }else{
            Toast.makeText(this, getString(R.string.setup_motion_detection_disabled),Toast.LENGTH_SHORT).show();
            resetMotionSensitivity();
        }
    }

    private CameraSettingsActor.Interface mActorInterface = new CameraSettingsActor.Interface() {
        @Override
        public void onDataSetChanged(ListChild listChild) {

        }

        @Override
        public void onNotificationSettingsReceived() {

        }

        @Override
        public void onParkReceived(Pair<String, Object> response) {

        }

        @Override
        public void onParkTimerReceived(Pair<String, Object> response) {

        }

        @Override
        public void onMotionNotificationChange(ListChild listChild, boolean shouldRevert, String responseMessage) {
            /*if(mProgressDialog!=null && mProgressDialog.isShowing() && !mIsDefaultMotion)
                mProgressDialog.dismiss();*/
            Log.d(TAG, "onMotionNotificationChange response message:"+responseMessage+" isSuccess:"+!shouldRevert+" isInMotionSetting:"+mIsInMotionSetting);

            if(shouldRevert && mIsInMotionSetting) {
                Log.d(TAG, "motion detectection change failed");
                mMotionDetection.revertToOldCopy();
                mMotionOnOffSwitch.setOnCheckedChangeListener(null);
                mMotionOnOffSwitch.setChecked(mMotionDetection.booleanValue);
                mMotionOnOffSwitch.setOnCheckedChangeListener(CameraConnectActivity.this);
                Toast.makeText(CameraConnectActivity.this, getString(R.string.motion_detection_change_failed),Toast.LENGTH_SHORT).show();

                if(!mMotionDetection.booleanValue)
                    resetMotionSensitivity();
                else{
                    restoreMotionSensitivityLevel();
                }
                /*if(mIsDefaultMotion){
                    *//*if(mProgressDialog!=null)
                        mProgressDialog.dismiss();*//*
                    exitDeviceSetup();
                }*/
            }else{
                //should be done in checked change listener after removing progress dialog
                /*if(!mMotionDetection.booleanValue)
                    resetMotionSensitivity();
                else{
                    restoreMotionSensitivityLevel();
                }*/
                Log.d(TAG, "Motion detection change success");
                if(mIsDefaultMotion && !mMotionSensitivityChanged){
                   Log.d(TAG, "Setting default motion sensitivity");
                    mMotionDetection.setOldCopy();
                    mMotionDetection.intValue = DEFAULT_MOTION_SENSITIVITY;
                    mCameraSettingActor.send(new ActorMessage.SetMotionSentivity(mMotionDetection, DEFAULT_MOTION_SENSITIVITY));
                }

            }
        }

        @Override
        public void onValueSet(ListChild listChild, boolean shouldRevert, String responseMessage) {
          /*  if(mProgressDialog!=null && mProgressDialog.isShowing() && !mSetSoundOnDefault)
                mProgressDialog.dismiss();*/

          Log.d(TAG, "onValueSet response message:"+responseMessage+" isSuccess:"+!shouldRevert+ " isInSoundSetting:"+mIsInSoundSetting +" isInMotionSetting:"+mIsInMotionSetting);

            if(listChild.equals(mSoundDetection)){
                if(shouldRevert && mIsInSoundSetting) {
                    mSoundDetection.revertToOldCopy();
                    mSoundOnOffSwitch.setOnCheckedChangeListener(null);
                    mSoundOnOffSwitch.setChecked(mSoundDetection.booleanValue);
                    mSoundOnOffSwitch.setOnCheckedChangeListener(CameraConnectActivity.this);
                    Toast.makeText(CameraConnectActivity.this, getString(R.string.sound_detection_change_failed),Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Sound detection change failed");
                    if(mSoundOnOffSwitch.isChecked()) {
                        switch (listChild.intValue) {
                            case 80:
                                mRadioSound1.setOnClickListener(null);
                                mRadioSound1.setChecked(true);
                                mRadioSound1.setOnClickListener(CameraConnectActivity.this);
                                break;

                            case 70:
                                mRadioSound2.setOnClickListener(null);
                                mRadioSound2.setChecked(true);
                                mRadioSound2.setOnClickListener(CameraConnectActivity.this);
                                break;

                            case 25:
                                mRadioSound3.setOnClickListener(null);
                                mRadioSound3.setChecked(true);
                                mRadioSound3.setOnClickListener(CameraConnectActivity.this);
                                break;
                            default:
                                resetSoundSensitivity();
                                break;
                        }
                    }else{
                      resetSoundSensitivity();
                    }
                    if(mSetSoundOnDefault) {

                        /*if(mProgressDialog!=null)
                            mProgressDialog.dismiss();*/
                        //setUpMotionDetection();

                        mSetSoundOnDefault=false;
                    }
                }else{
                    //should be done in checked change after no progress
                    /*if(mSoundOnOfFSwitchChecked && !mSoundDetection.booleanValue){
                        resetSoundSensitivity();
                    }else{
                        restoreSoundSensitivityLevel();
                    }*/
                    if(mSetSoundOnDefault && !mSoundSensitivityChanged){
                        Log.d(TAG, "Sound detectection change success..setting sound sensitivity");
                        mSoundDetection.setOldCopy();
                        mSoundDetection.intValue = DEFAULT_SOUND_SENSITIVITY;
                        mCameraSettingActor.send(new ActorMessage.SetSoundThreshold(mSoundDetection, DEFAULT_SOUND_SENSITIVITY));
                        mIsDefaultSound=true;
                        mSetSoundOnDefault=false;
                        return;
                    }
                }
                //not required after progress is removed

                /*if(mIsDefaultSound)
                    setUpMotionDetection();*/
            }else if(listChild.equals(mMotionDetection) && mIsInMotionSetting){
                if(shouldRevert) {
                    mMotionDetection.revertToOldCopy();
                    Toast.makeText(CameraConnectActivity.this,getString(R.string.sensitivity_changed_failed),Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Motion sensitivity change failed");
                    switch (listChild.intValue) {
                        case 0:
                            mRadioMotion1.setOnClickListener(null);
                            mRadioMotion1.setChecked(true);
                            mRadioMotion1.setOnClickListener(CameraConnectActivity.this);
                            break;
                        case 1:
                            mRadioMotion2.setOnClickListener(null);
                            mRadioMotion2.setChecked(true);
                            mRadioMotion2.setOnClickListener(CameraConnectActivity.this);
                            break;
                        case 2:
                            mRadioMotion3.setOnClickListener(null);
                            mRadioMotion3.setChecked(true);
                            mRadioMotion3.setOnClickListener(CameraConnectActivity.this);
                            break;
                        case 3:
                            mRadioMotion4.setOnClickListener(null);
                            mRadioMotion4.setChecked(true);
                            mRadioMotion4.setOnClickListener(CameraConnectActivity.this);
                            break;
                        case 4:
                            mRadioMotion5.setOnClickListener(null);
                            mRadioMotion5.setChecked(true);
                            mRadioMotion5.setOnClickListener(CameraConnectActivity.this);
                            break;
                        default:
                            resetMotionSensitivity();
                            break;

                    }
                }
               /* if(mIsDefaultMotion)
                    exitDeviceSetup();*/
            }

        }

        @Override
        public void onScheduleDataReceived() {

        }
    };

    private String getSafeString(int stringResourceId) {
        if (this != null) {
            return this.getString(stringResourceId);
        } else {
            return "";
        }
    }

    private void resetSoundSensitivity(){
       mSoundRadioGroup.clearCheck();
    }

    private void resetMotionSensitivity(){
       mMotionRadioGroup.clearCheck();
    }

    private boolean checkNameValid(String name) {
        boolean valid = false;
        valid = name.matches("[a-zA-Z'0-9 ._-]+");
        boolean validLength=name.length()>=3 && name.length()<=25;
        return valid && validLength;
    }

    private void showDialogValidName() {
        mAlertDialog = new AlertDialog.Builder(this).setMessage(getSafeString(R.string.name_camera_invalid))
                .setTitle(getSafeString(R.string.title_name_camera_invalid))
                .setNegativeButton(getSafeString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dialog.dismiss();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).show();
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void setDefaultMotionSettings(){

       // mProgressDialog= ProgressDialog.show(CameraConnectActivity.this, null, getString(R.string.setup_set_default), true, false);

        if(!mMotionSettingChanged){
            mMotionDetection.setOldCopy();
            mMotionDetection.booleanValue=true;
            mCameraSettingActor.send(new ActorMessage.SetMotionNotification(mMotionDetection, true));
        }else if(!mMotionSensitivityChanged) {
            mMotionDetection.setOldCopy();
            mMotionDetection.intValue = DEFAULT_MOTION_SENSITIVITY;
            mCameraSettingActor.send(new ActorMessage.SetMotionSentivity(mMotionDetection, DEFAULT_MOTION_SENSITIVITY));
        }

        mIsDefaultMotion=true;
    }

    private void setDefaultSoundSettings(){

       // mProgressDialog= ProgressDialog.show(CameraConnectActivity.this, null, getString(R.string.setup_set_default), true, false);

        if(!mSoundSettingChanged){
            mSetSoundOnDefault=true;
            mSoundDetection.setOldCopy();
            mSoundDetection.booleanValue=true;
            mCameraSettingActor.send(new ActorMessage.SetSoundDetection(mSoundDetection, true));
        }else{
            mSoundDetection.setOldCopy();
            mSoundDetection.intValue = DEFAULT_SOUND_SENSITIVITY;
            mCameraSettingActor.send(new ActorMessage.SetSoundThreshold(mSoundDetection, DEFAULT_SOUND_SENSITIVITY));
            mIsDefaultSound=true;
        }


    }

    private void exitDeviceSetup(){
        mIsInMotionSetting=false;
        resetPrivacyMode();
        if (mDeviceType == CommonConstants.DEVICE_TYPE_ORBIT) {
            Intent onBoardingIntent = new Intent(CameraConnectActivity.this, OnBoardingFragment.class);
            onBoardingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(onBoardingIntent);
            finish();
        } else {
            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
            finish();
        }
    }


    private void resetPrivacyMode(){

        DeviceAttribute privacyAttribute = new DeviceAttribute(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regID, "privacy_mode_enabled", "0");
        DeviceManager.getInstance(getApplicationContext()).updateDeviceAttribute(privacyAttribute, new Response.Listener<DeviceAttributeDetails>() {
            @Override
            public void onResponse(DeviceAttributeDetails response) {

            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void restoreMotionSensitivityLevel(){
        mMotionRadioGroup.clearCheck();
        switch(mMotionDetection.intValue){
            case 0:
                mRadioMotion1.setOnClickListener(null);
                mRadioMotion1.setChecked(true);
                mRadioMotion1.setOnClickListener(CameraConnectActivity.this);
                break;
            case 1:
                mRadioMotion2.setOnClickListener(null);
                mRadioMotion2.setChecked(true);
                mRadioMotion2.setOnClickListener(CameraConnectActivity.this);
                break;
            case 2:
                mRadioMotion3.setOnClickListener(null);
                mRadioMotion3.setChecked(true);
                mRadioMotion3.setOnClickListener(CameraConnectActivity.this);
                break;
            case 3:
                mRadioMotion4.setOnClickListener(null);
                mRadioMotion4.setChecked(true);
                mRadioMotion4.setOnClickListener(CameraConnectActivity.this);
                break;
            case 4:
                mRadioMotion5.setOnClickListener(null);
                mRadioMotion5.setChecked(true);
                mRadioMotion5.setOnClickListener(CameraConnectActivity.this);
                break;
            default:
                mRadioMotion3.setOnClickListener(null);
                mRadioMotion3.setChecked(true);
                mRadioMotion3.setOnClickListener(CameraConnectActivity.this);
                break;
        }
    }

    private void restoreSoundSensitivityLevel(){
        mSoundRadioGroup.clearCheck();
        switch(mSoundDetection.intValue){
            case 80:
                mRadioSound1.setOnClickListener(null);
                mRadioSound1.setChecked(true);
                mRadioSound1.setOnClickListener(CameraConnectActivity.this);
                break;

            case 70:
                mRadioSound2.setOnClickListener(null);
                mRadioSound2.setChecked(true);
                mRadioSound2.setOnClickListener(CameraConnectActivity.this);
                break;

            case 25:
                mRadioSound3.setOnClickListener(null);
                mRadioSound3.setChecked(true);
                mRadioSound3.setOnClickListener(CameraConnectActivity.this);
                break;
            default:
                mRadioSound3.setOnClickListener(null);
                mRadioSound3.setChecked(true);
                mRadioSound3.setOnClickListener(CameraConnectActivity.this);
                break;
        }
    }


}