package com.hubble.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import base.hubble.Api;

/**
 * Created by Pragnya on 24-05-2017.
 */
public class NotificationDismissIntentService extends IntentService {

	private static final String TAG = NotificationDismissIntentService.class.getSimpleName();

	public NotificationDismissIntentService() {
		super("NotificationDismissIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null) {
			return;
		}
		if(intent.getIntExtra(NotificationReceiver.DISMISS_NOTIFICATION_CONSTANT,0) == NotificationReceiver.DISMISS_NOTIFICATION){
			Log.d(TAG, "Notification Dismissed");
			Api.getInstance().deleteAllNotifications();
		}
	}
}
