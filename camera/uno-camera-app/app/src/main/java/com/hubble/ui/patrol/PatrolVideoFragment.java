/*package com.hubble.ui.patrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.blinkhd.playback.LiveFragment;
import com.crittercism.app.Crittercism;
import com.discovery.ScanProfile;
import com.google.common.util.concurrent.SettableFuture;
import com.hubble.HubbleApplication;
import com.hubble.Patrolling;
import com.hubble.PatrollingActorJava;
import com.hubble.SecureConfig;
import com.hubble.VideoPlaybackTasks;
import com.hubble.analytics.GoogleAnalyticsController;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.devcomm.impl.hubble.P2pCommunicationManager;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.framework.service.p2p.IP2pListener;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.helpers.AsyncPackage;
import com.hubble.model.MobileSupervisor;
import com.hubble.model.VideoBandwidthSupervisor;
import com.hubble.model.WifiSupervisor;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.interfaces.ICameraScanner;
import com.hubble.registration.interfaces.IWifiScanUpdater;
import com.hubble.registration.models.BabyMonitorAuthentication;
import com.hubble.registration.models.LegacyCamProfile;
import com.hubble.registration.tasks.ConnectToNetworkTask;
import com.hubble.registration.tasks.RemoteStreamTask;
import com.hubble.registration.ui.CommonDialogListener;
import com.hubble.streaming.HubbleSessionManager;
import com.hubble.ui.DebugFragment;
import com.hubble.ui.LiveMenuItem;
import com.hubble.ui.PlaybackMenuAdapterJava;
import com.hubble.util.P2pSettingUtils;
import com.blinkhd.R;
import com.media.ffmpeg.FFMpegPlayer;
import com.media.ffmpeg.android.FFMpegMovieViewAndroid;
import com.melnykov.fab.FloatingActionButton;
import com.msc3.ITimerUpdater;
import com.msc3.Streamer;
import com.msc3.update.CheckVersionFW;
import com.nxcomm.blinkhd.ui.ILiveFragmentCallback;
import com.nxcomm.blinkhd.ui.LiveCameraActionButtonListener;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.nxcomm.blinkhd.ui.dialog.VideoTimeoutDialog;
import com.nxcomm.jstun_android.P2pClient;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.TimerTask;

import base.hubble.PublicDefineGlob;
import base.hubble.constants.Streaming;

public class
    PatrolVideoFragment extends Fragment implements IP2pListener, Callback, ICameraScanner, LiveCameraActionButtonListener, ILiveFragmentCallback, WifiSupervisor.WifiSupervisorInterface {
  private Device selectedDevice;
  private PatrollingActorJava mPatrollingActor;

  public PatrolVideoFragment() {
    // Required empty public constructor
  }

  private static final String SHOULD_VIDEO_TIMEOUT = "should_video_view_timeout";

  private static final String TAG = "PatrolVideoFragment";
  private static final long VIDEO_TIMEOUT = 15 * 60 * 1000;
  private static final long MAX_BUFFERING_TIME = 15 * 1000;
  private static final long MAX_LONG_CLICK_DURATION = 1000;
  private static final int MAX_CLICK_DISTANCE = 15;

  private static final int DEFAULT_VIDEO_BITRATE = PublicDefineGlob.VIDEO_BITRATE_800;

  private View view;
  private boolean initialized = false;
  private Runnable onCreateViewCompleteRunnable;
  private LiveFragment liveFragment;
  private static SecureConfig settings = HubbleApplication.AppConfig;

  public boolean isOrbitP2PEnabled()
  {
    if(selectedDevice != null && selectedDevice.getProfile().isStandBySupported())
    {
      return settings.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);

    }
    return true;
  }
  @Override
  public void onP2pSessionOpenSucceeded(P2pClient p2pClient) {
    if (p2pClient != null) {
      Log.i(TAG, "onP2pSessionOpenSucceeded, regId? " + p2pClient.getRegistrationId());
      if (!sa.userWantToCancel && !da.activity_has_stopped) {
        sa.p2pClients = new P2pClient[]{p2pClient};
        sa.filePath = "";
        if (isMobileDataConnected() == true) {
          if (selectedDevice != null && selectedDevice.getProfile() != null && isOrbitP2PEnabled() &&
              selectedDevice.getProfile().canUseP2p() && selectedDevice.getProfile().canUseP2pRelay()) {
            openFFmpegFragment();
          } else {
            if (p2pClient.getUpnp() == 0 && p2pClient.getMobileNatType() == P2pManager.P2P_NAT_TYPE_UNSUPPORTED) {
              Log.i(TAG, "Upnp: " + p2pClient.getUpnp() + ", mobile nat type: " + p2pClient.getMobileNatType() +
                  ", switch to relay rtmp");
              prepareToViewCameraViaRelay();
            } else {
              openFFmpegFragment();
            }
          }
        } else {
          openFFmpegFragment();
        }
      } else {
        Log.d(TAG, "onP2pSessionOpenSucceeded, fragment has stopped -> do nothing");
      }
    } else {
      Log.d(TAG, "onP2pSessionOpenSucceeded, p2pClients is empty -> do nothing");
    }
  }

  public boolean isLocalStreaming() {
    boolean isInLocalMode = false;
    if (da.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_RTSP_LOCAL ||
        P2pManager.getInstance().isP2pLocalStreaming()) {
      isInLocalMode = true;
    }
    return isInLocalMode;
  }

  @Override
  public void onP2pSessionOpenFailed() {
    if (!sa.userWantToCancel && !da.activity_has_stopped) {
      Log.d(TAG, "Create p2p session failed, isRtmpStreamingEnabled? " + P2pManager.getInstance().isRtmpStreamingEnabled());
      boolean isInLocal = isLocalStreaming();
      if (P2pManager.getInstance().isRtmpStreamingEnabled()) {
        if (isInLocal) {
          prepareToViewCameraViaRtsp();
        } else {
          prepareToViewCameraViaRelay();
        }
      } else {
        Log.d(TAG, "RTMP streaming is disabled -> continue to try p2p");
        prepareToViewCameraViaP2p(isInLocal);
      }
    } else {
      Log.d(TAG, "Create p2p session failed, fragment has stopped -> do nothing");
    }
  }

  @Override
  public void onP2pSessionClosed(boolean isSucceeded) {

  }

  private void prepareToViewCameraViaRtsp() {
    sa.viewRelayRtmp = false;
    sa.viewP2p = false;
    da.currentConnectionMode = HubbleSessionManager.CONNECTION_MODE_RTSP_LOCAL;
    HubbleSessionManager.getInstance().updateCurrentConnectionMode(da.currentConnectionMode);
    HubbleSessionManager.getInstance().reset();
    da.device_ip = selectedDevice.getProfile().getDeviceLocation().getLocalIp();
    int devicePort = -1;
    try {
      devicePort = Integer.parseInt(selectedDevice.getProfile().getDeviceLocation().getLocalPort1());
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    da.device_port = devicePort;
    String deviceIPOverride = getIPOverrideSetting(mActivity);
    if (!deviceIPOverride.equals("")) {
      sa.filePath = String.format("rtsp://user:pass@%s:%d/blinkhd", deviceIPOverride, 6667);
    } else {
      sa.filePath = String.format("rtsp://user:pass@%s:%d/blinkhd", da.device_ip, 6667);
    }
    openFFmpegFragment();
  }

  public class DeviceAttributes {
    int video_width = 0;
    int video_height = 0;
    int currentConnectionMode;
    boolean activity_has_stopped;
    boolean is_upgrading = false;
    int current_bitrate_value = PublicDefineGlob.VIDEO_BITRATE_200; // PublicDefineGlob.VIDEO_BITRATE_600;
    String device_ip;
    int device_port;
  }

  private class SessionAttributes {
    long create_session_start_time = System.currentTimeMillis();
    long open_stream_start_time = System.currentTimeMillis();
    long connecting_start_time = System.currentTimeMillis();
    long view_session_start_time = -1;
    int default_screen_height;
    int default_screen_width;
    int default_width;
    int default_height;
    float scale = 1f;
    float lastScaleFactor = 1f;
    float ratio = 0;
    float pressedX;
    float pressedY;
    float mx, my;
    int remote_reconnect_times = 0;
    String string_currentSSID = "string_currentSSID";
    boolean isPortrait = true;
    boolean userWantToCancel;
    boolean isVideoTimeout = false;
    boolean viewRelayRtmp = false;
    boolean isDebugEnabled = false;
    boolean isAdaptiveBitrateEnabled = false;
    String filePath;
    BabyMonitorAuthentication bm_session_auth;
    int p2pTries = 0;
    boolean viewP2p = false;
    P2pClient[] p2pClients = null;
  }

  private Activity mActivity;
  private boolean mIsLocal = false;
  private DeviceAttributes da = new DeviceAttributes();
  private SessionAttributes sa = new SessionAttributes();
  private VideoPlaybackTasks videoPlaybackTasks = new VideoPlaybackTasks();

  private FrameLayout streamView = null;
  private long pressStartTime = -1;
  private ScaleGestureDetector mScaleDetector;

  private ActionBar mActionBar;
  private boolean mIsFirstTime = true;
  private RelativeLayout topViewHolder;
  private ProgressBar mProgressBar;
  private FloatingActionButton btnPrevious;
  private FloatingActionButton btnPlayPause;
  private FloatingActionButton btnNext;
  private Button btnStop;
  private SettableFuture<Object> mDevicePromise = null;

  private List<Device> patrollingCameras;
  private ProgressBar videoLoadingBar;
  private EventData eventData;

  @Override
  public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // // Log.d(TAG, "onCreateView");
    eventData = new EventData();
    view = inflater.inflate(R.layout.fragment_patrol_view, container, false);

    if (!initialized) {
      initialize();
    }

    if (onCreateViewCompleteRunnable != null) {
      this.onCreateViewCompleteRunnable.run();
      this.onCreateViewCompleteRunnable = null;
    }

    da.activity_has_stopped = false;
    sa.userWantToCancel = false;
    if (!sa.isVideoTimeout && !da.is_upgrading) {
      doOrientationLayout();
    }
    return view;
  }

  public void setOnCreateViewCompleteRunnable(Runnable r) {
    this.onCreateViewCompleteRunnable = r;
  }

  @Override
  public void onAttach(Activity activity) {
    // // Log.d(TAG, "onAttach");
    super.onAttach(activity);
    mActivity = activity;
  }

  @Override
  public void onPause() {
    mActivity.unregisterReceiver(WifiSupervisor.getInstance().getBroadcastReceiver());
    MobileSupervisor.getInstance().removeDelegate(); // Delegate gets added when wifi connection fails only in onUnexpectedStreamEnd()
    clearKeepScreenOnFlags();
    if (getWindow() != null) {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    ((ActionBarActivity) this.mActivity).getSupportActionBar().show();
    if (mPatrollingActor != null) {
      mPatrollingActor.pause();
      btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
      paused = true;
    }
    if (liveFragment != null) {
      Log.i(TAG, "Stop live stream");
      liveFragment.stopStreamingBlocked();
    }
    stopBackgroundTasks();
    super.onPause();
  }

  private void initialize() {
    // // Log.d(TAG, "initialize");
    liveFragment = new LiveFragment();
    liveFragment.setLiveCameraActionButtonListener(this);
    liveFragment.setVideoViewCallback(this);
    liveFragment.setLiveFragmentListener(this);

    topViewHolder = (RelativeLayout) findViewById(R.id.patrolFragmentTopView);
    streamView = (FrameLayout) findViewById(R.id.patrolFragmentVideoView);
    mProgressBar = (ProgressBar) findViewById(R.id.patrolFragmentProgressBar);
    videoLoadingBar = (ProgressBar) findViewById(R.id.patrolFragmentIVLoading);

    btnPrevious = (FloatingActionButton) findViewById(R.id.patrolFragmentBackButton);
    btnPlayPause = (FloatingActionButton) findViewById(R.id.patrolFragmentPlayPauseButton);
    btnNext = (FloatingActionButton) findViewById(R.id.patrolFragmentNextButton);

    btnStop = (Button) view.findViewById(R.id.patrolFragment_stopButton);

    btnStop.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        ((MainActivity) mActivity).switchToCameraFragment(selectedDevice);
      }
    });

    btnPrevious.setOnClickListener(patrolButtonClickListener);
    btnNext.setOnClickListener(patrolButtonClickListener);

    btnPlayPause.setClickable(true);
    btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
    btnPlayPause.setOnClickListener(patrolButtonClickListener);


    /* added to force url */

    /*WifiManager w = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
    String curr_ssid = w.getConnectionInfo().getSSID();
    settings.putString(sa.string_currentSSID, curr_ssid);
    streamView.setBackgroundResource(R.color.black);

    mActionBar = ((ActionBarActivity) this.mActivity).getSupportActionBar();
    mActionBar.setDisplayHomeAsUpEnabled(true);
    mActionBar.setHomeButtonEnabled(true);
    mActionBar.setTitle(mActivity.getTitle());

    initialized = true;
  }

  public boolean isP2pStreaming() {
    boolean isInP2pMode = true;
    if (da.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_RTMP_REMOTE ||
        da.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_RTSP_LOCAL) {
      isInP2pMode = false;
    }
    return isInP2pMode;
  }

  private void stopBackgroundTasks() {
    if (videoPlaybackTasks != null) {
      Log.i(TAG, "Stop background player tasks");
      videoPlaybackTasks.stopAllTimers();
      videoPlaybackTasks.stopCountDownTimer();
      videoPlaybackTasks.stopRunningWifiScanTask();
      videoPlaybackTasks.stopLiveStreamingTasks();
      if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled()) {
        P2pCommunicationManager.getInstance().updateP2pCommHandler(null);

        // Switch camera to p2p passive mode if needed
        if (isP2pStreaming() == true) {
          if (selectedDevice != null && selectedDevice.getProfile() != null && isOrbitP2PEnabled() &&
              selectedDevice.getProfile().canUseP2p() && selectedDevice.getProfile().canUseP2pRelay()) {
//            final boolean isExistInP2pList = SelectedP2pClients.doesClientExist(selectedDevice.getProfile().getRegistrationId());
//            if (isExistInP2pList) {
//            /*
//             * 20160107: HOANG:
//             * New p2p cameras: switching to p2p passive mode, preparing for getting jpeg data here.
//             * Still keep p2p session.
//             * 20160229: app will switch to preview mode in CameraListFragment, so don't need to
//             * set here.
//             */
//              //P2pManager.getInstance().switchToPreviewModeAsync();
//            } else {
//              // If camera registration id doesn't exist in preview list, destroy it
//              P2pManager.getInstance().destroyP2pSession();
//            }
            /*P2pManager.getInstance().destroyP2pSession();
          } else {
            /*
             * 20160107: HOANG:
             * Old p2p cameras: Close & destroy current p2p session.
             */
            /*P2pManager.getInstance().destroyP2pSession();
          }
        }
      }
      videoPlaybackTasks.stopRunningWifiScanTask();
    }
  }

  private void showCamera(Device showMeNow) {
    if (selectedDevice != null) {
      stopBackgroundTasks();
    }
    if (mActivity != null) {
      if (liveFragment != null) {
        Log.i(TAG, "Stop live stream");
        liveFragment.stopStreamingBlocked();
      }
      Toast.makeText(mActivity.getApplicationContext(), mActivity.getApplicationContext().getString(R.string.loading_simple) + " " + showMeNow.getProfile().getName(), Toast.LENGTH_SHORT).show();
      setCamera(mActivity.getApplicationContext(), showMeNow);
      viewCamera();
      if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        if (liveFragment != null) {
          liveFragment.goToFullScreenImmediately();
        }
      }

      startBufferTimeoutTimer();
    }
  }

  public boolean paused = false;

  public void startPatrolling(List<Device> patrolCameras) {
    int patrolDelaySeconds = Patrolling.getPatrollingDelay(mActivity);

    patrollingCameras = patrolCameras;
    mProgressBar.setMax(patrolDelaySeconds * 10);
    mProgressBar.setProgress(0);
    mPatrollingActor = new PatrollingActorJava(patrolCameras, patrolDelaySeconds) {
      @Override
      public void onUpdateProgress(long currentMillis) {
        mProgressBar.setProgress((int) (currentMillis / 100));
      }

      @Override
      public void onNextDevice(@NotNull Device device, @NotNull SettableFuture<Object> promise) {
        showCamera(device);
        mProgressBar.setProgress(0);
        mDevicePromise = promise;
      }

      @Override
      public void onPreviousDevice(@NotNull Device device, @NotNull SettableFuture<Object> promise) {
        showCamera(device);
        mProgressBar.setProgress(0);
        mDevicePromise = promise;
      }

      @Override
      public void onPause() {
        Toast.makeText(mActivity.getApplicationContext(), getString(R.string.pausing_patrol), Toast.LENGTH_SHORT).show();
        btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
      }

      @Override
      public void onResume() {
        Toast.makeText(mActivity.getApplicationContext(), getString(R.string.continuing_patrol), Toast.LENGTH_SHORT).show();
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
      }

      @Override
      public void onStartPatrolling(@NotNull Device device) {
        showCamera(device);
      }
    };

    mPatrollingActor.start();
  }

  private Object getSystemService(String service) {
    return mActivity.getSystemService(service);
  }

  private View findViewById(int id) {
    return view.findViewById(id);
  }

  private int unexpectedStopRetries = 0;

  @Override
  public void onResume() {
    super.onResume();
    setupWifiBroadcastReceiver();

    if (patrollingCameras == null) {
      ((MainActivity) mActivity).switchToDeviceList();
    }

    MobileSupervisor.getInstance().setDelegate(new MobileSupervisor.Interface() {
      @Override
      public void onMobileDataConnected() {
        if (!isWifiConnected()) {
          buildMobileDataConnectedDialog();
        }
      }
    });
  }

  private void setupWifiBroadcastReceiver() {
    WifiSupervisor.getInstance().setListener(this);
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
    intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    mActivity.registerReceiver(WifiSupervisor.getInstance().getBroadcastReceiver(), intentFilter);
  }

  @Override
  public void onWifiConnected() {
    isCameraInSameNetwork(); // Need to set a couple variables
    showSpinner();
    stopLiveFragmentStreaming();
    switchToLiveFragment();
    onExpectedStreamEnd();
    hideSpinner();
  }

  @Override
  public void onWifiDisconnected() {
    buildMobileDataConnectedDialog();
  }

  private void onExpectedStreamEnd() {
    checkToShowTimelineAndVideoView();
    if (isCameraInSameNetwork()) {
      prepareToViewCameraLocally();
    } else {
      prepareToViewCameraViaRelay();
    }
  }

  private boolean checkIfDeviceIsMobileCapable() {
    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

    if (networkInfo == null) {
      return false;
    } else {
      return true;
    }
  }

  private void buildMobileDataConnectedDialog() {
    if (checkIfDeviceIsMobileCapable()) {
      if (!settings.getBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, false)) {
        try {
          AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
          LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          LinearLayout streamingAskLayout = (LinearLayout) inflater.inflate(R.layout.bb_dont_ask_me_again, null);
          final CheckBox dontAskAgain = (CheckBox) streamingAskLayout.findViewById(R.id.skip);
          Spanned msg = Html.fromHtml("<big>" + mActivity.getResources().getString(R.string.mobile_data_3g_is_enabled_continue_to_connect_may_incur_air_time_charge_do_you_want_to_proceed_) + "</big>");
          builder.setMessage(msg)
              .setView(streamingAskLayout)
              .setCancelable(true)
              .setPositiveButton(mActivity.getResources().getString(R.string.Proceed), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      if (dontAskAgain.isChecked()) {
                        settings.putBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, true);
                        settings.putBoolean(PublicDefineGlob.PREFS_SHOULD_TURN_ON_WIFI, false);
                      }
                      isCameraInSameNetwork();
                      resolveRemoteStream(true);
                      dialog.dismiss();
                    }
                  }
              ).setNegativeButton(mActivity.getResources().getString(R.string.No), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  if (dontAskAgain.isChecked()) {
                    settings.putBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, true);
                    settings.putBoolean(PublicDefineGlob.PREFS_SHOULD_TURN_ON_WIFI, true);
                  }
                  resolveRemoteStream(false);
                  dialog.dismiss();
                }
              }
          ).show();
        } catch (Exception e) {
          resolveRemoteStream(false);
        }
      } else {
        resolveRemoteStream(true);
      }
    } else {
      resolveRemoteStream(false);
    }
  }

  private void resolveRemoteStream(boolean shouldConnect) {
    if (shouldConnect) {
      remoteVideoHasStopped(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY);
    } else {
      Toast.makeText(mActivity, getString(R.string.unable_to_continue_streaming), Toast.LENGTH_SHORT).show();
    }
  }

  private Window getWindow() {
    return mActivity.getWindow();
  }

  @Override
  public void onConfigurationChanged(@NotNull Configuration newConfig) {
    // // Log.d(TAG, "onConfigurationChanged()");
    super.onConfigurationChanged(newConfig);

    Log.i(TAG, " is update earlier tab again");
    // Prevent manipulation of fragments before this view has been initialized.
    if (initialized) {
      doOrientationLayout();
    }
  }

  private void doOrientationLayout() {
    View fragmentHolder = findViewById(R.id.patrolFragmentBottomView);
    RelativeLayout.LayoutParams video_params, controls_params;
    topViewHolder = (RelativeLayout) findViewById(R.id.patrolFragmentTopView);
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      topViewHolder.setVisibility(View.VISIBLE);
      video_params = setupLandscapeVideoLayout();
      controls_params = setupLandscapeControlLayout(fragmentHolder);
      mActionBar.hide();
    } else {
      if (selectedDevice == null || (!selectedDevice.getProfile().isAvailable())) {
        topViewHolder.setVisibility(View.GONE);
      }
      video_params = setupPortraitVideoLayout();
      controls_params = setupPortraitControlLayout(fragmentHolder);
      mActionBar.show();
    }

    if (topViewHolder != null) {
      topViewHolder.setLayoutParams(video_params);
      recalcDefaultScreenSize();
      resizeFFmpegView(1f);
    }

    if (fragmentHolder != null) {
      fragmentHolder.setLayoutParams(controls_params);
    }
  }

  private RelativeLayout.LayoutParams setupPortraitControlLayout(View holder) {
    RelativeLayout.LayoutParams controls_params;
    controls_params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    controls_params.addRule(RelativeLayout.BELOW, R.id.patrolFragmentTopView);
    controls_params.addRule(RelativeLayout.CENTER_VERTICAL);

    LinearLayout controlHolder = (LinearLayout) (holder.findViewById(R.id.patrolFragmentControlHolder));
    FrameLayout.LayoutParams mParams = (FrameLayout.LayoutParams) controlHolder.getLayoutParams();
    mParams.gravity = Gravity.CENTER;
    controlHolder.setLayoutParams(mParams);
    btnStop.setVisibility(View.VISIBLE);

    ((LinearLayout) (holder.findViewById(R.id.layoutPrev))).setGravity(Gravity.CENTER);
    ((LinearLayout) (holder.findViewById(R.id.layoutPlay))).setGravity(Gravity.CENTER);
    ((LinearLayout) (holder.findViewById(R.id.layoutNext))).setGravity(Gravity.CENTER);

    return controls_params;
  }

  private RelativeLayout.LayoutParams setupPortraitVideoLayout() {
    Log.i(TAG, "Current Orientation is Portrait");
    // detect large screen height > 480dp
    Display display = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    DisplayMetrics metrics = new DisplayMetrics();
    display.getMetrics(metrics);
    display.getSize(size);

    float portrait_height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
    if (size.x > 480 * metrics.density) {
      portrait_height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 360, getResources().getDisplayMetrics());
    }
    sa.isPortrait = true;

    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    RelativeLayout.LayoutParams video_params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) portrait_height);

    DisplayMetrics displaymetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    int height = displaymetrics.heightPixels;
    int width = displaymetrics.widthPixels;
    if (width < height) {
      video_params.width = width;
      video_params.height = (int) (video_params.width / 1.777778f);
      Log.i(TAG, "SIZE " + video_params.width + " x " + video_params.height);
    }
    return video_params;
  }

  private RelativeLayout.LayoutParams setupLandscapeControlLayout(View holder) {
    RelativeLayout.LayoutParams controls_params;
    controls_params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    controls_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    LinearLayout controlHolder = (LinearLayout) (holder.findViewById(R.id.patrolFragmentControlHolder));
    FrameLayout.LayoutParams mParams = (FrameLayout.LayoutParams) controlHolder.getLayoutParams();
    mParams.gravity = Gravity.BOTTOM;
    controlHolder.setLayoutParams(mParams);
    btnStop.setVisibility(View.INVISIBLE);

    ((LinearLayout) (holder.findViewById(R.id.layoutPrev))).setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    ((LinearLayout) (holder.findViewById(R.id.layoutPlay))).setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    ((LinearLayout) (holder.findViewById(R.id.layoutNext))).setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

    return controls_params;
  }

  private RelativeLayout.LayoutParams setupLandscapeVideoLayout() {
    Log.i(TAG, "Current Orientation is Landscape");
    sa.isPortrait = false;
    RelativeLayout.LayoutParams video_params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    video_params.addRule(RelativeLayout.CENTER_IN_PARENT);
    // make FULL Screen
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    return video_params;
  }

  private WindowManager getWindowManager() {
    return mActivity.getWindowManager();
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    if (selectedDevice != null) {
      if (PublicDefine.isSharedCam(
          PublicDefine.getMacFromRegId(selectedDevice.getProfile().getRegistrationId())
      )) {
        menu.clear();
      }
    }
  }

  @Override
  public void onDestroyView() {
    // // Log.d("VideoViewFragment", "onDestroyView()");
    initialized = false;
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onStop() {
    // // Log.d(TAG, "onStop");
    sa.userWantToCancel = true;
    da.activity_has_stopped = true;
    sa.remote_reconnect_times = 0;

    stopAllThread();

    clearKeepScreenOnFlags();
    videoPlaybackTasks.stopAllTimers();
    stopCountDownTimer();

    videoPlaybackTasks.stopLiveStreamingTasks();
    super.onStop();
  }

  @Override
  public void onStart() {
    Log.i(TAG, "inside onStart");
    super.onStart();

    da.activity_has_stopped = false;
    sa.userWantToCancel = false;

    // TODO: remove dep on global state
    if (!sa.isVideoTimeout && !da.is_upgrading) {
      scanAndViewCamera();
    }
  }

  @Override
  public boolean handleMessage(@NotNull Message msg) {
    if (mActivity != null && mActivity.getApplication() != null) {
      switch (msg.what) {
        case CheckVersionFW.PATCH_AVAILABLE:
          break;
        case CheckVersionFW.UPGRADE_DONE:
          showFirmwareUpgradeDoneDialog();
          break;
        case CheckVersionFW.UPGRADE_FAILED:
          showFirmwareUpgradeFailedDialog(msg);
          break;
        case FFMpegPlayer.MSG_MEDIA_STREAM_START_BUFFERING:
          //Log.d(TAG, "<-- handleMessage MSG_MEDIA_STREAM_START_BUFFERING");
          break;
        case FFMpegPlayer.MSG_MEDIA_STREAM_STOP_BUFFERING:
          //Log.d(TAG, "<-- handleMessage MSG_MEDIA_STREAM_STOP_BUFFERING");
          break;
        case FFMpegPlayer.MSG_MEDIA_STREAM_RECORDING_TIME:
          //Log.d(TAG, "<-- handleMessage MSG_MEDIA_STREAM_RECORDING_TIME");
          break;
        case FFMpegPlayer.MSG_MEDIA_STREAM_LOADING_VIDEO:
          // Log.d(TAG, "<-- handleMessage MSG_MEDIA_STREAM_LOADING_VIDEO");
          showSpinner();
          break;
        case FFMpegPlayer.MSG_MEDIA_STREAM_LOADING_VIDEO_CANCEL:
          // // Log.d(TAG, "<-- handleMessage MSG_MEDIA_STREAM_LOADING_VIDEO_CANCEL");
          hideSpinner();
          break;
        case Streamer.MSG_VIDEO_FPS:
          //Log.d(TAG, "<-- handleMessage MSG_VIDEO_FPS :" + msg.arg1);
          if (mIsFirstTime) {
            //da.current_bitrate_value = PublicDefineGlob.MODIFIED_VIDEO_BITRATE;
            //VideoBandwidthSupervisor.getInstance().setBitrate(PublicDefineGlob.MODIFIED_VIDEO_BITRATE);
            mIsFirstTime = false;
          }
          //VideoBandwidthSupervisor.getInstance().updateFPS(msg.arg1);
          //updateFPS(msg.arg1);
          break;
        case Streamer.MSG_VIDEO_SIZE_CHANGED:
          //Log.d(TAG, "<-- handleMessage MSG_VIDEO_SIZE_CHANGED");
          onVideoSizeChanged(msg);
          break;
        case Streamer.MSG_CAMERA_IS_NOT_AVAILABLE:
          // // Log.d(TAG, "<-- handleMessage MSG_CAMERA_IS_NOT_AVAILABLE");
          onCameraNotAvailable();
          break;
        case Streamer.MSG_VIDEO_STREAM_HAS_STOPPED:
          //Log.d(TAG, "<-- handleMessage MSG_VIDEO_STREAM_HAS_STOPPED:");
          onVideoStreamStopped();
          break;
        case Streamer.MSG_VIDEO_STREAM_HAS_STARTED:
          //Log.d(TAG, "<-- handleMessage MSG_VIDEO_STREAM_HAS_STARTED:");
          stopBufferTimeoutTimer();
          onVideoStreamStarted();
          break;
        case Streamer.MSG_RTSP_VIDEO_STREAM_BITRATE_BPS:
          updateDebugBitrateDisplay(msg.arg1);
          break;
        case Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY:
          //Log.d(TAG, "<-- handleMessage MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY");
          onUnexpectedStreamEnd();
          break;
        case RemoteStreamTask.MSG_VIEW_CAM_SUCCESS:
          //Log.d(TAG, "<-- handleMessage MSG_VIEW_CAM_SUCCESS");
          onViewCameraSuccess(msg);
          break;
        case RemoteStreamTask.MSG_VIEW_CAM_FALIED:
          //Log.d(TAG, "<-- handleMessage MSG_VIEW_CAM_FALIED");
          onViewCameraFailed(msg);
          break;
        default:
          break;
      }

    }
    return false;
  }

  private void onVideoStreamStarted() {
    // BlinkHDApplication.KissMetricsRecord("View Camera Successfull.");
    if (!da.activity_has_stopped && !sa.userWantToCancel) {
      sa.view_session_start_time = System.currentTimeMillis();
      if (liveFragment != null) {
        liveFragment.setViewSessionStartTime(sa.view_session_start_time);
      }

      if (!isLocalStreaming()) {
        GoogleAnalyticsController.getInstance().sendOpenWowzaStreamEvent(mActivity, true);
        videoPlaybackTasks.initRemoteVideoTimer();

        if (shouldVideoTimeout()) {
          videoPlaybackTasks.scheduleRemoteVideoTimerTask(new VideoTimeoutTask(), VIDEO_TIMEOUT);
        }

        if (sa.remote_reconnect_times == 0) {
          // first time start stream
          updateGlobalBitrateView(da.current_bitrate_value);
        }
      }

      recalcDefaultScreenSize();

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          resizeFFmpegView(1f);
          hideSpinner();
        }
      });
    }
  }

  private boolean shouldVideoTimeout() {
    return settings.getBoolean(SHOULD_VIDEO_TIMEOUT, true);
  }

  private void onVideoStreamStopped() {
    clearKeepScreenOnFlags();
    videoPlaybackTasks.stopRemoteVideoTimer();
  }

  private void onUnexpectedStreamEnd() {
    AsyncPackage.doInBackground(new Runnable() {
      @Override
      public void run() {
        GoogleAnalyticsController.getInstance().trackCameraInfo(getString(R.string.accessing_stream_event_unexcpected_end), selectedDevice);
        AnalyticsInterface.getInstance().trackEvent("UnexpectedEndOfStream","Unexpected_end_of_stream",eventData);
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            updateDebugBitrateDisplay(0); // reset bitrate info to 0 kbps
          }
        });

        if (!sa.userWantToCancel && !da.activity_has_stopped) {
          if (!da.is_upgrading) {
            videoPlaybackTasks.stopAllTimers();
            stopCountDownTimer();

            if (isCameraInSameNetwork()) {
              checkToShowTimelineAndVideoView();
              prepareToViewCameraLocally();
            } else {
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  if (isWifiConnected()) {
                    if (isCameraInSameNetwork()) {
                      videoHasStoppedUnexpectedly();
                    } else {
                      resolveRemoteStream(true);
                    }
                  } else {
                    resolveRemoteStream(false);
                  }
                }

                private void resolveRemoteStream(boolean shouldConnectViaWifi) {
                  if (shouldConnectViaWifi && !MobileSupervisor.getInstance().getIsMobileDataConnected()) {
                    remoteVideoHasStopped(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY);
                  } else if (MobileSupervisor.getInstance().getIsMobileDataConnected()) {
                    buildMobileDataConnectedDialog();
                  }
                }
              });
            }
          }
        }
      }
    });
  }

  private void onCameraNotAvailable() {
    if (selectedDevice != null) {
      AsyncPackage.doInBackground(new Runnable() {
        @Override
        public void run() {
          if (selectedDevice.isAvailableRemotely()) {
            mIsLocal = false;
            selectedDevice.setIsAvailableLocally(false);
            VideoBandwidthSupervisor.getInstance().useRecoverySettings();

            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                GoogleAnalyticsController.getInstance().sendOpenWowzaStreamEvent(mActivity, sa.viewRelayRtmp, false);
                Toast.makeText(mActivity.getApplicationContext(), getString(R.string.low_data_bandwidth_detected_trying_to_reconnect), Toast.LENGTH_LONG).show();
                cancelVideoTimeoutTask();
                prepareToViewCameraViaRelay();
              }
            });
          } else {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(mActivity, mActivity.getString(R.string.camera_not_available), Toast.LENGTH_SHORT).show();
                cancelVideoTimeoutTask();
                switchToCameraListFragment();
              }
            });
          }
        }
      });
    }
  }

  private void onVideoSizeChanged(Message msg) {
    da.video_width = msg.arg1;
    da.video_height = msg.arg2;
    if (da.video_width != 0 && da.video_height != 0) {
      sa.ratio = (float) da.video_width / da.video_height;
    }
    recalcDefaultScreenSize();

    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        resizeFFmpegView(1f);

        TextView txtRes = (TextView) findViewById(R.id.textResolution);
        if (txtRes != null) {
          String res_str = String.format("%dx%d", da.video_width, da.video_height);
          txtRes.setText(res_str);
        }

        TextView txtFps = (TextView) findViewById(R.id.textFrameRate);
        if (txtFps != null) {
          String fps_str;
          if (P2pSettingUtils.hasP2pFeature()) {
            fps_str = String.format("%s %d", getCurrentConnectionModeLetter(), 0);
          } else {
            fps_str = String.format("%s %d", isInLocalString(), 0);
          }
          txtFps.setText(fps_str);
        }

        if (liveFragment != null && (liveFragment.getMenuAdapter() != null)) {
          PlaybackMenuAdapterJava leftSideMenuImageAdapter = liveFragment.getMenuAdapter();
          if (leftSideMenuImageAdapter != null) {
            if (da.video_height >= 1080) {
              Log.d(TAG, "Got video resolution, is HD? true");
              leftSideMenuImageAdapter.setItemPressed(LiveMenuItem.HD, true);
            } else {
              Log.d(TAG, "Got video resolution, is HD? false");
              leftSideMenuImageAdapter.setItemPressed(LiveMenuItem.HD, false);
            }
          }
        }
      }
    });
  }

  private String isInLocalString() {
    return selectedDevice.isAvailableLocally() ? "L" : "R";
  }

  private int onViewCameraFailed(Message msg) {
    int status = msg.arg1;
    int code = msg.arg2;

    // // Log.d(TAG, "Can't get Session Key status: " + status + " code: " + code);

    sa.viewRelayRtmp = false;
    // cancelVideoStoppedReminder();

    if (sa.userWantToCancel && da.activity_has_stopped) {
      GoogleAnalyticsController.getInstance().sendCreateSessionEvent(mActivity, false);
      Toast.makeText(mActivity.getApplicationContext(), getString(R.string.camera_is_not_accessible), Toast.LENGTH_LONG).show();
      prepareToViewCameraViaRelay();
    }
    return status;
  }

  private void onViewCameraSuccess(Message msg) {
    if (!sa.userWantToCancel && !da.activity_has_stopped) {
      BabyMonitorAuthentication bm_auth = (BabyMonitorAuthentication) msg.obj;
      if (videoPlaybackTasks.setStreamingState()) {
        String stream_url = bm_auth.getStreamUrl();
        videoPlaybackTasks.setStreamUrl(stream_url);
        GoogleAnalyticsController.getInstance().sendCreateSessionEvent(mActivity, true);
        sa.open_stream_start_time = System.currentTimeMillis();
        setupRemoteCamera(bm_auth);
      }
    }
  }

  private void showSpinner() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (videoLoadingBar != null) {
          videoLoadingBar.setVisibility(View.VISIBLE);
        }
      }
    });
  }

  private void hideSpinner() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (videoLoadingBar != null) {
          videoLoadingBar.setVisibility(View.INVISIBLE);
        }

        if (mDevicePromise != null) {
          mDevicePromise.set(null);
          mDevicePromise = null;
        }
      }
    });
  }

  private void updateDebugBitrateDisplay(int data_in_bytes) {
    final int data_in_kb = data_in_bytes * 8 / 1000;
    if (liveFragment != null) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          TextView txtBitrate = (TextView) findViewById(R.id.textBitrate);
          if (txtBitrate != null && txtBitrate.getVisibility() == View.VISIBLE) {
            String bitrate_str = String.format("%d %s", data_in_kb, "kbps");
            txtBitrate.setText(bitrate_str);
          }
        }
      });
    }
  }

  private void updateFPS(final int fps) {
    if (liveFragment != null) {
      if (mActivity != null) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            TextView txtFps = (TextView) findViewById(R.id.textFrameRate);
            if (txtFps != null && txtFps.getVisibility() == View.VISIBLE) {
              String fps_str;
              if (P2pSettingUtils.hasP2pFeature()) {
                Log.i(TAG, "current connection mode: " + da.currentConnectionMode);
                fps_str = String.format("%s %d", getCurrentConnectionModeLetter(), fps);
              } else {
                Log.i(TAG, "isInLocalString " + isInLocalString());
                fps_str = String.format("%s %d", isInLocalString(), fps);
              }
              txtFps.setText(fps_str);
            }
          }
        });
      }
    }
  }

  private void stopBufferTimeoutTimer() {
    Log.d(TAG, "bufferTimerStopped");
    videoPlaybackTasks.stopBufferingTimer();
  }

  private void startBufferTimeoutTimer() {
    Log.d(TAG, "bufferTimerStarted");
    videoPlaybackTasks.initBufferingTimer();
    TimerTask timeOutTask = new TimerTask() {
      @Override
      public void run() {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Log.d(TAG, "bufferTimerTriggered");
            if (mDevicePromise != null) {
              mDevicePromise.set(null);
              mDevicePromise = null;
            }
            mPatrollingActor.next();
          }
        });
      }
    };
    videoPlaybackTasks.scheduleBufferTimerTask(timeOutTask, MAX_BUFFERING_TIME);
  }

  private void showFirmwareUpgradeFailedDialog(Message msg) {
    AlertDialog.Builder builder;
    Spanned message1;
    AlertDialog alert;
    da.is_upgrading = false;
    String reason = (String) msg.obj;
    // // Log.d(TAG, "FW upgrade failed, reason: " + reason);
    if (!sa.userWantToCancel && !da.activity_has_stopped) {
      builder = new AlertDialog.Builder(mActivity);
      message1 = Html.fromHtml("<big>" +
          getString(R.string.upgrade_fw_failed) +
          " " + getString(R.string.reason) + ": " + reason +
          " " + getString(R.string.please_manually_reboot_the_camera) +
          "</big>");
      builder.setMessage(message1).setIcon(R.drawable.ic_launcher).setTitle(R.string.app_brand).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(@NotNull DialogInterface dialog, int which) {
          dialog.dismiss();
          switchToCameraListFragment();
        }
      });

      alert = builder.create();
      try {
        alert.show();
      } catch (Exception e1) {
      }
    }
  }

  private void showFirmwareUpgradeDoneDialog() {
    // // Log.d(TAG, "FW upgrade done");
    da.is_upgrading = false;
    AlertDialog.Builder builder;
    AlertDialog alert;
    Spanned message1;
    if (!sa.userWantToCancel && !da.activity_has_stopped) {
      builder = new AlertDialog.Builder(mActivity);
      message1 = Html.fromHtml("<big>" + getString(R.string.upgrade_fw_done) + "</big>");
      builder.setMessage(message1).setIcon(R.drawable.ic_launcher).setTitle(getString(R.string.updating)).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(@NotNull DialogInterface dialog, int which) {
              dialog.cancel();
            }
          }
      );
      builder.setOnCancelListener(new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          scanAndViewCamera();
        }
      });

      alert = builder.create();
      try {
        alert.show();
      } catch (Exception e1) {
        // // Log.e(TAG, e1.getLocalizedMessage());
      }
    }
  }

  private void viewCamera() {
    //videoPlaybackTasks.stopRemoteVideoTimer();
    scanAndViewCamera();
  }

  private void runOnUiThread(Runnable r) {
    if (mActivity != null) {
      mActivity.runOnUiThread(r);
    }
  }

  //ILiveFragmentCallback
  @Override
  public void setupScaleDetector() {
    OnScaleGestureListener scaleListener = new OnScaleGestureListener() {
      @Override
      public void onScaleEnd(@NotNull ScaleGestureDetector detector) {
      }

      @Override
      public boolean onScaleBegin(@NotNull ScaleGestureDetector detector) {
        // // Log.d(TAG, String.format("detector focusX %f, focusY %f", detector.getFocusX(), detector.getFocusY()));
        return true;
      }

      @Override
      public boolean onScale(@NotNull ScaleGestureDetector detector) {
        if (getResources() != null && getResources().getConfiguration() != null) { // Fix NPE
          if ((getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE) {
            float scaleFactor = detector.getScaleFactor();

            sa.scale *= scaleFactor;
            sa.scale = Math.max(1f, Math.min(sa.scale, 5f));
            if (!(sa.scale == 1f && sa.lastScaleFactor == 1f)) {
              // // Log.d(TAG, "Scaling to:" + sa.scale);
              resizeFFmpegView(sa.scale);
            }
            if (sa.scale == 1f) {
              //mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
          }
        }
        return true;
      }
    };
    mScaleDetector = new ScaleGestureDetector(mActivity, scaleListener);
  }

  @Override
  public void setupOnTouchEvent() {
    RelativeLayout mMovieView = (RelativeLayout) findViewById(R.id.content_frame);
    if (mMovieView != null) {
      mMovieView.setOnTouchListener(new OnTouchListener() {

        @Override
        public boolean onTouch(View v, @NotNull MotionEvent event) {
          Log.i(TAG, "mMovieView inside onTouch");
          float curX, curY;
          final ScrollView vScroll = (ScrollView) findViewById(R.id.vscroll);
          final HorizontalScrollView hScroll = (HorizontalScrollView) findViewById(R.id.hscroll);
          if (event.getPointerCount() > 1) {
            // // Log.d(TAG, "> 1 pointer found.");
            mScaleDetector.onTouchEvent(event);
          } else {
            final int action = event.getAction();
            switch (action & MotionEvent.ACTION_MASK) {
              case MotionEvent.ACTION_DOWN:
                pressStartTime = System.currentTimeMillis();
                sa.pressedX = event.getX();
                sa.pressedY = event.getY();
                sa.mx = event.getX();
                sa.my = event.getY();

                if (liveFragment.isFullScreen()) {
                  liveFragment.showSideMenusAndStatus();
                } else {
                  liveFragment.goToFullScreenImmediately();
                  liveFragment.cancelFullscreenTimer();
                }

                break;
              case MotionEvent.ACTION_UP:
                break;

              case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                curY = event.getY();
                vScroll.smoothScrollBy((int) (sa.mx - curX), (int) (sa.my - curY));
                hScroll.smoothScrollBy((int) (sa.mx - curX), (int) (sa.my - curY));
                sa.mx = curX;
                sa.my = curY;
                break;
            }
          }

          return true;
        }
      });

    }
  }

  // IScanner
  @Override
  public void updateScanResult(ScanProfile[] results, int arg1, int arg2) {
    settings.putString(PublicDefineGlob.PREFS_CAM_BEING_VIEWED, selectedDevice.getProfile().getMacAddress());

    // go to view camera
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!da.activity_has_stopped) {
          scanAndViewCamera();
        }
      }
    });
  }

  private void switchToLiveFragment() {
    liveFragment = new LiveFragment();
    liveFragment.setLiveCameraActionButtonListener(this);
    liveFragment.setVideoViewCallback(this);
    liveFragment.setLiveFragmentListener(this);
    switchUpperFragmentTo(liveFragment);
  }

  public void stopAllThread() {
    settings.remove(PublicDefineGlob.PREFS_CAM_BEING_VIEWED);
    // stop WifiScan task
    videoPlaybackTasks.stopRunningWifiScanTask();
  }

  public void setCamera(Context context, Device selectedChannel) {
    if (context != null && selectedChannel != null) {
      Crittercism.leaveBreadcrumb(TAG + " setCamera");
      //CameraAvailabilityManager.getInstance().isCameraInSameNetworkAsync(context, selectedChannel);
    }
    mIsFirstTime = true;
    selectedDevice = selectedChannel;
    da.current_bitrate_value = PublicDefineGlob.INITIAL_VIDEO_BITRATE;
    //VideoBandwidthSupervisor.getInstance().resetValues().setDevice(selectedChannel).checkIfAdaptiveBitrateIsEnabled().setBitrate(PublicDefineGlob.INITIAL_VIDEO_BITRATE);
  }

  public void scanAndViewCamera() {
    if (!da.activity_has_stopped) {
      if (getWindow() != null) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      }
      if (selectedDevice != null) {
        showSpinner();
        final boolean useRemoteOnly = settings.getBoolean(PublicDefineGlob.PREFS_USE_REMOTE_ONLY, false);
        if (useRemoteOnly) {
          Toast.makeText(mActivity, "Forcing remote - set in debug preferences", Toast.LENGTH_SHORT).show();
        }
        checkToShowTimelineAndVideoView();

        AsyncPackage.doInBackground(new Runnable() {
          @Override
          public void run() {
//            final boolean isExistInP2pList = SelectedP2pClients.doesClientExist(selectedDevice.getProfile().getRegistrationId());
//            // If camera registration id doesn't exist in preview list, still need to check in local here
//            final boolean isInLocal;
//            Log.i(TAG, "Camera " + selectedDevice.getProfile().getName() + " isExistInP2pList " + isExistInP2pList);
//            if (P2pManager.hasP2pFeature() && P2pManager.getInstance().isP2pStreamingEnabled() &&
//                selectedDevice.getProfile().canUseP2p() && selectedDevice.getProfile().canUseP2pRelay() && isExistInP2pList) {
//              isInLocal = false;
//            } else {
//              isInLocal = isCameraInSameNetwork();
//            }
            final boolean isInLocal = isCameraInSameNetwork();
            Log.i(TAG, "is camera in same network? " + isInLocal);
            if (isWifiConnected() && isInLocal && !useRemoteOnly) {
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  /* 20151107: hoang
                   * Don't force show wifi strength here.
                   * Need to show it when setupFFmpegPlayer.
                   */
