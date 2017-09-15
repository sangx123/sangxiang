package com.hubble.registration.ui;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.Iterables;
import com.hubble.HubbleApplication;
import com.hubble.registration.models.ApScanBase;
import com.hubble.registration.models.CameraBonjourInfo;
import com.hubble.registration.models.NameAndSecurity;
import com.hubbleconnected.camera.R;

import java.util.ArrayList;
import java.util.List;

import base.hubble.PublicDefineGlob;

public class SensorAccessPointListAdapter extends BaseAdapter {
  private static final String TAG = "CameraAccessPoint";
  private Context mContext;
  private List<ApScanBase> apList = new ArrayList<ApScanBase>();
  private List<ApScanBase> wifiList = new ArrayList<ApScanBase>();
  private List<ApScanBase> mdnsList = new ArrayList<ApScanBase>();
  private int selectedPosition = -1;

  public SensorAccessPointListAdapter(Context c, List<ApScanBase> ap_list) {
    mContext = c;
    this.apList = ap_list;
  }

  public boolean setWifiResults(List<ApScanBase> wifiResults) {
    boolean shouldUpdateList = wifiList == null || !Iterables.elementsEqual(wifiResults, wifiList);
    if (shouldUpdateList) {
      wifiList = wifiResults;
      combineLists();
    }
    return shouldUpdateList;
  }

  public boolean setMdnsResults(List<ApScanBase> mdnsResults) {
    boolean shouldUpdateList = mdnsList == null || !Iterables.elementsEqual(mdnsList, mdnsResults);
    if (shouldUpdateList) {
      boolean is931LanAvailable = HubbleApplication.AppConfig.getBoolean("enable_931_lan", false);
      if (!is931LanAvailable) { // if debug option is disabled, filter out 931s from LAN results
        for (ApScanBase device : mdnsResults) {
          if (getDeviceModel(((CameraBonjourInfo) device).getCameraName()).equals("0931")) { // Filter out the 931s from the LAN results
            mdnsResults.remove(device);
          }
        }
      }
      mdnsList = mdnsResults;
      combineLists();
    }
    return shouldUpdateList;
  }

  public void combineLists() {
    apList.clear();
    apList.addAll(wifiList);
    apList.addAll(mdnsList);
  }

  @Override
  public int getCount() {
    if (apList != null) {
      return apList.size();
    } else {
      return 0;
    }
  }

  @Override
  public Object getItem(int arg0) {
    if (arg0 >= 0 && arg0 < apList.size()) {
      return apList.get(arg0);
    } else {
      return null;
    }
  }

  @Override
  public long getItemId(int arg0) {
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup arg2) {
    LinearLayout listItemHolder;
    NameAndSecurity ap;

    Object obj = getItem(position);
    /* Stat setting ups the view */
    if (convertView == null) { // if it's not recycled, initialize some attributes
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      listItemHolder = (LinearLayout) inflater.inflate(R.layout.sensor_access_point_list_item, null);
    } else {
      listItemHolder = (LinearLayout) convertView;
    }

    TextView deviceName = (TextView) listItemHolder.findViewById(R.id.AccessPointItem);
    ImageView deviceIcon = (ImageView) listItemHolder.findViewById(R.id.imgCamera);

    if (obj instanceof NameAndSecurity) {
      ap = (NameAndSecurity) apList.get(position);
      if (ap == null) {
        listItemHolder.setClickable(false);
        listItemHolder.setVisibility(View.GONE);
        return listItemHolder;
      }

      listItemHolder.setVisibility(View.VISIBLE);
      deviceName.setText(Html.fromHtml("<b>" + ap.toString() + "</b>" + " - WiFi"));
//      setDeviceImage(deviceIcon, ap.toString());
    } else if (obj instanceof CameraBonjourInfo) {
      CameraBonjourInfo info = (CameraBonjourInfo) apList.get(position);
      if (info == null) {
        listItemHolder.setClickable(false);
        listItemHolder.setVisibility(View.GONE);
        return listItemHolder;
      }

      listItemHolder.setVisibility(View.VISIBLE);

      deviceName.setText(Html.fromHtml("<b>" + info.getCameraName() + "</b>" + " - LAN"));
//      setDeviceImage(deviceIcon, info.getCameraName());
    }
    deviceName.setSelected(true);
    return listItemHolder;
  }

  private void setDeviceImage(ImageView deviceIcon, String deviceName) {
    String model = getDeviceModel(deviceName);
    switch (model) {
      case "0073":
        deviceIcon.setImageResource(R.drawable.focus_73);
        break;
      case "0083":
      case "0084":
      case "0085":
      case "0854":
        deviceIcon.setImageResource(R.drawable.focus_83);
        break;
      case "1662":
      case "0662":
      case "0066":
        deviceIcon.setImageResource(R.drawable.focus_66);
        break;
      case "0086":
        deviceIcon.setImageResource(R.drawable.focus_86);
        break;
      case "0931":
        deviceIcon.setImageResource(R.drawable.default_no_brand_cam);
        break;
      case "0921":
        deviceIcon.setImageResource(R.drawable.default_no_brand_cam);
        break;
      default:
        deviceIcon.setImageResource(R.drawable.default_no_brand_cam);
        break;
    }
  }

  private String getDeviceModel(String deviceName) {
    if (deviceName.startsWith(PublicDefineGlob.DEFAULT_VTECH_SSID_HD)) {
      return "0" + deviceName.substring(2, 5);
    } else if (deviceName.startsWith(PublicDefineGlob.DEFAULT_SSID_HD)) {
      return deviceName.substring(9, 13);
    } else {
      return "";
    }
  }

  public void clear() {
    apList.clear();
  }
}
