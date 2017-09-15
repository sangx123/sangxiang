package com.hubble.registration;

import android.util.Log;
import android.util.Pair;

import java.util.StringTokenizer;

/**
 * Created by dan on 06/08/14.
 */
public class PublicDefine {

  public static final String IS_APP_CHANGE_PWD = "is_app_change_pwd";
  public static final int BUFFER_EMPTY = 0;
  public static final int BUFFER_PROCESSING = 1;
  public static final int BUFFER_FULL = 2;
  // prefs for remote talkback
  // prefs for remote talkback
  public static final String PREFS_GO_DIRECTLY_TO_CAMERA = "selected_camera_mac_from_camera_setting";
  public static final String PREFS_SHOULD_GO_TO_CAMERA = "com.hubble.shouldGoDirectlyToCamera";
  public static final String PREFS_APP_VERSION_NUMBER = "com.hubble.lastAppVersionNumber";
  public static final String PREFS_GO_DIRECTLY_TO_SUMMARY = "go_to_summary";
  public static final String PREFS_GO_DIRECTLY_TO_REGID = "registration_id";
  public static final String SET_MOTION_AREA_CMD = "set_motion_area";
  public static final String SET_MOTION_AREA_PARAM_1 = "&grid=";
  public static final String SET_MOTION_AREA_PARAM_2 = "&zone=";
  public static final String MOTION_ON_PARAM = SET_MOTION_AREA_PARAM_1
      + "1x1"
      + SET_MOTION_AREA_PARAM_2
      + "00,";
  public static final String MOTION_OFF_PARAM = SET_MOTION_AREA_PARAM_1
      + "1x1";
  public static final String SET_RECORDING_PARAMETER_CMD = "set_recording_parameter";
  public static final String SET_RECORDING_PARAMETER_MVR_ON_PARAM = "&value=11";
  public static final String SET_RECORDING_PARAMETER_MVR_OFF_PARAM = "&value=01";
  public static final String GET_RECORDING_PARAMETER_CMD = "get_recording_parameter";
  public static final String PHONE_MBP2k = "Home Phone MBP2000";
  public static final String PHONE_MBP1k = "MBP1000";
  public static final String GET_FLV_STREAM_CMD = "/?action=flvstream";
  public static final String GET_FLV_STREAM_PARAM_1 = "&remote_session=";
  public static final String BM_SERVER = "https://monitoreverywhere.com/BMS/phoneservice?";
  public static final String BM_HTTP_CMD_PART = "action=command&command=";
  public static final String VIEW_CAM_CMD = "view_cam";
  public static final String VIEW_CAM_PARAM_1 = "&email=";
  public static final String VIEW_CAM_PARAM_2 = "&macaddress=";
  public static final int DEFAULT_AUDIO_PORT = 51108;
  public static final int STREAM_MODE_HTTP_LOCAL = 0;
  public static final int STREAM_MODE_HTTP_REMOTE = 1;
  public static final String GET_IMG_CMD = "get_image";
  public static final String GET_IMG_PARAM_1 = "&macaddress=";
  public static final String IS_CAM_AVAILABLE_ONLOAD_CMD = "is_cam_available_onload";
  public static final String IS_CAM_AVAILABLE_UPNP_CMD = "is_cam_available_upnp";
  public static final String PREFS_CLOCK_MODE = "int_clockMode";
  public static final int CLOCK_MODE_12H = 0;
  public static final int CLOCK_MODE_24H = 1;
  public static final String HTTP_CMD_PART = "/?action=command&command=";
  public static final String HTTP_GET_LOCAL_COMMAND = "http://%s" + HTTP_CMD_PART +"%s";
  public static final String GET_ROUTER_LIST = "get_routers_list";
  public static final String GET_RT_LIST = "get_rt_list";
  public static final String PREFS_NAME = "MBP_SETTINGS";
  public static final String PREFS_SAVED_PORTAL_TOKEN = "string_PortalToken";
  public static final String PREFS_POLICY_CHOICE_OPTION = "choice_option";
  public static final String PREFS_HINT_SCHEDULE_ONLY_SAVE_WHEN_EXIT = "hint_schedule_only_save_when_exit";
  public static final String PREFS_LAST_TIME_GET_SOC_VERSION = "last_time_get_soc_version";
  public static final String PREFS_SOC_VERSION = "soc_version";
  public static final String GET_VERSION = "get_version";
  public static final String GET_CODECS_SUPPORT = "get_codecs_support";
  public static final String GET_MAC_ADDRESS = "get_mac_address";
  public static final String GET_SDLOCAL_FILE_CMD = "sd_localfileplay";
  public static final String GET_SDLOCAL_FILE_CMD_SUCCEED_RESULT = "sd_localfileplay: 0";

