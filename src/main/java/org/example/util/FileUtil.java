package org.example.util;

import lombok.extern.slf4j.Slf4j;
import org.example.compressor.HuffmanCompressor;
import org.example.constant.OperateType;
import org.example.entity.file.DirectoryStructure;
import org.example.entity.file.FileBasicInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作工具类
 */
@Slf4j
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
    }

    /**
     * 文件类型识别
     *
     * @param filePath 文件路径
     */
    public static void FileTypeReader(String filePath, OperateType chosenType) {
        File file = new File(filePath);
        if (file.exists()) {
            // 判断文件时单文件还是文件夹
            if (file.isFile()) {
                // 对单文件的哈夫曼压缩
                HuffmanCompressor compressor = new HuffmanCompressor();
                FileBasicInfo fileBasicInfo = FileBasicInfo.getInstance();
                String inputFilePath = fileBasicInfo.getFilePath();
                String outputFilePath = fileBasicInfo.getTargetPath();
                try {
                    // 判断操作类型是压缩还是解压
                    if (OperateType.COMPRESS.equals(chosenType)) {

                        compressor.compressFile(inputFilePath, outputFilePath);
                    } else if (OperateType.DECOMPRESS.equals(chosenType)) {
                        compressor.decompressFile(inputFilePath, outputFilePath);
                    }
                } catch (Exception e) {
                    log.error("FileUtil-54: Error compressing file: {}", e.getMessage());
                }
            } else if (file.isDirectory()) {
                System.out.println("The path is a directory.");
                // 记录文件夹结构
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
                log.error("FileUtil-70: The path is neither a file nor a directory.");
            }
        } else {
            log.error("FileUtil-73: The path does not exist.");
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
