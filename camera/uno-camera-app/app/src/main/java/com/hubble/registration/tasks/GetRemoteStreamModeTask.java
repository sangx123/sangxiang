package com.hubble.registration.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import base.hubble.meapi.Device;
import base.hubble.meapi.device.GetCameraInfoResponse;


/**
 * @author phung
 *         <p/>
 *         <p/>
 *         https://monitoreverywhere.com/BMS28/phoneservice?action=command&command=get_stream_mode&mac=<mac>
 *         <p/>
 *         Response:
 *         HTTP/1.1 200 OK
 *         Contents:
 *         Streaming_mode=0
 *         <p/>
 *         Result:
 *         - 0 - unknown
 *         - 1 - UPNP
 *         - 2 - ManualPortMapping
 *         - 3 - UDT mode
 */
public class GetRemoteStreamModeTask extends AsyncTask<String, String, Integer> {


  private static final String TAG = "GetRemoteStreamModeTask";
  private String regId;
  private String userToken;
  private static final String STREAMING_MODE = "Streaming_mode=";

  private static final int GET_MODE_SUCCESS = 0x1;
  private static final int GET_MODE_FAILED_UNKNOWN = 0x2;
  private static final int GET_MODE_FAILED_SERVER_DOWN = 0x11;

  public static final int MSG_GET_MODE_TASK_SUCCESS = 0xDE000006;
  public static final int MSG_GET_MODE_TASK_FAILED = 0xDE000007;

  public static final int STREAM_MODE_UNKNOWN = 0;
  public static final int STREAM_MODE_MANUAL_PORT_FWD = 1;
  public static final int STREAM_MODE_UPNP = 2;
  public static final int STREAM_MODE_UDT = 3;
  public static final int STREAM_MODE_RELAY = 5;

  private Handler mHandler;
  private int server_err_code;

  private int result;

  /* handler h can be NULL */
  public GetRemoteStreamModeTask(Handler h, Context mContext) {
    mHandler = h;
    result = STREAM_MODE_UNKNOWN;
  }


  @Override
  protected Integer doInBackground(String... params) {

    regId = params[0];
    userToken = params[1];

    int ret = -1;
    try {
      GetCameraInfoResponse cam_res = Device.getCameraInfo(userToken, regId);
      if (cam_res.isSucceed()) {
        ret = GET_MODE_SUCCESS;
        if (cam_res.getCameraInfo() != null && cam_res.getCameraInfo().getDevice_accessibility() != null) {
          String result_str = cam_res.getCameraInfo().getDevice_accessibility().getMode();
          // // Log.d("mbp", "Stream mode: " + result_str);
          result = getStreamModeIntValue(result_str);
        }
      } else {
        server_err_code = cam_res.getStatus();
      }
    } catch (NumberFormatException nfe) {
      // // Log.e(TAG, Log.getStackTraceString(nfe));
      ret = GET_MODE_FAILED_UNKNOWN;
    } catch (MalformedURLException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
      ret = GET_MODE_FAILED_SERVER_DOWN;
    } catch (SocketTimeoutException se) {
      // // Log.e(TAG, Log.getStackTraceString(se));
      ret = GET_MODE_FAILED_SERVER_DOWN;
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
      ret = GET_MODE_FAILED_SERVER_DOWN;
    }

    if (ret != GET_MODE_SUCCESS) {
      result = STREAM_MODE_UNKNOWN;
      return null;
    }

    return result;
  }

  /* UI thread */
  protected void onPostExecute(Integer result) {
    if (mHandler != null) {
      Message m;
      if ((result != null)) {
        m = Message.obtain(mHandler, MSG_GET_MODE_TASK_SUCCESS, result);
      } else {
        m = Message.obtain(mHandler, MSG_GET_MODE_TASK_FAILED, server_err_code, server_err_code);
      }
      mHandler.dispatchMessage(m);
    }
  }

  /**
   * @param result_str
   * @return
   */
  private int getStreamModeIntValue(String result_str) {
    int ret;
    if (result_str.equalsIgnoreCase("upnp")) {
      ret = STREAM_MODE_UPNP;
    } else if (result_str.equalsIgnoreCase("stun")) {
      ret = STREAM_MODE_UDT;
    } else if (result_str.equalsIgnoreCase("relay")) {
      ret = STREAM_MODE_RELAY;
    } else {
      ret = STREAM_MODE_UNKNOWN;
    }

    return ret;
  }
}




