package com.hubble.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

/**
 * Created by Sean on 15-03-26.
 */
public class TemperatureNumberPicker extends NumberPicker {
  public TemperatureNumberPicker(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void addView(View child) {
    super.addView(child);
    updateView(child);
  }

  @Override
  public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
    super.addView(child, index, params);
    updateView(child);
  }

  @Override
  public void addView(View child, android.view.ViewGroup.LayoutParams params) {
    super.addView(child, params);
    updateView(child);
  }

  private void updateView(View view) {
    if (view instanceof EditText) {
      ((EditText) view).setTextSize(36);
      ((EditText) view).setTextColor(Color.parseColor("#f5f5f5"));
    }
  }
}
