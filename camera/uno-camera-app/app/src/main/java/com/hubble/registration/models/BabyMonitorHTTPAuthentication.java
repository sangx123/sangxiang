package com.hubble.registration.models;

/**
 * @author phung
 *         <p/>
 *         BabyMonitorAuthentication - simple object to store authentication information
 *         - ip:port
 *         - session_key
 */
public class BabyMonitorHTTPAuthentication extends BabyMonitorAuthentication {

  private String macAddress;

  public BabyMonitorHTTPAuthentication(String ip, String port, String ss_key, String mac) {
    device_ip = ip;
    device_port = Integer.parseInt(port);
    session_key = ss_key;
    stream_url = null;
    macAddress = mac;
  }


  public String getIP() {
    return device_ip;
  }

  public int getPort() {
    return device_port;
  }

  public String getSSKey() {
    return session_key;
  }


  public int getLocalPort() {
    return 0;
  }

  @Override
  public String getStreamUrl() {
    return stream_url;
  }

  /**
   * @return the macAddress
   */
  public String getMacAddress() {
    return macAddress;
  }


  /**
   * @param macAddress the macAddress to set
   */
  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }


  public String toString() {
    return "http_cam: " + device_ip + ":" + device_port;
  }
}
