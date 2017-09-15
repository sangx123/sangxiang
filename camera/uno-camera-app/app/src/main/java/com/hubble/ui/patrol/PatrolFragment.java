/*package com.hubble.ui.patrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hubble.Patrolling;
import com.hubble.actors.Actor;
import com.hubble.controllers.CameraController;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.localytics.AppErrorEvent;
import com.hubble.registration.EScreenName;
import com.hubble.registration.AnalyticsController;
import com.blinkhd.R;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.nxcomm.blinkhd.ui.blur.Blur;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PatrolFragment extends Fragment {

  private ListView mListView;
  private List<Device> cameras = new ArrayList<Device>();
  private PatrolListArrayAdapter adapter;
  private ImageView mBackgroundImage;
  private TextView mCurrentInterval;
  public static final String TAG = "PatrolFragment";
  private EventData eventData;
  private class RefreshCameraList {
    boolean fromCache = false;

    RefreshCameraList(boolean cached) {
      fromCache = cached;
    }
  }

  private Actor actor = new Actor() {

    @Override
    public Object receive(Object m) {
      if (m instanceof RefreshCameraList) {
        List<Device> devices = DeviceSingleton.getInstance().getOnlineDevices();
        cameras = devices;
        refreshCameraList(devices);
      }
      return null;
    }
  };

  public static PatrolFragment newInstance() {
    PatrolFragment fragment = new PatrolFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public PatrolFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    eventData = new EventData();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_patrol, container, false);
    mBackgroundImage = (ImageView) view.findViewById(R.id.patrol_fragment_background_image);
    mListView = (ListView) view.findViewById(R.id.patrol_listview);
    mCurrentInterval = (TextView) view.findViewById(R.id.patrolCurrentInterval);

    setupIntervalLengthString();

    mCurrentInterval.setSelected(true);

    List<Device> result = DeviceSingleton.getInstance().getDevices();
    Blur.blurImageBackground(mBackgroundImage, getActivity().getApplicationContext(), result);

    //Query the server for cameras,
    actor.send(new RefreshCameraList(true));
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    //AA-1532
    AppErrorEvent.getInstance().setScreen(AppErrorEvent.Screen.PATROL);
    //AA-1480
    AnalyticsController.getInstance().trackScreen(EScreenName.Patrol);
    AnalyticsInterface.getInstance().trackEvent("Patrol","Patrol",eventData);
  }

  private void setupIntervalLengthString() {
    int patrollingInterval = Patrolling.getPatrollingDelay(getActivity());

    if (patrollingInterval < 60) {
      mCurrentInterval.setText(String.format(getActivity().getString(R.string.current_interval_blank_seconds), Patrolling.getPatrollingDelay(getActivity())));
    } else if (patrollingInterval >= 60 && patrollingInterval < 120) {
      mCurrentInterval.setText(getActivity().getString(R.string.current_interval_one_minute));
    } else {
      mCurrentInterval.setText(String.format(getActivity().getString(R.string.current_interval_blank_minutes), patrollingInterval / 60));
    }

    mCurrentInterval.setSelected(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.clear();
    inflater.inflate(R.menu.patrol_actionbar_items, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.start_patrol_actionbar_button: {
        List<Device> enabledCameras = filterDisabledCameras();
        if (enabledCameras.size() > 1) {
          CameraController.switchToPatrollingMode((MainActivity) getActivity(), enabledCameras);
        } else {
          Toast.makeText(getActivity(), getActivity().getString(R.string.select_at_least_two_cameras_to_patrol), Toast.LENGTH_SHORT).show();
        }
        return true;
      }
      case R.id.patrol_menu_set_delay_button: {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.set_patrol_delay));
        String[] delayOptions = {
            getActivity().getString(R.string.fifteen_seconds),
            getActivity().getString(R.string.thirty_seconds),
            getActivity().getString(R.string.one_minute),
            getActivity().getString(R.string.five_minutes)
        };
        builder.setItems(delayOptions, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // // Log.d(TAG, "Position:" + i);
            switch (i) {
              case 0:
                Patrolling.savePatrollingDelayPref(getActivity(), 15);
                break;
              case 1:
                Patrolling.savePatrollingDelayPref(getActivity(), 30);
                break;
              case 2:
                Patrolling.savePatrollingDelayPref(getActivity(), 60);
                break;
              case 3:
                Patrolling.savePatrollingDelayPref(getActivity(), 300);
                break;
              default:
                break;
            }
            dialogInterface.dismiss();
            setupIntervalLengthString();
          }
        });
        builder.show();
      }
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private List<Device> filterDisabledCameras() {
    List<Device> lcp = new ArrayList<Device>();
    String[] arr_enabled = Patrolling.getCameraIdsFromNamedPref(getActivity(), "patrolling_enabled_cameras");
    List<String> enabledCameraIds = Arrays.asList(arr_enabled);
    for (Device cp : cameras) {
      if (enabledCameraIds.contains(cp.getProfile().getRegistrationId())) {
        if (cp.getProfile().isAvailable()) {
          lcp.add(cp);
        }
      }
    }
    return lcp;
  }

  private void refreshCameraList(final List<Device> devices) {
    Activity activity = getActivity();
    if (activity != null) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          adapter = new PatrolListArrayAdapter(getActivity(), devices);
          mListView.setAdapter(adapter);
          adapter.notifyDataSetChanged();
        }
      });
    }
  }

}*/
