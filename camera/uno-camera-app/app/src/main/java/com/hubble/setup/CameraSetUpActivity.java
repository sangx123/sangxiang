package com.hubble.setup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.hubble.BaseActivity;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.framework.common.BaseContext;
import com.hubble.framework.common.TransportMode;
import com.hubble.framework.common.exception.BaseException;
import com.hubble.framework.core.connectivityManager.setup.HTTPRequestSendRecvTask;
import com.hubble.framework.device.Configuration;
import com.hubble.framework.service.connectivity.NetworkStatusManager;
import com.hubble.framework.service.connectivity.P2PDiscovery;
import com.hubble.framework.service.connectivity.RemoteDevice;
import com.hubble.framework.service.p2p.P2pUtils;
import com.hubble.tls.LocalDevice;
import com.hubble.tls.TLSPSK;
import com.hubble.tls.TLSResponse;

import com.hubble.util.CommonConstants;
import com.hubble.util.P2pSettingUtils;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

import base.hubble.PublicDefineGlob;


/**
 * Created by sonikas on 19/09/16.
 */
public class CameraSetUpActivity extends BaseActivity implements NetworkStatusManager,View.OnClickListener{

    private static final String TAG="CameraSetUpActivity";
    private ArrayAdapter adapter;
    private boolean isCameraListViewLoaded=false;
    private SecureConfig settings = HubbleApplication.AppConfig;
    private RemoteDevice mSelectedRemoteDevice;
    private String regId;
    private ImageView mLoadingAnimationView;
    private boolean isPairingHelpShown=false;
    private AnimationDrawable animationDrawable;
    private int mWiFiScanCounter = 0;
    private final int WIFI_SCAN_LIMIT = 3;
    private CountDownTimer mConfigTimer;
    private static final long CONFIG_TIMEOUT = 2 * 60 * 1000;

    private static final List<String> DEVICE_SSID_STARTS_UNO = Arrays.asList("Camera");
    private static final List<String> DEVICE_SSID_STARTS_ORBIT = Arrays.asList("Orbit");
    private static final List<String> DEVICE_SSID_STARTS_73 = Arrays.asList("Camera-","CameraHD-0073", "CameraHD-0173","Focus-");

    private int mDeviceType;

