
package com.nxcomm.blinkhd.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceCategory;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;
import com.crittercism.app.Crittercism;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.common.collect.ImmutableMap;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.bta.BTATask;
import com.hubble.command.CameraCommandUtils;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.impl.cvision.NightLightHelper;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.dialog.HubbleDialogFactory;
import com.hubble.events.SendCommandEvent;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceStatus;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.helpers.AsyncPackage;
import com.hubble.ota.OtaActivity;
import com.hubble.registration.AnalyticsController;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.SubscriptionWizard;
import com.hubble.registration.Util;
import com.hubble.registration.interfaces.IChangeNameCallBack;
import com.hubble.registration.tasks.ChangeNameTask;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubble.registration.tasks.CheckFirmwareUpdateTask;
import com.hubble.registration.tasks.RemoveDeviceTask;
import com.hubble.ui.DebugFragment;
import com.hubble.util.CommandUtils;
import com.hubble.util.CommonConstants;
import com.hubble.util.ListChild;
import com.hubble.util.LocalDetectorService;
import com.hubble.util.LogZ;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.HeadersCallback;
import com.koushikdutta.ion.HeadersResponse;
import com.koushikdutta.ion.Ion;
import com.msc3.ConnectToNetworkActivity;
import com.nxcomm.blinkhd.actors.ActorMessage;
import com.nxcomm.blinkhd.actors.CameraSettingsActor;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.nxcomm.blinkhd.ui.dialog.TurnScheduleTask;
import com.nxcomm.blinkhd.util.NotificationSettingUtils;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.DeviceWakeup;
import com.util.SettingsPrefUtils;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.hubble.Api;
import base.hubble.IAsyncTaskCommonHandler;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.database.DeviceProfile;
import base.hubble.meapi.Upload;
import base.hubble.meapi.User;
import base.hubble.meapi.user.UploadTokenResponse;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import com.hubbleconnected.camera.R;
import static com.hubbleconnected.camera.R.id.ll_motionLayout;
import static com.hubbleconnected.camera.R.id.radio_remove_clip;
import static com.hubbleconnected.camera.R.id.switch_motionDetection;
import static com.hubble.registration.PublicDefine.MODEL_ID_FOCUS72;
import static com.hubble.registration.PublicDefine.PIR_MOTION_DETECTION_SOURCE;

;


	/**
	 * Created by CVision on 6/30/2016.
	 */
	public class CameraSettingsActivity extends AppCompatActivity implements View.OnClickListener, IChangeNameCallBack, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
		private CameraSettingsActor actor;
		private SharedPreferences sharedPreferences;
		private ImageView imgSettingsBack, imgCameraDetails, imgCameraSettings, imgGeneralSettings, deleteCamera;
		private LinearLayout llCameraDetails, llCameraSettings, llGeneralSettings, llStreetCamera, llNightLigth;
		private TextView txtCameraDetails, txtCemeraSettings, txtGeneralSettings;
		private SwitchCompat motionSwitch, soundSwitch, tempSwitch,lensCorrectionSwitch, schedulerSwitch;
		private LinearLayout ll_motionSensitivity, ll_soundSensitivity, ll_temparatureDetection, mvrLayout;
		private LinearLayout sendCameraLogs, tempFormat, slaveCheck, timeZoneCheck;
		private RelativeLayout firmwareLayout, schedulerLayout;

		private LinearLayout mMotionVideoRecodingLayout,mVideoRecordingLayout;
		private RadioButton mSdcardRecordingRButton,mOffRecordingRButton;
		private RadioGroup mMotionRecordingGroup;
		private TextView mVideoRecordingDurationTv;

		private LinearLayout mMacAddressLayout;
		private ImageView closeCamaraSettings, closeCameraDetails, closeGeneralSettings;
		private TextView cameraName, macAddress, firmwareVersion, slaveFirmware, currentTimeZone, currentPlan, wifiStrength, schedulerCurrentText, schedulerNextText;
		private TextView settingsHeader, cameraNameHeader, deleteAllEvents, cameraId, settingsHeaderCamera;
		private SwitchCompat streetCameraSwitch;
		private static boolean isSettings;
		private Device mDevice;
		//private RadioButton celsius, fahrenheit, hourFormat12, hourFormat24;
		private ImageView editCameraName;
		private RadioButton motionSentivity1, motionSentivity2, motionSentivity3, motionSentivity4, motionSentivity5;
		private RadioButton soundSensitivity1, soundSensitivity2, soundSensitivity3, soundSensitivity4, soundSensitivity5;
		private RadioButton mvrCould, mvrSdCard, mvrOff;
		private RadioButton mRemoveSdcardClip, mSwitchCloud;
		private RadioButton mCentigrade, mfahrenheit;
        private RadioButton snapShotAuto, snapShotChoose;
        private RadioButton mdTypeNone, mdTypeRegular, mdTypeSleepAnalytics, mdTypeExpression;
        private ImageView snapShotCapture;
		private RangeSeekBar tempSeekbar;
		LinearLayout motionLayout, sdcardLayout, soundLayout, temperatureLayout, currentPlanLayout, wifiLayout,lensCorrectLayout;
        LinearLayout babyMdTypesLayout, videoQualityLayout, viewModeLayout;
        RadioButton narrow, wide;

		ProgressDialog changeNameDialog;
		private AlertDialog mAlertDialog, mSecondaryAlertDialog;
		private Dialog mMvrWarningDialog = null;
		private SecureConfig settings = HubbleApplication.AppConfig;
		private String latestCameraName = null;
		private String cameraWifiStrength = "0%";
		PreferenceCategory profileCategory = null;
		String apiKey = null;
		private boolean removeDialogShowing = false;

		// For general settings
		boolean generalSettingsVisible = false;

		LinearLayout ceilingLayout = null;
		TextView ceilingIdTV = null;
		private SwitchCompat ceilingSwitch = null;
		LinearLayout nightVisionLayout = null;
		RadioButton nightVisionAuto = null;
		RadioButton nightVisionON = null;
		RadioButton nightVisionOFF = null;
		RadioButton nightLightAuto, nightLightOff, nightLightOn;
		LinearLayout brightnessLight = null;
		SeekBar brightnessSeekBar = null;
		LinearLayout volumeLayout = null;
        LinearLayout overlayLayout = null;
        SwitchCompat overlayDateSwitch = null;
		SeekBar volumeSeekbar = null;
		int seekBarMaxValue = 8;
		int volumeSeekBarMaxValue = 7;

		private LinearLayout mSdcardFormatLayout;

		public static Context mContext;
		private ProgressDialog applyingDialog, loadingDailog;
		private ProgressDialog remoteFwDialog;
		private String[] groupSettings;
		private final int NIGHTVISION_AUTO = 0;
		private final int NIGHTVISION_ON = 1;
		private final int NIGHTVISION_OFF = 2;
		public static final int NIGHT_LIGHT_AUTO = 0, NIGHT_LIGHT_ON =1, NIGHT_LIGHT_OFF = 2;
        private int nightLightMode = NIGHT_LIGHT_AUTO;
		private int orbitCurrentMotionValue = 60;
		private Dialog mNoNetworkDialog = null;

		private LinearLayout mMCULayout;
		private TextView mMCUVersionTv;
		private String mMCUVersion;

        Dialog mDialog = null;

        private int remainingBTATime = 0;
		private int mMacAddressClickCount = 0;


		ListChild ceilingMount, viewMode, overlayDate, motionDetection, soundDetection, temperature,qualityOfService,
				brightness, volume, contrast, nightVision, park, videoQuality, statusLed, mLEDFlicker,lensCorrection,
                socVersion, timezone,videoRecording,mcuVersionListChild, nightligthListChild;

		private static final String TAG = "CameraSettingsActivity";

		private boolean isNotiSettingsVisible = false;
        private boolean isSettingsMainVisible = false;
		private int previousSelectionValue = 0; // SD card by deafult

		public static final int RECORD_MOTION_OPT_OFF = 0;
		public static final int RECORD_MOTION_OPT_CLOUD = 1;
		public static final int RECORD_MOTION_OPT_SDCARD = 2;

        private static final int FROM_GALLERY = 3;
        private static final int FROM_CAMERA = 2;
        private static final int FROM_CLEAR_IMAGE = 1;
	    public static final int FROM_MVR_SCHEDULE = 4;
	    private static final int FIRMWARE_UPGRADE_REQUEST_CODE = 5;


        public static final int MD_TYPE_OFF_INDEX = 0;
        public static final int MD_TYPE_MD_INDEX = 1;
        public static final int MD_TYPE_BSC_INDEX = 2;
        public static final int MD_TYPE_BSD_INDEX = 3;
        public static final int MD_TYPE_IP_INDEX = 9999;
        public static final int MD_TYPE_PIR_INDEX = 2;

        public static final String MD_TYPE_OFF = "OFF";
        public static final String MD_TYPE_MD = "MD";
        public static final String MD_TYPE_BSC = "BSC";
        public static final String MD_TYPE_BSD = "BSD";
        public static final String MD_TYPE_IP = "IP";
        public static final String MD_TYPE_PIR = "PIR";


        File photoFile;

		private EventData eventData;


		private CompoundButton.OnCheckedChangeListener mSwitchListner;

        private LinearLayout mOtaLinearLayout;
		private SwitchCompat mProdOtaSwitch;
		private GoogleApiClient client;
		private long settingsInTime;
		private long settingsOutTime;


	    private Models.DeviceSchedule mScheduleData;

	    private boolean mIsFromResult = false;

	    private  String mNewFirmwareVersion;
        private boolean mIsActivityRunning = false;

		private NightLightHelper nightLightHelper = NightLightHelper.getInstance();



        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            /*if (resultCode == PublicDefine.CODE_DEVICE_REMOVAL) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).switchToDeviceList(true);
                }
                return;
            }*/
	        mIsFromResult = true;
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            if (requestCode == FROM_GALLERY) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath = getPath(selectedImageUri);
                if (selectedImagePath != null) {
                    uploadImage(selectedImagePath, null);

                }
            } else if (requestCode == FROM_CAMERA) {
                byte[] image = data.getByteArrayExtra("image");
                Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                uploadImage(null, bmp);

            } else if (requestCode == FROM_CLEAR_IMAGE) {
                galleryAddPic();
                if (photoFile != null) {
                    uploadImage(photoFile.getPath(), null);

                }
            } else if (requestCode == FROM_MVR_SCHEDULE) {
                /*if ( data == null || mScheduleData == null) {
                    return;
                }
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                // if #MvrScheduleCrudActivity has been crashed, data all were null.
                Object tempData = bundle.getSerializable("newDrawnData");
                if (tempData == null) {
                    return;
                }
                boolean isEnable = mScheduleData.isEnable();
	            mScheduleData.inverse((HashMap<String, ArrayList<String>>) tempData);
	            mScheduleData.setEnable(isEnable);*/

	            setScheduleData();

            }else if(requestCode == FIRMWARE_UPGRADE_REQUEST_CODE)
	        {
		        if(mNewFirmwareVersion != null) {
			        mDevice.getProfile().setFirmwareVersion(mNewFirmwareVersion);
			        if (firmwareVersion != null)
				        firmwareVersion.setText(mNewFirmwareVersion);
		        }
	        }
        }

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			eventData = new EventData();
		}

		@Override
		public void onPause(){
				super.onPause();

		   settingsOutTime =  System.currentTimeMillis() - settingsInTime;

			int time =(int) settingsOutTime / 1000;
			String timeSpentOnSettingsScreen = null;
			Log.d("LoginTime","LoginTime : "+settingsOutTime + " Sec = "+time);

			if(time<=5){
				timeSpentOnSettingsScreen = "5 sec";
			}else if(time>5 && time<=10){
				timeSpentOnSettingsScreen = "10 sec";
			}else if(time>10 && time<=30){
				timeSpentOnSettingsScreen = "30 sec";
			}else if(time>30 && time<=60){
				timeSpentOnSettingsScreen = "1 min";
			}else if(time>60 && time<=120){
				timeSpentOnSettingsScreen = "2 min";
			}else if(time>120 && time<=180){
				timeSpentOnSettingsScreen = "3 min";
			}else if(time > 180){
				timeSpentOnSettingsScreen = "> 3 min";
			}
			GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SETTINGS,AppEvents.TIME_SPENT_ON_SETTINGS_SCREEN+" : "+ timeSpentOnSettingsScreen,AppEvents.TIME_SPENT_ON_SETTINGS_SCREEN);
			ZaiusEvent settingsScreenTimeEvt = new ZaiusEvent(AppEvents.SETTINGS);
			settingsScreenTimeEvt.action(AppEvents.TIME_SPENT_ON_SETTINGS_SCREEN+" : "+ timeSpentOnSettingsScreen);
			try {
				ZaiusEventManager.getInstance().trackCustomEvent(settingsScreenTimeEvt);
			} catch (ZaiusException e) {
				e.printStackTrace();
			}
			mIsActivityRunning = false;

	}

		@Override
		protected void onResume() {
			super.onResume();

            mIsActivityRunning = true;
			if(mIsFromResult){
				mIsFromResult = false;
				return;
			}
			settingsInTime = System.currentTimeMillis();
			apiKey = Global.getApiKey(getApplicationContext());

			mContext = getApplicationContext();
			sharedPreferences = getSharedPreferences("app_config", Context.MODE_PRIVATE);
			mDevice = DeviceSingleton.getInstance().getSelectedDevice();

			if (mDevice == null || mDevice.getProfile() == null) {
				return;
			}

			actor = new CameraSettingsActor(getApplicationContext(), mDevice, mActorInterface);



            motionDetection = new ListChild(getSafeString(R.string.motion_detection), "", true);

            if (mDevice.getProfile().doesHaveMicrophone()) {
                //	ll_soundSensitivity.setVisibility(View.VISIBLE);
                soundDetection = new ListChild(getSafeString(R.string.sound_detection), "", true);
                volume = new ListChild(getSafeString(R.string.volume), "", true);
            } else {
                //ll_soundSensitivity.setVisibility(View.GONE);
                soundDetection = null;
            }


            if (mDevice.getProfile().doesHaveTemperature()) {
                //	ll_temparatureDetection.setVisibility(View.VISIBLE);
                temperature = new ListChild(getSafeString(R.string.temperature), "", true);
            } else {
                //ll_temparatureDetection.setVisibility(View.GONE);
                temperature = null;
            }

			if(nightLightHelper.isSupportNightLight(mDevice.getProfile().getRegistrationId())) {
				nightligthListChild = new ListChild(getSafeString(R.string.night_light), "", true);
			} else {
				nightligthListChild = null;
			}

            if (mDevice.getProfile().doesHaveCeilingMount()) {
                ceilingMount = new ListChild(getSafeString(R.string.ceiling_mount), "", true);
                //ceilingLayout.setVisibility(View.VISIBLE);

            } else {
                //ceilingLayout.setVisibility(View.GONE);
                ceilingMount = null;
            }

            if ("0086".equals(mDevice.getProfile().getModelId())) {
                overlayDate = new ListChild(getSafeString(R.string.overlay_date), "", true);
            }else{
                overlayDate = null;
            }

            brightness = new ListChild(getSafeString(R.string.brightness), "", true);
            nightVision = new ListChild(getSafeString(R.string.night_vision), "", true);


            if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT))
            {
                motionDetection = new ListChild(getSafeString(R.string.motion_detection), "", true);


	            if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_SDCARD_CAPACITY_FIRMWARE_VERSION))
	            {
		            lensCorrection = new ListChild(getSafeString(R.string.lens_correction), "", true);
	            }
	            if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION))
	            {
		            videoRecording = new ListChild(getSafeString(R.string.video_recording_duration), "", true);
	            }

				mcuVersionListChild = new ListChild(getSafeString(R.string.mcu_version), "", true);

                checkDeviceStatus(mDevice);
			}
            else {

				// set device local status for other camera
				AsyncPackage.doInBackground(new Runnable() {
												@Override
												public void run() {

														if (actor != null)
															actor.setDeviceLocal(CameraAvailabilityManager.getInstance().isCameraInSameNetwork(HubbleApplication.AppContext, mDevice));


												}
											});
                getNotificationSettings();
                buildGroupSetting2Codes();
                getSetting2IfAvailable(PublicDefine.SETTING_2_KEY_CEILING_MOUNT);
                if (mDevice.getProfile().getModelId().equals("0086")) {
                    getOverlayDateIfAvailable(true);
                    //getOverlayDateIfAvailable(settingsActor);
                }

	            if(mDevice.getProfile().doesSupportSDCardAccess() && !mDevice.getProfile().getModelId().equals(PublicDefine.MODEL_ID_ORBIT)){
		            Log.d(TAG,"Fetch recording plan onResume");
		            getRecordingPlan();
	            }

                if(nightLightHelper.isSupportNightLight(mDevice.getProfile().getRegistrationId())) {
                    getNightLightIfAvailable(true);
                }

            }
			long settingLoadTime = System.currentTimeMillis() - settings.getLong(CommonConstants.SETTINGS_CLICKED_TIME,0);

			int time =(int) settingLoadTime / 1000;
			String settingsLaunchTime;
			Log.d("LoginTime","LoginTime : "+settingLoadTime + " Sec = "+time);
			if(time<=1){
				settingsLaunchTime = "1 sec";
			}else if(time>1 && time<=3){
				settingsLaunchTime = "3 sec";
			}else if(time>3 && time<=5){
				settingsLaunchTime = "5 sec";
			}else if(time>5 && time<=10){
				settingsLaunchTime = "10 sec";
			}else{
				settingsLaunchTime = ">10 sec";
			}
			GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SETTINGS,AppEvents.SETTINGS_LOAD_TIME+" : "+ settingsLaunchTime,AppEvents.SETTINGS_LOAD_TIME);
			ZaiusEvent settingsLoadEvt = new ZaiusEvent(AppEvents.SETTINGS);
			settingsLoadEvt.action(AppEvents.SETTINGS_LOAD_TIME+" : "+ settingsLaunchTime);
			try {
				ZaiusEventManager.getInstance().trackCustomEvent(settingsLoadEvt);
			} catch (ZaiusException e) {
				e.printStackTrace();
			}
			settings();

		}

		public void onEventMainThread(SendCommandEvent sendCommandEvent) {
			if (applyingDialog != null && applyingDialog.isShowing()) {
				try {
					applyingDialog.dismiss();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		private CameraSettingsActor.Interface mActorInterface = new CameraSettingsActor.Interface() {
            @Override
            public void onDataSetChanged(ListChild listChild) {
                Log.d("CameraSettingsActivity", "Notify data set changed step 1");
                try {
                    if (listChild.equals(socVersion) && socVersion != null && !socVersion.value.equalsIgnoreCase("Loading…")) {
                        if (slaveFirmware.isShown()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    slaveFirmware.setText(socVersion.value);
                                }
                            });

                        }
                        CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOC_SLAVE_FW, socVersion.value);

                    }

                    if (listChild.equals(timezone) && timezone != null && !timezone.value.equalsIgnoreCase("Loading…")) {
                        if (currentTimeZone.isShown()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentTimeZone.setText(timezone.value);
                                }
                            });

                        }
                        CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.TIMEZONE, timezone.value);

                    }

                    if(listChild.equals(mcuVersionListChild) && mcuVersionListChild != null && !mcuVersionListChild.value.equalsIgnoreCase(getResources().getString(R.string.loading)))
					{
						if(mMCUVersionTv != null)
						{
							mMCUVersionTv.setText(mcuVersionListChild.value);
						}
					}

                    //if (generalSettingsVisible) {
                    if (listChild.equals(ceilingMount)) {
                        if (ceilingMount != null && ceilingMount.value.equalsIgnoreCase(getSafeString(R.string.on))) {
                            if (generalSettingsVisible) {
                                ceilingSwitch.setChecked(true);
                            }
                            CommonUtil.setSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.CEILING_MOUNT, true);
                        } else {
                            if (generalSettingsVisible) {
                                ceilingSwitch.setChecked(false);
                            }
                            CommonUtil.setSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.CEILING_MOUNT, false);
                        }
                    }

                    if (listChild.equals(brightness) && brightness != null && !brightness.value.equalsIgnoreCase(getSafeString(R.string.failed_to_retrieve_camera_data)) && !brightness.value.equalsIgnoreCase("Loading…")) {
                        if (generalSettingsVisible) {
                            brightnessSeekBar.setProgress(brightness.intValue -1 );
                        }
                        CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.BRIGHTNESS, brightness.intValue);
                    }

                    if (listChild.equals(volume) && volume != null && !volume.value.equalsIgnoreCase(getSafeString(R.string.failed_to_retrieve_camera_data)) && !volume.value.equalsIgnoreCase("Loading…")) {
                        if (generalSettingsVisible) {
                            volumeSeekbar.setProgress(volume.intValue);
                        }
                        CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VOLUME, volume.intValue);

                    }

                    if (generalSettingsVisible && overlayLayout.isShown()) {
                        if (listChild.equals(overlayDate)) {
                            boolean isOverlayOn = false;
                            if (overlayDate != null && overlayDate.value.equalsIgnoreCase(getSafeString(R.string.on)))
                                isOverlayOn = true;
                            overlayDateSwitch.setOnCheckedChangeListener(null);
                            overlayDateSwitch.setChecked(isOverlayOn);
                            overlayDateSwitch.setOnCheckedChangeListener(CameraSettingsActivity.this);
                            CommonUtil.setSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.OVERLAY_DATE, isOverlayOn);
                        }
                    }

                    if (generalSettingsVisible && viewModeLayout.isShown()) {
                        if (listChild.equals(viewMode)) {
                            if (viewMode != null ){

                                wide.setOnClickListener(null);
                                narrow.setOnClickListener(null);
                                if(viewMode.intValue == 0) {
                                    wide.setChecked(true);

                                }
                                else
                                     narrow.setChecked(true);
                                wide.setOnClickListener(CameraSettingsActivity.this);
                                narrow.setOnClickListener(CameraSettingsActivity.this);
                            }else{

                            }
                            CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIEW_MODE, viewMode.intValue);
                        }
                    }

                    if (listChild.equals(lensCorrection)) {
                        if (lensCorrection != null && !lensCorrection.value.equalsIgnoreCase(getSafeString(R.string.failed_to_retrieve_camera_data)) && !lensCorrection.value.equalsIgnoreCase("Loading…")) {
                            boolean isLensCorrectionOn= false;
                            if (lensCorrection.value.equalsIgnoreCase(getSafeString(R.string.on)))
                                isLensCorrectionOn = true;

                                CommonUtil.setSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LENS_CORRECTION, isLensCorrectionOn);

                                if (isNotiSettingsVisible) {
                                    lensCorrectionSwitch.setOnCheckedChangeListener(null);
                                    lensCorrectionSwitch.setChecked(isLensCorrectionOn);
                                    lensCorrectionSwitch.setOnCheckedChangeListener(CameraSettingsActivity.this);
                                }
                            }/* else {
                                if (isNotiSettingsVisible)
                                    lensCorrectionSwitch.setChecked(false);
                                CommonUtil.setSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LENS_CORRECTION, false);
                            }*/

                    }

                    if (listChild.equals(videoRecording)) {

                        if (videoRecording != null && !videoRecording.value.equalsIgnoreCase(getSafeString(R.string.failed_to_retrieve_camera_data)) && !videoRecording.value.equalsIgnoreCase("Loading…")) {
	                        if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)){
		                        setUpVideoRecordingDuration(videoRecording.intValue);
	                        }else if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION))
	                        {
		                        setUpOrbitVRDFirmwareSetting(videoRecording.intValue);
	                        }
                        }

	                    if(!motionSwitch.isChecked()){
		                    mVideoRecordingLayout.setVisibility(View.GONE);
	                    }
                    }
					if (listChild.equals(nightligthListChild)) {
                        nightLightMode = nightligthListChild.intValue;
						switch(nightligthListChild.intValue) {
							case 0:
								nightLightOn.setChecked(false);
								nightLightOff.setChecked(false);
								nightLightAuto.setChecked(true);
								break;
							case 1:
								nightLightOn.setChecked(true);
								nightLightOff.setChecked(false);
								nightLightAuto.setChecked(false);
								break;
							case 2:
								nightLightOn.setChecked(false);
								nightLightOff.setChecked(true);
								nightLightAuto.setChecked(false);
								break;
						}
					}
                    if (listChild.equals(nightVision)){
                        if (nightVision != null && !nightVision.value.equalsIgnoreCase(getSafeString(R.string.failed_to_retrieve_camera_data)) && !brightness.value.equalsIgnoreCase("Loading…")) {
                            String nightVisionValueString = nightVision.value;
                            int nightVisionValue = NIGHTVISION_AUTO;

                            if (nightVisionValueString.equalsIgnoreCase("AUTO"))
                                nightVisionValue = NIGHTVISION_AUTO;
                            else if (nightVisionValueString.equalsIgnoreCase("ON"))
                                nightVisionValue = NIGHTVISION_ON;
                            else if (nightVisionValueString.equalsIgnoreCase("OFF"))
                                nightVisionValue = NIGHTVISION_OFF;

                            CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_VISION, nightVisionValue);

                            if (generalSettingsVisible) {
                                switch (nightVisionValue) {
                                    case NIGHTVISION_AUTO:
                                        nightVisionAuto.setChecked(true);
                                        nightVisionON.setChecked(false);
                                        nightVisionOFF.setChecked(false);

                                        break;
                                    case NIGHTVISION_ON:
                                        nightVisionAuto.setChecked(false);
                                        nightVisionON.setChecked(true);
                                        nightVisionOFF.setChecked(false);
                                        break;
                                    case NIGHTVISION_OFF:
                                        nightVisionAuto.setChecked(false);
                                        nightVisionON.setChecked(false);
                                        nightVisionOFF.setChecked(true);
                                        break;
                                    default:
                                        nightVisionAuto.setChecked(true);
                                        nightVisionON.setChecked(false);
                                        nightVisionOFF.setChecked(false);
                                        CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_VISION, NIGHTVISION_AUTO);

                                        break;
                                }
                            }

                        }
                }
                    //}
                    try {
                        if (applyingDialog != null && applyingDialog.isShowing()) {
                            applyingDialog.dismiss();
                        }
                        if (loadingDailog != null && loadingDailog.isShowing()) {
                            loadingDailog.dismiss();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onNotificationSettingsReceived()
			{
                if (streetCameraSwitch.isShown()) {
                    if (CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_STATUS) ||
                            CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_STATUS) ||
                            CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_STATUS) ||
                            CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS)) {
                        llCameraSettings.setEnabled(true);
                        llGeneralSettings.setEnabled(true);

                        txtCemeraSettings.setEnabled(true);
                        txtGeneralSettings.setEnabled(true);

                        imgCameraSettings.setEnabled(true);
                        imgGeneralSettings.setEnabled(true);

                        txtCemeraSettings.setTextColor(getResources().getColor(R.color.color_blue));
                        txtGeneralSettings.setTextColor(getResources().getColor(R.color.color_blue));

                    } else {
                        streetCameraSwitch.setChecked(false);
                        txtCemeraSettings.setTextColor(getResources().getColor(R.color.text_gray));
                        txtGeneralSettings.setTextColor(getResources().getColor(R.color.text_gray));

                        llCameraSettings.setEnabled(false);
                        llGeneralSettings.setEnabled(false);

                        txtCemeraSettings.setEnabled(false);
                        txtGeneralSettings.setEnabled(false);

                        imgCameraSettings.setEnabled(false);
                        imgGeneralSettings.setEnabled(false);
                    }
                }

                if (isNotiSettingsVisible ) {
                    setupSoundOrMotionValueField(soundDetection);

                    setupGenericMotionValueField(motionDetection);
                    setupTemperatureValueField(temperature);

	                if (!mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
			                || Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)) {
		                Log.d(TAG,"notification received setUpMotionVideoRecording");
		                setUpMotionVideoRecording();
	                }

