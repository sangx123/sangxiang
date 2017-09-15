package com.hubble.registration;

import android.content.Context;
import android.util.Log;

import com.hubble.HubbleApplication;
import com.hubble.registration.models.CamChannel;
import com.hubble.registration.models.LegacyCamProfile;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;

import base.hubble.meapi.Device;
import base.hubble.meapi.PublicDefines;
import base.hubble.meapi.device.CamListResponse;
import base.hubble.meapi.device.CameraInfo;
import base.hubble.meapi.device.DeviceLocation;


public class UserAccount {
  private static final int REG_ID_LEN = 26;
  private static final long FIVE_MINUTES = 5 * 60 * 1000;
  private static final String TAG = "UserAccount";
  private String sessionToken;
  private SetupDataCache savedData;
  private SSLContext ssl_context;
  private File externalFilesDir;
  private Context mContext;

  public UserAccount(String sessionToken, File externalFilesDir, SSLContext ssl_context, Context mContext) throws Exception {
    this.sessionToken = sessionToken;
    this.mContext = mContext;
    this.externalFilesDir = externalFilesDir;
    this.ssl_context = ssl_context;
    savedData = new SetupDataCache();
    try {
      if (externalFilesDir != null && savedData.restore_session_data(externalFilesDir)) {

      } else {
        // // Log.e("mbp", "Can't restore session data sth is wrong");
      }
    } catch (Exception e) {
      throw e;
    }
  }

  public void sync_user_data() throws IOException {
    LegacyCamProfile[] online_list = query_online_cammera_list();

    // register if it's not iHome Phone
    if (online_list != null) {

			/* Purging invalid alert if any */
      ArrayList<String> cameraMacs = new ArrayList<String>();
      for (LegacyCamProfile anOnline_list : online_list) {
        cameraMacs.add(anOnline_list.get_MAC());
      }
    }
    sync_online_and_offline_data(online_list);
  }

  private LegacyCamProfile[] query_online_cammera_list() throws IOException {
    LegacyCamProfile[] list = null;
    boolean server_error = false;
    int retry = 1;
    while (retry > 0) {

      try {
        PublicDefines.setHttpTimeout(60000);
        CamListResponse camlist_res = Device.getOwnCamList(sessionToken);
        if (camlist_res != null) {
          // // Log.d("mbp", "Get cam list response code: " + camlist_res.getStatus());
          if (camlist_res.getStatus() == HttpURLConnection.HTTP_OK) {
            CameraInfo[] caminfos = camlist_res.getCamList();
            try {
              /*
               * 20130110: hoang: fix for new get cam list query
							 * "command=cam_list4"
							 */
              // list = parse_cam_list_temp_new(inputStream);
              list = parse_cam_list_temp_new2(caminfos);
            } catch (NumberFormatException nfe) {
              throw new IOException("Error parsing input stream");
            }

            break;
          } else if (camlist_res.getStatus() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            retry--;
            // after last try we throw...
            if (retry == 0) {
              server_error = true;
            }
          } else {
            retry--;
          }
        } else {
          retry--;
          // after last try we throw...
          if (retry == 0) {
            server_error = true;
          }
        }

      } catch (MalformedURLException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
        retry--;
        // Should not happen
      } catch (IOException ioe) {
        retry--;
        // after last try we throw...
        if (retry == 0) {
          // // Log.d("mbp", "throw big ioe now ");
          throw ioe;
        }
      }

    }

    if (server_error) {
      // // Log.d("mbp", "throw big ioe now ");
      throw new IOException("Server internal error");
    }

    return list;
  }

  public boolean queryCameraAvailableOnline(String mac) {
    boolean isOn = false;
    LegacyCamProfile[] online_list = null;
    LegacyCamProfile cp;
    try {
      online_list = query_online_cammera_list();
      if (online_list != null) {
        for (LegacyCamProfile anOnline_list : online_list) {
          cp = anOnline_list;
          if (cp.get_MAC() != null && cp.get_MAC().equalsIgnoreCase(mac)) {
            isOn = cp.isReachableInRemote();
            break;
          }
        }
      }
    } catch (IOException e) {
      Log.e(TAG, Log.getStackTraceString(e));
    }
    return isOn;
  }

  public String getRegistrationAt(String mac) {
    String registrationAt = "";
    LegacyCamProfile[] online_list = null;
    LegacyCamProfile cp;
    try {
      online_list = query_online_cammera_list();
      if (online_list != null) {
        for (LegacyCamProfile anOnline_list : online_list) {
          cp = anOnline_list;
          if (cp.get_MAC() != null && cp.get_MAC().equalsIgnoreCase(mac)) {
            registrationAt = cp.getRegistration_at();
            break;
          }
        }
      }
    } catch (IOException e) {
    }
    return registrationAt;
  }

