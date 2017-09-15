package com.nxcomm.blinkhd.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.bta.BTATask;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.helpers.AsyncPackage;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.SubscriptionWizard;
import com.hubble.registration.Util;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;
//import com.nxcomm.blinkhd.ui.CameraSettingsFragment;
import com.nxcomm.blinkhd.ui.Global;
import com.nxcomm.blinkhd.ui.MvrScheduleActivity;
import com.nxcomm.blinkhd.util.NotificationSettingUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.database.DeviceProfile;
import retrofit.RetrofitError;
import retrofit.mime.TypedByteArray;


/**
 * Created by BinhNguyen on 10/9/2015.
 */
public class MotionOptionDialog {//extends AlertDialog.Builder {
  private static final String TAG = "CameraSettingsFragment";

/*  public static final int RECORD_MOTION_OPT_OFF = 0;
  public static final int RECORD_MOTION_OPT_CLOUD = 1;
  public static final int RECORD_MOTION_OPT_SDCARD = 2;

  public static final int SDCARD_FULL_OPT_REMOVE_OLDEST = 0;
  public static final int SDCARD_FULL_OPT_SWITCH_TO_CLOUD = 1;

  public static final int MD_TYPE_OFF_INDEX = 0;
  public static final int MD_TYPE_MD_INDEX = 1;
  public static final int MD_TYPE_BSC_INDEX = 2;
  public static final int MD_TYPE_BSD_INDEX = 3;

  public static final String MD_TYPE_OFF = "OFF";
  public static final String MD_TYPE_MD = "MD";
  public static final String MD_TYPE_BSC = "BSC";
  public static final String MD_TYPE_BSD = "BSD";*/

