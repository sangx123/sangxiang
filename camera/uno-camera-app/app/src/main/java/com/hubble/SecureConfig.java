package com.hubble;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.hubble.registration.PublicDefine;
import com.hubble.util.LogZ;
import com.securepreferences.SecurePreferences;

import java.util.Map;

import base.hubble.PublicDefineGlob;

/**
 * Created by Son Nguyen on 15/09/2015.
 */
public class SecureConfig {
  private SecurePreferences securePreferences;
  private Context context;
  private static SecureConfig instance;
  public static final String HAS_USED_SECURE_SHARE_PREFS = "HAS_USED_SECURE_SHARE_PREFS";

  private static final String TAG = "SecureConfig";
  private boolean useNormalConfig;
  private SharedPreferences sharedPreferences;

  private SecureConfig(Context context) {
    this.context = context;
    long start = System.currentTimeMillis();

    useNormalConfig = needUseNormalConfig();

    if (useNormalConfig) {
      sharedPreferences = context.getSharedPreferences("hubble_app_config", Context.MODE_PRIVATE);
    } else {
      if (HubbleApplication.isVtechApp()) {
        // this code will make settings from old version gone
        securePreferences = new SecurePreferences(context, "Hu66bble2015", "vtech_config.xml");
      } else {
        securePreferences = new SecurePreferences(context, "Hu66bble2015", "hubble_config");
      }
      // if app did not use secure share preference before, clean up old preferences
      if (securePreferences.getBoolean(HAS_USED_SECURE_SHARE_PREFS, false)) {
        LogZ.i("App already used secure shared preferences");
      } else {
        LogZ.i("App did not used secure shared preferences, copy all share preferences");
        copySharePreferences();
        securePreferences.edit().putBoolean(HAS_USED_SECURE_SHARE_PREFS, true).commit();
      }
      LogZ.i("End of init share secure preference: " + (System.currentTimeMillis() - start));
    }
  }

  private void copySharePreferences() {
    SharedPreferences oldPrefs = context.getSharedPreferences(PublicDefine.PREFS_NAME, 0);
    Map<String, ?> allEntries = oldPrefs.getAll();
    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
    // after copy value, delete all non secure share preferences
    oldPrefs.edit().clear().apply();
  }

  public static synchronized SecureConfig getInstance(Context context) {
    if (instance == null) {
      instance = new SecureConfig(context);
    }
    return instance;
  }

  private void put(String key, Object value) {
    if (useNormalConfig) {
      if (value == null) {
        remove(key);
      } else {
        if (value instanceof Long) {
          sharedPreferences.edit().putLong(key, (Long) value).apply();
        } else if (value instanceof Integer) {
          sharedPreferences.edit().putInt(key, (Integer) value).apply();
        } else if (value instanceof String) {
          sharedPreferences.edit().putString(key, (String) value).apply();
        } else if (value instanceof Float) {
          sharedPreferences.edit().putFloat(key, (Float) value).apply();
        } else if (value instanceof Boolean) {
          sharedPreferences.edit().putBoolean(key, (Boolean) value).apply();
        }
      }
    } else {
      if (value == null) {
        remove(key);
      } else {
        if (value instanceof Long) {
          securePreferences.edit().putLong(key, (Long) value).apply();
        } else if (value instanceof Integer) {
          securePreferences.edit().putInt(key, (Integer) value).apply();
        } else if (value instanceof String) {
          securePreferences.edit().putString(key, (String) value).apply();
        } else if (value instanceof Float) {
          securePreferences.edit().putFloat(key, (Float) value).apply();
        } else if (value instanceof Boolean) {
          securePreferences.edit().putBoolean(key, (Boolean) value).apply();
        }
      }
    }
  }

  public boolean check(String key) {
    return useNormalConfig ? sharedPreferences.contains(key) : securePreferences.contains(key);
  }

  public Integer getInt(String key, int defaltValue) {
    if (useNormalConfig) {
      return sharedPreferences.getInt(key, defaltValue);
    } else {
      return securePreferences.getInt(key, defaltValue);
    }
  }

  public Long getLong(String key, long defaltValue) {
    if (useNormalConfig) {
      return sharedPreferences.getLong(key, defaltValue);
    } else {
      return securePreferences.getLong(key, defaltValue);
    }
  }

  public String getString(String key, String defaltValue) {
    if (useNormalConfig) {
      return sharedPreferences.getString(key, defaltValue);
    } else {
      return securePreferences.getString(key, defaltValue);
    }
  }

  public boolean getBoolean(String key, boolean defaltValue) {
    if (useNormalConfig) {
      return sharedPreferences.getBoolean(key, defaltValue);
    } else {
      return securePreferences.getBoolean(key, defaltValue);
    }
  }

  public float getFloat(String key, float defaltValue) {
    if (useNormalConfig) {
      return sharedPreferences.getFloat(key, defaltValue);
    } else {
      return securePreferences.getFloat(key, defaltValue);
    }
  }

  public void putInt(String key, int value) {
    put(key, value);
  }

  public void putString(String key, String value) {
    put(key, value);
  }

  public void putLong(String key, long value) {
    put(key, value);
  }

  public void putFloat(String key, float value) {
    put(key, value);
  }

  public void putBoolean(String key, boolean value) {
    put(key, value);
  }

  public void remove(String key) {
    if (useNormalConfig) {
      sharedPreferences.edit().remove(key).apply();
    } else {
      securePreferences.edit().remove(key).apply();
    }
  }

  public void clear() {
    if (useNormalConfig) {
      sharedPreferences.edit().clear().commit();
    } else {
      securePreferences.edit().clear().commit();
    }
  }

  public static boolean needUseNormalConfig() {
    String phoneModel = Build.MODEL;
    Log.i(TAG, "Phone Model 22" + phoneModel);
    for (String model : PublicDefineGlob.NON_SECURE_MODEL) {
      if (phoneModel.equalsIgnoreCase(model)) {
        Log.i(TAG, "Model " + phoneModel + " need use normal share preference");
        return true;
      }
    }
    Log.i(TAG, "Model " + phoneModel + " need use secure preference");
    return false;
  }

  public boolean isBackgroundMonitoringEnable() {
    // Default OFF for all apps
    return getBoolean(PublicDefineGlob.PREFS_BACKGROUND_MONITORING, false);
  }
}
