package com.nxcomm.blinkhd.util;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencePersist {
    private final String PREF_NAME = "hubble_home_persist";
    private static SharedPreferencePersist instance;
    private final SharedPreferences mPref;


    private SharedPreferencePersist(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencePersist initializeInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencePersist(context);
        }
        return instance;
    }

    public static synchronized SharedPreferencePersist getInstance() {
        if (instance == null) {
            throw new IllegalStateException(SharedPreferencePersist.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public void putString(String key, String value) {
        mPref.edit().putString(key, value).commit();
    }

    public String getString(String key, String defValue) {
        return mPref.getString(key, defValue);
    }

    public void putInt(String key, int value) {
        mPref.edit().putInt(key, value).commit();
    }

    public int getInt(String key, int defValue) {
        return mPref.getInt(key, defValue);
    }

    public void putLong(String key, long value) {
        mPref.edit().putLong(key, value).commit();
    }

    public long getLong(String key, long defValue) {
        return mPref.getLong(key, defValue);
    }

    public void putBoolean(String key, boolean value) {
        mPref.edit().putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mPref.getBoolean(key, defValue);
    }

    public void remove(String key) {
        mPref.edit().remove(key).commit();
    }

    public boolean clear() {
        return mPref.edit().clear().commit();
    }

    public boolean isContain(String key){
        return mPref.contains(key);
    }
}
