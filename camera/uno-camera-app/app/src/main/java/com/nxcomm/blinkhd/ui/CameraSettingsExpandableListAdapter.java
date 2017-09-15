package com.nxcomm.blinkhd.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hubble.util.ListChild;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.customview.AnimatedExpandableListView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Sean on 15-01-28.
 */
public class CameraSettingsExpandableListAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

  private Context _context;
  private List<Integer> _listDataHeader; // header titles
  // child data in format of header title, child title
  private HashMap<Integer, List<ListChild>> _listDataChild;

  public CameraSettingsExpandableListAdapter(Context context, List<Integer> listDataHeader,
                                             HashMap<Integer, List<ListChild>> listChildData) {
    this._context = context;
    this._listDataHeader = listDataHeader;
    this._listDataChild = listChildData;
  }

  @Override
  public ListChild getChild(int groupPosition, int childPosition) {
    return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosition);
  }

  @Override
  public long getChildId(int groupPosition, int childPosition) {
    return childPosition;
  }

  @Override
  public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    ListChild child = getChild(groupPosition, childPosition);

    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.fragment_camera_settings_list_item, parent, false);
    }

    TextView childTitle = (TextView) convertView.findViewById(R.id.lblListItem);
    TextView childValue = (TextView) convertView.findViewById(R.id.lblListItemSubtext);
    childTitle.setText(child.title);
    if (child.value != null) {
      childValue.setVisibility(View.VISIBLE);
      childValue.setText(child.value);
    } else {
      childValue.setVisibility(View.GONE);
    }

    if (child.isClickable) {
      childTitle.setTextColor(_context.getResources().getColor(android.R.color.holo_blue_dark));
    } else {
      childTitle.setTextAppearance(_context, android.R.style.TextAppearance_Medium);
    }
    return convertView;
  }

  @Override
  public int getRealChildrenCount(int groupPosition) {
    return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
  }

  @Override
  public Integer getGroup(int groupPosition) {
    return this._listDataHeader.get(groupPosition);
  }

  @Override
  public int getGroupCount() {
    return this._listDataHeader.size();
  }

  @Override
  public long getGroupId(int groupPosition) {
    return groupPosition;
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    String headerTitle = _context.getString(getGroup(groupPosition));
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.fragment_camera_settings_list_group, parent, false);
    }

    TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
    lblListHeader.setText(headerTitle);

    return convertView;
  }

  @Override
  public boolean hasStableIds() {
    return false;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }
}