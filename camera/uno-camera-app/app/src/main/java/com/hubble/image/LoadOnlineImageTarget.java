package com.hubble.image;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.hubble.HubbleApplication;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by hoang on 11/20/15.
 */
public class LoadOnlineImageTarget implements Target {
  private static final String TAG = HubbleApplication.TAG;

  private String mFilePath = null;

  public LoadOnlineImageTarget() {
  }

  public void setOfflineFinePath(String mFilePath) {
    this.mFilePath = mFilePath;
    // Log.i(TAG, "update LoadOnlineImageTarget instance, file path: " + mFilePath);
  }

  @Override
  public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
    if (bitmap != null) {
      if (mFilePath != null) {
        Runnable runn = new Runnable() {
          @Override
          public void run() {
            boolean isSucceeded = false;
            File latestSnapFile = new File(mFilePath);
            try {
              FileOutputStream fos = new FileOutputStream(latestSnapFile);
              bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
              fos.close();
              isSucceeded = true;
            } catch (IOException e) {
              e.printStackTrace();
            }
            Log.i(TAG, "LoadOnlineImageTarget for file: " + mFilePath + " DONE, isSucceeded? " + isSucceeded);
          }
        };
        Thread worker = new Thread(runn);
        worker.start();
      } else {
        Log.i(TAG, "LoadOnlineImageTarget for file: " + mFilePath + " DONE, file path is null -> don't save it");
      }
    } else {
      Log.i(TAG, "LoadOnlineImageTarget for file: " + mFilePath + " FAILED, bitmap is null");
    }
  }

  @Override
  public void onBitmapFailed(Drawable errorDrawable) {
    Log.i(TAG, "LoadOnlineImageTarget for file: " + mFilePath + " FAILED");
  }

  @Override
  public void onPrepareLoad(Drawable placeHolderDrawable) {
    //Log.i(TAG, "LoadOnlineImageTarget for file: " + mFilePath + " onPrepareLoad");
  }
}
