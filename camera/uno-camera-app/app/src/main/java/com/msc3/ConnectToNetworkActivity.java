package com.msc3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.hubble.HubbleApplication;
import com.hubble.SecureConfig;
import com.hubble.registration.Util;
import com.hubble.registration.interfaces.IWifiScanUpdater;
import com.hubble.registration.models.NameAndSecurity;
import com.hubble.registration.tasks.ConnectToNetworkTask;
import com.hubble.registration.tasks.WifiScan;
import com.hubble.registration.ui.AccessPointAdapter;
import com.hubble.registration.ui.CommonDialogListener;
import com.hubble.util.CommonConstants;
import com.hubbleconnected.camera.R;
import com.nxcomm.blinkhd.ui.dialog.Notify3gChargeDialog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import base.hubble.PublicDefineGlob;

public class ConnectToNetworkActivity extends FragmentActivity implements IWifiScanUpdater, Handler.Callback {
  private static final String TAG = "ConnectToNetworkAct";

  public static final String bool_ForceChooseWifi = "ConnectToNetworkActivity_bool_ForceChooseWifi";
  public static final String bool_AddCameraWhenUsing3G = "ConnectToNetworkActivity_bool_AddCameraWhenUsing3G";

  private static final int CONNECTING_DIALOG = 0;
  private static final int ASK_WEP_KEY_DIALOG = 1;
  private static final int ASK_WPA_KEY_DIALOG = 2;
  private static final int CONNECTION_FAILED_DIALOG = 3;
  private static final int CONNECT_THRU_MOBILE_NETWORK = 4;
  private static final int CONNECT_THRU_MOBILE_NETWORK_WITH_OPTION = 6;
  private static final int DIALOG_SEARCHING_NETWORK = 7;
  private static final int VERIFY_KEY_DIALOG = 8;

  private String selected_SSID;
  private WifiConfiguration newConf_needs_key;

