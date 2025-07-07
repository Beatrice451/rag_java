package com.beatrice.rag.repositoryprocessor.fileparser;

import java.util.Map;
import java.util.Objects;

public class FileData {
    private String content;
    private Map<String, String> metadata;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FileData fileData = (FileData) o;
        return Objects.equals(content, fileData.content) && Objects.equals(metadata, fileData.metadata);
    }

    @Override
    public String toString() {
        return "FileData{" +
                "content='" + content + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
