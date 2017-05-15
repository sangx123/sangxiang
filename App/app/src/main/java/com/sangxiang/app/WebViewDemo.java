package com.sangxiang.app;

/**
 * Created by sangxiang on 20/4/17.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import static android.webkit.WebSettings.LOAD_NO_CACHE;

/**
 * Demonstrates how to embed a WebView in your activity. Also demonstrates how
 * to have javascript in the WebView call into the activity, and how the activity
 * can invoke javascript.
 * <p>
 * In this example, clicking on the android in the WebView will result in a call into
 * the activities code in {@link DemoJavaScriptInterface#clickOnAndroid()}. This code
 * will turn around and invoke javascript using the {@link WebView#loadUrl(String)}
 * method.
 * <p>
 * Obviously all of this could have been accomplished without calling into the activity
 * and then back into javascript, but this code is intended to show how to set up the
 * code paths for this sort of communication.
 *
 */
public class WebViewDemo extends LogActivity {
    private static final String LOG_TAG = "WebViewDemo";
    private WebView mWebView;
    private Handler mHandler = new Handler();
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(LOAD_NO_CACHE);
        mWebView.addJavascriptInterface(new JsBridge(), "bridge");
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.loadUrl("http://192.168.0.167:10008/gp/cart");
        //mWebView.loadUrl("file:///android_asset/demo.html");
    }

    final class JsBridge{
        JsBridge(){

        }

        /**
         * This is not called on the UI thread. Post a runnable to invoke
         * loadUrl on the UI thread.
         */
        @JavascriptInterface
        public void customerService(){
                       Log.e(TAG, "customerService: ");
        }
        @JavascriptInterface
        public void backToMallHome(){

            Log.e(TAG, "backToMallHome: ");
        }

        @JavascriptInterface
        public void productShare(String data) {
            Log.e(TAG, "productShare: ");
        }
    }
    final class DemoJavaScriptInterface {
        DemoJavaScriptInterface() {
        }
        /**
         * This is not called on the UI thread. Post a runnable to invoke
         * loadUrl on the UI thread.
         */
        @JavascriptInterface
        public void showMobile(){
            Log.e(TAG, "showMobile: ");
        }
        @JavascriptInterface
        public void showName(){
            Log.e(TAG, "showName: ");
        }

        @JavascriptInterface
        public void clickOnAndroid() {
            mHandler.post(new Runnable() {
                public void run() {
                    mWebView.loadUrl("javascript:wave()");
                }
            });
        }
    }
    @JavascriptInterface
    public void showMobile(){
        Log.e(TAG, "showMobile: ");
    }
    @JavascriptInterface
    public void showName(){
        Log.e(TAG, "showName: ");
    }

    @JavascriptInterface
    public void clickOnAndroid() {
        mHandler.post(new Runnable() {
            public void run() {
                mWebView.loadUrl("javascript:wave()");
            }
        });
    }
    /**
     * Provides a hook for calling "alert" from javascript. Useful for
     * debugging your javascript.
     */
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.d(LOG_TAG, message);
            result.confirm();
            return true;
        }
    }
}
