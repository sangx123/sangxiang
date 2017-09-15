package com.hubble.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.p2p.P2pDevice;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.framework.service.p2p.P2pUtils;
import com.hubble.notifications.NotificationReceiver;
import com.hubble.ota.OtaActivity;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubble.ui.eventsummary.EventSummaryFragment;
import com.hubble.util.BgMonitorData;
import com.hubble.util.CommonConstants;
import com.hubble.util.P2pSettingUtils;
import com.nxcomm.blinkhd.ui.CameraSettingsActivity;
import com.nxcomm.blinkhd.ui.Global;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.nxcomm.jstun_android.RmcChannel;
import com.util.AppEvents;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.util.ArrayList;
import java.util.List;

import base.hubble.database.DeviceStatusDetail;
import com.hubbleconnected.camera.R;


public class ViewFinderActivity extends AppCompatActivity implements IViewFinderEventListCallBack,IViewFinderCallback{

    private final String TAG = "ViewFinderActivity";

    private final String VIEW_FINDER_FRAGMENT_TAG = "ViewFinderFragment";
    private final String EVENT_HISTORY_FRAGMENT_TAG = "EventHistoryFragment";
	private final String EVENT_SUMMARY_FRAGMENT_TAG = "EventSummaryFragment";

    private ViewFinderFragment mViewFinderFragment;
    private EventHistoryFragment mEventHistoryFragment;
	private EventSummaryFragment mEventSummaryFragment;
    private Device selectedCamera;
    private BroadcastReceiver mRefreshEventsBroadcastReceiver= null;
    private FragmentTransaction fragmentTransaction = null;
	private String mLaunchReason = "";

	private int mSummaryLaunchFrom = -1;

	private static final int FIRMWARE_UPGRADE_REQUEST_CODE = 0x01;