//				runOnUiThreadIfVisible(new Runnable() {
//					@Override
//					public void run() {
//						Log.d(TAG, "onNotificationSettingsReceived data set changed step 1");
//						try {
//							//listAdapter.notifyDataSetChanged();
//						} catch (Exception ex) {
//							Log.d(TAG, "Error when notify dataset changed");
//							ex.printStackTrace();
//						}
//					}
//				});

                }
                if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
				                && Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)) {
		            Log.d(TAG,"Orbit on plan.Fetch video recording duration");
	                 getVideoRecordingDuration(true);

                }

                if (loadingDailog != null && loadingDailog.isShowing()) {
                    try {
                        loadingDailog.dismiss();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void onParkReceived(Pair<String, Object> response) {

            }

            @Override
            public void onParkTimerReceived(Pair<String, Object> response) {

            }

            public void onMotionNotificationChange(ListChild listChild, boolean shouldRevert, String responseMessage) {
                Log.d(TAG, "actor interface: " + responseMessage);

                if (CameraSettingsActivity.this != null) {
                    if (shouldRevert) {
                        listChild.revertToOldCopy();
                        //listAdapter.notifyDataSetChanged();
                        //cameraSettings();
                        CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS, listChild.booleanValue);

                        CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SENSITIVITY, listChild.intValue);

						motionSwitch.setOnCheckedChangeListener(null);
						motionSwitch.setChecked(listChild.booleanValue);
						if (!motionSwitch.isChecked()) {
							//motionSwitch.setChecked(false);
							ll_motionSensitivity.setVisibility(View.GONE);

							if(mDevice.getProfile().getModelId().equalsIgnoreCase("0877") ||
								mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72))
                                babyMdTypesLayout.setVisibility(View.GONE);


							if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
							&& Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION))
							{
								//mMotionVideoRecodingLayout.setVisibility(View.GONE);
								mVideoRecordingLayout.setVisibility(View.GONE);
							}
						} else {
							//motionSwitch.setChecked(true);
							ll_motionSensitivity.setVisibility(View.VISIBLE);

							if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT))
							{
								if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION)) {
									mvrLayout.setVisibility(View.VISIBLE);
									mVideoRecordingLayout.setVisibility(View.VISIBLE);
								}else {
									mVideoRecordingLayout.setVisibility(View.GONE);
									mvrLayout.setVisibility(View.GONE);
								}
							} else
							mvrLayout.setVisibility(View.VISIBLE);

                                if(mDevice.getProfile().getModelId().equalsIgnoreCase("0877")){
                                    int mdTypeIndex =  CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE);
                                    //  String mdType = NotificationSettingUtils.getMotionDetectionType(mdTypeIndex);
                                    switch(mdTypeIndex){
                                        case 0 :
                                            mdTypeNone.setOnClickListener(null);
                                            mdTypeNone.setChecked(true);
                                            mdTypeNone.setOnClickListener(CameraSettingsActivity.this);
                                            break;
                                        case 1:
                                            mdTypeRegular.setOnClickListener(null);
                                            mdTypeRegular.setChecked(true);
                                            mdTypeRegular.setOnClickListener(CameraSettingsActivity.this);
                                            break;
                                        case 2:
                                            mdTypeSleepAnalytics.setOnClickListener(null);
                                            mdTypeSleepAnalytics.setChecked(true);
                                            mdTypeSleepAnalytics.setOnClickListener(CameraSettingsActivity.this);
                                            ll_motionSensitivity.setVisibility(View.GONE);
                                            getBTARemainingTimeIfAvailable();
                                            break;
                                        case 3:
                                            mdTypeExpression.setOnClickListener(null);
                                            mdTypeExpression.setChecked(true);
                                            mdTypeExpression.setOnClickListener(CameraSettingsActivity.this);
                                            ll_motionSensitivity.setVisibility(View.GONE);
                                            break;
                                        default: mdTypeNone.setChecked(true);
                                            break;
                                    }
                            }

						}
						motionSwitch.setOnCheckedChangeListener(CameraSettingsActivity.this);


                    }
                    Toast.makeText(mContext, responseMessage, Toast.LENGTH_SHORT).show();
                }
                try {
                    if (applyingDialog != null && applyingDialog.isShowing()) {
                        applyingDialog.dismiss();
                    }
                    if (loadingDailog != null && loadingDailog.isShowing()) {
                        loadingDailog.dismiss();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onValueSet(final ListChild listChild, final boolean shouldRevert, final String responseMessage) {
                Log.d(TAG, "actor interface: " + responseMessage);
                Crittercism.leaveBreadcrumb(TAG + " actor interface: " + responseMessage);

                if (CameraSettingsActivity.this != null) {
                    if (shouldRevert) {
                        listChild.revertToOldCopy();
                        //listAdapter.notifyDataSetChanged();
                        if (listChild.equals(motionDetection))
                        {
                            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS, listChild.booleanValue);

                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SENSITIVITY, listChild.intValue);

                            switch (listChild.intValue)
							{
                                case 0:
                                    motionSentivity1.setOnClickListener(null);
                                    motionSentivity1.setChecked(true);
                                    motionSentivity1.setOnClickListener(CameraSettingsActivity.this);
                                    break;
                                case 1:
                                    motionSentivity2.setOnClickListener(null);
                                    motionSentivity2.setChecked(true);
                                    motionSentivity2.setOnClickListener(CameraSettingsActivity.this);
                                    break;
                                case 2:
                                    motionSentivity3.setOnClickListener(null);
                                    motionSentivity3.setChecked(true);
                                    motionSentivity3.setOnClickListener(CameraSettingsActivity.this);
                                    break;
                                case 3:
                                    motionSentivity4.setOnClickListener(null);
                                    motionSentivity4.setChecked(true);
                                    motionSentivity4.setOnClickListener(CameraSettingsActivity.this);
                                    break;
                                case 4:
                                    motionSentivity5.setOnClickListener(null);
                                    motionSentivity5.setChecked(true);
                                    motionSentivity5.setOnClickListener(CameraSettingsActivity.this);
                                    break;
                            }

                            if(mDevice.getProfile().getModelId().equalsIgnoreCase("0877"))
                            {
                                int mdTypeIndex =  CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE);
                              //  String mdType = NotificationSettingUtils.getMotionDetectionType(mdTypeIndex);
                                switch(mdTypeIndex){
                                    case 0 :
                                        mdTypeNone.setOnClickListener(null);
                                        mdTypeNone.setChecked(true);
                                        mdTypeNone.setOnClickListener(CameraSettingsActivity.this);
                                        break;
                                    case 1:
                                        mdTypeRegular.setOnClickListener(null);
                                        mdTypeRegular.setChecked(true);
                                        mdTypeRegular.setOnClickListener(CameraSettingsActivity.this);
                                        break;
                                    case 2:
                                        mdTypeSleepAnalytics.setOnClickListener(null);
                                        mdTypeSleepAnalytics.setChecked(true);
                                        mdTypeSleepAnalytics.setOnClickListener(CameraSettingsActivity.this);
                                        ll_motionSensitivity.setVisibility(View.GONE);
                                        getBTARemainingTimeIfAvailable();
                                        break;
                                    case 3:
                                        mdTypeExpression.setOnClickListener(null);
                                        mdTypeExpression.setChecked(true);
                                        mdTypeExpression.setOnClickListener(CameraSettingsActivity.this);
                                        ll_motionSensitivity.setVisibility(View.GONE);
                                        break;
                                    default: mdTypeNone.setChecked(true);
                                        break;
                                }

                            }
                            else if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72))
							{
								switch (motionDetection.motionSource)
								{
									case PublicDefine.MOTION_DETECTION_SOURCE:
										mdTypeRegular.setChecked(true);
										break;

									case PublicDefine.PIR_MOTION_DETECTION_SOURCE:
										mdTypeSleepAnalytics.setChecked(true);
										break;

									default:
										// default, we should display motion detection as source
										mdTypeRegular.setChecked(true);
										break;

								}
							}
                        }
                        if (listChild.equals(soundDetection)) {
                            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_STATUS, listChild.booleanValue);

                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_SENSITIVITY, listChild.intValue);
                            soundSwitch.setOnCheckedChangeListener(null);
                            soundSwitch.setChecked(listChild.booleanValue);
                            if (!soundSwitch.isChecked()) {
                                //motionSwitch.setChecked(false);
                                ll_soundSensitivity.setVisibility(View.GONE);

                            } else {
                                //motionSwitch.setChecked(true);

                                ll_soundSensitivity.setVisibility(View.VISIBLE);
                                switch (listChild.intValue) {
                                    case 25:
                                        soundSensitivity5.setOnClickListener(null);
                                        soundSensitivity5.setChecked(true);
                                        soundSensitivity5.setOnClickListener(CameraSettingsActivity.this);
                                        break;

                                    case 70:
                                        soundSensitivity3.setOnClickListener(null);
                                        soundSensitivity3.setChecked(true);
                                        soundSensitivity3.setOnClickListener(CameraSettingsActivity.this);
                                        break;

                                    case 80:
                                        soundSensitivity1.setOnClickListener(null);
                                        soundSensitivity1.setChecked(true);
                                        soundSensitivity1.setOnClickListener(CameraSettingsActivity.this);
                                        break;
                                }

                            }
                            soundSwitch.setOnCheckedChangeListener(CameraSettingsActivity.this);
                        } else if (listChild.equals(temperature)) {
                            tempSwitch.setOnCheckedChangeListener(null);
                            tempSwitch.setChecked(listChild.booleanValue);
                            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_STATUS, listChild.booleanValue);
                            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_STATUS, listChild.secondaryBooleanValue);
                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE, temperature.intValue);
                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_VALUE, temperature.secondaryIntValue);


                            if (!tempSwitch.isChecked()) {

                                //motionSwitch.setChecked(false);
                                ll_temparatureDetection.setVisibility(View.GONE);

                            } else {
                                //motionSwitch.setChecked(true);
                                ll_temparatureDetection.setVisibility(View.VISIBLE);
                                tempSeekbar.setSelectedMinValue(temperature.intValue);
                                tempSeekbar.setSelectedMaxValue(temperature.secondaryIntValue);
                                    /*tempSeekbar.setMinValue((float)temperature.intValue);
                                    tempSeekbar.setMaxValue((float)temperature.secondaryIntValue);*/
									/*int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);

									if (savedTempUnit != PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
										mfahrenheit.setChecked(true);

									}else{

										mCentigrade.setChecked(true);
									}*/

                            }
                            tempSwitch.setOnCheckedChangeListener(CameraSettingsActivity.this);
                        } else if (listChild.equals(nightVision)) {
                            nightVisionAuto.setOnCheckedChangeListener(null);
                            nightVisionON.setOnCheckedChangeListener(null);
                            nightVisionOFF.setOnCheckedChangeListener(null);
                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_VISION, listChild.intValue);
                            switch (listChild.intValue) {
                                case NIGHTVISION_AUTO:
                                    nightVisionAuto.setChecked(true);
                                    nightVisionON.setChecked(false);
                                    nightVisionOFF.setChecked(false);

                                    break;
                                case NIGHTVISION_ON:
                                    nightVisionAuto.setChecked(false);
                                    nightVisionON.setChecked(true);
                                    nightVisionOFF.setChecked(false);
                                    break;
                                case NIGHTVISION_OFF:
                                    nightVisionAuto.setChecked(false);
                                    nightVisionON.setChecked(false);
                                    nightVisionOFF.setChecked(true);
                                    break;
                                default:
                                    nightVisionAuto.setChecked(true);
                                    nightVisionON.setChecked(false);
                                    nightVisionOFF.setChecked(false);

                            }
                            nightVisionAuto.setOnCheckedChangeListener(CameraSettingsActivity.this);
                            nightVisionON.setOnCheckedChangeListener(CameraSettingsActivity.this);
                            nightVisionOFF.setOnCheckedChangeListener(CameraSettingsActivity.this);
                        } else if (listChild.equals(ceilingMount)) {
                            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.CEILING_MOUNT, listChild.booleanValue);
                            ceilingSwitch.setOnCheckedChangeListener(null);
                            ceilingSwitch.setChecked(listChild.booleanValue);
                            ceilingSwitch.setOnCheckedChangeListener(CameraSettingsActivity.this);
                        } else if (listChild.equals(lensCorrection)) {
                            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LENS_CORRECTION, listChild.booleanValue);
                            lensCorrectionSwitch.setOnCheckedChangeListener(null);
                            lensCorrectionSwitch.setChecked(listChild.booleanValue);
                            lensCorrectionSwitch.setOnCheckedChangeListener(CameraSettingsActivity.this);
                        } else if (listChild.equals(videoRecording)) {
                            if (BuildConfig.DEBUG)
                                Log.d(TAG, "Fail to Apply Video Recording duration and value :- " + listChild.intValue);

                            CommonUtil.setVideoRecording(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_RECORDING, listChild.oldIntValue);
	                        setSpinnerSelectionWithoutChangeEvent(previousSelectionValue);
	                        setUpVideoRecordingDuration(listChild.oldIntValue);
	                        if(listChild.oldIntValue > 0){
		                        motionDetection.secondaryBooleanValue = true;
	                        }else {
		                        motionDetection.secondaryBooleanValue = false;
	                        }

                        } else if (listChild.equals(volume)) {
                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VOLUME, listChild.intValue);
                            volumeSeekbar.setOnSeekBarChangeListener(null);
                            volumeSeekbar.setProgress(listChild.intValue);
                            volumeSeekbar.setOnSeekBarChangeListener(CameraSettingsActivity.this);
                        } else if (listChild.equals(brightness)) {
                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.BRIGHTNESS, listChild.intValue);
                            brightnessSeekBar.setOnSeekBarChangeListener(null);
                            brightnessSeekBar.setProgress(listChild.intValue - 1);
                            brightnessSeekBar.setOnSeekBarChangeListener(CameraSettingsActivity.this);
                        } else if (listChild.equals(timezone)) {
                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.TIMEZONE, listChild.value);
                            if (timeZoneCheck.isShown()) {
                                currentTimeZone.setText(timezone.value);
                            }

                        } else if (listChild.equals(overlayDate)) {
                            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.OVERLAY_DATE, listChild.booleanValue);
                            overlayDateSwitch.setOnCheckedChangeListener(null);
                            overlayDateSwitch.setChecked(listChild.booleanValue);
                            overlayDateSwitch.setOnCheckedChangeListener(CameraSettingsActivity.this);
                        }

                    } else {
                        if (listChild.equals(nightVision)) {
                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_VISION, listChild.intValue);
                        } else if (listChild.equals(ceilingMount)) {
                            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.CEILING_MOUNT, listChild.booleanValue);
                        } else if (listChild.equals(volume)) {
                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VOLUME, listChild.intValue);
                        } else if (listChild.equals(brightness)) {
                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.BRIGHTNESS, listChild.intValue);
                        } else if (listChild.equals(lensCorrection)) {
                            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LENS_CORRECTION, listChild.booleanValue);
                        } else if (listChild.equals(videoRecording)) {
                            if (BuildConfig.DEBUG)
                                Log.d(TAG, "Applied Video Recording duration and value :- " + listChild.intValue);
                            CommonUtil.setVideoRecording(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_RECORDING, listChild.intValue);
	                        if (listChild.intValue == 0) {
                                // recording off
                                //mMotionVideoRecodingLayout.setVisibility(View.VISIBLE);
                                //mOffRecordingRButton.setChecked(true);
                                motionDetection.secondaryBooleanValue = false;
	                            setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_OFF);
                                mVideoRecordingLayout.setVisibility(View.GONE);
                                mVideoRecordingDurationTv.setText(getResources().getString(R.string.no_recording));
                            } else if (listChild.intValue > 0) {
                                //mMotionVideoRecodingLayout.setVisibility(View.VISIBLE);
                                mVideoRecordingLayout.setVisibility(View.VISIBLE);
	                            motionDetection.secondaryBooleanValue = true;
	                            //mvr button will retain its value so no need to update that
                                //mSdcardRecordingRButton.setChecked(true);
                                mVideoRecordingDurationTv.setText(String.format(Locale.getDefault(), getResources().getString(R.string.video_recording_duration_time), String.valueOf(videoRecording.intValue)));
                            } else {
                                //mMotionVideoRecodingLayout.setVisibility(View.GONE);
	                            motionDetection.secondaryBooleanValue = false;
	                            setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_OFF);
                                mVideoRecordingLayout.setVisibility(View.GONE);
                            }
                        } else if (listChild.equals(timezone)) {
                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.TIMEZONE, listChild.value);
                            String timezoneString = timezone.value.substring(4).replace(":", ".");

                            double timezoneValue = (Float.valueOf(timezoneString));
                            DecimalFormat decimalFormat = new DecimalFormat("#.##");
                            double twoDigitsF = Double.valueOf(decimalFormat.format(timezoneValue));
                            mDevice.getProfile().setTimeZone(twoDigitsF);


                        }
                        Toast.makeText(mContext, responseMessage, Toast.LENGTH_SHORT).show();

                    }

                    if (applyingDialog != null && applyingDialog.isShowing()) {
                        try {
                            applyingDialog.dismiss();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
			@Override
			public void onScheduleDataReceived() {
				setScheduleData();
			}
		};

		@Override
		public void onClick(View v) {
			// actor = new CameraSettingsActor(getApplicationContext(), mDevice, mActorInterface);
			switch (v.getId()) {
				case R.id.ll_camera_details:
				case R.id.txtCameraDetails:
				case R.id.imgCameraDetails:
					cameraDetails();
					break;

				case R.id.ll_camera_settings:
				case R.id.txtCameraSettings:
				case R.id.imgCameraSettings:
                    if (!CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS)) {
                        getNotificationSettings();
                    }

                    cameraSettings();

				/*if(mDevice.getProfile().getModelId().equalsIgnoreCase("0080")){
					getMotionSensitivityOrbit();
				}else*/
					// getNotificationSettings();

					break;

				case R.id.ll_general_settings:
				case R.id.imgGeneralSettings:
				case R.id.txtGeneralSettings:
                    if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_VISION)) {
                        buildGroupSetting2Codes();
                        getSetting2IfAvailable(PublicDefine.SETTING_2_KEY_CEILING_MOUNT);
                    }
					generalSettings();
					break;
				case R.id.closeGeneralSettings:
					generalSettingsVisible = false;
					settings();
					break;
				case R.id.closeCameraDetails:
					isNotiSettingsVisible = false;

					settings();
					break;
				case R.id.settings_back:
					onBackPressed();
					break;
				case R.id.delete_camera:
					removeDialogShowing = true;
					showRemoveDialog();
					break;
				case R.id.edit_cameraName:
					AnalyticsInterface.getInstance().trackEvent(AppEvents.EDIT_CAMERA_NAME, AppEvents.EDIT_CAMERANAME_CLICKED, eventData);
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.EDIT_CAMERA_NAME_CLICKED,AppEvents.EDIT_CAMERA_NAME);
					ZaiusEvent editCameraNameEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
					editCameraNameEvt.action(AppEvents.EDIT_CAMERA_NAME_CLICKED);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(editCameraNameEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
					showChangeCameraNameDialog(false);
					break;
			/*	case R.id.rb_celsius:
                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_TEMP_FORMAT_C,AppEvents.TEMP_CELSIUS);
					ZaiusEvent cameraTempEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
					cameraTempEvt.action(AppEvents.CAMERA_TEMP_FORMAT_C);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(cameraTempEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
					settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
					celsius.setChecked(true);
					fahrenheit.setChecked(false);
					break;
				case R.id.rb_fh:
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_TEMP_FORMAT_F,AppEvents.TEMP_FORENHEIT);
					ZaiusEvent cameraTempFormatEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
					cameraTempFormatEvt.action(AppEvents.CAMERA_TEMP_FORMAT_F);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(cameraTempFormatEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
					settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_F);
					fahrenheit.setChecked(true);
					celsius.setChecked(false);
					break;*/
			/*	case R.id.rb_24:
					AnalyticsInterface.getInstance().trackEvent(AppEvents.TIMEFORMAT24, AppEvents.TIME_FORMAT_24, eventData);
                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.TIME_FORMAT_24,AppEvents.TIMEFORMAT24);
					ZaiusEvent timeFormatEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
					timeFormatEvt.action(AppEvents.TIME_FORMAT_24);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(timeFormatEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
					settings.putInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, PublicDefineGlob.PREFS_TIME_FORMAT_UNIT_24);
					hourFormat24.setChecked(true);
					hourFormat12.setChecked(false);
					break;
				case R.id.rb_12:
					AnalyticsInterface.getInstance().trackEvent(AppEvents.TIMEFORMAT12, AppEvents.TIME_FORMAT_12, eventData);
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.TIME_FORMAT_12,AppEvents.TIMEFORMAT12);
					ZaiusEvent timeEventEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
					timeEventEvt.action(AppEvents.TIME_FORMAT_12);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(timeEventEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}


					settings.putInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, PublicDefineGlob.PREFS_TIME_FORMAT_UNIT_12);
					hourFormat12.setChecked(true);
					hourFormat24.setChecked(false);
					break;*/
				case R.id.delete_all_events:
					AnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DELETE_ALL_EVENTS,AppEvents.CAMERA_DELETE_ALL_EVENTS_CLICKED,eventData);
					ZaiusEvent deleteEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
					deleteEvt.action(AppEvents.CAMERA_DELETE_ALL_EVENTS_CLICKED);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(deleteEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_DELETE_ALL_EVENTS_CLICKED,AppEvents.DELETE_ALL_EVENTS);
					showDeleteAllEventsDialog();
					break;
				case R.id.rb_nv_auto:
					AnalyticsInterface.getInstance().trackEvent(AppEvents.NIGHTVISION, AppEvents.NIGHT_VISIONAUTO, eventData);
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.GENERAL_SETTING,AppEvents.NIGHTVISION_AUTO,AppEvents.NIGHT_VISION_AUTO);
					ZaiusEvent nightVisionEvt = new ZaiusEvent(AppEvents.GENERAL_SETTING);
					nightVisionEvt.action(AppEvents.NIGHTVISION_AUTO);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(nightVisionEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
					nightVisionAuto.setChecked(true);
					nightVisionOFF.setChecked(false);
					nightVisionON.setChecked(false);
					setNightVisionHubble(NIGHTVISION_AUTO);

					break;
				case R.id.rb_nv_on:
					AnalyticsInterface.getInstance().trackEvent(AppEvents.NIGHTVISION, AppEvents.NIGHT_VISIONON, eventData);
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.GENERAL_SETTING,AppEvents.NIGHTVISION_ON,AppEvents.NIGHT_VISION_ON);
					ZaiusEvent nightVisionOnEvt = new ZaiusEvent(AppEvents.GENERAL_SETTING);
					nightVisionOnEvt.action(AppEvents.NIGHTVISION_ON);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(nightVisionOnEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
					nightVisionAuto.setChecked(false);
					nightVisionOFF.setChecked(false);
					nightVisionON.setChecked(true);
					setNightVisionHubble(NIGHTVISION_ON);
					break;
				case R.id.rb_nv_off:
					AnalyticsInterface.getInstance().trackEvent(AppEvents.NIGHTVISION, AppEvents.NIGHT_VISIONOFF, eventData);
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.GENERAL_SETTING,AppEvents.NIGHTVISION_OFF,AppEvents.NIGHT_VISION_OFF);
					ZaiusEvent nightVisionModeEvt = new ZaiusEvent(AppEvents.GENERAL_SETTING);
					nightVisionModeEvt.action(AppEvents.NIGHTVISION_OFF);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(nightVisionModeEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
					nightVisionAuto.setChecked(false);
					nightVisionOFF.setChecked(true);
					nightVisionON.setChecked(false);
					setNightVisionHubble(NIGHTVISION_OFF);
					break;
				case R.id.radio_vr_cloud:
					if (mDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0) {
						if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)){
							enableMvrOnCloud();
						}else if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION))
						{
							int currentRecordingDuration = CommonUtil.getVideoRecording(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VIDEO_RECORDING);
							if(currentRecordingDuration != -1 && currentRecordingDuration != 0)
								SetVideoRecordingDuration(currentRecordingDuration,currentRecordingDuration);
							else
								SetVideoRecordingDuration(PublicDefine.VIDEO_RECORDING_DEFAULT_DURATION,currentRecordingDuration);
						}
					}else {
						enableMvrOnCloud();
					}
					break;
				case R.id.radio_vr_sdcard:
					if (mDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0) {
						if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)){
							enableMvrOnSDCard();
						}else if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION))
						{
							int currentRecordingDuration = CommonUtil.getVideoRecording(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VIDEO_RECORDING);
							if(currentRecordingDuration != -1 && currentRecordingDuration != 0)
								SetVideoRecordingDuration(currentRecordingDuration,currentRecordingDuration);
							else
								SetVideoRecordingDuration(PublicDefine.VIDEO_RECORDING_DEFAULT_DURATION,currentRecordingDuration);
						}
					}else {
						enableMvrOnSDCard();
					}

					break;
				case R.id.radio_vr_off:
					if (mDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0) {
						if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)){
							disableMvr();
						}else if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION))
						{
							int currentRecordingDuration = CommonUtil.getVideoRecording(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VIDEO_RECORDING);
							SetVideoRecordingDuration(PublicDefine.VIDEO_RECORDING_OFF_DURATION,currentRecordingDuration);
						}
					}else {
						disableMvr();
					}
					break;
				case radio_remove_clip:
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.VIDEO_STORAGE_MODE_SDCARD_FULL+" : "+AppEvents.REMOVE_OLDEST_10CLIPS,AppEvents.SD_CARD_FULL);
					ZaiusEvent sdCardFullEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
					sdCardFullEvt.action(AppEvents.VIDEO_STORAGE_MODE_SDCARD_FULL+" : "+AppEvents.REMOVE_OLDEST_10CLIPS);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(sdCardFullEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
					setRecordingPlan("0");
					break;
				case R.id.radio_switch_cloud:
					if (!hasSubscription()) {
						showMvrWarningDialog();
						mRemoveSdcardClip.setChecked(true);
					} else {
						setRecordingPlan("1");
					}
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.VIDEO_STORAGE_MODE_SDCARD_FULL+" : "+AppEvents.SWITCH_TO_CLOUD,AppEvents.SD_CARD_FULL);
					ZaiusEvent sdCardFullCloudEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
					sdCardFullCloudEvt.action(AppEvents.VIDEO_STORAGE_MODE_SDCARD_FULL+" : "+AppEvents.SWITCH_TO_CLOUD);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(sdCardFullCloudEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
					break;
				case R.id.radio_sound0:
					setSoundThreshold(80);
					break;

				case R.id.radio_sound2:
					setSoundThreshold(70);
					break;

				case R.id.radio_sound4:
					setSoundThreshold(25);
					break;
				case R.id.radio_motion1:

					if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)) {
						ll_motionSensitivity.setVisibility(View.GONE);
						motionSwitch.setChecked(false);
					} else {
						ll_motionSensitivity.setVisibility(View.VISIBLE);
						setMotionSenitivityValue(0);
					}
					break;
				case R.id.radio_motion2:
					setMotionSenitivityValue(1);
					break;
				case R.id.radio_motion3:
					setMotionSenitivityValue(2);
					break;
				case R.id.radio_motion4:
					setMotionSenitivityValue(3);
					break;
				case R.id.radio_motion5:
					setMotionSenitivityValue(4);
					break;
                case R.id.rb_snap_auto:
                    snapShotChoose.setOnClickListener(null);
                    snapShotChoose.setChecked(false);
                    snapShotAuto.setTextColor(mContext.getResources().getColor(R.color.color_dark_blue));
                    snapShotChoose.setTextColor(mContext.getResources().getColor(R.color.help_text_gray));
                    snapShotCapture.setImageResource(R.drawable.snap_shot_disabled);
                    snapShotChoose.setOnClickListener(this);
                    CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE, true) ;

                    break;
                case R.id.rb_snap_choose:
                    snapShotAuto.setOnClickListener(null);
                    snapShotAuto.setChecked(false);
                    snapShotChoose.setTextColor(mContext.getResources().getColor(R.color.color_dark_blue));
                    snapShotAuto.setTextColor(mContext.getResources().getColor(R.color.help_text_gray));
                    snapShotCapture.setImageResource(R.drawable.snap_shot_enabled);
                    snapShotAuto.setOnClickListener(this);
                    CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE, false) ;

                    break;
                case R.id.snap_choose:
                    showChangeImageDialog();
                    break;
                case R.id.timezonecheck:
                    showTimezoneDialog();
                    break;
                case R.id.motion_none:
                    break;
                case R.id.motion_regular:
                	if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72))
					{
						setMotionSource(PublicDefine.MOTION_DETECTION_SOURCE);
					}
                	else
					{
						ll_motionSensitivity.setVisibility(View.VISIBLE);
						int previousPosition = CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE);
						motionDetection.intValue = CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SENSITIVITY);

						if (previousPosition == 2 && remainingBTATime > 0) {
							showWarningDialogAboutBTA(1, previousPosition, mDevice);
						} else
							setMotionDetectionVda(1, motionDetection.intValue, previousPosition);
					}
                    break;

                case R.id.motion_sleep_analytics:
					if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72))
					{
						setMotionSource(PublicDefine.PIR_MOTION_DETECTION_SOURCE);
					}
					else
					{
						ll_motionSensitivity.setVisibility(View.GONE);
						int previousposition1 = CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE);

						applyBSCBSD(2, previousposition1);
					}
                    break;
                case R.id.motion_expression:
                    ll_motionSensitivity.setVisibility(View.GONE);
                    int previousposition2 = CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE);

                    applyBSCBSD(3, previousposition2);
                    break;
                case R.id.narrow:
                    viewMode.setOldCopy();
                    viewMode.intValue = 1;
                    showWarningViewModeChange();
                    break;
                case R.id.wide:
                    viewMode.setOldCopy();
                    viewMode.intValue = 0;
                    showWarningViewModeChange();
                    break;
				case R.id.rb_nl_auto:
					nightLightAuto.setChecked(true);
					nightLightOff.setChecked(false);
					nightLightOn.setChecked(false);
					setNightLightIfAvailable(NIGHT_LIGHT_AUTO);

					break;
				case R.id.rb_nl_on:
					nightLightAuto.setChecked(false);
					nightLightOff.setChecked(false);
					nightLightOn.setChecked(true);
					setNightLightIfAvailable(NIGHT_LIGHT_ON);
					break;
				case R.id.rb_nl_off:
					nightLightAuto.setChecked(false);
					nightLightOff.setChecked(true);
					nightLightOn.setChecked(false);
					setNightLightIfAvailable(NIGHT_LIGHT_OFF);
					break;

			}

		}

		private void settings() {

            if(mDevice == null || mDevice.getProfile() == null){
                finish();
                return;
            }
			setContentView(R.layout.camera_settings_layout);
			isSettings = true;
			cameraNameHeader = (TextView) findViewById(R.id.txtStreetCamera);
            settingsHeaderCamera = (TextView) findViewById(R.id.settingsHeaderCamera) ;
			cameraNameHeader.setText(mDevice.getProfile().getName());
            cameraNameHeader.setVisibility(View.GONE);
            String cameraName = mDevice.getProfile().getName();
            if(cameraName.length() > 10){
                cameraName = cameraName.substring(0,10) + "...";
            }
            settingsHeaderCamera.setText("(" + cameraName + ")");
			imgSettingsBack = (ImageView) findViewById(R.id.settings_back);
			deleteCamera = (ImageView) findViewById(R.id.delete_camera);
			imgCameraDetails = (ImageView) findViewById(R.id.imgCameraDetails);
			imgCameraSettings = (ImageView) findViewById(R.id.imgCameraSettings);
			imgGeneralSettings = (ImageView) findViewById(R.id.imgGeneralSettings);
			settingsHeader = (TextView) findViewById(R.id.settingsHeader);
			imgGeneralSettings.setOnClickListener(this);
			imgCameraSettings.setOnClickListener(this);
			imgCameraDetails.setOnClickListener(this);
			imgSettingsBack.setOnClickListener(this);
			deleteCamera.setOnClickListener(this);

			llCameraDetails = (LinearLayout) findViewById(R.id.ll_camera_details);
			llCameraSettings = (LinearLayout) findViewById(R.id.ll_camera_settings);
			llGeneralSettings = (LinearLayout) findViewById(R.id.ll_general_settings);
			llStreetCamera = (LinearLayout) findViewById(R.id.ll_street_camera);
			llCameraDetails.setOnClickListener(this);
			llCameraSettings.setOnClickListener(this);
			llGeneralSettings.setOnClickListener(this);

			txtCameraDetails = (TextView) findViewById(R.id.txtCameraDetails);
			txtCemeraSettings = (TextView) findViewById(R.id.txtCameraSettings);
			txtGeneralSettings = (TextView) findViewById(R.id.txtGeneralSettings);

			txtCameraDetails.setOnClickListener(this);
			txtGeneralSettings.setOnClickListener(this);
			txtCemeraSettings.setOnClickListener(this);


			streetCameraSwitch = (SwitchCompat) findViewById(R.id.street_camera_switch);
            streetCameraSwitch.setVisibility(View.GONE);


            if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT))
            {
                checkDeviceStatus(mDevice);
            }else {

                if (mDevice.getProfile().isAvailable() && !isCameraOff(mDevice)) {
                    streetCameraSwitch.setChecked(true);
                    txtCemeraSettings.setTextColor(getResources().getColor(R.color.color_blue));
                    txtGeneralSettings.setTextColor(getResources().getColor(R.color.color_blue));
                    llCameraSettings.setEnabled(true);
                    llGeneralSettings.setEnabled(true);

                    txtCemeraSettings.setEnabled(true);
                    txtGeneralSettings.setEnabled(true);

                    imgCameraSettings.setEnabled(true);
                    imgGeneralSettings.setEnabled(true);
                } else {
                    if(isCameraOff(mDevice)){
                        txtCameraDetails.setTextColor(getResources().getColor(R.color.text_gray));
                        txtCameraDetails.setEnabled(false);
                        llCameraDetails.setEnabled(false);
                        imgCameraDetails.setEnabled(false);
                    }
                    streetCameraSwitch.setChecked(false);
                    txtCemeraSettings.setTextColor(getResources().getColor(R.color.text_gray));
                    txtGeneralSettings.setTextColor(getResources().getColor(R.color.text_gray));

                    llCameraSettings.setEnabled(false);
                    llGeneralSettings.setEnabled(false);

                    txtCemeraSettings.setEnabled(false);
                    txtGeneralSettings.setEnabled(false);

                    imgCameraSettings.setEnabled(false);
                    imgGeneralSettings.setEnabled(false);
                }
            }


		}

        private boolean isCameraOff(Device device){
            if(device != null) {
                String mPrivacyMode = device.getProfile().getDeviceAttributes().getPrivacyMode();
                // mIsNotificationOn = selectedDevice.getProfile().getDeviceAttributes().getPrivacyMode();//mSharedPreferences.getBoolean(selectedDevice.getProfile().getName()+"notification", true);
                if ((mPrivacyMode == null) || (!TextUtils.isEmpty(mPrivacyMode) && mPrivacyMode.equalsIgnoreCase("0"))) {
                    return false; // Not in privcay mode
                } else {
                    return true;
                }

            }
            return false;
        }

		private void cameraDetails() {
			AnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL, AppEvents.CAMERA_DETAILS_CLICKED, eventData);
			setContentView(R.layout.camera_details);
			GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_DETAILS_CLICKED,AppEvents.CAMERA_FW_CLICKED);
			ZaiusEvent camerDetailsEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
			camerDetailsEvt.action(AppEvents.CAMERA_DETAILS_CLICKED);
			try {
				ZaiusEventManager.getInstance().trackCustomEvent(camerDetailsEvt);
			} catch (ZaiusException e) {
				e.printStackTrace();
			}

			isSettings = false;
			cameraName = (TextView) findViewById(R.id.cameraModelName);
			cameraId = (TextView) findViewById(R.id.cameraid);
            snapShotAuto = (RadioButton) findViewById(R.id.rb_snap_auto);
            snapShotChoose = (RadioButton) findViewById(R.id.rb_snap_choose);
            snapShotCapture = (ImageView) findViewById(R.id.snap_choose);

			macAddress = (TextView) findViewById(R.id.macAddress);

			mMacAddressLayout = (LinearLayout)findViewById(R.id.mac_address_layout);


			firmwareLayout = (RelativeLayout) findViewById(R.id.firmwarelayout);

			firmwareVersion = (TextView) findViewById(R.id.firmwareVersion);
			tempFormat = (LinearLayout) findViewById(R.id.selecttempformat);
			slaveFirmware = (TextView) findViewById(R.id.slaveFirmware);
            currentTimeZone = (TextView) findViewById(R.id.currenttimezone);
			currentPlan = (TextView) findViewById(R.id.currentPlan);
			currentPlanLayout = (LinearLayout) findViewById(R.id.curren_plan_layout);
			wifiStrength = (TextView) findViewById(R.id.wifiStrength);
			wifiLayout = (LinearLayout) findViewById(R.id.wifilayout);

			mMCULayout = (LinearLayout)findViewById(R.id.mcu_version_layout);
			mMCUVersionTv = (TextView)findViewById(R.id.mcu_version_tv);


			sendCameraLogs = (LinearLayout) findViewById(R.id.sendcameralog);
			closeCameraDetails = (ImageView) findViewById(R.id.closeCameraDetails);
			closeCameraDetails.setOnClickListener(this);
			/*celsius = (RadioButton) findViewById(R.id.rb_celsius);
			fahrenheit = (RadioButton) findViewById(R.id.rb_fh);
			hourFormat12 = (RadioButton) findViewById(R.id.rb_12);
			hourFormat24 = (RadioButton) findViewById(R.id.rb_24);*/
			editCameraName = (ImageView) findViewById(R.id.edit_cameraName);
			editCameraName.setOnClickListener(this);
			cameraName.setText(mDevice.getProfile().getName());
			cameraId.setText(mDevice.getProfile().getModelId());
			macAddress.setText(formatMacAddress(mDevice.getProfile().getMacAddress()));
			firmwareVersion.setText(mDevice.getProfile().getFirmwareVersion());
			currentPlan.setText(mDevice.getProfile().getPlanId());
			slaveCheck = (LinearLayout) findViewById(R.id.slavecheck);
            timeZoneCheck = (LinearLayout) findViewById(R.id.timezonecheck);

            timeZoneCheck.setOnClickListener(this);
			deleteAllEvents = (TextView) findViewById(R.id.delete_all_events);
			deleteAllEvents.setOnClickListener(this);
            snapShotAuto.setOnClickListener(this);
            snapShotChoose.setOnClickListener(this);
            snapShotCapture.setOnClickListener(this);

			timezone = new ListChild(getSafeString(R.string.timezone), "", true);

            if (BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equals("hubblenew")) {
                socVersion = new ListChild(getSafeString(R.string.socVersion), "", false);
            } else {
                socVersion = null;
            }

			if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)) {
				slaveCheck.setVisibility(View.GONE);
				tempFormat.setVisibility(View.GONE);
				currentPlanLayout.setVisibility(View.GONE);
				mMCULayout.setVisibility(View.VISIBLE);
				getMCUDetails();
			}
			else if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72))
			{
				slaveCheck.setVisibility(View.GONE);
				// assign null value so application will not try to fetch data.
				socVersion = null;
			}

            //showTimezoneDialog();
            getTimeZoneIfAvailable();

			getWifiStrengthIfAvailable();
            getSOCVersionIfAvailable();

			wifiLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					getWifiStrengthIfAvailable();
				}
			});

			firmwareLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_FW_CLICKED,AppEvents.CAMERA_FW_CLICKED,eventData);
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_FW_CLICKED,AppEvents.CAMERA_FW_CLICKED);
					ZaiusEvent camerFwEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
					camerFwEvt.action(AppEvents.CAMERA_FW_CLICKED);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(camerFwEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
					checkForFirmwareUpdate();
				}
			});

			sendCameraLogs.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    displayProgressDialog(false);
					sendCameraLogs sendCameraLog = new sendCameraLogs();
					sendCameraLog.execute();
				}
			});




			// This is applicable for Orbit device only
			if (BuildConfig.BUILD_TYPE.compareToIgnoreCase("debug") == 0 && mDevice.getProfile().isStandBySupported()) {
				mSdcardFormatLayout = (LinearLayout) findViewById(R.id.sdcard_format_layout);
				mSdcardFormatLayout.setVisibility(View.VISIBLE);
				mSdcardFormatLayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						sdcardFormat();
					}
				});
			}

			mOtaLinearLayout = (LinearLayout) findViewById(R.id.ota_option);
			mProdOtaSwitch = (SwitchCompat) findViewById(R.id.use_debug_switch);
			mProdOtaSwitch.setChecked(settings.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false));

			mProdOtaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton cb, boolean on) {
					if (BuildConfig.DEBUG)
						Log.d(TAG, "ota debug switch value :- " + on);
					if (on) {
						settings.putBoolean(DebugFragment.PREFS_USE_DEV_OTA, true);
					} else {
						settings.putBoolean(DebugFragment.PREFS_USE_DEV_OTA, false);
					}
				}
			});

			if (settings.getBoolean(DebugFragment.PREFS_DEBUG_ENABLED, false)) {
				mOtaLinearLayout.setVisibility(View.VISIBLE);
			} else {
				mOtaLinearLayout.setVisibility(View.GONE);
			}

            if(CommonUtil.checkSettings(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE)){
                if(CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE)){
                    CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE, true) ;
                    snapShotAuto.setChecked(true);
                    snapShotChoose.setChecked(false);
                    snapShotChoose.setTextColor(mContext.getResources().getColor(R.color.help_text_gray));
                    snapShotAuto.setTextColor(mContext.getResources().getColor(R.color.color_dark_blue));
                    snapShotCapture.setImageResource(R.drawable.snap_shot_disabled);
                }else{
                    CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE, false) ;
                    snapShotChoose.setChecked(true);
                    snapShotAuto.setChecked(false);
                    snapShotAuto.setTextColor(mContext.getResources().getColor(R.color.help_text_gray));
                    snapShotChoose.setTextColor(mContext.getResources().getColor(R.color.color_dark_blue));
                    snapShotCapture.setImageResource(R.drawable.snap_shot_enabled);
                }

            }else{
                CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE, true) ;
                snapShotAuto.setChecked(true);
                snapShotChoose.setChecked(false);
                snapShotChoose.setTextColor(mContext.getResources().getColor(R.color.help_text_gray));
                snapShotAuto.setTextColor(mContext.getResources().getColor(R.color.color_dark_blue));
                snapShotCapture.setImageResource(R.drawable.snap_shot_disabled);
            }


		}

		private void cameraSettings() {

			AnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTINGS, AppEvents.CAMERA_SETTINGS_CLICKED, eventData);
			isNotiSettingsVisible = true;
			setContentView(R.layout.activity_camera_settings);
			isSettings = false;
            String cameraName = mDevice.getProfile().getName();
            if(cameraName.length() > 10){
                cameraName = cameraName.substring(0,10) + "...";
            }
            settingsHeaderCamera = (TextView) findViewById(R.id.cameraname);
            settingsHeaderCamera.setText("(" + cameraName + ")");
			motionSwitch = (SwitchCompat) findViewById(R.id.switch_motionDetection);
			soundSwitch = (SwitchCompat) findViewById(R.id.switch_soundDetection);
			tempSwitch = (SwitchCompat) findViewById(R.id.switch_tempDetection);
			lensCorrectionSwitch = (SwitchCompat)findViewById(R.id.lens_correction_switch);
			ll_motionSensitivity = (LinearLayout) findViewById(R.id.ll_motionSensitivity);

            babyMdTypesLayout = (LinearLayout) findViewById(R.id.baby_md_types);

			mdTypeNone = (RadioButton) findViewById(R.id.motion_none);
			mdTypeNone.setOnClickListener(this);
			mdTypeRegular = (RadioButton) findViewById(R.id.motion_regular);
			mdTypeRegular.setOnClickListener(this);
			mdTypeSleepAnalytics = (RadioButton) findViewById(R.id.motion_sleep_analytics);
			mdTypeSleepAnalytics.setOnClickListener(this);
			mdTypeExpression = (RadioButton) findViewById(R.id.motion_expression);
			mdTypeExpression.setOnClickListener(this);

			if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72))
			{
				mdTypeRegular.setText(getResources().getString(R.string.motion_detection));
				mdTypeRegular.setVisibility(View.VISIBLE);

				mdTypeSleepAnalytics.setText(getResources().getString(R.string.pir_detection));
				mdTypeSleepAnalytics.setVisibility(View.VISIBLE);

				mdTypeExpression.setVisibility(View.GONE);
				mdTypeNone.setVisibility(View.GONE);
			}


			ll_soundSensitivity = (LinearLayout) findViewById(R.id.ll_soundSensitivity);
			ll_temparatureDetection = (LinearLayout) findViewById(R.id.ll_temparatureDetection);

			//mMotionVideoRecodingLayout  = (LinearLayout)findViewById(R.id.motion_recording_layout);
			mVideoRecordingLayout = (LinearLayout)findViewById(R.id.video_recording_layout);

			//mSdcardRecordingRButton = (RadioButton) findViewById(R.id.recording_sdcard_button);
			//mOffRecordingRButton  = (RadioButton)findViewById(R.id.recording_off_button);

			//mMotionRecordingGroup = (RadioGroup)findViewById(R.id.motion_recording_radio_group);

			mVideoRecordingDurationTv = (TextView)findViewById(R.id.video_recording_duration_tv);

			final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
			if(motionSwitch.isChecked()){
				GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.MOTIONDETECTION_ENABLED,AppEvents.MOTION_DETECTION_ENABLED);
				ZaiusEvent motionDetectionEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
				motionDetectionEvt.action(AppEvents.MOTIONDETECTION_ENABLED);
				try {
					ZaiusEventManager.getInstance().trackCustomEvent(motionDetectionEvt);
				} catch (ZaiusException e) {
					e.printStackTrace();
				}

			}else {
				GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.MOTIONDETECTION_DISABLED,AppEvents.MOTION_DETECTION_DISABLED);
				ZaiusEvent motionDetectionOffEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
				motionDetectionOffEvt.action(AppEvents.MOTIONDETECTION_DISABLED);
				try {
					ZaiusEventManager.getInstance().trackCustomEvent(motionDetectionOffEvt);
				} catch (ZaiusException e) {
					e.printStackTrace();
				}

			}
			mvrLayout = (LinearLayout) findViewById(R.id.mvrlayout);
			sdcardLayout = (LinearLayout) findViewById(R.id.sdcardlayout);

			motionLayout = (LinearLayout) findViewById(R.id.motionlayout);
			soundLayout = (LinearLayout) findViewById(R.id.soundlayout);
			temperatureLayout = (LinearLayout) findViewById(R.id.temperaturelayout);
			lensCorrectLayout = (LinearLayout) findViewById(R.id.lens_correct_layout);


			motionSentivity1 = (RadioButton) findViewById(R.id.radio_motion1);
			motionSentivity2 = (RadioButton) findViewById(R.id.radio_motion2);
			motionSentivity3 = (RadioButton) findViewById(R.id.radio_motion3);
			motionSentivity4 = (RadioButton) findViewById(R.id.radio_motion4);
			motionSentivity5 = (RadioButton) findViewById(R.id.radio_motion5);

			soundSensitivity1 = (RadioButton) findViewById(R.id.radio_sound0);
			soundSensitivity2 = (RadioButton) findViewById(R.id.radio_sound1);
			soundSensitivity3 = (RadioButton) findViewById(R.id.radio_sound2);
			soundSensitivity4 = (RadioButton) findViewById(R.id.radio_sound3);
			soundSensitivity5 = (RadioButton) findViewById(R.id.radio_sound4);

			mvrCould = (RadioButton) findViewById(R.id.radio_vr_cloud);
			mvrSdCard = (RadioButton) findViewById(R.id.radio_vr_sdcard);
			mvrOff = (RadioButton) findViewById(R.id.radio_vr_off);

			mRemoveSdcardClip = (RadioButton) findViewById(radio_remove_clip);
			mSwitchCloud = (RadioButton) findViewById(R.id.radio_switch_cloud);

			mCentigrade = (RadioButton) findViewById(R.id.rbcel);
			mfahrenheit = (RadioButton) findViewById(R.id.rbfheit);


			tempSeekbar = (RangeSeekBar) findViewById(R.id.rangeSeekbar);
			tempSeekbar.setRangeValues(10, 33);

			schedulerLayout = (RelativeLayout) findViewById(R.id.scheduler_layout);
			schedulerCurrentText = (TextView)findViewById(R.id.scheduler_current);
			schedulerNextText = (TextView)findViewById(R.id.scheduler_next);
			schedulerSwitch = (SwitchCompat)findViewById(R.id.scheduler_switch);
			schedulerSwitch.setOnCheckedChangeListener(CameraSettingsActivity.this);

			llNightLigth = (LinearLayout) findViewById(R.id.ll_night_light);
			nightLightAuto = (RadioButton) findViewById(R.id.rb_nl_auto);
			nightLightOff= (RadioButton) findViewById(R.id.rb_nl_off);
			nightLightOn = (RadioButton) findViewById(R.id.rb_nl_on);

			actor.getMotionSchedule();
			if (BuildConfig.ENABLE_MVR_SCHEDULING && mDevice.getProfile().isSupportMvrScheduling()) {
				setScheduleData();
			}

            //rangeSeekbar.setMinValue(50).setMaxValue(100);
      /*  tempSeekbar.setMinStartValue(14);
        tempSeekbar.setMaxStartValue(33);
        tempSeekbar.apply();

*/
			schedulerLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, MvrScheduleActivity.class);
					intent.putExtra("regId", mDevice.getProfile().getRegistrationId());
					if (mScheduleData != null) {
						intent.putExtra("schedule", mScheduleData.getSchedule());
						intent.putExtra("drawnData", mScheduleData.getScheduleData());
					}
					startActivityForResult(intent, FROM_MVR_SCHEDULE);
				}
			});


			boolean isSDCardSupported = mDevice.getProfile().doesSupportSDCardAccess();
			if (isSDCardSupported) {
				mvrSdCard.setVisibility(View.VISIBLE);
			} else {
				mvrSdCard.setVisibility(View.GONE);
			}

			if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)) {

				//mvrLayout.setVisibility(View.GONE);
				motionSentivity4.setVisibility(View.GONE);
                motionSentivity1.setVisibility(View.GONE);
				if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_SDCARD_CAPACITY_FIRMWARE_VERSION))
					lensCorrectLayout.setVisibility(View.VISIBLE);
				else
					lensCorrectLayout.setVisibility(View.GONE);

				if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION))
				{
					setUpOrbitPlanFirmwareSetting();

				}else if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION))
				{

					//mMotionVideoRecodingLayout.setVisibility(View.VISIBLE);
					setUpOrbitVRDFirmwareSetting(CommonUtil.getVideoRecording(mContext,mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VIDEO_RECORDING));

					if(!motionSwitch.isChecked())
					{
						//mMotionVideoRecodingLayout.setVisibility(View.GONE);
						mVideoRecordingLayout.setVisibility(View.GONE);
					}

				}
				else
				{
					//mMotionVideoRecodingLayout.setVisibility(View.GONE);
					mvrLayout.setVisibility(View.GONE);
					mVideoRecordingLayout.setVisibility(View.GONE);
				}

				/*mSdcardRecordingRButton.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View view)
					{
						int currentRecordingDuration = CommonUtil.getVideoRecording(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VIDEO_RECORDING);
						if(currentRecordingDuration != -1 && currentRecordingDuration != 0)
							SetVideoRecordingDuration(currentRecordingDuration,currentRecordingDuration);
						else
							SetVideoRecordingDuration(PublicDefine.VIDEO_RECORDING_DEFAULT_DURATION,currentRecordingDuration);
					}
				}); */

				/*mOffRecordingRButton.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View view)
					{
						int currentRecordingDuration = CommonUtil.getVideoRecording(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VIDEO_RECORDING);
						SetVideoRecordingDuration(PublicDefine.VIDEO_RECORDING_OFF_DURATION,currentRecordingDuration);
					}
				});*/

				mVideoRecordingLayout.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						final int currentRecordingDuration = CommonUtil.getVideoRecording(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VIDEO_RECORDING);
						final String[] durationArrays = getResources().getStringArray(R.array.video_duration_array);
						final int[] durationValues = getResources().getIntArray(R.array.video_duration_array_value);
						int selectedPos = -1;
						if(durationValues != null)
						{
							for(int count = 0; count < durationValues.length; count++)
							{
								if(currentRecordingDuration == durationValues[count])
								{
									selectedPos = count;
									break;
								}
							}
						}

						final int currentSelectedPos = selectedPos;
						AlertDialog.Builder builder;
						if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
							builder = new AlertDialog.Builder(CameraSettingsActivity.this);
						} else {
							builder = new AlertDialog.Builder(CameraSettingsActivity.this, AlertDialog.THEME_HOLO_LIGHT);
						}

						builder.setTitle(getResources().getString(R.string.video_recording_duration))

								.setSingleChoiceItems(durationArrays, currentSelectedPos, new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
									}
								})
								.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int id)
									{
										int selectedPosition= ((AlertDialog)dialog).getListView().getCheckedItemPosition();

										if(currentSelectedPos != selectedPosition && currentSelectedPos  != -1)
										{
											SetVideoRecordingDuration(durationValues[selectedPosition],durationValues[currentSelectedPos]);
										}
										else if(currentSelectedPos == -1)
										{
											SetVideoRecordingDuration(durationValues[selectedPosition],currentSelectedPos);
										}
										dialog.dismiss();

									}
								})
								.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int id)
									{
										dialog.dismiss();
									}
								});

						AlertDialog dialog = builder.create();

						dialog.setOnShowListener(new OnShowListener()
						{
							@Override
							public void onShow(DialogInterface dialog)
							{
								((AlertDialog)dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.text_blue));
								((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.text_blue));
								int textViewId = ((AlertDialog)dialog).getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
								if (textViewId != 0) {
									TextView tv = (TextView) ((AlertDialog)dialog).findViewById(textViewId);
									tv.setTextColor(getResources().getColor(R.color.text_blue));
								}
							}
						});

						dialog.show();




					}
				});
			}
			else
			{
                motionSentivity1.setVisibility(View.VISIBLE);
				motionSentivity4.setVisibility(View.VISIBLE);
				mvrLayout.setVisibility(View.VISIBLE);
				mvrCould.setChecked(true);
				lensCorrectLayout.setVisibility(View.GONE);

				//mMotionVideoRecodingLayout.setVisibility(View.GONE);
				mVideoRecordingLayout.setVisibility(View.GONE);

                if(mDevice.getProfile().getModelId().equals("0877"))
                {
                    if(mDevice.getProfile().getRegistrationId().startsWith("010877"))
                    {
                        String apiKey = Global.getApiKey(HubbleApplication.AppContext);

                        task = new BTATask(apiKey, mDevice, getApplicationContext(), mBTAInterface);
                    }

                    babyMdTypesLayout.setVisibility(View.VISIBLE);


                   	int mdTypeIndex =  CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE);
					String mdType = NotificationSettingUtils.getMotionDetectionType(mdTypeIndex);

					switch(mdTypeIndex)
					{
                        case 0 :
                        	mdTypeNone.setChecked(true);
							break;

                        case 1:
                        	mdTypeRegular.setChecked(true);
                            ll_motionSensitivity.setVisibility(View.VISIBLE);
                            break;

                        case 2:
                        	mdTypeSleepAnalytics.setChecked(true);
                            ll_motionSensitivity.setVisibility(View.GONE);
                            getBTARemainingTimeIfAvailable();
                            break;

                        case 3:
                        	mdTypeExpression.setChecked(true);
                            ll_motionSensitivity.setVisibility(View.GONE);
                            break;

                        default:
                        	mdTypeNone.setChecked(true);
                            break;
                    }

                }
                else if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72))
				{
					babyMdTypesLayout.setVisibility(View.VISIBLE);

					int sourceIndex =  CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_SOURCE);

					switch(sourceIndex)
					{
						case PublicDefine.MOTION_DETECTION_SOURCE:
							mdTypeRegular.setChecked(true);
							break;

						case PublicDefine.PIR_MOTION_DETECTION_SOURCE:
							mdTypeSleepAnalytics.setChecked(true);
							break;

						default:
							// default, we should display motion detection as source
							mdTypeRegular.setChecked(true);
							break;
					}

				}
                else
				{
                    babyMdTypesLayout.setVisibility(View.GONE);
                }
			}

			if (orbitCurrentMotionValue == 0) {
				motionSentivity1.setChecked(true);
			} else if (orbitCurrentMotionValue == 30) {
				motionSentivity2.setChecked(true);
			} else if (orbitCurrentMotionValue == 60) {
				motionSentivity3.setChecked(true);
			} else if (orbitCurrentMotionValue == 60) {
				motionSentivity4.setChecked(true);
			} else if (orbitCurrentMotionValue == 100) {
				motionSentivity5.setChecked(true);
			}


			if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS)) {
				motionSwitch.setChecked(CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS));
				if (motionSwitch.isChecked()) {

					if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SENSITIVITY)) {
						showMotionSensitivityValue(CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SENSITIVITY));
					} else {
						showMotionSensitivityValue(3); //MS Default value
					}

					if (!mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
							&& CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING)) {

						if (CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING)) {
							int storageMode = CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE);
							if (storageMode == 0) {
                                mvrCould.setChecked(true);
                                setRecordingPlanVisible(false);
                            }
							else {
								mvrSdCard.setChecked(true);
                                setRecordingPlanVisible(true);
								if (CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_DELETE_LAST_TEN)) {
									mRemoveSdcardClip.setChecked(true);
								} else if (CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_SWITCH_CLOUD)) {
									mSwitchCloud.setChecked(true);
								}
							}
						} else {
							mvrOff.setChecked(true);
                            setRecordingPlanVisible(false);
						}
					}
					handleMotionDetectionSwitch(true, false);
				}else{
                    handleMotionDetectionSwitch(false, false);
                }
			}

			if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_STATUS)) {
				soundSwitch.setChecked(CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_STATUS));
				if (soundSwitch.isChecked() && (mDevice.getProfile().doesHaveMicrophone())) {
					if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_SENSITIVITY)) {
						showSoundSensitivityValue(CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_SENSITIVITY));
					} else {
						setSoundThreshold(70); //VS Default value
					}
					handleSoundDetectionSwitch(true, false);
				}
			}

			if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_STATUS)) {
				if (CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_STATUS) &&
						CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_STATUS)) {

					tempSwitch.setChecked(CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_STATUS));
					if (tempSwitch.isChecked() && (mDevice.getProfile().doesHaveTemperature()) ) {
						if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_VALUE)) {
                      /*  tempSeekbar.setMinValue((float)CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE));
                        tempSeekbar.setMaxValue((float)CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_VALUE));
                */
							int minTempValue = CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE);
							int maxTempValue = CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_VALUE);
							if(minTempValue != -1 && maxTempValue != -1) {
								temperature.intValue = minTempValue;
								temperature.secondaryIntValue = maxTempValue;
								tempSeekbar.setSelectedMinValue(CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE));
								tempSeekbar.setSelectedMaxValue(CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_VALUE));
							}else{
								tempSeekbar.setSelectedMinValue(14);
								tempSeekbar.setSelectedMaxValue(32);
								temperature.intValue = 14;
								temperature.secondaryIntValue = 32;
								CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE, 14);
								CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE, 32);

							}
						} else {
                       /* tempSeekbar.setMinValue((float)14);
                        tempSeekbar.setMaxValue((float)32);*/
							tempSeekbar.setSelectedMinValue(14);
							tempSeekbar.setSelectedMaxValue(32);
							temperature.intValue = 14;
							temperature.secondaryIntValue = 32;
							CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE, 14);
							CommonUtil.setSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE, 32);


						}
						handleTemperatureDetectionSwitch(true, false);
					}
				}
			}

			if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LENS_CORRECTION))
			{
				lensCorrectionSwitch.setChecked(CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LENS_CORRECTION));
			}




			motionSentivity1.setOnClickListener(this);
			motionSentivity2.setOnClickListener(this);
			motionSentivity3.setOnClickListener(this);
			motionSentivity4.setOnClickListener(this);
			motionSentivity5.setOnClickListener(this);

			soundSensitivity1.setOnClickListener(this);
			soundSensitivity2.setOnClickListener(this);
			soundSensitivity3.setOnClickListener(this);
			soundSensitivity4.setOnClickListener(this);
			soundSensitivity5.setOnClickListener(this);

			mvrCould.setOnClickListener(this);
			mvrSdCard.setOnClickListener(this);
			mvrOff.setOnClickListener(this);

			mRemoveSdcardClip.setOnClickListener(this);
			mSwitchCloud.setOnClickListener(this);

			int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);

			if (savedTempUnit != PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
				mfahrenheit.setChecked(true);
			} else {
				mCentigrade.setChecked(true);
			}

	/*	if(sharedPreferences.contains(mDevice.getProfile().getName()+"notification") ) {
			if (sharedPreferences.getBoolean(mDevice.getProfile().getName() + "notification", false)) {
				motionSwitch.setChecked(true);
				ll_motionSensitivity.setVisibility(View.VISIBLE);
				soundSwitch.setChecked(true);
				ll_soundSensitivity.setVisibility(View.VISIBLE);
				tempSwitch.setChecked(true);
				ll_temparatureDetection.setVisibility(View.VISIBLE);

			} else {
				motionSwitch.setChecked(false);
				soundSwitch.setChecked(false);
				tempSwitch.setChecked(false);

			}
		}else{
			motionSwitch.setChecked(true);
			ll_motionSensitivity.setVisibility(View.VISIBLE);
			soundSwitch.setChecked(true);
			ll_soundSensitivity.setVisibility(View.VISIBLE);
			tempSwitch.setChecked(true);
			ll_temparatureDetection.setVisibility(View.VISIBLE);

		}
      */
