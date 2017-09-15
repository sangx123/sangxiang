package com.hubble.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Sean on 2014-09-18.
 */
public class SquareFrameLayout extends FrameLayout {
  public SquareFrameLayout(Context context) {
    super(context);
  }

  public SquareFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SquareFrameLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
  }
}
