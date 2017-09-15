package com.hubble.streaming.sdcard;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hubble.command.CameraCommandUtils;
import com.hubble.util.DebugUtils;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Random;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.ServerDeviceCommand;
import base.hubble.command.PublishCommandRequestBody;
import base.hubble.command.RemoteCommandRequest;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.converter.ConversionException;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;

/**
 * Created by hoang on 12/12/16.
 */

public class RtmpFileStreamingJobBasedTask extends AsyncTask<String, Void, Boolean> {
    private static final String TAG = "RtmpFileStreaming";

    private static final long FILE_STREAMING_TASK_DURATION_MAX = 180 * 1000;
    private static final String HEADER_LOCATION_PREFIX = "/v1/jobs/";
    private static final String HEADER_LOCATION = "Location";
    private static final String HEADER_RETRY_AFTER = "Retry-After";
    public static final int DEVICE_OFFLINE_CONNECTIVITY_ISSUE_CODE = 701;
    public static final int DEVICE_DISCONNECTED = 709;

    private Gson mGson;
    private GsonConverter mGsonConverter;
    private String mJobId = null;
    private int mRetryTime = 0;
    private RtmpFileStreamingHandler mRtmpFileStremingHandler;

    private String mApiKey, mRegistrationId, mClientType, mClipName, mMd5Sum, mClientNatIp, mRtmpUrl;

    private int mErrorCode;
    private int mStatusCode;

    public RtmpFileStreamingJobBasedTask() {
        mErrorCode = -1;
        mStatusCode = -1;
        GsonBuilder builder = new GsonBuilder();
        mGson = builder.create();
        mGsonConverter = new GsonConverter(mGson);
    }

    public void setRtmpFileStremingHandler(RtmpFileStreamingHandler rtmpFileStremingHandler) {
        mRtmpFileStremingHandler = rtmpFileStremingHandler;
    }

    public void setApiKey(String apiKey) {
        mApiKey = apiKey;
    }

    public void setRegistrationId(String registrationId) {
        mRegistrationId = registrationId;
    }

    public void setClientType(String clientType) {
        mClientType = clientType;
    }

    public void setClipName(String clipName) {
        mClipName = clipName;
    }

    public void setMd5Sum(String md5Sum) {
        mMd5Sum = md5Sum;
    }

