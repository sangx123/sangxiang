package com.blinkhd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.devcomm.impl.hubble.P2pCommunicationManager;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.helpers.AsyncPackage;
import com.hubble.ui.ViewFinderFragment;
import com.msc3.PCMRecorder;
import com.util.AppEvents;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import base.hubble.PublicDefineGlob;
import base.hubble.meapi.PublicDefines;
import base.hubble.meapi.device.CreateTalkbackSessionResponse;
import base.hubble.meapi.device.CreateTalkbackSessionResponseData;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

public class TalkbackFragment extends Fragment implements Callback {
    private static final String TAG = "TalkbackFragment";

    private static final int STEP_INITIAL                 = 0;
    private static final int STEP_CREATE_TALKBACK_SESSION = 1;
    private static final int STEP_START_TALKBACK_SESSION  = 2;
    private static final int STEP_HANDSHAKE               = 3;
    private static final int STEP_LISTENING               = 4;

    private Device      selected_channel;
    private String      device_ip;
    private int         device_port;
    private int         device_audio_in_port;
    private PCMRecorder pcmRecorder;
    private String      http_pass;
    private boolean     talkback_enabled;

    private String saved_reg_id      = null;
    private String session_key       = null;
    private String stream_id         = null;
    private String relay_server_ip   = null;
    private int    relay_server_port = -1;

    private Activity       activity       = null;
    private ProgressDialog progressDialog = null;

    private int     current_step = STEP_INITIAL;
    private boolean isPortrait   = false;

    private ImageView mImgTalkback     = null;
    private TextView  mTxtTalkback     = null;
    private TextView  mTxtTalkbackLand = null;
    private ImageView mRippleImage1, mRippleImage2;

    private SecureConfig settings = HubbleApplication.AppConfig;

    private ViewFinderFragment mParentFragment;

