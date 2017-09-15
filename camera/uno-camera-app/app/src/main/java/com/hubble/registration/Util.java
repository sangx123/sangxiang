package com.hubble.registration;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.common.collect.ImmutableMap;
import com.hubble.HubbleApplication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import base.hubble.PublicDefineGlob;

public class Util {


  public static final int USER_ID = 1;
  public static final int EMAIL = 2;
  public static final int CAMERA_NAME = 3;

  private static final String DASH_BOARD_EVENT_SP = "com.hubble.dash_board_last_event";
  private static final String DASH_BOARD_EVENT_NAME_KEY = "dash_board_event_name";
  private static final String DASH_BOARD_EVENT_TIME_KEY = "dash_board_event_time";

  public static  String  mPicExtension = ".png";

  public static String getGatewayIp(Context mContext) {
    String gatewayIp = null;
    WifiManager w = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    gatewayIp = ((w.getDhcpInfo().serverAddress) & 0xFF) + "." +
        ((w.getDhcpInfo().serverAddress >> 8) & 0xFF) + "." +
        ((w.getDhcpInfo().serverAddress >> 16) & 0xFF) + "." +
        ((w.getDhcpInfo().serverAddress >> 24) & 0xFF);
    return gatewayIp;
  }

  public static String getRecordFileName(String appname, String cameraName) {
    return new File(HubbleApplication.getVideoFolder(), cameraName + "@" + System.currentTimeMillis() + ".flv").getAbsolutePath();
  }

  private static String getDownloadFileName() {
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    return formatter.format(date) + ".flv";
  }

