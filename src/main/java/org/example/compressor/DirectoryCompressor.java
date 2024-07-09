package org.example.compressor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.file.DirectoryStructure;
import org.example.entity.file.DirectoryZipInfo;
import org.example.entity.file.FileNode;
import org.example.entity.file.FileNodeSerializer;
import org.example.entity.huffman.HuffmanNode;
import org.example.entity.huffman.HuffmanTree;
import org.example.entity.io.BitInputStream;
import org.example.entity.io.BitOutputStream;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DirectoryCompressor {
    public static final int BUFFER_SIZE = 8192 * 5;
    private final Map<Byte, Integer> weightMap;
    private final DirectoryZipInfo directoryZipInfo;
    private final Kryo kryo;
    private Map<Byte, String> huffmanCodes;
    private HuffmanTree huffmanTree;

    public DirectoryCompressor() {
        this.directoryZipInfo = new DirectoryZipInfo();
        this.weightMap = new HashMap<>();
        this.huffmanCodes = new HashMap<>();
        this.kryo = new Kryo();
        kryo.setReferences(true);
        kryo.register(HuffmanNode.class);
        kryo.register(FileNode.class, new FileNodeSerializer());
        kryo.register(DirectoryZipInfo.class);
    }

    private void buildFrequencyMapFromDirectory(FileNode node, String basePath) throws IOException {
        String currentPath = basePath + File.separator + node.getName();
        if (node.isFile()) {
            try (FileInputStream fis = new FileInputStream(currentPath)) {
                int b;
                while ((b = fis.read()) != -1) {
                    byte byteValue = (byte) b;
                    weightMap.put(byteValue, weightMap.getOrDefault(byteValue, 0) + 1);
                }
            }
        } else {
            // 如果是目录
            // 递归遍历子节点
            for (FileNode child : node.getChildren()) {
                buildFrequencyMapFromDirectory(child, currentPath);
            }
        }
    }

    /**
     * 记录每个文件的大小
     *
     * @param node     文件节点
     * @param basePath 当前路径
     */
    private void recordFileSizes(FileNode node, String basePath) {
        // 获取当前文件路径
        String currentPath = basePath + File.separator + node.getName();
        if (node.isFile()) {
            // 如果是文件
            File file = new File(currentPath);
            // 设置文件大小
            node.setFileSize(file.length());
        } else {
            for (FileNode child : node.getChildren()) {
                recordFileSizes(child, currentPath);
            }
        }
    }

    /**
     * 压缩文件夹
     *
     * @param inputDirectoryPath 输入文件夹路径
     * @param outputFilePath     输出文件路径
     * @throws IOException IOException
     */
    public void compressDirectory(String inputDirectoryPath, String outputFilePath) throws IOException {

        File outputFile = new File(outputFilePath);

        // 检查并创建父目录
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        // 构建目录树
        DirectoryStructure ds = new DirectoryStructure();
        FileNode rootNode = ds.buildTree(new File(inputDirectoryPath));

        File file = new File(inputDirectoryPath);
        String parentPath = file.getParent();

        // 记录文件夹结构
        buildFrequencyMapFromDirectory(rootNode, new File(parentPath).getAbsolutePath());
        // 记录每个文件的大小
        recordFileSizes(rootNode, new File(parentPath).getAbsolutePath());

        // 构建哈夫曼树
        this.huffmanTree = new HuffmanTree(weightMap);

        // 获取哈夫曼编码
        this.huffmanCodes = huffmanTree.getHuffmanCodes();

        String compressedFilePath = outputFilePath + File.separator + new File(inputDirectoryPath).getName() + ".hzip";

        try (FileOutputStream fos = new FileOutputStream(compressedFilePath); BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);
//             ObjectOutputStream oos = new ObjectOutputStream(bos)
             Output output = new Output(bos)) {

            // 序列化并写入哈夫曼树和目录结构
            directoryZipInfo.setRoot(huffmanTree.getRoot());
            directoryZipInfo.setRootNode(rootNode);
            kryo.writeClassAndObject(output, directoryZipInfo);

            output.flush();

            // 写入文件内容
            try (BitOutputStream bitOut = new BitOutputStream(bos, BUFFER_SIZE)) {
                compressDirectoryContent(rootNode, new File(parentPath).getAbsolutePath(), bitOut);
                bitOut.flush();
                log.info("Compressed directory completed");
            }
        }
    }

    /**
     * 递归压缩目录内容
     *
     * @param node     文件节点
     * @param basePath 当前路径
     * @param bitOut   BitOutputStream
     * @throws IOException IOException
     */
    private void compressDirectoryContent(FileNode node, String basePath, BitOutputStream bitOut) throws IOException {
        String currentPath = basePath + File.separator + node.getName();
        if (node.isFile()) {
            try (FileInputStream fis = new FileInputStream(currentPath)) {
                int b;
                while ((b = fis.read()) != -1) {
                    String code = huffmanCodes.get((byte) b);
                    for (char bit : code.toCharArray()) {
                        // 写入编码
                        bitOut.writeBit(bit == '1');
                    }
                }
            }
        } else {
            for (FileNode child : node.getChildren()) {
                compressDirectoryContent(child, currentPath, bitOut);
            }
        }
    }

    /**
     * 解压目录
     *
     * @param compressedFilePath 压缩文件路径
     * @param targetDirectory    目标目录
     * @throws IOException IOException
     */
    public void decompressDirectory(String compressedFilePath, String targetDirectory) throws IOException {
        // 判断文件后缀是否合法
        if (!compressedFilePath.endsWith(".hzip")) {
            throw new IllegalArgumentException("The file is not a compressed file.");
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(compressedFilePath), BUFFER_SIZE);
//             ObjectInputStream ois = new ObjectInputStream(bis)
             Input input = new Input(bis)) {

            // 读取并反序列化Huffman树和目录结构
            DirectoryZipInfo directoryZipInfo = (DirectoryZipInfo) kryo.readClassAndObject(input);
            HuffmanNode root = directoryZipInfo.getRoot();
            FileNode rootNode = directoryZipInfo.getRootNode();
            this.huffmanTree = new HuffmanTree(root);


            // 解压文件内容
            BitInputStream bitIn = new BitInputStream(input, BUFFER_SIZE);
            Map<String, FileOutputStream> outputStreams = new HashMap<>();
            decompressDirectoryContent(rootNode, targetDirectory, bitIn, outputStreams);

            for (FileOutputStream fos : outputStreams.values()) {
                fos.close();
            }

            log.info("deCompressed directory completed");
            bitIn.close();
        }
    }

    /**
     * 递归解压目录内容
     *
     * @param node          文件节点
     * @param outputPath    目标路径
     * @param bitIn         BitInputStream
     * @param outputStreams 输出流
     * @throws IOException IOException
     */
    private void decompressDirectoryContent(FileNode node, String outputPath, BitInputStream bitIn, Map<String, FileOutputStream> outputStreams) throws IOException {
        File outputFile = new File(outputPath, node.getName());
        if (node.isFile()) {
            FileOutputStream fos = new FileOutputStream(outputFile);
            outputStreams.put(outputFile.getAbsolutePath(), fos);
            try {
                HuffmanNode current = huffmanTree.getRoot();
                // 获取到文件大小
                long remainingBytes = node.getFileSize();
                while (remainingBytes > 0) {
                    int bit = bitIn.readBit();
                    if (bit == -1) {
                        // 读取到文件末尾
                        break;
                    }
                    if (bit == 0) {
                        current = current.getLeft();
                    } else {
                        current = current.getRight();
                    }
                    if (current.getLeft() == null && current.getRight() == null) {
                        fos.write(current.getData());
                        // 减少剩余字节数
                        remainingBytes--;
                        current = huffmanTree.getRoot();
                    }
                }
            } catch (EOFException e) {
                System.out.println(e.getMessage());
            }
        } else {
            if (!outputFile.exists()) {
                outputFile.mkdirs();
            }
            for (FileNode child : node.getChildren()) {
                decompressDirectoryContent(child, outputFile.getAbsolutePath(), bitIn, outputStreams);
            }
        }
    }

}
