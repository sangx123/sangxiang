package com.hubble.bta;

import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;

import org.joda.time.DateTime;

import java.util.List;

import base.hubble.database.TimelineEvent;

/**
 * Created by songn_000 on 16 Aug 2016.
 */
public class DailyChartData {
  private static final long FIVE_MINUTES = 5 * 60 * 1000;
  private static final String TAG = DailyChartData.class.getSimpleName();
  private List<TimelineEvent> timelineEventList;
  private List<BarEntry> barEntries;

  public DailyChartData(List<TimelineEvent> timelineEvents) {
    this.timelineEventList = timelineEvents;
  }

  private void calc() {
    DateTime dateTime = DateTime.now();
    DateTime startOfDay = dateTime.withTimeAtStartOfDay();
    long timeStamp = dateTime.getMillis() - startOfDay.getMillis();
    long timesOfFiveMinutes = timeStamp / FIVE_MINUTES;

    Log.i(TAG, "Number of expectation entries: " + timesOfFiveMinutes);
    Log.i(TAG, "Number of real entries: " + timelineEventList.size());
  }

  private void doMapping() {

  }
}