  private LegacyCamProfile[] parse_cam_list_temp_new2(CameraInfo[] caminfos) throws IOException, NumberFormatException {
    if (caminfos == null) {
      return null;
    }

    LegacyCamProfile[] online_list = new LegacyCamProfile[caminfos.length];

    for (int i = 0; i < caminfos.length; i++) {
      // get camera name
      String camName = caminfos[i].getName();

      // get mac address
      String registrationId = caminfos[i].getRegistration_id();
      if (registrationId == null || registrationId.length() != REG_ID_LEN) {
        registrationId = "00000000000000000000000000";
      }

      String MAC_addr = PublicDefine.getMacFromRegId(registrationId);
      MAC_addr = PublicDefine.add_colon_to_mac(MAC_addr);

      // get fw version
      // DeviceFirmware camFwVersion_obj =
      // caminfos[i].getDevice_firmware();
      String camFwVersion = null;
      camFwVersion = caminfos[i].getFirmware_version();

      String planId = null;
      planId = caminfos[i].getPlan_id();

      // get host_ssid
      String host_ssid = caminfos[i].getHost_ssid();

      // get model id
      String camModel = null;
      camModel = PublicDefine.getModelIdFromRegId(registrationId);

      int camId = -1;
      camId = caminfos[i].getId();

      String mode = null;
      mode = caminfos[i].getMode();

      Date lastDateUpdate = null;
      lastDateUpdate = caminfos[i].getUpdated_at();
      TimeZone tz = TimeZone.getDefault();
      // String dateFormat = "hh:mm a, d'" +
      // Util.getDayOfMonthSuffix(lastDateUpdate.getDate()) +
      // "' MMMM yyyy";

      int clockMode = HubbleApplication.AppConfig.getInt(PublicDefine.PREFS_CLOCK_MODE, PublicDefine.CLOCK_MODE_12H);
      String dateFormat;
      if (clockMode == PublicDefine.CLOCK_MODE_12H) {
        dateFormat = "h:mm a, MMM d, yyyy";
      } else {
        dateFormat = "H:mm, MMM d, yyyy";
      }
      SimpleDateFormat destFormat = new SimpleDateFormat(dateFormat, Locale.US);
      destFormat.setTimeZone(tz);
      String lastUpdate = destFormat.format(lastDateUpdate);

      // get is_available value
      boolean isAvailable = false;
      isAvailable = caminfos[i].isIs_available();

      InetAddress remote_addr = null;
      int remoteRtspPort = -1;
      int remoteRtpVideoPort = -1;
      int remoteRtpAudioPort = -1;
      int remoteTalkBackPort = -1;
      InetAddress local_addr = null;
      int localRtspPort = -1;
      int localRtpVideoPort = -1;
      int localRtpAudioPort = -1;
      int localTalkBackPort = -1;
      String imageLink = null;

      // dev_loc now always null
      DeviceLocation dev_loc = caminfos[i].getDevice_location();
      if (dev_loc != null) {
        // get remote addr
        String remote_ip = dev_loc.getRemoteIP();
        if (remote_ip != null && !remote_ip.equalsIgnoreCase("null") && !remote_ip.equalsIgnoreCase("")) {
          // remote_addr = InetAddress.getByName(remote_ip);
        }

        // get remote RTSP port
        String remoteRtspPort_str = dev_loc.getRemotePort1();
        if (remoteRtspPort_str != null) {
          remoteRtspPort = Integer.parseInt(remoteRtspPort_str);
        }

        // get remote RTP Video port
        String remoteRtpVideoPort_str = dev_loc.getRemotePort2();
        if (remoteRtpVideoPort_str != null) {
          remoteRtpVideoPort = Integer.parseInt(remoteRtpVideoPort_str);
        }

        // get remote RTP Audio port
        String remoteRtpAudioPort_str = dev_loc.getRemotePort3();
        if (remoteRtpAudioPort_str != null) {
          remoteRtpAudioPort = Integer.parseInt(remoteRtpAudioPort_str);
        }

        // get remote TalkBack port
        String remoteTalkBackPort_str = dev_loc.getRemotePort4();
        if (remoteTalkBackPort_str != null) {
          remoteTalkBackPort = Integer.parseInt(remoteTalkBackPort_str);
        }

        // get local addr
        String local_ip = dev_loc.getLocalIP();
        if (local_ip != null && !local_ip.equalsIgnoreCase("null") && !local_ip.equalsIgnoreCase("")) {
          local_addr = InetAddress.getByName(local_ip);
        }

        // get local RTSP port
        String localRtspPort_str = dev_loc.getLocalPort1();
        if (localRtspPort_str != null) {
          localRtspPort = Integer.parseInt(localRtspPort_str);
        }

        // get local RTP Video port
        String localRtpVideoPort_str = dev_loc.getLocalPort2();
        if (localRtpVideoPort_str != null) {
          localRtpAudioPort = Integer.parseInt(localRtpVideoPort_str);
        }

        // get local RTP Audio port
        String localRtpAudioPort_str = dev_loc.getLocalPort3();
        if (localRtpAudioPort_str != null) {
          localRtpAudioPort = Integer.parseInt(localRtpAudioPort_str);
        }

        // get local TalkBack port
        String localTalkBackPort_str = dev_loc.getLocalPort4();
        if (localTalkBackPort_str != null) {
          localTalkBackPort = Integer.parseInt(localTalkBackPort_str);
        }
      }

      // get local addr
      String local_ip = caminfos[i].getLocal_ip();
      if (local_ip != null && !local_ip.equalsIgnoreCase("null") && !local_ip.equalsIgnoreCase("")) {
        local_addr = InetAddress.getByName(local_ip);
      }

      // get registration at feild
      String registration_at = caminfos[i].getRegistration_at();
      imageLink = caminfos[i].getSnaps_url();

      online_list[i] = new LegacyCamProfile(local_addr, MAC_addr);
      online_list[i].setRegistrationId(registrationId);
      online_list[i].setName(camName);
      online_list[i].setFirmwareVersion(camFwVersion);
      online_list[i].setPlanId(planId);
      online_list[i].setModelId(camModel);
      online_list[i].setCamId(camId);
      online_list[i].setMode(mode);
      online_list[i].setReachableInRemote(isAvailable);
      online_list[i].setLastUpdate(lastUpdate);
      online_list[i].setHostSsid(host_ssid);

      if (imageLink != null && !imageLink.contains("hubble.png?AWSAccessKeyId")) {
        online_list[i].setImageLink(imageLink);
      }


      // set camera remote info
      // online_list[i].setRemote_addr(remote_addr);
      online_list[i].setRemoteRtspPort(remoteRtspPort);
      online_list[i].setRemoteRtpVideoPort(remoteRtpVideoPort);
      online_list[i].setRemoteRtpAudioPort(remoteRtpAudioPort);
      online_list[i].setRemoteTalkBackPort(remoteTalkBackPort);
      /*
			 * 20140611_bhavesh_bug_HCD193
			 */
      online_list[i].setRegistration_at(registration_at);

      if (caminfos[i].getFirmware_time() != null) {
        TimeZone timezone = TimeZone.getTimeZone("UTC");
        Calendar c = Calendar.getInstance(timezone);
        long time_stamp = c.getTime().getTime() - caminfos[i].getFirmware_time().getTime();

        // Log.i("mbp","Time left from update: " + (time_stamp/1000) +
        // " seconds.");

        if (time_stamp < FIVE_MINUTES) {

          if (caminfos[i].getFirmware_status() == 1) {
            online_list[i].setUpgrading(true);
          } else {
            online_list[i].setUpgrading(false);
          }
        } else {
          online_list[i].setUpgrading(false);
        }

      } else {
        online_list[i].setUpgrading(false);
      }

    }

    return online_list;
  }


