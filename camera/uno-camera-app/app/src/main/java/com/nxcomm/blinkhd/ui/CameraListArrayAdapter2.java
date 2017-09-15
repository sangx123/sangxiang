package com.nxcomm.blinkhd.ui;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.common.collect.ImmutableMap;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceStatus;
import com.hubble.framework.service.cloudclient.device.pojo.response.EventResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.framework.service.p2p.P2pService;
import com.hubble.helpers.AsyncPackage;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubble.registration.tasks.CheckFirmwareUpdateTask;
import com.hubble.ui.BTAActivity;
import com.hubble.ui.DebugFragment;
import com.hubble.util.CommonConstants;
import com.hubble.util.ListChild;
import com.hubble.util.P2pSettingUtils;
import com.nest.thermostat.ThermostatMainViewHolder;
import com.nestlabs.sdk.Thermostat;
import com.nxcomm.blinkhd.actors.ActorMessage;
import com.nxcomm.blinkhd.actors.CameraSettingsActor;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.nxcomm.jstun_android.P2pClient;
import com.sensor.ui.AddSensorActivity;
import com.squareup.picasso.Picasso;
import com.util.AppEvents;
import com.util.ChangePrivacyMode;
import com.util.CommonUtil;
import com.util.DeviceWakeup;
import com.util.NotificationStatusTask;
import com.util.PrivacyCustomDialog;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import base.hubble.IAsyncTaskCommonHandler;
import base.hubble.PublicDefineGlob;
import base.hubble.database.DeviceStatusDetail;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

/**
 * Created by hoang on 3/8/16.
 */

