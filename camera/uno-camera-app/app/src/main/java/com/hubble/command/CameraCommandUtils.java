package com.hubble.command;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.hubble.HubbleApplication;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.hubble.devcomm.Device;
import com.hubble.devcomm.impl.hubble.IP2pCommunicationHandler;
import com.hubble.devcomm.impl.hubble.P2pCommunicationManager;
import com.hubble.helpers.AsyncPackage;
import com.nxcomm.blinkhd.ui.Global;

import base.hubble.PublicDefineGlob;
import base.hubble.command.BaseCommandRequest;
import base.hubble.command.CameraCommandCallback;
import base.hubble.command.LocalCommandRequest;
import base.hubble.command.PublishCommandRequestBody;
import base.hubble.command.PublishCommandTask;
import base.hubble.command.RemoteCommandRequest;


/**
 * Created by hoang on 3/16/17.
 */

public class CameraCommandUtils {
    private static final String TAG = CameraCommandUtils.class.getSimpleName();

    /**
     * Send command to camera in remote network.
     * This is blocking method, should call it from background thread.
     * @param commandRequest
     * @return
     */
    public static String sendRemoteCommand(RemoteCommandRequest commandRequest) {
        String response = null;
        if (commandRequest != null) {
            if (!TextUtils.isEmpty(commandRequest.getApiKey())) {
                if (!TextUtils.isEmpty(commandRequest.getRegistrationId())) {
                    if (commandRequest.getPublishCommandRequestBody() != null) {
                        PublishCommandTask publishCommandTask = new PublishCommandTask();
                        publishCommandTask.setApiKey(commandRequest.getApiKey());
                        publishCommandTask.setRegistrationId(commandRequest.getRegistrationId());
                        publishCommandTask.setCommandRequestBody(commandRequest.getPublishCommandRequestBody());
                        publishCommandTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        try {
                            response = publishCommandTask.get(commandRequest.getCommandTimeout(), TimeUnit.MILLISECONDS);
                            Log.d(TAG, "Send remote command done, res: " + response);
                        } catch (InterruptedException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        } catch (ExecutionException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        } catch (TimeoutException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    } else {
                        Log.e(TAG, "Send remote command failed, command request body is null");
                    }
                } else {
                    Log.e(TAG, "Send remote command failed, registrationId is null");
                }
            } else {
                Log.e(TAG, "Send remote command failed, apiKey is null");
            }
        } else {
            Log.e(TAG, "Remote command request is null");
        }
        return response;
    }

    /**
     * Send command to camera asynchronously.
     * @param commandRequest
     * @param commandCallback
     */
    public static void sendRemoteCommandAsync(RemoteCommandRequest commandRequest, CameraCommandCallback commandCallback) {
        if (commandRequest != null) {
            if (!TextUtils.isEmpty(commandRequest.getApiKey())) {
                if (!TextUtils.isEmpty(commandRequest.getRegistrationId())) {
                    if (commandRequest.getPublishCommandRequestBody() != null) {
                        PublishCommandTask publishCommandTask = new PublishCommandTask();
                        publishCommandTask.setApiKey(commandRequest.getApiKey());
                        publishCommandTask.setRegistrationId(commandRequest.getRegistrationId());
                        publishCommandTask.setCommandRequestBody(commandRequest.getPublishCommandRequestBody());
                        publishCommandTask.setCommandCallback(commandCallback);
                        publishCommandTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        Log.e(TAG, "Send remote command failed, command request body is null");
                        if (commandCallback != null) {
                            commandCallback.onCommandFailed();
                        }
                    }
                } else {
                    Log.e(TAG, "Send remote command failed, registrationId is null");
                    if (commandCallback != null) {
                        commandCallback.onCommandFailed();
                    }
                }
            } else {
                Log.e(TAG, "Send remote command failed, apiKey is null");
                if (commandCallback != null) {
                    commandCallback.onCommandFailed();
                }
            }
        } else {
            Log.e(TAG, "Remote command request is null");
            if (commandCallback != null) {
                commandCallback.onCommandFailed();
            }
        }
    }

    /**
     * Send command to camera in local and get response.
     * @param commandRequest The LocalCommandRequest object.
     * @return The camera response or null if failed.
     */
    public static String sendLocalCommand(LocalCommandRequest commandRequest) {
        String response = null;
        if (commandRequest != null) {
           /* LocalDevice localDevice = DeviceSingleton.INSTANCE$.getLocalDeviceByIp(commandRequest.getCameraIp());
            if (localDevice != null) {
                response = localDevice.sendCommandAndGetResponse(commandRequest.getFullCommand(), commandRequest.getCommandTimeout());
            } else {*/
                final String deviceAddressPort = commandRequest.getCameraIp() + ":" + commandRequest.getCameraPort();
                if (!TextUtils.isEmpty(commandRequest.getCommand())) {
                    String http_addr = String.format("%1$s%2$s%3$s%4$s", "http://", deviceAddressPort, "/?", commandRequest.getFullCommand());
                    Log.d(TAG, "Send local cmd: " + http_addr);
                    URL url = null;
                    URLConnection conn = null;
                    DataInputStream inputStream = null;
                    String contentType = null;

                    if (!TextUtils.isEmpty(commandRequest.getCameraIp())) {
                        String usr = "";
                        String pwd = "";
                        int timeout = commandRequest.getCommandTimeout(); // Default timeout for local is 5s

                        String usr_pass = String.format("%s:%s", usr, pwd);
                        Log.d(TAG, "Sending local command: " + http_addr);
                        try {
                            url = new URL(http_addr);
                            conn = url.openConnection();

                            conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP));
                            conn.setConnectTimeout(timeout);
                            conn.setReadTimeout(timeout);
                            inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
                            contentType = conn.getContentType();
                                /* Make sure the return type is text before using readLine */
                            if (contentType != null && contentType.equalsIgnoreCase("text/plain")) {
                                response = inputStream.readLine();
                            }

                        } catch (Exception e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        } finally {
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    Log.e(TAG, Log.getStackTraceString(e));
                                }
                            }
                        }

                        Log.d(TAG, "Sending local command res: " + response);
                    } else {
                        Log.d(TAG, "Sending command fail: local ip is null");
                    }
                } else {
                    Log.e(TAG, "Send local cmd failed, command is null");
                }
            //}
        } else {
            Log.e(TAG, "Send local cmd failed, command request is null");
        }
        return response;
    }

    public static String sendP2pCommand(P2pCommandRequest p2pCommandRequest) {
        String response = null;
        if (p2pCommandRequest != null) {
            IP2pCommunicationHandler p2pCommunicationHandler = p2pCommandRequest.getP2pCommunicationHandler();
            if (p2pCommunicationHandler != null) {
                response = p2pCommunicationHandler.sendCommand(p2pCommandRequest.getFullCommand());
                Log.d(TAG, "Sending p2p command res: " + response);
            } else {
                Log.e(TAG, "Send p2p cmd failed, p2p communication handler is null");
            }
        } else {
            Log.e(TAG, "Send p2p cmd failed, command request is null");
        }
        return response;
    }

    public static String sendCommandGetFullResponse(BaseCommandRequest commandRequest) {
        String response = null;
        if (commandRequest instanceof LocalCommandRequest) {
            response = sendLocalCommand((LocalCommandRequest) commandRequest);
        } else if (commandRequest instanceof P2pCommandRequest) {
            response = sendP2pCommand((P2pCommandRequest) commandRequest);
        } else if (commandRequest instanceof RemoteCommandRequest) {
            response = sendRemoteCommand((RemoteCommandRequest) commandRequest);
        }
        return response;
    }

    public static boolean sendCommandGetSuccess(BaseCommandRequest commandRequest) {
        boolean success = false;
        final String response = sendCommandGetFullResponse(commandRequest);
        if (!TextUtils.isEmpty(response) && response.equalsIgnoreCase(commandRequest.getCommand() + ": 0")) {
            success = true;
        }
        return success;
    }

    public static String sendCommandGetStringValue(BaseCommandRequest commandRequest) {
        String responseValue = null;
        final String response = sendCommandGetFullResponse(commandRequest);
        if (!TextUtils.isEmpty(response) && response.startsWith(commandRequest.getCommand())) {
            responseValue = response.replace(commandRequest.getCommand() + ": ", "");
        }
        return responseValue;
    }

    public static int sendCommandGetIntValue(BaseCommandRequest commandRequest) {
        int responseValue = -1;
        final String responseStr = sendCommandGetFullResponse(commandRequest);
        if (!TextUtils.isEmpty(responseStr)) {
            try {
                responseValue = Integer.parseInt(responseStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return responseValue;
    }


    /**
     ***********************************
     * Utility methods
     ***********************************
     */

    public static void sendCommandGetFullResponseAsync(final Device device, final String command, final String value, final String setup, final CameraCommandCallback commandCallback) {
        if (device != null) {
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {
                    final String response = sendCommandGetFullResponse(device, command, value, setup);
                    if (commandCallback != null) {
                        if (response != null) {
                            commandCallback.onCommandSuccess(response);
                        } else {
                            commandCallback.onCommandFailed();
                        }
                    }
                }
            });
        } else {
            if (commandCallback != null) {
                commandCallback.onCommandFailed();
            }
        }
    }

    /**
     *
     * @param device
     * @param command The command without params and prefix ("action=command&command=").
     * @param value The &value= param of command.
     * @param setup The &setup= param of command.
     * @return
     */
    public static String sendCommandGetFullResponse(final Device device, final String command, String value, String setup) {
        String response = null;
        if (device != null) {
            final boolean isInLocal = device.isAvailableLocally();
            if (isInLocal) {
                LocalCommandRequest localCmdReq = new LocalCommandRequest();
                localCmdReq.setCameraIp(device.getProfile().getDeviceLocation().getLocalIp());
                localCmdReq.setCommand(command);
                String cmdParams = null;
                if (setup != null || value != null) {
                    cmdParams = "";
                    if (setup != null) {
                        cmdParams += "&setup=" + setup;
                    }
                    if (value != null) {
                        cmdParams += "&value=" + value;
                    }
                }
                localCmdReq.setCommandParams(cmdParams);
                response = CameraCommandUtils.sendLocalCommand(localCmdReq);
            } else {
                if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
                    P2pCommandRequest p2pCmdReq = new P2pCommandRequest();
                    p2pCmdReq.setP2pCommunicationHandler(P2pCommunicationManager.getInstance().getP2pCommHandler());
                    p2pCmdReq.setCommand(command);
                    String cmdParams = null;
                    if (setup != null || value != null) {
                        cmdParams = "";
                        if (setup != null) {
                            cmdParams += "&setup=" + setup;
                        }
                        if (value != null) {
                            cmdParams += "&value=" + value;
                        }
                    }
                    p2pCmdReq.setCommandParams(cmdParams);
                    response = CameraCommandUtils.sendP2pCommand(p2pCmdReq);
                } else {
                    PublishCommandRequestBody.Builder builder = new PublishCommandRequestBody.Builder();
                    builder.setCommand(command)
                            .setSetup(setup)
                            .setValue(value);
                    PublishCommandRequestBody requestBody = builder.create();
                    RemoteCommandRequest remoteCmdReq = new RemoteCommandRequest();
                    String saved_token = Global.getApiKey(HubbleApplication.AppContext);
                    remoteCmdReq.setApiKey(saved_token);
                    remoteCmdReq.setRegistrationId(device.getProfile().getRegistrationId());
                    remoteCmdReq.setPublishCommandRequestBody(requestBody);
                    response = CameraCommandUtils.sendRemoteCommand(remoteCmdReq);
                }
            }
        } else {
            Log.e(TAG, "sendCommandGetFullResponse failed, device is null");
        }
        return response;
    }

    /**
     *
     * @param device
     * @param command The command without params and prefix ("action=command&command=").
     * @param value The &value= param of command.
     * @param setup The &setup= param of command.
     * @return
     */
    public static String sendCommandGetStringValue(final Device device, final String command, String value, String setup) {
        String responseValue = null;
        final String response = sendCommandGetFullResponse(device, command, value, setup);
        if (response != null && response.startsWith(command)) {
            responseValue = response.replace(command + ": ", "");
        }
        return responseValue;
    }

    /**
     *
     * @param device
     * @param command The command without params and prefix ("action=command&command=").
     * @param value The &value= param of command.
     * @param setup The &setup= param of command.
     * @return
     */
    public static int sendCommandGetIntValue(final Device device, final String command, String value, String setup) {
        int responseValue = -1;
        final String response = sendCommandGetStringValue(device, command, value, setup);
        if (response != null) {
            try {
                responseValue = Integer.parseInt(response);
            } catch (NumberFormatException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return responseValue;
    }

    /**
     *
     * @param device
     * @param command The command without params and prefix ("action=command&command=").
     * @param value The &value= param of command.
     * @param setup The &setup= param of command.
     * @return
     */
    public static boolean sendCommandGetSuccess(final Device device, final String command, String value, String setup) {
        boolean success = false;
        final String response = sendCommandGetFullResponse(device, command, value, setup);
        if (response != null && response.equals(command + ": 0")) {
            success = true;
        }
        return success;
    }

    /**
     *
     * @param device
     * @param command The command without params and prefix ("action=command&command=").
     * @param grid The &grid= param of command.
     * @param zone The &zone= param of command.
     * @return
     */
    public static boolean sendMvrCommandGetSuccess(final Device device, final String command, String grid, String zone) {
        boolean success = false;
        String response = null;
        if (device != null) {
            final boolean isInLocal = device.isAvailableLocally();
            if (isInLocal) {
                LocalCommandRequest localCmdReq = new LocalCommandRequest();
                localCmdReq.setCameraIp(device.getProfile().getDeviceLocation().getLocalIp());
                localCmdReq.setCommand(command);
                String cmdParams = PublicDefineGlob.SET_MOTION_AREA_PARAM_1 + grid + PublicDefineGlob.SET_MOTION_AREA_PARAM_2 + zone;
                localCmdReq.setCommandParams(cmdParams);
                response = CameraCommandUtils.sendLocalCommand(localCmdReq);
            } else {
                if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
                    P2pCommandRequest p2pCmdReq = new P2pCommandRequest();
                    p2pCmdReq.setP2pCommunicationHandler(P2pCommunicationManager.getInstance().getP2pCommHandler());
                    p2pCmdReq.setCommand(command);
                    String cmdParams = PublicDefineGlob.SET_MOTION_AREA_PARAM_1 + grid + PublicDefineGlob.SET_MOTION_AREA_PARAM_2 + zone;
                    p2pCmdReq.setCommandParams(cmdParams);
                    response = CameraCommandUtils.sendP2pCommand(p2pCmdReq);
                } else {
                    PublishCommandRequestBody.Builder builder = new PublishCommandRequestBody.Builder();
                    builder.setCommand(command)
                            .setMvrToggleGrid(grid)
                            .setMvrToggleZone(zone);
                    PublishCommandRequestBody requestBody = builder.create();
                    RemoteCommandRequest remoteCmdReq = new RemoteCommandRequest();
                    String saved_token = Global.getApiKey(HubbleApplication.AppContext);
                    remoteCmdReq.setApiKey(saved_token);
                    remoteCmdReq.setRegistrationId(device.getProfile().getRegistrationId());
                    remoteCmdReq.setPublishCommandRequestBody(requestBody);
                    response = CameraCommandUtils.sendRemoteCommand(remoteCmdReq);
                }
            }

            if (response != null && response.equals(command + ": 0")) {
                success = true;
            }
        } else {
            Log.e(TAG, "sendMvrCommandGetSuccess failed, device is null");
        }

        return success;
    }
}
