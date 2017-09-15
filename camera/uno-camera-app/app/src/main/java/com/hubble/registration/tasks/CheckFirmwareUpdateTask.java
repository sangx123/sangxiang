package com.hubble.registration.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.firmware.FirmwareInfo;
import com.hubble.firmware.FirmwareInfoFactory;
import com.hubble.registration.JWebClient;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.util.CommandUtils;
import com.hubbleconnected.camera.BuildConfig;
import com.koushikdutta.ion.Ion;
import com.nxcomm.blinkhd.ui.Global;
import com.util.CommonUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.hubble.IAsyncTaskCommonHandler;
import base.hubble.meapi.PublicDefines;
import base.hubble.meapi.device.SendCommandResponse;


public class CheckFirmwareUpdateTask extends AsyncTask<Void, Void, CheckFirmwareUpdateResult> {
	private static final String TAG = "CheckFirmwareUpdateTask";
	private static final String CHECK_FW_UPGRADE_CMD = "action=command&command=check_fw_upgrade";
	private static final String CHECK_FW_UPGRADE_RESULT_OK = "check_fw_upgrade: ";
	private static final String CHECK_FW_UPGRADE_COMMAND = "check_fw_upgrade";
	public static final String FW_VERSION_01_16_01 = "01.16.01";
	public static final String FW_VERSION_01_16_99 = "01.16.99";
	public static final String FW_VERSION_USING_MD5_LAST = "01.19.35";
	public static final String ORBIT_NEW_FIRMWARE_WORK_FLOW = "01.19.87";

	private static final String CHECK_OTA_UPDATE_AVAILABLE_URL = "http://ota.hubble.in/ota/%s_patch/version.txt";
	private static final String CHECK_DEV_OTA_UPDATE_AVAILABLE_URL = "http://ota.hubble.in/ota/%s_patch/version_dev.txt";
	private static final String FIRMWARE_DOWNLOAD_LINK_URL_PATTERN = "https://ota.hubble.in/ota/%s_patch/%s-%s.tar.gz";
	private static final String FIRMWARE_DOWNLOAD_LINK_MODEL_0086_URL_PATTERN = "https://ota.hubble.in/ota/%s_patch/%s-%s.tar";
	private static final String FIRMWARE_DOWNLOAD_LINK_MODEL_0080_URL_PATTERN = "https://ota.hubble.in/ota1/%s_patch/%s-%s.fw.pkg";
	private static final String FIRMWARE_MD5_DOWNLOAD_LINK_URL_PATTERN = "http://ota.hubble.in/ota/%s_patch/%s-%s.md5";
	private static final String FIRMWARE_DOWNLOAD_LINK_MTAG = "https://ota.hubble.in/ota/06%s_patch/%s-%s.zip";

	// AA-88: Use different ota folder name for manual FW upgrade on FW >=
	// 01.17.xx APPLY for model 0854

	private static final String CHECK_OTA_UPDATE_AVAILABLE_0854_URL = "http://ota.hubble.in/ota1/%s_patch/version.txt";
	private static final String CHECK_DEV_OTA_UPDATE_AVAILABLE_0854_URL = "http://ota.hubble.in/ota1/%s_patch/version_dev.txt";
	private static final String FIRMWARE_DOWNLOAD_LINK_URL_0854_PATTERN = "https://ota.hubble.in/ota1/%s_patch/%s-%s.tar.gz";
	private static final String FIRMWARE_MD5_DOWNLOAD_LINK_URL_0854_PATTERN = "http://ota.hubble.in/ota1/%s_patch/%s-%s.md5";
	private static final String DEV_DEVICE_LIST_OTA_URL = "http://ota.hubble.in/ota/%s_patch/udid.txt";
	private static final String DEV_DEVICE_LIST_OTA1_URL = "http://ota.hubble.in/ota1/%s_patch/udid.txt";
	private static final String FIRMWARE_MD5_DOWNLOAD_LINK_URL_MTAG = "http://ota.hubble.in/ota/06%s_patch/%s-%s.md5";
	private static final String FIRMWARE_SIGNATURE_DOWNLOAD_LINK = "http://ota.hubble.in/ota1/%s_patch/%s-%s.sig"; //0066-01.19.37.sig

