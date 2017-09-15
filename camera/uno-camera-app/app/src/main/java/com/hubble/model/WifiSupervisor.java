package com.hubble.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

/**
 * Created by brennan on 15-04-15.
 */
public class WifiSupervisor {
  private static final String TAG = WifiSupervisor.class.getSimpleName();
  private static WifiSupervisor mInstance;

  private boolean mIsWifiDisconnected = false;
  private BroadcastReceiver mBroadcastReceiver;
  private WifiSupervisorInterface mListener;

  public static WifiSupervisor getInstance() {
    if (mInstance == null) {
      mInstance = new WifiSupervisor();
    }
    return mInstance;
  }

  private WifiSupervisor() {
    initializeBroadcastReceiver();
  }

  public void setListener(WifiSupervisorInterface listener) {
    mListener = listener;
    mIsWifiDisconnected = false;
  }

  private void initializeBroadcastReceiver() {
    mBroadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
          NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
          if (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
            if (networkInfo.isConnected()) {
              if (mListener != null && mIsWifiDisconnected) {
                mIsWifiDisconnected = false;
                mListener.onWifiConnected();
              }
            } else {
              mIsWifiDisconnected = true;
            }
          }
        }
      }

      @Override
      public IBinder peekService(Context myContext, Intent service) {
        return super.peekService(myContext, service);
      }
    };
  }

  public BroadcastReceiver getBroadcastReceiver() {
    if (mBroadcastReceiver == null) {
      initializeBroadcastReceiver();
    }
    return mBroadcastReceiver;
  }

  public interface WifiSupervisorInterface {
    void onWifiConnected();

    void onWifiDisconnected();
  }
}
