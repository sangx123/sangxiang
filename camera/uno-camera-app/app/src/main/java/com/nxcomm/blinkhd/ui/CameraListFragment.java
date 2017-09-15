package com.nxcomm.blinkhd.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.actors.Actor;
import com.hubble.controllers.CameraController;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.dialog.HubbleDialogFactory;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginListener;
import com.hubble.framework.service.Plugins.ThirdParty.Nest.NestPluginManager;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.DeviceEvent;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.DeviceEventDetail;
import com.hubble.framework.service.cloudclient.device.pojo.response.EventResponse;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.hubble.framework.service.device.DeviceManagerService;
import com.hubble.framework.service.p2p.P2pDevice;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.framework.service.p2p.P2pService;
import com.hubble.framework.service.p2p.Utils;
import com.hubble.notifications.NotificationReceiver;
import com.hubble.receivers.AppExitReceiver;
import com.hubble.registration.AnalyticsController;
import com.hubble.registration.EScreenName;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.setup.CameraSetUpActivity;
import com.hubble.ui.DebugFragment;
import com.hubble.ui.HintScreenActivity;
import com.hubble.ui.MyVideoView;
import com.hubble.util.CommonConstants;
import com.hubble.util.P2pSettingUtils;

import com.msc3.ConnectToNetworkActivity;
import com.nest.common.Settings;
import com.nestlabs.sdk.DeviceUpdate;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.SmokeCOAlarm;
import com.nestlabs.sdk.Thermostat;
import com.nxcomm.blinkhd.actors.CameraSettingsActor;
import com.nxcomm.blinkhd.ui.customview.CameraStatusView;
import com.nxcomm.jstun_android.RmcChannel;
import com.sensor.ui.DeviceTabSupportFragment;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.hubble.PublicDefineGlob;
import base.hubble.database.DeviceProfile;
import base.hubble.database.DeviceStatusDetail;

import static android.os.Looper.getMainLooper;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;


public class CameraListFragment extends BaseFragment implements OnItemClickListener, CameraListViewHolder.IDataChangeListener {
    private CameraSettingsActor settingsActor;
    public static final String BROADCAST_REFRESH_CAMERA_LIST = "broadcast_camera_list";

    private BroadcastReceiver broadcaster;
    private SharedPreferences sharedPreferences;


    private Actor actor;

