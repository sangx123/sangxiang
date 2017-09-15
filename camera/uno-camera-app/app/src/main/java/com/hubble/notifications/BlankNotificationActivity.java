package com.hubble.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.registration.PublicDefine;
import com.nxcomm.blinkhd.ui.MainActivity;

/**
 * Created by CVision on 1/15/2016.
 */
public class BlankNotificationActivity extends FragmentActivity {

    private String registrationId;
    public static final String EVENT_TYPE = "event_type";
    public static final String EVENT_SIZE = "event_size";
    private int eventType;
    private int eventSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        registrationId = extras.getString(MainActivity.EXTRA_DEVICE_REGISTRATION_ID, "");
        eventType = extras.getInt(EVENT_TYPE);
        eventSize = extras.getInt(EVENT_SIZE);

        Intent intent = new Intent(BlankNotificationActivity.this, MainActivity.class);
        Device selectedDevice = DeviceSingleton.getInstance().getDeviceByRegId(registrationId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (eventType == 29 || eventType == 30 || eventType == 31) {
            intent.putExtra(MainActivity.EXTRA_DIRECTLY_TO_DEVICE, true);
            if (selectedDevice != null) {
                DeviceSingleton.getInstance().setSelectedDevice(selectedDevice);
                HubbleApplication.AppConfig.putBoolean(PublicDefine.PREFS_SHOULD_GO_TO_CAMERA, true);
            }
        } else {
            if (eventSize == 1) {
                intent.putExtra(MainActivity.EXTRA_DIRECTLY_TO_DEVICE, true);
            } else {
                intent.putExtra(MainActivity.EXTRA_DIRECTLY_TO_EVENT_LOG, true);
            }
        }

        startActivity(intent);
        finish();
    }
}
