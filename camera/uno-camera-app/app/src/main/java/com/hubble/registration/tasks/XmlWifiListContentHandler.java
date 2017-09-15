package com.hubble.registration.tasks;

import android.text.TextUtils;

import com.hubble.registration.models.CameraWifiEntry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;


public class XmlWifiListContentHandler extends DefaultHandler {

  // ===========================================================
  // Fields
  // ===========================================================

  private boolean _wifi_list = false;
  private boolean _num_entries = false;
  private boolean _wifi_entry = false;
  private boolean _wifi_entry_ssid = false;
  private boolean _wifi_entry_auth_mode = false;
  private boolean _wifi_entry_quality = false;
  private boolean _wifi_entry_signal_level = false;
  private boolean _wifi_entry_noise_level = false;
  private boolean _wifi_entry_channel = false;


  /// Element names

  //Element Name:
  private static final String WIFI_LIST_VERSION = "wifi_list";
  private static final String WIFI_LIST_VERSION_ATT = "version";
  private static final String NUM_ENTRY = "num_entries";
  private static final String WIFI_ENTRY = "wifi";
  private static final String WIFI_ENTRY_SSID = "ssid";
  private static final String WIFI_ENTRY_BSSID = "bssid";
  private static final String WIFI_ENTRY_AUTH_MODE = "auth_mode";
  private static final String WIFI_ENTRY_QUALITY = "quality";
  private static final String WIFI_ENTRY_SIGNAL_LEVEL = "signal_level";
  private static final String WIFI_ENTRY_NOISE_LEVEL = "noise_level";
  private static final String WIFI_ENTRY_CHANNEL = "channel";


  //new XML tag
  private static final String WIFI_LIST_VERSION_2 = "wl";
  private static final String WIFI_LIST_VERSION_ATT_2 = "v";
  private static final String NUM_ENTRY_2 = "n";
  private static final String WIFI_ENTRY_2 = "w";
  private static final String WIFI_ENTRY_SSID_2 = "s";
  private static final String WIFI_ENTRY_BSSID_2 = "b";
  private static final String WIFI_ENTRY_AUTH_MODE_2 = "a";
  private static final String WIFI_ENTRY_QUALITY_2 = "q";
  private static final String WIFI_ENTRY_SIGNAL_LEVEL_2 = "si";
  private static final String WIFI_ENTRY_NOISE_LEVEL_2 = "nl";
  private static final String WIFI_ENTRY_CHANNEL_2 = "ch";

  private boolean shouldUseNewParser = true;

  private ArrayList<CameraWifiEntry> cameraWifiList = new ArrayList<CameraWifiEntry>();

  // ===========================================================
  // Getter & Setter
  // ===========================================================

  public XmlWifiListContentHandler(boolean useNewParser) {
    // TODO Auto-generated constructor stub
    shouldUseNewParser = useNewParser;
  }

  public ArrayList<CameraWifiEntry> getParsedData() {
    return cameraWifiList;
  }

  // ===========================================================
  // Methods
  // ===========================================================
  @Override
  public void startDocument() throws SAXException {
    cameraWifiList = new ArrayList<CameraWifiEntry>();
  }

  @Override
  public void endDocument() throws SAXException {
    // Nothing to do
  }

  /**
   * Gets be called on opening tags like:
   * <tag>
   * Can provide attribute(s), when xml was like:
   * <tag attribute="attributeValue">
   */
  @Override
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

