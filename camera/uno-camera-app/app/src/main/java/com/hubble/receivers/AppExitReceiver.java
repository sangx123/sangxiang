package com.hubble.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceStatus;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.framework.service.p2p.P2pService;
import com.hubble.registration.PublicDefine;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import base.hubble.PublicDefineGlob;

import static com.hubble.framework.service.p2p.P2pService.ACTION_P2P_CHANNEL_STATUS_CHANGED;
import static com.hubble.framework.service.p2p.P2pService.EXTRA_P2P_CHANNEL_REG_ID;
import static com.hubble.framework.service.p2p.P2pService.EXTRA_P2P_CHANNEL_STATUS;


/**
 * Created by sonikas on 21/12/16.
 */
public class AppExitReceiver extends BroadcastReceiver {

    public static final String APP_EXIT_INTENT = "com.hubble.receivers.AppExit";
    private static final String TAG = "AppExitReceiver";
    private boolean isLogOut = false;
    private boolean isKillApp = false;
    private int numberOfDevices = 0;
    private SecureConfig settings = HubbleApplication.AppConfig;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "  APP KILLED broadcast received");
        numberOfDevices = 0;
        if (intent.hasExtra("isLogout")) {
            isLogOut = intent.getBooleanExtra("isLogout", false);
        }else if(intent.hasExtra("isKillApp")){
            isKillApp = intent.getBooleanExtra("isKillApp", false);
        }

        String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
        List<Device> mDevices = new ArrayList<Device>();
        List<Device> allDevices = DeviceSingleton.getInstance().getDevices();
        for (Device device : allDevices) {
            if (!device.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR) || !TextUtils.isEmpty(device.getProfile().getParentId())) {
                mDevices.add(device);

            }
        }

        for (Device device : mDevices) {
            if (device.getProfile().isStandBySupported()) {
                if (isLogOut || isKillApp)
                    ++numberOfDevices;
                checkDeviceStatus(accessToken, device, context);
            }
        }
        if (isLogOut && numberOfDevices == 0) {
            Log.d(TAG, "  APP clear devcies called ");
            DeviceSingleton.getInstance().clearDevices();
            settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
        }else if(isKillApp && numberOfDevices == 0){
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }

    private void checkDeviceStatus(final String accessToken, final Device selectedDevice, final Context context) {
        Log.d(TAG, "  checkDeviceStatus");
        final DeviceStatus deviceStatus = new DeviceStatus(accessToken, selectedDevice.getProfile().getRegistrationId());

        DeviceManagerService.getInstance(context).getDeviceStatus(deviceStatus, new Response.Listener<StatusDetails>() {
                    @Override
                    public void onResponse(StatusDetails response) {
                        Log.d(TAG, "  checkDeviceStatus response : " + context);
                        if (context == null) {
                            return;
                        }
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

                                Log.d(TAG, "  device status :- " + deviceStatus);

                                if (deviceStatus != null) {
                                    if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0) {
                                        putDeviceIntoStandByMode(accessToken, selectedDevice, context);
                                    } else {
                                        if (isLogOut || isKillApp)
                                            --numberOfDevices;
                                    }
                                }
                            }
                        }

                        if (isLogOut && numberOfDevices == 0) {
                            Log.d(TAG, "  APP clear devcies called ");
                            DeviceSingleton.getInstance().clearDevices();
                            settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                        }else if(isKillApp && numberOfDevices == 0){
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isLogOut || isKillApp)
                            --numberOfDevices;

                        if (isLogOut && numberOfDevices == 0) {
                            Log.d(TAG, "  APP clear devcies called ");
                            DeviceSingleton.getInstance().clearDevices();
                            settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                        }else if(isKillApp && numberOfDevices == 0){
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }

                        if (context == null) {
                            return;
                        }

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
                        }
                    }
                });

    }

    public void putDeviceIntoStandByMode(String accessToken, final Device selectedDevice, Context context) {
        Log.d(TAG, "  putDeviceIntoStandByMode");
        SendCommand standByCommand = new SendCommand(accessToken, selectedDevice.getProfile().getRegistrationId(), "action=command&command=stand_by_mode");

        DeviceManagerService.getInstance(context).sendCommandToDevice(standByCommand,
                new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response) {
                        if (isLogOut || isKillApp)
                            --numberOfDevices;
                        float returnStatus = -1;

                        String responseBody = response.getDeviceCommandResponse().getBody().toString();
                        Log.d(TAG, "   response:" + responseBody);

                        if (response.getDeviceCommandResponse() != null && responseBody.contains("stand_by_mode")) {
                            try {
                                final Pair<String, Object> parsedResponse = CommonUtil.parseResponseBody(responseBody);

                                if (parsedResponse != null && parsedResponse.second instanceof Float) {
                                    returnStatus = ((Float) parsedResponse.second).floatValue();
                                } else if (parsedResponse != null && parsedResponse.second instanceof String) {
                                    try {
                                        returnStatus = Float.valueOf((String) parsedResponse.second);
                                    } catch (NumberFormatException e) {
                                    }
                                }
                                Log.d(TAG, "  return status:" + returnStatus);
                                if (returnStatus == 0) {
                                    // delete p2p client if present for orbit
                                    if(selectedDevice.getProfile().getRegistrationId() != null) {
                                        P2pService.destroyP2pClientsDevice(selectedDevice.getProfile().getRegistrationId());
                                    }


                                    Log.d(TAG, "  APP KILLED putDeviceIntoStandByMode:success");

                                } else {
                                    Log.d(TAG, "  APP KILLED putDeviceIntoStandByMode:failed");
                                }
                            } catch (Exception exception) {
                                Log.d(TAG, "  APP KILLED Exception :failed");
                                Log.d(TAG, exception.getMessage());
                            }
                        }

                        if (isLogOut && numberOfDevices == 0) {
                            Log.d(TAG, "  APP clear devcies called ");
                            DeviceSingleton.getInstance().clearDevices();
                        }else if(isKillApp && numberOfDevices == 0){
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isLogOut || isKillApp)
                            --numberOfDevices;
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, "  APP KILLED Error Message :- " + new String(error.networkResponse.data));
                        }
                        if (isLogOut && numberOfDevices == 0) {
                            Log.d(TAG, "  APP clear devcies called ");
                            DeviceSingleton.getInstance().clearDevices();
                            settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                        }else if(isKillApp && numberOfDevices == 0){
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    }
                }
        );
    }
}
