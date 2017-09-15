package com.nxcomm.blinkhd.ui.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubbleconnected.camera.R;

import java.util.ArrayList;
import java.util.HashMap;

import base.hubble.PublicDefineGlob;

/**
 * Created by BinhNguyen on 10/6/2015.
 */
public class MvrScheduleView extends View {

  private static String[] DAYS;
  private static String[] HOURS;
  private static final String[] HOURS_24 = new String[]{"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
  private static String[] HOURS_12;

  private static final float TEXT_SIZE_HOUR = 10.25f;
  private static final float TEXT_SIZE_DAY = 14.25f;

  private final int mTextHourMarginHor, mTextDayMarginVer;

  private int mWidth, mHeight; // size of view
  private int mUnitWidth, mUnitHeight;
  private int mTopBarHeight, mLeftBarWidth;

  private int mStandardHeight; // minimum height finger can touch

  private TextPaint mTextPaint; // draw text header
  private Paint mPaint; // draw shape

  private HashMap<String, ArrayList<DataRect>> dataToDraw;
  private Listener mListener;

  private Rect tempTextBound = new Rect();
  private int hourTextHeight, dayTextHeight;
  private int hourTextWidth;
  private int drawnRectMargin;

  private RectF mTouchRect;
  private Bitmap mAddScheduleBitmap;

  private boolean isSelectionMode;
  private Bitmap mCheckOn, mCheckOff;
  private SecureConfig settings = HubbleApplication.AppConfig;

  public void setDataToDraw(HashMap<String, ArrayList<String>> value) {
    // clear selection mode if needed
    setSelectionMode(false);
    // create DataRect hash map base on given data
    if (value == null || value.size() == 0) {
      return;
    }
    dataToDraw = new HashMap<>();
    for (int i = 0; i < PublicDefine.KEYS.length; i++) {
      ArrayList<String> timeEntries = value.get(PublicDefine.KEYS[i]);
      ArrayList<DataRect> temp = new ArrayList<>();
      for (String time : timeEntries) {
        temp.add(new DataRect(i, time));
      }
      dataToDraw.put(PublicDefine.KEYS[i], temp);
    }
    // refresh view if it appeared
    if (mWidth != 0 && mHeight != 0) {
      invalidate();
    }
  }

  public void setListener(Listener listener) {
    mListener = listener;
  }

  public void setSelectionMode(boolean value) {
    isSelectionMode = value;
    if (mTouchRect != null) {
      mTouchRect = null;
    }
    invalidate();
  }

  public boolean isSelectionMode() {
    return isSelectionMode;
  }


  public MvrScheduleView(Context context) {
    this(context, null);
  }

  public MvrScheduleView(Context context, AttributeSet attrs) {
    super(context, attrs);

    setOnTouchListener(new CustomTouchListener(context) {

      @Override
      public void onClick(MotionEvent motionEvent) {
        super.onClick(motionEvent);
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        // rect that indicate schedule will be receive touch at first
        if (dataToDraw != null && dataToDraw.size() != 0) {
          for (int i = 0; i < PublicDefine.KEYS.length; i++) {
            ArrayList<DataRect> dataRects = dataToDraw.get(PublicDefine.KEYS[i]);
            for (DataRect dataRect : dataRects) {
              if (dataRect.checkTouchInside(x, y)) {
                boolean needValidateView = false;
                if (mTouchRect != null) { // clear temporary touch rect if it exists
                  mTouchRect = null;
                  needValidateView = true;
                }
                if (isSelectionMode) { // revert selection to this dataRect in selection mode
                  dataRect.isChecked = !dataRect.isChecked;
                  needValidateView = true;
                } else {
                  if (mListener != null) {
                    mListener.onScheduleClicked(dataRect.time, dataRect.column);
                  }
                }
                // refresh view if needed
                if (needValidateView) {
                  invalidate();
                }
                return;
              }
            }
          }
        }
        if (isSelectionMode) { // have no touch rect in selection mode
          return;
        }
        // if no data rect is touched, check the touchRect or create it
        if (mTouchRect != null && mTouchRect.contains(x, y)) {
          if (mListener != null) {
            int column = (int) ((mTouchRect.left - mLeftBarWidth) / mUnitWidth);
            int row = (int) ((mTouchRect.top - mTopBarHeight) / mUnitHeight);
            String timeSpan = (row != 23) ? String.format("%02d00-%02d00", row, row + 1) : "2300-2359";
            mListener.onCreateSchedule(timeSpan, column);
          }
          mTouchRect = null;
        } else {
          mTouchRect = new RectF();
          mTouchRect.left = x - (x - mLeftBarWidth) % mUnitWidth;
          mTouchRect.top = y - (y - mTopBarHeight) % mUnitHeight;
          mTouchRect.right = mTouchRect.left + mUnitWidth;
          mTouchRect.bottom = mTouchRect.top + mUnitHeight;
        }
        // update view
        invalidate();
      }
    });

    mAddScheduleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.add_camera);
    mCheckOn = BitmapFactory.decodeResource(getResources(), R.drawable.btn_mvr_check_on);
    mCheckOff = BitmapFactory.decodeResource(getResources(), R.drawable.btn_mvr_check_off);

    mTextPaint = new TextPaint();
    mTextPaint.setAntiAlias(true);

    mPaint = new Paint();
    mPaint.setStyle(Paint.Style.FILL);

    mStandardHeight = (int) Util.dp2Px(context, 44);
    mTextHourMarginHor = (int) Util.dp2Px(context, 6);
    mTextDayMarginVer = (int) Util.dp2Px(context, 12);
    drawnRectMargin = (int) Util.dp2Px(context, 2);

    String tempStr = String.format("23 %s", context.getString(R.string.half_day_pm));
    mTextPaint.setTextSize(Util.sp2Px(context, TEXT_SIZE_HOUR));
    mTextPaint.getTextBounds(tempStr, 0, tempStr.length(), tempTextBound);

    hourTextWidth = tempTextBound.width();
    hourTextHeight = tempTextBound.height();

    mLeftBarWidth = hourTextWidth + mTextHourMarginHor * 2;

    tempStr = context.getString(R.string.wednesday_short);
    mTextPaint.setTextSize(Util.sp2Px(context, TEXT_SIZE_DAY));
    mTextPaint.getTextBounds(tempStr, 0, tempStr.length(), tempTextBound);

    dayTextHeight = tempTextBound.height();

    mTopBarHeight = dayTextHeight + mTextDayMarginVer * 2;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int tempWidth = MeasureSpec.getSize(widthMeasureSpec);
    int tempHeight = MeasureSpec.getSize(heightMeasureSpec);

    if (tempWidth == 0 || tempHeight == 0) {
      return;
    }
    if (tempWidth == mWidth && tempHeight == mHeight) {
      return;
    }

    mWidth = tempWidth;
    mHeight = tempHeight;

    Log.i("debug", "Size changed: width = " + mWidth + " height = " + mHeight);

    mUnitWidth = (mWidth - mLeftBarWidth) / 7;
    mUnitHeight = (mHeight - mTopBarHeight) / 24;

    if (mUnitHeight != 0 && mUnitHeight < mStandardHeight) {
      mUnitHeight = mStandardHeight;
      mHeight = mTopBarHeight + 24 * mUnitHeight;
      setMeasuredDimension(mWidth, mUnitHeight * 24 + mTopBarHeight);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (mWidth == 0 || mHeight == 0) {
      return;
    }

    drawGrid(canvas);
    drawDayHeader(canvas);
    drawHourHeader(canvas);

    if (mTouchRect != null) {
      mPaint.setColor(0xff2299CB);
      canvas.drawRect(mTouchRect, mPaint);
      // draw add icon
      float xPos = mTouchRect.left + (mUnitWidth - mAddScheduleBitmap.getWidth()) / 2;
      float yPos = mTouchRect.top + (mUnitHeight - mAddScheduleBitmap.getHeight()) / 2;
      canvas.drawBitmap(mAddScheduleBitmap, xPos, yPos, mPaint);
    }
    drawData(canvas);
  }

  private void drawGrid(Canvas canvas) {

    for (int i = 0; i < 7; i++) {
      mPaint.setColor(0xff979797);
      int xPos = mLeftBarWidth + i * mUnitWidth;
      canvas.drawLine(xPos, 0, xPos, mHeight, mPaint);
      //Fill header colors for day
      RectF dyaHeaderRect = new RectF();
      dyaHeaderRect.left = xPos;
      dyaHeaderRect.top = 0;
      dyaHeaderRect.right = mLeftBarWidth + (i+1) * mUnitWidth;
      dyaHeaderRect.bottom = mTopBarHeight;
      if(i%2 == 0) {
        mPaint.setColor(0xff1c5f7b);
      } else {
        mPaint.setColor(0xff2689b2);
      }
      canvas.drawRect(dyaHeaderRect, mPaint);
    }

    mPaint.setColor(0xff979797);
    for (int i = 0; i < 24; i++) {
      int yPos = mTopBarHeight + i * mUnitHeight;
      canvas.drawLine(mLeftBarWidth, yPos, mWidth, yPos, mPaint);
    }
  }

  private void drawData(Canvas canvas) {
    if (dataToDraw == null || dataToDraw.size() == 0) {
      return;
    }
    mPaint.setColor(0xff55acd0);

    for (int i = 0; i < PublicDefine.KEYS.length; i++) {
      ArrayList<DataRect> dataRects = dataToDraw.get(PublicDefine.KEYS[i]);
      for (DataRect dataRect : dataRects) {
        dataRect.calculateRect();
        RectF tempRect = dataRect.getRect();
        canvas.drawRect(tempRect.left + drawnRectMargin, tempRect.top,
            tempRect.right - drawnRectMargin, tempRect.bottom, mPaint);
        // draw checkbox in selection mode
        if (isSelectionMode) {
          Bitmap tempBitmap = dataRect.isChecked ? mCheckOn : mCheckOff;
          float xPos = tempRect.left + (tempRect.width() - tempBitmap.getWidth()) / 2;
          float yPos = tempRect.top + (tempRect.height() - tempBitmap.getHeight()) / 2;
          canvas.drawBitmap(tempBitmap, xPos, yPos, mPaint);
        }
      }
    }
  }

  private void drawHourHeader(Canvas canvas) {
    if (HOURS_12 == null) {
      Context context = getContext();
      String halfDayAM = context.getString(R.string.half_day_am);
      String halfDayPM = context.getString(R.string.half_day_pm);
      HOURS_12 = new String[24];
      int temp;
      for (int i = 0; i < 12; i++) {
        temp = (i == 0) ? 12 : i;
        HOURS_12[i] = String.valueOf(temp) + " " + halfDayAM;
        HOURS_12[i + 12] = String.valueOf(temp) + " " + halfDayPM;
      }
    }
    mTextPaint.setColor(0xff0f5471);
    mTextPaint.setTextSize(Util.sp2Px(getContext(), 10.25f));
    if (settings.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0) == 0) {
      HOURS = HOURS_12;
    } else {
      HOURS = HOURS_24;
    }
    int numberOfPoints = HOURS.length;
    for (int i = 0; i < numberOfPoints; i++) {
      int yPos = mTopBarHeight + i * mUnitHeight + hourTextHeight / 2;
      canvas.drawText(HOURS[i], 0, HOURS[i].length(), mTextHourMarginHor, yPos, mTextPaint);
    }
  }

