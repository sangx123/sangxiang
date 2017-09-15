package com.hubble.registration.tasks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;

import com.discovery.ScanProfile;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.UserAccount;
import com.hubble.registration.Util;
import com.hubble.registration.interfaces.ICameraScanner;
import com.hubble.registration.models.LegacyCamProfile;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimeZone;

import base.hubble.PublicDefineGlob;
import base.hubble.database.DeviceProfile;
import base.hubble.meapi.device.CheckStatusResponse;
import base.hubble.meapi.device.CheckStatusResponseData;
import com.hubbleconnected.camera.BuildConfig;

public class ConfigureLANCamera extends AsyncTask<LegacyCamProfile, String, Boolean> implements ICameraScanner {
  private static final String TAG = "ConfigureLANCamera";

  private static final int DEFAULT_SEARCHING_FOR_NEW_NETWORK = 20000; // sec
  private static final int DEFAULT_WAITING_FOR_CONNECTION = 30000;   // sec

  public static final int MSG_AUTO_CONF_SUCCESS = 0x1111;
  public static final int MSG_AUTO_CONF_FAILED = 0x2222;
  public static final int MSG_AUTO_CONF_CANCELED = 0x3333;
  public static final int MSG_AUTO_CONF_SHOW_ABORT_PAGE = 0x4444;

  public static final int START_SCAN_FAILED = 0x2225;
  public static final int CONNECT_TO_CAMERA_FAILED = 0x2226;
  public static final int SEND_DATA_TO_CAMERA_FAILED = 0x2227;
  public static final int CONNECT_TO_HOME_WIFI_FAILED = 0x2228;
  public static final int SCAN_CAMERA_FAILED = 0x2229;
  public static final int CAMERA_DOES_NOT_HAVE_SSID = 0x2230;
  public static final int USB_STORAGE_TURNED_ON = 0x2231;
  public static final int INCORRECT_WIFI_PASSWORD = 0x2232;

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
  private String home_ssid, home_key;
  private String security_type;
  private String wep_auth_mode, wep_key_index;
  private String http_usr, http_pass;
  private WifiConfiguration conf;
  private long start_time = 0;
  private boolean isCancelable;
  private LegacyCamProfile cam_profile = null;

  public ConfigureLANCamera(Context mContext, Handler h) {
    this.mContext = mContext;
    wifiLockObject = new Object();
    supplicantLockObject = new Object();
    sstate = null;
    mWifiNetworkInfo = null;
    mWifiState = -1;
    wep_auth_mode = wep_key_index = null;
    conf = null;
    mHandler = h;
  }

  public void setStartTime(long start_time) {
    this.start_time = start_time;
  }

