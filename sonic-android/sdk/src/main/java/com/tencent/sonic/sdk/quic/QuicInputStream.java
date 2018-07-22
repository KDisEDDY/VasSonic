package com.tencent.sonic.sdk.quic;

import android.util.Log;

import com.tencent.sonic.sdk.SonicUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.acl.LastOwnerException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author eddyliu
 * @date 2018/6/2
 */
public class QuicInputStream extends InputStream{

    private static String TAG = "QuicInputStream";

    private LinkedBlockingQueue<Integer> blockingQueue;

    private int account = 0;

    private volatile boolean isFinish = false;

    public QuicInputStream(LinkedBlockingQueue<Integer> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public int read() throws IOException {
        int c = -1;
        try {
            c = blockingQueue.take();
//            SonicUtils.log(TAG , Log.INFO , "byte : " + c);
        } catch (InterruptedException e) {
            SonicUtils.log(TAG , Log.INFO , "read error : " + e.getMessage());
        }
        if(c == -1){
            isFinish = true;
//            SonicUtils.log(TAG , Log.INFO , "the stream is end");
        }
//        SonicUtils.log(TAG , Log.INFO , "had read account : " + account++);
        return c;
    }

    @Override
    public void close() throws IOException {
        super.close();
        SonicUtils.log(TAG , Log.INFO , "Stream is closed");
    }
}
