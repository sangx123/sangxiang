package com.util;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.hubble.util.CommonConstants;

import base.hubble.PublicDefineGlob;

public class GetNotificationStatusTask extends AsyncTask<Boolean, Void, Boolean> {
    private static final String TAG = "NotificationStatusTask";
    private Device mDevice;
    private boolean mIsMotionEnabled = false;
    private boolean mIsSoundEnabled = false;
    private boolean mIsLowTempEnabled = false;
    private boolean mIsHiTempEnabled = false;

    boolean isAnyError = false;
    private int mPosition;
    private boolean mNotificationStatus = false;
    private DeviceManager mDeviceManager;
    private SecureConfig mSettings;
    private String mRegId;
    private SharedPreferences mSharedPreferences;
    private Activity mActivity;
    private Handler mNotificationHandler = null;

    public GetNotificationStatusTask(Device device, Activity activity, Handler handler) {
        mDevice = device;

        mActivity = activity;
        mDeviceManager = DeviceManager.getInstance(activity);
        mNotificationHandler = handler;

        mSettings = HubbleApplication.AppConfig;
        mRegId = device.getProfile().getRegistrationId();
        mIsMotionEnabled = false;
        mIsSoundEnabled = false;
        mIsLowTempEnabled = false;
        mIsHiTempEnabled = false;

        mSharedPreferences = mActivity.getSharedPreferences("app_config", Context.MODE_PRIVATE);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Boolean... params) {

        {
            try {
                if(mDevice.getProfile().getModelId().equalsIgnoreCase("0080")){
                    getPIR();
                }else
                getNotificationSettings();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

        }

        return true;
    }

    private void getPIR(){

        SendCommand setRecording = new SendCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId, "value_pir_sensitivity");

        mDeviceManager.sendCommandRequest(setRecording, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("value_pir_sensitivity")) {

                            try {
                                try{
                                    final Pair<String, Object> parsedResponse= CommonUtil.parseResponseBody(responsebody);
                                    if(parsedResponse != null && parsedResponse.second instanceof Float)
                                    {
                                        mIsMotionEnabled = (( (Float)parsedResponse.second > 0.0)? true : false);
                                    }
                                } catch (Exception exception) {
                                    mIsMotionEnabled = false;
                                    NotificationHandleError();
                                    Log.d(TAG, exception.getMessage());
                                    exception.printStackTrace();
                                }


                            } catch (Exception ex) {
                                mIsMotionEnabled = false;
                                NotificationHandleError();
                            }
                        }else{
                            mIsMotionEnabled = false;
                            NotificationHandleError();
                        }
                        if(mIsMotionEnabled) {
                            //ARUNA FOR ORBIT
                            mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", true).commit();
                        }else{
                            mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", false).commit();
                        }
                            Message message = new Message();
                            message.what = CommonConstants.DEVICE_NOTIFICATION_STATUS;
                            message.obj = mNotificationStatus;
                            message.arg1 = mPosition;
                            if(mNotificationHandler != null)
                                mNotificationHandler.sendMessage(message);



                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {

                        if(error != null && error.networkResponse != null)
                        {
                            Log.d(TAG,error.networkResponse.toString());
                            Log.d(TAG,error.networkResponse.data.toString());
                        }
                        mIsMotionEnabled = false;
                        NotificationHandleError();
                    }
                }

        );

    }

    private void getNotificationSettings() {


        SecureConfig settings = HubbleApplication.AppConfig;
        String regId = mDevice.getProfile().getRegistrationId();
        SendCommand getTemp = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "camera_parameter_setting");
        mDeviceManager.sendCommandRequest(getTemp, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {

                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("camera_parameter_setting")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && response.second instanceof String) {
                                    String responseString = (String) response.second;

            /*
             * device_setting: ms=[motion status],me=[motion
             * sensitivity], vs=[vox status],vt=[vox threshold],hs=[high
             * temp detection status], ls=[high temp detection
             * status],ht=[high temp threshold],lt=[low temp threshold]
             */

                                    String individual_setting[] = responseString.split(",");
                                    if (individual_setting.length > 8) {

                                        String ms = individual_setting[0];
                                        mIsMotionEnabled = ms.equalsIgnoreCase("ms=1");

                                        String vs = individual_setting[2];
                                        mIsSoundEnabled = (vs.equalsIgnoreCase("vs=1"));


                                        String hs = individual_setting[4];
                                        mIsHiTempEnabled = (hs.equalsIgnoreCase("hs=1"));


                                        String ls = individual_setting[5];
                                        mIsLowTempEnabled = (ls.equalsIgnoreCase("ls=1"));

                                    }
                                    if (mIsSoundEnabled || mIsMotionEnabled ||  mIsHiTempEnabled || mIsLowTempEnabled) {

                                        mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", true).commit();
                                    }else if(mIsMotionEnabled == false && mIsSoundEnabled == false && mIsLowTempEnabled == false && mIsHiTempEnabled == false){
                                        mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", false).commit();
                                    }
                                    Message message = new Message();
                                    message.what = CommonConstants.DEVICE_NOTIFICATION_STATUS;
                                    message.obj = mNotificationStatus;
                                    message.arg1 = mPosition;
                                    if(mNotificationHandler != null)
                                    mNotificationHandler.sendMessage(message);
                                }
                            } catch (Exception exception) {
                                Log.d(TAG, exception.getMessage());
                                NotificationHandleError();
                            }


                        } else {
                            NotificationHandleError();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NotificationHandleError();
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }

        );

        // mListener.onNotificationSettingsReceived();
    }


    private void NotificationHandleError() {
        /*if(mDevice.getProfile().getModelId().equalsIgnoreCase("0080")){
            if (mIsMotionEnabled) {

                mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", true).commit();
            } else  {
                mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", false).commit();
            }
        }else {
            if (mIsSoundEnabled || mIsMotionEnabled || mIsHiTempEnabled || mIsLowTempEnabled) {

                mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", true).commit();
            } else if (mIsMotionEnabled == false && mIsSoundEnabled == false && mIsLowTempEnabled == false && mIsHiTempEnabled == false) {
                mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", false).commit();
            }
        }*/

        Message message = new Message();
        message.what = CommonConstants.DEVICE_NOTIFICATION_STATUS;
        message.obj = mNotificationStatus;
        message.arg1 = mPosition;
        if(mNotificationHandler != null)
            mNotificationHandler.sendMessage(message);
    }


}

