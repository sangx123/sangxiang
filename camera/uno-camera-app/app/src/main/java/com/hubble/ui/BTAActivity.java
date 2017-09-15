package com.hubble.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.bta.BTAEventView;
import com.hubble.bta.BTATask;
import com.hubble.bta.DrawData;
import com.hubble.bta.PushEvent;
import com.hubble.bta.XAxisValueFormatter;
import com.hubble.bta.YValueFormatter;
import com.hubble.devcomm.Device;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.networkinterface.device.DeviceManager;
import com.hubble.framework.service.cloudclient.device.pojo.request.SendCommand;
import com.hubble.framework.service.cloudclient.device.pojo.response.SendCommandDetails;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubble.util.CommandUtils;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.CameraSettingsActivity;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import base.hubble.database.AverageData;
import base.hubble.database.TimelineEvent;
import de.greenrobot.event.EventBus;


public class BTAActivity extends ActionBarActivity implements View.OnClickListener, Runnable {
  private static final String TAG = "BTAActivity";
  private static final int ONE_HOUR = 60 * 60;

  private static final long BABY_CHECKING_TIME_MS_MAX = 15 * 1000;
  private static final String CHECK_BSC_HEAD_STATUS_CMD = "check_bsc_head_status";
  private static final int UNKNOWN_STATUS = -3;
  public static final int NO_BABY_HEAD = -2;
  public static final int BSC_NOT_RUNNING = -1;
  public static final int HAS_BABY_HEAD = 1;

  private static final boolean DEBUG = false;
  private static final String PREFS_BTA_SCREEN_SHOWN = "com_hubble_ui_BTAActivity_boolean_BTA_screen_shown";
  private static final boolean BTA_SCREEN_SHOWN_DEFAULT = false;
  // runtime cache for current day event
  private static int retry = 0;
  private final CharSequence[] items = {"1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "11h", "12h"};
  private Device device;
  private BarChart mChart;
  private BTATask btaTask;
  private HubbleApplication app = HubbleApplication.AppContext;
  private volatile List<TimelineEvent> bscEvents, btaEvents;
  private boolean isActivityDestroy = false;
  private int remainBTATime = -1;
  private Button buttonStartStopBTA;
  private Thread threadUpdateTime;
  private BTAEventView[] btaEventView = new BTAEventView[3];
  private DonutProgress donutProgress;
  private TextView textViewAverage;
  private int drawMode = 0; // 0 - daily activity level, 1 day average;
  private TextView textViewDay, textViewWeek, textViewMonth, textViewTitle;
  private ProgressBar progressBar, progressBarStart; // check when start BTA
  private ProgressDialog mBabyCheckingDialog;
  private Dialog mBabyNotDetectedDialog;
  private int numberOfHour;
  private TextView textViewDate;
  private Menu menu;
  private ScheduledExecutorService executorService;
  private TextView tvTime;

  private boolean mTodayEventsSync;
  private boolean mThisWeekEventsSync;
  private boolean mThisMonthEventsSync;
  int bscValueIntervalInSecond = 150;
  private int mChartTimeIntervalInSecond = 15 * 60;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bta);

    bindingView();

