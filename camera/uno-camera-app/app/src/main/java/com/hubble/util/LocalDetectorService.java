package com.hubble.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.koushikdutta.async.future.FutureCallback;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.hubble.devcomm.impl.cvision.JWebClient;

/**
 * Created by songn_000 on 05/10/2015.
 */
public class LocalDetectorService {

  private static LocalDetectorService mInstance = new LocalDetectorService();

  private LocalDetectorService() {
  }

  public static LocalDetectorService getService() {
    return mInstance;
  }

  /**
   * Check an camera is in local network or not
   *
   * @param device   Device
   * @param callback FutureCallback<Boolean> callback
   */
  public void isLocalCamera(final Device device, final FutureCallback<Boolean> callback) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        // 1. Quick checking, filter by camera IP first
        boolean isInSameSubnet = false;
        isInSameSubnet = isInSameSubnet(device);
        // 2. If phone and camera are in same subnet, send get_mac_address command then check the response
        boolean isMACEqual = false;
        if (isInSameSubnet == true) {
          isMACEqual = isMACAddressSame(device);
        }
        Log.d("LocalDetectorService", "camera  " + device.getProfile().getName() + " is available in local: " + isMACEqual);
        if (callback != null) {
          callback.onCompleted(null, isMACEqual);
        }
      }
    }).start();
  }

  private boolean isMACAddressSame(Device device) {
    boolean isEqual = false;
    String storedMACAddress = device.getProfile().getMacAddress();
    String macAddress = "";
    if (device != null) {
      if (device.getProfile().deviceLocation != null) {
        String deviceLocalIp = device.getProfile().getDeviceLocation().getLocalIp();
        String response = JWebClient.downloadAsStringWithoutEx(String.format("http://%s/?action=command&command=get_mac_address", deviceLocalIp));
        if (response != null) {
          // NOTE: get_mac_address response could have no "get_mac_address" prefix.
          // App should handle both case.
          if (response.startsWith("get_mac_address: ")) {
            macAddress = response.replace("get_mac_address: ", "");
          } else {
            macAddress = response;
          }
        }
      }
    }
    isEqual = storedMACAddress.equals(macAddress);
    if (!isEqual) {
      storedMACAddress = storedMACAddress.replace(":", "");
      isEqual = storedMACAddress.equals(macAddress);
    }
    return isEqual;
  }

  private boolean isInSameSubnet(Device device) {
    Context context = HubbleApplication.AppContext;
    boolean isInSameSubnet = false;
    String localIp = device.getProfile().getDeviceLocation().getLocalIp();
    if (localIp != null) {
      WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      DhcpInfo dhcp = wifi.getDhcpInfo();
      int dhcpNetworkAddrInt = (dhcp.ipAddress & dhcp.netmask);

      InetAddress camAddress = null;
      try {
        camAddress = InetAddress.getByName(localIp);
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
      byte[] camAddrArr = camAddress.getAddress();
      int camAddrInt = 0;
      for (int k = 0; k < camAddrArr.length; k++) {
        camAddrInt = camAddrInt | ((camAddrArr[k] & 0xFF) << k * 8);
      }

      int camNetworkAddrInt = (camAddrInt & dhcp.netmask);
      if (dhcpNetworkAddrInt == camNetworkAddrInt) {
        // camera address is in the same subnet with app address
        isInSameSubnet = true;
      }
    }
    return isInSameSubnet;
  }
}
