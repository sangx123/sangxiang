package com.hubble.registration.models;

import android.util.Log;

import com.hubble.registration.PublicDefine;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import base.hubble.PublicDefineGlob;

/**
 * Created by admin on 11/2/16.
 */
public class VtechCamConfiguration extends CamConfiguration {
  private static final String TAG = "VtechCamConfiguration";

  public VtechCamConfiguration(
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
      String api_key) {
    super(ssid, security_type, pass_no_quotes, key_idx, auth_method, address_mode,
        gw, nm, ip, usrName, pwd, device_ssid, api_key);


    //Vtech camera needs more time
    time_to_finish_scanning = 7 * 60 * 1000;

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
  public List<String> getDefaultAlertSettings(String modelId,String fwver) {
    String motionAreaCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "set_motion_area&value=&grid=1x1&zone=";
    String tempHighOnCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "set_temp_hi_enable&value=0";
    String tempLowOnCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "set_temp_lo_enable&value=0";
    String soundDetectionOnCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "vox_disable";
    String recordCoolOffDurationCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "recording_cooloff_duration&value=120";
    String recordActiveDurationCommand = PublicDefineGlob.BM_HTTP_CMD_PART + "recording_active_duration&value=90";


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

    return default_command;
  }
}

