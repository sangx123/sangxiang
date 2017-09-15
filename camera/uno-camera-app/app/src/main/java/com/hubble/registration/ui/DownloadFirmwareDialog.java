package com.hubble.registration.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hubble.registration.JDownloader;
import com.hubble.registration.Util;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubbleconnected.camera.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

public class DownloadFirmwareDialog extends CommonDialog {
  private TextView mTextViewCurrentTask;
  private ProgressBar mProgressBar;
  private CheckFirmwareUpdateResult mCheckFirmwareUpdateResult;
  private Button mNegativeButton, mPositiveButton;
  private JDownloader downloader;
  private Status mStatus = Status.INIT;
  private boolean started = false;
  private AlertDialog mDialog;

  private static final String TAG = "DownloadFirmwareDialog";

  public enum Status {
    INIT, DOWNLOADING, ERROR, DOWNLOADED, CANCEL, RETRY_SETUP;
  }

  public DownloadFirmwareDialog() {
  }

  public void setCheckFirmwareUpdateResult(CheckFirmwareUpdateResult checkFirmwareUpdateResult) {
    mCheckFirmwareUpdateResult = checkFirmwareUpdateResult;
  }

  @Override
  public AlertDialog onCreateDialog(Bundle savedInstanceState) {
    Log.i(TAG, "On dialog created.");
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    contentView = inflater.inflate(R.layout.upgrade_layout, null);
    builder.setView(contentView);

    mTextViewCurrentTask = (TextView) contentView.findViewById(R.id.textViewCurrentTask);
    mTextViewCurrentTask.setText(R.string.downloading_new_firmware_message);

    mProgressBar = (ProgressBar) contentView.findViewById(R.id.prgBar);
    mProgressBar.setMax(100);
    builder.setCancelable(false);

    builder.setTitle(R.string.downloading_new_firmware_title);
    builder.setNegativeButton(R.string.Cancel, null);
    builder.setPositiveButton(R.string.OK, null);
    mDialog = builder.create();
    return mDialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.i(TAG, "On created view.");
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onResume() {
    Log.i(TAG, "On dialog resume.");
    super.onResume();
  }

  @Override
  public void onStart() {
    Log.i(TAG, "On dialog started.");
    super.onStart();
    mNegativeButton = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
    mPositiveButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);

    if (!started) {
      start();
      started = true;
    }
  }

  @Override
  public void onDetach() {
    Log.i(TAG, "On dialog detacted.");
    started = false;
    super.onDetach();
  }

  private void start() {
    if (mCheckFirmwareUpdateResult.isHaveNewFirmwareVersion() && mCheckFirmwareUpdateResult.getFirmwareDownloadLink() != null) {
      try {
        downloadFirmware(mCheckFirmwareUpdateResult.getFirmwareDownloadLink());
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onDestroy() {
    Log.i(TAG, "On dialog destroy.");
    if (downloader != null) {
      downloader.deleteObservers();
    }
    super.onDestroy();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    Log.i(TAG, "On dialog cancel.");
    setStatus(Status.CANCEL);
    super.onCancel(dialog);
  }

  private void downloadFirmware(String url) throws MalformedURLException {
    Log.i(TAG, "Start download new firmware: " + url);
    URL fwLink = new URL(url);
    downloader = new JDownloader(fwLink, Util.getFirmwareDirectory(), mCheckFirmwareUpdateResult.getNewFirmwareFileName());
    downloader.addObserver(new Observer() {
      @Override
      public void update(Observable arg0, Object arg1) {
        Runnable updateUIRunnable = null;
        Log.d(TAG, "downloadFirmware update arg0 = " + arg0);
        Log.d(TAG, "downloadFirmware update arg0 = " + arg1);

        if (downloader.getStatus() == JDownloader.DOWNLOADING) {

          updateUIRunnable = new Runnable() {
            @Override
            public void run() {
              if (mProgressBar != null) {
                mProgressBar.setProgress((int) downloader.getProgress());
                setStatus(Status.DOWNLOADING);
              }

            }
          };
        } else if (downloader.getStatus() == JDownloader.ERROR) {

          updateUIRunnable = new Runnable() {
            @Override
            public void run() {
              if (mTextViewCurrentTask != null) {
                mTextViewCurrentTask.setText(getString(R.string.download_firmware_error));
                setCancelable(true);
                setStatus(Status.ERROR);
              }
            }
          };
        } else if (downloader.getStatus() == JDownloader.COMPLETE) {
          updateUIRunnable = new Runnable() {
            @Override
            public void run() {
              if (mProgressBar != null) {
                mProgressBar.setProgress(100);
                setStatus(Status.DOWNLOADED);
              }
            }
          };
        }

        if (updateUIRunnable != null && getActivity() != null) {
          getActivity().runOnUiThread(updateUIRunnable);
        }

      }
    });
  }

  public Status getStatus() {
    return mStatus;
  }

  public void setStatus(Status mStatus) {
    if (!started) {
      return;
    }

    Log.i(TAG, "Status changed " + mStatus);
    this.mStatus = mStatus;

    if (mStatus == Status.DOWNLOADING) {
      try {
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        String percentCompleted = String.format(getString(R.string.completed), Integer.valueOf((int) downloader.getProgress()));
        mTextViewCurrentTask.setText(getString(R.string.downloading_new_firmware_message) + " " + percentCompleted);
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      mPositiveButton.setEnabled(false);
      mPositiveButton.setVisibility(View.GONE);
      mNegativeButton.setEnabled(true);
      mNegativeButton.setVisibility(View.VISIBLE);

      mNegativeButton.setOnClickListener(dismissClickListener);
    } else if (mStatus == Status.ERROR) {
      mNegativeButton.setEnabled(true);
      mPositiveButton.setEnabled(true);
      mNegativeButton.setVisibility(View.VISIBLE);
      mPositiveButton.setVisibility(View.VISIBLE);

      mPositiveButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          start();
        }
      });

    } else if (mStatus == Status.DOWNLOADED) {
      mNegativeButton.setEnabled(false);
      mPositiveButton.setEnabled(true);
      mPositiveButton.setVisibility(View.VISIBLE);
      mNegativeButton.setVisibility(View.GONE);
      mPositiveButton.setText(R.string.continue_text);

      mTextViewCurrentTask.setText(R.string.download_new_firmware_succeeded_press_ok);
      mPositiveButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          setStatus(Status.RETRY_SETUP);
          if (commonDialogListener != null) {
            commonDialogListener.onDialogPositiveClick(DownloadFirmwareDialog.this);
          }
          dismiss();
        }
      });
    }
  }

  private OnClickListener dismissClickListener = new OnClickListener() {

    @Override
    public void onClick(
        View view) {
      try {
        getDialog()
            .dismiss();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  };
}
