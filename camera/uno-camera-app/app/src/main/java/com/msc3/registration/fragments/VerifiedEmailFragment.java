package com.msc3.registration.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hubbleconnected.camera.R;
import com.msc3.registration.EmailConfirmationActivity;

public class VerifiedEmailFragment extends Fragment implements View.OnClickListener
{
	private static final String TAG = VerifiedEmailFragment.class.getSimpleName();

	private Context mContext;

	private Button mTakeInButton;


	public static Fragment newInstance()
	{
		Fragment fragment = new VerifiedEmailFragment();

		Bundle bundle = new Bundle();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
	}

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		mContext = context;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mContext = activity;
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		mContext = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.verify_email_completed, container, false);
		initialize(view);
		return view;
	}

	private void initialize(View view)
	{
		mTakeInButton = (Button)view.findViewById(R.id.take_in_button);
		mTakeInButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.take_in_button:
			{
				if(mContext != null)
				{
					((EmailConfirmationActivity)mContext).startMainActivity();
				}
			}
			break;
		}
	}

}
