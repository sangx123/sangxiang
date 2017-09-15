package com.hubble.util;

import android.util.Log;

/**
 * Creator: Son Nguyen
 * Email  : son.nguyen@hubblehome.com
 * Date   : 3:27 PM 10 Mar 2017
 */
public class BgMonitorData {
  private String registrationId = "undefine";
  private boolean shouldEnableBgAfterQuitView = false;
  private static BgMonitorData instance = new BgMonitorData("undefine", false);

  private BgMonitorData(String registrationId, boolean shouldEnableBgAfterQuitView) {
    this.registrationId = registrationId;
    this.shouldEnableBgAfterQuitView = shouldEnableBgAfterQuitView;
  }

  public String getRegistrationId() {
    return registrationId;
  }

  public BgMonitorData setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
    return this;
  }

  public boolean isShouldEnableBgAfterQuitView() {
    return shouldEnableBgAfterQuitView;
  }

  public BgMonitorData setShouldEnableBgAfterQuitView(boolean shouldEnableBgAfterQuitView) {
    Log.d("mbp", "setShouldEnableBgAfterQuitView? " + shouldEnableBgAfterQuitView);
    this.shouldEnableBgAfterQuitView = shouldEnableBgAfterQuitView;
    return this;
  }

  public static BgMonitorData getInstance() {
    return instance;
  }
}
