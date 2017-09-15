package com.nxcomm.blinkhd.ui.customview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hubbleconnected.camera.R;


public class CameraStatusView extends FrameLayout
{
  public static final int DEVICE_STATUS_OFFLINE = 0;
  public static final int DEVICE_STATUS_CONNECTING = 1;
  public static final int DEVICE_STATUS_ONLINE = 2;
  public static final int DEVICE_STATUS_STANDBY = 3;
  public static final int DEVICE_STATUS_UPDATING = 4;

  public static final String DEVICE_STATUS_RES_ONLINE = "online";
  public static final String DEVICE_STATUS_RES_OFFLINE = "offline";
  public static final String DEVICE_STATUS_RES_STANDBY = "standby";
  public static final String DEVICE_STATUS_RES_ONLINE_200_SUCCESS = "200:success";
  public static final String DEVICE_STATUS_RES_OFFLINE_404_STATUS = "404:Device";

  public static final int ORBIT_BATTERY_CHARGING = 1;
  public static final int ORBIT_BATTERY_DISCHARGING = 0;

  private TextView textView;
  private boolean online;
  private int deviceStatus;
  private ImageView imageViewOnlineStatus;
  AnimationDrawable mUpdateAnimation;

  private CameraStatus cameraStatus;

  public enum CameraStatus {
    NORMAL, UPGRADING
  }

  public CameraStatusView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initView();
  }

  public CameraStatusView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  public CameraStatusView(Context context) {
    super(context);
    initView();
  }

  private void initView() {
    View view = inflate(getContext(), R.layout.camera_status_view, null);
    addView(view);

    if (!this.isInEditMode()) {
      textView = (TextView) view.findViewById(R.id.textViewCameraStatus);
      imageViewOnlineStatus = (ImageView) view.findViewById(R.id.imageViewCameraStatus);
    }
  }

  public boolean isOnline() {
    return online;
  }

  public void setOnline(boolean online) {
    this.online = online;
    if (online) {
      if (imageViewOnlineStatus != null) {
        imageViewOnlineStatus.setImageResource(R.drawable.settings_circle_green);
      }
      if (textView != null) {
        textView.setText(R.string.online);
      }
    } else {
      if (imageViewOnlineStatus != null) {
        imageViewOnlineStatus.setImageResource(R.drawable.settings_circle_disable);
      }
      if (textView != null) {
        textView.setText(R.string.offline);
      }
    }
  }

  public int getDeviceStatus() {
    return deviceStatus;
  }

  public void setDeviceStatus(int deviceStatus) {
    this.deviceStatus = deviceStatus;
    if (deviceStatus == DEVICE_STATUS_ONLINE) {
      if (imageViewOnlineStatus != null) {
        imageViewOnlineStatus.setBackgroundResource(0);
        imageViewOnlineStatus.setImageResource(R.drawable.settings_circle_green);
        if(mUpdateAnimation != null)
          mUpdateAnimation.stop();
      }
      if (textView != null) {
        textView.setText(R.string.online);
      }
    } else if (deviceStatus == DEVICE_STATUS_CONNECTING) {
      if (imageViewOnlineStatus != null) {
        imageViewOnlineStatus.setBackgroundResource(0);
        imageViewOnlineStatus.setImageResource(R.drawable.settings_circle_green);
        if(mUpdateAnimation != null)
          mUpdateAnimation.stop();
      }
      if (textView != null) {
        textView.setText(R.string.ConnectToNetworkActivity_connecting);
      }
    }
    else if (deviceStatus == DEVICE_STATUS_STANDBY)
    {
      if (imageViewOnlineStatus != null)
      {
        imageViewOnlineStatus.setBackgroundResource(0);
        imageViewOnlineStatus.setImageResource(R.drawable.settings_circle_standby);
        if(mUpdateAnimation != null)
          mUpdateAnimation.stop();
      }
      if (textView != null)
      {
        textView.setText(R.string.standby);
      }
    }
    else if(deviceStatus == DEVICE_STATUS_UPDATING)
    {
      if (imageViewOnlineStatus != null)
      {
        imageViewOnlineStatus.setImageResource(0);
        imageViewOnlineStatus.setBackgroundResource(R.drawable.update_animation);
        mUpdateAnimation = (AnimationDrawable)imageViewOnlineStatus.getBackground();
        if(mUpdateAnimation != null)
          mUpdateAnimation.start();
      }
      if (textView != null)
      {
        textView.setText(R.string.updating_status);
      }
    }
    else {
      if (imageViewOnlineStatus != null) {
        imageViewOnlineStatus.setBackgroundResource(0);
        imageViewOnlineStatus.setImageResource(R.drawable.settings_circle_disable);

      }
      if (textView != null) {
        textView.setText(R.string.offline);
      }
    }
  }

  public CameraStatus getCameraStatus() {
    return cameraStatus;
  }

  public void setCameraStatus(CameraStatus cameraStatus) {
    this.cameraStatus = cameraStatus;
    if (cameraStatus == CameraStatus.UPGRADING) {
      if (textView != null) {
        textView.setText(R.string.upgrading);
      }
    } else {
      setOnline(online);
    }
  }
}
