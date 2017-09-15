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
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.PublishCommand;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.JobStatusResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.hubble.registration.PublicDefine;
import com.hubble.util.CommonConstants;

import base.hubble.PublicDefineGlob;

public class NotificationStatusTask extends AsyncTask<Boolean, Void, Boolean>
{
    private static final String TAG="NotificationStatusTask";
    private Device mDevice;
    private boolean mIsMotionEnabled = false;
    private boolean mIsSoundEnabled = false;
    private boolean mIsLowTempEnabled = false;
    private boolean mIsHiTempEnabled = false;
    private boolean mIsPIREnabled = false;

    boolean isAnyError = false;
    private int mPosition;
    private boolean mNotificationStatus = false;
    private DeviceManager mDeviceManager;
    private SecureConfig mSettings;
    private String mRegId;
    private SharedPreferences mSharedPreferences;
    private Activity mActivity;
    private Handler mNotificationHandler;
    private boolean isLocal=false;

    public NotificationStatusTask(Device device, int position, boolean notificationStatus, Activity activity, Handler handler)
    {
        mDevice = device;
        mPosition = position;
        mNotificationStatus = notificationStatus;
        mActivity=activity;
        mDeviceManager = DeviceManager.getInstance(activity);
        mNotificationHandler=handler;

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
    protected Boolean doInBackground(Boolean... params)
    {

        {
            if (CameraAvailabilityManager.getInstance().isCameraInSameNetwork(mActivity, mDevice))
                isLocal = true;
            try {
                if(mDevice.getProfile().getModelId().equalsIgnoreCase("0080"))
                {
                    setPIR(mNotificationStatus);
                }
                else if(mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72)
                        || mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS73)
                        || mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_173))
                {
                    setMotionDetection(mNotificationStatus,false);
                }
                else
                {
                    setMotionDetection(mNotificationStatus,true);
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, Log.getStackTraceString(e));
            }

        }

        return true;
    }

