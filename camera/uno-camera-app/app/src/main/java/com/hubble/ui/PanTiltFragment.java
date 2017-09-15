package com.hubble.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actor.model.Direction;
import com.actor.model.QueuePanTilt;
import com.hubble.HubbleApplication;
import com.hubble.PanTiltActorJava;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.helpers.AsyncPackage;
import com.hubble.registration.PublicDefine;
import com.hubbleconnected.camera.R;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.LinkedBlockingQueue;



/**
 * Created by brennan on 15-04-23.
 */
public class PanTiltFragment extends Fragment{
  private static final String TAG = "PanTiltFragment";

  public static final long DIRECTION_COMMAND_DURATION = 1000;
  private static LinkedBlockingQueue<QueuePanTilt> panTiltBlockingQueue;

  public static final int PAN_TILT_FAILED = -1;
  public static final int PAN_TILT_SUCCESS = 0;
  public static final int PAN_TILT_BOUNDARY = 1;

  private boolean mIsFinishedStopping = true;
  private boolean mIsVTech = false;
  private Configuration mConfiguration;
  private Device mDevice;
  private Fragment mParentFragment;
  private ImageButton btnUp;
  private ImageButton btnDown;
  private ImageButton btnLeft;
  private ImageButton btnRight;
  private LinearLayout queueHolder;
  private PanTiltActorJava actor;
  private ImageButton btnCenter;
  private RelativeLayout layoutBlock;
  private String modelName;
  private SecureConfig settings = HubbleApplication.AppConfig;
  private Activity mActivity;
  private ImageView pantiltLeft;
  private ImageView pantiltRigt;
  private boolean displayRTL;
  private boolean mPanTiltTouched;
  private boolean mIsRTMP=false;
  private PanTiltBoundaryHolder mPanTitlBoundaryHolder;

  public PanTiltFragment() {
    /*
     * 20151203: HOANG: AA-1273
     * App need to provide default empty constructor for fragment then use setArguments.
     * Avoid to declare non empty constructor for fragment because it could cause NoSuchMethod
     * exception.
     */
  }

  public void setBoundaryHolder(PanTiltBoundaryHolder panTiltBoundaryHolder) {
    mPanTitlBoundaryHolder = panTiltBoundaryHolder;
  }

  public void setDevice(Device device) {
    mDevice = device;
    if (mDevice != null) {
      mIsVTech = mDevice.getProfile().isVTechCamera();
    }
    if (panTiltBlockingQueue != null) {
      panTiltBlockingQueue.clear();
    }
  }

  public static PanTiltFragment newInstance(Device device) {
    PanTiltFragment fragment = new PanTiltFragment();
    fragment.setDevice(device);
    return fragment;
  }
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity = getActivity();
    displayRTL = settings.getBoolean(PublicDefine.DISPLAY_RTL, false);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View mView = inflater.inflate(R.layout.pan_tilt_fragment, container, false);


    if (panTiltBlockingQueue == null) {
      panTiltBlockingQueue = new LinkedBlockingQueue<QueuePanTilt>();
    }

