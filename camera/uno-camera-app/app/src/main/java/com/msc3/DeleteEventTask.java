package com.msc3;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import base.hubble.meapi.Device;
import base.hubble.meapi.JsonResponse;

public class DeleteEventTask extends AsyncTask<String, String, Integer> {

  private static final String TAG = "DeleteEventTask";
  private String _error_desc;

  private static final int UPDATE_SUCCESS = 0x1;
  private static final int UPDATE_FAILED_SERVER_UNREACHABLE = 0x11;
  private static final int UPDATE_FAILED_WITH_DESC = 0x12;

  private Context mContext;
  private IDeleteEventCallBack mCallBack;

  public DeleteEventTask(Context c, IDeleteEventCallBack cb) {
    mContext = c;
    mCallBack = cb;
  }

  @Override
  protected Integer doInBackground(String... params) {

    String apiKey = params[0];
    String deviceID = params[1];
    String eventID = params[2];
    int ret = -1;
    try {
      JsonResponse jRes = Device.deleteEvents(apiKey, deviceID, eventID);
      if (jRes != null && jRes.isSucceed()) {
        if (jRes.getStatus() == HttpURLConnection.HTTP_OK) {
          ret = UPDATE_SUCCESS;
          Log.i("TAG", "Event deletion successfully");
        } else {
          ret = UPDATE_FAILED_WITH_DESC;
          // // Log.d("mbp", "Update camName _error_desc: " + jRes.getStatus());
          _error_desc = jRes.getMessage();
        }
      }
    } catch (MalformedURLException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
      // Connection Timeout - Server unreachable ???
      ret = UPDATE_FAILED_SERVER_UNREACHABLE;
    } catch (SocketTimeoutException se) {
      // Connection Timeout - Server unreachable ???
      ret = UPDATE_FAILED_SERVER_UNREACHABLE;
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
      // Connection Timeout - Server unreachable ???
      ret = UPDATE_FAILED_SERVER_UNREACHABLE;
    }

    return ret;
  }

  /* UI thread */
  protected void onPostExecute(Integer result) {
    if (result == UPDATE_SUCCESS) {
      mCallBack.delete_event_success();
    } else {
      mCallBack.delete_event_failed();
    }
  }
}
