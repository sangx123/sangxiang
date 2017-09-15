package com.nxcomm.blinkhd.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.hubble.BaseActivity;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.util.CommonConstants;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import base.hubble.PublicDefineGlob;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

/**
 * Created by sonikas on 11/04/17.
 */

public class ApplicationSettingsActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    private RelativeLayout mDNDLayout;
    private SwitchCompat mSoundSwitch, mVibrateSwitch,mBgMonitorSwitch,mRemoteTimeoutSwitch;
    private TextView mSendAppLogText;
    private SecureConfig settings = HubbleApplication.AppConfig;
    private RadioGroup mRgTimeformat, mTempformat;
    private RadioButton mRbTwelve,mRbTwentyfour;
    private RadioButton mCentigrade, mFahrenheit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        //setUpActionBar
        TextView tv_title = (TextView) findViewById(R.id.tv_toolbar_title);
        tv_title.setText(getString(R.string.app_settings_title));
        ImageView tv_back=(ImageView)findViewById(R.id.tv_toolbar_back) ;
        tv_back.setOnClickListener(this);

        mDNDLayout=(RelativeLayout)findViewById(R.id.dnd_layout);
        mDNDLayout.setOnClickListener(this);
        mSoundSwitch=(SwitchCompat)findViewById(R.id.sound_on_off_switch);
        mSoundSwitch.setOnCheckedChangeListener(this);
        mVibrateSwitch=(SwitchCompat)findViewById(R.id.vibrate_on_off_switch);
        mVibrateSwitch.setOnCheckedChangeListener(this);
        mBgMonitorSwitch=(SwitchCompat)findViewById(R.id.bgmonitor_on_off_switch);
        mBgMonitorSwitch.setOnCheckedChangeListener(this);
        mRemoteTimeoutSwitch=(SwitchCompat)findViewById(R.id.timeout_on_off_switch);
        mRemoteTimeoutSwitch.setOnCheckedChangeListener(this);
        mSendAppLogText=(TextView)findViewById(R.id.sendlogs_txt);
        mSendAppLogText.setOnClickListener(this);
        mRgTimeformat = (RadioGroup) findViewById(R.id.rg_timeformat);
        mTempformat = (RadioGroup) findViewById(R.id.tempformat);

        mRbTwelve = (RadioButton) findViewById(R.id.rb_twelve);
        mRbTwentyfour = (RadioButton) findViewById(R.id.rb_twentyfour);
        mCentigrade = (RadioButton) findViewById(R.id.rb_celsius);
        mFahrenheit = (RadioButton) findViewById(R.id.rb_fh);

        mCentigrade.setOnClickListener(this);
        mFahrenheit.setOnClickListener(this);
        mRbTwelve.setOnClickListener(this);
        mRbTwentyfour.setOnClickListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isSoundEnabled= CommonUtil.getSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_NOTIFY_BY_SOUND, true);
        boolean isVibrateEnabled=CommonUtil.getSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_NOTIFY_BY_VIBRATE,false);
        boolean isBgMonitorEnabled=CommonUtil.getSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_BACKGROUND_MONITORING,false);

        boolean isRemoteTimeoutEnabled=CommonUtil.getSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_SHOULD_VIDEO_TIMEOUT,true);

        int isFaherenheitEnabled = settings.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_F);
        if(isFaherenheitEnabled == 0){
            mFahrenheit.setChecked(true);
            mCentigrade.setChecked(false);
        }else{
            mFahrenheit.setChecked(false);
            mCentigrade.setChecked(true);
        }


        if(CommonUtil.getSettingInfo(getApplicationContext(),SettingsPrefUtils.TIME_FORMAT_12, true)){
            mRbTwelve.setChecked(true);
        }else{
            mRbTwentyfour.setChecked(true);
        }


        mSoundSwitch.setChecked(isSoundEnabled);
        mVibrateSwitch.setChecked(isVibrateEnabled);
        mBgMonitorSwitch.setChecked(isBgMonitorEnabled);
        mRemoteTimeoutSwitch.setChecked(isRemoteTimeoutEnabled);

    }

    @Override
    protected void onStop(){
        super.onStop();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_toolbar_back:
                onBackPressed();
                break;
            case R.id.dnd_layout:
                Intent intent = new Intent(this, DoNotDisturbActivity.class);
                startActivity(intent);
                break;
            case R.id.sendlogs_txt:
                sendApplicationLog();

                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.APP_SETTINGS,AppEvents.AS_SEND_APP_LOGS_CLICKED,AppEvents.AS_SEND_APP_LOGS);
                ZaiusEvent appLogsEvt = new ZaiusEvent(AppEvents.AS_SEND_APP_LOGS);
                appLogsEvt.action(AppEvents.AS_SEND_APP_LOGS_CLICKED);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(appLogsEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.rb_celsius:
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_TEMP_FORMAT_C,AppEvents.TEMP_CELSIUS);
                ZaiusEvent cameraTempEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
                cameraTempEvt.action(AppEvents.CAMERA_TEMP_FORMAT_C);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(cameraTempEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
                settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
                mCentigrade.setChecked(true);
                mFahrenheit.setChecked(false);
                break;
            case R.id.rb_fh:
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_TEMP_FORMAT_F,AppEvents.TEMP_FORENHEIT);
                ZaiusEvent cameraTempFormatEvt = new ZaiusEvent(AppEvents.CAMERA_DETAIL);
                cameraTempFormatEvt.action(AppEvents.CAMERA_TEMP_FORMAT_F);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(cameraTempFormatEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
                settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_F);
                mFahrenheit.setChecked(true);
                mCentigrade.setChecked(false);
                break;

            case R.id.rb_twelve:
                mRbTwelve.setChecked(true);
                CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.TIME_FORMAT_12, true);
                break;
            case R.id.rb_twentyfour:
                mRbTwentyfour.setChecked(true);
                CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.TIME_FORMAT_12, false);
                break;

        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!buttonView.isPressed())
            return;
        String eventString;
        switch(buttonView.getId()){
            case R.id.sound_on_off_switch:
                CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_NOTIFY_BY_SOUND,isChecked);

                if(isChecked)
                    eventString=AppEvents.AS_NOTIFICATION_SOUND_ENABLED;
                else
                    eventString=AppEvents.AS_NOTIFICATION_SOUND_DISABLED;
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.APP_SETTINGS,eventString,AppEvents.AS_NOTIFICATION_SETTINGS);
                ZaiusEvent appLogsEvt = new ZaiusEvent(AppEvents.AS_NOTIFICATION_SETTINGS);
                appLogsEvt.action(eventString);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(appLogsEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.vibrate_on_off_switch:
                CommonUtil.setSettingInfo(getApplicationContext(), SettingsPrefUtils.PREFS_NOTIFY_BY_VIBRATE,isChecked);

                if(isChecked)
                    eventString=AppEvents.AS_NOTIFICATION_VIBRATE_ENABLED;
                else
                    eventString=AppEvents.AS_NOTIFICATION_VIBRATE_DISABLED;
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.APP_SETTINGS,eventString,AppEvents.AS_NOTIFICATION_SETTINGS);
                ZaiusEvent appLogsEvtVibrate = new ZaiusEvent(AppEvents.AS_NOTIFICATION_SETTINGS);
                appLogsEvtVibrate.action(eventString);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(appLogsEvtVibrate);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bgmonitor_on_off_switch:
                CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_BACKGROUND_MONITORING,isChecked);

                if(isChecked)
                    eventString=AppEvents.AS_BG_MONITORING_ENABLED;
                else
                    eventString=AppEvents.AS_BG_MONITORING_DISABLED;
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.APP_SETTINGS,eventString,AppEvents.AS_BG_MONITORING);
                ZaiusEvent appLogsEvtBg = new ZaiusEvent(AppEvents.AS_BG_MONITORING);
                appLogsEvtBg.action(eventString);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(appLogsEvtBg);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.timeout_on_off_switch:
                CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_SHOULD_VIDEO_TIMEOUT,isChecked);
                break;


        }
    }

    private void sendApplicationLog() {
        Crittercism.logHandledException(new Throwable(String.valueOf(System.currentTimeMillis())));

        String fileName = "logcat_" + System.currentTimeMillis() + ".txt";
        File outputFile = new File(getExternalCacheDir(), fileName);
        String encLogFileName = "encrypt_" + fileName;
        File encLogFile = new File(getExternalCacheDir(), encLogFileName);

        String logFilePath = HubbleApplication.getLogFilePath();
        outputFile = new File(logFilePath);
        Log.d("mbp", "Send Hubble log file length: " + outputFile.length());

        HubbleApplication.writeLogAndroidDeviceInfo();

        // Temporary stop print debug for preparing to encrypt log
        HubbleApplication.stopPrintAdbLog();

        // Zip log file to reduce file length
        String zipLogFilePath = logFilePath.replace(".log", ".zip");
        File zipLogFile = new File(zipLogFilePath);
        Log.d("mbp", "Zip log file path: " + zipLogFile.getAbsolutePath());
        HubbleApplication.zipLogFile(zipLogFilePath);

        // Encrypt the log file
        try {
            //encrypt(outputFile.getAbsolutePath(), encLogFile.getAbsolutePath());
            encrypt(zipLogFilePath, encLogFile.getAbsolutePath());
            Log.d("mbp", "Send enc Hubble log file length: " + encLogFile.length());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HubbleApplication.startPrintAdbLog();

        String titleEmail = "";
        String bodyEmail = getString(R.string.body_email);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"android.techsupport@hubblehome.com"});
        // Send encrypted file
        Uri contentUri;
        if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
            contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.vtech.fileprovider", encLogFile);
            titleEmail = String.format(getString(R.string.title_email), "Vtech", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny")) {
            contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.inanny.fileprovider", encLogFile);
            titleEmail = String.format(getString(R.string.title_email), "iNanny", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
            contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.beurer.fileprovider", encLogFile);
            titleEmail = String.format(getString(R.string.title_email), "Beurer", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
        } else {
            contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, CommonConstants.FILE_PROVIDER_AUTHORITY_HUBBLE, encLogFile);
            titleEmail = String.format(getString(R.string.title_email), "Hubble", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
        }

        sendIntent.putExtra(Intent.EXTRA_SUBJECT, titleEmail);
        sendIntent.putExtra(Intent.EXTRA_TEXT, bodyEmail);
        sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void encrypt(String plainFilePath, String cipherFilePath)
            throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException {
        // Here you read the cleartext.
        FileInputStream fis = new FileInputStream(plainFilePath);
        // This stream write the encrypted text. This stream will be wrapped by
        // another stream.
        FileOutputStream fos = new FileOutputStream(cipherFilePath);

        // Length is 16 byte
        SecretKeySpec sks = new SecretKeySpec("Super-LovelyDuck".getBytes(), "AES");
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        // Wrap the output stream
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        // Write bytes
        int b;
        byte[] d = new byte[4096];
        while ((b = fis.read(d)) != -1) {
            cos.write(d, 0, b);
        }
        // Flush and close streams.
        cos.flush();
        cos.close();
        fis.close();
    }


}
