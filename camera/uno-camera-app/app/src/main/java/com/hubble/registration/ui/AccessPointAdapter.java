package com.hubble.registration.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hubble.registration.models.NameAndSecurity;
import com.hubbleconnected.camera.R;

import java.util.ArrayList;

public class AccessPointAdapter extends BaseAdapter {

  private Context mContext;
  private ArrayList<NameAndSecurity> ap_list;
  private int selectedPosition = -1;

  public AccessPointAdapter(Context c, ArrayList<NameAndSecurity> ap_list) {
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
  public NameAndSecurity getItem(int arg0) {
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
    NameAndSecurity ap;
    ViewHolder holder;
    if (convertView == null) {
      convertView = LayoutInflater.from(mContext).inflate(R.layout.bb_access_point_list_item_w_rssid, arg2, false);
      holder = new ViewHolder();
      holder.root = (LinearLayout) convertView.findViewById(R.id.layout_root);
      holder.wifiName = (TextView) convertView.findViewById(R.id.AccessPointItem);
      holder.wifiStrength = (ImageView) convertView.findViewById(R.id.imageSS);
      holder.checkbox = (ImageView) convertView.findViewById(R.id.imgChecked);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    if (holder.checkbox != null) {
      int state = (position == selectedPosition) ? View.VISIBLE : View.INVISIBLE;
      holder.checkbox.setVisibility(state);
    }

    ap = ap_list.get(position);
    if (holder.root != null) {
      holder.root.setVisibility(View.VISIBLE);
    }

    if (holder.wifiName != null) {
      holder.wifiName.setText(ap.toString());
      holder.wifiName.setSelected(true);
    }

    if (holder.wifiStrength != null) {
      int imgRes = getWifiStrengthImage(ap.getLevel());
      holder.wifiStrength.setBackgroundResource(imgRes);
    }

    return convertView;
  }

  private int getWifiStrengthImage(int level) {
    if (level > 75) {
      return R.drawable.wifi_strength_level_4;
    }
    if (level > 50) {
      return R.drawable.wifi_strength_level_3;
    }
    if (level > 25) {
      return R.drawable.wifi_strength_level_2;
    }
    if (level > 0) {
      return R.drawable.wifi_strength_level_1;
    }
    return R.drawable.wifi_strength_level_0;
  }

  private static class ViewHolder {
    LinearLayout root;
    TextView wifiName;
    ImageView wifiStrength;
    ImageView checkbox;
  }

}
