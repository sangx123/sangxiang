package com.msc3.registration.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.networkinterface.user.AccountManager;
import com.hubble.framework.service.account.AccountManagement;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.user.pojo.request.ResendVerificationEmail;
import com.hubble.framework.service.cloudclient.user.pojo.request.ValidateUserAccount;
import com.hubble.framework.service.cloudclient.user.pojo.response.ResendEmailDetails;
import com.hubble.framework.service.cloudclient.user.pojo.response.ValidateUserDetail;
import com.hubble.framework.service.configuration.AppSDKConfiguration;
import com.hubble.framework.service.database.DeviceDatabaseHelper;
import com.hubble.ui.DebugFragment;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;
import com.msc3.registration.EmailConfirmationActivity;
import com.msc3.registration.RegisterActivity;
import com.nxcomm.blinkhd.ui.CameraListFragment;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.nxcomm.blinkhd.ui.RSAUtils;
import com.util.AppEvents;
import com.util.NetworkDetector;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import base.hubble.Api;
import base.hubble.PublicDefineGlob;

public class EmailConfirmationFragment extends Fragment implements View.OnClickListener
{
	private static final String TAG = EmailConfirmationFragment.class.getSimpleName();

	private AccountManagement mAccountManagement;
	private ProgressDialog mProgressDialog;

	private TextView mUserEmailTv, mEmailContent;
	private String mUserEmail;
	private LinearLayout mChangeEmailLinearLayout;
	private EditText mEnterVerificationCodeET;
	private Button mVerifyButton;
	private TextView mResentVerificationTv,mVerificationCodeTv;

	private NetworkDetector mNetworkDetector;
	private SecureConfig mSettings = HubbleApplication.AppConfig;

	private Context mContext;

	private DeviceDatabaseHelper mDeviceDatabaseHelper;


	public static Fragment newInstance()
	{
		Fragment fragment = new EmailConfirmationFragment();

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
		View view = inflater.inflate(R.layout.email_verification, container, false);
		initialize(view);
		return view;
	}

	private void initialize(View view)
	{
		mUserEmailTv = (TextView)view.findViewById(R.id.user_email_tv);
        mEmailContent = (TextView)view.findViewById(R.id.email_content);

		mUserEmail = mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, null);

		if(!TextUtils.isEmpty(mUserEmail))
		{
            if(validateEmail(mUserEmail))
			     mUserEmailTv.setText(mUserEmail);
            else {
                mEmailContent.setText(R.string.email_content_info);
                mUserEmailTv.setVisibility(View.GONE);
            }
		}

		mChangeEmailLinearLayout = (LinearLayout) view.findViewById(R.id.change_email_linearlayout);
		mChangeEmailLinearLayout.setOnClickListener(this);

		mEnterVerificationCodeET = (EditText) view.findViewById(R.id.enter_verification_code_et);

		if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
			mEnterVerificationCodeET.setLetterSpacing((float)0.2);

