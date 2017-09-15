package com.hubble.videobrowser;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Son Nguyen on 11/12/2015.
 */
public class VideoItem {
  private String cameraName;
  private Date time;
  private String filePath;

  private boolean selected = false;

  public VideoItem(String cameraName, String time, String filePath) {
    this.cameraName = cameraName;
    Long dateValue = System.currentTimeMillis();
    try {
      dateValue = Long.parseLong(time);
    } catch (Exception ex) {
      // just ignore it and set default time
    }
    this.time = new Date(dateValue);
    this.filePath = filePath;
  }

  public String getFormattedDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy HH:mm:ss a");
    return sdf.format(this.time);
  }

  public String getCameraName() {
    return cameraName;
  }

  public VideoItem setCameraName(String cameraName) {
    this.cameraName = cameraName;
    return this;
  }

  public Date getTime() {
    return time;
  }

  public VideoItem setTime(Date time) {
    this.time = time;
    return this;
  }

  public String getFilePath() {
    return filePath;
  }

  public VideoItem setFilePath(String filePath) {
    this.filePath = filePath;
    return this;
  }

  public boolean isSelected() {
    return selected;
  }

  public VideoItem setSelected(boolean selected) {
    this.selected = selected;
    return this;
  }

}
