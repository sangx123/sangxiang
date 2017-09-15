package com.hubble.motioncalibration;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.hubble.HubbleApplication;
import com.hubble.devcomm.Device;
import com.hubbleconnected.camera.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMotionCalibrationListener} interface
 * to handle interaction events.
 * Use the {@link MotionCalibrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MotionCalibrationFragment extends Fragment implements View.OnClickListener, OnChartValueSelectedListener {

  private OnMotionCalibrationListener mListener;
  private static final String TAG = MotionCalibrationFragment.class.getSimpleName();
  private static final String START_CLB = "motion_start_clb";
  private static final String STOP_CLB = "motion_stop_clb";
  private static final String GET_CLB = "motion_get_clb";
  private static final String SET_CLB = "motion_set_clb";

  private static final String START_CLB_CMD = "http://%s:80/?action=command&command=" + START_CLB + "&setup=%s";
  private static final String STOP_CLB_CMD = "http://%s:80/?action=command&command=" + STOP_CLB;
  private static final String GET_CLB_CMD = "http://%s:80/?action=command&command=" + GET_CLB;
  private static final String SET_CLB_CMD = "http://%s:80/?action=command&command=" + SET_CLB + "&setup=%d,%d";

  private static final long CLB_DURATION = 30 * 1000;
  private ImageView startButton;
  private TextView motionIntThresholdTextView, motionOutThresholdTextView;
  private volatile boolean started = false;
  private Thread receivedThread;
  private LineChart mChart;
  private int xAxis = 0;

  public MotionCalibrationFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment MotionCalibrationFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static MotionCalibrationFragment newInstance() {
    MotionCalibrationFragment fragment = new MotionCalibrationFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    started = false;
    View layoutView = inflater.inflate(R.layout.fragment_motion_calibration, container, false);

    startButton = (ImageView) layoutView.findViewById(R.id.startCLBButton);
    startButton.setOnClickListener(this);

    motionIntThresholdTextView = (TextView) layoutView.findViewById(R.id.motionInLevelTextView);
    motionOutThresholdTextView = (TextView) layoutView.findViewById(R.id.motionOutLevelTextView);

    motionOutThresholdTextView.setEnabled(false);
    motionIntThresholdTextView.setEnabled(false);

    mChart = (LineChart) layoutView.findViewById(R.id.chart1);

    motionIntThresholdTextView.setOnClickListener(this);
    motionOutThresholdTextView.setOnClickListener(this);
    setupChart();

    return layoutView;
  }

  private void setupChart() {

    //mChart.setOnChartValueSelectedListener(this);

    // no description text
    mChart.setDescription("");
    mChart.setNoDataTextDescription(HubbleApplication.AppContext.getString(R.string.click_on_play_button_to_start_analyzing));
    mChart.setOnChartValueSelectedListener(this);
    // enable touch gestures
    mChart.setTouchEnabled(true);

    // enable scaling and dragging
    mChart.setDragEnabled(true);
    mChart.setScaleEnabled(true);
    mChart.setDrawGridBackground(false);
    mChart.setAutoScaleMinMaxEnabled(true);
    // if disabled, scaling can be done on x- and y-axis separately
    mChart.setPinchZoom(true);

    // set an alternative background color
    mChart.setBackgroundColor(Color.LTGRAY);
    LineData data = new LineData();
    data.setValueTextColor(Color.WHITE);
    // add empty data
    mChart.setData(data);
    // get the legend (only possible after setting data)
    Legend l = mChart.getLegend();

    // modify the legend ...
    // l.setPosition(LegendPosition.LEFT_OF_CHART);
    l.setForm(Legend.LegendForm.LINE);
    l.setTextColor(Color.WHITE);

    XAxis xl = mChart.getXAxis();
    xl.setTextColor(Color.WHITE);
    xl.setDrawGridLines(false);
    xl.setAvoidFirstLastClipping(true);
    //xl.setSpaceBetweenLabels(5);
    xl.setEnabled(true);

    YAxis leftAxis = mChart.getAxisLeft();
    leftAxis.setTextColor(Color.WHITE);
    leftAxis.setStartAtZero(false);
    leftAxis.setDrawGridLines(true);

    YAxis rightAxis = mChart.getAxisRight();
    rightAxis.setEnabled(false);
  }

  private LineDataSet createSet() {

    LineDataSet set = new LineDataSet(null, "Threshold");
    set.setAxisDependency(YAxis.AxisDependency.LEFT);
    set.setColor(ColorTemplate.getHoloBlue());
    set.setCircleColor(Color.WHITE);
    set.setLineWidth(1f);
    set.setCircleSize(1f);
    set.setFillAlpha(65);
    set.setFillColor(ColorTemplate.getHoloBlue());
    set.setHighLightColor(Color.rgb(244, 117, 117));
    set.setValueTextColor(Color.WHITE);
    set.setValueTextSize(9f);
    set.setDrawValues(false);
    return set;
  }

  private void addEntry(UDPData udpData) {
    int xvalue = udpData.getThreshold();
  /*
    LineData data = mChart.getData();

    if (data != null) {

      LineDataSet set = data.getDataSetByIndex(0);
      // set.addEntry(...); // can be called as well

      if (set == null) {
        set = createSet();
        data.addDataSet(set);
      }

      // add a new x-value first
      data.addXValue(xAxis + "");
      xAxis++;

      data.addEntry(new Entry(xvalue, set.getEntryCount()), 0);
      Highlight highlight = new Highlight(xAxis, 0);

      mChart.highlightValue(highlight);
      // let the chart know it's data has changed
      mChart.notifyDataSetChanged();

      // limit the number of visible entries
      mChart.setVisibleXRangeMaximum(120);
      // mChart.setVisibleYRange(30, AxisDependency.LEFT);

      // move to the latest entry
      mChart.moveViewToX(data.getXValCount() - 121);

      // this automatically refreshes the chart (calls invalidate())
      // mChart.moveViewTo(data.getXValCount()-7, 55f,
      // AxisDependency.LEFT);
    }
    */
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();
    Log.i(TAG, "On detach");
    if (started) {
      started = false;
      silentStopCLB();
      if (receivedThread != null && receivedThread.isAlive()) {
        Log.i(TAG, "Stop udp receiving thread");
        receivedThread.interrupt();
      }
    }
    mListener = null;
  }

  public void setMotionCablirationListener(OnMotionCalibrationListener listener) {
    this.mListener = listener;
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.startCLBButton) {
      if (started) {
        stopCLB();
      } else {
        startCLB();
      }
    } else if (v.getId() == R.id.motionInLevelTextView) {
      showInputDialog(motionIntThresholdTextView);
    } else if (v.getId() == R.id.motionOutLevelTextView) {
      showInputDialog(motionOutThresholdTextView);
    }
  }

  public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
    //Log.i(TAG, "Selected: " + e.getVal());
  }

  @Override
  public void onValueSelected(Entry e, Highlight h) {

  }

  @Override
  public void onNothingSelected() {

  }

  public interface OnMotionCalibrationListener {
    // TODO: Update argument type and name
    void onMotionCalibrationEvent(MotionCalibrationEvent motionCalibrationEvent);

    Device getSelectedDevice();
  }

  public static class MotionCalibrationEvent {
    public static final int START_EVENT = 1;
    public static final int STOP_EVENT = 2;
    private int eventCode;
    private Object extraData;

    public MotionCalibrationEvent(int eventCode, Object extraData) {
      this.eventCode = eventCode;
      this.extraData = extraData;
    }

    public int getEventCode() {
      return eventCode;
    }

    public Object getExtraData() {
      return extraData;
    }
  }

  private class UpdateMotionThresholdRunnable implements Runnable {
    private int motionInThreshold, motionOutThreshold;

    public UpdateMotionThresholdRunnable(int motionInThreshold, int motionOutThreshold) {
      this.motionInThreshold = motionInThreshold;
      this.motionOutThreshold = motionOutThreshold;
    }

    @Override
    public void run() {
      motionIntThresholdTextView.setText(getActivity().getString(R.string.motion_in_threshold) + " " + motionInThreshold);
      motionOutThresholdTextView.setText(getActivity().getString(R.string.motion_out_threshold) + " " + motionOutThreshold);
      motionIntThresholdTextView.setTag(motionInThreshold);
      motionOutThresholdTextView.setTag(motionOutThreshold);

      motionOutThresholdTextView.setEnabled(true);
      motionIntThresholdTextView.setEnabled(true);

    }
  }

  private void showInputDialog(final TextView textView) {
    int value = (int) textView.getTag();

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = (LayoutInflater) HubbleApplication.AppContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    builder.setTitle(R.string.change_threshold);
    View view = inflater.inflate(R.layout.input_value_layout, null);
    final EditText valueEditText = (EditText) view.findViewById(R.id.valueEditText);
    valueEditText.setText(value + "");
    valueEditText.setFilters(new InputFilter[]{new InputFilterMinMax("1", "10000")});
    builder.setView(view);
    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
          // ok button
          String newValue = valueEditText.getText().toString();
          if (textView.getId() == R.id.motionInLevelTextView) {
            textView.setText(getString(R.string.motion_in_threshold) + " " + newValue);
          } else {
            textView.setText(getString(R.string.motion_out_threshold) + " " + newValue);
          }
          try {
            int newIntValue = Integer.parseInt(newValue);
            textView.setTag(newIntValue);
            applyThresholdToCamera();
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          dialog.dismiss();
        } else {
          dialog.dismiss();
        }
      }
    };
    builder.setNegativeButton(R.string.cancel, onClickListener);
    builder.setPositiveButton(R.string.ok, onClickListener);
    builder.show();
  }

  private void applyThresholdToCamera() {
    int inThreshold = (int) motionIntThresholdTextView.getTag();
    int outThreshold = (int) motionOutThresholdTextView.getTag();

    if (getActivity() != null && mListener != null) {
      Device device = mListener.getSelectedDevice();
      if (device != null && device.getProfile() != null && device.getProfile().getDeviceLocation() != null) {
        String deviceIP = device.getProfile().getDeviceLocation().localIP;
        String setThreshold = String.format(SET_CLB_CMD, deviceIP, inThreshold, outThreshold);
        Ion.with(HubbleApplication.AppContext)
            .load(setThreshold)
            .asString()
            .setCallback(new FutureCallback<String>() {
              @Override
              public void onCompleted(Exception e, String result) {
                if (e != null) {
                  Log.e(TAG, "Error when set new threshold");
                  e.printStackTrace();
                } else {
                  Log.i(TAG, "Set threshold result: " + result);
                }
              }
            });
      }
    }
  }

  @Override
  public void onStart() {
    super.onStart();

    FutureCallback<String> futureCallback = new FutureCallback<String>() {
      @Override
      public void onCompleted(Exception e, String result) {
        if (e != null) {
          Log.e(TAG, "Get motion calibration threshold error");
          e.printStackTrace();
        } else {
          if (result.startsWith(GET_CLB)) {
            String temp = result.replace(GET_CLB + ": ", "");
            String[] thresholds = temp.split(",");
            if (thresholds.length >= 2) {
              int motionInThreshold = 0;
              int motionOutThreshold = 0;
              try {
                motionInThreshold = Integer.parseInt(thresholds[0]);
                motionOutThreshold = Integer.parseInt(thresholds[1]);
                if (getActivity() != null) {
                  getActivity().runOnUiThread(new UpdateMotionThresholdRunnable(motionInThreshold, motionOutThreshold));
                } else {
                  Log.w(TAG, "getActivity return null");
                }
              } catch (Exception ex) {
                Log.i(TAG, "Get motion calibration receive weird result: " + result);
                ex.printStackTrace();
              }
            } else {
              Log.w(TAG, "Get motion calibration receive weird result: " + result);
            }
          } else {
            Log.e(TAG, "Get motion calibration receive weird result: " + result);
          }
        }
      }
    };
    if (getActivity() != null && mListener != null) {
      Device device = mListener.getSelectedDevice();
      if (device != null && device.getProfile() != null && device.getProfile().getDeviceLocation() != null) {
        String deviceIP = device.getProfile().getDeviceLocation().localIP;
        Ion.with(HubbleApplication.AppContext)
            .load(String.format(GET_CLB_CMD, deviceIP))
            .asString()
            .setCallback(futureCallback);
      } else {
        Log.w(TAG, "It seem we do not have any device");
      }
    } else {
      Log.w(TAG, "getActivity is null or mListener is null");
    }
  }

  @Override
  public void onStop() {
    super.onStop();
  }


  private void startCLB() {
    if (getActivity() != null && mListener != null) {
      Device device = mListener.getSelectedDevice();
      if (device != null && device.getProfile() != null && device.getProfile().getDeviceLocation() != null) {
        String deviceIP = device.getProfile().getDeviceLocation().localIP;
        Log.i(TAG, "Device name: " + device.getProfile().getName() + " device location: " + deviceIP);
        Log.i(TAG, "Phone ip address by read file: " + Utils.getIPAddress(true));
        Log.i(TAG, "Phone ip address by function: " + getPhoneIPAddress());
        String startClbCommand = String.format(START_CLB_CMD, deviceIP, getPhoneIPAddress());

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getActivity().getString(R.string.start_motion_calibration));
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Runnable succeedRunnable = new Runnable() {
          @Override
          public void run() {
            startButton.setImageResource(R.drawable.ic_pause_circle_outline_blue_900_24dp);
            mChart.clearValues();
            mChart.clear();
            mChart.invalidate();
            setupChart();
            xAxis = 0;
          }
        };

        final Runnable dimissDialogRunnable = new Runnable() {
          @Override
          public void run() {
            if (progressDialog != null && progressDialog.isShowing()) {
              progressDialog.dismiss();
            }
          }
        };

        FutureCallback<String> futureCallback = new FutureCallback<String>() {
          @Override
          public void onCompleted(Exception e, String result) {
            // AA-1506: sleep to allow user read message
            try {
              Thread.sleep(1200);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
            getActivity().runOnUiThread(dimissDialogRunnable);
            if (e != null) {
              Log.w(TAG, "Error when send start clb command");
              e.printStackTrace();
            } else {
              if (result != null) {
                if (result.startsWith(START_CLB)) {
                  result = result.replace(START_CLB + ": ", "");
                  Log.i(TAG, "Start clb parameter: " + result);
                  if (!result.equalsIgnoreCase("-1")) {
                    started = true;
                    startReceiveDataThread();
                    getActivity().runOnUiThread(succeedRunnable);
                  } else {
                    Log.i(TAG, "Start clb failed");
                    getActivity().runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                        Toast.makeText(getActivity(), R.string.start_motion_calibration_failed, Toast.LENGTH_SHORT).show();
                      }
                    });
                  }
                } else {
                  Log.w(TAG, "Start clb receive wrong response: " + result);
                }
              } else {
                Log.w(TAG, "Response for start clb command is null");
              }
            }
          }
        };

        Ion.with(HubbleApplication.AppContext)
            .load(startClbCommand)
            .setTimeout(10000)
            .setLogging(TAG, Log.VERBOSE)
            .asString()
            .setCallback(futureCallback);
      }
    } else {
      Log.w(TAG, "mListener is null");
    }
  }
  private void silentStopCLB(){
    if (mListener != null) {
      Device device = mListener.getSelectedDevice();
      if (device != null && device.getProfile() != null && device.getProfile().getDeviceLocation() != null) {
        String deviceIP = device.getProfile().getDeviceLocation().localIP;
        String stopClbCommand = String.format(STOP_CLB_CMD, deviceIP);

        Ion.with(HubbleApplication.AppContext)
            .load(stopClbCommand)
            .setTimeout(10000)
            .setLogging(TAG, Log.VERBOSE)
            .asString()
            .setCallback(new FutureCallback<String>() {
              @Override
              public void onCompleted(Exception e, String result) {

              }
            });
      }
    }
  }
  private void stopCLB() {
    if (getActivity() != null && mListener != null) {
      Device device = mListener.getSelectedDevice();
      if (device != null && device.getProfile() != null && device.getProfile().getDeviceLocation() != null) {
        String deviceIP = device.getProfile().getDeviceLocation().localIP;
        Log.i(TAG, "Device name: " + device.getProfile().getName() + " device location: " + deviceIP);
        Log.i(TAG, "Phone ip address by read file: " + Utils.getIPAddress(true));
        Log.i(TAG, "Phone ip address by function: " + getPhoneIPAddress());
        String stopClbCommand = String.format(STOP_CLB_CMD, deviceIP);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getActivity().getString(R.string.stop_motion_calibration));
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Runnable succeedRunnable = new Runnable() {
          @Override
          public void run() {
            startButton.setImageResource(R.drawable.ic_play_circle_outline_blue_900_24dp);
          }
        };

        final Runnable dimissDialogRunnable = new Runnable() {
          @Override
          public void run() {
            if (progressDialog != null && progressDialog.isShowing()) {
              progressDialog.dismiss();
            }
          }
        };

        FutureCallback<String> futureCallback = new FutureCallback<String>() {
          @Override
          public void onCompleted(Exception e, String result) {
            // AA-1506: sleep to allow user read message
            try {
              Thread.sleep(1200);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
            getActivity().runOnUiThread(dimissDialogRunnable);
            if (e != null) {
              Log.w(TAG, "Error when send stop clb command");
              e.printStackTrace();
            } else {
              if (result != null) {
                if (result.startsWith(STOP_CLB)) {
                  result = result.replace(STOP_CLB + ": ", "");
                  Log.i(TAG, "Stop clb parameter: " + result);
                  started = false;
                  if(receivedThread != null && receivedThread.isAlive()){
                    receivedThread.interrupt();
                  }
                  getActivity().runOnUiThread(succeedRunnable);
                } else {
                  Log.i(TAG, "Stop clb receive wrong response: " + result);
                }
              } else {
                Log.w(TAG, "Response for stop clb command is null");
              }
            }
          }
        };

        Ion.with(HubbleApplication.AppContext)
            .load(stopClbCommand)
            .setTimeout(10000)
            .setLogging(TAG, Log.VERBOSE)
            .asString()
            .setCallback(futureCallback);
      }
    } else {
      Log.w(TAG, "mListener is null");
    }
  }

  private void startReceiveDataThread() {
    final long endTime = System.currentTimeMillis() + CLB_DURATION;
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          DatagramSocket clientSocket = new DatagramSocket(52423);
          clientSocket.setReuseAddress(true);
          int packetsReceived = 0;
          while (started && System.currentTimeMillis() < endTime) {
            byte[] receiveData = new byte[6];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            if (packetsReceived > 0) {
              final UDPData udpData = new UDPData(receiveData);
              if(getActivity()!= null) {
                getActivity().runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    addEntry(udpData);
                  }
                });
              }
            }
            packetsReceived++;
          }
          Log.i(TAG, "Stop receiving thread. Total packet received: " + packetsReceived);
          clientSocket.disconnect();
          clientSocket.close();
          if(getActivity()!= null) {
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                stopCLB();
              }
            });
          }

        } catch (Exception ex) {
          Log.e(TAG, "Error when receving calibration data");
          ex.printStackTrace();
        }
      }
    };
    if (receivedThread != null && receivedThread.isAlive()) {
      receivedThread.interrupt();
    }
    receivedThread = new Thread(runnable);
    receivedThread.start();
  }

  /**
   * Get current Wi-Fi IP Address
   *
   * @return current ip address or null
   */

  private String getPhoneIPAddress() {
    WifiManager wm = (WifiManager) HubbleApplication.AppContext.getSystemService(Context.WIFI_SERVICE);
    String ip = null;
    if (wm.getConnectionInfo() != null) {
      ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }
    return ip;
  }
  public static class InputFilterMinMax implements InputFilter {

    private int min, max;

    public InputFilterMinMax(int min, int max) {
      this.min = min;
      this.max = max;
    }

    public InputFilterMinMax(String min, String max) {
      this.min = Integer.parseInt(min);
      this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
      try {
        int input = Integer.parseInt(dest.toString() + source.toString());
        if (isInRange(min, max, input))
          return null;
      } catch (NumberFormatException nfe) { }
      return "";
    }

    private boolean isInRange(int a, int b, int c) {
      return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
  }
}
