package com.hubble.ui;


import com.hubble.devcomm.Device;
import com.hubble.framework.service.cloudclient.device.pojo.response.EventResponse;

public interface IEventHistoryActionCallBack {
	void onLoadFullScreenImage(EventImage eventImage, EventResponse event);

	void onPlayVideo(EventVideo eventVideo, EventResponse event);

	void shareVideoEvent(EventVideo eventVideo, Device selectedDevice);

	void downloadAndShareEvent(String imageUrl, int eventShareType, int actionType, Device selectedDevice);

}
