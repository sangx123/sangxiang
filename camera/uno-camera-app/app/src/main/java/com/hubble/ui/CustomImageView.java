package com.hubble.ui;

/**
 * Created by Son Nguyen on 26/01/2016.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CustomImageView extends ImageView {

  public CustomImageView(Context context) {
    super(context);
  }

  public CustomImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    Drawable d = getDrawable();
    if (d != null) {
      int w = MeasureSpec.getSize(widthMeasureSpec);
      int h = (int) (w * (9.0f / 16.0f));
      setMeasuredDimension(w, h);
    } else
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}