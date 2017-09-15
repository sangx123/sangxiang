package com.hubble.streaming.sdcard;

/**
 * Created by hoang on 12/12/16.
 */

public interface RtmpFileStreamingHandler {
    public void onRtmpFileStreamingSuccess(String rtmpUrl);
    public void onRtmpFileStreamingFailed(int errorCode, int statusCode);
}
