package com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.wrappers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Node {
    static {
        System.loadLibrary("treesitterjni");
    }

    private final long nodePtr;
    private final Tree tree;
    private final int startByte;
    private final int endByte;

    public Node(long nodePtr, Tree tree) {
        if (nodePtr == 0) {
            throw new IllegalArgumentException("Node pointer cannot be 0");
        }
        this.nodePtr = nodePtr;
        this.tree = tree;
        this.startByte = getStartByte(nodePtr);
        this.endByte = getEndByte(nodePtr);
    }

    public String getContent() {
        return tree.getSourceCode().substring(startByte, endByte);
    }

    public List<Node> getNodesOfType(String type) {
        List<Node> result = new ArrayList<>();
        long[] nodePtrs = getNodesOfType(this.nodePtr, type);
        for (long ptr : nodePtrs) {
            result.add(new Node(ptr, this.tree));
        }
        return result;
    }

    public Node getChildByFieldName(String fieldName) {
        long childPtr = getChildByFieldName(this.nodePtr, fieldName);
        return new Node(childPtr, this.tree);
    }



    /**
     * @param nodePtr pointer to the node to free
     */
    private native void freeNode(long nodePtr);

    public void freeNode() {
        freeNode(this.nodePtr);
    }

    public long getNodePtr() {
        return nodePtr;
    }

    public long[] getNodeCoordinates() {
        return getNodeCoordinates(nodePtr);
    }

    public void close() {
        this.freeNode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return nodePtr == node.nodePtr;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodePtr);
    }

    /**
     * @param nodePtr pointer to the node
     * @return start position of the node in bytes
     */
    private native int getStartByte(long nodePtr);

    /**
     * @param nodePtr pointer to the node
     * @return end position of the node in bytes
     */
    private native int getEndByte(long nodePtr);

    /**
     * @param nodePtr pointer to the node
     * @return array of the form [startRow, startColumn, endRow, endColumn]
     */
    private native long[] getNodeCoordinates(long nodePtr);

    private native long getChildByFieldName(long nodePtr, String fieldName);

    private native long[] getNodesOfType(long nodePtr, String type);
}
