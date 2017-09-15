package com.hubble.controllers;

import com.hubble.devcomm.Device;
//import com.hubble.ui.patrol.PatrolVideoFragment;
import com.nxcomm.blinkhd.ui.MainActivity;

import java.util.Comparator;
import java.util.List;

/**
 * Created by dan on 2014-07-10.
 */
public class CameraController {

  private static final String TAG = "CameraController";

  /**
   * Will trigger a fragment transition
   *
   * @param view
   * @param camProfile
   */
  static public void switchToCameraFragment(MainActivity view, Device camProfile) {
    //setCameraToUseWithSharedPreferences(view, camProfile);
    view.switchToCameraFragment(camProfile);
  }

  public static Comparator<Device> buildCamOrderComparator() {
    return new Comparator<Device>() {
      @Override
      public int compare(Device camProfile, Device camProfile2) {
        return camProfile.getProfile().getName().compareTo(camProfile2.getProfile().getName());
      }
    };
  }

  /*static public void switchToPatrollingMode(final MainActivity a, final List<Device> patrolCameras) {
    if (patrolCameras.size() > 0) {
      a.switchToCameraFragmentForPatrol(new Runnable() {
        @Override
        public void run() {
          PatrolVideoFragment videoView = a.getPatrolVideoFragment();
          videoView.startPatrolling(patrolCameras);
        }
      });
    }
  }*/
}