  public static final String SEND_CTRL_CMD = "send_stun_command";
  public static final String SEND_CTRL_PARAM_1 = "&macaddress=";
  public static final String SEND_CTRL_PARAM_2 = "&channelid=";
  public static final String SEND_CTRL_PARAM_3 = "&query=";

  public static final String DEFAULT_SSID_HD = "CameraHD-";
  public static final String GET_MOTION_AREA_CMD = "get_motion_area";
  public static final String GET_MODEL = "get_model";
  public static final String GET_UDID = "get_udid";
  public static final String CODEC_MJPEG = "MJPEG";
  public static final String CODEC_H264 = "H264";
  public static final String MODEL_MBP36N = "MBP36N";
  public static final String MODEL_MBP33N = "MBP33N";
  public static final String MODEL_MBP41N = "MBP41N";
  public static final String MODEL_FOCUS66 = "FOCUS66";
  public static final String MODEL_FOCUS96 = "FOCUS96";
  public static final String MODEL_FOCUS85 = "FOCUS85";
  public static final String MODEL_FOCUS854 = "FOCUS854";
  public static final String MODEL_MBP83 = "MBP83";
  public static final String MODEL_MBP836 = "MBP836";
  public static final String MODEL_ID_MBP36N = "0036";
  public static final String MODEL_ID_MBP33N = "0033";
  public static final String MODEL_ID_MBP41N = "0041";
  public static final String MODEL_ID_FOCUS66 = "0066";
  public static final String MODEL_ID_FOCUS96 = "0096";
  public static final String MODEL_ID_FOCUS85 = "0085";
  public static final String MODEL_ID_FOCUS662 = "0662";
  public static final String MODEL_ID_FOCUS662S = "1662";
  public static final String MODEL_ID_173 = "0173";
  public static final String MODEL_ID_172 = "0172";

  public static final String MODEL_ID_MBP931 = "0931";
  public static final String MODEL_ID_MBP921 = "0921";
  public static final String MODEL_ID_FOCUS854 = "0854";
  public static final String MODEL_ID_MBP83 = "0083";
  public static final String MODEL_ID_MBP836 = "0836";

  public static final String MODEL_ID_FOCUS73 = "0073";
  public static final String MODEL_ID_FOCUS72 = "0072";
  public static final String MODEL_ID_FOCUS86 = "0086";

  public static final String MODEL_ID_ORBIT = "0080";
  public static final String MODEL_ID_SMART_NURSERY = "0877";

  public static final String DEFAULT_REGID_OPEN_SENSOR = "070";
  public static final int RESULT_SHARE_SNAPSHOT = 1017;

