package com.hubble.firmware;

/**
 * Creator: Son Nguyen
 * Email  : son.nguyen@hubblehome.com
 * Date   : 11:31 AM 13 Jun 2017
 */
public class FirmwareInfoFactory {

  public FirmwareInfo getFirmwareInfo(String model, boolean devMode) {
    switch (model) {
      case Model0072FwInfo.MODEL_ID:
        return new Model0072FwInfo(devMode);
      default:
        //return new GenericModelFwInfo(model, devMode);
        //currently return null;
        return null;
    }
  }
}
