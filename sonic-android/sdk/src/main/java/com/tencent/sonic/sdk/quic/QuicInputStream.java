package com.tencent.sonic.sdk.quic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author eddyliu
 * @date 2018/6/2
 */
public class QuicInputStream extends InputStream{

    LinkedBlockingQueue<ByteBuffer> blockingQueue;

    public QuicInputStream(LinkedBlockingQueue<ByteBuffer> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    @Override
    public int read() throws IOException {

        return 0;
    }
}
