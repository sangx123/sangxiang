package com.media.ffmpeg.android;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.actors.Actor;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.models.StartRecordingMessage;
import com.hubble.devcomm.models.StopRecordingMessage;
import com.hubble.registration.Util;
import com.hubble.ui.ViewFinderFragment;
import com.hubble.util.P2pSettingUtils;

import com.media.ffmpeg.FFMpegPlayer;
import com.msc3.Streamer;
import com.nxcomm.jstun_android.P2pClient;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.hubbleconnected.camera.BuildConfig;
import com.hubbleconnected.camera.R;

public class FFMpegMovieViewAndroid extends SurfaceView implements VideoControllerView.MediaPlayerControl {
  public static final int VIDEO_STREAM_CONNECTION_TIMEOUT = 0;
  public static final int VIDEO_STREAM_STREAM_NOT_FOUND = 1;

 // VideoViewFragment mVideoViewFragment;
  ViewFinderFragment mParentFragment;

  /**
   * Handler for maintaining updateTimerThread used for manual recording to Camera.
   * Note we update the textView in VideoViewFragment when we return from StartRecording in our Actor.
   * This timer may not be accurate as to when the camera actually starts and stops its recording!
   */
  private Handler mRecordingTimeHandler = new Handler();

  private Actor actor = new Actor() {
    @Override
    public Object receive(Object m) {
      if (m != null) {
        if (m instanceof StartRecordingMessage) {
          Device p = (Device) ((StartRecordingMessage) m).getValue();
          final Pair<String, Object> response = p.sendCommandGetValue("start_sd_recording", null, null);
          runOnMainThread(new Runnable() {
            @Override
            public void run() {
              if (response != null && (Integer) response.second != -1) {
                recordingStartTimeMillis = SystemClock.uptimeMillis();
                if (mParentFragment != null){
                  Toast.makeText(getContext(), getContext().getString(R.string.camera_recording_started), Toast.LENGTH_SHORT).show();
                  mRecordingTimeHandler.post(updateTimerThread);
                }
              } else {
                Toast.makeText(getContext(), getContext().getString(R.string.recording_to_camera_failed), Toast.LENGTH_LONG).show();
              }
            }
          });
        } else if (m instanceof StopRecordingMessage) {
          Device p = (Device) ((StopRecordingMessage) m).getValue();
          final Pair<String, Object> response = p.sendCommandGetValue("stop_sd_recording", null, null);
          runOnMainThread(new Runnable() {
            @Override
            public void run() {
              if (response != null && (Integer) response.second != -1) {
                if (mParentFragment != null) {
                  Toast.makeText(getContext(), getContext().getString(R.string.camera_recording_stopped), Toast.LENGTH_SHORT).show();
                  mRecordingTimeHandler.removeCallbacks(updateTimerThread);
                }
              } else {
                Toast.makeText(getContext(), getContext().getString(R.string.failed_to_stop_camera_recording), Toast.LENGTH_LONG).show();
              }
            }
          });
        }
      }
      return null;
    }
  };

  private static final String TAG = "FFMpegMovieViewAndroid";

  private FFMpegPlayer mPlayer;
  private MediaController mMediaController;
  private VideoControllerView controller;

  private Context mContext;
  private String filePath;
  private Thread initializing_thrd = null;
  private Handler mHandler;
  private boolean inPlayBackMode = false;
  private boolean forSharedCam = false;
  private boolean isRtcpTcp = false;
  private boolean shouldShowDuration = true;
  private boolean isInitilizing = false;
  private boolean shouldInterrupt = false;
  private boolean isPlayerReleased = false;
  private Timer fadeOutTimer = null;
  private boolean isSurfaceDestroyed = false;
  private DateTimeFormatter fileNameFormat = DateTimeFormat.forPattern("MM_dd_yyyy_HHmmss");
  private P2pClient[] mP2pClients = null;

