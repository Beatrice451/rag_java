package com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.parser;

import com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.wrappers.Tree;

import java.util.HashSet;
import java.util.Set;

public class TreeSitterParser implements AutoCloseable {
    static {
        System.loadLibrary("treesitterjni");
    }

    private final long parserPtr;
    private Language currentLanguage;

    public TreeSitterParser() {
        this.parserPtr = createParser();
    }

    /**
     * Create TSParser and return the pointer to it
     *
     * @return pointer to the created parser
     */
    private static native long createParser();

    public Tree parse(String sourceCode, Language language) {
        if (currentLanguage == null || currentLanguage != language) {
            this.setLanguage(language);
            currentLanguage = language;
        }
        return createTree(sourceCode);
    }

    private Tree createTree(String sourceCode) {
        long treePtr = parse(parserPtr, sourceCode);
        if (treePtr == 0) {
            throw new RuntimeException("Failed to parse source code: tree pointer is null");
        }
        return new Tree(treePtr, sourceCode);
    }

    @Override
    public void close() {
        deleteParser(parserPtr);
    }

    private void setLanguage(Language language) {
        setParserLanguage(parserPtr, language.toString());
    }

    /**
     * Set the parse language for the parser
     *
     * @param parserPtr pointer to the parser
     * @param language  parser language to set
     */
    private native void setParserLanguage(long parserPtr, String language);

    /**
     * @param parserPtr  pointer to the created parser
     * @param sourceCode full source code of the file to parse
     * @return pointer to the generated tree (AST) of a long type
     */
    private native long parse(long parserPtr, String sourceCode);

    /**
     * @param parserPtr pointer to the parser to delete
     */
    private native void deleteParser(long parserPtr);
}
