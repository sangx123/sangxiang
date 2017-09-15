package com.blinkhd;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.hubble.HubbleApplication;
import com.hubble.command.CameraCommandUtils;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.devcomm.impl.hubble.CameraAvailabilityManager;
import com.hubble.devcomm.impl.hubble.P2pCommunicationManager;
import com.hubble.framework.service.analytics.GeAnalyticsInterface;
import com.hubble.framework.service.analytics.zaius.ZaiusEventManager;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.tasks.comm.HTTPRequestSendRecvTask;
import com.hubble.registration.tasks.comm.UDTRequestSendRecvTask;
import com.hubble.ui.ViewFinderFragment;
import com.hubble.util.ListChild;
import com.nxcomm.blinkhd.actors.ActorMessage;
import com.nxcomm.blinkhd.actors.CameraSettingsActor;
import com.util.AppEvents;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;
import com.zaius.androidsdk.ZaiusEvent;
import com.zaius.androidsdk.ZaiusException;

import java.util.Timer;
import java.util.TimerTask;

import base.hubble.PublicDefineGlob;
import base.hubble.ServerDeviceCommand;

import com.hubbleconnected.camera.R;

/**
 * Created by sonikas on 19/10/16.
 */
public class MusicFragment extends Fragment implements OnClickListener,SeekBar.OnSeekBarChangeListener{