// ARUNA
	/*	motionDetection = new ListChild(getSafeString(R.string.motion_detection), "", true);

		if (mDevice.getProfile().doesHaveMicrophone()) {
		//	ll_soundSensitivity.setVisibility(View.VISIBLE);
			soundDetection = new ListChild(getSafeString(R.string.sound_detection), "", true);
			volume = new ListChild(getSafeString(R.string.volume), "", true);
		}else{
			//ll_soundSensitivity.setVisibility(View.GONE);
			soundDetection = null;
		}


		if (mDevice.getProfile().doesHaveTemperature()) {
		//	ll_temparatureDetection.setVisibility(View.VISIBLE);
			temperature = new ListChild(getSafeString(R.string.temperature), "", true);
		} else {
			//ll_temparatureDetection.setVisibility(View.GONE);
			temperature = null;
		}*/
			closeCamaraSettings = (ImageView) findViewById(R.id.closeCameraSettings);
			closeCamaraSettings.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//	displayProgressDialog();
				/*GetNotificationStatusTask getNotificationStatus = new GetNotificationStatusTask(mDevice, CameraSettingsActivity.this, mDeviceHandler);
				getNotificationStatus.execute();*/
					settings();
				}
			});

			motionSwitch.setOnCheckedChangeListener(this);

			soundSwitch.setOnCheckedChangeListener(this);

			tempSwitch.setOnCheckedChangeListener(this);

			lensCorrectionSwitch.setOnCheckedChangeListener(this);

       final Button save = (Button) findViewById(R.id.tempupdate) ;
			//boolean isTouchByUser = tempSeekbar.isInTouchMode();

