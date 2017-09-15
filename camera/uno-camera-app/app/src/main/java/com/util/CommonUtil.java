package com.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Pair;

import com.activeandroid.Model;
import com.google.gson.Gson;
import com.hubble.HubbleApplication;
import com.hubble.framework.service.cloudclient.device.pojo.response.EventResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;
/**
 * Created by connovatech on 10/5/2016.
 */
public class CommonUtil {

	private static final String TAG = CommonUtil.class.getSimpleName();
	private static final String EVENT_HISTORY_SP = "com.hubble.view_finder_evet";
	private static final String EVENT_READ_TIME = "view_finder_read_event_time";
	private static final String CAMERA_TYPE_SHARED_PREF = "camera_type_shared_pref";
	private static final String SETTINGS_PREFS_NAME = "settings_shared_pref";

	private static final String HINT_SCREEN_SHARED_PREF = "camera_hint_screen_shared_pref";
	private static final String USER_LOG_IN_FIRST_TIME = "user_login_first_time";
	private static final String CAMERA_ADD_FIRST_TIME = "camera_add_first_time";;

	private static final String APP_FORCE_UPGRADE_SP = "com.hubble.force.upgrade";
	public static final String APP_FORCE_UPGRADE_KEY = "com.hubble.force.upgrade.key";
	public static final String APP_FORCE_LOGOUT_KEY = "com.hubble.force.logout.key";
	public static final String APP_FORCE_UPGRADE_ONBOARDING_KEY = "com.hubble.force.upgrade.onboarding.key";
	private static final String DAILY_SUMMARY_OFFER_AVAILABLE = "daily_summary_offer_available";
	private static final String DAILY_SUMMARY_OPT_IN = "daily_summary_opt_in";
	private static final String FREE_TRIAL_DAYS= "free_trial_days";


	public static Pair<String, Object> parseResponseBody(String body) {
		if (body == "NA") {
			return new Pair("error", -1);
		} else if (body.contains("error_in_control_command")) {
			return new Pair("error", -1);
		} else if (body == "{\"value\": \"-6\"}") {
			return new Pair("error", -1);
		}
		String[] fields = body.split(": ");
		float intValue = -1;
		String strValue = null;
		try {
            if(body.contains("get_time_zone")){
                strValue =  fields[fields.length - 1];
            }else {
                intValue = Float.parseFloat(fields[fields.length - 1]);
                Log.d(TAG, "value after parse :- " + intValue);
            }
		} catch (NumberFormatException e) {
			strValue = fields[fields.length - 1];
		}
		if (intValue == -1) {
			return new Pair(fields[0], strValue);
		} else {
			return new Pair(fields[0], intValue);
		}
	}

	public static Pair<String,Object> parsePublishResponseBody(String body){
		if(body==null || !body.contains(":"))
			return new Pair("error",-1);
		String[] responseArr=body.split(":");
		if(responseArr.length==2){
			float floatValue=-1;
			String strValue="";
			try {
				floatValue = Float.parseFloat(responseArr[1]);
			}catch(NumberFormatException exception){
				strValue=responseArr[1];
			}
			if(floatValue==-1)
				return new Pair(responseArr[0], strValue);
			else
				return new Pair(responseArr[0],floatValue);
		}else
			return new Pair("error", -1);
	}


	public static int convertCtoF(float c) {
		return  Math.round(9f * c / 5f + 32);
	}

	public static int convertFtoC(int fahrenheit) {
		return Math.round((fahrenheit - 32) * 5f / 9f);
	}

	public enum EventType {
		MOTION,
		SOUND,
		TEMP_HIGH,
		TEMP_LOW,
		BATTERY,
		INVALID;

		//Add when app supports
		/*DOOR_MOTION,
		PRESENCE_ENTER,
		PRESENCE_LEFT,

		//for sensor
		CLOSE,
		OPEN,
		BATTERY,
		// for 877
		BABY_SMILE_DETECTION,
		BABY_SLEEPING_CARE;*/

		public static EventType fromAlertIntType(int type) {
			switch (type) {
				case 4:
					return MOTION;
				case 1:
					return SOUND;
				case 2:
					return TEMP_HIGH;
				case 3:
					return TEMP_LOW;
				case 16:
					return BATTERY;
				/*case 21:
					return DOOR_MOTION;
				case 22:
					return PRESENCE_ENTER;
				case 28:
					return PRESENCE_LEFT;
				//for sensor
				case 31:
					return BATTERY;
				case 29:
					return OPEN;
				case 30:
					return CLOSE;
				// for 877
				case 35:
					return BABY_SMILE_DETECTION;
				case 36:
					return BABY_SLEEPING_CARE;*/
				default:
					return INVALID;
			}


		}
	}

