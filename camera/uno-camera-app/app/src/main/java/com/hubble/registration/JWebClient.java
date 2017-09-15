package com.hubble.registration;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JWebClient {

  private static final String TAG = "JWebClient";

  public static String downloadAsStringWithoutEx(String strURL) {
    String result = "";

    try {
      URL url = new URL(strURL);
      InputStream stream = null;

      // Open connection to URL.
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      // Connect to server.
      connection.connect();
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
      if (connection.getResponseCode() == 200) {
        stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String tmp = reader.readLine();
        while (tmp != null) {
          result += tmp;
          tmp = reader.readLine();
        }
        reader.close();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    if (result.equalsIgnoreCase("")) {
      return null;
    }
    return result;

  }

  /**
   * Download camera signature to byte array
   * @param strURL signature download link
   * @return signature data as byte array or null if error
   */
  public static byte[] downloadSignatureData(String strURL) {
    Log.i(TAG, "Download signature link: " + strURL);
    byte[] byteArray = null;
    try {
      URL url = new URL(strURL);
      InputStream stream = null;
      // Open connection to URL.
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      // Connect to server.
      connection.connect();
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
      if (connection.getResponseCode() == 200) {
        stream = connection.getInputStream();
        byteArray = com.google.common.io.ByteStreams.toByteArray(stream);
        stream.close();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return byteArray;
  }

  public static String downloadAsString(String strURL) throws IOException {
    String result = "";

    URL url = new URL(strURL);
    InputStream stream = null;

    // Open connection to URL.
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);

    // Connect to server.
    connection.connect();
    if (connection.getResponseCode() == 200) {
      stream = connection.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

      String tmp = reader.readLine();
      while (tmp != null) {
        result += tmp;
        tmp = reader.readLine();
      }
      reader.close();
    }
    if (result.equalsIgnoreCase("")) {
      return null;
    }
    return result;

  }
}
