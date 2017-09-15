package com.blinkhd.playback;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import base.hubble.meapi.Device;
import base.hubble.meapi.PublicDefines;
import base.hubble.meapi.device.GetTimelineEventsResponse;
import base.hubble.meapi.device.TimelineEvent;

//My AsyncTask start...
public class LoadPlaylistTask extends AsyncTask<String, Void, Void> {

  private static String TAG = "LoadPlaylistTask";
  public static final int NO_ERROR = 0;
  public static final int SOCKET_TIMEOUT_EXCEPTION = 1;
  public static final int IO_EXCEPTION = 2;
  public static final int MALFORMED_URL_EXCEPTION = 3;


  ProgressDialog pDialog;
  private LoadPlaylistListener listener;
  private TimelineEvent latest;
  private TimelineEvent[] allEvents;
  private boolean getAllEvents;
  private int errorCode;

  public LoadPlaylistTask(LoadPlaylistListener listener, boolean getAllEvents) {
    this.listener = listener;
    this.getAllEvents = getAllEvents;
    this.errorCode = NO_ERROR;
  }

  /* on UI Thread - when cancelled don't invoke callback. */
  protected void onCancelled() {
    //Do nothing on Cancelled
  }

  @Override
  protected Void doInBackground(String... params) {
    Log.d(TAG, "LoadPlaylistTask started.");
    String saved_token = params[0];
    String mac = params[1];
    String before_start_time = null;
    String eventCode = params[2];
    String alerts = null;
    int page = -1;
    int size = -1;

    TimelineEvent[] timelineEvents = null;
    try {
      //query for one event only
      if (!this.getAllEvents) {
        PublicDefines.setHttpTimeout(30 * 1000);
        GetTimelineEventsResponse eventRes = Device.getTimelineEvents(saved_token, mac, before_start_time, eventCode, alerts, page, size);
        if (eventRes != null && eventRes.getStatus() == HttpURLConnection.HTTP_OK) {
          timelineEvents = eventRes.getEvents();
        }

        if (timelineEvents != null && timelineEvents.length > 0) {
          latest = timelineEvents[0];
        } else {
          // // Log.d(TAG, "Latest event is null");
        }
      } else // Get all recorded playlist
      {

        PublicDefines.setHttpTimeout(30 * 1000);
        GetTimelineEventsResponse eventRes = Device.getTimelineEvents(saved_token, mac);
        if (eventRes != null && eventRes.getStatus() == HttpURLConnection.HTTP_OK) {
          timelineEvents = eventRes.getEvents();
        }
        allEvents = timelineEvents;

      }
    } catch (SocketTimeoutException e) {

      errorCode = SOCKET_TIMEOUT_EXCEPTION;
      e.printStackTrace();
      // // Log.e(TAG, Log.getStackTraceString(e));
    } catch (MalformedURLException e) {

      errorCode = MALFORMED_URL_EXCEPTION;
      e.printStackTrace();
      // // Log.e(TAG, Log.getStackTraceString(e));
    } catch (Exception ex) {
      errorCode = IO_EXCEPTION;
      ex.printStackTrace();
      // // Log.e(TAG, Log.getStackTraceString(ex));
    }

    if (isCancelled()) {
      return null;
    }


    return null;
  }

  @Override
  protected void onCancelled(Void result) {

    super.onCancelled(result);
    // // Log.d("mbp", "LoadPlaylistTask is cancelled.");
  }

  @Override
  protected void onPostExecute(Void result) {

    if (this.isCancelled()) {
      // // Log.d("mbp", "LoadPlaylistTask is cancelled.");
    } else {
      super.onPostExecute(result);
      if (errorCode == NO_ERROR) {
        if (this.listener != null) {
          this.listener.onRemoteCallSucceeded(latest, allEvents);
        }
      } else {
        if (this.listener != null) {
          this.listener.onRemoteCallFailed(errorCode);
        }
      }
    }
  }
}
