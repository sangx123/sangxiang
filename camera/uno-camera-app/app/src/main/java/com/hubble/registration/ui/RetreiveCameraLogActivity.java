package com.hubble.registration.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.registration.AnalyticsController;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.SubscriptionWizard;
import com.hubble.registration.Util;
import com.hubble.registration.interfaces.IChangeNameCallBack;
import com.hubble.registration.interfaces.IWifiScanUpdater;
import com.hubble.registration.models.ApScanBase;
import com.hubble.registration.models.CamConfiguration;
import com.hubble.registration.models.CameraBonjourInfo;
import com.hubble.registration.models.CameraWifiEntry;
import com.hubble.registration.models.LegacyCamProfile;
import com.hubble.registration.models.NameAndSecurity;
import com.hubble.registration.models.VtechCamConfiguration;
import com.hubble.registration.tasks.AutoConfigureCameras;
import com.hubble.registration.tasks.AutoConfigureCamerasNew;
import com.hubble.registration.tasks.CameraScanner;
import com.hubble.registration.tasks.CameraScanner.ICameraScanCompleted;
import com.hubble.registration.tasks.ChangeNameTask;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubble.registration.tasks.CheckFirmwareUpdateTask;
import com.hubble.registration.tasks.ConfigureLANCamera;
import com.hubble.registration.tasks.ConnectToNetworkTask;
import com.hubble.registration.tasks.QueryCameraWifiListTask;
import com.hubble.registration.tasks.RemoveDeviceTask;
import com.hubble.registration.tasks.SendCommandAsyncTask;
import com.hubble.registration.tasks.VerifyNetworkKeyTask;
import com.hubble.registration.tasks.WifiScan;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubble.ui.DebugFragment;
import com.hubble.util.CommonConstants;
import com.hubble.util.LogZ;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;
import com.koushikdutta.ion.HeadersCallback;
import com.koushikdutta.ion.HeadersResponse;
import com.koushikdutta.ion.Ion;
import com.msc3.ConnectToNetworkActivity;

import java.io.File;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import base.hubble.IAsyncTaskCommonHandler;
import base.hubble.PublicDefineGlob;
import base.hubble.meapi.Device;
import base.hubble.meapi.PublicDefines;
import base.hubble.meapi.device.AddNewDeviceResponse;

/**
 * @author phung
 */
public class RetreiveCameraLogActivity extends ActionBarActivity implements IWifiScanUpdater, Handler.Callback, IChangeNameCallBack, ICameraScanCompleted {
  private static final String TAG = "SingleCamConfigActivity";
  public static final String str_userToken = "token";

  private static final int ADD_CAM_SUCCESS = 0x1;
  private static final int ADD_CAM_FAILED_UNKNOWN = 0x2;
  private static final int ADD_CAM_FAILED_WITH_DESC = 0x3;
  private static final int ADD_CAM_FAILED_SERVER_DOWN = 0x11;
  public static final String ACTION_TURN_WIFI_NOTIF_ON_OFF = "com.msc3.SingleCamConfigureActivity.ACTION_TURN_OFF_WIFI_NOTIF";
  public static final String WIFI_NOTIF_BOOL_VALUE = "wifi_notif_bool_value";
  public static final boolean TURN_OFF = false;
  public static final boolean TURN_ON = true;

  private static final int SCAN_FOR_CAMERA = 0x100;
  private static final int SCAN_FOR_ALL = 0x101;
  private int wifi_scan_purpose;

  /* Transient data - Only valid during the Configuration process */
  private WifiConfiguration selectedWifiCon;
  private String user_token;
  private CamConfiguration configuration;
  private LegacyCamProfile cam_profile;
  private AnimationDrawable anim = null;
  private AsyncTask<CamConfiguration, String, String> config_task = null;
  private long add_camera_start_time = -1;
  private CameraScanner wifiCameraScanner;
  private CameraScanner mdnsCameraScanner;
  private boolean goToCamLiveView = false;
  private boolean isActivityDestroyed;
  private Dialog verifyKeyDialog;
  private Dialog searchingNetworkDialog;
  private boolean shouldShowWifiScanningInMenu = false;
  private ListView deviceSelectionList;
  private Context mContext;
  private com.hubble.devcomm.Device mDevice;
  private PublicDefine.CameraModel mCameraModelToPair;

  SubscriptionWizard subWizard;

  protected WakeLock wl = null;
  private AtomicBoolean wifiScanning = new AtomicBoolean(false);
  private AtomicBoolean mdnsScanning = new AtomicBoolean(false);

  private boolean shouldContinueQueryWifiList = true;
  private int currentPagePosition = 0;
  private ViewPagerParallax instructionsViewPager;

  private long start_time_from_get_rt_list = 0;
  private int wifi_selected_position;
  private boolean isCameraSelectionStarted = false;
  private CameraAccessPointListAdapter deviceSelectionAdapter;
  private String saved_ssid;
  private boolean isWifiInterfaceAvailable;
  private Object mNetworkCallback = null;
  private SecureConfig settings = HubbleApplication.AppConfig;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;
    if (getApplicationContext().getPackageName().contains("vtech")) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    Bundle extra = getIntent().getExtras();
    if (extra != null) {
      user_token = extra.getString(str_userToken);
      extra.remove(str_userToken);
    }

    /*
     * IMPORTANT: From Android 5.0 (Lollipop), setMobileDataEnabled & getMobileDataEnabled
		 * methods are removed. App can't auto switch mobile network on/off anymore.
		 * Thus, don't expect below code has effect.
		 */
    if (ConnectToNetworkActivity.hasMobileNetwork(this
        .getApplicationContext())) {
      boolean isMobileDataEnabled = ConnectToNetworkActivity
          .isMobileDataEnabled(this.getApplicationContext());
      Log.d(TAG, "Prepare to setup, isMobileNetworkEnabled? "
          + isMobileDataEnabled);
      if (isMobileDataEnabled == true) {
                /* Temporarily turn off mobile data. */
        Log.d(TAG, "Temporarily disable mobile data.");
        ConnectToNetworkActivity.setMobileDataEnabled(
            this.getApplicationContext(), false);
      }
    }

        /*
         * Wifi interface is always available for Android < 5.0
	     * For Android 5.0 or higher, if mobile data is enabled and the wifi has no internet connection
	     * then the wifi interface could be disabled.
	     */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      isWifiInterfaceAvailable = false;
      mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void onAvailable(Network network) {
          Log.i(TAG, "Wifi network request, onAvailable called");
          ConnectivityManager.setProcessDefaultNetwork(network);
          isWifiInterfaceAvailable = true;
        }

