package com.media.ffmpeg;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.blinkhd.playback.LoadPlaylistListener;
import com.blinkhd.playback.LoadPlaylistTask;
import com.blinkhd.playback.RequestRelayFileStreamService;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.command.CameraCommandUtils;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.file.FileService;
import com.hubble.helpers.AsyncPackage;
import com.hubble.registration.PublicDefine;
import com.hubble.streaming.sdcard.RtmpFileStreamingHandler;
import com.hubble.streaming.sdcard.RtmpFileStreamingJobBasedTask;
import com.hubbleconnected.camera.R;

import com.hubble.util.CameraFeatureUtils;
import com.koushikdutta.async.future.FutureCallback;
import com.msc3.Streamer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.command.PublishCommandRequestBody;
import base.hubble.command.RemoteCommandRequest;
import base.hubble.constants.Streaming;
import base.hubble.meapi.device.GeneralData;
import base.hubble.meapi.device.TimelineEvent;

/**
 * Created by CVision on 12/11/2015.
 */
public class RecordStreamVideo extends FragmentActivity implements LoadPlaylistListener,RtmpFileStreamingHandler {

    private static final String TAG = "RecordStreamVideo";
    private static final int QUERY_PLAYLIST_INTERVAL = 5000;
    public static RecordStreamVideo instance;
    private FFMpegPlayer mPlayer;
    private Thread initializing_thrd = null;
    private String mSdcardStreamingUrl;
    private String rtmp;
    private boolean isInitilizing = false;
    private boolean shouldInterrupt = false;
    private String device_mac;
    private String eventCode;
    private String md5Sum;
    private String clipName;
    private String cameraName;
    private ProgressDialog downloadingDialog;
    private String pathFileNameVideo;
    private SecureConfig settings = HubbleApplication.AppConfig;
    private long timeStamp;
    private Timer mQueryPlaylistTask;
    private List<String> mPlaylist;
    private int type;

    private String mModelId, mFwVersion;
    private boolean shouldStopLoading;
    private boolean isClosingRelayFileStream = false;
    private RtmpFileStreamingJobBasedTask mRtmpFileStreamingTask;

    private Handler mHanler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Streamer.MSG_CAMERA_IS_NOT_AVAILABLE:
                    Log.e(TAG, "File streaming is not available!");
                    release(true, false);
                    break;

                case Streamer.MSG_VIDEO_STREAM_HAS_STOPPED:
                    Log.d(TAG, "File streaming has stopped");
                    release(false, true);
                    break;

                case Streamer.MSG_VIDEO_STREAM_HAS_STARTED:
                    Log.d(TAG, "File streaming has started");
                    break;

