package com.nxcomm.blinkhd.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.AlignmentSpan;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import com.github.amlcurran.showcaseview.Const;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hubble.BaseActivity;
import com.hubble.HubbleApplication;
import com.hubble.RGBFragment;
import com.hubble.SecureConfig;
import com.hubble.adapters.DrawerItemAdapter;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.NetworkStateChangeReceiver;
import com.hubble.dialog.AppReleaseNotesDialog;
import com.hubble.dialog.HubbleDialogFactory;
import com.hubble.events.AppVersionData;
import com.hubble.events.MessageEvent;
import com.hubble.events.ShareEventData;
import com.hubble.file.FileService;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.networkinterface.v1.pojo.HubbleRequest;
import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginManager;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.hubble.framework.service.cloudclient.profile.pojo.request.ProfileImage;
import com.hubble.framework.service.cloudclient.profile.pojo.request.RegisterProfile;
import com.hubble.framework.service.cloudclient.profile.pojo.response.ProfileImageStatus;
import com.hubble.framework.service.cloudclient.profile.pojo.response.RegisterProfileDetails;
import com.hubble.framework.service.cloudclient.user.pojo.response.UserSubscriptionPlanResponse;
import com.hubble.framework.service.notification.FirebaseManager;
import com.hubble.framework.service.notification.GCMManager;
import com.hubble.framework.service.p2p.P2pDevice;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.framework.service.p2p.P2pUtils;
import com.hubble.framework.service.profile.ProfileManagerService;
import com.hubble.framework.service.subscription.SubscriptionInfo;
import com.hubble.framework.service.subscription.SubscriptionService;
import com.hubble.helpers.AsyncPackage;
import com.hubble.model.DrawerItemModel;
import com.hubble.model.MobileSupervisor;
import com.hubble.receivers.AppExitReceiver;
import com.hubble.registration.AnalyticsController;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.SetupDataCache;
import com.hubble.registration.models.CameraBonjourInfo;
import com.hubble.registration.tasks.BonjourScan;
import com.hubble.setup.CameraSetUpActivity;
import com.hubble.subscription.ManagePlanActivity;
import com.hubble.subscription.PlanFragment;
import com.hubble.ui.DebugFragment;
import com.hubble.ui.ViewFinderActivity;
import com.hubble.util.BLEUtil;
import com.hubble.util.BgMonitorData;
import com.hubble.util.CommonConstants;
import com.hubble.util.LogZ;
import com.hubble.util.P2pSettingUtils;
import com.koushikdutta.async.future.FutureCallback;
import com.msc3.ConnectToNetworkActivity;
import com.msc3.registration.LaunchScreenActivity;
import com.nest.common.GetStartedActivity;
import com.nest.common.NestHomeActivity;
import com.nest.common.SmokeService;
import com.nestlabs.sdk.NestToken;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.nxcomm.blinkhd.util.SharedPreferencePersist;
import com.nxcomm.jstun_android.RmcChannel;
import com.profile.ProfileSynManager;
import com.profile.ProfileSynManagerCallback;
import com.sensor.bluetooth.BluetoothLeService;
import com.sensor.constants.SensorConstants;
import com.sensor.ui.CameraSensorEventLogFragment;
import com.sensor.ui.DeviceTabSupportFragment;
import com.sensor.ui.SensorDetailsFragment;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.NetworkDetector;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.SubscriptionWrapper;
import base.hubble.database.DeviceStatusDetail;
import base.hubble.subscriptions.ServerEvent;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

//import com.hubble.ui.patrol.PatrolFragment;
//import com.hubble.ui.patrol.PatrolVideoFragment;
//import com.sensor.ui.AddDeviceFragment;
//import ui.EventLogFragmentJava;

//import com.hubble.ui.VideoViewFragment;

public class MainActivity extends BaseActivity implements /*VideoViewFragment.OnFragmentInteractionListener,*/NavigationView.OnNavigationItemSelectedListener {
  public static final String PROPERTY_REG_ID = "registration_id";
  private static final String PROPERTY_APP_VERSION = "appVersion";

  private static final String TAG = "MainActivity";
  public static final String EXTRA_DIRECTLY_TO_EVENT_LOG = "com.hubble.goDirectlyToEventLog";
  public static final String EXTRA_DIRECTLY_TO_DEVICE = "com.hubble.goDirectlyToDevice";
  public static final String EXTRA_DEVICE_REGISTRATION_ID = "com.hubble.deviceRegistrationId";
  public static final String EXTRA_FLAG_SKIP_SERVER_SYNC = "skip_server_syn";
  public static final String EXTRA_DIRECTLY_TO_GALLERY = "com.hubble.goDirectlyToGallery";
  public static final String EXTRA_DIRECTLY_TO_PLAN = "com.hubble.goDirectlyToPlan";
  public static final int CAMERA_REMOVE_STATUS = 100;
  public static final int CAMERA_REMOVED_OK = 1;
  private static final String LATEST_APP_VERSION_CHECK = "last_app_version_check";
  public static final String PREF_SHOWCASE_PULL_REFRESH = "ShowcasePullToRefreshEventLog";
  public static final String PREF_SHOWCASE_SWIPE_DELETE = "ShowcaseSwipeRightToDelete";
  private static final int HANDLER_KEY_SHOWCASE_PULL_REFRESH = 1;
  private static final int HANDLER_KEY_SHOWCASE_SWIPE_DELETE = 2;
  private static final int SHOWCASE_DELAY_TIME = 500;
  private static final long EIGHT_HOUR_IN_MILLISECONDS = 8 * 3600 * 1000;
  public List<DrawerItemModel> drawerItems;
    final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

  private DeviceTabSupportFragment deviceTabSupportFragment;
  private AccountSettingFragment accountSettingFragment;
  //private PatrolVideoFragment patrolVideoFragment;
  private DebugFragment debugFragment;
  //private PatrolFragment patrollingItemFragment;
  private DrawerLayout drawerLayout;
  private CoordinatorLayout contentView;
  //private LinearLayout leftDrawer;
 // private ListView leftDrawerList;
  private ActionBarDrawerToggle mDrawerToggle;
  private boolean isActivityDestroyed;
  //private VideoViewFragment videoViewFragment;
  private SensorDetailsFragment mSensorDetailsFragment;
  //private AddDeviceFragment mAddDeviceFragment;

//  private HelpFragment helpFragment;
  private com.nxcomm.blinkhd.ui.HelpFragment helpFragment;
  //private EventLogFragmentJava eventLogFragment;
  private CameraSensorEventLogFragment cameraSensorEventLogFragment;
  private CameraListFragment cameraListFragment;
  private DrawerItemAdapter drawerAdapter;

  private NetworkStateChangeReceiver networkStateChangeReceiver = new NetworkStateChangeReceiver();

  //private CameraSettingsFragment cameraSettingFragment;
  private boolean isSensorDevice;
  private boolean isRefreshing;
  private FrameLayout overlayAddSensorsLayout;
  private ImageView overlayImageAddSensors, overlayImageSensorEvents;
  //private boolean isVideoViewFragment;
  private BonjourScan cameraBonjourScan;
  private SecureConfig settings = HubbleApplication.AppConfig;
  private String apiKey;
  private BluetoothLeService mBleService;
  private RGBFragment rbgFragment;

  private GoogleCloudMessaging gcm;
  private String regid = "";
  private NavigationView navigationView;
  private TextView mUserName, mUserEmail;
  private ImageView mUserImage;
  private final int SELECT_PHOTO = 1;
  //AA-920: Support Offline Feature on V4.2
  final public static String EXTRA_OFFLINE_MODE = "offline_mode";
  private static int mDebugEnableCount=0;
  private boolean isOfflineMode;
  private EventData eventData;
  private AlertDialog dlInAppMotion, dlInAppSound, dlInAppHiTemp, dlInAppLoTemp;
  //private MenuItem mTryUsforfree;
  private boolean mIsFreeTrial = false;
  private String mProfilePath = "";
  private SharedPreferencePersist mPrefMangerPersist;
  private SharedPreferences sharedpreferences;
  private  SharedPreferences.Editor editor;
  private BitmapProcessTask mBitmapProcessTask;
  NetworkDetector networkDetector;
  private ProfileSynManager profileSynManager;

  private MenuItem mCurrentPlan;
  private String mCurrentPlanString ;
  private long appLogoutStartTime;
  private long appLogoutTime;

  Menu menu;


