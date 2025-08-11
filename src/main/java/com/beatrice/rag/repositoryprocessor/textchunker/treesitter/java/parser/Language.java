package com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.parser;

public enum Language {
    JAVA("java"),
    PYTHON("python"),
    JAVASCRIPT("javascript");

    private final String value;

    Language(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return value;
    }
}
