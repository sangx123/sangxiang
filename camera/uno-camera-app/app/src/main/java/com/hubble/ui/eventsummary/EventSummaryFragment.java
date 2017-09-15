package com.hubble.ui.eventsummary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeleteEventSummary;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceEventSummary;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeleteEventSummaryDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceEventSummaryResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceSummaryDetail;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.ui.IViewFinderEventListCallBack;
import com.hubbleconnected.camera.R;
import com.hubble.util.CommonConstants;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.hubble.PublicDefineGlob;

/**
 * Created by Admin on 06-03-2017.
 */
public class EventSummaryFragment extends Fragment {

	private Device mSelectedDevice = null;
	private String mRegId = null;
	private Activity mActivity = null;
	private String mAccessToken = null;
	private DeviceManagerService mDeviceManagerService = null;
	private EventSummaryAdapter mEventSummaryAdapter = null;

	private ImageView mCalenderView;
	private RecyclerView mSummaryRecyclerView;
	private RelativeLayout mNoDataLayout;
	private LinearLayout mSummaryConstructionLayout;
	private TextView mErrorMessage;
	private Button mRetryButton;
	private ProgressBar mProgressBar;

	private boolean mIsSummaryFetchInProgress = false;

	private Map<String, EventSummary> mDayWiseSummaryMap = new HashMap<String, EventSummary>();
	private IViewFinderEventListCallBack mIViewFinderEventListCallBack = null;

