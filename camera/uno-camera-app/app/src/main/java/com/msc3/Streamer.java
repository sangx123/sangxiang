package com.msc3;

import com.hubble.registration.models.BabyMonitorAuthentication;

/**
 * abstract class streamer --
 * |
 * |--- VideoStreamer
 * |----UdtVideoStreamer
 * <p/>
 * Use this with factory design pattern ..
 *
 * @author phung
 */
public abstract class Streamer implements Runnable {

  public static final int MSG_VIDEO_STREAM_HAS_STOPPED_FROM_SERVER = 0xCA000001;
  public static final int MSG_VIDEO_STREAM_HAS_STOPPED_TIMEOUT = 0xCA000002;
  public static final int MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY = 0xCA000003;
  public static final int MSG_VIDEO_STREAM_HAS_STARTED = 0xCA000004;
  public static final int MSG_VIDEO_STREAM_SWICTHED_TO_UDT_RELAY = 0xCA000005;
  public static final int MSG_VIDEO_STREAM_INTERNAL_ERROR = 0xCA000006;
  public static final int MSG_VIDEO_STREAM_HAS_STOPPED = 0xCA000007;

  //20130124: hoang: use when session key mismatched
  public static final int MSG_SESSION_KEY_MISMATCHED = 0xCA000008;

  //20130218: hoang: use when camera is not available in local mode
  public static final int MSG_CAMERA_IS_NOT_AVAILABLE = 0xCA000009;

  public static final int MSG_VIDEO_STREAM_SWICTHED_TO_UDT_RELAY_2 = 0xCA00000A;

  public static final int MSG_RTSP_VIDEO_STREAM_BITRATE_BPS = 0xCA00000B;

  public static final int MSG_VIDEO_SIZE_CHANGED = 0xCA00000C;
  public static final int MSG_VIDEO_FPS = 0xCA00000D;
  public static final int MSG_VIDEO_STREAM_STATUS = 0xCA00000E;

  public static final int STOP_REASON_USER = 1;
  public static final int STOP_REASON_TIMEOUT = 2;

  public abstract void run();

  public abstract void initQueries();

  abstract void setAccessToken(String usrToken);

  abstract void setRemoteAuthentication(BabyMonitorAuthentication bm);

  abstract void setHTTPCredential(String http_usr, String http_pass);

  abstract void addVideoSink(IVideoSink videoSink);

  abstract void removeVideoSink(IVideoSink videoSink);

  abstract void setMelodyUpdater(IMelodyUpdater m);

  abstract void setResUpdater(IResUpdater m);

  abstract void setTemperatureUpdater(ITemperatureUpdater t);

  abstract void enableAudio(boolean enableAudio);

  abstract int setImageResolution(int vga_or_qvga);

  abstract int getImageResolution();

  abstract void restart();

  abstract void stop(int reason);

  abstract int getResetFlag();

  abstract int getResetAudioBufferCount();

  abstract int getImgCurrentIndex();


  abstract boolean isEnableVideo();

  abstract void setEnableVideo(boolean enableVideo);

  abstract void checkDisconnectReason();

  abstract void closeCurrentSession();

  abstract boolean isStreaming();


  //Just a wrapper - too many parties using it now
  public void stop() {
    stop(STOP_REASON_USER);
  }


  protected Thread _keepAlive;

  protected static final byte[] keepAlive = {'h', 'e', 'l', 'l', 'o'};

  abstract void kickReadWatchDog();

}
