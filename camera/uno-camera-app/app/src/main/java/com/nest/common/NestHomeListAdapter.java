package com.nest.common;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hubbleconnected.camera.R;
import com.nest.dashboard.NestHomeViewHolder;

import java.util.ArrayList;

/**
 * Created by dasari on 12/01/17.
 */

public class NestHomeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater layoutInflater;
    private ArrayList<NestStructure> deviceList;
    private Context context;

    public NestHomeListAdapter(Context context){
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setStructureList(ArrayList<NestStructure> deviceList){
        this.deviceList = deviceList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
            View view = layoutInflater.inflate(R.layout.nest_home_viewholder, parent, false);
            viewHolder = new NestHomeViewHolder(view);
            return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
         NestHomeViewHolder nestHomeViewHolder = (NestHomeViewHolder)holder;
         nestHomeViewHolder.bindToHome(context, deviceList.get(position));
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

}
