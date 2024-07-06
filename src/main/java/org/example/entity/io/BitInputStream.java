package org.example.entity.io;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements Closeable {
    private static final int BYTE_SIZE = 8;
    private BufferedInputStream in;
    /**
     * 当前字节
     */
    private int currentByte;
    /**
     * 当前字节剩余的未读取的位数
     */
    private int numBitsRemaining;

    public BitInputStream(InputStream in, int bufferSize) {
        this.in = new BufferedInputStream(in, bufferSize);
        this.currentByte = 0;
        this.numBitsRemaining = 0;
    }

    /**
     * 按位读取
     *
     * @return 读取的位（0 或 1），如果到达流的末尾则返回 -1
     * @throws IOException 如果发生 I/O 错误
     */
    public int readBit() throws IOException {
        if (currentByte == -1) {
            return -1;
        }
        if (numBitsRemaining == 0) {
            currentByte = in.read();
            if (currentByte == -1) {
                return -1;
            }
            numBitsRemaining = BYTE_SIZE;
        }
        numBitsRemaining--;
        return (currentByte >> numBitsRemaining) & 1;
    }

    /**
     * 关闭输入流
     *
     * @throws IOException 如果发生 I/O 错误
     */
    public void close() throws IOException {
        in.close();
    }
}





