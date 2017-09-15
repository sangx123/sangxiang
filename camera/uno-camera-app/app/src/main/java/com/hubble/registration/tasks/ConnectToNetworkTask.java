package com.hubble.registration.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.hubble.registration.PublicDefine;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import base.hubble.PublicDefineGlob;

/*
 * Connect to a network given a WifiConfiguration
 */

public class ConnectToNetworkTask extends AsyncTask<WifiConfiguration, String, Boolean> {

  public static final int MSG_CONNECT_TO_NW_DONE = 0x100;
  public static final int MSG_CONNECT_TO_NW_FAILED = 0x101;
  public static final int MSG_CONNECT_TO_NW_CANCELLED = 0x102;
  private static final String TAG = "ConnectToNetworkTask";

  private Context mContext;
  private Object wifiLockObject;
  private Handler mHandler;
  private String bssid = null;
  private String ssid = null;

  NetworkInfo mWifiNetworkInfo;
  private WifiBr br;
  private boolean ignoreBSSID;
  private boolean dontRemoveConfigurationIfConnectFailed;

  public ConnectToNetworkTask(Context c, Handler h) {
    mContext = c;
    wifiLockObject = new Object();

    mHandler = h;
    ignoreBSSID = false;
    dontRemoveConfigurationIfConnectFailed = false;
  }

  public void setIgnoreBSSID(boolean ignore) {
    ignoreBSSID = ignore;
  }

  public void dontRemoveFailedConnection(boolean r) {
    dontRemoveConfigurationIfConnectFailed = r;
  }

  protected void onPostExecute(Boolean result) {
    if (mHandler != null) {
      if (result) {
        Message m = Message.obtain(mHandler, MSG_CONNECT_TO_NW_DONE);
        mHandler.dispatchMessage(m);
      } else {
        mHandler.dispatchMessage(Message.obtain(mHandler, MSG_CONNECT_TO_NW_FAILED, ssid));
      }
    }
  }

  protected void onCancelled() {
    if (mHandler != null) {
      mHandler.dispatchMessage(Message.obtain(mHandler, MSG_CONNECT_TO_NW_CANCELLED));
    }
  }

