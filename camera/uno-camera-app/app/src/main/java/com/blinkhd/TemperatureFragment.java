package com.blinkhd;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.helpers.AsyncPackage;

import com.nxcomm.blinkhd.ui.customview.TemperatureView;

import java.util.Timer;
import java.util.TimerTask;

import base.hubble.PublicDefineGlob;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;


public class TemperatureFragment extends Fragment {
  private static final String TAG = "TemperatureFragment";
  private Device selectedDevice = null;

  private float currentTemperatureInC = 25f;
  private Activity activity = null;
  private Timer queryTempTimer = null;

  private int currentTemperatureUnit;
  private TemperatureView temperatureView;
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    activity = null;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.temperature_fragment, container, false);

    temperatureView = (TemperatureView) view.findViewById(R.id.temperatureView1);
    if ((getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE) {
      temperatureView.setMainTextSize(50);
    } else {
      temperatureView.setMainTextSize(133);
    }
    temperatureView.setTextColor(getResources().getColor(R.color.main_blue));
    temperatureView.setVisibility(View.INVISIBLE);

    temperatureView.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (activity != null) {
          if (temperatureView != null) {
            temperatureView.setTemperature(currentTemperatureInC);
            temperatureView.setVisibility(View.VISIBLE);
            currentTemperatureUnit = currentTemperatureUnit == TemperatureView.F ? TemperatureView.C : TemperatureView.F;
            temperatureView.setMode(currentTemperatureUnit);
          }
        }
      }
    });

    return view;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
    if (savedTempUnit == PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
      currentTemperatureUnit = TemperatureView.C;
      temperatureView.setMode(TemperatureView.C);
    } else {
      currentTemperatureUnit = TemperatureView.F;
      temperatureView.setMode(TemperatureView.F);
    }
    queryTempTimer = new Timer();
    queryTempTimer.scheduleAtFixedRate(new QueryTemperatureTask(), 0, 10000);
  }

  public void setDevice(Device device) {
    this.selectedDevice = device;
  }

  /**
   * @return current temperature in C
   */
  public float getCurrentTemperature() {
    return currentTemperatureInC;
  }

  public void setCurrentTemperature(float current_temp_c) {
    currentTemperatureInC = current_temp_c;
  }

  private void queryTemperature() {
    AsyncPackage.doInBackground(new Runnable() {
      @Override
      public void run() {
        if (selectedDevice != null) {
          final Pair<String, Object> response = selectedDevice.sendCommandGetValue("value_temperature", null, null);
          if (response != null && response.second instanceof Float) {
            currentTemperatureInC = (Float) response.second;
          } else if (response != null && response.second instanceof String) {
            try {
              currentTemperatureInC = Float.valueOf((String) response.second);
            } catch (NumberFormatException e) {
              // Camera sometimes returns non-ASCII characters
            }
          }
          if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                updateTemperature();
              }
            });
          }
        }
      }
    });
  }

  private void updateTemperature() {
    if (activity != null) {
      if (temperatureView != null) {
        temperatureView.setTemperature(currentTemperatureInC);
        temperatureView.setVisibility(View.VISIBLE);
      }
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if ((getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE) {
      temperatureView.setMainTextSize(50);
    } else {
      temperatureView.setMainTextSize(133);
    }
    temperatureView.setTextColor(getResources().getColor(R.color.main_blue));
  }

  @Override
  public void onStop() {
    super.onStop();
    if (queryTempTimer != null) {
      queryTempTimer.cancel();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  private class QueryTemperatureTask extends TimerTask {
    @Override
    public void run() {
      queryTemperature();
    }
  }
}