/*
 * Copyright 2016 (c) Hubble Connected (HKT) Ltd. - All Rights Reserved
 *
 * Proprietary and confidential.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */


package com.nest.common;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginListener;
import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginManager;
import com.hubbleconnected.camera.R;
import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.SmokeCOAlarm;
import com.nxcomm.blinkhd.ui.MainActivity;

public class SmokeService extends Service {
	Handler mHandler;
	Runnable smokeRunnable = null;
	private static final String TAG = SmokeService.class.getSimpleName();

	@Override
	public void onCreate() {
		Log.i(TAG, "Service onCreate");
		mHandler = new Handler();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Service onStartCommand");
		smokeRunnable = new Runnable() {

			@Override
			public void run() {
				getSmokeState();
			}
		};
		mHandler.post(smokeRunnable);

		return Service.START_STICKY;
	}

	void getSmokeState() {
		Log.i(TAG, "getSmokeState");
		NestPluginManager.getInstance().addGlobalListener(new NestPluginListener.GlobalListener() {
			@Override
			public void onUpdate(@NonNull GlobalUpdate update) {
				for (SmokeCOAlarm smokeCOAlarm : update.getSmokeCOAlarms()) {
					Log.i(TAG, "Service onStartCommand   " + smokeCOAlarm.getSmokeAlarmState());
					if (smokeCOAlarm.getSmokeAlarmState().equals("emergency") || smokeCOAlarm.getSmokeAlarmState().equals("warning")) {
						smokeNotification(smokeCOAlarm.getName(), smokeCOAlarm.getSmokeAlarmState());
					}
				}
			}
		});

		mHandler.postDelayed(smokeRunnable, 3000);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "Service onBind");
		return null;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Service onDestroy");
		mHandler.removeCallbacks(smokeRunnable);
	}

	private void smokeNotification(String smokeName, String smokeStatus) {
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.home)
						.setContentTitle(getResources().getString(R.string.smoke_detected) + " " + smokeName + " - " + smokeStatus);

		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(contentIntent);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(0, builder.build());
	}
}
