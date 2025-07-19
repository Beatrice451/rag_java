package com.beatrice.rag.repositoryprocessor.fileparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFileParser implements FileParser {
    protected static long countLines(String input) {
        if (input.isBlank()) return 0;
        String[] lines = input.split("\r\n|\r|\n");
        return lines.length;
    }

    protected static Map<String, String> extractMetadata(Path file, String content) throws IOException {
        String[] parts = file.getFileName().toString().split("\\.");
        String ext = parts.length > 1 ? parts[parts.length - 1] : "";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("path", file.toString());
        metadata.put("size", String.valueOf(Files.size(file)));
        metadata.put("ext", ext);
        metadata.put("lines", String.valueOf(countLines(content)));
        return metadata;
    }
}