  private void drawDayHeader(Canvas canvas) {
    if (DAYS == null) {
      Context context = getContext();
      DAYS = new String[]{
          context.getString(R.string.sunday_short), context.getString(R.string.monday_short),
          context.getString(R.string.tuesday_short), context.getString(R.string.wednesday_short),
          context.getString(R.string.thursday_short), context.getString(R.string.friday_short),
          context.getString(R.string.saturday_short)
      };
    }
    mTextPaint.setColor(0xffffffff);
    mTextPaint.setTextSize(Util.sp2Px(getContext(), 12f));

    int numberOfPoints = DAYS.length;
    for (int i = 0; i < numberOfPoints; i++) {
      mTextPaint.getTextBounds(DAYS[i], 0, DAYS[i].length(), tempTextBound);
      int xPos = mLeftBarWidth + i * mUnitWidth + (mUnitWidth - tempTextBound.width()) / 2;
      int yPos = (mTopBarHeight + dayTextHeight) / 2;
      canvas.drawText(DAYS[i], 0, DAYS[i].length(), xPos, yPos, mTextPaint);
    }
  }

  public boolean canSwitchToSelectionMode() {
    if (dataToDraw == null || dataToDraw.size() == 0) {
      return false;
    }
    for (int i = 0; i < PublicDefine.KEYS.length; i++) {
      ArrayList<DataRect> dataRects = dataToDraw.get(PublicDefine.KEYS[i]);
      if (dataRects != null && dataRects.size() > 0) {
        return true;
      }
    }
    return false;
  }

