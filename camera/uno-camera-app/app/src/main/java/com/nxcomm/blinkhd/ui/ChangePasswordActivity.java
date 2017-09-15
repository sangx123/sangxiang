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

package com.nxcomm.blinkhd.ui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.hubbleconnected.camera.R;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.dialog.HubbleDialogFactory;
import com.hubble.framework.service.account.AccountManagement;
import com.hubble.framework.service.account.AccountMgrListener;
import com.hubble.framework.service.account.UserPasswordInfo;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.notification.FirebaseManager;
import com.hubble.framework.service.notification.GCMManager;
import com.hubble.framework.service.p2p.P2pUtils;
import com.hubble.helpers.AsyncPackage;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.SetupDataCache;
import com.hubble.util.CommonConstants;
import com.hubble.util.P2pSettingUtils;
import com.msc3.registration.LaunchScreenActivity;
import com.util.AppEvents;
import com.util.NetworkDetector;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.hubble.Api;
import base.hubble.PublicDefineGlob;
import de.greenrobot.event.EventBus;


public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener, AccountMgrListener {
    private Button mResetpassword;
    private EditText mCurrentPassword,mNewPassword,mConfirmPassword;
    private AccountManagement accountManagement;
    private ProgressDialog mProgressDialog;
   // private SharedPreferenceManager preManger;
    private TextView mCurrentPasswordError,mNewPasswordError, mConfirmPasswordError;
    private TextView mShowCurrentPassword,mShowNewPassword,mShowConfirmPassword;
    private boolean isCurrentPasswordClicked;
    private boolean isNewPasswordClicked;
    private boolean isConfirmtPasswordClicked;
    private final String CONFIRM_PASSWORD_FAIL = "401";
    private SecureConfig settings = HubbleApplication.AppConfig;


    String email;
    private static final String TAG = ChangePasswordActivity.class.getSimpleName();
    NetworkDetector networkDetector;
    private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,255})";
    private String appUserName, appUserMailId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_changepassword);
       // overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        mCurrentPassword = (EditText) findViewById(R.id.et_currentpassword);
        mNewPassword= (EditText) findViewById(R.id.et_newpassword);
        mConfirmPassword= (EditText) findViewById(R.id.et_confirmpassword);
        mResetpassword = (Button) findViewById(R.id.btn_resetpassword);

        mCurrentPasswordError = (TextView) findViewById(R.id.tv_currentpassworderror);
        mNewPasswordError = (TextView) findViewById(R.id.tv_newpassworderror);
        mConfirmPasswordError = (TextView) findViewById(R.id.tv_confirmpassworderror);

        mShowCurrentPassword = (TextView)findViewById(R.id.tv_showcurrentpassword);
        mShowNewPassword = (TextView) findViewById(R.id.tv_shownewpassword);
        mShowConfirmPassword = (TextView) findViewById(R.id.tv_showconfirmpassword);

        mShowCurrentPassword.setOnClickListener(this);
        mShowNewPassword.setOnClickListener(this);
        mShowConfirmPassword.setOnClickListener(this);
        networkDetector = new NetworkDetector(getApplicationContext());


        mShowCurrentPassword.setOnClickListener(this);
        mResetpassword.setOnClickListener(this);
        accountManagement = new AccountManagement();
        //SharedPreferenceManager.initializeInstance(this);
        isCurrentPasswordClicked = false;
        isConfirmtPasswordClicked= false;
        isNewPasswordClicked = false;
        String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
        appUserMailId = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, "");
        appUserName = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, null);
        mCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mCurrentPasswordError.getVisibility() == View.VISIBLE)
                mCurrentPasswordError.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });



        mNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mNewPasswordError.getVisibility() == View.VISIBLE)
                    mNewPasswordError.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });




        mConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mConfirmPasswordError.getVisibility() == View.VISIBLE)
                    mConfirmPasswordError.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_resetpassword:
                if(!validate()){
                    return ;
                }
                if(mCurrentPassword.getText().toString().equals(mNewPassword.getText().toString())){
                    Toast.makeText(getBaseContext(),R.string.old_password_new_password_should_not_same,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(appUserName.equalsIgnoreCase(mNewPassword.getText().toString()) || appUserMailId.equalsIgnoreCase(mNewPassword.getText().toString())){
                   Toast.makeText(getBaseContext(),R.string.same_user_name_and_password,Toast.LENGTH_SHORT).show();
                   return;
                }

                Boolean isInternetPresent = networkDetector.isConnectingToInternet();
                if(isInternetPresent) {
                    UserPasswordInfo userPasswordInfo = new UserPasswordInfo();
                    userPasswordInfo.setCurrentPassword(mCurrentPassword.getText().toString());
                    userPasswordInfo.setNewPassword(mNewPassword.getText().toString());
                    userPasswordInfo.setConfirmpassword(mConfirmPassword.getText().toString());
                    String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                    userPasswordInfo.setAccesstoken(apiKey);
                    accountManagement.resetUserPassword(userPasswordInfo);
                    displayProgressDialog();
                } else{
                    Toast.makeText(getApplicationContext(),R.string.enable_internet_connection,Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tv_showcurrentpassword:
                if(mCurrentPassword.getText().toString().length()>0) {
                    if (!isCurrentPasswordClicked) {
                        mCurrentPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        isCurrentPasswordClicked = true;
                        mShowCurrentPassword.setText(R.string.hide);
                    } else {
                        mCurrentPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        isCurrentPasswordClicked = false;
                        mShowCurrentPassword.setText(R.string.show);
                    }
                }else{
                    Toast.makeText(getApplicationContext(),R.string.please_enter_current_password,Toast.LENGTH_SHORT).show();
                }

                break;


            case R.id.tv_shownewpassword:
                if(mNewPassword.getText().toString().length()>0) {
                    if (!isNewPasswordClicked) {
                        mNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        isNewPasswordClicked = true;
                        mShowNewPassword.setText(R.string.hide);
                    } else {
                        mNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        isNewPasswordClicked = false;
                        mShowNewPassword.setText(R.string.show);
                    }

                }else {
                    Toast.makeText(getApplicationContext(),R.string.please_enter_new_password, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tv_showconfirmpassword:
                if(mConfirmPassword.getText().toString().length()>0) {
                    if (!isConfirmtPasswordClicked) {
                        mConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        isConfirmtPasswordClicked = true;
                        mShowConfirmPassword.setText(R.string.hide);
                    } else {
                        mConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        isConfirmtPasswordClicked = false;
                        mShowConfirmPassword.setText(R.string.show);
                    }
                }else {
                    Toast.makeText(getApplicationContext(),R.string.please_enter_confirm_password,Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }


    @Override
    public void onErrorResponse(accountMgmtEvent accountMgmtEvent, VolleyError error) {
        dismissDialog();

        Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed), Toast.LENGTH_LONG).show();

        if (error != null && error.networkResponse != null) {
            Log.d("Account Mgmt Error ", error.networkResponse.toString());
            Log.d("Account Mgmt Error", error.networkResponse.data.toString());
        }


    }

    @Override
    public void onUserLoginStatus(accountMgmtEvent accEvent, String response) {
        //Log.d("Response", "Response in ChangePassword Activity=" + response);
        dismissDialog();
        String status = null;
        String message = null;


        if (accEvent == accountMgmtEvent.USER_CHANGEPASSWORD_SUCCESS) {
            Toast.makeText(getApplicationContext(), R.string.password_changed_successfully, Toast.LENGTH_SHORT).show();

            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.ACCOUNT_MANAGEMENT,AppEvents.DM_PASSWORD_CHANGED+" : "+AppEvents.SUCCESS,AppEvents.CHANGE_PASSWORD);
            ZaiusEvent passwordSuccessEvt = new ZaiusEvent(AppEvents.ACCOUNT_MANAGEMENT);
            passwordSuccessEvt.action(AppEvents.DM_PASSWORD_CHANGED+" : "+AppEvents.SUCCESS);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(passwordSuccessEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.log_out);
            builder.setMessage(R.string.you_have_been_log_out);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EventBus.getDefault().removeAllStickyEvents();
                   // isUnauthorizedDialogShow = false;
                    onUserLogout();
                }
            });
          //  isUnauthorizedDialogShow = true;
            builder.create().show();
            settings.putBoolean(PublicDefine.IS_APP_CHANGE_PWD, true);
           // finish();

        } else if(accEvent == accountMgmtEvent.USER_CHANGEPASSSWORD_FAILURE){
            try {
                JSONObject jObject = new JSONObject(response);
                status = jObject.getString("status");
                message = jObject.getString("message");
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), R.string.password_not_changed_please_try_again , Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.ACCOUNT_MANAGEMENT,AppEvents.DM_PASSWORD_CHANGED+" "+AppEvents.FAILURE+" : "+status,AppEvents.CHANGE_PASSWORD);
            ZaiusEvent passwordFailureEvt = new ZaiusEvent(AppEvents.ACCOUNT_MANAGEMENT);
            passwordFailureEvt.action(AppEvents.DM_PASSWORD_CHANGED+" "+AppEvents.FAILURE+" : "+status);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(passwordFailureEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }
            if (status != null && status.equals(CONFIRM_PASSWORD_FAIL) && !TextUtils.isEmpty(message)) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(message)){
                Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), R.string.password_not_changed_please_try_again , Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(getApplicationContext(), R.string.password_not_changed_please_try_again , Toast.LENGTH_SHORT).show();
        }
     }

    public void onUserLogout() {

        final Dialog unregisteringDialog = HubbleDialogFactory.createProgressDialog(ChangePasswordActivity.this, getString(R.string.logging_out), false, false);

        try
        {
            unregisteringDialog.show();
        }
        catch (Exception e)
        {
        }

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

                        DeviceSingleton.getInstance().clearDevices();

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

                            Intent new_login = new Intent(ChangePasswordActivity.this, LaunchScreenActivity.class);
                            new_login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            startActivity(new_login);
                            finishAffinity();

                        }

                        try {

                            unregisteringDialog.dismiss();
                        }
                        catch (Exception e)
                        {
                        }

                        if (P2pSettingUtils.hasP2pFeature()) {
                            // When user logged out, stop p2p service if it's running
                            P2pUtils.stopP2pService(ChangePasswordActivity.this);
                        }

                        setResult(CommonConstants.SHOULD_EXIT_NOW_YES);
                        settings.putBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, false);
                        settings.putBoolean(PublicDefineGlob.PREFS_SHOULD_TURN_ON_WIFI, false);
                        settings.remove(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT);
                        finish();
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
            FirebaseManager.getInstance(this).stopNotificationService();
        }

        Log.d(TAG, "Finish unregister notification, app id: " + appId);
    }
    @Override
    protected void onResume() {
        super.onResume();
        accountManagement.registerCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        accountManagement.unRegisterCallback();
    }

    private void displayProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.resetting_password));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
     //   overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        Intent dashBoardIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(dashBoardIntent);
        finish();
    }


   private boolean validateCurrentPassword() {

       Pattern p = Pattern.compile(PASSWORD_PATTERN);
       Matcher m = p.matcher(mCurrentPassword.getText().toString().trim());
       boolean validpassword = m.find();
       if (!validpassword) {
           mCurrentPasswordError.setVisibility(View.VISIBLE);
       } else {
           mCurrentPasswordError.setVisibility(View.INVISIBLE);
       }

       return validpassword;
   }

    private boolean validateNewPassword() {

        Pattern p = Pattern.compile(PASSWORD_PATTERN);
        Matcher m = p.matcher(mNewPassword.getText().toString().trim());
        boolean validpassword = m.find();
        if (!validpassword) {
            mNewPasswordError.setVisibility(View.VISIBLE);
        } else {
            mNewPasswordError.setVisibility(View.INVISIBLE);
        }

        return validpassword;
    }

    private boolean validateConfirmPassword() {
        if (!mConfirmPassword.getText().toString().trim().equals(mNewPassword.getText().toString().trim())) {
            mConfirmPasswordError.setVisibility(View.VISIBLE);
            return false;
        } else {
            mConfirmPasswordError.setVisibility(View.INVISIBLE);
        }
        return true;
    }

    private boolean validate() {
        if(mCurrentPassword.getText().toString().trim().length()==0 || mNewPassword.getText().toString().trim().length()==0
                || mConfirmPassword.getText().toString().trim().length()== 0){
            Toast.makeText(getBaseContext(),R.string.please_enter_all_fields,Toast.LENGTH_SHORT).show();
            return false;
        }


        if (!validateCurrentPassword()) {
            Toast.makeText(getApplicationContext(),R.string.currentpwd_error_message,Toast.LENGTH_SHORT).show();
            return false;
        } else if (!validateNewPassword()) {
            Toast.makeText(getApplicationContext(),R.string.newpwd_error_message,Toast.LENGTH_SHORT).show();
            return false;
        } else if (!validateConfirmPassword()) {
            Toast.makeText(getApplicationContext(),R.string.passwd_match_new_conf,Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }
}
