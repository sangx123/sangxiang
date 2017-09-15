package com.hubble.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.events.MessageEvent;
import com.hubble.file.FileService;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceStatus;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.registration.PublicDefine;
import com.hubble.util.CommonConstants;
import com.hubble.util.EventUtil;
import com.hubbleconnected.camera.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.DeviceWakeup;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import base.hubble.PublicDefineGlob;
import base.hubble.constants.Streaming;
import cz.havlena.ffmpeg.ui.FFMpegPlaybackActivity;


/**
 * Created by Admin on 17-11-2016.
 */
public class EventHistoryFullScreenActivity extends AppCompatActivity {

	private final String TAG = "EventHistoryFullScreen";

	private final String USER_AGREE_FOR_REMOTE_ACCESS_SD_CARD = "user_agree_for_remote_access_sd_card";

	private RelativeLayout mFullScreenParentLayout;
	private ImageView mFullscreenImage;
	private ImageView mEventPlayImage;
	private TextView mEventTime;
	private TextView mEventText;
	private ImageView mEventDownload;
	private ImageView mEventDeleteButton;
	private ImageView mEventShare;
	private ProgressBar mProgressBar;


	private Device mSelectedDevice = null;


	private final int GET_DEVICE_STATUS_TASK_COMPLETED = 0;

	private int mStorageMode;
	private Intent mMotionIntent;
	private boolean mIsCameraLocal;
	private EventVideo mEventVideo;

	private ProgressDialog mProgressDialog = null;

	private final int WAKEUP_EVENT_DOWNLOAD = 0;
	private final int WAKEUP_EVENT_PLAY = 1;
	private final int WAKEUP_EVENT_SHARE = 2;

	private int mWakeUpReason = -1;
	private int mActionType = -1;

	private EventUtil mEventUtil;

	private DeviceWakeup mDeviceWakeup = null;

