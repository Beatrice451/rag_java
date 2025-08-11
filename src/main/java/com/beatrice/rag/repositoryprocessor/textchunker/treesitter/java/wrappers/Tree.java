package com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.wrappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tree implements AutoCloseable {
    static {
        System.loadLibrary("treesitterjni");
    }

    private long treePtr;
    private final String sourceCode;
    private final Set<Node> childNodes = new HashSet<>();

    public Tree(long treePtr, String content) {
        if (treePtr == 0) {
            throw new IllegalArgumentException("Tree pointer cannot be 0");
        }
        this.treePtr = treePtr;
        this.sourceCode = content;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    /**
     * @param treePtr pointer to the AST to delete
     */
    public native void deleteTree(long treePtr);

    public List<Node> getNodesOfType(String type) {
        long rootNodePtr = getRootNode(this.treePtr);
        Node rootNode = createNode(rootNodePtr);
        return rootNode.getNodesOfType(type);
    }

    private Node createNode(long nodePtr) {
        Node node = new Node(nodePtr, this);
        childNodes.add(node);
        return node;
    }

    @Override
    public void close() {
        if (this.treePtr != 0) {
            childNodes.forEach(Node::close);
            childNodes.clear();
            deleteTree(treePtr);
            treePtr = 0;
        }

    }


    private native long getRootNode(long treePtr);
}
