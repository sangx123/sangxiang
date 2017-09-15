package com.hubble.util;

import android.text.TextUtils;
import android.util.Log;

import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubble.registration.tasks.comm.UDTRequestSendRecvTask;
import com.nxcomm.blinkhd.ui.Global;

import base.hubble.PublicDefineGlob;
import com.hubble.devcomm.impl.hubble.P2pCommunicationManager;

/**
 * Created by hoang on 11/5/15.
 */

/**
 * Don't use this class.
 * Please use CameraCommandUtils for sending command.
 * This class is only used for backward compatible purpose.
 */
public class CommandUtils {
  private static final String TAG = "CommandUtils";
  private static final String http_pass = String.format("%s:%s", PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, PublicDefineGlob.DEFAULT_CAM_PWD);

  /**
   * Sending command to camera. This is a blocking method, so app need to call it on background thread.
   * @param device Current camera device.
   * @param command The command without "action=command&command=" prefix. E.g. get_mac_address
   * @param isInLocal true for local command, false for remote command.
   * @return The response from camera, otherwise null.
   */
  @Deprecated
  public static String sendCommand(Device device, String command, boolean isInLocal) {
    Log.i(TAG, "Sending command: " + command + ", isInLocal? " + isInLocal);
    String response = null;
    if (device != null && device.getProfile() != null) {
      if (isInLocal) {
        if (device.getProfile().getDeviceLocation() != null &&
            device.getProfile().getDeviceLocation().getLocalIp() != null) {
          final String deviceIp = device.getProfile().getDeviceLocation().getLocalIp();
          String localPort = device.getProfile().getDeviceLocation().getLocalPort1();
          if (localPort == null || localPort.isEmpty()) {
            localPort = "80";
          }
          response = sendLocalCommand(deviceIp, localPort, command);
        } else {
          Log.i(TAG, "Send command failed, device location is null");
        }
      } else {
        String request = PublicDefineGlob.BM_HTTP_CMD_PART + command;
        response = sendRemoteCommand(device.getProfile().getRegistrationId(), request);
      }
    } else {
      Log.e(TAG, "Send command failed, device is null");
    }
    Log.i(TAG, "Send command: " + command + " DONE, response: " + response);
    return response;
  }

  @Deprecated
  public static String sendCommand(Device device, String command, boolean isInLocal, boolean useP2pCommand) {
    Log.i(TAG, "Sending command: " + command + ", isInLocal? " + isInLocal);
    String response = null;
    if (device != null && device.getProfile() != null) {
      if (isInLocal) {
        if (device.getProfile().getDeviceLocation() != null &&
                device.getProfile().getDeviceLocation().getLocalIp() != null) {
          final String deviceIp = device.getProfile().getDeviceLocation().getLocalIp();
          String localPort = device.getProfile().getDeviceLocation().getLocalPort1();
          if (localPort == null || localPort.isEmpty()) {
            localPort = "80";
          }
          response = sendLocalCommand(deviceIp, localPort, command);
        } else {
          Log.i(TAG, "Send command failed, device location is null");
        }
      } else {
        String request = PublicDefineGlob.BM_HTTP_CMD_PART + command;
        response = sendRemoteCommand(device.getProfile().getRegistrationId(), request, useP2pCommand);
      }
    } else {
      Log.e(TAG, "Send command failed, device is null");
    }
    Log.i(TAG, "Send command: " + command + " DONE, response: " + response);
    return response;
  }

  @Deprecated
  public static String sendLocalCommand(String deviceIp, String port, String command) {
    String response = null;
    final String deviceAddressPort = deviceIp + ":" + port;
    if (!TextUtils.isEmpty(command)) {
      String http_addr = String.format("%1$s%2$s%3$s%4$s", "http://", deviceAddressPort, PublicDefineGlob.HTTP_CMD_PART, command);
      Log.d(TAG, "Request: " + http_addr);
      response = HTTPRequestSendRecvTask.sendRequest_block_for_response_with_timeout(http_addr, PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, http_pass);
    } else {
      Log.e(TAG, "Send command failed, command is null");
    }
    return response;
  }

  @Deprecated
  public static String sendRemoteCommand(String registrationId, String command) {
    return sendRemoteCommand(registrationId, command, false);
  }

  @Deprecated
  public static String sendRemoteCommand(String registrationId, String command, boolean useP2pCommand) {
    String response = null;
    if (useP2pCommand) {
      if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
        // Send command via p2p
        response = P2pCommunicationManager.getInstance().sendCommand(command);
      } else {
        // Send command via stun
        if (registrationId != null) {
          String saved_token = Global.getApiKey(HubbleApplication.AppContext);
          if (saved_token != null) {
            response = UDTRequestSendRecvTask.sendRequest_via_stun2(saved_token, registrationId, command);
          } else {
            Log.e(TAG, "Send cmd failed, user token is null");
          }
        } else {
          Log.e(TAG, "Send cmd failed, device registration is null");
        }
      }
    } else {
      // Don't use P2P command, send command via stun
      if (registrationId != null) {
        String saved_token = Global.getApiKey(HubbleApplication.AppContext);
        if (saved_token != null) {
          response = UDTRequestSendRecvTask.sendRequest_via_stun2(saved_token, registrationId, command);
        } else {
          Log.e(TAG, "Send cmd failed, user token is null");
        }
      } else {
        Log.e(TAG, "Send cmd failed, device registration is null");
      }
    }
    return response;
  }

  /**
   * Sending command to camera. This is a non-blocking method, so app can call it on main thread.
   * @param device Current camera device.
   * @param command The command without "action=command&command=" prefix. E.g. get_mac_address
   * @param isInLocal true for local command, false for remote command.
   * @return The response from camera, otherwise null.
   */
  @Deprecated
  public static void sendCommandAsyncNoCallback(final Device device, final String command, final boolean isInLocal) {
    Runnable runn = new Runnable() {
      @Override
      public void run() {
        sendCommand(device, command, isInLocal);
      }
    };
    Thread worker = new Thread(runn);
    worker.start();
  }
}
