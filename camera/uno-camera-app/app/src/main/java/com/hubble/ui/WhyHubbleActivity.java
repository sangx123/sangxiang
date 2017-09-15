package com.hubble.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.hubbleconnected.camera.R;


public class WhyHubbleActivity extends AppCompatActivity
{
	private static final String TAG = WhyHubbleActivity.class.getSimpleName();
	private WebView mWebView;
	private ProgressBar mProgressBar;

	private static final String HUBBLE_URL = "https://hubbleconnected.com/";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hubble_webview);

		mProgressBar = (ProgressBar)findViewById(R.id.loading_pb);
		mProgressBar.setIndeterminate(true);

		mWebView = (WebView) findViewById(R.id.root_web_view);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress)
			{
				if (progress < 100 && mProgressBar.getVisibility() == View.GONE) {
					mProgressBar.setVisibility(View.VISIBLE);
					mProgressBar.setProgress(progress);
				}
				if (progress == 100) {
					mProgressBar.setVisibility(View.GONE);
				}
			}
		});
		mWebView.loadUrl(HUBBLE_URL);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

}
