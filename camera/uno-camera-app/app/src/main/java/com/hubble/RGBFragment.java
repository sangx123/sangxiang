package com.hubble;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hubbleconnected.camera.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.sensor.helper.IWatcherMqttPublishAction;
import com.sensor.helper.MqttHandler;


/**
 * Created by CVision on 11/18/2015.
 */
public class RGBFragment extends Fragment implements IWatcherMqttPublishAction {

    private String macAddress;
    private ColorPicker colorPicker;
    private EditText edtColor;
    private Button sendColor;

    private String topic = "%s/rgb";
    private String content = "{\"r\":%s,\"g\":%s,\"b\":%s}";
    private MqttHandler mqttHandle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.change_rbg_layout, container, false);

        colorPicker = (ColorPicker) view.findViewById(R.id.color_picker);
        edtColor = (EditText) view.findViewById(R.id.edt_color);
        sendColor = (Button) view.findViewById(R.id.btn_send);
        colorPicker.setShowOldCenterColor(false);
        connectToMqttServer();

        colorPicker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                String hexColor = String.format("#%06X", (0xFFFFFF & color));
                Log.e("Last color : ", hexColor);
                edtColor.setText(hexColor);
            }
        });

        View.OnClickListener sendColorListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                senColorMqtt();
                sendColor.setEnabled(false);
            }
        };

        sendColor.setOnClickListener(sendColorListener);

        return view;
    }

    public void setmMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void connectToMqttServer(){
        String clientId = "00" + getDeviceId();;
        if (macAddress != null) {
            topic = String.format(topic, macAddress);
        }
        mqttHandle = MqttHandler.getInstance(getContext());
        mqttHandle.setPublishAction(this);
        mqttHandle.connect(clientId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mqttHandle.disconnect();
    }

    private void senColorMqtt(){
        int color = colorPicker.getColor();
        String msg = String.format(content, String.valueOf(Color.red(color)), String.valueOf(Color.green(color)), String.valueOf(Color.blue(color)));
        mqttHandle.publish(topic, msg, false, 1);
    }

    @Override
    public void onCompletedMqttSendColor() {
        Toast.makeText(getActivity(), "Send Color Success", Toast.LENGTH_LONG).show();
        sendColor.setEnabled(true);
    }

    @Override
    public void onFailMqttSendColor() {
        Toast.makeText(getActivity(), "Send Color Fail", Toast.LENGTH_LONG).show();
        sendColor.setEnabled(true);
    }

    private String getDeviceId() {
        String deviceId = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return deviceId;
    }
}
