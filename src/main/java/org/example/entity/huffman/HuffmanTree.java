package org.example.entity.huffman;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * 哈夫曼树
 */
@Getter
public class HuffmanTree implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 根节点
     */
    private final HuffmanNode root;
    /**
     * 哈夫曼编码
     */
    private final Map<Byte, String> huffmanCodes;

    /**
     * 根据权重构建哈夫曼树
     * @param weightMap 权重表
     */
    public HuffmanTree(Map<Byte, Integer> weightMap) {
        // 使用优先队列来构建哈夫曼树
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(HuffmanNode::getWeight));

        // 将所有字节和其对应的权重转换为哈夫曼节点，并加入优先队列
        for (Map.Entry<Byte, Integer> entry : weightMap.entrySet()) {
            priorityQueue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        // 合并节点直到优先队列中只剩下一个节点，即哈夫曼树的根节点
        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            HuffmanNode parent = null;
            if (right != null) {
                parent = new HuffmanNode(left.getWeight() + right.getWeight(), left, right);
            }
            priorityQueue.add(parent);
        }
        // 哈夫曼树的根节点
        root = priorityQueue.poll();
        huffmanCodes = new HashMap<>();
        buildHuffmanCodes(root, "");
    }

    /**
     * 根据根节点构建哈夫曼树
     * @param root 根节点
     */
    public HuffmanTree(HuffmanNode root) {
        this.root = root;
        huffmanCodes = new HashMap<>();
        buildHuffmanCodes(root, "");
    }

    /**
     * 递归构建哈夫曼编码表
     * @param node 当前节点
     * @param code 当前节点对应的编码
     */
    private void buildHuffmanCodes(HuffmanNode node, String code) {
        if (node == null) {
            return;
        }
        // 如果当前节点是叶子节点，则将其编码放入编码表
        if (node.getLeft() == null && node.getRight() == null) {
            huffmanCodes.put(node.getData(), code);
        }

        buildHuffmanCodes(node.getLeft(), code + "0");
        buildHuffmanCodes(node.getRight(), code + "1");
    }

}

