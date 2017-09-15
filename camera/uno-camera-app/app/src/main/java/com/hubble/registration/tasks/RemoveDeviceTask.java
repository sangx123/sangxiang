package com.hubble.registration.tasks;

import android.content.Context;
import android.os.AsyncTask;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.registration.Util;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import base.hubble.meapi.Device;
import base.hubble.meapi.SimpleJsonResponse;

/**
 * Created by Sean on 14-10-27.
 */

/**
 * Remove device from Account.
 * Use onDeleteTaskCompleted interface for onPostExecute.
 * <p/>
 * Param[0] = Device RegistrationId
 * Param[1] = User Auth Token
 */
public class RemoveDeviceTask extends AsyncTask<String, String, Integer> {
  private static final String TAG = "RemoveCameraTask";
  public static final int REMOVE_CAM_SUCCESS = 0x1;
  public static final int REMOVE_CAM_FAILED_UNKNOWN = 0x2;
  public static final int REMOVE_CAM_FAILED_SERVER_DOWN = 0x11;
  private String usrToken;
  private String regId;
  private onDeleteTaskCompleted mListener;
    private Context mContext;
    int ret = -1;
  public interface onDeleteTaskCompleted {
    void onDeleteTaskCompleted(int result);
  }

  public RemoveDeviceTask(Context mContext, onDeleteTaskCompleted listener) {
      this.mContext = mContext;
    mListener = listener;
  }

  public RemoveDeviceTask() {
  }

  @Override
  protected Integer doInBackground(String... params) {
    regId = params[0];
    usrToken = params[1];
   ret = -1;
    try {
      SimpleJsonResponse del_res = Device.delete(usrToken, regId);
      if (del_res != null && del_res.isSucceed()) {
          int responseCode = del_res.getStatus();
          if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
              ret = REMOVE_CAM_FAILED_UNKNOWN;
          } else if (responseCode == HttpURLConnection.HTTP_OK) {
              ret = REMOVE_CAM_SUCCESS;
              Util.deleteLatestPreview(regId);
              Util.removeDashBoardEventsFromSP(mContext,regId);
              CommonUtil.removeSettingsInfo(mContext, regId+ "-" + SettingsPrefUtils.FETCH_SETTINGS);

              DeviceSingleton.getInstance().remove(regId); // clean up local state
          }
      }
    } catch (MalformedURLException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    } catch (SocketTimeoutException se) {
      // Connection Timeout - Server unreachable ???
      ret = REMOVE_CAM_FAILED_SERVER_DOWN;
    } catch (IOException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }

    return ret;
  }

  protected void onPostExecute(Integer result) {
      if (mListener != null) {
          mListener.onDeleteTaskCompleted(result);
      }
  }
}