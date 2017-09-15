package com.hubble.notifications;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.actors.Actor;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceStatus;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.helpers.AsyncPackage;
import com.hubble.registration.PublicDefine;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.Global;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.squareup.picasso.Picasso;
import com.util.DeviceWakeup;

import base.hubble.Api;
import base.hubble.Models.ApiResponse;
import base.hubble.Models.TimelineEventList;
import base.hubble.PublicDefineGlob;
import base.hubble.constants.Streaming;
import base.hubble.database.TimelineEvent;
import cz.havlena.ffmpeg.ui.FFMpegPlaybackActivity;
import retrofit.RetrofitError;

public class MotionEventInteractionActivity extends FragmentActivity {
  private static final String TAG = "MotionEventInteraction";

  public static final String TRIGGER_DEVICE_ID = "string_DeviceMac";
  public static final String TRIGGER_TYPE = "int_VoxType";
  public static final String TRIGGER_EVENT_CODE = "string_EventCode";
  public static final String TRIGGER_DEVICE_NAME = "string_CameraName";
  public static final String TRIGGER_IMAGE_URL = "string_imageUrl";

  private String registrationId, eventCode, deviceName, imageUrl;
  private String mApiKey;

  private boolean imageLoaded = false;

  private ProgressDialog mProgressDialog = null;
  private DeviceWakeup mDeviceWakeup = null;

  Button btnIgnore, btnViewEvent, btnGoToCamera;
  ImageView imageView;
  TextView eventText;
  private String clipName, md5Sum;
  private boolean isClipOnSdCard;
  private String comeFrom = null;
  private class GetEvent {
    public String eventCode;
    public String registrationId;

    public GetEvent(String registrationId, String eventCode) {
      this.registrationId = registrationId;
      this.eventCode = eventCode;
    }
  }

