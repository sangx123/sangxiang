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

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Build;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.targets.Target;

import static com.github.amlcurran.showcaseview.AnimationFactory.AnimationEndListener;
import static com.github.amlcurran.showcaseview.AnimationFactory.AnimationStartListener;

/**
 * A view which allows you to showcase areas of your app with an explanation.
 */
public class ShowcaseView extends RelativeLayout
        implements View.OnTouchListener, ShowcaseViewApi {
    private static final String TAG = "ShowcaseView";

    private static final int HOLO_BLUE = Color.parseColor("#33B5E5");
    public static final int UNDEFINED = -1;
    public static final int LEFT_OF_SHOWCASE = 0;
    public static final int RIGHT_OF_SHOWCASE = 2;
    public static final int ABOVE_SHOWCASE = 1;
    public static final int BELOW_SHOWCASE = 3;

    private Button mEndButton;
    private final TextDrawer textDrawer;
    private ShowcaseDrawer showcaseDrawer;
    private final ShowcaseAreaCalculator showcaseAreaCalculator;
    private final AnimationFactory animationFactory;
    private final ShotStateStore shotStateStore;

    // Showcase metrics
    private int showcaseX = -1;
    private int showcaseY = -1;
    private float scaleMultiplier = 1f;

    // Touch items
    private boolean blockTouchOutsideCircle = true;
    private boolean blockTouchHoldScreen;
    private boolean hideOnTouch = false;
    private OnShowcaseEventListener mEventListener = OnShowcaseEventListener.NONE;

    private boolean hasAlteredText = false;
    private boolean hasNoTarget = false;
    private boolean shouldCentreText;
    private Bitmap bitmapBuffer;

    // Animation items
    private long fadeInMillis;
    private long fadeOutMillis;
    private boolean isShowing;
    private int backgroundColor;
    private View mHandy;
    private int showcaseColor;

    private Listener mListener;

    protected ShowcaseView(Context context, boolean newStyle) {
        this(context, null, R.styleable.CustomTheme_showcaseViewStyle, newStyle);
    }

    protected ShowcaseView(Context context, AttributeSet attrs, int defStyle, boolean newStyle) {
        super(context, attrs, defStyle);

        animationFactory = new AnimatorAnimationFactory();
        showcaseAreaCalculator = new ShowcaseAreaCalculator();
        shotStateStore = new ShotStateStore(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            this.setFitsSystemWindows(true);
        }
        getViewTreeObserver().addOnPreDrawListener(new CalculateTextOnPreDraw());
        getViewTreeObserver().addOnGlobalLayoutListener(new UpdateOnGlobalLayout());

        // Get the attributes for the ShowcaseView
        final TypedArray styled = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.ShowcaseView, R.attr.showcaseViewStyle,
                        R.style.ShowcaseView);

        // Set the default animation times
        fadeInMillis = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        fadeOutMillis = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mEndButton = (Button) LayoutInflater.from(context).inflate(R.layout.showcase_button, this, false);
        if (newStyle) {
            showcaseDrawer = new NewShowcaseDrawer(getResources());
        } else {
            showcaseDrawer = new StandardShowcaseDrawer(getResources());
        }
        textDrawer = new TextDrawer(getResources(), showcaseAreaCalculator, getContext());

        updateStyle(styled, false);

        init();
    }

    private void init() {

        setOnTouchListener(this);

        if (mEndButton.getParent() == null) {
            int margin = (int) getResources().getDimension(R.dimen.button_margin);
            RelativeLayout.LayoutParams lps = (LayoutParams) generateDefaultLayoutParams();
            lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lps.setMargins(margin, margin, margin, margin);

            mEndButton.setLayoutParams(lps);
            mEndButton.setText(android.R.string.ok);
            mEndButton.setOnClickListener(hideOnClickListener);

            addView(mEndButton);
        }

    }

    private boolean hasShot() {
        return shotStateStore.hasShot();
    }

    void setShowcasePosition(Point point) {
        setShowcasePosition(point.x, point.y);
    }

    void setShowcasePosition(int x, int y) {
        if (shotStateStore.hasShot()) {
            return;
        }
        showcaseX = x;
        showcaseY = y;
        //init();
        invalidate();
    }

    public void setTarget(final Target target) {
        setShowcase(target, false);
    }

    public void setShowcase(final Target target, final boolean animate) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!shotStateStore.hasShot()) {
                    updateBitmap();
                    Point targetPoint = target.getPoint();
                    if (targetPoint != null) {
                        hasNoTarget = false;
                        if (animate) {
                            animationFactory.animateTargetToPoint(ShowcaseView.this, targetPoint);
                        } else {
                            setShowcasePosition(targetPoint);
                            if (showcaseDrawer.isCircleStyle()) {
                                float newRadius = target.getWidth() * 0.6f;
                                if (newRadius >= 0.0f) {
                                    showcaseDrawer.setBlockedRadius(newRadius);
                                }
                            }
                        }
                    } else {
                        hasNoTarget = true;
                        invalidate();
                    }
                }
            }
        }, 100);
    }

    private void updateBitmap() {
        if (bitmapBuffer == null || haveBoundsChanged()) {
            if(bitmapBuffer != null)
        		    bitmapBuffer.recycle();
            int bmWidth = getMeasuredWidth();
            int bmHeight = getMeasuredHeight();
            if (bmWidth > 0 && bmHeight > 0) {
                bitmapBuffer = Bitmap.createBitmap(bmWidth, bmHeight, Bitmap.Config.ARGB_8888);
            } else {
                Log.e(TAG, "Invalid bitmap dimensions: " + String.format("w %d, h %d", bmWidth, bmHeight));
            }
        }
    }

    private boolean haveBoundsChanged() {
        return getMeasuredWidth() != bitmapBuffer.getWidth() ||
                getMeasuredHeight() != bitmapBuffer.getHeight();
    }

    public boolean hasShowcaseView() {
        return (showcaseX != Const.NULL_POINT && showcaseY != Const.NULL_POINT) && !hasNoTarget;
    }

    public void setShowcaseX(int x) {
        setShowcasePosition(x, showcaseY);
    }

    public void setShowcaseY(int y) {
        setShowcasePosition(showcaseX, y);
    }

    public int getShowcaseX() {
        return showcaseX;
    }

    public int getShowcaseY() {
        return showcaseY;
    }

    /**
     * Override the standard button click event
     *
     * @param listener Listener to listen to on click events
     */
    public void overrideButtonClick(Listener listener) {
        mListener = listener;
    }

    public void setOnShowcaseEventListener(OnShowcaseEventListener listener) {
        if (listener != null) {
            mEventListener = listener;
        } else {
            mEventListener = OnShowcaseEventListener.NONE;
        }
    }

    public void setButtonText(CharSequence text) {
        if (mEndButton != null) {
            mEndButton.setText(text);
        }
    }

    private void recalculateText() {
        boolean recalculatedCling = showcaseAreaCalculator.calculateShowcaseRect(showcaseX, showcaseY, showcaseDrawer);
        boolean recalculateText = recalculatedCling || hasAlteredText;
        if (recalculateText) {
            textDrawer.calculateTextPosition(getMeasuredWidth(), getMeasuredHeight(), this, shouldCentreText);
        }
        hasAlteredText = false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (showcaseX >= 0 && showcaseY >= 0 && !shotStateStore.hasShot() && bitmapBuffer != null) {
            if (showcaseDrawer != null) {
                if (showcaseDrawer.isCircleStyle() && showcaseDrawer.getBlockedRadius() >= 0) {
                    //Draw background color
                    showcaseDrawer.erase(bitmapBuffer);
                    // Draw the showcase drawable
                    if (!hasNoTarget) {
                        showcaseDrawer.drawShowcase(bitmapBuffer, showcaseX, showcaseY, scaleMultiplier);
                        showcaseDrawer.drawToCanvas(canvas, bitmapBuffer);
                    }
                    // Draw the text on the screen, recalculating its position if necessary
                    textDrawer.draw(canvas);
                }
            }
        }
        super.dispatchDraw(canvas);

    }

    /**
     * Adds an animated hand performing a gesture.
     * All parameters passed to this method are relative to the center of the showcased view.
     * @param offsetStartX  x-offset of the start position
     * @param offsetStartY  y-offset of the start position
     * @param offsetEndX    x-offset of the end position
     * @param offsetEndY    y-offset of the end position
     */
    public void animateGesture(float offsetStartX, float offsetStartY, float offsetEndX,
                               float offsetEndY) {
        animateGesture(offsetStartX, offsetStartY, offsetEndX, offsetEndY, false);
    }

    /**
     * Adds an animated hand performing a gesture.
     * @param startX                x-coordinate or x-offset of the start position
     * @param startY                y-coordinate or x-offset of the start position
     * @param endX                  x-coordinate or x-offset of the end position
     * @param endY                  y-coordinate or x-offset of the end position
     * @param absoluteCoordinates   If true, this will use absolute coordinates instead of coordinates relative to the center of the showcased view
     */
    public void animateGesture(float startX, float startY, float endX,
                               float endY, boolean absoluteCoordinates) {
        mHandy = LayoutInflater.from(getContext()).inflate(R.layout.handy, null);
        addView(mHandy);
        moveHand(startX, startY, endX, endY, absoluteCoordinates, new AnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                // removeView(mHandy);
            }
        });
    }

    private void moveHand(float startX, float startY, float endX,
                          float endY, boolean absoluteCoordinates, AnimationEndListener listener) {
        animationFactory.createMovementAnimation(mHandy, absoluteCoordinates ? 0 : showcaseX,
            absoluteCoordinates ? 0 : showcaseY, startX, startY, endX, endY, listener)
            .start();
    }

    @Override
    public void hide() {
        clearBitmap();
        // If the type is set to one-shot, store that it has shot
        shotStateStore.storeShot();
        mEventListener.onShowcaseViewHide(this);
        fadeOutShowcase();
    }

    private void clearBitmap() {
        if (bitmapBuffer != null && !bitmapBuffer.isRecycled()) {
            bitmapBuffer.recycle();
            bitmapBuffer = null;
        }
    }

    private void fadeOutShowcase() {
        animationFactory.fadeOutView(
            this, fadeOutMillis, new AnimationEndListener() {
                @Override
                public void onAnimationEnd() {
                    setVisibility(View.GONE);
                    isShowing = false;
                    mEventListener.onShowcaseViewDidHide(ShowcaseView.this);
                }
            }
        );
    }

    @Override
    public void show() {
        isShowing = true;
        mEventListener.onShowcaseViewShow(this);
        fadeInShowcase();
    }

    private void fadeInShowcase() {
        animationFactory.fadeInView(
                this, fadeInMillis,
                new AnimationStartListener() {
                    @Override
                    public void onAnimationStart() {
                        setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (blockTouchHoldScreen) {
            return true;
        }
        float xDelta = Math.abs(motionEvent.getRawX() - showcaseX);
        float yDelta = Math.abs(motionEvent.getRawY() - showcaseY);
        double distanceFromFocus = Math.sqrt(Math.pow(xDelta, 2) + Math.pow(yDelta, 2));

        if (MotionEvent.ACTION_UP == motionEvent.getAction() &&
                hideOnTouch && distanceFromFocus > showcaseDrawer.getBlockedRadius()) {
            this.hide();
            return true;
        }

        return blockTouchOutsideCircle && distanceFromFocus > showcaseDrawer.getBlockedRadius();
    }

    private static void insertShowcaseView(ShowcaseView showcaseView, Activity activity) {
        // prevent the showcaseView overlaps status bar and navigation bar
        ((ViewGroup) activity.findViewById(android.R.id.content)).addView(showcaseView);
        //((ViewGroup) activity.getWindow().getDecorView()).addView(showcaseView);
        if (!showcaseView.hasShot()) {
            showcaseView.show();
        } else {
            showcaseView.hideImmediate();
        }
    }

    private void hideImmediate() {
        isShowing = false;
        setVisibility(GONE);
    }

    @Override
    public void setContentTitle(CharSequence title) {
        textDrawer.setContentTitle(title);
    }

    @Override
    public void setContentText(CharSequence text) {
        textDrawer.setContentText(text);
    }

    private void setScaleMultiplier(float scaleMultiplier) {
        this.scaleMultiplier = scaleMultiplier;
    }

    public void hideButton() {
        mEndButton.setVisibility(GONE);
    }

    public void showButton() {
        mEndButton.setVisibility(VISIBLE);
    }

    /**
     * Builder class which allows easier creation of {@link ShowcaseView}s.
     * It is recommended that you use this Builder class.
     */
    public static class Builder {

        final ShowcaseView showcaseView;
        private final Activity activity;

        public Builder(Activity activity) {
            this(activity, false);
        }

        /**
         * @param useNewStyle should use "new style" showcase (see {@link #withNewStyleShowcase()}
         * @deprecated use {@link #withHoloShowcase()}, {@link #withNewStyleShowcase()}, or
         *  {@link #setShowcaseDrawer(ShowcaseDrawer)}
         */
        @Deprecated
        public Builder(Activity activity, boolean useNewStyle) {
            this.activity = activity;
            this.showcaseView = new ShowcaseView(activity, useNewStyle);
            this.showcaseView.setTarget(Target.NONE);
        }

        /**
         * Create the {@link com.github.amlcurran.showcaseview.ShowcaseView} and show it.
         *
         * @return the created ShowcaseView
         */
        public ShowcaseView build() {
            insertShowcaseView(showcaseView, activity);
            return showcaseView;
        }

        /**
         * Draw a holo-style showcase. This is the default.<br/>
         * <img alt="Holo showcase example" src="../../../../../../../../example2.png" />
         */
        public Builder withHoloShowcase() {
            return setShowcaseDrawer(new StandardShowcaseDrawer(activity.getResources()));
        }

        /**
         * Draw a new-style showcase.<br/>
         * <img alt="Holo showcase example" src="../../../../../../../../example.png" />
         */
        public Builder withNewStyleShowcase() {
            return setShowcaseDrawer(new NewShowcaseDrawer(activity.getResources()));
        }

        /**
         * Draw a material style showcase.
         * <img alt="Material showcase" src="../../../../../../../../material.png" />
         */
        public Builder withMaterialShowcase() {
            return setShowcaseDrawer(new MaterialShowcaseDrawer(activity.getResources()));
        }

        /**
         * Set a custom showcase drawer which will be responsible for measuring and drawing the showcase
         */
        public Builder setShowcaseDrawer(ShowcaseDrawer showcaseDrawer) {
            showcaseView.setShowcaseDrawer(showcaseDrawer);
            return this;
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public Builder setContentTitle(int resId) {
            return setContentTitle(activity.getString(resId));
        }

        /**
         * Set the title text shown on the ShowcaseView.
         */
        public Builder setContentTitle(CharSequence title) {
            showcaseView.setContentTitle(title);
            return this;
        }

        /**
         * Set the descriptive text shown on the ShowcaseView.
         */
        public Builder setContentText(int resId) {
            return setContentText(activity.getString(resId));
        }

        /**
         * Set the descriptive text shown on the ShowcaseView.
         */
        public Builder setContentText(CharSequence text) {
            showcaseView.setContentText(text);
            return this;
        }

        /**
         * Set the target of the showcase.
         *
         * @param target a {@link com.github.amlcurran.showcaseview.targets.Target} representing
         *               the item to showcase (e.g., a button, or action item).
         */
        public Builder setTarget(Target target) {
            showcaseView.setTarget(target);
            return this;
        }

        /**
         * Set the style of the ShowcaseView. See the sample app for example styles.
         */
        public Builder setStyle(int theme) {
            showcaseView.setStyle(theme);
            return this;
        }

        /**
         * Set a listener which will override the button clicks.
         * <p/>
         * Note that you will have to manually hide the ShowcaseView
         */
        public Builder setOnClickListener(Listener listener) {
            showcaseView.overrideButtonClick(listener);
            return this;
        }

        /**
         * Don't make the ShowcaseView block touches on itself. This doesn't
         * block touches in the showcased area.
         * <p/>
         * By default, the ShowcaseView does block touches
         */
        public Builder doNotBlockTouches() {
            showcaseView.blockTouchOutsideCircle(false);
            return this;
        }

        /**
         * Make the ShowcaseView block touches even on showcased area.
         * <p/>
         * By default, the ShowcaseView does not block showcase area.
         */
        public Builder blockTouchHoldScreen() {
            showcaseView.blockTouchHoldScreen(true);
            return this;
        }

        /**
         * Make this ShowcaseView hide when the user touches outside the showcased area.
         * This enables {@link #doNotBlockTouches()} as well.
         * <p/>
         * By default, the ShowcaseView doesn't hide on touch.
         */
        public Builder hideOnTouchOutside() {
            showcaseView.blockTouchOutsideCircle(true);
            showcaseView.setHideOnTouchOutside(true);
            return this;
        }

        /**
         * Set the ShowcaseView to only ever show once.
         *
         * @param shotId a unique identifier (<em>across the app</em>) to store
         *               whether this ShowcaseView has been shown.
         */
        public Builder singleShot(long shotId) {
            showcaseView.setSingleShot(shotId);
            return this;
        }

        public Builder setShowcaseEventListener(OnShowcaseEventListener showcaseEventListener) {
            showcaseView.setOnShowcaseEventListener(showcaseEventListener);
            return this;
        }

        /**
         * Sets the paint that will draw the text as specified by {@link #setContentText(CharSequence)}
         * or {@link #setContentText(int)}
         */
        public Builder setContentTextPaint(TextPaint textPaint) {
            showcaseView.setContentTextPaint(textPaint);
            return this;
        }

        /**
         * Sets the paint that will draw the text as specified by {@link #setContentTitle(CharSequence)}
         * or {@link #setContentTitle(int)}
         */
        public Builder setContentTitlePaint(TextPaint textPaint) {
            showcaseView.setContentTitlePaint(textPaint);
            return this;
        }

        /**
         * Replace the end button with the one provided. Note that this resets any OnClickListener provided
         * by {@link #setOnClickListener(OnClickListener)}, so call this method before that one.
         */
        public Builder replaceEndButton(Button button) {
            showcaseView.setEndButton(button);
            return this;
        }

        /**
         * Replace the end button with the one provided. Note that this resets any OnClickListener provided
         * by {@link #setOnClickListener(OnClickListener)}, so call this method before that one.
         */
        public Builder replaceEndButton(int buttonResourceId) {
            View view = LayoutInflater.from(activity).inflate(buttonResourceId, showcaseView, false);
            if (!(view instanceof Button)) {
                throw new IllegalArgumentException("Attempted to replace showcase button with a layout which isn't a button");
            }
            return replaceEndButton((Button) view);
        }
    }

    private void setEndButton(Button button) {
        LayoutParams copyParams = (LayoutParams) mEndButton.getLayoutParams();
        mEndButton.setOnClickListener(null);
        removeView(mEndButton);
        mEndButton = button;
        button.setOnClickListener(hideOnClickListener);
        button.setLayoutParams(copyParams);
        addView(button);
    }

    private void setShowcaseDrawer(ShowcaseDrawer showcaseDrawer) {
        this.showcaseDrawer = showcaseDrawer;
        this.showcaseDrawer.setBackgroundColour(backgroundColor);
        this.showcaseDrawer.setShowcaseColour(showcaseColor);
        hasAlteredText = true;
        invalidate();
    }

    private void setContentTitlePaint(TextPaint textPaint) {
        this.textDrawer.setTitlePaint(textPaint);
        hasAlteredText = true;
        invalidate();
    }

    private void setContentTextPaint(TextPaint paint) {
        this.textDrawer.setContentPaint(paint);
        hasAlteredText = true;
        invalidate();
    }

    /**
     * Set whether the text should be centred in the screen, or left-aligned (which is the default).
     */
    public void setShouldCentreText(boolean shouldCentreText) {
        this.shouldCentreText = shouldCentreText;
        hasAlteredText = true;
        invalidate();
    }

    /**
     * @see com.github.amlcurran.showcaseview.ShowcaseView.Builder#setSingleShot(long)
     */
    private void setSingleShot(long shotId) {
        shotStateStore.setSingleShot(shotId);
    }

    /**
     * Change the position of the ShowcaseView's button from the default bottom-right position.
     *
     * @param layoutParams a {@link android.widget.RelativeLayout.LayoutParams} representing
     *                     the new position of the button
     */
    @Override
    public void setButtonPosition(RelativeLayout.LayoutParams layoutParams) {
        mEndButton.setLayoutParams(layoutParams);
    }

    /**
     * Sets the text alignment of the detail text
     */
    public void setDetailTextAlignment(Layout.Alignment textAlignment) {
        textDrawer.setDetailTextAlignment(textAlignment);
        hasAlteredText = true;
        invalidate();
    }

    /**
     * Sets the text alignment of the title text
     */
    public void setTitleTextAlignment(Layout.Alignment textAlignment) {
        textDrawer.setTitleTextAlignment(textAlignment);
        hasAlteredText = true;
        invalidate();
    }

    /**
     * Set the duration of the fading in and fading out of the ShowcaseView
     */
    private void setFadeDurations(long fadeInMillis, long fadeOutMillis) {
        this.fadeInMillis = fadeInMillis;
        this.fadeOutMillis = fadeOutMillis;
    }

    public void forceTextPosition(int textPosition) {
        textDrawer.forceTextPosition(textPosition);
        hasAlteredText = true;
        invalidate();
    }

    /**
     * @see com.github.amlcurran.showcaseview.ShowcaseView.Builder#hideOnTouchOutside()
     */
    @Override
    public void setHideOnTouchOutside(boolean hideOnTouch) {
        this.hideOnTouch = hideOnTouch;
    }

    /**
     * @see com.github.amlcurran.showcaseview.ShowcaseView.Builder#doNotBlockTouches()
     */
    @Override
    public void blockTouchOutsideCircle(boolean blockTouches) {
        this.blockTouchOutsideCircle = blockTouches;
    }

    /**
     * @see com.github.amlcurran.showcaseview.ShowcaseView.Builder#doNotBlockTouches()
     */
    @Override
    public void blockTouchHoldScreen(boolean blockTouches) {
        this.blockTouchHoldScreen = blockTouches;
    }

    /**
     * @see com.github.amlcurran.showcaseview.ShowcaseView.Builder#setStyle(int)
     */
    @Override
    public void setStyle(int theme) {
        TypedArray array = getContext().obtainStyledAttributes(theme, R.styleable.ShowcaseView);
        updateStyle(array, true);
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    private void updateStyle(TypedArray styled, boolean invalidate) {
        backgroundColor = styled.getColor(R.styleable.ShowcaseView_sv_backgroundColor, Color.argb(128, 80, 80, 80));
        showcaseColor = styled.getColor(R.styleable.ShowcaseView_sv_showcaseColor, HOLO_BLUE);
        String buttonText = styled.getString(R.styleable.ShowcaseView_sv_buttonText);
        if (TextUtils.isEmpty(buttonText)) {
            buttonText = getResources().getString(android.R.string.ok);
        }
        boolean tintButton = styled.getBoolean(R.styleable.ShowcaseView_sv_tintButtonColor, true);

        int titleTextAppearance = styled.getResourceId(R.styleable.ShowcaseView_sv_titleTextAppearance,
                R.style.TextAppearance_ShowcaseView_Title);
        int detailTextAppearance = styled.getResourceId(R.styleable.ShowcaseView_sv_detailTextAppearance,
                R.style.TextAppearance_ShowcaseView_Detail);

        styled.recycle();

        showcaseDrawer.setShowcaseColour(showcaseColor);
        showcaseDrawer.setBackgroundColour(backgroundColor);
        // tintButton(showcaseColor, tintButton);
        mEndButton.setText(buttonText);
        textDrawer.setTitleStyling(titleTextAppearance);
        textDrawer.setDetailStyling(detailTextAppearance);
        hasAlteredText = true;

        if (invalidate) {
            invalidate();
        }
    }

    private void tintButton(int showcaseColor, boolean tintButton) {
        if (tintButton) {
            mEndButton.getBackground().setColorFilter(showcaseColor, PorterDuff.Mode.MULTIPLY);
        } else {
            mEndButton.getBackground().setColorFilter(HOLO_BLUE, PorterDuff.Mode.MULTIPLY);
        }
    }

    private class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {
            if (!shotStateStore.hasShot()) {
                updateBitmap();
            }
        }
    }

    private class CalculateTextOnPreDraw implements ViewTreeObserver.OnPreDrawListener {

        @Override
        public boolean onPreDraw() {
            recalculateText();
            return true;
        }
    }

    private OnClickListener hideOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            hide();
            if (mListener != null) {
                mListener.onGotIt();
            }
        }
    };

    public interface Listener {
        void onGotIt();
    }

}
