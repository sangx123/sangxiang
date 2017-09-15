package com.nest.common;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hubbleconnected.camera.R;
import com.nest.dashboard.SmokeDetectorViewHolder;
import com.nest.dashboard.ThermostatViewHolder;

import java.util.ArrayList;

/**
 * Created by dasari on 12/01/17.
 */

public class NestDeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int THERMOSTAT = 0;
    private static final int SMOKE_DETECTOR = 1;
    private static final int NEST_CAM = 2;
    private LayoutInflater layoutInflater;
    private ArrayList<NestDevice> deviceList;
    private Context mContext;
    private String homeID;
    private String homeName;
    public NestDeviceListAdapter(Context context){
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void setDeviceList(ArrayList<NestDevice> deviceList,String homeID,String homeName){
        this.deviceList = deviceList;
        this.homeID = homeID;
        this.homeName = homeName;
        Log.d("NestDevicelistAdapter",""+deviceList.toString());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == THERMOSTAT) {
            View view = layoutInflater.inflate(R.layout.nest_thermostat_viewholder, parent, false);
            viewHolder = new ThermostatViewHolder(view);
        } else if (viewType == SMOKE_DETECTOR ) {
            View view = layoutInflater.inflate(R.layout.nest_smoke_detector_viewholder, parent, false);
            viewHolder = new SmokeDetectorViewHolder(view,homeID,homeName);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(deviceList.get(position).getDeviceType() == NestDevice.DEVICE_TYPE.THERMOSTAT){
            ThermostatViewHolder thermostatViewHolder = (ThermostatViewHolder)holder;
            thermostatViewHolder.bindToDevice(mContext, deviceList.get(position));
        }else if(deviceList.get(position).getDeviceType() == NestDevice.DEVICE_TYPE.SMOKE_DETECTOR){
            SmokeDetectorViewHolder smokeDetectorViewHolder = (SmokeDetectorViewHolder)holder;
            smokeDetectorViewHolder.bindToDevice(mContext, deviceList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    @Override
    public int getItemViewType(int position) {

        if(deviceList.get(position).getDeviceType() == NestDevice.DEVICE_TYPE.THERMOSTAT)
            return THERMOSTAT;
        else if(deviceList.get(position).getDeviceType() == NestDevice.DEVICE_TYPE.SMOKE_DETECTOR)
            return SMOKE_DETECTOR;
        else if(deviceList.get(position).getDeviceType() == NestDevice.DEVICE_TYPE.CAMERA)
            return NEST_CAM;
        else
            return 0;
    }
}
