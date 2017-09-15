package com.hubble.util;

import android.util.Log;

import com.hubble.HubbleApplication;


/**
 * Created by SoneyU on 8/10/2015.
 */
public class LogZ {
  private static final String TAG = HubbleApplication.TAG;
  private static final boolean SHOW_LOG = true;

  public static void i(String msg, Object... objs) {
    if (SHOW_LOG) {
      Log.i(TAG, String.format(msg, objs));
    }
  }

  public static void w(String msg, Object... objs) {
    if (SHOW_LOG) {
      Log.i(TAG, String.format(msg, objs));
    }
  }

  public static void e(String msg, Exception ex, Object... objs) {
    if (SHOW_LOG) {
      Log.e(TAG, String.format(msg, objs));
      if (ex != null) {
        ex.printStackTrace();
        Log.i(TAG, "======================== END OF STACK TRACE =============================\n\n\n");
      }
    }
  }

  public static void d(String msg, Object... objs) {
    if (SHOW_LOG) {
      Log.d(TAG, String.format(msg, objs));
    }
  }
}
