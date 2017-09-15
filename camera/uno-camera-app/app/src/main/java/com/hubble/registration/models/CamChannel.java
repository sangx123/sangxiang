package com.hubble.registration.models;

import android.widget.ImageView;

import com.hubble.registration.tasks.RemoteStreamTask;

import java.io.Serializable;

public class CamChannel implements Serializable {

  private static final long serialVersionUID = -4306588857336898530L;
  private static final String TAG = "CamChannel";
  private transient ImageView channel_view; //non serialize this
  private int channel_view_disable_res_id;
  private int channel_view_enable_res_id;

  private LegacyCamProfile profile;
  private int channel_configure_status;

  private String stream_url;

  public static final int LOCAL_VIEW = 1;
  public static final int REMOTE_HTTP_VIEW = 2;
  public static final int REMOTE_UDT_VIEW = 3;
  public static final int REMOTE_RELAY_VIEW = 4;
  private transient int currentViewSession;

  //*only valid if currentViewSession = REMOTE_HTTP_VIEW
  public static final int RV_INVALID_STATE = -1;
  public static final int RV_GET_PORT_STATE = 1;
  public static final int RV_VIEW_REQ_STATE = 2;
  public static final int RV_STREAM_STATE = 3;
  private transient int remoteViewSessionState;

  //private transient GetRemoteCamPortTask getPortTask; // just keep a reference
  private transient RemoteStreamTask viewRMTask; // just keep a reference
  private transient boolean relinkMode;


  public static final int CONFIGURE_STATUS_NOT_ASSIGNED = 0x100;
  public static final int CONFIGURE_STATUS_ASSIGNED = 0x102;


  public CamChannel() {
    profile = null;
    stream_url = null;
    channel_configure_status = CONFIGURE_STATUS_NOT_ASSIGNED;
    currentViewSession = LOCAL_VIEW;
    remoteViewSessionState = RV_INVALID_STATE;
    relinkMode = false;
  }

  public boolean setViewReqState(RemoteStreamTask viewTask) {
    if (((currentViewSession == REMOTE_HTTP_VIEW) && (RV_GET_PORT_STATE != remoteViewSessionState))) {
      // // Log.d(TAG, "invalid state at: setViewreq -- return false; ");
      return false;
    }

    if ((currentViewSession == REMOTE_UDT_VIEW) && (RV_STREAM_STATE == remoteViewSessionState)) {
      // // Log.d(TAG, "UDT STREAM : Was in RV_STREAM_STATE before this -- set relink to TRUE");
      relinkMode = true;
    }


    synchronized (this) {
      viewRMTask = viewTask;
      remoteViewSessionState = RV_VIEW_REQ_STATE;
    }
    return true;
  }

  /**
   * @return the stream_url
   */
  public String getStreamUrl() {
    return stream_url;
  }


  /**
   * @param stream_url the stream_url to set
   */
  public void setStreamUrl(String stream_url) {
    this.stream_url = stream_url;
  }


  public boolean setStreamingState() {
    if (RV_VIEW_REQ_STATE != remoteViewSessionState) {
      // // Log.d(TAG, "setStreamingState():invalid state at: setStream -- return false; ");
      return false;
    }


    synchronized (this) {
      remoteViewSessionState = RV_STREAM_STATE;
    }

    return true;
  }

  public void cancelRemoteConnection() {
    // // Log.d(TAG, "Cancel current remote connection");
    synchronized (this) {
      switch (remoteViewSessionState) {
        case RV_GET_PORT_STATE:
          break;
        case RV_VIEW_REQ_STATE:
          // // Log.d(TAG, "Current state: RV_VIEW_REQ_STATE (viewRMTask != null)? " + (viewRMTask != null));
          if (viewRMTask != null) {
            viewRMTask.cancel(true);
            viewRMTask = null;
          }
          break;

        case RV_STREAM_STATE:
          // // Log.d(TAG, "Current state: RV_STREAM_STATE");
          break;
        default:
          break;
      }

      //Reset state machine variables
      remoteViewSessionState = RV_INVALID_STATE;
      relinkMode = false;
    }


  }

  public void setCurrentViewSession(int currentViewSession) {
    this.currentViewSession = currentViewSession;
  }

  public boolean setCamProfile(LegacyCamProfile cp) {
    if (cp == null) {
      return false;
    }
    setState(CONFIGURE_STATUS_ASSIGNED);
    profile = cp;
    return true;
  }


  public LegacyCamProfile getCamProfile() {
    return profile;
  }

  public void reset() {
    setState(CONFIGURE_STATUS_NOT_ASSIGNED);
    profile = null;
    if (channel_view != null) {
      channel_view.setImageResource(channel_view_disable_res_id);
    }
  }

  /* reflect the change in image if any,
   * called after data restored to refresh the UI
   */
  public void refresh() {
    if ((channel_configure_status == CONFIGURE_STATUS_ASSIGNED) &&
        (profile != null) &&
        (channel_view != null)) {
      channel_view.setImageResource(channel_view_enable_res_id);
    } else {
      reset();
    }
  }

  private boolean setState(int state) {
    if ((state >= CONFIGURE_STATUS_NOT_ASSIGNED) && (state <= CONFIGURE_STATUS_ASSIGNED)) {
      channel_configure_status = state;
      return true;
    }
    return false;
  }

  public int getState() {
    return channel_configure_status;
  }

  public String toString() {
    if (channel_configure_status == CONFIGURE_STATUS_ASSIGNED) {
      return String.format("Channel %s", profile.getName());
    } else {
      return "Empty Channel";
    }
  }
}
