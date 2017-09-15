package com.hubble.registration.tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.discovery.LocalScanForCameras;
import com.discovery.ScanProfile;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.registration.SetupDataCache;
import com.hubble.registration.interfaces.ICameraScanner;
import com.hubble.registration.models.LegacyCamProfile;
import com.hubble.registration.ui.FirstTimeActivity;
import com.hubbleconnected.camera.R;

import java.util.ArrayList;

import base.hubble.PublicDefineGlob;
import base.hubble.constants.Camera;

/**
 * @author Nguyen
 */
public class CameraConnectivityDetector implements Runnable, ICameraScanner {
  private static final int SLEEP_PERIOD_30s = 30 * 1000; //30sec
  private static final String TAG = "CameraConnectivityDetector";

  private Context mContext;
  private String home_ssid;
  private LegacyCamProfile[] _localCameras;
  private boolean running;
  public static final int[] notificationIds = {11111, 11112, 11113, 11114};
  public static final int VOX_STORAGE_UNAVAILABLE_ID = 11115;

  private WakeLock keep_cpu_running;

  public CameraConnectivityDetector(Context mContext) {
    this.mContext = mContext;
    _localCameras = null;
    home_ssid = "";

    WifiManager w = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    if ((w != null) && (w.isWifiEnabled()) && (w.getConnectionInfo() != null)) {

      home_ssid = w.getConnectionInfo().getSSID();
      // // Log.d("mbp", "at creation ssid is: " + home_ssid);
    }

    running = true;
  }

  @Override
  public void updateScanResult(ScanProfile[] results, int status, int index) {
  }

  public void stop() {
    running = false;
  }


  private void releaseLock() {
    if (keep_cpu_running != null && keep_cpu_running.isHeld()) {
      keep_cpu_running.release();
    }
  }

  private void getWakeLock() {
    releaseLock();
    PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
    keep_cpu_running = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KEEP CPU RUNNING");
    keep_cpu_running.acquire();

  }

  private boolean isInHomeWifi() {
    String current_ssid = "";
    WifiManager w = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    if ((w != null) && (w.isWifiEnabled()) && (w.getConnectionInfo() != null)) {
      current_ssid = w.getConnectionInfo().getSSID();

      if (current_ssid.equalsIgnoreCase(home_ssid)) {
        return true;
      }
    }

    return false;
  }


  /**
   * @param lastTimeRead
   * @return true - setup data is renewed-
   * false - setup data is old (no read)
   */
  private boolean readSetupDataIfNeeded(long lastTimeRead) throws Exception {

    LegacyCamProfile[] offline_profiles = null;
    SetupDataCache readData = new SetupDataCache();
    if (readData.hasUpdate(mContext.getExternalFilesDir(null), lastTimeRead)) {
      if (readData.restore_session_data(mContext.getExternalFilesDir(null))) {
        offline_profiles = readData.get_CamProfiles();
      }

      if (offline_profiles != null) {
        //// // Log.d("mbp", "Filtering in Local cameras .. Total cam: "+ offline_profiles.length);
        /* Filter those NOT in local */
        ArrayList<LegacyCamProfile> localCameras = new ArrayList<LegacyCamProfile>();
        for (LegacyCamProfile offline_profile : offline_profiles) {
          if (offline_profile.isInLocal()) {
            localCameras.add(offline_profile);
          }
        }


        _localCameras = localCameras.toArray(new LegacyCamProfile[1]);

        //// // Log.d("mbp", "Local cam: "+ _localCameras.length);

        return true;
      }

    }


    return false;
  }