    private Animation mRippleAnimation1, mRippleAnimation2;
    private EventData eventData;
    public static final int MSG_START_P2P_TALKBACK_SUCCESS = 0xDEADBEF1;
    public static final int MSG_START_P2P_TALKBACK_FAILED = 0xDEADBEF2;
    public static final int MSG_START_P2P_TALKBACK_BUSY = 0xDEADBEF3;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.talkback_fragment, container, false);
        eventData = new EventData();
        mImgTalkback = (ImageView) v.findViewById(R.id.imgTalkback);
        mTxtTalkback = (TextView) v.findViewById(R.id.textTalkback);
        mTxtTalkbackLand = (TextView) v.findViewById(R.id.textTalkback_land);
        mRippleImage1 = (ImageView) v.findViewById(R.id.ripple1);
        mRippleImage2 = (ImageView) v.findViewById(R.id.ripple2);
        ImageView closeImage = (ImageView) v.findViewById(R.id.close_talkback);
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewFinderFragment) mParentFragment).removeFragment(TalkbackFragment.this);
            }
        });
        return v;
    }

    public boolean isPortrait() {
        return isPortrait;
    }

    public void setPortrait(boolean isPortrait) {
        this.isPortrait = isPortrait;
    }

    @Override
    public void onResume() {
        super.onResume();
        Crittercism.leaveBreadcrumb(TAG + " onResume");
        CameraAvailabilityManager.getInstance()
                                 .isCameraInSameNetworkAsync(getActivity().getApplicationContext(), DeviceSingleton.getInstance().getSelectedDevice());

    }

    @Override
    public void onStart() {
        super.onStart();
        if (selected_channel != null) {

            // Show "Initilzing..." when checking locally
            Log.d(TAG, "Preparing talkback...");
            updateInitializingLayout();
            AsyncPackage.doInBackground(new Runnable() {
                @Override
                public void run() {
                    // Calling isAvailableLocally() on UI thread could cause hang
                    final boolean isInLocal = selected_channel.isAvailableLocally();
                    Log.d(TAG, "Preparing talkback DONE, isInLocal? " + isInLocal);
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isInLocal) {
                                    setupLocalTalkback();
                                } else {
                                    restore_previous_session_info();
                                    setupRemoteTalkback();
                                }
                            }
                        });
                    }
                }
            });

        }
    }


    private void restore_previous_session_info() {
        if (getActivity() != null) {
            saved_reg_id = settings.getString(PublicDefineGlob.PREFS_SAVED_TALKBACK_CAMERA_REG_ID, null);
            relay_server_ip = settings.getString(PublicDefineGlob.PREFS_SAVED_TALKBACK_RELAY_SERVER_IP, null);
            relay_server_port = settings.getInt(PublicDefineGlob.PREFS_SAVED_TALKBACK_RELAY_SERVER_PORT, -1);
            session_key = settings.getString(PublicDefineGlob.PREFS_SAVED_TALKBACK_SESSION_KEY, null);
            stream_id = settings.getString(PublicDefineGlob.PREFS_SAVED_TALKBACK_STREAM_ID, null);
        }

        if (saved_reg_id != null && session_key != null && stream_id != null && relay_server_ip != null && relay_server_port != -1) {
            if (saved_reg_id.equalsIgnoreCase(selected_channel.getProfile().getRegistrationId())) {
                if (pcmRecorder != null) {
                    pcmRecorder.setRelayAddr(relay_server_ip);
                    pcmRecorder.setRelayPort(relay_server_port);
                    pcmRecorder.setSessionKey(session_key);
                    pcmRecorder.setStreamId(stream_id);
                }
            } else {
                clear_talkback_session_info();
            }
        }
    }

    public int getScreenOrientation() {
        Display getOrient   = getActivity().getWindowManager().getDefaultDisplay();
        int     orientation = Configuration.ORIENTATION_UNDEFINED;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    private void setTextUpdatingShown(boolean flag) {
        if (flag) {
            if (mTxtTalkbackLand != null) {
                mTxtTalkbackLand.setVisibility(View.VISIBLE);
            }
      /*RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      p.addRule(RelativeLayout.BELOW, R.id.textTalkback_land);
      p.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
      if (mImgTalkback != null) {
        mImgTalkback.setLayoutParams(p);
      }*/
        } else {
            if (mTxtTalkbackLand != null) {
                mTxtTalkbackLand.setVisibility(View.INVISIBLE);
            }
     /* RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      p.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
      if (mImgTalkback != null) {
        mImgTalkback.setLayoutParams(p);
      }*/
        }
        mTxtTalkback.setVisibility(View.VISIBLE);
        mTxtTalkbackLand.setVisibility(View.INVISIBLE);
    /*if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT) {
      if (mTxtTalkback != null) {
        mTxtTalkback.setVisibility(View.VISIBLE);
      }
      if (mTxtTalkbackLand != null) {
        mTxtTalkbackLand.setVisibility(View.INVISIBLE);
      }
    } else if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
      if (mTxtTalkback != null) {
        mTxtTalkback.setVisibility(View.INVISIBLE);
      }
      if (mTxtTalkbackLand != null) {
        mTxtTalkbackLand.setVisibility(View.VISIBLE);
      }
    }*/
    }

    @Override
    public void onStop() {

        super.onStop();
        if (relay_server_ip != null && relay_server_port != -1) {
            relay_server_ip = null;
            relay_server_port = -1;
            clear_talkback_session_info();
            stop_talkback_session(session_key, stream_id);
        }
        setTalkBackEnabled(false);
        unlockOrientation();
        unmuteSpeaker();
        setTalkbackButtonEnabled(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    public void setDevice(Device s_channel) {
        selected_channel = s_channel;
        device_ip = selected_channel.getProfile().getDeviceLocation().getLocalIp();
        String stringDevicePort = selected_channel.getProfile().getDeviceLocation().getLocalPort1();
        if (stringDevicePort == null || stringDevicePort.trim().isEmpty()) {
            device_port = 80;
        } else {
            device_port = Integer.parseInt(stringDevicePort);
        }
        device_audio_in_port = PublicDefineGlob.DEFAULT_AUDIO_PORT;
        http_pass = String.format("%s:%s", PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, PublicDefineGlob.DEFAULT_CAM_PWD);

        pcmRecorder = new PCMRecorder(device_ip, device_port, http_pass, device_audio_in_port, new Handler(TalkbackFragment.this));
        pcmRecorder.setDeviceId(selected_channel.getProfile().getRegistrationId());

        //    if (selected_channel != null && selected_channel != null && !selected_channel.isAvailableLocally()) {
        //      restore_previous_session_info();
        //    }
    }

    public void setParentFragment(Fragment fragment) {
        mParentFragment = (ViewFinderFragment) fragment;
    }

    public static final int MSG_LONG_TOUCH                       = 0xDEADBEEF;
    public static final int MSG_LONG_TOUCH_START                 = 0xDEADBEEE;
    public static final int MSG_AUDIO_STREAM_HANDSHAKE_FAILED    = 0xDEADBEED;
    public static final int MSG_AUDIO_STREAM_HANDSHAKE_SUCCESS   = 0xDEADBEEC;
    public static final int MSG_START_LOCAL_TALKBACK_SUCCESS     = 0xDEADBEEB;
    public static final int MSG_START_LOCAL_TALKBACK_FAILED      = 0xDEADBEEA;
    public static final int MSG_SEND_AUDIO_DATA_TO_SOCKET_FAILED = 0xDEADBEF0;

    public static final int MSG_LONG_TOUCH_RELEASED  = 0xCAFEBEEF;
    public static final int MSG_SHORT_TOUCH_RELEASED = 0xCAFEBEED;
    public static final int MSG_PCM_RECORDER_ERR     = 0xDEADDEAD;

    private void setupLocalTalkback() {
        // TalkBackTouchListener tb = new TalkBackTouchListener(new
        // Handler(this));
        // ImageView mImgTalkback = (ImageView) activity.findViewById(R.id.imgTalkback);
        // if (mImgTalkback != null)
        // {
        // mImgTalkback.setVisibility(View.VISIBLE);
        // mImgTalkback.setOnTouchListener(tb);
        // }

        if (activity == null) {
            return;
        }

        current_step = STEP_INITIAL;
        updateStepInitialLayout();

        if (mImgTalkback != null) {
            mImgTalkback.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:

                            switch (current_step) {
                                case STEP_INITIAL:
                                    Log.d(TAG, "Start local talkback, camera: " + selected_channel.getProfile().getRegistrationId());
                                    GeAnalyticsInterface.getInstance()
                                                        .trackEvent(AppEvents.CAMERA_STREAM_SCREEN, AppEvents.VF_PUSH_TO_TALK_CLICKED, AppEvents.PUSH_TO_TALK);
                                    ZaiusEvent zoomEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                                    zoomEvt.action(AppEvents.VF_PUSH_TO_TALK_CLICKED);
                                    try {
                                        ZaiusEventManager.getInstance().trackCustomEvent(zoomEvt);
                                    }
                                    catch (ZaiusException e) {
                                        e.printStackTrace();
                                    }
                                    setTalkbackButtonEnabled(false);
                                    current_step = STEP_HANDSHAKE;
                                    updateSetupTalkbackSessionLayout();
                                    lockOrientation();
                                    muteSpeaker();
                                    setTalkBackEnabledWithoutStreaming(true);
                                    if (pcmRecorder != null) {
                                        pcmRecorder.startStreaming();
                                    }
                                    break;
                                case STEP_LISTENING:
                                    setTalkbackButtonEnabled(false);
                                    // Disable talkback button while turning off talkback to avoid user re-enable it
                                    new Thread(new Runnable() {

                                        @Override
                                        public void run() {
                                            disconnectTalkbackBlocked();
                                        }
                                    }).start();
                                    break;
                                default:
                                    break;
                            }

                            break;

                        default:
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private void setTalkbackButtonEnabled(final boolean isEnabled) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mImgTalkback != null) {
                        if (isEnabled == true) {
                            mImgTalkback.setClickable(true);
                        } else {
                            mImgTalkback.setClickable(false);
                        }
                    }
                }
            });

        }
    }

    private void setupRemoteTalkback() {

        if (activity == null) {
            return;
        }

        current_step = STEP_INITIAL;
        updateStepInitialLayout();

        if (mImgTalkback != null) {
            mImgTalkback.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    AnalyticsInterface.getInstance().trackEvent("viewfinder", "touch_to_talk_cliked", eventData);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            switch (current_step) {
                                case STEP_INITIAL:
                                    setTalkbackButtonEnabled(false);
                                    current_step = STEP_CREATE_TALKBACK_SESSION;
                                    updateSetupTalkbackSessionLayout();
                                    lockOrientation();
                                    muteSpeaker();
                                    setTalkBackEnabledWithoutStreaming(true);

                                    if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
                                        Log.d(TAG, "Start talkback via p2p, camera: " + selected_channel.getProfile().getRegistrationId());
                                        current_step = STEP_HANDSHAKE;
                                        if (pcmRecorder != null) {
                                            pcmRecorder.startStreaming();
                                        }
                                    } else {
                                        Log.d(TAG, "Start talkback via relay, camera: " + selected_channel.getProfile().getRegistrationId());
                                        if (saved_reg_id != null && session_key != null && stream_id != null && relay_server_ip != null && relay_server_port != -1) {
                                            if (pcmRecorder != null) {
                                                pcmRecorder.startStreaming();
                                            }
                                        } else {
                                            String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                                            create_talkback_session(saved_token, selected_channel.getProfile().getRegistrationId()
                                            );
                                        }
                                    }
                                    break;
                                case STEP_LISTENING:
                                    // Disable talkback button while turning off talkback to avoid user re-enable it
                                    setTalkbackButtonEnabled(false);
                                    new Thread(new Runnable() {

                                        @Override
                                        public void run() {
                                            disconnectTalkbackBlocked();
                                        }
                                    }).start();
                                    break;
                                default:
                                    break;
                            }

                            break;

                        default:
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private void updateInitializingLayout() {
        if (activity == null) {
            return;
        }

        if (mTxtTalkback != null) {
            mTxtTalkback.setText(getSafeString(R.string.initializing_talkback));
            mTxtTalkback.setVisibility(View.VISIBLE);
        }

        if (!isPortrait) {
            if (mTxtTalkbackLand != null) {
                mTxtTalkbackLand.setText(getSafeString(R.string.initializing_talkback));
                setTextUpdatingShown(true);
            }
        }
    }

    private void updateStepInitialLayout() {
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mImgTalkback != null) {
                    if (getActivity() != null) {
                        mImgTalkback.setImageDrawable((getActivity().getResources().getDrawable(R.drawable.talk)));
                    }
                }

                if (mTxtTalkback != null) {
                    mTxtTalkback.setText(getSafeString(R.string.touch_to_talk));
                    mTxtTalkback.setVisibility(View.VISIBLE);
                }

                if (!isPortrait) {
                    if (mTxtTalkbackLand != null) {
                        mTxtTalkbackLand.setText(getSafeString(R.string.touch_to_talk));
                        setTextUpdatingShown(true);
                    }
                }
            }
        });
    }

    private void updateSetupTalkbackSessionLayout() {
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mImgTalkback != null) {
                    if (getActivity() != null) {
                        mImgTalkback.setImageDrawable((getActivity().getResources().getDrawable(R.drawable.talk)));
                    }
                }

                if (mTxtTalkback != null) {
                    mTxtTalkback.setText(getSafeString(R.string.processing));
                    mTxtTalkback.setVisibility(View.VISIBLE);
                }

                if (!isPortrait) {
                    if (mTxtTalkbackLand != null) {
                        mTxtTalkbackLand.setText(getSafeString(R.string.processing));
                        setTextUpdatingShown(true);
                    }
                }
            }
        });
    }

    private void updateStepListeningLayout() {
        if (activity == null || getView() == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mImgTalkback != null) {
                    if (getActivity() != null) {
                        mImgTalkback.setImageDrawable((getActivity().getResources().getDrawable(R.drawable.stop)));
                        startAnimation();
                    }
                }

                if (mTxtTalkback != null) {
                    mTxtTalkback.setText(getSafeString(R.string.please_speak));
                    mTxtTalkback.setVisibility(View.VISIBLE);
                }

                if (!isPortrait) {
                    if (mTxtTalkbackLand != null) {
                        mTxtTalkbackLand.setText(getSafeString(R.string.please_speak));
                        setTextUpdatingShown(true);
                    }
                }
            }
        });
    }

    private void clear_talkback_session_info() {
        if (getActivity() != null) {
            SecureConfig settings = HubbleApplication.AppConfig;
            settings.remove(PublicDefineGlob.PREFS_SAVED_TALKBACK_CAMERA_REG_ID);
            settings.remove(PublicDefineGlob.PREFS_SAVED_TALKBACK_RELAY_SERVER_IP);
            settings.remove(PublicDefineGlob.PREFS_SAVED_TALKBACK_RELAY_SERVER_PORT);
            settings.remove(PublicDefineGlob.PREFS_SAVED_TALKBACK_SESSION_KEY);
            settings.remove(PublicDefineGlob.PREFS_SAVED_TALKBACK_STREAM_ID);
        }
    }

    private void startAnimation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mRippleAnimation1 = AnimationUtils
                .loadAnimation(getActivity(), R.anim.ripple_effect);
            mRippleAnimation2 = AnimationUtils
                .loadAnimation(getActivity(), R.anim.ripple_effect);
        } else {
            mRippleAnimation1 = AnimationUtils
                .loadAnimation(getActivity(), R.anim.ripple_effect_land);
            mRippleAnimation2 = AnimationUtils
                .loadAnimation(getActivity(), R.anim.ripple_effect_land);
        }

        if (mRippleAnimation2 != null) {
            mRippleAnimation2.cancel();
            mRippleAnimation2.reset();
        }
        mRippleImage2.startAnimation(mRippleAnimation2);
        mRippleImage2.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mRippleAnimation1 != null) {
                    mRippleAnimation1.cancel();
                    mRippleAnimation1.reset();
                }
                mRippleImage1.startAnimation(mRippleAnimation1);
                mRippleImage1.setVisibility(View.VISIBLE);
            }
        }, 800);

    }

    private void stopAnimation() {
        if(mRippleAnimation1 != null) {
            mRippleAnimation1.cancel();
            mRippleAnimation2.cancel();
            mRippleImage1.clearAnimation();
            mRippleImage1.setVisibility(View.INVISIBLE);
            mRippleImage2.clearAnimation();
            mRippleImage2.setVisibility(View.INVISIBLE);
        }
    }

    private void store_talkback_session_info(String regId, String session_key, String stream_id, String relay_server_ip, int relay_server_port) {
        if (getActivity() != null) {
            if (regId != null) {
                settings.putString(PublicDefineGlob.PREFS_SAVED_TALKBACK_CAMERA_REG_ID, regId);
            }

            if (relay_server_ip != null) {
                settings.putString(PublicDefineGlob.PREFS_SAVED_TALKBACK_RELAY_SERVER_IP, relay_server_ip);
            }

            if (relay_server_port != -1) {
                settings.putInt(PublicDefineGlob.PREFS_SAVED_TALKBACK_RELAY_SERVER_PORT, relay_server_port);
            }

            if (session_key != null) {
                settings.putString(PublicDefineGlob.PREFS_SAVED_TALKBACK_SESSION_KEY, session_key);
            }

            if (stream_id != null) {
                settings.putString(PublicDefineGlob.PREFS_SAVED_TALKBACK_STREAM_ID, stream_id);
            }
        }
    }

    private void muteSpeaker() {
        // mute speaker
        if (getActivity() != null) {
            AudioManager amanager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }
    }

    private void unmuteSpeaker() {
        // unmute speaker
        if (getActivity() != null) {
            AudioManager amanager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    }

    private void lockOrientation() {
        String phoneModel = android.os.Build.MODEL;
        if (!phoneModel.equals(PublicDefineGlob.PHONE_MBP1k) && !phoneModel.equals(PublicDefineGlob.PHONE_MBP2k)) {
            // // Log.d("mbp", "Turn on talkback - Lock screen orientation");
            int currOrient = getCurrentOrientation();
            if (getActivity() != null) {
                getActivity().setRequestedOrientation(currOrient);
            }
        }
    }

    private void unlockOrientation() {
        String phoneModel = android.os.Build.MODEL;
        if (!phoneModel.equals(PublicDefineGlob.PHONE_MBP1k) && !phoneModel.equals(PublicDefineGlob.PHONE_MBP2k)) {
            // // Log.d(TAG, "Turn off talkback - Unlock screen orientation");
            if (getActivity() != null) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        }
    }

    private boolean setTalkBackEnabledWithoutStreaming(boolean enabled) {
        if (enabled) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
          /* enable talkback */
                        if (selected_channel != null && selected_channel != null) {
                            pcmRecorder.setLocalMode(selected_channel.isAvailableLocally());
                        }
                        pcmRecorder.setActivity(getActivity());
                        talkback_enabled = pcmRecorder.startRecording();
                        Log.d(TAG, "setTalkBackEnabledWithoutStreaming, talkback enabled? " + talkback_enabled);
                        if (!talkback_enabled) {
                            Spanned msg = Html.fromHtml("<big>" + getSafeString(R.string.EntryActivity_ptt_2) + "</big>");
                            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Log.d(TAG, "setTalkBackEnabledWithoutStreaming, activity is null");
            }

            // Dont fade
            // cancelFullscreenTimer();
        } else {
      /* disable talkback */
            if (pcmRecorder != null) {
                pcmRecorder.stopRecording();
                pcmRecorder.releaseRecorder();
            }
            talkback_enabled = false;
            // tryToGoToFullScreen();
        }

        return talkback_enabled;
    }

    private boolean setTalkBackEnabled(boolean enabled) {
        if (enabled) {
      /*
       * Disable audio - But dont touch the UI stuf onSpeaker(null);
       */
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
          /* enable talkback */
                        if (selected_channel != null && selected_channel != null) {
                            pcmRecorder.setLocalMode(selected_channel.isAvailableLocally());
                        }
                        pcmRecorder.setActivity(getActivity());
                        talkback_enabled = pcmRecorder.startRecording();

                        if (!talkback_enabled) {
                            Spanned msg = Html.fromHtml("<big>" + getSafeString(R.string.EntryActivity_ptt_2) + "</big>");
                            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                        } else {
                            pcmRecorder.startStreaming();
                        }

                    }
                });
            } else {
                Log.d(TAG, "setTalkBackEnabled? " + enabled + ", activity is null");
            }
            // Dont fade
            // cancelFullscreenTimer();
        } else {
      /* disable talkback */
            if (pcmRecorder != null) {
                pcmRecorder.stopRecording();
                pcmRecorder.stopStreaming();
            }

            if (talkback_enabled) {
                showDisabledToast();
            }
            talkback_enabled = false;
        }

        return talkback_enabled;
    }

    private void showDisabledToast() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        stopAnimation();
                        Toast.makeText(getActivity(), getSafeString(R.string.talkback_disabled), Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception ex) {
                        //AA-1470: App always crash when play TALKBACK
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    private void showEnabledToast() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Toast.makeText(getActivity(), getSafeString(R.string.talkback_enabled), Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception ex) {
                        //AA-1470: App always crash when play TALKBACK
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {

            case MSG_START_LOCAL_TALKBACK_SUCCESS:
                current_step = STEP_LISTENING;
                showEnabledToast();
                updateStepListeningLayout();
                setTalkbackButtonEnabled(true);
                break;

            case MSG_START_LOCAL_TALKBACK_FAILED:
                disconnectTalkbackBlocked();
                break;
            case MSG_START_P2P_TALKBACK_SUCCESS:
            case MSG_AUDIO_STREAM_HANDSHAKE_SUCCESS:
                current_step = STEP_LISTENING;
                showEnabledToast();
                updateStepListeningLayout();
                setTalkbackButtonEnabled(true);
                break;

            case MSG_AUDIO_STREAM_HANDSHAKE_FAILED:
                relay_server_ip = null;
                relay_server_port = -1;
                if (pcmRecorder != null) {
                    pcmRecorder.setRelayAddr(relay_server_ip);
                    pcmRecorder.setRelayPort(relay_server_port);
                    pcmRecorder.setSessionKey(session_key);
                    pcmRecorder.setStreamId(stream_id);
                }
                clear_talkback_session_info();
                stop_talkback_session(session_key, stream_id);
                disconnectTalkbackBlocked();
                break;

            case MSG_SEND_AUDIO_DATA_TO_SOCKET_FAILED:
                if (selected_channel != null && !selected_channel.isAvailableLocally()) {
                    relay_server_ip = null;
                    relay_server_port = -1;
                    if (pcmRecorder != null) {
                        pcmRecorder.setRelayAddr(relay_server_ip);
                        pcmRecorder.setRelayPort(relay_server_port);
                        pcmRecorder.setSessionKey(session_key);
                        pcmRecorder.setStreamId(stream_id);
                    }
                    clear_talkback_session_info();
                    stop_talkback_session(session_key, stream_id);
                }
                disconnectTalkbackBlocked();
                break;

            case MSG_SHORT_TOUCH_RELEASED:
                if (getView() != null) {
                    getView().post(new Runnable() {

                        @Override
                        public void run() {
                            if (mImgTalkback != null) {
                                if (getActivity() != null) {
                                    mImgTalkback.setImageDrawable((getActivity().getResources().getDrawable(R.drawable.talk)));
                                }
                            }
                            if (mTxtTalkback != null) {
                                mTxtTalkback.setText(getSafeString(R.string.hold_to_talk));
                                mTxtTalkback.setVisibility(View.VISIBLE);
                            }

                            if (!isPortrait) {
                                if (mTxtTalkbackLand != null) {
                                    mTxtTalkbackLand.setText(getSafeString(R.string.hold_to_talk));
                                    setTextUpdatingShown(true);
                                }
                            }

                        }
                    });
                }

                // unmute speaker
                unmuteSpeaker();

                setTalkBackEnabledWithoutStreaming(false);
                break;

            case MSG_LONG_TOUCH_RELEASED:
                String phoneModel = android.os.Build.MODEL;
                if (getView() != null) {
                    getView().post(new Runnable() {

                        @Override
                        public void run() {
                            if (mImgTalkback != null) {
                                if (getActivity() != null) {
                                    mImgTalkback.setImageDrawable((getActivity().getResources().getDrawable(R.drawable.talk)));
                                }
                            }
                            if (mTxtTalkback != null) {
                                mTxtTalkback.setText(getSafeString(R.string.hold_to_talk));
                                mTxtTalkback.setVisibility(View.VISIBLE);
                            }

                            if (!isPortrait && mTxtTalkbackLand != null) {
                                mTxtTalkbackLand.setText(getSafeString(R.string.hold_to_talk));
                                setTextUpdatingShown(true);
                            }

                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                        }
                    });
                }

                unlockOrientation();
                // unmute speaker
                unmuteSpeaker();

                setTalkBackEnabled(false);
                // if (selected_channel != null && selected_channel
                // != null &&
                // !selected_channel.isInLocal())
                // {
                // stop_talkback_session(session_key, stream_id);
                // }

                break;

            case MSG_LONG_TOUCH_START:
                if (getView() != null) {
                    getView().post(new Runnable() {

                        public void run() {
                            lockOrientation();
                            // mute speaker
                            muteSpeaker();

                            if (mImgTalkback != null) {
                                if (getActivity() != null) {
                                    mImgTalkback.setImageDrawable((getActivity().getResources().getDrawable(R.drawable.stop)));
                                    startAnimation();
                                }
                            }

                            if (mTxtTalkback != null) {
                                mTxtTalkback.setVisibility(View.INVISIBLE);
                            }

                            if (!isPortrait && mTxtTalkbackLand != null) {
                                setTextUpdatingShown(false);
                            }
                        }
                    });
                }
                setTalkBackEnabledWithoutStreaming(true);
                break;

            case MSG_LONG_TOUCH:
                phoneModel = android.os.Build.MODEL;
                if (getActivity() != null) {
                    if (selected_channel != null && selected_channel != null && !selected_channel
                        .isAvailableLocally() && (relay_server_ip == null || relay_server_port == -1)) {
                        // progressDialog = ProgressDialog.show(
                        // getActivity(), null,
                        // getActivity().getApplicationContext().getSafeString(
                        // R.string.remove_camera_notification), true, false);
                        // start setup remote talkback first time
                        String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                        create_talkback_session(saved_token, selected_channel.getProfile().getRegistrationId());
                    } else {
                        pcmRecorder.startStreaming();
                    }
                }
                break;

            case MSG_PCM_RECORDER_ERR:// talkback problem from PCMRecorder
                if (talkback_enabled) {
                    setTalkBackEnabled(false);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getActivity(), getSafeString(R.string.EntryActivity_ptt_1), Toast.LENGTH_LONG
                                ).show();

                            }
                        });
                    }
                }
                break;

        }

        return false;
    }

    public void disconnectTalkback() {
        if (talkback_enabled == true) {
            setTalkbackButtonEnabled(false);
            // Disable talkback button while turning off talkback to avoid user re-enable it
            new Thread(new Runnable() {

                @Override
                public void run() {
                    disconnectTalkbackBlocked();
                }
            }).start();
        }
    }

    private void disconnectTalkbackBlocked() {
        Log.d(TAG, "Disconnecting talkback...");
        setTalkBackEnabled(false);
        unlockOrientation();
        unmuteSpeaker();
        updateStepInitialLayout();
        setTalkbackButtonEnabled(true);
        current_step = STEP_INITIAL;
        Log.d(TAG, "Disconnect talkback...DONE");
    }

    private String getSafeString(int stringResourceId) {
        if (getActivity() != null) {
            return getActivity().getString(stringResourceId);
        } else {
            return "";
        }
    }

    private int getCurrentOrientation() {
        int ret = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        if (getActivity() != null) {
            int rotation    = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            int orientation = getActivity().getResources().getConfiguration().orientation;

            if ((orientation & Configuration.ORIENTATION_PORTRAIT) == Configuration.ORIENTATION_PORTRAIT) {
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270) {
                    ret = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else {
                    ret = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                }
            } else if ((orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE) {
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                    ret = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                } else {
                    ret = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                }
            }
        }

        return ret;
    }

    private void create_talkback_session(String saved_token, String regId) {
        // do create_talkback_session task
        CreateTalkBackSessionTask createTalkBackTask = new CreateTalkBackSessionTask();
        createTalkBackTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, saved_token, regId);
    }

    private void start_talkback_session(String session_key, String stream_id) {
        // do start_talkback_session task
        StartTalkBackSessionTask startTalkBackTask = new StartTalkBackSessionTask();
        startTalkBackTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, session_key, stream_id);
    }

    private void create_talkback_session_success(String session_key, String stream_id) {
        this.session_key = session_key;
        this.stream_id = stream_id;
        current_step = STEP_START_TALKBACK_SESSION;
        saved_reg_id = selected_channel.getProfile().getRegistrationId();
        store_talkback_session_info(saved_reg_id, session_key, stream_id, null, -1);
        start_talkback_session(session_key, stream_id);
    }

    private void create_talkback_session_failed(int error_code, String error_desc) {
        Log.d(TAG, "Create talkback session failed, error code: " + error_code + ", reason: " + error_desc);
        switch (error_code) {
            case 422: // "Validation failed: Registration has already been taken"
                stop_talkback_session(session_key, stream_id);
                clear_talkback_session_info();
                if (session_key != null && stream_id != null) {
                    start_talkback_session(session_key, stream_id);
                } else {
                    stop_talkback_session(session_key, stream_id);
                    clear_talkback_session_info();
                    disconnectTalkbackBlocked();
                }
                break;

            case 404: // "Not found! " Device
            case 403: // "Unauthorized"
            case 500: // Internal server error
            default:
                disconnectTalkbackBlocked();
                break;
        }
    }

    private void start_talkback_session_success(String relay_server_ip, int relay_server_port) {
        // set relay ip & port for PCMRecorder
        // // // Log.d(TAG, "start relay talkback success, relay_ip: "
        // + relay_server_ip + ", relay_port: " + relay_server_port);
        current_step = STEP_HANDSHAKE;
        store_talkback_session_info(null, null, null, relay_server_ip, relay_server_port);
        this.relay_server_ip = relay_server_ip;
        this.relay_server_port = relay_server_port;
        if (pcmRecorder != null) {
            pcmRecorder.setRelayAddr(relay_server_ip);
            pcmRecorder.setRelayPort(relay_server_port);
            pcmRecorder.setSessionKey(session_key);
            pcmRecorder.setStreamId(stream_id);
            pcmRecorder.startStreaming();
        }
    }

    private void start_talkback_session_failed(int error_code, String error_desc) {
        Log.d(TAG, "Start talkback session failed, error code: " + error_code + ", reason: " + error_desc);

        switch (error_code) {
            case 404: // �Not found ! Relay session � or �Not found ! Relay Device �
                // re-create talkback session
                //        if (talkback_enabled) {
                //          current_step = STEP_CREATE_TALKBACK_SESSION;
                //          String saved_token = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                //          create_talkback_session(saved_token, selected_channel.getProfile().getRegistrationId());
                //        }
                //        break;
                // case 403: // "Unauthorized"
                // case 500: // Internal server error
                // break;

            default:
                stop_talkback_session(session_key, stream_id);
                clear_talkback_session_info();
                disconnectTalkbackBlocked();
                break;
        }
    }

    class CreateTalkBackSessionTask extends AsyncTask<String, String, Integer> {
        private static final int SUCCESS = 1;
        private static final int FAILED  = 2;

        private String session_key = null;
        private String stream_id   = null;

        private String accessToken = null;
        private String regId       = null;

        private String error_desc = null;
        private int    error_code = -1;

        @Override
        protected Integer doInBackground(String... params) {
            accessToken = params[0];
            regId = params[1];
            int ret = -1;

            try {
                CreateTalkbackSessionResponse createTalkBackRes = base.hubble.meapi.Device.createTalkbackSession(accessToken, regId);
                if (createTalkBackRes != null) {
                    error_code = createTalkBackRes.getStatus();
                    Log.d(TAG, "Create talkback session, response code: " + error_code);
                    if (error_code == 200) {
                        CreateTalkbackSessionResponseData createTalkBackResData = createTalkBackRes.getData();
                        if (createTalkBackResData != null) {
                            ret = SUCCESS;
                            session_key = createTalkBackResData.getSession_key();
                            stream_id = createTalkBackResData.getStream_id();
                        }
                    } else {
                        error_desc = createTalkBackRes.getMessage();
                    }
                } else {
                    Log.e(TAG, "Create talkback session failed, response null");
                }
            }
            catch (IOException e) {

                Log.e(TAG, Log.getStackTraceString(e));
            }

            return ret;
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result == SUCCESS) {
                create_talkback_session_success(session_key, stream_id);
            } else {
                create_talkback_session_failed(error_code, error_desc);
            }

        }

    }

    class StartTalkBackSessionTask extends AsyncTask<String, String, Integer> {
        private static final int SUCCESS = 1;
        private static final int FAILED  = 2;

        private String relay_server_ip   = null;
        private int    relay_server_port = -1;

        private String session_key = null;
        private String stream_id   = null;

        private String error_desc = null;
        private int    error_code = -1;
        private Gson   gson       = new Gson();

        @Override
        protected Integer doInBackground(String... params) {
            session_key = params[0];
            stream_id = params[1];

            URL               url         = null;
            HttpURLConnection conn        = null;
            DataInputStream   inputStream = null;
            String            response    = null;
            String            usr         = "";
            String            pwd         = "";
            String            usr_pass    = String.format("%s:%s", usr, pwd);
            int               ret         = -1;

            String serverUrl = settings.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL) + "/devices";
            serverUrl = serverUrl.replace("api", "talkback").replace("https", "http");
            String http_cmd = serverUrl + PublicDefineGlob.START_TALKBACK_CMD + PublicDefineGlob.TALKBACK_PARAM_1 + session_key + PublicDefineGlob.TALKBACK_PARAM_2 + stream_id;
            // Log.d(TAG, "Send start talkback cmd: " + http_cmd);
            try {
                url = new URL(http_cmd);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                Log.d(TAG, "Start talkback res code: " + conn.getResponseCode());
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
                    String                response_str = inputStream.readLine();
                    StartTalkBackResponse startTbRes   = null;
                    try {
                        startTbRes = gson.fromJson(response_str, StartTalkBackResponse.class);
                        if (startTbRes != null) {
                            error_code = startTbRes.getStatus();
                            Log.d(TAG, "Start talkback session status code: " + error_code);
                            if (error_code == 200) {
                                ret = SUCCESS;
                                relay_server_ip = startTbRes.getRelay_server_ip();
                                relay_server_port = startTbRes.getRelay_server_port();
                            } else {
                                error_desc = startTbRes.getMessage();
                            }
                        } else {
                            Log.d(TAG, "Start talkback session response null");
                        }
                    }
                    catch (JsonSyntaxException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }
            catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            return ret;
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result == SUCCESS) {
                start_talkback_session_success(relay_server_ip, relay_server_port);
            } else {
                start_talkback_session_failed(error_code, error_desc);
            }
        }

        private class StartTalkBackResponse {
            private int    status;
            private String message;
            private String device_response;
            private int    device_response_code;
            private String relay_server_ip;
            private int    relay_server_port;

            public int getStatus() {
                return status;
            }

            public String getMessage() {
                return message;
            }

            public String getDevice_response() {
                return device_response;
            }

            public int getDevice_response_code() {
                return device_response_code;
            }

            public String getRelay_server_ip() {
                return relay_server_ip;
            }

            public int getRelay_server_port() {
                return relay_server_port;
            }

        }

    }

    private void stop_talkback_session(String session_key, String stream_id) {
        StopTalkBackSessionTask stopTalkBackTask = new StopTalkBackSessionTask();
        stopTalkBackTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, session_key, stream_id);
    }

    private void stop_talkback_session_success() {
        Log.d(TAG, "stop relay talkback success");
    }

    private void stop_talkback_session_failed(int error_code, String error_desc) {
        Log.d(TAG, "stop relay talkback failed, reason: " + error_desc);
    }

    class StopTalkBackSessionTask extends AsyncTask<String, String, Integer> {
        private static final int SUCCESS = 1;
        private static final int FAILED  = 2;

        private String session_key = null;
        private String stream_id   = null;

        private String error_desc = null;
        private int    error_code = -1;
        private Gson   gson       = new Gson();

        @Override
        protected Integer doInBackground(String... params) {
            session_key = params[0];
            stream_id = params[1];

            URL               url         = null;
            HttpURLConnection conn        = null;
            DataInputStream   inputStream = null;
            String            response    = null;
            String            usr         = "";
            String            pwd         = "";
            String            usr_pass    = String.format("%s:%s", usr, pwd);
            int               ret         = -1;

            if (activity != null) {
                String serverUrl = settings.getString(PublicDefineGlob.PREFS_SAVED_SERVER_URL, PublicDefines.SERVER_URL) + "/devices";
                serverUrl = serverUrl.replace("api", "talkback").replace("https", "http");
                String http_cmd = serverUrl + PublicDefineGlob.START_TALKBACK_CMD + PublicDefineGlob.TALKBACK_PARAM_1 + session_key + PublicDefineGlob.TALKBACK_PARAM_2 + stream_id;
                // // Log.d(TAG, "Send stop talkback cmd");
                try {
                    url = new URL(http_cmd);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    // conn.addRequestProperty("Authorization", "Basic " +
                    // Base64.encodeToString(usr_pass.getBytes("UTF-8"),Base64.NO_WRAP));
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    error_code = conn.getResponseCode();
                    if (error_code == HttpURLConnection.HTTP_OK) {
                        ret = SUCCESS;
                        inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream(), 4 * 1024));
                    }
                }
                catch (IOException e) {
                    // // Log.e(TAG, Log.getStackTraceString(e));
                }
            }

            return ret;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == SUCCESS) {
                stop_talkback_session_success();
            } else {
                stop_talkback_session_failed(error_code, error_desc);
            }
        }
    }

}
