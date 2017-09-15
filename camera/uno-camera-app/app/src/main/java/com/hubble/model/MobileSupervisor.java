package com.hubble.model;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Created by brennan on 2015-07-06.
 */
public class MobileSupervisor {
  public static final String TAG = MobileSupervisor.class.getSimpleName();
  public static MobileSupervisor mInstance;
  public static boolean mIsMobileDataConnected;
  private Interface mDelegate;
  private PhoneStateListener mListener;

  public static MobileSupervisor getInstance() {
    if (mInstance == null) {
      mInstance = new MobileSupervisor();
    }
    return mInstance;
  }

  private MobileSupervisor() {
    mIsMobileDataConnected = false;
  }

  public MobileSupervisor setDelegate(Interface delegate) {
    mDelegate = delegate;
    return mInstance;
  }

  public static void resetIsMobileConnected() {
    mIsMobileDataConnected = false;
  }

  public void removeDelegate() {
    mDelegate = null;
  }

  public boolean getIsMobileDataConnected() {
    return mIsMobileDataConnected;
  }

  public void registerListener(Context context) {
    mListener = new PhoneStateListener() {
      @Override
      public void onDataConnectionStateChanged(int state) {
        switch (state) {
          case TelephonyManager.DATA_DISCONNECTED:
            mIsMobileDataConnected = false;
            break;
          case TelephonyManager.DATA_CONNECTED:
            if (mDelegate != null && !mIsMobileDataConnected) {
              mIsMobileDataConnected = true;
              mDelegate.onMobileDataConnected();
            }
            break;
          case TelephonyManager.DATA_CONNECTING:
            break;
          case TelephonyManager.DATA_SUSPENDED:
            break;
        }
      }
    };

    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    telephonyManager.listen(mListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
  }

  public interface Interface {
    void onMobileDataConnected();
  }
}
