package com.sangxiang.app.widgets;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.sangxiang.app.tools.DisplayUtil;

import static android.R.attr.type;

/**
 * Created by sangxiang on 15/8/17.
 */
public class Custom8View extends View {
    private int r=0;
    private Paint paint;
    private Paint paint1;
    private boolean hasDrawCircle;
    private int degree=0;
    private boolean flg=true;
    int type;
    int count;
    int x,y;
    public Custom8View(Context context) {
        this(context,null);
    }

    public Custom8View(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }


    public Custom8View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }



    private void init() {
        r=new DisplayUtil(getContext()).dpToPixel(50);
        paint=new Paint();
        paint.setColor(Color.parseColor("#3F51B5"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
        paint1=new Paint();
        paint1.setColor(Color.parseColor("#3F51B5"));
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(20);
        paint1.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(1920,1080);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制上半圆
        canvas.drawCircle(r, r, r - 5, paint);
        canvas.drawCircle(r,3*r,r-5,paint);
        int left= count%720;
        if(left>=0&&left<=180) {
            type = 1;
            degree=count;
            flg=true;
             x= (int) (Math.sin(degree*Math.PI/180)*r)+r;

        }else if(left>=180&&left<=360){
            degree=count;
            type=2;
            flg=false;
            x= (int) (Math.sin(degree*Math.PI/180)*r)+r;
            //y= (int) (Math.cos((degree)*Math.PI/180)*r)+ 3*r;
        }else if(left>=360&&left<=540){
            type=3;
            flg=false;
            degree=count;
            x= (int) (Math.sin(degree*Math.PI/180)*r)+r;
            //y= (int) (Math.cos((degree)*Math.PI/180)*r)+ 3*r;
        }else if(left>=540&&left<=720){
            type=4;
            flg=true;
            degree=count;
            x= (int) (Math.sin(degree*Math.PI/180)*r)+r;
            //y= (int) (Math.cos((degree-180)*Math.PI/180)*r)+ r;
        }
        y= (int) (Math.cos((degree)*Math.PI/180)*r)+ (flg?r:-r);

        Log.e("sangxiang", "onDraw: degree="+degree);
        Log.e("sangxiang", "onDraw:x= "+x);
        Log.e("sangxiang", "onDraw:y= "+y);
        canvas.drawCircle(x,y,10,paint1);
    }

    private double getType() {
        double returnValue=0;
        switch (type){
            case 1:returnValue=r;break;
            case 2:returnValue=3*r;break;
            case 3:returnValue=3*r;break;
            case 4:returnValue=r;break;
            default:;
        }
        return returnValue;
    }

    public void setValue(int count){
        this.count=count;
    }
}