  @Override
  public void run() {

    long lastTimeRead = 0;
    SecureConfig settings = HubbleApplication.AppConfig;
    while (running) {
      if (settings.getBoolean(PublicDefineGlob.PREFS_MBP_CAMERA_DISCONNECT_ALERT, false)) {
        home_ssid = settings.getString(PublicDefineGlob.PREFS_MBP_CAMERA_DISCONNECT_HOME_WIFI, home_ssid);

        // // Log.d("mbp", "home ssid: " + home_ssid);

        //Step 1: read  camera profiles from offline data & check which is in Local
        try {
          if (readSetupDataIfNeeded(lastTimeRead)) {
            lastTimeRead = System.currentTimeMillis();
          }
        } catch (Exception e1) {
          // // Log.d("mbp", e1.getLocalizedMessage());
          String msg = mContext.getResources().getString(R.string.service_has_stopped_due_to_usb_storage_turned_on);
          sendNotificationWithSound(msg, VOX_STORAGE_UNAVAILABLE_ID);
          break;
        }

        //     1.1: Check if there IS ANY camera in local list
        if (_localCameras == null || (_localCameras.length == 1 && _localCameras[0] == null)) {
          // // Log.e("mbp", "NO LOCAL camera to detect.. ");
        } else // THere is some local cameras...
        {

          getWakeLock();


          //Dummy arrays
          ScanProfile[] results = null;


          //2:  Start camera scan
          // // Log.d("mbp", "start scan");
          LocalScanForCameras scan_task = new LocalScanForCameras(mContext, this);
          scan_task.setShouldGetSnapshot(false);
          scan_task.startScan(_localCameras);


          try {
            results = scan_task.getScanResults(45000);

            if (isInHomeWifi()) {
              if (results != null) {
                // // Log.d("mbp", "results != null");
                // 3: Find out which LOCAL camera is OFFline ? Compare restore_profile with results.
                checkCamExistenceAndUpdate(_localCameras, results);

                // 4: --->> SETUP notification.
                for (int i = 0; i < _localCameras.length; i++) {
                  // // Log.d("mbp", "cam " + _localCameras[i].get_MAC() + " is online?  " + _localCameras[i].isInLocal());
                  if (!_localCameras[i].isInLocal()) {
                    String notifyMessage = _localCameras[i].getName() + " is offline now";
                    sendNotificationWithSound(notifyMessage, notificationIds[i]);
                  }
                }

              } else //results == null - Here it really NULL .. coz if there is any exception it wont be here.
              {
                // // Log.d("mbp", "results = null");
                for (int i = 0; i < _localCameras.length; i++) {
                  String notifyMessage = _localCameras[i].getName() + " is offline now";
                  if (i == (_localCameras.length - 1)) {
                    sendNotificationWithSound(notifyMessage, notificationIds[i]);
                  } else {
                    sendNotification(notifyMessage, notificationIds[i]);
                  }
                }
              }

            } else {
              // // Log.d("mbp", "NOT at home... ");
            }

          } catch (Exception e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
          }





						/*
             *  Possible route to this place:
						 *   0. Successfully scan
						 *   1. Timeout while scanning.
						 *   2. Not in Home wifi
						 *   
						 *  In all cases, sleep 30sec. 
						 */

          //sleep 30s
          releaseLock();

        } //if THere is some local cameras...

        if (!running) {
          break;
        }

        try {
          // // Log.d("mbp", "Thread sleep 30s");
          Thread.sleep(SLEEP_PERIOD_30s);
        } catch (InterruptedException e) {
          // // Log.e(TAG, Log.getStackTraceString(e));
        }


      } // if Setting is enable
      else {
        // // Log.w("mbp", "Setting is not enabled - EXIT thread. Until next time");
        break;
      }

    } // while (running)
  }

  /**
   * Check whether each offline camera in offlineCams exists in latest_results or not, update status for corresponding cameras.
   *
   * @param localCams      the array of offline cameras have to check
   * @param latest_results the array of online cameras returned by a scan camera function
   */
  private void checkCamExistenceAndUpdate(LegacyCamProfile[] localCams, ScanProfile[] latest_results) {
    for (LegacyCamProfile localCam : localCams) {
      localCam.setInLocal(false);
      for (ScanProfile latest_result : latest_results) {
        if ((localCam.get_MAC() != null) && (localCam.get_MAC().equalsIgnoreCase(latest_result.get_MAC()))) {
          localCam.setInetAddr(latest_result.get_inetAddress());
          localCam.setInLocal(true);
          break;
        }
      }

    }

  }

  private void sendNotificationWithSound(String message, int notifId) {
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(ns);

    int icon = R.drawable.cam_icon;
    long when = System.currentTimeMillis();

   /* Notification notification = new Notification(icon, message, when);
    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    notification.defaults = Notification.DEFAULT_ALL;*/




    Context context = mContext.getApplicationContext();      // application Context
    CharSequence contentTitle = "";
    Intent intent = new Intent(mContext, FirstTimeActivity.class);
    intent.putExtra(Camera.bool_VoxInfraMode, true);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

    //changed for build sdk 23
    //notification.setLatestEventInfo(context, contentTitle, message, contentIntent);

    Notification.Builder builder = new Notification.Builder(mContext);

    builder.setAutoCancel(false);
    builder.setContentTitle(contentTitle);
    builder.setContentText(message);
    builder.setSmallIcon(R.drawable.ic_launcher);
    builder.setContentIntent(contentIntent);
    builder.setDefaults(Notification.DEFAULT_ALL);
    builder.build();
    Notification notification = builder.getNotification();

    mNotificationManager.notify(notifId, notification);
  }

  private void sendNotification(String message, int notifId) {
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(ns);

    int icon = R.drawable.cam_icon;
    long when = System.currentTimeMillis();

    //changed for build sdk 23
    /*Notification notification = new Notification(icon, message, when);
    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;*/


    Context context = mContext.getApplicationContext();      // application Context
    CharSequence contentTitle = "";
    Intent intent = new Intent(mContext, FirstTimeActivity.class);
    intent.putExtra(Camera.bool_VoxInfraMode, true);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

    //changed for build sdk 23
    //notification.setLatestEventInfo(context, contentTitle, message, contentIntent);

    Notification.Builder builder = new Notification.Builder(mContext);

    builder.setAutoCancel(false);
    builder.setContentTitle(contentTitle);
    builder.setContentText(message);
    builder.setSmallIcon(R.drawable.ic_launcher);
    builder.setContentIntent(contentIntent);
    builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
    builder.build();
    Notification notification = builder.getNotification();

    mNotificationManager.notify(notifId, notification);
  }

}
