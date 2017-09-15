package com.nxcomm.blinkhd.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.hubble.HubbleApplication;

import base.hubble.PublicDefineGlob;

public class Global {
  public static String getApiKey(Context ctx) {
    if (ctx == null) {
      return null;
    }
    return HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
  }
}
