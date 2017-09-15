package com.hubble;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.hubble.framework.service.p2p.IP2pListener;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.framework.service.p2p.P2pStreamTask;
import com.hubble.registration.interfaces.IWifiScanUpdater;
import com.hubble.registration.models.CamChannel;
import com.hubble.registration.models.LegacyCamProfile;
import com.hubble.registration.tasks.RemoteStreamTask;
import com.hubble.registration.tasks.WifiScan;
//import com.hubble.ui.patrol.PatrolVideoFragment;
import com.msc3.CountDownTimer;
import com.msc3.ITimerUpdater;
import com.msc3.RtmpStream2Task;

import java.util.Timer;
import java.util.TimerTask;

import base.hubble.constants.Streaming;

public class VideoPlaybackTasks {
  private static final String TAG = "VideoPlaybackTasks";
  //private LocalScanForCameras scan_task;
  private boolean mHasHandler = false;
  private Handler mHandler = null;
  private Timer remoteVideoTimer = null;
  private Timer standByVideoTimer = null;
  private CountDownTimer ctimer = null;
  private CountDownTimer standBytimer = null;
  private Timer bufferingTimer = null;
  private WifiScan ws = null;
  private RemoteStreamTask viewUpnpCamTask;
  private P2pStreamTask mP2pStreamTask;
  private Thread countDownTimerThread;
  private Thread standByDownTimerThread;
  private CamChannel selected_channel;
  private String streamUrl;
  Context context;


  public void stopRemoteVideoTimer() {
    if (remoteVideoTimer != null) {
      remoteVideoTimer.cancel();
      remoteVideoTimer = null;
    }
  }

  public void initRemoteVideoTimer() {
    stopRemoteVideoTimer();
    remoteVideoTimer = new Timer();
  }

  public void scheduleRemoteVideoTimerTask(TimerTask task, long time) {
    remoteVideoTimer.schedule(task, time);
  }

  public void stopStandByVideoTimer()
  {
    if(standByVideoTimer != null)
    {
      standByVideoTimer.cancel();
      standByVideoTimer = null;
    }
  }

  public void initStandByVideoTimer()
  {
    stopStandByVideoTimer();
    standByVideoTimer = new Timer();
  }

  public void scheduleStandByVideoTimerTask(TimerTask task,long time)
  {
    standByVideoTimer.schedule(task,time);
  }

  public void stopBufferingTimer() {
    if (bufferingTimer != null) {
      bufferingTimer.cancel();
      bufferingTimer = null;
    }
  }

  public void initBufferingTimer() {
    stopBufferingTimer();
    bufferingTimer = new Timer();
  }

  public void scheduleBufferTimerTask(TimerTask task, long time) {
    if (bufferingTimer != null) {
      bufferingTimer.schedule(task, time);
    }
  }

  public void initStandByCountDownTimer(ITimerUpdater timerUpdater) {
    stopStandByCountDownTimer();
    standBytimer = new CountDownTimer(1, timerUpdater);
  }

  public void startStandByCountDownTimerThread() {
    standByDownTimerThread = new Thread(standBytimer);
    standByDownTimerThread.start();
  }

  public void stopStandByCountDownTimer() {
    if (standBytimer != null) {
      standBytimer.stop();
    }
  }

  public void initCountDownTimer(ITimerUpdater timerUpdater) {
    stopCountDownTimer();
    ctimer = new CountDownTimer(1, timerUpdater);
  }

  public void startCountDownTimerThread() {
    countDownTimerThread = new Thread(ctimer);
    countDownTimerThread.start();
  }



  private void stopRelayStreamTask() {
    if (viewUpnpCamTask != null) {
      viewUpnpCamTask.cancel(true);
    }
  }

  private void stopP2pStreamTask() {
    if (mP2pStreamTask != null) {
      mP2pStreamTask.cancel(true);
    }
  }

  public void stopLiveStreamingTasks() {
      stopRelayStreamTask();
      stopP2pStreamTask();
  }

  public void startRelayStreamTask(Activity activity,
                                   Handler.Callback callback,
                                   LegacyCamProfile selectedCamProfile,
                                   String regId,
                                   String apiKey) {
    selected_channel = new CamChannel();
    selected_channel.setCamProfile(selectedCamProfile);
//    viewUpnpCamTask = new RtmpStreamTask(getHandler(videoViewFragment), activity);
    viewUpnpCamTask = new RtmpStream2Task(getHandler(callback), activity);

    selected_channel.getCamProfile().setRemoteCommMode(Streaming.STREAM_MODE_HTTP_REMOTE);
    selected_channel.setCurrentViewSession(CamChannel.REMOTE_RELAY_VIEW);
    selected_channel.setViewReqState(viewUpnpCamTask);
    viewUpnpCamTask.executeOnExecutor(
        AsyncTask.THREAD_POOL_EXECUTOR,
        regId,
        apiKey,
        "BROWSER" /*TODO: should we look at using 'ANDROID'?*/
    );
  }