 /* private CameraSettingsFragment mContext;
  private Models.DeviceSchedule mScheduleData;
  private ProgressDialog mDialog;
  private SecureConfig settings = HubbleApplication.AppConfig;
  private BTATask task;
  private volatile int btaRemainingTime = -1;
  public void setScheduleData(Models.DeviceSchedule scheduleData) {
    if (!BuildConfig.ENABLE_MVR_SCHEDULING) {
      return;
    }
    scheduleStorageSpinner.setOnItemSelectedListener(null);
    mScheduleData = scheduleData;
    if (mContext != null && mContext.getActivity() != null) {
      mContext.getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (mScheduleData != null && mScheduleData.isEnable()) {
            setScheduleStorageSpinner(1);
            updateMvrTextView();
          } else {
            setScheduleStorageSpinner(0);
          }
          scheduleStorageSpinner.setOnItemSelectedListener(scheduleListener);
        }
      });
    }
  }

  public interface Listener {

    CameraSettingsFragment.ListChild getMotionDetection();

    CameraSettingsFragment.ListChild getCurrentPlan();

    Device getDevice();

    CompoundButton.OnCheckedChangeListener getMotionRecordingCheckedChanged();

    AdapterView.OnItemSelectedListener getRecordingPlanListener();

    void handleRecordingPlan(Spinner spinner);

    void setupSoundOrMotionValueField(CameraSettingsFragment.ListChild motionDetection);

    void onClickPositiveButton(boolean enableMotion, int motionLevel);

    void onClickPositiveButtonVDa(int position, int motionLevel, int prevPosition);
  }

  private Switch detectSwitch, recordSwitch;
  private SeekBar detectSeekbar;
  private View seekbarHolder, recordHolder, recordingPlanHolder, scheduleHolder;
  private Spinner recordStorageSpinner, recordingPlanSpinner, scheduleStorageSpinner;
  private TextView txtScheduleNext, txtScheduleCurrent;
  private Spinner motionDetectionVDA;

  public View recordStorageHolder;

  private Listener mListener;
  private AdapterView.OnItemSelectedListener recordListener, scheduleListener, mRecordingPlanListener, motionvdaListener;
  private int previousSelectionValue;
  private CompoundButton.OnCheckedChangeListener motionListener;

  public MotionOptionDialog(CameraSettingsFragment context, Listener listener) {
    super(context.getActivity());
    mContext = context;
    mListener = listener;

    recordListener = new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "recordListener, onItemSelected, position? " + position);
        if (position == RECORD_MOTION_OPT_OFF) { // off mvr
          disableMvr();
        } else if (position == RECORD_MOTION_OPT_CLOUD) { // cloud storage
          enableMvrOnCloud();
        } else if (position == RECORD_MOTION_OPT_SDCARD) {
          enableMvrOnSDCard();
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    };
    scheduleListener = new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (mScheduleData == null) {
          return;
        }
        turnSchedulingOnOff(position != 0, new TurnScheduleTask.TurnScheduleListener() {
          @Override
          public void onComplete(Models.ApiResponse<String> res) {
            // reset to previous selection if failed
            if (res == null || !res.getStatus().equalsIgnoreCase("200")) {
              mScheduleData.setEnable(!mScheduleData.isEnable());
            }
            mContext.runOnUiThreadIfVisible(new Runnable() {
              @Override
              public void run() {
                mDialog.dismiss();
                updateMvrTextView();
              }
            });
          }
        });
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    };

    motionvdaListener = new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
          case MD_TYPE_OFF_INDEX:
            seekbarHolder.setVisibility(View.GONE);
            setRecordHolderVisibility(View.GONE);
            setScheduleHolderVisibility(View.GONE);
            break;
          case MD_TYPE_MD_INDEX:
            seekbarHolder.setVisibility(View.VISIBLE);
            setRecordHolderVisibility(View.VISIBLE);
            setScheduleHolderVisibility(View.VISIBLE);
            break;
          case MD_TYPE_BSC_INDEX:
          case MD_TYPE_BSD_INDEX:
            seekbarHolder.setVisibility(View.GONE);
            // 20160830: Hoang: hide record and schedule settings for BSC and BSD
            setRecordHolderVisibility(View.GONE);
            setScheduleHolderVisibility(View.GONE);
            break;
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    };

    mRecordingPlanListener = listener.getRecordingPlanListener();
    CameraSettingsFragment.ListChild motionDetection = mListener.getMotionDetection();
    final Device device = mListener.getDevice();
    if(device.getProfile().getRegistrationId().startsWith("010877")) {
      final String apiKey = Global.getApiKey(HubbleApplication.AppContext);
      task = new BTATask(apiKey, device);
      //TODO future callback argument
      Futures.addCallback(task.getRemainBTATime(), new FutureCallback<Object>() {
        @Override
        public void onSuccess(Object response) {
          Pair<String,Object> result=(Pair<String,Object>)response;
          if(result.second instanceof Integer) {
            int val = (int) result.second;
            Log.i(TAG, "get bta remaining time response " + result.second + " --> " + val);
            btaRemainingTime = val;
          } else {
            Log.i(TAG, "get bta remaining time response " + result.first + " --> " + result.second);
          }
        }
        @Override
        public void onFailure(Throwable t) {
          Log.w(TAG, "get bta remaining time error");
          t.printStackTrace();
        }
      });
    }
    View view = LayoutInflater.from(context.getActivity()).inflate(R.layout.dialog_motion_detection, (ViewGroup) mContext.getView(), false);
    setView(view);
    setTitle(getSafeString(R.string.detect_motion));
    setPositiveButton(getSafeString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        int position = motionDetectionVDA.getSelectedItemPosition();
        // if change from MD to OFF -> use old command
        // if change from BSC BSD to OFF -> use new command
        int prevPosition = MD_TYPE_OFF_INDEX;
        String vdaMode = mListener.getMotionDetection().modeVda;
        if (MD_TYPE_MD.equals(vdaMode)) {
          prevPosition = MD_TYPE_MD_INDEX;
        } else if (MD_TYPE_BSC.equals(vdaMode)) {
          prevPosition = MD_TYPE_BSC_INDEX;
        } else if (MD_TYPE_BSD.equals(vdaMode)) {
          prevPosition = MD_TYPE_BSD_INDEX;
        }

        if (position == 2) {
          checkVdaLicense(position, prevPosition);
        } else if(position == 3) {
          Log.i(TAG, "bta remain time " + btaRemainingTime);
          if(btaRemainingTime > 0) {
            showWarningDialogAboutBTA(position, prevPosition, device);
          } else {
            checkVdaLicense(position, prevPosition);
          }
        } else {
          Log.i(TAG, "bta remain time " + btaRemainingTime);
          if(btaRemainingTime > 0) {
            showWarningDialogAboutBTA(position, prevPosition, device);
          } else {
            handleMD(position, prevPosition, device);
          }
        }
      }
    });
    setNegativeButton(getSafeString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    detectSwitch = (Switch) view.findViewById(R.id.dialog_motiondetection_detectSwitch);
    recordSwitch = (Switch) view.findViewById(R.id.dialog_motiondetection_recordSwitch);
    detectSeekbar = (SeekBar) view.findViewById(R.id.dialog_motiondetection_seekbar);
    seekbarHolder = view.findViewById(R.id.dialog_motion_seekbarHolder);
    recordHolder = view.findViewById(R.id.dialog_motion_recordHolder);

    recordingPlanHolder = view.findViewById(R.id.recording_plan_holder);
    recordingPlanSpinner = (Spinner) view.findViewById(R.id.recording_plan_spinner);

    scheduleHolder = view.findViewById(R.id.layout_mvr_schedule);
    scheduleStorageSpinner = (Spinner) scheduleHolder.findViewById(R.id.schedule_storage_option_spinner);
    txtScheduleNext = (TextView) scheduleHolder.findViewById(R.id.txt_schedule_notice);
    txtScheduleCurrent = (TextView) scheduleHolder.findViewById(R.id.txt_schedule_current);
    // setup record storage holder
    recordStorageHolder = view.findViewById(R.id.dialog_motion_storage_option_holder);
    //recordStorageHolder.setVisibility(View.GONE);
    motionDetectionVDA = (Spinner)view.findViewById(R.id.motiondetection_vda);

    // setup record option storage spinner
    recordStorageSpinner = (Spinner) view.findViewById(R.id.record_storage_option_spinner);
    boolean sdCardSupported = device.getProfile().doesSupportSDCardAccess();
    StorageOptionAdapter rAdapter = new StorageOptionAdapter(context.getActivity(), sdCardSupported);
    recordStorageSpinner.setAdapter(rAdapter);


    // setup schedule option storage spinner
    OnOffOptionAdapter sAdapter = new OnOffOptionAdapter(context.getActivity());
    scheduleStorageSpinner.setAdapter(sAdapter);

    // setup recording plan spinner
    RecordingPlanOptionAdapter recordingPlanOptionAdapter = new RecordingPlanOptionAdapter(context.getActivity());
    recordingPlanSpinner.setAdapter(recordingPlanOptionAdapter);
    setRecordingPlanVisible(false);
    if (device.getProfile().doesSupportSDCardAccess()) {
      mListener.handleRecordingPlan(recordingPlanSpinner);
    }

    motionListener = new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          seekbarHolder.setVisibility(View.VISIBLE);
          setRecordHolderVisibility(View.VISIBLE);
          setScheduleHolderVisibility(View.VISIBLE);
        } else {
          seekbarHolder.setVisibility(View.GONE);
          setRecordHolderVisibility(View.GONE);
          setScheduleHolderVisibility(View.GONE);
        }
      }
    };

    MotionVdaOptionAdapter motionVdaOptionAdapter = new MotionVdaOptionAdapter(context.getActivity());

    if (NotificationSettingUtils.supportMultiMotionTypes(device.getProfile().modelId, device.getProfile().firmwareVersion)) {
      detectSwitch.setVisibility(View.GONE);
      motionDetectionVDA.setVisibility(View.VISIBLE);
      motionDetectionVDA.setAdapter(motionVdaOptionAdapter);
      motionDetectionVDA.setOnItemSelectedListener(motionvdaListener);

      Log.d(TAG, "Motion detection vda, mode: " + motionDetection.modeVda);
      if (motionDetection.modeVda.equalsIgnoreCase(MD_TYPE_MD)) {
        seekbarHolder.setVisibility(View.VISIBLE);
        setRecordHolderVisibility(View.VISIBLE);
        setScheduleHolderVisibility(View.VISIBLE);
        motionDetectionVDA.setSelection(MD_TYPE_MD_INDEX);
      } else if (motionDetection.modeVda.equalsIgnoreCase(MD_TYPE_BSD)) {
        seekbarHolder.setVisibility(View.GONE);
        // 20160830: Hoang: hide record and schedule settings for BSD
        setRecordHolderVisibility(View.GONE);
        setScheduleHolderVisibility(View.GONE);
        motionDetectionVDA.setSelection(MD_TYPE_BSD_INDEX);
      } else if (motionDetection.modeVda.equalsIgnoreCase(MD_TYPE_BSC)) {
        seekbarHolder.setVisibility(View.GONE);
        // 20160830: Hoang: hide record and schedule settings for BSC
        setRecordHolderVisibility(View.GONE);
        setScheduleHolderVisibility(View.GONE);
        motionDetectionVDA.setSelection(MD_TYPE_BSC_INDEX);
      } else {
        seekbarHolder.setVisibility(View.GONE);
        setRecordHolderVisibility(View.GONE);
        setScheduleHolderVisibility(View.GONE);
        motionDetectionVDA.setSelection(MD_TYPE_OFF_INDEX);
      }
    } else {
      detectSwitch.setOnCheckedChangeListener(motionListener);
      detectSwitch.setChecked(motionDetection.booleanValue);

      if (motionDetection.booleanValue) {
        seekbarHolder.setVisibility(View.VISIBLE);
        setRecordHolderVisibility(View.VISIBLE);
        setScheduleHolderVisibility(View.VISIBLE);
      } else {
        seekbarHolder.setVisibility(View.GONE);
        setRecordHolderVisibility(View.GONE);
        setScheduleHolderVisibility(View.GONE);
      }
    }

    if (motionDetection.booleanValue) {
      recordSwitch.setChecked(motionDetection.secondaryBooleanValue);
      if (motionDetection.secondaryBooleanValue) {
        //showRecordStorageOptionIfAvailable();
        // restore record storage option
        String storageMode = device.getProfile().getDeviceAttributes().getStorageMode();
        Log.i(TAG, "Record storage mode: " + storageMode);
        if (storageMode == null || storageMode.equalsIgnoreCase("0")) {
          *//*
           * 20160415: HOANG: AA-1734
           * Call setSpinnerSelectionWithoutChangeEvent to set default value of "previousSelectionValue"
           *//*
          setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_CLOUD);
          setRecordingPlanVisible(false);
        } else {
          *//*
           * 20160415: HOANG: AA-1734
           * Call setSpinnerSelectionWithoutChangeEvent to set default value of "previousSelectionValue"
           *//*
          setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_SDCARD);
          setRecordingPlanVisible(true);
        }
      } else {
        // MVR: off
        *//*
         * 20160415: HOANG: AA-1734
         * Call setSpinnerSelectionWithoutChangeEvent to set default value of "previousSelectionValue"
         *//*
        setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_OFF);
        setRecordingPlanVisible(false);
      }
    } else {
      *//*
       * 20160415: HOANG: AA-1734
       * Call setSpinnerSelectionWithoutChangeEvent to set default value of "previousSelectionValue"
       *//*
      setSpinnerSelectionWithoutChangeEvent(RECORD_MOTION_OPT_OFF);
    }

    recordSwitch.setOnCheckedChangeListener(mListener.getMotionRecordingCheckedChanged());

    if (device.getProfile().isVTechCamera()) {
      detectSeekbar.setMax(6);
    } else {
      detectSeekbar.setMax(4);
    }
    detectSeekbar.setProgress(motionDetection.intValue);

    recordStorageSpinner.setOnItemSelectedListener(recordListener);
    scheduleStorageSpinner.setOnItemSelectedListener(scheduleListener);

    final View.OnClickListener onClick = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        goToMvrScheduleActivity();
      }
    };
    view.findViewById(R.id.txt_mvr_schedule).setOnClickListener(onClick);
    txtScheduleNext.setOnClickListener(onClick);
    txtScheduleCurrent.setOnClickListener(onClick);
  }

  private void showWarningDialogAboutBTA(final int position, final int prevPosition, final Device device) {
    AlertDialog.Builder builder = new AlertDialog.Builder(mContext.getActivity());
    builder.setMessage(R.string.warning);
    builder.setMessage(R.string.bta_will_disable_if_you_choose_detect_motion_mode_is_md_off);
    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if(which == AlertDialog.BUTTON_POSITIVE) {
          final ProgressDialog progressDialog = new ProgressDialog(mContext.getActivity());
          progressDialog.setMessage(mContext.getString(R.string.disable_bta));
          progressDialog.setCancelable(false);
          progressDialog.show();
          //TODO check callback argument
          Futures.addCallback(task.stopBTA(), new FutureCallback<Object>() {
                @Override
                public void onSuccess(Object response) {
                  Pair<String, Object> result=(Pair<String, Object>)response;
                  Log.w(TAG, "stop bta done");
                  mContext.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      progressDialog.dismiss();
                      if(position == 3) {
                        checkVdaLicense(position, prevPosition);
                      } else {
                        handleMD(position, prevPosition, device);
                      }
                    }
                  });
                }
                @Override
                public void onFailure(Throwable t) {
                  // TODO: need retry before handleMD
                  Log.w(TAG, "stop bta error");
                  mContext.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      progressDialog.dismiss();
                      if(position == 3) {
                        checkVdaLicense(position, prevPosition);
                      } else {
                        handleMD(position, prevPosition, device);
                      }
                    }
                  });
                }
              });
        } else {
          dialog.dismiss();
        }
      }
    };
    builder.setNegativeButton(R.string.cancel, onClickListener);
    builder.setPositiveButton(R.string.ok, onClickListener);
    builder.show();
  }
  private void handleMD(final int position, final int prevPosition, final Device device) {
      mListener.getMotionDetection().setOldCopy();
      mListener.getMotionDetection().booleanValue = detectSwitch.isChecked();
      *//*
       * 20160830: Hoang: update secondaryBooleanValue for updating recording storage UI later.
       * The recording storage option is just available for MD mode now.
       * In BSC and BSD mode, app will ignore it.
       *//*
      mListener.getMotionDetection().secondaryBooleanValue = (recordStorageSpinner.getSelectedItemPosition() != RECORD_MOTION_OPT_OFF);
      Log.i(TAG, "handle motion detection changed, selected recording storage position? " + recordStorageSpinner.getSelectedItemPosition());
      mListener.getMotionDetection().intValue = detectSeekbar.getProgress();

      if (NotificationSettingUtils.supportMultiMotionTypes(device.getProfile().getModelId(), device.getProfile().getFirmwareVersion())) {
        Log.e(TAG, "set motion detection vda from " + prevPosition + " to " + position);
        mListener.onClickPositiveButtonVDa(position, detectSeekbar.getProgress(), prevPosition);
      } else {
        mListener.onClickPositiveButton(detectSwitch.isChecked(), detectSeekbar.getProgress());
      }
  }

  private void checkVdaLicense(final int position, final int prevPosition) {
    final ProgressDialog tempDialog = new ProgressDialog(getContext());
    tempDialog.setCancelable(false);
    tempDialog.setMessage(getSafeString(R.string.applying));
    tempDialog.show();
    // check license expire
    new Thread(new Runnable() {
      @Override
      public void run() {
        String cmdValue = position == 2 ? "check_vda_license_bsc" : "check_vda_license_bsd";
        Pair<String, Object> temp = mListener.getDevice().sendCommandGetValue(cmdValue, null, null);
        try {
          tempDialog.dismiss();
        } catch (Exception e) {
        }
        if (temp != null && temp.second != null && (temp.second instanceof Integer)) {
          Integer responseObj = (Integer) temp.second;
          if (responseObj != 1) {
            mContext.getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                motionDetectionVDA.setSelection(prevPosition);
                AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle(R.string.warning_license_expired_title)
                    .setMessage(Html.fromHtml(getSafeString(R.string.warning_license_expired)))
                    .setPositiveButton(R.string.dialog_ok, null).show();
                ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
              }
            });
          } else {
            mContext.getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                mListener.getMotionDetection().setOldCopy();
                mListener.getMotionDetection().booleanValue = detectSwitch.isChecked();
                mListener.getMotionDetection().intValue = detectSeekbar.getProgress();

                Device tempDevice = mListener.getDevice();
                if (NotificationSettingUtils.supportMultiMotionTypes(tempDevice.getProfile().getModelId(),
                    tempDevice.getProfile().getFirmwareVersion())) {
                  Log.e(TAG, "set motion detection vda from " + prevPosition + " to " + position);
                  mListener.onClickPositiveButtonVDa(position, detectSeekbar.getProgress(), prevPosition);
                } else {
                  mListener.onClickPositiveButton(detectSwitch.isChecked(), detectSeekbar.getProgress());
                }
              }
            });
          }
        }
      }
    }).start();
  }

  private void turnSchedulingOnOff(boolean enable, TurnScheduleTask.TurnScheduleListener listener) {
    String deviceId = mListener.getDevice().getProfile().registrationId;
    mScheduleData.setEnable(enable);

    Models.DeviceScheduleSubmit obj = new Models.DeviceScheduleSubmit();
    obj.setRegistrationId(deviceId);
    obj.setDeviceSchedule(mScheduleData);

    if (mDialog == null) {
      mDialog = new ProgressDialog(mContext.getActivity());
      mDialog.setCancelable(false);
    }
    if (enable) {
      mDialog.setMessage(getSafeString(R.string.enabling_motion_video_scheduling));
    } else {
      mDialog.setMessage(getSafeString(R.string.disabling_motion_video_scheduling));
    }
    mDialog.show();
    new Thread(new TurnScheduleTask(mContext.getActivity(), obj, listener)).start();
  }

  private String getSafeString(int stringRes) {
    return mContext != null ? mContext.getString(stringRes) : "";
  }

  private void setRecordHolderVisibility(int state) {
    recordHolder.setVisibility(state);
  }

  public void setListener(Listener listener) {
    mListener = listener;
  }

  private void setScheduleStorageSpinner(int index) {
    scheduleStorageSpinner.setSelection(index, false);
    if (index == 0) {
      txtScheduleNext.setVisibility(View.GONE);
      txtScheduleCurrent.setVisibility(View.GONE);
    } else {
      txtScheduleNext.setVisibility(View.VISIBLE);
      txtScheduleCurrent.setVisibility(View.VISIBLE);
    }
  }

  private void setRecordingPlanVisible(boolean isVisible) {
    if (isVisible) {
      recordingPlanHolder.setVisibility(View.VISIBLE);
    } else {
      recordingPlanHolder.setVisibility(View.GONE);
    }
  }

  private void setScheduleHolderVisibility(int state) {
    if (!BuildConfig.ENABLE_MVR_SCHEDULING) {
      scheduleHolder.setVisibility(View.GONE);
      return;
    }
    if (state != View.VISIBLE) {
      scheduleHolder.setVisibility(View.GONE);
    } else {
      // check firmware version
      if (mListener.getDevice().getProfile().isSupportMvrScheduling()) {
        scheduleHolder.setVisibility(View.VISIBLE);
      } else {
        scheduleHolder.setVisibility(View.GONE);
      }
    }
    if (scheduleHolder.getVisibility() == View.VISIBLE) {
      updateMvrTextView();
    }
  }

  private void goToMvrScheduleActivity() {
    Intent intent = new Intent(mContext.getActivity(), MvrScheduleActivity.class);
    intent.putExtra("regId", mListener.getDevice().getProfile().getRegistrationId());
    if (mScheduleData != null) {
      intent.putExtra("schedule", mScheduleData.getSchedule());
      intent.putExtra("drawnData", mScheduleData.getScheduleData());
    }
    mContext.startActivityForResult(intent, CameraSettingsFragment.FROM_MVR_SCHEDULE);
  }

  private void setSpinnerSelectionWithoutChangeEvent(int value) {
    recordStorageSpinner.setOnItemSelectedListener(null);
    recordStorageSpinner.setSelection(value, false);
    recordStorageSpinner.setOnItemSelectedListener(recordListener);
    previousSelectionValue = value;
  }

  private void disableMvr() {
    final Dialog mDialog = ProgressDialog.show(mContext.getActivity(), null, getSafeString(R.string.disabling_motion_video_recording));
    AsyncPackage.doInBackground(new Runnable() {
      @Override
      public void run() {
        final boolean success = mListener.getDevice().sendCommandGetSuccess("set_recording_parameter", "01", null);
        mContext.runOnUiThreadIfVisible(new Runnable() {
          @Override
          public void run() {
            CameraSettingsFragment.ListChild motionDetection = mListener.getMotionDetection();
            if (success) {
              setSpinnerSelectionWithoutChangeEvent(0);
              // setScheduleHolderVisibility(View.GONE);
              setRecordingPlanVisible(false);

              // Update setting for device
              motionDetection.secondaryBooleanValue = false;
            } else {
              setSpinnerSelectionWithoutChangeEvent(previousSelectionValue);
              mListener.getMotionDetection().secondaryIntValue = previousSelectionValue;
              // setScheduleHolderVisibility(View.VISIBLE);
            }
            try {
              mDialog.dismiss();
            } catch (Exception e) {
            }
            motionDetection.secondaryBooleanValue = !success;
            mListener.setupSoundOrMotionValueField(motionDetection);
          }
        });
      }
    });
  }

  private void enableMvrOnCloud() {
    if (BuildConfig.ENABLE_SUBSCRIPTIONS) {
      final ProgressDialog progressDialog = new ProgressDialog(mContext.getActivity());
      progressDialog.setIndeterminate(true);
      progressDialog.setCancelable(false);
      progressDialog.setMessage(getSafeString(R.string.enabling));
      progressDialog.show();
      AsyncPackage.doInBackground(new Runnable() {
        @Override
        public void run() {
          final boolean shouldEnableMVR = doSubscriptionFlow();
          mContext.runOnUiThreadIfVisible(new Runnable() {
            @Override
            public void run() {
              if (progressDialog != null) {
                if (!shouldEnableMVR) {
                  progressDialog.setMessage(getSafeString(R.string.disabling));
                }
              }
            }
          });
          try {
            DeviceSingleton.getInstance().update(false).get();
          } catch (Exception ignored) {
            ignored.printStackTrace();
          }
          CameraSettingsFragment.ListChild currentPlan = mListener.getCurrentPlan();
          if (currentPlan != null) {
            currentPlan.value = getSubscriptionPlanText();
          }
          mContext.runOnUiThreadIfVisible(new Runnable() {
            @Override
            public void run() {
              if (progressDialog != null) {
                progressDialog.hide();
              }
              CameraSettingsFragment.ListChild motionDetection = mListener.getMotionDetection();
              Log.i(TAG, "Should enable MVR: " + shouldEnableMVR);
              if (!shouldEnableMVR) {
                setSpinnerSelectionWithoutChangeEvent(previousSelectionValue);
                // setScheduleHolderVisibility(View.GONE);
              } else {
                Device device = mListener.getDevice();
                if (device != null) {
                  device.getProfile().getDeviceAttributes().setStorageMode("0");
                }
                setRecordingPlanVisible(false);
                // setScheduleHolderVisibility(View.VISIBLE);
              }
              motionDetection.secondaryBooleanValue = shouldEnableMVR;
              mListener.setupSoundOrMotionValueField(motionDetection);
            }
          });
        }
      });
    } else {
      //showOldFreeTrialDialog(buttonView);
      Log.i(TAG, "Show old free trial dialog here.");
    }
  }

  private void enableMvrOnSDCard() {
    Log.i("mbp", "Enable motion video recording on sd card");
    final ProgressDialog progressDialog = new ProgressDialog(mContext.getActivity());
    progressDialog.setIndeterminate(true);
    progressDialog.setCancelable(false);
    progressDialog.setMessage(getSafeString(R.string.switching_motion_recording_to_sdcard_storage));
    progressDialog.show();
    AsyncPackage.doInBackground(new Runnable() {
                                  @Override
                                  public void run() {
                                    try {
                                      // AA-1177: As Tho request, set_recording_parameter should call after set storage mode
                                      String apiKey = Global.getApiKey(mContext.getActivity());
                                      String regId = mListener.getDevice().getProfile().getRegistrationId();

                                      // send set storage mode on sdcard to server
                                      final Models.ApiResponse<Models.DeviceSettingData> response = Api.getInstance().getService().setDeviceSettings(apiKey, regId,
                                          "storage_mode", "1", "device", "1");
                                      final String status = response.getStatus();

                                      if (status.equals("200")) {
                                        Log.i(TAG, "Set storage mode on sdcard to server succeeded, enable motion recording on camera");
                                        if (mListener.getDevice().sendCommandGetSuccess("set_recording_parameter", "11", null)) {
                                          // setSpinnerSelectionWithoutChangeEvent(spinner, 2); ??? just comment it
                                          // successful case
                                          mContext.runOnUiThreadIfVisible(new Runnable() {
                                            @Override
                                            public void run() {
                                              previousSelectionValue = 2;
                                              Toast.makeText(mContext.getActivity(), getSafeString(R.string.switched_record_storage_mode_succeeded), Toast.LENGTH_SHORT).show();
                                              mListener.getMotionDetection().secondaryBooleanValue = true;
                                              Device device = mListener.getDevice();
                                              if (device != null) {
                                                device.getProfile().getDeviceAttributes().setStorageMode("1");
                                              }
                                              // show nest views
                                              setRecordingPlanVisible(true);
                                              // setScheduleHolderVisibility(View.VISIBLE);
                                        } else { // enable motion recording on camera failed
                                          mContext.runOnUiThreadIfVisible(new Runnable() {
                                            @Override
                                            public void run() {
                                              try {
                                                progressDialog.dismiss();
                                              } catch (Exception e) {
                                              }
                                              Toast.makeText(mContext.getActivity(), getSafeString(R.string.switched_record_storage_mode_failed), Toast.LENGTH_SHORT).show();
                                            }
                                          });
                                        }
                                      } else {
                                        Log.i(TAG, "Set storage mode on sdcard to server succeeded " + response.getMessage() + " old value: " + previousSelectionValue);
                                        mContext.runOnUiThreadIfVisible(new Runnable() {
                                          @Override
                                          public void run() {
                                            setSpinnerSelectionWithoutChangeEvent(previousSelectionValue);
                                            Toast.makeText(mContext.getActivity(), response.getMessage(), Toast.LENGTH_SHORT).show();
                                            if (previousSelectionValue == RECORD_MOTION_OPT_OFF) { //  from off to sd card
                                              mListener.getMotionDetection().secondaryBooleanValue = false;
                                            } else { // from cloud to sd card
                                              mListener.getMotionDetection().secondaryBooleanValue = true;
                                            }
                                            // hide nest views
                                            setRecordingPlanVisible(false);
                                            // setScheduleHolderVisibility(View.GONE);
                                          }
                                        });
                                      }
                                      try {
                                        progressDialog.dismiss();
                                      } catch (Exception e) {
                                        e.printStackTrace();
                                      }
                                    } catch (final RetrofitError e) {
                                      e.printStackTrace();
                                      mContext.runOnUiThreadIfVisible(new Runnable() {
                                        @Override
                                        public void run() {
                                          if (e.getResponse() != null && e.getResponse().getBody() != null) {
                                            TypedByteArray body = (TypedByteArray) e.getResponse().getBody();
                                            String bodyText = new String(body.getBytes());
                                            Toast.makeText(mContext.getActivity(), getSafeString(R.string.switched_record_storage_mode_failed), Toast.LENGTH_SHORT).show();
                                            setSpinnerSelectionWithoutChangeEvent(previousSelectionValue);
                                            Log.i(TAG, "SET SDCARD RECORDING ERROR: " + bodyText);
                                          }
                                          // hide nest views
                                          setRecordingPlanVisible(false);
                                          // setScheduleHolderVisibility(View.GONE);
                                          try {
                                            progressDialog.dismiss();
                                          } catch (Exception e1) {
                                            e1.printStackTrace();
                                          }
                                        }
                                      });
                                    } catch (final Exception ex) {
                                      ex.printStackTrace();
                                    }
                                  }

    );
  }

  private boolean doSubscriptionFlow() {
    String savedToken = Global.getApiKey(mContext.getActivity());
    if (savedToken == null) {
      return false;
    }
    SubscriptionWizard subWizard = new SubscriptionWizard(savedToken, mContext.getActivity(), mListener.getDevice(), true);
    try {
      return subWizard.verify().get();
    } catch (Exception e) {
      return false;
    }
  }

  private String getSubscriptionPlanText() {
    String subscriptionText = getSafeString(R.string.none);
    if (BuildConfig.ENABLE_SUBSCRIPTIONS) {
      try {
        DeviceProfile profile = mListener.getDevice().getProfile();
        if (profile != null) {
          if (profile.getPlanId() != null && !profile.getPlanId().equalsIgnoreCase("freemium")) {
            if (profile.getDeviceFreeTrial() != null) {
              if (profile.getDeviceFreeTrial().isActive()) {
                subscriptionText = getSafeString(R.string.free_trial);
              } else if (profile.getPlanId() != null) {
                subscriptionText = profile.getPlanId();
              } else {
                subscriptionText = getSafeString(R.string.none);
              }
            } else if (profile.getPlanId() != null) {
              subscriptionText = profile.getPlanId();
            }
          } else {
            subscriptionText = getSafeString(R.string.none);
          }
        }
      } catch (Exception ignored) {
      }
    }
    return subscriptionText;
  }

  private void updateMvrTextView() {
    if (mScheduleData == null || !mScheduleData.isEnable()) {
      txtScheduleCurrent.setVisibility(View.GONE);
      txtScheduleNext.setVisibility(View.GONE);
    } else {
      txtScheduleNext.setVisibility(View.VISIBLE);
      txtScheduleNext.setText(getNextMvrSchedule());
      String currentSchedule = getCurrentMvrSchedule();
      if (TextUtils.isEmpty(currentSchedule)) {
        txtScheduleCurrent.setVisibility(View.GONE);
      } else {
        txtScheduleCurrent.setVisibility(View.VISIBLE);
        txtScheduleCurrent.setText(currentSchedule);
      }
    }
  }

  public String getNextMvrSchedule() {
    if (mScheduleData != null && mScheduleData.isEnable() && scheduleHasElements()) {
      HashMap<String, ArrayList<String>> scheduleData = mScheduleData.getScheduleData();

      Calendar calendar = Calendar.getInstance();
      int keyPosition = calendar.get(Calendar.DAY_OF_WEEK) - 1;
      int now = Integer.parseInt(new SimpleDateFormat("HHmm").format(new Date()));
      // if it is 9:10 AM, now will be 910
      now = keyPosition * 10000 + now;
      // if to day is wednesday, now will become 30910

      // find from today to saturday
      for (int i = keyPosition; i < PublicDefine.KEYS.length; i++) {
        if (!scheduleData.containsKey(PublicDefine.KEYS[i])) {
          continue;
        }
        String[] times = findNextMvrSchedule(now, i, scheduleData.get(PublicDefine.KEYS[i]));
        if (times == null) {
          continue;
        }
        Log.i("debug", "Next MVR: " + PublicDefine.KEYS[i] + " " + times[0] + " -> " + times[1]);
        String capitalDay = Util.getStringByName(mContext.getActivity(), PublicDefine.KEYS[i]);
        return String.format(getSafeString(R.string.next_mvr_scheduling_for), capitalDay, times[0], times[1]);
      }

      // find from Sunday to yesterday
      for (int i = 0; i < keyPosition; i++) {
        if (!scheduleData.containsKey(PublicDefine.KEYS[i])) {
          continue;
        }
        String[] times = findNextMvrSchedule(now, i + 7, scheduleData.get(PublicDefine.KEYS[i]));
        if (times == null) {
          continue;
        }
        Log.i("debug", "Next MVR: " + PublicDefine.KEYS[i] + " " + times[0] + " -> " + times[1]);
        String capitalDay = Util.getStringByName(mContext.getActivity(), PublicDefine.KEYS[i]);
        return String.format(getSafeString(R.string.next_mvr_scheduling_for), capitalDay, times[0], times[1]);
      }

      // there is no next schedule
      String day = Util.getStringByName(mContext.getActivity(), PublicDefine.KEYS[keyPosition]);
      return String.format(getSafeString(R.string.next_mvr_scheduling_no), day);
    }
    Log.i("debug", "Next MVR: no existing");
    return getSafeString(R.string.always_detecting_mvr_events);
  }

  private String[] findNextMvrSchedule(int now, int plusForDay, ArrayList<String> arrTemp) {
    for (String temp : arrTemp) {
      String[] split = temp.split("-");
      int from = Integer.parseInt(split[0]) + plusForDay * 10000;
      int to = Integer.parseInt(split[1]) + plusForDay * 10000;
      if (now < from && now < to) {
        return new String[]{getTimeDisplayString(split[0]), getTimeDisplayString(split[1])};
      }
    }
    return null;
  }

  private String getCurrentMvrSchedule() {
    if (mScheduleData != null && mScheduleData.isEnable() && scheduleHasElements()) {
      HashMap<String, ArrayList<String>> scheduleData = mScheduleData.getScheduleData();

      Calendar calendar = Calendar.getInstance();
      int keyPosition = calendar.get(Calendar.DAY_OF_WEEK) - 1;
      int now = Integer.parseInt(new SimpleDateFormat("HHmm").format(new Date()));
      // if it is 9:10 AM, now will be 910

      if (scheduleData.containsKey(PublicDefine.KEYS[keyPosition])) {
        ArrayList<String> arrTemp = scheduleData.get(PublicDefine.KEYS[keyPosition]);
        for (String temp : arrTemp) {
          String[] split = temp.split("-");
          int from = Integer.parseInt(split[0]);
          int to = Integer.parseInt(split[1]);
          if (now >= from && now < to) {
            String capitalDay = Util.getStringByName(mContext.getActivity(), PublicDefine.KEYS[keyPosition]);
            String pattern = getSafeString(R.string.current_mvr_scheduling_for);
            return String.format(pattern, capitalDay, getTimeDisplayString(split[0]), getTimeDisplayString(split[1]));
          }
        }
      }
      String capitalDay = Util.getStringByName(mContext.getActivity(), PublicDefine.KEYS[keyPosition]);
      return String.format(getSafeString(R.string.current_mvr_scheduling_no), capitalDay);
    }
    return null;
  }

  private boolean scheduleHasElements() {
    boolean result = false;
    if (mScheduleData != null && mScheduleData.isEnable()) {
      HashMap<String, ArrayList<String>> scheduleData = mScheduleData.getScheduleData();
      for (String day : PublicDefine.KEYS) {
        if (scheduleData.containsKey(day)) {
          ArrayList<String> elements = scheduleData.get(day);
          if (elements != null && elements.size() > 0) {
            result = true;
            break;
          }
        }
      }
    }
    return result;
  }


  *//**
   * @param time in format 'HHmm'
   *//*
  private String getTimeDisplayString(String time) {
    int hour = Integer.parseInt(time.substring(0, 2));
    int minute = Integer.parseInt(time.substring(2));
    int timeFormat = settings.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0);
    String temp = "";
    if (timeFormat == 0) {
      temp = hour >= 12 ? getSafeString(R.string.half_day_pm) : getSafeString(R.string.half_day_am);
      if (hour > 12) {
        hour = hour - 12;
      }
    } else {
      temp = "";
    }

    return String.format("%d:%02d %s", hour, minute, temp);
  }*/
}
