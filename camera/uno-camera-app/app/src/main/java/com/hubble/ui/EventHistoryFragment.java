package com.hubble.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.blinkhd.SwipeDismissListViewTouchListener;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.dialog.AskForFreeTrialDialog;
import com.hubble.dialog.FreeTrialFailureDialog;
import com.hubble.dialog.FreeTrialSuccessDialog;
import com.hubble.framework.networkinterface.v1.pojo.HubbleRequest;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeleteEvent;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceEvent;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceID;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceStatus;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeleteEventDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceEventDetail;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceFreeTrialDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.EventResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.cloudclient.user.pojo.response.UserSubscriptionPlanResponse;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.framework.service.subscription.SubscriptionInfo;
import com.hubble.framework.service.subscription.SubscriptionService;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.subscription.ManagePlanActivity;
import com.hubble.subscription.OfferExecutor;
import com.hubble.subscription.PlanFragment;
import com.hubble.subscription.SubscriptionCommandUtil;
import com.hubble.ui.eventsummary.EventSummaryConstant;
import com.hubble.ui.eventsummary.VideoAnalyticsOfferDialog;
import com.hubble.util.CommonConstants;
import com.hubble.util.EventUtil;
import com.hubble.util.SubscriptionUtil;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.recurly.android.model.Plan;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.DeviceWakeup;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.devices.SerializableDeviceProfile;
import retrofit.RetrofitError;
import retrofit.http.HEAD;
import com.hubbleconnected.camera.R;

public class EventHistoryFragment extends Fragment implements IEventHistoryActionCallBack {

	private final String TAG = "EventHistoryFragment";
	private final String USER_AGREE_FOR_REMOTE_ACCESS_SD_CARD = "user_agree_for_remote_access_sd_card";
	private final int FULL_SCREEN_REQUEST_CODE = 1;

	private ImageView mEventButton;
	private TextView mEventHistoryText;
	private ImageView mPromoSubcriptionButton;
	private ImageView mSummaryButton;
	private TextView mEventHistoryDate;
	private ImageView mCalenderButton;
	private SwipeRefreshLayout mSwipeRefreshView;
	private ListView mListView;
	private TextView mNoEventView;
	private ProgressBar mProgressBar;
	private SwipeDismissListViewTouchListener touchListener = null;
	private View footerView = null;
	private ImageView mFooterProgressView;
	private Button mBackToToday;
	private ImageView mBackToTop;
	private ImageView mDeleteAll;

	private boolean mLoadingMoreItems = false;
	private boolean mIsEventFetchInProgress = false;
	private boolean mIsEventDeleteInProgress = false;
	private boolean mIsEndReached = false;
	private boolean mIsCurrentDay = true;

	private int mPageNumber = 0;
	private int mTotalCount = 0;

	private Reason mFetchReason;

	private String mStartDay;
	private String mEndDay;

	private Device mSelectedDevice = null;
	private DeviceManagerService mDeviceManagerService;
	private DeleteEvent mDeleteEvent;

	private DatePickerFragment mDatePickerFragment = null;

	private ArrayList<EventResponse> mDeleteIndex = new ArrayList<EventResponse>();

	private IViewFinderEventListCallBack mIViewFinderEventListCallBack = null;
	private EventHistoryArrayAdapter mEventHistoryArrayAdapter = null;

	private SimpleDateFormat df1 = new SimpleDateFormat("dd MMM, EEEE yyyy");
	private SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


	private Animation mProgressAnim;

	private Context mContext = null;

	private EventResponse mLoadEvent = null;

	public enum Reason {
		REFRESH, NEWDATE, ALLDATE, SCROLL,
	}

	private int mTotaPageCount = 0;

	private ProgressDialog mProgressDialog = null;
	private DeviceWakeup mDeviceWakeup = null;

	private boolean mIsViewAvailable = false;

	private EventUtil mEventUtil;
	private EventVideo mEventVideo;

	private String mAccessToken;

	private AskForFreeTrialDialog mAskForFreeTrialDialog;
	private FreeTrialSuccessDialog mFreeTrialSuccessDialog;
	private FreeTrialFailureDialog mFreeTrialFailureDialog;
	private Dialog mEnableProgressDialog;

	private VideoAnalyticsOfferDialog mVideoAnalyticsOfferDialog;
	private OfferExecutor mOfferExecutor;

	private boolean mIsSummaryOptIn = false;
	private double mDeviceTimeZone;
	private boolean mIsPendingNewEvent = false;


	private Timer mEventTimer = null;
	private int mEventTimerCount = 0;

