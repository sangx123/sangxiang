package com.sensor.bluetooth;

import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GattInfo {
  // Bluetooth SIG identifiers
  public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  private static final String uuidBtSigBase = "0000****-0000-1000-8000-00805f9b34fb";
  private static final String uuidTiBase = "f000****-0451-4000-b000-000000000000";

  public static final UUID GENERIC_ACCESS_SERVICE_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");

  public static final UUID HID_SERVICE_UUID = UUID.fromString("00001812-0000-1000-8000-00805f9b34fb");
  public static final UUID HID_REPORT_TYPE_UUID = UUID.fromString("00002A4d-0000-1000-8000-00805f9b34fb");

  public static final UUID OAD_SERVICE_UUID = UUID.fromString("f000ffc0-0451-4000-b000-000000000000");
  public static final UUID OAD_CHARACTERISTIC = UUID.fromString("F000FFC1-0451-4000-b000-000000000000");
  public static final UUID CC_CHARACTERISTIC = UUID.fromString("F000CCC2-0451-4000-b000-000000000000");
  public static final UUID OAD2_CHARACTERISTIC = UUID.fromString("F000FFC2-0451-4000-b000-000000000000");

  public static final UUID CC_SERVICE_UUID = UUID.fromString("f000ccc0-0451-4000-b000-000000000000");
  public static final UUID GECKO_PROFILE = UUID.fromString("F000FCF0-0451-4000-B000-000000000000"); //UUID.fromString("F000AA10-0451-4000-B000-000000000000"); //UUID.fromString("F000FCF0-0451-4000-B000-000000000000");
  public static final UUID GECKO_PROFILE_ALERT_CONFIGUTATION = UUID.fromString("F000FCF2-0451-4000-B000-000000000000");// UUID.fromString("F000FCF2-0451-4000-B000-000000000000");
  public static final UUID GECKO_SERVICE = UUID.fromString("F000FEF0-0451-4000-B000-000000000000");
  public static final UUID CONNECTION_CONTROL_CHARACTERISTIC = UUID.fromString("F000FCF4-0451-4000-b000-000000000000");
  public static final UUID DEVICE_INFORMATION = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");
  public static final UUID HARDWARE_VERSION_UUID = UUID.fromString("00002A27-0000-1000-8000-00805f9b34fb");
  public static final UUID FIRMWARE_VERSION_UUID = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb");

  public static final UUID BATTERY_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
  public static final UUID BATTERY_LEVEL_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

  public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
  public static final UUID IMMEDIATE_ALERT_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
  public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
  public static final UUID ALERT_LEVEL_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

  public static final UUID CONNECTION_PARAM_UUID = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb");

  public static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

  public static final UUID CURRENT_TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
  public static final UUID DATE_TIME_UUID = UUID.fromString("00002A08-0000-1000-8000-00805f9b34fb");
  public static final UUID ALARM = UUID.fromString("F000FBF0-0451-4000-B000-000000000000");

  private static Map<String, String> mNameMap = new HashMap<String, String>();
  private static Map<String, String> mDescrMap = new HashMap<String, String>();

  public GattInfo(XmlResourceParser xpp) {
    // XML data base
    try {
      readUuidData(xpp);
    } catch (XmlPullParserException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String uuidToName(UUID uuid) {
    String str = toShortUuidStr(uuid);
    return uuidToName(str.toUpperCase());
  }

  public static String getDescription(UUID uuid) {
    String str = toShortUuidStr(uuid);
    return mDescrMap.get(str.toUpperCase());
  }

  static public boolean isTiUuid(UUID u) {
    String us = u.toString();
    String r = toShortUuidStr(u);
    us = us.replace(r, "****");
    return us.equals(uuidTiBase);
  }

  static public boolean isBtSigUuid(UUID u) {
    String us = u.toString();
    String r = toShortUuidStr(u);
    us = us.replace(r, "****");
    return us.equals(uuidBtSigBase);
  }

  static public String uuidToString(UUID u) {
    String uuidStr;
    if (isBtSigUuid(u))
      uuidStr = GattInfo.toShortUuidStr(u);
    else
      uuidStr = u.toString();
    return uuidStr.toUpperCase();
  }

  static private String toShortUuidStr(UUID u) {
    return u.toString().substring(4, 8);
  }

  private static String uuidToName(String uuidStr16) {
    return mNameMap.get(uuidStr16);
  }

  //
  // XML loader
  //
  private void readUuidData(XmlResourceParser xpp) throws XmlPullParserException, IOException {
    xpp.next();
    String tagName = null;
    String uuid = null;
    String descr = null;
    int eventType = xpp.getEventType();

    while (eventType != XmlPullParser.END_DOCUMENT) {
      if (eventType == XmlPullParser.START_DOCUMENT) {
        // do nothing
      } else if (eventType == XmlPullParser.START_TAG) {
        tagName = xpp.getName();
        uuid = xpp.getAttributeValue(null, "uuid");
        descr = xpp.getAttributeValue(null, "descr");
      } else if (eventType == XmlPullParser.END_TAG) {
        // do nothing
      } else if (eventType == XmlPullParser.TEXT) {
        if (tagName.equalsIgnoreCase("item")) {
          if (!uuid.isEmpty()) {
            uuid = uuid.replace("0x", "");
            mNameMap.put(uuid, xpp.getText());
            mDescrMap.put(uuid, descr);
          }
        }
      }
      eventType = xpp.next();
    }
  }
}
