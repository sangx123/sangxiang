package com.hubble.registration.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hubble.registration.models.NameAndSecurity;
import com.hubbleconnected.camera.R;

import java.util.ArrayList;

public class SensorAccessPointAdapter extends BaseAdapter {

  private Context mContext;
  private ArrayList<NameAndSecurity> ap_list;
  private int selectedPosition = -1;

  public SensorAccessPointAdapter(Context c, ArrayList<NameAndSecurity> ap_list) {
    mContext = c;
    this.ap_list = ap_list;
  }

  @Override
  public int getCount() {
    if (ap_list != null)
    //return ap_list.size() + 1;// extra item for "Add wifi network" option
    {
      return ap_list.size();
    } else {
      return 0;
    }
  }

  @Override
  public Object getItem(int arg0) {
    if (arg0 >= 0 && arg0 < ap_list.size()) {
      return ap_list.get(arg0);
    } else {
      return null;
    }
  }

  @Override
  public long getItemId(int arg0) {
    return 0;
  }

  public void setSelectedPositision(int position) {
    selectedPosition = position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup arg2) {
    LinearLayout itemView;
    NameAndSecurity ap;

    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      itemView = (LinearLayout) inflater.inflate(R.layout.sensor_access_point_list_item_w_rssid, null);
    } else {
      itemView = (LinearLayout) convertView;
    }

    ap = ap_list.get(position);
    itemView.setVisibility(View.VISIBLE);

    TextView text = (TextView) itemView.findViewById(R.id.AccessPointItem);
    if (text != null) {
      text.setText(ap.toString());
      text.setSelected(true);
    }

    return itemView;
  }

}
