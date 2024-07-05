package org.example.Huffman;

import lombok.Getter;


@Getter
public class HuffmanNode implements Comparable<HuffmanNode> {
    private byte data;
    private int frequency;
    private HuffmanNode left;
    private HuffmanNode right;

    public HuffmanNode(byte data, int frequency) {
        this.data = data;
        this.frequency = frequency;
    }

    public HuffmanNode(int frequency, HuffmanNode left, HuffmanNode right) {
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return Integer.compare(this.frequency, o.frequency);
    }
}
