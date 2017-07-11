package com.sangxiang.app;

/**
 * Created by sangxiang on 20/4/17.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.ConsoleMessage;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import static android.webkit.WebSettings.LOAD_NO_CACHE;

/**
 * Demonstrates how to embed a WebView in your activity. Also demonstrates how
 * to have javascript in the WebView call into the activity, and how the activity
 * can invoke javascript.
 * <p>
 * In this example, clicking on the android in the WebView will result in a call into
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
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.loadUrl("file:///android_asset/index.html");
            }
        });
        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JsBridge(), "xksJsBridge");
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.loadUrl("file:///android_asset/demo.html");
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
        public void backToMallHome(int aa){

            Log.e(TAG, "backToMallHome: ");
        }

        @JavascriptInterface
        public void productShare(String data) {

            //Log.e(TAG, "title="+title+",url="+url);
        }
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

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.e("webview", consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
            //mWebView.loadUrl(ABOUT_BLANK);
        }
    }
    public class MyWebViewClient extends WebViewClient{
        //控制新的连接在当前WebView中打开
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.e(TAG, "shouldOverrideUrlLoading: " );
            return super.shouldOverrideUrlLoading(view, request);
        }
        //网页开始加载
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.e(TAG, "onPageStarted: " );
            super.onPageStarted(view, url, favicon);
        }
        //网页加载完毕
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.e(TAG, "onPageFinished: " );
            Log.e(TAG, "onPageFinished: "+view.getTitle() );
            super.onPageFinished(view, url);
        }
        //加载指定地址提供的资源
        @Override
        public void onLoadResource(WebView view, String url) {
            Log.e(TAG, "onLoadResource: " );
            super.onLoadResource(view, url);
        }

        //
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.e(TAG, "shouldInterceptRequest: " );
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Log.e(TAG, "shouldInterceptRequest: " );
            return super.shouldInterceptRequest(view, request);
        }
        //报告错误信息
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.e(TAG, "onReceivedError: ");
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            Log.e(TAG, "onReceivedHttpError: ");
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            Log.e(TAG, "onFormResubmission: ");
            super.onFormResubmission(view, dontResend, resend);
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            Log.e(TAG, "doUpdateVisitedHistory: ");
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.e(TAG, "onReceivedSslError: " );
            super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
            Log.e(TAG, "onReceivedClientCertRequest: ");
            super.onReceivedClientCertRequest(view, request);
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            Log.e(TAG, "onReceivedHttpAuthRequest: " );
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            Log.e(TAG, "shouldOverrideKeyEvent: " );
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            Log.e(TAG, "onUnhandledKeyEvent: ");
            super.onUnhandledKeyEvent(view, event);
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            Log.e(TAG, "onScaleChanged: " );
            super.onScaleChanged(view, oldScale, newScale);
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            Log.e(TAG, "onReceivedLoginRequest: " );
            super.onReceivedLoginRequest(view, realm, account, args);
        }
    }
}
