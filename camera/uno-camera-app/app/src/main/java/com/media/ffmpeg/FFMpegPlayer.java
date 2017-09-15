package com.media.ffmpeg;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.msc3.PCMPlayer;
import com.msc3.Streamer;
import com.nxcomm.jstun_android.P2pClient;

import cz.havlena.ffmpeg.ui.IPlaylistUpdater;
import com.hubble.devcomm.impl.hubble.IP2pCommunicationHandler;

public class FFMpegPlayer implements IPlaylistUpdater, IP2pCommunicationHandler {
  private static final int MEDIA_NOP = 0;            // interface
  private static final int MEDIA_PREPARED = 1;
  private static final int MEDIA_PLAYBACK_COMPLETE = 2;
  private static final int MEDIA_BUFFERING_UPDATE = 3;
  private static final int MEDIA_SEEK_COMPLETE = 4;
  private static final int MEDIA_SET_VIDEO_SIZE = 5;
  private static final int MEDIA_VIDEO_STREAM_HAS_STARTED = 6;
  private static final int MEDIA_SEND_KEEP_ALIVE = 7;
  private static final int MEDIA_ERROR = 100;
  private static final int MEDIA_INFO = 200;

  private static final int MEDIA_PLAYBACK_STATUS_STARTED = 0;
  private static final int MEDIA_PLAYBACK_STATUS_IN_PROGRESS = 1;
  private static final int MEDIA_PLAYBACK_STATUS_COMPLETE = 2;

  public static final int MEDIA_PLAYBACK_NEXT = 300;

  private double mScaled = 1;
  private PointF mTranslated = new PointF(0, 0);
  private float xMaxLeft = 0;
  private float xMaxRight = 0;
  private float yMaxTop = 0;
  private float yMaxBottom = 0;

  private float sizeW = 0;
  private float sizeH = 0;
  private float focusX, focusY;
  private Matrix mDrawMatrix = new Matrix();
  private float translateX = 0;
  private float translateY = 0;

  public PointF getTranslated() {
    return mTranslated;
  }

  public void setFocus(float x, float y) {
    focusX = (float) (x * (mScaled));
    focusY = (float) (y * (mScaled));

    //Logger.i("Focus changed: " + x + " " + y);
    //Logger.i("Adjust forcus: " + focusX + " " + focusY);

    calculateDrawMatrix();
  }

  private void calcXY() {
    if (sizeW > 0 && sizeH > 0) {
      xMaxLeft = 0f - (float) (sizeW * (mScaled - 1));

      xMaxRight = 0f;// (float) (focusX * (mScaled - 1));

      yMaxTop = 0f - (float) (sizeH * (mScaled - 1));

      yMaxBottom = 0;// (float) (focusY * (mScaled - 1));

      // Logger.i("x max left = " + xMaxLeft);
      // Logger.i(" x max right = " + xMaxRight);

    }
  }

  public void addTranslated(float x, float y) {
    mTranslated.x += x;
    mTranslated.y += y;

    calculateDrawMatrix();
  }

  private void calculateDrawMatrix() {
    mDrawMatrix = new Matrix();

    // Logger.i("TRANSLATED: x = " + mTranslated.x + ", y = " +
    // mTranslated.y);

    calcXY();

    if (Math.abs(mScaled - 1) > 0.015) {
      translateX = (float) ((1 - mScaled) * focusX + mTranslated.x);
      translateY = (float) ((1 - mScaled) * focusY + mTranslated.y);

      // Logger.i("Calculated tranlastion: x = " + translateX + ", y = "
      // + translateY);

      if (translateX < xMaxLeft)
        translateX = xMaxLeft;
      if (translateX > xMaxRight)
        translateX = xMaxRight;
      if (translateY < yMaxTop)
        translateY = yMaxTop;
      if (translateY > yMaxBottom)
        translateY = yMaxBottom;

      mDrawMatrix.postScale((float) mScaled, (float) mScaled);
      mDrawMatrix.postTranslate(translateX, translateY);

      // Logger.i("Adjustment tranlastion: x = " + translateX + ", y = "
      // + translateY);

    } else {
      // Logger.i("Scaled = 1");
      mScaled = 1;
      mTranslated.x = 0;
      mTranslated.y = 0;
      // Logger.i(" Width x Height " + sizeW + " x " + sizeH);
    }

    surfaceRender();
  }

  public double getScaled() {
    return mScaled;
  }

  public void setScaled(double scaled) {
    this.mScaled = scaled;

    calculateDrawMatrix();
  }

  /**
   * Unspecified media player error.
   *
   * @see android.media.MediaPlayer.OnErrorListener
   */
  public static final int MEDIA_ERROR_UNKNOWN = 1;