// Get noticed while dragging
			tempSeekbar.setNotifyWhileDragging(true);

			tempSeekbar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
				@Override
				public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, final Integer minValue, final Integer maxValue) {

					if (tempSeekbar.isInTouchMode()) {
						save.setVisibility(View.VISIBLE);
						scrollView.post(new Runnable() {
							@Override
							public void run() {

								scrollView.scrollTo(0, scrollView.getBottom());

							}
						});

						if (minValue > 18  ) {
							bar.setEnabled(false);

                               tempSeekbar.setSelectedMinValue(18);


							AlertDialog.Builder alert = new AlertDialog.Builder(CameraSettingsActivity.this);
							alert.setNegativeButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									tempSeekbar.setEnabled(true);
								}
							});
							alert.setCancelable(false);
							alert.setMessage("The minimum temperature should be in the range of 10 to 18 degree Celsius").show();

						}

                        if ( maxValue < 26 ) {
                            bar.setEnabled(false);

                                tempSeekbar.setSelectedMaxValue(26);

                            AlertDialog.Builder alert = new AlertDialog.Builder(CameraSettingsActivity.this);
                            alert.setNegativeButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    tempSeekbar.setEnabled(true);
                                }
                            });
                            alert.setCancelable(false);
                            alert.setMessage("The maximum temperature should be in the range of 26 to 33 degree Celsius").show();

                        }



                    }
				}
			});


        save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				save.setVisibility(View.GONE);
				temperature.booleanValue = tempSwitch.isChecked();
				temperature.secondaryBooleanValue = tempSwitch.isChecked();

				temperature.setOldCopy();
				int lowThreshold =  tempSeekbar.getSelectedMinValue().intValue();
				int highThreshold = tempSeekbar.getSelectedMaxValue().intValue();
				int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
			/*	if (savedTempUnit != PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
					lowThreshold = CommonUtil.convertFtoC(lowThreshold);
					highThreshold = CommonUtil.convertFtoC(highThreshold);

				}*/

				temperature.intValue = lowThreshold;
				temperature.secondaryIntValue = highThreshold;
				setupTemperatureValueField(temperature);
				setLowTemperatureThreshold(lowThreshold);
				setHighTemperatureThreshold(highThreshold);
				//listAdapter.notifyDataSetChanged();

			}
		});
			// set listener
		/*tempSeekbar.setOnRangeSeekbarChangeListener(new CrystalRangeSeekbar.OnRangeSeekbarChangeListener() {
			@Override
			public void valueChanged(Number minValue, Number maxValue) {
		temperature.booleanValue = tempSwitch.isChecked();
		temperature.secondaryBooleanValue = tempSwitch.isChecked();
				temperature.setOldCopy();
				int lowThreshold =  minValue.intValue();
				int highThreshold = (int) maxValue.intValue();
				int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
				if (savedTempUnit != PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
					lowThreshold = CommonUtil.convertCtoF((float) minValue.floatValue());
					highThreshold = CommonUtil.convertCtoF((float) maxValue.floatValue());
				}
				temperature.booleanValue = tempSwitch.isChecked();
				temperature.secondaryBooleanValue = tempSwitch.isChecked();
				temperature.intValue = lowThreshold;
				temperature.secondaryIntValue = highThreshold;
				setupTemperatureValueField(temperature);
				setLowTemperatureThreshold(lowThreshold);
				setHighTemperatureThreshold(highThreshold);
			//	setTemperatureDetectionIfAvailable(tempSwitch.isChecked(), tempSwitch.isChecked(), lowThreshold, highThreshold);
				//listAdapter.notifyDataSetChanged();

			}
		});*/


			// set final value listener
	/*	tempSeekbar.setOnRangeSeekbarFinalValueListener(new CrystalRangeSeekbar.OnRangeSeekbarFinalValueListener() {
			@Override
			public void finalValue(final Number minValue, final Number maxValue) {
				Log.d("CRS=>", String.valueOf(minValue) + " : " + String.valueOf(maxValue));
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						temperature.booleanValue = tempSwitch.isChecked();
						temperature.secondaryBooleanValue = tempSwitch.isChecked();

						temperature.setOldCopy();
						int lowThreshold =  minValue.intValue();
						int highThreshold = maxValue.intValue();
						int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
						if (savedTempUnit != PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
							lowThreshold = CommonUtil.convertFtoC((int) minValue.intValue());
							highThreshold = CommonUtil.convertFtoC((int) maxValue.intValue());

						}

						temperature.intValue = lowThreshold;
						temperature.secondaryIntValue = highThreshold;
						setupTemperatureValueField(temperature);
						setLowTemperatureThreshold(20);
						setHighTemperatureThreshold(30);
						//listAdapter.notifyDataSetChanged();

					}
				}, 500);
			}
		});*/
        if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)){
            soundLayout.setVisibility(View.GONE);
	        temperatureLayout.setVisibility(View.GONE);
        }else {
            if(!mDevice.getProfile().doesHaveMicrophone()){
                soundLayout.setVisibility(View.GONE);
            }

            if(!mDevice.getProfile().doesHaveTemperature())
                temperatureLayout.setVisibility(View.GONE);
        }

		}

		private void generalSettings() {
			AnalyticsInterface.getInstance().trackEvent(AppEvents.GENERAL_SETTINGS, AppEvents.GENERAL_SETTINGS_CLICKED, eventData);
			generalSettingsVisible = true;

			setContentView(R.layout.activity_general_settings);
			isSettings = false;
            String cameraName = mDevice.getProfile().getName();
            if(cameraName.length() > 10){
                cameraName = cameraName.substring(0,10) + "...";
            }
            settingsHeaderCamera = (TextView) findViewById(R.id.genarlcameraname);
            settingsHeaderCamera.setText("(" + cameraName + ")");

			ceilingIdTV = (TextView) findViewById(R.id.ceiling_id_tv);
			ceilingLayout = (LinearLayout) findViewById(R.id.ceiling);
			if((mDevice.getProfile().getModelId().compareTo(MODEL_ID_FOCUS72)==0) && ceilingIdTV != null)
			{
				ceilingIdTV.setText(getResources().getString(R.string.invert_image));
			}

			ceilingSwitch = (SwitchCompat) findViewById(R.id.ceilingmount_switch);
			nightVisionLayout = (LinearLayout) findViewById(R.id.nightvision);
			nightVisionAuto = (RadioButton) findViewById(R.id.rb_nv_auto);
			nightVisionON = (RadioButton) findViewById(R.id.rb_nv_on);
			nightVisionOFF = (RadioButton) findViewById(R.id.rb_nv_off);
			brightnessLight = (LinearLayout) findViewById(R.id.brightness);
			brightnessSeekBar = (SeekBar) findViewById(R.id.brightnessSeekbar);
			volumeLayout = (LinearLayout) findViewById(R.id.volume);
			volumeSeekbar = (SeekBar) findViewById(R.id.volume_seekbar);
            overlayLayout = (LinearLayout) findViewById(R.id.overlay);
            overlayDateSwitch = (SwitchCompat) findViewById(R.id.overlaydate_switch);
            videoQualityLayout = (LinearLayout) findViewById(R.id.video_quality_layout);
            viewModeLayout = (LinearLayout) findViewById(R.id.view_mode_layout);
            narrow = (RadioButton) findViewById(R.id.narrow);
            wide = (RadioButton) findViewById(R.id.wide);



			closeGeneralSettings = (ImageView) findViewById(R.id.closeGeneralSettings);
			closeGeneralSettings.setOnClickListener(this);
//ARUNA
		/*if(mDevice.getProfile().doesHaveCeilingMount()){
			ceilingMount = new ListChild(getSafeString(R.string.ceiling_mount), "", true);
			ceilingLayout.setVisibility(View.VISIBLE);

		}else{
			ceilingLayout.setVisibility(View.GONE);
			ceilingMount = null;
		}

		brightness = new ListChild(getSafeString(R.string.brightness), "", true);*/

			if (isHubbleIR()) {
				nightVision = new ListChild(getSafeString(R.string.night_vision), "", true);
				nightVisionLayout.setVisibility(View.VISIBLE);
				nightVisionON.setOnClickListener(this);
				nightVisionAuto.setOnClickListener(this);
				nightVisionOFF.setOnClickListener(this);

				if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_VISION)) {
					int nightVisionValue = CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_VISION);
					switch (nightVisionValue) {
						case NIGHTVISION_AUTO:
							nightVisionAuto.setChecked(true);
							nightVisionON.setChecked(false);
							nightVisionOFF.setChecked(false);

							break;
						case NIGHTVISION_ON:
							nightVisionAuto.setChecked(false);
							nightVisionON.setChecked(true);
							nightVisionOFF.setChecked(false);
							break;
						case NIGHTVISION_OFF:
							nightVisionAuto.setChecked(false);
							nightVisionON.setChecked(false);
							nightVisionOFF.setChecked(true);
							break;
						default:
							nightVisionAuto.setChecked(true);
							nightVisionON.setChecked(false);
							nightVisionOFF.setChecked(false);
					}
				} else {
			/* By default Night vision should be AUTO  */
					nightVisionAuto.setChecked(true);
					nightVisionON.setChecked(false);
					nightVisionOFF.setChecked(false);
				}

			} else {
				nightVisionLayout.setVisibility(View.GONE);
			}


			if (!mDevice.getProfile().hasNoSpeaker()) {
                volume = new ListChild(getSafeString(R.string.volume), "", true);
                volumeLayout.setVisibility(View.VISIBLE);
                volumeSeekbar.setMax(volumeSeekBarMaxValue);

                if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VOLUME))
                {
                    int volumeProgressValue = CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VOLUME);
                    volumeSeekbar.setProgress(volumeProgressValue);//CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VOLUME));

                } else {
			       /* By default brightness value is 7  */
                    volumeSeekbar.setProgress(volumeSeekBarMaxValue);
                }

                volumeSeekbar.setOnSeekBarChangeListener(this);

            }else {
				volume = null;
				volumeLayout.setVisibility(View.GONE);
			}

			if ("0086".equals(mDevice.getProfile().getModelId())) {
				overlayDate = new ListChild(getSafeString(R.string.overlay_date), "", true);
                overlayLayout.setVisibility(View.VISIBLE);

                if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.OVERLAY_DATE))
                {
                    boolean overlayDateStatus = CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.OVERLAY_DATE);
                    overlayDateSwitch.setChecked(overlayDateStatus);//CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VOLUME));

                } else {
			       /* By default overlay date is false */
                    overlayDateSwitch.setChecked(false);
                }
                overlayDateSwitch.setOnCheckedChangeListener(this);

            }else{
                overlayDate = null;
                overlayLayout.setVisibility(View.GONE);
            }


			if (mDevice.getProfile().isVTechCamera()) {
				contrast = new ListChild(getSafeString(R.string.contrast), "", true);
				videoQuality = new ListChild(getSafeString(R.string.video_quality), "", true);
				statusLed = new ListChild(getSafeString(R.string.status_led), "", true);
				mLEDFlicker = new ListChild(getSafeString(R.string.led_flicker), "", true);
			} else {
				contrast = null;
				videoQuality = null;
				statusLed = null;
			}
			nightVision = new ListChild(getSafeString(R.string.night_vision), "", true);

			if (mDevice.getProfile().canPark()) {
				park = new ListChild(getSafeString(R.string.park), "", true);
			} else {
				park = null;
			}

			if (mDevice.getProfile().isSupportViewMode()) {
				viewMode = new ListChild(getSafeString(R.string.view_mode), "", true);
                viewModeLayout.setVisibility(View.VISIBLE);
			} else {
				viewMode = null;
                viewModeLayout.setVisibility(View.GONE);
			}


            if (mDevice.getProfile().modelId.equals("0877") && checkVersionSupportIR(mDevice.getProfile().firmwareVersion, "02.00.26")) {
                qualityOfService = new ListChild(getSafeString(R.string.video_quality), "", true);
                videoQualityLayout.setVisibility(View.GONE);
            }

			if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.CEILING_MOUNT)) {
				ceilingSwitch.setChecked(CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.CEILING_MOUNT));

			} else {
			/* By default ceiling mount should be OFF  */
				ceilingSwitch.setChecked(false);
			}
			llNightLigth = (LinearLayout) findViewById(R.id.ll_night_light);
			nightLightAuto = (RadioButton) findViewById(R.id.rb_nl_auto);
			nightLightOff= (RadioButton) findViewById(R.id.rb_nl_off);
			nightLightOn = (RadioButton) findViewById(R.id.rb_nl_on);
			nightLightOn.setOnClickListener(this);
			nightLightOff.setOnClickListener(this);
			nightLightAuto.setOnClickListener(this);

            switch(nightLightMode) {
                case 0:
                    nightLightAuto.setChecked(true);
                    nightLightOff.setChecked(false);
                    nightLightOn.setChecked(false);
                    break;
                case 1:
                    nightLightAuto.setChecked(false);
                    nightLightOff.setChecked(false);
                    nightLightOn.setChecked(true);
                    break;
                case 2:
                    nightLightAuto.setChecked(false);
                    nightLightOff.setChecked(true);
                    nightLightOn.setChecked(false);
                    break;
            }

			if(nightLightHelper.isSupportNightLight(mDevice.getProfile().getRegistrationId())) {
				nightligthListChild = new ListChild(getSafeString(R.string.night_light), "", true);
				llNightLigth.setVisibility(View.VISIBLE);
			} else {
				nightligthListChild = null;
				llNightLigth.setVisibility(View.GONE);
			}

			ceilingSwitch.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (ceilingSwitch.isChecked()) {
						AnalyticsInterface.getInstance().trackEvent(AppEvents.CEILING_MOUNT, AppEvents.CEILING_MOUNT_ON, eventData);
						GeAnalyticsInterface.getInstance().trackEvent(AppEvents.GENERAL_SETTING,AppEvents.CEILINGMOUNT_ENABLED,AppEvents.CEILING_ENABLED);
						ZaiusEvent ceilingMountOnEvt = new ZaiusEvent(AppEvents.GENERAL_SETTING);
						ceilingMountOnEvt.action(AppEvents.CEILINGMOUNT_ENABLED);
						try {
							ZaiusEventManager.getInstance().trackCustomEvent(ceilingMountOnEvt);
						} catch (ZaiusException e) {
							e.printStackTrace();
						}
						ceilingSwitch.setChecked(true);
						setCeilingMountIfAvailable(true);
					} else {
						AnalyticsInterface.getInstance().trackEvent(AppEvents.CEILING_MOUNT, AppEvents.CEILING_MOUNT_OFF, eventData);
						GeAnalyticsInterface.getInstance().trackEvent(AppEvents.GENERAL_SETTING,AppEvents.CEILINGMOUNT_DISABLED,AppEvents.CEILING_DISABLED);
						ZaiusEvent ceilingMountOffEvt = new ZaiusEvent(AppEvents.GENERAL_SETTING);
						ceilingMountOffEvt.action(AppEvents.CEILINGMOUNT_DISABLED);
						try {
							ZaiusEventManager.getInstance().trackCustomEvent(ceilingMountOffEvt);
						} catch (ZaiusException e) {
							e.printStackTrace();
						}
						ceilingSwitch.setChecked(false);
						setCeilingMountIfAvailable(false);
					}
				}
			});


            brightnessSeekBar.setMax(seekBarMaxValue - 1);
			if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.BRIGHTNESS)) {
				brightnessSeekBar.setProgress(CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.BRIGHTNESS) - 1);

			} else {
			/* By default brightness value is 9  */
				brightnessSeekBar.setProgress(seekBarMaxValue - 1);
			}


			brightnessSeekBar.setOnSeekBarChangeListener(this);

	/*	brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser) {
					brightness.setOldCopy();
					int brightnessValue = 0;
					try {
						brightnessValue = progress + 1;
					} catch (NumberFormatException ignored) {
						brightnessValue = 0;
					}
					brightness.value = String.valueOf(brightnessValue);
					brightness.intValue = brightnessValue;

					setBrightnessIfAvailable(brightnessValue);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});*/


            if(mDevice.getProfile().getModelId().equalsIgnoreCase("0877")){
                getViewModeIfAvailable(true);
                getQualityOfServiceIfAvailable(true);

                int viewModeInt = CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VIEW_MODE);
                if(viewModeInt == 0) {
                    wide.setChecked(true);
                    viewMode.intValue = 0;
                    narrow.setChecked(false);
                }
                else {
                    narrow.setChecked(true);
                    viewMode.intValue = 1;
                    wide.setChecked(false);
                }

                narrow.setOnClickListener(this);
                wide.setOnClickListener(this);
            }

		}


		@Override
		public void onBackPressed() {
			if (isSettings) {
				super.onBackPressed();
			} else {
				/*displayProgressDialog();
				GetNotificationStatusTask getNotificationStatus = new GetNotificationStatusTask(mDevice, this, mDeviceHandler);
				getNotificationStatus.execute();*/
				settings();
			}
		}

		private void showChangeCameraNameDialog(boolean isSensor) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog_edittext, (ViewGroup) findViewById(R.id.dialog_edittext_root));

			final EditText editText = (EditText) layout.findViewById(R.id.dialog_edittext_edittext);
			if (mDevice.getProfile().getName() != null && mDevice.getProfile().getName().length() > 0) {
				editText.setText(mDevice.getProfile().getName());
			} else {
				editText.setHint(getSafeString(R.string.hint_for_camera_name));
				editText.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
			}
			editText.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
					if (editText.getText().toString().length() >= 25)
						Toast.makeText(CameraSettingsActivity.this, R.string.camera_name_max_characters, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void afterTextChanged(Editable editable) {

				}
			});
			String title = isSensor ? getSafeString(R.string.change_sensor_name) : getSafeString(R.string.enter_the_new_name_of_this_camera);
			final String prgString = isSensor ? getSafeString(R.string.changing_sensor_name) : getSafeString(R.string.changing_camera_name);
			//String message = isSensor ? getSafeString(R.string.enter_the_new_name_of_this_sensor) : getSafeString(R.string.enter_the_new_name_of_this_camera);

			mAlertDialog = new AlertDialog.Builder(this).setTitle(title)
					.setView(layout)
					.setPositiveButton(getSafeString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String newCameraName = editText.getText().toString().trim();
							if (checkNameValid(newCameraName) ) {
								if (newCameraName != null && !newCameraName.isEmpty() && !mDevice.getProfile().getName().equals(newCameraName)) {
										latestCameraName = newCameraName;
										//dialog.dismiss();
                                        if(mIsActivityRunning)
										   changeNameDialog = ProgressDialog.show(CameraSettingsActivity.this, null, prgString, true, true);

										String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
										ChangeNameTask rename = new ChangeNameTask(getApplicationContext()
												, CameraSettingsActivity.this);
										rename.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, saved_token, newCameraName, mDevice.getProfile().getRegistrationId());
								}
							} else {
								try {
									if (dialog != null)
										dialog.dismiss();
									showDialogValidName();
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						}
					})
					.setNegativeButton(getSafeString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
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

		@Override
		public void update_cam_success() {
			if (changeNameDialog != null) {
				try {
					changeNameDialog.dismiss();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (!TextUtils.isEmpty(latestCameraName)) {
				mDevice.getProfile().setName(latestCameraName);
				cameraName.setText(latestCameraName);
				String toast = "";
				if (mDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
					toast = getSafeString(R.string.sensor_name_changed);
				} else {
					toast = getSafeString(R.string.changed_camera_name);
					AnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_NAME_CHANGE_SUCCESS, AppEvents.CAMERA_NAME_CHANGE_SUCCESS, eventData);
				    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_NAME_CHANGE_SUCCESS,AppEvents.CAMERANAME_CHANGE_SUCCESS);
					ZaiusEvent cameraNameEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
					cameraNameEvt.action(AppEvents.CAMERA_NAME_CHANGE_SUCCESS);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(cameraNameEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}

				}
				Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
				latestCameraName = null;
			}
		}

		@Override
		public void update_cam_failed() {
			if (changeNameDialog != null) {
				try {
					changeNameDialog.dismiss();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			cameraName.setText(mDevice.getProfile().getName());

			String toast = "";
			if (mDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
				toast = getSafeString(R.string.failed_to_change_sensor_name);
			} else {
				toast = getSafeString(R.string.failed_to_change_camera_name);
				AnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_NAME_CHANGE_FAILURE,AppEvents.CAMERA_NAME_CHANGE_FAILURE, eventData);
				GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_NAME_CHANGE_FAILURE,AppEvents.CAMERANAME_CHANGE_FAILURE);

				ZaiusEvent cameraNameFailureEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
				cameraNameFailureEvt.action(AppEvents.CAMERA_NAME_CHANGE_FAILURE);
				try {
					ZaiusEventManager.getInstance().trackCustomEvent(cameraNameFailureEvt);
				} catch (ZaiusException e) {
					e.printStackTrace();
				}
			}

			Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
		}

		private String getSafeString(int stringResourceId) {
			if (this != null) {
				return this.getString(stringResourceId);
			} else {
				return "";
			}
		}

        private String[] getSafeStringArray(int stringArrayResourceId) {
            if (this != null) {
                return this.getResources().getStringArray(stringArrayResourceId);
            } else {
                return new String[]{""};
            }
        }

		private boolean checkNameValid(String name) {
			boolean valid = false;
			if(name==null)
				return false;
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

		private String formatMacAddress(String macString) {
			Pattern regex = Pattern.compile("(..)(..)(..)(..)(..)(..)");
			final Matcher matcher = regex.matcher(macString);
			if (matcher.matches()) {
				return String.format("%s:%s:%s:%s:%s:%s",
						matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5), matcher.group(6));
			} else {
				// MAC not matches
				Log.d("mbp", "what returned: " + macString);
				return getSafeString(R.string.failed_to_retrieve_camera_data);
			}
		}

		private void getMCUDetails()
		{
			/* Get MCU Version details */
			if(mMCUVersion == null)
			{
				mMCUVersion = CommonUtil.getStringValue(mContext,mDevice.getProfile().getRegistrationId()+"-"+mDevice.getProfile().getFirmwareVersion()+"-"+SettingsPrefUtils.MCU_VERSION);
				mMCUVersionTv.setText(mMCUVersion);

				if(mMCUVersion == null)
				{
					if (mcuVersionListChild != null)
					{
						mcuVersionListChild.value = getSafeString(R.string.loading);
						mMCUVersionTv.setText(mcuVersionListChild.value);

						actor.send(new ActorMessage.GetMCUVersion(mcuVersionListChild));
					}
				}
			}
			else
			{
				mMCUVersionTv.setText(mMCUVersion);
			}


		}

		private void getWifiStrengthIfAvailable() {
			if (cameraWifiStrength != null) {
				cameraWifiStrength = getSafeString(R.string.loading);
				wifiStrength.setText(cameraWifiStrength);
				Runnable runn = new Runnable() {
					@Override
					public void run() {
						boolean isInLocal = false;
//          if (getActivity() != null) {
//            isInLocal = ((MainActivity) getActivity()).isStreamingViaLocal();
//          }
						isInLocal = mDevice.isAvailableLocally();
						// get wifi strength
						//final String wifiStrengthRes = CommandUtils.sendCommand(mDevice, "get_wifi_strength", isInLocal);
						final String wifiStrengthRes= CameraCommandUtils.sendCommandGetStringValue(mDevice,"get_wifi_strength",null,null);
						/*final String wifiStrengthValue;
						if (wifiStrengthRes != null && wifiStrengthRes.startsWith("get_wifi_strength")) {
							wifiStrengthValue = wifiStrengthRes.substring("get_wifi_strength".length() + 2);
						} else {
							wifiStrengthValue = null;
						}*/

						if (getApplicationContext() != null) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {

									if (wifiStrengthRes != null) {
										int wifiStrengthLevel = -1;
										try {
											wifiStrengthLevel = Integer.parseInt(wifiStrengthRes);
										} catch (NumberFormatException e) {
											e.printStackTrace();
										}

										if (wifiStrengthLevel == 0 || wifiStrengthLevel == 101) {
											cameraWifiStrength = getSafeString(R.string.lan_connection);
										} else if (wifiStrengthLevel > 0) {
											cameraWifiStrength = wifiStrengthRes + "%";
										} else {
											cameraWifiStrength = getSafeString(R.string.failed_to_retrieve_camera_data);
										}
									} else {
										cameraWifiStrength = getSafeString(R.string.failed_to_retrieve_camera_data);
									}
									wifiStrength.setText(cameraWifiStrength);
								}
							});
						}
					}
				};
				Thread worker = new Thread(runn);
				worker.start();
			}
		}

        private void getSOCVersionIfAvailable() {
            if (socVersion != null && shouldRefreshListChildValues(socVersion)) {
                if(CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOC_SLAVE_FW)){
                    socVersion.value = CommonUtil.getStringValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOC_SLAVE_FW);
                    slaveFirmware.setText(socVersion.value);
                }else {
                    socVersion.value = getSafeString(R.string.loading);
                    slaveFirmware.setText(getSafeString(R.string.loading));
                }
                actor.send(new ActorMessage.GetSOCVersion(socVersion));
            }
        }

	/*	private void setUpTempFormat() {
			int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
			if (savedTempUnit == PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
				celsius.setChecked(true);
			} else {
				fahrenheit.setChecked(true);
				celsius.setChecked(false);
			}

		}

		private void setUpTimeFormat() {


			final int tempPreference = settings.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0);

			if (tempPreference == 1) {
				hourFormat24.setChecked(true);
				hourFormat12.setChecked(false);

			} else {
				hourFormat12.setChecked(true);
				hourFormat24.setChecked(false);
			}

		}
*/
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch (buttonView.getId()) {
				case switch_motionDetection:
					handleMotionDetectionSwitch(isChecked, true);
					break;
				case R.id.switch_soundDetection:
					handleSoundDetectionSwitch(isChecked, true);
					break;
				case R.id.switch_tempDetection:
					handleTemperatureDetectionSwitch(isChecked, true);
					break;
				case R.id.lens_correction_switch:
					handleLensCorrectionSwitch(isChecked,true);
					break;
                case R.id.overlaydate_switch:
                    hanldeOverlayDateSwitch(isChecked, true );
                    break;
				case R.id.scheduler_switch:
					handleScheduleSwitch(isChecked);
					break;

			}//Switch end
		}

        private void hanldeOverlayDateSwitch(boolean isChecked, boolean isUser ){
            if(isChecked) {
                AnalyticsInterface.getInstance().trackEvent(AppEvents.OVERLAY_DATE, AppEvents.OVERLAY_DATE_ON, eventData);
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.GENERAL_SETTING, AppEvents.OVERLAY_DATE_ENABLED, AppEvents.OVERLAY_ENABLED);
                ZaiusEvent overlayDateEvt = new ZaiusEvent(AppEvents.GENERAL_SETTING);
                overlayDateEvt.action(AppEvents.OVERLAY_DATE_ENABLED);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(overlayDateEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
            }else{
                AnalyticsInterface.getInstance().trackEvent(AppEvents.OVERLAY_DATE, AppEvents.OVERLAY_DATE_OFF, eventData);
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.GENERAL_SETTING, AppEvents.OVERLAY_DATE_DISABLED, AppEvents.OVERLAY_DISABLED);
                ZaiusEvent overlayDateEvt = new ZaiusEvent(AppEvents.GENERAL_SETTING);
                overlayDateEvt.action(AppEvents.OVERLAY_DATE_DISABLED);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(overlayDateEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
            }
            if (isUser)
              setOverlayDate(isChecked);
        }


		private void handleTemperatureDetectionSwitch(boolean isChecked, boolean isUser) {
			if (isChecked) {
				ll_temparatureDetection.setVisibility(View.VISIBLE);
				int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
				if (savedTempUnit != PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
					mfahrenheit.setChecked(true);
				} else {
					mCentigrade.setChecked(true);
				}
				if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_VALUE)) {
				tempSeekbar.setSelectedMinValue(CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE));
				tempSeekbar.setSelectedMaxValue(CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_VALUE));
					temperature.intValue = CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE);
					temperature.secondaryIntValue = CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_VALUE);
				} else {
			/*	tempSeekbar.setMinValue((float)14);
				tempSeekbar.setMaxValue((float)32);*/
					tempSeekbar.setSelectedMinValue(14);
					tempSeekbar.setSelectedMaxValue(32);
					temperature.intValue = 14;
					temperature.secondaryIntValue = 32;

					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.MIN_TEMP+" : "+String.valueOf(temperature.intValue) ,AppEvents.LOW_TEMPARATURE);
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.MAX_TEMP+" : "+String.valueOf(temperature.secondaryIntValue),AppEvents.HIGH_TEMPARATURE);

					ZaiusEvent tempLowEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
					tempLowEvt.action(AppEvents.MIN_TEMP+" : "+String.valueOf(temperature.intValue) );
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(tempLowEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}

					ZaiusEvent tempMaxEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
					tempMaxEvt.action(AppEvents.MAX_TEMP+" : "+String.valueOf(temperature.secondaryIntValue));
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(tempMaxEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
				}


			} else {
				ll_temparatureDetection.setVisibility(View.GONE);
			}
			if (isUser) {
                temperature.setOldCopy();
                temperature.booleanValue = tempSwitch.isChecked();
                temperature.secondaryBooleanValue = tempSwitch.isChecked();
                setupTemperatureValueField(temperature);
				setTemperatureDetectionIfAvailable(tempSwitch.isChecked());
			}
		}


		private void handleSoundDetectionSwitch(boolean isChecked, boolean isUser) {
			if (isChecked) {
				sharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", true).commit();
				ll_soundSensitivity.setVisibility(View.VISIBLE);
				if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_SENSITIVITY)) {
					showSoundSensitivityValue(CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_SENSITIVITY));
				} else {
					setMotionSenitivityValue(50); //VS Default value
				}
			} else {
				sharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", true).commit();
				ll_soundSensitivity.setVisibility(View.GONE);
			}
			if (isUser)
				setSoundDetectionIfAvailable(soundSwitch.isChecked());


		}

		private void handleLensCorrectionSwitch(boolean isChecked, boolean isUser)
		{
			if (isUser)
				setLensCorrection(isChecked);

		}

		private void setMotionSource(int source)
		{
			showApplyingDialog();
			motionDetection.setOldCopy();
			actor.send(new ActorMessage.SetMotionSource(motionDetection,source));
		}

		private void setLensCorrection(boolean isChecked)
		{
			showApplyingDialog();
			int lowThreshold = isChecked ? 1 : 0;
			actor.send(new ActorMessage.SetLensCorrection(lensCorrection, lowThreshold));
		}

		private void SetVideoRecordingDuration(int duration,int currentDuration)
		{
			// avoid setting wrong value if duration is not present in database
			if(currentDuration == -1)
			{
				currentDuration = PublicDefine.VIDEO_RECORDING_OFF_DURATION;
			}
			showApplyingDialog();

			actor.send(new ActorMessage.SetVideoRecordingDuration(videoRecording, duration,currentDuration));
		}

		private void handleMotionDetectionSwitch(boolean isChecked, boolean isUser) {
			boolean isWifiEnabledOrEnabling = ConnectToNetworkActivity.isWifiEnabledOrEnabling();
			boolean isMobileNetworkEnabled = ConnectToNetworkActivity.hasMobileNetwork(HubbleApplication.AppContext) &&
					ConnectToNetworkActivity.isMobileDataEnabled(HubbleApplication.AppContext);
			if (isUser && isWifiEnabledOrEnabling == false && isMobileNetworkEnabled == false) {
				Log.i(TAG, "No network enabled, notify user");
				setNoNetworkDialogVisible(true);
			} else {
				if (isChecked) {
					AnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_DETECTION_ON, eventData);
					if (isUser)
						setMotionNotificationIfAvailable(motionSwitch.isChecked());

					if(mDevice.getProfile().getModelId().equalsIgnoreCase("0877"))
					{
                        babyMdTypesLayout.setVisibility(View.VISIBLE);
                        int mdTypeIndex =  CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE);
                          if(mdTypeIndex > 1)
                              ll_motionSensitivity.setVisibility(View.GONE);

                    }
                    else if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72))
					{
						babyMdTypesLayout.setVisibility(View.VISIBLE);
						ll_motionSensitivity.setVisibility(View.VISIBLE);
					}
                    else {
						ll_motionSensitivity.setVisibility(View.VISIBLE);
					}

					if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT))
					{
						//mvrLayout.setVisibility(View.GONE);
						if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)){
							setUpOrbitPlanFirmwareSetting();
						}else if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION))
						{
							setUpOrbitVRDFirmwareSetting(CommonUtil.getVideoRecording(mContext,mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VIDEO_RECORDING));
						} else {
							mvrLayout.setVisibility(View.GONE);
							mVideoRecordingLayout.setVisibility(View.GONE);
						}
					}
					else
						mvrLayout.setVisibility(View.VISIBLE);



					//if(CommonUtil.checkSettings(getApplicationContext(),mDevice.getProfile().getRegistrationId()+ "-" +SettingsPrefUtils.MOTION_STATUS )) {
					//motionSwitch.setChecked(CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS));
					if (motionSwitch.isChecked()) {

						if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SENSITIVITY)) {
							showMotionSensitivityValue(CommonUtil.getSettingValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SENSITIVITY));
						} else {
							showMotionSensitivityValue(3); //MS Default value
						}
						if (!mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
								&& CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING)) {
							if (CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING)) {
								int storageMode = CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE);
								if (storageMode == 0) {
                                    mvrCould.setChecked(true);
                                    setRecordingPlanVisible(false);
                                }
								else {
									mvrSdCard.setChecked(true);
									setRecordingPlanVisible(true);
									if (CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_DELETE_LAST_TEN)) {
										mRemoveSdcardClip.setChecked(true);
									} else if (CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_SWITCH_CLOUD)) {
										mSwitchCloud.setChecked(true);
									}

								}
							} else {
								mvrOff.setChecked(true);
                                setRecordingPlanVisible(false);
							}
						}

						//}
					}

				} else {
					AnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_DETECTION_OFF, eventData);
					if (isUser) {

                        if (mDevice.getProfile().getModelId().equalsIgnoreCase("0877"))
                        {
                            babyMdTypesLayout.setVisibility(View.GONE);

                            int mdTypeIndex =  CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE);
                            if(mdTypeIndex == 2 && remainingBTATime > 0){
                                showWarningDialogAboutBTA(mdTypeIndex,mdTypeIndex,mDevice);
                            }else{
                                setMotionNotificationIfAvailable(motionSwitch.isChecked());
                            }


                        }
                        else if(mDevice.getProfile().getModelId().equalsIgnoreCase(MODEL_ID_FOCUS72)) {
							babyMdTypesLayout.setVisibility(View.GONE);
							setMotionNotificationIfAvailable(motionSwitch.isChecked());
						}
                        else{
                            setMotionNotificationIfAvailable(motionSwitch.isChecked());
                        }
                    }

                    if (mDevice.getProfile().getModelId().equalsIgnoreCase("0877") || mDevice.getProfile().getModelId().equalsIgnoreCase(MODEL_ID_FOCUS72)) {
						babyMdTypesLayout.setVisibility(View.GONE);
					}



					ll_motionSensitivity.setVisibility(View.GONE);
					mvrLayout.setVisibility(View.GONE);

					if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT))
					{
						//mMotionVideoRecodingLayout.setVisibility(View.GONE);
						mVideoRecordingLayout.setVisibility(View.GONE);
					}

				}
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (seekBar == volumeSeekbar) {
				if (fromUser) {
					volume.setOldCopy();
					int fieldValue = progress;

					long firmwareVersion = 0;
					try {
						firmwareVersion = Long.valueOf(mDevice.getProfile().getFirmwareVersion().replace(".", ""));
					} catch (Exception ignored) {
					}
					if (mDevice.getProfile().isVTechCamera()) {
						volume.intValue = fieldValue;
					} else {
						if (firmwareVersion >= 11900) {
							volume.intValue = fieldValue;
						} else {
							volume.intValue = fieldValue + 21;
						}
					}

					volume.value = String.valueOf(fieldValue);

					setVolumeIfAvailable(volume.intValue);
				}
			} else if (seekBar == brightnessSeekBar) {
				if (fromUser) {
					brightness.setOldCopy();
					int brightnessValue = 0;
					try {
						brightnessValue = progress + 1;
					} catch (NumberFormatException ignored) {
						brightnessValue = 0;
					}
					brightness.value = String.valueOf(brightnessValue);
					brightness.intValue = brightnessValue;

					setBrightnessIfAvailable(brightnessValue);
				}
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}


        private class DeleteAllEvents extends AsyncTask<Device, Void, Boolean> {
			Dialog dialog;

			public DeleteAllEvents(Dialog dialog) {
				this.dialog = dialog;
			}

			@Override
			protected void onPreExecute() {
                if(mIsActivityRunning) {
                    dialog = ProgressDialog.show(CameraSettingsActivity.this, null, getSafeString(R.string.deleting_events));
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(false);
                }
				super.onPreExecute();
			}

			@Override
			protected Boolean doInBackground(Device... params) {
				boolean returnValue;
				Device cam = params[0];
				Response response;
				try {

					response = Api.getInstance().getService().deleteTimelineEvents(cam.getProfile().getRegistrationId(), apiKey, null, true);
					returnValue = response.getStatus() == 200;
				} catch (Exception e) {
					return false;
				}
				return returnValue;
			}

			protected void onPostExecute(Boolean wasSuccessful) {
				if (dialog != null) {
					try {
						dialog.dismiss();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				if (getApplicationContext() != null) {
					if (wasSuccessful) {
						Toast.makeText(getApplicationContext(), getSafeString(R.string.events_deleted), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), getSafeString(R.string.failed_to_delete_events), Toast.LENGTH_SHORT).show();
					}
				}
			}
		}

		private void showDeleteAllEventsDialog() {
			mAlertDialog = new AlertDialog.Builder(this).setMessage(getSafeString(R.string.delete_all_event_confirm))
					.setTitle(getSafeString(R.string.confirm))
					.setPositiveButton(getSafeString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new DeleteAllEvents(new ProgressDialog(CameraSettingsActivity.this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mDevice);
						}
					})
					.setNegativeButton(getSafeString(R.string.no), new DialogInterface.OnClickListener() {
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

		private void showRemoveDialog() {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.remove_camera_confirm).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface mDialog, int which) {
					final ProgressDialog progressDialog = new ProgressDialog(CameraSettingsActivity.this);
					progressDialog.setIndeterminate(true);
					progressDialog.setCancelable(false);
					progressDialog.setMessage(getSafeString(R.string.removing_camera));
					progressDialog.show();

					final String muteKey = "mute" + mDevice.getProfile().getRegistrationId();

					RemoveDeviceTask deleteDeviceTask = new RemoveDeviceTask(mContext, new RemoveDeviceTask.onDeleteTaskCompleted() {
						@Override
						public void onDeleteTaskCompleted(int result) {
							Activity activity = CameraSettingsActivity.this;
							if (activity != null) {
								progressDialog.dismiss();
								if (mDialog != null ) {
									try {
										mDialog.dismiss();
										removeDialogShowing = false;
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}
								if (result == RemoveDeviceTask.REMOVE_CAM_SUCCESS) {
									AnalyticsInterface.getInstance().trackEvent(AppEvents.REMOVE_CAMERA, AppEvents.REMOVE_CAMERA_SUCCESS, eventData);
                                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SETTINGS,AppEvents.SETTINGS_CAMERA_REMOVE_SUCCESS,AppEvents.REMOVE_CAMERA_SUCCESS);

									ZaiusEvent removeCameraEvt = new ZaiusEvent(AppEvents.SETTINGS);
									removeCameraEvt.action(AppEvents.SETTINGS_CAMERA_REMOVE_SUCCESS);
									try {
										ZaiusEventManager.getInstance().trackCustomEvent(removeCameraEvt);
									} catch (ZaiusException e) {
										e.printStackTrace();
									}

									if (settings != null && settings.check(muteKey)) {
										settings.remove(muteKey);
									}
									//activity.onBackPressed();
									Intent entry = new Intent(activity, MainActivity.class);

									activity.startActivity(entry);
									activity.finish();

									AnalyticsController.getInstance().onCameraRemoved();
								} else {
									// // Log.e(TAG, "Remove camera failed.");
									GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SETTINGS,AppEvents.REMOVE_CAMERA_FAILURE+" :"+result,AppEvents.REMOVECAMERA_FAILURE);
									ZaiusEvent removeCameraFailureEvt = new ZaiusEvent(AppEvents.SETTINGS);
									removeCameraFailureEvt.action(AppEvents.REMOVE_CAMERA_FAILURE+" :"+result);
									try {
										ZaiusEventManager.getInstance().trackCustomEvent(removeCameraFailureEvt);
									} catch (ZaiusException e) {
										e.printStackTrace();
									}

									AlertDialog.Builder builder = new AlertDialog.Builder(activity);
									builder.setMessage(R.string.remove_camera_failed);
									builder.create().show();
								}
							}
						}
					});
					if (apiKey != null && mDevice != null && mDevice.getProfile() != null) {
						deleteDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mDevice.getProfile().getRegistrationId(), apiKey);
					}
				}
			}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						dialog.dismiss();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					removeDialogShowing = false;
				}
			});

			AlertDialog removeConfirmDialog = builder.create();
			removeConfirmDialog.setCancelable(true);
			removeConfirmDialog.setCanceledOnTouchOutside(false);
			removeConfirmDialog.setTitle(R.string.remove_camera);
            if(mIsActivityRunning)
			   removeConfirmDialog.show();
		}

	/* Code for general settings */

		private Map<String, String> modelSupportIR = ImmutableMap.<String, String>builder()
				.put("0073", "01.19.10")
				.put("0173", "01.19.32")
				.put("0066", "01.19.30")
				.put("0662", "01.19.32")
				.put("1662", "01.19.32")
				.put("0085", "01.19.32")
				.put("0854", "01.19.32")
				.put("1854", "01.19.32")
				.put("0086", "") // any versions
				.put("0877", "") // any versions
				.put("0855", "")
				.put("0072", "01.19.16")
				.put("0080", "")
				.put("0083", "01.19.30")
				.put("0836", "01.19.30")
				.put("0667", "") // any versions
				.put("1855", "")
				.put("2855", "")
				.put("0068", "")
				.build();


		private boolean isHubbleIR() {
			String modelId = mDevice.getProfile().getModelId();
			String fw = mDevice.getProfile().getFirmwareVersion();
			if (!modelSupportIR.containsKey(modelId)) {
				return false;
			}
			if (TextUtils.isEmpty(modelSupportIR.get(modelId))) { // any versions
				return true;
			}
			return checkVersionSupportIR(fw, modelSupportIR.get(modelId));
		}

		public static boolean checkVersionSupportIR(String version1, String version2) {
			// version format 01.01.01
			String[] versions1 = version1.split("\\.");
			String[] versions2 = version2.split("\\.");
			boolean result = false;

			if (versions1.length == 3 && versions2.length == 3) {
				Integer major1 = Integer.parseInt(versions1[0]);
				Integer major2 = Integer.parseInt(versions2[0]);
				Integer minor1 = Integer.parseInt(versions1[1]);
				Integer minor2 = Integer.parseInt(versions2[1]);
				Integer patch1 = Integer.parseInt(versions1[2]);
				Integer patch2 = Integer.parseInt(versions2[2]);

				if (major1 > major2) {
					result = true;
				} else if (major1 == major2) {
					if (minor1 > minor2) {
						result = true;
					} else if (minor1 == minor2) {
						if (patch1 > patch2) {
							result = true;
						} else if (patch1 == patch2) {
							result = true;
						} else {
							result = false;
						}
					} else if (minor2 > minor1) {
						result = false;
					}
				}
			}

			return result;
		}

		private void setVolumeIfAvailable(final int volumeToSet) {
			showApplyingDialog();
			actor.send(new ActorMessage.SetVolume(volume, volumeToSet));
		}

		/**
		 * Get specific setting2 code. called when click on Failed-to-retrieve-camera-data listView item
		 *
		 * @param setting2Code
		 */
		private void getSetting2IfAvailable(String setting2Code) {
			boolean setting2Compatible = mDevice != null && Util.checkSetting2Compatibility(mDevice.getProfile().getModelId(),
					mDevice.getProfile().getFirmwareVersion());
			// if this code is supported by setting2 and device is compatible with setting2
			// -> build all items that do not have value or have invalid value -> then reload these items
			// opposite reload only given code
			if (setting2Compatible && Arrays.asList(PublicDefine.groupSettingsAll).contains(setting2Code)) {
                // all codes that device needs
                buildGroupSetting2Codes();
            } else {
                groupSettings = new String[]{setting2Code};
			}
			getSetting2IfAvailable();
		}


		/**
		 * Get all setting2 codes in groupSettings if available. handle for both setting and setting2.
		 */
		private void getSetting2IfAvailable() {
			if (groupSettings != null && groupSettings.length > 0) {
				// just for logging
				String log = "";
				for (String code : groupSettings) {
					log += (code + ",");
				}
				Log.d("CameraSettingsActivity", "setting codes inside request: " + log);

				boolean setting2Compatible = mDevice != null && Util.checkSetting2Compatibility(mDevice.getProfile().getModelId(),
						mDevice.getProfile().getFirmwareVersion());
				// all setting2 keys have to be handled. include no-supported keys.
                List<String> allSetting2 = Arrays.asList(PublicDefine.groupSettingsAll);
                // build map: setting2 key (base on groupSettings) - UI listChild
				Map<String, ListChild> listChildMap = buildSettingListChildMap();
				// separately get value for no-supported keys in groupSettings. mark others as loading.
				boolean needSendingSetting2Request = false;
				for (int i = 0; i < groupSettings.length; i++) {
					if (setting2Compatible && allSetting2.contains(groupSettings[i])) {
						ListChild item = getListChildBySetting2Code(groupSettings[i]);
						// if item does not have value or have invalid value -> reload it
						if (item != null) {
							needSendingSetting2Request = true;
							item.value = getSafeString(R.string.loading);
						}
					} else {
						if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_CEILING_MOUNT)) {
							Log.d(TAG, "get ceiling mount setting ...");
							getCeilingMountIfAvailable(true);
						} else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_PARKING)) {
							getNotificationSettings(); // need values for park
							Log.d(TAG, "get parking setting ...");
							getParkIfAvailable(true);
						} else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_BRIGHTNESS)) {
							Log.d(TAG, "get brightness setting ...");
							getBrightnessIfAvailable(true);
						} else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_VOLUME)) {
							Log.d(TAG, "get volume setting ...");
							getVolumeIfAvailable(true);
						} else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_CONTRACT)) {
							Log.d(TAG, "get contrast setting ...");
							getContrastIfAvailable(true);
						} else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_NIGHT_VISION)) {
							Log.d(TAG, "get night vision setting ...");
							getNightVisionIfAvailable(true);
						} else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_OVERLAY_DATE)) {
							Log.d(TAG, "get overlay date setting ...");
							getOverlayDateIfAvailable(true);
						} else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_VIEW_MODE)) {
							Log.d(TAG, "get view mode setting ...");
							getViewModeIfAvailable(true);
						} else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_QUALITY_OF_SERVICE)) {
                            Log.d(TAG, "get quality of service setting ...");
                            getQualityOfServiceIfAvailable(true);
                        } else {
							Log.d(TAG, "Skip setting2 code " + groupSettings[i] + ". It may be added after Dec 18, 2015.");
						}
					}
				}
				//listAdapter.notifyDataSetChanged();
				if (needSendingSetting2Request) {
					Log.d(TAG, "a command camera_setting2 has been sent");
					//	showLoadingialog();
					actor.send(new ActorMessage.GetSetting2Settings(groupSettings, listChildMap));
				}
			} else {
				Log.d(TAG, "skip get setting2 because groupSettings is empty");
			}
		}

		private Map<String, ListChild> buildSettingListChildMap() {
			Map<String, ListChild> listChildMap = new HashMap<>();
			List<String> allSetting2 = Arrays.asList(PublicDefine.groupSettingsAll);
			for (int i = 0; i < groupSettings.length; i++) {
				if (allSetting2.contains(groupSettings[i])) {
					listChildMap.put(groupSettings[i], getListChildBySetting2Code(groupSettings[i]));
				}
			}
			return listChildMap;
		}

		private void buildGroupSetting2Codes() {
			if (mDevice.getProfile().isVTechCamera()) {
				groupSettings = PublicDefine.groupSettingsVtech;
			} else {
				// hubble device that support night vision mode
				if (isHubbleIR()) {
					groupSettings = PublicDefine.groupSettingsHubbleIR;
				} else {
					groupSettings = PublicDefine.groupSettingsGeneric;
				}
			}
			// filter codes that already have valid value
			ArrayList<String> filterList = new ArrayList<>();
			for (String code : groupSettings) {
				ListChild item = getListChildBySetting2Code(code);
				if (item == null) {
					continue;
				}
				if (TextUtils.isEmpty(item.value) || item.value.equals(getSafeString(R.string.failed_to_retrieve_camera_data))) {
					filterList.add(code);
				}
			}
			groupSettings = new String[filterList.size()];
			for (int i = 0; i < filterList.size(); i++) {
				groupSettings[i] = filterList.get(i);
			}
		}

		private ListChild getListChildBySetting2Code(String setting2Code) {
			ListChild listChild = null;
			if (setting2Code.equals(PublicDefine.SETTING_2_KEY_CEILING_MOUNT)) {
				listChild = ceilingMount;
			} else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_BRIGHTNESS)) {
				listChild = brightness;
			} else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_VOLUME)) {
				listChild = volume;
			} else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_CONTRACT)) {
				listChild = contrast;
			} else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_NIGHT_VISION)) {
				listChild = nightVision;
			} else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_PARKING)) {
				listChild = park;
			} else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_OVERLAY_DATE)) {
				listChild = overlayDate;
			} else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_VIEW_MODE)) {
				listChild = viewMode;
			}else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_QUALITY_OF_SERVICE)) {
                listChild = qualityOfService;
            }
			return listChild;
		}

		private void getCeilingMountIfAvailable(boolean force) {
			showLoadingialog();
			if (force) {
				if (ceilingMount != null) {
					//ceilingMount.value = getSafeString(R.string.loading);

					actor.send(new ActorMessage.GetCeilingMount(ceilingMount));
				}
			} else {
				if (ceilingMount != null && shouldRefreshListChildValues(ceilingMount)) {
					ceilingMount.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetCeilingMount(ceilingMount));
				}
			}
		}

		private void getNightLightIfAvailable(boolean force) {
			showLoadingialog();
			if (force) {
				if (nightligthListChild != null) {
					actor.send(new ActorMessage.GetNightLight(nightligthListChild));
				}
			} else {
				if (nightligthListChild != null && shouldRefreshListChildValues(nightligthListChild)) {
					nightligthListChild.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetNightLight(nightligthListChild));
				}
			}
		}

		private boolean shouldRefreshListChildValues(ListChild child) {
			boolean shouldRefresh = true;
			if (child != null) {
				if (child.value != null && !child.value.isEmpty()) {
					shouldRefresh = false;
				}
			}
			return shouldRefresh;
		}


        private void setOverlayDate(boolean status) {
            if (overlayDate != null) {
                overlayDate.setOldCopy();
                overlayDate.booleanValue = status;
                showApplyingDialog();
                actor.send(new ActorMessage.SetOverlayDate(ceilingMount, status));
            }

        }

		private void setNightLightIfAvailable(final int mode) {
			if (nightligthListChild != null) {
				nightligthListChild.setOldCopy();
				nightligthListChild.intValue = mode;
				showApplyingDialog();
				actor.send(new ActorMessage.SetNightLight(nightligthListChild, mode));
			}
		}

		private void setCeilingMountIfAvailable(final boolean orientation) {
			if (ceilingMount != null) {
				ceilingMount.setOldCopy();
				ceilingMount.booleanValue = orientation;
				showApplyingDialog();
				actor.send(new ActorMessage.SetCeilingMount(ceilingMount, orientation));
			}
		}

		private void getNotificationSettings() {
			if (motionDetection != null && shouldRefreshListChildValues(motionDetection)) {
				motionDetection.value = getSafeString(R.string.loading);
				if (soundDetection != null) {
					soundDetection.value = getSafeString(R.string.loading);
				}
				if (temperature != null) {
					temperature.value = getSafeString(R.string.loading);
				}
				//showLoadingialog();
				actor.send(new ActorMessage.GetNotificationSettings(motionDetection, soundDetection, temperature));
			}
		}


		private void getLensCorrectionSettings(boolean force)
		{
			if(force)
			{
				if (lensCorrection != null) {
					lensCorrection.value = getSafeString(R.string.loading);

					actor.send(new ActorMessage.GetLensCorrection(lensCorrection));
				}
			}
			else
			{
				if (lensCorrection != null && shouldRefreshListChildValues(lensCorrection)) {
					lensCorrection.value = getSafeString(R.string.loading);

					actor.send(new ActorMessage.GetLensCorrection(lensCorrection));
				}
			}
		}

		private void getVideoRecordingDuration(boolean force)
		{
			if(force)
			{
				if (videoRecording != null)
				{
					videoRecording.value = getSafeString(R.string.loading);

					actor.send(new ActorMessage.GetVideoRecordingDuration(videoRecording));
				}
			}
			else
			{
				if (videoRecording != null && shouldRefreshListChildValues(videoRecording))
				{
					videoRecording.value = getSafeString(R.string.loading);

					actor.send(new ActorMessage.GetVideoRecordingDuration(videoRecording));
				}
			}
		}

		private void forceGetNotificationSettings() {

			if (motionDetection != null) {
				motionDetection.value = getSafeString(R.string.loading);
				if (soundDetection != null) {
					soundDetection.value = getSafeString(R.string.loading);
				}
				if (temperature != null) {
					temperature.value = getSafeString(R.string.loading);
				}
				actor.send(new ActorMessage.GetNotificationSettings(motionDetection, soundDetection, temperature));
			}
			//listAdapter.notifyDataSetInvalidated();

		}

		private void getParkIfAvailable(boolean force) {
			if (force) {
				if (park != null) {
					park.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetPark());
					//listAdapter.notifyDataSetChanged();
				}
			} else {
				if (park != null && shouldRefreshListChildValues(park)) {
					park.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetPark());
				}
			}
		}


		private void getBrightnessIfAvailable(boolean force) {
			showLoadingialog();
			if (force) {
				if (brightness != null) {
					brightness.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetBrightness(brightness));
					//listAdapter.notifyDataSetChanged();
				}
			} else {
				if (brightness != null && shouldRefreshListChildValues(brightness)) {
					brightness.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetBrightness(brightness));
				}
			}
		}

		private void getVolumeIfAvailable(boolean force) {
			showLoadingialog();
			if (force) {
				if (volume != null) {
					volume.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetVolume(volume));
					//listAdapter.notifyDataSetChanged();
				}
			} else {
				if (volume != null && shouldRefreshListChildValues(volume)) {
					volume.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetVolume(volume));
				}
			}
		}

		private void getOverlayDateIfAvailable(boolean force) {
			if (force) {
				if (overlayDate != null) {
					overlayDate.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetOverlayDate(overlayDate));
					//listAdapter.notifyDataSetChanged();
				}
			} else {
				if (overlayDate != null && shouldRefreshListChildValues(overlayDate)) {
					overlayDate.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetOverlayDate(overlayDate));
				}
			}
		}

        private void getViewModeIfAvailable(boolean force) {
            if (force) {
                if (viewMode != null) {
                    viewMode.value = getSafeString(R.string.loading);
                    if (qualityOfService != null && qualityOfService.value.equals(getSafeString(R.string.failed_to_retrieve_camera_data))) {
                        qualityOfService.value = getSafeString(R.string.loading);
                    }
                    actor.send(new ActorMessage.GetViewMode(viewMode, qualityOfService));

                }
            } else {
                if (viewMode != null && shouldRefreshListChildValues(viewMode)) {
                    viewMode.value = getSafeString(R.string.loading);
                    if (qualityOfService != null && qualityOfService.value.equals(getSafeString(R.string.failed_to_retrieve_camera_data))) {
                        qualityOfService.value = getSafeString(R.string.loading);
                    }
                    actor.send(new ActorMessage.GetViewMode(viewMode, qualityOfService));
                }
            }
        }



        private void getQualityOfServiceIfAvailable(boolean force) {
            if (force) {
                if (qualityOfService != null && viewMode != null) {
                    qualityOfService.value = getSafeString(R.string.loading);
                    // because qos depends on view-mode. view-mode should have value first.
                    // message GetViewMode also gets qos.
                    if (!viewMode.value.equals(getSafeString(R.string.loading))) {
                        if (viewMode.value.equals(getSafeString(R.string.failed_to_retrieve_camera_data))) {
                            viewMode.value = getSafeString(R.string.loading);
                            actor.send(new ActorMessage.GetViewMode(viewMode, qualityOfService));
                        } else {
                            actor.send(new ActorMessage.GetQualityOfService(qualityOfService, viewMode.intValue));
                        }
                    }
                }
            } else {
                if (viewMode != null && qualityOfService != null && shouldRefreshListChildValues(qualityOfService)) {
                    // because qos depends on view-mode. view-mode should have value first.
                    // message GetViewMode also gets qos.
                    if (!viewMode.value.equals(getSafeString(R.string.loading))) {
                        if (viewMode.value.equals(getSafeString(R.string.failed_to_retrieve_camera_data))) {
                            viewMode.value = getSafeString(R.string.loading);
                            actor.send(new ActorMessage.GetViewMode(viewMode, qualityOfService));
                        } else {
                            actor.send(new ActorMessage.GetQualityOfService(qualityOfService, viewMode.intValue));
                        }
                    }
                }
            }
        }

		private void getContrastIfAvailable(boolean force) {
			if (force) {
				if (contrast != null) {
					contrast.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetContrast(contrast));
				}
			} else {
				if (contrast != null && shouldRefreshListChildValues(contrast)) {
					contrast.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetContrast(contrast));
				}
			}
		}

		private void getNightVisionIfAvailable(boolean force) {
			showLoadingialog();
			boolean useCommandIR = checkUseCommandIR();
			if (force) {
				if (nightVision != null) {
					nightVision.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetNightVision(nightVision, useCommandIR));
					//listAdapter.notifyDataSetChanged();
				}
			} else {
				if (nightVision != null && shouldRefreshListChildValues(nightVision)) {
					nightVision.value = getSafeString(R.string.loading);
					actor.send(new ActorMessage.GetNightVision(nightVision, useCommandIR));
				}
			}
		}

		private void setBrightnessIfAvailable(final int brightnessToSet) {
			if (brightness != null) {
				showApplyingDialog();
				actor.send(new ActorMessage.SetBrightness(brightness, brightnessToSet));
			}
		}

		/* model 0086 and model 0073 with firmware at least 19.22 use command get_night_vision
   * model 0073 with firmware less than 19.22 use command get_ir_mode */
		private boolean checkUseCommandIR() {
			boolean useCommandIR = false;
			String modelId = mDevice.getProfile().getModelId();
			if ("0086".equals(modelId) || "0877".equals(modelId)) {
				useCommandIR = true;
			} /*else if ("0073".equals(modelId)) {
      String firmwareVersion = device.getProfile().getFirmwareVersion();
      *//* firmware version must be at least 01.19.18 *//*
      if (!Util.isThisVersionGreaterThan("01.19.18", firmwareVersion)) {
        useCommandIR = false;
      }
    } else if ("0173".equals(modelId)) { // model 73S
      String firmwareVersion = device.getProfile().getFirmwareVersion();
      *//* firmware version must be at least 01.19.14 *//*
      if (!Util.isThisVersionGreaterThan("01.19.14", firmwareVersion)) {
        useCommandIR = false;
      }
    }*/
			return useCommandIR;
		}

		private void setNightVisionIfAvailable(final int nightVisionMode, final int nightVisionIntensity) {
			showApplyingDialog();
			actor.send(new ActorMessage.SetNightVision(nightVision, nightVisionMode, nightVisionIntensity));
		}

		private void setNightVisionHubble(final int nightVisionMode) {
			nightVision.setOldCopy();
			nightVision.intValue = nightVisionMode;
			showApplyingDialog();
			actor.send(new ActorMessage.SetNightVisionHubble(nightVision, nightVisionMode, checkUseCommandIR()));
		}

		private void showApplyingDialog() {
			if (CameraSettingsActivity.this != null) {
				if (applyingDialog != null && applyingDialog.isShowing()) {
					try {
						applyingDialog.dismiss();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				applyingDialog = new ProgressDialog(CameraSettingsActivity.this);
				applyingDialog.setCancelable(true);
				applyingDialog.setMessage(getSafeString(R.string.applying));
				applyingDialog.show();
			}
		}

		private void showLoadingialog() {
			if (CameraSettingsActivity.this != null) {
				if (loadingDailog != null && loadingDailog.isShowing()) {
					try {
						loadingDailog.dismiss();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				loadingDailog = new ProgressDialog(CameraSettingsActivity.this);
				loadingDailog.setCancelable(true);
				loadingDailog.setMessage(getSafeString(R.string.loading));
				loadingDailog.show();
			}
		}

		private void showSoundSensitivityValue(int threshold) {
			if (threshold <= 25) {
				soundSensitivity5.setOnClickListener(null);
				soundSensitivity5.setChecked(true);
				soundSensitivity5.setOnClickListener(this);
			} else if (threshold <= 70) {
				soundSensitivity3.setOnClickListener(null);
				soundSensitivity3.setChecked(true);
				soundSensitivity3.setOnClickListener(this);
			} else {
				soundSensitivity1.setOnClickListener(null);
				soundSensitivity1.setChecked(true);
				soundSensitivity1.setOnClickListener(this);
			}

		}

		private void showMotionSensitivityValue(int motionDetectionSensitivity) {
			switch (motionDetectionSensitivity) {
				case 0:
					motionSentivity1.setOnClickListener(null);
					motionSentivity1.setChecked(true);
					motionSentivity1.setOnClickListener(this);
					break;
				case 1:
					motionSentivity2.setOnClickListener(null);
					motionSentivity2.setChecked(true);
					motionSentivity2.setOnClickListener(this);
					break;
				case 2:
					motionSentivity3.setOnClickListener(null);
					motionSentivity3.setChecked(true);
					motionSentivity3.setOnClickListener(this);
					break;
				case 3:
					motionSentivity4.setOnClickListener(null);
					motionSentivity4.setChecked(true);
					motionSentivity4.setOnClickListener(this);
					break;
				case 4:
					motionSentivity5.setOnClickListener(null);
					motionSentivity5.setChecked(true);
					motionSentivity5.setOnClickListener(this);
					break;
			}
		}

		// Classes
	/*public class ListChild {
		public int intValue = -1;
		public int secondaryIntValue = -1;
		public boolean booleanValue = false;
		public boolean secondaryBooleanValue = false;
		public boolean isClickable;
		public String title;
		public String value;
		public String modeVda;

		// Old copy
		public int oldIntValue = -1;
		public int oldSecondaryIntValue = -1;
		public boolean oldBooleanValue = false;
		public boolean oldSecondaryBooleanValue = false;
		public boolean oldIsClickable;
		public String oldTitle;
		public String oldValue;
		public String oldModeVda;

		public ListChild(String title, String value, boolean isClickable) {
			this.title = title;
			this.value = value;
			this.isClickable = isClickable;
		}

		public void setOldCopy() {
			oldIntValue = intValue;
			oldSecondaryIntValue = secondaryIntValue;
			oldBooleanValue = booleanValue;
			oldSecondaryBooleanValue = secondaryBooleanValue;
			oldIsClickable = isClickable;
			oldTitle = title;
			oldValue = value;
			oldModeVda = modeVda;
		}

		public void revertToOldCopy() {
			intValue = oldIntValue;
			secondaryIntValue = oldSecondaryIntValue;
			booleanValue = oldBooleanValue;
			secondaryBooleanValue = oldSecondaryBooleanValue;
			isClickable = oldIsClickable;
			title = oldTitle;
			value = oldValue;
			modeVda = oldModeVda;
		}
	}
*/

		private void setupSoundOrMotionValueField(ListChild notificationChild) {
			if (notificationChild != null) {
				if (!notificationChild.value.equalsIgnoreCase(getSafeString((R.string.failed_to_retrieve_camera_data)))) {
					String value = getSafeString(R.string.detection) + " ";
					if (notificationChild.equals(soundDetection)) {
						soundSwitch.setOnCheckedChangeListener(null);
						soundSwitch.setChecked(soundDetection.booleanValue);
						if (soundDetection.booleanValue) {

							ll_soundSensitivity.setVisibility(View.VISIBLE);
						} else {

							ll_soundSensitivity.setVisibility(View.GONE);
						}
						soundSwitch.setOnCheckedChangeListener(this);
					}
					else if (notificationChild.equals(motionDetection))
					{
						motionSwitch.setOnCheckedChangeListener(null);
						motionSwitch.setChecked(motionDetection.booleanValue);
						if (motionDetection.booleanValue)
						{
                            if(mDevice.getProfile().getModelId().equalsIgnoreCase("0877"))
                            {
                                babyMdTypesLayout.setVisibility(View.VISIBLE);
                                //ARUNA Check if BSC/BSD selected, show only motion options and hide the sensitivity
                                int mdTypeIndex =  CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE);
                                if(mdTypeIndex > 1){
                                    ll_motionSensitivity.setVisibility(View.GONE);
                                }
                            }
                            else if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72))
							{
								babyMdTypesLayout.setVisibility(View.VISIBLE);
							}
                            else
							    ll_motionSensitivity.setVisibility(View.VISIBLE);

							if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)){
								if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION)){
									mvrLayout.setVisibility(View.VISIBLE);
								} else {
									mvrLayout.setVisibility(View.GONE);
								}
							} else {
								mvrLayout.setVisibility(View.VISIBLE);
							}
						} else {

                            if(mDevice.getProfile().getModelId().equalsIgnoreCase("0877") ||
									mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72)) {
								babyMdTypesLayout.setVisibility(View.GONE);
							}

							ll_motionSensitivity.setVisibility(View.GONE);
							mvrLayout.setVisibility(View.GONE);
						}
						motionSwitch.setOnCheckedChangeListener(this);
					}

					if (notificationChild.booleanValue) {
						int threshold = notificationChild.intValue;
						if (threshold != -1) {
							value += getHighMedLowStringFromInt(notificationChild, threshold);
						}

						if (notificationChild.secondaryBooleanValue) {//Record Motion On
							value += ", " + getSafeString(R.string.recording_enabled);
						}

						if (notificationChild.equals(soundDetection)) {

							showSoundSensitivityValue(threshold);
						} else
							//AA-1413: Turn On Recording analytics
							if (notificationChild.equals(motionDetection)) {
								//motionSwitch.setChecked(motionDetection.booleanValue);
								int motionDetectionSensitivity = motionDetection.intValue;
								if (mDevice.getProfile().isVTechCamera()) {
									switch (motionDetectionSensitivity) {
										case 0:
											motionSentivity1.setChecked(true);
											break;
										case 1:
											motionSentivity2.setChecked(true);
											break;
										case 2:
											motionSentivity3.setChecked(true);
											break;
										case 3:
											motionSentivity4.setChecked(true);
											break;
										case 4:
										case 5:
										case 6:
											motionSentivity5.setChecked(true);
											break;
									}

								} else {

									showMotionSensitivityValue(motionDetectionSensitivity);
								}

							} else {
								value += getSafeString(R.string.off);
							}
						notificationChild.value = value;
					}
				}
			}
		}

		private void setupGenericMotionValueField(ListChild notificationChild) {
			String modelId = (mDevice != null) ? mDevice.getProfile().getModelId() : null;
			String fwVersion = (mDevice != null) ? mDevice.getProfile().getFirmwareVersion() : null;
			if (NotificationSettingUtils.supportMultiMotionTypes(modelId, fwVersion))
			{
				setupMotionVdaValueField(notificationChild);
			}
			else {
				setupSoundOrMotionValueField(notificationChild);
			}
		}


		private void setupMotionVdaValueField(ListChild notificationChild) {
			if (notificationChild != null) {
				if (!notificationChild.value.equalsIgnoreCase(getSafeString((R.string.failed_to_retrieve_camera_data)))) {
					String value = getSafeString(R.string.detection) + " ";
					int mdTypeIndex = NotificationSettingUtils.getMotionDetectionTypeIndex(notificationChild.modeVda);
					switch (mdTypeIndex) {
						case CameraSettingsActivity.MD_TYPE_MD_INDEX:
							value += getSafeString(R.string.motion_detection);
							break;
						case CameraSettingsActivity.MD_TYPE_BSC_INDEX:
							value += getSafeString(R.string.sleep_analytics);
							break;
						case CameraSettingsActivity.MD_TYPE_BSD_INDEX:
							value += getSafeString(R.string.experession_detection);
							break;
						case CameraSettingsActivity.MD_TYPE_OFF_INDEX:
							value += getSafeString(R.string.off);
							break;
					}

        /*
         * 20160830: Hoang: The recording storage option is just available for MD mode now.
         * In BSC and BSD mode, app will ignore it.
         */
					Log.d(TAG, "Setup motion detection type VDA, mode: " + notificationChild.modeVda);
					if (mdTypeIndex == CameraSettingsActivity.MD_TYPE_MD_INDEX) {
						if (notificationChild.secondaryBooleanValue) {
							value += ", " + getSafeString(R.string.recording_enabled);
						}
					}
					notificationChild.value = value;
				}
			}
		}

		private void setupMotionVda(ListChild notificationChild, int valueMode) {
			if (notificationChild != null) {
				Log.d(TAG, "setup motion vda, mode? " + valueMode);
				if (!notificationChild.value.equalsIgnoreCase(getSafeString((R.string.failed_to_retrieve_camera_data)))) {
					String value = getSafeString(R.string.detection) + " ";
					switch (valueMode) {
						case CameraSettingsActivity.MD_TYPE_MD_INDEX:
							value += getSafeString(R.string.motion_detection);
							notificationChild.modeVda = CameraSettingsActivity.MD_TYPE_MD;
							break;
						case CameraSettingsActivity.MD_TYPE_BSC_INDEX:
                            if (NotificationSettingUtils.supportMultiMotionTypes(mDevice.getProfile().getModelId(),
                                    mDevice.getProfile().getFirmwareVersion())) {
                                value += getSafeString(R.string.sleep_analytics);
                                notificationChild.modeVda = CameraSettingsActivity.MD_TYPE_BSC;
                            } else {
                                value += getSafeString(R.string.pir_based_detection);
                                notificationChild.modeVda = CameraSettingsActivity.MD_TYPE_PIR;
                            }
                            break;
						case CameraSettingsActivity.MD_TYPE_BSD_INDEX:
							value += getSafeString(R.string.experession_detection);
							notificationChild.modeVda = CameraSettingsActivity.MD_TYPE_BSD;
							break;
						case CameraSettingsActivity.MD_TYPE_OFF_INDEX:
							value += getSafeString(R.string.off);
							notificationChild.modeVda = CameraSettingsActivity.MD_TYPE_OFF;
							break;
					}

        /*
         * 20160830: Hoang: The recording storage option is just available for MD mode now.
         * In BSC and BSD mode, app will ignore it.
         */
					if (valueMode == CameraSettingsActivity.MD_TYPE_MD_INDEX) {
						if (notificationChild.secondaryBooleanValue) {//Record Motion On
							value += ", " + getSafeString(R.string.recording_enabled);
						}
					}
					notificationChild.value = value;
				}
			}
		}

		private void setupTemperatureValueField(ListChild temperatureListChild) {
			if (temperatureListChild != null) {
				if (!temperatureListChild.value.equalsIgnoreCase(getSafeString((R.string.failed_to_retrieve_camera_data)))) {
					String value = getSafeString(R.string.low_than) + " ";
					tempSwitch.setOnCheckedChangeListener(null);
					tempSwitch.setChecked(temperature.booleanValue);
					if (temperature.booleanValue && temperature.secondaryBooleanValue) {
						ll_temparatureDetection.setVisibility(View.VISIBLE);
					} else {
						ll_temparatureDetection.setVisibility(View.GONE);
					}
					tempSwitch.setOnCheckedChangeListener(this);

					if (temperatureListChild.booleanValue) {
						value += getTemperatureInRegionMeasurement(temperatureListChild.intValue);
					} else {
						value += getSafeString(R.string.off);
					}
					value += ", " + getSafeString(R.string.high_than) + " ";
					if (temperatureListChild.secondaryBooleanValue) {
						value += getTemperatureInRegionMeasurement(temperatureListChild.secondaryIntValue) + " ";
					} else {
						value += getSafeString(R.string.off);
					}
       /* tempSeekbar.setMinValue(temperatureListChild.intValue);
	      tempSeekbar.setMaxValue(temperatureListChild.secondaryIntValue);*/
					tempSeekbar.setSelectedMinValue(temperatureListChild.intValue);
					tempSeekbar.setSelectedMaxValue(temperatureListChild.secondaryIntValue);
					temperatureListChild.value = value;
				}
			}
		}

		private int getTemperatureInRegionMeasurement(int temperatureValue) {

			final Boolean isCelsius = (settings.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) == PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);

			if (isCelsius) {
				return temperatureValue;
			} else {
				return Math.round(CommonUtil.convertCtoF(temperatureValue));
			}
		}

		private String getHighMedLowStringFromInt(ListChild notificationChild, int levelInt) {
			if (mDevice.getProfile().isVTechCamera()) {
				if (levelInt <= 1) {
					return notificationChild.equals(motionDetection) ? getSafeString(R.string.low)
							: getSafeString(R.string.low_sound);
				} else if (levelInt <= 4) {
					return getSafeString(R.string.medium);
				} else {
					return notificationChild.equals(motionDetection) ? getSafeString(R.string.high)
							: getSafeString(R.string.high_sound);
				}
			} else {
				if (notificationChild.equals(motionDetection)) {
					if (levelInt <= 1) {
						return getSafeString(R.string.low);
					} else if (levelInt == 2) {
						return getSafeString(R.string.medium);
					} else {
						return getSafeString(R.string.high);
					}
				} else {
					if (levelInt <= 0) {
						return getSafeString(R.string.low_sound);
					} else if (levelInt == 1) {
						return getSafeString(R.string.medium);
					} else {
						return getSafeString(R.string.high_sound);
					}
				}
			}
		}

		private void disableMvr() {
            if(mIsActivityRunning)
			    mDialog = ProgressDialog.show(CameraSettingsActivity.this, null, getSafeString(R.string.disabling_motion_video_recording));
			actor.setRecordingParameter(PublicDefineGlob.SET_RECORDING_PARAMETER_MVR_OFF_PARAM, new CameraSettingsActor.MVRListener() {
				@Override
				public void onMVRResponse(boolean success) {
					if (success) {
						CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING, false);
						if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
								&& Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(), PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)) {
							/*int currentRecordingDuration = CommonUtil.getVideoRecording(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_RECORDING);
							SetVideoRecordingDuration(PublicDefine.VIDEO_RECORDING_OFF_DURATION, currentRecordingDuration);*/
							//Do not set duration to zero firmware will ignore. Only hide the view
							mVideoRecordingLayout.setVisibility(View.GONE);
						}
						GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.VIDEO_STORAGE_MODE_CHANGE_TO+" :"+AppEvents.OFF_SUCCESS,AppEvents.VIDEO_STORAGE_OFF);
						ZaiusEvent videoStorageOffEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
						videoStorageOffEvt.action(AppEvents.VIDEO_STORAGE_MODE_CHANGE_TO+" :"+AppEvents.OFF_SUCCESS);
						try {
							ZaiusEventManager.getInstance().trackCustomEvent(videoStorageOffEvt);
						} catch (ZaiusException e) {
							e.printStackTrace();
						}
						setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_OFF);
						setRecordingPlanVisible(false);

						// Update setting for device
						motionDetection.secondaryBooleanValue = false;
					} else {
						GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.VIDEO_STORAGE_MODE_CHANGE_TO+" : "+AppEvents.OFF_FAILURE,AppEvents.VIDEO_STORAGE_OFF);
						ZaiusEvent videoStorageOffEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
						videoStorageOffEvt.action(AppEvents.VIDEO_STORAGE_MODE_CHANGE_TO+" : "+AppEvents.OFF_FAILURE);
						try {
							ZaiusEventManager.getInstance().trackCustomEvent(videoStorageOffEvt);
						} catch (ZaiusException e) {
							e.printStackTrace();
						}

						setSpinnerSelectionWithoutChangeEvent(previousSelectionValue);
						motionDetection.secondaryIntValue = previousSelectionValue;
					}
					try {
                        if(mDialog != null && mDialog.isShowing())
						    mDialog.dismiss();
					} catch (Exception e) {
						e.printStackTrace();
					}
					motionDetection.secondaryBooleanValue = !success;
					motionDetection.booleanValue = motionSwitch.isChecked();
					setupSoundOrMotionValueField(motionDetection);
				}
			});
		}

		private boolean doSubscriptionFlow(Dialog dialog) {
			String savedToken = Global.getApiKey(mContext);
			if (savedToken == null) {
				return false;
			}
			SubscriptionWizard subWizard = new SubscriptionWizard(savedToken, CameraSettingsActivity.this, mDevice, true, dialog);
			try {
				return subWizard.verify().get();
			} catch (Exception e) {
				return false;
			}
		}

		private void enableMvrOnCloud() {
			if (BuildConfig.ENABLE_SUBSCRIPTIONS) {
			/*final ProgressDialog progressDialog = new ProgressDialog(CameraSettingsActivity.this);
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.setMessage(getSafeString(R.string.enabling));
			//progressDialog.show();*/
                if(mIsActivityRunning)
				 mDialog = ProgressDialog.show(CameraSettingsActivity.this, null, getSafeString(R.string.enabling_motion_video_recording));
				AsyncPackage.doInBackground(new Runnable() {
					@Override
					public void run() {
						final boolean shouldEnableMVR = doSubscriptionFlow(mDialog);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Dialog mDialogDisable = null;
								if (mDialog != null && mDialog.isShowing()) {
									if (!shouldEnableMVR) {
										mDialog.dismiss();
                                        if(mIsActivityRunning)
										    mDialogDisable = ProgressDialog.show(CameraSettingsActivity.this, null, getSafeString(R.string.disabling_motion_video_recording));
									}
								}

								try {
									DeviceSingleton.getInstance().update(false).get();
								} catch (Exception ignored) {
									ignored.printStackTrace();
								}
								ListChild currentPlan = new ListChild(getSafeString(R.string.current_plan), getSubscriptionPlanText(), false);

								if (currentPlan != null) {
									currentPlan.value = getSubscriptionPlanText();
								}

								if (mDialog != null && mDialog.isShowing() ) {
									mDialog.dismiss();
								}

								if (mDialogDisable != null && mDialogDisable.isShowing())  {
									mDialogDisable.dismiss();
								}

								Log.i(TAG, "Should enable MVR: " + shouldEnableMVR);
								if (!shouldEnableMVR) {
									Toast.makeText(mContext, getSafeString(R.string.switched_record_storage_mode_failed), Toast.LENGTH_SHORT).show();
									GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.VIDEO_STORAGE_MODE_CHANGETOCLOUD+" : "+AppEvents.FAILURE,AppEvents.VIDEO_STORAGE_MODE);
										setSpinnerSelectionWithoutChangeEvent(previousSelectionValue);

									ZaiusEvent videoStorageStatusEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
									videoStorageStatusEvt.action(AppEvents.VIDEO_STORAGE_MODE_CHANGETOCLOUD+" : "+AppEvents.FAILURE);
									try {
										ZaiusEventManager.getInstance().trackCustomEvent(videoStorageStatusEvt);
									} catch (ZaiusException e) {
										e.printStackTrace();
									}


								} else {
									Device device = mDevice;
									if (device != null) {
										device.getProfile().getDeviceAttributes().setStorageMode("0");
										CommonUtil.setSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING, true);
										CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 0);

									}
					                previousSelectionValue = 1;
									setRecordingPlanVisible(false);
									Toast.makeText(mContext, getSafeString(R.string.switched_record_storage_mode_succeeded), Toast.LENGTH_SHORT).show();
									//If orbit firmware with plan and cloud is switched on from off set recording time
									if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
											&& Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(), PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)
											&& previousSelectionValue == RECORD_MOTION_OPT_OFF) {
										int currentRecordingDuration = CommonUtil.getVideoRecording(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_RECORDING);
										if (currentRecordingDuration != -1 && currentRecordingDuration != 0)
											setUpVideoRecordingDuration(currentRecordingDuration);
										else
											SetVideoRecordingDuration(PublicDefine.VIDEO_RECORDING_DEFAULT_DURATION, currentRecordingDuration);
									}
								    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.VIDEO_STORAGE_MODE_CHANGETOCLOUD+" : " +AppEvents.SUCCESS,AppEvents.VIDEO_STORAGE_MODE);

									ZaiusEvent vidoeStorageSuccessEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
									vidoeStorageSuccessEvt.action(AppEvents.VIDEO_STORAGE_MODE_CHANGETOCLOUD+" : " +AppEvents.SUCCESS);
									try {
										ZaiusEventManager.getInstance().trackCustomEvent(vidoeStorageSuccessEvt);
									} catch (ZaiusException e) {
										e.printStackTrace();
									}

								}
								motionDetection.secondaryBooleanValue = shouldEnableMVR;
								motionDetection.booleanValue = motionSwitch.isChecked();

								setupSoundOrMotionValueField(motionDetection);
							}
						});
					}
				});
			} else {
				//showOldFreeTrialDialog(buttonView);
				Log.i(TAG, "Show old free trial dialog here.");
			}
		}

		private void enableMvrOnSDCard() {
			Log.i(TAG, "Enable motion video recording on sd card");

            if(mIsActivityRunning)
			 mDialog = ProgressDialog.show(CameraSettingsActivity.this, null,
					getSafeString(R.string.switching_motion_recording_to_sdcard_storage));

			AsyncPackage.doInBackground(new Runnable() {
				@Override
				public void run() {
					try {
						// AA-1177: As Tho request, set_recording_parameter should call after set storage mode
						final String apiKey = Global.getApiKey(mContext);
						final String regId = mDevice.getProfile().getRegistrationId();
						// send set storage mode on sdcard to server
						final Models.ApiResponse<Models.DeviceSettingData> response = Api.getInstance().getService().setDeviceSettings(apiKey, regId,
								"storage_mode", "1", "device", "1");
						final String status = response.getStatus();
						if (status.equals("200")) {
							Log.i(TAG, "Set storage mode on sdcard to server succeeded, enable motion recording on camera");
							actor.setRecordingParameter(PublicDefineGlob.SET_RECORDING_PARAMETER_MVR_ON_PARAM, new CameraSettingsActor.MVRListener() {
								@Override
								public void onMVRResponse(final boolean success) {
									if (!success) {
										Log.i(TAG, "Enable motion recording on camera is failed revert the server settings");
										if(previousSelectionValue != RECORD_MOTION_OPT_OFF) {
											//revert to cloud as cloud is default storage mode
											AsyncPackage.doInBackground(new Runnable() {
												@Override
												public void run() {
													final Models.ApiResponse<Models.DeviceSettingData> response = Api.getInstance().getService().setDeviceSettings(apiKey, regId,
															"storage_mode", "0", "device", "1");
												}
											});
										}

									}
									Log.i(TAG," Set storage mode to sdcard result"+success);
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (success) {
												Device device = mDevice;
												if (device != null) {
													device.getProfile().getDeviceAttributes().setStorageMode("1");
													CommonUtil.setSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING, true);

													CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 1);

												}
												//If orbit firmware with plan and sdcard is switched on from off set recording time
												if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
														&& Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(), PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)
														&& previousSelectionValue == RECORD_MOTION_OPT_OFF) {
													int currentRecordingDuration = CommonUtil.getVideoRecording(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_RECORDING);
													if (currentRecordingDuration != -1 && currentRecordingDuration != 0)
														setUpVideoRecordingDuration(currentRecordingDuration);
													else
														SetVideoRecordingDuration(PublicDefine.VIDEO_RECORDING_DEFAULT_DURATION, currentRecordingDuration);
												}
												//Sdcard change is successful both in server and camera
												setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_SDCARD); //??? just comment it
												// successful case
												GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.VIDEO_STORAGE_MODE_CHANGE_TO_SDCARD + " : " + AppEvents.SDCARD_SUCCESS, AppEvents.VIDEO_STORAGE_ONSDCARD);
												ZaiusEvent videoStorageSdcardEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
												videoStorageSdcardEvt.action(AppEvents.VIDEO_STORAGE_MODE_CHANGE_TO_SDCARD + " : " + AppEvents.SDCARD_SUCCESS);
												try {
													ZaiusEventManager.getInstance().trackCustomEvent(videoStorageSdcardEvt);
												} catch (ZaiusException e) {
													e.printStackTrace();
												}
												previousSelectionValue = 2;
												Toast.makeText(mContext, getSafeString(R.string.switched_record_storage_mode_succeeded), Toast.LENGTH_SHORT).show();
												motionDetection.secondaryBooleanValue = true;
												if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)){
													if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(), PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)) {
														setRecordingPlanVisible(true);
													} else {
														setRecordingPlanVisible(false);
													}
												} else {
													setRecordingPlanVisible(true);
												}
												try {
                                                    if(mDialog != null && mDialog.isShowing())
													   mDialog.dismiss();
												} catch (Exception e) {

												}
											} else {
												Log.i(TAG, "Set storage mode on sdcard to server failed " + response.getMessage() + " old value: " + previousSelectionValue);
												setSpinnerSelectionWithoutChangeEvent(previousSelectionValue);
												if (previousSelectionValue == RECORD_MOTION_OPT_OFF) { //  from off to sd card
													motionDetection.secondaryBooleanValue = false;
												} else { // from cloud to sd card
													motionDetection.secondaryBooleanValue = true;
												}
												// hide nest views
												setRecordingPlanVisible(false);
												try {
                                                    if(mDialog != null && mDialog.isShowing())
													    mDialog.dismiss();
												} catch (Exception e) {
													GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.VIDEO_STORAGE_MODE_CHANGE_TO_SDCARD+" : "+AppEvents.SDCARD_FAILURE+" : "+status ,AppEvents.VIDEO_STORAGE_ONSDCARD);
													ZaiusEvent videoStorageSdcardEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
													videoStorageSdcardEvt.action(AppEvents.VIDEO_STORAGE_MODE_CHANGE_TO_SDCARD+" : "+AppEvents.SDCARD_FAILURE+" : "+status);
													try {
														ZaiusEventManager.getInstance().trackCustomEvent(videoStorageSdcardEvt);
													} catch (ZaiusException ze) {
														ze.printStackTrace();
													}
												}
												Toast.makeText(mContext, getSafeString(R.string.switched_record_storage_mode_failed),Toast.LENGTH_SHORT).show();
											}
										}
									});
								}
							});
						}
					}catch (final RetrofitError e) {
						e.printStackTrace();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (e.getResponse() != null && e.getResponse().getBody() != null) {
									TypedByteArray body = (TypedByteArray) e.getResponse().getBody();
									String bodyText = new String(body.getBytes());
									setSpinnerSelectionWithoutChangeEvent(previousSelectionValue);
									Log.i(TAG, "SET SDCARD RECORDING ERROR: " + bodyText);
									GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING,AppEvents.VIDEO_STORAGE_MODE+" : "+AppEvents.SDCARD_FAILURE+" : "+e.getResponse().getReason() ,AppEvents.VIDEO_STORAGE_ONSDCARD);
									ZaiusEvent videoStorageSdcardEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
									videoStorageSdcardEvt.action(AppEvents.VIDEO_STORAGE_MODE+" : "+AppEvents.SDCARD_FAILURE+" : "+e.getResponse().getReason());
									try {
										ZaiusEventManager.getInstance().trackCustomEvent(videoStorageSdcardEvt);
									} catch (ZaiusException ze) {
										ze.printStackTrace();
									}
								}
								// hide nest views
								setRecordingPlanVisible(false);
								try {
                                    if(mDialog != null && mDialog.isShowing())
									   mDialog.dismiss();
								} catch (Exception e) {

								}
								Toast.makeText(mContext, getSafeString(R.string.switched_record_storage_mode_failed),Toast.LENGTH_SHORT).show();
							}
						});
					} catch (final Exception ex) {
						ex.printStackTrace();
						try {
                            if(mDialog != null && mDialog.isShowing())
							   mDialog.dismiss();
						} catch (Exception e) {

						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mContext, getSafeString(R.string.switched_record_storage_mode_failed), Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			});
		}

		private void setSpinnerSelectionWithoutChangeEvent(int value) {
//		recordStorageSpinner.setOnItemSelectedListener(null);
//		recordStorageSpinner.setSelection(value, false);
//		recordStorageSpinner.setOnItemSelectedListener(recordListener);
//		previousSelectionValue = value;
			previousSelectionValue = value;
			switch (value) {

				case RECORD_MOTION_OPT_OFF:
					mvrOff.setOnCheckedChangeListener(null);
					mvrOff.setChecked(true);
					mvrOff.setOnCheckedChangeListener(this);
					CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING, false);

					break;

				case RECORD_MOTION_OPT_CLOUD:

					mvrCould.setOnCheckedChangeListener(null);
					mvrCould.setChecked(true);
					mvrCould.setOnCheckedChangeListener(this);
					CommonUtil.setSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING, true);

					CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 0);

					break;

				case RECORD_MOTION_OPT_SDCARD:
					mvrSdCard.setOnCheckedChangeListener(null);
					mvrSdCard.setChecked(true);
					mvrSdCard.setOnCheckedChangeListener(this);
					CommonUtil.setSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING, true);

					CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 1);

					break;
			}
		}

		private void setRecordingPlanVisible(boolean isVisible) {
			if (isVisible) {
				sdcardLayout.setVisibility(View.VISIBLE);
                if (CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_DELETE_LAST_TEN)) {
                    mRemoveSdcardClip.setChecked(true);
                } else if (CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_SWITCH_CLOUD)) {
                    mSwitchCloud.setChecked(true);
                }
			} else {
				sdcardLayout.setVisibility(View.GONE);
			}
		}

		private String getSubscriptionPlanText() {
			String subscriptionText = getSafeString(R.string.none);
			if (BuildConfig.ENABLE_SUBSCRIPTIONS) {
				try {
					DeviceProfile profile = mDevice.getProfile();
					if (profile != null) {
						if (profile.getPlanId() != null && !profile.getPlanId().equalsIgnoreCase("freemium")) {
							if (profile.getDeviceFreeTrial() != null) {
								if (profile.getDeviceFreeTrial().isActive()) {
									subscriptionText = getSafeString(R.string.free_trial);
								} else if (profile.getPlanId() != null) {
									subscriptionText = profile.getPlanId();
								} else {
									subscriptionText = getSafeString(R.string.none);
								}
							} else if (profile.getPlanId() != null) {
								subscriptionText = profile.getPlanId();
							}
						} else {
							subscriptionText = getSafeString(R.string.none);
						}
					}
				} catch (Exception ignored) {
				}
			}
			return subscriptionText;
		}

		void setSoundThreshold(int threshold) {
			soundDetection.setOldCopy();
			soundDetection.booleanValue = soundSwitch.isChecked();
			soundDetection.intValue = threshold;
			soundDetection.value = getHighMedLowStringFromInt(soundDetection, threshold);
			setupSoundOrMotionValueField(soundDetection);
			setSoundThresholdIfAvailable(threshold);
			//listAdapter.notifyDataSetChanged();
		}

		void setMotionSenitivityValue(int sensitivityValue) {
		/*if(mDevice.getProfile().getModelId().equalsIgnoreCase("0080")){
			switch (sensitivityValue){
				case 0: setMotionSensitivity(0);
					    break;
				case 1: setMotionSensitivity(30);
					break;
				case 2: setMotionSensitivity(60);
					break;
				case 3: setMotionSensitivity(60);
					break;
				case 4: setMotionSensitivity(100);
					break;
			}
		}else{*/
			//	motionDetection.booleanValue = motionSwitch.isChecked();
			setMotionSensitivityIfAvailable();

			//}
		}

		void setMotionSensitivity(final int sensitivity) {
			if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)) {
				AnalyticsInterface.getInstance().trackEvent(AppEvents.MOTIONSENSITIVITY, AppEvents.MOTION_SENSITIVITY_VALUE + sensitivity, eventData);
				displayProgressDialog(false);
				SendCommand setRecording = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), mDevice.getProfile().getRegistrationId(), "set_pir_sensitivity" + "&value=" + sensitivity);

				DeviceManager.getInstance(CameraSettingsActivity.this).sendCommandRequest(setRecording, new com.android.volley.Response.Listener<SendCommandDetails>() {

							@Override
							public void onResponse(SendCommandDetails response1) {
								String responsebody = response1.getDeviceCommandResponse().getBody().toString();
								Log.i(TAG, "SERVER RESP : " + responsebody);
								if (response1.getDeviceCommandResponse() != null && responsebody.contains("set_pir_sensitivity")) {

									try {
										try {
											final Pair<String, Object> parsedResponse = CommonUtil.parseResponseBody(responsebody);
											if (parsedResponse != null && parsedResponse.second instanceof Float) {
												if (motionLayout.getVisibility() == View.VISIBLE) {
													orbitCurrentMotionValue = sensitivity;

													if (sensitivity == 0) {
														sharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", false).commit();
													} else {
														sharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", true).commit();
													}
													dismissDialog();
													cameraSettings();

												}
											}
										} catch (Exception exception) {

											Log.d(TAG, exception.getMessage());
											exception.printStackTrace();
											dismissDialog();
											cameraSettings();
										}


									} catch (Exception ex) {
										dismissDialog();
										cameraSettings();
									}
								}
							}
						}, new com.android.volley.Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {

								if (error != null && error.networkResponse != null) {
									Log.d(TAG, error.networkResponse.toString());
									Log.d(TAG, error.networkResponse.data.toString());
								}
								dismissDialog();
								cameraSettings();
							}
						}

				);

			}
		}

		/*void getMotionSensitivityOrbit(){

		displayProgressDialog();
		SendCommand setRecording = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mDevice.getProfile().getRegistrationId(), "value_pir_sensitivity");

		DeviceManager.getInstance(CameraSettingsActivity.this).sendCommandRequest(setRecording, new com.android.volley.Response.Listener<SendCommandDetails>() {

					@Override
					public void onResponse(SendCommandDetails response1) {
						String responsebody = response1.getDeviceCommandResponse().getBody().toString();
						Log.i(TAG, "SERVER RESP : " + responsebody);
						if (response1.getDeviceCommandResponse() != null && responsebody.contains("value_pir_sensitivity")) {

							try {
								try{
									final Pair<String, Object> parsedResponse= CommonUtil.parseResponseBody(responsebody);
									if(parsedResponse != null && parsedResponse.second instanceof Float)
									{
										if(motionLayout.getVisibility() == View.VISIBLE){
											orbitCurrentMotionValue = ((Float) parsedResponse.second).intValue();
											dismissDialog();
											cameraSettings();

										}
									}
								} catch (Exception exception) {

									Log.d(TAG, exception.getMessage());
									exception.printStackTrace();
									dismissDialog();
									cameraSettings();
								}


							} catch (Exception ex) {
								dismissDialog();
								cameraSettings();
							}
						}
					}
				}, new com.android.volley.Response.ErrorListener()
				{
					@Override
					public void onErrorResponse(VolleyError error)
					{

						if(error != null && error.networkResponse != null)
						{
							Log.d(TAG,error.networkResponse.toString());
							Log.d(TAG,error.networkResponse.data.toString());
						}
						dismissDialog();
						cameraSettings();
					}
				}

		);
	}
*/
		private void setSoundDetectionIfAvailable(final boolean soundDetectionEnabled) {
			showApplyingDialog();

			soundDetection.setOldCopy();
			soundDetection.booleanValue = soundSwitch.isChecked();

			setupSoundOrMotionValueField(soundDetection);
			actor.send(new ActorMessage.SetSoundDetection(soundDetection, soundDetectionEnabled));

		}

		private void setSoundThresholdIfAvailable(final int soundDetectionThreshold) {
			showApplyingDialog();
			actor.send(new ActorMessage.SetSoundThreshold(soundDetection, soundDetectionThreshold));

		}

		private void setTemperatureDetectionIfAvailable(final boolean isEnabled) {
			showApplyingDialog();
			actor.send(new ActorMessage.SetTemperatureDetection(temperature, isEnabled));
		}

		private void setLowTemperatureThreshold(int lowThreshold) {
			showApplyingDialog();
			actor.send(new ActorMessage.SetLowTemperatureThreshold(temperature, lowThreshold));
		}

		private void setHighTemperatureThreshold(int highThreshold) {
			showApplyingDialog();
			actor.send(new ActorMessage.SetHighTemperatureThreshold(temperature, highThreshold));
		}

		private void getFwVersionIfAvailable() {

			if (mDevice.getProfile().getFirmwareVersion() != null) {
				firmwareVersion.setText(mDevice.getProfile().getFirmwareVersion());
			}

		}

		private void checkForFirmwareUpdate() {
			if (!mDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
				if (mDevice.getProfile().isAvailable()) {
					//Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.processing_animation);

                    displayProgressDialog(true);
					LocalDetectorService.getService().isLocalCamera(mDevice
							, new FutureCallback<Boolean>() {
								@Override
								public void onCompleted(Exception e, final Boolean result) {
									if(mIsActivityRunning) {
										if ((BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew")) && !"0086".equals(mDevice.getProfile().getModelId())) {

											createCheckFirmwareUpdateTask(result);


										} else {
											if (result) {

												createCheckFirmwareUpdateTask(true);

											} else {
												dismissDialog();
												showFWDialog(getSafeString(R.string.unable_to_fw_upgrade_remotely));
											}
										}
									}

								}
							});
				} else {
					showFWDialog(getSafeString(R.string.your_camera_is_offline_now));
				}
			}
		}


		private void createCheckFirmwareUpdateTask(final boolean isLocal) {
			//showProgressBar();
			final String fwVersion = mDevice.getProfile().getFirmwareVersion();
			final String regId = mDevice.getProfile().getRegistrationId();
			final String modelId = mDevice.getProfile().getModelId();
			final String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

			boolean deviceOTA = false;
			if (mDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0)
			{
				if (Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(), CheckFirmwareUpdateTask.ORBIT_NEW_FIRMWARE_WORK_FLOW))
				{
					deviceOTA = true;
				}

				final boolean isDeviceOTA  = deviceOTA;
				Runnable runnableBatteryMode = new Runnable()
				{
					@Override
					public void run()
					{
						float deviceMode = -1;
						float batteryValue = -1;

						int allowFirmwareUpgrade = PublicDefine.DEVICE_FIRMWARE_UPGRADE_NOT_ALLOW;

						if(BuildConfig.DEBUG)
							Log.d(TAG, "getting battery mode ");

						boolean isInLocal = false;

						String res = CommandUtils.sendCommand(mDevice, PublicDefine.GET_DEVICE_MODE, isLocal);

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


						final int batteryStatus = (int)deviceMode;

						if(batteryStatus != -1 && batteryStatus != CameraStatusView.ORBIT_BATTERY_CHARGING)
						{
							allowFirmwareUpgrade = PublicDefine.DEVICE_FIRMWARE_UPGRADE_NOT_ALLOW;

							String batteryResponse = CommandUtils.sendCommand(mDevice, PublicDefine.GET_BATTERY_VALUE, isInLocal);

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

								if(batteryValue != -1 && batteryValue <= PublicDefine.ORBIT_MINIMUM_BATTERY_LEVEL)
								{
									allowFirmwareUpgrade = PublicDefine.DEVICE_FIRMWARE_LOW_BATTERY;
								}
								else if(batteryValue == -1 )
								{
									allowFirmwareUpgrade = PublicDefine.DEVICE_FIRMWARE_UPGRADE_NOT_ALLOW;
								}
								else
								{
									allowFirmwareUpgrade = PublicDefine.DEVICE_FIRMWARE_ALLOW;
								}

							}

						}
						else if(batteryStatus == -1)
						{
							allowFirmwareUpgrade  = PublicDefine.DEVICE_FIRMWARE_UPGRADE_NOT_ALLOW;
						}
						else
						{
							allowFirmwareUpgrade = PublicDefine.DEVICE_FIRMWARE_ALLOW;
						}


						if(allowFirmwareUpgrade == PublicDefine.DEVICE_FIRMWARE_ALLOW)
						{
							CheckFirmwareUpdate(saved_token, regId, fwVersion, modelId,isLocal,isDeviceOTA);
						}
						else
						{
							final String message = (allowFirmwareUpgrade == PublicDefine.DEVICE_FIRMWARE_LOW_BATTERY) ? getResources().getString(R.string.device_offline_battery_low)
									: getResources().getString(R.string.device_not_online);

							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									dismissDialog();

									AlertDialog.Builder builder = new AlertDialog.Builder(CameraSettingsActivity.this);

									builder.setMessage(message)
										   .setTitle(getSafeString(R.string.firmware_upgrade))
											.setPositiveButton(getSafeString(R.string.OK), new DialogInterface.OnClickListener() {
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
									AlertDialog alertDialog = builder.create();
									alertDialog.show();
									Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
									pbutton.setTextColor(getResources().getColor(R.color.text_blue));
								}
							});
						}
					}
				};
				Thread worker = new Thread(runnableBatteryMode);
				worker.start();

			}
			else
			{
				CheckFirmwareUpdate(saved_token, regId, fwVersion, modelId,isLocal,deviceOTA);
			}


		}

		private void CheckFirmwareUpdate(final String token,String regID,String firmwareVersion,String modelID,final boolean isLocal,final boolean deviceOTA)
		{
			new CheckFirmwareUpdateTask(token, regID, firmwareVersion, modelID, mDevice, new IAsyncTaskCommonHandler() {
				@Override
				public void onPreExecute() {
				}

				@Override
				public void onPostExecute(final Object result) {
					if (result instanceof CheckFirmwareUpdateResult) {
						CheckFirmwareUpdateResult checkFirmwareUpdateResult = (CheckFirmwareUpdateResult) result;
						checkFirmwareUpdateResult.setLocalCamera(true);
						checkFirmwareUpdateResult.setInetAddress(mDevice.getProfile().getDeviceLocation().getLocalIp());
						checkFirmwareUpdateResult.setApiKey(apiKey);
						checkFirmwareUpdateResult.setRegID(mDevice.getProfile().getRegistrationId());
						//hideProgressBar();
						handleCheckFwUpdateResult(checkFirmwareUpdateResult, isLocal);
					}

					dismissDialog();
				}

				@Override
				public void onCancelled() {

					dismissDialog();
				}
			}, settings.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false), deviceOTA).execute();
		}

		private void handleCheckFwUpdateResult(final CheckFirmwareUpdateResult result, boolean isLocal) {
			mNewFirmwareVersion = null;
			if(mIsActivityRunning) {
				if (result.isHaveNewFirmwareVersion()) { // new firmware on OTA server
					CommonUtil.setSettingInfo(this, result.getRegID() + "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, true);
					if (isLocal) {
						if ((BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew")) && !"0086".equals(mDevice.getProfile().getModelId()) && !PublicDefine.MODEL_ID_ORBIT.equalsIgnoreCase(mDevice.getProfile().getModelId())) {
							showOptionFWDialog(result);
						} else {
							showOTAUpdateDialog(result);
							//showConfirmFWDialog(result);
						}
					} else {
						if ((BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew")) && !"0086".equals(mDevice.getProfile().getModelId()) && !PublicDefine.MODEL_ID_ORBIT.equalsIgnoreCase(mDevice.getProfile().getModelId())) {
							showRemotelyUpgrade(getSafeString(R.string.fw_upgrade_remotely), result);
						} else {
							showFWDialog(getSafeString(R.string.unable_to_fw_upgrade_remotely));
						}
					}

				} else { // no new firmware on OTA server or OTA version < current version
					CommonUtil.setSettingInfo(this, result.getRegID() + "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, false);
					showFWDialog(String.format(getSafeString(R.string.no_fw_upgrade_found), result.getCurrentFirmwareVersion()));
				}
			}
		}

		/*private void showConfirmFWDialog(final CheckFirmwareUpdateResult result) {
			AlertDialog.Builder builder = new AlertDialog.Builder(CameraSettingsActivity.this);
			Spanned spannedMsg = Html.fromHtml("<big>" + getSafeString(R.string.camera_firmware_upgrade_available) + "</big>");
			builder.setMessage(spannedMsg).setIcon(R.drawable.ic_launcher).setTitle(getSafeString(R.string.updating)).setPositiveButton(getSafeString(R.string.OK), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(@NotNull DialogInterface dialog, int which) {
							dialog.dismiss();
							result.setRequestUpgradeOnly(true);
							showUpdateDialog(result);
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
			}).create().show();
		}*/


		private void showOptionFWDialog(final CheckFirmwareUpdateResult result) {
			AlertDialog.Builder builder = new AlertDialog.Builder(CameraSettingsActivity.this);
			//builder.setIcon(R.drawable.ic_launcher).setTitle(getSafeString(R.string.updating));
			LayoutInflater inflater = CameraSettingsActivity.this.getLayoutInflater();
			final View dialogView = inflater.inflate(R.layout.option_upgrade_dialog, null);
			builder.setView(dialogView);

			final Button btnLocal = (Button) dialogView.findViewById(R.id.btn_upgrade_local);
			final Button btnRemote = (Button) dialogView.findViewById(R.id.btn_upgrade_remote);
			final Button btnCancel = (Button) dialogView.findViewById(R.id.btn_cancel);

			final AlertDialog dialog = builder.create();

			btnLocal.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					dialog.dismiss();
					showOtaInstructionDialog(result);
					//showConfirmFWDialog(result);
				}
			});

			btnRemote.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					dialog.dismiss();
					checkFwRemotely(result.getNewFirmwareFileName());
				}
			});


			btnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					dialog.dismiss();
				}
			});


			dialog.show();
		}

		//	private void showProgressBar() {
