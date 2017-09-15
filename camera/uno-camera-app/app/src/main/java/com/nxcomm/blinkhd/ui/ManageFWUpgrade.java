package com.nxcomm.blinkhd.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubbleconnected.camera.R;
import com.msc3.update.CheckVersionFW;
import com.msc3.update.IpAndVersion;

import org.jetbrains.annotations.NotNull;

import base.hubble.PublicDefineGlob;
import com.hubbleconnected.camera.BuildConfig;
/**
 * Created by brennan on 15-03-30.
 */
public class ManageFWUpgrade implements Handler.Callback {
  private static final String TAG = "ManageFWUpgrade";
  private static ManageFWUpgrade mInstance;
  private static Activity mActivity;
  private static Device mDevice;

  private boolean mIsUpgrading = false;
  private IpAndVersion mDeviceIPFW;
  private SecureConfig settings = HubbleApplication.AppConfig;
  public static ManageFWUpgrade instantiate(Activity activity, Device device) {
    mActivity = activity;
    mDevice = device;

    if (mInstance == null) {
      mInstance = new ManageFWUpgrade();
    }
    return mInstance;
  }

  /**
   * Use instantiate first to set context/activity
   * and device.
   */
  public static ManageFWUpgrade getInstance() {
    if (mInstance == null) {
      mInstance = new ManageFWUpgrade();
    }
    return mInstance;
  }

  public void doCheckFwUpgradeTask() {
    if (isLocal()) {
      String portalUserName = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, null);
      String portalUserPass = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_PWD, null);
      CheckVersionFW checkVersionFW = new CheckVersionFW(mActivity,
          new Handler(this),
          false,
          null,
          mDevice.getProfile().getRegistrationId(),
          portalUserName,
          portalUserPass);

      checkVersionFW.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
          mDevice.getProfile().getDeviceLocation().getLocalIp(),
          mDevice.getProfile().getDeviceLocation().getLocalPort1(),
          PublicDefineGlob.HTTP_CMD_PART,
          CheckVersionFW.CHECK_FW_UPGRADE);
    } else {
      showFWDialog(mActivity.getString(R.string.unable_to_fw_upgrade_remotely));
    }
  }

  public boolean isLocal() {
    return CameraAvailabilityManager.getInstance().isCameraInSameNetwork(mActivity.getApplicationContext(), mDevice);
  }

  @Override
  public boolean handleMessage(@NotNull Message msg) {
    if (mActivity != null) {
      if (msg != null) {
        switch (msg.what) {
          case CheckVersionFW.PATCH_AVAILABLE:
            showDeviceFirmwareUpgradeDialog(msg);
            break;
          case CheckVersionFW.UPGRADE_DONE:
            showFirmwareUpgradeDoneDialog();
            break;
          case CheckVersionFW.UPGRADE_FAILED:
            showFirmwareUpgradeFailedDialog(msg);
            break;
          case CheckVersionFW.NO_PATCH_AVAILABLE:
            showFWDialog(mActivity.getString(R.string.no_fw_upgrade_found));
            break;
        }
      }
    }

    return false;
  }

  private void showDeviceFirmwareUpgradeDialog(Message msg) {
    mDeviceIPFW = (IpAndVersion) msg.obj;
    String deviceIP = mDeviceIPFW.device_ip;
    if (deviceIP != null) {
      if (mDevice != null && mDevice.getProfile().getDeviceLocation().getLocalIp().equalsIgnoreCase(deviceIP)) {
        showDialogFwPatchFound();
      }
    }
  }

  private void showFWDialog(String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    Spanned spannedMsg = Html.fromHtml("<big>" + message + "</big>");
    builder.setMessage(spannedMsg).setIcon(R.drawable.ic_launcher).setTitle(mActivity.getString(R.string.updating)).setPositiveButton(mActivity.getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(@NotNull DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        }
    );
    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        dialog.dismiss();
      }
    }).create().show();
  }

  private void showFirmwareUpgradeDoneDialog() {
    AlertDialog.Builder builder;
    AlertDialog alert;
    Spanned message1;
    builder = new AlertDialog.Builder(mActivity);
    message1 = Html.fromHtml("<big>" + mActivity.getString(R.string.upgrade_fw_done) + "</big>");
    builder.setMessage(message1).setIcon(R.drawable.ic_launcher).setTitle(mActivity.getString(R.string.updating)).setPositiveButton(mActivity.getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(@NotNull DialogInterface dialog, int which) {
            dialog.cancel();
          }
        }
    );
    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        //scanAndViewCamera();
      }
    });

    alert = builder.create();
    try {
      alert.show();
    } catch (Exception e1) {
    }
  }

  private void showFirmwareUpgradeFailedDialog(Message msg) {
    AlertDialog.Builder builder;
    Spanned message1;
    AlertDialog alert;
    mIsUpgrading = false;
    String reason = (String) msg.obj;
    builder = new AlertDialog.Builder(mActivity);
    message1 = Html.fromHtml("<big>" +
        mActivity.getString(R.string.upgrade_fw_failed) +
        " " + mActivity.getString(R.string.reason) + ": " + reason +
        " " + mActivity.getString(R.string.please_manually_reboot_the_camera) +
        "</big>");
    builder.setMessage(message1).setIcon(R.drawable.ic_launcher).setTitle(R.string.app_brand).setPositiveButton(mActivity.getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(@NotNull DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    alert = builder.create();
    try {
      alert.show();
    } catch (Exception e1) {
    }
  }

  private void showDialogFwPatchFound() {
    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    String msg = mActivity.getString(R.string.camera_firmware_upgrade_available);
    builder.setMessage(msg).setIcon(R.drawable.ic_launcher).setTitle(R.string.app_brand).setCancelable(true).setPositiveButton(mActivity.getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(@NotNull DialogInterface dialog, int which) {
            dialog.dismiss();
            if (mIsUpgrading) {
              return;
            }

            if (mDevice != null) {
              // stop all before upgrade
              mIsUpgrading = true;
              //hideRotatingIcon();
              String _portal_usrName = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, null);
              String _portal_usrPwd = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_PWD, null);
              // this task is to display the %
              CheckVersionFW test = new CheckVersionFW(
                  mActivity,
                  new Handler(ManageFWUpgrade.this),
                  true, mDevice.getProfile().getFirmwareVersion(),
                  mDevice.getProfile().getRegistrationId(),
                  _portal_usrName,
                  _portal_usrPwd
              );
              test.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mDevice.getProfile().getDeviceLocation().getLocalIp(), String.valueOf(mDevice.getProfile().getDeviceLocation().getLocalPort1()), PublicDefineGlob.HTTP_CMD_PART, CheckVersionFW.REQUEST_FW_UPGRADE);
            }
          }
        }
    ).setNegativeButton(mActivity.getResources().getString(R.string.No), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(@NotNull DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    AlertDialog fwUpgradeAlert = builder.create();
    fwUpgradeAlert.setCanceledOnTouchOutside(false);
    try {
      fwUpgradeAlert.show();
    } catch (Exception ignored) {
    }
  }
}
