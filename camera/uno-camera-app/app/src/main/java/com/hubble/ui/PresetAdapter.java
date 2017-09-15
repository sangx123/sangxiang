package com.hubble.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hubbleconnected.camera.R;

import java.util.ArrayList;

/**
 * Created by CVision on 12/16/2015.
 */
public class PresetAdapter extends BaseAdapter {

    private ArrayList<PresetModel> data;
    private Context mContext;

    public PresetAdapter(ArrayList<PresetModel> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.list_item_preset, viewGroup, false);

            viewHolder.tvNamePreset = (TextView) view.findViewById(R.id.tv_preset);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvNamePreset.setText(data.get(i).getNamePreset());

        return view;
    }

    public static class ViewHolder{
        TextView tvNamePreset;
    }
}
