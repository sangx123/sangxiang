package com.nxcomm.blinkhd.actors;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.crittercism.app.Crittercism;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.actors.Actor;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.events.SendCommandEvent;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.networkinterface.v1.EndPoint;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.PublishCommand;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.JobStatusResponse;
import com.hubble.framework.service.cloudclient.job.pojo.response.GetJobDetail;
import com.hubble.model.MobileSupervisor;
import com.hubble.model.VideoBandwidthSupervisor;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.util.ListChild;
import com.nxcomm.blinkhd.ui.CameraSettingsActivity;
import com.nxcomm.blinkhd.ui.Global;
import com.nxcomm.blinkhd.util.NotificationSettingUtils;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import com.hubbleconnected.camera.BuildConfig;
import com.hubbleconnected.camera.R;

/**
 * Created by brennan on 15-07-17.
 */
public class CameraSettingsActor extends Actor {

  private static final String TAG = "CameraSettingsActor";

  private Context mContext;
  private Device mDevice;
  private Interface mListener;
  int responseCode = -1;
  private DeviceManager mDeviceManager;
  private SecureConfig settings;
  private String regId;
  private boolean mIsOrbit=false;

  private static final int DEVICE_VOLUME_RANGE_100_LL  = 30;
  private static final int DEVICE_VOLUME_RANGE_SEPARATOR = 10;
  private static final int DEVICE_VOLUME_RANGE_100_UL = 100;

  private boolean isLocal = false;


    public CameraSettingsActor(Context context, Device device, Interface listener) {
        super();
        mContext = context;
        mDevice = device;
        mListener = listener;
        if(mDevice!=null && mDevice.getProfile()!=null)
            regId=mDevice.getProfile().registrationId;
    }



    public CameraSettingsActor(Context context, Device device, Interface listener, boolean local) {
        super();
        mContext = context;
        mDevice = device;
        mListener = listener;
        if (mDevice != null && mDevice.getProfile() != null)
            regId = mDevice.getProfile().registrationId;

        isLocal = local;
    }

    public void setDeviceLocal(boolean local) {
        isLocal = local;
    }

    //Do not use this constructor from anywhere other than camera setup
    public CameraSettingsActor(Context context, String regId, Interface listener) {
        super();
        mContext = context;
        this.regId = regId;
        mListener = listener;
        mDevice = DeviceSingleton.getInstance().getDeviceByRegId(regId);

    }

    public void setIsOrbit(boolean isOrbit) {
        mIsOrbit = isOrbit;
    }

    @Override
    public Object receive(Object m) {
        Log.d(TAG, "actor received message: " + m.getClass().getSimpleName());
        mDeviceManager = DeviceManager.getInstance(mContext);
        settings = HubbleApplication.AppConfig;
        if (mDevice != null && mDevice.getProfile() != null)
            regId = mDevice.getProfile().getRegistrationId();

        Crittercism.leaveBreadcrumb(TAG + " actor received message: " + m.getClass().getSimpleName());
        if (m instanceof ActorMessage.GetAdaptiveQuality) {
            getAdaptiveQuality(((ActorMessage.GetAdaptiveQuality) m).listChild);
        } else if (m instanceof ActorMessage.GetBrightness) {
            getBrightness(((ActorMessage.GetBrightness) m).listChild);
        } else if (m instanceof ActorMessage.GetLensCorrection) {
            getLensCorrection(((ActorMessage.GetLensCorrection) m).listChild);
        } else if (m instanceof ActorMessage.SetLensCorrection) {
            ActorMessage.SetLensCorrection msg = (ActorMessage.SetLensCorrection) m;
            setLensCorrection(msg.listChild, msg.value);
        } else if (m instanceof ActorMessage.GetVideoRecordingDuration) {
            getVideoRecordingDuration(((ActorMessage.GetVideoRecordingDuration) m).listChild);
        } else if (m instanceof ActorMessage.SetVideoRecordingDuration) {
            ActorMessage.SetVideoRecordingDuration msg = (ActorMessage.SetVideoRecordingDuration) m;
            setVideoRecordingDuration(msg.listChild, msg.value);
        } else if (m instanceof ActorMessage.GetMCUVersion) {
            GetMCUVersion(((ActorMessage.GetMCUVersion) m).listChild);
        } else if (m instanceof ActorMessage.GetCeilingMount) {
            getCeilingMount(((ActorMessage.GetCeilingMount) m).listChild);
        } else if (m instanceof ActorMessage.GetContrast) {
            getContrast(((ActorMessage.GetContrast) m).listChild);
        } else if (m instanceof ActorMessage.GetLEDFlicker) {
            getLEDFlicker(((ActorMessage.GetLEDFlicker) m).listChild);
        } else if (m instanceof ActorMessage.GetNightVision) {
            if (mDevice != null && mDevice.getProfile() != null && mDevice.getProfile().isVTechCamera()) {
                getNightVision(((ActorMessage.GetNightVision) m).listChild);
            } else {
                getNightVisionHubble((ActorMessage.GetNightVision) m);
            }
        } else if (m instanceof ActorMessage.GetNotificationSettings) {
            ActorMessage.GetNotificationSettings msg = (ActorMessage.GetNotificationSettings) m;
            getNotificationSettings(msg.motionDetection, msg.soundDetection, msg.temperature);
        } else if (m instanceof ActorMessage.GetSetting2Settings) {
            getSetting2Settings((ActorMessage.GetSetting2Settings) m);
        } else if (m instanceof ActorMessage.GetPark) {
            getPark();
        } else if (m instanceof ActorMessage.GetSlaveFirmware) {
            getSlaveFirmware(((ActorMessage.GetSlaveFirmware) m).slaveFirmware);
        } else if (m instanceof ActorMessage.GetStatusLED) {
            getStatusLED(((ActorMessage.GetStatusLED) m).listChild);
        } else if (m instanceof ActorMessage.GetTimeZone) {
            getTimeZone(((ActorMessage.GetTimeZone) m).timezone);
        } else if (m instanceof ActorMessage.GetVolume) {
            getVolume(((ActorMessage.GetVolume) m).listChild);
        } else if (m instanceof ActorMessage.GetSOCVersion) {
            getSOCVersion(((ActorMessage.GetSOCVersion) m).listChild);
        } else if (m instanceof ActorMessage.GetOverlayDate) {
            getOverlayDate(((ActorMessage.GetOverlayDate) m).listChild);
        } else if (m instanceof ActorMessage.GetCameraBattery) {
            getCameraBattery(((ActorMessage.GetCameraBattery) m).listChild);
        } else if (m instanceof ActorMessage.SetAdaptiveQuality) {
            ActorMessage.SetAdaptiveQuality msg = (ActorMessage.SetAdaptiveQuality) m;
            setAdaptiveQuality(msg.listChild, msg.isAdaptive, msg.resolutionValue, msg.bitrateValue);
        } else if (m instanceof ActorMessage.SetBrightness) {
            ActorMessage.SetBrightness msg = (ActorMessage.SetBrightness) m;
            setBrightness(msg.listChild, msg.brightnessLevel);
        } else if (m instanceof ActorMessage.SetCeilingMount) {
            ActorMessage.SetCeilingMount msg = (ActorMessage.SetCeilingMount) m;
            setCeilingMount(msg.listChild, msg.orientation);
        } else if (m instanceof ActorMessage.SetContrast) {
            ActorMessage.SetContrast msg = (ActorMessage.SetContrast) m;
            setContrast(msg.listChild, msg.contrastLevel);
        } else if (m instanceof ActorMessage.SetLEDFlicker) {
            ActorMessage.SetLEDFlicker msg = (ActorMessage.SetLEDFlicker) m;
            setLEDFlicker(msg.listChild, msg.ledHz);
        } else if (m instanceof ActorMessage.SetMotionDetection) {
            ActorMessage.SetMotionDetection msg = (ActorMessage.SetMotionDetection) m;
            setMotionDetection(msg.listChild, msg.motionDetectionEnabled, msg.motionDetectionLevel);
        } else if (m instanceof ActorMessage.SetMotionDetectionVda) {
            ActorMessage.SetMotionDetectionVda msg = (ActorMessage.SetMotionDetectionVda) m;
            setMotionDetectionVda(msg.listChild, msg.position, msg.motionDetectionLevel, msg.prevPosition);
        } else if (m instanceof ActorMessage.SetMotionSource) {
            ActorMessage.SetMotionSource msg = (ActorMessage.SetMotionSource) m;
            SetMotionSource(msg.listChild, msg.motionSource);
        } else if (m instanceof ActorMessage.SetPark) {
            ActorMessage.SetPark msg = (ActorMessage.SetPark) m;
            setPark(msg.listChild, msg.isEnabled, msg.parkTimer);
        } else if (m instanceof ActorMessage.SetNightVision) {
            ActorMessage.SetNightVision msg = (ActorMessage.SetNightVision) m;
            setNightVision(msg.listChild, msg.nightVisionMode, msg.nightVisionIntensity);
        } else if (m instanceof ActorMessage.SetSoundDetection) {
            ActorMessage.SetSoundDetection msg = (ActorMessage.SetSoundDetection) m;
            setSoundDetection(msg.listChild, msg.soundDetectionEnabled);
        } else if (m instanceof ActorMessage.SetStatusLED) {
            ActorMessage.SetStatusLED msg = (ActorMessage.SetStatusLED) m;
            setStatusLED(msg.listChild, msg.ledOn);
        } else if (m instanceof ActorMessage.SetTemperatureDetection) {
            ActorMessage.SetTemperatureDetection msg = (ActorMessage.SetTemperatureDetection) m;
            setTemperatureDetection(msg.listChild, msg.lowEnabled);
        } else if (m instanceof ActorMessage.SetTimeZone) {
            ActorMessage.SetTimeZone msg = (ActorMessage.SetTimeZone) m;
            setTimeZone(msg.listChild, msg.timezone);
        } else if (m instanceof ActorMessage.SetVolume) {
            ActorMessage.SetVolume msg = (ActorMessage.SetVolume) m;
            setVolume(msg.listChild, msg.volumeLevel);
        } else if (m instanceof ActorMessage.SetNightVisionHubble) {
            ActorMessage.SetNightVisionHubble msg = (ActorMessage.SetNightVisionHubble) m;
            setNightVisionHubble(msg.listChild, msg.nightVisionMode, msg.useCommandIR);
        } else if (m instanceof ActorMessage.SetOverlayDate) {
            ActorMessage.SetOverlayDate msg = (ActorMessage.SetOverlayDate) m;
            setOverlayDate(msg.listChild, msg.on);
        } else if (m instanceof ActorMessage.SetViewMode) {
            ActorMessage.SetViewMode msg = (ActorMessage.SetViewMode) m;
            setViewMode(msg.vm, msg.qos);
        } else if (m instanceof ActorMessage.GetViewMode) {
            ActorMessage.GetViewMode msg = (ActorMessage.GetViewMode) m;
            getViewMode(msg.vm, msg.qos);
        } else if (m instanceof ActorMessage.SetMotionNotification) {
            ActorMessage.SetMotionNotification msg = (ActorMessage.SetMotionNotification) m;
            setMotionNotification(msg.listChild, msg.motionDetectionEnabled);
        } else if (m instanceof ActorMessage.SetMotionSentivity) {
            ActorMessage.SetMotionSentivity msg = (ActorMessage.SetMotionSentivity) m;
            setMotionSentivity(msg.listChild, msg.motionDetectionLevel);
        } else if (m instanceof ActorMessage.SetSoundThreshold) {
            ActorMessage.SetSoundThreshold msg = (ActorMessage.SetSoundThreshold) m;
            setSoundThreshold(msg.listChild, msg.soundDetectionThreshold);
        } else if (m instanceof ActorMessage.SetLowTemperatureThreshold) {
            ActorMessage.SetLowTemperatureThreshold msg = (ActorMessage.SetLowTemperatureThreshold) m;
            setLowTemperatureThreshold(msg.listChild, msg.lowThreshold);
        } else if (m instanceof ActorMessage.SetHighTemperatureThreshold) {
            ActorMessage.SetHighTemperatureThreshold msg = (ActorMessage.SetHighTemperatureThreshold) m;
            setHighTemperatureThreshold(msg.listChild, msg.highThreshold);
        } else if (m instanceof ActorMessage.SetQualityOfService) {
            ActorMessage.SetQualityOfService msg = (ActorMessage.SetQualityOfService) m;
            setQualityOfService(msg.listChild, msg.position);
        } else if (m instanceof ActorMessage.GetQualityOfService) {
            ActorMessage.GetQualityOfService msg = (ActorMessage.GetQualityOfService) m;
            getQualityOfService(msg.listChild, msg.viewMode);
        } else if (m instanceof ActorMessage.SetNightLight) {
            ActorMessage.SetNightLight msg = (ActorMessage.SetNightLight) m;
            setNightLight(msg.listChild, msg.mode);
        } else if (m instanceof ActorMessage.GetNightLight) {
            ActorMessage.GetNightLight msg = (ActorMessage.GetNightLight) m;
            getNightLight(msg.listChild);
        }
        return null;
    }

    // Getters
    //TODO test
    private void getAdaptiveQuality(final ListChild listChild) {
        DeviceManager mDeviceManager;

        mDeviceManager = DeviceManager.getInstance(mContext);
        SecureConfig settings = HubbleApplication.AppConfig;
        String regId = mDevice.getProfile().getRegistrationId();
        // SendCommand getAdaptiveQuality = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "get_adaptive_bitrate");
        PublishCommand getAdaptiveQuality = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_adaptive_bitrate", null);

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getAdaptiveQuality.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(getAdaptiveQuality, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("get_adaptive_bitrate")) {

                            try {
                                final Pair<String, Object> adaptiveResponse = CommonUtil.parsePublishResponseBody(responsebody);
                                if (adaptiveResponse != null && adaptiveResponse.second instanceof String) {
                                    final boolean isActivated = adaptiveResponse.second.equals("on");
                                    listChild.booleanValue = isActivated;

                                    if (mDevice.getProfile().isVTechCamera()) {
                                        getResolution(listChild);
                                    }

                                    getBitrate(listChild);
                                    listChild.value = getAdaptiveQualityStringFromValues(listChild);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }, isLocal
        );

    }


    private void getLensCorrection(final ListChild listChild) {
        // SendCommand getLensCorrection = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
        //         regId, PublicDefine.GET_LDC_STATUS);
        PublishCommand getLensCorrection = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
                regId, PublicDefine.GET_LDC_STATUS, null);

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getLensCorrection.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }


