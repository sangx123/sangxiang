package com.hubble.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.ui.RetreiveCameraLogActivity;
import com.hubble.util.CommonConstants;
import com.hubble.util.P2pSettingUtils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import base.hubble.PublicDefineGlob;
import base.hubble.meapi.PublicDefines;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;
/**
 * Created by Sean on 15-03-26.
 */
public class DebugFragment extends Fragment {
  private SecureConfig settings = HubbleApplication.AppConfig;

  public static final String PREFS_DEBUG_ENABLED = "debug_enabled";
  public static final String PREFS_USE_DEV_OTA = "debug_use_dev_ota";
  public static final String PREFS_ENABLED_TOS = "debug_enabled_tos";
  public static final String PREFS_ENABLED_P2P_PLAY_BY_TIMESTAMP = "debug_enabled_p2p_play_by_timestamp";
  public static final String PREFS_ENABLED_CORRUPTED_FRAME_FILTERING = "debug_enabled_p2p_corrupted_frame_filtering";
  public static final String PREFS_ENABLED_RTMP_STREAMING = "debug_enabled_rtmp_streaming";
  public static final String PREFS_ENABLE_P2P_ORBIT = "debug_p2p_orbit";

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_debug, null);
    TextView mUsername = (TextView) view.findViewById(R.id.fragmentDebug_username);
    TextView mEmail = (TextView) view.findViewById(R.id.fragmentDebug_email);
    TextView mEndpoint = (TextView) view.findViewById(R.id.fragmentDebug_endpoint);
    TextView mAppVersion = (TextView) view.findViewById(R.id.fragmentDebug_appVersion);
    TextView mDeviceMake = (TextView) view.findViewById(R.id.fragmentDebug_deviceMake);
    TextView mDeviceModel = (TextView) view.findViewById(R.id.fragmentDebug_deviceModel);
    TextView mDeviceOS = (TextView) view.findViewById(R.id.fragmentDebug_deviceOS);
    TextView mDeviceProvider = (TextView) view.findViewById(R.id.fragmentDebug_deviceProvider);
    CheckBox mCameraDebugMenu = (CheckBox) view.findViewById(R.id.debug_toggle_options);
    CheckBox mEnableLanConnection = (CheckBox) view.findViewById(R.id.debug_enable_lan_connection);
    CheckBox mForceLocalVideo = (CheckBox) view.findViewById(R.id.debug_force_local);
    CheckBox useDevOTA = (CheckBox) view.findViewById(R.id.debug_use_dev_ota);
    CheckBox enableToS = (CheckBox) view.findViewById(R.id.debug_enable_tos);
    CheckBox enablePlayByTimestamp = (CheckBox) view.findViewById(R.id.debug_p2p_timestamp_delay);
    CheckBox enableFrameFiltering = (CheckBox) view.findViewById(R.id.debug_p2p_corrupted_frame_filtering);
    CheckBox enableRtmpStreaming = (CheckBox) view.findViewById(R.id.debug_rtmp_streaming);
    final Button sendLogButton = (Button) view.findViewById(R.id.debug_send_log);
    final Button sendCamLogButton = (Button) view.findViewById(R.id.debug_send_camera_log_setup_mode);
    ScrollView srlDebug = (ScrollView) view.findViewById(R.id.srl_view_debug);

    mUsername.setText(getActivity().getString(R.string.username) + " " + settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, ""));
    mEmail.setText(getActivity().getString(R.string.email) + " " + settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
    mEndpoint.setText(getActivity().getString(R.string.endpoint) + " " + settings.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL));
    mAppVersion.setText(getActivity().getString(R.string.app_version) + " " + BuildConfig.BUILD_TYPE + " " + BuildConfig.VERSION_NAME + ": " + BuildConfig.VERSION_CODE + " ");
    mDeviceMake.setText(getActivity().getString(R.string.device_make) + " " + Build.MANUFACTURER);
    mDeviceModel.setText(getActivity().getString(R.string.device_model) + " " + Build.MODEL);
    mDeviceOS.setText(getActivity().getString(R.string.device_os) + " " + Build.VERSION.SDK_INT);

    mAppVersion.setVisibility(View.GONE);
    /*
     * 20151202: HOANG: AA-1207
     * Hide all unused debug options for non-Vtech app.
     */
    if (!BuildConfig.FLAVOR.equals("vtech")) {
      mUsername.setVisibility(View.GONE);
      mEmail.setVisibility(View.GONE);
      mEndpoint.setVisibility(View.GONE);
      mAppVersion.setVisibility(View.GONE);
      mDeviceMake.setVisibility(View.GONE);
      mDeviceModel.setVisibility(View.GONE);
      mDeviceOS.setVisibility(View.GONE);
      mDeviceProvider.setVisibility(View.GONE);
      mEnableLanConnection.setVisibility(View.GONE);
      mForceLocalVideo.setVisibility(View.GONE);
      useDevOTA.setVisibility(View.VISIBLE);
      enablePlayByTimestamp.setVisibility(View.GONE);
      enableFrameFiltering.setVisibility(View.GONE);
      enableRtmpStreaming.setVisibility(View.GONE);
      // remove "Use dev OTA" setting if any
      //settings.remove("debug_use_dev_ota");
    }

    TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
    mDeviceProvider.setText(getActivity().getString(R.string.device_provider) + " " + telephonyManager.getNetworkOperatorName());

    mCameraDebugMenu.setChecked(settings.getBoolean(PREFS_DEBUG_ENABLED, false));
    mCameraDebugMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        settings.putBoolean(PREFS_DEBUG_ENABLED, isChecked);
      }
    });

    mEnableLanConnection.setChecked(settings.getBoolean("enable_931_lan", false));
    mEnableLanConnection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        settings.putBoolean("enable_931_lan", isChecked);
      }
    });

    //Pragnya to check usage
    /*mForceLocalVideo.setChecked(VideoViewFragment.getLocalOnlySetting(getActivity()));
    mForceLocalVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        VideoViewFragment.toggleLocalOnlySetting(getActivity());
      }
    });*/

    useDevOTA.setChecked(settings.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false));
    useDevOTA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        settings.putBoolean(DebugFragment.PREFS_USE_DEV_OTA, isChecked);
      }
    });

    enableToS.setChecked(settings.getBoolean(DebugFragment.PREFS_ENABLED_TOS, false));
    enableToS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        settings.putBoolean(DebugFragment.PREFS_ENABLED_TOS, isChecked);
        new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.relauch_app_for_term_service))
            .setPositiveButton(android.R.string.ok, null).show();
      }
    });

    enablePlayByTimestamp.setChecked(P2pSettingUtils.getInstance().isP2pPlayByTimestampEnabled());
    enablePlayByTimestamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        settings.putBoolean(DebugFragment.PREFS_ENABLED_P2P_PLAY_BY_TIMESTAMP, isChecked);
      }
    });

    enableFrameFiltering.setChecked(P2pSettingUtils.getInstance().isP2pFrameFilteringEnabled());
    enableFrameFiltering.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        settings.putBoolean(DebugFragment.PREFS_ENABLED_CORRUPTED_FRAME_FILTERING, isChecked);
      }
    });

    enableRtmpStreaming.setChecked(P2pManager.getInstance().isRtmpStreamingEnabled());
    enableRtmpStreaming.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        settings.putBoolean(DebugFragment.PREFS_ENABLED_RTMP_STREAMING, isChecked);
      }
    });

    sendLogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendDeviceLog();
      }
    });

    sendCamLogButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getActivity(), RetreiveCameraLogActivity.class);
        intent.putExtra("token", settings.getString(PublicDefine.PREFS_SAVED_PORTAL_TOKEN, null));
        startActivity(intent);
      }
    });

    if (BuildConfig.FLAVOR.contains("hubblenew")) {
      srlDebug.setVisibility(View.GONE);
      sendCamLogButton.setVisibility(View.GONE);
    }


    return view;
  }


  private void sendDeviceLog() {
    Crittercism.logHandledException(new Throwable(String.valueOf(System.currentTimeMillis())));

    String fileName = "logcat_" + System.currentTimeMillis() + ".txt";
    File outputFile = new File(getActivity().getExternalCacheDir(), fileName);
    String encLogFileName = "encrypt_" + fileName;
    File encLogFile = new File(getActivity().getExternalCacheDir(), encLogFileName);

    /*
     * 20151202: HOANG: AA-1276
     * Use same collecting log flow for all apps.
     */
    // getAppLog(outputFile.getAbsolutePath());
    String logFilePath = HubbleApplication.getLogFilePath();
    outputFile = new File(logFilePath);
    Log.d("mbp", "Send Hubble log file length: " + outputFile.length());

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
      //encrypt(outputFile.getAbsolutePath(), encLogFile.getAbsolutePath());
      encrypt(zipLogFilePath, encLogFile.getAbsolutePath());
      Log.d("mbp", "Send enc Hubble log file length: " + encLogFile.length());
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    HubbleApplication.startPrintAdbLog();

    String titleEmail = "";
    String bodyEmail = getString(R.string.body_email);

    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"android.techsupport@hubblehome.com"});
    // Send encrypted file
    Uri contentUri;
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.vtech.fileprovider", encLogFile);
      titleEmail = String.format(getString(R.string.title_email), "Vtech", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
    } else if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.inanny.fileprovider", encLogFile);
      titleEmail = String.format(getString(R.string.title_email), "iNanny", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
    } else if (BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.beurer.fileprovider", encLogFile);
      titleEmail = String.format(getString(R.string.title_email), "Beurer", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
    } else {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, CommonConstants.FILE_PROVIDER_AUTHORITY_HUBBLE, encLogFile);
      titleEmail = String.format(getString(R.string.title_email), "Hubble", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
    }

    sendIntent.putExtra(Intent.EXTRA_SUBJECT, titleEmail);
    sendIntent.putExtra(Intent.EXTRA_TEXT, bodyEmail);
    sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
    sendIntent.setType("text/plain");
    startActivity(sendIntent);
  }

  private void getAppLog(String logFilePath) {
    final StringBuilder log = new StringBuilder();
    String logcatCmd = "logcat -d -v time";
    try {
      final String lineSeparator = System.getProperty("line.separator");
      Process process = Runtime.getRuntime().exec(logcatCmd);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = bufferedReader.readLine()) != null) {
        log.append(line);
        log.append(lineSeparator);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

        /* create a data file in the external dir */
    File file = new File(logFilePath);
    try {
      OutputStream os = new FileOutputStream(file);
      PrintWriter pw = new PrintWriter(os);
      pw.write(log.toString());
      pw.close();
    } catch (IOException e) {
      if (file.exists()) {
        file.delete();
      }
      e.printStackTrace();
    }
  }

  private void encrypt(String plainFilePath, String cipherFilePath)
      throws IOException, NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException {
    // Here you read the cleartext.
    FileInputStream fis = new FileInputStream(plainFilePath);
    // This stream write the encrypted text. This stream will be wrapped by
    // another stream.
    FileOutputStream fos = new FileOutputStream(cipherFilePath);

    // Length is 16 byte
    SecretKeySpec sks = new SecretKeySpec("Super-LovelyDuck".getBytes(), "AES");
    // Create cipher
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.ENCRYPT_MODE, sks);
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
}
