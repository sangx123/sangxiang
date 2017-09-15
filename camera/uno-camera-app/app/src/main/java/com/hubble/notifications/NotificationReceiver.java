package com.hubble.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;
import com.blinkhd.StartActivity;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.bta.PushEvent;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceEventSummary;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceEventSummaryResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceSummaryDetail;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.framework.service.notification.FirebaseManager;
import com.hubble.framework.service.notification.GCMManager;
import com.hubble.framework.service.notification.HubbleNotification;
import com.hubble.framework.service.notification.NotificationConstant;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.subscription.OfferPopUpActivity;
import com.hubble.ui.eventsummary.EventSummaryConstant;
import com.hubble.util.CommonConstants;
import com.nxcomm.blinkhd.ui.CameraListFragment;
import com.nxcomm.blinkhd.ui.Global;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.sensor.constants.SensorConstants;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import base.hubble.Api;
import base.hubble.PublicDefineGlob;
import base.hubble.meapi.device.GetTimelineEventsResponse;
import base.hubble.subscriptions.ServerEvent;
import cz.havlena.ffmpeg.ui.FFMpegPlaybackActivity;
import de.greenrobot.event.EventBus;

public class NotificationReceiver extends BroadcastReceiver
{
	private static final String TAG = NotificationReceiver.class.getSimpleName();

	public static final String REFRESH_EVENTS_BROADCAST = "com.hubble.refreshEventList";
	public static final String IN_APP_NOTIFICATION = "in.app.motion.notification";


	private NotificationManager mNotificationManager;
	private String apiKey;

	private Context mContext;

	private List<String> eventMessages;

	SecureConfig settings;
	GetTimelineEventsResponse timelineResponse;
	private String bitmapURL;

	private static final int SIMPLE_NOTIFICATION = 2;
	private static final int SERVER_NOTIFICATION = SIMPLE_NOTIFICATION + 1;
	private static final int EVENT_NOTIFICATION = SIMPLE_NOTIFICATION + 2;
	private static final int IMAGE_NOTIFICATION = SIMPLE_NOTIFICATION + 3;
	private static final int OFFER_NOTIFICATION = SIMPLE_NOTIFICATION + 4;
	private static final int SUMMARY_NOTIFICATION = SIMPLE_NOTIFICATION + 5;
	public static final int DISMISS_NOTIFICATION = EVENT_NOTIFICATION + 10;

	private static final String SDCARD_EVENT_INSERTED = "200";
	private static final String SDCARD_EVENT_REMOVED = "201";
	private static final String SDCARD_EVENT_TEN_PERCENT_STORAGE_LEFT = "202";
	private static final String SDCARD_EVENT_STORAGE_FULL = "203";

