package com.hubble.streaming;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.hubble.HubbleApplication;
import com.hubble.command.CameraCommandUtils;
import com.nxcomm.blinkhd.ui.Global;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import base.hubble.command.PublishCommandRequestBody;
import base.hubble.command.RemoteCommandRequest;
import base.hubble.meapi.Device;
import base.hubble.meapi.device.SendCommandResponse;

/**
 * Created by hoang on 10/28/15.
 */
public class HubbleSessionManager {

  private static final String TAG = HubbleApplication.TAG;

  public static final int CONNECTION_MODE_UNKNOWN = -1;
  public static final int CONNECTION_MODE_RTSP_LOCAL = 0;
  public static final int CONNECTION_MODE_RTMP_REMOTE = 1;
  public static final int CONNECTION_MODE_P2P_LOCAL = 2;
  public static final int CONNECTION_MODE_P2P_REMOTE = 3;
  public static final int CONNECTION_MODE_P2P_RELAY = 4;

  private static final int VIDEO_BITRATE_MIN = 300;
  private static final int VIDEO_BITRATE_MAX = 1000;
  private static final int VIDEO_BITRATE_DEFAULT_ORBIT = 300;
  private static final int VIDEO_BITRATE_DEFAULT = 600;
  private static final int VIDEO_BITRATE_TEMP = 200;
  private static final int VIDEO_BITRATE_ANALYZING_DURATION = 20;
  private static final int VIDEO_BITRATE_STEP = 50;
  private int mBitRateAnalyzingCounter;
  private long mAccumulateBitRate;

  private static final String SET_VIDEO_BITRATE_CMD = "action=command&command=set_video_bitrate&value=%d";

  private static HubbleSessionManager sSessionManager = null;
  private int mCurrConnectionMode;
  private int mDefaultVideoBitrate;
  private int mCurrVideoBitrate;
  private String mCurrentRegId;
  private static Context sContext = HubbleApplication.AppContext;
  private static final Gson sGson = new Gson();
  private String mApiKey;


  public static synchronized HubbleSessionManager getInstance() {
    if (sSessionManager == null) {
      sSessionManager = new HubbleSessionManager();
    }
    return sSessionManager;
  };

  private HubbleSessionManager() {
    mApiKey = Global.getApiKey(sContext);
    initBitRate();
    reset();
  }

  /**
   * Set current camrea registration id, this is required for sending video bitrate command
   * to camera.
   * @param currRegId The registration id of current camera.
   */
  public void setCurrentCameraRegId(String currRegId) {
    mCurrentRegId = currRegId;
  }


  public void initBitRate() {
    mDefaultVideoBitrate = VIDEO_BITRATE_DEFAULT;
    mCurrentRegId = "";
  }

  public void initOrbitBitRate()
  {
    mDefaultVideoBitrate = VIDEO_BITRATE_DEFAULT_ORBIT;
    mCurrentRegId = "";
  }

  /**
   * Reset video bitrate every view session.
   * Note: For RTMP session, app should call adjustToTempVideoBitrate() method later.
   */
  public void reset() {
    mCurrVideoBitrate = mDefaultVideoBitrate;
    mCurrConnectionMode = CONNECTION_MODE_UNKNOWN;

    mAccumulateBitRate = 0;
    mBitRateAnalyzingCounter = 0;
  }

  /**
   * App has to update current connection mode every view session so that
   * the video bitrate can be properly handled.
   * @param newConnectionMode The new connection mode.
   */
  public void updateCurrentConnectionMode(int newConnectionMode) {
    mCurrConnectionMode = newConnectionMode;
  }

  /**
   * Make video bitrate lower to get first video frame faster.
   * Currently, this logic is just applied to connection mode RTMP.
   */
  public void adjustToTempVideoBitrate() {
    if (mCurrConnectionMode == CONNECTION_MODE_RTMP_REMOTE) {
      mCurrVideoBitrate = VIDEO_BITRATE_TEMP;
      sendAdjustBitrateCmdAsync();
    }
  }

  /**
   * Adjust video bitrate after received first video frame.
   * Currently, this logic is just applied to connection mode RTMP
   */
  public void adjustToDefaultVideoBitrate() {
    if (mCurrConnectionMode == CONNECTION_MODE_RTMP_REMOTE) {
      mCurrVideoBitrate = mDefaultVideoBitrate;
      sendAdjustBitrateCmdAsync();
    }
  }

  /**
   * Change current default video bitrate to an expected value.
   */
  public void decreaseDefaultVideoBitrate() {
    if (mCurrConnectionMode == CONNECTION_MODE_RTMP_REMOTE) {
      if (mDefaultVideoBitrate > VIDEO_BITRATE_MIN) {
        mDefaultVideoBitrate -= VIDEO_BITRATE_STEP;
        Log.i(TAG, "Rtmp disconnected, decrease video bitrate");
      }
      // sendAdjustBitrateCmdAsync();
    }
  }

  /**
   * Increase default video bitrate by VIDEO_BITRATE_STEP.
   */
  public void increaseDefaultVideoBitRate() {
    if (mCurrConnectionMode == CONNECTION_MODE_RTMP_REMOTE) {
      if (mDefaultVideoBitrate < VIDEO_BITRATE_MAX) {
        mDefaultVideoBitrate += VIDEO_BITRATE_STEP;
        Log.i(TAG, "Increase default video bitrate to: " + mDefaultVideoBitrate);
//        sendAdjustBitrateCmdAsync();
      }
    }
  }

  /**
   * Get current video bitrate of view session.
   * @return The current video bitrate of view session.
   */
  public int getCurrentVideoBitrate() {
    return mCurrVideoBitrate;
  }

  /**
   * Update current video bitrate value to camera.
   * This is asynchronous method, so app can call it on main thread.
   */
  private void sendAdjustBitrateCmdAsync() {
    Runnable runn = new Runnable() {
      @Override
      public void run() {
        sendAdjustBitrateCmd();
      }
    };
    Thread worker = new Thread(runn);
    worker.start();
  }

  /**
   * Update current video bitrate value to camera.
   * This is synchronous method, so app should call it on background thread.
   */
  private String sendAdjustBitrateCmd() {
    String res = "";
    PublishCommandRequestBody.Builder builder = new PublishCommandRequestBody.Builder();
    builder.setCommand("set_video_bitrate")
            .setValue("" + mCurrVideoBitrate);
    PublishCommandRequestBody requestBody = builder.create();
    RemoteCommandRequest request = new RemoteCommandRequest();
    request.setApiKey(Global.getApiKey(sContext));
    request.setRegistrationId(mCurrentRegId);
    request.setPublishCommandRequestBody(requestBody);
    res = CameraCommandUtils.sendRemoteCommand(request);
    Log.i(TAG, "Adjust video bitrate res: " + res);
    return res;
  }



}
