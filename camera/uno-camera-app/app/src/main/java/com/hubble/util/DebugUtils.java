package com.hubble.util;

import android.content.Context;

import com.hubble.HubbleApplication;


/**
 * Created by hoang on 8/31/16.
 */
public class DebugUtils {
    public static boolean needForceSecureRemoteStreaming() {
        boolean forceSecure=false;
        //forceSecure = HubbleApplication.appSettings.getBoolean(DebugFragment.PREFS_FORCE_SECURE_REMOTE_STREAMING,
         //       DebugFragment.FORCE_SECURE_REMOTE_STREAMING_DEFAULT);
        return forceSecure;
    }
}
