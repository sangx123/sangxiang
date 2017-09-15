package com.sensor.ui;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hubbleconnected.camera.R;
import com.sensor.constants.ProfileInfo;
import com.sensor.constants.SensorConstants;


public class InstructionStickToDoorFragment extends Fragment {

  Activity activity;
  View view;
  Button mContinueText;
  RelativeLayout mInstructionBg;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_insturction_stick_to_door, container, false);
    String sensorType =  ((AddSensorActivity) activity).getSensorType();
    mInstructionBg = (RelativeLayout) findViewById(R.id.instruction_bg);


      if(sensorType.equalsIgnoreCase(SensorConstants.PRESENCE_DETECTION))
        mInstructionBg.setBackgroundResource(R.drawable.presence_type_bg);
      else
        mInstructionBg.setBackgroundResource(R.drawable.coin_on_door_img);

    mContinueText = (Button) findViewById(R.id.continue_stick_to_door);

    mContinueText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if ( getFragmentManager().getBackStackEntryCount() > 0)
        {
          getFragmentManager().popBackStack();
          return;
        }

        //((AddSensorActivity) activity).switchToAfterFixingMotionTagFragment();
      }
    });
    return view;
  }

  private View findViewById(int id) {
    return view.findViewById(id);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
  }
}
