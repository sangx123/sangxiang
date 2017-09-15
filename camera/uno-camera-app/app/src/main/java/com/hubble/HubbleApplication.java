package com.hubble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.app.Application;
import com.blinkhd.CircularLogFile;

import com.crittercism.app.Crittercism;
import com.crittercism.app.CrittercismConfig;
import com.hubble.bta.SyncInfo;
import com.hubble.devcomm.ISettings;
import com.hubble.devcomm.impl.cvision.NightLightHelper;
import com.hubble.events.AppVersionData;
import com.hubble.events.MessageEvent;
import com.hubble.file.FileService;
import com.hubble.framework.common.BaseContext;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.Config;
import com.hubble.framework.service.configuration.AppSDKConfiguration;
import com.hubble.framework.service.notification.NotificationConstant;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.framework.service.p2p.P2pUtils;
import com.hubble.notifications.NotificationReceiver;
import com.hubble.receivers.AppExitReceiver;
import com.hubble.registration.AnalyticsController;
import com.hubble.tls.TLSPSK;
import com.hubble.util.CommonConstants;
import com.hubble.videobrowser.VideoCollector;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.securepreferences.SecureStorage;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import base.hubble.Api;
import base.hubble.PublicDefineGlob;
import base.hubble.database.Attributes;
import base.hubble.database.AverageData;
import base.hubble.database.DeviceEnvironment;
import base.hubble.database.DeviceEvent;
import base.hubble.database.DeviceEventDate;
import base.hubble.database.DeviceEventGeneralData;
import base.hubble.database.DeviceLocation;
import base.hubble.database.DeviceProfile;
import base.hubble.database.DeviceStatus;
import base.hubble.database.DeviceStatusDetail;
import base.hubble.database.FreeTrial;
import base.hubble.database.GeneralData;
import base.hubble.database.Notification;
import base.hubble.database.TimelineEvent;
import base.hubble.meapi.JsonRequest;
import base.hubble.meapi.PublicDefines;
import de.greenrobot.event.EventBus;
import com.hubbleconnected.camera.R;





public class HubbleApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks, ISettings {

  public static final String TAG = "HubbleApplication";
  public static final String APP_CONFIG = "app_config";
  private static final String LOG_FILE_DIR = "logs";
  private static final String LOG_FILE_NAME = "hubblelog.log";
  private static final String LOG_FILE_PATH = File.separator + LOG_FILE_NAME;
  public static HubbleApplication AppContext;
  public static byte[] ADK;
  public static SecureConfig AppConfig;
  private static File sLogFile = null;
  private static CircularLogFile sCirLogFile = null;
  private SharedPreferences sharedPreferences;
  public static SharedPreferences appSettings;
  private File mInternalLogDir = null;
  private Process mLogcatProcess = null;

  private NotificationReceiver mNotificationReceiver;
  private Handler mHandler;
  private Runnable mAppExitRunnable = new Runnable() {
    @Override
    public void run() {
      Log.d(TAG,"app exit");
      Intent intent=new Intent();
      intent.setAction(AppExitReceiver.APP_EXIT_INTENT);
      sendBroadcast(intent);
    }
  };

  public static void stopPrintAdbLog() {
    if (sCirLogFile != null) {
      Log.d("mbp", "Stop print adb log");
      sCirLogFile.close();
    }
  }

  public static void startPrintAdbLog() {
    if (sCirLogFile != null) {
      Log.d("mbp", "Start print adb log");
      try {
        sCirLogFile.open();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      }
    }
  }

  public static String getLogFilePath() {
    String logFilePath = null;
    if (sLogFile != null) {
      logFilePath = sLogFile.getAbsolutePath();
    }
    return logFilePath;
  }

  public static void writeLogAndroidDeviceInfo() {

    writeAppDebugInfo();

    try {
      Class<?> c = Build.class;
      for (Field f : c.getDeclaredFields()) {
        if (Modifier.isPublic(f.getModifiers())) {
          Class<?> t = f.getType();
          if (t == String.class) {
            Log.i("mbp", f.getName() + ": " + f.get(null));
          }
        } else {
          Log.i("mbp", f.getName() + " field is not accessible");
        }
      }

      Class<?> version = Build.VERSION.class;

      for (Field f : version.getDeclaredFields()) {

        if (Modifier.isPublic(f.getModifiers())) {
          Class<?> t = f.getType();
          if (t == String.class) {
            Log.i("mbp", f.getName() + ": " + f.get(null));
          } else if (t == Integer.class) {
            Log.i("mbp", f.getName() + ": " + f.getInt(null));
          }
        } else {
          Log.i("mbp", f.getName() + " field is not accessible");
        }
      }
    } catch (Exception x) {
      x.printStackTrace();
    }

    DisplayMetrics displaymetrics = new DisplayMetrics();
    Object windowManagerObj = AppContext.getSystemService(Context.WINDOW_SERVICE);

    if (windowManagerObj instanceof WindowManager) {
      WindowManager windowManager = (WindowManager) windowManagerObj;

      windowManager.getDefaultDisplay().getMetrics(displaymetrics);
      int height = displaymetrics.heightPixels;
      int width = displaymetrics.widthPixels;
      Log.i("mbp", "Screen DPI: " + displaymetrics.densityDpi);
      Log.i("mbp", "Screen resolution " + width + " x " + height
          + " pixels.");
      Log.i("mbp", "Screen resolution " + width / displaymetrics.density
          + " x " + height / displaymetrics.density + " dpi.");
    }
  }

