package com.nxcomm.blinkhd.ui;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;

import retrofit.http.HEAD;


/**
 * Created by hoang on 3/8/16.
 */
public class CameraListViewHolder extends RecyclerView.ViewHolder {

    public boolean isSensor;
    public String registrationId;
    public View rootView;
    public ImageButton btnSettings, imgConnectedIcon;
    public ImageView camImage;
    public CameraStatusView cameraStatus;
    public TextView cameraName;
    public TextView currentTemperature;
    public TextView currentHumidity;
    public LinearLayout humidityLayout, temperatureLayout;
    public String imageURL;
    public TextView linkedCamera, sensorStatus;
    public LinearLayout cameraInfoSensor;
    public ImageButton buttonBTA;
    public TextView notificationSwitchON;
    public SwitchCompat notificationSwitch, privacySwitch;
    public TextView eventName;
    public TextView eventTime;
    public LinearLayout batteryLayout;
    public TextView batteryStatusTv;
    public LinearLayout notificationLayout;
    public ImageView batteryImage;
    public LinearLayout layoutImageCamera, llMotionLayout, llTemparatureAndHumidityLayout;
    public ImageView addFocusTag, addedTag1, addedTag2, addedTag3;
    public TextView focusTagName, addedTagName1, addedTagName2, addedTagName3;
    public LinearLayout focusTagLayout, addedFocusTagLayout1, addedFocusTagLayout2, addedFocusTagLayout3;
    public LinearLayout addFocusTagLayout;
    public RelativeLayout privacyModeLayout;
    public TextView privacyCameraName;

    public LinearLayout sdcardLayout;
    public TextView sdcardStatusTv;
    public ImageView sdcardImageView;


    public CameraListViewHolder(View itemView) {
        super(itemView);
        rootView = itemView;
        isSensor = false;
        registrationId = null;
        initViews();
    }

    public CameraListViewHolder(View itemView, boolean isSensor) {
        super(itemView);
        rootView = itemView;
        this.isSensor = isSensor;
        registrationId = null;
        initViews();
    }

    private void initViews() {
        if (!isSensor) {
            // Update camera registration id for holder view
            btnSettings = (ImageButton) rootView.findViewById(R.id.list_row_camera_setting_camSettingBtn);
            camImage = (ImageView) rootView.findViewById(R.id.list_row_camera_setting_imageCamera);
            cameraStatus = (CameraStatusView) rootView.findViewById(R.id.list_row_camera_ImageView_CameraStatus);
            cameraName = (TextView) rootView.findViewById(R.id.textViewCameraName);
            buttonBTA = (ImageButton) rootView.findViewById(R.id.list_row_camera_setting_btaButton);
            currentTemperature = (TextView) rootView.findViewById(R.id.temperature);
            temperatureLayout = (LinearLayout) rootView.findViewById(R.id.temperature_layout);
            currentHumidity = (TextView) rootView.findViewById(R.id.humidity);
            humidityLayout = (LinearLayout) rootView.findViewById(R.id.humidity_layout);
            notificationSwitch = (SwitchCompat) rootView.findViewById(R.id.camera_on_off_switch);
         //   notificationSwitchON = (TextView) rootView.findViewById(R.id.switchon);
            notificationLayout = (LinearLayout) rootView.findViewById(R.id.notifylayout);
            batteryLayout = (LinearLayout) rootView.findViewById(R.id.battery_status_layout);
            batteryImage = (ImageView) rootView.findViewById(R.id.battery_image);
            batteryStatusTv = (TextView) rootView.findViewById(R.id.battery_status);
            imageURL = "";

            eventName = (TextView) rootView.findViewById(R.id.dashboard_event_name);
            eventTime = (TextView) rootView.findViewById(R.id.dashboard_event_time);
            layoutImageCamera = (LinearLayout) rootView.findViewById(R.id.ll_imageCamera);
            llTemparatureAndHumidityLayout = (LinearLayout) rootView.findViewById(R.id.ll_humidity_and_temparature);
            llMotionLayout = (LinearLayout) rootView.findViewById(R.id.ll_motionLayout);
            //focusTagName = (TextView) rootView.findViewById(R.id.focustagName);
            addFocusTag = (ImageView) rootView.findViewById(R.id.addtag);
            focusTagLayout = (LinearLayout) rootView.findViewById(R.id.focustaglayout);
            addFocusTagLayout = (LinearLayout) rootView.findViewById(R.id.focustagadd);
            addedFocusTagLayout1 = (LinearLayout) rootView.findViewById(R.id.focustagadded1);
            //addedTag1 = (ImageView) rootView.findViewById(R.id.addedtag1);
            addedTagName1 = (TextView) rootView.findViewById(R.id.focustagName1);

            sdcardLayout = (LinearLayout)rootView.findViewById(R.id.sdcard_status_layout);
            sdcardStatusTv = (TextView) rootView.findViewById(R.id.sdcard_space_tv);
            sdcardImageView = (ImageView) rootView.findViewById(R.id.sdcard_image);


            addedFocusTagLayout2 = (LinearLayout) rootView.findViewById(R.id.focustagadded2);
           // addedTag2 = (ImageView) rootView.findViewById(R.id.addedtag2);
            addedTagName2 = (TextView) rootView.findViewById(R.id.focustagName2);

            addedFocusTagLayout3 = (LinearLayout) rootView.findViewById(R.id.focustagadded3);
           // addedTag3 = (ImageView) rootView.findViewById(R.id.addedtag3);
            addedTagName3 = (TextView) rootView.findViewById(R.id.focustagName3);

            privacyModeLayout = (RelativeLayout) rootView.findViewById(R.id.privacy_mode);
            privacyCameraName = (TextView) rootView.findViewById(R.id.privacycameraname);
            privacySwitch = (SwitchCompat) rootView.findViewById(R.id.privacy_switch);

        } else {
            // Update camera registration id for holder view
            btnSettings = (ImageButton) rootView.findViewById(R.id.list_row_sensor_setting_buton);
            camImage = (ImageView) rootView.findViewById(R.id.list_row_sensor_image);
            cameraName = (TextView) rootView.findViewById(R.id.textViewSensorName);
            cameraInfoSensor = (LinearLayout) rootView.findViewById(R.id.camera_info);
            linkedCamera = (TextView) rootView.findViewById(R.id.textViewLinkedCameraName);
            sensorStatus = (TextView) rootView.findViewById(R.id.textViewActiveState);
            imgConnectedIcon = (ImageButton) rootView.findViewById(R.id.list_connected_icon);
            notificationSwitch = (SwitchCompat) rootView.findViewById(R.id.camera_on_off_switch);
            batteryLayout = (LinearLayout) rootView.findViewById(R.id.battery_status_layout);
            batteryStatusTv = (TextView) rootView.findViewById(R.id.battery_status);
           // focusTagName = (TextView) rootView.findViewById(R.id.focustagName);
            addFocusTag = (ImageView) rootView.findViewById(R.id.addtag);
            focusTagLayout = (LinearLayout) rootView.findViewById(R.id.focustaglayout);
            imageURL = "";

            sdcardLayout = (LinearLayout)rootView.findViewById(R.id.sdcard_status_layout);
            sdcardStatusTv = (TextView) rootView.findViewById(R.id.sdcard_space_tv);
            sdcardImageView = (ImageView) rootView.findViewById(R.id.sdcard_image);
        }

    }

    interface IDataChangeListener {
        void onDataChanged();
    }
}
