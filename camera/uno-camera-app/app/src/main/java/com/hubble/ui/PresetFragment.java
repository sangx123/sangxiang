package com.hubble.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actor.model.Direction;
import com.hubble.HubbleApplication;
import com.hubble.PanTiltActorJava;
import com.hubble.SecureConfig;
import com.hubble.devcomm.Device;
import com.hubble.helpers.AsyncPackage;
import com.hubbleconnected.camera.R;


/**
 * Created by CVision on 12/15/2015.
 */
public class PresetFragment extends Fragment implements View.OnLongClickListener, View.OnClickListener {

    public final static String TAG = "PresetFragment";
    private SecureConfig settings = HubbleApplication.AppConfig;
    private Device mDevice;
    private PanTiltActorJava actor;
    private Activity mActivity;
    private RelativeLayout layoutPreset;
    private TextView tvPreset1;
    private TextView tvPreset2;
    private TextView tvPreset3;
    private TextView tvPreset4;
    private TextView tvPreset5;
    private RelativeLayout moveCenter;
    private boolean block;
    private boolean longClick = false;
    private RelativeLayout layoutPoints;
    private TextView tvLoading;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.preset_layout, container, false);
        if (mDevice != null) {
            if (actor == null) {
                long queueTime = 2000;
                long stopTime = 600;
                Log.e("actor", "set");
                actor = new PanTiltActorJava(mDevice, queueTime, stopTime) {
                    @Override
                    public void onPresetFail(final int code) {
                        block = false;
                        try {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG, "Preset error code : " + code);
                                    if (code == -2) {
                                        Toast.makeText(mActivity, getResources().getString(R.string.preset_camera_busy), Toast.LENGTH_LONG).show();
                                    } else if (code == -99) {
                                        Toast.makeText(mActivity, getResources().getString(R.string.not_support_SOC), Toast.LENGTH_LONG).show();
                                    } else if (code == -3) {
                                        Toast.makeText(mActivity, getResources().getString(R.string.point_not_set), Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(mActivity, getResources().getString(R.string.preset_fail), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onPanBoundary(Direction direction) {

                    }

                    @Override
                    public void onPresetSuccess() {
                        block = false;
                        try {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity, getResources().getString(R.string.preset_success), Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onPanSuccess(Direction direction) {
                    }

                    @Override
                    public void onPanFailure(Direction direction) {

                    }

                    @Override
                    public void onStop() {

                    }

                    @Override
                    public void onFinishCenter() {
                    }

                    @Override
                    public void onFailCenter() {
                    }
                };
            }
        }

        layoutPreset = (RelativeLayout) view.findViewById(R.id.layout_preset);
        layoutPoints = (RelativeLayout) view.findViewById(R.id.points);
        tvLoading = (TextView) view.findViewById(R.id.tv_preset_loading);
        tvPreset1 = (TextView) view.findViewById(R.id.preset_1);
        tvPreset2 = (TextView) view.findViewById(R.id.preset_2);
        tvPreset3 = (TextView) view.findViewById(R.id.preset_3);
        tvPreset4 = (TextView) view.findViewById(R.id.preset_4);
        tvPreset5 = (TextView) view.findViewById(R.id.preset_5);
        moveCenter = (RelativeLayout) view.findViewById(R.id.move_center);

        tvPreset1.setOnLongClickListener(this);
        tvPreset2.setOnLongClickListener(this);
        tvPreset3.setOnLongClickListener(this);
        tvPreset4.setOnLongClickListener(this);
        tvPreset5.setOnLongClickListener(this);

        tvPreset1.setOnClickListener(this);
        tvPreset2.setOnClickListener(this);
        tvPreset3.setOnClickListener(this);
        tvPreset4.setOnClickListener(this);
        tvPreset5.setOnClickListener(this);
        moveCenter.setOnClickListener(this);

        getListPreset();

        return view;
    }

    public void getListPreset() {
        tvLoading.setVisibility(View.VISIBLE);
        layoutPoints.setVisibility(View.INVISIBLE);

        AsyncPackage.doInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    final Pair<String, Object> response = mDevice.sendCommandGetValue("get_preset_support", null, null);
                    if (response != null) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layoutPoints.setVisibility(View.VISIBLE);
                                tvLoading.setVisibility(View.GONE);

                                Log.e(TAG, "list preset : " + response);
                                String second = response.second.toString();
                                if (second.equals("-1")) {
                                    try {
                                        Toast.makeText(mActivity, getResources().getString(R.string.get_list_preset_error), Toast.LENGTH_LONG).show();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (second.equals("-99")) {
                                    try {
                                        Toast.makeText(mActivity, getResources().getString(R.string.not_support_SOC), Toast.LENGTH_LONG).show();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                } else if (second.replace("5", "").equals("")) {
                                } else if (second.startsWith("5_")) {
                                    second = second.substring(2, second.length());
                                    String[] list = second.split("_");
                                    if (list.length != 0) {
                                        initData(list);
                                    }
                                }
                            }
                        });
                    } else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(mActivity, getResources().getString(R.string.get_list_preset_error), Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void setDevice(Device mDevice) {
        this.mDevice = mDevice;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.preset_1:
                movePreset("1");
                break;
            case R.id.preset_2:
                movePreset("2");
                break;
            case R.id.preset_3:
                movePreset("3");
                break;
            case R.id.preset_4:
                movePreset("4");
                break;
            case R.id.preset_5:
                movePreset("5");
                break;
            case R.id.move_center:
                setPresetPoint("0", moveCenter);
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.preset_1:
                setPresetPoint("1", tvPreset1);
                break;
            case R.id.preset_2:
                setPresetPoint("2", tvPreset2);
                break;
            case R.id.preset_3:
                setPresetPoint("3", tvPreset3);
                break;
            case R.id.preset_4:
                setPresetPoint("4", tvPreset4);
                break;
            case R.id.preset_5:
                setPresetPoint("5", tvPreset5);
                break;
        }
        return false;
    }

    private void setPresetPoint(final String point, final View tv) {
        longClick = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Pair<String, Object> response = mDevice.sendCommandGetValue("set_preset", point, null);
                    if (response != null) {
                        try {
                            Log.e(TAG, "set preset - response : " + response + " - second : " + response.second);
                            if ((Integer) response.second == 0) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (tv.getId() == R.id.move_center) {
                                            Toast.makeText(mActivity, getResources().getString(R.string.succeeded), Toast.LENGTH_LONG).show();
                                        } else {
                                            tv.setBackgroundResource(R.drawable.state_button_pressed_preset_blue);
                                            Toast.makeText(mActivity, getResources().getString(R.string.set_preset_success), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (tv.getId() == R.id.move_center) {
                                            Toast.makeText(mActivity, getResources().getString(R.string.failed), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(mActivity, getResources().getString(R.string.set_preset_fail), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (tv.getId() == R.id.move_center) {
                                    Toast.makeText(mActivity, getResources().getString(R.string.failed), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(mActivity, getResources().getString(R.string.set_preset_fail), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tv.getId() == R.id.move_center) {
                                Toast.makeText(mActivity, getResources().getString(R.string.failed), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mActivity, getResources().getString(R.string.set_preset_fail), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private void movePreset(String point) {
        if (!longClick) {
            if (!block) {
                block = true;
                actor.sendPanPreset(point);
            }
        }

        longClick = false;
    }

    private void initData(String[] pid){
        for (String id: pid) {
            switch (id) {
                case "1":
                    tvPreset1.setBackgroundResource(R.drawable.state_button_pressed_preset_blue);
                    break;

                case "2":
                    tvPreset2.setBackgroundResource(R.drawable.state_button_pressed_preset_blue);
                    break;

                case "3":
                    tvPreset3.setBackgroundResource(R.drawable.state_button_pressed_preset_blue);
                    break;

                case "4":
                    tvPreset4.setBackgroundResource(R.drawable.state_button_pressed_preset_blue);
                    break;

                case "5":
                    tvPreset5.setBackgroundResource(R.drawable.state_button_pressed_preset_blue);
                    break;
            }

        }
    }
}
