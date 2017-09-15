package com.hubble.registration.tasks;

import java.io.Serializable;

public class CheckFirmwareUpdateResult implements Serializable{

  private boolean mHaveNewFirmwareVersion = false;
  private String mFirmwareDownloadLink;
  private boolean mFirmwareResidedInCamera = false;
  private String mFirmwareVersionResidedInCamera;
  private String mCurrentFirmwareVersion;
  private String mOTAVersion;
  private boolean mLocalCamera = false;
  private boolean mRequestUpgradeOnly = false;

  private String mInetAddress;
  private static final String UPLOAD_FW_URL_PATTERN = "http://%s:8080/cgi-bin/haserlupgrade.cgi";
  private static final String BURNING_PROCESS_URL_PATTERN = "http://%s:8080/cgi-bin/fullupgrade";
  private static final String REQUEST_FW_URL_PATTERN = "http://%s:80/?action=command&command=request_fw_upgrade";
  private static final String REQUEST_FW_URL_COMMAND = "action=command&command=request_fw_upgrade";
  public static final String REQUEST_FW_URL_COMMAND_RESPONSE = "request_fw_upgrade: ";
  private static final String KEEP_ALIVE_URL_PATTERN = "http://%s:80/?action=command&command=get_version";
  private String mApiKey;
  private String mRegID;
  private String mNewFirmwareMD5;
  private String mNewFirmwareFileName;
  private String mNewFirmwareVersion;
  private boolean isDeviceOTA = false;

  public byte[] getSignatureData() {
    return signatureData;
  }

  public CheckFirmwareUpdateResult setSignatureData(byte[] signatureData) {
    this.signatureData = signatureData;
    return this;
  }

  public void setDeviceOTA(boolean deviceOTA)
  {
    isDeviceOTA = deviceOTA;
  }

  public boolean isDeviceOTA()
  {
    return isDeviceOTA;
  }
  private byte[] signatureData;

  public String getSignatureFileName() {
    return mSignatureFileName;
  }

  public CheckFirmwareUpdateResult setSignatureFileName(String mSignatureFileName) {
    this.mSignatureFileName = mSignatureFileName;
    return this;
  }

  private String mSignatureFileName;

  public void setNewFirmwareVersion(String fwVersion) {
    mNewFirmwareVersion = fwVersion;
  }

  public String getNewFirmwareVersion() {
    return mNewFirmwareVersion;
  }

  public void setHaveNewFirmwareVersion(boolean mHaveNewFirmwareVersion) {
    this.mHaveNewFirmwareVersion = mHaveNewFirmwareVersion;
  }

  public void setFirmwareDownloadLink(String mFirmwareDownloadLink) {
    this.mFirmwareDownloadLink = mFirmwareDownloadLink;
  }

  public void setFirmwareResidedInCamera(boolean mFirmwareResidedInCamera) {
    this.mFirmwareResidedInCamera = mFirmwareResidedInCamera;
  }

  public boolean isFirmwareResidedInCamera() {
    return mFirmwareResidedInCamera;
  }

  public boolean isHaveNewFirmwareVersion() {
    return mHaveNewFirmwareVersion;
  }

  public String getFirmwareDownloadLink() {
    return mFirmwareDownloadLink;
  }

  public String getFirmwareVersionResidedInCamera() {
    return mFirmwareVersionResidedInCamera;
  }

  public void setFirmwareVersionResidedInCamera(String mFirmwareVersionResidedInCamera) {
    this.mFirmwareVersionResidedInCamera = mFirmwareVersionResidedInCamera;
  }

  public String getCurrentFirmwareVersion() {
    return mCurrentFirmwareVersion;
  }

  public void setCurrentFirmwareVersion(String mCurrentFirmwareVersion) {
    this.mCurrentFirmwareVersion = mCurrentFirmwareVersion;
  }

  public String getOTAVersion() {
    return mOTAVersion;
  }

  public void setOTAVersion(String mOTAVersion) {
    this.mOTAVersion = mOTAVersion;
  }

  public boolean isLocalCamera() {
    return mLocalCamera;
  }

  public void setLocalCamera(boolean mLocalCamera) {
    this.mLocalCamera = mLocalCamera;
  }

  public String getInetAddress() {
    return mInetAddress;
  }

  public void setInetAddress(String mInetAddress) {
    this.mInetAddress = mInetAddress;
  }

  public String getUploadFwURL() {
    return String.format(UPLOAD_FW_URL_PATTERN, mInetAddress);
  }

  public String getBurningProgressURL() {
    return String.format(BURNING_PROCESS_URL_PATTERN, mInetAddress);
  }

  public String getRequestFWUpgradeURL() {
    return String.format(REQUEST_FW_URL_PATTERN, mInetAddress);
  }

  public String getRequestFWUpgradeCommand()
  {
    return REQUEST_FW_URL_COMMAND;
  }

  public String getKeepAliveURL() {
    return String.format(KEEP_ALIVE_URL_PATTERN, mInetAddress);
  }

  public String getApiKey() {
    return mApiKey;
  }

  public void setApiKey(String mApiKey) {
    this.mApiKey = mApiKey;
  }

  public String getRegID() {
    return mRegID;
  }

  public void setRegID(String mRegID) {
    this.mRegID = mRegID;
  }

  public boolean isRequestUpgradeOnly() {
    return mRequestUpgradeOnly;
  }

  public void setRequestUpgradeOnly(boolean mRequestUpgradeOnly) {
    this.mRequestUpgradeOnly = mRequestUpgradeOnly;
  }

  public String getNewFirmwareMD5() {
    return mNewFirmwareMD5;
  }

  public void setNewFirmwareMD5(String mNewFirmwareMD5) {
    this.mNewFirmwareMD5 = mNewFirmwareMD5;
  }

  public String getNewFirmwareFileName() {
    return mNewFirmwareFileName;
  }

  public void setNewFirmwareFileName(String mNewFirmwareFileName) {
    this.mNewFirmwareFileName = mNewFirmwareFileName;
  }

}
