package com.firmware;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.HubbleApplication;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.registration.JDownloader;
import com.hubble.registration.JUploader;
import com.hubble.registration.JWebClient;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.registration.tasks.CheckFirmwareUpdateTask;
import com.hubble.registration.ui.CommonDialog;
import com.hubbleconnected.camera.R;
import com.util.AppEvents;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

public class UpgradeDialogFirmware extends CommonDialog {
  private static final String TAG = "UpgradeDialogFirmware";
  private TextView mTextViewCurrentTask;
  private TextView mTextViewPercent;
  private ProgressBar mProgressBar;
//  private CheckFirmwareUpdateResult mCheckFirmwareUpdateResult;
  private TextView mTextViewUpgradeSucceded;
  private int percentCompleted = -10;
  private boolean mRequestUpgradeOnly;
  private static final String REQUEST_FW_UPGRADE = "request_fw_upgrade";
  private static final String REQUEST_FW_UPGRADE_OK = "request_fw_upgrade: 0";
  private static final long FIVE_MINUTES = 60 * 1000 * 5;
  private static final long SIX_MINUTES = 60 * 1000 * 6;
  private Status mStatus;
  private Button mNegativeButton, mPositiveButton;
  private AlertDialog mDialog;
  private JDownloader downloader;
  private boolean started = false;
  private IUpgradeCallbackFirmware mUpgradeCallback;
  private static final String UPLOAD_FW_URL_PATTERN = "http://192.168.193.1:8080/cgi-bin/haserlupgrade.cgi";
  private String firmwareVersion;
  private String md5;
  private Activity activity;
  private String modelId;
  private boolean isFW17;
  private String currentFWUpgrade;

  public enum Status {
    DOWNLOADING_NEW_FIRMWARE_FROM_OTA_SERVER, UPLOADING_FIRMWARE_TO_DEVICE, CAMERA_IS_UPGRADING, CAMREA_UPDATE_SUCCEEDED, CAMREA_UPDATE_FAILED
  }

  public UpgradeDialogFirmware() {
    super();
  }

