package com.sensor.helper;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

import base.hubble.database.DeviceProfile;

public class DeviceSensorDbo {

  public static List<DeviceProfile> getAllSensors() {
    return new Select().all().from(DeviceProfile.class).execute();
  }

  public static List<DeviceProfile> getSensorByMac(String macAddress) {

    return new Select()
        .from(DeviceProfile.class)
        .where("macAddress = ?", macAddress)
        .execute();
  }

  public static List<DeviceProfile> getSensorByParentId(String parentId) {

    return new Select()
        .from(DeviceProfile.class)
        .where("parentId = ?", parentId)
        .execute();
  }

  public static void deleteSensor(Long id) {
    new Delete().from(DeviceProfile.class).where("Id = ?", id).execute();
  }
}
