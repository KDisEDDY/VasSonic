package com.tencent.sonic.demo.quic;

import android.annotation.TargetApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.tencent.sonic.R;
import com.tencent.sonic.demo.SonicJavaScriptInterface;
import com.tencent.sonic.demo.SonicRuntimeImpl;
import com.tencent.sonic.demo.SonicSessionClientImpl;
import com.tencent.sonic.sdk.SonicCacheInterceptor;
import com.tencent.sonic.sdk.SonicConfig;
import com.tencent.sonic.sdk.SonicEngine;
import com.tencent.sonic.sdk.SonicSession;
import com.tencent.sonic.sdk.SonicSessionClient;
import com.tencent.sonic.sdk.SonicSessionConfig;

public class QuicWebViewActivity extends AppCompatActivity {

    private static final String URL = "https://stgwhttp2.kof.qq.com/3.jpg";
    //http://mc.vip.qq.com/demo/indexv3
    //https://stgwhttp2.kof.qq.com/3.jpg
    SonicSession session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(new SonicRuntimeImpl(getApplication()), new SonicConfig.Builder().build());
        }
        SonicSessionConfig config = new SonicSessionConfig.Builder()
                .setSonicConnectionMode(SonicSessionConfig.CONNECTION_MODE_QUICCONNECTION)
                .setSupportLocalServer(true)
                .build();
        session = SonicEngine.getInstance().createSession(URL , config);
        SonicSessionClientImpl sonicSessionClient = null;
        if (null != session) {
            session.bindClient(sonicSessionClient = new SonicSessionClientImpl());
        } else {
            Toast.makeText(this, "create sonic session fail!", Toast.LENGTH_LONG).show();
        }
        setContentView(R.layout.activity_quic_web_view);
        WebView webView = (WebView) findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (session != null) {
                    session.getSessionClient().pageFinish(url);
                }
            }

            @TargetApi(21)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return shouldInterceptRequest(view, request.getUrl().toString());
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (session != null) {
                    return (WebResourceResponse) session.getSessionClient().requestResource(url);
                }
                return null;
            }
        });

        WebSettings webSettings = webView.getSettings();

        // add java script interface
        // note:if api level lower than 17(android 4.2), addJavascriptInterface has security
        // issue, please use x5 or see https://developer.android.com/reference/android/webkit/
        // WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)
        webSettings.setJavaScriptEnabled(true);
        webView.removeJavascriptInterface("searchBoxJavaBridge_");

        // init webview settings
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);


        // webview is ready now, just tell session client to bind
        if ( sonicSessionClient != null) {
            sonicSessionClient.bindWebView(webView);
            sonicSessionClient.clientReady();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != session) {
            session.destroy();
            session = null;
        }
        SonicEngine.getInstance().cleanCache();
    }
}
