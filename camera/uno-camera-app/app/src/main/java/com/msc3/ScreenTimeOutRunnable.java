package com.msc3;

import android.app.Activity;

/**
 * @author MC
 *         <p/>
 *         a simple timeout runnable, when timeout occurs, it will run the runOnUi runnable on the UI thread, using act.runOnUiThread()
 */
public class ScreenTimeOutRunnable implements Runnable {

  private Activity mActivity;
  private Runnable uiRunnable;

  private boolean timeOutCancelled;

  public ScreenTimeOutRunnable(Activity act, Runnable runOnUi) {
    timeOutCancelled = false;

    mActivity = act;
    uiRunnable = runOnUi;
  }

  public void setCancel(boolean b) {
    timeOutCancelled = b;
  }

  @Override
  public void run() {
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      //// // Log.d("mbp","ScreenTimeOutRunnable gets Interrupted ");
    }//10 sec
    finally {
      if (timeOutCancelled) {
        return;
      }
    }

    mActivity.runOnUiThread(uiRunnable);

  }
}
