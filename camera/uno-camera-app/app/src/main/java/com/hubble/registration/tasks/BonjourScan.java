package com.hubble.registration.tasks;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.hubble.registration.models.CameraBonjourInfo;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubble.util.LogZ;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import base.hubble.PublicDefineGlob;

public class BonjourScan implements Runnable {
  private static final String TAG = "BonjourScan";
  private JmDNS mdnsService;

  private String serviceType = "_camera._tcp.local.";
  private List<CameraBonjourInfo> listCameraBonjourInfo = new ArrayList<>();
  private static final String ipPropStr = "ip";
  private static final String macPropStr = "mac";
  private static final String ssidPropStr = "ssid";
  private IBonjourScanCompleted onBonjourScanCompletedHandler = null;
  private Context mContext;
  private boolean discoverMode = false;
  private boolean isCancelled = false;

  public interface IBonjourScanCompleted {
    void onBonjourScanCompleted (List<CameraBonjourInfo> info);
    void onBonjourScanCancelled ();
  }

  @Deprecated
  public BonjourScan(IBonjourScanCompleted handler, Context mContext) {
    this.onBonjourScanCompletedHandler = handler;
    this.mContext = mContext;
  }

  public BonjourScan(Context context, IBonjourScanCompleted callback, boolean discoveryMode) {
    this.onBonjourScanCompletedHandler = callback;
    this.mContext = context;
    this.discoverMode = discoveryMode;
  }

  public InetAddress getLocalIpAddress() throws Exception {
    WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    WifiInfo wifiinfo = wm.getConnectionInfo();
    int intaddr = wifiinfo.getIpAddress();

    byte[] byteaddr = new byte[]{(byte) (intaddr & 0xff), (byte) (intaddr >> 8 & 0xff), (byte) (intaddr >> 16 & 0xff), (byte) (intaddr >> 24 & 0xff)};
    InetAddress addr = InetAddress.getByAddress(byteaddr);

    return addr;
  }

  @Override
  public void run() {
    isCancelled = false;
    try {
      InetAddress inetAddress = getLocalIpAddress();
      // prevent scan when do not have network interface
      if (!inetAddress.toString().equalsIgnoreCase("0.0.0.0")) {
        LogZ.i("Scan on inet address: %s", inetAddress.toString());
        mdnsService = JmDNS.create(inetAddress);
        mdnsService.addServiceListener(serviceType, mdnsServiceListener);
        ServiceInfo[] infos = mdnsService.list(serviceType);
        for (ServiceInfo info : infos) {
          String ssid = info.getPropertyString(ssidPropStr);
          String cam_ip = info.getPropertyString(ipPropStr);
          String cam_mac_address = info.getPropertyString(macPropStr);
          int cam_port = info.getPort();
          Log.d(TAG, String.format("Camera info: ssid %s, cam_ip %s, cam_port %d, mac %s", ssid, cam_ip, cam_port, cam_mac_address));
          if (ssid != null) {
            for (String cameraSSID : PublicDefineGlob.CAMERA_SSID_LIST) {
              if (ssid.startsWith(cameraSSID)) {
            /* Verify whether Bonjour record is valid or not by get_mac_address. */
                if (cam_ip != null && !cam_ip.equalsIgnoreCase("null") && !cam_ip.equalsIgnoreCase("")) {
                  String response = HTTPRequestSendRecvTask.getMacAddress(cam_ip, String.valueOf(cam_port), "", "");
                  Log.d(TAG, "Bonjour scan, mac from record: " + cam_mac_address + ", get mac res: " + response);
                  if (response != null && response.equalsIgnoreCase(cam_mac_address)) {
                    CameraBonjourInfo camInfo = new CameraBonjourInfo(
                        info.getName(), info.getAddress(),
                        info.getPropertyString(ipPropStr),
                        info.getPropertyString(macPropStr));

                    // Log.i(TAG, "Camera info: " + camInfo);
                    camInfo.setCameraName(ssid);
                    listCameraBonjourInfo.add(camInfo);
                  }
                }
                break;
              }
            }
          } else if (discoverMode) {
            CameraBonjourInfo camInfo = new CameraBonjourInfo(
                info.getName(), info.getAddress(),
                info.getPropertyString(ipPropStr),
                info.getPropertyString(macPropStr));

            // Log.i(TAG, "Camera info: " + camInfo);
            listCameraBonjourInfo.add(camInfo);
          }

          if (isCancelled) {
            break;
          }
        }

        mdnsService.removeServiceListener(serviceType, mdnsServiceListener);
      } else {
        Log.i(TAG, "Inet address is 0.0.0.0, we do not need to scan.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (mdnsService != null) {
        try {
          mdnsService.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    if (!isCancelled) {
      if (onBonjourScanCompletedHandler != null) {
        onBonjourScanCompletedHandler.onBonjourScanCompleted(listCameraBonjourInfo);
      }
    } else {
      if (onBonjourScanCompletedHandler != null) {
        onBonjourScanCompletedHandler.onBonjourScanCancelled();
      }
    }
  }

  public void cancel() {
    isCancelled = true;
  }

  public IBonjourScanCompleted getOnBonjourScanCompletedHandler() {
    return onBonjourScanCompletedHandler;
  }

  public void setOnBonjourScanCompletedHandler(IBonjourScanCompleted onBonjourScanCompletedHandler) {
    this.onBonjourScanCompletedHandler = onBonjourScanCompletedHandler;
  }

  private ServiceListener mdnsServiceListener = new ServiceListener() {

    public void serviceAdded(ServiceEvent serviceEvent) {
      mdnsService.requestServiceInfo(serviceType, serviceEvent.getName());
    }

    public void serviceRemoved(ServiceEvent serviceEvent) {
    }

    public void serviceResolved(ServiceEvent serviceEvent) {
      String serviceUrl = serviceEvent.getInfo().getURL();
    }
  };

}
