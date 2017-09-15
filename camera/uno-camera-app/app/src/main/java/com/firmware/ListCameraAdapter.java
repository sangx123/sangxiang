package com.firmware;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.hubbleconnected.camera.R;

import java.util.ArrayList;

/**
 * Created by CVision on 10/29/2015.
 */
public class ListCameraAdapter extends BaseAdapter {

    private ArrayList<CameraModel> data;
    private Activity mContext;

    public ListCameraAdapter(Activity mContext, ArrayList<CameraModel> data){
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.list_item_camera_model, viewGroup, false);

            viewHolder.tvNameCamera = (TextView) view.findViewById(R.id.tv_name);
            viewHolder.btnUpgrade = (Button) view.findViewById(R.id.btn_upgrade);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvNameCamera.setText(data.get(i).getNameModel());
        viewHolder.btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FirmwareUpdateActivity)mContext).onClickUpgrade(data.get(i).getModelId());
            }
        });

        return view;
    }

    class ViewHolder{
        TextView tvNameCamera;
        Button btnUpgrade;
    }
}