	private Handler mHandler=new Handler();;
	private Runnable mRunnable;
	private boolean mShouldStreamInBg=true,mIsBGStreaming=false;
	private String mRegistrationID;
	private static int VIDEO_STREAM_TIMEOUT=1*60*1000;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_finder);
		Log.d(TAG, "onCreate");
		mRegistrationID=getIntent().getStringExtra("reg_id");
		selectedCamera=DeviceSingleton.getInstance().getDeviceByRegId(mRegistrationID);
        //Fragment initialization
	    mViewFinderFragment=ViewFinderFragment.newInstance(mRegistrationID);
	    mViewFinderFragment.setViewFinderEventListCallBack(this);
	    mViewFinderFragment.setViewFinderCallback(this);

	    mEventHistoryFragment =new  EventHistoryFragment();
		mEventHistoryFragment.setSelectedDevice(mRegistrationID);
	    mEventHistoryFragment.setViewFinderEventListCallBack(this);
        mRefreshEventsBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String notificationCamera = intent.getStringExtra(NotificationReceiver.REFRESH_EVENTS_BROADCAST);
				if(selectedCamera != null && selectedCamera.getProfile().getRegistrationId().equals(notificationCamera)) {
					if(mViewFinderFragment != null && mViewFinderFragment.isVisible()){
						mViewFinderFragment.refreshCount();
					}else if(mEventHistoryFragment != null && mEventHistoryFragment.isVisible()){
						mEventHistoryFragment.newEventArrived();
					}
				}
			}
		};

	    mEventSummaryFragment = new EventSummaryFragment();
		mEventSummaryFragment.setSelectedDevice(mRegistrationID);
		mEventSummaryFragment.setViewFinderEventListCallBack(this);


	    Intent launchIntent = getIntent();
	    if(launchIntent != null){
		    mLaunchReason = launchIntent.getStringExtra(CommonConstants.VIEW_FINDER_LAUNCH_TAG);
		    if(mLaunchReason.equalsIgnoreCase(CommonConstants.VIEW_FINDER_GOTO_EVENT)){
			    switchToEventLogFragment();
		    }else if(mLaunchReason.equalsIgnoreCase(CommonConstants.VIEW_FINDER_GOTO_STREAM)) {
				switchToViewFinderFragment();
		    }else if(mLaunchReason.equalsIgnoreCase(CommonConstants.VIEW_FINDER_GOTO_SUMMARY)){
			    switchToEventSummaryFragment(CommonConstants.LAUNCH_SUMMARY_FROM_MA);
		    }
	    }else{
			switchToViewFinderFragment();
	    }
    }


	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");

		/*
     * 20160226: HOANG: VIC-1454
     * There's a case: go to setup camera screen, then press back to go back to camera list.
     * Sometimes, app restarted app without calling onDestroy of SingleCamConfigureActivity (may be due to low memory).
     * Thus, P2pService is stopped unexpectedly and will not restarted until user exit and reopen app.
     * Solution: check whether P2pService is running on MainActivity start. If not, restart it immediately.
     */
		if (P2pSettingUtils.hasP2pFeature()) {
			// In startP2pService() method, app already checked whether P2pService is running.
			// Build P2P device list
			List<P2pDevice> p2pDevices = new ArrayList<>();
			List<Device> cameraDevices = DeviceSingleton.getInstance().getDevices();
			if (cameraDevices != null) {
				for (Device cameraDevice : cameraDevices) {
					Log.d(TAG,"cameraDevice :- " + cameraDevice.getProfile().getRegistrationId());
					Log.d(TAG," canUseP2p :" + cameraDevice.getProfile().canUseP2p());
					Log.d(TAG," canUseP2pRelay :- " + cameraDevice.getProfile().canUseP2pRelay());

					 boolean isOrbitP2PEnabled =  !cameraDevice.getProfile().isStandBySupported() || HubbleApplication.AppConfig.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);

					if (isOrbitP2PEnabled && cameraDevice.getProfile().canUseP2p() && cameraDevice.getProfile().canUseP2pRelay() &&
							!TextUtils.isEmpty(cameraDevice.getProfile().getRegistrationId()))
					{
						Log.d(TAG,"Added in P2P device ");
						P2pDevice newDevice = new P2pDevice();
						newDevice.setRegistrationId(cameraDevice.getProfile().getRegistrationId());
						newDevice.setFwVersion(cameraDevice.getProfile().getFirmwareVersion());
						newDevice.setMacAddress(cameraDevice.getProfile().getMacAddress());
						newDevice.setModelId(cameraDevice.getProfile().getModelId());

						if (cameraDevice.getProfile().getDeviceLocation() != null)
						{
							newDevice.setLocalIp(cameraDevice.getProfile().getDeviceLocation().getLocalIp());
						}
						if (cameraDevice.getProfile().isStandBySupported())
						{
							DeviceStatusDetail deviceStatusDetail = cameraDevice.getProfile().getDeviceStatusDetail();
							if(deviceStatusDetail != null && deviceStatusDetail.getDeviceStatus() != null)
							{
								String deviceStatus  = deviceStatusDetail.getDeviceStatus();
								if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0)
								{
									newDevice.setAvailable(true);
								}
								else
								{
									newDevice.setAvailable(false);
								}
							}
							else
							{
								newDevice.setAvailable(cameraDevice.getProfile().isAvailable());
							}


						} else {
							newDevice.setAvailable(cameraDevice.getProfile().isAvailable());

						}
						p2pDevices.add(newDevice);
					}
				}
			} else {
				Log.d(TAG, "View finder activity, device list is null");
			}
			P2pUtils.startP2pService(this, Global.getApiKey(getApplicationContext()), p2pDevices);

			// stop p2p timer when activity started
			P2pManager.getInstance().stopP2pTimer();
		}

		IntentFilter intentFilter = new IntentFilter(NotificationReceiver.REFRESH_EVENTS_BROADCAST);
		Log.i(TAG, "Register broadcast REFRESH_EVENTS_BROADCAST");
		registerReceiver(mRefreshEventsBroadcastReceiver, intentFilter);

		Log.d(TAG, "view finder resumed..hence no need to stop streaming");
		mHandler.removeCallbacks(mRunnable);
		mIsBGStreaming=false;
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onPause");
		super.onStart();
		Log.d(TAG, "view finder resumed..hence no need to stop streaming");
		mHandler.removeCallbacks(mRunnable);
		mIsBGStreaming=false;
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();

		/*
		 * 20160229: HOANG: AA-1591
		 * When exit main activity, app need to set keep alive mode for all p2p sessions.
		 */
		if (P2pSettingUtils.hasP2pFeature()) {
			P2pManager.getInstance().setGlobalRmcChannelMode(RmcChannel.RMC_CHANNEL_MODE_KEEP_ALIVE);
			String regId = BgMonitorData.getInstance().getRegistrationId();
			if (BgMonitorData.getInstance().isShouldEnableBgAfterQuitView()) {
				P2pManager.getInstance().switchAllToModeAsyncExcludeDevice(RmcChannel.RMC_CHANNEL_MODE_KEEP_ALIVE, regId);
			} else {
				P2pManager.getInstance().switchAllToModeAsync(RmcChannel.RMC_CHANNEL_MODE_KEEP_ALIVE);
			}
			P2pManager.getInstance().startP2pTimer(
					BgMonitorData.getInstance().isShouldEnableBgAfterQuitView(),
					BgMonitorData.getInstance().getRegistrationId());
		}

		if (mRefreshEventsBroadcastReceiver != null) {
			try {
				unregisterReceiver(mRefreshEventsBroadcastReceiver);
			} catch (IllegalArgumentException ignored) {
			}
		}
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		if (mViewFinderFragment != null && selectedCamera!=null) {
			if (!mViewFinderFragment.canBackgroundMonitoring() && mShouldStreamInBg && !selectedCamera.getProfile().isStandBySupported() && !mViewFinderFragment.isStreamingTimedOut()) {
				keepStreamingInBg();
			} else if(mViewFinderFragment.isVisible() && mViewFinderFragment.canBackgroundMonitoring()) {
				mViewFinderFragment.setIsInBGMonitoring(true);
			}else{
				mViewFinderFragment.stopFFMPegPlayer();
			}

		}

	}

	@Override
	public void onDestroy(){
		Log.d(TAG, "onDestroy");
		super.onDestroy();

	}

    @Override
    public void onBackPressed()
    {
		Log.d(TAG, "onBackPressed");
	    if (mViewFinderFragment != null && mViewFinderFragment.isVisible()) {
			mViewFinderFragment.stopFFMPegPlayer();
			mShouldStreamInBg=false;
	    } else if (mEventHistoryFragment != null && mEventHistoryFragment.isVisible() &&
			    mLaunchReason.equalsIgnoreCase(CommonConstants.VIEW_FINDER_GOTO_STREAM)) {
		    switchToViewFinderFragment();
			return;
	    } else if(mEventSummaryFragment != null && mEventSummaryFragment.isVisible()){
            if(mSummaryLaunchFrom == CommonConstants.LAUNCH_SUMMARY_FROM_EH){
	            switchToEventLogFragment();
	            mSummaryLaunchFrom = -1;
	            return;
            } else if(mSummaryLaunchFrom == CommonConstants.LAUNCH_SUMMARY_FROM_VF) {
	            switchToViewFinderFragment();
	            mSummaryLaunchFrom = -1;
	            return;
            }
	    }
		super.onBackPressed();

    }

	@Override
	public void onClick(int type) {
		switch (type) {
			case CommonConstants.CLICK_TYPE_VIEW_FINDER:
				switchToViewFinderFragment();
				break;
			case CommonConstants.CLICK_TYPE_EVENT_HISTORY:
				if(mLaunchReason.equalsIgnoreCase(CommonConstants.VIEW_FINDER_GOTO_STREAM)){
					//mViewFinderFragment.onBackPressed();
					//getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(VIEW_FINDER_FRAGMENT_TAG)).commit();
				}
				switchToEventLogFragment();
				break;
			case CommonConstants.LAUNCH_SUMMARY_FROM_VF:
				mSummaryLaunchFrom = CommonConstants.LAUNCH_SUMMARY_FROM_VF;
				switchToEventSummaryFragment(CommonConstants.LAUNCH_SUMMARY_FROM_VF);
				break;
			case CommonConstants.LAUNCH_SUMMARY_FROM_EH:
				mSummaryLaunchFrom = CommonConstants.LAUNCH_SUMMARY_FROM_EH;
				switchToEventSummaryFragment(CommonConstants.LAUNCH_SUMMARY_FROM_EH);
				break;
			default:
				break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PublicDefine.RESULT_SHARE_SNAPSHOT)
		{
		}
		else if(requestCode == FIRMWARE_UPGRADE_REQUEST_CODE)
		{
			if(resultCode ==  Activity.RESULT_OK){
				mViewFinderFragment.checkFirmwareVersion();
			}
		}
	}

	public void startOtaActivity(Device device, CheckFirmwareUpdateResult checkFirmwareUpdateResult)
	{
		Intent intent=new Intent(this, OtaActivity.class);

		Bundle bundle = new Bundle();
		bundle.putBoolean(OtaActivity.IS_FROM_SETUP,false);
		bundle.putString(OtaActivity.DEVICE_MODEL_ID,device.getProfile().getModelId());
		bundle.putSerializable(OtaActivity.CHECK_FIRMWARE_UPGRADE_RESULT,checkFirmwareUpdateResult);

		intent.putExtras(bundle);
		startActivityForResult(intent,FIRMWARE_UPGRADE_REQUEST_CODE);
	}

	@Override
	public void onCameraChanged(Device selectedCamera) {
		this.selectedCamera=selectedCamera;
		mRegistrationID=selectedCamera.getProfile().registrationId;
		DeviceSingleton.getInstance().setSelectedDevice(selectedCamera);
		mViewFinderFragment.stopFFMPegPlayer();
		mViewFinderFragment.setCamera(mRegistrationID);
		mEventHistoryFragment.setSelectedDevice(mRegistrationID);
		mEventHistoryFragment.clearAdapter();
		mEventSummaryFragment.setSelectedDevice(mRegistrationID);
		//mEventSummaryFragment.clearAdapter(); TODO: Enable
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(VIEW_FINDER_FRAGMENT_TAG);
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.detach(fragment);
		ft.attach(fragment);
		ft.commit();

	}

	@Override
	public String getLaunchReason() {
		return mLaunchReason;
	}

	@Override
	public void launchCameraSettings() {
		switchToCameraSettingsActivity();
	}

	@Override
	public boolean isStreamingViaLocal() {
		boolean isInLocal = false;
		if (mViewFinderFragment != null) {
			isInLocal = mViewFinderFragment.isLocalStreaming();
		}
		return isInLocal;
	}

	private void switchToViewFinderFragment(){
		FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
		if(!mViewFinderFragment.isAdded())
			ft.add(R.id.fragment_container, mViewFinderFragment, VIEW_FINDER_FRAGMENT_TAG);
		if(mEventHistoryFragment.isAdded()){
			ft.remove(mEventHistoryFragment);
			//unmute the audio if not already muted in shared pref
			mViewFinderFragment.checkAndMuteAudio(false);
		}
		if(mEventSummaryFragment.isAdded()){
			ft.remove(mEventSummaryFragment);
			//unmute the audio if not already muted in shared pref
			mViewFinderFragment.checkAndMuteAudio(false);
		}
		ft.show(mViewFinderFragment);
		ft.commit();
		mShouldStreamInBg=true;
	}

	private void switchToEventLogFragment() {
		//mute the audio before showing another fragment
		mEventHistoryFragment.setSelectedDevice(mRegistrationID);
		FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
		if(mViewFinderFragment!=null && mViewFinderFragment.isVisible()) {
			mViewFinderFragment.checkAndMuteAudio(true);
			ft.hide(mViewFinderFragment);
		}
		if(mEventSummaryFragment!=null && mEventSummaryFragment.isVisible()){
			ft.remove(mEventSummaryFragment);
		}
		ft.add(R.id.fragment_container, mEventHistoryFragment, EVENT_HISTORY_FRAGMENT_TAG);
		ft.show(mEventHistoryFragment);
		ft.commit();
		GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_EVENT_HISTORY_CLICKED,AppEvents.EVENT_HISTORY_CLICKED);

		ZaiusEvent eventHistoryEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
		eventHistoryEvt.action(AppEvents.VF_EVENT_HISTORY_CLICKED);
		try {
			ZaiusEventManager.getInstance().trackCustomEvent(eventHistoryEvt);
		} catch (ZaiusException e) {
			e.printStackTrace();
		}
	}

	public void switchToCameraSettingsActivity(){
		keepStreamingInBg();
		Intent cameraSettingsIntent = new Intent(ViewFinderActivity.this,CameraSettingsActivity.class);
		startActivity(cameraSettingsIntent);
	}

	private void switchToEventSummaryFragment(int fromScreen) {
		if (selectedCamera != null) {
			mEventSummaryFragment.setSelectedDevice(mRegistrationID);
			FragmentTransaction ft=getSupportFragmentManager().beginTransaction();

			if (fromScreen == CommonConstants.LAUNCH_SUMMARY_FROM_VF) {
				//mute the audio before showing another fragment
				mViewFinderFragment.checkAndMuteAudio(true);
				ft.hide(mViewFinderFragment);

			} else if (fromScreen == CommonConstants.LAUNCH_SUMMARY_FROM_EH) {
				ft.remove(getSupportFragmentManager().
						findFragmentByTag(EVENT_HISTORY_FRAGMENT_TAG));
			}

			ft.add(R.id.fragment_container, mEventSummaryFragment, EVENT_SUMMARY_FRAGMENT_TAG);
			ft.show(mEventSummaryFragment);
			ft.commit();

		} else {
			Toast.makeText(this, "Empty camera", Toast.LENGTH_LONG).show();
		}
	}

	private void keepStreamingInBg(){
		Log.d(TAG, "keepStreamingInBg");
		if(mIsBGStreaming)
			return;
		if(mViewFinderFragment!=null) {
			mViewFinderFragment.setIsInBGMonitoring(true);
			mViewFinderFragment.checkAndMuteAudio(true);
			mIsBGStreaming=true;
			mRunnable=new Runnable() {
				@Override
				public void run() {
					if(mViewFinderFragment!=null){
						Log.d(TAG, "Stop streaming after timeout");
						mViewFinderFragment.stopFFMPegPlayer();
						mIsBGStreaming=false;
					}
				}
			};

			mHandler.postDelayed(mRunnable,VIDEO_STREAM_TIMEOUT);
		}

	}


}
