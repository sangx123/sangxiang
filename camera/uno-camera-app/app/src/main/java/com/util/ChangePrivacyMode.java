package com.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.impl.hubble.P2pCommunicationManager;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceAttribute;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceID;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceAttributeDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceAttributeResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.GetDeviceAttributeDetails;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubble.registration.tasks.comm.UDTRequestSendRecvTask;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import base.hubble.PublicDefineGlob;
import base.hubble.ServerDeviceCommand;

/**
 * Created by aruna on 13/03/17.
 */

public class ChangePrivacyMode extends AsyncTask<Boolean, Void, Boolean> {

    private static final String TAG="ChangePrivacyMode";
    private Device mDevice;


    boolean isAnyError = false;
    private int mPosition;
    private boolean mNotificationStatus = false;
    private DeviceManager mDeviceManager;

    private String mRegId;
    private SharedPreferences mSharedPreferences;
    private Activity mActivity;
    private Handler mNotificationHandler;
    SecureConfig mSettings = HubbleApplication.AppConfig;
    String privacyMode = "0";

    public ChangePrivacyMode(Device device, int position, boolean notificationStatus, Activity activity, Handler handler){
        mDevice = device;
        mPosition = position;
        mNotificationStatus = notificationStatus;
        mActivity=activity;
        mDeviceManager = DeviceManager.getInstance(activity);
        mNotificationHandler=handler;

        mSettings = HubbleApplication.AppConfig;
        mRegId = device.getProfile().getRegistrationId();

        mSharedPreferences = mActivity.getSharedPreferences("app_config", Context.MODE_PRIVATE);
    }

    @Override
    protected Boolean doInBackground(Boolean... params) {

        if(mNotificationStatus)
            privacyMode = "1";
        else
            privacyMode = "0";

        //if(mNotificationStatus &&  CommonUtil.getSettingInfo(mActivity,mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.IS_MUSIC_PLAYING)){
            //queryMelodyStatus();
       // }
       if(mNotificationStatus) {
           if(mDevice.getProfile().melodyNotSupported())
               setPrivacyModeChanges();
           else
              queryMelodyStatus();
       }
        else
         setPrivacyModeChanges();


        return null;
    }

    private void NotificationHandleError(){
        if(mDevice.getProfile().getModelId().equalsIgnoreCase("0080")){

        }else {

            }
        }
       /* Message message = new Message();


       message.what = CommonConstants.DEVICE_NOTIFICATION_STATUS;
        message.obj = mNotificationStatus;
        message.arg1 = mPosition;
        mNotificationHandler.sendMessage(message);
    }*/


