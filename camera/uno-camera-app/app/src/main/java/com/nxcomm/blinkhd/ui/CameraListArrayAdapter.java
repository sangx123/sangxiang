package com.nxcomm.blinkhd.ui;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.helpers.AsyncPackage;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.sensor.constants.SensorConstants;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.hubble.PublicDefineGlob;
import com.hubbleconnected.camera.BuildConfig;

/**
 * @Deprecated: do not use it any more. Please use CameraListArrayAdapter2
 */
@Deprecated
public class CameraListArrayAdapter extends ArrayAdapter<Device> {
  private static final String TAG = "CameraListArrayAdapter";
  private final MainActivity activity;
  List<Device> mDevices = new ArrayList<Device>();
  List<Device> allDevices = new ArrayList<Device>();
  Map<String, Boolean> mShouldLoadPreviewMap = new HashMap<>();
  ColorMatrixColorFilter grayScaleFilter;
  private boolean isSensor;
  private SecureConfig settings = HubbleApplication.AppConfig;
  private boolean isOfflineMode = false;//AA-920
  private boolean isCheckingCameraInSameNetwork = false;

  public CameraListArrayAdapter(MainActivity settingsActivity) {
    super(settingsActivity, R.layout.camera_list_item);
    // Clear old preview bitmap when initializing camera list.
    allDevices = DeviceSingleton.getInstance().getDevices();
    isSensor = settingsActivity.getDeviceType();
    if (isSensor) {
      for (Device device : allDevices) {
        filterDevices(isSensor, device);
      }
    }

    this.activity = settingsActivity;
    isOfflineMode = activity.isOfflineMode();

    ColorMatrix cm = new ColorMatrix();
    cm.setSaturation(0);
    grayScaleFilter = new ColorMatrixColorFilter(cm);
  }

  @Override
  public void notifyDataSetChanged() {
    isOfflineMode = activity.isOfflineMode();
    super.notifyDataSetChanged();
  }

