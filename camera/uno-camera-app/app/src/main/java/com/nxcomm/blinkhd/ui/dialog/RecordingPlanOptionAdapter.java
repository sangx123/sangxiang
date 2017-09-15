package com.nxcomm.blinkhd.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.hubbleconnected.camera.R;


/**
 * Created by Son Nguyen on 6/19/2015.
 */
public class RecordingPlanOptionAdapter extends BaseAdapter implements SpinnerAdapter {
  private String[] items;
  private Context mContext;

  public RecordingPlanOptionAdapter(Context ctx) {
    items = ctx.getResources().getStringArray(R.array.recording_plan);
    mContext = ctx;
  }

  @Override
  public int getCount() {
    return items.length;
  }

  @Override
  public Object getItem(int position) {
    return items[position];
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View view, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    view = inflater.inflate(R.layout.storage_option_spinner_item, null);
    TextView textView = (TextView) view.findViewById(R.id.option_textview);
    textView.setText(items[position]);
    return view;
  }
}
