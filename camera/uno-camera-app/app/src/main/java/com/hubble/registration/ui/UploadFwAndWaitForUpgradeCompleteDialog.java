package com.hubble.registration.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hubble.registration.JDownloader;
import com.hubble.registration.JUploader;
import com.hubble.registration.JWebClient;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubbleconnected.camera.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.devices.SerializableDeviceProfile;

public class UploadFwAndWaitForUpgradeCompleteDialog extends CommonDialog {
  private static final String TAG = "UploadFwDialog";
  private TextView mTextViewCurrentTask;
  private ProgressBar mProgressBar;
  private CheckFirmwareUpdateResult mCheckFirmwareUpdateResult;
  private Button mNegativeButton, mPositiveButton;
  private AlertDialog dialog;
  private JDownloader downloader;
  private Status mStatus = Status.INIT;
  private String mFwFilePath;
  private boolean uploading = false;
  private String modelId;

  public enum Status {
    INIT, UPLOADING, ERROR, UPLOADED, CANCEL, RETRY_SETUP, UPGRADING, UPGRADED, UPGRADE_ERROR, RECOVERY
  }

  public UploadFwAndWaitForUpgradeCompleteDialog(CheckFirmwareUpdateResult result, String fwFilePath, String modelId) {
    mCheckFirmwareUpdateResult = result;
    mFwFilePath = fwFilePath;
    this.modelId = modelId;
  }

  @Override
  public AlertDialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();

    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout

    contentView = inflater.inflate(R.layout.upgrade_layout, null);

    builder.setView(contentView);

    // build type face
    mTextViewCurrentTask = (TextView) contentView.findViewById(R.id.textViewCurrentTask);
    mTextViewCurrentTask.setText(R.string.upload_new_firmware_to_camera);

    mProgressBar = (ProgressBar) contentView.findViewById(R.id.prgBar);
    mProgressBar.setMax(100);

    builder.setTitle(R.string.upload_firmware);
    builder.setNegativeButton(R.string.Cancel, null);
    builder.setPositiveButton(R.string.OK, null);

