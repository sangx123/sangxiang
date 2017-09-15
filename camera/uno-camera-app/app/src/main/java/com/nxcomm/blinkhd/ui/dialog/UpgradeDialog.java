package com.nxcomm.blinkhd.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.devcomm.Device;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceID;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceDetail;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceDetailsResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceWakeupResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.registration.JDownloader;
import com.hubble.registration.JUploader;
import com.hubble.registration.JWebClient;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubble.registration.tasks.CheckFirmwareUpdateTask;
import com.hubble.registration.ui.CommonDialog;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.BuildConfig;
import com.hubbleconnected.camera.R;
import com.koushikdutta.async.future.FutureCallback;
import com.nxcomm.blinkhd.ui.IUpgradeCallback;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.sensor.ui.UpgradeSensorActivity;
import com.util.DeviceWakeup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.devices.SerializableDeviceProfile;

import static com.hubble.registration.Util.getRootRecordingDirectory;

public class UpgradeDialog extends CommonDialog {
  private static final String TAG = "UpgradeDialog";
  private Device mDevice;
  private TextView mTextViewCurrentTask;
  private ProgressBar mProgressBar;
  private CheckFirmwareUpdateResult mCheckFirmwareUpdateResult;
  private TextView mTextViewUpgradeSucceded;
  private int percentCompleted = -10;
  private boolean mRequestUpgradeOnly;
  private static final String REQUEST_FW_UPGRADE = "request_fw_upgrade";
  private static final String REQUEST_FW_UPGRADE_OK = "request_fw_upgrade: 0";

  private static final long TOTAL_FLASH_TIME_WAIT = 240 * 1000;
  private static final long FLASH_PROCESS_WAIT_TIME = 120 * 1000;
  private static final long WAKEUP_WAIT_TIME = 25 * 1000;

  private Status mStatus;
  private Button mNegativeButton, mPositiveButton;
  private AlertDialog mDialog;
  private JDownloader downloader;
  private boolean started = false;
  private IUpgradeCallback mUpgradeCallback;
  private static final long UPGRADE_TIMEOUT_DELAY = 140 * 1000;

  public enum Status {
    DOWNLOADING_NEW_FIRMWARE_FROM_OTA_SERVER, UPLOADING_FIRMWARE_TO_DEVICE,WAKEUP_DEVICE, CAMERA_IS_UPGRADING,CAMREA_UPDATE_SUCCEEDED, CAMREA_UPDATE_FAILED, TIME_OUT, WAITING_RECOVERY
  }

  public UpgradeDialog() {
    super();
  }

  public UpgradeDialog(Device device, CheckFirmwareUpdateResult result, IUpgradeCallback upgradeCallback) {
    mDevice = device;
    mCheckFirmwareUpdateResult = result;
    mUpgradeCallback = upgradeCallback;
  }

  @Override
  public AlertDialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater(); // Get the layout inflater

    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    contentView = inflater.inflate(R.layout.upgrade_layout, null);
    builder.setView(contentView);

    // build type face
    mTextViewCurrentTask = (TextView) contentView.findViewById(R.id.textViewCurrentTask);

    mProgressBar = (ProgressBar) contentView.findViewById(R.id.prgBar);
    mProgressBar.setMax(100);

    mTextViewUpgradeSucceded = (TextView) contentView.findViewById(R.id.textViewUpgradeSucceeded);

    builder.setTitle(R.string.upgrading_firmware);
    builder.setNegativeButton(R.string.Cancel, null);
    builder.setPositiveButton(R.string.OK, null);

    mDialog = builder.create();

