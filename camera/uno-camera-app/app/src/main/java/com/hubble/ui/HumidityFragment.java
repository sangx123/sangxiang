package com.hubble.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hubble.devcomm.Device;
import com.hubble.helpers.AsyncPackage;
import com.hubbleconnected.camera.R;


/**
 * Created by CVision on 3/1/2016.
 */
public class HumidityFragment extends Fragment {

    private TextView tvHumidity;
    private ProgressBar prgHumidity;
    private Device device;

    public HumidityFragment() {

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.humidity_fragment, container, false);

        tvHumidity = (TextView) view.findViewById(R.id.tv_humidity);
        prgHumidity = (ProgressBar) view.findViewById(R.id.prg_humidity);
//        prgHumidity.getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getHumidity();
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void getHumidity() {
        AsyncPackage.doInBackground(new Runnable() {
            @Override
            public void run() {
                if (device != null) {
                    try {
                        final Pair<String, Object> response = device.sendCommandGetValue("get_humidity", null, null);
                        if (response !=null ) {
                            if (response.first.equals("get_humidity")) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvHumidity.setText(response.second + "%");
                                        prgHumidity.setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvHumidity.setText(getString(R.string.error));
                                        prgHumidity.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvHumidity.setText(getString(R.string.error));
                                    prgHumidity.setVisibility(View.GONE);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

}