    private TransportMode mCurrentTransportMode = TransportMode.WI_FI_HUBBLE;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (P2pSettingUtils.hasP2pFeature())
        {
            stopP2pService();
        }
        showSelectCamera();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        P2PDiscovery.getInstance().unRegisterDiscoveryCallback(mCurrentTransportMode);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(android.os.Build.VERSION.SDK_INT>=23)
            checkLocationSetting();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(mConfigTimer!=null)
            mConfigTimer.cancel();
    }

    @Override
    public void onBackPressed()
    {
        if(!isPairingHelpShown)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setMessage(getString(R.string.setup_cancel))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(mConfigTimer!=null)
                                mConfigTimer.cancel();
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();

            alertDialog.show();

            Button noButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            noButton.setTextColor(getResources().getColor(R.color.text_blue));

            Button yesButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            yesButton.setTextColor(getResources().getColor(R.color.text_blue));
        }
        else
        {
            isPairingHelpShown=false;
            showPairInstruction();
        }

    }


    @Override
    public void onClick(View v)
    {
        switch(v.getId()){
            case R.id.setup_instruction_cancel:
            case R.id.setup_instruction_73_cancel:
            case R.id.setup_instruction_73_wifi_cancel:
            case R.id.setup_pair_73_cancel:
            case R.id.setup_pair_72_cancel:
            case R.id.setup_instruction_72_lan_cancel:
            case R.id.setup_instruction_72_wifi_cancel:
                onBackPressed();
                break;
            case R.id.setup_instruction_continue:
            case R.id.setup_instruction_73_next:
            case R.id.setup_instruction_73_wifi_next:
            case R.id.setup_instruction_72_lan_next:
            case R.id.setup_instruction_72_wifi_next:
                showPairInstruction();
                break;

            case R.id.setup_detect_camera:
            case R.id.setup_pair_73_next:
            case R.id.setup_pair_72_next:
                searchDevice();
                break;
            case R.id.detect_camera_again_btn:
                searchDevice();
                break;
            case R.id.detect_try_again:
                searchDevice();
                break;
            case R.id.camera_detected_next_btn:
                Intent intent=new Intent(CameraSetUpActivity.this,CameraConnectActivity.class);
                //TODO sometimes remoteDevice becomes NULL..need to check the reason;default string is passed to avoid crash
                if(mSelectedRemoteDevice!=null)
                    intent.putExtra("Camera_Name", mSelectedRemoteDevice.getName());
                else
                    intent.putExtra("Camera_Name", "camera");
                intent.putExtra(CameraConnectActivity.REMOTE_DEVICE,mSelectedRemoteDevice);
                intent.putExtra("reg_id", regId);
                intent.putExtra("device_type", mDeviceType);
                intent.putExtra(CameraConnectActivity.TRANSPORT_MODE,mCurrentTransportMode);
                startActivity(intent);
                finish();
                break;
            case R.id.tv_toolbar_back:
                onBackPressed();
                break;
            case R.id.setup_show_pair_instruction:
                showPairHelp();
                break;
            case R.id.connect_try_again:
                P2PDiscovery.getInstance().registerDiscoveryCallback(mCurrentTransportMode, this);
                showSetUpInstructions();
                break;
            case R.id.select_orbit_layout:
                mDeviceType=CommonConstants.DEVICE_TYPE_ORBIT;
                mCurrentTransportMode = TransportMode.WI_FI_HUBBLE;
                P2PDiscovery.getInstance().registerDiscoveryCallback(mCurrentTransportMode, this);
                showSetUpInstructions();
                break;
            case R.id.select_other_layout:
                mDeviceType=CommonConstants.DEVICE_TYPE_OTHER;
                mCurrentTransportMode = TransportMode.WI_FI_HUBBLE;
                P2PDiscovery.getInstance().registerDiscoveryCallback(mCurrentTransportMode, this);
                showSetUpInstructions();
                break;
            case R.id.select_73_layout:
                mDeviceType=CommonConstants.DEVICE_TYPE_73;
                showFocus72SetupPage();
                break;
            case R.id.select_72_layout:
                mDeviceType=CommonConstants.DEVICE_TYPE_72;
                showFocus72SetupPage();
                break;

            case R.id.select_72_layout_lan:
                mCurrentTransportMode = TransportMode.LAN;
                P2PDiscovery.getInstance().registerDiscoveryCallback(mCurrentTransportMode, this);
                showSetUpInstructions();
                break;

            case R.id.select_72_layout_wifi:
                mCurrentTransportMode = TransportMode.WI_FI_HUBBLE;
                P2PDiscovery.getInstance().registerDiscoveryCallback(mCurrentTransportMode, this);
                showSetUpInstructions();
                break;


            case R.id.setup_instruction_orbit_cancel:
                onBackPressed();
                break;
            case R.id.setup_instruction_orbit_next:
                searchDevice();
                break;

        }
    }

    private void checkLocationSetting()
    {
        try
        {
            int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);

            if (locationMode==Settings.Secure.LOCATION_MODE_OFF) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        this);

                alertDialogBuilder
                        .setMessage(getString(R.string.setup_enable_location))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.setup_location_settings), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finish();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();

                Button noButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                noButton.setTextColor(getResources().getColor(R.color.text_blue));

                Button yesButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                yesButton.setTextColor(getResources().getColor(R.color.text_blue));

            }
        }catch(Settings.SettingNotFoundException e){
            Log.d(TAG, "Unable to check location settings");
        }
    }

    private void setActionBar(String title){
        TextView tv_title = (TextView) findViewById(R.id.tv_toolbar_title);
        tv_title.setText(title);
        ImageView tv_back=(ImageView)findViewById(R.id.tv_toolbar_back) ;
        tv_back.setOnClickListener(this);
    }

    private void showSelectCamera()
    {
        setContentView(R.layout.device_setup_select_camera);
        setActionBar(getString(R.string.setup_add_camera));
        LinearLayout selectOrbitLayout=(LinearLayout)findViewById(R.id.select_orbit_layout);
        LinearLayout selectOtherLayout=(LinearLayout)findViewById(R.id.select_other_layout);
        LinearLayout select73Layout=(LinearLayout)findViewById(R.id.select_73_layout);
        LinearLayout select72Layout= (LinearLayout)findViewById(R.id.select_72_layout);
        selectOrbitLayout.setOnClickListener(this);
        selectOtherLayout.setOnClickListener(this);
        select73Layout.setOnClickListener(this);
        select72Layout.setOnClickListener(this);
    }
    private void showFocus72SetupPage()
    {
        setContentView(R.layout.focus_72_setup_instruction);
        if(mDeviceType== CommonConstants.DEVICE_TYPE_72)
        {
            setActionBar(getString(R.string.focus_72));
        }
        else
        {
            setActionBar(getString(R.string.focus_73));
        }

        LinearLayout select72LanLayout =(LinearLayout)findViewById(R.id.select_72_layout_lan);
        LinearLayout select72WifiLayout=(LinearLayout)findViewById(R.id.select_72_layout_wifi);

        select72LanLayout.setOnClickListener(this);
        select72WifiLayout.setOnClickListener(this);
    }


    private void showSetUpInstructions()
    {
        if(mDeviceType==CommonConstants.DEVICE_TYPE_OTHER)
        {
            setContentView(R.layout.device_setup_instruction);
            setActionBar(getString(R.string.setup_title));
            TextView cancel = (TextView) findViewById(R.id.setup_instruction_cancel);
            TextView continu = (TextView) findViewById(R.id.setup_instruction_continue);
            cancel.setOnClickListener(this);
            continu.setOnClickListener(this);
        }
        else if(mDeviceType ==CommonConstants.DEVICE_TYPE_ORBIT)
        {
            setContentView(R.layout.device_setup_instruction_orbit);
            setActionBar(getString(R.string.setup_title));
            TextView cancel = (TextView) findViewById(R.id.setup_instruction_orbit_cancel);
            TextView next = (TextView) findViewById(R.id.setup_instruction_orbit_next);
            cancel.setOnClickListener(this);
            next.setOnClickListener(this);
        }
        else if(mDeviceType ==CommonConstants.DEVICE_TYPE_73)
        {
            if(mCurrentTransportMode == TransportMode.LAN)
            {
                setContentView(R.layout.device_setup_instruction_73);
                setActionBar(getString(R.string.setup_title));
                TextView cancel = (TextView) findViewById(R.id.setup_instruction_73_cancel);
                TextView next = (TextView) findViewById(R.id.setup_instruction_73_next);
                cancel.setOnClickListener(this);
                next.setOnClickListener(this);
            }
            else
            {
                setContentView(R.layout.device_setup_instruction_73_wifi);
                setActionBar(getString(R.string.setup_title));
                TextView cancel = (TextView) findViewById(R.id.setup_instruction_73_wifi_cancel);
                TextView next = (TextView) findViewById(R.id.setup_instruction_73_wifi_next);
                cancel.setOnClickListener(this);
                next.setOnClickListener(this);

            }
        }

        else if(mDeviceType == CommonConstants.DEVICE_TYPE_72)
        {
            if(mCurrentTransportMode == TransportMode.LAN)
            {
                setContentView(R.layout.device_setup_instruction_72_lan);
                setActionBar(getString(R.string.setup_title));
                TextView cancel = (TextView) findViewById(R.id.setup_instruction_72_lan_cancel);
                TextView next = (TextView) findViewById(R.id.setup_instruction_72_lan_next);
                cancel.setOnClickListener(this);
                next.setOnClickListener(this);
            }
            else if(mCurrentTransportMode == TransportMode.WI_FI_HUBBLE)
            {
                setContentView(R.layout.device_setup_instruction_72_wifi);
                setActionBar(getString(R.string.setup_title));
                TextView cancel = (TextView) findViewById(R.id.setup_instruction_72_wifi_cancel);
                TextView next = (TextView) findViewById(R.id.setup_instruction_72_wifi_next);
                cancel.setOnClickListener(this);
                next.setOnClickListener(this);
            }
        }

    }

    private void showPairInstruction()
    {

        if(mDeviceType==CommonConstants.DEVICE_TYPE_73)
        {
            setContentView(R.layout.device_setup_pair_73);
            setActionBar(getString(R.string.setup_title));
            Button cancel = (Button) findViewById(R.id.setup_pair_73_cancel);
            Button detectCamera = (Button) findViewById(R.id.setup_pair_73_next);
            cancel.setOnClickListener(this);
            detectCamera.setOnClickListener(this);
        }
        else if(mDeviceType==CommonConstants.DEVICE_TYPE_OTHER)
        {
            setContentView(R.layout.device_setup_pair);
            setActionBar(getString(R.string.setup_title));
            Button cancel = (Button) findViewById(R.id.setup_instruction_cancel);
            Button detectCamera = (Button) findViewById(R.id.setup_detect_camera);
            TextView showPairHelp = (TextView) findViewById(R.id.setup_show_pair_instruction);
            showPairHelp.setOnClickListener(this);
            cancel.setOnClickListener(this);
            detectCamera.setOnClickListener(this);
        }
        else if(mDeviceType == CommonConstants.DEVICE_TYPE_72)
        {
            setContentView(R.layout.device_setup_pair_72);
            setActionBar(getString(R.string.setup_title));
            Button cancel = (Button) findViewById(R.id.setup_pair_72_cancel);
            Button detectCamera = (Button) findViewById(R.id.setup_pair_72_next);
            cancel.setOnClickListener(this);
            detectCamera.setOnClickListener(this);
        }

    }

    private void showPairHelp(){
        isPairingHelpShown=true;
        setContentView(R.layout.device_setup_pair_help);
        TextView tv_title = (TextView) findViewById(R.id.tv_toolbar_title);
        tv_title.setText(getString(R.string.setup_title));
        ImageView tv_back=(ImageView)findViewById(R.id.tv_toolbar_back) ;
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPairingHelpShown=false;
                showPairInstruction();
            }
        });

    }

    private void searchDevice()
    {
        // started WiFi Scanning procedure
        //mWiFiScanCounter = 1;
        Configuration configuration = null;

        setContentView(R.layout.device_setup_detecting_camera);
        setActionBar(getString(R.string.setup_device));
        //starting animation
        ImageView detectCameraImage=(ImageView)findViewById(R.id.detect_camera_img);

        if(mDeviceType==CommonConstants.DEVICE_TYPE_ORBIT)
        {
            detectCameraImage.setBackgroundResource(R.drawable.detecting_orbit_anim);
        }
        else if(mDeviceType==CommonConstants.DEVICE_TYPE_73)
        {
            detectCameraImage.setBackgroundResource(R.drawable.detecting_73_anim);
            configuration = new Configuration();
            configuration.setModeEnable(false);
        }
        else if(mDeviceType==CommonConstants.DEVICE_TYPE_72)
        {
            detectCameraImage.setBackgroundResource(R.drawable.detecting_72_anim);
        }
        else
        {
            detectCameraImage.setBackgroundResource(R.drawable.detecting_camera_anim);
        }


        animationDrawable = (AnimationDrawable) detectCameraImage.getBackground();
        animationDrawable.start();

        adapter = new ArrayAdapter(getApplicationContext(), R.layout.device_setup_devicelist_item, R.id.device_item);
        isCameraListViewLoaded=false;

        try
        {
            if(mDeviceType==CommonConstants.DEVICE_TYPE_ORBIT)
            {
                P2PDiscovery.getInstance().findNearbyDevices(mCurrentTransportMode, 25000,configuration);
            }
            else if(mCurrentTransportMode == TransportMode.LAN){
                P2PDiscovery.getInstance().findNearbyDevices(mCurrentTransportMode, 15000, configuration);
            }
            else {
                P2PDiscovery.getInstance().findNearbyDevices(mCurrentTransportMode, 10000, configuration);
            }
        }
        catch (BaseException e)
        {
            Log.d(TAG, "Exception while scanning for devices");
            e.printStackTrace();
        }
    }

    private void showDevices()
    {
        Log.d(TAG,"showDevices");

        if(!isCameraListViewLoaded)
        {
            isCameraListViewLoaded=true;

            setContentView(R.layout.device_setup_camera_list);
            setActionBar(getString(R.string.setup_device));

            mLoadingAnimationView = (ImageView) findViewById(R.id.progress_image);
            final Button searchAgainButton=(Button)findViewById(R.id.detect_camera_again_btn);
            searchAgainButton.setOnClickListener(this);


            final ListView lv_selectdevice = (ListView) findViewById(R.id.camera_list);
            lv_selectdevice.setAdapter(adapter);

            lv_selectdevice.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    searchAgainButton.setVisibility(View.INVISIBLE);
                    RemoteDevice remoteDevice = (RemoteDevice) lv_selectdevice.getItemAtPosition(position);
                    Log.d(TAG, "selected device from list:"+ remoteDevice);
                    selectDevice(remoteDevice);
                }
            });
        }
        else
        {
            adapter.notifyDataSetChanged();
        }
    }

    private void selectDevice(RemoteDevice remoteDevice )
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String accessToken=settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");

        //mSelectedRemoteDevice = remoteDevice;
        //mSelectedRemoteDevice becomes null randomly, hence initializing remote device..need to observe for null
        mSelectedRemoteDevice= new RemoteDevice();
        mSelectedRemoteDevice.setName(remoteDevice.getName());
        mSelectedRemoteDevice.setAddress(remoteDevice.getAddress());
        mSelectedRemoteDevice.setMacAddress(remoteDevice.getMacAddress());
        mSelectedRemoteDevice.setSecurityType(remoteDevice.getSecurityType());
        mSelectedRemoteDevice.setType(remoteDevice.getType());

        if(BuildConfig.DEBUG)
            Log.d(TAG, "selectedDevice:"+ mSelectedRemoteDevice.getName());

        P2PDiscovery.getInstance().stopDiscoveryProcess(mCurrentTransportMode);

        Configuration configuration = new Configuration();
        configuration.setSsid("\"" + mSelectedRemoteDevice.getName() + "\"");
        configuration.accessToken = accessToken;
        configuration.setIpAddress(mSelectedRemoteDevice.getAddress());

        if(mDeviceType == CommonConstants.DEVICE_TYPE_72) {
            configuration.setTLSSupport(true);
        }
        else {
            configuration.setTLSSupport(isSupportTLS(mSelectedRemoteDevice.getName()));
        }


        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.image_rotate);
        mLoadingAnimationView.setVisibility(View.VISIBLE);
        mLoadingAnimationView.startAnimation(animation);

        P2PDiscovery.getInstance().configureDevice(mCurrentTransportMode, configuration);

        mConfigTimer=new CountDownTimer(CONFIG_TIMEOUT,CONFIG_TIMEOUT)
        {
            public void onTick(long millisUntilFinished)
            {
            }

            public void onFinish()
            {
                Log.d(TAG, "Configuration timeout");
                P2PDiscovery.getInstance().unRegisterDiscoveryCallback(mCurrentTransportMode);
                showErrorScreen();
            }
        };

        mConfigTimer.start();

    }

    private static boolean isSupportTLS(String cameraSSID) {
        boolean result = false;
        if(!TextUtils.isEmpty(cameraSSID)) {
            result = cameraSSID.startsWith("CameraHD-0068");
        }
        return result;
    }

    private void showNoDeviceFound(){
        setContentView(R.layout.device_setup_network_error);
        setActionBar(getString(R.string.setup_device));
        TextView errorText=(TextView)findViewById(R.id.error_text);
        errorText.setText(getString(R.string.setup_device_error));
        Button tryAgainButton=(Button)findViewById(R.id.detect_try_again);
        tryAgainButton.setOnClickListener(this);
    }

    private void deviceDetected()
    {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.device_setup_camera_detected);
        setActionBar(getString(R.string.setup_device));
        TextView cameraName=(TextView)findViewById(R.id.camera_name);

        // we don't know exact reason that what's reason parameter value is null
        // prefer that we need to verify again with more log.
        if(mSelectedRemoteDevice != null) {
            cameraName.setText(mSelectedRemoteDevice.getName());

            if (BuildConfig.DEBUG)
                Log.d(TAG, "selected camera:" + mSelectedRemoteDevice.getName());
        }

        Button nextButton=(Button)findViewById(R.id.camera_detected_next_btn);
        nextButton.setOnClickListener(this);

    }

    private void showErrorScreen(){
        setContentView(R.layout.device_setup_error);
        setActionBar(getString(R.string.setup_device));
        TextView errorText=(TextView)findViewById(R.id.error_msg);
        errorText.setText(getString(R.string.setup_config_error));
        Button tryAgainButton=(Button)findViewById(R.id.connect_try_again);
        tryAgainButton.setOnClickListener(this);
    }


    @Override
    public void onConnectStatus(NetworkConnectEvent networkEvent, RemoteDevice remoteDevice) {
        Log.d(TAG, "onConnectStatus");

    }


    @Override
    public void onDeviceFound(final List<RemoteDevice> remoteDevices)
    {
        Log.d(TAG, "onDeviceFound");

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (remoteDevices.size() > 0)
                {
                    for (RemoteDevice remoteDevice : remoteDevices)
                    {
                        String deviceName = remoteDevice.getName();

                        if(BuildConfig.DEBUG)
                            Log.d(TAG,"found device name :- " + deviceName + " device type :- " + mDeviceType);

                        if (deviceName != null)
                        {
                            List<String> deviceSSIDName = DEVICE_SSID_STARTS_UNO;

                            if (mDeviceType==CommonConstants.DEVICE_TYPE_ORBIT)
                            {
                                deviceSSIDName = DEVICE_SSID_STARTS_ORBIT;
                            }
                            else if(mDeviceType==CommonConstants.DEVICE_TYPE_73)
                            {
                                deviceSSIDName= DEVICE_SSID_STARTS_73;
                            }

                            for(int counter = 0; counter < deviceSSIDName.size(); counter++)
                            {
                                if(deviceName.startsWith(deviceSSIDName.get(counter)))
                                {
                                    if (adapter != null && adapter.getPosition(remoteDevice) < 0)
                                    {
                                        //73 should now be listed for "other cameras"
                                        if(mDeviceType==CommonConstants.DEVICE_TYPE_OTHER &&
                                                (deviceName.startsWith("CameraHD-0173") ||
                                                        deviceName.startsWith("CameraHD-0073")))
                                            continue;

                                        adapter.add(remoteDevice);
                                        break;
                                    }
                                }
                            }

                        }
                    }
                    if (adapter != null && adapter.getCount() > 0)
                    {
                        showDevices();
                    }
                }

                if(adapter != null && adapter.getCount() == 0 /*&& mWiFiScanCounter >= WIFI_SCAN_LIMIT*/)
                {
                    showNoDeviceFound();
                }

            }
        });

        //disable multiple retries due to stack overflow error
        /*else
        {
            try
            {
                P2PDiscovery.getInstance().findNearbyDevices(TransportMode.WI_FI_HUBBLE, 7000 * mWiFiScanCounter);
            }
            catch (BaseException e)
            {
                Log.d(TAG, "Exception while scanning for devices");
                e.printStackTrace();
            }
            mWiFiScanCounter++;
        }*/

    }

    public void onConfigStatus(ConfigurationEvent configurationEvent, final LocalDevice tlsDevice)
    {
        Log.d(TAG, "onConfigStatus");
        if(mConfigTimer!=null)
            mConfigTimer.cancel();
        switch (configurationEvent) {
            case SUCCESS:
                Log.d(TAG, "Config success");
                Thread mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int k = 10;

                        while (k-- > 0) {
                            try {

                                if (mDeviceType == CommonConstants.DEVICE_TYPE_72) {
                                    LocalDevice localDevice = null;
                                    if (mCurrentTransportMode == TransportMode.LAN) {
                                        localDevice = new LocalDevice(BaseContext.getBaseContext(), mSelectedRemoteDevice.getAddress(), null);
                                    } else {
                                        localDevice = new LocalDevice(BaseContext.getBaseContext(), CommonConstants.DEFAULT_DEVICE_IP, null);
                                    }
                                    Log.d(TAG, "Trying TLS communication");

                                    TLSResponse res = localDevice.performTestBlock();

                                    // in some device, having ip address does not mean camera is ready for connecting.
                                    // in this case, we need waiting a bit

                                    if (res != null && res.getCode() == TLSPSK.MBEDTLS_ERR_NET_CONNECT_FAILED) {
                                        Log.d(TAG, "Maybe camera is not ready for connecting. Need waiting a bit ...");
                                        try {
                                            Thread.sleep(3000);
                                        }
                                        catch (InterruptedException e) {
                                        }

                                        res = localDevice.performTestBlock();
                                        Log.d(TAG, "Perform test after waiting: response code: " + (res != null ? res.getCode() : "null"));
                                    }
                                    regId = localDevice.sendCommandAndGetValue("action=command&command=get_udid");


                                } else if (mCurrentTransportMode == TransportMode.LAN) {
                                    regId = HTTPRequestSendRecvTask.getUdid(mSelectedRemoteDevice
                                        .getAddress(), CommonConstants.DEVICE_PORT, CommonConstants.DEFAULT_BASIC_AUTH_USR, CommonConstants.DEFAULT_BASIC_AUTH_PASS);
                                } else if (tlsDevice != null && tlsDevice.isSupportTLS()) {
                                    Log.w(TAG, "Local device is not null, use it to setup");
                                    regId = tlsDevice.getUuid();
                                } else {
                                    regId = HTTPRequestSendRecvTask
                                        .getUdid(CommonConstants.DEFAULT_DEVICE_IP, CommonConstants.DEVICE_PORT, CommonConstants.DEFAULT_BASIC_AUTH_USR, CommonConstants.DEFAULT_BASIC_AUTH_PASS);
                                }
                                Log.d(TAG, "RegID: " + regId);
                                if (regId != null) {
                                    break;
                                } else {
                                    Log.d(TAG, "retry get Registrationid");
                                }
                            }
                            catch (ConnectException e) {
                                e.printStackTrace();
                            }
                        }

                        if (regId != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mLoadingAnimationView != null) {
                                        mLoadingAnimationView.clearAnimation();
                                        mLoadingAnimationView.setVisibility(View.GONE);
                                    }
                                    deviceDetected();
                                    Log.d(TAG, "deviceDetected");
                                }
                            });
                        } else if (regId == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    //Toast.makeText(CameraSetUpActivity.this, "Setup failed, please try again", Toast.LENGTH_SHORT).show();
                                    if (mLoadingAnimationView != null) {
                                        mLoadingAnimationView.clearAnimation();
                                        mLoadingAnimationView.setVisibility(View.GONE);

                                    }
                                    showErrorScreen();

                                }
                            });
                        }
                    }
                });

                mThread.start();
                break;
            case FAILURE:
                if (mLoadingAnimationView != null) {
                    mLoadingAnimationView.clearAnimation();
                    mLoadingAnimationView.setVisibility(View.GONE);

                }
                Toast.makeText(CameraSetUpActivity.this, "Setup failed, please try again", Toast.LENGTH_SHORT).show();
                showSetUpInstructions();
                break;
        }
    }

    @Override
    public void onConfigStatus(ConfigurationEvent configurationEvent) {
        onConfigStatus(configurationEvent, null);
    }

    @Override
    public void onAuthorizationEvent(AuthorizationEvent authorizationEvent, Object result) {
        Log.d(TAG, "onAuthorizationEvent");
    }

    @Override
    public void onDataAvailable(String property, Object value) {
        Log.d(TAG, "onDataAvailable");
    }

    @Override
    public void onDeviceRegistrationOnServer() {
        Log.d(TAG, "onDeviceRegistration");
    }

    private void stopP2pService() {
        if (P2pSettingUtils.hasP2pFeature()) {
            P2pUtils.stopP2pService(getApplicationContext());
        }
    }



}

