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


import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.networkinterface.user.AccountManager;
import com.hubble.framework.networkinterface.v1.pojo.HubbleRequest;
import com.hubble.framework.service.account.AccountManagement;
import com.hubble.framework.service.account.AccountMgrListener;
import com.hubble.framework.service.account.UserLoginInfo;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.user.pojo.response.UserDetails;
import com.hubble.framework.service.security.SecurityService;
import com.hubble.registration.AnalyticsController;
import com.hubble.ui.DebugFragment;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;
import com.nest.common.Settings;
import com.nxcomm.blinkhd.ui.CameraListFragment;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.nxcomm.blinkhd.ui.RSAUtils;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.NetworkDetector;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.hubble.Api;
import base.hubble.PublicDefineGlob;
import base.hubble.constants.Login;

//import com.hubble.framework.service.account.AccountManagement;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, AccountMgrListener {
    private Button btn_loginbtn;
    private TextView tv_forward;
    private EditText et_loginname, et_password;
    private AccountManagement accountManagement;
    private ProgressDialog mProgressDialog;
    private SecureConfig settings = HubbleApplication.AppConfig;
    private TextView tv_nameerror, tv_passworderror, tv_showpassword, tv_signup, mForgetpassword;
    private boolean isPasswordClicked;
    String email;
    NetworkDetector networkDetector;
    private final String LOGIN_FAIL = "401";
    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d$@$!%*#?&]{8,30}$";
    private static final String ENCRYPT_API_KEY = "encrypt_api_key";
    private static final byte[] encryptKey = {48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, -127, 0, -90, 88, -102, 19, 52, 32, -75, 17, -102, 12, 86, 1, -111, 15, -18, 107, -81, -85, -19, 13, -120, 77, -88, 18, -110, 88, -17, 121, -108, 34, -81, 59, 18, 106, 14, -124, 113, -113, 90, -38, 106, 23, 1, 110, -79, -93, -80, -127, -62, -16, 22, 98, 71, 33, -124, 65, 91, 77, 5, 39, 82, -37, -89, 86, 40, -102, 92, 98, 122, -120, -69, 55, -29, -2, 9, -93, -13, 23, 68, -12, 43, 22, -57, -29, -34, -126, 77, -78, 39, -114, -77, 12, 63, 93, 34, 103, -16, 117, -68, 26, 62, 86, -31, 85, 78, 36, -18, 103, 3, -44, 23, -109, -73, 29, -62, -112, -32, -116, 36, -20, 83, -39, 83, -56, 70, -10, 38, -75, 2, 3, 1, 0, 1};
    public static final String BROADCAST_ACTION = "com.blinkhd.AUTH_TOKEN";
    private static final String USER_UNIQUE_REGISTRATION_ID = "unique_registration_id";

    private static final String TAG = "LoginActivity";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private long loginStartTime;
    private long loginTime;
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        et_loginname = (EditText) findViewById(R.id.et_loginname);
        et_password = (EditText) findViewById(R.id.et_login_password);
        btn_loginbtn = (Button) findViewById(R.id.btn_login_btn);
        tv_forward = (TextView) findViewById(R.id.tv_forgetpassword);
        tv_nameerror = (TextView) findViewById(R.id.tv_email_error);
        tv_passworderror = (TextView) findViewById(R.id.tv_password_error);
        tv_showpassword = (TextView) findViewById(R.id.tv_showpassword);
        tv_signup = (TextView) findViewById(R.id.tv_signup);
        mForgetpassword = (TextView) findViewById(R.id.tv_forgetpassword);
        mForgetpassword.setOnClickListener(this);
        tv_signup.setOnClickListener(this);
        tv_showpassword.setOnClickListener(this);
        tv_forward.setOnClickListener(this);
        btn_loginbtn.setOnClickListener(this);
        accountManagement = new AccountManagement();
        sharedPreferences = getSharedPreferences(CommonConstants.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        decimalFormat = new DecimalFormat("##.0");

        isPasswordClicked = false;
        networkDetector = new NetworkDetector(getApplicationContext());

        et_loginname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tv_nameerror.getVisibility() == View.VISIBLE)
                    tv_nameerror.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tv_passworderror.getVisibility() == View.VISIBLE)
                    tv_passworderror.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.tv_forgetpassword:
                Intent forgetIntent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
                startActivity(forgetIntent);
                finish();
                break;

            case R.id.btn_login_btn:
                Boolean isInternetPresent = networkDetector.isConnectingToInternet();
                if (isInternetPresent) {
                    if (validate()) {
                        loginStartTime = System.currentTimeMillis();
                        UserLoginInfo userLoginInfo = new UserLoginInfo();
                        userLoginInfo.setUserName(et_loginname.getText().toString());
                        userLoginInfo.setPassword(et_password.getText().toString());
                        email = et_loginname.getText().toString();
                        String ourUserEmail = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, "");
                        String ourUserName = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, "");
                        if (!TextUtils.isEmpty(ourUserEmail) && (!TextUtils.isEmpty(ourUserName))) {
                            if (!email.equalsIgnoreCase(ourUserEmail) && !email.equalsIgnoreCase(ourUserName)) {
                                settings.clear();
                                editor.clear();
                                editor.commit();
                                CommonUtil.clearSettingSharedPref(getApplicationContext());
                            }
                        }
                        editor.putLong(CommonConstants.APP_REGISTRATION_TIME, loginStartTime);
                        editor.commit();
                        accountManagement.userlogin(this, userLoginInfo);
                        displayProgressDialog();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.enable_internet_connection, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_signup:
                Intent signupIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(signupIntent);
                finish();
                break;
            case R.id.tv_showpassword:

                if (et_password.getText().toString().length() > 0) {
                    if (!isPasswordClicked) {
                        et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        isPasswordClicked = true;
                        tv_showpassword.setText(R.string.hide);
                    } else {
                        et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        isPasswordClicked = false;
                        tv_showpassword.setText(R.string.show);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.please_enter_password, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public void onUserLoginStatus(accountMgmtEvent accEvent, String response) {
        dismissDialog();
        loginTime = (System.currentTimeMillis() - loginStartTime);
        int time =(int) loginTime / 1000;
        String appLoginTime;
        Log.d("LoginTime","LoginTime : "+loginTime + " Sec = "+time);
        if(time<=1){
            appLoginTime = " 1 sec";
        }else if(time>1 && time<=3){
            appLoginTime = " 3 sec";
        }else if(time>3 && time<=5){
            appLoginTime = " 5 sec";
        }else if(time>5 && time<=10){
            appLoginTime = " 10 sec";
        }else{
            appLoginTime = ">10 sec";
        }
        // Log.d("Response", "Response =" + response);
        if (accEvent == accountMgmtEvent.USER_LOGIN_SUCESS) {

            Log.d("LoginTime","appLoginTime : "+appLoginTime);

            AnalyticsInterface.getInstance().setUserLoginStatus(AnalyticsInterface.LoginStatus.LOGIN_STATUS_SUCCESS);
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN, AppEvents.LOGIN_SUCCESS, AppEvents.LOGIN_SUCCESS);
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.LOGIN_TIME+" : "+ appLoginTime,AppEvents.LOGIN_TIME);

            try {
               // ZaiusEventManager.getInstance().trackEvent(HubbleZaiusEvent.EVENT_TYPE.ZE_LOGIN, HubbleZaiusEvent.ACTION.ZA_LOGIN_SUCCESS);

                ZaiusEvent loginEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
                loginEvt.action(AppEvents.LOGIN_SUCCESS);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(loginEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }


                ZaiusEvent loginTimeEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
                loginTimeEvt.action(AppEvents.LOGIN_TIME+" : "+ appLoginTime);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(loginTimeEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }

                JSONObject jsonObject = new JSONObject(response.toString());

                String sessionToken = jsonObject.getString("auth-token");
                String registrationToken = jsonObject.getString(USER_UNIQUE_REGISTRATION_ID);


                HubbleRequest hubbleRequest = new HubbleRequest();
                hubbleRequest.setAuthToken(sessionToken);
                if(!TextUtils.isEmpty(sessionToken) && !sessionToken.equalsIgnoreCase("null")) {
                    AccountManager.getInstance(this).getUserRequest(hubbleRequest, new Response.Listener<UserDetails>() {
                        @Override
                        public void onResponse(UserDetails response) {
                            settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, response.getEmail());
                            settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, response.getName());
                            try {
                                ZaiusEventManager.getInstance().setCustomerID(response.getRecurlyId());
                            } catch (ZaiusException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "volley getUserRequest error =" + error.toString());
                        }
                    });
                }

                if (registrationToken != null && registrationToken.length() > 6) {
                    settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, email);
                    settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_PWD, et_password.getText().toString());
                    settings.putString(PublicDefineGlob.PREFS_SAVED_REGISTRATION_ID, registrationToken);

                    Intent intent = new Intent(this, EmailConfirmationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                    finish();
                } else {
                    Intent intent = new Intent();
                    try {
                        intent.putExtra(ENCRYPT_API_KEY, RSAUtils.encryptRSA(encryptKey, sessionToken.getBytes()));
                    } catch (Exception e) {
                    }
                    intent.setAction(BROADCAST_ACTION);
                    this.sendBroadcast(intent);

                    try {
                        //TODO Aruna commented relook
                        // dismissDialog(DIALOG_CONTACTING_BMS_SERVER);
                    } catch (Exception e) {
                    }
                    //AA-1572
                    AnalyticsController.getInstance().setUserEmail(email);

                    Intent entry = new Intent(LoginActivity.this, SplashScreen.class);
                    entry.putExtra(Login.bool_isLoggedIn, true);
                    entry.putExtra(Login.STR_USER_TOKEN, sessionToken);

                    String savedToken = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
                    String savedUser = settings.getString(PublicDefineGlob.PREFS_TEMP_PORTAL_ID, "");
                    int tempUnit = settings.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_F);

                    boolean soundNotify = settings.getBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_SOUND, true);
                    boolean vibrationNotify = settings.getBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_VIBRATE, true);
                    boolean notifyOnCall = settings.getBoolean(PublicDefineGlob.PREFS_NOTIFY_ON_CALL, true);
                    boolean remoteTimeout = settings.getBoolean("should_video_view_timeout", true);
                    boolean donotDisturb = settings.getBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, false);
                    long remainingDoNotDisturb = settings.getLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, 0);
                    long donotDisturbTime = settings.getLong(PublicDefineGlob.PREFS_TIME_DO_NOT_DISTURB_EXPIRED, 0);

                    boolean pullRefreshEvent = settings.getBoolean(MainActivity.PREF_SHOWCASE_PULL_REFRESH, false);
                    boolean swipeDelete = settings.getBoolean(MainActivity.PREF_SHOWCASE_SWIPE_DELETE, false);
                    boolean cameraView = settings.getBoolean(CameraListFragment.PREF_SHOWCASE_CAMERA_VIEW, false);
                    boolean pullRefreshCameraList = settings.getBoolean(CameraListFragment.PREF_SHOWCASE_PULL_REFRESH, false);
                    boolean cameraDetails = settings.getBoolean(CameraListFragment.PREF_SHOWCASE_CAMERA_DETAILS, false);
                    boolean isP2pEnabled = settings.getBoolean(PublicDefineGlob.PREFS_IS_P2P_ENABLED, true);
                    long lastTry = settings.getLong(PublicDefineGlob.PREFS_LAST_P2P_TRY, -1);
                    boolean isDebugEnabled = settings.getBoolean(DebugFragment.PREFS_DEBUG_ENABLED, false);
                    boolean useDevOta = settings.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false);
                    boolean dontAskMeAgain = settings.getBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, false);
                    int timeFormat = settings.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0);
                    String serverURL = settings.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, null);


                    if (!savedUser.equalsIgnoreCase(email) && !savedUser.equalsIgnoreCase(email)) {
                        Log.i(TAG, "Saved user: " + savedUser + ", username: " + email + " --> clear");
                        Settings.saveAuthToken(LoginActivity.this, null);
                        settings.clear();
                        settings.putBoolean(SecureConfig.HAS_USED_SECURE_SHARE_PREFS, true);
                        newUserAttemptingLogIn();

                        if (serverURL != null) {
                            settings.putString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, serverURL);
                        }
                    }

                    settings.putString(PublicDefineGlob.PREFS_TEMP_PORTAL_ID, savedUser);
                    settings.putBoolean(MainActivity.PREF_SHOWCASE_PULL_REFRESH, pullRefreshEvent);
                    settings.putBoolean(MainActivity.PREF_SHOWCASE_SWIPE_DELETE, swipeDelete);
                    settings.putBoolean(CameraListFragment.PREF_SHOWCASE_CAMERA_VIEW, cameraView);
                    settings.putBoolean(CameraListFragment.PREF_SHOWCASE_PULL_REFRESH, pullRefreshCameraList);
                    settings.putBoolean(CameraListFragment.PREF_SHOWCASE_CAMERA_DETAILS, cameraDetails);

                    settings.putBoolean(PublicDefineGlob.PREFS_IS_P2P_ENABLED, isP2pEnabled);
                    settings.putLong(PublicDefineGlob.PREFS_LAST_P2P_TRY, lastTry);
                    settings.putBoolean(DebugFragment.PREFS_DEBUG_ENABLED, isDebugEnabled);
                    settings.putBoolean(DebugFragment.PREFS_USE_DEV_OTA, useDevOta);

                    settings.putBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, dontAskMeAgain);
                    settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, tempUnit);
                    settings.putBoolean(PublicDefineGlob.PREFS_IS_FIRST_TIME, false);
                    settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, sessionToken);
                    settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, email);
                    settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, email);
                    settings.putBoolean(PublicDefineGlob.PREFS_USER_ACCESS_INFRA_OFFLINE, false);

                    settings.putBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_SOUND, soundNotify);
                    settings.putBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_VIBRATE, vibrationNotify);
                    settings.putBoolean("should_video_view_timeout", remoteTimeout);
                    settings.putBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, donotDisturb);
                    settings.putLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, remainingDoNotDisturb);
                    settings.putLong(PublicDefineGlob.PREFS_TIME_DO_NOT_DISTURB_EXPIRED, donotDisturbTime);
                    settings.putBoolean(PublicDefineGlob.PREFS_NOTIFY_ON_CALL, notifyOnCall);
                    settings.putInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, timeFormat);

                    entry.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    SecurityService securityService = new SecurityService();
                    if (securityService.isPhoneRooted()) {
                        Toast.makeText(LoginActivity.this, R.string.phone_is_rooted, Toast.LENGTH_SHORT).show();
                    }
                    LoginActivity.this.startActivity(entry);
                    settings.remove(PublicDefineGlob.PREFS_TEMP_PORTAL_PWD);

                    finish();

                    Toast.makeText(getApplicationContext(), R.string.login_successful, Toast.LENGTH_SHORT).show();
                    //   checked_for_connectivity = false;
                }
            } catch (Exception e) {

                Log.d("Exception", "Authtoken error=" + e);
            }

            Toast.makeText(getApplicationContext(), R.string.login_successful, Toast.LENGTH_SHORT).show();
        } else if (accEvent == accountMgmtEvent.USER_LOGIN_FORBIDDEN) {

            Toast.makeText(getApplicationContext(), R.string.account_blocked, Toast.LENGTH_SHORT).show();

        } else if (accEvent == accountMgmtEvent.USER_LOGIN_FAILURE) {
            AnalyticsInterface.getInstance().setUserLoginStatus(AnalyticsInterface.LoginStatus.LOGIN_STATUS_FAILURE);

            try {
                JSONObject jObject = new JSONObject(response);
                String status = jObject.getString("status");
                String message = jObject.getString("message");
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN, AppEvents.FAILURE+" : " + status, AppEvents.LOGIN_FAILURE);
                //ZaiusEventManager.getInstance().trackEvent(HubbleZaiusEvent.EVENT_TYPE.ZE_LOGIN, HubbleZaiusEvent.ACTION.ZA_LOGIN_FAILURE);
                ZaiusEvent loginFailureEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
                loginFailureEvt.action(AppEvents.FAILURE+" : " + status);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(loginFailureEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }



                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.FAILURE+" : "+ appLoginTime,AppEvents.LOGIN_TIME);
                ZaiusEvent loginTimeEvt = new ZaiusEvent(AppEvents.LOGIN_TIME);
                loginTimeEvt.action(AppEvents.FAILURE+" : "+ appLoginTime);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(loginTimeEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }

                if (status.equals(LOGIN_FAIL) && !TextUtils.isEmpty(message)) {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.login_fail_please_try_again, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), R.string.login_fail_please_try_again, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.login_fail_please_try_again, Toast.LENGTH_SHORT).show();
        }
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
        mProgressDialog.setMessage(getResources().getString(R.string.login_progress));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
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
    public void onBackPressed() {
        super.onBackPressed();
      //  overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        Intent homeIntent = new Intent(LoginActivity.this, LaunchScreenActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        finish();

    }


    private boolean validateEmail() {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(et_loginname.getText().toString()).matches()) {
            tv_nameerror.setVisibility(View.VISIBLE);
        } else {
            tv_nameerror.setVisibility(View.INVISIBLE);
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(et_loginname.getText().toString()).matches();
    }

    private boolean validateName() {
        if (et_loginname.getText().toString().trim().length() < 3
                || et_loginname.getText().toString().trim().length() > 25) {
            tv_nameerror.setVisibility(View.VISIBLE);
            tv_nameerror.setText(R.string.enter_valid_name);
            return false;
        } else {
            tv_nameerror.setVisibility(View.INVISIBLE);
        }

        return true;
    }

    private boolean validateLoginName() {
        boolean isvalid = false;
        /* Enforce email id for login*/
        if (et_loginname.getText().toString().trim().contains(".com")) {
            isvalid = validateEmail();
        } else {
            isvalid = validateName();
        }
        return isvalid;
    }


    private boolean validatePassword() {

        Pattern p = Pattern.compile(PASSWORD_PATTERN);
        Matcher m = p.matcher(et_password.getText().toString().trim());
        boolean validpassword = m.find();
        if (!validpassword) {
            tv_passworderror.setVisibility(View.VISIBLE);
        } else {
            tv_passworderror.setVisibility(View.INVISIBLE);
        }

        return validpassword;
    }


    private boolean validate() {
        if (et_loginname.getText().toString().trim().length() == 0 || et_password.getText().toString().trim().length() == 0) {
            Toast.makeText(getBaseContext(), R.string.please_enter_all_fields, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!validateLoginName()) {
            Toast.makeText(getApplicationContext(), R.string.email_error_message, Toast.LENGTH_SHORT).show();
            return false;
        }
        /* Remove password validation in the login*/
        /*else if (!validatePassword()) {
            Toast.makeText(getApplicationContext(), R.string.paswd_error_message, Toast.LENGTH_SHORT).show();
            return false;
        }*/
        return true;

    }

    public void newUserAttemptingLogIn() {
        Api.getInstance().deleteDatabase();
        DeviceSingleton.getInstance().clearDevices();

        SecureConfig settings = HubbleApplication.AppConfig;
        boolean offlineMode = settings.getBoolean(PublicDefineGlob.PREFS_USER_ACCESS_INFRA_OFFLINE, false);
        if (!offlineMode) {
            // vox service should not take wakelock
            settings.putBoolean(PublicDefineGlob.PREFS_VOX_SHOULD_TAKE_WAKELOCK, false);
            // remove password when user logout
            settings.remove(PublicDefineGlob.PREFS_TEMP_PORTAL_PWD);

            // Remove all pending notification on Status bar
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }
}
