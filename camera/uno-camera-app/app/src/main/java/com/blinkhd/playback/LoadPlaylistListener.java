package com.blinkhd.playback;

import base.hubble.meapi.device.TimelineEvent;

public interface LoadPlaylistListener {
  void onRemoteCallSucceeded (TimelineEvent latest, TimelineEvent[] allEvents);

  void onRemoteCallFailed (int errorCode);
}
