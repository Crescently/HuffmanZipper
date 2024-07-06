package org.example.compressor;

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
    private static final int BUFFER_SIZE = 8192;
    /**
     * 频率映射表
     */
    private Map<Byte, Integer> weightMap;
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
        try (FileOutputStream fos = new FileOutputStream(compressedFilePath); BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE); ObjectOutputStream oos = new ObjectOutputStream(bos); FileInputStream fis = new FileInputStream(inputFilePath); BitOutputStream bitOut = new BitOutputStream(bos, BUFFER_SIZE)) {

            // 写入频率表大小和条目
            oos.writeInt(weightMap.size());
            for (Map.Entry<Byte, Integer> entry : weightMap.entrySet()) {
                oos.writeByte(entry.getKey());
                oos.writeInt(entry.getValue());
            }

            // 序列化并写入哈夫曼树
            oos.writeObject(huffmanTree.getRoot());
            oos.flush();

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
     * @throws IOException            如果发生 I/O 错误
     * @throws ClassNotFoundException 如果类未找到
     */
    public void decompressFile(String compressedFilePath, String targetDirectory) throws IOException, ClassNotFoundException {
        String outputFilePath = compressedFilePath;
        // 判断是否为待解压文件
        if (outputFilePath.endsWith(".hzip")) {
            outputFilePath = outputFilePath.substring(0, outputFilePath.length() - 5);
        }
        outputFilePath = targetDirectory + File.separator + new File(outputFilePath).getName();

        try (FileInputStream fis = new FileInputStream(compressedFilePath); BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE); ObjectInputStream ois = new ObjectInputStream(bis); FileOutputStream fos = new FileOutputStream(outputFilePath)) {

            // 读取频率表大小和条目
            int mapSize = ois.readInt();
            weightMap = new HashMap<>();
            for (int i = 0; i < mapSize; i++) {
                byte key = ois.readByte();
                int value = ois.readInt();
                weightMap.put(key, value);
            }

            // 反序列化并重建哈夫曼树
            HuffmanNode root = (HuffmanNode) ois.readObject();
            this.huffmanTree = new HuffmanTree(root);

            // 使用哈夫曼树解码文件内容
            BitInputStream bitIn = new BitInputStream(bis, BUFFER_SIZE);
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
