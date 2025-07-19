package com.beatrice.rag.repositoryprocessor.fileparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileParserImpl extends AbstractFileParser {
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

}