	/*
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

  protected Boolean doInBackground(LegacyCamProfile... camProfiles) {
    // // Log.d(TAG, "starting configuration of LAN camera");
    isCancelable = false;
    cam_profile = camProfiles[0];
    http_usr = cam_profile.getBasicAuth_usr();
    http_pass = cam_profile.getBasicAuth_pass();
    String regId = cam_profile.getRegistrationId();
    // // Log.d(TAG, "Camera regId: " + regId);
    long currentTime = System.currentTimeMillis();
    if (currentTime < start_time + 6000) {
      // // Log.d(TAG, "Auto configure camera, sleep for: " + (start_time + 6000 - currentTime));
      safeSleep(start_time + 6000 - currentTime);
    }
    String gatewayIp = cam_profile.get_inetAddress().getHostAddress();
    String device_address_port = gatewayIp + ":" + PublicDefineGlob.DEVICE_PORT;
    Log.d(TAG, "Turn all default alert settings ON, address port: " + device_address_port);
    setDefaultAntiFlicker(device_address_port);
    setDefaultAlertSettings(device_address_port, PublicDefine.getModelIdFromRegId(regId),
            cam_profile.getFirmwareVersion());

    String saved_token = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
    int offset = TimeZone.getDefault().getRawOffset();
    String timeZone = String.format("%s%02d.%02d", offset >= 0 ? "+" : "-", Math.abs(offset) / 3600000, (Math.abs(offset) / 60000) % 60);
    if (setDeviceAuthentication(device_address_port, saved_token, timeZone)) {
      long time_to_show_abort_page = System.currentTimeMillis() + 60 * 1000;

      restartDevice(device_address_port);

      boolean deviceStatus = spinCheckDeviceStatus(regId, saved_token);

      if (deviceStatus == true) {
        DeviceProfile profile = cam_profile.toDeviceProfile();
        if (profile != null) {
          Device device = DeviceSingleton.getInstance().factory.build(profile);
          // Don't need to check whether camera is in same network
//        if (CameraAvailabilityManager.getInstance().isCameraInSameNetwork(mContext, device)) { // Need to fall through to next return statement if false here.
//          return true;
//        }
        }

        return checkUserAccountForDevice(regId, saved_token, time_to_show_abort_page);
      } else {
        Log.d(TAG, "Configure LAN camera failed: device status is false");
      }
    } else {
      Log.d(TAG, "Set device authentication error!");
    }
    return false;
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
    Log.d(TAG, "Sending flicker cmd");
    response = send_request(cmd, 10000);
    Log.d(TAG, "Set flicker " + flicker_value + " res: " + response);
  }

  private void setDefaultAlertSettings(String device_address_port, String modelId, String fwVersion) {
    String cmd, response;

    String motionAreaCommand;
    if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew") ||
        BuildConfig.FLAVOR.equalsIgnoreCase("inanny") || BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
      motionAreaCommand = "set_motion_area&value=&grid=1x1&zone=00";
    } else {
      motionAreaCommand = "set_motion_area&value=&grid=1x1&zone=";
    }
    //set motion setting ON
    cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, motionAreaCommand);
    Log.d(TAG, "Sending motion cmd");
    response = send_request(cmd, 10000);
    Log.d(TAG, "Set motion on res: " + response);

    if (PublicDefine.shouldEnableTemp(modelId)) {
      String tempHighOnCommand;
      if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny") || BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
        tempHighOnCommand = "set_temp_hi_enable" + "&value=1";
      } else {
        tempHighOnCommand = "set_temp_hi_enable" + "&value=0";
      }
      cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, tempHighOnCommand);
      Log.d(TAG, "Sending tempHigh cmd");
      response = send_request(cmd, 10000);
      Log.d(TAG, "Set tempHigh on res: " + response);

      String tempLowOnCommand;
      if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny") || BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
        tempLowOnCommand = "set_temp_lo_enable" + "&value=1";
      } else {
        tempLowOnCommand = "set_temp_lo_enable" + "&value=0";
      }
      cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, tempLowOnCommand);
      Log.d(TAG, "Sending tempLow cmd");
      response = send_request(cmd, 10000);
      Log.d(TAG, "Set tempLow on res: " + response);
    }

    if (PublicDefine.shouldEnableMic(modelId)) {
      String soundDetectionOnCommand;
      if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny") || BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
        soundDetectionOnCommand = "vox_enable";
      } else {
        soundDetectionOnCommand = "vox_disable";
      }
      cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, soundDetectionOnCommand);
      Log.d(TAG, "Sending sound detection cmd");
      response = send_request(cmd, 10000);
      Log.d(TAG, "Set sound detection on res: " + response);
    }

    int cooloffDuration;
    int activeDuration;
    if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew") ||
        BuildConfig.FLAVOR.equalsIgnoreCase("inanny") || BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
      if (PublicDefine.shouldSetDurationInSeconds(modelId, fwVersion)) {
        cooloffDuration = 120;
        activeDuration = 90;
      } else {
        cooloffDuration = 0;
        activeDuration = 0;
      }
    } else {
      cooloffDuration = 120;
      activeDuration = 90;
    }
    String recordCoolOffDurationCommand = "recording_cooloff_duration" + "&value=" + cooloffDuration;
    cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, recordCoolOffDurationCommand);
    Log.d(TAG, "Sending cooloff duration cmd");
    response = send_request(cmd, 10000);
    Log.d(TAG, "Set cooloff duration res: " + response);

    String recordActiveDurationCommand = "recording_active_duration" + "&value=" + activeDuration;
    cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, recordActiveDurationCommand);
    Log.d(TAG, "Sending active duration cmd");
    response = send_request(cmd, 10000);
    Log.d(TAG, "Set active duration res: " + response);

    // 20160129: binh: AA-1521
    if ("0931".equals(modelId)) {
      cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, "set_cam_park&value=0");
      response = send_request(cmd, 10000);
      Log.d(TAG, "Turn off park duration res: " + response);
    }
  }

  private boolean checkUserAccountForDevice(String regId, String saved_token, long time_to_show_abort_page) {
    Log.d(TAG, "checkUserAccountForDevice");
    String mac = PublicDefine.getMacFromRegId(regId).toUpperCase();
    mac = PublicDefineGlob.add_colon_to_mac(mac);
    boolean foundConfiguredCam = false;
    long time_to_finish_scanning = System.currentTimeMillis() + 3 * 60 * 1000;
    boolean abortPageShown = false;

    while (System.currentTimeMillis() < time_to_finish_scanning && !isCancelled()) {
      if (System.currentTimeMillis() > time_to_show_abort_page && !abortPageShown) {
        abortPageShown = true;
        if (mHandler != null) {
          mHandler.sendMessage(Message.obtain(mHandler, MSG_AUTO_CONF_SHOW_ABORT_PAGE));
        }
      }
      if (isCancelled()) {
        break;
      }
      Log.d(TAG, "Querying online for camera...");
      // reset to the nxt minute
      UserAccount online_user;
      try {
        online_user = new UserAccount(saved_token, mContext.getExternalFilesDir(null), null, mContext);
      } catch (Exception e1) {
        // // Log.d(TAG, e1.getLocalizedMessage());
        errorCode = USB_STORAGE_TURNED_ON;
        break;
      }

      boolean isOnline = online_user.queryCameraAvailableOnline(mac);
      Log.d(TAG, "Device online: " + isOnline);
      if (isOnline) {
        foundConfiguredCam = true;
        // updateHostSsid(saved_token, regId, home_ssid);
        DeviceSingleton.getInstance().update(false);
        break;
      } else {
        // // Log.d(TAG, "Sleeping for a second...");
        safeSleep(1000);
      }
    }
    if (!foundConfiguredCam) {
      // Sth is wrong
      Log.d(TAG, "Can't find camera -- ");
      errorCode = SCAN_CAMERA_FAILED;
    }
    Log.d(TAG, "checkUserAccountForDevice done, foundConfiguredCam? " + foundConfiguredCam);
    return foundConfiguredCam;
  }

  private boolean spinCheckDeviceStatus(String regId, String saved_token) {
    // // Log.d(TAG, "spinCheckDeviceStatus");
    int k2 = 0;
    boolean shouldExit = false;
    while (k2++ < 15 && !shouldExit && !isCancelled()) {
      shouldExit = checkDeviceStatus(regId, saved_token);
      if (shouldExit) break;
      safeSleep(2000);
    }
    return shouldExit;
  }

  private boolean setDeviceAuthentication(String device_address_port, String saved_token, String timeZone) {
    // // Log.d(TAG, "setDeviceAuthentication");
    int k = 0;
    while (k++ < 10 && !isCancelled()) {
      String set_auth_cmd = String.format("%1$s%2$s%3$s%4$s%5$s%6$s%7$s%8$s",
          "http://", device_address_port, "/?action=command&command=", "set_server_auth", "&value=", saved_token, "&timezone=", timeZone);
      String response = send_request(set_auth_cmd, 20000);
      // // Log.d(TAG, "Set server auth res: " + response);
      if (!verify_response(PublicDefineGlob.SET_SERVER_AUTHENTICATION_RESPONSE, response)) {
        // loop back;
        // // Log.d(TAG, "Failed to send server auth: " + response + " retry: " + k);
        errorCode = SEND_DATA_TO_CAMERA_FAILED;
        errorMsg = mContext.getString(R.string.send_data_to_camera_failed);
      } else {
        return true;
      }
    }
    return false;
  }

  private boolean checkDeviceStatus(String regId, String saved_token) {
    Log.d(TAG, "checking device status...");
    boolean deviceStatusRet = false;
    try {
      CheckStatusResponse res = base.hubble.meapi.Device.checkStatus(saved_token, regId);
      if (res != null && res.getStatus() == HttpURLConnection.HTTP_OK && res.getData() != null) {
        CheckStatusResponseData checkStatusData = res.getData();
        if (checkStatusData != null) {
          int status = checkStatusData.getDevice_status();
          Log.d(TAG, "check device status res: " + status);
          switch (status) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 5:
              deviceStatusRet = true;
              break;
            case 1:
              errorMsg = mContext.getString(R.string.device_is_not_present_in_device_master);
              deviceStatusRet = false;
              break;
            default:
              break;
          }
        }
      }
    } catch (Exception e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }
    Log.d(TAG, "checking device status DONE, device status? " + deviceStatusRet);
    return deviceStatusRet;
  }

  private void restartDevice(String device_address_port) {
    String response;
    int k2 = 0;
    do {
      String restart_cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.RESTART_DEVICE);
      response = send_request(restart_cmd, 5000);
      // // Log.d(TAG, "restart--->>: " + response);
      // "restart_system: 0" / -1
      if (!verify_response("restart_system: ", response)) {
      } else {
        break;
      }
    } while (k2++ < 5 && !isCancelled());
  }

  private void safeSleep(long time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
    }
  }

  /**
   * Update host ssid for camera after add successfully
   *
   * @param host_ssid the ssid which camera is added
   */
  private void updateHostSsid(String saved_token, String regId, String host_ssid) {
    try {
      base.hubble.meapi.Device.changeBasicInfo(saved_token, regId, null, null, null, null, host_ssid, null);
    } catch (Exception e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }
  }

