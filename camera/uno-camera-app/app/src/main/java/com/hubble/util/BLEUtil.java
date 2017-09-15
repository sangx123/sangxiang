package com.hubble.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by songn_000 on 07/10/2015.
 */
public class BLEUtil {
  public static boolean isSupportedBLE(Context context) {
    if (context == null) {
      return false;
    }

    boolean result = false;
    if (Build.VERSION.SDK_INT >= 18 &&
        context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      result = true;
    }
    return result;
  }
}
