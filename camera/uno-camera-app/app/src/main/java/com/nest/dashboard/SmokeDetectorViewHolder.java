package com.nest.dashboard;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hubbleconnected.camera.R;
import com.nest.common.NestDevice;
import com.nest.common.NestRuleSettingsActivity;
import com.nest.common.Settings;

/**
 * Created by dasari on 09/01/17.
 */

public class SmokeDetectorViewHolder extends RecyclerView.ViewHolder {


    private Context mContext;
    private TextView mProductNameView;
    private TextView mRuleSettings;
    private boolean isSmokeDetectorOn;
    private String homeID;
    private String homeName;
    public SmokeDetectorViewHolder(View itemView,String homeID,String homeName) {
        super(itemView);
        mProductNameView = (TextView) itemView.findViewById(R.id.product_name);
        mRuleSettings = (TextView) itemView.findViewById(R.id.rule_settings);
       this.homeID = homeID;
        this.homeName = homeName;
    }

    public void bindToDevice(Context context, final NestDevice nestDevice) {
        mContext = context;
        mProductNameView.setText(nestDevice.getName());

        isSmokeDetectorOn = Settings.isDeviceEnabled(mContext,nestDevice.getDeviceID());
        /*if(isSmokeDetectorOn){
            mSwitchView.setChecked(true);
        }else{
            mSwitchView.setChecked(false);
        }*/
        /*mSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.addDeviceToDashboard(mContext, nestDevice.getDeviceID(), isChecked);
            }
        });*/

        mRuleSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent ruleSettingIntent = new Intent(mContext, NestRuleSettingsActivity.class);
                ruleSettingIntent.putExtra("HOME_ID",homeID);
                ruleSettingIntent.putExtra("HOME_NAME",homeName);

               mContext.startActivity(ruleSettingIntent);
            }
        });
    }


}
