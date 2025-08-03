package com.beatrice.rag.repositoryprocessor.textchunker.treesitter.jni;

public class TreeSitterJNI {
    static {
        System.loadLibrary("treesitterjni");
    }

    public native long createParser();

    public native long parse(long parserPtr, String sourceCode);

    public native String getRootNode(long treePtr);

    public static void main(String[] args) {
        TreeSitterJNI jni = new TreeSitterJNI();

        long parser = jni.createParser();
        System.out.println("Parser created: " + parser);

        String javaCode = "public class Test { public static void main(String[] args) {} }";
        long tree = jni.parse(parser, javaCode);
        System.out.println("Parsing done, tree ptr: " + tree);

        String rootType = jni.getRootNode(tree);
        System.out.println("Root node type: " + rootType);
    }
}