  /**
   * Media server died. In this case, the application must release the
   * MediaPlayer object and instantiate a new one.
   *
   * @see android.media.MediaPlayer.OnErrorListener
   */
  public static final int MEDIA_ERROR_SERVER_DIED = 100;

  /**
   * The video is streamed and its container is not valid for progressive
   * playback i.e the video's index (e.g moov atom) is not at the start of the
   * file.
   *
   * @see android.media.MediaPlayer.OnErrorListener
   */
  public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;

  /**
   * Unspecified media player info.
   *
   * @see android.media.MediaPlayer.OnInfoListener
   */
  public static final int MEDIA_INFO_UNKNOWN = 1;

  /**
   * The video is too complex for the decoder: it can't decode frames fast
   * enough. Possibly only the audio plays fine at this stage.
   *
   * @see android.media.MediaPlayer.OnInfoListener
   */
  public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;

  /**
   * Bad interleaving means that a media has been improperly interleaved or
   * not interleaved at all, e.g has all the video samples first then all the
   * audio ones. Video is playing but a lot of disk seeks may be happening.
   *
   * @see android.media.MediaPlayer.OnInfoListener
   */
  public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;

  /**
   * The media cannot be seeked (e.g live stream)
   *
   * @see android.media.MediaPlayer.OnInfoListener
   */
  public static final int MEDIA_INFO_NOT_SEEKABLE = 801;

  /**
   * A new set of metadata is available.
   *
   * @see android.media.MediaPlayer.OnInfoListener
   */
  public static final int MEDIA_INFO_METADATA_UPDATE = 802;

  public static final int MEDIA_INFO_FRAMERATE_VIDEO = 900;
  public static final int MEDIA_INFO_FRAMERATE_AUDIO = 901;
  public static final int MEDIA_INFO_VIDEO_SIZE = 902;
  public static final int MEDIA_INFO_BITRATE_BPS = 903;
  // use for video timer task
  public static final int MEDIA_INFO_CORRUPT_FRAME_TIMEOUT = 904;
  public static final int MEDIA_INFO_RECEIVED_VIDEO_FRAME = 905;
  public static final int MEDIA_INFO_RECORDING_TIME = 906;
  public static final int MEDIA_INFO_START_BUFFERING = 907;
  public static final int MEDIA_INFO_STOP_BUFFERING = 908;
  public static final int MEDIA_INFO_CLIP_URL = 909;
  public static final int MEDIA_INFO_HANDSHAKE_RESULT = 910;
  public static final int MEDIA_INFO_P2P_SESSION_INDEX = 911;
  public static final int MEDIA_INFO_HANDSHAKE_FAILED = 912;

  public static final int MSG_MEDIA_STREAM_LOADING_VIDEO = 1000;
  public static final int MSG_MEDIA_STREAM_LOADING_VIDEO_CANCEL = 1001;
  public static final int MSG_MEDIA_STREAM_RECORDING_TIME = 1002;
  public static final int MSG_MEDIA_STREAM_START_BUFFERING = 1003;
  public static final int MSG_MEDIA_STREAM_STOP_BUFFERING = 1004;
  public static final int MSG_MEDIA_STREAM_SEEK_COMPLETE = 1005;
  public static final int MSG_MEDIA_STREAM_SEND_KEEP_ALIVE = 1006;
  public static final int MSG_MEDIA_STREAM_CLIP_URL = 1007;
  public static final int MSG_MEDIA_INFO_HANDSHAKE_RESULT = 1008;
  public static final int MSG_MEDIA_INFO_P2P_SESSION_INDEX = 1009;
  public static final int MSG_MEDIA_INFO_P2P_HANDSHAKE_FAILED = 1010;

	/* Phung: added some error code for retries */

  public static final int MEDIA_ERROR_TIMEOUT_WHILE_STREAMING = 101;

  private static final String STR_MEDIA_PLAYBACK_COMPLETE = "complete";
  private static final String STR_MEDIA_PLAYBACK_IN_PROGRESS = "in_progress";

  public static final int MEDIA_STREAM_ALL_FRAME = 0;
  public static final int MEDIA_STREAM_IFRAME_ONLY = 1;
  public static final int MEDIA_STREAM_RTSP_WITH_TCP = 2;
  public static final int MEDIA_STREAM_VIDEO_SKIP_TO_KEYFRAME = 3;
  public static final int MEDIA_STREAM_DISABLE_SYNC = 4;
  public static final int MEDIA_STREAM_ENABLE_SYNC = 5;
  public static final int MEDIA_STREAM_ADJUST_BITRATE = 6;
  public static final int MEDIA_STREAM_SHOW_DURATION = 7;
  public static final int MEDIA_STREAM_TURN_ON_DEBUG_LOG = 8;
  public static final int MEDIA_STREAM_TURN_OFF_DEBUG_LOG = 9;

  public enum RTSP_PROTOCOL {
    UDP, TCP
  }

