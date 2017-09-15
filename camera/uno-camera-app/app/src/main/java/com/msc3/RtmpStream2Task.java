package com.msc3;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.analytics.GAEvent;
import com.hubble.analytics.GAEventAction;
import com.hubble.analytics.GoogleAnalyticsController;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.registration.models.BabyMonitorAuthentication;
import com.hubble.registration.tasks.RemoteStreamTask;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import base.hubble.PublicDefineGlob;
import base.hubble.meapi.Device;
import base.hubble.meapi.device.CreateSessionKeyResponse3;
import base.hubble.meapi.device.CreateSessionKeyResponse3.CreateSessionKeyResponseData3;
import base.hubble.meapi.device.JobStatusResponse;

public class RtmpStream2Task extends RemoteStreamTask {

  private static final String TAG = "RtmpStream2Task";
  private String regId;
  private String userToken;
  private String clientType;

  protected Context mContext;

  private int server_status_code;
  private int server_error_code;
  private String server_error_msg;

  private static final String LOCATION = "Location";
  private static final String RETRYAFTER = "Retry-After";
  private String job_location = null;
  private int    retry_time = 0;
  private EventData eventData;
  private SecureConfig settings = HubbleApplication.AppConfig;

  public RtmpStream2Task(Handler mHandler, Context mContext) {
    super(mHandler, mContext);

    this.mContext = mContext;
    eventData = new EventData();
    server_error_code = 777; // Init with a strange value
    server_status_code = 778; // Init with a strange value
    server_error_msg = "";
  }


  private void parseForLocationAndRetry(Map <String, List<String>> responseHeaders)
  {
    String value = null;
    if (responseHeaders.containsKey(LOCATION) == true)
    {

      job_location = responseHeaders.get(LOCATION).get(0);
    }

    if (responseHeaders.containsKey(RETRYAFTER) == true)
    {
      value = responseHeaders.get(RETRYAFTER).get(0);
      try
      {
        retry_time = Integer.parseInt(value);
      }
      catch(NumberFormatException ne)
      {
        retry_time = 3;// Default value
      }
    }

    Log.d("mbp", "Parsed Location: " + job_location + " retry:" + retry_time);

    return ;
  }

