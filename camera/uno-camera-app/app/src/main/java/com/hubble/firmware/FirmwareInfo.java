package com.hubble.firmware;

import android.webkit.URLUtil;

import java.util.Locale;

/**
 * Creator: Son Nguyen
 * Email  : son.nguyen@hubblehome.com
 * Date   : 2:08 PM 13 Jun 2017
 */
public abstract class FirmwareInfo {
  protected String model, fwVersion;
  protected boolean devMode = false;

  public FirmwareInfo(String model, boolean devMode) {
    this.model = model;
    this.devMode = devMode;
  }

  public FirmwareInfo(String model, String fwVersion, boolean devMode) {
    this.model = model;
    this.fwVersion = fwVersion;
    this.devMode = devMode;
  }

  public String getSignatureDownloadLink() {
    return String.format(Locale.ENGLISH, getFwSignatureUrlPattern(), model, model, fwVersion);
  }

  public String getBinaryDownloadLink() {
    return String.format(Locale.ENGLISH, getFwBinaryUrlPattern(), model, model, fwVersion);
  }

  public String getFwCheckLink() {
    return String.format(Locale.ENGLISH, getCheckOTAUrlPattern(), model);
  }

  abstract String getFwBinaryUrlPattern();

  abstract String getFwSignatureUrlPattern();

  abstract String getCheckOTAUrlPattern();

  public void setOtaVersion(String otaFwVersion) {
    this.fwVersion = otaFwVersion;
  }

  public String getBinaryFilename() {
    String binaryUrl = getBinaryDownloadLink();
    if (binaryUrl == null) return null;
    return getFilenameFromUrl(binaryUrl);
  }

  public String getSignatureFilename() {
    String url = getSignatureDownloadLink();
    if (url == null) return null;
    return getFilenameFromUrl(url);
  }

  public static String getFilenameFromUrl(String url) {
    if (url == null) return null;
    int index = url.lastIndexOf("/") + 1;
    if(index <= 0) return null;
    return url.substring(index);
  }

}