    if (shouldUseNewParser) {
      //new parser
      if (qName.equalsIgnoreCase(WIFI_LIST_VERSION_2)) {
        _wifi_list = true;
      } else if (qName.equalsIgnoreCase(NUM_ENTRY_2)) {
        _num_entries = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_2)) {
        _wifi_entry = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_AUTH_MODE_2)) {
        _wifi_entry_auth_mode = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_BSSID_2)) {

      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_SSID_2)) {
        _wifi_entry_ssid = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_SIGNAL_LEVEL_2)) {
        _wifi_entry_signal_level = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_NOISE_LEVEL_2)) {
        _wifi_entry_noise_level = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_CHANNEL_2)) {
        _wifi_entry_channel = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_QUALITY_2)) {
        _wifi_entry_quality = true;
      }
    } else {
      if (qName.equalsIgnoreCase(WIFI_LIST_VERSION)) {
        _wifi_list = true;
      } else if (qName.equalsIgnoreCase(NUM_ENTRY)) {
        _num_entries = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY)) {
        _wifi_entry = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_AUTH_MODE)) {
        _wifi_entry_auth_mode = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_BSSID)) {

      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_SSID)) {
        _wifi_entry_ssid = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_SIGNAL_LEVEL)) {
        _wifi_entry_signal_level = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_NOISE_LEVEL)) {
        _wifi_entry_noise_level = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_CHANNEL)) {
        _wifi_entry_channel = true;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_QUALITY)) {
        _wifi_entry_quality = true;
      }
    }
  }

  /**
   * Gets be called on closing tags like:
   * </tag>
   */
  @Override
  public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

    if (shouldUseNewParser) {
      if (qName.equalsIgnoreCase(WIFI_LIST_VERSION_2)) {
        _wifi_list = false;
      } else if (qName.equalsIgnoreCase(NUM_ENTRY_2)) {
        _num_entries = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_2)) {
        _wifi_entry = false;

        if (wifiEntry != null) {
          cameraWifiList.add(wifiEntry);
        }

        wifiEntry = null;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_AUTH_MODE_2)) {
        _wifi_entry_auth_mode = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_BSSID_2)) {

      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_SSID_2)) {
        _wifi_entry_ssid = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_SIGNAL_LEVEL_2)) {
        _wifi_entry_signal_level = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_NOISE_LEVEL_2)) {
        _wifi_entry_noise_level = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_CHANNEL_2)) {
        _wifi_entry_channel = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_QUALITY_2)) {
        _wifi_entry_quality = false;
      }
    } else {
      if (qName.equalsIgnoreCase(WIFI_LIST_VERSION)) {
        _wifi_list = false;
      } else if (qName.equalsIgnoreCase(NUM_ENTRY)) {
        _num_entries = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY)) {
        _wifi_entry = false;

        if (wifiEntry != null) {
          cameraWifiList.add(wifiEntry);
        }

        wifiEntry = null;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_AUTH_MODE)) {
        _wifi_entry_auth_mode = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_BSSID)) {

      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_SSID)) {
        _wifi_entry_ssid = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_SIGNAL_LEVEL)) {
        _wifi_entry_signal_level = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_NOISE_LEVEL)) {
        _wifi_entry_noise_level = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_CHANNEL)) {
        _wifi_entry_channel = false;
      } else if (qName.equalsIgnoreCase(WIFI_ENTRY_QUALITY)) {
        _wifi_entry_quality = false;
      }
    }

  }

  private CameraWifiEntry wifiEntry;

  /**
   * Gets be called on the following structure:
   * <tag>characters</tag>
   */
  @Override
  public void characters(char ch[], int start, int length) {

    String data = new String(ch, start, length);

    if (_num_entries) {
      cameraWifiList.ensureCapacity(Integer.parseInt(data));
    } else if (_wifi_entry_ssid) {
      if (wifiEntry != null) {
        data = wifiEntry.getSsid().concat(data);
        wifiEntry.setSsid(data);
      } else {
        wifiEntry = new CameraWifiEntry(data);
      }
    } else if (_wifi_entry_auth_mode) {
      if (wifiEntry != null) {
        wifiEntry.setAuth_mode(data);
      }
    } else if (_wifi_entry_quality) {
      if (wifiEntry != null && !TextUtils.isEmpty(data)) {
        wifiEntry.setQuality(data);
      }
    } else if (_wifi_entry_signal_level) {
      if (wifiEntry != null && !TextUtils.isEmpty(data) && TextUtils.isDigitsOnly(data)) {
        wifiEntry.setSignal_level(Integer.parseInt(data));
      }
    } else if (_wifi_entry_noise_level) {
      if (wifiEntry != null && !TextUtils.isEmpty(data) && TextUtils.isDigitsOnly(data)) {
        wifiEntry.setNoise_level(Integer.parseInt(data));
      }
    } else if (_wifi_entry_channel) {
      if (wifiEntry != null && !TextUtils.isEmpty(data) && TextUtils.isDigitsOnly(data)) {
        wifiEntry.setChannel(Integer.parseInt(data));
      }
    }


    //20121212: phung: for now we only need to know IF the camera
    //                 can 'see' the ssid, thus just need SSID.
    //               TODO: full parsing of the xml file if needed.


  }
}