		mEnterVerificationCodeET.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable s)
			{
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				setVerificationCodeText(true);
			}
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{

			}
		});

		mVerificationCodeTv = (TextView) view.findViewById(R.id.verification_code_tv);

		mVerifyButton = (Button)view.findViewById(R.id.verify_button);
		mVerifyButton.setOnClickListener(this);

		mResentVerificationTv = (TextView)view.findViewById(R.id.resend_verification_code_tv);
		mResentVerificationTv.setOnClickListener(this);

		mAccountManagement = new AccountManagement();

		if(mContext != null)
			mNetworkDetector = new NetworkDetector(mContext);

		mDeviceDatabaseHelper = new DeviceDatabaseHelper();
	}

	private void setVerificationCodeText(boolean verify)
	{
		if(verify)
		{
			mVerificationCodeTv.setText(R.string.enter_verification_code);
			mVerificationCodeTv.setTextColor(getResources().getColor(R.color.text_gray));
		}
		else
		{
			mEnterVerificationCodeET.setText("");
			mVerificationCodeTv.setText(R.string.failed_verification_code);
			mVerificationCodeTv.setTextColor(getResources().getColor(R.color.text_error));
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.verify_button:
			{
				 long regStartTime = mSettings.getLong(CommonConstants.APP_REGISTRATION_TIME,0);
				 long verificationTime = System.currentTimeMillis()-regStartTime;
                String appVerificationTime = null;
				int time =(int) verificationTime / 1000;
				if(time<=10){
					appVerificationTime = " 10 sec";
				}else if(time>10 && time<=20){
					appVerificationTime = "20 sec";
				}else if(time>20 && time<=30){
					appVerificationTime = "30 sec";
				}else if(time>30){
					appVerificationTime = " >30 sec";
				}

				GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.VERIFICATION_ENTERED_IN+" : "+ appVerificationTime,AppEvents.VERFICATIONCODE_ENTERED);
				ZaiusEvent emailVerificationeEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
				emailVerificationeEvt.action(AppEvents.VERIFICATION_ENTERED_IN+" : "+ appVerificationTime);
				try {
					ZaiusEventManager.getInstance().trackCustomEvent(emailVerificationeEvt);
				} catch (ZaiusException e) {
					e.printStackTrace();
				}


				if(!AppSDKConfiguration.getInstance(mContext).getEmailVerificationStatus())
				{
					if(mContext != null)
					{
						((EmailConfirmationActivity)mContext).startMainActivity();
					}
				}
				else
				{
					if (mEnterVerificationCodeET != null && mEnterVerificationCodeET.getText().toString().length() >= 6)
					{
						String userEmail = mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, null);
						String registrationID = mSettings.getString(PublicDefineGlob.PREFS_SAVED_REGISTRATION_ID, null);


						if (userEmail != null && registrationID != null)
						{
							ValidateUserAccount validateUserAccount = new ValidateUserAccount(userEmail, registrationID, mEnterVerificationCodeET.getText().toString());

							if(mContext != null)
							{
								((EmailConfirmationActivity)mContext).displayProgressDialog();

								AccountManager.getInstance(mContext).validateUserAccount(validateUserAccount,
									new Response.Listener<ValidateUserDetail>()
									{
										@Override
										public void onResponse (ValidateUserDetail response)
										{
											((EmailConfirmationActivity)mContext).dismissDialog();
											if (response != null)
											{
											 GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.EMAIL_VERIFICATION_SUCCESS,AppEvents.EMAIL_VERIFICATION_SUCCESS);

												ZaiusEvent emailVerificationSuccessEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
												emailVerificationSuccessEvt.action(AppEvents.EMAIL_VERIFICATION_SUCCESS);
												try {
													ZaiusEventManager.getInstance().trackCustomEvent(emailVerificationSuccessEvt);
												} catch (ZaiusException e) {
													e.printStackTrace();
												}

												restoreUserSetting(response);
												((EmailConfirmationActivity)mContext).switchFragment(VerifiedEmailFragment.newInstance(),true);
											}

										}
									},
									new Response.ErrorListener() {
										@Override
										public void onErrorResponse (VolleyError error)
										{
											((EmailConfirmationActivity) mContext).dismissDialog();
											GeAnalyticsInterface.getInstance().trackEvent(AppEvents.USER_LOGIN,AppEvents.EMAIL_VERIFICATION_FAILURE+" "+error.getMessage(),AppEvents.EMAIL_VERIFICATION_FAILURE);

											ZaiusEvent emailVerificationFailureEvt = new ZaiusEvent(AppEvents.USER_LOGIN);
											emailVerificationFailureEvt.action(AppEvents.EMAIL_VERIFICATION_FAILURE+" "+error.getMessage());
											try {
												ZaiusEventManager.getInstance().trackCustomEvent(emailVerificationFailureEvt);
											} catch (ZaiusException e) {
												e.printStackTrace();
											}
											setVerificationCodeText(false);

											if (error != null && error.networkResponse != null) {
												Log.d(TAG, error.networkResponse.toString());
												Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
											}
										}
									});



							}
						} else {
							if (mContext != null)
								Toast.makeText(mContext, R.string.oops_something_wrong_login_again, Toast.LENGTH_SHORT).show();
						}

					} else {
						if (mContext != null)
							Toast.makeText(mContext, R.string.enter_correct_verification_code, Toast.LENGTH_SHORT).show();
					}
				}

			}
			break;

			case R.id.resend_verification_code_tv:
			{
				showResentVerifyCodeConfirm();
			}
			break;

			case R.id.change_email_linearlayout:
			{
				((EmailConfirmationActivity)mContext).switchFragment(ChangeEmailFragment.newInstance(),true);
			}
			break;

		}
	}

	private void restoreUserSetting(ValidateUserDetail response)
	{
		String email = response.getEmail();
		String name = response.getName();

		Intent intent = new Intent();
		try
		{
			intent.putExtra(RegisterActivity.ENCRYPT_API_KEY, RSAUtils.encryptRSA(RegisterActivity.encryptKey, response.getAuthToken().getBytes()));
		}
		catch (Exception e)
		{
		}
		intent.setAction(RegisterActivity.BROADCAST_ACTION);
		mContext.sendBroadcast(intent);

		String savedToken = mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
		String savedUser = mSettings.getString(PublicDefineGlob.PREFS_TEMP_PORTAL_ID, "");
		int tempUnit = mSettings.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_F);

		boolean soundNotify = mSettings.getBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_SOUND, true);
		boolean vibrationNotify = mSettings.getBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_VIBRATE, true);
		boolean notifyOnCall = mSettings.getBoolean(PublicDefineGlob.PREFS_NOTIFY_ON_CALL, true);
		boolean remoteTimeout = mSettings.getBoolean("should_video_view_timeout", true);
		boolean donotDisturb = mSettings.getBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, false);
		long remainingDoNotDisturb = mSettings.getLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, 0);
		long donotDisturbTime = mSettings.getLong(PublicDefineGlob.PREFS_TIME_DO_NOT_DISTURB_EXPIRED, 0);

		boolean pullRefreshEvent = mSettings.getBoolean(MainActivity.PREF_SHOWCASE_PULL_REFRESH, false);
		boolean swipeDelete = mSettings.getBoolean(MainActivity.PREF_SHOWCASE_SWIPE_DELETE, false);
		boolean cameraView = mSettings.getBoolean(CameraListFragment.PREF_SHOWCASE_CAMERA_VIEW, false);
		boolean pullRefreshCameraList = mSettings.getBoolean(CameraListFragment.PREF_SHOWCASE_PULL_REFRESH, false);
		boolean cameraDetails = mSettings.getBoolean(CameraListFragment.PREF_SHOWCASE_CAMERA_DETAILS, false);
		boolean isP2pEnabled = mSettings.getBoolean(PublicDefineGlob.PREFS_IS_P2P_ENABLED, true);
		long lastTry = mSettings.getLong(PublicDefineGlob.PREFS_LAST_P2P_TRY, -1);
		boolean isDebugEnabled = mSettings.getBoolean(DebugFragment.PREFS_DEBUG_ENABLED, false);
		boolean useDevOta = mSettings.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false);
		boolean dontAskMeAgain = mSettings.getBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, false);
		int timeFormat = mSettings.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0);


		if (!savedUser.equalsIgnoreCase(email) && !savedUser.equalsIgnoreCase(response.getName()))
		{
			Log.i(TAG, "Saved user: " + savedUser + ", username: " + email + " --> clear");
			mSettings.clear();
			mSettings.putBoolean(SecureConfig.HAS_USED_SECURE_SHARE_PREFS, true);
			newUserAttemptingLogIn();
		}
		else
		{

			mSettings.putBoolean(MainActivity.PREF_SHOWCASE_PULL_REFRESH, pullRefreshEvent);
			mSettings.putBoolean(MainActivity.PREF_SHOWCASE_SWIPE_DELETE, swipeDelete);
			mSettings.putBoolean(CameraListFragment.PREF_SHOWCASE_CAMERA_VIEW, cameraView);
			mSettings.putBoolean(CameraListFragment.PREF_SHOWCASE_PULL_REFRESH, pullRefreshCameraList);
			mSettings.putBoolean(CameraListFragment.PREF_SHOWCASE_CAMERA_DETAILS, cameraDetails);

			mSettings.putBoolean(PublicDefineGlob.PREFS_IS_P2P_ENABLED, isP2pEnabled);
			mSettings.putLong(PublicDefineGlob.PREFS_LAST_P2P_TRY, lastTry);
			mSettings.putBoolean(DebugFragment.PREFS_DEBUG_ENABLED, isDebugEnabled);
			mSettings.putBoolean(DebugFragment.PREFS_USE_DEV_OTA, useDevOta);

			mSettings.putBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, dontAskMeAgain);
			mSettings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, tempUnit);
			mSettings.putBoolean(PublicDefineGlob.PREFS_USER_ACCESS_INFRA_OFFLINE, false);

			mSettings.putBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_SOUND, soundNotify);
			mSettings.putBoolean(PublicDefineGlob.PREFS_NOTIFY_BY_VIBRATE, vibrationNotify);
			mSettings.putBoolean("should_video_view_timeout", remoteTimeout);
			mSettings.putBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, donotDisturb);
			mSettings.putLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, remainingDoNotDisturb);
			mSettings.putLong(PublicDefineGlob.PREFS_TIME_DO_NOT_DISTURB_EXPIRED, donotDisturbTime);
			mSettings.putBoolean(PublicDefineGlob.PREFS_NOTIFY_ON_CALL, notifyOnCall);
			mSettings.putInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, timeFormat);
		}

		mSettings.putString(PublicDefineGlob.PREFS_TEMP_PORTAL_ID, savedUser);
		mSettings.putBoolean(PublicDefineGlob.PREFS_IS_FIRST_TIME, false);
		mSettings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, response.getAuthToken());
		mSettings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, name);
		mSettings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, email);

	}

	private void newUserAttemptingLogIn()
	{
		Api.getInstance().deleteDatabase();
		DeviceSingleton.getInstance().clearDevices();

		SecureConfig settings = HubbleApplication.AppConfig;
		boolean offlineMode = settings.getBoolean(PublicDefineGlob.PREFS_USER_ACCESS_INFRA_OFFLINE, false);

		if (!offlineMode)
		{
			// vox service should not take wakelock
			settings.putBoolean(PublicDefineGlob.PREFS_VOX_SHOULD_TAKE_WAKELOCK, false);
			// remove password when user logout
			settings.remove(PublicDefineGlob.PREFS_TEMP_PORTAL_PWD);

			// Remove all pending notification on Status bar
			NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();
		}
	}

	private void resendVerificationCode()
	{
		String email = mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, null);
		String registrationID = mSettings.getString(PublicDefineGlob.PREFS_SAVED_REGISTRATION_ID,null);
		String password = mSettings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_PWD,null);

		ResendVerificationEmail resendVerificationEmail = new ResendVerificationEmail(email,registrationID,password);

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
							Toast.makeText(mContext,mContext.getResources().getString(R.string.verification_code_sent),Toast.LENGTH_SHORT).show();

							mSettings.putString(PublicDefineGlob.PREFS_SAVED_REGISTRATION_ID, response.getUniqueRegistrationNumber());

						}

					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse (VolleyError error)
					{
						((EmailConfirmationActivity)mContext).dismissDialog();

						Toast.makeText(mContext,mContext.getResources().getString(R.string.verification_code_failed),Toast.LENGTH_SHORT).show();

						if (error != null && error.networkResponse != null) {
							Log.d(TAG, error.networkResponse.toString());
							Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
						}
					}
				});

	}

	private void showResentVerifyCodeConfirm()
	{
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		View resentCodeDialog = layoutInflater.inflate(R.layout.dialog_resent_verification, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
		alertDialogBuilder.setView(resentCodeDialog);

		alertDialogBuilder.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.dialog_ok),
						new DialogInterface.OnClickListener()
						{
							public void onClick (DialogInterface dialog, int id)
							{
								resendVerificationCode();
							}
						})
				.setNegativeButton(getResources().getString(R.string.dialog_cancel),
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								dialog.cancel();
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();
	}

    private boolean validateEmail(String email)
    {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
