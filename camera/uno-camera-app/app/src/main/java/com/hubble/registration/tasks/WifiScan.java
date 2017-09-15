package com.hubble.registration.tasks;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;

import com.hubble.registration.interfaces.IWifiScanUpdater;
import com.hubbleconnected.camera.R;

import java.util.List;
import java.util.ListIterator;

public class WifiScan extends AsyncTask<String, String, List<ScanResult>> {

  private static final String TAG = "WifiScan";
  private final int DEFAULT_SEARCHING_FOR_NEW_NETWORK = 5000; //milliseconds
  private Object wifiObject;
  private IWifiScanUpdater _updater;
  private Context mContext;

  private NetworkInfo mWifiNetworkInfo;

  private ProgressDialog dialog;
  private boolean silence;
  private String dialog_message;


  public WifiScan(Context mContext, IWifiScanUpdater u) {
    this.mContext = mContext;

    this._updater = u;

    wifiObject = new Object();
    mWifiNetworkInfo = null;
    silence = false;
  }

  public String getDialog_message() {
    return dialog_message;
  }

  public void setSilence(boolean s) {
    silence = s;
  }

	/* 
   * (non-Javadoc)
	 * @Param[]: - SSID of cameras in Setup-mode
	 *           - SSID of Network to be configured 
	 *           - Key  of network to be configured
	 *           - Max_num_of_Cam (int string)
	 *  Description:
	 *  		 - Disconnect fr Original Network
	 *           Search for "MotAndroid" ssid
	 *           Try to connect 
	 *           send over the configuration using config_write
	 *           send over the restart cmd 
	 *           - expect irabot reboot - 
	 *           Repeat until we have found Max_num_of_Cam (4) 
	 *           Or when we hit a time out - 
	 *           - Reconnect back to Original Network
	 */

  @Override
  protected List<ScanResult> doInBackground(String... arg0) {
    WifiReceiver_1 br = null;
    List<ScanResult> list = null;

    WifiManager w = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

    IntentFilter i = new IntentFilter();
    br = new WifiReceiver_1();

    i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    i.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    mContext.registerReceiver(br, i);

    if (w.startScan()) {
      //Scanning started
      synchronized (this) {
        try {
          // // Log.d(TAG, "Waiting for wifi results");
          this.wait(DEFAULT_SEARCHING_FOR_NEW_NETWORK);
        } catch (InterruptedException e) {
          // // Log.e(TAG, Log.getStackTraceString(e));
        }
      }

      list = w.getScanResults();
    } else {
    }

    if (!isCancelled() && br != null && mContext != null) {
      try {
        mContext.unregisterReceiver(br);
      } catch (IllegalArgumentException ignored) {
      }
    }

		/*20121017: filter 5Ghz network */
    if (list != null) {
      ListIterator<ScanResult> li = list.listIterator();
      ScanResult result = null;
      while (li.hasNext()) {
        result = li.next();
        if (result.frequency > 3000) {
          li.remove();
        }
      }
    }
    return list;
  }


  /* on UI Thread */
  protected void onPreExecute() {
    if (silence) {
      return;
    }
    String msg = getDialog_message();
    if (msg == null) {
      msg = "Scanning for Wifi network."; // TODO: Extract string
    }
    Spanned msg_1 = Html.fromHtml("<big>" + msg + "</big>");
    dialog = new ProgressDialog(mContext);
    dialog.setMessage(msg_1);
    dialog.setIndeterminate(true);
    dialog.setCancelable(true);
    dialog.setOnCancelListener(new OnCancelListener() {

      @Override
      public void onCancel(DialogInterface dialog) {
        //WifiScan.this.cancel(false);
        WifiScan.this.cancel(true);
      }
    });
    dialog.setButton(mContext.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    dialog.show();
  }

  /* on UI Thread */
  protected void onProgressUpdate(String... progress) {
    if (dialog != null) {
      dialog.setMessage(progress[0]);
    }
  }

  protected void onCancelled() {
    onCancelled(null);
  }

  protected void onCancelled(List<ScanResult> result) {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }
    // // Log.d("mbp", "onCancel called");
    //Don't call update

    if (_updater != null) {
      _updater.scanWasCanceled();
    }


  }

  /* on UI Thread */
  protected void onPostExecute(List<ScanResult> result) {
    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }

    if (_updater != null) {
      _updater.updateWifiScanResult(result);
    }
  }


  private void notifyScanResult() {
    synchronized (this) {
      this.notify();
    }
  }

  public void notifyWifiState() {
    synchronized (wifiObject) {
      wifiObject.notify();
    }
  }

  private class WifiReceiver_1 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      switch (action) {
        case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
          // // Log.d(TAG, "Got wifi results");
          notifyScanResult();
          break;
        case WifiManager.NETWORK_STATE_CHANGED_ACTION:
          mWifiNetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

          if (mWifiNetworkInfo != null && mWifiNetworkInfo.isConnected()) {
            notifyWifiState();
          }
          break;
        case WifiManager.WIFI_STATE_CHANGED_ACTION:
          break;
        default:
          break;
      }
    }
  }
}