    return mDialog;
  }

  @Override
  public void onStart() {
    super.onStart();
    mPositiveButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
    mNegativeButton = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
    start();
  }

  public String getFirmwareBinaryCachedPath(String firmwareFile)
  {
    if(getActivity() != null)
    {
      File firmwareBinaryFile = new File(Util.getFirmwareDirectory(), firmwareFile);

      if(BuildConfig.DEBUG)
        Log.d(TAG,"Upload Cache directory path:- " + firmwareBinaryFile.getAbsolutePath());

      return firmwareBinaryFile.getAbsolutePath();
    }
    return "";
  }

  private void start() {
    if (started == false) {
      started = true;
      try {
        if (mDevice.getProfile().modelId.equals("0001")) {
          final String nameFile = mCheckFirmwareUpdateResult.getNewFirmwareFileName();
          if (nameFile != null && isFirmwareBinaryCachedMtag(nameFile)) {
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                dismiss();
                Intent intent = new Intent(getActivity(), UpgradeSensorActivity.class);
                intent.putExtra(UpgradeSensorActivity.REG_ID, mDevice.getProfile().registrationId);
                intent.putExtra(UpgradeSensorActivity.FW_NAME, mCheckFirmwareUpdateResult.getNewFirmwareVersion());
                startActivity(intent);
              }
            });
          } else {
            downloadFirmware(mCheckFirmwareUpdateResult.getFirmwareDownloadLink());
          }
        }
        else
        {
          if(mCheckFirmwareUpdateResult.isDeviceOTA())
          {
            checkDeviceStatus();
          }
          else if (mCheckFirmwareUpdateResult.isLocalCamera()) {
            String md5 = mCheckFirmwareUpdateResult.getNewFirmwareMD5();
            if (md5 != null && isFirmwareBinaryCached(mDevice.getProfile().getModelId(), md5)) {
              pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(), getFirmwareBinaryCachedPath(buildFirmwareName(mDevice.getProfile().getModelId(),md5)), mCheckFirmwareUpdateResult.getNewFirmwareFileName());
            } else {
              downloadFirmware(mCheckFirmwareUpdateResult.getFirmwareDownloadLink());
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onDetach() {
    if (downloader != null) {
      downloader.deleteObservers();
    }
    super.onDetach();
    started = false;
  }

  private void downloadFirmware(String url) throws MalformedURLException {
    setStatus(Status.DOWNLOADING_NEW_FIRMWARE_FROM_OTA_SERVER);
    URL fwLink = new URL(url);
    String firmwareFileName = buildFirmwareName(mDevice.getProfile().modelId,mCheckFirmwareUpdateResult.getNewFirmwareMD5());
    if (firmwareFileName == null) {
      firmwareFileName = "tempfirmware";
    }
    if (mDevice.getProfile().modelId.equals(CheckFirmwareUpdateTask.MTAG_MODEL)) {
      firmwareFileName = mCheckFirmwareUpdateResult.getNewFirmwareFileName();
    }
    Log.i(TAG, "Firmware url: " + url);
    downloader = new JDownloader(fwLink, Util.getFirmwareDirectory(), firmwareFileName);
    downloader.addObserver(new Observer() {
      @Override
      public void update(Observable arg0, Object arg1) {
        Runnable updateUIRunnable = null;

        if (downloader.getStatus() == JDownloader.DOWNLOADING) {
          updateUIRunnable = new Runnable() {
            @Override
            public void run() {
              if (mProgressBar != null) {
                mProgressBar.setProgress((int) downloader.getProgress());
              }

              if (mTextViewCurrentTask != null) {
                String msg = getString(R.string.downloading_new_firmware_message);
                msg = msg + " " + String.format(getString(R.string.upgrading_firmware_do_not_power_off_the_camera), Integer.valueOf((int) downloader.getProgress()));
                mTextViewCurrentTask.setText(msg);
              }
            }
          };
        } else if (downloader.getStatus() == JDownloader.ERROR) {
          updateUIRunnable = new Runnable() {
            @Override
            public void run() {
              Util.deleteFile(getFirmwareBinaryCachedPath(buildFirmwareName(PublicDefine.getModelIdFromRegId(mCheckFirmwareUpdateResult.getRegID()), mCheckFirmwareUpdateResult.getNewFirmwareMD5())));
              if (mTextViewCurrentTask != null) {
                mTextViewCurrentTask.setText(getString(R.string.download_firmware_error));
                setCancelable(true);
                try {
                  mPositiveButton.setVisibility(View.VISIBLE);
                  mNegativeButton.setVisibility(View.VISIBLE);
                  mPositiveButton.setText(R.string.OK);
                  mNegativeButton.setText(R.string.Cancel);
                  mPositiveButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                      started = false;
                      start();
                    }
                  });
                  mNegativeButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      dismiss();
                    }
                  });
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              }
            }
          };
        } else if (downloader.getStatus() == JDownloader.COMPLETE) {
          if (mDevice.getProfile().modelId.equals("0001")) {
            updateUIRunnable = new Runnable() {
              @Override
              public void run() {
                Log.e("FW", "start new active");
                dismiss();
                Intent intent = new Intent(getActivity(), UpgradeSensorActivity.class);
                intent.putExtra(UpgradeSensorActivity.REG_ID, mDevice.getProfile().registrationId);
                intent.putExtra(UpgradeSensorActivity.FW_NAME, mCheckFirmwareUpdateResult.getNewFirmwareVersion());
                startActivity(intent);
              }
            };
          }
          else if (PublicDefine.getModelIdFromRegId(mCheckFirmwareUpdateResult.getRegID()).compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0)
          {
            updateUIRunnable = new Runnable() {
              @Override
              public void run() {
                checkDeviceStatus();
              }
            };
          }
          else {
            pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(), downloader.getSavedFilePath(), mCheckFirmwareUpdateResult.getNewFirmwareFileName());
            updateUIRunnable = new Runnable() {
              @Override
              public void run() {
                if (mProgressBar != null) {
                  mProgressBar.setProgress(0);
                }
              }
            };
          }

        }

        if (updateUIRunnable != null) {
          getActivity().runOnUiThread(updateUIRunnable);
        }
      }
    });
  }

  private void wakeupDevice()
  {
    DeviceID deviceID = new DeviceID(mCheckFirmwareUpdateResult.getApiKey(), mCheckFirmwareUpdateResult.getRegID());
    DeviceManagerService.getInstance(getActivity()).wakeUpDevice(deviceID, new Response.Listener<DeviceWakeupResponse>()
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

  private void checkDeviceStatus()
  {
    setStatus(Status.WAKEUP_DEVICE);
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
  private void wakeupDevice(boolean wakeupDevice)
  {
    if(wakeupDevice && getActivity() != null)
    {
      DeviceWakeup deviceWakeup = DeviceWakeup.newInstance();
      deviceWakeup.wakeupDevice(mCheckFirmwareUpdateResult.getRegID(),mCheckFirmwareUpdateResult.getApiKey(),mDeviceHandler);

    }
    else {

      if(mCheckFirmwareUpdateResult.isDeviceOTA())
      {
        upgradeDevice();
      }
      else {
        pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(), downloader.getSavedFilePath(), mCheckFirmwareUpdateResult.getNewFirmwareFileName());

        if (mProgressBar != null) {
          mProgressBar.setProgress(0);
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

          if(BuildConfig.DEBUG)
            Log.d(TAG, "Device status task completed..device status:" + result);

          if (result)
          {
            if(mCheckFirmwareUpdateResult.isDeviceOTA())
            {
              upgradeDevice();
            }
            else {
              pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(),
                      downloader.getSavedFilePath(), mCheckFirmwareUpdateResult.getNewFirmwareFileName());
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

  private void pushSignatureToFirmware(final String uploadURL, final byte[] signatureData, final String signatureFileName, final FutureCallback<Boolean> callback) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        JUploader uploader = new JUploader(uploadURL, signatureData, signatureFileName, 0);
        uploader.setModelId(mDevice.getProfile().getModelId());
        String result = uploader.uploadFile(null);
        if ((result.indexOf("Signature updated!") > 0) || (result.indexOf("Upload digital signature success")>0)) {
          callback.onCompleted(null, true);
        } else {
          callback.onCompleted(null, false);
        }

      }
    }).start();
  }

  private void pushFirmwareToCamera(final String uploadURL, final String filePath, final String fileName) {
    FutureCallback<Boolean> callback = new FutureCallback<Boolean>() {
      @Override
      public void onCompleted(Exception e, Boolean result) {
        if (getActivity() == null) {
          return;
        }
        if (e != null) {
          Log.e(TAG, "Upload signature error");
          getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(getActivity(), R.string.incorrect_fw_signature, Toast.LENGTH_SHORT).show();
            }
          });
        } else {
          if (result) {
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Log.d(TAG, "Upload signature ok, upload firmware now");
                _pushFirmwareToCamera(uploadURL, filePath, fileName);
              }
            });
          } else {
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(getActivity(), R.string.incorrect_fw_signature, Toast.LENGTH_SHORT).show();
              }
            });
          }
        }
      }
    };

    if (Util.isUseSignatureForFwUpgrade(mDevice.getProfile().getModelId(), mCheckFirmwareUpdateResult.getCurrentFirmwareVersion())) {
      Log.i(TAG, "Camera firmware version is " + mCheckFirmwareUpdateResult.getCurrentFirmwareVersion() + " ==> so upload signature data first");
      pushSignatureToFirmware(mCheckFirmwareUpdateResult.getUploadFwURL(), mCheckFirmwareUpdateResult.getSignatureData(), mCheckFirmwareUpdateResult.getSignatureFileName(), callback);
    } else {
      _pushFirmwareToCamera(uploadURL, filePath, fileName);
    }
  }

  private void _pushFirmwareToCamera(String uploadURL, String filePath, String fileName) {
    try {
      final JUploader uploader = new JUploader(uploadURL, filePath, fileName);
      uploader.setModelId(mDevice.getProfile().getModelId());
      setStatus(Status.UPLOADING_FIRMWARE_TO_DEVICE);

      uploader.addObserver(new Observer() {
        @Override
        public void update(Observable arg0, Object arg1) {
          Runnable updateUIRunnable = null;
          if (uploader.getStatus() == JUploader.UPLOADING) {
            updateUIRunnable = new Runnable() {
              @Override
              public void run() {
                if (mProgressBar != null) {
                  mProgressBar.setProgress((int) uploader.getPercent());
                }
              }
            };
          } else if (uploader.getStatus() == JUploader.COMPLETE) {
            waitForLocalUpgradeCompleted();
          } else if (uploader.getStatus() == JUploader.ERROR) {
            updateUIRunnable = new Runnable() {
              @Override
              public void run() {
                if (mTextViewCurrentTask != null) {
                  mTextViewCurrentTask.setText(getString(R.string.sending_firmware_to_camera_failed));
                  getDialog().setCancelable(true);
                  mNegativeButton.setVisibility(View.VISIBLE);
                  mPositiveButton.setVisibility(View.VISIBLE);
                  mPositiveButton.setText(R.string.OK);
                  mPositiveButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(), downloader.getSavedFilePath(), mCheckFirmwareUpdateResult.getNewFirmwareFileName());
                    }
                  });
                  mNegativeButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      dismiss();
                    }
                  });
                }
              }
            };
          } else if (uploader.getStatus() == JUploader.TIMEOUT) {
            updateUIRunnable = new Runnable() {
              @Override
              public void run() {
                if (mTextViewCurrentTask != null) {
                  mTextViewCurrentTask.setText(getString(R.string.sending_firmware_to_camera_timeout));
                  getDialog().setCancelable(true);
                  mNegativeButton.setVisibility(View.VISIBLE);
                  mPositiveButton.setVisibility(View.GONE);
                  mNegativeButton.setText(R.string.OK);
                  mNegativeButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      dismiss();
                    }
                  });
                }
              }
            };
          }

          if (updateUIRunnable != null) {
            getActivity().runOnUiThread(updateUIRunnable);
          }
        }
      });
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


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
            if (PublicDefine.getModelIdFromRegId(mCheckFirmwareUpdateResult.getRegID()).compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0)
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
                setStatus(Status.CAMREA_UPDATE_SUCCEEDED);

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
          setStatus(Status.CAMREA_UPDATE_FAILED);
        }
      }
    });
    waitForUpgradeComplete.start();
  }

  private void waitForLocalUpgradeCompleted() {
    setStatus(Status.CAMERA_IS_UPGRADING);

    final String burningProcessURL = mCheckFirmwareUpdateResult.getBurningProgressURL();
    final String BURNING_PROCESS = "burning_process :";

    final String versionURL = mCheckFirmwareUpdateResult.getKeepAliveURL();
    final String GET_VERSION = "get_version: ";

    Thread waitForUpgradeComplete = new Thread(new Runnable() {
      @Override
      public void run() {
        int i = 0;
        int numberOfFailed = 0;
        long endTime = System.currentTimeMillis() + TOTAL_FLASH_TIME_WAIT;
        boolean isConnectionException = false;

        while (System.currentTimeMillis() < endTime) {
          try {

            if(PublicDefine.getModelIdFromRegId(mCheckFirmwareUpdateResult.getRegID()).compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0)
            {
              if(i==0) {
                try {
                  Thread.sleep(FLASH_PROCESS_WAIT_TIME);
                } catch (InterruptedException e) {
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

              if(availableVersion.startsWith(GET_VERSION))
              {
                String newVersion = availableVersion.replace(GET_VERSION, "");

                if(BuildConfig.DEBUG)
                  Log.d(TAG,"new version :- " +newVersion + " and ota version :- "+mCheckFirmwareUpdateResult.getOTAVersion());


                if(!Util.isThisVersionGreaterThan(mCheckFirmwareUpdateResult.getOTAVersion(),
                        newVersion))
                {
                  setStatus(Status.CAMREA_UPDATE_SUCCEEDED);
                  break;
                }
              }
            }
            else {
              String burningProcess = JWebClient.downloadAsString(burningProcessURL);
              Log.i(TAG, "Burning process result: " + burningProcess);
              if (burningProcess.startsWith(BURNING_PROCESS)) {
                if (numberOfFailed > 40) { // upgrade failed
                  break;
                }

                String strBurningPercent = burningProcess.replace(BURNING_PROCESS, "");
                int burningPercent = Integer.parseInt(strBurningPercent);
                if (burningPercent == -1) {
                  numberOfFailed++;
                } else if (burningPercent >= 0) {
                  if (burningPercent > 100) {
                    burningPercent = 100;
                  }

                  int displayValue = mDevice.getProfile().getModelId().equals("0877")
                          ? burningPercent / 2 : burningPercent;
                  setUpgradeProgress(displayValue);

                  if (burningPercent == 100) {
                    if ("0877".equals(mDevice.getProfile().getModelId())) {
                      setStatus(Status.WAITING_RECOVERY);
                    } else {
                      setStatus(Status.CAMREA_UPDATE_SUCCEEDED);
                    }
                    break;
                  }
                }
              }
            }
          } catch (SocketException ex) {
            if(BuildConfig.DEBUG)
              Log.d(TAG,"connection is failed");
            isConnectionException = true;
            ex.printStackTrace();
          } catch (IOException e) {
            if(BuildConfig.DEBUG)
              Log.d(TAG,"io connection is failed");
            isConnectionException = true;
            e.printStackTrace();
          } catch (Exception e) {
            e.printStackTrace();
          }

          try {
            i++;
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        // 10 min passed, but camera upgrade still was not done. Recheck on server...
        if (getStatus() == Status.WAITING_RECOVERY) {
          Log.d(TAG, "waiting for recovery in 3 minutes ....");
          // prevent after burning, user has to wait 3 minutes at 100% -> very inhibited
          // solution: just show 50% after that increase 5% every 18s. total 10 times
          int count = 0;
          do {
            try {
              Thread.sleep(18000);
            } catch (InterruptedException e) {
            }
            int displayValue = 50 + (count + 1) * 5;
            if (displayValue > 100) {
              displayValue = 100;
            }
            setUpgradeProgress(displayValue);
          } while (++count < 10);
          Log.d(TAG, "waiting for recovery in 3 minutes .... DONE");
          checkUpgradeResultOnServer();
        } else {
          if (getStatus() != Status.CAMREA_UPDATE_SUCCEEDED) {
            checkUpgradeResultOnServer();
          }
        }
      }
    });
    waitForUpgradeComplete.start();
  }

  private void changeStatusText(final int strID) {
    if (getActivity() == null) {
      return;
    }

    getActivity().runOnUiThread(new Runnable() {

      @Override
      public void run() {
        try {
          mTextViewCurrentTask.setText(getString(strID));
          mPositiveButton.setVisibility(View.GONE);
          mNegativeButton.setVisibility(View.GONE);

          if (strID == R.string.downloading_new_firmware_message) {
            getDialog().setTitle(R.string.downloading_new_firmware_title);
            getDialog().setCanceledOnTouchOutside(false);
            mPositiveButton.setVisibility(View.GONE);
            mNegativeButton.setVisibility(View.VISIBLE);
            mNegativeButton.setText(R.string.Cancel);
            mNegativeButton.setOnClickListener(new OnClickListener() {

              @Override
              public void onClick(View v) {
                try {
                  downloader.cancel();
                  mDialog.dismiss();
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              }
            });
          } else if (strID == R.string.upload_new_firmware_to_camera) {
            getDialog().setTitle(R.string.upload_firmware);
            getDialog().setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);
            mPositiveButton.setVisibility(View.GONE);
            mNegativeButton.setVisibility(View.GONE);
          } else if (strID == R.string.camera_is_updating_to_new_firmware) {
            getDialog().setTitle(R.string.upgrading_firmware);
            getDialog().setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);

            if (mProgressBar != null) {
              if(mDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)==0)
              {
                mProgressBar.setIndeterminate(true);
              }
              else {
                mProgressBar.setIndeterminate(false);
                mProgressBar.setProgress(0);
              }
            }
          }
          else if(strID == R.string.camera_wake_up)
          {
            getDialog().setTitle(R.string.wakeup_device);
            if (mProgressBar != null) {
              mProgressBar.setProgress(0);
              mProgressBar.setIndeterminate(true);
            }
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
  }

  private void checkUpgradeResultOnServer() {
    try {
      Log.i(TAG, "After upgraded, device is online now");
      String fw = null;
      if ("0877".equals(mDevice.getProfile().getModelId())) {
        Log.i(TAG, "Checking fw via http command");
        fw = JWebClient.downloadAsString(mCheckFirmwareUpdateResult.getKeepAliveURL());
        if (!TextUtils.isEmpty(fw)) {
          fw = fw.replace("get_version: ", "");
        }
      }
      if (TextUtils.isEmpty(fw))
      {
        Log.i(TAG, "Checking fw via request api");
        Models.ApiResponse<SerializableDeviceProfile> deviceProfile = Api.getInstance().getService()
            .getDeviceProfile(mCheckFirmwareUpdateResult.getApiKey(), mCheckFirmwareUpdateResult.getRegID());

        if (deviceProfile.getStatus().equalsIgnoreCase("200") && deviceProfile.getData().getFirmwareVersion() != null) {
          fw = deviceProfile.getData().getFirmwareVersion();
        } else {
          Log.i(TAG, "After upgrade, check device information from server error");
        }
      }
      if (mCheckFirmwareUpdateResult.getOTAVersion().equalsIgnoreCase(fw)) {
          Log.i(TAG, "After upgrade, device has new version = ota version => succeeded.");
          setStatus(Status.CAMREA_UPDATE_SUCCEEDED);
        } else {
          Log.i(TAG, "After upgrade, device has new version != ota version => failed. Device firmware version is: " + fw);
          setStatus(Status.CAMREA_UPDATE_FAILED);
        }
    } catch (Exception ex) {
      ex.printStackTrace();
      setStatus(Status.CAMREA_UPDATE_FAILED);
    }
  }

  private boolean isFirmwareBinaryCached(String cameraModel, String fwVersion) {
    String fwDirectory = Util.getFirmwareDirectory();
    String fwFileName = buildFirmwareName(cameraModel, fwVersion);

    File fwFile = new File(fwDirectory + File.separator + fwFileName);

    if (fwFile.exists()) {
      return true;
    } else {
      return false;
    }
  }

  private boolean isFirmwareBinaryCachedMtag(String fileName) {
    File fwFile = new File(getRootRecordingDirectory() + File.separator + "fw" + File.separator + fileName);
   // File fwFile = new File(getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + fileName);
    if (fwFile.exists()) {
      return true;
    } else {
      return false;
    }
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

  public Status getStatus() {
    return mStatus;
  }

  public void setStatus(Status mStatus) {
    this.mStatus = mStatus;
    if (mStatus == Status.DOWNLOADING_NEW_FIRMWARE_FROM_OTA_SERVER) {
      changeStatusText(R.string.downloading_new_firmware_message);
    } else if (mStatus == Status.UPLOADING_FIRMWARE_TO_DEVICE) {
      changeStatusText(R.string.upload_new_firmware_to_camera);
    } else if (mStatus == Status.CAMERA_IS_UPGRADING) {
      changeStatusText(R.string.camera_is_updating_to_new_firmware);
    }
    else if(mStatus == Status.WAKEUP_DEVICE)
    {
      changeStatusText(R.string.camera_wake_up);
    }
    else if (mStatus == Status.CAMREA_UPDATE_SUCCEEDED) {
      changeToSuccessLayout();
    } else if (mStatus == Status.CAMREA_UPDATE_FAILED) {
      changeToFailLayout();
    } else if (mStatus == Status.TIME_OUT) {
      changeToTimeOutLayout();
    }
  }

  private void setUpgradeProgress(final int progress) {
    if (getActivity() == null)
      return;

    getActivity().runOnUiThread(new Runnable() {

      @Override
      public void run() {
        try {
          mProgressBar.setIndeterminate(false);
          mProgressBar.setProgress(progress);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
  }

  private void changeToFailLayout() {
    if (getActivity() == null) {
      return;
    }

    getActivity().runOnUiThread(new Runnable() {

      @Override
      public void run() {
        try {
          mTextViewUpgradeSucceded.setVisibility(View.VISIBLE);
          mTextViewUpgradeSucceded.setText(R.string.upgrade_fw_failed);
          mProgressBar.setVisibility(View.GONE);
          mTextViewCurrentTask.setVisibility(View.GONE);

          if (mPositiveButton != null) {
            mPositiveButton.setVisibility(View.VISIBLE);
            mPositiveButton.setText(R.string.OK);
            mPositiveButton
                .setOnClickListener(new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    try {
                      mDialog.dismiss();
                      mUpgradeCallback.onUpgradeFail();
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                  }
                });
          }

          setCancelable(true);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });

  }

  private void changeToSuccessLayout() {
    if (getActivity() == null) {
      return;
    }

    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        try {
          mTextViewUpgradeSucceded.setVisibility(View.VISIBLE);
          mTextViewUpgradeSucceded.setText(R.string.upgrade_fw_done);
          mProgressBar.setVisibility(View.GONE);
          mTextViewCurrentTask.setVisibility(View.GONE);

          if (mPositiveButton != null) {
            mPositiveButton.setVisibility(View.VISIBLE);
            mPositiveButton.setText(R.string.OK);
            mPositiveButton.setOnClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                try {
                  mDialog.dismiss();
                  mUpgradeCallback.onUpgradeSucceed();
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              }
            });
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
  }

  private void changeToTimeOutLayout() {
    if (getActivity() == null) {
      return;
    }

    getActivity().runOnUiThread(new Runnable() {

      @Override
      public void run() {
        try {
          mTextViewUpgradeSucceded.setVisibility(View.VISIBLE);
          mTextViewUpgradeSucceded.setText(R.string.sending_firmware_to_camera_timeout);
          mProgressBar.setVisibility(View.GONE);
          mTextViewCurrentTask.setVisibility(View.GONE);

          if (mPositiveButton != null) {
            mPositiveButton.setVisibility(View.VISIBLE);
            mPositiveButton.setText(R.string.OK);
            mPositiveButton
                .setOnClickListener(new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    try {
                      mDialog.dismiss();
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                  }
                });
          }

          setCancelable(true);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });

  }
}
