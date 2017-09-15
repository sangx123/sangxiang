package cz.havlena.ffmpeg.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.discovery.LocalScanForCameras;
import com.discovery.ScanProfile;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.interfaces.ICameraScanner;
import com.hubble.registration.interfaces.IWifiScanUpdater;
import com.hubble.registration.models.BabyMonitorAuthentication;
import com.hubble.registration.models.CamChannel;
import com.hubble.registration.models.CameraPassword;
import com.hubble.registration.models.LegacyCamProfile;
import com.hubble.registration.tasks.ConnectToNetworkTask;
import com.hubble.registration.tasks.RemoteStreamTask;
import com.hubble.registration.tasks.WifiScan;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubble.registration.tasks.comm.UDTRequestSendRecvTask;
import com.hubble.registration.ui.FadeOutAnimationAndGoneListener;
import com.hubble.registration.ui.FirstTimeActivity;
import com.hubbleconnected.camera.R;
import com.media.ffmpeg.FFMpeg;
import com.media.ffmpeg.FFMpegException;
import com.media.ffmpeg.FFMpegPlayer;
import com.media.ffmpeg.FFMpegPlayer.RTSP_PROTOCOL;
import com.media.ffmpeg.android.FFMpegMovieViewAndroid;
import com.msc3.ConnectToNetworkActivity;
import com.msc3.IMelodyUpdater;
import com.msc3.IResUpdater;
import com.msc3.LeftSideMenuImageAdapter;
import com.msc3.RtmpStream2Task;
import com.msc3.ScreenTimeOutRunnable;
import com.msc3.Streamer;
import com.msc3.update.CheckVersionFW;
import com.msc3.update.IpAndVersion;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import base.hubble.PublicDefineGlob;
import base.hubble.constants.Streaming;
import base.hubble.meapi.Device;
import base.hubble.meapi.device.SendCommandResponse;
import com.hubbleconnected.camera.BuildConfig;
import com.hubbleconnected.camera.R;

public class FFMpegPlayerActivity extends FragmentActivity implements Callback, IResUpdater, IMelodyUpdater {
  private static final String TAG = "FFMpegPlayerActivity";

  public static final String ACTION_FFMPEG_PLAYER_STOPPED = "cz.havlena.ffmpeg.ui.ACTION_FFMPEG_PLAYER_STOPPED";

  // private static final String LICENSE =
  // "This software uses libraries from the FFmpeg project under the LGPLv2.1";

  private FFMpegMovieViewAndroid mMovieView;
  private String device_ip;
  private int device_port;
  private String http_pass;
  // private WakeLock mWakeLock;
  private String filePath = null;

  private int default_screen_width, default_screen_height;
  private int default_width, default_height;
  private float ratio;

  private final String[] resolution_cmds = new String[]{PublicDefineGlob.RESOLUTION_480P, PublicDefineGlob.RESOLUTION_720P_10, PublicDefineGlob.RESOLUTION_720P_15};
  private int currentResolutionIdx = 0;

  private boolean isConnectingForTheFirstTime;
  private boolean userWantToCancel;

  private int motion_rows;
  private int motion_cols;
  private boolean[][] zoneDetect;
  private boolean[][] zoneDetectTemp;

  private CamChannel selected_channel = null;

  private int currentMelodyIndx = 0;

  private WifiScan ws = null;


  public static final String CAMCHANNEL_SHOWING_CHANNEL = "camera_channel";
  private SecureConfig settings = HubbleApplication.AppConfig;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    isConnectingForTheFirstTime = true;
    userWantToCancel = false;
    device_ip = null;
    device_port = -1;

    motion_rows = 3;
    motion_cols = 3;
    zoneDetect = new boolean[motion_rows][motion_cols];
    zoneDetectTemp = new boolean[motion_rows][motion_cols];
    for (int i = 0; i < motion_rows; i++) {
      for (int j = 0; j < motion_cols; j++) {
        //row i, column j
        zoneDetect[i][j] = false;
        zoneDetectTemp[i][j] = false;
      }
    }

    http_pass = String.format("%s:%s", PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, PublicDefineGlob.DEFAULT_CAM_PWD);

    setContentView(R.layout.ffmpeg_main);

    Intent i = getIntent();
    if (i != null) {
      Bundle extra = i.getExtras();
      if (extra != null) {
        selected_channel = (CamChannel) getIntent().getExtras().getSerializable(CAMCHANNEL_SHOWING_CHANNEL);

      }
    }

