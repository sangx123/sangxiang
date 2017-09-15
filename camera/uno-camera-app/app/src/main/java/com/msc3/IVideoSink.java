package com.msc3;

public interface IVideoSink {
  void onFrame(byte[] frame, byte[] pcm, int pcm_len);

  void onInitError(String errorMessage);

  void onVideoEnd();
}
