package com.hubble.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 * Created by brennan on 15-06-18.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
  private static final String TAG = "DatePickerFragment";
  private boolean mIsDialogOpen = false;
  private DatePickerFragmentInterface mListener;
  private long mCurrentTime,mTimeMonthBack;

  public void setListener(DatePickerFragmentInterface listener) {
    mListener = listener;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Use the current date as the default date in the picker
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);

    // Create a new instance of DatePickerDialog
    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);

    //setting min and max date to display. 30 days in the past is min. Today is max.
    DatePicker datePicker = datePickerDialog.getDatePicker();
    mCurrentTime = calendar.getTimeInMillis();

    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    //to prevent going ahead of time. Unexpected behaviour is possible
    long timeUntilEndOfDay= calendar.getTimeInMillis();

    long anHour = 3600 * 1000;
    long aMonth = anHour * 24 * 29; //30th day is today, isn't?
    mTimeMonthBack = mCurrentTime - aMonth;

    datePicker.setMinDate(mTimeMonthBack);
    datePicker.setMaxDate(timeUntilEndOfDay);

    return datePickerDialog;
  }

  @Override
  public void onDateSet(DatePicker datePicker, int year, int month, int day) {
    if (datePicker.isShown() && mListener != null) {
      //fix for calendar issue in Lollipop where dates outside min and max boundaries are selectable
      Calendar calendar=Calendar.getInstance();
      calendar.set(year, month, day, 0, 0);
      long selectedTime=calendar.getTimeInMillis();
      long oneDay=24*3600*1000;
      Log.d(TAG,"selected Date:"+selectedTime+" month back:"+mTimeMonthBack+" current time:"+mCurrentTime);
      if((mTimeMonthBack<selectedTime||mTimeMonthBack-selectedTime<oneDay) && selectedTime<=mCurrentTime)
        mListener.onDateChosen(year, month, day);
    }
  }

  private long getTimeInMillis(int year, int month, int day) {
    return new GregorianCalendar(year, month, day, 0, 0).getTimeInMillis();
  }

  private long adjustTimeToUTC(int year, int month, int day) {
    GregorianCalendar gregsCalendar = new GregorianCalendar(year, month, day, 0, 0);
    gregsCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
    return gregsCalendar.getTimeInMillis();
  }

  /*
  private long adjustTimeToUTC(int year, int month, int day) {
    DateTime jodaTime = new DateTime(year, month, day, 0, 0); // Crashes if you give it a day value of 31 O_o
    jodaTime = jodaTime.toDateTime(DateTimeZone.UTC);
    return jodaTime.getMillis();
  }
  */

  public void setIsDialogOpen(boolean isOpen) {
    mIsDialogOpen = isOpen;
  }

  public boolean getIsDialogOpen() {
    return mIsDialogOpen;
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    mIsDialogOpen = false;
    super.onDismiss(dialog);
  }

  public interface DatePickerFragmentInterface {
    void onDateChosen(int year, int month, int day);
  }
}