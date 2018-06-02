package com.tencent.sonic.sdk.quic;

import android.content.Context;

import org.chromium.base.ContextUtils;
import org.chromium.net.CronetEngine;
import org.chromium.net.UploadDataProvider;
import org.chromium.net.UrlRequest;
import org.chromium.net.impl.ImplVersion;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author eddyliu
 * @date 2018/6/1
 */
public class CronetEngineUtils {
    private static final String TAG = "CronetUtils";
    private static volatile CronetEngineUtils sInstance;

    private static CronetEngine mCronetEngine;

    private CronetEngineUtils() {
    }

    public static CronetEngineUtils getsInstance() {
        if (sInstance == null) {
                sInstance = new CronetEngineUtils();
            }
        return sInstance;
    }

    public synchronized CronetEngineUtils initEngine(Context context) {
        if (mCronetEngine == null) {
            CronetEngine.Builder builder = new CronetEngine.Builder(context);
            builder.enableHttp2(true).enableQuic(true);
            mCronetEngine = builder.build();
        }
        return this;
    }

    public void startWithURL(String url, UrlRequest.Callback callback) {
        Executor executor = Executors.newSingleThreadExecutor();
        UrlRequest.Builder builderWrapper = new UrlRequestBuilderWrapper(url , callback , executor);
        builderWrapper.build().start();
    }

    class UrlRequestBuilderWrapper extends UrlRequest.Builder{
        UrlRequest.Builder mBuilder ;
        public UrlRequestBuilderWrapper(String url, UrlRequest.Callback callback , Executor executor){
            mBuilder = mCronetEngine.newUrlRequestBuilder(url, callback , executor);
        }

        @Override
        public UrlRequest.Builder setHttpMethod(String s) {
            if(s != null && !s.isEmpty()){
                mBuilder.setHttpMethod(s);
            } else {
                mBuilder.setHttpMethod("POST");
            }
            return this;
        }

        @Override
        public UrlRequest.Builder addHeader(String s, String s1) {
            mBuilder.addHeader(s ,s1);
            return this;
        }

        @Override
        public UrlRequest.Builder disableCache() {
            mBuilder.disableCache();
            return mBuilder;
        }

        @Override
        public UrlRequest.Builder setPriority(int i) {
            mBuilder.setPriority(i);
            return mBuilder;
        }

        @Override
        public UrlRequest.Builder setUploadDataProvider(UploadDataProvider uploadDataProvider, Executor executor) {
            mBuilder.setUploadDataProvider(uploadDataProvider , executor);
            return mBuilder;
        }

        @Override
        public UrlRequest.Builder allowDirectExecutor() {
            mBuilder.allowDirectExecutor();
            return mBuilder;
        }

        @Override
        public UrlRequest build() {
            return mBuilder.build();
        }
    }
}
