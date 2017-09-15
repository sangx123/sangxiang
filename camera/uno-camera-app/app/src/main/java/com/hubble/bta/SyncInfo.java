package com.hubble.bta;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Son Nguyen on 01 Sep 2016.
 */

/**
 * database model to store sync information
 */
@Table(name = "sync_info")
public class SyncInfo extends Model {
  public static final String EVENT_TYPE_BTA = "bta";
  public static final String EVENT_TYPE_BSC = "bsc";

  @Column(name = "device_registration_id")
  private String device_registration_id;
  @Column(name = "date")
  private String date;
  @Column(name = "unique_key", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
  private String unique_key;
  @Column(name = "event_type")
  private String event_type;

  public SyncInfo() {

  }

  public String getEventType() {
    return event_type;
  }

  public void setEventType(String eventType) {
    this.event_type = eventType;
    buildUniqueKey();
  }

  public String getDeviceRegistrationId() {
    return device_registration_id;
  }

  public void setDeviceRegistrationId(String deviceRegistrationId) {
    this.device_registration_id = deviceRegistrationId;
    buildUniqueKey();
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
    buildUniqueKey();
  }

  public String getUniqueKey() {
    return event_type;
  }

  private void buildUniqueKey() {
    this.unique_key = device_registration_id + "_" + date + "_" + event_type;
  }

  public static String buildUniqueKey(String device_registration_id, String date, String event_type) {
    return device_registration_id + "_" + date + "_" + event_type;
  }
}
