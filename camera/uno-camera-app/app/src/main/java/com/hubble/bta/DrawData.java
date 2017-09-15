package com.hubble.bta;

import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;
import com.hubble.ui.BTAActivity;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import base.hubble.database.TimelineEvent;

/**
 * Created by Son Nguyen on 16 Aug 2016.
 */

/**
 * Build bar chart data for BSC events chart
 */
public class DrawData {
  private static final String TAG = "DrawData";
  private static final boolean DEBUG = false;
  private DateTime fromDate, toDate;
  private List<TimelineEvent> timelineEvents, btaEvents;
  private List<BarEntry> barEntryList = new ArrayList<>();
  private List<BarEntry> btaEntryList = new ArrayList<>();
  private List<BarEntry> noBabyEntryList = new ArrayList<>();
  private int average;
  private boolean mBabyDetected = true;

  /**
   * Constructor
   *
   * @param fromDate utc time
   * @param toDate   utc time
   */
  public DrawData(DateTime fromDate, DateTime toDate) {
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.timelineEvents = new ArrayList<>();
    this.btaEvents = new ArrayList<>();
    build();
    calAverage();
  }

  /**
   * Constructor
   *
   * @param fromDate       utc time
   * @param toDate         utc time
   * @param timelineEvents bsc events in that time span
   */
  public DrawData(DateTime fromDate, DateTime toDate, List<TimelineEvent> timelineEvents, List<TimelineEvent> btaEvents) {
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.timelineEvents = timelineEvents;
    this.btaEvents = btaEvents;

    build();
    calAverage();
  }

  public boolean isBabyDetected() {
    return mBabyDetected;
  }

  /**
   * Build List of BarEntry
   */
  private void build() {
    // allocation iMax slot chart data
    final HashMap<String, Integer> xVals = new HashMap<>();
    // why 288 + 2
    // we have bsc average value every 5 minutes, so 1 day is broken into 288 parts
    // hash map key will be some things like this 10:52, 11:02
    // convert utc time to local time
    DateTime fromDateLocalTime = roundTo15(fromDate);

    // build list of max x entries
    int iMax = 294 * 2;


    // build list of x have value
    for (TimelineEvent event : timelineEvents) {
      // dateTime is utc time of events
      DateTime dateTime = new DateTime(event.getTimestamp().getTime());
      // when you to string a Joda DateTime object, it will auto apply to your timezone
      if (DEBUG)
        Log.w(TAG, "event id " + event.getEventId() + ", event utc time " + dateTime.toString(DateTimeFormat.forPattern("MMM-dd HH:mm Z")) + ", value: " + event.getValue());
      //dateTime = dateTime.plusSeconds(Utils.getTimeZoneOffset());
      //Log.w(TAG, "event gmt time " + dateTime.toString(DateTimeFormat.forPattern("MMM-dd HH:mm")) + ", value: " + event.getValue());
      String xValKey = roundToFive(dateTime);
      xVals.put(xValKey, Integer.valueOf(event.getValue()));
      if (DEBUG)
        Log.w(TAG, "Event time when round to five: " + xValKey + ", value: " + event.getValue());
    }
    // fill barEntryList
    barEntryList.clear();
    noBabyEntryList.clear();
    for (int i = 0; i < iMax; i++) {
      DateTime roundedDate = fromDateLocalTime.plusSeconds(i * 150);
      String xValKey = roundToFive(roundedDate);

      if (roundedDate.getMillis() > toDate.getMillis()) {
        BarEntry barEntry = new BarEntry(i, -2.5f, xValKey);
        barEntryList.add(barEntry);
      } else {
        if (xVals.containsKey(xValKey)) {
          if (DEBUG) Log.w(TAG, "At " + xValKey + " bsc value is " + xVals.get(xValKey));
          BarEntry barEntry;
          if (xVals.get(xValKey) == BTAActivity.NO_BABY_HEAD) {
            Log.d(TAG, "Add no baby entry, value: " + xVals.get(xValKey));
            barEntry = new BarEntry(i, 3, xValKey);
            noBabyEntryList.add(barEntry);
            mBabyDetected = false;
          } else {
            mBabyDetected = true;
          }

          Log.d(TAG, "Add BSC entry, value: " + xVals.get(xValKey));
          barEntry = new BarEntry(i, xVals.get(xValKey), xValKey);
          barEntryList.add(barEntry);
        } else {
          BarEntry barEntry = new BarEntry(i, -2.5f, xValKey);
          barEntryList.add(barEntry);
        }
      }
    }


    btaEntryList.clear();
    if (btaEvents != null) {
      for (int i = 0; i < btaEvents.size(); i++) {
        TimelineEvent timelineEvent = btaEvents.get(i);
        DateTime dateTime = new DateTime(timelineEvent.getTimestamp().getTime());
        float index = ((float) (timelineEvent.getTimestamp().getTime() - fromDateLocalTime.getMillis())) / (2.5f * 60 * 1000.0f);
        String xVal = dateTime.toString(DateTimeFormat.forPattern("HH:mm"));
        float yVal = Float.valueOf(timelineEvent.getValue());
        BarEntry barEntry = new BarEntry(index, yVal, xVal);
        if (DEBUG) {
          Log.w(TAG, "btaEntryList: At " + index + " xVal: " + xVal + ", yVal: " + yVal);
        }
        btaEntryList.add(barEntry);

      }
    }
    Collections.sort(btaEntryList, new Comparator<BarEntry>() {
      @Override
      public int compare(BarEntry lhs, BarEntry rhs) {
        /*
         * 20160909: HOANG: should sort in ascending order or the chart will skip entries.
         */
        return (int) ((lhs.getX() - rhs.getX()) * 1000);
      }
    });
    for (BarEntry barEntry : btaEntryList) {
      Log.w(TAG, "BTA entry added: index " + barEntry.getX() + ", xVal = " + barEntry.getData().toString() + ", yVal = " + barEntry.getY());
    }
  }