//    @Override
//    protected void onPostExecute(Boolean aBoolean) {
//        super.onPostExecute(aBoolean);
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                boolean result = false;
//                if(mIsSoundEnabled && mIsMotionEnabled && mIsHiTempEnabled && mIsLowTempEnabled) {
//                    result = true;
//                    mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", mNotificationStatus).commit();
//                }
//
//                Message message = new Message();
//                message.what = CommonConstants.DEVICE_NOTIFICATION_STATUS;
//                message.obj = mNotificationStatus;
//                message.arg1 = mPosition;
//                mNotificationHandler.sendMessage(message);
//            }
//        }, 15000);
//    }

    private void setMotionDetection(final boolean motionDetectionEnabled, final boolean isSettingPresent){
        String commandValue = "";
        PublishCommand setRecording=new PublishCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), mDevice.getProfile().getRegistrationId(), "set_motion_area",null);

        if(motionDetectionEnabled) {
            //commandValue = PublicDefineGlob.MOTION_ON_PARAM;
            setRecording.setMvrToggleGrid(PublicDefineGlob.MOTION_ON_GRID_VALUE);
            setRecording.setMvrToggleZone(PublicDefineGlob.MOTION_ON_ZONE_VALUE);
        }else{
            commandValue = PublicDefineGlob.MOTION_OFF_PARAM;
            setRecording.setMvrToggleGrid(PublicDefineGlob.MOTION_OFF_GRID_VALUE);
            setRecording.setMvrToggleZone(PublicDefineGlob.MOTION_OFF_ZONE_VALUE);
        }
        //SendCommand setRecording = new SendCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), mDevice.getProfile().getRegistrationId(), "set_motion_area" + commandValue);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setRecording.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(setRecording, new Response.Listener<JobStatusResponse>()
                {
                    @Override
                    public void onResponse(JobStatusResponse response1)
                    {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody + " setting present :" + isSettingPresent);

                        if (responsebody != null && responsebody.contains("set_motion_area") &&
                                responsebody.contains("0"))
                        {
                            try {
                                mIsMotionEnabled = true;
                                if(isSettingPresent) {
                                    setSoundDetection(motionDetectionEnabled);
                                }

                            } catch (Exception ex) {
                                NotificationHandleError();
                            }
                        }
                        else{
                            mIsMotionEnabled = false;
                            NotificationHandleError();
                        }

                        if(!isSettingPresent) {
                            if (mIsMotionEnabled) {
                                mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", mNotificationStatus).commit();
                                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS, mNotificationStatus);
                                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_STATUS, mNotificationStatus);
                                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_STATUS, mNotificationStatus);
                                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_STATUS, mNotificationStatus);

                                Message message = new Message();
                                message.what = CommonConstants.DEVICE_NOTIFICATION_STATUS;
                                message.obj = mNotificationStatus;
                                message.arg1 = mPosition;
                                mNotificationHandler.sendMessage(message);
                            } else {
                                mIsMotionEnabled = false;
                                NotificationHandleError();
                            }
                        }


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
                        CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_STATUS, mIsMotionEnabled);

                        NotificationHandleError();
                    }
                },isLocal

        );

    }

    private void NotificationHandleError(){
        if(mDevice.getProfile().getModelId().equalsIgnoreCase("0080")){
            if(mIsPIREnabled) {
                mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", mNotificationStatus).commit();
                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_STATUS, mNotificationStatus);
                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.SOUND_STATUS, mNotificationStatus);
                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.LOW_TEMP_STATUS, mNotificationStatus);
                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.HIGH_TEMP_STATUS, mNotificationStatus);

            }
        }else {
            if (mIsSoundEnabled && mIsMotionEnabled && mIsHiTempEnabled && mIsLowTempEnabled) {

                mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", mNotificationStatus).commit();
                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_STATUS, mNotificationStatus);
                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.SOUND_STATUS, mNotificationStatus);
                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.LOW_TEMP_STATUS, mNotificationStatus);
                CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.HIGH_TEMP_STATUS, mNotificationStatus);

            }
        }
        Message message = new Message();
        message.what = CommonConstants.DEVICE_NOTIFICATION_STATUS;
        message.obj = mNotificationStatus;
        message.arg1 = mPosition;
        mNotificationHandler.sendMessage(message);
    }

    private void setSoundDetection(final boolean soundDetectionEnabled){
        final String command;
        if (soundDetectionEnabled) {
            command = "vox_enable";
        } else {
            command = "vox_disable";
        }

       // SendCommand setSoundDetection = new SendCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId, command);
        PublishCommand setSoundDetection=new PublishCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId, command,null);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setSoundDetection.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(setSoundDetection, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains(command) && responsebody.contains("0")) {
                            mIsSoundEnabled = true;
                            setTempHighEnabled(soundDetectionEnabled);

                        }else{
                            mIsSoundEnabled = false;
                            NotificationHandleError();
                        }
                      //  CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.SOUND_STATUS, mIsSoundEnabled);

                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        mIsSoundEnabled = false;
                        if(error != null && error.networkResponse != null)
                        {
                            Log.d(TAG,error.networkResponse.toString());
                            Log.d(TAG,error.networkResponse.data.toString());
                        }
                        CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.SOUND_STATUS, mIsSoundEnabled);

                        NotificationHandleError();

                    }
                },isLocal

        );

    }

    private void setTempLowEnabled(boolean tempEnabaled){
        String tempLowEnable = tempEnabaled ? "1" : "0";
        String tempHighEnable = tempEnabaled ? "1" : "0";
        // mDevice.sendCommandGetSuccess("set_temp_lo_enabled", tempLowEnable, null);
        //SendCommand setTempLOEnabled = new SendCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId, "set_temp_lo_enable" +"&value="+tempLowEnable );
        PublishCommand setTempLOEnabled=new PublishCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId, "set_temp_lo_enable",null);
        setTempLOEnabled.setValue(tempLowEnable);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setTempLOEnabled.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(setTempLOEnabled, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "ARUNA SERVER RESP set_temp_lo_enable : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_temp_lo_enable") && responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                mIsLowTempEnabled = true;
                            } catch (Exception ex) {
                                mIsLowTempEnabled = false;
                                NotificationHandleError();
                            }

                        }else{
                            mIsLowTempEnabled = false;
                            NotificationHandleError();
                        }


                  //      CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.LOW_TEMP_STATUS, mIsLowTempEnabled);
                        if(mIsSoundEnabled && mIsMotionEnabled && mIsHiTempEnabled && mIsLowTempEnabled) {

                                    mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", mNotificationStatus).commit();
                            CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_STATUS, mNotificationStatus);
                            CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.SOUND_STATUS, mNotificationStatus);
                            CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.LOW_TEMP_STATUS, mNotificationStatus);
                            CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.HIGH_TEMP_STATUS, mNotificationStatus);


                        }

                                Message message = new Message();
                                message.what = CommonConstants.DEVICE_NOTIFICATION_STATUS;
                                message.obj = mNotificationStatus;
                                message.arg1 = mPosition;
                                mNotificationHandler.sendMessage(message);


                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {

                        if(error != null && error.networkResponse != null)
                        {
                            Log.d(TAG,"set_temp_lo_enable : "+error.networkResponse.toString());
                            Log.d(TAG,error.networkResponse.data.toString());
                        }
                        mIsLowTempEnabled = false;
                        NotificationHandleError();
                    }
                },isLocal

        );

        //  mDevice.sendCommandGetSuccess("set_temp_hi_enabled", tempHighEnable, null);



    }

    void setTempHighEnabled(final boolean tempHiEnabled){
       // SendCommand setTempHighEnable = new SendCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId, "set_temp_hi_enable"+"&value="+tempHiEnabled);
        PublishCommand setTempHighEnable=new PublishCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId, "set_temp_hi_enable",null);
        setTempHighEnable.setValue(String.valueOf(tempHiEnabled));
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setTempHighEnable.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(setTempHighEnable, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, " SERVER RESP set_temp_hi_enable : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_temp_hi_enable") && responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                mIsHiTempEnabled = true;
                                setTempLowEnabled(tempHiEnabled);
                            } catch (Exception ex) {
                                mIsHiTempEnabled = false;
                                NotificationHandleError();
                            }
                        } else {
                            mIsHiTempEnabled = false;
                            NotificationHandleError();
                        }
                      //  CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.HIGH_TEMP_STATUS, mIsHiTempEnabled);


                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {

                        if(error != null && error.networkResponse != null)
                        {
                            Log.d(TAG,"set_temp_hi_enable"+error.networkResponse.toString());
                            Log.d(TAG,error.networkResponse.data.toString());
                        }
                        mIsHiTempEnabled = false;
                        CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.HIGH_TEMP_STATUS, mIsHiTempEnabled);

                        NotificationHandleError();
                    }
                },isLocal

        );
    }

    private void setPIR(final boolean isPirEnabled){
        String pirOff = "0"; // OFF
        String pirOn = "60"; //ON
        String commandValue = "";
        mIsPIREnabled = false;
        if(isPirEnabled)
            commandValue = pirOn;
        else
            commandValue = pirOff;

        //SendCommand setRecording = new SendCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId, "set_pir_sensitivity"+"&value="+commandValue);
        PublishCommand setRecording=new PublishCommand(mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),mRegId, "set_pir_sensitivity",null);
        setRecording.setValue(commandValue);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setRecording.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(setRecording, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_pir_sensitivity")) {

                            try {
                                try{
                                    final Pair<String, Object> parsedResponse= CommonUtil.parsePublishResponseBody(responsebody);
                                  if(parsedResponse != null && parsedResponse.second instanceof Float)
                                    {
                                        mIsPIREnabled = (( (Float)parsedResponse.second == 0.0)? true : false);
                                    }
                                } catch (Exception exception) {
                                    mIsPIREnabled = false;
                                    NotificationHandleError();
                                    Log.d(TAG, exception.getMessage());
                                    exception.printStackTrace();
                                }


                            } catch (Exception ex) {
                                mIsPIREnabled = false;
                                NotificationHandleError();
                            }
                        }else{
                            mIsPIREnabled = false;
                            NotificationHandleError();
                        }

                        if(mIsPIREnabled) {


                            mSharedPreferences.edit().putBoolean(mDevice.getProfile().getName() + "notification", mNotificationStatus).commit();
                            CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_STATUS, mNotificationStatus);
                            CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.SOUND_STATUS, mNotificationStatus);
                            CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.LOW_TEMP_STATUS, mNotificationStatus);
                            CommonUtil.setSettingInfo(mActivity.getApplicationContext(), mDevice.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.HIGH_TEMP_STATUS, mNotificationStatus);

                            Message message = new Message();
                            message.what = CommonConstants.DEVICE_NOTIFICATION_STATUS;
                            message.obj = mNotificationStatus;
                            message.arg1 = mPosition;
                            mNotificationHandler.sendMessage(message);
                        }else{
                            mIsPIREnabled = false;
                            NotificationHandleError();
                        }


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
                        mIsPIREnabled = false;
                        NotificationHandleError();
                    }
                },isLocal

        );

    }
}
