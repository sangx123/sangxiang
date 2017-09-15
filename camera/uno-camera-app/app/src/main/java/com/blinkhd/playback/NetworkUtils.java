package com.blinkhd.playback;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import base.hubble.PublicDefineGlob;

public class NetworkUtils {
  public static boolean isNetworkAvailable(Activity activity) {
    ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivity == null) {
      return false;
    } else {
      NetworkInfo[] info = connectivity.getAllNetworkInfo();
      if (info != null) {
        for (NetworkInfo anInfo : info) {
          if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean isCameraSsid(String ssidNoQuotes) {
    boolean isCameraSsid = false;
    if (!TextUtils.isEmpty(ssidNoQuotes)) {
      for (String cameraSsid : PublicDefineGlob.CAMERA_SSID_LIST) {
        if (ssidNoQuotes.startsWith(cameraSsid)) {
          isCameraSsid = true;
          break;
        }
      }
    }
    return isCameraSsid;
  }
}
