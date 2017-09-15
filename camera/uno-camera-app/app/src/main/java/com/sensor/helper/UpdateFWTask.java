package com.sensor.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hubble.HubbleApplication;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.UserAccount;
import com.hubble.registration.interfaces.IChangeNameCallBack;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import base.hubble.meapi.Device;
import base.hubble.meapi.JsonResponse;

public class UpdateFWTask extends AsyncTask<String, String, Integer> {

  private static final String TAG = "UpdateFWTask";
  private String _error_desc;

  private static final int UPDATE_SUCCESS = 0x1;
  private static final int UPDATE_FAILED_SERVER_UNREACHABLE = 0x11;
  private static final int UPDATE_FAILED_WITH_DESC = 0x12;

  private Context mContext;
  private IChangeNameCallBack mCallBack;

  public UpdateFWTask(Context c, IChangeNameCallBack cb) {
    mContext = c;
    mCallBack = cb;
  }

  @Override
  protected Integer doInBackground(String... params) {

    String usrToken = params[0];
    String versionFw = params[1];

    String regId = params[2];

    int ret = -1;
    Log.d(TAG, "Changing device FW to: " + versionFw);
    try {
      JsonResponse jRes = Device.changeVersionFW(usrToken, regId, versionFw);
      if (jRes != null && jRes.isSucceed()) {
        if (jRes.getStatus() == HttpURLConnection.HTTP_OK) {
          ret = UPDATE_SUCCESS;
          String saved_token = HubbleApplication.AppConfig.getString(PublicDefine.PREFS_SAVED_PORTAL_TOKEN, null);
          UserAccount online_user;
          try {
            online_user = new UserAccount(saved_token, mContext.getExternalFilesDir(null), null, mContext);
            online_user.sync_user_data();
          } catch (Exception e1) {
            // // Log.e(TAG, Log.getStackTraceString(e1));
            Log.d(TAG, "Update device FW error");
          }
        } else {
          ret = UPDATE_FAILED_WITH_DESC;
          _error_desc = jRes.getMessage();
          Log.d(TAG, "Update device FW error msg: " + _error_desc);
        }
      } else {
        Log.d(TAG, "Update device FW res null");
      }
    } catch (MalformedURLException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
      //Connection Timeout - Server unreachable ???
      ret = UPDATE_FAILED_SERVER_UNREACHABLE;
    } catch (SocketTimeoutException se) {
      //Connection Timeout - Server unreachable ???
      Log.d(TAG, "Update device FW socket timeout exception");
      ret = UPDATE_FAILED_SERVER_UNREACHABLE;
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
      //Connection Timeout - Server unreachable ???
      Log.d(TAG, "Update device FW IO exception");
      ret = UPDATE_FAILED_SERVER_UNREACHABLE;
    }
    Log.d(TAG, "Changing device FW DONE");

    return ret;
  }

  /* UI thread */
  protected void onPostExecute(Integer result) {
    if (result == UPDATE_SUCCESS) {
      mCallBack.update_cam_success();
    } else {
      mCallBack.update_cam_failed();
    }
  }
}