  public static final int CAMERA_UPNP_NOT_OK = 0;
  public static final int CAMERA_UPNP_OK = 1;
  public static final int CAMERA_UPNP_IN_PROGRESS = 2;
  public static final int MSG_AUTO_CONF_SUCCESS = 0x1111;
  public static final int MSG_AUTO_CONF_FAILED = 0x2222;
  public static final int MSG_AUTO_CONF_CANCELED = 0x3333;
  public static final int MSG_AUTO_CONF_SHOW_ABORT_PAGE = 0x4444;
  public static final int MSG_AUTO_CONF_SSID_CHANGED = 0x5555;
  public static final int START_SCAN_FAILED = 0x2225;
  public static final int CONNECT_TO_CAMERA_FAILED = 0x2226;
  public static final int SEND_DATA_TO_CAMERA_FAILED = 0x2227;
  public static final int CONNECT_TO_HOME_WIFI_FAILED = 0x2228;
  public static final int SCAN_CAMERA_FAILED = 0x2229;
  public static final int CAMERA_DOES_NOT_HAVE_SSID = 0x2230;
  public static final int USB_STORAGE_TURNED_ON = 0x2231;
  public static final int INCORRECT_WIFI_PASSWORD = 0x2232;
  public static final int CONNECTING_TO_HOME_WIFI_FAILED = 0x2233;
  public static final int CONNECTING_TO_HOME_WIFI_SUCCESSFUL = 0x2234;
  public static final String BROADCAST_BINDING_DATA_CHANGE = "binding_data_change";
  public static final String BROADCAST_BINDING_DATA_CHANGE_REGID = "binding_data_change_regid";
  public static final String BROADCAST_BINDING_DATA_CHANGE_HAVE_NEW_DATA = "binding_data_change_have_new_data";
  private static final String TAG = "PublicDefine";
  public static final int CAMERA_UNREACHABLE_THRESHOLD = 10;               // minutes
  public static final int SHORT_CLIP_SIZE = 2;
  public static final String IS_USER_ARGEED_WITH_MOTION_TRIGGER_RECORDING_PRIVACY_POLICY = "user_agreed_privacy_policy";
  public static final int FW_UPGRADE_FAILED = 0x2233;
  public static final String YOUTUBE_API_KEY = "AIzaSyD0aiMRO8jnVatBQyxueBLmY220hgPftqQ";
  public static final String CHECK_LATEST_FW_OTA1 = "http://ota.hubble.in/ota1/%s_patch/version.txt";
  public static final String CHECK_LATEST_FW_OTA = "http://ota.hubble.in/ota/%s_patch/version.txt";
  public static final String CMD_UPGRADE_FW = "http://192.168.193.1:8080/cgi-bin/fullupgrade";
  public static final String FIRMWARE_DOWNLOAD_LINK_URL_0854_PATTERN = "https://ota.hubble.in/ota1/%s_patch/%s-%s.tar.gz";
  public static final String FIRMWARE_DOWNLOAD_LINK_URL_0086_PATTERN = "https://ota.hubble.in/ota/%s_patch/%s-%s.tar";
  public static final String FIRMWARE_011700_PATTERN = "https://ota.hubble.in/ota/%s_patch/%s-%s.tar.gz";

  public static final String MQTT_HOST = "mqtt-dev-iot.hubble.in";
  public static final String MQTT_PORT = "1883";
  public static final String RGB_KEY = "rgb_mqtt";

