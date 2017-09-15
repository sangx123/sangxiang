package com.firmware;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
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
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
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

import com.google.gson.Gson;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.registration.JWebClient;
import com.hubble.registration.PublicDefine;
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
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubble.registration.tasks.ConnectToNetworkTask;
import com.hubble.registration.tasks.QueryCameraWifiListTask;
import com.hubble.registration.tasks.VerifyNetworkKeyTask;
import com.hubble.registration.tasks.WifiScan;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubble.registration.ui.AccessPointAdapter;
import com.hubble.registration.ui.CameraAccessPointListAdapter;
import com.hubble.registration.ui.CommonDialogListener;
import com.hubble.registration.ui.FadeOutAnimationAndGoneListener;
import com.hubble.registration.ui.UploadFwAndWaitForUpgradeCompleteDialog;
import com.hubble.registration.ui.ViewPagerParallax;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import base.hubble.PublicDefineGlob;

@SuppressWarnings("deprecation")
public class FirmwareCamConfigureActivity extends ActionBarActivity implements IWifiScanUpdater, Handler.Callback, IChangeNameCallBack, FirmwareCameraScanner.ICameraScanCompleted {
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
  public static final String IS_CAMERA = "is_camera";
  public static final String MODEL_ID = "model_id";
  private int wifi_scan_purpose;
  private UpgradeDialogFirmware upgradeDialog;
  private static final String FW_VERSION_DOT = "\\.";
  private String modelId;

  public enum CameraModel {
    VC931, VC921, FOCUS66, FOCUS73, FOCUS83, FOCUS86
  }

  /* Transient data - Only valid during the Configuration process */
  private WifiConfiguration selectedWifiCon;
  private String user_token;
  private CamConfiguration configuration;
  private LegacyCamProfile cam_profile;
  private AnimationDrawable anim = null;
  private AsyncTask<CamConfiguration, String, String> config_task = null;
  private long add_camera_start_time = -1;
  private FirmwareCameraScanner wifiCameraScanner;
  private FirmwareCameraScanner mdnsCameraScanner;
  private boolean goToCamLiveView = false;
  private boolean isActivityDestroyed;
  private Dialog verifyKeyDialog;
  private Dialog searchingNetworkDialog;
  private boolean shouldShowWifiScanningInMenu = false;
  private ListView deviceSelectionList;
  private Context mContext;
//  private Device mDevice;
  private CameraModel mCameraModelToPair;

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
  private Object mNetwork;
  private boolean isMobileDataEnabled = false;
  private SecureConfig settings = HubbleApplication.AppConfig;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;

    Bundle extra = getIntent().getExtras();
    if (extra != null) {
      user_token = extra.getString(str_userToken);
      modelId = extra.getString(MODEL_ID,"");
      extra.remove(str_userToken);
    }

    /*
     * IMPORTANT: From Android 5.0 (Lollipop), setMobileDataEnabled & getMobileDataEnabled
		 * methods are removed. App can't auto switch mobile network on/off anymore.
		 * Thus, don't expect below code has effect.
		 */
    if (hasMobileNetwork(this.getApplicationContext())) {
      isMobileDataEnabled = isMobileDataEnabled(this.getApplicationContext());
      Log.d(TAG, "Prepare to setup, isMobileNetworkEnabled? " + isMobileDataEnabled);
      if (isMobileDataEnabled == true) {
                /* Temporarily turn off mobile data. */
        Log.d(TAG, "Temporarily disable mobile data.");
        setMobileDataEnabled(this.getApplicationContext(), false);
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
          mNetwork = network;
          isWifiInterfaceAvailable = true;
        }
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

    initialSetup();
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

    check_and_reconnect_to_home();

        /* Force disable wifi interface for Android 5.0 */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      forceWifiInterfaceEnabled(false);
    }

    if (isMobileDataEnabled == true) {
            /* Reverse mobile data state. */
      Log.d(TAG, "Restore mobile data state.");
      setMobileDataEnabled(this.getApplicationContext(), true);
    }
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