	private static final String CHECK_OTA_UPDATE_AVAILABLE_0877_URL = "http://ota.hubble.in/ota2/%s_patch/version.txt";
	private static final String CHECK_DEV_OTA_UPDATE_AVAILABLE_0877_URL = "http://ota.hubble.in/ota2/%s_patch/version_dev.txt";
	private static final String DEV_DEVICE_LIST_OTA2_URL = "http://ota.hubble.in/ota2/%s_patch/udid.txt";
	private static final String FIRMWARE_DOWNLOAD_LINK_URL_0877_PATTERN = "https://ota.hubble.in/ota2/%s_patch/%s-%s.tar";
	private static final String FIRMWARE_MD5_DOWNLOAD_LINK_URL_0877_PATTERN = "http://ota.hubble.in/ota2/%s_patch/%s-%s.md5";
	private static final String FIRMWARE_SIGNATURE_DOWNLOAD_LINK_URL_0877 = "http://ota.hubble.in/ota2/%s_patch/%s-%s.sig";

	public static final String FIRMWARE_TAR_GZ = "%s-%s.tar.gz";
	public static final String FIRMWARE_TAR = "%s-%s.tar";
	public static final String FIRMWARE_SIG = "%s-%s.sig";
	public static final String FIRMWARE_ZIP = "%s-%s.zip";
	public static final String FIRMWARE_FW_PKG = "%s-%s.fw.pkg";
	public static final String MTAG_MODEL = "0001";
	private String mCheckOTAUpdateAvailableURL, mCheckDevDeviceURL;
	private IAsyncTaskCommonHandler mHandler;
	private String mCurrentFwVersion;
	private CheckFirmwareUpdateResult mCheckFirmwareUpdateResult;
	private String mAPIKey;
	private String mRegID;
	private String mCameraModel;

	private boolean mUseDevOTA;
	private boolean isDeviceOTA = false;
	private Device mDevice;
	private static final String MODEL_0086_VERSION_STR = "version=";

	private static final int DEVICE_FIRMWARE_UPGRADE_STATUS_IN_PROGRESS = 2;
	private static final int DEVICE_FIRMWARE_UPGRADE_STATUS_NA = 0;
	private static final int DEVICE_FIRMWARE_UPGRADE_STATUS_FAIL = -1;

	private FirmwareInfoFactory mFirmwareInfoFactory = new FirmwareInfoFactory();

	public CheckFirmwareUpdateTask(String apiKey, String regID, String currentFwVersion, String modelID, Device device, IAsyncTaskCommonHandler handler, boolean useDevOTA)
	{
		this(apiKey,regID,currentFwVersion,modelID,device,handler,useDevOTA,false);
	}

	public CheckFirmwareUpdateTask(String apiKey, String regID, String currentFwVersion, String modelID, Device device, IAsyncTaskCommonHandler handler, boolean useDevOTA,boolean deviceOTA) {

		mAPIKey = apiKey;
		mRegID = regID;
		mCurrentFwVersion = currentFwVersion;
		mHandler = handler;
		mCameraModel = modelID;
		this.mDevice = device;
		this.mUseDevOTA = useDevOTA;
		isDeviceOTA = deviceOTA;

		mCheckFirmwareUpdateResult = new CheckFirmwareUpdateResult();
		mCheckFirmwareUpdateResult.setRegID(regID);
		mCheckFirmwareUpdateResult.setCurrentFirmwareVersion(currentFwVersion);

		mCheckFirmwareUpdateResult.setDeviceOTA(isDeviceOTA);
	}

	public boolean isNeedUseOtherOTAFolder() {
		return Util.isThisVersionGreaterThan(mCurrentFwVersion, FW_VERSION_01_16_99);
	}

	public String getCurrentVersion()
	{
		return mCurrentFwVersion;
	}

	@Override
	protected void onPostExecute(CheckFirmwareUpdateResult result) {
		mHandler.onPostExecute(result);
		super.onPostExecute(result);
	}

	String firmwareVersion;

