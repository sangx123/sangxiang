package com.hubble.registration.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hubble.registration.PublicDefine;
import com.hubbleconnected.camera.R;

import java.util.ArrayList;
import java.util.List;
import com.hubbleconnected.camera.BuildConfig;

public class DeviceSelectionAdapter extends BaseAdapter {
  private static final String TAG = "DeviceSelectionAdapter";
  private Context mContext;
  private List<DeviceSelectionDevice> availableDevices;

  public DeviceSelectionAdapter(Context c) {
    mContext = c;
    availableDevices = new ArrayList<>();
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      availableDevices.add(new DeviceSelectionDevice(PublicDefine.CameraModel.VC921, R.drawable.default_cam));
      availableDevices.add(new DeviceSelectionDevice(PublicDefine.CameraModel.VC931, R.drawable.default_cam));
    } else {
      availableDevices.add(new DeviceSelectionDevice(PublicDefine.CameraModel.FOCUS66, R.drawable.focus_66_big));
      availableDevices.add(new DeviceSelectionDevice(PublicDefine.CameraModel.FOCUS73, R.drawable.focus_73_big));
      availableDevices.add(new DeviceSelectionDevice(PublicDefine.CameraModel.FOCUS83, R.drawable.focus_83_big));
      availableDevices.add(new DeviceSelectionDevice(PublicDefine.CameraModel.FOCUS86, R.drawable.focus_86_big));
    }
  }

  @Override
  public int getCount() {
    return availableDevices.size();
  }

  @Override
  public Object getItem(int arg0) {
    return availableDevices.get(arg0);
  }

  @Override
  public long getItemId(int arg0) {
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup arg2) {
    LinearLayout listItemHolder;
    /* Stat setting ups the view */
    if (convertView == null) { // if it's not recycled, initialize some attributes
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      listItemHolder = (LinearLayout) inflater.inflate(R.layout.list_item_device_selection, null);
    } else {
      listItemHolder = (LinearLayout) convertView;
    }

    TextView deviceName = (TextView) listItemHolder.findViewById(R.id.deviceSelection_cameraName);
    ImageView deviceIcon = (ImageView) listItemHolder.findViewById(R.id.deviceSelection_cameraImage);

    DeviceSelectionDevice currentDevice = availableDevices.get(position);

    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      deviceName.setVisibility(View.GONE);
    } else {
      deviceName.setVisibility(View.VISIBLE);
      deviceName.setText(currentDevice.getModelName());
    }

    deviceIcon.setImageResource(currentDevice.getImageResource());

    return listItemHolder;
  }

  class DeviceSelectionDevice {
    private PublicDefine.CameraModel model;
    private int imageResource;

    public DeviceSelectionDevice(PublicDefine.CameraModel model, int imageResource) {
      this.model = model;
      this.imageResource = imageResource;
    }

    public int getImageResource() {
      return imageResource;
    }

    public PublicDefine.CameraModel getModel() {
      return model;
    }

    private String getModelName() {
      switch (model) {
        case VC921:
          return "VTech 921";
        case VC931:
          return "VTech 931";
        case FOCUS66:
          return "Focus 66";
        case FOCUS73:
          return "Focus 73";
        case FOCUS83:
          return "Focus 83";
        case FOCUS86:
          return "Focus 86";
        default:
          return "";
      }
    }
  }
}