        mDeviceManager.publishCommandRequest(getLensCorrection, new Response.Listener<JobStatusResponse>() {
                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();

                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "SERVER RESP : " + responsebody);

                        if (responsebody != null && responsebody.contains(PublicDefine.GET_LDC_STATUS)) {
                            try {
                                Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                if (response != null && response.second instanceof Integer) {
                                    handleLensCorrection(listChild, (Integer) response.second);
                                } else if (response != null && response.second instanceof Float) {
                                    handleLensCorrection(listChild, ((Float) response.second).intValue());
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);

                            } catch (Exception ex) {
                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                    }
                }, isLocal);

    }

    private void GetMCUVersion(final ListChild listChild) {
        //SendCommand getMCUVersion = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
        //        regId, PublicDefine.GET_MCU_VERSION);
        PublishCommand getMCUVersion = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
                regId, PublicDefine.GET_MCU_VERSION, null);


        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getMCUVersion.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(getMCUVersion, new Response.Listener<JobStatusResponse>() {
                    @Override
                    public void onResponse(JobStatusResponse response) {
                        String responsebody = response.getData().getOutput().getResponseMessage();


                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "Server Response :- " + responsebody);

                        if (responsebody != null && responsebody.contains(PublicDefine.GET_MCU_VERSION)) {
                            try {
                                Pair<String, Object> pairResponse = CommonUtil.parsePublishResponseBody(responsebody);

                                if (pairResponse != null && pairResponse.second instanceof String) {
                                    handleMCUVersion(listChild, (String) pairResponse.second);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);

                            } catch (Exception ex) {
                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                    }
                }, isLocal);

    }


    private void getVideoRecordingDuration(final ListChild listChild) {
        //SendCommand getVideoRecordingDuration = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
        //        regId, PublicDefine.GET_RECORDING_DURATION);
        PublishCommand getVideoRecordingDuration = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
                regId, PublicDefine.GET_RECORDING_DURATION, null);

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getVideoRecordingDuration.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }


        mDeviceManager.publishCommandRequest(getVideoRecordingDuration, new Response.Listener<JobStatusResponse>() {
                    @Override
                    public void onResponse(JobStatusResponse response) {
                        String responsebody = response.getData().getOutput().getResponseMessage();

                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "Server Response :- " + responsebody);

                        if (responsebody != null && responsebody.contains(PublicDefine.GET_RECORDING_DURATION)) {
                            try {
                                Pair<String, Object> pairResponse = CommonUtil.parsePublishResponseBody(responsebody);

                                if (pairResponse != null && pairResponse.second instanceof Integer) {
                                    handleVideoRecordingDuration(listChild, (Integer) pairResponse.second);
                                } else if (pairResponse != null && pairResponse.second instanceof Float) {
                                    handleVideoRecordingDuration(listChild, ((Float) pairResponse.second).intValue());
                                } else {
                                    listChild.intValue = -1;
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);

                            } catch (Exception ex) {
                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                    }
                }, isLocal);
    }


    //this method is not used , hence not changed to PublishCommand API
    private void getBrightness(final ListChild listChild) {
        SendCommand getBrightness = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_brightness");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getBrightness.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }


        mDeviceManager.sendCommandRequest(getBrightness, new Response.Listener<SendCommandDetails>() {


                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_brightness")) {

                            try {

                                Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && response.second instanceof Integer) {
                                    handleGenericValue(listChild, (int) response.second, true);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {

                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }, isLocal
        );

    }

    private void getSOCVersion(final ListChild listChild) {
        String gatewayIp = Util.getGatewayIp(mContext);
        //SendCommand getSOCVersion = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "get_soc_version");
        PublishCommand getSOCVersion = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_soc_version", null);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getSOCVersion.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(getSOCVersion, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("get_soc_version")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                if (response != null && response.second.toString().matches("(.*)_(.*)")) {
                                    listChild.value = response.second.toString().substring(response.second.toString().indexOf("_") + 1);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);

                            } catch (Exception ex) {

                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        mListener.onDataSetChanged(listChild);
                    }
                }, isLocal
        );

    }

    //TODO test
    private synchronized void getOverlayDate(final ListChild listChild) {
        //SendCommand getOverlayDate = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "overlay_date_get");
        PublishCommand getOverlayDate = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "overlay_date_get", null);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getOverlayDate.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(getOverlayDate, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("overlay_date_get")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);

                                if (response != null && response.second instanceof Float) {

                                    handleCeilingMountValue(listChild, Math.round((Float) response.second));
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                        listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                        mListener.onDataSetChanged(listChild);

                    }
                }, isLocal

        );

    }

    private void handleGenericValue(ListChild listChild, int value,
                                    boolean markNegativeNumberAsFailed) {
        listChild.intValue = value;
        if (listChild.intValue < 0 && markNegativeNumberAsFailed) {
            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
        } else {
            listChild.value = String.valueOf(listChild.intValue);
            //if(listChild.equals()) s/ ARUNA TODO  when contrast is added check for childvalue and add corresponding key value
            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.BRIGHTNESS, listChild.intValue);

        }
    }

    //this method is not used , hence not changed to PublishCommand API
    private void getCeilingMount(final ListChild listChild) {

        SendCommand getCeilingMount = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "value_flipup");
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getCeilingMount.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.sendCommandRequest(getCeilingMount, new Response.Listener<SendCommandDetails>() {

            @Override
            public void onResponse(SendCommandDetails response1) {
                String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                Log.i(TAG, "SERVER RESP : " + responsebody);
                if (response1.getDeviceCommandResponse() != null && responsebody.contains("value_flipup")) {

                    try {
                        final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                        if (response != null && response.second instanceof Float) {
                            handleCeilingMountValue(listChild, Math.round((Float) response.second));
                        } else if (response != null && response.second instanceof Integer) {
                            handleCeilingMountValue(listChild, (Integer) response.second);
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                        }
                        mListener.onDataSetChanged(listChild);
                    } catch (Exception ex) {

                    }
                } else {
                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                    mListener.onDataSetChanged(listChild);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error != null && error.networkResponse != null) {
                    Log.d(TAG, error.networkResponse.toString());
                    Log.d(TAG, error.networkResponse.data.toString());
                }

                listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                mListener.onDataSetChanged(listChild);

            }
        }, isLocal);

    }

    //this method is not used , hence not changed to PublishCommand API
    private void getNightLight(final ListChild listChild) {


        SendCommand getNL = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_night_light_status");


        mDeviceManager.sendCommandRequest(getNL, new Response.Listener<SendCommandDetails>() {
                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_night_light_status")) {
                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && (response.second instanceof Float)) {
                                    int value = (int) ((Float) response.second).floatValue();
                                    listChild.intValue = value;
                                    listChild.value = getSafeString(R.string.night_light);
                                    handleNightLight(listChild, value);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        listChild.intValue = 0;
                        listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                        mListener.onDataSetChanged(listChild);

                    }
                },isLocal

        );

    }

    private void handleCeilingMountValue(ListChild listChild, int value) {
        final boolean isActivated = value == 1;
        listChild.booleanValue = isActivated;
        listChild.value = isActivated ? getSafeString(R.string.on) : getSafeString(R.string.off);
        if (listChild.title.equals(getSafeString(R.string.ceiling_mount)))
            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.CEILING_MOUNT, isActivated);
        else if (listChild.title.equals(getSafeString(R.string.overlay_date)))
            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.OVERLAY_DATE, isActivated);

    }

    private void handleNightLight(ListChild listChild, int value) {
        listChild.intValue = value;
        if (listChild.title.equals(getSafeString(R.string.night_light)))
            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_LIGHT, value);
    }


    private void setLensCorrection(final ListChild listChild, final int value1) {
        //  boolean success = mDevice.sendCommandGetSuccess("set_contrast", contrastLevel + "", null);

        // SendCommand setContrast = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, PublicDefine.SET_LDC_STATUS + "&value=" +value1);
        PublishCommand setLensCorrection = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, PublicDefine.SET_LDC_STATUS, null);
        setLensCorrection.setValue(value1 + "");


        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setLensCorrection.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(setLensCorrection, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        boolean success = false;
                        String responsebody = response1.getData().getOutput().getResponseMessage();

                        if (BuildConfig.DEBUG)
                            Log.i(TAG, "SERVER RESP : " + responsebody);

                        if (responsebody != null && responsebody.contains(PublicDefine.SET_LDC_STATUS) &&
                                responsebody.contains("0")) {
                            try {
                                int value = -1;
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                if (response != null && response.second instanceof Integer) {
                                    value = (int) response.second;
                                } else if (response != null && response.second instanceof Float) {
                                    value = ((Float) response.second).intValue();
                                }
                                if (value == 0) {
                                    success = true;
                                } else {
                                    success = false;
                                }

                            } catch (Exception ex) {

                            }
                        } else {
                            success = false;
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_LENS_CORRECTION, success);
                        EventBus.getDefault().post(sendCommandEvent);

                        if (value1 == 1)
                            handleSetterResponse(listChild, success, R.string.lens_correction_applied, R.string.lens_correction_failed);
                        else
                            handleSetterResponse(listChild, success, R.string.lens_correction_removed, R.string.lens_correction_failed);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_LENS_CORRECTION, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.lens_correction_applied, R.string.lens_correction_failed);

                    }
                }, isLocal
        );


    }

    private void setVideoRecordingDuration(final ListChild listChild, int value) {

        //SendCommand setVideoRecording = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
        //       regId, PublicDefine.SET_RECORDING_DURATION + "&value=" +value);
        PublishCommand setVideoRecording = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, PublicDefine.SET_RECORDING_DURATION, null);
        setVideoRecording.setValue(value + "");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setVideoRecording.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setVideoRecording, new Response.Listener<JobStatusResponse>() {


                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        boolean success = false;

                        String responsebody = response1.getData().getOutput().getResponseMessage();


                        if (BuildConfig.DEBUG)
                            Log.i(TAG, "Server Response : " + responsebody);

                        if (responsebody != null && responsebody.contains(PublicDefine.SET_RECORDING_DURATION) &&
                               responsebody.contains("0")) {
                            try {
                                int value = -1;
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);

                                if (response != null && response.second instanceof Integer) {
                                    value = (int) response.second;
                                } else if (response != null && response.second instanceof Float) {
                                    value = ((Float) response.second).intValue();
                                }
                                if (value == 0) {
                                    success = true;
                                } else {
                                    success = false;
                                }

                            } catch (Exception ex) {

                            }
                        } else {
                            success = false;
                        }

                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_VIDEO_RECORDING_DURATION, success);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, success, R.string.video_recording_duration_applied,
                                R.string.video_recording_duration_failed);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_VIDEO_RECORDING_DURATION, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.contrast_changed, R.string.lens_correction_failed);

                    }
                }, isLocal
        );


    }

    private void handleLensCorrection(ListChild listChild, int value) {
        final boolean isActivated = value == 1;
        listChild.booleanValue = isActivated;

        listChild.value = isActivated ? getSafeString(R.string.on) : getSafeString(R.string.off);
        CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LENS_CORRECTION, isActivated);

    }

    private void handleMCUVersion(ListChild listChild, String value) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "mcu version :-" + value);

        listChild.value = value;
        CommonUtil.setMCUVersion(mContext, mDevice.getProfile().getRegistrationId() + "-" + mDevice.getProfile().getFirmwareVersion() + "-" + SettingsPrefUtils.MCU_VERSION, value);
    }

    private void handleVideoRecordingDuration(ListChild listChild, int value) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "video recording duration :" + value);

        listChild.intValue = value;
        listChild.value = "" + value;

        CommonUtil.setVideoRecording(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_RECORDING, value);

    }

    //this method is not used , hence not changed to PublishCommand API
    private void getContrast(final ListChild listChild) {
        SendCommand getContrast = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_contrast");
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getContrast.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.sendCommandRequest(getContrast, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_contrast")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && response.second instanceof Integer) {
                                    handleGenericValue(listChild, (int) response.second, false);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {

                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                        mListener.onDataSetChanged(listChild);


                    }
                }, isLocal
        );
    }

    //this method is not used , hence not changed to PublishCommand API
    private void getCameraBattery(final ListChild listChild) {
        SendCommand getCameraBattery = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_battery_percent");

        mDeviceManager.sendCommandRequest(getCameraBattery, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_battery_percent")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && response.second instanceof Integer) {
                                    listChild.intValue = (int) response.second;
                                    if (listChild.intValue < 0) {
                                        listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                    } else {
                                        listChild.value = String.valueOf(listChild.intValue) + "%";
                                    }
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {

                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                        listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                        mListener.onDataSetChanged(listChild);

                    }
                }

        );

    }

    //this method is not used , hence not changed to PublishCommand API
    private void getLEDFlicker(final ListChild listChild) {

        SendCommand getLEDFlicker = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_flicker");

        mDeviceManager.sendCommandRequest(getLEDFlicker, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_flicker")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && response.second instanceof Integer) {
                                    listChild.intValue = (Integer) response.second;
                                    listChild.value = listChild.intValue == 60 ? getSafeString(R.string.led_flicker_sixty_hertz) : getSafeString(R.string.led_flicker_fifty_hertz);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {

                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                        listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                        mListener.onDataSetChanged(listChild);

                    }
                }

        );

    }

    //this method is not used , hence not changed to PublishCommand API
    private void getNightVision(final ListChild listChild) {


        SendCommand getNightVision = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_night_vision");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getNightVision.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.sendCommandRequest(getNightVision, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_night_vision")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && response.second instanceof Integer) {
                                    handleNightVisionValue(listChild, (Integer) response.second);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }

                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {

                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                        mListener.onDataSetChanged(listChild);
                    }
                }, isLocal
        );

    }

    //this method is not used , hence not changed to PublishCommand API
    private void handleNightVisionValue(final ListChild listChild, int value) {
        listChild.intValue = value;
        listChild.value = getNightVisionStringFromIntValue(value);

        SendCommand nightVisionValue = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_ir_pwm");

        mDeviceManager.sendCommandRequest(nightVisionValue, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_ir_pwm") &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && response.second instanceof Integer) {
                                    final int nightVisionIntensity = (Integer) response.second;
                                    listChild.secondaryIntValue = nightVisionIntensity;
                                }
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }
        );

    }

    //this method is not used , hence not changed to PublishCommand API
    private void getNightVisionHubble(ActorMessage.GetNightVision actorMessage) {
        final ListChild listChild = actorMessage.listChild;
        final String command = actorMessage.useCommandIR ? "get_ir_mode" : "get_night_vision";
        SendCommand getTemp = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, command);

        mDeviceManager.sendCommandRequest(getTemp, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains(command)) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && response.second instanceof Integer) {
                                    handleNightVisionValueForHubble(listChild, (Integer) response.second);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);

                            } catch (Exception ex) {

                            }
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            mListener.onDataSetChanged(listChild);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                        mListener.onDataSetChanged(listChild);
                    }
                }
        );

    }

    private void handleNightVisionValueForHubble(ListChild listChild, int value) {
        listChild.intValue = value;
        listChild.value = getNightVisionStringFromIntValue(value);
    }

    private synchronized void getNotificationSettings(final ListChild motionDetection, final ListChild soundDetection, final ListChild temperature) {

        DeviceManager mDeviceManager;

        mDeviceManager = DeviceManager.getInstance(mContext);
        SecureConfig settings = HubbleApplication.AppConfig;
        String regId = mDevice.getProfile().getRegistrationId();


        if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)) {
            // SendCommand getTemp = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "value_pir_sensitivity");
            PublishCommand getTemp = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "value_pir_sensitivity", null);
            if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                getTemp.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
            }
            mDeviceManager.publishCommandRequest(getTemp, new Response.Listener<JobStatusResponse>() {

                        @Override
                        public void onResponse(JobStatusResponse response1) {

                            String responsebody = response1.getData().getOutput().getResponseMessage();
                            Log.i(TAG, "SERVER RESP : " + responsebody);
                            if (responsebody != null && responsebody.contains("value_pir_sensitivity")) {

                                try {
                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                    if (response != null && response.second instanceof Float) {
                                        int motionDetectThreshold = ((Float) response.second).intValue();

                                        if (motionDetectThreshold <= 0) {
                                            motionDetection.booleanValue = false;
                                            motionDetectThreshold = 0;
                                        } else if (motionDetectThreshold <= 30) {
                                            motionDetection.booleanValue = true;
                                            motionDetectThreshold = 1;
                                        } else if (motionDetectThreshold <= 60) {
                                            motionDetection.booleanValue = true;
                                            motionDetectThreshold = 2;
                                        } else {
                                            motionDetection.booleanValue = true;
                                            motionDetectThreshold = 4;
                                        }

                                        motionDetection.intValue = motionDetectThreshold;
                                        CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS, motionDetection.booleanValue);
                                        CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SENSITIVITY, motionDetectThreshold);

                                        if (Util.isThisVersionGreaterThan(mDevice.getProfile().getFirmwareVersion(), PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)) {
                                            if (motionDetectThreshold <= 0) {
                                                mListener.onNotificationSettingsReceived();
                                            } else {
                                                Log.d(TAG,"Orbit on plan call get recording parameter");
                                                getRecordingParameter(motionDetection);
                                            }
                                        } else {
                                            mListener.onNotificationSettingsReceived();
                                        }
                                    }else {
                                        if (motionDetection != null) {
                                            motionDetection.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                        }
                                        mListener.onNotificationSettingsReceived();
                                    }

                                } catch (Exception exception) {
                                    Log.d(TAG, exception.getMessage());
                                    mListener.onNotificationSettingsReceived();
                                }

                            } else {
                                if (motionDetection != null) {
                                    motionDetection.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }

                                mListener.onNotificationSettingsReceived();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mListener.onNotificationSettingsReceived();
                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }

                        }
                    }, isLocal

            );


        } else {
            //SendCommand getTemp = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "camera_parameter_setting");
            PublishCommand getTemp = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "camera_parameter_setting", null);
            if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                getTemp.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
            }
            mDeviceManager.publishCommandRequest(getTemp, new Response.Listener<JobStatusResponse>() {

                        @Override
                        public void onResponse(JobStatusResponse response1) {

                            String responsebody = response1.getData().getOutput().getResponseMessage();
                            Log.i(TAG, "Camera Name :" + mDevice.getProfile().getName() + "SERVER RESP : " + responsebody);
                            if (responsebody != null && responsebody.contains("camera_parameter_setting")) {
                                try {
                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                    if (response != null && response.second instanceof String) {
                                        String responseString = ((String) response.second).trim();
                                        if (responseString.contains("mvr=") && motionDetection != null) {
                                            if (responseString.contains(PublicDefineGlob.MVR_ON)) {
                                                motionDetection.secondaryBooleanValue = true;

                                                final String oldStorageMode = mDevice.getProfile().getDeviceAttributes().getStorageMode();
                                                Log.i(TAG, "Record storage mode: " + oldStorageMode);
                                                if (oldStorageMode == null || oldStorageMode.equalsIgnoreCase("0")) {
                                                    CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 0);

                                                } else {
                                                    CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 1);

                                                }
                                                //   final String oldStorageMode = mDevice.getProfile().getDeviceAttributes().getStorageMode();
                                                // mvr is always updated everytime going to Camera Settings. so that just check when storage mode has value.
                                                if (motionDetection != null && motionDetection.secondaryBooleanValue &&
                                                        !TextUtils.isEmpty(oldStorageMode) && mDevice.getProfile().doesSupportSDCardAccess()) {
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Log.d(TAG, "Update storage mode: Reload camera list ...");
                                                            final ListenableFuture<Object> listenableFuture = DeviceSingleton.getInstance().update(false);
                                                            Futures.addCallback(listenableFuture, new FutureCallback<Object>() {
                                                                @Override
                                                                public void onSuccess(Object result) {
                                                                    Log.d(TAG, "Update storage mode: Update MotionDialog layout");
                                                                    if (mContext != null && mContext != null) {

                                                                        Device reloadedDevice = DeviceSingleton.getInstance().getDeviceByRegId(mDevice.getProfile().getRegistrationId());
                                                                        if (reloadedDevice != null) {
                                                                            String newStorageMode = reloadedDevice.getProfile().getDeviceAttributes().getStorageMode();
                                                                            Log.d(TAG, "Update storage mode: New storage mode: " + newStorageMode);
                                                                            if (!TextUtils.isEmpty(newStorageMode) && !oldStorageMode.equals(newStorageMode)) {
                                                            /*  Toast.makeText(mContext.getActivity(), mContext.getString(R.string.storage_mode_has_been_changed), Toast.LENGTH_SHORT).show();
                                                              updateMvrStorageLayout(reloadedDevice);*/

                                                                                if (newStorageMode == null || newStorageMode.equalsIgnoreCase("0")) {
                                                                                    CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 0);

                                                                                } else {
                                                                                    CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 1);

                                                                                }
                                                                            }
                                                                        }

                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Throwable t) {
                                                                    Log.d(TAG, "Update storage mode: Reload device lists failed", t);
                                                                }
                                                            });
                                                        }
                                                    }).start();
                                                }

                                            } else if (responseString.contains(PublicDefineGlob.MVR_OFF)) {
                                                motionDetection.secondaryBooleanValue = false;
                                            }
                                            CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING, motionDetection.secondaryBooleanValue);

                                        }
            /*
             * device_setting: ms=[motion status],me=[motion
             * sensitivity], vs=[vox status],vt=[vox threshold],hs=[high
             * temp detection status], ls=[high temp detection
             * status],ht=[high temp threshold],lt=[low temp threshold]
             */

                                        String individual_setting[] = responseString.split(",");
                                        if (individual_setting.length > 8) {
                                            if (motionDetection != null) {
                                                if (NotificationSettingUtils.supportMultiMotionTypes(mDevice.getProfile().getModelId(), mDevice.getProfile().getFirmwareVersion())) {
                                                    // get md type here
                                                    getMotionDetectionType(motionDetection);
                                                    Log.d(TAG, "Motion detection type: " + motionDetection.modeVda);

                                                } else if (mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72)) {
                                                    String ms = individual_setting[0];
                                                    if (ms.equalsIgnoreCase("ms=1")) {
                                                        motionDetection.booleanValue = true;
                                                    } else {
                                                        motionDetection.booleanValue = false;
                                                        motionDetection.modeVda = "";
                                                    }
                                                    CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS, motionDetection.booleanValue);
                                                    getMotionDetectionSource(motionDetection);

                                                } else {
                                                    String ms = individual_setting[0];
                                                    if (ms.equalsIgnoreCase("ms=1")) {
                                                        motionDetection.booleanValue = true;
                                                        motionDetection.modeVda = "md";
                                                    } else {
                                                        motionDetection.booleanValue = false;
                                                        motionDetection.modeVda = "";
                                                        //for vda : ms=2 : MD, ms=3 :BSD, ms=4 :BSC
                                                    }
                                                    CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS, motionDetection.booleanValue);

                                                }

                                                String me = individual_setting[1];
                                                int motionDetectThreshold = Integer.valueOf(me.split("=")[1]);
                                                if (mDevice.getProfile().isVTechCamera()) {
                                                    if (motionDetectThreshold < 1 && motionDetectThreshold > 7) {
                                                        motionDetectThreshold = 0;
                                                    } else {
                                                        motionDetectThreshold = motionDetectThreshold - 1;
                                                    }
                                                } else {
                                                    if (motionDetectThreshold <= 5) {
                                                        motionDetectThreshold = 0;
                                                    } else if (motionDetectThreshold <= 10) {
                                                        motionDetectThreshold = 1;
                                                    } else if (motionDetectThreshold <= 50) {
                                                        motionDetectThreshold = 2;
                                                    } else if (motionDetectThreshold <= 90) {
                                                        motionDetectThreshold = 3;
                                                    } else {
                                                        motionDetectThreshold = 4;
                                                    }
                                                }
                                                motionDetection.intValue = motionDetectThreshold;
                                                CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SENSITIVITY, motionDetection.intValue);

                                            }

                                            if (soundDetection != null) {
                                                String vs = individual_setting[2];
                                                soundDetection.booleanValue = (vs.equalsIgnoreCase("vs=1"));
                                                CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_STATUS, soundDetection.booleanValue);


                                                String vt = individual_setting[3];
                                                int soundThreshold = Integer.valueOf(vt.split("=")[1]);

                                                if (mDevice.getProfile().isVTechCamera()) {
                                                    soundThreshold = (soundThreshold - 1);
                                                }