	private String mRegistrationID;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"On create of Event History");
		setRetainInstance(true);
		mContext = getContext();

		df3.setTimeZone(TimeZone.getTimeZone("UTC"));
		mProgressAnim = AnimationUtils.loadAnimation(mContext, R.anim.image_rotate);
		if (getActivity() != null) {
			mEventUtil = new EventUtil(getActivity());
			mEventUtil.setCallBack(new EventUtil.IEventUtilCallBack() {
				@Override
				public void onDismissDialog() {
					dismissProgressDialog();
				}
			});
		}
		mAccessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
		mOfferExecutor = new OfferExecutor(mContext);
	}

	private synchronized void fetchEventForCamera(int pageNum, Reason fetchReason) {
		Log.d(TAG, "FetchEventForCamera of Event history fetch in progress " + mIsEventFetchInProgress);
		if (!mIsEventFetchInProgress && mDeviceManagerService != null) {
			mCalenderButton.setEnabled(false);
			mFetchReason = fetchReason;
			mIsEventFetchInProgress = true;
			mNoEventView.setVisibility(View.INVISIBLE);
			if (mFetchReason == Reason.REFRESH) {
				mSwipeRefreshView.setRefreshing(true);
				if (mEventHistoryArrayAdapter == null || mEventHistoryArrayAdapter.getCount() == 0) {
					hideFooterView();
				}
			}
			//Initialize device event
			Log.d(TAG, "Start day time is " + mStartDay + "End day time is" + mEndDay);
			DeviceEvent deviceEvent = new DeviceEvent(HubbleApplication.
					AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, ""),
					mSelectedDevice.getProfile().registrationId);
			deviceEvent.setSize(CommonConstants.EVENT_PAGE_SIZE);
			deviceEvent.setPage(pageNum);
			if (!mStartDay.isEmpty()) {
				deviceEvent.setAfterStartTime(mStartDay);
				if (!mEndDay.isEmpty()) {
					deviceEvent.setBeforeStartTime(mEndDay);
				}
			}
			if ((mEventHistoryArrayAdapter == null || mEventHistoryArrayAdapter.getCount() == 0) && mEventTimer == null) {
				Log.d(TAG, "mEventTimer initialized");
				mEventTimerCount = 0;
				mEventTimer = new Timer();
				mEventTimer.scheduleAtFixedRate(new EventTask(), EventUtil.EVENT_INTERMEDIATE_TIMEOUT, EventUtil.EVENT_FETCH_TIMEOUT);

			}
			Log.d(TAG, "FetchEventForCamera of Event history before getDeviceEvent");
			mDeviceManagerService.getDeviceEvent(deviceEvent, new Response.Listener<DeviceEventDetail>() {
				@Override
				public void onResponse(DeviceEventDetail eventDetailList) {
					Log.d(TAG, "FetchEventForCamera of Event history onResponse");
					resetEventTimer();
					if (mContext != null && isAdded()) {
						mCalenderButton.setEnabled(true);
						setSwipeRefreshView();
						if (eventDetailList != null) {
							Log.d(TAG, "FetchEventForCamera of Event history onResponse load data total event: "
									+ eventDetailList.getTotalEvents() + " total page: " + eventDetailList.getTotalPages());
							mTotaPageCount = eventDetailList.getTotalPages();
							if (mFetchReason == Reason.ALLDATE || mFetchReason == Reason.REFRESH ||
									(mFetchReason == Reason.NEWDATE && mIsCurrentDay)) {
								mBackToToday.setVisibility(View.INVISIBLE);
								if (eventDetailList.getEventResponse() != null &&
										eventDetailList.getEventResponse().length > 0 && mSelectedDevice != null) {
									String lastSeenTime = df3.format(eventDetailList.
											getEventResponse()[0].getEventTime(mSelectedDevice.getProfile().getTimeZone()));
									CommonUtil.setEventReadTimeToSP(mContext, mSelectedDevice.getProfile().
											getRegistrationId(), lastSeenTime);
								}
							}
							switch (mFetchReason) {
								case REFRESH:
								case ALLDATE:
								case NEWDATE:
									mTotalCount = eventDetailList.getTotalEvents();
									mPageNumber = 0;
									mIsEndReached = false;
									break;
							}
							showFooterView();
							loadAdapter(eventDetailList);
							mSwipeRefreshView.setRefreshing(false);
						} else if (mEventHistoryArrayAdapter.getCount() == 0) {
							showNoEventView(mContext.getString(R.string.no_events_found));
						}
					}
					mIsEventFetchInProgress = false;
					mLoadingMoreItems = false;
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Log.d(TAG, "FetchEventForCamera of Event history onErrorResponse mContext " + mContext + "isAdded" + isAdded());
					resetEventTimer();
					if (mContext != null && isAdded()) {
						mCalenderButton.setEnabled(true);
						if (error != null) {
							Log.d(TAG, "Error is " + error.toString());
							if (error.networkResponse != null) {
								Log.d(TAG, error.networkResponse.toString());
								Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
							}
						}
						setSwipeRefreshView();
						mSwipeRefreshView.setRefreshing(false);
						hideFooterView();
						if (mFetchReason == Reason.SCROLL) {
							mPageNumber--;
						}
						Log.d(TAG, "FetchEventForCamera of Event history onErrorResponse load error screen");
						if (mEventHistoryArrayAdapter == null || mEventHistoryArrayAdapter.getCount() == 0) {
							showNoEventView(mContext.getString(R.string.event_fetch_failure));
						}
					}
					mIsEventFetchInProgress = false;
					mLoadingMoreItems = false;
				}
			});
		}
	}


	private synchronized void deleteEventForCamera(final String eventCode) {
		if (CommonUtil.isInternetAvailable(mContext)) {
			if (!mIsEventDeleteInProgress && mDeviceManagerService != null && mDeleteEvent != null) {
				mIsEventDeleteInProgress = true;
				if (!TextUtils.isEmpty(eventCode)) {
					mDeleteEvent.setEventCode(eventCode);
				}
				mDeleteEvent.setDeleteChildEvents(true);
				mDeviceManagerService.deleteDeviceEvent(mDeleteEvent, new Response.Listener<DeleteEventDetails>() {
					@Override
					public void onResponse(DeleteEventDetails response) {
						Log.d(TAG, "Delete event response is received");
						if (mContext != null && isAdded()) {
							if (response != null) {
								if (TextUtils.isEmpty(eventCode)) {
									Log.d(TAG, "All events deleted");
									if (mEventHistoryArrayAdapter != null) {
										mEventHistoryArrayAdapter.clearEvents();
										mEventHistoryArrayAdapter.notifyDataSetChanged();
										showNoEventView(mContext.getString(R.string.no_events_found));
									}
								} else {
									Log.d(TAG, "Event deleted");
									int position = mListView.getFirstVisiblePosition();
									if (mDeleteIndex.size() == 1) {
										mEventHistoryArrayAdapter.remove(mDeleteIndex.get(0));
										mListView.setAdapter(mEventHistoryArrayAdapter);
										mListView.setSelection(position);
									} else if (mDeleteIndex.size() > 1) {
										for (EventResponse event : mDeleteIndex) {
											mEventHistoryArrayAdapter.remove(event);
										}
										mListView.setAdapter(mEventHistoryArrayAdapter);
										mListView.setSelection(position);
									}
								}
							}
							mProgressBar.setVisibility(View.GONE);
							Toast.makeText(mContext, mContext.getString(R.string.deletion_successful), Toast.LENGTH_LONG).show();
						}
						mIsEventDeleteInProgress = false;
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						if (mContext != null && isAdded()) {
							mProgressBar.setVisibility(View.GONE);
							Toast.makeText(mContext, mContext.getString(R.string.deletion_failure), Toast.LENGTH_LONG).show();
						}
						mIsEventDeleteInProgress = false;
					}
				});
			}
		} else {
			Toast.makeText(mContext, mContext.getString(R.string.dialog_no_network_enabled), Toast.LENGTH_LONG).show();
		}
	}

	public void newEventArrived(){
		Log.d(TAG," New event arrived");
        if(mListView.getFirstVisiblePosition() == 0){
	        mIsPendingNewEvent = false;
	        refreshEventList();
        } else {
            mIsPendingNewEvent = true;
        }
	}

	public void refreshEventList() {
		if (mIsCurrentDay) {
			mIsPendingNewEvent = false;
			Log.d(TAG,"Refreshing and fetching all events");
			fetchEventForCamera(0, Reason.REFRESH);
		}
	}

	private void initDeviceManager() {
		//Device Event Instance Setup
		if (mSelectedDevice != null && mContext != null) {
			if (mDeviceManagerService == null) {
				mDeviceManagerService = DeviceManagerService.getInstance(mContext);
			}
			mDeleteEvent = new DeleteEvent(mAccessToken, mSelectedDevice.getProfile().getRegistrationId());


		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mContext = getContext();
		Log.d(TAG,"onCreateView of EventHistoryFragment");
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		View view = inflater.inflate(R.layout.fragment_event_history, null);
		footerView = inflater.inflate(R.layout.loading_view, null, false);
		initDeviceManager();
		mFooterProgressView = (ImageView) footerView.findViewById(R.id.footer_progress);
		//Initialization of variables with default value
		mLoadingMoreItems = false;
		mIsEventFetchInProgress = false;
		mIsEventDeleteInProgress = false;
		mIsCurrentDay = true;
		mIsEndReached = false;

		mPageNumber = 0;
		mTotalCount = 0;

		//Calnder to pick events for single days
		mDatePickerFragment = new DatePickerFragment();
		mDatePickerFragment.setListener(new DatePickerFragment.DatePickerFragmentInterface() {
			@Override
			public void onDateChosen(int year, int month, int day) {
				if(mSelectedDevice != null && mSelectedDevice.getProfile() != null) {
					mBackToToday.setVisibility(View.VISIBLE);
					String timezoneString = CommonUtil.getCameraTimeZone(mSelectedDevice.getProfile().getTimeZone());
					TimeZone timeZone = TimeZone.getTimeZone(timezoneString);
					Calendar calendar1 = Calendar.getInstance(timeZone);
					calendar1.set(year, month, day, 0, 0, 0);
					Date selectedDate = calendar1.getTime();
					mStartDay = df3.format(selectedDate);

					calendar1.add(Calendar.DAY_OF_YEAR, 1);
					Date tomorrow = calendar1.getTime();
					mEndDay = df3.format(tomorrow);

					SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
					String formatedDateToCompare = sdf.format(selectedDate);
					if (isCurrentDay(formatedDateToCompare)) {
						mIsCurrentDay = true;
					} else {
						mIsCurrentDay = false;
					}
					mSwipeRefreshView.setRefreshing(false);
					mPageNumber = 0;

					mEventHistoryDate.setText(new SimpleDateFormat("dd MMM, EEEE yyyy").format(selectedDate));
					clearAdapter();
					mEventHistoryArrayAdapter.notifyDataSetChanged();
					showFooterView();
					Log.d(TAG, "Calendar date changed. Loading all events for the date");
					fetchEventForCamera(0, Reason.NEWDATE);

					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN, AppEvents.VF_DAILY_EVENTS_CLICKED, AppEvents.DAILY_EVENTS);
					ZaiusEvent dailyEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
					dailyEvt.action(AppEvents.VF_DAILY_EVENTS_CLICKED);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(dailyEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}
				}

			}
		});

		ImageView backButton = (ImageView) view.findViewById(R.id.vf_toolbar_back);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getActivity() != null) {
					getActivity().onBackPressed();
				}
			}
		});

		ImageView settingsButton = (ImageView) view.findViewById(R.id.vf_toolbar_settings);
		//Make settings button invisible if privacy mode is on.
		if(mSelectedDevice != null) {
			String mPrivacyMode = mSelectedDevice.getProfile().getDeviceAttributes().getPrivacyMode();
			if ((mPrivacyMode == null) || (!mPrivacyMode.isEmpty() && mPrivacyMode.equalsIgnoreCase("0"))) {
				settingsButton.setVisibility(View.VISIBLE);
			} else {
				settingsButton.setVisibility(View.INVISIBLE);
			}
		}
		settingsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissFreeTrialDialog();
				mIViewFinderEventListCallBack.launchCameraSettings();
				GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_HISTORY,AppEvents.EH_SETTINGS_CLICKED,AppEvents.EVENT_HISTORY_SETTINGS);
				ZaiusEvent ehSettingsEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
				ehSettingsEvt.action(AppEvents.EH_SETTINGS_CLICKED);
				try {
					ZaiusEventManager.getInstance().trackCustomEvent(ehSettingsEvt);
				} catch (ZaiusException e) {
					e.printStackTrace();
				}

			}
		});

		mDeleteAll = (ImageView) view.findViewById(R.id.event_delete_all);
		mDeleteAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG," Event delete all is called");
				showDeleteAllConfirmationDialog();
			}
		});

		view.findViewById(R.id.camera_spinner).setVisibility(View.GONE);
		TextView actionBarTitle = (TextView) view.findViewById(R.id.vf_toolbar_title);
		actionBarTitle.setVisibility(View.VISIBLE);
		if (mSelectedDevice != null) {
			actionBarTitle.setText(mSelectedDevice.getProfile().getName());
		}

		mEventButton = (ImageView) view.findViewById(R.id.event_Load_button);
		mEventButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				launchViewFinder();
			}
		});

		mEventHistoryText = (TextView) view.findViewById(R.id.event_load_text);
		mEventHistoryText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				launchViewFinder();
			}
		});

		mPromoSubcriptionButton = (ImageView) view.findViewById(R.id.promot_subscription_button);
		mPromoSubcriptionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showFreeTrialDialog();
			}
		});

		mSummaryButton = (ImageView)view.findViewById(R.id.event_summary_button);
		mSummaryButton.setVisibility(View.GONE);
		mSummaryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsSummaryOptIn) {
					if (mIViewFinderEventListCallBack != null) {
						mIViewFinderEventListCallBack.onClick(CommonConstants.LAUNCH_SUMMARY_FROM_EH);
					}
				} else {
					dismissVideoAnalyticsDialog();
					mVideoAnalyticsOfferDialog = new VideoAnalyticsOfferDialog(mContext, new VideoAnalyticsOfferDialog.IVAOfferListener() {
						@Override
						public void vaOfferOptIn() {
							dismissVideoAnalyticsDialog();
							mEnableProgressDialog = ProgressDialog.show(mContext, null, getString(R.string.va_opt_in_progress));
							mOfferExecutor.consumeUserOffer(new OfferExecutor.IOfferConsumeResponse() {
								@Override
								public void onOfferConsumeResponse(boolean consumeSuccess) {
									if (mContext != null && isAdded()) {
										dismissEnableProgressDialog();
										mIsSummaryOptIn = consumeSuccess;
										if (mIsSummaryOptIn) {
											dismissVideoAnalyticsDialog();
											mVideoAnalyticsOfferDialog = new VideoAnalyticsOfferDialog(mContext, EventSummaryConstant.VA_HINT_DIALOG);
											mVideoAnalyticsOfferDialog.show();
										}else {
											Toast.makeText(mContext, getString(R.string.va_opt_in_failure), Toast.LENGTH_LONG).show();
										}
									}
								}
							});
						}
					}, EventSummaryConstant.VA_OFFER_DIALOG);
					mVideoAnalyticsOfferDialog.show();
				}
			}
		});

		Calendar c = Calendar.getInstance();
		mEventHistoryDate = (TextView) view.findViewById(R.id.event_history_date);
		if(mSelectedDevice != null) {
			String dateString = CommonUtil.getTimeStampFromTimeZone(c.getTime(), mSelectedDevice.getProfile().getTimeZone(), df1);
			mEventHistoryDate.setText(dateString);
		}

		mCalenderButton = (ImageView) view.findViewById(R.id.calender_button);
		mCalenderButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mDatePickerFragment.getIsDialogOpen() && getActivity() != null) {
					mDatePickerFragment.setIsDialogOpen(true);
					mDatePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
					GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_HISTORY,AppEvents.EH_CALENDER_CLICKED,AppEvents.CALENDER_CLICKED);

					ZaiusEvent ehCalenderEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
					ehCalenderEvt.action(AppEvents.EH_CALENDER_CLICKED);
					try {
						ZaiusEventManager.getInstance().trackCustomEvent(ehCalenderEvt);
					} catch (ZaiusException e) {
						e.printStackTrace();
					}

				}
			}
		});

		mBackToToday = (Button) view.findViewById(R.id.back_to_today);
		mBackToToday.setVisibility(View.INVISIBLE);
		mBackToToday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG,"Load back to Today button clicked. Loading all events");
				fetchToday();
			}
		});

		mSwipeRefreshView = (SwipeRefreshLayout) view.findViewById(R.id.event_log_swipeview);
		mSwipeRefreshView.setEnabled(false);
		mSwipeRefreshView.setColorSchemeResources(R.color.app_refreshview_color_1,
				R.color.app_refreshview_color_2,
				R.color.app_refreshview_color_3,
				R.color.app_refreshview_color_4);
		mSwipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Log.d(TAG,"User is refreshing. Loading all events");
				refreshEventList();
			}
		});

		mNoEventView = (TextView) view.findViewById(R.id.event_log_noItemsHolder);

		mListView = (ListView) view.findViewById(R.id.event_log_listview);
		mListView.addFooterView(footerView);
		showFooterView();

		mBackToTop = (ImageView)view.findViewById(R.id.event_move_to_top);
		mBackToTop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBackToTop.setVisibility(View.GONE);
				mListView.smoothScrollToPosition(0);
			}
		});

		if (/*mEventHistoryArrayAdapter == null &&*/ mContext != null) {
			mEventHistoryArrayAdapter = new EventHistoryArrayAdapter(mContext,
					CommonConstants.CONFIG_EVENT_HISTORY, R.layout.event_history_item, this, mSelectedDevice);
		}
		mListView.setAdapter(mEventHistoryArrayAdapter);

		mListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if(mEventHistoryArrayAdapter.getCount() != 0 &&
						mEventHistoryArrayAdapter.getCount() <= mListView.getLastVisiblePosition() + 1){
					hideFooterView();
				}
			}
		});

		// Infinite scroll and on-demand loading
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					if (mEventHistoryArrayAdapter.getCount() > 0) {
						EventResponse eventResponse = (EventResponse) mEventHistoryArrayAdapter.getItem(mListView.getFirstVisiblePosition());
						if (eventResponse != null) {
                            if(mSelectedDevice != null) {
							     Date date = eventResponse.getEventTime(mSelectedDevice.getProfile().getTimeZone());
								 String dateString = CommonUtil.getTimeStampFromTimeZone(date, mSelectedDevice.getProfile().getTimeZone(), df1);
								 mEventHistoryDate.setText(dateString);
							}
						}
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				if(firstVisibleItem == 0){
					if(mIsPendingNewEvent){
						Log.d(TAG,"View scrolled to top. Loading all events");
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
				if ((threshold > 0 && lastInScreen >= threshold) && !(mLoadingMoreItems)
						&& !(mIsEndReached) && mEventHistoryArrayAdapter.getCount() > 0
						&& mPageNumber < mTotaPageCount) {
					mPageNumber++;
					showFooterView();
					Log.d(TAG,"Last page reached. Loading next page events");
					fetchEventForCamera(mPageNumber, Reason.SCROLL);
					mLoadingMoreItems = true;
				}
			}
		});

		//Delete event on swipe
		touchListener = new SwipeDismissListViewTouchListener(mListView,
				new SwipeDismissListViewTouchListener.DismissCallbacks() {
					@Override
					public boolean canDismiss(int position) {
						return true;
					}

					@Override
					public void onDismiss(ListView listView, int[] reverseSortedPositions) {
						final int[] sortedPosition = reverseSortedPositions;
						GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SINGLE_EVENT,AppEvents.EVENT_HISTORY_DELETE,AppEvents.EH_SWIPE_DELETE);

						ZaiusEvent eh_deleteEvt = new ZaiusEvent(AppEvents.SINGLE_EVENT);
						eh_deleteEvt.action(AppEvents.EVENT_HISTORY_DELETE);
						try {
							ZaiusEventManager.getInstance().trackCustomEvent(eh_deleteEvt);
						} catch (ZaiusException e) {
							e.printStackTrace();
						}

						if (mContext != null && isAdded()) {
							AlertDialog mAlertDialog = new AlertDialog.Builder(mContext)
									.setMessage(mContext.getString(R.string.deletion_confirmation_dialog))
									.setPositiveButton(mContext.getString(R.string.Delete), new DialogInterface.OnClickListener() {
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
												deleteEventForCamera(mId);
											}
											GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_HISTORY,AppEvents.SWIPE_TO_DELETE_OK,AppEvents.OK_CLICKED);

											ZaiusEvent deleteOkEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
											deleteOkEvt.action(AppEvents.SWIPE_TO_DELETE_OK);
											try {
												ZaiusEventManager.getInstance().trackCustomEvent(deleteOkEvt);
											} catch (ZaiusException e) {
												e.printStackTrace();
											}
										}
									})
									.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_HISTORY,AppEvents.SWIPE_TO_DELETE_CANCEL,AppEvents.CANCEL_CLICKED);
											// do nothing
											ZaiusEvent deleteCancelEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
											deleteCancelEvt.action(AppEvents.SWIPE_TO_DELETE_CANCEL);
											try {
												ZaiusEventManager.getInstance().trackCustomEvent(deleteCancelEvt);
											} catch (ZaiusException e) {
												e.printStackTrace();
											}

										}
									})
									.show();
							Button nbutton = mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
							nbutton.setTextColor(getResources().getColor(R.color.text_blue));
							nbutton.setAllCaps(true);
							Button pbutton = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
							pbutton.setTextColor(getResources().getColor(R.color.text_blue));
							pbutton.setAllCaps(true);

						}
					}

				});
		mListView.setOnTouchListener(touchListener);

		mProgressBar = (ProgressBar) view.findViewById(R.id.eventLog_progressBar);
		//Fetch all events
		//Log.d(TAG,"View initialized. Loading all events");
		//loadAllEvents();
		//Check for free trial
		/*if (mSelectedDevice != null && !mSelectedDevice.getProfile().getModelId().
				equalsIgnoreCase(PublicDefine.MODEL_ID_SMART_NURSERY) && (!mSelectedDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
				|| (Util.isThisVersionGreaterThan(mSelectedDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)))) {
			checkFreeTrialAndSubscription();
		}*/
		if (CommonUtil.getDailySummaryFeatureAvailable(mContext)) {
			mSummaryButton.setVisibility(View.VISIBLE);
		} else {
			mSummaryButton.setVisibility(View.GONE);
		}
		mIsSummaryOptIn = CommonUtil.getDailySummaryFeatureOptedIn(mContext);
		//Check for Video summary
		if ((!CommonUtil.getDailySummaryFeatureAvailable(mContext) || !CommonUtil.getDailySummaryFeatureAvailable(mContext))
				&& (mSelectedDevice != null && (!mSelectedDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
				|| (Util.isThisVersionGreaterThan(mSelectedDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION))))){
			mOfferExecutor.checkUserOfferOptIn(new OfferExecutor.IOfferOptInResponse() {
				@Override
				public void onOfferOptInResponse(boolean isOfferAvailable, boolean isOfferOptedIn) {
					if(mContext != null && isAdded()) {
						CommonUtil.setDailySummaryFeatureAvailable(mContext, isOfferAvailable);
						CommonUtil.setDailySummaryFeatureOptedIn(mContext, isOfferOptedIn);
						mIsSummaryOptIn = isOfferOptedIn;
						if (isOfferAvailable) {
							mSummaryButton.setVisibility(View.VISIBLE);
						} else {
							mSummaryButton.setVisibility(View.GONE);
						}
					}
				}
			});
		}
		return view;
	}

	/**
	 * Sets the current active camera
	 *
	 * @param selectedDevice : Camera for which  event is fetched
	 */
	public void setSelectedDevice(String registrationID) {
		mRegistrationID=registrationID;
		mSelectedDevice = DeviceSingleton.getInstance().getDeviceByRegId(registrationID);
	}

	public void setViewFinderEventListCallBack(IViewFinderEventListCallBack viewFinderEventListCallBack) {
		mIViewFinderEventListCallBack = viewFinderEventListCallBack;
	}

	private void loadAdapter(DeviceEventDetail eventDetailList) {
		Log.d(TAG,"Inside load adapter");
		if (eventDetailList != null) {
			Log.d(TAG,"Inside load adapter eventDetailList");
			if (mPageNumber >= eventDetailList.getTotalPages()) /*||
					mTotalCount <= CommonConstants.EVENT_PAGE_SIZE)*/ {
				mIsEndReached = true;
				hideFooterView();
			}
			if (eventDetailList.getEventResponse().length > 0) {
                if(mSelectedDevice != null) {
				    Date date = eventDetailList.getEventResponse()[0].getEventTime(mSelectedDevice.getProfile().getTimeZone());

					String dateString = CommonUtil.getTimeStampFromTimeZone(date, mSelectedDevice.getProfile().getTimeZone(), df1);
					mEventHistoryDate.setText(dateString);
				}
			}
			if (eventDetailList.getTotalEvents() != 0) {
				mNoEventView.setVisibility(View.INVISIBLE);
				if (mEventHistoryArrayAdapter == null && mContext != null) {
					mEventHistoryArrayAdapter = new EventHistoryArrayAdapter(mContext,
							CommonConstants.CONFIG_EVENT_HISTORY, R.layout.event_history_item, this, mSelectedDevice);
				}
				mEventHistoryArrayAdapter.setEventList(new ArrayList<EventResponse>(Arrays.asList(eventDetailList.getEventResponse())), mPageNumber);
				if (mListView != null) {
					Log.d(TAG,"Inside load adapter  notifyDataSetChanged");
					mEventHistoryArrayAdapter.notifyDataSetChanged();
				}
			}
			if (mEventHistoryArrayAdapter.getCount() == 0) {
				Log.d(TAG,"Inside load adapter adapter count 0");
				showNoEventView(mContext.getString(R.string.no_events_found));
			}
		}
	}

	@Override
	public void onLoadFullScreenImage(EventImage eventImage, EventResponse event) {
		EventImage eventImageLoad = eventImage;
		mLoadEvent = event;
		Intent fullScreenIntent = new Intent(mContext, EventHistoryFullScreenActivity.class);
		fullScreenIntent.putExtra(CommonConstants.EVENT_TYPE, CommonConstants.EVENT_IMAGE);
		fullScreenIntent.putExtra(CommonConstants.EVENT_IMAGE_TAG, eventImageLoad);
		startActivityForResult(fullScreenIntent, FULL_SCREEN_REQUEST_CODE);
		GeAnalyticsInterface.getInstance().trackEvent(AppEvents.SINGLE_EVENT,AppEvents.EH_EVENT_CLICKED,AppEvents.EVENT_CLICKED);
		ZaiusEvent eventClickedEvt = new ZaiusEvent(AppEvents.SINGLE_EVENT);
		eventClickedEvt.action(AppEvents.EH_EVENT_CLICKED);
		try {
			ZaiusEventManager.getInstance().trackCustomEvent(eventClickedEvt);
		} catch (ZaiusException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onPlayVideo(EventVideo eventVideo, EventResponse event) {
		mLoadEvent = event;
		Intent fullScreenIntent = new Intent(mContext, EventHistoryFullScreenActivity.class);
		fullScreenIntent.putExtra(CommonConstants.EVENT_TYPE, CommonConstants.EVENT_VIDEO);
		fullScreenIntent.putExtra(CommonConstants.EVENT_VIDEO_TAG, eventVideo);
		startActivityForResult(fullScreenIntent, FULL_SCREEN_REQUEST_CODE);
	}

	@Override
	public void downloadAndShareEvent(String imageUrl, int eventShareType, int actionType, Device selectedDevice) {
		if (selectedDevice != null && mEventUtil != null) {
			mEventUtil.downloadAndShareEventUtil(imageUrl, eventShareType, actionType, selectedDevice);
		}
	}


	@Override
	public void shareVideoEvent(EventVideo eventVideo, Device selectedDevice) {
		mSelectedDevice = selectedDevice;
		mEventVideo = eventVideo;
		showProgressDialog(getString(R.string.please_wait));
		if (mSelectedDevice != null) {
			if (mSelectedDevice.getProfile().isStandBySupported() &&
					mEventVideo.getStorageMode() == 1) {
				checkDeviceStatus();
			} else if (mEventUtil != null) {
				mEventUtil.shareVideoUtil(mEventVideo, mSelectedDevice, CommonConstants.ACTION_TYPE_SHARE);
			} else {
				dismissProgressDialog();
			}
		}

	}

	public void clearAdapter() {
		if (mEventHistoryArrayAdapter != null) {
			mEventHistoryArrayAdapter.clearEvents();
		}
		hideFooterView();
	}

	private boolean isCurrentDay(String day) {
		Calendar c = Calendar.getInstance();
		System.out.println("Current time => " + c.getTime());
		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = df.format(c.getTime());
		if (day.equalsIgnoreCase(formattedDate)) {
			return true;
		} else {
			return false;
		}
	}

	private void showFooterView() {
		mNoEventView.setVisibility(View.INVISIBLE);
		mFooterProgressView.setVisibility(View.VISIBLE);
		if (mProgressAnim != null) {
			mFooterProgressView.startAnimation(mProgressAnim);
		}
	}

	private void hideFooterView() {
		if (mFooterProgressView != null) {
			mFooterProgressView.clearAnimation();
			mFooterProgressView.setVisibility(View.INVISIBLE);
		}
	}

	/*private void loadAllEvents() {
		clearAdapter();
		mEventHistoryArrayAdapter.notifyDataSetChanged();
		mIsCurrentDay = true;
		mStartDay = "";
		mEndDay = "";
		showFooterView();
		fetchEventForCamera(0, Reason.ALLDATE);
	}*/

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FULL_SCREEN_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				if (mLoadEvent != null) {
					mProgressBar.setVisibility(View.VISIBLE);
					mDeleteIndex.clear();
					mDeleteIndex.add(mLoadEvent);
					deleteEventForCamera(String.valueOf(mLoadEvent.getID()));
				}

			}
		} else if (requestCode == PublicDefine.RESULT_SHARE_SNAPSHOT) {
			boolean canShare = false;
			if (resultCode == Activity.RESULT_OK) {

			}
		} else if (requestCode == PlanFragment.MANAGE_PLAN_REQUEST_CODE){
			if (mSelectedDevice != null && !mSelectedDevice.getProfile().getModelId().
					equalsIgnoreCase(PublicDefine.MODEL_ID_SMART_NURSERY)&& (!mSelectedDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
			        || (Util.isThisVersionGreaterThan(mSelectedDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)))) {
				checkFreeTrialAndSubscription();
			}
		}

	}

	private void launchViewFinder() {
		if (mIViewFinderEventListCallBack != null && mIViewFinderEventListCallBack.getLaunchReason().
				equalsIgnoreCase(CommonConstants.VIEW_FINDER_GOTO_STREAM)) {
			mIViewFinderEventListCallBack.onClick(CommonConstants.CLICK_TYPE_VIEW_FINDER);
		}

	}

	private void setSwipeRefreshView() {
		if (mFetchReason == Reason.NEWDATE && !mIsCurrentDay) {
			mSwipeRefreshView.setEnabled(false);
		} else {
			mSwipeRefreshView.setEnabled(true);
		}
	}

	private void checkDeviceStatus() {
		if (mContext != null) {

			final DeviceStatus deviceStatus = new DeviceStatus(mAccessToken, mSelectedDevice.getProfile().getRegistrationId());

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
											mSelectedDevice.getProfile().setAvailable(true);
											mSelectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);
											Log.d(TAG, "device online..start sharing video");
											if (mContext != null && isAdded()) {
												if (mEventUtil != null && mSelectedDevice != null) {
													mEventUtil.shareVideoUtil(mEventVideo, mSelectedDevice, CommonConstants.ACTION_TYPE_SHARE);
												} else {
													dismissProgressDialog();
												}
											}
										} else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_STANDBY) == 0) {
											mSelectedDevice.getProfile().setAvailable(false);
											mSelectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);
											Log.d(TAG, "device standby..wakeup");
											//wakeup device
											wakeUpRemoteDevice();
										} else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_OFFLINE) == 0) {
											Log.d(TAG, "setting device available false");
											mSelectedDevice.getProfile().setAvailable(false);
											mSelectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
											//device offline
											if (mContext != null && isAdded()) {
												dismissProgressDialog();
												Toast.makeText(mContext, getString(R.string.camera_offline), Toast.LENGTH_SHORT).show();
											}
										} else {
											if(mContext != null && isAdded()) {
												dismissProgressDialog();
												Toast.makeText(mContext, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_SHORT).show();
											}
										}
									} else {
										if(mContext != null && isAdded()) {
											dismissProgressDialog();
											Toast.makeText(mContext, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_SHORT).show();
										}
									}
								} else {
									if(mContext != null && isAdded()) {
										dismissProgressDialog();
										Toast.makeText(mContext, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_SHORT).show();
									}
								}
							} else {
								if(mContext != null && isAdded()) {
									dismissProgressDialog();
									Toast.makeText(mContext, getString(R.string.event_full_screen_fail_camera), Toast.LENGTH_SHORT).show();
								}
							}
						}
					},
					new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							if(mContext != null && isAdded()) {
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

	private void wakeUpRemoteDevice() {
		if (mContext != null) {
			showProgressDialog(getString(R.string.viewfinder_progress_wakeup));
			Log.d(TAG, "wakeUpRemoteDevice");
			mDeviceWakeup = DeviceWakeup.newInstance();
			mDeviceWakeup.wakeupDevice(mSelectedDevice.getProfile().registrationId, mAccessToken, mDeviceHandler, mSelectedDevice);


		}
	}

	private Handler mDeviceHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CommonConstants.DEVICE_WAKEUP_STATUS:

					/*if (mProgressDialog != null && mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}*/
					boolean result = (boolean) msg.obj;
					Log.d(TAG, "Device status task completed..device status:" + result);
					if (result) {
						if (mEventUtil != null && mSelectedDevice != null) {
							mEventUtil.shareVideoUtil(mEventVideo, mSelectedDevice, CommonConstants.ACTION_TYPE_SHARE);
						} else {
							if(mContext != null && isAdded()) {
								dismissProgressDialog();
							}
						}
					} else {
						if (mContext != null && isAdded()) {
							dismissProgressDialog();
							Log.d(TAG, "wakeup device:failure");
							Toast.makeText(mContext, getResources().getString(R.string.failed_to_start_device), Toast.LENGTH_LONG).show();
						}
					}

					break;
				default:
					if(mContext != null && isAdded()) {
						dismissProgressDialog();
					}
					break;

			}
		}
	};

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG,"On Pause of Event History");
		dismissProgressDialog();

		if (mDeviceWakeup != null) {
			mDeviceWakeup.cancelTask(mSelectedDevice.getProfile().registrationId,mDeviceHandler);
		}

		if (mFreeTrialSuccessDialog != null && mFreeTrialSuccessDialog.isShowing()) {
			mFreeTrialSuccessDialog.dismiss();
		}

		if (mFreeTrialFailureDialog != null && mFreeTrialFailureDialog.isShowing()) {
			mFreeTrialFailureDialog.dismiss();
		}

		dismissEnableProgressDialog();
		mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
		mLoadingMoreItems = false;
		mIsEventDeleteInProgress = false;
		mIsEventFetchInProgress = false;
		Log.d(TAG,"mEventTimer cancelled in onpause");
		resetEventTimer();
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG,"On Start of Event History and fetch today's data");
		if (mEventHistoryArrayAdapter == null || mEventHistoryArrayAdapter.getCount() == 0) {
			fetchToday();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG,"On Resume of Event History");
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setCancelable(false);
		if (mSelectedDevice != null && !mSelectedDevice.getProfile().getModelId().
				equalsIgnoreCase(PublicDefine.MODEL_ID_SMART_NURSERY) && (!mSelectedDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
		        || (Util.isThisVersionGreaterThan(mSelectedDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)))) {
			checkFreeTrialAndSubscription();
		}
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	private void checkFreeTrialAndSubscription() {
		if (mContext != null && isAdded()) {
			SubscriptionService mSubscriptionService = SubscriptionService.getInstance(mContext);
			HubbleRequest hubbleRequest = new HubbleRequest(mAccessToken);
			mSubscriptionService.getUserSubscriptionPlan(SubscriptionInfo.ServicePlan.MONITORING_SERVICE_PLAN, hubbleRequest,
					new Response.Listener<UserSubscriptionPlanResponse>() {
						@Override
						public void onResponse(UserSubscriptionPlanResponse response) {
							if (mContext != null && isAdded() && mSelectedDevice != null) {
								if (response != null) {
									UserSubscriptionPlanResponse.PlanResponse[] mPlanResponseList = response.getPlanResponse();
									if (mPlanResponseList == null || mPlanResponseList.length == 0 ||
											!checkActiveSubscription(mPlanResponseList)) {
										new AsyncTask<Void, Void, Models.ApiResponse<List<SerializableDeviceProfile>>>() {
											@Override
											protected Models.ApiResponse<List<SerializableDeviceProfile>> doInBackground(Void... params) {
												Models.ApiResponse<List<SerializableDeviceProfile>> apiResponse = null;
												try {
													apiResponse = Api.getInstance().getService().getDeviceProfiles2(mAccessToken);
												} catch (RetrofitError error) {
												}
												return apiResponse;
											}

											@Override
											protected void onPostExecute(Models.ApiResponse<List<SerializableDeviceProfile>> apiResponse) {
												if (mContext != null && isAdded()) {
													SerializableDeviceProfile selectedDevice = null;
													//DeviceDetailsResponse[] deviceDetailsResponseList = response.getDeviceDetailsResponse();
													List<SerializableDeviceProfile> deviceDetailsResponseList = apiResponse.getData();
													if (deviceDetailsResponseList != null && deviceDetailsResponseList.size() > 0) {
														for (SerializableDeviceProfile device : deviceDetailsResponseList) {
															if (device.getRegistrationId().
																	equalsIgnoreCase(mSelectedDevice.getProfile().getRegistrationId())) {
																selectedDevice = device;
																break;
															}
														}
														if (selectedDevice != null) {
															boolean isFreeTrialActive = selectedDevice.getDeviceFreeTrial() != null
																	&& PlanFragment.ACTIVE.equals(selectedDevice.getDeviceFreeTrial().getStatus());
															boolean isFreeTrialExpired = selectedDevice.getDeviceFreeTrial() != null
																	&& PlanFragment.EXPIRE.equals(selectedDevice.getDeviceFreeTrial().getStatus());
															String planId = selectedDevice.getPlanId();
															if (!isFreeTrialActive && !isFreeTrialExpired && selectedDevice.getFreeTrialQuota() > 0
																	&& (planId == null || planId.isEmpty() || PublicDefineGlob.FREEMIUM.equals(planId))) {
																mPromoSubcriptionButton.setVisibility(View.VISIBLE);
																if (SubscriptionUtil.getShowFreeTrial(mContext, mSelectedDevice.getProfile().getRegistrationId())) {
																	SubscriptionUtil.setShowFreeTrial(mContext, mSelectedDevice.getProfile().getRegistrationId(), false);
																	showFreeTrialDialog();
																}
															} else {
																mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
																Log.d(TAG, "There is camera that is already free trial or no camera that is eligible for free trial. " +
																		"Do not show button Try us for Free.");
															}
														}

													} else {
														mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
														Log.d(TAG, "No data while fetching subscription ");
													}
												}
											}
										}.execute();

										/*HubbleRequest hubbleRequest = new HubbleRequest(mAccessToken);
										mDeviceManagerService.getDeviceList(hubbleRequest, new Response.Listener<MultipleDeviceDetails>() {
											@Override
											public void onResponse(MultipleDeviceDetails response) {
												if (mContext != null && isAdded()) {
													DeviceDetailsResponse selectedDevice = null;
													DeviceDetailsResponse[] deviceDetailsResponseList = response.getDeviceDetailsResponse();
													if (deviceDetailsResponseList != null && deviceDetailsResponseList.length > 0) {
														for (DeviceDetailsResponse device : deviceDetailsResponseList) {
															if (device.getRegistrationID().
																	equalsIgnoreCase(mSelectedDevice.getProfile().getRegistrationId())) {
																selectedDevice = device;
																break;
															}
														}
														if (selectedDevice != null) {
															boolean isFreeTrialActive = selectedDevice.getDeviceFreeTrialDetails() != null
																	&& "active".equals(selectedDevice.getDeviceFreeTrialDetails().getStatus());
															boolean isFreeTrialExpired = selectedDevice.getDeviceFreeTrialDetails() != null
																	&& "expire".equals(selectedDevice.getDeviceFreeTrialDetails().getStatus());
															String planId = selectedDevice.getPlanID();
															if (!isFreeTrialActive && !isFreeTrialExpired && selectedDevice.getFreeTrialQuota() > 0
																	&& (planId == null || planId.isEmpty() || PublicDefineGlob.FREEMIUM.equals(planId))) {
																mPromoSubcriptionButton.setVisibility(View.VISIBLE);
																if (SubscriptionUtil.getShowFreeTrial(mContext, mSelectedDevice.getProfile().getRegistrationId())) {
																	SubscriptionUtil.setShowFreeTrial(mContext, mSelectedDevice.getProfile().getRegistrationId(), false);
																	showFreeTrialDialog();
																}
															} else {
																mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
																Log.d(TAG, "There is camera that is already free trial or no camera that is eligible for free trial. " +
																		"Do not show button Try us for Free.");
															}
														}

													} else {
														mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
														Log.d(TAG, "No data while fetching subscription ");
													}
												}

											}
										}, new Response.ErrorListener() {
											@Override
											public void onErrorResponse(VolleyError error) {
												if (mContext != null && isAdded()) {
													mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
													Log.e(TAG, "Error while fetching subscription " + error.toString());
												}
											}
										});*/

									} else {
										mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
										Log.d(TAG, "Camera is already on plan");
									}

								} else {
									mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
									Log.d(TAG, "Plan response is null");
								}
							}
						}
					},
					new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							if (mContext != null && isAdded()) {
								mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
								Log.e(TAG, "Error while fetching subscription " + error.toString());
							}

						}
					});
		}

	}


	private boolean checkActiveSubscription(UserSubscriptionPlanResponse.PlanResponse[] subscriptionPlan) {
		for (UserSubscriptionPlanResponse.PlanResponse item : subscriptionPlan) {
			if ("active".equals(item.getPlanState()) || "pending".equals(item.getPlanState())
					|| "canceled".equals(item.getPlanState())) {
				return true;
			}
		}
		return false;
	}

	private void showFreeTrialDialog() {
		if (getActivity() != null && mContext != null && mSelectedDevice != null) {
			mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
			mAskForFreeTrialDialog = new AskForFreeTrialDialog(mContext, new AskForFreeTrialDialog.PlanListener() {
				@Override
				public void onEnableFreeTrialClick() {
					dismissFreeTrialDialog();
					enableFreeTrial();
				}

				@Override
				public void onUpgradePlanClick() {
					dismissFreeTrialDialog();
					/*Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(getString(R.string.hubble_web_app_url)));
					startActivity(intent);*/
					Intent intent = new Intent(mContext, ManagePlanActivity.class);
					startActivityForResult(intent,PlanFragment.MANAGE_PLAN_REQUEST_CODE);
					mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
				}
			});
			mAskForFreeTrialDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					mPromoSubcriptionButton.setVisibility(View.VISIBLE);
				}
			});
			mAskForFreeTrialDialog.show();
		}
	}

	private void enableFreeTrial() {
		if (mContext != null && mSelectedDevice != null) {
			mEnableProgressDialog = ProgressDialog.show(mContext, null, getString(R.string.free_trial_enable_progress));
			final SubscriptionCommandUtil subscriptionUtil = new SubscriptionCommandUtil(mContext, mAccessToken);
			DeviceManagerService deviceManagerService = DeviceManagerService.getInstance(mContext);
			DeviceID deviceID = new DeviceID(mAccessToken, mSelectedDevice.getProfile().getRegistrationId());
			deviceManagerService.enableFreeTrailOnDevice(deviceID, new Response.Listener<DeviceFreeTrialDetails>() {
				@Override
				public void onResponse(DeviceFreeTrialDetails response) {
					if (mContext != null && isAdded()) {
						dismissEnableProgressDialog();
						if (mSelectedDevice != null) {
							if (response != null && response.getStatus() == 200) {
								mPromoSubcriptionButton.setVisibility(View.INVISIBLE);
								mFreeTrialSuccessDialog = new FreeTrialSuccessDialog(mContext);
								mFreeTrialSuccessDialog.show();
								subscriptionUtil.enableMotionVideoRecording(mSelectedDevice.getProfile().getRegistrationId());
							} else {
								mPromoSubcriptionButton.setVisibility(View.VISIBLE);
								//subscriptionUtil.disableMotionVideoRecording(mSelectedDevice.getProfile().getRegistrationId());
								mFreeTrialFailureDialog = new FreeTrialFailureDialog(mContext);
								mFreeTrialFailureDialog.show();
							}
						}
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					if (mContext != null && isAdded()) {
						dismissEnableProgressDialog();
						//Todo:show failure dialog
						if (mSelectedDevice != null) {
							mPromoSubcriptionButton.setVisibility(View.VISIBLE);
							//subscriptionUtil.disableMotionVideoRecording(mSelectedDevice.getProfile().getRegistrationId());
							mFreeTrialFailureDialog = new FreeTrialFailureDialog(mContext);
							mFreeTrialFailureDialog.show();
						}
					}
				}
			});
		}
	}

	private void dismissFreeTrialDialog() {
		if (mAskForFreeTrialDialog != null && mAskForFreeTrialDialog.isShowing()) {
			mAskForFreeTrialDialog.dismiss();
		}
	}

	private void dismissEnableProgressDialog() {
		if (mEnableProgressDialog != null && mEnableProgressDialog.isShowing()) {
			mEnableProgressDialog.dismiss();
		}
		mEnableProgressDialog = null;
	}

	private void dismissVideoAnalyticsDialog(){
		if (mVideoAnalyticsOfferDialog != null && mVideoAnalyticsOfferDialog.isShowing()) {
			mVideoAnalyticsOfferDialog.dismiss();
		}
	}

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

	private void showNoEventView(String message){
		hideFooterView();
		mNoEventView.setVisibility(View.VISIBLE);
		mNoEventView.setText(message);

	}

	private class EventTask extends TimerTask {
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
							mNoEventView.setVisibility(View.VISIBLE);
							mNoEventView.setText(mContext.getString(R.string.event_fetch_progress));
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
							mCalenderButton.setEnabled(true);
							setSwipeRefreshView();
							mSwipeRefreshView.setRefreshing(false);
							showNoEventView(mContext.getString(R.string.event_fetch_failure));
						}
					});
				}
				mIsEventFetchInProgress = false;
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

	private void fetchToday(){

		if(mSelectedDevice != null && mSelectedDevice.getProfile() != null) {
			mBackToToday.setVisibility(View.GONE);
			Log.d(TAG, "fetching data for today");
			mIsCurrentDay = true;
			String timezoneString = CommonUtil.getCameraTimeZone(mSelectedDevice.getProfile().getTimeZone());
			TimeZone timeZone = TimeZone.getTimeZone(timezoneString);
			Calendar calendar1 = Calendar.getInstance(timeZone);
			int year = calendar1.get(Calendar.YEAR);
			int month = calendar1.get(Calendar.MONTH);
			int day = calendar1.get(Calendar.DAY_OF_MONTH);
			calendar1.set(year, month, day, 0, 0, 0);
			Date selectedDate = calendar1.getTime();
			mStartDay = df3.format(selectedDate);
			calendar1.add(Calendar.DAY_OF_YEAR, 1);
			Date tomorrow = calendar1.getTime();
			mEndDay = df3.format(tomorrow);
			Log.d(TAG, "fetching data for today starttime " + mStartDay + "End day " + mEndDay);

			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
			String formatedDateToCompare = sdf.format(selectedDate);

			if (mEventHistoryArrayAdapter == null || mEventHistoryArrayAdapter.getCount() == 0) {
				mSwipeRefreshView.setRefreshing(false);
				showFooterView();
			} else {
				hideFooterView();
				mSwipeRefreshView.setEnabled(true);
				mSwipeRefreshView.setRefreshing(true);
			}

			mPageNumber = 0;

			mEventHistoryDate.setText(new SimpleDateFormat("dd MMM, EEEE yyyy").format(selectedDate));

			Log.d(TAG, "Loading today's data");
			fetchEventForCamera(0, Reason.NEWDATE);
		}
	}

	private void showDeleteAllConfirmationDialog() {
		AlertDialog mAlertDialog = new AlertDialog.Builder(mContext)
				.setMessage(mContext.getString(R.string.delete_all_confirmation_msg))
				.setPositiveButton(mContext.getString(R.string.Delete), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mProgressBar.setVisibility(View.VISIBLE);
						//For delete all no event code is required
						deleteEventForCamera(null);

						GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.DELETE_ALL_EVENTS_OK,AppEvents.DELETE_ALL_EVENTS);
						ZaiusEvent deleteEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
						deleteEvt.action(AppEvents.DELETE_ALL_EVENTS_OK);
						try {
							ZaiusEventManager.getInstance().trackCustomEvent(deleteEvt);
						} catch (ZaiusException e) {
							e.printStackTrace();
						}
					}
				})
				.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_HISTORY, AppEvents.DELETE_ALL_EVENTS_CANCEL, AppEvents.CANCEL_CLICKED);
						// do nothing
						ZaiusEvent deleteCancelEvt = new ZaiusEvent(AppEvents.EVENT_HISTORY);
						deleteCancelEvt.action(AppEvents.DELETE_ALL_EVENTS_CANCEL);
						try {
							ZaiusEventManager.getInstance().trackCustomEvent(deleteCancelEvt);
						} catch (ZaiusException e) {
							e.printStackTrace();
						}

					}
				})
				.show();
		Button nbutton = mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		nbutton.setTextColor(getResources().getColor(R.color.text_blue));
		nbutton.setAllCaps(true);
		Button pbutton = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		pbutton.setTextColor(getResources().getColor(R.color.text_blue));
		pbutton.setAllCaps(true);
		TextView messageView = (TextView) mAlertDialog.findViewById(android.R.id.message);
		messageView.setGravity(Gravity.CENTER);

	}
}