  private Handler mAppExitHandler;
    private Runnable mAppExitRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"app exit");
            Intent intent=new Intent();
            intent.putExtra("isLogout", true);
            intent.setAction(AppExitReceiver.APP_EXIT_INTENT);
            sendBroadcast(intent);
        }
    };

  private BroadcastReceiver mMotionReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "MainActivity receive broadcast IN_APP_NOTIFICATION");
      int message = intent.getExtras().getInt("message");
      if (message == R.string.v_has_motion_detected) {
        if (dlInAppMotion == null) {
          dlInAppMotion = new AlertDialog.Builder(MainActivity.this).setPositiveButton(R.string.dialog_ok, null).create();
          dlInAppMotion.setMessage(getString(message));
        }
        dlInAppMotion.show();
      } else if (message == R.string.v_has_sound_detected) {
        if (dlInAppSound == null) {
          dlInAppSound = new AlertDialog.Builder(MainActivity.this).setPositiveButton(R.string.dialog_ok, null).create();
          dlInAppSound.setMessage(getString(message));
        }
        dlInAppSound.show();
      } else if (message == R.string.v_has_temp_lo_detected) {
        if (dlInAppLoTemp == null) {
          dlInAppLoTemp = new AlertDialog.Builder(MainActivity.this).setPositiveButton(R.string.dialog_ok, null).create();
          dlInAppLoTemp.setMessage(getString(message));
        }
        dlInAppLoTemp.show();
      } else if (message == R.string.v_has_temp_hi_detected) {
        if (dlInAppHiTemp == null) {
          dlInAppHiTemp = new AlertDialog.Builder(MainActivity.this).setPositiveButton(R.string.dialog_ok, null).create();
          dlInAppHiTemp.setMessage(getString(message));
        }
        dlInAppHiTemp.show();
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    registerWithGCM();
    setTimeFormatFromSystemSetting();
    eventData = new EventData();
    sharedpreferences = getSharedPreferences(CommonConstants.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
    editor = sharedpreferences.edit();
    mPrefMangerPersist = SharedPreferencePersist.initializeInstance(getApplicationContext());
    networkDetector = new NetworkDetector(getApplicationContext());
      mAppExitHandler = new Handler(getMainLooper());

//    VideoBandwidthSupervisor.getInstance().setActivity(this);

    /* added to force url */
    apiKey = Global.getApiKey(this);
    if (apiKey == null) {
      showLoginErrorAndExit();
    } else {
      Bundle extra = getIntent().getExtras();
      if (extra != null) {
        isOfflineMode = extra.getBoolean(EXTRA_OFFLINE_MODE, false);//AA-920: Support Offline Feature on V4.2
      }
      DeviceSingleton.getInstance().init(apiKey, this);
      try {
        Api.getInstance().getService().getUserSubscriptions(apiKey, new Callback<Models.ApiResponse<SubscriptionWrapper>>() {
          @Override
          public void success(Models.ApiResponse<SubscriptionWrapper> subscriptionWrapperApiResponse, Response response) {
            if (subscriptionWrapperApiResponse != null) {
              AnalyticsController.getInstance().setUserPlan(subscriptionWrapperApiResponse);
            }
          }

          @Override
          public void failure(RetrofitError error) {
          }
        });
      } catch (Exception e) {
        e.printStackTrace();
      }

      setContentView(R.layout.main_home_activity);
      profileSynManager = new ProfileSynManager(apiKey, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""), profileSynManagerCallback);
      profileSynManager.startProfileSyn();

      Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     // getSupportActionBar().setDisplayShowTitleEnabled(true);

      //getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.headerlogo));

      getSupportActionBar().setTitle("");


      overlayAddSensorsLayout = (FrameLayout) findViewById(R.id.banner_overlay_addsensors);
      overlayImageAddSensors = (ImageView) findViewById(R.id.overlay_image_addsensors);
      overlayImageSensorEvents = (ImageView) findViewById(R.id.overlay_image_sensor_events);

      if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew") ||
          BuildConfig.FLAVOR.equalsIgnoreCase("inanny") || BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
        displayOverlayAddSensor();
      }
      isActivityDestroyed = false;

      ActionBar actionBar = getSupportActionBar();

      if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
        cameraListFragment = new CameraListFragment();
      } else {
        deviceTabSupportFragment = new DeviceTabSupportFragment();
      }
      //cameraSettingFragment = new CameraSettingsFragment();
      accountSettingFragment = new AccountSettingFragment(isOfflineMode);
      //videoViewFragment = new VideoViewFragment();
      rbgFragment = new RGBFragment();

      /*if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
        patrolVideoFragment = new PatrolVideoFragment();
        patrollingItemFragment = PatrolFragment.newInstance();
      }*/
      helpFragment = new com.nxcomm.blinkhd.ui.HelpFragment();

      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayUseLogoEnabled(true);
      if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
        switchToFragment(cameraListFragment, false); // set the initial parentFragment, dont add to the backstack
      } else {
        LogZ.i("Swap to device tab support fragment");
        switchToFragment(deviceTabSupportFragment, false); // set the initial parentFragment, dont add to the backstack
      }


      setupDrawerLayout();
     /* drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      drawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));
      contentView = (CoordinatorLayout) findViewById(R.id.main_content_view);
       mDrawerToggle = new ActionBarDrawerToggle(
              this, drawerLayout, toolbar, R.string.view_event, R.string.view_event) {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
          super.onDrawerSlide(drawerView, 0);
          contentView.setTranslationX(slideOffset * drawerView.getWidth());
          drawerLayout.bringChildToFront(drawerView);
          drawerLayout.requestLayout();
        }

        @Override
        public void onDrawerOpened(View drawerView) {
          super.onDrawerOpened(drawerView);
          super.onDrawerSlide(drawerView, 0);
        }
      };
      drawerLayout.setDrawerListener(mDrawerToggle);
      mDrawerToggle.syncState();*/

      Boolean shouldGoDirectlyToEventLog = false;
      Boolean shouldGoDirectlyToGallery=false;
      Boolean shouldGoDirectlyToPlan = false;
      Boolean isFromUpdate = false;
      String deviceIdFromBundle = null;
      if (extra != null) {
        shouldGoDirectlyToEventLog = extra.getBoolean(EXTRA_DIRECTLY_TO_EVENT_LOG, false);
        isFromUpdate = extra.getBoolean("FROM_UPDATE", false);
        isOfflineMode = extra.getBoolean(EXTRA_OFFLINE_MODE, false);//AA-920: Support Offline Feature on V4.2
        shouldGoDirectlyToGallery=extra.getBoolean(EXTRA_DIRECTLY_TO_GALLERY,false);
        shouldGoDirectlyToPlan = extra.getBoolean(EXTRA_DIRECTLY_TO_PLAN,false);
      }
      if (settings.getBoolean(PublicDefine.PREFS_SHOULD_GO_TO_CAMERA, false)) {
        Device mDevice = DeviceSingleton.getInstance().getSelectedDevice();
        if (mDevice != null) {
          notDisplayOverlaySensorEvents();
          switchToCameraFragment(mDevice);
        }
        settings.remove(PublicDefine.PREFS_SHOULD_GO_TO_CAMERA);
      } else if (shouldGoDirectlyToEventLog) {
        goToEventLog();
      } else if (isFromUpdate) {
      }else if(shouldGoDirectlyToGallery){
        gotoGallery();
      } else if(shouldGoDirectlyToPlan){
        goToPlan();
      } else if(settings.getBoolean(PublicDefine.PREFS_GO_DIRECTLY_TO_SUMMARY, false)){
        Device mDevice = DeviceSingleton.getInstance().getSelectedDevice();
        if(mDevice == null){
          mDevice = DeviceSingleton.getInstance().getDeviceByRegId(settings.getString(PublicDefine.PREFS_GO_DIRECTLY_TO_REGID,""));
          if (mDevice != null) {
            // only go to the device if we found it
            DeviceSingleton.getInstance().setSelectedDevice(mDevice);
          }
        }
        if (mDevice != null) {
          goToEventSummary(mDevice);
        }
        settings.remove(PublicDefine.PREFS_GO_DIRECTLY_TO_SUMMARY);
          settings.remove(PublicDefine.PREFS_GO_DIRECTLY_TO_REGID);
      }
    }
    EventBus.getDefault().register(this);
    /*
     * Bind BluetoothLeService for changing sensor motion sensitivity purpose.
     */
    if (BLEUtil.isSupportedBLE(this)) {
      bindBLeService();
    }

    checkAppVersionIfNeeded();


  }


  @Override
  protected void onStart()
  {
    super.onStart();
  }


  private boolean isUnauthorizedDialogShow = false;

  public void onEventMainThread(ServerEvent event) {
    Log.i(TAG, "Server event received. Event code: " + event.getEventCode());

    if (event.getEventCode() == ServerEvent.UNAUTHORIZED_REQUEST) {
      if (!isUnauthorizedDialogShow) {
        showUnauthorizedDialog();
      }
    }
  }

  public void onEventMainThread(MessageEvent event) {
    Log.i(TAG, "General event received. Event code: " + event.getEventCode());
    if (event.getEventCode() == MessageEvent.SHOW_EVENT_LOG_SHOWCASE) {
      mHandler.removeCallbacksAndMessages(null);
      if (!settings.getBoolean(PREF_SHOWCASE_PULL_REFRESH, false)) {
        mHandler.sendEmptyMessageDelayed(HANDLER_KEY_SHOWCASE_PULL_REFRESH, SHOWCASE_DELAY_TIME);
      } else if (!settings.getBoolean(PREF_SHOWCASE_SWIPE_DELETE, false)) {
        mHandler.sendEmptyMessageDelayed(HANDLER_KEY_SHOWCASE_SWIPE_DELETE, SHOWCASE_DELAY_TIME);
      }
    } else if (event.getEventCode() == MessageEvent.HAS_NEW_APP_VERSION_ON_STORE) {
      App.set(LATEST_APP_VERSION_CHECK, System.currentTimeMillis());
      AppVersionData appVersionData = (AppVersionData) event.getExtra();
      Log.i(TAG, "Release notes: " + appVersionData.getReleaseNotes());
      AppReleaseNotesDialog appReleaseNotesDialog = AppReleaseNotesDialog.newInstance(appVersionData.getVersionText(), appVersionData.getReleaseNotes());
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
      appReleaseNotesDialog.show(transaction, "new-app-release-dialog");
    } else if (event.getEventCode() == MessageEvent.CAMERA_REMOVED) {
      Log.d(TAG, "Refresh camera list");
    }
  }

  private void showUnauthorizedDialog() {


      Intent authErrorBroadcast = new Intent("com.hubble.receivers.SessionExpireReceiver");
      getApplicationContext().sendBroadcast(authErrorBroadcast);

  }

  @Override
  protected void onStop() {
    super.onStop();
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      Log.d(TAG, "MainActivity unregister in app notification");
      unregisterReceiver(mMotionReceiver);
      try {
        if (dlInAppHiTemp != null && dlInAppHiTemp.isShowing()) {
          dlInAppHiTemp.dismiss();
        }
        if (dlInAppLoTemp != null && dlInAppLoTemp.isShowing()) {
          dlInAppLoTemp.dismiss();
        }
        if (dlInAppMotion != null && dlInAppMotion.isShowing()) {
          dlInAppMotion.dismiss();
        }
        if (dlInAppSound != null && dlInAppSound.isShowing()) {
          dlInAppSound.dismiss();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }

    return false;
  }

  private void showLoginErrorAndExit() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(getString(R.string.error_occurred_please_log_in)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            onUserLogout();
            setResult(CommonConstants.SHOULD_EXIT_NOW_YES);
            finish();
          }
        }
    );

    AlertDialog confirmExitDialog = builder.create();
    confirmExitDialog.setIcon(R.drawable.ic_launcher);
    confirmExitDialog.setTitle(R.string.app_brand_application_name);
    confirmExitDialog.setCancelable(true);
    confirmExitDialog.show();
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "MainActivity onResume");
    MobileSupervisor.getInstance().registerListener(getApplicationContext());
    registerNetworkChangeReceiver();
    setTimeFormatFromSystemSetting();

    if (settings.getBoolean(PublicDefine.PREFS_SHOULD_GO_TO_CAMERA, false)) {
      Device mDevice = DeviceSingleton.getInstance().getSelectedDevice();
      if (mDevice != null) {
//        if (settings.getBoolean(PublicDefine.RGB_KEY, false)) {
//          rbgFragment.setmMacAddress(mDevice.getProfile().macAddress);
//          switchToFragment(rbgFragment);
//          settings.remove(PublicDefine.RGB_KEY);
//        } else {
          switchToCameraFragment(mDevice);
//        }
      }
      settings.remove(PublicDefine.PREFS_SHOULD_GO_TO_CAMERA);
    } else if (deviceTabSupportFragment != null) {
      isRefreshing = true;
    } else if (cameraListFragment != null) {
      cameraListFragment.setRefreshing(true);
    }

    /*
     * 20160226: HOANG: VIC-1454
     * There's a case: go to setup camera screen, then press back to go back to camera list.
     * Sometimes, app restarted app without calling onDestroy of SingleCamConfigureActivity (may be due to low memory).
     * Thus, P2pService is stopped unexpectedly and will not restarted until user exit and reopen app.
     * Solution: check whether P2pService is running on MainActivity start. If not, restart it immediately.
     */
    if (P2pSettingUtils.hasP2pFeature()) {
      // In startP2pService() method, app already checked whether P2pService is running.
      // Build P2P device list
      final List<P2pDevice> p2pDevices = new ArrayList<>();
      List<Device> cameraDevices = DeviceSingleton.getInstance().getDevices();
      if (cameraDevices != null) {
        for (Device cameraDevice : cameraDevices) {

          boolean isOrbitP2PEnabled =  !cameraDevice.getProfile().isStandBySupported() || HubbleApplication.AppConfig.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);

          Log.d(TAG,"is Live Status :-" + cameraDevice.getProfile().isLiveStatus());

          boolean isAllowedP2P = true; // true for other device
          if(cameraDevice.getProfile().isStandBySupported())
          {
            if(!cameraDevice.getProfile().isLiveStatus()){
              isAllowedP2P = false;
            }
          }

          if (isOrbitP2PEnabled && isAllowedP2P && cameraDevice.getProfile().canUseP2p() && cameraDevice.getProfile().canUseP2pRelay() &&
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
      } else {
        Log.d(TAG, "Main activity, device list is null");
      }

      P2pUtils.startP2pService(getApplicationContext(), apiKey, p2pDevices);
      /*
      new Handler().postDelayed(new Runnable()
      {
        @Override
        public void run()
        {

        }
      }, 1000 * 3); */

      // stop p2p timer when activity started
      P2pManager.getInstance().stopP2pTimer();
    }

    if (needDiscoverLocalCamera()) {
      discoverLocalCamera();
    } else {
      LogZ.i("Last camera scan is less than 30 seconds.");
    }

     /* if(SettingsPrefUtils.SHOULD_READ_SETTINGS){
          List<Device> mLocalDevice = DeviceSingleton.getInstance().getDevices();
          for(Device sDevice:mLocalDevice){
              CommonUtil.setSettingInfo(getApplicationContext(), sDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.FETCH_SETTINGS, true);

          }
          if(mLocalDevice.size() > 0)
              SettingsPrefUtils.SHOULD_READ_SETTINGS = false;
      }*/
  }

  public void checkAppVersionIfNeeded() {
    long latestCheckAppVersion = App.getLong(LATEST_APP_VERSION_CHECK, -1);

    if (latestCheckAppVersion == -1 || (System.currentTimeMillis() - latestCheckAppVersion) > EIGHT_HOUR_IN_MILLISECONDS) {
      Log.i(TAG, "Check new app version");
      App.checkAppVersion();
    } else {
      Log.i(TAG, "No need to check new app version");
    }

  }

  public boolean networkChangeRegistered = false;

  public void registerNetworkChangeReceiver() {

    //Phung: lets' not do this
//    if (!networkChangeRegistered) {
//      IntentFilter intentFilter = new IntentFilter();
//      intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//      registerReceiver(networkStateChangeReceiver, intentFilter);
//      networkChangeRegistered = true;
//    }
  }

  public void unregisterNetworkChangeReceiver() {
//Phung: lets' not do this
//    if (networkChangeRegistered) {
//      unregisterReceiver(networkStateChangeReceiver);
//      networkChangeRegistered = false;
//    }
  }

  @Override
  protected void onPause() {
    Log.d(TAG, "MainActivity onPaused");

    /*
	 * 20160229: HOANG: AA-1591
	 * When exit main activity, app need to set keep alive mode for all p2p sessions.
	 */
    if (P2pSettingUtils.hasP2pFeature()) {
      P2pManager.getInstance().setGlobalRmcChannelMode(RmcChannel.RMC_CHANNEL_MODE_KEEP_ALIVE);
      String regId = BgMonitorData.getInstance().getRegistrationId();
      if (BgMonitorData.getInstance().isShouldEnableBgAfterQuitView()) {
        P2pManager.getInstance().switchAllToModeAsyncExcludeDevice(RmcChannel.RMC_CHANNEL_MODE_KEEP_ALIVE, regId);
      } else {
        P2pManager.getInstance().switchAllToModeAsync(RmcChannel.RMC_CHANNEL_MODE_KEEP_ALIVE);
      }
      P2pManager.getInstance().startP2pTimer(
              BgMonitorData.getInstance().isShouldEnableBgAfterQuitView(),
              BgMonitorData.getInstance().getRegistrationId());
    }

    notDisplayOverlaySensorEvents();
    unregisterNetworkChangeReceiver();
    if (deviceTabSupportFragment != null) {
      isRefreshing = false;
    } else if (cameraListFragment != null) {
      cameraListFragment.setRefreshing(false);
    }

    super.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    isActivityDestroyed = true;
    EventBus.getDefault().unregister(this);
    /*
     * Unbind BluetoothLeService for changing sensor motion sensitivity purpose.
     */
    if (BLEUtil.isSupportedBLE(this)) {
      unbindBleService();
    }
    if(CommonUtil.getNestConfig(this)){
      NestPluginManager.getInstance().clearAuthListener();
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    // // Log.d(TAG, "onConfigurationChange()");
    super.onConfigurationChanged(newConfig);
    //setFullscreenWhenLandscape();
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    if (mDrawerToggle != null) {
      mDrawerToggle.syncState();
    }
  }

  @Override
  public void onBackPressed() {
    if (showcaseView != null && showcaseView.getParent() != null) {
      ((ViewGroup) findViewById(android.R.id.content)).removeView(showcaseView);
      return;
    }
    if (showcaseView != null) {
      showcaseView = null;
    }
    if (drawerLayout != null /*&& leftDrawer != null*/) {
      /*if (drawerLayout.isDrawerOpen(leftDrawer)) {
        drawerLayout.closeDrawer(leftDrawer);
      } else */{
        if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
          if (cameraListFragment.isVisible()) {
            finish();
          } /*else if (isVideoViewFragment && !videoViewFragment.isVisible()) {
            super.onBackPressed();
          }*/ else {
            // Stop stream as soon as possible to intterupt p2p command if any
            Log.d(TAG, "onBackPressed interrupt streaming immediately!");
            //stopStreamingImmediately();
            notDisplayOverlaySensorEvents();
            //isVideoViewFragment = false;
            switchToFragment(cameraListFragment);
            if (drawerAdapter != null) {
              drawerAdapter.setSelectedPosition(0);
            }
          }
        } else {
          if (deviceTabSupportFragment.isVisible()) {
            finish();
          } /*else if (isVideoViewFragment && !videoViewFragment.isVisible()) {
            super.onBackPressed();
          } */else {
            // Stop stream as soon as possible to intterupt p2p command if any
            Log.d(TAG, "onBackPressed interrupt streaming immediately!");
            //stopStreamingImmediately();
            //videoViewFragment.onBackPressed();
            notDisplayOverlaySensorEvents();
            //isVideoViewFragment = false;
            switchToFragment(deviceTabSupportFragment);
            if (drawerAdapter != null) {
              drawerAdapter.setSelectedPosition(0);
            }
          }
        }
      }

    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == PLAY_SERVICES_RESOLUTION_REQUEST ){
        if (resultCode == Activity.RESULT_OK) {
           registerWithGCM();
        }
    }
    else if (requestCode == PublicDefine.RESULT_SHARE_SNAPSHOT) {
    }else if (requestCode == SELECT_PHOTO){
      if (resultCode == RESULT_OK) {
        try {
          if (data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
              mBitmapProcessTask = new BitmapProcessTask(imageUri);
              mBitmapProcessTask.execute();
            }
          }

        } catch (OutOfMemoryError e) {
          e.printStackTrace();
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
        }

      }

    }
  }

  private void setTimeFormatFromSystemSetting() {
    boolean h24 = DateFormat.is24HourFormat(this);
    settings.putInt(PublicDefineGlob.PREFS_CLOCK_MODE, h24 ? PublicDefineGlob.CLOCK_MODE_24H : PublicDefineGlob.CLOCK_MODE_12H);
  }

  //Assumed to be called only from VideoViewFragment, so we transition from the left
  public void switchToCameraFragment(Device camProfile) {
    if (camProfile != null) {
     /* isVideoViewFragment = true;
      videoViewFragment.setCamera(getApplicationContext(), camProfile);
      switchToFragment(videoViewFragment);*/
      Intent intent=new Intent(MainActivity.this,ViewFinderActivity.class);
      intent.putExtra(CommonConstants.VIEW_FINDER_LAUNCH_TAG,CommonConstants.VIEW_FINDER_GOTO_STREAM);
      intent.putExtra("reg_id",camProfile.getProfile().registrationId);
      DeviceSingleton.getInstance().setSelectedDevice(camProfile);
      startActivity(intent);

    }
  }

  /*public void switchToSelectedCamera(Device camProfile) {
    if (camProfile != null) {
      videoViewFragment.setCamera(getApplicationContext(), camProfile);
      videoViewFragment.switchToSelectedCamera();
    }
  }*/

  public void switchToSensorDetailFragment(Device sensorDevice) {
    if (mSensorDetailsFragment == null)
      mSensorDetailsFragment = new SensorDetailsFragment();

    /* if (sensorDevice != null) {
     *  getSupportActionBar().setTitle(sensorDevice.getProfile().getName());
     * } */
    invalidateOptionsMenu();
    mSensorDetailsFragment.setmSensorDevice(sensorDevice);
    switchToFragment(mSensorDetailsFragment);
  }


  /*public void switchToPatrolVideoFragment(Device camProfile) {
    if (camProfile != null) {
      //patrolVideoFragment.setCamera(camProfile);
    }
    switchToFragment(patrolVideoFragment);
  }*/

  /*public void switchToCameraFragmentForPatrol(Runnable runnable) {
    patrolVideoFragment.setOnCreateViewCompleteRunnable(runnable);
    switchToPatrolVideoFragment(DeviceSingleton.getInstance().getSelectedDevice());
  }*/

  /*public VideoViewFragment getVideoViewFragment() {
    return videoViewFragment;
  }*/

  /*public PatrolVideoFragment getPatrolVideoFragment() {
    return patrolVideoFragment;
  }*/

  public void switchToDeviceList() {
    switchToDeviceList(false);
  }

  public void switchToDeviceList(boolean refreshList) {
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      switchToFragment(cameraListFragment);
    } else {
      switchToFragment(deviceTabSupportFragment);
    }
    if (refreshList) {
      mHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          Intent mIntent = new Intent(CameraListFragment.BROADCAST_REFRESH_CAMERA_LIST);
          sendBroadcast(mIntent);
        }
      }, 200);
    }
  }

  private void setupDrawerLayout() {

    // DrawerLayout side menu
   // drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
   // leftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
   // leftDrawerList = (ListView) findViewById(R.id.left_drawer_listview);

    String ourUserEmail = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, "");
    String ourUserName = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, "");

    if (ourUserEmail.length() > 1 && ourUserName.length() > 1) {
     // View usernameHolder = findViewById(R.id.left_drawer_username_holder);
      //TextView usernameTV = (TextView) findViewById(R.id.left_drawer_username);

      //usernameHolder.setVisibility(View.VISIBLE);

      /*if (ourUserName.length() > 1) {
        usernameTV.setVisibility(View.VISIBLE);
        usernameTV.setText(ourUserName);
      } else if (ourUserEmail.length() > 1) {
        usernameTV.setVisibility(View.VISIBLE);
        usernameTV.setText(ourUserEmail);
      }*/
    }

    /*mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_menu, R.string.close_menu) {
      public void onDrawerClosed(View view) {
        super.onDrawerClosed(view);
      }

      public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
      }
    };*/

    // Set the drawer toggle as the DrawerListener
    // drawerLayout.setDrawerListener(mDrawerToggle);

    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));
    contentView = (CoordinatorLayout) findViewById(R.id.main_content_view);
    mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.open_menu, R.string.close_menu) {
      @Override
      public void onDrawerSlide(View drawerView, float slideOffset) {
        super.onDrawerSlide(drawerView, 0);
        contentView.setTranslationX(slideOffset * drawerView.getWidth());
        drawerLayout.bringChildToFront(drawerView);
        drawerLayout.requestLayout();
      }

      @Override
      public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        super.onDrawerSlide(drawerView, 0);
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.ACCOUNT_MANAGEMENT,AppEvents.DM_OPENED,AppEvents.ACCOUNT_MANAGEMENT);
        ZaiusEvent drawerMenuEvt = new ZaiusEvent(AppEvents.ACCOUNT_MANAGEMENT);
        drawerMenuEvt.action(AppEvents.DM_OPENED);
        try {
          ZaiusEventManager.getInstance().trackCustomEvent(drawerMenuEvt);
        } catch (ZaiusException e) {
          e.printStackTrace();
        }
        checkSmartNurserryApp();
        checkUserSubscription();
        /*if (!CommonUtil.isOrbit(MainActivity.this)) {
          checkFreeTrialAndsetMenu();
        }*/
      }
    };
    drawerLayout.setDrawerListener(mDrawerToggle);
    mDrawerToggle.syncState();

    navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    View headerView = navigationView.inflateHeaderView(R.layout.nav_header_dash_board);
    mUserImage = (ImageView) headerView.findViewById(R.id.user_image);
    mUserName = (TextView) headerView.findViewById(R.id.user_name);
    mUserEmail = (TextView) headerView.findViewById(R.id.user_email);



    //if (ourUserEmail.length() > 1 && ourUserName.length() > 1) {
      // View usernameHolder = findViewById(R.id.left_drawer_username_holder);
      //TextView usernameTV = (TextView) findViewById(R.id.left_drawer_username);

      //usernameHolder.setVisibility(View.VISIBLE);

      if (ourUserName.length() > 1)
          mUserName.setText(ourUserName);

      if (ourUserEmail.length() > 1)
         mUserEmail.setText(ourUserEmail);

    //}

      menu= navigationView.getMenu();

   /* MenuItem nestSetting = menu.findItem(R.id.settings);
    SpannableString spannable_nestSettings = new SpannableString(nestSetting.getTitle());
    spannable_nestSettings.setSpan(new TextAppearanceSpan(this, R.style.Navigation_ItemColor), 0, spannable_nestSettings.length(), 0);
    nestSetting.setTitle(spannable_nestSettings);

    if(CommonUtil.getNestConfig(MainActivity.this)){
      nestSetting.setVisible(true);
    }else{
      nestSetting.setVisible(false);
    }
*/

      MenuItem changePassword= menu.findItem(R.id.change_password);
      SpannableString spannableChangePassword = new SpannableString(changePassword.getTitle());
      spannableChangePassword.setSpan(new TextAppearanceSpan(this, R.style.Menu_ItemColor), 0, spannableChangePassword.length(), 0);
      changePassword.setTitle(spannableChangePassword);


      MenuItem accountSettings= menu.findItem(R.id.menu_accountSettings);
      SpannableString spannable_accSettings = new SpannableString(accountSettings.getTitle());
      spannable_accSettings.setSpan(new TextAppearanceSpan(this, R.style.Navigation_ItemColor), 0, spannable_accSettings.length(), 0);
      accountSettings.setTitle(spannable_accSettings);


      MenuItem managePlan= menu.findItem(R.id.manage_plan);
      SpannableString spannable_managePlan = new SpannableString(managePlan.getTitle());
      spannable_managePlan.setSpan(new TextAppearanceSpan(this, R.style.Menu_ItemColor), 0, spannable_managePlan.length(), 0);
      managePlan.setTitle(spannable_managePlan);

      mCurrentPlan = menu.findItem(R.id.current_plan);
      if(mCurrentPlanString != null) {
        mCurrentPlan.setVisible(true);
        mCurrentPlan.setTitle(getString(R.string.current_plan_drawer)+ " "  + mCurrentPlanString);
        setCurrentPlanMenuColor();
      }else {
        mCurrentPlan.setVisible(false);
      }
      checkUserSubscription();


      MenuItem workWithNest= menu.findItem(R.id.work_with_nest);
      SpannableString spannable_workWithNest = new SpannableString(workWithNest.getTitle());
      spannable_workWithNest.setSpan(new TextAppearanceSpan(this, R.style.Menu_ItemColor), 0, spannable_workWithNest.length(), 0);
      workWithNest.setTitle(spannable_workWithNest);




      MenuItem dnd= menu.findItem(R.id.app_settings);
      SpannableString spannable_dnd = new SpannableString(dnd.getTitle());
      spannable_dnd.setSpan(new TextAppearanceSpan(this, R.style.Menu_ItemColor), 0, spannable_dnd.length(), 0);
      dnd.setTitle(spannable_dnd);

      MenuItem support = menu.findItem(R.id.support);
      SpannableString spannable_support = new SpannableString(support.getTitle());
      spannable_support.setSpan(new TextAppearanceSpan(this, R.style.Menu_ItemColor), 0, spannable_support.length(), 0);
      support.setTitle(spannable_support);

      MenuItem privacy = menu.findItem(R.id.privacy_policy);
      SpannableString spannable_privacy = new SpannableString(privacy.getTitle());
      spannable_privacy.setSpan(new TextAppearanceSpan(this, R.style.Menu_ItemColor), 0, spannable_privacy.length(), 0);
      privacy.setTitle(spannable_privacy);

      MenuItem licenses = menu.findItem(R.id.licenses);
      SpannableString spannable_licenses = new SpannableString(licenses.getTitle());
      spannable_licenses.setSpan(new TextAppearanceSpan(this, R.style.Menu_ItemColor), 0, spannable_licenses.length(), 0);
      licenses.setTitle(spannable_licenses);


        /*  MenuItem currentPlan = navigationView.getMenu().findItem(R.id.manage_plan).setActionView(new TextView(this));
    TextView currentPlanTV = (TextView) currentPlan.getActionView();
    currentPlanTV.setText("Freemium" );

    MenuItem managePlan = navigationView.getMenu().findItem(R.id.manage_plan);
    String managePlanTitle = managePlan.getTitle().toString();
    managePlanTitle = managePlanTitle + "\n" + "freemium";
    managePlan.setTitle(managePlanTitle);
    managePlan.setTitleCondensed(managePlanTitle);*/


  /*  MenuItem planSettings = menu.findItem(R.id.menu_plan_Settings);
    //if (CommonUtil.isOrbit(this)) {
      //planSettings.setVisible(false);
    //} else {
      planSettings.setVisible(true);
      SpannableString spannable_planSettings = new SpannableString(planSettings.getTitle());
      spannable_planSettings.setSpan(new TextAppearanceSpan(this, R.style.Navigation_ItemColor), 0, spannable_planSettings.length(), 0);
      planSettings.setTitle(spannable_planSettings);
    //}*/

    /*mTryUsforfree = menu.findItem(R.id.try_us_for_free);
    mTryUsforfree.setVisible(false);*/

    final MenuItem p2pStreaming =  navigationView.getMenu().findItem(R.id.p2p)
            .setActionView(new Switch(MainActivity.this));
    final Switch p2pStreamingSwitch = (Switch) p2pStreaming.getActionView();

    boolean isDebugEnabled= settings.getBoolean(DebugFragment.PREFS_DEBUG_ENABLED, false);

    if(isDebugEnabled){
      p2pStreaming.setVisible(true);
      if(settings.getBoolean(PublicDefineGlob.PREFS_IS_P2P_ENABLED, true)){
        p2pStreamingSwitch.setChecked(true);
        p2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
        p2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(this, R.color.text_blue), PorterDuff.Mode.SRC_IN);

      }else {
        p2pStreamingSwitch.setChecked(false);
        p2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(this, R.color.color_divider), PorterDuff.Mode.SRC_IN);
        p2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(this, R.color.color_grey_white), PorterDuff.Mode.SRC_IN);
      }

    }else{
      p2pStreaming.setVisible(false);
    }

    p2pStreamingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
          settings.putBoolean(PublicDefineGlob.PREFS_IS_P2P_ENABLED, true);
          p2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
          p2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
        }else {
          settings.putBoolean(PublicDefineGlob.PREFS_IS_P2P_ENABLED, false);
          p2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_divider), PorterDuff.Mode.SRC_IN);
          p2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_grey_white), PorterDuff.Mode.SRC_IN);
        }
      }
    });

    /****** ORBIT SPECIFIC **/
    final MenuItem orbitP2pStreaming =  navigationView.getMenu().findItem(R.id.orbit_p2p)
            .setActionView(new Switch(MainActivity.this));
    final Switch orbitP2pStreamingSwitch = (Switch) orbitP2pStreaming.getActionView();


    if(isDebugEnabled && BuildConfig.DEBUG)
    {
      orbitP2pStreaming.setVisible(true);
      if(settings.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false))
      {
        orbitP2pStreamingSwitch.setChecked(true);
        orbitP2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
        orbitP2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(this, R.color.text_blue), PorterDuff.Mode.SRC_IN);

      }
      else
      {
        orbitP2pStreamingSwitch.setChecked(false);
        orbitP2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(this, R.color.color_divider), PorterDuff.Mode.SRC_IN);
        orbitP2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(this, R.color.color_grey_white), PorterDuff.Mode.SRC_IN);
      }

    }
    else
    {
      orbitP2pStreaming.setVisible(false);
    }

    orbitP2pStreamingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
        if(isChecked)
        {
          settings.putBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, true);
          orbitP2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
          orbitP2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);

          List<P2pDevice> p2pDevices = new ArrayList<>();
          List<Device> cameraDevices = DeviceSingleton.getInstance().getDevices();
          if (cameraDevices != null) {
            for (Device cameraDevice : cameraDevices) {
              if (cameraDevice.getProfile().canUseP2p() && cameraDevice.getProfile().canUseP2pRelay() &&
                      !TextUtils.isEmpty(cameraDevice.getProfile().getRegistrationId())) {
                P2pDevice newDevice = new P2pDevice();
                newDevice.setRegistrationId(cameraDevice.getProfile().getRegistrationId());
                newDevice.setFwVersion(cameraDevice.getProfile().getFirmwareVersion());
                newDevice.setMacAddress(cameraDevice.getProfile().getMacAddress());
                newDevice.setModelId(cameraDevice.getProfile().getModelId());
                if (cameraDevice.getProfile().getDeviceLocation() != null) {
                  newDevice.setLocalIp(cameraDevice.getProfile().getDeviceLocation().getLocalIp());
                }
                if (cameraDevice.getProfile().isStandBySupported()) {
                  DeviceStatusDetail statusDetails = cameraDevice.getProfile().getDeviceStatusDetail();
                  if (statusDetails != null) {
                    String status = statusDetails.getDeviceStatus();
                    if (status.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0) {
                      newDevice.setAvailable(true);
                    } else {
                      newDevice.setAvailable(false);
                    }
                  } else {
                    newDevice.setAvailable(cameraDevice.getProfile().isAvailable());
                  }


                } else {
                  newDevice.setAvailable(cameraDevice.getProfile().isAvailable());

                }
                p2pDevices.add(newDevice);
              }
            }
            P2pUtils.startP2pService(getBaseContext(), settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), p2pDevices);

          } else {
            Log.d(TAG, "device wakeup, device list is null");
          }
        }
        else
        {
          settings.putBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);
          orbitP2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_divider), PorterDuff.Mode.SRC_IN);
          orbitP2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_grey_white), PorterDuff.Mode.SRC_IN);
        }
      }
    });


    final MenuItem rtmpsStreaming =  navigationView.getMenu().findItem(R.id.rtmps)
            .setActionView(new Switch(MainActivity.this));
    final Switch rtmpsSwitch = (Switch) rtmpsStreaming.getActionView();

    if(isDebugEnabled){
      rtmpsStreaming.setVisible(true);
      if(settings.getBoolean(PublicDefineGlob.PREFS_IS_RTMPS_ENABLED, true)){
        rtmpsSwitch.setChecked(true);
        rtmpsSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
        rtmpsSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(this, R.color.text_blue), PorterDuff.Mode.SRC_IN);

      }else {
        rtmpsSwitch.setChecked(false);
        rtmpsSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(this, R.color.color_divider), PorterDuff.Mode.SRC_IN);
        rtmpsSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(this, R.color.color_grey_white), PorterDuff.Mode.SRC_IN);
      }

    }else{
      rtmpsStreaming.setVisible(false);
    }

    rtmpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
          settings.putBoolean(PublicDefineGlob.PREFS_IS_RTMPS_ENABLED, true);
          rtmpsSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
          rtmpsSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
        }else {
          settings.putBoolean(PublicDefineGlob.PREFS_IS_RTMPS_ENABLED, false);
          rtmpsSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_divider), PorterDuff.Mode.SRC_IN);
          rtmpsSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_grey_white), PorterDuff.Mode.SRC_IN);
        }
      }
    });

    MenuItem about = menu.findItem(R.id.menu_about);
    SpannableString spannableabout = new SpannableString(about.getTitle());
    spannableabout.setSpan(new TextAppearanceSpan(this, R.style.Navigation_ItemColor), 0, spannableabout.length(), 0);
    about.setTitle(spannableabout);

    String ver = null;
      PackageInfo pinfo;
      try {
        pinfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
        ver = pinfo.versionName;
      } catch (PackageManager.NameNotFoundException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
        ver = "Unknown";
      }
    //SubMenu subMenu = about.getSubMenu();
    //MenuItem item = subMenu.findItem(R.id.info);
      MenuItem info= menu.findItem(R.id.info);
      SpannableString spannable_info = new SpannableString("App Version : "+ver);
      spannable_info.setSpan(new TextAppearanceSpan(this, R.style.Menu_ItemColor), 0, spannable_info.length(), 0);
      info.setTitle(spannable_info);
    //item.setTitle("App Version : "+ver);


      info.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        mDebugEnableCount++;
        if(mDebugEnableCount==5){
          mDebugEnableCount=0;
          boolean isDebugEnabled= settings.getBoolean(DebugFragment.PREFS_DEBUG_ENABLED, false);

          if(!isDebugEnabled){
            settings.putBoolean(DebugFragment.PREFS_DEBUG_ENABLED, true);
            Toast.makeText(MainActivity.this,getString(R.string.debug_enabled),Toast.LENGTH_SHORT).show();
            p2pStreaming.setVisible(true);
            rtmpsStreaming.setVisible(true);
            orbitP2pStreaming.setVisible(true);

            if(settings.getBoolean(PublicDefineGlob.PREFS_IS_P2P_ENABLED, true)){
              p2pStreamingSwitch.setChecked(true);
              p2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
              p2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
            }else {
              p2pStreamingSwitch.setChecked(false);
              p2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_divider), PorterDuff.Mode.SRC_IN);
              p2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_grey_white), PorterDuff.Mode.SRC_IN);

            }

            if(settings.getBoolean(PublicDefineGlob.PREFS_IS_RTMPS_ENABLED, true)){
              rtmpsSwitch.setChecked(true);
              rtmpsSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
              rtmpsSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
            }else {
              rtmpsSwitch.setChecked(false);
              rtmpsSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_divider), PorterDuff.Mode.SRC_IN);
              rtmpsSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_grey_white), PorterDuff.Mode.SRC_IN);

            }


            if(settings.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false))
            {
              orbitP2pStreamingSwitch.setChecked(true);
              orbitP2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);
              orbitP2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.text_blue), PorterDuff.Mode.SRC_IN);

            }
            else
            {
              orbitP2pStreamingSwitch.setChecked(false);
              orbitP2pStreamingSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_divider), PorterDuff.Mode.SRC_IN);
              orbitP2pStreamingSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_grey_white), PorterDuff.Mode.SRC_IN);
            }


          }else{
            settings.putBoolean(DebugFragment.PREFS_DEBUG_ENABLED, false);
            Toast.makeText(MainActivity.this,getString(R.string.debug_disabled),Toast.LENGTH_SHORT).show();
            p2pStreaming.setVisible(false);
            rtmpsStreaming.setVisible(false);
            orbitP2pStreaming.setVisible(false);
          }
        }

        return true;
      }
    });

    MenuItem logout = menu.findItem(R.id.logout);
    SpannableString spannable_logout = new SpannableString(logout.getTitle());
    spannable_logout.setSpan(new TextAppearanceSpan(this, R.style.Logout_ItemColor), 0, spannable_logout.length(), 0);
    spannable_logout.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, spannable_logout.length(), 0);
    logout.setTitle(spannable_logout);


    mUserImage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AnalyticsInterface.getInstance().trackEvent(AppEvents.CHANGE_PROFILE_PIC,AppEvents.CHANGE_PROFILE_PIC,eventData);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);

       }
    });

    String imagePath = sharedpreferences.getString(CommonConstants.ACCOUNT_PROFILE_IMAGE, "");

    if (mPrefMangerPersist.isContain(imagePath)) {
      mProfilePath = mPrefMangerPersist.getString(imagePath, "");
      if (!TextUtils.isEmpty(mProfilePath)) {
        setProfilePic(mProfilePath);
      }
    }

    //TODO: localization needed here, but is this the best way to add items to the left?
    // TODO: it no longer would need to be dynamic and have the list of cameras... perhaps this is best done in the xml layout
