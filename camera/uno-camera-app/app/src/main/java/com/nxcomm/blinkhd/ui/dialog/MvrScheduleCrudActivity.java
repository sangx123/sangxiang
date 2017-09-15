package com.nxcomm.blinkhd.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hubble.HubbleApplication;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.Util;
import com.hubbleconnected.camera.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import base.hubble.Models;
import base.hubble.PublicDefineGlob;

public class MvrScheduleCrudActivity extends Activity implements View.OnClickListener {

  private static final String PREFS_SHOW_SCHEDULE_HINT = "show_schedule_hint_";
  private static final int MIN_DURATION = 30;
  private static final String TAG = "MvrScheduleCrudActivity";

  private TextView txtStartTime, txtEndTime;
  private CheckBox[] chkDayOfWeek;

  private String timeSpan; // in format HHmm-HHmm
  private int column; // show index of weekday. exam sunday is 0, monday is 1 ...
  private boolean isAddingMode;
  private TimeEntry mStartTime, mEndTime;
  private HashMap<String, ArrayList<String>> scheduleData;
  private int mHandlingTV; // show which TextView is adjusted time
  private boolean isExceedNextDay;

  // prevent recalculate isEnable again when result in MvrScheduleActivity
  private ProgressDialog mLoadingDialog;
  private TimePickerDialog mPickerDialog;

