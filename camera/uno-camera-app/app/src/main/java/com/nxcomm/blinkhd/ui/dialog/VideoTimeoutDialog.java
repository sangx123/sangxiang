package com.nxcomm.blinkhd.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hubbleconnected.camera.R;


public class VideoTimeoutDialog extends com.hubble.registration.ui.CommonDialog {
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();

    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout

    contentView = inflater.inflate(R.layout.dialog_video_timout, null);
    Button btnCancel = (Button) contentView.findViewById(R.id.dialog_video_timeout_btn_cancel);
    Button btnOk = (Button) contentView.findViewById(R.id.dialog_video_timeout_btn_ok);
    final CheckBox checkBox = (CheckBox) contentView.findViewById(R.id.dialog_video_timeout_cb_askagain);
    TextView tvMessage = (TextView) contentView.findViewById(R.id.dialog_video_timeout_tv_message);

    builder.setView(contentView);

    btnCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (commonDialogListener != null) {
          commonDialogListener.onDialogNegativeClick(VideoTimeoutDialog.this);
        }
      }
    });

    btnOk.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (commonDialogListener != null) {
          if (checkBox.isChecked()) {
            commonDialogListener.onDialogPositiveClick(VideoTimeoutDialog.this);
          } else {
            commonDialogListener.onDialogNeutral(VideoTimeoutDialog.this);
          }
        }
      }
    });



    return builder.create();
  }



  private String getMessageBasedOnConnection() {
    final ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = connMgr.getActiveNetworkInfo();
    if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
      return getString(R.string.dialog_excessive_data_usage);
    } else {
      return getString(R.string.dialog_extended_streaming_charges);
    }
  }
}
