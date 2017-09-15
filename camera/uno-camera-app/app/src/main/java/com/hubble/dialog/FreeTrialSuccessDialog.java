package com.hubble.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

/**
 * Created by Admin on 18-01-2017.
 */
public class FreeTrialSuccessDialog extends Dialog {

	private Context mContext;

	private TextView mOk;

	public FreeTrialSuccessDialog(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setContentView(R.layout.free_trial_success_layout);
		mOk = (TextView)findViewById(R.id.free_trial_success_ok);
		mOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isShowing()) {
					dismiss();
				}
			}
		});
	}
}
