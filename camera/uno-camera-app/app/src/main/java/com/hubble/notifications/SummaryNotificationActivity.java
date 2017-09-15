package com.hubble.notifications;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceEventSummary;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceEventSummaryResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceSummaryDetail;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.registration.PublicDefine;
import com.hubble.ui.eventsummary.EventSummaryConstant;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.Global;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.squareup.picasso.Picasso;
import com.util.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Admin on 17-05-2017.
 */
public class SummaryNotificationActivity extends FragmentActivity {
	private final String TAG = "SummaryActivity";

	public final static String SUMMARY_DEVICE_ID = "summary_device_id";
	public final static String SUMMARY_DAY = "summary_day";
	public final static String SUMMARY_DEVICE_NAME = "summary_device_name";
	public final static String SUMMARY_SNAP_URL = "summary_snap_url";
	public final static String SUMMARY_CLIP_URL = "summary_clip_url";

	private ImageView mSnapImage;
	private TextView mSummaryText;
	private Button mBtnIgnore;
	private Button mBtnViewSummary;
	private Button mBtnGoToCamera;

	private String mSummaryRegistrationId;
	private String mSummaryDay;
	private String mSummaryDeviceName;
	private String mSummarySnapUrl;
	private String mSummaryClipUrl;

	private String mApiKey;

	private boolean mImageLoaded = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"oncreate of SummaryNotificationActivity");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_notification_interaction);
		getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		final Context mContext = getApplicationContext();
		mApiKey = Global.getApiKey(mContext);
		if (mApiKey != null) {
			DeviceSingleton.getInstance().init(mApiKey, mContext);
		}

		Bundle extras = getIntent().getExtras();
		mSummaryRegistrationId = extras.getString(SUMMARY_DEVICE_ID);
		mSummaryDay = extras.getString(SUMMARY_DAY);
		mSummaryDeviceName = extras.getString(SUMMARY_DEVICE_NAME);
		mSummarySnapUrl = extras.getString(SUMMARY_SNAP_URL);

		mSnapImage = (ImageView) findViewById(R.id.notificationInteraction_imageVew);
		mSummaryText = (TextView) findViewById(R.id.notificationInteraction_eventText);
		mBtnViewSummary = (Button) findViewById(R.id.notificationInteraction_playButton);
		mBtnIgnore = (Button) findViewById(R.id.notificationInteraction_ignoreButton);
		mBtnGoToCamera = (Button) findViewById(R.id.notificationInteraction_cameraButton);

		mSummaryText.setText(String.format(getString(R.string.summary_generated_message), mSummaryDeviceName));
		mBtnGoToCamera.setText(R.string.goto_summary_event);
		mBtnViewSummary.setVisibility(View.GONE);
		mBtnViewSummary.setText(getString(R.string.play_summary_event));
		mBtnViewSummary.setTextColor(getResources().getColor(R.color.material_deep_teal_500));

		if (mSummarySnapUrl != null && !mSummarySnapUrl.isEmpty()) {
			Picasso.with(this).load(mSummarySnapUrl).placeholder(R.drawable.notificationnteraction_defaultimage).into(mSnapImage);
			mImageLoaded = true;
		}

		mBtnViewSummary.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSummaryClipUrl != null) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse(mSummaryClipUrl), "video/mp4");
					startActivity(intent);
				}else {
					Toast.makeText(mContext, mContext.getString(R.string.cannot_play_summary), Toast.LENGTH_SHORT).show();
				}
				finish();
			}
		});


		mBtnIgnore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mBtnGoToCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mApiKey != null) {
					Intent intent = new Intent(SummaryNotificationActivity.this, MainActivity.class);
					Device selectedDevice = DeviceSingleton.getInstance().getDeviceByRegId(mSummaryRegistrationId);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					if (selectedDevice != null) {
						// only go to the device if we found it
						DeviceSingleton.getInstance().setSelectedDevice(selectedDevice);
						HubbleApplication.AppConfig.putBoolean(PublicDefine.PREFS_GO_DIRECTLY_TO_SUMMARY, true);
						HubbleApplication.AppConfig.putString(PublicDefine.PREFS_GO_DIRECTLY_TO_REGID, mSummaryRegistrationId);
					}
					startActivity(intent);
				} else {
					Toast.makeText(mContext, mContext.getString(R.string.cannot_launch_summary), Toast.LENGTH_SHORT).show();
				}
				finish();
			}
		});
		//Fetch ImageUrl and clipUrl;
		fetchClipImageUrl();
	}

	private void fetchClipImageUrl() {
		final DeviceEventSummary deviceEventSummary = new DeviceEventSummary(mApiKey, mSummaryRegistrationId);
		deviceEventSummary.setWindow(EventSummaryConstant.WINDOW_DAILY);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date convertedDate = new Date();
		try {
			convertedDate = dateFormat.parse(mSummaryDay);
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			deviceEventSummary.setDay(sdf.format(convertedDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DeviceManagerService deviceManagerService = DeviceManagerService.getInstance(getApplicationContext());
		deviceManagerService.getDeviceEventSummary(deviceEventSummary, new Response.Listener<DeviceEventSummaryResponse>() {
			@Override
			public void onResponse(DeviceEventSummaryResponse response) {
				String snapUrl = null;
				String clipUrl = null;
				if (response != null && response.getStatus() == 200 && response.getMessage().equalsIgnoreCase("Success")) {
					DeviceEventSummaryResponse.DeviceSummaryDaily deviceSummaryDaily = response.getDeviceSummaryDaily();
					DeviceSummaryDetail[] deviceSummaryDetailArray = deviceSummaryDaily.getDeviceSummaryDailyDetail();
					DeviceSummaryDetail.DailySummaryDetail[] dailyDetailArray = deviceSummaryDetailArray[0].getDailySummaryDetail();
					for(DeviceSummaryDetail.DailySummaryDetail dailyDetail:dailyDetailArray) {
						CommonUtil.EventType type = CommonUtil.EventType.fromAlertIntType(dailyDetail.getAlertType());
						if (type == CommonUtil.EventType.MOTION && dailyDetail.getSummaryUrl() != null
								&& !dailyDetail.getSummaryUrl().equalsIgnoreCase(EventSummaryConstant.EVENT_SUMMARY_NOT_COMPUTED) ) {
							snapUrl = dailyDetail.getSummarySnapUrl();
							clipUrl = dailyDetail.getSummaryUrl();
						}
					}

				}
				if (snapUrl != null && !snapUrl.isEmpty()) {
					mSummarySnapUrl = snapUrl;
					Picasso.with(getApplicationContext()).load(snapUrl).placeholder(R.drawable.notificationnteraction_defaultimage).into(mSnapImage);
				}
				if (clipUrl != null && !clipUrl.isEmpty()) {
					mSummaryClipUrl = clipUrl;
					mBtnViewSummary.setVisibility(View.VISIBLE);
					mBtnViewSummary.setEnabled(true);
				} else {
					mSummaryClipUrl = null;
					mBtnViewSummary.setVisibility(View.GONE);
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				mSummaryClipUrl = null;
				mBtnViewSummary.setVisibility(View.GONE);
			}
		});
	}
}
