package com.tencent.sonic.sdk.quic;

import org.chromium.net.UploadDataProvider;
import org.chromium.net.UrlRequest;

import java.util.concurrent.Executor;

/**
 * @author eddyliu
 * @date 2018/5/31
 */
public class CronetBuilder extends UrlRequest.Builder{
    @Override
    public UrlRequest.Builder setHttpMethod(String s) {
        return null;
    }

    @Override
    public UrlRequest.Builder addHeader(String s, String s1) {
        return null;
    }

    @Override
    public UrlRequest.Builder disableCache() {
        return null;
    }

    @Override
    public UrlRequest.Builder setPriority(int i) {
        return null;
    }

    @Override
    public UrlRequest.Builder setUploadDataProvider(UploadDataProvider uploadDataProvider, Executor executor) {
        return null;
    }

    @Override
    public UrlRequest.Builder allowDirectExecutor() {
        return null;
    }

    @Override
    public UrlRequest build() {
        return null;
    }
}
