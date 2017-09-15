package com.nxcomm.blinkhd.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubbleconnected.camera.R;

import base.hubble.PublicDefineGlob;

public class NotificationFrequencyDialog extends com.hubble.registration.ui.CommonDialog {
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();

    contentView = inflater.inflate(R.layout.dialog_notification_frequency, null);
    final TextView notificationFrequency = (TextView) contentView.findViewById(R.id.dialog_notification_tv_frequency);
    final SeekBar seekBar = (SeekBar) contentView.findViewById(R.id.dialog_notification_seekbar_frequency);
    SecureConfig settings = HubbleApplication.AppConfig;

    seekBar.setMax(61);

    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress == 0) {
          notificationFrequency.setText(getActivity().getString(R.string.immediately));
        } else if (progress == 1) {
          notificationFrequency.setText(getActivity().getString(R.string.one_minute));
        } else if (progress == seekBar.getMax()) {
          notificationFrequency.setText(getActivity().getString(R.string.disable_notifications));
        } else {
          notificationFrequency.setText(String.format(getActivity().getString(R.string.blank_minutes), progress));
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
    builder.setView(contentView);
    builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            if (commonDialogListener != null) {
              commonDialogListener.onDialogPositiveClick(NotificationFrequencyDialog.this);
            }
          }
        }
    ).setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            NotificationFrequencyDialog.this.getDialog().cancel();
          }
        }
    );

    boolean isEnabled = settings.getBoolean(PublicDefineGlob.PREFS_SHOULD_HAVE_NOTIFICATIONS, true);
    if (isEnabled) {
      seekBar.setProgress(settings.getInt(PublicDefineGlob.PREFS_MINUTES_BETWEEN_NOTIFICATIONS,
          PublicDefineGlob.PREFS_DEFAULT_NOTIFICATIONS_FREQUENCY));
    } else {
      seekBar.setProgress(seekBar.getMax());
    }

    return builder.create();
  }
}
