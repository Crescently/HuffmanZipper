package org.example.util;

import com.esotericsoftware.kryo.io.Input;
import lombok.extern.slf4j.Slf4j;
import org.example.compressor.DirectoryCompressor;
import org.example.compressor.SingleFileCompressor;
import org.example.constant.Constants;
import org.example.constant.OperateType;
import org.example.entity.file.FileBasicInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


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
     * @param filePath   文件路径
     * @param chosenType 操作类型
     */
    public static void FileTypeReader(String filePath, OperateType chosenType) {
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("The path does not exist.");
            return;
        }
        FileBasicInfo fileBasicInfo = FileBasicInfo.getInstance();
        String inputPath = fileBasicInfo.getFilePath();
        String outputPath = fileBasicInfo.getTargetPath();
        SingleFileCompressor singleFileCompressor = new SingleFileCompressor();
        DirectoryCompressor directoryCompressor = new DirectoryCompressor();
        try {
            if (chosenType.equals(OperateType.COMPRESS)) {
                if (file.isFile()) {
                    handleFile(singleFileCompressor, inputPath, outputPath, chosenType);
                } else if (file.isDirectory()) {
                    handleDirectory(directoryCompressor, inputPath, outputPath, chosenType);
                } else {
                    log.error("The path is neither a file nor a directory.");
                }
            } else if (chosenType.equals(OperateType.DECOMPRESS)) {
                String fileTypeInZip = getFileTypeFromZip(inputPath);
                if (Constants.SINGLE_FILE.equals(fileTypeInZip)) {
                    handleFile(singleFileCompressor, inputPath, outputPath, chosenType);
                } else if (Constants.DIRECTORY.equals(fileTypeInZip)) {
                    handleDirectory(directoryCompressor, inputPath, outputPath, chosenType);
                } else {
                    log.error("Unknown file type in compressed file.");
                }

            }
        } catch (Exception e) {
            log.error("Error processing file or directory: {}", e.getMessage());
        }
    }

    /**
     * 从压缩文件中获取文件类型
     *
     * @param compressedFilePath 压缩文件路径
     * @return 文件类型
     * @throws IOException 读取文件时可能抛出的异常
     */
    private static String getFileTypeFromZip(String compressedFilePath) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(compressedFilePath), Constants.BUFFER_SIZE);
             Input input = new Input(bis)) {
            return input.readString();
        }
    }


    /**
     * 处理单个文件的压缩或解压
     *
     * @param singleFileCompressor SingleFileCompressor 实例
     * @param inputPath            输入文件路径
     * @param outputPath           输出文件路径
     * @param chosenType           操作类型（压缩或解压）
     * @throws Exception 处理过程中可能抛出的异常
     */
    private static void handleFile(SingleFileCompressor singleFileCompressor, String inputPath, String outputPath, OperateType chosenType) throws Exception {
        if (OperateType.COMPRESS.equals(chosenType)) {
            singleFileCompressor.compressFile(inputPath, outputPath);
        } else if (OperateType.DECOMPRESS.equals(chosenType)) {
            singleFileCompressor.decompressFile(inputPath, outputPath);
        }
    }

    /**
     * 处理文件夹的压缩或解压
     *
     * @param directoryCompressor DirectoryCompressor 实例
     * @param inputPath           输入文件夹路径
     * @param outputPath          输出文件夹路径
     * @param chosenType          操作类型（压缩或解压）
     * @throws Exception 处理过程中可能抛出的异常
     */
    private static void handleDirectory(DirectoryCompressor directoryCompressor, String inputPath, String outputPath, OperateType chosenType) throws Exception {
        if (OperateType.COMPRESS.equals(chosenType)) {
            directoryCompressor.compressDirectory(inputPath, outputPath);
        } else if (OperateType.DECOMPRESS.equals(chosenType)) {
            directoryCompressor.decompressDirectory(inputPath, outputPath);
        }
    }

}
