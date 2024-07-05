package org.example.Huffman;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements Closeable {
    private InputStream in;
    private int currentByte;
    private int numBitsRemaining;

    public BitInputStream(InputStream in) {
        this.in = in;
        this.currentByte = 0;
        this.numBitsRemaining = 0;
    }

    // todo 优化读入缓冲区，加快文件读入速度
    public int readBit() throws IOException {
        if (numBitsRemaining == 0) {
            currentByte = in.read();
            if (currentByte == -1) {
                return -1;
            }
            numBitsRemaining = 8;
        }
        numBitsRemaining--;
        return (currentByte >> numBitsRemaining) & 1;
    }

    public byte readByte() throws IOException {
        return (byte) in.read();
    }

    public int readInt() throws IOException {
        return (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | in.read();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
