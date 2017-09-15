package com.nxcomm.blinkhd.ui;


import android.app.Activity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;



import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.framework.service.notification.FirebaseManager;
import com.hubble.framework.service.notification.GCMManager;
import com.hubble.framework.service.p2p.P2pUtils;
import com.hubble.helpers.AsyncPackage;
import com.hubble.registration.SetupDataCache;
import com.hubble.util.CommonConstants;
import com.hubble.util.P2pSettingUtils;
import com.msc3.registration.LoginActivity;
import com.hubbleconnected.camera.R;


import base.hubble.Api;
import base.hubble.PublicDefineGlob;

public class SessinExpireActivity extends Activity {
    private static final String TAG = "SessinExpireActivity";
    private SecureConfig settings = HubbleApplication.AppConfig;

    Button ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sessin_expire);

        ok = (Button) findViewById(R.id.ok);
        Log.i(TAG, "Created SessinExpireActivity dialog");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserLogout();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
       Log.i("Do nothing", "Please");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        //replaces the default 'Back' button action
        if(keyCode==KeyEvent.KEYCODE_BACK)   {
           return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    public void onUserLogout() {




        AsyncPackage.doInBackground(new Runnable()
        {
            @Override
            public void run()
            {
                // unregister notification first
                sendUnregistrationToBackend();

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Api.getInstance().deleteDatabase();

                        // DeviceSingleton.getInstance().clearDevices();

                        boolean offlineMode = settings.getBoolean(PublicDefineGlob.PREFS_USER_ACCESS_INFRA_OFFLINE, false);

                        if (!offlineMode)
                        {
                            // vox service should not take wakelock
                            settings.putBoolean(PublicDefineGlob.PREFS_VOX_SHOULD_TAKE_WAKELOCK, false);
                            // remove password when user logout
                            boolean notAuto = settings.getBoolean(CommonConstants.shouldNotAutoLogin, true);
                            if (notAuto) {
                                settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                            } else {
                                settings.remove(CommonConstants.shouldNotAutoLogin);
                                settings.putBoolean(PublicDefineGlob.REMEBER_PASS, true);
                            }
                            // Remove offline data.
                            new SetupDataCache().clear_session_data(getExternalFilesDir(null));

                            // Remove all pending notification on Status bar
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancelAll();

                            Intent new_login = new Intent(SessinExpireActivity.this, LoginActivity.class);
                            new_login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(new_login);

                        }



                        if (P2pSettingUtils.hasP2pFeature()) {
                            // When user logged out, stop p2p service if it's running
                            P2pUtils.stopP2pService(SessinExpireActivity.this);
                        }

                        SessinExpireActivity.this.finish();
                    }
                });
            }
        });

    }

    private void sendUnregistrationToBackend()
    {
        String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
        long appId = settings.getLong(PublicDefineGlob.PREFS_PUSH_NOTIFICATION_APP_ID, -1);

        Log.d(TAG, "unregister notification, app id: " + appId);
        if (appId != -1 && apiKey != null)
        {
            GCMManager.getInstance(this).unregisterGCM(apiKey,(int)appId);
            FirebaseManager.getInstance(getApplicationContext()).stopNotificationService();
        }

        Log.d(TAG, "Finish unregister notification, app id: " + appId);
    }
}
