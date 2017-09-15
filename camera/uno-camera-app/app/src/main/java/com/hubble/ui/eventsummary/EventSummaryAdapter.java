package com.hubble.ui.eventsummary;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.HubbleApplication;
import com.hubble.file.FileService;
import com.hubble.registration.PublicDefine;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;
import com.koushikdutta.async.future.FutureCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Admin on 07-03-2017.
 */
public class EventSummaryAdapter extends RecyclerView.Adapter {

	private final String TAG = "EventSummaryAdapter";
	private List<EventSummary> mEventSummaries = new ArrayList<EventSummary>();
	private IEventSummaryActionCallBack mIEventSummaryActionCallBack = null;
	private Activity mContext = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy");
	private boolean mItemColorIsDark = true;
	private boolean mIsVideoDownloadInProgress = true;
	private boolean mIsImageDownloadInProgress = true;
	private EventSummaryInfoDialog mInfoDialog = null;

	private String mRegId = null;

	public EventSummaryAdapter(Activity context, String regdId, IEventSummaryActionCallBack iEventSummaryActionCallBack) {
		this.mContext = context;
		this.mIEventSummaryActionCallBack = iEventSummaryActionCallBack;
		setRegdId(regdId);
	}

	public EventSummaryAdapter(Activity context, IEventSummaryActionCallBack iEventSummaryActionCallBack) {
		this.mContext = context;
		this.mIEventSummaryActionCallBack = iEventSummaryActionCallBack;
	}

	public void setRegdId(String regdID){
		mRegId = regdID;
	}

	public void setData(List<EventSummary> eventSummaries) {
		if (mEventSummaries != null) {
			mEventSummaries.clear();
			mEventSummaries.addAll(eventSummaries);
			notifyDataSetChanged();
		}
	}

	public void setItemAt(int position, EventSummary eventSummary ){
		if(mEventSummaries != null && mEventSummaries.size() > position) {
			mEventSummaries.set(position,eventSummary);
			notifyDataSetChanged();
		}
	}