  /*public void startRelayStreamPatrolTask(Activity activity,
                                         PatrolVideoFragment patrolVideoFragment,
                                         LegacyCamProfile selectedCamProfile,
                                         String regId,
                                         String apiKey) {
    selected_channel = new CamChannel();
    selected_channel.setCamProfile(selectedCamProfile);
    viewUpnpCamTask = new RtmpStream2Task(new Handler(patrolVideoFragment), activity);
    selected_channel.getCamProfile().setRemoteCommMode(Streaming.STREAM_MODE_HTTP_REMOTE);
    selected_channel.setCurrentViewSession(CamChannel.REMOTE_RELAY_VIEW);
    selected_channel.setViewReqState(viewUpnpCamTask);
    viewUpnpCamTask.executeOnExecutor(
        AsyncTask.THREAD_POOL_EXECUTOR,
        regId,
        apiKey,
        "BROWSER" /*TODO: should we look at using 'ANDROID'?*/
    //);
  //}

  public void startP2pStreamTask(Activity activity,
                                 IP2pListener p2pListener,
                                 LegacyCamProfile selectedCamProfile,
                                 String regId,
                                 String apiKey,
                                 boolean usingMobileNetwork,
                                 boolean isSupportRelayP2p) {
    selected_channel = new CamChannel();
    selected_channel.setCamProfile(selectedCamProfile);
    mP2pStreamTask = new P2pStreamTask(activity.getApplicationContext(), p2pListener);
    selected_channel.getCamProfile().setRemoteCommMode(Streaming.STREAM_MODE_HTTP_REMOTE);
    selected_channel.setCurrentViewSession(CamChannel.REMOTE_RELAY_VIEW);
    Log.d(TAG, "startP2pStreamTask isInLocal? " + selectedCamProfile.isInLocal() + ", isSupportRelayP2p? " + isSupportRelayP2p);
    if (selectedCamProfile.isInLocal()) {
      mP2pStreamTask.executeOnExecutor(
              AsyncTask.THREAD_POOL_EXECUTOR,
              String.valueOf(isSupportRelayP2p),
              String.valueOf(P2pManager.P2P_SESSION_TYPE_LOCAL),
              selectedCamProfile.get_inetAddress().getHostAddress(),
              regId,
              String.valueOf(usingMobileNetwork)
      );
    } else {
      mP2pStreamTask.executeOnExecutor(
              AsyncTask.THREAD_POOL_EXECUTOR,
              String.valueOf(isSupportRelayP2p),
              String.valueOf(P2pManager.P2P_SESSION_TYPE_REMOTE),
              apiKey,
              regId,
              String.valueOf(usingMobileNetwork)
      );
    }
  }

  public void stopRunningWifiScanTask() {
    if (ws != null && ws.getStatus() == AsyncTask.Status.RUNNING) {
      ws.cancel(true);
    }
  }

  public void stopCountDownTimer() {
    if (ctimer != null) {
      ctimer.stop();
    }
  }

  public void stopAllTimers() {
    stopRemoteVideoTimer();
    stopBufferingTimer();
    stopCountDownTimer();
    stopStandByVideoTimer();
  }

  public void startWifiTask(Activity activity, IWifiScanUpdater scanUpdater) {
    ws = new WifiScan(activity, scanUpdater);
    ws.setSilence(true);
    ws.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Scan now");
  }

  private Handler getHandler(Handler.Callback callback) {
    if (mHandler == null) {
      mHandler = new Handler(callback);
    }
    return mHandler;
  }

  public String getStreamUrl() {
    if (selected_channel != null) {
      return selected_channel.getStreamUrl();
    } else {
      return null;
    }
  }

  public boolean setStreamingState() {
    if (selected_channel != null) {
      return selected_channel.setStreamingState();
    } else {
      return false;
    }
  }

  public int getStreamingState() {
    if (selected_channel != null) {
      return selected_channel.getState();
    } else {
      return -1;
    }
  }

  public void setStreamUrl(String streamUrl) {
    if (selected_channel != null) {
      selected_channel.setStreamUrl(streamUrl);
    }
  }
}
