package com.hubble.events;

/**
 * Created by Son Nguyen on 01/10/2015.
 */

/**
 * General message event object
 */
public class MessageEvent {

  public static final int DOWNLOAD_AND_SHARE_VIDEO = 1001;
  public static final int DOWNLOAD_AND_SHARE_IMAMGE = 1000;
  public static final int SHOW_EVENT_LOG_SHOWCASE = 1002;
  public static final int HAS_NEW_APP_VERSION_ON_STORE = 1003;
  public static final int CAMERA_REMOVED = 1008;
  private final int eventCode;
  private Object extra;

  public MessageEvent(int eventCode) {
    this.eventCode = eventCode;
  }

  public MessageEvent(int eventCode, Object extra) {
    this.eventCode = eventCode;
    this.extra = extra;
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
}
