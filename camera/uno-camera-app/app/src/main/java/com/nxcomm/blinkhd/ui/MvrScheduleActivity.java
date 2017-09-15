package com.nxcomm.blinkhd.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Iterables;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.registration.PublicDefine;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.customview.MvrScheduleView;
import com.nxcomm.blinkhd.ui.dialog.MvrScheduleCrudActivity;
import com.util.CommonUtil;
import com.util.SettingsPrefUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import base.hubble.Api;
import base.hubble.Models;
import base.hubble.PublicDefineGlob;
import retrofit.RetrofitError;

public class MvrScheduleActivity extends ActionBarActivity {

  private static final String TAG = "MvrScheduleActivity";

  private String deviceRegistrationId;
  private HashMap<String, ArrayList<String>> dataBackup;
  private HashMap<String, ArrayList<String>> dataToDraw;
  private MvrScheduleView mvrScheduleView;
  private SwipeRefreshLayout swipeLayout;
  private boolean isLoadingMvrSchedule;
  private boolean isConcurrentData;

  private BroadcastReceiver broadcaster;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fragment_mvr_schedule);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(getString(R.string.scheduler_title));

    Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      deviceRegistrationId = bundle.getString("regId");
      Object temp = bundle.getSerializable("drawnData");
      if (temp != null) {
        dataToDraw = (HashMap<String, ArrayList<String>>) temp;
      }
      temp = bundle.getSerializable("dataBackup");
      if (temp == null) {
        dataBackup = dataToDraw;
        getIntent().putExtra("dataBackup", dataBackup);
      } else {
        Log.d("debug", "restore dataBackup from bundle");
        dataBackup = (HashMap<String, ArrayList<String>>) temp;
        bundle.remove("dataBackup");
      }
    }

    mvrScheduleView = (MvrScheduleView) findViewById(R.id.view_schedule);
    mvrScheduleView.setListener(new MvrScheduleView.Listener() {
      @Override
      public void onScheduleClicked(String timeSpan, int column) {
        go2MvrScheduleCrudActivity(timeSpan, column, false);
      }

      @Override
      public void onCreateSchedule(String timeSpan, int column) {
        go2MvrScheduleCrudActivity(timeSpan, column, true);
      }
    });
    mvrScheduleView.setDataToDraw(dataToDraw);

    ImageButton img = (ImageButton) findViewById(R.id.btn_add_schedule);
    img.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mvrScheduleView == null || mvrScheduleView.isSelectionMode()) {
          return;
        }
        go2MvrScheduleCrudActivity(null, -1, true);
      }
    });

    swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
    swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        loadMvrSchedule();
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    broadcaster = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Needs to close video view");
        if (deviceRegistrationId == null) {
          return;
        }

        final String notification_MAC = intent.getExtras().getString("regId");
        Log.i(TAG, "Selected camera MAC : " + deviceRegistrationId);
        Log.i(TAG, "Notification MAC : " + notification_MAC);
        if (deviceRegistrationId.equals(notification_MAC)) {
          if (!isDestroyed()) {
            show_Device_removal_dialog();
          } else {
            Log.i(TAG, "Activity has stopped, don't show device removed dialog.");
          }
        } else {
          Log.i(TAG, "Not current device, don't show device removed dialog.");
        }
      }
    };
    try {
      IntentFilter intentFilter = new IntentFilter(PublicDefine.NOTIFY_NOTIFY_DEVICE_REMOVAL);
      registerReceiver(broadcaster, intentFilter);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void show_Device_removal_dialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.device_removal_confirmation)
        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            setResult(PublicDefine.CODE_DEVICE_REMOVAL);
            finish();
          }
        });

    AlertDialog confirmDeviceRemovalDialog = builder.create();
    confirmDeviceRemovalDialog.setCancelable(false);
    confirmDeviceRemovalDialog.setCanceledOnTouchOutside(false);
    try {
      confirmDeviceRemovalDialog.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (mvrScheduleView == null || !mvrScheduleView.isSelectionMode()) {
      getMenuInflater().inflate(R.menu.menu_mvr_schedule, menu);
    } else {
      getMenuInflater().inflate(R.menu.menu_mvr_schedule_delete, menu);
    }
    return true;
  }

  @Override
  public void onBackPressed() {
    if (dataToDraw != null) {
      if (checkEdited()) {
        askSavingData();
      } else {
        saveResultData();
        super.onBackPressed();
      }
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected void onStop() {
    try {
      unregisterReceiver(broadcaster);
    } catch (Exception e) {
      e.printStackTrace();
    }
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    // unregister broadcast camera removing
    try {
      if (dialog != null && dialog.isShowing()) {
        dialog.dismiss();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    super.onDestroy();
  }

  private boolean checkEdited() {
    boolean isEdited = false;
    for (String day : PublicDefine.KEYS) {
      if (!Iterables.elementsEqual(dataToDraw.get(day), dataBackup.get(day))) {
        isEdited = true;
        break;
      }
    }
    return isEdited;
  }

  private void askSavingData() {
    if (isConcurrentData) {
      showOptionDialog(getString(R.string.save_schedule_when_concurrent), getString(R.string.continue_saving),
          getString(R.string.quit_without_saving), null, new Listener() {
            @Override
            public void onOptionSelected(int option) {
              if (option == R.id.btn_option1) {
                Models.DeviceSchedule schedule = new Models.DeviceSchedule();
                schedule.inverse(dataToDraw);
                submitScheduleData(schedule.getSchedule(), dataToDraw, false, true);
              } else if (option == R.id.btn_option2) {
                finish();
              }
            }
          });
    } else {
      showOptionDialog(getString(R.string.ask_save_schedule_modify), getString(R.string.save_and_exit),
          getString(R.string.quit_without_saving), null, new Listener() {
            @Override
            public void onOptionSelected(int option) {
              if (option == R.id.btn_option1) {
                Models.DeviceSchedule schedule = new Models.DeviceSchedule();
                schedule.inverse(dataToDraw);
                submitScheduleData(schedule.getSchedule(), dataToDraw, false, true);
              } else if (option == R.id.btn_option2) {
                finish();
              }
            }
          });
    }
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case android.R.id.home:
        if (checkEdited()) {
          askSavingData();
        } else {
          saveResultData();
          finish();
        }
        return true;
      case R.id.btn_delete_all:
        new AlertDialog.Builder(this).setMessage(R.string.delete_all_schedule_confirm)
            .setPositiveButton(R.string.Delete, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                dataToDraw = new Models.DeviceScheduleSub().getDrawData();
                mvrScheduleView.setDataToDraw(dataToDraw);
                saveNewDataToIntent();
                invalidateOptionsMenu();
                showSaveHintDialog();
              }
            })
            .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
              }
            }).show();
        return true;
      case R.id.btn_delete_selected:
        if (mvrScheduleView != null && mvrScheduleView.canSwitchToSelectionMode()) {
          mvrScheduleView.setSelectionMode(true);
          invalidateOptionsMenu();
        }
        return true;
      case R.id.btn_cancel:
        if (mvrScheduleView != null) {
          mvrScheduleView.setSelectionMode(false);
          invalidateOptionsMenu();
        }
        return true;
      case R.id.btn_delete:
        dataToDraw = mvrScheduleView.getDataAfterClear();
        mvrScheduleView.setDataToDraw(dataToDraw);
        saveNewDataToIntent();
        invalidateOptionsMenu();
        showSaveHintDialog();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // camera is removed
    if (resultCode == PublicDefine.CODE_DEVICE_REMOVAL) {
      setResult(PublicDefine.CODE_DEVICE_REMOVAL);
      finish();
      return;
    }

    if (requestCode != 1 || resultCode != Activity.RESULT_OK || data == null) {
      return;
    }
    Bundle bundle = data.getExtras();
    if (bundle == null) {
      return;
    }
    dataToDraw = (HashMap<String, ArrayList<String>>) bundle.getSerializable("newDrawnData");
    saveNewDataToIntent();
    mvrScheduleView.setDataToDraw(dataToDraw);
    // hint user that their schedule is not saved yet
    boolean dataIsChanged = false;
    for (String day : PublicDefine.KEYS) {
      if (!Iterables.elementsEqual(dataToDraw.get(day), dataBackup.get(day))) {
        dataIsChanged = true;
        break;
      }
    }
    if (dataIsChanged) {
      showSaveHintDialog();
    }
  }

  private void showSaveHintDialog() {
    SecureConfig settings = HubbleApplication.AppConfig;
    boolean shouldHintUser = settings.getBoolean(PublicDefine.PREFS_HINT_SCHEDULE_ONLY_SAVE_WHEN_EXIT, true);
    if (shouldHintUser) {
      settings.putBoolean(PublicDefine.PREFS_HINT_SCHEDULE_ONLY_SAVE_WHEN_EXIT, false);
      new AlertDialog.Builder(this).setMessage(R.string.schedule_action_not_save_to_remote_yet)
          .setPositiveButton(R.string.dialog_ok, null).show();
    }
  }

  private void go2MvrScheduleCrudActivity(String timeSpan, int column, boolean isAddingMode) {
    Intent intent = new Intent(this, MvrScheduleCrudActivity.class);
    intent.putExtra("regId", deviceRegistrationId);
    intent.putExtra("addMode", isAddingMode);
    if (!TextUtils.isEmpty(timeSpan)) {
      intent.putExtra("timeSpan", timeSpan);
      intent.putExtra("column", column);
    }
    if (dataToDraw != null) {
      intent.putExtra("data", dataToDraw);
    }
    startActivityForResult(intent, 1);
  }

  private void submitScheduleData(final Models.DeviceScheduleSub tempSchedule, final HashMap<String,
      ArrayList<String>> tempDataToDraw, final boolean invalidateMenu, final boolean finishAfterDone) {
    final Models.DeviceScheduleSubmit obj = new Models.DeviceScheduleSubmit();
    obj.setRegistrationId(deviceRegistrationId);
    obj.setDeviceSchedule(new Models.DeviceSchedule(tempSchedule));

    final Dialog mDialog = ProgressDialog.show(MvrScheduleActivity.this, null, getString(R.string.saving_schedule));
    new Thread(new Runnable() {
      @Override
      public void run() {
        Models.ApiResponse<String> resFinal = null;
        String apiKey = Global.getApiKey(MvrScheduleActivity.this);
        try {
          resFinal = Api.getInstance().getService().submitDeviceSchedule(apiKey, obj);
        }catch(RetrofitError e){
          resFinal = null;
          Log.e(TAG,"Retrofit error while submitDeviceSchedule");
        }
        final Models.ApiResponse<String> res = resFinal;
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try {
              mDialog.dismiss();
            } catch (Exception e) {
            }
            if (res == null || !res.getStatus().equalsIgnoreCase("200")) {
              Toast.makeText(MvrScheduleActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(MvrScheduleActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
              CommonUtil.setSettingSchedule(MvrScheduleActivity.this, deviceRegistrationId + "-" + SettingsPrefUtils.MVR_SCHEDULE, new Models.DeviceSchedule(tempSchedule));
              dataToDraw = tempDataToDraw;
              saveNewDataToIntent();
              mvrScheduleView.setDataToDraw(dataToDraw);
              if (invalidateMenu) {
                invalidateOptionsMenu();
              }
              if (finishAfterDone) {
                saveResultData();
                finish();
              }
            }
          }
        });
      }
    }).start();
  }

  private void saveResultData() {
    Intent intent = new Intent();
    intent.putExtra("newDrawnData", dataToDraw);
    setResult(Activity.RESULT_OK, intent);
  }

  private void loadMvrSchedule() {
    if (isLoadingMvrSchedule) {
      return;
    }
    isLoadingMvrSchedule = true;
    new Thread(new Runnable() {
      @Override
      public void run() {
        // start request
        Log.i("debug", "Start getting mvr schedule");
        String deviceId = DeviceSingleton.getInstance().getSelectedDevice().getProfile().registrationId;
        String apiKey = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
        Models.ApiResponse<Models.DeviceSchedule> res = Api.getInstance().getService().getDeviceSchedule(apiKey, deviceId);
        // request ends
        Log.i("debug", "Getting mvr schedule complete");
        isLoadingMvrSchedule = false;
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            swipeLayout.setRefreshing(false);
          }
        });
        if (res != null && res.getStatus().equalsIgnoreCase("200")) {
          // handle response
          Models.DeviceSchedule tempSchedule = res.getData();
          if (tempSchedule == null) {
            return;
          }
          tempSchedule.parse();
          Log.i("debug", "Parsing mvr schedule response successful");
          if (tempSchedule.getSchedule() == null) {
            return;
          }
          /* now we save schedule only one time when user leave screen. so that we create a backup
            data to detect modifying to ask user saving. When pulling to refresh, new data will override
            backup data and merge with current data to avoiding wrong displaying */
          final HashMap<String, ArrayList<String>> dataServer = tempSchedule.getScheduleData();
          boolean needMerge = false;
          for (String day : PublicDefine.KEYS) {
            if (!Iterables.elementsEqual(dataServer.get(day), dataBackup.get(day))) {
              needMerge = true;
              break;
            }
          }
          if (needMerge) {
            isConcurrentData = true;
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                showOptionDialog(getString(R.string.there_is_new_schedule_data), getString(R.string.use_remote_data),
                    getString(R.string.use_local_data), getString(R.string.merge), new Listener() {
                      @Override
                      public void onOptionSelected(int option) {
                        if (option == R.id.btn_option1) {
                          dataToDraw = dataServer;
                          dataBackup = dataServer;
                          saveNewDataToIntent();
                          mvrScheduleView.setDataToDraw(dataToDraw);
                          isConcurrentData = false;
                        } else if (option == R.id.btn_option2) {
                          isConcurrentData = false;
                        } else {
                          Log.d("debug", "Need merging origin data with new data from server");
                          mergeRemoteData(dataServer);
                          Log.i("debug", "Update mvr schedule screen");
                          saveNewDataToIntent();
                          mvrScheduleView.setDataToDraw(dataToDraw);
                          isConcurrentData = false;
                        }
                      }
                    });
              }
            });
          }
        }
      }
    }).start();
  }

  private void mergeRemoteData(HashMap<String, ArrayList<String>> dataServer) {
    Comparator<String> stringComparator = new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        if (s1 == null) {
          return s2 == null ? 0 : -1;
        }
        return s1.compareTo(s2);
      }
    };
    String temp;
    for (String day : PublicDefine.KEYS) {
      List<String> local = dataToDraw.get(day);
      List<String> server = dataServer.get(day);
      List<String> backup = dataBackup.get(day);
      // remove items that are removed or modified in server
      for (int i = local.size() - 1; i >= 0; i--) {
        temp = local.get(i);
        if (!server.contains(temp) && backup.contains(temp)) {
          local.remove(i);
        }
      }
      // add item that are added in server
      boolean needCheckConflict = false;
      for (int i = 0; i < server.size(); i++) {
        temp = server.get(i);
        if (!local.contains(temp) && !backup.contains(temp)) {
          local.add(temp);
          needCheckConflict = true;
        }
      }
      // resolve overlapping items if there is any
      if (needCheckConflict) {
        Collections.sort(local, stringComparator);
        for (int k = local.size() - 1; k >= 0; k--) {
          local.set(k, adjustScheduleElement(k, local));
        }
      }
    }
    isConcurrentData = false;
    dataBackup = dataServer;
  }

  private String adjustScheduleElement(int index, List<String> elements) {
    if (index == elements.size() - 1) {
      return elements.get(index);
    }

    String element = elements.get(index);
    String[] split = element.split("-");

    int fromHour = Integer.parseInt(split[0]) / 100;
    int fromMinute = Integer.parseInt(split[0]) % 100;

    int toTime = Integer.parseInt(split[1]);
    int toHour = toTime / 100;
    int toMinute = toTime % 100;

    if (index != elements.size() - 1) {
      String nextElement = elements.get(index + 1);
      split = nextElement.split("-");
      int nextFrom = Integer.parseInt(split[0]);
      int nextTo = Integer.parseInt(split[1]);
      if (toTime >= nextFrom && toTime < nextTo) {
        toHour = nextTo / 100;
        toMinute = nextTo % 100;
        elements.remove(index + 1);
      } else if (toTime >= nextTo) {
        elements.remove(index + 1);
      }
    }
    return String.format("%02d%02d-%02d%02d", fromHour, fromMinute, toHour, toMinute);
  }

  private void saveNewDataToIntent() {
    if (getIntent() != null) {
      getIntent().putExtra("drawnData", dataToDraw);
    } else {
      Log.i("debug", "Failed to save new schedule data to intent. Intent is null.");
    }
  }

  private AlertDialog dialog;

  private void showOptionDialog(String message, String btn1Test, String btn2Test, String btn3Test, final Listener listener) {
    // 20160219 binh AA-1549: there are 2 things that must remember when call setView for AlertDialog
    // 1. never pass layout resource id to setView method. it could be crashed in some api.
    // 2. must call setView before anything else.
    dialog = new AlertDialog.Builder(this)
        .setView(getLayoutInflater().inflate(R.layout.dialog_with_long_option, null)).create();
    dialog.setCanceledOnTouchOutside(false);
    dialog.show();
    // handle UI
    TextView txtMessage = (TextView) dialog.findViewById(R.id.txt_message);
    if (txtMessage != null) {
      txtMessage.setText(message);
    }
    View.OnClickListener optionListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dialog.dismiss();
        if (listener != null) {
          listener.onOptionSelected(view.getId());
        }
      }
    };
    Button btnTemp = (Button) dialog.findViewById(R.id.btn_option1);
    if (btnTemp != null) {
      if (TextUtils.isEmpty(btn1Test)) {
        btnTemp.setVisibility(View.GONE);
      } else {
        btnTemp.setVisibility(View.VISIBLE);
        btnTemp.setText(btn1Test);
        btnTemp.setOnClickListener(optionListener);
      }
    }
    btnTemp = (Button) dialog.findViewById(R.id.btn_option2);
    if (btnTemp != null) {
      if (TextUtils.isEmpty(btn2Test)) {
        btnTemp.setVisibility(View.GONE);
      } else {
        btnTemp.setVisibility(View.VISIBLE);
        btnTemp.setText(btn2Test);
        btnTemp.setOnClickListener(optionListener);
      }
    }
    btnTemp = (Button) dialog.findViewById(R.id.btn_option3);
    if (btnTemp != null) {
      if (TextUtils.isEmpty(btn3Test)) {
        btnTemp.setVisibility(View.GONE);
      } else {
        btnTemp.setVisibility(View.VISIBLE);
        btnTemp.setText(btn3Test);
        btnTemp.setOnClickListener(optionListener);
      }
    }
  }

  private interface Listener {
    void onOptionSelected(int option);
  }

}
