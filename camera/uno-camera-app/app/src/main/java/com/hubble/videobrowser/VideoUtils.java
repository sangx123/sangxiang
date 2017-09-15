package com.hubble.videobrowser;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import com.hubble.HubbleApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.greenrobot.event.EventBus;

/**
 * Created by Son Nguyen on 11/12/2015.
 */
public class VideoUtils {
  private static final String TAG = "VideoUtils";

  public static void generateThumbnailAsync(final String filePath) {
    long start = System.currentTimeMillis();
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (isCache(md5(filePath))) {
          Log.i(TAG, "Thumbnail is cached");
        } else {
          Log.i(TAG, "Start generated thumbnail for " + md5(filePath));
          generateThumbnail(filePath);
          Log.i(TAG, "Generated thumbnail done");
        }

      }
    }).start();
    Log.i(TAG, "Run thread cost: " + (System.currentTimeMillis() - start));
  }

  public static void generateThumbnail(String filePath) {
    String md5 = md5(filePath);
    Log.i(TAG, "File path: " + filePath);
    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(filePath,
        MediaStore.Images.Thumbnails.MINI_KIND);

    if (!isCache(md5)) {
      Log.i(TAG, "Thumbnail is not cached, cached it.");
      String cachedFilePath = cacheBitmap(thumb, md5);
      if (thumb != null) {
        ThumbnailCreatedEvent thumbnailCreatedEvent = new ThumbnailCreatedEvent(md5, cachedFilePath);
        EventBus.getDefault().post(thumbnailCreatedEvent);
      }
    } else {
      Log.i(TAG, "Thumbnail is cached");
    }
  }

  private static String cacheBitmap(Bitmap bitmap, String id) {
    FileOutputStream out = null;
    File cachedFile = HubbleApplication.AppContext.getCacheFile(id);
    try {
      out = new FileOutputStream(cachedFile);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
      out.flush();
      // PNG is a lossless format, the compression factor (100) is ignored
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return cachedFile.getAbsolutePath();
  }

  public static boolean isCache(String id) {
    File file = HubbleApplication.AppContext.getCacheFile(id);
    return file.exists();
  }

  public static final String md5(final String s) {
    final String MD5 = "MD5";
    try {
      // Create MD5 Hash
      MessageDigest digest = MessageDigest
          .getInstance(MD5);
      digest.update(s.getBytes());
      byte messageDigest[] = digest.digest();

      // Create Hex String
      StringBuilder hexString = new StringBuilder();
      for (byte aMessageDigest : messageDigest) {
        String h = Integer.toHexString(0xFF & aMessageDigest);
        while (h.length() < 2)
          h = "0" + h;
        hexString.append(h);
      }
      return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return "aabbcc";
  }


}
