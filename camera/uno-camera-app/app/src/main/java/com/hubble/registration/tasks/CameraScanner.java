package com.hubble.registration.tasks;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.util.Log;

import com.hubble.registration.interfaces.IWifiScanUpdater;
import com.hubble.registration.models.CameraBonjourInfo;
import com.hubble.registration.tasks.BonjourScan.IBonjourScanCompleted;

import java.util.ArrayList;
import java.util.List;

import base.hubble.PublicDefineGlob;

public class CameraScanner implements IWifiScanUpdater, IBonjourScanCompleted, Runnable {
  private static final String TAG = "CameraScanner";
  private final String mode;
  private WifiScan ws;
  private BonjourScan bs;
  private Context ctx;
  private volatile boolean wsScanCompleted, bsScanCompleted, isCamera;

  private ICameraScanCompleted onCameraScanCompletedHandler = null;

  private List<ScanResult> wsResult;
  private List<CameraBonjourInfo> bsResult;
  private AsyncTask<Void, Void, Void> bsTask;

  public interface ICameraScanCompleted {
    void onWifiCameraScanCompleted (List<ScanResult> results);

    void onMdnsCameraScanCompleted (List<CameraBonjourInfo> results);
  }

  public CameraScanner(Context ctx, ICameraScanCompleted handler, String mode) {
    bs = null;
    ws = null;
    this.ctx = ctx;
    wsScanCompleted = false;
    bsScanCompleted = false;
    this.mode = mode;
    this.onCameraScanCompletedHandler = handler;
    this.isCamera = true;
  }

  public CameraScanner(Context ctx, ICameraScanCompleted handler, String mode, boolean isCamera) {
    bs = null;
    ws = null;
    this.ctx = ctx;
    wsScanCompleted = false;
    bsScanCompleted = false;
    this.mode = mode;
    this.onCameraScanCompletedHandler = handler;
    this.isCamera = isCamera;
  }

  @Override
  public void run() {
    this.stop();
    Log.i(TAG, "CameraScanner BTATask start...");
    if (mode.equals("mdns")) {
      bsScanCompleted = false;
      bs = new BonjourScan(this, ctx);
      final Runnable bsRunnable = bs;
      bsTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          bsRunnable.run();
          return null;
        }
      };
      bsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else if (mode.equals("wifi")) {
      wsScanCompleted = false;
      ws = new WifiScan(ctx, this);
      ws.setSilence(true);
      ws.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "wifiScan");
    }
  }

  public void stop() {
    if (ws != null) {
      ws.cancel(true);
    }
    if (bs != null) {
      bs.cancel();
    }
    if (bsTask != null) {
      bsTask.cancel(true);
    }
  }

  @Override
  public void updateWifiScanResult(List<ScanResult> result) {
    wsScanCompleted = true;
    wsResult = new ArrayList<ScanResult>();
    for (ScanResult sr : result) {
      Log.d(TAG, "Wifi scan result: " + sr.SSID);
      if (isCamera) {
        for (String cameraSSID : PublicDefineGlob.CAMERA_SSID_LIST) {
          if (sr.SSID.startsWith(cameraSSID)) {
            wsResult.add(sr);
            break;
          }
        }
      } else {
        if (sr.SSID.startsWith(PublicDefineGlob.DEFAULT_SSID_SENSOR)) {
          wsResult.add(sr);
        }
      }
    }
    Log.i(TAG, "Wi-Fi camera scanner task completed.");
    if (onCameraScanCompletedHandler != null) {
      onCameraScanCompletedHandler.onWifiCameraScanCompleted(wsResult);
    }
    Log.i(TAG, "CameraScanner WIFI BTATask completed.");
  }

  @Override
  public void scanWasCanceled() {

    Log.d(TAG, "WiFi scan is cancelled");
  }

  public boolean isCompleted() {
    return (mode.equals("wifi") && wsScanCompleted) ||
        (mode.equals("mdns") && bsScanCompleted);
  }

  @Override
  public void onBonjourScanCompleted(List<CameraBonjourInfo> cameraBonjourInfo) {
    bsScanCompleted = true;

    bsResult = new ArrayList<CameraBonjourInfo>();
    for (CameraBonjourInfo info : cameraBonjourInfo) {

      Log.d(TAG, "Bonjour scan result: " + info.getCameraName());
      for (String cameraSSID : PublicDefineGlob.CAMERA_SSID_LIST) {
        if (info.getCameraName().startsWith(cameraSSID)) {
          bsResult.add(info);
          break;
        }
      }
    }

    Log.i(TAG, "Bonjour camera scanner task completed.");
    if (onCameraScanCompletedHandler != null) {
      onCameraScanCompletedHandler.onMdnsCameraScanCompleted(bsResult);
    }
    Log.i(TAG, "CameraScanner MDNS BTATask completed.");
  }

  @Override
  public void onBonjourScanCancelled() {
    Log.i(TAG, "Bonjour camera scanner task cancelled.");
  }
}
