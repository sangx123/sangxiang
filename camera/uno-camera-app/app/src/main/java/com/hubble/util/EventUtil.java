package com.hubble.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.answers.ShareEvent;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.events.MessageEvent;
import com.hubble.events.ShareEventData;
import com.hubble.file.FileService;
import com.hubble.registration.PublicDefine;
import com.hubble.ui.EventVideo;
import com.hubbleconnected.camera.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.media.ffmpeg.RecordStreamVideo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import base.hubble.constants.Streaming;

/**
 * Created by Admin on 28-12-2016.
 */
public class EventUtil {

	private final String TAG = "EventUtil";
	private Activity mActivity;

	private SimpleDateFormat downloadDf = new SimpleDateFormat("yyyyMMdd_hhmmssa");

	public IEventUtilCallBack mIEventUtilCallBack = null;

	public static final long EVENT_INTERMEDIATE_TIMEOUT = 10000;
	public static final long EVENT_FETCH_TIMEOUT = 60000;

	public interface Listener<T> {
		/** Called when a response is received. */
		void onResponse (T response);
	}


	public EventUtil(Activity activity) {
		mActivity = activity;
	}

	public void setCallBack(IEventUtilCallBack iEventUtilCallBack){
		mIEventUtilCallBack = iEventUtilCallBack;
	}

