package com.hubble.registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.SettableFuture;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.base.hubble.subscriptions.SubscriptionRequestor;
import com.hubble.dialog.AskForFreeTrialDialog;
import com.hubble.subscription.ManagePlanActivity;
import com.hubbleconnected.camera.R;

import org.jetbrains.annotations.NotNull;

import base.hubble.database.DeviceProfile;
import base.hubble.subscriptions.DeviceSubscription;
/**
 * Created by Sean on 15-02-26.
 */
public class SubscriptionWizard extends SubscriptionRequestor {
  private String mApiKey;
  private Activity mActivity;
  private Device mDevice;
  private AskForFreeTrialDialog mAskForFreeTrialDialog;
  private Dialog mDialog;

  public SubscriptionWizard(@NotNull String apiKey, @NotNull Activity activity, @NotNull Device device, boolean manually) {
    super(apiKey, device, activity.getApplicationContext());
    mApiKey = apiKey;
    mActivity = activity;
    mDevice = device;
  }

  public SubscriptionWizard(@NotNull String apiKey, @NotNull Activity activity, @NotNull Device device, boolean manually, Dialog dialog) {
    super(apiKey, device, activity.getApplicationContext());
    mApiKey = apiKey;
    mActivity = activity;
    mDevice = device;
    mDialog = dialog;
  }

  private void promiseYesNoDialog(String title, String message, final SettableFuture<Boolean> promise) {
    if (mActivity != null) {
      try {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_subscription_simple_text, (ViewGroup) mActivity.findViewById(R.id.dialog_subscriptionSimpleTextRoot));
        TextView textView = (TextView) layout.findViewById(R.id.dialog_subscriptionSimpleText);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(Html.fromHtml(message));
        new AlertDialog.Builder(mActivity)
            .setView(layout)
            .setTitle(title)
            .setPositiveButton(mActivity.getString(R.string.start_free_trial), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                promise.set(true);
              }
            }).setNegativeButton(mActivity.getString(R.string.decline_free_trial), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            promise.set(false);
          }
        }).show();
      } catch (Exception e) {
        promise.set(false);
      }
    }
  }

  private void showAskForFreeTrialDialog(final SettableFuture<Boolean> promise) {

    if (mActivity != null) {
      mAskForFreeTrialDialog = new AskForFreeTrialDialog(mActivity, new AskForFreeTrialDialog.PlanListener() {
        @Override
        public void onEnableFreeTrialClick() {
          dismissFreeTrialDialog();
          promise.set(true);
        }

        @Override
        public void onUpgradePlanClick() {
          dismissFreeTrialDialog();
          /*Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(Uri.parse(mActivity.getString(R.string.hubble_web_app_url)));
          mActivity.startActivity(intent);*/
          Intent intent = new Intent(mActivity, ManagePlanActivity.class);
          mActivity.startActivity(intent);
          promise.set(false);
        }
      });
      mAskForFreeTrialDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          promise.set(false);
        }
      });
      mAskForFreeTrialDialog.show();
    }

  }

  private void dismissFreeTrialDialog() {
    if (mAskForFreeTrialDialog != null && mAskForFreeTrialDialog.isShowing()) {
      mAskForFreeTrialDialog.dismiss();
    }
  }


  private void notifyDialog(String title, String message) {
    if (message != null && mActivity != null) {
      try {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_subscription_simple_text, (ViewGroup) mActivity.findViewById(R.id.dialog_subscriptionSimpleTextRoot));
        TextView textView = (TextView) layout.findViewById(R.id.dialog_subscriptionSimpleText);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(Html.fromHtml(message));
        new AlertDialog.Builder(mActivity)
            .setView(layout)
            .setTitle(title)
            .setPositiveButton(mActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            }).show();
      } catch (Exception ignore) {
      }
    }
  }

  @Override
  public void askToUseSDCardRecording(DeviceProfile deviceProfile, final SettableFuture<Boolean> promise) {
    try {
      LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View layout = inflater.inflate(R.layout.dialog_subscription_simple_text, (ViewGroup) mActivity.findViewById(R.id.dialog_subscriptionSimpleTextRoot));
      TextView textView = (TextView) layout.findViewById(R.id.dialog_subscriptionSimpleText);
      textView.setMovementMethod(LinkMovementMethod.getInstance());
      textView.setText(Html.fromHtml(mActivity.getString(R.string.no_subscription_no_plan_2)));
      new AlertDialog.Builder(mActivity)
          .setView(layout)
          .setTitle(mActivity.getString(R.string.current_plan))
          .setNegativeButton(mActivity.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              promise.set(false);
            }
          })
          .setPositiveButton(mActivity.getString(R.string.use_sdcard), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              promise.set(true);
            }
          }).show();
    } catch (Exception ignore) {
      promise.set(false);
    }
  }

  @Override
  public void askToApplySubscription(@NotNull DeviceSubscription subscription, @NotNull SettableFuture<Boolean> response) {
  }

  @Override
  public void askToApplyFreeTrial(@NotNull SettableFuture<Boolean> response) {
    //String freeTrialMessage = mActivity.getString(R.string.free_trial_message);
    //promiseYesNoDialog(mActivity.getString(R.string.free_trial_dialog_title), freeTrialMessage, response);
    if(mDialog != null && mDialog.isShowing()){
      mDialog.dismiss();
    }
    showAskForFreeTrialDialog(response);
  }

  /**
   * 24 June, 2015
   * If user has no subscription and no plan but device support sdcard recording
   * we will ask them to enable it
   *
   * @param device
   */
  @Override
  public void notifyNoSubscriptionAndNoFreeTrial(@NotNull DeviceProfile device) {
    notifyDialog(mActivity.getString(R.string.current_plan), mActivity.getString(R.string.no_subscription_no_plan));
  }

  @Override
  public void notifySubscriptionExpired(@NotNull DeviceSubscription subscription) {
  }

  @Override
  public void notifyFreeTrialApplied(@NotNull DeviceProfile device) {
  }

  @Override
  public void notifySubscriptionApplied(@NotNull DeviceProfile device, @NotNull DeviceSubscription subscription) {
  }

  @Override
  public void notifyFreeTrialNotApplied(@NotNull DeviceProfile device, String errorMessage) {
    notifyDialog(mActivity.getString(R.string.current_plan), errorMessage);
  }

  @Override
  public void notifySubscriptionNotApplied(@NotNull DeviceProfile device) {
  }

  @Override
  public void notifySDCardRecordingApplied(@NotNull DeviceProfile device) {
    Toast.makeText(mActivity, mActivity.getString(R.string.switched_record_storage_mode_succeeded), Toast.LENGTH_SHORT).show();
  }

  @Override
  public void notifySDCardRecordingNotApplied(@NotNull DeviceProfile device, String errorMessage) {
    Toast.makeText(mActivity, mActivity.getString(R.string.switched_record_storage_mode_failed), Toast.LENGTH_SHORT).show();
  }

  @Override
  public void notifySubscriptionExceedMaxAllowedNumber() {
    notifyDialog(mActivity.getString(R.string.current_plan), mActivity.getString(R.string.subscription_exceed_max_allowed_number));
  }

}