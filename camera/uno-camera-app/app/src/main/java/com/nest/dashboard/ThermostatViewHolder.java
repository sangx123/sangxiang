package com.nest.dashboard;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hubbleconnected.camera.R;
import com.nest.common.NestDevice;
import com.nest.common.Settings;


/**
 * Created by dasari on 09/01/17.
 */

public class ThermostatViewHolder extends RecyclerView.ViewHolder {

    private Context mContext;
    private TextView mProductNameView;
    private SwitchCompat mSwitchView;
    private boolean isThermostatOn;

    public ThermostatViewHolder(View itemView) {
        super(itemView);
        mProductNameView = (TextView)itemView.findViewById(R.id.product_name);
        mSwitchView = (SwitchCompat)itemView.findViewById(R.id.switch_button);
    }

    public void bindToDevice(Context context, final NestDevice nestDevice){
        mContext = context;
        mProductNameView.setText(nestDevice.getName());
        isThermostatOn = Settings.isDeviceEnabled(mContext,nestDevice.getDeviceID());
        if(isThermostatOn){
            mSwitchView.setChecked(true);
        }else{
            mSwitchView.setChecked(false);
        }
        mSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.addDeviceToDashboard(mContext, nestDevice.getDeviceID(), isChecked);
            }
        });
    }
}