//                          else {
//                            if (soundThreshold <= 5) {
//                              soundThreshold = 0;
//                            }else if (soundThreshold <= 25) {
//                              soundThreshold = 1;
//                            } else if (soundThreshold <= 50) {
//                              soundThreshold = 2;
//                            }  else if (soundThreshold <= 75) {
//                              soundThreshold = 3;
//                            } else if (soundThreshold <= 100) {
//                              soundThreshold = 4;
//                            }
//                          }

                                                soundDetection.intValue = soundThreshold;
                                                CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SOUND_SENSITIVITY, soundDetection.intValue);

                                            }

                                            if (temperature != null) {
                                                String hs = individual_setting[4];
                                                temperature.secondaryBooleanValue = (hs.equalsIgnoreCase("hs=1"));
                                                CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_STATUS, temperature.secondaryBooleanValue);

                                                String ht = individual_setting[6];
                                                int highTempThreshold = Integer.valueOf(ht.split("=")[1]);
                                                if (highTempThreshold == 0) {
                                                    highTempThreshold = mContext.getResources().getInteger(R.integer.default_high_temp_celcius);
                                                }
                                                temperature.secondaryIntValue = highTempThreshold;
                                                CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_VALUE, temperature.secondaryIntValue);


                                                String ls = individual_setting[5];
                                                temperature.booleanValue = (ls.equalsIgnoreCase("ls=1"));
                                                CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_STATUS, temperature.booleanValue);


                                                String lt = individual_setting[7];
                                                int lowTempThreshold = Integer.valueOf(lt.split("=")[1]);
                                                if (lowTempThreshold == 0) {
                                                    lowTempThreshold = mContext.getResources().getInteger(R.integer.default_low_temp_celcius);
                                                }
                                                temperature.intValue = lowTempThreshold;
                                                CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE, temperature.intValue);

                                            }
                                        }
                                    }
                                } catch (Exception exception) {
                                    Log.d(TAG, exception.getMessage());
                                }

                                mListener.onNotificationSettingsReceived();
                            } else {
                                if (motionDetection != null) {
                                    motionDetection.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                if (soundDetection != null) {
                                    soundDetection.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                if (temperature != null) {
                                    temperature.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onNotificationSettingsReceived();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mListener.onNotificationSettingsReceived();
                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }

                        }
                    }, isLocal

            );
        }

        // mListener.onNotificationSettingsReceived();
    }

    /**
     * Get motion detection type with command: get_md_type
     *
     * @return The response format is: get_md_type: [OFF | MD | BSC | BSD]
     */
    //TODO test
    private void getMotionDetectionType(final ListChild motionDetection) {
        // SendCommand getTemp = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "get_md_type");
        PublishCommand getTemp = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_md_type", null);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getTemp.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(getTemp, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("get_md_type")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);

                                if (response != null && response.second instanceof String) {
                                    String responseString = (String) response.second;
                                    Log.d(TAG, "Get motion detection type res: " + responseString);

                                    // Just accept responses: OFF, MD, BSC, BSD
                                    int mdTypeIndex = NotificationSettingUtils.getMotionDetectionTypeIndex(responseString.trim());
                                    motionDetection.modeVda = NotificationSettingUtils.getMotionDetectionType(mdTypeIndex);
                                    motionDetection.booleanValue = CameraSettingsActivity.MD_TYPE_MD.equalsIgnoreCase(motionDetection.modeVda) ||
                                            CameraSettingsActivity.MD_TYPE_BSC.equalsIgnoreCase(motionDetection.modeVda) ||
                                            CameraSettingsActivity.MD_TYPE_BSD.equalsIgnoreCase(motionDetection.modeVda);

                                    CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE, mdTypeIndex);
                                    CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_STATUS, motionDetection.booleanValue);

                                    mListener.onNotificationSettingsReceived();
                                }
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }, isLocal

        );
    }

    //TODO test
    private void getMotionDetectionSource(final ListChild motionDetection) {

        // SendCommand getTemp = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, PublicDefineGlob.GET_MOTION_SOURCE);
        PublishCommand getTemp = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, PublicDefineGlob.GET_MOTION_SOURCE, null);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getTemp.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(getTemp, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();

                        Log.i(TAG, "SERVER RESP : " + responsebody);

                        if (responsebody != null && responsebody.contains(PublicDefineGlob.GET_MOTION_SOURCE)) {
                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);

                                if (response != null && response.second instanceof Integer) {
                                    motionDetection.motionSource = (int) response.second;
                                } else if (response != null && response.second instanceof Float) {
                                    motionDetection.motionSource = ((Float) response.second).intValue();

                                } else {
                                    motionDetection.motionSource = -1;
                                }

                                CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SOURCE, motionDetection.motionSource);

                                mListener.onNotificationSettingsReceived();

                            } catch (Exception ex) {

                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }, isLocal

        );
    }

    /* response format: br=%d,ct=%d,cm=%d,vl=%d,nv=%d,fl=%d */
    private void getSetting2Settings(final ActorMessage.GetSetting2Settings setting2) {
        Log.d(TAG, "Using setting2 to retrieve setting info");
        Pair<String, Object> response = null;
        try {
            DeviceManager mDeviceManager;

            mDeviceManager = DeviceManager.getInstance(mContext);
            SecureConfig settings = HubbleApplication.AppConfig;
            String regId = mDevice.getProfile().getRegistrationId();
            //SendCommand getSetting2Settings = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "camera_setting2");
            PublishCommand getSetting2Settings = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "camera_setting2", null);
            if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                getSetting2Settings.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
            }

            mDeviceManager.publishCommandRequest(getSetting2Settings, new Response.Listener<JobStatusResponse>() {

                        @Override
                        public void onResponse(JobStatusResponse response1) {
                            String responsebody = response1.getData().getOutput().getResponseMessage();
                            Log.i(TAG, "Camera Name : " + mDevice.getProfile().getName() + "  SERVER RESP : " + responsebody);
                            if (responsebody != null && responsebody.contains("camera_setting2")) {

                                try {
                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                    if (response == null || response.second == null || !(response.second instanceof String)) {
                                        handleSetting2Values(setting2, null);
                                    } else {
                                        String responseStr = (String) response.second;
                                        responseStr = responseStr.replace("camera_setting2: ", "");
                                        String[] elements = responseStr.split(",");
                                        if (elements == null || elements.length == 0) {
                                            handleSetting2Values(setting2, null);
                                        } else {
                                            // parsing response value into map of key - value
                                            Map<String, String> results = new HashMap<>();
                                            String element;
                                            for (int i = 0; i < elements.length; i++) {
                                                element = elements[i];
                                                // element has to be in format setting2key=value
                                                // invalid value will be handled in #handleSetting2Values
                                                if (TextUtils.isEmpty(element) || !element.contains("=") || element.startsWith("=")
                                                        || element.endsWith("=")) {
                                                    continue;
                                                }
                                                String[] keyValue = element.split("=");
                                                results.put(keyValue[0], keyValue[1].trim());
                                            }
                                            // apply response value to UI
                                            handleSetting2Values(setting2, results);
                                        }
                                    }
                                } catch (Exception ex) {

                                }
                            } else {
                                handleSetting2Values(setting2, null);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }
                            handleSetting2Values(setting2, null);
                        }
                    }, isLocal

            );

        } catch (RetrofitError e) {
            Log.d(TAG, "RetrofitError: Failed to get command camera_setting2", e);
        } catch (Exception e) {
            Log.d(TAG, "Exception: Failed to get command camera_setting2", e);
        }
        // response value has to be a string

    }

    /**
     * Apply camera_setting2 response value to UI
     *
     * @param setting2 contain all setting2 keys have to be handled. include no-supported keys.
     * @param dataMap  map of setting2 key - setting2 response value
     */
    private void handleSetting2Values(ActorMessage.GetSetting2Settings setting2, Map<String, String> dataMap) {
        // all setting2 keys supported up to now
        List<String> allSetting2 = Arrays.asList(PublicDefine.groupSettingsAll);
        // all setting2 keys have to be handled. include no-supported keys.
        String[] groupSettings = setting2.settings;
        for (int i = 0; i < groupSettings.length; i++) {
            // if the key is not supported -> skip it. It is get and handled in a other request.
            if (!allSetting2.contains(groupSettings[i])) {
                continue;
            }
            // get UI item corresponds with the key
            ListChild item = setting2.getListChildBySettingCode(groupSettings[i]);
            if (item != null) {
                // if response value does not have or has invalid value for the key
                // dataMap will not have that key. treat as failed-to-retrieve ->
                if (dataMap == null || !dataMap.containsKey(groupSettings[i])) {
                    item.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                } else {
                    String mapValue = dataMap.get(groupSettings[i]);
                    // if value is not a number -> treat as failed-to-retrieve
                    if (TextUtils.isEmpty(mapValue) || !Util.isDigitOnly(mapValue)) {
                        item.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                    } else {
                        // valid value -> handle it
                        if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_CEILING_MOUNT)) {
                            handleCeilingMountValue(item, Integer.parseInt(mapValue));

                        } else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_VOLUME)) {
                            handleVolumeValue(item, Integer.parseInt(mapValue));
                        } else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_NIGHT_VISION)) {
                            if (mDevice.getProfile().isVTechCamera()) {
                                handleNightVisionValue(item, Integer.parseInt(mapValue));
                            } else {
                                handleNightVisionValueForHubble(item, Integer.parseInt(mapValue));
                            }
                        } else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_BRIGHTNESS)) {
                            handleGenericValue(item, Integer.parseInt(mapValue), true);
                        } else if (groupSettings[i].equals(PublicDefine.SETTING_2_KEY_OVERLAY_DATE)) {
                            handleCeilingMountValue(item, Integer.parseInt(mapValue));

                        } else {
                            handleGenericValue(item, Integer.parseInt(mapValue), false);
                        }
                    }
                }
            }
        }
        // mListener.onDataSetChanged(listChild);
    }

    //this method is not used , hence not changed to PublishCommand API
    private void getPark() {
        SendCommand getCamPark = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_cam_park");

        mDeviceManager.sendCommandRequest(getCamPark, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_cam_park")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                mListener.onParkReceived(response);
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }

        );

        SendCommand getParkTimer = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_park_timer");

        mDeviceManager.sendCommandRequest(getParkTimer, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_park_timer")) {

                            try {
                                final Pair<String, Object> response2 = CommonUtil.parseResponseBody(responsebody);
                                mListener.onParkTimerReceived(response2);
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }

        );

    }

    //this method is not used , hence not changed to PublishCommand API
    private void getSlaveFirmware(final ListChild slaveFirmware) {
        String gatewayIp = Util.getGatewayIp(mContext);
        SendCommand getSlaveFirmware = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_soc_version");

        mDeviceManager.sendCommandRequest(getSlaveFirmware, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_soc_version")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && response.second instanceof String) {
                                    slaveFirmware.value = (String) response.second;
                                } else {
                                    slaveFirmware.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }

                                mListener.onDataSetChanged(slaveFirmware);
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                            mListener.onDataSetChanged(slaveFirmware);
                        }

                    }
                }

        );

    }

    //this method is not used , hence not changed to PublishCommand API
    private void getStatusLED(final ListChild listChild) {
        SendCommand getStatusLED = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_led_func");

        mDeviceManager.sendCommandRequest(getStatusLED, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_led_func")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && response.second instanceof Integer) {
                                    final boolean isActivated = (Integer) response.second == 1;
                                    listChild.booleanValue = isActivated;
                                    listChild.value = isActivated ? getSafeString(R.string.on) : getSafeString(R.string.off);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                            mListener.onDataSetChanged(listChild);
                        }

                    }
                }

        );

    }

    private void getTimeZone(final ListChild timezone) {
        if (mDevice.getProfile().isAvailable()) {
            //SendCommand getTimeZone = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "get_time_zone");
            PublishCommand getTimeZone = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_time_zone", null);

            if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                getTimeZone.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
            }


            mDeviceManager.publishCommandRequest(getTimeZone, new Response.Listener<JobStatusResponse>() {


                        @Override
                        public void onResponse(JobStatusResponse response1) {
                            String responsebody = response1.getData().getOutput().getResponseMessage();
                            Log.i(TAG, "SERVER RESP : " + responsebody);
                            if (responsebody != null && responsebody.contains("get_time_zone")) {

                                try {
                                    //this change is done so that timezone value is not converted to float in parsePublishResponseBody method
                                    if (responsebody.contains("."))
                                        responsebody = responsebody.replace(".", ";");
                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                    if (response != null) {

                                        String timezoneString = ((String) response.second).replace(";", ":");
                                        if (timezoneString.length() < 8) {
                                            timezone.value = "GMT" + timezoneString;
                                        }
                                    } else {
                                        timezone.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                    }
                                    if (mListener != null) {
                                        mListener.onDataSetChanged(timezone);
                                    }
                                } catch (Exception ex) {
                                    timezone.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                    if (mListener != null) {
                                        mListener.onDataSetChanged(timezone);
                                    }
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }
                            timezone.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            if (mListener != null) {
                                mListener.onDataSetChanged(timezone);
                            }
                        }
                    }, isLocal);


        } else {
            timezone.value = getSafeString(R.string.failed_to_retrieve_camera_data);
        }
        if (TextUtils.isEmpty(timezone.value)) {
            timezone.value = getSafeString(R.string.failed_to_retrieve_camera_data);
        }

        if (mListener != null) {
            mListener.onDataSetChanged(timezone);
        }
    }


    private void getVolume(final ListChild listChild) {
        SendCommand getVolume = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_spk_volume");
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getVolume.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.sendCommandRequest(getVolume, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_spk_volume")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);

                                if (response != null && response.second instanceof Integer) {
                                    handleVolumeValue(listChild, (int) response.second);
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                }
                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        mListener.onDataSetChanged(listChild);
                    }
                },
                isLocal
        );

    }

    private void handleVolumeValue(ListChild listChild, int value) {
        if (value < 0) {
            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
        } else {
            long firmwareVersion = 0;
            try {
                firmwareVersion = Long.valueOf(mDevice.getProfile().getFirmwareVersion().replace(".", ""));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
            if (firmwareVersion < 11900) {
                value = value - 21;
            } else if (mDevice.getProfile().isStandBySupported()) {
                if (value >= DEVICE_VOLUME_RANGE_100_LL) {
                    value = ((int) ((value - DEVICE_VOLUME_RANGE_100_LL) / DEVICE_VOLUME_RANGE_SEPARATOR));
                } else {
                    value = 0;
                }
            }

            listChild.intValue = value;
            listChild.value = String.valueOf(listChild.intValue);
            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VOLUME, listChild.intValue);

        }
    }

    // Setters
    //TODO test
    private void setAdaptiveQuality(final ListChild listChild, final boolean isAdaptive, final String resolutionValue, final String bitrateValue) {
        // boolean success = mDevice.sendCommandGetSuccess("set_adaptive_bitrate", (isAdaptive ? "on" : "off"), null);
        //SendCommand setAdaptiveQuality = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_adaptive_bitrate"+"&value="+(isAdaptive ? "on" : "off"));
        PublishCommand setAdaptiveQuality = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_adaptive_bitrate", null);
        setAdaptiveQuality.setValue(isAdaptive ? "on" : "off");
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setAdaptiveQuality.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setAdaptiveQuality, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        boolean success = false;
                        if (responsebody != null && responsebody.contains("set_adaptive_bitrate") &&
                                responsebody.contains("0")) {

                            try {
                                success = true;
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                VideoBandwidthSupervisor.getInstance().setAdaptiveBitrate(true);

                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_ADAPTIVE_QUALITY, success);
                                EventBus.getDefault().post(sendCommandEvent);

                                handleSetterResponse(listChild, success, R.string.video_quality_settings_changed, R.string.failed_to_change_video_quality);


                            } catch (Exception ex) {

                            }
                        } else {
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_ADAPTIVE_QUALITY, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(listChild, false, R.string.video_quality_settings_changed, R.string.failed_to_change_video_quality);

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_ADAPTIVE_QUALITY, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.video_quality_settings_changed, R.string.failed_to_change_video_quality);


                    }
                }, isLocal


        );


        if (!isAdaptive) {
            if (mDevice.getProfile().isVTechCamera()) {
                mDevice.sendCommandGetSuccess("set_resolution", resolutionValue, null); // TODO: Possibly remove if the final word is that it should not be in the app. Originally it was just supposed to be a debug option.
            }

            final String bitrateToSend = bitrateValue.substring(0, bitrateValue.length() - 4);
            //  boolean bitrateSuccess = mDevice.sendCommandGetSuccess("set_video_bitrate", bitrateToSend, null);
            // SendCommand setVideoBitRate = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_video_bitrate" + "value=" + bitrateToSend);
            PublishCommand setVideoBitRate = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_video_bitrate", null);
            setVideoBitRate.setValue(bitrateToSend);
            if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                setVideoBitRate.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
            }

            mDeviceManager.publishCommandRequest(setVideoBitRate, new Response.Listener<JobStatusResponse>() {

                        @Override
                        public void onResponse(JobStatusResponse response1) {
                            String responsebody = response1.getData().getOutput().getResponseMessage();
                            Log.i(TAG, "SERVER RESP : " + responsebody);
                            if (responsebody != null && responsebody.contains("set_video_bitrate") &&
                                    responsebody.contains("0")) {

                                try {
                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                    VideoBandwidthSupervisor.getInstance().setBitrateVariable(Integer.valueOf(bitrateToSend));
                                } catch (Exception ex) {

                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }

                        }
                    }, isLocal

            );

        }
    }

    private void setBrightness(final ListChild listChild, int brightnessLevel) {
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.GENERAL_SETTING, AppEvents.BRIGHTNESS_VALUE + " : " + String.valueOf(brightnessLevel), AppEvents.BRIGHTNESSVALUE);
        ZaiusEvent brightnessValueEvt = new ZaiusEvent(AppEvents.GENERAL_SETTING);
        brightnessValueEvt.action(AppEvents.BRIGHTNESS_VALUE + " : " + String.valueOf(brightnessLevel));
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(brightnessValueEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }


        // boolean success = mDevice.sendCommandGetSuccess("set_brightness", brightnessLevel + "", null);
        // SendCommand setBrightness = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_brightness"+"&value="+brightnessLevel);
        PublishCommand setBrightness = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_brightness", null);
        setBrightness.setValue(brightnessLevel + "");
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setBrightness.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }


        mDeviceManager.publishCommandRequest(setBrightness, new Response.Listener<JobStatusResponse>() {

            @Override
            public void onResponse(JobStatusResponse response1) {
                String responsebody = response1.getData().getOutput().getResponseMessage();
                boolean success = false;
                Log.i(TAG, "SERVER RESP : " + responsebody);
                if (responsebody != null && responsebody.contains("set_brightness") &&
                        responsebody.contains("0")) {

                    try {
                        success = true;
                        final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);

                    } catch (Exception ex) {

                    }
                } else {
                    success = false;
                }
                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_BRIGHTNESS, success);
                EventBus.getDefault().post(sendCommandEvent);

                handleSetterResponse(listChild, success, R.string.brightness_changed, R.string.brightness_change_failed);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_BRIGHTNESS, false);
                EventBus.getDefault().post(sendCommandEvent);

                handleSetterResponse(listChild, false, R.string.brightness_changed, R.string.brightness_change_failed);
                if (error != null && error.networkResponse != null) {
                    Log.d(TAG, error.networkResponse.toString());
                    Log.d(TAG, error.networkResponse.data.toString());
                }
            }
        }, isLocal);

    }

    private void setCeilingMount(final ListChild listChild, boolean orientation) {

        // SendCommand setCeilingMount = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_flipup"+"&value="+(orientation ? 1 : 0));
        int ceilingMountValue = orientation ? 1 : 0;
        PublishCommand setCeilingMount = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_flipup", null);
        setCeilingMount.setValue(ceilingMountValue + "");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setCeilingMount.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setCeilingMount, new Response.Listener<JobStatusResponse>() {


                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        boolean success = false;
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_flipup") &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                success = true;

                            } catch (Exception ex) {

                            }
                        } else {
                            success = false;
                        }
                        CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.CEILING_MOUNT, success);

                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_CEILING_MOUNT, success);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, success, R.string.camera_image_flipped, R.string.camera_image_flip_failed);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_CEILING_MOUNT, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.camera_image_flipped, R.string.camera_image_flip_failed);
                    }
                }, isLocal

        );

    }

    //TODO test
    private void setNightLight(final ListChild listChild, int mode) {

        //SendCommand setCeilingMount = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId,
        //   "set_night_light_status&value=" + mode);
        PublishCommand setCeilingMount = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_night_light_status", null);
        setCeilingMount.setValue(mode + "");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setCeilingMount.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(setCeilingMount, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        boolean success = false;
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_night_light_status") &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                success = true;

                            } catch (Exception ex) {

                            }
                        } else {
                            success = false;
                        }
                        CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_LIGHT, success);

                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_NIGHT_LIGHT, success);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, success, R.string.night_light_changed_success, R.string.night_light_changed_failed);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_NIGHT_LIGHT, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.night_light_changed_success, R.string.night_light_changed_failed);
                    }
                }, isLocal

        );

    }

    //TODO test
    private void setOverlayDate(final ListChild listChild, boolean on) {
        //boolean success = mDevice.sendCommandGetSuccess("overlay_date_set", (on ? 1 : 0) + "", null);

        // SendCommand setOverlayDate = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "overlay_date_set"+"&value=" + (on ? 1 : 0));
        PublishCommand setOverlayDate = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "overlay_date_set", null);
        if (on) {
            setOverlayDate.setValue("1");
        } else {
            setOverlayDate.setValue("0");
        }

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setOverlayDate.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setOverlayDate, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("overlay_date_set") &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);

                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_OVERLAY_DATE, true);
                                EventBus.getDefault().post(sendCommandEvent);

                                handleSetterResponse(listChild, true, R.string.change_overlay_date_success, R.string.change_overlay_date_fail);
                            } catch (Exception ex) {

                            }
                        } else {

                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_OVERLAY_DATE, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(listChild, false, R.string.change_overlay_date_success, R.string.change_overlay_date_fail);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_OVERLAY_DATE, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.change_overlay_date_success, R.string.change_overlay_date_fail);
                    }
                }, isLocal

        );

    }

    //TODO test
    // This is not being used
    private void setContrast(final ListChild listChild, int contrastLevel) {
        //  boolean success = mDevice.sendCommandGetSuccess("set_contrast", contrastLevel + "", null);

   /* if(mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null)
    {
      setContrast.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
    }*/
        // SendCommand setContrast = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_contrast" + "&value=" +contrastLevel);
        PublishCommand setContrast = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_contrast", null);
        setContrast.setValue(contrastLevel + "");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setContrast.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setContrast, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        boolean success = false;
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_contrast") &&
                                responsebody.contains("0")) {

                            try {
                                success = true;
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                            } catch (Exception ex) {

                            }
                        } else {
                            success = false;
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_CONTRAST, success);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, success, R.string.contrast_changed, R.string.contrast_change_failed);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_CONTRAST, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.contrast_changed, R.string.contrast_change_failed);

                    }
                }, isLocal

        );


    }


    //TODO test
    private void setLEDFlicker(final ListChild listChild, int ledHz) {
        //SendCommand setLEDFlicker = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_flicker"+"&value="+ledHz);
        PublishCommand setLEDFlicker = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_flicker", null);
        setLEDFlicker.setValue(ledHz + "");


        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setLEDFlicker.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setLEDFlicker, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        boolean success = false;
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_flicker") &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                success = true;
                            } catch (Exception ex) {

                            }
                        } else {
                            success = false;
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_LED_FLICKER, success);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, success, R.string.led_flicker_changed, R.string.led_flicker_change_failed);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_LED_FLICKER, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.led_flicker_changed, R.string.led_flicker_change_failed);

                    }
                }, isLocal

        );

    }

    //TODO test
    private void SetMotionSource(final ListChild listChild, final int motionSource) {
        String command = PublicDefine.BM_HTTP_CMD_PART + PublicDefineGlob.SET_MOTION_SOURCE + PublicDefineGlob.SET_VALUE_CONSTANT + motionSource;

        // SendCommand motionSourceRequest = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, command);
        PublishCommand motionSourceRequest = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, PublicDefineGlob.SET_MOTION_SOURCE, null);
        motionSourceRequest.setValue(motionSource + "");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            motionSourceRequest.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(motionSourceRequest, new Response.Listener<JobStatusResponse>() {
                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();

                        Log.i(TAG, "SERVER RESP : " + responsebody);

                        if (responsebody != null && responsebody.contains(PublicDefineGlob.SET_MOTION_SOURCE))  {
                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                int value = -1;

                                if (response != null && response.second instanceof Integer) {
                                    value = (int) response.second;
                                } else if (response != null && response.second instanceof Float) {
                                    value = ((Float) response.second).intValue();
                                }

                                if (value == 0) {
                                    success = true;
                                    listChild.motionSource = motionSource;
                                    CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_SOURCE, motionSource);
                                } else {
                                    success = false;
                                }

                                handleSetterResponse(listChild, success, R.string.motion_source_applied, R.string.motion_source_failed);

                            } catch (Exception ex) {
                                handleSetterResponse(listChild, false, R.string.motion_source_applied, R.string.motion_source_failed);
                            }
                        }else{
                            handleSetterResponse(listChild, false, R.string.motion_source_applied, R.string.motion_source_failed);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        setStatus = -1;
                        handleSetterResponse(listChild, true, R.string.motion_detection_changed, R.string.motion_detection_change_failed);


                    }
                }, isLocal);
    }

    //TODO test
    private void setMotionDetection(final ListChild listChild, final boolean motionDetectionEnabled, int motionDetectionLevel) {
    /*
     * 20160405: hoang:
     * Fix "Record Motion" is not updated after changing successfully.
     * Update list child boolean value to fix it.
     */
        listChild.booleanValue = motionDetectionEnabled;

        int status = -1;
        String value = "";
        PublishCommand setMotionArea = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_motion_area", null);
        if (motionDetectionEnabled) {
            //value = PublicDefineGlob.MOTION_ON_PARAM;
            setMotionArea.setMvrToggleGrid(PublicDefineGlob.MOTION_ON_GRID_VALUE);
            setMotionArea.setMvrToggleZone(PublicDefineGlob.MOTION_ON_ZONE_VALUE);
        } else {
            //value = PublicDefineGlob.MOTION_OFF_PARAM;
            setMotionArea.setMvrToggleGrid(PublicDefineGlob.MOTION_OFF_GRID_VALUE);

            // mDevice.sendCommandGetSuccess("set_recording_parameter", "01", null);
            // SendCommand setRecording = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, PublicDefineGlob.SET_RECORDING_PARAMETER_CMD + PublicDefineGlob.SET_RECORDING_PARAMETER_MVR_OFF);
            PublishCommand setRecording = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, PublicDefineGlob.SET_RECORDING_PARAMETER_CMD, null);
            setRecording.setValue(PublicDefineGlob.SET_RECORDING_PARAMETER_MVR_OFF);

            if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                setRecording.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
            }

            mDeviceManager.publishCommandRequest(setRecording, new Response.Listener<JobStatusResponse>() {

                        @Override
                        public void onResponse(JobStatusResponse response1) {
                            String responsebody = response1.getData().getOutput().getResponseMessage();
                            Log.i(TAG, "SERVER RESP : " + responsebody);
                            if (responsebody != null && responsebody.contains(PublicDefineGlob.SET_RECORDING_PARAMETER_CMD) &&
                                        responsebody.contains("0")) {

                                try {

                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                } catch (Exception ex) {

                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }

                        }
                    }, isLocal

            );
            listChild.secondaryBooleanValue = false;
            listChild.modeVda = "";
        }


        //SendCommand setMotionArea = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_motion_area" + value);

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setMotionArea.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(setMotionArea, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {

                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        responseCode = response1.getStatus();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_motion_area")  &&
                                responsebody.contains("0")) {

                            try {
                                if (responseCode == 200)
                                    setMotionDetectionResponse(true, listChild);
                                else
                                    setMotionDetectionResponse(false, listChild);
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                            } catch (Exception ex) {

                            }
                        } else {
                            setMotionDetectionResponse(false, listChild);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setMotionDetectionResponse(false, listChild);
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }, isLocal

        );
        //       status = mDevice.sendCommandGetStatus("set_motion_area" + value, null, null);

        //  boolean success = (responseCode == 200);
        //success = device.sendCommandGetSuccess("set_motion_area" + value, null, null); // Leaving in place. We're using the above line to just get the server response, since there's a firmware bug and it's returning false failures.

 /*   String sensitivityValue = "";
    if (mDevice.getProfile().isVTechCamera()) {
      switch (motionDetectionLevel) {
        case 0:
          sensitivityValue = "1";
          break;
        case 1:
          sensitivityValue = "2";
          break;
        case 2:
          sensitivityValue = "3";
          break;
        case 3:
          sensitivityValue = "4";
          break;
        case 4:
          sensitivityValue = "5";
          break;
        case 5:
          sensitivityValue = "6";
          break;
        case 6:
          sensitivityValue = "7";
          break;
      }
    } else {
      switch (motionDetectionLevel) {
        case 0:
          sensitivityValue = "5";
          break;
        case 1:
          sensitivityValue = "10";
          break;
        case 2:
          sensitivityValue = "50";
          break;
        case 3:
          sensitivityValue = "90";
          break;
        case 4:
          sensitivityValue = "95";
          break;
      }
    }

    SendCommand setSensitivity = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_motion_sensitivity"+"&setup=" + sensitivityValue);
    mDeviceManager.sendCommandRequest(setSensitivity, new Response.Listener<SendCommandDetails>() {


              @Override
              public void onResponse(SendCommandDetails response1) {
                String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                Log.i(TAG, "SERVER RESP : " + responsebody);
                if (response1.getDeviceCommandResponse() != null && responsebody.contains("set_motion_sensitivity")) {

                  try {

                    final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                  } catch (Exception ex) {

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

              }
            }

    );*/
  }



    void setMotionSentivity(final ListChild listChild, int motionDetectionLevel) {
        String sensitivityValue = "";
        if (mDevice != null && mDevice.getProfile() != null && mDevice.getProfile().isVTechCamera()) {
            switch (motionDetectionLevel) {
                case 0:
                    sensitivityValue = "1";
                    break;
                case 1:
                    sensitivityValue = "2";
                    break;
                case 2:
                    sensitivityValue = "3";
                    break;
                case 3:
                    sensitivityValue = "4";
                    break;
                case 4:
                    sensitivityValue = "5";
                    break;
                case 5:
                    sensitivityValue = "6";
                    break;
                case 6:
                    sensitivityValue = "7";
                    break;
            }
        } else if (mIsOrbit || (mDevice != null && mDevice.getProfile() != null && mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT))) {
            switch (motionDetectionLevel) {
                case 0:
                    sensitivityValue = "0";
                    break;
                case 1:
                    sensitivityValue = "30";
                    break;
                case 2:
                    sensitivityValue = "60";
                    break;
                case 3:
                    sensitivityValue = "60";
                    break;
                case 4:
                    sensitivityValue = "100";
                    break;
            }
        } else {
            switch (motionDetectionLevel) {
                case 0:
                    sensitivityValue = "5";
                    break;
                case 1:
                    sensitivityValue = "10";
                    break;
                case 2:
                    sensitivityValue = "50";
                    break;
                case 3:
                    sensitivityValue = "90";
                    break;
                case 4:
                    sensitivityValue = "95";
                    break;
            }
        }
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY + " : " + sensitivityValue, AppEvents.SENSITIVITY_VALUE);
        ZaiusEvent sensitivitValueEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
        sensitivitValueEvt.action(AppEvents.MOTION_SENSITIVITY + " : " + sensitivityValue);
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(sensitivitValueEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }

        if (mIsOrbit || (mDevice != null && mDevice.getProfile() != null && mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT))) {
            // SendCommand setSensitivity = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_pir_sensitivity" + "&value=" + sensitivityValue);
            PublishCommand setSensitivity = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_pir_sensitivity", null);
            setSensitivity.setValue(sensitivityValue + "");
            if (mDevice!=null && mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                setSensitivity.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
            }

            mDeviceManager.publishCommandRequest(setSensitivity, new Response.Listener<JobStatusResponse>() {

                        @Override
                        public void onResponse(JobStatusResponse response1) {
                            String responsebody = response1.getData().getOutput().getResponseMessage();
                            Log.i(TAG, "SERVER RESP : " + responsebody);
                            if (responsebody != null && responsebody.contains("set_pir_sensitivity")  &&
                                    responsebody.contains("0")) {

                                try {

                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                    if (responsebody.contains("0")) {
                                        handleSetterResponse(listChild, true, R.string.sensitivity_changed, R.string.sensitivity_changed_failed);
                                        CommonUtil.setSettingValue(mContext, regId + "-" + SettingsPrefUtils.MOTION_SENSITIVITY, listChild.intValue);
                                        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.SUCCESS, AppEvents.MOTION_SENSITIVITY_SUCCESS);
                                        ZaiusEvent sensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                        sensitivityChangeEvt.action(AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.SUCCESS);
                                        try {
                                            ZaiusEventManager.getInstance().trackCustomEvent(sensitivityChangeEvt);
                                        } catch (ZaiusException e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        if (responsebody.contains("-1"))
                                            handleSetterResponse(listChild, false, R.string.sensitivity_changed, R.string.sensitivity_changed_failed);
                                        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + " : -1", AppEvents.MOTION_SENSITIVITY_FAILURE);
                                        ZaiusEvent sensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                        sensitivityChangeEvt.action(AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + " : -1");
                                        try {
                                            ZaiusEventManager.getInstance().trackCustomEvent(sensitivityChangeEvt);
                                        } catch (ZaiusException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                } catch (Exception ex) {
                                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + ex.getMessage(), AppEvents.MOTION_SENSITIVITY_FAILURE);
                                    ZaiusEvent sensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                    sensitivityChangeEvt.action(AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + ex.getMessage());
                                    try {
                                        ZaiusEventManager.getInstance().trackCustomEvent(sensitivityChangeEvt);
                                    } catch (ZaiusException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                handleSetterResponse(listChild, false, R.string.sensitivity_changed, R.string.sensitivity_changed_failed);
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE, AppEvents.MOTION_SENSITIVITY_FAILURE);
                                ZaiusEvent sensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                sensitivityChangeEvt.action(AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE);
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(sensitivityChangeEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            handleSetterResponse(listChild, false, R.string.sensitivity_changed, R.string.sensitivity_changed_failed);

                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + error.networkResponse.data.toString(), AppEvents.MOTION_SENSITIVITY_FAILURE);

                                ZaiusEvent sensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                sensitivityChangeEvt.action(AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + error.networkResponse.data.toString());
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(sensitivityChangeEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }, isLocal

            );
        } else {
            PublishCommand setSensitivity = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_motion_sensitivity", null);

            String commandValue = null;
            if (regId != null && regId.length() > 6 && regId.substring(2, 6).equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72)) {
                // commandValue = "&value=";
                setSensitivity.setValue(sensitivityValue);
            } else {
                //commandValue = "&setup=";
                setSensitivity.setSetup(sensitivityValue);
            }

            //SendCommand setSensitivity = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_motion_sensitivity" +  commandValue + sensitivityValue);

            if (mDevice!=null && mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                setSensitivity.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
            }
            mDeviceManager.publishCommandRequest(setSensitivity, new Response.Listener<JobStatusResponse>() {

                        @Override
                        public void onResponse(JobStatusResponse response1) {
                            String responsebody = response1.getData().getOutput().getResponseMessage();
                            Log.i(TAG, "SERVER RESP : " + responsebody);
                            if (responsebody != null && responsebody.contains("set_motion_sensitivity")) {

                                try {

                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                    if (responsebody.contains("0")) {
                                        handleSetterResponse(listChild, true, R.string.sensitivity_changed, R.string.sensitivity_changed_failed);
                                        CommonUtil.setSettingValue(mContext, regId + "-" + SettingsPrefUtils.MOTION_SENSITIVITY, listChild.intValue);
                                        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.SUCCESS, AppEvents.MOTION_SENSITIVITY_SUCCESS);
                                        ZaiusEvent sensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                        sensitivityChangeEvt.action(AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.SUCCESS);
                                        try {
                                            ZaiusEventManager.getInstance().trackCustomEvent(sensitivityChangeEvt);
                                        } catch (ZaiusException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        handleSetterResponse(listChild, false, R.string.sensitivity_changed, R.string.sensitivity_changed_failed);
                                        if (responsebody.contains("-1")) {
                                            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + " : -1", AppEvents.MOTION_SENSITIVITY_FAILURE);
                                            ZaiusEvent sensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                            sensitivityChangeEvt.action(AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + " : -1");
                                            try {
                                                ZaiusEventManager.getInstance().trackCustomEvent(sensitivityChangeEvt);
                                            } catch (ZaiusException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + " :" + ex.getMessage(), AppEvents.MOTION_SENSITIVITY_FAILURE);
                                    ZaiusEvent sensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                    sensitivityChangeEvt.action(AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + " :" + ex.getMessage());
                                    try {
                                        ZaiusEventManager.getInstance().trackCustomEvent(sensitivityChangeEvt);
                                    } catch (ZaiusException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                handleSetterResponse(listChild, false, R.string.sensitivity_changed, R.string.sensitivity_changed_failed);
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE, AppEvents.MOTION_SENSITIVITY_FAILURE);
                                ZaiusEvent sensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                sensitivityChangeEvt.action(AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE);
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(sensitivityChangeEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            handleSetterResponse(listChild, false, R.string.sensitivity_changed, R.string.sensitivity_changed_failed);
                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + " :" + error.networkResponse.data.toString(), AppEvents.MOTION_SENSITIVITY_FAILURE);
                                ZaiusEvent sensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                sensitivityChangeEvt.action(AppEvents.MOTION_SENSITIVITY_CHANGE + " : " + AppEvents.FAILURE + " :" + error.networkResponse.data.toString());
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(sensitivityChangeEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }, isLocal

            );
        }
    }

    void setMotionDetectionResponse(boolean success, ListChild listChild) {

        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_MOTION, success);
        EventBus.getDefault().post(sendCommandEvent);

        handleSetterResponse(listChild, success, R.string.motion_detection_changed, R.string.motion_detection_change_failed);
    }

    int setStatus = -1;

    //TODO test
    private void setMotionDetectionVda(final ListChild listChild, final int pos, int motionDetectionLevel, int prevPosition) {
    /*
     * 20160405: hoang:
     * Fix "Record Motion" is not updated after changing successfully.
     * Update list child boolean value to fix it.
     */
        listChild.booleanValue = (pos > 0);

        boolean success = false;
        int status = -1;

        if (pos > 0) {
            if (NotificationSettingUtils.supportMultiMotionTypes(mDevice.getProfile().getModelId(), mDevice.getProfile().getFirmwareVersion())) {
                String cmdName = "";
                String cmdPrefix = "";
                String cmdValue = "";
                switch (pos) {
                    case 1:
                        //cmdValue = "start_ait_md";
                        cmdName = "start_ait_md";
                        cmdValue = null;
                        cmdPrefix = "start_ait_md";
                        break;
                    case 2:
                        //cmdValue = "start_vda&value=bsc";
                        cmdName = "start_vda";
                        cmdValue = "bsc";
                        cmdPrefix = "start_vda";
                        break;
                    case 3:
                        //cmdValue = "start_vda&value=bsd";
                        cmdName = "start_vda";
                        cmdValue = "bsd";
                        cmdPrefix = "start_vda";
                        break;
                }

                //  status = mDevice.sendCommandGetStatus(cmdValue, null, null);
                final String command = cmdName;
                final String commandPrefix = cmdPrefix;

                //SendCommand setMotionvda = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, command);
                PublishCommand setMotionvda = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, command, null);
                if (cmdValue != null)
                    setMotionvda.setValue(cmdValue);

                if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                    setMotionvda.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
                }

                mDeviceManager.publishCommandRequest(setMotionvda, new Response.Listener<JobStatusResponse>() {

                            @Override
                            public void onResponse(JobStatusResponse response1) {
                                String responsebody = response1.getData().getOutput().getResponseMessage();
                                Log.i(TAG, "SERVER RESP : " + responsebody);
                                setStatus = response1.getStatus();
                                if (responsebody != null && responsebody.contains(commandPrefix)  &&
                                        responsebody.contains("0")) {

                                    try {
                                        final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                        CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE, pos);


                                        if (pos == 1) {
                                            setMotionNotification(listChild, listChild.booleanValue);
                                        } else {

                                            handleSetterResponse(listChild, true, R.string.motion_detection_changed, R.string.motion_detection_change_failed);

                                        }


                                    } catch (Exception ex) {
                                        handleSetterResponse(listChild, false, R.string.motion_detection_changed, R.string.motion_detection_change_failed);

                                    }
                                }else{
                                    setStatus = -1;
                                    handleSetterResponse(listChild, true, R.string.motion_detection_changed, R.string.motion_detection_change_failed);
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                if (error != null && error.networkResponse != null) {
                                    Log.d(TAG, error.networkResponse.toString());
                                    Log.d(TAG, error.networkResponse.data.toString());
                                }
                                setStatus = -1;
                                handleSetterResponse(listChild, true, R.string.motion_detection_changed, R.string.motion_detection_change_failed);


                            }
                        }, isLocal

                );
            } else if (NotificationSettingUtils.supportMultiMotionTypesPIR(mDevice.getProfile().getModelId(), mDevice.getProfile().getFirmwareVersion())) {
                // turn on normally then set_motion_source
                if (prevPosition == 0) {
                    setMotionNotification(listChild, listChild.booleanValue);
                }

            }

            //success = (status == 200);
        } else if (prevPosition != 1 &&
                mDevice.getProfile().getModelId().equals("0877")) {
            // status = mDevice.sendCommandGetStatus("set_playpause_motion_detection", "0", null);
            //SendCommand setPlayPauseMD = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_playpause_motion_detection" + "0");
            PublishCommand setPlayPauseMD = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_playpause_motion_detection", null);
            setPlayPauseMD.setValue("0");

            if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                setPlayPauseMD.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
            }
            mDeviceManager.publishCommandRequest(setPlayPauseMD, new Response.Listener<JobStatusResponse>() {

                        @Override
                        public void onResponse(JobStatusResponse response1) {
                            String responsebody = response1.getData().getOutput().getResponseMessage();
                            Log.i(TAG, "SERVER RESP : " + responsebody);
                            setStatus = response1.getStatus();
                            if (responsebody != null && responsebody.contains("set_playpause_motion_detection")  &&
                                    responsebody.contains("0")) {

                                try {
                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                    handleSetterResponse(listChild, true, R.string.motion_detection_changed, R.string.motion_detection_change_failed);


                                } catch (Exception ex) {
                                    handleSetterResponse(listChild, false, R.string.motion_detection_changed, R.string.motion_detection_change_failed);

                                }
                            }else{
                                setStatus = -1;
                                handleSetterResponse(listChild, false, R.string.motion_detection_changed, R.string.motion_detection_change_failed);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }
                            setStatus = -1;
                            handleSetterResponse(listChild, false, R.string.motion_detection_changed, R.string.motion_detection_change_failed);

                        }
                    }, isLocal

            );

        }
        // success = (status == 200);


        int type = pos == 2 ? SendCommandEvent.SET_MOTION_VDA_BSC : SendCommandEvent.SET_MOTION_VDA_OTHER;
        SendCommandEvent sendCommandEvent = new SendCommandEvent(type, (setStatus == 200));
        EventBus.getDefault().post(sendCommandEvent);

    }

    private void setNightVision(final ListChild listChild, int nightVisionMode, int nightVisionIntensity) {
        //mDevice.sendCommandGetSuccess("set_night_vision", nightVisionMode + "", null);
        //SendCommand setNightVision = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_night_vision"+"&value="+nightVisionMode);
        PublishCommand setNightVision = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_night_vision", null);
        setNightVision.setValue(nightVisionIntensity + "");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setNightVision.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(setNightVision, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_night_vision") &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }


                    }
                }, isLocal

        );

        //boolean success = mDevice.sendCommandGetSuccess("set_ir_pwm", nightVisionIntensity + "", null);

        // SendCommand setNightVisionIntensity = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_ir_pwm" +"&value="+nightVisionIntensity);
        PublishCommand setNightVisionIntensity = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_night_vision", null);
        setNightVisionIntensity.setValue(nightVisionIntensity + "");
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setNightVision.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(setNightVisionIntensity, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        boolean success = false;
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_ir_pwm")  &&
                                responsebody.contains("0")) {

                            try {
                                success = true;
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                            } catch (Exception ex) {

                            }
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.NIGHT_VERSION, success);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, success, R.string.night_vision_changed, R.string.nightvision_change_failed);
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.NIGHT_VERSION, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.night_vision_changed, R.string.nightvision_change_failed);
                    }
                }, isLocal

        );

    }

    private void setNightVisionHubble(final ListChild listChild, int nightVisionMode, boolean useCommandIR) {
        final String command = useCommandIR ? "set_ir_mode" : "set_night_vision";
        // boolean success = mDevice.sendCommandGetSuccess(command, nightVisionMode + "", null);

        // SendCommand setNightVision = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, command+"&value="+ nightVisionMode);
        PublishCommand setNightVision = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, command, null);
        setNightVision.setValue(nightVisionMode + "");
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setNightVision.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }


        mDeviceManager.publishCommandRequest(setNightVision, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        boolean success = false;
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains(command)  &&
                                responsebody.contains("0")) {

                            try {
                                success = true;
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                            } catch (Exception ex) {

                            }
                        } else {
                            success = false;
                        }
                        CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_VISION, listChild.intValue);
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.NIGHT_VERSION, success);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, success, R.string.night_vision_changed, R.string.nightvision_change_failed);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.NIGHT_VERSION, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.night_vision_changed, R.string.nightvision_change_failed);

                    }
                }, isLocal
        );


    }

    //TODO test
    private void setPark(final ListChild listChild, boolean isEnabled, int parkTimer) {
        int parkEnabledCode = isEnabled ? 1 : 0;
        int parkTimerInSeconds = parkTimer * 60;
        //  mDevice.sendCommandGetSuccess("set_cam_park", parkEnabledCode + "", null);
        //SendCommand setCameraPark = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_cam_park"+"&value=" + parkEnabledCode);
        PublishCommand setCameraPark = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_cam_park", null);
        setCameraPark.setValue(parkEnabledCode + "");
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setCameraPark.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setCameraPark, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_cam_park")  &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }, isLocal

        );

        // boolean success = mDevice.sendCommandGetSuccess("set_park_timer", parkTimerInSeconds + "", null);

        //SendCommand setParkTimer = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_park_timer" +"&value="+parkTimerInSeconds);
        PublishCommand setParkTimer = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_park_timer", null);
        setParkTimer.setValue(parkTimerInSeconds + "");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setParkTimer.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setParkTimer, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_park_timer")  &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_PARK_TIMER, true);
                                EventBus.getDefault().post(sendCommandEvent);

                                handleSetterResponse(listChild, true, R.string.park_settings_changed, R.string.park_mode_change_failed);
                            } catch (Exception ex) {

                            }
                        } else {
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_PARK_TIMER, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(listChild, false, R.string.park_settings_changed, R.string.park_mode_change_failed);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_PARK_TIMER, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.park_settings_changed, R.string.park_mode_change_failed);

                    }
                }, isLocal

        );

    }

    private void setSoundDetection(final ListChild soundDetection, boolean soundDetectionEnabled) {
        final String command;
        if (soundDetectionEnabled) {
            command = PublicDefineGlob.VOX_ENABLE;
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.SOUND_DETECTION + " : " + AppEvents.ENABLED, AppEvents.SOUND_DETECTION_ENABLED);

            ZaiusEvent soundDetectionEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
            soundDetectionEvt.action(AppEvents.SOUND_DETECTION + " : " + AppEvents.ENABLED);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(soundDetectionEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }
        } else {
            command = PublicDefineGlob.VOX_DISABLE;
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.SOUND_DETECTION + " : " + AppEvents.DISABLED, AppEvents.SOUND_DETECTION_DISABLED);
            ZaiusEvent soundDetectionEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
            soundDetectionEvt.action(AppEvents.SOUND_DETECTION + " : " + AppEvents.DISABLED);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(soundDetectionEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }
        }

        //SendCommand setSoundDetection = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, command);
        PublishCommand setSoundDetection = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, command, null);
        if (mDevice!=null && mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setSoundDetection.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setSoundDetection, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains(command) &&
                                     responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                CommonUtil.setSettingInfo(mContext, regId + "-" + SettingsPrefUtils.SOUND_STATUS, soundDetection.booleanValue);
                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_SOUND, true);
                                EventBus.getDefault().post(sendCommandEvent);
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_SOUND_STATUS_SUCCESS, AppEvents.SOUND_DETECTION_SUCCESS);
                                ZaiusEvent soundStatusEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                soundStatusEvt.action(AppEvents.CHANGE_SOUND_STATUS_SUCCESS);
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(soundStatusEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                                handleSetterResponse(soundDetection, true, R.string.sound_detection_changed, R.string.sound_detection_change_failed);
                            } catch (Exception ex) {
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_SOUND_STATUS_FAILURE + " : " + ex.getMessage(), AppEvents.SOUND_DETECTION_FAILURE);
                                ZaiusEvent soundStatusEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                soundStatusEvt.action(AppEvents.CHANGE_SOUND_STATUS_FAILURE + " : " + ex.getMessage());
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(soundStatusEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_SOUND, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(soundDetection, false, R.string.sound_detection_changed, R.string.sound_detection_change_failed);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_SOUND_STATUS_FAILURE + " : " + error.networkResponse.data.toString(), AppEvents.CHANGE_SOUND_STATUS_FAILURE);
                            ZaiusEvent soundStatusEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                            soundStatusEvt.action(AppEvents.CHANGE_SOUND_STATUS_FAILURE + " : " + error.networkResponse.data.toString());
                            try {
                                ZaiusEventManager.getInstance().trackCustomEvent(soundStatusEvt);
                            } catch (ZaiusException e) {
                                e.printStackTrace();
                            }
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_SOUND, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(soundDetection, false, R.string.sound_detection_changed, R.string.sound_detection_change_failed);

                    }
                }, isLocal

        );

    }

    private void setSoundThreshold(final ListChild soundDetection, int soundDetectionThreshold) {

        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.SOUND_SENSITIVITY + " :" + soundDetectionThreshold, AppEvents.SOUND_SENSITIVITY_VALUE);
        ZaiusEvent soundSensitivityEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
        soundSensitivityEvt.action(AppEvents.SOUND_SENSITIVITY + " : " + soundDetectionThreshold);
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(soundSensitivityEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }
        String soundThreshold = "";
        if (mDevice != null && mDevice.getProfile() != null && mDevice.getProfile().isVTechCamera()) {
            soundThreshold = String.valueOf(soundDetectionThreshold + 1);
        }


        //SendCommand voxSetThreshold = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, PublicDefineGlob.VOX_SET_THRESHOLD + PublicDefineGlob.VOX_SET_THRESHOLD_VALUE + soundDetectionThreshold);
        PublishCommand voxSetThreshold = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, PublicDefineGlob.VOX_SET_THRESHOLD, null);
        voxSetThreshold.setValue(soundDetectionThreshold + "");
        if (mDevice!=null && mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            voxSetThreshold.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(voxSetThreshold, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains(PublicDefineGlob.VOX_SET_THRESHOLD)  &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                CommonUtil.setSettingValue(mContext, regId + "-" + SettingsPrefUtils.SOUND_SENSITIVITY, soundDetection.intValue);
                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_SOUND, true);
                                EventBus.getDefault().post(sendCommandEvent);
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.SOUND_SENSITIVITY_CHANGE_SUCCESS, AppEvents.SOUND_SENSITIVITY_SUCCESS);
                                ZaiusEvent soundSensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                soundSensitivityChangeEvt.action(AppEvents.SOUND_SENSITIVITY_CHANGE_SUCCESS);
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(soundSensitivityChangeEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                                handleSetterResponse(soundDetection, true, R.string.sound_detection_changed, R.string.sound_detection_change_failed);

                            } catch (Exception ex) {
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.SOUND_SENSITIVITY_CHANGE_FAILURE + " : " + ex.getMessage(), AppEvents.SOUND_SENSITIVITY_FAILURE);
                                ZaiusEvent soundSensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                soundSensitivityChangeEvt.action(AppEvents.SOUND_SENSITIVITY_CHANGE_FAILURE + " : " + ex.getMessage());
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(soundSensitivityChangeEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_SOUND, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(soundDetection, false, R.string.sound_detection_changed, R.string.sound_detection_change_failed);

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.SOUND_SENSITIVITY_CHANGE_FAILURE + " : " + error.networkResponse.data.toString(), AppEvents.SOUND_SENSITIVITY_FAILURE);
                            ZaiusEvent soundSensitivityChangeEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                            soundSensitivityChangeEvt.action(AppEvents.SOUND_SENSITIVITY_CHANGE_FAILURE + " : " + error.networkResponse.data.toString());
                            try {
                                ZaiusEventManager.getInstance().trackCustomEvent(soundSensitivityChangeEvt);
                            } catch (ZaiusException e) {
                                e.printStackTrace();
                            }
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_SOUND, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(soundDetection, false, R.string.sound_detection_changed, R.string.sound_detection_change_failed);

                    }
                }, isLocal
        );
    }

    //TODO test
    private void setStatusLED(final ListChild listChild, boolean ledOn) {
        // boolean success = mDevice.sendCommandGetSuccess("set_led_func", (ledOn ? 1 : 0) + "", null);

        // SendCommand setStatusLED = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_led_func" +"&value=" +(ledOn ? 1 : 0));
        PublishCommand setStatusLED = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_led_func", null);
        setStatusLED.setValue(ledOn ? "1" : "0");
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setStatusLED.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setStatusLED, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_led_func")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_STATUS_LED, true);
                                EventBus.getDefault().post(sendCommandEvent);

                                handleSetterResponse(listChild, true, R.string.status_led_changed, R.string.status_led_change_failed);
                            } catch (Exception ex) {

                            }
                        } else {
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_STATUS_LED, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(listChild, false, R.string.status_led_changed, R.string.status_led_change_failed);

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_STATUS_LED, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.status_led_changed, R.string.status_led_change_failed);

                    }
                }, isLocal

        );


    }

    private void setTemperatureDetection(final ListChild temperature, final boolean isEnabled) {
        if (isEnabled) {
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.TEMPARATURE_DETECTION + " : " + AppEvents.ENABLED, AppEvents.TEMPARATURE_DETECTION_ENABLED);
            ZaiusEvent tempDetectionEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
            tempDetectionEvt.action(AppEvents.TEMPARATURE_DETECTION + " : " + AppEvents.ENABLED);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(tempDetectionEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }
        } else {
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.TEMPARATURE_DETECTION + " : " + AppEvents.DISABLED, AppEvents.TEMPARATURE_DETECTION_DISABLED);
            ZaiusEvent tempDetectionEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
            tempDetectionEvt.action(AppEvents.TEMPARATURE_DETECTION + " : " + AppEvents.DISABLED);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(tempDetectionEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }
        }
        String tempLowEnable = isEnabled ? "1" : "0";
        // SendCommand setTempLOEnabled = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_temp_lo_enable" +"&value="+tempLowEnable );
        PublishCommand setTempLOEnabled = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_temp_lo_enable", null);
        setTempLOEnabled.setValue(tempLowEnable);

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setTempLOEnabled.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setTempLOEnabled, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP set_temp_lo_enable : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_temp_lo_enable")  &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, true);
                                CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_STATUS, temperature.booleanValue);
                                EventBus.getDefault().post(sendCommandEvent);
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_STATUS_SUCCESS, AppEvents.TEMP_DETECTION_SUCCESS);
                                // handleSetterResponse(temperature, true, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                                ZaiusEvent tempStatusEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                tempStatusEvt.action(AppEvents.CHANGE_TEMP_STATUS_SUCCESS);
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(tempStatusEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                                setHighTemperatureDetection(temperature, isEnabled);

                            } catch (Exception ex) {
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_STATUS_FAILURE + " : " + ex.getMessage(), AppEvents.TEMP_DETECTION_FAILURE);

                                ZaiusEvent tempStatusEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                tempStatusEvt.action(AppEvents.CHANGE_TEMP_STATUS_FAILURE + " : " + ex.getMessage());
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(tempStatusEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(temperature, false, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, "set_temp_lo_enable : " + error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_STATUS_FAILURE + " : " + error.networkResponse.data.toString(), AppEvents.CHANGE_TEMP_STATUS_FAILURE);

                            ZaiusEvent tempStausEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                            tempStausEvt.action(AppEvents.CHANGE_TEMP_STATUS_FAILURE + " : " + error.networkResponse.data.toString());
                            try {
                                ZaiusEventManager.getInstance().trackCustomEvent(tempStausEvt);
                            } catch (ZaiusException e) {
                                e.printStackTrace();
                            }
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(temperature, false, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);


                    }
                }, isLocal

        );
    }

    private void setHighTemperatureDetection(final ListChild temperature, boolean highEnabled) {
        String tempHighEnable = highEnabled ? "1" : "0";
        //SendCommand setTempHighEnable = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_temp_hi_enable"+"&value="+tempHighEnable);
        PublishCommand setTempHighEnable = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_temp_hi_enable", null);
        setTempHighEnable.setValue(tempHighEnable);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setTempHighEnable.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setTempHighEnable, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP set_temp_hi_enable : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_temp_hi_enable")  &&
                                responsebody.contains("0")) {

                            try {

                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, true);
                                CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_STATUS, temperature.secondaryBooleanValue);

                                EventBus.getDefault().post(sendCommandEvent);

                                handleSetterResponse(temperature, true, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                            } catch (Exception ex) {

                            }
                        } else {
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(temperature, false, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, "set_temp_hi_enable" + error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(temperature, false, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                    }
                }, isLocal

        );


    }

    private void setLowTemperatureThreshold(final ListChild temperature, int lowThreshold) {


        //SendCommand setTempLowEnable = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_temp_lo_threshold"+"&value="+String.valueOf(lowThreshold));
        PublishCommand setTempLowEnable = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_temp_lo_threshold", null);
        setTempLowEnable.setValue(lowThreshold + "");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setTempLowEnable.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setTempLowEnable, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP set_temp_lo_threshold: " + responsebody);
                        if (responsebody != null && responsebody.contains("set_temp_lo_threshold")) {
                            if (responsebody.contains("0")) {
                                try {
                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                    CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.LOW_TEMP_VALUE, temperature.intValue);
                                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_MIN_SUCCESS + " : " + AppEvents.SUCCESS, AppEvents.CHANGETEMPMINSUCCESS);
                                    ZaiusEvent tempMinSuccessEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                    tempMinSuccessEvt.action(AppEvents.CHANGE_TEMP_MIN_SUCCESS + " : " + AppEvents.SUCCESS);
                                    try {
                                        ZaiusEventManager.getInstance().trackCustomEvent(tempMinSuccessEvt);
                                    } catch (ZaiusException e) {
                                        e.printStackTrace();
                                    }
                                    SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, true);
                                    EventBus.getDefault().post(sendCommandEvent);

                                    handleSetterResponse(temperature, true, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                                } catch (Exception ex) {
                                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_MIN_FAILURE + " : " + ex.getMessage(), AppEvents.CHANGETEMPMINFAILURE);
                                    ZaiusEvent tempMinStatusEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                    tempMinStatusEvt.action(AppEvents.CHANGE_TEMP_MIN_FAILURE + " : " + ex.getMessage());
                                    try {
                                        ZaiusEventManager.getInstance().trackCustomEvent(tempMinStatusEvt);
                                    } catch (ZaiusException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, false);
                                EventBus.getDefault().post(sendCommandEvent);
                                if (responsebody.contains("-1")) {
                                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_MIN_FAILURE + " : " + "-1", AppEvents.CHANGETEMPMINFAILURE);
                                    ZaiusEvent tempMinStatusEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                    tempMinStatusEvt.action(AppEvents.CHANGE_TEMP_MIN_FAILURE + " : " + "-1");
                                    try {
                                        ZaiusEventManager.getInstance().trackCustomEvent(tempMinStatusEvt);
                                    } catch (ZaiusException e) {
                                        e.printStackTrace();
                                    }
                                }
                                handleSetterResponse(temperature, false, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                            }
                        } else {
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(temperature, false, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, "set_temp_lo_threshold " + error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_MIN_FAILURE + " : " + error.networkResponse.data.toString(), AppEvents.CHANGETEMPMINFAILURE);

                            ZaiusEvent tempMinFailureEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                            tempMinFailureEvt.action(AppEvents.CHANGE_TEMP_MIN_FAILURE + " : " + error.networkResponse.data.toString());
                            try {
                                ZaiusEventManager.getInstance().trackCustomEvent(tempMinFailureEvt);
                            } catch (ZaiusException e) {
                                e.printStackTrace();
                            }
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(temperature, false, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                    }
                }, isLocal

        );


    }

    private void setHighTemperatureThreshold(final ListChild temperature, int highThreshold) {

        //SendCommand setTempHIThreshold = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_temp_hi_threshold"+"&value="+highThreshold);
        PublishCommand setTempHIThreshold = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_temp_hi_threshold", null);
        setTempHIThreshold.setValue(highThreshold + "");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setTempHIThreshold.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setTempHIThreshold, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP set_temp_hi_threshold: " + responsebody);
                        if (responsebody != null && responsebody.contains("set_temp_hi_threshold")) {
                            if (responsebody.contains("0")) {
                                try {
                                    final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                    CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.HIGH_TEMP_VALUE, temperature.secondaryIntValue);
                                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_MAX_SUCCESS, AppEvents.CHANGETEMPMAXSUCCESS);

                                    ZaiusEvent tempMaxEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                    tempMaxEvt.action(AppEvents.CHANGE_TEMP_MAX_SUCCESS);
                                    try {
                                        ZaiusEventManager.getInstance().trackCustomEvent(tempMaxEvt);
                                    } catch (ZaiusException e) {
                                        e.printStackTrace();
                                    }

                                    SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, true);
                                    EventBus.getDefault().post(sendCommandEvent);

                                    handleSetterResponse(temperature, true, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                                } catch (Exception ex) {
                                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_MAX_FAILURE + " : " + ex.getMessage(), AppEvents.CHANGETEMP_MAXFAILURE);
                                    ZaiusEvent maxTempEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                    maxTempEvt.action(AppEvents.CHANGE_TEMP_MAX_FAILURE + " : " + ex.getMessage());
                                    try {
                                        ZaiusEventManager.getInstance().trackCustomEvent(maxTempEvt);
                                    } catch (ZaiusException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, false);
                                EventBus.getDefault().post(sendCommandEvent);
                                if (responsebody.contains("-1")) {
                                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_MAX_FAILURE + " : " + "-1", AppEvents.CHANGETEMP_MAXFAILURE);
                                    ZaiusEvent tempMaxFailureEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                                    tempMaxFailureEvt.action(AppEvents.CHANGE_TEMP_MAX_FAILURE + " : " + "-1");
                                    try {
                                        ZaiusEventManager.getInstance().trackCustomEvent(tempMaxFailureEvt);
                                    } catch (ZaiusException e) {
                                        e.printStackTrace();
                                    }
                                }
                                handleSetterResponse(temperature, false, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                            }
                        } else {
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(temperature, false, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, "set_temp_hi_threshold" + error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_SETTING, AppEvents.CHANGE_TEMP_MAX_FAILURE + " : " + error.networkResponse.data.toString(), AppEvents.CHANGETEMP_MAXFAILURE);
                            ZaiusEvent tempMaxFailureEvt = new ZaiusEvent(AppEvents.CAMERA_SETTING);
                            tempMaxFailureEvt.action(AppEvents.CHANGE_TEMP_MAX_FAILURE + " : " + error.networkResponse.data.toString());
                            try {
                                ZaiusEventManager.getInstance().trackCustomEvent(tempMaxFailureEvt);
                            } catch (ZaiusException e) {
                                e.printStackTrace();
                            }
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TEMP, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(temperature, false, R.string.temperature_detection_changed, R.string.temperature_detection_change_failed);

                    }
                }, isLocal

        );
    }

    private void setTimeZone(final ListChild timeZone, String strTimezone) {
        String finalTimezone = strTimezone.replace(":", ".").substring(4);

        //SendCommand setTimeZone = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_time_zone"+"&setup="+finalTimezone);
        PublishCommand setTimeZone = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_time_zone", null);
        setTimeZone.setSetup(finalTimezone);

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setTimeZone.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setTimeZone, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_time_zone")  &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                handleSetterResponse(timeZone, true, R.string.timezone_changed, R.string.failed_to_set_timezone);
                            } catch (Exception ex) {

                            }
                        } else {
                            handleSetterResponse(timeZone, false, R.string.timezone_changed, R.string.failed_to_set_timezone);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        handleSetterResponse(timeZone, false, R.string.timezone_changed, R.string.failed_to_set_timezone);

                    }
                }, isLocal

        );


    }


    private void resetTimeZone(final ListChild timeZone, String strTimezone) {
        String finalTimezone = strTimezone.replace(":", ".").substring(4);

        // SendCommand setTimeZone = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_time_zone"+"&setup="+finalTimezone);
        PublishCommand setTimeZone = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_time_zone", null);
        setTimeZone.setSetup(finalTimezone);

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setTimeZone.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setTimeZone, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_time_zone")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                //   handleSetterResponse(timeZone, true, R.string.timezone_changed, R.string.failed_to_set_timezone);

                                String responseMessage = getSafeString(R.string.failed_to_set_timezone);
                                mListener.onValueSet(timeZone, true, responseMessage);

                            } catch (Exception ex) {

                            }
                        } else {
                            //handleSetterResponse(timeZone, false, R.string.timezone_changed, R.string.failed_to_set_timezone);
                            String responseMessage = getSafeString(R.string.failed_to_set_timezone);
                            mListener.onValueSet(timeZone, false, responseMessage);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        //handleSetterResponse(timeZone, false, R.string.timezone_changed, R.string.failed_to_set_timezone);
                        String responseMessage = getSafeString(R.string.failed_to_set_timezone);
                        mListener.onValueSet(timeZone, false, responseMessage);

                    }
                }, isLocal

        );


    }

    private void setVolume(final ListChild listChild, int volumeLevel) {

        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.GENERAL_SETTING, AppEvents.VOLUME_VALUE + " : " + volumeLevel, AppEvents.VOLUME_LEVEL);
        ZaiusEvent volumeEventEvt = new ZaiusEvent(AppEvents.GENERAL_SETTING);
        volumeEventEvt.action(AppEvents.VOLUME_VALUE + " : " + volumeLevel);
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(volumeEventEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }
        int volume = volumeLevel;
        if (mDevice != null && mDevice.getProfile().isStandBySupported()) {
            volume = DEVICE_VOLUME_RANGE_100_LL + (volumeLevel * DEVICE_VOLUME_RANGE_SEPARATOR);
            if (volume > DEVICE_VOLUME_RANGE_100_UL) {
                volume = DEVICE_VOLUME_RANGE_100_UL;
            }
        }


        // SendCommand setVolume = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_spk_volume" + "&setup=" + volume);
        PublishCommand setVolume = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_spk_volume", null);
        setVolume.setSetup(volume + "");
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setVolume.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setVolume, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("set_spk_volume")  &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parseResponseBody(responsebody);
                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_VOLUMNE, true);
                                EventBus.getDefault().post(sendCommandEvent);

                                handleSetterResponse(listChild, true, R.string.volume_changed, R.string.volume_change_failed);
                            } catch (Exception ex) {

                            }
                        } else {
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_VOLUMNE, false);
                            EventBus.getDefault().post(sendCommandEvent);

                            handleSetterResponse(listChild, false, R.string.volume_changed, R.string.volume_change_failed);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_VOLUMNE, false);
                        EventBus.getDefault().post(sendCommandEvent);

                        handleSetterResponse(listChild, false, R.string.volume_changed, R.string.volume_change_failed);

                    }
                }, isLocal
        );

    }

    // Helpers
    private String getAdaptiveQualityStringFromValues(ListChild listChild) {
        String value = "";
        if (listChild.booleanValue) {
            value = getSafeString(R.string.adaptive);
        } else {
            if (mDevice.getProfile().isVTechCamera()) {
                // Try catch to avoid ArrayIndexOutOfBoundsException
                try {
                    value = getSafeStringArray(R.array.resolutions)[listChild.intValue] + " - ";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Try catch to avoid ArrayIndexOutOfBoundsException
            try {
                value += getSafeStringArray(R.array.bitrate)[listChild.secondaryIntValue];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    //this method is not used , hence not changed to PublishCommand API
    private void getResolution(final ListChild listChild) {
        if (mDevice.getProfile().isVTechCamera()) {
            SendCommand getResolution = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_resolution");

            mDeviceManager.sendCommandRequest(getResolution, new Response.Listener<SendCommandDetails>() {

                        @Override
                        public void onResponse(SendCommandDetails response1) {
                            String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                            Log.i(TAG, "SERVER RESP : " + responsebody);
                            if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_resolution")) {

                                try {
                                    final Pair<String, Object> resolutionResponse = CommonUtil.parseResponseBody(responsebody);
                                    if (resolutionResponse != null && resolutionResponse.second instanceof String) {
                                        String resolutionValue = "";
                                        try {
                                            resolutionValue = ((String) resolutionResponse.second).substring(0, 4);
                                        } catch (Exception e) {
                                            resolutionValue = "-1";
                                        }

                                        int pos = 0;
                                        if (resolutionValue.equals("-1")) {
                                            pos = -1;
                                        } else {
                                            String[] resolutions = getSafeStringArray(R.array.resolutions);
                                            for (int i = 0; i < resolutions.length; i++) {
                                                if (resolutions[i].equals(resolutionValue)) {
                                                    pos = i;
                                                }
                                            }
                                        }

                                        if (pos == -1) {
                                            listChild.intValue = 0;
                                        } else {
                                            listChild.intValue = pos;
                                        }
                                    }
                                } catch (Exception ex) {

                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }

                        }
                    }

            );


        }
    }

    //this method is not used , hence not changed to PublishCommand API
    private void getBitrate(final ListChild listChild) {
        SendCommand getBitrate = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_video_bitrate");

        mDeviceManager.sendCommandRequest(getBitrate, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response1) {
                        String value = "-1";
                        String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_video_bitrate")) {

                            try {
                                final Pair<String, Object> bitrateResponse = CommonUtil.parseResponseBody(responsebody);
                                if (bitrateResponse.second instanceof Integer) {
                                    value = String.valueOf(bitrateResponse.second) + "kb/s";
                                } else if (bitrateResponse.second instanceof String) {
                                    value = (bitrateResponse.second + "kb/s");
                                }

                                int bitratePosition = 0;
                                if (value.equals("-1")) {
                                    bitratePosition = -1;
                                } else {
                                    String[] bitrates = getSafeStringArray(R.array.bitrate);
                                    for (int i = 0; i < bitrates.length; i++) {
                                        if (bitrates[i].equals(value)) {
                                            bitratePosition = i;
                                        }
                                    }
                                }
                                if (bitratePosition == -1) {
                                    listChild.secondaryIntValue = 0;
                                } else {
                                    listChild.secondaryIntValue = bitratePosition;
                                }
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                }

        );


    }

    private String getSafeString(int stringResourceId) {
        if (mContext != null) {
            return mContext.getString(stringResourceId);
        } else {
            return "";
        }
    }

    private String[] getSafeStringArray(int stringArrayResourceId) {
        if (mContext != null) {
            return mContext.getResources().getStringArray(stringArrayResourceId);
        } else {
            return new String[]{""};
        }
    }

    private String getNightVisionStringFromIntValue(int nightVisionMode) {
        String[] nightVisionModes = getSafeStringArray(R.array.night_vision_modes);
        if (nightVisionMode < nightVisionModes.length && nightVisionMode >= 0) {
            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.NIGHT_VISION, nightVisionMode);
            return nightVisionModes[nightVisionMode];
        } else {
            return getSafeString(R.string.failed_to_retrieve_camera_data);
        }
    }

    private void handleSetterResponse(ListChild listChild, boolean success, int successResource, int failureResource) {
        String responseMessage;
        if (listChild != null && listChild.title != null &&
                listChild.title.equals(getSafeString(R.string.timezone))) {
            if (success) {
                updateTimeZone(listChild.value.replace("GMT ", "").replace(":", "."), listChild);
            } else {
                responseMessage = getSafeString(failureResource);
                mListener.onValueSet(listChild, !success, responseMessage);
            }
            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_TIME_ZONE, success);
            EventBus.getDefault().post(sendCommandEvent);
        } else {
            if (success) {
                responseMessage = getSafeString(successResource);
            } else {
                responseMessage = getSafeString(failureResource);
            }
            mListener.onValueSet(listChild, !success, responseMessage);
        }
    }

    private void handleNotificationSetResponse(ListChild listChild, boolean success, int successResource, int failureResource) {
        String responseMessage;

        if (success) {
            responseMessage = getSafeString(successResource);
        } else {
            responseMessage = getSafeString(failureResource);
        }
        mListener.onMotionNotificationChange(listChild, !success, responseMessage);

    }


    public interface MVRListener {
        void onMVRResponse(boolean success);
    }

    public interface RecodngPlanListener {
        void onRecordingPlanResponse(boolean success, int plan);
    }


    // Interface
    public interface Interface {
        void onDataSetChanged(ListChild listChild);

        // Getters
        void onNotificationSettingsReceived();

        void onParkReceived(Pair<String, Object> response);

        void onParkTimerReceived(Pair<String, Object> response);

        void onMotionNotificationChange(ListChild listChild, boolean success, String responseMessage);

        // Setters
        void onValueSet(ListChild listChild, boolean shouldRevert, String responseMessage);

        void onScheduleDataReceived();

    }

    private void updateTimeZone(final String timeZone, final ListChild listChild) {
        Thread update = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String apiKey = Global.getApiKey(mContext);
                try {
                    String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                    Models.TimeZone obj = new Models.TimeZone();
                    obj.setTimeZone(timeZone);
                    Models.ApiResponse<Models.TimeZone> tzRes = Api.getInstance().getService().changeTimeZone(mDevice.getProfile().registrationId, apiKey, obj);


                    if (tzRes != null && timeZone.replace("-0", "-").contains(tzRes.getData().getTimeZone())) {
                        mListener.onValueSet(listChild, false, getSafeString(R.string.timezone_changed));

                    } else {

                        //  mListener.onValueSet(listChild, true, getSafeString(R.string.failed_to_set_timezone));
              /* For some reason writing camera timezone to server fails,
              revert the newly set camera timezone from camera to old timezone.
              Else we may endup in having different timezones for same camera one in server and one in camera */

                        listChild.revertToOldCopy();
                        resetTimeZone(listChild, listChild.value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
             /* For some reason writing camera timezone to server fails,
              revert the newly set camera timezone from camera to old timezone.
              Else we may endup in having different timezones for same camera one in server and one in camera */

                    listChild.revertToOldCopy();
                    resetTimeZone(listChild, listChild.value);
                    // mListener.onValueSet(listChild, true, getSafeString(R.string.failed_to_set_timezone));
                }
                Looper.loop();
            }
        });

        update.start();
    }

    //TODO test
    private void getViewMode(final ListChild listChild, final ListChild qos) {
        //SendCommand getViewMode = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "get_video_driver_view_mode");
        PublishCommand getViewMode = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_video_driver_view_mode", null);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getViewMode.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(getViewMode, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("get_video_driver_view_mode")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                if (response != null && response.second instanceof Float) {
                                    listChild.intValue = ((Float) response.second).intValue();
                                    if (listChild.intValue == 1) {
                                        listChild.value = getSafeString(R.string.view_mode_narrow);
                                    } else if (listChild.intValue == 0) {
                                        listChild.value = getSafeString(R.string.view_mode_wide);
                                    } else {
                                        listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                    }

                                    CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIEW_MODE, listChild.intValue);

                                    if (qos != null) {
                                        // if qos has no value -> load it. else -> update value
                                        if (qos.value.equals(getSafeString(R.string.loading))) {
                                            getQualityOfService(qos, 1);
                                        } else {
                                            if (!qos.booleanValue && qos.intValue == 2) {
                                                if (listChild.intValue == 1)
                                                    qos.value = getSafeString(R.string.hd_enhance);
                                                else
                                                    qos.value = getSafeString(R.string.full_hd);
                                            }
                                        }
                                    }
                                } else {
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                    listChild.intValue = 0;
                                }
                                mListener.onDataSetChanged(listChild);
                            } catch (Exception ex) {

                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        mListener.onDataSetChanged(listChild);
                    }
                }, isLocal

        );

    }

    //TODO test
    private void getQualityOfService(final ListChild listChild, final int viewMode) {
        //  String response = CommandUtils.sendCommand(mDevice, "get_video_qos", isInLocal);
        // SendCommand getViewMode = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "get_video_qos");
        PublishCommand getViewMode = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_video_qos", null);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getViewMode.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(getViewMode, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String response = response1.getData().getOutput().getResponseMessage();
                        if ("get_video_qos: 1".equals(response)) {
                            listChild.booleanValue = true;
                            listChild.value = getSafeString(R.string.auto);
                        } else if ("get_video_qos: 0".equals(response)) {
                            // get video quality setting
                            listChild.booleanValue = false;
                            getQualityLevel(listChild, viewMode);
                           /* response = CommandUtils.sendCommand(mDevice, "get_video_quality_level", isInLocal);
                            if (response != null && response.startsWith("get_video_quality_level")) {
                                response = response.replace("get_video_quality_level: ", "");
                            }
                            try {
                                listChild.intValue = Integer.parseInt(response);
                            } catch (Exception ignore) {
                                listChild.intValue = -1;
                            }
                            switch (listChild.intValue) {
                                case 0: // low quality
                                    listChild.value = getSafeString(R.string.sd);
                                    break;
                                case 1: // medium quality
                                    listChild.value = getSafeString(R.string.hd);
                                    break;
                                case 2: // high quality
                                    if (viewMode == 1) { // narrow
                                        listChild.value = getSafeString(R.string.hd_enhance);
                                    } else if (viewMode == 0) { // wide
                                        listChild.value = getSafeString(R.string.full_hd);
                                    }
                                    break;
                                default:
                                    listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                    break;
                            }*/
                        } else {
                            listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        mListener.onDataSetChanged(listChild);
                    }
                }, isLocal
        );


    }

    //TODO test
    private void getQualityLevel(final ListChild listChild, final int viewMode) {
        //  String response = CommandUtils.sendCommand(mDevice, "get_video_qos", isInLocal);
        // SendCommand getViewMode = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "get_video_quality_level");
        PublishCommand getViewMode = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_video_quality_level", null);
        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            getViewMode.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        mDeviceManager.publishCommandRequest(getViewMode, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String response = response1.getData().getOutput().getResponseMessage();
                        if (response != null && response.startsWith("get_video_quality_level")) {
                            response = response.replace("get_video_quality_level: ", "");
                        }
                        try {
                            listChild.intValue = Integer.parseInt(response);
                        } catch (Exception ignore) {
                            listChild.intValue = -1;
                        }
                        switch (listChild.intValue) {
                            case 0: // low quality
                                listChild.value = getSafeString(R.string.sd);
                                break;
                            case 1: // medium quality
                                listChild.value = getSafeString(R.string.hd);
                                break;
                            case 2: // high quality
                                if (viewMode == 1) { // narrow
                                    listChild.value = getSafeString(R.string.hd_enhance);
                                } else if (viewMode == 0) { // wide
                                    listChild.value = getSafeString(R.string.full_hd);
                                }
                                break;
                            default:
                                listChild.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                break;
                        }
                        mListener.onDataSetChanged(listChild);
                    }
                }, new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        mListener.onDataSetChanged(listChild);
                    }
                }, isLocal

        );
    }

    //TODO test
    private void setViewMode(final ListChild listChild, final ListChild qos) {
        String oldmode = listChild.intValue == 1 ? "narrow" : "wide";
        final String newmode = listChild.intValue == 1 ? "narrow" : "wide";
        Log.d(TAG, "switch view mode from " + oldmode + " to " + newmode);
        // boolean success = mDevice.sendCommandGetSuccess("set_view_mode", newmode, null);

        //SendCommand setViewMode = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "switch_video_driver_view_mode" +"&value="+ newmode);
        PublishCommand setViewMode = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "switch_video_driver_view_mode", null);
        setViewMode.setValue(newmode);

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setViewMode.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setViewMode, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains("switch_video_driver_view_mode")  &&
                                responsebody.contains("0")) {

                            try {
                                final Pair<String, Object> response = CommonUtil.parsePublishResponseBody(responsebody);
                                listChild.intValue = newmode.equals("wide") ? 0 : 1;
                                if (listChild.intValue == 1) {
                                    listChild.value = getSafeString(R.string.view_mode_narrow);
                                    listChild.value = getSafeString(R.string.view_mode_narrow);
                                    if (qos != null && qos.value.equals(getSafeString(R.string.full_hd))) {
                                        qos.value = getSafeString(R.string.hd_enhance);
                                    }
                                } else if (listChild.intValue == 0) {
                                    listChild.value = getSafeString(R.string.view_mode_wide);
                                    if (qos != null && qos.value.equals(getSafeString(R.string.hd_enhance))) {
                                        qos.value = getSafeString(R.string.full_hd);
                                    }
                                }
                                mListener.onDataSetChanged(listChild);

                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_VIEW_MODE, true);
                                EventBus.getDefault().post(sendCommandEvent);
                            } catch (Exception ex) {

                            }
                        } else {
                            listChild.intValue = listChild.oldIntValue;
                            mListener.onDataSetChanged(listChild);
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_VIEW_MODE, false);
                            EventBus.getDefault().post(sendCommandEvent);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        listChild.intValue = listChild.oldIntValue;
                        mListener.onDataSetChanged(listChild);
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_VIEW_MODE, false);
                        EventBus.getDefault().post(sendCommandEvent);

                    }
                }, isLocal

        );

    }

    boolean success = false;

    //TODO test
    private void setQualityOfService(final ListChild listChild, final int position) {

        boolean isInLocal = mDevice.isAvailableLocally();
        if (position == 0) {
            // String response = CommandUtils.sendCommand(mDevice, "set_video_qos&value=1", isInLocal);
            // SendCommand setViewMode = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_video_qos&value=1");
            PublishCommand setViewMode = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_video_qos", null);
            setViewMode.setValue("1");

            if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
                setViewMode.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
            }

            mDeviceManager.publishCommandRequest(setViewMode, new Response.Listener<JobStatusResponse>() {

                        @Override
                        public void onResponse(JobStatusResponse response1) {

                            String responsebody = response1.getData().getOutput().getResponseMessage();
                            success = "set_video_qos: 0".equals(responsebody);
                            Log.i(TAG, "SERVER RESP : " + responsebody);
                            if (responsebody != null && responsebody.contains("set_video_qos")) {

                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }
                            SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_QUALITY, false);
                            EventBus.getDefault().post(sendCommandEvent);

                        }
                    }

            );

        } else {
            final String response = "na";
            if (listChild.oldBooleanValue) {
                //  response = CommandUtils.sendCommand(mDevice, "set_video_qos&value=0", isInLocal);

                //SendCommand setViewMode = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_video_qos&value=0");
                PublishCommand setViewMode = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_video_qos", null);
                setViewMode.setValue("0");

                mDeviceManager.publishCommandRequest(setViewMode, new Response.Listener<JobStatusResponse>() {

                            @Override
                            public void onResponse(JobStatusResponse response1) {

                                String responsebody = response1.getData().getOutput().getResponseMessage();
                                success = "set_video_qos: 0".equals(responsebody);
                                Log.i(TAG, "SERVER RESP : " + responsebody);
                                if (responsebody != null && responsebody.contains("set_video_qos")) {
                                    if ("na".equals(response) || "set_video_qos: 0".equals(responsebody)) {
                                        int quality = position - 1; // because offset 0 is Auto
                                        // this command depends on the previous, it should be success here.
                                        int counter = 0;
                                        setQualityLevel(listChild, position);

                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                if (error != null && error.networkResponse != null) {
                                    Log.d(TAG, error.networkResponse.toString());
                                    Log.d(TAG, error.networkResponse.data.toString());
                                }
                                SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_QUALITY, false);
                                EventBus.getDefault().post(sendCommandEvent);

                            }
                        }, isLocal

                );
            }

        }

        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_QUALITY, success);
        EventBus.getDefault().post(sendCommandEvent);

        handleSetterResponse(listChild, success, R.string.video_quality_settings_changed, R.string.failed_to_change_video_quality);
    }

    //TODO test
    public void setQualityLevel(final ListChild listChild, int position) {
        int quality = position - 1; // because offset 0 is Auto
        // this command depends on the previous, it should be success here.
        // SendCommand setViewMode = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "set_video_quality_level&value="+ quality);
        PublishCommand setViewMode = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_video_quality_level", null);
        setViewMode.setValue(quality + "");

        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setViewMode.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        mDeviceManager.publishCommandRequest(setViewMode, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {

                        String responsebody = response1.getData().getOutput().getResponseMessage();

                        if (responsebody != null && responsebody.contains("set_video_quality_level")) {
                            handleSetterResponse(listChild, success, R.string.video_quality_settings_changed, R.string.failed_to_change_video_quality);

                        } else {
                            handleSetterResponse(listChild, false, R.string.video_quality_settings_changed, R.string.failed_to_change_video_quality);

                        }
                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_QUALITY, true);
                        EventBus.getDefault().post(sendCommandEvent);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        handleSetterResponse(listChild, success, R.string.video_quality_settings_changed, R.string.failed_to_change_video_quality);

                        SendCommandEvent sendCommandEvent = new SendCommandEvent(SendCommandEvent.SET_QUALITY, false);
                        EventBus.getDefault().post(sendCommandEvent);

                    }
                }, isLocal

        );
    }


    public void setRecordingParameter(String recordingValue, final MVRListener listener) {

        String deviceRegId;
        if (mDevice == null || mDevice.getProfile() == null)
            deviceRegId = regId;
        else
            deviceRegId = mDevice.getProfile().registrationId;

        DeviceManager deviceManager = DeviceManager.getInstance(mContext);
        //SendCommand setRecording = new SendCommand(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
        //	  deviceRegId, PublicDefineGlob.SET_RECORDING_PARAMETER_CMD + PublicDefineGlob.SET_VALUE_CONSTANT + recordingValue);
        PublishCommand setRecording = new PublishCommand(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), deviceRegId, PublicDefineGlob.SET_RECORDING_PARAMETER_CMD, null);
        setRecording.setValue(recordingValue);


        if (mDevice!=null && mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setRecording.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        deviceManager.publishCommandRequest(setRecording, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response) {
                        String responsebody = response.getData().getOutput().getResponseMessage();
                        boolean success = false;
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains(PublicDefineGlob.SET_RECORDING_PARAMETER_CMD) && responsebody.contains("0")) {
                            success = true;
                        } else {
                            success = false;
                        }
                        listener.onMVRResponse(success);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onMVRResponse(false);
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                    }
                }, isLocal
        );

    }

    //Same as applyRecordingPlan from old app
    public void setRecordingPlan(final String recordingPlanValue, final MVRListener listener) {
        DeviceManager deviceManager = DeviceManager.getInstance(mContext);
        /*SendCommand setRecordingPlan = new SendCommand(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
			mDevice.getProfile().getRegistrationId(),
			PublicDefineGlob.SET_RECORDING_PLAN_CMD + PublicDefineGlob.SET_VALUE_CONSTANT + recordingPlanValue);*/
        PublishCommand setRecordingPlan = new PublishCommand(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
                mDevice.getProfile().getRegistrationId(), PublicDefineGlob.SET_RECORDING_PLAN_CMD, null);
        setRecordingPlan.setValue(recordingPlanValue);


        if (mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setRecordingPlan.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }

        deviceManager.publishCommandRequest(setRecordingPlan, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response) {
                        String responsebody = response.getData().getOutput().getResponseMessage();
                        boolean success = false;
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains(PublicDefineGlob.SET_RECORDING_PLAN_CMD)  &&
                                responsebody.contains("0")) {
                          /*  StringTokenizer st = new StringTokenizer(responsebody, ":");
                            while (st.hasMoreTokens()) {
                                if (st.nextToken().equalsIgnoreCase(PublicDefineGlob.SET_RECORDING_PLAN_CMD) &&
                                        st.nextToken().replaceAll("\\D+", "").equalsIgnoreCase("0")) {*/
                                    success = true;
                                    Log.d(TAG, "Set recording plan succeeded");
                                    if (Integer.valueOf(recordingPlanValue) == 0) {
                                        CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_DELETE_LAST_TEN, true);
                                        CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_SWITCH_CLOUD, false);

                                    } else if (Integer.valueOf(recordingPlanValue) == 1) {
                                        CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_SWITCH_CLOUD, true);
                                        CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_DELETE_LAST_TEN, false);

                                    }
                               // }
                         //   }
                        } else {
                            success = false;
                            Log.d(TAG, "Set recording plan failed");
                        }
                        listener.onMVRResponse(success);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onMVRResponse(false);
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                    }
                }, isLocal

        );
    }


    //Same as queryRecordingPlanValue from old app
    public void getRecordingPlanValue(final RecodngPlanListener listener) {
        DeviceManager deviceManager = DeviceManager.getInstance(mContext);
		/*SendCommand setRecordingPlan = new SendCommand(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
			mDevice.getProfile().getRegistrationId(),
			PublicDefineGlob.GET_RECORDING_PLAN_CMD + PublicDefineGlob.SET_VALUE_CONSTANT + null);*/
        PublishCommand setRecordingPlan = new PublishCommand(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
                mDevice.getProfile().getRegistrationId(),
                PublicDefineGlob.GET_RECORDING_PLAN_CMD, null);
        if (mDevice!=null && mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setRecordingPlan.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }


        deviceManager.publishCommandRequest(setRecordingPlan, new Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response) {
                        String responsebody = response.getData().getOutput().getResponseMessage();
                        boolean success = false;
                        int recordingPlan = -1;
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && responsebody.contains(PublicDefineGlob.GET_RECORDING_PLAN_CMD)) {
                            StringTokenizer st = new StringTokenizer(responsebody, ":");
                            while (st.hasMoreTokens()) {
                                if (st.nextToken().equalsIgnoreCase(PublicDefineGlob.GET_RECORDING_PLAN_CMD)) {
                                    String val = st.nextToken().replaceAll("\\D+", "");
                                    recordingPlan = Integer.valueOf(val);
                                    Log.i(TAG, "SERVER RESP : Recording plan is " + recordingPlan);
                                }
                            }
                            success = true;
                        } else {
                            success = false;
                        }

                        if (success) {
                            if (recordingPlan == 0) {
                                CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_DELETE_LAST_TEN, true);
                                CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_SWITCH_CLOUD, false);

                            } else if (recordingPlan == 1) {
                                CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_SWITCH_CLOUD, true);
                                CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.SD_CARD_FULL_DELETE_LAST_TEN, false);

                            }

                        }
                        listener.onRecordingPlanResponse(success, recordingPlan);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onRecordingPlanResponse(false, -1);
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                    }
                }, isLocal

        );
    }


    private void setMotionNotification(final ListChild listChild, final boolean motionDetectionEnabled) {

        String commandValue = "";
        PublishCommand setRecording = null;
        if (mIsOrbit || (mDevice != null && mDevice.getProfile() != null && mDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT))) {
            if (motionDetectionEnabled)
                commandValue = "60";
            else
                commandValue = "0";
            setRecording = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_pir_sensitivity", null);
            setRecording.setValue(commandValue);

        } else {

            setRecording = new PublishCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "set_motion_area", null);

            if (motionDetectionEnabled) {
                //commandValue = PublicDefineGlob.MOTION_ON_PARAM;
                setRecording.setMvrToggleGrid(PublicDefineGlob.MOTION_ON_GRID_VALUE);
                setRecording.setMvrToggleZone(PublicDefineGlob.MOTION_ON_ZONE_VALUE);
            } else {
                //commandValue = PublicDefineGlob.MOTION_OFF_PARAM;
                setRecording.setMvrToggleGrid(PublicDefineGlob.MOTION_OFF_GRID_VALUE);
                setRecording.setMvrToggleZone(PublicDefineGlob.MOTION_OFF_ZONE_VALUE);
            }
        }
        if (mDevice!=null && mDevice.getProfile() != null && mDevice.getProfile().getDeviceLocation() != null) {
            setRecording.setDeviceIP(mDevice.getProfile().getDeviceLocation().getLocalIp());
        }
        DeviceManager.getInstance(mContext).publishCommandRequest(setRecording, new com.android.volley.Response.Listener<JobStatusResponse>() {

                    @Override
                    public void onResponse(JobStatusResponse response1) {
                        String responsebody = response1.getData().getOutput().getResponseMessage();
                        Log.i(TAG, "SERVER RESP : " + responsebody);
                        if (responsebody != null && (responsebody.contains("set_motion_area") || responsebody.contains("set_pir_sensitivity"))) {

                            try {
                                if (responsebody.contains("0")) {
                                    CommonUtil.setSettingInfo(mContext, regId + "-" + SettingsPrefUtils.MOTION_STATUS, listChild.booleanValue);
                                    listChild.value = getSafeString(R.string.motion_detection);

                                    handleNotificationSetResponse(listChild, true, R.string.motion_detection_changed, R.string.motion_detection_change_failed);

                                } else {
                                    //errorHandler(motionSwitch,motionDetectionEnabled);
                                    handleNotificationSetResponse(listChild, false, R.string.motion_detection_changed, R.string.motion_detection_change_failed);
                                }


                            } catch (Exception ex) {
                                // errorHandler(motionSwitch,motionDetectionEnabled);
                                handleNotificationSetResponse(listChild, false, R.string.motion_detection_changed, R.string.motion_detection_change_failed);
                            }
                        } else {
                            handleNotificationSetResponse(listChild, false, R.string.motion_detection_changed, R.string.motion_detection_change_failed);
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleNotificationSetResponse(listChild, false, R.string.motion_detection_changed, R.string.motion_detection_change_failed);
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }

                    }
                },isLocal

        );

    }

    public synchronized void getMotionSchedule() {
        new AsyncTask<Void, Void, Models.DeviceSchedule>() {
            @Override
            protected Models.DeviceSchedule doInBackground(Void... params) {
                Models.DeviceSchedule mSchedule = new Models.DeviceSchedule();
                if (mDevice != null) {
                    String deviceId = mDevice.getProfile().getRegistrationId();
                    String apiKey = Global.getApiKey(mContext);
                    Log.d(TAG, "Querying mvr schedule");
                    Models.ApiResponse<Models.DeviceSchedule> res = null;
                    try {
                        res = Api.getInstance().getService().getDeviceSchedule(apiKey, deviceId);
                    } catch (Exception ex) {
                        Log.d(TAG, "Query MVR Schedule error");
                        ex.printStackTrace();
                    }
                    Log.d(TAG, "Query mvr schedule done");
                    if (res != null && res.getStatus().equalsIgnoreCase("200")) {
                        // handle response
                        mSchedule = res.getData();
                        if (mSchedule != null) {
                            mSchedule.parse();
                            Log.d(TAG, "Parsing schedule successful");
                        } else {
                            Log.d(TAG, "Parsing schedule done, schedule is null");
                        }
                    } else {
                        Log.d(TAG, "Query mvr schedule failed");
                    }
                }
                return mSchedule;
            }

            @Override
            protected void onPostExecute(Models.DeviceSchedule schedule) {
                if (schedule != null) {
                    CommonUtil.setSettingSchedule(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MVR_SCHEDULE, schedule);
                    mListener.onScheduleDataReceived();
                }
            }
        }.execute();
    }

    public void getRecordingParameter(final ListChild motionDetection) {
        //This method is only used for orbit and default storage mode is set to SDCARD of its null
        String deviceRegId;
        if (mDevice == null || mDevice.getProfile() == null)
            deviceRegId = regId;
        else
            deviceRegId = mDevice.getProfile().registrationId;

        DeviceManager deviceManager = DeviceManager.getInstance(mContext);
        SendCommand getRecording = new SendCommand(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),
                deviceRegId, PublicDefineGlob.GET_RECORDING_PARAMETER_CMD);
        deviceManager.sendCommandRequest(getRecording, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response) {
                        String responsebody = response.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP for get recording parameter : " + responsebody);
                        if (response.getDeviceCommandResponse() != null && responsebody.contains(PublicDefineGlob.GET_RECORDING_PARAMETER_CMD)) {
                            try {
                                Pair<String, Object> responseVal = CommonUtil.parseResponseBody(responsebody);
                                if (response != null && responseVal.second instanceof Float) {
                                    int recordingVal = ((Float) responseVal.second).intValue();
                                    if (recordingVal == 11) {
                                        motionDetection.secondaryBooleanValue = true;
                                        final String oldStorageMode = mDevice.getProfile().getDeviceAttributes().getStorageMode();
                                        Log.i(TAG, "Inside getRecordingParameter record storage mode: " + oldStorageMode);
                                        if (oldStorageMode != null && oldStorageMode.equalsIgnoreCase("0")) {
                                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 0);
                                        } else {
                                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 1);

                                        }
                                        //   final String oldStorageMode = mDevice.getProfile().getDeviceAttributes().getStorageMode();
                                        // mvr is always updated everytime going to Camera Settings. so that just check when storage mode has value.
                                        if (motionDetection != null && motionDetection.secondaryBooleanValue &&
                                                !TextUtils.isEmpty(oldStorageMode) && mDevice.getProfile().doesSupportSDCardAccess()) {
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Log.d(TAG, "Update storage mode: Reload camera list ...");
                                                    final ListenableFuture<Object> listenableFuture = DeviceSingleton.getInstance().update(false);
                                                    Futures.addCallback(listenableFuture, new FutureCallback<Object>() {
                                                        @Override
                                                        public void onSuccess(Object result) {
                                                            Log.d(TAG, "Update storage mode: Update MotionDialog layout");
                                                            if (mContext != null && mContext != null) {

                                                                Device reloadedDevice = DeviceSingleton.getInstance().getDeviceByRegId(mDevice.getProfile().getRegistrationId());
                                                                if (reloadedDevice != null) {
                                                                    String newStorageMode = reloadedDevice.getProfile().getDeviceAttributes().getStorageMode();
                                                                    Log.d(TAG, "Update storage mode: New storage mode: " + newStorageMode);
                                                                    if (!TextUtils.isEmpty(newStorageMode) && !oldStorageMode.equals(newStorageMode)) {
                                                            /*  Toast.makeText(mContext.getActivity(), mContext.getString(R.string.storage_mode_has_been_changed), Toast.LENGTH_SHORT).show();
                                                              updateMvrStorageLayout(reloadedDevice);*/

                                                                        if (newStorageMode != null && newStorageMode.equalsIgnoreCase("0")) {
                                                                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 0);

                                                                        } else {
                                                                            CommonUtil.setSettingValue(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.VIDEO_STORAGE_MODE, 1);

                                                                        }
                                                                    }
                                                                }

                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Throwable t) {
                                                            Log.d(TAG, "Update storage mode: Reload device lists failed", t);
                                                        }
                                                    });
                                                }
                                            }).start();
                                        }
                                    } else {
                                        motionDetection.secondaryBooleanValue = false;
                                    }
                                    CommonUtil.setSettingInfo(mContext, mDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.MOTION_VIDEO_RECORDING, motionDetection.secondaryBooleanValue);

                                } else {
                                    if (motionDetection != null) {
                                        motionDetection.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                                    }
                                }
                            } catch (Exception ex) {

                            }
                        } else {
                            if (motionDetection != null) {
                                motionDetection.value = getSafeString(R.string.failed_to_retrieve_camera_data);
                            }
                        }
                        mListener.onNotificationSettingsReceived();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mListener.onNotificationSettingsReceived();
                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                    }
                }

        );

    }


}