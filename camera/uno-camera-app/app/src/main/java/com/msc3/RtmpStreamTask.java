package com.msc3;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hubble.analytics.GAEvent;
import com.hubble.analytics.GAEventAction;
import com.hubble.analytics.GoogleAnalyticsController;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.registration.models.BabyMonitorAuthentication;
import com.hubble.registration.tasks.RemoteStreamTask;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import base.hubble.meapi.Device;
import base.hubble.meapi.device.CreateSessionKeyResponse2;
import base.hubble.meapi.device.CreateSessionKeyResponse2.CreateSessionKeyResponseData2;

public class RtmpStreamTask extends RemoteStreamTask {

  private static final String TAG = "RtmpStreamTask";
  private String regId;
  private String userToken;
  private String clientType;

  protected Context mContext;

  private int server_status_code;
  private int server_error_code;
  private int deviceResponseCode;
  private String server_error_msg;
  private EventData eventData;

  public RtmpStreamTask(Handler mHandler, Context mContext) {
    super(mHandler, mContext);

    this.mContext = mContext;
    eventData = new EventData();
    server_error_code = -1;
    server_status_code = -1;
    deviceResponseCode = -1;
    server_error_msg = "";
  }

  @Override
  protected BabyMonitorAuthentication doInBackground(String... params) {
    BabyMonitorAuthentication bm_auth = null;

    regId = params[0];
    userToken = params[1];
    clientType = params[2];
    Log.d(TAG, "Sending create rtmp session...");
    try {
      CreateSessionKeyResponse2 session_info = Device.getSessionKey2(userToken, regId, clientType);
      if (session_info != null) {
        CreateSessionKeyResponseData2 session_data = session_info.getData();
        if (session_info.getStatus() == HttpURLConnection.HTTP_OK) {
          if (session_data != null) {
            String streamUrl = session_data.getUrl();
            //streamUrl = "rtmp://54.152.231.255:1935/camera/blinkhd.bc56279e8a24.stream";
            if (streamUrl != null) {
              // // Log.d(TAG, "Viewing " + streamUrl);
              final String url = streamUrl;
              Log.d(TAG, streamUrl);
              bm_auth = new BabyMonitorRelayAuthentication(null, "80", null, regId, null, streamUrl, 80, null, null);
            }
          }
        } else {
          server_status_code = session_info.getStatus();
          server_error_code = session_info.getCode();
          if (session_data != null) {
            CreateSessionKeyResponse2.CreateSessionDeviceResponse deviceResponse = session_data.getDevice_response();
            if (deviceResponse != null) {
              deviceResponseCode = deviceResponse.getDevice_response_code();
              Log.i(TAG, "Create rtmp session failed, device response code: " + deviceResponseCode);
            }
          }
          server_error_msg = session_info.getMessage();
        }
      }
    } catch (Exception e) {
      Log.e(TAG, Log.getStackTraceString(e));
      server_error_msg = "Timeout exception";
    }
    Log.d(TAG, "Sending create rtmp session...DONE");
    return bm_auth;
  }

  @Override
  protected void onPostExecute(BabyMonitorAuthentication result) {
    if (mHandler != null) {
      Message msg;
      if (result != null) {
        // // Log.d(TAG, "Got remote stream...");
        GoogleAnalyticsController.getInstance().trackEvent("Create_session pass");
        AnalyticsInterface.getInstance().trackEvent("Create_session_pass","Create_session_pass",eventData);
        msg = Message.obtain(mHandler, MSG_VIEW_CAM_SUCCESS, result);
      } else {
        // // Log.d(TAG, "Failed to view remote stream.");
        String message = "";
        String status = "";
        String code = "";
        if (server_error_msg.equals("Timeout Exception")) {
          message = String.valueOf(server_error_msg);
        } else {
          status = String.valueOf(server_status_code);
          code = String.valueOf(server_error_code);
          message = String.valueOf(server_error_msg);
        }
        List<GAEventAction> actions = new ArrayList<GAEventAction>();
        actions.add(new GAEventAction("status", status));
        actions.add(new GAEventAction("code", code));
        actions.add(new GAEventAction("message", message));
        GoogleAnalyticsController.getInstance().trackEvent(new GAEvent("Create_session failed", actions));
        AnalyticsInterface.getInstance().trackEvent("Create_session_failed","Create_session_failed",eventData);
        msg = Message.obtain(mHandler, MSG_VIEW_CAM_FALIED, server_status_code, deviceResponseCode);
      }
      mHandler.dispatchMessage(msg);
    }
  }

}
