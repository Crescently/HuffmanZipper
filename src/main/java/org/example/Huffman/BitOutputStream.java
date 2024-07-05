package org.example.Huffman;

import java.io.*;

public class BitOutputStream implements Closeable, Flushable {
    private OutputStream out;
    private int currentByte;
    private int numBitsInCurrentByte;

    // todo 优化写入缓冲区，加快文件写入速度
    public BitOutputStream(String sourceFilePath, String targetDirectory) throws IOException {
        File sourceFile = new File(sourceFilePath);
        String outputFilePath = targetDirectory + File.separator + sourceFile.getName() + ".hzip";
        this.out = new FileOutputStream(outputFilePath);
        this.currentByte = 0;
        this.numBitsInCurrentByte = 0;
    }

    public void writeBit(boolean bit) throws IOException {
        if (bit) {
            currentByte = currentByte | (1 << (7 - numBitsInCurrentByte));
        }
        numBitsInCurrentByte++;
        if (numBitsInCurrentByte == 8) {
            flushCurrentByte();
        }
    }

    public void writeByte(byte b) throws IOException {
        out.write(b);
    }

    public void writeInt(int value) throws IOException {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    @Override
    public void flush() throws IOException {
        flushCurrentByte();
        out.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }

    private void flushCurrentByte() throws IOException {
        if (numBitsInCurrentByte > 0) {
            out.write(currentByte);
            currentByte = 0;
            numBitsInCurrentByte = 0;
        }
    }
}
