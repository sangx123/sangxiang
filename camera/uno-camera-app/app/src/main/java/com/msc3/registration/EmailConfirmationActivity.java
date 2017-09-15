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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.hubbleconnected.camera.R;
import com.msc3.registration.fragments.EmailConfirmationFragment;
import com.nxcomm.blinkhd.ui.MainActivity;

public class EmailConfirmationActivity extends AppCompatActivity
{
    private static final String TAG = EmailConfirmationActivity.class.getSimpleName();

    public static final String ROOT_FRAGMENT = "RootFragment";

    private Context mContext = EmailConfirmationActivity.this;

    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_email_confirm);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

        if(savedInstanceState == null)
            switchFragment(EmailConfirmationFragment.newInstance(),false);
    }

    public void startMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    public void displayProgressDialog()
    {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        ;
    }

    public void dismissDialog()
    {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }


    public void switchFragment(Fragment fragment, boolean addToBackStack)
    {
        if (fragment != null)
        {
            getFragmentManager().popBackStackImmediate(ROOT_FRAGMENT, 0);
            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.email_confirm_main_content);
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            if (addToBackStack)
            {
                ft.replace(R.id.email_confirm_main_content, fragment);
                ft.addToBackStack(null);
            }
            else
            {
                if (currentFragment == null)
                {
                    ft.add(R.id.email_confirm_main_content, fragment, ROOT_FRAGMENT);
                }
                else
                {
                    ft.replace(R.id.email_confirm_main_content, fragment, ROOT_FRAGMENT);
                }
            }
            ft.commit();
        }
    }

    public interface OnBackPress {
        boolean onBackPressed();
    }

    @Override
    public void onBackPressed() {
        onBackPressed(false);
    }


    public void onBackPressed(boolean force)
    {
        if(!force) {
            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.main_content);
            if (currentFragment instanceof OnBackPress) {
                if (((OnBackPress) currentFragment).onBackPressed()) {
                    return;
                }
            }
        }
        super.onBackPressed();
    }

}