  /**
   * @param context used to check the device version and DownloadManager
   *                information
   * @return true if the download manager is available
   */
  public static boolean isDownloadManagerAvailable(Context context) {
    try {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
        return false;
      }
      Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.addCategory(Intent.CATEGORY_LAUNCHER);
      intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
      List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
      return list.size() > 0;
    } catch (Exception e) {
      return false;
    }
  }


  public static String getDownloadDirectory(String folderName) {
    File directory = null;
    //if there is no SD card, create new directory objects to make directory on device
    if (Environment.getExternalStorageState() == null) {
      //create new file directory object
      directory = new File(Environment.getDataDirectory() + "/" + folderName + "/");
      if (!directory.exists()) {
        directory.mkdir();
      }
      // if phone DOES have sd card
    } else if (Environment.getExternalStorageState() != null) {
      // search for directory on SD card
      directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + folderName + "/");
      if (!directory.exists()) {
        directory.mkdir();
      }
    }

    return directory.getPath();
  }

  public static void downloadClip(Context context, String url, String appName, String cameraName) {

    Log.i("Utils", "Preparing to download: " + url);
    String filename = cameraName + "@" + System.currentTimeMillis();
    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
    request.setDescription("Event video");
    request.setTitle(filename);
    request.allowScanningByMediaScanner();
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
    request.setDestinationUri(Uri.fromFile(new File(getRecordFileName(appName, cameraName))));
    request.setMimeType("video/x-flv");

    // get download service and enqueue file
    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    manager.enqueue(request);
  }

  public static String getVideoPathFromUri(Context context, Uri uri) {
    // just some safety built in
    if (uri == null) {
      // TODO perform some logging or show user feedback
      return null;
    }
    // try to retrieve the image from the media store first
    // this will only work for images selected from gallery
    String[] projection = {MediaStore.Video.Media.DATA};
    CursorLoader loader = new CursorLoader(context, uri, projection, null, null, null);
    Cursor cursor = loader.loadInBackground();
    if (cursor != null && cursor.getCount() > 0) {
      int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
      cursor.moveToFirst();
      return cursor.getString(column_index);
    }
    // this is our fallback here
    return uri.getPath();
  }

  public static String getLogDirectory() {
    String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "hubble"; // mbp_tflash
    if (Environment.getExternalStorageDirectory().getPath() != null && Environment.getExternalStorageDirectory().getPath().contains("external_sd")) {
      path = Environment.getExternalStorageDirectory().getPath() + File.separator + "hubble_sdcard"; // mbp_sdcard
    }

    File f = new File(path);
    if (!f.exists()) {
      boolean res = f.mkdirs();
      // // Log.w("hubble-dir", "create new dir: " + path + " ->" + res);

    }
    return path;
  }

  public static String getRootRecordingDirectory() {
    String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "hubble"; // mbp_tflash
    if (Environment.getExternalStorageDirectory().getPath() != null && Environment.getExternalStorageDirectory().getPath().contains("external_sd")) {
      path = Environment.getExternalStorageDirectory().getPath() + File.separator + "hubble_sdcard"; // mbp_sdcard
    }

    File f = new File(path);
    if (!f.exists()) {
      boolean res = f.mkdirs();
      // // Log.w("hubble-dir", "create new dir: " + path + " ->" + res);

    }
    return path;
  }

  public static String getLastPathComponent(String filePath) {
    String[] segments = filePath.split("/");
    String lastPathComponent = segments[segments.length - 1];
    return lastPathComponent;
  }


  public static String getFirmwareDirectory() {
    String path = getRootRecordingDirectory() + File.separator + "fw";

    if (Environment.getExternalStorageDirectory().getPath() != null && Environment.getExternalStorageDirectory().getPath().contains("external_sd")) {
      path = Environment.getExternalStorageDirectory().getPath() + File.separator + "hubble_sdcard"; // mbp_sdcard
    }

    File f = new File(path);
    if (!f.exists()) {
      boolean res = f.mkdirs();
      // // Log.w("hubble-dir", "create new dir: " + path + " ->" + res);
    }
    return path;
  }

  /**
   * Get cache directory in external storage: /storage/emulated/0/hubble/cache
   *
   * @return The path of cache directory.
   */
  public static String getCacheDirectory() {
    String path = getRootRecordingDirectory() + File.separator + "cache";
    File f = new File(path);
    if (!f.exists()) {
      boolean res = f.mkdirs();
      Log.w(HubbleApplication.TAG, "create new dir: " + path + " ->" + res);
    }
    return path;
  }

  /**
   * Get the latest snapshot file path for a camera.
   *
   * @param registrationId The registration id of camera.
   * @return The latest snapshot file path in device storage.
   */
  public static String getLatestSnapshotPath(String registrationId) {
    String snapshotPath = null;
    if (registrationId != null) {
      //since snapshot is saved only after 10 mins, its not saved in cache
      snapshotPath = HubbleApplication.getAppFolder() + File.separator + "latest_snapshot_" + registrationId + ".png";
    }
    return snapshotPath;
  }

  public static String getLatestPreviewPath(String registrationId) {
    String previewSnapshotPath = null;
    if (registrationId != null) {
      previewSnapshotPath = getCacheDirectory() + File.separator + "latest_preview_" + registrationId + ".png";
    }
    return previewSnapshotPath;
  }

  public static String getDashBoardPreviewPath(String registrationId) {
    String previewSnapshotPath = null;
    if (registrationId != null) {
      previewSnapshotPath = getCacheDirectory() + File.separator + "latest_dashboard_" + registrationId + ".png";
    }
    return previewSnapshotPath;
  }

    public static void deleteLatestPreview(String registrationId){
        String dashboardPreviewSnapshotPath = null;
        String previewSnapshotPath = null;
        if (registrationId != null) {
            dashboardPreviewSnapshotPath = getCacheDirectory() + File.separator + "latest_dashboard_" + registrationId + ".png";
            previewSnapshotPath = getCacheDirectory() + File.separator + "latest_preview_" + registrationId + ".png";
            deleteFile(dashboardPreviewSnapshotPath);
            deleteFile(previewSnapshotPath);
        }
    }

  /**
   * Check whether the latest snapshot of a camera is available.
   * App should call this method to check first before loading latest camera snapshot.
   *
   * @param registrationId The registration id of camera.
   * @return true if the latest snapshot is available, otherwise false.
   */
  public static boolean isLatestSnapshotAvailable(String registrationId) {
    boolean isSnapAvail = false;
    String latest = getLatestSnapshotPath(registrationId);
    if (latest != null) {
      File latestSnapFile = new File(latest);
      isSnapAvail = latestSnapFile.exists();
    }
    Log.i(HubbleApplication.TAG, "isLatestSnapshotAvailable, regId? " + registrationId + ", isAvailable? " + isSnapAvail);
    return isSnapAvail;
  }

  public static boolean isLatestPreviewAvailable(String registrationId) {
    boolean isSnapAvail = false;
    String latest = getLatestPreviewPath(registrationId);
    if (latest != null) {
      File latestSnapFile = new File(latest);
      isSnapAvail = latestSnapFile.exists();
    }
    //Log.i(HubbleApplication.TAG, "isLatestPreviewAvailable, regId? " + registrationId + ", isAvailable? " + isSnapAvail);
    return isSnapAvail;
  }

  public static boolean isDashBoardPreviewAvailable(String registrationId) {
    boolean isSnapAvail = false;
    String latest = getDashBoardPreviewPath(registrationId);
    if (latest != null) {
      File latestSnapFile = new File(latest);
      isSnapAvail = latestSnapFile.exists();
    }
    return isSnapAvail;
  }

  /**
   * Check whether the latest preview or the latest live snapshot is newer.
   * @param registrationId The registration id of camera.
   * @return true if the latest preview is newer than the live snapshot. Otherise false.
   */
  public static boolean shouldLoadLatestPreview(final String registrationId) {
    boolean shouldLoadPreview = false;
    if (isLatestPreviewAvailable(registrationId)) {
      if (isLatestSnapshotAvailable(registrationId)) {
        File latestPreviewFile = new File(getLatestPreviewPath(registrationId));
        File latestSnapshotFile = new File(getLatestSnapshotPath(registrationId));
        if (latestPreviewFile.lastModified() > latestSnapshotFile.lastModified()) {
          shouldLoadPreview = true;
        }
      } else {
        // Latest live snapshot is not available, should load preview instead
        shouldLoadPreview = true;
      }
    }
    return shouldLoadPreview;
  }

  /**
   * Check whether app should update the latest snapshot.
   * App should call this method to check before overwrite the latest snapshot of camera.
   *
   * @param registrationId  The registration id of camera.
   * @param newModifiedTime The modified time of new snapshot.
   * @return true if app should update the latest snapshot, otherwise false.
   */
  public static boolean shouldUpdateLatestSnapshot(String registrationId, long newModifiedTime) {
    boolean shouldUpdate = true;
    String latest = getLatestSnapshotPath(registrationId);
    if (latest != null) {
      File latestSnapFile = new File(latest);
      if (latestSnapFile.lastModified() >= newModifiedTime) {
        shouldUpdate = false;
      }
//      Log.i(HubbleApplication.TAG, "shouldUpdateLatestSnapshot, lastModified? " + latestSnapFile.lastModified() +
//          ", newData? " + newModifiedTime + ", shouldUpdate? " + shouldUpdate);
    }
    return shouldUpdate;
  }

  public static boolean shouldUpdateLatestPreview(String registrationId, long newModifiedTime) {
    boolean shouldUpdate = true;
    String latest = getLatestPreviewPath(registrationId);
    if (latest != null) {
      File latestSnapFile = new File(latest);
      if (latestSnapFile.lastModified() + 5*1000 >= newModifiedTime) {
        shouldUpdate = false;
      }
    }
    return shouldUpdate;
  }

  /**
   * Check whether app should update the latest snapshot.
   * App should call this method to check before overwrite the latest snapshot of camera.
   *
   * @param registrationId The registration id of camera.
   * @param newDate        The create time of new snapshot.
   * @return true if app should update the latest snapshot, otherwise false.
   */
  public static boolean shouldUpdateLatestSnapshot(String registrationId, Date newDate) {
    boolean shouldUpdate = true;
    String latest = getLatestSnapshotPath(registrationId);
    if (latest != null) {
      File latestSnapFile = new File(latest);
      if (latestSnapFile.lastModified() >= newDate.getTime()) {
        shouldUpdate = false;
      }
//      Log.i(HubbleApplication.TAG, "shouldUpdateLatestSnapshot, lastModified? " + latestSnapFile.lastModified() +
//          ", newData? " + newDate.getTime() + ", shouldUpdate? " + shouldUpdate);
    }
    return shouldUpdate;
  }

  /**
   * Check whether app should update the latest preview.
   * App should call this method to check before overwrite the latest preview of camera.
   *
   * @param registrationId The registration id of camera.
   * @param newDate        The create time of new snapshot.
   * @return true if app should update the latest snapshot, otherwise false.
   */
  public static boolean shouldUpdateLatestPreview(String registrationId, Date newDate) {
    boolean shouldUpdate = true;
    String latest = getLatestPreviewPath(registrationId);
    if (latest != null) {
      File latestSnapFile = new File(latest);
      if (latestSnapFile.lastModified() + 5*1000 >= newDate.getTime()) {
        shouldUpdate = false;
      }
    }
    return shouldUpdate;
  }

  public static void saveBitmapToFile(Bitmap bitmap, String filePath) {
    //Log.d(HubbleApplication.TAG, "saveImageToFile filePath: " + filePath);
    FileOutputStream fos = null;
    try {
      File image = new File(filePath);
      if (bitmap != null) {
        fos = new FileOutputStream(image);
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.close();
      }
    } catch (Exception ignored) {
      ignored.printStackTrace();
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static Character[] hex_chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'};

  public static boolean isThisAHexString(String str) {
    char ch;
    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if (Arrays.asList(hex_chars).indexOf(ch) == -1) {
        return false;
      }
    }
    return true;
  }

  public static String getLogFile() {
    String path = getLogDirectory() + File.separator + "logs";

    File f = new File(path);
    if (!f.exists()) {
      f.mkdirs();
    }

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyddMM_HHmmss", Locale.US);
    return path + File.separator + formatter.format(date) + ".txt";
  }

  public static boolean deleteFile(String filepath) {
    File f = new File(filepath);
    if (f.exists()) {
      // // Log.d("mbp", " delete: " + filepath);
      return f.delete();
    }
    return false;
  }

  public static void hideSoftKeyboard(Context context, View currentFocus) {
    if (currentFocus != null) {
      InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }
  }

  public static boolean validate(int fieldId, String fieldContent) {
    boolean isValid = false;
    switch (fieldId) {
      case USER_ID:
        String regularExpression = "^[_a-zA-Z0-9]+$";
        if (fieldContent.matches(regularExpression)) {
          isValid = true;
        }
        break;

      case EMAIL:
        regularExpression = "^[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)+$";
        if (fieldContent.matches(regularExpression)) {
          isValid = true;
        }
        break;

		/*
     * 20130306: hoang: issue 1505 validate camera name
		 */
      case CAMERA_NAME:
        regularExpression = "^[a-zA-Z0-9' \\._-]+$";
        if (fieldContent.matches(regularExpression)) {
          isValid = true;
        }
        break;

      default:
        break;
    }

    return isValid;
  }

  /**
   * A copy of the Android internals  insertImageToGallery method, this method populates the
   * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
   * that is inserted manually gets saved at the end of the gallery (because date is not populated).
   *
   * @see android.provider.MediaStore.Images.Media#insertImage(ContentResolver, Bitmap, String, String)
   */
  public static final String insertImageToGallery(Context context, Bitmap source, String title, String description) {
    String stringUrl = null;
    try {
//      File pictureFolder = new File(HubbleApplication.getVideoFolder().getAbsolutePath());//Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//      File imagesFolder = new File(pictureFolder, description + "" + context.getString(com.vtech.vtechconnect.R
//          .string.photos));
//
//      if (!imagesFolder.exists()) {
//        imagesFolder.mkdirs();
//      }

      File image = new File(HubbleApplication.getVideoFolder().getAbsolutePath() + File.separator + title + mPicExtension);

      if (image == null) {
        return stringUrl;
      }

      if (source != null) {
        FileOutputStream fos = new FileOutputStream(image);
        if(mPicExtension.equals(".jpg")) {
          source.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        } else {
          source.compress(Bitmap.CompressFormat.PNG, 90, fos);
        }
        fos.close();
        stringUrl = image.getPath();
        MediaScannerConnection.scanFile(context, new String[]{image.getPath()}, null, null);
      }
    } catch (Exception ignored) {
      Log.e("Util", ignored.getMessage());
    }
    mPicExtension = ".png";
    return stringUrl;
  }

  public static int getLocalHertz() {
    int iOffset = getTimeZoneOffsetWithDST() / 3600000;
    int hertz = iOffset >= -4 && iOffset <= 9 ? 50 : 60;
    return hertz;
  }

  public static int getTimeZoneOffsetWithDST() {
    int offset = TimeZone.getDefault().getRawOffset();
    if (TimeZone.getDefault().useDaylightTime()) {
      int dst = TimeZone.getDefault().getDSTSavings();
      offset += dst;
    }

    return offset;
  }

  /**
   * A copy of the Android internals StoreThumbnail method, it used with the insertImageToGallery to
   * populate the android.provider.MediaStore.Images.Media#insertImageToGallery with all the correct
   * meta data. The StoreThumbnail method is private so it must be duplicated here.
   *
   * @see android.provider.MediaStore.Images.Media (StoreThumbnail private method)
   */
  private static final Bitmap storeThumbnail(ContentResolver cr, Bitmap source, long id, float width, float height, int kind) {

    // create the matrix to scale it
    Matrix matrix = new Matrix();

    float scaleX = width / source.getWidth();
    float scaleY = height / source.getHeight();

    matrix.setScale(scaleX, scaleY);

    Bitmap thumb = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

    ContentValues values = new ContentValues(4);
    values.put(MediaStore.Images.Thumbnails.KIND, kind);
    values.put(MediaStore.Images.Thumbnails.IMAGE_ID, (int) id);
    values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.getHeight());
    values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.getWidth());

    Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

    try {
      OutputStream thumbOut = cr.openOutputStream(url);
      thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
      thumbOut.close();
      return thumb;
    } catch (FileNotFoundException ex) {
      return null;
    } catch (IOException ex) {
      return null;
    }
  }

  public static float sp2Px(Context context, float sp) {
    float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
    return sp * scaledDensity;
  }

  public static float dp2Px(Context context, float dp) {
    float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
    return dp * scaledDensity;
  }

  public static String getStringByName(Context context, String resName) {
    String packageName = context.getPackageName();
    int resId = context.getResources().getIdentifier(resName, "string", packageName);
    return context.getString(resId);
  }

  /**
   * Compare version 1 and version 2, if version 1 greater than version 2 than
   * return true, else return false
   *
   * @param version1
   * @param version2
   * @return true or false
   */
  public static boolean isThisVersionGreaterThan(String version1, String version2) {
    // version format 01.01.01
    // if any version is null then we can considered wrong parameter so we will return
    // status as false
    // this helps to avoid crashes.
    if(version1 == null || version2 == null)
    {
      return false;
    }
    String[] versions1 = version1.split(PublicDefine.FW_VERSION_DOT);
    String[] versions2 = version2.split(PublicDefine.FW_VERSION_DOT);
    boolean result = false;

    if (versions1.length == 3 && versions2.length == 3) {
      Integer major1 = Integer.parseInt(versions1[0]);
      Integer major2 = Integer.parseInt(versions2[0]);
      Integer minor1 = Integer.parseInt(versions1[1]);
      Integer minor2 = Integer.parseInt(versions2[1]);
      Integer patch1 = Integer.parseInt(versions1[2]);
      Integer patch2 = Integer.parseInt(versions2[2]);

      if (major1 > major2) {
        result = true;
      } else if (major1 == major2) {
        if (minor1 > minor2) {
          result = true;
        } else if (minor1 == minor2) {
          if (patch1 > patch2) {
            result = true;
          } else {
            result = false;
          }
        } else {
          result = false;
        }
      }
    }

    return result;
  }

  private static final Map<String, String> groupSettingsCompatibilityMap = ImmutableMap.<String, String>builder()
      .put("0073", "01.19.22")
      .put("0173", "01.19.22")
      .put("0083", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("0084", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("0085", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("0854", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("1854", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("1855", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("1662", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("0662", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("0066", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("0086", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("0080", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION)
      .put("0931", "01.19.14")
      .put("0921", "01.19.14")
      .put("0068", "01.19.24")
      .put("2855", "01.19.24")
      .put("0877", "01.19.28")
      .put("0667", PublicDefine.SETTING2_GENERIC_FIRMWARE_VERSION).build();

  public static boolean checkSetting2Compatibility(String cameraModel, String firmwareVersion) {
    if (TextUtils.isEmpty(firmwareVersion) || !groupSettingsCompatibilityMap.containsKey(cameraModel)) {
      return false;
    }
    String compatibleVersion = groupSettingsCompatibilityMap.get(cameraModel);
    /* if compatibleVersion = 19.14 and firmwareVersion = 19.14 #isThisVersionGreaterThan will return false -> this method return true
     * if compatibleVersion = 19.14 and firmwareVersion = 19.13 #isThisVersionGreaterThan will return true -> this method return false
     * if compatibleVersion = 19.14 and firmwareVersion = 19.15 #isThisVersionGreaterThan will return false -> this method return true */
    return !isThisVersionGreaterThan(compatibleVersion, firmwareVersion);
  }

  public static boolean isDigitOnly(String str) {
    return str.matches("-?\\d+(\\.\\d+)?");
  }

  // plan id - max number of subscription
  public static final Map<String, Integer> hubbleTierMap = ImmutableMap.<String, Integer>builder()
      .put(PublicDefineGlob.HUBBLE_TIER1, 4).put(PublicDefineGlob.HUBBLE_TIER1_YEARLY, 4)
      .put(PublicDefineGlob.HUBBLE_TIER2, 4).put(PublicDefineGlob.HUBBLE_TIER2_YEARLY, 4)
      .put(PublicDefineGlob.HUBBLE_TIER3, 10).put(PublicDefineGlob.HUBBLE_TIER3_YEARLY, 10)
      .build();

  /**
   * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
   * @param context Context reference to get the TelephonyManager instance from
   * @return country code or null
   */
  public static String getUserCountry(Context context) {
    try {
      final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      final String simCountry = tm.getSimCountryIso();
      if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
        return simCountry.toLowerCase(Locale.US);
      } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
        String networkCountry = tm.getNetworkCountryIso();
        if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
          return networkCountry.toLowerCase(Locale.US);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String getNetworkInfo() {
    HttpURLConnection urlConnection = null;
    String response = null;
    try {
      URL url = new URL("http://ip-api.com/json");
      urlConnection = (HttpURLConnection) url.openConnection();
      InputStream in = new BufferedInputStream(urlConnection.getInputStream());
      response = streamToString(in);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }
    return response;
  }

  private static String streamToString(InputStream is) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder builder = new StringBuilder();
    String line = reader.readLine();
    while (line != null) {
      builder.append(line).append("\n");
      line = reader.readLine();
    }
    is.close();
    return builder.toString();
  }

  public static boolean isUseSignatureForFwUpgrade(String modelId, String fwVersion) {
    return !modelId.equals("0877") && !modelId.equals("0086") && !modelId.equals("0081")
        && isThisVersionGreaterThan(fwVersion, "01.19.35");
  }

  public static void setDashBoardEventNameToSP(Context context, String registrationId,
                                             String eventName) {
    SharedPreferences sharedPref = context.getSharedPreferences(
            DASH_BOARD_EVENT_SP, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString(DASH_BOARD_EVENT_NAME_KEY+registrationId, eventName);
    editor.commit();
  }

  public static void setDashBoardEventTimeToSP(Context context, String registrationId,
                                           String eventTime) {
    SharedPreferences sharedPref = context.getSharedPreferences(
            DASH_BOARD_EVENT_SP, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString(DASH_BOARD_EVENT_TIME_KEY+registrationId, eventTime);
    editor.commit();
  }

  public static String getDashBoardEventNameFromSP(Context context, String registrationId) {
    SharedPreferences sharedPref = context.getSharedPreferences(
            DASH_BOARD_EVENT_SP, Context.MODE_PRIVATE);
    String eventName = sharedPref.getString(DASH_BOARD_EVENT_NAME_KEY + registrationId, "");
    return eventName;
  }

  public static String getDashBoardEventTimeFromSP(Context context, String registrationId) {
    SharedPreferences sharedPref = context.getSharedPreferences(
            DASH_BOARD_EVENT_SP, Context.MODE_PRIVATE);
    String eventTime = sharedPref.getString(DASH_BOARD_EVENT_TIME_KEY+registrationId, "");
    return eventTime;
  }

  public static void removeDashBoardEventsFromSP(Context context, String registrationId){
      SharedPreferences sharedPref = context.getSharedPreferences(
              DASH_BOARD_EVENT_SP, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = sharedPref.edit();
      editor.remove(DASH_BOARD_EVENT_TIME_KEY+registrationId);
      editor.remove(DASH_BOARD_EVENT_NAME_KEY+registrationId);
      editor.commit();
  }
}