  ;

  private final static String TAG = "FFMpegMediaPlayer-java";

  private int mNativeContext;                             // accessed
  // by
  // native
  // methods
  // private int mListenerContext; // accessed by native methods
  private Surface mSurface;                                 // accessed
  // by
  // native
  // methods
  private SurfaceHolder mSurfaceHolder;
  private PowerManager.WakeLock mWakeLock = null;
  private boolean mScreenOnWhilePlaying;
  private boolean mStayAwake;

  private boolean isFinishLoading;
  private int playlist_idx = 0;
  private ArrayList<String> playlist;
  private Handler mHandler;
  private boolean forPlayback;
  private boolean forSharedCam;
  private boolean isDecryptionMode = false;

  static {
    native_init();
  }

  public FFMpegPlayer(Handler h, boolean forPlayBack, boolean forSharedCam) {
    /*
     * Native setup requires a weak reference to our object. It's easier to
     * create it here than in C++.
     */
    native_setup(new WeakReference<FFMpegPlayer>(this), forPlayBack,
        forSharedCam);

    // Initializ a 16k audio-buff : this is to transfer audio data bw native
    // and java
    audio_buff = new byte[16 * 1024]; // 8k

    mHandler = h;
    playlist = new ArrayList<String>();
    isFinishLoading = false;
    this.forPlayback = forPlayBack;
    this.forSharedCam = forSharedCam;
  }

  public FFMpegPlayer() {
    Log.i(TAG, "Create FFMpegPlayer for decryption mode.");
    isDecryptionMode = true;
  }

  /**
   * Called from native code when an interesting event happens. This method
   * just uses the EventHandler system to post the event back to the main app
   * thread. We use a weak reference to the original MediaPlayer object so
   * that the native code is safe from the object disappearing from underneath
   * it. (This is the cookie passed to native_setup().)
   */
  private void postEventFromNative(Object mediaplayer_ref, int what,
                                   int arg1, int arg2, Object obj) {

    switch (what) {
      case MEDIA_INFO_P2P_SESSION_INDEX:
        //Log.d(TAG, "Received MEDIA_INFO_P2P_SESSION_INDEX...");
        mHandler.sendMessage(Message.obtain(mHandler, MSG_MEDIA_INFO_P2P_SESSION_INDEX,
            arg1, arg2));
        break;
      case MEDIA_INFO_HANDSHAKE_RESULT:
        mHandler.sendMessage(Message.obtain(mHandler, MSG_MEDIA_INFO_HANDSHAKE_RESULT,
            arg1, arg2));
        break;
      case MEDIA_INFO_HANDSHAKE_FAILED:
        Log.d(TAG, "Received MEDIA_INFO_HANDSHAKE_FAILED...");
        mHandler.sendMessage(Message.obtain(mHandler, MSG_MEDIA_INFO_P2P_HANDSHAKE_FAILED,
            arg1, arg2));
        break;
      case MEDIA_INFO_CLIP_URL:
        mHandler.sendMessage(Message.obtain(mHandler, MSG_MEDIA_STREAM_CLIP_URL,
            getCurrentUrl()));
        break;
      case MEDIA_INFO_FRAMERATE_VIDEO:
        // Log.d(TAG, "Video fps:" + arg1);
        mHandler.sendMessage(Message.obtain(mHandler,
            Streamer.MSG_VIDEO_FPS, arg1, arg2));
        break;
      case MEDIA_INFO_FRAMERATE_AUDIO:
        // Log.d(TAG, "Audio fps:" + arg1);

        break;
      case MEDIA_INFO_VIDEO_SIZE:
        Log.d(TAG, "Received MEDIA_INFO_VIDEO_SIZE...");
        mHandler.sendMessage(Message.obtain(mHandler,
            Streamer.MSG_VIDEO_SIZE_CHANGED, arg1, arg2));
        break;
      case MEDIA_INFO_BITRATE_BPS:

        mHandler.sendMessage(Message.obtain(mHandler,
            Streamer.MSG_RTSP_VIDEO_STREAM_BITRATE_BPS, arg1, -1));

        break;

      case MEDIA_ERROR_SERVER_DIED:
      case MEDIA_ERROR_TIMEOUT_WHILE_STREAMING:
        mHandler.sendMessage(Message.obtain(mHandler,
            Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY, arg1, arg2, obj));

      /*
       * TODO:
       *
       * Why are we failling? Our issue: Switch WIFIs, or WIFI <--> 3g
       * Going out of range
       *
       * Camera issue: Camera turn off/ restarted / Ip changed
       *
       * What mode are we in - Local -> Recovery in local - Remote ->
       * Recovery in REMOTE (UPNP or Wowza)
       */

        break;
      case MEDIA_PLAYBACK_COMPLETE:
        Log.d(TAG, "Received MEDIA_PLAYBACK_COMPLETE...");
        mHandler.sendMessage(Message.obtain(mHandler,
            Streamer.MSG_VIDEO_STREAM_HAS_STOPPED));

        break;
      case MEDIA_VIDEO_STREAM_HAS_STARTED:
        Log.d(TAG, "Received MEDIA_VIDEO_STREAM_HAS_STARTED...");
        mHandler.sendMessage(Message.obtain(mHandler,
            Streamer.MSG_VIDEO_STREAM_HAS_STARTED));
        break;
      case MEDIA_SEND_KEEP_ALIVE:
        // Log.d(TAG, "Received MSG_MEDIA_STREAM_SEND_KEEP_ALIVE...");
        mHandler.sendMessage(Message.obtain(mHandler,
            MSG_MEDIA_STREAM_SEND_KEEP_ALIVE));
        break;
      case MEDIA_INFO_CORRUPT_FRAME_TIMEOUT:
        Log.d(TAG, "Received MEDIA_INFO_CORRUPT_FRAME_TIMEOUT...");
        mHandler.sendMessage(Message.obtain(mHandler,
            MSG_MEDIA_STREAM_LOADING_VIDEO));
        break;
      case MEDIA_INFO_RECEIVED_VIDEO_FRAME:
        Log.d(TAG, "Received MEDIA_INFO_RECEIVED_VIDEO_FRAME...");
        mHandler.sendMessage(Message.obtain(mHandler,
            MSG_MEDIA_STREAM_LOADING_VIDEO_CANCEL));
        break;
      case MEDIA_INFO_RECORDING_TIME:
        // Log.d(TAG, "Received MEDIA_INFO_RECORDING_TIME...");
        mHandler.sendMessage(Message.obtain(mHandler,
            MSG_MEDIA_STREAM_RECORDING_TIME, arg1, arg2));
        break;
      case MEDIA_INFO_START_BUFFERING:
        Log.d(TAG, "Received MEDIA_INFO_START_BUFFERING...");
        mHandler.sendMessage(Message.obtain(mHandler,
            MSG_MEDIA_STREAM_START_BUFFERING, arg1, arg2));
        break;
      case MEDIA_INFO_STOP_BUFFERING:
        Log.d(TAG, "Received MEDIA_INFO_STOP_BUFFERING...");
        mHandler.sendMessage(Message.obtain(mHandler,
            MSG_MEDIA_STREAM_STOP_BUFFERING, arg1, arg2));
        break;
      case MEDIA_SEEK_COMPLETE:
        mHandler.sendMessage(Message.obtain(mHandler,
            MSG_MEDIA_STREAM_SEEK_COMPLETE, arg1, arg2));
        break;

      default:
        // Log.d(TAG, "Unknown event: " + what);
        break;

    }
  }

