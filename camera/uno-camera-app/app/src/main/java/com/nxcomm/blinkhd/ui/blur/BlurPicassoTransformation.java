package com.nxcomm.blinkhd.ui.blur;

import android.content.Context;
import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

/**
 * Created by Sean on 14-10-27.
 */
public class BlurPicassoTransformation implements Transformation {
  private Context mContext;

  public BlurPicassoTransformation(Context context) {
    mContext = context;
  }

  @Override
  public Bitmap transform(Bitmap source) {

    Bitmap mBitmap = Blur.fastblur(mContext, source, 25, 5);

    if (mBitmap != null) {
      source.recycle();
    }
    return mBitmap;
  }

  @Override
  public String key() {
    return null;
  }
}
