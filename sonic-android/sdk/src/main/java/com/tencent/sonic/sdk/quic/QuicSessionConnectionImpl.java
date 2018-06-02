package com.tencent.sonic.sdk.quic;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.tencent.sonic.sdk.SonicConstants;
import com.tencent.sonic.sdk.SonicEngine;
import com.tencent.sonic.sdk.SonicSession;
import com.tencent.sonic.sdk.SonicSessionConnection;
import com.tencent.sonic.sdk.SonicUtils;

import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.chromium.net.impl.UrlRequestError;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static com.tencent.sonic.sdk.SonicConstants.ERROR_CODE_SUCCESS;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * @author eddyliu
 * @date 2018/5/31
 */
public class QuicSessionConnectionImpl extends SonicSessionConnection {

    private static final String TAG = "QuicSessionConnectionImpl";

    private BufferedInputStream quicInputStream;
    private LinkedBlockingQueue<List<ByteBuffer>> quicBlockQueue;
    private List<ByteBuffer> bufferList ;
    private CronetEngineUtils engineUtils;
    private UrlRequestCallback requestCallback = new UrlRequestCallback();
    private Map<String , List<String>> quicResponseHeaderFields;
    private int responseCode = HTTP_OK;
    /**
     * Constructor
     * @param session The SonicSession instance
     * @param intent  The intent
     */
    public QuicSessionConnectionImpl(SonicSession session, Intent intent) {
        super(session, intent);
        engineUtils = CronetEngineUtils.getsInstance().initEngine(SonicEngine.getInstance().getRuntime().getContext());
        quicBlockQueue = new LinkedBlockingQueue(1);
        bufferList = new ArrayList<>();
    }

    @Override
    public void disconnect() {
        SonicUtils.log(TAG , Log.INFO , "the connect is closed");
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public Map<String, List<String>> getResponseHeaderFields() {
        return quicResponseHeaderFields;
    }

    @Override
    public String getResponseHeaderField(String key) {
        if (null != quicResponseHeaderFields && 0 != quicResponseHeaderFields.size()) {
            List<String> responseHeaderValues = quicResponseHeaderFields.get(key.toLowerCase());
            if (null != responseHeaderValues && 0 != responseHeaderValues.size()) {
                StringBuilder stringBuilder = new StringBuilder(responseHeaderValues.get(0));
                for (int index = 1, size = responseHeaderValues.size(); index < size; ++index) {
                    stringBuilder.append(',');
                    stringBuilder.append(responseHeaderValues.get(index));
                }
                return stringBuilder.toString();
            }
        }
        return null;
    }

    @Override
    protected int internalConnect() {
        SonicUtils.log(TAG , Log.INFO , "the connect is began");
        if(quicInputStream == null){
            quicInputStream = new BufferedInputStream(new ByteArrayInputStream(new byte[32]));
        }
        engineUtils.startWithURL(session.srcUrl , requestCallback);
        return responseCode = HTTP_OK;
    }

    @Override
    protected BufferedInputStream internalGetResponseStream() {
        SonicUtils.log(TAG , Log.INFO , "getQuicResponseStream is began");
        if(quicBlockQueue.size() > 0){
            SonicUtils.log(TAG , Log.INFO , "readBuffer is began");
            readBuffer();
            return quicInputStream;
        } else {
            try {
                quicBlockQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SonicUtils.log(TAG , Log.INFO , "return the stream directly");
            return quicInputStream;
        }
    }

    class UrlRequestCallback extends UrlRequest.Callback {

        @Override
        public void onRedirectReceived(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, String s) throws Exception {
            SonicUtils.log(TAG , Log.INFO , "request redirected");
        }

        @Override
        public void onResponseStarted(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo) throws Exception {
            quicResponseHeaderFields = urlResponseInfo.getAllHeaders();
            urlRequest.read(ByteBuffer.allocateDirect(32 * 1024));
            SonicUtils.log(TAG , Log.INFO , "the response is STARTED");
        }

        @Override
        public void onReadCompleted(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, ByteBuffer byteBuffer) throws Exception {
            SonicUtils.log(TAG , Log.INFO , "the response is transporting the data , size is " + byteBuffer.arrayOffset());
            bufferList.add(byteBuffer);
            byteBuffer.clear();
            urlRequest.read(byteBuffer);
        }

        @Override
        public void onSucceeded(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo) {
            SonicUtils.log(TAG , Log.INFO , "the response transmission is succeed");

//            readBuffer();
        }

        @Override
        public void onFailed(UrlRequest urlRequest, UrlResponseInfo urlResponseInfo, CronetException e) {
            SonicUtils.log(TAG , Log.INFO , "the response transmission is fail, errorMsg ：" + e.getMessage());
            setResponseCode(urlResponseInfo.getHttpStatusCode());
        }
    }

    private void setResponseCode(int quicStatusCode){
        if(quicStatusCode == UrlRequestError.CONNECTION_TIMED_OUT){
            responseCode = SonicConstants.ERROR_CODE_CONNECT_TOE;
        } else if(quicStatusCode == UrlRequestError.INTERNET_DISCONNECTED){
            responseCode = SonicConstants.ERROR_CODE_CONNECT_NNE;
        } else if(quicStatusCode == UrlRequestError.CONNECTION_CLOSED
                || quicStatusCode == UrlRequestError.CONNECTION_REFUSED){
            responseCode = SonicConstants.ERROR_CODE_CONNECT_IOE;
        } else {
            responseCode = SonicConstants.ERROR_CODE_UNKNOWN;
        }
    }

    private void readBuffer(){
        for (List<ByteBuffer> bufferList: quicBlockQueue) {
            try {
                for(ByteBuffer buffer : bufferList){
                    quicInputStream.read(buffer.array(), 0 , buffer.array().length);
                }
            } catch (IOException e) {
                setResponseCode(SonicConstants.ERROR_CODE_CONNECT_IOE);
                SonicUtils.log(TAG , Log.ERROR , e.getMessage());
            }
        }
        quicBlockQueue.clear();
    }
}