  private static void writeAppDebugInfo() {
    PackageInfo pInfo;
    try {

      Log.d("mbp", "=======================DEBUG INFORMATION=======================");
      Runtime rt = Runtime.getRuntime();
      long maxMemory = rt.maxMemory();

      pInfo = AppContext.getPackageManager().getPackageInfo(AppContext.getPackageName(), 0);
      String appInfo = pInfo.packageName + " version code "
          + pInfo.versionCode + " version name " + pInfo.versionName;
      Log.d("mbp", appInfo);
      Log.d("mbp", "Application information: " + appInfo);
      Log.d("mbp", "Device name: " + getDeviceName());
      Log.d("mbp", "CPU Information: \n" + getCpuInfo());
      Log.d("mbp", "Memory Information: \n" + getMemoryInfo());
      Log.d("mbp", "Max heap size for me: " + (maxMemory / 1024) + " Kbytes.");
      Log.d("mbp", "=======================END DEBUG INFORMATION=======================");

    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static String getDeviceName() {
    String manufacturer = Build.MANUFACTURER;
    String model = Build.MODEL;
    if (model.startsWith(manufacturer)) {
      return capitalize(model);
    } else {
      return capitalize(manufacturer) + "_" + model;
    }
  }

  private static String capitalize(String s) {
    if (s == null || s.length() == 0) {
      return "";
    }
    char first = s.charAt(0);
    if (Character.isUpperCase(first)) {
      return s;
    } else {
      return Character.toUpperCase(first) + s.substring(1);
    }
  }

  private static String getCpuInfo() {
    try {
      Process proc = Runtime.getRuntime().exec("cat /proc/cpuinfo");
      InputStream is = proc.getInputStream();

      return getStringFromInputStream(is);
    } catch (IOException e) {
      Log.e("mbp", "------ getCpuInfo " + e.getMessage());
    }

    return null;
  }

  public static String getMemoryInfo() {
    try {
      Process proc = Runtime.getRuntime().exec("cat /proc/meminfo");
      InputStream is = proc.getInputStream();
      return getStringFromInputStream(is);
    } catch (IOException e) {
      Log.e("mbp", "------ getMemoryInfo " + e.getMessage());
    }
    return null;
  }

  private static String getStringFromInputStream(InputStream is) {
    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line = null;

    try {
      while ((line = br.readLine()) != null) {
        sb.append(line);
        sb.append("\n");
      }
    } catch (IOException e) {
      Log.e("mbp", "------ getStringFromInputStream " + e.getMessage());
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          Log.e("mbp",
              "------ getStringFromInputStream " + e.getMessage());
        }
      }
    }
    return sb.toString();
  }

