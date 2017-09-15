package com.hubble.motioncalibration;

/**
 * Created by Son Nguyen on 25/12/2015.
 */
public class UDPData {
  private static final String TAG = UDPData.class.getSimpleName();
  private byte[] bytes;
  private int threshold = 0;
  private boolean motionStart = false;
  private boolean motionStop = false;

  public UDPData(byte[] bytes) {
    this.bytes = bytes;
    if (bytes.length >= 2) {
      int base = ((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff);
      threshold = base >> 2;
      motionStop = (base & 3) == 1;
      motionStart = (base & 1) == 1;
      //Log.i(TAG, "Base value: " + base + " threshold: " + threshold + " motion start: " + motionStart + " motion stop: " + motionStop);
    }
  }

  public int getThreshold() {
    return threshold;
  }

  public boolean isMotionStart() {
    return motionStart;
  }

  public boolean isMotionStop() {
    return motionStop;
  }

}
