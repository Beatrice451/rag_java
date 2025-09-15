package com.beatrice.rag.repositoryprocessor.textchunker;

import com.beatrice.rag.repositoryprocessor.fileparser.FileData;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CharacterTextChunker implements TextChunker {
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_CHUNK_OVERLAP = 100;
    private static final String[] DEFAULT_SEPARATORS = {"\n\n", "\n", " "};
    private final int chunkSize;
    private final int chunkOverlap;
    private final String[] separators;


    public CharacterTextChunker(int chunkSize, int chunkOverlap, String[] separators) {
        if (chunkOverlap < 0 || chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException("Invalid chunk size or overlap");
        }

        if (separators == null || separators.length == 0) {
            throw new IllegalArgumentException("Separators cannot be null or empty");
        }

        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.separators = separators;
    }

    public CharacterTextChunker() {
        this(DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP, DEFAULT_SEPARATORS);
    }

    @Override
    public List<Chunk> chunk(FileData fileData) {
        String content = fileData.content();
        Map<String, String> textMetadata = fileData.metadata();

        List<Chunk> chunks = new ArrayList<>();
        int start = 0;
        int text_length = content.length();

        while (start < text_length) {
            int end = Math.min(start + chunkSize, text_length);
            int sepPos = findSeparatorIndex(content, end);
            if (sepPos > start) {
                end = sepPos;
            }

            String chunkContent = content.substring(start, end);
            if (chunkContent.isBlank()) {
                int newStart = end - this.chunkOverlap;
                if (newStart <= start) {
                    start = end;
                } else {
                    start = newStart;
                }
                continue;
            }
            Chunk chunk = new Chunk();
            chunk.setText(chunkContent);

            int lineStart = getLineNumber(content, start);
            int lineEnd = getLineNumber(content, end);
            Path source = Path.of(textMetadata.get("path"));

            chunk.setLineStart(lineStart);
            chunk.setLineEnd(lineEnd);
            chunk.setSource(source);
            chunk.setMetadata(textMetadata);
            chunk.setContentHash(hash(chunk));

            chunks.add(chunk);

            int newStart = end - this.chunkOverlap;
            if (newStart <= start) {
                start = end;
            } else {
                start = newStart;
            }


        }
        return chunks;
    }

    private int findSeparatorIndex(String text, int end) {
        String substr = text.substring(0, end);
        for (String sep : this.separators) {
            int pos = substr.lastIndexOf(sep);
            if (pos >= 0) {
                return pos;
            }
        }

        return end;


    }

    private int getLineNumber(String text, int pos) {
        return (int) text.substring(0, pos)
                .chars()
                .filter(ch -> ch == '\n')
                .count() + 1;
    }
}