  /**
   * Sync online and offline list Offline list may be shrinked or extended
   * Offline list will be saved after the synchronization
   *
   * @param online_list - online list of CamProfile, query from Server
   */

  private void sync_online_and_offline_data(LegacyCamProfile[] online_list) {

    LegacyCamProfile[] offline_list = savedData.get_CamProfiles();

    if (online_list == null) {
      // If online_list == null -> user has no cam online --remove all cam
      // offline too
      savedData.set_CamProfiles(new LegacyCamProfile[0]);

      CamChannel[] chs = new CamChannel[4];
      for (int i = 0; i < chs.length; i++) {
        chs[i] = new CamChannel();
      }
      savedData.set_Channels(chs);

      savedData.set_AccessMode(SetupDataCache.ACCESS_VIA_LAN);
      savedData.set_SSID("NA");// will not be used

      savedData.save_session_data(externalFilesDir);

      return;
    }


    if (offline_list == null) {
      savedData.set_CamProfiles(online_list);
    } else {
      // SYNC online/offline list
      offline_list = online_list;
      // update the reference .. yes,needed
      savedData.set_CamProfiles(offline_list);
    }

    if ((savedData.get_AccessMode() == -1) || (savedData.get_Channels() == null)) {
      // Looks like there is no saved data, create one
      savedData.set_AccessMode(SetupDataCache.ACCESS_VIA_LAN);
      savedData.set_SSID("NA");// will not be used

      LegacyCamProfile[] cps = savedData.get_CamProfiles();
      CamChannel[] chs = new CamChannel[4];
      for (int i = 0; i < 4; i++) {
        chs[i] = new CamChannel();

        if ((i < cps.length) && (cps[i] != null)) {
          cps[i].bind(true);
          chs[i].setCamProfile(cps[i]);
          cps[i].setChannel(chs[i]);
        }
      }
      savedData.set_Channels(chs);

    } else {

      LegacyCamProfile[] cps = savedData.get_CamProfiles();
      CamChannel[] chs = savedData.get_Channels();

			/* Update the channel list */
      for (CamChannel ch : chs) {
        // reset the channel
        if (ch != null) {
          ch.reset();
        }
      }
      for (LegacyCamProfile cp : cps) {

        if (cp != null) {
          for (CamChannel ch : chs) {
            if (ch != null) {
              if (ch.getState() == CamChannel.CONFIGURE_STATUS_NOT_ASSIGNED) {
                cp.bind(true);
                ch.setCamProfile(cp);
                cp.setChannel(ch);
                break;
              }
            }
          }
        }
      }
    }

		/* save data for offline used */
    savedData.save_session_data(externalFilesDir);
  }

}