  /**
   * Sets the SurfaceHolder to use for displaying the video portion of the
   * media. This call is optional. Not calling it when playing back a video
   * will result in only the audio track being played.
   *
   * @param sh the SurfaceHolder to use for video display
   * @throws IOException
   */
  public void setDisplay(SurfaceHolder sh) throws IOException,
      IllegalArgumentException, IllegalStateException {
      try {
          mSurfaceHolder = sh;
          if (sh != null) {
              mSurface = sh.getSurface();
          } else {
              mSurface = null;
          }
          _setVideoSurface(mSurface);
          // updateSurfaceScreenOn();
      }catch(IllegalStateException illegalex){

      }catch (IllegalArgumentException illegalArg){

      }catch (IOException ioEx){

      }
  }

  public void setPlaylist(ArrayList<String> list) {
    playlist = list;
  }

  public String getNextUrl() {
    String url = null;

    if (playlist.size() > 0) {
      if (playlist_idx + 1 < playlist.size()) {
        playlist_idx++;
        url = playlist.get(playlist_idx);
      }

//      Log.d(TAG, "url " + url + ", isFinishLoading? " + isFinishLoading);
      if (url == null) {
        if (isFinishLoading) {
          url = STR_MEDIA_PLAYBACK_COMPLETE;
        } else {
          url = STR_MEDIA_PLAYBACK_IN_PROGRESS;
        }
      }
    } else {
      url = STR_MEDIA_PLAYBACK_IN_PROGRESS;
    }

    return url;
  }

  public String getCurrentUrl() {
    String url = null;

    if (playlist.size() > 0) {
      if (playlist_idx < playlist.size()) {
        url = playlist.get(playlist_idx);
      }
    }

    return url;
  }

  /**
   * Starts or resumes playback. If playback had previously been paused,
   * playback will continue from where it was paused. If playback had been
   * stopped, or never started before, playback will start at the beginning.
   *
   * @throws IllegalStateException if it is called in an invalid state
   */
  public void start() throws IllegalStateException {
    // stayAwake(true);
    _start();
  }