	private boolean isOrientationChange = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_history_full_screen);
		Intent intent = getIntent();
		if(savedInstanceState != null){
			isOrientationChange = true;
		}
		if (intent != null) {

			mFullScreenParentLayout = (RelativeLayout)findViewById(R.id.event_full_screen_parent);
			if(!isOrientationChange){
				mFullScreenParentLayout.setVisibility(View.INVISIBLE);
			}
			mFullscreenImage = (ImageView) findViewById(R.id.event_image);
			mEventPlayImage = (ImageView) findViewById(R.id.event_play_image);

			mEventTime = (TextView) findViewById(R.id.event_time);
			mEventText = (TextView) findViewById(R.id.event_text);

			mEventDownload = (ImageView) findViewById(R.id.event_download);
			mEventDeleteButton = (ImageView) findViewById(R.id.event_delete);
			mEventShare = (ImageView) findViewById(R.id.event_share);

			mEventUtil = new EventUtil(this);
			mEventUtil.setCallBack(new EventUtil.IEventUtilCallBack() {
				@Override
				public void onDismissDialog() {
					dismissProgressDialog();
				}
			});

			mProgressBar = (ProgressBar) findViewById(R.id.eventLog_progressBar);
			String eventType = intent.getStringExtra(CommonConstants.EVENT_TYPE);
			if (eventType.equalsIgnoreCase(CommonConstants.EVENT_VIDEO)) {
				EventVideo eventVideo = (EventVideo) intent.getSerializableExtra(CommonConstants.EVENT_VIDEO_TAG);
				boolean isDeviceSet = setSelectedDevice(eventVideo.getRegistrationId());
				if (isDeviceSet && mSelectedDevice != null) {
					setUpPlayVideo(eventVideo);
				} else {
					Toast.makeText(this, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_LONG).show();
					finish();
				}
			} else if (eventType.equalsIgnoreCase(CommonConstants.EVENT_IMAGE)) {
				EventImage eventImage = (EventImage) intent.getSerializableExtra(CommonConstants.EVENT_IMAGE_TAG);
				boolean isDeviceSet = setSelectedDevice(eventImage.getRegistrationId());
				if (isDeviceSet && mSelectedDevice != null) {
					setUpandLoadFullScreenImage(eventImage);
				} else {
					Toast.makeText(this, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_LONG).show();
					finish();
				}
			}

		} else {
			finish();
		}
	}



	public void setUpandLoadFullScreenImage(final EventImage eventImage) {
		setUpFullScreenEventResources(eventImage.getEventTime(), eventImage.getEventString());
		mEventPlayImage.setVisibility(View.GONE);
		loadImage(eventImage.getImageURL());
		mEventShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SINGLE_EVENT,AppEvents.EH_SHARE_CLICKED,AppEvents.EH_SHARE_CLICKED);
				ZaiusEvent ehShareEvt = new ZaiusEvent(AppEvents.EH_SHARE_CLICKED);
				ehShareEvt.action(AppEvents.EH_SHARE_CLICKED);
				try {
					ZaiusEventManager.getInstance().trackCustomEvent(ehShareEvt);
				} catch (ZaiusException e) {
					e.printStackTrace();
				}
				if(mSelectedDevice != null && mEventUtil != null) {
					mEventUtil.downloadAndShareEventUtil(eventImage.getImageURL(), MessageEvent.DOWNLOAD_AND_SHARE_IMAMGE,
							CommonConstants.ACTION_TYPE_SHARE, mSelectedDevice);
				}
			}
		});
		mEventDownload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SINGLE_EVENT,AppEvents.EH_DOWNLOAD_CLICKED,AppEvents.EH_DOWNLOAD_CLICKED);
				ZaiusEvent ehDownloadEvt = new ZaiusEvent(AppEvents.EH_DOWNLOAD);
				ehDownloadEvt.action(AppEvents.EH_DOWNLOAD_CLICKED);
				try {
					ZaiusEventManager.getInstance().trackCustomEvent(ehDownloadEvt);
				} catch (ZaiusException e) {
					e.printStackTrace();
				}
				if(mSelectedDevice != null && mEventUtil != null) {
					mEventUtil.downloadAndShareEventUtil(eventImage.getImageURL(), MessageEvent.DOWNLOAD_AND_SHARE_IMAMGE,
							CommonConstants.ACTION_TYPE_DOWNLOAD, mSelectedDevice);
				}
			}
		});
	}

	private void setUpPlayVideo(final EventVideo eventVideo) {
		final Device selectedDevice = mSelectedDevice;
		mEventVideo = eventVideo;
		mIsCameraLocal = eventVideo.isLocal();
		mStorageMode = eventVideo.getStorageMode();


		final Intent motionIntent = new Intent(this, FFMpegPlaybackActivity.class);
		if(mEventUtil != null) {
			motionIntent.putExtra(Streaming.EXTRA_EVENT_CODE, mEventUtil.getEventCodeFromUrl(eventVideo.getImageURL()));
		}
		motionIntent.putExtra(Streaming.EXTRA_REGISTRATION_ID, selectedDevice.getProfile().getRegistrationId());
		motionIntent.putExtra(Streaming.CAMERA_NAME, selectedDevice.getProfile().getName());
		motionIntent.putExtra(FFMpegPlaybackActivity.COME_FROM, FFMpegPlaybackActivity.COME_FROM_EVENT_HISTORY);
		motionIntent.putStringArrayListExtra(Streaming.EVENT_VIDEO_CLIP_LIST, eventVideo.getVideoClipList());
		Log.d(TAG, "Camera is in local: " + mIsCameraLocal);
		if (eventVideo.getStorageMode() == 1) {
			motionIntent.putExtra(Streaming.EXTRA_CLIP_NAME, eventVideo.getClipName());
			motionIntent.putExtra(Streaming.EXTRA_MD5_SUM, eventVideo.getMd5Sum());
			motionIntent.putExtra(Streaming.EXTRA_LOCAL_URL, selectedDevice.getLocalFilePrefixURL());
			motionIntent.putExtra(Streaming.EXTRA_GET_CLIP_STATUS_URL, selectedDevice.getSDCardVideoFileStatus());
			motionIntent.putExtra(Streaming.EXTRA_IS_CLIP_ON_SDCARD, true);
			//motionIntent.putExtra(Streaming.EXTRA_IS_LOCAL_CAMERA, mIsCameraLocal);
		}
		//	playVideo(isCameraInLocal, eventVideo.storageMode, motionIntent);
		mMotionIntent = motionIntent;
		//Start playing video directly
		int delayDuration = 1000;
		if(!isOrientationChange) {
			if (mSelectedDevice.getProfile().isStandBySupported() &&
					eventVideo.getStorageMode() == 1) {
				showProgressDialog(getString(R.string.please_wait));
				mWakeUpReason = WAKEUP_EVENT_PLAY;
				checkDeviceStatus();
				delayDuration = 6000;
			} else {
				playVideo();
				delayDuration = 3000;
			}
		}


		mEventPlayImage.setVisibility(View.VISIBLE);
		mEventPlayImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedDevice.getProfile().isStandBySupported() &&
						eventVideo.getStorageMode() == 1){
					showProgressDialog(getString(R.string.please_wait));
					mWakeUpReason = WAKEUP_EVENT_PLAY;
                    checkDeviceStatus();
				} else {
					playVideo();
				}
			}
		});
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				setUpFullScreenEventResources(eventVideo.getEventTime(), eventVideo.getEventString());
				loadImage(eventVideo.getImageURL());
			}
		}, delayDuration);

		mEventShare.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(mSelectedDevice.getProfile().isStandBySupported() &&
						eventVideo.getStorageMode() == 1)
				{
					File clipFile = null;
					if(eventVideo.getEventTime() != null) {
						clipFile = FileService.getFormatedFilePathForVideo(mSelectedDevice.getProfile().getName(), eventVideo.getEventTime().getTime());
					}
					if(clipFile != null && clipFile.exists())
					{
						Uri contentUri = FileService.getFileUri(clipFile);
						Intent shareIntent = new Intent();
						shareIntent.setAction(Intent.ACTION_SEND);
						shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
						shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						shareIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
						shareIntent.setType("video/flv");
						startActivity(shareIntent);
					}
					else
					{
						showProgressDialog(getString(R.string.please_wait));
						mWakeUpReason = WAKEUP_EVENT_SHARE;
						checkDeviceStatus();
					}
				} else if(mEventUtil != null && mSelectedDevice != null) {
					showProgressDialog(getString(R.string.please_wait));
					mEventUtil.shareVideoUtil(mEventVideo, mSelectedDevice, CommonConstants.ACTION_TYPE_SHARE);
				} else {
					Toast.makeText(EventHistoryFullScreenActivity.this, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_SHORT);
				}
			}
		});

		mEventDownload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog(getString(R.string.please_wait));
				if (mSelectedDevice.getProfile().isStandBySupported() &&
						eventVideo.getStorageMode() == 1) {
					showProgressDialog(getString(R.string.please_wait));
					mWakeUpReason = WAKEUP_EVENT_DOWNLOAD;
					checkDeviceStatus();
				} else if(mEventUtil != null && mSelectedDevice != null) {
					showProgressDialog(getString(R.string.please_wait));
					mEventUtil.shareVideoUtil(mEventVideo, mSelectedDevice, CommonConstants.ACTION_TYPE_DOWNLOAD);
				} else {
					Toast.makeText(EventHistoryFullScreenActivity.this, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_SHORT);
				}
			}
		});
	}

	private void setUpFullScreenEventResources(Date eventTime, String eventText) {
		mFullScreenParentLayout.setVisibility(View.VISIBLE);
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd");
		String tempVal = sdf1.format(eventTime);
		String dateSuffix = getDayNumberSuffix(Integer.parseInt(tempVal));
		SimpleDateFormat sdf2 = new SimpleDateFormat("'at' HH:mm 'on' dd'" + dateSuffix + "' MMMM");

		if (CommonUtil.getSettingInfo(getApplicationContext(), SettingsPrefUtils.TIME_FORMAT_12, true)) {

			sdf2 = new SimpleDateFormat("'at' hh:mm aa 'on' dd'" + dateSuffix + "' MMMM");
		}

		if(mSelectedDevice != null && eventTime != null) {
			String timeDateString = CommonUtil.getTimeStampFromTimeZone(eventTime, mSelectedDevice.getProfile().getTimeZone(), sdf2);
			if (timeDateString != null) {
				mEventTime.setText(timeDateString);
			}
		}
		mEventText.setText(eventText);
		mEventDeleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SINGLE_EVENT,AppEvents.EH_DELETE_CLICKED,AppEvents.EH_DELETE_CLICKED);

				ZaiusEvent eventDeleteClickedEvt = new ZaiusEvent(AppEvents.EH_DELETE);
				eventDeleteClickedEvt.action(AppEvents.EH_DELETE_CLICKED);
				try {
					ZaiusEventManager.getInstance().trackCustomEvent(eventDeleteClickedEvt);
				} catch (ZaiusException e) {
					e.printStackTrace();
				}

				new AlertDialog.Builder(EventHistoryFullScreenActivity.this)
						.setMessage(getString(R.string.deletion_confirmation_dialog))
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Intent returnIntent = new Intent();
								setResult(Activity.RESULT_OK, returnIntent);
								finish();
								GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SINGLE_EVENT,AppEvents.EH_DELETE+" : ok",AppEvents.EH_DELETE);
								ZaiusEvent deleteOkEvt = new ZaiusEvent(AppEvents.EH_DELETE);
								deleteOkEvt.action(AppEvents.EH_DELETE+" : ok");
								try {
									ZaiusEventManager.getInstance().trackCustomEvent(deleteOkEvt);
								} catch (ZaiusException e) {
									e.printStackTrace();
								}
							}
						})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// do nothing
								GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SINGLE_EVENT,AppEvents.EH_DELETE+" : cancel",AppEvents.EH_DELETE);
								ZaiusEvent deleteCancelEvt = new ZaiusEvent(AppEvents.EH_DELETE);
								deleteCancelEvt.action(AppEvents.EH_DELETE+" : cancel");
								try {
									ZaiusEventManager.getInstance().trackCustomEvent(deleteCancelEvt);
								} catch (ZaiusException e) {
									e.printStackTrace();
								}
							}
						})
						.show();
			}
		});
	}

	private void loadImage(String imageURL) {
		mProgressBar.setVisibility(View.VISIBLE);
		try {
			ImageLoader.getInstance().loadImage(imageURL, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					mFullscreenImage.setImageResource(android.R.color.transparent);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					mProgressBar.setVisibility(View.GONE);
					mFullscreenImage.setImageBitmap(loadedImage);
				}

			});
		} catch (Exception exception) {
			Toast.makeText(this, this.getResources().getString(R.string.image_loading_failure_message), Toast.LENGTH_LONG).show();
			hideFullscreenImageView();
		}
	}


	private void playVideo() {
		if(mSelectedDevice != null) {
			if(mStorageMode == 1) {
				showProgressDialog(getString(R.string.please_wait));
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						mIsCameraLocal = CameraAvailabilityManager.getInstance().
								isCameraInSameNetwork(HubbleApplication.AppContext, mSelectedDevice);
						return null;
					}

					@Override
					protected void onPostExecute(Void aVoid) {
						dismissProgressDialog();
						if (mMotionIntent != null) {
							mMotionIntent.putExtra(Streaming.EXTRA_IS_LOCAL_CAMERA, mIsCameraLocal);
							if (!mIsCameraLocal && mStorageMode == 1 &&
									!HubbleApplication.AppConfig.getBoolean(USER_AGREE_FOR_REMOTE_ACCESS_SD_CARD, false)) {
								AlertDialog.Builder builder = new AlertDialog.Builder(EventHistoryFullScreenActivity.this);
								builder.setTitle(getString(R.string.warning))
										.setMessage(getString(R.string.access_sdcard_clip_warning))
										.setPositiveButton(getString(R.string.proceed), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												HubbleApplication.AppConfig.putBoolean(USER_AGREE_FOR_REMOTE_ACCESS_SD_CARD, true);
												startActivity(mMotionIntent);
											}
										})
										.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}

										});
								builder.create().show();
							} else {
								startActivity(mMotionIntent);
							}
						}
					}
				}.execute();
			}else {
				//If the video is in cloud no need to check iscamera local
				dismissProgressDialog();
				mMotionIntent.putExtra(Streaming.EXTRA_IS_LOCAL_CAMERA, false);
				startActivity(mMotionIntent);
			}
		}else {
			dismissProgressDialog();
			Toast.makeText(this,getString(R.string.event_full_screen_fail_camera),Toast.LENGTH_LONG);
		}

	}



	@Override
	public void onBackPressed() {
		hideFullscreenImageView();
		super.onBackPressed();
	}




	private void hideFullscreenImageView() {
		Intent returnIntent = new Intent();
		setResult(Activity.RESULT_CANCELED, returnIntent);
		finish();
	}

	private String getDayNumberSuffix(int day) {
		if (day >= 11 && day <= 13) {
			return "th";
		}
		switch (day % 10) {
			case 1:
				return "st";
			case 2:
				return "nd";
			case 3:
				return "rd";
			default:
				return "th";
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PublicDefine.RESULT_SHARE_SNAPSHOT) {
		}
	}

	private boolean setSelectedDevice(String registrationId){
		List<Device> deviceList = DeviceSingleton.getInstance().getDevices();
		if(deviceList != null){
			for(Device device : deviceList){
				if(device != null && device.getProfile().
						getRegistrationId().equalsIgnoreCase(registrationId)){
					mSelectedDevice = device;
					return true;
				}
			}
		}
		return false;
	}


	private void  checkDeviceStatus()
	{
		String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");

		final DeviceStatus deviceStatus = new DeviceStatus(accessToken,mSelectedDevice.getProfile().getRegistrationId());

		DeviceManagerService.getInstance(this).getDeviceStatus(deviceStatus,new Response.Listener<StatusDetails>()
				{
					@Override
					public void onResponse(StatusDetails response)
					{
						//dismissDialog();
						if(response != null)
						{
							StatusDetails.StatusResponse[] statusResponseList = response.getDeviceStatusResponse();

							StatusDetails.StatusResponse statusResponse = null;

							if(statusResponseList != null && statusResponseList.length > 0)
							{
								statusResponse = statusResponseList[0]; // fetch first object only
							}

							if(statusResponse != null)
							{
								StatusDetails.DeviceStatusResponse deviceStatusResponse = statusResponse.getDeviceStatusResponse();
								String deviceStatus = deviceStatusResponse.getDeviceStatus();

								Log.d(TAG,"device status :- " + deviceStatus);

								if(deviceStatus != null)
								{
									if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE)==0)
									{
										mSelectedDevice.getProfile().setAvailable(true);
										mSelectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
										Log.d(TAG, "device online..start streaming");
										switch (mWakeUpReason){
											case WAKEUP_EVENT_PLAY:
												playVideo();
												break;
											case WAKEUP_EVENT_DOWNLOAD:
												if(mEventUtil != null && mSelectedDevice != null) {
													mEventUtil.shareVideoUtil(mEventVideo, mSelectedDevice, CommonConstants.ACTION_TYPE_DOWNLOAD);
												}
												break;
											case WAKEUP_EVENT_SHARE:
												if(mEventUtil != null && mSelectedDevice != null) {
													mEventUtil.shareVideoUtil(mEventVideo, mSelectedDevice, CommonConstants.ACTION_TYPE_SHARE);
												}
												break;
											 default:
												 dismissProgressDialog();
												 break;
										}
									}
									else if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_STANDBY)==0)
									{
										mSelectedDevice.getProfile().setAvailable(false);
										mSelectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);
										Log.d(TAG, "device standby..wakeup");
										//wakeup device
										wakeUpRemoteDevice();
									}
									else if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_OFFLINE)==0)
									{
										dismissProgressDialog();
										Log.d(TAG, "setting device available false");
										mSelectedDevice.getProfile().setAvailable(false);
										mSelectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
										//device offline
										Toast.makeText(getApplicationContext(), getString(R.string.camera_offline),Toast.LENGTH_SHORT).show();
									} else {
										dismissProgressDialog();
										Toast.makeText(getApplicationContext(), getString(R.string.event_full_screen_fail_camera),Toast.LENGTH_SHORT).show();
									}
								} else {
									dismissProgressDialog();
									Toast.makeText(getApplicationContext(), getString(R.string.event_full_screen_fail_camera),Toast.LENGTH_SHORT).show();
								}
							} else {
								dismissProgressDialog();
								Toast.makeText(getApplicationContext(), getString(R.string.event_full_screen_fail_camera),Toast.LENGTH_SHORT).show();
							}
						} else {
							dismissProgressDialog();
							Toast.makeText(getApplicationContext(), getString(R.string.event_full_screen_fail_camera),Toast.LENGTH_SHORT).show();
						}
					}
				},
				new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse (VolleyError error)
					{
						dismissProgressDialog();
						if (error != null && error.networkResponse != null)
						{
							Log.d(TAG, error.networkResponse.toString());
							Log.d(TAG, "Error Message :- " +new String(error.networkResponse.data));
						}
						Toast.makeText(getApplicationContext(), getString(R.string.event_full_screen_fail_camera),Toast.LENGTH_SHORT).show();
					}
				});

	}

	private void wakeUpRemoteDevice() {
		showProgressDialog(getString(R.string.viewfinder_progress_wakeup));
		Log.d(TAG, "wakeUpRemoteDevice");
		String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
		mDeviceWakeup = DeviceWakeup.newInstance();
		mDeviceWakeup.wakeupDevice(mSelectedDevice.getProfile().registrationId, accessToken, mDeviceHandler,mSelectedDevice);


	}

	private Handler  mDeviceHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case CommonConstants.DEVICE_WAKEUP_STATUS:

					/*if(mProgressDialog != null && mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}*/
					boolean result = (boolean)msg.obj;
					Log.d(TAG, "Device status task completed..device status:"+result);
					if(result)
					{
						switch (mWakeUpReason){
							case WAKEUP_EVENT_PLAY:
								playVideo();
								break;
							case WAKEUP_EVENT_DOWNLOAD:
								if(mEventUtil != null && mSelectedDevice != null) {
									mEventUtil.shareVideoUtil(mEventVideo, mSelectedDevice, CommonConstants.ACTION_TYPE_DOWNLOAD);
								}
								break;
							case WAKEUP_EVENT_SHARE:
								if(mEventUtil != null && mSelectedDevice != null) {
									mEventUtil.shareVideoUtil(mEventVideo, mSelectedDevice, CommonConstants.ACTION_TYPE_SHARE);
								}
								break;
							default:
								dismissProgressDialog();
								break;
						}
					}
					else
					{
						dismissProgressDialog();
						Log.d(TAG, "wakeup device:failure");
						Toast.makeText(getApplicationContext(),getResources().getString(R.string.failed_to_start_device),Toast.LENGTH_LONG).show();
					}

					break;

			}
		}
	};

	private void showProgressDialog(String message){
		dismissProgressDialog();
		if (mProgressDialog != null) {
			mProgressDialog.setMessage(message);
			mProgressDialog.show();
		}
	}

	private void dismissProgressDialog(){
		if(mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}


	@Override
	protected void onPause() {
		super.onPause();
		dismissProgressDialog();
		mProgressDialog = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setCancelable(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mDeviceWakeup != null) {
			mDeviceWakeup.cancelTask(mSelectedDevice.getProfile().registrationId,mDeviceHandler);
		}
		mMotionIntent = null;
	}
}


