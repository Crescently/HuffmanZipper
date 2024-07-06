package org.example.compressor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.example.entity.huffman.HuffmanNode;
import org.example.entity.huffman.HuffmanTree;
import org.example.entity.io.BitInputStream;
import org.example.entity.io.BitOutputStream;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HuffmanCompressor {
    /**
     * 缓冲区大小
     */
    public static final int BUFFER_SIZE = 8192;
    /**
     * 频率映射表
     */
    private final Map<Byte, Integer> weightMap;
    private final Kryo kryo;
    /**
     * 哈夫曼编码表
     */
    private Map<Byte, String> huffmanCodes;
    /**
     * 哈夫曼树
     */
    private HuffmanTree huffmanTree;

    public HuffmanCompressor() {
        this.weightMap = new HashMap<>();
        this.huffmanCodes = new HashMap<>();
        this.kryo = new Kryo();
        kryo.register(HuffmanNode.class);
        kryo.register(HuffmanTree.class);
    }

    /**
     * 构建频率表
     *
     * @param inputFilePath 输入文件路径
     * @throws IOException 如果发生 I/O 错误
     */
    private void buildFrequencyMap(String inputFilePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFilePath)) {
            int b;
            while ((b = fis.read()) != -1) {
                byte byteValue = (byte) b;
                weightMap.put(byteValue, weightMap.getOrDefault(byteValue, 0) + 1);
            }
        }
    }


    /**
     * 压缩文件
     *
     * @param inputFilePath   输入文件路径
     * @param targetDirectory 目标目录
     * @throws IOException 如果发生 I/O 错误
     */
    public void compressFile(String inputFilePath, String targetDirectory) throws IOException {
        buildFrequencyMap(inputFilePath);
        this.huffmanTree = new HuffmanTree(weightMap);
        this.huffmanCodes = huffmanTree.getHuffmanCodes();

        String compressedFilePath = targetDirectory + File.separator + new File(inputFilePath).getName() + ".hzip";
        try (FileOutputStream fos = new FileOutputStream(compressedFilePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);
             Output output = new Output(bos);
             FileInputStream fis = new FileInputStream(inputFilePath);
             BitOutputStream bitOut = new BitOutputStream(bos, BUFFER_SIZE)) {

            // 哈夫曼树序列化并写入
            kryo.writeClassAndObject(output, huffmanTree.getRoot());
            output.flush();

            // 写入编码后的文件内容
            int b;
            while ((b = fis.read()) != -1) {
                String code = huffmanCodes.get((byte) b);
                for (char bit : code.toCharArray()) {
                    bitOut.writeBit(bit == '1');
                }
            }
        }
    }

    /**
     * 解压文件
     *
     * @param compressedFilePath 压缩文件路径
     * @param targetDirectory    目标目录
     * @throws IOException 如果发生 I/O 错误
     */
    public void decompressFile(String compressedFilePath, String targetDirectory) throws IOException {
        String outputFilePath = compressedFilePath;
        // 判断是否为待解压文件
        if (outputFilePath.endsWith(".hzip")) {
            outputFilePath = outputFilePath.substring(0, outputFilePath.length() - 5);
        }
        outputFilePath = targetDirectory + File.separator + new File(outputFilePath).getName();
        File tempFile = File.createTempFile("decompressed", null);
        tempFile.deleteOnExit();

        try (FileInputStream fis = new FileInputStream(compressedFilePath);
             BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE);
             Input input = new Input(bis);
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {


            // 反序列化并重建哈夫曼树
            HuffmanNode root = (HuffmanNode) kryo.readClassAndObject(input);
            this.huffmanTree = new HuffmanTree(root);


            BitInputStream bitIn = new BitInputStream(input, BUFFER_SIZE);
            HuffmanNode current = root;
            while (true) {
                int bit = bitIn.readBit();
                if (bit == -1) {
                    break;
                }
                if (bit == 0) {
                    current = current.getLeft();
                } else {
                    current = current.getRight();
                }
                if (current.getLeft() == null && current.getRight() == null) {
                    fos.write(current.getData());
                    current = root;
                }
            }
            bitIn.close();
        }
    }
}
