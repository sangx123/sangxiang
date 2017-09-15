package com.hubble.registration.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.hubble.registration.models.BabyMonitorAuthentication;


/**
 * @author phung
 *         <p/>
 *         <p/>
 *         Response:
 *         HTTP/1.1 200 OK
 *         Contents:
 *         cam_url=http://<ip_address>:<cam_port>/?action=appletvastream&
 *         remote_session=<session authentication key 32 bytes HEX string><CR><LF>
 *         <p/>
 *         HTTP/1.1 202 ACCEPTED (but not processed due to errors)
 *         Contents:
 *         Error=251<CR><LF>Description=Invalid MAC Address/invalid format
 *         Error=201<CR><LF>Description=User email is not registered
 *         Error=911<CR><LF>Description=Web server under maintenance
 *         <p/>
 *         Result:
 *         - Code : OK or not OK, any errors
 *         - Ip:port
 *         - session_key:
 */
public abstract class RemoteStreamTask extends AsyncTask<String, String, BabyMonitorAuthentication> {

  private static final String TAG = "RemoteStreamTask";
  private String usrName;
  private String usrPass;
  private String macAddress;
  private String camName;

  private static final String CAM_IP = "Camera_IP=";
  private static final String CAM_PORT = "<br>Camera_Port=";
  private static final String SS_KEY = "SessionAutenticationKey=";
  private static final int SS_KEY_LEN = 64;

  private static final int VIEW_CAM_SUCCESS = 0x1;
  private static final int VIEW_CAM_FAILED_UNKNOWN = 0x2;
  private static final int VIEW_CAM_FAILED_SERVER_DOWN = 0x11;

  public static final int MSG_VIEW_CAM_SUCCESS = 0xDE000001;
  public static final int MSG_VIEW_CAM_FALIED = 0xDE000002;
  public static final int MSG_VIEW_CAM_CANCELED = 0xDE00000A;
  public static final int MSG_VIEW_CAM_SWITCH_TO_RELAY = 0xDE00000B;

  protected Handler mHandler;
  private int server_err_code;

  public RemoteStreamTask(Handler h, Context mContext) {
    mHandler = h;
  }

  @Override
  protected abstract BabyMonitorAuthentication doInBackground(String... params);

  protected void onCancelled() {
    // // Log.d(TAG, "Cancelled.");
    mHandler.dispatchMessage(Message.obtain(mHandler, MSG_VIEW_CAM_CANCELED));
  }

  public static final String GA_VIEW_CAMERA_CATEGORY = "View Remote Camera";

  protected abstract void onPostExecute(BabyMonitorAuthentication result);
}




