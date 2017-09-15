package com.hubble.bta;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.TimeZone;

/**
 * Created by songn_000 on 29 Aug 2016.
 */
public class Utils {
  public static boolean isToday(DateTime dateTime) {
    DateTime now = DateTime.now();
    return (now.getYear() == dateTime.getYear() && now.getMonthOfYear() == dateTime.getMonthOfYear() && now.getDayOfMonth() == dateTime.getDayOfMonth());
  }

  /**
   * convert to UTC  time, day light time saving applying too
   *
   * @param dateTime date time
   * @return UTC with day light time saving
   */
  public static DateTime toUTCTime(DateTime dateTime) {
    return dateTime.minusSeconds(getTimeZoneOffset());
  }

  /**
   * Return timezone offset by seconds
   *
   * @return time zone offset by seconds
   */
  public static int getTimeZoneOffset() {
    TimeZone tz = TimeZone.getDefault();
    Date now = new Date();
    int offsetFromUtc = tz.getOffset(now.getTime()) / 1000;
    return offsetFromUtc;
  }
}