  /**
   * Stops playback after playback has been stopped or paused.
   *
   * @throws IllegalStateException if the internal player engine has not been initialized.
   */
  public void stop() throws IllegalStateException {
    // stayAwake(false);
    _stop();

    // Stop PCM player too
    stopPCMPlayer();

  }

  /**
   * Pauses playback. Call start() to resume.
   *
   * @throws IllegalStateException if the internal player engine has not been initialized.
   */
  public void pause() throws IllegalStateException {
    // stayAwake(false);
    _pause();
  }

  /**
   * Set the low-level power management behavior for this MediaPlayer. This
   * can be used when the MediaPlayer is not playing through a SurfaceHolder
   * set with {@link #setDisplay(SurfaceHolder)} and thus can use the
   * high-level {@link #setScreenOnWhilePlaying(boolean)} feature.
   *
   * <p>
   * This function has the MediaPlayer access the low-level power manager
   * service to control the device's power usage while playing is occurring.
   * The parameter is a combination of {@link android.os.PowerManager} wake
   * flags. Use of this method requires
   * {@link android.Manifest.permission#WAKE_LOCK} permission. By default, no
   * attempt is made to keep the device awake during playback.
   *
   * @param context
   *            the Context to use
   * @param mode
   *            the power/wake mode to set
   * @see android.os.PowerManager
   */

  /**
   * Control whether we should use the attached SurfaceHolder to keep the
   * screen on while video playback is occurring. This is the preferred method
   * over where possible, since it doesn't require that
   * the application have permission for low-level wake lock access.
   *
   * @param screenOn Supply true to keep the screen on, false to allow it to turn
   *                 off.
   */
  public void setScreenOnWhilePlaying(boolean screenOn) {
    if (mScreenOnWhilePlaying != screenOn) {
      mScreenOnWhilePlaying = screenOn;
      // updateSurfaceScreenOn();
    }
  }


  /*
   * private void updateSurfaceScreenOn() { if (mSurfaceHolder != null) {
   * mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake); } }
   */

  /**
   * Try to decrypt an encrypted file.
   *
   * @param filePath path to the encrypted file key key for decryption iv iv for
   *                 decryption
   * @return 0 if success, otherwise unsuccess.
   */
  public native int decryptFile(String filePath, String key, String iv);

  /**
   * Update the MediaPlayer ISurface. Call after updating mSurface.
   */
  private native void _setVideoSurface(Surface surface) throws IOException,
      IllegalArgumentException, IllegalStateException;

  /**
   * Sets the data source (file-path or http/rtsp URL) to use.
   *
   * @param path the path of the file you want to play
   * @throws IllegalStateException if it is called in an invalid state
   */
  public native void setDataSource(String path) throws IOException,
      IllegalArgumentException, IllegalStateException;

  public native void setEncryptionKey(String key_hex_string)
      throws IllegalArgumentException, RuntimeException;

  public native void setEncryptionIv(String iv_hex_string)
      throws IllegalArgumentException, RuntimeException;

  public native void setEncryptionEnable(boolean encrypt_enabled)
      throws IllegalArgumentException;

  public native void setP2pSessionCount(int p2pCount);

  public native void setP2PInfo(P2pClient[] p2pClients)
      throws RuntimeException;

  public native void handShake(String src_ip, int port, int sock_fd) throws RuntimeException;

  public native void setUIDs(String[] uids, int numId) throws RuntimeException;

  public native String sendCommand(String request) throws RuntimeException;

  public native void sendTalkbackData(byte[] data, int offset, int length) throws RuntimeException;

  public native boolean isP2pConnected();

  /**
   * Set ffmpeg audio enabled/disabled. This function should be called
   * before we connect to input. We could not disabled/enabled audio back later.
   * For temporarily mute audio then unmute later, use setAudioStreamMuted() method.
   *
   * @param isEnabled true enable audio, false disable audio
   * @throws IllegalArgumentException if media player is null
   */
  public native void setAudioEnabled(boolean isEnabled)
      throws IllegalArgumentException;

  public native void setRecordModeEnabled(boolean isEnabled)
      throws IllegalArgumentException;

  /**
   * Set audio stream volume mute/unmute.
   *
   * @param shouldMute
   * @return true if successfull, false if failed (PCMPlayer has been released or not been initialized)
   */
  public boolean setAudioStreamMuted(boolean shouldMute) {
    if (_pcmPlayer != null) {
      _pcmPlayer.setAudioMuted(shouldMute);
      return true;
    }

    return false;
  }

  /**
   * Set media player buffer size.
   *
   * @param size_in_kb size of buffer in KB (from 0KB to 200KB).
   * @throws IllegalArgumentException if media player is null.
   */
  public native void setBufferSize(int size_in_kb)
      throws IllegalArgumentException;