  public FFMpegMovieViewAndroid(Context context) {
    super(context);
    //setWillNotDraw(false);
    mContext = context;
    SurfaceHolder surfHolder = getHolder();
    surfHolder.addCallback(mSHCallback);
  }

  public FFMpegMovieViewAndroid(Context context, AttributeSet attrs) {
    super(context, attrs);
    //setWillNotDraw(false);
    mContext = context;
    SurfaceHolder surfHolder = getHolder();
    surfHolder.addCallback(mSHCallback);
  }

  public FFMpegMovieViewAndroid(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    //setWillNotDraw(false);
    mContext = context;
    SurfaceHolder surfHolder = getHolder();
    surfHolder.addCallback(mSHCallback);
  }

  /**
   * Update latest snapshot of camera
   * @param registrationId The registration id of camera.
   * @return true if updated successfully, otherwise false.
   */
  public boolean updateLatestSnapshot(String registrationId) {
    boolean isSucceeded = false;
    byte[] picture = null;
    int snapWidth = 0;
    int snapHeight = 0;
    /*
     * 20170301 HOANG
     * Fix crash due to mPlayer released while update latest snapshot
     * Need to synchronized it
     */
    synchronized (this) {
      if (mPlayer != null) {
        picture = mPlayer.native_getSnapShot();
        snapWidth = mPlayer.getVideoWidth();
        snapHeight = mPlayer.getVideoHeight();
      } else {
        Log.i(TAG, "Update latest snapshot failed, mPlayer is null");
      }
    }
    if (picture != null && picture.length > 0) {
      if (snapWidth > 0 && snapHeight > 0) {
        final Bitmap bitmap = Bitmap.createBitmap(snapWidth, snapHeight, Bitmap.Config.ARGB_8888);
        ByteBuffer buffer = ByteBuffer.wrap(picture);
        //to prevent "buffer not large enough for pixels" exception
        buffer.rewind();
        bitmap.copyPixelsFromBuffer(buffer);

        String latestSnapPath = Util.getLatestSnapshotPath(registrationId);
        Log.i(TAG, "Update latest snapshot, camera: " + registrationId + ", path: " + latestSnapPath + String.format(", w %d, h %d, length %d", snapWidth, snapHeight, picture.length));
        if (latestSnapPath != null) {
          File latestSnapFile = new File(latestSnapPath);
          try {
            FileOutputStream fos = new FileOutputStream(latestSnapFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            isSucceeded = true;
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      } else {
        Log.d(TAG, "Update latest snapshot failed, invalid dimension: width " + snapWidth + ", height: " + snapHeight);
      }
    } else {
      Log.i(TAG, "Update latest snapshot failed, mPlayer picture is null");
    }
    return isSucceeded;
  }

  public void getSnapShot(int snap_width, int snap_height, boolean shouldShowDialog,String filename) {
    if (mPlayer != null) {
      byte[] picture = null;
      picture = mPlayer.native_getSnapShot();
      if (picture != null && picture.length > 0) {
        final Bitmap bitmap = Bitmap.createBitmap(snap_width, snap_height, Bitmap.Config.ARGB_8888);
        ByteBuffer buffer = ByteBuffer.wrap(picture);
        bitmap.copyPixelsFromBuffer(buffer);

        //final String filename = fileNameFormat.print(DateTime.now());

        Util.insertImageToGallery(getContext(), bitmap, filename, getResources().getString(R.string.app_brand_application_name));

        if (shouldShowDialog) {
          post(new Runnable() {
            @Override
            public void run() {
              String text = mContext.getResources().getString(R.string.saved_photo);
              int duration = Toast.LENGTH_SHORT;
              Toast toast = Toast.makeText(mContext, text, duration);
              toast.show();
            }
          });

        }
      } else {
        if (shouldShowDialog) {
          post(new Runnable() {
            @Override
            public void run() {
              String text = getContext().getString(R.string.error_could_not_take_snapshot);
              int duration = Toast.LENGTH_SHORT;
              Toast toast = Toast.makeText(mContext, text, duration);
              toast.show();
            }
          });

        }
      }
    }
  }

  public void initVideoView(Handler mHandler, boolean forPlayBack, boolean forSharedCam) {
    this.forSharedCam = forSharedCam;
    inPlayBackMode = forPlayBack;
    this.forSharedCam = forSharedCam;
    this.mHandler = mHandler;
    isRtcpTcp = false;
  }

  public void initVideoView(Handler mHandler, boolean forPlayBack, boolean forSharedCam,boolean isTCP) {
    this.forSharedCam = forSharedCam;
    inPlayBackMode = forPlayBack;
    this.forSharedCam = forSharedCam;
    this.mHandler = mHandler;
    isRtcpTcp = isTCP;
  }

  public void setP2PInfo(P2pClient[] p2pClients) {
    this.mP2pClients = p2pClients;
  }

  public void enableAudio(Boolean isEnabled) {
    if (mPlayer != null) {
      mPlayer.setAudioStreamMuted(isEnabled);
    }
  }

  public void setShouldShowDuration(boolean shouldShow) {
    shouldShowDuration = shouldShow;
  }

  public void setDuration(int duration_msec) {
    if (mPlayer != null) {
      mPlayer.setDuration(duration_msec);
    }
  }

  public FFMpegPlayer getFFMpegPlayer() {
    return mPlayer;
  }

  public void setFFMpegPlayer(FFMpegPlayer player) {
    mPlayer = player;
  }

  public void setBufferSize(int bufferSizeKb) {
    if (mPlayer != null) {
      mPlayer.setBufferSize(bufferSizeKb);
      // // Log.d(TAG, "Set buffer size in to : " + bufferSizeKb + " kb");
    }
  }

  public void setFFMpegPlayerOptions(int mode) {
    if (mPlayer != null) {
      mPlayer.setPlayOption(mode);
    }
  }

  private void attachMediaController() {

    controller = new VideoControllerView(mContext);
    if (hideThumb) {
      controller.hideThumb();
    }

    controller.setMediaPlayer(this);
    View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
    controller.setAnchorView((ViewGroup) anchorView);
  }

  public void setFullProgressBar() {
    if (controller != null) {
      controller.setPlaybackFinished(true);
      controller.setProgressCompleted();
    }
  }

  public void setEncryptionInfo(String key_hex, String iv_hex) {
    if (mPlayer != null) {
      mPlayer.setEncryptionKey(key_hex);
      mPlayer.setEncryptionIv(iv_hex);
    }
  }

  public void setEncryptionEnable(boolean isEnabled) {
    if (mPlayer != null) {
      mPlayer.setEncryptionEnable(isEnabled);
    }
  }

  /**
   * @param filePath
   */
  public void setVideoPath(String filePath) {
    Log.d(TAG, "Set video path: " + filePath);
    this.filePath = filePath;

    Thread worker = new Thread(new Runnable() {

      @Override
      public void run() {

        mPlayer = new FFMpegPlayer(mHandler, inPlayBackMode, forSharedCam);
        isPlayerReleased = false;

        if (inPlayBackMode == true) {
          // in playback mode, add filepath to playlist
          ArrayList<String> playlist = new ArrayList<String>();
          playlist.add(FFMpegMovieViewAndroid.this.filePath);
          mPlayer.updatePlaylist(playlist);
        }

        if(BuildConfig.DEBUG)
          Log.d(TAG, "isRtcpTcp :- " + isRtcpTcp);

        if(isRtcpTcp)
        {
          try
          {
            mPlayer.setPlayOption(FFMpegPlayer.MEDIA_STREAM_RTSP_WITH_TCP);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }

        Log.d(TAG, "shouldShowDuration: " + shouldShowDuration);
        if (!shouldShowDuration) {
          try {
            mPlayer.setPlayOption(FFMpegPlayer.MEDIA_STREAM_SHOW_DURATION);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        boolean isInitializedSucceeded = false;
        int retries = 20;
        do {
          // Moves here from onSurfaceCreated
          Log.d(TAG, "isShown? " + isShown());
          if (getHolder() != null) {
            Log.d(TAG, "getHolder isValid? " + getHolder().getSurface().isValid());
          } else {
            Log.d(TAG, "getHolder null");
          }
          if (getHolder() != null && getHolder().getSurface().isValid() && isShown()) {

                        /* IMPORTANT!!! REMEMBER!!!
                         * Should post initializeVideo() to run on main thread to avoid thread-handling related issue.
                         * If run it on background thread, the surface could never be created again and this could cause unexpected issues.
                         * E.g. The SurfaceView is shown but the surface is not valid.
                         */
            post(new Runnable() {

              @Override
              public void run() {
                initializeVideo(getHolder());
              }
            });
            isInitializedSucceeded = true;
            break;
          } else {
            Log.d(TAG, "Surface is not valid yet, waiting...");
            try {
              // waiting more a bit longer to make sure surface is valid
              Thread.sleep(200);
            } catch (InterruptedException e) {
              // // Log.e(TAG, Log.getStackTraceString(e));
            }
          }
        } while (!isPlayerReleased && !isSurfaceDestroyed && retries-- > 0);

        if (isSurfaceDestroyed == true) {
          Log.i(TAG, "Surface has been destroyed, don't need to initialize video anymore.");
        }

        // send message not available to handler
        if (isInitializedSucceeded == false) {
          release();
          failedToInitVideo();
        }
      }
    });
    worker.start();
  }

  /**
   * initialize player
   * <p/>
   * prerequisite: filePath, mPlayer, SurfaceHolder
   */
  private void initializeVideo(SurfaceHolder surfHolder) {
    try {

      // if player is releasing, not need to initialize video anymore
      if (mPlayer != null && !shouldInterrupt) {
        // ORDER is important -- Set display before prepare()!!!
        mPlayer.setDisplay(surfHolder);
        if (mP2pClients != null) {
          mPlayer.setP2PInfo(mP2pClients);
          mPlayer.setP2pPlayByTimestampEnabled(P2pSettingUtils.getInstance().isP2pPlayByTimestampEnabled());
        }
        initializing_thrd = new Thread(new Runnable() {

          @Override
          public void run() {

            boolean isStreamNotFoundDetected = false;
            boolean hasInitialized = false;
            int retries = 3;

            while (retries > 0 && !shouldInterrupt) {
              synchronized (FFMpegMovieViewAndroid.this) {
                if (mPlayer == null) {
                  return;
                }

                isInitilizing = true;
                try {
                  mPlayer.setDataSource(filePath);
                  if (mPlayer != null) {
                    mPlayer.prepare();
                    if (!inPlayBackMode) {
                      mPlayer.setAudioStreamMuted(true);
                    }
                    hasInitialized = true;
                  } else {
                    hasInitialized = false;
                  }
                  isInitilizing = false;

                  break;
                } catch (IllegalArgumentException | IllegalStateException e) {
                  mHandler.sendMessage(Message.obtain(mHandler, Streamer.MSG_VIDEO_STREAM_STATUS, VIDEO_STREAM_CONNECTION_TIMEOUT, -1));
                  Log.e(TAG, "Couldn't prepare player: " + e.getMessage());
                } catch (IOException e) {
                  mHandler.sendMessage(Message.obtain(mHandler, Streamer.MSG_VIDEO_STREAM_STATUS, VIDEO_STREAM_STREAM_NOT_FOUND, -1));
                  Log.e(TAG, "IO Exception Couldn't prepare player: " + e.getMessage());
                }
                isInitilizing = false;

                int waiting = 0;
                while (waiting++ < 3 && !shouldInterrupt) {
                  try {
                    Thread.sleep(1000);
                  } catch (InterruptedException e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                  }
                }

                Log.e(TAG, "Initializing player failed...Retries: " + retries);
                retries--;
              }
            }

            if (!shouldInterrupt) {
              if (hasInitialized) {
                // Initialize successfully
                startVideo();
              } else {
                release();
                // Initialize failed
                failedToStartVideo();
              }
            } else {
              shouldInterrupt = false;
            }
          }

        });
        initializing_thrd.start();
      }

    } catch (IllegalStateException | IllegalArgumentException e) {
      Log.e(TAG, "Couldn't prepare player: " + e.getMessage());
    } catch (IOException e) {
      Log.e(TAG, "IO Exception Couldn't prepare player: " + e.getMessage());
    }
  }

  public void showOptions() {
    if (inPlayBackMode) {
      if (controller != null) {
        controller.show();
      }

      final ImageView imgFullClose = (ImageView) ((Activity) mContext).findViewById(R.id.imgCloseFull);
      final LinearLayout layoutOpt = (LinearLayout) ((Activity) mContext).findViewById(R.id.layoutOption);
      final TextView txtDone = (TextView) ((Activity) mContext).findViewById(R.id.txtPlaybackDone);
      final int orientation = mContext.getResources().getConfiguration().orientation;
      if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        imgFullClose.setVisibility(View.VISIBLE);
      } else {
        txtDone.setVisibility(View.VISIBLE);
      }
      layoutOpt.setVisibility(View.VISIBLE);
    }
  }

  public void hideOptions() {
    if (inPlayBackMode) {
      if (controller != null) {
        controller.hide();
      }

      final ImageView imgFullClose = (ImageView) ((Activity) mContext).findViewById(R.id.imgCloseFull);
      final LinearLayout layoutOpt = (LinearLayout) ((Activity) mContext).findViewById(R.id.layoutOption);
      final TextView txtDone = (TextView) ((Activity) mContext).findViewById(R.id.txtPlaybackDone);
      imgFullClose.setVisibility(View.VISIBLE);
      txtDone.setVisibility(View.VISIBLE);
      layoutOpt.setVisibility(View.VISIBLE);
    }
  }

  private void startVideo() {
    if (inPlayBackMode) {
      ((Activity) mContext).runOnUiThread(new Runnable() {

        @Override
        public void run() {
          attachMediaController();
        }
      });
    }

    // could be stopping now..

    synchronized (this) {
      //setBufferSize(200); // 5
      mPlayer.start();
    }

  }

  private void failedToStartVideo() {
    mHandler.sendMessage(Message.obtain(mHandler, Streamer.MSG_CAMERA_IS_NOT_AVAILABLE));
  }

  private void failedToInitVideo() {
    mHandler.sendMessage(Message.obtain(mHandler, Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY));
  }

  public boolean isReleasingPlayer() {
    return shouldInterrupt;
  }

  public void release() {
    isPlayerReleased = true;
    if (mPlayer != null) // && mPlayer.isPlaying())
    {
      Log.d(TAG, "releasing ...");
      shouldInterrupt = true;
      mPlayer.suspend();
      synchronized (this) {
        try {
          mPlayer.stop();
          while (isInitilizing) {
            try {
              Thread.sleep(500);
            } catch (InterruptedException e) {
            }
          }
          mPlayer.release();
          mPlayer = null;
        } catch (Exception e) {
        } finally {
          if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
          }
        }
      }
      shouldInterrupt = false;
      Log.d(TAG, "player released");
    }
  }

  public boolean onTouchEvent(android.view.MotionEvent event) {
    // if(mMediaController != null && !mMediaController.isShowing()) {
    // mMediaController.show(3000);
    // }

    if (inPlayBackMode) {
      final ImageView imgFullClose = (ImageView) ((Activity) mContext).findViewById(R.id.imgCloseFull);
      final LinearLayout layoutOpt = (LinearLayout) ((Activity) mContext).findViewById(R.id.layoutOption);
      final TextView txtDone = (TextView) ((Activity) mContext).findViewById(R.id.txtPlaybackDone);
      final int orientation = mContext.getResources().getConfiguration().orientation;

      if (controller != null) {
        if (!controller.isShowing()) {
          controller.show(3000);
          if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imgFullClose.setVisibility(View.VISIBLE);
          } else {
            txtDone.setVisibility(View.VISIBLE);
          }
          layoutOpt.setVisibility(View.VISIBLE);

          fadeOutTimer = new Timer();
          fadeOutTimer.schedule(new TimerTask() {

            @Override
            public void run() {

              post(new Runnable() {

                @Override
                public void run() {

                  imgFullClose.setVisibility(View.INVISIBLE);
                  txtDone.setVisibility(View.INVISIBLE);
                  layoutOpt.setVisibility(View.INVISIBLE);
                }
              });

            }
          }, 3000);
        } //if (!controller.isShowing())
        else {
          if (fadeOutTimer != null) {
            fadeOutTimer.cancel();
            fadeOutTimer = null;
          }
          controller.hide();
          imgFullClose.setVisibility(View.INVISIBLE);
          txtDone.setVisibility(View.INVISIBLE);
          layoutOpt.setVisibility(View.INVISIBLE);
        }
      }

    }

    return false;
  }

  SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
      Log.d(TAG, "Surface created...");
      isSurfaceDestroyed = false;
      //AA-1376:
      if (mPlayer != null) {
        try {
          mPlayer.setDisplay(holder);
        } catch (IOException e) {
          e.printStackTrace();
          Log.d(TAG, "Surface created...mPlayer setDisplay get Exception: " + e.getMessage());
        }
      }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
      Log.d(TAG, "Surface destroyed...");
      isSurfaceDestroyed = true;
      //AA-1376: BACKGROUND MONITORING FOR ANDROID
      //In case none of BACKGROUND MONITORING, release player immediately
      if (mParentFragment != null && !mParentFragment.isInBGMonitoring()) {
        try {
          release();
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
      boolean retry = true;
      if (initializing_thrd != null && initializing_thrd.isAlive()) {
        shouldInterrupt = true;
        while (retry) {
          try {
            initializing_thrd.join(2000);
            retry = false;
          } catch (InterruptedException e) {
          }
        }
        initializing_thrd = null;
      }

      if (mMediaController != null && mMediaController.isShowing()) {
        mMediaController.hide();
      }
    }
  };

  @Override
  public void start() {
    if (mPlayer != null) {
      mPlayer.resume();
    }
  }

  @Override
  public void pause() {
    if (mParentFragment!=null)
      mRecordingTimeHandler.removeCallbacks(updateTimerThread);
    if (mPlayer != null) {
      try {
        mPlayer.pause();
      } catch (IllegalStateException e) {

        // // Log.e(TAG, Log.getStackTraceString(e));
      }
    }
  }

  @Override
  public int getDuration() {
    int result;
    if (mPlayer != null) {
      result = mPlayer.getDuration();
    } else {
      result = 0;
    }
    return result;
  }

  @Override
  public int getCurrentPosition() {
    int result;
    if (mPlayer != null) {
      //result = mPlayer.getCurrentPosition();
      try {
        result = mPlayer.getCurrentPosition();
      } catch (IllegalStateException e) {

        // // Log.e(TAG, Log.getStackTraceString(e));
        result = 0;
      }
    } else {
      result = 0;
    }
    return result;
  }

  @Override
  public void seekTo(int pos) {
    if (mPlayer != null) {
//      try {
//        ((FFMpegPlaybackActivity) mContext).showSpinner();
//        mPlayer.seekTo(pos);
//      } catch (IllegalStateException e) {
//        ((FFMpegPlaybackActivity) mContext).hideSpinner();
//      }
    }
  }

  @Override
  public boolean isPlaying() {
    boolean result = false;
    if (mPlayer != null) {
      try {
        result = mPlayer.isPlaying();
      } catch (IllegalStateException e) {}
    }
    return result;
  }

  @Override
  public int getBufferPercentage() {
    return 0;
  }

  @Override
  public boolean canPause() {

    return true;
  }

  @Override
  public boolean canSeekBackward() {
    return false;
  }

  @Override
  public boolean canSeekForward() {
    return false;
  }

  @Override
  public boolean isFullScreen() {

    return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

  }

  @Override
  public void toggleFullScreen() {

    // // Log.d(TAG, "Toggle Fullscreen");

    if (isFullScreen()) {
      ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      // ((Activity)
      // mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    } else {
      ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
  }

  /**
   * Start/Stop recording
   * <p/>
   * if recording is already started - dont start again
   * <p/>
   * The recording will stop automatically if the MovieView is destroyed /
   * hidenn/ remove from view
   *
   * @param isEnable - true _Start recording - false stop recording
   */
  public void startRecord(boolean isEnable, boolean isPhoneStorage, Device camProfile, final String fileUriString, final Fragment videoViewFragment) {
    mParentFragment = (ViewFinderFragment)videoViewFragment;
    if (mPlayer != null && fileUriString != null && fileUriString.split("/").length > 0) {
      if (isPhoneStorage) {
        if (isEnable) {
          mPlayer.startRecording(fileUriString);
        } else {
          mPlayer.stopRecord();

          post(new Runnable() {
            @Override
            public void run() {
              String[] uriParts = fileUriString.split("/");
              final String fileName = uriParts[uriParts.length - 1];

              File mFile = new File(fileUriString);
              if(mFile.length()/1024 > 70) {//Download only if video size is greater than 70kb
                DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                manager.addCompletedDownload(fileName, getContext().getString(R.string.event_video), true, "video/x-flv", fileUriString, mFile.length(), false);
              }
            }
          });

        }
      } else {
        if (isEnable) {
          actor.send(new StartRecordingMessage(camProfile));
        } else {
          actor.send(new StopRecordingMessage(camProfile));
        }
      }
    }
  }

  public boolean isRecording() {
    if (mPlayer != null) {
      return mPlayer.isRecording();
    }

    return false;
  }

  public void checkAndFlushAllBuffers() {
    if (mPlayer != null) {
      mPlayer.checkAndFlushAllBuffers();
    }
  }

  public void flushAllBuffers() {
    if (mPlayer != null) {
      mPlayer.flushAllBuffers();
    }
  }

  // used for keeping track of the recording timer
  // during manual recording to SD Card
  private long recordingTimeInMillis = 0L;
  private long recordingStartTimeMillis = 0L;

  /**
   * Runnable used to update the textView in VideoViewFragment
   * during manual recording to the SD Card.
   *
   * Called from mRecordingTimeHandler
   */
  private Runnable updateTimerThread = new Runnable() {
    @Override
    public void run() {
      recordingTimeInMillis = SystemClock.uptimeMillis() - recordingStartTimeMillis;
      if (mParentFragment != null){
        mParentFragment.updateRecordingTime(Math.round(recordingTimeInMillis / 1000));
      }
      mRecordingTimeHandler.postDelayed(this, 1000);
    }
  };

  //AA-1376:
 /* public void setVideoViewFragment(VideoViewFragment fragment) {
    this.mVideoViewFragment = fragment;
  }*/

  public void setParentFragment(Fragment fragment){
    mParentFragment=(ViewFinderFragment)fragment;
  }

  public void resumeDisplay() {
    if (mPlayer != null) {
      try {
        mPlayer.setBackgroundModeEnabled(false, FFMpegMovieViewAndroid.this.getHolder().getSurface());
      } catch (Exception e) {
        e.printStackTrace();
        Log.d(TAG, "Surface created...Exception: " + e.getMessage());
      }
    }
  }

  private boolean hideThumb;

  public void hideControllerThumb() {
    if (controller != null) {
      controller.hideThumb();
    } else {
      Log.d(TAG, "Controller is not initialized yet");
      hideThumb = true;
    }
  }
}