    private Handler mAppExitHandler;
    private Runnable mAppExitRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"Kill app and exit");
            Intent intent=new Intent();
            intent.putExtra("isKillApp", true);
            intent.setAction(AppExitReceiver.APP_EXIT_INTENT);
            getActivity().sendBroadcast(intent);
        }
    };

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }


  private boolean refreshing = true;
  private List<Device> mDevices;
  public List<Device> sensorDevices;
  public List<Device> nestDevices;

    private boolean removeSettingEntry = true;
    private DeviceManagerService mDeviceManagerService = null;
    private DeviceEvent mDeviceEvent = null;

    private Map<String, EventResponse> mEventDetailMap = new HashMap<String, EventResponse>();

    @Override
    public void onDataChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class RefreshDeviceList {
        boolean fromCache = false;

        public RefreshDeviceList(boolean cached) {
            this.fromCache = cached;
        }
    }

    private class CheckDeviceStatus {
    }

    private class ScanForLocalCamera {
    }

    private class GoToCameraDirectly {
    }


    private static final String TAG = "CameraListFragment";

    public static final String PREF_SHOWCASE_ADD_CAMERA = "ShowcaseAddCamera";
    public static final String PREF_SHOWCASE_PULL_REFRESH = "ShowcasePullToRefreshCameraList";
    public static final String PREF_SHOWCASE_CAMERA_VIEW = "ShowcaseCameraView";
    public static final String PREF_SHOWCASE_CAMERA_DETAILS = "ShowcaseCameraDetails";

    private static final int HANDLER_KEY_SHOWCASE_ADD_CAMERA = 1;
    private static final int HANDLER_KEY_SHOWCASE_PULL_REFRESH = 2;
    private static final int HANDLER_KEY_SHOWCASE_CAMERA_VIEW = 3;
    private static final int HANDLER_KEY_SHOWCASE_CAMERA_DETAILS = 4;

    private static final int SHOWCASE_DELAY_TIME = 100;

    private Activity mActivity = null;
    private Context mContext = null;
    private AnimationDrawable anim = null;

    // private GridView cameraListGridView = null;
    private RecyclerView cameraListView = null;
    private GridLayoutManager mLayoutManager = null;

    private SwipeRefreshLayout swipeLayout = null;
    private ScrollView mNoCameraViewHolder;
    private ImageView backgroundView = null;
    private boolean didBackgroundLoad = false;
    private SecureConfig settings = HubbleApplication.AppConfig;
    //private CameraListArrayAdapter mAdapter;
    private CameraListArrayAdapter2 mAdapter;
    private RelativeLayout mLoading;
    private TextView mLoadingText;
    private TextView mAddCameraText;
    private YouTubePlayerSupportFragment mYouTubeFragment;
    private View mView;
    private Dialog warningNetworkSwitchDialog;
    private AlertDialog mTurnOnWifiDialog;
    private Dialog mNoNetworkDialog = null;
    private View addCamera;

    private MyVideoView myVideoView;
    private MediaController mediaControls;

    private boolean firstLoad = true;
    private ShowcaseView showcaseView;


    float currentTemperatureInC = 0;
    String mainText = null, subText = null;
    float currentHumidity = 0;
    private EventData eventData;
    private ArrayList<Thermostat> thermostats;
    private ArrayList<SmokeCOAlarm> smokeCOAlarms;
    private boolean isShown = false;
    private Dialog smokeDialog;
    private ImageView smokeIndicator;
    private TextView smokeView, coIndicator, showCamera, liveStream, ignore, protectLocation, moveToFreshAir;
    private NestPluginListener.DeviceListener deviceListener;
    private long deviceListLoadingStartTime;
    private long deviceListLoadingEndTime;
    private boolean isUserRefreshing = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        eventData = new EventData();
        mView = inflater.inflate(R.layout.fragment_camera_list, container, false);
        sharedPreferences = getContext().getSharedPreferences("app_config", Context.MODE_PRIVATE);
        setHasOptionsMenu(true);


        if (broadcaster == null) {
            broadcaster = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "Needs to refresh the camera list");
                    refreshCameraList();
                }
            };
        }
        try {
            IntentFilter intentFilter = new IntentFilter(BROADCAST_REFRESH_CAMERA_LIST);
            getActivity().registerReceiver(broadcaster, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }


        thermostats = new ArrayList<>();
        smokeCOAlarms = new ArrayList<>();

        return mView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //createTurnOnWifiDialog();
        mAppExitHandler = new Handler(getMainLooper());
        mDeviceManagerService = DeviceManagerService.getInstance(getContext());
    }

    private void createTurnOnWifiDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                mActivity);

        alertDialogBuilder
                .setMessage(getString(R.string.wifi_is_disabled_please_turn_on_wifi_to_add_camera))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.turn_on_wifi), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        ConnectToNetworkActivity.setWifiEnabled(true);
                        Intent intent = new Intent(getActivity(), CameraSetUpActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        Button noButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        noButton.setTextColor(getResources().getColor(R.color.text_blue));

        Button yesButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        yesButton.setTextColor(getResources().getColor(R.color.text_blue));
    }

    private void stopMedia() {
        if (myVideoView != null && myVideoView.isPlaying()) {
            myVideoView.pause();
        }
    }

	/*
     * As per new UI, it is not required that we should display video if there is not device present.
      * That's reason, it is good that we should remove video file from assets and comment related code.
     */
  /*
    private void SetupMediaView() {
    //initialize the VideoView
    myVideoView = (MyVideoView) mView.findViewById(R.id.videoView);
    try {
      //set the media controller in the VideoView
      //mediaControls.setAnchorView(myVideoView);
      //myVideoView.setMediaController(mediaControls);
      //set the uri of the video to be played
      myVideoView.setVideoURI(Uri.parse("android.resource://" + mActivity.getPackageName() + "/" + R.raw.hubble_480p));
      myVideoView.setPlayPauseListener(new MyVideoView.PlayPauseListener() {
        @Override
        public void onPlay() {
          Log.i(TAG, "ON PLAYING");
          myVideoView.setBackgroundResource(0);
        }


        @Override
        public void onPause() {
          Log.i(TAG, "ON PAUSE");
        }
      });
      myVideoView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
          if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            if (myVideoView.isPlaying()) {
              myVideoView.pause();
            } else {
              myVideoView.start();
            }
          }
          return true;
        }
      });
      myVideoView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (myVideoView.isPlaying()) {
            myVideoView.pause();
          } else {
            myVideoView.start();
          }
        }
      });
      myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
        }
      });
      myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
          myVideoView.setBackgroundResource(R.drawable.hubble_bg);
        }
      });
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      e.printStackTrace();
    }
  }
  */

    private void checkAndGoToCameraView() {
        Device selectedDevice = DeviceSingleton.getInstance().getSelectedDevice();
        if (mActivity != null && selectedDevice != null) {
            new CameraController().switchToCameraFragment((MainActivity) mActivity, selectedDevice);
        } else {
            Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.cannot_go_to_camera), Toast.LENGTH_LONG).show();
        }
    }

    private Actor setupActor() {
        return new Actor() {
            @Override
            public Object receive(@Nullable Object m) {
                if (m == null) {
                    return null;
                }

                if (m instanceof ScanForLocalCamera) {
                } else if (m instanceof RefreshDeviceList) {
                    refreshDeviceList((RefreshDeviceList) m);
                } else if (m instanceof CheckDeviceStatus) {
                } else if (m instanceof GoToCameraDirectly) {
                    goToCameraDirectly();
                }

                return null;
            }

            private void goToCameraDirectly() {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        checkAndGoToCameraView();
                    }
                });
            }

            private void refreshDeviceList(RefreshDeviceList m) {

               /* if (m.fromCache == false) {
                    DeviceSingleton.getInstance().clearDevices();
                    DeviceSingleton.getInstance().removeTempDevice();
                }*/
                if (mActivity != null) {
                    try {

                        DeviceSingleton.getInstance().update(m.fromCache).get(); // block this actor until the refresh from the server is completed.
                        // Log.d(TAG, "AFTER Refreshing device list");

                    } catch (Exception e) {
                        // // Log.e(TAG, "Unable to refresh device list." + Log.getStackTraceString(e));
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    InetAddress address = InetAddress.getByName("api.hubble.in");
                                    Log.i(TAG, "Host address : " + address.getHostAddress());
                                    Log.i(TAG, "Host address is reachable : " + address.isReachable(1000));

                                    InetAddress[] ipAddress = address.getAllByName("api.hubble.in");
                                    for(int i = 0; i < ipAddress.length; i++)
                                        Log.i(TAG, "Host IP addresses : "+  i +"  :  " + ipAddress[i].toString());

                                }catch (UnknownHostException unhost){
                                    runOnMainThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            Log.i(TAG, "Unable to get camera list");
                                            if(getActivity() != null) {
                                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                                                alertDialog.setTitle(getString(R.string.unable_to_communicate_server));
                                                alertDialog.setMessage(getString(R.string.relaunch_app));
                                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.OK),
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                dialog.dismiss();
                                                                mAppExitHandler.postDelayed(mAppExitRunnable, 0);
                                                                getActivity().finishAffinity();
                                                            }
                                                        });
                                                alertDialog.show();
                                            }
                                        }
                                    });
                                }catch (IOException io){

                                }catch (NullPointerException ne){

                                }
                            }
                        };

                        thread.start();

                        e.printStackTrace();


                    }
                    // .get() is a blocking call, must ensure mActivity didn't close while this was happening
                    if (mActivity != null) {

                      sharedPreferences.edit().putBoolean("isSensorPrtesent", false).commit();
                      updateLocalDevices();
                        //AA-1571: Localytics Re-Integration: Implement CUSTOM DIMENSIONS
                        if (mDevices != null && mDevices.size() > 0) {
                            boolean isSensor = ((MainActivity) mActivity).getDeviceType();
                            if (!isSensor) {
                                AnalyticsController.getInstance().setNumOfCameras(mDevices.size());
                                AnalyticsController.getInstance().setNumOfCamerasAttribute(mDevices.size());
                            }
                        }//End AA-1571
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                swipeLayout.setRefreshing(false);
                            }
                        });
                        if (mDevices != null && mDevices.size() > 0) {

                            updateCameraListUI();

                        } else {
                            runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    setupNoCameraView();
                                }
                            });
                        }
                    }

                }
            }
        };
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PublicDefineGlob.SETUP_CAMERA_ACTIVITY_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                ((MainActivity) mActivity).registerNetworkChangeReceiver();
                if (actor != null) {
                    actor.send(new GoToCameraDirectly());
                }
            }
        }


    else if(requestCode == PublicDefineGlob.REGISTER_SENSOR_ACTIVITY_REQUEST){
      if (resultCode == PublicDefineGlob.REGISTER_SENSOR_ACTIVITY_RESULT) {
        //String sensorType = data.getStringExtra(SensorConstants.EXTRA_SENSOR_TYPE);
        //((MainActivity) mActivity).displayOverlaySensorEvents(sensorType);
        refreshCameraList();
      }
    }

  }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof Activity){
            this.mActivity = (Activity) context;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        actor = setupActor();

        didBackgroundLoad = false;

        // // Log.d(TAG, "Reached onStart");

        if (mActivity == null || getView() == null) {
            return;
        }

        swipeLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.camera_list_swipe_container);
        swipeLayout.setColorSchemeResources(R.color.app_refreshview_color_1,
                R.color.app_refreshview_color_2,
                R.color.app_refreshview_color_3,
                R.color.app_refreshview_color_4);

        mNoCameraViewHolder = (ScrollView) mActivity.findViewById(R.id.camera_list_no_cameras);

        mAddCameraText = (TextView) mActivity.findViewById(R.id.cameraListFragment_textPlusToAddCamera);


        cameraListView = (RecyclerView) mActivity.findViewById(R.id.cameraSettingListView);
        cameraListView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getActivity(), 1);

        cameraListView.setLayoutManager(mLayoutManager);

        cameraListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int topRowVerticalPosition = (cameraListView == null || cameraListView.getChildCount() == 0) ? 0 : cameraListView.getChildAt(0).getTop();
                GridLayoutManager layoutManager = ((GridLayoutManager) cameraListView.getLayoutManager());
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

                if(layoutManager.findFirstCompletelyVisibleItemPosition() == 0){
                    addCamera.setVisibility(View.VISIBLE);
                }else{
                    addCamera.setVisibility(View.GONE);
                }
                swipeLayout.setEnabled(firstVisiblePosition == 0 && topRowVerticalPosition >= 0);
            }
        });


        cameraListView.setVisibility(View.GONE);
        mNoCameraViewHolder.setVisibility(View.GONE);

        swipeLayout.setOnRefreshListener(refreshListener);
        final RelativeLayout loading = (RelativeLayout) getView().findViewById(R.id.settingImgHolder);
        if (loading != null) {
            loading.setVisibility(View.VISIBLE);
        }

    /*  Setup UI:
         @mLoading
         @mLoadingText
       Update local list: @mDevices - get data from Cache
       Update CameraList UI
       */
        if(SettingsPrefUtils.SHOULD_READ_SETTINGS)
            isUserRefreshing = true;
        initializeView();
        mEventDetailMap.clear();
        for (Device device : mDevices) {
            getEventDetail(device.getProfile().getRegistrationId());
        }


        //Phung: Don't do anything here, all logic moved to UserLoginActivity.java
        //syncCameraListFromCache();
        //syncCameraListFromServer();


        //XXX: NEED to turn on some time later
        if ((BuildConfig.FLAVOR.equals("vtech")) &&
                settings.getBoolean(PublicDefineGlob.PREFS_SHOULD_SHOW_TUTORIAL, true)) {
      /* Intent tutorialIntent = new Intent(getActivity(), TutorialActivity.class);
         startActivity(tutorialIntent); */
        }


        this.handler.postAtTime(refreshCameraListOnStartRunnable,60*1000);
    }

    @Override
    public void onStop() {
        this.handler.removeCallbacksAndMessages(refreshCameraListOnStartRunnable);
        if (actor != null) {
            actor.kill();
            actor = null;
        }
        super.onStop();
        if(deviceListener != null){
            NestPluginManager.getInstance().removeListener(deviceListener);
        }
    }


  /*  public void fadeOutView(View v, int duration_ms) {
        Animation myFadeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadeout);
        myFadeAnimation.setDuration(duration_ms);
        myFadeAnimation.setAnimationListener(new FadeOutAnimationAndGoneListener(v));
        v.startAnimation(myFadeAnimation);
    }

    private void fade_outin_view(View v, int duration_ms) {
        Animation myFadeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_button_in);
        myFadeAnimation.setDuration(duration_ms);
        myFadeAnimation.setAnimationListener(new FadeOutAnimationAndGoneListener(v));
        v.startAnimation(myFadeAnimation);
    }
*/
    private boolean isRefreshing = false;

    @Override
    public void onResume() {
        super.onResume();
        deviceListLoadingStartTime = System.currentTimeMillis();
        //AA-1480
        AnalyticsController.getInstance().trackScreen(EScreenName.Cameras);
        AnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_LIST, AppEvents.CAMERA_LIST, eventData);
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cancelledSync = false;
        getActivity().invalidateOptionsMenu();
       // initializeView();

        refreshCameraList();

        boolean isWifiEnabledOrEnabling = ConnectToNetworkActivity.isWifiEnabledOrEnabling();
        boolean isMobileNetworkEnabled = ConnectToNetworkActivity.hasMobileNetwork(HubbleApplication.AppContext) &&
                ConnectToNetworkActivity.isMobileDataEnabled(HubbleApplication.AppContext);
        if (isWifiEnabledOrEnabling == false && isMobileNetworkEnabled == false) {
            Log.i(TAG, "No network enabled, notify user");
            setNoNetworkDialogVisible(true);
        }

        if (Utils.supportPreviewMode()) {
            P2pManager.getInstance().setGlobalRmcChannelMode(RmcChannel.RMC_CHANNEL_MODE_PREVIEW);
            P2pManager.getInstance().switchAllToModeAsync(RmcChannel.RMC_CHANNEL_MODE_PREVIEW);
        } else {
            P2pManager.getInstance().setGlobalRmcChannelMode(RmcChannel.RMC_CHANNEL_MODE_KEEP_ALIVE);
            P2pManager.getInstance().switchAllToModeAsync(RmcChannel.RMC_CHANNEL_MODE_KEEP_ALIVE);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(P2pService.ACTION_P2P_CHANNEL_STATUS_CHANGED);
        mActivity.registerReceiver(mCameraStatusReceiver, intentFilter);

        IntentFilter intentFilterForNotification = new IntentFilter(NotificationReceiver.REFRESH_EVENTS_BROADCAST);
        Log.i(TAG, "Register broadcast REFRESH_EVENTS_BROADCAST in camera list fragment");
        mActivity.registerReceiver(mRefreshEventsBroadcastReceiver, intentFilterForNotification);

    }

    @Override
    public void onPause() {
        super.onPause();
        long timeSpentOnCameraTab = System.currentTimeMillis() - deviceListLoadingStartTime;
        int time =(int) timeSpentOnCameraTab / 1000;
        String cameraTabDuration = null;
        Log.d("LoginTime","LoginTime : "+deviceListLoadingEndTime + " Sec = "+time);
        if(time<=5){
            cameraTabDuration = "5 sec";
        }else if(time>5 && time<=10){
            cameraTabDuration = "10 sec";
        }else if(time>10 && time<=20){
            cameraTabDuration = "20 sec";
        }else if(time>20 && time<=30){
            cameraTabDuration = "30 sec";
        }else if(time>30 && time<=40){
            cameraTabDuration = "40 sec";
        }else if(time>40 && time<=50){
            cameraTabDuration = "50 sec";
        }else if(time>50 && time<=60){
            cameraTabDuration = "60 sec";
        }else{
            cameraTabDuration = ">1 min";
        }

        GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,AppEvents.TIME_SPENT_ON_CAMERA_TAB+" : " + cameraTabDuration,AppEvents.TIME_SPENT_ON_CAMERA_TAB);
        ZaiusEvent cameraTabEvt = new ZaiusEvent(AppEvents.DASHBOARD);
        cameraTabEvt.action(AppEvents.TIME_SPENT_ON_CAMERA_TAB + " : "+ cameraTabDuration);
        try {
            ZaiusEventManager.getInstance().trackCustomEvent(cameraTabEvt);
        } catch (ZaiusException e) {
            e.printStackTrace();
        }

        if (mActivity != null) {
            mActivity.unregisterReceiver(mCameraStatusReceiver);
            if (mRefreshEventsBroadcastReceiver != null) {
                try {
                    mActivity.unregisterReceiver(mRefreshEventsBroadcastReceiver);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

    /*
     * 20160309: HOANG: VIC-1435
     * Clear preview holder when exit camera list screen.
     */
//    P2pManager.getInstance().clearPreviewViewHolders();
    }

    private void setNoNetworkDialogVisible(boolean isVisible) {
        if (mNoNetworkDialog == null) {
            String msg = getActivity().getString(R.string.dialog_no_network_enabled);
            mNoNetworkDialog = HubbleDialogFactory.createAlertDialog(getActivity(), msg, getActivity().getString(R.string.OK), null, null, null, false, false);
        }

        if (isVisible) {
            if (mNoNetworkDialog != null && !mNoNetworkDialog.isShowing()) {
                try {
                    mNoNetworkDialog.show();
                } catch (Exception e) {
                }
            }
        } else {
            if (mNoNetworkDialog != null && mNoNetworkDialog.isShowing()) {
                try {
                    mNoNetworkDialog.dismiss();
                } catch (Exception e) {
                }
            }
        }

    }

    @Override
    public void onDestroyView() {
        cancelledSync = true;
        didBackgroundLoad = false;
        // unregister broadcast camera removing
        if (getActivity() != null) {
            try {
                getActivity().unregisterReceiver(broadcaster);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mAdapter != null)
            mAdapter.cancelAsyncTask();
        mActivity = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.activity_main, menu);
//Removed below options for UNO app
//    if (BuildConfig.FLAVOR.equals("vtech")) {
//      menu.findItem(R.id.menu_patrol).setVisible(true);
//      menu.findItem(R.id.menu_buy_camera).setVisible(false);
//    }
//   if (BuildConfig.FLAVOR.equals("hubble")) {
//      menu.findItem(R.id.menu_smart_nursery).setVisible(true);
//   } else {
        // Use package name which we want to check


        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_add_camera:
                if (isOfflineMode) {
                    //Do nothing
                    return true;
                }
                if (ConnectToNetworkActivity.isWifiEnabledOrEnabling()) {
                    //proceedSetupCamera();
                    Intent intent = new Intent(getActivity(), CameraSetUpActivity.class);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Wifi is not enabled or enabling, ask user to turn it on");

                    try {
                        createTurnOnWifiDialog();
                    } catch (Exception e) {
                    }
                }

                return true;
            case R.id.menu_buy_camera:
                try {
                    String country = Util.getUserCountry(getActivity());
                    if (TextUtils.isEmpty(country)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String networkInfo = Util.getNetworkInfo();
                                if (getActivity() == null || !isVisible() || !isResumed()) {
                                    return;
                                }
                                if (TextUtils.isEmpty(networkInfo)) {
                                    buyCamera(null);
                                } else {
                                    Log.d(TAG, networkInfo);
                                    try {
                                        JSONObject jsonObject = new JSONObject(networkInfo);
                                        if (jsonObject.has("countryCode")) {
                                            String country = jsonObject.getString("countryCode");
                                            buyCamera(country);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                    } else {
                        buyCamera(country);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            /*case R.id.menu_patrol:
                ((MainActivity) mActivity).switchToPatrolFragment();
                return true;*/
            case R.id.menu_app_settings:
                Intent appSettingIntent=new Intent(getActivity(), ApplicationSettingsActivity.class);
                startActivity(appSettingIntent);
                return true;
            default:
                break;
        }
        return false;
    }


    private void buyCamera(String country) {
        Log.d(TAG, "country code: " + country);
        int strRes = ("il".equalsIgnoreCase(country)) ? R.string.hubble_products_url_israel
                : R.string.hubble_products_url;
        String url = mActivity.getString(strRes);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private boolean isSamsungDevice() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        boolean result = false;
        if (manufacturer.indexOf("samsung") >= 0) {
            result = true;
        }
        return result;
    }

    private void proceedSetupCamera() {
    /*
     * For Android 5.0 or higher, check setting "Smart/Auto Network Switch" first.
     * If it's on, don't allow to continue setup process.
     */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean isMobileDataEnabled, isPoorNwAvoidEnabled;
            isMobileDataEnabled = ConnectToNetworkActivity.isMobileDataEnabled(mActivity.getApplicationContext());
            isPoorNwAvoidEnabled = ConnectToNetworkActivity.isPoorNetworkAvoidanceEnabled(mActivity.getApplicationContext());
            Log.d(TAG, "on add camera clicked, isMobileDataEnabled? " + isMobileDataEnabled +
                    ", isPoorNwAvoidEnabled? " + isPoorNwAvoidEnabled);
            // AA-1566 => we only show smart network switch dialog on samsung device
            if (isMobileDataEnabled == true && isPoorNwAvoidEnabled == true && isSamsungDevice()) {
                // Set preference value to show reminder dialog once setup completed.
                Log.d(TAG, "Smart/Auto Network Switch is on, save value preference flag");
                settings.putBoolean(PublicDefineGlob.PREFS_SHOULD_REMIND_TURN_ON_MOBILE_DATA, true);
                // show "Smart/Auto network switch" warning dialog here
                // AA-1598: Do not block user when they are setup camera on device which has smart network switch
                /*warningNetworkSwitchDialog = getWarningNetworkDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ((MainActivity) mActivity).switchToAddDeviceFragment();
                    }
                });
                warningNetworkSwitchDialog.show();*/

            } else {
                /*if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
                    launchAddCameraActivity();
                } else {
                    ((MainActivity) mActivity).switchToAddDeviceFragment();
                }*/
            }
        } else {
            /*if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
                launchAddCameraActivity();
            } else {
                ((MainActivity) mActivity).switchToAddDeviceFragment();
            }*/
        }
    }


    @Override
    public void onConfigurationChanged(Configuration config) {
//    if (cameraListGridView != null) {
//      layoutGridviewColumns(cameraListGridView);
//    }
        if (cameraListView != null) {
            layoutGridviewColumns(cameraListView);
        }

        if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew")) {
            ((DeviceTabSupportFragment) getParentFragment()).setTabHostVisibiliy(true);
        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny") || BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
            if ((mDevices == null || mDevices.size() == 0) && config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (DeviceSingleton.getInstance().isF86inList()) {
                    ((DeviceTabSupportFragment) getParentFragment()).setTabHostVisibiliy(true);
                } else {
                    ((DeviceTabSupportFragment) getParentFragment()).setTabHostVisibiliy(false);
                }
            } else if (DeviceSingleton.getInstance().isF86inList()) {
                ((DeviceTabSupportFragment) getParentFragment()).setTabHostVisibiliy(true);
            }
        }

        super.onConfigurationChanged(config);

        if (showcaseView != null && showcaseView.getVisibility() == View.VISIBLE) {
            ((ViewGroup) getActivity().findViewById(android.R.id.content)).removeView(showcaseView);
            showcaseView = null;
            //showShowcaseViewIfNeeded();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // // Log.d("mbp", "On item clicked.");
    }

    public CameraListArrayAdapter2 getAdapter() {
        return mAdapter;
    }

    private void launchAddCameraActivity() {
        final WifiManager w = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        if (w.getWifiState() == WifiManager.WIFI_STATE_ENABLED || w.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

            ((MainActivity) mActivity).unregisterNetworkChangeReceiver();
            //ToDo add coorespodning camerasetupactivity and test flow
            /*Intent cam_conf = new Intent(mActivity, SingleCamConfigureActivity.class);
            cam_conf.putExtra(SingleCamConfigureActivity.str_userToken, saved_token);
            startActivityForResult(cam_conf, PublicDefineGlob.SETUP_CAMERA_ACTIVITY_RESULT);*/
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            Spanned msg = Html.fromHtml("<big>" + getResources().getString(R.string.mobile_data_is_enabled_please_turn_on_wifi_to_add_camera) + "</big>");
            builder.setMessage(msg).setCancelable(true).setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
            ).setPositiveButton(getResources().getString(R.string.turn_on_wifi), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            turnOnWifi();

                            String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);

                            ((MainActivity) mActivity).unregisterNetworkChangeReceiver();
                            //ToDo add coorespodning camerasetupactivity and test flow
                            /*Intent cam_conf = new Intent(mActivity, SingleCamConfigureActivity.class);
                            cam_conf.putExtra(SingleCamConfigureActivity.str_userToken, saved_token);
                            startActivityForResult(cam_conf, PublicDefineGlob.SETUP_CAMERA_ACTIVITY_RESULT);*/
                            mActivity.finish();
                        }
                    }
            );

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private Dialog getWarningNetworkDialog(DialogInterface.OnClickListener onClickListener) {
        final Dialog warningDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setMessage(R.string.warning_smart_network_switch)
                .setPositiveButton(getResources().getString(R.string.OK), onClickListener);
        warningDialog = builder.create();
        warningDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return warningDialog;
    }

    private void turnOnWifi() {
    /* re-use Vox_main, just an empty and transparent activity */
        WifiManager w = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        w.setWifiEnabled(true);
    }

    private void initializeView() {
        mLoading = (RelativeLayout) getView().findViewById(R.id.settingImgHolder);
        mLoadingText = (TextView) getView().findViewById(R.id.settingTextLoader);
        addCamera = getView().findViewById(R.id.fab);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addCamera.setElevation(getResources().getDimension(R.dimen.elevation_3));
        }
        MainActivity ma = (MainActivity) getActivity();
        if (ma.getDeviceType()) {
            mLoadingText.setText(getString(R.string.loading_sensor_list));
        }
        updateLocalDevices();
        updateCameraListUI();

        addCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,AppEvents.DASHBOARD_CAMERA_ADD,AppEvents.ADD_CAMERA);
                ZaiusEvent addCameraClickedEvt = new ZaiusEvent(AppEvents.DASHBOARD);
                addCameraClickedEvt.action(AppEvents.DASHBOARD_CAMERA_ADD);
                try {
                    ZaiusEventManager.getInstance().trackCustomEvent(addCameraClickedEvt);
                } catch (ZaiusException e) {
                    e.printStackTrace();
                }

                if (isOfflineMode) {
                    //Do nothing
                    return;
                }
                if (ConnectToNetworkActivity.isWifiEnabledOrEnabling()) {
                    //proceedSetupCamera();
                    Intent intent = new Intent(getActivity(), CameraSetUpActivity.class);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "Wifi is not enabled or enabling, ask user to turn it on");

                    try {
                        createTurnOnWifiDialog();
                    } catch (Exception e) {
                    }
                }
            }
        });

    }

    private void stopLoading(boolean areCameras) {
        if (mLoading != null) {
            mLoading.setVisibility(View.INVISIBLE);
        }

        swipeLayout.setRefreshing(false);
        if (areCameras) {
            //cameraListGridView.setVisibility(View.VISIBLE);
            cameraListView.setVisibility(View.VISIBLE);
            mNoCameraViewHolder.setVisibility(View.GONE);
            deviceListLoadingEndTime = System.currentTimeMillis() - deviceListLoadingStartTime ;
            int time =(int) deviceListLoadingEndTime / 1000;
            String deviceListLoadingTime = null;
            Log.d("LoginTime","LoginTime : "+deviceListLoadingEndTime + " Sec = "+time);
            if(time<=1){
                deviceListLoadingTime = "1 sec";
            }else if(time>1 && time<=3){
                deviceListLoadingTime = "3 sec";
            }else if(time>3 && time<=5){
                deviceListLoadingTime = "5 sec";
            }else if(time>5 && time<=10){
                deviceListLoadingTime = "10 sec";
            }else{
                deviceListLoadingTime = ">10 sec";
            }
            GeAnalyticsInterface.getInstance().trackEvent(AppEvents.DASHBOARD,AppEvents.DEVICE_LIST_LOADING_TIME +" : "+deviceListLoadingTime,AppEvents.DEVICE_LIST_LOADING_TIME);
            ZaiusEvent deviceLoadingEvt = new ZaiusEvent(AppEvents.DASHBOARD);
            deviceLoadingEvt.action(AppEvents.DEVICE_LIST_LOADING_TIME+" : "+deviceListLoadingTime);
            try {
                ZaiusEventManager.getInstance().trackCustomEvent(deviceLoadingEvt);
            } catch (ZaiusException e) {
                e.printStackTrace();
            }

        } else {
            setupNoCameraView();
        }
        //showShowcaseViewIfNeeded();
        showHintScreen();
    }

    private void showHintScreen() {
        String userName = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, "");
        boolean isShowHintScreen = false;
        if (getContext() != null && CommonUtil.getForceUpgradeValueFromSP(getContext().getApplicationContext(), CommonUtil.APP_FORCE_UPGRADE_ONBOARDING_KEY, false)) {
            if (mAdapter != null && mAdapter.getItemCount() > 0 && getContext() != null) {
                isShowHintScreen = true;
                CommonUtil.setForceUpgradeValueToSP(getContext().getApplicationContext(), CommonUtil.APP_FORCE_UPGRADE_ONBOARDING_KEY, false);
            }
        } else {
            //Hint Screen will be shown for first time user logs in and first camera add
            if (mAdapter != null && mAdapter.getItemCount() == 1 && getContext() != null) {
                if (!CommonUtil.isHintScreenShownForUserFromSP(getContext(), userName) &&
                        CommonUtil.getFirstCameraAddedFromSP(getContext(), userName)) {
                    isShowHintScreen = true;
                }
            }
        }
        if (isShowHintScreen) {
            CommonUtil.setHintScreenShownForUserToSP(getContext(), userName, true);
            CommonUtil.setFirstCameraAddedToSP(getContext(), userName, false);
            Intent intent = new Intent(getContext(), HintScreenActivity.class);
            startActivity(intent);
        }
    }

    private void updateCameraListUI() {
        if (mActivity != null && getView() != null) {
            //backgroundView = (ImageView) getView().findViewById(R.id.camera_list_background_image);


            //Phung: remove Blur effect : costly operation!!! Cause crash in alot of cases
//      if (!didBackgroundLoad) {
//        runOnUIThreadIfActivityAvailable(new Runnable() {
//          @Override
//          public void run() {
//            Blur.blurImageBackground(backgroundView, mActivity.getApplicationContext(), mDevices);
//            didBackgroundLoad = true;
//          }
//        });
//      }

            Log.i("mbp", ">>>>>>>>>>>>> Send msg camera list changed");
            Intent it = new Intent(P2pManager.ACTION_CAMERA_LIST_CHANGED);
            String apiKey = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
            it.putExtra(P2pService.EXTRA_USER_TOKEN, apiKey);
            if (P2pSettingUtils.hasP2pFeature()) {
                // In startP2pService() method, app already checked whether P2pService is running.
                // Build P2P device list
                List<P2pDevice> p2pDevices = new ArrayList<>();
                List<Device> cameraDevices = DeviceSingleton.getInstance().getDevices();
                if (cameraDevices != null) {
                    for (Device cameraDevice : cameraDevices) {
                        boolean isOrbitP2PEnabled =  !cameraDevice.getProfile().isStandBySupported() || HubbleApplication.AppConfig.getBoolean(DebugFragment.PREFS_ENABLE_P2P_ORBIT, false);
                        if (isOrbitP2PEnabled && cameraDevice.getProfile().canUseP2p() && cameraDevice.getProfile().canUseP2pRelay() &&
                                 !TextUtils.isEmpty(cameraDevice.getProfile().getRegistrationId())) {
                            P2pDevice newDevice = new P2pDevice();
                            newDevice.setRegistrationId(cameraDevice.getProfile().getRegistrationId());
                            newDevice.setFwVersion(cameraDevice.getProfile().getFirmwareVersion());
                            newDevice.setMacAddress(cameraDevice.getProfile().getMacAddress());
                            newDevice.setModelId(cameraDevice.getProfile().getModelId());
                            if (cameraDevice.getProfile().getDeviceLocation() != null) {
                                newDevice.setLocalIp(cameraDevice.getProfile().getDeviceLocation().getLocalIp());
                            }
                            Log.d(TAG,"ADded device :- " + cameraDevice.getProfile().getRegistrationId() + " and status :" + cameraDevice.getProfile().isAvailable());
                            if (cameraDevice.getProfile().isStandBySupported())
                            {
                                DeviceStatusDetail deviceStatusDetail = cameraDevice.getProfile().getDeviceStatusDetail();
                                if(deviceStatusDetail != null && deviceStatusDetail.getDeviceStatus() != null)
                                {
                                    String deviceStatus  = deviceStatusDetail.getDeviceStatus();
                                    if(deviceStatus.compareToIgnoreCase(CameraStatusView.DEVICE_STATUS_RES_ONLINE) == 0)
                                    {
                                        newDevice.setAvailable(true);
                                    }
                                    else
                                    {
                                        newDevice.setAvailable(false);
                                    }
                                }
                                else
                                {
                                    newDevice.setAvailable(cameraDevice.getProfile().isAvailable());
                                }


                            } else {
                                newDevice.setAvailable(cameraDevice.getProfile().isAvailable());

                            }
                            p2pDevices.add(newDevice);
                        }
                    }
                } else {
                    Log.d(TAG, "Camera list fragment, device list is null");
                }
                it.putParcelableArrayListExtra(P2pService.EXTRA_P2P_DEVICES, (ArrayList<? extends Parcelable>) p2pDevices);
            }
            getActivity().sendBroadcast(it);

            // Update p2p waiting start time
            P2pManager.getInstance().updateP2pWaitingStartTime();
            runOnUIThreadIfActivityAvailable(new Runnable() {


        @Override
        public void run() {
          if (mAdapter == null) {
            mAdapter = new CameraListArrayAdapter2(((MainActivity) mActivity), CameraListFragment.this);
            // cameraListGridView.setOnItemClickListener(CameraListFragment.this);
          }

                    //layoutGridviewColumns(cameraListGridView);
                    layoutGridviewColumns(cameraListView);

//          if (cameraListGridView.getAdapter() == null) {
//            cameraListGridView.setAdapter(mAdapter);
//          }
                    if (cameraListView.getAdapter() == null) {
                        cameraListView.setAdapter(mAdapter);
                    }

                    synchronized (this) {
                        Collections.reverse(mDevices);
                        mAdapter.setDevices(mDevices);
                    }
                    mAdapter.notifyDataSetChanged();
                    if (mAdapter.getItemCount() > 0) {
                        //cameraListGridView.setVisibility(View.VISIBLE);
                        cameraListView.setVisibility(View.VISIBLE);
                        mNoCameraViewHolder.setVisibility(View.GONE);
                        if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew")) {
                            ((DeviceTabSupportFragment) getParentFragment()).setTabHostVisibiliy(true);
                        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny") || BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
                            // 20160204: binh: this condition is always true. see above.
                            if (DeviceSingleton.getInstance().getDevices().size() != 0) {
                                if (DeviceSingleton.getInstance().isF86inList() || ((DeviceTabSupportFragment) getParentFragment()).getCurrentTab() == 1) {
                                    ((DeviceTabSupportFragment) getParentFragment()).setTabHostVisibiliy(true);
                                } else {
                                    ((DeviceTabSupportFragment) getParentFragment()).setTabHostVisibiliy(false);
                                }
                            }
                        }
                    } else {
                        if (BuildConfig.FLAVOR.equalsIgnoreCase("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew")) {
                            ((DeviceTabSupportFragment) getParentFragment()).setTabHostVisibiliy(true);
                        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny") || BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
                            if (((DeviceTabSupportFragment) getParentFragment()).getCurrentTab() == 1) {
                                ((DeviceTabSupportFragment) getParentFragment()).setTabHostVisibiliy(true);
                            } else {
                                ((DeviceTabSupportFragment) getParentFragment()).setTabHostVisibiliy(false);
                            }
                        }
                    }
                    stopLoading(mAdapter.getItemCount() > 0);
                    if (isOfflineMode) {
                        showOfflineModeWarningView();
                    }
                }
            });
        }
    }


  private void updateLocalDevices() {

      //Phung : duplicate the whole List from DeviceSingleton
      //        instead of just getting the reference to the list.
      //        This is needed to avoid ConcurrentModificationException when we are processing here
      //         some other parties is calling DeviceSingleton to update the devices
    ArrayList<Device> devices = new ArrayList<Device>(DeviceSingleton.getInstance().getDevices());
      //boolean isSensor = ((MainActivity) mActivity).getDeviceType(); // Not needed any more
     // Reset sensor value to false on every refresh

      if(removeSettingEntry && devices != null)
      {
          removeSettingEntry = false;


      }

      synchronized (this)
      {
        mDevices = new ArrayList<>();
        sensorDevices = new ArrayList<>();
        for (Device aDevice : devices)
        {
          if (!mDevices.contains(aDevice))
          {
              filterDevices(aDevice);
          }
        }

          SettingsPrefUtils.SHOULD_READ_SETTINGS = false;
          isUserRefreshing = false;

          if(nestDevices != null) {
             for (Device nestDevice : nestDevices) {
                 if (!mDevices.contains(nestDevice)) {
                     mDevices.add(nestDevice);
                 }
             }
         }



      }


    if (mAdapter != null) {

      runOnUIThreadIfActivityAvailable(new Runnable() {
        @Override
        public void run() {
          synchronized (this) {
            mAdapter.setDevices(mDevices);
            mAdapter.fetchLatestDevices();

          }
          mAdapter.notifyDataSetChanged();
        }
    });
    }

  }

  private synchronized void filterDevices(final Device device) {
    //if (isSensor) {
      if (device.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR) || (device.getProfile().getParentId() != null && !device.getProfile().getParentId().equals(""))) {
        synchronized (this) {
          sharedPreferences.edit().putBoolean("isSensorPrtesent", true).commit();
          sensorDevices.add(device);
        }
      //}
    } else {

      if (!(device.getProfile().getParentId() != null && !device.getProfile().getParentId().equals("")) && !device.getProfile().registrationId.startsWith(PublicDefine.DEFAULT_REGID_OPEN_SENSOR)) {

                synchronized (this) {
                    boolean isNewDevice = false;
                    mDevices.add(device);
                    if(SettingsPrefUtils.SHOULD_READ_SETTINGS){
                        Log.i("TAG", "Fetching settings");
                        CommonUtil.setSettingInfo(getActivity(), device.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.FETCH_SETTINGS, true);

                    }else if(!CommonUtil.checkSettings(getActivity(), device.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.FETCH_SETTINGS)){
                        Log.i("TAG", "NEW camera Fetching settings");
                        isNewDevice = true;
                        CommonUtil.setSettingInfo(getActivity(), device.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.FETCH_SETTINGS, true);
                    }
                    if((device.getProfile().isAvailable()) && (isUserRefreshing || isNewDevice)){
                    if (!device.getProfile().getModelId().equalsIgnoreCase("0080")) {
                        if (device.getProfile().doesHaveTemperature() /*&&
                                ((( sharedPreferences.getString(device.getProfile().getRegistrationId() + "temp", null)) == null) ||
                                        (CommonUtil.getSettingInfo(getActivity(), device.getProfile().getRegistrationId() + "-" + SettingsPrefUtils.FETCH_SETTINGS)) ||
                                        isUserRefreshing )*/) {
                            Log.i("TAG","queryTemperature called for " + device.getProfile().getName());
                            queryTemperature(device);
                        }
                        if (device.getProfile().doesHaveHumidity()) {
                            queryHumidity(device);
                        }



                        if (device.getProfile().doesSupportSDCardAccess() && device.getProfile().isAvailable()) {

                            queryStorage(device);
                        }

                        if(device.getProfile().isDeviceBatteryOperated()){
                            queryBatteryMode(device);
                        }
                    }

                    if (device.getProfile().isStandBySupported()) {
                        if (device.getProfile().isAvailable()) {
                            if (!sharedPreferences.contains(PublicDefine.getSharedPrefKey(device.getProfile().getRegistrationId(), PublicDefine.GET_DEVICE_MODE))) {
                                queryBatteryMode(device);
                            } else {
                                String value = sharedPreferences.getString(PublicDefine.getSharedPrefKey(device.getProfile().getRegistrationId(), PublicDefine.GET_DEVICE_MODE), null);
                                if (value != null) {
                                    Pair<Long, String> responseVal = PublicDefine.getSharedPrefValue(value);
                                    if (responseVal != null) {
                                        if (PublicDefine.isExpire(responseVal.first, PublicDefine.SHARED_PREF_EXPIRE_TIME)) {
                                            queryBatteryMode(device);
                                        }
                                    }
                                }
                            }

                            if (device.getProfile().doesSupportSDCardAccess()) {
                                if (!sharedPreferences.contains(PublicDefine.getSharedPrefKey(device.getProfile().getRegistrationId(), PublicDefine.SDCARD_FREE_SPACE))) {
                                    queryStorage(device);
                                } else {
                                    String value = sharedPreferences.getString(PublicDefine.getSharedPrefKey(device.getProfile().getRegistrationId(), PublicDefine.SDCARD_FREE_SPACE), null);
                                    if (value != null) {
                                        Pair<Long, String> responseVal = PublicDefine.getSharedPrefValue(value);
                                        if (responseVal != null) {
                                            if (PublicDefine.isExpire(responseVal.first, PublicDefine.SHARED_PREF_EXPIRE_TIME)) {
                                                queryStorage(device);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                        isNewDevice = false;
                }//check end for isUserRefreshing
                }
            }
        }
    }





    private void getEventDetail(final String registrationId) {

        String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
        mDeviceEvent = new DeviceEvent(accessToken, registrationId);
        mDeviceEvent.setPage(0);
        mDeviceEvent.setSize(CommonConstants.EVENT_PAGE_SIZE);
        mDeviceManagerService.getDeviceEvent(mDeviceEvent, new Response.Listener<DeviceEventDetail>() {
            @Override
            public void onResponse(DeviceEventDetail response) {
                if (response != null && response.getEventResponse().length > 0) {
                    synchronized (this) {
                        EventResponse eventResponse = response.getEventResponse()[0];
                        mEventDetailMap.put(registrationId, eventResponse);
                        if (mAdapter != null) {
                            mAdapter.setEventDetail(mEventDetailMap);
                        }
                        //Save image to file
                     /*   if (eventResponse != null) {
                            EventResponse.EventData[] eventDataList = eventResponse.getEventDataList();
                            if ((eventDataList != null && eventDataList.length > 0) && imageUrl != null ) {
                                String eventImageURL = eventDataList[0].getImage();
                                if(CommonUtil.checkSettings(mContext, registrationId + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE)) {
                                    if (CommonUtil.getSettingInfo(mContext, registrationId + "-" + SettingsPrefUtils.SNAP_SHOT_AUTO_UPDATE)) {
                                        eventImageURL = eventDataList[0].getImage();
                                    }else{
                                        eventImageURL = imageUrl;
                                    }
                                }else{
                                    eventImageURL = eventDataList[0].getImage();
                                }

                                     //   String eventImageURL = imageUrl;//eventDataList[0].getImage();
                                if (eventImageURL != null) {
                                    com.squareup.picasso.Target target = new com.squareup.picasso.Target() {
                                        @Override
                                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                            Log.d(TAG, "Image saved to file for " + registrationId);
                                            saveBitmapToFile(registrationId, bitmap);
                                        }

                                        @Override
                                        public void onBitmapFailed(Drawable errorDrawable) {
                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                                        }
                                    };
                                    if (mActivity != null) {
                                        Picasso.with(mActivity.getApplicationContext()).
                                                load(eventImageURL).
                                                resize(100, 100).
                                                into(target);
                                    }
                                }
                            }
                        }*/
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mEventDetailMap.put(registrationId, null);
            }
        });
    }

    private boolean saveBitmapToFile(String registrationId, Bitmap bm) {
        File imageFile = new File(Util.getDashBoardPreviewPath(registrationId));
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e("app", e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }


    private void queryBatteryMode(final Device selectedDevice)
    {
        DeviceManager deviceManager;

        deviceManager = DeviceManager.getInstance(getActivity());
        SecureConfig settings = HubbleApplication.AppConfig;
        String regId = selectedDevice.getProfile().getRegistrationId();
        String command = null;
        if(selectedDevice.getProfile().getModelId().equalsIgnoreCase("0080")){
            command = PublicDefine.GET_DEVICE_MODE;
        }else{
            command = "get_battery_charging";
        }

        SendCommand getBatteryMode = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, command);

        deviceManager.sendCommandRequest(getBatteryMode, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response) {
                        String responsebody = response.getDeviceCommandResponse().getBody().toString();

                        int deviceMode = -1;

                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "Battery mode res : " + selectedDevice.getProfile().getRegistrationId() + "  : " + responsebody);

                        if (response.getDeviceCommandResponse() != null &&
                                (responsebody.contains(PublicDefine.GET_DEVICE_MODE) ||
                                        responsebody.contains("get_battery_charging"))) {

                            try {
                                final Pair<String, Object> parsedResponse = CommonUtil.parseResponseBody(responsebody);
                                if (parsedResponse != null && parsedResponse.second instanceof Float)
                                {
                                    deviceMode = ((Float)parsedResponse.second).intValue();
                                }
                                else if (parsedResponse != null && parsedResponse.second instanceof String)
                                {
                                    try {
                                        deviceMode =  Integer.parseInt((String) parsedResponse.second);
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                }
                                else if (parsedResponse != null && parsedResponse.second instanceof Integer) {
                                    deviceMode = (Integer) parsedResponse.second;
                                }
                            } catch (Exception exception) {
                                Log.d(TAG, exception.getMessage());
                                exception.printStackTrace();
                            }


                            sharedPreferences.edit().putString(PublicDefine.getSharedPrefKey(
                                    selectedDevice.getProfile().getRegistrationId(), PublicDefine.GET_DEVICE_MODE),
                                    PublicDefine.setSharedPrefValue(String.valueOf(deviceMode))).apply();

                            if ((int) deviceMode == CameraStatusView.ORBIT_BATTERY_CHARGING ||
                                    (int) deviceMode == CameraStatusView.ORBIT_BATTERY_DISCHARGING) {
                                queryBatteryValue(selectedDevice);
                            }

                            if (BuildConfig.DEBUG)
                                Log.i(TAG, "Parsed battery mode : " + selectedDevice.getProfile().getRegistrationId() + " : " + deviceMode);

                        }
                        else
                        {
                            if (BuildConfig.DEBUG)
                                Log.i(TAG, "Error parsed for battery mode : " + selectedDevice.getProfile().getRegistrationId() + " : " + deviceMode);

                            sharedPreferences.edit().remove(PublicDefine.getSharedPrefKey(
                                    selectedDevice.getProfile().getRegistrationId(), PublicDefine.GET_DEVICE_MODE)).apply();
                        }

                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
                        }
                        if (BuildConfig.DEBUG)
                            Log.i(TAG, "Error parsed for battery mode : " + selectedDevice.getProfile().getRegistrationId());

                        sharedPreferences.edit().remove(PublicDefine.getSharedPrefKey(selectedDevice.getProfile().getRegistrationId(),
                                PublicDefine.GET_DEVICE_MODE)).apply();
                    }
                }

        );
    }

    private void queryBatteryValue(final Device selectedDevice) {
        DeviceManager deviceManager;

        deviceManager = DeviceManager.getInstance(getActivity());
        SecureConfig settings = HubbleApplication.AppConfig;
        String regId = selectedDevice.getProfile().getRegistrationId();
        String command = null;
        if(selectedDevice.getProfile().getModelId().equalsIgnoreCase("0080")){
            command = PublicDefine.GET_BATTERY_VALUE;
        }else{
            command = "get_battery_percent";
        }
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Battery value req : " + selectedDevice.getProfile().getRegistrationId());

        SendCommand getBatteryValue = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, command);

        deviceManager.sendCommandRequest(getBatteryValue, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response) {
                        String responsebody = response.getDeviceCommandResponse().getBody().toString();
                        int batteryStatus = -1;

                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "Battery value res : " + selectedDevice.getProfile().getRegistrationId() + "  : " + responsebody);

                        if (response.getDeviceCommandResponse() != null &&
                                (responsebody.contains(PublicDefine.GET_BATTERY_VALUE) ||
                                        (responsebody.contains("get_battery_percent")))) {

                            try
                            {
                                final Pair<String, Object> parsedResponse = CommonUtil.parseResponseBody(responsebody);

                                if (parsedResponse != null && parsedResponse.second instanceof Float)
                                {
                                    batteryStatus = ((Float)parsedResponse.second).intValue();
                                }
                                else if (parsedResponse != null && parsedResponse.second instanceof String) {
                                    try {
                                        batteryStatus = Integer.parseInt((String) parsedResponse.second);
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                }
                                else if (parsedResponse != null && parsedResponse.second instanceof Integer) {
                                    batteryStatus = (Integer) parsedResponse.second;
                                }
                            } catch (Exception exception) {
                                Log.d(TAG, exception.getMessage());
                                exception.printStackTrace();
                            }


                            sharedPreferences.edit().putString(PublicDefine.getSharedPrefKey(
                                    selectedDevice.getProfile().getRegistrationId(), PublicDefine.GET_BATTERY_VALUE),
                                    PublicDefine.setSharedPrefValue(String.valueOf(batteryStatus))).apply();


                            if (BuildConfig.DEBUG)
                                Log.i(TAG, "Parsed battery status : " + selectedDevice.getProfile().getRegistrationId() + " : " + batteryStatus);

                        } else {
                            if (BuildConfig.DEBUG)
                                Log.i(TAG, "Error parsed for battery value : " + selectedDevice.getProfile().getRegistrationId() + " : " + batteryStatus);

                            sharedPreferences.edit().remove(PublicDefine.getSharedPrefKey(selectedDevice.getProfile().getRegistrationId(),
                                    PublicDefine.GET_BATTERY_VALUE)).apply();


                        }
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
                        }
                        if (BuildConfig.DEBUG)
                            Log.i(TAG, "Error parsed for battery mode : " + selectedDevice.getProfile().getRegistrationId());

                        sharedPreferences.edit().remove(PublicDefine.getSharedPrefKey(selectedDevice.getProfile().getRegistrationId(),
                                PublicDefine.GET_BATTERY_VALUE)).apply();
                    }
                }

        );
    }

    private String queryTemperature(final Device selectedDevice) {
        DeviceManager mDeviceManager;

        mDeviceManager = DeviceManager.getInstance(getActivity());
        SecureConfig settings = HubbleApplication.AppConfig;
        String regId = selectedDevice.getProfile().getRegistrationId();
        Log.i(TAG, "SERVER REQUEST : " + selectedDevice.getProfile().getRegistrationId());
        SendCommand getTemp = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "value_temperature");
        mDeviceManager.sendCommandRequest(getTemp, new Response.Listener<SendCommandDetails>() {

                    @Override
                    public void onResponse(SendCommandDetails response) {

                        String responsebody = response.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "SERVER RESP : " + selectedDevice.getProfile().getRegistrationId() + "  : " + responsebody);
                        if (response.getDeviceCommandResponse() != null && responsebody.contains("value_temperature")) {

                            try {
                                final Pair<String, Object> parsedResponse = CommonUtil.parseResponseBody(responsebody);
                                if (parsedResponse != null && parsedResponse.second instanceof Float) {
                                    currentTemperatureInC = (Float) parsedResponse.second;
                                } else if (parsedResponse != null && parsedResponse.second instanceof String) {
                                    try {
                                        currentTemperatureInC = Float.valueOf((String) parsedResponse.second);
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                } else if (parsedResponse != null && parsedResponse.second instanceof Integer) {
                                    currentTemperatureInC = (Integer) parsedResponse.second;
                                }
                            } catch (Exception exception) {
                                Log.d(TAG, exception.getMessage());
                                exception.printStackTrace();
                            }

                            mainText = subText = null;
                            int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
                            if (savedTempUnit == PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
                                mainText = Math.round(currentTemperatureInC) + "";
                                subText = "\u2103";
                            } else {
                                mainText = Math.round(CommonUtil.convertCtoF(currentTemperatureInC)) + "";
                                subText = "\u2109";
                            }

                            sharedPreferences.edit().putInt(selectedDevice.getProfile().getRegistrationId() + "camera_temperature", Math.round(currentTemperatureInC)).commit();

                            sharedPreferences.edit().putString(selectedDevice.getProfile().getRegistrationId() + "temp", mainText + subText).commit();
                            Log.i(TAG, "PARSED TEMP for : " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText + subText);

                        } else {
                            mainText = subText = "0";
                            Log.i(TAG, "ERROR PARSED TEMP for : " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText + subText);

                        }
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, "Error Message :- " + new String(error.networkResponse.data));
                        }
                        Log.i(TAG, "ERROR PARSED TEMP for : " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText + subText);
                        int previousTempValue = sharedPreferences.getInt(selectedDevice.getProfile().getRegistrationId() + "camera_temperature", 0);
                        int savedTempUnit = HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
                        if (savedTempUnit == PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) {
                            mainText = Math.round(previousTempValue) + "";
                            subText = "\u2103";
                        } else {
                            mainText = Math.round(CommonUtil.convertCtoF(previousTempValue)) + "";
                            subText = "\u2109";
                        }
                        sharedPreferences.edit().putInt(selectedDevice.getProfile().getRegistrationId() + "camera_temperature", Math.round(currentTemperatureInC));
                        sharedPreferences.edit().putString(selectedDevice.getProfile().getRegistrationId() + "temp", mainText + subText).commit();


                    }
                }

        );
        return mainText + subText;
    }


    private void queryStorage(final Device selectedDevice)
    {
        SecureConfig settings = HubbleApplication.AppConfig;
        String regId = selectedDevice.getProfile().getRegistrationId();

        if(selectedDevice.getProfile().isStandBySupported() && Util.isThisVersionGreaterThan(PublicDefine.ORBIT_SDCARD_CAPACITY_FIRMWARE_VERSION,selectedDevice.getProfile().getFirmwareVersion()))
        {
            SendCommand getOrbitFreeSpace = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, PublicDefine.ORBIT_FREE_STORAGE_SPACE);

            mDeviceManagerService.sendCommandToDevice(getOrbitFreeSpace, new Response.Listener<SendCommandDetails>() {
                        @Override
                        public void onResponse (SendCommandDetails response) {

                            String responseBody = response.getDeviceCommandResponse().getBody().toString();


                            if (response.getDeviceCommandResponse() != null && responseBody.contains(PublicDefine.ORBIT_FREE_STORAGE_SPACE))
                            {
                                int freeSpace = -2;

                                try
                                {
                                    final Pair<String, Object> parsedResponse = CommonUtil.parseResponseBody(responseBody);

                                    if (parsedResponse != null && parsedResponse.second instanceof Float)
                                    {
                                        freeSpace = ((Float)parsedResponse.second).intValue();
                                    }
                                    else if (parsedResponse != null && parsedResponse.second instanceof String)
                                    {
                                        try {
                                            freeSpace = Integer.parseInt((String) parsedResponse.second);
                                        } catch (NumberFormatException e) {
                                            // Camera sometimes returns non-ASCII characters
                                        }
                                    }
                                    else if (parsedResponse != null && parsedResponse.second instanceof Integer)
                                    {
                                        try {
                                            freeSpace = ((Integer) parsedResponse.second);
                                        } catch (NumberFormatException e) {
                                            // Camera sometimes returns non-ASCII characters
                                        }
                                    }
                                    else if(parsedResponse != null && parsedResponse.second  == null)
                                    {
                                        freeSpace = -1;
                                    }
                                }
                                catch (Exception exception)
                                {
                                    Log.d(TAG, exception.getMessage());
                                }
                                if(freeSpace != -2)
                                {
                                    sharedPreferences.edit().putInt(
                                            selectedDevice.getProfile().getRegistrationId()+PublicDefine.SDCARD_FREE_SPACE, freeSpace).apply();

                                }

                                if(BuildConfig.DEBUG)
                                    Log.i(TAG, PublicDefine.ORBIT_FREE_STORAGE_SPACE + " :- " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText);

                            }
                            else
                            {
                                mainText = subText = "0";
                                if(BuildConfig.DEBUG)
                                    Log.i(TAG, PublicDefine.ORBIT_FREE_STORAGE_SPACE + " error  for : " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText);
                            }
                            if (mAdapter != null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse (VolleyError error)
                        {

                            if(error != null)
                                error.printStackTrace();
                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }
                            if(BuildConfig.DEBUG)
                                Log.i(TAG, PublicDefine.SDCARD_FREE_SPACE + " error : " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText);
                        }
                    }

            );
        }
        else {


            SendCommand getFreeStorage = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, PublicDefine.SDCARD_FREE_SPACE);

            mDeviceManagerService.sendCommandToDevice(getFreeStorage, new Response.Listener<SendCommandDetails>() {
                        @Override
                        public void onResponse (SendCommandDetails response) {

                            String responseBody = response.getDeviceCommandResponse().getBody().toString();

                            if (response.getDeviceCommandResponse() != null && responseBody.contains(PublicDefine.SDCARD_FREE_SPACE))
                            {
                                int freeSpace = -2; // replace error value. -1 dedicate to indicate no sdcard error
                                try
                                {
                                    final Pair<String, Object> parsedResponse = CommonUtil.parseResponseBody(responseBody);
                                    if (parsedResponse != null && parsedResponse.second instanceof Float)
                                    {
                                        freeSpace = ((Float)parsedResponse.second).intValue();
                                    }
                                    else if (parsedResponse != null && parsedResponse.second instanceof String) {
                                        try {
                                            freeSpace = Integer.parseInt((String) parsedResponse.second);
                                        } catch (NumberFormatException e) {
                                            // Camera sometimes returns non-ASCII characters
                                        }
                                    } else if (parsedResponse != null && parsedResponse.second instanceof Integer) {
                                        try {
                                            freeSpace = (Integer) parsedResponse.second;
                                        } catch (NumberFormatException e) {
                                            // Camera sometimes returns non-ASCII characters
                                        }
                                    }
                                    else if(parsedResponse != null && parsedResponse.second  == null)
                                    {
                                        freeSpace = -1;
                                    }
                                }
                                catch (Exception exception)
                                {
                                    Log.d(TAG, exception.getMessage());
                                }

                                if(freeSpace != -2) {
                                    sharedPreferences.edit().putInt(
                                            selectedDevice.getProfile().getRegistrationId()+PublicDefine.SDCARD_FREE_SPACE, freeSpace).apply();

                                }

                                if(BuildConfig.DEBUG)
                                    Log.i(TAG, PublicDefine.SDCARD_FREE_SPACE + " :- " + selectedDevice.getProfile().getRegistrationId() + " : " + freeSpace);

                            }
                            else
                            {
                                mainText = subText = "0";
                                if(BuildConfig.DEBUG)
                                    Log.i(TAG, PublicDefine.SDCARD_FREE_SPACE + " error  for : " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText);
                            }

                            if (mAdapter != null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse (VolleyError error) {

                            if(error != null)
                                error.printStackTrace();
                            if (error != null && error.networkResponse != null) {
                                Log.d(TAG, error.networkResponse.toString());
                                Log.d(TAG, error.networkResponse.data.toString());
                            }
                            if(BuildConfig.DEBUG)
                                Log.i(TAG, PublicDefine.SDCARD_FREE_SPACE + " error : " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText);
                        }
                    }

            );

        }
    }



    private String queryHumidity(final Device selectedDevice) {
        DeviceManagerService mDeviceManagerService;

        mDeviceManagerService = DeviceManagerService.getInstance(getActivity());
        SecureConfig settings = HubbleApplication.AppConfig;
        String regId = selectedDevice.getProfile().getRegistrationId();
        SendCommand getTemp = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null), regId, "get_humidity");

        mDeviceManagerService.sendCommandToDevice(getTemp, new Response.Listener<SendCommandDetails>() {
                    @Override
                    public void onResponse(SendCommandDetails response) {

                        String responseBody = response.getDeviceCommandResponse().getBody().toString();
                        Log.i(TAG, "HUMIDITY SERVER RESP : " + responseBody);

                        if (response.getDeviceCommandResponse() != null && responseBody.contains("get_humidity")) {
                            try {
                                final Pair<String, Object> parsedResponse = CommonUtil.parseResponseBody(responseBody);
                                if (parsedResponse != null && parsedResponse.second instanceof Float) {
                                    currentHumidity = (Float) parsedResponse.second;
                                } else if (parsedResponse != null && parsedResponse.second instanceof String) {
                                    try {
                                        currentHumidity = Float.valueOf((String) parsedResponse.second);
                                    } catch (NumberFormatException e) {
                                        // Camera sometimes returns non-ASCII characters
                                    }
                                } else if (parsedResponse != null && parsedResponse.second instanceof Integer) {
                                    try {
                                        currentHumidity = Float.valueOf((Integer) parsedResponse.second);
                                    } catch (NumberFormatException e) {
                                        // Camera sometimes returns non-ASCII characters
                                    }
                                }
                            } catch (Exception exception) {
                                Log.d(TAG, exception.getMessage());
                            }
                            mainText = subText = null;
                            mainText = Math.round(currentHumidity) + "";
                            sharedPreferences.edit().putString(selectedDevice.getProfile().getRegistrationId() + "humidity", mainText).apply();
                            Log.i(TAG, "HUMIDITY PARSED HUMIDITY for : " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText);

                        } else {
                            mainText = subText = "0";
                            Log.i(TAG, "ERROR PARSED HUMIDITY for : " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText);

                        }
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error != null && error.networkResponse != null) {
                            Log.d(TAG, error.networkResponse.toString());
                            Log.d(TAG, error.networkResponse.data.toString());
                        }
                        Log.i(TAG, "ERROR PARSED HUMIDITY for : " + selectedDevice.getProfile().getRegistrationId() + " : " + mainText);
                    }
                }

        );
        //  settings.putString(selectedDevice.getProfile().getRegistrationId(), mainText+subText);

        // onDataChanged();
        return mainText;
    }