	public static final String DISMISS_NOTIFICATION_CONSTANT = "com.hubble.onNotificationDismiss";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG,"notification received");
		mContext = context;
		settings = HubbleApplication.AppConfig;

		if(intent != null && intent.getAction() != null)
		{
			Log.d(TAG,"Action Received :- " + intent.getAction());

			if(intent.getAction().compareToIgnoreCase(NotificationConstant.NOTIFICATION_RECEIVED)==0)
			{
				mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

				apiKey = Global.getApiKey(context);

				if (apiKey != null)
				{
					DeviceSingleton.getInstance().init(apiKey, context);
				}
				HubbleNotification hubbleNotification = intent.getParcelableExtra(NotificationConstant.EXTRA_DATA_NOTIFICATION);

				if (hubbleNotification != null)
				{
					if (hubbleNotification.isSubscriptionNotification())
					{
						sendServerNotification(hubbleNotification.getBundle(), hubbleNotification);
					}
					else if (hubbleNotification.isLogoutNotification())
					{
						// unregister gcm
						GCMManager.getInstance(mContext).unregisterGCM(apiKey,settings.getInt(PublicDefineGlob.PREFS_PUSH_NOTIFICATION_APP_ID,-1));
						FirebaseManager.getInstance(mContext).stopNotificationService();
						// application id set as -1
						settings.putInt(PublicDefineGlob.PREFS_PUSH_NOTIFICATION_APP_ID,-1);

						// if user change password on this device, we do not need send UNAUTHORIZED_REQUEST
						if (settings.getBoolean(PublicDefine.IS_APP_CHANGE_PWD, false))
						{
							settings.putBoolean(PublicDefine.IS_APP_CHANGE_PWD, false);
						}
						else
						{
							EventBus.getDefault().postSticky(new ServerEvent(ServerEvent.UNAUTHORIZED_REQUEST));
						}
					}
					else if (hubbleNotification.isRemoveCameraNotification())
					{
						handleCameraRemovedMessage(hubbleNotification);
					}
					else if (hubbleNotification.isSDCardNotification())
					{
						handleSDCardEventPushNotification(hubbleNotification);
					}
					else if (hubbleNotification.isOfferNotification()) {
						sendOfferNotification(hubbleNotification);
					}
					else if(hubbleNotification.isSummaryNotification() && shouldShowMessage()){
						generateSummaryNotification(hubbleNotification);
					}
					else if (hubbleNotification.isValid())
					{
						if (isCameraAvailableInAccount(hubbleNotification.getDeviceRegistrationId(), hubbleNotification.getDeviceName())) {
							handleValidMessage(hubbleNotification.getBundle(), hubbleNotification);
						}
					}

					else
					{
						if ((hubbleNotification.getEventType() == HubbleNotification.ALERT_TYPE_INFO || hubbleNotification.getEventType() == HubbleNotification.ALERT_TYPE_SERVER_INFO) && shouldShowMessage()) {
							sendSimpleMessage(hubbleNotification);
						}
					}

				}
			}
			else if(intent.getAction().compareToIgnoreCase(NotificationConstant.REGISTRATION_FAILED)==0)
			{

			}
			else if(intent.getAction().compareToIgnoreCase(NotificationConstant.REGISTRATION_COMPLETE)==0)
			{
				int applicationID = intent.getIntExtra(GCMManager.APP_ID, -1);
				settings.putInt(PublicDefineGlob.PREFS_PUSH_NOTIFICATION_APP_ID,applicationID);
			}
		}
	}
	private void sendServerNotification(Bundle extras,HubbleNotification hubbleNotification)
	{
		int icon = R.drawable.ic_stat_notification;
		/*String myUrl = hubbleNotification.getMessage();

		if (myUrl == null)
		{
			myUrl = "http://hubblehome.com";
		}*/

		String message = SubscriptionNotificationActivity.translateNotification(mContext, hubbleNotification);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
				.setSmallIcon(icon)
				.setContentTitle(mContext.getString(R.string.app_brand_application_name))
				.setContentText(message);

        //As per new plan flow all notification should launch plan fragment.
		Intent notificationIntent = new Intent(mContext, MainActivity.class);
		notificationIntent.putExtra(MainActivity.EXTRA_DIRECTLY_TO_PLAN, true);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, HubbleNotification.ALERT_TYPE_INFO + 1000, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pendingIntent);
		mBuilder.setAutoCancel(true);
		mNotificationManager.notify(SERVER_NOTIFICATION, mBuilder.build());

		/*if (hubbleNotification.getSubscriptionName() != null)
		{
			startSubscriptionNotificationActivity(extras, mBuilder);
		}
		else
		{
			handleUrlNotification(hubbleNotification, myUrl, mBuilder);
		}*/
	}

	private void startSubscriptionNotificationActivity(Bundle extras, NotificationCompat.Builder mBuilder)
	{
		Intent notificationIntent = new Intent(mContext, SubscriptionNotificationActivity.class);
		notificationIntent.putExtras(extras);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, HubbleNotification.ALERT_TYPE_INFO + 1000, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pendingIntent);
		mBuilder.setAutoCancel(true);

		mNotificationManager.notify(SERVER_NOTIFICATION, mBuilder.build());
	}

	private void sendOfferNotification(HubbleNotification hubbleNotification) {
		if (hubbleNotification != null) {
			int icon = R.drawable.ic_stat_notification;
			String eventTitle = mContext.getString(R.string.app_brand_application_name);

			NotificationCompat.Builder mBuilder;
			mBuilder = new NotificationCompat.Builder(mContext);
			mBuilder.setAutoCancel(true)
					.setSmallIcon(icon)
					.setContentTitle(eventTitle)
					.setColor(mContext.getResources().getColor(R.color.app_theme_color))
					.setContentText(hubbleNotification.getMessage());

			Intent notificationIntent = new Intent(mContext, OfferPopUpActivity.class);
			//notificationIntent.putExtra(CommonConstants.OFFER_TYPE_FLAG, hubbleNotification.getOfferType());
			notificationIntent.putExtra(CommonConstants.OFFER_TYPE_FLAG, CommonConstants.OFFER_TYPE_VA);
			PendingIntent intent = PendingIntent.getActivity(mContext, OFFER_NOTIFICATION + 1000, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(intent);
			mNotificationManager.notify(OFFER_NOTIFICATION, mBuilder.build());

		}
	}

	private void sendDailySummaryNotification(HubbleNotification hubbleNotification){



	}


	private void handleSDCardEventPushNotification(HubbleNotification notification)
	{
		if (notification.getAlertVal().equalsIgnoreCase(SDCARD_EVENT_REMOVED))
		{
			notification.setMessage(String.format(mContext.getString(R.string.sdcard_removed_on_camera), notification.getDeviceName()));
			sendSimpleMessage(notification);
		}
		else if (notification.getAlertVal().equalsIgnoreCase((SDCARD_EVENT_INSERTED)))
		{
			notification.setMessage(String.format(mContext.getString(R.string.sdcard_inserted_on_camera), notification.getDeviceName()));
			sendSimpleMessage(notification);
		}
		else if (notification.getAlertVal().equalsIgnoreCase((SDCARD_EVENT_STORAGE_FULL)))
		{
			notification.setMessage(String.format(mContext.getString(R.string.sdcard_is_full_on_camera), notification.getDeviceName()));
			sendSimpleMessage(notification);
		}
		else if (notification.getAlertVal().equalsIgnoreCase((SDCARD_EVENT_TEN_PERCENT_STORAGE_LEFT)))
		{
			notification.setMessage(String.format(mContext.getString(R.string.sdcard_is_nearly_full_on_camera), notification.getDeviceName()));
			sendSimpleMessage(notification);
		}
	}

	private boolean shouldShowMessage() {
		long lastNotification = settings.getLong(PublicDefineGlob.PREFS_LAST_NOTIFICATION_TIME, (DateTime.now().getMillis() - 1000));
		int minutesBetweenNotifications = settings.getInt(PublicDefineGlob.PREFS_MINUTES_BETWEEN_NOTIFICATIONS,
				PublicDefineGlob.PREFS_DEFAULT_NOTIFICATIONS_FREQUENCY);
		boolean shouldShowNotifications = settings.getBoolean(PublicDefineGlob.PREFS_SHOULD_HAVE_NOTIFICATIONS, true);boolean isDoNotDisturb=CommonUtil.getSettingInfo(mContext,SettingsPrefUtils.PREFS_IS_DO_NOT_DISTURB_ENABLE,false);
		long doNotDisturbRemaining = DateTime.now().minusDays(1).getMillis();
		boolean isInfiniteDND=false;
		if (isDoNotDisturb) {
			try {
				doNotDisturbRemaining=CommonUtil.getLongValue(mContext, SettingsPrefUtils.PREFS_DO_NOT_DISTURB_REMAINING_TIME,DateTime.now().getMillis());
			} catch (Exception e) {
				doNotDisturbRemaining=DateTime.now().getMillis();
				CommonUtil.setLongValue(mContext, SettingsPrefUtils.PREFS_DO_NOT_DISTURB_REMAINING_TIME,doNotDisturbRemaining);
			}
			int dndDuration=CommonUtil.getSettingValue(mContext,SettingsPrefUtils.PREFS_DO_NOT_DISTURB_DURATION,30);
			if(dndDuration==Integer.MAX_VALUE)
				isInfiniteDND=true;
		}
		boolean isAfterRemainingTime=DateTime.now().isAfter(new DateTime(doNotDisturbRemaining));
		DateTime lastNotificationTime = new DateTime(lastNotification);
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSS");
		Log.d(TAG, ">>> last notification = " + dtf.print(lastNotificationTime) + " > now = " + dtf.print(DateTime.now()));
		/*return shouldShowNotifications &&
				DateTime.now().isAfter(lastNotificationTime.plusMinutes(minutesBetweenNotifications)) &&
				(!isDoNotDisturb ||isAfterRemainingTime);*/
		boolean showNotification=true;
		if(isDoNotDisturb && (!isAfterRemainingTime || isInfiniteDND))
			showNotification=false;
		return showNotification;
	}

	private void handleValidMessage(Bundle extras, HubbleNotification hubbleNotification)
	{
		Intent mIntent = new Intent(REFRESH_EVENTS_BROADCAST);
		mIntent.putExtra(REFRESH_EVENTS_BROADCAST, hubbleNotification.getDeviceRegistrationId());
		mContext.sendBroadcast(mIntent);
		Log.i(TAG, "Send broadcast REFRESH_EVENTS_BROADCAST");

		boolean showMessage = shouldShowMessage();
		if (showMessage)
		{
			int eventType = hubbleNotification.getEventType();
			Log.d(TAG, "send notification ... type: " + hubbleNotification.getEventType());

			Api.getInstance().saveNotification(hubbleNotification.getDeviceName(), getNotificationContentText(hubbleNotification));
			eventMessages = Api.getInstance().getAllNotifications();

			if(hubbleNotification.getEventType() == HubbleNotification.ALERT_TYPE_BABY_SLEEPING_CARE)
			{
				PushEvent pushEvent = new PushEvent(hubbleNotification.getDeviceRegistrationId(), hubbleNotification.getEventType());
				EventBus.getDefault().post(pushEvent);
			}


			if (eventType == HubbleNotification.ALERT_TYPE_INFO || eventType == HubbleNotification.ALERT_TYPE_SERVER_INFO)
			{
				sendServerNotification(extras, hubbleNotification);
			}
			else if (eventType == HubbleNotification.ALERT_TYPE_MOTION_ON)
			{
				/*sendMotionNotificationTask sendNotification = new sendMotionNotificationTask(hubbleNotification);
				sendNotification.execute();*/
				sendMotionNotification(hubbleNotification);
			}
			else if (eventType == HubbleNotification.ALERT_TYPE_SENSOR_PAIRED_EVENT)
			{
				Intent sensorPairedIntent = new Intent(SensorConstants.ACTION_SENSOR_PAIRED_EVENT);
				sensorPairedIntent.putExtra(SensorConstants.EXTRA_DEVICE_REGISTRATION_ID, hubbleNotification.getAlertVal());
				mContext.sendBroadcast(sensorPairedIntent);
			}
			else if (eventType == HubbleNotification.ALERT_TYPE_SENSOR_NOT_PAIRED_EVENT)
			{
				Intent sensorPairedIntent = new Intent(SensorConstants.ACTION_SENSOR_NOT_PAIRED_EVENT);
				sensorPairedIntent.putExtra(SensorConstants.EXTRA_DEVICE_REGISTRATION_ID, hubbleNotification.getAlertVal());
				mContext.sendBroadcast(sensorPairedIntent);
			} else {
				sendEventNotification(hubbleNotification);
			}
		} else {
			Log.d(TAG, "skip notification");
		}

		if (showMessage || !settings.check(PublicDefineGlob.PREFS_LAST_NOTIFICATION_TIME))
		{
			settings.putLong(PublicDefineGlob.PREFS_LAST_NOTIFICATION_TIME, DateTime.now().getMillis());
		}
	}

	private void handleCameraRemovedMessage(HubbleNotification hubbleNotification)
	{
		if (hubbleNotification != null)
		{
			Log.i(TAG, "Device " + hubbleNotification.getDeviceName() + " has been removed");


			Intent mIntent = new Intent(CameraListFragment.BROADCAST_REFRESH_CAMERA_LIST);
			if(mContext != null)
				mContext.sendBroadcast(mIntent);

			// call broadcast for camera removal
			mIntent = new Intent(PublicDefine.NOTIFY_NOTIFY_DEVICE_REMOVAL);
			mIntent.putExtra("regId", hubbleNotification.getDeviceRegistrationId());
			if(mContext != null)
				mContext.sendBroadcast(mIntent);

			// remove device event details once it is removed
			Util.removeDashBoardEventsFromSP(mContext,hubbleNotification.getDeviceRegistrationId());
			Util.deleteLatestPreview(hubbleNotification.getDeviceRegistrationId());
		}
	}

	private void sendSimpleMessage(HubbleNotification hubbleNotification)
	{
		if (hubbleNotification != null)
		{
			int icon = R.drawable.ic_stat_notification;
			String eventTitle = mContext.getString(R.string.app_brand_application_name);

			NotificationCompat.Builder mBuilder;
			mBuilder = new NotificationCompat.Builder(mContext);
			mBuilder.setAutoCancel(true)
					.setSmallIcon(icon)
					.setContentTitle(eventTitle)
					.setColor(mContext.getResources().getColor(R.color.app_theme_color))
					.setContentText(hubbleNotification.getMessage());

			String myUrl = hubbleNotification.getServerUrl();

			if (myUrl != null)
			{
				handleUrlNotification(hubbleNotification, myUrl, mBuilder);
			}
			else
			{
				Intent notificationIntent = new Intent(mContext, StartActivity.class);
				notificationIntent.putExtra(StartActivity.COME_FROM, StartActivity.COME_FROM_GCM);
				PendingIntent intent = PendingIntent.getActivity(mContext, SERVER_NOTIFICATION + 1000, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				mBuilder.setContentIntent(intent);
				mNotificationManager.notify(SERVER_NOTIFICATION, mBuilder.build());
			}
		}
	}

	private void handleUrlNotification(HubbleNotification hubbleNotification, String myUrl, NotificationCompat.Builder mBuilder)
	{
		Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(myUrl));
		notificationIntent.setFlags(Notification.FLAG_AUTO_CANCEL);
		notificationIntent.putExtra(MotionEventInteractionActivity.TRIGGER_DEVICE_ID, hubbleNotification.getDeviceRegistrationId());

		String select = mContext.getResources().getString(R.string.select);
		Intent chooser = Intent.createChooser(notificationIntent, select);
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1000, chooser, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(pendingIntent);
		mBuilder.setAutoCancel(true);
		mNotificationManager.notify(SERVER_NOTIFICATION, mBuilder.build());
	}


	private void sendMotionNotification(HubbleNotification hubbleNotification)
	{
		if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech"))
		{
			Log.d(TAG, "Send broadcast IN_APP_NOTIFICATION");
			Intent motionIntent = new Intent(IN_APP_NOTIFICATION);
			motionIntent.putExtra("message", R.string.v_has_motion_detected);
			mContext.sendBroadcast(motionIntent);
		}
		else
		{
			Intent notificationIntent = new Intent(mContext, MotionEventInteractionActivity.class);
			notificationIntent.putExtra(MotionEventInteractionActivity.TRIGGER_DEVICE_ID, hubbleNotification.getDeviceRegistrationId());
			notificationIntent.putExtra(MotionEventInteractionActivity.TRIGGER_EVENT_CODE, hubbleNotification.getEventCode());
			notificationIntent.putExtra(MotionEventInteractionActivity.TRIGGER_DEVICE_NAME, hubbleNotification.getDeviceName());
			notificationIntent.putExtra(FFMpegPlaybackActivity.COME_FROM, FFMpegPlaybackActivity.COME_FROM_GCM);

			/*try
			{
				timelineResponse = Device.getTimelineEvents(apiKey, hubbleNotification.getDeviceRegistrationId(), null, hubbleNotification.getEventCode(), HubbleNotification.ALERT_TYPE_MOTION_ON + "", 0, -1);

				if (timelineResponse != null && timelineResponse.getStatus() == 200)
				{
					if (timelineResponse.getEvents().length > 0 && timelineResponse.getEvents()[0].getData().length > 0)
					{
						bitmapURL = timelineResponse.getEvents()[0].getData()[0].getImage();
						notificationIntent.putExtra(MotionEventInteractionActivity.TRIGGER_IMAGE_URL, bitmapURL);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}*/

			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent intent = PendingIntent.getActivity(mContext, HubbleNotification.ALERT_TYPE_MOTION_ON + 1000, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			try
			{
				Notification notification = createMotionEventNotification(hubbleNotification, intent);

				if (CommonUtil.getSettingInfo(mContext, SettingsPrefUtils.PREFS_NOTIFY_BY_SOUND, true))
				{
					notification.defaults |= Notification.DEFAULT_SOUND;
				}

				if (CommonUtil.getSettingInfo(mContext, SettingsPrefUtils.PREFS_NOTIFY_BY_VIBRATE, false))
				{
					boolean personOnCall = checkIfPersonIsOnACall();

					if (personOnCall && checkPreference(PublicDefineGlob.PREFS_NOTIFY_ON_CALL) == true)
					{
						Vibrator v = (Vibrator) mContext.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
						v.vibrate(500);
						notification.defaults |= Notification.DEFAULT_VIBRATE;
					}
					else if (!personOnCall)
					{
						notification.defaults |= Notification.DEFAULT_VIBRATE;
					}
				}

				mNotificationManager.notify(IMAGE_NOTIFICATION, notification);
			}
			catch (Exception ignored)
			{
			}
		}
	}

	private boolean checkIfPersonIsOnACall()
	{
		AudioManager manager = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

		if (manager.getMode() == AudioManager.MODE_IN_CALL)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean checkPreference(final String sharedPreferenceName)
	{
		if (sharedPreferenceName == null || sharedPreferenceName.trim().isEmpty() || settings == null)
		{
			return false;
		}
		return settings.getBoolean(sharedPreferenceName, BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equals("hubblenew"));
	}

	private void sendEventNotification(HubbleNotification hubbleNotification)
	{
		int eventType = hubbleNotification.getEventType();

		if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech"))
		{
			Intent eventMotion = new Intent(IN_APP_NOTIFICATION);

			if (eventType == HubbleNotification.ALERT_TYPE_SOUND)
			{
				eventMotion.putExtra("message", R.string.v_has_sound_detected);
			}
			else if (eventType == HubbleNotification.ALERT_TYPE_TEMP_HI)
			{
				eventMotion.putExtra("message", R.string.v_has_temp_hi_detected);
			}
			else if (eventType == HubbleNotification.ALERT_TYPE_TEMP_LO)
			{
				eventMotion.putExtra("message", R.string.v_has_temp_lo_detected);
			}
			mContext.sendBroadcast(eventMotion);
		}
		else
		{
			String registrationId = hubbleNotification.getDeviceRegistrationId();

			Intent notificationIntent = new Intent(mContext, BlankNotificationActivity.class);
			notificationIntent.putExtra(MainActivity.EXTRA_DEVICE_REGISTRATION_ID, registrationId);
			notificationIntent.putExtra(BlankNotificationActivity.EVENT_TYPE, eventType);
			notificationIntent.putExtra(BlankNotificationActivity.EVENT_SIZE, eventMessages.size());

			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent intent = PendingIntent.getActivity(mContext, eventType + 1000, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
			builder.setSmallIcon(R.drawable.ic_stat_notification)
					.setAutoCancel(true)
					.setContentIntent(intent)
					.setColor(mContext.getResources().getColor(R.color.app_theme_color))
					.setDeleteIntent(createOnDismissedIntent(DISMISS_NOTIFICATION));

			if (eventMessages.size() == 1 || (eventMessages.size() + "").trim().isEmpty())
			{
				builder.setAutoCancel(true)
						.setContentTitle(hubbleNotification.getDeviceName())
						.setContentText(getNotificationContentText(hubbleNotification))
						.setGroupSummary(true);
			}
			else
			{
				String notificationTitle = String.format(mContext.getString(R.string.blank_new_events), eventMessages.size());

				NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
				inboxStyle.setBigContentTitle(notificationTitle);

				for (String messageString : eventMessages)
				{
					inboxStyle.addLine(Html.fromHtml(messageString));
				}

				builder.setContentTitle(String.format(notificationTitle, eventMessages.size()))
						.setContentText(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, mContext.getString(R.string.app_brand_application_name)))
						.setStyle(inboxStyle)
						.setGroupSummary(true);
			}

			builder.setGroup(mContext.getString(R.string.app_brand));

			try {
				Notification notification = builder.build();

				if (CommonUtil.getSettingInfo(mContext, SettingsPrefUtils.PREFS_NOTIFY_BY_SOUND, true))
				{
					boolean personOnCall = checkIfPersonIsOnACall();

					if (personOnCall && checkPreference(PublicDefineGlob.PREFS_NOTIFY_ON_CALL) == true)
					{
						notification.defaults |= Notification.DEFAULT_SOUND;
					}
					else if (!personOnCall)
					{
						notification.defaults |= Notification.DEFAULT_SOUND;
					}
				}

				if (CommonUtil.getSettingInfo(mContext, SettingsPrefUtils.PREFS_NOTIFY_BY_VIBRATE, false))
				{
					boolean personOnCall = checkIfPersonIsOnACall();
					if (personOnCall && checkPreference(PublicDefineGlob.PREFS_NOTIFY_ON_CALL) == true)
					{
						Vibrator v = (Vibrator) mContext.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
						v.vibrate(500);
						notification.defaults |= Notification.DEFAULT_VIBRATE;
					}
					else if (!personOnCall)
					{
						notification.defaults |= Notification.DEFAULT_VIBRATE;
					}
				}

				mNotificationManager.notify(EVENT_NOTIFICATION, notification);
			} catch (Exception ignored) {
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				sendWearableEventNotification(hubbleNotification, intent);
			}
		}
	}

	private void sendWearableEventNotification(HubbleNotification hubbleNotification, PendingIntent intent)
	{
		Notification wearableNotification = new NotificationCompat.Builder(mContext).setSmallIcon(R.drawable.ic_stat_notification)
				.setAutoCancel(true)
				.setContentTitle(hubbleNotification.getDeviceName())
				.setContentText(getNotificationContentText(hubbleNotification))
				.setContentIntent(intent)
				.setColor(mContext.getResources().getColor(R.color.app_theme_color))
				.setGroup(mContext.getString(R.string.app_brand))
				.setDeleteIntent(createOnDismissedIntent(DISMISS_NOTIFICATION)).build();
		String devRegistrationId = hubbleNotification.getDeviceRegistrationId();
		String notifId = "123456789";

		if (devRegistrationId != null)
		{
			notifId = hubbleNotification.getDeviceRegistrationId().replaceAll("\\D+", "");
		}

		notifId = notifId.substring(notifId.length() - 5);
		mNotificationManager.notify(Integer.valueOf(notifId), wearableNotification);
	}

	private PendingIntent createOnDismissedIntent(int notificationId)
	{
		Intent onNotificationDeleted = new Intent(mContext, NotificationDismissIntentService.class);
		onNotificationDeleted.putExtra(DISMISS_NOTIFICATION_CONSTANT, notificationId);
		return PendingIntent.getService(mContext, notificationId, onNotificationDeleted, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private Notification createMotionEventNotification(HubbleNotification hubbleNotification, PendingIntent intent)
	{
		String eventTitle = hubbleNotification.getDeviceName();
		Bitmap bitmap = null;
		String contentText = getNotificationContentText(hubbleNotification);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.ic_stat_notification);
		builder.setTicker(contentText + " on " + eventTitle);

		/*if (bitmapURL != null) {
			bitmap = getBitmapFromURL(bitmapURL);

			builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap)
					.setBigContentTitle(eventTitle)
					.setSummaryText(contentText))
					.extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true));
		}*/

		builder.setContentTitle(eventTitle);
		builder.setContentText(contentText);

		builder.setContentIntent(intent);

		return builder.build();
	}

	public Bitmap getBitmapFromURL(String strURL)
	{
		try
		{
			URL url = new URL(strURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			final BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(input, null, opts);
			opts.inSampleSize = calculateInSampleSize(opts, 200);
			connection.disconnect();

			// open second connection
			connection = (HttpURLConnection) url.openConnection();
			input = connection.getInputStream();
			opts.inJustDecodeBounds = false;
			Bitmap myBitmap = BitmapFactory.decodeStream(input, null, opts);
			return myBitmap;
		}
		catch (Exception e)
		{
			// // Log.e(TAG, Log.getStackTraceString(e));
			return null;
		}
	}

	private int calculateInSampleSize(BitmapFactory.Options options, int reqHeight)
	{
		// Raw height and width of image
		final int height = options.outHeight;
		// final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight) {
			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((height / inSampleSize) > reqHeight) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	private String getEventTypeStringFromCode(int eventTypeCode)
	{
		String eventString = mContext.getString(R.string.alert);

		if (eventTypeCode == HubbleNotification.ALERT_TYPE_SOUND)
		{
			eventString = mContext.getString(R.string.sound_detected);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_TEMP_HI)
		{
			eventString = mContext.getString(R.string.temperature_to_high);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_TEMP_LO)
		{
			eventString = mContext.getString(R.string.temperature_to_low);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_MOTION_ON)
		{
			eventString = mContext.getString(R.string.motion_detected);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_SENSOR_MOTION_DETECTION)
		{
			eventString = mContext.getString(R.string.sensor_motion_detected);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_SENSOR_PRESENCE_DETECTION)
		{
			eventString = mContext.getString(R.string.sensor_presence_detected);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_SENSOR_LEFT_DETECTION)
		{
			eventString = mContext.getString(R.string.sensor_left_detected);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_SENSOR_PAIRED_EVENT)
		{
			eventString = mContext.getString(R.string.sensor_paired_event);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_INFO)
		{
			eventString = mContext.getString(R.string.info);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_SERVER_INFO)
		{
			eventString = mContext.getString(R.string.info);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_SENSOR_BATTERY)
		{
			eventString = mContext.getString(R.string.battery);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_SENSOR_OPEN)
		{
			eventString = mContext.getString(R.string.open_door);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_SENSOR_CLOSE)
		{
			eventString = mContext.getString(R.string.close_door);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_MTAG_BATTERY)
		{
			eventString = mContext.getString(R.string.battery);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_CAMERA_LOW_BATTERY)
		{
			eventString = mContext.getString(R.string.battery);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_BABY_SMILE_DETECTION)
		{
			eventString = mContext.getString(R.string.baby_smile_detection);
		}
		else if (eventTypeCode == HubbleNotification.ALERT_TYPE_BABY_SLEEPING_CARE)
		{
			eventString = mContext.getString(R.string.baby_sleeping_care);
		}
		else if(eventTypeCode == HubbleNotification.ALERT_TYPE_PEOPLE_MOVEMENT_DETECTED)
		{
			eventString = mContext.getString(R.string.people_movement_detected);
		}
		else if(eventTypeCode == HubbleNotification.ALERT_TYPE_OBJECT_MOVEMENT_DETECTED)
		{
			eventString = mContext.getString(R.string.object_movement_detected);
		}
		else if(eventTypeCode == HubbleNotification.ALERT_TYPE_LOW_MEMORY_SPACE_DETECTED)
		{
			eventString = mContext.getString(R.string.low_memory_space_detected);
		}
		else if(eventTypeCode == HubbleNotification.ALERT_TYPE_OVER_HEATING_DETECTED)
		{
			eventString = mContext.getString(R.string.overheated_device);
		}
		else if(eventTypeCode == HubbleNotification.ALERT_TYPE_DEVICE_SHUTDOWN_EVENT)
		{
			eventString = mContext.getString(R.string.shutdown_device);
		}
		else if(eventTypeCode == HubbleNotification.ALERT_HIGH_HUMIDITY_EVENT)
		{
			eventString = mContext.getString(R.string.high_humidity_detected);
		}
		else if(eventTypeCode == HubbleNotification.ALERT_LOW_HUMIDITY_EVENT)
		{
			eventString = mContext.getString(R.string.low_humidity_detected);
		}

		return eventString;
	}

	private boolean isCameraAvailableInAccount(String regId, String name)
	{
		boolean isInAccount = false;
		try
		{
			boolean regIdFound = DeviceSingleton.getInstance().getDeviceByRegId(regId) != null;
			boolean deviceNameFound = DeviceSingleton.getInstance().getDeviceByName(name) != null;
			isInAccount = regIdFound || deviceNameFound;
		}
		catch (Exception e)
		{
			isInAccount = false;
		}
		Log.d(TAG, "Checking isCameraAvailableInAccount: regId " + regId + ", name " + name + ", isInAccount? " + isInAccount);
		return isInAccount;
	}

	private String getNotificationContentText(HubbleNotification hubbleNotification)
	{
		String contentText = getEventTypeStringFromCode(hubbleNotification.getEventType());
		if (HubbleNotification.isBleAlertEvent(hubbleNotification.getEventType()))
		{
			contentText = String.format(contentText, hubbleNotification.getTagName());
		}
		return contentText;
	}



	private class sendMotionNotificationTask extends AsyncTask<HubbleNotification, Void, Void> {
		HubbleNotification hubbleNotification;
		sendMotionNotificationTask(HubbleNotification hubbleNotification){
			this.hubbleNotification = hubbleNotification;
		}

		@Override
		protected Void doInBackground(HubbleNotification... params) {
			sendMotionNotification(hubbleNotification);
			return null;
		}
	}

	private void generateSummaryNotification(final HubbleNotification hubbleNotification) {
		//fetch clip and snap url
		if (hubbleNotification != null) {
			final DeviceEventSummary deviceEventSummary = new DeviceEventSummary(apiKey, hubbleNotification.getRegistrationId());
			deviceEventSummary.setWindow(EventSummaryConstant.WINDOW_DAILY);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			Date convertedDate = new Date();
			try {
				convertedDate = dateFormat.parse(hubbleNotification.getSummaryDay());
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				deviceEventSummary.setDay(sdf.format(convertedDate));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DeviceManagerService deviceManagerService = DeviceManagerService.getInstance(mContext);
			deviceManagerService.getDeviceEventSummary(deviceEventSummary, new Response.Listener<DeviceEventSummaryResponse>() {
				@Override
				public void onResponse(DeviceEventSummaryResponse response) {
					String snapUrl = null;
					String clipUrl = null;
					if (response != null && response.getStatus() == 200 && response.getMessage().equalsIgnoreCase("Success")) {
						DeviceEventSummaryResponse.DeviceSummaryDaily deviceSummaryDaily = response.getDeviceSummaryDaily();
						DeviceSummaryDetail[] deviceSummaryDetailArray = deviceSummaryDaily.getDeviceSummaryDailyDetail();
						DeviceSummaryDetail.DailySummaryDetail[] dailyDetailArray = deviceSummaryDetailArray[0].getDailySummaryDetail();
						for (DeviceSummaryDetail.DailySummaryDetail dailyDetail : dailyDetailArray) {
							CommonUtil.EventType type = CommonUtil.EventType.fromAlertIntType(dailyDetail.getAlertType());
							if (type == CommonUtil.EventType.MOTION && dailyDetail.getSummaryUrl() != null
									&& !dailyDetail.getSummaryUrl().equalsIgnoreCase(EventSummaryConstant.EVENT_SUMMARY_NOT_COMPUTED)) {
								snapUrl = dailyDetail.getSummarySnapUrl();
								clipUrl = dailyDetail.getSummaryUrl();
							}
						}

					}
					sendSummaryNotification(snapUrl, clipUrl, hubbleNotification);
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					sendSummaryNotification(null, null, hubbleNotification);
				}
			});
		}
	}

	private void sendSummaryNotification(final String snapUrl, String clipUrl, final HubbleNotification hubbleNotification) {
		if (clipUrl == null || clipUrl.isEmpty()) {//Without motion daily summary
			int icon = R.drawable.ic_stat_notification;
			String eventTitle = hubbleNotification.getDeviceName();

			NotificationCompat.Builder mBuilder;
			mBuilder = new NotificationCompat.Builder(mContext);
			mBuilder.setAutoCancel(true)
					.setSmallIcon(icon)
					.setContentTitle(eventTitle)
					.setTicker(hubbleNotification.getAlertType() + " on " + eventTitle)
					.setColor(mContext.getResources().getColor(R.color.app_theme_color))
					.setContentText(hubbleNotification.getAlertType());
			String regId = hubbleNotification.getRegistrationId();
			Intent notificationIntent = new Intent(mContext, MainActivity.class);
			if (regId != null && !regId.isEmpty()) {
				com.hubble.devcomm.Device summaryDevice = DeviceSingleton.getInstance().getDeviceByRegId(regId);
				notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				if (summaryDevice != null) {
					// only go to the device if we found it
					DeviceSingleton.getInstance().setSelectedDevice(summaryDevice);
					HubbleApplication.AppConfig.putBoolean(PublicDefine.PREFS_GO_DIRECTLY_TO_SUMMARY, true);
					HubbleApplication.AppConfig.putString(PublicDefine.PREFS_GO_DIRECTLY_TO_REGID, regId);
				}
			}
			int requestCode = SUMMARY_NOTIFICATION + Integer.parseInt(regId.substring(2, 6));
			PendingIntent intent = PendingIntent.getActivity(mContext, SUMMARY_NOTIFICATION + 1000, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(intent);
			mNotificationManager.notify(requestCode, mBuilder.build());
		} else { //Motion daily summary
			//Fetch bitmap from url
			new AsyncTask<Void, Void, Bitmap>() {
				@Override
				protected Bitmap doInBackground(Void... params) {
					Bitmap imageBitmap = null;
					if (snapUrl != null && !snapUrl.isEmpty()) {
						try {
							URL url = new URL(snapUrl);
							imageBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
						} catch (Exception e) {
							Log.d(TAG, "exception" + e);
						}
					}
					return imageBitmap;
				}

				@Override
				protected void onPostExecute(Bitmap bitmap) {
					Intent notificationIntent = new Intent(mContext, SummaryNotificationActivity.class);
					notificationIntent.putExtra(SummaryNotificationActivity.SUMMARY_DEVICE_ID, hubbleNotification.getRegistrationId());
					notificationIntent.putExtra(SummaryNotificationActivity.SUMMARY_DAY, hubbleNotification.getSummaryDay());
					notificationIntent.putExtra(SummaryNotificationActivity.SUMMARY_DEVICE_NAME, hubbleNotification.getDeviceName());
					notificationIntent.putExtra(SummaryNotificationActivity.SUMMARY_SNAP_URL, snapUrl);
					notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					PendingIntent intent = PendingIntent.getActivity(mContext, SUMMARY_NOTIFICATION + 1000, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					String eventTitle = hubbleNotification.getDeviceName();
					NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
					builder.setAutoCancel(true);
					builder.setSmallIcon(R.drawable.ic_stat_notification);
					builder.setTicker(hubbleNotification.getAlertType() + " on " + eventTitle);
					builder.setColor(mContext.getResources().getColor(R.color.app_theme_color));
					if (bitmap != null) {
						builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap)
								.setBigContentTitle(eventTitle)
								.setSummaryText(hubbleNotification.getAlertType()))
								.extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true));
					}
					builder.setContentTitle(eventTitle);
					builder.setContentText(hubbleNotification.getAlertType());
					builder.setContentIntent(intent);
					Notification notification = builder.build();

					if (CommonUtil.getSettingInfo(mContext, SettingsPrefUtils.PREFS_NOTIFY_BY_SOUND, true)) {
						notification.defaults |= Notification.DEFAULT_SOUND;
					}

					if (CommonUtil.getSettingInfo(mContext, SettingsPrefUtils.PREFS_NOTIFY_BY_VIBRATE, false)) {
						boolean personOnCall = checkIfPersonIsOnACall();

						if (personOnCall && checkPreference(PublicDefineGlob.PREFS_NOTIFY_ON_CALL) == true) {
							Vibrator v = (Vibrator) mContext.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
							v.vibrate(500);
							notification.defaults |= Notification.DEFAULT_VIBRATE;
						} else if (!personOnCall) {
							notification.defaults |= Notification.DEFAULT_VIBRATE;
						}
					}
					int requestCode = SUMMARY_NOTIFICATION + Integer.parseInt(hubbleNotification.getRegistrationId().substring(2, 6));
					mNotificationManager.notify(requestCode, notification);
				}

			}.execute();

		}
	}
}