  @Override
  public void updateScanResult(ScanProfile[] results, int status, int index) {
  }

  /* on UI Thread */
  protected void onProgressUpdate(String... progress) {
    Spanned msg = Html.fromHtml("<big>" + mContext.getString(R.string.camera_does_not_have_ssid_warning) + "</big>");
    if (!isCancelable) {
      if (progress[0].equalsIgnoreCase(mContext.getResources().getString(R.string.camera_does_not_have_ssid_warning))) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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
      }
    }
  }

  /* on UI Thread */
  protected void onPostExecute(Boolean success) {
    if (success) {
      mHandler.dispatchMessage(Message.obtain(mHandler, MSG_AUTO_CONF_SUCCESS));
    } else {
      mHandler.dispatchMessage(Message.obtain(mHandler, MSG_AUTO_CONF_FAILED, errorCode, errorCode, errorMsg));
    }
  }

  protected void onCancelled() {
    super.onCancelled();
    // // Log.d(TAG, "Configure camera canceled...");
    mHandler.sendMessage(Message.obtain(mHandler, MSG_AUTO_CONF_CANCELED));
  }

  private boolean verify_response(String request_command, String response) {
    if ((response != null) && (response.startsWith(request_command))) {
      String response_code = response.substring(request_command.length());
      try {
        if (Integer.parseInt(response_code) == 0) {
          // OK
          return true;
        }
      } catch (NumberFormatException ne) {
        // // Log.e(TAG, "Response from camera not a number.");
        return false;
      }
    }
    return false;
  }

  /* handle url connection */
  private String send_request(String request, int timeout) {
    URL url;
    int responseCode = -1;
    HttpURLConnection conn = null;
    DataInputStream _inputStream;
    String response_str = null;
    String usr_pass = String.format("%s:%s", http_usr, http_pass);
    try {
      url = new URL(request);
      /* send the request to device by open a connection */
      conn = (HttpURLConnection) url.openConnection();
      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));
      conn.setConnectTimeout(timeout);
      conn.setReadTimeout(timeout);
    } catch (Exception ioe) {
      // // Log.e(TAG, Log.getStackTraceString(ioe));
    }
    try {
      responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        _inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
        /* make sure the return type is text before using readLine */
        response_str = _inputStream.readLine();
      }
    } catch (Exception ex) {
      // // Log.e(TAG, Log.getStackTraceString(ex));
    } finally {
      conn.disconnect();
    }
    return response_str;
  }

}
