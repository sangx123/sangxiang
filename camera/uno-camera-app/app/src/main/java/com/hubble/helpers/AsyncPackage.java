package com.hubble.helpers;

import android.os.AsyncTask;

/**
 * Created by Sean on 5/11/2015.
 */
public class AsyncPackage {
  public static AsyncTask<Void, Void, Void> doInBackground(final Runnable runnable) {
    return new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void... params) {
        runnable.run();
        return null;
      }
    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }
}
