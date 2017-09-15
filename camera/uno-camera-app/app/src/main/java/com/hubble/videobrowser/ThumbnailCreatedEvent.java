package com.hubble.videobrowser;

/**
 * Created by songnob on 26/09/2015.
 */
public class ThumbnailCreatedEvent {
  public final String md5;
  public final String thumbnailPath;

  public ThumbnailCreatedEvent(String md5, String thumbnailPath) {
    this.md5 = md5;
    this.thumbnailPath = thumbnailPath;
  }
}