                case Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY:
                    Log.e(TAG, "Viewing relay sdcard clip stopped, stop loading immediately");
                    break;
            }
            return false;
        }
    });

    private void setRtmpURL() {
        /*
         * 20161222 HOANG IMPORTANT
         * Issue: when there's no live streaming session started before, so files are still not loaded yet -> UnsatisfiedLinkError
         * Solution: Should create FFMpeg instance for loading so files to memory to avoid UnsatisfiedLinkError
         */
        try {
            FFMpeg ffmpeg = new FFMpeg();
        } catch (FFMpegException e) {
            e.printStackTrace();
        }
        Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    mPlayer = new FFMpegPlayer(mHanler, true, false);
                    mPlayer.setRecordModeEnabled(true);

                    ArrayList<String> playlist = new ArrayList<String>();
                    playlist.add(rtmp);
                    mPlayer.updatePlaylist(playlist);
                    mPlayer.finishLoadingPlaylist(true);
                    try {
                        mPlayer.setPlayOption(FFMpegPlayer.MEDIA_STREAM_SHOW_DURATION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    boolean isInitializedSucceeded = false;
                    int retries = 20;
                    do {
                        mHanler.post(new Runnable() {
                            @Override
                            public void run() {
                                initializeVideo();
                            }
                        });
                        isInitializedSucceeded = true;
                        break;
                    }

                    while (retries-- > 0);
                    // send message not available to handler
                    if (!isInitializedSucceeded) {
                        release(true, false);
                    }
                }
            }
        );

        worker.start();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pathFileNameVideo = FileService.getFormatedFilePathForVideo(cameraName).getAbsolutePath();
        mPlaylist = new ArrayList<>();
        mSdcardStreamingUrl = null;
        rtmp = null;
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        Log.d(TAG, "Start recording video stream...");
        if (extra != null) {
            device_mac = extra.getString(Streaming.EXTRA_REGISTRATION_ID);
            eventCode = extra.getString(Streaming.EXTRA_EVENT_CODE);
            clipName = extra.getString(Streaming.EXTRA_CLIP_NAME);
            md5Sum = extra.getString(Streaming.EXTRA_MD5_SUM);
            cameraName = extra.getString(Streaming.CAMERA_NAME);
            timeStamp = System.currentTimeMillis();//extra.getLong(Streaming.EVENT_TIME_STAMP);
            pathFileNameVideo = FileService.getFormatedFilePathForVideo(cameraName).getAbsolutePath();
            type = extra.getInt(PublicDefine.PREFS_DOWNLOAD_FOR, PublicDefine.DOWNLOAD_FOR_SHARING);
            dialogWaitingRecord();

            Log.d(TAG, "event code: " + eventCode + " camera name: " + cameraName);

            /*
             * 20161212 HOANG AA-2215
             * Support RTMP file streaming job based.
             */
            Device selectedDevice = DeviceSingleton.getInstance().getDeviceByRegId(device_mac);
            if (selectedDevice != null) {
                if (selectedDevice.getProfile() != null) {
                    mModelId = selectedDevice.getProfile().getModelId();
                    mFwVersion = selectedDevice.getProfile().getFirmwareVersion();
                    Log.d(TAG, "View event for device " + device_mac + ", model id: " + mModelId + ", fwVersion: " + mFwVersion);
                } else {
                    Log.e(TAG, "Device " + device_mac + " has empty profile");
                }
            } else {
                Log.e(TAG, "Device " + device_mac + " not found");
            }
            if (CameraFeatureUtils.doesSupportRtmpFileStreamJobBased(mModelId, mFwVersion)) {
                startRtmpFileStreamJobBasedTask(device_mac, clipName, md5Sum);
            } else {
                startPlayRelayFileStream(device_mac, clipName, md5Sum);
            }
        } else {
            Log.d(TAG, "Start recording video stream failed, extra is null");
        }
    }

    private void initializeVideo() {
        try {
            // if player is releasing, not need to initialize video anymore
            if (mPlayer != null && !shouldInterrupt) {
                initializing_thrd = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        boolean hasInitialized = false;
                        int retries = 3;

                        while (retries > 0 && !shouldInterrupt) {
                            synchronized (this) {
                                if (mPlayer == null) {
                                    return;
                                }

                                isInitilizing = true;
                                try {
                                    mPlayer.setDataSource(rtmp);
                                    mPlayer.prepare();

                                    isInitilizing = false;
                                    hasInitialized = true;
                                    break;
                                } catch (IllegalArgumentException | IllegalStateException e) {
                                    Log.e(TAG, "Couldn't prepare player: " + e.getMessage());
                                } catch (IOException e) {
                                    Log.e(TAG, "IO Exception Couldn't prepare player: " + e.getMessage());
                                }
                                isInitilizing = false;

                                int waiting = 0;
                                while (waiting++ < 3 && !shouldInterrupt) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        // // Log.e(TAG, Log.getStackTraceString(e));
                                    }
                                }

                                Log.e(TAG, "Initializing player failed...Retries: " + retries);
                                retries--;
                            }
                        }

                        if (!shouldInterrupt) {
                            if (hasInitialized) {
                                // Initialize successfully
                                startRecordVideo();
                            } else {
                                release(true, false);
                            }
                        } else {
                            shouldInterrupt = false;
                        }
                    }

                });
                initializing_thrd.start();
            }

        } catch (IllegalStateException | IllegalArgumentException e) {
            Log.e(TAG, "Couldn't prepare player: " + e.getMessage());
        }
    }

    private void startRecordVideo() {
        synchronized (this) {
            mPlayer.start();
            mPlayer.startRecording(pathFileNameVideo);
        }
    }

    public void release(boolean error, boolean share) {
        /*if (mPlayer != null) {
            Log.d(TAG, "releasing ...");
            shouldInterrupt = true;
            mPlayer.suspend();
            synchronized (this) {
                try {
                    mPlayer.stop();
                    while (isInitilizing) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    }
                    mPlayer.release();
                    mPlayer = null;
                } catch (Exception e) {
                } finally {
                    if (mPlayer != null) {
                        mPlayer.release();
                        mPlayer = null;
                    }
                }
            }
            shouldInterrupt = false;
            Log.d(TAG, "player released");*/
            if (error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),getString(R.string.download_firmware_error), Toast.LENGTH_LONG).show();
                        downloadingDialog.dismiss();
                        stopPlayer();
                        stopPlayback();
                        finish();
                    }
                });
            } else  {
                if (share) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            File videoFile = new File(pathFileNameVideo);
                            if (videoFile.exists()) {
                                downloadingDialog.dismiss();
                                if (type == PublicDefine.DOWNLOAD_FOR_SHARING) {
                                /*String additional = getResources().getString(R.string.vtech_inform_view_file);
                                Intent shareIntent = FileService.getShareIntent(videoFile, additional);*/
                                    Uri contentUri = FileService.getFileUri(videoFile);
                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                    shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    shareIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    shareIntent.setType("video/flv");
                                    startActivity(shareIntent);
                                } else if(type == PublicDefine.DOWNLOAD){
                                    Toast.makeText(getApplicationContext(), getString(R.string.download_success), Toast.LENGTH_LONG).show();
                                } else if (type == PublicDefine.DOWNLOAD_FOR_DELETING) {
                                    Log.d(TAG, "download file complete ...");
                                    setResult(Activity.RESULT_OK);
                                }
                                stopPlayer();
                                stopPlayback();
                                RecordStreamVideo.this.finish();
                            }
                        }
                    });
                }
            }
        //}
    }

    private void requestFileStreamToServer(final String fileName, final String md5Sum) {
        Intent msgIntent = new Intent(this, RequestRelayFileStreamService.class);
        msgIntent.setAction(RequestRelayFileStreamService.ACTION_RELAY_FILE_STREAM_REQUEST);
        final String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
        msgIntent.putExtra(RequestRelayFileStreamService.API_KEY, apiKey);
        msgIntent.putExtra(RequestRelayFileStreamService.MD5_SUM, md5Sum);
        msgIntent.putExtra(RequestRelayFileStreamService.DEVICE_MAC, device_mac);
        msgIntent.putExtra(RequestRelayFileStreamService.FILE_NAME, fileName);
        msgIntent.putExtra(RequestRelayFileStreamService.USE_JOB_BASED, CameraFeatureUtils.doesSupportRtmpFileStreamJobBased(mModelId, mFwVersion));
        startService(msgIntent);
    }

    @Override
    public void onRemoteCallSucceeded(TimelineEvent latest, TimelineEvent[] allEvents) {
        if (latest != null) {
            GeneralData[] playlist = latest.getData();
            if (playlist != null && playlist.length > 0) {
                for (int i = 0; i < playlist.length; i++) {
                    if (playlist[i] != null && (playlist[i].getFile() != null)) {
                        String filePath = playlist[i].getFile();

                        if (!filePath.isEmpty() && isSupportedFormat(filePath)) {
                            if (i < mPlaylist.size()) {
                                mPlaylist.set(i, filePath);
                            } else {
                                if (i > 0) {
                                    /* Send file stream request for new file */
                                    requestFileStreamToServer(filePath, playlist[i].getMd5Sum());
                                }
                                mPlaylist.add(filePath);
                            }
                        }
                    }
                }

                if (TextUtils.isEmpty(rtmp)) {
                    // HOANG Just start RTMP stream once. This code won't be called next time.
                    rtmp = mSdcardStreamingUrl;
                    setRtmpURL();
                }
            } else {
                Log.d(TAG, "onRemoteCallSucceeded event is null");
            }

            if (mPlaylist.size() > 0) {
                // check whether the playlist is complete
                String last_file = mPlaylist.get(mPlaylist.size() - 1);
                Log.i(TAG, "Last clip of playlist: " + last_file);
                if (isLastClip(last_file)) {
                    Log.i(TAG, "LAST CLIP OF MOTION VIDEO: " + last_file);
                    // the playlist is complete --> stop loading
                    shouldStopLoading = true;
                }
            }
        } else {
            Log.d(TAG, "onRemoteCallSucceeded latest event is null");
        }

        if (!shouldStopLoading) {
            try {
                // try catch Timer was cancelled error
                mQueryPlaylistTask.schedule(new QueryPlaylistTask(), QUERY_PLAYLIST_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRemoteCallFailed(int errorCode) {
        if (mQueryPlaylistTask != null) {
            Log.d(TAG, "Load playlist timeout...try to reload...");
            mQueryPlaylistTask.schedule(new QueryPlaylistTask(), QUERY_PLAYLIST_INTERVAL);
        }
    }

    private void startRtmpFileStreamJobBasedTask(final String deviceMac, final String clipName, final String md5Sum) {
        Log.d(TAG, "Start RTMP file stream job based task");
        final String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
        mRtmpFileStreamingTask = new RtmpFileStreamingJobBasedTask();
        mRtmpFileStreamingTask.setRtmpFileStremingHandler(this);
        mRtmpFileStreamingTask.setApiKey(apiKey);
        mRtmpFileStreamingTask.setRegistrationId(deviceMac);
        mRtmpFileStreamingTask.setClientType("android");
        mRtmpFileStreamingTask.setClipName(clipName);
        mRtmpFileStreamingTask.setMd5Sum(md5Sum);
        mRtmpFileStreamingTask.execute();
    }

    public void startPlayRelayFileStream(final String device_mac, final String clip_name, final String md5_sum) {
        final String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
        AsyncPackage.doInBackground(new Runnable() {
            @Override
            public void run() {
                int retry = 3;
                while (retry-- > 0) {
                    Log.d(TAG, "Close relay RTMP...");
                    // 20160825: HOANG: don't try catch whole method, it would skip some codes if there is any exception
                    // Just try..catch.. on a specific method.
                    PublishCommandRequestBody.Builder builder = new PublishCommandRequestBody.Builder();
                    builder.setCommand("close_relay_rtmp");
                    PublishCommandRequestBody requestBody = builder.create();
                    RemoteCommandRequest request = new RemoteCommandRequest();
                    request.setApiKey(apiKey);
                    request.setRegistrationId(device_mac);
                    request.setPublishCommandRequestBody(requestBody);
                    CameraCommandUtils.sendRemoteCommand(request);

                    Log.d(TAG, "Close relay RTMP...DONE");
                    Log.i(TAG, "Create record file stream for file: " + clip_name);
                    try {
                        final Models.ApiResponse<Models.CreateFileStreamResponse> response =
                                Api.getInstance().getService().createFileSession(apiKey, device_mac, "1", new Models.CreateFileSessionRequest("android", clip_name, md5_sum, null));
                        if (response != null) {
                            final String status = response.getStatus();
                            Log.e(TAG, "Create record file stream status: " + response.toString() + " -> " + status + " retries: " + retry);
                            if (status == null || !status.equals("200")) {
                                if (retry == 0) {
                                    if (response.getData() != null && response.getData().getDeviceResponse() != null && response.getData().getDeviceResponse().getBody() != null) {
                                        Pattern errorPattern = Pattern.compile("(.*)(error=)(\\d+)(,desc=)(\\w+)(.*)");
                                        Matcher errorMatch = errorPattern.matcher(response.getData().getDeviceResponse().getBody());
                                        Log.i(TAG, response.getData().getDeviceResponse().getBody());
                                        String errorCode = "0";
                                        String desc = "unknown";
                                        if (errorMatch.matches()) {
                                            errorCode = errorMatch.group(3);
                                            desc = errorMatch.group(5);
                                        }
                                        String errorMsg = String.format(getString(R.string.cant_open_file_with_error), response.getStatus(), errorCode, desc);
                                        Log.e(TAG, errorMsg);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), getString(R.string.download_firmware_error), Toast.LENGTH_LONG).show();
                                                finish();
                                                Log.e(TAG, "Create record file stream failed");
                                            }
                                        });
                                    } else {
                                        Log.e(TAG, "Error");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), dismissErroMessage(), Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                        });
                                    }
                                    break;
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (status.equals("200")) {
                                            mSdcardStreamingUrl = response.getData().getURL();
                                            Log.i(TAG, "Create file stream succeeded, url: " + mSdcardStreamingUrl);
                                            mQueryPlaylistTask = new Timer();
                                            // run query playlist immediately
                                            mQueryPlaylistTask.schedule(new QueryPlaylistTask(), 0);
                                        } else {
                                            Toast.makeText(getApplicationContext(), dismissErroMessage(), Toast.LENGTH_LONG).show();
                                            finish();
                                            Log.e(TAG, "Create record file stream failed");
                                        }
                                    }
                                });
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (retry == 0) {
                        // All 3 times failed, show notify dialog
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), dismissErroMessage(), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    } else {
                        try {
                            Thread.sleep(6000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        });
    }

    private void dialogWaitingRecord(){
        if (downloadingDialog == null) {
            downloadingDialog = new ProgressDialog(RecordStreamVideo.this, ProgressDialog.THEME_HOLO_LIGHT);
        }

        if (!downloadingDialog.isShowing()) {
            if (type == PublicDefine.DOWNLOAD_FOR_SHARING) {
                downloadingDialog.setTitle(getString(R.string.title_download_motion_video));
            } else {
                downloadingDialog.setTitle(getString(R.string.title_download_motion_video_2));
            }
            downloadingDialog.setMessage(getString(R.string.message_download_motion_video));
            downloadingDialog.setCancelable(true);
            // 20161221 HOANG AA-2215 Disable touch out side cancellable
            downloadingDialog.setCanceledOnTouchOutside(false);
            downloadingDialog.setIndeterminate(true);
            downloadingDialog.show();
        }

        downloadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                //stopPlayback();
                release(false, false);
                finish();
            }
        });
    }

    @Override
    public void onRtmpFileStreamingSuccess(String rtmpUrl) {
        Log.d(TAG, "on create RTMP file streaming success, rtmp url: " + rtmpUrl);
        mSdcardStreamingUrl = rtmpUrl;

        // HOANG: Should query playlist for requesting camera to stream all files to server.
        mQueryPlaylistTask = new Timer();
        // run query playlist immediately
        mQueryPlaylistTask.schedule(new QueryPlaylistTask(), 0);
    }

    @Override
    public void onRtmpFileStreamingFailed(int errorCode, int statusCode) {
        Log.d(TAG, "on create RTMP file streaming failed, errorCode: " + errorCode + ", statusCode: " + statusCode);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getString(R.string.download_firmware_error), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private class QueryPlaylistTask extends TimerTask {
        @Override
        public void run() {
            String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
            LoadPlaylistTask load_list_task = new LoadPlaylistTask(RecordStreamVideo.this, false);
            load_list_task.execute(saved_token, device_mac, eventCode);
        }

    }

    private void stopPlayback() {
        Log.d(TAG, "Stop play back...");
        if (mRtmpFileStreamingTask != null) {
            mRtmpFileStreamingTask.cancel(true);
        }

        if (mQueryPlaylistTask != null) {
            mQueryPlaylistTask.cancel();
        }

        if (mPlaylist != null) {
            mPlaylist.clear();
        }

        closeRelayFileStream(new FutureCallback<Models.DeviceCommandResponse>() {
            @Override
            public void onCompleted(Exception e, Models.DeviceCommandResponse result) {
                if (e != null) {
                    Log.d(TAG, "Close relay file stream failed");
                    Log.e(TAG, Log.getStackTraceString(e));
                } else {
                    Log.d(TAG, "Close relay file stream done");
                }
            }
        });
    }

    private void closeRelayFileStream(final FutureCallback<Models.DeviceCommandResponse> callback) {
        Log.d(TAG, "Close relay rtmp file streaming, isClosingRelayFileStream? " + isClosingRelayFileStream);
        if (isClosingRelayFileStream) {
            Log.d(TAG, "Don't need to send close sd card relay rtmp command");
            callback.onCompleted(null, null);
            return;
        }
        isClosingRelayFileStream = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 20160826: HOANG: Also fix Bad Request here
                    final String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                    PublishCommandRequestBody.Builder builder = new PublishCommandRequestBody.Builder();
                    builder.setCommand("close_sdcard_relay_rtmp");
                    PublishCommandRequestBody requestBody = builder.create();
                    RemoteCommandRequest request = new RemoteCommandRequest();
                    request.setApiKey(apiKey);
                    request.setRegistrationId(device_mac);
                    request.setPublishCommandRequestBody(requestBody);
                    CameraCommandUtils.sendRemoteCommand(request);
                    isClosingRelayFileStream = false;
                    callback.onCompleted(null, null);

                } catch (Exception ex) {
                    callback.onCompleted(ex, null);
                }
            }
        }).start();
    }

    private boolean isLastClip(String fileUrl) {
        boolean isLast = false;
        String fileName = null;
        int startIdx = fileUrl.lastIndexOf("/") + 1;
        int endIdx = fileUrl.lastIndexOf("?");

        if (startIdx > 0 && endIdx > 0 && startIdx < endIdx) {
            try {
                fileName = fileUrl.substring(startIdx, endIdx);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        if (fileUrl.endsWith("last.flv") || fileUrl.endsWith("last.FLV")) {
            isLast = true;
        } else {
            if (fileName != null) {
                if (fileName.contains("last.flv") && fileName.lastIndexOf("last.flv") == fileName.length() - 8) {
                    isLast = true;
                }
            } else {
                endIdx = fileUrl.lastIndexOf(".flv") + 4;
                try {
                    fileName = fileUrl.substring(startIdx, endIdx);
                    Log.i(TAG, "Last clip name: " + fileName);
                    if (fileName.contains("last.flv") && fileName.lastIndexOf("last.flv") == fileName.length() - 8) {
                        isLast = true;
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }

        return isLast;
    }

    private boolean isSupportedFormat(String fileUrl) {
        boolean isSupported = false;
        String fileName = null;
        int startIdx = fileUrl.lastIndexOf("/") + 1;
        int endIdx = fileUrl.lastIndexOf("?");

        if (fileUrl.endsWith(".flv") || fileUrl.endsWith(".FLV")) {
            return true;
        }

        if (startIdx > 0 && endIdx > 0 && startIdx < endIdx) {
            fileName = fileUrl.substring(startIdx, endIdx);
        }

        if (fileName != null && fileName.contains(".flv") && fileName.lastIndexOf(".flv") == fileName.length() - 4) {
            isSupported = true;
        }


        return isSupported;
    }


    private String dismissErroMessage(){
        if (type == PublicDefine.DOWNLOAD_FOR_SHARING) {
            return getString(R.string.share_fail_error);
        } else {
            return getString(R.string.download_firmware_error);
        }
    }

    private void stopPlayer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mPlayer != null) {
                    Log.d(TAG, "releasing ...");
                    shouldInterrupt = true;
                    mPlayer.suspend();
                    synchronized (this) {
                        try {
                            mPlayer.stop();
                            /*while (isInitilizing) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }
                            }*/
                            mPlayer.release();
                            mPlayer = null;
                        } catch (Exception e) {
                        } finally {
                            if (mPlayer != null) {
                                mPlayer.release();
                                mPlayer = null;
                            }
                        }
                    }
                    shouldInterrupt = false;
                    Log.d(TAG, "player released");
                }
            }
        }).start();
    }

}
