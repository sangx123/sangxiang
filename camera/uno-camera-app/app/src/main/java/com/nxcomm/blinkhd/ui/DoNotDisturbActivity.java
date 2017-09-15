package com.nxcomm.blinkhd.ui;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.BaseActivity;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * Created by sonikas on 10/04/17.
 */

public class DoNotDisturbActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SwitchCompat mDNDOnOffSwitch, mDNDInfiniteSwitch;
    private TextView mDNDDurationText, mDNDTimeText, mDNDHintText, mDNDInfiniteText;
    private ImageView mAddDurImage, mSubDurImage;
    private RelativeLayout mInifniteDNDLayout;
    private SecureConfig settings = HubbleApplication.AppConfig;
    private int mDNDduration=30;
    private long mDNDDateInMillis;
    private static final String TAG="DoNotDisturbActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dnd);

        //setUpActionBar
        TextView tv_title = (TextView) findViewById(R.id.tv_toolbar_title);
        tv_title.setText(getString(R.string.do_not_disturb));
        ImageView tv_back=(ImageView)findViewById(R.id.tv_toolbar_back) ;
        tv_back.setOnClickListener(this);

        initialize();


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //get dnd settings from shared pref
        boolean dndEnabled= CommonUtil.getSettingInfo(getApplicationContext(), SettingsPrefUtils.PREFS_IS_DO_NOT_DISTURB_ENABLE,false);
        mDNDDateInMillis=CommonUtil.getLongValue(getApplicationContext(),SettingsPrefUtils.PREFS_DO_NOT_DISTURB_REMAINING_TIME, DateTime.now().getMillis());

        Date receivedDate = new Date(mDNDDateInMillis);
        Date currentDate = new Date(System.currentTimeMillis());
        long duration  = receivedDate.getTime() - currentDate.getTime();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        /*Below if condition is used only for the users who migrate from old Monitor app to new App */
        if(dndEnabled && !CommonUtil.checkSettings(getApplicationContext(),SettingsPrefUtils.PREFS_DO_NOT_DISTURB_DURATION)){
            long mins_30 = 30;
            long mins_60 = 60;
            //show expire time
            if(diffInMinutes < mins_30) {

                mDNDduration = 30;
            }else if(diffInMinutes > mins_30 && diffInMinutes < mins_60){
                mDNDduration = 60;
            }else{
                /* Convert to hours and then convert hours to min to show the remaining time matching
                * Here we will missout the fraction time, as we donyt have any UI to show fraction time
                * such as 1:30 or 1:45, we consider only hours */
                int mDNDDurationInHours = (int) diffInMinutes/60;
                mDNDduration = (int) mDNDDurationInHours * 60;
            }
            CommonUtil.setSettingValue(getApplicationContext(),SettingsPrefUtils.PREFS_DO_NOT_DISTURB_DURATION, mDNDduration);

            updateRemainingTime();
            Toast.makeText(getApplicationContext(), "DND set to : " + mDNDduration, Toast.LENGTH_LONG).show();
        }
        //reset to default DND duration
        else {
                 mDNDduration=CommonUtil.getSettingValue(getApplicationContext(),SettingsPrefUtils.PREFS_DO_NOT_DISTURB_DURATION,30);

                 if(mDNDduration==0)
                     mDNDduration=30;
             }

             Log.d(TAG, "DND enabled:"+dndEnabled);
        if(dndEnabled){
            //if DND is ON but time has expired, reset DND state to OFF
            boolean isAfterRemainingTime=DateTime.now().isAfter(new DateTime(mDNDDateInMillis));
            //if time has expired and infinite DND option is disabled
            if(isAfterRemainingTime && !(mDNDduration==Integer.MAX_VALUE)){
                Log.d(TAG, "DND time expired");
                dndEnabled=false;
                CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_IS_DO_NOT_DISTURB_ENABLE,dndEnabled);
            }
        }
        updateDNDView(dndEnabled);

    }

    @Override
    protected void onStop(){
        super.onStop();

    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.add_duration_img:
                if(mDNDduration==30){
                    mDNDduration=60;
                }else{
                    mDNDduration=mDNDduration+60;
                }
                CommonUtil.setSettingValue(getApplicationContext(),SettingsPrefUtils.PREFS_DO_NOT_DISTURB_DURATION,mDNDduration);
                updateRemainingTime();
                updateDNDView(true);
                break;
            case R.id.minus_duration_img:
                if(mDNDduration==60){
                    mDNDduration=30;
                }else{
                    mDNDduration=mDNDduration-60;
                }
                CommonUtil.setSettingValue(getApplicationContext(),SettingsPrefUtils.PREFS_DO_NOT_DISTURB_DURATION,mDNDduration);
                updateRemainingTime();
                updateDNDView(true);
                break;
            case R.id.tv_toolbar_back:
                onBackPressed();
                break;



        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!buttonView.isPressed())
            return;
        switch(buttonView.getId()){
            case R.id.dnd_on_off_switch:
                if(isChecked) {
                    Log.d(TAG, "DND enabled");
                    CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_IS_DO_NOT_DISTURB_ENABLE,true);
                    CommonUtil.setSettingValue(getApplicationContext(),SettingsPrefUtils.PREFS_DO_NOT_DISTURB_DURATION,mDNDduration);
                    updateRemainingTime();
                    updateDNDView(true);
                }else{
                    Log.d(TAG, "DND disabled");
                    CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_IS_DO_NOT_DISTURB_ENABLE,false);
                    CommonUtil.setSettingValue(getApplicationContext(),SettingsPrefUtils.PREFS_DO_NOT_DISTURB_DURATION,0);
                    updateDNDView(false);
                }
                break;
            case R.id.dnd_infinite_switch:
                if(isChecked){
                    Log.d(TAG, "'Until I turn it Off' option enabled");
                    mDNDduration=Integer.MAX_VALUE;
                    CommonUtil.setSettingValue(getApplicationContext(), SettingsPrefUtils.PREFS_DO_NOT_DISTURB_DURATION,mDNDduration);
                    updateDNDView(true);
                }else{
                    Log.d(TAG, "DND disabled");
                    CommonUtil.setSettingInfo(getApplicationContext(),SettingsPrefUtils.PREFS_IS_DO_NOT_DISTURB_ENABLE,false);
                    CommonUtil.setSettingValue(getApplicationContext(),SettingsPrefUtils.PREFS_DO_NOT_DISTURB_DURATION,0);
                    updateDNDView(false);
                }

        }


    }

    private void initialize(){
        mInifniteDNDLayout=(RelativeLayout)findViewById(R.id.infinite_dnd_layout);
        mDNDOnOffSwitch=(SwitchCompat)findViewById(R.id.dnd_on_off_switch);
        mDNDInfiniteSwitch=(SwitchCompat)findViewById(R.id.dnd_infinite_switch);
        mDNDDurationText=(TextView)findViewById(R.id.dnd_duration_text);
        mDNDTimeText=(TextView)findViewById(R.id.dnd_time_text);
        mDNDHintText=(TextView)findViewById(R.id.dnd_duration_hint);
        mDNDInfiniteText=(TextView)findViewById(R.id.dnd_infinite_hint);
        mAddDurImage=(ImageView)findViewById(R.id.add_duration_img);
        mSubDurImage=(ImageView)findViewById(R.id.minus_duration_img);

        mDNDOnOffSwitch.setOnCheckedChangeListener(this);
        mDNDInfiniteSwitch.setOnCheckedChangeListener(this);
        mAddDurImage.setOnClickListener(this);
        mSubDurImage.setOnClickListener(this);
    }

    private void updateDNDView(boolean isDNDEnabled){
        if(isDNDEnabled){
            mDNDOnOffSwitch.setChecked(true);
            //if infinte DND option is enabled
            if(mDNDduration==Integer.MAX_VALUE){
                mInifniteDNDLayout.setBackgroundColor(getResources().getColor(R.color.dnd_infinite_bg));
                mDNDInfiniteSwitch.setChecked(true);
                //reseting duration to default
                mDNDduration=30;
                if(mDNDDurationText.getText().equals("")) {
                    mDNDDurationText.setText(mDNDduration+" "+getString(R.string.mins));
                    mSubDurImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_minus_inactiv_disabled));
                    mAddDurImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_activ));
                }

                disbaleDNDLayout(true);
                mDNDTimeText.setText("");

            }else {
                mInifniteDNDLayout.setBackgroundColor(getResources().getColor(R.color.white));
                mDNDInfiniteSwitch.setChecked(false);

                //setting state of + and - buttons
                if (mDNDduration == 30) {
                    mSubDurImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_minus_inactiv_disabled));
                    mSubDurImage.setEnabled(false);
                    mAddDurImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_activ));
                    mAddDurImage.setEnabled(true);
                    mDNDDurationText.setText(mDNDduration +" "+ getString(R.string.mins));
                } else if (mDNDduration == 24*60) {
                    mSubDurImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_minus_inactiv));
                    mSubDurImage.setEnabled(true);
                    mAddDurImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_activ_disabled));
                    mAddDurImage.setEnabled(false);
                    mDNDDurationText.setText(mDNDduration/60 +" "+ getString(R.string.hours));
                } else {
                    mSubDurImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_minus_inactiv));
                    mAddDurImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_activ));
                    mAddDurImage.setEnabled(true);
                    mSubDurImage.setEnabled(true);
                    mDNDDurationText.setText(mDNDduration/60 +" "+ getString(R.string.hours));
                }

                Date expireTime= new Date(mDNDDateInMillis);
                SimpleDateFormat formatter ;
                if(CommonUtil.getSettingInfo(getApplicationContext(),SettingsPrefUtils.TIME_FORMAT_12, true)){
                    formatter = new SimpleDateFormat("hh:mm a");
                }else{
                    formatter = new SimpleDateFormat("HH:mm");
                }
                String newTime = formatter.format(expireTime);
                mDNDTimeText.setText(getString(R.string.dnd_active_till,newTime));
                enableDNDLayout();

            }
        }else{
            mDNDOnOffSwitch.setChecked(false);
            mDNDInfiniteSwitch.setChecked(false);
            mInifniteDNDLayout.setBackgroundColor(getResources().getColor(R.color.white));
            mDNDDurationText.setText("30 "+getString(R.string.mins));
            mDNDTimeText.setText("");
            disbaleDNDLayout(false);

        }
    }

    private void enableDNDLayout(){
        mDNDOnOffSwitch.setEnabled(true);
        mDNDInfiniteSwitch.setEnabled(true);
        mDNDHintText.setTextColor(getResources().getColor(R.color.text_blue));
        mDNDInfiniteText.setTextColor(getResources().getColor(R.color.help_text_gray));
        mDNDTimeText.setTextColor(getResources().getColor(R.color.color_text_grey));
        mDNDDurationText.setTextColor(getResources().getColor(R.color.text_blue));
        mDNDInfiniteSwitch.setEnabled(true);

    }

    private void disbaleDNDLayout(boolean isDNDEnabled){
        mAddDurImage.setEnabled(false);
        mSubDurImage.setEnabled(false);
        mDNDHintText.setTextColor(getResources().getColor(R.color.disbaled_text));
        mDNDInfiniteText.setTextColor(getResources().getColor(R.color.disbaled_text));
        mDNDTimeText.setTextColor(getResources().getColor(R.color.disbaled_text));
        mDNDDurationText.setTextColor(getResources().getColor(R.color.disbaled_text));
        mAddDurImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_activ_disabled));
        mSubDurImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_minus_inactiv_disabled));
        mDNDOnOffSwitch.setEnabled(true);
        if(isDNDEnabled) {
            mDNDInfiniteSwitch.setEnabled(true);
        }else{
            mDNDInfiniteSwitch.setEnabled(false);
        }

    }

    private void updateRemainingTime(){
        DateTime doNotDisturbTime = DateTime.now().plusMinutes(mDNDduration);
        long time = doNotDisturbTime.getMillis();
        mDNDDateInMillis=time;
        CommonUtil.setLongValue(getApplicationContext(),SettingsPrefUtils.PREFS_DO_NOT_DISTURB_REMAINING_TIME,time);
    }




}
