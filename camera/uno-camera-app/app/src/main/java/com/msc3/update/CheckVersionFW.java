package com.msc3.update;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubbleconnected.camera.R;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import base.hubble.PublicDefineGlob;
import base.hubble.meapi.Device;
import base.hubble.meapi.device.CameraInfo;
import base.hubble.meapi.device.GetCameraInfoResponse;

public class CheckVersionFW extends AsyncTask<String, String, String> {
  private static final String MY_DEBUG_TAG = "mbp";
  private static final String no_patch = "check_fw_upgrade: 0";
  private static final String no_patch1 = "check_fw_upgrade: -1";

  private static final String request_upgrade = "request_upgrade";
  private static final String processing = "processing";
  private static final String upgrade_timeout = "upgrade_timeout";

  private static final String user = "msc2000";
  private static final String pass = "patch2012";
  public static final String CHECK_FW_UPGRADE = "check_fw_upgrade";
  public static final String REQUEST_FW_UPGRADE = "request_fw_upgrade";

  private static final long TIME_OUT = 3 * 60 * 1000;
  private static final long EXTEND_TIME_OUT = 2 * 60 * 1000;

  public static final int PATCH_AVAILABLE = 0xde000005;
  public static final int NO_PATCH_AVAILABLE = 0xde000006;
  public static final int UPGRADE_DONE = 0xde000008;
  public static final int UPGRADE_FAILED = 0xde000009;
  private static final String TAG = "CheckVersionFW";

  private Context mContext;
  private Handler mHandler;
  private boolean check_status;
  private String device_ip;
  private int device_port;
  private String device_version;
  private IpAndVersion parse_object;
  private Message mess;
  private ProgressDialog dialog;
  private String new_version;
  private String regId;
  private String portalUser, portalPass;
  private String reason;

  public CheckVersionFW(Context c, Handler h, boolean check, String newVersion, String regId, String portalUser, String portalPass) {
    new_version = newVersion;
    check_status = check;
    mContext = c;
    mHandler = h;
    this.regId = regId;

    this.portalUser = portalUser;
    this.portalPass = portalPass;

  }