//		if (parentView != null && settingsIsVisible) {
//			usingProgressBarCount++;
//			progressBarHolder.setVisibility(View.VISIBLE);
//		}
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				runOnUiThreadIfVisible(new Runnable() {
//					@Override
//					public void run() {
//						if (progressBarHolder.getVisibility() == View.VISIBLE) {
//							Toast.makeText(mActivity, getSafeString(R.string.failed_to_retrieve_camera_data), Toast.LENGTH_SHORT).show();
//							hideProgressBar();
//						}
//					}
//				});
//			}
//		}, 10000);
		//}
		private void showRemotelyUpgrade(String message, final CheckFirmwareUpdateResult result) {
			AlertDialog.Builder builder = new AlertDialog.Builder(CameraSettingsActivity.this);
			Spanned spannedMsg = Html.fromHtml("<big>" + message + "</big>");
			builder.setMessage(spannedMsg).setTitle(getSafeString(R.string.updating)).setPositiveButton(getSafeString(R.string.OK), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(@NotNull DialogInterface dialog, int which) {
							dialog.dismiss();
							mNewFirmwareVersion = result.getNewFirmwareVersion();
							checkFwRemotely(result.getNewFirmwareVersion());
						}
					}
			);
			builder.setNegativeButton(getSafeString(R.string.Cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
				}
			});
            AlertDialog alertDialog = builder.create();
			alertDialog.show();
			Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setTextColor(getResources().getColor(R.color.text_blue));
			Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
			pbutton.setTextColor(getResources().getColor(R.color.text_blue));

		}

		private void requestUpgradeRemotely() {
			final String response = CommandUtils.sendRemoteCommand(mDevice.getProfile().registrationId, "request_fw_upgrade");
			try {
				if (response != null && Integer.parseInt(response.trim().replace("request_fw_upgrade: ", "")) == 0) {

					if (remoteFwDialog != null) {
						remoteFwDialog.dismiss();
					}
					CommonUtil.setSettingInfo(this, mDevice.getProfile().registrationId+ "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, false);
					showFWDialog(getSafeString(R.string.remote_upgrade_success));
					if(mNewFirmwareVersion != null) {
						mDevice.getProfile().setFirmwareVersion(mNewFirmwareVersion);
						if (firmwareVersion != null)
							firmwareVersion.setText(mNewFirmwareVersion);
					}

				} else {

					if (remoteFwDialog != null) {
						remoteFwDialog.dismiss();
					}
					showFWDialog(getSafeString(R.string.upgrade_remotely_fail));

				}
			} catch (Exception ex) {
				ex.printStackTrace();

				if (remoteFwDialog != null) {
					remoteFwDialog.dismiss();
				}
				showFWDialog(getSafeString(R.string.upgrade_remotely_fail));

			}
		}


		private void showFWDialog(String message) {
			AlertDialog.Builder builder = new AlertDialog.Builder(CameraSettingsActivity.this);
			Spanned spannedMsg = Html.fromHtml("<big>" + message + "</big>");
			builder.setMessage(spannedMsg).setTitle(getSafeString(R.string.updating)).setPositiveButton(getSafeString(R.string.OK), new DialogInterface.OnClickListener() {
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
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
			Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setTextColor(getResources().getColor(R.color.text_blue));
			Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
			pbutton.setTextColor(getResources().getColor(R.color.text_blue));
		}

		/*private void showUpdateDialog(CheckFirmwareUpdateResult result) {
			final String newFirmwareVersion = result.getNewFirmwareVersion();
			UpgradeDialog upgradeDialog = new UpgradeDialog(mDevice, result, new IUpgradeCallback() {
				@Override
				public void onUpgradeSucceed() {
					//firmwareVersion.value = newFirmwareVersion;
					mDevice.getProfile().setFirmwareVersion(newFirmwareVersion);
					if (firmwareVersion != null)
						firmwareVersion.setText(newFirmwareVersion);

				}

				@Override
				public void onUpgradeFail() {
				}
			});

			upgradeDialog.show(getSupportFragmentManager(), "upgrade");
			upgradeDialog.setCancelable(false);

			//upgradeDialog.show(mContext, "upgrade");
		}*/

		private void checkFwRemotely(final String newFw) {
			remoteFwDialog = new ProgressDialog(CameraSettingsActivity.this);
			remoteFwDialog.setMessage(getSafeString(R.string.checking_fw_remote_camera));
			remoteFwDialog.setCancelable(false);
			remoteFwDialog.show();
			Looper.getMainLooper();
			new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					final String response = CommandUtils.sendRemoteCommand(mDevice.getProfile().registrationId, "check_fw_upgrade");
					try {
						if (response != null && response.replace("check_fw_upgrade: ", "").equals(newFw)) {
							requestUpgradeRemotely();
						} else {

							if (remoteFwDialog != null) {
								remoteFwDialog.dismiss();
							}
							showFWDialog(getSafeString(R.string.fw_not_available_on_camera));

						}
					} catch (Exception ex) {
						ex.printStackTrace();

						if (remoteFwDialog != null) {
							remoteFwDialog.dismiss();
						}
						showFWDialog(getSafeString(R.string.fw_not_available_on_camera));

					}

					Looper.loop();
				}
			}).start();

		}

		private String cameraLogFileName = "dummy.txt";

		private void showSendCameraLog(boolean isLocal) {
			cameraLogFileName = "dummy.txt";
            Log.i(TAG, "Called showSendCameraLog");
             dismissDialog();

            Log.i(TAG, "Called showSendCameraLog");

            dismissDialog();

			if (isLocal) {
				try {
					long start = System.currentTimeMillis();
					DeviceProfile deviceProfile = mDevice.getProfile();
					SimpleDateFormat sdf = new SimpleDateFormat("DD_MM_yyyy_HH_mm");
					String cameraIP = deviceProfile.getDeviceLocation().getLocalIp();

					String fileName = deviceProfile.getName() + "_" + deviceProfile.getRegistrationId() + "_" + deviceProfile.getFirmwareVersion() + "_"
							+ sdf.format(new Date()) + ".txt";
					Log.d("su", "Get ip address time: " + (System.currentTimeMillis() - start));
					File file = new File(mContext.getExternalFilesDir("camera-log"), fileName);
					String logURL = String.format("http://%s:8080/cgi-bin/logdownload.cgi", cameraIP);
					LogZ.i("Download camera log url: %s", logURL);
					final ProgressDialog downloadFirmwareLogProgressDialog = new ProgressDialog(CameraSettingsActivity.this);
					downloadFirmwareLogProgressDialog.setMessage(getSafeString(R.string.downloading_camera_log));
					downloadFirmwareLogProgressDialog.setCancelable(false);
					downloadFirmwareLogProgressDialog.show();
					Log.d(TAG, "Force Orientation ");
					this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

					Ion.with(this)
							.load(logURL)
							.setLogging("mbp", Log.DEBUG)
							.setTimeout(1000 * 120)
							.progressDialog(downloadFirmwareLogProgressDialog)
							.onHeaders(new HeadersCallback() {
								@Override
								public void onHeaders(HeadersResponse headers) {
									String fileNameContainer = headers.getHeaders().get("content-disposition");
									if (fileNameContainer != null) {
										cameraLogFileName = fileNameContainer.replace("attachment; filename=", "") + ".txt";
									}
								}
							})
							.write(file)
							.setCallback(new FutureCallback<File>() {
								@Override
								public void onCompleted(Exception e, File resultFile) {
									downloadFirmwareLogProgressDialog.dismiss();
									if (e == null) {
										if (resultFile != null) {
											File realFile = new File(mContext.getExternalFilesDir("camera-log"), cameraLogFileName);
											resultFile.renameTo(realFile);
											Uri contentUri;
											String titleEmail = "";
											String bodyEmail = getSafeString(R.string.body_email);
											if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
												contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.vtech.fileprovider", realFile);
												titleEmail = String.format(getSafeString(R.string.title_email), "Vtech", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
											} else if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny")) {
												contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.inanny.fileprovider", realFile);
												titleEmail = String.format(getSafeString(R.string.title_email), "iNanny", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
											} else if (BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
												contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.beurer.fileprovider", realFile);
												titleEmail = String.format(getString(R.string.title_email), "Beurer", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
											} else {
												contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, CommonConstants.FILE_PROVIDER_AUTHORITY_HUBBLE, realFile);
												titleEmail = String.format(getSafeString(R.string.title_email), "Hubble", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
											}

											ArrayList<Uri> uriList = new ArrayList<>();
											uriList.add(contentUri);
											uriList.add(HubbleApplication.AppContext.getAppLogUri());

											Intent sendIntent = new Intent();
											sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
											sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"android.techsupport@hubblehome.com"});
											sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
											sendIntent.putExtra(Intent.EXTRA_SUBJECT, titleEmail);
											sendIntent.putExtra(Intent.EXTRA_TEXT, bodyEmail);
											sendIntent.setType("text/plain");
											startActivity(sendIntent);
										} else {
											LogZ.e("Result file is null", null);
										}
									} else {
										Toast.makeText(getApplicationContext(), R.string.download_camera_log_failed, Toast.LENGTH_SHORT);
									}

									if (this != null) {
										Log.d(TAG, "Reset Orientation ");
										CameraSettingsActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
									}
								}
							});
				} catch (Exception ex) {
					LogZ.e("Error when send device log", ex);
				}

			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(CameraSettingsActivity.this);
				builder.setMessage(R.string.request_camera_log_is_not_support_remotely);
				builder.setNegativeButton(getSafeString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				});
				builder.create().show();
			}
		}


		private class sendCameraLogs extends AsyncTask<Void, Boolean, Boolean> {
			@Override
			protected Boolean doInBackground(Void... params) {

				boolean isLocal = CameraAvailabilityManager.getInstance().isCameraInSameNetwork(getApplicationContext(), mDevice);
				mDevice.setIsAvailableLocally(isLocal);
				return isLocal;
			}

			@Override
			protected void onPostExecute(Boolean isLocal) {
                Log.i(TAG, "sendCameraLogs ONPOST EXECUTE");

                if(mIsActivityRunning) {

                    showSendCameraLog(isLocal);
                }
				super.onPostExecute(isLocal);
			}
		}

		private void getSetOnlineOrbitSettings(final Device selectedDevice)
		{
			selectedDevice.getProfile().setAvailable(true);
			selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
			if(BuildConfig.DEBUG)
				Log.d(TAG, "device online..start settings");
			Toast.makeText(CameraSettingsActivity.this, getResources().getString(R.string.online), Toast.LENGTH_SHORT).show();
			//displayProgressDialog();


											/*GetNotificationStatusTask getNotificationStatus = new GetNotificationStatusTask(mDevice, CameraSettingsActivity.this, mDeviceHandler);
											getNotificationStatus.execute();*/

			//if (!CommonUtil.checkSettings(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS)) {
				getNotificationSettings();
				buildGroupSetting2Codes();
				getSetting2IfAvailable(PublicDefine.SETTING_2_KEY_CEILING_MOUNT);
			//}

			getLensCorrectionSettings(true);
			if( Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)){
				Log.d(TAG,"Fetch recording plan");
				getRecordingPlan();
			}


			if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION)
					&& !Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)) {
				getVideoRecordingDuration(true);
			}


			//displayProgressDialog();
			llCameraSettings.setEnabled(true);
			llGeneralSettings.setEnabled(true);

			txtCemeraSettings.setEnabled(true);
			txtGeneralSettings.setEnabled(true);

			imgCameraSettings.setEnabled(true);
			imgGeneralSettings.setEnabled(true);

			txtCemeraSettings.setTextColor(getResources().getColor(R.color.text_blue));
			txtGeneralSettings.setTextColor(getResources().getColor(R.color.text_blue));

		}
		private void checkDeviceStatus(final Device selectedDevice)
		{

			AsyncPackage.doInBackground(new Runnable()
				{
					@Override
					public void run()
					{
						if (CameraAvailabilityManager.getInstance().isCameraInSameNetwork(mContext, selectedDevice))
						{
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									if(actor != null)
										actor.setDeviceLocal(true);

									getSetOnlineOrbitSettings(selectedDevice);

								}
							});

						}
						else
						{
							final DeviceStatus deviceStatus = new DeviceStatus(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), selectedDevice.getProfile().getRegistrationId());

							DeviceManagerService.getInstance(getApplicationContext()).getDeviceStatus(deviceStatus, new com.android.volley.Response.Listener<StatusDetails>() {
										@Override
										public void onResponse(StatusDetails response)
										{
											if (response != null)
											{
												StatusDetails.StatusResponse[] statusResponseList = response.getDeviceStatusResponse();

												StatusDetails.StatusResponse statusResponse = null;

												if (statusResponseList != null && statusResponseList.length > 0)
												{
													statusResponse = statusResponseList[0]; // fetch first object only
												}

												if (statusResponse != null)
												{
													StatusDetails.DeviceStatusResponse deviceStatusResponse = statusResponse.getDeviceStatusResponse();
													final String deviceStatus = deviceStatusResponse.getDeviceStatus();

													if(BuildConfig.DEBUG)
														Log.d(TAG, "device status :- " + deviceStatus);

													if (deviceStatus != null)
													{
														AsyncPackage.doInBackground(new Runnable() {
																						@Override
																						public void run() {
																							if (actor != null)
																								actor.setDeviceLocal(CameraAvailabilityManager.getInstance().isCameraInSameNetwork(HubbleApplication.AppContext, selectedDevice));
																						}
																					});

														runOnUiThread(new Runnable()
														{
															@Override
															public void run()
															{
																if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0)
																{
																	getSetOnlineOrbitSettings(selectedDevice);

																}
																else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_STANDBY) == 0)
																{
																	selectedDevice.getProfile().setAvailable(false);
																	selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);

																	//wakeup device
																	wakeUpRemoteDevice();
																	if(actor != null)
																		actor.setDeviceLocal(false);

																}
																else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_OFFLINE) == 0)
																{
																	selectedDevice.getProfile().setAvailable(false);
																	selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
																	//device offline
																	Toast.makeText(getApplicationContext(), getString(R.string.camera_offline), Toast.LENGTH_SHORT).show();
																	//Toast.makeText(CameraSettingsActivity.this, getResources().getString(R.string.online), Toast.LENGTH_SHORT).show();

																	//displayProgressDialog();
																	llCameraSettings.setEnabled(false);
																	llGeneralSettings.setEnabled(false);

																	txtCemeraSettings.setEnabled(false);
																	txtGeneralSettings.setEnabled(false);

																	imgCameraSettings.setEnabled(false);
																	imgGeneralSettings.setEnabled(false);

																	txtCemeraSettings.setTextColor(getResources().getColor(R.color.text_gray));
																	txtGeneralSettings.setTextColor(getResources().getColor(R.color.text_gray));

																	if(actor != null)
																		actor.setDeviceLocal(false);
																}
															}
														});
													}
												}
											}
										}
									},
									new com.android.volley.Response.ErrorListener() {
										@Override
										public void onErrorResponse(final VolleyError error) {
											runOnUiThread(new Runnable()
											{
												@Override
												public void run() {
													dismissDialog();
													if (error != null && error.networkResponse != null) {
														Log.d(TAG, error.networkResponse.toString());
														Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
														Toast.makeText(getApplicationContext(), "Unable to reach camera, check your internet connection", Toast.LENGTH_LONG).show();
													}
												}

											});

										}
									});

						}
					}
				});

			Log.d(TAG, "checkDeviceStatus");


		}

		private void wakeUpRemoteDevice() {

			final Device selectedDevice = mDevice;

			if (selectedDevice != null) {
				displayProgressDialog(false);

				DeviceWakeup deviceWakeup = DeviceWakeup.newInstance();
				deviceWakeup.wakeupDevice(selectedDevice.getProfile().registrationId, apiKey, mDeviceHandler, selectedDevice);


			}

		}

		private ProgressDialog mProgressDialog;

		private void displayProgressDialog(boolean isCancelable) {
			try {
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}

				mProgressDialog = new ProgressDialog(CameraSettingsActivity.this);
				mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
				mProgressDialog.setCancelable(isCancelable);
				mProgressDialog.show();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

		private void dismissDialog() {
			try {
				if (mProgressDialog != null && mProgressDialog.isShowing())
					mProgressDialog.dismiss();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private Handler mDeviceHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case CommonConstants.DEVICE_WAKEUP_STATUS:

						boolean result = (boolean) msg.obj;
						final int position = msg.arg1;


						dismissDialog();

						if (CameraSettingsActivity.this != null) {

                            if (result) {
								AsyncPackage.doInBackground(new Runnable() {
									@Override
									public void run() {
										if(actor != null)
											actor.setDeviceLocal(CameraAvailabilityManager.getInstance().isCameraInSameNetwork(HubbleApplication.AppContext, mDevice));

									}
								});

                                Toast.makeText(CameraSettingsActivity.this, getResources().getString(R.string.online), Toast.LENGTH_SHORT).show();
                                getNotificationSettings();
                                buildGroupSetting2Codes();
                                getSetting2IfAvailable(PublicDefine.SETTING_2_KEY_CEILING_MOUNT);

                                getLensCorrectionSettings(true);

	                            if( Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)){
		                            Log.d(TAG,"Fetch recording plan");
		                            getRecordingPlan();
	                            }

                                if (Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(), PublicDefine.ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION)
		                                && !Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(), PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)) {
                                    getVideoRecordingDuration(true);
                                }



								//displayProgressDialog();
								llCameraSettings.setEnabled(true);
								llGeneralSettings.setEnabled(true);

								txtCemeraSettings.setEnabled(true);
								txtGeneralSettings.setEnabled(true);

								imgCameraSettings.setEnabled(true);
								imgGeneralSettings.setEnabled(true);

								txtCemeraSettings.setTextColor(getResources().getColor(R.color.text_blue));
								txtGeneralSettings.setTextColor(getResources().getColor(R.color.text_blue));
							} else {
								llCameraSettings.setEnabled(false);
								llGeneralSettings.setEnabled(false);

								txtCemeraSettings.setEnabled(false);
								txtGeneralSettings.setEnabled(false);

								imgCameraSettings.setEnabled(false);
								imgGeneralSettings.setEnabled(false);

								txtCemeraSettings.setTextColor(getResources().getColor(R.color.text_gray));
								txtGeneralSettings.setTextColor(getResources().getColor(R.color.text_gray));
								Toast.makeText(getApplicationContext(), "Unable to wake up the camera, please check internet connection. Settings will not work. Please try again", Toast.LENGTH_LONG).show();
							}
						}
						break;

					case CommonConstants.DEVICE_NOTIFICATION_STATUS:

						boolean result1 = (boolean) msg.obj;
						final int position1 = msg.arg1;
						dismissDialog();
						if (llCameraDetails != null && llCameraDetails.isShown()) {
							settings();
						} else if (motionLayout != null && motionLayout.isShown()) {
							cameraSettings();
						}

