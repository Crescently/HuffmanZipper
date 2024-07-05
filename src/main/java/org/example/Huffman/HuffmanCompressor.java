package org.example.Huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HuffmanCompressor {
    private Map<Byte, Integer> frequencyMap;
    private Map<Byte, String> huffmanCodes;
    private HuffmanTree huffmanTree;

    public HuffmanCompressor() {
        this.frequencyMap = new HashMap<>();
        this.huffmanCodes = new HashMap<>();
    }

    public void compressFile(String inputFilePath, String outputFilePath) throws IOException {
        buildFrequencyMap(inputFilePath);
        this.huffmanTree = new HuffmanTree(frequencyMap);
        this.huffmanCodes = huffmanTree.getHuffmanCodes();

        // todo 在压缩时，将该文件对应的哈夫曼树和文件类型存储到文件中

        try (FileInputStream fis = new FileInputStream(inputFilePath); BitOutputStream bos = new BitOutputStream(inputFilePath, outputFilePath)) {

            bos.writeInt(frequencyMap.size());
            for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
                bos.writeByte(entry.getKey());
                bos.writeInt(entry.getValue());
            }

            int b;
            while ((b = fis.read()) != -1) {
                String code = huffmanCodes.get((byte) b);
                for (char bit : code.toCharArray()) {
                    bos.writeBit(bit == '1');
                }
            }
        }
    }

    private void buildFrequencyMap(String inputFilePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFilePath)) {
            int b;
            while ((b = fis.read()) != -1) {
                byte byteValue = (byte) b;
                frequencyMap.put(byteValue, frequencyMap.getOrDefault(byteValue, 0) + 1);
            }
        }
    }

    public void decompressFile(String compressedFilePath, String targetDirectory) throws IOException {
        // todo 在解压时，读取文件中的哈夫曼树和文件类型
        String outputFilePath = compressedFilePath;
        if (outputFilePath.endsWith(".hzip")) {
            outputFilePath = outputFilePath.substring(0, outputFilePath.length() - 5);
        }
        outputFilePath = targetDirectory + File.separator + new File(outputFilePath).getName();
        try (BitInputStream bis = new BitInputStream(new FileInputStream(compressedFilePath));
             FileOutputStream fos = new FileOutputStream(outputFilePath)) {

            int mapSize = bis.readInt();
            frequencyMap = new HashMap<>();
            for (int i = 0; i < mapSize; i++) {
                byte key = bis.readByte();
                int value = bis.readInt();
                frequencyMap.put(key, value);
            }

            this.huffmanTree = new HuffmanTree(frequencyMap);
            HuffmanNode root = huffmanTree.getRoot();

            HuffmanNode current = root;
            while (true) {
                int bit = bis.readBit();
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
        }
    }

}
