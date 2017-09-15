package com.hubble.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 18-01-2017.
 */
public class SubscriptionUtil {

	private static final String SUBSCRIPTION_SHARED_PREF = "com.hubbleconnected.camera.subscription";
	private static final String SUBSCRIPTION_SHOW_FREE_TRIAL_KEY = "com.hubbleconnected.camera.subscription.show.free.trial";
	private static final String SUBSCRIPTION_PLAN = "com.hubbleconnected.camera.subscription.plan";

	public static final int PLAN_FREE_TRIAL_AVAILABLE = 0;
	public static final int PLAN_FREE_TRIAL_APPLIED = 1;
	public static final int PLAN_FREE_TRIAL_EXPIRED = 2;
	public static final int PLAN_ON_SOME_PLAN = 3;
	public static final int PLAN_AVAILABLE = 4;
	public static final int PLAN_APPLIED = 5;
	public static final int PLAN_MAX_QUOTA_REACHED = 6;

	public static final int NO_PLAN = 0;
	public static final int PLAN_ENABLED = 1;

	public static final String HUBBLE_TIER1 = "hubble-tier1";
	public static final String HUBBLE_TIER2 = "hubble-tier2";
	public static final String HUBBLE_TIER3 = "hubble-tier3";

	public static Map<String, String> planTypeDetailMap =  ImmutableMap.<String, String>builder()
			.put(HUBBLE_TIER1,"last 24 hours of motion-triggered video storage")
			.put(HUBBLE_TIER2,"07 days of motion-triggered video storage")
			.put(HUBBLE_TIER3,"30 days of motion-triggered video storage")
			.build();
	public static Map<String, String> planNames =  ImmutableMap.<String, String>builder()
			.put(HUBBLE_TIER1,"Bronze - 1 day ")
			.put(HUBBLE_TIER2,"Silver - 7 days")
			.put(HUBBLE_TIER3,"Gold - 30 days")
			.build();

	/**
	 * Sets the boolean whether free trial popup should be shown
	 * @param context
	 * @param registartionId
	 * @param value
	 */
	public static void setShowFreeTrial(Context context, String registartionId, boolean value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SUBSCRIPTION_SHARED_PREF+registartionId, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(SUBSCRIPTION_SHOW_FREE_TRIAL_KEY, value);
		editor.commit();
	}

	/**
	 * Returns the boolean whether free trial popup should be shown
	 * @param context
	 * @param registartionId
	 * @return
	 */
	public static boolean getShowFreeTrial(Context context, String registartionId) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SUBSCRIPTION_SHARED_PREF+registartionId, Context.MODE_PRIVATE);
		return sharedPref.getBoolean(SUBSCRIPTION_SHOW_FREE_TRIAL_KEY, true);
	}

	/**
	 * Sets the boolean whether camera is added and subscription should be applied.
	 * @param context
	 * @param registartionId
	 * @param value
	 */
	public static void setShouldCheckSubscriptionPlan(Context context, String registartionId, boolean value) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SUBSCRIPTION_SHARED_PREF+registartionId, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean(SUBSCRIPTION_PLAN, value);
		editor.commit();
	}

	/**
	 * Retunrs the boolean whether camera is added and subscription should be applied.
	 * @param context
	 * @param registartionId
	 * @return
	 */
	public static boolean getShouldCheckSubscriptionPlan(Context context, String registartionId) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				SUBSCRIPTION_SHARED_PREF+registartionId, Context.MODE_PRIVATE);
		return sharedPref.getBoolean(SUBSCRIPTION_PLAN, false);
	}
}
