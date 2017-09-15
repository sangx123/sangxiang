package com.hubble.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hubble.model.DrawerItemModel;
import com.hubbleconnected.camera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dan on 2014-07-10.
 */
public class DrawerItemAdapter extends BaseAdapter {

  private Activity activity;
  private List<DrawerItemModel> items;
  private int selectedPosition = 0;

  public DrawerItemAdapter(Activity activity, List<DrawerItemModel> items) {
    this.activity = activity;
    this.items = items;
  }

  @Override
  public int getCount() {
    return items.size();
  }

  @Override
  public Object getItem(int i) {
    return items.get(i);
  }

  public DrawerItemModel getModelAt(long index) {
    return (DrawerItemModel) this.getItem((int) index);
  }

  @Override
  public long getItemId(int i) {
    return i; // I'd imagine this isn't correct. - we want the R.id value..?
  }

  //HACK: The onItemClick is defined and handled by the consumer
  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {

    LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    View vi = view;
    if (view == null) {
      vi = inflater.inflate(R.layout.drawer_list_item, null);
    }

    if (i == selectedPosition) {
      vi.setBackgroundColor(activity.getResources().getColor(R.color.selected_list_item));
    } else {
      vi.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
    }

    DrawerItemModel model = items.get(i);

    TextView title = (TextView) vi.findViewById(R.id.title); // title

    ImageView icon = (ImageView) vi.findViewById(R.id.drawer_list_item_image);

    icon.setImageDrawable(model.icon);

    title.setText(model.title);

    return vi;
  }

  public void setSelectedPosition(int position) {
    selectedPosition = position;
    notifyDataSetChanged();
  }

  public void addItem(DrawerItemModel item) {
    if (item != null) {
      if (items == null) {
        items = new ArrayList<>();
      }
      items.add(item);
      notifyDataSetChanged();
    }
  }

  public void removeItem(DrawerItemModel.StaticMenuItems itemId) {
    if (items != null) {
      boolean needRefreshView = false;
      for (int i = items.size() - 1; i >= 0; i--) {
        if (items.get(i).menuItemType == itemId) {
          items.remove(i);
          needRefreshView = true;
        }
      }
      if (needRefreshView) {
        notifyDataSetChanged();
      }
    }
  }
}
