package com.hubble.subscription;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.dialog.FreeTrialFailureDialog;
import com.hubble.dialog.FreeTrialSuccessDialog;
import com.hubble.framework.networkinterface.v1.pojo.HubbleRequest;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceID;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceDetailsResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceFreeTrialDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.MultipleDeviceDetails;
import com.hubble.framework.service.cloudclient.user.pojo.request.UpdateSubscription;
import com.hubble.framework.service.cloudclient.user.pojo.response.UserPlanResponse;
import com.hubble.framework.service.cloudclient.user.pojo.response.UserSubscriptionPlanResponse;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.framework.service.subscription.SubscriptionInfo;
import com.hubble.framework.service.subscription.SubscriptionService;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.util.SubscriptionUtil;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import base.hubble.Api;
import base.hubble.IHubbleRestApi;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.devices.SerializableDeviceProfile;
import base.hubble.subscriptions.DeviceSubscription;
import retrofit.RetrofitError;
import com.hubbleconnected.camera.R;
/**
 * Created by Admin on 19-01-2017.
 */
public class PlanFragment extends Fragment implements PlanAdapter.IClickCallBack {

	private final String TAG = "PlanFragment";

	public final static int MANAGE_PLAN_REQUEST_CODE = 100;

	private RelativeLayout mPlanStoreFooterLayout;
	private LinearLayout mPlanParentLayout;
	private RelativeLayout mNoPlanTopLayout;
	private RelativeLayout mPlanTopLayout;
	private RelativeLayout mPlanActiveLayout;
	private RelativeLayout mPlanDowngradeLayout;
	private TextView mCurrentPlanLabel;
	private TextView mCurrentPlanText;
	private TextView mCurrentPlanDetail;
	private TextView mFromPlanText;
	private TextView mTotPlanText;
	private TextView mRenewDateTag;
	private TextView mRenewDate;
	private Button mEnablePaidPlan;
	private Button mChangePlan;
	private Button mUnsubscribePlan;
	private TextView mCameraListTextView;
	private ListView mListView;
	private ProgressBar mProgressBar;
	private TextView mNoDeviceTextView;
	private RelativeLayout mNoDataLayout;
	private TextView mNoDataDetailText;
	private Button mRetryButton;

	private String mUserPlan = null;
	private String mUserPlanStatus = null;
	private String mUserPendingPlan = null;
	private String mExpiryDate = null;
	private String mSubscriptionUUID = new String();
	private boolean isFetchingPlan = false;
	private volatile int mIsFreeTrialFetchProgress = 0;
	private int mSlotsRemaining = 0;

	private String mAccessToken;
	private Models.DeviceSubscriptionData devices = null;
	private SubscriptionService mSubscriptionService = null;
	private DeviceManagerService mDeviceManagerService = null;

	private List<DevicePlanStatus> mDevicePlanList = new ArrayList<DevicePlanStatus>();
	private final Object lock = new Object();
	private PlanAdapter mPlanAdapter = null;
	private Context mContext;
	private ProgressDialog mProgressDialog;
	private FreeTrialSuccessDialog mFreeTrialSuccessDialog;
	private FreeTrialFailureDialog mFreeTrialFailureDialog;

	public static final String FREEMIUM = "Freemium";
	public static final String FREE_TRIAL = "Free trial";
	public static final String ACTIVE = "active";
	public static final String CANCELED = "canceled";
	public static final String PENDING = "pending";
	public static final String EXPIRE = "expired";

	private final String HUBBLE_STORE_URL = "https://www.motorolastore.com";

