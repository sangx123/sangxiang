package com.hubble.receivers;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.IntentCompat;
import android.text.TextUtils;


import com.hubble.HubbleApplication;
import com.msc3.registration.LoginActivity;
import com.nxcomm.blinkhd.ui.SessinExpireActivity;

import base.hubble.PublicDefineGlob;
import base.hubble.meapi.PublicDefines;

/**
 * Created by aruna on 20/06/17.
 */

public class SessionExpireReceiver extends BroadcastReceiver{
    public static final String SESSION_EXPIRE_INTENT = "com.hubble.receivers.SessionExpireReceiver";
    private static final String TAG = "SessionExpireReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent.hasExtra("ignore_error")) {
            String errorFrom = intent.getStringExtra("ignore_error");
            if (errorFrom.contains("LoginActivity") ||
                    errorFrom.contains("AccountManagement") ||
                    errorFrom.contains("UnregistrationIntentService") ||
                    errorFrom.contains("EmailConfirmationFragment") ||
                    errorFrom.contains("ChangeEmailFragment")) {
                return;
            }
        }

        if (TextUtils.isEmpty(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null))) {
            return;
        }

        Intent launchLogin = new Intent(context, SessinExpireActivity.class);
        launchLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        launchLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        context.startActivity(launchLogin);
    }
}