  public List<BarEntry> getEntries() {
    return barEntryList;
  }

  public List<BarEntry> getBTAEntries() {
    return btaEntryList;
  }

  public List<BarEntry> getNoBabyEntries() {
    return noBabyEntryList;
  }

  /**
   * Round datetime to nearest 2:30 mins
   *
   * @param dateTime
   * @return
   */
  private String roundToFive(DateTime dateTime) { // round to 2:30 minutes
    int hour = dateTime.getHourOfDay();
    int minute = dateTime.getMinuteOfHour();
    int seconds = dateTime.getSecondOfMinute();
    int mSeconds = minute * 60 + seconds;
    if (mSeconds % 150 < 75) {
      minute = (mSeconds - (mSeconds % 150)) / 60; // give minutes to lower limit: ex: 11: 11: 10 ->  11:10:00
      seconds = (mSeconds - (mSeconds % 150)) % 60;
    } else {
      minute = (mSeconds + (150 - mSeconds % 150)) / 60; // give minutes to higher limit: ex: 11: 11: 40 ->  11:12:30
      seconds = (mSeconds + (150 - mSeconds % 150)) % 60;
    }

    if (minute == 57 && seconds > 30 || minute > 57) {

      hour += 1;
      minute = 0;
    }
    hour = hour % 24;
    String str = null;
    if (seconds >= 10) {
      str = (hour >= 10 ? hour : "0" + hour) + ":" + (minute >= 10 ? minute : "0" + minute) + ":" + seconds;
    } else if (seconds > 0 && seconds < 10) {
      str = (hour >= 10 ? hour : "0" + hour) + ":" + (minute >= 10 ? minute : "0" + minute) + ":0" + seconds;
    } else if (seconds == 0) {
      str = (hour >= 10 ? hour : "0" + hour) + ":" + (minute >= 10 ? minute : "0" + minute);
    }
    if (DEBUG)
      Log.i(TAG, "Round " + dateTime.toString(DateTimeFormat.forPattern("HH:mm:ss")) + " to " + str);
    Log.i(TAG, "Second " + seconds);
    Log.i(TAG, "str " + str);
    return str;
  }

  private DateTime roundTo15(DateTime dateTime) {
    int minutesOfHour = dateTime.getMinuteOfHour();
    int diff = 15 - (minutesOfHour % 15);
    if (diff > 7) {
      return dateTime.minusMinutes(minutesOfHour % 15);
    }
    return dateTime.plusMinutes(diff);
  }

  public int getAverage() {
    return average;
  }

  private void calAverage() {
    float total = 0;
    int count = 0;
    for (TimelineEvent timelineEvent : timelineEvents) {
      if (DEBUG)
        Log.i(TAG, "TimelineEvent " + timelineEvent.getValue() + " date: " + timelineEvent.getTimestamp().toGMTString());
      float yVal = Float.valueOf(timelineEvent.getValue());
      if (yVal <= -1f) {
        Log.d(TAG, "invalid event : " + timelineEvent.getAlertName() + ", value: " + timelineEvent.getValue());
      } else {
        count++;
        total += yVal;
      }
    }
    if (count > 0) {
      average = Math.round(total / count);
    }
    if (DEBUG)
      Log.w(TAG, "DrawData calAverage: count? " + count + ", total? " + total + ", average " + average);
  }
}
