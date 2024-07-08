package org.example.entity.file;

import lombok.Data;
import org.example.entity.huffman.HuffmanNode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件夹压缩信息
 */
@Data
public class DirectoryZipInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 哈夫曼树根节点
     */
    private HuffmanNode root;
    /**
     * 文件目录根节点
     */
    private FileNode rootNode;


}