    Intent intent = getIntent();
    if (intent != null) {
      String regId = intent.getStringExtra(PublicDefine.DEVICE_REG_ID);
      String apiKey = intent.getStringExtra(PublicDefine.PREFS_SAVED_PORTAL_TOKEN);
      device = DeviceSingleton.getInstance().getDeviceByRegId(regId);
        if(device != null && device.getProfile() != null) {
            btaTask = new BTATask(apiKey, device, getApplicationContext(), mBTAInterface);
      /*if(Util.isThisVersionGreaterThanOrEqual(device.getProfile().firmwareVersion, "02.04.00")){
        bscValueIntervalInSecond = 60;
      }*/
            // set title bar with camera name
            textViewTitle.setText(device.getProfile().getName());
            threadUpdateTime = new Thread(this);
            threadUpdateTime.start();
            initialize();
        }
        else{

            finish();
            return;
        }
    } else {
      finish();
    }
    // scheduler thread to update bsc chart every 5 minutes
    Runnable updateBSCUIRunnable = new Runnable() {
      @Override
      public void run() {
        if (drawMode == 0)
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              updateRecent24hBSCEventsChart(true);
              updateRecent24hBTAEventsChart(false);
            }
          });
      }
    };
    executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate(updateBSCUIRunnable, 5, 5, TimeUnit.MINUTES);

    checkToShowBtaTips();
  }

  private void checkToShowBtaTips() {
    boolean hasBtaTipsShown = HubbleApplication.AppConfig.getBoolean(PREFS_BTA_SCREEN_SHOWN, BTA_SCREEN_SHOWN_DEFAULT);
    if (!hasBtaTipsShown) {
      showBtaTips();
      // Don't show BTA tips next time
      /*SharedPreferences.Editor editor = HubbleApplication.AppConfig.edit();
      editor.putBoolean(PREFS_BTA_SCREEN_SHOWN, true);
      editor.apply();*/
        HubbleApplication.AppConfig.putBoolean(PREFS_BTA_SCREEN_SHOWN, true);
    }
  }

  private void showBtaTips() {
    // DialogFragment.show() will take care of adding the fragment
    // in a transaction.  We also want to remove any currently showing
    // dialog, so make our own transaction and take care of that here.
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    Fragment prev = getSupportFragmentManager().findFragmentByTag("btaTipsDialog");
    if (prev != null) {
      ft.remove(prev);
    }
    ft.addToBackStack(null);

    // Create and show the dialog.
    DialogFragment btaTipsDialogFragment = new BtaTipDialogFragment();
    btaTipsDialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    btaTipsDialogFragment.show(ft, "btaTipsDialog");
  }

  private void showBabyCheckingDialog() {
    if (mBabyCheckingDialog == null) {
      mBabyCheckingDialog = new ProgressDialog(this);
      mBabyCheckingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      mBabyCheckingDialog.setMessage(getString(R.string.analyzing)+ "...");
    }

    if (!mBabyCheckingDialog.isShowing()) {
      try {
        mBabyCheckingDialog.show();
      } catch (Exception e) {
      }
    }
  }

  private void hideBabyCheckingDialog() {
    if (mBabyCheckingDialog != null && mBabyCheckingDialog.isShowing()) {
      try {
        mBabyCheckingDialog.dismiss();
      } catch (Exception e) {
      }
    }
  }

  private void bindingView() {
    Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
    setSupportActionBar(myToolbar);
    getSupportActionBar().setTitle("");
    mChart = (BarChart) findViewById(R.id.barChart);
    buttonStartStopBTA = (Button) findViewById(R.id.buttonStartBTA);
    buttonStartStopBTA.setVisibility(View.INVISIBLE);

    buttonStartStopBTA.setOnClickListener(this);
    btaEventView[0] = (BTAEventView) findViewById(R.id.btaEventView1);
    btaEventView[1] = (BTAEventView) findViewById(R.id.btaEventView2);
    btaEventView[2] = (BTAEventView) findViewById(R.id.btaEventView3);
    donutProgress = (DonutProgress) findViewById(R.id.avgActivityLevelChart);
    donutProgress.setProgress(0);
    textViewAverage = (TextView) findViewById(R.id.avgActivityLevelTV);
    textViewDay = (TextView) findViewById(R.id.textViewDay);
    textViewWeek = (TextView) findViewById(R.id.textViewWeek);
    textViewTitle = (TextView) findViewById(R.id.textViewTitle);
    progressBar = (ProgressBar) findViewById(R.id.progressBarTop);
    progressBarStart = (ProgressBar) findViewById(R.id.progressBarStart);
    textViewDate = (TextView) findViewById(R.id.textViewDate);
    textViewMonth = (TextView) findViewById(R.id.textViewMonth);
    tvTime = (TextView) findViewById(R.id.textView3);
    tvTime.setVisibility(View.GONE);

    textViewDay.setOnClickListener(this);
    textViewWeek.setOnClickListener(this);
    textViewMonth.setOnClickListener(this);

    initializeBarChart();
  }

  public void onClick(View view) {
    if (view.getId() == buttonStartStopBTA.getId()) {
      handleStartStopBTAClick();
    } else if (view.getId() == textViewDay.getId()) {
      // drawmode !=0 mean "Day" view
      Log.d(TAG, "Button Day onClicked");
      if (drawMode != 0) {
        drawMode = 0;
        textViewDate.setVisibility(View.INVISIBLE);
        textViewWeek.setTextColor(getResources().getColor(R.color.white));
        textViewDay.setTextColor(getResources().getColor(R.color.bta_button_select));
        textViewMonth.setTextColor(getResources().getColor(R.color.white));
        updateRecent24hBSCEventsChart(true);
        updateRecent24hBTAEventsChart(false);
      }
    } else if (view.getId() == textViewWeek.getId()) {
      Log.d(TAG, "Button Week onClicked");
      showProgressBar();
      AsyncTask.execute(new Runnable() {
        @Override
        public void run() {
          // Wait for sync events completed
          while (!isActivityDestroy && !mThisWeekEventsSync) {
            try {
              Thread.sleep(2000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }

          runOnUiThread(new Runnable() {

            @Override
            public void run() {
              if (!isActivityDestroy) {
                drawMode = 1;
                textViewDate.setVisibility(View.INVISIBLE);
                textViewWeek.setTextColor(getResources().getColor(R.color.bta_button_select));
                textViewDay.setTextColor(getResources().getColor(R.color.white));
                textViewMonth.setTextColor(getResources().getColor(R.color.white));
                drawDaysAverageChart(7);
              }
              hideProgressBar();
            }
          });

        }
      });

    } else if (view.getId() == textViewMonth.getId()) {
      Log.d(TAG, "Button Month onClicked");
      showProgressBar();
      AsyncTask.execute(new Runnable() {
        @Override
        public void run() {
          // Wait for sync events completed
          while (!isActivityDestroy && !mThisMonthEventsSync) {
            try {
              Thread.sleep(2000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }

          runOnUiThread(new Runnable() {

            @Override
            public void run() {
              if (!isActivityDestroy) {
                drawMode = 1;
                textViewDate.setVisibility(View.INVISIBLE);
                textViewMonth.setTextColor(getResources().getColor(R.color.bta_button_select));
                textViewDay.setTextColor(getResources().getColor(R.color.white));
                textViewWeek.setTextColor(getResources().getColor(R.color.white));
                drawDaysAverageChart(30);
              }
              hideProgressBar();
            }
          });

        }
      });


    }
  }

  private void checkBabyIsInsightOfCamera() {
    // update UI
    //progressBarStart.setVisibility(View.VISIBLE);
    //showBabyCheckingDialog();
    //buttonStartStopBTA.setEnabled(false);
    //buttonStartStopBTA.setText("");
    // start checking
    new Thread(new Runnable() {
      @Override
      public void run() {
        boolean babyDetected = false;
        int tryCount = 1;
        String response = null;
        int babyHeadStatus = UNKNOWN_STATUS;
        long babyCheckingEndTime = System.currentTimeMillis() + BABY_CHECKING_TIME_MS_MAX;
        do {
          try {
            Log.d(TAG, "check baby is insight of camera ... " + tryCount);
            response = CommandUtils.sendCommand(device, CHECK_BSC_HEAD_STATUS_CMD, device.isAvailableLocally());
            /*
             * Response: check_bsc_head_status: -1 | -2 | 1
             */
            Log.d(TAG, "Check baby insight of camera, response: " + response);
            if (!TextUtils.isEmpty(response) && response.startsWith(CHECK_BSC_HEAD_STATUS_CMD)) {
              response = response.substring(CHECK_BSC_HEAD_STATUS_CMD.length() + 2);
              try {
                babyHeadStatus = Integer.parseInt(response);
                babyDetected = (babyHeadStatus == HAS_BABY_HEAD);
              } catch (NumberFormatException e) {
                e.printStackTrace();
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "check baby status: FAILED!!!", e);
          }

          if (babyDetected) {
            break;
          } else {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
          }
        } while (!isActivityDestroy && (System.currentTimeMillis() < babyCheckingEndTime));
        Log.d(TAG, "Check baby head status DONE, head status: " + babyHeadStatus);
        final int finalBabyHeadStatus = babyHeadStatus;
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // restore UI
            //progressBarStart.setVisibility(View.GONE);
            hideBabyCheckingDialog();
            //buttonStartStopBTA.setEnabled(true);
            //buttonStartStopBTA.setText(R.string.start);
            // handle result
            //if (finalBabyHeadStatus == HAS_BABY_HEAD) {
            //  startBTAStep2();
            //} else
            if (finalBabyHeadStatus == NO_BABY_HEAD) {
              showBabyNotDetectedDialog();
            }
            //} else {
            //  startFailedWithCommonMessage();
            //}
          }
        });
      }
    }).start();
  }

  private void showBabyNotDetectedDialog() {
    Log.d(TAG, "Show baby not detected dialog");
    if (mBabyNotDetectedDialog == null) {
      mBabyNotDetectedDialog = new AlertDialog.Builder(BTAActivity.this)
              .setCancelable(false)
              .setTitle(getString(R.string.baby_is_not_detected).toUpperCase())
              .setMessage(R.string.baby_not_insight_of_camera)
              .setPositiveButton(R.string.show_tips, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
                  stopDismissDialogTimer();
                  showBtaTips();
                }
              })
              .setNegativeButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
                  stopDismissDialogTimer();
                }
              })
              .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                  stopDismissDialogTimer();
                }
              }).create();
    }

    try {
      mBabyNotDetectedDialog.show();
    } catch (Exception e) {
    }

    startDismissDialogTimer(mBabyNotDetectedDialog);
  }

  private CountDownTimer mDismissDialogTimer;
  private static final long DISMISS_NO_BABY_DIALOG_DURATION_MS = 30 * 1000;
  private void startDismissDialogTimer(final Dialog dialog) {
    mDismissDialogTimer = new CountDownTimer(DISMISS_NO_BABY_DIALOG_DURATION_MS, 1000) {
      @Override
      public void onTick(long millisUntilFinished) {
//        Log.d(TAG, "Dismiss baby not detected dialog, remaining time: " + millisUntilFinished / 1000);
      }

      @Override
      public void onFinish() {
        Log.d(TAG, "Countdown timer is finished, dissmiss the dialog now");
        if (dialog != null && dialog.isShowing()) {
          dialog.dismiss();
        }
      }
    };
    Log.d(TAG, "Start dismiss Baby Not Detected dialog countdown timer");
    mDismissDialogTimer.start();
  }

  private void stopDismissDialogTimer() {
    if (mDismissDialogTimer != null) {
      Log.d(TAG, "Stop dismiss Baby Not Detected dialog countdown timer");
      mDismissDialogTimer.cancel();
    }
  }

  /**
   * Should check if baby is detected before start BTA
   */
  private void handleStartStopBTAClick() {
    if (remainBTATime > 0) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(R.string.do_you_want_to_stop_bta);
      builder.setTitle(R.string.alert);
      builder.setCancelable(true);
      DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (which == AlertDialog.BUTTON_NEGATIVE) {
            dialog.dismiss();
          } else if (which == AlertDialog.BUTTON_POSITIVE) {
            stopBTA();
          }
        }
      };
      builder.setNegativeButton(R.string.cancel, onClickListener);
      builder.setPositiveButton(R.string.ok, onClickListener);
      builder.show();
    } else {
      showBTATimerSelection();
    }
  }

     ProgressDialog progressDialog = null;
  private void stopBTA() {
      //ARUNA enable below code

    //ListenableFuture<Pair<String, Object>> future = btaTask.stopBTA();
    progressDialog = new ProgressDialog(this);
    progressDialog.setMessage("Stop BTA mode");
    progressDialog.setCancelable(false);
    progressDialog.show();
      btaTask.stopBTA();

   /* Futures.addCallback(future, new FutureCallback<Pair<String, Object>>() {
      @Override
      public void onSuccess(Pair<String, Object> result) {
      //  Log.i(TAG, "command , value " + result.getFirst() + " " + result.getSecond());
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            progressDialog.dismiss();
            remainBTATime = 0;
            updateBTATimeUI();
          }
        });
      }

      @Override
      public void onFailure(Throwable t) {
        Log.i(TAG, "Stop BTA failure");
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(BTAActivity.this, "Cannot stop BTA mode", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
          }
        });
      }
    });*/
  }

  private void showBTATimerSelection() {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.select_timer));
    builder.setItems(items, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int item) {
        Log.i(TAG, "User choose item index: " + item);
        numberOfHour = item + 1;
        startBTA();
      }
    });
    AlertDialog alert = builder.create();
    alert.show();
  }

  private void startBTA() {
    showBabyCheckingDialog();
    retry = 0;
      btaTask.startBSCMode();
    /*btaTask.startBSCMode(new com.koushikdutta.async.future.FutureCallback<Pair<String, Object>>() {
      @Override
      public void onCompleted(Exception e, Pair<String, Object> result) {
        if (e == null) {

          Log.i(TAG, "Start BSC mode result: " + result.first + " " + result.second);
          if (result.second instanceof Integer && (int) result.second == 0) {
            try {
              Thread.sleep(9000);
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  //hideBabyCheckingDialog();
                  startBTAStep2();
                }
              });
            } catch (InterruptedException e1) {
              e1.printStackTrace();
            }
          } else {
            Log.e(TAG, "Start BSC mode failed -- 1");
            if (retry < 3) {
              retry++;
              try {
                Thread.sleep(4000);
              } catch (InterruptedException e1) {
                e1.printStackTrace();
              }
              btaTask.startBSCMode(this);
            } else {
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  hideBabyCheckingDialog();
                  startFailedWithCommonMessage();
                }
              });
            }
          }
        } else {
          Log.e(TAG, "Start BSC mode faile -- 2");
          if (retry < 3) {
            retry++;
            try {
              Thread.sleep(4000);
            } catch (InterruptedException e1) {
              e1.printStackTrace();
            }
            btaTask.startBSCMode(this);
          } else {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                hideBabyCheckingDialog();
                startFailedWithCommonMessage();
              }
            });
          }
          if (e != null) e.printStackTrace();
        }
      }
    });*/
  }

  private void startBTAStep2() {
    retry = 0;
      btaTask.startBTA(numberOfHour * 3600);
   /* btaTask.startBTA(numberOfHour * 3600, new com.koushikdutta.async.future.FutureCallback<Pair<String, Object>>() {
      @Override
      public void onCompleted(Exception e, Pair<String, Object> result) {
        if (e == null) {
          Log.i(TAG, "Start BTA result command , value " + result.first + " " + result.second);
          if (result.second instanceof Integer && (int) result.second == 0) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                //hideBabyCheckingDialog();
                remainBTATime = numberOfHour * 3600;
                updateBTATimeUI();
                checkBabyIsInsightOfCamera();
              }
            });
          } else {
            if (retry < 3) {
              try {
                Thread.sleep(3000);
                retry++;
                btaTask.startBTA(numberOfHour * 3600);
              } catch (InterruptedException e1) {
                e1.printStackTrace();
              }
            } else {
              Log.i(TAG, "Start BTA failed -- 1");
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  hideBabyCheckingDialog();
                  startFailedWithCommonMessage();
                }
              });
            }
          }
        } else {
          Log.i(TAG, "Start BTA failed -- 2");
          if (retry < 3) {
            try {
              Thread.sleep(3000);
              retry++;
              btaTask.startBTA(numberOfHour * 3600);
            } catch (InterruptedException e1) {
              e1.printStackTrace();
            }
          } else {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                hideBabyCheckingDialog();
                startFailedWithCommonMessage();
              }
            });
          }
        }
      }
    });*/
  }

  private void startFailedWithCommonMessage() {
    new AlertDialog.Builder(BTAActivity.this).setMessage(R.string.check_baby_head_error)
        .setPositiveButton(R.string.show_tips, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            showBtaTips();
          }
        })
        .setNegativeButton(R.string.dialog_ok, null).show();
  }

  private void setupBTAEventsUI() {
    // first: update ui from cache
    updateRecent24hBTAEventsChart(true);
    // second: update ui from internet
    updateRecent24hBTAEventsChart(false);
  }

  private void initialize() {

    mTodayEventsSync = false;
    mThisWeekEventsSync = false;
    mThisMonthEventsSync = false;
    getRemainBTATime();
    /*
     * 20160909: HOANG: don't need to load cache data at the first time
     */
    doSyncEventTask();

    if (device.getProfile().isAvailable()) {
      Log.w(TAG, "Device " + device.getProfile().getName() + " is online");
    } else {
      // offline case
      Log.w(TAG, "Device " + device.getProfile().getName() + " is offline");
    }
  }

  private void doSyncEventTask() {
    showProgressBar();
    AsyncTask.execute(new Runnable() {
      @Override
      public void run() {
        if (!isActivityDestroy) {
          Log.d(TAG, "Sync BTA events today");
          updateRecent24hBTAEventsChartBlocked(false);
          Log.d(TAG, "Sync BTA events today DONE");

          if (!isActivityDestroy) {
            btaTask.syncBSCEventsToday();
            Log.d(TAG, "Sync BSC events today completed");
            mTodayEventsSync = true;
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                updateRecent24hBSCEventsChart(false);
              }
            });
          }

          DateTime today = DateTime.now();
          if (!isActivityDestroy) {
            btaTask.syncBscEventsThisWeekExceptToday(today);
            Log.d(TAG, "Sync BSC events this week completed");
            mThisWeekEventsSync = true;
          }

          if (!isActivityDestroy) {
            btaTask.syncBscEventsThisMonthExceptThisWeek(today);
            Log.d(TAG, "Sync BSC events this month completed");
            mThisMonthEventsSync = true;
          }
        }
      }
    });
  }

  private void getRemainBTATime() {

    buttonStartStopBTA.setVisibility(View.INVISIBLE);
    tvTime.setVisibility(View.INVISIBLE);
    Log.i(TAG, "Start get remain BTA time");
      DeviceManager mDeviceManager;

      mDeviceManager = DeviceManager.getInstance(getApplicationContext());
      SecureConfig settings = HubbleApplication.AppConfig;
      String regId = device.getProfile().getRegistrationId();
      SendCommand getAdaptiveQuality = new SendCommand(settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, null),regId, "get_bsc_remain_duration");

      mDeviceManager.sendCommandRequest(getAdaptiveQuality, new Response.Listener<SendCommandDetails>() {

                  @Override
                  public void onResponse(SendCommandDetails response1) {
                      String responsebody = response1.getDeviceCommandResponse().getBody().toString();
                      Log.i(TAG, "SERVER RESP : " + responsebody);
                       if (response1.getDeviceCommandResponse() != null && responsebody.contains("get_bsc_remain_duration")) {

                      try {
                          final Pair<String, Object> result = CommonUtil.parseResponseBody(responsebody);
                          Log.w(TAG, "Result: " + result.first + " and " + result.second);
                          try {
                              runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      if (!result.first.equalsIgnoreCase("error")) {
                                          buttonStartStopBTA.setVisibility(View.VISIBLE);
                                          String res = result.second.toString();
                                          int time = ((Float)result.second).intValue();
                                          remainBTATime = Math.round(time);
                                          updateBTATimeUI();
                                      } else {
                                          Log.e(TAG, "Error when get remain BTA");

                                      }
                                  }
                              });
                          } catch (Exception ex) {
                              Log.e(TAG, "Error when get remaining bta time -- 1");
                              ex.printStackTrace();
                          }

                      } catch (Exception ex) {

                      }
                       }
                  }
              }, new Response.ErrorListener()
              {
                  @Override
                  public void onErrorResponse(VolleyError error)
                  {

                      if(error != null && error.networkResponse != null)
                      {
                          Log.d(TAG,error.networkResponse.toString());
                          Log.d(TAG,error.networkResponse.data.toString());
                      }

                  }
              }



      );
   /* device.sendCommandAsync("get_bsc_remain_duration", null, null, new FutureCallback<Pair<? extends String, ? extends Object>>() {
      @Override
      public void onSuccess(final Pair<? extends String, ? extends Object> result) {
        Log.w(TAG, "Result: " + result.first + " and " + result.second);
        try {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (!result.first.equalsIgnoreCase("error")) {
                buttonStartStopBTA.setVisibility(View.VISIBLE);
                  String res = result.second.toString();
                int time = Integer.valueOf((String) result.second);
                remainBTATime = time;
                updateBTATimeUI();
              } else {
                Log.e(TAG, "Error when get remain BTA");

              }
            }
          });
        } catch (Exception ex) {
          Log.e(TAG, "Error when get remaining bta time -- 1");
          ex.printStackTrace();
        }
      }


      @Override
      public void onFailure(Throwable t) {
        Log.e(TAG, "Error when get remaining bta time -- 2");
        t.printStackTrace();
      }
    });*/
  }

  private void getAverageActivity() {
    ListenableFuture<List<Models.Average>> listListenableFuture = btaTask.getAggregate();
    Log.w(TAG, "Start get average activity level data");
    Futures.addCallback(listListenableFuture, new FutureCallback<List<Models.Average>>() {
      @Override
      public void onSuccess(List<Models.Average> result) {
        if (result != null && result.get(0) != null && result.get(0).getAverageData() != null) {
          List<AverageData> averageDataList = result.get(0).getAverageData();
          for (AverageData average : averageDataList) {
            average.setDeviceRegId(device.getProfile().getRegistrationId());
            average.save();
          }
          Log.i(TAG, "Get average data ok, size" + averageDataList.size());
        }
      }

      @Override
      public void onFailure(Throwable t) {
        Log.e(TAG, "Get average data failed");
        t.printStackTrace();
      }
    });
  }

  private void updateBTATimeUI() {
    if (!isActivityDestroy) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (remainBTATime > 0) {
            buttonStartStopBTA.setText(R.string.stop);
            tvTime.setText(formatBTATime(remainBTATime));
            tvTime.setVisibility(View.VISIBLE);
          } else {
            buttonStartStopBTA.setText(R.string.start);
            tvTime.setVisibility(View.GONE);
            tvTime.setText("");
          }
        }
      });
    }
  }

  /**
   * get BTA events
   */
  private void getAndCacheBTAEvents() {
    Log.w(TAG, "get and cache BTA events");
    showProgressBar();
    ListenableFuture<List<TimelineEvent>> listListenableFuture = btaTask.queryLast24HoursBTAEvents(false);
    Futures.addCallback(listListenableFuture, new FutureCallback<List<TimelineEvent>>() {
      @Override
      public void onSuccess(List<TimelineEvent> timelineEvents) {
        if (timelineEvents == null) {
          hideProgressBar();
        } else {
          Log.w(TAG, "BTA Events count: " + timelineEvents.size());
          ActiveAndroid.beginTransaction();
          try {
            //displayTop3BTAActivitiesEventOnImageViews(null);
          } catch (Exception ex) {
            ex.printStackTrace();
          } finally {
            ActiveAndroid.endTransaction();
            hideProgressBar();
          }
        }
      }

      @Override
      public void onFailure(Throwable t) {
        Log.w(TAG, "Get BTA Events error");
        t.printStackTrace();
        hideProgressBar();
      }
    });
  }

  /**
   * Update bsc events chart for recent 24 hours
   */
  private void updateRecent24hBSCEventsChart(boolean forceOnlineQuery) {
    DateTime now = roundUpBscInterval(DateTime.now());
    DateTime fromDate = now.minusDays(1);
    DateTime toDate = now;
    updateBSCEventChartForPeriod(fromDate, toDate, forceOnlineQuery);
  }

  private DateTime roundUpBscInterval(DateTime dateTime) {
    int minutesOfHour = dateTime.getMinuteOfHour();
    int diff = (mChartTimeIntervalInSecond/60) - (minutesOfHour % (mChartTimeIntervalInSecond/60));
    dateTime = dateTime.minusSeconds(dateTime.getSecondOfMinute());
    return dateTime.plusMinutes(diff);
  }
  /**
   * Update bsc events charts base on time span
   *
   * @param fromDate from date in utc time
   * @param toDate   end date in utc time
   */
  private void updateBSCEventChartForPeriod(final DateTime fromDate, final DateTime toDate, boolean forceOnlineQuery) {
    showProgressBar();
    ListenableFuture<List<TimelineEvent>> listenableFuture;
    Log.w(TAG, "update bsc events charts");
    Log.w(TAG, "from date: " + fromDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss Z")));
    Log.w(TAG, "to date: " + toDate.toString(DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss Z")));
    if(forceOnlineQuery) {
      listenableFuture = btaTask.queryLast24HoursBSCEvents();
    } else {
      listenableFuture = btaTask.queryBSCEvents(fromDate, toDate);
    }
    Futures.addCallback(listenableFuture, new FutureCallback<List<TimelineEvent>>() {
      @Override
      public void onSuccess(List<TimelineEvent> timelineEvents) {
        bscEvents = timelineEvents;
        renderChart(fromDate, toDate);
      }

      @Override
      public void onFailure(Throwable t) {
        Log.e(TAG, "Error when query bta events");
      }
    });
  }

  private void drawDaysChart(final DateTime now, List<BarEntry> barEntries, final int days) {

    BarDataSet barDataSet = new BarDataSet(barEntries, null);
    barDataSet.setValues(barEntries);
    barDataSet.setHighLightColor(Color.parseColor("#00D1FF"));
    barDataSet.setColor(Color.parseColor("#AA00FFBB"));
    barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
    BarData barData = new BarData(barDataSet);
    // setup mChart
    mChart.setData(barData);
    mChart.getXAxis().setGranularity(1f);
    mChart.getXAxis().setLabelCount(7);
    mChart.getAxisLeft().setGranularity(4f);
    barData.setBarWidth(0.1f);
    mChart.notifyDataSetChanged();
    mChart.invalidate();

    barDataSet.setValueFormatter(new ValueFormatter() {
      @Override
      public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        float y = entry.getY();
        if (y <= 0.01f) {
          return "";
        } else {
          return ((int) y) + "";
        }
      }
    });

    AxisValueFormatter xAxisFormatter = new AxisValueFormatter() {
      @Override
      public String getFormattedValue(float value, AxisBase axis) {
        DateTime day = now.minusDays((int) (days - value));
        String dayStr = day.toString(DateTimeFormat.forPattern("MMM-dd"));
        return dayStr;
      }

      @Override
      public int getDecimalDigits() {
        return 0;
      }
    };
    AxisValueFormatter yAxisFormatter = new YValueFormatter();

    // set x, y label formatter
    mChart.getXAxis().setValueFormatter(xAxisFormatter);
    mChart.getAxisLeft().setValueFormatter(yAxisFormatter);

    //setup data

    mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
      @Override
      public void onValueSelected(Entry e, Highlight h) {
        if (DEBUG) Log.i(TAG, "Entry x " + e.getX() + " y " + e.getY() + " clicked");
        if (e.getData() != null /*&& e.getY() > 0f*/) { // AA-2047
          if (e.getData() instanceof DateTime) {
            DateTime day = (DateTime) e.getData();
            textViewDate.setVisibility(View.VISIBLE);
            textViewDate.setText(day.toString(DateTimeFormat.forPattern("YYYY-MM-dd")));
            DateTime fromDate, endDate;
            DateTime now = roundUpBscInterval(DateTime.now());
            if (now.withTimeAtStartOfDay().getMillis() == day.withTimeAtStartOfDay().getMillis()) {
              // It's today
              Log.d(TAG, "Selected day is today");
              fromDate = now.minusDays(1);
              endDate = now;
            } else {
              // Not today
              Log.d(TAG, "Selected day is not today");
              fromDate = (day.withTime(0, 0, 0, 1));
              endDate = (day.withTime(23, 59, 59, 999));
            }
            updateBTA(fromDate, endDate);
            updateBSCEventChartForPeriod(fromDate, endDate, false);
            // AA-1991
            textViewWeek.setTextColor(getResources().getColor(R.color.white));
            textViewDay.setTextColor(getResources().getColor(R.color.bta_button_select));
            textViewMonth.setTextColor(getResources().getColor(R.color.white));
          }
        }
      }

      @Override
      public void onNothingSelected() {

      }
    });
    mChart.setVisibleXRangeMaximum(7);
    mChart.setVisibleXRangeMinimum(7);
    mChart.moveViewToX(days - 1);
    mChart.invalidate();
  }

  /**
   * Render bsc events bar chart, bta events bar chart
   *
   * @param fromDate
   * @param toDate
   */
  private void renderChart(final DateTime fromDate, final DateTime toDate) {
      final DrawData drawData = new DrawData(fromDate, toDate, bscEvents, btaEvents);
      Log.d(TAG, "Render chart, event count: bsc? " + (bscEvents==null?0:bscEvents.size()) +
              ", bta? " + (btaEvents==null?0:btaEvents.size()) + ", average? " + drawData.getAverage());
      runOnUiThread(new Runnable() {
          @Override
          public void run() {
              BarDataSet barDataSet = new BarDataSet(drawData.getEntries(), null);
              barDataSet.setHighLightColor(Color.parseColor("#00D1FF"));
              barDataSet.setColor(Color.parseColor("#AA00FFBB"));
              barDataSet.setDrawValues(true);
              barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

              Log.d(TAG, "BTA entries count? " + (drawData.getBTAEntries()==null?0:drawData.getBTAEntries().size()));
              BarDataSet barDataSet1 = new BarDataSet(drawData.getBTAEntries(), null);
              barDataSet1.setHighLightColor(Color.YELLOW);
              barDataSet1.setColor(Color.YELLOW);
              barDataSet1.setDrawValues(true);
              barDataSet1.setAxisDependency(YAxis.AxisDependency.LEFT);

              List<BarEntry> noBabyEntries = drawData.getNoBabyEntries();
              BarDataSet barDataSet2 = new BarDataSet(noBabyEntries, null);
              barDataSet2.setHighLightColor(Color.RED);
              barDataSet2.setColor(Color.RED);
              barDataSet2.setDrawValues(false);
              barDataSet2.setAxisDependency(YAxis.AxisDependency.LEFT);
              Log.d(TAG, "No baby detected entries count? " + noBabyEntries.size() + ", current baby detected? " + drawData.isBabyDetected());
              if (!drawData.isBabyDetected()) {
                  if((toDate.minusMinutes(3).toLocalDate()).equals(new LocalDate())) {
                      // just show dialog for today
                      showBabyNotDetectedDialog();
                  } else {
                      Log.d(TAG, "No baby detected but it's not today, don't show dialog");
                  }
              }

              BarData data = new BarData(barDataSet);
              data.addDataSet(barDataSet1);
              data.addDataSet(barDataSet2);
              data.setBarWidth(0.05f);

              mChart.setData(data);
              mChart.getXAxis().setGranularity(3f);
              mChart.getAxisLeft().setGranularity(4f);
              mChart.notifyDataSetChanged();
              mChart.invalidate();

              mChart.getXAxis().setValueFormatter(new XAxisValueFormatter(drawData.getEntries()));
              mChart.getXAxis().setLabelCount(15);
              mChart.getAxisLeft().setValueFormatter(new YValueFormatter());

              barDataSet.setValueFormatter(new ValueFormatter() {
                  @Override
                  public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            /*float y = entry.getY();
            if (y <= 0.01f) {
              return "";
            } else {
              return ((int) y) + "";
            }
            */
                      return "";
                  }
              });
              mChart.setVisibleXRangeMaximum(15);
              mChart.setVisibleXRangeMinimum(15);
              mChart.animateXY(1000, 1000);
              mChart.moveViewToX(278);
              mChart.invalidate();
              hideProgressBar();

              textViewAverage.setText("" + drawData.getAverage());
              donutProgress.setProgress(drawData.getAverage());
          }
      });
  }

  /**
   * update top 3 bta activities chart and image view
   *
   * @param fromCache from cache or online
   */
  private void updateRecent24hBTAEventsChart(final boolean fromCache) {
    if (DEBUG) Log.w(TAG, "--> update last 24 hours bta ui " + fromCache);
    showProgressBar();
    ListenableFuture<List<TimelineEvent>> listListenableFuture = btaTask.queryLast24HoursBTAEvents(fromCache);
    Futures.addCallback(listListenableFuture, new FutureCallback<List<TimelineEvent>>() {
      @Override
      public void onSuccess(List<TimelineEvent> timelineEvents) {
        if (timelineEvents != null) {
          for (TimelineEvent timelineEvent : timelineEvents) {
            if (DEBUG) Log.w(TAG, "BTA top: time " + timelineEvent.getTimestamp() + ", value " + timelineEvent.getValue() +
                    ", data " + timelineEvent.getCachedData().get(0).toString());
          }
          displayTop3BTAActivitiesEventOnImageViews(timelineEvents);
        } else {
          Log.e(TAG, "BTA events is null");
        }
        hideProgressBar();
      }

      @Override
      public void onFailure(Throwable t) {
        if (DEBUG) Log.w(TAG, "<-- update last 24 hours bta ui error");
        t.printStackTrace();
        hideProgressBar();
      }
    });
  }

  /**
   * update top 3 bta activities chart and image view
   *
   * @param fromCache from cache or online
   */
  private void updateRecent24hBTAEventsChartBlocked(final boolean fromCache) {
    if (DEBUG) Log.w(TAG, "--> update last 24 hours bta ui " + fromCache);
    showProgressBar();
    DateTime toDate = DateTime.now();
    DateTime fromDate = toDate.minusDays(1);
    Log.e(TAG, "Update recent24hBTAEventsChartBlocked from date " + fromDate.toString() + " to date " + toDate.toString());
    List<TimelineEvent> timelineEvents = null;
    if (fromCache) {
      timelineEvents = btaTask.queryOfflineBTAEventsForPeriodBlocked(fromDate, toDate);
    } else {
      timelineEvents = btaTask.queryOnlineBTAEventsForPeriodBlocked(fromDate, toDate);
    }

    if (timelineEvents != null) {
      for (TimelineEvent timelineEvent : timelineEvents) {
        if (DEBUG) Log.w(TAG, "BTA top: time " + timelineEvent.getTimestamp() + ", value " + timelineEvent.getValue() +
                ", data " + timelineEvent.getCachedData().get(0).toString());
      }
      displayTop3BTAActivitiesEventOnImageViews(timelineEvents);
    }

    hideProgressBar();
  }

  private void updateBTA(final DateTime fromDate, final DateTime toDate) {
    ListenableFuture<List<TimelineEvent>> listenableFuture = btaTask.queryTop3BTAEventsForPeriod(fromDate, toDate);
    Futures.addCallback(listenableFuture, new FutureCallback<List<TimelineEvent>>() {
      @Override
      public void onSuccess(List<TimelineEvent> result) {
        if (result != null) {
          displayTop3BTAActivitiesEventOnImageViews(result);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              mChart.notifyDataSetChanged();
            }
          });
        }
      }

      @Override
      public void onFailure(Throwable t) {
        Log.e(TAG, "Update BTA from " + fromDate.toString() + " to date " + toDate.toString() + " failed");
      }
    });
  }

  /**
   * Display top three BTA activity events on ImageView
   *
   * @param timelineEvents contain 0-3 {@link TimelineEvent}
   */
  private void displayTop3BTAActivitiesEventOnImageViews(final List<TimelineEvent> timelineEvents) {
    if (DEBUG) Log.i(TAG, "Update 3 tops activities BTA events " + timelineEvents.size());
    btaEvents = timelineEvents;
    if (timelineEvents != null) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          int i = 0;
          for (TimelineEvent timelineEvent : timelineEvents) {
            if (i < 3) {
              btaEventView[i].setTimelineEvent(timelineEvent);
//              if (DEBUG)
//                Log.i(TAG, "Top3: Image url " + timelineEvent.getCachedData().get(0).getImage());

            }
            i++;
          }

          switch (timelineEvents.size()) {
            case 1:
              btaEventView[0].setVisibility(View.VISIBLE);
              btaEventView[1].setVisibility(View.INVISIBLE);
              btaEventView[2].setVisibility(View.INVISIBLE);
              break;
            case 2:
              btaEventView[0].setVisibility(View.VISIBLE);
              btaEventView[1].setVisibility(View.VISIBLE);
              btaEventView[2].setVisibility(View.INVISIBLE);
              break;
            case 3:
              btaEventView[0].setVisibility(View.VISIBLE);
              btaEventView[1].setVisibility(View.VISIBLE);
              btaEventView[2].setVisibility(View.VISIBLE);
              break;
            default:
              btaEventView[0].setVisibility(View.INVISIBLE);
              btaEventView[1].setVisibility(View.INVISIBLE);
              btaEventView[2].setVisibility(View.INVISIBLE);
              break;

          }
        }
      });
    } else {
      Log.w(TAG, "timelineEvents event is null");
    }
  }

  /**
   * show top right small progress bar
   * automatic run on UI thread
   */
  private void showProgressBar() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (menu != null) {
          menu.findItem(R.id.refresh_bsc_menu).setVisible(false);
        }
        progressBar.setVisibility(View.VISIBLE);
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onResume() {
      super.onResume();
      EventBus.getDefault().register(this);


  }

    BTATask.BTAInterface mBTAInterface = new BTATask.BTAInterface() {
        // ANU
        @Override
        public void onCompleted(String command, Pair<String, Object> result, Exception e) {

            {
                if (command.equalsIgnoreCase("start_vda&value=bsc")) {
                    if (e == null) {

                        Log.i(TAG, "Start BSC mode result: " + result.first + " " + result.second);
                        if (result.second instanceof Float &&  ((Float) result.second).floatValue() == 0.0) {
                            try {
                                CommonUtil.setSettingValue(getApplicationContext(), device.getProfile().getRegistrationId()+ "-" + SettingsPrefUtils.MOTION_DETECTION_TYPE, CameraSettingsActivity.MD_TYPE_BSC_INDEX);
                                Thread.sleep(9000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //hideBabyCheckingDialog();
                                        startBTAStep2();
                                    }
                                });
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            Log.e(TAG, "Start BSC mode failed -- 1");
                            if (retry < 3) {
                                retry++;
                                try {
                                    Thread.sleep(4000);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                                btaTask.startBSCMode();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideBabyCheckingDialog();
                                        startFailedWithCommonMessage();
                                    }
                                });
                            }
                        }
                    } else {
                        Log.e(TAG, "Start BSC mode faile -- 2");
                        if (retry < 3) {
                            retry++;
                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            btaTask.startBSCMode();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideBabyCheckingDialog();
                                    startFailedWithCommonMessage();
                                }
                            });
                        }
                        if (e != null) e.printStackTrace();
                    }
                }else if (command.contains("set_bsc_bed_time&start_time=NA&duration=") && !(command.contains("duration=0"))){
                    if (e == null) {
                        Log.i(TAG, "Start BTA result command , value " + result.first + " " + result.second);
                        if (result.second instanceof Float &&  ((Float) result.second).floatValue() == 0.0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //hideBabyCheckingDialog();
                                    remainBTATime = numberOfHour * 3600;
                                    updateBTATimeUI();
                                    checkBabyIsInsightOfCamera();
                                }
                            });
                        } else {
                            if (retry < 3) {
                                try {
                                    Thread.sleep(3000);
                                    retry++;
                                    btaTask.startBTA(numberOfHour * 3600);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            } else {
                                Log.i(TAG, "Start BTA failed -- 1");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideBabyCheckingDialog();
                                        startFailedWithCommonMessage();
                                    }
                                });
                            }
                        }
                    } else {
                        Log.i(TAG, "Start BTA failed -- 2");
                        if (retry < 3) {
                            try {
                                Thread.sleep(3000);
                                retry++;
                                btaTask.startBTA(numberOfHour * 3600);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideBabyCheckingDialog();
                                    startFailedWithCommonMessage();
                                }
                            });
                        }
                    }

                }else if (command.equalsIgnoreCase("set_bsc_bed_time&start_time=NA&duration=0")){
                    if(e == null){
                        if(progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                        remainBTATime = 0;
                        updateBTATimeUI();
                    }else{
                        Toast.makeText(BTAActivity.this, "Cannot stop BTA mode", Toast.LENGTH_LONG).show();
                        if(progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                }
            }

        }
    };
  public void onEventMainThread(PushEvent pushEvent) {
    if (pushEvent.isSameDevice(device.getProfile().getRegistrationId())) {
      updateRecent24hBTAEventsChart(false);
    }
  }

  @Override
  protected void onPause() {
    EventBus.getDefault().unregister(this);
    super.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    isActivityDestroy = true;
      if(executorService != null)
          executorService.shutdown();
    super.onDestroy();
  }

 /* @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.analytics_activity_menu, menu);
    this.menu = menu;
    this.menu.findItem(R.id.refresh_bsc_menu).setVisible(false);
    return true;
  }*/

  /*@Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.refresh_bsc_menu) {
      initialize();
    }
    return true;
  }*/

  private String formatBTATime(int time) {
    int hour = time / ONE_HOUR;
    int min = (time - (hour * ONE_HOUR)) / (60);
    int second = (time - (hour * ONE_HOUR) - (min * (60)));
    String retVal = "";
    retVal += hour >= 10 ? hour + ":" : "0" + hour + ":";
    retVal += min >= 10 ? min + ":" : "0" + min + ":";
    retVal += second >= 10 ? second : "0" + second;
    return retVal;
  }

  @Override
  public void run() {
    while (!isActivityDestroy) {
      if (remainBTATime > 0) {
        remainBTATime -= 1;
        updateBTATimeUI();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void drawDaysAverageChart(int days) {

    List<AverageData> averageDataList = new ArrayList<>();
    try {
      averageDataList = new Select()
          .from(AverageData.class)
          .execute();
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    HashMap<String, Integer> hashMap = new HashMap<>();
    for (AverageData averageData : averageDataList) {
      hashMap.put(averageData.getDate(), (int) averageData.getValue());
    }

    final DateTime now = DateTime.now();
    List<BarEntry> barEntries = new ArrayList<>();
    for (int i = 0; i < days; i++) {
      DateTime day = now.minusDays(i);
      String dayStr = day.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));
      Log.i(TAG, "drawDaysAverageChart, count: " + days + ", date: " + dayStr + ", value " + hashMap.get(dayStr));
      if (hashMap.containsKey(dayStr)) {
//        Log.i(TAG, "Has average value for date : " + dayStr + ", value " + hashMap.get(dayStr));
        barEntries.add(new BarEntry(days - i, hashMap.get(dayStr), day));
      } else {
        barEntries.add(new BarEntry(days - i, 0, day));
      }
    }

    drawDaysChart(now, barEntries, days);
  }

  /**
   * Hide loading bar on the top right of activity
   */
  private void hideProgressBar() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (menu != null) {
          menu.findItem(R.id.refresh_bsc_menu).setVisible(true);
        }
        progressBar.setVisibility(View.GONE);
      }
    });
  }

  private void initializeBarChart() {

    mChart.setDescription("");
    mChart.setDrawGridBackground(false);
    mChart.setDoubleTapToZoomEnabled(false);
    mChart.setPinchZoom(false);
    mChart.getLegend().setEnabled(false);
    mChart.setVisibleXRangeMaximum(15);

    mChart.setExtraLeftOffset(10);
    mChart.setExtraRightOffset(10);
    mChart.setExtraTopOffset(10);
    mChart.setExtraBottomOffset(10);
    mChart.setFitBars(false);

    XAxis xAxis = mChart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setDrawGridLines(false);
    xAxis.setLabelCount(15);
    xAxis.setGranularity(1f); // only intervals of 1 day

    YAxis leftAxis = mChart.getAxisLeft();
    leftAxis.setLabelCount(3, false);
    leftAxis.setGranularity(4f);
    leftAxis.setDrawAxisLine(false);
    leftAxis.setDrawGridLines(true);
    leftAxis.setGridLineWidth(0.7f);
    leftAxis.setGridColor(Color.WHITE);
    leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
    leftAxis.setAxisMaxValue(100f);
    leftAxis.setAxisMinValue(0f);

    mChart.getAxisRight().setEnabled(false);
  }


}
