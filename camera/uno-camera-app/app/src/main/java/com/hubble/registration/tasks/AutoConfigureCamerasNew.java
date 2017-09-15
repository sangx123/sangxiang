package com.hubble.registration.tasks;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

import com.blinkhd.playback.NetworkUtils;
import com.discovery.LocalScanForCameras;
import com.discovery.ScanProfile;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.UserAccount;
import com.hubble.registration.Util;
import com.hubble.registration.interfaces.ICameraScanner;
import com.hubble.registration.models.CamConfiguration;
import com.hubble.registration.models.LegacyCamProfile;
import com.hubble.registration.models.VtechCamConfiguration;
import com.hubbleconnected.camera.R;

import org.jetbrains.annotations.NotNull;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.TimeZone;

import base.hubble.PublicDefineGlob;
import base.hubble.meapi.Device;
import base.hubble.meapi.device.CheckStatusResponse;
import base.hubble.meapi.device.CheckStatusResponseData;

public class AutoConfigureCamerasNew extends AsyncTask<CamConfiguration, String, String> implements ICameraScanner {
  private static final String TAG = "AutoConfigureCamerasNew";

  private static final int DEFAULT_SEARCHING_FOR_NEW_NETWORK = 20000; // sec
  private static final int DEFAULT_WAITING_FOR_CONNECTION = 30000;   // sec

  private int errorCode;
  private String errorMsg;

  // use when does not find ssid in wifi list
  private boolean continueAddCamera;
  private boolean isWaiting;

  private Context mContext;
  private Handler mHandler;
  private Object wifiLockObject;

  private Object supplicantLockObject;
  private SupplicantState sstate;

  private NetworkInfo mWifiNetworkInfo;
  private int mWifiState;

  // private ProgressDialog dialog;

  private String home_ssid, home_key;
  private String security_type;
  private String wep_auth_mode, wep_key_index;
  private String http_usr, http_pass;

  private WifiConfiguration conf;
  private LocalScanForCameras scan_task;
  private long start_time_from_get_rt_list = 0;
  private long add_camera_start_time = -1;
  private long start_time = 0;

  private boolean isCancelable;
  private boolean isCamera;

  private LegacyCamProfile cam_profile = null;
  private WifiManager mWifiManager;

  public AutoConfigureCamerasNew(Context mContext, Handler h, boolean isCamera) {
    this.mContext = mContext;
    this.isCamera = isCamera;
    this.mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    wifiLockObject = new Object();
    supplicantLockObject = new Object();
    sstate = null;
    mWifiNetworkInfo = null;
    mWifiState = -1;
    wep_auth_mode = wep_key_index = null;
    conf = null;
    mHandler = h;
    scan_task = null;

  }

  public void setStartTimeFromGetRtList(long start_time) {
    this.start_time_from_get_rt_list = start_time;
  }

  public void setAddCameraStartTime(long add_camera_start_time) {
    this.add_camera_start_time = add_camera_start_time;
  }

  /*
   *
   * (non-Javadoc)
   *
   * @Param[]: - SSID of cameras in Setup-mode - Max_num_of_Cam (int string) -
   * SSID of Network to be configured - Key of network to be configured -
   * Security type (wpa or wep) - WEP method (null if not valid) - WEP key
   * (null if not valid)
   *
   * Description: - Disconnect fr Original Network Search for "MotAndroid"
   * ssid Try to connect send over the configuration using config_write send
   * over the restart cmd - expect irabot reboot - Repeat until we have found
   * Max_num_of_Cam (4) Or when we hit a time out - - Reconnect back to
   * Original Network
   */

