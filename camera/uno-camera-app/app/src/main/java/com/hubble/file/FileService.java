package com.hubble.file;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.hubble.HubbleApplication;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import base.hubble.PublicDefineGlob;

/**
 * Created by Son Nguyen on 18/12/2015.
 */
public class FileService {
  private static final String TAG = FileService.class.getSimpleName();
  //public static final String VIDEO_TYPE = "Videos";
  public static final String IMAGE_TYPE = "Images";

  private static String recordFileName = new String();

  public static void downloadVideo(String url, String fileNameWithExtension, FutureCallback<File> callback) {
    downloadFile(url, fileNameWithExtension, getUserFolder(), callback);
  }

  public static void downloadVideo(String url, String fileNameWithExtension, FutureCallback<File> callback, ProgressDialog progressDialog) {
    downloadFile(url, fileNameWithExtension, getUserFolder(), callback, progressDialog);
  }

  public static void downloadFile(String url, final String fileName, String type, FutureCallback<File> callback, ProgressDialog progressDialog) {
    File appFolder = HubbleApplication.getAppFolder();
    File typeFolder = new File(appFolder, type);
    if (!typeFolder.exists()) {
      typeFolder.mkdir();
    }
    File downloadFile = new File(typeFolder, fileName);
    Ion.with(HubbleApplication.AppContext)
        .load(url)
        .setTimeout(90000)
        .setLogging(TAG, Log.VERBOSE)
        .progressDialog(progressDialog)
        .progress(new ProgressCallback() {
          @Override
          public void onProgress(long downloaded, long total) {
            int percent = (int) ((downloaded * 100) / total);
            Log.i(TAG, "Downloaded: " + percent);
          }
        })
        .write(downloadFile)
        .setCallback(callback);
  }


  public static void downloadFile(String url, final String fileName, String type, FutureCallback<File> callback) {
    File appFolder = HubbleApplication.getAppFolder();
    File typeFolder = new File(appFolder, type);
    if (!typeFolder.exists()) {
      typeFolder.mkdir();
    }
    File downloadFile = new File(typeFolder, fileName);
    Ion.with(HubbleApplication.AppContext)
        .load(url)
        .setTimeout(90000)
        .setLogging(TAG, Log.VERBOSE)
        .progress(new ProgressCallback() {
          @Override
          public void onProgress(long downloaded, long total) {
            int percent = (int) ((downloaded * 100) / total);
            Log.i(TAG, "Downloaded: " + percent);
          }
        })
        .write(downloadFile)
        .setCallback(callback);
  }

  public static void downloadFile(String url, final String fileName,FutureCallback<File> callback) {
    File appFolder = HubbleApplication.getAppFolder();
    File typeFolder = new File(appFolder, getUserFolder());
    if (!typeFolder.exists()) {
      typeFolder.mkdir();
    }
    File downloadFile = new File(typeFolder, fileName);
    Ion.with(HubbleApplication.AppContext)
            .load(url)
            .setTimeout(90000)
            .setLogging(TAG, Log.VERBOSE)
            .write(downloadFile)
            .setCallback(callback);
  }

  public static void downloadFileWithProgressNotification(String url, final String fileName, FutureCallback<File> callback, final NotificationManager notifyManager, final NotificationCompat.Builder builder, final int notificationId ){

    File appFolder = HubbleApplication.getAppFolder();
    File typeFolder = new File(appFolder, getUserFolder());
    if (!typeFolder.exists()) {
      typeFolder.mkdir();
    }
    File downloadFile = new File(typeFolder, fileName);

    Ion.with(HubbleApplication.AppContext)
            .load(url)
            .setTimeout(90000)
            .setLogging(TAG, Log.VERBOSE)
            .progress(new ProgressCallback() {
              @Override
              public void onProgress(long downloaded, long total) {
                int percent = (int) ((downloaded * 100) / total);
                builder.setProgress(100, percent, false);
                notifyManager.notify(notificationId, builder.build());
              }
            })
            .write(downloadFile)
            .setCallback(callback);
  }

  /**
   * This method get correct Uri from File (support Android 6.0)
   *
   * @param file File object need to parse to File Uri
   * @return Uri if file existing, otherwise null
   */
  public static Uri getFileUri(File file) {
    Uri contentUri;
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.vtech.fileprovider", file);
    } else if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.inanny.fileprovider", file);
    } else if (BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.beurer.fileprovider", file);
    } else {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, CommonConstants.FILE_PROVIDER_AUTHORITY_HUBBLE, file);
    }
    return contentUri;
  }

  public static Intent getShareIntent(File file, String textAdditional) {
    Uri contentUri = getFileUri(file);
    Intent shareIntent = new Intent();
    if (BuildConfig.FLAVOR.equals("vtech")) {
      shareIntent.setAction(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_TEXT,textAdditional);
    } else {
      shareIntent.setAction(Intent.ACTION_SEND);
    }

    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
    shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    shareIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    shareIntent.setType("video/mp4");
    return shareIntent;
  }

  public static File getFormatedVideoFilePath(String cameraName, long time) {
    String dateString = new SimpleDateFormat("yyyyMMdd_hhmmssa").format(new Date(time));
    return new File(HubbleApplication.getVideoFolder(), cameraName + "@" + dateString + ".flv");
  }

  public static void setFormatedFilePath(long time) {
    recordFileName = new SimpleDateFormat("yyyyMMdd_hhmmssa").format(new Date(time));
  }

  public static File getFormatedFilePathForVideo(String cameraName) {
    return  new File(HubbleApplication.getVideoFolder(), cameraName + "@" + recordFileName + ".flv");
  }

  public static File getFormatedFilePathForVideo(String cameraName,long time) {
    return  new File(HubbleApplication.getVideoFolder(), cameraName + "@" + new SimpleDateFormat("yyyyMMdd_hhmmssa").format(new Date(time)) + ".flv");
  }




  public static String getFormatedFilePath() {
    return  recordFileName;
  }

  public static String getFormatedVideoFileName(String cameraName, long time) {
    String dateString = new SimpleDateFormat("yyyyMMdd_hhmmssa").format(new Date(time));
    return cameraName + "@" +  dateString + ".flv";
  }

  public static File fileExists(final String fileName, String type) {
    File typeFolder = new File(HubbleApplication.getAppFolder(), type);
    if (typeFolder.exists()) {
      File downloadFile = new File(typeFolder, fileName);
      if (downloadFile.exists()) {
        return downloadFile;
      }
    }
    return null;
  }

  public static String getUserFolder(){
    String folderName = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, "");
    if(folderName == null || folderName.isEmpty()){
      //If user name is empty in any case put the folder name as "Videos"
      folderName = "Videos";
    }
    return folderName;
  }
}