  @Override
  protected Boolean doInBackground(WifiConfiguration... params) {
    WifiConfiguration conf = params[0];
    WifiManager w = (WifiManager) mContext
        .getSystemService(Context.WIFI_SERVICE);

    List<WifiConfiguration> wcs = null;

    bssid = conf.BSSID;
    ssid = conf.SSID;

    br = new WifiBr();
    IntentFilter i = new IntentFilter();
    i.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    mContext.registerReceiver(br, i);

    int nw_id = -1;
    /* 1. try to connect to the corresponding BSSID */
    Log.d(TAG, "Connect to: " + ssid);
    boolean connection_result = false;
    WifiConfiguration wc;
    wcs = w.getConfiguredNetworks();
    if (wcs != null) {
      for (int j = 0; j < wcs.size(); j++) {
        wc = wcs.get(j);
        if ((wc.SSID != null) && (wc.SSID.equalsIgnoreCase(ssid))) {
          if (ignoreBSSID || (wc.BSSID != null && wc.BSSID.equalsIgnoreCase(bssid))) {
            Log.d(TAG, "Found wc:" + wc.networkId + " : "
                + wc.SSID + " algo: "
                + wc.allowedAuthAlgorithms + " key: "
                + wc.allowedKeyManagement + " grp:"
                + wc.allowedGroupCiphers + " pair: "
                + wc.allowedPairwiseCiphers + " proto:"
                + wc.allowedProtocols + " hidden: "
                + wc.hiddenSSID + " preSharedKey: "
                + wc.preSharedKey);
            nw_id = wc.networkId;
            break;
          }
        }

      }
    }


    int retries = 2;
    do {

      if (isCancelled()) {
        break;
      }

      boolean enabled;
      enabled = w.enableNetwork(nw_id, true);
      Log.d(TAG, "enableNetwork: " + nw_id + " : " + enabled);
      Log.d(TAG, ">>> current connection ip: " + w.getConnectionInfo().getIpAddress());

      if (ignoreBSSID
          || (w.getConnectionInfo().getBSSID() == null)
          || (!w.getConnectionInfo().getBSSID()
          .equalsIgnoreCase(bssid))
          || (w.getConnectionInfo().getIpAddress() == 0)

          ) {

        // need to wait for the network to be up
        synchronized (wifiLockObject) {
          try {
            wifiLockObject.wait(30000);
          } catch (InterruptedException e) {
            e.printStackTrace();

          } finally {
            if (isCancelled()) {
              w.disableNetwork(nw_id);
              break;
            }
          }
        }

        int waiting_retries = 60;
        do {
          if ((w.getConnectionInfo() != null &&
              w.getConnectionInfo().getIpAddress() != 0) &&
              w.getDhcpInfo() != null && w.getDhcpInfo().ipAddress != 0) {
            //We're connected but don't have any IP yet
            Log.d(TAG, "IP: " + (w.getDhcpInfo().ipAddress & 0xFF) + "." + ((w.getDhcpInfo().ipAddress >> 8) & 0xFF) + "." +
                    ((w.getDhcpInfo().ipAddress >> 16) & 0xFF) + "." + ((w.getDhcpInfo().ipAddress >> 24) & 0xFF));
            Log.d(TAG, "SV: " + (w.getDhcpInfo().serverAddress & 0xFF) + "." + ((w.getDhcpInfo().serverAddress >> 8) & 0xFF) + "." +
                    ((w.getDhcpInfo().serverAddress >> 16) & 0xFF) + "." + ((w.getDhcpInfo().serverAddress >> 24) & 0xFF));

            String current_ssid = w.getConnectionInfo().getSSID();
            if (ssid != null &&
                (ssid.equals(PublicDefine.convertToQuotedString(current_ssid)) ||
                    ssid.equals(current_ssid))) {
              break;
            } else {
              Log.d(TAG, "Connected to unexpected network -> try to enable expected network");
              enabled = w.enableNetwork(nw_id, true);
              Log.d(TAG, "enableNetwork: " + nw_id + " : " + enabled);
            }
          }

          //Log.d(TAG, "connected but don't have any IP yet...");
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        while (waiting_retries-- > 0 && !isCancelled());
      }

      ConnectivityManager conn = (ConnectivityManager) mContext
          .getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo wifiInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

      /*
       * From Android 5.0 (Lollipop), we can't auto switch mobile network on/off anymore.
       * So the problem is: if both wifi & mobile data are ON and "auto switch network" setting is also ON,
       * the method wifiInfo.isConnected() can always return false here.
       * Thus, the solution is: don't need to check wifiInfo.isConnected(), because app still can
       * communicate with camera while wifiInfo.isConnected() false.
       */
      //if (wifiInfo != null && wifiInfo.isConnected())
      if (wifiInfo != null) {
        Log.e(TAG, "Wifi info is connected? " + wifiInfo.isConnected());
        if ( /* some check on the network id */
            (w.getConnectionInfo() != null
                && w.getConnectionInfo().getNetworkId() != -1)
                && w.getDhcpInfo() != null
                && (ignoreBSSID || (w.getConnectionInfo().getBSSID() != null)
                && w.getConnectionInfo().getBSSID()
                .equalsIgnoreCase(bssid))) {
          Log.d(TAG, "got the correct BSSID!!>>>>>>> ip: "
              + w.getDhcpInfo().ipAddress + "\n gw ip: "
              + w.getDhcpInfo().gateway);


          if ((w.getConnectionInfo().getIpAddress() == 0)
              || (w.getDhcpInfo().ipAddress == 0)) {
            // We're connected but don't have any IP yet
            //retries++; // give 1 more chance
            //Log.d(TAG, "connected but don't have any IP yet...");

            // Don't loop too fast, should sleep a bit here
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            continue;
          } else {
            // Now we're sure that the connection is ready
            connection_result = true;
            break;
          }
        }

      } else {
        Log.e(TAG, "Wifi info is null");
      }
    }
    while (retries-- > 0);

    if ((connection_result == false)
        && (dontRemoveConfigurationIfConnectFailed == false)) {
      /* bad connection - remove it */
      w.removeNetwork(nw_id);
      w.saveConfiguration();
    }

    if (br != null && !isCancelled()) {
      try {
        mContext.unregisterReceiver(br);
      } catch (IllegalArgumentException iae) {
        // ignore it
      }
    }

    return new Boolean(connection_result);
  }

  private String rawIpToString(int rawIp) {
    return intToInetAddress(rawIp).getHostAddress();
  }

  public static InetAddress intToInetAddress(int hostAddress) {
    byte[] addressBytes = {(byte) (0xff & hostAddress),
        (byte) (0xff & (hostAddress >> 8)),
        (byte) (0xff & (hostAddress >> 16)),
        (byte) (0xff & (hostAddress >> 24))};

    try {
      return InetAddress.getByAddress(addressBytes);
    } catch (UnknownHostException e) {
      throw new AssertionError();
    }
  }

  private class WifiBr extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      if (wm != null && wm.getDhcpInfo() != null) {
        Log.d(TAG, "NW_STATE, ip address: " + wm.getDhcpInfo().ipAddress);
        if (wm.getDhcpInfo().ipAddress != 0) {
          synchronized (wifiLockObject) {
            wifiLockObject.notify();
          }
        }
      }
    }
  }
}
