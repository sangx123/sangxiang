package com.hubble.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class EventVideo implements Serializable {
	private String imageURL;
	private String filePath;
	private String clipName;
	private String md5Sum;
	private int storageMode;
	private boolean isLocal;
	private Date eventTime;
	private String eventString;
	private String registrationId;


	private ArrayList<String> videoClipList;


	public EventVideo(String imageURL, String filePath, String clipName, String md5Sum, final int storageMode,
	                   Date eventTime, String eventString, String registrationId) {

		this.imageURL = imageURL;
		this.filePath = filePath;
		this.clipName = clipName;
		this.md5Sum = md5Sum;
		this.storageMode = storageMode;
		this.eventTime = eventTime;
		this.eventString = eventString;
		this.registrationId = registrationId;
	}


	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getClipName() {
		return clipName;
	}

	public void setClipName(String clipName) {
		this.clipName = clipName;
	}

	public String getMd5Sum() {
		return md5Sum;
	}

	public void setMd5Sum(String md5Sum) {
		this.md5Sum = md5Sum;
	}

	public int getStorageMode() {
		return storageMode;
	}

	public void setStorageMode(int storageMode) {
		this.storageMode = storageMode;
	}

	public boolean isLocal() {
		return isLocal;
	}

	public void setLocal(boolean local) {
		isLocal = local;
	}

	public Date getEventTime() {
		return eventTime;
	}

	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}

	public String getEventString() {
		return eventString;
	}

	public void setEventString(String eventString) {
		this.eventString = eventString;
	}

	public String getRegistrationId() {
		return registrationId;
	}

	public void setRegistrationId(String registrationId) {
		this.registrationId = registrationId;
	}

	public ArrayList<String> getVideoClipList() {
		return videoClipList;
	}

	public void setVideoClipList(ArrayList<String> videoClipList) {
		this.videoClipList = videoClipList;
	}
}