      // Refresh wifi list by getting from camera, dont start WifiScan here.
      if (cam_profile != null) {
        shouldContinueQueryWifiList = true;
        doQueryWifiListTask(cam_profile.getFirmwareVersion());
      }

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
    finish();
  }

  @Override
  public void update_cam_failed() {
    FirmwareCamConfigureActivity.this.finish();
  }

  @Override
  public boolean handleMessage(Message arg0) {
    switch (arg0.what) {
      case ConnectToNetworkTask.MSG_CONNECT_TO_NW_DONE:
        Thread mThread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              Thread.sleep(1000);

              final String currentFirmWare = JWebClient.downloadAsString("http://192.168.193.1/?action=command&command=get_version").replace("get_version: ", "");

              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Log.e("current fw :", currentFirmWare);
                  String firmwareVersion = settings.getString(modelId, "fw");
                  TextView textView = (TextView) FirmwareCamConfigureActivity.this.findViewById(R.id.txtConnecting);
                  textView.setText(textView.getText().toString().replace("Connecting", "Upgrading"));
                  if (isNeedUseOtherOTAFolder(currentFirmWare) && !modelId.equals("0086")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.should_update_firmware_to_011700), Toast.LENGTH_LONG).show();
                    showUpdateDialog(firmwareVersion, true);
                  } else {
                    if (isThisVersionGreaterThan(firmwareVersion, currentFirmWare)) {
                      showUpdateDialog(firmwareVersion, false);
                    } else {
                      showFWDialog(String.format(getString(R.string.no_fw_upgrade_found, currentFirmWare)));
                    }
                  }
                }
              });
            } catch (SocketException ex) {
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  connectToCameraFailed("Camera");
                }
              });
            }catch (IOException e) {
              e.printStackTrace();
            } catch (InterruptedException e) {
              e.printStackTrace();
            } catch (NullPointerException e) {
              e.printStackTrace();
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  connectToCameraFailed("Camera");
                }
              });
            }
          }
        });
        mThread.start();
        break;
      case ConnectToNetworkTask.MSG_CONNECT_TO_NW_FAILED:
        // Connect to camera network failed OR connect to wifi network
        // failed
        final String checkSSID = (String) arg0.obj;
        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            try {
              connectToCameraFailed(checkSSID);
            } catch (Exception ignored) {
            }

          }
        });
        break;
      case PublicDefine.MSG_AUTO_CONF_SUCCESS:
        Log.d(TAG, "Receive msg MSG_AUTO_CONF_SUCCESS");
