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
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.account.AccountManagement;
import com.hubble.framework.service.account.AccountMgrListener;
import com.hubble.framework.service.account.UserRegInfo;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.configuration.AppSDKConfiguration;
import com.hubble.framework.service.database.DeviceDatabaseHelper;
import com.hubble.framework.service.device.model.Profile;
import com.hubble.framework.service.security.SecurityService;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;
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


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, AccountMgrListener
{
    private Button btn_createaccount;
    private EditText et_name, et_email, et_password, et_confirmpassword;
    private TextView tv_reg_nameerr, tv_reg_passworderr, tv_reg_confirmpassworderr, tv_reg_emailerror;
    JSONObject json;
    private TextView tv_showpassword,tv_showconfirmpassword;
    private TextView tv_signin,mtermsandconditions;
    private ImageView validUserName, validemail, validPassword, validConfirmPassword;
    private CheckBox termsAccepted;
    Context context;
    AccountManagement accountManagement;
    private ProgressDialog mProgressDialog;
    private ImageView mLoadingAnimationView;
    //    SharedPreferences sharedpreferences;
    private boolean isPasswordClicked,isConfirmPasswordClicked;
    private DeviceDatabaseHelper deviceDatabaseHelper;
    NetworkDetector networkDetector;
    private View progressbarView;
    private Animation animation;

    private final String REGISTRATION_FAIL= "422";

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d$@$!%*#?&]{8,30}$";
          // "(?=.*\\d)(?=.*[A-Z])[a-zA-Z\\\\d]{8,30}";
    private static final String USERNAME_PATTERN = "^[0-9a-zA-Z\\.\\-\\_]*$";

    public static final String ENCRYPT_API_KEY = "encrypt_api_key";
    public static final String BROADCAST_ACTION = "com.blinkhd.AUTH_TOKEN";

    public static final byte[] encryptKey = {48, -127, -97, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, -127, 0, -90, 88, -102, 19, 52, 32, -75, 17, -102, 12, 86, 1, -111, 15, -18, 107, -81, -85, -19, 13, -120, 77, -88, 18, -110, 88, -17, 121, -108, 34, -81, 59, 18, 106, 14, -124, 113, -113, 90, -38, 106, 23, 1, 110, -79, -93, -80, -127, -62, -16, 22, 98, 71, 33, -124, 65, 91, 77, 5, 39, 82, -37, -89, 86, 40, -102, 92, 98, 122, -120, -69, 55, -29, -2, 9, -93, -13, 23, 68, -12, 43, 22, -57, -29, -34, -126, 77, -78, 39, -114, -77, 12, 63, 93, 34, 103, -16, 117, -68, 26, 62, 86, -31, 85, 78, 36, -18, 103, 3, -44, 23, -109, -73, 29, -62, -112, -32, -116, 36, -20, 83, -39, 83, -56, 70, -10, 38, -75, 2, 3, 1, 0, 1};
    private SecureConfig settings = HubbleApplication.AppConfig;

    private SharedPreferences sharedPreferences;
    private  SharedPreferences.Editor editor;
    private long regStartTime;
    private long regTime;
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signup);

        progressbarView = findViewById(R.id.progrssbarview);
        mLoadingAnimationView = (ImageView) findViewById(R.id.anim_image);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.processing_animation);
        progressbarView.setVisibility(View.INVISIBLE);

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        context = getApplicationContext();
        et_email = (EditText) findViewById(R.id.et_singup_email);
        et_name = (EditText) findViewById(R.id.et_signup_name);
        et_password = (EditText) findViewById(R.id.et_signup_password);
        et_confirmpassword = (EditText) findViewById(R.id.et_signup_confirmpassword);
        btn_createaccount = (Button) findViewById(R.id.btn_createaccount);
        tv_reg_confirmpassworderr = (TextView) findViewById(R.id.tv_signup_confirm_passworderror);
        tv_reg_nameerr = (TextView) findViewById(R.id.tv_signup_name_error);
        tv_reg_emailerror = (TextView) findViewById(R.id.tv_signup_email_error);
        tv_reg_passworderr = (TextView) findViewById(R.id.tv_signup_password_error);
        tv_showpassword = (TextView) findViewById(R.id.tv_signup_showpassword);
        tv_showconfirmpassword = (TextView) findViewById(R.id.tv_signup_showconfpassword);
        tv_signin = (TextView) findViewById(R.id.tv_reg_singin);
        mtermsandconditions = (TextView) findViewById(R.id.termsandconditions);
        mtermsandconditions.setOnClickListener(this);
        tv_showconfirmpassword.setOnClickListener(this);
        termsAccepted = (CheckBox)findViewById(R.id.termscheckbox);
        termsAccepted.setOnClickListener(this);
        tv_signin.setOnClickListener(this);
        tv_showpassword.setOnClickListener(this);
        validUserName = (ImageView)findViewById(R.id.validusername);
        validemail = (ImageView)findViewById(R.id.validemail);
        validPassword = (ImageView)findViewById(R.id.validpassword);
        validConfirmPassword = (ImageView)findViewById(R.id.validconfirmpassword);

        sharedPreferences = getSharedPreferences(CommonConstants.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        decimalFormat = new DecimalFormat("##.0");
        btn_createaccount.setOnClickListener(this);
        accountManagement = new AccountManagement();
        deviceDatabaseHelper = new DeviceDatabaseHelper();
        // sharedpreferences = getSharedPreferences("mypreference", Context.MODE_PRIVATE);
        isPasswordClicked = false;
        isConfirmPasswordClicked = false;
        networkDetector = new NetworkDetector(getApplicationContext());


        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               if(tv_reg_nameerr.getVisibility() == View.VISIBLE)
                   tv_reg_nameerr.setVisibility(View.INVISIBLE);

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateName();

            }
        });

        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(tv_reg_emailerror.getVisibility() == View.VISIBLE)
                tv_reg_emailerror.setVisibility(View.INVISIBLE);

            }

            @Override
            public void afterTextChanged(Editable s) {

                validateEmail();
            }
        });


        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validatePassword();
            }
        });

        et_confirmpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(tv_reg_confirmpassworderr.getVisibility() == View.VISIBLE)
                    tv_reg_confirmpassworderr.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateConfirmPassword();
            }
        });
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_forgetpassword:
                break;
            case R.id.btn_createaccount:
                if(!validate()){
                    return ;
                }
                Boolean isInternetPresent = networkDetector.isConnectingToInternet();
                 if (isInternetPresent) {
                     CommonUtil.clearSettingSharedPref(getApplicationContext());
                     regStartTime = System.currentTimeMillis();
                     editor.putLong(CommonConstants.APP_REGISTRATION_TIME,regStartTime);
                     editor.commit();
                UserRegInfo userRegInfo = new UserRegInfo();
                userRegInfo.setUserName(et_name.getText().toString().trim());
                userRegInfo.setEmail(et_email.getText().toString().trim());
                userRegInfo.setPassword(et_password.getText().toString().trim());
                userRegInfo.setConfirmpassword(et_confirmpassword.getText().toString().trim());
                userRegInfo.setPhoneNumber("55555555");
                accountManagement.registerUser(this, userRegInfo);
                displayProgressDialog();
                }else{
                     Toast.makeText(getApplicationContext(),R.string.enable_internet_connection,Toast.LENGTH_SHORT).show();
                 }
                break;

            case R.id.tv_signup_showpassword:

                if(et_password.getText().toString().length()>0) {
                    if (!isPasswordClicked) {
                        et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        isPasswordClicked = true;
                        tv_showpassword.setText(R.string.hide);
                    } else {
                        et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        isPasswordClicked = false;
                        tv_showpassword.setText(R.string.show);
                    }
                }else {
                    Toast.makeText(getApplicationContext(),R.string.please_enter_password,Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_signup_showconfpassword:
                if(et_confirmpassword.getText().toString().length()>0) {
                    if (!isConfirmPasswordClicked) {
                        et_confirmpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        isConfirmPasswordClicked = true;
                        tv_showconfirmpassword.setText(R.string.hide);
                    } else {
                        et_confirmpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        isConfirmPasswordClicked = false;
                        tv_showconfirmpassword.setText(R.string.show);
                    }
                }else {
                    Toast.makeText(getApplicationContext(),R.string.please_enter_confirm_password,Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.tv_reg_singin:
                Intent singinIntent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(singinIntent);
                finish();

                break;
           case R.id.termsandconditions:
               Intent i = new Intent(Intent.ACTION_VIEW);
               i.setData(Uri.parse("https://hubbleconnected.com/hubble-service"));
               startActivity(i);
               break;
            case R.id.termscheckbox:
                if(termsAccepted.isChecked()) {
                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.APP_SIGNUP, AppEvents.TERMS_CONDITIONS_ACCEPTED, AppEvents.TERMS_CONDITIONS_ACCEPTED);
                    ZaiusEvent termsAccepted = new ZaiusEvent(AppEvents.APP_SIGNUP);
                    termsAccepted.action(AppEvents.TERMS_CONDITIONS_ACCEPTED);
                    try {
                        ZaiusEventManager.getInstance().trackCustomEvent(termsAccepted);
                    } catch (ZaiusException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    @Override
    public void onUserLoginStatus(accountMgmtEvent accEvent, String regJson)
    {
        JSONObject jsonObj;
        String regId = null;
        String apikey = null;
        String registrationID = null;
        regTime = (System.currentTimeMillis() - regStartTime);
        int time =(int) regTime / 1000;
        String appRegTime;
        Log.d("LoginTime","LoginTime : "+regTime + " Sec = "+time);
        if(time<=1){
            appRegTime = "1 sec";
        }else if(time>1 && time<=3){
            appRegTime = "3 sec";
        }else if(time>3 && time<=5){
            appRegTime = "5 sec";
        }else if(time>5 && time<=10){
            appRegTime = "10 sec";
        }else{
            appRegTime = ">10 sec";
        }



        if (accEvent.equals(accountMgmtEvent.USER_REGISTERATION_SUCESS))
        {
            AnalyticsInterface.getInstance().setUserSignupStatus(AnalyticsInterface.SignupStatus.SIGNUP_STATUS_SUCCESS);
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.SIGNUP_SUCCESS,AppEvents.SIGNUP_SUCCESS);
            ZaiusEvent signupSuccessEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
            signupSuccessEvt.action(AppEvents.SIGNUP_SUCCESS);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(signupSuccessEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }

            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.SUCCESS+" : "+ appRegTime,AppEvents.REG_TIME);
            ZaiusEvent regTimeEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
            regTimeEvt.action(AppEvents.SUCCESS+" : "+ appRegTime);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(regTimeEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }

            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.TERMS_CONDITIONS_ACCEPTED,AppEvents.TERMS_CONDITIONS_ACCEPTED);
            ZaiusEvent termsAccepted  = new ZaiusEvent(AppEvents.USER_LOGIN);
            termsAccepted.action(AppEvents.TERMS_CONDITIONS_ACCEPTED);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(termsAccepted);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }

            String name = et_name.getText().toString().trim();
            String email = et_email.getText().toString().trim();
            String password = et_password.getText().toString().trim();
            SecurityService securityService = new SecurityService();
            if(securityService.isPhoneRooted()){
                Toast.makeText(RegisterActivity.this, R.string.phone_is_rooted, Toast.LENGTH_SHORT).show();
            }
            try
            {
                jsonObj = new JSONObject(regJson);
                if(jsonObj.has("registration_id"))
                {
                    registrationID = jsonObj.getString("registration_id");

                    settings.putString(PublicDefineGlob.PREFS_SAVED_REGISTRATION_ID, registrationID);
                }
                else
                {
                    regId = jsonObj.getString("id");
                    apikey = jsonObj.getString("auth_token");

                    Profile profile = new Profile();
                    profile.setFname(name);
                    profile.setLname("");
                    if(regId != null)
                    {
                        profile.setProfileId(regId);
                    }
                    else
                    {
                        profile.setProfileId("");
                    }
                    profile.setApiKey(apikey);
                    profile.setEmail(email);
                    profile.setCity("");
                    profile.setCountry("");
                    profile.setDeviceCount(0);
                    profile.setDob("");

                    deviceDatabaseHelper.insertProfileData(profile);
                    Intent intent = new Intent();
                    try {
                        intent.putExtra(ENCRYPT_API_KEY, RSAUtils.encryptRSA(encryptKey, apikey.getBytes()));
                    } catch (Exception e) {
                        // // Log.e(TAG, e.getMessage());
                    }
                    intent.setAction(BROADCAST_ACTION);
                    this.sendBroadcast(intent);
                }

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }


            newUserAttemptingLogIn();

            settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, email);
            settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_PWD, password);
            settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, name);
            settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, apikey);
            settings.putString(PublicDefineGlob.PREFS_TEMP_PORTAL_USR, name);
            settings.putString(PublicDefineGlob.PREFS_TEMP_PORTAL_PWD, password);

            dismissDialog();

            if(AppSDKConfiguration.getInstance(getApplicationContext()).getEmailVerificationStatus()) {
                Intent i = new Intent(this, EmailConfirmationActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }else{
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            finish();

        }
        else if (accEvent == accountMgmtEvent.USER_REGISTRATION_FAILURE)
        {
            dismissDialog();
            AnalyticsInterface.getInstance().setUserSignupStatus(AnalyticsInterface.SignupStatus.SIGNUP_STATUS_FAILURE);

            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.FAILURE+" : "+ appRegTime,AppEvents.REG_TIME);
            ZaiusEvent regTimeEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
            regTimeEvt.action(AppEvents.FAILURE+" : "+ appRegTime);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(regTimeEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }
            try
            {
                JSONObject jObject = new JSONObject(regJson);
                String status = jObject.getString("status");
                String message = jObject.getString("message");
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.SIGNUP_FAILURE+"  StatusCode : "+status,AppEvents.SIGNUP_FAILURE);
                ZaiusEvent signupFailureEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
                signupFailureEvt.action(AppEvents.SIGNUP_FAILURE+"  StatusCode : "+status);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(signupFailureEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
                if (status.equals(REGISTRATION_FAIL) || status.equals("400"))
                {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }

                else
                {
                    Toast.makeText(getApplicationContext(),R.string.register_fail_message, Toast.LENGTH_SHORT).show();
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.register_fail_message, Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), R.string.register_fail_message, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onErrorResponse(accountMgmtEvent accountMgmtEvent, VolleyError error) {

        Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed), Toast.LENGTH_LONG).show();

        if (error != null && error.networkResponse != null) {
            Log.d("Account Mgmt Error ", error.networkResponse.toString());
            Log.d("Account Mgmt Error", error.networkResponse.data.toString());
        }


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


    private void displayProgressDialog() {
//        if (mProgressDialog != null && mProgressDialog.isShowing())
//            mProgressDialog.dismiss();
//
//        mProgressDialog = new ProgressDialog(this);
//        mProgressDialog.setMessage(getResources().getString(R.string.register_progress));
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.show();
//        ;
        if(progressbarView.getVisibility() == View.VISIBLE) {
            mLoadingAnimationView.clearAnimation();
        }
        progressbarView.setVisibility(View.VISIBLE);
        mLoadingAnimationView.startAnimation(animation);
    }

    private void dismissDialog() {
//        if (mProgressDialog != null && mProgressDialog.isShowing())
//            mProgressDialog.dismiss();
        if(progressbarView.getVisibility() == View.VISIBLE)
            mLoadingAnimationView.clearAnimation();
        progressbarView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
       // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        Intent homeIntent = new Intent(RegisterActivity.this,LaunchScreenActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        finish();
    }

    private boolean validateName() {
        if (et_name.getText().toString().trim().length() < 3
                || et_name.getText().toString().trim().length() > 25) {
            tv_reg_nameerr.setVisibility(View.VISIBLE);
            validUserName.setVisibility(View.GONE);
            return false;
        } else {
            if(!checkUserNameFormat()) {
                tv_reg_nameerr.setVisibility(View.VISIBLE);
                validUserName.setVisibility(View.GONE);
            }else{
                tv_reg_nameerr.setVisibility(View.INVISIBLE);
                validUserName.setVisibility(View.VISIBLE);
            }
        }

        return true;
    }

    private boolean checkUserNameFormat(){
        Pattern p = Pattern.compile(USERNAME_PATTERN);
        Matcher m = p.matcher(et_name.getText().toString().trim());
        boolean validusername = m.find();
        return validusername;
    }

    private boolean validatePassword() {
        int tempPassword = et_password.getText().toString().length();
        boolean validpassword = false;
        if (et_password.getText().toString().trim().length() < 8
                || et_password.getText().toString().trim().length() > 30) {
            tv_reg_passworderr.setVisibility(View.VISIBLE);
            tv_reg_passworderr.setTextColor(getResources().getColor(R.color.text_error));
            validPassword.setVisibility(View.GONE);
            return validpassword;
        }else {
            Pattern p = Pattern.compile(PASSWORD_PATTERN);
            Matcher m = p.matcher(et_password.getText().toString().trim());
            validpassword = m.find();
            if (!validpassword ) {
                tv_reg_passworderr.setTextColor(getResources().getColor(R.color.text_error));
                validPassword.setVisibility(View.GONE);
            } else {

                validPassword.setVisibility(View.VISIBLE);
                tv_reg_passworderr.setTextColor(getResources().getColor(R.color.hint_text));

            }
        }
        return validpassword;
    }

    private boolean validateConfirmPassword() {
        if (!et_confirmpassword.getText().toString().trim().equals(et_password.getText().toString().trim())) {

            tv_reg_confirmpassworderr.setVisibility(View.VISIBLE);
            validConfirmPassword.setVisibility(View.GONE);

            return false;
        } else {
            tv_reg_confirmpassworderr.setVisibility(View.INVISIBLE);
            validConfirmPassword.setVisibility(View.VISIBLE);

        }

        return true;
    }


    private boolean validateEmail() {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(et_email.getText().toString()).matches()) {
            tv_reg_emailerror.setVisibility(View.VISIBLE);
            validemail.setVisibility(View.GONE);
        } else {
            tv_reg_emailerror.setVisibility(View.INVISIBLE);
            validemail.setVisibility(View.VISIBLE);
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(et_email.getText().toString()).matches();
    }


    private boolean validate() {

        if(et_email.getText().toString().length() == 0 || et_name.getText().toString().trim().length() == 0
                || et_password.getText().toString().length() ==0 || et_confirmpassword.getText().toString().length()==0){
            Toast.makeText(getApplicationContext(),R.string.empty_error_message,Toast.LENGTH_SHORT).show();
            return  false;
        }
        if (!validateName()) {
            Toast.makeText(getApplicationContext(),R.string.name_error_message,Toast.LENGTH_SHORT).show();
            return false;
        } else if(!checkUserNameFormat()){
            Toast.makeText(getApplicationContext(),R.string.user_name_format_error_message,Toast.LENGTH_SHORT).show();
            return false;
        } else if (!validateEmail()) {
            Toast.makeText(getApplicationContext(),R.string.email_error_message,Toast.LENGTH_SHORT).show();
            return false;
        } else if (!validatePassword()) {
            Toast.makeText(getApplicationContext(),R.string.paswd_error_message,Toast.LENGTH_SHORT).show();
            return false;
        } else if (!validateConfirmPassword()) {
            Toast.makeText(getApplicationContext(),R.string.paswd_match_error_message,Toast.LENGTH_SHORT).show();
            return false;
        }else if (et_name.getText().toString().equalsIgnoreCase(et_password.getText().toString())){
            Toast.makeText(getApplicationContext(),R.string.same_user_name_and_password,Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!termsAccepted.isChecked()){
            Toast.makeText(getApplicationContext(),R.string.LoginOrRegistrationActivity_confirm_the_term,Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }
}
