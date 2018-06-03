package com.tencent.sonic.sdk.quic;

import android.util.Log;

import com.tencent.sonic.sdk.SonicUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author eddyliu
 * @date 2018/6/2
 */
public class QuicInputStream extends InputStream{

    private static String TAG = "QuicInputStream";

    private LinkedBlockingQueue<ByteBuffer> blockingQueue;

    private int bufferIndex = 0;

    private byte[] currentBufferArray = null;


    public QuicInputStream(LinkedBlockingQueue<ByteBuffer> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public int read() throws IOException {
        int c = 0;
        if(currentBufferArray != null){
            c = readSingleByte();
        } else {
            try {
                currentBufferArray = blockingQueue.take().array();
                SonicUtils.log(TAG , Log.INFO , "take next array , currentBufferArray's length is " + currentBufferArray.length);
                c = readSingleByte();
            } catch (InterruptedException e) {
                SonicUtils.log(TAG , Log.ERROR , e.getMessage());
                e.printStackTrace();
            }
        }
        return c;
    }

    private synchronized int readSingleByte(){
        int c = 0;
        if(bufferIndex >= currentBufferArray.length){
            bufferIndex = 0;
            currentBufferArray = null;
            return c;
        }
        c = currentBufferArray[bufferIndex];
        if(c == -1){
            currentBufferArray = null;
            bufferIndex = 0;
        } else {
            bufferIndex++ ;
        }
        SonicUtils.log(TAG , Log.INFO , "reading the buffer , bufferIndex is " + bufferIndex);
        return c;
    }

    @Override
    public void close() throws IOException {
        super.close();
        SonicUtils.log(TAG , Log.INFO , "Stream is closed");
    }
}