//        syncDevices();
        break;
      case PublicDefine.MSG_AUTO_CONF_FAILED:
        Log.d(TAG, "Receive msg MSG_AUTO_CONF_FAILED");
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

            findViewById(R.id.cameraSetupFailed_btn_help).setVisibility(View.GONE);
            connect.setOnClickListener(new OnClickListener() {

              @Override
              public void onClick(View v) {
                showSearchingNetworkDialog();
                WifiScan ws = new WifiScan(FirmwareCamConfigureActivity.this, FirmwareCamConfigureActivity.this);
                ws.setSilence(true);
                wifi_scan_purpose = SCAN_FOR_ALL;
                ws.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Scan now");
              }
            });
          }
        });

        break;
      case VerifyNetworkKeyTask.MSG_NETWORK_KEY_VERIFY_PASSED:
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
    } else {
      setupCameraScan();
    }
  }

  private void deviceSelectionScreen() {
    setContentView(R.layout.device_setup_device_selection);
    findViewById(R.id.camera_setup_buy_button).setVisibility(View.GONE);
    ListView deviceListView = (ListView) findViewById(R.id.deviceSelection_deviceListView);
    final FirmwareDeviceSelectionAdapter deviceAdapter = new FirmwareDeviceSelectionAdapter(this);
    deviceListView.setAdapter(deviceAdapter);
    deviceListView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FirmwareDeviceSelectionAdapter.DeviceSelectionDevice selectedDevice = (FirmwareDeviceSelectionAdapter.DeviceSelectionDevice) deviceAdapter.getItem(position);
        mCameraModelToPair = selectedDevice.getModel();
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

    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      btnBuy.setVisibility(View.GONE);
    }

    ImageView camPairImage = (ImageView) findViewById(R.id.imgCamThree);
    ImageView camPlugImage = (ImageView) findViewById(R.id.imageView01);
    ImageView camLEDImage1 = (ImageView) findViewById(R.id.imageView03);
    ImageView camLEDImage2 = (ImageView) findViewById(R.id.imageViewSnapShot);
    TextView camPairButton = (TextView) findViewById(R.id.cameraInstruction_pressPairButton);
    TextView camConnectPower = (TextView) findViewById(R.id.cameraInstruction_connectPowerCord);

    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      switch (mCameraModelToPair) {
        /*
        case VC921:
          camPairImage.setBackgroundResource(R.drawable.vtech_921_pair);
          camPlugImage.setImageResource(R.drawable.vtech_921_cord);
          camLEDImage1.setImageResource(R.drawable.vtech_921_light1);
          camLEDImage2.setImageResource(R.drawable.vtech_921_light2);
          camPairButton.setText(camPairButton.getText() + " " + getString(R.string.camera_pair_instructions_921_portion));
          break;
        case VC931:
          camPairImage.setBackgroundResource(R.drawable.vtech_931_pair);
          camPlugImage.setImageResource(R.drawable.vtech_931_cord);
          camLEDImage1.setImageResource(R.drawable.vtech_931_light);
          camLEDImage2.setImageResource(R.drawable.vtech_931_light);
          camConnectPower.setText(camConnectPower.getText() + " " + getString(R.string.do_not_connect_ethernet_cable));
          camPairButton.setText(camPairButton.getText() + " " + getString(R.string.camera_pair_instructions_931_portion));
          break;
        */
        default:
          camPairImage.setBackgroundResource(R.drawable.camera_instruction_animation);
          AnimationDrawable animationDrawable = (AnimationDrawable) camPairImage.getBackground();
          animationDrawable.start();
      }
    } else {
      AnimationDrawable animationDrawable = (AnimationDrawable) camPairImage.getBackground();
      animationDrawable.start();
    }

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
    if (BuildConfig.FLAVOR.contains("hubble") || BuildConfig.FLAVOR.contains("hubblenew")) {
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

    btnBuy.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
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

    fade_outin_view(camLEDImage2, 500);
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
          wifiCameraScanner = new FirmwareCameraScanner(this.getApplicationContext(), this, mode, modelId);
        }
        wifiCameraScanner.run();
      }
    } else {
      if (mdnsScanning.compareAndSet(false, true)) {
        //Log.d(TAG, "MDNS -> Scan starting...");
        if (mdnsCameraScanner == null) {
          mdnsCameraScanner = new FirmwareCameraScanner(this.getApplicationContext(), this, mode, modelId);
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
          CameraBonjourInfo info = (CameraBonjourInfo) selectCamera;
          cam_profile = new LegacyCamProfile(info.getCameraAddress(), info.getMac());
          cam_profile.setName(info.getCameraName());
          showConnectingToCameraView(info.getCameraName());
          beginSetupCamera(info);
        } else if (selectCamera instanceof NameAndSecurity) {
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
          WifiManager w = (WifiManager) FirmwareCamConfigureActivity.this.getSystemService(Context.WIFI_SERVICE);
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
          final String gatewayIp = Util.getGatewayIp(FirmwareCamConfigureActivity.this);
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
              ConnectToNetworkTask connect_task = new ConnectToNetworkTask(FirmwareCamConfigureActivity.this, new Handler(FirmwareCamConfigureActivity.this));
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
    setContentView(R.layout.connecting_to_camera);
    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();
    ImageView imgConn = (ImageView) findViewById(R.id.imgConnecting);
    if (imgConn != null) {
      // start waiting animation
      imgConn.setVisibility(View.VISIBLE);
      imgConn.setImageResource(R.drawable.camera_connecting_anim);
      anim = (AnimationDrawable) imgConn.getDrawable();
      anim.start();
    }
    TextView txtConn = (TextView) findViewById(R.id.txtConnecting);
    String strConn = String.format(getResources().getString(R.string.connecting_to_camera_wifi), name);
    txtConn.setText(strConn);
    TextView txtDesc = (TextView) findViewById(R.id.txtDesc);
    txtDesc.setText(R.string.this_may_take_up_to_a_minute);
  }

//  private void showUpgradingToCameraView(String name) {
//    setContentView(R.layout.connecting_to_camera);
//    setupToolbar();
//    shouldShowWifiScanningInMenu = false;
//    invalidateOptionsMenu();
//    ImageView imgConn = (ImageView) findViewById(R.id.imgConnecting);
//    if (imgConn != null) {
//      // start waiting animation
//      imgConn.setVisibility(View.VISIBLE);
//      imgConn.setImageResource(R.drawable.camera_connecting_anim);
//      anim = (AnimationDrawable) imgConn.getDrawable();
//      anim.start();
//    }
//    TextView txtConn = (TextView) findViewById(R.id.txtConnecting);
//    String strConn = String.format(getResources().getString(R.string.upgrading_to_camera_wifi), name);
//    txtConn.setText(strConn);
//    TextView txtDesc = (TextView) findViewById(R.id.txtDesc);
//    txtDesc.setText(R.string.this_may_take_up_to_a_minute);
//  }

  private void beginSetupCamera(final CameraBonjourInfo cameraInfo) {
    //Log.d(TAG, "beginSetupCamera - LAN:" + (cameraInfo != null));
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
          int k = 5;
          while (k-- > 0 && isActivityDestroyed == false) {
            if (isWifiInterfaceAvailable == true) {
              break;
            }

            Log.i(TAG, "Wifi interface is still not available, waiting...");

            try {
              Thread.sleep(1000);
            } catch (Exception e) {
            }
          }
        }

//        cameraVerifyPassword(cameraInfo);
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

    findViewById(R.id.cameraSetupFailed_btn_help).setVisibility(View.GONE);
  }

  private WifiConfiguration findCameraAP(String checkSSID, String checkBSSID) {
    WifiManager w = (WifiManager) FirmwareCamConfigureActivity.this.getSystemService(Context.WIFI_SERVICE);
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
    while (k-- > 0 && isActivityDestroyed == false) {
      try {
        regId = HTTPRequestSendRecvTask.getUdid(gatewayIp, PublicDefineGlob.DEVICE_PORT, cam_profile.getBasicAuth_usr(), cam_profile.getBasicAuth_pass());
        Log.i(TAG, "UDID: " + regId);
        if (regId != null) {
          break;
        } else {
          Log.i(TAG, "Cannot get camera regId or modelId -> retry");
        }
      } catch (ConnectException e2) {
        Log.e(TAG, Log.getStackTraceString(e2));
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
        Log.e(TAG, Log.getStackTraceString(e));
      }
    }

    String fw_version = null;
    k = 5;
    while (k-- > 0 && isActivityDestroyed == false) {
      try {
        fw_version = HTTPRequestSendRecvTask.getFirmwareVersion(gatewayIp, PublicDefineGlob.DEVICE_PORT, cam_profile.getBasicAuth_usr(), cam_profile.getBasicAuth_pass());
        Log.i(TAG, "Firmware version: " + fw_version);
        if (fw_version != null) {
          break;
        } else {
          Log.i(TAG, "Cannot get fw_version -> retry");
        }
      } catch (SocketException e1) {
        Log.e(TAG, Log.getStackTraceString(e1));
      }

      try {
        Thread.sleep(1500);
      } catch (InterruptedException e) {
      }
    }
    cam_profile.setFirmwareVersion(fw_version);

    final String key = "camera_need_upgrade_firmware_before_setup" + regId;
    String jsonCheckFwUpgradeResult = settings.getString(key, null);

    Log.i(TAG, "KEY CHECK: " + key);
    if (jsonCheckFwUpgradeResult != null) {
      Log.i(TAG, "JSON " + jsonCheckFwUpgradeResult);
      try {
        Gson gson = new Gson();
        final CheckFirmwareUpdateResult checkFwUpgradeResult = gson.fromJson(jsonCheckFwUpgradeResult, CheckFirmwareUpdateResult.class);
        checkFwUpgradeResult.setInetAddress(gatewayIp);
        final CommonDialogListener uploadFirmwareDialogListener = new CommonDialogListener() {

          @Override
          public void onDialogPositiveClick(DialogFragment dialog) {
            UploadFwAndWaitForUpgradeCompleteDialog uploadDialog = (UploadFwAndWaitForUpgradeCompleteDialog) dialog;
            if (uploadDialog.getStatus() == UploadFwAndWaitForUpgradeCompleteDialog.Status.UPGRADED) {
              settings.remove(key);
              initialSetup();
            }
          }

          @Override
          public void onDialogNeutral(DialogFragment dialog) {

          }

          @Override
          public void onDialogNegativeClick(DialogFragment dialog) {

          }
        };
        // usually: fw_version equals
        // checkFwUpgradeResult.getCurrentFirmwareVersion
        if (checkFwUpgradeResult.getCurrentFirmwareVersion().equalsIgnoreCase(fw_version)) {
          // this camera need to upgrade firmware before setup
          final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              if (which == DialogInterface.BUTTON_POSITIVE) {
                String fwFilePath = getFilePathForFirmware(checkFwUpgradeResult.getFirmwareDownloadLink());
                String modelId = cam_profile != null ? cam_profile.getModelId() : null;
                UploadFwAndWaitForUpgradeCompleteDialog uploadDialog = new UploadFwAndWaitForUpgradeCompleteDialog(checkFwUpgradeResult, fwFilePath, modelId);
                uploadDialog.show(getSupportFragmentManager(), "upload_firmware_dialog");
                uploadDialog.setCommonDialogListener(uploadFirmwareDialogListener);
              }
            }

          };
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              showConfirmUpgradeDialogWhenReSetup(listener);
            }
          });

        }
      } catch (Exception ex) {
        Log.i(TAG, "Cast saved PREFS_CAMERA_NEED_UPGRADE_FIRMWARE_BEFORE_SETUP failed.");
        ex.printStackTrace();
      }
    }
    Log.i(TAG, String.format("RegId %s, fw version %s", regId, fw_version));
    if (regId != null && fw_version != null) {
      if (cameraObject != null && cameraObject instanceof CameraBonjourInfo) {
        this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            skipWifiSetup();
          }
        });
        return;
      }
      if (PublicDefine.shouldUseNewSetupFlow(cam_profile.getModelId())) {
        // get wifi list from camera to show on UI
        FirmwareCamConfigureActivity.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            //Log.d(TAG, "fw version: " + cam_profile.getFirmwareVersion());
            if (isFwVersionOutdated(cam_profile.getFirmwareVersion())) {
              String errorMsg = getString(R.string.the_camera_fw_version_is_outdated_and_is_not_supported);
              camera_config_failed(0, errorMsg);
            } else {
              doQueryWifiListTask(cam_profile.getFirmwareVersion());
            }
          }
        });
      } else if (cam_profile.getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_MBP931)) {
        doQueryWifiListTask(cam_profile.getFirmwareVersion());
      } else {
        FirmwareCamConfigureActivity.this.runOnUiThread(new Runnable() {
          public void run() {
            // GO to scan for all network and select router
            WifiScan ws1 = new WifiScan(FirmwareCamConfigureActivity.this, FirmwareCamConfigureActivity.this);
            ws1.setSilence(true);
            wifi_scan_purpose = SCAN_FOR_ALL;
            ws1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Scan now");
          }
        });
      }
    } else {
      FirmwareCamConfigureActivity.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          connectToCameraFailed(cam_profile.getName());
        }
      });
    }

  }

  private void skipWifiSetup() {

    cam_profile.setHasWiFiInfo(false);
    showConnectingPage();
    configuration = createCamConfiguration(null, null, null, null, null);
    configuration.setCamProfiles(new LegacyCamProfile[]{cam_profile});
//    AsyncTask<LegacyCamProfile, String, Boolean> config_task_lan = new ConfigureLANCamera(this, new Handler(this));
//    ((ConfigureLANCamera) config_task_lan).setStartTime(add_camera_start_time);
//    config_task_lan.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cam_profile);
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

    return isOutdated;
  }

  private void doQueryWifiListTask(final String fw_version) {
    final QueryCameraWifiListTask getWifiList = new QueryCameraWifiListTask(this.getApplicationContext(), fw_version, cam_profile.getModelId(), cam_profile.getRegistrationId());
    Log.i(TAG, "Start query wifi list task, camera IP: " + cam_profile.get_inetAddress().getHostAddress());
    getWifiList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cam_profile.getHostInetAddress().getHostAddress());

    // get results in background to avoid hang UI
    Thread worker = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          final ArrayList<CameraWifiEntry> wifiListFromCamera = (ArrayList<CameraWifiEntry>) getWifiList.get(10000, TimeUnit.MILLISECONDS);

          if (wifiListFromCamera != null
              && !wifiListFromCamera.isEmpty()) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Log.d(TAG, "Show router list from camera");
                start_time_from_get_rt_list = System.currentTimeMillis();
                routerSelectionNew(wifiListFromCamera);
              }
            });
            shouldContinueQueryWifiList = false;
          } else {
            Log.d(TAG, "WiFi list is null");

            if (shouldContinueQueryWifiList == true) {
              try {
                Thread.sleep(3000);
              } catch (Exception e) {
                e.printStackTrace();
              }

              runOnUiThread(new Runnable() {

                @Override
                public void run() {
                  doQueryWifiListTask(fw_version);
                }
              });
            }
          }

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
        Log.d(TAG, "CameraWifiEntry, ssid: " + result.getSsid() + ", ssid_no_quote: " + result.getSsidNoQuote());
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
        wpa_conf.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
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
      WifiManager w = (WifiManager) FirmwareCamConfigureActivity.this.getSystemService(Context.WIFI_SERVICE);

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
          pwd_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
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
//          start_configure_camera(wpa_conf);
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
          pwd_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
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
//          start_configure_camera(wep_conf);
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

    AlertDialog.Builder builder = new AlertDialog.Builder(FirmwareCamConfigureActivity.this);
    Spanned msg = Html.fromHtml("<big>" + getResources().getString(R.string.ssid_without_password_is_not_supported_due_to_security_concern) + "</big>");
    builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    return builder.create();

  }

  private void configureOPEN_2(String ssid) {
    // OPEN
    configuration = createCamConfiguration(ssid, "OPEN", "", null, null);
    configuration.setWifiConf(FirmwareCamConfigureActivity.this.selectedWifiCon);

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
    configuration.setWifiConf(FirmwareCamConfigureActivity.this.selectedWifiCon);

    Vector<String> dev_list = new Vector<String>(1);
    dev_list.addElement(cam_profile.getRegistrationId());
    configuration.setDeviceList(dev_list);
  }

  private void configureWPA_2(WifiConfiguration conf, String ssid) {
    configuration = createCamConfiguration(ssid, "WPA/WPA2", conf.preSharedKey, null, null);
    configuration.setWifiConf(FirmwareCamConfigureActivity.this.selectedWifiCon);

    Vector<String> dev_list = new Vector<String>(1);
    dev_list.addElement(cam_profile.getRegistrationId());
    configuration.setDeviceList(dev_list);
  }

  private void camera_config_failed(int errCode, String errMsg) {
    Log.d(TAG, "Add camera failed.");
    clear_default_wifi_info();

    if (!PublicDefine.shouldUseNewSetupFlow(cam_profile.getModelId())) {
      if (errCode != FirmwareCamConfigureActivity.ADD_CAM_FAILED_WITH_DESC) {
//        RemoveDeviceTask remove = new RemoveDeviceTask();
//        remove.execute(cam_profile.getRegistrationId(), user_token);
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
        case FirmwareCamConfigureActivity.ADD_CAM_FAILED_WITH_DESC:
          strMessage = "This camera is not registered. Setup camera failed";
          break;
        default:
          strMessage = getString(R.string.SingleCamConfigureActivity_conf_cam_failed_1);
          break;
      }
    }

    releaseWakeLock();
  }

  private void showAbortPage() {

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
          initialSetup();
          releaseWakeLock();
          if (config_task != null && config_task.getStatus() == AsyncTask.Status.RUNNING) {
            config_task.cancel(true);
          }
        }
      });
    }

  }

  //End Steps

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
        shouldContinueQueryWifiList = false;
      }
    });

    return scan_dialog;
  }

  private Dialog getAddNetworkDialog() {
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

//        start_configure_camera(_conf);

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

    // remove all CameraHD-XXXXXXXXXX SSIDs
    if (wcs != null) {
      for (WifiConfiguration wc : wcs) {
        if ((wc == null) || (wc.SSID == null))
          continue;
        if (wc.SSID.startsWith(PublicDefine.DEFAULT_SSID_HD) ||
            (convertToNoQuotedString(wc.SSID).startsWith(PublicDefine.DEFAULT_SSID_HD))) {
          wm.removeNetwork(wc.networkId);
          Log.d(TAG, "Remove SSID: " + wc.SSID);
        }
      }
      boolean saveConfig = wm.saveConfiguration();
      Log.d(TAG, "Save wifi configuration? " + saveConfig);
    }

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
      Log.d(TAG, "Result SSID: " + result.SSID);
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

    FirmwareCameraScanner scanner;

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
    if (wsResult != null) {
      for (ScanResult result : wsResult) {
        // note that:ScanResult SSID has no quote
        if (result.SSID != null
            && (result.SSID.startsWith(PublicDefineGlob.DEFAULT_SSID_HD) ||
            result.SSID.startsWith(PublicDefineGlob.DEFAULT_SSID) ||
            result.SSID.startsWith(PublicDefineGlob.DEFAULT_VTECH_SSID_HD) || result.SSID.startsWith(PublicDefineGlob.DEFAULT_SSID_SENSOR))) {
          NameAndSecurity _ns = new NameAndSecurity(result.SSID, result.capabilities, result.BSSID);
          _ns.setShowSecurity(false);
          _ns.setHideMac(true);
          accessPointList.add(_ns);
        }
      }
    }
    // Filter Bonjour scan result
    if (bsResult != null) {
      for (CameraBonjourInfo info : bsResult) {
        accessPointList.add(info);
      }
    }

    return accessPointList;
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

  private String getFilePathForFirmware(String downloadLink) {
    return Util.getFirmwareDirectory() + File.separator + Util.getLastPathComponent(downloadLink);
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


  private void displaySetupFailedLayout(final String strMessage, int errCode) {

    setContentView(R.layout.connect_to_camera_failed);
    setupToolbar();
    shouldShowWifiScanningInMenu = false;
    invalidateOptionsMenu();
    TextView txtFailed = (TextView) findViewById(R.id.connectToCameraFailed_txtDescription);
    String desc_str = getString(R.string.camera_setup_failed_message);
    if (cam_profile != null) {
      desc_str += String.format(getString(R.string.camera_udid), cam_profile.getRegistrationId());
    }
    txtFailed.setText(desc_str);

    findViewById(R.id.cameraSetupFailed_btn_connect).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        initialSetup();
      }
    });

    final Context context = this;
    findViewById(R.id.cameraSetupFailed_btn_help).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        new AlertDialog.Builder(context).setTitle(getString(R.string.help)).setMessage(strMessage).setCancelable(true).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface diag, int which) {
            diag.dismiss();
          }
        }).create().show();
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

    settings.remove(PublicDefineGlob.PREFS_SAVED_WIFI_PWD);
    settings.remove(PublicDefineGlob.PREFS_SAVED_WIFI_SEC_WEP_AUTH);
    settings.remove(PublicDefineGlob.PREFS_SAVED_WIFI_SEC_WEP_IDX);
    settings.remove(PublicDefineGlob.PREFS_SAVED_WIFI_SSID);
  }

  private void showSearchingNetworkDialog() {
    searchingNetworkDialog = getSearchingNetworkDialog();
    searchingNetworkDialog.show();
  }

  public boolean hasMobileNetwork(Context context) {
    boolean hasMobileNetwork = false;
    ConnectivityManager connMan = (ConnectivityManager)
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mobileInfo = connMan.getNetworkInfo(
            ConnectivityManager.TYPE_MOBILE);
    if (mobileInfo != null) {
      hasMobileNetwork = true;
    } else {
      hasMobileNetwork = false;
    }

    return hasMobileNetwork;
  }

  public boolean isMobileDataEnabled(Context context) {
    boolean isEnabled = false;
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      try {
        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
        iConnectivityManagerField.setAccessible(true);
        final Object iConnectivityManager = iConnectivityManagerField.get(conman);
        final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
        final Method getMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
        if (getMobileDataEnabledMethod != null) {
          getMobileDataEnabledMethod.setAccessible(true);
          isEnabled = (Boolean) getMobileDataEnabledMethod.invoke(iConnectivityManager);
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    } else {
      isEnabled = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
    }

    return isEnabled;
  }

  public void setMobileDataEnabled(Context context, boolean enabled) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      try {
        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
        iConnectivityManagerField.setAccessible(true);
        final Object iConnectivityManager = iConnectivityManagerField.get(conman);
        final Class iConnectivityManagerClass = Class.forName(
                iConnectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = iConnectivityManagerClass.
                getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        if (setMobileDataEnabledMethod != null) {
          setMobileDataEnabledMethod.setAccessible(true);
          setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  private void showUpdateDialog(final String firmwareVersion, final boolean isFW17) {
    upgradeDialog = new UpgradeDialogFirmware(FirmwareCamConfigureActivity.this, firmwareVersion, modelId, isFW17, new IUpgradeCallbackFirmware() {
      @Override
      public void onUpgradeSucceed() {
        finish();
      }

      @Override
      public void onUpgradeFail() {
        finish();
      }

      @Override
      public void onDownloadSucceed() {
      }

      @Override
      public void onDownloadFailded() {
      }
    });
    upgradeDialog.setCancelable(false);
    upgradeDialog.show(getSupportFragmentManager(), "upgrade");
  }

  public static boolean isThisVersionGreaterThan(String version1, String version2) {
    // version format 01.01.01
    String[] versions1 = version1.split(FW_VERSION_DOT);
    String[] versions2 = version2.split(FW_VERSION_DOT);
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

  private void showFWDialog(String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(FirmwareCamConfigureActivity.this);
    Spanned spannedMsg = Html.fromHtml("<big>" + message + "</big>");
    builder.setMessage(spannedMsg).setIcon(R.drawable.cam_icon).setTitle(getString(R.string.updating)).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(@NotNull DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
              }
            }
    );
    builder.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        dialog.dismiss();
      }
    }).create().show();
  }

  public boolean isNeedUseOtherOTAFolder(String mCurrentFwVersion) {
    return (isThisVersionGreaterThan("01.17.00", mCurrentFwVersion));
  }

}