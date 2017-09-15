package com.hubble;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.hubble.registration.PublicDefine;
import com.hubbleconnected.camera.BuildConfig;

/**
 * Created by CVision on 3/10/2016.
 */
public class BaseActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLanguageRTL();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceAlign(boolean isRight) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (isRight) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            } else {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            }
        }
    }

    private void checkLanguageRTL() {
        String currentLocale = getResources().getConfiguration().locale.toString();
        if (BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equals("hubblenew")) {
            if (currentLocale.startsWith("ar") || currentLocale.startsWith("iw") || currentLocale.startsWith("he")) {
                HubbleApplication.AppConfig.putBoolean(PublicDefine.DISPLAY_RTL, true);
                forceAlign(true);
            } else {
                forceAlign(false);
                HubbleApplication.AppConfig.remove(PublicDefine.DISPLAY_RTL);
            }
        }
    }
}