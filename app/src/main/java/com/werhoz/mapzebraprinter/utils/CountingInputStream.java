package com.werhoz.mapzebraprinter.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends FilterInputStream {
    private long bytesRead = 0;
    private final ProgressListener listener;
    private final long totalBytes;

    public interface ProgressListener {
        void onProgress(int percent);
    }

    public CountingInputStream(InputStream in, long totalBytes, ProgressListener listener) {
        super(in);
        this.totalBytes = totalBytes;
        this.listener = listener;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            bytesRead++;
            reportProgress();
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0) {
            bytesRead += n;
            reportProgress();
        }
        return n;
    }

    private void reportProgress() {
        if (listener != null && totalBytes > 0) {
            int percent = (int) (((double) bytesRead / totalBytes) * 100);
            listener.onProgress(percent);
        }
    }
}

