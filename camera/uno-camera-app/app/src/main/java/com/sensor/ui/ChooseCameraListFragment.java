package com.sensor.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hubble.devcomm.Device;
import com.hubbleconnected.camera.R;
import com.sensor.constants.SensorConstants;
import com.sensor.helper.DeviceSensorDbo;

import java.util.List;

import base.hubble.database.DeviceProfile;

public class ChooseCameraListFragment extends android.support.v4.app.Fragment {

  private final String TAG = ChooseCameraListFragment.class.getSimpleName();
  private Activity activity;
  View view;
  private TextView mChooseCameraText;
  private ChooseCameraListAdapter mAdapter;
  private ListView mCameraListView;
  private List<Device> mDevices;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_choose_camera_list, container, false);

    mCameraListView = (ListView) findViewById(R.id.choose_camera_list);
    mChooseCameraText = (TextView) findViewById(R.id.choose_camera_text);

    mAdapter = new ChooseCameraListAdapter();
    if (mCameraListView.getAdapter() == null)
      mCameraListView.setAdapter(mAdapter);

    mChooseCameraText.setText(getString(R.string.found_camera_in_network_which_support_sensors, mDevices.size()));

    mCameraListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Device device = mDevices.get(position);
        List<DeviceProfile> mSensorProfiles = DeviceSensorDbo.getSensorByParentId(device.getProfile().getRegistrationId());
        if (mSensorProfiles != null && mSensorProfiles.size() < SensorConstants.NUMBER_OF_SENSORS_PER_CAMERA) {
          ((AddSensorActivity) activity).setSelectedCameraDevice(device);
          ((AddSensorActivity) activity).switchToSetupTagSensorFragment();
        } else {
          AlertDialog.Builder builder = new AlertDialog.Builder(
              new ContextThemeWrapper(activity, android.R.style.Theme_DeviceDefault_Light_Dialog));
          builder.setMessage(R.string.sorry_can_not_added_morethan_5);
          builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              //
            }
          });
          builder.create();
          builder.show();
        }
      }
    });

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.activity = activity;
  }

  private class ChooseCameraListAdapter extends BaseAdapter {

    public ChooseCameraListAdapter() {
    }

    @Override
    public int getCount() {
      return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder mViewHolder;
      if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.choose_camera_list_row, parent, false);
        mViewHolder = new ViewHolder();

        mViewHolder.mChooseCameraName = (TextView) convertView.findViewById(R.id.choose_camera_name);
        convertView.setTag(mViewHolder);
      } else {
        mViewHolder = (ViewHolder) convertView.getTag();
      }
      mViewHolder.mChooseCameraName.setText(mDevices.get(position).getProfile().getName());
      return convertView;
    }

    private class ViewHolder {
      public TextView mChooseCameraName;
    }
  }

  public void setDevices(List<Device> mDevices) {
    this.mDevices = mDevices;
  }

  public List<Device> getDevices() {
    return mDevices;
  }

  private View findViewById(int id) {
    return view.findViewById(id);
  }
}
