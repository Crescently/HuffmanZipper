package org.example.entity.file;

import lombok.Data;

/**
 * 文件基本信息类
 */
@Data
public class FileBasicInfo {
    /**
     * 待压缩文件路径
     */
    private String filePath;
    /**
     * 压缩后文件输出目录
     */
    private String targetPath;

    private FileBasicInfo() {
        // 私有构造方法防止实例化
    }


    public static FileBasicInfo getInstance() {
        return FileBasicInfo.InstanceHolder.instance;
    }

    private static final class InstanceHolder {
        private static final FileBasicInfo instance = new FileBasicInfo();
    }
}