  public synchronized void setDevices(final List<Device> devices) {
    mDevices.clear();
    /*
     * 20160129: HOANG: AA-1520
     */
    mShouldLoadPreviewMap.clear();
    for (Device aDevice : devices) {
      if (!mDevices.contains(aDevice)) {
        mDevices.add(aDevice);
        /*
         * 20160129: HOANG: AA-1520
         * Init flag for check whether preview cache image is loaded or not.
         */
        if (aDevice != null && aDevice.getProfile() != null && !TextUtils.isEmpty(aDevice.getProfile().getRegistrationId())) {
          mShouldLoadPreviewMap.put(aDevice.getProfile().getRegistrationId(), true);
        }
      }
    }
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    final ViewHolder mHolder;
//ARUNA
    /*This for implicit case isSensor wrong value*/
    if (mDevices.get(0).getProfile().getParentId() == null) {
      isSensor = false;
    }

    if (convertView == null) {
      final Device device = mDevices.get(position);
      String registrationId = null;
      if (device != null && device.getProfile() != null) {
        registrationId = device.getProfile().getRegistrationId();
      }
      if (!isSensor) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (HubbleApplication.isVtechApp()) {
          convertView = inflater.inflate(R.layout.camera_list_item_vtech, parent, false);
        } else {
          convertView = inflater.inflate(R.layout.camera_list_item, parent, false);
        }
        mHolder = new ViewHolder();
        // Update camera registration id for holder view
        mHolder.registrationId = registrationId;
        mHolder.btnSettings = (ImageButton) convertView.findViewById(R.id.list_row_camera_setting_camSettingBtn);
        mHolder.camImage = (de.hdodenhof.circleimageview.CircleImageView) convertView.findViewById(R.id.list_row_camera_setting_imageCamera);
        mHolder.cameraStatus = (CameraStatusView) convertView.findViewById(R.id.list_row_camera_ImageView_CameraStatus);
        mHolder.cameraName = (TextView) convertView.findViewById(R.id.textViewCameraName);
        mHolder.buttonBTA = (ImageButton) convertView.findViewById(R.id.list_row_camera_setting_btaButton);
        mHolder.imageURL = "";
        convertView.setTag(mHolder);
      } else {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.sensor_list_item, parent, false);
        mHolder = new ViewHolder();
        // Update camera registration id for holder view
        mHolder.registrationId = registrationId;
        mHolder.btnSettings = (ImageButton) convertView.findViewById(R.id.list_row_sensor_setting_buton);
        mHolder.camImage1 = (ImageView) convertView.findViewById(R.id.list_row_sensor_image);
        mHolder.cameraName = (TextView) convertView.findViewById(R.id.textViewSensorName);
        mHolder.cameraInfoSensor = (LinearLayout) convertView.findViewById(R.id.camera_info);
        mHolder.linkedCamera = (TextView) convertView.findViewById(R.id.textViewLinkedCameraName);
        mHolder.sensorStatus = (TextView) convertView.findViewById(R.id.textViewActiveState);
        mHolder.imgConnectedIcon = (ImageButton) convertView.findViewById(R.id.list_connected_icon);
        mHolder.imageURL = "";
        convertView.setTag(mHolder);
      }
    } else {
      mHolder = (ViewHolder) convertView.getTag();
      Device device = mDevices.get(position);
      String registrationId = null;
      if (device != null && device.getProfile() != null) {
        registrationId = device.getProfile().getRegistrationId();
      }

      if (!isSensor) {
        if (!TextUtils.isEmpty(registrationId) && !registrationId.equalsIgnoreCase(mHolder.registrationId)) {
          //Log.d(TAG, "Reuse itemview but camera changed, old " + mHolder.registrationId + ", new " + registrationId);
          mShouldLoadPreviewMap.put(registrationId, true);
        }
      }

      // Update camera registration id for holder view
      mHolder.registrationId = registrationId;
    }
    //  if (!isSensor) {
    mHolder.camImage.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isCheckingCameraInSameNetwork) {
          //Do nothing
          return;
        }
        Device device = mDevices.get(position);
        Log.i(TAG, "Camera image clicked: " + device.getProfile().getRegistrationId());
        Device cloneDevice = DeviceSingleton.getInstance().getDeviceByMAC(device.getProfile().getMacAddress());
        if (cloneDevice != null) {
          mDevices.set(position, cloneDevice);
        } else {
          cloneDevice = device;
        }
        // Make the boolean false once F86 cameras come into picture for proper validation
        boolean isF86Camera = false;
        String uuid = cloneDevice.getProfile().getRegistrationId();
        if (uuid.startsWith("010086"))
          isF86Camera = true;

        if (isSensor) {
          activity.goToEventLog(cloneDevice);
        } else if (cloneDevice.getProfile().isAvailable()) {
          boolean shouldContinue = true;
          if (isOfflineMode) {
            if (shouldContinue) {
              settings.putString(PublicDefineGlob.PREFS_LAST_CAMERA, (cloneDevice.getProfile().getMacAddress()));
              DeviceSingleton.getInstance().setSelectedDevice(cloneDevice);
              AsyncPackage.doInBackground(new CheckCameraInSameNetworkRunnable(activity.getApplicationContext(), cloneDevice));
            }
          }

          if (shouldContinue) {
            settings.putString(PublicDefineGlob.PREFS_LAST_CAMERA, (cloneDevice.getProfile().getMacAddress()));
            DeviceSingleton.getInstance().setSelectedDevice(cloneDevice);

            switchToCameraFragment(cloneDevice);
          }

        } else if (!isOfflineMode) {
          if (isF86Camera)
            activity.goToCameraEventLog(cloneDevice);
          else {
//            if (cloneDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
//              activity.goToRGBFragment(cloneDevice);
//            } else {
            activity.goToEventLog(cloneDevice);
//            }
          }
        }
      }
    });

    mHolder.camImage1.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isCheckingCameraInSameNetwork) {
          //Do nothing
          return;
        }
        Device device = mDevices.get(position);
        Log.i(TAG, "Camera image clicked: " + device.getProfile().getRegistrationId());
        Device cloneDevice = DeviceSingleton.getInstance().getDeviceByMAC(device.getProfile().getMacAddress());
        if (cloneDevice != null) {
          mDevices.set(position, cloneDevice);
        } else {
          cloneDevice = device;
        }
        // Make the boolean false once F86 cameras come into picture for proper validation
        boolean isF86Camera = false;
        String uuid = cloneDevice.getProfile().getRegistrationId();
        if (uuid.startsWith("010086"))
          isF86Camera = true;

        if (isSensor) {
          activity.goToEventLog(cloneDevice);
        } else if (cloneDevice.getProfile().isAvailable()) {
          boolean shouldContinue = true;
          if (isOfflineMode) {
            if (shouldContinue) {
              settings.putString(PublicDefineGlob.PREFS_LAST_CAMERA, (cloneDevice.getProfile().getMacAddress()));
              DeviceSingleton.getInstance().setSelectedDevice(cloneDevice);
              AsyncPackage.doInBackground(new CheckCameraInSameNetworkRunnable(activity.getApplicationContext(), cloneDevice));
            }
          }

          if (shouldContinue) {
            settings.putString(PublicDefineGlob.PREFS_LAST_CAMERA, (cloneDevice.getProfile().getMacAddress()));
            DeviceSingleton.getInstance().setSelectedDevice(cloneDevice);

            switchToCameraFragment(cloneDevice);
          }

        } else if (!isOfflineMode) {
          if (isF86Camera)
            activity.goToCameraEventLog(cloneDevice);
          else {
//            if (cloneDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
//              activity.goToRGBFragment(cloneDevice);
//            } else {
            activity.goToEventLog(cloneDevice);
//            }
          }
        }
      }
    });
    //    }

    mHolder.btnSettings.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isOfflineMode) {
          //Do nothing
          return;
        }
        Device device = mDevices.get(position);
        Log.i(TAG, "Camera setting image clicked: " + device.getProfile().getRegistrationId());
        Device cloneDevice = DeviceSingleton.getInstance().getDeviceByMAC(device.getProfile().getMacAddress());
        if (cloneDevice != null) {
          mDevices.set(position, cloneDevice);
        } else {
          cloneDevice = device;
        }

        /*if (isSensor) {
          if (cloneDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
            DeviceSingleton.getInstance().setSelectedDevice(cloneDevice);
            activity.switchToCameraSettingsFragment();
          } else {
            activity.switchToSensorDetailFragment(cloneDevice);
          }
        } else {
          if (DeviceSingleton.getInstance().isConnected()) {
            DeviceSingleton.getInstance().setSelectedDevice(cloneDevice);
            activity.switchToCameraSettingsFragment();
          }
        }*/
      }
    });

    final String imageLink = mDevices.get(position).getProfile().getSnapshotUrl();
    if (!isSensor) {
      Device device = mDevices.get(position);
      String registrationId = device.getProfile().getRegistrationId();
      // 20160127: HOANG: AA-1502
      // Just show latest preview snapshot if preview failed
      if (!P2pManager.getInstance().isPreviewSucceeded(registrationId) && Util.isLatestPreviewAvailable(registrationId)) {
        /*
         * 20160129: HOANG: AA-1520
         * Currently, app has to call notifyDataSetChanged to update jpeg image for preview mode.
         * This will reload the preview cache image multiple times -> the camera image view will blink.
         * Solution: app should add a flag that means preview cache image is loaded or not.
         * When user go to camera list, app should check this flag before loading the cache image.
         */
        Boolean shouldLoadCache = mShouldLoadPreviewMap.get(registrationId);
        if (shouldLoadCache != null && shouldLoadCache.booleanValue()) {
          //Log.d(TAG, "Latest preview for camera: " + registrationId + " is available, load it");
          loadLatestCameraPreview(registrationId, mHolder.camImage);
          // The preview cache image has been loaded, clear the flag now
          mShouldLoadPreviewMap.put(registrationId, false);
        }
      } else {
        if (imageLink != null && !imageLink.isEmpty() && !imageLink.contains("hubble.png")) {
          ImageLoader.getInstance().displayImage(imageLink, mHolder.camImage);
        } else {
          if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
            Picasso.with(activity).load(R.drawable.default_wide_cam).into(mHolder.camImage);
          } else {
            Picasso.with(activity).load(R.drawable.default_cam).into(mHolder.camImage);
          }
        }
      }

    } else {
      if (mDevices.get(position).getProfile().getMode().equals(SensorConstants.MOTION_DETECTION)) {
        Picasso.with(activity).load(R.drawable.iconbig_doormotion).into(mHolder.camImage);
      } else if (mDevices.get(position).getProfile().getMode().equals(SensorConstants.PRESENCE_DETECTION)) {
        Picasso.with(activity).load(R.drawable.iconbig_proximity).into(mHolder.camImage);
      } else {
        Picasso.with(activity).load(R.drawable.detail_opensensor).into(mHolder.camImage);
      }

      Log.i(TAG, "position: " + position + ", status: " + mDevices.get(position).getProfile().getStatus());
      //added non-null check
      if (mDevices.get(position).getProfile().getStatus() != null && mDevices.get(position).getProfile().getStatus().equals("0")) {
        mHolder.sensorStatus.setVisibility(View.VISIBLE);
        mHolder.cameraInfoSensor.setVisibility(View.GONE);
      } else {
        mHolder.sensorStatus.setVisibility(View.GONE);
        mHolder.cameraInfoSensor.setVisibility(View.VISIBLE);
        if (mDevices.get(position).getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
          mHolder.imgConnectedIcon.setVisibility(View.GONE);
          mHolder.linkedCamera.setVisibility(View.GONE);
        } else {
          mHolder.imgConnectedIcon.setVisibility(View.VISIBLE);
          mHolder.linkedCamera.setVisibility(View.VISIBLE);
          mHolder.linkedCamera.setText(mDevices.get(position).getProfile().getMode());
        }
      }
    }

    mHolder.cameraName.setText(mDevices.get(position).getProfile().getName());
    if (!BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      mHolder.cameraName.setSelected(true);
    } else {
      mHolder.cameraName.setFocusable(false);
      mHolder.cameraName.setSelected(false);
    }

    if (!mDevices.get(position).getProfile().isAvailable()) {
      if (BuildConfig.FLAVOR.equals("hubble") ||
          BuildConfig.FLAVOR.equals("hubblenew")) {
         mHolder.camImage.setColorFilter(grayScaleFilter);
      }
      if (!isSensor) {
        //mHolder.cameraStatus.setOnline(false);
        mHolder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
      }
    } else {
      // Check p2p status from p2p service first
      if (BuildConfig.FLAVOR.equals("hubble") ||
          BuildConfig.FLAVOR.equals("hubblenew")) {
        mHolder.camImage.clearColorFilter();
      }
      if (!isSensor) {
        mHolder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
      }
    }

    // invisible status view for sensor - temporary
    /* 20150909 HOANG AA-800 CRASH HERE!!!!
     * REMEMBER: Need to check whether this is sensor or camera before use
     * mHolder.cameraStatus or mHolder.sensorStatus reference.
     * Because mHolder.cameraStatus would be NULL for camera item and
     * mHolder.sensorStatus would be NULL for sensor item.
     */
    if (!isSensor) {
      if (mDevices.get(position).getProfile().getRegistrationId().startsWith("070004")) {
        // This is open sensor
        mHolder.cameraStatus.setVisibility(View.INVISIBLE);
      } else {
        mHolder.cameraStatus.setVisibility(View.VISIBLE);
      }
    }
    //end check sensor

    return convertView;
  }

  @Override
  public int getCount() {
    int count = 0;
    if (mDevices != null) {
      count = mDevices.size();
    } else {
      count = 0;
    }
    return count;
  }

  private void filterDevices(boolean isSensor, Device device) {
    if (isSensor) {
      Log.i(TAG, "is sensor device, parentId " + device.getProfile().getParentId());
      if (device.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR) || (device.getProfile().getParentId() != null && !device.getProfile().getParentId().equals(""))) {
        mDevices.add(device);
      }
    } else {
      if (!(device.getProfile().getParentId() != null && !device.getProfile().getParentId().equals(""))) {
        mDevices.add(device);
        /*
         * 20160129: HOANG: AA-1520
         * Init flag for check whether preview cache image is loaded or not.
         */
        mShouldLoadPreviewMap.put(device.getProfile().getRegistrationId(), true);
      }
    }
  }

  private void loadLatestCameraPreview(String registrationId, ImageView cameraImg) {
    if (Util.isLatestPreviewAvailable(registrationId)) {
      if (activity != null) {
        //Log.d(TAG, "Loading latest preview for camera: " + registrationId + ", path: " + Util.getLatestPreviewPath(registrationId));
        int placeHolderResId;
        if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
          placeHolderResId = R.drawable.default_wide_cam;
        } else {
          placeHolderResId = R.drawable.default_cam;
        }
        Picasso.with(activity)
            .load(new File(Util.getLatestPreviewPath(registrationId)))
            .placeholder(placeHolderResId)
            .skipMemoryCache()
            .noFade()
            .into(cameraImg);
      }
    } else {
      //Log.d(TAG, "Latest preview path for camera " + registrationId +  " is not available");
    }
  }

  private void switchToCameraFragment(Device device) {
    settings.putString(PublicDefineGlob.PREFS_LAST_CAMERA, (device.getProfile().getMacAddress()));
    DeviceSingleton.getInstance().setSelectedDevice(device);

    activity.switchToCameraFragment(device);
  }

  private static class ViewHolder {
    String registrationId;
    ImageButton btnSettings, imgConnectedIcon, buttonBTA;
    de.hdodenhof.circleimageview.CircleImageView camImage;
    ImageView camImage1;
    CameraStatusView cameraStatus;
    TextView cameraName;
    String imageURL;
    TextView linkedCamera, sensorStatus;
    LinearLayout cameraInfoSensor;
  }

  private class CheckCameraInSameNetworkRunnable implements Runnable {
    Context context;
    Device device;

    public CheckCameraInSameNetworkRunnable(Context context, Device device) {
      this.context = context;
      this.device = device;
    }

    @Override
    public void run() {
      isCheckingCameraInSameNetwork = true;
      CameraAvailabilityManager.getInstance().isCameraInSameNetworkAsync(context, device,
          new CameraAvailabilityManager.CameraAvailabilityManagerCallback() {
            @Override
            public void isCameraInSameNetwork(boolean isSameNetwork) {
              if (isSameNetwork) {
                switchToCameraFragment(device);
              }
              isCheckingCameraInSameNetwork = false;
            }
          });
    }
  }
}