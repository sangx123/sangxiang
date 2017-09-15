package com.sensor.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.blinkhd.SwipeDismissListViewTouchListener;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.file.FileService;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.AllDeviceEvent;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeleteEvent;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceStatus;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeleteEventDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceEventDetail;
import com.hubble.framework.service.cloudclient.device.pojo.response.EventResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.notifications.NotificationReceiver;
import com.hubble.registration.PublicDefine;
import com.hubble.ui.EventHistoryArrayAdapter;
import com.hubble.ui.EventHistoryFullScreenActivity;
import com.hubble.ui.EventImage;
import com.hubble.ui.EventVideo;
import com.hubble.ui.IEventHistoryActionCallBack;
import com.hubble.util.CommonConstants;
import com.hubble.util.EventUtil;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.DeviceWakeup;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import base.hubble.PublicDefineGlob;


public class NotificationFragment extends Fragment implements IEventHistoryActionCallBack {

	private final String TAG = "NotificationFragment";
	private final int FULL_SCREEN_REQUEST_CODE = 1;

	private BroadcastReceiver mRefreshEventsBroadcastReceiver = null;
	private Context mContext = null;
	private DeviceManagerService mDeviceManagerService = null;
	private AllDeviceEvent mAllDeviceEvent = null;
	private DeleteEvent mDeleteEvent = null;

	private SwipeRefreshLayout mSwipeRefreshLayout = null;
	private ListView mListView = null;
	private View footerView = null;
	private RelativeLayout mNoNotificationLayout = null;
	private TextView mNoNotificationText = null;
	private Button mNoNotificationButton = null;
	private ImageView mFooterProgressView = null;
	private ProgressBar mProgressBar = null;
	private ImageView mBackToTop;
	private TextView mIntermediateTextView;

	private EventHistoryArrayAdapter mEventHistoryArrayAdapter = null;
	private List<Device> mAllDevices = null;
	private EventResponse mLoadEvent = null;
	private Animation mProgressAnim = null;
	private SwipeDismissListViewTouchListener touchListener = null;
	private ArrayList<EventResponse> mDeleteIndex = new ArrayList<EventResponse>();



	private boolean mLoadingMoreItems = false;
	private boolean mIsNotificationFetchInProgress = false;
	private boolean mIsNotificationDeleteInProgress = false;
	private boolean isRefresh = false;
	private boolean isEndReached = false;
	private boolean mIsPendingNewEvent = false;

	private int mPageNumber = 0;
	private int mTotalCount = -1;

	private int mTotaPageCount = 0;

	private ProgressDialog mProgressDialog = null;
	private DeviceWakeup mDeviceWakeup = null;

	private Device mShareDevice;

	private EventVideo mEventVideo;
	private EventUtil mEventUtil;
	private long eventLoadStartTime;
	private long eventLoadTime;
	private boolean isEventLoadTime ;

	private Timer mEventTimer = null;
	private int mEventTimerCount = 0;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getContext();
		//Initialize device instance
		String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
		if (mDeviceManagerService == null) {
			mDeviceManagerService = DeviceManagerService.getInstance(getContext());
		}
		if (mAllDeviceEvent == null) {
			mAllDeviceEvent = new AllDeviceEvent(accessToken);
			mAllDeviceEvent.setSize(CommonConstants.EVENT_PAGE_SIZE);
		}

		mRefreshEventsBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG," New event notification");
				String notificationCamera = intent.getStringExtra(NotificationReceiver.REFRESH_EVENTS_BROADCAST);
				//if(selectedCamera != null && selectedCamera.getProfile().getRegistrationId().equals(notificationCamera)) {
				newEventArrived();
				//}
			}
		};
		mProgressAnim = AnimationUtils.loadAnimation(mContext, R.anim.image_rotate);
		mEventUtil = new EventUtil(getActivity());
		mEventUtil.setCallBack(new EventUtil.IEventUtilCallBack() {
			@Override
			public void onDismissDialog() {
				dismissProgressDialog();
			}
		});
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		Log.d(TAG,"onCreateView of NotificationFragment");
		View view = inflater.inflate(R.layout.fragment_notification, null);
		eventLoadStartTime = System.currentTimeMillis();
		isEventLoadTime = false;
		footerView = inflater.inflate(R.layout.loading_view, null, false);
		mFooterProgressView = (ImageView)footerView.findViewById(R.id.footer_progress);
		mAllDevices = DeviceSingleton.getInstance().getDevices();
		mNoNotificationLayout = (RelativeLayout)view.findViewById(R.id.no_notification_layout);
		mNoNotificationText = (TextView)view.findViewById(R.id.no_notification_text_detail);
		mNoNotificationButton = (Button)view.findViewById(R.id.no_notification_retry_plan);
		mNoNotificationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showFooterView();
				fetchAllEventList(0);
			}
		});
		isRefresh = true;
		isEndReached = false;
		mIsNotificationDeleteInProgress = false;
		mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.notification_swipeview);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.app_refreshview_color_1,
				R.color.app_refreshview_color_2,
				R.color.app_refreshview_color_3,
				R.color.app_refreshview_color_4);
		mSwipeRefreshLayout.setEnabled(false);
		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Log.d(TAG,"User is refreshing. Fetching all events");
				refreshEventList();
			}
		});

		mListView = (ListView) view.findViewById(R.id.notification_listview);
		mListView.addFooterView(footerView);
		showFooterView();

		mBackToTop = (ImageView)view.findViewById(R.id.notification_move_to_top);
		mBackToTop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBackToTop.setVisibility(View.GONE);
				mListView.smoothScrollToPosition(0);
			}
		});

		mIntermediateTextView = (TextView)view.findViewById(R.id.notification_noItemsHolder);

		//if (mEventHistoryArrayAdapter == null) {
			mEventHistoryArrayAdapter = new EventHistoryArrayAdapter(mContext,
					CommonConstants.CONFIG_NOTIFICATION, R.layout.notifcation_item,this, mAllDevices);
		//}
		mListView.setAdapter(mEventHistoryArrayAdapter);

		mListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if(mEventHistoryArrayAdapter.getCount() != 0 &&
						mEventHistoryArrayAdapter.getCount() <= mListView.getLastVisiblePosition()+ 1){
					hideFooterView();
				}
			}
		});
		// Infinite scroll and on-demand loading
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				if(firstVisibleItem == 0){
					if(mIsPendingNewEvent){
						Log.d(TAG,"View scrolled to top. Fetching all events");
						refreshEventList();
					}
					mBackToTop.setVisibility(View.GONE);
				} else {
					mBackToTop.setVisibility(View.VISIBLE);
				}
				//what is the bottom item that is visible
				int lastInScreen = firstVisibleItem + visibleItemCount;

				//is the bottom item visible & not loading more already? Load more!
				int threshold = totalItemCount - CommonConstants.EVENT_PAGE_SIZE / 2;
				if ((threshold > 0 && lastInScreen >= threshold) && !(mLoadingMoreItems) && !(isEndReached)
						&& mPageNumber < mTotaPageCount) {
					isRefresh = false;
					mPageNumber++;
					showFooterView();
					Log.d(TAG,"Last page reached. Fetching next page events");
					fetchAllEventList(mPageNumber);
					mLoadingMoreItems = true;
				}
			}
		});
		touchListener = new SwipeDismissListViewTouchListener(mListView,
				new SwipeDismissListViewTouchListener.DismissCallbacks() {
					@Override
					public boolean canDismiss(int position) {
						return true;
					}

					@Override
					public void onDismiss(ListView listView, int[] reverseSortedPositions) {
						final int[] sortedPosition = reverseSortedPositions;
						if (mContext != null && isAdded()) {
							new AlertDialog.Builder(mContext)
									.setMessage(mContext.getString(R.string.deletion_confirmation_dialog))
									.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											mProgressBar.setVisibility(View.VISIBLE);
											String mId = new String();
											mDeleteIndex.clear();
											for (int i = 0; i < sortedPosition.length; i++) {
												int position = sortedPosition[i];
												mDeleteIndex.add((EventResponse) mEventHistoryArrayAdapter.getItem(position));
												if (i > 0) {
													mId = mId + ",";
												}
												mId = mId + mDeleteIndex.get(mDeleteIndex.size() - 1).getID();
												deleteEventForCamera(mId, ((EventResponse) mEventHistoryArrayAdapter.
														getItem(position)).getDeviceRegistrationID());
											}
										}
									})
									.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											// do nothing
										}
									})
									.show();

						}
					}

				});
		mListView.setOnTouchListener(touchListener);
		mProgressBar = (ProgressBar) view.findViewById(R.id.notification_progressBar);
		Log.d(TAG,"View initialized. Fetching all events");
		return view;
	}

	private void loadAdapter(DeviceEventDetail eventDetailList) {
		Log.d(TAG,"Inside load adapter");
		mIntermediateTextView.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		if (eventDetailList != null) {
			Log.d(TAG,"Inside load adapter eventDetailList not null");
			if (mPageNumber >= eventDetailList.getTotalPages()) {
				isEndReached = true;
				hideFooterView();
			}

			if(eventDetailList.getTotalEvents() != 0) {
				Log.d(TAG,"Inside load adapter eventDetailList not zero");
				mNoNotificationLayout.setVisibility(View.GONE);
				if (mEventHistoryArrayAdapter == null) {
					mEventHistoryArrayAdapter = new EventHistoryArrayAdapter(mContext,
							CommonConstants.CONFIG_NOTIFICATION, R.layout.notifcation_item, this, mAllDevices);
				}
				mEventHistoryArrayAdapter.setEventList(new ArrayList<EventResponse>(Arrays.asList(eventDetailList.getEventResponse())), mPageNumber);
				if (mListView != null) {
					mEventHistoryArrayAdapter.notifyDataSetChanged();
				}
			}
			if(mEventHistoryArrayAdapter.getCount() == 0){
				hideFooterView();
				Log.d(TAG,"Inside load adapter adapter size zero");
				showNoDataLayout(getString(R.string.no_notification_detail));
			}
		} else if (mEventHistoryArrayAdapter.getCount() == 0) {
			hideFooterView();
			Log.d(TAG,"Inside load adapter count zero");
			showNoDataLayout(getString(R.string.no_notification_detail));
		}
	}

	private void fetchAllEventList(int pageNum) {
		Log.d(TAG,"FetchAllEventList of Event tab fetch in progress "+mIsNotificationFetchInProgress);
		if (CommonUtil.isInternetAvailable(mContext)) {
			if (!mIsNotificationFetchInProgress && mDeviceManagerService != null
					&& mAllDeviceEvent != null) {
				mIsNotificationFetchInProgress = true;
				mIntermediateTextView.setVisibility(View.GONE);
				mAllDeviceEvent.setPage(pageNum);
				if((mEventHistoryArrayAdapter == null || mEventHistoryArrayAdapter.getCount() == 0) && mEventTimer == null) {
					Log.d(TAG, "mEventTimer initialized");
					mEventTimerCount = 0;
					mEventTimer =  new Timer();
					mEventTimer.scheduleAtFixedRate(new NotificationTask(), EventUtil.EVENT_INTERMEDIATE_TIMEOUT, EventUtil.EVENT_FETCH_TIMEOUT);
				}
				Log.d(TAG,"FetchAllEventList of Event tab fetch before getDeviceEvent");
				mDeviceManagerService.getAllDeviceEvent(mAllDeviceEvent, new Response.Listener<DeviceEventDetail>() {
					@Override
					public void onResponse(DeviceEventDetail eventDetailList) {
						Log.d(TAG,"FetchAllEventList of Event tab onResponse");
						resetEventTimer();
						if(mContext != null && isAdded()) {
							Log.d(TAG,"FetchAllEventList of Event tab onResponse load data");
							mSwipeRefreshLayout.setEnabled(true);
							if (eventDetailList != null) {
								Log.d(TAG,"FetchAllEventList of Event tab onResponse eventDetailList total event: "
										+eventDetailList.getTotalEvents()+" total page: "+eventDetailList.getTotalPages());
								mTotaPageCount = eventDetailList.getTotalPages();
								if (isRefresh) {
									//if (mTotalCount != eventDetailList.getTotalEvents()) {
									showFooterView();
									mPageNumber = 0;
									isEndReached = false;
									mTotalCount = eventDetailList.getTotalEvents();
									loadAdapter(eventDetailList);
									//}
								} else {
									loadAdapter(eventDetailList);
								}
								eventLoadTime = System.currentTimeMillis() - eventLoadStartTime;
								int time = (int) eventLoadTime / 1000;
								String eventListLoadTime;
								Log.d("LoginTime", "LoginTime : " + eventLoadTime + " Sec = " + time);
								if (time <= 1) {
									eventListLoadTime = " 1 sec";
								} else if (time > 1 && time <= 3) {
									eventListLoadTime = " 3 sec";
								} else if (time > 3 && time <= 5) {
									eventListLoadTime = " 5 sec";
								} else if (time > 5 && time <= 10) {
									eventListLoadTime = " 10 sec";
								} else {
									eventListLoadTime = ">10 sec";
								}
								if (!isEventLoadTime) {
									Log.d(TAG, "notificationFragment" + eventListLoadTime);
									isEventLoadTime = true;
									GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_HISTORY, AppEvents.EH_LOAD_TIME, AppEvents.EH_LOAD_TIME);
									ZaiusEvent eventHistoryLoadTimeEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
									eventHistoryLoadTimeEvt.action(AppEvents.EH_LOAD_TIME);
									try {
										ZaiusEventManager.getInstance().trackCustomEvent(eventHistoryLoadTimeEvt);
									} catch (ZaiusException e) {
										e.printStackTrace();
									}

									GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_HISTORY, AppEvents.EH_LOAD_SUCCESSFUL, AppEvents.EH_LOAD_SUCCESSFUL);
									ZaiusEvent eventHistoryLoadSuccessEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
									eventHistoryLoadSuccessEvt.action(AppEvents.EH_LOAD_SUCCESSFUL);
									try {
										ZaiusEventManager.getInstance().trackCustomEvent(eventHistoryLoadSuccessEvt);
									} catch (ZaiusException e) {
										e.printStackTrace();
									}
								}
							}
							mSwipeRefreshLayout.setRefreshing(false);
						}
						mIsNotificationFetchInProgress = false;
						mLoadingMoreItems = false;
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d(TAG,"FetchAllEventList of Event tab onErrorResponse load error screen mContext "+mContext + "isAdded "+isAdded());
						resetEventTimer();
						if(mContext != null && isAdded()) {
							if(error != null) {
								Log.d(TAG, "Error is " + error.toString());
								if (error.networkResponse != null) {
									Log.d(TAG, error.networkResponse.toString());
									Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
								}
							}

							mSwipeRefreshLayout.setEnabled(true);
							mSwipeRefreshLayout.setRefreshing(false);
							if (!isRefresh) {
								mPageNumber--;
							}
							hideFooterView();
							if (mEventHistoryArrayAdapter.getCount() == 0) {
								Log.d(TAG, "Error screen load fetch failure ");
								showNoDataLayout(getString(R.string.notification_fetch_failure));
							}
						}

						mIsNotificationFetchInProgress = false;
						mLoadingMoreItems = false;
					}
				});
			}
		} else {
			mSwipeRefreshLayout.setRefreshing(false);
			if (mEventHistoryArrayAdapter.getCount() == 0) {
				hideFooterView();
				showNoDataLayout(getString(R.string.dialog_no_network_enabled));
			}
		}
	}

	private void showNoDataLayout(String msg) {
		if(mContext != null && isAdded()) {
			mNoNotificationLayout.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.INVISIBLE);
			mIntermediateTextView.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.INVISIBLE);
			mNoNotificationText.setText(msg);

			GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_HISTORY,AppEvents.EH_LOAD_FAILURE,AppEvents.EH_LOAD_FAILURE);
			ZaiusEvent eventHistoryLoadfailureEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
			eventHistoryLoadfailureEvt.action(AppEvents.EH_LOAD_FAILURE);
			try {
				ZaiusEventManager.getInstance().trackCustomEvent(eventHistoryLoadfailureEvt);
			} catch (ZaiusException e) {
				e.printStackTrace();
			}

		}
	}

	public void newEventArrived(){
		if(mListView.getFirstVisiblePosition() == 0){
			mIsPendingNewEvent = false;
			Log.d(TAG,"New events arrived. Fetching all events");
			refreshEventList();
		} else {
			mIsPendingNewEvent = true;
		}
	}
	public void refreshEventList() {
		if (!mIsNotificationFetchInProgress) {
			mIsPendingNewEvent = false;
			isRefresh = true;
			mSwipeRefreshLayout.setRefreshing(true);
			mListView.setVisibility(View.VISIBLE);
			mNoNotificationLayout.setVisibility(View.GONE);
			Log.d(TAG,"Refreshing and fetching all events");
			fetchAllEventList(0);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG,"On Resume of Event Tab");
		IntentFilter intentFilter = new IntentFilter(NotificationReceiver.REFRESH_EVENTS_BROADCAST);
		Log.i(TAG, "Register broadcast REFRESH_EVENTS_BROADCAST");
		mContext = getContext();
		if (mContext != null) {
			mContext.registerReceiver(mRefreshEventsBroadcastReceiver, intentFilter);
		}
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setCancelable(false);
		isRefresh = false;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG,"On Start of Event Tab and load all events");
		fetchAllEventList(0);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG,"On Pause of Event Tab");
		if (mRefreshEventsBroadcastReceiver != null) {
			try {
				if (mContext != null) {
					mContext.unregisterReceiver(mRefreshEventsBroadcastReceiver);
				}
			} catch (IllegalArgumentException ignored) {
			}
		}
		if(mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		if(mDeviceWakeup != null) {
			mDeviceWakeup.cancelTask(mShareDevice.getProfile().registrationId,mDeviceHandler);
		}

		dismissProgressDialog();
		//Reinitialize values
		mLoadingMoreItems = false;
		mIsNotificationDeleteInProgress = false;
		mIsNotificationFetchInProgress = false;
		Log.d(TAG,"mEventTimer canceled in onpause");
		resetEventTimer();
	}

	@Override
	public void onLoadFullScreenImage(EventImage eventImage, EventResponse event) {
		EventImage eventImageLoad = eventImage;
		mLoadEvent = event;
		Intent fullScreenIntent = new Intent(mContext, EventHistoryFullScreenActivity.class);
		fullScreenIntent.putExtra(CommonConstants.EVENT_TYPE, CommonConstants.EVENT_IMAGE);
		fullScreenIntent.putExtra(CommonConstants.EVENT_IMAGE_TAG, eventImageLoad);
		startActivityForResult(fullScreenIntent, FULL_SCREEN_REQUEST_CODE);
	}


	@Override
	public void onPlayVideo(EventVideo eventVideo, final EventResponse event) {
		mLoadEvent = event;
		Intent fullScreenIntent = new Intent(mContext, EventHistoryFullScreenActivity.class);
		fullScreenIntent.putExtra(CommonConstants.EVENT_TYPE, CommonConstants.EVENT_VIDEO);
		fullScreenIntent.putExtra(CommonConstants.EVENT_VIDEO_TAG,eventVideo);
		startActivityForResult(fullScreenIntent,FULL_SCREEN_REQUEST_CODE);

	}

	@Override
	public void downloadAndShareEvent(String imageUrl, int eventShareType, int actionType, Device selectedDevice) {
		if(selectedDevice != null && mEventUtil != null) {
			mEventUtil.downloadAndShareEventUtil(imageUrl, eventShareType, actionType, selectedDevice);
		}
	}


	@Override
	public void shareVideoEvent(EventVideo eventVideo, Device selectedDevice) {
		mShareDevice = selectedDevice;
		mEventVideo = eventVideo;
		if (mShareDevice != null) {
			showProgressDialog(getString(R.string.please_wait));
			if (mShareDevice.getProfile().isStandBySupported() &&
					mEventVideo.getStorageMode() == 1) {

				File clipFile = null;
				if(eventVideo.getEventTime() != null) {
					clipFile = FileService.getFormatedFilePathForVideo(selectedDevice.getProfile().getName(), eventVideo.getEventTime().getTime());
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
				else {
					checkDeviceStatus();
				}
			} else if(mEventUtil != null) {
				mEventUtil.shareVideoUtil(mEventVideo, mShareDevice, CommonConstants.ACTION_TYPE_SHARE);
			} else {
				dismissProgressDialog();
			}
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FULL_SCREEN_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				if(mLoadEvent != null) {
					mProgressBar.setVisibility(View.VISIBLE);
					mDeleteIndex.clear();
					mDeleteIndex.add(mLoadEvent);
					deleteEventForCamera(String.valueOf(mLoadEvent.getID()), mLoadEvent.getDeviceRegistrationID());
				}

			}
		}else if (requestCode == PublicDefine.RESULT_SHARE_SNAPSHOT) {
			if (resultCode == Activity.RESULT_OK) {

			}
		}

	}

	private void showFooterView(){
		mListView.setVisibility(View.VISIBLE);
		mNoNotificationLayout.setVisibility(View.GONE);
		mFooterProgressView.setVisibility(View.VISIBLE);
		if(mProgressAnim != null) {
			mFooterProgressView.startAnimation(mProgressAnim);
		}
	}



	private void hideFooterView(){
		if(mFooterProgressView != null) {
			mFooterProgressView.clearAnimation();
			mFooterProgressView.setVisibility(View.INVISIBLE);
		}
	}

	private synchronized void deleteEventForCamera(String eventCode, String registrationId) {
		if (!mIsNotificationDeleteInProgress && mDeviceManagerService != null) {
			mIsNotificationDeleteInProgress = true;
			String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
			mDeleteEvent = new DeleteEvent(accessToken, registrationId);
			mDeleteEvent.setEventCode(eventCode);
			mDeleteEvent.setDeleteChildEvents(false);
			mDeviceManagerService.deleteDeviceEvent(mDeleteEvent, new Response.Listener<DeleteEventDetails>() {
				@Override
				public void onResponse(DeleteEventDetails response) {
					if (mContext != null && isAdded()) {
						if (response != null) {
							int position = mListView.getFirstVisiblePosition();
							if (mDeleteIndex.size() == 1) {
								mEventHistoryArrayAdapter.remove(mDeleteIndex.get(0));
								mListView.setAdapter(mEventHistoryArrayAdapter);
								mListView.setSelection(position);
							} else if (mDeleteIndex.size() > 1) {
								for (EventResponse event : mDeleteIndex) {
									mEventHistoryArrayAdapter.remove(event);
								}
							}
							mListView.setAdapter(mEventHistoryArrayAdapter);
							mListView.setSelection(position);
						}
						mProgressBar.setVisibility(View.GONE);
						Toast.makeText(mContext, mContext.getString(R.string.deletion_successful), Toast.LENGTH_LONG).show();
					}
					mIsNotificationDeleteInProgress = false;
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					if (mContext != null && isAdded()) {
					    mProgressBar.setVisibility(View.GONE);
						Toast.makeText(mContext, mContext.getString(R.string.deletion_failure), Toast.LENGTH_LONG).show();
					}
					mIsNotificationDeleteInProgress = false;
				}
			});
		}
	}

	private void  checkDeviceStatus()
	{
		if(mContext != null) {
			String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");

			final DeviceStatus deviceStatus = new DeviceStatus(accessToken, mShareDevice.getProfile().getRegistrationId());

			DeviceManagerService.getInstance(mContext).getDeviceStatus(deviceStatus, new Response.Listener<StatusDetails>() {
						@Override
						public void onResponse(StatusDetails response) {
							//dismissDialog();
							if (response != null) {
								StatusDetails.StatusResponse[] statusResponseList = response.getDeviceStatusResponse();

								StatusDetails.StatusResponse statusResponse = null;

								if (statusResponseList != null && statusResponseList.length > 0) {
									statusResponse = statusResponseList[0]; // fetch first object only
								}

								if (statusResponse != null) {
									StatusDetails.DeviceStatusResponse deviceStatusResponse = statusResponse.getDeviceStatusResponse();
									String deviceStatus = deviceStatusResponse.getDeviceStatus();

									Log.d(TAG, "device status :- " + deviceStatus);

									if (deviceStatus != null) {
										if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0) {
											mShareDevice.getProfile().setAvailable(true);
											mShareDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
											Log.d(TAG, "device online..start sharing video");
											if(mShareDevice != null && mEventUtil != null) {
												mEventUtil.shareVideoUtil(mEventVideo, mShareDevice, CommonConstants.ACTION_TYPE_SHARE);
											} else {
												dismissProgressDialog();
											}
										} else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_STANDBY) == 0) {
											mShareDevice.getProfile().setAvailable(false);
											mShareDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);
											Log.d(TAG, "device standby..wakeup");
											//wakeup device
											wakeUpRemoteDevice();
										} else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_OFFLINE) == 0) {
											Log.d(TAG, "setting device available false");
											mShareDevice.getProfile().setAvailable(false);
											mShareDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
											//device offline
											if (mContext != null && isAdded()) {
												dismissProgressDialog();
												Toast.makeText(mContext, getString(R.string.camera_offline), Toast.LENGTH_SHORT).show();
											}
										} else {
											if (mContext != null && isAdded()) {
												dismissProgressDialog();
												Toast.makeText(mContext, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_SHORT).show();
											}
										}
									} else {
										if (mContext != null && isAdded()) {
											dismissProgressDialog();
											Toast.makeText(mContext, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_SHORT).show();
										}
									}
								} else {
									if (mContext != null && isAdded()) {
										dismissProgressDialog();
										Toast.makeText(mContext, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_SHORT).show();
									}
								}
							} else {
								if (mContext != null && isAdded()) {
									dismissProgressDialog();
									Toast.makeText(mContext, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_SHORT).show();
								}
							}
						}
					},
					new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							if (mContext != null && isAdded()) {
								dismissProgressDialog();
								if (error != null && error.networkResponse != null) {
									Log.d(TAG, error.networkResponse.toString());
									Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
								}
							}
						}
					});
		}

	}
	private void wakeUpRemoteDevice()
	{
		if(mContext != null) {
			showProgressDialog(getString(R.string.viewfinder_progress_wakeup));
			Log.d(TAG, "wakeUpRemoteDevice");
			String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
			mDeviceWakeup = DeviceWakeup.newInstance();
			mDeviceWakeup.wakeupDevice(mShareDevice.getProfile().registrationId, accessToken, mDeviceHandler,mShareDevice);

		}
	}

	private Handler mDeviceHandler = new Handler()
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
						if(mShareDevice != null && mEventUtil != null) {
							mEventUtil.shareVideoUtil(mEventVideo, mShareDevice, CommonConstants.ACTION_TYPE_SHARE);
						} else if (mContext != null && isAdded()) {
							dismissProgressDialog();
						}
					}
					else
					{
						Log.d(TAG, "wakeup device:failure");
						if (mContext != null && isAdded()) {
							dismissProgressDialog();
							Toast.makeText(mContext, getResources().getString(R.string.failed_to_start_device), Toast.LENGTH_LONG).show();
						}
					}

					break;
				default:
					if (mContext != null && isAdded()) {
						dismissProgressDialog();
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

	private class NotificationTask extends TimerTask {
		@Override
		public void run() {
			if(mEventTimerCount == 0){
				mEventTimerCount++;
				Log.d(TAG,"mEventTimer intermediate message received");
				if((mEventHistoryArrayAdapter == null || mEventHistoryArrayAdapter.getCount() == 0)
						&& (mContext != null && getActivity() != null &&  isAdded())){
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.d(TAG,"mEventTimer  show intermediate message");
							mNoNotificationLayout.setVisibility(View.GONE);
							mIntermediateTextView.setVisibility(View.VISIBLE);
							mIntermediateTextView.setText(mContext.getString(R.string.event_fetch_progress));
						}
					});
				}

			}else if(mEventTimerCount == 1){
				mEventTimerCount = 0;
				Log.d(TAG,"mEventTimer onFinish");
				resetEventTimer();
				if((mEventHistoryArrayAdapter == null || mEventHistoryArrayAdapter.getCount() == 0)
						&& (mContext != null && isAdded())){
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.d(TAG,"mEventTimer onFinish show failure message");
							//mSwipeRefreshLayout.setEnabled(true);
							mSwipeRefreshLayout.setRefreshing(false);
							hideFooterView();
							showNoDataLayout(getString(R.string.notification_fetch_failure));
						}
					});
				}
				mIsNotificationFetchInProgress = false;
				mLoadingMoreItems = false;
			}
		}
	}

	private void resetEventTimer(){
		if(mEventTimer != null){
			Log.d(TAG,"mEventTimer is cancelled");
			mEventTimer.cancel();
			mEventTimer = null;
			mEventTimerCount = 0;
		}
	}
}
