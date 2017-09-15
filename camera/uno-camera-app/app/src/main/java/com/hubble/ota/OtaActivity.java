package com.hubble.ota;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.Log;

import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubbleconnected.camera.R;


public class OtaActivity extends AppCompatActivity implements View.OnClickListener
{
	private static final String TAG = OtaActivity.class.getSimpleName();

	private static final String ROOT_FRAGMENT = "RootFragment";

	public static final String IS_FROM_SETUP = "isFromSetup";
	public static final String CHECK_FIRMWARE_UPGRADE_RESULT = "checkfwupgraderesult";
	public static final String DEVICE_MODEL_ID = "deviceModelID";

	FragmentManager mFragmentManager;
	FragmentTransaction mFragmentTransaction;

	private boolean isFromSetup = false;
	private CheckFirmwareUpdateResult mCheckFirmwareUpdateResult;
	private String mDeviceModelID;

	private OtaUIFragment mOTAFragment ;
	private static final int FIRMWARE_UPGRADE_REQUEST_CODE = 0x01;

	private PowerManager.WakeLock mWakeUpLock;
	private boolean isDeviceOTA = false;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ota_activity_layout);

		Bundle bundle = getIntent().getExtras();

		isFromSetup  = bundle.getBoolean(IS_FROM_SETUP);
		mCheckFirmwareUpdateResult = (CheckFirmwareUpdateResult)bundle.getSerializable(CHECK_FIRMWARE_UPGRADE_RESULT);
		mDeviceModelID = bundle.getString(DEVICE_MODEL_ID);

		if(savedInstanceState == null)
		{
			mOTAFragment = OtaUIFragment.newInstance(isFromSetup,mDeviceModelID, mCheckFirmwareUpdateResult);
			switchFragment(mOTAFragment, false);
		}

		setActionBar();
		acquireLock();
	}

	private void acquireLock()
	{
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeUpLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "otawakeuplog");
		mWakeUpLock.acquire();
	}

	private void releaseLock()
	{
		if(mWakeUpLock != null && mWakeUpLock.isHeld())
		{
			mWakeUpLock.release();
		}
	}

	private void setActionBar()
	{
		TextView toolbarTv = (TextView) findViewById(R.id.tv_toolbar_title);
		toolbarTv.setText(getString(R.string.updating));

		ImageView backIv=(ImageView)findViewById(R.id.tv_toolbar_back) ;
		backIv.setOnClickListener(this);

		View toolbarRelativeLayout = findViewById(R.id.header_setup);

		if(isFromSetup)
		{
			toolbarTv.setTextColor(Color.WHITE);
			backIv.setImageDrawable(getResources().getDrawable(R.drawable.back));
			toolbarRelativeLayout.setBackgroundColor(getResources().getColor(R.color.main_blue));
		}
		else
		{
			toolbarTv.setTextColor(Color.WHITE);
			backIv.setImageDrawable(getResources().getDrawable(R.drawable.vector_drawable_back));
			toolbarRelativeLayout.setBackgroundColor(getResources().getColor(R.color.viewfinder_primary_bg));
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();
		releaseLock();
	}

	public void switchFragment(Fragment fragment, boolean addToBackStack)
	{
		if (fragment != null)
		{
			getFragmentManager().popBackStackImmediate(ROOT_FRAGMENT, 0);
			Fragment currentFragment = getFragmentManager().findFragmentById(R.id.main_content);
			FragmentTransaction ft = getFragmentManager().beginTransaction();

			if (addToBackStack)
			{
				ft.replace(R.id.main_content, fragment);
				ft.addToBackStack(null);
			}
			else
			{
				if (currentFragment == null)
				{
					ft.add(R.id.main_content, fragment, ROOT_FRAGMENT);
				}
				else
				{
					ft.replace(R.id.main_content, fragment, ROOT_FRAGMENT);
				}
			}
			ft.commit();
		}
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.tv_toolbar_back:
				onBackPressed();
				break;
		}

	}

	@Override
	public void onBackPressed()
	{
		mOTAFragment.onBackPressed();
	}

	public void onfinish(int resultCode)
	{
		Intent intent=new Intent();
		setResult(resultCode,intent);
		releaseLock();
		finish();
	}
}
