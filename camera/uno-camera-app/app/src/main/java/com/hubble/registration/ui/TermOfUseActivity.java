package com.hubble.registration.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hubble.HubbleApplication;
import com.hubble.framework.service.analytics.AnalyticsInterface;
import com.hubble.framework.service.analytics.EventData;
import com.hubble.registration.EScreenName;
import com.hubble.registration.AnalyticsController;
import com.hubble.registration.PublicDefine;
import com.hubble.ui.DebugFragment;
import com.hubbleconnected.camera.R;

import base.hubble.Api;
import base.hubble.Models;
import retrofit.RetrofitError;

public class TermOfUseActivity extends ActionBarActivity {

  private static final String TAG = "TermOfUseActivity";

  ProgressBar loader;
  private EventData eventData;
  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.bb_is_term_of_use_screen);
    setupToolbar();
    eventData = new EventData();

    Intent intent = getIntent();
    setResult(RESULT_CANCELED, intent);

    final WebView terms = (WebView) findViewById(R.id.webView1);
    terms.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int progress) {
        if (progress < 100 && loader.getVisibility() == View.GONE) {
          loader.setVisibility(View.VISIBLE);
        }
        if (progress == 100) {
          loader.setVisibility(View.GONE);
        }
      }
    });

    loader = (ProgressBar) findViewById(R.id.term_of_service_loading_bar);

    if (HubbleApplication.AppConfig.getBoolean(DebugFragment.PREFS_ENABLED_TOS, false)) {
      terms.getSettings().setJavaScriptEnabled(true);
      terms.setWebViewClient(new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
          // waiting until url query has choice_option parameter. In other word, waiting until user taps agree.
          // 1 <= choice_option <= 99 and choice_option accepts comma
          Uri uri = Uri.parse(url);
          String choiceOption = uri.getQueryParameter("choice_option");

          boolean result;
          if (!TextUtils.isEmpty(choiceOption) && TextUtils.isDigitsOnly(choiceOption)) {
            result = true;
            final Float option = Float.parseFloat(choiceOption);
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                getIntent().putExtra(PublicDefine.PREFS_POLICY_CHOICE_OPTION, option);
                setResult(Activity.RESULT_OK, getIntent());
                finish();
              }
            });
          } else {
            result = super.shouldOverrideUrlLoading(view, url);
          }
          return result;
        }
      });

      String policyPath = null;
      Bundle bundle = intent.getExtras();
      if (bundle != null && bundle.containsKey("policy_path")) {
        policyPath = bundle.getString("policy_path");
      }

      if (TextUtils.isEmpty(policyPath)) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            Log.d(TAG, "Get latest policies ....");
            Models.ApiResponse<Models.Policies> res = null;
            try {
              res = Api.getInstance().getService().getLatestPolicies();
            } catch (RetrofitError error) {
              Log.d(TAG, "RetrofitError while get latest version of policy");
            }
            Log.d(TAG, "Get latest policies .... DONE");
            String errorMessage = null;
            if (res != null && res.getData() != null) {
              final Models.Policies latestPolicies = res.getData();
              if (latestPolicies.isActive() && !TextUtils.isEmpty(latestPolicies.getPath())) {
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    terms.loadUrl(latestPolicies.getPath());
                  }
                });
              } else {
                errorMessage = getString(R.string.failed_to_get_tos_url);
              }
            } else {
              errorMessage = getString(R.string.failed_to_get_tos_url);
            }
            if (!TextUtils.isEmpty(errorMessage)) {
              final String temp = errorMessage;
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Toast.makeText(TermOfUseActivity.this, temp, Toast.LENGTH_SHORT).show();
                  loader.setVisibility(View.GONE);
                }
              });
            }
          }
        }).start();
      } else {
        terms.loadUrl(policyPath);
      }
    } else { // not enable tos
      if (terms != null) {
        terms.loadUrl(getString(R.string.term_of_use_url));
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    //AA-1480
    AnalyticsController.getInstance().trackScreen(EScreenName.About);
    AnalyticsInterface.getInstance().trackEvent("ToS","Terms_of_use",eventData);
  }

  private void setupToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.myTermsOfUseToolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(getString(R.string.terms_of_service));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
    }
    return true;
  }

}
