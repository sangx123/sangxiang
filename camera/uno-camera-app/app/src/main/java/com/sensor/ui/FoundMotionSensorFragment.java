package com.sensor.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.registration.Util;
import com.hubbleconnected.camera.R;
import com.sensor.constants.SensorConstants;


public class FoundMotionSensorFragment extends Fragment {

  private View view;
  private Activity activity;
  private TextView mProceedFoundSensor, mNameSensorExampleText;
  private EditText mNameSensorEditText;
  private ProgressBar mRegisterProgressBar;
  private LinearLayout mHowToAddTag;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_found_motion_sensor, container, false);

    mNameSensorEditText = (EditText) findViewById(R.id.name_sensor_edit_text);
    mProceedFoundSensor = (TextView) findViewById(R.id.proceed_found_sensor);
    mNameSensorExampleText = (TextView) findViewById(R.id.name_sensor_example_text);
    mRegisterProgressBar = (ProgressBar) findViewById(R.id.sensor_register_progressbar);
    mHowToAddTag = (LinearLayout) findViewById(R.id.how_to_add_tag);

    switch (((AddSensorActivity) activity).getSensorType()) {
      case SensorConstants.PRESENCE_DETECTION:
//        mNameSensorExampleText.setText(R.string.name_sensor_presence_example_text);
//        mNameSensorEditText.setHint(R.string.name_presence_sensor_hint);
        break;
      case SensorConstants.MOTION_DETECTION:
//        mNameSensorExampleText.setText(R.string.name_sensor_motion_example_text);
//        mNameSensorEditText.setHint(R.string.name_motion_sensor_hint);
        break;
    }

    mHowToAddTag.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((AddSensorActivity) activity).switchToInstructionStickToDoorFragment();
      }
    });
    mNameSensorEditText.requestFocus();
    mNameSensorEditText.setOnKeyListener(new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        // If the event is a key-down event on the "enter" button
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
            (keyCode == KeyEvent.KEYCODE_ENTER)) {
          validateAndContinue();
          return true;
        }
        return false;
      }
    });

    mProceedFoundSensor.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        validateAndContinue();
      }
    });

    return view;
  }

  private void validateAndContinue() {
    if (validate(mNameSensorEditText)) {
      String nameSensor = mNameSensorEditText.getText().toString();
      ((AddSensorActivity) activity).setmSensorName(nameSensor);
      mRegisterProgressBar.setVisibility(View.VISIBLE);
      mNameSensorEditText.setEnabled(false);
      mProceedFoundSensor.setEnabled(false);
      ((AddSensorActivity) activity).addSensorAsyncTask();
    }
  }

  private boolean validate(EditText editText) {
    String newDeviceName = editText.getText().toString().trim();

    if ((newDeviceName.length() < 5) || (newDeviceName.length() > 30)) {
      Toast.makeText(getActivity().getApplicationContext(), getString(R.string.device_name_length), Toast.LENGTH_LONG).show();
      editText.setText("");
      return false;
    } else if (!Util.validate(Util.CAMERA_NAME, newDeviceName)) {
      Toast.makeText(getActivity().getApplicationContext(), getString(R.string.invalid_device_name), Toast.LENGTH_LONG).show();
      editText.setText("");
      return false;
    }

    return true;
  }

  @Override
  public void onResume() {
    mNameSensorEditText.setText("");
    super.onResume();
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
