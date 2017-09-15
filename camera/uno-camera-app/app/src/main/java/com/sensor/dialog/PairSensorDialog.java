package com.sensor.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.ProgressBar;

import com.hubble.registration.ui.CommonDialog;
import com.hubbleconnected.camera.R;


/**
 * Created by hoang on 10/26/15.
 */
public class PairSensorDialog extends CommonDialog {
  private LayoutInflater mInflater = null;
  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog dialog = null;
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    if (mInflater == null) {
      mInflater = LayoutInflater.from(getActivity());
    }
    contentView = mInflater.inflate(R.layout.dialog_pair_sensor, null);
    //ProgressBar progressBar = (ProgressBar) contentView.findViewById(R.id.tag_sensor_progress_bar);
    builder.setView(contentView);
    dialog = builder.create();
    return dialog;
  }
}
