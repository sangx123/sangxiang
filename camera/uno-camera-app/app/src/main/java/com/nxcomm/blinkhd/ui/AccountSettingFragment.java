package com.nxcomm.blinkhd.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.BadTokenException;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.actors.Actor;
import com.hubble.devcomm.DeviceSingleton;
import com.hubble.framework.service.p2p.P2pManager;
import com.hubble.registration.EScreenName;
import com.hubble.registration.AnalyticsController;
import com.hubble.registration.PublicDefine;
import com.hubble.registration.ui.CommonDialogListener;
import com.hubble.registration.ui.TermOfUseActivity;
import com.hubble.ui.DebugFragment;
import com.hubble.util.CommonConstants;
import com.hubble.util.P2pSettingUtils;

import com.nxcomm.blinkhd.ui.customview.CircularSeekBar;
import com.nxcomm.blinkhd.ui.dialog.ChangePasswordDialog;
import com.nxcomm.blinkhd.ui.dialog.DoNotDisturbDialog;
import com.nxcomm.blinkhd.ui.dialog.NotificationFrequencyDialog;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import base.hubble.Api;
import base.hubble.Models.ApiResponse;
import base.hubble.PublicDefineGlob;
import base.hubble.SubscriptionWrapper;
import base.hubble.meapi.PublicDefines;
import base.hubble.meapi.User;
import base.hubble.meapi.user.LoginResponse2;
import base.hubble.meapi.user.UserInformation;
import base.hubble.subscriptions.UserSubscription;
import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;

