package com.aircraftcarrier.framework.excel;

import java.io.IOException;
import java.io.OutputStream;

// 自定义计数输出流
public abstract class CountingOutputStream extends OutputStream {
    private final OutputStream out;
    private long count;

    public CountingOutputStream(OutputStream out) {
        this.out = out;
    }

    public long getCount() {
        return count;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        count++;
        afterWrite(1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        count += b.length;
        afterWrite(b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        count += len;
        afterWrite(len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    protected abstract void afterWrite(int n) throws IOException;
}