//  private void layoutGridviewColumns(GridView listView) {
//    if (mActivity != null && mDevices != null) {
//      Configuration config = mActivity.getResources().getConfiguration();
//      if (config != null && listView != null) {
//        if (mDevices.size() == 0) {
//          setupNoCameraView();
//          return;
//        }
//        if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
//          if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            listView.setNumColumns(1);
//          } else {
//            listView.setNumColumns(2);
//          }
//        } else {
//          stopMedia();
//          if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            if (mDevices.size() <= 2) {
//              listView.setNumColumns(1);
//            } else {
//              listView.setNumColumns(2);
//            }
//          } else {
//            listView.setNumColumns(3);
//          }
//        }
//      }
//    }
//  }

    private void layoutGridviewColumns(RecyclerView listView) {
        if (mActivity != null && mDevices != null) {
            Configuration config = mActivity.getResources().getConfiguration();
            if (config != null && listView != null) {
                if (mDevices.size() == 0) {
                    setupNoCameraView();
                    return;
                }
                if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
                    if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        mLayoutManager.setSpanCount(1);
                    } else {
                        mLayoutManager.setSpanCount(2);
                    }
                } else {
                    stopMedia();
                    if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        // if (mDevices.size() <= 2) {
                        mLayoutManager.setSpanCount(1);
//          } else {
//            mLayoutManager.setSpanCount(2);
//          }
                    } else {
                        mLayoutManager.setSpanCount(1);
                    }
                }
            }
        }
    }

    private void setupNoCameraView() {
        //cameraListGridView.setVisibility(View.GONE);
        if(mDevices != null && mDevices.size() == 0 ) {
            cameraListView.setVisibility(View.GONE);
            mNoCameraViewHolder.setVisibility(View.VISIBLE);
        }
        if (mLoading != null) {
            mLoading.setVisibility(View.GONE);
        }
        if (firstLoad) {
            firstLoad = false;

        }
    }

    private void setupYoutubeVideo() {
        try {
            FragmentManager fragmentManager = getChildFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mYouTubeFragment = new YouTubePlayerSupportFragment();
            fragmentTransaction.replace(R.id.cameraList_youtubeHolder, mYouTubeFragment);
            fragmentTransaction.commitAllowingStateLoss();
            //fragmentManager.executePendingTransactions();

            mYouTubeFragment.initialize(PublicDefine.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer onInitializedListener, boolean wasRestored) {
                    if (!wasRestored) {
                        try {
                            onInitializedListener.cueVideo("LMcSrQyRI-U"); // Hubble YouTube video ID
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
                }
            });
        } catch (Exception error) {
            if (mView != null) {
                mView.findViewById(R.id.cameraList_youtubeHolder).setVisibility(View.GONE);
            }
        }
    }

    private void runOnUIThreadIfActivityAvailable(Runnable runnable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(runnable);
        } else {
            Log.d(TAG, "getActivity() is NULL ");
        }
    }
