package com.hubble.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.SwitchCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actor.model.Direction;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.blinkhd.MusicFragment;
import com.blinkhd.TalkbackFragment;
import com.blinkhd.playback.CaptureFragment;
import com.crittercism.app.Crittercism;
import com.cvision.stun.StunClient;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.VideoPlaybackTasks;
import com.hubble.analytics.GoogleAnalyticsController;
import com.hubble.command.CameraCommandUtils;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.devcomm.impl.hubble.P2pCommunicationManager;
import com.hubble.file.FileService;
import com.hubble.framework.networkinterface.v1.pojo.HubbleRequest;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceEvent;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceStatus;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceEventDetail;
import com.hubble.framework.service.cloudclient.device.pojo.response.StatusDetails;
import com.hubble.framework.service.cloudclient.user.pojo.response.UserSubscriptionPlanResponse;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.framework.service.p2p.IP2pListener;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.framework.service.p2p.P2pService;
import com.hubble.framework.service.p2p.P2pUtils;
import com.hubble.framework.service.subscription.SubscriptionInfo;
import com.hubble.framework.service.subscription.SubscriptionService;
import com.hubble.helpers.AsyncPackage;
import com.hubble.model.VideoBandwidthSupervisor;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.registration.interfaces.IWifiScanUpdater;
import com.hubble.registration.models.BabyMonitorAuthentication;
import com.hubble.registration.models.LegacyCamProfile;
import com.hubble.registration.tasks.CheckFirmwareUpdateResult;
import com.hubble.registration.tasks.CheckFirmwareUpdateTask;
import com.hubble.registration.tasks.ConnectToNetworkTask;
import com.hubble.registration.tasks.RemoteStreamTask;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubble.registration.tasks.comm.UDTRequestSendRecvTask;
import com.hubble.registration.ui.CommonDialogListener;
import com.hubble.streaming.HubbleSessionManager;
import com.hubble.subscription.ManagePlanActivity;
import com.hubble.subscription.OfferExecutor;
import com.hubble.subscription.PlanFragment;
import com.hubble.ui.eventsummary.EventSummaryConstant;
import com.hubble.ui.eventsummary.VideoAnalyticsOfferDialog;
import com.hubble.util.BgMonitorData;
import com.hubble.util.CommandUtils;
import com.hubble.util.CommonConstants;
import com.hubble.util.P2pSettingUtils;

import com.media.ffmpeg.FFMpeg;
import com.media.ffmpeg.FFMpegException;
import com.media.ffmpeg.FFMpegPlayer;
import com.media.ffmpeg.android.FFMpegMovieViewAndroid;
import com.msc3.BabyMonitorRelayAuthentication;
import com.msc3.ITimerUpdater;
import com.msc3.RtmpStreamTask;
import com.msc3.Streamer;
import com.msc3.update.CheckVersionFW;
import com.msc3.update.IpAndVersion;
import com.nxcomm.blinkhd.ui.AccountSettingFragment;
import com.nxcomm.blinkhd.ui.BaseFragment;
import com.nxcomm.blinkhd.ui.ILiveFragmentCallback;
import com.nxcomm.blinkhd.ui.LiveCameraActionButtonListener;
import com.nxcomm.blinkhd.ui.MainActivity;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.nxcomm.blinkhd.ui.dialog.StandByTimeoutDialog;
import com.nxcomm.blinkhd.ui.dialog.VideoTimeoutDialog;
import com.nxcomm.jstun_android.P2pClient;
import com.squareup.picasso.Picasso;
import com.util.AppEvents;
import com.util.ChangePrivacyMode;
import com.util.CommonUtil;
import com.util.DeviceWakeup;
import com.util.NotificationStatusTask;
import com.util.PrivacyCustomDialog;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import base.hubble.CustomHorizontalScrollView;
import base.hubble.CustomVerticalScrollView;
import base.hubble.IAsyncTaskCommonHandler;
import base.hubble.PublicDefineGlob;
import base.hubble.constants.Streaming;
import base.hubble.database.FreeTrial;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

