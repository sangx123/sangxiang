/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.msc3.registration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.Config;
import com.hubble.framework.service.configuration.AppSDKConfiguration;
import com.hubble.ui.WhyHubbleActivity;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import base.hubble.Api;
import base.hubble.PublicDefineGlob;
import base.hubble.meapi.JsonRequest;
import base.hubble.meapi.PublicDefines;

public class LaunchScreenActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "LaunchScreenActivity";
    private Button btn_login, btn_signup;
    private TextView tv_explore_products, tagLine;
    private ImageView imgChangeUrl;
    private SecureConfig settings = HubbleApplication.AppConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i("App", "On Launch Screen Show");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.bb_is_get_started_screen);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_signup = (Button) findViewById(R.id.btn_signup);
        tv_explore_products = (TextView) findViewById(R.id.tv_launch_explore_products);
        tagLine = (TextView) findViewById(R.id.tag_line);
        tv_explore_products.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        btn_signup.setOnClickListener(this);
        SettingsPrefUtils.SHOULD_READ_SETTINGS = true;
//        tagLine.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                showConfigDialog();
//                return true;
//            }
//        });

        imgChangeUrl = (ImageView) findViewById(R.id.imageView2);
        imgChangeUrl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialogChangeUrl();
                return false;
            }
        });

        ImageView changeUnoOrbit = (ImageView) findViewById(R.id.imageView);
        changeUnoOrbit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAppSelectionDialog();
                return false;
            }
        });

        tv_explore_products.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showNestSelectionDialog();
                return false;
            }
        });
    }

    private void showConfigDialog() {
        final String[] items = {"https://sn-api.hubble.in", "https://dev-api.hubble.in", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select server");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Config.setApiServerUrl(items[0]);
                } else if (item == 1) {
                    Config.setApiServerUrl(items[1]);
                } else if (item == 2) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
              Intent loginIntent = new Intent(LaunchScreenActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                break;
            case R.id.btn_signup:
                Intent signupIntent = new Intent(LaunchScreenActivity.this, RegisterActivity.class);
                startActivity(signupIntent);
                finish();
                break;
            case R.id.tv_launch_explore_products:
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.KNOW_MORE_CLICKED,AppEvents.KNOW_MORECLICKED);

                ZaiusEvent knowMoreEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
                knowMoreEvt.action(AppEvents.KNOW_MORE_CLICKED);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(knowMoreEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }

                Intent exploreIntent = new Intent(LaunchScreenActivity.this, WhyHubbleActivity.class);
                startActivity(exploreIntent);
                break;
            default:
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
       // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
    int selectedItem = 0;

    private void showAppSelectionDialog(){

        final CharSequence[] items = { "Orbit Camera", "Other Camera" };
        final String userAgent = HubbleApplication.AppContext.getUserAgentHeader();
        AlertDialog.Builder builder = new AlertDialog.Builder(LaunchScreenActivity.this);
        builder.setTitle("Select Camera to test");
        if (CommonUtil.checkIsOrbitPresent(this)) {
            if (CommonUtil.isOrbit(this)) {
              selectedItem = 0;
            }
            else
                selectedItem = 1;
        }else{
           selectedItem = 0;
        }
        builder.setSingleChoiceItems(items, selectedItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                       selectedItem = item;
                    }
                });

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                switch(selectedItem) {
                    case 0:
                        // This case if for Orbit where we enable headers
                        Api.init(LaunchScreenActivity.this, settings.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL), userAgent, BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME);
                        JsonRequest.init(userAgent, BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME);
                        AppSDKConfiguration.getInstance(LaunchScreenActivity.this).setConfigurationHeader(BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME);
                        CommonUtil.setOrbit(LaunchScreenActivity.this, true);
                        break;
                    case 1:
                        // This is for Uno/Other cameras where we disable headers
                        Api.init(LaunchScreenActivity.this, settings.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL), userAgent);
                        JsonRequest.init(userAgent);
                        CommonUtil.setOrbit(LaunchScreenActivity.this, false);
                        AppSDKConfiguration.getInstance(LaunchScreenActivity.this).resetConfigurationHeader();
                        break;
                    default:
                        Api.init(LaunchScreenActivity.this, settings.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL), userAgent, BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME);
                        JsonRequest.init(userAgent, BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME);
                        AppSDKConfiguration.getInstance(LaunchScreenActivity.this).setConfigurationHeader(BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME);
                        CommonUtil.setOrbit(LaunchScreenActivity.this, true);

                }
                Toast.makeText(LaunchScreenActivity.this, items[selectedItem], Toast.LENGTH_SHORT)
                        .show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(LaunchScreenActivity.this, "Cancelled selection", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
    private void showNestSelectionDialog(){

        final CharSequence[] items = { "Enable Works with Nest ", "Disable Works with  Nest" };
        final String userAgent = HubbleApplication.AppContext.getUserAgentHeader();
        AlertDialog.Builder builder = new AlertDialog.Builder(LaunchScreenActivity.this);
        builder.setTitle("Select Camera to test");
        if (CommonUtil.getNestConfig(this)) {

                selectedItem = 0;
            }
            else
                selectedItem = 1;

        builder.setSingleChoiceItems(items, selectedItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        selectedItem = item;
                    }
                });

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                switch(selectedItem) {
                    case 0:
                        // This case if for enabling "Works With Nest" Setting

                        CommonUtil.setNestConfig(LaunchScreenActivity.this, true);
                        break;
                    case 1:
                        // This case if for disabling "Works With Nest" Setting

                        CommonUtil.setNestConfig(LaunchScreenActivity.this, false);
                        break;
                    default:
                         CommonUtil.setNestConfig(LaunchScreenActivity.this, false);

                }
                Toast.makeText(LaunchScreenActivity.this, items[selectedItem], Toast.LENGTH_SHORT)
                        .show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(LaunchScreenActivity.this, "Cancelled selection", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
    private void showDialogChangeUrl()
    {
        final Dialog dialog = new Dialog(LaunchScreenActivity.this, R.style.myDialogTheme);
        dialog.setContentView(R.layout.bb_server_url_new_name);

        TextView currentServerName = (TextView) dialog.findViewById(R.id.textCurrentServer);
        currentServerName.setText(settings.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL));

        EditText edittext = (EditText) dialog.findViewById(R.id.text_new_name);
        edittext.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        edittext.setText(PublicDefines.SERVER_URL);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.setCancelable(true);

        Button connect = (Button) dialog.findViewById(R.id.change_btn);
        connect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EditText text = (EditText) dialog.findViewById(R.id.text_new_name);

                if (text != null && !text.getText().toString().trim().isEmpty())
                {
                    String serverUrl = text.getText().toString().trim();
                    Log.i(TAG, "New server URL: " + serverUrl);

                    PublicDefines.SERVER_URL = serverUrl;
                    settings.putString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, serverUrl);
                    if (CommonUtil.checkIsOrbitPresent(LaunchScreenActivity.this)) {
                        if (CommonUtil.isOrbit(LaunchScreenActivity.this)) {
                            Api.init(LaunchScreenActivity.this, serverUrl, HubbleApplication.AppContext.getUserAgentHeader(), BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME);
                        } else {
                            Api.init(LaunchScreenActivity.this, serverUrl, HubbleApplication.AppContext.getUserAgentHeader());

                        }
                    } else {
                        Api.init(LaunchScreenActivity.this, serverUrl, HubbleApplication.AppContext.getUserAgentHeader(), BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME);
                    }
                     // Api.init(LaunchScreenActivity.this,serverUrl, HubbleApplication.AppContext.getUserAgentHeader(),BuildConfig.FLAVOR,BuildConfig.APPLICATION_ID,BuildConfig.VERSION_NAME);
                //    Api.init(LaunchScreenActivity.this,serverUrl, HubbleApplication.AppContext.getUserAgentHeader());


                    Toast.makeText(LaunchScreenActivity.this, "Set server url to: " + serverUrl, Toast.LENGTH_SHORT).show();

                    Config.setApiServerUrl(serverUrl);
                    //Hide the keyboard

                    if(PublicDefines.SERVER_URL.compareToIgnoreCase(PublicDefines.PRODUCTION_URL)==0)
                    {
                        AppSDKConfiguration.getInstance(LaunchScreenActivity.this).setProductionServer(true);
                    }
                    else
                    {
                        AppSDKConfiguration.getInstance(LaunchScreenActivity.this).setProductionServer(false);
                    }

                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                }
                dialog.cancel();
            }
        });

        Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public String getUserAgentHeader() {
        String result = getString(R.string.app_brand_application_name);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
            result = result + "/" + versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
}