package com.hubble.ui;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Admin on 17-11-2016.
 */
public class EventImage implements Serializable {
	private String imageURL;
	private Date eventTime;
	private String eventString;
	private String registrationId;



	public EventImage(String imageURL, Date eventTime, String eventString, String registrationId) {

		this.imageURL = imageURL;
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
}
