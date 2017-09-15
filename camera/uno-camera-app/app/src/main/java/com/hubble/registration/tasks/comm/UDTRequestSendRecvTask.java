package com.hubble.registration.tasks.comm;

import android.net.Uri;
import android.os.AsyncTask;

import com.hubble.registration.PublicDefine;
import com.hubble.registration.models.BabyMonitorAuthentication;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

import base.hubble.meapi.Device;
import base.hubble.meapi.device.SendCommandResponse;
import base.hubble.meapi.device.SendCommandResponse.DeviceCmdResponse;
import base.hubble.meapi.device.SendCommandResponse.SendCommandResponseData;

/**
 * DONT USE!
 * <p/>
 * Use com.hubble.devicecommunication.Device sendCommandGet* instead
 */
@Deprecated
public class UDTRequestSendRecvTask extends AsyncTask<String, Integer, String> {

  private static final String TAG = "UDTRequestSendRecvTask";
  //protected  SocketUDT udtSock ;
  protected InetSocketAddress device_addr;
  protected int localPort;
  protected DataInputStream _inputStream;
  protected BabyMonitorAuthentication bm;


  /**
   * ********* STATIC METHODS **************************
   */

  public static String sendRequest_via_stun2(String... urls) {
    String userToken = null;
    String regId = null;
    String command = null;

    userToken = urls[0];
    regId = urls[1];
    command = urls[2];

    // URL-Encoded
    //command =  Uri.encode(command);

    String response = null;

    try {
      SendCommandResponse send_cmd_res = Device.sendCommand(userToken, regId, command);
      if (send_cmd_res != null) {
        int status_code = send_cmd_res.getStatus();
        if (status_code == HttpURLConnection.HTTP_OK) {
          SendCommandResponseData res_data = send_cmd_res.getSendCommandResponseData();
          if (res_data != null) {
            DeviceCmdResponse dev_cmd_res = res_data.getDevice_response();
            if (dev_cmd_res != null) {
              response = dev_cmd_res.getBody();
            }
          }
        }
      }
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }


    return response;
  }


  /**
   * Use for send some stun cmd: close_session, close_relay_session, close_relay_session2...
   *
   * @param urls mac, channelId, command, userName, userPass
   * @return
   */
  public static String send_stun_command(String... urls) {

    String command = null, mac = null, http_userName = null, http_userPass = null;
    String channelId = null;

    mac = urls[0];
    channelId = urls[1];
    command = urls[2];
    http_userName = urls[3];
    http_userPass = urls[4];

    command = "action=" + command;
    // URL-Encoded
    command = Uri.encode(command);
    if (mac.contains(":")) {
      mac = PublicDefine.strip_colon_from_mac(mac);
    }

    String http_addr = String.format("%1$s%2$s%3$s%4$s%5$s", PublicDefine.BM_SERVER + PublicDefine.BM_HTTP_CMD_PART, PublicDefine.SEND_CTRL_CMD, PublicDefine.SEND_CTRL_PARAM_1 + mac, PublicDefine.SEND_CTRL_PARAM_2 + channelId, PublicDefine.SEND_CTRL_PARAM_3 + command);


    // // Log.d("mbp", "stun cmd: " + http_addr);

    String response = HTTPRequestSendRecvTask.sendRequest_block_for_response_1(http_addr, http_userName, http_userPass);

    return response;
  }

  /************ end of  STATIC METHODS ***************************/


  /**
   * ********** Asyn Taks methods ****************************
   */

	/* background thread: post the type of request here 
   * for e.g.: "action=command&command=brightness_plus"
	 * */
  protected String doInBackground(String... urls) {
    String request = null;
    String response = null;
    byte[] request_bytes = null;

    if (device_addr == null) {
      // // Log.d("mbp", "device_addr: = null");
      return null;
    }

    return response;
  }

  /* on UI Thread */
  protected void onProgressUpdate(Integer... progress) {
  }


  /* on UI Thread */
  protected void onPostExecute(String result) {
    // // Log.d("mbp", "UDT:response: >" + result + "<");
    //TODO: parse result
  }


}
