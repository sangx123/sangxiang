package com.blinkhd;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import base.hubble.meapi.User;
import base.hubble.meapi.user.UploadTokenResponse;

public class UploadService extends IntentService { // TODO: Remove this class

  public static final String UPLOAD_FILE_PATH = "upload_file_path";
  public static final String USER_TOKEN = "user_token";
  public static final String UPLOAD_REG_ID = "reg_id";
  private static final String TAG = "UploadService";

  public UploadService() {
    super("UploadService");
  }

  public UploadService(String name) {
    super(name);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Bundle extra = intent.getExtras();
    if (extra != null) {
      String filePath = extra.getString(UPLOAD_FILE_PATH);
      String apiKey = extra.getString(USER_TOKEN);
      String regId = extra.getString(UPLOAD_REG_ID);

      if (filePath == null || apiKey == null || regId == null) {
        Log.i("mbp", "UploadService: File path or token or regId is null");
      } else {
        try {
          Log.i("mbp", "UploadService: start upload image for camera id: " + regId
          );
          UploadTokenResponse res = User.getUserUploadToken(apiKey);
          if (res.getStatus() == 200) {
            if (res.getData() != null && res.getData().getUpload_token() != null) {
              InputStream stream = new FileInputStream(new File(filePath));
              //Upload.uploadFile(stream, apiKey, res.getData().getUpload_token(), regId);
            }
          } else {
            Log.i("mbp", "Upload service: get upload token failed.");
          }
        } catch (Exception e) {
          // // Log.e(TAG, Log.getStackTraceString(e));
        }
      }
    } else {
      Log.i("mbp", "UploadService: extra is null.");
    }
  }

}
