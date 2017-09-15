package com.sensor.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hubbleconnected.camera.R;


public class AfterFixingMotionTagFragment extends Fragment {

  Activity activity;
  View view;
  TextView continueAfterFix;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_after_fixing_motion_mototag, container, false);
    continueAfterFix = (TextView) findViewById(R.id.continue_after_fixing);

    continueAfterFix.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((AddSensorActivity) activity).switchToSettingTagAsMotionSensorFragment();
      }
    });
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    // This will redirect to sensor register page after 10 seconds
        /*new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                ((AddSensorActivity) activity).switchToSettingTagAsMotionSensorFragment();
            }
        }.start();*/
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