public class AccountSettingFragment extends PreferenceFragment {
  private static final String SHOULD_VIDEO_TIMEOUT = "should_video_view_timeout";
  private static final String TAG = "AccountSettingFragment";
  private static final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z]).{8,30})";
  private static final int ACTIVITY_CODE_APPLY_PLAN = 1;

  private Activity mActivity = null;
  private ProgressDialog update_dialog = null;
  Preference debugMode = null;
  Preference subscriptionPlan = null;
  Preference applyPlan = null;
  PreferenceCategory profileCategory = null;
  private String oldPassword;
  private int clicksUntilDebug = 6;
  DoNotDisturbDialog mDoNotDisturbDialog;
  NotificationFrequencyDialog mNotificationFrequencyDialog;
  private SecureConfig settings = HubbleApplication.AppConfig;

  //AA-920: Support Offline Feature on V4.2
  private boolean isOfflineMode;
  private String prevSubscription = null;
  private boolean needRefreshSubscription = true;
  private boolean needCheckSubscriptionPaid = false;
  public static final String SWIPE_TO_PANTILT = "swipe_to_pantilt";
  public AccountSettingFragment() {
    this(false);
  }

  public AccountSettingFragment(boolean isOfflineMode) {
    Bundle extras = new Bundle();
    extras.putBoolean(MainActivity.EXTRA_OFFLINE_MODE, isOfflineMode);
    setArguments(extras);
  }

  private class GetUserSubscriptionsMessage {
  }

  private Actor httpActor = new Actor() {
    @Override
    public Object receive(Object m) {
      try {
        /*if (mActivity != null) {
          setupSubscriptionPreference();
          if (!BuildConfig.ENABLE_SUBSCRIPTIONS && subscriptionPlan != null) {
            mActivity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                subscriptionPlan.setSummary(getString(R.string.none));
              }
            });
            SharedPreferences.Editor editor = subscriptionPlan.getEditor();
            editor.putString("subscription", getString(R.string.none)).apply();
          } else if (subscriptionPlan != null) {
            ApiResponse<SubscriptionWrapper> subs = null;
            try {
              subs = Api.getInstance().getService().getUserSubscriptions(Global.getApiKey(mActivity));
            } catch (Exception e) {
              e.printStackTrace();
            }
            if (subs != null && "200".equals(subs.getStatus())) {
              List<UserSubscription> userSubscriptions = subs.getData().getPlans();
            if (userSubscriptions != null && subscriptionPlan != null) {
              UserSubscription activeSubscription = findActiveSubscription(userSubscriptions);
              SharedPreferences.Editor editor = subscriptionPlan.getEditor();
              editor.remove("subscription");
              final String subscriptionPlanText;
              if (activeSubscription != null) {
                subscriptionPlanText = activeSubscription.getPlanId();
                prevSubscription = subscriptionPlanText;
              } else {
                subscriptionPlanText = mActivity.getString(R.string.none);
              }
              editor.putString("subscription", subscriptionPlanText);
              mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  subscriptionPlan.setSummary(subscriptionPlanText);
                }
              });
              editor.apply();
            }
          }
        }
        }*/
      } catch (Exception e) {
        Log.e(TAG, Log.getStackTraceString(e));
        if (mActivity != null) {
          mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              subscriptionPlan.setSummary(mActivity.getString(R.string.none));
            }
          });
        }
      }
      needCheckSubscriptionPaid = false;
      return null;
    }

    private UserSubscription findActiveSubscription(List<UserSubscription> subscriptions) {
      for (UserSubscription sub : subscriptions) {
        if (sub.getState() != null && sub.getState().equals("active")) {
          return sub;
        }
      }
      return null;
    }
  };

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = super.onCreateView(inflater, container, savedInstanceState);
    return v;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle extras = getArguments();
    if (extras != null) {
      isOfflineMode = extras.getBoolean(MainActivity.EXTRA_OFFLINE_MODE, false);
    }
    setHasOptionsMenu(true);
    if (BuildConfig.FLAVOR.equals("vtech")) {
      addPreferencesFromResource(R.xml.vtech_preference_account);
    } else {
      addPreferencesFromResource(R.xml.hubble_preference_account);
    }
    needCheckSubscriptionPaid = false;
  }

  public void onAttach(Activity activity) {
    super.onAttach(activity);
    this.mActivity = activity;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mActivity = null;
  }

  @Override
  public void onResume() {
    super.onResume();
    ListView list = (ListView) getView().findViewById(android.R.id.list);
    list.setDivider(null);
    if (needRefreshSubscription) {
      httpActor.send(new GetUserSubscriptionsMessage());
      needRefreshSubscription = false;
    }
    //AA-1480
    AnalyticsController.getInstance().trackScreen(EScreenName.Account);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
  }

  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setupProfileCategory();
    setupAboutCategory();
    setupRemoteConnectionCategory();
    //setupPlanCategory();
    if (BuildConfig.FLAVOR.equals("vtech")) {
      setupVTechNotificationCategory();
    } else {
      setupHubbleNotificationCategory();
    }
    if (BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equals("hubblenew")) {
      setupBackgroundMonitoringCategory();//AA-1376: BACKGROUND MONITORING
    }
  }

  private void setupVTechNotificationCategory() {
    final PreferenceCategory notificationCategory = (PreferenceCategory) findPreference("pref_notification_category");
    final CheckBoxPreference checkBoxNotifications = (CheckBoxPreference) notificationCategory.findPreference("notifications_on_off");
    setupToggleSetting(checkBoxNotifications, PublicDefineGlob.PREFS_SHOULD_HAVE_NOTIFICATIONS, true);
  }

  private void setupHubbleNotificationCategory() {
    final PreferenceCategory notificationCategory = (PreferenceCategory) findPreference("pref_notification_category");
    final Preference doNotDisturb = notificationCategory.findPreference("notification_do_not_disturb");
    final Preference notificationFrequency = notificationCategory.findPreference("notification_frequency");
    final CheckBoxPreference checkBoxNotifyBySound = (CheckBoxPreference) notificationCategory.findPreference("notify_by_sound");
    final CheckBoxPreference checkBoxNotifyByVibrate = (CheckBoxPreference) notificationCategory.findPreference("notify_by_vibration");
    final CheckBoxPreference checkBoxNotifyWhenOnCall = (CheckBoxPreference) notificationCategory.findPreference("notify_when_on_call");

    String disturbSummary = getDoNotDisturbSummary();
    doNotDisturb.setSummary(disturbSummary);
    doNotDisturb.setSelectable(!isOfflineMode);
    doNotDisturb.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        if (mDoNotDisturbDialog == null) {
          mDoNotDisturbDialog = new DoNotDisturbDialog();
        }
        mDoNotDisturbDialog.setCancelable(true);
        mDoNotDisturbDialog.setCommonDialogListener(new CommonDialogListener() {

          @Override
          public void onDialogPositiveClick(DialogFragment arg0) {
            CircularSeekBar seekBar = (CircularSeekBar) mDoNotDisturbDialog.findViewById(R.id.dialog_doNotDisturb_seekbar);

            if (seekBar.getProgress() == 0) {
              settings.putBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, false);
            } else {
              DateTime doNotDisturbTime = DateTime.now().plusMinutes(seekBar.getProgress());
              long time = doNotDisturbTime.getMillis();
              settings.putLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, time);
              settings.putBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, true);

              DateTime remaining = new DateTime(time);
              Duration difference = new Duration(DateTime.now().minuteOfDay().roundCeilingCopy(), remaining.minuteOfDay().roundCeilingCopy());
            }

            doNotDisturb.setSummary(getDoNotDisturbSummary());
          }

          @Override
          public void onDialogNegativeClick(DialogFragment dialog) {
          }

          @Override
          public void onDialogNeutral(DialogFragment dialog) {
          }
        });
        if (mActivity != null && mDoNotDisturbDialog != null && !mDoNotDisturbDialog.isResumed()) {
          mDoNotDisturbDialog.show(((ActionBarActivity) mActivity).getSupportFragmentManager(), "do_not_disturb");
        }
        return true;
      }
    });

    notificationFrequency.setSummary(getNotificationFrequencySummary());
    notificationFrequency.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        if (mNotificationFrequencyDialog == null) {
          mNotificationFrequencyDialog = new NotificationFrequencyDialog();
        }
        mNotificationFrequencyDialog.setCancelable(true);
        mNotificationFrequencyDialog.setCommonDialogListener(new CommonDialogListener() {

          @Override
          public void onDialogPositiveClick(DialogFragment arg0) {
            SeekBar seekBar = (SeekBar) mNotificationFrequencyDialog.findViewById(R.id.dialog_notification_seekbar_frequency);

            if (seekBar.getProgress() == seekBar.getMax()) {
              settings.putBoolean(PublicDefineGlob.PREFS_SHOULD_HAVE_NOTIFICATIONS, false);
            } else {
              settings.putInt(PublicDefineGlob.PREFS_MINUTES_BETWEEN_NOTIFICATIONS, seekBar.getProgress());
              settings.putBoolean(PublicDefineGlob.PREFS_SHOULD_HAVE_NOTIFICATIONS, true);
            }

            notificationFrequency.setSummary(getNotificationFrequencySummary());
          }

          @Override
          public void onDialogNegativeClick(DialogFragment dialog) {
          }

          @Override
          public void onDialogNeutral(DialogFragment dialog) {
          }
        });
        if (mActivity != null && mNotificationFrequencyDialog != null && !mNotificationFrequencyDialog.isResumed()) {
          mNotificationFrequencyDialog.show(((ActionBarActivity) mActivity).getSupportFragmentManager(), "notification_frequency");
        }
        return true;
      }
    });

    setupToggleSetting(checkBoxNotifyBySound, PublicDefineGlob.PREFS_NOTIFY_BY_SOUND, BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew"));
    setupToggleSetting(checkBoxNotifyByVibrate, PublicDefineGlob.PREFS_NOTIFY_BY_VIBRATE, BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew"));
    setupToggleSetting(checkBoxNotifyWhenOnCall, PublicDefineGlob.PREFS_NOTIFY_ON_CALL, BuildConfig.FLAVOR.equals("hubble") || BuildConfig.FLAVOR.equalsIgnoreCase("hubblenew"));
  }

  /*private void setupPlanCategory() {
    setupSubscriptionPreference();
    subscriptionPlan.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        needRefreshSubscription = true;
        needCheckSubscriptionPaid = true;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(mActivity.getString(R.string.hubble_web_app_url)));
        startActivity(intent);
        return true;
      }
    });
    if (applyPlan != null) {
      applyPlan.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          Intent intent = new Intent(getActivity(), ApplyPlanActivity.class);
          startActivityForResult(intent, ACTIVITY_CODE_APPLY_PLAN);
          return true;
        }
      });
    }
  }*/

  /*private void setupSubscriptionPreference() {
    final PreferenceCategory storageSettingsCat = (PreferenceCategory) findPreference("pref_plan_category");
    if (subscriptionPlan == null) {
      subscriptionPlan = storageSettingsCat.findPreference("subscription");
      applyPlan = storageSettingsCat.findPreference("applyPlan");
    }
  }*/

  private void setupRemoteConnectionCategory() {
    PreferenceCategory remoteConnectionCategory = (PreferenceCategory) findPreference("pref_remote_connection_category");
    setupToggleSetting((CheckBoxPreference) remoteConnectionCategory.findPreference("string_timeout_alert_setting"), SHOULD_VIDEO_TIMEOUT, true);

    // Setup "Remote P2P Streaming" setting if any
    CheckBoxPreference remoteP2pPref = (CheckBoxPreference) remoteConnectionCategory.findPreference("string_remote_p2p_streaming_setting");
    CheckBoxPreference remoteP2pKeepAlivePref = (CheckBoxPreference) remoteConnectionCategory.findPreference("string_remote_p2p_keep_alive_setting");
    if (P2pSettingUtils.hasP2pFeature()) {
      if (remoteP2pPref != null) {
        setupToggleSetting(remoteP2pPref, PublicDefineGlob.PREFS_IS_P2P_ENABLED, true);
      }

      if (remoteP2pKeepAlivePref != null) {
        if (HubbleApplication.isVtechApp()) {
          setupToggleSetting(remoteP2pKeepAlivePref, P2pManager.PREFS_P2P_KEEP_ALIVE_SETTING, P2pManager.P2P_KEEP_ALIVE_SETTING_DEFAULT);
        } else {
          remoteConnectionCategory.removePreference(remoteP2pKeepAlivePref);
        }
      }
    } else {
      if (remoteP2pPref != null) {
        remoteConnectionCategory.removePreference(remoteP2pPref);
      }
      if (remoteP2pKeepAlivePref != null) {
        remoteConnectionCategory.removePreference(remoteP2pKeepAlivePref);
      }
    }
  }

  //AA-1376: BACKGROUND MONITORING
  private void setupBackgroundMonitoringCategory() {
    PreferenceCategory bgMonitoringCategory = (PreferenceCategory) findPreference("pref_bg_monitoring_category");
    CheckBoxPreference pref = (CheckBoxPreference) bgMonitoringCategory.findPreference("pref_background_monitoring");
    pref.setChecked(settings.isBackgroundMonitoringEnable());
    pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        ((CheckBoxPreference) preference).setChecked(!((CheckBoxPreference) preference).isChecked());
        settings.putBoolean(PublicDefineGlob.PREFS_BACKGROUND_MONITORING, ((CheckBoxPreference) preference).isChecked());
        return false;
      }
    });
  }

  private void setupAboutCategory() {
    PreferenceCategory aboutCategory = (PreferenceCategory) findPreference("pref_about_category");
    Preference termPref = aboutCategory.findPreference("term_and_condition");
    termPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Intent termsIntent = new Intent(mActivity, TermOfUseActivity.class);
        startActivity(termsIntent);

        return false;
      }
    });

    Preference aboutPref = aboutCategory.findPreference("app_version");

    if (settings.getBoolean(DebugFragment.PREFS_DEBUG_ENABLED, false)) {
      addDebugMode();
    } else {
      aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

        @Override
        public boolean onPreferenceClick(Preference preference) {
          clicksUntilDebug--;
          if (clicksUntilDebug == 0) {
            Toast.makeText(mActivity, "Debug mode enabled.", Toast.LENGTH_SHORT).show();
            addDebugMode();
          }
          return false;
        }
      });
    }
    String ver = null;
    if (mActivity != null) {
      PackageInfo pinfo;
      try {
        pinfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
        ver = pinfo.versionName;
      } catch (NameNotFoundException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
        ver = "Unknown";
      }
    }
    aboutPref.setSummary(ver);

    Preference sendLog = aboutCategory.findPreference("sendLog");
    if (!BuildConfig.FLAVOR.equals("hubblenew")) {
      aboutCategory.removePreference(sendLog);
    }

    sendLog.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        sendApplicationLog();
        return false;
      }
    });
  }

  private void setupProfileCategory() {
    profileCategory = (PreferenceCategory) findPreference("pref_profile_category");
    String ourUserEmail = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, "");
    String ourUserName = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, "");
    final int tempPreference = settings.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);

    String tempString = getString(R.string.celsius);
    if (tempPreference == PublicDefineGlob.TEMPERATURE_UNIT_DEG_F) {
      tempString = getString(R.string.fahrenheit);
    }

    Preference userEmail = profileCategory.findPreference("string_PortalUsrId");
    userEmail.setSummary(ourUserEmail);

    Preference userName = profileCategory.findPreference("string_userName");
    userName.setSummary(ourUserName);

    final Preference temperature = profileCategory.findPreference("pref_temp");
    if (temperature != null) {
      temperature.setSummary(tempString);
      temperature.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          final CharSequence[] temps = {getString(R.string.celsius), getString(R.string.fahrenheit)};

          int currentSelection = settings.getInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C) == PublicDefineGlob.TEMPERATURE_UNIT_DEG_C ? 0 : 1;

          AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
          builder.setTitle(mActivity.getString(R.string.select_temperature_units));
          builder.setSingleChoiceItems(temps, currentSelection, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              switch (which) {
                case 0:
                  settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
                  temperature.setSummary(getString(R.string.celsius));
                  break;
                case 1:
                  settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_F);
                  temperature.setSummary(getString(R.string.fahrenheit));
                  break;
                default:
                  settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, PublicDefineGlob.TEMPERATURE_UNIT_DEG_C);
                  temperature.setSummary(getString(R.string.celsius));
                  break;
              }
              dialog.dismiss();
            }
          });
          builder.create().show();
          return true;
        }
      });
    }

    setUpTimeFormat();

    if (BuildConfig.FLAVOR.equals("vtech") && temperature != null) {
      profileCategory.removePreference(temperature);
    }

    Preference changePwd = profileCategory.findPreference("change_pwd");
    changePwd.setSelectable(!isOfflineMode);
    changePwd.setOnPreferenceClickListener(new OnPreferenceClickListener() {

      @Override
      public boolean onPreferenceClick(Preference preference) {
        // TODO : goto Change FWD link
        final ChangePasswordDialog dialog = new ChangePasswordDialog();
        dialog.setCancelable(true);

        dialog.setCommonDialogListener(new CommonDialogListener() {
          @Override
          public void onDialogPositiveClick(DialogFragment arg0) {
            EditText pass = (EditText) dialog.findViewById(R.id.txtNewPassword);
            EditText confirmPass = (EditText) dialog.findViewById(R.id.txtConfirmNewPassword);
            EditText oldpass = (EditText) dialog.findViewById(R.id.txtOldPassword);

            if (pass == null || confirmPass == null || oldpass == null) {
              return;
            }
            // Check if entered Old password is correct
            if (oldpass.length() < 8) {
              issueDialog(getResources().getString(R.string.AccountSettingFragment_oldpasswd_2));
            } else if (oldpass.length() > 30) {
              issueDialog(getResources().getString(R.string.AccountSettingFragment_oldpasswd_3));
            } else if (pass.length() < 8) {
              issueDialog(getResources().getString(R.string.AccountSettingFragment_newpasswd_2));
            } else if (pass.length() > 30) {
              issueDialog(getResources().getString(R.string.AccountSettingFragment_newpasswd_3));
            } else {
              String user_id = settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_ID, null);

              oldPassword = oldpass.getText().toString();
              String newpass_str = pass.getText().toString();
              String confirmnewPass_str = confirmPass.getText().toString();
              Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

              if (!newpass_str.equals(confirmnewPass_str)) {
                issueDialog(getResources().getString(R.string.AccountSettingFragment_newpasswd_1));
              } else if (!pattern.matcher(newpass_str).matches()) {
                issueDialog(getResources().getString(R.string.password_lowercase_uppercase_digit));
              } else if (user_id.equalsIgnoreCase(pass.getText().toString())) {
                issueDialog(getResources().getString(R.string.password_cannot_match_username));
              } else {
                update_dialog = new ProgressDialog(mActivity);
                Spanned msg = Html.fromHtml("<big>" + getResources().getString(R.string.changing_password) + "</big>");
                update_dialog.setMessage(msg);
                update_dialog.setIndeterminate(true);
                update_dialog.show();
                user_logging_in_with_old_pwd(user_id, oldPassword, newpass_str);
              }
            }
            dialog.dismiss();
          }

          @Override
          public void onDialogNegativeClick(DialogFragment dialog) {
          }

          @Override
          public void onDialogNeutral(DialogFragment dialog) {
          }
        });
        dialog.show(((ActionBarActivity) mActivity).getSupportFragmentManager(), "Change_Camera_Name");

        return true;
      }
    });

    if (debugMode == null) {
      debugMode = profileCategory.findPreference("debug_mode");
      profileCategory.removePreference(debugMode);
    }
    Preference logOut = profileCategory.findPreference("logOut");
    logOut.setOnPreferenceClickListener(new OnPreferenceClickListener() {

      @Override
      public boolean onPreferenceClick(Preference preference) {
        showLogoutDialog();

        return false;
      }
    });

    final Preference swipeToPanTilt = profileCategory.findPreference("pref_swipe_to_pan_tilt");
    if(swipeToPanTilt != null) {
      final boolean isSwipeToPanTiltEnable = settings.getBoolean(SWIPE_TO_PANTILT, false);
      final int sumaryTextId = isSwipeToPanTiltEnable ? R.string.enable : R.string.disable;
      swipeToPanTilt.setSummary(sumaryTextId);
      swipeToPanTilt.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          boolean _isSwipeToPanTiltEnable = settings.getBoolean(SWIPE_TO_PANTILT, false);
          if(_isSwipeToPanTiltEnable) {
            swipeToPanTilt.setSummary(R.string.disable);
          } else {
            swipeToPanTilt.setSummary(R.string.enable);
          }
          settings.putBoolean(SWIPE_TO_PANTILT, !_isSwipeToPanTiltEnable);
          return false;
        }
      });
    }
  }

  private void setUpTimeFormat(){
    profileCategory = (PreferenceCategory) findPreference("pref_profile_category");
    final Preference time = profileCategory.findPreference("pref_time");

    if (BuildConfig.FLAVOR.equals("vtech") && time != null) {
      profileCategory.removePreference(time);
    } else {
      final int tempPreference = settings.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0);

      String tempString = getString(R.string.time_am_pm);
      if (tempPreference == 1) {
        tempString = getString(R.string.time_non_am_pm);
      }

      if (time != null) {
        time.setSummary(tempString);
        time.setOnPreferenceClickListener(new OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            final CharSequence[] temps = {getString(R.string.time_am_pm), getString(R.string.time_non_am_pm)};

            int currentSelection = settings.getInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0) == 0 ? 0 : 1;

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(mActivity.getString(R.string.select_temperature_units));
            builder.setSingleChoiceItems(temps, currentSelection, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                  case 0:
                    settings.putInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 0);
                    time.setSummary(getString(R.string.time_am_pm));
                    break;
                  case 1:
                    settings.putInt(PublicDefineGlob.PREFS_TIME_FORMAT_UNIT, 1);
                    time.setSummary(getString(R.string.time_non_am_pm));
                    break;
                  default:
                    settings.putInt(PublicDefineGlob.PREFS_TEMPERATURE_UNIT, 0);
                    time.setSummary(getString(R.string.time_am_pm));
                    break;
                }
                dialog.dismiss();
              }
            });
            builder.create().show();
            return true;
          }
        });
      }
    }
  }

  private String getNotificationFrequencySummary() {
    boolean notificationsEnabled = settings.getBoolean(PublicDefineGlob.PREFS_SHOULD_HAVE_NOTIFICATIONS, true);
    int notifcationFrequency = settings.getInt(PublicDefineGlob.PREFS_MINUTES_BETWEEN_NOTIFICATIONS,
        PublicDefineGlob.PREFS_DEFAULT_NOTIFICATIONS_FREQUENCY);
    String notificationSummary = getString(R.string.disable_notifications);
    if (notificationsEnabled) {
      if (notifcationFrequency == 0) {
        notificationSummary = getString(R.string.immediately);
      } else if (notifcationFrequency == 1) {
        notificationSummary = getString(R.string.one_minute);
      } else {
        notificationSummary = String.format(getString(R.string.blank_minutes), notifcationFrequency);
      }
    }
    return notificationSummary;
  }

  private String getDoNotDisturbSummary() {
    boolean enabled = settings.getBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, false);
    String notificationSummary = getString(R.string.disabled);
    if (enabled) {
      long doNotDisturbDateInMillis;
      try {
        doNotDisturbDateInMillis = settings.getLong(PublicDefineGlob.PREFS_DO_NOT_DISTURB_REMAINING_TIME, DateTime.now().getMillis());
      } catch (ClassCastException exception) {
        doNotDisturbDateInMillis = DateTime.now().getMillis();
      }
      if (doNotDisturbDateInMillis > DateTime.now().getMillis()) {
        DateTime remaining = new DateTime(doNotDisturbDateInMillis);
        Duration difference = new Duration(DateTime.now().minuteOfDay().roundCeilingCopy(), remaining.minuteOfDay().roundCeilingCopy());

        if (difference.getStandardMinutes() <= 0) {
          notificationSummary = getString(R.string.disabled);
          settings.putBoolean(PublicDefineGlob.PREFS_IS_DO_NOT_DISTURB_ENABLE, false);
        } else if (difference.getStandardMinutes() == 1) {
          notificationSummary = getString(R.string.one_minute);
        } else {
          notificationSummary = String.format(getString(R.string.blank_minutes), difference.getStandardMinutes());
        }
      }
    }
    return notificationSummary;
  }

  private void addDebugMode() {
    if (debugMode != null) {
      profileCategory.addPreference(debugMode);
      settings.putBoolean(DebugFragment.PREFS_DEBUG_ENABLED, true);
      debugMode.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          ((MainActivity) mActivity).goToDebugFragment();
          return false;
        }
      });
    }
  }

  private void setupToggleSetting(CheckBoxPreference pref, final String sharedPreferenceName, boolean defaultSetting) {
    pref.setChecked(settings.getBoolean(sharedPreferenceName, defaultSetting));
    pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        ((CheckBoxPreference) preference).setChecked(!((CheckBoxPreference) preference).isChecked());
        settings.putBoolean(sharedPreferenceName, ((CheckBoxPreference) preference).isChecked());
        return false;
      }
    });
  }

  private void showLogoutDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    builder.setMessage(R.string.AccountSettingFragment_logout_confirmation).setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // Do Nothing- User do not want to logout
          }
        }
    ).setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // User really want to logout-> Do logout now
            dialog.dismiss();
            ((MainActivity) mActivity).onUserLogout();
          }
        }
    );

    AlertDialog confirmExitDialog = builder.create();
    confirmExitDialog.setIcon(R.drawable.ic_launcher);
    confirmExitDialog.setTitle(R.string.app_brand_application_name);
    confirmExitDialog.setCancelable(true);
    confirmExitDialog.show();
  }

  private void issueDialog(String dialogString) {
    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    String msg = dialogString;
    builder.setMessage(msg).setCancelable(true).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        }
    );
    builder.create().show();
  }

  private void change_password_success(String userToken) {
    settings.putString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, userToken);
    if(mActivity != null) {
      Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.password_successfully_changed), Toast.LENGTH_SHORT).show();
    }

    if (update_dialog != null && update_dialog.isShowing()) {
      update_dialog.dismiss();
    }
  }

  private void change_password_failed(String error_message) {
    if (update_dialog != null && update_dialog.isShowing()) {
      update_dialog.dismiss();
    }

    AlertDialog.Builder builder;
    AlertDialog alert;
    builder = new AlertDialog.Builder(mActivity);
    builder.setMessage(error_message).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface diag, int which) {
            diag.cancel();
          }
        }
    );

    alert = builder.create();
    alert.show();
  }

  private class ChangePasswordTask extends AsyncTask<String, String, Integer> {
    private static final int UPDATE_SUCCESS = 0x1;
    private static final int UPDATE_FAILED_SERVER_UNREACHABLE = 0x11;
    private static final int UPDATE_FAILED_WITH_DESC = 0x12;

    private String userToken;
    private String oldPassword;
    private String newPassword;
    private String newToken = null;
    private String _error_desc;

    @Override
    protected Integer doInBackground(String... params) {
      userToken = params[0];
      oldPassword = params[1];
      newPassword = params[2];

      int ret = -1;
      try {
        settings.putBoolean(PublicDefine.IS_APP_CHANGE_PWD, true);
        UserInformation user_info = User.changePassword(userToken, oldPassword, newPassword);
        if (user_info != null) {
          if (user_info.getStatus() == HttpURLConnection.HTTP_OK) {
            ret = UPDATE_SUCCESS;
            String userName = user_info.getName();
            try_login(userName, newPassword);
          } else {
            ret = UPDATE_FAILED_WITH_DESC;
            _error_desc = user_info.getMessage();
          }
        }
      } catch (SocketTimeoutException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
        ret = UPDATE_FAILED_SERVER_UNREACHABLE;
      } catch (MalformedURLException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
        ret = UPDATE_FAILED_SERVER_UNREACHABLE;
      } catch (IOException e) {
        // // Log.e(TAG, Log.getStackTraceString(e));
        ret = UPDATE_FAILED_SERVER_UNREACHABLE;
      }

      return Integer.valueOf(ret);
    }

    private void try_login(String userName, String userPassword) {
      int retries = 5;
      do {
        try {
          LoginResponse2 login_res = User.login2(userName, userPassword);
          if (login_res != null) {
            if (login_res.getStatus() == HttpURLConnection.HTTP_OK) {
              String usrToken = login_res.getAuthenticationToken();
              if (usrToken != null) {
                newToken = usrToken;
                break;
              }
            }
          }

        } catch (SocketTimeoutException e1) {
          // // Log.e(TAG, Log.getStackTraceString(e1));
        } catch (MalformedURLException e1) {
          // // Log.e(TAG, Log.getStackTraceString(e1));
        } catch (IOException e1) {
          // // Log.e(TAG, Log.getStackTraceString(e1));
        }

        retries--;

      } while (retries > 0);

    }

    @Override
    protected void onPostExecute(Integer result) {
      if (result.intValue() == UPDATE_SUCCESS) {
        DeviceSingleton.getInstance().init(newToken, HubbleApplication.AppContext);
        change_password_success(newToken);
      } else {
        change_password_failed(_error_desc);
      }
    }
  }

  class LoginWithOldPwdTask extends AsyncTask<String, String, Integer> {

    private String usrName;
    private String usrPwd;
    private String usrToken;

    /*
     * 20150519_bhavesh add newpassword variable to pass it when
     * authorization successfull with old password
     */
    private String newpassword;

    private String _error_desc;

    private static final int USER_LOGIN_SUCCESS = 0x1;
    private static final int USER_LOGIN_FAILED_PWD_NOT_CORRECT = 0x2;
    private static final int USER_LOGIN_FAILED_SERVER_UNREACHABLE = 0x11;
    private static final int USER_LOGIN_FAILED_WITH_DESC = 0x12;

    @Override
    protected Integer doInBackground(String... params) {

      // BlinkHDApplication.KissMetricsRecord("User Login");
      usrName = params[0];
      usrPwd = params[1];

      // save newpassword here
      newpassword = params[2];

      usrToken = null;
      int ret = -1;
      int retry = 1;
      do {
        try {
          PublicDefines.setHttpTimeout(30000);
          LoginResponse2 login_res = User.login2(usrName, usrPwd);
          if (login_res != null) {
            if (login_res.getStatus() == HttpURLConnection.HTTP_OK) {
              usrToken = login_res.getAuthenticationToken();
              // Log.i(TAG, "Auth Token =>" + usrToken);
              if (usrToken != null) {
                ret = USER_LOGIN_SUCCESS;
                break;
              } else {
                ret = USER_LOGIN_FAILED_SERVER_UNREACHABLE;
              }
            } else if (login_res.getStatus() < 500) {
              ret = USER_LOGIN_FAILED_WITH_DESC;
              _error_desc = login_res.getMessage();
            } else {
              ret = USER_LOGIN_FAILED_SERVER_UNREACHABLE;
            }
          } else {
            ret = USER_LOGIN_FAILED_SERVER_UNREACHABLE;
          }
        } catch (SocketTimeoutException e1) {
          // // Log.e(TAG, Log.getStackTraceString(e1));
          // // Log.d(TAG, "retry #: " + (retry - 1) + " Login exception: " + e1.getLocalizedMessage());
          ret = USER_LOGIN_FAILED_SERVER_UNREACHABLE;
        } catch (MalformedURLException e1) {
          // // Log.e(TAG, Log.getStackTraceString(e1));
          // // Log.d(TAG, "retry #: " + (retry - 1) + " Login exception: " + e1.getLocalizedMessage());
          ret = USER_LOGIN_FAILED_SERVER_UNREACHABLE;
        } catch (IOException e1) {
          // // Log.e(TAG, Log.getStackTraceString(e1));
          // // Log.d(TAG, "retry #: " + (retry - 1) + " Login exception: " + e1.getLocalizedMessage());
          ret = USER_LOGIN_FAILED_SERVER_UNREACHABLE;
        } finally {
          if (isCancelled()) {
            // // Log.d(TAG, "Login cancelled");
            retry = 0;
          }
        }

      } while (--retry > 0);

      return Integer.valueOf(ret);
    }

    /* UI thread */
    protected void onPostExecute(Integer result) {

      switch (result.intValue()) {
        case USER_LOGIN_SUCCESS:
          // we got success so passing newpassword here to call
          // Changepassword Webservice
          user_login_with_old_password_success(usrToken, oldPassword, newpassword);
          break;
        case USER_LOGIN_FAILED_PWD_NOT_CORRECT:
          user_login_with_old_password_failed();
          break;
        case USER_LOGIN_FAILED_SERVER_UNREACHABLE:
          user_login_with_old_password_failed_to_connect();
          break;
        case USER_LOGIN_FAILED_WITH_DESC:
          user_login_with_old_password_failed_with_desc();
          break;

        default:
          break;
      }
    }
  }

  private void user_logging_in_with_old_pwd(String userName, String passwrd, String newpasswrd) {
    LoginWithOldPwdTask loginWithOldPwdTask = new LoginWithOldPwdTask();
    /*
     * passing three parameter : 1)username 2)oldpasswrd 3)newpasswrd
		 */
    loginWithOldPwdTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, userName, passwrd, newpasswrd);

  }

  private void user_login_with_old_password_success(String usrToken, String oldPassword, String newPasswrd) {
    Log.i(TAG, "inside user_login_with_old_password_success");
    /*
     * Call Change Password Webservice after Successfull authorization with
		 * old passwrd
		 */
    ChangePasswordTask rename = new ChangePasswordTask();
    rename.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, usrToken, oldPassword, newPasswrd);
  }

  private void user_login_with_old_password_failed() {
    /*
     * Dismiss update Dialog
		 */
    if (update_dialog != null && update_dialog.isShowing()) {
      update_dialog.dismiss();
    }
    AlertDialog.Builder builder;
    AlertDialog alert;
    builder = new AlertDialog.Builder(mActivity);
    builder.setMessage(getResources().getString(R.string.LoginOrRegistrationActivity_login_2)
    ).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface diag, int which) {
                diag.cancel();
              }
            }
    );
    alert = builder.create();
    alert.show();

  }

  private void user_login_with_old_password_failed_with_desc() {
    /*
     * Dismiss update Dialog
		 */
    if (update_dialog != null && update_dialog.isShowing()) {
      update_dialog.dismiss();
    }

    AlertDialog.Builder builder;
    AlertDialog alert;
    builder = new AlertDialog.Builder(mActivity);
    builder.setMessage(getResources().getString(R.string.AccountSettingFragment_oldpasswd_1)
    ).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface diag, int which) {
            diag.cancel();

          }
        }
    );

    alert = builder.create();
    alert.show();
  }

  private void user_login_with_old_password_failed_to_connect() {

		/*
     * Dismiss update Dialog
		 */
    if (update_dialog != null && update_dialog.isShowing()) {
      update_dialog.dismiss();
    }

    AlertDialog.Builder builder;
    AlertDialog alert;
    builder = new AlertDialog.Builder(mActivity);
    Spanned msg = Html.fromHtml(getString(R.string.login_failed_server_error));
    builder.setMessage(msg)
        .setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface diag, int which) {
            diag.cancel();

          }
        }
    );

    alert = builder.create();
    try {
      alert.show();

      // Make the url in dialog message clickable. Must be called after show()
      TextView tv = (TextView) alert.findViewById(android.R.id.message);
      if (tv != null) {
        tv.setMovementMethod(LinkMovementMethod.getInstance());
      }
    } catch (BadTokenException bad) {
    }
  }

  private void sendApplicationLog() {
    Crittercism.logHandledException(new Throwable(String.valueOf(System.currentTimeMillis())));

    String fileName = "logcat_" + System.currentTimeMillis() + ".txt";
    File outputFile = new File(getActivity().getExternalCacheDir(), fileName);
    String encLogFileName = "encrypt_" + fileName;
    File encLogFile = new File(getActivity().getExternalCacheDir(), encLogFileName);

    /*
     * 20151202: HOANG: AA-1276
     * Use same collecting log flow for all apps.
     */
    // getAppLog(outputFile.getAbsolutePath());
    String logFilePath = HubbleApplication.getLogFilePath();
    outputFile = new File(logFilePath);
    Log.d("mbp", "Send Hubble log file length: " + outputFile.length());

    HubbleApplication.writeLogAndroidDeviceInfo();

    // Temporary stop print debug for preparing to encrypt log
    HubbleApplication.stopPrintAdbLog();

    // Zip log file to reduce file length
    String zipLogFilePath = logFilePath.replace(".log", ".zip");
    File zipLogFile = new File(zipLogFilePath);
    Log.d("mbp", "Zip log file path: " + zipLogFile.getAbsolutePath());
    HubbleApplication.zipLogFile(zipLogFilePath);

    // Encrypt the log file
    try {
      //encrypt(outputFile.getAbsolutePath(), encLogFile.getAbsolutePath());
      encrypt(zipLogFilePath, encLogFile.getAbsolutePath());
      Log.d("mbp", "Send enc Hubble log file length: " + encLogFile.length());
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    HubbleApplication.startPrintAdbLog();

    String titleEmail = "";
    String bodyEmail = getString(R.string.body_email);

    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"android.techsupport@hubblehome.com"});
    // Send encrypted file
    Uri contentUri;
    if (BuildConfig.FLAVOR.equalsIgnoreCase("vtech")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.vtech.fileprovider", encLogFile);
      titleEmail = String.format(getString(R.string.title_email), "Vtech", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
    } else if (BuildConfig.FLAVOR.equalsIgnoreCase("inanny")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.inanny.fileprovider", encLogFile);
      titleEmail = String.format(getString(R.string.title_email), "iNanny", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
    } else if (BuildConfig.FLAVOR.equalsIgnoreCase("beurer")) {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, "in.beurer.fileprovider", encLogFile);
      titleEmail = String.format(getString(R.string.title_email), "Beurer", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
    } else {
      contentUri = FileProvider.getUriForFile(HubbleApplication.AppContext, CommonConstants.FILE_PROVIDER_AUTHORITY_HUBBLE, encLogFile);
      titleEmail = String.format(getString(R.string.title_email), "Hubble", BuildConfig.VERSION_NAME, settings.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_USR, ""));
    }

    sendIntent.putExtra(Intent.EXTRA_SUBJECT, titleEmail);
    sendIntent.putExtra(Intent.EXTRA_TEXT, bodyEmail);
    sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
    sendIntent.setType("text/plain");
    startActivity(sendIntent);
  }

  private void encrypt(String plainFilePath, String cipherFilePath)
          throws IOException, NoSuchAlgorithmException,
          NoSuchPaddingException, InvalidKeyException {
    // Here you read the cleartext.
    FileInputStream fis = new FileInputStream(plainFilePath);
    // This stream write the encrypted text. This stream will be wrapped by
    // another stream.
    FileOutputStream fos = new FileOutputStream(cipherFilePath);

    // Length is 16 byte
    SecretKeySpec sks = new SecretKeySpec("Super-LovelyDuck".getBytes(), "AES");
    // Create cipher
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.ENCRYPT_MODE, sks);
    // Wrap the output stream
    CipherOutputStream cos = new CipherOutputStream(fos, cipher);
    // Write bytes
    int b;
    byte[] d = new byte[4096];
    while ((b = fis.read(d)) != -1) {
      cos.write(d, 0, b);
    }
    // Flush and close streams.
    cos.flush();
    cos.close();
    fis.close();
  }
}