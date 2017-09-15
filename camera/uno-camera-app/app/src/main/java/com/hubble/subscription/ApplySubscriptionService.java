package com.hubble.subscription;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.hubble.HubbleApplication;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.util.CommonConstants;

import java.util.List;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.devices.SerializableDeviceProfile;
import base.hubble.subscriptions.DeviceSubscription;
import base.hubble.subscriptions.FreeTrial;
import com.hubbleconnected.camera.BuildConfig;

/**
 * Created by Pragnya on 24-01-2017.
 */
public class ApplySubscriptionService extends IntentService {

	private final String TAG = "ApplySubsService";
	private String mAccessToken;
	private String mRegistrationId;
	private SubscriptionCommandUtil mSubscriptionUtil;

	@Override
	public void onCreate() {
		super.onCreate();
		mAccessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
		mSubscriptionUtil = new SubscriptionCommandUtil(this, mAccessToken);
	}

	public ApplySubscriptionService() {
		super(ApplySubscriptionService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Service Started!");
		if (BuildConfig.ENABLE_SUBSCRIPTIONS && intent != null) {
			SerializableDeviceProfile deviceProfile = getSerializableDeviceProfile();
			if (deviceProfile == null || "0877".equals(deviceProfile.getDeviceModelId()) || "0082".equals(deviceProfile.getDeviceModelId())
					|| "0821".equals(deviceProfile.getDeviceModelId()) || ("0080".equals(deviceProfile.getDeviceModelId())
					&& !Util.isThisVersionGreaterThan(deviceProfile.getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION))) {

				Log.d(TAG, "skip subscription flow for these models: 0877, 0082, 0821 0080");
				return;
			}
			mRegistrationId = intent.getStringExtra(CommonConstants.REGISTRATION_ID);
			if (mRegistrationId != null) {
				if (hasActiveDeviceSubscription() || hasFreeTrialDeviceAlready()) {
					Log.d(TAG, "user has subscription or activated free trial for this device already");
					Log.d(TAG, "enabling recording");
					mSubscriptionUtil.enableMotionVideoRecording(mRegistrationId);
				} else {
					mSubscriptionUtil.disableMotionVideoRecording(mRegistrationId);
				}
			}

		}
	}

	private boolean hasActiveDeviceSubscription() {
		try {
			Models.ApiResponse<Models.DeviceSubscriptionData> response = Api.getInstance().getService().getDeviceSubscriptions(mAccessToken);
			List<DeviceSubscription> subscriptions = response.getData().getDevices();
			DeviceSubscription activeDeviceSubscription = null;
			if (response.getData() != null) {
				for (DeviceSubscription deviceSubscription : subscriptions) {
					if (deviceSubscription.getRegistrationId().equalsIgnoreCase(mRegistrationId)) {
						activeDeviceSubscription = deviceSubscription;
						break;
					}
				}
			}
			boolean isActive = activeDeviceSubscription != null && activeDeviceSubscription.getPlanId() != null &&
					!activeDeviceSubscription.getPlanId().isEmpty() && !activeDeviceSubscription.getPlanId().equalsIgnoreCase("freemium");
			Log.d(TAG, "Subscription is active " + isActive);
			return isActive;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean hasFreeTrialDeviceAlready() {
		try {
			Models.ApiResponse<FreeTrial> response = Api.getInstance().getService().getFreeTrialInfo(mAccessToken, mRegistrationId);
			FreeTrial freeTrial = response.getData();
			boolean isFreeTrialActive = freeTrial != null && freeTrial.getStatus() != null && freeTrial.getStatus().equals("active");
			Log.d(TAG, "Free trial is active" + isFreeTrialActive);
			return isFreeTrialActive;
		} catch (Exception e) {
			return false;
		}
	}

	private SerializableDeviceProfile getSerializableDeviceProfile() {
		try {
			return Api.getInstance().getService().getDeviceProfile(mAccessToken, mRegistrationId).getData();
		} catch (Exception e) {
			return null;
		}
	}

}
