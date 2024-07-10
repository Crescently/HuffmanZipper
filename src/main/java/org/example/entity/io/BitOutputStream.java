package org.example.entity.io;

import org.example.constant.Constants;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 自定义文件输出流
 */
public class BitOutputStream extends OutputStream implements Closeable {

    private final BufferedOutputStream out;
    /**
     * 当前字节
     */
    private int currentByte;
    /**
     * 当前字节已填充的位数
     */
    private int numBitsFilled;

    public BitOutputStream(OutputStream out, int bufferSize) {
        this.out = new BufferedOutputStream(out, bufferSize);
        this.currentByte = 0;
        this.numBitsFilled = 0;
    }

    /**
     * 按位写入
     *
     * @param bit 要写入的位（true 表示 1，false 表示 0）
     * @throws IOException 如果发生 I/O 错误
     */
    public void writeBit(boolean bit) throws IOException {
        currentByte = (currentByte << 1) | (bit ? 1 : 0);
        numBitsFilled++;
        if (numBitsFilled == Constants.BYTE_SIZE) {
            out.write(currentByte);
            numBitsFilled = 0;
            currentByte = 0;
        }
    }

    @Override
    public void write(int b){

    }

    /**
     * 关闭输出流，确保所有未写入的位被写入
     *
     * @throws IOException 如果发生 I/O 错误
     */
    public void close() throws IOException {
        if (numBitsFilled > 0) {
            currentByte <<= (Constants.BYTE_SIZE - numBitsFilled);
            out.write(currentByte);
        }
        out.close();
    }
}



