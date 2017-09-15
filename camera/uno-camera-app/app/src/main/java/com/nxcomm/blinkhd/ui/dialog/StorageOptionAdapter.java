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
public class StorageOptionAdapter extends BaseAdapter implements SpinnerAdapter {
  private String[] items;
  private Context mContext;

  public StorageOptionAdapter(Context ctx, boolean isSupportSDCard) {
    if (isSupportSDCard) {
      items = ctx.getResources().getStringArray(R.array.record_storage_option_with_sdcard);
    } else {
      items = ctx.getResources().getStringArray(R.array.record_storage_option_without_sdcard);
    }
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