    public void setClientNatIp(String clientNatIp) {
        mClientNatIp = clientNatIp;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean result = false;

        // Close relay RTMP first
        int closeRelayRtmpRetries = 3;
        while (closeRelayRtmpRetries-- > 0 && !isCancelled()) {
            Log.d(TAG, "Close relay RTMP streaming...");
            PublishCommandRequestBody.Builder builder = new PublishCommandRequestBody.Builder();
            builder.setCommand("close_relay_rtmp");
            PublishCommandRequestBody requestBody = builder.create();
            RemoteCommandRequest request = new RemoteCommandRequest();
            request.setApiKey(mApiKey);
            request.setRegistrationId(mRegistrationId);
            request.setPublishCommandRequestBody(requestBody);
            request.setCommand("close_relay_rtmp");
            boolean success = CameraCommandUtils.sendCommandGetSuccess(request);
            if (success) {
                Log.d(TAG, "Close relay RTMP streaming success");
                break;
            } else {
                if(closeRelayRtmpRetries <= 0)
                {
                    mErrorCode = DEVICE_OFFLINE_CONNECTIVITY_ISSUE_CODE;
                    return result;
                }
                Log.d(TAG, "Close relay RTMP streaming failed");
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
        }

        // Create RTMP file session V5 to server
        Response createFileSessionRes = null;
        int createRtmpFileSessionRetries = 1;
        while (createRtmpFileSessionRetries-- > 0 && !isCancelled()) {
            Log.d(TAG, "Create RTMP file streaming...");
            try {
                createFileSessionRes = Api.getInstance().getService().createFileSessionJobBased(mApiKey, mRegistrationId, "1",
                        new Models.CreateFileSessionRequest(mClientType, mClipName, mMd5Sum, mClientNatIp));
                if (createFileSessionRes != null) {
                    mErrorCode = createFileSessionRes.getStatus();
                    Log.d(TAG, "Create RTMP file streaming DONE, error code: " + mErrorCode);
                    if (mErrorCode == HttpURLConnection.HTTP_OK) {
                        break;
                    }
                } else {
                    Log.d(TAG, "Create RTMP file streaming DONE, response is null");
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            if (createRtmpFileSessionRetries > 0) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
            }
        }

        if (createFileSessionRes != null) {
            mErrorCode = createFileSessionRes.getStatus();
            List<Header> headers = createFileSessionRes.getHeaders();
            TypedInput body = createFileSessionRes.getBody();
            parseForLocationAndRetry(headers);
            Models.ApiResponse<Models.CreateFileStreamJobBasedData> createFileSessionResData = parseResponseBody(body);
//                    Log.d(TAG, "Create RTMP file streaming DONE, body: " + body.toString());

            // Parse header and get response
            if (mErrorCode == HttpURLConnection.HTTP_OK) {
                // Get RTMP url from body
                if (createFileSessionResData != null) {
                    try {
                        mStatusCode = Integer.parseInt(createFileSessionResData.getStatus());
                    } catch (NumberFormatException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }

                    Models.CreateFileStreamJobBasedData data = createFileSessionResData.getData();
                    if (data != null) {
                        Models.CreateFileStreamJobBasedOutput output = data.getOutput();
                        if (output != null) {
                            mRtmpUrl = output.getRtmp_url();
                            Log.d(TAG, "Got file streaming url response: " + mRtmpUrl);

                        } else {
                            Log.e(TAG, "Create file streaming job based, empty data output");
                        }
                    } else {
                        Log.e(TAG, "Create file streaming job based, empty body response data");
                    }
                } else {
                    Log.e(TAG, "Create file streaming job based, empty body response");
                }
            } else if (mErrorCode == HttpURLConnection.HTTP_ACCEPTED) {
                // Waiting rtmp stream ready with job query
                String rtmp_link = null;
                long fileStreamTaskEndTime = System.currentTimeMillis() + FILE_STREAMING_TASK_DURATION_MAX;
                do {
                    try {
                        Response jobStatusRes = Api.getInstance().getService().getJobStatus(mApiKey, mJobId);
                        if (jobStatusRes != null) {
                            mStatusCode = jobStatusRes.getStatus();
                            Log.d(TAG, "Get job status code: " + mStatusCode);
                            // 200 -> looks OK RTMP link should be ready
                            TypedInput jobStatusBody = jobStatusRes.getBody();
                            Models.ApiResponse<Models.JobStatusResponseData> jobStatusResData = parseJobStatusResponseBody(jobStatusBody);
                            if (mStatusCode == HttpURLConnection.HTTP_OK) {
                                Models.JobStatusResponseData data = jobStatusResData.getData();
                                if (data != null) {
                                    Models.JobStatusResponseOutput output = data.getOutput();
                                    if (output != null) {
                                        rtmp_link = output.getRtmp_url();
                                        Log.d(TAG, "Got file streaming url from job status response: " + rtmp_link);
                                        if (!TextUtils.isEmpty(rtmp_link)) {
                                            if (DebugUtils.needForceSecureRemoteStreaming()) {
                                                Log.d(TAG, "File streaming URL after transform: " + rtmp_link);
                                            }

                                            mRtmpUrl = rtmp_link;
                                            //found it !!!
                                            break;
                                        }
                                        else if(output.getDeviceStatus() != null && output.getDeviceStatus().equalsIgnoreCase("701") || output.getDeviceStatus().equalsIgnoreCase("709"))
                                        {
                                            // device is disconnected
                                            mErrorCode = DEVICE_OFFLINE_CONNECTIVITY_ISSUE_CODE;
                                            break;
                                        }
                                    }
                                }

                                Log.d(TAG, "jobStatusResponse 200 but rtmp_link is empty, retrying...");
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                }
                            }
                            // if status = 202
                            //       Get HTTP response Header "Location" and "Retry-After" -> Update server_job_url & server_processing_time
                            //        Go to Sleep
                            else if (mStatusCode == HttpURLConnection.HTTP_ACCEPTED) {
                                // set server_status_code value for analytic purpose
                                parseForLocationAndRetry(jobStatusRes.getHeaders());

                                try {
                                    // Sleep(server_processing_time)
                                    Thread.sleep(mRetryTime * 1000);
                                } catch (InterruptedException ie) {
                                }
                                //Loop back to check again
                            }
                            //if status = 424 / 404
                            //        Error--- out & retry global flow
                            else {
                                Models.JobStatusResponseData data = jobStatusResData.getData();
                                if (data != null && data.getOutput() != null) {
                                    Log.e(TAG, "Error output: status " + data.getOutput().getDeviceStatus() +
                                            ", reason:" + data.getOutput().getReason());
                                }

//                            server_error_code = data.getCode();
//                            server_error_msg = jobStatusRes.getReason();
                                mRtmpUrl = null;

                                // 20161117: HOANG AA-1852: Handle server overloaded if any
                                handleServerOverloaded(mStatusCode);
                                break;
                            }
                        } else //jobStatusResponse = null
                        {
                            //NULL Response !!! XXX !!!
                            Log.e(TAG, "Error getting response from getJobStatus");
                            break;
                        }
                    } catch (Exception e) {
                        Log.d(TAG, Log.getStackTraceString(e));
                        break;
                    }

                    if (this.isCancelled()) {
                        Log.d(TAG, "Rtmp file streaming task is being canceled, exit");
                        break;
                    }

                } while (System.currentTimeMillis() < fileStreamTaskEndTime);
            } else {
                // Other error
                Log.d(TAG, "Create rtmp file streaming task failed");
                // 20161117: HOANG AA-1852: Handle server overloaded if any
                handleServerOverloaded(mErrorCode);
            }
        } else {
            Log.d(TAG, "Create RTMP file streaming DONE, response is null");
        }

        return result;
    }

    private void handleServerOverloaded(int serverErrorCode) {
        /*
         * 20161117 HOANG: AA-1852
         * When server overloaded, don't retry immediately.
         */
        long sleepTime = getRandomSleepTime();
        Log.d(TAG, "Create RTMP session error code: " + serverErrorCode);
        if (serverErrorCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            Log.d(TAG, "Server overloaded, random sleep time: " + sleepTime);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
        }
    }

    private long getRandomSleepTime() {
        long sleepTime;
        /*
         * 20161208 HOANG AA-1852
         * Generate a random sleep time between 10s and 20s
         */
        int minSleepTime = 10;
        int maxSleepTime = 20;
        Random r = new Random();
        int randomSleepTime = r.nextInt(maxSleepTime - minSleepTime + 1) + minSleepTime;
        sleepTime = randomSleepTime * 1000;
        return sleepTime;
    }

    private void parseForLocationAndRetry(List<Header> headers)
    {
        if (headers != null) {
            for (Header header : headers) {
//            Log.d(TAG, "Head name: " + header.getName() + ", value: " + header.getValue());
                if (header.getName() != null) {
                    if (header.getName().equalsIgnoreCase(HEADER_LOCATION)) {
                        String locationRes = header.getValue();
                        if (locationRes != null && locationRes.startsWith(HEADER_LOCATION_PREFIX)) {
                            mJobId = locationRes.substring(HEADER_LOCATION_PREFIX.length());
                        }
                    }

                    if (header.getName().equalsIgnoreCase(HEADER_RETRY_AFTER)) {
                        String retryAfter = header.getValue();
                        try {
                            mRetryTime = Integer.parseInt(retryAfter);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                            mRetryTime = 13;// Default value
                        }
                    }
                }
            }
        } else {
            Log.d(TAG, "Parse for location and retry failed, null headers");
        }

        Log.d(TAG, "Parsed job id: " + mJobId + ", retry:" + mRetryTime);
    }

    private Models.ApiResponse<Models.CreateFileStreamJobBasedData> parseResponseBody(TypedInput body) {
        Models.ApiResponse<Models.CreateFileStreamJobBasedData> createFileStreamBody = null;
        try {
            createFileStreamBody = (Models.ApiResponse<Models.CreateFileStreamJobBasedData>) mGsonConverter.fromBody(body,
                    new TypeToken<Models.ApiResponse<Models.CreateFileStreamJobBasedData>>(){}.getType());
        } catch (ConversionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        if (createFileStreamBody != null) {
            Log.d(TAG, "Parse response body: " + mGson.toJson(createFileStreamBody));
        } else {
            Log.d(TAG, "Parse response body null");
        }
        return createFileStreamBody;
    }

    private Models.ApiResponse<Models.JobStatusResponseData> parseJobStatusResponseBody(TypedInput body) {
        Models.ApiResponse<Models.JobStatusResponseData> jobStatusRes = null;
        try {
            jobStatusRes = (Models.ApiResponse<Models.JobStatusResponseData>) mGsonConverter.fromBody(
                    body, new TypeToken<Models.ApiResponse<Models.JobStatusResponseData>>(){}.getType());
        } catch (ConversionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        if (jobStatusRes != null) {
            Log.d(TAG, "Parse job status response body: " + mGson.toJson(jobStatusRes));
        } else {
            Log.d(TAG, "Parse job status response body null");
        }
        return jobStatusRes;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mRtmpFileStremingHandler != null) {
            if (!TextUtils.isEmpty(mRtmpUrl)) {
                mRtmpFileStremingHandler.onRtmpFileStreamingSuccess(mRtmpUrl);
            } else {
                mRtmpFileStremingHandler.onRtmpFileStreamingFailed(mErrorCode, mStatusCode);
            }
        } else {
            Log.d(TAG, "Rtmp file streaming job based task DONE, handler is null");
        }
    }
}
