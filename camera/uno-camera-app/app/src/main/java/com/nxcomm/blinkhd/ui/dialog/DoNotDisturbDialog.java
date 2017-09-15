package com.nxcomm.blinkhd.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.customview.CircularSeekBar;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import base.hubble.PublicDefineGlob;

public class DoNotDisturbDialog extends com.hubble.registration.ui.CommonDialog {
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();

    contentView = inflater.inflate(R.layout.dialog_do_not_disturb, null);
    final CircularSeekBar seekBar = (CircularSeekBar) contentView.findViewById(R.id.dialog_doNotDisturb_seekbar);
    final TextView textView = (TextView) contentView.findViewById(R.id.dialog_doNotDisturb_textView);
    SecureConfig settings = HubbleApplication.AppConfig;

    seekBar.setMax(1440);

    seekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
      @Override
      public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
        textView.setText(formatTime(progress));
      }

      @Override
      public void onStopTrackingTouch(CircularSeekBar seekBar) {
      }

      @Override
      public void onStartTrackingTouch(CircularSeekBar seekBar) {

      }
    });

    builder.setView(contentView);
    builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            if (commonDialogListener != null) {
              commonDialogListener.onDialogPositiveClick(DoNotDisturbDialog.this);
            }
          }
        }
    ).setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            DoNotDisturbDialog.this.getDialog().cancel();
          }
        }
    );

    boolean isEnabled = settings.getBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, false);
    if (isEnabled) {
      long remainTime;
      try {
        remainTime = settings.getLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, DateTime.now().getMillis());
      } catch (Exception e) {
        settings.putLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, DateTime.now().getMillis());
        remainTime = settings.getLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, DateTime.now().getMillis());
      }
      if (remainTime > DateTime.now().getMillis()) {
        DateTime remaining = new DateTime(remainTime);
        Duration difference = new Duration(DateTime.now(), remaining);

        seekBar.setProgress((int) difference.getStandardMinutes());
      } else {
        seekBar.setProgress(0);
      }
    } else {
      seekBar.setProgress(0);
    }

    return builder.create();
  }

  private String formatTime(int minutes) {
    if (minutes < 60) {
      if (minutes == 0) {
        return getActivity().getString(R.string.disabled);
      } else if (minutes == 1) {
        return getActivity().getString(R.string.one_minute);
      } else {
        return (String.format(getActivity().getString(R.string.blank_minutes), minutes));
      }
    } else {
      int hours = minutes / 60;
      int mins = minutes % 60;
      if(mins == 0){
        return String.format(getActivity().getString(R.string.do_not_disturb_hour_format), formatNumber(hours));
      } else {
        return String.format(getActivity().getString(R.string.do_not_disturb_hour_mins), formatNumber(hours), formatNumber(mins));
      }
    }
  }

  private String formatNumber(int num) {
    return num < 10 ? "0" + num : num + "";
  }
}
