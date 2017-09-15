package com.hubble.ui.patrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.hubble.Patrolling;
import com.hubble.devcomm.Device;
import com.hubbleconnected.camera.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dan on 30/07/14.
 */
public class PatrolListArrayAdapter extends ArrayAdapter<Device> {


  private final List<Device> cameras;
  private final Context ctx;
  private List<Device> enabledCameras = new ArrayList<Device>();

  public PatrolListArrayAdapter(Context ctx, List<Device> cameras) {
    super(ctx, R.layout.patrol_list_item, R.id.patrol_item_text, cameras);
    this.ctx = ctx;
    this.cameras = cameras;
  }

  public List<Device> getEnabledCameras() {
    return enabledCameras;
  }


  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.patrol_list_item, parent, false);
    TextView ctv = (TextView) layout.findViewById(R.id.patrol_item_text);
    ImageView thumb = (ImageView) layout.findViewById(R.id.camera_thumbnail);

    final Device item = cameras.get(position);
    final Switch aSwitch = (Switch) layout.findViewById(R.id.patrol_item_switch);

    String thumbURLString = item.getProfile().getSnapshotUrl();
    if (thumbURLString != null && thumbURLString.equals("")) {
      // URL cannot be an empty String
      thumbURLString = null;
    }

    Picasso.with(ctx).load(thumbURLString).placeholder(R.drawable.default_cam).error(R.drawable.default_cam).into(thumb);

    thumb.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        aSwitch.toggle();
      }
    });
    // enabled in prefs

    if (isChecked(item)) {
      aSwitch.setChecked(true);
      enabledCameras.add(item);
    }

    aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        // // Log.d("PatrolListArrayAdapter", "onCheckedChanged()");
        if (compoundButton.isChecked()) {
          enabledCameras.add(item);
        } else {
          enabledCameras.remove(item);
        }
        Patrolling.saveCameraIdsAsNamedPref(ctx, "patrolling_enabled_cameras", enabledCameras);
      }
    });

    ctv.setText(item.getProfile().getName());

    return layout;
  }

  private boolean isChecked(Device cp) {
    String[] enabledCameraPreferences =
        Patrolling.getCameraIdsFromNamedPref(ctx, "patrolling_enabled_cameras");
    List<String> camPrefs = Arrays.asList(enabledCameraPreferences);
    return camPrefs.contains(cp.getProfile().getRegistrationId());
  }


  @Override
  public int getCount() {
    return cameras.size();
  }
}
