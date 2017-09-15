package com.hubble.ui.eventsummary;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.file.FileService;
import com.hubbleconnected.camera.R;
import com.koushikdutta.async.future.FutureCallback;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by Admin on 07-03-2017.
 */
public class EventSummaryFullScreen extends AppCompatActivity {

	private final String TAG = "EventSummaryFullScreen";

	private ImageView mSummaryClose;
	private TextView mCameraName;
	private RelativeLayout mSummaryDateParentLayout;
	private TextView mDateText;
	private RelativeLayout mSummaryVideoLayout;
	private ImageView mSummaryLoadingView;
	private VideoView mSummaryVideoView;
	private ImageView mSummaryDownload;
	private LinearLayout mSummaryMotionLayout;
	private TextView mSummaryMotionText;
	private LinearLayout mSummarySoundLayout;
	private TextView mSummarySoundText;
	private LinearLayout mSummaryTemperatureLayout;
	private TextView mSummaryTempText;

	private String mRegistrationId;
	private String mSummaryCameraName;

	private EventSummary mEventSummary;
	private EventSummaryInfoDialog mInfoDialog = null;

	private MediaController mSummaryMC;

	private boolean mIsVideoDownloadInProgress = false;
	private boolean mIsImageDownloadInProgress = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_summary_full_screen);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

		Intent intent = getIntent();
		if (intent != null) {
			mEventSummary = (EventSummary) intent.
					getSerializableExtra(EventSummaryConstant.EVENT_SUMMARY_DETAIL_EXTRA);
			mRegistrationId = intent.getStringExtra(EventSummaryConstant.EVENT_SUMMARY_CAMERA_REGID_EXTRA);
			mSummaryLoadingView = (ImageView) findViewById(R.id.summary_progress_image);
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.image_rotate);
			mSummaryLoadingView.setVisibility(View.VISIBLE);
			mSummaryLoadingView.startAnimation(animation);

			mSummaryVideoView = (VideoView) findViewById(R.id.summary_video_view);
			mSummaryVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mSummaryVideoView.requestFocus();
					mSummaryVideoView.start();
					mSummaryLoadingView.clearAnimation();
					mSummaryLoadingView.setVisibility(View.GONE);
					mSummaryDownload.setVisibility(View.VISIBLE);
				}
			});

			Uri vidUri = Uri.parse(mEventSummary.getSummaryVideoUrlPath());
			mSummaryVideoView.setVideoURI(vidUri);
			mSummaryMC = new MediaController(this);
			mSummaryMC.setAnchorView(mSummaryVideoView);
			mSummaryVideoView.setMediaController(mSummaryMC);


			mSummaryClose = (ImageView) findViewById(R.id.summary_close);
			mCameraName = (TextView) findViewById(R.id.summary_camera_text);
			mSummaryDateParentLayout = (RelativeLayout) findViewById(R.id.summary_date_parent_layout);
			mDateText = (TextView) findViewById(R.id.summary_date);
			mSummaryVideoLayout = (RelativeLayout) findViewById(R.id.summary_video_layout);
			mSummaryDownload = (ImageView) findViewById(R.id.summary_download);
			mSummaryMotionLayout = (LinearLayout) findViewById(R.id.summary_motion_layout);
			mSummaryMotionText = (TextView) findViewById(R.id.summary_full_screen_motion_text);
			mSummarySoundLayout = (LinearLayout) findViewById(R.id.summary_sound_layout);
			mSummarySoundText = (TextView) findViewById(R.id.summary_full_screen_sound_text);
			mSummaryTemperatureLayout = (LinearLayout) findViewById(R.id.summary_temperature_layout);
			mSummaryTempText = (TextView) findViewById(R.id.summary_full_screen_temp_text);

			mSummaryDownload.setVisibility(View.GONE);
			mSummaryClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mSummaryVideoView != null) {
						if (mSummaryVideoView.isPlaying()) {
							mSummaryVideoView.stopPlayback();
						}
						mSummaryVideoView = null;
					}
					finish();
				}
			});
			if (mRegistrationId != null) {
				Device device = DeviceSingleton.getInstance().getDeviceByRegId(mRegistrationId);
				if (device != null && device.getProfile() != null) {
					mSummaryCameraName = device.getProfile().getName();
					mCameraName.setText(mSummaryCameraName);
				}
			}

			if (mEventSummary != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
				mDateText.setText(sdf.format(mEventSummary.getDate()));
				mSummaryDownload.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!mIsVideoDownloadInProgress && !mIsImageDownloadInProgress) {
							dismissInfoDialog();
							mInfoDialog = new EventSummaryInfoDialog(EventSummaryFullScreen.this, EventSummaryConstant.DOWNLOAD_INFO, mEventSummary.getDate(), new EventSummaryInfoDialog.IEventDownloadCallBack() {
								@Override
								public void onDownloadClick() {
									downloadDailySummaryFile();
								}
							});
							mInfoDialog.show();

						} else {
							Toast.makeText(EventSummaryFullScreen.this, "Summary download is in progress", Toast.LENGTH_LONG).show();
						}
					}
				});
				mSummaryMotionLayout.setVisibility(View.GONE);
				mSummarySoundLayout.setVisibility(View.GONE);
				mSummaryTemperatureLayout.setVisibility(View.VISIBLE);
				mSummaryTempText.setText(getString(R.string.temperature)+getString(R.string.summary_temp_with_in_range));
				setUpDetailLayout();
			}
		} else {
			finish();
		}
	}

	private void setUpDetailLayout() {
		if (mEventSummary.getTotalMotionEvent() > 0) {
			mSummaryMotionLayout.setVisibility(View.VISIBLE);
			mSummaryMotionText.setText(getString(R.string.summary_motion_events, mEventSummary.getTotalMotionEvent()
					+ getString(R.string.summary_captured)));
		}
		if (mEventSummary.getTotalSoundEvent() > 0) {
			mSummarySoundLayout.setVisibility(View.VISIBLE);
			mSummarySoundText.setText(getString(R.string.summary_sound_events, mEventSummary.getTotalSoundEvent()
					+ getString(R.string.summary_captured)));
		}
		if (mEventSummary.getTempMax() != -100 && mEventSummary.getTempMin() != -100) {
			mSummaryTempText.setText(getString(R.string.summary_temperature_events_range,
					mEventSummary.getTempMin(), mEventSummary.getTempMax()));
		} else if (mEventSummary.getTempMax() != -100) {
			mSummaryTempText.setText((getString(R.string.summary_temperature_events_above, mEventSummary.getTempMax())));
		} else if (mEventSummary.getTempMin() != -100) {
			mSummaryTempText.setText((getString(R.string.summary_temperature_events_above, mEventSummary.getTempMin())));
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			dismissInfoDialog();
			mSummaryClose.setVisibility(View.GONE);
			mCameraName.setVisibility(View.GONE);
			mSummaryDateParentLayout.setVisibility(View.GONE);
			mSummaryMotionLayout.setVisibility(View.GONE);
			mSummarySoundLayout.setVisibility(View.GONE);
			mSummaryTemperatureLayout.setVisibility(View.GONE);
			LinearLayout.LayoutParams layoutParamsLinear = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			mSummaryVideoLayout.setLayoutParams(layoutParamsLinear);
			RelativeLayout.LayoutParams layoutParamsRelative = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			mSummaryVideoView.setLayoutParams(layoutParamsRelative);
		} else {
			mSummaryClose.setVisibility(View.VISIBLE);
			mCameraName.setVisibility(View.VISIBLE);
			mSummaryDateParentLayout.setVisibility(View.VISIBLE);
			mSummaryMotionLayout.setVisibility(View.GONE);
			mSummarySoundLayout.setVisibility(View.GONE);
			mSummaryTemperatureLayout.setVisibility(View.VISIBLE);
			mSummaryTempText.setText(getString(R.string.temperature)+getString(R.string.summary_temp_with_in_range));
			setUpDetailLayout();
			LinearLayout.LayoutParams layoutParamsLinear = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					getResources().getDimensionPixelSize(R.dimen.viewfinder_player_height));
			RelativeLayout.LayoutParams layoutParamsRelative = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					getResources().getDimensionPixelSize(R.dimen.viewfinder_player_height));
			mSummaryVideoLayout.setLayoutParams(layoutParamsLinear);
			mSummaryVideoView.setLayoutParams(layoutParamsRelative);
		}
	}

	private void downloadDailySummaryFile() {
		mIsVideoDownloadInProgress = true;
		mIsImageDownloadInProgress = true;
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String dateString = df.format(mEventSummary.getDate());
		final String videoName = mRegistrationId + "@" + dateString + "@" + "SV" + ".mp4";

		File appFolder = HubbleApplication.getAppFolder();
		final File typeFolder = new File(appFolder, FileService.getUserFolder());
		if (typeFolder.exists()) {
			File downloadFile = new File(typeFolder, videoName);
			if(downloadFile.exists()){
				Toast.makeText(this, R.string.summary_video_exists, Toast.LENGTH_LONG).show();
				return;
			}
		}
		Toast.makeText(EventSummaryFullScreen.this, "Summary download is started", Toast.LENGTH_LONG).show();
		final int notificationId = Integer.valueOf(dateString);
		final NotificationManager notifyManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		SimpleDateFormat dfNotification = new SimpleDateFormat("dd MMM");
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.va_notification_title) + dfNotification.format(mEventSummary.getDate()))
				.setContentText(getString(R.string.va_notification_progress))
				.setSmallIcon(R.drawable.event_summary);
		FutureCallback<File> videoDownloadCallBack = new FutureCallback<File>() {
			@Override
			public void onCompleted(Exception e, File downloadFile) {
				mIsVideoDownloadInProgress = false;
				if (e != null) {
					builder.setContentText(getString(R.string.va_notification_fail))
							.setProgress(0,0,false);
					notifyManager.notify(notificationId, builder.build());
					Toast.makeText(EventSummaryFullScreen.this, R.string.download_firmware_error, Toast.LENGTH_LONG).show();
				} else {
					Uri intentUri = Uri.fromFile(new File(typeFolder, videoName));
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(intentUri, "video/mp4");
					PendingIntent pIntent = PendingIntent.getActivity(EventSummaryFullScreen.this, 0, intent, 0);
					builder.setContentText(getString(R.string.va_notification_complete))
							.setContentIntent(pIntent)
							.setAutoCancel(true)
							.setProgress(0,0,false);
					notifyManager.notify(notificationId, builder.build());
					Toast.makeText(EventSummaryFullScreen.this, R.string.saved_video, Toast.LENGTH_LONG).show();
				}
				Log.i(TAG, "Thumbnail for video download is complete");
			}
		};
		FileService.downloadFileWithProgressNotification(mEventSummary.getSummaryVideoUrlPath(), videoName, videoDownloadCallBack, notifyManager, builder, notificationId);

		String imageName = mRegistrationId + "@" + dateString + "@" + "SV" + ".jpg";
		FutureCallback<File> imageDownloadCallBack = new FutureCallback<File>() {
			@Override
			public void onCompleted(Exception e, File downloadFile) {
				mIsImageDownloadInProgress = false;
				Log.i(TAG, "Thumbnail for image download is complete");
			}
		};
		FileService.downloadFile(mEventSummary.getSnapShotPath(), imageName, imageDownloadCallBack);
	}

	private void dismissInfoDialog(){
		if(mInfoDialog != null && mInfoDialog.isShowing()){
			mInfoDialog.dismiss();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mSummaryVideoView != null && mSummaryVideoView.isPlaying()){
			mSummaryVideoView.pause();
		}
		dismissInfoDialog();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mSummaryVideoView != null){
			mSummaryVideoView.stopPlayback();
		}
	}
}