public class CameraListArrayAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
  private SharedPreferences sharedPreferences;
  private static final String TAG = "CameraListArrayAdapter2";
  private final MainActivity activity;

  private final CameraListFragment cameraListContext;
  private LayoutInflater mInflater;
  ConcurrentMap<String, Integer> mDevicePositionMap = new ConcurrentHashMap<>();
  List<Device> mDevices = new ArrayList<Device>();
  List<Device> sensorDevices = new ArrayList<Device>();
  List<Device> allDevices = new ArrayList<Device>();
  ColorMatrixColorFilter grayScaleFilter;
  private boolean isSensor;
  private SecureConfig settings = HubbleApplication.AppConfig;
  private boolean isOfflineMode = false;//AA-920
  private boolean isCheckingCameraInSameNetwork = false;
  private String currentTemp = null;

  private boolean notificationEnabled = false;
    private boolean isDashBoardRefresh = false;
    CameraListViewHolder holder = null;


    private Handler mHandler;
    private ReentrantReadWriteLock mDeviceLock = new ReentrantReadWriteLock();
    private Map<String, EventResponse> mEventResponseMap = null;
    private boolean mIsEventResponseReceived = false;
    private EventData eventData;

  private static final int GET_DEVICE_STATUS_TASK_COMPLETED = 0;
  private DeviceWakeup mDeviceWakeup = null;

    private static final int THERMOSTAT = 1;
    private static final int CAMERA = 2;

    private ArrayList<Thermostat> thermostats;
    String privacy_mode = null;
    CameraSettingsActor settingsActor;
    //int position = -1;

    private SimpleDateFormat sdf = null;



  public CameraListArrayAdapter2(MainActivity settingsActivity, CameraListFragment cameraListContext) {
    sharedPreferences = settingsActivity.getSharedPreferences("app_config", Context.MODE_PRIVATE);
    this.activity = settingsActivity;
    this.cameraListContext = cameraListContext;
    mHandler = new Handler();
    mInflater = LayoutInflater.from(activity);
    eventData = new EventData();

    // Clear old preview bitmap when initializing camera list.
//    P2pManager.getInstance().clearPreviewBitmaps();

    fetchLatestDevices();

    ColorMatrix cm = new ColorMatrix();
    cm.setSaturation(0);
    grayScaleFilter = new ColorMatrixColorFilter(cm);
  }

  public void fetchLatestDevices(){
    mDevicePositionMap.clear();
    mDevices.clear();
    sensorDevices.clear();
    allDevices = DeviceSingleton.getInstance().getDevices();
    isSensor = activity.getDeviceType();
    // No need to filter devices any more as we are not showing 2 different tabs for sensors and cameras
    synchronized (this) {
      for (Device device : allDevices) {
        if (!mDevices.contains(device)) {
          filterDevices(isSensor, device);
        }
      }
      // mDevices = allDevices;
      buildDevicePositionMap(mDevices);
    }
    isOfflineMode = activity.isOfflineMode();

      if(CommonUtil.getSettingInfo(activity,SettingsPrefUtils.TIME_FORMAT_12,true)){
          sdf = new SimpleDateFormat("hh:mm aa, dd MMM, yyyy ");
      }else{
          sdf = new SimpleDateFormat("HH:mm, dd MMM, yyyy ");
      }
  }

  public void notifyPreviewChanged(String registrationId) {
    if (!TextUtils.isEmpty(registrationId)) {
      final int position = mDevicePositionMap.get(registrationId);
      Log.d(TAG, "notifyPreviewChanged: " + registrationId + ", position: " + position);
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          notifyItemChanged(position);
        }
      });
    } else {
      Log.d(TAG, "notifyPreviewChanged: registrationId is null");
    }
  }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;
        if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
            view = mInflater.inflate(R.layout.camera_list_item_vtech, parent, false);
        } else if (viewType == THERMOSTAT) {
            view = mInflater.inflate(R.layout.nest_thermostat_main_viewholder, parent, false);
        } else {
            if (isSensor) {
                view = mInflater.inflate(R.layout.sensor_list_item, parent, false);
            } else {
                view = mInflater.inflate(R.layout.camera_list_item, parent, false);
            }
        }
        if (viewType == THERMOSTAT) {
            viewHolder = new ThermostatMainViewHolder(view);
        } else {
            viewHolder = new CameraListViewHolder(view, isSensor);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder,  final int position) {

        final Device device = mDevices.get(position);
        String registrationId = null;

        if (device.getProfile().getName().contains("Thermostat")) {
            ThermostatMainViewHolder thermostatViewHolder = (ThermostatMainViewHolder) viewHolder;
            for(int i=0; i < thermostats.size(); i++){
                if(device.getProfile().getRegistrationId().equals(thermostats.get(i).getDeviceId())){
                    thermostatViewHolder.bindToDevice(thermostats.get(i));
                    break;
                }
            }
            return;
        }
        if (device != null && device.getProfile() != null) {
            registrationId = device.getProfile().getRegistrationId();

        }
        //Load event name , time and camera Image from event response.
        holder = (CameraListViewHolder) viewHolder;
        boolean isImageLoaded = false;
        boolean isEventLoaded = false;
        if (mIsEventResponseReceived) {
            EventResponse eventResponse = mEventResponseMap.get(registrationId);
            if (eventResponse != null) {
                isEventLoaded = true;
                String eventName = CommonUtil.getEventString(eventResponse, activity.getApplicationContext());
                holder.eventName.setText(eventName);
                Util.setDashBoardEventNameToSP(activity.getApplicationContext(), registrationId, eventName);

                String time=CommonUtil.getTimeStampFromTimeZone(eventResponse.getEventTime(device.getProfile().getTimeZone()),
                       device.getProfile().getTimeZone(),sdf);

                if (time != null) {
                    holder.eventTime.setText(activity.getResources().getString(R.string.at)+ " " +time);
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                String timeToSave = simpleDateFormat.format(eventResponse.getEventTime(device.getProfile().getTimeZone()));
                Util.setDashBoardEventTimeToSP(activity.getApplicationContext(), registrationId, timeToSave);
                EventResponse.EventData[] eventDataList = eventResponse.getEventDataList();
                if (eventDataList != null && eventDataList.length > 0) {
                    String eventImageURL = device.getProfile().getSnapshotUrl();//eventDataList[0].getImage();
                    if(CommonUtil.checkSettings(activity.getApplicationContext(), registrationId + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE)) {
                        if (CommonUtil.getSettingInfo(activity.getApplicationContext(), registrationId + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE)) {
                            eventImageURL = eventDataList[0].getImage();
                        }
                    }else{
                        eventImageURL = eventDataList[0].getImage();
                    }
                    if (eventImageURL != null) {
                        Log.d(TAG, "Updating dashboard for" + registrationId + " image with url " + eventImageURL);
                        isImageLoaded = true;
                        final String regId = registrationId;
                      /*  Picasso.with(activity.getApplicationContext()).
                                load(eventImageURL).
                                placeholder(getLastAvailableImage(holder.camImage, registrationId)).
                                resize(100, 100).
                                error(R.drawable.default_cam).
                                into(holder.camImage);*/

                        if (eventImageURL != null) {

                            Picasso.with(activity.getApplicationContext()).
                                    load(eventImageURL).
                                    placeholder(getLastAvailableImage(holder.camImage, registrationId)).
                                    resize(100, 100).
                                    error(R.drawable.default_cam).
                                    into(holder.camImage);


                            com.squareup.picasso.Target target = new com.squareup.picasso.Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    Log.d(TAG, "Image saved to file for " + device.getProfile().getRegistrationId());
                                    saveBitmapToFile(device.getProfile().getRegistrationId(), bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                }
                            };
                            if (activity != null) {


                                Picasso.with(activity.getApplicationContext()).
                                        load(eventImageURL).
                                        resize(100, 100).
                                        into(target);
                            }
                        }
                    }

        }
      }
    }

    if (!isImageLoaded) {
        if(CommonUtil.checkSettings(activity.getApplicationContext(), registrationId + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE)) {
            if (!CommonUtil.getSettingInfo(activity.getApplicationContext(), registrationId + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE)) {

                String eventImageURL = device.getProfile().getSnapshotUrl();//eventDataList[0].getImage();




                if (eventImageURL != null) {

                    Picasso.with(activity.getApplicationContext()).
                            load(eventImageURL).
                            placeholder(getLastAvailableImage(holder.camImage, registrationId)).
                            resize(100, 100).
                            error(R.drawable.default_cam).
                            into(holder.camImage);

                    com.squareup.picasso.Target target = new com.squareup.picasso.Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Log.d(TAG, "Image saved to file for " + device.getProfile().getRegistrationId());
                            saveBitmapToFile(device.getProfile().getRegistrationId(), bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    };


                    if (activity != null) {



                        Picasso.with(activity.getApplicationContext()).
                                load(eventImageURL).
                                resize(100, 100).
                                into(target);


                    }
                }
            }else{
                holder.camImage.setImageDrawable(getLastAvailableImage(holder.camImage, registrationId));
            }
        }else {
              holder.camImage.setImageDrawable(getLastAvailableImage(holder.camImage, registrationId));
        }
    }
    if (!isEventLoaded ||  holder.eventName.getText().toString().isEmpty()) {
      String text = Util.getDashBoardEventNameFromSP(activity.getApplicationContext(), registrationId);
      if (!text.isEmpty()) {
        holder.eventName.setText(text);

          String time1 = Util.getDashBoardEventTimeFromSP(activity.getApplicationContext(), registrationId);
          if (!TextUtils.isEmpty(time1)) {
              if (time1.startsWith("at"))
                  time1 = (time1.substring(3, time1.length())).trim();
              try{
                  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                  Date date = simpleDateFormat.parse(time1);
                  String time = CommonUtil.getTimeStampFromTimeZone(date, device.getProfile().getTimeZone(), sdf);
                  if (time != null) {
                      holder.eventTime.setText(activity.getResources().getString(R.string.at)+ " " +time);
                  }
              }catch(ParseException e){
                  Log.e(TAG,"Date parsing failed while reading from shared pref.Format doesn't match");
              }

          }
      } else {
        holder.eventName.setText(activity.getApplicationContext().getString(R.string.movement_detection_text));
        holder.eventTime.setText(activity.getApplicationContext().getString(R.string.movement_detection_text_line2));
      }

    }
    // Update camera registration id for holder view
    holder.registrationId = registrationId;
    // if device is 0877 camera, show BTA button, otherwise hide it
    if (holder.buttonBTA != null) {
      if (device.getProfile().getRegistrationId().startsWith("010877")) {
        Log.i(TAG, "Camera 0877, show BTA button");
        holder.buttonBTA.setVisibility(View.VISIBLE);
        holder.buttonBTA.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              if(device.getProfile().isAvailable()) {
                  Log.i(TAG, "BTA button clicked on device " + device.getProfile().getRegistrationId());
                  Intent intent = new Intent(activity, BTAActivity.class);
                  intent.putExtra(PublicDefine.PREFS_SAVED_PORTAL_TOKEN, HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null));
                  intent.putExtra(PublicDefine.DEVICE_REG_ID, device.getProfile().getRegistrationId());
                  activity.startActivity(intent);
              }else{
                  Toast.makeText(activity,"Camera is offline", Toast.LENGTH_LONG).show();
              }
          }
        });
      } else {
        holder.buttonBTA.setVisibility(View.GONE);
      }
    } else {
      Log.e(TAG, "Button BTA is null");
    }
        privacy_mode = device.getProfile().getDeviceAttributes().getPrivacyMode();
    holder.llMotionLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          privacy_mode = device.getProfile().getDeviceAttributes().getPrivacyMode();
        if((privacy_mode == null) || (!TextUtils.isEmpty(privacy_mode) && privacy_mode.equalsIgnoreCase("0")))
           openViewFinder(position);
      }
    });
    holder.llTemparatureAndHumidityLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          privacy_mode = device.getProfile().getDeviceAttributes().getPrivacyMode();
        if((privacy_mode == null) || (!TextUtils.isEmpty(privacy_mode) && privacy_mode.equalsIgnoreCase("0")))
            openViewFinder(position);
      }
    });
    holder.layoutImageCamera.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          privacy_mode = device.getProfile().getDeviceAttributes().getPrivacyMode();
        if((privacy_mode == null) || (!TextUtils.isEmpty(privacy_mode) && privacy_mode.equalsIgnoreCase("0")))
            openViewFinder(position);
      }
    });
    holder.camImage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          privacy_mode = device.getProfile().getDeviceAttributes().getPrivacyMode();
        if((privacy_mode == null) || (!TextUtils.isEmpty(privacy_mode) && privacy_mode.equalsIgnoreCase("0")))
            openViewFinder(position);
      }
    });



  //  if(sharedPreferences.getBoolean(device.getProfile().getName()+"notification", true)) {
      if((privacy_mode == null) || (!TextUtils.isEmpty(privacy_mode) && privacy_mode.equalsIgnoreCase("0"))){

          holder.notificationSwitch.setOnCheckedChangeListener(null);
          holder.notificationSwitch.setChecked(true);
          holder.notificationSwitch.setEnabled(true);
          holder.privacyModeLayout.setVisibility(View.GONE);
      }
    else
    {
      //holder.notificationSwitch.setClickable(false);
        holder.notificationSwitch.setOnCheckedChangeListener(null);
        holder.privacySwitch.setOnCheckedChangeListener(null);
        holder.notificationSwitch.setChecked(false);
        holder.notificationSwitch.setEnabled(false);
        holder.privacyModeLayout.setVisibility(View.VISIBLE);
        holder.privacyCameraName.setText(device.getProfile().getName());
        holder.privacySwitch.setChecked(false);
        holder.privacyModeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewFinder( position);
            }
        });
    }

        if (device.getProfile().isDeviceBatteryOperated() && device.getProfile().isAvailable()) {
            holder.batteryLayout.setVisibility(View.VISIBLE);
            if (mDevices != null) {
                showBatteryStatus(position);
            }
        } else {
            holder.batteryLayout.setVisibility(View.GONE);
        }

    if(device.getProfile().doesSupportBleTag()){
      holder.focusTagLayout.setVisibility(View.VISIBLE);
    }else{
      holder.focusTagLayout.setVisibility(View.GONE);
    }

       /* check if the received device is Focus Tag, find the parent possition and then assign the Focus tag to parent view*/
    if(device.getProfile().doesSupportBleTag() && sharedPreferences.getBoolean("isSensorPrtesent", false)){
      int numberOfTags = -1;
      List<Device> latestSensorList =  new ArrayList<>();

      for (int i = 0 ; i < sensorDevices.size(); i++) {
        Device aDevice = sensorDevices.get(i);
        if (device.getProfile().getRegistrationId().equalsIgnoreCase(aDevice.getProfile().getParentRegistrationId())) {
          numberOfTags ++;
           switch (numberOfTags) {
             case 0:
               holder.addedFocusTagLayout1.setVisibility(View.VISIBLE);
               holder.addedFocusTagLayout1.setTag(aDevice.getProfile().getRegistrationId());
               holder.addedTagName1.setText(shortenTagName(aDevice.getProfile().getName()));//aDevice.getProfile().getName());
               holder.addFocusTagLayout.setVisibility(View.VISIBLE);

               holder.addedFocusTagLayout2.setVisibility(View.GONE);
               holder.addedFocusTagLayout3.setVisibility(View.GONE);
               break;
             case 1:
               holder.addedFocusTagLayout2.setVisibility(View.VISIBLE);
               holder.addedFocusTagLayout2.setTag(aDevice.getProfile().getRegistrationId());
               holder.addedTagName2.setText(shortenTagName(aDevice.getProfile().getName()));//aDevice.getProfile().getName());
               holder.addFocusTagLayout.setVisibility(View.VISIBLE);
               holder.addedFocusTagLayout1.setVisibility(View.VISIBLE);
               holder.addedFocusTagLayout3.setVisibility(View.GONE);

               break;
             case 2:
               holder.addedFocusTagLayout3.setVisibility(View.VISIBLE);
               holder.addedFocusTagLayout3.setTag(aDevice.getProfile().getRegistrationId());
               holder.addedTagName3.setText(shortenTagName(aDevice.getProfile().getName()));//aDevice.getProfile().getName());
               holder.addFocusTagLayout.setVisibility(View.GONE);

               holder.addedFocusTagLayout1.setVisibility(View.VISIBLE);
               holder.addedFocusTagLayout2.setVisibility(View.VISIBLE);
               break;
           }

        }

      }
    }else{
      holder.addedFocusTagLayout1.setVisibility(View.GONE);
      holder.addedFocusTagLayout2.setVisibility(View.GONE);
      holder.addedFocusTagLayout3.setVisibility(View.GONE);
    }

    holder.addedFocusTagLayout1.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {
          String regId = v.getTag().toString();
          Device selectedFocusTag = DeviceSingleton.getInstance().getDeviceByRegId(regId);
          activity.switchToSensorDetailFragment(selectedFocusTag);
      }
    });

    holder.addedFocusTagLayout2.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {
        String regId = v.getTag().toString();
        Device selectedFocusTag = DeviceSingleton.getInstance().getDeviceByRegId(regId);
        activity.switchToSensorDetailFragment(selectedFocusTag);
      }
    });

    holder.addedFocusTagLayout3.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {
        String regId = v.getTag().toString();
        Device selectedFocusTag = DeviceSingleton.getInstance().getDeviceByRegId(regId);
        activity.switchToSensorDetailFragment(selectedFocusTag);
      }
    });

    holder.addFocusTag.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          if(position >= mDevices.size()){
              return;
          }
          GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD, AppEvents.TAG_BUTTON_CLICKED, AppEvents.TAG_CLICKED);
          ZaiusEvent tagEvt = new ZaiusEvent(AppEvents.DASHBOARD);
          tagEvt.action(AppEvents.TAG_BUTTON_CLICKED);
          try {
              ZaiusEventManager.getInstance().trackCustomEvent(tagEvt);
          } catch (ZaiusException e) {
              e.printStackTrace();
          }

        DeviceSingleton.getInstance().setSelectedDevice(mDevices.get(position));
        Intent intent = new Intent(activity.getApplicationContext(), AddSensorActivity.class);
        intent.putExtra("isCameraDetail", true);
        cameraListContext.startActivityForResult(intent, PublicDefineGlob.REGISTER_SENSOR_ACTIVITY_REQUEST);
      }
    });

        holder.privacySwitch.setTag(position);
        holder.privacySwitch.setOnCheckedChangeListener(privacyChangeListner);

        holder.notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if(position >= mDevices.size()){
                    return;
                }
                isDashBoardRefresh = false;
                if (!CommonUtil.getSettingInfo(activity, mDevices.get(position).getProfile().getRegistrationId() + "-" + SettingsPrefUtils.DONT_SHOW_PRIVACY_DIALOG)) {
                    PrivacyCustomDialog privacyDailog = new PrivacyCustomDialog(activity, new PrivacyCustomDialog.PrivacyListener() {
                        @Override
                        public void onPrivacyConfirmClick() {
                            if (!isChecked) {
                                if(position >= mDevices.size()){
                                    return;
                                }
                                AnalyticsInterface.getInstance().trackEvent(AppEvents.DEVICE_NOTIFICATION, AppEvents.DEVICE_NOTIFICATION_OFF, eventData);
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD, AppEvents.CAMERA_PRIVACY_ENABLED,AppEvents.CAMERA_PRIVACY_ENABLED);
                                ZaiusEvent cameraPrivacyEnableEvt = new ZaiusEvent(AppEvents.DASHBOARD);
                                cameraPrivacyEnableEvt.action(AppEvents.CAMERA_PRIVACY_ENABLED);
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(cameraPrivacyEnableEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }

                                if (mDevices.get(position).getProfile().isStandBySupported()) {
                                    notificationEnabled = false;
                                    checkDeviceStatus(mDevices.get(position), position, holder.notificationSwitch.isChecked());

                                } else {
                                    displayProgressDialog(activity.getResources().getString(R.string.communicate_with_camera));
                                    NotificationStatusTask notificationChange = new NotificationStatusTask(mDevices.get(position), position, false, activity, mDeviceHandler);
                                    notificationChange.execute();
                                }
                               /* ChangePrivacyMode changePrivacyMode = new ChangePrivacyMode(mDevices.get(position), position, true, activity, mDeviceHandler);
                                changePrivacyMode.execute();*/

                                // holder.notificationSwitchON.setTextColor(activity.getResources().getColor(R.color.text_gray));
                            }
                        }

                        @Override
                        public void doNotShowDailog(boolean isChecked) {
                            if(position >= mDevices.size()){
                                return;
                            }
                            CommonUtil.setSettingInfo(activity, mDevices.get(position).getProfile().getRegistrationId() + "-" + SettingsPrefUtils.DONT_SHOW_PRIVACY_DIALOG, isChecked);
                        }

                        @Override
                        public void onPrivacyCancel() {
                            //if (!holder.notificationSwitch.isChecked()) {
                            if(position >= mDevices.size()){
                                return;
                            }
                            notifyItemChange(position);
                            notifyDataSetChanged();
                            //}
                        }
                    });
                    privacyDailog.show();
                } else {

                    if ((privacy_mode == null) || (!TextUtils.isEmpty(privacy_mode) && privacy_mode.equalsIgnoreCase("0"))) {


                        if (holder.notificationSwitch.isChecked()) {
                            if(position >= mDevices.size()){
                                return;
                            }
                            AnalyticsInterface.getInstance().trackEvent(AppEvents.DEVICE_NOTIFICATION, AppEvents.DEVICE_NOTIFICATION_ON, eventData);
                            if (mDevices.get(position).getProfile().isStandBySupported()) {
                                notificationEnabled = true;
                                checkDeviceStatus(mDevices.get(position), position, holder.notificationSwitch.isChecked());

                            } else {
                                displayProgressDialog(activity.getResources().getString(R.string.communicate_with_camera));
                                NotificationStatusTask notificationChange = new NotificationStatusTask(mDevices.get(position), position, true, activity, mDeviceHandler);
                                notificationChange.execute();


                            }
                          /*  ChangePrivacyMode changePrivacyMode = new ChangePrivacyMode(mDevices.get(position), position, false, activity, mDeviceHandler);
                            changePrivacyMode.execute();*/

                            //holder.notificationSwitchON.setTextColor(activity.getResources().getColor(R.color.text_blue));
                        } else {
                            if(position >= mDevices.size()){
                                return;
                            }
                            AnalyticsInterface.getInstance().trackEvent(AppEvents.DEVICE_NOTIFICATION, AppEvents.DEVICE_NOTIFICATION_OFF, eventData);
                            if (mDevices.get(position).getProfile().isStandBySupported()) {
                                notificationEnabled = false;
                                checkDeviceStatus(mDevices.get(position), position, holder.notificationSwitch.isChecked());

                            } else {
                                displayProgressDialog(activity.getResources().getString(R.string.communicate_with_camera));
                                NotificationStatusTask notificationChange = new NotificationStatusTask(mDevices.get(position), position, false, activity, mDeviceHandler);
                                notificationChange.execute();
                            }
                           /* ChangePrivacyMode changePrivacyMode = new ChangePrivacyMode(mDevices.get(position), position, true, activity, mDeviceHandler);
                            changePrivacyMode.execute();*/

                            // holder.notificationSwitchON.setTextColor(activity.getResources().getColor(R.color.text_gray));
                        }
                    }
                }
            }
        });
    holder.btnSettings.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          if(position >= mDevices.size()){
              return;
          }
        if ((privacy_mode == null) || (!TextUtils.isEmpty(privacy_mode) && privacy_mode.equalsIgnoreCase("0"))) {

            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,AppEvents.DASHBOARD_SETTINGS_CLICKED,AppEvents.DASHBOARD_SETTINGS_CLICKED);

            ZaiusEvent dashboardSettingsEvt = new ZaiusEvent(AppEvents.DASHBOARD);
            dashboardSettingsEvt.action(AppEvents.DASHBOARD_SETTINGS_CLICKED);
          try {
            ZaiusEventManager.getInstance().trackCustomEvent(dashboardSettingsEvt);
          } catch (ZaiusException e) {
            e.printStackTrace();
          }

          long settingsClickedTime = System.currentTimeMillis();
          settings.putLong(CommonConstants.SETTINGS_CLICKED_TIME,settingsClickedTime);

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

          if (isSensor) {
            if (cloneDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
              DeviceSingleton.getInstance().setSelectedDevice(cloneDevice);
              activity.switchToCameraSettingsActivity();
            } else {
              activity.switchToSensorDetailFragment(cloneDevice);
            }
          } else {
            if (DeviceSingleton.getInstance().isConnected()) {
              DeviceSingleton.getInstance().setSelectedDevice(cloneDevice);
              activity.switchToCameraSettingsActivity();
            }else{
                Toast.makeText(activity, activity.getString(R.string.enable_internet_connection), Toast.LENGTH_LONG).show();
            }
          }
        }
      }
    });
        if (position >= mDevices.size()) {
            return;
        }
    final String imageLink = mDevices.get(position).getProfile().getSnapshotUrl();
    if (!isSensor) {

  if (mDevices.get(position).getProfile().doesHaveTemperature()) {

    if (sharedPreferences.contains(mDevices.get(position).getProfile().getRegistrationId() + "camera_temperature")) {

      holder.currentTemperature.setVisibility(View.VISIBLE);
      if (mDevices.get(position).getProfile().isAvailable()) {
          String subText = null;
          String mainText = null;
          int tempValue = sharedPreferences.getInt(mDevices.get(position).getProfile().getRegistrationId() + "camera_temperature", 0);
          int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
          if (savedTempUnit == PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
              mainText = Math.round(tempValue) + "";
              subText = "\u2103";
          } else {
              mainText = Math.round(CommonUtil.convertCtoF(tempValue)) + "";
              subText = "\u2109";
          }
          holder.currentTemperature.setText(mainText + subText);
      }
      else
        holder.currentTemperature.setText("---");
      Log.i(TAG, " Device and Temp" + mDevices.get(position).getProfile().getRegistrationId() + sharedPreferences.getString(mDevices.get(position).getProfile().getName() + "temp", "0"));
    } else {
      holder.currentTemperature.setText("---");
    }
  }else{
    holder.temperatureLayout.setVisibility(View.GONE);
  }
/* TODO : Enable below commented code when any camera supports humidity : connect 7 camera */
  if (mDevices.get(position).getProfile().doesHaveHumidity()) {
    if (sharedPreferences.contains(mDevices.get(position).getProfile().getRegistrationId() + "humidity")) {
      holder.humidityLayout.setVisibility(View.VISIBLE);
      if (mDevices.get(position).getProfile().isAvailable())
        holder.currentHumidity.setText((sharedPreferences.getString(mDevices.get(position).getProfile().getRegistrationId() + "humidity", "0")) + "%");
      else
        holder.currentHumidity.setText("---");
      Log.i(TAG, " HUMIDITY Device : " + mDevices.get(position).getProfile().getRegistrationId() + sharedPreferences.getString(mDevices.get(position).getProfile().getName(), "0").toString());
    } else {
      holder.currentHumidity.setText("---");
    }
  } else {
    holder.humidityLayout.setVisibility(View.GONE);
  }


// 20160127: HOANG: AA-1502
      // Just show latest preview snapshot if preview failed
      /*if (Util.isLatestPreviewAvailable(registrationId)) {
        /*
         * 20160129: HOANG: AA-1520
         * Currently, app has to call notifyDataSetChanged to update jpeg image for preview mode.
         * This will reload the preview cache image multiple times -> the camera image view will blink.
         * Solution: app should add a flag that means preview cache image is loaded or not.
         * When user go to camera list, app should check this flag before loading the cache image.
         */
        //Log.d(TAG, "Latest preview for camera: " + registrationId + " is available, load it");
       // loadLatestCameraPreview(registrationId, holder.camImage);

      /*} else {
        if (imageLink != null && !imageLink.isEmpty() && !imageLink.contains("hubble.png")) {
          ImageLoader.getInstance().displayImage(imageLink, holder.camImage);
        } else {
          if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
            Picasso.with(activity).load(R.drawable.default_wide_cam).into(holder.camImage);
          } else {
            Picasso.with(activity).load(R.drawable.default_cam).into(holder.camImage);
          }
        }
      }*/


    } else {
      /*if (mDevices.get(position).getProfile().getMode().equals(SensorConstants.MOTION_DETECTION)) {
        Picasso.with(activity).load(R.drawable.iconbig_doormotion).into(holder.camImage);
      } else if (mDevices.get(position).getProfile().getMode().equals(SensorConstants.PRESENCE_DETECTION)) {
        Picasso.with(activity).load(R.drawable.iconbig_proximity).into(holder.camImage);
      } else {
        Picasso.with(activity).load(R.drawable.detail_opensensor).into(holder.camImage);
      }*/


      Log.i(TAG, "position: " + position + ", status: " + mDevices.get(position).getProfile().getStatus());
      //added non-null check
      if (mDevices.get(position).getProfile().getStatus() != null && mDevices.get(position).getProfile().getStatus().equals("0")) {
        holder.sensorStatus.setVisibility(View.VISIBLE);
        holder.cameraInfoSensor.setVisibility(View.GONE);
      } else {
        holder.sensorStatus.setVisibility(View.GONE);
        holder.cameraInfoSensor.setVisibility(View.VISIBLE);
        if (mDevices.get(position).getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
          holder.imgConnectedIcon.setVisibility(View.GONE);
          holder.linkedCamera.setVisibility(View.GONE);
        } else {
          holder.imgConnectedIcon.setVisibility(View.VISIBLE);
          holder.linkedCamera.setVisibility(View.VISIBLE);
          holder.linkedCamera.setText(mDevices.get(position).getProfile().getMode());
        }
      }
    }

    holder.cameraName.setText(mDevices.get(position).getProfile().getName());
    if (!BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      holder.cameraName.setSelected(true);
    } else {
      holder.cameraName.setFocusable(false);
      holder.cameraName.setSelected(false);
    }

    if(mDevices.get(position).getProfile().doesSupportSDCardAccess())
    {
      float freeStorage = -2, totalStorage = 0;

      if (sharedPreferences.contains(mDevices.get(position).getProfile().getRegistrationId()+PublicDefine.SDCARD_FREE_SPACE)) {
             int responseValue = sharedPreferences.getInt(mDevices.get(position).getProfile().getRegistrationId()+PublicDefine.SDCARD_FREE_SPACE, 0);
          freeStorage = Float.valueOf(responseValue);

      }


      if(BuildConfig.DEBUG)
        Log.d(TAG,"freeStorage :-  " + freeStorage);
      // receive free storage details
      if(freeStorage != -2 && freeStorage != -1)
      {
        holder.sdcardLayout.setVisibility(View.VISIBLE);
        holder.sdcardImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.s_dcard));
        DecimalFormat precision = new DecimalFormat("#.##");
        // that's means that it is orbit device
          double gb = freeStorage/1024;
          if(gb > 1){
              holder.sdcardStatusTv.setText(String.format(activity.getResources().getString(R.string.orbit_free_storage),precision.format((gb))));

          }else{
              holder.sdcardStatusTv.setText(String.format(activity.getResources().getString(R.string.free_storage_MB),precision.format((freeStorage))));

          }

      }
      else if(freeStorage == -1.0 || freeStorage == -2.0)
      {
        holder.sdcardLayout.setVisibility(View.VISIBLE);
        holder.sdcardStatusTv.setText(String.format(activity.getResources().getString(R.string.no_card)));
        holder.sdcardImageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.no_sdcard));
      }
    }else{
        holder.sdcardLayout.setVisibility(View.GONE);
    }
        if(position >= mDevices.size()){
            return;
        }
    if(mDevices.get(position).getProfile().isStandBySupported())
    {
      final Device standBySupportedDevice = mDevices.get(position);

      holder.cameraStatus.setVisibility(View.INVISIBLE);
      /*
      if(mDevices.get(position).getProfile().isAvailable())
      {
        holder.camImage.clearColorFilter();

        holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
        holder.notificationSwitch.setChecked(true);
        holder.notificationSwitch.setClickable(true);

        holder.notificationSwitchON.setTextColor(activity.getResources().getColor(R.color.text_blue));
      }
      else
      {
        holder.camImage.setColorFilter(grayScaleFilter);
        holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
      }
      */

      if (BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equals("hubblenew"))
      {
        DeviceStatusDetail deviceStatusDetail = standBySupportedDevice.getProfile().getDeviceStatusDetail();


        if(deviceStatusDetail != null)
        {
          holder.cameraStatus.setVisibility(View.VISIBLE);
          String deviceStatus = deviceStatusDetail.getDeviceStatus();

            if(BuildConfig.DEBUG)
                Log.d(TAG,"Live Status :- " + mDevices.get(position).getProfile().isLiveStatus());
          if(deviceStatus != null && mDevices.get(position).getProfile().isLiveStatus())
          {
            if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0)
            {
                P2pClient selectedP2pClient = P2pService.getP2pClient(mDevices.get(position).getProfile().getRegistrationId());
                boolean existInP2pList = selectedP2pClient != null;

                //boolean isOrbitP2PEnabled =  !mDevices.get(position).getProfile().isStandBySupported() || HubbleApplication.AppConfig.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);

                if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() &&
                        mDevices.get(position).getProfile().canUseP2p() && mDevices.get(position).getProfile().canUseP2pRelay()
                        && existInP2pList)
                {
                    if (selectedP2pClient.isValid())
                    {
                        Log.i(TAG, " camera  : " + mDevices.get(position).getProfile().getName() + " Status : " + "Online");
                        holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
                    }
                    else
                    {
                        Log.i(TAG, " camera  : " + mDevices.get(position).getProfile().getName() + " Status : " + "Connecting");
                        holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_CONNECTING);
                    }
                }
                else {
                    Log.i(TAG, "always on camera  : " +mDevices.get(position).getProfile().getName() + " Status : " + "Online");
                    holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
                }


                mDevices.get(position).getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);

                holder.camImage.clearColorFilter();
                holder.notificationLayout.setVisibility(View.VISIBLE);

                if (CommonUtil.checkSettings(activity, mDevices.get(position).getProfile().getRegistrationId() + "-" + SettingsPrefUtils.FETCH_SETTINGS)) {

                    if (CommonUtil.getSettingInfo(activity, mDevices.get(position).getProfile().getRegistrationId() + "-" + SettingsPrefUtils.FETCH_SETTINGS)) {
                        Log.i(TAG, "Calling settings info");

                        fetchCameraSettings(mDevices.get(position));
                    }
                } else {
                    fetchCameraSettings(mDevices.get(position));
                }

//              holder.notificationSwitch.setChecked(true);
//              holder.notificationSwitch.setClickable(true);

//              holder.notificationSwitchON.setTextColor(activity.getResources().getColor(R.color.text_blue));
            }
            else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_STANDBY) == 0)
            {
              holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);
                if(position >= mDevices.size()){
                    return;
                }

              mDevices.get(position).getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);

              holder.camImage.setColorFilter(grayScaleFilter);
              holder.notificationLayout.setVisibility(View.VISIBLE);
              isDashBoardRefresh = true;
              wakeUpRemoteDevice(position);
			}
            else
            {
              holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
                if(position >= mDevices.size()){
                    return;
                }
              mDevices.get(position).getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);

              holder.camImage.setColorFilter(grayScaleFilter);
              holder.notificationLayout.setVisibility(View.GONE);

            }

            float batteryLevel = deviceStatusDetail.getBatteryLevel();
            int batteryMode = -1;
              if(position >= mDevices.size()){
                  return;
              }
            if (sharedPreferences.contains(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_DEVICE_MODE)))
            {
                String responseValueStr  = sharedPreferences.getString(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_DEVICE_MODE),null);
                if(responseValueStr != null)
                {
                    Pair<Long,String> responseVal = PublicDefine.getSharedPrefValue(responseValueStr);
                    if(responseVal != null)
                    {
                        try {
                            batteryMode = Integer.parseInt(responseVal.second);
                            Log.d(TAG,"batteryMode :- " + batteryMode);
                        }
                        catch (Exception e)
                        {
                            Log.d(TAG,e.getMessage());
                        }
                    }
                }


              if (sharedPreferences.contains(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_BATTERY_VALUE)))
              {
                  responseValueStr  = sharedPreferences.getString(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_BATTERY_VALUE),null);
                  if(responseValueStr != null)
                  {
                      Pair<Long,String> responseVal = PublicDefine.getSharedPrefValue(responseValueStr);
                      if(responseVal != null)
                      {
                          try {
                              batteryLevel = Integer.parseInt(responseVal.second);
                              Log.d(TAG,"batteryLevel :- " + batteryLevel);
                          }
                          catch (Exception e){Log.d(TAG,e.getMessage());}
                      }
                  }
              }
            }

            if(batteryMode == CameraStatusView.ORBIT_BATTERY_CHARGING)
            {
              holder.batteryLayout.setVisibility(View.VISIBLE);

              holder.batteryStatusTv.setText(activity.getResources().getString(R.string.battery_status_percentage, (int) batteryLevel));

              holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_charging);
            }
            else
            {
              if (batteryLevel != 0.0 && batteryLevel != 999)
              {
                holder.batteryLayout.setVisibility(View.VISIBLE);

                holder.batteryStatusTv.setText(activity.getResources().getString(R.string.battery_status_percentage, (int) batteryLevel));

                if (batteryLevel >= 1.0 && batteryLevel <= 9.9)
                  holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_empty);
                else if (batteryLevel >= 10.0 && batteryLevel <= 24.9)
                  holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_low);
                else if (batteryLevel >= 25 && batteryLevel <= 74.9)
                  holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_half);
                else if (batteryLevel >= 75 && batteryLevel <= 100)
                  holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_full);
              }
              else if(batteryLevel == 999 || batteryLevel == 0.0)
              {
                holder.batteryLayout.setVisibility(View.VISIBLE);
                holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_charging);
              }
            }
          }
          else
          {

              // show updating status when application is getting data from server
              holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_UPDATING);
              mDevices.get(position).getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_UPDATING);
          }
        }
        else
        {

            holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_UPDATING);
            mDevices.get(position).getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_UPDATING);
          final DeviceStatus deviceStatus = new DeviceStatus(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
                  standBySupportedDevice.getProfile().getRegistrationId());


          DeviceManagerService.getInstance(activity).getDeviceStatus(deviceStatus, new Response.Listener<StatusDetails>()
          {
                    @Override
                    public void onResponse (StatusDetails response)
                    {
                      boolean isDeviceStatusSet = false;

                      if (response != null)
                      {
                        holder.cameraStatus.setVisibility(View.VISIBLE);


                        StatusDetails.StatusResponse[] statusResponseList = response.getDeviceStatusResponse();

                        StatusDetails.StatusResponse statusResponse = null;

                        if (statusResponseList != null && statusResponseList.length > 0) {
                          statusResponse = statusResponseList[ 0 ]; // fetch first object only
                        }

                        if (statusResponse != null)
                        {

                          StatusDetails.DeviceStatusResponse deviceStatusResponse = statusResponse.getDeviceStatusResponse();

                          String deviceStatus = deviceStatusResponse.getDeviceStatus();


                          if (deviceStatus != null)
                          {
                            isDeviceStatusSet = true;

                            if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0)
                            {
                              Log.d(TAG, "Set DeviceStatus as online");

                                P2pClient selectedP2pClient = P2pService.getP2pClient(mDevices.get(position).getProfile().getRegistrationId());
                                boolean existInP2pList = selectedP2pClient != null;

                              //  boolean isOrbitP2PEnabled =  !mDevices.get(position).getProfile().isStandBySupported() || HubbleApplication.AppConfig.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);

                                if  (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() &&
                                    mDevices.get(position).getProfile().canUseP2p() && mDevices.get(position).getProfile().canUseP2pRelay()
                                        && existInP2pList) {

                                    if (selectedP2pClient.isValid())
                                    {
                                        Log.i(TAG, " camera  : " + mDevices.get(position).getProfile().getName() + " Status : " + "Online");
                                        holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
                                    }
                                    else
                                    {
                                        Log.i(TAG, " camera  : " + mDevices.get(position).getProfile().getName() + " Status : " + "Connecting");
                                        holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_CONNECTING);
                                    }
                                }
                                else {
                                    Log.i("TAG", "always on camera  : " +mDevices.get(position).getProfile().getName() + " Status : " + "Online");
                                    holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
                                }

                                if(position >= mDevices.size()){
                                    return;
                                }
                              mDevices.get(position).getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);

                              holder.camImage.clearColorFilter();
                              holder.notificationLayout.setVisibility(View.VISIBLE);

                            }
                            else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_STANDBY) == 0)
                            {
                              Log.d(TAG, "set DeviceStatus as stand by");
                              holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);
                                if(position >= mDevices.size()){
                                    return;
                                }
                              mDevices.get(position).getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);

                              holder.camImage.setColorFilter(grayScaleFilter);
                              isDashBoardRefresh = true;
                                if(position >= mDevices.size()){
                                    return;
                                }
                              wakeUpRemoteDevice(position);

                            } else {
                                if(position >= mDevices.size()){
                                    return;
                                }
                              Log.d(TAG, "set DeviceStatus as offline");
                              holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);

                              mDevices.get(position).getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);

                              holder.camImage.setColorFilter(grayScaleFilter);
                              holder.notificationLayout.setVisibility(View.GONE);
                            }
                          }

                          float batteryLevel = deviceStatusResponse.getBatteryLevel();
                          int batteryMode = -1;

                            if (sharedPreferences.contains(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_DEVICE_MODE)))
                            {
                                String responseValueStr  = sharedPreferences.getString(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_DEVICE_MODE),null);
                                if(responseValueStr != null)
                                {
                                    Pair<Long,String> responseVal = PublicDefine.getSharedPrefValue(responseValueStr);
                                    if(responseVal != null)
                                    {
                                        try {
                                            batteryMode = Integer.parseInt(responseVal.second);
                                        }
                                        catch (Exception e){}
                                    }
                                }


                                if (sharedPreferences.contains(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_BATTERY_VALUE)))
                                {
                                    responseValueStr  = sharedPreferences.getString(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_BATTERY_VALUE),null);
                                    if(responseValueStr != null)
                                    {
                                        Pair<Long,String> responseVal = PublicDefine.getSharedPrefValue(responseValueStr);
                                        if(responseVal != null)
                                        {
                                            try {
                                                batteryLevel = Integer.parseInt(responseVal.second);
                                            }
                                            catch (Exception e){}
                                        }
                                    }
                                }
                            }

                          if(batteryMode == CameraStatusView.ORBIT_BATTERY_CHARGING)
                          {
                            holder.batteryLayout.setVisibility(View.VISIBLE);

                            holder.batteryStatusTv.setText(activity.getResources().getString(R.string.battery_status_percentage, (int) batteryLevel));

                            holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_charging);
                          }
                          else
                          {
                            if (batteryLevel != 0.0 && batteryLevel != 999)
                            {
                              holder.batteryLayout.setVisibility(View.VISIBLE);

                              holder.batteryStatusTv.setText(activity.getResources().getString(R.string.battery_status_percentage, (int) batteryLevel));

                              if (batteryLevel >= 1.0 && batteryLevel <= 9.9)
                                holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_empty);
                              else if (batteryLevel >= 10.0 && batteryLevel <= 24.9)
                                holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_low);
                              else if (batteryLevel >= 25 && batteryLevel <= 74.9)
                                holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_half);
                              else if (batteryLevel >= 75 && batteryLevel <= 100)
                                holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_full);
                            }
                            else if(batteryLevel == 999 || batteryLevel == 0.0)
                            {
                              holder.batteryLayout.setVisibility(View.VISIBLE);
                              holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_charging);
                            }
                          }
                        }
                      }

                      if(!isDeviceStatusSet)
                      {
                          if(position >= mDevices.size()){
                              return;
                          }
                        if(mDevices.get(position).getProfile().isAvailable())
                        {
                          holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
                          holder.cameraStatus.setVisibility(View.VISIBLE);
                        }

                      }
                    }
                  },
                  new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse (VolleyError error) {
                      Log.d(TAG, "Failed to get device status :- " + error.getMessage());

                      holder.cameraStatus.setVisibility(View.VISIBLE);

                      if (error != null && error.networkResponse != null) {
                        Log.d(TAG, error.networkResponse.toString());
                        Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));

                        holder.batteryLayout.setVisibility(View.GONE);

                        holder.notificationSwitch.setChecked(false);
                        holder.notificationSwitch.setClickable(false);

                      }
                    }
                  });
        }
      }
    }
    else if (!mDevices.get(position).getProfile().isAvailable())
    {
      if (BuildConfig.FLAVOR.equals("hubble") ||
          BuildConfig.FLAVOR.equals("hubblenew"))
      {
        holder.camImage.setColorFilter(grayScaleFilter);
      }
      if (!isSensor)
      {

        //holder.cameraStatus.setOnline(false);
        holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
        holder.notificationLayout.setVisibility(View.GONE);
      }
    }
    else
    {
      // Check p2p status from p2p service first

      if (BuildConfig.FLAVOR.equals("hubble") ||
          BuildConfig.FLAVOR.equals("hubblenew"))
      {
        holder.camImage.clearColorFilter();
      }
      if (!isSensor)
      {
          if(position >= mDevices.size()){
              return;
          }
          holder.notificationLayout.setVisibility(View.VISIBLE);
          P2pClient selectedP2pClient = P2pService.getP2pClient(mDevices.get(position).getProfile().getRegistrationId());
          boolean existInP2pList = selectedP2pClient != null;

          Log.d(TAG, "exist in P2P list:"+existInP2pList);
          final boolean isInLocal;

         // boolean isOrbitP2PEnabled =  !mDevices.get(position).getProfile().isStandBySupported() || HubbleApplication.AppConfig.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);

          if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() &&
                  mDevices.get(position).getProfile().canUseP2p() && mDevices.get(position).getProfile().canUseP2pRelay()
                  && existInP2pList) {
              if(selectedP2pClient.isValid()) {
                  Log.i("TAG", " camera  : " +mDevices.get(position).getProfile().getName() + " Status : " + "Online");
                  holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
              }else {
                  Log.i("TAG", " camera  : " +mDevices.get(position).getProfile().getName() + " Status : " + "Connecting");
                  holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_CONNECTING);
              }

          }else {
              Log.i(TAG, "always on camera  : " +mDevices.get(position).getProfile().getName() + " Status : " + "Online");

              holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
          }


          if (CommonUtil.checkSettings(activity, mDevices.get(position).getProfile().getRegistrationId() + "-" + SettingsPrefUtils.FETCH_SETTINGS)) {
              if (CommonUtil.getSettingInfo(activity, mDevices.get(position).getProfile().getRegistrationId() + "-" + SettingsPrefUtils.FETCH_SETTINGS))
                  fetchCameraSettings(mDevices.get(position));
          } else
              fetchCameraSettings(mDevices.get(position));


          //ARUNA
