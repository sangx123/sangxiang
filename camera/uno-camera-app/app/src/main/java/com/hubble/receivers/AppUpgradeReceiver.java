package com.hubble.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;


import com.hubbleconnected.camera.BuildConfig;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.util.CommonUtil;

/**
 * Created by Sean on 15-04-17.
 */
public class AppUpgradeReceiver extends WakefulBroadcastReceiver {
  private static final String TAG = "AppUpgradeReceiver";
  public static final String APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID;
  public static final int APP_VERSION_FOR_FORCE_LOGOUT = 5000;


  @Override
  public void onReceive(Context context, Intent intent) {
    /*
   if(context.getPackageName().equals(APP_PACKAGE_NAME) && CommonUtil.getForceUpgradeValueFromSP(context.getApplicationContext(),CommonUtil.APP_FORCE_UPGRADE_KEY,true)) {
      Log.d(TAG,"Hubble App is upgraded");
      try {
        int currentBuild = context.getPackageManager().getPackageInfo(APP_PACKAGE_NAME, 0).versionCode;
        Log.d(TAG, "App is upgraded" + currentBuild + "package name" + context.getPackageName());
        if(currentBuild >= APP_VERSION_FOR_FORCE_LOGOUT){
          CommonUtil.setForceUpgradeValueToSP(context.getApplicationContext(),CommonUtil.APP_FORCE_UPGRADE_KEY, false);
          CommonUtil.setForceUpgradeValueToSP(context.getApplicationContext(),CommonUtil.APP_FORCE_LOGOUT_KEY, true);
          CommonUtil.setForceUpgradeValueToSP(context.getApplicationContext(),CommonUtil.APP_FORCE_UPGRADE_ONBOARDING_KEY, true);

        }
      } catch (PackageManager.NameNotFoundException ignored) {
      }
    }
    */


    /*if (shouldSendUpdateMessage) {
      try {
        String currentBuild = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        SecureConfig settings = HubbleApplication.AppConfig;
        if(currentBuild. )
        /*if (currentBuild == null || !settings.getString(PublicDefine.PREFS_APP_VERSION_NUMBER, "").equals(currentBuild)) {
          settings.putString(PublicDefine.PREFS_APP_VERSION_NUMBER, currentBuild);
          //sendUpdateNotification(context);
        }

      } catch (PackageManager.NameNotFoundException ignored) {
      }
    }*/
  }

  private void sendUpdateNotification(Context context) {
    int icon = R.drawable.ic_stat_notification;
    String eventTitle = context.getString(R.string.app_brand_application_name);

    NotificationCompat.Builder mBuilder;
    mBuilder = new NotificationCompat.Builder(context);
    mBuilder.setAutoCancel(true)
        .setSmallIcon(icon)
        .setContentTitle(eventTitle)
        .setColor(context.getResources().getColor(R.color.app_theme_color))
        .setContentText("Check out our new settings page!");

    Intent notificationIntent = new Intent(context, MainActivity.class);
    notificationIntent.putExtra("FROM_UPDATE", true);
    PendingIntent intent = PendingIntent.getActivity(context, 12 + 1000, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(intent);
    ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(12 + 1000, mBuilder.build());

  }
}