  public HashMap<String, ArrayList<String>> getDataAfterClear() {
    HashMap<String, ArrayList<String>> result = new HashMap<>(7);
    if (dataToDraw == null || dataToDraw.size() == 0) {
      for (String day : PublicDefine.KEYS) {
        result.put(day, new ArrayList<String>());
      }
    } else {
      for (String day : PublicDefine.KEYS) {
        ArrayList<String> timeElements = new ArrayList<>();
        ArrayList<DataRect> rects = dataToDraw.get(day);
        for (DataRect item : rects) {
          if (!item.isChecked)
            timeElements.add(item.time);
        }
        result.put(day, timeElements);
      }
    }
    return result;
  }

  private class DataRect {
    int column;
    String time;
    RectF rect;
    boolean isChecked;

    DataRect(int column, String timeSpan) {
      this.column = column;
      setTime(timeSpan);
    }

    void setTime(String timeSpan) {
      time = timeSpan;
    }

    public RectF getRect() {
      return rect;
    }

    void calculateRect() {
      if (rect == null) {
        rect = new RectF();
      }

      String[] times = time.split("-");
      if (times.length != 2) {
        Log.i("debug", "MVR schedule input data wrong format: " + time);
      } else if (times[0].length() != 4 && times[1].length() != 4) {
        Log.i("debug", "MVR schedule input data wrong format: " + time);
      } else {
        rect.left = mLeftBarWidth + column * mUnitWidth;
        rect.right = rect.left + mUnitWidth;

        int fromHour = Integer.parseInt(times[0].substring(0, 2));
        int fromMinute = Integer.parseInt(times[0].substring(2));

        rect.top = mTopBarHeight + fromHour * mUnitHeight; // top by hour
        rect.top += (fromMinute * mUnitHeight / 60); // mUnitHeight / 60 -> height by 1 minute

        int toHour = Integer.parseInt(times[1].substring(0, 2));
        int toMinute = Integer.parseInt(times[1].substring(2));

        rect.bottom = mTopBarHeight + toHour * mUnitHeight; // bottom by hour
        rect.bottom += (toMinute * mUnitHeight / 60); // bottom by minute
      }
    }

    boolean checkTouchInside(float x, float y) {
      return rect != null && rect.contains(x, y);
    }
  }

  public interface Listener {

    void onScheduleClicked(String timeSpan, int column);

    void onCreateSchedule(String timeSpan, int column);
  }
}
