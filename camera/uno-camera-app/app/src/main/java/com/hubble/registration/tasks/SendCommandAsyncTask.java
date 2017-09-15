package com.hubble.registration.tasks;

import android.os.AsyncTask;

import java.io.IOException;

import base.hubble.IAsyncTaskCommonHandler;
import base.hubble.meapi.Device;
import base.hubble.meapi.device.SendCommandResponse;
import base.hubble.meapi.device.SendCommandResponse.DeviceCmdResponse;
import base.hubble.meapi.device.SendCommandResponse.SendCommandResponseData;

public class SendCommandAsyncTask extends AsyncTask<String, Void, SendCommandResponse> {
  private static final String TAG = "SendCommandAsyncTask";
  private IAsyncTaskCommonHandler handler;

  public SendCommandAsyncTask(IAsyncTaskCommonHandler handler) {
    this.handler = handler;
  }

  @Override
  protected SendCommandResponse doInBackground(String... params) {

    SendCommandResponse res = null;
    if (params.length >= 3) {
      try {
        // // Log.d(TAG, "Send cmd via stun: " + params[2]);
        res = Device.sendCommand(params[0], params[1], params[2]);
        if (res != null) {
          SendCommandResponseData data = res.getSendCommandResponseData();
          if (data != null) {
            DeviceCmdResponse cmd_res = data.getDevice_response();
            if (cmd_res != null) {
              //String res_body = cmd_res.getBody();
              // // Log.d(TAG, "Send cmd res: " + res_body);
            } else {
              // // Log.d(TAG, "Send cmd " + params[2] + ", device res: null");
            }
          } else {
            // // Log.d(TAG, "Send cmd " + params[2] + ", data res: null");
          }
        } else {
          // // Log.d(TAG, "Send cmd " + params[2] + ", res: null");
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return res;
  }

  @Override
  protected void onPostExecute(SendCommandResponse result) {
    // TODO Auto-generated method stub
    if (handler != null) {
      handler.onPostExecute(result);
    }
    super.onPostExecute(result);
  }

}
