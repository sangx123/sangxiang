package com.hubble.registration.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.registration.models.CameraWifiEntry;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import base.hubble.PublicDefineGlob;

public class QueryCameraWifiListTask extends AsyncTask<String, String, List<CameraWifiEntry>> {

  public static final int NEW_APP_AVAILABLE = 1;
  public static final int NO_UPDATE = 2;

  private static final String MY_DEBUG_TAG = "mbp";
  private static final String TAG = "QueryCameraWifiListTask";

  private Context mContext;
  private Handler mHandler;
  private String fw_version = null;

  private boolean reportNoUpdate = false;
  private String model_id;
  private String registrationId;

  public QueryCameraWifiListTask(Context c, String fw_ver, String model_id, String registrationId) {
    mContext = c;
    mHandler = null;
    fw_version = fw_ver;
    this.model_id = model_id;
    this.registrationId = registrationId;
  }

  protected void onPreExecute() {

  }

  protected void onPostExecute(List<CameraWifiEntry> result) {

  }

  // Assumption: Camera is in Direct Mode - otherwise need to modify code to
  // support different ip

  /**
   * Called when the activity is first created.
   */
  protected List<CameraWifiEntry> doInBackground(String... params) {

    URL url = null;
    List<CameraWifiEntry> result = null;
    HttpURLConnection conn = null;
    int responseCode = -1;
    boolean shouldUseNewParser = true;
    DataInputStream inputStream = null;

		/* query current */
    String gatewayIp = null;
    // Son: use ip parameter if have
    if (params.length > 0) {
      gatewayIp = params[0];
    } else {
      gatewayIp = Util.getGatewayIp(mContext);
    }

    if (gatewayIp == null || gatewayIp.isEmpty()) {
      return null;
    }

    int ver_no_0 = -1;
    int ver_no_1 = -1;
    int ver_no_2 = -1;

    if (fw_version != null) {
      String[] fw_version_arr = fw_version.split("\\.");
      if (fw_version_arr != null && fw_version_arr.length == 3) {
        try {
          ver_no_0 = Integer.parseInt(fw_version_arr[0]);
          ver_no_1 = Integer.parseInt(fw_version_arr[1]);
          ver_no_2 = Integer.parseInt(fw_version_arr[2]);
        } catch (NumberFormatException e) {
          Log.e(TAG, Log.getStackTraceString(e));
        }
      }
    }

    String device_address_port = gatewayIp + ":" + PublicDefineGlob.DEVICE_PORT;
    String usr_pass = "camera:000000";
    String http_addr;
    if (shouldUseNewRouterListCmd(model_id)) {
      http_addr = String.format("%1$s%2$s%3$s", "http://", device_address_port,
              PublicDefine.HTTP_CMD_PART + PublicDefine.GET_RT_LIST);
      shouldUseNewParser = true;
    } else {
      if (ver_no_0 < 1 || (ver_no_0 == 1 && (ver_no_1 < 12 || (ver_no_1 == 12 && ver_no_2 < 58)))) {
        if (registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
          http_addr = String.format("%1$s%2$s%3$s", "http://", device_address_port,
                  PublicDefine.HTTP_CMD_PART + PublicDefine.GET_RT_LIST);
          shouldUseNewParser = true;
        } else {
          http_addr = String.format("%1$s%2$s%3$s", "http://", device_address_port,
                  PublicDefine.HTTP_CMD_PART + PublicDefine.GET_ROUTER_LIST);
          shouldUseNewParser = false;
        }
      } else {
        http_addr = String.format("%1$s%2$s%3$s", "http://", device_address_port,
                PublicDefine.HTTP_CMD_PART + PublicDefine.GET_RT_LIST);
        shouldUseNewParser = true;
      }
    }
    Log.d("mbp", "get wifi list cmd: " + http_addr);

		/* Create a new TextView to display the parsingresult later. */
    try {
      url = new URL(http_addr);
      conn = (HttpURLConnection) url.openConnection();

      conn.addRequestProperty("Authorization", "Basic " + Base64.encodeToString(usr_pass.getBytes("UTF-8"), Base64.NO_WRAP)
      );

      conn.setConnectTimeout(20000);
      conn.setReadTimeout(20000);
      responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
      }

			/* Get a SAXParser from the SAXPArserFactory. */
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */

      XMLReader xr = sp.getXMLReader();
      /* Create a new ContentHandler and apply it to the XML-Reader */
      XmlWifiListContentHandler myExampleHandler = new XmlWifiListContentHandler(shouldUseNewParser);
      xr.setContentHandler(myExampleHandler);

			/* Parse the xml-data from our URL. */
      xr.parse(new InputSource(inputStream));
      /* Parsing has finished. */

			/* Our ExampleHandler now provides the parsed data to us. */
      result = myExampleHandler.getParsedData();
    } catch (Exception e) {
      /* Display any Error to the GUI. */
      Log.e(TAG, Log.getStackTraceString(e));
      return null;
    }

    return result;
  }

  private boolean shouldUseNewRouterListCmd(String modelId) {
    boolean shouldUse = false;
    if (!TextUtils.isEmpty(modelId)) {
      if (modelId.equalsIgnoreCase(PublicDefine.MODEL_ID_MBP931) ||
              modelId.equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72)) {
        shouldUse = true;
      }
    }
    return shouldUse;
  }
}