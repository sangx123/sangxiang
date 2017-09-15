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

import com.hubble.registration.Util;
import com.hubble.registration.models.CamConfiguration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class VerifyNetworkKeyTask extends AsyncTask<CamConfiguration, String, Boolean> {


  public static final int MSG_NETWORK_KEY_VERIFY_PASSED = 0x11112222;
  public static final int MSG_NETWORK_KEY_VERIFY_FAILED = 0x11113333;

  private static final int DEFAULT_WAITING_FOR_CONNECTION = 30000;//sec
  private static final String TAG = "VerifyNetworkKeyTask";
  private Context mContext;
  private Handler mHandler;
  private Object wifiLockObject;

  private NetworkInfo mWifiNetworkInfo;

  private String home_ssid, home_key;
  private String security_type;
  private String wep_key_index;

  private CamConfiguration myConfig;
  private WifiConfiguration conf;

  public VerifyNetworkKeyTask(Context mContext, Handler mHandler) {
    this.mContext = mContext;

    wifiLockObject = new Object();
    mWifiNetworkInfo = null;
    wep_key_index = null;
    conf = null;
    this.mHandler = mHandler;
    myConfig = null;

  }

  private boolean connectivityTest() {

    try {
      URL url = new URL("https://www.justfuckinggoogleit.com");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(2000);
      conn.setReadTimeout(2000);
      conn.connect();

      int response = conn.getResponseCode();

    } catch (MalformedURLException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
      return false;
    } catch (IOException e) {
      // // Log.d("mbp", "Can't reach outsideworld");
      return false;
    }

    return true;
  }

  //private String home_ssid, home_key has to be set before this
  private boolean verify_network_key() {
    boolean is_found;
    int networkId = -1;
    boolean key_valid = false;

    if (home_key == null || home_ssid == null) {
      return key_valid;
    }


//		// // Log.d("mbp", "home_ssid= " + home_ssid + " home_key:" + home_key);
    WifiManager w = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);


    if ((w.getConnectionInfo() != null) &&
        (w.getConnectionInfo().getSSID() != null) &&
        ((w.getConnectionInfo().getSSID().equals(convertToQuotedString(home_ssid))) ||
            (w.getConnectionInfo().getSSID().equals(home_ssid)))) {

      // // Log.d("mbp", "Connecting to same SSID disconnect first");
      //w.disconnect();
    }

		/* Verify the pass String */
    List<WifiConfiguration> wcs = w.getConfiguredNetworks();
    WifiConfiguration wc1 = null;
    is_found = false;
    for (int i1 = 0; i1 < wcs.size(); i1++) {

      wc1 = wcs.get(i1);
      if ((wc1 == null) || (wc1.SSID == null)) {
        continue;
      }
      if (wc1.SSID.equals(home_ssid) || wc1.SSID.equals(convertToQuotedString(home_ssid))) {
        is_found = true;
        //				// // Log.d("mbp","Found: " +home_ssid + " key: " + wc1.allowedKeyManagement +
        //						" grpcp:" + wc1.allowedGroupCiphers + " auth:" + wc1.allowedAuthAlgorithms);
        break;
      }
    }


    if (is_found) {

      if (security_type.equalsIgnoreCase("WEP")) {
        wc1.wepTxKeyIndex = (wep_key_index != null) ? Integer.parseInt(wep_key_index) - 1 : 0;

				
				/*20120203: WEP key in HEX len should be either 26 or 10
         * ??? COULD BE WRONG  */
        if ((home_key.length() == 26 || home_key.length() == 10) && Util.isThisAHexString(home_key)) {
          wc1.wepKeys[wc1.wepTxKeyIndex] = home_key;
        } else {
          wc1.wepKeys[wc1.wepTxKeyIndex] = convertToQuotedString(home_key);
        }
        //wc1.wepKeys[wc1.wepTxKeyIndex] = convertToQuotedString(home_key);
        //wc1.wepKeys[wc1.wepTxKeyIndex] = convertToQuotedString(home_key);
        //wc1.wepKeys[wc1.wepTxKeyIndex] = convertToQuotedString(home_key);
      } else if (security_type.equalsIgnoreCase("WPA/WPA2")) {
        /*20120203: WPA key in HEX len should be 64
         * ??? COULD BE WRONG  */

        if ((home_key.length() == 64) && Util.isThisAHexString(home_key)) {
          wc1.preSharedKey = home_key;
        } else {
          wc1.preSharedKey = convertToQuotedString(home_key);
        }
      } else //Else it should be OPEN
      {
      }


      networkId = w.updateNetwork(wc1);

			/* updated failed */
      if (networkId == -1) {
        // // Log.d("mbp", "networkId == -1, remove network configuration");
        w.removeNetwork(networkId);
        w.saveConfiguration();
        return false;
      }
      w.enableNetwork(networkId, true);
      //w.reconnect();


      synchronized (wifiLockObject) {
        try {
          wifiLockObject.wait(DEFAULT_WAITING_FOR_CONNECTION);
        } catch (InterruptedException e) {
          // // Log.e(TAG, Log.getStackTraceString(e));
        }
      }

      int max_try = 60;

      int retries = 0;
      do {
        if ((w.getConnectionInfo().getIpAddress() != 0) && (w.getDhcpInfo().ipAddress != 0)) {
          //We're connected but don't have any IP yet

          // // Log.d("mbp", "IP:" + ((w.getDhcpInfo().ipAddress & 0xFF000000) >> 24) + "" +
          //  ((w.getDhcpInfo().ipAddress & 0x00FF0000) >> 16) + "." +
          //  ((w.getDhcpInfo().ipAddress & 0x0000FF00) >> 8) + "." +
          //  (w.getDhcpInfo().ipAddress & 0x000000FF));

          // // Log.d("mbp", "SV:" + ((w.getDhcpInfo().serverAddress & 0xFF000000) >> 24) + "" +
          //  ((w.getDhcpInfo().serverAddress & 0x00FF0000) >> 16) + "." +
          //  ((w.getDhcpInfo().serverAddress & 0x0000FF00) >> 8) + "." +
          //  (w.getDhcpInfo().serverAddress & 0x000000FF));
          break;
        }
        // // Log.d(TAG, "connected but don't have any IP yet...");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          // // Log.e(TAG, Log.getStackTraceString(e));
        }
      } while (retries++ < max_try);

      ConnectivityManager conn = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo wifiInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      if (wifiInfo != null && wifiInfo.isConnectedOrConnecting()) {
        retries = 0;
        while (retries++ < 20 && (!connectivityTest())) {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
          }
        }

        key_valid = true;

      } else {
        key_valid = false;
        // // Log.d("mbp", "Remove failed configuration");
        w.removeNetwork(networkId);
        w.saveConfiguration();

      }
      mWifiNetworkInfo = null;
    } else //This is a new network .. then how?
    {
      // // Log.d("mbp", " new network ...");
      /* 20111012: terrible way !!! */
      if (conf != null) {

        if (security_type.equalsIgnoreCase("WEP")) {
          conf.wepTxKeyIndex = (wep_key_index != null) ? Integer.parseInt(wep_key_index) - 1 : 0;
					
					/*20120203: WEP key in HEX len should be either 26 or 10
					 * ??? COULD BE WRONG  */
          if ((home_key.length() == 26 || home_key.length() == 10) && Util.isThisAHexString(home_key)) {
            conf.wepKeys[conf.wepTxKeyIndex] = home_key;
          } else {
            conf.wepKeys[conf.wepTxKeyIndex] = convertToQuotedString(home_key);
          }

          //wc1.wepKeys[wc1.wepTxKeyIndex] = convertToQuotedString(home_key);
          //wc1.wepKeys[wc1.wepTxKeyIndex] = convertToQuotedString(home_key);
          //wc1.wepKeys[wc1.wepTxKeyIndex] = convertToQuotedString(home_key);
        } else if (security_type.equalsIgnoreCase("WPA/WPA2")) {
					
					/*20120203: WPA key in HEX len should be 64
					 * ??? COULD BE WRONG  */
          if ((home_key.length() == 64) && Util.isThisAHexString(home_key)) {
            conf.preSharedKey = home_key;
          } else {
            conf.preSharedKey = convertToQuotedString(home_key);
          }
        } else {
          //if its open .. return true anwyay
          //return true;
        }

        networkId = w.addNetwork(conf);

				/* updated failed */
        if (networkId == -1) {
          // // Log.d("mbp", "adddNetwork failed");
          return false;
        }
        w.enableNetwork(networkId, true);
        //w.reconnect();

        synchronized (wifiLockObject) {
          try {
            wifiLockObject.wait(DEFAULT_WAITING_FOR_CONNECTION);
          } catch (InterruptedException e) {
            // // Log.e(TAG, Log.getStackTraceString(e));
          }
        }
        int retries = 0;

        ConnectivityManager conn = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnectedOrConnecting()) {

          do {
            if ((w.getConnectionInfo().getIpAddress() != 0) && (w.getDhcpInfo().ipAddress != 0)) {
              //We're connected but don't have any IP yet
              break;
            }
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              // // Log.e(TAG, Log.getStackTraceString(e));
            }
          } while (retries++ < 60);

          key_valid = true;

        } else {
          key_valid = false;
        }
        mWifiNetworkInfo = null;
      } else {
				/* create a new configuration */
        // // Log.d("mbp", this.getClass().getName() + " Should not be here");
        return false;
      }


    }

    return key_valid;
  }


  protected Boolean doInBackground(CamConfiguration... arg0) {
    myConfig = arg0[0];
    home_ssid = myConfig.ssid();
    home_key = myConfig.pass_string();
    security_type = myConfig.security_type();
    wep_key_index = myConfig.key_index();
    conf = myConfig.wc();

    WifiReceiver br = null;

    IntentFilter i = new IntentFilter();
    br = new WifiReceiver();
    i.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    mContext.registerReceiver(br, i);

    boolean ret = verify_network_key();

    try {
      mContext.unregisterReceiver(br);
    } catch (IllegalArgumentException iae) {
      // // Log.e(TAG, "Receiver not registered, but unregisterReceiver was called: " + Log.getStackTraceString(iae));
    }
    return ret;
  }

  /* on UI Thread */
  protected void onPreExecute() {
  }

  /* on UI Thread */
  protected void onProgressUpdate(String... progress) {

  }

  /* on UI Thread */
  protected void onPostExecute(Boolean result) {

    Message m;
    if (result) {
      m = Message.obtain(mHandler, MSG_NETWORK_KEY_VERIFY_PASSED);
    } else {
      m = Message.obtain(mHandler, MSG_NETWORK_KEY_VERIFY_FAILED);
    }
    mHandler.dispatchMessage(m);
  }


  protected static String convertToQuotedString(String string) {
    return "\"" + string + "\"";
  }


  private void notifyScanResult() {
    synchronized (this) {
      this.notify();
    }
  }

  private void notifyWifiState(String bssid) {
    synchronized (wifiLockObject) {

      wifiLockObject.notify();
    }
  }

	/* For WPA2 authentication:
	 * SupplicantState in case of ERROR
	 *  - (ASSOCIATING)
	 *  - ASSOCIATED 
	 *  - FOUR_WAY_HANDSHAKE 
	 *  - DISCONNECTED 
	 *  - INACTIVE  <---- We will wait for this
	 * SupplicantState in case of SUCCESS
	 *  - (ASSOCIATING)
	 *  - ASSOCIATED 
	 *  - FOUR_WAY_HANDSHAKE 
	 *  - GROUP_HANDSHAKE
	 *  - COMPLETED  <---- We will wait for this
	 */


  private class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      switch (action) {
        case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
          notifyScanResult();
          break;
        case WifiManager.NETWORK_STATE_CHANGED_ACTION:
          mWifiNetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

          // // Log.d("mbp", this.getClass().getName() + " NETWORK_STATE_CHANGED_ACTION: " + mWifiNetworkInfo.isConnected());

          if ((mWifiNetworkInfo != null) && mWifiNetworkInfo.isConnected()) {
            // // Log.d("mbp", "NOTIFY - Connected");
            notifyWifiState(null);

          }
          break;
        default:
          break;
      }
    }
  }
}
