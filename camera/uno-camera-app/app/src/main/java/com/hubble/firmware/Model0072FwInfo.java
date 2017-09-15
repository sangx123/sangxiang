package com.hubble.firmware;

/**
 * Creator: Son Nguyen
 * Email  : son.nguyen@hubblehome.com
 * Date   : 11:36 AM 13 Jun 2017
 */
public class Model0072FwInfo extends GenericModelFwInfo {

  public static final String MODEL_ID = "0072";
  private static final String FW_0072_BINARY_URL_PATTERN = "https://ota.hubble.in/ota1/%s_patch/%s-%s.fw.pkg2";
  private static final String FW_0072_SIGNATURE_URL_PATTERN = "https://ota.hubble.in/ota1/%s_patch/%s-%s.sig";

  public Model0072FwInfo(String fwVersion, boolean devMode) {
    super(MODEL_ID, fwVersion, devMode);
  }

  public Model0072FwInfo(boolean devMode) {
    super(MODEL_ID, devMode);
  }

  @Override
  String getFwBinaryUrlPattern() {
    return FW_0072_BINARY_URL_PATTERN;
  }

  @Override
  String getFwSignatureUrlPattern() {
    return FW_0072_SIGNATURE_URL_PATTERN;
  }
}
