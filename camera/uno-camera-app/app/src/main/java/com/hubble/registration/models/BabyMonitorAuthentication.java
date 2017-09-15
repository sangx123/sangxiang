package com.hubble.registration.models;

/**
 * @author phung
 *         <p/>
 *         BabyMonitorAuthentication - simple object to store authentication information
 *         - ip:port
 *         - session_key
 */

public abstract class BabyMonitorAuthentication {

  protected String device_ip;
  protected int device_port;
  protected String session_key;
  protected String stream_url;

  public abstract String getIP();

  public abstract int getPort();

  public abstract String getSSKey();

  public abstract String getStreamUrl();

  /**
   * Get the LocalPort to bind to when connecting to BM
   * - This is only applicable to UDT mode BM -
   * This port is the one that app uses to connect to UDT server and thus
   * has to be used to connect to camera (otherwise camera will reject the connection)
   * <p/>
   * - HTTP mode BM - this should not be used
   * --
   *
   * @return LocalUDTPort - to bind to when connecting
   * 0            - if the comm mode is HTTP
   */
  public abstract int getLocalPort();

  public abstract String toString();

  public void setCamIp(String ip) {
    this.device_ip = ip;

  }

  public void setCamPort(int port) {
    this.device_port = port;
  }

  public void setSSKey(String sskey) {
    this.session_key = sskey;

  }

  /**
   * @param stream_url the stream_url to set
   */
  public void setStreamUrl(String stream_url) {
    this.stream_url = stream_url;
  }

}
