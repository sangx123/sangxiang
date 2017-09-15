package com.msc3;

public class CountDownTimer implements Runnable {

  private static final String TAG = "CountDownTimer";
  private int durationInSec;
  private boolean running;
  private ITimerUpdater updater;

  public CountDownTimer(int d, ITimerUpdater itime) {
    durationInSec = d;
    running = true;
    updater = itime;
  }

  public void stop() {
    running = false;
  }

  public void run() {

    int count = durationInSec;
    while (running) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
        if (!running) {
          break;
        }
      }
      count--;

      if (count <= 0) {
        break;
      }
      updater.updateCurrentCount(count);

    }

    if (count <= 0) {
      updater.timeUp();
    } else {
      // stopped
      updater.timerKick();
    }

  }

}
