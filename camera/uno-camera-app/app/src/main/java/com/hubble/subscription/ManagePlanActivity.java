package com.hubble.subscription;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.hubble.HubbleApplication;
import com.hubbleconnected.camera.R;

import base.hubble.PublicDefineGlob;
import base.hubble.meapi.PublicDefines;

/**
 * Created by Pragnya on 08-02-2017.
 */
public class ManagePlanActivity extends Activity {

	private final String TAG = "ManagePlanActivity";

	private String APP_PLAN_URL = "https://apps-payment.hubbleconnected.com/#apppayment/";
	private String APP_PLAN_STAGING_URL = "https://staging-apps-payment.hubbleconnected.com/#apppayment/";
	private String APP_PLAN_TEST = "/payment_test";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		String accessToken = HubbleApplication.AppConfig.getString(PublicDefineGlob.PREFS_SAVED_PORTAL_TOKEN, "");
		String launchURL = APP_PLAN_URL + accessToken + APP_PLAN_TEST;
		if(PublicDefines.SERVER_URL.compareToIgnoreCase(PublicDefines.PRODUCTION_URL)!=0){
			launchURL = APP_PLAN_STAGING_URL + accessToken + APP_PLAN_TEST;
		}
        setContentView(R.layout.activity_manage_plan);
		WebView webview = (WebView) findViewById(R.id.plan_webiview);
		WebSettings webSettings = webview.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webview.loadUrl(launchURL);
		webview.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Log.e(TAG, "Some error occured while loading page "+description);
				Toast.makeText(ManagePlanActivity.this,getString(R.string.unknown_error),Toast.LENGTH_LONG).show();
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				super.onReceivedSslError(view, handler, error);
				Log.e(TAG, "SSL error occured while loading page "+error);
			}

			@Override
			public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
				super.onReceivedHttpError(view, request, errorResponse);
				Log.d(TAG, "onReceivedHttpError ");
			}
		});

		ImageView backButton = (ImageView)findViewById(R.id.plan_back_button);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_OK);
				finish();
			}
		});
	}
}
