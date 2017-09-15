package com.nxcomm.blinkhd.ui;

public interface LiveCameraActionButtonListener {
  void onPan (boolean isEnable);

  void onMic (boolean isEnable);

  void onRecord (boolean isEnable);

  void onStorage (boolean isEnable);

  void onMelody (boolean isEnable);

  void onTemperature (boolean isEnable);

  void onAudioEnable (boolean isEnable);

  void onSnap ();

  void onSettings (boolean enabled);

  void onZoom (boolean enabled);

  void onHD (boolean enabled);

  void onPreset (boolean enabled);

  void onMotionCalibration(boolean enabled);

  void onHumidity (boolean enabled);

  void onBTA(boolean enabled);
}
