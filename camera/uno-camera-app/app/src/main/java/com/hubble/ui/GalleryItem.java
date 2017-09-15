package com.hubble.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Admin on 22-02-2017.
 */
public class GalleryItem {
	private String cameraName;
	private Date time;
	private String filePath;
	private boolean isSelected = false;


	public SimpleDateFormat getDownloadDf() {
		return downloadDf;
	}

	public void setDownloadDf(SimpleDateFormat downloadDf) {
		this.downloadDf = downloadDf;
	}

	private boolean isSummaryVideo = false;

	private boolean selected = false;
	private SimpleDateFormat downloadDf = new SimpleDateFormat("MMM dd");

	public GalleryItem(String cameraName, Date time, String filePath) {
		this.cameraName = cameraName;
		this.time = time;
		this.filePath = filePath;
	}


	public String getCameraName() {
		return cameraName;
	}

	public void setCameraName(String cameraName) {
		this.cameraName = cameraName;
	}

	public String getTime() {
		if(time != null) {
			return downloadDf.format(time);
		}else {
			return null;
		}
	}

	public Date getDate(){
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSummaryVideo() {
		return isSummaryVideo;
	}

	public void setSummaryVideo(boolean summaryVideo) {
		isSummaryVideo = summaryVideo;
	}
}
