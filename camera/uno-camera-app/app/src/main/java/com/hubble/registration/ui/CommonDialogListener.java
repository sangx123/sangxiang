package com.hubble.registration.ui;

import android.support.v4.app.DialogFragment;

public interface CommonDialogListener {
  void onDialogPositiveClick (DialogFragment dialog);

  void onDialogNegativeClick (DialogFragment dialog);

  void onDialogNeutral (DialogFragment dialog);
}
