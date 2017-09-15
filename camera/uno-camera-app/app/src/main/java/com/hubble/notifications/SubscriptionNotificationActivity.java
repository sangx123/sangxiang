package com.hubble.notifications;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.hubble.devcomm.DeviceSingleton;
import com.hubbleconnected.camera.R;

import com.hubble.framework.service.notification.HubbleNotification;

public class SubscriptionNotificationActivity extends FragmentActivity {
  private static final String TAG = "SubscriptionNotificationActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    new AlertDialog.Builder(this)
        .setTitle(getString(R.string.current_plan))
        .setMessage(translateNotification(this, new HubbleNotification(getIntent().getExtras())))
        .setPositiveButton(getString(R.string.details), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String url = getString(R.string.hubble_web_app_url);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            finish();
          }
        })
        .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            finish();
          }
        }).show();
  }

  public static String translateNotification(Context ct, HubbleNotification hubbleNotification) {
    if (hubbleNotification != null && hubbleNotification.getRegistrationId() != null && ct != null) {
      com.hubble.devcomm.Device mDevice = null;
      try {
        mDevice = DeviceSingleton.getInstance().getDeviceByRegId(hubbleNotification.getRegistrationId());
      } catch (Exception ignored) {
      }

      String message = hubbleNotification.getMessage();
      String messageType = hubbleNotification.getMessageType();
      String expiresIn = hubbleNotification.getFreeTrialExpiresInXDays();
      String deviceName = "";
      if (mDevice != null && !mDevice.getProfile().getName().isEmpty()) {
        deviceName = mDevice.getProfile().getName();
      } else {
        deviceName = ct.getString(R.string.your_camera);
      }
      String subName = hubbleNotification.getSubscriptionName();

      if (messageType != null) {
        // translate the notification for base.hubble.subscriptions to the user's locale
        switch (messageType) {
          case "free trial available":
            message = String.format(ct.getString(R.string.free_trial_available), deviceName);
            break;
          case "free trial expired":
            message = String.format(ct.getString(R.string.free_trial_expired), deviceName);
            break;
          case "free trial expiry pending":
            message = String.format(ct.getString(R.string.free_trial_expiry_pending), expiresIn, deviceName);
            break;
          case "free trial applied":
            message = String.format(ct.getString(R.string.free_trial_applied), deviceName);
            break;
          case "subscription cancelled":
            message = String.format(ct.getString(R.string.subscription_cancelled), subName, deviceName);
            break;
          case "subscription applied":
            message = String.format(ct.getString(R.string.subscription_applied), subName, deviceName);
            break;
          default:
            break;
        }
      }
      return message;
    } else {
      return "";
    }
  }
}