  public static boolean zipLogFile(String dstFilePath) {
    // ArrayList<String> contentList = new ArrayList<String>();
    final int BUFFER = 2048;
    try {
      BufferedInputStream origin = null;
      //FileOutputStream dest = AppContext.openFileOutput(dstFilePath, Context.MODE_PRIVATE);
      FileOutputStream dest = new FileOutputStream(dstFilePath);
      ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

      String srcFileName = LOG_FILE_NAME;
      String srcFilePath = getLogFilePath();
      File logFile = new File(srcFilePath);
      try {
        byte data[] = new byte[BUFFER];
        Log.i("mbp", "FILE PATH: " + srcFilePath);
        FileInputStream fi = new FileInputStream(logFile);
        origin = new BufferedInputStream(fi, BUFFER);
        ZipEntry entry = new ZipEntry(srcFileName);
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, BUFFER)) != -1) {
          out.write(data, 0, count);
        }
        origin.close();
      } catch (Exception ex) {
        ex.printStackTrace();
        ZipEntry entry = new ZipEntry("Phone is rooted");
        out.putNextEntry(entry);
      }

      out.close();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public static File getAppFolder() {
    File shareFolder = new File(Environment.getExternalStorageDirectory(), AppContext.getString(R.string.app_brand));
    if (!shareFolder.exists()) {
      shareFolder.mkdir();
    }
    return shareFolder;
  }

  public static File getVideoFolder() {
    File shareFolder = getAppFolder();
    if (shareFolder.exists()) {
      return createVideoFolder(shareFolder);
    } else {
      shareFolder.mkdir();
      return createVideoFolder(shareFolder);
    }
  }

  private static File createVideoFolder(File hubbleFolder) {
    File shareFolder = new File(hubbleFolder, FileService.getUserFolder());
    if (shareFolder.exists()) {
      return shareFolder;
    } else {
      shareFolder.mkdir();
      return shareFolder;
    }
  }

  public static boolean isVtechApp() {
    return BuildConfig.FLAVOR.equalsIgnoreCase("vtech");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.i(TAG, "App started");
    BaseContext.setBaseContext(getApplicationContext());
    AppContext = this;
    getHash();
      // REL-642: this call do not really disable debug on TLS library
      // due the TLS library bundle is always disable debug
      // purpose is load the native TLS library
      TLSPSK.disableDebug();

    sharedPreferences = getSharedPreferences(APP_CONFIG, Context.MODE_PRIVATE);
    AppConfig = SecureConfig.getInstance(this);
    appSettings = SecureStorage.getInstance(this);
    if(!CommonUtil.checkSettings(getApplicationContext(),SettingsPrefUtils.PREFS_IS_USER_MIGRATED))
      migrateAppSetting();

    initializeDB();

    Api.IsVtechApp = isVtechApp();


    String userAgent = getUserAgentHeader();
    if(CommonUtil.checkIsOrbitPresent(this)) {
      if (CommonUtil.isOrbit(this)) {
        Api.init(this,AppConfig.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL), userAgent,BuildConfig.FLAVOR,BuildConfig.APPLICATION_ID,BuildConfig.VERSION_NAME);
        JsonRequest.init(userAgent,BuildConfig.FLAVOR,BuildConfig.APPLICATION_ID,BuildConfig.VERSION_NAME);
      }else{
        Api.init(this,AppConfig.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL),userAgent);
        JsonRequest.init(userAgent);
      }
    }else{
      Api.init(this,AppConfig.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL), userAgent,BuildConfig.FLAVOR,BuildConfig.APPLICATION_ID,BuildConfig.VERSION_NAME);
      JsonRequest.init(userAgent,BuildConfig.FLAVOR,BuildConfig.APPLICATION_ID,BuildConfig.VERSION_NAME);
    }

   // Api.init(this,AppConfig.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL), userAgent,BuildConfig.FLAVOR,BuildConfig.APPLICATION_ID,BuildConfig.VERSION_NAME);
  //   Api.init(this,AppConfig.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL),userAgent);
    //JsonRequest.init(userAgent,BuildConfig.FLAVOR,BuildConfig.APPLICATION_ID,BuildConfig.VERSION_NAME);
   //  JsonRequest.init(userAgent);


    BaseContext.setBaseContext(getApplicationContext());

    P2pUtils.LocalService = LocalCommandService.getInstance();
    P2pManager.init(this);

    startLogToFile();

    AnalyticsController.init(getApplicationContext());
    registerActivityLifecycleCallbacks(AnalyticsController.getInstance().getLocalyticsLifecycleCallback());
    AnalyticsController.getInstance().openMainSession(getString(R.string.gcm_project_number));

    try {
      ZaiusEventManager.getInstance().initialize(getApplicationContext(),"Okm8PLZludvfhBlojTBz9Q","hubble_mobile", "AIzaSyCzU4H1NLwQM_z9c18YS8WduCMMjUlo_nA");
     } catch (ZaiusException e) {
      e.printStackTrace();
    }


    CrittercismConfig crittercismConfig = new CrittercismConfig();
    crittercismConfig.setLogcatReportingEnabled(true);

    if (BuildConfig.DEBUG) {
      Crittercism.initialize(this, getString(R.string.crittercism_id_debug), crittercismConfig);
    } else {
      Crittercism.initialize(this, getString(R.string.crittercism_id), crittercismConfig);
    }

    DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheOnDisc(true).cacheInMemory(true).build();
    ImageLoaderConfiguration config =
        new ImageLoaderConfiguration.Builder(this)
            .defaultDisplayImageOptions(defaultOptions)
            .build();
    ImageLoader.getInstance().init(config);

    VideoCollector.getRecordedVideos();

    String url = AppConfig.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL);
    if(url.compareToIgnoreCase(PublicDefines.SERVER_URL) != 0)
    {
      PublicDefines.SERVER_URL = url;
    }
    Log.d(TAG,"URL :- " + url);
    Config.setApiServerUrl(url);

    AppSDKConfiguration.getInstance(AppContext).setEmailVerificationRequired(true);
    GeAnalyticsInterface.getInstance().setGAnalyticsConfig(HubbleApplication.AppContext.getString(R.string.ga_trackingId));
    AppSDKConfiguration.getInstance(AppContext).setPackageName(getApplicationContext().getPackageName());

    if(CommonUtil.checkIsOrbitPresent(this)) {
      if (CommonUtil.isOrbit(this)) {
        AppSDKConfiguration.getInstance(AppContext).setConfigurationHeader(BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME, getUserAgentHeader());
      }
      else
      {
        AppSDKConfiguration.getInstance(AppContext).resetConfigurationHeader();
      }
    }
    else
    {
      AppSDKConfiguration.getInstance(AppContext).setConfigurationHeader(BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME, getUserAgentHeader());
    }
    // This is not correct way to determine that server is production server or not.
    // Todo we need to use proper solution for this.
    //
    if(PublicDefines.SERVER_URL.compareToIgnoreCase(PublicDefines.PRODUCTION_URL)==0)
    {
      AppSDKConfiguration.getInstance(AppContext).setProductionServer(true);
    }
    else {
      AppSDKConfiguration.getInstance(AppContext).setProductionServer(false);
    }

    registerNotification();
    registerActivityLifecycleCallbacks(this);
    mHandler = new Handler(getMainLooper());

    String ver = null;
    PackageInfo pinfo;
    try {
      pinfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
      ver = pinfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      ver = "Unknown";
    }

    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.APP_VERSION,AppEvents.APP_VERSION +" : "+ver,AppEvents.APP_VERSION);
    ZaiusEvent AppVersionEvt = new ZaiusEvent(AppEvents.APP_VERSION);
    AppVersionEvt.action(AppEvents.APP_VERSION+" : "+ver);
    try {
      ZaiusEventManager.getInstance().trackCustomEvent(AppVersionEvt);
    } catch (ZaiusException e) {
      e.printStackTrace();
    }
    NightLightHelper.initialize(getApplicationContext());
   }

  public String getUserAgentHeader() {
    String result = getString(R.string.app_brand_application_name);
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      String versionName = pInfo.versionName;
      result = result + "/" + versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return result;
  }


    private void getHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(signature.toByteArray());
                ADK = md.digest();
            }
        } catch (NoSuchAlgorithmException e) {
            //Log.e(TAG, "key hash error", e);
        } catch (Exception e) {
            //Log.e(TAG, "key hash error", e);
        }
    }

  private String getDebugInfo() {

    PackageInfo pInfo = null;
    String appVersionCode = "error", appVersionName = "error";
    try {
      pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      appVersionName = pInfo.versionName;
      appVersionCode = pInfo.versionCode + "";
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("App " + getPackageName() + " version code: " + appVersionCode + " version name: "
        + appVersionName + " crashed at " + new Date().toGMTString() + "\n");
    stringBuilder.append("User name: " + AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, "(not logged in)") + "\n");
    stringBuilder.append("User email: " + AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, "(not logged in)") + "\n");
    stringBuilder.append("Phone manufacturer: " + Build.MANUFACTURER + " model " + Build.MODEL + "\n");
    stringBuilder.append("Android API: " + Build.VERSION.SDK_INT + "\n");

    return stringBuilder.toString();
  }

  protected void initializeDB() {
    Configuration.Builder configurationBuilder = new Configuration.Builder(this);
    configurationBuilder.addModelClasses(DeviceLocation.class);
    configurationBuilder.addModelClasses(DeviceStatus.class);
    configurationBuilder.addModelClasses(DeviceEnvironment.class);
    configurationBuilder.addModelClasses(DeviceStatusDetail.class);

    configurationBuilder.addModelClasses(DeviceProfile.class);
    configurationBuilder.addModelClasses(FreeTrial.class);
    configurationBuilder.addModelClasses(Notification.class);
    configurationBuilder.addModelClasses(Attributes.class);
    configurationBuilder.addModelClasses(DeviceEvent.class);
    configurationBuilder.addModelClasses(DeviceEventGeneralData.class);
    configurationBuilder.addModelClasses(DeviceEventDate.class);
    configurationBuilder.addModelClass(TimelineEvent.class);
    configurationBuilder.addModelClass(GeneralData.class);
    configurationBuilder.addModelClass(AverageData.class);
      configurationBuilder.addModelClass(SyncInfo.class);

    ActiveAndroid.initialize(configurationBuilder.create(), false);
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }

  public void set(String key, Object value) {
    if (value instanceof Long) {
      sharedPreferences.edit().putLong(key, (Long) value).commit();
    } else if (value instanceof Integer) {
      sharedPreferences.edit().putInt(key, (Integer) value).commit();
    } else if (value instanceof String) {
      sharedPreferences.edit().putString(key, (String) value).commit();
    } else if (value instanceof Float) {
      sharedPreferences.edit().putFloat(key, (Float) value).commit();
    } else if (value instanceof Boolean) {
      sharedPreferences.edit().putBoolean(key, (Boolean) value).commit();
    }
  }

  public Integer getInt(String key, int defaltValue) {
    return sharedPreferences.getInt(key, defaltValue);
  }

  public Long getLong(String key, long defaltValue) {
    return sharedPreferences.getLong(key, defaltValue);
  }

  public String getString(String key, String defaltValue) {
    return sharedPreferences.getString(key, defaltValue);
  }

  public boolean getBoolean(String key, boolean defaltValue) {
    return sharedPreferences.getBoolean(key, defaltValue);
  }

  public float getFloat(String key, float defaltValue) {
    return sharedPreferences.getFloat(key, defaltValue);
  }

  public void stopLogcatProcess() {
    if (mLogcatProcess != null) {
      Log.d("mbp", "Stop logcat process...");
      mLogcatProcess.destroy();
    }

    stopPrintAdbLog();
  }

  public void startLogToFile() {
    try {
      Log.i("mbp", "Start logcat overtime.");
      mInternalLogDir = this.getDir(LOG_FILE_DIR, Context.MODE_PRIVATE);
      sLogFile = new File(mInternalLogDir, LOG_FILE_PATH);
      if (sCirLogFile == null || sCirLogFile.canWrite() == false) {
        sCirLogFile = new CircularLogFile(sLogFile);
                /* Set max log file size is 4MB. */
        sCirLogFile.setMaxSize(5 * 1024);
        startPrintAdbLog();
      }

      File logFolder = mInternalLogDir;
      if (logFolder.exists()) {
        String[] fileList = logFolder.list();
        if (fileList != null) {
          for (String fileName : fileList) {
            if (fileName != null && fileName.contains(".log")) {
              Log.d("mbp", "Found log file: " + fileName);
            }
          }
        }
      }

      // mLogcatProcess = Runtime.getRuntime().exec(logcat_cmd);
//            mLogcatProcess = new ProcessBuilder()
//                .command("logcat", "-v", "time", "*:s", "mbp:v",
//                    "RtspStunBridgeService:v",
//                    "FFMpegPlayerActivity:v",
//                    "FFMpegPlaybackActivity:v", "mbp.update:v",
//                    "System.err:v", "ActivityManager:I",
//                    "AndroidRuntime:I", "CameraListFragment:v",
//                    "FFMpegPlayer-JNI:v", "FFMpegMediaPlayer-native:v",
//                    "FFMpeg:v", "ffmpeg_onLoad:v",
//                    "FFMpegMovieViewAndroid:v", "libffmpeg:v",
//                    "FFMpegMediaPlayer-java:v", "FFMpegAudioDecoder:v",
//                    "FFMpegVideoDecoder:v", "FFMpegIDecoder:v",
//                    "System.out:s", "AudioTrack:s", "librudp-client:v",
//                    "librudp:v", "stun-client-java:v",
//                    "stun-client-jni:v", "stun_onLoad:v", "DEBUG:v",
//                    "FFMpegVideoRecorder:v", "FFMpegAudioRecorder:v",
//                    "LocalDiscovery:v").redirectErrorStream(true)
//                .start();
      mLogcatProcess = new ProcessBuilder()
          .command("logcat", "-v", "time").redirectErrorStream(true)
          .start();
      Log.d("mbp", "Start log to file");
      Runnable shutdownHook = new Runnable() {
        public void run() {
          Log.i("mbp", "Shutdown hook run.");
          stopLogcatProcess();
        }
      };

      Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));

      readLogFromLogcatProcess();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void readLogFromLogcatProcess() {
    Runnable runn = new Runnable() {

      @Override
      public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(
            mLogcatProcess.getInputStream()), 4 * 1024);
        String line = null;
        byte[] line_bytes;
        do {
          try {
            line = in.readLine();
            if (line != null) {
              line = line + "\n";
              line_bytes = line.getBytes("UTF-8");
              try {
                if (sCirLogFile.canWrite()) {
                  sCirLogFile.write(line_bytes, 0, line_bytes.length);
                }
              } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          } catch (IOException e) {
            e.printStackTrace();
          }

        }
        while (line != null);
      }
    };
    Thread worker = new Thread(runn);
    worker.start();
  }

  public Uri getAppLogUri() {
    String fileName = "logcat_" + System.currentTimeMillis() + ".txt";
    File outputFile = new File(getExternalCacheDir(), fileName);
    String encLogFileName = "encrypt_" + fileName;
    File encLogFile = new File(getExternalCacheDir(), encLogFileName);
    /*
     * 20151202: HOANG: AA-1276
     * Use same collecting log flow for all apps.
     */
    // getAppLog(outputFile.getAbsolutePath());
    String logFilePath = HubbleApplication.getLogFilePath();
    outputFile = new File(logFilePath);

    HubbleApplication.writeLogAndroidDeviceInfo();

    // Temporary stop print debug for preparing to encrypt log
    HubbleApplication.stopPrintAdbLog();

    // Zip log file to reduce file length
    String zipLogFilePath = logFilePath.replace(".log", ".zip");
    File zipLogFile = new File(zipLogFilePath);
    Log.d("mbp", "Zip log file path: " + zipLogFile.getAbsolutePath());
    HubbleApplication.zipLogFile(zipLogFilePath);
    // Encrypt the log file
    try {
      encrypt(zipLogFilePath, encLogFile.getAbsolutePath());
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InvalidAlgorithmParameterException e) {
        e.printStackTrace();
    }

      Uri contentUri;
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.vtech.fileprovider", encLogFile);
    } else if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.inanny.fileprovider", encLogFile);
    } else if (BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.beurer.fileprovider", encLogFile);
    } else {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, CommonConstants.FILE_PROVIDER_AUTHORITY_HUBBLE, encLogFile);
    }
    return contentUri;
  }

  private void encrypt(String plainFilePath, String cipherFilePath)
      throws IOException, NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    // Here you read the cleartext.
    FileInputStream fis = new FileInputStream(plainFilePath);
    // This stream write the encrypted text. This stream will be wrapped by
    // another stream.
    FileOutputStream fos = new FileOutputStream(cipherFilePath);

    // Length is 16 byte
    SecretKeySpec sks = new SecretKeySpec(com.hubble.tls.Utils.key().getBytes(), "AES");
    AlgorithmParameterSpec iv = new IvParameterSpec(com.hubble.tls.Utils.iv().getBytes());
    // Create cipher
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, sks, iv);
    // Wrap the output stream
    CipherOutputStream cos = new CipherOutputStream(fos, cipher);
    // Write bytes
    int b;
    byte[] d = new byte[4096];
    while ((b = fis.read(d)) != -1) {
      cos.write(d, 0, b);
    }
    // Flush and close streams.
    cos.flush();
    cos.close();
    fis.close();
  }

  public void checkAppVersion() {
    // need to parse this line <div class="content" itemprop="softwareVersion"> 4.2.1(354)  </div>")
    final String startPhase = "<div class=\"content\" itemprop=\"softwareVersion\">";
    final String endPhase = "</div>";

    final String packageName = getPackageName();
    Ion.with(this)
        .load("https://play.google.com/store/apps/details?id=" + packageName + "&hl=en")
        .asString()
        .setCallback(new FutureCallback<String>() {
          @Override
          public void onCompleted(Exception e, String result) {
            if (e != null) {
              Log.i(TAG, "Error when check new app version");
              e.printStackTrace();
            } else {
              if (result != null) {
                int startIndex = result.indexOf(startPhase);
                if (startIndex > 0) {
                  int endIndex = result.indexOf(endPhase, startIndex + startPhase.length());
                  if (endIndex > 0) {
                    String version = result.substring(startIndex + startPhase.length() + 1, endIndex);
                    version = version.trim();
                    Pattern versionPattern = Pattern.compile("(\\d\\.\\d\\.\\d)[(](\\d+)[)]");
                    Matcher versionMatcher = versionPattern.matcher(version);
                    if (versionMatcher.matches()) {
                      Log.i(TAG, "Parse version: " + versionMatcher.group(1) + " build " + versionMatcher.group(2));
                      PackageInfo pInfo = null;
                      try {
                        pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        int currentVersion = pInfo.versionCode;
                        int storeVersion = Integer.valueOf(versionMatcher.group(2));
                        Log.i(TAG, "Store version " + storeVersion + " current version " + currentVersion);
                        if (storeVersion > currentVersion) {
                          Log.i(TAG, "App " + packageName + " has new release on store");
                          List<String> releasesNotes = parseReleaseNote(result);
                          AppVersionData appVersionData = new AppVersionData();
                          appVersionData.setVersionText(versionMatcher.group(1) + "(" + storeVersion + ")");
                          appVersionData.setReleaseNotes(toHTML(releasesNotes));
                          MessageEvent messageEvent = new MessageEvent(MessageEvent.HAS_NEW_APP_VERSION_ON_STORE, appVersionData);
                          EventBus.getDefault().post(messageEvent);
                        } else {
                          Log.i(TAG, "Store version is <= current version.");
                        }
                      } catch (Exception ex) {
                        ex.printStackTrace();
                      }
                    }

                  } else {
                    Log.i(TAG, "Parse data from google play store failed 1");
                  }
                } else {
                  Log.i(TAG, "Parse data from google play store failed 2");
                  Log.i(TAG, result);
                }
              }
            }
          }
        });
  }

  private String toHTML(List<String> releaseNotes) {
    StringBuilder html = new StringBuilder();
    if (releaseNotes.size() > 0) {
      for (String releaseNote : releaseNotes) {
        html.append("<p>&#8226;&#160;&#160" + releaseNote + "<p>");
      }
    }
    return html.toString();
  }

  private List<String> parseReleaseNote(String googlePlayPageResult) {
    String startPhase = "<div class=\"recent-change\">";
    int startIndex = googlePlayPageResult.indexOf(startPhase, 0);
    List<String> releaseNotes = new ArrayList<>();
    while (startIndex > 0) {
      int endIndex = googlePlayPageResult.indexOf("</div>", startIndex);
      if (endIndex > 0) {
        String temp = googlePlayPageResult.substring(startIndex + startPhase.length(), endIndex);
        if (temp != null) {
          Log.i(TAG, "Added release note: " + temp);
          releaseNotes.add(temp.trim());
        }
        startIndex = googlePlayPageResult.indexOf(startPhase, endIndex);
      }
    }
    return releaseNotes;
  }

  public File getCacheFile(String id) {
    return new File(getExternalCacheDir(), id);
  }

  private void registerNotification()
  {
    mNotificationReceiver = new NotificationReceiver();

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(NotificationConstant.NOTIFICATION_RECEIVED);
    intentFilter.addAction(NotificationConstant.REGISTRATION_FAILED);
    intentFilter.addAction(NotificationConstant.REGISTRATION_COMPLETE);
    LocalBroadcastManager.getInstance(this).registerReceiver(mNotificationReceiver, intentFilter);
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    mHandler.removeCallbacks(mAppExitRunnable);
  }

  @Override
  public void onActivityStarted(Activity activity) {
      if(!activity.getComponentName().toString().contains("LaunchScreenActivity"))
    mHandler.removeCallbacks(mAppExitRunnable);
  }

  @Override
  public void onActivityResumed(Activity activity) {
      if(!activity.getComponentName().toString().contains("LaunchScreenActivity"))
    mHandler.removeCallbacks(mAppExitRunnable);
  }

  @Override
  public void onActivityPaused(Activity activity) {
    mHandler.postDelayed(mAppExitRunnable, 1000);
  }


  @Override
  public void onActivityStopped(Activity activity) {

  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

  }

  @Override
  public void onActivityDestroyed(Activity activity) {
      
  }


    void migrateAppSetting() {
        /*Monitor app had 2 types of shared pref, 1. Secure with encrypted key 2. Secured with password
         * We used 2nd appraoch. And we use different shared pref file to store settings.
         * Hence migrate from both files */
        boolean hasUsedSecureSettingStorage = appSettings.getBoolean(SecureStorage.PREFS_USED_SECURE_STORAGE, false);
        if (hasUsedSecureSettingStorage) {
            Log.i(TAG, "App already used secure storage");
            transferOldAppSettings(appSettings);
        } else {
            Log.i(TAG, "App didn't use secure storage before, move old settings to new secure storage");
            transferOldAppSettings(AppConfig);
        }
    }