	public static String getEventString(EventResponse event, Context context) {
		EventType type = EventType.fromAlertIntType(event.getAlertType());
		String eventName = event.getAlertMessage();
		switch (type) {
			case MOTION:
				eventName = context.getString(R.string.motion_detected);
				break;
			case SOUND:
				eventName = context.getString(R.string.sound_detected);
				break;
			case TEMP_HIGH:
				eventName = context.getString(R.string.temperature_to_high);
				break;
			case TEMP_LOW:
				eventName = context.getString(R.string.temperature_to_low);
				break;
			case BATTERY:
				eventName = context.getResources().getString(R.string.battery);
				break;
			/*case DOOR_MOTION:
				eventName = context.getString(R.string.door_motion_detection));
				break;
			case PRESENCE_ENTER:
				eventName = context.getResources().getString(R.string.tag_is_back, event.getDevice().getName());
				break;
			case PRESENCE_LEFT:
				eventName = context.getResources().getString(R.string.tag_has_left, event.getDevice().getName());
				break;
			//for sensor
			case CLOSE:
				eventName = context.getResources().getString(R.string.close_door);
				break;
			case OPEN:
				eventName = context.getResources().getString(R.string.open_door);
				break;

			// for 877
			case BABY_SMILE_DETECTION:
				eventName = context.getResources().getString(R.string.baby_smile_detection);
				break;
			case BABY_SLEEPING_CARE:
				eventName = context.getResources().getString(R.string.baby_sleeping_care);
				break; */
			case INVALID:
				eventName = event.getAlertMessage();
				break;
		}
		return eventName;
	}