	private final String DELETE_RESPONSE_CONSTANT = "Events Summary deleted!";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mActivity = getActivity();
		mAccessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
	}


	/**
	 * Sets the current active camera
	 *
	 * @param selectedDevice : Camera for which  event is fetched
	 */
	public void setSelectedDevice(String registrationId) {
		mRegId=registrationId;
		mSelectedDevice = DeviceSingleton.getInstance().getDeviceByRegId(mRegId);
	}

	public void setViewFinderEventListCallBack(IViewFinderEventListCallBack viewFinderEventListCallBack) {
		mIViewFinderEventListCallBack = viewFinderEventListCallBack;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mActivity = getActivity();
		mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		View view = inflater.inflate(R.layout.fragment_event_summary, null);
		initDeviceManager();
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
				if(mIViewFinderEventListCallBack != null) {
					mIViewFinderEventListCallBack.launchCameraSettings();
				}
			}
		});

		view.findViewById(R.id.camera_spinner).setVisibility(View.GONE);
		TextView actionBarTitle = (TextView) view.findViewById(R.id.vf_toolbar_title);
		actionBarTitle.setVisibility(View.VISIBLE);
		if (mSelectedDevice != null) {
			actionBarTitle.setText(mSelectedDevice.getProfile().getName());
		}
		mCalenderView = (ImageView) view.findViewById(R.id.calender_button);
		mCalenderView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO: Implement calender functionality
			}
		});

		mSummaryRecyclerView = (RecyclerView) view.findViewById(R.id.event_summary_recyclerview);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
		mSummaryRecyclerView.setLayoutManager(mLayoutManager);

		//Delete event summary on swipe
		ItemTouchHelper.Callback callback =
				new RecyclerViewItemTouchHelperCallback(new ItemSwipeHelper(){
					@Override
					public void onSwipe(final int position) {
						mEventSummaryAdapter.notifyDataSetChanged();
						if (mEventSummaryAdapter.isDataAvailableToDelete(position)) {
							if (mActivity != null) {
								EventSummary eventSummary = mEventSummaryAdapter.getEventSummary(position);
								final Date dayToDelete = eventSummary.getDate();
								if(dayToDelete != null) {
									SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy");
									new AlertDialog.Builder(mActivity)
											.setMessage(mActivity.getString(R.string.deletion_confirmation_dialog_summary, sdf.format(dayToDelete)))
											.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_SUMMARY, AppEvents.SWIPE_TO_DELETE_OK, AppEvents.OK_CLICKED);
													ZaiusEvent deleteOkEvt = new ZaiusEvent(AppEvents.EVENT_SUMMARY);
													deleteOkEvt.action(AppEvents.SWIPE_TO_DELETE_OK);
													try {
														ZaiusEventManager.getInstance().trackCustomEvent(deleteOkEvt);
													} catch (ZaiusException e) {
														e.printStackTrace();
													}
													//delete call
													mProgressBar.setVisibility(View.VISIBLE);
													SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
													deleteEventSummary(sdf.format(dayToDelete), position);
												}
											})
											.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int which) {
													GeAnalyticsInterface.getInstance().trackEvent(AppEvents.EVENT_SUMMARY, AppEvents.SWIPE_TO_DELETE_CANCEL, AppEvents.CANCEL_CLICKED);
													// do nothing
													ZaiusEvent deleteCancelEvt = new ZaiusEvent(AppEvents.EVENT_SUMMARY);
													deleteCancelEvt.action(AppEvents.SWIPE_TO_DELETE_CANCEL);
													try {
														ZaiusEventManager.getInstance().trackCustomEvent(deleteCancelEvt);
													} catch (ZaiusException e) {
														e.printStackTrace();
													}
													mEventSummaryAdapter.notifyDataSetChanged();

												}
											})
											.show();
								}

							}
						}else {
							Toast.makeText(mActivity, mActivity.getString(R.string.summary_unavailable_to_delete), Toast.LENGTH_LONG).show();
						}
					}
				});
		ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
		touchHelper.attachToRecyclerView(mSummaryRecyclerView);

		mEventSummaryAdapter = new EventSummaryAdapter(mActivity, new IEventSummaryActionCallBack() {
			@Override
			public void onSummaryVideoPlay(EventSummary eventSummary) {
				if (mActivity != null && mSelectedDevice != null) {
					Intent fullScreenIntent = new Intent(mActivity, EventSummaryFullScreen.class);
					fullScreenIntent.putExtra(EventSummaryConstant.EVENT_SUMMARY_DETAIL_EXTRA, eventSummary);
					fullScreenIntent.putExtra(EventSummaryConstant.EVENT_SUMMARY_CAMERA_REGID_EXTRA,
							mSelectedDevice.getProfile().getRegistrationId());
					startActivity(fullScreenIntent);
				}
			}


		});
		if(mSelectedDevice != null){
			mEventSummaryAdapter.setRegdId(mSelectedDevice.getProfile().getRegistrationId());
		}
		mSummaryRecyclerView.setAdapter(mEventSummaryAdapter);

		mNoDataLayout = (RelativeLayout) view.findViewById(R.id.event_summary_no_data_layout);
		mErrorMessage = (TextView) view.findViewById(R.id.event_summary_no_data_detail);
		mRetryButton = (Button) view.findViewById(R.id.event_summary_retry);
		mRetryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fetchEventSummary();
			}
		});

		mSummaryConstructionLayout = (LinearLayout)view.findViewById(R.id.summary_under_construction_layout);
		mProgressBar = (ProgressBar) view.findViewById(R.id.event_summary_progressBar);
		mProgressBar.setVisibility(View.VISIBLE);

		fetchEventSummary();

		return view;
	}

	private void initDeviceManager() {
		//Device Event Instance Setup
		if (mSelectedDevice != null && mActivity != null) {
			if (mDeviceManagerService == null) {
				mDeviceManagerService = DeviceManagerService.getInstance(mActivity);
			}
		}
	}

	private void fetchEventSummary() {
		mProgressBar.setVisibility(View.VISIBLE);
		mNoDataLayout.setVisibility(View.GONE);
		mSummaryConstructionLayout.setVisibility(View.GONE);
		if (mSelectedDevice != null && mActivity != null) {
			if (!mIsSummaryFetchInProgress) {
				if (CommonUtil.isInternetAvailable(mActivity)) {
					mIsSummaryFetchInProgress = true;
					mRegId = mSelectedDevice.getProfile().getRegistrationId();
					final DeviceEventSummary deviceEventSummary = new DeviceEventSummary(mAccessToken, mRegId);
					deviceEventSummary.setWindow(EventSummaryConstant.WINDOW_DAILY);
					mDeviceManagerService.getDeviceEventSummary(deviceEventSummary, new Response.Listener<DeviceEventSummaryResponse>() {
						@Override
						public void onResponse(DeviceEventSummaryResponse response) {
							mIsSummaryFetchInProgress = false;
							if (mActivity != null && isAdded()) {
								if (response != null && response.getStatus() == 200 && response.getMessage().equalsIgnoreCase("Success")) {
									DeviceEventSummaryResponse.DeviceSummaryDaily deviceSummaryDaily = response.getDeviceSummaryDaily();
									if (deviceSummaryDaily != null) {
										loadAdapter(deviceSummaryDaily.getDeviceSummaryDailyDetail());
									} else {
										mSummaryConstructionLayout.setVisibility(View.GONE);
										mNoDataLayout.setVisibility(View.VISIBLE);
										mErrorMessage.setVisibility(View.INVISIBLE);
										mRetryButton.setVisibility(View.VISIBLE);
									}
								} else {
									mSummaryConstructionLayout.setVisibility(View.GONE);
									mNoDataLayout.setVisibility(View.VISIBLE);
									mErrorMessage.setVisibility(View.VISIBLE);
									mErrorMessage.setText(getString(R.string.no_summary_error_message));
									mRetryButton.setVisibility(View.INVISIBLE);
									//TODO : Error scenario response is not correct
								}
								mProgressBar.setVisibility(View.GONE);
							} else {
								//TODO : Error scenario activity is gone : no need to do anything
							}
						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							mIsSummaryFetchInProgress = false;
							if (mActivity != null && isAdded()) {
								boolean isSummaryConstructInProgress = false;
								if(error != null && error.networkResponse != null && error.networkResponse.data != null
										&& error.networkResponse.statusCode == 404){
									try {
										JSONObject summaryObject = new JSONObject(new String(error.networkResponse.data));
										String message = summaryObject.getString("message");
										if(message.equalsIgnoreCase(EventSummaryConstant.EVENT_SUMMARY_UNDER_CONSTRUCTION)){
											isSummaryConstructInProgress = true;
										}
									}catch (JSONException e){
										isSummaryConstructInProgress = false;
									}

								}
								if(isSummaryConstructInProgress){
									mSummaryConstructionLayout.setVisibility(View.VISIBLE);
									mNoDataLayout.setVisibility(View.GONE);
								}else {
									mSummaryConstructionLayout.setVisibility(View.GONE);
									mNoDataLayout.setVisibility(View.VISIBLE);
									mErrorMessage.setVisibility(View.VISIBLE);
									mErrorMessage.setText(getString(R.string.no_summary_error_message));
									mRetryButton.setVisibility(View.INVISIBLE);
								}
								mProgressBar.setVisibility(View.GONE);
							} else {
								//TODO : Error scenario activity is gone no need to do anything
							}
						}
					});
				} else {
					mIsSummaryFetchInProgress = false;
					mProgressBar.setVisibility(View.GONE);
					mSummaryConstructionLayout.setVisibility(View.GONE);
					mNoDataLayout.setVisibility(View.VISIBLE);
					mErrorMessage.setVisibility(View.VISIBLE);
					mErrorMessage.setText(getString(R.string.enable_internet_connection));
					mRetryButton.setVisibility(View.VISIBLE);
				}
			} else {
				//ToDO fetch is in progress.
			}
		} else {
			mProgressBar.setVisibility(View.GONE);
			mSummaryConstructionLayout.setVisibility(View.GONE);
			mNoDataLayout.setVisibility(View.VISIBLE);
			mErrorMessage.setVisibility(View.VISIBLE);
			mErrorMessage.setText("Device not found Exit and relaunch the screen");
			mRetryButton.setVisibility(View.INVISIBLE);
		}

	}

	private void loadAdapter(DeviceSummaryDetail[] deviceSummaryDetailArray) {
		if (deviceSummaryDetailArray != null && deviceSummaryDetailArray.length > 0) {
			List<EventSummary> eventSummaries = new ArrayList<EventSummary>();
			for (DeviceSummaryDetail deviceSummaryDetail : deviceSummaryDetailArray) {
				EventSummary eventSummary = new EventSummary();
				eventSummary.setDate(getDate(deviceSummaryDetail.getDay()));
				DeviceSummaryDetail.DailySummaryDetail[] dailyDetailArray = deviceSummaryDetail.getDailySummaryDetail();
				if (dailyDetailArray != null && dailyDetailArray.length > 0) {
					for (DeviceSummaryDetail.DailySummaryDetail dailySummary : dailyDetailArray) {
						CommonUtil.EventType type = CommonUtil.EventType.fromAlertIntType(dailySummary.getAlertType());
						if (type == CommonUtil.EventType.MOTION && dailySummary.getSummaryUrl() != null
								&& !dailySummary.getSummaryUrl().equalsIgnoreCase(EventSummaryConstant.EVENT_SUMMARY_NOT_COMPUTED) ) {
							eventSummary.setSummaryVideoUrlPath(dailySummary.getSummaryUrl());
							eventSummary.setSnapShotPath(dailySummary.getSummarySnapUrl());
							eventSummary.setSummaryType(EventSummaryConstant.MOTION_SUMMARY_VIEW);
							eventSummary.setTotalMotionEvent(dailySummary.getTotal());
						} else if (type == CommonUtil.EventType.SOUND) {
							eventSummary.setTotalSoundEvent(dailySummary.getTotal());
						} else if (type == CommonUtil.EventType.TEMP_HIGH) {
							eventSummary.setTempMax(dailySummary.getMax());
						} else if (type == CommonUtil.EventType.TEMP_LOW) {
							eventSummary.setTempMin(dailySummary.getMin());
						}
					}
				} else {
					eventSummary.setSummaryType(EventSummaryConstant.NO_SUMMARY_VIEW);
				}
				eventSummaries.add(eventSummary);
			}
			mSummaryConstructionLayout.setVisibility(View.GONE);
			mNoDataLayout.setVisibility(View.GONE);
			if(mSelectedDevice != null){
				mEventSummaryAdapter.setRegdId(mSelectedDevice.getProfile().getRegistrationId());
			}
			mEventSummaryAdapter.setData(eventSummaries);
		} else {
			mSummaryConstructionLayout.setVisibility(View.GONE);
			mNoDataLayout.setVisibility(View.VISIBLE);
			mErrorMessage.setVisibility(View.INVISIBLE);
			mRetryButton.setVisibility(View.INVISIBLE);
		}

	}



	private Date getDate(String dateToParse) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		try {
			Date date = format.parse(dateToParse);
			return date;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private void deleteEventSummary(String dateForDeleteSummary, final int position){
		if (CommonUtil.isInternetAvailable(mActivity)) {
			if (mDeviceManagerService != null) {
				DeleteEventSummary deleteEventSummary = new DeleteEventSummary(mAccessToken, mRegId);
				deleteEventSummary.setEventSummaryDay(dateForDeleteSummary);
				mDeviceManagerService.deleteDeviceEventSummary(deleteEventSummary, new Response.Listener<DeleteEventSummaryDetails>() {
					@Override
					public void onResponse(DeleteEventSummaryDetails response) {
						if (mActivity != null && isAdded()) {
							mProgressBar.setVisibility(View.GONE);
							if (response != null && response.getStatus() == 200) {
								String responseData = response.getData();
								if (responseData != null && responseData.equalsIgnoreCase(DELETE_RESPONSE_CONSTANT)) {

									Toast.makeText(mActivity, mActivity.getString(R.string.summary_deletion_successful), Toast.LENGTH_LONG).show();
								}
								EventSummary previousEventSummary = mEventSummaryAdapter.getEventSummary(position);
								EventSummary eventSummary = new EventSummary();
								eventSummary.setDate(previousEventSummary.getDate());
								eventSummary.setSummaryType(EventSummaryConstant.NO_SUMMARY_VIEW);
								mEventSummaryAdapter.setItemAt(position, eventSummary);
							} else {
								Toast.makeText(mActivity, mActivity.getString(R.string.summary_deletion_failure), Toast.LENGTH_LONG).show();
							}
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						if (mActivity != null && isAdded()) {
							mProgressBar.setVisibility(View.GONE);
							Toast.makeText(mActivity, mActivity.getString(R.string.summary_deletion_failure), Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		} else {
			Toast.makeText(mActivity, mActivity.getString(R.string.dialog_no_network_enabled), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mActivity = null;
	}

	@Override
	public void onStop() {
		super.onStop();
		if(mEventSummaryAdapter != null) {
			mEventSummaryAdapter.dismissInfoDialog();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
}
