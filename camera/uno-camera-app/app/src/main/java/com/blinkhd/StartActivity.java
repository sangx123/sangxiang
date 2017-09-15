package com.blinkhd;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.activeandroid.util.SQLiteUtils;
import com.crashlytics.android.Crashlytics;
import com.firmware.FirmwareUpdateActivity;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.util.CommonConstants;
import com.msc3.registration.LaunchScreenActivity;
import com.msc3.registration.SplashScreen;
import com.nxcomm.blinkhd.ui.Global;
import com.util.CommonUtil;

import java.util.Locale;

import base.hubble.Api;
import base.hubble.PublicDefineGlob;
import io.fabric.sdk.android.Fabric;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;


//TODO: Currently the entrypoint for the application - needs cleanup - remove statics
public class StartActivity extends FragmentActivity {
  private static final String TAG = "MainActivity";
  public static final String COME_FROM = "COME_FROM";
  public static final String COME_FROM_GCM = "COME_FROM_GCM";
  public static final String COME_FROM_CAM_CONFIGURE = "COME_FROM_CAM_CONFIGURE";
  ImageView hubbleSpinner;
 // AnimationDrawable animationDrawable;

  public static boolean isFirstTime;
  private AnimationDrawable anim = null;
  private SecureConfig settings = HubbleApplication.AppConfig;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Crashlytics());
    setContentView(R.layout.bb_is_first_screen);

    hubbleSpinner = (ImageView) findViewById(R.id.imageLoader);

     hubbleSpinner.setVisibility(View.VISIBLE);

      /*hubbleSpinner.setImageResource(R.drawable.loader_anim1);

     animationDrawable = (AnimationDrawable) hubbleSpinner.getDrawable();

      animationDrawable.start();*/
    if (!settings.getBoolean("clear_timeline_data", false)) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          // clear old data from timeline avoid caching data in Event list screen
          Log.d(TAG, "clear old data of timeline ....");
          try {
            SQLiteUtils.execSql("DELETE FROM DeviceEventGeneralData");
            SQLiteUtils.execSql("DELETE FROM DeviceEvent");
            SQLiteUtils.execSql("DELETE FROM DeviceEventDate");

            Log.d(TAG, "clear old data of timeline DONE. Status = successful");
            settings.putBoolean("clear_timeline_data", true);
          } catch (Exception e) {
            Log.d(TAG, "clear old data of timeline DONE. Status = failed");
            e.printStackTrace();
          }
        }
      }).start();
    }

    Api.getInstance().deleteAllNotifications();

//    if (P2pSettingUtils.hasP2pFeature()) {
//      Intent serviceIntent = new Intent(this.getApplicationContext(), P2pService.class);
//      Log.d(TAG, "isP2pServiceRunning? " + P2pUtils.isP2pServiceRunning(this));
//      if (!P2pUtils.isP2pServiceRunning(this)) {
//        Log.i(TAG, "Start new p2p service");
//        startService(serviceIntent);
//      }
//    }

    //no need track App Launch event at this time. Tracking this event will increase data points
//    //AA-1415: track event App Launch
//    String come_from = getIntent().getExtras() != null ? getIntent().getExtras().getString(COME_FROM, null) : null;

  }

  @Override
  protected void onStart() {
    super.onStart();
        /* Issue: Users upgrade from V2 to V4 app, they have to re-login.
         * We need to check preference value of the V2 app first for backward compatible.
         */
    if (BuildConfig.FLAVOR.equals("hubblefirmware")) {
        startActivity(new Intent(StartActivity.this, FirmwareUpdateActivity.class));
        finish();
    } else {
      boolean isNotFirstTimeFromV2 = settings.getBoolean(PublicDefineGlob.PREFS_V2_IS_NOT_FIRST_TIME, false);
      if (isNotFirstTimeFromV2 == true) {
        isFirstTime = false;
        HubbleApplication.AppConfig.remove(PublicDefineGlob.PREFS_V2_IS_NOT_FIRST_TIME);
        String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
        String savedUser = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, null);
        if (apiKey != null && savedUser != null) {
          // If old login credentials existed, app should auto login for backward compatible
          settings.putBoolean(CommonConstants.shouldNotAutoLogin, false);
        }
      } else {
        isFirstTime = settings.getBoolean(PublicDefineGlob.PREFS_IS_FIRST_TIME, true);
      }
      Log.d(TAG,"isFirstTime :- " + isFirstTime);
      if (Global.getApiKey(this) == null) {
        proceedWithInitialSetup();
      }else if (isFirstTime) {
        settings.putBoolean(PublicDefineGlob.PREFS_MBP_VOX_ALERT_IS_RECURRING, true);
        settings.putBoolean(PublicDefineGlob.PREFS_MBP_VOX_ALERT_ENABLED_IN_CALL, true);
        settings.putBoolean(PublicDefineGlob.PREFS_MBP_CAMERA_DISCONNECT_ALERT, true);
        String language = Locale.getDefault().getDisplayName();

        if (language.equalsIgnoreCase(Locale.US.getDisplayName())) {
          settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_F);
        } else {
          settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
        }

        proceedWithInitialSetup();
      } else {
        proceedDirectly();
      }
    }

  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    if (hasFocus && anim != null) {
      anim.start();
    }
  }

  private void proceedDirectly() {

    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
      //  if(animationDrawable != null && animationDrawable.isRunning())
       //   animationDrawable.stop();
        //Do something after 100ms
        Intent entry = new Intent(StartActivity.this, SplashScreen.class);
        entry.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(entry);

        finish();
        overridePendingTransition(0, 0);
      }
    }, 1000);

  }

  private void proceedWithInitialSetup() {
    Intent entry = new Intent(StartActivity.this, LaunchScreenActivity.class);
    entry.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    startActivity(entry);

    finish();
    overridePendingTransition(0, 0);
  }


}

