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


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.hubble.framework.service.account.AccountManagement;
import com.hubble.framework.service.account.AccountMgrListener;
import com.hubble.framework.service.account.UserPasswordInfo;
import com.hubbleconnected.camera.R;
import com.util.NetworkDetector;


public class ForgetPasswordActivity extends AppCompatActivity implements View.OnClickListener, AccountMgrListener {
    private Button mSend;

    private EditText et_loginname;
    private AccountManagement accountManagement;
    private ProgressDialog mProgressDialog;

    private TextView tv_nameerror;
    private TextView mUserTextView;
    NetworkDetector networkDetector;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_forgetpassword);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        tv_nameerror = (TextView) findViewById(R.id.tv_email_error);
        et_loginname = (EditText) findViewById(R.id.et_loginname);
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
        mSend = (Button) findViewById(R.id.btn_send);
        mSend.setOnClickListener(this);


        accountManagement = new AccountManagement();
        networkDetector = new NetworkDetector(getApplicationContext());


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.btn_send:
                Boolean isInternetPresent = networkDetector.isConnectingToInternet();
                if(isInternetPresent) {
                    if (validate()) {
                        UserPasswordInfo userPasswordInfo = new UserPasswordInfo();
                        userPasswordInfo.setUserName(et_loginname.getText().toString());
                        accountManagement.updateUserPassword(userPasswordInfo);
                        displayProgressDialog();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),R.string.enable_internet_connection,Toast.LENGTH_SHORT).show();
                }
                break;


        }
    }

    @Override
    public void onUserLoginStatus(accountMgmtEvent accEvent, String response) {
        dismissDialog();
       // Log.d("Response", "Response =" + response);
        if (accEvent == accountMgmtEvent.USER_FORGETPASSWORD_SUCCESS) {
            try {
                Intent mainIntent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                finish();
            } catch (Exception e) {
                Log.d("Exception", "Authtoken error=" + e);
            }
         Toast.makeText(getApplicationContext(), R.string.we_just_emailed_you_the_link_to_reset_your_password, Toast.LENGTH_SHORT).show();
        } else if (accEvent == accountMgmtEvent.USER_FORGETPASSWORD_FAILURE) {

            Toast.makeText(getApplicationContext(), R.string.please_enter_emailid_and_try_again, Toast.LENGTH_SHORT).show();
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
        mProgressDialog.setMessage("Processing request");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        ;
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
       // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        Intent backtoLogin = new Intent(ForgetPasswordActivity.this,LoginActivity.class);
        startActivity(backtoLogin);
    }


    private boolean validateEmail() {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(et_loginname.getText().toString()).matches()) {
            tv_nameerror.setVisibility(View.VISIBLE);
        } else {
            tv_nameerror.setVisibility(View.INVISIBLE);
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(et_loginname.getText().toString()).matches();
    }


    private boolean validate() {
        if (et_loginname.getText().toString().trim().length() == 0) {
            Toast.makeText(getBaseContext(),R.string.please_enter_email, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!validateEmail()) {
            return false;
        }
        return true;

    }
}
