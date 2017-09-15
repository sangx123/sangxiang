package com.hubble.ui;

/**
 * Created by reginneil on 1/27/15.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


public class CustomGrid extends ImageView {

  public CustomGrid(Context context) {
    super(context);
  }

  public CustomGrid(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomGrid(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
  }
}