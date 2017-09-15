package com.media.ffmpeg.android;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.SurfaceHolder;

import com.hubble.actors.Actor;

/**
 * Created by brennan on 15-03-26.
 */
public class VideoScaling {
  private static final String TAG = "VideoScaling";
  private static VideoScaling mInstance;

  private boolean panning = false, scaling = false, rendering = false;
  private boolean gesture = false, shouldReset = false, mShouldScale = true;
  private float mScaled = 1;
  private float focusX = 0, focusY = 0;
  private float mViewWidth = 0;
  private float mViewHeight = 0;
  //private Matrix mDrawMatrix = new Matrix();
  private float mLeftEdge = 0;
  private float mTopEdge = 0;
  private float rightEdge = 0;
  private float bottomEdge = 0;
  private int mScreenWidth = 0, mScreenHeight = 0;
  private float mTranslateX = 0f, mTranslateY = 0f;
  private FFMpegMovieViewAndroid mMovieView;


  public static VideoScaling getInstance() {
    if (mInstance == null) {
      mInstance = new VideoScaling();
    }
    return mInstance;
  }

  private VideoScaling() {
    super();
  }


  public void setScreenWidthHeight(int width, int height) {
    focusX = width / 2;
    focusY = height / 2;
    mScreenWidth = width;
    mScreenHeight = height;

  }

  public void setFocus(float fX, float fY, boolean g) {
    focusX = fX;
    focusY = fY;
    gesture = g;
  }

  public void setMovieView(FFMpegMovieViewAndroid movieView) {
    mMovieView = movieView;
  }

  public void setScaled(float scaled) {
    this.mScaled = scaled;
    //calculateDrawMatrix();
  }

  public void calculateDrawMatrix(float scaleFactor, float focusX, float focusY, boolean isGesture) {
    if (mScreenWidth > 0 && mScreenHeight > 0 && mMovieView != null) {
      SurfaceHolder surfaceHolder = mMovieView.getHolder();
      //synchronized(this) {
      float translateX;
      float translateY;
      float gesturePercentageOfScreenX;
      float gesturePercentageOfScreenY;
      float viewWidth = 0f;
      float viewHeight = 0f;
      Canvas canvas = surfaceHolder.lockCanvas(null);

      if (canvas == null) {
        return;
      }

      try {
        if (!panning && !rendering) {
          scaling = true;
          Matrix drawMatrix = new Matrix();
          //mViewWidth = (mScaled * mScreenWidth);
          //mViewHeight = (mScaled * mScreenHeight);
          viewWidth = (scaleFactor * mScreenWidth);
          viewHeight = (scaleFactor * mScreenHeight);

          gesturePercentageOfScreenX = focusX / mScreenWidth;
          gesturePercentageOfScreenY = focusY / mScreenHeight;

          if (isGesture) {
            translateX = -((viewWidth - mScreenWidth) * gesturePercentageOfScreenX);
            translateY = -((viewHeight - mScreenHeight) * gesturePercentageOfScreenY);
            mLeftEdge = translateX;
            mTopEdge = translateY;

            //mDrawMatrix.postScale(mScaled, mScaled);
            //mDrawMatrix.postTranslate(translateX, translateY);
          } else {
            translateX = -((viewWidth - mScreenWidth) / 2.0f);
            translateY = -((viewHeight - mScreenHeight) / 2.0f);

            mLeftEdge = translateX;
            mTopEdge = translateY;

            //mDrawMatrix.postScale(mScaled, mScaled);
            //mDrawMatrix.postTranslate(translateX, translateY);
          }

          drawMatrix.postScale(mScaled, mScaled);
          drawMatrix.postTranslate(translateX, translateY);

          //ViewGroup.LayoutParams layoutParams = mMovieView.getLayoutParams();
          //if (viewWidth >= mScreenWidth && viewHeight >= mScreenHeight) {
          synchronized (surfaceHolder) {
            canvas.setMatrix(drawMatrix);
            mMovieView.invalidate();
          }

          /*
          layoutParams.width = (int) viewWidth;
          layoutParams.height = (int) viewHeight;
          mMovieView.setLayoutParams(layoutParams);
          mMovieView.setTranslationX(translateX);
          mMovieView.setTranslationY(translateY);
          */
          //mMovieView.invalidate();
          //} else {
          //Log.d(TAG, "gesturescalingtest smaller than screen width!");
          //}

          //mShouldScale = false;
          //scalingTimerActor.after(10, new Object());

          //surfaceRender();
        }
      } finally {
        surfaceHolder.unlockCanvasAndPost(canvas);
      }
      //}
    }
    scaling = false;
  }

  public int getViewWidth() {
    if (mViewWidth < mScreenWidth) {
      mViewWidth = mScreenWidth;
    }
    return (int) mViewWidth;
  }

  public int getViewHeight() {
    if (mViewHeight < mScreenHeight) {
      mViewHeight = mScreenHeight;
    }
    return (int) mViewHeight;
  }

  public float getTranslateX() {
    if (mLeftEdge > 0) {
      mLeftEdge = 0;
    }
    return mLeftEdge;
  }

  public float getTranslateY() {
    if (mTopEdge > 0) {
      mTopEdge = 0;
    }
    return mTopEdge;
  }

  private Actor scalingTimerActor = new Actor() {
    public Object receive(Object m) {
      mShouldScale = true;
      return null;
    }
  };

  private boolean shouldPan = true;

  public void pan(float tX, float tY) {
    if (shouldPan) {
      if (!scaling && !rendering) {
        panning = true;

        if (mLeftEdge + tX >= 0f) {
          tX = 0;
        }

        if (mTopEdge + tY >= 0f) {
          tY = 0;
        }

        rightEdge = mLeftEdge + mViewWidth;
        bottomEdge = mTopEdge + mViewHeight;

        if (rightEdge + tX <= mScreenWidth) {
          tX = 0;
        }

        if (bottomEdge + tY <= mScreenHeight) {
          tY = 0;
        }

        mLeftEdge += tX;
        mTopEdge += tY;

        //mDrawMatrix.postTranslate(tX, tY);
        //surfaceRender();
        panning = false;
      }
    }
  }
}
