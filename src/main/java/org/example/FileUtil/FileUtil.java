package org.example.FileUtil;

import org.example.FileEntity.DirectoryStructure;
import org.example.FileEntity.FileBasicInfo;
import org.example.Huffman.HuffmanCompressor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作工具类
 */
public class FileUtil {

    /**
     * 获取文件基本信息
     *
     * @param filePath   文件路径
     * @param targetPath 目标生成路径
     */
    public static void getFileInfo(String filePath, String targetPath) {
        FileBasicInfo fileBasicInfo = FileBasicInfo.getInstance();
        fileBasicInfo.setFilePath(filePath);
        fileBasicInfo.setTargetPath(targetPath);
        FileTypeReader(filePath);
    }

    /**
     * 文件类型识别
     *
     * @param filePath 文件路径
     */
    public static void FileTypeReader(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                System.out.println("The path is a file.");
                // 对单文件的哈夫曼压缩
                HuffmanCompressor compressor = new HuffmanCompressor();
                FileBasicInfo fileBasicInfo = FileBasicInfo.getInstance();
                String inputFilePath = fileBasicInfo.getFilePath();
                String outputFilePath = fileBasicInfo.getTargetPath();

                try {
                    // 对文件后缀进行识别，如果是hzip就进行解压，否则进行压缩
                    if (inputFilePath.endsWith(".hzip")) {
                        compressor.decompressFile(inputFilePath, outputFilePath);
                    }
                    compressor.compressFile(inputFilePath, outputFilePath);

                } catch (IOException e) {
                    System.out.println("Error compressing file: " + e.getMessage());
                }

            } else if (file.isDirectory()) {
                System.out.println("The path is a directory.");
                // 记录 文件夹结构
                DirectoryStructure ds = DirectoryStructure.getInstance();
                ds.buildTree(file);
                ds.printStructure(ds.getRoot(), "");

                // 递归读取文件夹内容
                List<File> allFiles = listAllFiles(file);
                for (File f : allFiles) {
                    System.out.println("File: " + f.getPath());
                }
                // todo 对文件夹的解压缩
            } else {
                System.out.println("The path is neither a file nor a directory.");
            }
        } else {
            System.out.println("The path does not exist.");
        }
    }

    /**
     * 获取目录下所有文件
     *
     * @param dir 目录
     * @return 文件列表
     */
    public static List<File> listAllFiles(File dir) {
        List<File> fileList = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.add(file);
                } else if (file.isDirectory()) {
                    fileList.addAll(listAllFiles(file));
                }
            }
        }
        return fileList;
    }
}
