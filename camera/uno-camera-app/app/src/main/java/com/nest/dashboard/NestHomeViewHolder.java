package com.nest.dashboard;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hubbleconnected.camera.R;
import com.nest.common.NestDevicesActivity;
import com.nest.common.NestStructure;

/**
 * Created by dasari on 30/01/17.
 */

public class NestHomeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private TextView homeName;
    private TextView homeStatus;
    private String structureID, structureName;
    private Context context;
    public NestHomeViewHolder(View itemView) {
        super(itemView);

        homeName = (TextView) itemView.findViewById(R.id.nest_home_name);
        homeStatus = (TextView) itemView.findViewById(R.id.home_status);
        itemView.setOnClickListener(this);
    }

    public void bindToHome(Context context, NestStructure homeStructure){
        homeName.setText(homeStructure.getHomeName());
        homeStatus.setText("(" + homeStructure.getHomeAwayStatus() + ")");
        structureID = homeStructure.getHomeID();
        structureName = homeStructure.getHomeName();
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(context , NestDevicesActivity.class);
        intent.putExtra("HOME_ID", structureID);
        intent.putExtra("HOME_NAME", structureName);
        context.startActivity(intent);
    }
}
