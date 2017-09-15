package com.hubble.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.events.MessageEvent;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.response.EventResponse;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;
import com.squareup.picasso.Picasso;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHistoryArrayAdapter extends BaseAdapter {

	private final String TAG = "EventHistory";

	private int mConfig = -1;
	private Context mContext;
	private ArrayList<EventResponse> mEvents = new ArrayList<EventResponse>();
	private IEventHistoryActionCallBack mIEventHistoryActionCallBack;
	private Device mSelectedDevice = null;
	private Map<String, Device> mSelectedDeviceMap = new HashMap<String, Device>();
	private int mLayoutResId = -1;
	SimpleDateFormat sdf;
	private SecureConfig settings = HubbleApplication.AppConfig;

	public EventHistoryArrayAdapter(Context context, int config, int layoutResId,
	                                IEventHistoryActionCallBack eventHistoryActionCallBack, Device camera) {
		super();
		mContext = context;
		mConfig = config;
		mLayoutResId = layoutResId;
		mIEventHistoryActionCallBack = eventHistoryActionCallBack;
		mSelectedDevice = camera;
	}

	public EventHistoryArrayAdapter(Context context, int config, int layoutResId,
	                                IEventHistoryActionCallBack eventHistoryActionCallBack, List<Device> cameraList) {
		super();
		mContext = context;
		mConfig = config;
		mLayoutResId = layoutResId;
		if(cameraList != null){
			for (Device device : cameraList) {
				mSelectedDeviceMap.put(device.getProfile().getRegistrationId(),device);
			}
		}
		mIEventHistoryActionCallBack = eventHistoryActionCallBack;
	}

	public void setEventList(ArrayList<EventResponse> events, int pageNumber) {
		if (pageNumber == 0) {
			mEvents.clear();
		}
		mEvents.addAll(events);
		if (CommonUtil.getSettingInfo(mContext, SettingsPrefUtils.TIME_FORMAT_12, true)) {
			sdf = new SimpleDateFormat("hh:mm aa, dd MMM");
		} else {
			sdf = new SimpleDateFormat("HH:mm, dd MMM");
		}

	}

	public void clearEvents(){
		if(mEvents.size() > 0) {
			mEvents.clear();
		}
	}


	@Override
	public int getCount() {
		return mEvents.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(mLayoutResId, null);
			holder.eventSnapDetailLayout = (RelativeLayout)convertView.findViewById(R.id.timeline_event_layout);
			holder.eventTimeLineImage = (ImageView) convertView.findViewById(R.id.timeline_image);
			holder.eventTime = (TextView) convertView.findViewById(R.id.timeline_event_time);
			holder.eventType = (TextView) convertView.findViewById(R.id.timeline_event_name);
			holder.eventSnap = (ImageView) convertView.findViewById(R.id.timeline_event_snap);
			holder.eventZoom = (ImageView) convertView.findViewById(R.id.timeline_event_load_image);
			holder.eventPlay = (ImageView) convertView.findViewById(R.id.timeline_event_play_video);
			holder.eventSource = (ImageView) convertView.findViewById(R.id.timeline_event_source);
			holder.eventShare = (RelativeLayout) convertView.findViewById(R.id.timeline_event_share_layout);
			holder.eventShareButton = (Button)convertView.findViewById(R.id.timeline_event_share);
			if(mConfig == CommonConstants.CONFIG_NOTIFICATION){
				holder.eventCameraName = (TextView) convertView.findViewById(R.id.timeline_event_camera_name);
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if(mConfig == CommonConstants.CONFIG_NOTIFICATION){
			mSelectedDevice = mSelectedDeviceMap.get(mEvents.get(position).getDeviceRegistrationID());
		}
		setUpEventTypeScreen(mEvents.get(position), holder);
		return convertView;
	}

	@Override
	public long getItemId(int position) {
		return mEvents.get(position).getID();
	}

	@Override
	public Object getItem(int position) {
		return mEvents.get(position);
	}

	public class ViewHolder {
		RelativeLayout eventSnapDetailLayout;
		ImageView eventTimeLineImage;
		TextView eventTime;
		TextView eventType;
		ImageView eventSnap;
		ImageView eventZoom;
		ImageView eventPlay;
		ImageView eventSource;
		TextView eventCameraName;
		RelativeLayout eventShare;
		Button eventShareButton;
	}

	private void setUpEventTypeScreen(EventResponse event, ViewHolder viewHolder) {
		if(mSelectedDevice != null) {
			String time = CommonUtil.getTimeStampFromTimeZone(event.getEventTime(mSelectedDevice.getProfile().getTimeZone()), mSelectedDevice.getProfile().getTimeZone(), sdf);
			if (time != null) {
				viewHolder.eventTime.setText(time);
			}
		}
		viewHolder.eventType.setText(CommonUtil.getEventString(event, mContext));
		if(mConfig == CommonConstants.CONFIG_NOTIFICATION){
			viewHolder.eventCameraName.setText(event.getEventDevice().getDeviceName());
		}
		CommonUtil.EventType type = CommonUtil.EventType.fromAlertIntType(event.getAlertType());
		if (type == CommonUtil.EventType.MOTION)/* ||
		        type == CommonUtil.EventType.BABY_SMILE_DETECTION ||
			        type == CommonUtil.EventType.BABY_SLEEPING_CARE)*/ {
			setupMotionView(viewHolder, event);
		} else {
			setupGenericView(viewHolder);
		}
	}

	private void setupMotionView(ViewHolder viewHolder, final EventResponse event) {
		if (mConfig == CommonConstants.CONFIG_NOTIFICATION) {
			viewHolder.eventTimeLineImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.timeline_long_ntfcn));
		} else {
			viewHolder.eventTimeLineImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.timeline));
		}

		String eventImageURL = new String();
		ArrayList<String> eventFileList = new ArrayList<String>();
		String eventFilePath = new String();
		String eventMd5Sum = new String();
		String eventClipName = new String();

		EventResponse.EventData[] eventDatalist = event.getEventDataList();
		if (eventDatalist != null && eventDatalist.length > 0) {
			eventImageURL = eventDatalist[0].getImage();
			eventFilePath = eventDatalist[0].getFilePath();
			eventMd5Sum = eventDatalist[0].getMd5Sum();
			for(EventResponse.EventData eventData: eventDatalist){
				eventFileList.add(eventData.getFilePath());
			}
		}

		String tempClipName = new String();
		if (event.getStorageMode() == 1 && eventFilePath != null) {
			tempClipName = eventFilePath;
			if(mSelectedDevice != null) {
				eventFilePath = mSelectedDevice.getSDCardVideoFile(eventFilePath);
			}
			if (mConfig == CommonConstants.CONFIG_NOTIFICATION) {
				viewHolder.eventSource.setImageResource(R.drawable.sdcard_ntfcn);
			} else {
				viewHolder.eventSource.setImageResource(R.drawable.event_sdcard);
			}
		} else {
			if (mConfig == CommonConstants.CONFIG_NOTIFICATION) {
				viewHolder.eventSource.setImageResource(R.drawable.cloud_ntfcn);
			} else {
				viewHolder.eventSource.setImageResource(R.drawable.event_cloud);
			}
		}
		eventClipName = tempClipName;
		final String imageUrl = eventImageURL;
		viewHolder.eventSnapDetailLayout.setVisibility(View.VISIBLE);
		viewHolder.eventShare.setVisibility(View.VISIBLE);

		if (!eventImageURL.isEmpty()) {
			viewHolder.eventSnap.setVisibility(View.VISIBLE);
			Picasso.with(mContext).
					load(eventImageURL).
					placeholder(R.drawable.default_cam).
					error(R.drawable.default_cam).
					resizeDimen(R.dimen.event_log_motion_image_width, R.dimen.event_log_motion_image_height).
					into(viewHolder.eventSnap);
		}
		Date eventTime = null;
		if (mSelectedDevice != null) {
			eventTime = event.getEventTime(mSelectedDevice.getProfile().getTimeZone());
		}
		if (eventFilePath != null && !eventFilePath.isEmpty()) {
			viewHolder.eventZoom.setVisibility(View.GONE);
			viewHolder.eventPlay.setVisibility(View.VISIBLE);
			final String clipName = eventClipName;
			final String md5Sum = eventMd5Sum;
			final String filePath = eventFilePath;

			final EventVideo eventVideo = new EventVideo(imageUrl, filePath, clipName, md5Sum, event.getStorageMode(),
					eventTime, CommonUtil.getEventString(event, mContext),
					event.getDeviceRegistrationID());
			eventVideo.setVideoClipList(eventFileList);
			viewHolder.eventPlay.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIEventHistoryActionCallBack.onPlayVideo(eventVideo, event);
				}
			});
			viewHolder.eventSnap.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIEventHistoryActionCallBack.onPlayVideo(eventVideo, event);
				}
			});
			viewHolder.eventShare.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					shareVideoEvent(eventVideo);
				}
			});
			viewHolder.eventShareButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					shareVideoEvent(eventVideo);
				}
			});
		} else if (!eventImageURL.isEmpty()) {
			final EventImage eventImage = new EventImage(imageUrl, eventTime, CommonUtil.getEventString(event, mContext),
					event.getDeviceRegistrationID());
			viewHolder.eventZoom.setVisibility(View.VISIBLE);
			viewHolder.eventPlay.setVisibility(View.GONE);
			viewHolder.eventZoom.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIEventHistoryActionCallBack.onLoadFullScreenImage(eventImage, event);
				}
			});
			viewHolder.eventSnap.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIEventHistoryActionCallBack.onLoadFullScreenImage(eventImage, event);
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SINGLE_EVENT,AppEvents.EVENTTAB_ITEM_CLICKED,AppEvents.EVENT_ITEM_CLICKED);
					ZaiusEvent addCameraEvt = new ZaiusEvent(AppEvents.SINGLE_EVENT);
					addCameraEvt.action(AppEvents.EVENTTAB_ITEM_CLICKED);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(addCameraEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
				}
			});
			viewHolder.eventShare.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					shareImageEvent(imageUrl);
				}
			});
			viewHolder.eventShareButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					shareImageEvent(imageUrl);
				}
			});
		} else {
			if (mConfig == CommonConstants.CONFIG_NOTIFICATION) {
				viewHolder.eventTimeLineImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.timeline_short_ntfcn));
			} else {
				viewHolder.eventTimeLineImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.timeline_small));
			}
			viewHolder.eventShare.setVisibility(View.GONE);
			viewHolder.eventSnapDetailLayout.setVisibility(View.GONE);
		}
	}


	private void setupGenericView(ViewHolder viewHolder) {
		viewHolder.eventTimeLineImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.timeline_small));
		viewHolder.eventShare.setVisibility(View.GONE);
		viewHolder.eventSnapDetailLayout.setVisibility(View.GONE);
	}

	public void remove(EventResponse event) {
		if (mEvents.contains(event)) {
			mEvents.remove(event);
			notifyDataSetChanged();
		}
	}

	private void shareVideoEvent(EventVideo eventVideo){
		mIEventHistoryActionCallBack.shareVideoEvent(eventVideo, mSelectedDevice);
		GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_HISTORY,AppEvents.EH_SHARE_CLICKED,AppEvents.EVENT_HISTORY_SHARE);
		ZaiusEvent historyShareEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
		historyShareEvt.action(AppEvents.EH_SHARE_CLICKED);
		try {
			ZaiusEventManager.getInstance().trackCustomEvent(historyShareEvt);
		} catch (ZaiusException e) {
			e.printStackTrace();
		}

	}

	private void shareImageEvent(String imageUrl){
		mIEventHistoryActionCallBack.downloadAndShareEvent(imageUrl, MessageEvent.DOWNLOAD_AND_SHARE_IMAMGE,
				CommonConstants.ACTION_TYPE_SHARE, mSelectedDevice);
		GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_HISTORY,AppEvents.EH_SHARE_CLICKED,AppEvents.EVENT_HISTORY_SHARE);
		ZaiusEvent eventHistoryShareEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
		eventHistoryShareEvt.action(AppEvents.EH_SHARE_CLICKED);
		try {
			ZaiusEventManager.getInstance().trackCustomEvent(eventHistoryShareEvt);
		} catch (ZaiusException e) {
			e.printStackTrace();
		}
	}

}
