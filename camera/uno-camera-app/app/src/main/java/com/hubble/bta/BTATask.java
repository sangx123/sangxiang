package com.hubble.bta;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.util.CommonUtil;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.database.AverageData;
import base.hubble.database.GeneralData;
import base.hubble.database.TimelineEvent;

/**
 * Author: Son Nguyen
 * Date: 1:38 PM 04 Aug, 2016
 */
public class BTATask {
  private static final String TAG = "BTATask";
  private static final boolean DEBUG = false;
  private final ListeningExecutorService executorService, executorService2;
  private final ListeningExecutorService startStopBTAExecutorService;
  private final ListeningExecutorService syncExecutorService;
  private Device device;
  private String apiKey;
    private Context mContext;
    private BTAInterface mInterface;


    public interface BTAInterface {
        void onCompleted(String command, Pair<String, Object> result, Exception e);
    }
  /**
   * BTATask Class: perform BTA task
   *
   * @param apiKey user access token
   * @param device device
   */
  public BTATask(String apiKey, Device device, Context mContext, BTAInterface mInterface) {
    executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    executorService2 = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    startStopBTAExecutorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    syncExecutorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    this.device = device;
    this.apiKey = apiKey;
      this.mContext = mContext;
      this.mInterface = mInterface;

      if(device != null && device.getProfile() != null) {
          if (!device.getProfile().getRegistrationId().startsWith("010877")) {
              throw new RuntimeException("BTATask only support camera model 0877");
          }
      }else{
          return;
      }
  }