  @Override
  protected BabyMonitorAuthentication doInBackground(String... params) {
    BabyMonitorAuthentication bm_auth = null;

    regId = params[0];
    userToken = params[1];
    clientType = params[2];
    Log.d(TAG, "Sending create rtmp session...");
    try {
      boolean isSecure=settings.getBoolean(PublicDefineGlob.PREFS_IS_RTMPS_ENABLED, true);
      CreateSessionKeyResponse3 session_info = Device.getSessionKey3(userToken, regId, clientType,isSecure);
      if (session_info != null) {
        if (session_info.getStatus() == HttpURLConnection.HTTP_OK) { //status : 200 - NO CHANGE
          CreateSessionKeyResponseData3 session_data = session_info.getData();
          if (session_data != null) {
            String streamUrl = session_data.getUrl();
            if (streamUrl != null) {
              Log.d(TAG, "** Viewing " + streamUrl);
              bm_auth = new BabyMonitorRelayAuthentication(null, "80", null, regId, null, streamUrl, 80, null, null);
            }
          }
        }
        else if (session_info.getStatus() == HttpURLConnection.HTTP_ACCEPTED) {// status: 202
          // set server_status_code value for analytic purpose
          server_status_code = session_info.getStatus();
          // Get HTTP resonse HEADER "Location" field   ---> server_job_url = SERVER_URL + "Location"
          // Get HTTP resonse HEADER "Retry-After" field --> server_processing_time
          parseForLocationAndRetry(session_info.getResponseHeaders());

          try {
            // Sleep(server_processing_time)
            Thread.sleep(retry_time* 1000);
          }
          catch (InterruptedException ie)
          {
            ie.printStackTrace();
          }

          String rtmp_link = null;
          Long timeout = System.currentTimeMillis() + 180*1000; //3min top retrying this madness
          do {
            // Query (server_job_url) again
            JobStatusResponse jobStatusResponse = Device.getJobStatus(userToken, job_location);
            if (jobStatusResponse != null) {
              // 200 -> looks OK RTMP link should be ready
              if (jobStatusResponse.getStatus() == HttpURLConnection.HTTP_OK)
              {
                JobStatusResponse.JobStatusResponseData data = jobStatusResponse.getData();

                rtmp_link = data.getOutput().getRtmp_url();
                Log.d(TAG, "rtmp_link:"+rtmp_link);
                if (!TextUtils.isEmpty(rtmp_link)) {

                  bm_auth = new BabyMonitorRelayAuthentication(null, "80", null, regId, null, rtmp_link, 80, null, null);
                  //found it !!!
                  break;
                } else {
                  Log.d(TAG, "jobStatusResponse 200 but rtmp_link is empty, retrying...");
                  try {
                    Thread.sleep(1000);
                  } catch (InterruptedException e) {
                  }
                }
              }
              // if status = 202
              //       Get HTTP response Header "Location" and "Retry-After" -> Update server_job_url & server_processing_time
              //        Go to Sleep
              else if (jobStatusResponse.getStatus() == HttpURLConnection.HTTP_ACCEPTED)
              {
                // set server_status_code value for analytic purpose
                server_status_code = jobStatusResponse.getStatus();
                parseForLocationAndRetry(jobStatusResponse.getResponseHeaders());


                try {
                  // Sleep(server_processing_time)
                  Thread.sleep(retry_time * 1000);
                } catch (InterruptedException ie) {
                }
                //Loop back to check again
              }
              //if status = 424 / 404
              //        Error--- out & retry global flow
              else
              {
                Log.e(TAG, "Error jobStatusResponse.getStatus() return : " + jobStatusResponse.getStatus());
                JobStatusResponse.JobStatusResponseData data = jobStatusResponse.getData();
                if (data != null) {
                  Log.e(TAG, "Error output: status" + data.getOutput().getDeviceStatus() +
                         " reason:" + data.getOutput().getReason());
                }


                server_status_code = jobStatusResponse.getStatus();
                server_error_code = jobStatusResponse.getCode();
                server_error_msg = jobStatusResponse.getMessage();
                rtmp_link = null;
                bm_auth = null;

                break;
              }
            }
            else //jobStatusResponse = null
            {
              //NULL Response !!! XXX !!!
              Log.e(TAG, "Error getting response from getJobStatus" );

              break;
            }

            if (this.isCancelled())
            {
              Log.d(TAG, "Streaming task is being canceled , user exit? " );
              break;
            }

          } while (timeout > System.currentTimeMillis());
        }

        else {
          server_status_code = session_info.getStatus();
          server_error_code = session_info.getCode();
          server_error_msg = session_info.getMessage();
        }
      }
    } catch (Exception e) {
      Log.e(TAG, Log.getStackTraceString(e));
      server_error_msg = "Timeout exception";
    }
    Log.d(TAG, "Sending create rtmp session...DONE");
    return bm_auth;
  }

  @Override
  protected void onPostExecute(BabyMonitorAuthentication result) {
    if (mHandler != null) {
      Message msg;
      if (result != null) {
        // // Log.d(TAG, "Got remote stream...");
        GoogleAnalyticsController.getInstance().trackEvent("Create_session pass");
        AnalyticsInterface.getInstance().trackEvent("Create_session_pass","Create_session_pass",eventData);
        msg = Message.obtain(mHandler, MSG_VIEW_CAM_SUCCESS, result);
      } else {
        // // Log.d(TAG, "Failed to view remote stream.");
        String message = "";
        String status = "";
        String code = "";
        if(server_error_msg == null) {
          message = "Server return null message";
        } else {
          if (server_error_msg.equals("Timeout Exception")) {
            message = String.valueOf(server_error_msg);
          } else {
            status = String.valueOf(server_status_code);
            code = String.valueOf(server_error_code);
            message = String.valueOf(server_error_msg);
          }
        }
        List<GAEventAction> actions = new ArrayList<GAEventAction>();
        actions.add(new GAEventAction("status", status));
        actions.add(new GAEventAction("code", code));
        actions.add(new GAEventAction("message", message));
        GoogleAnalyticsController.getInstance().trackEvent(new GAEvent("Create_session failed", actions));
        msg = Message.obtain(mHandler, MSG_VIEW_CAM_FALIED, server_status_code, server_error_code);
      }
      mHandler.dispatchMessage(msg);
    }
  }

  @Override
  protected void onCancelled() {
    Log.d(TAG, "RtmpStream2Task onCancelled");
  }
}
