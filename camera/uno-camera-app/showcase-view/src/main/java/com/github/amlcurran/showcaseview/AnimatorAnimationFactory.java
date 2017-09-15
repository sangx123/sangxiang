/*
 * Copyright 2014 Alex Curran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.amlcurran.showcaseview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

class AnimatorAnimationFactory implements AnimationFactory {

    public static final int DEFAULT_DURATION = 300;

    private static final String ALPHA = "alpha";
    private static final float INVISIBLE = 0f;
    private static final float VISIBLE = 1f;
    private static final String COORD_X = "x";
    private static final String COORD_Y = "y";
    private static final int INSTANT = 0;

    private final AccelerateDecelerateInterpolator interpolator;

    public AnimatorAnimationFactory() {
        interpolator = new AccelerateDecelerateInterpolator();
    }

    @Override
    public void fadeInView(View target, long duration, final AnimationStartListener listener) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(target, ALPHA, INVISIBLE, VISIBLE);
        oa.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                listener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        oa.start();
    }

    @Override
    public void fadeOutView(View target, long duration, final AnimationEndListener listener) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(target, ALPHA, INVISIBLE);
        oa.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                listener.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        oa.start();
    }

    @Override
    public void animateTargetToPoint(ShowcaseView showcaseView, Point point) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator xAnimator = ObjectAnimator.ofInt(showcaseView, "showcaseX", point.x);
        ObjectAnimator yAnimator = ObjectAnimator.ofInt(showcaseView, "showcaseY", point.y);
        set.playTogether(xAnimator, yAnimator);
        set.setInterpolator(interpolator);
        set.start();
    }

    @Override
    public AnimatorSet createMovementAnimation(View view, float canvasX, float canvasY,
                                               float offsetStartX, float offsetStartY,
                                               float offsetEndX, float offsetEndY,
                                               final AnimationEndListener listener) {
        // view.setAlpha(0);

        // ObjectAnimator alphaIn = ObjectAnimator.ofFloat(view, ALPHA, INVISIBLE, VISIBLE).setDuration(500);

        ObjectAnimator setUpX = ObjectAnimator.ofFloat(view, COORD_X, canvasX + offsetStartX).setDuration(INSTANT);
        ObjectAnimator setUpY = ObjectAnimator.ofFloat(view, COORD_Y, canvasY + offsetStartY).setDuration(INSTANT);

        ObjectAnimator moveX = ObjectAnimator.ofFloat(view, COORD_X, canvasX + offsetEndX).setDuration(1000);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(view, COORD_Y, canvasY + offsetEndY).setDuration(1000);
        moveX.setStartDelay(1000);
        moveY.setStartDelay(1000);

        // ObjectAnimator alphaOut = ObjectAnimator.ofFloat(view, ALPHA, INVISIBLE).setDuration(500);
        // alphaOut.setStartDelay(2500);

        AnimatorSet as = new AnimatorSet();
        as.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        // as.play(setUpX).with(setUpY).before(alphaIn).before(moveX).with(moveY).before(alphaOut);
        as.play(setUpX).with(setUpY).before(moveX).with(moveY);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                listener.onAnimationEnd();
            }
        };
        handler.postDelayed(runnable, 3000);

        return as;
    }

    @Override
    public AnimatorSet createMovementAnimation(View view, float x, float y) {
        ObjectAnimator alphaIn = ObjectAnimator.ofFloat(view, ALPHA, INVISIBLE, VISIBLE).setDuration(500);

        ObjectAnimator setUpX = ObjectAnimator.ofFloat(view, COORD_X, x).setDuration(INSTANT);
        ObjectAnimator setUpY = ObjectAnimator.ofFloat(view, COORD_Y, y).setDuration(INSTANT);

        AnimatorSet as = new AnimatorSet();
        as.play(setUpX).with(setUpY).before(alphaIn);
        return as;
    }

}
