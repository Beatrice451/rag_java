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

    /**
     * @param treePtr pointer to the AST to delete
     */
    public native void deleteTree(long treePtr);

    public List<Node> getNodesOfType(String type) {
        List<Node> nodes = new ArrayList<>();
        long[] nodePtrs = getNodesOfType(treePtr, type);
        for (long nodePtr : nodePtrs) {
            Node node = createNode(nodePtr, sourceCode);
            nodes.add(node);
        }
        return nodes;
    }

    private Node createNode(long nodePtr, String sourceCode) {
        Node node = new Node(nodePtr, sourceCode);
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

    /**
     * @param treePtr  pointer to the AST
     * @param nodeType name of the node type to extract
     * @return array of pointers to nodes of the specified type
     */
    private native long[] getNodesOfType(long treePtr, String nodeType);

}
