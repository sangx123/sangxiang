package com.hubble.firmware;

/**
 * Creator: Son Nguyen
 * Email  : son.nguyen@hubblehome.com
 * Date   : 2:36 PM 13 Jun 2017
 */
public class GenericModelFwInfo extends FirmwareInfo {

  private static final String GENERIC_FW_BINARY_URL_PATTERN = "https://ota.hubble.in/ota1/%s_patch/%s-%s.tar.gz";
  private static final String GENERIC_FW_SIGNATURE_URL_PATTERN = "http://ota.hubble.in/ota1/%s_patch/%s-%s.md5";

  private static final String CHECK_FW_OTA_OFFICIAL_URL_PATTERN = "https://ota.hubble.in/ota1/%s_patch/version.txt";
  private static final String CHECK_FW_OTA_DEV_URL_PATTERN = "https://ota.hubble.in/ota1/%s_patch/version_dev.txt";

  public GenericModelFwInfo(String model, String fwVersion, boolean devMode) {
    super(model, fwVersion, devMode);
  }

  public GenericModelFwInfo(String model, boolean devMode) {
    super(model, devMode);
  }

  @Override
  String getFwBinaryUrlPattern() {
    return GENERIC_FW_BINARY_URL_PATTERN;
  }

  @Override
  String getFwSignatureUrlPattern() {
    return GENERIC_FW_SIGNATURE_URL_PATTERN;
  }

  @Override
  String getCheckOTAUrlPattern() {
    if (devMode) return CHECK_FW_OTA_DEV_URL_PATTERN;
    else return CHECK_FW_OTA_OFFICIAL_URL_PATTERN;
  }
}
