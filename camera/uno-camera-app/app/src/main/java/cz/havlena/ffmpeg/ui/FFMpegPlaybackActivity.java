package cz.havlena.ffmpeg.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blinkhd.playback.LoadPlaylistListener;
import com.blinkhd.playback.LoadPlaylistTask;
import com.blinkhd.playback.RequestRelayFileStreamService;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.command.CameraCommandUtils;
import com.hubble.devcomm.Device;
import com.hubble.file.FileService;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.helpers.AsyncPackage;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.streaming.sdcard.RtmpFileStreamingHandler;
import com.hubble.streaming.sdcard.RtmpFileStreamingJobBasedTask;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.media.ffmpeg.FFMpeg;
import com.media.ffmpeg.FFMpegException;
import com.media.ffmpeg.FFMpegPlayer;
import com.media.ffmpeg.android.FFMpegMovieViewAndroid;
import com.msc3.DeleteEventTask;
import com.msc3.IDeleteEventCallBack;
import com.msc3.Streamer;

import org.apache.http.HttpStatus;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.ServerDeviceCommand;
import base.hubble.command.PublishCommandRequestBody;
import base.hubble.command.RemoteCommandRequest;
import base.hubble.constants.Streaming;
import base.hubble.meapi.device.GeneralData;
import base.hubble.meapi.device.TimelineEvent;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

public class FFMpegPlaybackActivity extends FragmentActivity implements Callback, LoadPlaylistListener, IDeleteEventCallBack,RtmpFileStreamingHandler {
  public static final String ACTION_FFMPEG_PLAYER_STOPPED = "cz.havlena.ffmpeg.ui.ACTION_FFMPEG_PLAYER_STOPPED";
  public static final String EVENT_NOTIFICATION = "FFMpegPlaybackActivity_event_notification";
  private static final String TAG = "FFMpegPlaybackActivity";

  public static final String COME_FROM = "come_from";
  public static final String COME_FROM_GCM = "come_from_gcm";
  public static final String COME_FROM_EVENT_HISTORY = "come_from_event_history";

  // private static final String LICENSE =
  // "This software uses libraries from the FFmpeg project under the LGPLv2.1";
  private static final int QUERY_PLAYLIST_INTERVAL = 10000;
  private static final int DIALOG_CONNECTION_IN_PROGRESS = 1;
  private static final int DIALOG_CONNECTION_CANCELLING = 2;
  private static final int DIALOG_CAMERA_IS_NOT_AVAILABLE = 3;
  private static final int DIALOG_CAMERA_STAND_BY_MODE = 4;
  private FFMpegMovieViewAndroid mMovieView;
  // private WakeLock mWakeLock;
  private String device_mac;
  private String local_url;
  private String local_file_status_url;
  private int default_width, default_height;
  private int default_screen_width, default_screen_height;
  private float ratio;
  private IPlaylistUpdater playlistUpdater;
  private Timer queryPlaylistTask;
  private TimelineEvent mEvent;
  private String eventCode;
  private String firstClipPath;
  private boolean shouldStopLoading;
  private boolean shouldShowDuration;
  private int totalDuration = -1;
  private ArrayList<String> mPlaylist;
  private PowerManager.WakeLock wl;
  private boolean user_cancelled;
  private ProgressDialog deleteEventDialog;
  private boolean isPlayingOffline = false;
  private String offlineFilePath = "";

