package com.hubble.registration.ui;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.registration.SetupDataCache;
import com.hubble.registration.services.CameraDetectorService;
import com.hubble.registration.tasks.CameraConnectivityDetector;
import com.hubbleconnected.camera.R;

import base.hubble.PublicDefineGlob;
import base.hubble.constants.Camera;
import base.hubble.meapi.PublicDefines;

public class FirstTimeActivity extends FragmentActivity {

  public static final String bool_DirectModeShowInfoAndConnect = "directModeShowInfoAndConnect";
  /* first time setup will set this flag */
  public static final String bool_InfraModeInitialSetup = "InfraModeInitialSetup";

  public static final String bool_InfraMode = "InfraMode";
  public static final String bool_ExitToIdleScreen = "CloseAppAndReturn";

  private static final int DIALOG_STORAGE_UNAVAILABLE = 1;
  private SecureConfig settings = HubbleApplication.AppConfig;
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // query server for versions- everytime activity is started

    String phonemodel = android.os.Build.MODEL;
    if (phonemodel.equals(PublicDefineGlob.PHONE_MBP2k) ||
        phonemodel.equals(PublicDefineGlob.PHONE_MBP1k)) {
      if (isCamDetectServiceRunning(this)) {
        // // Log.d("mbp", "Stop Cam Detector");
        Intent i = new Intent(this, CameraDetectorService.class);
        stopService(i);
      }

			/*20121204: phung: clear all disconnect alerts */
      NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      for (int i = 0; i < CameraConnectivityDetector.notificationIds.length; i++) {
        notificationManager.cancel(CameraConnectivityDetector.notificationIds[i]);
      }
    }
  }

  /* 20130305: hoang:
   * @see android.app.Activity#onCreateDialog(int)
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    AlertDialog.Builder builder;
    AlertDialog alert;
    Spanned msg;
    switch (id) {
      case DIALOG_STORAGE_UNAVAILABLE:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml("<big>" + getString(R.string.usb_storage_is_turned_on_please_turn_off_usb_storage_before_launching_the_application) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                FirstTimeActivity.this.finish();
              }
            }
        );

        alert = builder.create();
        return alert;
    }
    return super.onCreateDialog(id);
  }

  protected void onNewIntent(Intent intent) {
    this.setIntent(intent);

  }

  protected void onPause() {
    super.onPause();
  }


  protected void onStop() {

    super.onStop();
  }


  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    //Do nothing
    // // Log.d("mbp", "onConfigurationChanged- newconf: " + newConfig);
  }

  protected void onStart() {
    super.onStart();

    String serverUrl = settings.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, getString(R.string.server_url_endpoint));
    PublicDefines.SERVER_URL = serverUrl;

    setContentView(R.layout.bb_is_waiting_screen);
    // // Log.d("mbp", "First Time on Start ");
    // // Log.d("mbp", "proceed 1... ");
    proceed();
  }

  private void proceed() {
    /* We may be waken up by some other intent */
    Bundle extra = getIntent().getExtras();
    if (extra != null) {
      boolean goDirectOrInfra = extra.getBoolean(bool_DirectModeShowInfoAndConnect);

      goDirectOrInfra = extra.getBoolean(bool_InfraMode);
      if (goDirectOrInfra) {
        //TODO: fix!!!
        /*
        getIntent().removeExtra(bool_InfraMode);
        Intent entry = new Intent(FirstTimeActivity.this, LoginOrRegistrationActivity.class);
        FirstTimeActivity.this.startActivity(entry);
        */
        return;
      }

      goDirectOrInfra = extra.getBoolean(bool_InfraModeInitialSetup);
      if (goDirectOrInfra) {

        //20131218:phung: change flow, initial just show Login screen , if user does not have any account
        //    they will need to click "Create Account"
        getIntent().removeExtra(bool_InfraModeInitialSetup);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.bb_server_url_new_name);
        dialog.setCancelable(true);

        //setup connect button
        Button connect = (Button) dialog.findViewById(R.id.change_btn);
        connect.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
            EditText text = (EditText) dialog.findViewById(R.id.text_new_name);
            if (text != null) {
              String serverUrl = "https://" + text.getText().toString().trim() + "/v1";
              if (!serverUrl.isEmpty()) {
                // // Log.d("mbp", "New server URL: " + serverUrl);
                PublicDefines.SERVER_URL = serverUrl;
                settings.putString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, serverUrl);
              }
            }
            dialog.cancel();
          }
        });


        Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
            dialog.cancel();
          }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

          @Override
          public void onCancel(DialogInterface dialog) {

            //TODO: FIX
            //GO to first time setup
            /*
            Intent entry = new Intent(FirstTimeActivity.this, LoginOrRegistrationActivity.class);
            entry.putExtra(LoginOrRegistrationActivity.bool_createUserAccount, true);
            FirstTimeActivity.this.startActivity(entry);
            */
          }
        });
        dialog.show();

        return;
      }

      boolean exit = extra.getBoolean(bool_ExitToIdleScreen);
      if (exit) {
        finish();
        return;
      }

			/*20120913: phung: vox here will just show the camera list 
       *  extra string_VoxDeviceAddr is useless for now.
			 *  
			 *  NEW: If there is no OFFLine data here we launch LoginOrRegistrationActivity instead
			 *  
			 */
      boolean voxInfra = extra.getBoolean(Camera.bool_VoxInfraMode);
      if (voxInfra) {
        getIntent().removeExtra(Camera.bool_VoxInfraMode);
        String voxDevice = extra.getString(Camera.string_VoxDeviceAddr);
        getIntent().removeExtra(Camera.string_VoxDeviceAddr);

        settings.putString(PublicDefineGlob.PREFS_SELECTED_CAMERA_MAC_FROM_CAMERA_SETTING, voxDevice);

        //Try to restore data
        try {
          if (new SetupDataCache().restore_session_data(getExternalFilesDir(null))) {
            /*
            Intent entry_intent = new Intent(FirstTimeActivity.this, SettingsActivity.class);
            //entry_intent.putExtra(EntryActivity.string_voxDeviceAddr, voxDevice);
            entry_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(entry_intent);
            */
          } else {
            // // Log.d("mbp", "Can't restore offline file  -> Login page instead");
            /*
            Intent entry = new Intent(FirstTimeActivity.this, LoginOrRegistrationActivity.class);
            FirstTimeActivity.this.startActivity(entry);
            */
          }
        } catch (Exception e) {
          // // Log.d("mbp", e.getLocalizedMessage());
          showDialog(DIALOG_STORAGE_UNAVAILABLE);

        }


      }


    } else {
      //main exit point
      // // Log.d("mbp", "FirstTimeAct: NOthing to do .. exit");
      finish();
    }


  }

  protected void onDestroy() {
    super.onDestroy();


		/*20120726: issue 450 - stop vox if app is close */ 

		/*20121116: issue 714 - Stop vox only if user logout 
		 * 
		if (LoginOrRegistrationActivity.isVOXServiceRunning(FirstTimeActivity.this))
		{
			// // Log.d("mbp", "ARGG Stop service when app close");
			Intent i = new Intent(this,VoiceActivationService.class);
			stopService(i);
		}

		 */

    String phonemodel = android.os.Build.MODEL;
    if (phonemodel.equals(PublicDefineGlob.PHONE_MBP2k) || phonemodel.equals(PublicDefineGlob.PHONE_MBP1k)) {
      if (!isCamDetectServiceRunning(this)) {
        // // Log.d("mbp", "Start Cam Detector");
        Intent i = new Intent(this, CameraDetectorService.class);
        startService(i);
      }
    }


  }

  public static boolean isCamDetectServiceRunning(Context c) {
    ActivityManager manager = (ActivityManager) c.getSystemService(ACTIVITY_SERVICE);
    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if ("com.msc3.CameraDetectorService".equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }


}

