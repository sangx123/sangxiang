package com.hubble.util;

import android.text.TextUtils;
import android.util.Log;

import com.hubble.HubbleApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by hoang on 11/20/15.
 */
public class DateTimeUtils {
  private static final String TAG = HubbleApplication.TAG;

  /**
   * Parse a UTC time string, return a date correspond to input timestamp.
   * @param utcTime UTC date time string.
   * @param dateFormat Date format that app want to parse.
   * @return UTC timestamp.
   */
  public static Date parseUtcTimestamp(String utcTime, String dateFormat) {
    Date date = null;
    if (!TextUtils.isEmpty(utcTime) && !TextUtils.isEmpty(dateFormat)) {
      SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.US);
      format.setTimeZone(TimeZone.getTimeZone("UTC"));
      if (format != null) {
        try {
          date = format.parse(utcTime);
        } catch (ParseException e) {
          e.printStackTrace();
        }
      } else {
        Log.i(TAG, "parseUtcTimestamp wrong date format? " + dateFormat);
      }
    } else {
      Log.i(TAG, "parseUtcTimestamp invalid input: utcTime: " + utcTime + ", dateFormat: " + dateFormat);
    }
    return date;
  }
}
