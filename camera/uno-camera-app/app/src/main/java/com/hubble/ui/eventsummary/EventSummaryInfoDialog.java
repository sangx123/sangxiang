package com.hubble.ui.eventsummary;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hubbleconnected.camera.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Admin on 13-03-2017.
 */
public class EventSummaryInfoDialog extends Dialog {
	private Context mContext;
	private TextView mEventSummaryTitle;
	private TextView mEventSummaryText;
	private TextView mOk;
	private RelativeLayout mDownloadLayout;
	private TextView mCancel;
	private TextView mDownload;

	private int mSummaryInfotype;
	private IEventDownloadCallBack mIEventDownloadCallBack;
	private Date mDate ;

	public EventSummaryInfoDialog(Context context, int type ) {
		super(context);
		mContext = context;
		mSummaryInfotype = type;
	}

	public EventSummaryInfoDialog(Context context, int type, Date date, IEventDownloadCallBack iEventDownloadCallBack) {
		super(context);
		mContext = context;
		mSummaryInfotype = type;
		mDate = date;
		mIEventDownloadCallBack = iEventDownloadCallBack;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setContentView(R.layout.event_summary_info_dialog);
		mEventSummaryTitle = (TextView)findViewById(R.id.summary_info_title);
		mEventSummaryText = (TextView)findViewById(R.id.summary_info_text);
		mOk = (TextView) findViewById(R.id.summary_info_ok);
		mDownloadLayout = (RelativeLayout)findViewById(R.id.summary_download);
		mCancel = (TextView) findViewById(R.id.summary_download_cancel);
		mDownload = (TextView) findViewById(R.id.summary_download_ok);
		if (mSummaryInfotype == EventSummaryConstant.DOWNLOAD_INFO) {
			mEventSummaryTitle.setAllCaps(false);
			mDownloadLayout.setVisibility(View.VISIBLE);
			mOk.setVisibility(View.GONE);
			SimpleDateFormat sdf =  new SimpleDateFormat("dd MMM yyyy");
			mEventSummaryTitle.setText(mContext.getString(R.string.summary_download_title, sdf.format(mDate)));
			mEventSummaryText.setText(mContext.getString(R.string.summary_download_text));
			mDownload.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					if(mIEventDownloadCallBack != null){
						mIEventDownloadCallBack.onDownloadClick();
					}
				}
			});
			mCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		} else if (mSummaryInfotype == EventSummaryConstant.SHARE_INFO){
			mEventSummaryTitle.setAllCaps(false);
			mDownloadLayout.setVisibility(View.VISIBLE);
			mOk.setVisibility(View.GONE);
			SimpleDateFormat sdf =  new SimpleDateFormat("dd MMM yyyy");
			mEventSummaryTitle.setText(mContext.getString(R.string.summary_share_title, sdf.format(mDate)));
			mEventSummaryText.setText(mContext.getString(R.string.summary_share_text));
			mDownload.setText(mContext.getString(R.string.share));
			mDownload.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					if(mIEventDownloadCallBack != null){
						mIEventDownloadCallBack.onDownloadClick();
					}
				}
			});
			mCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		} else {
			mEventSummaryTitle.setAllCaps(true);
			mDownloadLayout.setVisibility(View.GONE);
			mOk.setVisibility(View.VISIBLE);
			if (mSummaryInfotype == EventSummaryConstant.NO_MOTION_SUMMARY_VIEW) {
				mEventSummaryTitle.setText(mContext.getString(R.string.no_summary_video));
				mEventSummaryText.setText(mContext.getString(R.string.no_summary_video_info));
			} else if (mSummaryInfotype == EventSummaryConstant.NO_SUMMARY_VIEW) {
				mEventSummaryTitle.setText(mContext.getString(R.string.no_summary));
				mEventSummaryText.setText(mContext.getString(R.string.no_summary_info));
			}
			mOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}
	}

	public interface IEventDownloadCallBack {
		public void onDownloadClick();
	}
}
