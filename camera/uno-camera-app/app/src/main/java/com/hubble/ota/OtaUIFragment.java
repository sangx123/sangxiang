package com.hubble.ota;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceID;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceDetail;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceDetailsResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceWakeupResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.helpers.AsyncPackage;
import com.hubble.registration.JDownloader;
import com.hubble.registration.JUploader;
import com.hubble.registration.JWebClient;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubble.registration.tasks.CheckFirmwareUpdateTask;
import com.hubble.util.CommonConstants;
import com.koushikdutta.async.future.FutureCallback;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.DeviceWakeup;
import com.util.SettingsPrefUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.devices.SerializableDeviceProfile;
import base.hubble.meapi.Device;
import base.hubble.meapi.PublicDefines;
import base.hubble.meapi.device.GetCameraInfoResponse;

import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

public class OtaUIFragment extends Fragment
{
	private static final String TAG = OtaUIFragment.class.getSimpleName();

	private static final String IS_FROM_SETUP = "isFromSetup";
	private static final String CHECK_FIRMWARE_UPGRADE_RESULT = "checkfwupgraderesult";
	private static final String DEVICE_MODEL_ID = "deviceModelID";

	private static final long TOTAL_FLASH_TIME_WAIT = 240 * 1000;
	private static final long FLASH_PROCESS_WAIT_TIME = 120 * 1000;
	private static final long TEN_MINUTES = 60 * 1000 * 10;
	private static final long WAKEUP_WAIT_TIME = 25 * 1000;

	private boolean isFromSetup = false;
	private CheckFirmwareUpdateResult mCheckFirmwareUpdateResult;
	private String mDeviceModelID;

	private RelativeLayout mRootRelativeLayout;
	private TextView mPlsWaitTv,mOtaStatusTv,mOtaLiveStatusTv,mOtaPercentageTv;
	private ProgressBar mOtaProgressBar;
	private Button mButton;

	private Context mContext;
	private JDownloader mDownloader;
	private JUploader mJUploader;
	private AlertDialog mAlertDialog;
	private ProgressDialog mBatteryStatusDialog;

	private boolean isStarted = false;
	private boolean isAnyErrorOccured = false;
	private enum Status {
		INITIAL_STAGE,DOWNLOADING_NEW_FIRMWARE_FROM_OTA_SERVER, WAKEUP_DEVICE,UPLOADING_FIRMWARE_TO_DEVICE, CAMERA_IS_UPGRADING,CAMREA_UPDATE_SUCCEEDED, CAMREA_UPDATE_FAILED, TIME_OUT, WAITING_RECOVERY
	}
	private Status mStatus = Status.INITIAL_STAGE;

	private static final int UPGRADE_DONE_TIMEOUT  = 0x01;
	private static final long UPGRADE_TIMEOUT_DELAY = 140 * 1000;


	public static OtaUIFragment newInstance(boolean isFromSetup,String modelID,CheckFirmwareUpdateResult checkFirmwareUpdateResult)
	{
		OtaUIFragment fragment = new OtaUIFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(IS_FROM_SETUP,isFromSetup);
		bundle.putSerializable(CHECK_FIRMWARE_UPGRADE_RESULT,checkFirmwareUpdateResult);
		bundle.putString(DEVICE_MODEL_ID,modelID);
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		mContext = context;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mContext = activity;
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		mContext = null;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.ota_work_flow, container, false);

		Bundle bundle = getArguments();

		if (bundle != null)
		{
			isFromSetup = (boolean)bundle.getBoolean(IS_FROM_SETUP);
			mCheckFirmwareUpdateResult = (CheckFirmwareUpdateResult)bundle.getSerializable(CHECK_FIRMWARE_UPGRADE_RESULT);
			mDeviceModelID = (String)bundle.getString(DEVICE_MODEL_ID,null);
		}

		initializeView(view);

