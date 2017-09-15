package com.hubble.events;

/**
 * Created by Son Nguyen on 03/12/2015.
 */
public class SendCommandEvent {
  public static final int SET_MOTION = 1;
  public static final int NIGHT_VERSION = 2;
  public static final int SET_PARK_TIMER = 3;
  public static final int SET_SOUND = 4;
  public static final int SET_STATUS_LED = 5;
  public static final int SET_TEMP = 6;
  public static final int SET_VOLUMNE = 7;
  public static final int SET_CONTRAST = 8;
  public static final int SET_CEILING_MOUNT = 9;
  public static final int SET_BRIGHTNESS = 10;
  public static final int SET_ADAPTIVE_QUALITY = 11;
  public static final int SET_LED_FLICKER = 12;
  public static final int SET_OVERLAY_DATE = 13;
  public static final int SET_VIEW_MODE = 14; // WIDE, NARROW
  public static final int SET_MOTION_VDA_BSC = 15;
  public static final int SET_MOTION_VDA_OTHER = 16;
  public static final int SET_TIME_ZONE = 17;
  public static final int SET_LENS_CORRECTION = 18;
  public static final int SET_VIDEO_RECORDING_DURATION = 19;
  public static final int SET_QUALITY = 20;
  public static final int SET_NIGHT_LIGHT = 21;
  private final boolean status;
  private final int eventCode;
  private Object extra;


  public SendCommandEvent(int eventCode, boolean status) {
    this.status = status;
    this.eventCode = eventCode;
  }

  public Object getExtra() {
    return extra;
  }

  public void setExtra(Object extra) {
    this.extra = extra;
  }

  public int getEventCode() {
    return eventCode;
  }

  public boolean isSuccess() {
    return status;
  }

}
