package com.hubble.subscription;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.util.SubscriptionUtil;
import com.hubbleconnected.camera.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Admin on 30-01-2017.
 */
public class PlanAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {

	private List<DevicePlanStatus> mDevicePlanList = new ArrayList<>();
	private int mPlanType = -1;
	private String mPlanStatus = null;
	private Context mContext;
	private ProgressDialog mFreeTrialApply;
	private IClickCallBack mIClickCallBack;
	private final String CANCELED = "canceled";
	private final String PENDING = "pending";

	public interface IClickCallBack{
		public void onClick(int position);
		public void onCheckChange(int position, boolean isChecked);
	}

	public PlanAdapter(Context context, IClickCallBack clickCallBack) {
		mContext = context;
		mIClickCallBack = clickCallBack;
	}

	public void setDeviceList(List<DevicePlanStatus> devicePlanList, int planType, String planStatus) {
		mDevicePlanList = devicePlanList;
		mPlanType = planType;
		mPlanStatus = planStatus;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getCount() {
		return mDevicePlanList.size();
	}


	@Override
	public Object getItem(int position) {
		return mDevicePlanList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.plan_adapter_list_item, null);
            holder.noPlanLayout = (LinearLayout)convertView.findViewById(R.id.no_plan_item_layout);
			holder.camNoPlanName = (TextView) convertView.findViewById(R.id.cam_no_plan_name);
			holder.activateFreeTrialButton = (Button)convertView.findViewById(R.id.activate_free_trial);
			holder.planDetailText = (TextView) convertView.findViewById(R.id.text_plan_detail);
			holder.planStatusText = (TextView)convertView.findViewById(R.id.text_plan_status);
			holder.planLayout = (RelativeLayout)convertView.findViewById(R.id.plan_item_layout);
			holder.camPlanName = (TextView)convertView.findViewById(R.id.cam_plan_name);
			holder.planChangeSwitch = (SwitchCompat)convertView.findViewById(R.id.plan_switch);
			holder.disableView = (RelativeLayout)convertView.findViewById(R.id.disable_view);
 			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//Load view as per plan
		DevicePlanStatus devicePlan = mDevicePlanList.get(position);
		holder.disableView.setVisibility(View.GONE);
		if(mPlanType == SubscriptionUtil.PLAN_ENABLED){
			holder.noPlanLayout.setVisibility(View.GONE);
			holder.planLayout.setVisibility(View.VISIBLE);
			holder.camPlanName.setText(devicePlan.getCameraName());
			if(devicePlan.getPlanType() == SubscriptionUtil.PLAN_APPLIED) {
				//Plan available
				holder.planChangeSwitch.setOnCheckedChangeListener(null);
				holder.planChangeSwitch.setChecked(true);
				holder.planChangeSwitch.setEnabled(true);
			}else {
				holder.planChangeSwitch.setOnCheckedChangeListener(null);
				holder.planChangeSwitch.setChecked(false);
				if(devicePlan.getPlanType() == SubscriptionUtil.PLAN_AVAILABLE){
					holder.planChangeSwitch.setEnabled(true);
				} else if(devicePlan.getPlanType() == SubscriptionUtil.PLAN_MAX_QUOTA_REACHED){
					holder.disableView.setVisibility(View.VISIBLE);
					holder.planChangeSwitch.setEnabled(false);
				}
			}
			holder.planChangeSwitch.setOnCheckedChangeListener(this);
			holder.planChangeSwitch.setTag(position);

		}else if(mPlanType == SubscriptionUtil.NO_PLAN) {
			holder.noPlanLayout.setVisibility(View.VISIBLE);
			holder.planLayout.setVisibility(View.GONE);
			holder.camNoPlanName.setText(devicePlan.getCameraName());
			String planDetailtext = devicePlan.getPlanName();
			if(devicePlan.getPlanType() == SubscriptionUtil.PLAN_FREE_TRIAL_APPLIED){
				planDetailtext = planDetailtext+mContext.getString(R.string.plan_detail,devicePlan.getExpiryDate());
				holder.activateFreeTrialButton.setVisibility(View.INVISIBLE);
				holder.planStatusText.setVisibility(View.INVISIBLE);
			}else if(devicePlan.getPlanType() == SubscriptionUtil.PLAN_FREE_TRIAL_AVAILABLE){
				holder.planStatusText.setVisibility(View.INVISIBLE);

				holder.activateFreeTrialButton.setVisibility(View.VISIBLE);
				holder.activateFreeTrialButton.setTag(position);
				holder.activateFreeTrialButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int clickPosition = (int)v.getTag();
						mIClickCallBack.onClick(clickPosition);
					}
				});
			}else if(devicePlan.getPlanType() == SubscriptionUtil.PLAN_FREE_TRIAL_EXPIRED) {
				holder.activateFreeTrialButton.setVisibility(View.INVISIBLE);
				holder.planStatusText.setVisibility(View.VISIBLE);
			}else if(devicePlan.getPlanType() == SubscriptionUtil.PLAN_ON_SOME_PLAN){
				holder.activateFreeTrialButton.setVisibility(View.INVISIBLE);
				holder.planStatusText.setVisibility(View.INVISIBLE);
			}
			holder.planDetailText.setText(planDetailtext);
		}
		return convertView;
	}
	public class ViewHolder {
		LinearLayout noPlanLayout;
		TextView camNoPlanName;
		Button activateFreeTrialButton;
		TextView planDetailText;
		TextView planStatusText;
		RelativeLayout planLayout;
		TextView camPlanName;
		SwitchCompat planChangeSwitch;
		RelativeLayout disableView;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int position  = (int)buttonView.getTag();
		mIClickCallBack.onCheckChange(position, isChecked);
	}
}
