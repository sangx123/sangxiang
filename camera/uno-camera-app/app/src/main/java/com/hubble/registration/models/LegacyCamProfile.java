package com.hubble.registration.models;

import android.util.Pair;

import com.hubble.devcomm.impl.cvision.DeviceDirectApi;
import com.hubble.registration.PublicDefine;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import base.hubble.database.DeviceProfile;
import base.hubble.devices.DeviceLocation;
import base.hubble.devices.DeviceSetting;
import base.hubble.devices.SerializableDeviceProfile;

//import com.hubble.model.ManagerCameraAvailability;

@Deprecated
public class LegacyCamProfile extends com.discovery.ScanProfile {

  private static final String TAG = "LegacyCamProfile";

  private InetAddress remote_addr;
  private int remoteRtspPort;
  private int remoteRtpVideoPort;
  private int remoteRtpAudioPort;
  private int remoteTalkBackPort;

  /* 20120201: add push-to-talk port */
  private int ptt_port;

  private String version_string;
  private String codec;
  private String firmwareVersion;
  private String model;
  private String modelId;
  private String planId;
  private String registrationId;
  private boolean isUpgrading;
  private String host_ssid = null;

  // TODO: We need this when we burninate this clazz
  transient private DeviceDirectApi restApi = null;

  private ArrayList<byte[]> shortclip;
  private boolean isBound;
  private CamChannel mChannel;
  private String camName;
  private int camId;

  /* 20120105:FROM SERVER: Last access Time and date in human-readable format */
  private String lastCommStatus;
  /*
   * 20120105:FROM SERVER: time between this request and last communication
   * from the device (in minutes)
   */
  private int minutesSinceLastComm;
  private String lastUpdate;

  /* 20111215: basic authentication usr/pass */
  private String basicAuth_usr;
  private String basicAuth_pass;

  /* 20120223: set if melody is being play on this camera */
  private boolean melodyIsOn;

  /* 20120420: set for remote case */
  private int remoteCommMode;

  /* 20120606: set if voxIsOn */
  private boolean voxEnabled;

  private boolean useLocalHttp = false;
  private transient boolean checkedForLocalHttp = false;

  /* 20131221: store path to image in gallery */
  private String galleryImagePath;

  /*
   * 20140609_bhavesh add registeration time
   */
  private String registration_at = "";

  private boolean addViaLAN = false;
  private boolean hasWiFiInfo = true;
  private String mode;
  private String p2pProtocol;
  private int free_trial_quota;
  private int pending_free_trial_days;

  /**
   * Create a CamProfile with the given inet address, port and mac address
   *
   * @param iaddress
   * @param port
   * @param MAC_addr
   */
  public LegacyCamProfile(InetAddress iaddress, int port, String MAC_addr) {
    super(iaddress, port, MAC_addr);
    ptt_port = PublicDefine.DEFAULT_AUDIO_PORT;
    isBound = false;
    mChannel = null;
    shortclip = new ArrayList<byte[]>(PublicDefine.SHORT_CLIP_SIZE);
    camName = null;
    lastCommStatus = "none";
    lastUpdate = null;

    melodyIsOn = false;
    remoteCommMode = PublicDefine.STREAM_MODE_HTTP_LOCAL;
    firmwareVersion = "0";
    codec = PublicDefine.CODEC_MJPEG;
    camId = -1;
    mode = null;
    p2pProtocol = null;
  }

  /**
   * Create a CamProfile with the given inet address, and mac address port
   * will be set to 80 by default.
   *
   * @param iaddress
   * @param MAC_addr
   */
  public LegacyCamProfile(InetAddress iaddress, String MAC_addr) {
    super(iaddress, MAC_addr);
    ptt_port = PublicDefine.DEFAULT_AUDIO_PORT;
    isBound = false;
    mChannel = null;
    shortclip = new ArrayList<byte[]>(PublicDefine.SHORT_CLIP_SIZE);
    camName = null;
    lastCommStatus = "none";
    lastUpdate = null;
    melodyIsOn = false;

    remoteCommMode = PublicDefine.STREAM_MODE_HTTP_LOCAL;
    firmwareVersion = "0";
    codec = PublicDefine.CODEC_MJPEG;
    camId = -1;
    mode = null;
    p2pProtocol = null;
  }

  public static LegacyCamProfile fromDeviceProfile(DeviceProfile profile) throws UnknownHostException {
    return new LegacyCamProfile(
        InetAddress.getByName(profile.getDeviceLocation().getLocalIp()),
        profile.getMacAddress()
    );
  }

