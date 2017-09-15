package com.blinkhd;

public class CameraModel {
  public static final int MODEL_UNKNOWN = -1;
  public static final int MODEL_66 = 1;
  public static final int MODEL_83 = 2;

  private static final String MODEL_66_STR = "010066";
  private static final String MODEL_83_STR = "010083";

  public static int getCameraModelFromUDID(String udid) {
    int model = MODEL_UNKNOWN;

    if (udid.startsWith(MODEL_66_STR)) {
      model = MODEL_66;
    } else if (udid.startsWith(MODEL_83_STR)) {
      model = MODEL_83;
    }
    return model;
  }
}