  public static final String[] KEYS = new String[]{"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};

  public static final String FW_VERSION_DOT = "\\.";
  public static final String SETTING2_GENERIC_FIRMWARE_VERSION = "01.19.24";

  public static final String SETTING_2_KEY_CEILING_MOUNT = "cm";
  public static final String SETTING_2_KEY_BRIGHTNESS = "br";
  public static final String SETTING_2_KEY_VOLUME = "vl";
  public static final String SETTING_2_KEY_CONTRACT = "ct";
  public static final String SETTING_2_KEY_NIGHT_VISION = "nv";
  public static final String SETTING_2_KEY_PARKING = "park";
  public static final String SETTING_2_KEY_OVERLAY_DATE = "overlay_date";
  public static final String SETTING_2_KEY_VIEW_MODE = "view_mode";
  public static final String SETTING_2_KEY_NIGHT_LIGHT = "night_light";
  public static final String DISPLAY_RTL = "display_rtl";
  public static final String SETTING_2_KEY_QUALITY_OF_SERVICE = "qos";

  public static final int MOTION_DETECTION_SOURCE = 0;
  public static final int PIR_MOTION_DETECTION_SOURCE = 1;
  public static final int HUMAN_DETECTION_SOURCE = 2;
  public static final int FACE_RECOGNITION_SOURCE = 3;



  // this is applied for sdcard storage
  public static final String SDCARD_FREE_SPACE = "sd_card_free";
  public static final String SDCARD_CAPACITY = "sd_card_cap";
  public static final String ORBIT_SDCARD_CAPACITY_FIRMWARE_VERSION = "01.19.95";
  public static final String ORBIT_VIDEO_RECORDING_FIRMWARE_VERSION = "02.10.02";
  public static final String ORBIT_PLAN_ENABLE_FIRMWARE_VERSION = "02.10.07";


  public static final String ORBIT_FREE_STORAGE_SPACE = "free_storage_space";
  public static final String ORBIT_OTA_COUNTER = "orbit_ota_counter";
  public static final int ORBIT_OTA_COUNTER_MAX = 3;

  public static final String GET_LDC_STATUS = "get_ldc_status";
  public static final String SET_LDC_STATUS = "set_ldc_status";

  public static final String GET_RECORDING_DURATION = "value_recording_active_duration";
  public static final String SET_RECORDING_DURATION = "recording_active_duration";
  public static final int VIDEO_RECORDING_OFF_DURATION = 0;
  public static final int VIDEO_RECORDING_DEFAULT_DURATION = 10;


  // use for preference
  public static final int SHARED_PREF_EXPIRE_TIME  = 210 * 1000; // 210 seconds
  public static final String GET_DEVICE_MODE = "get_device_mode";
  public static final String GET_BATTERY_VALUE = "get_battery_value";;
  public static final String GET_REMAINING_TIME = "remaining_time";
  public static final int GET_REMAINING_TIME_OUT = 10;
  public static final int DEFAULT_WAIT_FOR_STAND_BY_MODE = 10;


  public static final int ORBIT_MINIMUM_BATTERY_LEVEL = 30;
  public static final int ORBIT_BATTERY_CHARGING = 1;
  public static final int ORBIT_BATTERY_DISCHARGING = 0;



  public static final String GET_MCU_VERSION = "get_mcu_version";
  public static final String GET_S2L_VERSION = "get_bpi_version";

  public static final String ORBIT_SKIP_OTA_FW_VERSION = "02.10.12";

  public static final int DEVICE_FIRMWARE_UPGRADE_NOT_ALLOW = 0;
  public static final int DEVICE_FIRMWARE_LOW_BATTERY = 1;
  public static final int DEVICE_FIRMWARE_ALLOW = 2;
  public static final String LOCAL_PORT_STR = "80";

  public static final String SHARED_PREF_SEPARATOR = "-";
  public static final String getSharedPrefKey(String deviceRegistrationID, String key)
  {
    return deviceRegistrationID + SHARED_PREF_SEPARATOR + key;
  }
  public static final String setSharedPrefValue(String value)
  {
    return System.currentTimeMillis() + SHARED_PREF_SEPARATOR + value;

  }
  public static final Pair<Long,String> getSharedPrefValue(String value)
  {
      if(value != null)
      {
        String[] fields = value.split(SHARED_PREF_SEPARATOR,2);
        if(fields != null && fields.length >= 2)
        {
          try {
            long lastSystemTime = Long.parseLong(fields[0]);
            return new Pair<>(lastSystemTime, fields[1]);
          }
          catch(Exception e)
          {
            Log.e(TAG,e.getMessage());
          }
        }
      }
      return null;
  }
  public static final boolean isExpire(long lastSystemTime, long interval)
  {
    return (System.currentTimeMillis() > ( lastSystemTime + interval));
  }

  // All elements in setting tab
  public static final String[] groupSettingsAll = new String[] {SETTING_2_KEY_CEILING_MOUNT, SETTING_2_KEY_BRIGHTNESS,
      SETTING_2_KEY_VOLUME, SETTING_2_KEY_CONTRACT, SETTING_2_KEY_NIGHT_VISION};
  // Elements in setting2 of generic
  public static final String[] groupSettingsGeneric = new String[] {SETTING_2_KEY_CEILING_MOUNT, SETTING_2_KEY_BRIGHTNESS,
      SETTING_2_KEY_VOLUME};
  // Elements in setting2 of vtech
  public static final String[] groupSettingsVtech = new String[] {SETTING_2_KEY_CEILING_MOUNT, SETTING_2_KEY_BRIGHTNESS,
      SETTING_2_KEY_VOLUME, SETTING_2_KEY_CONTRACT, SETTING_2_KEY_NIGHT_VISION, SETTING_2_KEY_PARKING};
  // Elements in setting2 of HubbleIR
  public static final String[] groupSettingsHubbleIR = new String[] {SETTING_2_KEY_CEILING_MOUNT, SETTING_2_KEY_BRIGHTNESS,
      SETTING_2_KEY_VOLUME, SETTING_2_KEY_NIGHT_VISION, SETTING_2_KEY_OVERLAY_DATE, SETTING_2_KEY_VIEW_MODE, SETTING_2_KEY_QUALITY_OF_SERVICE};

  public static final String NOTIFY_NOTIFY_DEVICE_REMOVAL = "broadcast_notify_device_removal";

  public static final int CODE_DEVICE_REMOVAL = -99;
  public static final String DEVICE_REG_ID = "DEVICE_REG_ID";

  public static final int DOWNLOAD_FOR_SHARING = 1;
  public static final int DOWNLOAD_FOR_DELETING = 2;
  public static final int DOWNLOAD = 3;
  public static final String PREFS_DOWNLOAD_FOR = "type";
  public static final String PREFS_DOWNLOAD_FILE = "download_sdcard_file";

  public enum CameraModel {
    VC931, VC921, FOCUS66, FOCUS73, FOCUS83, FOCUS86
  }

  public static String add_colon_to_mac(String mac_no_colon) {
    String mac_with_colon = "";
    if (mac_no_colon.length() != 12) {
      // // Log.e("mbp", "add_colon_to_mac-ERROR mac:" + mac_no_colon);
    } else {
      mac_with_colon = mac_no_colon.substring(0, 2) + ":" + mac_no_colon.substring(2, 4) + ":" + mac_no_colon.substring(4, 6) + ":" + mac_no_colon.substring(6, 8) + ":" + mac_no_colon.substring(8, 10) + ":" + mac_no_colon.substring(10);
    }
    return mac_with_colon;
  }

  public static String strip_colon_from_mac(String mac_with_colon) {
    String mac_no_colon = "";
    StringTokenizer stringTok = new StringTokenizer(mac_with_colon, ":");
    if (stringTok.countTokens() != 6) {
      // // Log.e("mbp", "strip_colon_from_mac-ERROR mac:" + mac_with_colon);
    } else {
      while (stringTok.hasMoreElements()) {
        mac_no_colon += stringTok.nextToken();
      }
    }
    return mac_no_colon;
  }

  public static String convertToNoQuotedString(String string) {
    String no_quoted_str = string;
    if (string != null && string.indexOf("\"") == 0 && string.lastIndexOf("\"") == string.length() - 1) {
      no_quoted_str = string.substring(1, string.lastIndexOf("\""));
    }
    return no_quoted_str;
  }

  public static String convertToQuotedString(String string) {
    return "\"" + string + "\"";
  }

  public static String getMacFromRegId(String regId) {
    String res = null;

    int startIdx = 6;
    int endIdx = startIdx + 12;
    try {
      res = regId.substring(startIdx, endIdx);
    } catch (Exception e) {
      Log.e("mbp", Log.getStackTraceString(e));
    }

    return res;
  }

  public static String getModelIdFromRegId(String regId) {
    String res = null;

    int startIdx = 2;
    int endIdx = startIdx + 4;
    try {
      res = regId.substring(startIdx, endIdx);
    } catch (Exception e) {

      // // Log.e("mbp", Log.getStackTraceString(e));
    }

    return res;
  }

  public static boolean shouldEnableTemp(String modelId) {
    if (modelId != null) {
      if (modelId.equalsIgnoreCase(MODEL_ID_FOCUS73)) {
        return false;
      }
    }
    return true;
  }

  public static boolean shouldEnableMic(String deviceId) {
    return !(deviceId.equalsIgnoreCase(MODEL_ID_MBP36N) ||
        deviceId.equalsIgnoreCase(MODEL_ID_MBP33N) ||
        deviceId.equalsIgnoreCase(MODEL_ID_MBP41N) ||
            deviceId.equalsIgnoreCase(MODEL_ID_FOCUS73));

  }

  public static boolean shouldEnablePanTilt(String deviceId) {
    return !(deviceId.equalsIgnoreCase(MODEL_ID_FOCUS66) ||
        deviceId.equalsIgnoreCase(MODEL_ID_MBP33N));

  }

  public static boolean shouldUseNewSetupFlow(String modelId) {
    return true;
  }

  public static boolean shouldSetDurationInSeconds(String modelId, String fwVersion) {
    boolean shouldSet = false;
    shouldSet = (modelId != null && modelId.equalsIgnoreCase(MODEL_ID_FOCUS86)) || Integer.valueOf(fwVersion.replace(".", "")) >= 11646;
    Log.d("mbp", "shouldSetDurationInSeconds: " +
            ", fw version " + fwVersion + ", ret? " + shouldSet);
    return shouldSet;
  }

  public static boolean isSharedCam(String modelId) {
    return modelId.equalsIgnoreCase(MODEL_ID_MBP36N) ||
        modelId.equalsIgnoreCase(MODEL_ID_MBP33N) ||
        modelId.equalsIgnoreCase(MODEL_ID_MBP41N);

  }

  public static boolean shouldCheckFwUpgrade(String modelId, String fw_version) {
    boolean can_check = false;
    //Allow for all models
    if (modelId != null && fw_version != null) {
      can_check = true;
    }
    /*// Port from V2 app, only check fw upgrade for Focus66 with fw version 01.15.11.
    if (modelId != null && fw_version != null) {
      if (modelId.equalsIgnoreCase(MODEL_ID_ORBIT)) {
        can_check = true;
      }
      else if (modelId.equalsIgnoreCase(MODEL_ID_FOCUS66) && fw_version.equalsIgnoreCase("01.15.11")) {
        can_check = true;
      }
    }*/
    return can_check;
  }

  public static String getModelFromId(String modelId) {
    String model = "";
    if (modelId != null) {
      if (modelId.equalsIgnoreCase(MODEL_ID_FOCUS66)) {
        model = MODEL_FOCUS66;
      } else if (modelId.equalsIgnoreCase(MODEL_ID_FOCUS96)) {
        model = MODEL_ID_FOCUS96;
      } else if (modelId.equalsIgnoreCase(MODEL_ID_MBP36N)) {
        model = MODEL_MBP36N;
      } else if (modelId.equalsIgnoreCase(MODEL_ID_MBP83)) {
        model = MODEL_MBP83;
      } else if (modelId.equalsIgnoreCase(MODEL_ID_MBP836)) {
        model = MODEL_MBP836;
      } else if (modelId.equalsIgnoreCase(MODEL_ID_MBP33N)) {
        model = MODEL_MBP33N;
      } else if (modelId.equalsIgnoreCase(MODEL_ID_MBP41N)) {
        model = MODEL_MBP41N;
      } else if (modelId.equalsIgnoreCase(MODEL_ID_FOCUS85)) {
        model = MODEL_FOCUS85;
      } else if (modelId.equalsIgnoreCase(MODEL_ID_FOCUS854)) {
        model = MODEL_FOCUS854;
      } else if (modelId.equalsIgnoreCase(MODEL_ID_FOCUS662)) {
        model = MODEL_ID_FOCUS662;
      } else if (modelId.equalsIgnoreCase(MODEL_ID_FOCUS662S)) {
        model = MODEL_ID_FOCUS662S;
      }
    }

    return model;
  }

  public static boolean isFwVersionOutdated(String modelId, String fw_version) {
    if (modelId == null || fw_version == null) {
      return false;
    }

    boolean isOutdated = false;
    int ver_no_0 = -1;
    int ver_no_1 = -1;
    int ver_no_2 = -1;

    if (modelId.equalsIgnoreCase(MODEL_ID_FOCUS73) || modelId.equalsIgnoreCase(MODEL_ID_FOCUS86)) {
      isOutdated = false;
    } else {
      String[] fw_version_arr = fw_version.split("\\.");
      if (fw_version_arr != null && fw_version_arr.length == 3) {
        try {
          ver_no_0 = Integer.parseInt(fw_version_arr[0]);
          ver_no_1 = Integer.parseInt(fw_version_arr[1]);
          ver_no_2 = Integer.parseInt(fw_version_arr[2]);

          if (ver_no_0 < 1 || (ver_no_0 == 1 && ver_no_1 < 12) || (ver_no_0 == 1 && ver_no_1 == 12 && ver_no_2 < 78)) {
            isOutdated = true;
          }
        } catch (NumberFormatException e) {
          e.printStackTrace();
        }
      }
    }

    return isOutdated;
  }

}