	public void fetchSDCardClipStatus(final String filepath, final String localFileStatuURL, final int type, final String clipName, final Device seletedDevice) {
		Log.i(TAG, "Local file status url: " + localFileStatuURL);
		// run query playlist immediately
		Ion.with(mActivity)
				.load("GET", localFileStatuURL)
				.setTimeout(4000)
				.asString()
				.setCallback(new FutureCallback<String>() {
					@Override
					public void onCompleted(Exception e, String result) {
						if(mIEventUtilCallBack != null) {
							mIEventUtilCallBack.onDismissDialog();
						}
						if (e != null) {
							Log.e(TAG, "Error when execute sd local file");
							Toast.makeText(mActivity,
									String.format(mActivity.getString(R.string.cannot_access_clip_on_sd_card), clipName),
									Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						} else {
							Log.i(TAG, "EXECUTE SD local file result: " + result + " status:" + getResultStatus(PublicDefine.GET_SDLOCAL_FILE_CMD, result));
							if (getResultStatus(PublicDefine.GET_SDLOCAL_FILE_CMD, result) == 0) {
								downloadAndShareEventUtil(filepath, MessageEvent.DOWNLOAD_AND_SHARE_VIDEO, type, seletedDevice);
							} else if (getResultStatus(PublicDefine.GET_SDLOCAL_FILE_CMD, result) == -1) {
								showNotifyDialog(mActivity.getString(R.string.error), mActivity.getString(R.string.unknown_error));
							} else if (getResultStatus(PublicDefine.GET_SDLOCAL_FILE_CMD, result) == -2) {
								showNotifyDialog(mActivity.getString(R.string.error), mActivity.getString(R.string.your_record_clip_is_not_found_on_camera_sd_card));
							} else if (getResultStatus(PublicDefine.GET_SDLOCAL_FILE_CMD, result) == -3) {
								showNotifyDialog(mActivity.getString(R.string.error), mActivity.getString(R.string.your_camera_sd_card_is_plugged_out));
							} else {
								showNotifyDialog(mActivity.getString(R.string.error), mActivity.getString(R.string.download_firmware_error));
							}
						}
					}
				});
	}



	public int getResultStatus(String cmd, String result) {
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
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setMessage(msg)
						.setTitle(title)
						.setPositiveButton(mActivity.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).show();
			}
		});
	}

	public void downloadAndShareEventUtil(String imageUrl, int eventShareType, int actionType, Device selectedDevice) {
		if(selectedDevice != null) {
			String imageName = new String();
			int messageId = R.string.downloading_snapshot;
			String shareType = "image/jpeg";
			String type = FileService.getUserFolder();
			final int typeVtech = eventShareType;
			String dateString = downloadDf.format(new Date(System.currentTimeMillis()));
			FileService.setFormatedFilePath(System.currentTimeMillis());
			if (eventShareType == MessageEvent.DOWNLOAD_AND_SHARE_VIDEO) {
				messageId = R.string.downloading_video;
				shareType = "video/flv";
				imageName = selectedDevice.getProfile().getName() + "@" + dateString + ".flv";
			} else {
				imageName = selectedDevice.getProfile().getName() + "@" + dateString + ".png";
			}
			final String finalShareType = shareType;
			final ShareEventData shareEventData = new ShareEventData(imageUrl, imageName);
			if (mActivity != null) {
				final ProgressDialog progressDialog = new ProgressDialog(mActivity);
				progressDialog.setMessage(mActivity.getString(messageId));
				if (CommonConstants.ACTION_TYPE_DOWNLOAD == actionType) {
					progressDialog.setCancelable(true);
				}else {
					progressDialog.setCancelable(false);
				}
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.show();
				final int finalActionType = actionType;

				FutureCallback<File> callback = new FutureCallback<File>() {
					@Override
					public void onCompleted(Exception e, File downloadFile) {
						if (progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						if (e != null) {
							if (CommonConstants.ACTION_TYPE_DOWNLOAD == finalActionType) {
								Toast.makeText(mActivity, R.string.download_firmware_error, Toast.LENGTH_SHORT).show();
							} else if (CommonConstants.ACTION_TYPE_SHARE == finalActionType) {
								Toast.makeText(mActivity, R.string.share_fail_error, Toast.LENGTH_SHORT).show();
							}
						} else {
							if (CommonConstants.ACTION_TYPE_DOWNLOAD == finalActionType) {
								Toast.makeText(mActivity, mActivity.getString(R.string.download_success), Toast.LENGTH_LONG).show();
							} else if (CommonConstants.ACTION_TYPE_SHARE == finalActionType) {
								Log.i(TAG, "File downloaded to: " + downloadFile.getAbsolutePath());
								Uri contentUri = FileService.getFileUri(downloadFile);
								Intent shareIntent = new Intent();
								shareIntent.setAction(Intent.ACTION_SEND);
							/*if (/*BuildConfig.FLAVOR.equals("vtech") && typeVtech == MessageEvent.DOWNLOAD_AND_SHARE_VIDEO) {
								shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.vtech_inform_view_file));
							}*/
								shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
								shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
								shareIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
								shareIntent.setType(finalShareType);
								mActivity.startActivityForResult(shareIntent, PublicDefine.RESULT_SHARE_SNAPSHOT);
							}
						}
					}
				};
				FileService.downloadFile(shareEventData.getImageUri(), shareEventData.getFileName(), type, callback, progressDialog);
			}
		}
	}

	public void downloadSnapShotForVideo(String imageUrl, Device selectedDevice) {
		String type = FileService.IMAGE_TYPE;
		String dateString = FileService.getFormatedFilePath();
		String imageName = selectedDevice.getProfile().getName() + "@" + dateString + ".jpg";
		FutureCallback<File> callback = new FutureCallback<File>() {
			@Override
			public void onCompleted(Exception e, File downloadFile) {
				Log.i(TAG,"Thumbnail for video download is complete");
			}
		};
		FileService.downloadFile(imageUrl, imageName, callback);
	}

	public void shareVideoUtil(final EventVideo eventVideo, final Device selectedDevice, final int type){
		if (eventVideo.getStorageMode() != 1 ) {
			if(mIEventUtilCallBack != null) {
				mIEventUtilCallBack.onDismissDialog();
			}
			downloadAndShareEventUtil(eventVideo.getFilePath(), MessageEvent.DOWNLOAD_AND_SHARE_VIDEO,
					type, selectedDevice);
		} else if (mActivity != null && selectedDevice != null && eventVideo.getStorageMode() == 1 ) {
			/*
			if(eventVideo.getEventTime() != null)
			{
				FileService.setFormatedFilePath(eventVideo.getEventTime().getTime());
			}
			else
			{
				FileService.setFormatedFilePath(System.currentTimeMillis());
			}
			*/
			FileService.setFormatedFilePath(System.currentTimeMillis());
			new AsyncTask<Void, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(Void... params) {
					boolean isCameraLocal = CameraAvailabilityManager.getInstance().
							isCameraInSameNetwork(HubbleApplication.AppContext, selectedDevice);
					return isCameraLocal;
				}

				@Override
				protected void onPostExecute(Boolean isCameraLocal) {
					if(isCameraLocal) {
						fetchSDCardClipStatus(eventVideo.getFilePath(), String.format(selectedDevice.getSDCardVideoFileStatus(), eventVideo.getClipName()),
								type, eventVideo.getClipName(), selectedDevice);
					}else {
						if(mIEventUtilCallBack != null) {
							mIEventUtilCallBack.onDismissDialog();
						}
						Intent motionIntent = new Intent(mActivity, RecordStreamVideo.class);
						motionIntent.putExtra(Streaming.EXTRA_REGISTRATION_ID, selectedDevice.getProfile().getRegistrationId());
						motionIntent.putExtra(Streaming.EXTRA_CLIP_NAME, eventVideo.getClipName());
						motionIntent.putExtra(Streaming.CAMERA_NAME, selectedDevice.getProfile().getName());
						motionIntent.putExtra(Streaming.EXTRA_MD5_SUM, eventVideo.getMd5Sum());
						motionIntent.putExtra(Streaming.EXTRA_EVENT_CODE, getEventCodeFromUrl(eventVideo.getImageURL()));
						if (type == CommonConstants.ACTION_TYPE_SHARE) {
							motionIntent.putExtra(PublicDefine.PREFS_DOWNLOAD_FOR, PublicDefine.DOWNLOAD_FOR_SHARING);
					    } else if(type == CommonConstants.ACTION_TYPE_DOWNLOAD) {
							motionIntent.putExtra(PublicDefine.PREFS_DOWNLOAD_FOR, PublicDefine.DOWNLOAD);
						}
						motionIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						mActivity.startActivity(motionIntent);
					}
				}
			}.execute();
		} else {
			if(mIEventUtilCallBack != null) {
				mIEventUtilCallBack.onDismissDialog();
			}
			Toast.makeText(mActivity, mActivity.getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_LONG);
		}
		downloadSnapShotForVideo(eventVideo.getImageURL(),selectedDevice);
	}



	public String getEventCodeFromUrl(String url) {

		String result = null;

		if (url != null) {
			int endIdx = url.indexOf(".jpg");
			int startIdx = endIdx - 33;

			try {
				result = url.substring(startIdx, endIdx);
			} catch (Exception e) {
			}

		}
		return result;
	}

	public interface IEventUtilCallBack {
        public void onDismissDialog();
	}
}
