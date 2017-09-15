package com.hubble.registration.models;

import java.net.InetAddress;

public class CameraBonjourInfo extends ApScanBase {
  private String cameraName;
  private InetAddress cameraAddress;
  private String ip;
  private String mac;

  public CameraBonjourInfo(String camName, InetAddress camAddress, String ip, String mac) {
    super("mdns");
    this.cameraAddress = camAddress;
    this.cameraName = camName;
    this.ip = ip;
    this.mac = mac;
  }

  public String getCameraName() {
    return cameraName;
  }

  public void setCameraName(String cameraName) {
    this.cameraName = cameraName;
  }

  public InetAddress getCameraAddress() {
    return cameraAddress;
  }

  public void setCameraAddress(InetAddress cameraAddress) {
    this.cameraAddress = cameraAddress;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getMac() {
    return mac;
  }

  public void setMac(String mac) {
    this.mac = mac;
  }

  @Override
  public String toString() {
    return String.format("Camera information: MAC -> %s, IP -> %s, name -> %s, inet address -> %s.", mac, ip, cameraName, cameraAddress);
  }
}