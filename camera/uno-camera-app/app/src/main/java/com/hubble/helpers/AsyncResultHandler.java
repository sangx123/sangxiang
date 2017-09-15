package com.hubble.helpers;

import base.hubble.IAsyncTaskCommonHandler;

/**
 * Created by dan on 2014-07-10.
 * <p/>
 * A helper to only care about implementing hendling the result of an async task.
 */
abstract public class AsyncResultHandler<T> implements IAsyncTaskCommonHandler {

  abstract public void onResult(T result);

  @Override
  public void onPostExecute(Object result) {
    this.onResult((T) result);
  }

  @Override
  public void onPreExecute() {
  }

  @Override
  public void onCancelled() {
  }
}
