package com.hubble.events;

/**
 * Created by songn_000 on 15/10/2015.
 */
public class ShareEventData {
  private String imageUri;
  private String fileName;

  public ShareEventData(String imageUri, String fileName) {
    this.fileName = fileName;
    this.imageUri = imageUri;
  }

  public String getImageUri() {
    return imageUri;
  }

  public String getFileName() {
    return fileName;
  }
}
