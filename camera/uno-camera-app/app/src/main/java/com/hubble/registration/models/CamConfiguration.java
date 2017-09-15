package com.hubble.registration.models;

import android.net.wifi.WifiConfiguration;

import com.hubble.registration.PublicDefine;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.TimeZone;

import base.hubble.PublicDefineGlob;

public class CamConfiguration {

  private static final String TAG = "CamConfiguration";
  private String device_ssid;
  //private String max_cam;
  private String ssid, security_type, pass_string_no_quotes;
  private String key_index, auth_method;
  private String address_mode;
  private String static_ip_gw, netmask, static_ip;
  private String usr_name, pass_wd;
  private WifiConfiguration wc;
  private Vector<String> device_bssid;
  private LegacyCamProfile[] cam_profiles;
  private CamChannel[] cam_channels;
  private Vector<String> skipped_device_bssid;
  private String master_key;
  private String user_api_key;


  //Configuration Paramater
  protected long time_to_finish_scanning ;


  // If we need this much data, perhaps a parameter object should be used.
  public CamConfiguration(
      String ssid,
      String security_type,
      String pass_no_quotes,
      String key_idx,
      String auth_method,
      String address_mode,
      String gw,
      String nm,
      String ip,
      String usrName,
      String pwd,
      String device_ssid,
      //String max_cam,
      String api_key) {

    this.ssid = ssid;
    this.security_type = security_type;
    this.pass_string_no_quotes = pass_no_quotes;
    this.key_index = key_idx;
    this.auth_method = auth_method;
    this.address_mode = address_mode;
    this.static_ip_gw = gw;
    this.static_ip = ip;
    this.netmask = nm;
    this.usr_name = (usrName != null) ? usrName : "";
    this.pass_wd = (pwd != null) ? pwd : "";
    this.device_ssid = device_ssid;
    //this.max_cam = max_cam;
    this.user_api_key = api_key;

    device_bssid = null;
    cam_profiles = null;
    cam_channels = null;
    this.master_key = null;
    skipped_device_bssid = new Vector<String>();

    time_to_finish_scanning = 3 * 60 * 1000;

  }

  public long getTimeToFinishScanning()
  {
    return time_to_finish_scanning;
  }



  /**
   * Get alert Default setting array,
   * this Array will contain all the commands that need to be sent to camera during setup.
   *
   * @param modelId - Vtech camera can have multiple models which may or may not have some of the default settitngs
   * @return array of strings each of which contains a single command to be sent to camera.
   * each command should have the form:
   * "action=command&command=[command to send to camera]"
   */
  public List<String> getDefaultAlertSettings(String modelId, String fwVersion) {
    String tempHighOnCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "set_temp_hi_enable&value=0";
    String tempLowOnCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "set_temp_lo_enable&value=0";
    String soundDetectionOnCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "vox_disable";

    // 20160519 binh AA-1791: fix bug MD is off after setup on 877
    String motionAreaCommand;
    if ("0877".equals(modelId)) {
      motionAreaCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "start_vda&value=bsd";
    } else {
      motionAreaCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "set_motion_area&value=&grid=1x1&zone=00";
    }

    String cooloffDuration, activeDuration;
    if (PublicDefine.shouldSetDurationInSeconds(modelId, fwVersion)) {
      cooloffDuration = "120";
      activeDuration = "90";
    } else {
      cooloffDuration = "0";
      activeDuration = "0";
    }
    String recordCoolOffDurationCommand = PublicDefineGlob.BM_HTTP_CMD_PART +
        "recording_cooloff_duration&value=" + cooloffDuration;
    String recordActiveDurationCommand = PublicDefineGlob.BM_HTTP_CMD_PART +
        "recording_active_duration&value=" + activeDuration;

    //AA-1888: Support City Timezone for DST
    TimeZone tz = TimeZone.getDefault();
    String setCityTimezoneCommand = PublicDefineGlob.BM_HTTP_CMD_PART +
            "set_city_timezone&value=" + tz.getID();

    List<String> default_command = new ArrayList<>();
    default_command.add(motionAreaCommand);
    if (PublicDefine.shouldEnableTemp(modelId)) {
      default_command.add(tempHighOnCommand);
      default_command.add(tempLowOnCommand);
    }
    if (PublicDefine.shouldEnableMic(modelId)) {
      default_command.add(soundDetectionOnCommand);
    }
    default_command.add(recordCoolOffDurationCommand);
    default_command.add(recordActiveDurationCommand);

    //AA-1888: Support City Timezone for DST
    default_command.add(setCityTimezoneCommand);

    return default_command;
  }



