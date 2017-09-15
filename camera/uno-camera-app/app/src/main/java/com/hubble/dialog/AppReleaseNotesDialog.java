package com.hubble.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hubbleconnected.camera.R;
import com.hubbleconnected.camera.BuildConfig;


/**
 * Created by Son Nguyen on 30/11/2015.
 */
public class AppReleaseNotesDialog extends DialogFragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Set a theme on the dialog builder constructor!
    AlertDialog.Builder builder =
        new AlertDialog.Builder(getActivity());

    String title = "";
    Spanned message = null;
    Bundle bundle = getArguments();

    if (bundle != null) {
      title = getActivity().getString(R.string.app_update_available);
      String titletext = "<p>" + String.format(getActivity().getString(R.string.new_app_version_released), getString(R.string.app_brand_application_name)) + "</p";
      titletext += "<br><p>" + getString(R.string.whats_new) + "</p>";
      message = Html.fromHtml(titletext + "<br>" + bundle.getString(RELEASES_NOTES));
      Log.i("mbp", "Released notes: " + bundle.getString(RELEASES_NOTES));
    }

    builder
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(R.string.go_to_store_now, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dismiss();
            openPlayStore();
          }
        })
        .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });
    return builder.create();
  }

  public void openPlayStore() {
    final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
    try {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
      //intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    } catch (android.content.ActivityNotFoundException anfe) {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName));
      //intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    }
  }

  private static final String VERSION = "version_code";
  private static final String RELEASES_NOTES = "release_notes";

  public static AppReleaseNotesDialog newInstance(String newVersion, String releaseNotes) {
    AppReleaseNotesDialog f = new AppReleaseNotesDialog();
    Bundle args = new Bundle();
    args.putString(VERSION, newVersion);
    args.putString(RELEASES_NOTES, releaseNotes);
    f.setArguments(args);
    return f;
  }
}