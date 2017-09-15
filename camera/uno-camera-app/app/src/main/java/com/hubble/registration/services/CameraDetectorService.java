package com.hubble.registration.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import com.hubble.registration.tasks.CameraConnectivityDetector;

import java.io.IOException;
import java.net.InetAddress;

import base.hubble.PublicDefineGlob;
import com.hubble.registration.tasks.CameraConnectivityDetector;

public class CameraDetectorService extends Service {

  private CameraConnectivityDetector mCamDetector;
  private Thread mCamDetectorThread;

  public CameraDetectorService() {
    super();
  }

  public CameraDetectorService(String name) {
    super();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    String phonemodel = android.os.Build.MODEL;
    if (!phonemodel.equals(PublicDefineGlob.PHONE_MBP2k) && !phonemodel.equals(PublicDefineGlob.PHONE_MBP1k)) // if it's not iHome Phone
    {
      //IGNORE local vox
      // // Log.d("mbp", "NOT iHome - Dont start this service");
      return;
    }
    mCamDetector = new CameraConnectivityDetector(this);
    mCamDetectorThread = new Thread(mCamDetector);
    mCamDetectorThread.start();
  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    boolean retry = true;
    if (mCamDetectorThread != null) {
      mCamDetector.stop();
      mCamDetectorThread.interrupt();
      while (retry) {
        try {
          mCamDetectorThread.join(5000);
          retry = false;
        } catch (InterruptedException e) {
        }
      }
      mCamDetectorThread = null;
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    if (mCamDetectorThread != null && !mCamDetectorThread.isAlive()) {
      mCamDetectorThread.start();
    } else {
    }

    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }

  /* 20120118: copied from ScanForCamera.java */
  private InetAddress broadcast, gateWay;

  private void getAddresses() throws IOException {
    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    DhcpInfo dhcp = wifi.getDhcpInfo();
    // handle null somehow
    int broadcast_addr = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
    byte[] quads = new byte[4];
    for (int k = 0; k < 4; k++) {
      quads[k] = (byte) ((broadcast_addr >> k * 8) & 0xFF);
    }
    broadcast = InetAddress.getByAddress(quads);
    for (int k = 0; k < 4; k++) {
      quads[k] = (byte) ((dhcp.gateway >> k * 8) & 0xFF);
    }
    gateWay = InetAddress.getByAddress(quads);
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

}