    mPanTiltTouched = false;
    if (mDevice != null) {
      if (actor == null) {
        long queueTime = 0;
        long stopTime = 0;

        if (mIsVTech) {
          queueTime = 2000;
          stopTime = 600;
        } else {
          queueTime = 600;
          stopTime=1490;
          //stopTime = 1490;
          /*if(mIsRTMP) {
            stopTime = 500;
            queueTime=5000;
          }else {
            stopTime = 1000;
            queueTime = 4500;
          }*/
        }

        actor = new PanTiltActorJava(mDevice, queueTime, stopTime) {
          @Override
          public void onPresetFail(int code) {

          }

          @Override
          public void onPresetSuccess() {

          }

          @Override
          public void onPanSuccess(Direction direction) {
            if (mIsVTech && getActivity() != null) {
              if (isPortrait()) {
                //getQueueHolder().removeView(getQueueHolder().getChildAt(0));
              }
            }
          }

          @Override
          public void onPanFailure(Direction direction) {
            if (mIsVTech && mActivity != null) {
              if (isPortrait()) {
                //getQueueHolder().removeView(getQueueHolder().getChildAt(0));
              }
            }

            if (direction == Direction.center || direction == Direction.centerH) {
              //removeDirectionViews();
              enableAllButtons();
              mIsFinishedStopping = true;
              if (mActivity != null)
                try {
                  Toast.makeText(mActivity, getResources().getString(R.string.send_cmd_pantilt_fail), Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
            }
          }

          @Override
          public void onPanBoundary(@NotNull Direction direction) {
            setPanTiltBoundaryEnabled(direction, true);
          }

          @Override
          public void onStop() {
            removeDirectionViews();
            enableAllButtons();
            mIsFinishedStopping = true;
          }

          @Override
          public void onFinishCenter() {
            //removeDirectionViews();
            enableAllButtons();
            mIsFinishedStopping = true;
          }

          @Override
          public void onFailCenter() {
            //layoutBlock.setVisibility(View.GONE);
            //removeDirectionViews();
            enableAllButtons();
            if (mActivity !=null)
              /*
               * 20151123: Hoang: crash here!!!
               * When PanTiltActor callback this method, PanTiltFragment could have been replaced by
               * other fragments. So add try...catch... here to avoid crashes.
               */
              try {
                Toast.makeText(mActivity, getResources().getString(R.string.not_support_SOC), Toast.LENGTH_LONG).show();
              } catch (Exception e) {
                e.printStackTrace();
              }
            mIsFinishedStopping = true;
          }
        };
        actor.setPanTiltTouchHandler(mPanTiltTouchHandler);
      }
    }

    btnUp = (ImageButton) mView.findViewById(R.id.panTiltUp);
    btnDown = (ImageButton) mView.findViewById(R.id.panTiltDown);
    btnLeft = (ImageButton) mView.findViewById(R.id.panTiltLeft);
    btnRight = (ImageButton) mView.findViewById(R.id.panTiltRight);
    btnCenter = (ImageButton) mView.findViewById(R.id.panTiltCenter);

    btnLeft.setOnTouchListener(directionTouchListener);
    btnRight.setOnTouchListener(directionTouchListener);

    ImageView closeImage=(ImageView)mView.findViewById(R.id.close_pan_tilt);
    closeImage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((ViewFinderFragment)mParentFragment).removeFragment(PanTiltFragment.this);
      }
    });


    if (mDevice != null && mDevice.getProfile().doesLeftRightPanTiltOnly()) {
      btnUp.setImageResource(R.drawable.top_disabled);
      btnDown.setImageResource(R.drawable.bottom_disabled);
    } else {
      btnUp.setOnTouchListener(directionTouchListener);
      btnDown.setOnTouchListener(directionTouchListener);
    }


    if (mDevice != null && mDevice.getProfile().modelId.equals("0085")) {
      modelName = findReadlCameraModel();
      if (modelName == null) {
        AsyncPackage.doInBackground(new Runnable() {
          @Override
          public void run() {
            final Pair<String, Object> response = mDevice.sendCommandGetValue("get_model", null, null);
            if (response != null) {
              if (response.second instanceof String) {
                modelName = (String) response.second;
                settings.putString("UDID_CACHED_" + mDevice.getProfile().getRegistrationId(), modelName);
              } else {
                Integer modelIdInt = (Integer) response.second;
                if (modelIdInt.intValue() != -1) {
                  String value = String.format("%04d", modelIdInt);
                  modelName = value;
                  settings.putString("UDID_CACHED_" + mDevice.getProfile().getRegistrationId(), value);
                }
              }
            }
           
            if (mActivity != null) {
              mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  if (modelName != null && (modelName.equals("0854")) && checkVersionSupportCenter(mDevice.getProfile().firmwareVersion, "01.19.06") && btnCenter != null) {
                    btnCenter.setVisibility(View.VISIBLE);
                    btnCenter.setOnTouchListener(directionTouchListener);
                  }
                }
              });
            }
          }
        });

      } else {
        if (modelName.equals("0854") && checkVersionSupportCenter(mDevice.getProfile().firmwareVersion, "01.19.06")) {
          btnCenter.setVisibility(View.VISIBLE);
          btnCenter.setOnTouchListener(directionTouchListener);
        }
      }
    } else {
      if (mDevice != null && mDevice.getProfile().isSupportMoveToCenter()) {
        btnCenter.setVisibility(View.VISIBLE);
        btnCenter.setOnTouchListener(directionTouchListener);
      }
    }

    return mView;
  }


  @Override
  public void onPause() {
    mIsFinishedStopping = false;
    actor.setStop(true);
    pauseVTechQueue();
    super.onPause();
  }

  public void pauseVTechQueue() {

    if (actor != null) {
      LinkedBlockingQueue<QueuePanTilt> tempQueueHolder = actor.getQueue();
      try {
        synchronized (tempQueueHolder) {
          for (QueuePanTilt item : tempQueueHolder) {
            panTiltBlockingQueue.put(item);
          }
        }
      } catch (InterruptedException e) {
      }
      actor.clearQueue();
    }
  }

  @Override
  public void onResume() {
    mIsFinishedStopping = true;
    super.onResume();
    mParentFragment = getParentFragment();
    mConfiguration = getResources().getConfiguration();
    if (actor != null && panTiltBlockingQueue != null) {

      actor.offerQueue(panTiltBlockingQueue);
      panTiltBlockingQueue.clear();
      actor.processCompleteQueue();
    }actor.processCompleteQueue();
  }


  /*
   * 20161124 HOANG VIC-1970
   * Used this for detecting whether user is long pressed
   */
  private PanTiltTouchHandler mPanTiltTouchHandler = new PanTiltTouchHandler() {
    @Override
    public boolean isPanTiltTouching() {
      return mPanTiltTouched;
    }
  };

  private View.OnTouchListener directionTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      int action = event.getAction();
      switch (action) {
        case MotionEvent.ACTION_DOWN:
          mPanTiltTouched = true;
          if (mIsVTech) {
            sendDirectionCommand(v);
          } else {
            if (mIsFinishedStopping) {
              mIsFinishedStopping = false;
              displayDirection(v);

              Direction direction = getDirectionFromView(v);
              if (displayRTL) {
                if (direction == Direction.left) {
                  direction = Direction.right;
                } else if (direction == Direction.right) {
                  direction = Direction.left;
                }
              }
              disableOtherButtons(direction);
              actor.panContinuously(direction);
            }

          }
          break;
        case MotionEvent.ACTION_UP:
          mPanTiltTouched = false;
          if (!mIsVTech) {
            actor.setStop(true);
          }
          break;
        default:
          break;
      }

      if (mParentFragment != null && mParentFragment instanceof ViewFinderFragment) {
        ((ViewFinderFragment) mParentFragment).checkAndFlushAllBuffers();
      }
      return false;
    }
  };

  private void displayDirection(final View v) {
    final Direction direction = getDirectionFromView(v);
    ((ViewFinderFragment)mParentFragment).showDirection(direction);
  }

  private void removeDirectionViews() {
    ((ViewFinderFragment)mParentFragment).removeDirectionView();
  }

  private void disableOtherButtons(Direction direction) {
    if (direction == Direction.right) {
      btnUp.setBackgroundResource(R.drawable.top_disabled);
      btnDown.setBackgroundResource(R.drawable.bottom_disabled);
      btnLeft.setBackgroundResource(R.drawable.left_disabled);
      btnCenter.setBackgroundResource(R.drawable.center_disabled);
      btnDown.setEnabled(false);
      btnLeft.setEnabled(false);
      btnCenter.setEnabled(false);
    } else if (direction == Direction.left) {
      btnUp.setBackgroundResource(R.drawable.top_disabled);
      btnDown.setBackgroundResource(R.drawable.bottom_disabled);
      btnRight.setBackgroundResource(R.drawable.right_disabled);
      btnCenter.setBackgroundResource(R.drawable.center_disabled);
      btnUp.setEnabled(false);
      btnDown.setEnabled(false);
      btnRight.setEnabled(false);
      btnCenter.setEnabled(false);
    } else if (direction == Direction.down) {
      btnUp.setBackgroundResource(R.drawable.top_disabled);
      btnLeft.setBackgroundResource(R.drawable.left_disabled);
      btnRight.setBackgroundResource(R.drawable.right_disabled);
      btnCenter.setBackgroundResource(R.drawable.center_disabled);
      btnUp.setEnabled(false);
      btnRight.setEnabled(false);
      btnCenter.setEnabled(false);
      btnLeft.setEnabled(false);
    } else if (direction == Direction.up) {
      btnLeft.setBackgroundResource(R.drawable.left_disabled);
      btnDown.setBackgroundResource(R.drawable.bottom_disabled);
      btnRight.setBackgroundResource(R.drawable.right_disabled);
      btnCenter.setBackgroundResource(R.drawable.center_disabled);
      btnDown.setEnabled(false);
      btnRight.setEnabled(false);
      btnCenter.setEnabled(false);
      btnLeft.setEnabled(false);
    } else if (direction == Direction.center) {
      btnUp.setBackgroundResource(R.drawable.top_disabled);
      btnDown.setBackgroundResource(R.drawable.bottom_disabled);
      btnRight.setBackgroundResource(R.drawable.right_disabled);
      btnLeft.setBackgroundResource(R.drawable.left_disabled);
      btnUp.setEnabled(false);
      btnDown.setEnabled(false);
      btnLeft.setEnabled(false);
    }
  }

  private void enableAllButtons(){
    btnUp.setBackgroundResource(R.drawable.top);
    btnDown.setBackgroundResource(R.drawable.bottom);
    btnRight.setBackgroundResource(R.drawable.right);
    btnLeft.setBackgroundResource(R.drawable.left);
    btnCenter.setBackgroundResource(R.drawable.center_navigate);
    btnUp.setEnabled(true);
    btnDown.setEnabled(true);
    btnRight.setEnabled(true);
    btnCenter.setEnabled(true);
    btnLeft.setEnabled(true);

  }

  private Direction getDirectionFromView(final View v) {
    Direction direction = null;

    if (v == btnUp) {
      direction = Direction.up;
    } else if (v == btnDown) {
      direction = Direction.down;
    } else if (v == btnLeft) {
      direction = Direction.left;
    } else if (v == btnRight) {
      direction = Direction.right;
    } else if (v == btnCenter) {
      direction = Direction.center;
    }

    return direction;
  }

  /**
   * Direction is: DIRECTION_LEFT, DIRECTION_RIGHT, DIRECTION_UP, DIRECTION_DOWN
   * @param direction
   * @param isEnabled
   */
  private void setPanTiltBoundaryEnabled(Direction direction, final boolean isEnabled) {
    final ImageView imgBoundaryIndicator;
    switch (direction) {
      case left:
        imgBoundaryIndicator = mPanTitlBoundaryHolder.imgBoundaryLeft;
        break;
      case right:
        imgBoundaryIndicator = mPanTitlBoundaryHolder.imgBoundaryRight;
        break;
      case up:
        imgBoundaryIndicator = mPanTitlBoundaryHolder.imgBoundaryUp;
        break;
      case down:
        imgBoundaryIndicator = mPanTitlBoundaryHolder.imgBoundaryDown;
        break;
      default:
        imgBoundaryIndicator = null;
        break;
    }

   /* if (imgBoundaryIndicator != null) {
      imgBoundaryIndicator.post(new Runnable() {
        @Override
        public void run() {
          if (isEnabled) {
            imgBoundaryIndicator.setVisibility(View.VISIBLE);
            Animation myBlinkingAnimation = AnimationUtils.loadAnimation(imgBoundaryIndicator.getContext(), R.anim.pan_tilt_boundary_blink);
            imgBoundaryIndicator.startAnimation(myBlinkingAnimation);
          } else {
            imgBoundaryIndicator.clearAnimation();
            imgBoundaryIndicator.setVisibility(View.GONE);
          }
        }
      });
    }*/
  }

  private void sendDirectionCommand(View v) {
    Direction direction = getDirectionFromView(v);

    if (mIsVTech) {
      actor.pan(direction);
     // addDirectionToQueueView(direction);
    }
  }


  private boolean isPortrait() {
    if (mConfiguration != null && (mConfiguration.orientation & Configuration.ORIENTATION_PORTRAIT) == Configuration.ORIENTATION_PORTRAIT) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean checkVersionSupportCenter(String version1, String version2) {
    // version format 01.01.01
    String[] versions1 = version1.split("\\.");
    String[] versions2 = version2.split("\\.");
    boolean result = false;

    if (versions1.length == 3 && versions2.length == 3) {
      Integer major1 = Integer.parseInt(versions1[0]);
      Integer major2 = Integer.parseInt(versions2[0]);
      Integer minor1 = Integer.parseInt(versions1[1]);
      Integer minor2 = Integer.parseInt(versions2[1]);
      Integer patch1 = Integer.parseInt(versions1[2]);
      Integer patch2 = Integer.parseInt(versions2[2]);

      if (major1 > major2) {
        result = true;
      } else if (major1 == major2) {
        if (minor1 > minor2) {
          result = true;
        } else if (minor1 == minor2) {
          if (patch1 > patch2) {
            result = true;
          } else if (patch1 == patch2) {
            result = true;
          } else {
            result = false;
          }
        } else if (minor2 > minor1) {
          result = false;
        }
      }
    }

    return result;
  }

  private String findReadlCameraModel() {
    if (mDevice!= null) {
      String cachedModelId = settings.getString("UDID_CACHED_" + mDevice.getProfile().getRegistrationId(), null);
      // if camera model is cached before, using it
      if(cachedModelId != null) {
        return cachedModelId;
      }
    }
    return null;
  }

  public void isRTMPStreaming(boolean isRTMP){
    mIsRTMP=isRTMP;
  }



}