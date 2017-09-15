package com.msc3;

import java.util.UUID;

/**
 * Creator: Son Nguyen
 * Email  : son.nguyen@hubblehome.com
 * Date   : 4:42 PM 24 Apr 2017
 */
public class AppUUID {
  public static String uuid;
  static {
    uuid = UUID.randomUUID().toString();
  }
  public static String getAppUuid() {
    return uuid;
  }
}
