package org.example.entity.file;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件节点类
 */
@Data
public class FileNode {
    /**
     * 节点名
     */
    private String name;
    /**
     * 是否为文件
     */
    private boolean isFile;
    /**
     * 子节点列表
     */
    private List<FileNode> children;
    /**
     * 文件内容偏移量
     */
    private long offset;

    public FileNode(String name, boolean isFile) {
        this.name = name;
        this.isFile = isFile;
        this.children = new ArrayList<>();
        this.offset = -1;
    }


    public void addChild(FileNode child) {
        this.children.add(child);
    }

}