    private String sendMelodyCmdGetRes(String cmd) {
        String melodyRes = null;
        String device_ip = null;
        int device_port = -1;
        String http_pass = String.format("%s:%s", PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, PublicDefineGlob.DEFAULT_CAM_PWD);

        boolean send_via_udt = false;
        if (mDevice != null) {
            if (!mDevice.isAvailableLocally()) {
                send_via_udt = true;
            }
            device_ip = mDevice.getProfile().getDeviceLocation().getLocalIp();
            String localPort = mDevice.getProfile().getDeviceLocation().getLocalPort1();
            device_port = localPort != null && !localPort.isEmpty() ? Integer.parseInt(localPort) : 80;
        }

        Log.d(TAG, "sending melody cmd: " + cmd);
        if (send_via_udt) {
            String request = PublicDefineGlob.BM_HTTP_CMD_PART + cmd;
            if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
                melodyRes = P2pCommunicationManager.getInstance().sendCommand(new ServerDeviceCommand(request, null, null));
            } else {
                String saved_token = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                if (saved_token != null) {
                    melodyRes = UDTRequestSendRecvTask.sendRequest_via_stun2(saved_token, mDevice.getProfile().getRegistrationId(), request);
                } else {
                    Log.d(TAG, "Send melody cmd failed, user token is null");
                }
            }
        } //if (send_via_udt == true)
        else {
            final String device_address_port = device_ip + ":" + device_port;
            String http_addr = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, "/?action=command&command=", cmd);
            melodyRes = HTTPRequestSendRecvTask.sendRequest_block_for_response(http_addr, PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, http_pass);
        }

        Log.d(TAG, "send melody cmd res: " + melodyRes);

        return melodyRes;
    }


    void stopMelody(){
        int retries = 3;
        boolean isMelodyStopped = false;
        String cmd = PublicDefineGlob.SET_MELODY_OFF;
        while (retries-- > 0 && isMelodyStopped == false) {
            String cmdRes = sendMelodyCmdGetRes(cmd);
            Log.i(TAG, "ARUNA Melody cmd: " + cmd + ", response: " + cmdRes);
            if (cmdRes != null && cmdRes.startsWith(cmd)) {
                try {
                    cmdRes = cmdRes.substring(cmd.length() + 2);
                    if (cmdRes != null && cmdRes.equalsIgnoreCase("0")) {
                        Log.d(TAG, "send melody cmd: success");
                        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN, AppEvents.VF_LULLABY_STARTED, AppEvents.LULLABY_STARTED);

                        ZaiusEvent lullabyEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                        lullabyEvt.action(AppEvents.VF_LULLABY_STARTED);
                        try {
                            ZaiusEventManager.getInstance().trackCustomEvent(lullabyEvt);
                        } catch (ZaiusException e) {
                            e.printStackTrace();
                        }
                        isMelodyStopped = true;
                        setPrivacyModeChanges();
                        break;
                    } else {
                        isMelodyStopped = false;
                       // setPrivacyModeChanges();
                        //Log.e(TAG, "Send melody cmd failed, retries: " + retries);
                        //  GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_LULLABY_FAILED+" : "+retries,AppEvents.LULLABY_FAILED);

                        ZaiusEvent lullabyFailedEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                        lullabyFailedEvt.action(AppEvents.VF_LULLABY_FAILED + " : " + 1);
                        try {
                            ZaiusEventManager.getInstance().trackCustomEvent(lullabyFailedEvt);
                        } catch (ZaiusException e) {
                            e.printStackTrace();
                        }

                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!isMelodyStopped){
            Message message = new Message();
            message.what = CommonConstants.DEVICE_CAMERA_STATUS;
            message.obj = "-1";
            message.arg1 = mPosition;
            mNotificationHandler.sendMessage(message);
        }

    }

    private void queryMelodyStatus() {

        Thread worker = new Thread() {
            public void run() {

                String melody_response = null;
                melody_response = sendMelodyCmdGetRes(PublicDefineGlob.GET_MELODY_VALUE);

                Log.d(TAG, "get melody res: " + melody_response);

                    if (melody_response != null &&
                            melody_response.startsWith(PublicDefineGlob.GET_MELODY_VALUE)) {
                        melody_response = melody_response.substring(
                                PublicDefineGlob.GET_MELODY_VALUE.length() + 2);
                        try {
                            int currentMelodyIndx = Integer.parseInt(melody_response);

                            if(currentMelodyIndx > 0){
                                Log.i(TAG, "Melody is playing, stop it first");
                                stopMelody();

                            }else{
                                Log.i(TAG, "Melody is not playing");
                                setPrivacyModeChanges();

                            }
                            Log.d(TAG, "CurrentMelodyIndex:"+currentMelodyIndx);



                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                    }


            }
        };

        worker.start();
    }

    void setPrivacyModeChanges(){
        DeviceAttribute privacyAttribute = new DeviceAttribute(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId,"privacy_mode_enabled", privacyMode);
        mDeviceManager.updateDeviceAttribute(privacyAttribute, new Response.Listener<DeviceAttributeDetails>() {
            @Override
            public void onResponse(DeviceAttributeDetails response) {
                if(response.getStatus() == 200) {
                    mDevice.getProfile().getDeviceAttributes().setPrivacyMode(privacyMode);
                    if (privacyMode.equals("1")) {
                        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN, AppEvents.VF_PRIVACY_MODE_ENABLED,AppEvents.VF_PRIVACY_MODE_ENABLED);
                        ZaiusEvent vfEnableEvt = new ZaiusEvent(AppEvents.VF_PRIVACY_MODE);
                        vfEnableEvt.action(AppEvents.VF_PRIVACY_MODE_DISABLED);
                        try {
                            ZaiusEventManager.getInstance().trackCustomEvent(vfEnableEvt);
                        } catch (ZaiusException e) {
                            e.printStackTrace();
                        }
                    } else {
                        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN, AppEvents.VF_PRIVACY_MODE_DISABLED, AppEvents.VF_PRIVACY_MODE_DISABLED);
                        ZaiusEvent vfDisableEvt = new ZaiusEvent(AppEvents.VF_PRIVACY_MODE);
                        vfDisableEvt.action(AppEvents.VF_PRIVACY_MODE_ENABLED);
                        try {
                            ZaiusEventManager.getInstance().trackCustomEvent(vfDisableEvt);
                        } catch (ZaiusException e) {
                            e.printStackTrace();
                        }
                    }
                }
                String responsebody = response.getDeviceAttributeResponse().toString();
                Log.i(TAG, "ATTRIBUTE STORE SERVER RESP : " + responsebody);

                Message message = new Message();
                message.what = CommonConstants.DEVICE_CAMERA_STATUS;
                message.obj = privacyMode;
                message.arg1 = mPosition;
                mNotificationHandler.sendMessage(message);
            }
        },  new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,error.getMessage(),AppEvents.VF_PRIVACY_MODE_ERROR);
                ZaiusEvent privacyError = new ZaiusEvent(AppEvents.VF_PRIVACY_MODE_ERROR);
                privacyError.action(error.getMessage());
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(privacyError);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what = CommonConstants.DEVICE_CAMERA_STATUS;
                message.obj = "-1";
                message.arg1 = mPosition;
                mNotificationHandler.sendMessage(message);

            }
        });

        DeviceID deviceID = new DeviceID(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId);
        mDeviceManager.getDeviceAttribute(deviceID, new Response.Listener<GetDeviceAttributeDetails>() {
            @Override
            public void onResponse(GetDeviceAttributeDetails response) {
                DeviceAttributeResponse responsebody[] =  responsebody = response.getDeviceAttributeResponse();
                Log.i(TAG, "ATTRIBUTE GET SERVER RESP : " + responsebody.toString());
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {

            }
        });
    }
}
