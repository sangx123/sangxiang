package com.hubble.registration.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.hubble.HubbleApplication;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.UserAccount;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.devices.SerializableDeviceProfile;
import base.hubble.meapi.Device;
import base.hubble.meapi.JsonResponse;
import com.hubble.registration.interfaces.IChangeNameCallBack;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import base.hubble.meapi.Device;
import base.hubble.meapi.JsonResponse;

public class ChangeNameTask extends AsyncTask<String, String, Integer> {

  private static final String TAG = "ChangeNameTask";
  private String _error_desc;

  private static final int UPDATE_SUCCESS = 0x1;
  private static final int UPDATE_FAILED_SERVER_UNREACHABLE = 0x11;
  private static final int UPDATE_FAILED_WITH_DESC = 0x12;

  private Context mContext;
  private IChangeNameCallBack mCallBack;

  public ChangeNameTask(Context c, IChangeNameCallBack cb) {
    mContext = c;
    mCallBack = cb;
  }

  @Override
  protected Integer doInBackground(String... params) {

    String usrToken = params[0];
    String encode_name = params[1];

    String regId = params[2];

    int ret = -1;
    Log.d(TAG, "Changing device name to: " + encode_name);
    try {
      Models.UpdateDeviceInfoRequest updateCameraNameReq = new Models.UpdateDeviceInfoRequest();
      updateCameraNameReq.setApiKey(usrToken);
      updateCameraNameReq.setName(encode_name);
      Models.ApiResponse<SerializableDeviceProfile> updateCameraNameRes = Api.getInstance().getService().updateDeviceInfo(
              regId, updateCameraNameReq);
      if (updateCameraNameRes != null) {
        String status = updateCameraNameRes.getStatus();
        Log.d(TAG, "Changing device name res code: " + status);
        if (status != null && status.equals(String.valueOf(HttpURLConnection.HTTP_OK))) {
          Log.d(TAG, "Changing device name succeeded");
          ret = UPDATE_SUCCESS;
          String saved_token = HubbleApplication.AppConfig.getString(PublicDefine.PREFS_SAVED_PORTAL_TOKEN, null);
          UserAccount online_user;
          try {
            online_user = new UserAccount(saved_token, mContext.getExternalFilesDir(null), null, mContext);
            online_user.sync_user_data();
          } catch (Exception e1) {
            // // Log.e(TAG, Log.getStackTraceString(e1));
            Log.d(TAG, "Update device name error");
          }
        } else {
          ret = UPDATE_FAILED_WITH_DESC;
          _error_desc = updateCameraNameRes.getMessage();
          Log.d(TAG, "Update device name error msg: " + _error_desc);
        }
      } else {
        Log.d(TAG, "Update device name res null");
      }
    } catch (Exception e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
      //Connection Timeout - Server unreachable ???
      Log.e(TAG, Log.getStackTraceString(e));
      //Connection Timeout - Server unreachable ???
      ret = UPDATE_FAILED_SERVER_UNREACHABLE;
    }
    Log.d(TAG, "Changing device name DONE");
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