  private AnimationDrawable anim = null;
  private boolean is_clip_on_sdcard = false;
  private boolean is_local_camera = false;
  private String clip_name, md5_sum, apiKey;
  private ArrayList<String> video_clip_list;
  private boolean isDestroyed = false;
  private String relaySdcardUrl = null;
  private SecureConfig settings = HubbleApplication.AppConfig;
  public static Device mDevice;
  private String nameFileDownload;
  private boolean isDownload;
  private String deviceName = "Vtech";
  //AA-1412:
  //Use to know where this activity is called
  private String comeFrom = null;
  private EventData eventData;
  private static final String LOCATION = "Location";
  private static final String RETRYAFTER = "Retry-After";
  private String job_location = null;
  private int    retry_time = 0;
  private RtmpFileStreamingJobBasedTask mRtmpFileStreamingTask;
  private boolean isExitInProgress = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, TAG + " onCreate...");
    eventData = new EventData();
    user_cancelled = false;
    isDestroyed = false;
    firstClipPath = null;
    mEvent = null;
    mPlaylist = new ArrayList<>();
    apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
    setContentView(R.layout.ffmpeg_main);

    try {
      FFMpeg ffmpeg = new FFMpeg();
    } catch (FFMpegException e) {
      Log.d(TAG, "Error when initializing ffmpeg: " + e.getMessage());
      FFMpegMessageBox.show(this, e);
      finish();
    }

    Intent intent = getIntent();
    if (intent != null) {
      Uri data = intent.getData();
      String filePath;
      if (data != null) {
        filePath = Util.getVideoPathFromUri(this, data);
        Log.d(TAG, "Play offline file from: " + filePath);
        isPlayingOffline = true;
        shouldStopLoading = true;
        shouldShowDuration = true;
        offlineFilePath = filePath;
        mPlaylist.add(filePath);
      }

      Bundle extra = intent.getExtras();
      if (extra != null) {
        String oneClipURL = extra.getString(Streaming.EXTRA_ONE_CLIP);
        if(oneClipURL != null) {
          isPlayingOffline = true;
          shouldStopLoading = true;
          shouldShowDuration = true;
          offlineFilePath = oneClipURL;
          mPlaylist.add(oneClipURL);
        } else {
          device_mac = extra.getString(Streaming.EXTRA_REGISTRATION_ID);
          eventCode = extra.getString(Streaming.EXTRA_EVENT_CODE);
          deviceName = extra.getString(Streaming.CAMERA_NAME, BuildConfig.FLAVOR);
          /**
           * local_url
           */
          local_url = extra.getString(Streaming.EXTRA_LOCAL_URL);
          local_file_status_url = extra.getString(Streaming.EXTRA_GET_CLIP_STATUS_URL);
          is_clip_on_sdcard = extra.getBoolean(Streaming.EXTRA_IS_CLIP_ON_SDCARD);
          is_local_camera = extra.getBoolean(Streaming.EXTRA_IS_LOCAL_CAMERA);
          clip_name = extra.getString(Streaming.EXTRA_CLIP_NAME);
          md5_sum = extra.getString(Streaming.EXTRA_MD5_SUM);
          video_clip_list = extra.getStringArrayList(Streaming.EVENT_VIDEO_CLIP_LIST);
          Log.d(TAG, "event code: " + eventCode + " camera name: " + deviceName);
          comeFrom = extra.getString(FFMpegPlaybackActivity.COME_FROM, null);
        }
      }
    }

    if (isPlayingOffline == false) {
      shouldStopLoading = false;
      shouldShowDuration = false;
      if (is_local_camera && is_clip_on_sdcard) {
        if(FFMpegPlaybackActivity.COME_FROM_EVENT_HISTORY.equals(comeFrom)
                && video_clip_list != null && !video_clip_list.isEmpty()){
          startPlayEvents();
        } else {
          startPlayLocalFileStream();
        }
      } else if (is_clip_on_sdcard && !is_local_camera) {
          startRtmpFileStreamJobBasedTask(device_mac, clip_name, md5_sum);
          //startPlayRelayFileStream(device_mac, clip_name, md5_sum);
      } else {
        if(FFMpegPlaybackActivity.COME_FROM_EVENT_HISTORY.equals(comeFrom)
                && video_clip_list != null && !video_clip_list.isEmpty()){
          startPlayEvents();
        } else {
          startPlayCloudStream();
        }
      }
    }
  }

  private void startPlayEvents() {
    Log.d(TAG,"Playing events from event history");
    try {
      showDialog(DIALOG_CONNECTION_IN_PROGRESS);
    } catch (Exception e) {
    }
    if (mPlaylist != null) {
      mPlaylist.clear();
    }
    for (String clipPath : video_clip_list) {
      if (is_local_camera && is_clip_on_sdcard) {
        fetchSDCardClipStatus(String.format(local_file_status_url, clipPath));
        clipPath = String.format(local_url, clipPath);
      }
      if (!clipPath.isEmpty() && isSupportedFormat(clipPath) && mPlaylist != null) {
        mPlaylist.add(clipPath);
      }
    }
    if (mPlaylist != null && mPlaylist.size() > 0) {
      // check whether the playlist is complete
      String last_file = mPlaylist.get(mPlaylist.size() - 1);
      Log.i(TAG, "Last clip of playlist: " + last_file);
      if (isLastClip(last_file)) {
        Log.i(TAG, "LAST CLIP OF MOTION VIDEO: " + last_file);
        // the playlist is complete --> stop loading
        shouldStopLoading = true;
        totalDuration = getDurationFromFileUrl(last_file);
        /*if (totalDuration <= 0) {
          dismissDialog(DIALOG_CONNECTION_IN_PROGRESS);
          showNotifyDialog(getString(R.string.error), getString(R.string.video_corrupt));
          return;
        }*/
        Log.i(TAG, "Total duration is: " + totalDuration);
        shouldShowDuration = true;
        firstClipPath = mPlaylist.get(0);
        Log.d(TAG, "First clip path: " + firstClipPath);
        setupFFMpegPlayback(false);
        if (shouldStopLoading && shouldShowDuration && mMovieView != null && totalDuration > 0) {
            mMovieView.setDuration(totalDuration * 1000);
        }
      } else {
        Log.d(TAG, "Playlist is not in correct format");
        //TODO: Add dialog with appropritae message
        if (is_clip_on_sdcard && is_local_camera) {
          startPlayLocalFileStream();
        } else {
          startPlayCloudStream();
        }
        //FFMpegPlaybackActivity.this.finish();
      }

    }
  }

  private void startPlayCloudStream() {
    try {
      showDialog(DIALOG_CONNECTION_IN_PROGRESS);
    } catch (Exception e) {
    }
    Log.i("mbp", "ANOTHER");
    if (device_mac == null || eventCode == null) {
      Log.d(TAG, "Not specified device mac or time code");
      if (mPlaylist.size() == 0) {
        Log.d(TAG, "Also no URL passed . Exiting.. ");
        finish();
      }

      // Wait to onStart to start playing
    } else {
      queryPlaylistTask = new Timer();
      // run query playlist immediately
      queryPlaylistTask.schedule(new QueryPlaylistTask(), 0);
    }
  }

  private void startPlayLocalFileStream() {
    // run query playlist immediately
    try {
      showDialog(DIALOG_CONNECTION_IN_PROGRESS);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Log.i("mbp", "Local camera and local file");
    if (device_mac == null || eventCode == null) {
      Log.d(TAG, "Not specified device mac or time code");
      if (mPlaylist.size() == 0) {
        Log.d(TAG, "Also no URL passed . Exiting.. ");
        finish();
      }
    } else {
      Log.i("mbp", "Start query task now");
      queryPlaylistTask = new Timer();
      queryPlaylistTask.schedule(new QueryPlaylistTask(), 0);
    }
  }

  private void runOnUiThreadIfVisible(Runnable runnable) {
    if (!isDestroyed) {
      runOnUiThread(runnable);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (mMovieView != null) {
      recalcDefaultScreenSize();
      resizeFFMpegView();
    }
  }

  private void fetchSDCardClipStatus(final String localFileStatuURL) {
    Log.i(TAG, "Local file status url: " + localFileStatuURL);
    // run query playlist immediately

    Ion.with(this)
        .load("GET", localFileStatuURL)
        .setTimeout(4000)
        .asString()
        .setCallback(new FutureCallback<String>() {
          @Override
          public void onCompleted(Exception e, String result) {
            if (e != null) {
              Log.e(TAG, "Error when execute sd local file");
              Toast.makeText(FFMpegPlaybackActivity.this, String.format(getString(R.string.cannot_access_clip_on_sd_card), clip_name), Toast.LENGTH_SHORT).show();
              e.printStackTrace();
            } else {
              Log.i(TAG, "EXECUTE SD local file result: " + result + " status:" + getResultStatus(PublicDefine.GET_SDLOCAL_FILE_CMD, result));
              if (getResultStatus(PublicDefine.GET_SDLOCAL_FILE_CMD, result) == 0) {
                //setupFFMpegPlayback(false);
              } else if (getResultStatus(PublicDefine.GET_SDLOCAL_FILE_CMD, result) == -1) {
                showNotifyDialog(getString(R.string.error), getString(R.string.unknown_error));
              } else if (getResultStatus(PublicDefine.GET_SDLOCAL_FILE_CMD, result) == -2) {
                showNotifyDialog(getString(R.string.error), getString(R.string.your_record_clip_is_not_found_on_camera_sd_card));
              } else if (getResultStatus(PublicDefine.GET_SDLOCAL_FILE_CMD, result) == -3) {
                showNotifyDialog(getString(R.string.error), getString(R.string.your_camera_sd_card_is_plugged_out));
              }
            }
          }
        });
  }

  private int getResultStatus(String cmd, String result) {
    String strResult = result.replace(cmd + ": ", "");
    int intResult = Integer.MAX_VALUE;
    try {
      intResult = Integer.parseInt(strResult);
    } catch (Exception ex) {
      // just ignore
    }
    return intResult;
  }

  private void showNotifyDialog(final String title, final String msg) {
    runOnUiThreadIfVisible(new Runnable() {
      @Override
      public void run() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FFMpegPlaybackActivity.this);
        builder.setMessage(msg)
            .setTitle(title)
            .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                stopPlayback();
                finish();
              }
            }).show();
      }
    });
  }
  
  private void startRtmpFileStreamJobBasedTask(final String deviceMac, final String clipName, final String md5Sum) {
    try {
      showDialog(DIALOG_CONNECTION_IN_PROGRESS);
    } catch (Exception e) {
    }

    Log.d(TAG, "Start RTMP file stream job based task");
    mRtmpFileStreamingTask = new RtmpFileStreamingJobBasedTask();
    mRtmpFileStreamingTask.setRtmpFileStremingHandler(this);
    mRtmpFileStreamingTask.setApiKey(apiKey);
    mRtmpFileStreamingTask.setRegistrationId(deviceMac);
    mRtmpFileStreamingTask.setClientType("android");
    mRtmpFileStreamingTask.setClipName(clipName);
    mRtmpFileStreamingTask.setMd5Sum(md5Sum);
    mRtmpFileStreamingTask.execute();
  }

  private void startPlayRelayFileStream(final String device_mac, final String clip_name, final String md5_sum) {
    try {
      showDialog(DIALOG_CONNECTION_IN_PROGRESS);
    } catch (Exception e) {
    }
    AsyncPackage.doInBackground(new Runnable() {
      @Override
      public void run() {
          int retry = 3;
          while (retry-- > 0 && !user_cancelled) {
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

            try {
              Log.i(TAG, "Create file stream for file: " + clip_name);
              final Models.ApiResponse<Models.CreateFileStreamResponse> response = Api.getInstance().getService().createFileSession(
                      apiKey, device_mac, "1", new Models.CreateFileSessionRequest("android", clip_name, md5_sum, null));
              if (response != null) {
                final String status = response.getStatus();
                Log.i(TAG, "Create file stream status: " + response.toString() + " -> " + status + " retries: " + retry);
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
                      showNotifyDialog(getString(R.string.error), errorMsg);
                    } else {
                      showNotifyDialog(getString(R.string.error), response.getMessage());
                    }
                    break;
                  }
                } else {
                  runOnUiThreadIfVisible(new Runnable() {
                    @Override
                    public void run() {
                      if (status.equals("200")) {
                        relaySdcardUrl = response.getData().getURL();
                        Log.i(TAG, "Create file stream succeeded, url: " + relaySdcardUrl);
                        queryPlaylistTask = new Timer();
                        // run query playlist immediately
                        queryPlaylistTask.schedule(new QueryPlaylistTask(), 0);
                      } else {
                        Log.i(TAG, "Create file stream failed");
                      }
                    }
                  });
                  break;
                }
              } else {
                Log.i(TAG, "Create file stream, response is null");
              }
            } catch (Exception e) {
              e.printStackTrace();
            }

            if (retry == 0) {
              // All 3 times failed, show notify dialog
              String errorMsg = String.format(getString(R.string.cant_open_file_with_error), "0", "0", "network error");
              showNotifyDialog(getString(R.string.error), errorMsg);
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

  protected Dialog onCreateDialog(int id) {
    AlertDialog.Builder builder;
    AlertDialog alert;
    ProgressDialog dialog;
    Spanned msg;
    switch (id) {
      case DIALOG_CAMERA_IS_NOT_AVAILABLE:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml("<big>" + getResources().getString(R.string.cant_open_file) + "</big>");
        builder.setMessage(msg).setCancelable(false).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                exitActivity(true);

              }
            }
        );

        alert = builder.create();
        return alert;
      case DIALOG_CAMERA_STAND_BY_MODE:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml("<p align=\"justify\">"+ getResources().getString(R.string.camera_may_enter_stand_by_mode) + "</p>");
        builder.setTitle(getResources().getString(R.string.event_playback));
        builder.setMessage(msg).setCancelable(false).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    exitActivity(false);

                  }
                }
        );

        alert = builder.create();
        return alert;

      case DIALOG_CONNECTION_IN_PROGRESS:
        dialog = new ProgressDialog(this);
        msg = Html.fromHtml("<big>" + getString(R.string.buffering_this_could_take_a_few_second_please_wait_) + "</big>");
        dialog.setMessage(msg);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            user_cancelled = true;
            dialog.dismiss();
            exitActivity(true);

          }
        });

        dialog.setButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
              }
            }
        );

        return dialog;

      case DIALOG_CONNECTION_CANCELLING:
        dialog = new ProgressDialog(this);
        msg = Html.fromHtml("<big>" + getString(R.string.cancelling_) + "</big>");
        dialog.setMessage(msg);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        return dialog;
      default:
        return null;
    }
  }

  public void showSpinner() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
          findViewById(R.id.imgFFMpegPlaybackActivityLoaderVTech).setVisibility(View.VISIBLE);
        } else {
          ImageView hubbleSpinner = (ImageView) findViewById(R.id.imgFFMpegPlaybackActivityLoaderHubble);
          hubbleSpinner.setVisibility(View.VISIBLE);
          hubbleSpinner.setImageResource(R.drawable.loader_anim1);
          AnimationDrawable animationDrawable = (AnimationDrawable) hubbleSpinner.getDrawable();
          animationDrawable.start();
        }
      }
    });
  }

  public void hideSpinner() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
          findViewById(R.id.imgFFMpegPlaybackActivityLoaderVTech).setVisibility(View.GONE);
        } else {
          ImageView hubbleSpinner = (ImageView) findViewById(R.id.imgFFMpegPlaybackActivityLoaderHubble);
          hubbleSpinner.clearAnimation();
          hubbleSpinner.setVisibility(View.GONE);
        }
      }
    });
  }

  private void setupFFMpegPlayback(boolean rtmpLink) {

    if (!user_cancelled) {

      if (firstClipPath == null) {
        Log.d(TAG, "First clip path is null");
        finish();
      } else {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "TURN ON for first time connect"
        );
        wl.setReferenceCounted(false);
        wl.acquire();

        setContentView(R.layout.ffmpeg_playback_activity);

        mMovieView = (FFMpegMovieViewAndroid) findViewById(R.id.imageVideo);

        if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
          Log.d(TAG, "vtech -> hide controller seek bar");
          mMovieView.hideControllerThumb();
        }

        mMovieView.initVideoView(new Handler(FFMpegPlaybackActivity.this), true, false);
        mMovieView.setShouldShowDuration(shouldShowDuration);
        mMovieView.setVideoPath(firstClipPath);
        // set playlist updater
//                playlistUpdater = mMovieView.getFFMpegPlayer();
//                playlistUpdater.finishLoadingPlaylist(shouldStopLoading);
//                playlistUpdater.updatePlaylist(mPlaylist);

        final ImageView imgFullClose = (ImageView) findViewById(R.id.imgCloseFull);
        if (imgFullClose != null) {
          imgFullClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
              user_cancelled = true;
              exitActivity(true);
            }
          });
        }

        final TextView txtDone = (TextView) findViewById(R.id.txtPlaybackDone);
        if (txtDone != null) {
          txtDone.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
              user_cancelled = true;
              exitActivity(true);
            }
          });
        }

				/*
         * 20140527_bhavesh_bug_hcd112 Add click event of delete button
				 */
        final ImageView imgDelete = (ImageView) findViewById(R.id.imgDelete);
        if (imgDelete != null) {
          imgDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
              Log.i(TAG, "on Click of delete event");
              show_delete_Event_Confirmation_dialog();
            }
          });
        }

				/*
         * 20140603_bhavesh_bug_hcd223 Download event
				 */
        final ImageView imgDownload = (ImageView) findViewById(R.id.imgDownload);
        if (imgDownload != null) {
          imgDownload.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
              Log.i(TAG, "on Click of Download event");
              show_Download_Event_Confirmation_dialog();

            }
          });
        }

        //Added to support new event history screen
        if (!TextUtils.isEmpty(comeFrom) && FFMpegPlaybackActivity.COME_FROM_EVENT_HISTORY.equals(comeFrom)) {
          imgDelete.setVisibility(View.INVISIBLE);
          imgDownload.setVisibility(View.INVISIBLE);
        } else {
          imgDownload.setVisibility(View.VISIBLE);
          imgDelete.setVisibility(View.VISIBLE);
        }

        if (isPlayingOffline) {
          imgDownload.setVisibility(View.GONE);
        }

      }
    } else {
      stopPlayback();
      finish();
    }
  }


  private void show_delete_Event_Confirmation_dialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(FFMpegPlaybackActivity.this);
    builder.setMessage(R.string.FFMpegPlaybackActivity_delete_event_confirmation).setPositiveButton(R.string.No, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // Do Nothing- User do not want to logout
          }
        }
    ).setNegativeButton(R.string.Yes, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            if (!isPlayingOffline) {
              deleteEventDialog = ProgressDialog.show(FFMpegPlaybackActivity.this, null, getApplicationContext().getResources().getString(R.string.FFMpegPlaybackActivity_deleting_event), true, false
              );
              final String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
              Log.i(TAG, "Delete event API key : " + apiKey);
              Log.i(TAG, "Delete event Device id : " + device_mac);
              Log.i(TAG, "Delete event Event id : " + mEvent.getId());
              DeleteEventTask delete = new DeleteEventTask(FFMpegPlaybackActivity.this, FFMpegPlaybackActivity.this);
              delete.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, apiKey, device_mac, String.valueOf(mEvent.getId()));
            } else {
              Log.i(TAG, "Needs to delete local video");
              if (!TextUtils.isEmpty(offlineFilePath)) {
                Log.i(TAG, "Deleting local video");
                if (Util.deleteFile(offlineFilePath)) {
                  stopPlayback();
                  finish();
                }
              }
            }
          }
        }
    );

    AlertDialog confirmExitDialog = builder.create();
    confirmExitDialog.setTitle(R.string.app_brand_application_name);
    confirmExitDialog.setCancelable(true);
    confirmExitDialog.show();
  }

  /*
   * 20140604_bhavesh_bug_HCD Description : Showing Confirmation Dialog when
   * Download event
   */
  private void show_Download_Event_Confirmation_dialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(FFMpegPlaybackActivity.this);
    builder.setMessage(R.string.FFMpegPlaybackActivity_download_event_confirmation).setPositiveButton(R.string.No, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // Do Nothing- User do not want to logout
          }
        }
    ).setNegativeButton(R.string.Yes, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            try {
              Toast.makeText(FFMpegPlaybackActivity.this, getResources().getString(R.string.downloading_started), Toast.LENGTH_SHORT
              ).show();
              isDownload = true;
              GeneralData[] playlist = mEvent.getData();
              if ((is_local_camera && is_clip_on_sdcard) || !is_clip_on_sdcard) {
                if (playlist != null && playlist.length > 0) {
                  for (GeneralData aPlaylist : playlist) {
                    if (aPlaylist != null && aPlaylist.getFile() != null) {
                      String filePath = aPlaylist.getFile();
                      if (!filePath.isEmpty() && isSupportedFormat(filePath)) {
                        DateTimeFormatter cameraFormat = DateTimeFormat.forPattern("MM_dd_HH:mm:ss");
                        if (is_local_camera && is_clip_on_sdcard) {
                          filePath = String.format(local_url, filePath);
                        }
                        FileService.downloadVideo(filePath, FileService.getFormatedVideoFileName(deviceName, mEvent.getTime_stamp().getTime()), new FutureCallback<File>() {
                          @Override
                          public void onCompleted(Exception e, File result) {
                            if (e != null) {
                              Log.e(TAG, "Download video file error");
                              e.printStackTrace();
                            } else {
                              Log.i(TAG, "Download video file done: " + result.getAbsolutePath());
                            }
                          }
                        });
                      }
                    }
                  }
                }
              }
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        }
    );

    AlertDialog confirmExitDialog = builder.create();
    confirmExitDialog.setCancelable(true);
    confirmExitDialog.show();
  }

  private void recalcDefaultScreenSize() {
    DisplayMetrics displaymetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    if (displaymetrics.widthPixels > displaymetrics.heightPixels) {
      default_screen_height = displaymetrics.heightPixels;
      default_screen_width = displaymetrics.widthPixels;
    } else {
      default_screen_height = displaymetrics.widthPixels;
      default_screen_width = displaymetrics.heightPixels;
    }
    Log.d(TAG, "Default screen size: default width, default height: " + default_screen_width + ", " + default_screen_height);

    if (mMovieView != null) {
      if (default_screen_height * ratio > default_screen_width) {
        default_width = default_screen_width;
        default_height = (int) (default_width / ratio);
      } else {
        default_height = default_screen_height;
        default_width = (int) (default_height * ratio);
      }
    }

    Log.d(TAG, "Recalculate default size: width: " + default_width + ", height: " + default_height);
  }

  private void resizeFFMpegView() {
    if (mMovieView != null) {
      int new_width, new_height;

      if ((getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE) {
        new_width = default_width;
        new_height = default_height;
      } else {
        new_width = default_screen_height;
        new_height = (int) (new_width / ratio);
      }

      LayoutParams params = mMovieView.getLayoutParams();
      params.width = new_width;
      params.height = new_height;
      mMovieView.setLayoutParams(params);
      Log.d(TAG, "Surface resized: width: " + new_width + ", height: " + new_height);
    }
  }

  private void stopPlayback() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (wl != null && wl.isHeld()) {
          wl.release();
          wl = null;
        }

        if (mPlaylist != null) {
          /*Iterator<String> iter = mPlaylist.iterator();
            while (iter.hasNext()) {
            iter.next();
            iter.remove();*/
          mPlaylist.clear();
          mPlaylist = null;
        }

        if (queryPlaylistTask != null) {
          queryPlaylistTask.cancel();
        }

        if (mMovieView != null && !mMovieView.isReleasingPlayer()) {
          mMovieView.release();
        }
      }
    }).start();

    /*if (is_clip_on_sdcard && !is_local_camera) {
      try {
        mMovieView.startRecord(false, true, mDevice, nameFileDownload, null);
        if ((user_cancelled || !isDownload) && nameFileDownload != null) {
          Util.deleteFile(nameFileDownload);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }*/

    //AnalyticsInterface.getInstance().trackEvent("playVideo","play_recorded_video",eventData);
  }

  @Override
  public void onBackPressed() {
    user_cancelled = true;
    exitActivity(true);
  }

  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onStart() {
    super.onStart();
    /**
     * Setup for play local file
     */
    if (device_mac == null || eventCode == null) {
      try {
        FFMpeg ffmpeg = new FFMpeg();
        firstClipPath = mPlaylist.get(0);
        shouldStopLoading = true;
        shouldStopLoading = true;
        shouldShowDuration = true;
        setupFFMpegPlayback(false);
      } catch (FFMpegException e) {
        Log.d(TAG, "Error when initializing ffmpeg: " + e.getMessage());
        FFMpegMessageBox.show(this, e);
        finish();
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    //AA-1532
  }

  // prevent c
  private boolean isClosingRelayFileStream;
  private ProgressDialog progressDialog;

  private void closeRelayFileStream(final FutureCallback<Models.DeviceCommandResponse> callback) {
    if (isDestroyed() || isClosingRelayFileStream) {
      callback.onCompleted(null, null);
      return;
    }
    isClosingRelayFileStream = true;
    progressDialog = new ProgressDialog(this);
    progressDialog.setCancelable(false);
    progressDialog.setMessage(getString(R.string.closing_session));
    progressDialog.show();
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // 20160826: HOANG: Also fix Bad Request here
         /* Models.ApiResponse<Models.CommandResponse<Models.DeviceCommandResponse>> res = Api.getInstance().getService().sendCommand(
                  device_mac, apiKey, new ServerDeviceCommand("close_sdcard_relay_rtmp", null, null));*/

          PublishCommandRequestBody.Builder builder = new PublishCommandRequestBody.Builder();
          builder.setCommand("close_sdcard_relay_rtmp");
          PublishCommandRequestBody requestBody = builder.create();
          RemoteCommandRequest request = new RemoteCommandRequest();
          request.setApiKey(apiKey);
          request.setRegistrationId(device_mac);
          request.setPublishCommandRequestBody(requestBody);
          // no need to wait for this command 30 second
          request.setCommandTimeout(15*1000);
          CameraCommandUtils.sendRemoteCommand(request);

          isClosingRelayFileStream = false;
          runOnUiThreadIfVisible(new Runnable() {
            @Override
            public void run() {
              try {
                progressDialog.dismiss();
              } catch (Exception e) {
              }
            }
          });

          callback.onCompleted(null, null);

        } catch (Exception ex) {
          runOnUiThreadIfVisible(new Runnable() {
            @Override
            public void run() {
              try {
                progressDialog.dismiss();
              } catch (Exception e) {
              }
            }
          });
          callback.onCompleted(ex, null);
        }
      }
    }).start();
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopPlayback();
    finish();
  }

  protected void onDestroy() {
    isDestroyed = true;
    // Close the player properly here.
    if (wl != null && wl.isHeld()) {
      wl.release();
      wl = null;
    }
    super.onDestroy();
  }

  @Override
  public boolean handleMessage(Message msg) {

    if (user_cancelled) {
      exitActivity(true);
      return false;
    }

    switch (msg.what) {
      case FFMpegPlayer.MSG_MEDIA_STREAM_SEEK_COMPLETE:
        break;
      case Streamer.MSG_CAMERA_IS_NOT_AVAILABLE:

        if (wl != null && wl.isHeld()) {
          wl.release();
          wl = null;
        }

        final Runnable showDialog = new Runnable() {
          @Override
          public void run() {

            try {
              dismissDialog(DIALOG_CONNECTION_IN_PROGRESS);
            } catch (Exception e) {

            }

            try {
              if(PublicDefine.getModelIdFromRegId(device_mac).compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0) {
                showDialog(DIALOG_CAMERA_STAND_BY_MODE);
              }
              else {
                showDialog(DIALOG_CAMERA_IS_NOT_AVAILABLE);
              }
            } catch (Exception ie) {
            }

          }
        };
        runOnUiThread(showDialog);
        break;

      case Streamer.MSG_VIDEO_SIZE_CHANGED:
        int video_width = msg.arg1;
        int video_height = msg.arg2;
        ratio = (float) video_width / video_height;
        Log.d(TAG, "Video width, height, ratio: " + video_width + ", " + video_height + ", " + ratio);
        recalcDefaultScreenSize();

        playlistUpdater = mMovieView.getFFMpegPlayer();
        if (playlistUpdater != null) {
          if (shouldStopLoading && shouldShowDuration) {
            if (mMovieView != null) {
              mMovieView.setDuration(totalDuration * 1000);
            }
          }
          playlistUpdater.finishLoadingPlaylist(shouldStopLoading);

          // REMEMBER: never update playlist for relay sdcard session
          if (!isViewingRelaySdcardClip(is_clip_on_sdcard, is_local_camera)) {
            playlistUpdater.updatePlaylist(mPlaylist);
          }
        }

        break;
      case Streamer.MSG_VIDEO_STREAM_HAS_STOPPED:
        Log.d(TAG, "handle messg MSG_VIDEO_STREAM_HAS_STOPPED : finishing.. () ");

        exitActivity(true);
        // show options

        // if (mMovieView != null)
        // {
        // mMovieView.release();
        // mMovieView.setFullProgressBar();
        // mMovieView.showOptions();
        // }
        break;

      case Streamer.MSG_VIDEO_STREAM_HAS_STARTED:

        if (is_clip_on_sdcard && !is_local_camera) {
          nameFileDownload = FileService.getFormatedVideoFilePath(deviceName, mEvent.getTime_stamp().getTime()).getAbsolutePath();
          mMovieView.startRecord(true, true, mDevice, nameFileDownload, null);
        }

        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            resizeFFMpegView();
          }
        });

        try {
          dismissDialog(DIALOG_CONNECTION_IN_PROGRESS);
        } catch (Exception e) {

        }
        break;
      case Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY:
        if (is_clip_on_sdcard == true && is_local_camera == false) {
          Log.i(TAG, "Viewing relay sdcard clip stopped, stop loading immediately");
          shouldStopLoading = true;
          if (playlistUpdater != null) {
            playlistUpdater.finishLoadingPlaylist(true);
          }
        }
        break;
    }

    return false;
  }

  /**
   * File url has format: 000AE2103290_04_20141119090719000_00001_totaltime_last.flv
   * totaltime is in seconds.
   * Return: duration in seconds.
   */
  private int getDurationFromFileUrl(String fileUrl) {
    int duration = -1;
    String fileName = getFileNameFromUrl(fileUrl);
    //Log.d("mbp", "getDuration, file name ret: " + fileName);
    if (fileName != null) {
      String[] file_parts = fileName.split("_");
      if (file_parts != null) {
        if (file_parts.length == 6) {
          // new file format
          try {
            duration = Integer.parseInt(file_parts[4]);
          } catch (NumberFormatException e) {
            e.printStackTrace();
            duration = -1;
          }
        }
      }
    }
    Log.d(TAG, "Duration return from url: " + duration);
    return duration;
  }

  /**
   * @param fileUrl completed url of clip
   * @return file name without ".flv"
   */
  public static String getFileNameFromUrl(String fileUrl) {
    String fileName = null;
    if (fileUrl != null) {
      //int startIdx = fileUrl.indexOf("/clips/") + 7;
      int startIdx = fileUrl.lastIndexOf("/") + 1;
      int endIdx = fileUrl.lastIndexOf(".flv");
      try {
        fileName = fileUrl.substring(startIdx, endIdx);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return fileName;
  }

  private boolean isViewingRelaySdcardClip(boolean isClipOnSdcard, boolean isLocalCamera) {
    boolean result = false;
    if (isClipOnSdcard == true && isLocalCamera == false) {
      result = true;
    }
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.blinkhd.playback.LoadPlaylistListener#lComplete(com.nxcomm
   * .meapi.device.Event, base.hubble.meapi.device.Event[])
   */
  @Override
  public void onRemoteCallSucceeded(TimelineEvent latest, TimelineEvent[] allEvents) {

    if (user_cancelled) {
      exitActivity(true);
    }

    if (latest != null) {
      mEvent = latest;
      GeneralData[] playlist = latest.getData();
      if (playlist != null && playlist.length > 0) {
        for (int i = 0; i < playlist.length; i++) {
          if (playlist[i] != null && (playlist[i].getFile() != null)) {
            String filePath = playlist[i].getFile();
            if (is_local_camera && is_clip_on_sdcard) {
              fetchSDCardClipStatus(String.format(local_file_status_url, filePath));
              filePath = String.format(local_url, playlist[i].getFile());
            } else {
              Log.i(TAG, "Clip is on sd card: " + is_clip_on_sdcard + " camera is in local: " + is_local_camera);
            }

            if (!filePath.isEmpty() && isSupportedFormat(filePath) && mPlaylist != null) {
              if (i < mPlaylist.size()) {
                mPlaylist.set(i, filePath);
              } else {
                if (isViewingRelaySdcardClip(is_clip_on_sdcard, is_local_camera)) {
                  // 20151117: hoang: AA-657. Dont send request file stream for the fist file because app
                  // has already sent it before.
                  if (i > 0) {
                    /* Send file stream request for new file */
                    requestFileStreamToServer(filePath, playlist[i].getMd5Sum());
                  }
                }
                mPlaylist.add(filePath);
              }
            }
          }
        }
      }

      if (mPlaylist != null && mPlaylist.size() > 0) {
        // check whether the playlist is complete
        String last_file = mPlaylist.get(mPlaylist.size() - 1);
        Log.i(TAG, "Last clip of playlist: " + last_file);
        if (isLastClip(last_file)) {
          Log.i(TAG, "LAST CLIP OF MOTION VIDEO: " + last_file);
          // the playlist is complete --> stop loading
          shouldStopLoading = true;
          totalDuration = getDurationFromFileUrl(last_file);
          /*if(totalDuration <= 0){
            dismissDialog(DIALOG_CONNECTION_IN_PROGRESS);
            showNotifyDialog(getString(R.string.error), getString(R.string.video_corrupt));
            return;
          }*/
          Log.i(TAG, "Total duration is: " + totalDuration);
          shouldShowDuration = true;
        }

        if (firstClipPath != null) {
          Log.d(TAG, "onRemoteCallSucceed, shouldStopLoading? " + shouldStopLoading);

                    /* Update duration for playback here, the playback could be running */
          if (shouldStopLoading) {
            // Update total duration to player
            if (mMovieView != null && shouldShowDuration && totalDuration > 0) {
              mMovieView.setDuration(totalDuration * 1000);
            }
          }

          // just update playlist for FFMpegPlayer if not in relay sdcard mode
          if (playlistUpdater != null) {
            if (!isViewingRelaySdcardClip(is_clip_on_sdcard, is_local_camera)) {
              playlistUpdater.updatePlaylist(mPlaylist);
            } else {
              Log.i(TAG, "Viewing via relay sdcard, don't update the play list");
            }
            playlistUpdater.finishLoadingPlaylist(shouldStopLoading);
          }
        } else {
          // for relay sdcard
          if (isViewingRelaySdcardClip(is_clip_on_sdcard, is_local_camera)) {
            firstClipPath = relaySdcardUrl;
          } else {
            firstClipPath = mPlaylist.get(0);
          }
          Log.d(TAG, "First clip path: " + firstClipPath);
          setupFFMpegPlayback(false);
        }

        if (!shouldStopLoading) {
          queryPlaylistTask.schedule(new QueryPlaylistTask(), QUERY_PLAYLIST_INTERVAL);
        }
      } else {
        // playlist is empty --> finish
        Log.d(TAG, "Playlist is empty");
        FFMpegPlaybackActivity.this.finish();
      }

    } else {
      Log.d(TAG, "Event is null");
      FFMpegPlaybackActivity.this.finish();
    }
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

  @Override
  public void onRemoteCallFailed(int errorCode) {

    if (user_cancelled) {
      exitActivity(true);
      return;
    }

    if (queryPlaylistTask != null) {
      Log.d(TAG, "Load playlist timeout...try to reload...");
      queryPlaylistTask.schedule(new QueryPlaylistTask(), QUERY_PLAYLIST_INTERVAL);
    }
  }

  @Override
  public void delete_event_success() {

    Log.i(TAG, "inside delete_event_success");

		/*
     * Needs to delete event id from database
		 */
    final String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
    Log.i(TAG, " API key : " + apiKey);
    Log.i(TAG, " registration  id : " + device_mac);
    Log.i(TAG, " Event Code : " + mEvent.getId());

    if (deleteEventDialog != null && deleteEventDialog.isShowing()) {
      try {
        deleteEventDialog.dismiss();
      } catch (Exception e) {
      }
    }

    stopPlayback();
    finish();
  }

  @Override
  public void delete_event_failed() {
    Log.i(TAG, "Event deletion failed");
    if (deleteEventDialog != null && deleteEventDialog.isShowing()) {
      try {
        deleteEventDialog.dismiss();
      } catch (Exception e) {
      }
    }
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    String msg = getResources().getString(R.string.FFMpegPlaybackActivity_delete_event_failed);
    builder.setMessage(msg).setCancelable(true).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {

            dialog.dismiss();
          }
        }
    );
    try {
      builder.create().show();
    } catch (Exception e) {
    }
  }

  private void requestFileStreamToServer(final String fileName, final String md5Sum) {
    Intent msgIntent = new Intent(FFMpegPlaybackActivity.this, RequestRelayFileStreamService.class);
    msgIntent.setAction(RequestRelayFileStreamService.ACTION_RELAY_FILE_STREAM_REQUEST);
    msgIntent.putExtra(RequestRelayFileStreamService.API_KEY, apiKey);
    msgIntent.putExtra(RequestRelayFileStreamService.MD5_SUM, md5Sum);
    msgIntent.putExtra(RequestRelayFileStreamService.DEVICE_MAC, device_mac);
    msgIntent.putExtra(RequestRelayFileStreamService.FILE_NAME, fileName);
    startService(msgIntent);
  }

  @Override
  public void onRtmpFileStreamingSuccess(String rtmpUrl) {
    Log.d(TAG, "on create RTMP file streaming success, rtmp url: " + rtmpUrl);
    relaySdcardUrl = rtmpUrl;
    queryPlaylistTask = new Timer();
    // run query playlist immediately
    queryPlaylistTask.schedule(new QueryPlaylistTask(), 0);
  }

  @Override
  public void onRtmpFileStreamingFailed(int errorCode, int statusCode) {
    Log.d(TAG, "on create RTMP file streaming failed, errorCode: " + errorCode + ", statusCode: " + statusCode);
    if(device_mac != null && PublicDefine.getModelIdFromRegId(device_mac).compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0 && (errorCode == RtmpFileStreamingJobBasedTask.DEVICE_OFFLINE_CONNECTIVITY_ISSUE_CODE
            || statusCode == HttpStatus.SC_UNPROCESSABLE_ENTITY))
    {
      showStandbyDialog();
    }
    else {
      showOpenFileFailedDialog();
    }
  }

  private void showOpenFileFailedDialog() {
    Log.d(TAG, "showOpenFileFailedDialog");
    try {
      dismissDialog(DIALOG_CONNECTION_CANCELLING);
    } catch (Exception e1) {
    }

    try {
      dismissDialog(DIALOG_CONNECTION_IN_PROGRESS);
    } catch (Exception e) {
    }

    try {
      showDialog(DIALOG_CAMERA_IS_NOT_AVAILABLE);
    } catch (Exception ie) {
    }
  }

  private void showStandbyDialog() {
    Log.d(TAG, "showStandbyDialog");
    try {
      dismissDialog(DIALOG_CONNECTION_CANCELLING);
    } catch (Exception e1) {
    }

    try {
      dismissDialog(DIALOG_CONNECTION_IN_PROGRESS);
    } catch (Exception e) {
    }

    try {
      dismissDialog(DIALOG_CAMERA_IS_NOT_AVAILABLE);
    } catch (Exception ie) {
    }

    try {
      showDialog(DIALOG_CAMERA_STAND_BY_MODE);
    }
    catch (Exception e4)
    {

    }
  }

  private class QueryPlaylistTask extends TimerTask {
    @Override
    public void run() {
      String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
      LoadPlaylistTask load_list_task = new LoadPlaylistTask(FFMpegPlaybackActivity.this, false);
      load_list_task.execute(saved_token, device_mac, eventCode);
    }

  }

  private void exitActivity(boolean closeSessionReq){
    if(!isExitInProgress) {
      isExitInProgress = true;
      if(!isFinishing()) {
        showDialog(DIALOG_CONNECTION_CANCELLING);
      }
      stopPlayback();
      if (!is_local_camera && is_clip_on_sdcard && closeSessionReq) {
        closeRelayFileStream(new FutureCallback<Models.DeviceCommandResponse>() {
          @Override
          public void onCompleted(Exception e, Models.DeviceCommandResponse result) {
            runOnUiThreadIfVisible(new Runnable() {
              @Override
              public void run() {
                try {

                  if(!isFinishing()) {
                    dismissDialog(DIALOG_CONNECTION_CANCELLING);
                  }
                } catch (Exception e1) {
                  e1.printStackTrace();
                }
                finish();
              }
            });
          }
        });
      } else {
        runOnUiThreadIfVisible(new Runnable() {
          @Override
          public void run() {
            if (progressDialog != null && progressDialog.isShowing()) {
              try {
                progressDialog.dismiss();
              } catch (Exception e) {
              }
            }
            try {
              if(!isFinishing()) {
                dismissDialog(DIALOG_CONNECTION_CANCELLING);
              }
            } catch (Exception e1) {
              e1.printStackTrace();
            }
            finish();
          }
        });
      }
    }
  }

}