  public UpgradeDialogFirmware(Activity activity, String firmwareVersion, String modelId, boolean isFW17, IUpgradeCallbackFirmware upgradeCallback) {
    this.activity = activity;
    this.firmwareVersion = firmwareVersion;
    mUpgradeCallback = upgradeCallback;
    md5 = "";
    this.modelId = modelId;
    this.isFW17 = isFW17;
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
    mTextViewPercent = (TextView) contentView.findViewById(R.id.tv_percent);

    mProgressBar = (ProgressBar) contentView.findViewById(R.id.prgBar);
    mProgressBar.setMax(100);

    mTextViewUpgradeSucceded = (TextView) contentView.findViewById(R.id.textViewUpgradeSucceeded);

    builder.setTitle(R.string.firmware_update);
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

  public String getFirmwareBinaryCachedPath(String md5) {
    File firmwareBinaryFile = new File(Util.getFirmwareDirectory(), md5);
    return firmwareBinaryFile.getAbsolutePath();
  }

  private void start() {
    if (started == false) {
      started = true;
      try {
          if (firmwareVersion != null && isFirmwareBinaryCached(modelId, firmwareVersion)) {
            if (activity instanceof FirmwareCamConfigureActivity) {
              if (isFW17) {
                currentFWUpgrade = "01.17.00";
                pushFirmwareToCamera(UPLOAD_FW_URL_PATTERN, getFirmwareBinaryCachedPath(buildFirmwareName(modelId, "01.17.00")),buildFirmwareName(modelId, "01.17.00"));
              } else {
                currentFWUpgrade = firmwareVersion;
                pushFirmwareToCamera(UPLOAD_FW_URL_PATTERN, getFirmwareBinaryCachedPath(buildFirmwareName(modelId, firmwareVersion)),buildFirmwareName(modelId, firmwareVersion));
              }
            } else {
              ((FirmwareUpdateActivity) activity).switchToCamConfigureActivity();
            }
          } else {
            if (modelId.equals("0086")) {
              downloadFirmware(String.format(PublicDefine.FIRMWARE_DOWNLOAD_LINK_URL_0086_PATTERN, modelId, modelId, firmwareVersion));
            } else {
              if (firmwareVersion.equals("01.17.00")) {
                downloadFirmware(String.format(PublicDefine.FIRMWARE_011700_PATTERN, modelId, modelId, firmwareVersion));
              } else {
                downloadFirmware(String.format(PublicDefine.FIRMWARE_DOWNLOAD_LINK_URL_0854_PATTERN, modelId, modelId, firmwareVersion));
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
    String nameFile = modelId + "-" + firmwareVersion;
    if (nameFile == null) {
      nameFile = "tempfirmware";
    }
    String fileFw = nameFile + ".tar.gz";
    if (modelId.equals("0086")) {
      fileFw = nameFile + ".tar";
    }
    downloader = new JDownloader(fwLink, Util.getFirmwareDirectory(), fileFw);
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
                msg = msg + " " + getString(R.string.download_firmware_do_not_power_off_the_camera);
                mTextViewCurrentTask.setText(msg);
                mTextViewPercent.setVisibility(View.VISIBLE);
                mTextViewPercent.setText(String.format(getString(R.string.percent_download), Integer.valueOf((int) downloader.getProgress()), "%"));
              }
            }
          };
        } else if (downloader.getStatus() == JDownloader.ERROR) {
          updateUIRunnable = new Runnable() {
            @Override
            public void run() {
              if (mTextViewCurrentTask != null) {
                if (mTextViewPercent != null) {
                  mTextViewPercent.setVisibility(View.GONE);
                }

                mTextViewCurrentTask.setText(getString(R.string.download_firmware_error));
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_FW_DOWNLOAD_FAILED+" failed :"+JDownloader.ERROR,AppEvents.CAMERA_FW_DOWNLOAD_FAILED);

                ZaiusEvent cameraFwDownloadeEvt = new ZaiusEvent(AppEvents.CAMERA_FW_DOWNLOAD);
                cameraFwDownloadeEvt.action(AppEvents.CAMERA_FW_DOWNLOAD_FAILED+"failed :"+JDownloader.ERROR);
                try {
                  ZaiusEventManager.getInstance().trackCustomEvent(cameraFwDownloadeEvt);
                } catch (ZaiusException e) {
                  e.printStackTrace();
                }
                setCancelable(true);
                try {
                  Util.deleteFile(getFirmwareBinaryCachedPath(buildFirmwareName(modelId, firmwareVersion)));
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
                      Util.deleteFile(getFirmwareBinaryCachedPath(buildFirmwareName(modelId, firmwareVersion)));
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
          boolean is17 = isFirmwareBinaryCached(modelId, "01.17.00");
          if (is17 || modelId.equals("1662") || modelId.equals("0086") || modelId.equals("1854") || modelId.equals("0662")) {
            mUpgradeCallback.onDownloadSucceed();
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_FW_DOWNLOAD +" : success",AppEvents.SUCCESS);
            ZaiusEvent cameraFwDownloadEvt = new ZaiusEvent(AppEvents.CAMERA_FW_DOWNLOAD);
            cameraFwDownloadEvt.action(AppEvents.SUCCESS);
            try {
              ZaiusEventManager.getInstance().trackCustomEvent(cameraFwDownloadEvt);
            } catch (ZaiusException e) {
              e.printStackTrace();
            }

          } else {
            activity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(activity, getString(R.string.download_firmware_version_011700), Toast.LENGTH_LONG).show();
                try {
                  firmwareVersion = "01.17.00";
                  downloadFirmware(String.format(PublicDefine.FIRMWARE_011700_PATTERN, modelId, modelId, firmwareVersion));
                } catch (MalformedURLException e) {
                  e.printStackTrace();
                }
              }
            });

          }
        }

        if (updateUIRunnable != null) {
          activity.runOnUiThread(updateUIRunnable);
        }
      }
    });
  }

  private void pushFirmwareToCamera(String uploadURL,final String filePath, final String fileName) {
    try {
      final JUploader uploader = new JUploader(uploadURL, filePath, fileName);
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
                  mTextViewCurrentTask.setText(getString(R.string.update_firmware_to_camera_failed));
                  GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_FW_UPLOAD_FAILURE+" : "+JUploader.ERROR,AppEvents.CAMERA_FW_UPLOAD_FAILURE);

                  ZaiusEvent cameraFwUploadEvt = new ZaiusEvent(AppEvents.CAMERA_FW_UPLOAD_FAILURE);
                  cameraFwUploadEvt.action("failure : "+JUploader.ERROR);
                  try {
                    ZaiusEventManager.getInstance().trackCustomEvent(cameraFwUploadEvt);
                  } catch (ZaiusException e) {
                    e.printStackTrace();
                  }

                  getDialog().setCancelable(true);
                  mNegativeButton.setVisibility(View.VISIBLE);
                  mPositiveButton.setVisibility(View.VISIBLE);
                  mPositiveButton.setText(R.string.OK);
                  mPositiveButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      pushFirmwareToCamera(UPLOAD_FW_URL_PATTERN, filePath, buildFirmwareName(modelId, currentFWUpgrade));
                    }
                  });
                  mNegativeButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      dismiss();
                      activity.finish();
                    }
                  });
                }
              }
            };
          }

          if (updateUIRunnable != null) {
            activity.runOnUiThread(updateUIRunnable);
          }
        }
      });
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void waitForLocalUpgradeCompleted() {
    setStatus(Status.CAMERA_IS_UPGRADING);

    final String burningProcessURL = PublicDefine.CMD_UPGRADE_FW;
    final String BURNING_PROCESS = "burning_process :";
    Thread waitForUpgradeComplete = new Thread(new Runnable() {
      @Override
      public void run() {
        int i = 0;
        int numberOfFailed = 0;
        long endTime = System.currentTimeMillis() + SIX_MINUTES;
        while (System.currentTimeMillis() < endTime) {
          try {
            String burningProcess = JWebClient.downloadAsString(burningProcessURL);

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

                setUpgradeProgress(burningPercent);

                if (burningPercent == 100) {
                  setStatus(Status.CAMREA_UPDATE_SUCCEEDED);
                  GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_DETAIL,AppEvents.CAMERA_FW_UPLOAD_SUCCESS+" : success",AppEvents.CAMERA_FW_UPLOAD_SUCCESS);

                  ZaiusEvent cameraFwUploadEvt = new ZaiusEvent(AppEvents.CAMERA_FW_UPLOAD_SUCCESS);
                  cameraFwUploadEvt.action(AppEvents.SUCCESS);
                  try {
                    ZaiusEventManager.getInstance().trackCustomEvent(cameraFwUploadEvt);
                  } catch (ZaiusException e) {
                    e.printStackTrace();
                  }

                  break;
                }
              }
            }
          } catch (SocketException ex) {
            ex.printStackTrace();
            changeToFailLayout();
          } catch (IOException e) {
            e.printStackTrace();
          } catch (NullPointerException ex){
            ex.printStackTrace();
            changeToFailLayout();
          }

          try {
            i++;
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        if (getStatus() != Status.CAMREA_UPDATE_SUCCEEDED) { // 5 min passed, but camera upgrade still was not done. Recheck on server...
          //update firmware done
//          mUpgradeCallback.onUpgradeSucceed();
        }
      }
    });
    waitForUpgradeComplete.start();
  }

  private void changeStatusText(final int strID) {
    if (activity == null) {
      return;
    }

    activity.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        try {
          mTextViewCurrentTask.setText(getString(strID));
          mPositiveButton.setVisibility(View.GONE);
          mNegativeButton.setVisibility(View.GONE);

          if (strID == R.string.downloading_new_firmware_message) {
            getDialog().setTitle(getString(R.string.downloading_new_firmware_title) + " " + firmwareVersion);
            getDialog().setCanceledOnTouchOutside(false);
            mPositiveButton.setVisibility(View.GONE);
            mNegativeButton.setVisibility(View.VISIBLE);
            mNegativeButton.setText(R.string.Cancel);
            mNegativeButton.setOnClickListener(new OnClickListener() {

              @Override
              public void onClick(View v) {
                try {
                  downloader.cancel();
                  Util.deleteFile(getFirmwareBinaryCachedPath(buildFirmwareName(modelId, firmwareVersion)));
                  mDialog.dismiss();
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              }
            });
          } else if (strID == R.string.upload_new_firmware_to_camera_not_turn_off) {
            getDialog().setTitle(getString(R.string.firmware_update) + " " + currentFWUpgrade);
            getDialog().setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);
            mPositiveButton.setVisibility(View.GONE);
            mNegativeButton.setVisibility(View.GONE);
          } else if (strID == R.string.camera_being_updated_to_new_firmware) {
            getDialog().setTitle(getString(R.string.firmware_update) + " " + currentFWUpgrade);
            getDialog().setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);

            if (mProgressBar != null) {
              mProgressBar.setProgress(0);
            }
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
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

  private String buildFirmwareName(String cameraModel, String fwVersion) {
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

    if(checkedSuffix == null) {
      String result = "%s-%s.tar.gz";
      if (cameraModel.equals("0086")) {
        result = "%s-%s.tar";
      }
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
      changeStatusText(R.string.upload_new_firmware_to_camera_not_turn_off);
    } else if (mStatus == Status.CAMERA_IS_UPGRADING) {
      changeStatusText(R.string.camera_being_updated_to_new_firmware);
    } else if (mStatus == Status.CAMREA_UPDATE_SUCCEEDED) {
      changeToSuccessLayout();
    } else if (mStatus == Status.CAMREA_UPDATE_FAILED) {
      changeToFailLayout();
    }
  }

  private void setUpgradeProgress(final int progress) {
    if (activity == null)
      return;

    activity.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        try {
          mProgressBar.setProgress(progress);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
  }

  private void changeToFailLayout() {
    if (activity == null) {
      return;
    }

    activity.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        try {
          mTextViewPercent.setVisibility(View.GONE);
          mTextViewUpgradeSucceded.setVisibility(View.VISIBLE);
          mTextViewUpgradeSucceded.setText(R.string.firmware_update_has_failed);
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
    if (activity == null) {
      return;
    }

    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        try {
          String message = getString(R.string.fw_upgrade_successfully);
          if (isFW17) {
            message = getString(R.string.re_pair_camera);
          }

          mTextViewPercent.setVisibility(View.GONE);
          mTextViewUpgradeSucceded.setVisibility(View.VISIBLE);
          mTextViewUpgradeSucceded.setText(message);
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
}