public class ViewFinderFragment extends BaseFragment implements Handler.Callback, IP2pListener, LiveCameraActionButtonListener,CompoundButton.OnCheckedChangeListener,
        ILiveFragmentCallback, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener,VideoBandwidthSupervisor.VideoBandwidthSupervisorInterface {

    private static final String TAG = "ViewFinderFragment";

    private static final String SHOULD_VIDEO_TIMEOUT = "should_video_view_timeout";
    private static final String DEBUG_TAG = "Gesture";
    public static int SHOULD_EXIT_NOW_YES = 0x0001FFFF;
    private static final long VIDEO_TIMEOUT = 15 * 60 * 1000;
    private static final long QUICKVIEW_TIMEOUT = 5 * 60 * 1000;
    private static final long MAX_BUFFERING_TIME = 30 * 1000;
    private static final long STAND_BY_VIDEO_TIMEOUT = 60 * 1000;

    private static final int WARNING_TIMEOUT = 30;
    private static final int DEFAULT_WAIT_TIMEOUT = 5*1000;
    private static final int QUERY_TEMPERATURE_FREQUENCY=5*60*1000;

    private static final int MAX_STREAM_RETRY_RTMP=10;
    private static final int MAX_STREAM_RETRY=3;
    private static final int SNAPSHOT_FREQUENCY=10*60*1000;


    private static IpAndVersion device;
    private Device selectedDevice;
    private String mRegistrationID;
    private Dialog mP2pOutdatedDialog = null;

    private OnFragmentInteractionListener mListener;

    private FFMpegPlayer mPlayer;
    private static SecureConfig settings = HubbleApplication.AppConfig;

    private FFMpegMovieViewAndroid mMovieView;
    private String remoteIP = "";
    private boolean mIsFirstTime = true;
    private boolean initialized = false;
    private BroadcastReceiver broadcaster;
    private Context context;
    private String filePath = null;
    private ILiveFragmentCallback liveFragmentListener = null;
    private LiveCameraActionButtonListener liveCameraActionButtonListener = null;
    private boolean shouldEnableMic = false;
    private boolean shouldTurnOnPanTilt = false;
    private boolean shouldTurnOnMelody = false;
    private boolean noTriggerResolutionChanged = false;
    private DisplayMetrics metrics;
    private FrameLayout zoomBarContainer;
    private long view_session_start_time = -1;
    private P2pClient[] p2pClient = null;
    private boolean isInBGMonitoring = false;
    private Activity mActivity;
    View view;
    private volatile boolean needPanTilt = false;
    private Thread panTiltThread;
    private volatile boolean shouldStop = false;
    private static final int DIRECTION_STOP = 0;
    private static final int DIRECTION_INVALID = -1;
    private volatile int currentDirection = DIRECTION_INVALID;
    private static final int DIRECTION_LEFT = 1;
    private static final int DIRECTION_RIGHT = 2;
    private static final int DIRECTION_UP = 3;
    private static final int DIRECTION_DOWN = 4;

    private ImageView mLoadingSpinner;
    private ImageView mLatestSnap;
    private TextView mLoadingMessageText;
    private LinearLayout mLoadingInfoLayout;

    private LinearLayout mErrorLayout;
    private ImageView mErrorImageView;
    private TextView mError1Tv,mError2Tv,mError3Tv;
    private Button mRetryButton;

    private GridView mControlGrid;
    private ViewFinderMenuAdapter mGridAdapter;
    private RelativeLayout mDefaultControlsLayout;
    private LinearLayout mMuteLayout, mHDLayout;
    private FrameLayout mPlaybackFrame;
    private ViewFinderMenuItem HD = ViewFinderMenuItem.HD;
    private ViewFinderMenuItem MUTE = ViewFinderMenuItem.MUTE;
    private LiveCameraActionButtonListener mLiveActionListener;

    private ImageView mSubscButton;
    private RelativeLayout mSubscExpireLayout;
    private TextView mSubscDaysLeft,mSubscDetail;
    private Button mUpgradePlan;
    private ImageView mSubscCloseButton;
    private ImageView mSummaryButton;

    private ImageView mEventButton = null;
    private TextView mEventHistory = null;
	private TextView mEventCount = null;
    private float mCurrentTemperature;

    private TalkbackFragment mTalkbackFragment;
    private MusicFragment mMusicFragment;
    private CaptureFragment mCaptureFragment;
    private PanTiltFragment mPanTiltFragment;

    private DeviceManagerService mDeviceManagerService;
	private String accessToken;
	private DeviceEvent mDeviceEvent;
	private boolean isEventFetchInProgress = false;
    private TextView mLiveStatusView;
    private ImageView mHDImage,mMuteImage,mHumidityImage;
    private TextView mHDText,mMuteText,mTempText,mHumidityText;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetectorCompat mGestureDetector;
    private PointF lastDownPoint = new PointF(0f, 0f);
    private boolean mIsControlsVisible=false,mIsControlFragmentVisible=false;
    private List<Device> mDevices = new ArrayList<Device>();
    private Spinner mDeviceSpinner;
    private TextView mDeviceText;
    private ArrayAdapter mSpinnerAdapter;
    private IViewFinderCallback mViewFinderCallback;

    private SwitchCompat mDeviceOnOffSwitch;
    private RelativeLayout mLiveStreamingRelativeLayout,mQuickSettingRelativeLayout;
    private TextView mQuickUpTextView,mQuickUpSummaryTextView;
    private ImageView mQuickUpImageView;
    private Animation mAnimation;

    private SharedPreferences mSharedPreferences;
    private RelativeLayout mNotificationOffLayout;
    private boolean mIsNotificationOn=true;
    private ProgressDialog mProgressDialog;
    private SwitchCompat mNotificationSwitch;
    private LinearLayout mAlertLayout;
    private Timer mQueryTempTimer;
    private Timer mQueryDateTimeTimer;
    private LinearLayout mDebugLayout;
    private TextView mFrameRateText;
    private TextView mResolutionText;
    private TextView mBitRateText;
    private TextView mGlobalBitRateText;
    private TextView mWifiSignalText;
    private ImageView mConnectionTypeImg;
    private TextView mLoadingTimeText;

    private ImageView mOtaAvailableIv;
    private LinearLayout mSwitchLayout;

    private Fragment mCurrentControlFragment;

    private RelativeLayout mViewFinderInfoLayout;
    private View mSeparatorView;
    private RelativeLayout mEventListLayout;
    private View mToolbar;
    private RelativeLayout mStreamingLayout;
    private boolean mIsStreaming=false;

    private int mStreamRetryCount=0;

    DeviceWakeup mDeviceWakeup;
    private SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private EventData eventData;
    private CheckFirmwareUpdateResult mCheckFirmwareUpdateResult = null;
    private boolean isAllowFirmwareUpgrade = false;
    private boolean mUSeP2P=false;
    private int mSelectedPosition;
    private boolean isDeviceCharging = false;
    ImageView mSettingsButton;
    private Context mContext;
    private boolean mIsFragmentHidden=false;
    private boolean mIsStreamingTimedOut=false;
    private boolean mIsScreenTurnedOFF=false;
    private boolean mIsRTMPLinkReceived=false;
    private boolean mIsHDChanged=false;
    BroadcastReceiver mScreenStateReceiver;
    private boolean mIsTimeFormat12=true;


    private boolean isLocalVerify = false;
    private boolean isDeviceLocal = false;

    public class DeviceAttributes {
        int video_width = 0;
        int video_height = 0;
        int currentConnectionMode;
        boolean activity_has_stopped;
        boolean is_upgrading = false;
        int current_bitrate_value = PublicDefineGlob.VIDEO_BITRATE_200; // PublicDefineGlob.VIDEO_BITRATE_600;
        String device_ip;
        int device_port;
    }

    private class SessionAttributes {
        long create_session_start_time = System.currentTimeMillis();
        long open_stream_start_time = System.currentTimeMillis();
        long connecting_start_time = System.currentTimeMillis();
        long view_session_start_time = -1;
        int default_screen_height;
        int default_screen_width;
        int default_width;
        int default_height;
        float scale = 1f;
        float lastScaleFactor = 1f;
        float ratio = 0;
        float pressedX;
        float pressedY;
        float mx, my;
        int remote_reconnect_times = 0;
        String string_currentSSID = "string_currentSSID";
        int currentBottomMenuPosition = -1; // Checking status on configuration changes
        boolean recOrSnap;
        boolean isRec;
        boolean isPortrait = true;
        boolean userWantToCancel;
        boolean isVideoTimeout = false;
        boolean viewRelayRtmp = false;
        boolean isDebugEnabled = false;
        boolean isAdaptiveBitrateEnabled = false;
        String filePath;
        BabyMonitorAuthentication babyMonitorAuthentication;
        boolean viewP2p = false;
        int unexpectedLocalRetries = 0;
        int p2pTries = 0;
        String reservedRtmpUrl = null;
        P2pClient[] p2pClients = null;
        private boolean isStandByTimeout = false;
    }

    private DeviceAttributes deviceAttributes = new DeviceAttributes();
    private SessionAttributes sessionAttributes = new SessionAttributes();
    private VideoPlaybackTasks videoPlaybackTasks = new VideoPlaybackTasks();

    private IViewFinderEventListCallBack mIViewFinderEventListCallBack = null;

    private boolean mIsSummaryOptIn = false;
    private OfferExecutor mOfferExecutor;
    private VideoAnalyticsOfferDialog mVideoAnalyticsOfferDialog;
    private Dialog mEnableProgressDialog;
    private Dialog mFirmwareUgradeProgressDialog;
    private long timeTakenToStream;
    private long cameraStreamingStartTime;
    private long vfSceenStartTime;
    private long vfScreenTime;

    private boolean mIsFromOta = false;
    private String mNewFirmwareVersion = null;

    private boolean mIsSubscLayoutVisible = false;


    public ViewFinderFragment() {
        // Required empty public constructor
    }

    public static ViewFinderFragment newInstance(String registrationId){
        ViewFinderFragment viewFinderFragment=new ViewFinderFragment();

        Bundle bundle = new Bundle();
        bundle.putString(CommonConstants.VIEW_FINDER_CAMERA_REG_ID, registrationId);
        viewFinderFragment.setArguments(bundle);

        return viewFinderFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventData = new EventData();
        mSharedPreferences = mActivity.getSharedPreferences("app_config", Context.MODE_PRIVATE);
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        accessToken=HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
        sessionAttributes.isDebugEnabled = settings.getBoolean(DebugFragment.PREFS_DEBUG_ENABLED, false);
        isDeviceCharging = false;
        mOfferExecutor = new OfferExecutor(mActivity);

        mRegistrationID=getArguments().getString(CommonConstants.VIEW_FINDER_CAMERA_REG_ID);
        selectedDevice=DeviceSingleton.getInstance().getDeviceByRegId(mRegistrationID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView");

        cameraStreamingStartTime = System.currentTimeMillis();
        view = inflater.inflate(R.layout.fragment_view_finder, container, false);
        if (!initialized) {
            initialize();
        }
        isInBGMonitoring = false;

        if (broadcaster == null) {
            broadcaster = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    Log.i(TAG, "Needs to close video view");
                    if(ConnectivityManager.CONNECTIVITY_ACTION.equalsIgnoreCase(intent.getAction())) {
                        // when network is changed, reset this flag
                        isLocalVerify = false;
                    }
                    else {
                        onCameraRemoved(context, intent);
                    }
                }
            };
        }
        try {
            IntentFilter intentFilter = new IntentFilter(PublicDefine.NOTIFY_NOTIFY_DEVICE_REMOVAL);
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mActivity.registerReceiver(broadcaster, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //fetch number of events
        mEventButton = (ImageView) view.findViewById(R.id.event_Load_button);
        mEventButton.setEnabled(true);
        mEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIViewFinderEventListCallBack != null) {
                    mIViewFinderEventListCallBack.onClick(CommonConstants.CLICK_TYPE_EVENT_HISTORY);
                }
            }
        });
        mEventHistory = (TextView) view.findViewById(R.id.event_history);
        mEventHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIViewFinderEventListCallBack != null) {
                    mIViewFinderEventListCallBack.onClick(CommonConstants.CLICK_TYPE_EVENT_HISTORY);
                }
            }
        });

        mEventCount = (TextView) view.findViewById(R.id.event_count);
        mEventCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIViewFinderEventListCallBack != null) {
                    mIViewFinderEventListCallBack.onClick(CommonConstants.CLICK_TYPE_EVENT_HISTORY);
                }
            }
        });

        initDeviceManager();
        fetchEventForCamera();
        mSubscExpireLayout = (RelativeLayout) view.findViewById(R.id.subsc_expire_layout);
        mSubscExpireLayout.setVisibility(View.GONE);
        mSubscDaysLeft = (TextView) view.findViewById(R.id.expiry_info1);
        mSubscDetail = (TextView) view.findViewById(R.id.expiry_info2);

        mSubscButton = (ImageView) view.findViewById(R.id.promot_subscription_button);
        mSubscButton.setVisibility(View.GONE);
        mSubscButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubscButton.setVisibility(View.GONE);
                mSubscExpireLayout.setVisibility(View.VISIBLE);
            }
        });

        mUpgradePlan = (Button) view.findViewById(R.id.upgrade_plan);
        mUpgradePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubscExpireLayout.setVisibility(View.GONE);
                mSubscButton.setVisibility(View.VISIBLE);
                if (CommonUtil.isInternetAvailable(mContext)) {
                    Intent intent = new Intent(mContext, ManagePlanActivity.class);
                    startActivity(intent);
                } else {
                    if(mContext != null && getActivity() != null) {
                        Toast.makeText(mContext, R.string.enable_internet_connection, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        mSubscCloseButton = (ImageView) view.findViewById(R.id.promot_subscription_button_close);
        mSubscCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubscExpireLayout.setVisibility(View.GONE);
                mSubscButton.setVisibility(View.VISIBLE);
            }
        });

        mSummaryButton = (ImageView) view.findViewById(R.id.event_summary_button);
        mSummaryButton.setVisibility(View.GONE);
        mSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSummaryOptIn) {
                    if (mIViewFinderEventListCallBack != null) {
                        mIViewFinderEventListCallBack.onClick(CommonConstants.LAUNCH_SUMMARY_FROM_VF);
                    }
                } else {
                    dismissVideoAnalyticsDialog();
                    mVideoAnalyticsOfferDialog = new VideoAnalyticsOfferDialog(mActivity, new VideoAnalyticsOfferDialog.IVAOfferListener() {
                        @Override
                        public void vaOfferOptIn() {
                            dismissVideoAnalyticsDialog();
                            mEnableProgressDialog = ProgressDialog.show(mActivity, null, getString(R.string.va_opt_in_progress));
                            mOfferExecutor.consumeUserOffer(new OfferExecutor.IOfferConsumeResponse() {
                                @Override
                                public void onOfferConsumeResponse(boolean consumeSuccess) {
                                    if (mActivity != null && isAdded()) {
                                        dismissEnableProgressDialog();
                                        mIsSummaryOptIn = consumeSuccess;
                                        if (mIsSummaryOptIn) {
                                            dismissVideoAnalyticsDialog();
                                            mVideoAnalyticsOfferDialog = new VideoAnalyticsOfferDialog(mActivity, EventSummaryConstant.VA_HINT_DIALOG);
                                            mVideoAnalyticsOfferDialog.show();
                                        }else {
                                            Toast.makeText(mActivity, getString(R.string.va_opt_in_failure), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            });
                        }
                    }, EventSummaryConstant.VA_OFFER_DIALOG);
                    mVideoAnalyticsOfferDialog.show();
                }
            }
        });


        if (CommonUtil.getDailySummaryFeatureAvailable(mActivity)) {
            mSummaryButton.setVisibility(View.VISIBLE);
        } else {
            mSummaryButton.setVisibility(View.GONE);
        }

        mIsSummaryOptIn = CommonUtil.getDailySummaryFeatureOptedIn(mActivity);

        //Check for Video summary
        if ((!CommonUtil.getDailySummaryFeatureAvailable(mContext) || !CommonUtil.getDailySummaryFeatureAvailable(mContext))
                && (selectedDevice != null && !selectedDevice.getProfile().getModelId().
                equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT))) {
            mOfferExecutor.checkUserOfferOptIn(new OfferExecutor.IOfferOptInResponse() {
                @Override
                public void onOfferOptInResponse(boolean isOfferAvailable, boolean isOfferOptedIn) {

                    if(mActivity!=null) {
                        CommonUtil.setDailySummaryFeatureAvailable(mActivity, isOfferAvailable);
                        CommonUtil.setDailySummaryFeatureOptedIn(mActivity, isOfferOptedIn);
                        if (isAdded()) {
                            mIsSummaryOptIn = isOfferOptedIn;
                            if (isOfferAvailable) {
                                mSummaryButton.setVisibility(View.VISIBLE);
                            } else {
                                mSummaryButton.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
        }

        if (mContext != null && selectedDevice != null && !(selectedDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0)) {
            if (CommonUtil.getSettingInfo(mContext, selectedDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, false)) {
                mOtaAvailableIv.setVisibility(View.VISIBLE);
            }else {
                mOtaAvailableIv.setVisibility(View.GONE);
            }
        }


        //broadcast receiver to receive screen on/off state
        mScreenStateReceiver=new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    Log.d(TAG,"Screen went OFF");
                    mIsScreenTurnedOFF=true;
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    Log.d(TAG,"Screen went ON");
                }
            }
        };

        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mActivity.registerReceiver(mScreenStateReceiver, screenStateFilter);

        if (selectedDevice != null && !selectedDevice.getProfile().getModelId().
                equalsIgnoreCase(PublicDefine.MODEL_ID_SMART_NURSERY)&& (!selectedDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_ORBIT)
                || (Util.isThisVersionGreaterThan(selectedDevice.getProfile().getFirmwareVersion(),PublicDefine.ORBIT_PLAN_ENABLE_FIRMWARE_VERSION)))) {
            checkUserSubscription();
        }

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");

        if(selectedDevice==null && mActivity!=null){
            Toast.makeText(context,getString(R.string.viewfinder_camera_deleted),Toast.LENGTH_SHORT).show();
            mActivity.finish();
        }

        if (selectedDevice != null && !selectedDevice.getProfile().isAvailable())
        {
            boolean returnFromHere = false;
            if(selectedDevice.getProfile().isStandBySupported())
            {
                if(selectedDevice.getProfile().getDeviceStatus() == CameraStatusView.DEVICE_STATUS_OFFLINE)
                {
                    returnFromHere = true;
                }
            }
            else
            {
                returnFromHere = true;
            }
            if(returnFromHere)
            {
                Crittercism.leaveBreadcrumb(TAG + " onResume");
                Toast.makeText(getActivity(), getActivity().getString(R.string.cannot_go_to_camera), Toast.LENGTH_SHORT).show();
                startMainActivity();
            }
        }
        liveFragmentListener=this;

        if(selectedDevice!=null) {
            String mPrivacyMode = selectedDevice.getProfile().getDeviceAttributes().getPrivacyMode();
           // mIsNotificationOn = selectedDevice.getProfile().getDeviceAttributes().getPrivacyMode();//mSharedPreferences.getBoolean(selectedDevice.getProfile().getName()+"notification", true);
            if((mPrivacyMode == null) || (!TextUtils.isEmpty(mPrivacyMode) && mPrivacyMode.equalsIgnoreCase("0"))){
                mIsNotificationOn = true;
            }else{
                mIsNotificationOn = false;
            }

        }
        if (mIsNotificationOn) {
            mDeviceOnOffSwitch.setOnCheckedChangeListener(null);
            mDeviceOnOffSwitch.setChecked(true);
            mDeviceOnOffSwitch.setOnCheckedChangeListener(ViewFinderFragment.this);
        } else {
            mDeviceOnOffSwitch.setOnCheckedChangeListener(null);
            mDeviceOnOffSwitch.setChecked(false);
            mDeviceOnOffSwitch.setOnCheckedChangeListener(ViewFinderFragment.this);
        }
        isInBGMonitoring=false;
        BgMonitorData.getInstance()
                .setRegistrationId("undefine")
                .setShouldEnableBgAfterQuitView(false);
        setDeviceList();
        mIsHDChanged=false;

    }

    private  void startMainActivity()
    {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        mGestureDetector = new GestureDetectorCompat(mActivity, this);
        mGestureDetector.setOnDoubleTapListener(this);
        mContext = context;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mGestureDetector = new GestureDetectorCompat(mActivity, this);
        mGestureDetector.setOnDoubleTapListener(this);
        mActivity = activity;
        mContext=activity;
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
        mListener = null;
        liveFragmentListener=null;
        mContext= null;
        mActivity=null;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        if(mActivity==null)
            mActivity=getActivity();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        Log.d(TAG, "onStart");
        super.onStart();

        isLocalVerify = false;

        if(selectedDevice==null || selectedDevice.getProfile()==null){
            //Toast.makeText(mActivity,getString(R.string.viewfinder_camera_deleted),Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }

        deviceAttributes.activity_has_stopped = false;
        sessionAttributes.userWantToCancel = false;
        sessionAttributes.unexpectedLocalRetries = 0;

        if (P2pSettingUtils.hasP2pFeature())
        {
            sessionAttributes.p2pClients = null;
            sessionAttributes.p2pTries = 0;
            sessionAttributes.reservedRtmpUrl = null;
        }

        if (sessionAttributes.isVideoTimeout == false && deviceAttributes.is_upgrading == false) {

            if (P2pSettingUtils.hasP2pFeature())
            {
                P2pCommunicationManager.getInstance().updateP2pCommHandler(null);
            }

            if (mIsNotificationOn && selectedDevice != null) {
                if (!selectedDevice.getProfile().isStandBySupported()) {
                    scanAndViewCamera();
                }else {
                    checkDeviceStatus();
                }
            } else {
                //show notification off layout in notification is disabled
                mSwitchLayout.setVisibility(View.GONE);
                mTempText.setVisibility(View.GONE);
                mLiveStreamingRelativeLayout.setVisibility(View.INVISIBLE);
                mNotificationOffLayout.setVisibility(View.VISIBLE);
                mNotificationSwitch.setOnCheckedChangeListener(null);
                mNotificationSwitch.setChecked(false);
                mNotificationSwitch.setOnCheckedChangeListener(ViewFinderFragment.this);
                mOtaAvailableIv.setVisibility(View.GONE);
                mNotificationSwitch.setTextColor(getResources().getColor(R.color.text_gray));
                mOtaAvailableIv.setVisibility(View.GONE);
            }

        }
        needPanTilt = false;
        startPanTiltThread();


    }

    public boolean isOrbitP2PEnabled()
    {
        if(selectedDevice != null && selectedDevice.getProfile().isStandBySupported())
        {
            return settings.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);

        }
        return true;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "ViewFinderFragment onPaused");
        clearKeepScreenOnFlags();
        //todo check if required
        //hideDialogFwPatchFound();
        // setP2pOutdatedDialogShown(false);
        vfScreenTime = System.currentTimeMillis() - vfSceenStartTime;
        int time =(int) vfScreenTime / 1000;
        String streamingTime = null;
        Log.d("LoginTime","LoginTime : "+timeTakenToStream + " Sec = "+time);
        if(time<=1){
            streamingTime = "1 sec";
        }else if(time>1 && time<=3){
            streamingTime = "3 sec";
        }else if(time>3 && time<=5){
            streamingTime = "5 sec";
        }else if(time>5 && time<=10){
            streamingTime = "10 sec";
        }else if(time>10 && time<=15){
            streamingTime = "15 sec";
        }else if(time>15 && time<=20){
            streamingTime = "20 sec";
        }else if(time>20 && time<=30){
            streamingTime = "30 sec";
        }else if(time>30 && time<=40){
            streamingTime = "40 sec";
        }else if(time>40 && time<=60){
            streamingTime = "1 min";
        }else if(time>60 && time<=120){
            streamingTime = "2 min";
        }else if(time>120 && time<=180){
            streamingTime = "3 min";
        }else if(time>180 && time<=240){
            streamingTime = "4 min";
        }else if(time>240 && time<=300){
            streamingTime = "5 min";
        }else if(time > 300){
            streamingTime = "> 5 min";
        }
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_SCREEN_TIME+" : "+ streamingTime,AppEvents.VF_SCREEN_TIME);
        ZaiusEvent vfStreamTimeEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
        vfStreamTimeEvt.action(AppEvents.VF_SCREEN_TIME+" : "+ streamingTime);
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(vfStreamTimeEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }


        stopRecording();
        //stopPlaying();

        if(mIsControlFragmentVisible && mCurrentControlFragment!=null)
            removeFragment(mCurrentControlFragment);

        /*if (!canBackgroundMonitoring()) {
            BgMonitorData.getInstance()
                    .setRegistrationId("undefine")
                    .setShouldEnableBgAfterQuitView(false);
            sessionAttributes.userWantToCancel = true;
            deviceAttributes.activity_has_stopped = true;
            sessionAttributes.remote_reconnect_times = 0;
            stopBackgroundTasks();
        }*/
        dismissEnableProgressDialog();
        dismissFWUpgradeProgressDialog();

        if(mIsStreaming)
            backupVideoPlayer();

        if(mQueryTempTimer!=null)
            mQueryTempTimer.cancel();
        if(mDeviceWakeup!=null && selectedDevice.getProfile().isStandBySupported())
            mDeviceWakeup.cancelTask(selectedDevice.getProfile().registrationId,mDeviceHandler);
        if(mQueryDateTimeTimer!=null)
            mQueryDateTimeTimer.cancel();
        if(mSubscExpireLayout.getVisibility()== View.VISIBLE){
            mSubscExpireLayout.setVisibility(View.GONE);
            mSubscButton.setVisibility(View.VISIBLE);
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        //AA-1376
        Log.d(TAG, "onStop");

        shouldStop = true;
        if (panTiltThread != null && panTiltThread.isAlive()) {
            panTiltThread.interrupt();
        }

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        initialized = false;
        // unregister broadcast camera removing
        if (mActivity != null) {
            try {
                mActivity.unregisterReceiver(broadcaster);
                mActivity.unregisterReceiver(mScreenStateReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroyView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            mToolbar.setVisibility(View.GONE);
            mViewFinderInfoLayout.setVisibility(View.GONE);
            mControlGrid.setVisibility(View.GONE);
            mSeparatorView.setVisibility(View.GONE);
            mEventListLayout.setVisibility(View.GONE);
            mPlaybackFrame.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mStreamingLayout.setLayoutParams(layoutParams);
            resizeFFmpegView(1f,newConfig.orientation);
            mSummaryButton.setVisibility(View.GONE);
            Toast.makeText(mContext, getString(R.string.viewfinder_landscape_msg),Toast.LENGTH_SHORT).show();
            if(mSubscExpireLayout.getVisibility()== View.VISIBLE){
                mSubscExpireLayout.setVisibility(View.GONE);
            }
            mSubscButton.setVisibility(View.GONE);
         }else {
            mToolbar.setVisibility(View.VISIBLE);
            mViewFinderInfoLayout.setVisibility(View.VISIBLE);
            mSeparatorView.setVisibility(View.VISIBLE);
            mEventListLayout.setVisibility(View.VISIBLE);
            if (mIsStreaming) {
                mControlGrid.setVisibility(View.VISIBLE);
                if (mIsControlFragmentVisible) {
                    mPlaybackFrame.setVisibility(View.VISIBLE);
                    mDefaultControlsLayout.setVisibility(View.GONE);
                } else {
                    mPlaybackFrame.setVisibility(View.GONE);
                    mDefaultControlsLayout.setVisibility(View.VISIBLE);
                }
            }
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.viewfinder_player_height));
            layoutParams.addRule(RelativeLayout.BELOW, mViewFinderInfoLayout.getId());
            mStreamingLayout.setLayoutParams(layoutParams);
            resizeFFmpegView(1f, newConfig.orientation);
            if (CommonUtil.getDailySummaryFeatureAvailable(mActivity)) {
                mSummaryButton.setVisibility(View.VISIBLE);
            } else {
                mSummaryButton.setVisibility(View.GONE);
            }
            if(mIsSubscLayoutVisible){
                mSubscButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mIsFragmentHidden=true;
            clearKeepScreenOnFlags();
            if(mSubscExpireLayout.getVisibility()== View.VISIBLE){
                mSubscExpireLayout.setVisibility(View.GONE);
                mSubscButton.setVisibility(View.VISIBLE);
            }
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            mIsFragmentHidden=false;
            mEventCount.setVisibility(View.INVISIBLE);
            fetchEventForCamera();
            if (mActivity.getWindow() != null) {
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.i(TAG, "Gesture onDown event");
        lastDownPoint.set(e.getX(), e.getY());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.i(TAG, "Gesture onShowPress event");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.i(TAG, "Gesture onSingleTapUp event");
       /* if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE && !mIsControlFragmentVisible) {
            if (!mIsControlsVisible) {
                mControlLayout.setVisibility(View.VISIBLE);
                mControlGrid.setVisibility(View.VISIBLE);
                if(selectedDevice!=null && selectedDevice.getProfile().isStandBySupported()) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            getResources().getDimensionPixelSize(R.dimen.viewfinder_alert_height)
                    );
                    params.addRule(RelativeLayout.ABOVE, mControlGrid.getId());
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    mAlertLayout.setLayoutParams(params);
                }

            } else {
                mControlLayout.setVisibility(View.GONE);
                mControlGrid.setVisibility(View.GONE);
                if(selectedDevice!=null && selectedDevice.getProfile().isStandBySupported()) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            getResources().getDimensionPixelSize(R.dimen.viewfinder_alert_height)
                    );
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    mAlertLayout.setLayoutParams(params);
                }
            }
            mIsControlsVisible = !mIsControlsVisible;
        }*/
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (sessionAttributes != null && sessionAttributes.scale > 1.01f) {
            final CustomVerticalScrollView vScroll = (CustomVerticalScrollView) view.findViewById(R.id.vscroll);
            final CustomHorizontalScrollView hScroll = (CustomHorizontalScrollView) view.findViewById(R.id.hscroll);
            float curX = e2.getX();
            float curY = e2.getY();
            vScroll.smoothScrollBy((int) (sessionAttributes.mx - curX), (int) (sessionAttributes.my - curY));
            hScroll.smoothScrollBy((int) (sessionAttributes.mx - curX), (int) (sessionAttributes.my - curY));
            sessionAttributes.mx = curX;
            sessionAttributes.my = curY;
            // when video view is not zoom, so we do pan
        } else if (sessionAttributes != null && sessionAttributes.scale <= 1.01f) {
            float scrollX = e1.getX() - e2.getX();
            float scrollY = e1.getY() - e2.getY();
            needPanTilt = true;
            if (Math.abs(scrollX) > Math.abs(scrollY)) {
                // move left or move right
                if (scrollX > 0) {
                    currentDirection = DIRECTION_RIGHT;
                } else {
                    currentDirection = DIRECTION_LEFT;
                }
            } else {
                // move up or move down
                if (scrollY > 0) {
                    currentDirection = DIRECTION_DOWN;
                } else {
                    currentDirection = DIRECTION_UP;
                }
            }
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public void setupScaleDetector() {
        mScaleDetector = new ScaleGestureDetector(mActivity, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(@NotNull ScaleGestureDetector detector) {
            }

            @Override
            public boolean onScaleBegin(@NotNull ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public boolean onScale(@NotNull ScaleGestureDetector detector) {
                if (mActivity != null) {
                    if (getResources() != null && getResources().getConfiguration() != null) {
                        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN, AppEvents.VF_ZOOM_CLICKED,AppEvents.VF_ZOOM);
                        ZaiusEvent zoomEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                        zoomEvt.action(AppEvents.VF_ZOOM_CLICKED);
                        try {
                            ZaiusEventManager.getInstance().trackCustomEvent(zoomEvt);
                        } catch (ZaiusException e) {
                            e.printStackTrace();
                        }

                        float scaleFactor = detector.getScaleFactor();
                        sessionAttributes.scale *= scaleFactor;
                        sessionAttributes.scale = Math.max(1f, Math.min(sessionAttributes.scale, 6f));
                        resizeFFmpegView(sessionAttributes.scale,getResources().getConfiguration().orientation);

                        if (sessionAttributes.scale == 1f) {
                        }
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.i(TAG, "Gesture onSingleTapConfirmed event");
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i(TAG, "Gesture onDoubleTap event");
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.i(TAG, "Gesture onDoubleTapEvent event");
        if (sessionAttributes != null) {
            if (sessionAttributes.scale <= 1.01f) {
                sessionAttributes.scale = 3f;
            } else {
                sessionAttributes.scale = 1f;
            }
            resizeFFmpegView(sessionAttributes.scale,getResources().getConfiguration().orientation);
        }
        return true;
    }

    @Override
    public void setupOnTouchEvent() {
        final RelativeLayout mMovieLayout = (RelativeLayout) view.findViewById(R.id.live_streaming_layout);

        if (mMovieLayout != null) {
            mMovieLayout.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, @NotNull MotionEvent event) {
                    final CustomVerticalScrollView vScroll = (CustomVerticalScrollView) view.findViewById(R.id.vscroll);
                    final CustomHorizontalScrollView hScroll = (CustomHorizontalScrollView) view.findViewById(R.id.hscroll);
                    if (event.getPointerCount() > 1) {
                        mScaleDetector.onTouchEvent(event);
                    } else {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Log.i(TAG, "Touch Up, disable pantilt thread");
                            if (needPanTilt) {
                                currentDirection = DIRECTION_STOP;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(500);
                                            needPanTilt = false;
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }
                        }
                        mGestureDetector.onTouchEvent(event);
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (sessionAttributes != null && sessionAttributes.scale <= 1.01f) {
            boolean isSwipeToPanTilt = settings.getBoolean(AccountSettingFragment.SWIPE_TO_PANTILT, false);
            Log.i(TAG, "Gesture onFling event --> isSwipeToPanTilt: " + isSwipeToPanTilt);
            if (isSwipeToPanTilt) {
                float x = e1.getX() - e2.getX();
                float y = e1.getY() - e2.getY();
                if (Math.abs(x) > Math.abs(y)) {
                    // right or left
                    if (x > 70) {
                        doPanTilt(DIRECTION_RIGHT);
                    } else {
                        doPanTilt(DIRECTION_LEFT);
                    }
                } else {
                    // down or up
                    if (y > 70) {
                        doPanTilt(DIRECTION_DOWN);
                    } else {
                        doPanTilt(DIRECTION_UP);
                    }
                }
            }
        } else {
            Log.i(TAG, "onFling but video view is zoom, do nothing");
        }
        return true;
    }

    @Override
    public void updateDebugBitrate(int bitrate) {
        updateGlobalBitrate(bitrate);
    }

    public void setViewFinderCallback(IViewFinderCallback viewFinderCallback){
        mViewFinderCallback=viewFinderCallback;
    }

    private void resizeFFmpegView(float scaleFactor, int orientation) {
        if(orientation==Configuration.ORIENTATION_LANDSCAPE) {
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_MODE+" : "+AppEvents.LANDSCAPE,AppEvents.VF_MODE_LANDSCAPE);
            ZaiusEvent vfmodeEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
            vfmodeEvt.action(AppEvents.VF_MODE+" : "+AppEvents.LANDSCAPE);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(vfmodeEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }

        }else{
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_MODE+" : "+AppEvents.PORTRAIT,AppEvents.VF_MODE_PORTRAIT);
            ZaiusEvent vfmodeEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
            vfmodeEvt.action(AppEvents.VF_MODE+" : "+AppEvents.PORTRAIT);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(vfmodeEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }
        }
        if (sessionAttributes.ratio != 0) {
            if (Math.abs(scaleFactor - sessionAttributes.lastScaleFactor) >= 0.03 || scaleFactor == 1f) {
                sessionAttributes.lastScaleFactor = scaleFactor;
                FFMpegMovieViewAndroid mMovieView = (FFMpegMovieViewAndroid) view.findViewById(R.id.imageVideo);
                RelativeLayout liveView = (RelativeLayout)view. findViewById(R.id.streaming_layout);
                if (mMovieView != null) {
                    int new_width, new_height;
                    if (orientation==Configuration.ORIENTATION_LANDSCAPE) {
                        new_width = sessionAttributes.default_width;
                        new_height = sessionAttributes.default_height;
                    } else {
                        ViewGroup.LayoutParams live_params = liveView.getLayoutParams();
                        //commented out to resolve video frame cut issue
                        /*if (sessionAttributes.default_screen_height / sessionAttributes.ratio < live_params.height) {
                            // use full height of screen size
                            new_width = sessionAttributes.default_screen_height;
                            // new_width = live_params.width;
                            new_height = (int) Math.ceil(new_width / sessionAttributes.ratio);
                        } else {
                            new_height = live_params.height;
                            new_width = (int) Math.ceil(new_height * sessionAttributes.ratio);
                        }*/
                        new_width = sessionAttributes.default_screen_height;
                        new_height = (int) Math.ceil(new_width / sessionAttributes.ratio);
                    }

                    new_width = (int) Math.ceil(new_width * scaleFactor);
                    new_height = (int) Math.ceil(new_height * scaleFactor);

                    ViewGroup.LayoutParams movie_params = mMovieView.getLayoutParams();
                    movie_params.width = new_width;
                    movie_params.height = new_height;

                    mMovieView.setLayoutParams(movie_params);
                }
            }
        }
    }

    public void backupVideoPlayer() {
        Log.d(TAG, "backupVideoPlayer");
        isInBGMonitoring = false;
        //if (liveFragment != null) {
       // FFMpegMovieViewAndroid mMovieView = (FFMpegMovieViewAndroid) view.findViewById(R.id.imageVideo);
        if (mMovieView != null) {
            mPlayer = mMovieView.getFFMpegPlayer();
            if (mPlayer != null) {
                try {
                    mPlayer.setBackgroundModeEnabled(true, null);
                    isInBGMonitoring = true;
                } catch (IllegalStateException e) {
                    isInBGMonitoring = false;
                }
            }
        }
        //}
    }


    public void stopFFMPegPlayer() {
        //make sure that the backup player must be release before exit this fragment
        Log.d(TAG, "stopFFMPegPlayer");
        if (selectedDevice != null) {
            BgMonitorData.getInstance()
                    .setRegistrationId("undefine")
                    .setShouldEnableBgAfterQuitView(false);
        }
        stopStreamingBlocked();
        if (canBackgroundMonitoring()) {
            sessionAttributes.userWantToCancel = true;
            deviceAttributes.activity_has_stopped = true;
            sessionAttributes.remote_reconnect_times = 0;
            stopBackgroundTasks();
            if (mMovieView != null) {
                mMovieView.release();
            } else if (mPlayer != null) {
                mPlayer.suspend();
                try {
                    mPlayer.stop();
                    mPlayer.release();
                    mPlayer = null;
                } catch (Exception e) {
                } finally {
                    if (mPlayer != null) {
                        mPlayer.release();
                        mPlayer = null;
                    }
                }
            }
        }
    }


    @Override
    public boolean handleMessage(Message msg) {
//        Log.d(TAG, "handle message" + msg.what);
        if(isInBGMonitoring || !isAdded() || getActivity()==null)
            return false;
        if (msg != null) {
            switch (msg.what) {
                case FFMpegPlayer.MSG_MEDIA_INFO_P2P_SESSION_INDEX:
                    int sessionIdx = msg.arg1;
                    Log.d(TAG, "Close all media session exclude index " + sessionIdx);
                    //P2pManager.getInstance().cleanupAllSessionExcludeAtIndex(sessionIdx);
                    break;
                case CheckVersionFW.PATCH_AVAILABLE:
                    //showDeviceFirmwareUpgradeDialog(msg);
                    break;
                case CheckVersionFW.UPGRADE_DONE:
                    //showFirmwareUpgradeDoneDialog();
                    break;
                case CheckVersionFW.UPGRADE_FAILED:
                    // showFirmwareUpgradeFailedDialog(msg);
                    break;
                case FFMpegPlayer.MSG_MEDIA_STREAM_START_BUFFERING:
                    Log.d(TAG, "<-- handleMessage MSG_MEDIA_STREAM_START_BUFFERING");
                    startMediaBuffering();
                    break;
                case FFMpegPlayer.MSG_MEDIA_STREAM_STOP_BUFFERING:
                    Log.d(TAG, "<-- handleMessage MSG_MEDIA_STREAM_STOP_BUFFERING");
                    stopMediaBuffering();
                    break;
                case FFMpegPlayer.MSG_MEDIA_STREAM_RECORDING_TIME:
                    updateRecordingTime(msg.arg1);
                    break;
                case FFMpegPlayer.MSG_MEDIA_STREAM_LOADING_VIDEO:
                    Log.d(TAG, "<-- handleMessage MSG_MEDIA_STREAM_LOADING_VIDEO");
                    showSpinner(getString(R.string.EntryActivity_connecting_to_bm));
                    break;
                case FFMpegPlayer.MSG_MEDIA_STREAM_LOADING_VIDEO_CANCEL:
                    Log.d(TAG, "<-- handleMessage MSG_MEDIA_STREAM_LOADING_VIDEO_CANCEL");
                    hideSpinner(true);
                    break;
                case Streamer.MSG_VIDEO_FPS:
                    Log.d(TAG, "<-- handleMessage MSG_VIDEO_FPS :" + msg.arg1);
                    /*if (mIsFirstTime) {
                        //deviceAttributes.current_bitrate_value = PublicDefineGlob.MODIFIED_VIDEO_BITRATE;
                        //VideoBandwidthSupervisor.getInstance().setBitrate(PublicDefineGlob.MODIFIED_VIDEO_BITRATE);
                        mIsFirstTime = false;
                    }*/

                    // Should update FPS to video screen when debug is turned on
                    if (sessionAttributes.isDebugEnabled) {
                         updateFPS(msg.arg1);
                    }
                    break;
                case Streamer.MSG_VIDEO_SIZE_CHANGED:
                    Log.d(TAG, "<-- handleMessage MSG_VIDEO_SIZE_CHANGED");
                    onVideoSizeChanged(msg);
                    break;
                case Streamer.MSG_CAMERA_IS_NOT_AVAILABLE: {
                    int errorType = msg.arg1;
                    Log.d(TAG, "<-- handleMessage MSG_CAMERA_IS_NOT_AVAILABLE, errorType? " + errorType);
                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_STREAM+" : "+AppEvents.FAILED+" : " +errorType+" Camera Disconnected",AppEvents.CAMERA_DISCONNECTED);

                    ZaiusEvent vfstreamEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                    vfstreamEvt.action(AppEvents.VF_STREAM+" : "+AppEvents.FAILED+" : " +errorType+" Camera Disconnected");
                    try {
                        ZaiusEventManager.getInstance().trackCustomEvent(vfstreamEvt);
                    } catch (ZaiusException e) {
                        e.printStackTrace();
                    }
                   /* if (shouldSendP2pAnalyticsEvent()) {
                        sendOpenNonP2pFailedErrorType(isLocalStreaming(), errorType, -1);
                    }*/
                    // setLiveStreamEventConnectionType();
                    //commented to enable retry
                    /*hideSpinner(false);
                    //mErrorLayout.setVisibility(View.VISIBLE);*/
                    onUnexpectedStreamEnd();
                    // onCameraNotAvailable();
                    break;
                }
                case Streamer.MSG_VIDEO_STREAM_STATUS: {
                    int errorType = msg.arg1;
                    Log.d(TAG, "<-- handleMessage MSG_VIDEO_STREAM_STATUS, errorType? " + errorType);
                    /*if (shouldSendP2pAnalyticsEvent()) {
                        sendOpenNonP2pFailedErrorType(isLocalStreaming(), errorType, -1);
                    }*/
                    GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_STREAM+" : "+AppEvents.STATUS+" : "+errorType +AppEvents.CAMERA_DISCONNECTED,AppEvents.CAMERA_DISCONNECTED);

                    ZaiusEvent vfStreamEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                    vfStreamEvt.action(AppEvents.VF_STREAM+" : "+AppEvents.STATUS+" : "+errorType +AppEvents.CAMERA_DISCONNECTED);
                    try {
                        ZaiusEventManager.getInstance().trackCustomEvent(vfStreamEvt);
                    } catch (ZaiusException e) {
                        e.printStackTrace();
                    }

                    if (deviceAttributes.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_RTMP_REMOTE) {
                       /* mTvConnectionStatus.post(new Runnable() {
                            @Override
                            public void run() {
                                mTvConnectionStatus.setText(getSafeString(R.string.getting_stream_from_camera_retrying));
                                mTvConnectionStatus.setVisibility(View.VISIBLE);
                            }
                        });*/
                    }
                    break;
                }
                case Streamer.MSG_VIDEO_STREAM_HAS_STOPPED:
                    Log.d(TAG, "<-- handleMessage MSG_VIDEO_STREAM_HAS_STOPPED:");
                    onUnexpectedStreamEnd();

                    break;
                case FFMpegPlayer.MSG_MEDIA_STREAM_SEND_KEEP_ALIVE:
                    // send keep-alive relay session
                    Log.d(TAG, "Receive MSG_MEDIA_STREAM_SEND_KEEP_ALIVE");
                    sendKeepAliveRelaySession();
                    break;
                case Streamer.MSG_VIDEO_STREAM_HAS_STARTED:
                     Log.d(TAG, "<-- handleMessage MSG_VIDEO_STREAM_HAS_STARTED");

                    sessionAttributes.remote_reconnect_times = 0;
                    sessionAttributes.unexpectedLocalRetries = 0;
                    if (P2pSettingUtils.hasP2pFeature()) {
                        // Start streaming successfully, reset p2p try counter
                        sessionAttributes.p2pTries = 0;
                        // Clear reserved rtmp url if any
                        if (sessionAttributes.reservedRtmpUrl != null) {
                            sessionAttributes.reservedRtmpUrl = null;
                        }

                        if (sessionAttributes.viewP2p == true) {
//                if (isMobileDataConnected() == true) {
//                  // Try p2p successfully, clear last try timestamp
//                  P2pManager.getInstance().updateLastP2pTry(true);
//                }
                            if (selectedDevice.getProfile().shouldUseP2PTalkbackInLocal()) {
                                    P2pCommunicationManager.getInstance().updateP2pCommHandler(getFFMpegMovieViewAndroid().getFFMpegPlayer());
                            } else {
                                if (isLocalStreaming()) {
                                    P2pCommunicationManager.getInstance().updateP2pCommHandler(null);
                                } else {
                                    // Update p2p communication handler for sending command
                                    if (getFFMpegMovieViewAndroid() != null) {

                                        //TODO check method argument casting again
                                        P2pCommunicationManager.getInstance().updateP2pCommHandler(
                                            getFFMpegMovieViewAndroid().getFFMpegPlayer());
                                    } else {
                                        // Invalid p2p communication handler, clear it
                                        P2pCommunicationManager.getInstance().updateP2pCommHandler(null);
                                    }
                                }
                            }

                        } else {
                            // Clear p2p communication handler
                            P2pCommunicationManager.getInstance().updateP2pCommHandler(null);
                        }
                    }
                    /*if (shouldSendP2pAnalyticsEvent()) {
                        sendOpenSessionSuccessEvent(isP2pStreaming(), isLocalStreaming());
                    }*/

                    //AA-1412: Implement Camera View Summary analytics
                    setLiveStreamEventConnectionType(isP2pStreaming(), isLocalStreaming());
                    onVideoStreamStarted();
                    break;
                case Streamer.MSG_RTSP_VIDEO_STREAM_BITRATE_BPS:

                    if (sessionAttributes.isDebugEnabled)
                        updateDebugBitrateDisplay(msg.arg1);
                    break;
                case FFMpegPlayer.MSG_MEDIA_INFO_P2P_HANDSHAKE_FAILED:
                    Log.d(TAG, "<-- handleMessage MSG_MEDIA_INFO_P2P_HANDSHAKE_FAILED");
                   /* if (shouldSendP2pAnalyticsEvent()) {
                        sendOpenSessionFailedEvent();
                    }*/
                    setLiveStreamEventConnectionType();
                    if (P2pSettingUtils.hasP2pFeature()) {
                        P2pCommunicationManager.getInstance().updateP2pCommHandler(null);
                    }
                    onUnexpectedStreamEnd();
                    break;
                case Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY:
                    int keyFrameTotal = msg.arg1;
                    Log.d(TAG, "<-- handleMessage MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY, keyFrameTotal? " + keyFrameTotal);
                    // Crash due to fragment has stopped.
                    // Only send Localytics event if VideoViewFragment doesn't stop.
                    if (sessionAttributes.userWantToCancel == false && deviceAttributes.activity_has_stopped == false) {
                        // Need to check whether this fragment is added to Activity or not, or getString() method would crash.
                       /* if (isAdded()) {
                            GoogleAnalyticsController.getInstance().trackCameraInfo(getSafeString(R.string.accessing_stream_event_unexcpected_end),
                                    selectedDevice, isLocalStreaming());
                        }*/
                    }

                    if (P2pSettingUtils.hasP2pFeature()) {
                        P2pCommunicationManager.getInstance().updateP2pCommHandler(null);
                        // send open session failed event for p2p session
                        // App will reset p2pTries counter once stream has started successfully.
                        // So p2pTries > 0 means P2P has really failed.
                        if (isP2pStreaming()) {
                            if (sessionAttributes.p2pTries > 0) {
                                //sendOpenSessionFailedEvent();
                                setLiveStreamEventConnectionType(isP2pStreaming(), isLocalStreaming());
                            } else if (keyFrameTotal < P2pManager.P2P_KEY_FRAME_MIN) {
                                // If key frame total < P2P_KEY_FRAME_MIN -> send open session failed event for p2p session
                                //sendOpenP2pSessionFailedWithError(GoogleAnalyticsController.GA_ACTION_P2P_FAILED_TOO_FEW_KEY_FRAMES);
                                setLiveStreamEventConnectionType(isP2pStreaming(), isLocalStreaming());
                            }
                        } else if (deviceAttributes.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_RTMP_REMOTE &&
                                sessionAttributes.remote_reconnect_times > 0) {
                            //sendOpenNonP2pFailedErrorType(false, Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY, -1);

                            setLiveStreamEventConnectionType(false, false);
                            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_STREAM+" : "+AppEvents.INTERRUPTED+" : "+getErrorMsg(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY, -1),AppEvents.STREAMING_FAILED);

                            ZaiusEvent vfStreamEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                            vfStreamEvt.action(AppEvents.VF_STREAM+" : "+AppEvents.INTERRUPTED+" : "+getErrorMsg(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY, -1));
                            try {
                                ZaiusEventManager.getInstance().trackCustomEvent(vfStreamEvt);
                            } catch (ZaiusException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    if (isP2pStreaming()) {
                        onStreamEndWithKeyFrameTotal(keyFrameTotal);
                    } else {
                        onUnexpectedStreamEnd();
                    }
                    break;
                case RemoteStreamTask.MSG_VIEW_CAM_SUCCESS:
                    Log.d(TAG, "<-- handleMessage MSG_VIEW_CAM_SUCCESS");
                    onViewCameraSuccess(msg);
                    break;
                case RemoteStreamTask.MSG_VIEW_CAM_FALIED:
                    int statusCode = msg.arg1;
                    int deviceResponseCode = msg.arg2;
                    Log.d(TAG, "<-- handleMessage MSG_VIEW_CAM_FALIED, errorCode? " + statusCode + ", device res code? " + deviceResponseCode);
                    /*
                     * Check whether current connection mode is p2p to avoid combine mode.
                     * In combine mode, app just reserve rtmp url, don't start streaming immediately.
                     */
                    if (sessionAttributes.viewP2p == false) {
                        /*if (shouldSendP2pAnalyticsEvent()) {
                            if (deviceResponseCode != -1 && deviceResponseCode != 200) {
                                sendOpenNonP2pFailedErrorType(isLocalStreaming(), RtmpStreamTask.MSG_VIEW_CAM_FALIED, deviceResponseCode);
                            } else {
                                sendOpenNonP2pFailedErrorType(isLocalStreaming(), RtmpStreamTask.MSG_VIEW_CAM_FALIED, statusCode);
                            }
                            // sendOpenSessionFailedEvent();
                        }*/

                        setLiveStreamEventConnectionType(false, isLocalStreaming());
                    }
                    onViewCameraFailed(msg);
                    break;
                default:
                    break;
            }
        }


        return false;

    }


    @Override
    public void onP2pSessionOpenSucceeded(P2pClient p2pClient) {
        if (p2pClient != null) {
            Log.i(TAG, "onP2pSessionOpenSucceeded, regId: " + p2pClient.getRegistrationId());
            if (!sessionAttributes.userWantToCancel && !deviceAttributes.activity_has_stopped) {
                sessionAttributes.p2pClients = new P2pClient[]{p2pClient};
                sessionAttributes.filePath = "";

                // Remove obsolete codes, don't need to check NAT type for mobile network anymore.
                setupFFMpegPlayer();

            } else {
                Log.d(TAG, "onP2pSessionOpenSucceeded, fragment has stopped -> do nothing");
            }
        } else {
            Log.d(TAG, "onP2pSessionOpenSucceeded, p2pClients is empty -> do nothing");
        }

    }

    @Override
    public void onP2pSessionOpenFailed() {
        Log.d(TAG, "onP2pSessionOpenFailed");
        if (!sessionAttributes.userWantToCancel && !deviceAttributes.activity_has_stopped) {
            Log.d(TAG, "Create p2p session failed, isRtmpStreamingEnabled? " + P2pManager.getInstance().isRtmpStreamingEnabled() +
                    ", isRtspStreamingEnabled? " + P2pSettingUtils.getInstance().isRtspStreamingEnabled());
            //if (isMobileDataConnected() == true && sessionAttributes.mobileIspNatType == P2pManager.P2P_NAT_TYPE_UNKNOWN) {
            //   P2pManager.getInstance().updateLastP2pTry(false);
            //}

            boolean isInLocal = isLocalStreaming();
            if (isInLocal) {
                if (P2pSettingUtils.getInstance().isRtspStreamingEnabled()) {
                    Log.d(TAG, "RTSP streaming is enabled -> switch to RTSP");
                    prepareToViewCameraViaRtsp();
                } else {
                    Log.d(TAG, "RTSP streaming is disabled -> continue to try P2P");
                    prepareToViewCameraViaP2p(isInLocal);
                }
            } else {
                if (P2pSettingUtils.getInstance().isRtmpStreamingEnabled()) {
                    prepareToViewCameraViaRelay();
                } else {
                    Log.d(TAG, "RTMP streaming is disabled -> continue to try P2P");
                    prepareToViewCameraViaP2p(isInLocal);
                }
            }
        } else {
            Log.d(TAG, "Create p2p session failed, fragment has stopped -> do nothing");
        }

    }

    @Override
    public void onP2pSessionClosed(boolean isSucceeded) {

    }

    private void onVideoSizeChanged(Message msg) {
        deviceAttributes.video_width = msg.arg1;
        deviceAttributes.video_height = msg.arg2;
        if (deviceAttributes.video_width != 0 && deviceAttributes.video_height != 0) {
            sessionAttributes.ratio = (float) deviceAttributes.video_width / deviceAttributes.video_height;
        }
        recalcDefaultScreenSize();
        if(mActivity!=null && isAdded()) {
            mActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    resizeFFmpegView(1f,mActivity.getResources().getConfiguration().orientation);

                    String res_str = String.format("%dx%d", deviceAttributes.video_width, deviceAttributes.video_height);
                    mResolutionText.setText(res_str);

                    String fps_str;
                    if (P2pSettingUtils.hasP2pFeature()) {
                        fps_str = String.format("%s %d", getCurrentConnectionModeLetter(), 0);
                    } else {
                        fps_str = String.format("%s %d", isInLocalString(), 0);
                    }
                    mFrameRateText.setText(fps_str);

                    //this should be executed, only when streaming has started and not when video resolution changes after user input
                    if(!mIsHDChanged) {
                        //based on video resolution, set HD state to enabled/disabled
                        if (deviceAttributes.video_height >= 1080) {
                            mHDImage.setImageResource(HD.pressedImage);
                            mHDText.setText(mActivity.getString(R.string.viewfinder_hd_on));
                            HD.pressed = true;
                        } else {
                            //if resolution is low on stream start and user has previously selected HD, set resolution to HD
                            if (settings.getBoolean("hd" + selectedDevice.getProfile().getRegistrationId(), false)) {
                                mHDImage.setImageResource(HD.pressedImage);
                                mHDText.setText(mActivity.getString(R.string.viewfinder_hd_on));
                                HD.pressed = true;
                                mLiveActionListener.onHD(HD.pressed);
                            } else {
                                mHDImage.setImageResource(HD.image);
                                mHDText.setText(mActivity.getString(R.string.viewfinder_hd_off));
                                HD.pressed = false;
                            }
                        }
                    }
                }
            });
        }
    }

    public boolean canBackgroundMonitoring() {
        if(mActivity!=null)
            return CommonUtil.getSettingInfo(mActivity.getApplicationContext(), SettingsPrefUtils.PREFS_BACKGROUND_MONITORING,false);
        else
            return false;
    }

    private void stopBackgroundTasks() {
        Log.d(TAG, "stopBackgroundTasks");
        if (videoPlaybackTasks != null) {
            videoPlaybackTasks.stopAllTimers();
            videoPlaybackTasks.stopCountDownTimer();
            videoPlaybackTasks.stopRunningWifiScanTask();
            videoPlaybackTasks.stopLiveStreamingTasks();
            videoPlaybackTasks.stopStandByVideoTimer();
            if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled()) {
                P2pCommunicationManager.getInstance().updateP2pCommHandler(null);

                // Switch camera to p2p passive mode if needed
                if (isP2pStreaming() == true) {
                    if (selectedDevice != null && selectedDevice.getProfile() != null && isOrbitP2PEnabled() &&
                            selectedDevice.getProfile().canUseP2p() && selectedDevice.getProfile().canUseP2pRelay()) {
                        P2pManager.getInstance().destroyP2pSession();
                    } else {
                        /*
                         * 20160107: HOANG:
                         * Old p2p cameras: Close & destroy current p2p session.
                         */
                        P2pManager.getInstance().destroyP2pSession();
                    }
                }
            }
        }
    }


    private void onViewCameraSuccess(Message msg) {
        Log.d(TAG, "onViewCameraSuccess");
        mIsRTMPLinkReceived=true;
        if (!sessionAttributes.userWantToCancel && !deviceAttributes.activity_has_stopped) {
            BabyMonitorAuthentication bm_auth = (BabyMonitorAuthentication) msg.obj;
            if (sessionAttributes.viewP2p == true) {
                sessionAttributes.reservedRtmpUrl = bm_auth.getStreamUrl();
                Log.d(TAG, "Save reserved rtmp url: " + sessionAttributes.reservedRtmpUrl);
            } else {
                if (videoPlaybackTasks.setStreamingState()) {
                    /*mTvConnectionStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvConnectionStatus.setText(getSafeString(R.string.getting_stream_from_camera));
                            mTvConnectionStatus.setVisibility(View.VISIBLE);
                        }
                    });*/

                    InetAddress remote_addr;
                    try {
                        remote_addr = InetAddress.getByName(bm_auth.getIP());
                        String stream_url = bm_auth.getStreamUrl();
                        remoteIP = stream_url;
                        videoPlaybackTasks.setStreamUrl(stream_url);
                        GoogleAnalyticsController.getInstance().sendCreateSessionEvent(mActivity, true);
                        long create_session_time = System.currentTimeMillis() - sessionAttributes.create_session_start_time;
                        sessionAttributes.open_stream_start_time = System.currentTimeMillis();
                        setupRemoteCamera(bm_auth);
                    } catch (UnknownHostException e) {
                        startMainActivity();
                    }
                }
            }
        }
    }

    private void onVideoStreamStarted() {
        Log.d(TAG, "onVideoStreamStarted");
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_STREAM_SUCCESSFULL,AppEvents.STREAM_SUCCESS);
        vfSceenStartTime = System.currentTimeMillis();
        ZaiusEvent vfStreamEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
        vfStreamEvt.action(AppEvents.VF_STREAM_SUCCESSFULL);
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(vfStreamEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }
        timeTakenToStream = System.currentTimeMillis() - cameraStreamingStartTime;
        int time =(int) timeTakenToStream / 1000;
        String timeToStream = null;
        Log.d("LoginTime","LoginTime : "+timeTakenToStream + " Sec = "+time);
        if(time<=1){
            timeToStream = "1 sec";
        }else if(time>1 && time<=3){
            timeToStream = "3 sec";
        }else if(time>3 && time<=5){
            timeToStream = "5 sec";
        }else if(time>5 && time<=10){
            timeToStream = "10 sec";
        }else if(time>10 && time<=15){
            timeToStream = "15 sec";
        }else if(time>15 && time<=20){
            timeToStream = "20 sec";
        }else if(time>20 && time<=30){
            timeToStream = "30 sec";
        }else if(time>30 && time<=40){
            timeToStream = "40 sec";
        }else if(time>40 && time<=60){
            timeToStream = "1 min";
        }else if(time>60 && time<=120){
            timeToStream = "2 min";
        }else if(time>120 && time<=180){
            timeToStream = "3 min";
        }else if(time>180 && time<=240){
            timeToStream = "4 min";
        }else if(time>240 && time<=300){
            timeToStream = "5 min";
        }else if(time > 300){
            timeToStream = "> 5 min";
        }

        String connetionMode = getCurrentConnectionModeLetter();
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_TIME_TAKEN_TO_STREAM+" : "+ connetionMode +"-"+timeToStream,AppEvents.VF_TIME_TAKEN_TO_STREAM);
        ZaiusEvent vfSteamTimeEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
        vfSteamTimeEvt.action(AppEvents.VF_TIME_TAKEN_TO_STREAM+" : "+ connetionMode +"-"+ timeToStream);
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(vfSteamTimeEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }


        if (deviceAttributes.activity_has_stopped == false && sessionAttributes.userWantToCancel == false) {
            sessionAttributes.view_session_start_time = System.currentTimeMillis();
            setViewSessionStartTime(sessionAttributes.view_session_start_time);

            setupBottomMenu();
            mIsStreaming=true;
          /* 20151118: hoang: AA-919
           * Update latest snapshot for camera every time view session started.
           * REMEMBER: don't get snapshot too soon. Because the first key frame is not perfect.
           */
            mLatestSnap.setVisibility(View.GONE);
            long previousSnapshotTime=settings.getLong("snapshot"+selectedDevice.getProfile().getRegistrationId(),0);
            //video snapshot is saved only after 10 mins or if previous snapshot is not available
            if(System.currentTimeMillis()-previousSnapshotTime > SNAPSHOT_FREQUENCY || !Util.isLatestSnapshotAvailable(selectedDevice.getProfile().getRegistrationId())) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mActivity != null && isAdded()) {
                            doUpdateLatestSnapTask();
                        }
                    }
                }, 1500);
            }

            // TODO Call isAvailableLocally() on UI thread could cause hang. Please fix this.
            //if (selectedDevice.isAvailableLocally()) {
            /*if (PublicDefine.shouldCheckFwUpgrade(PublicDefine.getModelIdFromRegId(selectedDevice.getProfile().getRegistrationId()),
                    selectedDevice.getProfile().getFirmwareVersion()))
            {
                // This is used to check for other model like focus 66 etc
                if(!(selectedDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)==0))
                {

                    if (isLocalStreaming())
                    {
                        //as this is not orbit device do not wait for battery status
                        isAllowFirmwareUpgrade = true;
                        doCheckFwUpgradeTask();
                    }
                }

            }*/

            if (mContext != null && selectedDevice != null && !(selectedDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0)) {
                if (CommonUtil.getSettingInfo(mContext, selectedDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, false)) {
                    mOtaAvailableIv.setVisibility(View.VISIBLE);
                    Log.i(TAG,"Ota dialog for "+selectedDevice.getProfile().getRegistrationId() + "-" + selectedDevice.getProfile().getFirmwareVersion()+" is shown "+CommonUtil.getSettingInfo(mContext, selectedDevice.getProfile().getRegistrationId() + "-" + selectedDevice.getProfile().getFirmwareVersion(), false));
                    if (!CommonUtil.getSettingInfo(mContext, selectedDevice.getProfile().getRegistrationId() + "-" + selectedDevice.getProfile().getFirmwareVersion(), false) && isLocalStreaming()) {
                        CommonUtil.setSettingInfo(mContext, selectedDevice.getProfile().getRegistrationId() + "-" + selectedDevice.getProfile().getFirmwareVersion(), true);
                        showOTAUpdateDialog(false);
                    }
                }else {
                    mOtaAvailableIv.setVisibility(View.GONE);
                }
            }

            if (!isLocalStreaming())
            {
                videoPlaybackTasks.initRemoteVideoTimer();
                if (shouldVideoTimeout()) {
                    videoPlaybackTasks.scheduleRemoteVideoTimerTask(new VideoTimeoutTask(), VIDEO_TIMEOUT);
                }
            }

            if(selectedDevice.getProfile().isStandBySupported())
                getRemainingTime(true);

            if(sessionAttributes.isDebugEnabled)
                mDebugLayout.setVisibility(View.VISIBLE);
            else
                mDebugLayout.setVisibility(View.GONE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getView() != null && mActivity!=null && isAdded() && sessionAttributes.isDebugEnabled) {
                        doGetWifiStrengthTask();
                        if(selectedDevice.getProfile().isStandBySupported())
                        {
                            getBatteryMode();
                        }
                    } else {
                        Log.i(TAG, "getView null or debug disabled, don't need to get wifi strength");
                    }
                }
            }, 2000);
            // Update global video bitrate for all connection mode
            HubbleSessionManager.getInstance().adjustToDefaultVideoBitrate();
            updateGlobalBitrate(HubbleSessionManager.getInstance().getCurrentVideoBitrate()); // first time start stream

            recalcDefaultScreenSize();
            if(mActivity!=null && isAdded()) {
                mActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        resizeFFmpegView(1f,getResources().getConfiguration().orientation);
                        hideSpinner(true);
                    }
                });
            }

            boolean keepPlayer = canBackgroundMonitoring();
            Log.d(TAG, "Keep player after exit live screen? " + keepPlayer);
            BgMonitorData.getInstance()
                    .setRegistrationId(selectedDevice.getProfile().getRegistrationId())
                    .setShouldEnableBgAfterQuitView(true);
        }
        mIsStreaming=true;
    }

    private void recalcDefaultScreenSize() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        if (mActivity!=null && mActivity.getWindowManager() != null) {
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        }

        if (displaymetrics.widthPixels > displaymetrics.heightPixels) {
            sessionAttributes.default_screen_height = displaymetrics.heightPixels;
            sessionAttributes.default_screen_width = displaymetrics.widthPixels;
        } else {
            sessionAttributes.default_screen_height = displaymetrics.widthPixels;
            sessionAttributes.default_screen_width = displaymetrics.heightPixels;
        }

        if (sessionAttributes.ratio != 0) {
            if (sessionAttributes.default_screen_height * sessionAttributes.ratio > sessionAttributes.default_screen_width) {
                sessionAttributes.default_width = sessionAttributes.default_screen_width;
                sessionAttributes.default_height = (int) (sessionAttributes.default_width / sessionAttributes.ratio);
            } else {
                sessionAttributes.default_height = sessionAttributes.default_screen_height;
                sessionAttributes.default_width = (int) (sessionAttributes.default_height * sessionAttributes.ratio);
            }
        }
    }


    private boolean shouldVideoTimeout() {
        //return settings.getBoolean(SHOULD_VIDEO_TIMEOUT, true);
        return CommonUtil.getSettingInfo(mContext,SettingsPrefUtils.PREFS_SHOULD_VIDEO_TIMEOUT,true);

    }

    private String getSavedToken() {
        return settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
    }

    private void onViewCameraFailedStandBy(boolean enteringStandByMode,int remainingTime)
    {
        if(BuildConfig.DEBUG)
            Log.d(TAG, "onViewCameraFailedStandBy :- " + remainingTime  + " and mode :-"+enteringStandByMode);

        hideSpinner(false);
        mErrorLayout.setVisibility(View.VISIBLE);
        stopLiveFragmentStreaming();

        if(enteringStandByMode)
        {
            int totalTime = (remainingTime + PublicDefine.DEFAULT_WAIT_FOR_STAND_BY_MODE) * 1000;

            mError1Tv.setText(getResources().getString(R.string.device_entering_powersaving));
            mError2Tv.setVisibility(View.GONE);
            mError3Tv.setVisibility(View.GONE);

            Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.image_rotate);
            mErrorImageView.setImageDrawable(getResources().getDrawable(R.drawable.settingup));
            mErrorImageView.setVisibility(View.VISIBLE);;
            mErrorImageView.startAnimation(animation);

            mRetryButton.setText(getResources().getString(R.string.wake_up));
            mRetryButton.setTextColor(getResources().getColor(R.color.white_transperent));
            mRetryButton.setEnabled(false);
            mRetryButton.setBackground(getResources().getDrawable(R.drawable.viewfinder_disable_button));

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if (mActivity != null && isAdded())
                    {
                        mErrorImageView.clearAnimation();
                        mErrorImageView.setVisibility(View.GONE);

                        mError1Tv.setText(mActivity.getResources().getString(R.string.device_sleep_mode));
                        mError2Tv.setVisibility(View.VISIBLE);
                        mError2Tv.setText(mActivity.getResources().getString(R.string.session_timeout));
                        mError3Tv.setVisibility(View.GONE);

                        mRetryButton.setEnabled(true);
                        mRetryButton.setBackground(getResources().getDrawable(R.drawable.viewfinder_button));
                        mRetryButton.setTextColor(getResources().getColor(R.color.white));
                    }
                }
            }, totalTime);
        }
        else
        {
            mError1Tv.setText(getResources().getString(R.string.device_sleep_mode));
            mError2Tv.setVisibility(View.VISIBLE);
            mError2Tv.setText(getResources().getString(R.string.session_timeout));
            mError3Tv.setVisibility(View.GONE);

            mErrorImageView.setVisibility(View.GONE);
            mRetryButton.setText(getResources().getString(R.string.wake_up));
            mRetryButton.setEnabled(true);
            mRetryButton.setBackground(getResources().getDrawable(R.drawable.viewfinder_button));
            mRetryButton.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private int onViewCameraFailed(Message msg)
    {
        Log.d(TAG, "onViewCameraFailed");
        int status = msg.arg1;
        int code = msg.arg2;
        mIsRTMPLinkReceived=false;
        if (sessionAttributes.viewP2p == false)
        {
            sessionAttributes.viewRelayRtmp = false;
            // cancelVideoStoppedReminder();

            if (sessionAttributes.userWantToCancel == false && deviceAttributes.activity_has_stopped == false)
            {
                GoogleAnalyticsController.getInstance().sendCreateSessionEvent(getActivity(), false);

                if (!selectedDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR))
                {
                    if (mActivity != null && !mIsFragmentHidden) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.camera_is_not_accessible), Toast.LENGTH_SHORT).show();
                    }
                }


                // as device is not online so we should not display message that "check internet connectivity"
                if(status == HttpURLConnection.HTTP_PRECON_FAILED)
                {
                    mError3Tv.setVisibility(View.GONE);
                }

                mStreamRetryCount++;
                if(mStreamRetryCount > MAX_STREAM_RETRY) {
                    hideSpinner(false);
                    mErrorLayout.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Stream retry count:"+mStreamRetryCount);
                }else {
                    // Wait a bit to avoid create_session too many times
                    Log.d(TAG, "stream retry count :"+mStreamRetryCount+" retrying..");
                    AsyncPackage.doInBackground(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                            }

                            if (sessionAttributes.userWantToCancel == false && deviceAttributes.activity_has_stopped == false && mActivity != null && isAdded()) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        prepareToViewCameraViaRelay();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
        return status;
    }


    private String getErrorMsg(int errorType, int errorCode) {
        Log.d(TAG, "getErrorMsg");
        String errorMsg = null;
        if (errorType == FFMpegMovieViewAndroid.VIDEO_STREAM_STREAM_NOT_FOUND) {
            errorMsg = "Stream ID not found";
        } else if (errorType == RtmpStreamTask.MSG_VIEW_CAM_FALIED) {
            if (errorCode == -1) {
                errorMsg = "Create Session Failure (STUN command) - others";
            } else {
                errorMsg = "Create Session Failure (STUN command) - " + errorCode;
            }
        } else if (errorType == FFMpegMovieViewAndroid.VIDEO_STREAM_CONNECTION_TIMEOUT) {
            errorMsg = "Streaming timeout";
        } else if (errorType == Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY) {
            errorMsg = "Streaming timeout";
        }
        return errorMsg;
    }

    private void sendKeepAliveRelaySession() {
        Log.d(TAG, "inside sendKeepAliveRelaySession method");
        if (selectedDevice != null) {
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        String saved_token = getSavedToken();
                        String clientType = "browser";
                        base.hubble.meapi.Device.getSessionKey2(saved_token, selectedDevice.getProfile().getRegistrationId(), clientType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void setLiveStreamEventConnectionType(boolean isP2pStreaming, boolean isInLocal) {
        Log.d(TAG, "setLiveStreamEventConnectionType");
    }

    private void onStreamEndWithKeyFrameTotal(final int keyFrameTotal) {
        Log.d(TAG, "onStreamEndWithKeyFrameTotal");
        if (keyFrameTotal > P2pManager.P2P_KEY_FRAME_MIN) {
            onUnexpectedStreamEnd();
        } else {
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {

                    if (!sessionAttributes.userWantToCancel && !deviceAttributes.activity_has_stopped && mActivity!=null && isAdded()) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //// TODO
                                 loadLatestCameraSnap();
                                 if(sessionAttributes.isDebugEnabled)
                                    updateDebugBitrateDisplay(0); // reset bitrate info to 0 kbps
                                 showSpinner(mActivity.getString(R.string.EntryActivity_connecting_to_bm));
                            }
                        });

                        if (!deviceAttributes.is_upgrading) {
                            videoPlaybackTasks.stopAllTimers();

                            /* 20151201: HOANG: FWPR-529
                             * If stream has stopped, stop recording if it's in progress.
                             */
                            //todo for recording
                            //stopRecording();
                           /* getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                   *//* if (recordingFragment != null && recordingFragment.isRecording()) {
                                        recordingFragment.stopRecording();
                                        recordingFragment.refreshRecordingUI();
                                    }*//*
                                }
                            });*/

                            Log.d(TAG, "onUnexpectedStreamEnd, checking isCameraInSameNetwork");
                            boolean isInSameNetwork = isCameraInSameNetwork();
                            Log.d(TAG, "onUnexpectedStreamEnd, isCameraInSameNetwork? " + isInSameNetwork);
                            if (isInSameNetwork) {
                                resolveLocalStream();
                            } else {
                                // Copy from resolveRemoteStream() method
                                disconnectTalkbackIfAvailable();
                                stopStreamingBlocked();

                                if (sessionAttributes.userWantToCancel || deviceAttributes.activity_has_stopped) {
                                    return;
                                }

                                sessionAttributes.remote_reconnect_times++;
                                if(mActivity!=null && isAdded()) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // switchToLiveFragment();

                                            if (HubbleApplication.isVtechApp()) {
                                                prepareToViewCameraRemotely();
                                            } else {
                                                Log.d(TAG, "Try rtmp because key frame total: " + keyFrameTotal);
                                                prepareToViewCameraViaRelay();
                                            }
                                        }
                                    });
                                }

                            }
                        }

                    }
                }
            });
        }
    }


    private void setLiveStreamEventConnectionType() {
        Log.d(TAG, "setLiveStreamEventConnectionType");
        if (deviceAttributes.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_RTMP_REMOTE) {
            // We have break down rtmp session, so don't send here anymore.
            // AnalyticsController.getInstance().sendOpenNonP2pStreamFailedEvent(false);
        } else if (deviceAttributes.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_RTSP_LOCAL) {
            // AnalyticsController.getInstance().sendOpenNonP2pStreamFailedEvent(true);
        } else if (deviceAttributes.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_P2P_REMOTE) {
          /*
           * P2P is considered failed if all p2p tries have failed.
           */
            boolean hasP2pFailed = hasP2pReallyFailed();
            if (hasP2pFailed == true) {
                setLiveStreamEventConnectionType(isP2pStreaming(), false);
            } else {
                Log.d(TAG, "Need to retry p2p more, dont send failed event now");
            }
        } else if (deviceAttributes.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_P2P_LOCAL) {
            setLiveStreamEventConnectionType(isP2pStreaming(), true);
        }
    }

    private boolean hasP2pReallyFailed() {
        Log.d(TAG, "hasP2pReallyFailed");
        boolean hasP2pFailed = true;
        boolean usingMobileNetwork = isMobileDataConnected();
        if (usingMobileNetwork == true) {
            hasP2pFailed = true;
        } else {
            if (sessionAttributes.p2pTries < P2pManager.HUBBLE_P2P_MAX_TRY) {
                hasP2pFailed = false;
            }
        }
        return hasP2pFailed;
    }


    private void stopMediaBuffering() {
        Log.d(TAG, "hasP2pReallyFailed");
        videoPlaybackTasks.stopBufferingTimer();
    }

    private void startMediaBuffering() {
        Log.d(TAG, "startMediaBuffering");
        videoPlaybackTasks.initBufferingTimer();
        TimerTask timeOutTask = new TimerTask() {
            @Override
            public void run() {
                if (!sessionAttributes.userWantToCancel) {
                    if (!deviceAttributes.activity_has_stopped) {
                        // TODO: remove runnable/toast here - it's for debug only
                        if (mActivity != null && isAdded()) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity, "Video timeout task completed...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        cancelVideoTimeoutTask();

                        if (isLocalStreaming()) {
                            resolveLocalStream();
                        } else {
                          /* 20151201: HOANG: call selectedDevice.isAvailableRemotely could cause hang UI.
                           * Solution: if not in local, just proceed remotely reconnecting.
                           */
                            remoteVideoHasStopped(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY);
                        }
                    }
                }
            }
        };
        videoPlaybackTasks.scheduleBufferTimerTask(timeOutTask, MAX_BUFFERING_TIME);
    }

    private void cancelVideoTimeoutTask() {
        videoPlaybackTasks.stopRemoteVideoTimer();
    }

    private void   remoteVideoHasStopped(final int reason) {
        Log.d(TAG, "remoteVideoHasStopped");
        AsyncPackage.doInBackground(new Runnable() {
            @Override
            public void run() {
                // Once disconnected from camera, should check and disconnect talkback also.
                disconnectTalkbackIfAvailable();
                stopStreamingBlocked();

                if (sessionAttributes.userWantToCancel || deviceAttributes.activity_has_stopped) {
                    return;
                }

                if (sessionAttributes.remote_reconnect_times > 0) {
                    HubbleSessionManager.getInstance().decreaseDefaultVideoBitrate();
                }
                sessionAttributes.remote_reconnect_times++;

                switch (reason) {
                    case Streaming.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY:
                        Log.i(TAG, " getActivity null? " + (getActivity() == null));
                        if(mActivity!=null && isAdded()) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //switchToLiveFragment();
                                    prepareToViewCameraRemotely();
                                }
                            });
                        }

                        break;
                }
            }
        });
    }

    private void resolveLocalStream() {
        Log.d(TAG, "Resolve local stream");
        AsyncPackage.doInBackground(new Runnable() {
            @Override
            public void run() {

                // Once disconnected from camera, should check and disconnect talkback also.
                disconnectTalkbackIfAvailable();
                // Stop players
                stopStreamingBlocked();
                if(mActivity!=null && isAdded()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mActivity != null) {

                                // Decide whether Router disconnects or Camera disconnect
                                if (sessionAttributes.userWantToCancel || deviceAttributes.activity_has_stopped) {
                                    return;
                                }

                                WifiManager wm = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
                                String ssid_no_quote = settings.getString(sessionAttributes.string_currentSSID, null);

                                if (wm.getConnectionInfo() == null || !wm.getConnectionInfo().getSSID().equalsIgnoreCase(ssid_no_quote)) { // Router down
                                    MiniWifiScanUpdater iw = new MiniWifiScanUpdater();
                                    if (mActivity != null) {
                                        videoPlaybackTasks.startWifiTask(mActivity, iw);
                                    }
                                } else {
                                    if (sessionAttributes.unexpectedLocalRetries > 3) {
                                        // Reset local retries counter
                                        sessionAttributes.unexpectedLocalRetries = 0;
                                        if (P2pManager.getInstance().isRtmpStreamingEnabled()) {
                                            prepareToViewCameraViaRelay();
                                        } else {
                                            prepareToViewCameraLocally();
                                        }
                                    } else {
                                        sessionAttributes.unexpectedLocalRetries++;
                                        prepareToViewCameraLocally();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }


    private void viewRelayStream() {
        Log.d(TAG, "viewRelayStream");
        sessionAttributes.connecting_start_time = System.currentTimeMillis();
        prepareToViewCameraRemotely();
    }

    private void clearKeepScreenOnFlags() {
        if (mActivity.getWindow() != null) {
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private class VideoTimeoutTask extends TimerTask {
        public void run() {
            if (ViewFinderFragment.this.isAdded() && mActivity!=null) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoPlaybackTasks.initCountDownTimer(new ITimerUpdater() {
                            @Override
                            public void updateCurrentCount(final int count) {
                                Log.d(TAG, "currentc count:"+count);
                            }

                            @Override
                            public void timeUp() {
                                if (deviceAttributes.activity_has_stopped) {
                                    return;
                                }
                                // Time is really up --
                                if (mActivity != null && isAdded()) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            stopLiveFragmentStreaming();
                                            mIsStreamingTimedOut=true;
                                            videoPlaybackTasks.stopAllTimers();
                                            clearKeepScreenOnFlags();
                                            createVideoTimeoutDialog();
                                        }
                                    });
                                }

                            }

                            private void createVideoTimeoutDialog() {
                                final VideoTimeoutDialog dialog = new VideoTimeoutDialog();


                                dialog.setCommonDialogListener(new CommonDialogListener() {
                                    @Override
                                    public void onDialogPositiveClick(DialogFragment arg0) {
                                        dialog.dismiss();
                                        sessionAttributes.isVideoTimeout = false;
                                        scanAndViewCamera();
                                        CommonUtil.setSettingInfo(mContext,SettingsPrefUtils.PREFS_SHOULD_VIDEO_TIMEOUT,false);

                                    }

                                    @Override
                                    public void onDialogNegativeClick(DialogFragment dialog) {
                                        dialog.dismiss();
                                        sessionAttributes.isVideoTimeout = false;

                                        if (mActivity != null) {
                                            startMainActivity();
                                        }
                                    }

                                    @Override
                                    public void onDialogNeutral(DialogFragment dialog) {
                                        dialog.dismiss();
                                        sessionAttributes.isVideoTimeout = false;
                                        scanAndViewCamera();
                                    }
                                });
                                /*GoogleAnalyticsController.getInstance().trackCameraInfo("Media Timeout While Streaming",
                                        selectedDevice, isLocalStreaming());*/
                                if(isAdded() && isVisible() && !isInBGMonitoring)
                                    dialog.show(getChildFragmentManager(), "Dialog_timeout_streaming");
                            }

                            @Override
                            public void timerKick() {
                                if (deviceAttributes.activity_has_stopped) {
                                    return;
                                }

                                // Timer is kicked before timeout
                                // create a new remoteVideoTimer ..
                                if(mActivity!=null && isAdded()) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendKeepAliveRelaySession();
                                        }
                                    });
                                }

                                videoPlaybackTasks.initRemoteVideoTimer();
                                videoPlaybackTasks.scheduleRemoteVideoTimerTask(new VideoTimeoutTask(), VIDEO_TIMEOUT);
                            }
                        });

                        videoPlaybackTasks.startCountDownTimerThread();
                    }
                });
            }
        }

    }


    private class StandByTimeoutTask extends TimerTask
    {
        private int mRemainingTime;
        public StandByTimeoutTask(int remainingTime)
        {
            mRemainingTime = remainingTime;
        }
        public void run()
        {
            if (ViewFinderFragment.this.isAdded() && mActivity!=null)
            {
                mActivity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        videoPlaybackTasks.initStandByCountDownTimer(new ITimerUpdater()
                        {
                            @Override
                            public void updateCurrentCount(final int count)
                            {
                            }

                            @Override
                            public void timeUp()
                            {
                                if (deviceAttributes.activity_has_stopped)
                                {
                                    return;
                                }
                                // Time is really up --
                                if (mActivity != null && isAdded())
                                {
                                    mActivity.runOnUiThread(new Runnable()
                                    {
                                        public void run()
                                        {
                                            createStandByTimeoutDialog();
                                        }
                                    });
                                }

                            }

                            private void createStandByTimeoutDialog()
                            {
                                final StandByTimeoutDialog dialog =  StandByTimeoutDialog.newInstance(false,mRemainingTime);

                                dialog.setCommonDialogListener(new CommonDialogListener()
                                {
                                    @Override
                                    public void onDialogPositiveClick(DialogFragment arg0)
                                    {
                                        sessionAttributes.isStandByTimeout = true;
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onDialogNegativeClick(DialogFragment dialog)
                                    {
                                        sessionAttributes.isStandByTimeout = true;
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onDialogNeutral(DialogFragment dialog)
                                    {
                                        sessionAttributes.isStandByTimeout = true;
                                    }
                                });

                                sessionAttributes.isStandByTimeout = true;
                                dialog.show(getChildFragmentManager(), "DIALOG_STAND_BY_VIDEO_TIMEOUT");

                            }

                            @Override
                            public void timerKick()
                            {
                                if (deviceAttributes.activity_has_stopped)
                                {
                                    return;
                                }

                            }
                        });

                        videoPlaybackTasks.startStandByCountDownTimerThread();
                    }
                });
            }
        }

    }
    private void onUnexpectedStreamEnd() {
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_STREAM+" : "+AppEvents.UNEXPECTED_STREAM_END,AppEvents.UNEXPECTED_STREAM_END);

        ZaiusEvent vfStreamEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
        vfStreamEvt.action(AppEvents.VF_STREAM+" : "+AppEvents.UNEXPECTED_STREAM_END);
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(vfStreamEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onUnexpectedStreamEnd :- " +sessionAttributes.isStandByTimeout );

        if(!sessionAttributes.isStandByTimeout)
        {
            mStreamRetryCount++;

            if (((mIsRTMPLinkReceived && mStreamRetryCount > MAX_STREAM_RETRY_RTMP) ||(!mIsRTMPLinkReceived  && mStreamRetryCount > MAX_STREAM_RETRY)) && mActivity != null)
            {
                Log.d(TAG,"stream retry count:"+mStreamRetryCount);
                if(mActivity!=null && isAdded()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideSpinner(false);
                            mErrorLayout.setVisibility(View.VISIBLE);
                        }
                    });
                }

                return;
            }
            Log.d(TAG, "stream retry count :"+mStreamRetryCount+" is RTMP link recieved:"+mIsRTMPLinkReceived+" retrying..");
            AsyncPackage.doInBackground(new Runnable()
            {
                @Override
                public void run() {

                    if (!sessionAttributes.userWantToCancel && !deviceAttributes.activity_has_stopped && mActivity != null && isAdded()) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadLatestCameraSnap();
                                if (sessionAttributes.isDebugEnabled)
                                    updateDebugBitrateDisplay(0); // reset bitrate info to 0 kbps
                                showSpinner(mActivity.getString(R.string.EntryActivity_connecting_to_bm));
                            }
                        });

                        if (!deviceAttributes.is_upgrading) {
                            videoPlaybackTasks.stopAllTimers();

                        /* 20151201: HOANG: FWPR-529
                         * If stream has stopped, stop recording if it's in progress.
                         */
                            //stopRecording();
                            if (mActivity != null && isAdded()) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCaptureFragment != null && mCaptureFragment.isRecording()) {
                                            mCaptureFragment.stopRecording();
                                            removeFragment(mCaptureFragment);
                                            Toast.makeText(mActivity, mActivity.getString(R.string.viewfinder_recording_stopped), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            Log.d(TAG, "onUnexpectedStreamEnd, checking isCameraInSameNetwork");
                            boolean isInSameNetwork = isCameraInSameNetwork();
                            Log.d(TAG, "onUnexpectedStreamEnd, isCameraInSameNetwork? " + isInSameNetwork);
                            if (isInSameNetwork) {
                                // checkToShowTimelineAndVideoView();
                                // prepareToViewCameraLocally();
                                resolveLocalStream();
                            } else {
                                if (mActivity != null && isAdded()) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isWifiConnected()) {
                                                resolveRemoteStream(true);
                                            } else {
                                                resolveRemoteStream(false);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            });

            mIsStreaming = false;
        }
        else
        {
            hideSpinner(false);
            mErrorLayout.setVisibility(View.VISIBLE);
            stopLiveFragmentStreaming();

            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {

                    if (!sessionAttributes.userWantToCancel && !deviceAttributes.activity_has_stopped && getActivity() != null)
                    {
                        if (!deviceAttributes.is_upgrading)
                        {
                            videoPlaybackTasks.stopAllTimers();
                            if (mActivity != null && isAdded()) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCaptureFragment != null && mCaptureFragment.isRecording())
                                        {
                                            mCaptureFragment.stopRecording();
                                            removeFragment(mCaptureFragment);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });

            mError1Tv.setText(getResources().getString(R.string.device_entering_powersaving));
            mError2Tv.setVisibility(View.GONE);
            mError3Tv.setVisibility(View.GONE);

            Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.image_rotate);
            mErrorImageView.setImageDrawable(getResources().getDrawable(R.drawable.settingup));
            mErrorImageView.setVisibility(View.VISIBLE);;
            mErrorImageView.startAnimation(animation);

            mRetryButton.setText(getResources().getString(R.string.wake_up));
            mRetryButton.setTextColor(getResources().getColor(R.color.white_transperent));
            mRetryButton.setEnabled(false);
            mRetryButton.setBackground(getResources().getDrawable(R.drawable.viewfinder_disable_button));

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if (mActivity != null && isAdded())
                    {
                        mErrorImageView.clearAnimation();
                        mErrorImageView.setVisibility(View.GONE);

                        mError1Tv.setText(getResources().getString(R.string.device_sleep_mode));
                        mError2Tv.setVisibility(View.VISIBLE);
                        mError2Tv.setText(getResources().getString(R.string.session_timeout));
                        mError3Tv.setVisibility(View.GONE);

                        mRetryButton.setEnabled(true);
                        mRetryButton.setBackground(getResources().getDrawable(R.drawable.viewfinder_button));
                        mRetryButton.setTextColor(getResources().getColor(R.color.white));
                    }
                }
            }, DEFAULT_WAIT_TIMEOUT);

        }
    }

    private void resolveRemoteStream(boolean shouldConnectViaWifi) {
        Log.d(TAG, "Resolve remote stream, shouldConnectViaWifi? " + shouldConnectViaWifi);
//    if (shouldConnectViaWifi && !MobileSupervisor.getInstance().getIsMobileDataConnected()) {
//      remoteVideoHasStopped(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY);
//    } else if (MobileSupervisor.getInstance().getIsMobileDataConnected()) {
//      Check whether fragment is added to show dialog or it would crash.
//      if (isAdded()) {
//        buildMobileDataConnectedDialog();
//      }
//      remoteVideoHasStopped(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY);
//    } else {
//      remoteVideoHasStopped(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY);
//    }
        remoteVideoHasStopped(Streamer.MSG_VIDEO_STREAM_HAS_STOPPED_UNEXPECTEDLY);
    }

    public void stopLiveFragmentStreaming() {
    /*
     * 20160420: HOANG: Fix leak rtmp/p2p stream task when user switch from live streaming view to
     * motion clip view.
     * Solution: app should cancel all pending tasks (Rtmp/P2p stream task).
     */
        Log.d(TAG, "stopLiveFragmentStreaming");
        if(videoPlaybackTasks != null)
            videoPlaybackTasks.stopLiveStreamingTasks();

        stopStreaming();

    }

    private void doPanTilt(int direction) {
        Log.d(TAG, "doPanTilt:" + direction);
        if (direction == DIRECTION_RIGHT) {
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {
                    final int panTiltRet = CameraCommandUtils.sendCommandGetIntValue(
                            selectedDevice, PublicDefineGlob.DIR_MOVE_RIGHT, null, null);
                    if (panTiltRet >=0) {
                        Log.i(TAG, "Move right result " + panTiltRet);

                    }
                }
            });
        } else if (direction == DIRECTION_LEFT) {
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {
                    final int panTiltRet = CameraCommandUtils.sendCommandGetIntValue(
                            selectedDevice, PublicDefineGlob.DIR_MOVE_LEFT, null, null);
                    if (panTiltRet >=0 ) {
                        Log.i(TAG, "Move left result " + panTiltRet);
                    }
                }
            });
        } else if (direction == DIRECTION_DOWN) {
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {
                    final int panTiltRet = CameraCommandUtils.sendCommandGetIntValue(
                            selectedDevice, PublicDefineGlob.DIR_MOVE_FWD, null, null);
                    if (panTiltRet >= 0) {
                        Log.i(TAG, "Move down result " + panTiltRet);
                    }
                }
            });
        } else if (direction == DIRECTION_UP) {
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {
                    final int panTiltRet = CameraCommandUtils.sendCommandGetIntValue(
                            selectedDevice, PublicDefineGlob.DIR_MOVE_BWD, null, null);
                    if (panTiltRet>=0) {
                        Log.i(TAG, "Move up result " + panTiltRet);
                    }
                }
            });
        } else if (direction == DIRECTION_STOP) {
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {
                    final String response = CameraCommandUtils.sendCommandGetStringValue(selectedDevice, PublicDefineGlob.DIR_FB_STOP, null, null);
                    if (response != null) {
                        Log.i(TAG, "Foward backward pan tilt stop result: " + response);
                    }
                }
            });
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {
                    final String response = CameraCommandUtils.sendCommandGetStringValue(selectedDevice, PublicDefineGlob.DIR_LR_STOP, null, null);
                    if (response != null) {
                        Log.i(TAG, "Left right pan tilt stop result: " + response);
                    }
                }
            });
        } else if (direction == DIRECTION_INVALID) {
            Log.i(TAG, "Direction is invalid, do not pantilt");
        }
    }

    private void startPanTiltThread() {
        Log.d(TAG, "startPanTilt");
        shouldStop = false;
        panTiltThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!shouldStop) {
                    if (needPanTilt && settings.getBoolean(AccountSettingFragment.SWIPE_TO_PANTILT, false)) {
                        // Log.i(TAG, "Pantilt thread is running, direction: " + currentDirection);
                        doPanTilt(currentDirection);
                    } else {
                        // Log.i(TAG, "Pantilt thread is running but do not need pantilt");
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
//                        e.printStackTrace();
                        Log.i(TAG, "Pantilt thread is interrupted");
                    }
                }
            }
        });
        panTiltThread.start();
        Log.i(TAG, "Started pantilt thread");
    }


    @Override
    public void onCheckNowOfflineMode() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void scanAndViewCamera() {
        Log.d(TAG, "scanAndViewCamera");
        Log.d(TAG, "selectedCamera:"+selectedDevice.getProfile().isAvailable());
        mIsStreamingTimedOut=false;
        mIsRTMPLinkReceived=false;
        if (isAdded()) {
            if (mActivity.getWindow() != null) {
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            mStreamRetryCount=0;
            if (selectedDevice != null) {
                showSpinner(getString(R.string.EntryActivity_connecting_to_bm));
                final boolean useRemoteOnly = settings.getBoolean(PublicDefineGlob.PREFS_USE_REMOTE_ONLY, false);
                if(useRemoteOnly) {
                    Toast.makeText(mActivity, "Forcing remote - set in debug preferences", Toast.LENGTH_SHORT).show();
                }
                //check if device available and load latest snapshot
                checkToShowVideoView();

                Log.d(TAG, "Reset default video bitrate value to begin new view session.");
                if(selectedDevice.getProfile().isStandBySupported())
                {
                    HubbleSessionManager.getInstance().initOrbitBitRate();
                    getRemainingTime(false);
                }
                else
                {
                    HubbleSessionManager.getInstance().initBitRate();
                }
                HubbleSessionManager.getInstance().setCurrentCameraRegId(selectedDevice.getProfile().getRegistrationId());
                HubbleSessionManager.getInstance().reset();

                AsyncPackage.doInBackground(new Runnable() {
                    @Override
                    public void run() {
                        P2pClient selectedP2pClient = P2pService.getP2pClient(selectedDevice.getProfile().getRegistrationId());
                        boolean existInP2pList = selectedP2pClient != null;
                        Log.d(TAG, "exist in P2P list:"+existInP2pList);
                        boolean isInLocal=false;
                        if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() &&
                                isOrbitP2PEnabled() && selectedDevice.getProfile().canUseP2p() && selectedDevice.getProfile().canUseP2pRelay()
                                && existInP2pList) {
                            /*
                             * 20170330 HOANG Apply keep alive mode for P2P session
                             * If the P2P connection is already created in P2P service, don't need to check local here.
                             */

                            isInLocal=false;
                            Log.d(TAG, "valid P2P session?"+selectedP2pClient.isValid());
                           /* if(selectedP2pClient.isValid()) {
                                mUSeP2P = true;
                            } else {
                                mUSeP2P = false;
                            }
                            Log.d(TAG, "isP2PSession valid?"+selectedP2pClient.isValid()+ ", switching to RTMP or RTSP:"+!mUSeP2P);
*/
                        }else{
                            isInLocal=isCameraInSameNetwork();
                        }


                       /* if(mUSeP2P || isMobileDataConnected())
                            isInLocal=false;
                        else
                            isInLocal = isCameraInSameNetwork();*/

                        Log.d(TAG, "is camera in same network? " + isInLocal);
                        if (isWifiConnected() && isInLocal && !useRemoteOnly) {
                            if(mActivity!=null && isAdded()) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        prepareToViewCameraLocally();

                                    }
                                });
                            }
                        } else if (selectedDevice.getProfile().isAvailable() || useRemoteOnly) {
                            if(mActivity!=null && isAdded()) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewRelayStream();

                                    }
                                });
                            }
                        } else {
                            if (!selectedDevice.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {
                                if(mActivity!=null && isAdded()) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(mActivity, mActivity.getString(R.string.camera_offline), Toast.LENGTH_SHORT).show();
                                            //((ViewFinderActivity) mActivity).switch(selectedDevice);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }

        }
    }

    private void checkToShowVideoView() {
        if (!selectedDevice.getProfile().isAvailable() && !mIsFragmentHidden) {
            Toast.makeText(mActivity.getApplicationContext(), getString(R.string.camera_disconnected), Toast.LENGTH_LONG).show();
        }
       // if(!selectedDevice.getProfile().isStandBySupported())
            loadLatestCameraSnap();
    }

    public boolean isLocalStreaming() {
        Log.d(TAG, "isLocalStreaming");
        boolean isInLocalMode = false;
        if (deviceAttributes.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_RTSP_LOCAL ||
                P2pManager.getInstance().isP2pLocalStreaming()) {
            isInLocalMode = true;
        }
        return isInLocalMode;
    }

    private boolean isWifiConnected() {
        NetworkInfo wifiInfo=null;
        if(getActivity()!=null) {
            ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        }
        return (wifiInfo != null && wifiInfo.isConnected());
    }

    public void checkAndFlushAllBuffers() {
        if (mMovieView != null) {
            mMovieView.checkAndFlushAllBuffers();
        }
    }

    public static void setIPOverrideSetting(Activity x, String ip) {
        settings.putString("LOCAL_IP_OVERRIDE", ip);
        String message = "Saved overriding local IP: " + ip;
        Toast.makeText(x, message, Toast.LENGTH_SHORT).show();
    }

    public static String getIPOverrideSetting(Activity activity) {
        String setting = settings.getString("LOCAL_IP_OVERRIDE", "");
        return setting;
    }

    private String getSelectedRegistrationId() {
        return selectedDevice.getProfile().getRegistrationId();
    }

    private String getApiKey() {
        return getPrefString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
    }

    public boolean isP2pStreaming() {
        boolean isInP2pMode = true;
        if (deviceAttributes.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_RTMP_REMOTE ||
                deviceAttributes.currentConnectionMode == HubbleSessionManager.CONNECTION_MODE_RTSP_LOCAL) {
            isInP2pMode = false;
        }
        return isInP2pMode;
    }

    private void prepareToViewCameraLocally() {
        Log.d(TAG, "prepareToViewCameraLocally");
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.CAMERA_STREAMING_VIA+" : Locally",AppEvents.CAMERA_STREAMING_LOCALLY);

        ZaiusEvent localStreamingEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
        localStreamingEvt.action(AppEvents.CAMERA_STREAMING_VIA+" : Locally");
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(localStreamingEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }

        if (!isOfflineMode && (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() && isOrbitP2PEnabled() && selectedDevice.getProfile().canUseP2p())) {
            // Port from V2 app. Show p2p outdated dialog for camera with p2p_protocol 02_00
//      if (selectedDevice.getProfile().canUseP2pRelay()) {
//        Log.d(TAG, "P2p protocol version of camera can use p2p relay -> show outdated dialog");
//        setP2pOutdatedDialogShown(true);
//      } else
            {
                if (sessionAttributes.p2pTries < P2pManager.P2P_LOCAl_TRIES_MAX) {
                    prepareToViewCameraViaP2p(true);
                } else {
                    if (HubbleApplication.isVtechApp()) {
                        prepareToViewCameraViaP2p(false);
                    } else {
                        if (P2pSettingUtils.getInstance().isRtspStreamingEnabled()) {
                            Log.d(TAG, String.format("P2p tries? %d -> switch to rtsp", sessionAttributes.p2pTries));
                            prepareToViewCameraViaRtsp();
                        } else {
                            Log.d(TAG, String.format("P2p tries? %d, rtsp is disabled -> switch to relay", sessionAttributes.p2pTries));
                            prepareToViewCameraViaRelay();
                        }
                    }
                }
            }
        } else {
            Log.d(TAG, "Try rtsp due to: isP2pStreamingEnabled? " + P2pSettingUtils.getInstance().isP2pStreamingEnabled() +
                    ", canUseP2p? " + selectedDevice.getProfile().canUseP2p() + ", canUseP2pRelay? " + selectedDevice.getProfile().canUseP2pRelay() + ", isOfflineMode? " + isOfflineMode+ ", orbit p2p :-" + isOrbitP2PEnabled());

            if (P2pSettingUtils.getInstance().isRtspStreamingEnabled()) {
                prepareToViewCameraViaRtsp();
            } else {
                Log.d(TAG, "Rtsp streaming is disabled, switch to rtmp relay");
                prepareToViewCameraViaRelay();
            }
        }
    }


    private void prepareToViewCameraViaRtsp() {
        Log.d(TAG, "prepareToViewCameraViaRtsp");
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.CAMERA_STREAMING_VIA+" : RTSP",AppEvents.CAMERA_STEAMING_RTSP);
        ZaiusEvent rtspStreamingEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
        rtspStreamingEvt.action(AppEvents.CAMERA_STREAMING_VIA+" : RTSP");
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(rtspStreamingEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }
        sessionAttributes.viewRelayRtmp = false;
        sessionAttributes.viewP2p = false;
        sessionAttributes.p2pClients = null;
        deviceAttributes.currentConnectionMode = HubbleSessionManager.CONNECTION_MODE_RTSP_LOCAL;
        HubbleSessionManager.getInstance().updateCurrentConnectionMode(deviceAttributes.currentConnectionMode);
        HubbleSessionManager.getInstance().reset();
        deviceAttributes.device_ip = selectedDevice.getProfile().getDeviceLocation().getLocalIp();
        int devicePort = -1;
        try {
            devicePort = Integer.parseInt(selectedDevice.getProfile().getDeviceLocation().getLocalPort1());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        deviceAttributes.device_port = devicePort;
        String deviceIPOverride = getIPOverrideSetting(mActivity);
        if (!deviceIPOverride.equals("")) {
            sessionAttributes.filePath = String.format("rtsp://user:pass@%s:%d/blinkhd", deviceIPOverride, 6667);
        } else {
            sessionAttributes.filePath = String.format("rtsp://user:pass@%s:%d/blinkhd", deviceAttributes.device_ip, 6667);
        }
        setupFFMpegPlayer();
    }

    private void prepareToViewCameraViaRelay() {
        Log.d(TAG, "prepareToViewCameraViaRelay");
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.CAMERA_STREAMING_VIA+" : RTMP",AppEvents.CAMERA_STREAMING_RTMP);

        ZaiusEvent rtmpStreamingEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
        rtmpStreamingEvt.action(AppEvents.CAMERA_STREAMING_VIA+" : RTMP");
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(rtmpStreamingEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }

        sessionAttributes.viewRelayRtmp = true;
        sessionAttributes.viewP2p = false;
        deviceAttributes.currentConnectionMode = HubbleSessionManager.CONNECTION_MODE_RTMP_REMOTE;
        HubbleSessionManager.getInstance().updateCurrentConnectionMode(deviceAttributes.currentConnectionMode);


        String saved_token = getApiKey();
        String regId = getSelectedRegistrationId();
        if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() &&
                P2pSettingUtils.getInstance().isRemoteP2pStreamingEnabled() && isOrbitP2PEnabled() && selectedDevice.getProfile().canUseP2p()) {
            // Port from V2 app. Show p2p outdated dialog for camera with p2p_protocol 02_00
//      if (selectedDevice.getProfile().canUseP2pRelay()) {
//        Log.d(TAG, "P2p protocol version of camera can use p2p relay -> show outdated dialog");
//        setP2pOutdatedDialogShown(true);
//      } else
            {
                HubbleSessionManager.getInstance().adjustToTempVideoBitrate();
                if (sessionAttributes.reservedRtmpUrl != null) {
                    viewRelayCameraWithExistingUrl();
                } else {
                    try {
                        videoPlaybackTasks.startRelayStreamTask(mActivity, this, LegacyCamProfile.fromDeviceProfile(selectedDevice.getProfile()), regId, saved_token);
                    } catch (Exception e) {
                    }
                }
            }
        } else {
            Log.d(TAG, "Try rtmp due to: isRemoteP2pEnabled? " + P2pSettingUtils.getInstance().isRemoteP2pStreamingEnabled() +
                    ", canUseP2p? " + selectedDevice.getProfile().canUseP2p() + " and orbit p2p :- " + isOrbitP2PEnabled());
            HubbleSessionManager.getInstance().adjustToTempVideoBitrate();
            try {
                videoPlaybackTasks.startRelayStreamTask(mActivity, this, LegacyCamProfile.fromDeviceProfile(selectedDevice.getProfile()), regId, saved_token);
            } catch (Exception e) {
            }
        }
    }

    private void viewRelayCameraWithExistingUrl() {

        Log.d(TAG, "viewRelayCameraWithExistingUrl: " + sessionAttributes.reservedRtmpUrl);
        String regId = getSelectedRegistrationId();
        String streamUrl = sessionAttributes.reservedRtmpUrl;
        sessionAttributes.reservedRtmpUrl = null;
        videoPlaybackTasks.setStreamUrl(streamUrl);
        BabyMonitorAuthentication bmAuth = new BabyMonitorRelayAuthentication(null, "80", null, regId, null, streamUrl, 80, null, null);
        setupRemoteCamera(bmAuth);
    }


    private void setupRemoteCamera(BabyMonitorAuthentication bm_auth) {
        //deviceAttributes.currentConnectionMode = CONNECTION_MODE_RTMP_REMOTE;
        Log.d(TAG, "setupRemoteCamera");
        if (bm_auth != null) {
            deviceAttributes.device_ip = bm_auth.getIP();
            deviceAttributes.device_port = bm_auth.getPort();
            sessionAttributes.babyMonitorAuthentication = bm_auth;// reserved to used later if we need to
            // restart the videostreamer - audio only mode
        }
        sessionAttributes.filePath = videoPlaybackTasks.getStreamUrl();
        if (P2pSettingUtils.hasP2pFeature()) {
            sessionAttributes.p2pClients = null;
        }

        // TURD: Follow the white rabbit.
        new Handler().post(new Runnable() {
            public void run() {
                // Down we go...
                setupFFMpegPlayer();
            }
        });

    }

    private void prepareToViewCameraRemotely() {
        Log.d(TAG, "prepareToViewCameraRemotely");
        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.CAMERA_STREAMING_VIA+" : Remotely",AppEvents.CAMERA_STREAMING_REMOTELY);

        ZaiusEvent remoteStreamingEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
        remoteStreamingEvt.action(AppEvents.CAMERA_STREAMING_VIA+" : Remotely");
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(remoteStreamingEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }

        sessionAttributes.create_session_start_time = System.currentTimeMillis();

        Log.d(TAG, "Try --- isP2pStreamingEnabled? " + P2pSettingUtils.getInstance().isP2pStreamingEnabled() +
                ", canUseP2p? " + selectedDevice.getProfile().canUseP2p() + ", canUseP2pRelay? " + selectedDevice.getProfile().canUseP2pRelay() + " and orbit p2p:-" + isOrbitP2PEnabled());

        if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() &&
                P2pSettingUtils.getInstance().isRemoteP2pStreamingEnabled() && isOrbitP2PEnabled() &&  selectedDevice.getProfile().canUseP2p()) {
            // Port from V2 app. Show p2p outdated dialog for camera with p2p_protocol 02_00
//      if (selectedDevice.getProfile().canUseP2pRelay()) {
//        Log.d(TAG, "P2p protocol version of camera can use p2p relay -> show outdated dialog");
//        setP2pOutdatedDialogShown(true);
//      } else
            {
                if (sessionAttributes.p2pTries < P2pManager.HUBBLE_P2P_MAX_TRY) {
                    prepareToViewCameraViaP2p(false);
                } else {
                    if (P2pManager.getInstance().isRtmpStreamingEnabled()) {
                        Log.d(TAG, String.format("P2p tries? %d, rtmp streaming is enabled -> switch to relay", sessionAttributes.p2pTries));
                        prepareToViewCameraViaRelay();
                    } else {
                        Log.d(TAG, "Rtmp streaming is disabled -> continue to try p2p");
                        prepareToViewCameraViaP2p(false);
                    }
                }
            }
        } else {
            Log.d(TAG, "Try rtmp due to: isP2pStreamingEnabled? " + P2pSettingUtils.getInstance().isP2pStreamingEnabled() +
                    ", canUseP2p? " + selectedDevice.getProfile().canUseP2p() + ", canUseP2pRelay? " + selectedDevice.getProfile().canUseP2pRelay()+", orbit p2p :" + isOrbitP2PEnabled());
            prepareToViewCameraViaRelay();
        }
    }

    private void prepareToViewCameraViaP2p(final boolean isInLocal) {
        Log.d(TAG, "prepareToViewCameraViaP2p, using mobile data? " + isMobileDataConnected());
        startP2pStreamTask(isInLocal);
    }

    private void startP2pStreamTask(boolean isInLocal) {
        Log.d(TAG, "startP2pStreamTask, isInLocal? " + isInLocal);
        sessionAttributes.viewRelayRtmp = false;
        sessionAttributes.viewP2p = true;
        sessionAttributes.p2pTries++;
        if (isInLocal) {
            deviceAttributes.currentConnectionMode = HubbleSessionManager.CONNECTION_MODE_P2P_LOCAL;
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.CAMERA_STREAMING_VIA+" : P2P_Local",AppEvents.CAMERA_STREAMING_P2PLOCAL);

            ZaiusEvent p2pLocalStreamingEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
            p2pLocalStreamingEvt.action(AppEvents.CAMERA_STREAMING_VIA+" : P2P_Local");
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(p2pLocalStreamingEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }

        } else {
            deviceAttributes.currentConnectionMode = HubbleSessionManager.CONNECTION_MODE_P2P_REMOTE;
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.CAMERA_STREAMING_VIA+" : P2P_Remote",AppEvents.CAMERA_STREAMING_P2PREMOTE);

            ZaiusEvent p2pRemoteStreamingEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
            p2pRemoteStreamingEvt.action(AppEvents.CAMERA_STREAMING_VIA +" : P2P_Remote");
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(p2pRemoteStreamingEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }

        }
        HubbleSessionManager.getInstance().updateCurrentConnectionMode(deviceAttributes.currentConnectionMode);
        HubbleSessionManager.getInstance().reset();
        String saved_token = getApiKey();
        String regId = getSelectedRegistrationId();
        try {
            LegacyCamProfile currCamProfile = LegacyCamProfile.fromDeviceProfile(selectedDevice.getProfile());
            currCamProfile.setInLocal(isInLocal);
            videoPlaybackTasks.startP2pStreamTask(mActivity, this, currCamProfile, regId, saved_token, isMobileDataConnected(), selectedDevice.getProfile().canUseP2pRelay());
        } catch (Exception e) {
        }
    }


    private void hideSpinner(final boolean success) {
        if(mActivity!=null && isAdded()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mLoadingSpinner != null) {
                        mLoadingSpinner.clearAnimation();
                        mLoadingSpinner.setVisibility(View.GONE);
                        mLoadingInfoLayout.setVisibility(View.GONE);
                    }
                    if(success) {
                        mLiveStatusView.setVisibility(View.VISIBLE);
                        if (!isDeviceCharging &&selectedDevice != null && selectedDevice.getProfile().isStandBySupported())
                        {
                            mAlertLayout.clearAnimation();
                            mAlertLayout.setVisibility(View.GONE);
                        }
                    }
                    else{
                        if(sessionAttributes.isDebugEnabled){
                            mDebugLayout.setVisibility(View.GONE);
                        }
                        mAlertLayout.clearAnimation();
                        mAlertLayout.setVisibility(View.GONE);
                    }

                }
            });
        }
    }


    private void showSpinner(final String message) {
        if(mActivity!=null && isAdded()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mActivity!=null) {
                        if(mErrorLayout!=null && mErrorLayout.getVisibility()==View.VISIBLE)
                             mErrorLayout.setVisibility(View.GONE);
                        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.image_rotate);
                        mLoadingSpinner.setVisibility(View.VISIBLE);
                        mLoadingInfoLayout.setVisibility(View.VISIBLE);
                        mLoadingMessageText.setText(message);
                        mLoadingSpinner.startAnimation(animation);
                        mLiveStatusView.setVisibility(View.GONE);
                        if (selectedDevice != null && selectedDevice.getProfile().isStandBySupported())
                        {
                            mAlertLayout.clearAnimation();
                            mAlertLayout.setVisibility(View.GONE);
                            mLoadingTimeText.setVisibility(View.VISIBLE);
                        }else{
                            mLoadingTimeText.setVisibility(View.GONE);
                        }
                    }

                }
            });
        }
    }

    private void loadLatestCameraSnap() {
        String registrationId = selectedDevice.getProfile().getRegistrationId();
        //Try to load preview image if possible.
        //what is preview image?
      /*  if (Util.shouldLoadLatestPreview(registrationId)) {
            loadLatestPreviewSnap(registrationId);
        } else {
            loadLatestLiveSnap(registrationId);
        }*/
      loadLatestLiveSnap(registrationId);
    }

    private void loadLatestPreviewSnap(String registrationId) {
        if (Util.isLatestPreviewAvailable(registrationId)) {
            if (getActivity() != null) {
                Log.d(TAG, "Loading latest preview snap for camera: " + registrationId + ", path: " + Util.getLatestPreviewPath(registrationId));
                mLatestSnap.setVisibility(View.VISIBLE);
                Picasso.with(getActivity())
                        .load(new File(Util.getLatestPreviewPath(registrationId)))
                        .skipMemoryCache()
                        .noFade()
                        .into(mLatestSnap);

            } else {
                mLatestSnap.setVisibility(View.GONE);
            }
        } else {
            mLatestSnap.setVisibility(View.GONE);
        }

    }

    private void loadLatestLiveSnap(String registrationId) {
        if (Util.isLatestSnapshotAvailable(registrationId)) {
            if (getActivity() != null && isAdded()) {
                Log.i(TAG, "Loading latest live snap for camera: " + registrationId + ", path: " + Util.getLatestSnapshotPath(registrationId));
                mLatestSnap.setVisibility(View.VISIBLE);
                Picasso.with(getActivity())
                        .load(new File(Util.getLatestSnapshotPath(registrationId)))
                        .skipMemoryCache()
                        .noFade()
                        .into(mLatestSnap);

            } else {
                mLatestSnap.setVisibility(View.GONE);
            }
        } else {
            mLatestSnap.setVisibility(View.GONE);
        }

    }


    private boolean isMobileDataConnected() {

        NetworkInfo mobileInfo=null;
        if(getActivity()!=null) {
            ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            mobileInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        }
        return (mobileInfo != null && mobileInfo.isConnected());
    }

    private boolean isCameraInSameNetwork() {

        boolean isLocal = false;
        if(!isMobileDataConnected())
        {
            if(isLocalVerify)
            {
               isLocal = selectedDevice.isAvailableLocally();
            }
            else
            {

                isLocal = CameraAvailabilityManager.getInstance().isCameraInSameNetwork(HubbleApplication.AppContext, selectedDevice);
                selectedDevice.setIsAvailableLocally(isLocal);
                isLocalVerify = true;
            }
        }

        return isLocal;
    }

    private void show_Device_removal_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(R.string.device_removal_confirmation)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Go to Camera List Page
                        startMainActivity();
                    }
                });

        AlertDialog confirmDeviceRemovalDialog = builder.create();
        confirmDeviceRemovalDialog.setCancelable(false);
        confirmDeviceRemovalDialog.setCanceledOnTouchOutside(false);
        try {
            confirmDeviceRemovalDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPrefString(final String prefName, final String optionalDefault) {
        return settings.getString(prefName, optionalDefault);
    }


    private void onCameraRemoved(Context context, Intent intent) {
        if (selectedDevice == null || selectedDevice.getProfile() == null
                || selectedDevice.getProfile().getRegistrationId() == null) {
            return;
        }
        final String selected_MAC = selectedDevice.getProfile().getRegistrationId();
        final String notification_MAC = intent.getExtras().getString("regId");
        Log.i("mbp", "Selected camera MAC : " + selected_MAC);
        Log.i("mbp", "Notification MAC : " + notification_MAC);
        if (selected_MAC.equalsIgnoreCase(notification_MAC)) {
            if (isVisible() && getActivity() != null) {
                show_Device_removal_dialog();
            } else {
                Log.i(TAG, "Activity has stopped, don't show device removed dialog.");
            }
        } else {
            Log.i(TAG, "Not current device, don't show device removed dialog.");
        }
    }

    public void setCamera(String regID) {
        // CameraAvailabilityManager.getInstance().isCameraInSameNetworkAsync(context, selectedChannel);
        //mIsFirstTime = true;
        mRegistrationID=regID;
        selectedDevice = DeviceSingleton.getInstance().getDeviceByRegId(regID);
    }

    public void setViewFinderEventListCallBack(IViewFinderEventListCallBack viewFinderEventListCallBack){
        mIViewFinderEventListCallBack = viewFinderEventListCallBack;
    }

    private void initialize() {

        if(mActivity!=null) {
            WifiManager w = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
            String curr_ssid = w.getConnectionInfo().getSSID();
            settings.putString(sessionAttributes.string_currentSSID, curr_ssid);
        }
        //check if notification is enabled
        if(selectedDevice!=null) {
            String mPrivacyMode = selectedDevice.getProfile().getDeviceAttributes().getPrivacyMode();
            // mIsNotificationOn = selectedDevice.getProfile().getDeviceAttributes().getPrivacyMode();//mSharedPreferences.getBoolean(selectedDevice.getProfile().getName()+"notification", true);
            if ((mPrivacyMode == null) || (!TextUtils.isEmpty(mPrivacyMode) && mPrivacyMode.equalsIgnoreCase("0"))) {
                mIsNotificationOn = true;
            } else {
                mIsNotificationOn = false;
            }
        }

        //todo set action bar

        mSettingsButton = (ImageView) view.findViewById(R.id.vf_toolbar_settings);


        mSettingsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((ViewFinderActivity) mActivity).switchToCameraSettingsActivity();
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.SETTINGS_CLICKED,AppEvents.SETTINGS_CLICKED);

                ZaiusEvent vf_settingsEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                vf_settingsEvt.action(AppEvents.SETTINGS_CLICKED);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(vf_settingsEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }
            }
        });


        ImageView backButton = (ImageView) view.findViewById(R.id.vf_toolbar_back);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onBackPressed");
                if (mActivity != null) {
                    mActivity.onBackPressed();
                }
            }
        });

        mOtaAvailableIv = (ImageView) view.findViewById(R.id.vf_ota_available);
        mOtaAvailableIv.setVisibility(View.GONE);
        mOtaAvailableIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (selectedDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0) {
                    if (shouldStartOTAProcess()) {
                        showOTAUpdateDialog(true);
                    } else {
                        showOTAUpdateDialog(false);
                    }

                } else {
                    if(isLocalStreaming()) {
                        showOTAUpdateDialog(false);
                    }else {
                        showLocalCameraInfoForOTA();
                    }
                }
            }
        });




        mControlGrid = (GridView) view.findViewById(R.id.control_button_grid);

        mLoadingSpinner = (ImageView) view.findViewById(R.id.progress_image);
        mLoadingMessageText=(TextView)view.findViewById(R.id.progress_msg);
        mLoadingInfoLayout=(LinearLayout)view.findViewById(R.id.progress_info_layout) ;
        mLoadingTimeText=(TextView)view.findViewById(R.id.progress_time) ;
        mErrorLayout=(LinearLayout)view.findViewById(R.id.error_layout);

        mErrorImageView=(ImageView)view.findViewById(R.id.error_iv);
        mError1Tv = (TextView)view.findViewById(R.id.error_tv1);
        mError2Tv = (TextView)view.findViewById(R.id.error_tv2);
        mError3Tv = (TextView)view.findViewById(R.id.error_tv3);

        mMovieView = (FFMpegMovieViewAndroid) view.findViewById(R.id.imageVideo);
        mLatestSnap = (ImageView) view.findViewById(R.id.preview_image);
        mDefaultControlsLayout = (RelativeLayout) view.findViewById(R.id.viewfinder_default_controls);
        mLiveActionListener = this;
        mPlaybackFrame=(FrameLayout)view.findViewById(R.id.playback_frame) ;
        mLiveStatusView=(TextView)view.findViewById(R.id.live_status_text);
        mHDImage=(ImageView)view.findViewById(R.id.hd_img);
        mMuteImage=(ImageView)view.findViewById(R.id.mute_img);
        mHDText=(TextView)view.findViewById(R.id.hd_text);
        mMuteText=(TextView)view.findViewById(R.id.mute_text);
        mTempText=(TextView)view.findViewById(R.id.temp_text);
        mNotificationOffLayout=(RelativeLayout)view.findViewById(R.id.notification_off_layout);
        mNotificationSwitch=(SwitchCompat) view.findViewById(R.id.notification_toggle);
        mDeviceOnOffSwitch = (SwitchCompat) view.findViewById(R.id.camera_on_switch);
        mAlertLayout=(LinearLayout)view.findViewById(R.id.alert_layout);
        mMuteLayout=(LinearLayout)view.findViewById(R.id.mute_layout);
        mHDLayout=(LinearLayout)view.findViewById(R.id.hd_layout);
        mHumidityText=(TextView)view.findViewById(R.id.humidity_text);
        mHumidityImage=(ImageView)view.findViewById(R.id.humidity_img);
        mDebugLayout=(LinearLayout)view.findViewById(R.id.debug_layout);
        mFrameRateText=(TextView)view.findViewById(R.id.textFrameRate);
        mResolutionText=(TextView)view.findViewById(R.id.textResolution);
        mBitRateText=(TextView)view.findViewById(R.id.textBitrate);
        mGlobalBitRateText=(TextView)view.findViewById(R.id.textGlobalBitrate);
        mWifiSignalText=(TextView)view.findViewById(R.id.textWifiSignal);
        mConnectionTypeImg=(ImageView)view.findViewById(R.id.connection_type);
        mViewFinderInfoLayout=(RelativeLayout)view.findViewById(R.id.viewfinder_info);
        mSeparatorView=(View)view.findViewById(R.id.viewfinder_separator);
        mEventListLayout=(RelativeLayout)view.findViewById(R.id.event_list_layout);
        mToolbar=(RelativeLayout)view.findViewById(R.id.toolbar);
        mStreamingLayout=(RelativeLayout)view.findViewById(R.id.streaming_layout);
        mSwitchLayout=(LinearLayout)view.findViewById(R.id.camera_switch_layout);
        mDeviceSpinner = (Spinner) view.findViewById(R.id.camera_spinner);
        mDeviceText=(TextView)view.findViewById(R.id.camera_name_text);
        mMusicFragment=new MusicFragment();
        //mRecordingFragment=new RecordingFragment();
       // mPanTiltFragment=new PanTiltFragment();
        mRetryButton =(Button)view.findViewById(R.id.retry_button);
        mRetryButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v)
            {
                mErrorImageView.setImageDrawable(getResources().getDrawable(R.drawable.alert_vf));
                mErrorImageView.setVisibility(View.VISIBLE);
                mError1Tv.setText(getResources().getString(R.string.viewfinder_error_connecting));
                mError1Tv.setVisibility(View.VISIBLE);
                mError2Tv.setText(getResources().getString(R.string.viewfinder_retry_instruction1));
                mError2Tv.setVisibility(View.VISIBLE);
                mError3Tv.setText(getResources().getString(R.string.viewfinder_retry_instruction2));
                mError3Tv.setVisibility(View.VISIBLE);
                mRetryButton.setText(getResources().getString(R.string.viewfinder_retry));
                mRetryButton.setVisibility(View.VISIBLE);
                mRetryButton.setBackground(getResources().getDrawable(R.drawable.viewfinder_button));
                mRetryButton.setTextColor(getResources().getColor(R.color.white));

                // reset timeout value
                sessionAttributes.isStandByTimeout = false;

                if(selectedDevice.getProfile().isStandBySupported())
                    checkDeviceStatus();
                else
                    scanAndViewCamera();

                if(mErrorLayout!=null)
                    mErrorLayout.setVisibility(View.GONE);
            }
        });



        mDeviceOnOffSwitch.setOnCheckedChangeListener(this);
        mNotificationSwitch.setOnCheckedChangeListener(this);

        mLiveStreamingRelativeLayout= (RelativeLayout)view.findViewById(R.id.live_streaming_layout);
        mLiveStreamingRelativeLayout.setVisibility(View.VISIBLE);

        mQuickSettingRelativeLayout = (RelativeLayout)view.findViewById(R.id.quick_setting_setup_layout);
        mQuickSettingRelativeLayout.setVisibility(View.GONE);

        mQuickUpTextView = (TextView)view.findViewById(R.id.setting_up_quick_text_view);
        mQuickUpSummaryTextView = (TextView)view.findViewById(R.id.setting_up_summary_text_view);
        mQuickUpImageView = (ImageView)view.findViewById(R.id.setting_up_anim_image);

        //quickSettingView(false);

        //setDeviceList();

        //commented to remove quickview for orbit
      /*  if(selectedDevice!=null && selectedDevice.getProfile().isStandBySupported()) {
            mLiveStreamingRelativeLayout.setVisibility(View.GONE);
            mQuickSettingRelativeLayout.setVisibility(View.VISIBLE);
            quickSettingView(false);
        }else{
            mLiveStreamingRelativeLayout.setVisibility(View.VISIBLE);
            mQuickSettingRelativeLayout.setVisibility(View.GONE);
        }*/

        //force portrait mode for orbit
       /* if((selectedDevice!=null && selectedDevice.getProfile().isStandBySupported())){
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }*/

        initialized = true;
    }

    private void displayProgressDialog()
    {
        if(mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage(mActivity.getResources().getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);

        mProgressDialog.show();
    }

    private void dismissDialog()
    {
        if(mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }


    private void quickSettingView(boolean isActive)
    {
        if(isActive)
        {
            mQuickUpTextView.setText(getResources().getString(R.string.setting_up_quick_view));
            mQuickUpTextView.setTextColor(getResources().getColor(R.color.white));

            mQuickUpSummaryTextView.setText(getResources().getString(R.string.setting_up_quick_summary));
            mQuickUpTextView.setTextColor(getResources().getColor(R.color.white));

            mQuickUpImageView.setImageDrawable(getResources().getDrawable(R.drawable.loading));
            mAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.processing_animation);
            mQuickUpImageView.startAnimation(mAnimation);
            mQuickUpImageView.setOnClickListener(null);

        }
        else
        {
            mQuickUpTextView.setText(getResources().getString(R.string.quick_view));
            mQuickUpTextView.setTextColor(getResources().getColor(R.color.quick_setting));

            mQuickUpSummaryTextView.setText(getResources().getString(R.string.quick_view_summary));
            mQuickUpTextView.setTextColor(getResources().getColor(R.color.white));

            mQuickUpImageView.clearAnimation();
            mQuickUpImageView.setImageDrawable(getResources().getDrawable(R.drawable.quickview));
            mQuickUpImageView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    checkDeviceStatus();
                }

            });
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!buttonView.isPressed())
            return;
        switch(buttonView.getId()) {
            case R.id.notification_toggle :
                handleNotificationToggle(isChecked);
                break;
            case R.id.camera_on_switch:
                if(mIsControlFragmentVisible && mCurrentControlFragment!=null)
                    removeFragment(mCurrentControlFragment);
                handleNotificationToggle(mDeviceOnOffSwitch.isChecked());
                if (mDeviceOnOffSwitch.isChecked()) {
                    mDeviceOnOffSwitch.setTextColor(getResources().getColor(R.color.text_blue));
                } else {
                    mDeviceOnOffSwitch.setTextColor(getResources().getColor(R.color.text_gray));
                }

                break;
        }//Switch end
    }

    private void wakeUpRemoteDevice()
    {
        //quickSettingView(true);
        Log.d(TAG, "wakeUpRemoteDevice");
        showSpinner(getString(R.string.viewfinder_progress_wakeup));

        mDeviceWakeup = DeviceWakeup.newInstance();
        mDeviceWakeup.setDelayP2PTask(15000);
        mDeviceWakeup.wakeupDevice(selectedDevice.getProfile().registrationId,accessToken,mDeviceHandler,selectedDevice);

    }

    private Handler  mDeviceHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if(!isAdded() || getActivity()==null)
                return;
            switch(msg.what)
            {
                case CommonConstants.DEVICE_WAKEUP_STATUS:
                    if(mActivity!=null && isAdded()) {
                        boolean result = (boolean) msg.obj;
                        Log.d(TAG, "Device status task completed..device status:" + result);
                        if (result) {

                            Log.d(TAG, "wakeup device:success");
                            hideSpinner(false);
                            scanAndViewCamera();
                        } else {
                            hideSpinner(false);
                            mErrorLayout.setVisibility(View.VISIBLE);
                            Log.d(TAG, "wakeup device:failure");
                        }
                    }
                    break;

                case CommonConstants.DEVICE_NOTIFICATION_STATUS:

                    boolean result1 = (boolean)msg.obj;
                    mIsNotificationOn=result1;

                    if(mActivity != null && isAdded())
                    {
                        dismissDialog();
                        displayProgressDialog();
                        if (result1) {
                            ChangePrivacyMode changePrivacyMode = new ChangePrivacyMode(selectedDevice, -1,  false, mActivity, mDeviceHandler);
                            changePrivacyMode.execute();
                        } else {
                            ChangePrivacyMode changePrivacyMode = new ChangePrivacyMode(selectedDevice, -1, true, mActivity, mDeviceHandler);
                            changePrivacyMode.execute();
                        }

                    }
                    break;

                case CommonConstants.DEVICE_CAMERA_STATUS:
                    if(mActivity!=null && isAdded()) {
                        dismissDialog();
                        final int position2 = msg.arg1;
                        String result2 = (String) msg.obj;
                        if (result2.equalsIgnoreCase("0")) {
                            mDeviceOnOffSwitch.setOnCheckedChangeListener(null);
                            mDeviceOnOffSwitch.setChecked(true);
                            mDeviceOnOffSwitch.setOnCheckedChangeListener(ViewFinderFragment.this);

                            mNotificationSwitch.setOnCheckedChangeListener(null);
                            mNotificationSwitch.setChecked(true);
                            mNotificationSwitch.setOnCheckedChangeListener(ViewFinderFragment.this);

                            //start streaming if notification is turned on
                            mNotificationOffLayout.setVisibility(View.GONE);
                            mLiveStreamingRelativeLayout.setVisibility(View.VISIBLE);
                            mLiveStatusView.setVisibility(View.VISIBLE);
                            mSwitchLayout.setVisibility(View.VISIBLE);
                            mTempText.setVisibility(View.VISIBLE);

                            if (!isDeviceCharging && selectedDevice != null && selectedDevice.getProfile().isStandBySupported()) {
                                mAlertLayout.clearAnimation();
                                mAlertLayout.setVisibility(View.GONE);
                            }

                            if (selectedDevice != null) {
                                if (CommonUtil.getSettingInfo(mActivity, selectedDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, false)) {
                                    mOtaAvailableIv.setVisibility(View.VISIBLE);
                                }else {
                                    mOtaAvailableIv.setVisibility(View.GONE);
                                }
                            }
                            scanAndViewCamera();

                        } else if (result2.equalsIgnoreCase("1")) {
                            mDeviceOnOffSwitch.setOnCheckedChangeListener(null);
                            mDeviceOnOffSwitch.setChecked(false);
                            mDeviceOnOffSwitch.setOnCheckedChangeListener(ViewFinderFragment.this);
                            //show notification off layout if notification is disabled
                            mNotificationOffLayout.setVisibility(View.VISIBLE);
                            mLiveStreamingRelativeLayout.setVisibility(View.INVISIBLE);
                            mDefaultControlsLayout.setVisibility(View.INVISIBLE);
                            mControlGrid.setVisibility(View.INVISIBLE);
                            mSwitchLayout.setVisibility(View.GONE);
                            mTempText.setVisibility(View.INVISIBLE);
                            mLiveStatusView.setVisibility(View.INVISIBLE);
                            mErrorLayout.setVisibility(View.GONE);
                            mNotificationSwitch.setOnCheckedChangeListener(null);
                            mNotificationSwitch.setChecked(false);
                            mNotificationSwitch.setOnCheckedChangeListener(ViewFinderFragment.this);
                            mOtaAvailableIv.setVisibility(View.GONE);
                            if (!isDeviceCharging && selectedDevice != null && selectedDevice.getProfile().isStandBySupported()) {
                                mAlertLayout.clearAnimation();
                                mAlertLayout.setVisibility(View.GONE);
                            }
                            stopStreamingBlocked();
                            mIsStreaming = false;


                        }
                    }
                    break;


            }
        }
    };


    public boolean isInBGMonitoring() {
        return isInBGMonitoring;
    }

    public void setIsInBGMonitoring(boolean value) {
        isInBGMonitoring = value;
    }

    public void setViewSessionStartTime(long start_time) {
        view_session_start_time = start_time;
    }

    public void stopStreaming() {
        Log.d(TAG, "ViewFinder, stop streaming...");
        AsyncPackage.doInBackground(new Runnable() {
            @Override
            public void run() {
                if (view_session_start_time != -1 && selectedDevice != null) {
                    view_session_start_time = -1;
                }

                if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() && p2pClient != null) {
                    if (selectedDevice != null && selectedDevice.getProfile() != null && isOrbitP2PEnabled() &&
                            selectedDevice.getProfile().canUseP2p() && selectedDevice.getProfile().canUseP2pRelay()) {
                        // Log.d(TAG, "Using new p2p flow, don't need to destroy p2p session");
                    } else {
                        P2pManager.getInstance().destroyP2pSession();
                    }
                }

                if (mMovieView != null && !mMovieView.isReleasingPlayer()) {
                    mMovieView.release();
                }
            }
        });
    }

    public void stopStreamingBlocked() {
        Log.d(TAG, "LiveFragment, stop streaming blocked...");
        videoPlaybackTasks.stopLiveStreamingTasks();
        if (view_session_start_time != -1 && selectedDevice != null) {
            view_session_start_time = -1;
        }

        if (P2pSettingUtils.hasP2pFeature() && P2pSettingUtils.getInstance().isP2pStreamingEnabled() && p2pClient != null) {
            if (selectedDevice != null && selectedDevice.getProfile() != null && isOrbitP2PEnabled() &&
                    selectedDevice.getProfile().canUseP2p() && selectedDevice.getProfile().canUseP2pRelay()) {
                // Log.d(TAG, "Using new p2p flow, don't need to destroy p2p session");
            } else {
                P2pManager.getInstance().destroyP2pSession();
            }
        }

        if (mMovieView != null && !mMovieView.isReleasingPlayer()) {
            mMovieView.release();
        }
        Log.d(TAG, "ViewFinder, stop streaming blocked...DONE");
        mIsStreaming=false;
    }

    public void muteAudio(Boolean isEnabled) {
        if(mMovieView!=null)
             mMovieView.enableAudio(isEnabled);
    }

    public FFMpegMovieViewAndroid getFFMpegMovieViewAndroid() {
        return mMovieView;
    }

    public void setStreamUrl(String url) {
        filePath = url;
        setupFFMpegPlayer();
    }

    private void queryRunningOS() {
        Log.d(TAG, "queryRunningOS");
        Thread worker = new Thread(new Runnable() {

            @Override
            public void run() {
                if (selectedDevice != null) {
                    String http_pass = String.format("%s:%s", PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, PublicDefineGlob.DEFAULT_CAM_PWD);
                    String res = null;
                    String cmd = PublicDefineGlob.GET_RUNNING_OS;
                    if (selectedDevice.isAvailableLocally()) {
                        final String device_address_port = selectedDevice.getProfile().getDeviceLocation().getLocalIp() + ":" + selectedDevice.getProfile().getDeviceLocation().getLocalPort1();
                        String http_addr = null;
                        http_addr = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, PublicDefineGlob.HTTP_CMD_PART, cmd);
                        // // Log.d("mbp", "get running OS cmd: " + http_addr);
                        res = HTTPRequestSendRecvTask.sendRequest_block_for_response(http_addr, PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, http_pass);
                    } else {
                        cmd = PublicDefineGlob.BM_HTTP_CMD_PART + cmd;
                        // // Log.d("mbp", "get running OS cmd: " + cmd);
                        String saved_token = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                        if (saved_token != null) {
                            res = UDTRequestSendRecvTask.sendRequest_via_stun2(saved_token, selectedDevice.getProfile().getRegistrationId(), cmd
                            );
                        }
                    }

                    // // Log.d("mbp", "get running OS res: " + res);
                    if (res != null && res.startsWith(PublicDefineGlob.GET_RUNNING_OS)) {
                        res = res.substring(PublicDefineGlob.GET_RUNNING_OS.length() + 2);
                        if (res.equalsIgnoreCase("-1")) {
                            res = null;
                        } else {
                            //TODO: capabilities?
                            if (res.equalsIgnoreCase("MACOS")) {
                                shouldTurnOnPanTilt = false;
                                shouldTurnOnMelody = false;
                            } else {
                                if (selectedDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_MBP33N)) {
                                    shouldTurnOnPanTilt = false;
                                } else {
                                    shouldTurnOnPanTilt = true;
                                }

                                shouldTurnOnMelody = true;
                            }
                        }
                    }

                    /*if (context != null) {
                        if(mActivity!=null) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setupBottomMenu();
                                }
                            });
                        }

                    }*/
                } // if (selected_channel != null)
            }
        });
        worker.start();
    }

    public void updateRecordingTime(int time_in_sec) {
        if (mCaptureFragment != null && mCaptureFragment.isRecording()) {
            mCaptureFragment.updateRecordingTime(time_in_sec);
        }
    }

    private void setupFFMpegPlayer() {
        mLoadingMessageText.setText(R.string.viewfinder_progress_getstream);
        if (sessionAttributes.p2pClients != null) {
            p2pClient = sessionAttributes.p2pClients;
            filePath = sessionAttributes.filePath;
            Log.d(TAG, "filePath in setup:" + filePath);
        } else if (sessionAttributes.filePath == null) {
            Log.d(TAG, "File path is null");
        } else if (!sessionAttributes.isVideoTimeout) {
            p2pClient = null;
            filePath = sessionAttributes.filePath;
        } else {
            //todo check what needs to be done
        }

        if (liveFragmentListener != null) {
            liveFragmentListener.setupScaleDetector();
            liveFragmentListener.setupOnTouchEvent();
        }

        Log.d(TAG, "setupFFMpegPlayer");
        mIsTimeFormat12=CommonUtil.getSettingInfo(mContext,SettingsPrefUtils.TIME_FORMAT_12,true);
        TextView actionBarTitle = (TextView) view.findViewById(R.id.vf_toolbar_title);
        actionBarTitle.setText(selectedDevice.getProfile().getName());
        //if timer is already running, cancel and restart new timer
        if(mQueryDateTimeTimer!=null)
            mQueryDateTimeTimer.cancel();

        if(mQueryDateTimeTimer!=null)
            mQueryDateTimeTimer.cancel();
         mQueryDateTimeTimer= new Timer();
         mQueryDateTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double deviceTimezone=selectedDevice.getProfile().getTimeZone();
                SimpleDateFormat df ;
                if(mIsTimeFormat12){
                    df = new SimpleDateFormat("hh:mm a, dd MMM ,EEEE");
                }else{
                    df = new SimpleDateFormat("HH:mm,  dd MMM ,EEEE");
                }

                final String date=CommonUtil.getTimeStampFromTimeZone(new Date(),deviceTimezone,df);
                if(mActivity!=null && isAdded()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView dateText = (TextView) view.findViewById(R.id.date_text);
                            int index=date.indexOf(',');
                            if(index!=-1) {
                                char[] dateArray = date.toCharArray();
                                dateArray[index] = '\n';
                                String dateString = new String(dateArray);
                                dateText.setText(dateString);
                            }else{
                                dateText.setText(date);
                            }
                        }
                    });
                }

            }
        }, 0, 60000);


       /* if (selectedDevice.getProfile().doesHaveHumidity() && mSharedPreferences.contains(selectedDevice.getProfile().getName() + "humidity"))
        {
            String humidity=mSharedPreferences.getString(selectedDevice.getProfile().getName() + "humidity", "0");
            if(humidity!=null && !humidity.equals("0")) {
                mHumidityImage.setVisibility(View.VISIBLE);
                mHumidityText.setText(humidity + "%");
            }
        }*/
        if (selectedDevice.getProfile().doesHaveHumidity()) {
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {
                    final String response = CameraCommandUtils.sendCommandGetStringValue(selectedDevice, "get_humidity", null, null);
                    if (response != null) {
                        if (mActivity != null && isAdded()) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHumidityImage.setVisibility(View.VISIBLE);
                                    mHumidityText.setText(response + " %");
                                }
                            });
                        }

                    }
                }
            });


        }

        shouldEnableMic = PublicDefine.shouldEnableMic(selectedDevice.getProfile().getModelId());

        if (selectedDevice != null && selectedDevice != null && PublicDefine.isSharedCam(selectedDevice.getProfile().getModelId())) {
            queryRunningOS();
        } else {
            shouldTurnOnPanTilt = PublicDefine.shouldEnablePanTilt(selectedDevice.getProfile().getModelId());
            shouldTurnOnMelody = true;
            //setupBottomMenu();
        }

        //set temperature
        if (selectedDevice.getProfile().doesHaveTemperature()) {
            Log.d(TAG, "fetching temperature");
            mLiveActionListener.onTemperature(true);
        }

        mMovieView.setParentFragment(this);
        mMovieView.setFFMpegPlayer(mMovieView.getFFMpegPlayer());
        boolean isSurfaceValid= mMovieView.getHolder().getSurface().isValid();
        //if phone was turned off , start streaming again instead of resumeStreaming to avoid video freeze
        if (!mMovieView.isPlaying() || mIsScreenTurnedOFF || !isSurfaceValid) {
            Log.d(TAG, "Media not playing..starting stream");
            try {
                FFMpeg ffmpeg = new FFMpeg(); // Needed for JNI.
                if (p2pClient != null) {
                    mMovieView.setP2PInfo(p2pClient);
                } else {
                    mMovieView.setP2PInfo(null);
                }
                boolean isEnableTCP = false;
                if(selectedDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)==0)
                {
                    isEnableTCP = true;
                }
                mMovieView.initVideoView(new Handler(this), false, PublicDefine.isSharedCam(selectedDevice.getProfile().getModelId()),isEnableTCP);
                mMovieView.setVideoPath(filePath);

                setIsInBGMonitoring(false);
                mIsScreenTurnedOFF=false;

            } catch (FFMpegException e) {
            }
       } else {

            //if (isInBGMonitoring()) {//AA-1376
                Log.d(TAG, "media already playing,..resuming stream");
                mMovieView.resumeDisplay();
                setIsInBGMonitoring(false);
           // }

        }
        if(!mIsFragmentHidden) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

    }

    private void setupBottomMenu() {
        List<ViewFinderMenuItem> menuItems = new ArrayList<ViewFinderMenuItem>();

        setUpDefaultControlLayout();
        //commented to remove quickview in orbit
       /* if(selectedDevice.getProfile().isStandBySupported())
            menuItems.add(ViewFinderMenuItem.QUICKVIEWCONTROL);*/

        if (selectedDevice.getProfile().doesHaveTalkback() && shouldEnableMic) {
            menuItems.add(ViewFinderMenuItem.MIC);
        }

        menuItems.add(ViewFinderMenuItem.RECORD);

        if (!selectedDevice.getProfile().hasNoSpeaker() && !selectedDevice.getProfile().melodyNotSupported() && shouldTurnOnMelody) {
            menuItems.add(ViewFinderMenuItem.MELODY);
        }

        if (selectedDevice.getProfile().doesHavePanTilt() && shouldTurnOnPanTilt) {
            menuItems.add(ViewFinderMenuItem.PAN);
        }


        setUpGridView(menuItems);
    }

    private void setUpGridView(List<ViewFinderMenuItem> menuItems) {
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)
         mControlGrid.setVisibility(View.VISIBLE);
        mGridAdapter = new ViewFinderMenuAdapter(menuItems, mActivity, this);
        mControlGrid.setNumColumns(menuItems.size());
        DisplayMetrics displaymetrics = new DisplayMetrics();
        if(mActivity!=null) {
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int screenWidth = displaymetrics.widthPixels;
            mControlGrid.setColumnWidth(screenWidth / menuItems.size());
        }
        mControlGrid.setAdapter(mGridAdapter);
        
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT && selectedDevice.getProfile().getModelId().equalsIgnoreCase(PublicDefine.MODEL_ID_FOCUS72))
        {
            mGridAdapter.setSelection(0);
            mGridAdapter.notifyDataSetChanged();

        }


    }

    private void setUpDefaultControlLayout() {

        mHDImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mIsHDChanged=true;
                HD.pressed = !HD.pressed;
                if (HD.pressed) {
                    mHDImage.setImageResource(HD.pressedImage);
                    mHDText.setText(getString(R.string.viewfinder_hd_on));
                    AnalyticsInterface.getInstance().trackEvent(AppEvents.VIEW_FINDER_HD,AppEvents.VIEW_FINDER_HD_ON,eventData);
                } else {
                    mHDImage.setImageResource(HD.image);
                    mHDText.setText(getString(R.string.viewfinder_hd_off));
                    AnalyticsInterface.getInstance().trackEvent(AppEvents.VIEW_FINDER_HD,AppEvents.VIEW_FINDER_HD_OFF,eventData);
                }
                mLiveActionListener.onHD(HD.pressed);
            }
        });

        mMuteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MUTE.pressed = !MUTE.pressed;
                if (MUTE.pressed) {
                    mMuteImage.setImageResource(MUTE.pressedImage);
                    mMuteText.setText(getString(R.string.viewfinder_sound_off));
                    AnalyticsInterface.getInstance().trackEvent(AppEvents.VIEW_FINDER_MUTE,AppEvents.VIEW_FINDER_MUTE_ON,eventData);
                } else {
                    mMuteImage.setImageResource(MUTE.image);
                    mMuteText.setText(getString(R.string.viewfinder_sound_on));
                    AnalyticsInterface.getInstance().trackEvent(AppEvents.VIEW_FINDER_MUTE,AppEvents.VIEW_FINDER_MUTE_OFF,eventData);
                }
                mLiveActionListener.onAudioEnable(MUTE.pressed);

            }
        });
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT && !mIsControlFragmentVisible)
            mDefaultControlsLayout.setVisibility(View.VISIBLE);

        if(!selectedDevice.getProfile().doesSupportFullHD()){
            mHDLayout.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mMuteLayout.setLayoutParams(layoutParams);
        }else{
            if(selectedDevice.getProfile().isStandBySupported())
                mLiveActionListener.onHD(false);
        }

        boolean isMute = settings.getBoolean("mute" + selectedDevice.getProfile().getRegistrationId(), false);
        if(mIsFragmentHidden)
            checkAndMuteAudio(true);
        else {
            if (isMute) {
                mMuteImage.setImageResource(MUTE.pressedImage);
                mMuteText.setText(getString(R.string.viewfinder_sound_off));
                mLiveActionListener.onAudioEnable(true);
            } else {
                mMuteImage.setImageResource(MUTE.image);
                mMuteText.setText(getString(R.string.viewfinder_sound_on));
                mLiveActionListener.onAudioEnable(false);
            }
        }

        if(selectedDevice.getProfile().hasNoSpeaker()){
            mMuteLayout.setVisibility(View.GONE);
        }else{
            mMuteLayout.setVisibility(View.VISIBLE);
        }


      /*  if(!selectedDevice.getProfile().isStandBySupported()) {
            mDefaultControlsLayout.setVisibility(View.VISIBLE);
        }else {
            if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT) {
                mDefaultControlsLayout.setVisibility(View.GONE);
                mStreamingInfoLayout.setVisibility(View.VISIBLE);
            }
        }*/

      if(selectedDevice.getProfile().hasNoSpeaker()){
          mMuteLayout.setVisibility(View.GONE);
      }else{
          mMuteLayout.setVisibility(View.VISIBLE);
      }


    }


    private void disconnectTalkbackIfAvailable() {
        Log.d(TAG, "disconnectTalkbackIfAvailable, talkbackFragment null? ");
        //todo
        if (mTalkbackFragment != null) {
            mTalkbackFragment.disconnectTalkback();
        }
    }


    private class MiniWifiScanUpdater implements IWifiScanUpdater {
        @Override
        public void scanWasCanceled() { // Do nothing here for now
        }

        @Override
        public void updateWifiScanResult(List<ScanResult> results) {
            String check_SSID = settings.getString(sessionAttributes.string_currentSSID, null);
            String check_SSID_w_quote = "\"" + check_SSID + "\"";
            boolean found_in_range = false;
            if (results != null) {
                for (ScanResult result : results) {
                    if ((result.SSID != null) && (result.SSID.equalsIgnoreCase(check_SSID))) {
                        found_in_range = true;
                        break;
                    }
                }
            }

            // This code appears to try to connect to RTSP in AP mode
            if (found_in_range) {
                // try to connect back to this BSSID
                WifiManager w = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                List<WifiConfiguration> wcs = w.getConfiguredNetworks();

                Handler.Callback h = new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NotNull Message msg) {
                        switch (msg.what) {
                            case ConnectToNetworkTask.MSG_CONNECT_TO_NW_DONE:
                                if(mActivity!=null && isAdded()) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            selectedDevice.setIsAvailableLocally(true);
                                            prepareToViewCameraLocally();
                                        }
                                    });
                                }
                                break;
                            case ConnectToNetworkTask.MSG_CONNECT_TO_NW_FAILED:
                                if (mActivity != null && isAdded()) {
                                    videoPlaybackTasks.startWifiTask(mActivity, new MiniWifiScanUpdater());
                                }
                                break;
                        }
                        return false;
                    }
                };

                ConnectToNetworkTask connect_task = new ConnectToNetworkTask(mActivity, new Handler(h));
                connect_task.dontRemoveFailedConnection(true);
                connect_task.setIgnoreBSSID(true);
                for (WifiConfiguration wc : wcs) {
                    if ((wc.SSID != null) && wc.SSID.equalsIgnoreCase(check_SSID_w_quote)) {
                        // This task will make sure the app is connected to the
                        // camera.
                        // At the end it will send MSG_CONNECT_TO_NW_DONE
                        connect_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, wc);
                        break;
                    }
                }
            } else {
                //Scan again
                Log.d(TAG, "updateWifiScanResult, not found in range -> rescan");
                if (mActivity != null) {
                    videoPlaybackTasks.startWifiTask(mActivity, new MiniWifiScanUpdater());
                }
            }

        }
    }

    private void doUpdateLatestSnapTask() {
        Runnable runn = new Runnable() {
            @Override
            public void run() {
                if (getFFMpegMovieViewAndroid() != null) {
                    Log.i(TAG, "Start updating latest snapshot...");
                    // TODO: app should not update latest snapshot too much.
                    // Need to add a delay. E.g. just update after 5 minutes.
                    boolean isSuccess = getFFMpegMovieViewAndroid().updateLatestSnapshot(
                            selectedDevice.getProfile().getRegistrationId());
                    Log.i(TAG, "Update latest snapshot DONE, isSuccess? " + isSuccess);
                    if(isSuccess){
                        settings.putLong("snapshot"+selectedDevice.getProfile().getRegistrationId(), System.currentTimeMillis());
                    }
                }
            }
        };
        Thread worker = new Thread(runn);
        worker.start();

    }

    @Override
    public void onPan(boolean isEnable) {
        if(isEnable) {
            mPanTiltFragment = PanTiltFragment.newInstance(selectedDevice);
            mPanTiltFragment.isRTMPStreaming(sessionAttributes.viewRelayRtmp);
            switchToFragment(mPanTiltFragment);
        }else{
            removeFragment(mPanTiltFragment);
        }
        if(mSubscExpireLayout.getVisibility()== View.VISIBLE){
            mSubscExpireLayout.setVisibility(View.GONE);
            mSubscButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMic(boolean isEnable) {
        if (PublicDefine.shouldEnableMic(PublicDefine.getModelIdFromRegId(selectedDevice.getProfile().getRegistrationId()))) {
            if (isEnable) {
                AnalyticsInterface.getInstance().trackEvent("viewfinderOnMic","onMic_enable",eventData);
                mTalkbackFragment = new TalkbackFragment();
                mTalkbackFragment.setDevice(selectedDevice);
                mTalkbackFragment.setParentFragment(this);
                switchToFragment(mTalkbackFragment);
            } else {
                removeFragment(mTalkbackFragment);
               AnalyticsInterface.getInstance().trackEvent("viewfinderOnMic","onMic_disable",eventData);

            }
        }
        if(mSubscExpireLayout.getVisibility()== View.VISIBLE){
            mSubscExpireLayout.setVisibility(View.GONE);
            mSubscButton.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onRecord(boolean isEnable) {
       AnalyticsInterface.getInstance().trackEvent(AppEvents.VIEW_FINDER,AppEvents.CAMERA_BUTTON_CLICKED,eventData);
        if (isEnable) {
            if (mCaptureFragment != null) {
                sessionAttributes.recOrSnap = false; //recordingFragment.getIsRecordNotSnapshot();
                sessionAttributes.isRec = mCaptureFragment.isRecording();
                mCaptureFragment.setDevice(selectedDevice);
            }else{
                mCaptureFragment=new CaptureFragment();
                mCaptureFragment.setRecording(sessionAttributes.isRec);
                mCaptureFragment.setDevice(selectedDevice);
            }
            switchToFragment(mCaptureFragment);
        } else {
            removeFragment(mCaptureFragment);
        }
        if(mSubscExpireLayout.getVisibility()== View.VISIBLE){
            mSubscExpireLayout.setVisibility(View.GONE);
            mSubscButton.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onStorage(boolean isEnable) {

    }

    @Override
    public void onMelody(boolean isEnable) {
        //todo check melody index
        int currentMelodyIdx = -1;
        /*if (mMelodyFragment != null) {
            currentMelodyIdx = mMelodyFragment.getCurrentMelodyIdx();
        }*/

        if (isEnable) {
            mMusicFragment=new MusicFragment();
            mMusicFragment.setDevice(selectedDevice);
            switchToFragment(mMusicFragment);
        } else {
            removeFragment(mMusicFragment);
        }
        if(mSubscExpireLayout.getVisibility()== View.VISIBLE){
            mSubscExpireLayout.setVisibility(View.GONE);
            mSubscButton.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onTemperature(boolean isEnable) {
        Log.d(TAG, "onTemperature");
        //if timer is already running, cancel and restart new timer
        if(mQueryTempTimer!=null)
            mQueryTempTimer.cancel();
        mQueryTempTimer = new Timer();
        mQueryTempTimer.scheduleAtFixedRate(new QueryTemperatureTask(), 0, QUERY_TEMPERATURE_FREQUENCY);

    }

    @Override
    public void onAudioEnable(boolean isEnable) {
        Log.d(TAG, "onAudioEnabled");
        if (selectedDevice != null) {
            settings.putBoolean("mute" + selectedDevice.getProfile().getRegistrationId(), isEnable);
        }
        mMovieView.enableAudio(isEnable);
    }

    @Override
    public void onSnap() {
        String fileName=selectedDevice.getProfile().getName()+"@"+FileService.getFormatedFilePath();
        try {
            mMovieView.getSnapShot(deviceAttributes.video_width, deviceAttributes.video_height, false,fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onSettings(boolean enabled) {

    }

    @Override
    public void onZoom(boolean enabled) {

    }

    @Override
    public void onHD(final boolean enabled) {
        Log.d(TAG, "onHD");
        if (!noTriggerResolutionChanged) {

            AsyncPackage.doInBackground(new Runnable() {
               String response;
                @Override
                public void run() {
                    if (enabled) {
                        response = CameraCommandUtils.sendCommandGetStringValue(
                                selectedDevice, "set_resolution", "1080p", null);
                        if (response != null ) {
                            Log.d(TAG, "Change to 1080p result " + response);
                        }

                    } else {
                        response = CameraCommandUtils.sendCommandGetStringValue(
                                selectedDevice, "set_resolution", "720p", null);
                        if (response != null) {
                            Log.d(TAG, "Change to 720p result " + response);
                        }
                    }
                    if ("0".equals(response)) {
                        settings.putBoolean("hd" + selectedDevice.getProfile().getRegistrationId(), enabled);
                    }else{
                        if(mActivity!=null && isAdded()) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mHDImage == null || mHDImage.getVisibility() != View.VISIBLE)
                                        return;
                                    if (mIsHDChanged)
                                        Toast.makeText(mActivity, mActivity.getString(R.string.viewfinder_hd_failed), Toast.LENGTH_SHORT).show();
                                    HD.pressed = !HD.pressed;
                                    if (HD.pressed) {
                                        mHDImage.setImageResource(HD.pressedImage);
                                        mHDText.setText(mActivity.getString(R.string.viewfinder_hd_on));
                                    } else {
                                        mHDImage.setImageResource(HD.image);
                                        mHDText.setText(mActivity.getString(R.string.viewfinder_hd_off));
                                    }
                                }
                            });
                        }

                    }


                }
            });
        } else {
            noTriggerResolutionChanged = false;
        }


    }

    @Override
    public void onPreset(boolean enabled) {

    }

    @Override
    public void onMotionCalibration(boolean enabled) {

    }

    @Override
    public void onHumidity(boolean enabled) {

    }

    @Override
    public void onBTA(boolean enabled) {

    }

    private void queryTemperature(){
        AsyncPackage.doInBackground(new Runnable() {
            @Override
            public void run() {
                if (selectedDevice != null) {
                    /*final Pair<String, Object> response = selectedDevice.sendCommandGetValue("value_temperature", null, null);
                    if (response != null && response.second instanceof Float) {
                        mCurrentTemperature = (Float) response.second;
                    } else if (response != null && response.second instanceof String) {
                        try {
                            mCurrentTemperature = Float.valueOf((String) response.second);
                        } catch (NumberFormatException e) {
                            // Camera sometimes returns non-ASCII characters
                        }
                    }*/
                    final String response = CameraCommandUtils.sendCommandGetStringValue(
                            selectedDevice, "value_temperature", null, null);
                    if (response != null) {
                        try {
                            mCurrentTemperature = Float.parseFloat(response);
                        } catch (NumberFormatException e) {
                            // Camera sometimes returns non-ASCII characters
                            e.printStackTrace();
                        }
                    }

                    Log.d(TAG, "current temperature:"+mCurrentTemperature);

                    if (isAdded() && getActivity() != null && mCurrentTemperature>0) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String tempString=(int)mCurrentTemperature+"";
                                int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
                                if (savedTempUnit == PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
                                    tempString+="\u2103";
                                }else{
                                    tempString= CommonUtil.convertCtoF(mCurrentTemperature)+"";
                                    tempString+="\u2109";
                                }

                                mTempText.setText(tempString);
                            }
                        });
                    }
                }
            }
        });
    }

    private class QueryTemperatureTask extends TimerTask {
        @Override
        public void run() {
            queryTemperature();

        }
    }

    private void switchToFragment(Fragment fragment){
        mIsControlFragmentVisible=true;
        mCurrentControlFragment=fragment;
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
            mControlGrid.setVisibility(View.GONE);
            if(selectedDevice!=null && selectedDevice.getProfile().isStandBySupported()) {
                mAlertLayout.clearAnimation();
                mAlertLayout.setVisibility(View.GONE);
            }
        }
        mDefaultControlsLayout.setVisibility(View.GONE);
        mPlaybackFrame.setVisibility(View.VISIBLE);
        FragmentTransaction fragTrans = getChildFragmentManager().beginTransaction();
        try {
            fragTrans.replace(R.id.playback_frame, fragment);
            fragTrans.commitAllowingStateLoss();
        } catch (Exception e) {
            Toast.makeText(mActivity, mActivity.getString(R.string.an_error), Toast.LENGTH_SHORT).show();
            mDefaultControlsLayout.setVisibility(View.VISIBLE);
            mPlaybackFrame.setVisibility(View.GONE);
        }
    }

    public void removeFragment(Fragment fragment){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragment != null) {
            fragmentTransaction.remove(fragment);
        }
        fragmentTransaction.commitAllowingStateLoss();
        mDefaultControlsLayout.setVisibility(View.VISIBLE);
        mPlaybackFrame.setVisibility(View.GONE);
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
            mControlGrid.setVisibility(View.VISIBLE);
            if(!isDeviceCharging && selectedDevice!=null && selectedDevice.getProfile().isStandBySupported())
                mAlertLayout.setVisibility(View.VISIBLE);
        }
        mGridAdapter.resetSelection();
        mGridAdapter.notifyDataSetChanged();
        mIsControlFragmentVisible=false;
        mCurrentControlFragment=null;
    }

    public void refreshCount() {
        fetchEventForCamera();
    }

	private synchronized void fetchEventForCamera() {
		if (!isEventFetchInProgress && mDeviceManagerService != null && mDeviceEvent != null) {
			isEventFetchInProgress = true;
			mDeviceEvent.setPage(0);
            if(mActivity != null) {
                String startDay = CommonUtil.getEventReadTimeFromSP(mActivity,
                        selectedDevice.getProfile().getRegistrationId());
                if(!startDay.isEmpty()){
                    String timezoneString = CommonUtil.getCameraTimeZone(selectedDevice.getProfile().getTimeZone());
                    TimeZone timeZone = TimeZone.getTimeZone(timezoneString);
                    Calendar calendar = Calendar.getInstance(timeZone);
                    Date endDate = calendar.getTime();
                    String endDay = utcFormat.format(endDate);
                    Log.d(TAG,"For event count startDay "+startDay +" endDay "+endDay);
                    mDeviceEvent.setAfterStartTime(startDay);
                    mDeviceEvent.setBeforeStartTime(endDay);
                }
            }
            mDeviceManagerService.getDeviceEvent(mDeviceEvent, new Response.Listener<DeviceEventDetail>() {
                @Override
                public void onResponse(DeviceEventDetail response) {
                    if(mActivity==null || !isAdded())
                        return;
                    isEventFetchInProgress = false;
                    if (response != null) {
                        int count = response.getTotalEvents() - 1;
                        if (count > 0) {
                            mEventCount.setVisibility(View.VISIBLE);
                            mEventCount.setText(String.valueOf(count));
                        } else {
                            mEventCount.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        mEventCount.setVisibility(View.INVISIBLE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(mActivity==null || !isAdded())
                        return;
                    isEventFetchInProgress = false;
                    mEventCount.setVisibility(View.INVISIBLE);
                }
            });
		}
	}

	private void initDeviceManager() {
        String savedAccessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
		//Device Event Instance Setup
		if (selectedDevice != null && mActivity != null) {
            if (mDeviceManagerService == null) {
                mDeviceManagerService = DeviceManagerService.getInstance(mActivity);
            }
            setUpDeviceEvent(savedAccessToken);
		}
	}

    private void setUpDeviceEvent(String savedAcsessToken){
        mDeviceEvent = new DeviceEvent(savedAcsessToken, selectedDevice.getProfile().registrationId);
        mDeviceEvent.setSize(CommonConstants.EVENT_PAGE_SIZE);
    }


    public void setRecord(boolean enable, final boolean isPhoneStorage) {
        final String recordFileName=FileService.getFormatedFilePathForVideo(selectedDevice.getProfile().getName()).getAbsolutePath();
        if (enable) {
            Log.i(TAG, "Record file name: " + recordFileName);
            mMovieView.startRecord(true, isPhoneStorage, selectedDevice, recordFileName, this);
            if(mActivity!=null && isAdded()) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //restrict orientation in recording mode
                        if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        else
                            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                        mLiveStatusView.setBackground(getResources().getDrawable(R.drawable.viewfinder_record_bg));
                        mLiveStatusView.setText(mActivity.getString(R.string.viewfinder_rec));


                    }
                });
            }

        } else {
            mMovieView.startRecord(false, isPhoneStorage, selectedDevice, recordFileName, this);
            if(mActivity!=null && isAdded()) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //enable orientation once recording is completed
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        mLiveStatusView.setBackground(mActivity.getResources().getDrawable(R.drawable.viewfinder_live_bg));
                        mLiveStatusView.setText(mActivity.getString(R.string.viewfinder_live));
                        File mFile = new File(recordFileName);
                        if (mFile.length() / 1024 > 70)//Download only if video size is greater than 70kb

                        {
                            if (isPhoneStorage) {
                                Toast.makeText(mActivity, mActivity.getString(R.string.saved_video), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mActivity, mActivity.getString(R.string.saved_video_to_camera), Toast.LENGTH_SHORT).show();
                            }
                        } else

                        {
                            if (mFile.exists()) {
                                mFile.delete();
                            }

                            Toast.makeText(mActivity, mActivity.getString(R.string.recording_fail), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

    }

    public void showDirection(Direction direction){
        ImageView directionView=(ImageView)view.findViewById(R.id.direction_image);
        directionView.setVisibility(View.VISIBLE);
        switch(direction){
            case up:
                directionView.setImageResource(R.drawable.top);
                break;
            case down:
                directionView.setImageResource(R.drawable.bottom);
                break;
            case left:
                directionView.setImageResource(R.drawable.left);
                break;
            case right:
                directionView.setImageResource(R.drawable.right);
                break;
            default:
                directionView.setVisibility(View.GONE);
                break;

        }
    }

    public void removeDirectionView(){
        ImageView directionView=(ImageView)view.findViewById(R.id.direction_image);
        directionView.setVisibility(View.GONE);
    }


    private void stopRecording() {
        if (mCaptureFragment != null && mCaptureFragment.isRecording()) {
            Log.i(TAG, "Recording is in progress, stop it now");
            setRecord(false, true);
        }
    }

    private void stopPlaying(){
        if(mMusicFragment!=null){
            mMusicFragment.stopPlaying();
        }
    }

    private void handleNotificationToggle(final boolean isChecked){
        if  (!isChecked &&  !CommonUtil.getSettingInfo(mActivity, selectedDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.DONT_SHOW_PRIVACY_DIALOG)) {
            PrivacyCustomDialog privacyDailog = new PrivacyCustomDialog(mActivity, new PrivacyCustomDialog.PrivacyListener() {
                @Override
                public void onPrivacyConfirmClick() {
                        displayProgressDialog();
                        NotificationStatusTask notificationChange = new NotificationStatusTask(selectedDevice, -1, false, mActivity, mDeviceHandler);
                        notificationChange.execute();

                      /*  ChangePrivacyMode changePrivacyMode = new ChangePrivacyMode(selectedDevice, -1, true, mActivity, mDeviceHandler);
                        changePrivacyMode.execute();*/

                        // holder.notificationSwitchON.setTextColor(activity.getResources().getColor(R.color.text_gray));

                }

                @Override
                public void doNotShowDailog(boolean isChecked) {
                    CommonUtil.setSettingInfo(mActivity, selectedDevice.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.DONT_SHOW_PRIVACY_DIALOG, isChecked);
                }

                @Override
                public void onPrivacyCancel() {
                    mDeviceOnOffSwitch.setOnCheckedChangeListener(null);
                    mDeviceOnOffSwitch.setChecked(true);
                    mDeviceOnOffSwitch.setOnCheckedChangeListener(ViewFinderFragment.this);
                }
            });
            privacyDailog.show();
        }else {
            displayProgressDialog();
            if (isChecked) {
                NotificationStatusTask notificationChange = new NotificationStatusTask(selectedDevice, -1, true, mActivity, mDeviceHandler);
                notificationChange.execute();

               /* ChangePrivacyMode changePrivacyMode = new ChangePrivacyMode(selectedDevice, -1, false, mActivity, mDeviceHandler);
                changePrivacyMode.execute();*/
            } else {
                NotificationStatusTask notificationChange = new NotificationStatusTask(selectedDevice, -1, false, mActivity, mDeviceHandler);
                notificationChange.execute();

             /*   ChangePrivacyMode changePrivacyMode = new ChangePrivacyMode(selectedDevice, -1, true, mActivity, mDeviceHandler);
                changePrivacyMode.execute();*/
            }
        }
    }

    private void  checkDeviceStatus()
    {
        loadLatestCameraSnap();
        showSpinner(getString(R.string.viewfinder_progress_check_device_status));

        if(BuildConfig.DEBUG)
            Log.d(TAG, "checkDeviceStatus");

        AsyncPackage.doInBackground(new Runnable()
        {
            @Override
            public void run()
            {

                // when device is on stand by mode then first condition is failed so
                // it is failed to start streaming in local mode sometime based on condition.
                // now, that's reason it is required that application should verify local
                // connectivity once again.
                // This is going to increase remote timeout by 10 second during WiFi network.

                if(false) // && isCameraInSameNetwork())
                {
                    if(mActivity != null && isAdded()) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                scanAndViewCamera();
                            }
                        });

                    }
                }
                else
                {

                    // reset flag, as device may be running in stand by mode.
                    isLocalVerify = false;

                    final DeviceStatus deviceStatus = new DeviceStatus(accessToken, selectedDevice.getProfile().getRegistrationId());

                    DeviceManagerService.getInstance(getActivity()).getDeviceStatus(deviceStatus, new Response.Listener<StatusDetails>()
                            {
                                @Override
                                public void onResponse(StatusDetails response) {
                                    if (getActivity() == null || !isAdded()) {
                                        return;
                                    }
                                    //dismissDialog();
                                    if (response != null) {
                                        StatusDetails.StatusResponse[] statusResponseList = response.getDeviceStatusResponse();

                                        StatusDetails.StatusResponse statusResponse = null;

                                        if (statusResponseList != null && statusResponseList.length > 0) {
                                            statusResponse = statusResponseList[0]; // fetch first object only
                                        }


                                        if (statusResponse != null) {
                                            StatusDetails.DeviceStatusResponse deviceStatusResponse = statusResponse.getDeviceStatusResponse();
                                            final String deviceStatus = deviceStatusResponse.getDeviceStatus();

                                            if(BuildConfig.DEBUG)
                                                Log.d(TAG, "device status :- " + deviceStatus);

                                            if(mActivity!=null && isAdded()) {
                                                mActivity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (deviceStatus != null) {
                                                            if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0) {
                                                                selectedDevice.getProfile().setAvailable(true);
                                                                selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_ONLINE);

                                                                hideSpinner(false);
                                                                //start live streaming
                                                                scanAndViewCamera();
                                                            } else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_STANDBY) == 0) {
                                                                selectedDevice.getProfile().setAvailable(false);
                                                                selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_STANDBY);

                                                                hideSpinner(false);
                                                                //wakeup device
                                                                wakeUpRemoteDevice();
                                                            } else if (deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_OFFLINE) == 0) {
                                                                selectedDevice.getProfile().setAvailable(false);
                                                                selectedDevice.getProfile().setDeviceStatus(CameraStatusView.DEVICE_STATUS_OFFLINE);
                                                                //device offline
                                                                Toast.makeText(mActivity, getActivity().getString(R.string.camera_offline), Toast.LENGTH_SHORT).show();
                                                                hideSpinner(false);
                                                                mErrorLayout.setVisibility(View.VISIBLE);
                                                            }
                                                        }
                                                    }
                                                });
                                            }

                                        }
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (mActivity == null || !isAdded()) {
                                        return;
                                    }
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dismissDialog();
                                        }
                                    });
                                    if (error != null && error.networkResponse != null) {
                                        Log.d(TAG, error.networkResponse.toString());
                                        Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
                                    }
                                }
                            });
                }

            }
        });



    }

    public void showGallery(){
        stopFFMPegPlayer();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_DIRECTLY_TO_GALLERY, true);
        startActivity(intent);
        mActivity.finish();

    }

    private String isInLocalString() {
        return isLocalStreaming() ? "L" : "R";
    }

    private String getCurrentConnectionModeLetter() {
        String modeStr = "";
        switch (deviceAttributes.currentConnectionMode) {
            case HubbleSessionManager.CONNECTION_MODE_RTSP_LOCAL:
                modeStr = "L";
                break;
            case HubbleSessionManager.CONNECTION_MODE_RTMP_REMOTE:
                modeStr = "R";
                break;
            case HubbleSessionManager.CONNECTION_MODE_P2P_LOCAL:
            case HubbleSessionManager.CONNECTION_MODE_P2P_REMOTE:
            case HubbleSessionManager.CONNECTION_MODE_P2P_RELAY:
                modeStr = P2pManager.getInstance().getCurrConnectionMode();
                break;
            default:
                modeStr = "";
                break;
        }
        return modeStr;
    }

    private void updateFPS(final int fps) {

        if (mActivity != null && isAdded()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mFrameRateText!=null && mFrameRateText.getVisibility() == View.VISIBLE) {
                        String fps_str;
                        if (P2pSettingUtils.hasP2pFeature()) {
                            fps_str = String.format("%s %d", getCurrentConnectionModeLetter(), fps);
                        } else {
                            fps_str = String.format("%s %d", isInLocalString(), fps);
                        }
                        mFrameRateText.setText(fps_str);
                    }
                }
            });
        }

    }

    private void updateDebugBitrateDisplay(int data_in_bytes) {
        final int data_in_kb = data_in_bytes * 8 / 1000;
        if (mActivity != null && isAdded()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBitRateText != null && mBitRateText.getVisibility() == View.VISIBLE) {
                        String bitrate_str = String.format("%d %s", data_in_kb, "kbps");
                        mBitRateText.setText(bitrate_str);
                    }
                }
            });
        }
    }

    private void updateGlobalBitrate(final int bitrate) {
        if(mActivity!=null && isAdded()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mGlobalBitRateText != null && mGlobalBitRateText.getVisibility()==View.VISIBLE) {
                        String bitrate_str = String.format("%d %s", bitrate, "kbps");
                        mGlobalBitRateText.setText(bitrate_str);
                    }
                }
            });
        }
    }

    private void doGetWifiStrengthTask() {
        Runnable runn = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Getting wifi signal strength ...");
                // error_in_control_command, get_wifi_strength
                int wifiStrength = -1;
                boolean isInLocal = false;
                if (getActivity() != null) {
                    isInLocal = ((ViewFinderActivity)getActivity()).isStreamingViaLocal();
                }

                // Don't send get_wifi_strength via P2P, it could cause block P2P client from terminating.
               // String res = CommandUtils.sendCommand(selectedDevice, "get_wifi_strength", isInLocal, false);
                final String res = CameraCommandUtils.sendCommandGetStringValue(
                        selectedDevice, "get_wifi_strength", null, null);

                if (res != null) {
                    try {
                        wifiStrength = Integer.parseInt(res);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(TAG, "Get wifi signal strength failed, res: " + res);
                }
                Log.i(TAG, "Get wifi signal strength DONE, result: " + wifiStrength);

                final int wifiStrengthFinal = wifiStrength;
        /* 20151031: hoang: crash due to run callback when activity has been destroyed.
         * IMPORTANT: This is a callback method, which its task needs to be runOnUiThread.
         * REMEMBER: all callback method need to check whether activity is null before runOnUiThread
         * because when the background task is completed, the activity could have been destroyed.
         */
                if (mActivity != null && isAdded()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mWifiSignalText!=null) {
                                mWifiSignalText.setVisibility(View.VISIBLE);
                                if (selectedDevice != null && selectedDevice.getProfile().doesHaveLANSetup()) {
                                    if (wifiStrengthFinal == 101 ||
                                            wifiStrengthFinal == 0) {
                                        mWifiSignalText.setText(mActivity.getString(R.string.lan_connection));
                                        mConnectionTypeImg.setVisibility(View.GONE);
                                    } else if (wifiStrengthFinal > 0) {
                                        mWifiSignalText.setText(String.valueOf(wifiStrengthFinal) + "%");
                                        mConnectionTypeImg.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    if (wifiStrengthFinal > 0) {
                                        mWifiSignalText.setText(String.valueOf(wifiStrengthFinal) + "%");
                                        mConnectionTypeImg.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                        }
                    });
                }
            }
        };
        Thread worker = new Thread(runn);
        worker.start();
    }


    private void getBatteryMode()
    {
        Runnable runnableBatteryMode = new Runnable()
        {
            @Override
            public void run()
            {
                float deviceMode = -1;
                float batteryValue = -1;


                boolean isInLocal = false;

                if (getActivity() != null)
                {
                    isInLocal = ((ViewFinderActivity)getActivity()).isStreamingViaLocal();
                }
                String res = CommandUtils.sendCommand(selectedDevice, PublicDefine.GET_DEVICE_MODE, isInLocal);

                if(BuildConfig.DEBUG)
                    Log.d(TAG,"battery mode:- " + res);

                if (res != null && res.startsWith(PublicDefine.GET_DEVICE_MODE))
                {

                    try
                    {
                        final Pair<String, Object> parsedResponse= CommonUtil.parseResponseBody(res);
                        if (parsedResponse != null && parsedResponse.second instanceof Float)
                        {
                            deviceMode = (Float) parsedResponse.second;
                        }
                        else if (parsedResponse != null && parsedResponse.second instanceof String)
                        {
                            try
                            {
                                deviceMode = Float.valueOf((String) parsedResponse.second);
                            }
                            catch (NumberFormatException e)
                            {
                                Log.e(TAG,e.getMessage());
                                deviceMode = -1;
                            }
                        }
                        else if(parsedResponse != null && parsedResponse.second instanceof Integer)
                        {
                            deviceMode = (Integer) parsedResponse.second;
                        }

                    }
                    catch (Exception exception)
                    {
                        Log.d(TAG, exception.getMessage());
                        exception.printStackTrace();


                    }
                }


                if (getActivity() != null)
                {
                    final int batteryStatus = (int)deviceMode;
                    if(batteryStatus == CameraStatusView.ORBIT_BATTERY_CHARGING)
                    {
                        isAllowFirmwareUpgrade = true;
                        isDeviceCharging = true;
                    }
                    else
                    {

                        isAllowFirmwareUpgrade = false;

                        String batteryResponse = CommandUtils.sendCommand(selectedDevice, PublicDefine.GET_BATTERY_VALUE, isInLocal);

                        if (batteryResponse != null && batteryResponse.startsWith(PublicDefine.GET_BATTERY_VALUE))
                        {

                            try
                            {
                                final Pair<String, Object> parsedResponse= CommonUtil.parseResponseBody(batteryResponse);
                                if (parsedResponse != null && parsedResponse.second instanceof Float)
                                {
                                    batteryValue = (Float) parsedResponse.second;
                                }
                                else if (parsedResponse != null && parsedResponse.second instanceof String)
                                {
                                    try
                                    {
                                        batteryValue = Float.valueOf((String) parsedResponse.second);
                                    }
                                    catch (NumberFormatException e)
                                    {
                                        Log.e(TAG,e.getMessage());
                                        batteryValue = -1;
                                    }
                                }
                                else if(parsedResponse != null && parsedResponse.second instanceof Integer)
                                {
                                    batteryValue = (Integer) parsedResponse.second;
                                }

                            }
                            catch (Exception exception)
                            {
                                Log.d(TAG, exception.getMessage());
                                exception.printStackTrace();


                            }

                            if(batteryValue <= PublicDefine.ORBIT_MINIMUM_BATTERY_LEVEL)
                            {
                                isAllowFirmwareUpgrade = false;
                            }
                            else
                            {
                                isAllowFirmwareUpgrade = true;
                            }
                        }
                    }

                    if(mActivity!=null && isAdded()) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (BuildConfig.DEBUG)
                                    Log.d(TAG, "batteryStatus :- " + batteryStatus);

                                if (batteryStatus == CameraStatusView.ORBIT_BATTERY_CHARGING) {
                                    mAlertLayout.setVisibility(View.GONE);
                                    mAlertLayout.clearAnimation();
                                } else {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (mActivity != null && isAdded() && !deviceAttributes.activity_has_stopped) {
                                                mAlertLayout.setVisibility(View.VISIBLE);

                                                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                                                animation.setDuration(1200);
                                                animation.setStartOffset(20);
                                                animation.setRepeatMode(Animation.REVERSE);
                                                animation.setRepeatCount(Animation.INFINITE);
                                                mAlertLayout.startAnimation(animation);
                                            } else {
                                                Log.i(TAG, "getView null, don't need to start handler");
                                            }
                                        }
                                    }, STAND_BY_VIDEO_TIMEOUT);
                                }

                                if (BuildConfig.DEBUG)
                                    Log.d(TAG, "isAllowFirmwareUpgrade :- " + isAllowFirmwareUpgrade);

                                // Firmware upgrade is allowed only when device is runnign with local streaming.
                                if (isAllowFirmwareUpgrade && (Util.isThisVersionGreaterThan(selectedDevice.getProfile().getFirmwareVersion(), CheckFirmwareUpdateTask.ORBIT_NEW_FIRMWARE_WORK_FLOW)
                                        || isLocalStreaming())) {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (mActivity!= null && isAdded() && !deviceAttributes.activity_has_stopped) {
                                                doCheckFwUpgradeTask();
                                            }
                                        }
                                    });
                                }

                            }

                        });
                    }

                }
            }
        };
        Thread worker = new Thread(runnableBatteryMode);
        worker.start();
    }


    private void getRemainingTime(final boolean startTimer)
    {
        if(BuildConfig.DEBUG)
            Log.d(TAG, "getRemainingTime :- " + startTimer);

        Runnable runnableRemainingTime = new Runnable()
        {
            @Override
            public void run()
            {
                float remainingTime = -1;

                boolean isInLocal = false;

                String res = CommandUtils.sendCommand(selectedDevice, PublicDefine.GET_REMAINING_TIME, false);

                if(BuildConfig.DEBUG)
                    Log.d(TAG,"remaining time :- " + res);

                if (res != null && res.startsWith(PublicDefine.GET_REMAINING_TIME))
                {
                    try
                    {
                        final Pair<String, Object> parsedResponse= CommonUtil.parseResponseBody(res);
                        if (parsedResponse != null && parsedResponse.second instanceof Float)
                        {
                            remainingTime = (Float) parsedResponse.second;
                        }
                        else if (parsedResponse != null && parsedResponse.second instanceof String)
                        {
                            try
                            {
                                remainingTime = Float.valueOf((String) parsedResponse.second);
                            }
                            catch (NumberFormatException e)
                            {
                                Log.e(TAG,e.getMessage());
                                remainingTime = -1;
                            }
                        }
                        else if(parsedResponse != null && parsedResponse.second instanceof Integer)
                        {
                            remainingTime = (Integer) parsedResponse.second;
                        }

                    }
                    catch (Exception exception)
                    {
                        Log.d(TAG, exception.getMessage());
                        exception.printStackTrace();
                    }
                }


                if (mActivity != null && isAdded())
                {
                    if(BuildConfig.DEBUG)
                        Log.d(TAG, "RemainingTime :- " + remainingTime);

                    final int time = (int)remainingTime;

                    mActivity.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(time == -1)
                            {
                                onViewCameraFailedStandBy(false,time);
                            }
                            else if(!startTimer && time != 0 && time < PublicDefine.GET_REMAINING_TIME_OUT)
                            {
                                onViewCameraFailedStandBy(true,time);
                            }
                            else if(time != 0)
                            {
                                if(startTimer)
                                {
                                    if(selectedDevice.getProfile().isStandBySupported())
                                    {
                                        videoPlaybackTasks.initStandByVideoTimer();
                                        if(time > WARNING_TIMEOUT)
                                        {
                                            videoPlaybackTasks.scheduleStandByVideoTimerTask(new StandByTimeoutTask((WARNING_TIMEOUT)), (time -WARNING_TIMEOUT) * 1000);
                                        }
                                        else
                                        {
                                            // you should display dialog box asap as device is going to enter into stand by mode in 30 seconds
                                            videoPlaybackTasks.scheduleStandByVideoTimerTask(new StandByTimeoutTask((time - 1)), (time - 1) * 1000);
                                        }
                                    }
                                }
                            }
                        }

                    });
                }
            }
        };
        Thread worker = new Thread(runnableRemainingTime);
        worker.start();
    }

    private void doCheckFwUpgradeTask()
    {
        if(isAllowFirmwareUpgrade)
        {
            String fwVersion = selectedDevice.getProfile().getFirmwareVersion();
            String regId = selectedDevice.getProfile().getRegistrationId();
            String modelId = selectedDevice.getProfile().getModelId();
            String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

            boolean deviceOTA = false;
            if(selectedDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)==0)
            {
                if(Util.isThisVersionGreaterThan(selectedDevice.getProfile().getFirmwareVersion(),CheckFirmwareUpdateTask.ORBIT_NEW_FIRMWARE_WORK_FLOW))
                {
                    deviceOTA = true;
                }
            }

            if(BuildConfig.DEBUG)
                Log.d(TAG,"deviceOTA : " + deviceOTA);

            new CheckFirmwareUpdateTask(saved_token, regId, fwVersion, modelId, selectedDevice, new IAsyncTaskCommonHandler() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void onPostExecute(final Object result)
            {
                if (result instanceof CheckFirmwareUpdateResult)
                {
                    CheckFirmwareUpdateResult checkFirmwareUpdateResult = (CheckFirmwareUpdateResult) result;
                    checkFirmwareUpdateResult.setLocalCamera(true);
                    checkFirmwareUpdateResult.setInetAddress(selectedDevice.getProfile().getDeviceLocation().getLocalIp());
                    checkFirmwareUpdateResult.setApiKey(getApiKey());
                    checkFirmwareUpdateResult.setRegID(selectedDevice.getProfile().getRegistrationId());
                    handleCheckFwUpdateResult(checkFirmwareUpdateResult);
                }
            }

            @Override
            public void onCancelled() {
            }
        }, settings.getBoolean(DebugFragment.PREFS_USE_DEV_OTA, false),deviceOTA).execute();
        }

    }

    private void handleCheckFwUpdateResult(CheckFirmwareUpdateResult checkFirmwareUpdateResult)
    {

        if(mActivity != null && isAdded()) {
            dismissFWUpgradeProgressDialog();
            mCheckFirmwareUpdateResult = checkFirmwareUpdateResult;
            if (mOtaAvailableIv != null && mCheckFirmwareUpdateResult != null && mCheckFirmwareUpdateResult.isHaveNewFirmwareVersion()) {
                if (mContext != null) {
                    CommonUtil.setSettingInfo(mContext, mCheckFirmwareUpdateResult.getRegID() + "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, true);
                }
                mOtaAvailableIv.setVisibility(View.VISIBLE);


                if (selectedDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) == 0) {
                    if (shouldStartOTAProcess()) {
                        showOTAUpdateDialog(true);
                    } else {
                        showOTAUpdateDialog(false);
                    }
                } else {
                    if (!mIsFromOta) {
                        mNewFirmwareVersion = mCheckFirmwareUpdateResult.getNewFirmwareVersion();
                        ((ViewFinderActivity) getActivity()).startOtaActivity(selectedDevice, mCheckFirmwareUpdateResult);
                    }
                }
            } else {
                if (mContext != null) {
                    CommonUtil.setSettingInfo(mContext, mCheckFirmwareUpdateResult.getRegID() + "-" + SettingsPrefUtils.PREFS_NEW_FIRMWARE_AVAILABLE, false);
                }
                mOtaAvailableIv.setVisibility(View.GONE);
            }
            mIsFromOta = false;
        }

    }

    private boolean shouldStartOTAProcess()
    {
        int counter = settings.getInt(selectedDevice.getProfile().getRegistrationId()+ PublicDefine.ORBIT_OTA_COUNTER,0);
        if(counter < PublicDefine.ORBIT_OTA_COUNTER_MAX)
        {
            settings.putInt(selectedDevice.getProfile().getRegistrationId()+PublicDefine.ORBIT_OTA_COUNTER,++counter);
            return false;
        }
        else
        {
           return true;
        }
    }

    private void showOTAUpdateDialog(final boolean force)
    {
        if(mContext != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setTitle(getResources().getString(R.string.ota_update_available));
            builder.setMessage(getResources().getString(R.string.ota_update_available_msg));
            builder.setCancelable(false);

            builder.setPositiveButton(getResources().getString(R.string.update_now),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NotNull DialogInterface dialog, int which) {
                            dialog.dismiss();
                            showOtaInstructionDialog(force);

                        }
                    });

            if (!force) {
                builder.setNegativeButton(getResources().getString(R.string.later),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(@NotNull DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });
            }


            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setTextColor(getResources().getColor(R.color.text_blue));
            Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(getResources().getColor(R.color.text_blue));
        }
    }

    private void showOtaInstructionDialog(boolean force)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getResources().getString(R.string.ota_update_available));
        builder.setMessage(getResources().getString(R.string.ota_update_intruction_msg));
        builder.setCancelable(false);

        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(@NotNull DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        // start ota activity to get result for orbit
                        if(selectedDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT)==0) {
                            mNewFirmwareVersion = mCheckFirmwareUpdateResult.getNewFirmwareVersion();
                            ((ViewFinderActivity) getActivity()).startOtaActivity(selectedDevice, mCheckFirmwareUpdateResult);
                        }else {
                            mFirmwareUgradeProgressDialog = ProgressDialog.show(mActivity, null, getString(R.string.ota_info_in_progress));
                            isAllowFirmwareUpgrade = true;
                            doCheckFwUpgradeTask();
                        }

                    }
                });

        if(!force) {
            builder.setNegativeButton(getResources().getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (@NotNull DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    });
        }



        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(getResources().getColor(R.color.text_blue));
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(getResources().getColor(R.color.text_blue));

    }

    private void showLocalCameraInfoForOTA()
    {
        if(mContext != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setTitle(getResources().getString(R.string.ota_update_available));
            builder.setMessage(getResources().getString(R.string.ota_update_local_info));
            builder.setCancelable(false);

            builder.setPositiveButton(getResources().getString(R.string.summary_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NotNull DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(getResources().getColor(R.color.text_blue));
        }
    }

    private void dismissVideoAnalyticsDialog(){
        if (mVideoAnalyticsOfferDialog != null && mVideoAnalyticsOfferDialog.isShowing()) {
            mVideoAnalyticsOfferDialog.dismiss();
        }
    }
    private void dismissEnableProgressDialog() {
        if (mEnableProgressDialog != null && mEnableProgressDialog.isShowing()) {
            mEnableProgressDialog.dismiss();
        }
        mEnableProgressDialog = null;
    }

    private void dismissFWUpgradeProgressDialog(){
        if (mFirmwareUgradeProgressDialog != null && mFirmwareUgradeProgressDialog.isShowing()) {
            mFirmwareUgradeProgressDialog.dismiss();
        }
        mFirmwareUgradeProgressDialog = null;
    }

    public void checkAndMuteAudio(boolean enable){
        if(selectedDevice != null) {
            boolean isMuted = settings.getBoolean("mute" + selectedDevice.getProfile().getRegistrationId(), false);
            if (enable && !isMuted) {
                muteAudio(true);
            } else if (!enable && !isMuted) {
                muteAudio(false);
            }
        }
    }

    private void setDeviceList(){
        mDevices.clear();
        List<Device> allDevices= DeviceSingleton.getInstance().getDevices();
        for(Device device:allDevices){
            if (!device.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR) && TextUtils.isEmpty(device.getProfile().getParentId())) {
                mDevices.add(device);

            }
        }

        if(selectedDevice!=null){
            if(mDevices!=null && mDevices.size()>1) {
                mDeviceText.setVisibility(View.GONE);
                mDeviceSpinner.setVisibility(View.VISIBLE);
                mSpinnerAdapter = new ArrayAdapter(mActivity, R.layout.view_finder_camera_spinner, R.id.camera_list_item);
                mSpinnerAdapter.setDropDownViewResource(R.layout.view_finder_camera_spinner);
                mDeviceSpinner.setAdapter(mSpinnerAdapter);

                mSelectedPosition = 0;
                int position = 0;
                for (Device d : mDevices) {
                    //if (d.getProfile().isAvailable()){
                    mSpinnerAdapter.add(d.getProfile().name);
                    if (selectedDevice.getProfile().registrationId.equals(d.getProfile().registrationId)) {
                        mSelectedPosition = position;
                    }
                    position++;
                    // }
                }

                mSpinnerAdapter.notifyDataSetChanged();
                mDeviceSpinner.setSelection(mSelectedPosition);

                mDeviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(TAG, "onItemSelected");
                        if (mViewFinderCallback != null && position!=mSelectedPosition) {
                            Log.d(TAG, "camera changed");
                            mSelectedPosition=position;
                            //mDeviceOnOffSwitch.setOnCheckedChangeListener(null);
                            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_SWITCH_CAMERA_CLICKED,AppEvents.SWITCH_CAMERA);

                            ZaiusEvent switchCameraEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                            switchCameraEvt.action(AppEvents.VF_SWITCH_CAMERA_CLICKED);
                            try {
                                ZaiusEventManager.getInstance().trackCustomEvent(switchCameraEvt);
                            } catch (ZaiusException e) {
                                e.printStackTrace();
                            }
                            mViewFinderCallback.onCameraChanged(mDevices.get(position));

                            setUpDeviceEvent(HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, ""));
                            fetchEventForCamera();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }else{
                mDeviceText.setVisibility(View.VISIBLE);
                mDeviceSpinner.setVisibility(View.GONE);
                mDeviceText.setText(selectedDevice.getProfile().name);
            }
        }

    }

    public void checkFirmwareVersion() {
        if (selectedDevice != null && selectedDevice.getProfile().getModelId().compareToIgnoreCase(PublicDefine.MODEL_ID_ORBIT) != 0) {
            if (mNewFirmwareVersion != null) {
                selectedDevice.getProfile().setFirmwareVersion(mNewFirmwareVersion);
            }
            mIsFromOta = true;
            isAllowFirmwareUpgrade = true;
            doCheckFwUpgradeTask();
        }

    }

    private void checkFreeTrialStatus() {
        mSubscExpireLayout.setVisibility(View.GONE);

        mIsSubscLayoutVisible = false;

        if (selectedDevice != null && selectedDevice.getProfile().getDeviceFreeTrial() != null) {
            String planId = selectedDevice.getProfile().getPlanId();
            boolean isFreeTrialActive = selectedDevice.getProfile().getDeviceFreeTrial() != null
                    && PlanFragment.ACTIVE.equals(selectedDevice.getProfile().getDeviceFreeTrial().getStatus());
            boolean isFreeTrialExpired = selectedDevice.getProfile().getDeviceFreeTrial() != null
                    && PlanFragment.EXPIRE.equals(selectedDevice.getProfile().getDeviceFreeTrial().getStatus());
            if (isFreeTrialActive) {
                int freeTrialDays = selectedDevice.getProfile().getPendingFreeTrialDays();
                if(freeTrialDays > 0 && freeTrialDays < 4 ) {
                    mSubscButton.setVisibility(View.VISIBLE);
                    mSubscDaysLeft.setText(getString(R.string.free_trial_expiry_detail,freeTrialDays));
                    mSubscDetail.setVisibility(View.VISIBLE);
                    mIsSubscLayoutVisible = true;
                }
            } else if (planId == null || planId.isEmpty() || PublicDefineGlob.FREEMIUM.equals(planId)) {
                if (isFreeTrialExpired || selectedDevice.getProfile().getFreeTrialQuota() < 1) {
                    mSubscButton.setVisibility(View.VISIBLE);
                    mSubscDaysLeft.setText(getString(R.string.plan_free_trial_expired));
                    mSubscDetail.setVisibility(View.INVISIBLE);
                    mIsSubscLayoutVisible = true;
                } else {
                    mSubscButton.setVisibility(View.GONE);
                }
            }
        } else {
            mSubscButton.setVisibility(View.GONE);
        }

    }

    private void checkUserSubscription() {
        if(mContext != null) {
            HubbleRequest hubbleRequest = new HubbleRequest(accessToken);
            SubscriptionService subscriptionService = SubscriptionService.getInstance(mContext);
            subscriptionService.getUserSubscriptionPlan(SubscriptionInfo.ServicePlan.MONITORING_SERVICE_PLAN, hubbleRequest, new com.android.volley.Response.Listener<UserSubscriptionPlanResponse>() {
                @Override
                public void onResponse(UserSubscriptionPlanResponse response) {
                    if(mContext != null && mActivity!=null && isAdded()) {
                        if (response == null || response.getStatus() != 200) {
                            mSubscButton.setVisibility(View.GONE);
                        } else {
                            UserSubscriptionPlanResponse.PlanResponse[] userSubscriptions = response.getPlanResponse();
                            String userPlan = null;
                            if (userSubscriptions != null && userSubscriptions.length > 0) {
                                for (UserSubscriptionPlanResponse.PlanResponse item : userSubscriptions) {
                                    if (PlanFragment.ACTIVE.equals(item.getPlanState()) || PlanFragment.CANCELED.equals(item.getPlanState())) {
                                        userPlan = item.getPlanID();
                                        break;
                                    }
                                }
                            }
                            if (!TextUtils.isEmpty(userPlan) && !userPlan.equalsIgnoreCase(PlanFragment.FREEMIUM)) {
                                mSubscButton.setVisibility(View.GONE);
                            } else {
                                checkFreeTrialStatus();
                            }
                        }
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
        }

    }


    public boolean isStreamingTimedOut(){
        return mIsStreamingTimedOut;
    }

}