  /**
   * Prepares the player for playback, synchronously.
   * <p/>
   * After setting the datasource and the display surface, you need to either
   * call prepare(). For files, it is OK to call prepare(), which blocks until
   * MediaPlayer is ready for playback.
   *
   * @throws IllegalStateException if it is called in an invalid state
   */
  private native void _prepare() throws IOException, IllegalStateException;

  private native void _start() throws IllegalStateException;

  private native void _stop() throws IllegalStateException;

  private native void _pause() throws IllegalStateException;

  /**
   * Returns the width of the video.
   *
   * @return the width of the video, or 0 if there is no video, no display
   * surface was set, or the width has not been determined yet. The
   * OnVideoSizeChangedListener can be registered via
   * {@link #setOnVideoSizeChangedListener(OnVideoSizeChangedListener)}
   * to provide a notification when the width is available.
   */
  public native int getVideoWidth();

  /**
   * Returns the height of the video.
   *
   * @return the height of the video, or 0 if there is no video, no display
   * surface was set, or the height has not been determined yet. The
   * OnVideoSizeChangedListener can be registered via
   * {@link #setOnVideoSizeChangedListener(OnVideoSizeChangedListener)}
   * to provide a notification when the height is available.
   */
  public native int getVideoHeight();

  /**
   * Checks whether the MediaPlayer is playing.
   *
   * @return true if currently playing, false otherwise
   */
  public native boolean isPlaying();

  public native boolean isRecording();

  /**
   * Seeks to specified time position.
   *
   * @param msec the offset in milliseconds from the start to seek to
   * @throws IllegalStateException if the internal player engine has not been initialized
   */
  public native void seekTo(int msec) throws IllegalStateException;

  /**
   * Gets the current playback position.
   *
   * @return the current position in milliseconds
   */
  public native int getCurrentPosition() throws IllegalStateException;

  public native int getCurrentStreamPosition() throws IllegalStateException;

  public native void setP2pPlayByTimestampEnabled(boolean isEnabled) throws IllegalStateException;

  /**
   * Gets the duration of the file.
   *
   * @return the duration in milliseconds
   */
  public native int getDuration();

  public native void setDuration(int duration_msec);

  private native void _release();

  private native void _reset();

  /**
   * @hide
   */
  private native int native_suspend_resume(boolean isSuspend)
      throws IllegalStateException;

  /**
   * Sets the audio stream type for this MediaPlayer. See {@link AudioManager}
   * for a list of stream types. Must call this method before prepare() or
   * prepareAsync() in order for the target stream type to become effective
   * thereafter.
   *
   * @param streamtype the audio stream type
   * @see android.media.AudioManager
   */
  public native void setAudioStreamType(int streamtype);

  /**
   * Enable/disable audio background mode.
   * @param isEnabled true to enable audio background mode, false to disable it.
   * @param surface The new surface that media player would rendering on when enter foreground.
   *                When enter audio background mode, just need to pass null surface here.
   */
  public native void setBackgroundModeEnabled(boolean isEnabled, Surface surface) throws IllegalStateException;

  private static native final void native_init() throws RuntimeException;

  private native final void native_setup(Object mediaplayer_this,
                                         boolean forPlayBack, boolean forSharedCam);

  private native final void native_finalize();

  public native void setPlayOption(int opts) throws IllegalStateException;

  public native void checkAndFlushAllBuffers();

  public native void flushAllBuffers();

  public native byte[] native_getSnapShot();

  public native void native_updateAudioClk(double pts);

  /**
   * Releases resources associated with this MediaPlayer object. It is
   * considered good practice to call this method when you're done using the
   * MediaPlayer.
   */
  public void release() {
    // stayAwake(false);
    // updateSurfaceScreenOn();
    _release();
    // free playlist
    if (playlist != null) {
      Iterator<String> iter = playlist.iterator();
      while (iter.hasNext()) {
        iter.next();
        iter.remove();
      }

      playlist = null;
    }

  }

  /**
   * Resets the MediaPlayer to its uninitialized state. After calling this
   * method, you will have to initialize it again by setting the data source
   * and calling prepare().
   */
  public void reset() {
    // stayAwake(false);
    _reset();
  }

  /**
   * Suspends the MediaPlayer. The only methods that may be called while
   * suspended are {@link #reset()}, {@link #release()} and {@link #resume()}.
   * MediaPlayer will release its hardware resources as far as possible and
   * reasonable. A successfully suspended MediaPlayer will cease sending
   * events. If suspension is successful, this method returns true, otherwise
   * false is returned and the player's state is not affected.
   *
   * @hide
   */
  public boolean suspend() {

    try {
      if (native_suspend_resume(true) < 0) {
        return false;
      }
    } catch (IllegalStateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }

    // stayAwake(false);

    // make sure none of the listeners get called anymore
    // mEventHandler.removeCallbacksAndMessages(null);

    return true;
  }

