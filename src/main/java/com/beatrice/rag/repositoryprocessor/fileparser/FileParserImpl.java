package com.beatrice.rag.repositoryprocessor.fileparser;

import com.beatrice.rag.exception.ParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileParserImpl extends AbstractFileParser {
    @Override
    public FileData parse(Path file) throws IOException, ParserException {
        String content = readFile(file);
        Map<String, String> metadata = extractMetadata(file, content);
        return new FileData(content, metadata);
    }

    private static String readFile(Path file) throws IOException, ParserException {
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
        throw new ParserException("File is not readable");
    }

}
