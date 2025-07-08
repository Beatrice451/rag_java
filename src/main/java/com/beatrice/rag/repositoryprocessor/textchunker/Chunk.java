package com.beatrice.rag.repositoryprocessor.textchunker;

import com.beatrice.rag.repositoryprocessor.chunkembedder.Embedding;

import java.nio.file.Path;
import java.util.Map;

public class Chunk {
    private String text;
    private Path source;
    private int lineStart;
    private int lineEnd;
    private Map<String, String> metadata;
    private Embedding embedding;
    private String contentHash; // md5 hash of the chunk content

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Path getSource() {
        return source;
    }

    public void setSource(Path source) {
        this.source = source;
    }

    public int getLineStart() {
        return lineStart;
    }

    public void setLineStart(int lineStart) {
        this.lineStart = lineStart;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    public void setLineEnd(int lineEnd) {
        this.lineEnd = lineEnd;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;


    }

    @Override
    public String toString() {
        return "Chunk{" +
                "text='" + text + '\'' +
                ", source=" + source +
                ", lineStart=" + lineStart +
                ", lineEnd=" + lineEnd +
                ", metadata=" + metadata +
                ", embedding=" + embedding +
                ", contentHash='" + contentHash + '\'' +
                '}';
    }
}