  /**
   * Resumes the MediaPlayer. Only to be called after a previous (successful)
   * call to {@link #suspend()}. MediaPlayer will return to a state close to
   * what it was in before suspension.
   *
   * @hide
   */
  public boolean resume() {
    if (native_suspend_resume(false) < 0) {
      return false;
    }

    /*
     * if (isPlaying()) { stayAwake(true); }
     */

    return true;
  }

  public void prepare() throws IllegalStateException, IOException {
    this._prepare();
  }

  @Override
  protected void finalize() {
    if (isDecryptionMode == false) {
      native_finalize();
    } else {
      Log.i(TAG, "Decryption mode finished, do not release media player.");
    }
  }

  public interface IFFMpegPlayer {
    void onPlay ();

    void onStop ();

    void onRelease ();

    void onError (String msg, Exception e);
  }

  // ////////////////////////////////////

  private PCMPlayer _pcmPlayer = null;
  private Thread pcmPlayer_thrd = null;
  private byte[] audio_buff;

  public void native_startPCMPlayer(int sampleRate, int channels) {
    Log.d(TAG, "Start pcm player with: sample: " + sampleRate + " channel:"
        + channels + " for playback? " + forPlayback);
    // _pcmPlayer = new PCMPlayer(11025,
    // AudioFormat.CHANNEL_CONFIGURATION_MONO);
    _pcmPlayer = new PCMPlayer(sampleRate, channels, forPlayback);
    _pcmPlayer.setFFMpegUpdateClkCb(this);
    pcmPlayer_thrd = new Thread(_pcmPlayer, "PCMPlayer");
    pcmPlayer_thrd.start();

  }

  public void flushPCMBuffer() {
    if (_pcmPlayer != null) {
      _pcmPlayer.flush();
    }
  }

  public void pausePCMPlayer() {
    if (_pcmPlayer != null) {
      _pcmPlayer.pause();
    }
  }

  public void resumePCMPlayer() {
    if (_pcmPlayer != null) {
      _pcmPlayer.resume();
    }
  }

  public void stopPCMPlayer() {
    if (pcmPlayer_thrd != null) {
      Log.d(TAG, "Stop PCM player");
      _pcmPlayer.stop();
      try {
        pcmPlayer_thrd.interrupt();
        pcmPlayer_thrd.join(2000);
      } catch (InterruptedException ie) {

      }

    }

  }

  // ///////Below functions are TO BE called from native code

  private byte[] get_audio_buff(int len) {
    // Log.d(TAG, "return predefined buff");
    return audio_buff;
  }

  /**
   * =======PCM === Process @len data in audio buff. Starting from index 0
   * -to- len-1
   *
   * @param len
   * @return
   */
  private int process_audio_buff_callback(int len) {
    // Log.d(TAG, "got pcm audioBuff with len:"+ len);

    byte[] outPCM = audio_buff;
    int outPCM_len = len;

    if (_pcmPlayer != null) {
      // Log.d(TAG, "decode audioBuf: " + outPCM_len);

      _pcmPlayer.writePCM(outPCM, outPCM_len);
    }

    return len;
  }

  // same process_audio_buff_callback include pts
  public int process_audio_buff_callback_with_pts(int len, double pts) {
    // Log.d(TAG, "got pcm audioBuff with len:"+ len);

    byte[] outPCM = audio_buff;
    int outPCM_len = len;

    if (_pcmPlayer != null) {
      // Log.d(TAG, "decode audioBuf: " + outPCM_len);

      // _pcmPlayer.writePCM(outPCM, outPCM_len);
      _pcmPlayer.writePCMWithPTS(outPCM, outPCM_len, pts);
    }

    return len;
  }

  private long get_unread_buff_len() {
    if (_pcmPlayer != null) {
      // Log.d(TAG, "decode audioBuf: " + outPCM_len);

      return _pcmPlayer.unReadData();
    }
    return 0;
  }

  private int is_audio_buff_full() {
    return _pcmPlayer.isBuffFull();
  }

  // ////////////////////////// VIDEO

  /*
   * 20121005 : these func: native_getNewBitMap(), native_updateVideoSurface()
   * are no longer used since creating new bitmap every time is too expensive
   * (memory wise- allocate & deallocate)
   *
   * Optimized Way: control image update in Native Pre-allocate 2 buffers (one
   * for ffmpeg decoder -to dump out raw image another is from mSurface - ) NO
   * need to create new bitmap everytime. TODO: Currently we do some
   * re-scaling with bitmap in Java code native_updateVideoSurface() -- May
   * need to do the same for native
   */

