package com.msc3.registration.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.framework.networkinterface.user.AccountManager;
import com.hubble.framework.service.cloudclient.user.pojo.request.ResendVerificationEmail;
import com.hubble.framework.service.cloudclient.user.pojo.response.ResendEmailDetails;
import com.hubbleconnected.camera.R;
import com.msc3.registration.EmailConfirmationActivity;
import com.util.NetworkDetector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.hubble.PublicDefineGlob;


public class ChangeEmailFragment extends Fragment implements View.OnClickListener
{
	private static final String TAG = ChangeEmailFragment.class.getSimpleName();

	private Context mContext;

	private EditText mEmailEt, mPasswordET;
	private TextView mShowPasswordTv;
	private ImageView mValidEmailIv;

	private TextView mEmailErrorTv,mPasswordErrorTv;

	private Button mChangeEmailButton;

	boolean isPasswordClicked;

	private SecureConfig mSettings = HubbleApplication.AppConfig;
	private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,255})";

	private NetworkDetector mNetworkDetector;
	private String mExistedmail = null;


	public static Fragment newInstance()
	{
		Fragment fragment = new ChangeEmailFragment();

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
	public void onStart()
	{
		super.onStart();
		if(mContext != null)
			mNetworkDetector = new NetworkDetector(mContext);

		mExistedmail = mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR,null);
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
		View view = inflater.inflate(R.layout.change_email, container, false);
		initialize(view);
		return view;
	}

	private void initialize(View view)
	{
		mEmailEt = (EditText) view.findViewById(R.id.login_et);

		mEmailErrorTv = (TextView)view.findViewById(R.id.email_error_tv);

		mValidEmailIv = (ImageView)view.findViewById(R.id.validemail);

		mEmailEt.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				if(mEmailErrorTv.getVisibility() == View.VISIBLE)
					mEmailErrorTv.setVisibility(View.INVISIBLE);

			}

			@Override
			public void afterTextChanged(Editable s) {

				validateEmail();
			}
		});

		mPasswordET = (EditText) view.findViewById(R.id.login_password_et);

		mShowPasswordTv = (TextView)view.findViewById(R.id.showpassword_tv);
		mShowPasswordTv.setOnClickListener(this);

		mPasswordErrorTv = (TextView)view.findViewById(R.id.tv_password_error);

		mChangeEmailButton = (Button) view.findViewById(R.id.change_email_button);
		mChangeEmailButton.setOnClickListener(this);

	}

	private boolean validateEmail()
	{
		String newEmail = mEmailEt.getText().toString();
		if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches())
		{
			mEmailErrorTv.setVisibility(View.VISIBLE);
			mValidEmailIv.setVisibility(View.GONE);
		}
		else if(mExistedmail != null && newEmail != null && mExistedmail.compareToIgnoreCase(newEmail)==0)
		{
			mEmailErrorTv.setVisibility(View.VISIBLE);
			mValidEmailIv.setVisibility(View.GONE);
		}
		else
		{
			mEmailErrorTv.setVisibility(View.INVISIBLE);
			mValidEmailIv.setVisibility(View.VISIBLE);
		}
		return android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailEt.getText().toString()).matches();
	}

	private boolean validatePassword()
	{
		Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);
		Matcher passwordMatcher = passwordPattern.matcher(mPasswordET.getText().toString().trim());

		boolean isValidPassword = passwordMatcher.find();

		if (!isValidPassword)
		{
			mPasswordErrorTv.setVisibility(View.VISIBLE);
		}
		else
		{
			mPasswordErrorTv.setVisibility(View.INVISIBLE);
		}

		return isValidPassword;
	}


	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.change_email_button:
			{
				if(validateParameter())
				{
					final String newEmail = mEmailEt.getText().toString().trim();
					String password  = mPasswordET.getText().toString().trim();

					String email = mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR,null);
					final String registrationID = mSettings.getString(PublicDefineGlob.PREFS_SAVED_REGISTRATION_ID,null);

					Boolean isInternetPresent = false;
					if(mNetworkDetector != null)
						isInternetPresent = mNetworkDetector.isConnectingToInternet();

					if(email != null && newEmail != null && newEmail.compareToIgnoreCase(email)==0)
					{
						Toast.makeText(mContext,getResources().getString(R.string.email_not_same),Toast.LENGTH_LONG).show();
						return;
					}

					if(mContext != null && isInternetPresent)
					{
						ResendVerificationEmail resendVerificationEmail = new ResendVerificationEmail(email,registrationID,password);
						resendVerificationEmail.setNewUserEmail(newEmail);

						((EmailConfirmationActivity)mContext).displayProgressDialog();

						AccountManager.getInstance(mContext).resendVerificationEmail(resendVerificationEmail,
								new Response.Listener<ResendEmailDetails>()
								{
									@Override
									public void onResponse (ResendEmailDetails response)
									{
										((EmailConfirmationActivity)mContext).dismissDialog();
										if (response != null)
										{
											mSettings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, newEmail);
											mSettings.putString(PublicDefineGlob.PREFS_SAVED_REGISTRATION_ID, response.getUniqueRegistrationNumber());

											((EmailConfirmationActivity)mContext).switchFragment(EmailConfirmationFragment.newInstance(),true);

										}

									}
								},
								new Response.ErrorListener() {
									@Override
									public void onErrorResponse (VolleyError error)
									{
										((EmailConfirmationActivity)mContext).dismissDialog();

										if (error != null && error.networkResponse != null)
										{
											Log.d(TAG, error.networkResponse.toString());
											Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));

											switch(error.networkResponse.statusCode)
											{
												case 401:
													Toast.makeText(getActivity(),getResources().getString(R.string.password_not_match),Toast.LENGTH_LONG).show();
													break;

												case 409:
													Toast.makeText(getActivity(),getResources().getString(R.string.email_already_exist),Toast.LENGTH_LONG).show();
													break;

												default:
													Toast.makeText(getActivity(),getResources().getString(R.string.failed_to_change_email),Toast.LENGTH_LONG).show();
													break;
											}
										}
									}
								});
					}
					else
					{
						Toast.makeText(mContext,R.string.enable_internet_connection,Toast.LENGTH_SHORT).show();
					}

				}
			}
			break;
			case R.id.showpassword_tv:
			{
				if (mPasswordET.getText().toString().length() > 0)
				{
					if (!isPasswordClicked)
					{
						mPasswordET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
						isPasswordClicked = true;
						mShowPasswordTv.setText(R.string.hide);
					}
					else
					{
						mPasswordET.setTransformationMethod(PasswordTransformationMethod.getInstance());
						isPasswordClicked = false;
						mShowPasswordTv.setText(R.string.show);
					}
				}
				else
				{
					Toast.makeText(mContext, R.string.please_enter_password, Toast.LENGTH_SHORT).show();
				}

			}
			break;
		}
	}

	private boolean validateParameter()
	{
		if (mEmailEt.getText().toString().trim().length() == 0 || mPasswordET.getText().toString().trim().length() == 0)
		{
			Toast.makeText(mContext, R.string.please_enter_all_fields, Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!validateEmail())
		{
			Toast.makeText(mContext,R.string.email_warning_message,Toast.LENGTH_SHORT).show();
			return false;
		}
		/*else if (!validatePassword())
		{
			Toast.makeText(mContext,R.string.paswd_error_message,Toast.LENGTH_SHORT).show();
			return false;
		}*/
		return true;

	}
}
