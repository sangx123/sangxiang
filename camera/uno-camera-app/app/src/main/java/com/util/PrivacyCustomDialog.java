package com.util;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hubble.devcomm.Device;
import com.hubble.dialog.AskForFreeTrialDialog;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.CameraListArrayAdapter2;

import org.w3c.dom.Text;

/**
 * Created by aruna on 14/03/17.
 */

public class PrivacyCustomDialog extends Dialog implements
        android.view.View.OnClickListener {
    TextView cancel, confirm;
    CheckBox dontShow;
    boolean isChecked = true;

    private PrivacyListener mPrivacyListener;

    public interface PrivacyListener{
        public void onPrivacyCancel();
        public void onPrivacyConfirmClick();
        public void doNotShowDailog(boolean isChecked);
        //public void onUpgradePlanClick();
    }

    public PrivacyCustomDialog(Activity a, PrivacyListener privacyListener) {
         super(a);

         this.mPrivacyListener = privacyListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_dailog);
        cancel = (TextView) findViewById(R.id.cancel);
        confirm = (TextView) findViewById(R.id.confirm);
        dontShow = (CheckBox) findViewById(R.id.dontshow);

        cancel.setOnClickListener(this);
        confirm.setOnClickListener(this);
        dontShow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                mPrivacyListener.onPrivacyCancel();
                dismiss();
                break;
            case R.id.confirm:
                mPrivacyListener.onPrivacyConfirmClick();
                dismiss();
                break;
            case R.id.dontshow:
                mPrivacyListener.doNotShowDailog(dontShow.isChecked());
               // dismiss();
                break;

        }
    }


}