    WifiManager w = (WifiManager) getSystemService(WIFI_SERVICE);
    String curr_ssid = w.getConnectionInfo().getSSID();
    settings.putString(string_currentSSID, curr_ssid);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
  }

  private RTSP_PROTOCOL rtspProtocol;

  protected void onStart() {
    super.onStart();

    // RTSP STUN

    ACTIVITY_HAS_STOPPED = false;

  }

  /*
   * (non-Javadoc)
     *
     * @see
     * android.app.Activity#onConfigurationChanged(android.content.res.Configuration
     * )
     */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    if (mMovieView != null) {
      resizeFFMpegView();
      RelativeLayout motionSetting = (RelativeLayout) findViewById(R.id.motionZones);
      if (motionSetting != null && motionSetting.isShown()) {
        setupMotionSettingView();
      }
    }
  }

  private void resizeFFMpegView() {
    if (mMovieView != null) {
      int new_width, new_height;

      if ((getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE) {
        new_width = default_width;
        new_height = default_height;
      } else {
        //use full height of screen size
        new_width = default_screen_height;
        new_height = (int) (new_width / ratio);
      }

      LayoutParams params = mMovieView.getLayoutParams();
      params.width = new_width;
      params.height = new_height;
      mMovieView.setLayoutParams(params);
      // // Log.d(TAG, "Surface changed: width: " + new_width + ", height: " + new_height);
    }
  }


  private void setupFFMpegPlayer(boolean show_dialog) {
    setContentView(R.layout.ffmpeg_player_activity);
    mMovieView = (FFMpegMovieViewAndroid) findViewById(R.id.imageVideo);
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      mMovieView.hideControllerThumb();
    }

    if (filePath == null) {
      // // Log.d(TAG, "Not specified video file");
      finish();
    } else {
      queryMotionAreaSetting();

      setupSideMenu();

      mMovieView.setOnTouchListener(new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

          // IF videorecording is going on.. ignore these event
          RelativeLayout recMenu = (RelativeLayout) findViewById(R.id.rec_menu);

          final int action = event.getAction();
          switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
              if (isFullScreen == true) {
                showSideMenusAndStatus();

                isFullScreen = false;
              } else {
                goToFullScreen();
              }
              break;
            case MotionEvent.ACTION_UP:
              tryToGoToFullScreen();
              break;
          }

          return true;
        }
      });
      tryToGoToFullScreen();

      try {

        FFMpeg ffmpeg = new FFMpeg();
        // mMovieView = ffmpeg.getMovieView(this);

        settings.putString(PublicDefineGlob.PREFS_CAM_BEING_VIEWED, selected_channel.getCamProfile().get_MAC());

        if (selected_channel.getCamProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_MBP36N)) {
          mMovieView.initVideoView(new Handler(FFMpegPlayerActivity.this), false, true);
        } else {
          mMovieView.initVideoView(new Handler(FFMpegPlayerActivity.this), false, false);
        }

        if (rtspProtocol == RTSP_PROTOCOL.TCP) {
          mMovieView.setFFMpegPlayerOptions(FFMpegPlayer.MEDIA_STREAM_RTSP_WITH_TCP);
        }
        // else don't need to do anything .. default is UDP

        mMovieView.setVideoPath(filePath);


        if (show_dialog) {
          if (filePath.startsWith("rtmp")) {
            try {
              showDialog(DIALOG_UDT_RELAY_CONNECTION_IN_PROG);
            } catch (Exception e) {

            }
          } else {

            try {
              showDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
            } catch (Exception e) {

            }
          }
        }
      } catch (FFMpegException e) {
        // // Log.d(TAG, "Error when initializing ffmpeg: " + e.getMessage());
        FFMpegMessageBox.show(this, e);
        finish();
      }
    }
  }

  /**
   *
   */
  private void setupMotionSettingView() {
    if (mMovieView != null) {
      final RelativeLayout motionSetting = (RelativeLayout) findViewById(R.id.motionZones);
      if (motionSetting != null) {
        LayoutParams movie_params = mMovieView.getLayoutParams();
        int video_width = movie_params.width;
        int video_height = movie_params.height;
        // // Log.d("mbp", "Motion Zones: width: " + video_width + ", height: " + video_height);
        int zoneWidth = video_width / motion_cols;
        int zoneHeight = video_height / motion_rows;

        for (int i = 0; i < motion_rows; i++) {
          for (int j = 0; j < motion_cols; j++) {
            int zoneId = i * motion_rows + j;
            int cbId = zoneId + motion_rows * motion_cols;
            RelativeLayout itemView = (RelativeLayout) findViewById(zoneId);
            CheckBox cb = (CheckBox) findViewById(cbId);
            if (itemView == null) {
              cb = new CheckBox(this);
              cb.setId(cbId);

              itemView = new RelativeLayout(this);
              itemView.setId(zoneId);
              itemView.addView(cb);
              motionSetting.addView(itemView);
            }

            cb.setChecked(zoneDetectTemp[i][j]);
            cb.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            final int row_position = i;
            final int col_position = j;
            cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

              @Override
              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                zoneDetectTemp[row_position][col_position] = isChecked;
                buttonView.setChecked(isChecked);
              }
            });

            int temp_width, temp_height;
            int left, right, top, bottom;

            left = j * zoneWidth;

            if (j == motion_cols - 1) {
              right = 0;
              temp_width = video_width - left;
            } else {
              right = video_width - (left + zoneWidth);
              temp_width = zoneWidth;
            }

            top = i * zoneHeight;

            if (i == motion_rows - 1) {
              bottom = 0;
              temp_height = video_height - top;
            } else {
              bottom = video_height - (top + zoneHeight);
              temp_height = zoneHeight;
            }

            // // Log.d("mbp", "setting zone " + i + j + ": " +
            //  "left, right, top, bottom: " + left + ", " + right +
            //  ", " + top + ", " + bottom);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(temp_width, temp_height);
            params.setMargins(left, top, right, bottom);
            itemView.setLayoutParams(params);
            itemView.setBackgroundResource(R.drawable.rectangle);
            itemView.setGravity(Gravity.CENTER);

          }
        }

        motionSetting.invalidate();

      } //if (motionSetting != null)
    } //if (mMovieView != null)
  }

  private void queryMotionAreaSetting() {
    Thread worker = new Thread(new Runnable() {

      @Override
      public void run() {
        final String str_get_motion_area = "get_motion_area: ";
        String response = null;
        String request = PublicDefineGlob.GET_MOTION_AREA_CMD;
        if (selected_channel.getCamProfile().isInLocal()) {
          request = PublicDefineGlob.HTTP_CMD_PART + request;
          final String device_address_port = device_ip + ":" + device_port;
          String http_addr = String.format("%1$s%2$s%3$s", "http://", device_address_port, request);
          // // Log.d("mbp", "get motion area cmd: " + http_addr);
          response = HTTPRequestSendRecvTask.sendRequest_block_for_response(http_addr, PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, PublicDefineGlob.DEFAULT_CAM_PWD);
        } else {
          request = PublicDefineGlob.BM_HTTP_CMD_PART + request;
          String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
          // // Log.d("mbp", "get motion area cmd: " + request);
          response = UDTRequestSendRecvTask.sendRequest_via_stun2(saved_token, selected_channel.getCamProfile().get_MAC(), request);
        }

        // // Log.d("mbp", "get motion area res: " + response);
        if (response != null && response.startsWith(str_get_motion_area)) {
          parseZoneResponse(response.substring(str_get_motion_area.length()));
        }
      }
    });

    worker.start();

  }

  private void setMotionArea() {
    String zoneParams = buildZoneRequest();
    if (zoneParams != null) {
      String request = PublicDefineGlob.SET_MOTION_AREA_CMD +
          PublicDefineGlob.SET_MOTION_AREA_PARAM_1 + (motion_rows + "x" + motion_cols) +
          PublicDefineGlob.SET_MOTION_AREA_PARAM_2 + zoneParams;

      if (selected_channel.getCamProfile().isInLocal()) {
        request = PublicDefineGlob.HTTP_CMD_PART + request;
        final String device_address_port = device_ip + ":" + device_port;
        String http_addr = String.format("%1$s%2$s%3$s", "http://", device_address_port, request);
        // // Log.d("mbp", "set motion area cmd: " + http_addr);
        HTTPRequestSendRecvTask.sendRequest_block_for_response(http_addr, PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, PublicDefineGlob.DEFAULT_CAM_PWD);
      } else {
        request = PublicDefineGlob.BM_HTTP_CMD_PART + request;
        String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
        // // Log.d("mbp", "set motion area cmd: " + request);
        UDTRequestSendRecvTask.sendRequest_via_stun2(saved_token, selected_channel.getCamProfile().get_MAC(), request);
      }

      updateMotionSetting();
    }

    updateMotionSetting();
  }

  //00,11...
  private String buildZoneRequest() {
    String request = "";
    for (int i = 0; i < motion_rows; i++) {
      for (int j = 0; j < motion_cols; j++) {
        if (zoneDetectTemp[i][j]) {
          request += "," + i + j;
        }
      }
    }

    if (!request.isEmpty()) {
      //remove "," at the beginning
      request = request.substring(1);
    }

    return request;
  }

  //grid=AxB,zone=00,11...
  private void parseZoneResponse(String zoneParams) {
    if (zoneParams == null) {
      return;
    }

    final String str_grid = "grid=";
    final String str_zone = "zone=";

    //find the first index of ","
    int split_idx = zoneParams.indexOf(",");
    String grid_params = zoneParams.substring(0, split_idx);
    String zone_params = zoneParams.substring(split_idx + 1);
    if (zone_params != null && zone_params.startsWith(str_zone)) {
      zone_params = zone_params.substring(str_zone.length());
      if (zone_params != null && !zone_params.isEmpty()) {
        String[] zone_params_arr = zone_params.split(",");
        if (zone_params_arr != null && zone_params_arr.length > 0) {
          for (int i = 0; i < zone_params_arr.length; i++) {
            int row_idx = -1;
            int col_idx = -1;
            String row_idx_str = zone_params_arr[i].substring(0, 1);
            String col_idx_str = zone_params_arr[i].substring(1);

            try {
              row_idx = Integer.parseInt(row_idx_str);
              col_idx = Integer.parseInt(col_idx_str);
            } catch (NumberFormatException e) {
              // // Log.e(TAG, Log.getStackTraceString(e));
            }

            if (row_idx != -1 && col_idx != -1) {
              zoneDetect[row_idx][col_idx] = true;
              zoneDetectTemp[row_idx][col_idx] = true;
            }
          }
        } //if (zone_params_arr != null && zone_params_arr.length > 0)

      } //if (zone_params != null && !zone_params.isEmpty())
    } //if (zone_params != null && zone_params.startsWith(str_zone))
  }

  private void recalcDefaultScreenSize() {
    DisplayMetrics displaymetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    if (displaymetrics.widthPixels > displaymetrics.heightPixels) {
      default_screen_height = displaymetrics.heightPixels;
      default_screen_width = displaymetrics.widthPixels;
    } else {
      default_screen_height = displaymetrics.widthPixels;
      default_screen_width = displaymetrics.heightPixels;
    }
    // // Log.d(TAG, "Default screen size: default width, default height: " +
    //   default_screen_width + ", " + default_screen_height);

    if (mMovieView != null) {
      if (default_screen_height * ratio > default_screen_width) {
        default_width = default_screen_width;
        default_height = (int) (default_width / ratio);
      } else {
        default_height = default_screen_height;
        default_width = (int) (default_height * ratio);
      }
    }
    // // Log.d(TAG, "Recalculate default size: width: " + default_width + ", height: " + default_height);
  }

  private LeftSideMenuImageAdapter leftSideMenuAdpt;

  /**
   * Split UI and Video Viewing
   */
  /* keep them on */
  private void showSideMenusAndStatus() {
    /* cancel any timeout running */
    cancelFullscreenTimer();

    // Show status bar
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    RelativeLayout leftSideMenu = (RelativeLayout) findViewById(R.id.left_side_menu);
    if (leftSideMenu != null) {
      leftSideMenu.clearAnimation();
      leftSideMenu.setVisibility(View.VISIBLE);
    }

    // // also show the joystick
    ImageView direction_pad = (ImageView) findViewById(R.id.directionPad);
    if (direction_pad != null
    /* && direction_indicator != null */) {
      direction_pad.clearAnimation();
      direction_pad.setVisibility(View.VISIBLE);
    }

  }

  private boolean isFullScreen;
  private ScreenTimeOutRunnable timeOut;

  private void goToFullScreen() {
    isFullScreen = true;

    RelativeLayout leftSideMenu = (RelativeLayout) findViewById(R.id.left_side_menu);
    if (leftSideMenu != null && leftSideMenu.isShown()) {
      fade_out_view(leftSideMenu, 1000);
    }

    // // also show the joystick
    ImageView direction_pad = (ImageView) findViewById(R.id.directionPad);
    // ImageView direction_indicator = (ImageView)
    // findViewById(R.id.directionInd);
    if (direction_pad != null
    /* && direction_indicator != null */) {
      fade_out_view(direction_pad, 1000);
      // fadeOutView(direction_indicator, 1000);
    }

    // Hide status bar
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    // exit subfunctions as well.
    // backKeyToGoBack = false;

    RelativeLayout pttLayout = (RelativeLayout) findViewById(R.id.pttLayout);
    if (pttLayout != null && pttLayout.isShown()) {
      fade_out_view(pttLayout, 1000);
    }

    RelativeLayout recMenu = (RelativeLayout) findViewById(R.id.rec_menu);
    if (recMenu != null && recMenu.isShown()) {
      fade_out_view(recMenu, 1000);
    }
  }

  private void fade_out_view(View v, int duration_ms) {
    Animation myFadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeout);
    myFadeAnimation.setDuration(duration_ms);
    myFadeAnimation.setAnimationListener(new FadeOutAnimationAndGoneListener(v));
    v.startAnimation(myFadeAnimation);
  }

  private void cancelFullscreenTimer() {

		/* cancel any timeout running */
    if (timeOut != null) {
      timeOut.setCancel(true);
      _timeOut.interrupt();
      try {
        _timeOut.join(1000);
      } catch (InterruptedException e) {
      }
      timeOut = null;
    }
  }

  private void tryToGoToFullScreen() {

    cancelFullscreenTimer();

		/* Start a 10sec remoteVideoTimer and made menus disappear */
    timeOut = new ScreenTimeOutRunnable(this, new Runnable() {

      @Override
      public void run() {

        goToFullScreen();
      }
    });
    _timeOut = new Thread(timeOut, "Screen Timeout");
    _timeOut.start();

  }

  private void setupSideMenu() {
    RelativeLayout leftSideMenu = (RelativeLayout) findViewById(R.id.left_side_menu);

		/* build the grid base on given size */
    GridView gridview = (GridView) leftSideMenu.findViewById(R.id.slide_content);
    gridview.setAdapter(null);
    if (leftSideMenuAdpt == null) {
      leftSideMenuAdpt = new LeftSideMenuImageAdapter(this, PublicDefine.shouldEnableMic(selected_channel.getCamProfile().getModelId()), PublicDefine.shouldEnablePanTilt(selected_channel.getCamProfile().getModelId()), true
      );
    }

    gridview.setAdapter(leftSideMenuAdpt);
    gridview.invalidateViews();

    gridview.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

          case MotionEvent.ACTION_DOWN:
            showSideMenusAndStatus();
            break;
          case MotionEvent.ACTION_UP:
            for (int i = 0; i < ((GridView) v).getChildCount(); i++) {
              View img = ((GridView) v).getChildAt(i);
              img.dispatchTouchEvent(MotionEvent.obtain(0, System.currentTimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            }

            tryToGoToFullScreen();
            break;
          default:

            break;
        }

				/* return false means we don't handle the event */
        return false;
      }
    });
    // ... and the handlers
    gridview.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, final View v, int position, long id) {

        switch (position) {
//				case LeftSideMenuImageAdapter.pos_menu:
//
//					break;

          case LeftSideMenuImageAdapter.pos_rec:// Trigger recording
            if (!selected_channel.getCamProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_MBP36N)) {
              onTriggerRecording();
            }

            break;
//				case LeftSideMenuImageAdapter.pos_imode:
//					if (selected_channel.getCamProfile().getModelId()
//							!= CamProfile.MODEL_ID_CAMERA_SERVER)
//					{
//						onPlayModeSwitch(v);
//					}
//					break;
//				case LeftSideMenuImageAdapter.pos_vox_settings:
//					if (selected_channel.getCamProfile().getModelId()
//							!= CamProfile.MODEL_ID_CAMERA_SERVER)
//					{
//						onAlarmSettings();
//					}
//					break;
          case LeftSideMenuImageAdapter.pos_melody: // melody
            break;
          case LeftSideMenuImageAdapter.pos_mic:
            break;
//				case LeftSideMenuImageAdapter.pos_cam_spk:
//					break;
//				case LeftSideMenuImageAdapter.pos_highquality:
//					if (selected_channel.getCamProfile().getModelId()
//							!= CamProfile.MODEL_ID_CAMERA_SERVER)
//					{
//						onHqSwitch(v);
//					}
//					break;
//				case LeftSideMenuImageAdapter.pos_motion:
//					if (selected_channel.getCamProfile().getModelId()
//							!= CamProfile.MODEL_ID_CAMERA_SERVER)
//					{
//						onMotionSetting(v);
//					}
          default:
            break;
        }
      }
    });

  }


  private void resetMotionSetting() {
    for (int i = 0; i < motion_rows; i++) {
      for (int j = 0; j < motion_cols; j++) {
        zoneDetectTemp[i][j] = zoneDetect[i][j];
      }
    }
  }

  private void updateMotionSetting() {
    for (int i = 0; i < motion_rows; i++) {
      for (int j = 0; j < motion_cols; j++) {
        zoneDetect[i][j] = zoneDetectTemp[i][j];
      }
    }
  }


  private boolean in720pMode = true;
  private boolean iFrameOnlyMode = false;

  /**
   *
   */
  protected void onTriggerRecording() {

    // isUpdatingResolution = true;
    // skip 10 frames when updating resolution
    // numberOfFrameToSkip = 0;

    Thread worker = new Thread() {
      public void run() {
        boolean send_via_udt = false;
        if (selected_channel != null) {
          if (!selected_channel.getCamProfile().isInLocal()) {
            send_via_udt = true;
          }
        }
        boolean isTriggered = settings.getBoolean(PublicDefineGlob.PREFS_TRIGGER_RECORDING, false);
        String request = PublicDefineGlob.RECORDING_STAT_MODE_OFF;
        if (isTriggered == true) {
          request = PublicDefineGlob.SET_RECORDING_STAT_CMD + PublicDefineGlob.SET_RECORDING_STAT_PARAM_1 + PublicDefineGlob.RECORDING_STAT_MODE_OFF;
          isTriggered = false;
        } else {
          request = PublicDefineGlob.SET_RECORDING_STAT_CMD + PublicDefineGlob.SET_RECORDING_STAT_PARAM_1 + PublicDefineGlob.RECORDING_STAT_MODE_ON;
          isTriggered = true;
        }

        if (send_via_udt == true) {
          request = PublicDefineGlob.BM_HTTP_CMD_PART + request;
          // // Log.d(TAG, "set recording stat cmd: " + request);
          String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
          if (saved_token != null) {
            UDTRequestSendRecvTask.sendRequest_via_stun2(saved_token, selected_channel.getCamProfile().get_MAC(), request);
          }

        } else {
          request = PublicDefineGlob.HTTP_CMD_PART + request;
          final String device_address_port = device_ip + ":" + device_port;
          String http_addr = String.format("%1$s%2$s%3$s", "http://", device_address_port, request);
          // // Log.d(TAG, "set recording stat cmd: " + http_addr);
          HTTPRequestSendRecvTask.sendRequest_block_for_response(http_addr, PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, http_pass);
        }

        settings.putBoolean(PublicDefineGlob.PREFS_TRIGGER_RECORDING, isTriggered);

        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            updateTriggerRecordingIcon();
          }

        });

      }
    };

    worker.start();
  }

  private void updateTriggerRecordingIcon() {
    boolean isTriggered = settings.getBoolean(PublicDefineGlob.PREFS_TRIGGER_RECORDING, false);

    if (isTriggered == true) {
      // VGA - HQ enabled
      leftSideMenuAdpt.setEnableRec(true);
    } else {
      leftSideMenuAdpt.setEnableRec(false);
    }

    // Refresh the ui
    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        RelativeLayout leftMenu = (RelativeLayout) findViewById(R.id.left_side_menu);
        if (leftMenu != null) {
          GridView gridview = (GridView) leftMenu.findViewById(R.id.slide_content);
          gridview.invalidateViews();

        }

      }
    });

  }

  private void queryHQStatus() {

    Thread worker = new Thread() {
      public void run() {

        String command = PublicDefineGlob.BM_HTTP_CMD_PART + PublicDefineGlob.GET_RESOLUTION_CMD;

        String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

        if (selected_channel != null) {

          try {
            SendCommandResponse resp = Device.sendCommand(saved_token, PublicDefineGlob.strip_colon_from_mac(selected_channel.getCamProfile().get_MAC()), command
            );

            // if not 200 switch back..
            if (resp != null && resp.getStatus() == 200) {
              // get_resolution: [720p,480p]

              String camera_response = resp.getSendCommandResponseData().getDevice_response().getBody();

              //resolution levels are different between blinkhd & blinkhd1.1

              if (camera_response.startsWith(PublicDefineGlob.GET_RESOLUTION_CMD)) {
                String resolution = camera_response.substring(PublicDefineGlob.GET_RESOLUTION_CMD.length() + 2);

                //for 1937 camera
                if (resolution.equalsIgnoreCase(PublicDefineGlob.RESOLUTION_480P)) {
                  // in720pMode = false;
                  currentResolutionIdx = 0;
                } else if (resolution.equalsIgnoreCase(PublicDefineGlob.RESOLUTION_720P_10)) {
                  // in720pMode = true;
                  currentResolutionIdx = 1;
                } else if (resolution.equalsIgnoreCase(PublicDefineGlob.RESOLUTION_720P_15)) {
                  currentResolutionIdx = 2;
                } else {
                  // // Log.e(TAG, "INVALID resolution: " + resolution);
                }


              } //if (camera_response.startsWith(PublicDefine.GET_RESOLUTION_CMD))
              else {
                // // Log.e(TAG, "INVALID get_resolution response: " + camera_response);
              }

            }

          } catch (SocketTimeoutException e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
          } catch (MalformedURLException e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
          } catch (IOException e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
          }

        }

        // updateHQIcon();
        updateResolution(currentResolutionIdx);

      }
    };

    worker.start();

  }


  private void queryTriggerRecordingStatus() {

    Thread worker = new Thread() {

      public void run() {
        boolean isTriggered = false;
        boolean send_via_udt = false;
        if (selected_channel != null) {
          if (!selected_channel.getCamProfile().isInLocal()) {
            send_via_udt = true;
          }
        }

        String resp = null;
        if (send_via_udt == true) {
          String command = PublicDefineGlob.BM_HTTP_CMD_PART + PublicDefineGlob.GET_RECORDING_STAT_CMD;

          String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

          resp = UDTRequestSendRecvTask.sendRequest_via_stun2(saved_token, selected_channel.getCamProfile().get_MAC(), command
          );
        } else {
          String command = PublicDefineGlob.HTTP_CMD_PART + PublicDefineGlob.GET_RECORDING_STAT_CMD;
          final String device_address_port = device_ip + ":" + device_port;
          String http_addr = String.format("%1$s%2$s%3$s", "http://", device_address_port, command);
          // // Log.d(TAG, "get recording stat cmd: " + http_addr);
          resp = HTTPRequestSendRecvTask.sendRequest_block_for_response(http_addr, PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, http_pass);
        }

        if (resp != null && resp.startsWith(PublicDefineGlob.GET_RECORDING_STAT_CMD)) {
          String recording_stat = resp.substring(PublicDefineGlob.GET_RECORDING_STAT_CMD.length() + 2);
          if (recording_stat.equalsIgnoreCase(PublicDefineGlob.RECORDING_STAT_MODE_ON)) {
            isTriggered = true;
          } else if (recording_stat.equalsIgnoreCase(PublicDefineGlob.RECORDING_STAT_MODE_OFF)) {
            isTriggered = false;
          }
        }

        settings.putBoolean(PublicDefineGlob.PREFS_TRIGGER_RECORDING, isTriggered);

        // updateHQIcon();
        updateTriggerRecordingIcon();
      }
    };

    worker.start();

  }


  private void queryMelodyStatus() {

    Thread worker = new Thread() {

      public void run() {
        int melodyIdx = 0;
        boolean send_via_udt = false;
        if (selected_channel != null) {
          if (!selected_channel.getCamProfile().isInLocal()) {
            send_via_udt = true;
          }
        }

        String resp = null;
        if (send_via_udt == true) {
          String command = PublicDefineGlob.BM_HTTP_CMD_PART + PublicDefineGlob.GET_MELODY_VALUE;

          String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

          resp = UDTRequestSendRecvTask.sendRequest_via_stun2(saved_token, selected_channel.getCamProfile().get_MAC(), command
          );
        } else {
          String command = PublicDefineGlob.HTTP_CMD_PART + PublicDefineGlob.GET_MELODY_VALUE;
          final String device_address_port = device_ip + ":" + device_port;
          String http_addr = String.format("%1$s%2$s%3$s", "http://", device_address_port, command);
          // // Log.d(TAG, "get melody value cmd: " + http_addr);
          resp = HTTPRequestSendRecvTask.sendRequest_block_for_response(http_addr, PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, http_pass);
        }

        if (resp != null && resp.startsWith(PublicDefineGlob.GET_MELODY_VALUE)) {
          String str_melody_idx = resp.substring(PublicDefineGlob.GET_MELODY_VALUE.length() + 2);

          try {
            melodyIdx = Integer.parseInt(str_melody_idx);
            updateMelodyIcon(melodyIdx);
          } catch (NumberFormatException e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
          }
        }

      }
    };

    worker.start();

  }

  protected void onStop() {
    ACTIVITY_HAS_STOPPED = true;

    super.onStop();
    // // Log.d(TAG, TAG + " onStop... + hide movieView");

    stopAllThread();

    if (wl != null && wl.isHeld()) {
      wl.release();
      wl = null;
      // // Log.d("mbp", "FFMpegPlayerActivity stopped - release WakeLock");
    }

  }

  protected void onDestroy() {
    // // Log.d(TAG, TAG + " onDestroy...");
    // Close the player properly here.
    super.onDestroy();
  }

  /********** View preparation **********/

  /**
   * GLOBAL - Send ViewCam Req to BMS to ask for camera url and start
   * streaming
   *
   * @selected_channel has to be not - null
   */

  private void prepareToViewCameraRemotely(final boolean shouldIgnoreErrorAndRetry) {
    if (selected_channel == null) {
      return; // STH is WRONG!!!!
    }

		/*
     * In some cases, if we show the dialog and the whole remote view
		 * process failed quickly which lead to calling this function multiple
		 * times -> the dialog will be flashing on/off
		 * 
		 * One case we know for sure is when there is no SSID & no IP, i.e. no
		 * Connection
		 */
    boolean shouldShowConnectingDialog = true;

    // Simple check to see if we use wifi or other data network
    if (ConnectToNetworkActivity.haveInternetViaOtherMedia(FFMpegPlayerActivity.this)) {
      // Can't do anything.
    } else {

      WifiManager w = (WifiManager) getSystemService(WIFI_SERVICE);
      if (w.getConnectionInfo() != null) {
        String curr_ssid = w.getConnectionInfo().getSSID();
        // // Log.e("mbp", "prepareToViewCameraRemotely:Before GET STREAM  SSID: " + curr_ssid + " ip is: " + w.getConnectionInfo().getIpAddress()
        //);

        if ((curr_ssid == null) && (w.getConnectionInfo().getIpAddress() == 0)) // NO IP
        // address
        {
          shouldShowConnectingDialog = false;
        }

      }
    }

    selected_channel.getCamProfile().setRemoteCommMode(Streaming.STREAM_MODE_HTTP_REMOTE);

//    RtmpStreamTask viewUpnpTask = new RtmpStreamTask(new Handler(FFMpegPlayerActivity.this), FFMpegPlayerActivity.this);
    RtmpStream2Task viewUpnpTask = new RtmpStream2Task(new Handler(FFMpegPlayerActivity.this), FFMpegPlayerActivity.this);

    selected_channel.setCurrentViewSession(CamChannel.REMOTE_RELAY_VIEW);
    selected_channel.setViewReqState(viewUpnpTask);

    String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

    String clientType = "browser";
    viewUpnpTask.execute(selected_channel.getCamProfile().get_MAC(), saved_token, clientType);

    if ((shouldIgnoreErrorAndRetry == false) && shouldShowConnectingDialog) {
      // // Log.d(TAG, "showing dialog");
      try {
        showDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
      } catch (Exception e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      }
    }

    settings.putString(PublicDefineGlob.PREFS_CAM_BEING_VIEWED, selected_channel.getCamProfile().get_MAC());

		/*

         * 20120906: clear all alert for this camera

		 * AlertData.clearAlertForCamera(selected_channel.getCamProfile()
		 * .get_MAC(), getExternalFilesDir(null));
		 * 
		 * if (remoteVideoTimer != null) { // // Log.d("mbp",
		 * "cancel current VideoTimeoutTask"); remoteVideoTimer.cancel();
		 * remoteVideoTimer = null; }
		 * 
		 * remoteVideoTimer = new Timer(); remoteVideoTimer.schedule(new
		 * VideoTimeoutTask(), VIDEO_TIMEOUT);
		 */

  }

  /**
   * ******** Dialogs *****************
   */

  public static final int DIALOG_CONNECTION_FAILED = 2;

  public static final int DIALOG_BMS_CONNECTION_IN_PROGRESS = 6;

  public static final int DIALOG_REMOTE_BM_IS_OFFLINE = 8;
  public static final int DIALOG_CAMERA_PORT_IS_INACCESSIBLE = 10;

  public static final int DIALOG_VIDEO_STOPPED_UNEXPECTEDLY = 12;
  public static final int DIALOG_WIFI_CANT_RECONNECT = 13;

  public static final int DIALOG_REMOTE_VIDEO_STREAM_TIMEOUT = 15;
  public static final int DIALOG_REMOTE_VIDEO_STREAM_STOPPED_UNEXPECTEDLY = 16;
  public static final int DIALOG_REMOTE_BM_IS_BUSY = 17;

  public static final int DIALOG_NEED_TO_LOGIN = 22;

  public static final int DIALOG_STORAGE_UNAVAILABLE = 24;

  public static final int DIALOG_STORAGE_NOT_ENOUGH_FREE_SPACE_FOR_SNAPSHOT = 27;
  public static final int DIALOG_STORAGE_NOT_ENOUGH_FREE_SPACE_FOR_VIDEO = 28;

  public static final int DIALOG_BMS_GET_STREAM_MODE_ERROR = 31;
  public static final int DIALOG_FAILED_TO_UNMUTE_CAM_AUDIO = 32;
  public static final int DIALOG_UDT_RELAY_CONNECTION_IN_PROG = 33;

  public static final int DIALOG_FW_PATCH_FOUND = 36;
  public static final int DIALOG_BMS_UPDATE_FAILED_TRY_AGAIN = 37;
  public static final int DIALOG_VIDEO_RECORDING_MODE_NO_CAMERA_SNAPSHOT_ALLOW = 38;

  public static final int DIALOG_SESSION_KEY_MISMATCHED = 41;
  public static final int DIALOG_CAMERA_IS_NOT_AVAILABLE = 42;

  public static final int DIAGLOG_JSON_ERROR_CODE_9011 = 9011;

  public static final int DONT_RESCAN_CAMERA = 100;
  public static final int RESCAN_CAMERA = 101;

  protected Dialog onCreateDialog(int id) {
    AlertDialog.Builder builder;
    AlertDialog alert;
    ProgressDialog dialog;
    Spanned msg;
    switch (id) {
      case DIALOG_CAMERA_PORT_IS_INACCESSIBLE:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.camera_port_is_inaccessible));
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                setResult(RESCAN_CAMERA);
                finish();

              }
            }
        ).setOnCancelListener(new OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {

          }
        });

        alert = builder.create();
        return alert;

      case DIALOG_CONNECTION_FAILED:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_conn_failed_wifi));
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                setResult(RESCAN_CAMERA);
                finish();
              }
            }
        ).setOnCancelListener(new OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {

          }
        });

        alert = builder.create();
        return alert;

      case DIALOG_BMS_CONNECTION_IN_PROGRESS:
        dialog = new ProgressDialog(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_connecting_to_bm) + "</big>");
        dialog.setMessage(msg);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);

        dialog.setOnCancelListener(new OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            userWantToCancel = true;

            cancelVideoStoppedReminder();

            // release WakeLock if it's held
            if (wl != null && wl.isHeld()) {
              wl.release();
              wl = null;
              // // Log.d("mbp", "release WakeLock");
            }

            setResult(RESCAN_CAMERA);
            finish();
          }
        });

        dialog.setButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
              }
            }
        );

        return dialog;

      case DIALOG_REMOTE_BM_IS_OFFLINE:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_cant_reach_cam_2) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                setResult(RESCAN_CAMERA);
                finish();

              }
            }
        ).setOnCancelListener(new OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {

          }
        });

        alert = builder.create();
        return alert;

      case DIALOG_VIDEO_STOPPED_UNEXPECTEDLY:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_no_signal_1) + "</big>");
        builder.setMessage(msg).setCancelable(true).setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

              }
            }
        ).setOnCancelListener(new OnCancelListener() {

          @Override
          public void onCancel(DialogInterface dialog) {
            userWantToCancel = true;
            try {
              dismissDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
            } catch (Exception ie) {

            }

            try {
              dismissDialog(DIALOG_UDT_RELAY_CONNECTION_IN_PROG);
            } catch (Exception e) {
            }

            stopAllThread();
            cancelVideoStoppedReminder();

            setResult(RESCAN_CAMERA);
            finish();
          }
        });

        alert = builder.create();
        return alert;

      case DIALOG_WIFI_CANT_RECONNECT:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_no_signal_2) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                setResult(RESCAN_CAMERA);
                finish();
              }
            }
        );

        alert = builder.create();
        return alert;

      case DIALOG_REMOTE_VIDEO_STREAM_TIMEOUT:

        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_no_signal_8) + "</big>");
        builder.setMessage(msg).setCancelable(false).setPositiveButton(getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (selected_channel != null) {
                  // cancel current remote connection
                  selected_channel.cancelRemoteConnection();
                }
                // RESTART new session

					/*
           * 20130201: hoang: issue 1260 turn on
					 * screen while preparing to view remotely
					 */
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "TURN ON Because of error"
                );
                wl.setReferenceCounted(false);
                wl.acquire();
                // // Log.d("mbp", "Acquire WakeLock for prepare to view remotely");
                //TODO : needed?
                //prepareToViewCameraRemotely(true);

                // Play tone
                if (_playTone != null && _playTone.isAlive()) {
                  // // Log.d("mbp", "stop play Tone thread now");
                  playTone.stopPlaying();
                  _playTone.interrupt();
                  _playTone = null;
                }
              }
            }
        ).setNegativeButton(getResources().getString(R.string.No), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // GO back to camera selection screens
					/*
					 * Don't rescan, just build base on the list
					 * we have
					 */
                stopAllThread();

                // Play tone
                if (_playTone != null && _playTone.isAlive()) {
                  // // Log.d(TAG, "stop play Tone thread now");
                  playTone.stopPlaying();
                  _playTone.interrupt();
                  _playTone = null;
                }

                // cancel current remote connection
                selected_channel.cancelRemoteConnection();

                setResult(RESCAN_CAMERA);
                finish();
              }
            }
        );

        alert = builder.create();
        return alert;
      case DIALOG_REMOTE_VIDEO_STREAM_STOPPED_UNEXPECTEDLY:

        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_no_signal_1) + "</big>");
        builder.setMessage(msg).setCancelable(true).setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
              }
            }
        ).setOnCancelListener(new OnCancelListener() {

          @Override
          public void onCancel(DialogInterface dialog) {
            userWantToCancel = true;
            try {
              dismissDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
            } catch (Exception e) {
            }

            try {
              dismissDialog(DIALOG_UDT_RELAY_CONNECTION_IN_PROG);
            } catch (Exception e) {
            }

            // Stop popup thread
            if (_playTone != null && _playTone.isAlive()) {
              // // Log.d("mbp", "Stop popup thread  now");
              playTone.stopPlaying();
              _playTone.interrupt();
              _playTone = null;
            }

            selected_channel.cancelRemoteConnection();

            stopAllThread();
            cancelVideoStoppedReminder();

            setResult(RESCAN_CAMERA);
            finish();
          }
        });

        alert = builder.create();
        return alert;

      case DIALOG_REMOTE_BM_IS_BUSY:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_no_signal_5) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setResult(RESCAN_CAMERA);
                finish();
              }
            }
        );

        alert = builder.create();
        return alert;

      case DIALOG_NEED_TO_LOGIN:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_not_login) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            }
        );

        alert = builder.create();
        return alert;

      case DIALOG_STORAGE_UNAVAILABLE:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getString(R.string.usb_storage_is_turned_on_please_turn_off_usb_storage_before_launching_the_application) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Intent homeScreen = new Intent(FFMpegPlayerActivity.this, FirstTimeActivity.class);
                homeScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeScreen);
                FFMpegPlayerActivity.this.finish();

              }
            }
        );

        alert = builder.create();
        return alert;

      case DIALOG_STORAGE_NOT_ENOUGH_FREE_SPACE_FOR_SNAPSHOT:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getString(R.string.there_is_not_enough_space_to_store_the_snapshot_please_remove_some_files_and_try_again_) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            }
        );

        alert = builder.create();
        return alert;

      case DIALOG_STORAGE_NOT_ENOUGH_FREE_SPACE_FOR_VIDEO:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getString(R.string.application_needs_free_storage_space_of_at_least_100mb_to_start_recording_) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            }
        );

        alert = builder.create();
        return alert;
      case DIALOG_BMS_GET_STREAM_MODE_ERROR:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_no_signal_10) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setResult(RESCAN_CAMERA);
                finish();

              }
            }
        );

        alert = builder.create();
        return alert;

      case DIALOG_UDT_RELAY_CONNECTION_IN_PROG:
        dialog = new ProgressDialog(this);
        msg = Html.fromHtml(getResources().getString(R.string.connecting_through_relay_please_wait_) + "</big>");
        dialog.setMessage(msg);
        dialog.setIndeterminate(true);

        return dialog;
      case DIALOG_FW_PATCH_FOUND:
        builder = new AlertDialog.Builder(this);
        // TODO Get it from CamProfile or CamChannel
        final String device_version = "Unknown"; // device.device_version;
        msg = Html.fromHtml(getString(R.string.camera_firmware_upgrade_available));
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (selected_channel != null) {
                  // stop all before upgrade
                  stopAllThread();
                  cancelVideoStoppedReminder();
                  // remove_ScreenTimeout_br();

                  // this task is to display the %
                  String _portal_usrName = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, null);
                  String _portal_usrPwd = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_PWD, null);
                  CheckVersionFW test = new CheckVersionFW(FFMpegPlayerActivity.this, new Handler(FFMpegPlayerActivity.this), true, device_version, selected_channel.getCamProfile().get_MAC(), _portal_usrName, _portal_usrPwd
                  );

                  test.execute(device_version, String.valueOf(device_port), PublicDefineGlob.HTTP_CMD_PART, CheckVersionFW.REQUEST_FW_UPGRADE);

                }
              }
            }
        ).setNegativeButton(getResources().getString(R.string.No), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            }
        );

        alert = builder.create();
        return alert;
      case DIALOG_BMS_UPDATE_FAILED_TRY_AGAIN:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getString(R.string.update_status_failed_please_try_again_) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            }
        );

        alert = builder.create();

        return alert;

      case DIALOG_VIDEO_RECORDING_MODE_NO_CAMERA_SNAPSHOT_ALLOW:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getString(R.string.DIALOG_VIDEO_RECORDING_MODE_NO_CAMERA_SNAPSHOT_ALLOW) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            }
        );
        alert = builder.create();
        return alert;
      case DIALOG_SESSION_KEY_MISMATCHED:

        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.the_session_key_on_camera_is_mismatched) + "</big>");
        builder.setMessage(msg).setCancelable(false).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // GO back to camera selection screens
									/*
									 * Don't rescan, just build base on the list
									 * we have
									 */
                stopAllThread();

                // Play tone
                if (_playTone != null && _playTone.isAlive()) {
                  // // Log.d("mbp", "stop play Tone thread now");
                  playTone.stopPlaying();
                  _playTone.interrupt();
                  _playTone = null;
                }

                selected_channel.cancelRemoteConnection();

                setResult(RESCAN_CAMERA);
                finish();
              }
            }
        );

        alert = builder.create();
        return alert;

      case DIALOG_CAMERA_IS_NOT_AVAILABLE:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.camera_is_not_available_please_make_sure_that_it_is_turned_on) + "</big>");
        builder.setMessage(msg).setCancelable(false).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // GO back to camera selection screens
                stopAllThread();

                // Play tone
                if (_playTone != null && _playTone.isAlive()) {
                  // // Log.d("mbp", "stop play Tone thread now");
                  playTone.stopPlaying();
                  _playTone.interrupt();
                  _playTone = null;
                }

                selected_channel.cancelRemoteConnection();

                setResult(RESCAN_CAMERA);
                finish();

              }
            }
        );

        alert = builder.create();
        return alert;

      /****** ERROR CODE FROM JSON SERVER ****/

      case DIAGLOG_JSON_ERROR_CODE_9011: {
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getString(R.string.unable_to_create_session_error_parsing_response_from_the_device_the_response_from_device_is_not_in_expected_format_) + "</big>");

        builder.setMessage(msg).setCancelable(false).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // GO back to camera selection screens
                stopAllThread();

                // Play tone
                if (_playTone != null && _playTone.isAlive()) {
                  // // Log.d("mbp", "stop play Tone thread now");
                  playTone.stopPlaying();
                  _playTone.interrupt();
                  _playTone = null;
                }

                finish();

              }
            }
        );

        alert = builder.create();
        return alert;
      }
      default: // Unknown error

        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml(getResources().getString(R.string.EntryActivity_no_signal_6) + "</big>");
        builder.setMessage(msg).setCancelable(false).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // GO back to camera selection screens
                stopAllThread();

                // Play tone
                if (_playTone != null && _playTone.isAlive()) {
                  // // Log.d("mbp", "stop play Tone thread now");
                  playTone.stopPlaying();
                  _playTone.interrupt();
                  _playTone = null;
                }

                finish();

              }
            }
        );

        alert = builder.create();
        return alert;

    }

  }


  /**
   * ** Retries and beep Tones ***
   */
  private Thread _playTone;
  private Thread _timeOut;
  private Thread _outOfRange;

  private PlayTone playTone;
  private VideoOutOfRangeReminder outOfRange;
  private boolean ACTIVITY_HAS_STOPPED;

  private LocalScanForCameras scan_task;

  private WakeLock wl;
  private String string_currentSSID = "string_currentSSID";

  private IpAndVersion device;

  /* Connection Constant */
  public static final int CONNECTION_MODE_LOCAL_INFRA = 1;
  public static final int CONNECTION_MODE_REMOTE = 2;

  private int currentConnectionMode;
  private int device_audio_in_port;
  private BabyMonitorAuthentication bm_session_auth;

  private void setupRemoteCamera(CamChannel s_channel, BabyMonitorAuthentication bm_auth) {
    currentConnectionMode = CONNECTION_MODE_REMOTE;

    if (bm_auth != null) {
      device_ip = bm_auth.getIP();
      device_port = bm_auth.getPort();
      bm_session_auth = bm_auth;// reserved to used later if we need to
      // restart the videostreamer -audio only
      // mode
    }

    try {
      http_pass = CameraPassword.getPasswordforCam(getExternalFilesDir(null), s_channel.getCamProfile().get_MAC()
      );
    } catch (Exception e) {
      // // Log.d("mbp", e.getLocalizedMessage());
      showDialog(DIALOG_STORAGE_UNAVAILABLE);
      return;
    }

    device_audio_in_port = s_channel.getCamProfile().get_ptt_port();

    filePath = s_channel.getStreamUrl();
    setupFFMpegPlayer(this.isConnectingForTheFirstTime);

  }

  private void stopAllThread() {

    if (mMovieView != null) {
      try {
        mMovieView.setVisibility(View.GONE);
      } catch (Exception e) {
      }
      ;

      if (!mMovieView.isReleasingPlayer()) {
        mMovieView.release();
      }
    }

    settings.remove(PublicDefineGlob.PREFS_CAM_BEING_VIEWED);

    if (scan_task != null && scan_task.getScanStatus() != LocalScanForCameras.SCAN_CAMERA_FINISHED) {
			/* if it's either PENDING or RUNNING */
      // // Log.d("mbp", "cancel SCAN task");
      scan_task.stopScan();
      // wait for this task to be canccel completely
      try {
        // at most 2 seconds
        Thread.sleep(2100);
      } catch (Exception e) {
        // // Log.d("mbp", "Exception while waiting for scan task to end");
        // // Log.e(TAG, Log.getStackTraceString(e));
      }
    }

    // stop WifiScan task
    if (ws != null && ws.getStatus() == AsyncTask.Status.RUNNING) {
      ws.cancel(true);
    }

  }

  private void cancelVideoStoppedReminder() {
    boolean retry = true;
    if (_outOfRange != null && _outOfRange.isAlive()) {
      // // Log.d("mbp", "stop alarm tone thread now");
      outOfRange.stop();
			/* try to interrupt with this thread is sleeping */
      _outOfRange.interrupt();
      while (retry) {
        try {
          _outOfRange.join(5000);

          retry = false;
        } catch (InterruptedException e) {
        }
      }
      _outOfRange = null;
      outOfRange = null;
    }
  }

  private void videoHasStoppedUnexpectedly() {
    this.runOnUiThread(new Runnable() {

      @Override
      public void run() {

        if (mMovieView != null && !mMovieView.isReleasingPlayer()) {
          // Stop players
          mMovieView.release();
        }
        // Decide whether Router disconnects or Camera disconnect

        if (userWantToCancel == true) {
          return;
        }

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);

        String ssid_no_quote = settings.getString(string_currentSSID, null);

        if (wm.getConnectionInfo() != null && wm.getConnectionInfo().getSSID() != null && wm.getConnectionInfo().getSSID().equalsIgnoreCase(ssid_no_quote)) {
          // Still on the same network --> camera down
          // // Log.d("mbp", "Wifi SSID is still the same, camera is probably down ");

          // re-scann
          scan_task = new LocalScanForCameras(FFMpegPlayerActivity.this, new OneCameraScanner());
          scan_task.setShouldGetSnapshot(false);
          // Callback: updateScanResult()
          // setup scanning for just 1 camera -
          // // Log.d("mbp", "setup scanning for just 1 camera - ");
          scan_task.startScan(new LegacyCamProfile[]{selected_channel.getCamProfile()});

        } else // Router down
        {
          // // Log.d("mbp", "Wifi SSID is not the same, Router is probably down ");
          MiniWifiScanUpdater iw = new MiniWifiScanUpdater();
          ws = new WifiScan(FFMpegPlayerActivity.this, iw);
          ws.setSilence(true);
          ws.execute("Scan now");
        }

        if (outOfRange == null || !_outOfRange.isAlive()) {
          // // Log.d("mbp", "start Reminder now!");
          outOfRange = new VideoOutOfRangeReminder();
          _outOfRange = new Thread(outOfRange, "outOfRange");
          _outOfRange.start();
        } else {
          // // Log.d("mbp", "reminder is running... dont start another one");
        }

        displayBG(true);

      }
    });
  }

  private void remoteVideoHasStopped(int reason) {

    stopAllThread();

    // just to be cautious - but should not happen
    cancelVideoStoppedReminder();

    if (userWantToCancel) {
      return;
    }

    displayBG(true);

    switch (reason) {
      case Streaming.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY:
        // // Log.d("mbp", "remote- video is stopped unexpectedly.");
        this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // Play tone -start if not start
            if (_playTone == null || !_playTone.isAlive()) {
              playTone = new PlayTone(Streaming.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY);
              _playTone = new Thread(playTone);
              _playTone.start();
            } else {
              // // Log.d("mbp", "PlayTone is running.. dont start another one");
            }

            // 20120509: auto-relink..???
            // // Log.d("mbp", "Auto - relink remote camera ");

					/*
					 * 20130201: hoang: issue 1260 turn on screen while
					 * preparing to view remotely
					 */
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "TURN ON Because of error"
            );
            wl.setReferenceCounted(false);
            wl.acquire();
            // // Log.d("mbp", "Acquire WakeLock for prepare to view remotely");
            // when failed retry
            prepareToViewCameraRemotely(true);
          }
        });

        break;
      case Streaming.MSG_VIDEO_STREAM_HAS_STOPPED_FROM_SERVER:

        final Runnable showDialog = new Runnable() {
          @Override
          public void run() {
            // Play tone -start if not start
            if (_playTone == null || !_playTone.isAlive()) {
              playTone = new PlayTone(Streaming.MSG_VIDEO_STREAM_HAS_STOPPED_FROM_SERVER);
              _playTone = new Thread(playTone);
              _playTone.start();
            }

          }
        };
        runOnUiThread(showDialog);
        break;
    }
  }

  private void updateAuthenticationObject(BabyMonitorAuthentication bm) {
    // stub function
  }

  public static final int MSG_LONG_TOUCH = 0xDEADBEEF;
  public static final int MSG_LONG_TOUCH_START = 0xDEADBEEE;

  public static final int MSG_LONG_TOUCH_RELEASED = 0xCAFEBEEF;
  public static final int MSG_SHORT_TOUCH_RELEASED = 0xCAFEBEED;
  public static final int MSG_PCM_RECORDER_ERR = 0xDEADDEAD;
  public static final int MSG_SURFACE_CREATED = 0xBABEBABE;
  public static final int MSG_ZOOM_EVENT = 0xCAFECAFE;


  @Override
  public boolean handleMessage(Message msg) {

    // // // Log.d(TAG, "Got mesg: "+ String.format("%08x", msg.what)+ " arg1: " +
    // msg.arg1 + " arg2: " + msg.arg2 );

    switch (msg.what) {

      case Streamer.MSG_VIDEO_SIZE_CHANGED:
        final int video_width = msg.arg1;
        final int video_height = msg.arg2;
        ratio = (float) video_width / video_height;
        // // Log.d(TAG, "Video width, height, ratio: " + video_width + ", " + video_height + ", " + ratio);
        recalcDefaultScreenSize();

        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            resizeFFMpegView();

            //update UI
            String textResolution = String.format("%dx%d", video_width, video_height);
            TextView resolutionView = (TextView) findViewById(R.id.textResolution);
            resolutionView.setText(textResolution);
          }
        });

        break;
      case Streamer.MSG_SESSION_KEY_MISMATCHED: {
        final Runnable showDialog = new Runnable() {
          @Override
          public void run() {
            // Should happen during remote-access only
            if (wl != null && wl.isHeld()) {
              wl.release();
              wl = null;
              // // Log.d("mbp", "MSG_SESSION_KEY_MISMATCHED - release WakeLock");
            }
            stopAllThread();
            cancelVideoStoppedReminder();

            try {
              dismissDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
            } catch (Exception e) {
            }

            try {
              dismissDialog(DIALOG_UDT_RELAY_CONNECTION_IN_PROG);
            } catch (Exception e) {
            }

            try {

              showDialog(DIALOG_SESSION_KEY_MISMATCHED);
            } catch (Exception ie) {
            }

          }
        };
        runOnUiThread(showDialog);
        break;
      }

      case Streamer.MSG_CAMERA_IS_NOT_AVAILABLE: {

        if (isConnectingForTheFirstTime == true) {
          final Runnable showDialog = new Runnable() {
            @Override
            public void run() {
              // Should happen during remote-access only
              if (wl != null && wl.isHeld()) {
                wl.release();
                wl = null;
                // // Log.d("mbp", "MSG_CAMERA_IS_NOT_AVAILABLE - release WakeLock");
              }
              stopAllThread();
              cancelVideoStoppedReminder();

              try {
                dismissDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
              } catch (Exception e) {

              }

              try {
                dismissDialog(DIALOG_UDT_RELAY_CONNECTION_IN_PROG);
              } catch (Exception e) {
              }

              try {
                showDialog(DIALOG_CAMERA_IS_NOT_AVAILABLE);
              } catch (Exception ie) {
              }

            }
          };
          runOnUiThread(showDialog);
        } else {
          runOnUiThread(new Runnable() {
            public void run() {
              Handler dummy = new Handler(FFMpegPlayerActivity.this);
              dummy.dispatchMessage(Message.obtain(dummy, Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY));
            }
          });
        }
        break;
      }

      case Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY: {
        isConnectingForTheFirstTime = false;

        if (wl != null && wl.isHeld()) {
          wl.release();
          wl = null;
          // // Log.d("mbp", "MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY - release WakeLock");
        }

        if (userWantToCancel == false) {
          if (ACTIVITY_HAS_STOPPED == false) {
            try {
              dismissDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
            } catch (Exception e) {
            }

            try {
              dismissDialog(DIALOG_UDT_RELAY_CONNECTION_IN_PROG);
            } catch (Exception e) {
            }

            // GET this when VideoStream about to end unexpectedly

            {

              if (FFMpegPlayerActivity.this.selected_channel.getCamProfile().isInLocal()) {
                videoHasStoppedUnexpectedly();
              } else if (FFMpegPlayerActivity.this.selected_channel.getCamProfile().isReachableInRemote()) {
                remoteVideoHasStopped(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY);
              }

            }

          } //if ACTIVITY_HAS_STOPPED == false
          else {
            // // Log.d(TAG, "Activity has stopped, do nothing here...");
          }
        } else {
          FFMpegPlayerActivity.this.finish();
        }

        break;
      }
      case Streamer.MSG_VIDEO_STREAM_HAS_STOPPED:
        if (wl != null && wl.isHeld()) {
          wl.release();
          wl = null;
          // // Log.d("mbp", "MSG_VIDEO_STREAM_HAS_STOPPED - release WakeLock");
        }
        break;

      case Streamer.MSG_VIDEO_STREAM_HAS_STARTED:

        if (!userWantToCancel) {
          isConnectingForTheFirstTime = false;

          queryHQStatus();
          queryTriggerRecordingStatus();
          queryMelodyStatus();

          if (wl != null && wl.isHeld()) {
            wl.release();
            wl = null;
            // // Log.d("mbp", "MSG_VIDEO_STREAM_HAS_STARTED - release WakeLock");
          }

          cancelVideoStoppedReminder();

          //shouldBeepAndShow = 0;

          if (_playTone != null && _playTone.isAlive()) {
            // // Log.d("mbp", "MSG_VIDEO_STREAM_HAS_STARTED: stop play Tone thread now");
            playTone.stopPlaying();
            _playTone.interrupt();
            _playTone = null;
          }

          try {
            dismissDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
          } catch (Exception e) {
          }

          try {
            dismissDialog(DIALOG_UDT_RELAY_CONNECTION_IN_PROG);
          } catch (Exception e) {
          }

          if (msg.obj != null) {
            final BabyMonitorAuthentication auth = (BabyMonitorAuthentication) msg.obj;
            runOnUiThread(new Runnable() {

              @Override
              public void run() {
                updateAuthenticationObject(auth);

              }
            });

          }
        } else {
          FFMpegPlayerActivity.this.finish();
        }

        break;

      case Streamer.MSG_VIDEO_STREAM_INTERNAL_ERROR: {

        if (wl != null && wl.isHeld()) {
          wl.release();
          wl = null;
          // // Log.d("mbp", "MSG_VIDEO_STREAM_INTERNAL_ERROR - release WakeLock");
        }

        try {
          dismissDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
        } catch (Exception e) {
        }

        try {
          dismissDialog(DIALOG_UDT_RELAY_CONNECTION_IN_PROG);
        } catch (Exception e) {
        }

        final int err = msg.arg1;

        // // Log.d("mbp", "MSG_VIDEO_STREAM_INTERNAL_ERROR : err: " + err);

        if (selected_channel.getCamProfile().isInLocal()) {
          // // Log.e("mbp", " call  videoHasStoppedUnexpectedly");
          videoHasStoppedUnexpectedly();
        } else if (selected_channel.getCamProfile().isReachableInRemote()) {
          // // Log.e("mbp", " call  remoteVideoHasStopped");
          remoteVideoHasStopped(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY);
        }

        break;
      }

      case Streamer.MSG_RTSP_VIDEO_STREAM_BITRATE_BPS: {
        final int byte_p_sec = msg.arg1;

        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            TextView frameRate = (TextView) findViewById(R.id.textFrameRate);
            // // // Log.d(TAG, "kBps:" + ((double)byte_p_sec)/1024);

            String bitrate = String.format("%.1f kB/s", ((double) byte_p_sec) / 1024);
            if (frameRate != null) {
              frameRate.setText(bitrate);
            }

          }
        });

        break;
      }

      // //UI thread .. UI Thread ..
      case RemoteStreamTask.MSG_VIEW_CAM_SUCCESS:

        if (!userWantToCancel) {
          BabyMonitorAuthentication bm_auth = (BabyMonitorAuthentication) msg.obj;
          if (selected_channel != null) {

            if (selected_channel.setStreamingState() == true) {

              InetAddress remote_addr;
              try {

                remote_addr = InetAddress.getByName(bm_auth.getIP());

                selected_channel.getCamProfile().setInetAddr(remote_addr);
                selected_channel.getCamProfile().setPort(bm_auth.getPort());

                try {
                  dismissDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
                } catch (Exception e) {
                }

                try {
                  dismissDialog(DIALOG_UDT_RELAY_CONNECTION_IN_PROG);
                } catch (Exception ie) {
                }

                if (bm_auth != null) {
                  String stream_url = bm_auth.getStreamUrl();
                  selected_channel.setStreamUrl(stream_url);
                }

                setupRemoteCamera(selected_channel, bm_auth);

              } catch (UnknownHostException e) {
                // // Log.e(TAG, Log.getStackTraceString(e));

							/* can't find the host */
                showDialog(DIALOG_CONNECTION_FAILED);
              }
            } else {
						/*
						 * 20130201: hoang: issue 1260 turn off screen when
						 * finish view request
						 */
              if (wl != null && wl.isHeld()) {
                wl.release();
                wl = null;
                // // Log.d("mbp", "MSG_VIEW_CAM_SUCCESS - release WakeLock");
              }
            }
          }
        } // end if (!userWantToCancel)
        else {
          FFMpegPlayerActivity.this.finish();
        }

        break;

      case RemoteStreamTask.MSG_VIEW_CAM_FALIED:

        if (!userWantToCancel) {

          int status = msg.arg1;
          int code = msg.arg2;

          // // Log.d(TAG, "Can't get Session Key status: " + status + " code: " + code);

          try {
            dismissDialog(DIALOG_BMS_CONNECTION_IN_PROGRESS);
          } catch (Exception e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
          }

          try {
            dismissDialog(DIALOG_UDT_RELAY_CONNECTION_IN_PROG);
          } catch (Exception e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
          }

				/*
				 * 20130201: hoang: issue 1260 turn off screen when finish
				 * view request
				 */
          if (wl != null && wl.isHeld()) {
            wl.release();
            wl = null;
            // // Log.d("mbp", "MSG_VIEW_CAM_FAILED - release WakeLock");
          }

          // showDialog(DIALOG_CAMERA_PORT_IS_INACCESSIBLE);

          switch (status) {

            case 500: // BlinkHD
              try {
                showDialog(code);
              } catch (Exception e1) {
                // // Log.e(TAG, Log.getStackTraceString(e1));
              }
              break;

            default:
              try {
                showDialog(DIALOG_REMOTE_BM_IS_OFFLINE);
              } catch (Exception e) {
                // // Log.e(TAG, Log.getStackTraceString(e));
              }
              break;
          }

          selected_channel.cancelRemoteConnection();

          if (_playTone != null && _playTone.isAlive()) {
            // // Log.d("mbp", "MSG_VIDEO_STREAM_HAS_STARTED: stop play Tone thread now");
            playTone.stopPlaying();
            _playTone.interrupt();
            _playTone = null;
          }

        } else {
          FFMpegPlayerActivity.this.finish();
        }

        break;

      case CheckVersionFW.PATCH_AVAILABLE:
        device = (IpAndVersion) msg.obj;
        String device_ip = device.device_ip;
        // // Log.d("mbp", "device ip >>> " + device_ip);
        if (device_ip != null) {
          if (selected_channel != null && selected_channel.getCamProfile() != null && selected_channel.getCamProfile().get_inetAddress().getHostAddress().equalsIgnoreCase(device_ip)) {
            try {
              this.showDialog(DIALOG_FW_PATCH_FOUND);
            } catch (IllegalArgumentException ie) {
            } catch (BadTokenException ie) {
            }
          } else {
            // // Log.d("mbp", "FW PATCH response available but from a different IP or Channel is NULL");
          }
        }
        break;
      case CheckVersionFW.UPGRADE_DONE:
        // // Log.d("mbp", "case UPGRADE_DONE>>>");
        // show a dialog to wait 80s

        Spanned message = Html.fromHtml(getString(R.string.upgrade_done_camera_is_rebooting_please_wait_for_about_1_minute_));
        final ProgressDialog dialog_wait = new ProgressDialog(this);
        dialog_wait.setMessage(message);
        dialog_wait.setIndeterminate(true);
        dialog_wait.setCancelable(false);
        dialog_wait.show();
        Handler hl = new Handler();
        hl.postDelayed(new Runnable() {
          @Override
          public void run() {
            dialog_wait.dismiss();

            setResult(RESCAN_CAMERA);
            finish();

          }
        }, 80000);
        break;

      case CheckVersionFW.UPGRADE_FAILED:
        // // Log.d("mbp", "UPGRADE_FAILED");
        AlertDialog.Builder builder;
        AlertDialog alert;
        Spanned message1;
        builder = new AlertDialog.Builder(this);
        message1 = Html.fromHtml(getString(R.string.upgrade_fw_failed) + "</big>");
        builder.setMessage(message1).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setResult(RESCAN_CAMERA);
                finish();
              }
            }
        );

        alert = builder.create();
        alert.show();
        break;

      default:
        break;
    }
    return false;
  }

  private android.graphics.Bitmap video_background;

  private void displayBG(boolean shouldDisplay) {
    Canvas c = null;
    SurfaceHolder _surfaceHolder = mMovieView.getHolder();
    if (_surfaceHolder == null) {
      // // Log.w("mbp", "_surfaceHolder is NULL");
      return;
    }

    try {
      c = _surfaceHolder.lockCanvas(null);
      if (c == null) {
        return;
      }
			/*
			 * video_background = Bitmap.createScaledBitmap(video_background,
			 * Math.min(c.getWidth(),video_background.getWidth()),
			 * Math.min(c.getHeight(),video_background.getHeight()),false);
			 */

			/*
			 * 20130603: hoang: release the native heap
			 */
      if (video_background != null) {
        video_background.recycle();
        video_background = null;
      }

      if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        video_background = BitmapFactory.decodeResource(this.getResources(), R.drawable.homepage);
      } else {
        video_background = BitmapFactory.decodeResource(this.getResources(), R.drawable.homepage_p);
      }
      video_background = Bitmap.createScaledBitmap(video_background, c.getWidth(), c.getHeight(), false);

      synchronized (_surfaceHolder) {
        // c.drawBitmap(background,0,0,null);

        if (shouldDisplay) {
          c.drawBitmap(video_background, 0, 0, null);
        } else {
          c.drawColor(Color.BLACK);
        }
      }
    } finally {
      // do this in a finally so that if an exception is thrown
      // during the above, we don't leave the Surface in an
      // inconsistent state
      if (c != null) {
        _surfaceHolder.unlockCanvasAndPost(c);
      }
    }

  }

  /**
   * *******************************************************************************************
   * ********************************* PRIVATE CLASSES *******************************************
   * ********************************************************************************************
   */

  private class VideoOutOfRangeReminder implements Runnable {

    private boolean running;

    public VideoOutOfRangeReminder() {
      running = true;
    }

    @Override
    public void run() {
			/* Play beep: preparing */
      MediaPlayer mMediaPlayer = new MediaPlayer();
      String uri = "android.resource://" + getPackageName() + "/" + R.raw.beep;
      try {
        mMediaPlayer.setDataSource(FFMpegPlayerActivity.this, Uri.parse(uri));
      } catch (IOException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      }

      mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);

      try {
        mMediaPlayer.prepare();
      } catch (IllegalStateException e1) {
        mMediaPlayer = null;
        // // Log.e(TAG, Log.getStackTraceString(e1));
      } catch (IOException e1) {
        mMediaPlayer = null;
        // // Log.e(TAG, Log.getStackTraceString(e1));
      }

			/* turn on the screen only once when we start.. */
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      if (!pm.isScreenOn()) {
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "TURN ON Because of error"
        );
        wl.setReferenceCounted(false);
        wl.acquire(10000);
      }

      // // Log.d("mbp", "Thread:" + Thread.currentThread().getId() + ":Beeping in local router mode start");
      while (running) {

        FFMpegPlayerActivity.this.runOnUiThread(new Runnable() {

          @Override
          public void run() {

            displayBG(true);

            try {
              showDialog(DIALOG_VIDEO_STOPPED_UNEXPECTEDLY);
            } catch (Exception ie) {
              // // Log.e(TAG, Log.getStackTraceString(ie));
            }
          }
        });

        try {
          Thread.sleep(5500);
        } catch (InterruptedException e) {
        }

        if (ACTIVITY_HAS_STOPPED == true) {
          // // Log.d("mbp", "Stop Beeping in local coz activity is stopped.");
          break;
        }

        // 20120511_: move the beeping to after 5 sec
        if (mMediaPlayer != null) {
          if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
          }

          mMediaPlayer.start();
        }

      } // while running
      // // Log.d("mbp", "Thread:" + Thread.currentThread().getId() + ":Beeping in local router mode stop");

      if ((mMediaPlayer != null) && mMediaPlayer.isPlaying()) {
        mMediaPlayer.stop();
      }

      runOnUiThread(new Runnable() {

        @Override
        public void run() {

          try {
            removeDialog(DIALOG_VIDEO_STOPPED_UNEXPECTEDLY);
          } catch (Exception e) {
          }
        }
      });

    }

    public void stop() {
      running = false;
    }

  }

  private class MiniWifiScanUpdater implements IWifiScanUpdater {

    @Override
    public void scanWasCanceled() {
      // Do nothing here for now
    }

    @Override
    public void updateWifiScanResult(List<ScanResult> results) {
      if (results == null) {
        return;
      }
      String check_SSID = settings.getString(string_currentSSID, null);
      String check_SSID_w_quote = "\"" + check_SSID + "\"";
      boolean found_in_range = false;
      for (ScanResult result : results) {
        if ((result.SSID != null) && (result.SSID.equalsIgnoreCase(check_SSID))) {
          // // Log.d("mbp", "found " + check_SSID + " .. in range");
          found_in_range = true;
          break;
        }
      }

      if (found_in_range) {
				/* try to connect back to this BSSID */
        WifiManager w = (WifiManager) getSystemService(WIFI_SERVICE);
        List<WifiConfiguration> wcs = w.getConfiguredNetworks();

        Handler.Callback h = new Handler.Callback() {

          @Override
          public boolean handleMessage(Message msg) {
            switch (msg.what) {
              case ConnectToNetworkTask.MSG_CONNECT_TO_NW_DONE:

                runOnUiThread(new Runnable() {

                  @Override
                  public void run() {
                    setupFFMpegPlayer(false);

                  }
                });

                break;
              case ConnectToNetworkTask.MSG_CONNECT_TO_NW_FAILED:
                // /Scan again
                ws = new WifiScan(FFMpegPlayerActivity.this, new MiniWifiScanUpdater());
                ws.setSilence(true);
                ws.execute("Scan now");

                break;
            }
            return false;
          }
        };

        ConnectToNetworkTask connect_task = new ConnectToNetworkTask(FFMpegPlayerActivity.this, new Handler(h));
        connect_task.dontRemoveFailedConnection(true);
        connect_task.setIgnoreBSSID(true);
        boolean foundExisting = false;
        for (WifiConfiguration wc : wcs) {
          if ((wc.SSID != null) && wc.SSID.equalsIgnoreCase(check_SSID_w_quote)) {
            // This task will make sure the app is connected to the
            // camera.
            // At the end it will send MSG_CONNECT_TO_NW_DONE
            connect_task.execute(wc);
            foundExisting = true;
            break;
          }
        }
        if (!foundExisting) {
          // popup dialog
          showDialog(DIALOG_WIFI_CANT_RECONNECT);

        }
      } else /* not found the SSID */ {

        // /Scan again
        ws = new WifiScan(FFMpegPlayerActivity.this, new MiniWifiScanUpdater());
        ws.setSilence(true);
        ws.execute("Scan now");
      }

    }
  }

  /**
   * @author phung This is to reconnect just 1 camera after scanning If no
   *         camera found -- rescan again until we found one or -- sbdy press
   *         stop
   */
  private class OneCameraScanner implements ICameraScanner {

    @Override
    public void updateScanResult(ScanProfile[] results, int status, int index) {

      if (userWantToCancel) {
        //do nothing, exit...
        return;
      }

      if (results != null && results.length == 1) {
        ScanProfile cp = results[0];
        // update the ip address
        LegacyCamProfile seletectProfile = selected_channel.getCamProfile();

        if (seletectProfile.get_MAC().equalsIgnoreCase(results[0].get_MAC())) {

          // Update the new IP
          seletectProfile.setInetAddr(results[0].get_inetAddress());
          seletectProfile.setPort(results[0].get_port());
          seletectProfile.setInLocal(true);

          // start connecting now..

          setupFFMpegPlayer(false);
          return;
        }
      } else {
        // // Log.d(TAG, "Failed to find camera via Scan");
      }

      // If the camera is not found -- send
      // the error message to trigger scanning
      // again .
      Handler dummy = new Handler(FFMpegPlayerActivity.this);
      dummy.dispatchMessage(Message.obtain(dummy, Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY));

    }
  }

  /**
   * PlayTone and also SHOW dialog in a while loop i.e. dialog showing is
   * called everytime ..
   *
   * @author phung
   */
  class PlayTone implements Runnable {
    private boolean isRunning;
    private int dialogId;

    /**
     * @param reason - VideoStreamer stopped reason i.e.:
     *               VideoStreamer.MSG_VIDEO_STREAM_HAS_STOPPED_FROM_SERVER OR
     *               VideoStreamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY
     */
    public PlayTone(int reason) {
      isRunning = true;

      dialogId = DIALOG_REMOTE_VIDEO_STREAM_STOPPED_UNEXPECTEDLY;
      if (reason == Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_FROM_SERVER) {
        dialogId = DIALOG_REMOTE_VIDEO_STREAM_TIMEOUT;
      }

    }

    public void stopPlaying() {
      isRunning = false;
    }

    public boolean isRunning() {
      return isRunning;
    }

    @Override
    public void run() {

      MediaPlayer mMediaPlayer = new MediaPlayer();
      String uri = "android.resource://" + getPackageName() + "/" + R.raw.beep;
      try {
        mMediaPlayer.setDataSource(FFMpegPlayerActivity.this, Uri.parse(uri));
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        mMediaPlayer.prepare();

      } catch (IOException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      }
      // // Log.d("mbp", "Thread:" + Thread.currentThread().getId() + ":PlayTone class " + "is running and showing dialog");

			/* turn on the screen for once */
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      if (!pm.isScreenOn()) {
        // // Log.d("mbp", "Turn on once ");
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "TURN ON Because of error"
        );
        wl.setReferenceCounted(false);
        wl.acquire(10000);

      }

      do {

        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            // // // Log.d("mbp","show both..bg & dialog");
            try {
              showDialog(dialogId);
            } catch (Exception e) {
            }

            displayBG(true);

          }

        });

        mMediaPlayer.start();

        try {
          Thread.sleep(5500);
        } catch (InterruptedException e) {
          // // Log.e(TAG, Log.getStackTraceString(e));
        }

        if (mMediaPlayer.isPlaying()) {
          mMediaPlayer.stop();
        }

      } while (isRunning);

      runOnUiThread(new Runnable() {

        @Override
        public void run() {
          try {
            dismissDialog(dialogId);
          } catch (Exception ie) {
          }
        }
      });

      // // Log.d("mbp", "Thread:" + Thread.currentThread().getId() + ":PlayTone class is stopped & dismiss dialog");
    }
  }

  /*
     * (non-Javadoc)
     *
     * @see com.msc3.IResUpdater#updateResolution(int)
     */
  @Override
  public void updateResolution(int index) {
    if (index >= 0 && index < 3) {
      currentResolutionIdx = index;

//			if (currentResolutionIdx == 0) {
//				leftSideMenuAdpt.setHQenable(false);
//			} 
//			else
//			{
//				leftSideMenuAdpt.setHQenable(true);
//			}
    }

    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        RelativeLayout leftMenu = (RelativeLayout) findViewById(R.id.left_side_menu);
        if (leftMenu != null) {
          GridView gridview = (GridView) leftMenu.findViewById(R.id.slide_content);
          gridview.invalidateViews();
        }

      }
    });
  }

  @Override
  public void updateMelodyIcon(int index) {
//		if (index >= 0) {
//			currentMelodyIndx = index;
//
//			leftSideMenuAdpt.setMelodyMuted((currentMelodyIndx == 0));
//		}

    runOnUiThread(new Runnable() {

      @Override
      public void run() {
        RelativeLayout leftMenu = (RelativeLayout) findViewById(R.id.left_side_menu);
        if (leftMenu != null) {
          GridView gridview = (GridView) leftMenu.findViewById(R.id.slide_content);
          gridview.invalidateViews();

        }

      }
    });
  }

}
