package com.nxcomm.blinkhd.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.hubbleconnected.camera.R;


public class FileEventDialog extends com.hubble.registration.ui.CommonDialog {
  public static final int EVENT_VIEW = 1;
  public static final int EVENT_SHARE = 2;
  public static final int EVENT_SAVE = 3;

  private int selectImageSource = EVENT_VIEW;
  private RadioButton viewRb;
  private RadioButton shareRb;
  private RadioButton saveRb;
  private TextView viewTv;
  private TextView shareTv;
  private TextView saveTv;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();

    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout

    contentView = inflater.inflate(R.layout.dialog_file_action, null);

    builder.setView(contentView);

    viewTv = (TextView) contentView.findViewById(R.id.dialog_file_action_view);
    shareTv = (TextView) contentView.findViewById(R.id.dialog_file_action_share);
    saveTv = (TextView) contentView.findViewById(R.id.dialog_file_action_save);

    viewRb = (RadioButton) contentView.findViewById(R.id.dialog_file_action_view_rb);
    shareRb = (RadioButton) contentView.findViewById(R.id.dialog_file_action_share_rb);
    saveRb = (RadioButton) contentView.findViewById(R.id.dialog_file_action_save_rb);

    Button btnCancel = (Button) contentView.findViewById(R.id.dialog_file_action_cancel);
    Button btnOk = (Button) contentView.findViewById(R.id.dialog_file_action_ok);

    viewRb.setChecked(true);

    View.OnClickListener radioOnClickListener = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        setRadioButton(v);
      }

    };
    viewRb.setOnClickListener(radioOnClickListener);
    viewTv.setOnClickListener(radioOnClickListener);
    shareRb.setOnClickListener(radioOnClickListener);
    shareTv.setOnClickListener(radioOnClickListener);
    saveRb.setOnClickListener(radioOnClickListener);
    saveTv.setOnClickListener(radioOnClickListener);

    btnOk.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (commonDialogListener != null) {
          commonDialogListener.onDialogPositiveClick(FileEventDialog.this);
        }
      }
    });

    btnCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        FileEventDialog.this.getDialog().cancel();
      }
    });

    return builder.create();
  }

  private void setRadioButton(View selectedRadio) {
    if (selectedRadio == viewRb || selectedRadio == viewTv) {
      selectImageSource = EVENT_VIEW;
      viewRb.setChecked(true);
      shareRb.setChecked(false);
      saveRb.setChecked(false);
    } else if (selectedRadio == shareRb || selectedRadio == shareTv) {
      selectImageSource = EVENT_SHARE;
      viewRb.setChecked(false);
      shareRb.setChecked(true);
      saveRb.setChecked(false);
    } else if (selectedRadio == saveRb || selectedRadio == saveTv) {
      selectImageSource = EVENT_SAVE;
      viewRb.setChecked(false);
      shareRb.setChecked(false);
      saveRb.setChecked(true);
    }
  }

  public int getSelectImageSource() {
    return selectImageSource;
  }
}