  String fileUrl = "";
  private Actor eventGatherer = new Actor() {
    Context ctx = MotionEventInteractionActivity.this;

    @Override
    public Object receive(final Object m) {
      if (!isPaused && m instanceof GetEvent) {
        final GetEvent message = (GetEvent) m;

        AsyncPackage.doInBackground(new Runnable() {
          @Override
          public void run() {
            try {
              if (Global.getApiKey(ctx) != null && message != null & message.registrationId != null && message.eventCode != null) {
                /*
                 * 20150819_hoang_AA-706: fix "page" offset of event query
                 * The "page" should start from 0 instead of 1.
                 */
                ApiResponse<TimelineEventList> events = Api.getInstance().getService().getTimelineEventsForDevice(message.registrationId, Global.getApiKey(ctx), null, message.eventCode, null, 0, 1, true);
                for (final TimelineEvent event : events.getData().getEvents()) {
                  if (event != null && event.getData() != null) {
                    if (!imageLoaded && !event.getData().isEmpty()) {
                      final String eventImageUrl = event.getData().get(0).getImage();
                      if (eventImageUrl != null && !eventImageUrl.isEmpty()) {
                        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                            Picasso.with(getApplicationContext()).load(eventImageUrl).placeholder(R.drawable.notificationnteraction_defaultimage).into(imageView);
                            imageLoaded = true;
                          }
                        });
                      }
                    }
                    fileUrl = event.getData().get(0).getFile();
                    if (fileUrl != null && !fileUrl.trim().isEmpty()) {
                      runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                          if (event.getStorage_mode() == 1) {
                            clipName = fileUrl.trim();
                            md5Sum = event.getData().get(0).getMD5Sum();
                            isClipOnSdCard = true;
                          }
                          btnViewEvent.setVisibility(View.VISIBLE);
                          btnViewEvent.setText(getString(R.string.play_event));
                          btnViewEvent.setTextColor(getResources().getColor(R.color.material_deep_teal_500));
                          btnViewEvent.setEnabled(true);
                        }
                      });
                      break;
                    } else {
                      after(5000, m);
                    }
                  }
                }
              }
            } catch (RetrofitError retrofitError) {
              Log.e(TAG, Log.getStackTraceString(retrofitError));
              after(5000, m);
            }
          }
        });
      }
      return null;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_notification_interaction);
    getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    initialize();

    Bundle extras = getIntent().getExtras();
    registrationId = extras.getString(TRIGGER_DEVICE_ID);
    eventCode = extras.getString(TRIGGER_EVENT_CODE);
    imageUrl = extras.getString(TRIGGER_IMAGE_URL);
    deviceName = extras.getString(TRIGGER_DEVICE_NAME);

    if (imageUrl != null && !imageUrl.isEmpty()) {
      Picasso.with(this).load(imageUrl).placeholder(R.drawable.notificationnteraction_defaultimage).into(imageView);
      imageLoaded = true;
    }

    eventText.setText(String.format(getString(R.string.motion_detection_on_blank_camera), deviceName));

    eventGatherer.send(new GetEvent(registrationId, eventCode));

    if (getIntent().getExtras() != null) {
      comeFrom = getIntent().getExtras().getString(FFMpegPlaybackActivity.COME_FROM, null);
    }
  }

  @Override
  protected void onPause() {
    isPaused = true;
    if(mProgressDialog != null && mProgressDialog.isShowing()) {
      mProgressDialog.dismiss();
    }
    super.onPause();
  }

  @Override
  protected void onResume() {
    isPaused = false;
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setCancelable(false);
    super.onResume();
  }

  private boolean isPaused = false;

  private void initialize() {
    final Context mContext = getApplicationContext();
    mApiKey = Global.getApiKey(mContext);

    if (mApiKey != null) {
      DeviceSingleton.getInstance().init(mApiKey, mContext);
    }

    imageView = (ImageView) findViewById(R.id.notificationInteraction_imageVew);
    eventText = (TextView) findViewById(R.id.notificationInteraction_eventText);
    btnIgnore = (Button) findViewById(R.id.notificationInteraction_ignoreButton);
    btnGoToCamera = (Button) findViewById(R.id.notificationInteraction_cameraButton);
    btnViewEvent = (Button) findViewById(R.id.notificationInteraction_playButton);

    btnIgnore.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    btnGoToCamera.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mApiKey != null) {
          Intent intent = new Intent(MotionEventInteractionActivity.this, MainActivity.class);
          Device selectedDevice = DeviceSingleton.getInstance().getDeviceByRegId(registrationId);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          if (selectedDevice != null) {
            // only go to the device if we found it
            DeviceSingleton.getInstance().setSelectedDevice(selectedDevice);
            HubbleApplication.AppConfig.putBoolean(PublicDefine.PREFS_SHOULD_GO_TO_CAMERA, true);
          }
          startActivity(intent);
        } else {
          Toast.makeText(mContext, mContext.getString(R.string.cannot_go_to_camera), Toast.LENGTH_SHORT).show();
        }
        finish();
      }
    });

    btnViewEvent.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Device selectedDevice = DeviceSingleton.getInstance().getDeviceByRegId(registrationId);
          if (selectedDevice != null && selectedDevice.getProfile().isStandBySupported() &&
                  isClipOnSdCard) {
            checkDeviceStatus(selectedDevice);
          } else {
            playMotionEvent();
          }
        }
    });
  }

  private void playMotionEvent(){
    Intent intent = new Intent(MotionEventInteractionActivity.this, FFMpegPlaybackActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra(Streaming.EXTRA_REGISTRATION_ID, registrationId);
    intent.putExtra(Streaming.CAMERA_NAME, deviceName);
    intent.putExtra(Streaming.EXTRA_EVENT_CODE, eventCode);
    intent.putExtra(Streaming.EXTRA_CLIP_NAME, clipName);
    intent.putExtra(Streaming.EXTRA_MD5_SUM, md5Sum);
    intent.putExtra(Streaming.EXTRA_IS_CLIP_ON_SDCARD, isClipOnSdCard);
    intent.putExtra(Streaming.EXTRA_IS_LOCAL_CAMERA, false);
    intent.putExtra(FFMpegPlaybackActivity.COME_FROM, comeFrom);
    startActivity(intent);
    finish();
  }

  private void checkDeviceStatus(final Device selectedDevice) {
    String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");

    final DeviceStatus deviceStatus = new DeviceStatus(accessToken, registrationId);

    DeviceManagerService.getInstance(this).getDeviceStatus(deviceStatus, new Response.Listener<StatusDetails>() {
              @Override
              public void onResponse(StatusDetails response) {
                //dismissDialog();
                if (response != null) {
                  StatusDetails.StatusResponse[] statusResponseList = response.getDeviceStatusResponse();

                  StatusDetails.StatusResponse statusResponse = null;

                  if (statusResponseList != null && statusResponseList.length > 0) {
                    statusResponse = statusResponseList[0]; // fetch first object only
                  }

                  if (statusResponse != null) {
                    StatusDetails.DeviceStatusResponse deviceStatusResponse = statusResponse.getDeviceStatusResponse();
                    String deviceStatus = deviceStatusResponse.getDeviceStatus();

                    Log.d(TAG, "device status :- " + deviceStatus);

                    if (deviceStatus != null) {
                      if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0) {
                        selectedDevice.getProfile().setAvailable(true);
                        selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
                        Log.d(TAG, "device online..start sharing video");
                        playMotionEvent();
                      } else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_STANDBY) == 0) {
                        selectedDevice.getProfile().setAvailable(false);
                        selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);
                        Log.d(TAG, "device standby..wakeup");
                        //wakeup device
                        wakeUpRemoteDevice();
                      } else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_OFFLINE) == 0) {
                        Log.d(TAG, "setting device available false");
                        selectedDevice.getProfile().setAvailable(false);
                        selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
                        //device offline
                        if(!isPaused)
                          Toast.makeText(getApplicationContext(), getString(R.string.camera_offline), Toast.LENGTH_SHORT).show();
                      }
                    }
                  }
                }
              }
            },
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                if (error != null && error.networkResponse != null) {
                  Log.d(TAG, error.networkResponse.toString());
                  Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
                }
              }
            });
  }


  private void wakeUpRemoteDevice() {
    if (mProgressDialog != null) {
      mProgressDialog.setMessage("Camera is Waking Up");
      mProgressDialog.show();
    }
    Log.d(TAG, "wakeUpRemoteDevice");
    String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
    mDeviceWakeup = DeviceWakeup.newInstance();
    mDeviceWakeup.wakeupDevice(registrationId,accessToken,mDeviceHandler,DeviceSingleton.getInstance().getDeviceByRegId(registrationId));

  }

  private Handler mDeviceHandler = new Handler()
  {
    public void handleMessage(Message msg)
    {
      switch(msg.what)
      {
        case CommonConstants.DEVICE_WAKEUP_STATUS:
          if(mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
          }
          boolean result = (boolean)msg.obj;
          Log.d(TAG, "Device status task completed..device status:"+result);
          if(result)
          {
           playMotionEvent();
          }
          else
          {
            Log.d(TAG, "wakeup device:failure");
            if(!isPaused)
              Toast.makeText(MotionEventInteractionActivity.this,getResources().
                      getString(R.string.failed_to_start_device),Toast.LENGTH_LONG).show();
          }

          break;

      }
    }
  };

  @Override
  protected void onStop() {
    if(mDeviceWakeup != null) {
      mDeviceWakeup.cancelTask(registrationId,mDeviceHandler);
    }
    super.onStop();
  }
}
