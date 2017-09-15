package com.sensor.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hubble.devcomm.Device;
import com.hubbleconnected.camera.R;
import com.sensor.constants.SensorConstants;

import java.io.File;

import base.hubble.PublicDefineGlob;
import base.hubble.database.DeviceProfile;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingTagAsMotionSensorFragment extends Fragment {

    private Activity activity;
    private View view;
    // private TextView mCheckingDetectionIsActive, mCheckingConnection, mActivateNowText;
    //private LinearLayout mActivateNowLayout;
    // private ImageView mSettingSensorImage, mNotifyDoneImageView, mNotifyProgress;
    private Device mSelectedCameraDevice;
    //  private AnimationDrawable mNotifyAnimationDrawable, mSensorPairingAnimation;
    private RelativeLayout mActivatingLayout, mCongratulationLay;
    private Button mTakeMeIn;
    private TextView mTagActivatedText;
    String mTagName = "";
    private WebView webView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_setting_up_tag_as_sensor, container, false);
    /*mCheckingDetectionIsActive = (TextView) findViewById(R.id.checking_detection_is_active);
    mCheckingConnection = (TextView) findViewById(R.id.checking_connection);
    mSettingSensorImage = (ImageView) findViewById(R.id.setting_sensor_image);
    mActivateNowLayout = (LinearLayout) findViewById(R.id.activate_now_sensor);
    mActivateNowText = (TextView) findViewById(R.id.txt_activate_now_sensor);
    mNotifyDoneImageView = (ImageView) findViewById(R.id.notify_sensor_done);*/

        mActivatingLayout = (RelativeLayout) findViewById(R.id.tag_activating);
        mCongratulationLay = (RelativeLayout) findViewById(R.id.tag_activated_layout);
        mTakeMeIn = (Button) findViewById(R.id.tag_takemein);
        mTagActivatedText = (TextView) findViewById(R.id.tag_activated_text);
        webView = (WebView) findViewById(R.id.setting_sensor_image);
        Bundle args = getArguments();
        mTagName = args.getString("name_sensor");

        webView.getSettings().setJavaScriptEnabled(true);
        String path = new File("file:///android_res/drawable/tag_setup_loop.gif").getPath();
        webView.loadUrl(path);
   /* mNotifyProgress = (ImageView) findViewById(R.id.notify_sensor_progress);


    mNotifyProgress.setBackgroundResource(R.drawable.progress_orange_drawable);
    mNotifyAnimationDrawable = (AnimationDrawable) mNotifyProgress.getBackground();*/
        mActivatingLayout.setVisibility(View.VISIBLE);
        mCongratulationLay.setVisibility(View.GONE);
   /* mActivateNowText.setTextColor(Color.LTGRAY);
    mActivateNowLayout.setEnabled(false);*/


        mSelectedCameraDevice = ((AddSensorActivity) activity).getSelectedCameraDevice();
        mTakeMeIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceProfile deviceProfile = mSelectedCameraDevice.getProfile();
                if (((AddSensorActivity) activity).isFromCameraDetails()) {
                    Intent intent = new Intent();
                    intent.putExtra(SensorConstants.EXTRA_SENSOR_TYPE, ((AddSensorActivity) activity).getSensorType());
                    activity.setResult(PublicDefineGlob.REGISTER_SENSOR_ACTIVITY_RESULT, intent);
                    activity.finish();

                } else {
                    Intent intent = new Intent();
                    intent.putExtra(SensorConstants.EXTRA_DEVICE_REGISTRATION_ID, deviceProfile.getRegistrationId());
                    intent.putExtra(SensorConstants.EXTRA_SENSOR_TYPE, ((AddSensorActivity) activity).getSensorType());
                    activity.setResult(PublicDefineGlob.SETUP_SENSOR_ACTIVITY_RESULT, intent);
                    activity.finish();
                }
            }
        });

    /*switch (((AddSensorActivity) activity).getSensorType()) {
      case SensorConstants.PRESENCE_DETECTION:
        mCheckingDetectionIsActive.setText(R.string.checking_connection_to_sensor);
        mSettingSensorImage.setImageResource(R.drawable.animation_presence_setup_frame);
        break;
      case SensorConstants.MOTION_DETECTION:
        mCheckingDetectionIsActive.setText(R.string.checking_connection_to_sensor);
        mSettingSensorImage.setImageResource(R.drawable.animation_motion_setup_frame);
        break;
    }

    mSettingSensorImage.setBackgroundResource(R.drawable.sensor_pairing_animation);
    mSensorPairingAnimation = (AnimationDrawable) mSettingSensorImage.getBackground();*/

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onPause() {
        //mNotifyAnimationDrawable.stop();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (((AddSensorActivity) activity).isSensorPairingNorified())
            enableActivateNow();
        else {
     /* mNotifyAnimationDrawable.start();
      mSensorPairingAnimation.start();*/
        }
    }


    private View findViewById(int id) {
        return view.findViewById(id);
    }

    public void enableActivateNow() {
    /*mNotifyAnimationDrawable.stop();
    mNotifyProgress.setVisibility(View.GONE);
    mNotifyDoneImageView.setVisibility(View.VISIBLE);
    mSensorPairingAnimation.stop();
    mCheckingConnection.setVisibility(View.INVISIBLE);
    mCheckingDetectionIsActive.setText(R.string.registration_done);
    mActivateNowLayout.setEnabled(true);
    mActivateNowText.setTextColor(Color.BLUE);*/
        mActivatingLayout.setVisibility(View.GONE);
        mCongratulationLay.setVisibility(View.VISIBLE);
        String mFormattedNameString = String.format(getString(R.string.tag_activated), mTagName);

        mTagActivatedText.setText(mFormattedNameString);
    }

    public void updateSensorSetupProgress(String msg) {
    /*if (mCheckingConnection != null) {
      mCheckingConnection.setText(msg);
      mCheckingConnection.setVisibility(View.VISIBLE);
    }*/
    }
}
