package com.hubble.model;

import android.graphics.drawable.Drawable;


/**
 * Created by dan on 2014-07-10.
 * TODO: rip this out of SettingsActivity?
 */
public class DrawerItemModel {

  public String title;
  public int key;
  public Drawable icon;

  public StaticMenuItems menuItemType;

  public DrawerItemModel(String title, Drawable icon, StaticMenuItems menuItemType) {
    this.title = title;
    this.icon = icon;
    this.menuItemType = menuItemType;
    this.key = -1;
  }

  public enum StaticMenuItems {
    ACCOUNT(-2),
    HELP(-3),
    SEPARATOR(-4),
    EVENT_LOG(-5),
    DEVICE(-6),
    CAMERA_LIST(-7),
    PATROL(-8),
    VIDEO(-9),
    TRY_US_FOR_FREE(-10);
    private final int id;

    StaticMenuItems(int i) {
      this.id = i;
    }

    public int getValue() {
      return id;
    }
  }
}