	public boolean isDataAvailableToDelete(int position){
		if(mEventSummaries != null && mEventSummaries.size() > position) {
			EventSummary eventSummary = mEventSummaries.get(position);
			if (eventSummary.getSummaryType() == EventSummaryConstant.NO_SUMMARY_VIEW) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	public EventSummary getEventSummary(int position){
		if(mEventSummaries != null && mEventSummaries.size() > position) {
			return mEventSummaries.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
		final EventSummary eventSummary = mEventSummaries.get(position);
		switch (eventSummary.getSummaryType()) {
			case EventSummaryConstant.MOTION_SUMMARY_VIEW:
				final MotionSummaryViewHolder motionSummaryVH = (MotionSummaryViewHolder) holder;
				int color = eventSummary.getLayoutColor();
				if(color == 0) {
					if (mItemColorIsDark) {
						motionSummaryVH.eventSummaryParentLayout.setBackgroundColor(mContext.getResources().
								getColor(R.color.color_dark_grey_one));
						mItemColorIsDark = false;
						mEventSummaries.get(position).setLayoutColor(mContext.getResources().
								getColor(R.color.color_dark_grey_one));
					} else {
						motionSummaryVH.eventSummaryParentLayout.setBackgroundColor(mContext.getResources().
								getColor(R.color.color_dark_grey_two));
						mItemColorIsDark = true;
						mEventSummaries.get(position).setLayoutColor(mContext.getResources().
								getColor(R.color.color_dark_grey_two));
					}
				}else {
					motionSummaryVH.eventSummaryParentLayout.setBackgroundColor(eventSummary.getLayoutColor());
				}
				String date = sdf.format(eventSummary.getDate());
				motionSummaryVH.date.setText(date);
				String snapShotUrl = eventSummary.getSnapShotPath();
				if (eventSummary.getSnapShotPath() != null) {
					Picasso.with(mContext).
							load(snapShotUrl).
							placeholder(R.drawable.event_summary_default_snap).
							error(R.drawable.event_summary_default_snap).
							resize(153, 97).
							transform(new RoundedCornersTransform(10)).
							into(motionSummaryVH.snapshot1);
					Picasso.with(mContext).
							load(snapShotUrl).
							placeholder(R.drawable.event_summary_default_snap).
							error(R.drawable.event_summary_default_snap).
							resize(153, 97).
							transform(new RoundedCornersTransform(10)).
							into(motionSummaryVH.snapshot2);
					Picasso.with(mContext).
							load(snapShotUrl).
							placeholder(R.drawable.event_summary_default_snap).
							error(R.drawable.event_summary_default_snap).
							resize(153, 97).
							transform(new RoundedCornersTransform(10)).
							into(motionSummaryVH.snapshot3);
				}
				motionSummaryVH.download.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismissInfoDialog();
						mInfoDialog = new EventSummaryInfoDialog(mContext, EventSummaryConstant.DOWNLOAD_INFO, eventSummary.getDate(), new EventSummaryInfoDialog.IEventDownloadCallBack() {
							@Override
							public void onDownloadClick() {
								downloadDailySummaryFile(position, CommonConstants.ACTION_TYPE_DOWNLOAD);
							}
						});
						mInfoDialog.show();
					}
				});
				motionSummaryVH.share.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismissInfoDialog();
						mInfoDialog = new EventSummaryInfoDialog(mContext, EventSummaryConstant.SHARE_INFO, eventSummary.getDate(), new EventSummaryInfoDialog.IEventDownloadCallBack() {
							@Override
							public void onDownloadClick() {
								downloadDailySummaryFile(position, CommonConstants.ACTION_TYPE_SHARE);
							}
						});
						mInfoDialog.show();
					}
				});
				motionSummaryVH.eventSummaryParentLayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mIEventSummaryActionCallBack.onSummaryVideoPlay(eventSummary);
					}
				});
				motionSummaryVH.motionLayout.setVisibility(View.GONE);
				motionSummaryVH.soundLayout.setVisibility(View.GONE);
				motionSummaryVH.temperatureLayout.setVisibility(View.VISIBLE);
				motionSummaryVH.temperatureText.setText(mContext.getString(R.string.summary_temp_alert_with_in_range));

				if (eventSummary.getTotalMotionEvent() > 0) {
					motionSummaryVH.motionLayout.setVisibility(View.VISIBLE);
					motionSummaryVH.motionText.setText(mContext.
							getString(R.string.summary_motion_events, eventSummary.getTotalMotionEvent()));
				}

				if (eventSummary.getTotalSoundEvent() > 0) {
					motionSummaryVH.soundLayout.setVisibility(View.VISIBLE);
					motionSummaryVH.soundText.setText(mContext.
							getString(R.string.summary_sound_events, eventSummary.getTotalSoundEvent()));
				}
				if (eventSummary.getTempMax() != -100 && eventSummary.getTempMin() != -100) {
					motionSummaryVH.temperatureText.setText((mContext.getString(R.string.summary_temperature_events_range,
							eventSummary.getTempMin(), eventSummary.getTempMax())));
				}else if(eventSummary.getTempMax() != -100){
					motionSummaryVH.temperatureText.setText((mContext.getString(R.string.summary_temperature_events_above, eventSummary.getTempMax())));
				}else if(eventSummary.getTempMin() != -100){
					motionSummaryVH.temperatureText.setText((mContext.getString(R.string.summary_temperature_events_below, eventSummary.getTempMin())));
				}
				break;
			case EventSummaryConstant.NO_MOTION_SUMMARY_VIEW:
				final NoMotionSummaryViewHolder noMotionSummaryVH = (NoMotionSummaryViewHolder) holder;
				String date_no_motion = sdf.format(eventSummary.getDate());
				noMotionSummaryVH.date.setText(date_no_motion);
				noMotionSummaryVH.soundLayout.setVisibility(View.GONE);
				noMotionSummaryVH.temperatureLayout.setVisibility(View.VISIBLE);
				noMotionSummaryVH.temperatureText.setText(mContext.getString(R.string.summary_temp_alert_with_in_range));
				if (eventSummary.getTotalSoundEvent() > 0) {
					noMotionSummaryVH.soundLayout.setVisibility(View.VISIBLE);
					noMotionSummaryVH.soundText.setText(mContext.
							getString(R.string.summary_sound_events, eventSummary.getTotalSoundEvent()));
				}
				if (eventSummary.getTempMax() != -100 && eventSummary.getTempMin() != -100) {
					noMotionSummaryVH.temperatureText.setText((mContext.getString(R.string.summary_temperature_events_range,
							eventSummary.getTempMin(), eventSummary.getTempMax())));
				}else if(eventSummary.getTempMax() != -100){
					noMotionSummaryVH.temperatureText.setText((mContext.getString(R.string.summary_temperature_events_above, eventSummary.getTempMax())));
				}else if(eventSummary.getTempMin() != -100){
					noMotionSummaryVH.temperatureText.setText((mContext.getString(R.string.summary_temperature_events_below, eventSummary.getTempMin())));
				}
				noMotionSummaryVH.noMotionParentLayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismissInfoDialog();
						mInfoDialog = new EventSummaryInfoDialog(mContext,EventSummaryConstant.NO_MOTION_SUMMARY_VIEW);
						mInfoDialog.show();
					}
				});
				break;
			case EventSummaryConstant.NO_SUMMARY_VIEW:
				final NoSummaryViewHolder noSummaryVH = (NoSummaryViewHolder) holder;
				String date_no_summary = sdf.format(eventSummary.getDate());
				noSummaryVH.date.setText(date_no_summary);
				noSummaryVH.noSummaryParentLayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismissInfoDialog();
						mInfoDialog = new EventSummaryInfoDialog(mContext,EventSummaryConstant.NO_SUMMARY_VIEW);
						mInfoDialog.show();
					}
				});
				break;
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = null;
		switch (viewType) {
			case EventSummaryConstant.MOTION_SUMMARY_VIEW:
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_summary_item, parent, false);
				return new MotionSummaryViewHolder(view);
			case EventSummaryConstant.NO_MOTION_SUMMARY_VIEW:
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_summary_no_motion, parent, false);
				return new NoMotionSummaryViewHolder(view);
			case EventSummaryConstant.NO_SUMMARY_VIEW:
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_summary_no_data_item, parent, false);
				return new NoSummaryViewHolder(view);
		}
		return null;
	}

	@Override
	public int getItemCount() {
		return mEventSummaries.size();
	}

	@Override
	public int getItemViewType(int position) {
		return mEventSummaries.get(position).getSummaryType();
	}

	public static class MotionSummaryViewHolder extends RecyclerView.ViewHolder {
		RelativeLayout eventSummaryParentLayout;
		TextView date;
		ImageView share;
		ImageView download;
		ImageView snapshot1;
		ImageView snapshot2;
		ImageView snapshot3;
		LinearLayout motionLayout;
		TextView motionText;
		LinearLayout soundLayout;
		TextView soundText;
		LinearLayout temperatureLayout;
		TextView temperatureText;


		public MotionSummaryViewHolder(View itemView) {
			super(itemView);
			eventSummaryParentLayout = (RelativeLayout)itemView.findViewById(R.id.event_summary_layout);
			date = (TextView) itemView.findViewById(R.id.summary_date);
			share = (ImageView) itemView.findViewById(R.id.summary_share);
			download = (ImageView) itemView.findViewById(R.id.summary_download);
			snapshot1 = (ImageView) itemView.findViewById(R.id.summary_image1);
			snapshot2 = (ImageView) itemView.findViewById(R.id.summary_image2);
			snapshot3 = (ImageView) itemView.findViewById(R.id.summary_image3);
			motionLayout = (LinearLayout) itemView.findViewById(R.id.summary_motion);
			motionText = (TextView) itemView.findViewById(R.id.summary_motion_text);
			soundLayout = (LinearLayout) itemView.findViewById(R.id.summary_sound);
			soundText = (TextView) itemView.findViewById(R.id.summary_sound_text);
			temperatureLayout = (LinearLayout) itemView.findViewById(R.id.summary_temp);
			temperatureText = (TextView) itemView.findViewById(R.id.summary_temp_text);
		}
	}

	public static class NoMotionSummaryViewHolder extends RecyclerView.ViewHolder {

		RelativeLayout noMotionParentLayout;
		TextView date;
		LinearLayout soundLayout;
		TextView soundText;
		LinearLayout temperatureLayout;
		TextView temperatureText;

		public NoMotionSummaryViewHolder(View itemView) {
			super(itemView);
			noMotionParentLayout = (RelativeLayout)itemView.findViewById(R.id.no_motion_summary_parent);
			date = (TextView) itemView.findViewById(R.id.no_motion_summary_date);
			soundLayout = (LinearLayout) itemView.findViewById(R.id.no_motion_summary_sound);
			soundText = (TextView) itemView.findViewById(R.id.no_motion_summary_sound_text);
			temperatureLayout = (LinearLayout) itemView.findViewById(R.id.no_motion_summary_temp);
			temperatureText = (TextView) itemView.findViewById(R.id.no_motion_summary_temp_text);
		}
	}

	public static class NoSummaryViewHolder extends RecyclerView.ViewHolder {
		RelativeLayout noSummaryParentLayout;
		TextView date;

		public NoSummaryViewHolder(View itemView) {
			super(itemView);
			noSummaryParentLayout = (RelativeLayout)itemView.findViewById(R.id.no_summary_parent_layout);
			date = (TextView) itemView.findViewById(R.id.summary_no_data_date);
		}
	}

	private void downloadDailySummaryFile(int position, int actionType) {
		mIsVideoDownloadInProgress = true;
		mIsImageDownloadInProgress = true;
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String realDate = df.format(mEventSummaries.get(position).getDate());
		final String videoName =  mRegId + "@" + realDate + "@" + "SV" + ".mp4";
		File appFolder = HubbleApplication.getAppFolder();
		final File typeFolder = new File(appFolder, FileService.getUserFolder());
		if (typeFolder.exists()) {
			File downloadFile = new File(typeFolder, videoName);
			if(downloadFile.exists()){
				if(actionType == CommonConstants.ACTION_TYPE_DOWNLOAD) {
					Toast.makeText(mContext, R.string.summary_video_exists, Toast.LENGTH_LONG).show();
				}else if(actionType == CommonConstants.ACTION_TYPE_SHARE){
					Uri contentUri = FileService.getFileUri(downloadFile);
					Intent shareIntent = new Intent();
					shareIntent.setAction(Intent.ACTION_SEND);
					shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
					shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					shareIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					shareIntent.setType("video/mp4");
					mContext.startActivityForResult(shareIntent, PublicDefine.RESULT_SHARE_SNAPSHOT);
				}
				return;
			}
		}
		if(actionType == CommonConstants.ACTION_TYPE_DOWNLOAD) {
			Toast.makeText(mContext, "Summary download is started", Toast.LENGTH_LONG).show();
			final int notificationId = Integer.valueOf(realDate);
			final NotificationManager notifyManager =
					(NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			SimpleDateFormat dfNotification = new SimpleDateFormat("dd MMM");
			final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
			builder.setContentTitle(mContext.getString(R.string.va_notification_title) + dfNotification.format(mEventSummaries.get(position).getDate()))
					.setContentText(mContext.getString(R.string.va_notification_progress))
					.setSmallIcon(R.drawable.event_summary);
			FutureCallback<File> videoDownloadCallBack = new FutureCallback<File>() {
				@Override
				public void onCompleted(Exception e, File downloadFile) {
					mIsVideoDownloadInProgress = false;
					if (mContext != null) {
						if (e != null) {
							builder.setContentText(mContext.getString(R.string.va_notification_fail))
									.setProgress(0, 0, false);
							notifyManager.notify(notificationId, builder.build());
							Toast.makeText(mContext, R.string.download_firmware_error, Toast.LENGTH_LONG).show();
						} else {
							Uri intentUri = FileService.getFileUri(downloadFile);
							//Uri intentUri = Uri.fromFile(new File(typeFolder, videoName));
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(intentUri, "video/mp4");
							PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
							builder.setContentText(mContext.getString(R.string.va_notification_complete))
									.setContentIntent(pIntent)
									.setAutoCancel(true)
									.setProgress(0, 0, false);
							notifyManager.notify(notificationId, builder.build());
							Toast.makeText(mContext, R.string.saved_video, Toast.LENGTH_LONG).show();
						}
					}
					Log.i(TAG, "Thumbnail for video download is complete");
				}
			};

			FileService.downloadFileWithProgressNotification(mEventSummaries.get(position).getSummaryVideoUrlPath(), videoName, videoDownloadCallBack, notifyManager, builder, notificationId);
		} else {
			final ProgressDialog progressDialog = new ProgressDialog(mContext);
			progressDialog.setMessage(mContext.getString(R.string.downloading_video));
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.show();
			FutureCallback<File> videoShareCallBack = new FutureCallback<File>() {
				@Override
				public void onCompleted(Exception e, File downloadFile) {
					mIsVideoDownloadInProgress = false;
					if (mContext != null) {
						if (progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						if (e != null) {
							Toast.makeText(mContext, R.string.share_fail_error, Toast.LENGTH_SHORT).show();
						} else {

							Uri contentUri = FileService.getFileUri(downloadFile);
							Intent shareIntent = new Intent();
							shareIntent.setAction(Intent.ACTION_SEND);
							shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
							shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
							shareIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
							shareIntent.setType("video/mp4");
							mContext.startActivityForResult(shareIntent, PublicDefine.RESULT_SHARE_SNAPSHOT);
						}
					}
					Log.i(TAG, "Thumbnail for video download is complete");
				}
			};

			FileService.downloadFile(mEventSummaries.get(position).getSummaryVideoUrlPath(), videoName,FileService.getUserFolder(), videoShareCallBack, progressDialog);

		}
		String imageName = mRegId + "@" + realDate + "@" + "SV" + ".jpg";
		FutureCallback<File> imageDownloadCallBack = new FutureCallback<File>() {
			@Override
			public void onCompleted(Exception e, File downloadFile) {
				mIsImageDownloadInProgress = false;
				Log.i(TAG, "Thumbnail for image download is complete");
			}
		};
		FileService.downloadFile(mEventSummaries.get(position).getSnapShotPath(), imageName, imageDownloadCallBack);
	}

	public void dismissInfoDialog(){
		if(mInfoDialog != null && mInfoDialog.isShowing()){
			mInfoDialog.dismiss();
		}
	}

	public class RoundedCornersTransform implements Transformation {

		private final int radius;

		public RoundedCornersTransform(int radius) {
			this.radius = radius;
		}

		@Override public Bitmap transform(Bitmap source) {

			Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

			Canvas canvas = new Canvas(result);
			Paint shaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			BitmapShader shader = new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
			shaderPaint.setShader(shader);

			canvas.drawRoundRect(new RectF(0, 0, source.getWidth(), source.getHeight()), radius, radius, shaderPaint);

			if (source != result) {
				source.recycle();
			}
			return result;
		}

		@Override public String key() {
			return RoundedCornersTransform.class.getCanonicalName();
		}
	}
}
