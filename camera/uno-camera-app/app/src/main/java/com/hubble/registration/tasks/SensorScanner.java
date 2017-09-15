package com.hubble.registration.tasks;

import android.content.Context;

import com.hubble.registration.tasks.CameraScanner;

/**
 * Created by CVision on 9/7/2015.
 */
public class SensorScanner extends CameraScanner {
  public SensorScanner(Context ctx, ICameraScanCompleted handler, String mode) {
    super(ctx, handler, mode, false);
  }
}