/* in old app if encrypted key is used */
    void transferOldAppSettings(SharedPreferences appSettings){
        Log.i("ARUNA", "entered transferOldAppSettings");
        String savedUser = appSettings.getString(PublicDefineGlob.PREFS_TEMP_PORTAL_ID, "");
        String userEmail = appSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, "");
        int tempUnit = appSettings.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_F);
        boolean soundNotify = appSettings.getBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_SOUND, true);
        boolean vibrationNotify = appSettings.getBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_VIBRATE, true);
        boolean donotDisturb = appSettings.getBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, false);
        long remainingDoNotDisturb = appSettings.getLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, 0);
        int timeFormat = appSettings.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT,0);
        boolean backGroundMonitor = appSettings.getBoolean(PublicDefineGlob.PREFS_BACKGROUND_MONITORING, false);
        Log.d(TAG, "Store old settings to new preference...");

/*
        Toast.makeText(getApplicationContext(), "Temp : " + tempUnit + "\n" +
                                                 "Time : " + timeFormat + "\n" +
                                                 "Dont disturb time: " + remainingDoNotDisturb + "\n" +
                                                  "Background  : " + backGroundMonitor + "\n" +
                                                  "sound : " + soundNotify + "\n" +
                                                   "DND : " + donotDisturb + "\n" +
                                                   "vibrate : " + vibrationNotify, Toast.LENGTH_LONG ).show();

        Log.i("ARUNA","Temp : " + tempUnit + "\n" +
                        "Time : " + timeFormat + "\n" +
                        "Dont disturb time: " + remainingDoNotDisturb + "\n" +
                        "Background  : " + backGroundMonitor + "\n" +
                        "sound : " + soundNotify + "\n" +
                        "DND : " + donotDisturb + "\n" +
                        "vibrate : " + vibrationNotify);*/

        AppConfig.putString(PublicDefineGlob.PREFS_TEMP_PORTAL_ID,savedUser );
        AppConfig.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, userEmail);
        AppConfig.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, tempUnit);

        CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_NOTIFY_BY_SOUND, soundNotify);
        CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_NOTIFY_BY_VIBRATE, vibrationNotify);
        CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_IS_DO_NOT_DISTURB_ENABLE, donotDisturb);
        CommonUtil.setLongValue(getApplicationContext(), SettingsPrefUtils.PREFS_DO_NOT_DISTURB_REMAINING_TIME, remainingDoNotDisturb);

        if(timeFormat == 1)
            CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.TIME_FORMAT_12, false);
        else
            CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.TIME_FORMAT_12, true);

        CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_BACKGROUND_MONITORING, backGroundMonitor);

        //AppConfig.putBoolean(SecureStorage.PREFS_USED_SECURE_STORAGE, false);
        CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_IS_USER_MIGRATED, true);

        SharedPreferences.Editor editor = appSettings.edit();
        editor.clear();
    }
    /* in old app if secure with password is used */
    void transferOldAppSettings(SecureConfig appConfig){
        String savedUser = appConfig.getString(PublicDefineGlob.PREFS_TEMP_PORTAL_ID, "");
        String userEmail = appConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, "");
        int tempUnit = appConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_F);

        boolean soundNotify = appConfig.getBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_SOUND, true);
        boolean vibrationNotify = appConfig.getBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_VIBRATE, true);
        boolean donotDisturb = appConfig.getBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, false);
        long remainingDoNotDisturb = appConfig.getLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, 0);
        int timeFormat = appConfig.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT,0);
        boolean backGroundMonitor = appConfig.getBoolean(PublicDefineGlob.PREFS_BACKGROUND_MONITORING, false);

        AppConfig.putString(PublicDefineGlob.PREFS_TEMP_PORTAL_ID,savedUser );
        AppConfig.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, userEmail);
        AppConfig.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, tempUnit);

        CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_NOTIFY_BY_SOUND, soundNotify);
        CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_NOTIFY_BY_VIBRATE, vibrationNotify);
        CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_IS_DO_NOT_DISTURB_ENABLE, donotDisturb);
        CommonUtil.setLongValue(getApplicationContext(), SettingsPrefUtils.PREFS_DO_NOT_DISTURB_REMAINING_TIME, remainingDoNotDisturb);
        if(timeFormat == 1)
            CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.TIME_FORMAT_12, false);
        else
            CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.TIME_FORMAT_12, true);
        CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_BACKGROUND_MONITORING, backGroundMonitor);

        CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_IS_USER_MIGRATED, true);
        

        /* Just update the values and do not clear as its same file*/
    }

  @Override
  public JSONObject getCachedP2PIdentifier(String regId) {
    JSONObject result = null;
    String data = appSettings.getString("p2p_identifier_" + regId, null);
    if (data != null) {
      try {
        result = new JSONObject(data);
        String p2pId = result.optString("p2pKey");
        String p2pkey = result.optString("p2pId");
        if (TextUtils.isEmpty(p2pId) || TextUtils.isEmpty(p2pkey)) {
          result = null;
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  @Override
  public void cacheP2PIdentifier(String regId, String p2pKey, String p2pId) {
    try {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("p2pId", p2pId);
      jsonObject.put("p2pKey", p2pKey);
      String json = jsonObject.toString();
      appSettings.edit().putString("p2p_identifier_" + regId, json).apply();

    } catch (JSONException ex) {
      ex.printStackTrace();
    }
  }
}
