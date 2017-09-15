package com.hubble.registration.ui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

/**
 * @author MC
 *         Simple fade out - and make the view invisible at the end of the animation.
 */
public class FadeOutAnimationAndGoneListener implements AnimationListener {

  private View viewToHide;

  public FadeOutAnimationAndGoneListener(View viewToHide) {
    this.viewToHide = viewToHide;
  }

  public void onAnimationStart(Animation animation) {
  }

  public void onAnimationRepeat(Animation animation) {
  }

  public void onAnimationEnd(Animation animation) {
    viewToHide.setVisibility(View.INVISIBLE);
  }
}
