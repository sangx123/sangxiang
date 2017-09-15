package com.nxcomm.blinkhd.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.hubble.HubbleApplication;
import com.hubbleconnected.camera.R;

import java.lang.reflect.Method;

import base.hubble.PublicDefineGlob;

/**
 * Created by BinhNguyen on 11/13/2015.
 */
public class TimePickerDialog extends Dialog {

  private static final int HALF_DAY_AM = 0;
  private static final int HALF_DAY_PM = 1;
  private static final int HALF_DAY_NONE = -1;
  private final Context mContext;

  private NumberPicker npHour, npMinute, npAmPm;
  private TextView txtTitle;
  private CheckBox chkNextDay;

  private Listener mListener;
  private int hour, minute;
  private boolean isEndTime; // show entry 23:59
  private int timeFormat12;
  private int amPm;

  public TimePickerDialog(Context context, Listener listener) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.dialog_time_picker);
    mContext = context;
    mListener = listener;

    timeFormat12 = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0);
    amPm = HALF_DAY_NONE;

    txtTitle = (TextView) findViewById(R.id.txt_set_time);
    chkNextDay = (CheckBox) findViewById(R.id.chk_exceed_next_day);
    npAmPm = (NumberPicker) findViewById(R.id.picker_am_pm);
    npHour = (NumberPicker) findViewById(R.id.picker_hour);
    String[] displayHours;
    if (timeFormat12 == 0) {
      npAmPm.setVisibility(View.VISIBLE);
      displayHours = new String[12];
      for (int i = 0; i < 12; i++) {
        displayHours[i] = String.valueOf(i + 1);
      }
      npHour.setMinValue(1);
      npHour.setMaxValue(12);

      npAmPm.setMinValue(0);
      npAmPm.setMaxValue(1);

      npAmPm.setFormatter(new AMFormatter());
      npAmPm.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

      npAmPm.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
          amPm = newValue;
          if (hour == 11) {
            refreshMinutePicker(hour);
            minute = pickerValue2MinuteValue(npMinute.getValue());
          }
        }
      });
    } else {
      npAmPm.setVisibility(View.GONE);
      amPm = HALF_DAY_NONE;
      displayHours = new String[24];
      for (int i = 0; i < 24; i++) {
        displayHours[i] = String.format("%02d", i);
      }
      npHour.setMinValue(0);
      npHour.setMaxValue(23);
    }

    npHour.setDisplayedValues(displayHours);
    npHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
      @Override
      public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
        if ((oldValue == 11 || newValue == 11) && amPm == HALF_DAY_PM) {
          refreshMinutePicker(newValue);
          minute = pickerValue2MinuteValue(npMinute.getValue());
        } else if (amPm == HALF_DAY_NONE && (oldValue == 23 || newValue == 23)) {
          refreshMinutePicker(newValue);
          minute = pickerValue2MinuteValue(npMinute.getValue());
        }
        hour = newValue;
      }
    });
    // prevent keyboard input
    npHour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

    npMinute = (NumberPicker) findViewById(R.id.picker_minute);
    npMinute.setMinValue(0);
    npMinute.setFormatter(new MinuteFormatter());
    npMinute.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
      @Override
      public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
        minute = pickerValue2MinuteValue(newValue);
      }
    });
    // prevent keyboard input
    npMinute.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    // http://stackoverflow.com/questions/17708325/android-numberpicker-with-formatter-does-not-format-on-first-rendering
    try {
      Method method = npMinute.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
      method.setAccessible(true);
      method.invoke(npMinute, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      Method method = npAmPm.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
      method.setAccessible(true);
      method.invoke(npAmPm, true);
    } catch (Exception e) {
      e.printStackTrace();
    }

    findViewById(R.id.connect_btn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mListener != null) {
          if (timeFormat12 == 0) {
            int tempHour = hour;
            if (amPm == HALF_DAY_AM && hour == 12) {
              tempHour = 0;
            } else if (amPm == HALF_DAY_PM && hour != 12) {
              tempHour += 12;
            }
            mListener.onTimeSet(tempHour, minute, chkNextDay.isChecked());
          } else {
            mListener.onTimeSet(hour, minute, chkNextDay.isChecked());
          }
        }
        dismiss();
      }
    });
    findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();
      }
    });
  }

  /**
   * Set #setShow2359 before #setTime to make it affects
   */
  public void setIsEndTime(boolean value) {
    isEndTime = value;
    if (txtTitle != null) {
      int titleRes = isEndTime ? R.string.set_end_time : R.string.set_start_time;
      txtTitle.setText(titleRes);
    }
    if (chkNextDay != null) {
      int visibility = isEndTime ? View.VISIBLE : View.GONE;
      chkNextDay.setVisibility(visibility);
    }
  }

  private void refreshMinutePicker(int hour) {
    if (timeFormat12 == 0) {
      if (hour == 11 && amPm == HALF_DAY_PM && isEndTime) {
        npMinute.setMaxValue(2);
      } else {
        npMinute.setMaxValue(1);
      }
    } else {
      if (hour != 23 || !isEndTime) {
        npMinute.setMaxValue(1);
      } else {
        npMinute.setMaxValue(2);
      }
    }

  }

  private int pickerValue2MinuteValue(int pickerValue) {
    switch (pickerValue) {
      case 0:
        return 0;
      case 1:
        return 30;
      default:
        return 59;
    }
  }

  public void setIsExceedNextDay(boolean isExceedNextDay) {
    if (chkNextDay != null) {
      chkNextDay.setChecked(isExceedNextDay);
    }
  }

  private class MinuteFormatter implements NumberPicker.Formatter {
    @Override
    public String format(int i) {
      switch (i) {
        case 0:
          return "00";
        case 1:
          return "30";
        default:
          return "59";
      }
    }
  }

  private class AMFormatter implements NumberPicker.Formatter {
    @Override
    public String format(int i) {
      String stringFormat="";
      switch (i) {
        case 0:
          stringFormat = getSafeString(R.string.half_day_am);
          break;
        case 1:
          stringFormat = getSafeString(R.string.half_day_pm);
          break;
      }
      return stringFormat;
    }
  }

  public void setTime(int hour, int minute) {
    this.hour = hour;
    this.minute = minute;

    if (timeFormat12 == 1) {
      amPm = HALF_DAY_NONE;
    } else {
      amPm = hour < 12 ? HALF_DAY_AM : HALF_DAY_PM;

      if (this.hour == 0) {
        this.hour = 12;
      }
      if (this.hour > 12) {
        this.hour -= 12;
      }
    }

    npHour.setValue(this.hour);
    if (amPm != HALF_DAY_NONE) {
      npAmPm.setValue(amPm);
    }

    refreshMinutePicker(this.hour);
    npMinute.setValue(minute);
  }

  private String getSafeString(int stringRes) {
    return mContext != null ? mContext.getString(stringRes) : "";
  }

  public interface Listener {
    void onTimeSet(int hour, int minute, boolean exceedNextDay);
  }

}
