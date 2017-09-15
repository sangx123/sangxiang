package com.hubble.analytics;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import com.google.android.gms.analytics.ExceptionParser;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;

import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import base.hubble.PublicDefineGlob;

/**
 * Created by QuayChenh on 2/18/2016.
 */
public class GoogleAnalyticsController {

    private static final String TAG = "GoogleAnalyticsController";
    public static final String GA_ACTION_P2P_FAILED_TOO_FEW_KEY_FRAMES = "Too few key frames";
    private static GoogleAnalyticsController instance;

    public static GoogleAnalyticsController getInstance() {
        if (instance == null) {
            instance = new GoogleAnalyticsController();
        }
        return instance;
    }

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap< TrackerName, Tracker> mTrackers = new HashMap< TrackerName, Tracker >( );

    synchronized public Tracker getTracker( TrackerName trackerId ) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(HubbleApplication.AppContext);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            analytics.enableAutoActivityReports(HubbleApplication.AppContext);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(HubbleApplication.AppContext.getString(R.string.ga_trackingId))
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker) : analytics
                    .newTracker(R.xml.ecommerce_tracker);
            t.enableAdvertisingIdCollection(true);
            Thread.setDefaultUncaughtExceptionHandler(new AnalyticsExceptionReporter(t,
                    Thread.getDefaultUncaughtExceptionHandler(), HubbleApplication.AppContext));

            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

    public void sendCreateSessionEvent(Activity mActivity, boolean isSuccess) {
        List<GAEventAction> actions = new ArrayList<GAEventAction>();
        String success = isSuccess ? "Create Session Success" : "Create Session Failed";
        actions.add(new GAEventAction("Event", success));
        String connectionType = "";

        if (haveInternetViaOtherMedia(mActivity)) {
            connectionType = "Relay/Mobile Data";
        } else {
            connectionType = "Relay/Wifi";
        }
        actions.add(new GAEventAction("Connection Type", connectionType));
        trackEvent(new GAEvent("Accessing Stream", actions));
    }

    public void sendOpenWowzaStreamEvent(Activity mActivity, boolean isSuccess) {
        sendOpenWowzaStreamEvent(mActivity, true, isSuccess);
    }

    public void sendOpenWowzaStreamEvent(Activity mActivity, boolean viewRelayRtmp, boolean isSuccess) {
        if (isSuccess || viewRelayRtmp) {
            List<GAEventAction> actions = new ArrayList<GAEventAction>();
            String success = isSuccess ? "Open Session Success" : "Open Session Failed";
            actions.add(new GAEventAction("Event", success));

            String connectionType = "";
            if (haveInternetViaOtherMedia(mActivity)) {
                connectionType = "Relay/Mobile Data";
            } else {
                connectionType = "Relay/Wifi";
            }
            actions.add(new GAEventAction("Connection Type", connectionType));
            trackEvent(new GAEvent("Accessing Stream", actions));
        }
    }

    public void sendOpenSessionSuccessEvent(Activity mActivity, boolean isP2pStreaming, boolean isInLocal, Device selectedDevice) {
        boolean usingMobileData = haveInternetViaOtherMedia(mActivity);
        String cam_model = selectedDevice.getProfile().getModelId();
        String cam_firmware = selectedDevice.getProfile().getFirmwareVersion();
        String connectionType = "";
        if (isP2pStreaming) {
            if (isInLocal == true) {
                connectionType = "P2P/Local/Success";
            } else {
                if (usingMobileData == true) {
                    connectionType = "P2P/Remote/Mobile Data/Success";
                } else {
                    connectionType = "P2P/Remote/Wifi/Success";
                }
            }
        } else {
            if (isInLocal == true) {
                connectionType = "RTSP/Success";
            } else {
                if (usingMobileData == true) {
                    connectionType = "RTMP2/Mobile Data/Sucess";
                } else {
                    connectionType = "RTMP2/Wifi/Success";
                }
            }
        }
        trackEvent("P2P Streaming 2", connectionType, cam_model + "," + cam_firmware);
    }

    public void sendOpenSessionFailedEvent(Activity mActivity, boolean isP2pStreaming, boolean isInLocal, Device selectedDevice) {
        boolean usingMobileData = haveInternetViaOtherMedia(mActivity);
        String cam_model = selectedDevice.getProfile().getModelId();
        String cam_firmware = selectedDevice.getProfile().getFirmwareVersion();

        String connectionType = "";
        if (isP2pStreaming) {
            if (isInLocal == true) {
                connectionType = "P2P/Local/Failed";

            } else {
                if (usingMobileData == true) {
                    connectionType = "P2P/Remote/Mobile Data/Failed";
                } else {
                    connectionType = "P2P/Remote/Wifi/Failed";
                }
            }
        } else {
            if (isInLocal == true) {
                connectionType = "RTSP/Failed";
            } else {
                if (usingMobileData) {
                    connectionType = "RTMP2/Mobile Data/Failed";
                } else {
                    connectionType = "RTMP2/Wifi/Failed";
                }
            }
        }
        trackEvent("P2P Streaming 2", connectionType, cam_model + "," + cam_firmware);
    }

    public void sendOpenP2pSessionFailedEventWithError(Activity mActivity, boolean isP2pStreaming, boolean isInLocal, Device selectedDevice, String errorMessage) {
        boolean usingMobileData = haveInternetViaOtherMedia(mActivity);
        String cam_model = selectedDevice.getProfile().getModelId();
        String cam_firmware = selectedDevice.getProfile().getFirmwareVersion();

        String connectionType = "";
        if (isP2pStreaming) {
            if (isInLocal == true) {
                connectionType = "P2P/Local/Failed";
            } else {
                if (usingMobileData == true) {
                    connectionType = "P2P/Remote/Mobile Data/Failed" + " - " + errorMessage;
                } else {
                    connectionType = "P2P/Remote/Wifi/Failed" + " - " + errorMessage;
                }
            }
            trackEvent("P2P Streaming 2", connectionType, cam_model + "," + cam_firmware);
        }
    }

    public void sendOpenNonP2pFailedError(Activity mActivity, boolean isInLocal, String errorType, Device selectedDevice) {
        if (!TextUtils.isEmpty(errorType)) {
            String cam_model = selectedDevice.getProfile().getModelId();
            String cam_firmware = selectedDevice.getProfile().getFirmwareVersion();

            String connectionType = "";
            if (isInLocal == true) {
                connectionType = "RTSP/" + errorType;
            } else if (haveInternetViaOtherMedia(mActivity)) {
                connectionType = "RTMP2/Mobile Data/" + errorType;
            } else {
                connectionType = "RTMP2/Wifi/" + errorType;
            }
            trackEvent("P2P Streaming 2", connectionType, cam_model + "," + cam_firmware);
        }
    }

    /**
     * @param context
     * @return - True if internet connection is available on 3g False otherwise
     */
    public static boolean haveInternetViaOtherMedia(Context context) {
        if (Build.MODEL != null && (Build.MODEL.equals(PublicDefineGlob.PHONE_MBP2k) || Build.MODEL.equals(PublicDefineGlob.PHONE_MBP1k) || Build.MODEL.equals(PublicDefineGlob.PHONE_IHOMEPHONE5))) {
            return false;
        }

        if (context == null) {
            // you're gonna have a bad time.
            return false;
        }

        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
            // mobile
            NetworkInfo.State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                // mobile
                return true;
            }
        }
        return false;
    }

    public void trackCameraInfo(String category, Device selectedDevice) {
        trackCameraInfo(category, selectedDevice, selectedDevice.isAvailableLocally());
    }

    public void trackCameraInfo(String category, Device selectedDevice, boolean isInLocal) {
        List<GAEventAction> actions = new ArrayList<GAEventAction>();
        actions.add(new GAEventAction("Camera Model", selectedDevice.getProfile().getModelId()));
        actions.add(new GAEventAction("Firmware Version", selectedDevice.getProfile().getFirmwareVersion()));

        String isRemote = isInLocal ? "No" : "Yes";
        actions.add(new GAEventAction("Is Remote", isRemote));
        trackEvent(new GAEvent(category, actions));
    }

    public void trackEvent(String category) {
        trackEvent(category, "", "", 0);
    }

    public void trackScreen(String screenName) {
        Tracker t = getTracker(TrackerName.APP_TRACKER);
        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void trackEvent(GAEvent event) {
        if (event != null) {
            Tracker t = getTracker(TrackerName.APP_TRACKER);
            List<GAEventAction> actions = event.getActions();
            if (actions != null && actions.size() > 0) {
                for (GAEventAction action : actions) {
                    t.send(new HitBuilders.EventBuilder().setCategory(event.getCategory()).setAction(action.getAction())
                            .setLabel(action.getLabel()).setValue(action.getValue()).build());
//                    android.util.Log.d(TAG, "****************************************");
//                    android.util.Log.d(TAG, "Category: " + event.getCategory() + "| Action: " + action.getAction() + " | Label: " + action.getLabel());
                }
            } else {
                t.send(new HitBuilders.EventBuilder().setCategory(event.getCategory()).setAction("")
                        .setLabel("").setValue(0L).build());
            }
        }
    }

    public void trackEvent(String category, String action, String label) {
        trackEvent(category, action, label, 1L);
//        android.util.Log.d(TAG, "****************************************");
//        android.util.Log.d(TAG, "Category: " + category + "| Action: " + action + " | Label: " + label);
    }

    public void trackEvent(String category, String action, String label, long value) {
        Tracker t = getTracker(TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).setValue(value).build());
//        android.util.Log.d(TAG, "****************************************");
//        android.util.Log.d(TAG, "Category: " + category + "| Action: " + action + " | Label: " + label);
    }

    public void sendException(String titleDescription, Throwable throwable) {
        Tracker t = getTracker(TrackerName.APP_TRACKER);
        // Build and send exception.
        String eDescription = titleDescription;
        if (throwable != null) {
            if (!TextUtils.isEmpty(titleDescription)) {
                eDescription = eDescription + "\n";
            }
            eDescription = eDescription + getExceptionDescription(throwable);
        }
        //android.util.Log.d(TAG, "************************");
        //android.util.Log.d(TAG, eDescription);
        t.send(new HitBuilders.ExceptionBuilder().setDescription(eDescription).setFatal(false).build());
    }

    private class AnalyticsExceptionReporter extends ExceptionReporter {

        public AnalyticsExceptionReporter(Tracker tracker, Thread.UncaughtExceptionHandler originalHandler, Context context) {
            super(tracker, originalHandler, context);
            setExceptionParser(new AnalyticsExceptionParser());
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            String exceptionDescription =  getExceptionParser().getDescription(t.getName(), e);
            //Add code to store the exception stack trace in shared preferences
            super.uncaughtException(t, e);
        }
    }

    private class AnalyticsExceptionParser implements ExceptionParser {

        @Override
        public String getDescription(String arg0, Throwable arg1) {
            return getExceptionDescription(arg1);
        }
    }

    private String getExceptionDescription(Throwable arg1) {
        String exceptionDescription = getExceptionInfo(arg1, "", true) + getCauseExceptionInfo(arg1.getCause());

        //150 Bytes is the maximum allowed by Analytics for custom dimensions values. Assumed that 1 Byte = 1 Character (UTF-8)
        if(exceptionDescription.length() > 150)
            exceptionDescription = exceptionDescription.substring(0, 150);

        return exceptionDescription;
    }

    private String getCauseExceptionInfo(Throwable t) {
        String causeDescription = "";
        while(t != null && causeDescription.isEmpty()) {
            causeDescription = getExceptionInfo(t, BuildConfig.APPLICATION_ID, false);
            t = t.getCause();
        }
        return causeDescription;
    }

    private String getExceptionInfo(Throwable t, String packageName, boolean includeExceptionName) {
        String exceptionName = "";
        String fileName = "";
        String lineNumber = "";

        for (StackTraceElement element : t.getStackTrace()) {
            String className = element.getClassName().toString().toLowerCase();
            if(packageName.isEmpty() || (!packageName.isEmpty() && className.contains(packageName))){
                exceptionName = includeExceptionName ? t.toString() : "";
                fileName = element.getFileName();
                lineNumber = String.valueOf(element.getLineNumber());
                return exceptionName + "@" + fileName + ":" + lineNumber;
            }
        }
        return "";
    }
}