  private boolean shouldForceEnterWifiPass;
  private boolean addCameraWhenUsing3G;
  private SecureConfig settings = HubbleApplication.AppConfig;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle extra = getIntent().getExtras();
    shouldForceEnterWifiPass = false;
    addCameraWhenUsing3G = false;
    if (extra != null) {
      shouldForceEnterWifiPass = extra.getBoolean(bool_ForceChooseWifi, false);
      addCameraWhenUsing3G = extra.getBoolean(bool_AddCameraWhenUsing3G, false);
    }
  }

  /**
<<<<<<< HEAD
=======
   * Checks whether the "Avoid poor networks" setting (named "Auto network switch" on
   * some Samsung devices) is enabled, which can in some instances interfere with Wi-Fi.
   *
   * @return true if the "Avoid poor networks" or "Auto network switch" setting is enabled
   */
  public static boolean isPoorNetworkAvoidanceEnabled(Context ctx) {
    final int SETTING_UNKNOWN = -1;
    final int SETTING_ENABLED = 1;
    final String AVOID_POOR = "wifi_watchdog_poor_network_test_enabled";
    final String WATCHDOG_CLASS = "android.net.wifi.WifiWatchdogStateMachine";
    final String DEFAULT_ENABLED = "DEFAULT_POOR_NETWORK_AVOIDANCE_ENABLED";
    final ContentResolver cr = ctx.getContentResolver();

    boolean isEnabled = false;
    int result = -1;

    boolean doesPoorNetworkAvoidancExist = false;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      //Setting was moved from Secure to Global as of JB MR1
      result = Settings.Global.getInt(cr, AVOID_POOR, SETTING_UNKNOWN);
      doesPoorNetworkAvoidancExist = true;
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
      result = Settings.Secure.getInt(cr, AVOID_POOR, SETTING_UNKNOWN);
      doesPoorNetworkAvoidancExist = true;
    } else {
      //Poor network avoidance not introduced until ICS MR1
      //See android.provider.Settings.java
      doesPoorNetworkAvoidancExist = false;
    }

    if (doesPoorNetworkAvoidancExist == true) {
      //Exit here if the setting value is known
      if (result != SETTING_UNKNOWN) {
        isEnabled = (result == SETTING_ENABLED);
      } else {
        //Setting does not exist in database, so it has never been changed.
        //It will be initialized to the default value.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
          //As of JB MR1, a constant was added to WifiWatchdogStateMachine to determine
          //the default behavior of the Avoid Poor Networks setting.
          try {
            //In the case of any failures here, take the safe route and assume the
            //setting is disabled to avoid disrupting the user with false information
            Class wifiWatchdog = Class.forName(WATCHDOG_CLASS);
            Field defValue = wifiWatchdog.getField(DEFAULT_ENABLED);
            if (!defValue.isAccessible()) defValue.setAccessible(true);
            isEnabled = defValue.getBoolean(null);
          } catch (IllegalAccessException ex) {
            return false;
          } catch (NoSuchFieldException ex) {
            return false;
          } catch (ClassNotFoundException ex) {
            return false;
          } catch (IllegalArgumentException ex) {
            isEnabled = false;
          }
        } else {
          //Prior to JB MR1, the default for the Avoid Poor Networks setting was
          //to enable it unless explicitly disabled
          isEnabled = false;
        }
      }
    }
    return isEnabled;
  }

  /**
>>>>>>> 20150910_sonnguyen_release_temp
   * @param context
   * @return true if device has mobile network, otherwise false
   */
  public static boolean hasMobileNetwork(Context context) {
    boolean hasMobileNetwork = false;
    ConnectivityManager connMan = (ConnectivityManager)
        context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mobileInfo = connMan.getNetworkInfo(
        ConnectivityManager.TYPE_MOBILE);
    if (mobileInfo != null) {
      hasMobileNetwork = true;
    } else {
      hasMobileNetwork = false;
    }

    return hasMobileNetwork;
  }


  /**
   * REMEMBER!!!!! From Android 5.0, method "getMobileDataEnabled" and "setMobileDataEnabled"
   * have been removed.
   * Solution: For Android 5.0 or higher, use Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) to
   * get mobile network state.
   *
   * @param context
   * @return true if mobile data is enabled, otherwise false.
   * Note: Some devices don't have need to check whether the device has mobile network info or not first by
   * hasMobileNetwork(Context context).
   */
  public static boolean isMobileDataEnabled(Context context) {
    boolean isEnabled = false;
    // Need to check whether mobile network info is null or not
    final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mobileInfo = conman.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      try {

        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
        iConnectivityManagerField.setAccessible(true);
        final Object iConnectivityManager = iConnectivityManagerField.get(conman);
        final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
        final Method getMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
        if (getMobileDataEnabledMethod != null) {
          getMobileDataEnabledMethod.setAccessible(true);
          isEnabled = (Boolean) getMobileDataEnabledMethod.invoke(iConnectivityManager);
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    } else {
      isEnabled = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
    }

    return isEnabled;
  }


  /**
   * Enable or disable mobile data.
   * REMEMBER!!!!! From Android 5.0, method "getMobileDataEnabled" and "setMobileDataEnabled"
   * have been removed, so this method would do nothing.
   *
   * @param context
   * @param enabled
   */
  public static void setMobileDataEnabled(Context context, boolean enabled) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      try {
        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
        iConnectivityManagerField.setAccessible(true);
        final Object iConnectivityManager = iConnectivityManagerField.get(conman);
        final Class iConnectivityManagerClass = Class.forName(
            iConnectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = iConnectivityManagerClass.
            getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        if (setMobileDataEnabledMethod != null) {
          setMobileDataEnabledMethod.setAccessible(true);
          setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchFieldException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  public static void setWifiEnabled(boolean isEnabled) {
    /* re-use Vox_main, just an empty and transparent activity */
    WifiManager w = (WifiManager) HubbleApplication.AppContext.getSystemService(Context.WIFI_SERVICE);
    w.setWifiEnabled(isEnabled);
  }

  public static boolean isWifiEnabledOrEnabling() {
    boolean isWifiEnabledOrEnabling = false;
    WifiManager w = (WifiManager) HubbleApplication.AppContext.getSystemService(Context.WIFI_SERVICE);
    if (w.getWifiState() == WifiManager.WIFI_STATE_ENABLED || w.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
      isWifiEnabledOrEnabling = true;
    }
    return isWifiEnabledOrEnabling;
  }

  public static boolean haveInternetViaOtherMedia(Context context) {
    if (context == null) {
      // you're gonna have a bad time.
      return false;
    }

    ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
      // mobile
      State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
      if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
        // mobile
        return true;
      }
    }
    return false;
  }

  private void showNotificationDialogWhenUse3G() {
    final Notify3gChargeDialog notify3gDialog = new Notify3gChargeDialog();
    notify3gDialog.setCommonDialogListener(new CommonDialogListener() {

      // Handle Yes and Don't ask again button
      @Override
      public void onDialogPositiveClick(DialogFragment dialog) {
        settings.putBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, true);

        setResult(Activity.RESULT_OK);
        finish();
      }

      // if user don't want to use 3G, exit App
      @Override
      public void onDialogNegativeClick(DialogFragment dialog) {
        setResult(CommonConstants.STOP_PLEASE);
        finish();
      }

      @Override
      public void onDialogNeutral(DialogFragment dialog) {
        setResult(Activity.RESULT_OK);
        finish();
      }
    });

    notify3gDialog.show(getSupportFragmentManager(), "NOTIFY_3G_CHARGE");

  }

  @Override
  protected void onStart() {
    super.onStart();
    if (!addCameraWhenUsing3G) {
      final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

      if (shouldForceEnterWifiPass) {
        /* re-use Vox_main, just an empty and transparent activity */
        setContentView(R.layout.bb_is_wifi_screen);

        TextView title = (TextView) findViewById(R.id.textTitle);
        title.setText(R.string.connect_camera_to_wi_fi);

        Button connect = (Button) findViewById(R.id.connectWifi);
        connect.getBackground().setColorFilter(0xff7fae00, android.graphics.PorterDuff.Mode.MULTIPLY);
        connect.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
            wifiManager.setWifiEnabled(true);
            while ((wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED)) {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ignored) {
              }
            }

            try {
              showDialog(DIALOG_SEARCHING_NETWORK);
            } catch (Exception ignored) {
            }

            WifiScan ws = new WifiScan(ConnectToNetworkActivity.this, ConnectToNetworkActivity.this);
            ws.setSilence(true);
            ws.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Scan now");
          }
        });

      } else {
        setContentView(R.layout.bb_is_waiting_screen);

        boolean wifiState = wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
        boolean hasConnectionInfo = wifiManager.getConnectionInfo() != null;
        boolean hasSSID = false;
        boolean ssidIsNotCamera = true;
        if (hasConnectionInfo) {
          hasSSID = wifiManager.getConnectionInfo().getSSID() != null;
          if (hasSSID) {
            for (String cameraSSID : PublicDefineGlob.CAMERA_SSID_LIST) {
              if (wifiManager.getConnectionInfo().getSSID().startsWith(cameraSSID)) {
                ssidIsNotCamera = false;
                break;
              }
            }
          }
        }

        if ((wifiState && hasConnectionInfo && hasSSID) && (ssidIsNotCamera)) {
          // Wifi is up, and we are connecting to some network--
          // simply finish now
          setResult(RESULT_OK);
          finish();
        } else if (haveInternetViaOtherMedia(this)) {
          boolean shouldSkip = settings.getBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, false);
          if (!shouldSkip) {
            showNotificationDialogWhenUse3G();
            // showDialog(CONNECT_THRU_MOBILE_NETWORK_WITH_OPTION);
          } else {
            boolean shouldTurnOnWifi = settings.getBoolean(PublicDefineGlob.PREFS_SHOULD_TURN_ON_WIFI, false);
            if (!shouldTurnOnWifi) {
              setResult(RESULT_OK);
              finish();
            } else {
              turnOnWifiAndScann();
            }
          }
        } else {
          turnOnWifiAndScann();
        }

      }
    } else {
      turnOnWifiAndScann();
      Intent returnIntent = new Intent();
      ConnectToNetworkActivity.this.setResult(RESULT_OK, returnIntent);
    }
  }

  private void turnOnWifiAndScann() {
    /* re-use Vox_main, just an empty and transparent activity */
    setContentView(R.layout.vox_main);
    WifiManager w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    w.setWifiEnabled(true);
    while ((w.getWifiState() != WifiManager.WIFI_STATE_ENABLED)) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignored) {
        // // Log.e(TAG, Log.getStackTraceString(e));
      }
    }

    try {
      showDialog(DIALOG_SEARCHING_NETWORK);
    } catch (Exception ignored) {
    }

    WifiScan ws = new WifiScan(this, this);
    ws.setSilence(true);
    ws.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "Scan now");
  }

  protected void onPause() {
    super.onPause();

  }

  protected void onStop() {
    super.onStop();
  }

  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    final Dialog dialog;
    AlertDialog.Builder builder;
    AlertDialog alert;
    switch (id) {
      case DIALOG_SEARCHING_NETWORK:
        final Dialog scan_dialog = new Dialog(this, R.style.CustomAlertDialogStyle);
        scan_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        scan_dialog.setContentView(R.layout.dialog_wait_for_connecting);
        scan_dialog.setCancelable(true);
        return scan_dialog;
      case VERIFY_KEY_DIALOG:
        final Dialog wait_dialog = new Dialog(this, R.style.CustomAlertDialogStyle);
        wait_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        wait_dialog.setContentView(R.layout.dialog_wait_for_connecting);
        wait_dialog.setCancelable(true);
        wait_dialog.setOnCancelListener(new OnCancelListener() {

          @Override
          public void onCancel(DialogInterface dialog) {
            if (connect_task != null) {
              connect_task.cancel(true);
              connect_task = null;
            }
          }
        });
        return wait_dialog;
      case CONNECTION_FAILED_DIALOG:
        builder = new AlertDialog.Builder(this);
        Spanned msg = Html.fromHtml("<big>" + getResources().getString(R.string.ConnectToNetworkActivity_conn_failed) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
              }
            }
        ).setOnCancelListener(new OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
          }
        });

        alert = builder.create();
        return alert;
      case CONNECTING_DIALOG:
        dialog = new ProgressDialog(this);
        msg = Html.fromHtml("<big>" + getResources().getString(R.string.ConnectToNetworkActivity_connecting) + "</big>");
        ((ProgressDialog) dialog).setMessage(msg);
        ((ProgressDialog) dialog).setIndeterminate(true);
        dialog.setCancelable(true);

        dialog.setOnCancelListener(new OnCancelListener() {

          @Override
          public void onCancel(DialogInterface dialog) {

          }
        });
        ((AlertDialog) dialog).setButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
              }
            }
        );

        return dialog;
      case ASK_WEP_KEY_DIALOG: {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.bb_is_router_key);
        dialog.setCancelable(true);

        String text;
        text = this.selected_SSID.substring(1, this.selected_SSID.length() - 1);

        final TextView ssid_text = (TextView) dialog.findViewById(R.id.t0);
        ssid_text.setText(text);

        final EditText pwd_text = (EditText) dialog.findViewById(R.id.text_key);
        pwd_text.setOnEditorActionListener(new OnEditorActionListener() {

          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
              v.setTransformationMethod(PasswordTransformationMethod.getInstance());
              pwd_text.clearFocus();
            }
            return false;
          }
        });

        final ImageButton btnShowPassword = (ImageButton) dialog.findViewById(R
            .id.is_router_key_password_visible);
        btnShowPassword.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (pwd_text.getInputType() != (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
              pwd_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
              pwd_text.setSelection(pwd_text.getText().length());
            } else {
              pwd_text.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
              pwd_text.setSelection(pwd_text.getText().length());
            }
          }
        });
        Button connect = (Button) dialog.findViewById(R.id.connect_btn);
        connect.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
            Util.hideSoftKeyboard(getApplicationContext(), getCurrentFocus());

            String pass_string;
            EditText text = (EditText) dialog.findViewById(R.id.text_key);

            if (text == null) {
              return;
            }

            pass_string = text.getText().toString();
            if (newConf_needs_key != null) {
              newConf_needs_key.wepTxKeyIndex = 0;// Integer.parseInt(key_index)-1;
              if ((pass_string.length() == 26 || pass_string.length() == 10) && Util.isThisAHexString(pass_string)) {
                newConf_needs_key.wepKeys[newConf_needs_key.wepTxKeyIndex] = pass_string;
              } else {
                newConf_needs_key.wepKeys[newConf_needs_key.wepTxKeyIndex] = '\"' + pass_string + '\"';
              }

            }

            start_connect();

            dialog.cancel();
          }
        });

        Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
            Util.hideSoftKeyboard(getApplicationContext(), getCurrentFocus());
            dialog.cancel();
          }
        });

        dialog.setOnCancelListener(new OnCancelListener() {

          @Override
          public void onCancel(DialogInterface dialog) {
            ConnectToNetworkActivity.this.removeDialog(ASK_WEP_KEY_DIALOG);
          }
        });

        return dialog;
      }
      case ASK_WPA_KEY_DIALOG: {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.bb_is_router_key);
        dialog.setCancelable(true);

        LinearLayout wep_opts = (LinearLayout) findViewById(R.id.wep_options);
        if (wep_opts != null) {
          wep_opts.setVisibility(View.GONE);
        }

        String text;
        text = this.selected_SSID.substring(1, this.selected_SSID.length() - 1);

        final TextView ssid_text = (TextView) dialog.findViewById(R.id.t0);
        ssid_text.setText(text);

        final EditText pwd_text = (EditText) dialog.findViewById(R.id.text_key);
        pwd_text.setOnEditorActionListener(new OnEditorActionListener() {

          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
              v.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
            return false;
          }
        });

        final ImageButton btnShowPassword = (ImageButton) dialog.findViewById(R
            .id.is_router_key_password_visible);
        btnShowPassword.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (pwd_text.getInputType() != (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
              pwd_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
              pwd_text.setSelection(pwd_text.getText().length());
            } else {
              pwd_text.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
              pwd_text.setSelection(pwd_text.getText().length());
            }
          }
        });
        Button connect = (Button) dialog.findViewById(R.id.connect_btn);
        connect.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
            Util.hideSoftKeyboard(getApplicationContext(), getCurrentFocus());

            String pass_string;

            EditText text = (EditText) dialog.findViewById(R.id.text_key);
            if (text == null) {
              return;
            }

            pass_string = text.getText().toString();
            if (newConf_needs_key != null) {
              if ((pass_string.length() == 64) && Util.isThisAHexString(pass_string)) {
                newConf_needs_key.preSharedKey = pass_string;
              } else {
                newConf_needs_key.preSharedKey = '\"' + pass_string + '\"';
              }

            }

            start_connect();
            dialog.cancel();
          }
        });

        Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
          /*
           * Hide keyboard
           */
            Util.hideSoftKeyboard(getApplicationContext(), getCurrentFocus());

            dialog.cancel();
          }
        });
        dialog.setOnCancelListener(new OnCancelListener() {

          @Override
          public void onCancel(DialogInterface dialog) {
            ConnectToNetworkActivity.this.removeDialog(ASK_WPA_KEY_DIALOG);
          }
        });

        return dialog;
      }
      case CONNECT_THRU_MOBILE_NETWORK:
        builder = new AlertDialog.Builder(this);
        msg = Html.fromHtml("<big>" + getResources().getString(R.string.mobile_data_3g_is_enabled_continue_to_connect_may_incur_air_time_charge_do_you_want_to_proceed_) + "</big>");
        builder.setMessage(msg).setCancelable(true).setPositiveButton(getResources().getString(R.string.Proceed), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setResult(RESULT_OK);
                finish();
              }
            }
        ).setNegativeButton(getResources().getString(R.string.turn_on_wifi), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                turnOnWifiAndScann();
              }
            }
        );

        alert = builder.create();
        return alert;
      /*
       * 20130121: hoang: issue 1173 create dialog with option
       * "Don't ask me again"
       */
      case CONNECT_THRU_MOBILE_NETWORK_WITH_OPTION:
        builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout1 = (LinearLayout) inflater.inflate(R.layout.bb_dont_ask_me_again, null);
        final CheckBox dontAskAgain = (CheckBox) layout1.findViewById(R.id.skip);
        msg = Html.fromHtml("<big>" + getResources().getString(R.string.mobile_data_3g_is_enabled_continue_to_connect_may_incur_air_time_charge_do_you_want_to_proceed_) + "</big>");
        builder.setMessage(msg).setView(layout1).setCancelable(true).setPositiveButton(getResources().getString(R.string.Proceed), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                if (dontAskAgain.isChecked()) {
                  settings.putBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, true);
                  settings.putBoolean(PublicDefineGlob.PREFS_SHOULD_TURN_ON_WIFI, false);
                }
                setResult(RESULT_OK);
                finish();
              }
            }
        ).setNegativeButton(getResources().getString(R.string.turn_on_wifi), new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                if (dontAskAgain.isChecked()) {
                  settings.putBoolean(PublicDefineGlob.PREFS_DONT_ASK_ME_AGAIN, true);
                  settings.putBoolean(PublicDefineGlob.PREFS_SHOULD_TURN_ON_WIFI, true);
                }
                turnOnWifiAndScann();
              }
            }
        );

        alert = builder.create();
        return alert;
      default:
        break;
    }

    return null;
  }

  @Override
  public void scanWasCanceled() {
    router_selection(null);
  }

  @Override
  public void updateWifiScanResult(List<ScanResult> result) {
    router_selection(result);
  }

  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

  }

  private int wifi_selected_position;

  private void router_selection(List<ScanResult> results) {
    try {
      dismissDialog(DIALOG_SEARCHING_NETWORK);
    } catch (Exception ignored) {
    }

    setContentView(R.layout.bb_is_wifi_selection);

    TextView title = (TextView) findViewById(R.id.textTitle);
    if (title != null) {
      title.setText(R.string.select_your_wifi_network);
    }

    if (results == null) {
      return;
    }

    ArrayList<NameAndSecurity> ap_list;

    final ListView wifi_list = (ListView) findViewById(R.id.wifi_list);
    if (wifi_list == null) {
      return;
    }

    ap_list = new ArrayList<>(results.size());
    for (ScanResult result : results) {
      if (result.SSID != null) {
        if (!result.SSID.startsWith(PublicDefineGlob.DEFAULT_CAM_NAME)) {
          NameAndSecurity _ns = new NameAndSecurity(result.SSID, result.capabilities, result.BSSID);
          _ns.setLevel(result.level);
          _ns.setHideMac(true);
          ap_list.add(_ns);

        }

      }

    }

    final AccessPointAdapter ap_adapter = new AccessPointAdapter(this, ap_list);
    wifi_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    wifi_list.setAdapter(ap_adapter);
    wifi_selected_position = -1;
    wifi_list.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        // last element is not a NameAndSec object
        wifi_selected_position = position;
        int startPosition = wifi_list.getFirstVisiblePosition();
        int endPosition = wifi_list.getLastVisiblePosition();
        ap_adapter.setSelectedPositision(wifi_selected_position);
        for (int i = 0; i <= endPosition - startPosition; i++) {
          if (wifi_list.getChildAt(i) != null) {
            ImageView checked = (ImageView) parent.getChildAt(i).findViewById(R.id.imgChecked);
            if (i + startPosition == position) {
              checked.setVisibility(View.VISIBLE);
            } else {
              checked.setVisibility(View.INVISIBLE);
            }
          }
        }

      }
    });

    Button connect = (Button) findViewById(R.id.buttonConnect);
    connect.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (wifi_selected_position != -1) {
          NameAndSecurity ns = ((NameAndSecurity) wifi_list.getItemAtPosition(wifi_selected_position));

          String ssid = ns.name; /* no quote */
          WifiManager w = (WifiManager) ConnectToNetworkActivity.this.getSystemService(Context.WIFI_SERVICE);
          if (!shouldForceEnterWifiPass && (w.getWifiState() == WifiManager.WIFI_STATE_ENABLED) && (w.getConnectionInfo() != null) && (w.getConnectionInfo().getSSID() != null) && (w.getConnectionInfo().getSSID().equals(ssid))) {
            if ((w.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED)) {
              ConnectToNetworkActivity.this.setResult(RESULT_OK);
              ConnectToNetworkActivity.this.finish();
              return;
            }
          }

          List<WifiConfiguration> wcs = w.getConfiguredNetworks();
          String checkSSID = '\"' + ssid + '\"';

          boolean foundExisting = false;

          for (WifiConfiguration wc : wcs) {
            if ((wc == null) || (wc.SSID == null)) {
              continue;
            }
            if (wc.SSID.equals(checkSSID)) {

              if (!shouldForceEnterWifiPass && (wc.allowedKeyManagement.equals(ns.getKeyManagement()))) {
                newConf_needs_key = wc;
                // USEful when use to debug - network security
                // connection
                // enable this and use android system to connect
                // and read back the configuration
                // // // Log.d(TAG," 1 Found wc:"+ wc.networkId+
                // " : " + wc.SSID +
                // " algo: " + wc.allowedAuthAlgorithms +
                // " key: " + wc.allowedKeyManagement +
                // " grp:" + wc.allowedGroupCiphers +
                // " pair: " + wc.allowedPairwiseCiphers +
                // " proto:" + wc.allowedProtocols);
                foundExisting = true;
              } else {

                w.removeNetwork(wc.networkId);
              }

            }
          }

          if (!foundExisting) {

            WifiConfiguration newWC = new WifiConfiguration();
            newWC.hiddenSSID = false;
            newWC.SSID = checkSSID;
            newWC.status = WifiConfiguration.Status.ENABLED;
            newWC.hiddenSSID = false;
            newWC.allowedAuthAlgorithms = ns.getAuthAlgorithm();
            newWC.allowedGroupCiphers = ns.getGroupCiphers();
            newWC.allowedKeyManagement = ns.getKeyManagement();
            newWC.allowedPairwiseCiphers = ns.getPairWiseCiphers();
            newWC.allowedProtocols = ns.getProtocols();

            newConf_needs_key = newWC;

            ConnectToNetworkActivity.this.selected_SSID = checkSSID;

            if (ns.security.equals("WEP")) {
              ConnectToNetworkActivity.this.showDialog(ASK_WEP_KEY_DIALOG);
            } else if (ns.security.startsWith("WPA")) {
              ConnectToNetworkActivity.this.showDialog(ASK_WPA_KEY_DIALOG);
            } else {
              start_connect();
            }
          } else {
            start_connect();
          }

        }
      }
    });

  }

  private ConnectToNetworkTask connect_task = null;

  private void start_connect() {

    synchronized (this) {
      if (connect_task != null) {
        // // Log.d(TAG, "Someone else has started .. return ");

        return;
      }
    }

    // TEST 3 cases: WPA, WEP, OPEN
    connect_task = new ConnectToNetworkTask(ConnectToNetworkActivity.this, new Handler(ConnectToNetworkActivity.this));
    connect_task.setIgnoreBSSID(true);

    WifiManager w = (WifiManager) ConnectToNetworkActivity.this.getSystemService(Context.WIFI_SERVICE);
    List<WifiConfiguration> wcs;
    wcs = w.getConfiguredNetworks();

    boolean isFound = false;

    for (WifiConfiguration wc : wcs) {
      if ((wc.SSID != null) && wc.SSID.equals(newConf_needs_key.SSID)) {
        try {
          showDialog(VERIFY_KEY_DIALOG);
        } catch (Exception ignored) {
        }
        connect_task.execute(wc);
        isFound = true;
        break;
      }
    }

    if (!isFound) {
      try {
        showDialog(VERIFY_KEY_DIALOG);
      } catch (Exception ignored) {

      }
      connect_task.execute(newConf_needs_key);
    }

  }

  @Override
  public boolean handleMessage(Message msg) {
    switch (msg.what) {
      case ConnectToNetworkTask.MSG_CONNECT_TO_NW_DONE:
        try {
          dismissDialog(VERIFY_KEY_DIALOG);
        } catch (Exception ignored) {
        }
        this.setResult(RESULT_OK);
        this.finish();
        break;
      case ConnectToNetworkTask.MSG_CONNECT_TO_NW_FAILED:
        try {
          dismissDialog(VERIFY_KEY_DIALOG);
        } catch (Exception ignored) {
        }

        try {
          this.showDialog(CONNECTION_FAILED_DIALOG);
        } catch (Exception ignored) {
        }
        break;
      default:
        break;
    }

    // reset it here
    synchronized (this) {
      connect_task = null;
    }

    return false;
  }
}
