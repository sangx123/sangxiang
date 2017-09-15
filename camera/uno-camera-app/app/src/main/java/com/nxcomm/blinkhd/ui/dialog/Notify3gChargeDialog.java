package com.nxcomm.blinkhd.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.CheckBox;

import com.hubbleconnected.camera.R;


public class Notify3gChargeDialog extends com.hubble.registration.ui.CommonDialog {

  private CheckBox cbxDontAskMeAgain = null;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();

    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout

    contentView = inflater.inflate(R.layout.dialog_3g_charge_notification, null);

    builder.setView(contentView);

    // build type face
    builder.setNeutralButton(R.string.Yes, new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (commonDialogListener != null) {
              commonDialogListener.onDialogNeutral(Notify3gChargeDialog.this);
            }
          }
        }
    );

    builder.setPositiveButton(R.string.YesDontAsk, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            if (commonDialogListener != null) {
              commonDialogListener.onDialogPositiveClick(Notify3gChargeDialog.this);
            }

          }
        }
    ).setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {

          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (commonDialogListener != null) {
              commonDialogListener.onDialogNegativeClick(Notify3gChargeDialog.this);
            }

          }
        }
    );

    builder.setCancelable(false);

    return builder.create();
  }

}
