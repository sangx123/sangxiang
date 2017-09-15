package com.hubble.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.base.hubble.subscriptions.SubscriptionCommandExecutor;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceID;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceFreeTrialDetails;
import com.hubble.framework.service.device.DeviceManagerService;

import com.hubbleconnected.camera.R;

import com.util.CommonUtil;

import base.hubble.Api;
import base.hubble.PublicDefineGlob;

/**
 * Created by Admin on 18-01-2017.
 */
public class AskForFreeTrialDialog extends Dialog implements android.view.View.OnClickListener {

	private final String TAG = "AskForFreeTrialDialog";
	private Context mContext;


	private Button mEnableFreeTrial;
	private Button mUpgradePlan;
	private TextView mFreeTrialDays;
	private TextView mAboutFreeTrial;
	private TextView mPrivacyPolicy;
	private TextView mAboutPlan;
	private ImageView mCloseButton;
	private PlanListener mPlanListener;

	public interface PlanListener{
		public void onEnableFreeTrialClick();
		public void onUpgradePlanClick();
	}


	public AskForFreeTrialDialog(Context context, PlanListener planListener) {
		super(context);
		mContext = context;
		mPlanListener = planListener;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.start_free_trial_button:
				mPlanListener.onEnableFreeTrialClick();
				break;
			case R.id.upgrade_plan:
				mPlanListener.onUpgradePlanClick();
				break;
			case R.id.free_trial_text3:
				Intent intentFreeTrial = new Intent(Intent.ACTION_VIEW);
				intentFreeTrial.setData(Uri.parse("https://hubbleconnected.com/plans"));
				mContext.startActivity(intentFreeTrial);
				break;
			case R.id.free_trial_text6:
				Intent intentPlan= new Intent(Intent.ACTION_VIEW);
				intentPlan.setData(Uri.parse("https://hubbleconnected.com/plans"));
				mContext.startActivity(intentPlan);
				break;
			case R.id.free_trial_close:
				cancel();
				break;
			default:
				break;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setContentView(R.layout.ask_free_trial_layout);
		mFreeTrialDays = (TextView)findViewById(R.id.free_trial_text1);
		mEnableFreeTrial = (Button)findViewById(R.id.start_free_trial_button);
		mUpgradePlan = (Button)findViewById(R.id.upgrade_plan);
		mAboutFreeTrial = (TextView)findViewById(R.id.free_trial_text3);
		mAboutPlan = (TextView)findViewById(R.id.free_trial_text6);
		mCloseButton = (ImageView)findViewById(R.id.free_trial_close);
		mPrivacyPolicy = (TextView)findViewById(R.id.free_trial_text4);

		int freeTrialDays = CommonUtil.getFreeTrialDays(mContext);
		mFreeTrialDays.setText(mContext.getString(R.string.free_trial_activate_motion_text, freeTrialDays));
		mEnableFreeTrial.setText(mContext.getString(R.string.free_trial_start_text, freeTrialDays));
		mPrivacyPolicy.setText(Html.fromHtml(mContext.getString(R.string.free_trial_privacy_policy)));
		mPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
		mEnableFreeTrial.setOnClickListener(this);
		mUpgradePlan.setOnClickListener(this);
		mAboutFreeTrial.setOnClickListener(this);
		mAboutPlan.setOnClickListener(this);
		mCloseButton.setOnClickListener(this);
	}
}
