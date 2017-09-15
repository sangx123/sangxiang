package com.hubble.model;

import android.app.Activity;
import android.util.Pair;

import com.crittercism.app.Crittercism;
import com.hubble.actors.Actor;
import com.hubble.command.CameraCommandUtils;
import com.hubble.devcomm.Device;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * Created by brennan on 15-05-13.
 */
public class VideoBandwidthSupervisor {
  private static final String TAG = VideoBandwidthSupervisor.class.getSimpleName();
  private static VideoBandwidthSupervisor mInstance;

  private Activity mActivity;
  private AtomicInteger mBitrate;
  private boolean mIsAdaptiveBitrateEnabled;
  private Device mDevice;
  private int mAverageFPS;
  private long mTotalFPS;
  private long mUpdateCounter;
  private LinkedBlockingQueue<Integer> mFPSQueue;
  private VideoBandwidthSupervisorInterface mListener;

  private class CheckIfAdaptiveBitrateIsEnabled {
  }

  private class DecrementBitrate {
  }

  private class IncrementBitrate {
  }

  private class PullBitrateFromDevice {
  }

  private class SetBitrate {
    public int bitrate = 200;

    public SetBitrate(int bitrate) {
      this.bitrate = bitrate;
    }
  }

  private class UpdateFPS {
    public int framesPerSecond = 0;

    public UpdateFPS(int framesPerSecond) {
      this.framesPerSecond = framesPerSecond;
    }
  }

  private Actor actor = new Actor() {
    @Override
    public Object receive(Object m) {
      Crittercism.leaveBreadcrumb(TAG + " actor.receive");
      if (m instanceof CheckIfAdaptiveBitrateIsEnabled) {
        if (mDevice.getProfile().isVTechCamera()) {
          final String adaptiveResponse = CameraCommandUtils.sendCommandGetStringValue(mDevice, "get_adaptive_bitrate", null, null);
          if (adaptiveResponse != null) {
            mIsAdaptiveBitrateEnabled = adaptiveResponse.equals("on");
          } else {
            mIsAdaptiveBitrateEnabled = false;
          }
        } else {

            //Phung: wtf do we have to do this ???
          //mDevice.sendCommandGetValue("set_adaptive_bitrate", "on", null);
          mIsAdaptiveBitrateEnabled = false;
        }
      } else if (m instanceof DecrementBitrate) {
        int num = mBitrate.get();
        if (num > 200) {
          setBitrate(num - 200);
        }
      } else if (m instanceof IncrementBitrate) {
        int num = mBitrate.get();
        if (num < 1000) {
          setBitrate(num + 200);
        }
      } else if (m instanceof PullBitrateFromDevice) {
        int bitrateResponse = CameraCommandUtils.sendCommandGetIntValue(mDevice, "get_video_bitrate", null, null);
        if (bitrateResponse >= 0) {
          mBitrate.set(bitrateResponse);
        }
      } else if (m instanceof SetBitrate) {
        final SetBitrate msg = (SetBitrate) m;
        if (mDevice != null) {
          boolean success = CameraCommandUtils.sendCommandGetSuccess(mDevice, "set_video_bitrate", msg.bitrate + "", null);
          if (success) {
            mBitrate.set(msg.bitrate);
            if (mListener != null) {
              mListener.updateDebugBitrate(msg.bitrate);
            }
          }
        }
      } else if (m instanceof UpdateFPS) {
        try {
          UpdateFPS msg = (UpdateFPS) m;
          if (mIsAdaptiveBitrateEnabled) {
            mUpdateCounter++;
            mTotalFPS += msg.framesPerSecond;
            pushFPSQueue(msg.framesPerSecond);
            mAverageFPS = getAverageFPS();

            if (mUpdateCounter % 10 == 0) {
              if (mAverageFPS <= 6) {
                decrementBitrate();
              } else if (mAverageFPS >= 10) {
                incrementBitrate();
              }
            }
          }
        } catch (Exception e) {
        }
      }
      return null;
    }
  };

  public static VideoBandwidthSupervisor getInstance() {
    if (mInstance == null) {
      mInstance = new VideoBandwidthSupervisor();
    }
    return mInstance;
  }

  private VideoBandwidthSupervisor() {
    mIsAdaptiveBitrateEnabled = true;
    mBitrate = new AtomicInteger(200);
    mAverageFPS = 0;
    mTotalFPS = 0;
    mUpdateCounter = 0;
    mFPSQueue = new LinkedBlockingQueue<Integer>();
  }

  public VideoBandwidthSupervisor resetValues() {
    mIsAdaptiveBitrateEnabled = true;
    mBitrate = new AtomicInteger(200);
    mAverageFPS = 0;
    mTotalFPS = 0;
    mUpdateCounter = 0;
    mFPSQueue = new LinkedBlockingQueue<Integer>();
    return this;
  }

  public VideoBandwidthSupervisor setActivity(Activity activity) {
    mActivity = activity;
    return this;
  }

  public VideoBandwidthSupervisor setListener(VideoBandwidthSupervisorInterface listener) {
    mListener = listener;
    return this;
  }

  public VideoBandwidthSupervisor setDevice(Device device) {
    mDevice = device;
    return this;
  }

  public VideoBandwidthSupervisor setBitrate(final int bitrate) {
    actor.send(new SetBitrate(bitrate));
    return this;
  }

  public void setBitrateVariable(int bitrate) {
    mBitrate.set(bitrate);
  }

  public int getBitrate() {
    return mBitrate.get();
  }

  public VideoBandwidthSupervisor pullBitrateFromDevice() {
    actor.send(new PullBitrateFromDevice());
    return this;
  }

  public VideoBandwidthSupervisor setAdaptiveBitrate(boolean isAdaptive) {
    mIsAdaptiveBitrateEnabled = isAdaptive;
    return this;
  }

  public VideoBandwidthSupervisor useRecoverySettings() {
    setBitrate(200);
    return this;
  }

  public VideoBandwidthSupervisor checkIfAdaptiveBitrateIsEnabled() {
    actor.send(new CheckIfAdaptiveBitrateIsEnabled());
    return this;
  }

  public boolean getIsAdaptiveBitrateEnabled() {
    return mIsAdaptiveBitrateEnabled;
  }

  public void updateFPS(int framesPerSecond) {
    actor.send(new UpdateFPS(framesPerSecond));
  }

  private VideoBandwidthSupervisor incrementBitrate() {
    actor.send(new IncrementBitrate());
    return this;
  }

  private VideoBandwidthSupervisor decrementBitrate() {
    actor.send(new DecrementBitrate());
    return this;
  }

  private void pushFPSQueue(int framesPerSecond) {
    try {
      int size = mFPSQueue.size();
      if (size >= 20) {
        mFPSQueue.remove();
      }
      if (size < 20) {
        mFPSQueue.put(framesPerSecond);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private int getAverageFPS() {
    int averageFPS;
    try {
      Integer[] fpsArray = mFPSQueue.toArray(new Integer[mFPSQueue.size()]);
      int sum = 0;
      for (Integer fps : fpsArray) {
        sum += fps;
      }
      averageFPS = sum / fpsArray.length;
    } catch (Exception e) {
      e.printStackTrace();
      averageFPS = (int) (mTotalFPS / mUpdateCounter);
    }
    return averageFPS;
  }

  public interface VideoBandwidthSupervisorInterface {
    void updateDebugBitrate(int bitrate);
  }
}