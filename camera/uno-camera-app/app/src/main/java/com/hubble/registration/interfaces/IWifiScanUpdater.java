package com.hubble.registration.interfaces;

import android.net.wifi.ScanResult;

import java.util.List;

public interface IWifiScanUpdater {
  void updateWifiScanResult (List<ScanResult> result);

  void scanWasCanceled ();
}