  private String checkVersion(String regId, String user, String pass) {
    String usr_pass = String.format("%s:%s", user, pass);

    String http_cmd = "http://" + device_ip + ":" + device_port +
        PublicDefineGlob.HTTP_CMD_PART + PublicDefineGlob.GET_VERSION;

    URL url = null;
    HttpURLConnection conn = null;
    DataInputStream inputStream = null;
    String contentType = null;
    String defaultVersion = null, response;


    try {
      url = new URL(http_cmd);
      conn = (HttpURLConnection) url.openConnection();

      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));

      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);

      inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      contentType = conn.getContentType();
      /* make sure the return type is text before using readLine */
      if (contentType != null && contentType.equalsIgnoreCase("text/plain")) {
        response = inputStream.readLine();
        if (response.startsWith(PublicDefineGlob.GET_VERSION)) {
          defaultVersion = response.substring(PublicDefineGlob.GET_VERSION.length() + 2);
        }
      }

    } catch (MalformedURLException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    } catch (SocketTimeoutException se) {
      //Connection Timeout - Server unreachable ???
      // // Log.e(TAG, Log.getStackTraceString(se));
      defaultVersion = null;
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
      defaultVersion = null;
    }

    return defaultVersion;
  }

  private CameraInfo getCamInfo(String saved_token, String device_id) {
    CameraInfo cam_info = null;

    try {
      GetCameraInfoResponse cam_res = Device.getCameraInfo(saved_token, device_id);
      if (cam_res != null && cam_res.getStatus() == HttpURLConnection.HTTP_OK) {
        cam_info = cam_res.getCameraInfo();
      }
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }

    return cam_info;
  }


  protected String doInBackground(String... params) {
    String response = null;
    String status = null;
    int percent = -100, sleep_time = 3000;
    device_ip = params[0];
    try {
      device_port = Integer.parseInt(params[1]);
    } catch (NumberFormatException e1) {
      // // Log.e(TAG, Log.getStackTraceString(e1));
    }

    //send a query
    String http_cmd = String.format("http://%s:%s%s%s", params[0], params[1], params[2], params[3]);

    if (params[3].equals(CHECK_FW_UPGRADE)) {
      //get a response to analyze
      response = HTTPRequestSendRecvTask.sendRequest_block_for_response(http_cmd, user, pass);
//			try {
//				Thread.sleep(sleep_time);
//			} catch (InterruptedException e) {
//			}
      //response = CHECK_FW_UPGRADE + ": 01.14.01";
      if (response != null && response.startsWith(CHECK_FW_UPGRADE)) {
        status = response.trim();
      } else {
        status = null;
      }
    }
    // if this is REQUEST_FW_UPGRADE then...
    else if (params[3].equals(REQUEST_FW_UPGRADE)) {
      status = request_upgrade;
      response = HTTPRequestSendRecvTask.sendRequest_block_for_response(http_cmd, user, pass);
      // // Log.e(MY_DEBUG_TAG, "respone >>> " + response);
      if (response != null && response.startsWith(REQUEST_FW_UPGRADE)) {
        response = response.substring(REQUEST_FW_UPGRADE.length() + 2);
      }

      if (response != null && response.equals("0")) {
        SecureConfig settings = HubbleApplication.AppConfig;
        String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

        status = processing;
        long endTime = System.currentTimeMillis() + TIME_OUT;
        long currentTime;
        CameraInfo cam_info = null;
        int previous_fw_status = 0;
        boolean continue_upgrade = true;
        while (continue_upgrade) {
          //timeout after 5min
          if (System.currentTimeMillis() > endTime) {
            continue_upgrade = false;
            status = upgrade_timeout;
            continue;
          }

          cam_info = getCamInfo(saved_token, regId);
          if (cam_info != null) {
            // // Log.e(MY_DEBUG_TAG, "Upgrading, check upgrading flag: " + cam_info.getFirmware_status());
            if (cam_info.getFirmware_status() == 1 || (cam_info.getFirmware_status() == 0 && previous_fw_status == 0)) {
              // upgrading
              previous_fw_status = cam_info.getFirmware_status();
              reason = mContext.getString(R.string.camera_upgrade_could_not_be_completed);
            } else {
              // // Log.e(MY_DEBUG_TAG, "Upgrading, check fw version: " + cam_info.getFirmware_version());
              if (cam_info.getFirmware_version().equalsIgnoreCase(new_version)) {
                // correct firmware version
                continue_upgrade = false;
                continue;
              } else {
                reason = mContext.getString(R.string.incorrect_firmware_version);
              }
            }

          } //if (cam_info != null)

          try {
            currentTime = System.currentTimeMillis();
            percent = (int) ((double) ((TIME_OUT + EXTEND_TIME_OUT) -
                (endTime + EXTEND_TIME_OUT - currentTime)) / (double) (TIME_OUT + EXTEND_TIME_OUT) * 100);
            //// // Log.e(MY_DEBUG_TAG, "% upgrade: " + percent);
            if (percent >= 0) {
              // show the progress
              String displayMessage = String.format(mContext.getString(R.string.upgrading_firmware_do_not_power_off_the_camera), String.valueOf(percent));
              publishProgress(displayMessage);
            }

          } catch (NumberFormatException nfe) {
            // // Log.e(TAG, Log.getStackTraceString(nfe));
          }

          //// // Log.e(MY_DEBUG_TAG, "Start to sleep for 3s");
          try {
            Thread.sleep(sleep_time);
          } catch (InterruptedException e) {
          }

        } //while (continue_upgrade == true)

        // check version success, continue to check camera online
        if (status.equalsIgnoreCase(processing)) {
          currentTime = System.currentTimeMillis();
          long remaining_time = endTime - currentTime;
          if (remaining_time < 0) {
            remaining_time = 0;
          }
          endTime = endTime + EXTEND_TIME_OUT - remaining_time;
          boolean continue_check_available = true;
          while (continue_check_available) {
            //timeout after 2min
            if (System.currentTimeMillis() > endTime) {
              continue_check_available = false;
              status = upgrade_timeout;
              reason = mContext.getString(R.string.camera_offline_after_upgrade);
              continue;
            }

            cam_info = getCamInfo(saved_token, regId);
            if (cam_info != null && cam_info.isIs_available()) {
              // upgrade done
              continue_check_available = false;
              continue;
            }

            try {
              currentTime = System.currentTimeMillis();
              percent = (int) ((double) ((TIME_OUT + EXTEND_TIME_OUT - remaining_time) - (endTime - currentTime)) / (double) (TIME_OUT + EXTEND_TIME_OUT - remaining_time) * 100);
              //// // Log.e(MY_DEBUG_TAG, "% upgrade: " + percent);
              if (percent >= 0) {
                // show the progress
                String displayMessage = String.format(mContext.getString(R.string.upgrading_firmware_do_not_power_off_the_camera), String.valueOf(percent));
                publishProgress(displayMessage);
              }

            } catch (NumberFormatException nfe) {
              // // Log.e(TAG, Log.getStackTraceString(nfe));
            }

            //// // Log.e(MY_DEBUG_TAG, "Start to sleep for 3s");
            try {
              Thread.sleep(sleep_time);
            } catch (InterruptedException e) {
            }

          } //while (continue_check_available == true)

        } //if (status.equalsIgnoreCase(processing))
      } else {
        // request firmware upgrade failed
        reason = mContext.getString(R.string.request_fw_upgrade_failed);
      }

    }

    return status;
  }

  protected void onPreExecute() {
    if (check_status) {
      Spanned msg = Html.fromHtml("<big>" + mContext.getString(R.string.camera_is_upgrading_please_do_not_power_off_) + "</big>");
      dialog = new ProgressDialog(mContext);
      dialog.setIcon(R.drawable.ic_launcher);
      dialog.setMessage(msg);
      dialog.setIndeterminate(true);
      dialog.setCancelable(false);
      dialog.show();
    }
  }

  protected void onProgressUpdate(String... progress) {
    if (check_status) {
      Spanned msg = Html.fromHtml("<big>" + progress[0] + "</big>");
      dialog.setMessage(msg);
    }
  }

  protected void onPostExecute(String result) {
    if (dialog != null) {
      dialog.dismiss();
    }

    if (result == null) {
      // // Log.e(MY_DEBUG_TAG, "some error while checking version .. skip silencely");
    } else if (result.equals(upgrade_timeout)) {
      // // Log.e(MY_DEBUG_TAG, "Upgrade timeout...");
      mess = Message.obtain(mHandler, UPGRADE_FAILED, reason);
      mHandler.sendMessage(mess);
    } else if (result.equals(request_upgrade)) {
      // camera maybe busy
      // // Log.e(MY_DEBUG_TAG, "Request FW upgrade failed...");
      mess = Message.obtain(mHandler, UPGRADE_FAILED, reason);
      mHandler.sendMessage(mess);
    } else if (result.equals(processing)) {
      // // Log.e(MY_DEBUG_TAG, "Upgrade processing is complete...");
      //send a message to EntryActivity in order to exit AsyncTask
      mess = Message.obtain(mHandler, UPGRADE_DONE);
      mHandler.sendMessage(mess);
    } else if (result.equals(no_patch) || result.equals(no_patch1)) {
      mess = Message.obtain(mHandler, NO_PATCH_AVAILABLE);
      mHandler.sendMessage(mess);
      // // Log.d(MY_DEBUG_TAG, "Check firmware upgrade result: " + result + " -->> no patch found");
    } else {
      if (device_ip != null) {
        if (result.startsWith(CHECK_FW_UPGRADE)) {
          device_version = result.substring(CHECK_FW_UPGRADE.length() + 2); //get version string
          // // Log.e(MY_DEBUG_TAG, "Found newer version " + device_version +
          //" for this FW -- send message");
          parse_object = new IpAndVersion(device_ip, device_version); //package all into an object
          mess = Message.obtain(mHandler, CheckVersionFW.PATCH_AVAILABLE, parse_object);
          mHandler.sendMessage(mess);
        } else {
          // // Log.e(MY_DEBUG_TAG, "Invalid check_fw_upgrade res: " + result);
        }
      } else {
        // // Log.e(MY_DEBUG_TAG, "Device ip is null.. skip this for now");
      }
    }
  }
}
