package org.example.compressor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.Constants;
import org.example.entity.file.SingleFileZipInfo;
import org.example.entity.huffman.HuffmanNode;
import org.example.entity.huffman.HuffmanTree;
import org.example.entity.io.BitInputStream;
import org.example.entity.io.BitOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SingleFileCompressor {
    /**
     * 频率映射表
     */
    private final Map<Byte, Integer> weightMap;
    /**
     * Kryo 序列化工具
     */
    private final Kryo kryo;
    /**
     * 哈夫曼编码表
     */
    private Map<Byte, String> huffmanCodes;
    /**
     * 哈夫曼树
     */
    private HuffmanTree huffmanTree;


    public SingleFileCompressor() {
        this.weightMap = new HashMap<>();
        this.huffmanCodes = new HashMap<>();
        this.kryo = new Kryo();
        kryo.register(HuffmanNode.class);
        kryo.register(HuffmanTree.class);
        kryo.register(SingleFileZipInfo.class);
    }

    private static Callable<Map<Byte, Integer>> getTask(String inputFilePath, int i, int chunkSize) {
        final int threadIndex = i;
        return () -> {
            Map<Byte, Integer> chunkMap = new HashMap<>();
            try (FileInputStream fisChunk = new FileInputStream(inputFilePath)) {
                fisChunk.skip((long) threadIndex * chunkSize);
                int b;
                int bytesRead = 0;
                while ((b = fisChunk.read()) != -1 && bytesRead < chunkSize) {
                    byte byteValue = (byte) b;
                    chunkMap.put(byteValue, chunkMap.getOrDefault(byteValue, 0) + 1);
                    bytesRead++;
                }
            }
            return chunkMap;
        };
    }


    private void buildFrequencyMapParallel(String inputFilePath) throws IOException {
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        AtomicInteger byteCount = new AtomicInteger(0);

        try (FileInputStream fis = new FileInputStream(inputFilePath)) {
            int fileSize = fis.available();
            int chunkSize = (fileSize + processors - 1) / processors;

            List<Callable<Map<Byte, Integer>>> tasks = new ArrayList<>();
            for (int i = 0; i < processors; i++) {
                Callable<Map<Byte, Integer>> task = getTask(inputFilePath, i, chunkSize);
                tasks.add(task);
            }

            List<Future<Map<Byte, Integer>>> futures = executor.invokeAll(tasks);
            for (Future<Map<Byte, Integer>> future : futures) {
                Map<Byte, Integer> chunkMap = future.get();
                chunkMap.forEach((byteValue, count) -> weightMap.merge(byteValue, count, Integer::sum));
                byteCount.addAndGet(chunkMap.size());
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error during parallel frequency map building: {}", e.getMessage());
        } finally {
            executor.shutdown();
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
        buildFrequencyMapParallel(inputFilePath);
        this.huffmanTree = new HuffmanTree(weightMap);
        this.huffmanCodes = huffmanTree.getHuffmanCodes();
        // 压缩文件
        // 获取原文件完整名
        String fileName = new File(inputFilePath).getName();
        //获取文件后缀名
        String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
        //去掉后缀
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        String compressedFilePath = targetDirectory + File.separator + fileName + ".hzip";
        try (FileOutputStream fos = new FileOutputStream(compressedFilePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos, Constants.BUFFER_SIZE);
             Output output = new Output(bos);
             FileInputStream fis = new FileInputStream(inputFilePath);
             BitOutputStream bitOut = new BitOutputStream(bos, Constants.BUFFER_SIZE)) {

            // 存储文件信息
            SingleFileZipInfo singleFileZipInfo = new SingleFileZipInfo();
            singleFileZipInfo.setFileSuffix(fileSuffix);
            singleFileZipInfo.setRoot(huffmanTree.getRoot());


            output.writeString(Constants.SINGLE_FILE);

            // 写入文件长度
            long originalFileSize = new File(inputFilePath).length();
            output.writeLong(originalFileSize);

            // 文件信息序列化并写入
            kryo.writeClassAndObject(output, singleFileZipInfo);
            output.flush();

            // 写入编码后的文件内容
            int b;
            while ((b = fis.read()) != -1) {
                String code = huffmanCodes.get((byte) b);
                for (char bit : code.toCharArray()) {
                    bitOut.writeBit(bit == '1');
                }
            }
            log.info("Compressed file finish");
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
        } else {
            throw new IllegalArgumentException("The file is not a compressed file.");
        }
        outputFilePath = targetDirectory + File.separator + new File(outputFilePath).getName();

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(compressedFilePath), Constants.BUFFER_SIZE);
             Input input = new Input(bis)) {


            // 获取文件类型
            String fileType = input.readString();
            if (!Constants.SINGLE_FILE.equals(fileType)) {
                throw new IllegalArgumentException("The file is not a single file.");
            }
            // 获取文件长度
            long originalFileSize = input.readLong();
            System.out.println(originalFileSize);

            // 反序列化读取信息
            SingleFileZipInfo singleFileZipInfo = (SingleFileZipInfo) kryo.readClassAndObject(input);
            HuffmanNode root = singleFileZipInfo.getRoot();
            this.huffmanTree = new HuffmanTree(root);

            // 重新拼接文件名
            String fileSuffix = singleFileZipInfo.getFileSuffix();
            outputFilePath = outputFilePath + fileSuffix;

            FileOutputStream fos = new FileOutputStream(outputFilePath);
            BitInputStream bitIn = new BitInputStream(input, Constants.BUFFER_SIZE);
            HuffmanNode current = root;

            // 解压文件
            int bit;
            long bytesRead = 0;
            while (bytesRead < originalFileSize && (bit = bitIn.readBit()) != -1) {
                current = (bit == 0) ? current.getLeft() : current.getRight();
                if (current.getLeft() == null && current.getRight() == null) {
                    fos.write(current.getData());
                    bytesRead++;
                    current = root;
                }
            }
            log.info("Decompressed file finish");
            bitIn.close();
        }
    }
}
