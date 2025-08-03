package com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.wrappers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class Node {
    static {
        System.loadLibrary("treesitterjni");
    }

    private final long nodePtr;
    private final String content;
    private final int startByte;
    private final int endByte;

    public Node(long nodePtr, String sourceCode) {
        if (nodePtr == 0) {
            throw new IllegalArgumentException("Node pointer cannot be 0");
        }
        this.nodePtr = nodePtr;
        this.startByte = getStartByte(nodePtr);
        this.endByte = getEndByte(nodePtr);
        this.content = new String(
                Arrays.copyOfRange(sourceCode.getBytes(StandardCharsets.UTF_8), startByte, endByte),
                StandardCharsets.UTF_8
        );
    }



    /**
     * @param nodePtr pointer to the node to free
     */
    private static native void freeNode(long nodePtr);

    public void freeNode() {
        freeNode(this.nodePtr);
    }

    public long getNodePtr() {
        return nodePtr;
    }

    public long[] getNodeCoordinates() {
        return getNodeCoordinates(nodePtr);
    }

    public String getContent() {
        return content;
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
}