  protected String doInBackground(CamConfiguration... arg0) {

    isCancelable = false;
    home_ssid = arg0[0].ssid();
    home_key = arg0[0].pass_string();
    security_type = arg0[0].security_type();
    wep_auth_mode = arg0[0].auth_method();
    wep_key_index = arg0[0].key_index();
    conf = arg0[0].wc();
    cam_profile = arg0[0].getCamProfiles()[0];
    String saved_token = arg0[0].getUser_api_key();
    if (saved_token ==  null) {

      Log.e(TAG, "NULL api key>>>  SETUP WILL FAIL OR CRASH");
    }
    String camera_name = null;
    if (cam_profile != null) {
      camera_name = cam_profile.getName();
    }

    http_usr = arg0[0].getHttpUsr();
    http_pass = arg0[0].getHttpPass();

    String output = "Done";
    String regId = arg0[0].getDeviceBSSIDList().elementAt(0);

    /*
     * We have finished adding all wificonfiguration of devices Now for each
     * of the network ID, try to connect and send the configuration data
     * over
     */
    Log.d(TAG, "Camera regId: " + regId);

    long current_time = System.currentTimeMillis();
    if (current_time < start_time_from_get_rt_list + 6000) {
      try {
        // // Log.d(TAG, "Auto configure camera, sleep for: " + (start_time_from_get_rt_list + 6000 - current_time));
        Thread.sleep(start_time_from_get_rt_list + 6000 - current_time);
      } catch (InterruptedException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      }
    }

    //String gatewayIp = Util.getGatewayIp(mContext.getApplicationContext());

    String gatewayIp = cam_profile.getHostInetAddress().getHostAddress();

    String device_address_port = gatewayIp + ":" + PublicDefineGlob.DEVICE_PORT;
    Log.d(TAG, "123 Turn all default alert settings ON, address port: " + device_address_port);
    setDefaultAntiFlicker(device_address_port);
    setDefaultAlertSettings2(arg0[0], device_address_port, PublicDefine.getModelIdFromRegId(regId),
        cam_profile.getFirmwareVersion());

    // retry up to 5 times
    String response;
    String req = "http://" + gatewayIp + ":" + PublicDefineGlob.DEVICE_PORT +
        arg0[0].build_setup_request();
    //String saved_token = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
    int offset = TimeZone.getDefault().getRawOffset();
    if (TimeZone.getDefault().useDaylightTime()) {
      offset = Util.getTimeZoneOffsetWithDST();
    }

    String timeZone = String.format("%s%02d.%02d", offset >= 0 ? "+" : "-", Math.abs(offset) / 3600000, (Math.abs(offset) / 60000) % 60);
    int k = 0;
    while (k++ < 10 && !isCancelled()) {
      String set_auth_cmd = String.format("%1$s%2$s%3$s%4$s%5$s%6$s%7$s%8$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.SET_SERVER_AUTHENTICATION, PublicDefineGlob.SET_SERVER_AUTHENTICATION_PARAM_1, saved_token, PublicDefineGlob.SET_SERVER_AUTHENTICATION_PARAM_2, timeZone);
      Log.d(TAG, "Setting server auth");
      response = send_request(set_auth_cmd, 20000);
      Log.d(TAG, "Set server auth res: " + response);

      if (!verify_response(PublicDefineGlob.SET_SERVER_AUTHENTICATION_RESPONSE, response)) {
        // loop back;
        // // Log.d(TAG, "Failed to send server auth: " + response + " retry: " + k);
        output = "Failed";
        errorCode = PublicDefine.SEND_DATA_TO_CAMERA_FAILED;
        errorMsg = mContext.getString(R.string.send_data_to_camera_failed);
      } else {
        output = "Done";
        break;
      }
    }

    if (output.equalsIgnoreCase("Done")) {
      k = 0;
      while (k++ < 10 && !isCancelled()) {
        Log.d(TAG, "Sending WiFi config to camera");
        response = send_request(req, 20000);
        Log.d(TAG, "Send WiFi config to camera DONE, res: " + response);
        /*
         * dont check response from setup wireless save
         */
        if (!verify_response(PublicDefineGlob.WIRELESS_SETUP_SAVE_RESPONSE, response)) {
          // // Log.d(TAG, "Failed to send WIRELESS info: " + response + " retry: " + k);
          output = "Failed";
          errorCode = PublicDefine.SEND_DATA_TO_CAMERA_FAILED;
          errorMsg = mContext.getString(R.string.send_data_to_camera_failed);
        } else {
          output = "Done";
          break;
        }
      }
    }

    if (output.equalsIgnoreCase("Done")) {
      String get_wifi_state_cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.GET_WIFI_CONNECTION_STATE);
      long waiting_time = System.currentTimeMillis() + 2 * 60 * 1000;
      while (System.currentTimeMillis() < waiting_time && !isCancelled()) {
        response = null;
        if (isCamera) {
          response = send_request(get_wifi_state_cmd, 5000);
        }
        //  Log.d(TAG, "get_wifi_state res: " + response);
        if (cam_profile.getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_MBP931) && cam_profile.isAddViaLAN()) {
          if (response!=null && response.startsWith(PublicDefineGlob.GET_WIFI_CONNECTION_STATE)) {
            String wifi_state = response.substring(PublicDefineGlob.GET_WIFI_CONNECTION_STATE.length() + 2);
            if (wifi_state.equalsIgnoreCase(PublicDefineGlob.WIFI_CONNECTION_STATE_CONNECTED)) {
              // // Log.d(TAG, "Camera has internet connection --> send restart to notify");
              break;
            } else if (wifi_state.equalsIgnoreCase(PublicDefineGlob.WIFI_CONNECTION_STATE_SCANNING)) {
              Log.i(TAG, "Camera is verifying Wi-Fi network...");
            }
          } else {
            break;
          }
          try {
            Thread.sleep(3000);
          } catch (InterruptedException e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
          }
        } else if (response != null) {
          if (response.startsWith(PublicDefineGlob.GET_WIFI_CONNECTION_STATE)) {
            String wifi_state = response.substring(PublicDefineGlob.GET_WIFI_CONNECTION_STATE.length() + 2);
            if (wifi_state.equalsIgnoreCase(PublicDefineGlob.WIFI_CONNECTION_STATE_CONNECTED)) {
              // // Log.d(TAG, "Camera has internet connection --> send restart to notify");
              break;
            } else {
              break;
            }
          } else {
            break;
          }
        } else {
          break;
        }
      }

      if (output.equalsIgnoreCase("Failed")) {
        errorCode = PublicDefine.INCORRECT_WIFI_PASSWORD;
        errorMsg = mContext.getString(R.string.verify_wifi_password_failed);
      } else {
        k = 0;
        do {
          String restart_cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.RESTART_DEVICE);
          response = send_request(restart_cmd, 5000);
          Log.d(TAG, "restart--->>: " + response);
          // "restart_system: 0" / -1

          if (!verify_response(PublicDefineGlob.RESTART_DEVICE_RESPONSE, response)) {
            // response = send_request(restart_cmd, 5000);
          } else {
            break;
          }
        } while (k++ < 5 && !isCancelled());
      }
    }

    long time_to_show_abort_page = System.currentTimeMillis() + 60 * 1000;

    // remove all CameraHD-XXXXXXXXXX SSIDs
    List<WifiConfiguration> wcs = mWifiManager.getConfiguredNetworks();
    if (wcs != null) {
      for (WifiConfiguration wc : wcs) {
        if ((wc == null) || (wc.SSID == null))
          continue;
        if (NetworkUtils.isCameraSsid(wc.SSID) ||
                NetworkUtils.isCameraSsid(PublicDefine.convertToNoQuotedString(wc.SSID))) {
          mWifiManager.removeNetwork(wc.networkId);
          Log.d(TAG, "Remove SSID: " + wc.SSID);
        }
      }
      boolean saveConfig = mWifiManager.saveConfiguration();
      Log.d(TAG, "Save wifi configuration? " + saveConfig);
    }

    // reconnect back to the original network
    //String homeSSID = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_HOME_WIFI_SSID_NO_QUOTE, home_ssid);
    String homeSSID = home_ssid;
    WifiConfiguration homeWc = null;
    List<WifiConfiguration> wcs2 = mWifiManager.getConfiguredNetworks();
    if (wcs2 != null) {
      for (WifiConfiguration wc : wcs2) {
        if ((wc == null) || (wc.SSID == null))
          continue;
        if (wc.SSID.equalsIgnoreCase(PublicDefine.convertToQuotedString(homeSSID)) ||
            wc.SSID.equalsIgnoreCase(homeSSID)) {
          // Log.d(TAG, "enable network: " + wc.SSID);
          // Log.d(TAG, "Found wc:" + wc.networkId + " : " + wc.SSID
          //        + " algo: " + wc.allowedAuthAlgorithms + " key: "
          //        + wc.allowedKeyManagement + " grp:"
          //        + wc.allowedGroupCiphers + " pair: "
          //        + wc.allowedPairwiseCiphers + " proto:"
          //        + wc.allowedProtocols + " hidden: " + wc.hiddenSSID
          //        + " preSharedKey: " + wc.preSharedKey);
          homeWc = wc;
          mWifiManager.enableNetwork(wc.networkId, true);
          break;
        }
      }
    }

    WifiReceiver1 br = new WifiReceiver1();
    IntentFilter i = new IntentFilter();
    i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    i.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    i.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
    mContext.registerReceiver(br, i);

    // how to know when Iam connected ?
    synchronized (wifiLockObject) {
      try {
        wifiLockObject.wait(DEFAULT_WAITING_FOR_CONNECTION);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    if (!isCancelled()) {
      mContext.unregisterReceiver(br);
    }

    if (output.equalsIgnoreCase("Done")) {
      // check if phone has reconnected to home wifi or not
      int max_try = 2 * 60;
      int retries = 0;
      boolean isObtainMsgConnectHomeWifiFailedAtFirstTime = false;
      do {
        if ((mWifiManager.getConnectionInfo() != null && mWifiManager.getConnectionInfo().getIpAddress() != 0) && (mWifiManager.getDhcpInfo() != null && mWifiManager.getDhcpInfo().ipAddress != 0)) {
          // We're connected but don't have any IP yet
          Log.d(TAG, "IP: " + (mWifiManager.getDhcpInfo().ipAddress & 0xFF) + "." + ((mWifiManager.getDhcpInfo().ipAddress >> 8) & 0xFF) + "." +
                  ((mWifiManager.getDhcpInfo().ipAddress >> 16) & 0xFF) + "." + ((mWifiManager.getDhcpInfo().ipAddress >> 24) & 0xFF));
          Log.d(TAG, "SV: " + (mWifiManager.getDhcpInfo().serverAddress & 0xFF) + "." + ((mWifiManager.getDhcpInfo().serverAddress >> 8) & 0xFF) + "." +
                  ((mWifiManager.getDhcpInfo().serverAddress >> 16) & 0xFF) + "." + ((mWifiManager.getDhcpInfo().serverAddress >> 24) & 0xFF));
          output = "Done";

          // 20151031: hoang: send message to notify that network ssid has changed
          if (mHandler != null) {
            String currentSsid = mWifiManager.getConnectionInfo().getSSID();
            Log.i(TAG, "Sending message MSG_AUTO_CONF_SSID_CHANGED, current ssid: " + currentSsid);
            mHandler.sendMessage(mHandler.obtainMessage(PublicDefine.MSG_AUTO_CONF_SSID_CHANGED, currentSsid));
            //AA-1260: If mHandler sent failed msg before, just reset the setup desc msg
            if (isObtainMsgConnectHomeWifiFailedAtFirstTime) {
              mHandler.sendMessage(mHandler.obtainMessage(PublicDefine.CONNECTING_TO_HOME_WIFI_SUCCESSFUL));
            }
          }
          break;
        } else {
          output = "Failed";
          //AA-1260: [SETUP] Android app Needs to check if phone is able to connect to Home wifi
          if (mHandler != null) {
            //send failed msg
            mHandler.sendMessage(mHandler.obtainMessage(PublicDefine.CONNECTING_TO_HOME_WIFI_FAILED));
            isObtainMsgConnectHomeWifiFailedAtFirstTime = true;
          }
        }
        Log.d(TAG, "connected but don't have any IP yet...retries? " + retries);

        if (homeWc != null) {
          // re-enable WiFi every 10s
          if (retries % 10 == 0 && retries / 10 > 0) {
            Log.d(TAG, "Connected to unexpected network -> try to enable expected network");
            boolean enabled = mWifiManager.enableNetwork(homeWc.networkId, true);
            Log.d(TAG, "enableNetwork: " + homeWc.networkId + ", " + homeWc.SSID + "? " + enabled);
          }
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
      } while (retries++ < max_try && !isCancelled());

      if (output.equalsIgnoreCase("Failed")) {
        errorCode = PublicDefine.CONNECT_TO_HOME_WIFI_FAILED;
        errorMsg = mContext.getString(R.string.connect_to_homw_wifi_failed);
      }
    }

    if (output.equalsIgnoreCase("Done")) {
      boolean shouldExit = false;
      do {
        try {
          CheckStatusResponse res = Device.checkStatus(saved_token, regId);
          if (res != null && res.getStatus() == HttpURLConnection.HTTP_OK && res.getData() != null) {
            CheckStatusResponseData checkStatusData = res.getData();
            int status = checkStatusData.getDevice_status();
            Log.d(TAG, "check device status res: " + status);
            switch (status) {
              case 0:
              case 2:
              case 3:
              case 4:
              case 5:
                // Success
                output = "Done";
                shouldExit = true;
                break;

              case 1:
                output = "Failed";
                errorMsg = mContext.getString(R.string.device_is_not_present_in_device_master);
                shouldExit = true;
                break;

              default:
                output = "Failed";
                break;
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {

          //Log.e(TAG, Log.getStackTraceString(e));
        }
      } while (k++ < 15 && !shouldExit && !isCancelled());

    }

    // scan for this camera here ..
    if (output.equalsIgnoreCase("Done")) {
      // InetAddress dummy;
      regId = arg0[0].getDeviceBSSIDList().elementAt(0);
      String mac = PublicDefine.getMacFromRegId(regId).toUpperCase();
      mac = PublicDefineGlob.add_colon_to_mac(mac);
      boolean foundConfiguredCam = false;
      long time_to_finish_scanning = System.currentTimeMillis() + arg0[0].getTimeToFinishScanning();
      // VTECH camera may take long time to setup
      if( arg0[0] instanceof VtechCamConfiguration  ) {
        Log.d(TAG, "VTECH camera setup, time to finish get device list from server will be 7 mins");
      }
      boolean isAbortPageShowed = false;
      do {
        if (System.currentTimeMillis() > time_to_show_abort_page && !isAbortPageShowed) {
          isAbortPageShowed = true;
          if (mHandler != null) {
            mHandler.sendMessage(Message.obtain(mHandler, PublicDefine.MSG_AUTO_CONF_SHOW_ABORT_PAGE));
          }
        }

        // // // Log.d(TAG,"start scanning for: " + mac);
        //
        // //start scanning now.. for one camera only ;
        // scan_task = new LocalScanForCameras(mContext, this);
        // scan_task.setShouldGetSnapshot(false);
        //
        //
        // //create a dummy profile
        // CamProfile dummy = new CamProfile(null, mac);
        // /*Assume that it is in local Becoz We have just finished
        // configuring it
        // Assume that it is 1 mins since last comm
        // ---NEED to set this to trick ScanForCameras to scan otherwise
        // it will skip this camera
        // */
        // dummy.setInLocal(true);
        // dummy.setMinutesSinceLastComm(1);
        // scan_task.startScan(new CamProfile[] {dummy});
        //
        // //wait for result
        // cps = null;
        // cps = scan_task.getScanResults();
        //
        // if (cps != null && cps.length == 1)
        // {
        // if (mac.equalsIgnoreCase(cps[0].get_MAC()))
        // {
        // //foundConfiguredCam = true;
        // // // Log.d(TAG,"Found it in local");
        // //break;
        // }
        //
        // }

        if (isCancelled()) {
          break;
        }

        // don't need to wait anymore
        // if ( System.currentTimeMillis() > time_to_query_online )
        {
          //Log.d(TAG, "Start query online");
          // reset to the nxt minute
          UserAccount online_user;
          try {
            online_user = new UserAccount(saved_token, mContext.getExternalFilesDir(null), null, mContext);
          } catch (Exception e1) {
            e1.printStackTrace();
            output = "Failed";
            errorCode = PublicDefine.USB_STORAGE_TURNED_ON;
            break;
          }

          boolean isOnline = false;
          if (isCamera) {
            isOnline = online_user.queryCameraAvailableOnline(mac);
          } else {
            try {
              String registAt = online_user.getRegistrationAt(mac);
              long currentTime = System.currentTimeMillis();

              DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZoneUTC();
              long registAtMili = formatter.parseMillis(registAt);
              Log.d("Opensensor time", "registration date : " + registAt + " - registration milis : " + registAtMili + " - current time : " + currentTime + " - time: " + DateFormat.format("dd/MM/yyyy hh:mm:ss", currentTime).toString());
              long delayTime = currentTime - registAtMili;
              if (Math.abs(delayTime) < 15*60*1000) {
                isOnline = true;
              } else {
                isOnline = false;
              }
            } catch (IllegalArgumentException ex) {
              ex.printStackTrace();
              isOnline = false;
            }
          }

          if (isOnline) {
            Log.d(TAG, "FOUND the IP online??? ");
            foundConfiguredCam = true;
            Log.d(TAG, "Anyway .. Found it");
            //update host ssid for camera
            updateHostSsid(saved_token, regId, camera_name, home_ssid);
            try {
              online_user.sync_user_data();
            } catch (IOException e) {

              e.printStackTrace();
            }
            // BlinkHDApplication.KissMetricsRecord("Add camera succeeded.");
            break;
          } else {
            try {
              Thread.sleep(3000);
            } catch (InterruptedException e) {

            }
          }

        }

      } while (System.currentTimeMillis() < time_to_finish_scanning && !isCancelled());

      if (!foundConfiguredCam) {
        // Sth is wrong
        Log.d(TAG, "Can't find camera -- ");
        output = "Failed";
        errorCode = PublicDefine.SCAN_CAMERA_FAILED;
      }

      scan_task = null;
    } // if output.equals("Done");

    return output;
  }


  /**
   * 20140718: hoang: issue 722 set default anti flicker timezone from UTC+9
   * to UTC-4: 60Hz otherwises 50Hz
   */
  private void setDefaultAntiFlicker(String device_address_port) {
    int flicker_value = Util.getLocalHertz();
    String cmd, response = null;
    // set flicker default setting
    cmd = String.format("%1$s%2$s%3$s%4$s%5$s%6$d", "http://",
            device_address_port, PublicDefine.HTTP_CMD_PART,
            "set_flicker", "&value=",
            flicker_value);
    Log.d(TAG, "Sending flicker value: " + flicker_value);
    response = send_request(cmd, 10000);
    Log.d(TAG, "Set flicker " + flicker_value + " res: " + response);
  }

  public void setDefaultAlertSettings2(CamConfiguration  camconf,
                                       String device_address_port, String modelId, String fwVersion) {

    String cmd, response;
    //camconf could be Hubble CamConfiguration or VtechCamConfiguration object
    if ("0877".equals(modelId)) {
      cmd = "http://" + device_address_port + "/?action=command&command=get_md_type";
      response = send_request(cmd, 10000);
      Log.d(TAG, "get md type response: " + response);
      String currentMdType = null;
      if (!TextUtils.isEmpty(response)) {
        currentMdType = response.replace("get_md_type: ", "");
      }
      Log.d(TAG, "current md type: " + currentMdType);
      if ("MD".equals(currentMdType)) {
        Log.d(TAG, "should turn of md before start bsd mode");
        cmd = "http://" + device_address_port + "/?action=command&command=set_recording_parameter&value=01";
        send_request(cmd, 10000);
        cmd = "http://" + device_address_port + "/?action=command&command=set_motion_area&value=&grid=1x1&zone=";
        send_request(cmd, 10000);
      }
    }
    List<String> default_cmds = camconf.getDefaultAlertSettings(modelId,fwVersion);

    if (default_cmds != null && default_cmds.size() > 0) {
      Log.d(TAG, ">>> Send default alert setting commands from " + camconf.getClass().getSimpleName());
      for (int i = 0; i < default_cmds.size(); i++) {
        cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, "/?",
            default_cmds.get(i));
        response = send_request(cmd, 10000);
        Log.d(TAG, "Sending  cmd: " + default_cmds.get(i) + "\n response: " + response);
      }
    }

    // 20160129: binh: AA-1521
    if ("0931".equals(modelId)) {
      cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, "set_cam_park&value=0");
      response = send_request(cmd, 10000);
      Log.d(TAG, "Turn off park duration res: " + response);
    }
  }

//  public void setDefaultAlertSettings(String device_address_port, String modelId, String fwVersion) {
//    String cmd, response;
//
//    String motionAreaCommand;
//    if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") ||
//        BuildConfig.FLAVOR.equalsIgnoreCase("inanny")) {
//      motionAreaCommand = "set_motion_area&value=&grid=1x1&zone=00";
//    } else {
//      motionAreaCommand = "set_motion_area&value=&grid=1x1&zone=";
//    }
//    //set motion setting ON
//    cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, motionAreaCommand);
//    Log.d(TAG, "Sending motion cmd");
//    response = send_request(cmd, 10000);
//    Log.d(TAG, "Set motion on res: " + response);
//
//    if (PublicDefine.shouldEnableTemp(modelId)) {
//      String tempHighOnCommand = "set_temp_hi_enable" + "&value=0";
//      cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, tempHighOnCommand);
//      Log.d(TAG, "Sending tempHigh cmd");
//      response = send_request(cmd, 10000);
//      Log.d(TAG, "Set tempHigh on res: " + response);
//
//      String tempLowOnCommand = "set_temp_lo_enable" + "&value=0";
//      cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, tempLowOnCommand);
//      Log.d(TAG, "Sending tempLow cmd");
//      response = send_request(cmd, 10000);
//      Log.d(TAG, "Set tempLow on res: " + response);
//    }
//
//    if (PublicDefine.shouldEnableMic(modelId)) {
//      String soundDetectionOnCommand = "vox_disable";
//      cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, soundDetectionOnCommand);
//      Log.d(TAG, "Sending sound detection cmd");
//      response = send_request(cmd, 10000);
//      Log.d(TAG, "Set sound detection on res: " + response);
//    }
//
//    int cooloffDuration;
//    int activeDuration;
//    if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") ||
//        BuildConfig.FLAVOR.equalsIgnoreCase("inanny")) {
//      if (PublicDefine.shouldSetDurationInSeconds(modelId, fwVersion)) {
//        cooloffDuration = 120;
//        activeDuration = 90;
//      } else {
//        cooloffDuration = 0;
//        activeDuration = 0;
//      }
//    } else {
//      cooloffDuration = 120;
//      activeDuration = 90;
//    }
//    String recordCoolOffDurationCommand = "recording_cooloff_duration" + "&value=" + cooloffDuration;
//    cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, recordCoolOffDurationCommand);
//    Log.d(TAG, "Sending cooloff duration cmd");
//    response = send_request(cmd, 10000);
//    Log.d(TAG, "Set cooloff duration res: " + response);
//
//    String recordActiveDurationCommand = "recording_active_duration" + "&value=" + activeDuration;
//    cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, recordActiveDurationCommand);
//    Log.d(TAG, "Sending active duration cmd");
//    response = send_request(cmd, 10000);
//    Log.d(TAG, "Set active duration res: " + response);
//  }

  /**
   * Update host ssid for camera after add successfully
   *
   * @param host_ssid the ssid which camera is added
   */
  private void updateHostSsid(String saved_token, String regId, String camera_name, String host_ssid) {
    try {
      Device.changeBasicInfo(saved_token, regId, camera_name, null, null, null, host_ssid, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void updateScanResult(ScanProfile[] results, int status, int index) {
  }

  /* on UI Thread */
  protected void onPreExecute() {
    // Spanned msg =
    // Html.fromHtml("<big>"+mContext.getString(R.string.AutoConf_working_)+"</big>");
    //
    // dialog = new ProgressDialog(mContext);
    // dialog.setMessage(msg);
    // dialog.setIndeterminate(true);
    // dialog.setCancelable(false);
    // dialog.show();
  }

  /* on UI Thread */
  protected void onProgressUpdate(String... progress) {
    if (progress[0].equalsIgnoreCase(mContext.getResources().getString(R.string.camera_does_not_have_ssid_warning))) {
      AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
      Spanned msg = Html.fromHtml("<big>" + mContext.getString(R.string.camera_does_not_have_ssid_warning) + "</big>");
      builder.setMessage(msg).setCancelable(true).setPositiveButton(mContext.getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              isWaiting = false;
              continueAddCamera = true;
            }
          }
      ).setNegativeButton(mContext.getResources().getString(R.string.No), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              isWaiting = false;
              continueAddCamera = false;
            }
          }
      );

      AlertDialog alert = builder.create();
      alert.show();
    } else {
      // dialog.setMessage(msg);
    }

  }

  /* on UI Thread */
  protected void onPostExecute(@NotNull String result) {
    if (result.equalsIgnoreCase("Done")) {
      mHandler.sendMessage(Message.obtain(mHandler, PublicDefine.MSG_AUTO_CONF_SUCCESS));
    } else {

      ///AnalyticsController.getInstance().trackEvent("Add device failed", "Reason", "Error: " + String.valueOf(errorCode));
      mHandler.sendMessage(Message.obtain(mHandler, PublicDefine.MSG_AUTO_CONF_FAILED, errorCode, errorCode, errorMsg));
    }
  }

  protected void onCancelled() {
    super.onCancelled();
    // dialog.cancel();

    if (scan_task != null) {
      scan_task.stopScan();
      scan_task = null;
    }

    // // Log.d(TAG, "Configure camera canceled...");
    mHandler.sendMessage(Message.obtain(mHandler, PublicDefine.MSG_AUTO_CONF_CANCELED));
  }

  private boolean verify_response(String request_command, String response) {
    if ((response != null) && (response.startsWith(request_command))) {
      String response_code = response.substring(request_command.length());
      try {
        if (Integer.parseInt(response_code) == 0) {
          return true;
        }
      } catch (NumberFormatException ne) {
        Log.d(TAG, "Response not a number: " + Log.getStackTraceString(ne));
        return false;
      }
    }
    return false;
  }

  /* handle url connection */
  private String send_request(String request, int timeout) {
    URL url;
    int responseCode;
    HttpURLConnection conn = null;
    DataInputStream _inputStream = null;
    String response_str = null;

    String usr_pass = String.format("%s:%s", http_usr, http_pass);
    try {
      url = new URL(request);
      /* send the request to device by open a connection */
      conn = (HttpURLConnection) url.openConnection();
      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP)
      );
      conn.setConnectTimeout(timeout);
      conn.setReadTimeout(timeout);

    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    try {

      responseCode = conn != null ? conn.getResponseCode() : 0;
      if (responseCode == HttpURLConnection.HTTP_OK) {
        _inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
        /* make sure the return type is text before using readLine */
        response_str = _inputStream.readLine();
      }

    } catch (Exception ex) {
      // continue;
      ex.printStackTrace();
    } finally {
      if (_inputStream != null) {
        try {
          _inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (conn != null) {
        conn.disconnect();
      }
    }

    return response_str;
  }

  private void notifyScanResult() {
    synchronized (this) {
      this.notify();
    }
  }

  private void notifySupplicantState(SupplicantState ss) {

    synchronized (supplicantLockObject) {
      sstate = ss;
      supplicantLockObject.notify();
    }
  }

  private void notifyWifiState(String bssid) {
    synchronized (wifiLockObject) {
      wifiLockObject.notify();
    }
  }

  /*
   * For WPA2 authentication: SupplicantState in case of ERROR - (ASSOCIATING)
   * - ASSOCIATED - FOUR_WAY_HANDSHAKE - DISCONNECTED - INACTIVE <---- We will
   * wait for this SupplicantState in case of SUCCESS - (ASSOCIATING) -
   * ASSOCIATED - FOUR_WAY_HANDSHAKE - GROUP_HANDSHAKE - COMPLETED <---- We
   * will wait for this
   */

  private class WifiReceiver1 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      switch (action) {
        case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
          notifyScanResult();
          break;
        case WifiManager.NETWORK_STATE_CHANGED_ACTION:
          WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
          if (wm != null && wm.getDhcpInfo() != null) {
            Log.d(TAG, "NW_STATE, ip address: " + wm.getDhcpInfo().ipAddress);
            if (wm.getDhcpInfo().ipAddress != 0) {
              notifyWifiState(null);
            }
          }
          break;
        case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
          SupplicantState ss = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
          // // // Log.d(TAG, "SUPPLICANT_STATE_CHANGED_ACTION: " + ss);

          if (ss.equals(SupplicantState.COMPLETED) || ss.equals(SupplicantState.INACTIVE) || ss.equals(SupplicantState.DISCONNECTED)) {
            notifySupplicantState(ss);
          }

          break;
        case WifiManager.WIFI_STATE_CHANGED_ACTION:
          mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

          // notifyWifiState(null);
          break;
        default:
          break;
      }
    }
  }
}