  public DeviceProfile toDeviceProfile() {
    String localIP = inet_addr.getHostAddress();
    // // Log.d(TAG, "addr is null?" + (local == null));
    SerializableDeviceProfile serializableDeviceProfile = new SerializableDeviceProfile(
        camId,
        camName,
        registrationId,
        get_MAC(),
        0,
        isReachableInRemote(), // is_available
        "", // snaps url
        "", //snaps modified at
        Integer.parseInt(modelId),
        mode, //remoteCommMode + "", //mode
        firmwareVersion,
        0, // firmware status
        planId != null ? planId : "freemium",
        "", // plan changed at
        "", // last accessed date
        false, //Deactivate
        "", // targetDeactivateDate
        0, // upnp usage
        0, // stun usage
        0, // relay usage
        0, // upnp count
        0, // stun count/
        0, //
        false,
        "", // relay usage reset date
        0, // latest relay usage
        "", // created date
        lastUpdate != null ? lastUpdate : "", // updated at
        registration_at,
        0, // user id
        host_ssid != null ? host_ssid : "",
        "", // host router
        new DeviceLocation(
            "",
            remoteRtspPort + "",
            remoteRtpAudioPort + "",
            remoteRtpVideoPort + "",
            remoteTalkBackPort + "",
            localIP + "",
            port + "",
            "",
            "",
            ""
        ),
        new ArrayList<DeviceSetting>(),
        null,
        null,
        null,
        null,
        free_trial_quota,
            pending_free_trial_days
    );

    return new DeviceProfile(serializableDeviceProfile);
  }

  public Pair<String, Object> sendDirectCommand(String apiKey, String command, String value, String setup) {
    if (this.restApi == null) {
      restApi = new DeviceDirectApi(this.toDeviceProfile(), apiKey);
    }
    return restApi.sendCommand(command, value, setup, this.isUseLocalHttp());
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getHostSsid() {
    return host_ssid;
  }

  public void setHostSsid(String host_ssid) {
    this.host_ssid = host_ssid;
  }

  public String getPlanId() {
    return planId;
  }

  public void setPlanId(String planId) {
    this.planId = planId;
  }

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }

  public String getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(String lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
    model = PublicDefine.getModelFromId(modelId);
  }

  public int getCamId() {
    return camId;
  }

  public void setCamId(int camId) {
    this.camId = camId;
  }

  /**
   * @return the remote_addr
   */
  public InetAddress getRemote_addr() {
    return remote_addr;
  }

  /**
   * @param remote_addr the remote_addr to set
   */
  public void setRemote_addr(InetAddress remote_addr) {
    this.remote_addr = remote_addr;
  }

  /**
   * @return the remoteRtspPort
   */
  public int getRemoteRtspPort() {
    return remoteRtspPort;
  }

  /**
   * @param remoteRtspPort the remoteRtspPort to set
   */
  public void setRemoteRtspPort(int remoteRtspPort) {
    this.remoteRtspPort = remoteRtspPort;
  }

  /**
   * @return the remoteRtpVideoPort
   */
  public int getRemoteRtpVideoPort() {
    return remoteRtpVideoPort;
  }

  /**
   * @param remoteRtpVideoPort the remoteRtpVideoPort to set
   */
  public void setRemoteRtpVideoPort(int remoteRtpVideoPort) {
    this.remoteRtpVideoPort = remoteRtpVideoPort;
  }

  /**
   * @return the remoteRtpAudioPort
   */
  public int getRemoteRtpAudioPort() {
    return remoteRtpAudioPort;
  }

  /**
   * @param remoteRtpAudioPort the remoteRtpAudioPort to set
   */
  public void setRemoteRtpAudioPort(int remoteRtpAudioPort) {
    this.remoteRtpAudioPort = remoteRtpAudioPort;
  }

  /**
   * @return the remoteTalkBackPort
   */
  public int getRemoteTalkBackPort() {
    return remoteTalkBackPort;
  }

  /**
   * @param remoteTalkBackPort the remoteTalkBackPort to set
   */
  public void setRemoteTalkBackPort(int remoteTalkBackPort) {
    this.remoteTalkBackPort = remoteTalkBackPort;
  }

  /**
   * @return the model
   */
  public String getModel() {
    return model;
  }

  /**
   * @param model the model to set
   */
  public void setModel(String model) {
    this.model = model;
  }

  /**
   * @return the firmwareVersion
   */
  public String getFirmwareVersion() {
    return firmwareVersion;
  }

  /**
   * @param firmwareVersion the firmwareVersion to set
   */
  public void setFirmwareVersion(String firmwareVersion) {
    if (firmwareVersion == null) {
      this.firmwareVersion = "0";
    } else {
      this.firmwareVersion = firmwareVersion;
    }
  }

