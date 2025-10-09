package com.beatrice.rag.textchunker.treesitter.chunkers;

import com.beatrice.rag.exception.ParserException;
import com.beatrice.rag.repositoryprocessor.fileparser.FileData;
import com.beatrice.rag.repositoryprocessor.fileparser.FileParser;
import com.beatrice.rag.repositoryprocessor.fileparser.TikaFileParser;
import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.chunkers.JavaTSChunker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaTSChunkerTest {
    private static final Path testSourceCode;

    static {
        testSourceCode = Path.of("src/main/java/com/beatrice/rag/repositoryprocessor/textchunker/CharacterTextChunker.java");
    }

    private JavaTSChunker chunker;

    @BeforeEach
    public void setup() {chunker = new JavaTSChunker();}

    @AfterEach
    public void cleanup() {
        if (chunker != null) {
            chunker.close();
        }
    }

    @Test
    public void createChunkerTest() {
        assertNotNull(chunker);
    }

    @Test
    public void chunkTest() throws ParserException, IOException {
        FileParser parser = new TikaFileParser();
        FileData fileData = parser.parse(testSourceCode);
        List<Chunk> chunks = chunker.chunk(fileData);
        chunks.forEach(System.out::println);
    }

}
