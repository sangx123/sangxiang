package com.hubble.subscription;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.base.hubble.subscriptions.SubscriptionCommandExecutor;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;

/**
 * Created by Admin on 24-01-2017.
 */
public class SubscriptionCommandUtil {

	private final String TAG = "SubscriptionUtil";

	private Context mContext;
	private String mAccessToken;

	public SubscriptionCommandUtil(Context context, String accessToken) {
		mContext = context;
		mAccessToken = accessToken;
	}

	public void enableMotionVideoRecording(final String registrationId) {

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				Device device = DeviceSingleton.getInstance().getDeviceByRegId(registrationId);
				try {
					SubscriptionCommandExecutor sce = new SubscriptionCommandExecutor();
					sce.executeCommand(mContext, mAccessToken,device, registrationId, PublicDefineGlob.SET_MOTION_AREA_CMD +
							PublicDefineGlob.MOTION_ON_PARAM, null, new SubscriptionCommandExecutor.MVRListener() {
						@Override
						public void onMVRResponse(boolean success) {
							Log.d(TAG, "Motion area command executed " + success);
						}
					});
					sce.executeCommand(mContext, mAccessToken, device,registrationId, PublicDefineGlob.SET_RECORDING_PARAMETER_CMD,
							PublicDefineGlob.SET_RECORDING_PARAMETER_MVR_ON, new SubscriptionCommandExecutor.MVRListener() {
								@Override
								public void onMVRResponse(boolean success) {
									Log.d(TAG, "Motion Recording enable command executed " + success);
								}
							});
					if (DeviceSingleton.getInstance().getDeviceByRegId(registrationId).getProfile().doesSupportSDCardAccess()) {
						Api.getInstance().getService().setDeviceSettings(mAccessToken, registrationId, "storage_mode", "0", "device", "1");
					}
				} catch (Exception e) {
					Log.e(TAG, "Enabling Motion recording failed");
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void response) {

			}
		}.execute();

	}

	public void disableMotionVideoRecording(final String registrationID) {
		new AsyncTask<Void, Void, Void>() {
			Device device = DeviceSingleton.getInstance().getDeviceByRegId(registrationID);
			@Override
			protected Void doInBackground(Void... params) {
				try {
					SubscriptionCommandExecutor sce = new SubscriptionCommandExecutor();
					sce.executeCommand(mContext, mAccessToken, device, registrationID, PublicDefineGlob.SET_RECORDING_PARAMETER_CMD,
							PublicDefineGlob.SET_RECORDING_PARAMETER_MVR_OFF, new SubscriptionCommandExecutor.MVRListener() {
								@Override
								public void onMVRResponse(boolean success) {
									Log.d(TAG, "Motion Recording disable command executed " + success);
								}
							});
				} catch (Exception e) {
					Log.e(TAG, "Disabling Motion recording failed");
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void response) {

			}
		}.execute();
	}

}