    private static final String TAG = "MusicFragment";
    private Device selected_channel = null;
    private String device_ip = null;
    private int device_port = -1;
    private String http_pass;
    private int currentMelodyIndx=-1;
    private boolean shouldUpdateMelody = true;
    private MelodyAdapter mMelodyAdapter = null;
    private Activity mActivity = null;
    private Timer mQueryMelodyTimer = null;
    private Dialog mWaitingDialog = null;
    private Dialog mLoadingDialog = null;
    private Dialog mUpdatingDialog = null;
    private Toast mUpdateDoneToast = null;
    private SeekBar mVolumeControlSeekBar;
    private String[] mPlaylistItems;
    private ImageView mPlay,mNext,mPrevious,mPlaylist;
    private TextView mTrackNameText;
    private boolean mIsPlaying=false;
    private Fragment mParentFragment;
    private CameraSettingsActor mCameraSettingActor;
    ListChild mCameraVolume;
    private final int mVolumeSeekBarMaxValue = 7;
    private final int mVolumeSeekBarDefaultValue=3;
    private Dialog mMelodyListdialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_music, container, false);
        mVolumeControlSeekBar=(SeekBar)v.findViewById(R.id.volume_seekbar);
        mPlay=(ImageView)v.findViewById(R.id.play_img);
        mPrevious=(ImageView)v.findViewById(R.id.previous_img);
        mNext=(ImageView)v.findViewById(R.id.next_img);
        mPlaylist=(ImageView)v.findViewById(R.id.playlist_img);
        mTrackNameText=(TextView)v.findViewById(R.id.track_name);
        mPlay.setOnClickListener(this);
        mPrevious.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPlaylist.setOnClickListener(this);

        mMelodyAdapter=new MelodyAdapter();
        createLoadingMelodyDialog();
        createUpdatingMelodyDialog();

        mCameraVolume = new ListChild(getSafeString(R.string.volume), "", true);
        mCameraSettingActor=new CameraSettingsActor(getActivity().getApplicationContext(), selected_channel, mActorInterface);
        initSeekbarControl();
        //currentMelodyIndx=-1;
        //mIsPlaying=false;
        ImageView closeImage=(ImageView)v.findViewById(R.id.close_music);
        closeImage.setOnClickListener(this);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mParentFragment=getParentFragment();
    }


    @Override
    public void onResume() {
        super.onResume();
        Crittercism.leaveBreadcrumb(TAG + " onResume");
        if(mIsPlaying){
            mPlay.setImageResource(R.drawable.pause);
            mTrackNameText.setText(mPlaylistItems[currentMelodyIndx]);
        }else{
            mPlay.setImageResource(R.drawable.play);
            mTrackNameText.setText("");
        }
        CameraAvailabilityManager.getInstance().isCameraInSameNetworkAsync(getActivity().getApplicationContext(), DeviceSingleton.getInstance().getSelectedDevice());
    }

    @Override
    public void onStart() {
        super.onStart();
        http_pass = String.format("%s:%s", PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, PublicDefineGlob.DEFAULT_CAM_PWD);
        shouldUpdateMelody = true;
        setupMelody();

        // show loading dialog for the first time
        setLoadingMelodyDialogVisible(true);

       mQueryMelodyTimer = new Timer();
        mQueryMelodyTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                queryMelodyStatus();
            }
        }, 0, 10000);
        queryMelodyStatus();
    }

    @Override
    public void onStop() {
        super.onStop();
        //setMelodyTimer(false);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.play_img:
                if (!mIsPlaying) {
                    if (currentMelodyIndx >= 0 && currentMelodyIndx < 6)
                        playPauseMelody(currentMelodyIndx, true, false, true);
                    else {
                        currentMelodyIndx = 0;
                        playPauseMelody(0, true, false, true);
                    }
                    mPlay.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.pause));
                } else {
                    playPauseMelody(currentMelodyIndx, false, false, true);
                    mPlay.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.play));
                }
                break;
            case R.id.previous_img:
                if (currentMelodyIndx - 1 >= 0) {
                    Log.d(TAG, "Previous track");
                    int currentIndex = currentMelodyIndx;
                    playPauseMelody(currentIndex - 1, true, false, true);

                } else {
                    int currentIndex = mPlaylistItems.length - 1;
                    currentMelodyIndx = currentIndex;
                    playPauseMelody(currentIndex, true, false, true);
                }
                break;
            case R.id.next_img:
                if (currentMelodyIndx + 1 < mPlaylistItems.length) {
                    Log.d(TAG, "next track");
                    int currentIndex = currentMelodyIndx;
                    playPauseMelody(currentIndex + 1, true, false, true);

                } else if (currentMelodyIndx + 1 == 5) {
                    final int currentIndex = -1;
                    playPauseMelody(currentIndex + 1, true, false, true);
                }
                break;
            case R.id.playlist_img:
                showPlaylistDialog();
                break;
            case R.id.close_music:
                ((ViewFinderFragment) mParentFragment).removeFragment(MusicFragment.this);
                break;
        }

    }

    public void stopPlaying(){
        if(mIsPlaying){
            Log.d(TAG, "Music playing..stopping now");
            playPauseMelody(currentMelodyIndx, false, false,false);
            currentMelodyIndx=0;
        }

    }

    private void initSeekbarControl(){
        try
        {
            createUpdatingVolumeDialog();
            int volume=CommonUtil.getSettingValue(mActivity, selected_channel.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VOLUME);
            mVolumeControlSeekBar.setMax(mVolumeSeekBarMaxValue);
            if(volume>0 && volume<=mVolumeSeekBarMaxValue)
                mVolumeControlSeekBar.setProgress(volume);
            else
                mVolumeControlSeekBar.setProgress(mVolumeSeekBarDefaultValue);

            mVolumeControlSeekBar.setOnSeekBarChangeListener(this);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    public void setDevice(Device s_channel) {
        selected_channel = s_channel;
        if (selected_channel != null) {
            device_ip = selected_channel.getProfile().getDeviceLocation().getLocalIp();
            String localPort = selected_channel.getProfile().getDeviceLocation().getLocalPort1();
            device_port = localPort != null && !localPort.isEmpty() ? Integer.parseInt(localPort) : 80;
        }
    }

    private void setupMelody(){
        mPlaylistItems=getResources().getStringArray(R.array.CameraMenuActivity_melody_items_2);

        if (PublicDefine.getModelIdFromRegId(
                selected_channel.getProfile().getRegistrationId()).equalsIgnoreCase(PublicDefine.MODEL_ID_MBP36N)) {
            mPlaylistItems = getResources().getStringArray(R.array.CameraMenuActivity_melody_items_3);
        }


    }

    private void setLoadingMelodyDialogVisible(boolean isVisible) {
        if (mLoadingDialog != null) {
            if (isVisible == true) {
                try {
                    mLoadingDialog.show();
                } catch (Exception e) {
                }
            } else {
                if (mLoadingDialog.isShowing()) {
                    try {
                        mLoadingDialog.dismiss();
                    } catch (Exception e) {
                    }
                }
            }
        }

    }

    private void setUpdatingVolumeDialogVisible(boolean isVisible) {
        if (mUpdatingDialog != null) {
            if (isVisible == true) {
                try {
                    mUpdatingDialog.show();
                } catch (Exception e) {
                }
            } else {
                if (mUpdatingDialog.isShowing()) {
                    try {
                        mUpdatingDialog.dismiss();
                    } catch (Exception e) {
                    }
                }
            }
        }

    }

    private void queryMelodyStatus() {
        if (mActivity == null || !shouldUpdateMelody) {
            return;
        }

        Thread worker = new Thread() {
            public void run() {

                String melody_response = null;
                melody_response = sendMelodyCmdGetRes(PublicDefineGlob.GET_MELODY_VALUE);

                Log.d(TAG, "get melody res: " + melody_response);
                //if (shouldUpdateMelody == true) {
                    if (melody_response != null &&
                            melody_response.startsWith(PublicDefineGlob.GET_MELODY_VALUE)) {
                        melody_response = melody_response.substring(
                                PublicDefineGlob.GET_MELODY_VALUE.length() + 2);
                        try {
                            currentMelodyIndx = Integer.parseInt(melody_response);
                            if (currentMelodyIndx >= 0 && currentMelodyIndx <=mPlaylistItems.length) {
                                currentMelodyIndx--;
                            }
                            if(currentMelodyIndx>=0 && currentMelodyIndx <mPlaylistItems.length){
                                mIsPlaying=true;
                                if(mActivity!=null) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mPlay.setImageResource(R.drawable.pause);
                                            mTrackNameText.setText(mPlaylistItems[currentMelodyIndx]);
                                        }
                                    });
                                }

                            }else{
                                mIsPlaying=false;
                                if(mActivity!=null) {
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mPlay.setImageResource(R.drawable.play);
                                            mTrackNameText.setText("");
                                        }
                                    });
                                }

                            }
                            Log.d(TAG, "CurrentMelodyIndex:"+currentMelodyIndx);
                            if(mActivity!=null){
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(mMelodyListdialog!=null && mMelodyListdialog.isShowing())
                                            mMelodyAdapter.notifyDataSetChanged();
                                    }
                                });
                            }


                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                    }

                //}

                setLoadingMelodyDialogVisible(false);
            }
        };

        worker.start();
    }

    private String sendMelodyCmdGetRes(String cmd) {

        Log.d(TAG, "sending melody cmd: " + cmd);
        String melodyRes = CameraCommandUtils.sendCommandGetFullResponse(selected_channel, cmd, null, null);

       /* boolean send_via_udt = false;
        if (selected_channel != null) {
            if (!selected_channel.isAvailableLocally()) {
                send_via_udt = true;
            }
        }

        Log.d(TAG, "sending melody cmd: " + cmd);
        if (send_via_udt) {
            String request = PublicDefineGlob.BM_HTTP_CMD_PART + cmd;
            if (P2pCommunicationManager.getInstance().isP2pCommunicationAvailable()) {
                melodyRes = P2pCommunicationManager.getInstance().sendCommand(new ServerDeviceCommand(request, null, null));
            } else {
                String saved_token = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null);
                if (saved_token != null) {
                    melodyRes = UDTRequestSendRecvTask.sendRequest_via_stun2(saved_token, selected_channel.getProfile().getRegistrationId(), request);
                } else {
                    Log.d(TAG, "Send melody cmd failed, user token is null");
                }
            }
        } //if (send_via_udt == true)
        else {
            final String device_address_port = device_ip + ":" + device_port;
            String http_addr = String.format("%1$s%2$s%3$s%4$s", "http://", device_address_port, "/?action=command&command=", cmd);
            melodyRes = HTTPRequestSendRecvTask.sendRequest_block_for_response(http_addr, PublicDefineGlob.DEFAULT_BASIC_AUTH_USR, http_pass);
        }

        Log.d(TAG, "send melody cmd res: " + melodyRes);*/

        return melodyRes;
    }

    /**
     * method to play or pause melody
     * cmd: melody(1-5) for playing melody and melodystop to stop
     * @param position melody number
     * @param enable play/pause
     * @param fromDialog if this method is called from melody dialog or from play/pause/previous/next button.
     *                   Need to call notifyDataSetChanged if called from dialog
     * @param showDialog whether progress dialog needs to be shown.
     */
    private void playPauseMelody(final int position, final boolean enable,final boolean fromDialog,final boolean showDialog){
        Log.d(TAG, "playPauseMelody..position"+position);
        //setMelodyTimer(false);
        shouldUpdateMelody = false;
        if(showDialog)
            setUpdatingMelodyDialogVisible(true);

       // final int tempPosition=position;


        Thread worker = new Thread() {
            public void run() {

                int retries = 3;
                String cmdRes = null;
                String cmd = null;
                boolean isSucceeded = false;
                while (retries-- > 0 && mActivity != null) {

                    if (!enable) {
                        cmd = PublicDefineGlob.SET_MELODY_OFF;
                    } else {
                        cmd = "melody" + String.valueOf(position + 1);
                    }
                    Log.d(TAG, "sending command:"+cmd);
                    cmdRes = sendMelodyCmdGetRes(cmd);

                    // check if the response has successfully
                    Log.i(TAG, "Melody cmd: " + cmd + ", response: " + cmdRes);
                    if (cmdRes != null && cmdRes.startsWith(cmd)) {
                        try {
                            cmdRes = cmdRes.substring(cmd.length() + 2);
                            if (cmdRes != null && cmdRes.equalsIgnoreCase("0")) {
                                Log.d(TAG, "send melody cmd: success");
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_LULLABY_STARTED,AppEvents.LULLABY_STARTED);

                                ZaiusEvent lullabyEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                                lullabyEvt.action(AppEvents.VF_LULLABY_STARTED);
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(lullabyEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }

                                isSucceeded = true;
                                if(enable)
                                    mIsPlaying=true;
                                else
                                    mIsPlaying=false;
                                break;
                            } else {
                                Log.e(TAG, "Send melody cmd failed, retries: " + retries);
                                GeAnalyticsInterface.getInstance().trackEvent(AppEvents.CAMERA_STREAM_SCREEN,AppEvents.VF_LULLABY_FAILED+" : "+retries,AppEvents.LULLABY_FAILED);

                                ZaiusEvent lullabyFailedEvt = new ZaiusEvent(AppEvents.CAMERA_STREAM_SCREEN);
                                lullabyFailedEvt.action(AppEvents.VF_LULLABY_FAILED+" : "+retries);
                                try {
                                    ZaiusEventManager.getInstance().trackCustomEvent(lullabyFailedEvt);
                                } catch (ZaiusException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }

                    // Sleep 1s then retry
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                final boolean isSucceededCopy = isSucceeded;
                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            if (isSucceededCopy) {
                                Log.d(TAG, "command succeded");
                                if(mIsPlaying)
                                    currentMelodyIndx = position;
                                else
                                    currentMelodyIndx=-1;
                                if(fromDialog)
                                    mMelodyAdapter.notifyDataSetChanged();
                                if(mIsPlaying){
                                    mPlay.setImageResource(R.drawable.pause);
                                    if(currentMelodyIndx!=-1)
                                        mTrackNameText.setText(mPlaylistItems[currentMelodyIndx]);
                                    Log.d(TAG, "current playing melody:"+currentMelodyIndx);
                                }else{
                                    mPlay.setImageResource(R.drawable.play);
                                    mTrackNameText.setText("");
                                }
                            } else {
                                // Show update failed toast
                                setUpdateMelodyToastVisible(true);
                                if(enable){
                                    mTrackNameText.setText("");
                                    mPlay.setImageResource(R.drawable.play);
                                    currentMelodyIndx=-1;
                                }else{
                                    mPlay.setImageResource(R.drawable.pause);
                                    if(currentMelodyIndx!=-1)
                                        mTrackNameText.setText(mPlaylistItems[currentMelodyIndx]);
                                }
                                if(fromDialog)
                                    mMelodyAdapter.notifyDataSetChanged();

                            }
                            shouldUpdateMelody = true;
                            //in some FWs, response is received immediately, however melody updation takes lil more time
                            //delay is added to dismiss dialog, to match it with iOS behaviour
                            if(showDialog) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        setUpdatingMelodyDialogVisible(false);
                                    }
                                },1000);

                            }

                        }
                    });

                }
            }
        };

        worker.start();
    }

    private void setUpdatingMelodyDialogVisible(boolean isVisible) {
        if (mWaitingDialog != null) {
            if (isVisible == true) {
                try {
                    mWaitingDialog.show();
                } catch (Exception e) {
                }
            } else {
                if (mWaitingDialog.isShowing()) {
                    try {
                        mWaitingDialog.dismiss();
                    } catch (Exception e) {
                    }
                }
            }
        }

    }

    private void createUpdatingMelodyDialog() {
        mWaitingDialog = new ProgressDialog(mActivity);
        mWaitingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ((ProgressDialog)mWaitingDialog).setMessage(getString(R.string.updating_melody));
        mWaitingDialog.setCancelable(false);
        mWaitingDialog.setCanceledOnTouchOutside(false);
        mWaitingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        mWaitingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
    }

    private void createLoadingMelodyDialog() {
        mLoadingDialog = new ProgressDialog(mActivity);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ((ProgressDialog)mLoadingDialog).setMessage(getString(R.string.loading));
        mLoadingDialog.setCancelable(true);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        mLoadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });


    }

    private void createUpdatingVolumeDialog() {
        mUpdatingDialog = new ProgressDialog(mActivity);
        mUpdatingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ((ProgressDialog)mUpdatingDialog).setMessage(getString(R.string.updating));
        mUpdatingDialog.setCancelable(false);
        mUpdatingDialog.setCanceledOnTouchOutside(false);
        mUpdatingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        mUpdatingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
            }
        });


    }


    private void setUpdateMelodyToastVisible(final boolean isVisible) {
        if (isVisible == true) {
            if (mActivity != null) {
                if (mUpdateDoneToast != null) {
                    mUpdateDoneToast.cancel();
                }

                mUpdateDoneToast = Toast.makeText(
                        mActivity,
                        getString(
                                R.string.update_melody_failed),
                        Toast.LENGTH_SHORT);
                mUpdateDoneToast.show();
            }
        } else {
            if (mUpdateDoneToast != null) {
                mUpdateDoneToast.cancel();
            }
        }
    }

    private void setMelodyTimer(boolean isEnabled) {
        if (isEnabled) {
            mQueryMelodyTimer = new Timer();
            try {
                mQueryMelodyTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        queryMelodyStatus();
                    }
                }, 10000, 10000);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        } else {
            if (mQueryMelodyTimer != null) {
                mQueryMelodyTimer.cancel();
            }
        }
    }

    private void showPlaylistDialog(){
        mMelodyListdialog=new Dialog(mActivity);
        mMelodyListdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMelodyListdialog.setContentView(R.layout.music_playlist_dialog);

        ImageView closeImage = (ImageView) mMelodyListdialog.findViewById(R.id.close_playlist);
        closeImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mMelodyListdialog.dismiss();
            }
        });

        ListView playlist=(ListView)mMelodyListdialog.findViewById(R.id.playlist);
        playlist.setAdapter(mMelodyAdapter);

        mMelodyListdialog.show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        setUpdatingVolumeDialogVisible(true);
        mCameraVolume.setOldCopy();
        int fieldValue = progress;

        long firmwareVersion = 0;
        try {
            firmwareVersion = Long.valueOf(selected_channel.getProfile().getFirmwareVersion().replace(".", ""));
        } catch (Exception ignored) {
        }
        if (selected_channel.getProfile().isVTechCamera()) {
            mCameraVolume.intValue = fieldValue;
        } else {
            if (firmwareVersion >= 11900) {
                mCameraVolume.intValue = fieldValue;
            } else {
                mCameraVolume.intValue = fieldValue + 21;
            }
        }

        mCameraVolume.value = String.valueOf(fieldValue);

        mCameraSettingActor.send(new ActorMessage.SetVolume(mCameraVolume, mCameraVolume.intValue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    class MelodyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mPlaylistItems.length;
        }

        @Override
        public Object getItem(int position) {
            if(position>=0 && position<mPlaylistItems.length)
                return mPlaylistItems[position];
            else
                return -1;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LinearLayout itemView;
            if (convertView == null && mActivity != null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = (LinearLayout) inflater.inflate(R.layout.bb_melody_list_item, null);

            } else {
                itemView = (LinearLayout) convertView;

            }

            final int melodyIdx = position;
            final ImageView imgMelody = (ImageView) itemView.findViewById(R.id.imgMelody);
            final TextView txtMelody = (TextView) itemView.findViewById(R.id.melodyItem);
            final LinearLayout layout_root = (LinearLayout) itemView.findViewById(R.id.layout_root);
            txtMelody.setText((String) getItem(position));

            if (imgMelody != null) {
                if (position == currentMelodyIndx) {
                    imgMelody.setImageResource(R.drawable.pause_popup);
                } else {
                    imgMelody.setImageResource(R.drawable.play_popup);
                }

                layout_root.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if(!mIsPlaying){
                            currentMelodyIndx=melodyIdx;
                            playPauseMelody(currentMelodyIndx,true,true,true);
                        }else{
                           if(currentMelodyIndx==melodyIdx)
                               playPauseMelody(currentMelodyIndx,false,true,true);
                            else{
                               currentMelodyIndx = melodyIdx;
                               playPauseMelody(currentMelodyIndx, true, true,true);
                           }

                        }

                        notifyDataSetChanged();
                    }
                });
            }



            return itemView;
        }
    }

    private CameraSettingsActor.Interface mActorInterface= new CameraSettingsActor.Interface() {
        @Override
        public void onDataSetChanged(ListChild listChild) {

        }

        @Override
        public void onNotificationSettingsReceived() {

        }

        @Override
        public void onParkReceived(Pair<String, Object> response) {

        }

        @Override
        public void onParkTimerReceived(Pair<String, Object> response) {

        }

        @Override
        public void onMotionNotificationChange(ListChild listChild, boolean success, String responseMessage) {

        }

        @Override
        public void onValueSet(ListChild listChild, boolean shouldRevert, String responseMessage) {
            if(mActivity==null)
                return;
            setUpdatingVolumeDialogVisible(false);
            if (mCameraVolume != null && !mCameraVolume.value.equalsIgnoreCase(getSafeString(R.string.failed_to_retrieve_camera_data))) {
                mVolumeControlSeekBar.setOnSeekBarChangeListener(null);
                mVolumeControlSeekBar.setProgress(Integer.parseInt(mCameraVolume.value));
                CommonUtil.setSettingValue(mActivity,selected_channel.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.VOLUME, mCameraVolume.intValue);
                mVolumeControlSeekBar.setOnSeekBarChangeListener(MusicFragment.this);
            }
        }

        @Override
        public void onScheduleDataReceived() {

        }
    };

    private String getSafeString(int stringResourceId) {
        if (mActivity!= null) {
            return this.getString(stringResourceId);
        } else {
            return "";
        }
    }



}
