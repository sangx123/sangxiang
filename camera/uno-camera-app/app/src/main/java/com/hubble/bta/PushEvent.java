package com.hubble.bta;

/**
 * Created by Son Nguyen on 24 Aug 2016.
 */
public class PushEvent {

  private final String deviceRegId;
  private final int eventType;

  public PushEvent(String deviceRegId, int eventType) {
    this.deviceRegId = deviceRegId;
    this.eventType = eventType;
  }

  public int getEventType() {
    return eventType;
  }

  public String getDeviceRegId() {
    return deviceRegId;
  }

  public boolean isSameDevice(String deviceRegId) {
    return this.deviceRegId.equalsIgnoreCase(deviceRegId);
  }
}
