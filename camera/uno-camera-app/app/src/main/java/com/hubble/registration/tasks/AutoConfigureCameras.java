package com.hubble.registration.tasks;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;

import com.discovery.LocalScanForCameras;
import com.discovery.ScanProfile;
import com.hubble.HubbleApplication;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.UserAccount;
import com.hubble.registration.Util;
import com.hubble.registration.interfaces.ICameraScanner;
import com.hubble.registration.models.CamConfiguration;
import com.hubble.registration.models.CameraWifiEntry;
import com.hubble.registration.models.LegacyCamProfile;
import com.hubble.registration.models.NameAndSecurity;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubbleconnected.camera.R;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import base.hubble.PublicDefineGlob;
import base.hubble.meapi.Device;


public class AutoConfigureCameras extends AsyncTask<CamConfiguration, String, String> implements ICameraScanner {

  private static final String TAG = "AutoConfigureCameras";

  private static final int DEFAULT_SEARCHING_FOR_NEW_NETWORK = 20000;//sec
  private static final int DEFAULT_WAITING_FOR_CONNECTION = 30000;//sec


  private int errorCode;

  //use when does not find ssid in wifi list
  private boolean continueAddCamera;
  private boolean isWaiting;

  private Context mContext;
  private Handler mHandler;
  private Object wifiLockObject;

  private Object supplicantLockObject;
  private SupplicantState sstate;


  private NetworkInfo mWifiNetworkInfo;
  private int mWifiState;

  //private ProgressDialog dialog;

  private String home_ssid, home_key;
  private String security_type;
  private String wep_auth_mode, wep_key_index;
  private String http_usr, http_pass;

  private WifiConfiguration conf;
  private LocalScanForCameras scan_task;

  private boolean isCancelable;

  private LegacyCamProfile cam_profile = null;
  private long add_camera_start_time = -1;
  public AutoConfigureCameras(Context mContext, Handler h) {
    this.mContext = mContext;

    wifiLockObject = new Object();
    supplicantLockObject = new Object();
    sstate = null;
    mWifiNetworkInfo = null;
    mWifiState = -1;
    wep_auth_mode = wep_key_index = null;
    conf = null;
    mHandler = h;
    scan_task = null;
  }

  public void setAddCameraStartTime(long add_camera_start_time) {
    this.add_camera_start_time = add_camera_start_time;
  }