	public static void setEventReadTimeToSP(Context context, String registrationId,
	                                         String lastSeenTime) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				EVENT_HISTORY_SP, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(EVENT_READ_TIME + registrationId, lastSeenTime);
		editor.commit();
	}

	public static String getEventReadTimeFromSP(Context context, String registrationId) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				EVENT_HISTORY_SP, Context.MODE_PRIVATE);
		String readEventTime = sharedPref.getString(EVENT_READ_TIME + registrationId, "");
		return readEventTime;
	}

	public static void setHintScreenShownForUserToSP(Context context, String accessToken,
	                                        boolean isHintScreenShown) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				HINT_SCREEN_SHARED_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(USER_LOG_IN_FIRST_TIME + accessToken, isHintScreenShown);
		editor.commit();
	}

	public static boolean isHintScreenShownForUserFromSP(Context context, String accessToken) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				HINT_SCREEN_SHARED_PREF, Context.MODE_PRIVATE);
		boolean isHintScreenShown = sharedPref.getBoolean(USER_LOG_IN_FIRST_TIME + accessToken, false);
		return isHintScreenShown;
	}

	public static void setFirstCameraAddedToSP(Context context, String accessToken,
	                                                 boolean isFirstCameraAdded) {
        if(context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(
                    HINT_SCREEN_SHARED_PREF, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(CAMERA_ADD_FIRST_TIME + accessToken, isFirstCameraAdded);
            editor.commit();
        }
	}

	public static boolean getFirstCameraAddedFromSP(Context context, String accessToken) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				HINT_SCREEN_SHARED_PREF, Context.MODE_PRIVATE);
		boolean isFirstCameraAdded = sharedPref.getBoolean(CAMERA_ADD_FIRST_TIME + accessToken, false);
		return isFirstCameraAdded;
	}

	public static String getTimeStampFromTimeZone(Date date, double deviceTimezone, SimpleDateFormat df){

		String timezoneString=getCameraTimeZone(deviceTimezone);
		TimeZone timeZone=TimeZone.getTimeZone(timezoneString);
		df.setTimeZone(timeZone);
		df.getCalendar().setTime(date);
		if(timeZone.useDaylightTime()){
			int dstOffset=timeZone.getDSTSavings();
			df.getCalendar().add(Calendar.HOUR, dstOffset);
		}

		return df.format(df.getCalendar().getTime());
	}

	public static String getCameraTimeZone(double deviceTimezone){
		String timezone;
		if(deviceTimezone<0) {
			timezone="GMT-";
			deviceTimezone=-deviceTimezone;
		}else{
			timezone="GMT+";
		}
		String timeZoneStr=String.valueOf(deviceTimezone);
		if(timeZoneStr.contains(".")){

			String[] parts=timeZoneStr.split("\\.");
			timeZoneStr=parts[0];
			if(parts[1].length()==2){
				timeZoneStr+=":"+parts[1];
			}else{
				timeZoneStr+=":"+parts[1]+"0";
			}
		}

		timezone+=timeZoneStr;
		return timezone;
	}

	public static void setOrbit(Context context, boolean value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				CAMERA_TYPE_SHARED_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(PublicDefineGlob.PREFS_IS_ORBIT_SELECTED, value);
		editor.commit();
	}

	public static boolean isOrbit(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				CAMERA_TYPE_SHARED_PREF, Context.MODE_PRIVATE);
		return sharedPref.getBoolean(PublicDefineGlob.PREFS_IS_ORBIT_SELECTED, true);
	}

	public static boolean checkIsOrbitPresent(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				CAMERA_TYPE_SHARED_PREF, Context.MODE_PRIVATE);
		return sharedPref.contains(PublicDefineGlob.PREFS_IS_ORBIT_SELECTED);
	}
	public static void setNestConfig(Context context, boolean value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				CAMERA_TYPE_SHARED_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(PublicDefineGlob.PREFS_WORKS_WITH_NEST, value);
		editor.commit();
	}
	public static boolean getNestConfig(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				CAMERA_TYPE_SHARED_PREF, Context.MODE_PRIVATE);
		return(sharedPref.getBoolean(PublicDefineGlob.PREFS_WORKS_WITH_NEST,true));
		//return sharedPref.contains(PublicDefineGlob.PREFS_WORKS_WITH_NEST);
	}

	public static boolean isInternetAvailable(Context context){
		ConnectivityManager connectivityManager
				= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static String formatMacAddress(String macString, Context context) {
		Pattern regex = Pattern.compile("(..)(..)(..)(..)(..)(..)");
		final Matcher matcher = regex.matcher(macString);
		if (matcher.matches()) {
			return String.format("%s:%s:%s:%s:%s:%s",
					matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5), matcher.group(6));
		} else {
			// MAC not matches
			Log.d("mbp", "what returned: " + macString);
			return context.getString(R.string.failed_to_retrieve_camera_data);
		}
	}

    public static void removeSettingsInfo(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.apply();
    }

	public static void setSettingInfo(Context context, String key,  boolean value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	public static boolean getSettingInfo(Context context, String key) {
		if(context==null)
			return false;
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		return(sharedPref.getBoolean(key,false));
		//return sharedPref.contains(PublicDefineGlob.PREFS_WORKS_WITH_NEST);
	}

	public static boolean getSettingInfo(Context context, String key,boolean defaultValue) {
		if(context==null)
			return defaultValue;
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		return(sharedPref.getBoolean(key,defaultValue));
	}

	public static void setSettingValue(Context context, String key,  int value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static void setVideoRecording(Context context, String key,  int value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(key, value);
		editor.apply();
	}
	public static void setMCUVersion(Context context,String key,String value)
	{
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.apply();
	}
	public static int getVideoRecording(Context context, String key) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		return(sharedPref.getInt(key,-1));
	}

    public static void setSettingValue(Context context, String key,  String value) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

	public static int getSettingValue(Context context, String key) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		return(sharedPref.getInt(key,0));
		//return sharedPref.contains(PublicDefineGlob.PREFS_WORKS_WITH_NEST);
	}

    public static String getStringValue(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
        return(sharedPref.getString(key,null));
        //return sharedPref.contains(PublicDefineGlob.PREFS_WORKS_WITH_NEST);
    }

	public static int getSettingValue(Context context, String key, int defaultValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		return(sharedPref.getInt(key,defaultValue));
		//return sharedPref.contains(PublicDefineGlob.PREFS_WORKS_WITH_NEST);
	}

	public static void setLongValue(Context context, String key,  long value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	public static long getLongValue(Context context, String key,long defaultValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		return(sharedPref.getLong(key,defaultValue));

	}


	public static boolean checkSettings(Context context, String key) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);

		return sharedPref.contains(key);
	}

	public static void clearSettingSharedPref(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.clear();
		editor.commit();
	}

	public static void setSettingSchedule(Context context, String key, Models.DeviceSchedule schedule) {
		Gson gson = new Gson();
		String jsonSchedule = gson.toJson(schedule);
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key,jsonSchedule);
		editor.commit();
	}

	public static Models.DeviceSchedule getSettingSchedule(Context context, String key) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		Gson gson = new Gson();
		String jsonSchedule = sharedPref.getString(key,"");
		Models.DeviceSchedule schedule = gson.fromJson(jsonSchedule, Models.DeviceSchedule.class);
		if (schedule== null) {
			schedule = new Models.DeviceSchedule();
		}
		schedule.parse();
		return schedule;
	}


	public static void setForceUpgradeValueToSP(Context context, String key, boolean value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				APP_FORCE_UPGRADE_SP, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static boolean getForceUpgradeValueFromSP(Context context, String key, boolean defaultValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				APP_FORCE_UPGRADE_SP, Context.MODE_PRIVATE);
		return(sharedPref.getBoolean(key,defaultValue));
	}

	public static void setDailySummaryFeatureAvailable(Context context, boolean value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(DAILY_SUMMARY_OFFER_AVAILABLE, value);
		editor.commit();
	}

	public static void setDailySummaryFeatureOptedIn(Context context, boolean value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(DAILY_SUMMARY_OPT_IN, value);
		editor.commit();

	}


	public static boolean getDailySummaryFeatureAvailable(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		return sharedPref.getBoolean(DAILY_SUMMARY_OFFER_AVAILABLE, false);
	}


	public static boolean getDailySummaryFeatureOptedIn(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		return sharedPref.getBoolean(DAILY_SUMMARY_OPT_IN, false);
	}

	public static void setFreeTrialDays(Context context, int days) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		String userName = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, "");
		editor.putInt(FREE_TRIAL_DAYS+"-"+userName, days);
		editor.commit();
	}

	public static int getFreeTrialDays(Context context){
		SharedPreferences sharedPref = context.getSharedPreferences(
				SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
		String userName = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, "");
		return sharedPref.getInt(FREE_TRIAL_DAYS+"-"+userName, 30);
	}


}

