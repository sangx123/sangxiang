package com.nxcomm.blinkhd.ui.dialog;

import android.content.Context;
import android.util.Log;

import com.nxcomm.blinkhd.ui.Global;

import base.hubble.Api;
import base.hubble.Models;
import retrofit.RetrofitError;

/**
 * Created by BinhNguyen on 11/11/2015.
 */
public class TurnScheduleTask implements Runnable {

  private Context mContext;
  private Models.DeviceScheduleSubmit mSubmitData;
  private TurnScheduleListener mListener;

  public TurnScheduleTask(Context context, Models.DeviceScheduleSubmit submitData, TurnScheduleListener listener) {
    mContext = context;
    mSubmitData = submitData;
    mListener = listener;
  }

  @Override
  public void run() {
    String apiKey = Global.getApiKey(mContext);
    try {
      Models.ApiResponse<String> res = Api.getInstance().getService().submitDeviceSchedule(apiKey, mSubmitData);
      if (mListener != null) {
        mListener.onComplete(res);
      }
    }catch(RetrofitError e){
       Log.e("TurnScheduleTask","Retrofit error while submitDeviceSchedule");
      mListener.onComplete(null);
    }
  }

  public interface TurnScheduleListener {
    void onComplete(final Models.ApiResponse<String> res);
  }

}
