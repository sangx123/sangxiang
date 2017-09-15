package com.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceID;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceStatus;
import com.hubble.framework.service.cloudclient.device.pojo.response.AvailableDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceWakeupResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.framework.service.p2p.P2pDevice;
import com.hubble.framework.service.p2p.P2pUtils;
import com.hubble.ui.DebugFragment;
import com.hubble.util.CommonConstants;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import base.hubble.database.DeviceStatusDetail;


/**
 * Created by sonikas on 22/12/16.
 */
public class DeviceWakeup {

    private static final String TAG="DeviceWakeup";

    private String mAccessToken;

    private static final long TIME_WAIT_BEFORE_POLLING_STATUS = 12 * 1000;
    private static final long TIME_WAIT_BEFORE_CHECKING_STATUS = 12 * 1000;
    private static final long POLLING_INTERVAL = 2000;
    private static final int  COUNTER_AFTER_WAKEUP = 15;
    private static final int  COUNTER_AFTER_WAKEUP_ERROR = 7;


    private int mMilliSeconds = -1;
    private static DeviceWakeup mDeviceWakeup;
    private HashMap<String,List<Handler>> mWakeUpDeviceMap;
    private HashMap<String,Integer> mDevicePosition;
    private HashMap<String,Device> mDeviceMap;


    public static DeviceWakeup newInstance()
    {
        if(mDeviceWakeup == null)
        {
            mDeviceWakeup = new DeviceWakeup();

        }
        return mDeviceWakeup;
    }

    public void setDelayP2PTask(int delayMilliSeconds)
    {
        mMilliSeconds = delayMilliSeconds;
    }
    public DeviceWakeup()
    {
        mWakeUpDeviceMap = new HashMap<String,List<Handler>>();
        mDevicePosition = new HashMap<String,Integer>();

        mDeviceMap =  new HashMap<String,Device>();
    }

    public boolean wakeupDevice(String deviceRegId,String accessToken, Handler handler,Device device)
    {
        List<Handler> wakeUpDeviceHandler = mWakeUpDeviceMap.get(deviceRegId);

        mAccessToken = accessToken;
        if(wakeUpDeviceHandler != null)
        {
            // device is already present to wakeup so don't start wakeup process again
            if(!wakeUpDeviceHandler.contains(handler))
            {
                Log.d(TAG,"Handler is not present so added in list");
                wakeUpDeviceHandler.add(handler);

                synchronized (this) {
                    mWakeUpDeviceMap.put(deviceRegId, wakeUpDeviceHandler);
                    mDeviceMap.put(deviceRegId, device);
                }
            }
            else
            {
                Log.d(TAG,"Handler is present so no need to add it");
            }
            return false;
        }
        else
        {
            List<Handler> deviceHandler = new ArrayList<Handler>();
            deviceHandler.add(handler);

            Log.d(TAG,"new HashMap  is created to add object");
            synchronized (this) {
                mWakeUpDeviceMap.put(deviceRegId, deviceHandler);
                mDeviceMap.put(deviceRegId, device);
            }
            wakeupRemoteDevice(deviceRegId);

            return true;
        }
    }

    public boolean wakeupDevice(String deviceRegID, String accessToken,Handler handler)
    {
        return wakeupDevice(deviceRegID,accessToken,handler,null);

    }

    public boolean wakeupDevice(String deviceRegId, String accessToken,Handler handler,Device device, int position)
    {
        boolean status =  wakeupDevice(deviceRegId,accessToken,handler,device);
        mDevicePosition.put(deviceRegId,position);
        return status;
    }


