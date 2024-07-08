package org.example.entity.file;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件节点类
 */
@Data
@NoArgsConstructor
public class FileNode implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
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
     * 文件内容大小
     */
    private long fileSize;

    public FileNode(String name, boolean isFile) {
        this.name = name;
        this.isFile = isFile;
        this.children = new ArrayList<>();
    }


    public void addChild(FileNode child) {
        this.children.add(child);
    }




}