//					if(activity != null)
//					{
//						if(mDevices.get(position1).getProfile().getModelId().equalsIgnoreCase("0080")){
//							if(result1) {
//
//								Toast.makeText(activity, activity.getResources().getString(R.string.pir_enabled), Toast.LENGTH_SHORT).show();
//							}
//							else
//								Toast.makeText(activity, activity.getResources().getString(R.string.pir_disabled), Toast.LENGTH_SHORT).show();
//						}else {
//							if (result1) {
//								Toast.makeText(activity, activity.getResources().getString(R.string.notifications_enabled), Toast.LENGTH_SHORT).show();
//							} else
//								Toast.makeText(activity, activity.getResources().getString(R.string.notifications_disabled), Toast.LENGTH_SHORT).show();
//						}
//						notifyItemChange(position1);
//						notifyDataSetChanged();


//
//					}
						break;

				}
			}
		};


		@Override
		protected void onStop() {

			super.onStop();
            Log.i("ARUNA", "Stop called");
			dismissDialog();
		}

		@Override
		protected void onDestroy() {
			super.onDestroy();
			dismissDialog();
		}

		private void sdcardFormat() {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CameraSettingsActivity.this);
			alertDialogBuilder.setTitle(getResources().getString(R.string.sdcard_format_title));
			alertDialogBuilder.setMessage(getResources().getString(R.string.delete_sdcard_file));


			alertDialogBuilder.setCancelable(true)
					.setPositiveButton(getResources().getString(R.string.dialog_ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									eraseSdcardContent();
								}
							})
					.setNegativeButton(getResources().getString(R.string.dialog_cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							});

			AlertDialog alertDialog = alertDialogBuilder.create();

			alertDialog.show();
		}

		private void eraseSdcardContent() {

			displayProgressDialog(false);


			Runnable sdcardFormatRunnable = new Runnable() {
				@Override
				public void run() {
					boolean isInLocal = false;

					isInLocal = mDevice.isAvailableLocally();

					final String sdFormatRes = CommandUtils.sendCommand(mDevice, "sd_format&value=1", isInLocal);
					final String sdFormatValue;

					if (sdFormatRes != null && sdFormatRes.startsWith("sd_format")) {
						sdFormatValue = sdFormatRes.substring("sd_format".length() + 2);
					} else {
						sdFormatValue = null;
					}

					if (getApplicationContext() != null) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								dismissDialog();
								int returnValue = -1;

								if (sdFormatValue != null) {
									try {
										returnValue = Integer.parseInt(sdFormatValue);
									} catch (NumberFormatException e) {
										e.printStackTrace();
									}

								}
								if (returnValue == 0) {
									Toast.makeText(CameraSettingsActivity.this, getResources().getString(R.string.sdcard_format_success), Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(CameraSettingsActivity.this, getResources().getString(R.string.sdcard_format_failed), Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				}
			};
			Thread worker = new Thread(sdcardFormatRunnable);
			worker.start();

		}

		private void setUpMotionVideoRecording() {
            motionDetection.booleanValue =  CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_STATUS, true);
            motionDetection.secondaryBooleanValue = CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING, true);

            motionSwitch.setOnCheckedChangeListener(null);
            motionSwitch.setChecked(motionDetection.booleanValue);
            motionSwitch.setOnCheckedChangeListener(this);
			if (motionDetection.booleanValue) {
				if (motionDetection.secondaryBooleanValue) {
					String storageMode = mDevice.getProfile().getDeviceAttributes().getStorageMode();
					if(storageMode == null) {
						storageMode = String.valueOf(CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE));
					}
					Log.i(TAG, "Record storage mode: " + storageMode);
					if (storageMode.equalsIgnoreCase("0")) {
						setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_CLOUD);
						setRecordingPlanVisible(false);
					} else {
						setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_SDCARD);
						setRecordingPlanVisible(true);
					}

				} else {
					setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_OFF);
					setRecordingPlanVisible(false);
				}
			} /*else {
				setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_OFF);
			}*/
		}

		private void showMvrWarningDialog() {
			dismissMvrWarningDialogIfAvailable();
			int noSubscriptionNoPlanResId;
			if (HubbleApplication.isVtechApp()) {
				noSubscriptionNoPlanResId = R.string.no_subscription_no_plan;
				//noSubscriptionNoPlanResId = R.string.no_subscription_no_plan_vtech; TODO:Pragnya Should we keeo Vtech
			} else {
				noSubscriptionNoPlanResId = R.string.no_subscription_no_plan_cloud;
			}
			mMvrWarningDialog = HubbleDialogFactory.createAlertDialog(this, Html.fromHtml(getSafeString(noSubscriptionNoPlanResId)),
					null, null, getSafeString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.dismiss();
							/*spinner.setOnItemSelectedListener(null);
							spinner.setSelection(0, false);
							spinner.setOnItemSelectedListener(mListener);*/
						}
					}, false, false);
			try {
				mMvrWarningDialog.show();
				((TextView) mMvrWarningDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
			} catch (Exception e) {
			}
		}

		private void dismissMvrWarningDialogIfAvailable() {
			if (mMvrWarningDialog != null && mMvrWarningDialog.isShowing()) {
				try {
					mMvrWarningDialog.dismiss();
				} catch (Exception e) {
				}
			}
		}

		private boolean hasSubscription() {
			if (BuildConfig.ENABLE_SUBSCRIPTIONS) {
				if (mDevice != null) {
					DeviceProfile profile = mDevice.getProfile();
					if (profile != null) {
						if (profile.getPlanId() != null && !profile.getPlanId().equalsIgnoreCase("freemium")) {
							Log.d(TAG, "Device plan id: " + profile.getPlanId());
							return true;
						} else {
							if (profile.getDeviceFreeTrial() != null && profile.getDeviceFreeTrial().isActive()) {
								Log.d(TAG, "Device plan is free trial and active.");
								return true;
							}
						}
					}
				}
			}
			return false;
		}


		private void setRecordingPlan(String recordingPlanValue) {
            if(mIsActivityRunning)
              mDialog = ProgressDialog.show(CameraSettingsActivity.this, null, getSafeString(R.string.set_record_plan));

			actor.setRecordingPlan(recordingPlanValue, new CameraSettingsActor.MVRListener() {
				@Override
				public void onMVRResponse(boolean success) {
					if (mIsActivityRunning && mDialog != null && mDialog.isShowing()) {
						mDialog.dismiss();
					}
					if (success) {
						Log.d(TAG, "Set recording plan succeeded");
					} else {
						if (mRemoveSdcardClip.isChecked()) {
							mSwitchCloud.setOnClickListener(null);
							mSwitchCloud.setChecked(true);
							mSwitchCloud.setOnClickListener(CameraSettingsActivity.this);
						} else {
							mRemoveSdcardClip.setOnClickListener(null);
							mRemoveSdcardClip.setChecked(true);
							mRemoveSdcardClip.setOnClickListener(CameraSettingsActivity.this);
						}
					}
				}
			});
		}




		private synchronized void getRecordingPlan() {
            /*if (isNotiSettingsVisible)
              mDialog = ProgressDialog.show(CameraSettingsActivity.this, null, getSafeString(R.string.set_record_plan));
*/
			actor.getRecordingPlanValue(new CameraSettingsActor.RecodngPlanListener() {
				@Override
				public void onRecordingPlanResponse(boolean success, int plan) {
					if (isNotiSettingsVisible) {
                      /*  if (mDialog != null && mDialog.isShowing()) {
					mDialog.dismiss();
                        }*/
						if (success) {
							if (plan == 0) {
								mRemoveSdcardClip.setOnClickListener(null);
								mRemoveSdcardClip.setChecked(true);
								mRemoveSdcardClip.setOnClickListener(CameraSettingsActivity.this);
							} else if (plan == 1) {
								mSwitchCloud.setOnClickListener(null);
								mSwitchCloud.setChecked(true);
								mSwitchCloud.setOnClickListener(CameraSettingsActivity.this);
							}
						}/* else {

                            if (mRemoveSdcardClip.isChecked()) {
                                mSwitchCloud.setOnClickListener(null);
                                mSwitchCloud.setChecked(true);
                                mSwitchCloud.setOnClickListener(CameraSettingsActivity.this);
                            } else {
                                mRemoveSdcardClip.setOnClickListener(null);
                                mRemoveSdcardClip.setChecked(true);
                                mRemoveSdcardClip.setOnClickListener(CameraSettingsActivity.this);

                            }
                        }*/

					}
				}
			});
		}


		private void changeMotionDetection() {
			//TODO: Pragnya Enable motionDetectionVDA
		/*int position = motionDetectionVDA.getSelectedItemPosition();
		// if change from MD to OFF -> use old command
		// if change from BSC BSD to OFF -> use new command
		int prevPosition = MD_TYPE_OFF_INDEX;
		String vdaMode = mListener.getMotionDetection().modeVda;
		if (MD_TYPE_MD.equals(vdaMode)) {
			prevPosition = MD_TYPE_MD_INDEX;
		} else if (MD_TYPE_BSC.equals(vdaMode)) {
			prevPosition = MD_TYPE_BSC_INDEX;
		} else if (MD_TYPE_BSD.equals(vdaMode)) {
			prevPosition = MD_TYPE_BSD_INDEX;
		} else if (MD_TYPE_PIR.equals(vdaMode)) {
			prevPosition = MD_TYPE_PIR_INDEX;
		}*/

		/*if (mListener.getDevice().getProfile().getModelId().equals("0877")) {
			// if previous option is BSC and bta time > 0 => show warning dialog BTA is running
			if(mListener.getMotionDetection().modeVda.equals("BSC") && btaRemainingTime > 0) {
				showWarningDialogAboutBTA(position, prevPosition, device);
			} else {
				if(position == MD_TYPE_BSC_INDEX || position == MD_TYPE_BSD_INDEX) {
					applyBSCBSD(position, prevPosition);
				} else {
					handleMD(position, prevPosition, device);
				}
			}
		} else { // handle for 0082*/
			//handleMD(position, prevPosition, device);
			handleMD();
			//}
		}

		private void handleMD(/*final int position, final int prevPosition,*/) {
			motionDetection.setOldCopy();
			if (NotificationSettingUtils.supportMultiMotionTypesPIR(mDevice.getProfile().getModelId(), mDevice.getProfile().getFirmwareVersion())) {
				//motionDetection.booleanValue = position > 0;
			} else {
				motionDetection.booleanValue = motionSwitch.isChecked();
			}
      /*
       * 20160830: Hoang: update secondaryBooleanValue for updating recording storage UI later.
       * The recording storage option is just available for MD mode now.
       * In BSC and BSD mode, app will ignore it.
       */
			motionDetection.secondaryBooleanValue = !mvrOff.isChecked();

			if (motionSentivity1.isChecked()) {
				motionDetection.intValue = 0;
			} else if (motionSentivity2.isChecked()) {
				motionDetection.intValue = 1;
			} else if (motionSentivity3.isChecked()) {
				motionDetection.intValue = 2;
			} else if (motionSentivity4.isChecked()) {
				motionDetection.intValue = 3;
			} else if (motionSentivity2.isChecked()) {
				motionDetection.intValue = 4;
			}

			if (NotificationSettingUtils.supportMultiMotionTypes(mDevice.getProfile().getModelId(), mDevice.getProfile().getFirmwareVersion())
					|| NotificationSettingUtils.supportMultiMotionTypesPIR(mDevice.getProfile().getModelId(), mDevice.getProfile().getFirmwareVersion())) {
				//Log.e(TAG, "set motion detection vda from " + prevPosition + " to " + position);
				//TODO: ImplementVDA
				//mListener.onClickPositiveButtonVDa(position, detectSeekbar.getProgress(), prevPosition);
			} else {
				setMotionDetectionIfAvailable(motionSwitch.isChecked(), motionDetection.intValue);
			/*if (mSchedule != null && mSchedule.isEnable() && !enableMotion) {
				turnOffSchedulingIfNeeded();
			}*/
				if (park != null) {
					if (!motionDetection.booleanValue) {
						park.value = park.booleanValue ? getSafeString(R.string.on) : getSafeString(R.string.off);
					} else {
						park.value = getSafeString(R.string.motion_detection_must_be_off);
					}
				}
			}
		}

		private void setMotionDetectionIfAvailable(final boolean motionEnabled, final int motionLevel) {
			showApplyingDialog();
			setupSoundOrMotionValueField(motionDetection);
			actor.send(new ActorMessage.SetMotionDetection(motionDetection, motionEnabled, motionLevel));
		}


		private void setMotionNotificationIfAvailable(final boolean motionEnabled) {
            motionDetection.value = getSafeString(R.string.motion_detection);
			motionDetection.setOldCopy();
			motionDetection.booleanValue = motionSwitch.isChecked();

			showApplyingDialog();
			setupSoundOrMotionValueField(motionDetection);
			actor.send(new ActorMessage.SetMotionNotification(motionDetection, motionEnabled));
		}

		private void setMotionSensitivityIfAvailable() {
			showApplyingDialog();

			motionDetection.setOldCopy();
			motionDetection.booleanValue = motionSwitch.isChecked();
			if (motionSentivity1.isChecked()) {
				motionDetection.intValue = 0;
			} else if (motionSentivity2.isChecked()) {
				motionDetection.intValue = 1;
			} else if (motionSentivity3.isChecked()) {
				motionDetection.intValue = 2;
			} else if (motionSentivity4.isChecked()) {
				motionDetection.intValue = 3;
			} else if (motionSentivity5.isChecked()) {
				motionDetection.intValue = 4;
			}

			setupSoundOrMotionValueField(motionDetection);
			actor.send(new ActorMessage.SetMotionSentivity(motionDetection, motionDetection.intValue));
		}

		private void setNoNetworkDialogVisible(boolean isVisible) {
			if (mNoNetworkDialog == null) {
				String msg = getApplicationContext().getString(R.string.dialog_no_network_enabled);
				mNoNetworkDialog = HubbleDialogFactory.createAlertDialog(CameraSettingsActivity.this, msg, getApplicationContext().getString(R.string.OK), null, null, null, false, false);
			}

			if (isVisible) {
				if (mNoNetworkDialog != null && !mNoNetworkDialog.isShowing()) {
					try {
						mNoNetworkDialog.show();
					} catch (Exception e) {
					}
				}
			} else {
				if (mNoNetworkDialog != null && mNoNetworkDialog.isShowing()) {
					try {
						mNoNetworkDialog.dismiss();
					} catch (Exception e) {
					}
				}
			}

		}

        public String getPath(Uri uri) {
            // just some safety built in
            if (uri == null) {
                // TODO perform some logging or show user feedback
                return null;
            }
            // try to retrieve the image from the media store first
            // this will only work for images selected from gallery
            String[] projection = {MediaStore.Images.Media.DATA};
            CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
            Cursor cursor = loader.loadInBackground();
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
            // this is our fallback here
            return uri.getPath();
        }

    /**
     * Takes a file uri or a bitmap and uploads it to our server.
     *
     * @param uriPath the uri path to the local image on the device to upload to the server
     * @param bitmap  the bitmap image to upload to the server
     */
    private void uploadImage(final String uriPath, final Bitmap bitmap) {
        final ProgressDialog progressDialog = new ProgressDialog(CameraSettingsActivity.this);
        progressDialog.setMessage(getString(R.string.change_camera_snapshot));
        progressDialog.setCancelable(true);
        progressDialog.show();
        AsyncPackage.doInBackground(new Runnable() {
            @Override
            public void run() {
                if (apiKey != null && mDevice.getProfile().getRegistrationId() != null) {
                    Bitmap localBitmap = null;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    if (uriPath != null) {
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(uriPath, options);
                        options.inSampleSize = calculateInSampleSize(options, 600);
                        options.inJustDecodeBounds = false; // Decode currentCameraImage with inSampleSize set
                        localBitmap = BitmapFactory.decodeFile(uriPath, options);
                    } else if (bitmap != null) {
                        localBitmap = bitmap;
                    } else {
                        localBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_cam);
                    }

                    try {
                        if (localBitmap != null) {
                            localBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
                            UploadTokenResponse res = User.getUserUploadToken(apiKey);
                            if (res != null && res.getData() != null && res.getData().getUpload_token() != null) {
                                Upload.uploadFile(getApplicationContext(), inputStream, apiKey,
                                        res.getData().getUpload_token(), mDevice.getProfile().getRegistrationId(),
                                        new Upload.IUpload() {
                                            @Override
                                            public void onComplete(final int code) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (code != 200) {
                                                            Log.w(TAG, "Change image failed.");
                                                            if (getApplicationContext() != null) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getApplicationContext(), R.string.change_image_failed, Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            Log.d(TAG, "Change image successful.");
                                                            if (getApplicationContext() != null) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getApplicationContext(), R.string.change_image_successful, Toast.LENGTH_SHORT).show();
                                                                /*SharedPreferences.Editor editor = settings.edit();
                                                                editor.putString("camera_avatar_changed", device.getProfile().getRegistrationId());
                                                                editor.apply();*/
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }); // TODO: modify upload server address
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                });
                                Toast.makeText(getApplicationContext(), getSafeString(R.string.unable_to_update_image), Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), getSafeString(R.string.unable_to_update_image), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            }
        });

    }


   /* private void uploadImage(final String uriPath, final Bitmap bitmap) {
        final ProgressDialog progressDialog = new ProgressDialog(CameraSettingsActivity.this);
        progressDialog.setMessage("Changing camera snapshot...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        AsyncPackage.doInBackground(new Runnable() {
            @Override
            public void run() {
                if (apiKey != null && mDevice.getProfile().getRegistrationId() != null) {
                    Bitmap localBitmap = null;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    if (uriPath != null) {
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(uriPath, options);
                        options.inSampleSize = calculateInSampleSize(options, 600);
                        options.inJustDecodeBounds = false; // Decode currentCameraImage with inSampleSize set
                        localBitmap = BitmapFactory.decodeFile(uriPath, options);
                    } else if (bitmap != null) {
                        localBitmap = bitmap;
                    } else {
                        localBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_cam);
                    }

                    try {
                        if (localBitmap != null) {
                            FileOutputStream outputStream = HubbleApplication.AppContext.openFileOutput("current_image.png", 0);
                            Bitmap newBitmap = scaleDown(localBitmap, 1024, true);
                            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            outputStream.close();
                            File file = new File(HubbleApplication.AppContext.getFilesDir(), "current_image.png");
                            Log.i(TAG ,"Camera icon file size: " + file.length());
                            Models.CameraIconInitialUploadBodyRequest bodyRequest = new Models.CameraIconInitialUploadBodyRequest(mDevice.getProfile().getRegistrationId());
                            Models.ApiResponse<Models.CameraIconInitialUploadResponse> response = Api.getInstance().getService().getCameraIconUploadUrl(apiKey, bodyRequest);
                            if (response != null && response.getData() != null && response.getData().url != null) {
                                Ion.with(HubbleApplication.AppContext)
                                        .load("PUT", response.getData().url)
                                        .setLogging("S3Upload", Log.DEBUG)
                                        .setHeader("Content-Type", "image/png")
                                        .setHeader("x-amz-server-side-encryption", "AES256")
                                        .setHeader("Content-Length", file.length() +"")
                                        .setFileBody(file)
                                        .asString()
                                        .setCallback(new FutureCallback<String>() {
                                            @Override
                                            public void onCompleted(Exception e, final String result) {
                                                if (e == null) {
                                                    runOnUiThreadIfVisible(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Log.d(TAG, "Change image successful." + result);
                                                            if (getActivity() != null) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(getActivity(), R.string.change_image_successful, Toast.LENGTH_SHORT).show();
                                                                SharedPreferences.Editor editor = settings.edit();
                                                                editor.putString("camera_avatar_changed", device.getProfile().getRegistrationId());
                                                                editor.apply();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    runOnUiThreadIfVisible(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(mActivity, getSafeString(R.string.unable_to_update_image), Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                            } else {
                                runOnUiThreadIfVisible(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                });
                                Toast.makeText(mActivity, getSafeString(R.string.unable_to_update_image), Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(mActivity, getSafeString(R.string.unable_to_update_image), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }*/
        private int calculateInSampleSize(BitmapFactory.Options options, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            // final int width = options.outWidth;
            int inSampleSize = 1;
            if (height > reqHeight) {

                final int halfHeight = height / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and
                // keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                       boolean filter) {
            float ratio = Math.min(
                    (float) maxImageSize / realImage.getWidth(),
                    (float) maxImageSize / realImage.getHeight());
            int width = Math.round((float) ratio * realImage.getWidth());
            int height = Math.round((float) ratio * realImage.getHeight());

            Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                    height, filter);
            return newBitmap;
        }

        public void galleryAddPic() {
            if (photoFile != null) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(photoFile);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
            }
        }


        // Details Settings
        private void showChangeImageDialog() {
            String[] values = new String[]{getSafeString(R.string.select_image_from_gallery), getSafeString(R.string.take_photo), getSafeString(R.string.clear_image)};
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.camera_image_select_dailog_item);
            arrayAdapter.addAll(values);

            mAlertDialog = new AlertDialog.Builder(new ContextThemeWrapper(CameraSettingsActivity.this, R.style.PauseDialog))
                    .setTitle(getSafeString(R.string.camera_snapshot))

                    .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    Intent fromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(fromGalleryIntent, FROM_GALLERY);
                                    break;
                                case 1:
                                    dispatchTakePictureIntent();
                                    break;

                                case 2:
                                    mSecondaryAlertDialog = new AlertDialog.Builder(CameraSettingsActivity.this).setMessage(getSafeString(R.string.are_you_sure))
                                            .setTitle(getSafeString(R.string.clear_device_image))
                                            .setPositiveButton(getSafeString(R.string.yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    uploadImage(null, BitmapFactory.decodeResource(getResources(), R.drawable.default_cam));
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setNegativeButton(getSafeString(R.string.no), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                    break;
                            }
                        }
                    })
                    .setNegativeButton(getSafeString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            mAlertDialog.getWindow().setTitleColor(getResources().getColor(R.color.text_blue));
        }

        private void dispatchTakePictureIntent() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(getApplicationContext(), getSafeString(R.string.unable_to_access_device_storage), Toast.LENGTH_SHORT).show();
                }
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, FROM_CLEAR_IMAGE);
                }
            } else {
                Toast.makeText(getApplicationContext(), getSafeString(R.string.no_device_camera_detected), Toast.LENGTH_SHORT).show();
            }
        }

        private File createImageFile() throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            return image;
        }

      /*  private void showTakeCameraSnapshotDialog() {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.dialog_camera_snapshot, (ViewGroup) mActivity.findViewById(R.id.dialog_camera_snapshot_root));
            final ImageView snapshotCameraImageView = (ImageView) layout.findViewById(R.id.dialog_camera_snapshot_imageView);
            final ProgressBar snapshotProgressBar = (ProgressBar) layout.findViewById(R.id.dialog_camera_snapshot_progress);
            final TextView snapshotTextView = (TextView) layout.findViewById(R.id.dialog_camera_snapshot_textView);
            Button snapshotButtonRefresh = (Button) layout.findViewById(R.id.dialog_camera_snapshot_buttonRefresh);
            Button snapshotButtonOk = (Button) layout.findViewById(R.id.dialog_camera_snapshot_buttonOk);

            snapshotTextView.setText(getSafeString(R.string.press_refresh_to_update_image));

            final AlertDialog dialog = new AlertDialog.Builder(mActivity).
                    setTitle(getSafeString(R.string.take_a_snapshot_now)).
                    setView(layout).show();

            snapshotButtonRefresh.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    loadImage(snapshotCameraImageView, snapshotProgressBar, snapshotTextView);
                }
            });

            snapshotButtonOk.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                }
            });

            snapshotProgressBar.setVisibility(View.INVISIBLE);
            snapshotCameraImageView.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(device.getProfile().getSnapshotUrl(), snapshotCameraImageView);
        }*/
     /* private void populateAndUpdateTimeZone() {

          //populate spinner with all timezones
          Spinner mSpinner = (Spinner) findViewById(R.id.ss);
          String[] idArray = TimeZone.getAvailableIDs();
          ArrayAdapter idAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                  idArray);
          idAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          mSpinner.setAdapter(idAdapter);

          // now set the spinner to default timezone from the time zone settings
          for(int i = 0; i < idAdapter.getCount(); i++) {
              if(idAdapter.getItem(i).equals(TimeZone.getDefault().getID())) {
                  mSpinner.setSelection(i);
              }
          }*/




        // Timezone
        private void showTimezoneDialog() {

            final String[] values = getSafeStringArray(R.array.timezones);
            if (timezone.intValue == -1) {
                for (int pos = 0; pos < values.length; pos++) {
                    String aTimezone = values[pos];
                    if (aTimezone.equals(timezone.value)) {
                        timezone.intValue = pos;
                    }
                }
            }

            mAlertDialog = new AlertDialog.Builder(CameraSettingsActivity.this).
                    setTitle(getSafeString(R.string.timezone)).
                    setSingleChoiceItems(values, timezone.intValue, null).setPositiveButton(getSafeString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timezone.setOldCopy();
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    if (selectedPosition == -1) {
                        selectedPosition = 0;
                    }
                    String selectedItem = values[selectedPosition];
                    timezone.intValue = selectedPosition;
                    timezone.value = selectedItem;
                    currentTimeZone.setText(timezone.value);
                    setTimezoneIfAvailable(selectedItem);

                  /*  String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                   ChangeTimeZoneTask timezoneChange = new ChangeTimeZoneTask(getApplicationContext()
                            , CameraSettingsActivity.this);
                    timezoneChange.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, saved_token, timezone.value, mDevice.getProfile().getRegistrationId());
*/
                    //listAdapter.notifyDataSetChanged();
                }
            }).setNegativeButton(getSafeString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }

        private void setTimezoneIfAvailable(final String timezoneString) {
            showApplyingDialog();
            actor.send(new ActorMessage.SetTimeZone(timezone, timezoneString));
        }

        private void getTimeZoneIfAvailable() {
            if (timezone != null && shouldRefreshListChildValues(timezone)) {
                if (!CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.TIMEZONE)) {
                    timezone.value = getSafeString(R.string.loading);
                    currentTimeZone.setText(timezone.value);
                } else {

                    timezone.value = CommonUtil.getStringValue(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.TIMEZONE);
                    currentTimeZone.setText(timezone.value);

                }
                actor.send(new ActorMessage.GetTimeZone(timezone));
            }
        }

	    private void setScheduleHolderVisibility(int state) {
		    if (!BuildConfig.ENABLE_MVR_SCHEDULING) {
			    schedulerLayout.setVisibility(View.GONE);
			    return;
		    }
		    if (state != View.VISIBLE) {
			    schedulerLayout.setVisibility(View.GONE);
		    } else {
			    // check firmware version
			    if (mDevice.getProfile().isSupportMvrScheduling()) {
				    schedulerLayout.setVisibility(View.VISIBLE);
			    } else {
				    schedulerLayout.setVisibility(View.GONE);
			    }
		    }
		    if (schedulerLayout.getVisibility() == View.VISIBLE) {
			    //Todo : updateOnOff text
		    }
	    }


	    private boolean isLoadingMvrSchedule = false;

	    public void setScheduleData() {
		    if (!BuildConfig.ENABLE_MVR_SCHEDULING) {
			    return;
		    }
            if(isNotiSettingsVisible) {
                schedulerSwitch.setOnCheckedChangeListener(null);
                mScheduleData = CommonUtil.getSettingSchedule(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MVR_SCHEDULE);
                schedulerLayout.setVisibility(View.VISIBLE);
                if (mScheduleData != null && mScheduleData.isEnable()) {
                    schedulerSwitch.setChecked(true);
                } else {
                    schedulerSwitch.setChecked(false);
                }
	            updateMvrTextView();
                schedulerSwitch.setOnCheckedChangeListener(CameraSettingsActivity.this);
            }
	    }


	    private void handleScheduleSwitch(boolean ischecked){
		    if (mScheduleData == null) {
			    return;
		    }
		    turnSchedulingOnOff(ischecked, new TurnScheduleTask.TurnScheduleListener() {
			    @Override
			    public void onComplete(Models.ApiResponse<String> res) {
				    // reset to previous selection if failed
				    if (res == null || !res.getStatus().equalsIgnoreCase("200")) {
					    CommonUtil.setSettingSchedule(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MVR_SCHEDULE, mScheduleData);
					    mScheduleData.setEnable(!mScheduleData.isEnable());
				    }
				    if(mContext != null) {
					    runOnUiThread(new Runnable() {
						    @Override
						    public void run() {
                                if(mDialog != null && mDialog.isShowing())
							         mDialog.dismiss();
							    updateMvrTextView();
						    }
					    });
				    }
			    }
		    });
	    }

	    private void turnSchedulingOnOff(boolean enable, TurnScheduleTask.TurnScheduleListener listener) {
		    String deviceId = mDevice.getProfile().registrationId;
		    mScheduleData.setEnable(enable);

		    Models.DeviceScheduleSubmit obj = new Models.DeviceScheduleSubmit();
		    obj.setRegistrationId(deviceId);
		    obj.setDeviceSchedule(mScheduleData);

		    if (mDialog == null) {
			    mDialog = new ProgressDialog(mContext);
			    mDialog.setCancelable(false);
		    }
            if(mIsActivityRunning) {
                if (enable) {
                    mDialog = ProgressDialog.show(CameraSettingsActivity.this, null, getSafeString(R.string.enabling_motion_video_scheduling));
                } else {
                    mDialog = ProgressDialog.show(CameraSettingsActivity.this, null, getSafeString(R.string.disabling_motion_video_scheduling));
                }
                mDialog.show();
            }
		    new Thread(new TurnScheduleTask(mContext, obj, listener)).start();
	    }



	    private void updateMvrTextView() {
		    if (mScheduleData == null || !mScheduleData.isEnable()) {
			    schedulerCurrentText.setVisibility(View.GONE);
			    schedulerNextText.setVisibility(View.GONE);
		    } else {
			    schedulerNextText.setVisibility(View.VISIBLE);
			    schedulerNextText.setText(getNextMvrSchedule());
			    String currentSchedule = getCurrentMvrSchedule();
			    if (TextUtils.isEmpty(currentSchedule)) {
				    schedulerCurrentText.setVisibility(View.GONE);
			    } else {
				    schedulerCurrentText.setVisibility(View.VISIBLE);
				    schedulerCurrentText.setText(currentSchedule);
			    }
		    }
	    }

	    public String getNextMvrSchedule() {
		    if (mScheduleData != null && mScheduleData.isEnable() && scheduleHasElements()) {
			    HashMap<String, ArrayList<String>> scheduleData = mScheduleData.getScheduleData();

			    Calendar calendar = Calendar.getInstance();
			    int keyPosition = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			    int now = Integer.parseInt(new SimpleDateFormat("HHmm").format(new Date()));
			    // if it is 9:10 AM, now will be 910
			    now = keyPosition * 10000 + now;
			    // if to day is wednesday, now will become 30910

			    // find from today to saturday
			    for (int i = keyPosition; i < PublicDefine.KEYS.length; i++) {
				    if (!scheduleData.containsKey(PublicDefine.KEYS[i])) {
					    continue;
				    }
				    String[] times = findNextMvrSchedule(now, i, scheduleData.get(PublicDefine.KEYS[i]));
				    if (times == null) {
					    continue;
				    }
				    Log.i("debug", "Next MVR: " + PublicDefine.KEYS[i] + " " + times[0] + " -> " + times[1]);
				    String capitalDay = Util.getStringByName(mContext, PublicDefine.KEYS[i]);
				    return String.format(getSafeString(R.string.next_mvr_scheduling_for), capitalDay, times[0], times[1]);
			    }

			    // find from Sunday to yesterday
			    for (int i = 0; i < keyPosition; i++) {
				    if (!scheduleData.containsKey(PublicDefine.KEYS[i])) {
					    continue;
				    }
				    String[] times = findNextMvrSchedule(now, i + 7, scheduleData.get(PublicDefine.KEYS[i]));
				    if (times == null) {
					    continue;
				    }
				    Log.i("debug", "Next MVR: " + PublicDefine.KEYS[i] + " " + times[0] + " -> " + times[1]);
				    String capitalDay = Util.getStringByName(mContext, PublicDefine.KEYS[i]);
				    return String.format(getSafeString(R.string.next_mvr_scheduling_for), capitalDay, times[0], times[1]);
			    }

			    // there is no next schedule
			    String day = Util.getStringByName(mContext, PublicDefine.KEYS[keyPosition]);
			    return String.format(getSafeString(R.string.next_mvr_scheduling_no), day);
		    }
		    Log.i("debug", "Next MVR: no existing");
		    return getSafeString(R.string.always_detecting_mvr_events);
	    }

	    private String[] findNextMvrSchedule(int now, int plusForDay, ArrayList<String> arrTemp) {
		    for (String temp : arrTemp) {
			    String[] split = temp.split("-");
			    int from = Integer.parseInt(split[0]) + plusForDay * 10000;
			    int to = Integer.parseInt(split[1]) + plusForDay * 10000;
			    if (now < from && now < to) {
				    return new String[]{getTimeDisplayString(split[0]), getTimeDisplayString(split[1])};
			    }
		    }
		    return null;
	    }

	    private String getCurrentMvrSchedule() {
		    if (mScheduleData != null && mScheduleData.isEnable() && scheduleHasElements()) {
			    HashMap<String, ArrayList<String>> scheduleData = mScheduleData.getScheduleData();

			    Calendar calendar = Calendar.getInstance();
			    int keyPosition = calendar.get(Calendar.DAY_OF_WEEK) - 1;
			    int now = Integer.parseInt(new SimpleDateFormat("HHmm").format(new Date()));
			    // if it is 9:10 AM, now will be 910

			    if (scheduleData.containsKey(PublicDefine.KEYS[keyPosition])) {
				    ArrayList<String> arrTemp = scheduleData.get(PublicDefine.KEYS[keyPosition]);
				    for (String temp : arrTemp) {
					    String[] split = temp.split("-");
					    int from = Integer.parseInt(split[0]);
					    int to = Integer.parseInt(split[1]);
					    if (now >= from && now < to) {
						    String capitalDay = Util.getStringByName(mContext, PublicDefine.KEYS[keyPosition]);
						    String pattern = getSafeString(R.string.current_mvr_scheduling_for);
						    return String.format(pattern, capitalDay, getTimeDisplayString(split[0]), getTimeDisplayString(split[1]));
					    }
				    }
			    }
			    String capitalDay = Util.getStringByName(mContext, PublicDefine.KEYS[keyPosition]);
			    return String.format(getSafeString(R.string.current_mvr_scheduling_no), capitalDay);
		    }
		    return null;
	    }

	    private boolean scheduleHasElements() {
		    boolean result = false;
		    if (mScheduleData != null && mScheduleData.isEnable()) {
			    HashMap<String, ArrayList<String>> scheduleData = mScheduleData.getScheduleData();
			    for (String day : PublicDefine.KEYS) {
				    if (scheduleData.containsKey(day)) {
					    ArrayList<String> elements = scheduleData.get(day);
					    if (elements != null && elements.size() > 0) {
						    result = true;
						    break;
					    }
				    }
			    }
		    }
		    return result;
	    }



	    private String getTimeDisplayString(String time) {
		    int hour = Integer.parseInt(time.substring(0, 2));
		    int minute = Integer.parseInt(time.substring(2));
		    int timeFormat = settings.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0);
		    String temp = "";
		    if (timeFormat == 0) {
			    temp = hour >= 12 ? getSafeString(R.string.half_day_pm) : getSafeString(R.string.half_day_am);
			    if (hour > 12) {
				    hour = hour - 12;
			    }
		    } else {
			    temp = "";
		    }

		    return String.format("%d:%02d %s", hour, minute, temp);
	    }

        private void applyBSCBSD(final int position, final int prevPosition) {
            final ProgressDialog tempDialog = new ProgressDialog(CameraSettingsActivity.this);
            tempDialog.setCancelable(false);
            tempDialog.setMessage(getSafeString(R.string.applying));
            tempDialog.show();
            // check license expire
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String cmdValue = position == 2 ? "start_vda&value=bsc" : "start_vda&value=bsd";
                    // Pair<String, Object> temp = mDevice.getDevice().sendCommandGetValue(cmdValue, null, null);
                    DeviceManager mDeviceManager;

                    mDeviceManager = DeviceManager.getInstance(mContext);
                    SecureConfig settings = HubbleApplication.AppConfig;
                    String regId = mDevice.getProfile().getRegistrationId();
                    SendCommand mdType = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, cmdValue);

                    mDeviceManager.sendCommandRequest(mdType, new com.android.volley.Response.Listener<SendCommandDetails>() {

                                @Override
                                public void onResponse(SendCommandDetails response1) {
                                    String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                                    Log.i(TAG, "SERVER RESP : " + responsebody);
                                    // if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_adaptive_bitrate")) {

                                    try {
                                        Pair<String, Object> temp = CommonUtil.parseResponseBody(responsebody);
                                        if (temp != null && temp.second != null && (temp.second instanceof Float)) {
                                            Integer checkVdaResult = (Integer) ((Float) temp.second).intValue();
                                            Log.d(TAG, "Check VDA cmd: " + temp.first + ", result: " + checkVdaResult);
           /* AA-2087
              start_vda&value=bsc/bsd
             -1: wrong command format
             -2: could not get license from server
             -3: bta is running
             -4: internal executing vda cmd error
              1: invalid license
              2: fresh camera, need to check local license file
              3: not fresh camera, get license file is in progress
              0: SUCCESS
            */
                                            if (checkVdaResult == -1 && "start_vda".equalsIgnoreCase(temp.first)) {
            /*
             * 20170504 HOANG AA-2495
             * When camera doesn't response or command timeout, sendCommandGetValue will return Pair("error", -1);
             * So need to check first() value as well.
             */
                                                showWarningDialog(prevPosition, R.string.wrong_command_format);
                                            } else if (checkVdaResult == -2) {
                                                showWarningDialog(prevPosition, R.string.could_not_get_license_file_from_server);
                                            } else if (checkVdaResult == -3) {
                                                showWarningDialogAboutBTA(position, prevPosition, mDevice);
                                            } else if (checkVdaResult == -4) {
                                                showWarningDialog(prevPosition, R.string.internal_memory_error_please_restart_the_camera_and_retry);
                                            } else if (checkVdaResult == 1) {
                                                showWarningDialog(prevPosition, R.string.your_license_registration_is_not_correctly_set);
                                            } else if (checkVdaResult == 2) {
                                                if (position == 2) {
                                                    showWarningDialog(prevPosition, R.string.camera_is_downloading_the_sleep_analytics_license_file);
                                                } else {
                                                    showWarningDialog(prevPosition, R.string.camera_is_downloading_the_expression_detection_file);
                                                }
                                            } else if (checkVdaResult == 3) {
                                                if (position == 2) {
                                                    showWarningDialog(prevPosition, R.string.camera_is_downloading_the_sleep_analytics_license_file);
                                                } else {
                                                    showWarningDialog(prevPosition, R.string.camera_is_downloading_the_expression_detection_file);
                                                }
                                            } else {
                                                CameraSettingsActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        motionDetection.setOldCopy();
                                                        motionDetection.booleanValue = motionSwitch.isChecked();
                                                        //motionDetection.intValue = motion.getProgress(); //ARUNA commented recheck

                                                        Device tempDevice = mDevice;
                                                        if (NotificationSettingUtils.supportMultiMotionTypes(tempDevice.getProfile().getModelId(),
                                                                tempDevice.getProfile().getFirmwareVersion())) {
                                                            Log.e(TAG, "set motion detection vda from " + prevPosition + " to " + position);
                                                            setMotionDetectionVda(position, motionDetection.intValue, prevPosition);
                                                        } else {
                                                            //mListener.onClickPositiveButton(detectSwitch.isChecked(), detectSeekbar.getProgress());
                                                            handleMotionDetectionSwitch(motionSwitch.isChecked(), false);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    } catch (Exception ex) {
                                        // mInterface.onCompleted(command, response, ex);
                                    }
                                    // }
                                }
                            }, new com.android.volley.Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    if (error != null && error.networkResponse != null) {
                                        Log.d(TAG, error.networkResponse.toString());
                                        Log.d(TAG, error.networkResponse.data.toString());

                                    }

                                }
                            }


                    );
                    try {
                        tempDialog.dismiss();
                    } catch (Exception e) {
                    }


                }
            }).start();
        }



        /**
         * show warning dialog and reserve motion setting position
         * @param prevPosition previous motion setting
         * @param msgId string resource id
         */
        private void showWarningDialog(final int prevPosition, int msgId) {
            final String msg = getSafeString(msgId);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   // motionDetectionVDA.setSelection(prevPosition);
                    AlertDialog dialog = new AlertDialog.Builder(CameraSettingsActivity.this).setTitle(R.string.setting_change_failed)
                            .setMessage(Html.fromHtml(msg)).setPositiveButton(R.string.dialog_ok, null).show();
                    ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                }
            });
        }

        private void setMotionDetectionVda(int position, int motionLevel, int prevPosition){
            showApplyingDialog();
            setupMotionVda(motionDetection, position);
            actor.send(new ActorMessage.SetMotionDetectionVda(motionDetection, position, motionLevel, prevPosition));
        }


        private BTATask task;
        ProgressDialog progressDialog = null;
        int mdTypePossition  = 0;
        int mdTypePreviousPossition = 0;


        private void showWarningDialogAboutBTA(final int position, final int prevPosition, final Device device) {
                    mdTypePossition = position;
                    mdTypePreviousPossition = prevPosition;
                    AlertDialog.Builder builder = new AlertDialog.Builder(CameraSettingsActivity.this);
                    builder.setMessage(R.string.warning);
                    builder.setMessage(R.string.bta_will_disable_if_you_choose_detect_motion_mode_is_md_off);
                    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == AlertDialog.BUTTON_POSITIVE) {
                                progressDialog = new ProgressDialog(CameraSettingsActivity.this);
                                progressDialog.setMessage(mContext.getString(R.string.disable_bta));
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                task.stopBTA();

                            } else {
                                switch (prevPosition) {
                                    case 0:
                                    mdTypeNone.setChecked(true);
                                        break;
                                    case 1:
                                        mdTypeRegular.setChecked(true);
                                        break;
                                    case 2:
                                        mdTypeSleepAnalytics.setChecked(true);
                                        break;
                                    case 3: mdTypeExpression.setChecked(true);
                                        break;
                                    default:
                                        mdTypeRegular.setChecked(true);
                                        break;
                                }
                                dialog.dismiss();
                            }
                        }
                    };
                    builder.setNegativeButton(R.string.cancel, onClickListener);
                    builder.setPositiveButton(R.string.ok, onClickListener);
                    builder.show();

        }


        private void getBTARemainingTimeIfAvailable() {
            if (mDevice != null && mDevice.getProfile().getRegistrationId().startsWith("010877")) {

            DeviceManager mDeviceManager = DeviceManager.getInstance(mContext);
            String regId = null;
            settings = HubbleApplication.AppConfig;
                regId = mDevice.getProfile().getRegistrationId();

            SendCommand setMotionvda = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_bsc_remain_duration");

            mDeviceManager.sendCommandRequest(setMotionvda, new com.android.volley.Response.Listener<SendCommandDetails>() {

                        @Override
                        public void onResponse(SendCommandDetails response1) {
                            String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                            Log.i(TAG, "SERVER RESP : " + responsebody);
                            int setStatus = response1.getStatus();
                            if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_bsc_remain_duration")) {

                                try {
                                    final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                    remainingBTATime = ((Float)response.second).intValue();

                                } catch (Exception ex) {

                                }
                            }
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }

                        }
                    }

            );

            }

        }


        BTATask.BTAInterface mBTAInterface = new BTATask.BTAInterface() {
            // ANU
            @Override
            public void onCompleted(String command, Pair<String, Object> result, Exception e) {

                {
                    if (command.equalsIgnoreCase("set_bsc_bed_time&start_time=NA&duration=0")) {
                        if(progressDialog != null && progressDialog.isShowing())
                             progressDialog.dismiss();
                        if(mdTypePossition == 2){
                            setMotionNotificationIfAvailable(motionSwitch.isChecked());
                        }else if(mdTypePossition == 3) {
                            applyBSCBSD(mdTypePossition, mdTypePreviousPossition);
                        } else {
                            //handleMD(mdTypePossition, mdTypePreviousPossition, mDevice);
                            setMotionDetectionVda(mdTypePossition, motionDetection.intValue, mdTypePreviousPossition);
                        }
                    }
                }

            }
        };

        private void showWarningViewModeChange() {
            mAlertDialog = new AlertDialog.Builder(CameraSettingsActivity.this).setMessage(R.string.switching_view_mode_message)
                    .setPositiveButton(R.string.ccontinue, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (viewMode.intValue >= 0 && viewMode.oldIntValue != viewMode.intValue) {
                               // showPickViewModeDialog();
                                setViewMode();
                            }
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            viewMode.intValue = viewMode.oldIntValue;
                            wide.setOnClickListener(null);
                            narrow.setOnClickListener(null);
                            if(viewMode.intValue == 0){

                                wide.setChecked(true);
                                narrow.setChecked(false);
                            }else{

                                narrow.setChecked(true);
                                wide.setChecked(false);
                            }
                            wide.setOnClickListener(CameraSettingsActivity.this);
                            narrow.setOnClickListener(CameraSettingsActivity.this);
                            mAlertDialog.dismiss();
                        }
                    }).show();
        }

        private void setViewMode() {
            if (viewMode != null) {
                showApplyingDialog();
                actor.send(new ActorMessage.SetViewMode(viewMode, qualityOfService));
            }
        }


	    private void showOTAUpdateDialog(final CheckFirmwareUpdateResult result) {
			    AlertDialog.Builder builder = new AlertDialog.Builder(this);

			    builder.setTitle(getString(R.string.ota_update_available));
			    builder.setMessage(getString(R.string.ota_update_available_msg));
			    builder.setCancelable(false);
			    builder.setPositiveButton(getResources().getString(R.string.update_now),
					    new DialogInterface.OnClickListener() {
						    @Override
						    public void onClick(@NotNull DialogInterface dialog, int which) {
							    dialog.dismiss();
							    showOtaInstructionDialog(result);
						    }
					    });
			    builder.setNegativeButton(getResources().getString(R.string.later),
					    new DialogInterface.OnClickListener() {
						    @Override
						    public void onClick(@NotNull DialogInterface dialog, int which) {
							    dialog.dismiss();

						    }
					    });
			    AlertDialog alertDialog = builder.create();
			    alertDialog.show();
			    Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			    nbutton.setTextColor(getResources().getColor(R.color.text_blue));
			    Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
			    pbutton.setTextColor(getResources().getColor(R.color.text_blue));
	    }

	    private void showOtaInstructionDialog(final CheckFirmwareUpdateResult result) {
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);

		    builder.setTitle(getResources().getString(R.string.ota_update_available));
		    builder.setMessage(getResources().getString(R.string.ota_update_intruction_msg));
		    builder.setCancelable(false);

		    builder.setPositiveButton(getResources().getString(R.string.ok),
				    new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(@NotNull DialogInterface dialog, int which) {
						    dialog.dismiss();
						    // start ota activity to get result for orbit
						    result.setRequestUpgradeOnly(true);
						    mNewFirmwareVersion = result.getNewFirmwareVersion();
						    Intent intent = new Intent(CameraSettingsActivity.this, OtaActivity.class);
						    Bundle bundle = new Bundle();
						    bundle.putBoolean(OtaActivity.IS_FROM_SETUP, true);
						    bundle.putString(OtaActivity.DEVICE_MODEL_ID, mDevice.getProfile().getModelId());
						    bundle.putSerializable(OtaActivity.CHECK_FIRMWARE_UPGRADE_RESULT, result);
						    intent.putExtras(bundle);
						    startActivityForResult(intent, FIRMWARE_UPGRADE_REQUEST_CODE);


					    }
				    });

		    builder.setNegativeButton(getResources().getString(R.string.cancel),
				    new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(@NotNull DialogInterface dialog, int which) {
						    dialog.dismiss();

					    }
				    });


		    AlertDialog alertDialog = builder.create();
		    alertDialog.show();
		    Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		    nbutton.setTextColor(getResources().getColor(R.color.text_blue));
		    Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		    pbutton.setTextColor(getResources().getColor(R.color.text_blue));

	    }


		private void setUpVideoRecordingDuration(int duration){
			if (duration == 0) {
				mVideoRecordingLayout.setVisibility(View.GONE);
				mVideoRecordingDurationTv.setText(getResources().getString(R.string.no_recording));
			} else if (duration > 0) {
				mVideoRecordingLayout.setVisibility(View.VISIBLE);
				mVideoRecordingDurationTv.setText(String.format(Locale.getDefault(),getResources().getString(R.string.video_recording_duration_time),String.valueOf(duration)));
			} else if (duration == -1) {
				mVideoRecordingLayout.setVisibility(View.VISIBLE);
				mVideoRecordingDurationTv.setText(String.format(Locale.getDefault(),getResources().getString(R.string.loading)));
			} else {
				mVideoRecordingLayout.setVisibility(View.GONE);
			}
			if(Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)
					&& !CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING, true)){
				mVideoRecordingLayout.setVisibility(View.GONE);
			}
		}


	    private void setUpOrbitPlanFirmwareSetting(){
		    mvrLayout.setVisibility(View.VISIBLE);
		    mvrCould.setVisibility(View.VISIBLE);
		    if (CommonUtil.checkSettings(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING)) {
			    if (CommonUtil.getSettingInfo(getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING)) {
				    setUpVideoRecordingDuration(CommonUtil.getVideoRecording(mContext,mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VIDEO_RECORDING));
				    motionDetection.secondaryBooleanValue = true;
				    int storageMode = CommonUtil.getSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE);
				    if (storageMode == 0) {
					    setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_CLOUD);
					    setRecordingPlanVisible(false);
				    }
				    else {
					    setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_SDCARD);
					    setRecordingPlanVisible(true);
					    if (CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_DELETE_LAST_TEN)) {
						    mRemoveSdcardClip.setOnClickListener(null);
						    mRemoveSdcardClip.setChecked(true);
						    mRemoveSdcardClip.setOnClickListener(CameraSettingsActivity.this);
					    } else if (CommonUtil.getSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_SWITCH_CLOUD)) {
						    mSwitchCloud.setOnClickListener(null);
						    mSwitchCloud.setChecked(true);
						    mSwitchCloud.setOnClickListener(CameraSettingsActivity.this);
					    }
				    }
			    } else {
				    motionDetection.secondaryBooleanValue = false;
				    setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_OFF);
				    mVideoRecordingLayout.setVisibility(View.GONE);
				    setRecordingPlanVisible(false);
			    }
		    }
	    }

	    private void setUpOrbitVRDFirmwareSetting(int duration){
		    mvrLayout.setVisibility(View.VISIBLE);
		    mvrCould.setVisibility(View.GONE);
		    mVideoRecordingLayout.setVisibility(View.VISIBLE);
		    setRecordingPlanVisible(false);
		    setUpVideoRecordingDuration(duration);
		    if(duration > 0 || duration == -1)
		    {
			    motionDetection.secondaryBooleanValue = true;
			    setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_SDCARD);
		    }
		    else
		    {
			    motionDetection.secondaryBooleanValue = false;
			    setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_OFF);
			    mVideoRecordingLayout.setVisibility(View.GONE);
			    mVideoRecordingDurationTv.setText(getResources().getString(R.string.no_recording));
		    }
	    }



    }