  /**
   * @return the codec
   */
  public String getCodec() {
    return codec;
  }

  /**
   * @param codec the codec to set
   */
  public void setCodec(String codec) {
    if (codec == null) {
      this.codec = PublicDefine.CODEC_MJPEG;
    } else {
      this.codec = codec;
    }
  }

  public boolean isVoxEnabled() {
    return voxEnabled;
  }

  public void setVoxEnabled(boolean voxIsEnabled) {
    this.voxEnabled = voxIsEnabled;
  }

  public void setName(String name) {
    camName = name;
  }

  public String getName() {
    if (camName != null) {
      return camName;
    } else {
      return "";
    }
  }

  public int getRemoteCommMode() {
    return remoteCommMode;
  }

  public void setRemoteCommMode(int remoteCommMode) {
    this.remoteCommMode = remoteCommMode;
  }

  public void setLastCommStatus(String lastComm) {
    lastCommStatus = lastComm;
  }

  public String getLastCommStatus() {
    return lastCommStatus;
  }

  public String getBasicAuth_usr() {
    return basicAuth_usr;
  }

  public void setBasicAuth_usr(String usr) {
    basicAuth_usr = usr;
  }

  public String getBasicAuth_pass() {
    return basicAuth_pass;
  }

  public void setBasicAuth_pass(String pass) {
    basicAuth_pass = pass;
  }

  public int get_ptt_port() {
    return ptt_port;
  }

  public boolean isBound() {
    return isBound;
  }

  public void bind(boolean sel) {
    isBound = sel;
  }

  public void setPTTPort(int port) {
    this.ptt_port = port;

  }

  public void setChannel(CamChannel chan) {
    mChannel = chan;
  }

  public boolean equals(LegacyCamProfile o) {

    if (o == null || o.get_MAC() == null) {
      return false;
    }

    return this.MAC_addr.equalsIgnoreCase(o.get_MAC());

  }

  public ArrayList<byte[]> getShortClip() {
    return shortclip;
  }

  public void setShortClip(ArrayList<byte[]> snapshots) {
    shortclip = snapshots;
  }

  public String toString() {
    // return inet_addr.toString()+ "@"+ MAC_addr;
    return MAC_addr;
  }

  public void unSelect() {
    isBound = false;
    if (mChannel != null) {
      mChannel.reset();
      mChannel = null;
    }
  }

  /**
   * 20120223: melodyIsOn is the lullaby status. This can only be set in two
   * places: - EntryActivity.updateMelodyIndex() called by VideoStreamer
   * during connection setup - ScanForCamera.updateMelodyIndex(CamProfile cam)
   * called when scanning for camera in local network.
   * <p/>
   * The update order should be : scanforcamera -> updateMelodyIndex() Once
   * restoredataSession is called, this value is passed to the newly restore
   * data as it is not SAVED in SetupData
   * <p/>
   * in Remote Mode, The update order should be just updateMelodyIndex()
   * because scanforcamera should not find this particular camera
   *
   * @return public boolean isMelodyIsOn() { return melodyIsOn; }
   * <p/>
   * public void setMelodyIsOn(boolean melodyIsOn) { this.melodyIsOn =
   * melodyIsOn; }
   */


  private boolean enable;

  private void setSelected(boolean select) {
    enable = select;
  }

  public boolean isSelected() {
    return enable;
  }

  public void setImageLink(String selectedImagePath) {
    galleryImagePath = selectedImagePath;
  }

  public String getImageLink() {
    return galleryImagePath;
  }

  public boolean isUpgrading() {
    return isUpgrading;
  }

  public void setUpgrading(boolean isUpgrading) {
    this.isUpgrading = isUpgrading;
  }

  public String getRegistration_at() {
    return registration_at;
  }

  public void setRegistration_at(String registration_at) {
    this.registration_at = registration_at;
  }

  public boolean isAddViaLAN() {
    return addViaLAN;
  }

  public void setAddViaLAN(boolean addViaLAN) {
    this.addViaLAN = addViaLAN;
  }

  public boolean isHasWiFiInfo() {
    return hasWiFiInfo;
  }

  public void setHasWiFiInfo(boolean hasWiFiInfo) {
    this.hasWiFiInfo = hasWiFiInfo;
  }

  public boolean isUseLocalHttp() {
    if (!hasCheckedForLocal) {
      checkForLocalHttp("");
    }
    return useLocalHttp;
  }

  public void setUseLocalHttp(boolean useLocalHttp) {
    this.useLocalHttp = useLocalHttp;
  }

  transient boolean hasCheckedForLocal = false;

  public void checkForLocalHttp(String apiKey) {
  }


}