package org.example.Huffman;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

@Getter
public class HuffmanTree {
    private HuffmanNode root;
    private Map<Byte, String> huffmanCodes;

    public HuffmanTree(Map<Byte, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();
        for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
            priorityQueue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            HuffmanNode parent = null;
            if (right != null) {
                parent = new HuffmanNode(left.getFrequency() + right.getFrequency(), left, right);
            }
            priorityQueue.add(parent);
        }

        this.root = priorityQueue.poll();
        this.huffmanCodes = new HashMap<>();
        buildHuffmanCodes(root, "");
    }

    private void buildHuffmanCodes(HuffmanNode node, String code) {
        if (node == null) {
            return;
        }
        if (node.getLeft() == null && node.getRight() == null) {
            huffmanCodes.put(node.getData(), code);
        }
        buildHuffmanCodes(node.getLeft(), code + "0");
        buildHuffmanCodes(node.getRight(), code + "1");
    }

}
