package org.example.entity.file;

import lombok.Data;
import org.example.entity.huffman.HuffmanNode;

/**
 * 单文件压缩信息
 */
@Data
public class SingleFileZipInfo {
    /**
     * 文件后缀名
     */
    private String fileSuffix;

    /**
     * 哈夫曼树根节点
     */
    private HuffmanNode root;
}