	void checkFirmwearUpdate(String deviceFwVersion) {
		if (mDevice.getProfile().getModelId() != null && !mDevice.getProfile().getModelId().equals("-1")) {
				mCameraModel = mDevice.getProfile().getModelId();
			}
		if (deviceFwVersion != null && !deviceFwVersion.equals("-1")) {
			mCurrentFwVersion = deviceFwVersion;
			mCheckFirmwareUpdateResult.setCurrentFirmwareVersion(mCurrentFwVersion);
		}

		Log.d(TAG, "Check fw upgrade, model id: " + mCameraModel + ", fw version: " + mCurrentFwVersion);

		if (mUseDevOTA) {
			if (mCameraModel.equals("0877")) {
				mCheckOTAUpdateAvailableURL = String.format(CHECK_DEV_OTA_UPDATE_AVAILABLE_0877_URL, mCameraModel);
				mCheckDevDeviceURL = String.format(DEV_DEVICE_LIST_OTA2_URL, mCameraModel);
			} else if (isNeedUseOtherOTAFolder()) { // Current fw version is greater than 01.17.00, so use different ota folder.
				mCheckOTAUpdateAvailableURL = String.format(CHECK_DEV_OTA_UPDATE_AVAILABLE_0854_URL, mCameraModel);
				mCheckDevDeviceURL = String.format(DEV_DEVICE_LIST_OTA1_URL, mCameraModel);
			} else {
				mCheckOTAUpdateAvailableURL = String.format(CHECK_DEV_OTA_UPDATE_AVAILABLE_URL, mCameraModel);
				mCheckDevDeviceURL = String.format(DEV_DEVICE_LIST_OTA_URL, mCameraModel);
			}
		} else { // Use Normal OTA
			if (mCameraModel.equals("0877")) {
				mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_0877_URL, mCameraModel);
			} else if (isNeedUseOtherOTAFolder()) { // Current fw version is greater than 01.17.00, so use different ota folder.
				mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_0854_URL, mCameraModel);
			} else {
				if (mCameraModel.equals(MTAG_MODEL)) {
					mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_URL, "06" + mCameraModel);
				} else {
					mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_URL, mCameraModel);
				}
			}
		}

		// if using dev ota but device is not in dev device list, reset check firmware upgrade url to normal OTA
		boolean isDevDevice = isDevDevice(mCheckDevDeviceURL, mRegID);
		if (mUseDevOTA && !isDevDevice) {
			Log.i(TAG, "Use dev ota but device id does not existing on dev device list, use official OTA");
			if (mCameraModel.equals("0877")) {
				mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_0877_URL, mCameraModel);
			} else if (isNeedUseOtherOTAFolder()) { // Current fw version is greater than 01.17.00, so use different ota folder.
				mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_0854_URL, mCameraModel);
			} else {
				mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_URL, mCameraModel);
			}
		}
		Log.d(TAG, "Check fw upgrade, useDevOTA? " + mUseDevOTA + ", url: " + mCheckOTAUpdateAvailableURL);

		try {
			URL url = null;
			String fwVersion = null;
			int responseCode = -1;
			url = new URL(mCheckOTAUpdateAvailableURL);
			HttpURLConnection conn = null;
			BufferedReader reader = null;
			conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(PublicDefines.getHttpTimeout());

			responseCode = conn.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(conn.getInputStream(), 20 * 1024)));
				fwVersion = reader.readLine();

				// camera focus 86 has different version string: version=xx.xx.xx, others model are only xx.xx.xx
				if (mCameraModel.equalsIgnoreCase(com.discovery.ScanProfile.MODEL_ID_FOCUS86)
						|| mCameraModel.equalsIgnoreCase("0877")
						|| mCameraModel.equalsIgnoreCase("0081")
						|| mCameraModel.equalsIgnoreCase("0082")) {
					fwVersion = fwVersion.replace(MODEL_0086_VERSION_STR, "");
					Log.i(TAG, "Device model is " + mCameraModel + " so using different parsing method. Version = " + fwVersion);
				}

				if (isVersionString(fwVersion)) {
					mCheckFirmwareUpdateResult.setOTAVersion(fwVersion);
					if (Util.isThisVersionGreaterThan(fwVersion, mCurrentFwVersion)) {
						mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(true);

						// camera focus 86 has different firmware binary file. extension: tar instead of tar.gz
						if (mCameraModel.equalsIgnoreCase(com.discovery.ScanProfile.MODEL_ID_FOCUS86)) {
							mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_MODEL_0086_URL_PATTERN, mCameraModel, mCameraModel, fwVersion));
							mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
						}else if(mCameraModel.equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)){
							mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_MODEL_0080_URL_PATTERN, mCameraModel, mCameraModel, fwVersion));
							mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
						}
						else if (mCameraModel.equals("0877")) {
							mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_URL_0877_PATTERN, mCameraModel, mCameraModel, fwVersion));
							mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
						} else {
							if (isNeedUseOtherOTAFolder()) {
								mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_URL_0854_PATTERN, mCameraModel, mCameraModel, fwVersion));
								mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
							} else {
								if (mCameraModel.equals(MTAG_MODEL)) {
									mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_MTAG, mCameraModel, mCameraModel, fwVersion));
								} else {
									mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_URL_PATTERN, mCameraModel, mCameraModel, fwVersion));
								}
								mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
							}
						}

						mCheckFirmwareUpdateResult.setFirmwareResidedInCamera(false);
						String md5Link;

						if (mCameraModel.equals("0877")) {
							md5Link = String.format(FIRMWARE_MD5_DOWNLOAD_LINK_URL_0877_PATTERN, mCameraModel, mCameraModel, fwVersion);
						} else if (isNeedUseOtherOTAFolder()) {
							md5Link = String.format(FIRMWARE_MD5_DOWNLOAD_LINK_URL_0854_PATTERN, mCameraModel, mCameraModel, fwVersion);
						} else {
							if (mCameraModel.equals(MTAG_MODEL)) {
								md5Link = String.format(FIRMWARE_MD5_DOWNLOAD_LINK_URL_MTAG, mCameraModel, mCameraModel, fwVersion);
							} else {
								md5Link = String.format(FIRMWARE_MD5_DOWNLOAD_LINK_URL_PATTERN, mCameraModel, mCameraModel, fwVersion);
							}
						}
						/**
						 * Format firmware file name
						 */
						String checkedSuffix = "";
						// camera focus 86 has different firmware binary file. extension: tar instead of tar.gz
						if (mCameraModel.equalsIgnoreCase(com.discovery.ScanProfile.MODEL_ID_FOCUS86) || mCameraModel.equals("0877")) {
							checkedSuffix = String.format(FIRMWARE_TAR, mCameraModel, fwVersion);
						}
						else if(mCameraModel.equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)) {
							checkedSuffix = String.format(FIRMWARE_FW_PKG, mCameraModel, fwVersion);
						}
						else {
							if (mCameraModel.equals(MTAG_MODEL)) {
								checkedSuffix = String.format(FIRMWARE_ZIP, mCameraModel, fwVersion);
							} else {
								checkedSuffix = String.format(FIRMWARE_TAR_GZ, mCameraModel, fwVersion);
							}
						}
						mCheckFirmwareUpdateResult.setNewFirmwareFileName(checkedSuffix);
						/**
						 * if firmware version is greater than 01.19.35, camera will verify firmware binary by signature
						 * new update: 877 uses md5 regardless of any fw version
						 **/
						if (Util.isUseSignatureForFwUpgrade(mCameraModel, mCurrentFwVersion)) {
							Log.i(TAG, "Current camera firmware is " + mCurrentFwVersion + ", we switch to using signature instead of md5");
							String signatureDownloadLink = String.format(FIRMWARE_SIGNATURE_DOWNLOAD_LINK, mCameraModel, mCameraModel, fwVersion);
							Log.d(TAG, "Signature link: " + signatureDownloadLink);
							mCheckFirmwareUpdateResult.setSignatureFileName(String.format(FIRMWARE_SIG, mCameraModel, fwVersion));
							byte[] signature = JWebClient.downloadSignatureData(signatureDownloadLink);
							mCheckFirmwareUpdateResult.setSignatureData(signature);
							if (signature != null) {
								String hexString = toHexString(signature);
								Log.i(TAG, "Camera firmware signature under hex format: " + hexString);
								if(BuildConfig.DEBUG)
									Log.i(TAG, "camera md5 :- " + md5(signature));
								mCheckFirmwareUpdateResult.setNewFirmwareMD5(md5(signature));
							} else {
								Log.e(TAG, "Camera firmware signature is NULL");
								mCheckFirmwareUpdateResult.setNewFirmwareMD5(null);
							}
						} else {
							Log.d(TAG, "MD5 link: " + md5Link);
							String md5Result = JWebClient.downloadAsStringWithoutEx(md5Link);
							if (md5Result != null) {
								if (md5Result.endsWith(checkedSuffix)) {
									md5Result = md5Result.split(" ")[0];
									Log.d(TAG, "RIGHT MD5 file: '" + md5Result + "'");
									mCheckFirmwareUpdateResult.setNewFirmwareMD5(md5Result);
								} else {
									Log.d(TAG, "MD5 wrong format.");
								}
							} else {
								Log.d(TAG, "MD5 file not found.");
							}
						}

					} else {
						Log.d(TAG, "Firmware version on server: " + fwVersion);
						Log.d(TAG, "Firmware version on camera: " + mCurrentFwVersion);
						Log.d(TAG, "Current FW version is newer than OTA version. No need to update.");
						mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(false);
					}
				} else {
					Log.d(TAG, "Result " + fwVersion + " is not VALID version string.");
				}
				reader.close();
			} else {
				Log.d(TAG, "Firmware version on server: " + fwVersion);
				Log.d(TAG, "Firmware version on camera: " + mCurrentFwVersion);
				Log.d(TAG, "No new firmware version available on OTA server.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		mHandler.onPostExecute(mCheckFirmwareUpdateResult);
	}

	@Override
     protected CheckFirmwareUpdateResult doInBackground(Void... arg0) {
	//	queryFirmwareVersion(mDevice);
		String deviceModelId = null;
		String deviceFwVersion = null;
		if (mDevice != null && !mCameraModel.equals(MTAG_MODEL)) {
			deviceModelId = mDevice.getProfile().getModelId();
			deviceFwVersion = mDevice.getProfile().getFirmwareVersion();
			if (deviceModelId != null && !deviceModelId.equals("-1")) {
				mCameraModel = deviceModelId;
			}
			if (deviceFwVersion != null && !deviceFwVersion.equals("-1")) {
				mCurrentFwVersion = deviceFwVersion;
				mCheckFirmwareUpdateResult.setCurrentFirmwareVersion(mCurrentFwVersion);
			}
		}
		Log.d(TAG, "Check fw upgrade, model id: " + mCameraModel + ", fw version: " + mCurrentFwVersion + " device ota :- " + isDeviceOTA);


		if(!isDeviceOTA)
		{
			if (mUseDevOTA)
			{
				if (mCameraModel.equals("0877"))
				{
					mCheckOTAUpdateAvailableURL = String.format(CHECK_DEV_OTA_UPDATE_AVAILABLE_0877_URL, mCameraModel);
					mCheckDevDeviceURL = String.format(DEV_DEVICE_LIST_OTA2_URL, mCameraModel);
				} else if (isNeedUseOtherOTAFolder()) { // Current fw version is greater than 01.17.00, so use different ota folder.
					mCheckOTAUpdateAvailableURL = String.format(CHECK_DEV_OTA_UPDATE_AVAILABLE_0854_URL, mCameraModel);
					mCheckDevDeviceURL = String.format(DEV_DEVICE_LIST_OTA1_URL, mCameraModel);
				} else {
					mCheckOTAUpdateAvailableURL = String.format(CHECK_DEV_OTA_UPDATE_AVAILABLE_URL, mCameraModel);
					mCheckDevDeviceURL = String.format(DEV_DEVICE_LIST_OTA_URL, mCameraModel);
				}
			} else { // Use Normal OTA
				if (mCameraModel.equals("0877")) {
					mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_0877_URL, mCameraModel);
				} else if (isNeedUseOtherOTAFolder()) { // Current fw version is greater than 01.17.00, so use different ota folder.
					mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_0854_URL, mCameraModel);
				} else {
					if (mCameraModel.equals(MTAG_MODEL)) {
						mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_URL, "06" + mCameraModel);
					} else {
						mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_URL, mCameraModel);
					}
				}
			}


			// if using dev ota but device is not in dev device list, reset check firmware upgrade url to normal OTA

			boolean isDevDevice = false;

			if(mCheckDevDeviceURL != null)
				isDevDevice = isDevDevice(mCheckDevDeviceURL, mRegID);

			if (mUseDevOTA && !isDevDevice) {
				Log.i(TAG, "Use dev ota but device id does not existing on dev device list, use official OTA");
				if (mCameraModel.equals("0877")) {
					mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_0877_URL, mCameraModel);
				} else if (isNeedUseOtherOTAFolder()) { // Current fw version is greater than 01.17.00, so use different ota folder.
					mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_0854_URL, mCameraModel);
				} else {
					mCheckOTAUpdateAvailableURL = String.format(CHECK_OTA_UPDATE_AVAILABLE_URL, mCameraModel);
				}
			}
			Log.d(TAG, "Check fw upgrade, useDevOTA? " + mUseDevOTA + ", url: " + mCheckOTAUpdateAvailableURL);

			FirmwareInfo fwInfo = mFirmwareInfoFactory.getFirmwareInfo(mCameraModel, mUseDevOTA && isDevDevice);
			if (fwInfo != null)
			{
				Log.w(TAG, "Use firmware info class. check ota link " + fwInfo.getFwCheckLink());
				mCheckOTAUpdateAvailableURL = fwInfo.getFwCheckLink();
			}
			Log.d(TAG, "Check fw upgrade, useDevOTA? " + mUseDevOTA + ", url: " + mCheckOTAUpdateAvailableURL);

			try {
				URL url = null;
				String fwVersion = null;
				int responseCode = -1;
				url = new URL(mCheckOTAUpdateAvailableURL);
				HttpURLConnection conn = null;
				BufferedReader reader = null;
				conn = (HttpURLConnection) url.openConnection();
				conn.setUseCaches(false);
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(60000);
				conn.setReadTimeout(PublicDefines.getHttpTimeout());

				responseCode = conn.getResponseCode();

				if (responseCode == HttpURLConnection.HTTP_OK) {
					reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(conn.getInputStream(), 20 * 1024)));
					fwVersion = reader.readLine();

					
					// camera focus 86 has different version string: version=xx.xx.xx, others model are only xx.xx.xx
					if (mCameraModel.equalsIgnoreCase(com.discovery.ScanProfile.MODEL_ID_FOCUS86)
							|| mCameraModel.equalsIgnoreCase("0877") || mCameraModel.equalsIgnoreCase("0081") || mCameraModel.equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
							|| mCameraModel.equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72)) {
						fwVersion = fwVersion.replace(MODEL_0086_VERSION_STR, "");
						Log.i(TAG, "Device model is " + mCameraModel + " so using different parsing method. Version = " + fwVersion);
					}

					if (isVersionString(fwVersion)) {
						mCheckFirmwareUpdateResult.setOTAVersion(fwVersion);
						if (Util.isThisVersionGreaterThan(fwVersion, mCurrentFwVersion)) {
							mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(true);

							// camera focus 86 has different firmware binary file. extension: tar instead of tar.gz
							if (mCameraModel.equalsIgnoreCase(com.discovery.ScanProfile.MODEL_ID_FOCUS86)) {
								mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_MODEL_0086_URL_PATTERN, mCameraModel, mCameraModel, fwVersion));
								mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
							} else if (mCameraModel.equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)) {
								mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_MODEL_0080_URL_PATTERN, mCameraModel, mCameraModel, fwVersion));
								mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
							} else if (mCameraModel.equals("0877")) {
								mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_URL_0877_PATTERN, mCameraModel, mCameraModel, fwVersion));
								mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
							} else {
								if (isNeedUseOtherOTAFolder()) {
									mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_URL_0854_PATTERN, mCameraModel, mCameraModel, fwVersion));
									mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
								} else {
									if (mCameraModel.equals(MTAG_MODEL)) {
										mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_MTAG, mCameraModel, mCameraModel, fwVersion));
									} else {
										mCheckFirmwareUpdateResult.setFirmwareDownloadLink(String.format(FIRMWARE_DOWNLOAD_LINK_URL_PATTERN, mCameraModel, mCameraModel, fwVersion));
									}
									mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
								}
							}

							mCheckFirmwareUpdateResult.setFirmwareResidedInCamera(false);
							String md5Link;

							if (mCameraModel.equals("0877")) {
								md5Link = String.format(FIRMWARE_MD5_DOWNLOAD_LINK_URL_0877_PATTERN, mCameraModel, mCameraModel, fwVersion);
							} else if (isNeedUseOtherOTAFolder()) {
								md5Link = String.format(FIRMWARE_MD5_DOWNLOAD_LINK_URL_0854_PATTERN, mCameraModel, mCameraModel, fwVersion);
							} else {
								if (mCameraModel.equals(MTAG_MODEL)) {
									md5Link = String.format(FIRMWARE_MD5_DOWNLOAD_LINK_URL_MTAG, mCameraModel, mCameraModel, fwVersion);
								} else {
									md5Link = String.format(FIRMWARE_MD5_DOWNLOAD_LINK_URL_PATTERN, mCameraModel, mCameraModel, fwVersion);
								}
							}
							/**
							 * Format firmware file name
							 */
							String checkedSuffix = "";
							// camera focus 86 has different firmware binary file. extension: tar instead of tar.gz
							if (mCameraModel.equalsIgnoreCase(com.discovery.ScanProfile.MODEL_ID_FOCUS86) || mCameraModel.equals("0877")) {
								checkedSuffix = String.format(FIRMWARE_TAR, mCameraModel, fwVersion);
							} else if (mCameraModel.equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)) {
								checkedSuffix = String.format(FIRMWARE_FW_PKG, mCameraModel, fwVersion);
							} else {
								if (mCameraModel.equals(MTAG_MODEL)) {
									checkedSuffix = String.format(FIRMWARE_ZIP, mCameraModel, fwVersion);
								} else {
									checkedSuffix = String.format(FIRMWARE_TAR_GZ, mCameraModel, fwVersion);
								}
							}
							mCheckFirmwareUpdateResult.setNewFirmwareFileName(checkedSuffix);
							/**
							 * if firmware version is greater than 01.19.35, camera will verify firmware binary by signature
							 * new update: 877 uses md5 regardless of any fw version
							 **/
							if (Util.isUseSignatureForFwUpgrade(mCameraModel, mCurrentFwVersion)) {
								Log.i(TAG, "Current camera firmware is " + mCurrentFwVersion + ", we switch to using signature instead of md5");
								String signatureDownloadLink = String.format(FIRMWARE_SIGNATURE_DOWNLOAD_LINK, mCameraModel, mCameraModel, fwVersion);
								Log.d(TAG, "Signature link: " + signatureDownloadLink);
								mCheckFirmwareUpdateResult.setSignatureFileName(String.format(FIRMWARE_SIG, mCameraModel, fwVersion));
								byte[] signature = JWebClient.downloadSignatureData(signatureDownloadLink);
								mCheckFirmwareUpdateResult.setSignatureData(signature);
								if (signature != null) {
									String hexString = toHexString(signature);
									Log.i(TAG, "Camera firmware signature under hex format: " + hexString);
									mCheckFirmwareUpdateResult.setNewFirmwareMD5(md5(signature));
								} else {
									Log.e(TAG, "Camera firmware signature is NULL");
									mCheckFirmwareUpdateResult.setNewFirmwareMD5(null);
								}
							} else {
								Log.d(TAG, "MD5 link: " + md5Link);
								String md5Result = JWebClient.downloadAsStringWithoutEx(md5Link);
								if (md5Result != null) {
									if (md5Result.endsWith(checkedSuffix)) {
										md5Result = md5Result.split(" ")[ 0 ];
										Log.d(TAG, "RIGHT MD5 file: '" + md5Result + "'");
										mCheckFirmwareUpdateResult.setNewFirmwareMD5(md5Result);
									} else {
										Log.d(TAG, "MD5 wrong format.");
									}
								} else {
									Log.d(TAG, "MD5 file not found.");
								}
							}

							if (fwInfo != null)
							{
								Log.e(TAG, "fwInfo != null");
								fwInfo.setOtaVersion(fwVersion);
								byte[] signatureData = JWebClient.downloadSignatureData(fwInfo.getSignatureDownloadLink());
								mCheckFirmwareUpdateResult.setFirmwareDownloadLink(fwInfo.getBinaryDownloadLink());
								mCheckFirmwareUpdateResult.setSignatureData(signatureData);
								mCheckFirmwareUpdateResult.setNewFirmwareVersion(fwVersion);
								mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(true);
								mCheckFirmwareUpdateResult.setSignatureFileName(fwInfo.getSignatureFilename());
								mCheckFirmwareUpdateResult.setNewFirmwareFileName(fwInfo.getBinaryFilename());
								mCheckFirmwareUpdateResult.setOTAVersion(fwVersion);
								mCheckFirmwareUpdateResult.setNewFirmwareMD5(null);
								mCheckFirmwareUpdateResult.setFirmwareResidedInCamera(false);
								mCheckFirmwareUpdateResult.setFirmwareVersionResidedInCamera(null);
								Gson gson = new GsonBuilder().setPrettyPrinting().create();
								String json = gson.toJson(mCheckFirmwareUpdateResult);
								Log.w(TAG, json);
							}

						} else {
							Log.d(TAG, "Firmware version on server: " + fwVersion);
							Log.d(TAG, "Firmware version on camera: " + mCurrentFwVersion);
							Log.d(TAG, "Current FW version is newer than OTA version. No need to update.");
							mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(false);
						}
					} else {
						Log.d(TAG, "Result " + fwVersion + " is not VALID version string.");
					}
					reader.close();
				} else {
					Log.d(TAG, "Firmware version on server: " + fwVersion);
					Log.d(TAG, "Firmware version on camera: " + mCurrentFwVersion);
					Log.d(TAG, "No new firmware version available on OTA server.");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		else
		{

			String sendCommandResponse = CommandUtils.sendCommand(mDevice, CHECK_FW_UPGRADE_COMMAND, false);
			if (sendCommandResponse != null)
			{
				String versionResponse = null;
				final Pair<String, Object> response = CommonUtil.parseResponseBody(sendCommandResponse);

				if(BuildConfig.DEBUG)
				{
					Log.d(TAG,"check firmware upgrade value :- " + response.second);
				}

				if (response.second instanceof String)
				{
					versionResponse = (String)response.second;

					if(BuildConfig.DEBUG)
					{
						Log.d(TAG,"Response :- " +versionResponse );
					}

					Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)").matcher(versionResponse);
					if(matcher.matches())
					{
						// Issue is present on firmware. UAM-2160
						if(Util.isThisVersionGreaterThan(versionResponse,mCurrentFwVersion)) {
							mCheckFirmwareUpdateResult.setNewFirmwareVersion(versionResponse);
							mCheckFirmwareUpdateResult.setFirmwareResidedInCamera(true);

							mCheckFirmwareUpdateResult.setOTAVersion(versionResponse);
							mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(true);
						}
						else
						{
							mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(false);
						}
					}
					else
					{
						if(BuildConfig.DEBUG)
						{
							Log.d(TAG,"failed to match pattern :- " +versionResponse );
						}
						if(!versionResponse.contains(","))
						{
							try {
								switch (Integer.parseInt(versionResponse))
								{
									case DEVICE_FIRMWARE_UPGRADE_STATUS_FAIL:
										mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(false);
										break;

									case DEVICE_FIRMWARE_UPGRADE_STATUS_IN_PROGRESS:
										mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(false);
										break;

									case DEVICE_FIRMWARE_UPGRADE_STATUS_NA:
										mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(false);
										break;
								}
							}
							catch(NumberFormatException e)
							{
								Log.e(TAG,e.getMessage());
							}
						}
					}

				}
				else if(response.second instanceof Integer)
				{
					Integer otaStatus = (Integer)response.second;
					try
					{
						switch (otaStatus)
						{
							case DEVICE_FIRMWARE_UPGRADE_STATUS_FAIL:
								mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(false);
								break;

							case DEVICE_FIRMWARE_UPGRADE_STATUS_IN_PROGRESS:
								mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(false);
								break;

							case DEVICE_FIRMWARE_UPGRADE_STATUS_NA:
								mCheckFirmwareUpdateResult.setHaveNewFirmwareVersion(false);
								break;
						}
					}
					catch(NumberFormatException e)
					{
						Log.e(TAG,e.getMessage());
					}
				}
			}

		}
		return mCheckFirmwareUpdateResult;
	}

	public static boolean isVersionString(String versionStr) {
		boolean result = false;
		String versionRegex = "^\\d+\\.\\d+\\.\\d+$";
		Pattern p = Pattern.compile(versionRegex);
		Matcher m = p.matcher(versionStr);
		while (m.find()) {
			result = true;
		}
		return result;
	}

	public static boolean isDevDevice(String url, final String udid) {
		try {
			String devDeviceList = Ion.with(HubbleApplication.AppContext.getApplicationContext())
					.load(url)
					.noCache()
					.setLogging(TAG, Log.DEBUG
					)
					.asString().get();
			if (devDeviceList != null) {
				String[] devicesUdid = devDeviceList.split("\n");
				for (String deviceUdid : devicesUdid) {
					if (deviceUdid.startsWith(udid)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String toHexString(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static final String md5(byte[] byteArray) {
		final String MD5 = "MD5";
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance(MD5);
			digest.update(byteArray);
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

}