//                  if (liveFragment != null && !sessionAttributes.isDebugEnabled) {
//                    liveFragment.forceShowWifiStrength();
//                  }
                  /*Log.i(TAG, "CALL --> prepareToViewCameraLocally");
                  prepareToViewCameraLocally();
                  /*
                   * 20151019: hoang: AA-936
                   * App crash due to updating resolution when video stream is not ready on LiveFragment.
                   * Solution: don't need to get_resolution here because app can get the video resolution
                   * directly from stream.
                   */
                  // updateResolutionTask();
                /*}
              });
            } else if (selectedDevice.getProfile().isAvailable() || useRemoteOnly) {
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Log.i(TAG, "CALL --> viewRelayStream");
                  viewRelayStream();
                  /*
                   * 20151019: hoang: AA-936
                   * App crash due to updating resolution when video stream is not ready on LiveFragment.
                   * Solution: don't need to get_resolution here because app can get the video resolution
                   * directly from stream.
                   */
                  // updateResolutionTask();
                /*}
              });
           /* } else {
              if (!selectedDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Toast.makeText(mActivity, mActivity.getString(R.string.camera_offline), Toast.LENGTH_SHORT).show();
                    ((MainActivity) mActivity).goToEventLog(selectedDevice);
                  }
                });
              }
            }
          }
        });

      }
    }
  }

  private boolean isCameraInSameNetwork() {
    mIsLocal = CameraAvailabilityManager.getInstance().isCameraInSameNetwork(mActivity, selectedDevice);

    return mIsLocal;
  }

  private boolean isWifiConnected() {
    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    return (wifiInfo != null && wifiInfo.isConnected());
  }

  private void viewRelayStream() {
    checkToShowTimelineAndVideoView();
    sa.connecting_start_time = System.currentTimeMillis();
    prepareToViewCameraRemotely();
  }

  private void prepareToViewCameraRemotely() {
    sa.create_session_start_time = System.currentTimeMillis();
    if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() &&
            P2pSettingUtils.getInstance().isRemoteP2pStreamingEnabled() &&  isOrbitP2PEnabled() && selectedDevice.getProfile().canUseP2p()) {
      // Port from V2 app. Show p2p outdated dialog for camera with p2p_protocol 02_00
//      if (selectedDevice.getProfile().canUseP2pRelay()) {
//        Log.d(TAG, "P2p protocol version of camera can use p2p relay -> show outdated dialog");
//        setP2pOutdatedDialogShown(true);
//      } else
      {
        prepareToViewCameraViaP2p(false);
//        if (sessionAttributes.p2pTries < P2pManager.P2P_MAX_TRY) {
//          prepareToViewCameraViaP2p(false);
//        } else {
//          Log.d(TAG, String.format("P2p tries? %d -> switch to relay", sessionAttributes.p2pTries));
//          prepareToViewCameraViaRelay();
//        }
      }
    } else {
      Log.d(TAG, "Try rtmp due to: isP2pStreamingEnabled? " + P2pSettingUtils.getInstance().isP2pStreamingEnabled() +
          ", canUseP2p? " + selectedDevice.getProfile().canUseP2p() + ", canUseP2pRelay? " + selectedDevice.getProfile().canUseP2pRelay());
      prepareToViewCameraViaRelay();
    }
  }

  private void updateGlobalBitrateView(final int bitrate) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {

        TextView txtGlobalBitrate = (TextView) findViewById(R.id.textGlobalBitrate);
        if (txtGlobalBitrate != null) {
          String bitrate_str = String.format("%d %s", bitrate, "kbps");
          txtGlobalBitrate.setText(bitrate_str);
        }
      }
    });
  }

  public static String getIPOverrideSetting(Activity x) {
    String setting = settings.getString("LOCAL_IP_OVERRIDE", "");
    return setting;
  }

  private String getSelectedRegistrationId() {
    return selectedDevice.getProfile().getRegistrationId();
  }

  private String getApiKey() {
    return getPrefString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
  }

  private String getPrefString(final String prefName, final String optionalDefault) {
    return settings.getString(prefName, optionalDefault);
  }

  private void cancelVideoTimeoutTask() {
    videoPlaybackTasks.stopRemoteVideoTimer();
  }

  private void setVideoBitrate(int videoBitrate) {
    da.current_bitrate_value = videoBitrate;
    VideoBandwidthSupervisor.getInstance().setBitrate(videoBitrate);
    updateGlobalBitrateView(videoBitrate);
  }

  private void prepareToViewCameraLocally2() {
    // // Log.d(TAG, "prepare to view local camera...");
    sa.viewRelayRtmp = false;
    da.device_ip = selectedDevice.getProfile().getDeviceLocation().getLocalIp();
    da.device_port = Integer.parseInt(selectedDevice.getProfile().getDeviceLocation().getLocalPort1());
    String deviceIPOverride = getIPOverrideSetting(mActivity);
    if (!deviceIPOverride.equals("")) {
      sa.filePath = String.format("rtsp://user:pass@%s:%d/blinkhd", deviceIPOverride, 6667);
    } else {
      sa.filePath = String.format("rtsp://user:pass@%s:%d/blinkhd", da.device_ip, 6667);
    }
    // // Log.d(TAG, "Viewing " + sa.filePath);
    Toast.makeText(mActivity, "Viewing locally", Toast.LENGTH_SHORT).show();
    openFFmpegFragment();
  }

  private void prepareToViewCameraLocally() {
    if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() && isOrbitP2PEnabled() && selectedDevice.getProfile().canUseP2p()) {
      // Port from V2 app. Show p2p outdated dialog for camera with p2p_protocol 02_00
//      if (selectedDevice.getProfile().canUseP2pRelay()) {
//        Log.d(TAG, "P2p protocol version of camera can use p2p relay -> show outdated dialog");
//        setP2pOutdatedDialogShown(true);
//      } else
      {
        if (sa.p2pTries < P2pManager.P2P_MAX_TRY) {
          prepareToViewCameraViaP2p(true);
        } else {
          Log.d(TAG, String.format("P2p tries? %d -> switch to relay", sa.p2pTries));
          prepareToViewCameraViaRelay();
        }
      }
    } else {
      Log.d(TAG, "Try rtsp due to: isP2pStreamingEnabled? " + P2pSettingUtils.getInstance().isP2pStreamingEnabled() +
          ", canUseP2p? " + selectedDevice.getProfile().canUseP2p() + ", canUseP2pRelay? " + selectedDevice.getProfile().canUseP2pRelay());
      if (selectedDevice.getProfile().isRtspStreamingOutdated()) {
        Log.d(TAG, "Rtsp streaming is outdated, switch to rtmp relay");
        prepareToViewCameraViaRelay();
      } else {
        prepareToViewCameraViaRtsp();
      }
    }
  }

  private void prepareToViewCameraViaP2p(boolean isInLocal) {
    startP2pStreamTask(isInLocal);
  }

  private void startP2pStreamTask(boolean isInLocal) {
    Log.d(TAG, "startP2pStreamTask, isInLocal? " + isInLocal);
    sa.viewRelayRtmp = false;
    sa.viewP2p = true;
    sa.p2pTries++;
    if (isInLocal) {
      da.currentConnectionMode = HubbleSessionManager.CONNECTION_MODE_P2P_LOCAL;
    } else {
      da.currentConnectionMode = HubbleSessionManager.CONNECTION_MODE_P2P_REMOTE;
    }
    //HubbleSessionManager.getInstance().updateCurrentConnectionMode(da.currentConnectionMode);
    //HubbleSessionManager.getInstance().resetVideoBitrate();
    String saved_token = getApiKey();
    String regId = getSelectedRegistrationId();
    try {
      LegacyCamProfile currCamProfile = LegacyCamProfile.fromDeviceProfile(selectedDevice.getProfile());
      currCamProfile.setInLocal(isInLocal);
      videoPlaybackTasks.startP2pStreamTask(mActivity, this, currCamProfile, regId, saved_token, isMobileDataConnected(), selectedDevice.getProfile().canUseP2pRelay());
    } catch (Exception e) {
    }
  }

  private boolean isMobileDataConnected() {
    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mobileInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    return (mobileInfo != null && mobileInfo.isConnected());
  }

  private void prepareToViewCameraViaRelay() {
    sa.create_session_start_time = System.currentTimeMillis();
    sa.viewRelayRtmp = true;
    sa.viewRelayRtmp = true;
    sa.viewP2p = false;
    da.currentConnectionMode = HubbleSessionManager.CONNECTION_MODE_RTMP_REMOTE;
    HubbleSessionManager.getInstance().updateCurrentConnectionMode(da.currentConnectionMode);
    // // Log.d(TAG, "prepare to view relay camera...");
    String saved_token = getApiKey();
    String regId = getSelectedRegistrationId();
    try {
      videoPlaybackTasks.startRelayStreamPatrolTask(mActivity, this, LegacyCamProfile.fromDeviceProfile(selectedDevice.getProfile()), regId, saved_token);
    } catch (Exception e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }
  }

  private void stopCountDownTimer() {
    videoPlaybackTasks.stopCountDownTimer();
  }

  private void switchToCameraListFragment() {
    MainActivity sa = (MainActivity) mActivity;
    sa.switchToDeviceList();
  }

  private void checkToShowTimelineAndVideoView() {
    topViewHolder = (RelativeLayout) findViewById(R.id.patrolFragmentTopView);
    if (!selectedDevice.getProfile().isAvailable()) {
      topViewHolder.setVisibility(View.GONE);
      Toast.makeText(mActivity.getApplicationContext(), getString(R.string.camera_disconnected), Toast.LENGTH_LONG).show();
    } else {
      topViewHolder.setVisibility(View.VISIBLE);
      switchToLiveFragment();
    }
  }

  private void switchUpperFragmentTo(Fragment fragment) {
    if (fragment != null) {
      try {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.patrolFragmentVideoView, fragment, "FFMpegPlayer");
        fragmentTransaction.commitAllowingStateLoss();
      } catch (IllegalArgumentException ignored) {
        //catches if view cannot be found or resolved
        //typically happens when activity is backgrounded
      }
    }
  }

  private void updateOptionsMenu() {
    mActivity.invalidateOptionsMenu();
  }

  private void setupRemoteCamera(BabyMonitorAuthentication bm_auth) {

    if (bm_auth != null) {
      da.device_ip = bm_auth.getIP();
      da.device_port = bm_auth.getPort();
      sa.bm_session_auth = bm_auth;// reserved to used later if we need to
      // restart the videostreamer -audio only
      // mode
    }
    sa.filePath = videoPlaybackTasks.getStreamUrl();

    // TURD: Follow the white rabbit.
    new Handler().post(new Runnable() {
      public void run() {
        // Down we go...
        openFFmpegFragment();
      }
    });

  }

  @Deprecated
  private void openFFmpegFragment() {
    // Break on through to the other side
    if (sa.p2pClients != null) {
      if (liveFragment != null) {
        // Oh wait, so we are running on the ui thread here...
        new Thread(new Runnable() {
          @Override
          public void run() {
            // But here we want to run in the background.... ok...
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                liveFragment.setP2PInfo(sa.p2pClients);
                Device currentDevice = selectedDevice;
                liveFragment.setDevice(currentDevice);
                liveFragment.setStreamUrl(sa.filePath);
              }
            });
          }
        }).start();
      }
    } else if (sa.filePath == null) {
      Log.d(TAG, "File path is null");
    } else if (!sa.isVideoTimeout) {
      if (liveFragment != null) {
        // Oh wait, so we are running on the ui thread here...
        new Thread(new Runnable() {
          @Override
          public void run() {
            // But here we want to run in the background.... ok...
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                liveFragment.setP2PInfo(null);
                Device currentDevice = selectedDevice;
                liveFragment.setDevice(currentDevice);
                liveFragment.setStreamUrl(sa.filePath);
              }
            });
          }
        }).start();
      } else {
        Log.d(TAG, "LiveFragment is null");
      }
    } else {
      Log.d(TAG, "openFFmpegFragment failed");
    }
  }


  private void remoteVideoHasStopped(int reason) {
    LiveFragment liveFrag = (LiveFragment) getFragmentManager().findFragmentByTag("FFMpegPlayer");
    if (liveFrag != null) {
      liveFrag.stopStreaming();
    }
    if (sa.userWantToCancel || da.activity_has_stopped) {
      return;
    }
    sa.remote_reconnect_times++;
    VideoBandwidthSupervisor.getInstance().useRecoverySettings();
    updateGlobalBitrateView(VideoBandwidthSupervisor.getInstance().getBitrate());

    switch (reason) {
      case Streaming.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY:
        this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            switchToLiveFragment();
            prepareToViewCameraViaRelay();
          }
        });
        break;
    }
  }

  private void videoHasStoppedUnexpectedly() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        // Stop players
        stopLiveFragmentStreaming();

        if (unexpectedStopRetries > 3) {
          Toast.makeText(mActivity, mActivity.getString(R.string.camera_not_available), Toast.LENGTH_SHORT).show();
          switchToCameraListFragment();
          return;
        }

        // Decide whether Router disconnects or Camera disconnect
        if (sa.userWantToCancel) {
          return;
        }

        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String ssid_no_quote = settings.getString(sa.string_currentSSID, null);

        if (wm.getConnectionInfo() == null || !wm.getConnectionInfo().getSSID().equalsIgnoreCase(ssid_no_quote)) { // Router down
          // // Log.d(TAG, "Wifi SSID is not the same, Router is probably down ");
          MiniWifiScanUpdater iw = new MiniWifiScanUpdater();
          videoPlaybackTasks.startWifiTask(mActivity, iw);
        }
      }
    });
    unexpectedStopRetries++;
  }

  private void stopLiveFragmentStreaming() {
    LiveFragment liveFrag = (LiveFragment) getFragmentManager().findFragmentByTag("FFMpegPlayer");
    if (liveFrag != null) {
      liveFrag.stopStreaming();
    }
  }

  private String getCurrentConnectionModeLetter() {
    String modeStr = "";
    switch (da.currentConnectionMode) {
      case HubbleSessionManager.CONNECTION_MODE_RTSP_LOCAL:
        modeStr = "L";
        break;
      case HubbleSessionManager.CONNECTION_MODE_RTMP_REMOTE:
        modeStr = "R";
        break;
      case HubbleSessionManager.CONNECTION_MODE_P2P_LOCAL:
      case HubbleSessionManager.CONNECTION_MODE_P2P_REMOTE:
      case HubbleSessionManager.CONNECTION_MODE_P2P_RELAY:
        modeStr = P2pManager.getInstance().getCurrConnectionMode();
        break;
      default:
        modeStr = "";
        break;
    }
    return modeStr;
  }

  private void resizeFFmpegView(float scaleFactor) {
    if (sa.ratio != 0) {
      if (Math.abs(scaleFactor - sa.lastScaleFactor) >= 0.03 || scaleFactor == 1f) {
        sa.lastScaleFactor = scaleFactor;
        FFMpegMovieViewAndroid mMovieView = (FFMpegMovieViewAndroid) findViewById(R.id.imageVideo);
        topViewHolder = (RelativeLayout) findViewById(R.id.patrolFragmentTopView);
        if (mMovieView != null) {
          int new_width, new_height;
          if ((getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE) {
            new_width = (int) (sa.default_width * scaleFactor);
            new_height = (int) (sa.default_height * scaleFactor);
          } else {
            RelativeLayout.LayoutParams live_params = (RelativeLayout.LayoutParams) topViewHolder.getLayoutParams();
            if (sa.default_screen_height / sa.ratio < live_params.height) {
              // use full height of screen size
              new_width = sa.default_screen_height;
              // new_width = live_params.width;
              new_height = (int) (new_width / sa.ratio);
            } else {
              new_height = live_params.height;
              new_width = (int) (new_height * sa.ratio);
            }
          }
          LayoutParams movie_params = mMovieView.getLayoutParams();
          movie_params.width = new_width;
          movie_params.height = new_height;
          mMovieView.setLayoutParams(movie_params);
          // // Log.d(TAG, "Surface resized: video_screen_width: " + new_width + ", video_screen_height: " + new_height);
        }

      }
    }
  }

  private void recalcDefaultScreenSize() {
    DisplayMetrics displaymetrics = new DisplayMetrics();
    if (getWindowManager() != null) {
      getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    }

    if (displaymetrics.widthPixels > displaymetrics.heightPixels) {
      sa.default_screen_height = displaymetrics.heightPixels;
      sa.default_screen_width = displaymetrics.widthPixels;
    } else {
      sa.default_screen_height = displaymetrics.widthPixels;
      sa.default_screen_width = displaymetrics.heightPixels;
    }

    // // Log.d(TAG, "Default screen size: default width, default height: " + sa.default_screen_width + ", " + sa.default_screen_height);

    if (sa.ratio != 0) {
      if (sa.default_screen_height * sa.ratio > sa.default_screen_width) {
        sa.default_width = sa.default_screen_width;
        sa.default_height = (int) (sa.default_width / sa.ratio);
      } else {
        sa.default_height = sa.default_screen_height;
        sa.default_width = (int) (sa.default_height * sa.ratio);
      }
    }
    // // Log.d(TAG, "Recalculate default size with ratio " + sa.ratio + ": width: " + sa.default_width + ", height: " + sa.default_height);
  }

  private void clearKeepScreenOnFlags() {
    if (getWindow() != null) {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
  }

  private OnClickListener patrolButtonClickListener = new OnClickListener() {
    @Override
    public void onClick(View patrolButton) {
      if (mPatrollingActor != null) {
        if (patrolButton.getId() == (R.id.patrolFragmentBackButton)) {
          if (mDevicePromise != null) {
            mDevicePromise.set(null);
            mDevicePromise = null;
          }
          mPatrollingActor.previous();
        } else if (patrolButton.getId() == (R.id.patrolFragmentPlayPauseButton)) {
          if (!paused) {
            mPatrollingActor.pause();
            paused = true;
          } else {
            mPatrollingActor.resume();
            paused = false;
          }
        } else if (patrolButton.getId() == (R.id.patrolFragmentNextButton)) {
          if (mDevicePromise != null) {
            mDevicePromise.set(null);
            mDevicePromise = null;
          }
          mPatrollingActor.next();
        }
      }
    }

  };

  private class MiniWifiScanUpdater implements IWifiScanUpdater {
    @Override
    public void scanWasCanceled() {
      // Do nothing here for now
    }

    @Override
    public void updateWifiScanResult(List<ScanResult> results) {
      String check_SSID = settings.getString(sa.string_currentSSID, null);
      String check_SSID_w_quote = "\"" + check_SSID + "\"";
      boolean found_in_range = false;
      if (results != null) {
        for (ScanResult result : results) {
          if ((result.SSID != null) && (result.SSID.equalsIgnoreCase(check_SSID))) {
            // // Log.d(TAG, "found " + check_SSID + " .. in range");
            found_in_range = true;
            break;
          }
        }
      }

      // This code appears to try to connect to RTSP in AP mode
      if (found_in_range) {
        // try to connect back to this BSSID
        WifiManager w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> wcs = w.getConfiguredNetworks();

        Callback h = new Callback() {
          @Override
          public boolean handleMessage(@NotNull Message msg) {
            switch (msg.what) {
              case ConnectToNetworkTask.MSG_CONNECT_TO_NW_DONE:
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    prepareToViewCameraLocally();
                  }
                });
                break;
              case ConnectToNetworkTask.MSG_CONNECT_TO_NW_FAILED:
                videoPlaybackTasks.startWifiTask(mActivity, new MiniWifiScanUpdater());
                break;
            }
            return false;
          }
        };

        ConnectToNetworkTask connect_task = new ConnectToNetworkTask(mActivity, new Handler(h));
        connect_task.dontRemoveFailedConnection(true);
        connect_task.setIgnoreBSSID(true);
        for (WifiConfiguration wc : wcs) {
          if ((wc.SSID != null) && wc.SSID.equalsIgnoreCase(check_SSID_w_quote)) {
            // This task will make sure the app is connected to the
            // camera.
            // At the end it will send MSG_CONNECT_TO_NW_DONE
            connect_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, wc);
            break;
          }
        }
      } else {
        //Scan again
        videoPlaybackTasks.startWifiTask(mActivity, new MiniWifiScanUpdater());
      }

    }
  }

  private class VideoTimeoutTask extends TimerTask {
    public void run() {
      if (PatrolVideoFragment.this.isAdded() && shouldVideoTimeout()) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            videoPlaybackTasks.initCountDownTimer(new ITimerUpdater() {
              @Override
              public void updateCurrentCount(final int count) {

              }

              @Override
              public void timeUp() {
                if (da.activity_has_stopped) {
                  return;
                }
                // Time is really up --
                mActivity.runOnUiThread(new Runnable() {
                  public void run() {
                    if (liveFragment != null) {
                      liveFragment.stopStreaming();
                    }
                    videoPlaybackTasks.stopAllTimers();
                    clearKeepScreenOnFlags();

                    createVideoTimeoutDialog();
                  }
                });
              }

              private void createVideoTimeoutDialog() {
                final VideoTimeoutDialog dialog = new VideoTimeoutDialog();

                dialog.setCommonDialogListener(new CommonDialogListener() {
                  @Override
                  public void onDialogPositiveClick(DialogFragment arg0) {
                    dialog.dismiss();
                    sa.isVideoTimeout = false;
                    scanAndViewCamera();
                    settings.putBoolean(SHOULD_VIDEO_TIMEOUT, false);
                  }

                  @Override
                  public void onDialogNegativeClick(DialogFragment dialog) {
                    dialog.dismiss();
                    sa.isVideoTimeout = false;
                    if (mActivity != null) {
                      ((MainActivity) mActivity).switchToDeviceList();
                    }
                  }

                  @Override
                  public void onDialogNeutral(DialogFragment dialog) {
                    dialog.dismiss();
                    sa.isVideoTimeout = false;
                    scanAndViewCamera();
                  }
                });
                AsyncPackage.doInBackground(new Runnable() {
                  @Override
                  public void run() {
                    GoogleAnalyticsController.getInstance().trackCameraInfo("Media Timeout While Streaming", selectedDevice);
                    AnalyticsInterface.getInstance().trackEvent("Media_timeout_while_streaming","Media_timeout_while_streaming",eventData);
                  }
                });
                dialog.show(getFragmentManager(), "Dialog_Change_Camera_Name");
              }

              @Override
              public void timerKick() {
                if (da.activity_has_stopped) {
                  return;
                }

                videoPlaybackTasks.initRemoteVideoTimer();
                videoPlaybackTasks.scheduleRemoteVideoTimerTask(new VideoTimeoutTask(), VIDEO_TIMEOUT);
              }
            });

            videoPlaybackTasks.startCountDownTimerThread();
          }
        });
      }
    }
  }


  @Override
  public void onZoom(boolean isEnabled) {
  }

  @Override
  public void onPan(boolean isEnable) {
  }

  @Override
  public void onMic(boolean isEnable) {
  }

  @Override
  public void onRecord(boolean isEnable) {
  }

  @Override
  public void onStorage(boolean isEnable) {
  }

  @Override
  public void onMelody(boolean isEnable) {
  }

  @Override
  public void onTemperature(boolean isEnable) {
  }

  @Override
  public void onAudioEnable(boolean isEnable) {
  }

  @Override
  public void onSettings(boolean enabled) {
  }

  @Override
  public void onSnap() {
  }

  @Override
  public void onHD(boolean onHD) {
  }

  @Override
  public void onPreset(boolean enabled) {

  }


  public void onMotionCalibration(boolean enabled) {

  }

  @Override
  public void onHumidity(boolean enabled) {

  }

  @Override
  public void onBTA(boolean enabled) {

  }
}*/