	/*
   * (non-Javadoc)
	 * @Param[]: - SSID of cameras in Setup-mode
	 *  		 - Max_num_of_Cam (int string)
	 *           - SSID of Network to be configured
	 *           - Key  of network to be configured
	 *           - Security type (wpa or wep)
	 *           - WEP method (null if not valid)
	 *           - WEP key (null if not valid)
	 *
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

  protected String doInBackground(CamConfiguration... arg0) {

    isCancelable = false;
    String saved_ssid = arg0[0].device_ssid();
    home_ssid = arg0[0].ssid();
    home_key = arg0[0].pass_string();
    security_type = arg0[0].security_type();
    wep_auth_mode = arg0[0].auth_method();
    wep_key_index = arg0[0].key_index();
    conf = arg0[0].wc();
    cam_profile = arg0[0].getCamProfiles()[0];

    http_usr = arg0[0].getHttpUsr();
    http_pass = arg0[0].getHttpPass();

    String output = "Done";
    WifiReceiver1 br = null;
    List<WifiConfiguration> wcs;
    IntentFilter i = new IntentFilter();
    br = new WifiReceiver1();

    i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    i.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    i.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);

    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

    publishProgress(mContext.getString(R.string.AutoConf_5));

    //remove the old configuration if any
    // // Log.d(TAG, "Removing all CameraHD-... entries");
    wcs = wifiManager.getConfiguredNetworks();
    for (WifiConfiguration wc : wcs) {
      if ((wc == null) || (wc.SSID == null)) {
        // // Log.d(TAG, " Stuck??  size is: " + wcs.size() + " wc == null? " + (wc == null));
        if (wc != null) {
          // // Log.d(TAG, " Stuck??  wc.ssid == null");
        }
        continue;
      }

      for (String cameraSSID : PublicDefineGlob.CAMERA_SSID_LIST) {
        if (wc.SSID.startsWith("\"" + cameraSSID)) {
          Log.d(TAG, "remove id: " + wc.networkId + " ssid:" + wc.SSID);
          wifiManager.removeNetwork(wc.networkId);
          break;
        }
      }
    }

    // // Log.d(TAG, " DONT save configuration..");

    if (wifiManager.startScan()) {
      mContext.registerReceiver(br, i);
      //Scanning started
      synchronized (this) {
        try {
          this.wait(DEFAULT_SEARCHING_FOR_NEW_NETWORK);
        } catch (InterruptedException e) {
          // // Log.e(TAG, Log.getStackTraceString(e));
        }
      }

      if (!isCancelled()) {
        mContext.unregisterReceiver(br);
      }

      // now we can read the result back
      List<ScanResult> list = wifiManager.getScanResults();

			/* list of network id to configure after adding */
      int[] networkIds = {-1, -1, -1, -1};
      int networkIds_idx = 0;
      //mWifiNetworkInfo= null;
      for (ScanResult scanned_nw : list) {

        if (scanned_nw.SSID == null) {
          // // Log.d(TAG, "Scanned SSID = null!");
          continue;
        }
        for (String cameraSSID : PublicDefineGlob.CAMERA_SSID_LIST) {
          if (scanned_nw.SSID.startsWith(cameraSSID)) {
            NameAndSecurity _ns = new NameAndSecurity(scanned_nw.SSID, scanned_nw.capabilities, scanned_nw.BSSID);
            _ns.setShowSecurity(false);
            _ns.setHideMac(true);
            String bssid = arg0[0].getDeviceBSSIDList().elementAt(0);
            if (scanned_nw.SSID.equalsIgnoreCase(saved_ssid)) {
              //found 1 camera in range, add new configuration
              WifiConfiguration wc = new WifiConfiguration();
              wc.SSID = PublicDefine.convertToQuotedString(scanned_nw.SSID);
              saved_ssid = PublicDefine.convertToQuotedString(scanned_nw.SSID);
              wc.BSSID = scanned_nw.BSSID;
              wc.hiddenSSID = false;
              wc.allowedAuthAlgorithms = _ns.getAuthAlgorithm();
              wc.allowedKeyManagement = _ns.getKeyManagement();
              wc.status = WifiConfiguration.Status.ENABLED;
              if (_ns.security.equalsIgnoreCase("WPA")) {
                wc.preSharedKey = PublicDefine.convertToQuotedString(PublicDefineGlob.DEFAULT_WPA_PRESHAREDKEY);
              }
              wc.allowedGroupCiphers = _ns.getGroupCiphers();
              wc.allowedPairwiseCiphers = _ns.getPairWiseCiphers();
              wc.allowedProtocols = _ns.getProtocols();

              logWifiConfiguration(wc);

              int netid = wifiManager.addNetwork(wc);
              // // Log.d(TAG, "added camera nw with id: " + netid);
              if (networkIds_idx < networkIds.length) {
                networkIds_idx++;
              }
            }
            break;
          }
        }
      }

      // // Log.d(TAG, "save configuration..2nd");