        ;
      };
    } else {
      isWifiInterfaceAvailable = true;
    }

    WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    if (wm.getConnectionInfo() != null
        && wm.getConnectionInfo().getSSID() != null) {
      saved_ssid = wm.getConnectionInfo().getSSID();
      Log.d(TAG, "Home wifi SSID: " + saved_ssid);
      settings.putString(PublicDefineGlob.PREFS_HOME_WIFI_SSID_NO_QUOTE,
          convertToNoQuotedString(saved_ssid));
    }

    getApplication().registerActivityLifecycleCallbacks(AnalyticsController.getInstance().getLocalyticsLifecycleCallback());
    setupCameraScan();
  }

  @Override
  protected void onDestroy() {
    stopCameraScanner("wifi");
    stopCameraScanner("mdns");
    isActivityDestroyed = true;
    shouldContinueQueryWifiList = false;

    Intent i = new Intent();
    i.setAction(ACTION_TURN_WIFI_NOTIF_ON_OFF);
    i.putExtra(WIFI_NOTIF_BOOL_VALUE, TURN_ON);
    sendBroadcast(i);
    if (config_task != null && config_task.getStatus() == AsyncTask.Status.RUNNING) {
      config_task.cancel(true);
    }

    stopCameraScanner("wifi");
    stopCameraScanner("mdns");
    check_and_reconnect_to_home();
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    Menu mMenu = menu;
    inflater.inflate(R.menu.select_wifi_menu, menu);
    if (shouldShowWifiScanningInMenu) {
      mMenu.findItem(R.id.menu_refresh_wifi).setVisible(true);
      mMenu.findItem(R.id.new_wifi).setVisible(true);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_refresh_wifi) {
      if (searchingNetworkDialog != null && searchingNetworkDialog.isShowing()) {
        searchingNetworkDialog.dismiss();
      }
      searchingNetworkDialog = getSearchingNetworkDialog();
      searchingNetworkDialog.show();
      WifiScan ws = new WifiScan(RetreiveCameraLogActivity.this, RetreiveCameraLogActivity.this);
      ws.setSilence(true);
      wifi_scan_purpose = SCAN_FOR_ALL;
      ws.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Scan now");
      return true;
    } else if (item.getItemId() == R.id.new_wifi) {
      getAddNetworkDialog().show();
      return true;
    } else if (item.getItemId() == R.id.refresh_camera) {
      startCameraScanner("wifi");
      startCameraScanner("mdns");
      return true;
    } else if (item.getItemId() == android.R.id.home) {
      showLeaveSetupWarning();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  private void showLeaveSetupWarning() {
    new AlertDialog.Builder(this).setTitle(getString(R.string.leave_device_setup))
        .setMessage(getString(R.string.are_you_sure_cancel_device_setup))
        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            finish();
          }
        })
        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        }).show();
  }

  @Override
  public void onBackPressed() {
    showLeaveSetupWarning();
  }

  @Override
  public void scanWasCanceled() {
    // Do nothing
  }

  @Override
  public void updateWifiScanResult(final List<ScanResult> result) {
    switch (wifi_scan_purpose) {
      case SCAN_FOR_CAMERA:
        if (result != null) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              switchToCameraSelection(result, new ArrayList<CameraBonjourInfo>(), "wifi");
              if (searchingNetworkDialog != null && searchingNetworkDialog.isShowing()) {
                searchingNetworkDialog.dismiss();
              }
            }
          });

        }
        break;
      case SCAN_FOR_ALL:
        if (result != null) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              router_selection(result);
              if (searchingNetworkDialog != null && searchingNetworkDialog.isShowing()) {
                searchingNetworkDialog.dismiss();
              }
            }
          });
        }
        break;
      default:
        break;
    }

  }

  @Override
  public void onWifiCameraScanCompleted(final List<ScanResult> results) {
    //Log.d(TAG, "onWifiCameraScanCompleted");
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        switchToCameraSelection(results, new ArrayList<CameraBonjourInfo>(), "wifi");
      }
    });
  }

  @Override
  public void onMdnsCameraScanCompleted(final List<CameraBonjourInfo> results) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        switchToCameraSelection(new ArrayList<ScanResult>(), results, "mdns");
      }
    });
  }

  @Override
  protected void onPause() {
    stopCameraScanner("wifi");
    stopCameraScanner("mdns");
    super.onPause();
  }

  @Override
  public void update_cam_success() {
    settings.putBoolean(PublicDefineGlob.PREFS_IS_FIRST_TIME, false);

    if (goToCamLiveView) {
      settings.putBoolean(PublicDefine.PREFS_SHOULD_GO_TO_CAMERA, true);
    }
    finish();
  }

  @Override
  public void update_cam_failed() {
    // What todo??-- for now skip silencely ..
    // Intent entry = new Intent(RetreiveCameraLogActivity.this,
    // UserLoginActivity.class);
    // entry.putExtra(UserLoginActivity.bool_isLoggedIn, true);
    // entry.putExtra(UserLoginActivity.STR_USER_TOKEN, user_token);
    //Intent entry = new Intent(RetreiveCameraLogActivity.this, SettingsActivity.class);
    //entry.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //startActivity(entry);
    RetreiveCameraLogActivity.this.finish();
  }

  @Override
  public boolean handleMessage(Message arg0) {
    switch (arg0.what) {
      case ConnectToNetworkTask.MSG_CONNECT_TO_NW_DONE:
        beginSetupCamera(null);
        break;
      case ConnectToNetworkTask.MSG_CONNECT_TO_NW_FAILED:
        // Connect to camera network failed OR connect to wifi network
        // failed
        final String checkSSID = (String) arg0.obj;
        //Log.d(TAG, "Connect to network " + checkSSID + " failed");
        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            try {
              // showDialog(CONNECT_TO_CAMERA_NETWORK_FAILED_DIALOG);
              connectToCameraFailed(checkSSID);
            } catch (Exception ignored) {
            }

          }
        });
        break;
      case PublicDefine.MSG_AUTO_CONF_SUCCESS:
        syncDevices();
        break;
      case PublicDefine.MSG_AUTO_CONF_FAILED:
        camera_config_failed(arg0.arg1, (String) arg0.obj);
        break;
      case PublicDefine.MSG_AUTO_CONF_SHOW_ABORT_PAGE:
        showAbortPage();
        break;

      case VerifyNetworkKeyTask.MSG_NETWORK_KEY_VERIFY_FAILED:
        if (verifyKeyDialog == null && verifyKeyDialog.isShowing()) {
          verifyKeyDialog.dismiss();
        }

        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            Crittercism.leaveBreadcrumb("Failed to connect to device. Device UDID is " + cam_profile.getRegistrationId());
            Crittercism.logHandledException(new Throwable());

            setContentView(R.layout.connect_to_camera_failed);
            setupToolbar();
            shouldShowWifiScanningInMenu = false;
            invalidateOptionsMenu();
            TextView txtFailed = (TextView) findViewById(R.id.connectToCameraFailed_txtDescription);
            String desc_str = String.format(getString(R.string.SingleCamConfigureActivity_wifi_key_err));
            if (cam_profile != null && cam_profile.getRegistrationId() != null) {

              desc_str += String.format(getString(R.string.camera_udid), cam_profile.getRegistrationId());
            }
            txtFailed.setText(desc_str);

            Button connect = (Button) findViewById(R.id.cameraSetupFailed_btn_connect);
            connect.setOnClickListener(new OnClickListener() {

              @Override
              public void onClick(View v) {
                showSearchingNetworkDialog();
                WifiScan ws = new WifiScan(RetreiveCameraLogActivity.this, RetreiveCameraLogActivity.this);
                ws.setSilence(true);
                wifi_scan_purpose = SCAN_FOR_ALL;
                ws.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Scan now");
              }
            });
          }
        });

        break;
      case VerifyNetworkKeyTask.MSG_NETWORK_KEY_VERIFY_PASSED:
        if (verifyKeyDialog == null && verifyKeyDialog.isShowing()) {
          verifyKeyDialog.dismiss();
        }
        setContentView(R.layout.connecting_to_wifi);
        setupToolbar();
        shouldShowWifiScanningInMenu = false;
        invalidateOptionsMenu();
        ImageView imgConn = (ImageView) findViewById(R.id.imgConnecting);
        if (imgConn != null) {
          imgConn.setVisibility(View.VISIBLE);
          imgConn.setImageResource(R.drawable.wifi_connecting_anim);
          anim = (AnimationDrawable) imgConn.getDrawable();
          anim.start();
        }

        TextView txtConn = (TextView) findViewById(R.id.txtConnecting);
        txtConn.setText(R.string.connecting_camera_to_wifi);
        TextView txtDesc = (TextView) findViewById(R.id.txtDesc);
        String desc_str = String.format(getResources().getString(R.string.please_wait_for_a_couple_of_minutes_while_camera_connects_to_your_network), cam_profile.getName());
        txtDesc.setText(desc_str);
        store_default_wifi_info();
        AddCameraTask try_addCam = new AddCameraTask(cam_profile);
        String deviceAccessibilityMode = "upnp";
        int offset = TimeZone.getDefault().getRawOffset();
        String timeZone = String.format("%s%02d.%02d", offset >= 0 ? "+" : "-", Math.abs(offset) / 3600000, (Math.abs(offset) / 60000) % 60);
        try_addCam.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, user_token, cam_profile.getName(), cam_profile.getRegistrationId(), cam_profile.getModelId(), deviceAccessibilityMode, cam_profile.getFirmwareVersion(), timeZone);
        break;
      default:
        break;
    }
    return false;
  }

  //Step 1 - tutorial and find camera to pair
  private void initialSetup() {
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      deviceSelectionScreen();
      Crittercism.leaveBreadcrumb("Start VTech device setup");
    } else {
      Crittercism.leaveBreadcrumb("Start device setup");
      setupCameraScan();
    }
  }

  private void deviceSelectionScreen() {
    setContentView(R.layout.device_setup_device_selection);
    findViewById(R.id.camera_setup_buy_button).setVisibility(View.GONE);
    ListView deviceListView = (ListView) findViewById(R.id.deviceSelection_deviceListView);
    final DeviceSelectionAdapter deviceAdapter = new DeviceSelectionAdapter(this);
    deviceListView.setAdapter(deviceAdapter);
    deviceListView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceSelectionAdapter.DeviceSelectionDevice selectedDevice = (DeviceSelectionAdapter.DeviceSelectionDevice) deviceAdapter.getItem(position);
        mCameraModelToPair = selectedDevice.getModel();
        Crittercism.leaveBreadcrumb("Selected to pair " + mCameraModelToPair.name());
        setupCameraScan();
      }
    });

    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();
  }

  private void setupCameraScan() {
    //Log.d(TAG, "setupCameraScan");
    setContentView(R.layout.camera_instructions_holder);

    final View buttonHolder = findViewById(R.id.cameraInstructions_buttonHolder);
    final Button btnBuy = (Button) findViewById(R.id.camera_setup_buy_button);
    final Button btnNext = (Button) findViewById(R.id.camera_setup_next_button);

    ImageView camPairImage = (ImageView) findViewById(R.id.imgCamThree);
    if (getApplicationContext().getPackageName().contains("vtech")) {
      camPairImage.setBackgroundResource(R.drawable.camera_instruction_animation);
    }
    AnimationDrawable animationDrawable = (AnimationDrawable) camPairImage.getBackground();
    animationDrawable.start();

    startCameraScanner("wifi");
    startCameraScanner("mdns");
    instructionsViewPager = (ViewPagerParallax) findViewById(R.id.camera_instructions_pager);
    final PagerAdapter pagerAdapter = new PagerAdapter() {
      public Object instantiateItem(View collection, int position) {
        int resId = 0;
        switch (position) {
          case 0:
            resId = R.id.camera_instructions_page1;
            break;
          case 1:
            resId = R.id.camera_instructions_page2;
            break;
          case 2:
            resId = R.id.camera_instructions_page3;
        }
        return findViewById(resId);
      }

      @Override
      public int getCount() {
        return 3;
      }

      @Override
      public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
      }

      @Override
      public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
      }

    };
    instructionsViewPager.setMaxPages(pagerAdapter.getCount());
    if (BuildConfig.FLAVOR.contains("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew")) {
      instructionsViewPager.setBackgroundAsset(R.drawable.blur_default);
    }
    instructionsViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        switch (position) {
          case 0:
            if (!BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
              btnBuy.setVisibility(View.VISIBLE);
            }
            break;
          case 1:
            if (!BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
              btnBuy.setVisibility(View.INVISIBLE);
            }
            buttonHolder.setVisibility(View.VISIBLE);
            break;
          case 2:
            buttonHolder.setVisibility(View.GONE);
            break;
          default:
            break;
        }
        currentPagePosition = position;
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });
    instructionsViewPager.setOffscreenPageLimit(3);
    instructionsViewPager.setAdapter(pagerAdapter);
    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();


    btnBuy.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          Crittercism.leaveBreadcrumb("Selected buy device");
          String url = getString(R.string.hubble_products_url);
          Intent i = new Intent(Intent.ACTION_VIEW);
          i.setData(Uri.parse(url));
          startActivity(i);
        } catch (Exception ex) {
          //Log.e(TAG, Log.getStackTraceString(ex));
        }
      }
    });

    btnNext.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        switch (currentPagePosition) {
          case 0:
            instructionsViewPager.setCurrentItem(1, true);
            break;
          case 1:
            instructionsViewPager.setCurrentItem(2, true);
            break;
          default:
            instructionsViewPager.setCurrentItem(1, true);
            break;
        }
      }
    });

    Crittercism.leaveBreadcrumb("Start scanning for available devices");
    ImageView eye = (ImageView) findViewById(R.id.imageViewSnapShot);
    fade_outin_view(eye, 500);

    WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    if (wm.getConnectionInfo() != null && wm.getConnectionInfo().getSSID() != null) {
      String saved_ssid = wm.getConnectionInfo().getSSID();
      //Log.d(TAG, "Home wifi SSID: " + saved_ssid);
      settings.putString(PublicDefineGlob.PREFS_HOME_WIFI_SSID_NO_QUOTE, convertToNoQuotedString(saved_ssid));
    }

    deviceSelectionList = (ListView) findViewById(R.id.cam_list);
    View deviceSelectionFooterView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_footer_camera_select, null, false);
    deviceSelectionList.addFooterView(deviceSelectionFooterView, null, false);
    deviceSelectionList.setOnItemClickListener(deviceListClickListener);

    if (deviceSelectionAdapter == null) {
      deviceSelectionAdapter = new CameraAccessPointListAdapter(this, new ArrayList<ApScanBase>());
      deviceSelectionList.setAdapter(deviceSelectionAdapter);
    }

    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();

  }

  private void startCameraScanner(String mode) {
    if (mode.equalsIgnoreCase("wifi")) {
      if (wifiScanning.compareAndSet(false, true)) {
        //Log.d(TAG, "WIFI -> Scan starting...");
        if (wifiCameraScanner == null) {
          wifiCameraScanner = new CameraScanner(this.getApplicationContext(), this, mode);
        }
        wifiCameraScanner.run();
      }
    } else {
      if (mdnsScanning.compareAndSet(false, true)) {
        //Log.d(TAG, "MDNS -> Scan starting...");
        if (mdnsCameraScanner == null) {
          mdnsCameraScanner = new CameraScanner(this.getApplicationContext(), this, mode);
        }
        mdnsCameraScanner.run();
      }
    }
  }

  private void setupToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.my_singlecam_toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(getString(R.string.device_setup));
  }

  private OnItemClickListener deviceListClickListener = new OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int mPosition, long id) {
      if (mPosition != -1) {
        // use for Google Analytics
        add_camera_start_time = System.currentTimeMillis();
        ApScanBase selectCamera = (ApScanBase) deviceSelectionList.getItemAtPosition(mPosition);
        if (selectCamera != null) {
          stopCameraScanner("wifi");
          stopCameraScanner("mdns");
        }
        if (selectCamera instanceof CameraBonjourInfo) {
          Crittercism.leaveBreadcrumb("Selected to pair LAN device");

          CameraBonjourInfo info = (CameraBonjourInfo) selectCamera;
          cam_profile = new LegacyCamProfile(info.getCameraAddress(), info.getMac());
          cam_profile.setName(info.getCameraName());
          showConnectingToCameraView(info.getCameraName());
          beginSetupCamera(info);
        } else if (selectCamera instanceof NameAndSecurity) {
          Crittercism.leaveBreadcrumb("Selected to pair WiFi device");
          NameAndSecurity ns = ((NameAndSecurity) deviceSelectionList.getItemAtPosition(mPosition));
          showConnectingToCameraView(ns.name);
          Intent intent = new Intent();
          intent.setAction(ACTION_TURN_WIFI_NOTIF_ON_OFF);
          intent.putExtra(WIFI_NOTIF_BOOL_VALUE, TURN_OFF);
          sendBroadcast(intent);
          getWakeLock();
          String ssid = ns.name;
          String checkSSID = '\"' + ssid + '\"';
          final String checkBSSID = ns.BSSID;
          WifiManager w = (WifiManager) RetreiveCameraLogActivity.this.getSystemService(Context.WIFI_SERVICE);
          WifiConfiguration wc = findCameraAP(checkSSID, checkBSSID);
          if (wc == null) {
            wc = buildWifiConfiguration(checkSSID, checkBSSID, ns);
            int res = w.addNetwork(wc);
            boolean save_succeeded = w.saveConfiguration();
            wc = findCameraAP(checkSSID, checkBSSID);
            //If we are still null at this point, we couldnt construct a network
            // So let's bail.
            if (wc == null) {
              connectToCameraFailed(checkSSID);
              return;
            }
          }

          final String default_cam_name = ns.name;
          final String gatewayIp = Util.getGatewayIp(RetreiveCameraLogActivity.this);
          final WifiConfiguration this_conf = wc;
          Handler local = new Handler(Looper.getMainLooper());
          local.post(new Runnable() {
            @Override
            public void run() {
              InetAddress default_inet_address = null;
              try {
                default_inet_address = InetAddress.getByName(gatewayIp);
              } catch (UnknownHostException e) {
                //Log.d(TAG, "exception: " + this.getClass().getName());
                //Log.e(TAG, Log.getStackTraceString(e));
              }
              cam_profile = new LegacyCamProfile(default_inet_address, checkBSSID);
              cam_profile.setName(default_cam_name);
              ConnectToNetworkTask connect_task = new ConnectToNetworkTask(RetreiveCameraLogActivity.this, new Handler(RetreiveCameraLogActivity.this));
              // ignore BSSID because some phones cannot get BSSID
              connect_task.setIgnoreBSSID(true);
              connect_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this_conf);
            }
          });
        }
      }
    }
  };

  //Step 2 connecting to camera
  private void showConnectingToCameraView(String name) {
    Crittercism.leaveBreadcrumb("Connecting to device");

    setContentView(R.layout.connecting_to_camera);
    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();
    ImageView imgConn = (ImageView) findViewById(R.id.imgConnecting);
    if (imgConn != null) {
      // start waiting animation
      imgConn.setVisibility(View.VISIBLE);
      imgConn.setImageResource(R.drawable.camera_connecting_anim);
      imgConn.setImageResource(R.drawable.wifi_connecting_anim);
      anim = (AnimationDrawable) imgConn.getDrawable();
      anim.start();
    }
    TextView txtConn = (TextView) findViewById(R.id.txtConnecting);
    String strConn = String.format(getResources().getString(R.string.connecting_to_camera_wifi), name);
    txtConn.setText(strConn);
    TextView txtDesc = (TextView) findViewById(R.id.txtDesc);
    txtDesc.setText(R.string.this_may_take_up_to_a_minute);
  }

  private void beginSetupCamera(final CameraBonjourInfo cameraInfo) {
    //Log.d(TAG, "beginSetupCamera - LAN:" + (cameraInfo != null));
    Crittercism.leaveBreadcrumb("Begin device setup");
    stopCameraScanner("wifi");
    stopCameraScanner("mdns");

    WifiManager w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    if ((w.getConnectionInfo() == null) || (w.getConnectionInfo().getBSSID() == null)) {
      return;
    }

    String camPassString = "000000";

    cam_profile.setBasicAuth_usr(PublicDefineGlob.DEFAULT_BASIC_AUTH_USR);
    cam_profile.setBasicAuth_pass(camPassString);

    Runnable worker = new Runnable() {
      public void run() {
        /*
         * IMPORTANT: App must enable wifi interface for Android 5.0 or higher
				 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          /*
           * Force enable wifi interface for Android 5.0
					 * NOTE: Don't call this method on UI thread. Because if app request
					 * network on UI, then send network request in another thread, the "machine is not on network"
					 * error would happen.
					 */
          forceWifiInterfaceEnabled(true);

					/*
           * Need to wait until wifi interface is available.
					 */

          int k = 10;
          while (k-- > 0 && isActivityDestroyed == false) {
            if (isWifiInterfaceAvailable == true) {
              break;
            }

            Log.i(TAG, "Wifi interface is still not available, waiting...");

            try {
              Thread.sleep(1500);
            } catch (Exception e) {
            }
          }
        }

        cameraVerifyPassword(cameraInfo);
      }
    };
    new Thread(worker, "Verify PWD").start();
  }

  /**
   * IMPORTANT: From Android 5.0, the Wifi interface could be disabled
   * Condition: WiFi is on, Mobile data is on, 'auto network switch' or 'disconnect poor network' setting is on
   * Wifi router doesnot have internet.
   * Result: All network request would be sent via mobile network. Thus, app cannot send any request to camera.
   * Solution: Check Android version first. Then force enable wifi interface although it has no internet access.
   *
   * @param isEnabled true to force wifi interface enabled,
   *                  false to force wifi interface disabled.
   */
  private void forceWifiInterfaceEnabled(boolean isEnabled) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      ConnectivityManager cm =
          (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      if (isEnabled == true) {
        Log.i(TAG, "Using Android Lollipop or higher, force enable Wifi interface");
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        NetworkRequest request = builder.build();
        cm.requestNetwork(request, (ConnectivityManager.NetworkCallback) mNetworkCallback);

      } else {
        if (isWifiInterfaceAvailable == true) {
          Log.i(TAG, "Using Android Lollipop or higher, force disable Wifi interface");
          cm.unregisterNetworkCallback((ConnectivityManager.NetworkCallback) mNetworkCallback);
          ConnectivityManager.setProcessDefaultNetwork(null);
          isWifiInterfaceAvailable = false;
        }
      }
    }
  }

  private void connectToCameraFailed(String cameraSSID) {
    Crittercism.leaveBreadcrumb("Failed to connect to device. Device UDID is " + cam_profile.getRegistrationId());
    Crittercism.logHandledException(new Throwable());

    setContentView(R.layout.connect_to_camera_failed);
    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();
    TextView txtFailed = (TextView) findViewById(R.id.connectToCameraFailed_txtDescription);
    String desc_str = String.format(getString(R.string.cannot_connect_to_camera_network_please_try_again), cameraSSID);
    if (cam_profile != null && cam_profile.getRegistrationId() != null) {
      desc_str += String.format(getString(R.string.camera_udid), cam_profile.getRegistrationId());
    }
    txtFailed.setText(desc_str);

    Button connect = (Button) findViewById(R.id.cameraSetupFailed_btn_connect);
    connect.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        initialSetup();
      }
    });
  }

  private WifiConfiguration findCameraAP(String checkSSID, String checkBSSID) {
    WifiManager w = (WifiManager) RetreiveCameraLogActivity.this.getSystemService(Context.WIFI_SERVICE);
    List<WifiConfiguration> wcs = w.getConfiguredNetworks();

    for (WifiConfiguration wc : wcs) {
      // note that: WifiConfiguration SSID has quotes
      if (wc.SSID == null) {
        continue;
      }
      if (wc.SSID.equalsIgnoreCase(checkSSID)) {
        return wc;
      }
    }
    return null;
  }

  private void stopCameraScanner(String mode) {
    if (mode.equalsIgnoreCase("wifi") && wifiScanning.compareAndSet(true, false)) {
      //Log.d(TAG, "WIFI -> Ending scan.");
      wifiCameraScanner.stop();
    } else if (mdnsScanning.compareAndSet(true, false)) {
      //Log.d(TAG, "MDNS -> Ending scan.");
      mdnsCameraScanner.stop();
    }
  }

  private void cameraVerifyPassword(final CameraBonjourInfo cameraObject) {
    String gatewayIp = null;
    // setup via Wi-Fi
    if (cameraObject == null) {
      gatewayIp = Util.getGatewayIp(this.getApplicationContext());
      cam_profile.setAddViaLAN(false);
    } else if (cameraObject instanceof CameraBonjourInfo) {
      CameraBonjourInfo info = cameraObject;
      gatewayIp = info.getIp();
      cam_profile.setAddViaLAN(true);
    } else {
      Log.i(TAG, "Invalid selected camera.");
    }

    PublicDefineGlob.DEVICE_PORT = PublicDefineGlob.DEFAULT_DEVICE_PORT;
    String regId = null;
    int k = 10;
    while (k-- > 0 && isActivityDestroyed == false &&
        isWifiInterfaceAvailable == true) {
      try {
        regId = HTTPRequestSendRecvTask.getUdid(gatewayIp, PublicDefineGlob.DEVICE_PORT, cam_profile.getBasicAuth_usr(), cam_profile.getBasicAuth_pass());
        Log.i(TAG, "UDID: " + regId);
        if (regId != null) {
          break;
        } else {
          Log.i(TAG, "Cannot get camera regId or modelId -> retry");
        }
      } catch (ConnectException e2) {
        //Log.e(TAG, Log.getStackTraceString(e2));
      }
    }

    if (regId != null && regId.length() == 26) {
      cam_profile.setRegistrationId(regId);
      String mac_address = PublicDefine.getMacFromRegId(regId);
      String modelId = PublicDefine.getModelIdFromRegId(regId);
      cam_profile.set_MAC(PublicDefineGlob.add_colon_to_mac(mac_address));
      cam_profile.setModelId(modelId);

      try {
        //cam_profile.setInetAddr(InetAddress.getByName(gatewayIp));
        cam_profile.setHostInetAddress(InetAddress.getByName(gatewayIp));
      } catch (UnknownHostException e) {
        //Log.e(TAG, "Error when set inet address to cam_profile.");
        //Log.e(TAG, Log.getStackTraceString(e));
      }
    }

    String fw_version = null;
    k = 5;
    while (k-- > 0 && isActivityDestroyed == false &&
        isWifiInterfaceAvailable == true) {
      try {
        fw_version = HTTPRequestSendRecvTask.getFirmwareVersion(gatewayIp, PublicDefineGlob.DEVICE_PORT, cam_profile.getBasicAuth_usr(), cam_profile.getBasicAuth_pass());
        Log.i(TAG, "Firmware version: " + fw_version);
        if (fw_version != null) {
          break;
        } else {
          Log.i(TAG, "Cannot get fw_version -> retry");
        }
      } catch (SocketException e1) {
        //Log.e(TAG, Log.getStackTraceString(e1));
      }

      try {
        Thread.sleep(1500);
      } catch (InterruptedException e) {
      }
    }
    cam_profile.setFirmwareVersion(fw_version);

    cam_profile.setFirmwareVersion(fw_version);
    SimpleDateFormat sdf = new SimpleDateFormat("DD_MM_yyyy_HH_mm");
    final String fileName = regId + "_" + fw_version + "_"
        + sdf.format(new Date()) + ".txt";
    final String logURL = String.format("http://%s:8080/cgi-bin/logdownload.cgi", gatewayIp);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        downloadCameraLog(logURL, fileName);
      }
    });
  }

  private String cameraLogFileName = "dummy.txt";

  private void downloadCameraLog(String logURL, String fileName) {
    cameraLogFileName = "dummy.txt";
    try {
      File file = new File(mContext.getExternalFilesDir("camera-log"), fileName);
      LogZ.i("Download camera log url: %s", logURL);
      final ProgressDialog downloadFirmwareLogProgressDialog = new ProgressDialog(RetreiveCameraLogActivity.this);
      downloadFirmwareLogProgressDialog.setMessage(getString(R.string.downloading_camera_log));
      downloadFirmwareLogProgressDialog.setCancelable(false);
      downloadFirmwareLogProgressDialog.show();

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
          .setCallback(new com.koushikdutta.async.future.FutureCallback<File>() {
            @Override
            public void onCompleted(Exception e, final File resultFile) {
              if (e == null) {
                if (resultFile != null) {
                  new Thread(new Runnable() {
                    @Override
                    public void run() {
                      check_and_reconnect_to_home();
                      try {
                        Thread.sleep(15000);

                        File realFile = new File(mContext.getExternalFilesDir("camera-log"), cameraLogFileName);
                        resultFile.renameTo(realFile);
                        Uri contentUri;
                        String titleEmail = "";
                        if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
                          contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.vtech.fileprovider", realFile);
                          titleEmail = String.format(getString(R.string.title_email), "Vtech", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
                        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny")) {
                          contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.inanny.fileprovider", realFile);
                          titleEmail = String.format(getString(R.string.title_email), "iNanny", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
                        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
                          contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.beurer.fileprovider", realFile);
                          titleEmail = String.format(getString(R.string.title_email), "Beurer", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
                        } else {
                          contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, CommonConstants.FILE_PROVIDER_AUTHORITY_HUBBLE, realFile);
                          titleEmail = String.format(getString(R.string.title_email), "Hubble", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
                        }

                        ArrayList<Uri> uriList = new ArrayList<>();
                        uriList.add(contentUri);
                        uriList.add(HubbleApplication.AppContext.getAppLogUri());

                        String bodyEmail = getString(R.string.body_email);
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                        sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"android.techsupport@hubblehome.com"});
                        sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, titleEmail);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, bodyEmail);
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);
                        RetreiveCameraLogActivity.this.finish();
                        downloadFirmwareLogProgressDialog.dismiss();

                      } catch (InterruptedException e1) {
                        e1.printStackTrace();
                      }
                    }
                  }).start();

                } else {
                  LogZ.e("Result file is null", null);
                }
              } else {
                Toast.makeText(RetreiveCameraLogActivity.this, getString(R.string.download_camera_log_failed), Toast.LENGTH_SHORT);
              }
            }
          });
    } catch (Exception ex) {
      LogZ.e("Error when send device log", ex);
    }
  }

  private void skipWifiSetup() {
    cam_profile.setHasWiFiInfo(false);
    showConnectingPage();
    configuration = createCamConfiguration(null, null, null, null, null);
    configuration.setCamProfiles(new LegacyCamProfile[]{cam_profile});
    AsyncTask<LegacyCamProfile, String, Boolean> config_task_lan = new ConfigureLANCamera(this, new Handler(this));
    ((ConfigureLANCamera) config_task_lan).setStartTime(add_camera_start_time);
    config_task_lan.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cam_profile);
  }

  private boolean isFwVersionOutdated(String fw_version) {
    boolean isOutdated = false;
    int ver_no_0 = -1;
    int ver_no_1 = -1;
    int ver_no_2 = -1;
    if (fw_version != null) {

      String[] fw_version_arr = fw_version.split("\\.");
      if (fw_version_arr != null && fw_version_arr.length == 3) {
        try {
          ver_no_0 = Integer.parseInt(fw_version_arr[0]);
          ver_no_1 = Integer.parseInt(fw_version_arr[1]);
          ver_no_2 = Integer.parseInt(fw_version_arr[2]);

          if (ver_no_0 < 1 || (ver_no_0 == 1 && (ver_no_1 < 12 || (ver_no_1 == 12 && ver_no_2 < 78)))) {
            isOutdated = true;
          }
        } catch (NumberFormatException e) {
          //Log.e(TAG, Log.getStackTraceString(e));
        }
      }
    }
    if (cam_profile.getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_MBP931)) {
      isOutdated = false;
    }

    if (isOutdated) {
      Crittercism.leaveBreadcrumb("Device firmware is out-of-date");
    } else {
      Crittercism.leaveBreadcrumb("Device firmware is up-to-date");
    }
    return isOutdated;
  }

  private void doQueryWifiListTask(final String fw_version) {
    final QueryCameraWifiListTask getWifiList = new QueryCameraWifiListTask(this.getApplicationContext(), fw_version, cam_profile.getModelId(), cam_profile.getRegistrationId());
    Log.i(TAG, "Camera IP: " + cam_profile.get_inetAddress().getHostAddress());
    getWifiList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cam_profile.getHostInetAddress().getHostAddress());

    // get results in background to avoid hang UI
    Thread worker = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          final ArrayList<CameraWifiEntry> wifiListFromCamera = (ArrayList<CameraWifiEntry>) getWifiList.get(10000, TimeUnit.MILLISECONDS);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              //Log.d(TAG, "Show router list from camera");
              start_time_from_get_rt_list = System.currentTimeMillis();
              routerSelectionNew(wifiListFromCamera);
            }
          });
          shouldContinueQueryWifiList = false;
        } catch (ExecutionException e) {
          //Log.e(TAG, Log.getStackTraceString(e));
        } catch (InterruptedException e) {
          //Log.e(TAG, Log.getStackTraceString(e));
        } catch (TimeoutException e) {
          //Log.e(TAG, Log.getStackTraceString(e));
        }
      }
    });
    worker.start();
  }

  //Step 3 selecting network to pair camera to
  private void routerSelectionNew(List<CameraWifiEntry> results) {
    //Log.d(TAG, "routerSelectionNew");
    if (searchingNetworkDialog != null && searchingNetworkDialog.isShowing()) {
      searchingNetworkDialog.dismiss();
    }
    routerSelectionWithLayout(results);
    return;
  }

  private void routerSelectionWithLayout(List<CameraWifiEntry> results) {
    Crittercism.leaveBreadcrumb("Selecting router setup device on");
    setContentView(R.layout.bb_is_wifi_selection);
    setupToolbar();
    shouldShowWifiScanningInMenu = true;
    invalidateOptionsMenu();
    setupWiFiSelectionLayout();
    ArrayList<NameAndSecurity> ap_list;
    final ListView wifi_list = (ListView) findViewById(R.id.wifi_list);
    ap_list = new ArrayList<NameAndSecurity>();

    if (results != null) {
      for (CameraWifiEntry result : results) {
      /* 20120220:filter those camera network out of this list */
        if (result != null && result.getSsidNoQuote() != null && !result.getSsidNoQuote().isEmpty() && !result.getSsidNoQuote().startsWith(PublicDefineGlob.DEFAULT_CAM_NAME)) {
          NameAndSecurity _ns = new NameAndSecurity(result.getSsidNoQuote(), result.getAuth_mode(), result.getBssid());
          _ns.setLevel(result.getSignal_level());
          _ns.setHideMac(true);
          ap_list.add(_ns);
        }
      }
    }

    final AccessPointAdapter ap_adapter = new AccessPointAdapter(this, ap_list);

    wifi_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    wifi_list.setAdapter(ap_adapter);
    wifi_selected_position = -1;
    wifi_list.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // last element is not a NameAndSec object
        wifi_selected_position = position;
        int startPosition = wifi_list.getFirstVisiblePosition();
        int endPosition = wifi_list.getLastVisiblePosition();
        ap_adapter.setSelectedPositision(wifi_selected_position);
        for (int i = 0; i <= endPosition - startPosition; i++) {
          if (wifi_list.getChildAt(i) != null) {
            ImageView checked = (ImageView) parent.getChildAt(i).findViewById(R.id.imgChecked);
            if (checked != null) {
              if (i + startPosition == position) {
                checked.setVisibility(View.VISIBLE);
              } else {
                checked.setVisibility(View.INVISIBLE);
              }
            }
          }
        }
      }
    });

    Button connect = (Button) findViewById(R.id.buttonConnect);
    connect.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (wifi_selected_position != -1) {
          NameAndSecurity ns = (NameAndSecurity) wifi_list.getItemAtPosition(wifi_selected_position);
          on_router_item_selected(ns);
        }
      }
    });
  }

  private void setupWiFiSelectionLayout() {
    if (cam_profile != null) {
      Button btnSkipWiFiSetup = (Button) findViewById(R.id.buttonSkipWiFiSetup);
      if (btnSkipWiFiSetup != null) {
        if (cam_profile.isAddViaLAN() && !cam_profile.getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_MBP921)) {
          //Log.d(TAG, "Adding Camera via LAN");
          btnSkipWiFiSetup.setVisibility(View.VISIBLE);
        } else {
          btnSkipWiFiSetup.setVisibility(View.INVISIBLE);
        }
        btnSkipWiFiSetup.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
            skipWifiSetup();
          }
        });
      }
    }
  }

  private void showConfirmUpgradeDialogWhenReSetup(DialogInterface.OnClickListener listener) {
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    dialogBuilder.setTitle(R.string.upgrade);
    dialogBuilder.setMessage(R.string.ask_for_user_upgrade_when_re_setup_camera);
    dialogBuilder.setNegativeButton(R.string.No, listener);
    dialogBuilder.setPositiveButton(R.string.upgrade_now, listener);
    dialogBuilder.create().show();
  }

  private void on_router_item_selected(NameAndSecurity ns) {
    Crittercism.leaveBreadcrumb("Selected router to setup device on");
    String ssid = ns.name;
    String saved_wifi_nw = settings.getString(PublicDefineGlob.PREFS_SAVED_WIFI_SSID, null);
    // String saved_wifi_sec = settings.getString(
    // PublicDefine.PREFS_SAVED_WIFI_SEC, null);
    String saved_wifi_sec = null;
    Boolean saved_wifi_hidden_ssid = settings.getBoolean(PublicDefineGlob.PREFS_SAVED_WIFI_HIDDEN_SSID, false);
    String saved_wifi_pass = settings.getString(PublicDefineGlob.PREFS_SAVED_WIFI_PWD, null);
    if (ns.name.equalsIgnoreCase(saved_wifi_nw)) {
      if (ns.security.equalsIgnoreCase("WEP")) {
        //Log.d(TAG, "Prefill wifi password...");
        String auth_method = settings.getString(PublicDefineGlob.PREFS_SAVED_WIFI_SEC_WEP_AUTH, "Open");
        String key_index = settings.getString(PublicDefineGlob.PREFS_SAVED_WIFI_SEC_WEP_IDX, "1");
        /*
         * Construct a WifiConf object here This is just a temp object
         * to store data, it should not be used as an actual
         * Wificonfiguration
         */
        WifiConfiguration wep_conf = new WifiConfiguration();
        wep_conf.SSID = '\"' + saved_wifi_nw + '\"';
        wep_conf.hiddenSSID = saved_wifi_hidden_ssid;
        if (auth_method.equalsIgnoreCase("Open")) {
          wep_conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        } else if (auth_method.equalsIgnoreCase("Shared")) {
          wep_conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        }

        wep_conf.allowedKeyManagement.set(KeyMgmt.NONE);
        wep_conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wep_conf.wepTxKeyIndex = Integer.parseInt(key_index) - 1;
        wep_conf.wepKeys[wep_conf.wepTxKeyIndex] = saved_wifi_pass;
        setSelectedSSID(saved_wifi_nw, ns.security, wep_conf);
        try {
          getWEPWifiKeyEntryDialog().show();
        } catch (Exception e) {
        }
      }
      // WPA/WPA2
      else if (ns.security.startsWith("WPA")) {
        //Log.d(TAG, "Prefill wifi password...");
        /* Construct a WifiConf object here */
        WifiConfiguration wpa_conf = new WifiConfiguration();
        wpa_conf.SSID = '\"' + saved_wifi_nw + '\"';
        wpa_conf.hiddenSSID = saved_wifi_hidden_ssid;
        wpa_conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wpa_conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wpa_conf.preSharedKey = saved_wifi_pass;
        setSelectedSSID(saved_wifi_nw, ns.security, wpa_conf);
        getWPAKeyEntryDialog().show();
      }
      // OPEN
      else {
        /* Don't allow user to add camera to OPEN SSID */
        getOpenNetworkNotSupportedDialog().show();
      }
      // autoConnectToOldWifi(saved_wifi_nw, saved_wifi_sec);
    } else {
      WifiManager w = (WifiManager) RetreiveCameraLogActivity.this.getSystemService(Context.WIFI_SERVICE);

      List<WifiConfiguration> wcs = w.getConfiguredNetworks();
      String checkSSID = '\"' + ssid + '\"';
      boolean foundExisting = false;

      WifiConfiguration selectedConfiguration;
      selectedConfiguration = null;

      for (WifiConfiguration wc : wcs) {
        if ((wc == null) || (wc.SSID == null)) {
          continue;
        }
        if (wc.SSID.equalsIgnoreCase(checkSSID)) {
          /*
           * 20140429: hoang: very important dont need to compare key
           * management WiFi list from phone scanning has key
           * management BUT WiFi list from camera may have NULL key
           * management
           */
          foundExisting = true;
          selectedConfiguration = wc;
        }
      }

      if (!foundExisting) {

        WifiConfiguration newWC = new WifiConfiguration();
        newWC.hiddenSSID = false;
        newWC.SSID = checkSSID;
        newWC.status = WifiConfiguration.Status.ENABLED;
        // the following is the settings
        // that found to be working for ai-ball
        newWC.hiddenSSID = false;
        newWC.allowedAuthAlgorithms = ns.getAuthAlgorithm();
        newWC.allowedGroupCiphers = ns.getGroupCiphers();
        newWC.allowedKeyManagement = ns.getKeyManagement();

        newWC.allowedPairwiseCiphers = ns.getPairWiseCiphers();
        newWC.allowedProtocols = ns.getProtocols();

        int res = w.addNetwork(newWC);
        /*
         * //Log.d(TAG,this.getClass().getName() + " add new ssid:"+
         * checkSSID+ " id:" + res + " cap: " +ns.security );
         */

        // DONT REMOVE THIS
        //Log.d(TAG, "save: " + w.saveConfiguration());

        selectedConfiguration = newWC;
      }

      /*
       * Store in some temporary location and wait until user enter the
       * key
       */
      setSelectedSSID(ns.name, ns.security, selectedConfiguration);

      /*
       * Popup a dialog asking for key if sec is WEP or WPA Else go direct
       * to Configure Camera
       */

      if (ns.security.equalsIgnoreCase("WEP")) {
        getWEPWifiKeyEntryDialog().show();
      } else if (ns.security.startsWith("WPA")) {
        getWPAKeyEntryDialog().show();
      } else {
        getOpenNetworkNotSupportedDialog();
      }
    }

  }

  private void setSelectedSSID(String newSSID, String securityType, WifiConfiguration w) {
    String security_type;
    synchronized (this) {
      if (securityType.startsWith("WPA")) {
        security_type = "WPA/WPA2";
      } else {
        security_type = securityType;
      }
      /* update the current_ssid with the seleted SSID */

      settings.putString(PublicDefineGlob.PREFS_CURRENT_SSID, newSSID);
      settings.putString(PublicDefineGlob.PREFS_CURRENT_NW_SEC, security_type);

      selectedWifiCon = w;
    }
  }

  //Step 4 connecting camera to network
  private Dialog getWPAKeyEntryDialog() {
    Crittercism.leaveBreadcrumb("Entering WPA router info");
    final Dialog dialog;
    dialog = new Dialog(this);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.bb_is_router_key);
    dialog.setCancelable(true);

    LinearLayout wep_opts = (LinearLayout) findViewById(R.id.wep_options);
    if (wep_opts != null) {
      wep_opts.setVisibility(View.GONE);
    }

    String text = null;
    if (this.selectedWifiCon != null) {
      // Obmit the quote
      text = this.selectedWifiCon.SSID.substring(1, this.selectedWifiCon.SSID.length() - 1);
    }

    final TextView ssid_text = (TextView) dialog.findViewById(R.id.t0);
    ssid_text.setText(text);

    final EditText pwd_text = (EditText) dialog.findViewById(R.id.text_key);

    String saved_wifi_nw = settings.getString(PublicDefineGlob.PREFS_SAVED_WIFI_SSID, null);
    String saved_wifi_pass = settings.getString(PublicDefineGlob.PREFS_SAVED_WIFI_PWD, null);
    if (selectedWifiCon != null && saved_wifi_nw != null && saved_wifi_nw.equalsIgnoreCase(convertToNoQuotedString(selectedWifiCon.SSID))) {
      pwd_text.setText(saved_wifi_pass);
    }
    pwd_text.setOnEditorActionListener(new OnEditorActionListener() {

      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          v.setTransformationMethod(PasswordTransformationMethod.getInstance());

        }
        return false;
      }

    });

    final CheckBox checkBoxIsKeyVisible = (CheckBox) dialog.findViewById(R.id.checkBoxIsKeyVisible);
    checkBoxIsKeyVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          pwd_text.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
          pwd_text.setSelection(pwd_text.getText().length());
        } else {
          pwd_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
          pwd_text.setSelection(pwd_text.getText().length());
        }
      }
    });

    // setup connect button
    Button connect = (Button) dialog.findViewById(R.id.connect_btn);
    connect.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        Util.hideSoftKeyboard(getApplicationContext(), getCurrentFocus());
        String pass_string = null;
        EditText text = (EditText) dialog.findViewById(R.id.text_key);
        if (text == null) {
          return;
        }
        pass_string = text.getText().toString();
        if (pass_string != null && !pass_string.isEmpty()) {
          WifiConfiguration wpa_conf = new WifiConfiguration();
          wpa_conf.SSID = ssid_text.getText().toString(); // Un-quoted
          wpa_conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
          wpa_conf.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
          wpa_conf.preSharedKey = pass_string;
          start_configure_camera(wpa_conf);
        }
        dialog.cancel();
      }
    });

    final Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
    cancel.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Util.hideSoftKeyboard(getApplicationContext(), getCurrentFocus());
        dialog.cancel();
      }
    });

    return dialog;
  }

  private Dialog getWEPWifiKeyEntryDialog() {
    Crittercism.leaveBreadcrumb("Entering WEP router info");
    final Dialog dialog;
    dialog = new Dialog(this);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.bb_is_router_key);
    dialog.setCancelable(true);

    String text = null;
    if (this.selectedWifiCon != null) {
      text = this.selectedWifiCon.SSID.substring(1, this.selectedWifiCon.SSID.length() - 1);
    }

    final TextView ssid_text = (TextView) dialog.findViewById(R.id.t0);
    ssid_text.setText(text);

    final EditText pwd_text = (EditText) dialog.findViewById(R.id.text_key);
    String saved_wifi_nw = settings.getString(PublicDefineGlob.PREFS_SAVED_WIFI_SSID, null);
    String saved_wifi_pass = settings.getString(PublicDefineGlob.PREFS_SAVED_WIFI_PWD, null);

    final CheckBox checkBoxIsKeyVisible = (CheckBox) dialog.findViewById(R.id.checkBoxIsKeyVisible);
    checkBoxIsKeyVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          pwd_text.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
          pwd_text.setSelection(pwd_text.getText().length());
        } else {
          pwd_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
          pwd_text.setSelection(pwd_text.getText().length());
        }
      }
    });

    if (selectedWifiCon != null && saved_wifi_nw != null && saved_wifi_nw.equalsIgnoreCase(convertToNoQuotedString(selectedWifiCon.SSID))) {
      pwd_text.setText(saved_wifi_pass);
    }
    pwd_text.setOnEditorActionListener(new OnEditorActionListener() {

      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          v.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        return false;
      }

    });

    // setup connect button
    Button connect = (Button) dialog.findViewById(R.id.connect_btn);
    connect.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        Util.hideSoftKeyboard(getApplicationContext(), getCurrentFocus());
        String pass_string = null;
        EditText text = (EditText) dialog.findViewById(R.id.text_key);
        if (text == null) {
          return;
        }
        pass_string = text.getText().toString();
        if (pass_string != null && !pass_string.isEmpty()) {
          Spinner spinner = (Spinner) dialog.findViewById(R.id.wep_opt_index);
          if (spinner != null) {
            spinner = (Spinner) dialog.findViewById(R.id.wep_opt_method);
          }

        /*
         * Construct a WifiConf object here This is just a temp
         * object to store data, it should not be used as an
         * actual Wificonfiguration
         */
          WifiConfiguration wep_conf = new WifiConfiguration();
          wep_conf.SSID = ssid_text.getText().toString(); // Un-quoted

          // hardcode - auth OPEN
          wep_conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
          // hardcode - index 1
          wep_conf.wepTxKeyIndex = 0;
          wep_conf.allowedKeyManagement.set(KeyMgmt.NONE);
          wep_conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
          wep_conf.wepKeys[wep_conf.wepTxKeyIndex] = pass_string;
          start_configure_camera(wep_conf);
        }
        dialog.cancel();
      }
    });
    Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
    cancel.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        Util.hideSoftKeyboard(getApplicationContext(), getCurrentFocus());
        dialog.cancel();
      }
    });

    return dialog;
  }

  private Dialog getOpenNetworkNotSupportedDialog() {
    //Log.d(TAG, "getOpenNetworkNotSupportedDialog");
    Crittercism.leaveBreadcrumb("Tried to pair device with OPEN network");
    AlertDialog.Builder builder = new AlertDialog.Builder(RetreiveCameraLogActivity.this);
    Spanned msg = Html.fromHtml("<big>" + getResources().getString(R.string.ssid_without_password_is_not_supported_due_to_security_concern) + "</big>");
    builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    return builder.create();

  }

  private void start_configure_camera(WifiConfiguration conf) {
    //Log.d(TAG, "start_configure_camera");
    // Directly start configure
    // TODO: RIP this out.
    if (cam_profile != null && PublicDefine.shouldUseNewSetupFlow(cam_profile.getModelId())) {
      start_configure_camera_new(conf);
    } else {
      start_configure_camera_old(conf);
    }
  }

  private void start_configure_camera_old(WifiConfiguration conf) {
    //Log.d(TAG, "start_configure_camera_old");
    Crittercism.leaveBreadcrumb("Configuring device via old method");
    String ssid = conf.SSID;
    ssid = convertToNoQuotedString(ssid);

    // 3 cases: WPA, WEP, OPEN
    if (configIsWPA(conf)) {
      configureWPA(conf, ssid);
    } else if (configIsWEP(conf)) {
      configureWEP(conf, ssid);
    } else {
      configureOPEN(ssid);
    }
  }

  private void start_configure_camera_new(WifiConfiguration conf) {
    Crittercism.leaveBreadcrumb("Configuring device");
    String ssid = conf.SSID;
    ssid = convertToNoQuotedString(ssid);

    showConnectingPage();
    // 3 cases: WPA, WEP, OPEN
    // Kick start a background task
    if (configIsWPA(conf)) {
      configureWPA_2(conf, ssid);
    } else if (configIsWEP(conf)) {
      configureWEP_2(conf, ssid);
    } else {
      configureOPEN_2(ssid);
    }
    executeConfiguration();
  }

  private void configureOPEN(String ssid) {
    // OPEN
    configureOPEN_2(ssid);
    VerifyNetworkKeyTask verify = new VerifyNetworkKeyTask(RetreiveCameraLogActivity.this, new Handler(RetreiveCameraLogActivity.this));
    verify.execute(configuration);
    verifyKeyDialog = getVerifyKeyDialog();
    verifyKeyDialog.show();
  }

  private void configureWEP(WifiConfiguration conf, String ssid) {
    configureWEP_2(conf, ssid);
    VerifyNetworkKeyTask verify = new VerifyNetworkKeyTask(RetreiveCameraLogActivity.this, new Handler(RetreiveCameraLogActivity.this));
    verify.execute(configuration);
    verifyKeyDialog = getVerifyKeyDialog();
    verifyKeyDialog.show();
  }

  private void configureWPA(WifiConfiguration conf, String ssid) {
    configureWPA_2(conf, ssid);
    VerifyNetworkKeyTask verify = new VerifyNetworkKeyTask(RetreiveCameraLogActivity.this, new Handler(RetreiveCameraLogActivity.this));
    verify.execute(configuration);
    verifyKeyDialog = getVerifyKeyDialog();
    verifyKeyDialog.show();
  }

  private void configureOPEN_2(String ssid) {
    // OPEN
    configuration = createCamConfiguration(ssid, "OPEN", "", null, null);
    configuration.setWifiConf(RetreiveCameraLogActivity.this.selectedWifiCon);

    Vector<String> dev_list = new Vector<String>(1);
    dev_list.addElement(cam_profile.getRegistrationId());
    configuration.setDeviceList(dev_list);
  }

  private CamConfiguration createCamConfiguration(String ssid, String secType, String pass, String index, String authMode) {
    CamConfiguration configuration;
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      configuration = new VtechCamConfiguration(ssid, secType, pass, index, authMode, null, null, null, null,
          cam_profile.getBasicAuth_usr(), cam_profile.getBasicAuth_pass(), cam_profile.getName(),
          HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null));
    } else {
      configuration = new CamConfiguration(ssid, secType, pass, index, authMode, null, null, null, null,
          cam_profile.getBasicAuth_usr(), cam_profile.getBasicAuth_pass(), cam_profile.getName(),
          HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null));
    }
    return configuration;
  }

  private void configureWEP_2(WifiConfiguration conf, String ssid) {
    // WEP
    String key_index = String.format("%d", conf.wepTxKeyIndex + 1);
    String auth_mode = conf.allowedAuthAlgorithms.get(WifiConfiguration.AuthAlgorithm.OPEN) ? "Open" : "Shared";

    configuration = createCamConfiguration(ssid, "WEP", conf.wepKeys[conf.wepTxKeyIndex], key_index, auth_mode);
    configuration.setWifiConf(RetreiveCameraLogActivity.this.selectedWifiCon);

    Vector<String> dev_list = new Vector<String>(1);
    dev_list.addElement(cam_profile.getRegistrationId());
    configuration.setDeviceList(dev_list);
  }

  private void configureWPA_2(WifiConfiguration conf, String ssid) {
    configuration = createCamConfiguration(ssid, "WPA/WPA2", conf.preSharedKey, null, null);
    configuration.setWifiConf(RetreiveCameraLogActivity.this.selectedWifiCon);

    Vector<String> dev_list = new Vector<String>(1);
    dev_list.addElement(cam_profile.getRegistrationId());
    configuration.setDeviceList(dev_list);
  }

  private void executeConfiguration() {
    configuration.setCamProfiles(new LegacyCamProfile[]{cam_profile});
    config_task = new AutoConfigureCamerasNew(RetreiveCameraLogActivity.this, new Handler(RetreiveCameraLogActivity.this), true);
    ((AutoConfigureCamerasNew) config_task).setStartTimeFromGetRtList(start_time_from_get_rt_list);
    ((AutoConfigureCamerasNew) config_task).setAddCameraStartTime(add_camera_start_time);
    config_task.execute(this.configuration);
  }

  //Step 5 setup complete
  private void cameraConfigCompleted() {
    Crittercism.leaveBreadcrumb("Device setup complete");
    if (cam_profile != null && cam_profile.isHasWiFiInfo()) {
      store_default_wifi_info();
    }

    setContentView(R.layout.bb_is_add_cam_end_screen);
    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();

    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      switch (mCameraModelToPair) {
        case VC921:
          ((TextView) findViewById(R.id.camera_instruction_setupComplete)).setText(getString(R.string.congratulations_you_have_successfully_setup_your_hubble_camera_921));
          break;
        case VC931:
          ((TextView) findViewById(R.id.camera_instruction_setupComplete)).setText(getString(R.string.congratulations_you_have_successfully_setup_your_hubble_camera_931));
          break;
        default:
          ((TextView) findViewById(R.id.camera_instruction_setupComplete)).setText(getString(R.string.congratulations_you_have_successfully_setup_your_hubble_camera_921));
      }
    }

    setBrandSpecificCameraImage();
    doMVRDialogSetup();

    if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew") ||
        BuildConfig.FLAVOR.equalsIgnoreCase("inanny") || BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
      new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          try {
            DeviceSingleton.getInstance().factory.build(cam_profile.toDeviceProfile()).sendCommandGetSuccess("get_image_snapshot", null, null); // TODO: Suggest that we move this to another location, user will try to view stream here and send commands but the camera will be uploading its snapshot

          } catch (Exception ignored) {
          }
          return null;
        }
      }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    final Button btnLiveView = (Button) findViewById(R.id.end_screen_btn_live_view);
    final Button btnCamList = (Button) findViewById(R.id.connect_btn);

    settings.putString(PublicDefineGlob.PREFS_SELECTED_CAMERA_MAC_FROM_CAMERA_SETTING, cam_profile.getRegistrationId());

    this.configuration = null;// clear the temp configuration

    TextView currentName = (TextView) findViewById(R.id.renameCam);
    if (cam_profile != null) {
      if (currentName != null) {
        currentName.setText(cam_profile.getName());
      }

    }

    final View.OnClickListener finishedClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        final EditText newName = (EditText) findViewById(R.id.renameCam);
        String newCameraName = newName.getText().toString().trim();

        if ((newCameraName.length() < 5) || (newCameraName.length() > 30)) {
          Toast.makeText(getApplicationContext(), getString(R.string.device_name_length), Toast.LENGTH_LONG).show();
          newName.setText("");
          return;
        } else if (!Util.validate(Util.CAMERA_NAME, newCameraName)) {
          Toast.makeText(getApplicationContext(), getString(R.string.invalid_device_name), Toast.LENGTH_LONG).show();
          newName.setText("");
          return;
        }

        releaseWakeLock();

        if (v == btnLiveView) {
          goToCamLiveView = true;
        } else {
          goToCamLiveView = false;
        }

        // Create a new Device from the profile. TODO: eventually remove LegacyCamProfile from here
        if (cam_profile != null && newCameraName != null && !newCameraName.equalsIgnoreCase(cam_profile.getName())) {
          if (mDevice != null && newCameraName.length() > 0) {
            mDevice.getProfile().setName(newCameraName);
          }

          // Start a rename task
          ChangeNameTask rename = new ChangeNameTask(mContext, RetreiveCameraLogActivity.this);
          rename.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, user_token, newCameraName, cam_profile.getRegistrationId());
        }

        update_cam_success();
      }
    };

    btnCamList.setOnClickListener(finishedClickListener);
    btnLiveView.setOnClickListener(finishedClickListener);
  }

  private void syncDevices() {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        final ListenableFuture<Object> listenableFuture = DeviceSingleton.getInstance().update(false);

        Futures.addCallback(listenableFuture, new FutureCallback<Object>() {
          @Override
          public void onSuccess(Object result) {
            mDevice = DeviceSingleton.getInstance().getDeviceByName(cam_profile.getName());

            DeviceSingleton.getInstance().addTempDevice(mDevice);
            DeviceSingleton.getInstance().setSelectedDevice(mDevice);
            mDevice.sendCommandGetSuccess("set_flicker", Util.getLocalHertz() + "", null);

            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                cameraConfigCompleted();
              }
            });
          }

          @Override
          public void onFailure(Throwable t) {
            DeviceSingleton.getInstance().addTempDevice(mDevice);
            DeviceSingleton.getInstance().setSelectedDevice(mDevice);
          }
        });
      }
    });
    thread.start();
  }

  private void performCompletion() {
    mDevice.setIsAvailableLocally(true);
    mDevice.sendCommandGetSuccess("set_flicker", Util.getLocalHertz() + "", null);
    DeviceSingleton.getInstance().addTempDevice(mDevice);
    DeviceSingleton.getInstance().setSelectedDevice(mDevice);

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        cameraConfigCompleted();
      }
    });
  }

  private void camera_config_failed(int errCode, String errMsg) {
    //Log.d(TAG, "Add camera failed.");
    clear_default_wifi_info();

    if (!PublicDefine.shouldUseNewSetupFlow(cam_profile.getModelId())) {
      if (errCode != RetreiveCameraLogActivity.ADD_CAM_FAILED_WITH_DESC) {
        RemoveDeviceTask remove = new RemoveDeviceTask();
        remove.execute(cam_profile.getRegistrationId(), user_token);
      }
    }

    String strMessage = null;
    if (errMsg != null) {
      strMessage = errMsg;
    } else {
      switch (errCode) {
        case PublicDefine.START_SCAN_FAILED:
          strMessage = getString(R.string.start_scan_failed);
          break;
        case PublicDefine.CONNECT_TO_CAMERA_FAILED:
          strMessage = getString(R.string.connect_to_camera_failed);
          break;
        case PublicDefine.SEND_DATA_TO_CAMERA_FAILED:
          strMessage = getString(R.string.send_data_to_camera_failed);
          break;
        case PublicDefine.CONNECT_TO_HOME_WIFI_FAILED:
          strMessage = getString(R.string.connect_to_homw_wifi_failed);
          break;
        case PublicDefine.SCAN_CAMERA_FAILED:
          strMessage = getString(R.string.scan_camera_failed);
          break;
        case PublicDefine.CAMERA_DOES_NOT_HAVE_SSID:
          strMessage = getString(R.string.camera_cannot_locate_router_please_move_camera_close_to_the_router_switch_off_and_on_the_camera_and_try_to_add_again_);
          break;
        case PublicDefine.FW_UPGRADE_FAILED:
          strMessage = getString(R.string.firmware_upgrade_failed) + " " + getString(R.string.please_manually_reboot_the_camera);
          break;
        case RetreiveCameraLogActivity.ADD_CAM_FAILED_WITH_DESC:
          strMessage = "This camera is not registered. Setup camera failed";
          break;
        default:
          strMessage = getString(R.string.SingleCamConfigureActivity_conf_cam_failed_1);
          break;
      }
    }
    CheckFirmwareUpdateTask task = createCheckFirmwareUpdateTask(errCode, strMessage);

    task.execute();
    releaseWakeLock();
  }

  private void showAbortPage() {
    Crittercism.leaveBreadcrumb("Setup taking too long, giving option to abort setup");
    setContentView(R.layout.connecting_to_wifi);
    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();
    ImageView imgConn = (ImageView) findViewById(R.id.imgConnecting);
    if (imgConn != null) {
      // start waiting animation
      imgConn.setVisibility(View.VISIBLE);
      imgConn.setImageResource(R.drawable.wifi_connecting_anim);
      anim = (AnimationDrawable) imgConn.getDrawable();
      anim.start();
    }

    TextView txtConn = (TextView) findViewById(R.id.txtConnecting);
    txtConn.setText(R.string.connecting_camera_to_wifi);
    TextView txtDesc = (TextView) findViewById(R.id.txtDesc);
    String desc_str = String.format(getResources().getString(R.string.please_wait_for_a_couple_of_minutes_while_camera_connects_to_your_network_press_button_to_restart), cam_profile.getName());
    if (cam_profile != null && cam_profile.getRegistrationId() != null) {
      desc_str += String.format(getString(R.string.camera_udid), cam_profile.getRegistrationId());
    }
    txtDesc.setText(desc_str);

    Button abort = (Button) findViewById(R.id.connectingToWifi_btnAbort);
    if (abort != null) {
      abort.setVisibility(View.VISIBLE);
      abort.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          isCameraSelectionStarted = false;
          setupCameraScan();
          releaseWakeLock();
          if (config_task != null && config_task.getStatus() == AsyncTask.Status.RUNNING) {
            config_task.cancel(true);
          }
        }
      });
    }

  }

  public synchronized void store_default_wifi_info() {
    clear_default_wifi_info();
    if (configuration != null) {
      String ssid_no_quote = convertToNoQuotedString(configuration.ssid());// no quote
      String current_security_type = configuration.security_type();
      String pwd_no_quote = null;
      settings.putString(PublicDefineGlob.PREFS_SAVED_WIFI_SSID, ssid_no_quote);
      settings.putBoolean(PublicDefineGlob.PREFS_SAVED_WIFI_HIDDEN_SSID, (configuration.wc() != null) && configuration.wc().hiddenSSID);
      if (current_security_type.startsWith("WPA")) {
        pwd_no_quote = convertToNoQuotedString(configuration.pass_string());
        settings.putString(PublicDefineGlob.PREFS_SAVED_WIFI_PWD, pwd_no_quote);
      } else if (current_security_type.startsWith("WEP")) {
        pwd_no_quote = convertToNoQuotedString(configuration.pass_string());
        settings.putString(PublicDefineGlob.PREFS_SAVED_WIFI_SEC_WEP_IDX, configuration.key_index());
        settings.putString(PublicDefineGlob.PREFS_SAVED_WIFI_PWD, pwd_no_quote);
        settings.putString(PublicDefineGlob.PREFS_SAVED_WIFI_SEC_WEP_AUTH, configuration.auth_method());
      }
    }
  }

  private void setBrandSpecificCameraImage() {
    ImageView cameraImage = (ImageView) findViewById(R.id.imgCam);
    if (isMotorolaFocus83()) {
      cameraImage.setImageResource(R.drawable.focus_83_big);
    } else if (isMotorolaFocus86()) {
      cameraImage.setImageResource(R.drawable.focus_86_big);
    } else if (isMotorolaFocus66()) {
      cameraImage.setImageResource(R.drawable.focus_66_big);
    } else if (isMotorolaFocus73()) {
      cameraImage.setImageResource(R.drawable.focus_73_big);
    } else if (isVtech931()) {
      cameraImage.setImageResource(R.drawable.default_no_brand_cam);
    } else if (isVtech921()) {
      cameraImage.setImageResource(R.drawable.default_no_brand_cam);
    } else {
      cameraImage.setImageResource(R.drawable.default_no_brand_cam);
    }
  }

  private void doMVRDialogSetup() {
    sendDefaultCameraValues();

    if (BuildConfig.ENABLE_SUBSCRIPTIONS) {
      new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          //show spinner
          try {
            boolean success = doSubscriptionFlow().get();
          } catch (Exception e) {
            // interrupted?
          }
          // hide spinner
          return null;
        }
      }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      showDialogMotionSetting();
    }
  }

  private void sendDefaultCameraValues() {
    String saved_token = settings.getString(PublicDefine.PREFS_SAVED_PORTAL_TOKEN, null);
    String saved_reg_id = settings.getString(PublicDefine.PREFS_GO_DIRECTLY_TO_CAMERA, null);

    // send commands

    String motionAreaCommand = "set_motion_area&value=&grid=1x1&zone=00";
    String tempHighOnCommand = "set_temp_hi_enable" + "&value=1";
    String tempLowOnCommand = "set_temp_lo_enable" + "&value=1";
    String soundDetectionOnCommand = "vox_enable";
    String recordCoolOffDurationCommand = "recording_cooloff_duration" + "&value=120";
    String recordActiveDurationCommand = "recording_active_duration" + "&value=90";

    if (cam_profile != null) {
      sendCommand(saved_token, cam_profile.getRegistrationId(), motionAreaCommand);
      sendCommand(saved_token, cam_profile.getRegistrationId(), tempHighOnCommand);
      sendCommand(saved_token, cam_profile.getRegistrationId(), tempLowOnCommand);
      sendCommand(saved_token, cam_profile.getRegistrationId(), soundDetectionOnCommand);
      sendCommand(saved_token, cam_profile.getRegistrationId(), recordCoolOffDurationCommand);
      sendCommand(saved_token, cam_profile.getRegistrationId(), recordActiveDurationCommand);
    } else {
      sendCommand(saved_token, saved_reg_id, motionAreaCommand);
      sendCommand(saved_token, saved_reg_id, tempHighOnCommand);
      sendCommand(saved_token, saved_reg_id, tempLowOnCommand);
      sendCommand(saved_token, saved_reg_id, soundDetectionOnCommand);
      sendCommand(saved_token, saved_reg_id, recordCoolOffDurationCommand);
      sendCommand(saved_token, saved_reg_id, recordActiveDurationCommand);
    }
  }

  private ListenableFuture<Boolean> doSubscriptionFlow() {
    final String saved_token = settings.getString(PublicDefine.PREFS_SAVED_PORTAL_TOKEN, null);
    subWizard = new SubscriptionWizard(saved_token, this, DeviceSingleton.getInstance().factory.build(cam_profile.toDeviceProfile()), false);
    return subWizard.verify();
  }

  private void showDialogMotionSetting() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(getString(R.string.setup_trial_dialog_text))
        .setTitle(getString(R.string.setup_trial_dialog_title))
        .setCancelable(false)
        .setNegativeButton(getString(R.string.decline_free_trial), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            disableMotionVideoRecording();
            dialog.dismiss();
          }
        }).setPositiveButton(getString(R.string.start_free_trial), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        enableMotionVideoRecording();
        dialog.dismiss();
      }
    });

    try {
      builder.show();
    } catch (Exception e) {
      //Log.d(TAG, "Exception when starting free trial dialog.:" + Log.getStackTraceString(e));
    }
  }

  private void enableMotionVideoRecording() {
    String saved_token = settings.getString(PublicDefine.PREFS_SAVED_PORTAL_TOKEN, null);
    String saved_reg_id = settings.getString(PublicDefine.PREFS_GO_DIRECTLY_TO_CAMERA, null);
    // send command
    String motionRecordingEnableCommand = PublicDefine.BM_HTTP_CMD_PART + "set_recording_parameter&value=11";

    settings.putBoolean(PublicDefine.IS_USER_ARGEED_WITH_MOTION_TRIGGER_RECORDING_PRIVACY_POLICY, true);

    if (cam_profile != null) {
      sendCommand(saved_token, cam_profile.getRegistrationId(), motionRecordingEnableCommand);
    } else {
      sendCommand(saved_token, saved_reg_id, motionRecordingEnableCommand);
    }
  }

  private void disableMotionVideoRecording() {
    final String saved_token = settings.getString(PublicDefine.PREFS_SAVED_PORTAL_TOKEN, null);
    final String saved_reg_id = settings.getString(PublicDefine.PREFS_GO_DIRECTLY_TO_CAMERA, null);

    String motionRecordingDisableCommand = PublicDefine.BM_HTTP_CMD_PART + "set_recording_parameter&value=01";
    if (cam_profile != null) {
      sendCommand(saved_token, cam_profile.getRegistrationId(), motionRecordingDisableCommand);
    } else {
      sendCommand(saved_token, saved_reg_id, motionRecordingDisableCommand);
    }
  }

  //End Steps

  private Dialog getVerifyKeyDialog() {
    final Dialog wait_dialog = new Dialog(this);
    wait_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    wait_dialog.setCancelable(false);
    wait_dialog.setContentView(R.layout.dialog_wait_for_connecting);
    TextView txtWaiting = (TextView) wait_dialog.findViewById(R.id.txtLoaderDesc);
    wait_dialog.setCancelable(true);
    wait_dialog.setOnCancelListener(new OnCancelListener() {

      @Override
      public void onCancel(DialogInterface dialog) {
        ProgressBar loader = (ProgressBar) wait_dialog.findViewById(R.id.imgLoader);
        if (loader != null) {
          loader.clearAnimation();
        }
      }
    });

    return wait_dialog;
  }

  private Dialog getSearchingNetworkDialog() {
    final Dialog scan_dialog = new Dialog(this);
    scan_dialog.setCancelable(false);
    scan_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    scan_dialog.setContentView(R.layout.dialog_wait_for_connecting);

    TextView txtWaiting = (TextView) scan_dialog.findViewById(R.id.txtLoaderDesc);
    txtWaiting.setText("Scanning Wifi");
    scan_dialog.setCancelable(true);
    scan_dialog.setOnCancelListener(new OnCancelListener() {

      @Override
      public void onCancel(DialogInterface dialog) {
        ProgressBar loader = (ProgressBar) scan_dialog.findViewById(R.id.imgLoader);
        if (loader != null) {
          loader.clearAnimation();
        }
        shouldContinueQueryWifiList = false;
      }
    });

    return scan_dialog;
  }

  private Dialog getAddNetworkDialog() {
    //Log.d(TAG, "getAddNetworkDialog");
    final Dialog dialog;
    dialog = new Dialog(this);
    dialog.setContentView(R.layout.bb_is_other_router);
    dialog.setTitle(getString(R.string.wireless_network_settings));
    dialog.setCancelable(true);
    final EditText pwd_text = (EditText) dialog.findViewById(R.id.text_key);
    pwd_text.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          v.setTransformationMethod(PasswordTransformationMethod.getInstance());

        }
        return false;
      }
    });

    Spinner spinner = (Spinner) dialog.findViewById(R.id.sec_type);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sec_type, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    spinner.setSelection(0);
    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        //Log.d(TAG, "Selected item is: " + arg2);
        LinearLayout ll = (LinearLayout) dialog.findViewById(R.id.linearLayout_pwd);
        if (ll != null) {
          ll.setVisibility(View.VISIBLE);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });

    // setup connect button
    Button connect = (Button) dialog.findViewById(R.id.connect_btn);
    connect.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        String pass_string = null;
        String ssid_string = null;
        EditText ssid_ = (EditText) dialog.findViewById(R.id.text_ssid);
        if (ssid_ == null) {
          return;
        }
        ssid_string = ssid_.getText().toString();
        EditText text = (EditText) dialog.findViewById(R.id.text_key);
        if (text == null) {
          return;
        }
        Util.hideSoftKeyboard(getApplicationContext(), getCurrentFocus());
        pass_string = text.getText().toString();

        WifiConfiguration _conf = new WifiConfiguration();
        _conf.SSID = "\"" + ssid_string + "\""; // Un-quoted
        _conf.hiddenSSID = true; //

        Spinner secType = (Spinner) dialog.findViewById(R.id.sec_type);
        switch (secType.getSelectedItemPosition()) {
          case 0: // Mixed WPA/WPA2 PSK
            _conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            _conf.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            _conf.preSharedKey = pass_string;
            break;
          case 1: // WPA
            _conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            _conf.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
            _conf.preSharedKey = pass_string;
            break;
          case 2: // WEP
            _conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            _conf.wepTxKeyIndex = 0;
            _conf.allowedKeyManagement.set(KeyMgmt.NONE);
            _conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            _conf.wepKeys[_conf.wepTxKeyIndex] = pass_string;
            break;
          default:// UNKNOWN
            break;
        }

        selectedWifiCon = _conf;

        start_configure_camera(_conf);

        dialog.cancel();
      }
    });

    Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
    cancel.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.cancel();
      }
    });

    return dialog;
  }

  private boolean configIsWEP(WifiConfiguration conf) {
    return conf.allowedKeyManagement.get(KeyMgmt.NONE) && (conf.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP104) || conf.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.WEP40));
  }

  private boolean configIsWPA(WifiConfiguration conf) {
    return conf.allowedKeyManagement.get(KeyMgmt.WPA_PSK) || conf.allowedKeyManagement.get(KeyMgmt.WPA_EAP);
  }

  private void showConnectingPage() {
    setContentView(R.layout.connecting_to_wifi);
    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();
    ImageView imgConn = (ImageView) findViewById(R.id.imgConnecting);
    if (imgConn != null) {
      // start waiting animation
      imgConn.setVisibility(View.VISIBLE);
      imgConn.setImageResource(R.drawable.wifi_connecting_anim);
      anim = (AnimationDrawable) imgConn.getDrawable();
      anim.start();
    }

    TextView txtConn = (TextView) findViewById(R.id.txtConnecting);
    if (cam_profile.isHasWiFiInfo()) {
      txtConn.setText(R.string.connecting_camera_to_wifi);
    } else {
      txtConn.setText(getString(R.string.finalizing_camera_setup));
    }
    TextView txtDesc = (TextView) findViewById(R.id.txtDesc);
    String desc_str = String.format(getResources().getString(R.string.please_wait_for_a_couple_of_minutes_while_camera_connects_to_your_network), cam_profile.getName());
    txtDesc.setText(desc_str);
  }

  private void check_and_reconnect_to_home() {
    WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    String homeSSID = settings.getString(
        PublicDefineGlob.PREFS_HOME_WIFI_SSID_NO_QUOTE, null);
    String savedSSID = settings.getString(
        PublicDefineGlob.PREFS_SAVED_WIFI_SSID, null);
    List<WifiConfiguration> wcs = wm.getConfiguredNetworks();
    String currentSSID = null;
    if (wm.getConnectionInfo() != null
        && wm.getConnectionInfo().getSSID() != null) {
      currentSSID = wm.getConnectionInfo().getSSID();
    }

    if (currentSSID != null &&
        !currentSSID.startsWith(PublicDefineGlob.DEFAULT_SSID_HD) &&
        (currentSSID.equalsIgnoreCase(convertToQuotedString(homeSSID)) ||
            currentSSID.equalsIgnoreCase(convertToQuotedString(savedSSID)) ||
            currentSSID.equalsIgnoreCase(homeSSID) ||
            currentSSID.equalsIgnoreCase(savedSSID))) {
      // phone has already connected --> do nothing
    } else {
      // reconnect to home WiFi
      if (homeSSID != null
          && !homeSSID.startsWith(PublicDefineGlob.DEFAULT_SSID_HD)) {
        for (WifiConfiguration wc : wcs) {
          if ((wc == null) || (wc.SSID == null))
            continue;

          if (wc.SSID.equalsIgnoreCase(convertToQuotedString(homeSSID)) ||
              wc.SSID.equalsIgnoreCase(homeSSID)) {
            wm.enableNetwork(wc.networkId, true);
            Log.d(TAG, "Reconnect to WiFi: " + wc.SSID);
            break;
          }
        }
      } else {
        for (WifiConfiguration wc : wcs) {
          if ((wc == null) || (wc.SSID == null))
            continue;

          if (wc.SSID.equalsIgnoreCase(convertToQuotedString(savedSSID)) ||
              wc.SSID.equalsIgnoreCase(savedSSID)) {
            wm.enableNetwork(wc.networkId, true);
            Log.d(TAG, "Reconnect to WiFi: " + wc.SSID);
            break;
          }
        }
      }
    }
  }

  private static String convertToNoQuotedString(String string) {
    String no_quoted_str = string;
    if (string != null && string.indexOf("\"") == 0 && string.lastIndexOf("\"") == string.length() - 1) {
      no_quoted_str = string.substring(1, string.lastIndexOf("\""));
    }
    return no_quoted_str;
  }

  private static String convertToQuotedString(String string) {
    return "\"" + string + "\"";
  }

  private void fade_outin_view(View v, int duration_ms) {
    Animation myFadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeout_in);
    myFadeAnimation.setDuration(duration_ms);
    myFadeAnimation.setAnimationListener(new FadeOutAnimationAndGoneListener(v));
    v.startAnimation(myFadeAnimation);
  }

  private void router_selection(List<ScanResult> results) {
    //Log.d(TAG, "router_selection");
    ImageView imgConn = (ImageView) findViewById(R.id.imgConnecting);
    if (imgConn != null) {
      imgConn.clearAnimation();
    }

    router_selection_with_layout(results);
  }

  private void router_selection_with_layout(List<ScanResult> results) {
    setContentView(R.layout.bb_is_wifi_selection);
    setupToolbar();
    shouldShowWifiScanningInMenu = true;
    invalidateOptionsMenu();
    if (results == null) {
      return;
    }


    ArrayList<NameAndSecurity> ap_list;
    final ListView wifi_list = (ListView) findViewById(R.id.wifi_list);
    ap_list = new ArrayList<NameAndSecurity>();
    for (ScanResult result : results) {
      /* 20120220:filter those camera network out of this list */
      if (!result.SSID.startsWith(PublicDefineGlob.DEFAULT_CAM_NAME)) {
        NameAndSecurity _ns = new NameAndSecurity(result.SSID, result.capabilities, result.BSSID);
        _ns.setLevel(result.level);
        _ns.setHideMac(true);
        ap_list.add(_ns);
      }
    }

    final AccessPointAdapter ap_adapter = new AccessPointAdapter(this, ap_list);

    wifi_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    wifi_list.setAdapter(ap_adapter);
    wifi_selected_position = -1;
    wifi_list.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        // last element is not a NameAndSec object
        wifi_selected_position = position;
        int startPosition = wifi_list.getFirstVisiblePosition();
        int endPosition = wifi_list.getLastVisiblePosition();
        ap_adapter.setSelectedPositision(wifi_selected_position);
        for (int i = 0; i <= endPosition - startPosition; i++) {
          if (wifi_list.getChildAt(i) != null) {
            ImageView checked = (ImageView) parent.getChildAt(i).findViewById(R.id.imgChecked);
            if (checked != null) {
              if (i + startPosition == position) {
                checked.setVisibility(View.VISIBLE);
              } else {
                checked.setVisibility(View.INVISIBLE);
              }
            }
          }
        }

      }
    });

    Button connect = (Button) findViewById(R.id.buttonConnect);
    connect.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (wifi_selected_position != -1) {
          NameAndSecurity ns = (NameAndSecurity) wifi_list.getItemAtPosition(wifi_selected_position);
          on_router_item_selected(ns);
        }
      }
    });

  }

  private void switchToCameraSelection(final List<ScanResult> wsResult, List<CameraBonjourInfo> bsResult, String mode) {
    if (wsResult == null && bsResult == null) {
      return;
    }

    CameraScanner scanner;

    if (mode.equalsIgnoreCase("wifi")) {
      scanner = wifiCameraScanner;
    } else {
      scanner = mdnsCameraScanner;
    }

    if (!isCameraSelectionStarted) {
      isCameraSelectionStarted = true;
      shouldShowWifiScanningInMenu = false;
    }

    List<ApScanBase> accessPointList = buildAccessPointList(wsResult, bsResult);

    boolean modified = false;
    if (deviceSelectionAdapter == null) {
      deviceSelectionAdapter = new CameraAccessPointListAdapter(this, accessPointList);
    } else {
      if (mode.equalsIgnoreCase("wifi")) {
        modified = deviceSelectionAdapter.setWifiResults(accessPointList);
      } else {
        modified = deviceSelectionAdapter.setMdnsResults(accessPointList);
      }
    }

    if (deviceSelectionList.getAdapter() == null) {
      deviceSelectionList.setAdapter(deviceSelectionAdapter);
    }

    if (!isActivityDestroyed && scanner.isCompleted()) {
      stopCameraScanner(mode);
      startCameraScanner(mode);
    }

    if (modified) {
      deviceSelectionAdapter.notifyDataSetChanged();
    }
  }

  private List buildAccessPointList(List<ScanResult> wsResult, List<CameraBonjourInfo> bsResult) {
    List<ApScanBase> accessPointList = new ArrayList<ApScanBase>();
    // Filter Wi-Fi scan result
    for (ScanResult result : wsResult) {
      // note that:ScanResult SSID has no quote
      for (String cameraSSID : PublicDefineGlob.CAMERA_SSID_LIST) {
        if (result.SSID.startsWith(cameraSSID)) {
          NameAndSecurity _ns = new NameAndSecurity(result.SSID, result.capabilities, result.BSSID);
          _ns.setShowSecurity(false);
          _ns.setHideMac(true);
          accessPointList.add(_ns);
          break;
        }
      }
    }
    // Filter Bonjour scan result
    for (CameraBonjourInfo info : bsResult) {
      //Log.d(TAG, info.toString());
      if (!deviceAlreadyInAccount(info)) {
        accessPointList.add(info);
      }
    }

    return accessPointList;
  }

  private boolean deviceAlreadyInAccount(CameraBonjourInfo info) {
    boolean ignore = false;
    for (com.hubble.devcomm.Device device : DeviceSingleton.getInstance().getDevices()) {
      if (info.getMac().equalsIgnoreCase(device.getProfile().getMacAddress())) {
        ignore = true;
        break;
      }
    }
    return ignore;
  }

  private WifiConfiguration buildWifiConfiguration(String checkSSID, String checkBSSID, NameAndSecurity ns) {
    WifiConfiguration newWC = new WifiConfiguration();
    newWC.hiddenSSID = false;
    newWC.SSID = checkSSID;
    newWC.BSSID = checkBSSID;
    newWC.status = WifiConfiguration.Status.ENABLED;
    // the following is the settings
    // that found to be working for ai-ball
    newWC.hiddenSSID = false;
    newWC.allowedAuthAlgorithms = ns.getAuthAlgorithm();
    newWC.allowedGroupCiphers = ns.getGroupCiphers();
    newWC.allowedKeyManagement = ns.getKeyManagement();
    if (ns.security.equalsIgnoreCase("WPA")) {
      newWC.preSharedKey = convertToQuotedString(PublicDefineGlob.DEFAULT_WPA_PRESHAREDKEY);
    }
    newWC.allowedPairwiseCiphers = ns.getPairWiseCiphers();
    newWC.allowedProtocols = ns.getProtocols();
    return newWC;
  }

  private void getWakeLock() {
    //Log.d(TAG, "Acquire WakeLock for adding camera.");
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    //Log.d(TAG, "Turn screen on for a bit");
    wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TURN ON Because of error");
    wl.setReferenceCounted(false);
    wl.acquire();

    if (getWindow() != null) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
  }

  private void showConfirmUpgradeDialogWhenSetupFailed(String fwVersion, DialogInterface.OnClickListener listener) {
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    dialogBuilder.setTitle(R.string.upgrade);
    dialogBuilder.setMessage(String.format(getString(R.string.ask_for_user_upgrade_when_setup_failed), fwVersion));
    dialogBuilder.setNegativeButton(R.string.No, listener);
    dialogBuilder.setPositiveButton(R.string.upgrade_now, listener);
    dialogBuilder.create().show();
  }

  private String getFilePathForFirmware(String downloadLink) {
    return Util.getFirmwareDirectory() + File.separator + Util.getLastPathComponent(downloadLink);
  }

  private void sendCommand(String apiKey, String deviceID, String command) {
    SendCommandAsyncTask sendCommandTask = new SendCommandAsyncTask(new IAsyncTaskCommonHandler() {
      @Override
      public void onPreExecute() {
        // TODO Auto-generated method stub

      }

      @Override
      public void onPostExecute(Object result) {
        // TODO Auto-generated method stub
      }

      @Override
      public void onCancelled() {
        // TODO Auto-generated method stub

      }
    });

    sendCommandTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, apiKey, deviceID, command);
  }

  public static String getSnapshotNameHashed(String str) {
    String hashed_str = null;
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] bytes = digest.digest(str.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : bytes) {
        sb.append(String.format("%02X", b));
      }
      hashed_str = sb.toString();
    } catch (NoSuchAlgorithmException e) {
      //Log.e(TAG, Log.getStackTraceString(e));
    }
    return hashed_str;
  }

  private boolean isVtech921() {
    return cam_profile.getModelId().contains("0921");
  }

  private boolean isVtech931() {
    return cam_profile.getModelId().contains("0931");
  }

  private boolean isVtechCamera() {
    return isVtech921() || isVtech931();
  }

  private boolean isMotorolaFocus66() {
    return cam_profile.getModelId().contains("0066") || cam_profile.getModelId().contains("0662") || cam_profile.getModelId().contains("1662");
  }

  private boolean isMotorolaFocus83() {
    return cam_profile.getModelId().contains("0083") || cam_profile.getModelId().contains("0085") || cam_profile.getModelId().contains("0854");
  }

  private boolean isMotorolaFocus73() {
    return cam_profile.getModelId().contains("0073");
  }

  private boolean isMotorolaFocus86() {
    return cam_profile.getModelId().contains("0086");
  }

  private CheckFirmwareUpdateTask createCheckFirmwareUpdateTask(int errCode, String strMessage) {
    /**
     * Son Nguyen: HCD-897
     */
    Log.i(TAG, "Setup camera failed, we need to check if need upgrade firmware or not.");
    String fwVersion = cam_profile.getFirmwareVersion();
    String regId = cam_profile.getRegistrationId();

    String modelId = cam_profile.getModelId();
    final String strMessageCopy = strMessage;
    final int errCodeCopy = errCode;
    return new CheckFirmwareUpdateTask(user_token, regId, fwVersion, modelId, null, new IAsyncTaskCommonHandler() {
      @Override
      public void onPreExecute() {
      }

      @Override
      public void onPostExecute(final Object result) {
        displaySetupFailedLayout(strMessageCopy, errCodeCopy);

        if (result instanceof CheckFirmwareUpdateResult) {
          final CheckFirmwareUpdateResult checkFirmwareUpdateResult = (CheckFirmwareUpdateResult) result;
          final Gson gson = new Gson();
          Log.i(TAG, gson.toJson(checkFirmwareUpdateResult));
          DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              if (which == DialogInterface.BUTTON_NEGATIVE) {
                // do nothing because failed setup layout was display
                Log.i(TAG, "User click Cancel");
              } else {
                if (checkFirmwareUpdateResult.isHaveNewFirmwareVersion()) {
                  showDownloadFirmwareDialog(checkFirmwareUpdateResult, gson);
                }
              }
            }
          };
          if (checkFirmwareUpdateResult.isHaveNewFirmwareVersion()) {
            showConfirmUpgradeDialogWhenSetupFailed(checkFirmwareUpdateResult.getCurrentFirmwareVersion(), listener);
          }
        }
      }

      @Override
      public void onCancelled() {
      }
    }, HubbleApplication.AppConfig.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false));
  }

  private void showDownloadFirmwareDialog(final CheckFirmwareUpdateResult checkFirmwareUpdateResult, final Gson gson) {
    final DownloadFirmwareDialog downloadFirmwareDialog = new DownloadFirmwareDialog();
    downloadFirmwareDialog.setCheckFirmwareUpdateResult(checkFirmwareUpdateResult);
    downloadFirmwareDialog.show(getSupportFragmentManager(), "download_firmware");
    downloadFirmwareDialog.setCommonDialogListener(new CommonDialogListener() {
      @Override
      public void onDialogPositiveClick(DialogFragment dialog) {
        if (downloadFirmwareDialog.getStatus() == DownloadFirmwareDialog.Status.RETRY_SETUP) {
          String key = PublicDefines.PREFS_CAMERA_NEED_UPGRADE_FIRMWARE_BEFORE_SETUP + checkFirmwareUpdateResult.getRegID();
          settings.putString(key, gson.toJson(checkFirmwareUpdateResult));
          Log.i(TAG, "KEY FOR CHECK FIRMWARE UPGRADE: " + key);
          setupCameraScan();
        }
      }

      @Override
      public void onDialogNeutral(DialogFragment dialog) {
      }

      @Override
      public void onDialogNegativeClick(DialogFragment dialog) {
      }
    });
  }

  private void displaySetupFailedLayout(String strMessage, int errCode) {
    setContentView(R.layout.connect_to_camera_failed);
    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();
    TextView txtFailed = (TextView) findViewById(R.id.connectToCameraFailed_txtDescription);
    String desc_str = String.format("%s.(%x)", strMessage, errCode);
    if (cam_profile != null && cam_profile.getRegistrationId() != null) {
      desc_str += String.format(getString(R.string.camera_udid), cam_profile.getRegistrationId());
    }
    txtFailed.setText(desc_str);
    Button connect = (Button) findViewById(R.id.cameraSetupFailed_btn_connect);
    connect.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        setupCameraScan();
      }
    });
  }

  private void releaseWakeLock() {
    /*
     * 20130201: hoang: issue 1260 release wakelock
     */
    if ((wl != null) && (wl.isHeld())) {
      wl.release();
      wl = null;
    }
    if (getWindow() != null) {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
  }

  private void clear_default_wifi_info() {
    // editor.remove(PublicDefine.PREFS_SAVED_WIFI_SEC);
    settings.remove(PublicDefineGlob.PREFS_SAVED_WIFI_PWD);
    settings.remove(PublicDefineGlob.PREFS_SAVED_WIFI_SEC_WEP_AUTH);
    settings.remove(PublicDefineGlob.PREFS_SAVED_WIFI_SEC_WEP_IDX);
    settings.remove(PublicDefineGlob.PREFS_SAVED_WIFI_SSID);
  }

  private void showSearchingNetworkDialog() {
    searchingNetworkDialog = getSearchingNetworkDialog();
    searchingNetworkDialog.show();
  }

  private void add_cam_success(String master_key) {
    //Log.d(TAG, "add_cam_success");
    configuration.setMasterKey(master_key);
    configuration.setCamProfiles(new LegacyCamProfile[]{cam_profile});
    config_task = new AutoConfigureCameras(RetreiveCameraLogActivity.this, new Handler(RetreiveCameraLogActivity.this));
    ((AutoConfigureCameras) config_task).setAddCameraStartTime(add_camera_start_time);
    config_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.configuration);
  }

  private void add_cam_failed(String error_desc) {
    //Log.d(TAG, "add_cam_failed");
    if (verifyKeyDialog == null && verifyKeyDialog.isShowing()) {
      verifyKeyDialog.dismiss();
    }

    AlertDialog.Builder builder;
    AlertDialog alert;
    builder = new AlertDialog.Builder(this);
    String message = String.format(getResources().getString(R.string.SingleCamConfigureActivity_add_cam_failed_1), error_desc);

    builder.setMessage(message).setCancelable(true).setPositiveButton(getResources().getString(R.string.SingleCamConfigureActivity_retry), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface diag, int which) {
        diag.cancel();
        AddCameraTask try_addCam = new AddCameraTask(cam_profile);
        String deviceAccessibilityMode = "upnp";
        int offset = TimeZone.getDefault().getRawOffset();
        String timeZone = String.format("%s%02d.%02d", offset >= 0 ? "+" : "-", Math.abs(offset) / 3600000, (Math.abs(offset) / 60000) % 60);
        try_addCam.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, user_token, cam_profile.getName(), cam_profile.getRegistrationId(), cam_profile.getModelId(), deviceAccessibilityMode, cam_profile.getFirmwareVersion(), timeZone);
        verifyKeyDialog = getVerifyKeyDialog();
        verifyKeyDialog.show();
      }
    }).setNegativeButton(getResources().getString(R.string.SingleCamConfigureActivity_exit), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface diag, int which) {
        releaseWakeLock();

        RetreiveCameraLogActivity.this.finish();
        diag.cancel();
      }
    });

    alert = builder.create();

    try {
      alert.show();
    } catch (Exception e) {
    }
  }

  class AddCameraTask extends AsyncTask<String, String, Integer> {

    private String usrToken;
    private String deviceName;
    private String registerId;
    private String deviceModelId;
    private String deviceAccessibilityMode;
    private String deviceFwVersion;
    private String timeZone;
    private String subscriptionType;
    private String _master_key;
    private String _error_desc;
    private LegacyCamProfile cp;

    public AddCameraTask(LegacyCamProfile cp) {
      this.cp = cp;
    }

    @Override
    protected Integer doInBackground(String... params) {
      int ret = -1;

      usrToken = params[0];
      deviceName = params[1];
      registerId = params[2]; // mac address
      deviceModelId = params[3];
      deviceAccessibilityMode = params[4];
      deviceFwVersion = params[5];
      timeZone = params[6];
      //Log.d(TAG, "deviceName: " + deviceName);
      //Log.d(TAG, "registration id: " + registerId);
      //Log.d(TAG, "deviceModelId: " + deviceModelId);
      //Log.d(TAG, "deviceAccessibilityMode: " + deviceAccessibilityMode);
      //Log.d(TAG, "deviceFwVersion: " + deviceFwVersion);
      //Log.d(TAG, "timeZone: " + timeZone);
      //Log.d(TAG, "Add camera " + deviceName + " to account.");

      try {
        AddNewDeviceResponse reg_res = Device.registerDevice(user_token, deviceName, registerId, deviceAccessibilityMode, deviceFwVersion, timeZone);
        if (reg_res != null) {
          if (reg_res.getStatus() == HttpURLConnection.HTTP_OK) {
            ret = ADD_CAM_SUCCESS;
            if (reg_res.getResponseData() != null) {
              _master_key = reg_res.getResponseData().getAuth_token();
            }
          } else {
            ret = ADD_CAM_FAILED_WITH_DESC;
            //Log.d(TAG, "Add camera res code: " + reg_res.getStatus());
          }
        } else {
          ret = ADD_CAM_FAILED_UNKNOWN;
        }
      } catch (Exception se) {
        // Connection Timeout - Server unreachable ???
        //Log.e(TAG, Log.getStackTraceString(se));
        ret = ADD_CAM_FAILED_SERVER_DOWN;
      }
      return Integer.valueOf(ret);
    }

    /* UI thread */
    protected void onPostExecute(Integer result) {
      if (result.intValue() == ADD_CAM_SUCCESS) {
        RetreiveCameraLogActivity.this.add_cam_success(_master_key);
      } else {
        if (result.intValue() == ADD_CAM_FAILED_SERVER_DOWN) {
          _error_desc = getResources().getString(R.string.server_connection_timeout);
        }
        RetreiveCameraLogActivity.this.add_cam_failed(_error_desc);
      }
    }
  }

}