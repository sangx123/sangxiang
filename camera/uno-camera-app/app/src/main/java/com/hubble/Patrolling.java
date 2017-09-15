package com.hubble;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hubble.devcomm.Device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dan on 31/07/14.
 */
public class Patrolling {

  // Get the camera order from prefs
  public static String[] getCameraOrder(Context a) {
    return getCameraIdsFromNamedPref(a, "patrolling_camera_order");
  }

  public static String[] getCameraIdsFromNamedPref(Context c, String prefName) {
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(c);
    String orders = prefs.getString(prefName, "");
    // // Log.d("PREFS", "got " + prefName + " :" + orders);
    return orders.split(",");
  }

  public static int getPatrollingDelay(Context c) {
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(c);
    return prefs.getInt("patrol_delay_seconds", 15);
  }

  public static void savePatrollingDelayPref(Context a, int seconds) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(a);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt("patrol_delay_seconds", seconds);
    editor.commit();
  }

  public static void saveCameraOrder(Context a, List<Device> cameras) {
    saveCameraIdsAsNamedPref(a, "patrolling_camera_order", cameras);
  }

  // Ugly, serialize the camera order as a string and save it to Prefs
  public static void saveCameraIdsAsNamedPref(Context a, String name, List<Device> cameras) {
    String order = "";
    List<String> orders = new ArrayList<String>();
    Iterator<Device> it = cameras.iterator();
    if (it.hasNext()) {
      order += it.next().getProfile().getRegistrationId();
    }
    while (it.hasNext()) {
      order += "," + it.next().getProfile().getRegistrationId();
    }
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(a);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(name, order);
    editor.commit();
    // // Log.d("PREFS", "got " + name + " :" + order);
  }


}