  private Bitmap native_getNewBitMap() {
    Bitmap bitMap = Bitmap.createBitmap(640, 480, Bitmap.Config.RGB_565);
    // renderbitmap(mBitmap, 0);
    // canvas.drawBitmap(mBitmap, 0, 0, null);

    return bitMap;
  }

  private void native_updateVideoSurface(Bitmap b) {
    Canvas c = null;
    int left = 0;
    int top = 0;
    int right = b.getWidth();
    int bottom = b.getHeight();
    int destWidth;
    int destHeight;

    try {
      c = mSurfaceHolder.lockCanvas(null);
      if (c == null) {
        return;
      }
      float ratio = (float) right / bottom;
      float fh = c.getWidth() / ratio;

      destWidth = c.getWidth();
      destHeight = (int) fh;

      int dst_top = 0;
      if (destHeight > c.getHeight()) {
        int delta = destHeight - c.getHeight();
        dst_top = -delta / 2;
        destHeight -= delta / 2;
      } else {
        // expand the height
        destHeight = c.getHeight();
      }

      Rect src = new Rect(left, top, right, bottom);

      Rect dest = new Rect(0, dst_top, destWidth, destHeight);

      synchronized (mSurfaceHolder) {

        c.drawBitmap(b, src, dest, null);
      }

    } finally {
      if (c != null) {
        mSurfaceHolder.unlockCanvasAndPost(c);
      }
    }

  }

  /*
   * (non-Javadoc)
   *
   * @see
   * cz.havlena.ffmpeg.ui.IPlaylistUpdater#updatePlaylist(java.util.ArrayList)
   */
  @Override
  public void updatePlaylist(ArrayList<String> list) {
    playlist = list;
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.havlena.ffmpeg.ui.IPlaylistUpdater#finishLoadingPlaylist(boolean)
   */
  @Override
  public void finishLoadingPlaylist(boolean isFinishLoading) {
    this.isFinishLoading = isFinishLoading;
  }

  private native void native_startRecord(String url)
      throws IllegalStateException;

  private native void native_stopRecord();

  public void stopRecord() {
    native_stopRecord();
  }

  public void startRecording(String recordFileName) {
    Log.d("mbp", "recording to file: " + recordFileName);
    try {
      native_startRecord(recordFileName);
    } catch (IllegalStateException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private Bitmap mBitmap;
  private ByteBuffer mByteBuffer;

  /**
   * Allocate a buffer that can be directly accessed from JNI code.
   *
   * @param w video width
   * @param h video height
   * @return the ByteBuffer
   */
  private ByteBuffer surfaceInit(int w, int h) {
    synchronized (this) {
      mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
      mByteBuffer = ByteBuffer.allocateDirect(w * h * 2);
      return mByteBuffer;
    }
  }

  /**
   * Will be called from JNI code to render the image buffer to SurfaceView.
   * Need to fill mByteBuffer first before call this method.
   */
  private void surfaceRender() {
    if (mBitmap == null || mByteBuffer == null) {
      return;
    }

    synchronized (this) {
      Canvas c = null;
      /* mByteBuffer has been filled from JNI code. */
      mBitmap.copyPixelsFromBuffer(mByteBuffer);
      /* IMPORTANT: need rewind to reset read pointer of buffer. */
      mByteBuffer.rewind();

      int left = 0;
      int top = 0;
      int right = mBitmap.getWidth();
      int bottom = mBitmap.getHeight();
      int destWidth;
      int destHeight;
      try {
        c = mSurfaceHolder.lockCanvas(null);

        if (c == null) {
          return;
        }
        float ratio = (float) right / bottom;
        float fh = c.getWidth() / ratio;

        destWidth = c.getWidth();
        destHeight = (int) fh;

        int dst_top = 0;
        if (destHeight > c.getHeight()) {
          int delta = destHeight - c.getHeight();
          dst_top = -delta / 2;
          destHeight -= delta / 2;
        } else {
          // expand the height
          destHeight = c.getHeight();
        }

        Rect src = new Rect(left, top, right, bottom);

        Rect dest = new Rect(0, dst_top, destWidth, destHeight);

        sizeW = destWidth;
        sizeH = destHeight;

        synchronized (mSurfaceHolder) {
          c.drawColor(Color.BLACK);
          c.setMatrix(mDrawMatrix);
          // c.scale(mScaled, mScaled);
          // c.translate(focusX, focusY);

          c.drawBitmap(mBitmap, src, dest, null);
        }

      } finally {
        if (c != null) {
          mSurfaceHolder.unlockCanvasAndPost(c);
        }
      }
    }
  }

  /* Release the bitmap and its pixel buffer. */
  private void surfaceRelease() {
    synchronized (this) {
      if (mBitmap != null) {
        mBitmap.recycle();
        mBitmap = null;
      }
      mByteBuffer = null;
    }
  }

}
