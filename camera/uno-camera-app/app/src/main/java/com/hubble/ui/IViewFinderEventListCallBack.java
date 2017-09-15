package com.hubble.ui;

/**
 * Created by Admin on 06-10-2016.
 */
public interface IViewFinderEventListCallBack {
	void onClick (int type);
	boolean isStreamingViaLocal ();
	String getLaunchReason ();
	void launchCameraSettings ();
}