	private List<String> mFilterRegistrationIdList = new ArrayList<String>();
	private List<String> mAvailableRegistrationIdList = new ArrayList<String>();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getContext();
		mAccessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
		mSubscriptionService = SubscriptionService.getInstance(mContext);
		mDeviceManagerService = DeviceManagerService.getInstance(mContext);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.apply_plan_fragment, null);

		mPlanStoreFooterLayout = (RelativeLayout) view.findViewById(R.id.plan_store_layout);
		mPlanParentLayout = (LinearLayout) view.findViewById(R.id.plan_parent_layout);

		mNoPlanTopLayout = (RelativeLayout) view.findViewById(R.id.no_plan_top_layout);
		mPlanTopLayout = (RelativeLayout) view.findViewById(R.id.plan_top_layout);
		mPlanActiveLayout = (RelativeLayout)view.findViewById(R.id.active_plan_layout);
		mPlanDowngradeLayout = (RelativeLayout)view.findViewById(R.id.downgrade_plan_layout);

		mCurrentPlanLabel = (TextView)view.findViewById(R.id.current_plan_status);
		mCurrentPlanText = (TextView) view.findViewById(R.id.current_plan_text);
		mCurrentPlanDetail = (TextView) view.findViewById(R.id.current_plan_detail);

		mFromPlanText = (TextView)view.findViewById(R.id.downgrade_plan_from_text);
		mTotPlanText = (TextView)view.findViewById(R.id.downgrade_plan_to_text);

		mRenewDateTag = (TextView) view.findViewById(R.id.current_plan_renew);
		mRenewDate = (TextView) view.findViewById(R.id.current_plan_renew_date);

		mEnablePaidPlan = (Button) view.findViewById(R.id.enable_plan_button);
		mChangePlan = (Button) view.findViewById(R.id.change_plan_button);
		mUnsubscribePlan = (Button) view.findViewById(R.id.unsubscribe_button);

		mPlanStoreFooterLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intentPlan = new Intent(Intent.ACTION_VIEW);
				intentPlan.setData(Uri.parse(HUBBLE_STORE_URL));
				mContext.startActivity(intentPlan);
			}
		});

		mEnablePaidPlan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadPlan();
				GeAnalyticsInterface.getInstance().trackEvent(AppEvents.PLAN_TAB,AppEvents.ENABLE_PAID_PLAN_CLICKED,AppEvents.ENABLE_PAID_PLAN_CLICKED);

				ZaiusEvent enablePlanEvt = new ZaiusEvent(AppEvents.ENABLE_PAID_PLAN);
				enablePlanEvt.action(AppEvents.ENABLE_PAID_PLAN_CLICKED);
				try {
					ZaiusEventManager.getInstance().trackCustomEvent(enablePlanEvt);
				} catch (ZaiusException e) {
					e.printStackTrace();
				}
			}
		});

		mChangePlan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadPlan();
				GeAnalyticsInterface.getInstance().trackEvent(AppEvents.PLAN_TAB,AppEvents.CHANGE_PLAN_CLICKED,AppEvents.CHANGE_PLAN_CLICKED);

				ZaiusEvent changePlanEvt = new ZaiusEvent(AppEvents.CHANGE_PLAN);
				changePlanEvt.action(AppEvents.CHANGE_PLAN_CLICKED);
				try {
					ZaiusEventManager.getInstance().trackCustomEvent(changePlanEvt);
				} catch (ZaiusException e) {
					e.printStackTrace();
				}

			}
		});

		mUnsubscribePlan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

                if(CANCELED.equals(mUserPlanStatus)) {
	                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.PLAN_TAB,AppEvents.PLAN_REACTIVATE_CLICKED,AppEvents.PLAN_REACTIVATE_CLICKED);

	                ZaiusEvent reactivateEvt = new ZaiusEvent(AppEvents.PLAN_REACTIVATE);
	                reactivateEvt.action(AppEvents.PLAN_REACTIVATE_CLICKED);
	                try {
		                ZaiusEventManager.getInstance().trackCustomEvent(reactivateEvt);
	                } catch (ZaiusException e) {
		                e.printStackTrace();
	                }
	                updateSubscription(SubscriptionInfo.UpdateSubscriptionType.RE_NEW_PLAN);
                }else {
	                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.PLAN_TAB,AppEvents.PLAN_UNSUBSCRIBE_CLICKED,AppEvents.PLAN_UNSUBSCRIBE_CLICKED);

	                ZaiusEvent unsubscribeEvt = new ZaiusEvent(AppEvents.PLAN_UNSUBSCRIBE);
	                unsubscribeEvt.action(AppEvents.PLAN_UNSUBSCRIBE_CLICKED);
	                try {
		                ZaiusEventManager.getInstance().trackCustomEvent(unsubscribeEvt);
	                } catch (ZaiusException e) {
		                e.printStackTrace();
	                }
	                updateSubscription(SubscriptionInfo.UpdateSubscriptionType.CANCEL_PLAN);
                }
			}
		});
		mCameraListTextView = (TextView) view.findViewById(R.id.camera_text);

		mListView = (ListView) view.findViewById(R.id.plan_device_listView);


		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		mProgressBar.setVisibility(View.VISIBLE);

		mNoDeviceTextView = (TextView) view.findViewById(R.id.no_device_text);
		mNoDeviceTextView.setVisibility(View.INVISIBLE);

		mNoDataLayout = (RelativeLayout) view.findViewById(R.id.no_data_layout);
		mNoDataLayout.setVisibility(View.INVISIBLE);
		mNoDataDetailText = (TextView) view.findViewById(R.id.no_data_text_detail);
		mRetryButton = (Button) view.findViewById(R.id.no_data_retry_plan);

		mRetryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgressBar.setVisibility(View.VISIBLE);
				fetchUserSubscription();
			}
		});

		mPlanAdapter = new PlanAdapter(mContext, this);
		mListView.setAdapter(mPlanAdapter);
		fetchUserSubscription();
		return view;
	}

	private void loadPlan() {
		/*Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(getString(R.string.hubble_web_app_url)));
		startActivity(intent);*/
		if (CommonUtil.isInternetAvailable(mContext)) {
			Intent intent = new Intent(mContext, ManagePlanActivity.class);
			startActivityForResult(intent, MANAGE_PLAN_REQUEST_CODE);
		} else {
			if(mContext != null && getActivity() != null) {
				Toast.makeText(mContext, R.string.enable_internet_connection, Toast.LENGTH_LONG).show();
			}
		}
	}

	private void fetchUserSubscription() {
		if (CommonUtil.isInternetAvailable(mContext)) {
			mNoDataLayout.setVisibility(View.GONE);
			mPlanParentLayout.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
			mNoDeviceTextView.setVisibility(View.INVISIBLE);
			mDevicePlanList.clear();
			isFetchingPlan = true;
			HubbleRequest hubbleRequest = new HubbleRequest(mAccessToken);
			mSubscriptionService.getUserSubscriptionPlan(SubscriptionInfo.ServicePlan.MONITORING_SERVICE_PLAN, hubbleRequest, new Response.Listener<UserSubscriptionPlanResponse>() {
				@Override
				public void onResponse(UserSubscriptionPlanResponse response) {

					if (mContext == null) {
						Log.d(TAG, "PlanFragment is not visible");
						return;
					}else if(response != null && response.getStatus() != 200){
						Log.d(TAG, "Get User Subscriptions failed");
						showNoDataLayout(getString(R.string.error_fetch_plan));
						return;
					}
					if (response == null || response.getPlanResponse() == null || response.getPlanResponse().length == 0 ||
							!checkActiveSubscription(response.getPlanResponse())) {
						Log.d("TAG", "no Plan check freetrial");
						checkAllDeviceFreeTrialStatus();
					} else {
						UserSubscriptionPlanResponse.PlanResponse[] userSubscriptions = response.getPlanResponse();
						mUserPendingPlan = null;
						if (userSubscriptions != null && userSubscriptions.length > 0) {
							for (UserSubscriptionPlanResponse.PlanResponse item : userSubscriptions) {
								String userPlanStatus = item.getPlanState();
								if (ACTIVE.equals(userPlanStatus) || CANCELED.equals(userPlanStatus)) {
									mUserPlanStatus = item.getPlanState();
									mUserPlan = item.getPlanID();
									mSubscriptionUUID = item.getSubUUID();
									Date time = item.getExpireTime();
									if (time != null) {
										SimpleDateFormat df = new SimpleDateFormat("dd MMM, yyyy");
										mExpiryDate = df.format(item.getExpireTime());
									}

									//break;
								}else if(PENDING.equals(userPlanStatus)){
									mUserPendingPlan = item.getPlanID();
								}
							}
						}
						// load device subscriptions
						if (!TextUtils.isEmpty(mUserPlan) && !mUserPlan.equalsIgnoreCase(FREEMIUM)) {
							//SetUp Plan top layout
							if(mUserPendingPlan != null && !mUserPendingPlan.equalsIgnoreCase(FREEMIUM)){
								mUserPlanStatus = PENDING;
							}
							mNoDataLayout.setVisibility(View.INVISIBLE);
							mPlanParentLayout.setVisibility(View.VISIBLE);
							mNoPlanTopLayout.setVisibility(View.GONE);
							mPlanTopLayout.setVisibility(View.VISIBLE);
							mCurrentPlanText.setText(mUserPlan);
							mCurrentPlanDetail.setText(SubscriptionUtil.planTypeDetailMap.get(mUserPlan));
							if (mExpiryDate != null && !mExpiryDate.isEmpty()) {
								mRenewDateTag.setVisibility(View.VISIBLE);
								mRenewDate.setVisibility(View.VISIBLE);
								mRenewDate.setText(mExpiryDate);
							} else {
								mRenewDateTag.setVisibility(View.GONE);
								mRenewDate.setVisibility(View.GONE);
							}
							if (PENDING.equals(mUserPlanStatus)) {
								mCurrentPlanLabel.setVisibility(View.VISIBLE);
								String planLabel = getString(R.string.colon) + "   " + getString(R.string.plan_changed);
								mCurrentPlanLabel.setTextColor(getResources().getColor(R.color.text_blue));
								mCurrentPlanLabel.setText(planLabel);
								mUnsubscribePlan.setText(R.string.unsubscribe);
								mRenewDateTag.setText(R.string.plan_change_renew_text);
								mPlanActiveLayout.setVisibility(View.GONE);
								mPlanDowngradeLayout.setVisibility(View.VISIBLE);
								mFromPlanText.setText(mUserPlan);
								mTotPlanText.setText(mUserPendingPlan);
							} else if (CANCELED.equals(mUserPlanStatus)) {
								mCurrentPlanLabel.setVisibility(View.VISIBLE);
								String planLabel = getString(R.string.colon) + "   " + getString(R.string.plan_cancelled);
								mCurrentPlanLabel.setText(planLabel);
								mUnsubscribePlan.setText(R.string.plan_reactivate);
								mRenewDateTag.setText(R.string.plan_cancel_renew_text);
								mPlanActiveLayout.setVisibility(View.VISIBLE);
								mPlanDowngradeLayout.setVisibility(View.GONE);
							} else {
								mCurrentPlanLabel.setVisibility(View.GONE);
								mUnsubscribePlan.setText(R.string.unsubscribe);
								mRenewDateTag.setText(R.string.plan_renew_date);
								mPlanActiveLayout.setVisibility(View.VISIBLE);
								mPlanDowngradeLayout.setVisibility(View.GONE);
							}
							checkAllDevicePlanStatus();
						} else {
							//TODO : Check this else condition
							Log.d(TAG, "User plan id is empty or freemium");
							checkAllDeviceFreeTrialStatus();
						}
					}
					isFetchingPlan = false;
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					mUserPlanStatus = null;
					mUserPlan = null;
					isFetchingPlan = false;
					if(mContext != null && getActivity() != null) {
						showNoDataLayout(getString(R.string.error_fetch_plan));
					}
				}
			});
		} else {
			showNoDataLayout(getString(R.string.dialog_no_network_enabled));
		}


	}

	private boolean checkActiveSubscription(UserSubscriptionPlanResponse.PlanResponse[] subscriptionPlan) {
		for (UserSubscriptionPlanResponse.PlanResponse item : subscriptionPlan) {
			mUserPlanStatus = item.getPlanState();
			if (ACTIVE.equals(mUserPlanStatus) || PENDING.equals(mUserPlanStatus) || CANCELED.equals(mUserPlanStatus)) {
				return true;
			}
		}
		return false;
	}

	private void checkAllDeviceFreeTrialStatus() {
		mNoDataLayout.setVisibility(View.INVISIBLE);
		mPlanParentLayout.setVisibility(View.VISIBLE);
		mPlanTopLayout.setVisibility(View.GONE);
		mNoPlanTopLayout.setVisibility(View.VISIBLE);
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
				if (apiResponse != null && apiResponse.getData() != null) {
					List<SerializableDeviceProfile> deviceDetailsResponseList = apiResponse.getData();
					//DeviceDetailsResponse[] deviceDetailsResponseList = response.getDeviceDetailsResponse();
					if (deviceDetailsResponseList != null && deviceDetailsResponseList.size() > 0) {
						if (mDevicePlanList == null) {
							mDevicePlanList = new ArrayList<DevicePlanStatus>();
						}
						mDevicePlanList.clear();
						for (SerializableDeviceProfile device : deviceDetailsResponseList) {
							String modelId = device.getRegistrationId().substring(2, 6);
							if ((!modelId.equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
									|| Util.isThisVersionGreaterThan(device.getFirmwareVersion(), PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION))
									&& !modelId.equalsIgnoreCase(PublicDefine.MODEL_ID_SMART_NURSERY)
									&& device.getParentProfile() == null) {
								boolean isFreeTrialActive = device.getDeviceFreeTrial() != null
										&& ACTIVE.equals(device.getDeviceFreeTrial().getStatus());
								boolean isFreeTrialExpired = device.getDeviceFreeTrial() != null
										&& EXPIRE.equals(device.getDeviceFreeTrial().getStatus());
								String planId = device.getPlanId();
								Log.d(TAG, "Plan id is " + planId);
								if (isFreeTrialActive) {
									DevicePlanStatus devicePlanStatus = new DevicePlanStatus(device.getName(), device.getRegistrationId(),
											SubscriptionUtil.PLAN_FREE_TRIAL_APPLIED, FREE_TRIAL);
									int remainingDays = device.getPendingFreeTrialDays();
									if (remainingDays >= 0) {
										devicePlanStatus.setExpiryDate(String.format("%02d", remainingDays));
									}
									mDevicePlanList.add(devicePlanStatus);
								} else if (planId == null || planId.isEmpty() || PublicDefineGlob.FREEMIUM.equals(planId)) {
									if (isFreeTrialExpired || device.getFreeTrialQuota() < 1) {
										Log.d(TAG, "Active free trial is expired for this device");
										DevicePlanStatus devicePlanStatus = new DevicePlanStatus(device.getName(), device.getRegistrationId(),
												SubscriptionUtil.PLAN_FREE_TRIAL_EXPIRED, FREEMIUM);
										mDevicePlanList.add(devicePlanStatus);
									} else if (device.getFreeTrialQuota() > 0) {
										Log.d(TAG, "Active free trial is available for this device");
										DevicePlanStatus devicePlanStatus = new DevicePlanStatus(device.getName(), device.getRegistrationId(),
												SubscriptionUtil.PLAN_FREE_TRIAL_AVAILABLE, FREEMIUM);
										mDevicePlanList.add(devicePlanStatus);
									}
								} else {
									Log.d(TAG, "Device is on some plan");
									DevicePlanStatus devicePlanStatus = new DevicePlanStatus(device.getName(), device.getRegistrationId(),
											SubscriptionUtil.PLAN_ON_SOME_PLAN, planId);
									mDevicePlanList.add(devicePlanStatus);
								}
							}
						}
					}
					updatePlanUI(SubscriptionUtil.NO_PLAN);
				} else {
					updatePlanUI(SubscriptionUtil.NO_PLAN);
				}
			}
		}.execute();
	}


	private void checkAllDevicePlanStatus() {
		List<Device> allDevices= DeviceSingleton.getInstance().getDevices();
		if(allDevices != null && allDevices.size() != 0) {
			for (Device device : allDevices) {
				String modelId = device.getProfile().getModelId();
				if ((modelId.equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT) && !Util.isThisVersionGreaterThan(device.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION))
						|| modelId.equalsIgnoreCase(PublicDefine.MODEL_ID_SMART_NURSERY)
						|| device.getProfile().getParentRegistrationId() != null
						|| device.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)){
					mFilterRegistrationIdList.add(device.getProfile().getRegistrationId());
				} else {
					mAvailableRegistrationIdList.add(device.getProfile().getRegistrationId());
				}
			}
		}
		new AsyncTask<Void, Void, Models.ApiResponse<Models.DeviceSubscriptionData>>() {
			@Override
			protected Models.ApiResponse<Models.DeviceSubscriptionData> doInBackground(Void... params) {
				Models.ApiResponse<Models.DeviceSubscriptionData> dResponse = null;
				try {
					dResponse = Api.getInstance().getService().getDeviceSubscriptions(mAccessToken);
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(TAG, "Load Device Subscriptions failed");
				}
				return dResponse;
			}

			@Override
			protected void onPostExecute(Models.ApiResponse<Models.DeviceSubscriptionData> response) {
				if (response == null || !"200".equals(response.getStatus())) {
					mProgressBar.setVisibility(View.INVISIBLE);
					Log.d(TAG, "Get Device Subscriptions failed");
				} else {
					devices = response.getData();
					if(devices != null && devices.getDevices() != null && devices.getDevices().size() != 0) {
						List<DevicePlanStatus> mAppliedDeviceList = new ArrayList<DevicePlanStatus>();
						List<DevicePlanStatus> mNotAvailableDeviceList = new ArrayList<DevicePlanStatus>();
						int deviceOnPlan = 0;
						for (DeviceSubscription deviceData : devices.getDevices()) {
							if (!mFilterRegistrationIdList.contains(deviceData.getRegistrationId())
									&& deviceData.getPlanId().equalsIgnoreCase(mUserPlan)) {
								deviceOnPlan++;
							}
							mSlotsRemaining = Util.hubbleTierMap.get(mUserPlan) - deviceOnPlan;
							if(mSlotsRemaining < 0){
								mSlotsRemaining = 0;
							}
						}
						mDevicePlanList.clear();
						for (DeviceSubscription deviceData : devices.getDevices()) {
							if (!mFilterRegistrationIdList.contains(deviceData.getRegistrationId()) &&
									mAvailableRegistrationIdList.contains(deviceData.getRegistrationId())){
								if (deviceData.getPlanId().equalsIgnoreCase(mUserPlan)) {
									DevicePlanStatus devicePlanStatus = new DevicePlanStatus(deviceData.getName(), deviceData.getRegistrationId(),
											SubscriptionUtil.PLAN_APPLIED, deviceData.getPlanId());
									mAppliedDeviceList.add(devicePlanStatus);
								} else {
									if (mSlotsRemaining > 0) {
										DevicePlanStatus devicePlanStatus = new DevicePlanStatus(deviceData.getName(),deviceData.getRegistrationId(),
												SubscriptionUtil.PLAN_AVAILABLE, deviceData.getPlanId());
										mNotAvailableDeviceList.add(devicePlanStatus);
									} else {
										DevicePlanStatus devicePlanStatus = new DevicePlanStatus(deviceData.getName(),deviceData.getRegistrationId(),
												SubscriptionUtil.PLAN_MAX_QUOTA_REACHED, deviceData.getPlanId());
										mNotAvailableDeviceList.add(devicePlanStatus);
									}
								}
							}
						}
						mDevicePlanList.addAll(mAppliedDeviceList);
						mDevicePlanList.addAll(mNotAvailableDeviceList);
						Log.d(TAG, "Load all subscriptions successful");
					}
				}
				updatePlanUI(SubscriptionUtil.PLAN_ENABLED);
			}
		}.execute();
	}

	private void updatePlanUI(int planType) {
		if(mContext != null && getActivity() != null) {
			if (!mDevicePlanList.isEmpty()) {
				mNoDeviceTextView.setVisibility(View.INVISIBLE);
				if (planType == SubscriptionUtil.PLAN_ENABLED) {
					int usedSlot = Util.hubbleTierMap.get(mUserPlan) - mSlotsRemaining;
					mCameraListTextView.setText(getString(R.string.cameras_available_slot, usedSlot, Util.hubbleTierMap.get(mUserPlan)));
					mCameraListTextView.setTextColor(mContext.getResources().getColor(R.color.text_blue));
					mCameraListTextView.setVisibility(View.VISIBLE);
					mListView.setDivider(null);
					mListView.setDividerHeight(0);
				} else if (planType == SubscriptionUtil.NO_PLAN) {
					mCameraListTextView.setText(getString(R.string.cameras_available));
					mCameraListTextView.setTextColor(mContext.getResources().getColor(R.color.text_gray));
					mCameraListTextView.setVisibility(View.VISIBLE);
					ColorDrawable divider = new ColorDrawable(this.getResources().getColor(R.color.text_gray));
					mListView.setDivider(divider);
					mListView.setDividerHeight(1);
				}
			} else {
				mNoDeviceTextView.setVisibility(View.VISIBLE);
			}
			mProgressBar.setVisibility(View.INVISIBLE);
			mPlanAdapter.setDeviceList(mDevicePlanList, planType, mUserPlanStatus);
			mPlanAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(int position) {
		String registrationId = mDevicePlanList.get(position).getRegistrationId();
		enableFreeTrial(registrationId, position);

		GeAnalyticsInterface.getInstance().trackEvent(AppEvents.PLAN_TAB,AppEvents.PLAN_ACTIVATE_FREE_TRAIL_CLICKED,AppEvents.PLAN_ACTIVATE_FREE_TRAIL_CLICKED);
		ZaiusEvent freeTrailEvt = new ZaiusEvent(AppEvents.PLAN_ACTIVATE_FREE_TRAIL);
		freeTrailEvt.action(AppEvents.PLAN_ACTIVATE_FREE_TRAIL_CLICKED);
		try {
			ZaiusEventManager.getInstance().trackCustomEvent(freeTrailEvt);
		} catch (ZaiusException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onCheckChange(final int position, boolean isChecked) {
		final String registrationId = mDevicePlanList.get(position).getRegistrationId();
		if (isChecked) {
			mProgressDialog = ProgressDialog.show(mContext, null, getString(R.string.enable_plan));
			//Enable Plan
			new AsyncTask<Void, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(Void... params) {
					int tryCount = 0;
					List<String> listRegistrationId = new ArrayList<String>();
					listRegistrationId.add(registrationId);
					boolean success = false;
					do {
						try {

							tryCount++;
							retrofit.client.Response response = Api.getInstance().getService().applySubscription(mAccessToken,
									new Models.ListDevice(listRegistrationId, mUserPlan));
							success = response != null && 200 == response.getStatus();
							if (success) {
								Log.d(TAG, "Apply subscriptions " + mUserPlan + " successful. Complete task.");
							} else {
								Log.d(TAG, "Apply subscriptions " + mUserPlan + " failed. Try again.");
								Thread.sleep(500);
							}
						} catch (Exception e) {
							success = false;
						}
					} while (!success && tryCount < 5);
					return success;
				}

				@Override
				protected void onPostExecute(Boolean response) {
					loadDataAfterPlanChange(response);
				}
			}.execute();
		} else {
			//DisablePlan
			mProgressDialog = ProgressDialog.show(mContext, null, getString(R.string.disable_plan));
			new AsyncTask<Void, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(Void... params) {
					int tryCount = 0;
					List<String> listRegistrationId = new ArrayList<String>();
					listRegistrationId.add(registrationId);
					boolean success = false;
					do {
						try {
							tryCount++;
							retrofit.client.Response response = Api.getInstance().getService().applySubscription(mAccessToken,
									new Models.ListDevice(listRegistrationId, null));
							success = (response != null && 200 == response.getStatus());
							if (success) {
								Log.d(TAG, "Apply subscriptions null successful. Next step.");
							} else {
								Log.d(TAG, "Apply subscriptions null failed. Try again.");
								Thread.sleep(500);
							}
						} catch (Exception e) {
							success = false;
						}
					} while (!success && tryCount < 5);
					return success;
				}

				@Override
				protected void onPostExecute(Boolean response) {
					loadDataAfterPlanChange(response);
				}
			}.execute();
		}


	}

	private void loadDataAfterPlanChange(boolean success) {
		dismissProgressDialog();
		if(mContext != null && getActivity() != null){
			if (success) {
				checkAllDevicePlanStatus();
			} else {
				Toast.makeText(mContext, R.string.plan_change_fail, Toast.LENGTH_LONG).show();
				mPlanAdapter.notifyDataSetChanged();
			}
		}
	}


	private void enableFreeTrial(final String regId, final int position) {
		if (CommonUtil.isInternetAvailable(mContext)) {
			mProgressDialog = ProgressDialog.show(mContext, null, getString(R.string.enable_free_trial));
			final SubscriptionCommandUtil subscriptionUtil = new SubscriptionCommandUtil(mContext, mAccessToken);
			DeviceManagerService deviceManagerService = DeviceManagerService.getInstance(mContext);
			DeviceID deviceID = new DeviceID(mAccessToken, regId);
			deviceManagerService.enableFreeTrailOnDevice(deviceID, new Response.Listener<DeviceFreeTrialDetails>() {
				@Override
				public void onResponse(DeviceFreeTrialDetails response) {
					dismissProgressDialog();
					if (mContext != null && getActivity() != null) {
						if (response != null && response.getStatus() == 200) {
							//if (mContext != null) {
								mFreeTrialSuccessDialog = new FreeTrialSuccessDialog(mContext);
								mFreeTrialSuccessDialog.show();
							//}
							subscriptionUtil.enableMotionVideoRecording(regId);
							checkAllDeviceFreeTrialStatus();
						} else {
							//subscriptionUtil.disableMotionVideoRecording(regId);
							mFreeTrialFailureDialog = new FreeTrialFailureDialog(mContext);
							mFreeTrialFailureDialog.show();
						}
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					dismissProgressDialog();
					if (mContext != null && getActivity() != null) {
						mFreeTrialFailureDialog = new FreeTrialFailureDialog(mContext);
						mFreeTrialFailureDialog.show();
						//subscriptionUtil.disableMotionVideoRecording(regId);
					}
				}
			});
		} else {
			if (mContext != null && getActivity() != null) {
				Toast.makeText(mContext, R.string.enable_internet_connection, Toast.LENGTH_LONG).show();
			}
		}
	}

	private void dismissProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = null;
	}

	private void showNoDataLayout(String msg) {
		if(mContext != null && getActivity() != null) {
			mNoDataLayout.setVisibility(View.VISIBLE);
			mPlanParentLayout.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.INVISIBLE);
			mNoDataDetailText.setText(msg);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == MANAGE_PLAN_REQUEST_CODE) {
			if (mContext != null && getActivity() != null) {
                fetchUserSubscription();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		dismissProgressDialog();
		mContext = null;
	}

	private void updateSubscription(final SubscriptionInfo.UpdateSubscriptionType updateType) {
		if (CommonUtil.isInternetAvailable(mContext)) {
			if (mSubscriptionUUID != null && !mSubscriptionUUID.isEmpty()) {
				UpdateSubscription updateSubscription = new UpdateSubscription(mAccessToken, mSubscriptionUUID);
				//SubscriptionInfo.UpdateSubscriptionType updateSubscriptionType = SubscriptionInfo.UpdateSubscriptionType.CANCEL_PLAN;
				SubscriptionInfo.UpdateSubscriptionType updateSubscriptionType = updateType;
				String progressMessage = getString(R.string.unsubscribe_plan);
				if(updateType == SubscriptionInfo.UpdateSubscriptionType.RE_NEW_PLAN){
					progressMessage = getString(R.string.reactivate_plan);
				}
				mProgressDialog = ProgressDialog.show(mContext, null, progressMessage);
				mSubscriptionService.updateUserSubscription(updateSubscriptionType, updateSubscription, new Response.Listener<UserPlanResponse>() {
					@Override
					public void onResponse(UserPlanResponse response) {
						dismissProgressDialog();
						if (mContext != null && getActivity() != null) {
							UserPlanResponse.PurchasePlanDetails purchasePlanDetails = response.getPurchasePlanDetails();
							if (purchasePlanDetails.getSubscriptionUUID().equalsIgnoreCase(mSubscriptionUUID)
									&& purchasePlanDetails.getSubscriptionPlan().equalsIgnoreCase(mUserPlan)
									&& purchasePlanDetails.getSubscriptionState().equalsIgnoreCase(CANCELED)) {
								Toast.makeText(mContext, getString(R.string.unsubscribe_plan_success), Toast.LENGTH_SHORT).show();
								fetchUserSubscription();
							} else if(purchasePlanDetails.getSubscriptionUUID().equalsIgnoreCase(mSubscriptionUUID)
									&& purchasePlanDetails.getSubscriptionPlan().equalsIgnoreCase(mUserPlan)
									&& purchasePlanDetails.getSubscriptionState().equalsIgnoreCase(ACTIVE)) {
								Toast.makeText(mContext, getString(R.string.reactivate_plan_success), Toast.LENGTH_SHORT).show();
								fetchUserSubscription();
							}else {
								String failureMessage = getString(R.string.unsubscribe_plan_failure);
								if(updateType == SubscriptionInfo.UpdateSubscriptionType.RE_NEW_PLAN){
									failureMessage = getString(R.string.reactivate_plan_failure);
								}
								Toast.makeText(mContext, failureMessage, Toast.LENGTH_LONG).show();
							}
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						dismissProgressDialog();
						if(mContext != null && getActivity() != null) {
							String failureMessage = getString(R.string.unsubscribe_plan_failure);
							if(updateType == SubscriptionInfo.UpdateSubscriptionType.RE_NEW_PLAN){
								failureMessage = getString(R.string.reactivate_plan_failure);
							}
							Toast.makeText(mContext, failureMessage, Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		} else {
			if(mContext != null && getActivity() != null) {
				Toast.makeText(mContext, R.string.enable_internet_connection, Toast.LENGTH_LONG).show();
			}
		}
	}
}
