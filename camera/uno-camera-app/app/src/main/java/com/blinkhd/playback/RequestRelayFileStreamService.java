package com.blinkhd.playback;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import base.hubble.Api;
import base.hubble.Models;
import retrofit.RetrofitError;

public class RequestRelayFileStreamService extends IntentService {
  // TODO: Rename actions, choose action names that describe tasks that this
  // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
  public static final String ACTION_RELAY_FILE_STREAM_REQUEST = "com.cvisionhk.RelayFileStreamRequest";


  public static final String API_KEY = "api_key";
  public static final String DEVICE_MAC = "device_mac";
  public static final String FILE_NAME = "file_name";
  public static final String MD5_SUM = "m5_sum";
  public static final String USE_JOB_BASED = "use_job_based";
  public static final boolean USE_JOB_BASED_DEFAULT = false;

  public RequestRelayFileStreamService() {
    super("RequestRelayFileStreamService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent != null) {
      final String action = intent.getAction();
      if (ACTION_RELAY_FILE_STREAM_REQUEST.equals(action)) {
        String apiKey = intent.getStringExtra(API_KEY);
        String device_mac = intent.getStringExtra(DEVICE_MAC);
        String fileName = intent.getStringExtra(FILE_NAME);
        String md5Sum = intent.getStringExtra(MD5_SUM);
        boolean useJobBased = intent.getBooleanExtra(USE_JOB_BASED, USE_JOB_BASED_DEFAULT);
        try {
          Log.d("mbp", "Request file stream to server for file: " + fileName + ", use job-based? " + useJobBased);
          /*
           * 20161212 HOANG AA-2215
           * Support RTMP file streaming job based.
           */
          if (useJobBased) {
            Api.getInstance().getService().createFileSessionJobBased(apiKey, device_mac, "1",
                    new Models.CreateFileSessionRequest("android", fileName, md5Sum, null));
          } else {
            Api.getInstance().getService().createFileSession(apiKey, device_mac, "1",
                    new Models.CreateFileSessionRequest("android", fileName, md5Sum, null));
          }
          Log.d("mbp", "Request file stream to server for file: " + fileName + " DONE");
        } catch(RetrofitError ex) {
          ex.printStackTrace();
        }
      }
    }
  }
}