/* REmoved below code as its not being used in latest
    drawerItems = new ArrayList<>();
    drawerItems.add(new DrawerItemModel(getString(R.string.cameras), getResources().getDrawable(R.drawable.ic_action_camera_icon), DrawerItemModel.StaticMenuItems.CAMERA_LIST));
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech") || BuildConfig.FLAVOR.equalsIgnoreCase("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew")) {
      drawerItems.add(new DrawerItemModel(getString(R.string.videos), getResources().getDrawable(R.drawable.ic_videocam_white_48dp), DrawerItemModel.StaticMenuItems.VIDEO));
    }
    drawerItems.add(new DrawerItemModel(getString(R.string.account), getResources().getDrawable(R.drawable.ic_action_person_icon), DrawerItemModel.StaticMenuItems.ACCOUNT));

    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      drawerItems.add(new DrawerItemModel(getString(R.string.help), getResources().getDrawable(R.drawable.ic_action_help), DrawerItemModel.StaticMenuItems.HELP));
    }


    drawerAdapter = new DrawerItemAdapter(MainActivity.this, drawerItems);
    */
   // leftDrawerList.setAdapter(drawerAdapter);
    //leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    Log.d(TAG, ">>> Verify to Add a Button in app to encourage freemium user to enrol in Free-Trial");
    //checkFreeTrialAndsetMenu();
  }

  /*private void checkFreeTrialAndsetMenu(){
    mIsFreeTrial = false;
    new Thread(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG, "Checking user subscription...");
        Models.ApiResponse<SubscriptionWrapper> userSubscriptions = null;
        try {
          userSubscriptions = Api.getInstance().getService().getUserSubscriptions(apiKey);
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (userSubscriptions != null && userSubscriptions.getData() != null) {
          List<UserSubscription> subscriptions = userSubscriptions.getData().getPlans();
          if (subscriptions == null || subscriptions.size() == 0 || !checkActiveSubscription(subscriptions)) {
            Log.d(TAG, "User has no subscription or any active subscription. Continue checking camera...");
            if (checkFreeTrialEligible()) {
              List<Device> filterDevices = getFreeTrialEligibleCameras(true);
              if (filterDevices != null && filterDevices.size() > 0) {
                Log.d(TAG, "There is cameras that that eligible for free trial. Show button Try us for Free.");
                mIsFreeTrial = true;
              } else {
                Log.d(TAG, "There is no cameras that eligible for free trial. Do not show button Try us for Free.");
              }
            } else {
              Log.d(TAG, "There is camera that is already free trial. Do not show button Try us for Free.");
            }
          }
        }
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if(mIsFreeTrial) {
              mTryUsforfree.setVisible(true);
            }else {
              mTryUsforfree.setVisible(false);
            }
          }
        });
      }
    }).start();
  }*/

  /*private boolean checkActiveSubscription(List<UserSubscription> subscriptions) {
    for (UserSubscription item : subscriptions) {
      if ("active".equals(item.getState()) || "pending".equals(item.getState())) {
        return true;
      }
    }
    return false;
  }*/

  // if there is any camera that is free trial, Free-Trial-Eligible will be fail.
  /*private boolean checkFreeTrialEligible() {
    List<Device> allDevices = DeviceSingleton.getInstance().getDevices();
    if (allDevices == null || allDevices.isEmpty()) {
      long startTime = System.currentTimeMillis();
      do {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        allDevices = DeviceSingleton.getInstance().getDevices();
      } while ((allDevices == null || allDevices.isEmpty()) // device list is not available
          && System.currentTimeMillis() - startTime < 60 * 1000); // wait time is not over 60s
    }
    if (allDevices == null || allDevices.isEmpty()) {
      return false;
    }
    for (Device device: allDevices) {
      if (device.getProfile() != null) {
        DeviceProfile profile = device.getProfile();
        if (TextUtils.isEmpty(profile.getParentId()) && // is camera
            profile.getDeviceFreeTrial() != null && "active".equals(profile.getDeviceFreeTrial().getStatus())) { // is free trial
          return false;
        }
      }
    }
    return true;
  }

  private List<Device> getFreeTrialEligibleCameras(boolean waitingDeviceList) {
    List<Device> allDevices = DeviceSingleton.getInstance().getDevices();
    if (waitingDeviceList) {
      long startTime = System.currentTimeMillis();
      while ((allDevices == null || allDevices.isEmpty()) // device list is not available
          && System.currentTimeMillis() - startTime < 60 * 1000) { // wait time is not over 60s
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        allDevices = DeviceSingleton.getInstance().getDevices();
      }
    }
    if (allDevices == null || allDevices.isEmpty()) {
      return new ArrayList<>();
    }
    List<Device> filterDevices = new ArrayList<>();
    for (Device device: allDevices) {
      if (device.getProfile() != null && TextUtils.isEmpty(device.getProfile().getParentId()) // is camera
          //&& device.isAvailableRemotely() // is online //Pragnya : not correct method to check if device is online
          && device.getProfile().free_trial_quota > 0 // has quota left
          && (TextUtils.isEmpty(device.getProfile().getPlanId()) ||PublicDefineGlob.FREEMIUM.equals(device.getProfile().getPlanId())) // plan id is null or freemium
          ) {
        filterDevices.add(device);
      }
    }
    return filterDevices;
  }*/

  private void sendUnregistrationToBackend()
  {
    String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
    long appId = settings.getLong(PublicDefineGlob.PREFS_PUSH_NOTIFICATION_APP_ID, -1);

    Log.d(TAG, "unregister notification, app id: " + appId);
    if (appId != -1 && apiKey != null)
    {
      GCMManager.getInstance(this).unregisterGCM(apiKey,(int)appId);
      FirebaseManager.getInstance(this).stopNotificationService();
    }

    Log.d(TAG, "Finish unregister notification, app id: " + appId);
  }

  public void onUserLogout() {

    if(isNestSmokeServiceRunning(SmokeService.class)){
      Log.d(TAG,"Stopping Smoke service ");
      stopService(new Intent(MainActivity.this, SmokeService.class));

    }

    final Dialog unregisteringDialog = HubbleDialogFactory.createProgressDialog(MainActivity.this, getString(R.string.logging_out), false, false);

    try
    {
      unregisteringDialog.show();
    }
    catch (Exception e)
    {
    }

    AsyncPackage.doInBackground(new Runnable()
    {
      @Override
      public void run()
      {
        // unregister notification first
        sendUnregistrationToBackend();

        runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            Api.getInstance().deleteDatabase();

           // DeviceSingleton.getInstance().clearDevices();

            boolean offlineMode = settings.getBoolean(PublicDefineGlob.PREFS_USER_ACCESS_INFRA_OFFLINE, false);

            if (!offlineMode)
            {
              // vox service should not take wakelock
              settings.putBoolean(PublicDefineGlob.PREFS_VOX_SHOULD_TAKE_WAKELOCK, false);
              // remove password when user logout
              boolean notAuto = settings.getBoolean(CommonConstants.shouldNotAutoLogin, true);
              if (notAuto) {
                settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
              } else {
                settings.remove(CommonConstants.shouldNotAutoLogin);
                settings.putBoolean(PublicDefineGlob.REMEBER_PASS, true);
              }
              // Remove offline data.
              new SetupDataCache().clear_session_data(getExternalFilesDir(null));

              // Remove all pending notification on Status bar
              NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
              notificationManager.cancelAll();

              Intent new_login = new Intent(MainActivity.this, LaunchScreenActivity.class);
              new_login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              startActivity(new_login);

	            appLogoutTime =System.currentTimeMillis() - appLogoutStartTime;
	            int time =(int) appLogoutTime / 1000;
	            String logoutTime;
	            if(time<=1){
		            logoutTime = " 1 sec";
	            }else if(time>1 && time<=3){
		            logoutTime = " 3 sec";
	            }else if(time>3 && time<=5){
		            logoutTime = " 5 sec";
	            }else if(time>5 && time<=10){
		            logoutTime = " 10 sec";
	            }else{
		            logoutTime = ">10 sec";
	            }

	            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.LOGOUT_TIME+" : "+ logoutTime,AppEvents.LOGOUT_TIME);
	            ZaiusEvent logoutTimeEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
	            logoutTimeEvt.action(AppEvents.LOGOUT_TIME+" : "+ logoutTime);
	            try {
		            ZaiusEventManager.getInstance().trackCustomEvent(logoutTimeEvt);
	            } catch (ZaiusException e) {
		            e.printStackTrace();
	            }
	            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN, AppEvents.DM_LOGOUT + " : " + AppEvents.SUCCESS, AppEvents.APPLOGOUT);
	            ZaiusEvent logoutEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
	            logoutEvt.action(AppEvents.DM_LOGOUT + " : " + AppEvents.SUCCESS);
	            try {
		            ZaiusEventManager.getInstance().trackCustomEvent(logoutEvt);
	            } catch (ZaiusException e) {
		            e.printStackTrace();
	            }
            }

            try {

              unregisteringDialog.dismiss();
            }
            catch (Exception e)
            {
            }

            if (P2pSettingUtils.hasP2pFeature()) {
              // When user logged out, stop p2p service if it's running
              P2pUtils.stopP2pService(MainActivity.this);
            }

            setResult(CommonConstants.SHOULD_EXIT_NOW_YES);
           /* settings.putBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, false);
            settings.putBoolean(PublicDefineGlob.PREFS_SHOULD_TURN_ON_WIFI, false);
            settings.remove(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT);*/
            finish();
          }
        });
      }
    });

  }

  protected void goToDebugFragment() {
    debugFragment = new DebugFragment();
    switchToFragment(debugFragment, true);
  }

  private void switchToFragment(Fragment frag) {
    if (frag != null) {
      if (!isActivityDestroyed) {
        switchToFragment(frag, true);
      }
    }
  }

  /*public void switchToCameraSettingsFragment() {
    switchToFragment(cameraSettingFragment);
  }*/
  public void switchToCameraSettingsActivity(){
    Intent cameraSettingsIntent = new Intent(MainActivity.this,CameraSettingsActivity.class);
    startActivity(cameraSettingsIntent);
  }

  private void switchToFragment(Fragment frag, boolean addToBackStack) {
    if (!isActivityDestroyed && frag != null) {
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      //fragmentTransaction.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
      fragmentTransaction.replace(R.id.main_view_holder, frag);
      if (addToBackStack) {
        fragmentTransaction.addToBackStack("back_fragment");
      }
      fragmentTransaction.commitAllowingStateLoss();
    }
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();
    switch (id) {
      case R.id.logout:
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        // set title
        alertDialogBuilder.setTitle(R.string.AccountSettingFragment_logout_confirmation);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    // must call unregister before clear preference
                    // unregisterPushNotification();
                    /*int appId = SharedPreferenceManager.getInstance().getInt(GCMManager.APP_ID, -1);
                    Log.i(TAG, "unregister app id: " + appId);
                    if (appId != -1) {
                      GCMManager.getInstance(DashBoardActivity.this).unregisterGCM(apiKey, appId);
                    }*/
	                appLogoutStartTime = System.currentTimeMillis();
                    AnalyticsInterface.getInstance().trackEvent(AppEvents.LOGOUT,AppEvents.LOGOUT,eventData);
                      mAppExitHandler.postDelayed(mAppExitRunnable, 0);
                    onUserLogout();
                    finish();
                  }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {

                    dialog.cancel();
                  }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        break;
      case R.id.change_password:
        Intent changePassword = new Intent(MainActivity.this, ChangePasswordActivity.class);
        startActivity(changePassword);
        AnalyticsInterface.getInstance().trackEvent(AppEvents.CHANGEPASSWORD,AppEvents.CHANGE_PASSWORD_CLICKED,eventData);
        break;
      case R.id.manage_plan:
        /*Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(R.string.hubble_web_app_url)));
        startActivity(intent);*/
        Intent intent = new Intent(this, ManagePlanActivity.class);
        startActivity(intent);
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.ACCOUNT_MANAGEMENT,AppEvents.DM_MANAGE_PLAN_CLICKED,AppEvents.MANAGE_PLAN);
        ZaiusEvent managePlanEvt = new ZaiusEvent(AppEvents.ACCOUNT_MANAGEMENT);
        managePlanEvt.action(AppEvents.DM_MANAGE_PLAN_CLICKED);
        try {
          ZaiusEventManager.getInstance().trackCustomEvent(managePlanEvt);
        } catch (ZaiusException e) {
          e.printStackTrace();
        }
        break;

      /*case R.id.apply_plan:
        Intent intentApplyPlan = new Intent(this, ApplyPlanActivity.class);
        startActivity(intentApplyPlan);
        break;*/
      /*case R.id.try_us_for_free:
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.get_free_trial_eligible_camera));
        progressDialog.show();
        new Thread(new Runnable() {
          @Override
          public void run() {
            final List<Device> filterDevices = getFreeTrialEligibleCameras(false);
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                  try {
                    progressDialog.dismiss();
                  } catch (Exception e) {
                  }
                }
                if (filterDevices != null && filterDevices.size() > 0) {
                  AskUserApplyFreeTrialDialog askUserApplyFreeTrialDialog = new AskUserApplyFreeTrialDialog();
                  askUserApplyFreeTrialDialog.setDeviceList(filterDevices);
                  askUserApplyFreeTrialDialog.setIgnoreCancel(true);
                  askUserApplyFreeTrialDialog.show(getFragmentManager(), "ask_user_apply_free_trial_dialog");
                } else {
                  Toast.makeText(MainActivity.this, R.string.there_is_no_camera_eligible_for_free_trial, Toast.LENGTH_SHORT).show();
                }
              }
            });
          }
        }).start();
        break;*/
      case R.id.support:
        Intent support = new Intent(Intent.ACTION_VIEW);
        support.setData(Uri.parse("http://www.hubbleconnected.support/"));
        startActivity(support);
        AnalyticsInterface.getInstance().trackEvent(AppEvents.SUPPORT_LINK,AppEvents.SUPPORT_CLICKED,eventData);

        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.ACCOUNT_MANAGEMENT,AppEvents.DM_SUPPORT_CLICKED,AppEvents.SUPPORT);
        ZaiusEvent supportPlanEvt = new ZaiusEvent(AppEvents.ACCOUNT_MANAGEMENT);
        supportPlanEvt.action(AppEvents.DM_SUPPORT_CLICKED);
        try {
          ZaiusEventManager.getInstance().trackCustomEvent(supportPlanEvt);
        } catch (ZaiusException e) {
          e.printStackTrace();
        }
        break;
      case R.id.privacy_policy:

        Intent privacy = new Intent(Intent.ACTION_VIEW);
        privacy.setData(Uri.parse("https://hubbleconnected.com/app-policy"));
        startActivity(privacy);
        AnalyticsInterface.getInstance().trackEvent(AppEvents.PRIVACY_LINK,AppEvents.PRIVACY_POLICY_CLICKED,eventData);

        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.ACCOUNT_MANAGEMENT,AppEvents.DM_PRIVACY_CLICKED,AppEvents.PRIVACY);
        ZaiusEvent privacyPolicyEvt = new ZaiusEvent(AppEvents.ACCOUNT_MANAGEMENT);
        privacyPolicyEvt.action(AppEvents.DM_PRIVACY_CLICKED);
        try {
          ZaiusEventManager.getInstance().trackCustomEvent(privacyPolicyEvt);
        } catch (ZaiusException e) {
          e.printStackTrace();
        }
        break;
        case R.id.licenses:

            Intent license = new Intent(Intent.ACTION_VIEW);
            license.setData(Uri.parse("https://hubbleconnected.com/open-source#android"));
            startActivity(license);
            AnalyticsInterface.getInstance().trackEvent("licenseLink","license_clicked",eventData);

	        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.ACCOUNT_MANAGEMENT,AppEvents.DM_LICENSE_CLICKED,AppEvents.LICENSE);
	        ZaiusEvent licenseEvt = new ZaiusEvent(AppEvents.ACCOUNT_MANAGEMENT);
	        licenseEvt.action(AppEvents.DM_LICENSE_CLICKED);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(licenseEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }
            break;

      case R.id.work_with_nest:
        NestToken mToken = com.nest.common.Settings.loadAuthToken(this);
        if (mToken == null) {
          Intent getStarted = new Intent(MainActivity.this, GetStartedActivity.class);
          startActivity(getStarted);
        } else {
          // Launch nest dashboard
          Intent nestDevices = new Intent(MainActivity.this, NestHomeActivity.class);
          startActivity(nestDevices);
        }

        break;

      case R.id.app_settings:
        Intent appSettingIntent=new Intent(MainActivity.this, ApplicationSettingsActivity.class);
        startActivity(appSettingIntent);
        break;

      case R.id.smart_nursery:
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_open_smart_nursery, null);
        new AlertDialog.Builder(this).setView(view)
                .setPositiveButton(R.string.yes_open_app, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    openSmartNursery();
                  }
                }).setNeutralButton(R.string.Cancel, null).show();
        break;
    }
    drawerLayout.closeDrawer(GravityCompat.START);
    return true;
  }

 /* private class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(@NotNull AdapterView parent, @NotNull View view, int position, long id) {
      drawerAdapter.setSelectedPosition(position);
      DrawerItemAdapter adapter = (DrawerItemAdapter) parent.getAdapter();
      DrawerItemModel model = adapter.getModelAt(id);
      if (model != null) {
        switch (model.menuItemType) {
          case ACCOUNT:
            // Stop stream as soon as possible to intterupt p2p command if any
            Log.d(TAG, "onDrawerItemClicked: account -> innterrupt stream immediately");
            stopStreamingImmediately();
            switchToFragment(accountSettingFragment);
            break;
          case CAMERA_LIST:
            // Stop stream as soon as possible to intterupt p2p command if any
            Log.d(TAG, "onDrawerItemClicked: cameras -> innterrupt stream immediately");
            stopStreamingImmediately();
            if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
              switchToFragment(cameraListFragment);
            } else {
              switchToFragment(deviceTabSupportFragment);
              deviceTabSupportFragment.switchToTab(0);
            }
            getSupportFragmentManager().executePendingTransactions();

            break;
          case PATROL: {
            if (patrollingItemFragment != null) {
              switchToFragment(patrollingItemFragment);
            }
            break;
          }
          case HELP:
            if (helpFragment != null) {
              switchToFragment(helpFragment);
            }
            break;
          case EVENT_LOG: {
            //goToEventLog();
            break;
          }
          case VIDEO:
            //TODO ARUNA this to be removed
           // Intent intent = new Intent(MainActivity.this, RecordVideoBrowserActivity.class);
           // startActivity(intent);
            break;
          case TRY_US_FOR_FREE:
            final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.get_free_trial_eligible_camera));
            progressDialog.show();
            new Thread(new Runnable() {
              @Override
              public void run() {
                final List<Device> filterDevices = getFreeTrialEligibleCameras(false);
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    if (progressDialog != null && progressDialog.isShowing()) {
                      try {
                        progressDialog.dismiss();
                      } catch (Exception e) {
                      }
                    }
                    if (filterDevices != null && filterDevices.size() > 0) {
                      AskUserApplyFreeTrialDialog askUserApplyFreeTrialDialog = new AskUserApplyFreeTrialDialog();
                      askUserApplyFreeTrialDialog.setDeviceList(filterDevices);
                      askUserApplyFreeTrialDialog.setIgnoreCancel(true);
                      askUserApplyFreeTrialDialog.show(getFragmentManager(), "ask_user_apply_free_trial_dialog");
                    } else {
                      Toast.makeText(MainActivity.this, R.string.there_is_no_camera_eligible_for_free_trial, Toast.LENGTH_SHORT).show();
                    }
                  }
                });
              }
            }).start();
            break;
          case SEPARATOR:
            break;
          case DEVICE:
            break;
          default:
            break;
        }

        drawerLayout.closeDrawers();
      }
    }
  }*/

  /*public void stopStreamingImmediately() {
    if (videoViewFragment != null) {
      videoViewFragment.stopLiveFragmentStreamingBlocked();
    }
  }*/

  public void goToEventLog() {
    notDisplayOverlaySensorEvents();
    //eventLogFragment = new EventLogFragmentJava();
    List<Device> mAvailableCameras = DeviceSingleton.getInstance().getDevices();
    if (mAvailableCameras != null && mAvailableCameras.size() > 0) {
      if(deviceTabSupportFragment != null){
        deviceTabSupportFragment.setIsNotificationTab(true);
      }
    } else {
      Toast.makeText(this, getString(R.string.no_devices_for_event_log_to_view), Toast.LENGTH_SHORT).show();
    }
  }
  public void goToEventSummary(Device device) {
    Intent intent=new Intent(MainActivity.this,ViewFinderActivity.class);
    DeviceSingleton.getInstance().setSelectedDevice(device);
    intent.putExtra(CommonConstants.VIEW_FINDER_LAUNCH_TAG,CommonConstants.VIEW_FINDER_GOTO_SUMMARY);
    intent.putExtra("reg_id",device.getProfile().registrationId);
    startActivity(intent);
  }


  public void goToEventLog(Device device) {
    notDisplayOverlaySensorEvents();
    Intent intent=new Intent(MainActivity.this,ViewFinderActivity.class);
    DeviceSingleton.getInstance().setSelectedDevice(device);
    intent.putExtra(CommonConstants.VIEW_FINDER_LAUNCH_TAG,CommonConstants.VIEW_FINDER_GOTO_EVENT);
    intent.putExtra("reg_id",device.getProfile().registrationId);
    startActivity(intent);

    /*eventLogFragment = new EventLogFragmentJava();
    eventLogFragment.setUseSelectedDevice(true);
    switchToFragment(eventLogFragment);*/
  }

  public void gotoGallery(){
    if(deviceTabSupportFragment != null){
      deviceTabSupportFragment.setIsGalleryTab(true);
    }
  }

  public void goToPlan(){
    if(deviceTabSupportFragment != null){
      deviceTabSupportFragment.setIsPlanTab(true);
    }
  }

  public void goToRGBFragment(Device device) {
    RGBFragment fragment = new RGBFragment();
    fragment.setmMacAddress(device.getProfile().macAddress);
    switchToFragment(fragment);
  }

  public void goToCameraEditEventLog(Device device) {
    notDisplayOverlaySensorEvents();
    DeviceSingleton.getInstance().setSelectedDevice(device);
    /*eventLogFragment = new EventLogFragmentJava();
    eventLogFragment.setUseSelectedDevice(true);
    switchToFragment(eventLogFragment);*/
  }

  public void goToCameraEventLog(Device device) {
    notDisplayOverlaySensorEvents();
    DeviceSingleton.getInstance().setSelectedDevice(device);
    //isVideoViewFragment = true;
    invalidateOptionsMenu();
    cameraSensorEventLogFragment = new CameraSensorEventLogFragment();
    cameraSensorEventLogFragment.setmSelectedCamera(device);
    switchToFragment(cameraSensorEventLogFragment);
  }

  /*public void switchToPatrolFragment() {
    if (patrollingItemFragment != null) {
      switchToFragment(patrollingItemFragment);
    }
  }*/

  /*public void switchToAddDeviceFragment() {
    mAddDeviceFragment = new AddDeviceFragment();
    if (mAddDeviceFragment != null) {
      switchToFragment(mAddDeviceFragment);
    }
  }*/

  public void displayOverlayAddSensor() {
    overlayAddSensorsLayout.setVisibility(View.GONE);
    overlayImageAddSensors.setVisibility(View.GONE);

    overlayAddSensorsLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        overlayAddSensorsLayout.setVisibility(View.GONE);
        overlayImageAddSensors.setVisibility(View.GONE);
      }
    });
  }

  public void displayOverlaySensorEvents(String sensorType) {
    overlayAddSensorsLayout.setVisibility(View.GONE);
    overlayImageSensorEvents.setVisibility(View.GONE);

    switch (sensorType) {
      case SensorConstants.PRESENCE_DETECTION:
        overlayImageSensorEvents.setImageResource(R.drawable.overlay_smallpresenceactive);
        break;
      case SensorConstants.MOTION_DETECTION:
        overlayImageSensorEvents.setImageResource(R.drawable.overlay_smalldoormotionactive);
        break;
    }

    overlayAddSensorsLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        overlayAddSensorsLayout.setVisibility(View.GONE);
        overlayImageSensorEvents.setVisibility(View.GONE);
      }
    });
  }

  public void notDisplayOverlaySensorEvents() {
    if (overlayAddSensorsLayout != null) {
      overlayAddSensorsLayout.setVisibility(View.GONE);
      overlayImageSensorEvents.setVisibility(View.GONE);
      overlayImageAddSensors.setVisibility(View.GONE);
    }
  }

  public void setDeviceType(boolean isSensorDevice) {
    this.isSensorDevice = isSensorDevice;
  }

  public boolean getDeviceType() {
    return isSensorDevice;
  }


  public boolean isRefreshing() {
    return isRefreshing;
  }

  public void setIsRefreshing(boolean isRefreshing) {
    this.isRefreshing = isRefreshing;
  }

  private static final String LAST_CAMERA_DISCOVER_TIME_STAMP = "LAST_CAMERA_DISCOVER_TIME_STAMP";
  private static final long DISCOVER_TIME_SPAN = 30 * 1000;
  private HubbleApplication App = HubbleApplication.AppContext;

  private boolean needDiscoverLocalCamera() {
    long last_camera_discover_timestamp = App.getLong(LAST_CAMERA_DISCOVER_TIME_STAMP, 0);
    long time_span = System.currentTimeMillis() - last_camera_discover_timestamp;
    if (time_span > DISCOVER_TIME_SPAN) {
      return true;
    }
    return false;
  }

  private Runnable refreshCameraListRunnable = new Runnable() {
    @Override
    public void run() {

      Bundle extra = getIntent().getExtras();
      Boolean skip_server_sync = false;
      if (extra != null) {
        skip_server_sync = extra.getBoolean(EXTRA_FLAG_SKIP_SERVER_SYNC, false);
      }
      if (skip_server_sync == false) {
        Log.d(TAG, "MainActivity:  DO  SERVER SYNC ");
        try {
          DeviceSingleton.getInstance().update(false).get();
        } catch (Exception e) {
        }
      } else {

        Log.d(TAG, "MainActivity: 3 SKIPPPP SERVER SYNC ");
        // extra.putBoolean(EXTRA_FLAG_SKIP_SERVER_SYNC, false );
        getIntent().removeExtra(EXTRA_FLAG_SKIP_SERVER_SYNC);

      }

      BonjourScan.IBonjourScanCompleted bonjourScanCallback = new BonjourScan.IBonjourScanCompleted() {
        @Override
        public void onBonjourScanCompleted(List<CameraBonjourInfo> cameraBonjourInfoList) {
          LogZ.i("Camera scan completed");
          App.set(LAST_CAMERA_DISCOVER_TIME_STAMP, System.currentTimeMillis());
          for (CameraBonjourInfo cameraBonjourInfo : cameraBonjourInfoList) {
            LogZ.i("Camera IP -> %s, MAC -> %s",
                cameraBonjourInfo.getIp(), cameraBonjourInfo.getMac());
            if (cameraBonjourInfo.getIp() != null && !cameraBonjourInfo.getIp().isEmpty()) {
              Device device = DeviceSingleton.getInstance().getDeviceByMAC(cameraBonjourInfo.getMac());
              if (device != null) {
                LogZ.i("Found camera %s in local mode.", device.getProfile().getName());
                device.setIsAvailableLocally(true);

                // If local ip has changed, app need to update device communicator also.
                if (device.getProfile().getDeviceLocation() != null && device.getProfile().getDeviceLocation().getLocalIp() != null) {
                  String oldLocalIp = device.getProfile().getDeviceLocation().getLocalIp();
                  if (!oldLocalIp.equalsIgnoreCase(cameraBonjourInfo.getIp())) {
                    Log.i(TAG, "Local IP has changed, device: " + device.getProfile().getRegistrationId() +
                        ", old local ip: " + oldLocalIp + ", new local ip: " + cameraBonjourInfo.getIp());
                    device.getProfile().getDeviceLocation().setLocalIp(cameraBonjourInfo.getIp());
                    DeviceSingleton.getInstance().updateDevice(device);
                  } else {
                    //Log.i(TAG, "oldLocalIp is equal cameraBonjourInfo ip");
                  }
                } else {
                  Log.i(TAG, "Old device location null -> force update, device " + device.getProfile().getRegistrationId() +
                      ", new local ip: " + cameraBonjourInfo.getIp());
                  device.getProfile().getDeviceLocation().setLocalIp(cameraBonjourInfo.getIp());
                  DeviceSingleton.getInstance().updateDevice(device);
                }

              } //if (device != null)
              else {
                //Log.i(TAG, "Device null -> do nothing");
              }
            } else { // cameraBonjourInfo is not null and empty
              //Log.i(TAG, "Camera Bonjour info is null or empty");
            }
          } // for loop
        }

        @Override
        public void onBonjourScanCancelled() {
          Log.d(TAG, "Camera scan cancelled");
        }
      };
      cameraBonjourScan = new BonjourScan(MainActivity.this, bonjourScanCallback, true);
      LogZ.i("Start discovery local camera");
      new Thread(cameraBonjourScan).start();

    }
  };


  private void discoverLocalCamera() {
    //SYnc from server & then run Bonjour on a separate thread
    Log.i(TAG, "Start local discovery");
    new Thread(refreshCameraListRunnable).start();

  }

  //Pragnya to check and delete in other places
  public boolean isStreamingViaLocal() {
    boolean isInLocal = false;
    /*if (videoViewFragment != null && videoViewFragment.isVisible()) {
      isInLocal = videoViewFragment.isLocalStreaming();
    }*/
    return isInLocal;
  }

  // Code to manage Service lifecycle.
  private final ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
      mBleService = ((BluetoothLeService.LocalBinder) service).getService();
      if (!mBleService.initialize()) {
        finish();
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      mBleService = null;
    }
  };

  public BluetoothLeService getBLEService() {
    return mBleService;
  }

  public void bindBLeService() {
    Intent intent = new Intent(this, BluetoothLeService.class);
    bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
  }

  public void unbindBleService() {
    unbindService(mServiceConnection);
  }

  /* Region showcase of EventLogFragment */

  private ShowcaseView showcaseView;

  private Handler mHandler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(Message message) {
      int what = message.what;
      if (what == HANDLER_KEY_SHOWCASE_PULL_REFRESH) {
        // showcasePullToRefresh();
      } else if (what == HANDLER_KEY_SHOWCASE_SWIPE_DELETE) {
        // showcaseSwipeRightToDelete();
      }
      return false;
    }
  });

  private void showcasePullToRefresh() {
    if (showcaseView != null) {
      return;
    }
    View listView = findViewById(R.id.event_log_listview);
    if (listView == null) {
      return;
    }
    showcaseView = new ShowcaseView.Builder(this).setTarget(Target.NONE)
        .withMaterialShowcase().blockTouchHoldScreen()
        .setContentTitle(getString(R.string.showcase_pull_down_to_refresh))
        .build();
    showcaseView.setButtonText(getString(R.string.got_it));
    showcaseView.overrideButtonClick(new ShowcaseView.Listener() {
      @Override
      public void onGotIt() {
        ((ViewGroup) findViewById(android.R.id.content)).removeView(showcaseView);
        showcaseView = null;
        settings.putBoolean(PREF_SHOWCASE_PULL_REFRESH, true);
        showShowcaseView(HANDLER_KEY_SHOWCASE_SWIPE_DELETE);
      }
    });

    int[] point = new int[2];
    listView.getLocationOnScreen(point);

    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);

    int xPos = metrics.widthPixels / 2;
    showcaseView.animateGesture(xPos, point[1], xPos, point[1] + 256);
  }

  private void showcaseSwipeRightToDelete() {
    if (showcaseView != null) {
      return;
    }
    ListView listView = (ListView) findViewById(R.id.event_log_listview);
    if (listView == null) {
      return;
    }
    View view = listView.getChildAt(0);
    if (view == null) {
      return;
    }
    ViewTarget target = new ViewTarget(view, this);
    Point point = target.getPoint();
    if (point.x == Const.NULL_POINT || point.y == Const.NULL_POINT) {
      return;
    }
    showcaseView = new ShowcaseView.Builder(this).setTarget(target)
        .withMaterialShowcase().blockTouchHoldScreen()
        .setContentTitle(getString(R.string.showcase_swipe_right_to_delete))
        .build();
    showcaseView.setButtonText(getString(R.string.got_it));
    showcaseView.overrideButtonClick(new ShowcaseView.Listener() {
      @Override
      public void onGotIt() {
        ((ViewGroup) findViewById(android.R.id.content)).removeView(showcaseView);
        showcaseView = null;
        settings.putBoolean(PREF_SHOWCASE_SWIPE_DELETE, true);
      }
    });
    showcaseView.animateGesture(24, point.y + 24, 280, point.y + 24);
  }

  private void showShowcaseView(int key) {
    mHandler.removeCallbacksAndMessages(null);
    mHandler.sendEmptyMessageDelayed(key, SHOWCASE_DELAY_TIME);
  }

  public boolean isOfflineMode() {
    return isOfflineMode;
  }

  private void registerWithGCM() {

      GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
      int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

      if (ConnectionResult.SUCCESS != resultCode) {
          Log.i(TAG, "Register with GCM result code " + resultCode);
          if (apiAvailability.isUserResolvableError(resultCode)) {
              Log.i(TAG, "Register with GCM Failed");
              UpdateGooglePlayServicesDialog();

          } else {
              GooglePlayServicesNotSupported();

          }
      } else {
          if (settings.getInt(PublicDefineGlob.PREFS_PUSH_NOTIFICATION_APP_ID, -1) == -1) {
              Log.i(TAG, "Register with GCM Success");
              final String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

              if (apiKey != null) {
                  String appName = getPackageName();
                  String deviceCode = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                  String appVersion = "1.0";
                  PackageInfo pInfo = null;
                  try {
                      pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                      appVersion = pInfo.versionName;
                  } catch (PackageManager.NameNotFoundException e) {
                      e.printStackTrace();
                  }
                  Log.i(TAG, "Register with GCM params " + "device code :" + deviceCode);

                  GCMManager.getInstance(this).registerGCM(apiKey, appName, deviceCode, appVersion, getString(R.string.gcm_project_name), getString(R.string.gcm_sender_id));
                  registerFirebase();
              }
          }
      }
  }


  private void registerFirebase(){
        FirebaseManager.getInstance(this).startNotificationService();
      }


    private void UpdateGooglePlayServicesDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder
                .setTitle(getString(R.string.update_google_play_service))
                .setMessage(getString(R.string.update_google_play_service_content))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        String LINK_TO_GOOGLE_PLAY_SERVICES = "play.google.com/store/apps/details?id=com.google.android.gms&hl=en";
                        try {
                            startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://" + LINK_TO_GOOGLE_PLAY_SERVICES)), PLAY_SERVICES_RESOLUTION_REQUEST);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + LINK_TO_GOOGLE_PLAY_SERVICES)), PLAY_SERVICES_RESOLUTION_REQUEST);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        Button noButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        noButton.setTextColor(getResources().getColor(R.color.text_blue));
        noButton.setAllCaps(false);

        Button yesButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        yesButton.setTextColor(getResources().getColor(R.color.text_blue));
        yesButton.setAllCaps(false);
    }


    private void GooglePlayServicesNotSupported() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        alertDialogBuilder
                .setTitle(getString(R.string.not_supported_google_play_service))
                .setMessage(getString(R.string.device_not_support_google_service))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        Button yesButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);

        yesButton.setTextColor(getResources().getColor(R.color.text_blue));
        yesButton.setAllCaps(false);
    }

  private static int getAppVersion(Context context) {
    try {
      PackageInfo packageInfo = context.getPackageManager()
              .getPackageInfo(context.getPackageName(), 0);
      return packageInfo.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException("Could not get package name: " + e);
    }
  }


  private void sendRegistrationIdToBackend() {
    String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
    String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    Log.d(TAG, "Android id: " + android_id);
    if (regid != null) {
      PackageInfo pInfo = null;
      try {
        pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      } catch (PackageManager.NameNotFoundException e) {
        Log.e(TAG, Log.getStackTraceString(e));
      }

      String software_version = null;
      if (pInfo == null) {
        software_version = "00.00";
      } else {
        software_version = pInfo.versionName.replace("(", "").replace(")", "").trim();
      }

      try {
        Models.ApiResponse<Models.Registration> registerResponse =
                Api.getInstance().getService().registerApp(apiKey, android.os.Build.MODEL, android_id, software_version);

        if (registerResponse != null && registerResponse.getStatus().equals("200")) {
          String appUniqueId = getString(R.string.gcm_project_name);
          Api.getInstance().getService().registerNotifications(apiKey, registerResponse.getData().getId(), "gcm", regid, appUniqueId);

          settings.putLong(PublicDefineGlob.PREFS_PUSH_NOTIFICATION_APP_ID, registerResponse.getData().getId());
          Log.d(TAG, "Successfully registered for GCM notifications");
        }
      } catch (Exception e) {
        Log.e(TAG, "Error registering for notifications: " + Log.getStackTraceString(e));
      }
    }
  }

  private void storeRegistrationId(Context context, String regId) {
    int appVersion = getAppVersion(context);
    Log.i(TAG, "Saving regId on app version " + appVersion);
    settings.putString(PROPERTY_REG_ID, regId);
    settings.putInt(PROPERTY_APP_VERSION, appVersion);
  }

  protected void setProfilePic(String profilePic) {
    try {
      if (!TextUtils.isEmpty(profilePic)) {
        final Bitmap imgBmp = bitmapProfileImage(profilePic);
        if (imgBmp != null) {
          mUserImage.setImageBitmap(imgBmp);
        }
      }
    } catch (Exception exp) {
      exp.getMessage();
    }
  }
  public class BitmapProcessTask extends AsyncTask<Void, Void, Boolean> {
    Uri pathUri;
    Bitmap imgBmp;

    public BitmapProcessTask(Uri uri) {
      pathUri = uri;
    }

    protected Boolean doInBackground(Void... params) {
      final String[] proj = {MediaStore.Images.Media.DATA};
      final Cursor cursor = managedQuery(pathUri, proj, null, null, null);
      final int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      cursor.moveToLast();
      mProfilePath = cursor.getString(column_index);
      Log.d("path for image", "IMAGE PATH =" + mProfilePath);

      if (!TextUtils.isEmpty(mProfilePath)) {
        imgBmp = bitmapProfileImage(mProfilePath);
        if (imgBmp != null) {
          mPrefMangerPersist.clear();
          mPrefMangerPersist.putString(sharedpreferences.getString(CommonConstants.ACCOUNT_PROFILE_IMAGE, ""), mProfilePath);
        }
      }

      return true;
    }

    protected void onPostExecute(Boolean result) {
      if (mUserImage != null && imgBmp != null) {
        mUserImage.setImageBitmap(imgBmp);
      }
      if (sharedpreferences.getString(CommonConstants.ACCOUNT_PROFILE_ID, "").equals("")) {
        registerUserProfile();
      } else {
        compressImageAndUpload();
      }
    }
  }

  public void registerUserProfile() {
    String name = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, "");
    RegisterProfile registerProfile = new RegisterProfile(apiKey);
    registerProfile.setName(name);
    registerProfile.setDOB("1990-04-16");
    registerProfile.setGender("male");
    HashMap<String, String> profileSettings = new HashMap<>();
    profileSettings.put("IS_ACCOUNT_PROFILE", String.valueOf(1));
    registerProfile.setProfileSettings(profileSettings);
    ProfileManagerService.getInstance(MainActivity.this).registerProfile(apiKey, registerProfile, new com.android.volley.Response.Listener<RegisterProfileDetails>() {
      @Override
      public void onResponse(RegisterProfileDetails response) {
        Log.d(TAG, "Profile adding success");
        editor.putString(CommonConstants.ACCOUNT_PROFILE_ID, response.getProfileID()).commit();
        compressImageAndUpload();


      }
    }, new com.android.volley.Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "Profile adding Failed" + error.toString());

      }
    });

  }

  public void compressImageAndUpload() {
    if (mProfilePath != null && !TextUtils.isEmpty(mProfilePath)) {
      try {
        Bitmap imgBmp = bitmapImage(mProfilePath);
        ContextWrapper cw = new ContextWrapper(MainActivity.this);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, sharedpreferences.getString(CommonConstants.ACCOUNT_PROFILE_ID, "1") + ".jpg");

        FileOutputStream fos = null;
        try {
          fos = new FileOutputStream(mypath);
          // Use the compress method on the BitMap object to write image to the OutputStream
          imgBmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
          Log.d(TAG, "Profile Image Size" + imgBmp.getByteCount());
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          fos.close();
        }

        mPrefMangerPersist.putString(sharedpreferences.getString(CommonConstants.ACCOUNT_PROFILE_IMAGE, ""), mypath.getAbsolutePath());
        updateProfileImage(mypath.getAbsolutePath());
      } catch (Exception e) {

      }
    }
  }

  public void updateProfileImage(String imagePath) {

    String profileID = sharedpreferences.getString(CommonConstants.ACCOUNT_PROFILE_ID, "1");
    if (imagePath != null && !TextUtils.isEmpty(imagePath)) {
      ProfileImage profileImage = new ProfileImage(apiKey, profileID);

      ProfileManagerService.getInstance(MainActivity.this).uploadProfileImage(imagePath, profileImage, new com.android.volley.Response.Listener<ProfileImageStatus>() {
        @Override
        public void onResponse(ProfileImageStatus response) {
          Log.d(TAG, "Profile Image Update success");
          GeAnalyticsInterface.getInstance().trackEvent(AppEvents.ACCOUNT_MANAGEMENT,AppEvents.DM_USER_IMAGE_CHANGED+" : "+AppEvents.SUCCESS,AppEvents.IMAGE_CHANGED);
          ZaiusEvent imageChangeEvt = new ZaiusEvent(AppEvents.ACCOUNT_MANAGEMENT);
          imageChangeEvt.action(AppEvents.DM_USER_IMAGE_CHANGED+" : "+AppEvents.SUCCESS);
          try {
            ZaiusEventManager.getInstance().trackCustomEvent(imageChangeEvt);
          } catch (ZaiusException e) {
            e.printStackTrace();
          }
        }
      }, new com.android.volley.Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          Log.d(TAG, "Profile Image Update Failed" + error.toString());
          Toast.makeText(getApplicationContext(), "Profile Image Upload Failed", Toast.LENGTH_SHORT).show();

          GeAnalyticsInterface.getInstance().trackEvent(AppEvents.ACCOUNT_MANAGEMENT,AppEvents.DM_USER_IMAGE_CHANGED+" : "+AppEvents.FAILURE+" : "+error.getMessage().toString(),AppEvents.IMAGE_CHANGED);
          ZaiusEvent imageChangefailureEvt = new ZaiusEvent(AppEvents.ACCOUNT_MANAGEMENT);
          imageChangefailureEvt.action(AppEvents.DM_USER_IMAGE_CHANGED+" : "+AppEvents.FAILURE+" : "+error.getMessage().toString());
          try {
            ZaiusEventManager.getInstance().trackCustomEvent(imageChangefailureEvt);
          } catch (ZaiusException e) {
            e.printStackTrace();
          }


        }
      });
    }
  }


  ProfileSynManagerCallback profileSynManagerCallback = new ProfileSynManagerCallback() {
    @Override
    public void onProfileDataChanged(String profileID) {
      if (!isFinishing()) {
        mProfilePath = sharedpreferences.getString(CommonConstants.ACCOUNT_PROFILE_IMAGE, "");
        Log.d(TAG, "Profile Image Update mProfilePath" + mProfilePath);
         if (!TextUtils.isEmpty(mProfilePath)) {
          setProfilePic(mProfilePath);
        }
      }
    }

    @Override
    public void onProfileImageChanged(String profileID) {
        mProfilePath = sharedpreferences.getString(CommonConstants.ACCOUNT_PROFILE_IMAGE, "");
        Log.d(TAG, "Profile Image Update mProfilePath" + mProfilePath);
        if (!TextUtils.isEmpty(mProfilePath)) {
          setProfilePic(mProfilePath);
        }

    }

    @Override
    public void onProfileLoadError() {
      if ( !networkDetector.isConnectingToInternet()) {
        Toast.makeText(MainActivity.this, R.string.internet_connection_down, Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(MainActivity.this, R.string.profile_sync_error, Toast.LENGTH_SHORT).show();
      }

    }
  };

  public Bitmap bitmapImage(String path){
    File imgFile = new File(path);
    try {

      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(new FileInputStream(imgFile), null, o);


      final int REQUIRED_SIZE = 180;

      final int height = o.outHeight;
      final int width = o.outWidth;
      int inSampleSize = 1;

      if (height > REQUIRED_SIZE || width > REQUIRED_SIZE) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) >= REQUIRED_SIZE
                && (halfWidth / inSampleSize) >= REQUIRED_SIZE) {
          inSampleSize *= 2;
        }
      }

      // Decode with inSampleSize
      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = inSampleSize;
      return BitmapFactory.decodeStream(new FileInputStream(imgFile), null, o2);
    } catch (FileNotFoundException e) {}
    return null;
  }

  public Bitmap bitmapProfileImage(String path){
    File imgFile = new File(path);
    Bitmap bitmap = null;
    int orientation = getExifAngel(path);

    try {
      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(new FileInputStream(imgFile), null, o);
      final int REQUIRED_SIZE = 180;
      // Find the correct scale value. It should be the power of 2.
      int scale = 2;
      while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
              o.outHeight / scale / 2 >= REQUIRED_SIZE) {
        scale *= 4;
      }

      // Decode with inSampleSize
      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = scale;
      bitmap = BitmapFactory.decodeStream(new FileInputStream(imgFile), null, o2);
    } catch (FileNotFoundException e){
    }
    if(orientation != 0) {
      return rotateImage(bitmap,orientation);
    }
    return bitmap;
  }

  public Bitmap rotateImage(Bitmap source,int angle) {
    Bitmap retVal;
    Matrix matrix = new Matrix();

    matrix.postRotate(angle);
    retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    source.recycle();
    return retVal;
  }

  public int getExifAngel(String photoPath){
    int orientation = 1;
    try {
      ExifInterface ei = new ExifInterface(photoPath);
      orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

      switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
          return 90;
        case ExifInterface.ORIENTATION_ROTATE_180:
          return 180;
        case ExifInterface.ORIENTATION_ROTATE_270:
          return 270;
      }
    }catch(Exception exp){

    }
    return orientation;
  }


  private boolean isNestSmokeServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  private void checkSmartNurserryApp(){
      MenuItem smartNursery= menu.findItem(R.id.smart_nursery);
      SpannableString spannable_smartNursery = new SpannableString(smartNursery.getTitle());
      spannable_smartNursery.setSpan(new TextAppearanceSpan(this, R.style.Menu_ItemColor), 0, spannable_smartNursery.length(), 0);
      smartNursery.setTitle(spannable_smartNursery);

      if(isAppInstalled("com.hubble.smartnursery"))
          smartNursery.setVisible(true);
      else
          smartNursery.setVisible(false);
  }

  private void checkUserSubscription() {
    if (CommonUtil.isInternetAvailable(this)) {
      HubbleRequest hubbleRequest = new HubbleRequest(apiKey);
      SubscriptionService subscriptionService = SubscriptionService.getInstance(this);
      subscriptionService.getUserSubscriptionPlan(SubscriptionInfo.ServicePlan.MONITORING_SERVICE_PLAN, hubbleRequest, new com.android.volley.Response.Listener<UserSubscriptionPlanResponse>() {
        @Override
        public void onResponse(UserSubscriptionPlanResponse response) {
          if (response == null || response.getStatus() != 200) {
            Log.d(TAG, "Get User Subscriptions failed");//Todo : Error scenario screen
          } else {
            UserSubscriptionPlanResponse.PlanResponse[] userSubscriptions = response.getPlanResponse();
            String userPlan = null;
            if (userSubscriptions != null && userSubscriptions.length > 0) {
              for (UserSubscriptionPlanResponse.PlanResponse item : userSubscriptions) {
                if (PlanFragment.ACTIVE.equals(item.getPlanState()) || PlanFragment.CANCELED.equals(item.getPlanState())) {
                  userPlan = item.getPlanID();
                  break;
                }
              }
            }
            if (!TextUtils.isEmpty(userPlan) && !userPlan.equalsIgnoreCase(PlanFragment.FREEMIUM)) {
              mCurrentPlanString = userPlan;
              mCurrentPlan.setVisible(true);
              mCurrentPlan.setTitle(getString(R.string.current_plan_drawer) + " "  + userPlan);
              setCurrentPlanMenuColor();
            } else {
              mCurrentPlanString = getString(R.string.no_paid_plan);
              mCurrentPlan.setVisible(true);
              mCurrentPlan.setTitle(getString(R.string.current_plan_drawer) + " "  + getString(R.string.no_paid_plan));
              setCurrentPlanMenuColor();
            }
          }
        }
      }, new com.android.volley.Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
      });

    }
  }

  private void setCurrentPlanMenuColor(){
    SpannableString spannable_currentPlan = new SpannableString(mCurrentPlan.getTitle());
    spannable_currentPlan.setSpan(new TextAppearanceSpan(this, R.style.Menu_currentPlan_ItemColor), 0, spannable_currentPlan.length(), 0);
    mCurrentPlan.setTitle(spannable_currentPlan);
  }

  private boolean isAppInstalled(String uri) {
    PackageManager pm = getPackageManager();
    try {
      pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
    }
    return false;
  }

  private void openSmartNursery() {
    try {
      String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
      if (TextUtils.isEmpty(apiKey)) {
        Log.d(TAG, "Cannot switch to smart nursery app. APIKEY IS NULL !!!!!!!!!!!!");
        return;
      }
      Intent intent = getPackageManager()
              .getLaunchIntentForPackage("com.hubble.smartnursery");
      intent.addCategory(Intent.CATEGORY_LAUNCHER);
      intent.putExtra("apiKey", apiKey);
      intent.putExtra("login_email", settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
      intent.putExtra("login_name", settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, ""));
      startActivity(intent);
    } catch (ActivityNotFoundException e) {
      Log.d(TAG, "Exception when open Smart Nursery app, App not found", e);
    } catch (Exception e) {
      Log.d(TAG, "Exception when open Smart Nursery app", e);
    }
  }


}