//  Unused for now
  private Handler handler = new Handler();
    // refresh is required to get current orbit status
  private Runnable refreshCameraListOnStartRunnable = new Runnable() {
    @Override
    public void run() {
      if (refreshing) {
          // Why need to sync fr cache and then later sync from server ? Skip one
        //syncCameraListFromCache();
          if(mDevices != null && mDevices.size() >0)
          {
              for(Device device : mDevices)
              {
                  if(device.getProfile().isStandBySupported())
                  {
                      syncCameraListFromServer();
                      break;
                  }
              }
          }

      }
      handler.postDelayed(this, 60 * 1000);
    }
  };

    boolean cancelledSync = false;

    private void syncCameraListFromCache() {
        if (actor != null) {
            actor.send(new RefreshDeviceList(true));
        }
    }

    private void syncCameraListFromServer() {
        if (actor != null) {
            actor.send(new RefreshDeviceList(false));
        }
    }

    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {

            refreshCameraList();
            isUserRefreshing = true;
        }
    };

    private void refreshCameraList() {


        if(CommonUtil.getNestConfig(getActivity())) {
            if (Settings.loadAuthToken(getActivity()) != null) {
                if(NestPluginManager.getInstance().getNestAuthListener() == null){
                    authenticate(Settings.loadAuthToken(getActivity()));
                }else{
                    fetchDevices();
                }
            }
        }


        if(!CommonUtil.isInternetAvailable(getActivity())){
            Toast.makeText(getActivity(), "Seems like you are not connected to network. Please check your network connection!", Toast.LENGTH_LONG).show();
            if (mLoading != null) {
                mLoading.setVisibility(View.GONE);
            }

            swipeLayout.setRefreshing(false);

        }else if (isOfflineMode) {
            syncCameraListFromCache();
        } else {
           if((mDevices == null || mDevices.size() == 0) ||  swipeLayout.isRefreshing()) {
               if (mLoading != null) {
                   mLoading.setVisibility(View.VISIBLE);
               }
           }
            //Fix white screen flash glitch for legacy code from V4 App.
            //swipeLayout.setVisibility(View.GONE);

            //cameraListGridView.setVisibility(View.GONE);
        /*    cameraListView.setVisibility(View.GONE); */
            mNoCameraViewHolder.setVisibility(View.GONE);

            syncCameraListFromServer();

//        if (mLoading != null) {
//          mLoading.setVisibility(View.VISIBLE);
//        }
        }
    }

    private void showShowcaseViewIfNeeded() {
        mHandler.removeCallbacksAndMessages(null);
        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            if (!settings.getBoolean(PREF_SHOWCASE_PULL_REFRESH, false)) {
                mHandler.sendEmptyMessageDelayed(HANDLER_KEY_SHOWCASE_PULL_REFRESH, SHOWCASE_DELAY_TIME);
            } else if (!settings.getBoolean(PREF_SHOWCASE_CAMERA_VIEW, false)) {
                mHandler.sendEmptyMessageDelayed(HANDLER_KEY_SHOWCASE_CAMERA_VIEW, SHOWCASE_DELAY_TIME);
            } else if (!settings.getBoolean(PREF_SHOWCASE_CAMERA_DETAILS, false)) {
                mHandler.sendEmptyMessageDelayed(HANDLER_KEY_SHOWCASE_CAMERA_DETAILS, SHOWCASE_DELAY_TIME);
            }
        } else {
            if (!settings.getBoolean(PREF_SHOWCASE_ADD_CAMERA, false)) {
                mHandler.sendEmptyMessageDelayed(HANDLER_KEY_SHOWCASE_ADD_CAMERA, SHOWCASE_DELAY_TIME);
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            int what = message.what;
            Log.d(TAG,"Message Received :- " + what);
            if (what == HANDLER_KEY_SHOWCASE_ADD_CAMERA) {
                //showcaseAddCamera();
            } else if (what == HANDLER_KEY_SHOWCASE_PULL_REFRESH) {
                showcasePullToRefresh();
            } else if (what == HANDLER_KEY_SHOWCASE_CAMERA_VIEW) {
                showcaseCameraView();
            } else if (what == HANDLER_KEY_SHOWCASE_CAMERA_DETAILS) {
                showcaseCameraDetails();
            }

            return false;
        }
    });


    private void showcaseAddCamera() {
        if (showcaseView != null) {
            return;
        }
        showcaseView = new ShowcaseView.Builder(getActivity())
                .setTarget(new ViewTarget(R.id.menu_add_camera, getActivity()))
                .withMaterialShowcase().blockTouchHoldScreen()
                .setContentTitle(getString(R.string.click_to_add_device))
                .build();
        showcaseView.setButtonText(getString(R.string.got_it));

        showcaseView.overrideButtonClick(new ShowcaseView.Listener() {
            @Override
            public void onGotIt() {
                ((ViewGroup) getActivity().findViewById(android.R.id.content)).removeView(showcaseView);
                showcaseView = null;
                settings.putBoolean(PREF_SHOWCASE_ADD_CAMERA, true);
            }
        });
    }

    private void showcasePullToRefresh() {
        if (showcaseView != null) {
            return;
        }
        showcaseView = new ShowcaseView.Builder(getActivity())
                .setTarget(Target.NONE).withMaterialShowcase().blockTouchHoldScreen()
                .setContentTitle(getString(R.string.showcase_pull_down_to_refresh))
                .build();
        showcaseView.setButtonText(getString(R.string.got_it));

        showcaseView.overrideButtonClick(new ShowcaseView.Listener() {
            @Override
            public void onGotIt() {
                ((ViewGroup) getActivity().findViewById(android.R.id.content)).removeView(showcaseView);
                showcaseView = null;
                settings.putBoolean(PREF_SHOWCASE_PULL_REFRESH, true);
                showShowcaseView(HANDLER_KEY_SHOWCASE_CAMERA_VIEW);
            }
        });
        //
        int[] point = new int[2];
        //cameraListGridView.getLocationOnScreen(point);
        cameraListView.getLocationOnScreen(point);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int xPos = metrics.widthPixels / 2;
        showcaseView.animateGesture(xPos, point[1], xPos, point[1] + 256);
    }

    private void showcaseCameraView() {
        if (showcaseView != null) {
            return;
        }
        showcaseView = new ShowcaseView.Builder(getActivity())
                .setTarget(new ViewTarget(R.id.camera_list_item_frame_holder, getActivity()))
                .withMaterialShowcase().blockTouchHoldScreen()
                .setContentTitle(getString(R.string.showcase_camera_view))
                .build();
        showcaseView.setButtonText(getString(R.string.got_it));

        showcaseView.overrideButtonClick(new ShowcaseView.Listener() {
            @Override
            public void onGotIt() {
                ((ViewGroup) getActivity().findViewById(android.R.id.content)).removeView(showcaseView);
                showcaseView = null;
                settings.putBoolean(PREF_SHOWCASE_CAMERA_VIEW, true);
                showShowcaseView(HANDLER_KEY_SHOWCASE_CAMERA_DETAILS);
            }
        });
    }

    private void showcaseCameraDetails() {
        if (showcaseView != null) {
            return;
        }
        showcaseView = new ShowcaseView.Builder(getActivity())
                .setTarget(new ViewTarget(R.id.list_row_camera_setting_camSettingBtn, getActivity()))
                .withMaterialShowcase().blockTouchHoldScreen()
                .setContentTitle(getString(R.string.showcase_camera_details))
                .build();
        showcaseView.setButtonText(getString(R.string.got_it));

        showcaseView.overrideButtonClick(new ShowcaseView.Listener() {
            @Override
            public void onGotIt() {
                ((ViewGroup) getActivity().findViewById(android.R.id.content)).removeView(showcaseView);
                showcaseView = null;
                settings.putBoolean(PREF_SHOWCASE_CAMERA_DETAILS, true);
            }
        });
    }

    private void showShowcaseView(int key) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(key, SHOWCASE_DELAY_TIME);
    }

    @Override
    public void onCheckNowOfflineMode() {
        /*settings.remove(PublicDefineGlob.PREFS_USER_ACCESS_INFRA_OFFLINE);
        settings.putBoolean(CommonConstants.shouldNotAutoLogin, false);
        Intent new_login = new Intent(getActivity(), LoginOrRegistrationActivity.class);
        new_login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(new_login);
        getActivity().finish();*/
    }

    private BroadcastReceiver mCameraStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "Received broadcast: " + action);
            if (!TextUtils.isEmpty(action)) {
                if (action.equalsIgnoreCase(P2pService.ACTION_P2P_CHANNEL_STATUS_CHANGED)) {
                    String regId = intent.getStringExtra(P2pService.EXTRA_P2P_CHANNEL_REG_ID);
                    boolean status = intent.getBooleanExtra(P2pService.EXTRA_P2P_CHANNEL_STATUS, false);
                    Log.d(TAG, "Camera " + regId + " status has changed, new status: " + status);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                        // this is rrequired to get current status from server.
                        runOnUIThreadIfActivityAvailable(new Runnable() {
                            @Override
                            public void run() {
                                refreshCameraList();
                            }
                        });
                    }
                }
            }
        }
    };

    private BroadcastReceiver mRefreshEventsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String notificationCamera = intent.getStringExtra(NotificationReceiver.REFRESH_EVENTS_BROADCAST);
            if (mDevices != null && mDevices.size() > 0) {
                for (Device device : mDevices) {
                    if (device.getProfile().getRegistrationId().equals(notificationCamera)) {
                        Log.d(TAG, "Notification has come fetch for event detail " + notificationCamera);
                        getEventDetail(notificationCamera);
                    }
                }
            }
        }
    };

    private void authenticate(NestToken token) {
        NestPluginManager.getInstance().authWithToken(token, new NestPluginListener.AuthListener() {

            @Override
            public void onAuthSuccess() {
                Log.v(TAG, "Authentication succeeded.");
                fetchDevices();
            }

            @Override
            public void onAuthFailure(NestException exception) {
                Log.e(TAG, "Authentication failed with error: " + exception.getMessage());
//        Settings.saveAuthToken(NestDevicesActivity.this, null);
//        NestPluginManager.getInstance().launchAuthFlow(NestDevicesActivity.this, AUTH_TOKEN_REQUEST_CODE);
//        showAnimation(false);
            }

            @Override
            public void onAuthRevoked() {
                Log.e(TAG, "Auth token was revoked!");
                Settings.saveAuthToken(getActivity(), null);
//        NestPluginManager.getInstance().launchAuthFlow(NestDevicesActivity.this, AUTH_TOKEN_REQUEST_CODE);
            }
        });
    }


    private void fetchDevices() {
        deviceListener = new NestPluginListener.DeviceListener() {
            @Override
            public void onUpdate(@NonNull DeviceUpdate update) {
                if(nestDevices != null){
                    nestDevices.clear();
                }
                for (Thermostat thermostat : update.getThermostats()) {
                    Log.d(TAG,"Device id ="+thermostat.getDeviceId());
                    Log.d(TAG,"CameraList context ="+mContext);
                    if(mContext != null) {
                        if (Settings.isDeviceEnabled(mContext, thermostat.getDeviceId())) {
                            NestToken mToken = com.nest.common.Settings.loadAuthToken(getActivity());
                            if (mToken != null) {
                                addDevice(thermostat);
                            }
                        }
                    }
                }
               // getAdapter().setDevices(DeviceSingleton.getInstance().getDevices());
                getAdapter().notifyNestData(update.getThermostats());

                for (SmokeCOAlarm smokeCOAlarm : update.getSmokeCOAlarms()) {
                    if (smokeCOAlarm.getSmokeAlarmState().equals("emergency")|| smokeCOAlarm.getSmokeAlarmState().equals("warning") ) {
                        Log.d(TAG,"CameraList context ="+getActivity());
                       if(getActivity()!=null) {
                           String camName = Settings.getCamID(getActivity(), smokeCOAlarm.getNameLong());
                           //if (camName != null) {
                           // if (Settings.isDeviceEnabled(getContext(), smokeCOAlarm.getDeviceId()))
                           showSmokeDialog(smokeCOAlarm, camName);

                       }
                        //}
                    }
                }
            }
        };
        NestPluginManager.getInstance().addDeviceListener(deviceListener);
    }

    private void addDevice(Thermostat thermostat) {
        DeviceProfile deviceProfile = new DeviceProfile();
        deviceProfile.setName("Thermostat");
        deviceProfile.setAvailable(true);
        deviceProfile.setDeviceStatus(1);
        deviceProfile.setFirmwareVersion(thermostat.getSoftwareVersion());
        deviceProfile.setModelId("1234");
        deviceProfile.setMode("eco");
        deviceProfile.setStatus("Online");
        deviceProfile.setRegistrationId(thermostat.getDeviceId());
        Device device = new Device(deviceProfile, null);
        DeviceSingleton.getInstance().addTempDevice(device);
        if(nestDevices == null){
            nestDevices = new ArrayList<Device>();
        }
        nestDevices.add(device);
        updateLocalDevices();
        updateCameraListUI();
    }

    private void showSmokeDialog(SmokeCOAlarm smokeCOAlarm, final String camName) {

        if (smokeDialog != null) {
            smokeDialog.dismiss();
        }
        if (smokeDialog == null) {
            smokeDialog = new Dialog(getActivity());
            smokeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            smokeDialog.setContentView(R.layout.nest_smoke_warning_layout);
            smokeView = (TextView) smokeDialog.findViewById(R.id.smoke_detected);
            smokeIndicator = (ImageView) smokeDialog.findViewById(R.id.smoke_indicator);
            coIndicator = (TextView) smokeDialog.findViewById(R.id.co_detected);
            protectLocation = (TextView) smokeDialog.findViewById(R.id.protect_location);
            showCamera = (TextView) smokeDialog.findViewById(R.id.show_camera_msg);
            ignore = (TextView) smokeDialog.findViewById(R.id.ignore);
            liveStream = (TextView) smokeDialog.findViewById(R.id.live_stream);
            if(camName!=null){
                liveStream.setEnabled(true);
            }else{
                liveStream.setEnabled(false);
            }
            moveToFreshAir = (TextView) smokeDialog.findViewById(R.id.move_to_fresh_air);
        }

        protectLocation.setText("(" + smokeCOAlarm.getNameLong() + ")");
        showCamera.setText(getString(R.string.show_camera_live_view) + " " + camName);
        Log.d(TAG,"SmokeCoAlarm status"+ smokeCOAlarm.getSmokeAlarmState());

        if (smokeCOAlarm.getSmokeAlarmState().equals("emergency")) {
            smokeIndicator.setImageResource(R.drawable.smoke_emergency);
            smokeView.setTextColor(getActivity().getResources().getColor(R.color.smoke_emergency_color));
            coIndicator.setText(R.string.eme_co_detected);
            coIndicator.setTextColor(getActivity().getResources().getColor(R.color.smoke_emergency_color));
            moveToFreshAir.setVisibility(View.VISIBLE);
        } else {
            smokeIndicator.setImageResource(R.drawable.smoke_warning);
            smokeView.setTextColor(getActivity().getResources().getColor(R.color.smoke_warning_color));
            coIndicator.setText(R.string.war_co_detected);
            coIndicator.setTextColor(getActivity().getResources().getColor(R.color.smoke_warning_color));
            moveToFreshAir.setVisibility(View.GONE);

        }

        ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smokeDialog.dismiss();
            }
        });

        liveStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Device> devices = DeviceSingleton.getInstance().getDevices();
                for (Device selectedItem : devices) {
                    if (selectedItem.getProfile().getName().equals(camName)) {
                        getAdapter().switchToCameraFragment(selectedItem);
                        break;
                    }
                }
            }
        });

        if (smokeDialog != null && !smokeDialog.isShowing()) {
            smokeDialog.show();
        }
    }



}