  private BroadcastReceiver broadcaster;
  private String deviceRegistrationId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fragment_mvr_schedule_crud);

    Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      deviceRegistrationId = bundle.getString("regId");
      isAddingMode = bundle.getBoolean("addMode");
      timeSpan = bundle.getString("timeSpan", null);
      Object temp = bundle.getSerializable("data");
      if (temp != null) {
        scheduleData = (HashMap<String, ArrayList<String>>) temp;
      } else {
        scheduleData = new HashMap<>(7);
      }
    }

    TextView txtTitle = (TextView) findViewById(R.id.txt_title);
    int titleRes = isAddingMode ? R.string.new_schedule : R.string.edit_schedule;
    txtTitle.setText(titleRes);

    txtStartTime = (TextView) findViewById(R.id.txt_start_time);
    txtStartTime.setOnClickListener(this);
    txtEndTime = (TextView) findViewById(R.id.txt_end_time);
    txtEndTime.setOnClickListener(this);

    findViewById(R.id.img_edit_start_time).setOnClickListener(this);
    findViewById(R.id.img_edit_end_time).setOnClickListener(this);

    chkDayOfWeek = new CheckBox[7];
    chkDayOfWeek[0] = (CheckBox) findViewById(R.id.chk_sunday);
    chkDayOfWeek[1] = (CheckBox) findViewById(R.id.chk_monday);
    chkDayOfWeek[2] = (CheckBox) findViewById(R.id.chk_tuesday);
    chkDayOfWeek[3] = (CheckBox) findViewById(R.id.chk_wednesday);
    chkDayOfWeek[4] = (CheckBox) findViewById(R.id.chk_thursday);
    chkDayOfWeek[5] = (CheckBox) findViewById(R.id.chk_friday);
    chkDayOfWeek[6] = (CheckBox) findViewById(R.id.chk_saturday);

    findViewById(R.id.schedule_cancel).setOnClickListener(this);
    findViewById(R.id.btn_save).setOnClickListener(this);
    if (isAddingMode()) {
      findViewById(R.id.btn_delete).setVisibility(View.GONE);
    } else {
      findViewById(R.id.btn_delete).setOnClickListener(this);
    }
    if(isAddingMode){
      Button save = (Button) findViewById(R.id.btn_save);
      RelativeLayout.LayoutParams layoutParams =
              (RelativeLayout.LayoutParams)save.getLayoutParams();
      layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
      layoutParams.setMargins(0, 0, 0, 0);
      save.setLayoutParams(layoutParams);

    }

    /*findViewById(R.id.btn_question_mark).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showHintDialog();
      }
    });*/

    initView(bundle);

    String loginUser = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, null);
    if (!TextUtils.isEmpty(loginUser)) {
      String key = PREFS_SHOW_SCHEDULE_HINT + loginUser;
      boolean showHintForFirstTime = HubbleApplication.AppConfig.getBoolean(key, true);
      if (showHintForFirstTime) {
        Log.i("debug", "Show schedule hint for user: " + loginUser);
        showHintDialog();
        HubbleApplication.AppConfig.putBoolean(key, false);
      }
    }
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

  private void initView(Bundle bundle) {
    switchHandlingTV(0);

    if (isAddingMode() && TextUtils.isEmpty(timeSpan)) {
      mStartTime = new TimeEntry(7, 0);
      mEndTime = new TimeEntry(7, 30);
    } else {
      String[] splts = timeSpan.split("-");

      int fromHour = Integer.parseInt(splts[0].substring(0, 2));
      int fromMinute = Integer.parseInt(splts[0].substring(2));

      mStartTime = new TimeEntry(fromHour, fromMinute);

      int toHour = Integer.parseInt(splts[1].substring(0, 2));
      int toMinute = Integer.parseInt(splts[1].substring(2));

      mEndTime = new TimeEntry(toHour, toMinute);

      // disable all checkbox in edit mode
      if (!isAddingMode()) {
        for (int i = 0; i < chkDayOfWeek.length; i++) {
          chkDayOfWeek[i].setEnabled(false);
        }
      }

      column = bundle.getInt("column", -1);
      if (column >= 0) {
        chkDayOfWeek[column].setChecked(true);
      }
    }

    txtStartTime.setText(mStartTime.getDisplayString());
    txtEndTime.setText(mEndTime.getDisplayString());
  }

  private boolean isAddingMode() {
    return isAddingMode;
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
      if (mPickerDialog != null && mPickerDialog.isShowing()) {
        mPickerDialog.dismiss();
      }
      if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
        mLoadingDialog.dismiss();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    super.onDestroy();
  }

  @Override
  public void onClick(View view) {
    int id = view.getId();
    switch (id) {
      case R.id.txt_start_time:
      case R.id.txt_end_time:
      case R.id.img_edit_start_time:
      case R.id.img_edit_end_time:
        // switch current handled TextView
        switchHandlingTV(id);
        break;
      case R.id.schedule_cancel:
        setResult(Activity.RESULT_CANCELED);
        finish();
        break;
      case R.id.btn_save:
        clickSave();
        break;
      case R.id.btn_delete:
        // nothings to delete in adding mode
        if (isAddingMode()) {
          showAlertDialog(R.string.mvr_item_is_not_existed);
          return;
        }
        // remove element from the schedule
        ArrayList<String> temp = scheduleData.get(PublicDefine.KEYS[column]);
        for (int i = temp.size() - 1; i >= 0; i--) {
          if (!timeSpan.equals(temp.get(i))) {
            continue;
          }
          temp.remove(i);
        }
        // submit new data to server
        if (mLoadingDialog == null) {
          mLoadingDialog = new ProgressDialog(this);
          mLoadingDialog.setMessage(getString(R.string.deleting_schedule));
          mLoadingDialog.setCancelable(false);
        } else {
          mLoadingDialog.setMessage(getString(R.string.deleting_schedule));
        }
        submitCurrentData(scheduleData);
        break;
      default:
        break;
    }
  }

  private void clickSave() {
    String status;
    // continue checking after resolving conflict or start checking
    if (isExceedNextDay) {
      status = checkInputData(mStartTime, new TimeEntry(23, 59));
      if (status.equals("DONE") && !mEndTime.getSubmitString().equals("0000")) {
        status = checkInputData(new TimeEntry(0, 0), mEndTime);
      }
    } else {
      status = checkInputData(mStartTime, mEndTime);
    }
    if (status.equals("DONE")) {
      // build new schedule from the current input
      HashMap<String, ArrayList<String>> tempSchedule = addOrEditItem();
      status = checkMaxItem(tempSchedule);
      if (status.equals("DONE")) {
        status = checkOverlapping(tempSchedule);
        if (status.equals("DONE")) {
          submitCurrentData(tempSchedule);
        } else {
          showDialogSuggestMerge(tempSchedule, status);
        }
      } else {
        showAlertDialog(status);
      }
    } else {
      showAlertDialog(status);
    }
  }

  private String checkInputData(TimeEntry start, TimeEntry end) {
    String status = "DONE";
    // check end time must be after start time
    if (start.compare(end) >= 0) {
      status = getString(R.string.mvr_start_must_smaller_end);
    } else if (Math.abs(start.getDurationWith(end)) < MIN_DURATION - 1) {
      // recording duration is at least 30 minutes
      status = getString(R.string.mvr_record_time_15_minutes);
    } else if (isAddingMode() && !isAnyCheckboxChecked()) {
      // check case no checkbox is checked and be in adding state
      status = getString(R.string.mvr_day_is_not_set);
    }
    return status;
  }

  private String checkMaxItem(HashMap<String, ArrayList<String>> tempSchedule) {
    String status = "DONE";
    for (int i = 0; i < chkDayOfWeek.length; i++) {
      if (!tempSchedule.containsKey(PublicDefine.KEYS[i])) {
        continue;
      }
      int size = tempSchedule.get(PublicDefine.KEYS[i]).size();
      if (size > 5) {
        String day = Util.getStringByName(this, PublicDefine.KEYS[i]);
        status = String.format(getString(R.string.mvr_schedule_error_max_five_items_per_day), day);
        break;
      }
    }
    return status;
  }

  private String checkOverlapping(HashMap<String, ArrayList<String>> tempSchedule) {
    String status = "DONE";
    for (int i = 0; i < chkDayOfWeek.length; i++) {
      String[] overlapItem = checkOverlapWithElements(tempSchedule.get(PublicDefine.KEYS[i]),
          scheduleData.get(PublicDefine.KEYS[i]));
      if (overlapItem == null || overlapItem.length != 2) {
        continue;
      }
      String day = Util.getStringByName(this, PublicDefine.KEYS[i]);
      status = String.format(getString(R.string.mvr_time_not_overlap), day, overlapItem[0], overlapItem[1]);
      break;
    }
    return status;
  }

  private HashMap<String, ArrayList<String>> addOrEditItem() {
    // clone current schedule. we do with the temp schedule to prevent losing
    // current data if somethings goes wrong.
    HashMap<String, ArrayList<String>> tempSchedule = new HashMap<>();
    for (int i = 0; i < PublicDefine.KEYS.length; i++) {
      ArrayList<String> tempArr = new ArrayList<>();
      for (String item : scheduleData.get(PublicDefine.KEYS[i])) {
        tempArr.add(item);
      }
      tempSchedule.put(PublicDefine.KEYS[i], tempArr);
    }
    // remove the edited item from the list
    if (!isAddingMode()) {
      ArrayList<String> temp = tempSchedule.get(PublicDefine.KEYS[column]);
      for (int i = temp.size() - 1; i >= 0; i--) {
        if (temp.get(i).equals(timeSpan)) {
          temp.remove(i);
        }
      }
    }
    // add new item to the list
    if (isExceedNextDay) {
      String newTime1 = mStartTime.getSubmitString() + "-" + new TimeEntry(23, 59).getSubmitString();
      String newTime2 = mEndTime.getSubmitString().equals("0000") ? null :
          new TimeEntry(0, 0).getSubmitString() + "-" + mEndTime.getSubmitString();
      for (int i = 0; i < chkDayOfWeek.length; i++) {
        if (!chkDayOfWeek[i].isChecked()) {
          continue;
        }
        addElementToGroup(tempSchedule, i, newTime1);
        if (!TextUtils.isEmpty(newTime2)) {
          int temp = i < chkDayOfWeek.length - 1 ? i + 1 : 0;
          addElementToGroup(tempSchedule, temp, newTime2);
        }
      }
    } else {
      String newTime = mStartTime.getSubmitString() + "-" + mEndTime.getSubmitString();
      for (int i = 0; i < chkDayOfWeek.length; i++) {
        if (!chkDayOfWeek[i].isChecked()) {
          continue;
        }
        addElementToGroup(tempSchedule, i, newTime);
      }
    }
    // sort the list
    for (String day : PublicDefine.KEYS) {
      ArrayList<String> temp = tempSchedule.get(day);
      if (temp != null && temp.size() > 0) {
        Collections.sort(temp, new StringComparator());
      }
    }
    return tempSchedule;
  }

  private void addElementToGroup(HashMap<String, ArrayList<String>> tempSchedule, int i,
                                 String newTime) {
    if (tempSchedule.containsKey(PublicDefine.KEYS[i])) {
      ArrayList<String> temp = tempSchedule.get(PublicDefine.KEYS[i]);
      temp.add(newTime);
    } else {
      ArrayList<String> temp = new ArrayList<>();
      temp.add(newTime);
      tempSchedule.put(PublicDefine.KEYS[i], temp);
    }
  }

  private void submitCurrentData(HashMap<String, ArrayList<String>> tempSchedule) {
    // prepare data
    Models.DeviceSchedule tempDeviceSchedule = new Models.DeviceSchedule();
    tempDeviceSchedule.inverse(tempSchedule);

    Intent intent = getIntent();
    intent.putExtra("newSchedule", tempDeviceSchedule.getSchedule());
    intent.putExtra("newDrawnData", tempSchedule);

    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  private boolean switchHandlingTV(int id) {
    if (id == 0) {
      mHandlingTV = 0;
    } else {
      if (id == R.id.txt_start_time || id == R.id.img_edit_start_time) {
        mHandlingTV = R.id.txt_start_time;
        showDatePickerDialog(false);
      } else {
        mHandlingTV = R.id.txt_end_time;
        showDatePickerDialog(true);
      }
    }
    return true;
  }

  private void showDatePickerDialog(boolean isEndTime) {
    if (mPickerDialog == null) {
      mPickerDialog = new TimePickerDialog(this, new TimePickerDialog.Listener() {
        @Override
        public void onTimeSet(int hour, int minute, boolean exceedNextDay) {
          if (mHandlingTV == R.id.txt_start_time) {
            mStartTime.hour = hour;
            mStartTime.minute = minute;
            txtStartTime.setText(mStartTime.getDisplayString());
            // auto set for exceed next day
            if (mStartTime.compare(mEndTime) >= 0 && !exceedNextDay) {
              mEndTime.hour = hour;
              mEndTime.minute = minute + 30;
              if (mEndTime.minute == 60) {
                mEndTime.hour++;
                mEndTime.minute = 0;
              }
              if (mEndTime.hour == 24) {
                mEndTime.hour = 23;
                mEndTime.minute = 59;
              }
              txtEndTime.setText(mEndTime.getDisplayString());
            }
          } else if (mHandlingTV == R.id.txt_end_time) {
            if (mStartTime.compare(new TimeEntry(hour, minute)) >= 0 && !exceedNextDay) {
              showAlertDialog(R.string.mvr_start_must_smaller_end);
            } else {
              mEndTime.hour = hour;
              mEndTime.minute = minute;
              txtEndTime.setText(mEndTime.getDisplayString());
            }
          }
          isExceedNextDay = exceedNextDay;
        }
      });
    }
    mPickerDialog.setIsEndTime(isEndTime);
    mPickerDialog.setIsExceedNextDay(isExceedNextDay);
    if (mHandlingTV == R.id.txt_start_time) {
      mPickerDialog.setTime(mStartTime.hour, mStartTime.minute);
    } else if (mHandlingTV == R.id.txt_end_time) {
      mPickerDialog.setTime(mEndTime.hour, mEndTime.minute);
    }
    mPickerDialog.show();
  }

  private boolean isAnyCheckboxChecked() {
    for (CheckBox chk : chkDayOfWeek) {
      if (chk.isChecked())
        return true;
    }
    return false;
  }

  private String[] checkOverlapWithElements(ArrayList<String> elements, ArrayList<String> src) {
    for (int i = 0; i < elements.size() - 1; i++) {
      String element = elements.get(i);
      String nextElement = elements.get(i + 1);

      String[] spiltStr = element.split("-");
      int from = Integer.parseInt(spiltStr[0]);
      int to = Integer.parseInt(spiltStr[1]);

      spiltStr = nextElement.split("-");
      int nextFrom = Integer.parseInt(spiltStr[0]);
      int nextTo = Integer.parseInt(spiltStr[1]);

      if ((nextFrom >= from && nextFrom <= to) || (nextTo >= from && nextTo <= to) ||
          (from >= nextFrom && from <= nextTo) || (to >= nextFrom && to <= nextTo)) {
        TimeEntry tempFrom, tempTo;
        if (src == null || src.size() == 0 || src.contains(element)) {
          tempFrom = new TimeEntry(from / 100, from % 100);
          tempTo = new TimeEntry(to / 100, to % 100);
        } else {
          tempFrom = new TimeEntry(nextFrom / 100, nextFrom % 100);
          tempTo = new TimeEntry(nextTo / 100, nextTo % 100);
        }
        return new String[]{tempFrom.getDisplayString(), tempTo.getDisplayString()};
      }
    }
    return null;
  }

  private void showAlertDialog(int stringRes) {
    showAlertDialog(getString(stringRes));
  }

  private void showAlertDialog(String message) {
    new AlertDialog.Builder(this).setTitle(R.string.time_scheduling_error)
        .setCancelable(false)
        .setMessage(message)
        .setPositiveButton(R.string.dialog_ok, null).show();
  }

  private void showDialogSuggestMerge(final HashMap<String, ArrayList<String>> tempSchedule,
                                      String message) {
    new AlertDialog.Builder(this).setTitle(R.string.time_scheduling_error)
        .setCancelable(false)
        .setMessage(message + "\n\n" + getString(R.string.ask_merge_overlapped_item))
        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            mergeOverlappedSchedule(tempSchedule);
          }
        })
        .setNegativeButton(R.string.dialog_cancel, null).show();
  }

  private void mergeOverlappedSchedule(HashMap<String, ArrayList<String>> tempSchedule) {
    // check and merge overlapped items
    for (int i = 0; i < PublicDefine.KEYS.length; i++) {
      ArrayList<String> tempArr = tempSchedule.get(PublicDefine.KEYS[i]);
      Collections.sort(tempArr, new StringComparator());
      for (int k = tempArr.size() - 1; k >= 0; k--) {
        tempArr.set(k, adjustScheduleElement(k, tempArr));
      }
    }
    submitCurrentData(tempSchedule);
  }

  private String adjustScheduleElement(int index, ArrayList<String> elements) {
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

  private void showHintDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.schedule_requirement_title));
    builder.setMessage(getString(R.string.schedule_requirement_content));
    builder.setCancelable(true).setIcon(R.drawable.ic_launcher);
    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        dialog.dismiss();
      }
    });
    builder.show();
  }

  private class TimeEntry {
    int hour, minute;

    TimeEntry(int hour, int minute) {
      this.hour = hour;
      this.minute = minute;
    }

    String getDisplayString() {
      if (HubbleApplication.AppConfig.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0) == 1) {
        return String.format("%02d:%02d", hour, minute);
      }
      String halfDay = getString(R.string.half_day_am);
      int tempHour = hour;
      if (hour == 0) {
        tempHour = 12;
      }
      if (hour >= 12) {
        if (hour > 12) {
          tempHour = hour - 12;
        }
        halfDay = getString(R.string.half_day_pm);
      }
      return String.format("%d:%02d %s", tempHour, minute, halfDay);
    }

    String getSubmitString() {
      return String.format("%02d%02d", hour, minute);
    }

    int compare(TimeEntry com) {
      if (hour < com.hour) {
        return -1;
      } else if (hour > com.hour) {
        return 1;
      } else {
        if (minute < com.minute) {
          return -1;
        } else if (minute > com.minute) {
          return 1;
        } else {
          return 0;
        }
      }
    }

    int getDurationWith(TimeEntry timeEntry) {
      int to = Integer.parseInt(getSubmitString());
      int from = Integer.parseInt(timeEntry.getSubmitString());
      return to - from;
    }
  }

  private class StringComparator implements Comparator<String> {
    @Override
    public int compare(String s1, String s2) {
      if (s1 == null) {
        return s2 == null ? 0 : -1;
      }
      return s1.compareTo(s2);
    }
  }
}
