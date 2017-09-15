package com.hubble;

import android.util.Log;

import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.impl.cvision.JWebClient;
import com.hubble.framework.service.p2p.P2pDevice;
import com.hubble.framework.service.p2p.P2pUtils;
import com.hubble.tls.LocalDevice;

/**
 * Author: Son Nguyen
 * Email : son.nguyen@hubblehome.com
 * Date: 11:49 AM, 30 Jun 2017
 */

public class LocalCommandService implements P2pUtils.ILocalService {
    private static final String              TAG                 = LocalCommandService.class.getSimpleName();
    private static       LocalCommandService localCommandService = new LocalCommandService();

    public static LocalCommandService getInstance() {
        return localCommandService;
    }

    private LocalCommandService() {

    }

    @Override
    public boolean isInLocal(P2pDevice p2pDevice) {
        /*LocalDevice localDevice = DeviceSingleton.getInstance().getLocalDeviceByRegId(p2pDevice.getRegistrationId());
        if (localDevice != null) {
            String mac = localDevice.sendCommandAndGetValue("action=command&command=get_mac_address",5000);
            Log.i(TAG, "P2p mac address " + p2pDevice.getMacAddress() + ", local device response " + mac);
            if (mac != null) mac = mac.replace(":", "");
            return p2pDevice.getMacAddress().equalsIgnoreCase(mac);
        } else {
            Log.e(TAG, "Local device is null");
        }
        return false;*/
        boolean isEqual = false;
        String storedMACAddress = p2pDevice.getMacAddress();
        String macAddress = "";
        if (p2pDevice != null) {
            // do not use this, device.sendCommandGetValue can be send via remote server
            //final Pair<String, Object> response = device.sendCommandGetValue("get_mac_address", null, null);

            //if (p2pDevice.getProfile().deviceLocation != null) {
                LocalDevice localDevice = DeviceSingleton.getInstance().getLocalDeviceByRegId(p2pDevice.getRegistrationId());
                String response = null;
                if(localDevice != null) {
                    response = localDevice.sendCommandAndGetValue("action=command&command=get_mac_address",5000);
                }else{
                    response = JWebClient.downloadAsStringWithoutEx(String
                            .format("http://%s/?action=command&command=get_mac_address", p2pDevice
                                    .getLocalIp()));
                }
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
        //}

        isEqual = storedMACAddress.equals(macAddress);
        if (!isEqual) {
            storedMACAddress = storedMACAddress.replace(":", "");
            isEqual = storedMACAddress.equals(macAddress);
        }

        return isEqual;
    }

    @Override
    public String sendLocalCommand(String camIp, String cmd) {
        LocalDevice localDevice = DeviceSingleton.getInstance().getLocalDeviceByIp(camIp);
        if(localDevice != null) {
            return localDevice.sendCommandAndGetResponse(cmd, 5000);
        }
        return null;
    }
}