    dialog = builder.create();
    return dialog;
  }

  @Override
  public void onStart() {
    super.onStart();

    mNegativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
    mPositiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

    dialog.setCancelable(false);
    dialog.setCanceledOnTouchOutside(false);
    if (!uploading) {
      uploading = true;
      start();
    }

  }

  @Override
  public void onDetach() {
    uploading = false;
    super.onDetach();
  }

  private void start() {
    pushFirmwareToCamera(mCheckFirmwareUpdateResult.getUploadFwURL(), mFwFilePath);
  }

  @Override
  public void onDestroy() {
    if (downloader != null) {
      downloader.deleteObservers();
    }
    super.onDestroy();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    setStatus(Status.CANCEL);
    super.onCancel(dialog);
  }

  private void pushFirmwareToCamera(String uploadURL, String filePath) {
    try {
      Log.i(TAG, "Upload firmware to camera " + uploadURL + " file path " + filePath);
      final JUploader uploader = new JUploader(uploadURL, filePath, mCheckFirmwareUpdateResult.getNewFirmwareFileName());

      setStatus(Status.UPLOADING);

      uploader.addObserver(new Observer() {
        @Override
        public void update(Observable arg0, Object arg1) {
          Runnable updateUIRunnable = null;
          if (uploader.getStatus() == JUploader.UPLOADING) {
            updateUIRunnable = new Runnable() {
              @Override
              public void run() {
                if (mProgressBar != null) {
                  mProgressBar.setProgress(90);
                }
              }
            };
          } else if (uploader.getStatus() == JUploader.COMPLETE) {
            waitForUpgradingCompleted();
          } else if (uploader.getStatus() == JUploader.ERROR) {
            setStatus(Status.ERROR);
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

  private void waitForUpgradingCompleted() {
    setStatus(Status.UPGRADING);

    final String burningProcessURL = mCheckFirmwareUpdateResult.getBurningProgressURL();
    final String BURNING_PROCESS = "burning_process :";
    Thread waitForUpgradeComplete = new Thread(new Runnable() {
      @Override
      public void run() {
        int i = 0;
        while (i < 360) {
          try {
            String burningProcess = JWebClient.downloadAsString(burningProcessURL);
            Log.i(TAG, "Burning process result: " + burningProcess);
            if (burningProcess.startsWith(BURNING_PROCESS)) {
              String strBurningPercent = burningProcess.replace(BURNING_PROCESS, "");
              int burningPercent = Integer.parseInt(strBurningPercent);
              if (burningPercent == -1) {

              } else if (burningPercent >= 0) {
                if (burningPercent > 100) {
                  burningPercent = 100;
                }

                int displayValue = "0877".equals(modelId) ? burningPercent / 2 : burningPercent;
                setUpgradeProgress(displayValue);

                if (burningPercent == 100) {
                  if ("0877".equals(modelId)) {
                    setStatus(Status.RECOVERY);
                  } else {
                    setStatus(Status.UPGRADED);
                  }
                  break;
                }
              }
            }
            if (i % 10 == 0) {
              try {
                JWebClient.downloadAsString(mCheckFirmwareUpdateResult.getKeepAliveURL());
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
          } catch (IOException e) {
            e.printStackTrace();
          }

          try {
            i++;
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        if (i >= 360) {
          Log.i(TAG, "360 seconds parsed, but camera upgrade still was not done.");
          setStatus(Status.UPGRADE_ERROR);
        } else {
          if ("0877".equals(modelId) && getStatus() == Status.RECOVERY) {
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
            String fw = null;
            try {
              Log.i(TAG, "Checking fw via http command");
              fw = JWebClient.downloadAsString(mCheckFirmwareUpdateResult.getKeepAliveURL());
              if (!TextUtils.isEmpty(fw)) {
                fw = fw.replace("get_version: ", "");
              } else {
                Log.i(TAG, "Checking fw via request api");
                Models.ApiResponse<SerializableDeviceProfile> deviceProfile = Api.getInstance().getService()
                    .getDeviceProfile(mCheckFirmwareUpdateResult.getApiKey(), mCheckFirmwareUpdateResult.getRegID());
                if (deviceProfile.getStatus().equalsIgnoreCase("200") && deviceProfile.getData().isAvailable()
                    && deviceProfile.getData().getFirmwareVersion() != null) {
                  fw = deviceProfile.getData().getFirmwareVersion();
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
            Log.d(TAG, "get fw version ... DONE with result: " + fw);
            if (!TextUtils.isEmpty(fw) && fw.equals(mCheckFirmwareUpdateResult.getOTAVersion())) {
              setStatus(Status.UPGRADED);
            } else {
              setStatus(Status.UPGRADE_ERROR);
            }
          }
        }
      }
    });
    waitForUpgradeComplete.start();
  }

  private void setUpgradeProgress(final int progress) {
    if (getActivity() == null) {
      return;
    }

    getActivity().runOnUiThread(new Runnable() {

      @Override
      public void run() {
        try {
          Log.i(TAG, "Update UI percent to " + progress);
          mProgressBar.setProgress(progress);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
  }

  public Status getStatus() {
    return mStatus;
  }

  public void setStatus(Status status) {
    this.mStatus = status;
    if (getActivity() == null) {
      return;
    }
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (mStatus == Status.UPLOADING) {
          dialog.setTitle(R.string.upload_firmware);

          mPositiveButton.setVisibility(View.GONE);
          mNegativeButton.setVisibility(View.GONE);

          mTextViewCurrentTask.setText(R.string.upload_new_firmware_to_camera);
        } else if (mStatus == Status.UPGRADING) {
          mPositiveButton.setVisibility(View.GONE);
          mNegativeButton.setVisibility(View.GONE);

          dialog.setTitle(R.string.upgrading_firmware);
          mTextViewCurrentTask.setText(R.string.camera_is_updating_to_new_firmware);
          mProgressBar.setProgress(0);
        } else if (mStatus == Status.UPGRADED) {
          mTextViewCurrentTask.setText(R.string.upgrade_complete_please_retry_setup);
          mPositiveButton.setVisibility(View.VISIBLE);
          mPositiveButton.setText(R.string.OK);
          mNegativeButton.setVisibility(View.GONE);

          if (commonDialogListener != null) {
            commonDialogListener.onDialogPositiveClick(UploadFwAndWaitForUpgradeCompleteDialog.this);
          }
        } else if (mStatus == Status.UPGRADE_ERROR) {
          setCancelable(true);
          mNegativeButton.setVisibility(View.GONE);
          mPositiveButton.setVisibility(View.VISIBLE);
          mPositiveButton.setText(R.string.OK);
          mTextViewCurrentTask.setText(R.string.failed);
          mPositiveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
              dismiss();
            }
          });
        } else if (mStatus == Status.ERROR) {
          mTextViewCurrentTask.setText(getString(R.string.sending_firmware_to_camera_failed));

          setCancelable(true);
          mNegativeButton.setVisibility(View.VISIBLE);
          mPositiveButton.setVisibility(View.VISIBLE);
          mNegativeButton.setText(R.string.Cancel);
          mPositiveButton.setText(R.string.OK);
          mPositiveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
              uploading = false;
              start();
            }
          });
          mNegativeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              if (commonDialogListener != null) {
                commonDialogListener.onDialogNegativeClick(UploadFwAndWaitForUpgradeCompleteDialog.this);
              }
              dismiss();
            }
          });

        }

      }
    });

  }
}
