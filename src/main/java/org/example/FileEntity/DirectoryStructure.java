package org.example.FileEntity;

import lombok.Data;

import java.io.File;
import java.util.Objects;

/**
 * 文件目录结构类
 */
@Data
public class DirectoryStructure {
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

    public void printStructure(FileNode node, String indent) {
        System.out.println(indent + (node.isFile() ? "File: " : "Dir: ") + node.getName());
        for (FileNode child : node.getChildren()) {
            printStructure(child, indent + "  ");
        }
    }

    private static final class InstanceHolder {
        private static final DirectoryStructure instance = new DirectoryStructure();
    }

}
