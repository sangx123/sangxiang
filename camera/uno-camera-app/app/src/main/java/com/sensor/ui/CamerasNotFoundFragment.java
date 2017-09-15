package com.sensor.ui;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hubbleconnected.camera.R;

import base.hubble.PublicDefineGlob;

public class CamerasNotFoundFragment extends Fragment {
  Activity activity;
  View view;
  private Button mBuyF86Camera;
  private Button mInstallF86Camera;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_no_supporting_cameras_found, container, false);
    mInstallF86Camera = (Button) findViewById(R.id.install_F86_camera);
    mBuyF86Camera = (Button) findViewById(R.id.buy_focus_86_camera);

    mInstallF86Camera.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        activity.setResult(PublicDefineGlob.CAMERA_NOT_FOUND_INSTALL_CAM_RESULT);
        activity.finish();
      }
    });

    mBuyF86Camera.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((AddSensorActivity) activity).buyCamera();
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