  /**
   * Get current device time zone under string format -XX:XX or +XX:XX
   *
   * @return time zone string -XX:XX or +XX:XX
   */
  public static String getTimeZoneStr() {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
        Locale.getDefault());
    Date currentLocalTime = calendar.getTime();
    SimpleDateFormat sdf = new SimpleDateFormat("ZZZZZ");
    return sdf.format(currentLocalTime);
  }

  /**
   * Start Baby Sleepy Care
   *
   * @return ListenableFuture<Pair<String, Object>>
   */

  public void startBSCMode(final com.koushikdutta.async.future.FutureCallback<Pair<String, Object>> callback) {
     /* Futures.addCallback(sendCommandToDevice("start_vda&value=bsc", startStopBTAExecutorService))
      new FutureCallback<Pair<String, Object>>() {
          @Override
          public void onSuccess(Pair<String, Object> result) {
            Log.i(TAG, "<-- Start bsc result" + result.first+ ": " + result.second);
            callback.onCompleted(null, result);
          }

          @Override
          public void onFailure(Throwable t) {
            callback.onCompleted(new Exception(t), null);
          }
        });*/
     // sendCommandToDevice("start_vda&value=bsc",
  }

    public void startBSCMode() {

        sendCommandToDevice("start_vda&value=bsc");
    }

  /**
   * Stop BSC mode
   *
   * @return promise
   */

  //ARUNA
  /*public ListenableFuture<Pair<String, Object>> stopBSCMode() {
    return sendCommandToDevice("stop_vda");
  }
*/
    public void stopBSCMode(){
        sendCommandToDevice("stop_vda");
    }
  /**
   * Start Bed Time Analytics (BTA)
   * action=command&command=set_bsc_bed_time&start_time=NA&duration=3600"
   *
   * @param duration in seconds
   * @return promise
   */

  //ARUNA
 /* public void startBTA(final int duration, final com.koushikdutta.async.future.FutureCallback<Pair<String, Object>> callback) {
    Futures.addCallback(sendCommandToDevice("set_bsc_bed_time&start_time=NA&duration=" + duration), new FutureCallback<Pair<String, Object>>() {
      @Override
      public void onSuccess(Pair<String, Object> result) {
        callback.onCompleted(null, result);
      }

      @Override
      public void onFailure(Throwable t) {
        callback.onCompleted(new Exception(t), null);
      }
    });
  }*/

    public void startBTA(final int duration){
        sendCommandToDevice("set_bsc_bed_time&start_time=NA&duration=" + duration);
    }

  /**
   * Stop BTA
   * To stop BTA we send start with duration = 0
   *
   * @return promise
   */
  //ARUNA
 /* public ListenableFuture<Pair<String, Object>> stopBTA() {
      //ARUNA
    //return sendCommandToDevice("set_bsc_bed_time&start_time=NA&duration=0", startStopBTAExecutorService);
  }*/

    public void stopBTA() {
        sendCommandToDevice("set_bsc_bed_time&start_time=NA&duration=0");
    }
  /**
   * Send command to current device
   *
   * @param command a string command. Eg start_vda&value=bsc
   * @return ListenableFuture<Pair<String, Object>>
   */
  android.util.Pair<String, Object> response = null;
  public void sendCommandToDevice(final String command) {
    //Callable<Pair<String, Object>> callable = new Callable<Pair<String, Object>>() {
      //@Override
     // public android.util.Pair<String, Object> call() throws Exception {
       // return device.sendCommandGetValue(command, null, null);
          DeviceManager mDeviceManager;

          mDeviceManager = DeviceManager.getInstance(mContext);
          SecureConfig settings = HubbleApplication.AppConfig;
          String regId = device.getProfile().getRegistrationId();
          SendCommand getAdaptiveQuality = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, command);

          mDeviceManager.sendCommandRequest(getAdaptiveQuality, new Response.Listener<SendCommandDetails>() {

                      @Override
                      public void onResponse(SendCommandDetails response1) {
                          String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                          Log.i(TAG, "SERVER RESP : " + responsebody);

                              try {
                                   response = CommonUtil.parseResponseBody(responsebody);
                                   mInterface.onCompleted(command, response, null);
                              } catch (Exception ex) {
                                  mInterface.onCompleted(command, response, ex);
                              }
                         // }
                      }
                  }, new Response.ErrorListener()
                  {
                      @Override
                      public void onErrorResponse(VolleyError error)
                      {

                          if(error != null && error.networkResponse != null)
                          {
                              Log.d(TAG,error.networkResponse.toString());
                              Log.d(TAG,error.networkResponse.data.toString());

                          }

                      }
                  }



          );
         // return response;
     // }
   // };
    //return listeningExecutorService.submit(callable);
  }

  /**
   * Get remain BTA time
   *
   * @return remain BTA time in seconds
   */
  //ARUNA
  /*public ListenableFuture<Pair<String, Object>> getRemainBTATime() {
    return sendCommandToDevice("get_bsc_remain_duration");
  }*/

    public void getRemainBTATime() {
        sendCommandToDevice("get_bsc_remain_duration");
    }

  /**
   * Get aggregate data of current camera
   * aggregate average value for a specific periods of time
   * currently server only support 24 hours only
   *
   * @return promise
   */
  public ListenableFuture<List<Models.Average>> getAggregate() {
    Callable<List<Models.Average>> callable = new Callable<List<Models.Average>>() {
      @Override
      public List<Models.Average> call() throws Exception {
        DateTime currentDate = DateTime.now().plusDays(1);
        DateTime thirtyDaysAgo = currentDate.minusDays(31);
        String currentDateString = currentDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));
        String thirtyDaysAgoString = thirtyDaysAgo.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));

        Models.AggregateRequest aggregateRequest = new Models.AggregateRequest();
        aggregateRequest.setRegistration_id(device.getProfile().getRegistrationId())
            .setFrom_date(thirtyDaysAgoString)
            .setTo_date(currentDateString)
            .setAggregate_function("average")
            .setTime_zone(getTimeZoneStr())
            .setAggregate_period(24)
            .setAlert(66);

        Models.ApiResponse<List<Models.Average>> request = Api.getInstance().getService().aggregateEvents(apiKey, aggregateRequest);
        if (request.isSucceeded()) {
          return request.getData();
        } else {
          throw new Exception(request.getMessage());
        }
      }
    };
    return executorService.submit(callable);
  }

  /**
   * Query bsc events
   * if data has cached before return it
   * if not query them on server
   *
   * @param fromDate after_start_time parameter, utc time
   * @param toDate   before_start_time parameter, utc time
   * @return List of TimelineEvent
   */
  public ListenableFuture<List<TimelineEvent>> queryBSCEvents(final DateTime fromDate, final DateTime toDate) {
    Callable<List<TimelineEvent>> callable = new Callable<List<TimelineEvent>>() {
      @Override
      public List<TimelineEvent> call() throws Exception {

        String fromDateString = fromDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"));
        String toDateString = toDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"));
        if (isSynced(toDate, SyncInfo.EVENT_TYPE_BSC)) {
          return queryOfflineBSCEventsForPeriod(fromDate, toDate).get();
        } else {
          Log.i(TAG, "Load bsc event from " + fromDateString + " to date " + toDateString);
          Models.ApiResponse<Models.TimelineEventList> request = Api.getInstance().getService().getBSCEvents(device.getProfile().getRegistrationId(), apiKey, fromDateString, toDateString);
          if (request.isSucceeded()) {
            return request.getData().getEvents();
          } else {
            throw new Exception(request.getMessage());
          }
        }
      }
    };
    return syncExecutorService.submit(callable);
  }

  /**
   * Query bsc events (blocking)
   * if data has cached before return it
   * if not query them on server
   *
   * @param fromDate after_start_time parameter, utc time
   * @param toDate   before_start_time parameter, utc time
   * @return List of TimelineEvent
   */
  public List<TimelineEvent> queryBSCEventsBlocked(final DateTime fromDate, final DateTime toDate) {
    List<TimelineEvent> timelineEvents = null;
    String fromDateString = fromDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"));
    String toDateString = toDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"));
    if (isSynced(toDate, SyncInfo.EVENT_TYPE_BSC)) {
      timelineEvents = queryOfflineBSCEventsForPeriodBlocked(fromDate, toDate);
    } else {
      Log.i(TAG, "Load bsc event from " + fromDateString + " to date " + toDateString);
      try {
        Models.ApiResponse<Models.TimelineEventList> request = Api.getInstance().getService().getBSCEvents(device.getProfile().getRegistrationId(), apiKey, fromDateString, toDateString);
        if (request.isSucceeded()) {
          timelineEvents = request.getData().getEvents();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return timelineEvents;
  }

  /**
   * Query bsc events online (blocking)
   * if data has cached before return it
   * if not query them on server
   *
   * @param fromDate after_start_time parameter, utc time
   * @param toDate   before_start_time parameter, utc time
   * @return List of TimelineEvent
   */
  public List<TimelineEvent> queryBSCEventsOnlineBlocked(final DateTime fromDate, final DateTime toDate) {
    List<TimelineEvent> timelineEvents = null;
    String fromDateString = fromDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"));
    String toDateString = toDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"));
    Log.i(TAG, "Load bsc event from " + fromDateString + " to date " + toDateString);
    try {
      Models.ApiResponse<Models.TimelineEventList> request = Api.getInstance().getService().getBSCEvents(device.getProfile().getRegistrationId(), apiKey, fromDateString, toDateString);
      if (request.isSucceeded()) {
        timelineEvents = request.getData().getEvents();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return timelineEvents;
  }

  /**
   * query cached bsc events for the periods, return null when did not cache before
   *
   * @param fromDate start date
   * @param toDate   end date
   * @return null or List<TimelineEvent>
   */
  public ListenableFuture<List<TimelineEvent>> queryOfflineBSCEventsForPeriod(final DateTime fromDate, final DateTime toDate) {
    if (DEBUG) Log.i(TAG, "OFFLINE - query offline bsc events");
    if (DEBUG) {
      DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MMM-dd HH:mm Z");
      Log.w(TAG, "OFFLINE - bsc from date: " + fromDate.toString(formatter));
      Log.w(TAG, "OFFLINE - bsc to date: " + toDate.toString(formatter));
    }
    Callable<List<TimelineEvent>> callable = new Callable<List<TimelineEvent>>() {
      @Override
      public List<TimelineEvent> call() throws Exception {
        // query from cache
        List<TimelineEvent> timelineEvents = new Select()
            .from(TimelineEvent.class)
            .where("device_registration_id =  ?", device.getProfile().getRegistrationId())
            .where("time_stamp > ?", fromDate.getMillis())
            .where("time_stamp < ?", toDate.getMillis())
            .where("alert = ?", 66)
            .execute();
        return timelineEvents;
      }
    };
    return executorService.submit(callable);
  }

  /**
   * query cached bsc events for the periods, return null when did not cache before
   *
   * @param fromDate start date
   * @param toDate   end date
   * @return null or List<TimelineEvent>
   */
  public List<TimelineEvent> queryOfflineBSCEventsForPeriodBlocked(final DateTime fromDate, final DateTime toDate) {
    List<TimelineEvent> timelineEvents = null;
    if (DEBUG) Log.i(TAG, "OFFLINE - query offline bsc events");
    if (DEBUG) {
      DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MMM-dd HH:mm Z");
      Log.w(TAG, "OFFLINE - bsc from date: " + fromDate.toString(formatter));
      Log.w(TAG, "OFFLINE - bsc to date: " + toDate.toString(formatter));
    }

    // query from cache
    timelineEvents = new Select()
            .from(TimelineEvent.class)
            .where("device_registration_id =  ?", device.getProfile().getRegistrationId())
            .where("time_stamp > ?", fromDate.getMillis())
            .where("time_stamp < ?", toDate.getMillis())
            .where("alert = ?", 66)
            .execute();
    return timelineEvents;
  }

  /**
   * only online result
   * @return
   */
  public ListenableFuture<List<TimelineEvent>> queryLast24HoursBSCEvents() {
    DateTime now = DateTime.now();
    final DateTime fromDate = Utils.toUTCTime(now.minusDays(1));
    final DateTime toDate = Utils.toUTCTime(now);
    Callable<List<TimelineEvent>> callable = new Callable<List<TimelineEvent>>() {
      @Override
      public List<TimelineEvent> call() throws Exception {
        String fromDateString = fromDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"));
        String toDateString = toDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss"));
        Log.i(TAG, "Load bsc event from " + fromDateString + " to date " + toDateString);
        Models.ApiResponse<Models.TimelineEventList> request = Api.getInstance().getService().getBSCEvents(device.getProfile().getRegistrationId(), apiKey, fromDateString, toDateString);
        if (request.isSucceeded()) {
          return request.getData().getEvents();
        } else {
          throw new Exception(request.getMessage());
        }
      }
    };
    return syncExecutorService.submit(callable);
  }

  public void syncBSCEvent() {
    Log.d(TAG, "Start sync BSC event...");
    DateTime dateTime = DateTime.now();
    for (int i = 0; i < 30; i++) {
      DateTime tempDate = dateTime.minusDays(i);
      if (isSynced(tempDate, SyncInfo.EVENT_TYPE_BSC) == false) {
        Log.i(TAG, "sync bsc event for day " + tempDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd")));
        syncBSCEventForDate(tempDate);
      } else {
        Log.i(TAG, "bsc event is cached for day " + tempDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd")));
      }
    }
  }

  public void syncBSCEventsToday() {
    Log.d(TAG, "Start sync BSC event today...");
    DateTime dateTime = DateTime.now();
    Log.i(TAG, "sync bsc event for day " + dateTime.toString(DateTimeFormat.forPattern("YYYY-MM-dd")));
    // Force sync online, skip cache
    syncBSCEventForDateOnlineBlocked(dateTime);
  }

  public void syncBscEventsThisWeekExceptToday(final DateTime today) {
    Log.d(TAG, "Start sync BSC events this week except today...");
    for (int i=1; i<7; i++) {
      DateTime tempDate = today.minusDays(i);
      Log.i(TAG, "sync bsc event for day " + tempDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd")));
      // Force sync online, skip cache
      syncBSCEventForDateOnlineBlocked(tempDate);
    }
  }

  public void syncBscEventsThisMonthExceptThisWeek(final DateTime today) {
    Log.d(TAG, "Start sync BSC events this month except this week...");
    for (int i=7; i<30; i++) {
      DateTime tempDate = today.minusDays(i);
      if (isSynced(tempDate, SyncInfo.EVENT_TYPE_BSC) == false) {
        Log.i(TAG, "sync bsc event for day " + tempDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd")));
        syncBSCEventForDateBlocked(tempDate);
      } else {
        Log.i(TAG, "bsc event is cached for day " + tempDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd")));
      }
    }
  }

  public int getAverageBSCLevelForDay(DateTime dateTime) {
    // get from database first
    String day = dateTime.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));
    AverageData cachedAverageData = new Select().from(AverageData.class)
        .where("device_registration_id = ?", device.getProfile().getRegistrationId())
        .where("date = ?", day)
        .executeSingle();
    int average = 0;
    if (cachedAverageData != null) {
      average = (int) cachedAverageData.getValue();
    }
    return average;
  }

  /**
   * Query all bsc events from server for camera
   * save all bsc events and average bsc for that day
   *
   * @param dateTime the time in that date (local time), it will convert to utc time later
   */
  private void syncBSCEventForDate(final DateTime dateTime) {
    final String day = dateTime.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));

    DateTime fromDate = Utils.toUTCTime(dateTime.withTime(0, 0, 0, 1)); // start of day
    DateTime toDate = Utils.toUTCTime(dateTime.withTime(23, 59, 59, 999)); // end of day

    ListenableFuture<List<TimelineEvent>> listenableFuture = queryBSCEvents(fromDate, toDate);
    Futures.addCallback(listenableFuture, new FutureCallback<List<TimelineEvent>>() {
      @Override
      public void onSuccess(List<TimelineEvent> result) {
        Log.w(TAG, "sync bsc events for day: " + day + " succeeded");
        saveBSCEventsToDatabase(result, dateTime);
      }

      @Override
      public void onFailure(Throwable t) {
        Log.e(TAG, "sync bsc event for day " + day + " error");
        t.printStackTrace();
      }
    });
  }

  /**
   * Query all bsc events from server for camera
   * save all bsc events and average bsc for that day
   *
   * @param dateTime the time in that date (local time), it will convert to utc time later
   */
  private void syncBSCEventForDateBlocked(final DateTime dateTime) {
    final String day = dateTime.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));

    DateTime fromDate = Utils.toUTCTime(dateTime.withTime(0, 0, 0, 1)); // start of day
    DateTime toDate = Utils.toUTCTime(dateTime.withTime(23, 59, 59, 999)); // end of day

    List<TimelineEvent> timelineEvents = queryBSCEventsBlocked(fromDate, toDate);
    if (timelineEvents != null) {
      Log.w(TAG, "sync bsc events for day: " + day + " succeeded");
      saveBSCEventsToDatabase(timelineEvents, dateTime);
    }
  }

  /**
   * Query all bsc events from server for camera
   * save all bsc events and average bsc for that day
   *
   * @param dateTime the time in that date (local time), it will convert to utc time later
   */
  private void syncBSCEventForDateOnlineBlocked(final DateTime dateTime) {
    final String day = dateTime.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));

    DateTime fromDate = Utils.toUTCTime(dateTime.withTime(0, 0, 0, 1)); // start of day
    DateTime toDate = Utils.toUTCTime(dateTime.withTime(23, 59, 59, 999)); // end of day

    List<TimelineEvent> timelineEvents = queryBSCEventsOnlineBlocked(fromDate, toDate);
    if (timelineEvents != null) {
      Log.w(TAG, "sync bsc events for day: " + day + " succeeded");
      saveBSCEventsToDatabase(timelineEvents, dateTime);
    }
  }

  /**
   * Save timeline event list to database
   *
   * @param bscEvents timeline event list
   * @param dateTime  date string YYYY-MM-dd
   */
  private void saveBSCEventsToDatabase(List<TimelineEvent> bscEvents, DateTime dateTime) {
    String date = dateTime.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));
    Log.w(TAG, "bsc events count for day " + date + " is " + bscEvents.size());
    try {
      ActiveAndroid.beginTransaction();
      // save it into database
      int count = 0;
      int total = 0;

      for (TimelineEvent timelineEvent : bscEvents) {
        timelineEvent.save();
        count++;
        int temp = Integer.valueOf(timelineEvent.getValue());
        if (temp >= 0) {
          total += temp;
        }
        if (timelineEvent.getData() != null) {
          for (GeneralData generalData : timelineEvent.getCachedData()) {
            generalData.setTimeline_event_id(timelineEvent.getEventId());
            generalData.save();
          }
        }
      }
      ActiveAndroid.setTransactionSuccessful();

      int average = 0;
      if (count > 0) {
        average = total / count;
      }

      // save average value to database
      AverageData averageData = new AverageData();
      averageData.setDeviceRegId(device.getProfile().getRegistrationId());
      averageData.setValue(average);
      averageData.setDate(date);
      averageData.save();

      // save sync info to database
      markSynced(dateTime, SyncInfo.EVENT_TYPE_BSC);

      Log.w(TAG, "Cached bsc events for day " + date + " ok. Average value is " + average);
    } catch (Exception e) {
      Log.e(TAG, "sync bsc event for day " + date + " error");
      e.printStackTrace();
    } finally {
      try {
        ActiveAndroid.endTransaction();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public ListenableFuture<List<TimelineEvent>> queryLast24HoursBTAEvents(boolean fromCache) {
    DateTime toDate = DateTime.now();
    DateTime fromDate = toDate.minusDays(1);
    if (fromCache) {
      return queryOfflineBTAEventsForPeriod(fromDate, toDate);
    } else {
      return queryOnlineBTAEventsForPeriod(fromDate, toDate);
    }
  }

  public ListenableFuture<List<TimelineEvent>> queryTop3BTAEventsForPeriod(DateTime fromDate, DateTime toDate) {
    boolean isSynced = isSynced(toDate, SyncInfo.EVENT_TYPE_BTA);
    if (isSynced) {
      return queryOfflineBTAEventsForPeriod(fromDate, toDate);
    } else {
      return queryOnlineBTAEventsForPeriod(fromDate, toDate);
    }
  }

  /**
   * select top 3 BTA events
   *
   * @param fromDate from date
   * @param toDate   to date
   * @return 3 top bta events
   */
  public ListenableFuture<List<TimelineEvent>> queryOfflineBTAEventsForPeriod(final DateTime fromDate, final DateTime toDate) {
    if (DEBUG) Log.i(TAG, "OFFLINE - query bta 3 tops activities BTA events from cache");
    if (DEBUG) {
      DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MMM-dd HH:mm Z");
      Log.w(TAG, "OFFLINE - bta from date: " + fromDate.toString(formatter));
      Log.w(TAG, "OFFLINE - bta to date: " + toDate.toString(formatter));
    }
    Callable<List<TimelineEvent>> callable = new Callable<List<TimelineEvent>>() {
      @Override
      public List<TimelineEvent> call() throws Exception {
        // query from cache
        List<TimelineEvent> top3BTAActivities = new Select()
            .from(TimelineEvent.class)
            .where("device_registration_id =  ?", device.getProfile().getRegistrationId())
            .where("time_stamp > ?", fromDate.getMillis())
            .where("time_stamp < ?", toDate.getMillis())
            .where("alert = ?", 36)
            .orderBy("value DESC")
            .limit(3)
            .execute();
        return top3BTAActivities;
      }
    };
    return executorService.submit(callable);
  }

  /**
   * select top 3 BTA events
   *
   * @param fromDate from date
   * @param toDate   to date
   * @return 3 top bta events
   */
  public List<TimelineEvent> queryOfflineBTAEventsForPeriodBlocked(final DateTime fromDate, final DateTime toDate) {
    if (DEBUG) Log.i(TAG, "OFFLINE - query bta 3 tops activities BTA events from cache");
    if (DEBUG) {
      DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MMM-dd HH:mm Z");
      Log.w(TAG, "OFFLINE - bta from date: " + fromDate.toString(formatter));
      Log.w(TAG, "OFFLINE - bta to date: " + toDate.toString(formatter));
    }
    // query from cache
    List<TimelineEvent> top3BTAActivities = new Select()
            .from(TimelineEvent.class)
            .where("device_registration_id =  ?", device.getProfile().getRegistrationId())
            .where("time_stamp > ?", fromDate.getMillis())
            .where("time_stamp < ?", toDate.getMillis())
            .where("alert = ?", 36)
            .orderBy("value DESC")
            .limit(3)
            .execute();
    return top3BTAActivities;
  }

  /**
   * query BTA events from fromDate to toDate on server
   * this method will do extra step that cache query result
   *
   * @param fromDate from date utc time
   * @param toDate   to date utc time
   * @return ListenableFuture<Models.ApiResponse<Models.TimelineEventList>>
   */
  public ListenableFuture<List<TimelineEvent>> queryOnlineBTAEventsForPeriod(final DateTime fromDate, final DateTime toDate) {
    DateTimeFormatter dtf = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();
    // when query online event, we need to adjust date time string text with time zone
    if (DEBUG) {
      DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MMM-dd HH:mm Z").withZoneUTC();
      Log.w(TAG, "ONLINE - load BTA events in period");
      Log.w(TAG, "ONLINE - from date: " + fromDate.toString(formatter));
      Log.w(TAG, "ONLINE - to date: " + toDate.toString(formatter));
    }
    final String fromDateStr = fromDate.toString(dtf);
    final String toDateStr = toDate.toString(dtf);
    // it is not effective to filter top 3 activities on client side due
    // client need to fetch all events in a periods
    // and it may be so much events
    // TODO: ask server support for query 3 top activities only
    Callable<List<TimelineEvent>> callable = new Callable<List<TimelineEvent>>() {
      @Override
      public List<TimelineEvent> call() throws Exception {
        Models.ApiResponse<Models.TimelineEventList> request = Api.getInstance().getService().getBedTimeEvents(device.getProfile().getRegistrationId(), apiKey, fromDateStr, toDateStr, true);
        if (request.isSucceeded()) {
          saveTimelineEvents(request.getData(), toDate, SyncInfo.EVENT_TYPE_BTA);
          markSynced(toDate, SyncInfo.EVENT_TYPE_BTA);
          List<TimelineEvent> timelineEvents = queryOfflineBTAEventsForPeriod(fromDate, toDate).get();
          return timelineEvents;
        } else {
          throw new Exception(request.getMessage());
        }
      }
    };
    return executorService2.submit(callable);
  }

  /**
   * query BTA events from fromDate to toDate on server
   * this method will do extra step that cache query result
   *
   * @param fromDate from date utc time
   * @param toDate   to date utc time
   * @return ListenableFuture<Models.ApiResponse<Models.TimelineEventList>>
   */
  public List<TimelineEvent> queryOnlineBTAEventsForPeriodBlocked(final DateTime fromDate, final DateTime toDate) {
    List<TimelineEvent> timelineEvents = new ArrayList<>();
    DateTimeFormatter dtf = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss").withZoneUTC();
    // when query online event, we need to adjust date time string text with time zone
    if (DEBUG) {
      DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MMM-dd HH:mm Z").withZoneUTC();
      Log.w(TAG, "ONLINE - load BTA events in period");
      Log.w(TAG, "ONLINE - from date: " + fromDate.toString(formatter));
      Log.w(TAG, "ONLINE - to date: " + toDate.toString(formatter));
    }
    final String fromDateStr = fromDate.toString(dtf);
    final String toDateStr = toDate.toString(dtf);
    // it is not effective to filter top 3 activities on client side due
    // client need to fetch all events in a periods
    // and it may be so much events
    // TODO: ask server support for query 3 top activities only
    try {
      Models.ApiResponse<Models.TimelineEventList> request = Api.getInstance().getService().getBedTimeEvents(device.getProfile().getRegistrationId(), apiKey, fromDateStr, toDateStr, true);
      if (request.isSucceeded()) {
        saveTimelineEvents(request.getData(), toDate, SyncInfo.EVENT_TYPE_BTA);
        markSynced(toDate, SyncInfo.EVENT_TYPE_BTA);
        timelineEvents = queryOfflineBTAEventsForPeriod(fromDate, toDate).get();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return timelineEvents;
  }

  /**
   * check if timeline events for the date is synced or not
   *
   * @param toDate    to date
   * @param eventType event type "bta|bsc"
   * @return synced or not
   */
  private boolean isSynced(DateTime toDate, String eventType) {
    boolean synced;
    String key = SyncInfo.buildUniqueKey(device.getProfile().getRegistrationId(),
        toDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd")),
        eventType);
    SyncInfo syncInfo = new Select()
        .from(SyncInfo.class)
        .where("unique_key = ?", key)
        .executeSingle();

    if (syncInfo != null) {
      synced = true;
    } else {
      synced = false;
    }
    if (synced) Log.w(TAG, eventType + " for " + toDate.toString() + " is synced before");
    return synced;
  }

  /**
   * Save timeline event list to database
   *
   * @param timelineEventList timeline event list
   * @param toDate            date time YYYY-MM-dd
   */
  private void saveTimelineEvents(Models.TimelineEventList timelineEventList, DateTime toDate, String eventType) {
    String date = toDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));
    if (timelineEventList.getEvents() == null) {
      Log.e(TAG, "SAVE - " + eventType + " event for date " + date + " is null");
    } else {
      Log.w(TAG, "SAVE - " + eventType + " events count for day " + date + " is " + timelineEventList.getEvents().size());
      try {

        ActiveAndroid.beginTransaction();
        List<TimelineEvent> bscEvents = timelineEventList.getEvents();
        for (TimelineEvent timelineEvent : bscEvents) {
          timelineEvent.save();
          if (timelineEvent.getData() != null) {
            for (GeneralData generalData : timelineEvent.getData()) {
              generalData.setTimeline_event_id(timelineEvent.getEventId());
              generalData.save();
            }
          }
        }
        ActiveAndroid.setTransactionSuccessful();

        Log.w(TAG, "SAVE - save " + eventType + " events for day " + date + " succeeded");
      } catch (Exception e) {
        Log.e(TAG, "SAVE - save " + eventType + " event for day " + date + " error");
        e.printStackTrace();
      } finally {
        ActiveAndroid.endTransaction();
      }
    }
  }

  /**
   * mark timeline events is synced for the date
   *
   * @param toDate    date string YYYY-MM-dd
   * @param eventType event type "bsc|bta"
   */
  private void markSynced(DateTime toDate, String eventType) {
    String date = toDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));
    if (DEBUG)
      Log.w(TAG, "SYNCED: mark timeline events type " + eventType + " is synced for date: " + date);

    SyncInfo syncInfo = new SyncInfo();
    syncInfo.setDate(date);
    syncInfo.setDeviceRegistrationId(device.getProfile().getRegistrationId());
    syncInfo.setEventType(eventType);
    syncInfo.save();
  }
}
