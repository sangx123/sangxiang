package com.hubble.util;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.ui.DebugFragment;


import base.hubble.PublicDefineGlob;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

/**
 * Created by hoang on 10/19/16.
 */

public class P2pSettingUtils {
    private static P2pSettingUtils sInstance = null;
    private SecureConfig mAppConfig;

    public static synchronized P2pSettingUtils getInstance() {
        if (sInstance == null) {
            sInstance = new P2pSettingUtils();
        }
        return sInstance;
    }

    private P2pSettingUtils() {
        mAppConfig = HubbleApplication.AppConfig;
    }

    /**
     * Check whether app has p2p feature or not.
     * Currently, only Hubble V4 app has p2p.
     */
    public static boolean hasP2pFeature() {
        boolean hasP2p = true;
        if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") ||
                BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew") ||
                BuildConfig.FLAVOR.equalsIgnoreCase("inanny") ||
                BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
            hasP2p = true;
        }
        return hasP2p;
    }

    /**
     * Check whether P2P streaming is enabled or not.
     *
     * @return true if P2P streaming is enabled.
     */
    public boolean isP2pStreamingEnabled() {
        boolean isP2pEnabled = true;
        isP2pEnabled = mAppConfig.getBoolean(PublicDefineGlob.PREFS_IS_P2P_ENABLED, true);
        return isP2pEnabled;
    }

    public boolean isP2pBackgroundKeepAliveEnabled() {
        boolean isP2pBackgroundKeepAliveEnabled = P2pManager.P2P_KEEP_ALIVE_SETTING_DEFAULT;
        isP2pBackgroundKeepAliveEnabled = mAppConfig.getBoolean(P2pManager.PREFS_P2P_KEEP_ALIVE_SETTING, P2pManager.P2P_KEEP_ALIVE_SETTING_DEFAULT);
        return isP2pBackgroundKeepAliveEnabled;
    }

    /**
     * Check "Remote P2P Streaming" settings (This can be changed on Account Setting Page).
     *
     * @return true if remote P2P streaming is enabled.
     */
    public boolean isRemoteP2pStreamingEnabled() {
        return isP2pStreamingEnabled();
    }

    public boolean isRelayP2pStreamingEnabled() {
        boolean isEnabled = true;
        if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") ||
                BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew") ||
                BuildConfig.FLAVOR.equalsIgnoreCase("inanny") ||
                BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
            isEnabled = true;
        }
        return isEnabled;
    }

    public boolean isForceRelayP2pEnabled() {
        boolean isEnabled = false;
        return isEnabled;
    }

    public boolean isP2pPlayByTimestampEnabled() {
        boolean isEnabled = false;
        // Get p2p play by timestamp enabled setting, default is off
        isEnabled = mAppConfig.getBoolean(DebugFragment.PREFS_ENABLED_P2P_PLAY_BY_TIMESTAMP, false);
        return isEnabled;
    }

    public boolean isP2pFrameFilteringEnabled() {
        boolean isEnabled = false;
        // Get p2p corrupted frame filtering setting, default is on
        isEnabled = mAppConfig.getBoolean(DebugFragment.PREFS_ENABLED_CORRUPTED_FRAME_FILTERING, true);
        return isEnabled;
    }

    public boolean isRtmpStreamingEnabled() {
        boolean isEnabled;
        // Default OFF for Vtech, ON for non-Vtech
        if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
            isEnabled = mAppConfig.getBoolean(DebugFragment.PREFS_ENABLED_RTMP_STREAMING, false);
        } else {
            isEnabled = mAppConfig.getBoolean(DebugFragment.PREFS_ENABLED_RTMP_STREAMING, true);
        }
        return isEnabled;
    }

    public boolean isRtspStreamingEnabled() {
        return true;
    }
}
