package com.blinkhd.playback;

import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.command.CameraCommandUtils;
import com.hubble.devcomm.Device;
import com.hubble.file.FileService;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.registration.Util;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubble.ui.ViewFinderFragment;
import com.hubbleconnected.camera.R;
import com.util.AppEvents;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import base.hubble.PublicDefineGlob;

/**
 * Created by sonikas on 17/10/16.
 */
public class CaptureFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "CaptureFragment";
    private Activity mActivity;
    private boolean mIsPortrait = false, mIsRecording = false, mCanStoreToSD = false, mIsPhoneStorage = true;
    private boolean mIsLoading = false;
    private Device mDevice;
    private ImageView mRecordImage, mGalleryImage, mCaptureImage;
    private Resources mResources;
    private TextView mTvRecordingState;
    private RecordMode mRecordMode;
    private Handler mHandler;
    private ProgressBar progressBarStorage;
    private Fragment mParentFragment;
    private EventData eventData;
    private TextView mCaptureText,mGalleryText;

    private enum RecordMode {
        RECORD_ON, RECORD_OFF, SNAPSHOT
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.capture_img:
                FileService.setFormatedFilePath(System.currentTimeMillis());
                mRecordMode=RecordMode.SNAPSHOT;
                Toast.makeText(mActivity, mResources.getString(R.string.saved_photo), Toast.LENGTH_SHORT).show();
                mCaptureImage.setEnabled(false);
                Runnable runn = new Runnable() {
                    @Override
                    public void run() {
                        Util.mPicExtension = ".png";
                        ((ViewFinderFragment)mParentFragment).onSnap();
                        enableTriggerButtonAfterDelay(mCaptureImage);
                    }
                };
                Thread worker = new Thread(runn);
                worker.start();
                AnalyticsInterface.getInstance().trackEvent("snapshot","Took_Snapshots",eventData);
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_PICTURE_CLICKED,AppEvents.VF_PICTURE_CLICKED);
                ZaiusEvent pictureEvt = new ZaiusEvent(AppEvents.VF_PICTURE);
                pictureEvt.action(AppEvents.VF_PICTURE_CLICKED);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(pictureEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.gallery_img:
                ((ViewFinderFragment)mParentFragment).showGallery();
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_GALLERY_CLICKED,AppEvents.VF_GALLERY_CLICKED);
                ZaiusEvent galleryEvt = new ZaiusEvent(AppEvents.VF_GALLERY_LAUNCHED);
                galleryEvt.action(AppEvents.VF_GALLERY_CLICKED);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(galleryEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.record_img:
                //if mode is snapshot or record off
                if (mRecordMode!=RecordMode.RECORD_ON) {
                    if (!mIsRecording) {
                        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_VIDEO_CLICKED,AppEvents.VF_VIDEO_CLICKED);
                        ZaiusEvent vfVideoEvt = new ZaiusEvent(AppEvents.VF_VIDEO);
                        vfVideoEvt.action(AppEvents.VF_VIDEO_CLICKED);
                        try {
                            ZaiusEventManager.getInstance().trackCustomEvent(vfVideoEvt);
                        } catch (ZaiusException e) {
                            e.printStackTrace();
                        }


                        FileService.setFormatedFilePath(System.currentTimeMillis());
                        //take snapshot
                        Runnable captureRun = new Runnable() {
                            @Override
                            public void run() {
                                Util.mPicExtension = ".jpg";
                                ((ViewFinderFragment)mParentFragment).onSnap();
                                //enableTriggerButtonAfterDelay(mCaptureImage);
                            }
                        };
                        Thread captureThread=new Thread(captureRun);
                        captureThread.start();
                        disableAllButtons();
                        //start recording
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                startRecording();
                                enableTriggerButtonAfterDelay(mRecordImage);
                            }
                        };
                        /*Thread thread = new Thread(runnable);
                        thread.start();*/
                        new Handler().postDelayed(runnable,1000);
                        AnalyticsInterface.getInstance().trackEvent("Performed_manual_recording","Performed_manual_recording",eventData);
                    }
                }else if (mRecordMode == RecordMode.RECORD_ON) {
                    if (mIsRecording) {
                        disableAllButtons();
                        Runnable runable = new Runnable() {
                            @Override
                            public void run() {
                                stopRecording();
                                enableAllButtons();

                            }
                        };
                        Thread thrd = new Thread(runable);
                        thrd.start();
                    }
                }
                break;
            case R.id.close_capture:
                ((ViewFinderFragment)mParentFragment).removeFragment(this);
                break;
        }

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
        eventData = new EventData();
        mHandler = new Handler();
        mActivity=getActivity();
        mResources=mActivity.getResources();
        mParentFragment=getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.capture_fragment, container, false);

        mRecordMode = RecordMode.SNAPSHOT;
        new CheckCameraStorageCapabilitiesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mDevice);

        mGalleryImage = (ImageView) view.findViewById(R.id.gallery_img);
        mRecordImage = (ImageView) view.findViewById(R.id.record_img);
        mCaptureImage = (ImageView) view.findViewById(R.id.capture_img);
        mTvRecordingState = (TextView) view.findViewById(R.id.record_time);
        mCaptureText=(TextView)view.findViewById(R.id.capture_txt);
        mGalleryText=(TextView)view.findViewById(R.id.gallery_txt);

        ImageView closeImage=(ImageView)view.findViewById(R.id.close_capture);
        closeImage.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mParentFragment=getParentFragment();
        mCaptureImage.setOnClickListener(this);
        mRecordImage.setOnClickListener(this);
        mGalleryImage.setOnClickListener(this);
        //setOrientation(mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

    }


    @Override
    public void onPause(){
        if(mIsRecording) {
            stopRecording();
        }
        super.onPause();
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void setDevice(Device device) {
        mDevice = device;
    }

    public void setRecording(boolean isRec) {
        mIsRecording = isRec;
    }

    private void enableTriggerButtonAfterDelay(final ImageView image) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    image.setEnabled(true);
                } catch (Exception ignored) {
                }
            }
        }, 250);
    }

    private void disableAllButtons(){
        mCaptureImage.setEnabled(false);
        mGalleryImage.setEnabled(false);
        mRecordImage.setEnabled(false);
        mCaptureImage.setImageResource(R.drawable.capture_disabled);
        mGalleryImage.setImageResource(R.drawable.gallery_disabled);
        mCaptureText.setTextColor(ContextCompat.getColor(getActivity(),R.color.disabled_white));
        mGalleryText.setTextColor(ContextCompat.getColor(getActivity(),R.color.disabled_white));
    }

    private void enableAllButtons(){

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mCaptureImage.setEnabled(true);
                    mGalleryImage.setEnabled(true);
                    mRecordImage.setEnabled(true);
                    mCaptureImage.setImageResource(R.drawable.capture);
                    mGalleryImage.setImageResource(R.drawable.gallery);
                    mCaptureText.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));
                    mGalleryText.setTextColor(ContextCompat.getColor(getActivity(),R.color.white));
                } catch (Exception ignored) {
                }
            }
        }, 250);

    }

    private void startRecording() {
        if (mActivity != null) {
            mIsRecording = true;
            ((ViewFinderFragment)getParentFragment()).setRecord(mIsRecording,mIsPhoneStorage);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mRecordImage.setImageResource(R.drawable.recording);
                    mRecordMode = RecordMode.RECORD_ON;

                }
            });
        }
    }

    public void stopRecording() {
        if (mActivity != null) {
            mIsRecording = false;
            ((ViewFinderFragment)mParentFragment).setRecord(mIsRecording, mIsPhoneStorage);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                  /*  ivStorage.setVisibility(View.VISIBLE);
                    if (isPortrait) {
                        ivMode.setImageResource(R.drawable.camera_action_pic_s);
                    } else {
                        ivMode.setImageResource(R.drawable.camera_action_pic_s_landscape);
                    }*/
                    mRecordImage.setImageResource(R.drawable.record);
                    mRecordMode = RecordMode.RECORD_OFF;
                    mTvRecordingState.setText("");

                }
            });

        }
    }

    public void updateRecordingTime(int time) {
        if (getView() != null) {
            int hours = time / 3600;
            int minutes = (time % 3600) / 60;
            int seconds = time % 60;
            String strTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            mTvRecordingState.setText(strTime);
        }
    }

    private class CheckCameraStorageCapabilitiesTask extends AsyncTask<Device, Void, Boolean> {
        protected void onPreExecute() {
            super.onPreExecute();
            mIsLoading = true;

        }
        @Override
        protected Boolean doInBackground(Device... params) {
            boolean isSupportSdCard = false;
            Device device = params[0];
            if (device != null) {
                if (device.getProfile().doesHaveSDStorage()) {
                    if (device.getProfile().getDeviceLocation().getLocalIp() != null) {
                        final String device_address_port = device.getProfile().getDeviceLocation().getLocalIp();
                        Log.d(TAG, "Checking SD card free...");
                        String httpAddress = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, "/?action=command&command=", PublicDefineGlob.GET_SD_CARD_FREE);
                        //String sdCardResponse = HTTPRequestSendRecvTask.sendRequest_block_for_response_with_timeout(httpAddress, null, null, "20000");
                        String sdCardResponse = CameraCommandUtils.sendCommandGetFullResponse(device, PublicDefineGlob.GET_SD_CARD_FREE, null, null);
                        Log.d(TAG, "Check SD card free response: " + sdCardResponse);
                        isSupportSdCard = sdCardResponse != null && sdCardResponse.startsWith(PublicDefineGlob.GET_SD_CARD_FREE) && !sdCardResponse.contains("-1");
                    } else {
                        Log.d(TAG, "Check SD card free: local ip is null");
                    }
                } else {
                    Log.d(TAG, "Camera does not support SD card, dont need to check sdcard free");
                }
            } else {
                Log.d(TAG, "Check SD card free: device is null");
            }
            return new Boolean(isSupportSdCard);
        }

        @Override
        protected void onPostExecute(final Boolean canStoreSD) {
            super.onPostExecute(canStoreSD);
            mIsLoading = false;
            if (mActivity != null) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCanStoreToSD = canStoreSD;
                        //todo check storage login
                       /* if (ivStorage != null && mRecordMode == RecordMode.RECORD_OFF) {
                            ivStorage.setVisibility(View.VISIBLE);
                            if (!mCanStoreToSD) { // only phone mode
                                ivStorage.setImageResource(R.drawable.phone_storage);
                            }
                        } else {
                            ivStorage.setVisibility(View.GONE);
                        }*/
                        if(progressBarStorage != null) {
                            progressBarStorage.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    }


}
