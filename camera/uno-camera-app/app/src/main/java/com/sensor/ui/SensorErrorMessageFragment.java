package com.sensor.ui;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hubbleconnected.camera.R;


public class SensorErrorMessageFragment extends Fragment {

  private Activity activity;
  private View view;
  private TextView errorMainTitleText, errorSubTitleText, errorSensorRetry;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_sensor_error_message, container, false);

    errorMainTitleText = (TextView) findViewById(R.id.error_sensor_main_title);
    errorSubTitleText = (TextView) findViewById(R.id.error_sensor_sub_title);
    errorSensorRetry = (TextView) findViewById(R.id.error_sensor_retry);

    if (getArguments() != null) {
      String mainTitle = getArguments().getString("error_main_title");
      String subTitle = getArguments().getString("error_sub_title");

      if (mainTitle != null)
        errorMainTitleText.setText(mainTitle);

      if (subTitle != null)
        errorSubTitleText.setText(subTitle);
    }
    errorSensorRetry.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        //activity.finish();
        if (activity != null) {
          try {
            ((AddSensorActivity)activity).switchToSetupTagSensorFragment();
          } catch (ClassCastException ex) {
            ((UpgradeSensorActivity)activity).switchToSetupTagSensorFragment();
          }
        }
      }
    });

    return view;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
  }

  private View findViewById(int id) {
    return view.findViewById(id);
  }
}