//        if(mDevices.get(position).getProfile().isStandBySupported())
//        {
//          holder.notificationSwitch.setChecked(true);
//          holder.notificationSwitch.setClickable(true);
//
//          holder.notificationSwitchON.setTextColor(activity.getResources().getColor(R.color.text_blue));
//          holder.notificationSwitch.setTrackDrawable(activity.getResources().getDrawable(R.drawable.custom_track_1));
//        }
      }
    }

    // invisible status view for sensor - temporary
    /* 20150909 HOANG AA-800 CRASH HERE!!!!
     * REMEMBER: Need to check whether this is sensor or camera before use
     * holder.cameraStatus or holder.sensorStatus reference.
     * Because holder.cameraStatus would be NULL for camera item and
     * holder.sensorStatus would be NULL for sensor item.
     */
    if (!isSensor) {
        if(position >= mDevices.size()){
            return;
        }
      if (mDevices.get(position).getProfile().getRegistrationId().startsWith("070004") && mDevices.get(position).getProfile().isStandBySupported()) {
        // This is open sensor
        holder.cameraStatus.setVisibility(View.INVISIBLE);
      } else {
        holder.cameraStatus.setVisibility(View.VISIBLE);
      }
    }

     if(settings.getBoolean(device.getProfile().getRegistrationId(),true)){
         settings.putBoolean(device.getProfile().getRegistrationId(),false);
         GeAnalyticsInterface.getInstance().trackEvent(AppEvents.UNO_CAMERA_DETAILS,AppEvents.CAMERA_MODEL_ID+" = "+device.getProfile().getRegistrationId().substring(2,6)+" : "+AppEvents.FW_VERSION+" = "+device.getProfile().getFirmwareVersion(),AppEvents.CAMERA_MODEL_ID);
         ZaiusEvent cameraNameEvt = new ZaiusEvent(AppEvents.UNO_CAMERA_DETAILS);
         cameraNameEvt.action(AppEvents.CAMERA_MODEL_ID+" = "+device.getProfile().getRegistrationId().substring(2,6)+" : "+AppEvents.FW_VERSION+" = "+device.getProfile().getFirmwareVersion());

         try {
             ZaiusEventManager.getInstance().trackCustomEvent(cameraNameEvt);
         } catch (ZaiusException e) {
             e.printStackTrace();
         }
     }
  }

    @Override
  public int getItemCount() {
    int count = 0;
    if (mDevices != null) {
      count = mDevices.size();
    } else {
      count = 0;
    }
    return count;
  }

    @Override
    public int getItemViewType(int position) {
        if(position >= mDevices.size()){
            return 0;
        }
        if (mDevices != null) {
            if (mDevices.get(position).getProfile().getName().contains("Thermostat")) {
                return THERMOSTAT;
            } else {
                return CAMERA;
            }
        }
        return 0;
    }

    public synchronized void setDevices(final List<Device> devices) {

    mDevicePositionMap.clear();
    mDevices.clear();
    if (devices != null) {
      // Clear old preview bitmap when updating device list.
      // P2pManager.getInstance().clearPreviewBitmaps();
      for (int i = 0; i < devices.size(); i++) {
        Device aDevice = devices.get(i);
        if (!mDevices.contains(aDevice)) {
          mDevicePositionMap.put(aDevice.getProfile().registrationId, i);
          mDevices.add(aDevice);
        }
      }
    }
  }

  private void filterDevices(boolean isSensor, Device device) {
   // if (isSensor) {
      Log.i(TAG, "is sensor device, parentId " + device.getProfile().getParentId());
      if (device.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR) || !TextUtils.isEmpty(device.getProfile().getParentId())) {
        sensorDevices.add(device);
      //}
    } else {
      if (!TextUtils.isEmpty(device.getProfile().getParentId())) {
        mDevices.add(device);
      }
    }
  }

  private void buildDevicePositionMap(final List<Device> devices) {
    if (devices != null) {
      for (int i = 0; i < devices.size(); i++) {
        Device aDevice = devices.get(i);
        if (aDevice != null && !TextUtils.isEmpty(aDevice.getProfile().registrationId)) {
          if (isSensor) {
            if (aDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR) || !TextUtils.isEmpty(aDevice.getProfile().getParentId())) {
              mDevicePositionMap.put(aDevice.getProfile().registrationId, i);
            }
          } else {
            if (!TextUtils.isEmpty(aDevice.getProfile().getParentId())) {
              mDevicePositionMap.put(aDevice.getProfile().registrationId, i);
            }
          }
        }
      } // end for
    } else {
      Log.d(TAG, "Build device list, devices list null");
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

  public void switchToCameraFragment(Device device) {
    settings.putString(PublicDefineGlob.PREFS_LAST_CAMERA, (device.getProfile().getMacAddress()));
    DeviceSingleton.getInstance().setSelectedDevice(device);

    activity.switchToCameraFragment(device);
  }

    CompoundButton.OnCheckedChangeListener privacyChangeListner = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
         int position = (int) buttonView.getTag();
            if(position >= mDevices.size()){
                return;
            }
            if (isChecked) {
                isDashBoardRefresh = false;
                AnalyticsInterface.getInstance().trackEvent(AppEvents.DEVICE_NOTIFICATION, AppEvents.DEVICE_NOTIFICATION_ON, eventData);
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD, AppEvents.CAMERA_PRIVACY_DISABLED,AppEvents.CAMERA_PRIVACY_DISABLED);
                ZaiusEvent cameraPrivacyDisableEvt = new ZaiusEvent(AppEvents.DASHBOARD);
                cameraPrivacyDisableEvt.action(AppEvents.CAMERA_PRIVACY_DISABLED);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(cameraPrivacyDisableEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }

                if (mDevices.get(position).getProfile().isStandBySupported()) {
                    notificationEnabled = true;
                    checkDeviceStatus(mDevices.get(position), position, holder.privacySwitch.isChecked());

                } else {
                    if(position >= mDevices.size()){
                        return;
                    }
                    if (mDevices.get(position).getProfile().isAvailable()) {
                        displayProgressDialog(activity.getResources().getString(R.string.communicate_with_camera));
                        NotificationStatusTask notificationChange = new NotificationStatusTask(mDevices.get(position), position, true, activity, mDeviceHandler);
                        notificationChange.execute();
                    } else {
                        holder.privacySwitch.setChecked(false);
                        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                        alertDialog.setTitle(R.string.privacy_action_failed);
                        alertDialog.setMessage(activity.getString(R.string.privact_action_failed_info));

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                         holder.privacySwitch.setChecked(false);
                                        dialog.dismiss();

                                    }
                                });

                        alertDialog.show();

                    }


                }

            }
        }

    };
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

  public void setEventDetail(Map<String,EventResponse> eventDetailMap){
    mIsEventResponseReceived = true;
    mEventResponseMap = eventDetailMap;
    notifyDataSetChanged();
  }

  private Drawable getLastAvailableImage(ImageView view, String registrationId) {
    Drawable placeholderImage = null;
    if (Util.isDashBoardPreviewAvailable(registrationId)) {
      placeholderImage = Drawable.createFromPath(Util.getDashBoardPreviewPath(registrationId));
    } else {
      placeholderImage = activity.getApplicationContext().getResources().getDrawable(R.drawable.default_cam);
    }
    return placeholderImage;
  }

  private ProgressDialog mProgressDialog;
  private void displayProgressDialog(String message)
  {
    if(mProgressDialog != null && mProgressDialog.isShowing())
      mProgressDialog.dismiss();

    mProgressDialog = new ProgressDialog(activity);
    mProgressDialog.setMessage(message);//activity.getResources().getString(R.string.please_wait));
    mProgressDialog.setCancelable(false);
    mProgressDialog.show();
  }

  private void dismissDialog()
  {
    if(mProgressDialog != null && mProgressDialog.isShowing())
      mProgressDialog.dismiss();
  }

    private void wakeUpRemoteDevice(int position) {
        if(position >= mDevices.size()){
            return;
        }
        if (!isDashBoardRefresh && mProgressDialog != null && mProgressDialog.isShowing())
            displayProgressDialog(activity.getResources().getString(R.string.camera_wake_up));
        if(!mDevices.get(position).getProfile().getStatus().equalsIgnoreCase("wakingup")) {
            
            String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");

            Log.d(TAG,"Wakeup device from cameraListArrayAdapter - 1");
            mDeviceWakeup = DeviceWakeup.newInstance();
            mDeviceWakeup.setDelayP2PTask(10000);
            mDeviceWakeup.wakeupDevice(mDevices.get(position).getProfile().registrationId,accessToken,mDeviceHandler,mDevices.get(position),position);

            mDevices.get(position).getProfile().setStatus("wakingup");
        }
    }
    public void cancelAsyncTask()
    {
        if(mDevices != null)
        {
            for(int count = 0; count < mDevices.size(); count++)
            {
                if(mDevices.get(count).getProfile().isStandBySupported())
                {
                    if(mDeviceWakeup != null)
                        mDeviceWakeup.cancelTask(mDevices.get(count).getProfile().registrationId,mDeviceHandler);
                }
            }
        }
    }
    public void cancelAsyncTask(String regID)
    {
       if(mDeviceWakeup != null)
           mDeviceWakeup.cancelTask(regID,mDeviceHandler);
    }


  private void notifyItemChange(final int position)
  {
    mHandler.post(new Runnable() {
      @Override
      public void run () {
          if(position >= mDevices.size()){
              return;
          }
        notifyItemChanged(position);
      }
    });
  }

  private Handler  mDeviceHandler = new Handler()
  {
    public void handleMessage(Message msg)
    {
      switch(msg.what) {
          case CommonConstants.DEVICE_WAKEUP_STATUS:

              boolean result = (boolean) msg.obj;
              final int position = msg.arg1;
              Log.d(TAG,"Device Handler position :- " + position);
              if(position >= mDevices.size()){
                  return;
              }

              boolean notificationChanged = false;
          /*final boolean notificationEnabled;
          if(msg.arg2 == 1)
            notificationEnabled = true;
          else
            notificationEnabled = false;*/
              if(position < mDevices.size())
                 mDevices.get(position).getProfile().setStatus("");

              if (!isDashBoardRefresh || (mProgressDialog != null && mProgressDialog.isShowing())) {

                  dismissDialog();
                  notificationChanged = true;
                  cancelAsyncTask(mDevices.get(position).getProfile().getRegistrationId() );
              }

              if (activity != null)
              {
                  if (result)
                  {
                      if (CommonUtil.checkSettings(activity, mDevices.get(position).getProfile().getRegistrationId() + "-" + SettingsPrefUtils.FETCH_SETTINGS)) {
                          if (CommonUtil.getSettingInfo(activity, mDevices.get(position).getProfile().getRegistrationId() + "-" + SettingsPrefUtils.FETCH_SETTINGS))
                              fetchCameraSettings(mDevices.get(position));
                      } else
                          fetchCameraSettings(mDevices.get(position));

                      if (!isDashBoardRefresh || notificationChanged || (mProgressDialog != null && mProgressDialog.isShowing())) {
                          dismissDialog();
                          Toast.makeText(activity, activity.getResources().getString(R.string.online), Toast.LENGTH_SHORT).show();
                          displayProgressDialog("Communicating with camera");

                          NotificationStatusTask notificationChange = new NotificationStatusTask(mDevices.get(position), position, notificationEnabled, activity, mDeviceHandler);
                          notificationChange.execute();
                      }


                      P2pClient selectedP2pClient = P2pService.getP2pClient(mDevices.get(position).getProfile().getRegistrationId());
                      boolean existInP2pList = selectedP2pClient != null;

                     // boolean isOrbitP2PEnabled =  !mDevices.get(position).getProfile().isStandBySupported() || HubbleApplication.AppConfig.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);
                      if (  P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() &&
                              mDevices.get(position).getProfile().canUseP2p() && mDevices.get(position).getProfile().canUseP2pRelay()
                              && existInP2pList)
                      {
                          if (selectedP2pClient.isValid())
                          {
                              Log.i(TAG, " camera  : " + mDevices.get(position).getProfile().getName() + " Status : " + "Online");
                              holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
                          }
                          else
                          {
                              Log.i(TAG, " camera  : " + mDevices.get(position).getProfile().getName() + " Status : " + "Connecting");
                              holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_CONNECTING);
                          }
                      }
                      else {
                          Log.i(TAG, "always on camera  : " +mDevices.get(position).getProfile().getName() + " Status : " + "Online");
                          holder.cameraStatus.setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
                      }

                      mDevices.get(position).getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);

                     // holder.camImage.clearColorFilter();
                     // holder.notificationLayout.setVisibility(View.VISIBLE);


                      notifyItemChange(position);
                  }


              }
              break;

          case CommonConstants.DEVICE_NOTIFICATION_STATUS:

              boolean result1 = (boolean) msg.obj;
              final int position1 = msg.arg1;
              dismissDialog();

              if(position1 >= mDevices.size()){
                  return;
              }
              if (activity != null) {
                  displayProgressDialog(activity.getResources().getString(R.string.changing_camera_status));
                /*  if (mDevices.get(position1).getProfile().getModelId().equalsIgnoreCase("0080")) {
                      if (result1) {

                          Toast.makeText(activity, activity.getResources().getString(R.string.pir_enabled), Toast.LENGTH_SHORT).show();
                      } else {
                          Toast.makeText(activity, activity.getResources().getString(R.string.pir_disabled), Toast.LENGTH_SHORT).show();
                      }
                  } else {
                      if (result1) {
                          Toast.makeText(activity, activity.getResources().getString(R.string.notifications_enabled), Toast.LENGTH_SHORT).show();
                      } else
                          Toast.makeText(activity, activity.getResources().getString(R.string.notifications_disabled), Toast.LENGTH_SHORT).show();
                  }*/
                  if (result1) {
                      ChangePrivacyMode changePrivacyMode = new ChangePrivacyMode(mDevices.get(position1), position1, false, activity, mDeviceHandler);
                      changePrivacyMode.execute();
                  } else {
                      ChangePrivacyMode changePrivacyMode = new ChangePrivacyMode(mDevices.get(position1), position1, true, activity, mDeviceHandler);
                      changePrivacyMode.execute();
                  }
                 /* notifyItemChange(position1);
                  notifyDataSetChanged();*/
                  // putDeviceIntoStandByMode(position1);


              }
              break;


          case CommonConstants.DEVICE_CAMERA_STATUS:
              dismissDialog();
              final int position2 = msg.arg1;

              notifyItemChange(position2);
              notifyDataSetChanged();
              break;

      }
    }
  };





  private void  checkDeviceStatus(final Device selectedDevice, final int position, final boolean isEnabled){
    displayProgressDialog(activity.getResources().getString(R.string.viewfinder_progress_check_device_status));
    Log.d(TAG, "checkDeviceStatus");
    final DeviceStatus deviceStatus = new DeviceStatus(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),selectedDevice.getProfile().getRegistrationId());

    DeviceManagerService.getInstance(activity).getDeviceStatus(deviceStatus,new Response.Listener<StatusDetails>()
            {
              @Override
              public void onResponse(StatusDetails response)
              {
                  if(position >= mDevices.size()){
                      return;
                  }
                //dismissDialog();
                if(response != null)
                {
                  StatusDetails.StatusResponse[] statusResponseList = response.getDeviceStatusResponse();

                  StatusDetails.StatusResponse statusResponse = null;

                  if(statusResponseList != null && statusResponseList.length > 0)
                  {
                    statusResponse = statusResponseList[0]; // fetch first object only
                  }

                  if(statusResponse != null)
                  {
                    StatusDetails.DeviceStatusResponse deviceStatusResponse = statusResponse.getDeviceStatusResponse();
                    String deviceStatus = deviceStatusResponse.getDeviceStatus();

                    Log.d(TAG,"device status :- " + deviceStatus);

                    if(deviceStatus != null)
                    {
                      if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE)==0)
                      {
                        selectedDevice.getProfile().setAvailable(true);
                        selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
                        Log.d(TAG, "device online..");
                        displayProgressDialog(activity.getResources().getString(R.string.communicate_with_camera));

                          NotificationStatusTask notificationChange = new NotificationStatusTask(mDevices.get(position), position, isEnabled, activity, mDeviceHandler);
                          notificationChange.execute();


                      }else if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_STANDBY)==0){
                        selectedDevice.getProfile().setAvailable(false);
                        selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);
                        Log.d(TAG, "device standby..wakeup");
                        //wakeup device
                        wakeUpRemoteDevice(position);
                      }
                      else if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_OFFLINE)==0)
                      {
                        Log.d(TAG, "setting device available false");
                        selectedDevice.getProfile().setAvailable(false);
                        selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
                        //device offline
                        Toast.makeText(activity, activity.getString(R.string.camera_offline),Toast.LENGTH_SHORT).show();
                          AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                          alertDialog.setTitle(R.string.privacy_action_failed);
                          alertDialog.setMessage(activity.getString(R.string.privact_action_failed_info));

                          alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                  new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int which) {
                                          holder.privacySwitch.setChecked(false);
                                          dialog.dismiss();
                                      }
                                  });
                          alertDialog.show();

                      }
                    }
                  }
                }
              }
            },
            new Response.ErrorListener()
            {
              @Override
              public void onErrorResponse (VolleyError error)
              {
                dismissDialog();
                if (error != null && error.networkResponse != null)
                {
                  Log.d(TAG, error.networkResponse.toString());
                  Log.d(TAG, "Error Message :- " +new String(error.networkResponse.data));
                }
              }
            });

  }


  private void openViewFinder(int listPosition){
      if(listPosition >= mDevices.size()){
          return;
      }
      AnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAMING,AppEvents.CAMERA_STREAMING_CLICKED,eventData);
      GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.CAMERA_STREAMING_CLICKED,AppEvents.CAMERA_STREAMING_CLICKED);
      ZaiusEvent cameraStreamingEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
      cameraStreamingEvt.action(AppEvents.CAMERA_STREAMING_CLICKED);
      try {
          ZaiusEventManager.getInstance().trackCustomEvent(cameraStreamingEvt);
      } catch (ZaiusException e) {
          e.printStackTrace();
      }
    if (isCheckingCameraInSameNetwork) {
      //Do nothing
      return;
    }

    final Device device = mDevices.get(listPosition);
    Log.i(TAG, "Camera image clicked: " + device.getProfile().getRegistrationId());
    Device cloneDevice = DeviceSingleton.getInstance().getDeviceByMAC(device.getProfile().getMacAddress());
    if (cloneDevice != null) {
      mDevices.set(listPosition, cloneDevice);
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
    }
    else if (cloneDevice.getProfile().isAvailable())
    {
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
    }
    else if(cloneDevice.getProfile().isStandBySupported() && cloneDevice.getProfile().getDeviceStatus() == CameraStatusView.DEVICE_STATUS_STANDBY)
    {
      settings.putString(PublicDefineGlob.PREFS_LAST_CAMERA, (cloneDevice.getProfile().getMacAddress()));
      DeviceSingleton.getInstance().setSelectedDevice(cloneDevice);
      switchToCameraFragment(cloneDevice);
    }
    else if (!isOfflineMode) {
      if (isF86Camera)
        //Note : Add support when F86 is added to support list in Uno app
        //activity.goToCameraEventLog(cloneDevice);
        activity.goToEventLog(cloneDevice);
      else {
        activity.goToEventLog(cloneDevice);
      }
    }

  }


    public void notifyNestData(ArrayList<Thermostat> thermostats) {
        this.thermostats = thermostats;
        for (Thermostat thermostat : thermostats) {
            for (int i = 0; i < mDevices.size(); i++) {
              //     if (thermostat.getDeviceId().equals(mDevices.get(i).getProfile().getRegistrationId())) {
                    notifyItemChange(i);
                    break;
               // }
            }
        }

    }

  private String shortenTagName(String tagName){
    String shortTagName = "";

      tagName = tagName.trim().replaceAll("\\s{2,}", " "); // The regex is to replace multple spaces if any between words


    if(tagName.contains(" ")){

      String[] tagNameWords = tagName.split(" ");
      for(int word = 0; word <= 1 ; word++){
        shortTagName = shortTagName + (tagNameWords[word].charAt(0));
      }
      return shortTagName;
    }else{
      if(tagName.length() >= 2)
        shortTagName = shortTagName + tagName.charAt(0) + tagName.charAt(1);
      else
        shortTagName = tagName;

      return shortTagName;
    }
  }

    private boolean saveBitmapToFile(String registrationId, Bitmap bm) {
        File imageFile = new File(Util.getDashBoardPreviewPath(registrationId));
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e("app", e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }


    public void fetchCameraSettings(final Device device){

        fetchFirmwareDetail(device);
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {

                        Log.i(TAG, "Called getcamera settings for " + device.getProfile().getName());


                        boolean isLocal = CameraAvailabilityManager.getInstance().isCameraInSameNetwork(activity, device);


                        settingsActor = new CameraSettingsActor(activity, device, mActorInterface,isLocal);

                        queryCameraSettings(device, settingsActor);
                        buildGroupSetting2Codes(device, settingsActor);
                        getSetting2IfAvailable(PublicDefine.SETTING_2_KEY_CEILING_MOUNT, device, settingsActor);
                        if(device.getProfile().getModelId().equals("0086")){
                            getOverlayDateIfAvailable(settingsActor);
                        }

                    if(device.getProfile().isSupportMvrScheduling()&&
                            !device.getProfile().getModelId().equalsIgnoreCase("0080")) {
                        getMotionSchedule(settingsActor);
                    }

                    /*if (device.getProfile().doesSupportSDCardAccess() &&
                            !device.getProfile().getModelId().equalsIgnoreCase("0080")) {
                        getRecordingPlan(settingsActor);
                    }
*/

                    CommonUtil.setSettingInfo(activity, device.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.FETCH_SETTINGS, false);

                    return null;
                }


            };
            asyncTask.execute();

        //Set free trial days

        if (activity != null && device != null && !device.getProfile().getModelId().
                equalsIgnoreCase(PublicDefine.MODEL_ID_SMART_NURSERY)
                && device.getProfile().getParentRegistrationId() == null
                && !device.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)
                && (!device.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
                || (Util.isThisVersionGreaterThan(device.getProfile().getFirmwareVersion(), PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)))) {
            CommonUtil.setFreeTrialDays(activity, device.getProfile().getPendingFreeTrialDays());
        }
    }


    private CameraSettingsActor.Interface mActorInterface = new CameraSettingsActor.Interface() {
        @Override
        public void onDataSetChanged(ListChild listChild) {}

        @Override
        public void onNotificationSettingsReceived() {}

        @Override
        public void onParkReceived(Pair<String, Object> response) {

        }

        @Override
        public void onParkTimerReceived(Pair<String, Object> response) {

        }

        public void onMotionNotificationChange(ListChild listChild, boolean shouldRevert, String responseMessage ){}

        @Override
        public void onValueSet(final ListChild listChild,final boolean shouldRevert, final String responseMessage) {

        }

        @Override
        public void onScheduleDataReceived() {

        }
    };

    boolean mIsMotionEnabled = false;
    boolean mIsSoundEnabled = false;
    boolean mIsLowTempEnabled = false;
    boolean mIsHiTempEnabled = false;


    ListChild   ceilingMount,viewMode,overlayDate,motionDetection,soundDetection,temperature,qualityOfService,
            brightness, volume, contrast, nightVision, park, videoQuality, statusLed, mLEDFlicker;

    String[] groupSettings;

    private void queryCameraSettings(final Device device, CameraSettingsActor settingsActor){



        motionDetection = new ListChild(activity.getString(R.string.motion_detection), "", true);
        if (device.getProfile().doesHaveMicrophone()) {

            soundDetection = new ListChild(activity.getString(R.string.sound_detection), "", true);
            //  volume = new CameraSettingsActivity.ListChild(getSafeString(R.string.volume), "", true);
        }else{
            //ll_soundSensitivity.setVisibility(View.GONE);
            soundDetection = null;
        }


        if (device.getProfile().doesHaveTemperature()) {
            temperature = new ListChild(activity.getString(R.string.temperature), "", true);
        } else {
            temperature = null;
        }

        settingsActor.send(new ActorMessage.GetNotificationSettings(motionDetection, soundDetection, temperature));

    }

    private void getOverlayDateIfAvailable( CameraSettingsActor settingsActor) {

        overlayDate = new ListChild(activity.getString(R.string.overlay_date), "", true);
            if (overlayDate != null) {

                settingsActor.send(new ActorMessage.GetOverlayDate(overlayDate));
                //listAdapter.notifyDataSetChanged();
            }

    }


    private void buildGroupSetting2Codes(Device device, CameraSettingsActor settingsActor) {
        if (device.getProfile().isVTechCamera()) {
            groupSettings = PublicDefine.groupSettingsVtech;
        } else {
            // hubble device that support night vision mode
            if (isHubbleIR(device)) {
                groupSettings = PublicDefine.groupSettingsHubbleIR;
            } else {
                groupSettings = PublicDefine.groupSettingsGeneric;
            }
        }
        // filter codes that already have valid value
        ArrayList<String> filterList = new ArrayList<>();
        for (String code : groupSettings) {
            ListChild item = getListChildBySetting2Code(code);
            if (item == null) {
                continue;
            }
            if (TextUtils.isEmpty(item.value) || item.value.equals(activity.getString(R.string.failed_to_retrieve_camera_data))) {
                filterList.add(code);
            }
        }
        groupSettings = new String[filterList.size()];
        for (int i = 0; i < filterList.size(); i++) {
            groupSettings[i] = filterList.get(i);
        }
    }

    private ListChild getListChildBySetting2Code(String setting2Code) {
        ListChild listChild = null;
        if (setting2Code.equals(PublicDefine.SETTING_2_KEY_CEILING_MOUNT)) {
            ceilingMount = new ListChild(activity.getString(R.string.ceiling_mount), "", true);
            listChild = ceilingMount;
        }else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_BRIGHTNESS)) {
            brightness = new ListChild(activity.getString(R.string.brightness), "", true);
            listChild = brightness;
        } else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_VOLUME)) {
            volume = new ListChild(activity.getString(R.string.volume), "", true);
            listChild = volume;
        } else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_CONTRACT)) {
            contrast =  new ListChild(activity.getString(R.string.contrast), "", true);
            listChild = contrast;
        } else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_NIGHT_VISION)) {
            nightVision = new ListChild(activity.getString(R.string.night_vision), "", true);
            listChild = nightVision;
        } else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_PARKING)) {
            listChild = park;
        } else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_OVERLAY_DATE)) {
            overlayDate = new ListChild(activity.getString(R.string.overlay_date),"", true);
            listChild = overlayDate;
        } else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_VIEW_MODE)) {
            viewMode = new ListChild(activity.getString(R.string.view_mode),"", true);
            listChild = viewMode;
        }else if (setting2Code.equals(PublicDefine.SETTING_2_KEY_QUALITY_OF_SERVICE)) {
            qualityOfService = new ListChild(activity.getString(R.string.video_quality),"", true);
            listChild = qualityOfService;
        }
        return listChild;
    }


    /**
     * Get specific setting2 code. called when click on Failed-to-retrieve-camera-data listView item
     *
     * @param setting2Code
     */
    private void getSetting2IfAvailable(String setting2Code, Device mDevice, CameraSettingsActor settingsActor) {
        boolean setting2Compatible = mDevice != null && Util.checkSetting2Compatibility(mDevice.getProfile().getModelId(),
                mDevice.getProfile().getFirmwareVersion());
        // if this code is supported by setting2 and device is compatible with setting2
        // -> build all items that do not have value or have invalid value -> then reload these items
        // opposite reload only given code
        if (setting2Compatible && Arrays.asList(PublicDefine.groupSettingsAll).contains(setting2Code)) {
            // all codes that device needs
            buildGroupSetting2Codes(mDevice, settingsActor);
        } else {
            groupSettings = new String[]{setting2Code};
        }
        getSetting2IfAvailable(mDevice, settingsActor);
    }

    private Map<String, ListChild> buildSettingListChildMap() {
        Map<String, ListChild> listChildMap = new HashMap<>();
        List<String> allSetting2 = Arrays.asList(PublicDefine.groupSettingsAll);
        for (int i = 0; i < groupSettings.length; i++) {
            if (allSetting2.contains(groupSettings[i])) {
                listChildMap.put(groupSettings[i], getListChildBySetting2Code(groupSettings[i]));
            }
        }
        return listChildMap;
    }




    /**
     * Get all setting2 codes in groupSettings if available. handle for both setting and setting2.
     */
    private void getSetting2IfAvailable(Device mDevice, CameraSettingsActor settingsActor) {
        if (groupSettings != null && groupSettings.length > 0) {
            // just for logging
            String log = "";
            for (String code : groupSettings) {
                log += (code + ",");
            }
            Log.d("CameraSettingsActivity", "setting codes inside request: " + log);

            boolean setting2Compatible = mDevice != null && Util.checkSetting2Compatibility(mDevice.getProfile().getModelId(),
                    mDevice.getProfile().getFirmwareVersion());
            // all setting2 keys have to be handled. include no-supported keys.
            List<String> allSetting2 = Arrays.asList(PublicDefine.groupSettingsAll);
            // build map: setting2 key (base on groupSettings) - UI listChild
            Map<String, ListChild> listChildMap = buildSettingListChildMap();
            // separately get value for no-supported keys in groupSettings. mark others as loading.
            boolean needSendingSetting2Request = false;
            for (int i = 0; i < groupSettings.length; i++) {
                if (setting2Compatible && allSetting2.contains(groupSettings[i])) {
                    ListChild item = getListChildBySetting2Code(groupSettings[i]);
                    // if item does not have value or have invalid value -> reload it
                    if (item != null) {
                        needSendingSetting2Request = true;

                    }
                } else {
                   /* if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_CEILING_MOUNT)) {
                        Log.d(TAG, "get ceiling mount setting ...");
                        getCeilingMountIfAvailable(true);
                    }else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_PARKING)) {
                        getNotificationSettings(); // need values for park
                        Log.d(TAG, "get parking setting ...");
                        getParkIfAvailable(true);
                    }else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_BRIGHTNESS)) {
                        Log.d(TAG, "get brightness setting ...");
                        getBrightnessIfAvailable(true);
                    } else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_VOLUME)) {
                        Log.d(TAG, "get volume setting ...");
                        getVolumeIfAvailable(true);
                    } else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_CONTRACT)) {
                        Log.d(TAG, "get contrast setting ...");
                        getContrastIfAvailable(true);
                    } else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_NIGHT_VISION)) {
                        Log.d(TAG, "get night vision setting ...");
                        getNightVisionIfAvailable(true);
                    } else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_OVERLAY_DATE)) {
                        Log.d(TAG, "get overlay date setting ...");
                        getOverlayDateIfAvailable(true);
                    } else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_VIEW_MODE)) {
                        Log.d(TAG, "get view mode setting ...");
                        getViewModeIfAvailable(true);
                    } else {
                        Log.d(TAG, "Skip setting2 code " + groupSettings[i] + ". It may be added after Dec 18, 2015.");
                    }*/
                }
            }
            //listAdapter.notifyDataSetChanged();
            if (needSendingSetting2Request) {

                settingsActor.send(new ActorMessage.GetSetting2Settings(groupSettings, listChildMap));
            }
        } else {
            Log.d(TAG, "skip get setting2 because groupSettings is empty");
        }
    }
    /* Code for general settings */

    private Map<String, String> modelSupportIR = ImmutableMap.<String, String>builder()
            .put("0073", "01.19.10")
            .put("0173", "01.19.32")
            .put("0066", "01.19.30")
            .put("0662", "01.19.32")
            .put("1662", "01.19.32")
            .put("0085", "01.19.32")
            .put("0854", "01.19.32")
            .put("1854", "01.19.32")
            .put("0086", "") // any versions
            .put("0877", "") // any versions
            .put("0855", "")
            .put("0072", "01.19.16")
            .put("0080", "")
            .put("0083", "01.19.30")
            .put("0836", "01.19.30")
            .put("0667", "") // any versions
            .put("1855", "")
            .put("2855", "")
            .build();


    private boolean isHubbleIR(Device device) {
        String modelId = device.getProfile().getModelId();
        String fw = device.getProfile().getFirmwareVersion();
        if (!modelSupportIR.containsKey(modelId)) {
            return false;
        }
        if (TextUtils.isEmpty(modelSupportIR.get(modelId))) { // any versions
            return true;
        }
        return checkVersionSupportIR(fw, modelSupportIR.get(modelId));
    }

    public static boolean checkVersionSupportIR(String version1, String version2) {
        // version format 01.01.01
        String[] versions1 = version1.split("\\.");
        String[] versions2 = version2.split("\\.");
        boolean result = false;

        if (versions1.length == 3 && versions2.length == 3) {
            Integer major1 = Integer.parseInt(versions1[0]);
            Integer major2 = Integer.parseInt(versions2[0]);
            Integer minor1 = Integer.parseInt(versions1[1]);
            Integer minor2 = Integer.parseInt(versions2[1]);
            Integer patch1 = Integer.parseInt(versions1[2]);
            Integer patch2 = Integer.parseInt(versions2[2]);

            if (major1 > major2) {
                result = true;
            } else if (major1 == major2) {
                if (minor1 > minor2) {
                    result = true;
                } else if (minor1 == minor2) {
                    if (patch1 > patch2) {
                        result = true;
                    } else if (patch1 == patch2) {
                        result = true;
                    } else {
                        result = false;
                    }
                } else if (minor2 > minor1) {
                    result = false;
                }
            }
        }

        return result;
    }


    private void getRecordingPlan(CameraSettingsActor actor) {

        actor.getRecordingPlanValue(new CameraSettingsActor.RecodngPlanListener() {
            @Override
            public void onRecordingPlanResponse(boolean success, int plan) {

            }
        });
    }

    private void getMotionSchedule(CameraSettingsActor actor){
        actor.getMotionSchedule();
    }

    private void fetchFirmwareDetail(Device sDevice){
        String fwVersion = sDevice.getProfile().getFirmwareVersion();
        String regId = sDevice.getProfile().getRegistrationId();
        String modelId = sDevice.getProfile().getModelId();
        String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

        boolean deviceOTA = false;
        if(sDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)==0)
        {
            if(Util.isThisVersionGreaterThan(sDevice.getProfile().getFirmwareVersion(), CheckFirmwareUpdateTask.ORBIT_NEW_FIRMWARE_WORK_FLOW))
            {
                deviceOTA = true;
            }
        }

        if(BuildConfig.DEBUG)
            Log.d(TAG,"deviceOTA : " + deviceOTA);
        final Device device = sDevice;

        new CheckFirmwareUpdateTask(saved_token, regId, fwVersion, modelId, sDevice, new IAsyncTaskCommonHandler() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void onPostExecute(final Object result)
            {
                if (result instanceof CheckFirmwareUpdateResult)
                {
                    CheckFirmwareUpdateResult checkFirmwareUpdateResult = (CheckFirmwareUpdateResult) result;
                    if(checkFirmwareUpdateResult != null && checkFirmwareUpdateResult.isHaveNewFirmwareVersion()) {
                        CommonUtil.setSettingInfo(activity, device.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, true);
                    }else {
                        CommonUtil.setSettingInfo(activity, device.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, false);
                    }
                }
            }

            @Override
            public void onCancelled() {
            }
        }, settings.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false),deviceOTA).execute();
    }

    void showBatteryStatus( int position ){
        int batteryLevel = -1;
        int batteryMode = -1;
        if(position >= mDevices.size()){
            return;
        }
        if (sharedPreferences.contains(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_DEVICE_MODE))) {
            String responseValueStr = sharedPreferences.getString(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_DEVICE_MODE), null);
            if (responseValueStr != null) {
                Pair<Long, String> responseVal = PublicDefine.getSharedPrefValue(responseValueStr);
                if (responseVal != null) {
                    try {
                        batteryMode = Integer.parseInt(responseVal.second);
                        Log.d(TAG, "batteryMode :- " + batteryMode);
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }


            if (sharedPreferences.contains(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_BATTERY_VALUE))) {
                responseValueStr = sharedPreferences.getString(PublicDefine.getSharedPrefKey(mDevices.get(position).getProfile().getRegistrationId(), PublicDefine.GET_BATTERY_VALUE), null);
                if (responseValueStr != null) {
                    Pair<Long, String> responseVal = PublicDefine.getSharedPrefValue(responseValueStr);
                    if (responseVal != null) {
                        try {
                            batteryLevel = Integer.parseInt(responseVal.second);
                            Log.d(TAG, "batteryLevel :- " + batteryLevel);
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }
            }
        }

        if(batteryMode == CameraStatusView.ORBIT_BATTERY_CHARGING)
        {
            holder.batteryLayout.setVisibility(View.VISIBLE);

            holder.batteryStatusTv.setText(activity.getResources().getString(R.string.battery_status_percentage, (int) batteryLevel));

            holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_charging);
        }
        else
        {
            if(batteryLevel == -1){
                holder.batteryLayout.setVisibility(View.VISIBLE);
            }
            else if (batteryLevel != 0.0 && batteryLevel != 999)
            {
                holder.batteryLayout.setVisibility(View.VISIBLE);


                holder.batteryStatusTv.setText(activity.getResources().getString(R.string.battery_status_percentage, (int) batteryLevel));

                if (batteryLevel >= 1.0 && batteryLevel <= 9.9)
                    holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_empty);
                else if (batteryLevel >= 10.0 && batteryLevel <= 24.9)
                    holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_low);
                else if (batteryLevel >= 25 && batteryLevel <= 74.9)
                    holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_half);
                else if (batteryLevel >= 75 && batteryLevel <= 100)
                    holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_full);
            }
            else if(batteryLevel == 999 || batteryLevel == 0.0)
            {
                holder.batteryLayout.setVisibility(View.VISIBLE);
                holder.batteryImage.setImageResource(R.drawable.vector_drawable_battery_charging);
            }
        }
    }

}
