package com.hubble.ui;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * Created by Sean on 2014-09-18.
 */
public class SquareCardLayout extends CardView {
  public SquareCardLayout(Context context) {
    super(context);
  }

  public SquareCardLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SquareCardLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
  }
}