  public void setMasterKey(String mkey) {
    this.master_key = mkey;
  }

  public String getMasterKey() {
    return this.master_key;
  }

  public void setCamProfiles(LegacyCamProfile[] cp) {
    this.cam_profiles = cp;
  }

  public void setCamChannels(CamChannel[] cp) {
    this.cam_channels = cp;
  }

  public LegacyCamProfile[] getCamProfiles() {
    return this.cam_profiles;
  }

  public CamChannel[] getCamChannels() {
    return this.cam_channels;
  }

  public Vector<String> getSkippedDeviceList() {
    return this.skipped_device_bssid;
  }


  public void setDeviceList(Vector<String> list) {
    device_bssid = list;
  }

  public Vector<String> getDeviceBSSIDList() {
    return device_bssid;
  }

  public void setWifiConf(WifiConfiguration wc) {
    this.wc = wc;
  }

  //  "WPA/WPA2", "WEP", "OPEN"
  public String security_type() {
    return security_type;
  }

  public String device_ssid() {
    return device_ssid;
  }

////  public String max_cam() {
//    return max_cam;
//  }

  public String ssid() {
    return ssid;
  }

  public String pass_string() {
    return pass_string_no_quotes;
  }

  public String key_index() {
    return key_index;
  }

  public String auth_method() {
    return auth_method;
  }

  public String address_mode() {
    return address_mode;
  }

  public String static_ip_gw() {
    return static_ip_gw;
  }

  public String netmask() {
    return netmask;
  }

  public String static_ip() {
    return static_ip;
  }

  public String getHttpUsr() {
    return usr_name;
  }

  public String getHttpPass() {
    return pass_wd;
  }

  public WifiConfiguration wc() {
    return wc;
  }

  public String getUser_api_key() {
    return user_api_key;
  }

  public void setUser_api_key(String user_api_key) {
    this.user_api_key = user_api_key;
  }

  public String build_setup_request() {
    String setup_request = null;
    setup_request = build_setup_core_request();
    return setup_request;
  }

  public String build_setup_core_request() {
    String setup_request = null;

    // setup in infra mode
    String wifi_mode = "1";

    // channel in adhoc mode
    String adhoc_chan = "00";
    String auth_mode = null, key_index = null;

    if (security_type.equalsIgnoreCase("WEP")) {
      /* use Wep */
      auth_mode = (auth_method.equalsIgnoreCase("Open")) ? "0" : "1";

      key_index = String.format("%d", Integer.parseInt(this.key_index) - 1);
    } else if (security_type.equalsIgnoreCase("OPEN")) {
      auth_mode = "0";
      key_index = "0";
    } else {
      /* use WPA-PSK */
      auth_mode = "2";
      key_index = "0";
    }

    // HACK: magic number 0 == DHCP
    String address_mode = "0";
    String ssid_len = String.format("%03d", ssid.getBytes().length);

    String sec_key_len = String.format("%02d", pass_string_no_quotes.length());

    // TODO: support for static ip might be set here
    // HACK: Dont use static ip so static ip len = 0
    String static_ip_len = "00";

    String static_ip_netmask = "00";
    String static_ip_gw_len = "00";
    String port = "0";
    String usr_name_len = String.format("%02d", this.usr_name.length());
    String passwd_len = String.format("%02d", this.pass_wd.length());

    // encode setup data in URL
    String setup_value = wifi_mode + adhoc_chan + auth_mode + key_index +
        address_mode + ssid_len + sec_key_len + static_ip_len +
        static_ip_netmask + static_ip_gw_len + port + usr_name_len + passwd_len +
        this.ssid + this.pass_string_no_quotes + this.usr_name + this.pass_wd;

    // // Log.d("mbp", "Encode setup data");
    try {
      setup_value = URLEncoder.encode(setup_value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // // Log.e(TAG, Log.getStackTraceString(e));
    }

    // setup_request = "/?action=command&command=setup_wireless_save&setup=" +
    // wifi_mode + adhoc_chan + auth_mode + key_index +
    // address_mode + ssid_len + sec_key_len + static_ip_len +
    // static_ip_netmask +  static_ip_gw_len + port +usr_name_len + passwd_len +
    // this.ssid+ this.pass_string_no_quotes + this.usr_name + this.pass_wd ;
    setup_request = "/?action=command&command=setup_wireless_save&setup=" + setup_value;

    return setup_request;
  }

}
