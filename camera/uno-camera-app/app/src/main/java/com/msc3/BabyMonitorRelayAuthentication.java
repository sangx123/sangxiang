/**
 *
 */
package com.msc3;

import com.hubble.registration.models.BabyMonitorAuthentication;

/**
 * @author NxComm
 */
public class BabyMonitorRelayAuthentication extends BabyMonitorAuthentication {

  private String channelId;
  private String regId;
  private String userName;
  private String userPass;
  private int udtLocalPort;

  public BabyMonitorRelayAuthentication(String ip, String port, String chann, String mac, String sskey, String relayUrl, int udtPort, String usrName, String usrPass) {
    device_ip = ip;
    device_port = Integer.parseInt(port);
    channelId = chann;
    regId = mac;
    session_key = sskey;
    stream_url = relayUrl;
    udtLocalPort = udtPort;
    userName = usrName;
    userPass = usrPass;
  }

  @Override
  public String getIP() {
    return device_ip;
  }

  @Override
  public int getPort() {
    return device_port;
  }

  @Override
  public String getSSKey() {
    return session_key;
  }

  @Override
  public int getLocalPort() {
    return getUdtLocalPort();
  }

  public int getUdtLocalPort() {
    return udtLocalPort;
  }

  @Override
  public String toString() {
    return null;
  }

  /**
   * @return the channelId
   */
  public String getChannelID() {
    return channelId;
  }

  /**
   * @param channelId the channelId to set
   */
  public void setChannelID(String channelId) {
    this.channelId = channelId;
  }

  /**
   * @return the macAddress
   */
  public String getRestrationId() {
    return regId;
  }

  /**
   * @param macAddress the macAddress to set
   */
  public void setRestrationId(String regId) {
    this.regId = regId;
  }


  @Override
  public String getStreamUrl() {
    return stream_url;
  }

  /**
   * @return the userName
   */
  public String getUser() {
    return userName;
  }

  /**
   * @param userName the userName to set
   */
  public void setUser(String userName) {
    this.userName = userName;
  }

  /**
   * @return the userPass
   */
  public String getPass() {
    return userPass;
  }

  /**
   * @param userPass the userPass to set
   */
  public void setPass(String userPass) {
    this.userPass = userPass;
  }


}
