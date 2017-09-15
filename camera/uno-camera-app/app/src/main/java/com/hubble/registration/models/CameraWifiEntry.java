package com.hubble.registration.models;

public class CameraWifiEntry {

  private String ssid_w_quote;
  private String bssid;
  private String auth_mode;
  private String encrypt_type;//version 1.1
  private String quality;
  int signal_level;
  int noise_level;
  int channel;

  public CameraWifiEntry(String ssid) {
    this.ssid_w_quote = ssid;
  }

  public String getSsidNoQuote() {
    String ret = ssid_w_quote;
    if (ssid_w_quote.contains("\"")) {
      ret = ssid_w_quote.substring(1, ssid_w_quote.length() - 1);
    }
    return ret;
  }

  public String getSsid() {
    return ssid_w_quote;
  }

  public void setSsid(String ssid_w_quote) {
    this.ssid_w_quote = ssid_w_quote;
  }

  public String getBssid() {
    return bssid;
  }

  public void setBssid(String bssid) {
    this.bssid = bssid;
  }

  public String getAuth_mode() {
    return auth_mode;
  }

  public void setAuth_mode(String auth_mode) {
    this.auth_mode = auth_mode;
  }

  public String getEncrypt_type() {
    return encrypt_type;
  }

  public void setEncrypt_type(String encrypt_type) {
    this.encrypt_type = encrypt_type;
  }

  public String getQuality() {
    return quality;
  }

  public void setQuality(String quality) {
    this.quality = quality;
  }

  public int getSignal_level() {
    return signal_level;
  }

  public void setSignal_level(int signal_level) {
    this.signal_level = signal_level;
  }

  public int getNoise_level() {
    return noise_level;
  }

  public void setNoise_level(int noise_level) {
    this.noise_level = noise_level;
  }

  public int getChannel() {
    return channel;
  }

  public void setChannel(int channel) {
    this.channel = channel;
  }

  public String log() {
    StringBuilder sb = new StringBuilder();
    sb.append("------------------------");
    sb.append("ssid_w_quote = " + ssid_w_quote + "\n");
    sb.append("bssid = " + bssid + "\n");
    sb.append("auth_mode = " + auth_mode + "\n");
    sb.append("encrypt_type = " + encrypt_type + "\n");
    sb.append("quality = " + quality + "\n");
    sb.append("signal_level = " + signal_level + "\n");
    sb.append("noise_level = " + noise_level + "\n");
    sb.append("channel = " + channel);
    return sb.toString();
  }
}
