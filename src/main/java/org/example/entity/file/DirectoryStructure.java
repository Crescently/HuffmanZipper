package org.example.entity.file;

import lombok.Data;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 文件目录结构类
 */
@Data
public class DirectoryStructure implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 文件根节点
     */
    private FileNode root;

    private DirectoryStructure() {
        // 私有构造方法防止实例化
    }

    public static DirectoryStructure getInstance() {
        return InstanceHolder.instance;
    }

    public FileNode buildTree(File file) {
        FileNode node = new FileNode(file.getName(), file.isFile());
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                node.addChild(buildTree(child));
            }
        }
        return node;
    }


    private static final class InstanceHolder {
        private static final DirectoryStructure instance = new DirectoryStructure();
    }

}
