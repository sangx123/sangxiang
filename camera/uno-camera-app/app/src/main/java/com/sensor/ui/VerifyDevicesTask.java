package com.sensor.ui;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import base.hubble.Api;
import base.hubble.Models;

/**
 * Created by hoang on 7/29/15.
 */
public class VerifyDevicesTask extends AsyncTask<String, Void, Void> {
  private static final String TAG = "VerifyDevicesTask";

  public interface IVerifyDevicesCallback {
    void onVerifyDevicesCompleted (Models.ApiResponse<List<Models.VerifyDeviceData>> verifyDeviceResponse);
  }

  private String mApiKey = null;
  private String mRegistrationIds = null;
  private Models.ApiResponse<List<Models.VerifyDeviceData>> mVerifyDeviceResponse = null;
  private IVerifyDevicesCallback mListener = null;

  public VerifyDevicesTask(IVerifyDevicesCallback mListener) {
    this.mListener = mListener;
  }

  @Override
  protected Void doInBackground(String... params) {
    mApiKey = params[0];
    mRegistrationIds = params[1];
    Log.d(TAG, "Verifying device regId: " + mRegistrationIds);
    // mVerifyDeviceResponse = Api.getInstance().getService().verifyDevices(mApiKey, mRegistrationIds);
    // Need to add retries for verify tag query
    Models.ApiResponse<List<Models.VerifyDeviceData>> verifyResponse = null;
    int retries = 3;
    do {
      try {
        verifyResponse = Api.getInstance().getService().verifyDevices(mApiKey, mRegistrationIds);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (verifyResponse != null) {
        break;
      } else {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
      }
    } while (--retries > 0 && !isCancelled() && verifyResponse == null);
    mVerifyDeviceResponse = verifyResponse;
    Log.d(TAG, "Verify device regId: " + mRegistrationIds + " DONE");
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    if (mListener != null) {
      mListener.onVerifyDevicesCompleted(mVerifyDeviceResponse);
    }
  }
}