		return view;
	}


	private void initializeView(View view)
	{
		mRootRelativeLayout = (RelativeLayout)view.findViewById(R.id.main_root);

		mPlsWaitTv = (TextView)view.findViewById(R.id.please_wait_tv);
		mOtaStatusTv = (TextView)view.findViewById(R.id.ota_status_tv);
		mOtaLiveStatusTv = (TextView)view.findViewById(R.id.ota_live_status_tv);
		mOtaPercentageTv = (TextView)view.findViewById(R.id.ota_percentage_tv);

		mOtaProgressBar = (ProgressBar)view.findViewById(R.id.ota_process_pb);
		mOtaProgressBar.setMax(100);
		mButton = (Button)view.findViewById(R.id.cancel_button);

		if(isFromSetup)
		{
			mRootRelativeLayout.setBackgroundColor(getResources().getColor(R.color.white));
			mPlsWaitTv.setTextColor(getResources().getColor(R.color.textColor));
			mOtaStatusTv.setTextColor(getResources().getColor(R.color.gray_2));
			mOtaLiveStatusTv.setTextColor(getResources().getColor(R.color.textColor));
			mOtaPercentageTv.setTextColor(getResources().getColor(R.color.textColor));

			mButton.setBackground(getResources().getDrawable(R.drawable.button_bg_setup_ota));
			mButton.setTextColor(getResources().getColor(R.color.textColor));

			mOtaProgressBar.getProgressDrawable().setColorFilter(Color.parseColor("#00acf7"), android.graphics.PorterDuff.Mode.SRC_IN);
		}
		else
		{
			mRootRelativeLayout.setBackgroundColor(getResources().getColor(R.color.viewfinder_marine_bg));
			mPlsWaitTv.setTextColor(getResources().getColor(R.color.textColor));
			mOtaStatusTv.setTextColor(getResources().getColor(R.color.white));
			mOtaLiveStatusTv.setTextColor(getResources().getColor(R.color.white));
			mOtaPercentageTv.setTextColor(getResources().getColor(R.color.white));

			mButton.setBackground(getResources().getDrawable(R.drawable.button_bg_transparent));
			mButton.setTextColor(getResources().getColor(R.color.white));

			mOtaProgressBar.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);

		}

		mButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick (View v)
			{
				onBackPressed();
			}
		});
	}

	public void onBackPressed()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

		builder.setMessage(getResources().getString(R.string.cancel_confimration));
		builder.setTitle(getResources().getString(R.string.firmware_upgrade));

		builder.setPositiveButton(getResources().getString(R.string.OK),
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface arg0, int arg1)
					{
						switch(getStatus())
						{
							case DOWNLOADING_NEW_FIRMWARE_FROM_OTA_SERVER:
								if(mDownloader != null)
								{
									GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_FW_UPGRADE,AppEvents.CANCEL,AppEvents.FW_UPGRADE_CANCEL);
									mDownloader.cancel();
								}
								break;

							case UPLOADING_FIRMWARE_TO_DEVICE:
								if(mJUploader != null)
								{
								}
								break;
						}
						((OtaActivity)mContext).onfinish(Activity.RESULT_CANCELED);
					}
				});

		builder.setNegativeButton(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.cancel();
					}
				});

		if(mAlertDialog != null && mAlertDialog.isShowing())
			mAlertDialog.cancel();
		mAlertDialog = builder.create();
		mAlertDialog.setCancelable(false);
		mAlertDialog.show();
		Button nbutton = mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		nbutton.setTextColor(getResources().getColor(R.color.text_blue));
		Button pbutton = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		pbutton.setTextColor(getResources().getColor(R.color.text_blue));
	}

	@Override
	public void onStart()
	{
		super.onStart();
		startOtaProgress();
	}

	private void startOtaProgress()
	{
		if (!isStarted)
		{
			isStarted = true;
			try
			{
				if(mCheckFirmwareUpdateResult.isDeviceOTA())
				{
					checkDeviceStatus();
				}
				else if (mCheckFirmwareUpdateResult.isLocalCamera())
				{
					String md5 = mCheckFirmwareUpdateResult.getNewFirmwareMD5();
					if (md5 != null && isFirmwareBinaryCached(mDeviceModelID, md5))
					{
						if(mDeviceModelID != null && mDeviceModelID.compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)==0)
						{
							checkDeviceStatus();
						}
						else {
							pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(), getFirmwareBinaryCachedPath(buildFirmwareName(mDeviceModelID, md5)), mCheckFirmwareUpdateResult.getNewFirmwareFileName());
						}
					}
					else
					{
						downloadFirmware(mCheckFirmwareUpdateResult.getFirmwareDownloadLink());

					}
				}
				else {
					setStatus(Status.CAMREA_UPDATE_FAILED);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				setStatus(Status.CAMREA_UPDATE_FAILED);
			}
		}
	}

	private boolean isFirmwareBinaryCached(String cameraModel, String fwVersion)
	{
		String fwDirectory = Util.getFirmwareDirectory();
		String fwFileName = buildFirmwareName(cameraModel, fwVersion);

		if(BuildConfig.DEBUG)
			Log.d(TAG,"fwFileName :- " +fwFileName + " and directory :- " + fwDirectory);


		File fwFile = new File(fwDirectory + File.separator + fwFileName);

		if (fwFile.exists())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean isFirmwareBinaryCachedMtag(String fileName)
	{
		if (mContext != null)
		{
			File fwFile = new File(mContext.getExternalCacheDir().getAbsolutePath() + File.separator + fileName);
			if (fwFile.exists())
			{
				return true;
			}
			else {
				return false;
			}
		}
		return false;
	}

	private String buildFirmwareName(String cameraModel, String fwVersion)
	{
		String checkedSuffix = null;
		if (cameraModel.equalsIgnoreCase(com.discovery.ScanProfile.MODEL_ID_FOCUS86) || cameraModel.equals("0877")) {
			checkedSuffix = String.format(CheckFirmwareUpdateTask.FIRMWARE_TAR, cameraModel, fwVersion);
		}
		else if(cameraModel.equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)) {
			checkedSuffix = String.format(CheckFirmwareUpdateTask.FIRMWARE_FW_PKG, cameraModel, fwVersion);
		}
		else {
			if (cameraModel.equals(CheckFirmwareUpdateTask.MTAG_MODEL)) {
				checkedSuffix = String.format(CheckFirmwareUpdateTask.FIRMWARE_ZIP, cameraModel, fwVersion);
			} else {
				checkedSuffix = String.format(CheckFirmwareUpdateTask.FIRMWARE_TAR_GZ, cameraModel, fwVersion);
			}
		}

		if(checkedSuffix == null)
		{
			String result = "%s-%s.tar.gz";
			return String.format(result, cameraModel, fwVersion);
		}
		else
		{
			return checkedSuffix;
		}
	}

	private String getFirmwareBinaryCachedPath(String firmwareVersion) {
		if(mContext != null)
		{
			File firmwareBinaryFile = new File(Util.getFirmwareDirectory(), firmwareVersion);

			if(BuildConfig.DEBUG)
				Log.d(TAG,"Cache directory path:- " + firmwareBinaryFile.getAbsolutePath());

			return firmwareBinaryFile.getAbsolutePath();
		}
		return "";
	}

	private void upgradeDevice()
	{
		setStatus(Status.UPLOADING_FIRMWARE_TO_DEVICE);

		SendCommand sendCommand = new SendCommand(mCheckFirmwareUpdateResult.getApiKey(),mCheckFirmwareUpdateResult.getRegID(),mCheckFirmwareUpdateResult.getRequestFWUpgradeCommand());

		DeviceManager.getInstance(getActivity()).sendCommandRequest(sendCommand,
				new Response.Listener<SendCommandDetails>()
				{
					@Override
					public void onResponse (SendCommandDetails response)
					{
						if(response != null && response.getDeviceCommandResponse() != null)
						{
							String message = response.getDeviceCommandResponse().getBody();

							if(BuildConfig.DEBUG)
								Log.d(TAG,"response message :- " + message);

							if(message != null && message.contains(CheckFirmwareUpdateResult.REQUEST_FW_URL_COMMAND_RESPONSE))
							{
								String responseValue = message.replace(CheckFirmwareUpdateResult.REQUEST_FW_URL_COMMAND_RESPONSE,"");
								try
								{
									if(BuildConfig.DEBUG)
										Log.d(TAG,"response value :- " + Integer.parseInt(responseValue));

									if (responseValue != null && Integer.parseInt(responseValue) == 0)
									{
										setStatus(Status.CAMERA_IS_UPGRADING);
										waitForUpgradeComplete();
									}
									else {
										setStatus(Status.CAMREA_UPDATE_FAILED);
									}
								}
								catch(Exception e)
								{
									setStatus(Status.CAMREA_UPDATE_FAILED);
									Log.e(TAG,e.getMessage());
								}
							}
						}
					}
				},
				new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse (VolleyError error)
					{
						if(error != null) {
							error.printStackTrace();
						}
						setStatus(Status.CAMREA_UPDATE_FAILED);
					}
				}
		);

	}

	private void downloadFirmware(String url) throws MalformedURLException
	{
		setStatus(Status.DOWNLOADING_NEW_FIRMWARE_FROM_OTA_SERVER);

		URL firmwareLink = new URL(url);

		String fileName =  buildFirmwareName(PublicDefine.getModelIdFromRegId(mCheckFirmwareUpdateResult.getRegID()),mCheckFirmwareUpdateResult.getNewFirmwareMD5());

		if (fileName == null)
		{
			fileName = getResources().getString(R.string.tmp_file_name);
		}

		if (mDeviceModelID.equals(CheckFirmwareUpdateTask.MTAG_MODEL))
		{
			fileName = mCheckFirmwareUpdateResult.getNewFirmwareFileName();
		}


		mDownloader = new JDownloader(firmwareLink, Util.getFirmwareDirectory(), fileName);

		mDownloader.addObserver(new Observer()
		{
			@Override
			public void update(Observable arg0, Object arg1)
			{
				Runnable updateUIRunnable = null;

				if (mDownloader.getStatus() == JDownloader.DOWNLOADING)
				{
					updateUIRunnable = new Runnable()
					{
						@Override
						public void run()
						{
							if (mOtaProgressBar != null) {
								mOtaProgressBar.setProgress((int) mDownloader.getProgress());
							}

							if (mOtaPercentageTv != null) {
								String msg = String.format(getString(R.string.percentage_status),
										Integer.valueOf((int) mDownloader.getProgress())) + "%";
								mOtaPercentageTv.setText(msg);
							}
						}
					};
				}
				else if (mDownloader.getStatus() == JDownloader.ERROR)
				{
					updateUIRunnable = new Runnable()
					{
						@Override
						public void run()
						{
							if (mOtaPercentageTv != null)
							{
								mOtaPercentageTv.setText(getString(R.string.download_firmware_error));

								isAnyErrorOccured = true;
							}
							restartOtaProcess();
							Util.deleteFile(getFirmwareBinaryCachedPath(buildFirmwareName(PublicDefines.getModelIDFromRegID(mCheckFirmwareUpdateResult.getRegID()), mCheckFirmwareUpdateResult.getNewFirmwareMD5())));
						}
					};
				}
				else if (mDownloader.getStatus() == JDownloader.COMPLETE)
				{
					if(PublicDefines.getModelIDFromRegID(mCheckFirmwareUpdateResult.getRegID()).compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)==0)
					{
						checkDeviceStatus();
					}
					else
					{
						updateUIRunnable = new Runnable() {
							@Override
							public void run () {
								pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(),
										mDownloader.getSavedFilePath(), mCheckFirmwareUpdateResult.getNewFirmwareFileName());
								if (mOtaProgressBar != null) {
									mOtaProgressBar.setProgress(0);
								}
							}
						};
					}

				}

				if (updateUIRunnable != null) {
					if(mContext != null) {
						((Activity)mContext).runOnUiThread(updateUIRunnable);
					}
				}
			}
		});
	}

	private void checkDeviceStatus()
	{
		Runnable updateUIRunnable = null;
		updateUIRunnable = new Runnable() {
			@Override
			public void run () {
				setStatus(Status.WAKEUP_DEVICE);
			}
		};

		if (updateUIRunnable != null && mContext != null) {
			((Activity)mContext).runOnUiThread(updateUIRunnable);
		}

		DeviceID deviceID = new DeviceID(mCheckFirmwareUpdateResult.getApiKey(), mCheckFirmwareUpdateResult.getRegID());

		DeviceManager.getInstance(getActivity()).getDeviceDetailsRequest(deviceID, new Response.Listener<DeviceDetail>()
				{
					@Override
					public void onResponse (DeviceDetail response)
					{
						boolean isAnyError = true;
						DeviceDetailsResponse deviceDetailsResponse = response.getDeviceDetailsResponse();


						if(deviceDetailsResponse != null)
						{
							if(deviceDetailsResponse.getFirmwareVersion() != null &&
									Util.isThisVersionGreaterThan(mCheckFirmwareUpdateResult.getOTAVersion(),
											deviceDetailsResponse.getFirmwareVersion()))
							{
								StatusDetails.StatusResponse statusResponse = deviceDetailsResponse.getDeviceStatus();
								if(statusResponse != null)
								{
									StatusDetails.DeviceStatusResponse deviceStatusResponse = statusResponse.getDeviceStatusResponse();
									if(deviceStatusResponse != null)
									{
										String deviceStatus = deviceStatusResponse.getDeviceStatus();
										if(deviceStatus != null)
										{
											if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE)==0)
											{
												isAnyError = false;
												wakeupDevice(false);
											}
											else if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_STANDBY)==0)
											{
												isAnyError = false;
												wakeupDevice(true);
											}

										}
									}
								}
								else
								{
									if(deviceDetailsResponse.isAvailable())
									{
										isAnyError = false;
										wakeupDevice(false);
									}
								}
							}
							else
							{
								isAnyError = false;
								setStatus(Status.CAMREA_UPDATE_SUCCEEDED);
							}

						}

						if(isAnyError)
						{
							setStatus(Status.CAMREA_UPDATE_FAILED);
						}
					}
				},
				new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse (VolleyError error)
					{

						if(error != null) {
							error.printStackTrace();
						}
						setStatus(Status.CAMREA_UPDATE_FAILED);
					}
				});

	}

	private void wakeupDevice(boolean wakeupDevice)
	{
		if(wakeupDevice && mContext != null)
		{
			DeviceWakeup deviceWakeup = DeviceWakeup.newInstance();
			deviceWakeup.wakeupDevice(mCheckFirmwareUpdateResult.getRegID(),mCheckFirmwareUpdateResult.getApiKey(),mDeviceHandler);

		}
		else {

			if(mCheckFirmwareUpdateResult.isDeviceOTA())
			{
				upgradeDevice();
			}
			else
			{
				String md5 = mCheckFirmwareUpdateResult.getNewFirmwareMD5();
				if(mDownloader == null && md5 != null && isFirmwareBinaryCached(mDeviceModelID, md5))
				{

					pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(), getFirmwareBinaryCachedPath(buildFirmwareName(mDeviceModelID, md5)), mCheckFirmwareUpdateResult.getNewFirmwareFileName());
				}
				else
				{
					pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(),
							mDownloader.getSavedFilePath(), mCheckFirmwareUpdateResult.getNewFirmwareFileName());
				}


				if (mOtaProgressBar != null) {
					mOtaProgressBar.setProgress(0);
				}
			}

		}
	}

	private Handler mDeviceHandler = new Handler()
	{
		public void handleMessage (Message msg)
		{
			switch (msg.what)
			{
				case CommonConstants.DEVICE_WAKEUP_STATUS:

					boolean result = (boolean) msg.obj;
					Log.d(TAG, "Device status task completed..device status:" + result);

					if (result)
					{
						if(mCheckFirmwareUpdateResult.isDeviceOTA())
						{
							upgradeDevice();
						}
						else {
							pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(),
									mDownloader.getSavedFilePath(), mCheckFirmwareUpdateResult.getNewFirmwareFileName());
						}
					}
					else
					{
						setStatus(Status.CAMREA_UPDATE_FAILED);
						Log.d(TAG, "wakeup device:failure");
					}

					break;
			}
		}
	};

	private void pushFirmwareToCamera(final String uploadURL, final String filePath, final String fileName)
	{
		final FutureCallback<Boolean> callback = new FutureCallback<Boolean>()
		{
			@Override
			public void onCompleted(Exception e, Boolean result)
			{
				if (getActivity() == null)
				{
					return;
				}
				if (e != null)
				{
					Log.e(TAG, "Upload signature error");
					getActivity().runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							restartOtaProcess();
						}
					});
				}
				else
				{
					if (result)
					{
						getActivity().runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								Log.d(TAG, "Upload signature ok, upload firmware now");
								_pushFirmwareToCamera(uploadURL, filePath, fileName);
							}
						});
					}
					else
					{
						getActivity().runOnUiThread(new Runnable()
						{
							@Override
							public void run() {
								restartOtaProcess();
							}
						});
					}
				}
			}
		};

		if(isFromSetup && mDeviceModelID != null && mDeviceModelID.equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT))
		{
			getActivity().runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					mBatteryStatusDialog = new ProgressDialog(getActivity());
					mBatteryStatusDialog.setIndeterminate(false);
					mBatteryStatusDialog.setMessage(getResources().getString(R.string.check_battery_level));
					mBatteryStatusDialog.show();

				}
			});

			AsyncPackage.doInBackground(new Runnable()
			{
				@Override
				public void run()
				{
					String localIP = mCheckFirmwareUpdateResult.getInetAddress();
					try {
						GetCameraInfoResponse camResponse = Device.getCameraInfo(mCheckFirmwareUpdateResult.getApiKey(), mCheckFirmwareUpdateResult.getRegID());
						if(camResponse != null && camResponse.getCameraInfo() != null && camResponse.getCameraInfo().getDevice_location() != null && camResponse.getCameraInfo().getDevice_location().getLocalIP() != null)
						{
							// change local ip based on server's response
							localIP = camResponse.getCameraInfo().getDevice_location().getLocalIP();
							mCheckFirmwareUpdateResult.setInetAddress(localIP);
						}
					}
					catch(Exception e)
					{

					}

					String deviceMode = String.format(Locale.US,PublicDefine.HTTP_GET_LOCAL_COMMAND,localIP,PublicDefine.GET_DEVICE_MODE);

					String deviceModeResult = JWebClient.downloadAsStringWithoutEx(deviceMode);
					boolean isFirmwareUpgradeAllow = false;

					if(deviceModeResult != null)
					{
						deviceModeResult = deviceModeResult.replaceAll(PublicDefine.GET_DEVICE_MODE,"").replaceAll(":","").trim();
						if(!TextUtils.isEmpty(deviceModeResult))
						{
							int deviceModeStatus = Integer.parseInt(deviceModeResult);
							if(deviceModeStatus ==  PublicDefine.ORBIT_BATTERY_CHARGING)
							{
								isFirmwareUpgradeAllow = true;
							}
							else
							{
								String batteryLevel = String.format(Locale.US,PublicDefine.HTTP_GET_LOCAL_COMMAND,localIP,PublicDefine.GET_BATTERY_VALUE);
								String batteryLevelResult = JWebClient.downloadAsStringWithoutEx(batteryLevel);
								if(batteryLevelResult != null)
								{
									batteryLevelResult = batteryLevelResult.replaceAll(PublicDefine.GET_BATTERY_VALUE,"").replaceAll(":","").trim();

									if(!TextUtils.isEmpty(batteryLevelResult))
									{
										int batteryStatus = Integer.parseInt(batteryLevelResult);

										if (batteryStatus > PublicDefine.ORBIT_MINIMUM_BATTERY_LEVEL) {
											isFirmwareUpgradeAllow = true;
										}
									}
								}
							}

						}
					}
					if(isFirmwareUpgradeAllow)
					{
						getActivity().runOnUiThread(new Runnable()
						{
							@Override
							public void run() {
								if(mBatteryStatusDialog != null)
									mBatteryStatusDialog.dismiss();
							}
						});

						pushSignatureToFirmware(mCheckFirmwareUpdateResult.getUploadFwURL(), mCheckFirmwareUpdateResult.getSignatureData(), mCheckFirmwareUpdateResult.getSignatureFileName(), callback);
					}
					else
					{
						getActivity().runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								if(mBatteryStatusDialog != null)
									mBatteryStatusDialog.dismiss();

								AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

								builder.setMessage(getResources().getString(R.string.connect_power_source));
								builder.setTitle(getResources().getString(R.string.firmware_upgrade));
								builder.setCancelable(false);

								builder.setPositiveButton(getResources().getString(R.string.try_again),
										new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface arg0, int arg1)
											{
												pushFirmwareToCamera(uploadURL,filePath,fileName);
											}
										});
								builder.setNegativeButton(getResources().getString(R.string.cancel),new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int arg1)
									{
										dialog.cancel();
										((OtaActivity)mContext).onfinish(Activity.RESULT_CANCELED);
									}
								});


								if(mAlertDialog != null && mAlertDialog.isShowing())
									mAlertDialog.cancel();

								mAlertDialog = builder.create();
								mAlertDialog.show();

								Button pbutton = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
								pbutton.setTextColor(getResources().getColor(R.color.text_blue));

								Button pnbutton = mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
								pnbutton.setTextColor(getResources().getColor(R.color.text_blue));


							}
						});

					}
				}
			});

		}
		else if (Util.isUseSignatureForFwUpgrade(mDeviceModelID, mCheckFirmwareUpdateResult.getCurrentFirmwareVersion()))
		{
			Log.d(TAG, "Camera firmware version is " + mCheckFirmwareUpdateResult.getCurrentFirmwareVersion() + " ==> so upload signature data first");
			pushSignatureToFirmware(mCheckFirmwareUpdateResult.getUploadFwURL(), mCheckFirmwareUpdateResult.getSignatureData(), mCheckFirmwareUpdateResult.getSignatureFileName(), callback);
		}
		else
		{
			_pushFirmwareToCamera(uploadURL, filePath, fileName);
		}
	}

	private void _pushFirmwareToCamera(String uploadURL, String filePath, String fileName)
	{
		try
		{
			setStatus(Status.UPLOADING_FIRMWARE_TO_DEVICE);

			mJUploader = new JUploader(uploadURL, filePath, fileName);
			mJUploader.setModelId(mDeviceModelID);

			mJUploader.addObserver(new Observer()
			{
				@Override
				public void update(Observable arg0, Object arg1)
				{
					Runnable updateUIRunnable = null;
					if (mJUploader.getStatus() == JUploader.UPLOADING)
					{
						updateUIRunnable = new Runnable()
						{
							@Override
							public void run()
							{
								if (mOtaProgressBar != null)
								{
									mOtaProgressBar.setProgress((int) mJUploader.getPercent());
								}

								if (mOtaPercentageTv != null) {
									String msg = String.format(getString(R.string.percentage_status),
											Integer.valueOf((int) mJUploader.getPercent())) + "%";
									mOtaPercentageTv.setText(msg);
								}
							}
						};
					}
					else if (mJUploader.getStatus() == JUploader.COMPLETE)
					{
						updateUIRunnable = new Runnable() {
							@Override
							public void run () {

								if(mCheckFirmwareUpdateResult.getRegID() != null)
								{
									if (PublicDefine.MODEL_ID_ORBIT.compareToIgnoreCase(PublicDefines.getModelIDFromRegID(mCheckFirmwareUpdateResult.getRegID())) == 0)
									{
										if (Util.isThisVersionGreaterThan(mCheckFirmwareUpdateResult.getCurrentFirmwareVersion(), CheckFirmwareUpdateTask.ORBIT_NEW_FIRMWARE_WORK_FLOW))
										{
											upgradeDevice();
										}
										else
										{
											waitForLocalUpgradeCompleted();
										}
									}
									else
									{
										waitForLocalUpgradeCompleted();
									}
								}
							}
						};
					}
					else if (mJUploader.getStatus() == JUploader.ERROR)
					{
						updateUIRunnable = new Runnable()
						{
							@Override
							public void run()
							{
								if (mOtaPercentageTv != null)
								{
									mOtaPercentageTv.setText(getString(R.string.sending_firmware_to_camera_failed));
									isAnyErrorOccured = true;

									restartOtaProcess();

								}
							}
						};
					}
					else if (mJUploader.getStatus() == JUploader.TIMEOUT)
					{
						updateUIRunnable = new Runnable()
						{
							@Override
							public void run() {
								if (mOtaPercentageTv != null)
								{
									mOtaPercentageTv.setText(getString(R.string.sending_firmware_to_camera_timeout));
									isAnyErrorOccured = true;

									restartOtaProcess();
								}
							}
						};
					}

					if (updateUIRunnable != null && mContext != null )
					{
						((Activity)mContext).runOnUiThread(updateUIRunnable);
					}
				}
			});
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			isAnyErrorOccured = true;
		}
	}


	private void pushSignatureToFirmware(final String uploadURL, final byte[] signatureData,
	                                     final String signatureFileName, final FutureCallback<Boolean> callback)
	{
		if(mContext != null)
		{
			((Activity) mContext).runOnUiThread(new Runnable() {
				@Override
				public void run () {
					setStatus(Status.UPLOADING_FIRMWARE_TO_DEVICE);
				}
			});
		}




		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				mJUploader = new JUploader(uploadURL, signatureData, signatureFileName, 0);
				mJUploader.setModelId(mDeviceModelID);

				String result = mJUploader.uploadFile(null);

				if ((result.indexOf("Signature updated!") > 0) || (result.indexOf("Upload digital signature success")>0))
				{
					callback.onCompleted(null, true);
				}
				else
				{
					callback.onCompleted(null, false);
				}

			}
		}).start();
	}

	private Handler mHandler =  new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case UPGRADE_DONE_TIMEOUT:
					setStatus(Status.CAMREA_UPDATE_SUCCEEDED);
					break;
			}
		}
	};

	private void waitForUpgradeComplete()
	{
		setStatus(Status.CAMERA_IS_UPGRADING);

		Thread waitForUpgradeComplete = new Thread(new Runnable()
		{
			@Override
			public void run ()
			{
				int i = 0;
				long endTime = System.currentTimeMillis() + TOTAL_FLASH_TIME_WAIT;
				String newServerFirmwareVersion = null;
				boolean isSuccess = false;

				while (System.currentTimeMillis() < endTime)
				{
					try
					{
						if (mDeviceModelID.compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0)
						{
							if(i==0)
							{
								try {
									Thread.sleep(FLASH_PROCESS_WAIT_TIME);
								}
								catch (InterruptedException e){
								}
							}

							Models.ApiResponse<SerializableDeviceProfile> deviceProfile = Api.getInstance().getService().getDeviceProfile(mCheckFirmwareUpdateResult.getApiKey(), mCheckFirmwareUpdateResult.getRegID());

							if (deviceProfile.getStatus().equalsIgnoreCase("200") && deviceProfile.getData().getFirmwareVersion() != null)
							{
								newServerFirmwareVersion = deviceProfile.getData().getFirmwareVersion();
							}

							if (mCheckFirmwareUpdateResult.getOTAVersion().equalsIgnoreCase(newServerFirmwareVersion))
							{
								if(BuildConfig.DEBUG)
									Log.d(TAG, "After upgrade, device has new version = ota version => succeeded.");
								isSuccess = true;

							}

							if(isSuccess)
							{
								mStatus = Status.CAMREA_UPDATE_SUCCEEDED;
								((Activity) mContext).runOnUiThread(new Runnable() {
									@Override
									public void run () {
										setStatus(Status.CAMREA_UPDATE_SUCCEEDED);
									}
								});
								break;
							}
							else
							{
								try
								{
									i++;
									Thread.sleep(1000);
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
							}
						}
						else
						{
							break;
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				if(!isSuccess)
				{
					mStatus = Status.CAMREA_UPDATE_FAILED;
					((Activity) mContext).runOnUiThread(new Runnable() {
						@Override
						public void run () {
							setStatus(Status.CAMREA_UPDATE_FAILED);
						}
					});
				}
			}
		});
		waitForUpgradeComplete.start();
	}


	private void waitForLocalUpgradeCompleted()
	{
		setStatus(Status.CAMERA_IS_UPGRADING);

		final String burningProcessURL = mCheckFirmwareUpdateResult.getBurningProgressURL();
		final String BURNING_PROCESS = "burning_process :";

		final String versionURL = mCheckFirmwareUpdateResult.getKeepAliveURL();
		final String GET_VERSION = "get_version: ";

		Thread waitForUpgradeComplete = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				int i = 0;
				int numberOfFailed = 0;
				long endTime = System.currentTimeMillis() + TOTAL_FLASH_TIME_WAIT;
				boolean isConnectionException = false;

				while (System.currentTimeMillis() < endTime)
				{
					try
					{
						if(mDeviceModelID.compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)==0)
						{
							if(i==0)
							{
								try {
									Thread.sleep(FLASH_PROCESS_WAIT_TIME);
								}
								catch (InterruptedException e){
								}
							}

							String availableVersion = "";
							if(isConnectionException)
							{
								isConnectionException = false;
								wakeupDevice();
								Thread.sleep(WAKEUP_WAIT_TIME);
								availableVersion = JWebClient.downloadAsString(versionURL);
							}
							else {
								availableVersion = JWebClient.downloadAsString(versionURL);
							}

							if(BuildConfig.DEBUG)
								Log.i(TAG,"available version:- " + availableVersion);

							if(mContext != null && availableVersion.startsWith(GET_VERSION))
							{
								String newVersion = availableVersion.replace(GET_VERSION, "");


								if(!Util.isThisVersionGreaterThan(mCheckFirmwareUpdateResult.getOTAVersion(),
										newVersion))
								{
									mStatus = Status.CAMREA_UPDATE_SUCCEEDED;
									((Activity) mContext).runOnUiThread(new Runnable() {
										@Override
										public void run () {
											setStatus(Status.CAMREA_UPDATE_SUCCEEDED);
										}
									});

									break;
								}
							}

						}
						else
						{
							String burningProcess = JWebClient.downloadAsString(burningProcessURL);
							Log.i(TAG, "Burning process result: " + burningProcess);

							if (burningProcess.startsWith(BURNING_PROCESS) && mContext != null)
							{
								if (numberOfFailed > 40)
								{ // upgrade failed
									break;
								}

								String strBurningPercent = burningProcess.replace(BURNING_PROCESS, "");

								int burningPercent = Integer.parseInt(strBurningPercent);

								if (burningPercent == -1)
								{
									numberOfFailed++;
								}
								else if (burningPercent >= 0)
								{
									if (burningPercent > 100)
									{
										burningPercent = 100;
									}

									int displayValue = mDeviceModelID.equals("0877") ? burningPercent / 2 : burningPercent;

									//setUpgradeProgress(displayValue);

									if (burningPercent == 100)
									{
										if ("0877".equals(mDeviceModelID))
										{
											mStatus = Status.WAITING_RECOVERY;
											((Activity) mContext).runOnUiThread(new Runnable() {
												@Override
												public void run () {
													setStatus(Status.WAITING_RECOVERY);
												}
											});

										}
										else
										{

											mStatus = Status.CAMREA_UPDATE_SUCCEEDED;

											((Activity) mContext).runOnUiThread(new Runnable() {
												@Override
												public void run () {
													setStatus(Status.CAMREA_UPDATE_SUCCEEDED);
												}
											});

										}
										break;
									}
								}
							}
						}
					}
					catch (SocketException ex)
					{
						isConnectionException = true;
						ex.printStackTrace();
					}
					catch (IOException e)
					{
						isConnectionException = true;
						e.printStackTrace();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					try
					{
						i++;
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}

				// 10 min passed, but camera upgrade still was not done. Recheck on server...
				if (getStatus() == Status.WAITING_RECOVERY)
				{
					Log.d(TAG, "waiting for recovery in 3 minutes ....");
					// prevent after burning, user has to wait 3 minutes at 100% -> very inhibited
					// solution: just show 50% after that increase 5% every 18s. total 10 times
					int count = 0;
					do
					{
						try
						{
							Thread.sleep(18000);
						}
						catch (InterruptedException e)
						{
						}
						int displayValue = 50 + (count + 1) * 5;
						if (displayValue > 100)
						{
							displayValue = 100;
						}
						//setUpgradeProgress(displayValue);
					}
					while (++count < 10);
					Log.d(TAG, "waiting for recovery in 3 minutes .... DONE");
					checkUpgradeResultOnServer();
				}
				else
				{

					if (getStatus() != Status.CAMREA_UPDATE_SUCCEEDED) {
						checkUpgradeResultOnServer();
					}
				}
			}
		});
		waitForUpgradeComplete.start();
	}

	private void wakeupDevice()
	{
		DeviceID deviceID = new DeviceID(mCheckFirmwareUpdateResult.getApiKey(), mCheckFirmwareUpdateResult.getRegID());
		DeviceManagerService.getInstance(mContext).wakeUpDevice(deviceID, new Response.Listener<DeviceWakeupResponse>()
				{
					@Override
					public void onResponse (DeviceWakeupResponse response)
					{
						if(BuildConfig.DEBUG)
							Log.d(TAG,"device wakeup done");

					}
				},
				new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse (VolleyError error)
					{
						if (error != null && error.networkResponse != null)
						{
							Log.d(TAG, error.networkResponse.toString());
							Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
						}
					}
				});
	}

	private void checkUpgradeResultOnServer()
	{
		boolean isSuccess = false;
		try
		{
			Log.i(TAG, "After upgraded, device is online now");
			String fw = null;

			if ("0877".equals(mDeviceModelID))
			{
				Log.i(TAG, "Checking fw via http command");
				fw = JWebClient.downloadAsString(mCheckFirmwareUpdateResult.getKeepAliveURL());
				if (!TextUtils.isEmpty(fw))
				{
					fw = fw.replace("get_version: ", "");
				}
			}
			if (TextUtils.isEmpty(fw))
			{
				Log.i(TAG, "Checking fw via request api");
				Models.ApiResponse<SerializableDeviceProfile> deviceProfile = Api.getInstance().getService().getDeviceProfile(mCheckFirmwareUpdateResult.getApiKey(), mCheckFirmwareUpdateResult.getRegID());

				if (deviceProfile.getStatus().equalsIgnoreCase("200") && deviceProfile.getData().getFirmwareVersion() != null)
				{
					fw = deviceProfile.getData().getFirmwareVersion();
				}
				else
				{
					Log.i(TAG, "After upgrade, check device information from server error");
				}
			}
			if (mCheckFirmwareUpdateResult.getOTAVersion().equalsIgnoreCase(fw))
			{
				Log.i(TAG, "After upgrade, device has new version = ota version => succeeded.");
				isSuccess = true;

			}
			else {
				Log.i(TAG, "After upgrade, device has new version != ota version => failed. Device firmware version is: " + fw);
				isSuccess = false;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();

		}

		if(mContext != null) {
			if (isSuccess) {
				((Activity) mContext).runOnUiThread(new Runnable() {
					@Override
					public void run () {
						setStatus(Status.CAMREA_UPDATE_SUCCEEDED);

					}
				});
			} else {
				((Activity) mContext).runOnUiThread(new Runnable() {
					@Override
					public void run () {
						setStatus(Status.CAMREA_UPDATE_FAILED);

					}
				});
			}
		}
	}

	private Status getStatus()
	{
		return mStatus;
	}

	public void setStatus(Status mStatus)
	{
		this.mStatus = mStatus;
		if (mStatus == Status.DOWNLOADING_NEW_FIRMWARE_FROM_OTA_SERVER)
		{
			mOtaLiveStatusTv.setText(getResources().getString(R.string.downloading));

			String msg = getResources().getString(R.string.default_percentage_status) + "%";
			mOtaPercentageTv.setText(msg);

			mOtaProgressBar.setProgress(0);
			mOtaProgressBar.setIndeterminate(false);
		}
		else if(mStatus == Status.WAKEUP_DEVICE)
		{
			mOtaLiveStatusTv.setText(getResources().getString(R.string.wakeup_device));

			String msg = getResources().getString(R.string.default_percentage_status) + "%";
			mOtaPercentageTv.setText(msg);

			mOtaProgressBar.setProgress(0);
			mOtaProgressBar.setIndeterminate(true);
		}
		else if (mStatus == Status.UPLOADING_FIRMWARE_TO_DEVICE)
		{
			mOtaLiveStatusTv.setText(getResources().getString(R.string.transferring));

			String msg = getResources().getString(R.string.default_percentage_status) + "%";
			mOtaPercentageTv.setText(msg);

			mOtaProgressBar.setProgress(0);
			mOtaProgressBar.setIndeterminate(false);
		}
		else if (mStatus == Status.CAMERA_IS_UPGRADING)
		{
			mOtaLiveStatusTv.setText(getResources().getString(R.string.ota_upgrading));

			String msg = getResources().getString(R.string.default_percentage_status) + "%";
			mOtaPercentageTv.setText(msg);
			mOtaPercentageTv.setVisibility(View.GONE);

			mOtaProgressBar.setProgress(0);
			mOtaProgressBar.setIndeterminate(true);
		}
		else if (mStatus == Status.CAMREA_UPDATE_SUCCEEDED)
		{

			if(mContext != null) {
				CommonUtil.setSettingInfo(mContext, mCheckFirmwareUpdateResult.getRegID() + "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, false);
				restartOtaProcess();
			}

		}
		else if (mStatus == Status.CAMREA_UPDATE_FAILED)
		{
			restartOtaProcess();
		}
	}



	private void restartOtaProcess()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

		builder.setTitle(getResources().getString(R.string.firmware_upgrade));
		switch(getStatus())
		{
			case DOWNLOADING_NEW_FIRMWARE_FROM_OTA_SERVER:
				builder.setMessage(getResources().getString(R.string.download_firmware_error));
				break;

			case UPLOADING_FIRMWARE_TO_DEVICE:
				builder.setMessage(getResources().getString(R.string.upload_failed));
				break;

			case CAMREA_UPDATE_SUCCEEDED:
				builder.setMessage(getResources().getString(R.string.camera_firmware_upgrade_done));
				break;

			default:
				builder.setMessage(getResources().getString(R.string.firmware_upgrade_error));
				break;
		}


		if(getStatus() != Status.CAMREA_UPDATE_SUCCEEDED)
		{
			builder.setPositiveButton(getResources().getString(R.string.try_again),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick (DialogInterface dialog, int arg1) {
							dialog.cancel();
							isAnyErrorOccured = false;
							isStarted = false;
							startOtaProgress();

						}
					});

			builder.setNegativeButton(getResources().getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick (DialogInterface dialog, int which) {
							dialog.cancel();
							((OtaActivity) mContext).onfinish(Activity.RESULT_CANCELED);
						}
					});
		}
		else
		{
			builder.setPositiveButton(getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick (DialogInterface dialog, int arg1)
						{
							dialog.cancel();
							isAnyErrorOccured = false;
							isStarted = false;
							((OtaActivity) mContext).onfinish(Activity.RESULT_OK);

						}
					});

		}

		if(mAlertDialog != null && mAlertDialog.isShowing())
			mAlertDialog.cancel();

		mAlertDialog = builder.create();
		mAlertDialog.setCancelable(false);
		mAlertDialog.show();
		Button nbutton = mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		nbutton.setTextColor(getResources().getColor(R.color.text_blue));
		Button pbutton = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		pbutton.setTextColor(getResources().getColor(R.color.text_blue));

	}
}
