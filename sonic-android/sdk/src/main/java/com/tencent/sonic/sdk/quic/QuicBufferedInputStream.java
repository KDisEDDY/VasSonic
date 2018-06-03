package com.tencent.sonic.sdk.quic;

import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * @author eddyliu
 * @date 2018/6/3
 */
public class QuicBufferedInputStream extends BufferedInputStream {

    private boolean isFinish = false;

    public QuicBufferedInputStream(@NonNull InputStream in) {
        super(in);
    }


    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean isFinish){
        this.isFinish = isFinish;
    }
}
