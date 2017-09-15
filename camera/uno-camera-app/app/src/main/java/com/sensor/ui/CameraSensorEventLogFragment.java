package com.sensor.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceFactory;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.registration.EScreenName;
import com.hubble.registration.AnalyticsController;

import com.nxcomm.blinkhd.ui.Global;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.sensor.constants.SensorConstants;
import com.sensor.helper.DeviceSensorDbo;

import java.util.ArrayList;
import java.util.List;

import base.hubble.PublicDefineGlob;
import base.hubble.database.DeviceProfile;
import com.hubbleconnected.camera.R;

/**
 * This class will have a sensor list spinner for a Camera and it shows the events for selected sensors
 * and it has the options menu which will have list of sensors and add moto tag button
 */
public class CameraSensorEventLogFragment extends Fragment {

  private Activity activity;
  private View view;
  private Device mSelectedCamera;
  private List<String> mEventsTypeList;
  private List<Device> mDeviceList;
  private DeviceFactory mDeviceFactory;
  private String apiKey;
  //private EventLogFragmentJava mEventFragment;
  private Spinner mCameraSensorListSpinner;
  private ArrayAdapter<String> mSensorListAdapter;
  private AlertDialog mAlertDialog;
  private PackageManager mPkgManager;
  private EventData eventData;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_camera_sensor_event_log, container, false);
    setHasOptionsMenu(true);
    eventData = new EventData();
    if (mSelectedCamera == null)
      mSelectedCamera = DeviceSingleton.getInstance().getSelectedDevice();
    apiKey = Global.getApiKey(activity.getApplicationContext());
    mDeviceFactory = new DeviceFactory(apiKey, activity);

    mCameraSensorListSpinner = (Spinner) findViewById(R.id.events_dropdown_spinner);

    mCameraSensorListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
          Device selectedDevice = mDeviceList.get(position);
          ((MainActivity) getActivity()).goToEventLog(selectedDevice);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    switchToEventFragment(mSelectedCamera);
    activity.invalidateOptionsMenu();
    return view;
  }

  public void updateSpinnerRemotely() {
    updateSensorListSpinner();
  }

  private void updateSensorListSpinner() {
    List<DeviceProfile> mSensorProfiles = DeviceSensorDbo.getSensorByParentId(mSelectedCamera.getProfile().getRegistrationId());

    if (mEventsTypeList == null)
      mEventsTypeList = new ArrayList<>();
    else
      mEventsTypeList.clear();

    if (mDeviceList == null)
      mDeviceList = new ArrayList<>();
    else
      mDeviceList.clear();

    if (mSensorListAdapter == null)
      mSensorListAdapter = new ArrayAdapter<String>(activity,
          android.R.layout.simple_spinner_item, mEventsTypeList);
    else
      mSensorListAdapter.notifyDataSetChanged();

    mEventsTypeList.add(activity.getString(R.string.camera_timeline));
    mDeviceList.add(mSelectedCamera);


    for (DeviceProfile mDeviceProfile : mSensorProfiles) {
      mEventsTypeList.add(mDeviceProfile.getName());
      Device device = mDeviceFactory.build(mDeviceProfile);
      mDeviceList.add(device);
    }

    if (mSensorProfiles != null && mSensorProfiles.size() > 0) {
      mCameraSensorListSpinner.setVisibility(View.VISIBLE);
      mSensorListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      mCameraSensorListSpinner.setAdapter(mSensorListAdapter);
    } else {
      mCameraSensorListSpinner.setVisibility(View.GONE);
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.mPkgManager = activity.getPackageManager();
    this.activity = activity;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.clear();
    inflater.inflate(R.menu.menu_camera_sensor, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    if (mSelectedCamera == null)
      mSelectedCamera = DeviceSingleton.getInstance().getSelectedDevice();
    MenuItem connectedSensor = menu.findItem(R.id.menu_show_sensor_list);
    if (connectedSensor != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        boolean shouldShowSensorIcon;
        if (mPkgManager != null) {
          shouldShowSensorIcon = mSelectedCamera.getProfile().doesSupportBleTag() &&
              mPkgManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        } else {
          shouldShowSensorIcon = mSelectedCamera.getProfile().doesSupportBleTag();
        }

        if (shouldShowSensorIcon) {
          connectedSensor.setVisible(true);
        } else {
          connectedSensor.setVisible(false);
        }
      } else {
        connectedSensor.setVisible(false);
      }
    }
    super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_show_sensor_list:
        List<DeviceProfile> mSensorProfiles = DeviceSensorDbo.getSensorByParentId(mSelectedCamera.getProfile().getRegistrationId());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_connected_mototags, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.connected_mototags);
        mAlertDialog = builder.create();
        ListView listView = (ListView) dialogView.findViewById(R.id.listView_connected_mototags);
        List<Item> itemList = new ArrayList<Item>();

        for (int i = 0; i < mSensorProfiles.size(); i++) {
          DeviceProfile mDeviceProfile = mSensorProfiles.get(i);
          itemList.add(new Item(mDeviceProfile.getName(), mDeviceProfile.getMode()));
        }
        ConnectedMototagsListAdapter connectedMototagsListAdapter = new ConnectedMototagsListAdapter(getActivity(), R.layout.item_in_list_dialog_connected_mototags, itemList);
        WindowManager.LayoutParams wmlp = mAlertDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.RIGHT;

        if (mSensorProfiles.size() < 5) {
          View footerView = inflater.inflate(R.layout.listview_footer_connected_mototags_dialog, null);
          footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              mAlertDialog.dismiss();
              if (getParentFragment() != null) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AddSensorActivity.class);
                intent.putExtra("isCameraDetail", true);
                getParentFragment().startActivityForResult(intent, PublicDefineGlob.REGISTER_SENSOR_ACTIVITY_REQUEST);
              } else {
                Intent intent = new Intent(getActivity().getApplicationContext(), AddSensorActivity.class);
                intent.putExtra("isCameraDetail", true);
                startActivityForResult(intent, PublicDefineGlob.REGISTER_SENSOR_ACTIVITY_REQUEST);
              }

            }
          });
          listView.addFooterView(footerView);
        }

        listView.setAdapter(connectedMototagsListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Device device = mDeviceList.get(position + 1);
            ((MainActivity) getActivity()).switchToSensorDetailFragment(device);
            mAlertDialog.dismiss();
          }
        });
        mAlertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mAlertDialog.setCanceledOnTouchOutside(true);
        mAlertDialog.show();
        return true;
      /*case R.id.menu_camera_delete_all:
        mEventFragment.alertDeleteAllEvents();
        break;*/
      case R.id.menu_edit_camera_sensor:
        ((MainActivity) getActivity()).goToCameraEditEventLog(mSelectedCamera);
        break;
      default:
        break;
    }
    return false;
  }

  public void dismissSensorListDialogue() {
    if (mAlertDialog != null)
      mAlertDialog.dismiss();
  }

  private void switchToEventFragment(Device selectedDevice) {
    /*mEventFragment = new EventLogFragmentJava();
    Bundle b = new Bundle();
    b.putString(PublicDefineGlob.EXTRA_API_KEY, apiKey);
    DeviceSingleton.getInstance().setSelectedDevice(selectedDevice);
    mEventFragment.setUseSelectedDevice(true);
    mEventFragment.setArguments(b);
    switchLowerFragmentTo(mEventFragment);*/
  }

  private void switchLowerFragmentTo(Fragment fragment) {
    if (fragment != null) {
      FragmentTransaction fragTrans = getChildFragmentManager().beginTransaction();
      try {
        fragTrans.replace(R.id.camera_sensor_event_holder, fragment);
        fragTrans.commitAllowingStateLoss();
      } catch (Exception e) {
        Toast.makeText(activity, activity.getString(R.string.an_error), Toast.LENGTH_SHORT).show();
        switchToCameraListFragment();
      }

    }
  }

  private View findViewById(int id) {
    return view.findViewById(id);
  }

  private void switchToCameraListFragment() {
    MainActivity mainActivity = (MainActivity) activity;
    mainActivity.switchToDeviceList();
  }

  public class ConnectedMototagsListAdapter extends ArrayAdapter<Item> {

    public ConnectedMototagsListAdapter(Context context, int resource, List<Item> items) {
      super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      View view = convertView;

      if (view == null) {
        LayoutInflater layoutInflater;
        layoutInflater = LayoutInflater.from(getContext());
        view = layoutInflater.inflate(R.layout.item_in_list_dialog_connected_mototags, null);
      }

      Item mItem = getItem(position);

      if (mItem != null) {
        TextView mNameTextView = (TextView) view.findViewById(R.id.textView_item_list_dialog_connected_mototag_name);
        ImageView mIconImageView = (ImageView) view.findViewById(R.id.imageView_item_list_dialog_connected_mototag_icon);

        if (mNameTextView != null)
          mNameTextView.setText(mItem.getName());

        if (mIconImageView != null)
          mIconImageView.setImageResource(mItem.getImageResourceId());
      }
      return view;
    }
  }

  private class Item {
    String mName;
    int mImgResourceId;

    public Item(String mName, String mode) {
      this.mName = mName;
      switch (mode) {
        case SensorConstants.PRESENCE_DETECTION:
          mImgResourceId = R.drawable.icon_proximitysensor_white;
          break;
        case SensorConstants.MOTION_DETECTION:
          mImgResourceId = R.drawable.icon_doormotionsensor_white;
      }

    }

    public String getName() {
      return mName;
    }

    public int getImageResourceId() {
      return mImgResourceId;
    }
  }

  public void setmSelectedCamera(Device mSelectedCamera) {
    this.mSelectedCamera = mSelectedCamera;
  }

  @Override
  public void onResume() {
    super.onResume();
    activity.invalidateOptionsMenu();
    updateSensorListSpinner();
    //AA-1480
    AnalyticsController.getInstance().trackScreen(EScreenName.Timeline);
    AnalyticsInterface.getInstance().trackEvent("Timeline","Timeline",eventData);
  }

  public void actionMenuItemMtag(){
    List<DeviceProfile> mSensorProfiles = DeviceSensorDbo.getSensorByParentId(mSelectedCamera.getProfile().getRegistrationId());
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();

    View dialogView = inflater.inflate(R.layout.dialog_connected_mototags, null);
    builder.setView(dialogView);
    builder.setTitle(R.string.connected_mototags);
    mAlertDialog = builder.create();
    ListView listView = (ListView) dialogView.findViewById(R.id.listView_connected_mototags);
    List<Item> itemList = new ArrayList<Item>();

    for (int i = 0; i < mSensorProfiles.size(); i++) {
      DeviceProfile mDeviceProfile = mSensorProfiles.get(i);
      itemList.add(new Item(mDeviceProfile.getName(), mDeviceProfile.getMode()));
    }
    ConnectedMototagsListAdapter connectedMototagsListAdapter = new ConnectedMototagsListAdapter(getActivity(), R.layout.item_in_list_dialog_connected_mototags, itemList);
    WindowManager.LayoutParams wmlp = mAlertDialog.getWindow().getAttributes();
    wmlp.gravity = Gravity.TOP | Gravity.RIGHT;

    if (mSensorProfiles.size() < 5) {
      View footerView = inflater.inflate(R.layout.listview_footer_connected_mototags_dialog, null);
      footerView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mAlertDialog.dismiss();
          if (getParentFragment() != null) {
            Intent intent = new Intent(getActivity().getApplicationContext(), AddSensorActivity.class);
            intent.putExtra("isCameraDetail", true);
            getParentFragment().startActivityForResult(intent, PublicDefineGlob.REGISTER_SENSOR_ACTIVITY_REQUEST);
          } else {
            Intent intent = new Intent(getActivity().getApplicationContext(), AddSensorActivity.class);
            intent.putExtra("isCameraDetail", true);
            startActivityForResult(intent, PublicDefineGlob.REGISTER_SENSOR_ACTIVITY_REQUEST);
          }

        }
      });
      listView.addFooterView(footerView);
    }

    listView.setAdapter(connectedMototagsListAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Device device = mDeviceList.get(position + 1);
        ((MainActivity) getActivity()).switchToSensorDetailFragment(device);
        mAlertDialog.dismiss();
      }
    });
    mAlertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    mAlertDialog.setCanceledOnTouchOutside(true);
    mAlertDialog.show();
  }
}
