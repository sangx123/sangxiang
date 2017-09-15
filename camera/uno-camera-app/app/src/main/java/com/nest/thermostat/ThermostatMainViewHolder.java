package com.nest.thermostat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hubbleconnected.camera.R;
import com.nestlabs.sdk.Thermostat;

/**
 * Created by dasari on 09/01/17.
 */

public class ThermostatMainViewHolder extends RecyclerView.ViewHolder {

    private TextView productName, mode, status, outerTemp, roomTemp, humidity;
    private TextView functional, targetTemp, targetTime;
    private RelativeLayout parentLayout;

    public ThermostatMainViewHolder(View itemView) {
        super(itemView);
        parentLayout = (RelativeLayout) itemView.findViewById(R.id.parent_layout);
        productName = (TextView) itemView.findViewById(R.id.product_name);
        mode = (TextView) itemView.findViewById(R.id.mode);
        status = (TextView) itemView.findViewById(R.id.status);
        //outerTemp = (TextView) itemView.findViewById(R.id.temparature);
        roomTemp = (TextView) itemView.findViewById(R.id.room_temparature);
        humidity = (TextView) itemView.findViewById(R.id.humidity);
        functional = (TextView) itemView.findViewById(R.id.functional);
        targetTemp = (TextView) itemView.findViewById(R.id.target_temparature);
        targetTime = (TextView) itemView.findViewById(R.id.target_time);
    }

    public void bindToDevice(Thermostat thermostat) {

        productName.setText(thermostat.getNameLong());
        productName.setSelected(true);
        if(thermostat.getHasLeaf()){
            mode.setVisibility(View.VISIBLE);
            mode.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.eco_leaf, 0);
        //    mode.setText(R.string.eco);
        }else{
            mode.setVisibility(View.INVISIBLE);
        }
        if(thermostat.getHvacMode().equals("eco")){
            mode.setVisibility(View.VISIBLE);
            mode.setText(R.string.eco);
        }else if(thermostat.getHvacMode().equals("heat")) {
            mode.setVisibility(View.VISIBLE);
            mode.setText(R.string.heat);
        }else if(thermostat.getHvacMode().equals("cool")) {
            mode.setVisibility(View.VISIBLE);
            mode.setText(R.string.cool);
        }else if(thermostat.getHvacMode().equals("heat-cool")) {
            mode.setVisibility(View.VISIBLE);
            mode.setText(R.string.heatcool);
        }else if(thermostat.getHvacMode().equals("off")) {
            mode.setVisibility(View.VISIBLE);
            mode.setText(R.string.off);
        }else
        {
            mode.setVisibility(View.INVISIBLE);
        }
        if (thermostat.isOnline()) {
            status.setText(R.string.online);
            status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.online, 0, 0, 0);
        } else {
            status.setText(R.string.offline);
            status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.offline, 0, 0, 0);
        }
        humidity.setText(thermostat.getHumidity() + "%");
        if (thermostat.getHvacState().equals("cooling")) {
            functional.setVisibility(View.VISIBLE);
            functional.setText(R.string.cooling);
            targetTemp.setVisibility(View.VISIBLE);
            targetTime.setVisibility(View.VISIBLE);
            parentLayout.setBackgroundResource(R.drawable.nest_background_cooling);
        } else if (thermostat.getHvacState().equals("heating")) {
            functional.setVisibility(View.VISIBLE);
            functional.setText(R.string.heating);
            targetTemp.setVisibility(View.VISIBLE);
            targetTime.setVisibility(View.VISIBLE);
            parentLayout.setBackgroundResource(R.drawable.nest_background_heating);
        } else {
            functional.setVisibility(View.INVISIBLE);
            targetTemp.setVisibility(View.INVISIBLE);
            targetTime.setVisibility(View.INVISIBLE);
            parentLayout.setBackgroundResource(R.drawable.nest_background_normal);
        }
        if (thermostat.getTemperatureScale().equals("C")) {
            roomTemp.setText(thermostat.getAmbientTemperatureC() + "\u2103");
            double tempC = thermostat.getTargetTemperatureC();
            long tempValue= (long) tempC;
            targetTemp.setText(""+tempValue);
            targetTime.setText("in "+thermostat.getmTimeToReachTarget()+" mins...");
           // outerTemp.setText("27" + "\u2103");
        } else if (thermostat.getTemperatureScale().equals("F")) {
            roomTemp.setText(thermostat.getAmbientTemperatureF() + "\u2109");
            targetTemp.setText(""+thermostat.getTargetTemperatureF());
            targetTime.setText("in "+thermostat.getmTimeToReachTarget()+" mins...");
            //outerTemp.setText("75" + "\u2109");
        }

    }

}
