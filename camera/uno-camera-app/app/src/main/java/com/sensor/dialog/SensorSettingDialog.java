package com.sensor.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hubble.registration.ui.CommonDialog;
import com.hubbleconnected.camera.R;

import org.jetbrains.annotations.NotNull;

/**
 * Created by hoang on 10/26/15.
 */
public class SensorSettingDialog extends CommonDialog {
  private static final int MAX_SENSITIVITY_LEVEL = 5;
  private LayoutInflater mInflater = null;
  private int mCurrLevel;
  private Context mContext;
  private ViewHolder mHolder = new ViewHolder();

  private SeekBar.OnSeekBarChangeListener mSeekbarListener = null;
  private View.OnClickListener mImgHelpListener = null;

  public void setCurrentLevel(int currentLevel) {
    this.mCurrLevel = currentLevel;
    if (mHolder.mTvValue != null) {
      mHolder.mTvValue.setText(String.valueOf(mCurrLevel + 1));
    }

    if (mHolder.mSbvLevel != null) {
      mHolder.mSbvLevel.setProgress(mCurrLevel);
    }
  }

  public void setOnSeekbarChangedListener(SeekBar.OnSeekBarChangeListener listener) {
    mSeekbarListener = listener;
  }

  public void setOnImgHelpClickedListener(View.OnClickListener listener) {
    mImgHelpListener = listener;
  }

  @NotNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog dialog = null;
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    if (mInflater == null) {
      mInflater = LayoutInflater.from(getActivity());
    }
    contentView = mInflater.inflate(R.layout.dialog_sensor_sensitivity_setting, null);
    mHolder.mTvDialogTitle = (TextView) contentView.findViewById(R.id.dialog_seekbar_textTitle);
    mHolder.mTvValue = (TextView) contentView.findViewById(R.id.dialog_seekbar_textValue);
    mHolder.mSbvLevel = (SeekBar) contentView.findViewById(R.id.dialog_seekbar);
    mHolder.mImgHelp = (ImageView) contentView.findViewById(R.id.imgHelp);

    mHolder.mTvDialogTitle.setText(getString(R.string.sensor_motion_sensitivity));
    mHolder.mTvValue.setText(String.valueOf(mCurrLevel + 1));
    mHolder.mSbvLevel.setMax(MAX_SENSITIVITY_LEVEL - 1);
    mHolder.mSbvLevel.incrementProgressBy(1);
    if (mCurrLevel != -1) {
      mHolder.mSbvLevel.setProgress(mCurrLevel);
    }
    mHolder.mSbvLevel.setOnSeekBarChangeListener(mSeekbarListener);
    mHolder.mImgHelp.setOnClickListener(mImgHelpListener);
    builder.setView(contentView);
    builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            if (commonDialogListener != null) {
              commonDialogListener.onDialogPositiveClick(SensorSettingDialog.this);
            }
          }
        }
    ).setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            if (commonDialogListener != null) {
              commonDialogListener.onDialogNegativeClick(SensorSettingDialog.this);
            }
          }
        }
    );
    dialog = builder.create();

    return dialog;
  }

  private class ViewHolder {
    TextView mTvDialogTitle;
    TextView mTvValue;
    SeekBar mSbvLevel;
    ImageView mImgHelp;
  }
}
