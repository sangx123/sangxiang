// Copyright Hubble Communications Inc.
package com.hubble.registration.models;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.hubble.devcomm.Device;

import com.hubble.devcomm.impl.cvision.JWebClient;

/**
 * Created by brennan on 15-04-08.
 */
public class CameraAvailabilityManager {
  private static final String TAG = CameraAvailabilityManager.class.getSimpleName();
  private static CameraAvailabilityManager mInstance;

  public static CameraAvailabilityManager getInstance() {
    if (mInstance == null) {
      mInstance = new CameraAvailabilityManager();
    }

    return mInstance;
  }

  private CameraAvailabilityManager() {
  }

  public boolean isMACAddressSame(Device device) {
    boolean isEqual = false;
    String storedMACAddress = device.getProfile().getMacAddress();
    String macAddress = "";
    if (device != null) {
      //final Pair<String, Object> response = device.sendCommandGetValue("get_mac_address", null, null);
      if (device.getProfile().deviceLocation != null) {
        String response = JWebClient.downloadAsStringWithoutEx(String.format("http://%s:80/?action=command&command=get_mac_address", device.getProfile().deviceLocation.localIP));
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

  public boolean isLocal(Context context, Device device) {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    String androidHostSSID = wifiManager.getConnectionInfo().getSSID().replaceAll("\"", "");
    String deviceHostSSID = device.getProfile().getHostSSID();
    boolean isLocal = false;

    if (androidHostSSID.equals(deviceHostSSID) || androidHostSSID.equals(deviceHostSSID + "-5G")) {
      isLocal = true;
    }

    return isLocal;
  }

    /*
  public boolean isCameraInSameNetwork(Context context, Device device) {
    // 1. Quick checking, filter by camera IP first
    boolean isInSameSubnet = false;
    isInSameSubnet = isInSameSubnet(context, device);

    // 2. If phone and camera are in same subnet, send get_mac_address command then check the response
    boolean isMACEqual = false;
    if (isInSameSubnet == true) {
      isMACEqual = isMACAddressSame(device);
    }

    Log.d("mbp", "modelsCameraAvailabilityManager, isCameraInSameNetwork? " + isMACEqual);
    return isMACEqual;
  }
*/

  /**
   * Check whether app ip is in same subnet with camera.
   * @param context The context
   * @param device The camera device that need to check with app.
   * @return true if they are in same subnet, otherwise false.

  public boolean isInSameSubnet(Context context, Device device) {
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
   */




}
