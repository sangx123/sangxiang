package com.blinkhd;

import java.util.Hashtable;

public class SupportModel {

  private static Hashtable<String, String> nonGoogleModel;

  static {
    nonGoogleModel = new Hashtable<String, String>();

    nonGoogleModel.put("Home Phone MBP2000", "Home Phone MBP2000");
    nonGoogleModel.put("MBP1000", "MBP1000");
    nonGoogleModel.put("A13MID", "A13MID");
    nonGoogleModel.put("S720c", "S720c");
    nonGoogleModel.put("722", "722");
    nonGoogleModel.put("N59-B", "N59-B");
  }


  public static boolean doesPhoneModelSupportGCM(String phoneModel) {
    return !nonGoogleModel.contains(phoneModel);

  }

}
