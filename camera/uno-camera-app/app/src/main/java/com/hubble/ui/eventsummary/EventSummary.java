package com.hubble.ui.eventsummary;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Admin on 07-03-2017.
 */
public class EventSummary implements Serializable {

	private Date date;
	private String snapShotPath;
	private String summaryVideoUrlPath;
	private double tempMin;
	private double tempMax;
	private double humidityMin;
	private double humidityMax;
	private int totalMotionEvent;
	private int totalSoundEvent;
	private int summaryType;
	private int layoutColor;

	public EventSummary(Date date, String snapShotPath, String summaryVideoUrlPath, int tempMin,
	                    int tempMax, int humidityMin, int humidityMax, int totalMotionEvent, int totalSoundEvent) {

		this(date, snapShotPath, summaryVideoUrlPath, totalMotionEvent, totalSoundEvent);
		this.tempMin = tempMin;
		this.tempMax = tempMax;
		this.humidityMin = humidityMin;
		this.humidityMax = humidityMax;
	}

	public EventSummary(Date date, String snapShotPath, String summaryVideoUrlPath,
	                    int totalMotionEvent, int totalSoundEvent) {
		this.date = date;
		this.snapShotPath = snapShotPath;
		this.summaryVideoUrlPath = summaryVideoUrlPath;
		this.totalMotionEvent = totalMotionEvent;
		this.totalSoundEvent = totalSoundEvent;
	}

	public EventSummary(){
		summaryType = EventSummaryConstant.NO_MOTION_SUMMARY_VIEW;
		totalMotionEvent = -1;
		totalSoundEvent = -1;
		tempMin = -100;
		tempMax = -100;
		layoutColor = 0;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getSnapShotPath() {
		return snapShotPath;
	}

	public void setSnapShotPath(String snapShotPath) {
		this.snapShotPath = snapShotPath;
	}

	public String getSummaryVideoUrlPath() {
		return summaryVideoUrlPath;
	}

	public void setSummaryVideoUrlPath(String summaryVideoUrlPath) {
		this.summaryVideoUrlPath = summaryVideoUrlPath;
	}

	public double getTempMin() {
		return tempMin;
	}

	public void setTempMin(double tempMin) {
		this.tempMin = tempMin;
	}

	public double getTempMax() {
		return tempMax;
	}

	public void setTempMax(double tempMax) {
		this.tempMax = tempMax;
	}

	public double getHumidityMin() {
		return humidityMin;
	}

	public void setHumidityMin(int humidityMin) {
		this.humidityMin = humidityMin;
	}

	public double getHumidityMax() {
		return humidityMax;
	}

	public void setHumidityMax(int humidityMax) {
		this.humidityMax = humidityMax;
	}

	public int getTotalMotionEvent() {
		return totalMotionEvent;
	}

	public void setTotalMotionEvent(int totalMotionEvent) {
		this.totalMotionEvent = totalMotionEvent;
	}

	public int getTotalSoundEvent() {
		return totalSoundEvent;
	}

	public void setTotalSoundEvent(int totalSoundEvent) {
		this.totalSoundEvent = totalSoundEvent;
	}

	public int getSummaryType() {
		return summaryType;
	}

	public void setSummaryType(int summaryType) {
		this.summaryType = summaryType;
	}

	public int getLayoutColor() {
		return layoutColor;
	}

	public void setLayoutColor(int layoutColor) {
		this.layoutColor = layoutColor;
	}


}
