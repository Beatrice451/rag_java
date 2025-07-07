package com.beatrice.rag.repositoryprocessor.fileparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileParserImpl implements FileParser {
    @Override
    public FileData parse(Path file) throws IOException {
        FileData output = new FileData();
        String content = readFile(file);
        Map<String, String> metadata = extractMetadata(file, content);
        output.setContent(content);
        output.setMetadata(metadata);
        return output;
    }

    private static String readFile(Path file) throws IOException {
        if (Files.isReadable(file)) {
            String line;
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }
            }
            return content.toString();
        }
        throw new IOException("File is not readable");
    }

    private static long countLines(String input) {
        if (input.isBlank()) return 0;
        String[] lines = input.split("\r\n|\r|\n");
        return lines.length;
    }

    private static Map<String, String> extractMetadata(Path file, String content) throws IOException {
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
