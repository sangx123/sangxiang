package com.msc3;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

/* Used to manage graphic change when button is pressed / released 
* Need 2 images : Highlighted image/Normal Image
* */
public class ButtonTouchListener implements OnTouchListener {

  private Drawable highlight_drawable;
  private Drawable normal_drawable;
  private boolean isPressed;
  private OnTouchListener mXtraListener;

  public ButtonTouchListener(Drawable normal, Drawable hl) {
    normal_drawable = normal;
    if (hl == null) {
      highlight_drawable = normal;
    } else {
      highlight_drawable = hl;
    }

    isPressed = false;
    mXtraListener = null;
  }

  @Override
  public boolean onTouch(View buttonView, MotionEvent event) {

    switch (event.getAction() & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:
        isPressed = true;
        ((ImageView) buttonView).setImageDrawable(highlight_drawable);

        if (mXtraListener != null) {
          mXtraListener.onTouch(buttonView, event);
        }

        break;
      case MotionEvent.ACTION_MOVE:
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        if (isPressed) {
          ((ImageView) buttonView).setImageDrawable(normal_drawable);
          isPressed = false;

          if (mXtraListener != null) {
            mXtraListener.onTouch(buttonView, event);
          }
        }

        break;
      default:
        break;
    }

    return false;
  }


  public void registerOnTouchListener(OnTouchListener extraListener) {
    this.mXtraListener = extraListener;
  }

}