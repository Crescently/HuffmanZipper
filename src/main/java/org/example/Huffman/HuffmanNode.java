package org.example.Huffman;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 哈夫曼树节点
 */
@Getter
public class HuffmanNode implements Comparable<HuffmanNode>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 数据
     */
    private byte data;
    /**
     * 权重
     */
    private int weight;
    /**
     * 左子树
     */
    private HuffmanNode left;
    /**
     * 右子树
     */
    private HuffmanNode right;

    public HuffmanNode(byte data, int weight) {
        this.data = data;
        this.weight = weight;
    }

    public HuffmanNode(int weight, HuffmanNode left, HuffmanNode right) {
        this.weight = weight;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return Integer.compare(this.weight, o.weight);
    }
}