    private void wakeupRemoteDevice(final String deviceRegID)
    {
        final DeviceID deviceId = new DeviceID(mAccessToken, deviceRegID);

        DeviceManagerService.getInstance(HubbleApplication.AppContext).wakeUpDevice(deviceId, new Response.Listener<DeviceWakeupResponse>()
                {
                    @Override
                    public void onResponse (DeviceWakeupResponse response)
                    {

                        if (response != null)
                        {
                            DeviceWakeupResponse.DeviceWakeupDetails deviceWakeupDetails = response.getDeviceStateDetails();

                            if (deviceWakeupDetails.getDeviceStateResponse() != null)
                            {
                                if ((deviceWakeupDetails.getDeviceStateResponse().compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0) || (deviceWakeupDetails.getDeviceStateResponse().compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE_200_SUCCESS) == 0))
                                {
                                    new Handler().postDelayed(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            DeviceStatusTask  statusTask = new DeviceStatusTask(COUNTER_AFTER_WAKEUP,deviceRegID);
                                            statusTask.execute();
                                        }
                                    }, TIME_WAIT_BEFORE_POLLING_STATUS);

                                }
                                else if(deviceWakeupDetails.getDeviceStateResponse().contains(CameraStatusView.DEVICE_STATUS_RES_OFFLINE_404_STATUS))
                                {
                                    Log.d(TAG,"device offline, not connected over");

                                    new Handler().postDelayed(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            Log.d(TAG,"404 error so checking device status");
                                            DeviceStatusTask  statusTask = new DeviceStatusTask(COUNTER_AFTER_WAKEUP_ERROR,deviceRegID);
                                            statusTask.execute();
                                        }
                                    }, TIME_WAIT_BEFORE_CHECKING_STATUS);
                                }
                                else
                                {


                                    Integer devicePosition = mDevicePosition.get(deviceRegID);


                                    List<Handler> wakeUpDeviceHandler = mWakeUpDeviceMap.get(deviceRegID);
                                    if(wakeUpDeviceHandler != null)
                                    {
                                        Log.d(TAG,"Handler is not null - 1");
                                        for(int count = 0; count < wakeUpDeviceHandler.size(); count++)
                                        {
                                            Message message = new Message();
                                            message.what = CommonConstants.DEVICE_WAKEUP_STATUS;
                                            message.obj = false;

                                            if(devicePosition != null)
                                                message.arg1 = devicePosition.intValue();

                                            wakeUpDeviceHandler.get(count).sendMessage(message);
                                        }
                                    }

                                    Log.d(TAG,"Remove entry from wakeup handler - 1");

                                    mWakeUpDeviceMap.remove(deviceRegID);
                                    mDevicePosition.remove(deviceRegID);
                                    mDeviceMap.remove(deviceRegID);

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

                        Log.d(TAG,"Device wakeup failed with error " + error);
                        if (error != null && error.networkResponse != null)
                        {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));

                            if(error.networkResponse.statusCode== HttpURLConnection.HTTP_NOT_FOUND)
                            {
                                new Handler().postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        Log.d(TAG,"404 error so checking device status");
                                        DeviceStatusTask statusTask = new DeviceStatusTask(COUNTER_AFTER_WAKEUP_ERROR,deviceRegID);
                                        statusTask.execute();
                                    }
                                }, TIME_WAIT_BEFORE_CHECKING_STATUS);

                                return ;
                            }
                        }

                        Integer devicePosition = mDevicePosition.get(deviceRegID);


                        List<Handler> wakeUpDeviceHandler = mWakeUpDeviceMap.get(deviceRegID);
                        if(wakeUpDeviceHandler != null)
                        {
                            Log.d(TAG,"Handler is not null - 2");
                            for(int count = 0; count < wakeUpDeviceHandler.size(); count++)
                            {
                                Message message = new Message();
                                message.what = CommonConstants.DEVICE_WAKEUP_STATUS;
                                message.obj = false;
                                if(devicePosition != null)
                                    message.arg1 = devicePosition.intValue();

                                wakeUpDeviceHandler.get(count).sendMessage(message);
                            }
                        }

                        Log.d(TAG,"Remove entry from wakeup handler - 2");
                        mWakeUpDeviceMap.remove(deviceRegID);
                        mDevicePosition.remove(deviceRegID);
                        mDeviceMap.remove(deviceRegID);

                    }
                });
    }

    public class DeviceStatusTask extends AsyncTask<Boolean, Void, Boolean>
    {
        private boolean isOnline = false;
        boolean isAnyError = false;
        private int mCounter = 15;
        private String mRegID = null;
        private boolean isStatusRequestGoingOn = false;

        public DeviceStatusTask(int counter,String deviceRegID)
        {
            mCounter = counter;
            mRegID = deviceRegID;
        }

        @Override
        protected Boolean doInBackground(Boolean... params)
        {
            int counter = 0;
            do
            {
                try
                {
                    final Device selectedDevice = mDeviceMap.get(mRegID);
                    Log.d(TAG,"AsyncTask :- Execute Device status " + counter + " and regid " + mRegID);

                    if(selectedDevice != null)
                        Log.d(TAG,"Stun cache status :- " + selectedDevice.getProfile().isAvailable());

                    if(false && selectedDevice != null &&  !selectedDevice.getProfile().isAvailable())
                    {
                        final DeviceID deviceID = new DeviceID(mAccessToken,mRegID);

                        DeviceManagerService.getInstance(HubbleApplication.AppContext).isDeviceOnline(deviceID,
                                new Response.Listener<AvailableDetails>()
                                {
                                    @Override
                                    public void onResponse (AvailableDetails response)
                                    {
                                        if(response != null)
                                        {
                                            if(response.isAvailableStaus())
                                            {
                                                selectedDevice.getProfile().setAvailable(true);
                                                isOnline = true;
                                            }
                                        }
                                    }
                                },
                                new Response.ErrorListener()
                                {
                                    @Override
                                    public void onErrorResponse (VolleyError error)
                                    {
                                        if (error != null && error.networkResponse != null)
                                        {
                                            isAnyError = true;

                                            Log.d(TAG, error.networkResponse.toString());
                                            Log.d(TAG, "Error Message :- " +new String(error.networkResponse.data));
                                        }
                                    }
                                });

                    }
                    else
                    {
                        final DeviceStatus deviceStatus = new DeviceStatus(mAccessToken, mRegID);

                        if(!isStatusRequestGoingOn)
                        {
                            isStatusRequestGoingOn = true;

                            DeviceManagerService.getInstance(HubbleApplication.AppContext).getDeviceStatus(deviceStatus, new Response.Listener<StatusDetails>() {
                                        @Override
                                        public void onResponse(StatusDetails response) {
                                            isStatusRequestGoingOn = false;
                                            if (response != null) {
                                                StatusDetails.StatusResponse[] statusResponseList = response.getDeviceStatusResponse();

                                                StatusDetails.StatusResponse statusResponse = null;

                                                if (statusResponseList != null && statusResponseList.length > 0) {
                                                    statusResponse = statusResponseList[0]; // fetch first object only
                                                }

                                                if (statusResponse != null) {
                                                    StatusDetails.DeviceStatusResponse deviceStatusResponse = statusResponse.getDeviceStatusResponse();
                                                    String deviceStatus = deviceStatusResponse.getDeviceStatus();

                                                    Log.d(TAG, "device status :- " + deviceStatus + " and regid " + mRegID);

                                                    if (deviceStatus != null) {
                                                        if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0) {
                                                            if (selectedDevice != null)
                                                                selectedDevice.getProfile().setAvailable(true);

                                                            isOnline = true;
                                                            DeviceStatusDetail deviceStatusDetail = selectedDevice.getProfile().getDeviceStatusDetail();
                                                            deviceStatusDetail.setDeviceStatus(CameraStatusView.DEVICE_STATUS_RES_ONLINE);

                                                            final List<P2pDevice> p2pDevices = new ArrayList<>();
                                                            List<Device> cameraDevices = DeviceSingleton.getInstance().getDevices();
                                                            if (cameraDevices != null) {
                                                                for (Device cameraDevice : cameraDevices)
                                                                {
                                                                    boolean isOrbitP2PEnabled = HubbleApplication.AppConfig.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);

                                                                    // boolean isOrbitP2PEnabled = cameraDevice.getProfile().isStandBySupported();
                                                                    if (isOrbitP2PEnabled && cameraDevice.getProfile().canUseP2p() && cameraDevice.getProfile().canUseP2pRelay() &&
                                                                            !TextUtils.isEmpty(cameraDevice.getProfile().getRegistrationId())) {
                                                                        P2pDevice newDevice = new P2pDevice();
                                                                        newDevice.setRegistrationId(cameraDevice.getProfile().getRegistrationId());
                                                                        newDevice.setFwVersion(cameraDevice.getProfile().getFirmwareVersion());
                                                                        newDevice.setMacAddress(cameraDevice.getProfile().getMacAddress());
                                                                        newDevice.setModelId(cameraDevice.getProfile().getModelId());
                                                                        if (cameraDevice.getProfile().getDeviceLocation() != null) {
                                                                            newDevice.setLocalIp(cameraDevice.getProfile().getDeviceLocation().getLocalIp());
                                                                        }
                                                                        if (cameraDevice.getProfile().isStandBySupported()) {
                                                                            DeviceStatusDetail statusDetails = cameraDevice.getProfile().getDeviceStatusDetail();
                                                                            if (statusDetails != null && deviceStatusDetail.getDeviceStatus() != null) {
                                                                                String status = deviceStatusDetail.getDeviceStatus();
                                                                                if (status.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0) {
                                                                                    newDevice.setAvailable(true);
                                                                                } else {
                                                                                    newDevice.setAvailable(false);
                                                                                }
                                                                            } else {
                                                                                newDevice.setAvailable(cameraDevice.getProfile().isAvailable());
                                                                            }


                                                                        } else {
                                                                            newDevice.setAvailable(cameraDevice.getProfile().isAvailable());

                                                                        }
                                                                        p2pDevices.add(newDevice);
                                                                    }
                                                                }
                                                            } else {
                                                                Log.d(TAG, "device wakeup, device list is null");
                                                            }

                                                            if (mMilliSeconds != -1) {
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Log.d(TAG, "start P2P service-1");
                                                                        P2pUtils.startP2pService(HubbleApplication.AppContext, mAccessToken, p2pDevices);
                                                                    }
                                                                }, mMilliSeconds);
                                                            } else {
                                                                Log.d(TAG, "start P2P service-2");
                                                                P2pUtils.startP2pService(HubbleApplication.AppContext, mAccessToken, p2pDevices);
                                                            }
                                                        } else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_OFFLINE) == 0) {
                                                            Log.d(TAG, "setting device available false " + " and regid " + mRegID);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            isStatusRequestGoingOn = false;
                                            if (error != null && error.networkResponse != null) {
                                                isAnyError = true;

                                                Log.d(TAG, error.networkResponse.toString());
                                                Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
                                            }
                                        }
                                    });
                        }
                    }

                }
                catch (Exception e)
                {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                try
                {
                    if(!isOnline) {
                        // This is not correct way to check device status
                        Thread.sleep(POLLING_INTERVAL);
                    }
                }
                catch (InterruptedException e)
                {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                // increase counter.
            } while (counter++ < mCounter && !isAnyError && !isOnline && !isCancelled());


            return isOnline;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);


            Integer devicePosition = mDevicePosition.get(mRegID);


            List<Handler> wakeUpDeviceHandler = mWakeUpDeviceMap.get(mRegID);
            if(wakeUpDeviceHandler != null)
            {
                Log.d(TAG,"Handler is not null - 3" + " and regid " + mRegID + " Result : " + result);
                for(int count = 0; count < wakeUpDeviceHandler.size(); count++)
                {
                    Message message = new Message();
                    message.what = CommonConstants.DEVICE_WAKEUP_STATUS;
                    message.obj = result;

                    if(devicePosition != null)
                        message.arg1 = devicePosition.intValue();

                    wakeUpDeviceHandler.get(count).sendMessage(message);
                }
            }

            Log.d(TAG,"Remove entry from wakeup handler - 3" + " and regid " + mRegID);
            mWakeUpDeviceMap.remove(mRegID);
            mDevicePosition.remove(mRegID);
            mDeviceMap.remove(mRegID);
        }
    }

    public void cancelTask(String deviceRegId,Handler handler)
    {
        List<Handler> wakeUpDeviceHandler = mWakeUpDeviceMap.get(deviceRegId);

        Log.d(TAG,"Cancel Task called :- " + wakeUpDeviceHandler);
        if(wakeUpDeviceHandler != null && wakeUpDeviceHandler.contains(handler))
        {
            Log.d(TAG,"remove handler which was present");
            wakeUpDeviceHandler.remove(handler);

            mWakeUpDeviceMap.put(deviceRegId,wakeUpDeviceHandler);

        }
    }
}
