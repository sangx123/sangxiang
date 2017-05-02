package com.sangxiang.app.widgets.TextView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.sangxiang.app.DisplayUtil;
import com.sangxiang.app.R;

/**
 * Created by sangxiang on 26/4/17.
 */

public class CircleTextView extends TextView {

    private int radius;

    private int borderWidth;

    private int borderColor;

    private int fillColor;

    public CircleTextView(Context context) {
        this(context, null);
    }

    public CircleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleTextView);
        borderWidth = typedArray.getDimensionPixelSize(R.styleable.CircleTextView_border_width, 0);
        borderColor = typedArray.getColor(R.styleable.CircleTextView_border_color, Color.WHITE);
        fillColor = typedArray.getColor(R.styleable.CircleTextView_fill_color, Color.WHITE);
        typedArray.recycle();
        DisplayUtil displayUtil=new DisplayUtil(context);
        setPadding(displayUtil.dpToPixel(1), displayUtil.dpToPixel(1), displayUtil.dpToPixel(1),
                displayUtil.dpToPixel(1));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int max = Math.max(measuredWidth, measuredHeight);
        setMeasuredDimension(max, max);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getWidth();
        // 半径
        radius = Math.min(width, height) / 2;
        if (borderWidth > 0) {
            Paint borderPaint = new Paint();
            borderPaint.setColor(borderColor);
            canvas.drawCircle(getWidth() / 2, getWidth() / 2, radius, borderPaint);
        }
        Paint fillPaint = new Paint();
        fillPaint.setColor(fillColor);
        canvas.drawCircle(getWidth() / 2, getWidth() / 2, radius - borderWidth, fillPaint);
        super.onDraw(canvas);
    }

}