      wifiManager.saveConfiguration();
      //No setup network being added
      if (networkIds_idx == 0) {
        // // Log.d(TAG, "no setup_ssid found: " + PublicDefineGlob.DEFAULT_SSID);
        publishProgress(String.format(mContext.getResources().getString(R.string.AutoConf_can_t_find_1), PublicDefineGlob.DEFAULT_SSID));
        output = "Failed";
      } else //
      {

				/* Scan the list of Configured N/W to update the newtork id array
         * reason being... below..
				 */
        networkIds_idx = 0;
        /* Note: It is possible for this method to change the network
         * IDs of existing networks. You should assume the network IDs can
				 * be different after calling this method.
				 */
        boolean connecting = false;
        String regId = arg0[0].getDeviceBSSIDList().elementAt(0);
        wcs = wifiManager.getConfiguredNetworks();
        // // Log.d(TAG, "Find regId in phone wifi list: " + regId);
        for (WifiConfiguration wc : wcs) {
          if (wc.SSID != null && wc.SSID.equals(saved_ssid)) {
            //start connecting.. now
            // // Log.d(TAG, "start connect to nw with id: " + wc.networkId);
            wifiManager.enableNetwork(wc.networkId, true);
            connecting = true;
            break;


          }
        }
        if (connecting) {

					/* We have finished adding all wificonfiguration of devices
           * Now for each of the network ID, try to connect and send the configuration
					 * data over
					 */
          boolean retry_waiting = true;
          output = "Done";

          do {

            try {
              Thread.sleep(1000);
            } catch (InterruptedException e1) {
              // // Log.e(TAG, Log.getStackTraceString(e1));
            }

            mContext.registerReceiver(br, i);

            //how to know when Iam connected ?
            synchronized (wifiLockObject) {
              try {
                wifiLockObject.wait(DEFAULT_WAITING_FOR_CONNECTION);
              } catch (InterruptedException e) {
                // // Log.e(TAG, Log.getStackTraceString(e));
              }
            }

            if (!isCancelled()) {
              mContext.unregisterReceiver(br);
            }

            int max_try = 30;
            int retries = 0;
            do {
              if ((wifiManager.getConnectionInfo().getIpAddress() != 0) && (wifiManager.getDhcpInfo().ipAddress != 0)) {
                //We're connected but don't have any IP yet
                // // Log.d(TAG, "IP:" + ipAddrToString(wifiManager.getDhcpInfo().ipAddress));
                // // Log.d(TAG, "SV:" + ipAddrToString(wifiManager.getDhcpInfo().serverAddress));
                break;
              }
              // // Log.d(TAG, "connected to camera but don't have any IP yet...");
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                // // Log.e(TAG, Log.getStackTraceString(e));
              }
            } while (retries++ < max_try && !isCancelled());


            // // Log.d(TAG, "Current ssid: " + wifiManager.getConnectionInfo().getSSID() + " setup_ssid:" + saved_ssid + " setup_regId: " + regId);

            ConnectivityManager conn = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiInfo == null) //sth is wrong
            {
              publishProgress(String.format(mContext.getResources().getString(R.string.AutoConf_2), saved_ssid));
            } else {
              String cameraRegId = null;
              //try connect 10s

              String DEVICE_PORT = PublicDefineGlob.DEFAULT_DEVICE_PORT;
              String gatewayIp = Util.getGatewayIp(mContext);
              PublicDefineGlob.DEVICE_PORT = PublicDefineGlob.DEFAULT_DEVICE_PORT;

              long try_connect_time = System.currentTimeMillis() + 20000;
              do {
                try {
                  cameraRegId = HTTPRequestSendRecvTask.getUdid(gatewayIp, PublicDefineGlob.DEVICE_PORT, PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, PublicDefineGlob.DEFAULT_CAM_PWD);
                  break;
                } catch (ConnectException e1) {
                  // // Log.e(TAG, Log.getStackTraceString(e1));
                }
              }
              while (System.currentTimeMillis() < try_connect_time && !isCancelled());

              // // Log.d(TAG, "get mac result: " + cameraRegId);
              // // Log.d(TAG, "camera bssid: " + regId);

              if (
                  /*
                  (wifiManager.getConnectionInfo().getBSSID() != null)  &&
									(wifiManager.getConnectionInfo().getBSSID().equalsIgnoreCase(bssid))
									 */
                  cameraRegId != null && cameraRegId.equalsIgnoreCase(regId)) {

                if ((wifiManager.getConnectionInfo().getIpAddress() == 0) || (wifiManager.getDhcpInfo().ipAddress == 0)) {
                  //Dont increase j repeat this step once
                  retry_waiting = false;//reset it here
                  continue; // well... break. do {} while(false){}
                }

                publishProgress(mContext.getString(R.string.AutoConf_5));
                //publishProgress(String.format(mContext.getString(R.string.AutoConf_3),setup_ssid ));

                //TODO
                /* 20130228: hoang: issue 1307
                 * skip warning message when hidden SSID
								 */
                if (!conf.hiddenSSID) {
                  // CHECK if the HOME wifi is seen by the camera
                  QueryCameraWifiListTask getWifiList = new QueryCameraWifiListTask(this.mContext.getApplicationContext(), cam_profile.getFirmwareVersion(), cam_profile.getModelId(), cam_profile.getRegistrationId());
                  getWifiList.executeOnExecutor(THREAD_POOL_EXECUTOR, "dummy");
                  ArrayList<CameraWifiEntry> wifiList = null;

                  try {
                    wifiList = (ArrayList<CameraWifiEntry>) getWifiList.get(30000, TimeUnit.MILLISECONDS);
                  } catch (ExecutionException | TimeoutException | InterruptedException e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                  }

                  boolean foundSsidInCamera = false;
                  if (wifiList != null) {

                    if (wifiList.isEmpty()) {
                      // // Log.d(TAG, "Wifi list is empty");
                    }

                    //Search for the home ssid in the camera wifi list
                    CameraWifiEntry entry = null;
                    for (CameraWifiEntry aWifiList : wifiList) {
                      entry = aWifiList;
                      if (entry.getSsidNoQuote().equals(home_ssid)) {
                        foundSsidInCamera = true;
                        break;
                      }
                    }
                    if (!foundSsidInCamera) {
                      // // Log.d(TAG, "Camera does no contain the SSID " + home_ssid);
                      isWaiting = true;
                      continueAddCamera = true;
											/* 20130110: hoang: show alert and wait for user select whether continue add camera or not
											 */
                      publishProgress(mContext.getString(R.string.camera_does_not_have_ssid_warning));
                      while (isWaiting) {
                        try {
                          Thread.sleep(500);
                        } catch (InterruptedException e) {
                          // // Log.e(TAG, Log.getStackTraceString(e));
                        }
                      }

                      if (!continueAddCamera) {
                        errorCode = PublicDefine.CAMERA_DOES_NOT_HAVE_SSID;
                        output = "Failed";
                        break;
                      }
                    } else {
                      // // Log.d(TAG, "Found ssid in camra .. continue");
                    }


                  } else {
                    // NULL wifilist.. wifi list is timeout
                    // // Log.d(TAG, "get wifi list timeout");
                  }
                } else {
                  // // Log.d(TAG, "Hidden SSID - Not need to query wifi list from camera.");
                }

                String device_address_port = gatewayIp + ":" + PublicDefineGlob.DEVICE_PORT;
                // // Log.d(TAG, "Set all default alert settings ON");
                //setDefaultAlertSettings(device_address_port);

                // retry up to 5 times
                int k = 0;
                String response;
                String req = "http://" + gatewayIp + ":" + PublicDefineGlob.DEVICE_PORT +
                    arg0[0].build_setup_request();

                while (k++ < 5 && !isCancelled()) {

                  String set_mkey_cmd = String.format("%1$s%2$s%3$s%4$s%5$s%6$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.SET_MASTER_KEY, PublicDefineGlob.SET_MASTER_KEY_PARAM_1, arg0[0].getMasterKey());
                  response = send_request(set_mkey_cmd, 20000);
                  // // Log.d(TAG, "Mkey response: " + response);
                  //set_master_key: 0
                  if (!verify_response(PublicDefineGlob.MASTER_KEY_RESPONSE, response)) {
                    //loop back;
                    // // Log.d(TAG, "Failed to send MKEY: " + response + " retry: " + k);
                    output = "Failed";
                    errorCode = PublicDefine.SEND_DATA_TO_CAMERA_FAILED;
                    continue;
                  }

                  int offset = TimeZone.getDefault().getRawOffset();
                  if (TimeZone.getDefault().useDaylightTime()) {
                    offset = Util.getTimeZoneOffsetWithDST();
                  }
                  String timeZone = String.format("%s%02d.%02d", offset >= 0 ? "+" : "-", Math.abs(offset) / 3600000, (Math.abs(offset) / 60000) % 60);
                  // // Log.d(TAG, "Send time zone " + timeZone + " to camera");
                  String set_tz_cmd = String.format("%1$s%2$s%3$s%4$s%5$s%6$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.SET_TIME_ZONE, PublicDefineGlob.SET_TIME_ZONE_PARAM, timeZone);
                  response = send_request(set_tz_cmd, 20000);
                  // // Log.d(TAG, "Timezone response: " + response);
                  if (!verify_response(PublicDefineGlob.SET_TIME_ZONE_RESPONSE, response)) {
                    //ignore response
                  }

                  // // Log.d(TAG, "Send wifi config to camera");
                  response = send_request(req, 20000);
                  // // Log.d(TAG, "wifi res response: " + response);
                  if (!verify_response(PublicDefineGlob.WIRELESS_SETUP_SAVE_RESPONSE, response)) {
                    // // Log.d(TAG, "Failed to send WIRELESS info: " + response + " retry: " + k);
                    output = "Failed";
                    errorCode = PublicDefine.SEND_DATA_TO_CAMERA_FAILED;
                  } else {
                    output = "Done";
                    break;
                  }


                }
                if (output.equalsIgnoreCase("Failed")) {
                  break;
                }
                String restart_cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.RESTART_DEVICE);
                response = send_request(restart_cmd, 10000);
                // // Log.d(TAG, "restart--->>: " + response);
                //"restart_system: 0"  / -1

                if (!verify_response(PublicDefineGlob.RESTART_DEVICE_RESPONSE, response)) {
                  response = send_request(restart_cmd, 10000);
                }

                mWifiNetworkInfo = null;
              } /*  wc.BSSID == wifiManager.getConnectionInfo().getBSSID() */ else // ERROR OUT -- cant' connect to camera network.
              {
                output = "Failed";
                errorCode = PublicDefine.CONNECT_TO_CAMERA_FAILED;

                // // Log.d(TAG, "cant' connect to camera network.");
                break;
              }
            } // wifiInfo != null
          } while (false);
        } //if connecting == true
        else {
          output = "Failed";
          // // Log.d(TAG, "Failed to start Find camera network in Phone's wifi");
          errorCode = PublicDefine.CONNECT_TO_CAMERA_FAILED;
        }
      } // if networkId > 0
    } else {
      output = "Failed";
      // // Log.d(TAG, "Start scan failed");
      errorCode = PublicDefine.CONNECT_TO_CAMERA_FAILED;
    }

    long time_to_show_abort_page = System.currentTimeMillis() + 60 * 1000;

    publishProgress(mContext.getString(R.string.AutoConf_5));

    //publishProgress(String.format(mContext.getString(R.string.AutoConf_4) , home_ssid));

    // reconnect back to the original network
    wcs = wifiManager.getConfiguredNetworks();
    for (WifiConfiguration wc : wcs) {
      if ((wc == null) || (wc.SSID == null)) {
        continue;
      }

      if (wc.SSID.equalsIgnoreCase(PublicDefine.convertToQuotedString(home_ssid))) {
        wifiManager.enableNetwork(wc.networkId, true);
        //wifiManager.reconnect();
      }
    }
    mContext.registerReceiver(br, i);

    //how to know when Iam connected ?
    synchronized (wifiLockObject) {
      try {
        wifiLockObject.wait(DEFAULT_WAITING_FOR_CONNECTION);
      } catch (InterruptedException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      }
    }
    if (!isCancelled()) {
      mContext.unregisterReceiver(br);
    }

    isCancelable = true;
    publishProgress(mContext.getString(R.string.AutoConf_5));

    // scan for this camera here ..
    if (output.equals("Done")) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e2) {
      }
      //InetAddress dummy;
      ScanProfile[] cps = null;
      String regId;
      regId = arg0[0].getDeviceBSSIDList().elementAt(0);
      String mac = PublicDefine.getMacFromRegId(regId).toUpperCase();
      mac = PublicDefineGlob.add_colon_to_mac(mac);
      boolean foundConfiguredCam = false;
      long time_to_finish_scanning = System.currentTimeMillis() + 4 * 60 * 1000;
      long time_to_query_online = System.currentTimeMillis() + 60 * 1000;
      boolean isAbortPageShowed = false;
      do {
        if (System.currentTimeMillis() > time_to_show_abort_page && !isAbortPageShowed) {
          isAbortPageShowed = true;
          if (mHandler != null) {
            mHandler.sendMessage(Message.obtain(mHandler, PublicDefine.MSG_AUTO_CONF_SHOW_ABORT_PAGE));
          }
        }

        // // Log.d(TAG, "start scanning for: " + mac);

        //start scanning now.. for one camera only ;
        scan_task = new LocalScanForCameras(mContext, this);
        scan_task.setShouldGetSnapshot(false);


        //create a dummy profile
        LegacyCamProfile dummy = new LegacyCamProfile(null, mac);
				/*Assume that it is in local Becoz We have just finished configuring it
						  Assume that it is 1 mins since last comm
						    ---NEED to set this to trick ScanForCameras to scan otherwise it will skip this camera
				 */
        dummy.setInLocal(true);
        scan_task.startScan(new LegacyCamProfile[]{dummy});

        //wait for result
        cps = null;
        cps = scan_task.getScanResults();

        if (cps != null && cps.length == 1) {
          if (mac.equalsIgnoreCase(cps[0].get_MAC())) {
            //foundConfiguredCam = true;
            // // Log.d(TAG, "Found it in local");
            //break;
          }

        }

        if (isCancelled()) {
          break;
        }

        //don't need to wait anymore
        //if ( System.currentTimeMillis() >  time_to_query_online )
        {
          // // Log.d(TAG, "checking server for device...");
          //reset to the nxt minute
          time_to_query_online = System.currentTimeMillis() + 60 * 1000;
          String saved_token = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
          UserAccount online_user;
          try {
            online_user = new UserAccount(saved_token, mContext.getExternalFilesDir(null), null, mContext);
          } catch (Exception e1) {
            // // Log.d(TAG, e1.getLocalizedMessage());
            output = "Failed";
            errorCode = PublicDefine.USB_STORAGE_TURNED_ON;
            break;
          }

          boolean isOnline = online_user.queryCameraAvailableOnline(mac);
          if (isOnline) {
            // // Log.d(TAG, "FOUND the IP online??? ");
            foundConfiguredCam = true;
            // // Log.d(TAG, "Anyway .. Found it");
            //update host ssid for camera
            updateHostSsid(saved_token, regId, home_ssid);
            try {
              online_user.sync_user_data();
            } catch (IOException e) {

              // // Log.e(TAG, Log.getStackTraceString(e));
            }
//						BlinkHDApplication.KissMetricsRecord("Add camera succeeded.");
            break;
          } else {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
          }
        }
        if (isCancelled()) {
          break;
        }
      }
      //Keep scanning up to 2 min
      while (System.currentTimeMillis() < time_to_finish_scanning && !isCancelled());

      if (!foundConfiguredCam) {
        //Sth is wrong
        // // Log.d(TAG, "Can't find camera -- ");
        output = "Failed";
        errorCode = PublicDefine.SCAN_CAMERA_FAILED;
      }

      scan_task = null;
    } // if output.equals("Done");


    return output;
  }

  private void logWifiConfiguration(WifiConfiguration wc) {
    // // Log.d(TAG, "add NEW wc:" + wc.networkId + " : " + wc.SSID +
    //  " algo: " + wc.allowedAuthAlgorithms +
    //  " key: " + wc.allowedKeyManagement +
    //  " grp:" + wc.allowedGroupCiphers +
    //  " pair: " + wc.allowedPairwiseCiphers +
    //  " proto:" + wc.allowedProtocols +
    //  " hidden: " + wc.hiddenSSID +
    //  " preSharedKey: " + wc.preSharedKey);
  }

  private String ipAddrToString(int w) {
    return ((w & 0xFF000000) >> 24) + "." +
        ((w & 0x00FF0000) >> 16) + "." +
        ((w & 0x0000FF00) >> 8) + "." +
        (w & 0x000000FF);
  }


  private void setDefaultAlertSettings(String device_address_port) {
    String cmd, response;
    //set motion setting ON
    cmd = String.format("%1$s%2$s%3$s%4$s%5$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.SET_MOTION_AREA_CMD, PublicDefineGlob.MOTION_ON_PARAM);
    response = send_request(cmd, 10000);
    // // Log.d(TAG, "Set motion on res: " + response);

    //set sound settings ON
    cmd = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.VOX_ENABLE);
    response = send_request(cmd, 10000);
    // // Log.d(TAG, "Set sound on res: " + response);

    //set temp high settings ON
    cmd = String.format("%1$s%2$s%3$s%4$s%5$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.SET_TEMP_HI, PublicDefineGlob.SET_TEMP_HI_ON_PARAM);
    response = send_request(cmd, 10000);
    // // Log.d(TAG, "Set temp high on res: " + response);

    //set temp low settings ON
    cmd = String.format("%1$s%2$s%3$s%4$s%5$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, PublicDefineGlob.SET_TEMP_LO, PublicDefineGlob.SET_TEMP_HI_ON_PARAM);
    response = send_request(cmd, 10000);
    // // Log.d(TAG, "Set temp low on res: " + response);
  }


  /**
   * Update host ssid for camera after add successfully
   *
   * @param host_ssid the ssid which camera is added
   */
  private void updateHostSsid(String saved_token, String regId, String host_ssid) {
    try {
      Device.changeBasicInfo(saved_token, regId, null, null, null, null, host_ssid, null);
    } catch (IOException e) {

      // // Log.e(TAG, Log.getStackTraceString(e));
    }
  }


  @Override
  public void updateScanResult(ScanProfile[] results, int status, int index) {
  }


  /* on UI Thread */
  protected void onPreExecute() {
    //		Spanned msg = Html.fromHtml("<big>"+mContext.getString(R.string.AutoConf_working_)+"</big>");
    //
    //		dialog = new ProgressDialog(mContext);
    //		dialog.setMessage(msg);
    //		dialog.setIndeterminate(true);
    //		dialog.setCancelable(false);
    //		dialog.show();

  }

  /* on UI Thread */
  protected void onProgressUpdate(String... progress) {

    Spanned msg = Html.fromHtml("<big>" + progress[0] + "</big>");


    // when the message box "... this may take from 2 to 5 minutes ...",
    // add Cancel button to allow user to choose to cancel the operation.
    if (isCancelable) {
      //			if (dialog != null)
      //			{
      //				dialog.dismiss();
      //			}
      //			dialog = new ProgressDialog(mContext);
      //			dialog.setMessage(msg);
      //			dialog.setIndeterminate(true);
      //			dialog.setCancelable(false);
      //			dialog.setButton(mContext.getString(R.string.Cancel), new DialogInterface.OnClickListener() {
      //
      //				@Override
      //				public void onClick(DialogInterface dialog, int which) {
      //
      //					AutoConfigureCameras.this.cancel(true);
      //				}
      //			});
      //
      //			dialog.show();
    } else if (progress[0].equalsIgnoreCase(mContext.getResources().getString(R.string.camera_does_not_have_ssid_warning))) {
      AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
      msg = Html.fromHtml("<big>" + mContext.getString(R.string.camera_does_not_have_ssid_warning) + "</big>");
      builder.setMessage(msg).setCancelable(true).setPositiveButton(mContext.getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
          isWaiting = false;
          continueAddCamera = true;
        }
      }).setNegativeButton(mContext.getResources().getString(R.string.No), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              isWaiting = false;
              continueAddCamera = false;
            }
          }
      );

      AlertDialog alert = builder.create();
      alert.show();
    } else {
      //dialog.setMessage(msg);
    }


  }


  /* on UI Thread */
  protected void onPostExecute(String result) {
    //		if (dialog != null)
    //		{
    //			dialog.dismiss();
    //		}

    if (result.equalsIgnoreCase("Done")) {
      long add_camera_time = System.currentTimeMillis() - add_camera_start_time;
      mHandler.dispatchMessage(Message.obtain(mHandler, PublicDefine.MSG_AUTO_CONF_SUCCESS));
    } else {
      mHandler.dispatchMessage(Message.obtain(mHandler, PublicDefine.MSG_AUTO_CONF_FAILED, errorCode, errorCode));
    }
  }

  protected void onCancelled() {
    super.onCancelled();
    //dialog.cancel();

    if (scan_task != null) {
      scan_task.stopScan();
      scan_task = null;
    }


    mHandler.sendMessage(Message.obtain(mHandler, PublicDefine.MSG_AUTO_CONF_CANCELED));


  }

  private boolean verify_response(String request_command, String response) {

    if ((response != null) && (response.startsWith(request_command))) {
      String response_code = response.substring(request_command.length());
      if (Integer.parseInt(response_code) == 0) {
        //OK
        return true;
      }
    }

    return false;

  }

  /* handle url connection */
  private String send_request(String request, int timeout) {
    URL url;
    String contentType = null;
    int responseCode = -1;
    HttpURLConnection conn = null;
    DataInputStream _inputStream = null;
    String response_str = null;
    String usr_pass = String.format("%s:%s", http_usr, http_pass);
    try {
      url = new URL(request);
			/* send the request to device by open a connection*/
      conn = (HttpURLConnection) url.openConnection();
      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));
      conn.setConnectTimeout(timeout);
      conn.setReadTimeout(timeout);

    } catch (IOException ioe) {
      // // Log.e(TAG, Log.getStackTraceString(ioe));
    }


    try {

      assert conn != null;
      responseCode = conn != null ? conn.getResponseCode() : 0;
      if (responseCode == HttpURLConnection.HTTP_OK) {
        _inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
        contentType = conn.getContentType();
				/* make sure the return type is text before using readLine */
        response_str = _inputStream.readLine();

        //// // Log.d(TAG,"Get response..: " +response_str );
      }


    } catch (Exception ex) {
      //continue;
      // // Log.e(TAG, Log.getStackTraceString(ex));
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }

    return response_str;
  }


  private void notifyScanResult() {
    synchronized (this) {
      this.notify();
    }
  }


  private void notifySupplicantState(SupplicantState ss) {

    synchronized (supplicantLockObject) {
      sstate = ss;
      supplicantLockObject.notify();
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


  private class WifiReceiver1 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      switch (action) {
        case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
          notifyScanResult();
          break;
        case WifiManager.NETWORK_STATE_CHANGED_ACTION:
          mWifiNetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

          // // Log.d(TAG, this.getClass().getName() + " NETWORK_STATE_CHANGED_ACTION: " + mWifiNetworkInfo.isConnected());

          if ((mWifiNetworkInfo != null) && mWifiNetworkInfo.isConnected()) {
            // // Log.d(TAG, "NOTIFY");

            notifyWifiState(null);

          }
          break;
        case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
          SupplicantState ss = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
          //// // Log.d(TAG, "SUPPLICANT_STATE_CHANGED_ACTION: " + ss);

          if (ss.equals(SupplicantState.COMPLETED) ||
              ss.equals(SupplicantState.INACTIVE) ||
              ss.equals(SupplicantState.DISCONNECTED)) {
            notifySupplicantState(ss);
          }

          break;
        case WifiManager.WIFI_STATE_CHANGED_ACTION:
          mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

          //notifyWifiState();
          break;
        default:
          break;
      }
    }
  }


}
