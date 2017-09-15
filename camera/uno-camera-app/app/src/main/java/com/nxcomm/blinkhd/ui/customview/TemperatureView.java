package com.nxcomm.blinkhd.ui.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.hubbleconnected.camera.R;


public class TemperatureView extends View {
  public static final int C = 1;
  public static final int F = 2;

  private float temperature = 21;
  private int mode = TemperatureView.C;

  private Paint mTextPaint1, mTextPaint2;

  private float width, height;
  Rect rect, rect2, rect1;
  private float density = 1.5f;
  private String mainText, subText;

  private Context mContext;

  public TemperatureView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    initView();
  }

  public void setTextColor(int id_color) {
    this.mTextPaint1.setColor(id_color);
    this.mTextPaint2.setColor(id_color);
  }

  public float convertFtoC(float f) {
    f = (f - 32) * 5f / 9f;
    return f;

  }

  public float convertCtoF(float c) {
    c = 9f * c / 5f + 32;
    return c;
  }

  private void initView() {

    density = getResources().getDisplayMetrics().density;

    mTextPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTextPaint1.setColor(getResources().getColor(R.color.main_blue));
    mTextPaint1.setTextSize(100 * density);
    mTextPaint1.setFakeBoldText(true);

    mTextPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTextPaint2.setColor(getResources().getColor(R.color.main_blue));
    mTextPaint2.setTextSize(40 * density);
    mTextPaint2.setFakeBoldText(true);
    this.mode = TemperatureView.C;

    rect1 = new Rect();
    rect2 = new Rect();
    mainText = "";
    subText = "";
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {

    super.onSizeChanged(w, h, oldw, oldh);
    calcData();
    this.invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    calcData();
  }

  private void calcData() {

    this.width = getMeasuredWidth();

    mTextPaint1.getTextBounds("21", 0, 2, rect1);

    mTextPaint2.getTextBounds("C", 0, 1, rect2);

    if (density < 1) {
      setMeasuredDimension((int) width, rect1.height());
      this.height = (rect1.height());
    } else {
      setMeasuredDimension((int) width, (int) (rect1.height() * density));
      this.height = (rect1.height() * density);
    }


  }

  public void setMainTextSize(float value) {
    this.mTextPaint1.setTextSize(value * density);
    this.mTextPaint2.setTextSize(value * density / 2.5f);
    this.calcData();
    this.invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {


    super.onDraw(canvas);

    if (mode == TemperatureView.C) {
      mainText = Math.round(this.temperature) + "";
      subText = "\u2103";
    } else {
      mainText = Math.round(convertCtoF(this.temperature)) + "";
      subText = "\u2109";
    }

    mTextPaint1.getTextBounds(mainText, 0, mainText.length(), rect1);

    mTextPaint2.getTextBounds(subText, 0, 1, rect2);

    float w = mTextPaint1.measureText("" + mainText);
    int xPos = (int) ((this.width - w) / 2);
    float textHeight = rect1.height();
    int yPos = (int) ((this.height / 2) + (textHeight / 2));
    int xPos1 = (int) (xPos + w);
    int yPos1 = (int) (this.height / 2 - textHeight / 2 + rect2.height());

    canvas.drawText(mainText, xPos, yPos, mTextPaint1);
    canvas.drawText(subText, xPos1, yPos1, mTextPaint2);

  }

  public float getTemperature() {
    return temperature;
  }

  public void setTemperature(float temperature) {
    this.temperature = temperature;
    this.invalidate();
  }

  public int getMode() {
    return mode;
  }

  public void setMode(int mode) {
    this.mode = mode;
    this.invalidate();
  }

}
