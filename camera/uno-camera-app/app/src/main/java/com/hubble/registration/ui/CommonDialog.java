package com.hubble.registration.ui;

import android.support.v4.app.DialogFragment;
import android.view.View;

public class CommonDialog extends DialogFragment {
  protected CommonDialogListener commonDialogListener;

  protected View contentView = null;

  protected int nagetiveTextID;
  protected int positiveTextID;

  public View findViewById(int id) {
    return contentView.findViewById(id);
  }

  public void setCommonDialogListener(CommonDialogListener commonDialogListener2) {
    this.commonDialogListener = commonDialogListener2;
  }
}
