package com.nxcomm.blinkhd.commonasynctask;

import android.os.AsyncTask;

import java.io.IOException;

import base.hubble.IAsyncTaskCommonHandler;
import base.hubble.meapi.Device;
import base.hubble.meapi.device.SendCommandResponse;

public class SendCommandAsyncTask extends AsyncTask<String, Void, SendCommandResponse> {
  private static final String TAG = "SendCommandAsyncTask";
  private IAsyncTaskCommonHandler handler;

  public SendCommandAsyncTask(IAsyncTaskCommonHandler handler) {
    this.handler = handler;
  }

  @Override
  protected SendCommandResponse doInBackground(String... params) {

    SendCommandResponse res = null;
    try {
      res = Device.sendCommand(params[0], params[1], params[2]);
    } catch (IOException e) {

      // // Log.e(TAG, Log.getStackTraceString(e));
    }
    return res;
  }

  @Override
  protected void onPostExecute(SendCommandResponse result) {

    if (handler != null) {
      handler.onPostExecute(result);
    }
    super.onPostExecute(result);
  }

}
