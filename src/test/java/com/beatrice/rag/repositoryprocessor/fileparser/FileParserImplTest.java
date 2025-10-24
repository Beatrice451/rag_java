package com.beatrice.rag.repositoryprocessor.fileparser;

import com.beatrice.rag.exception.ParserException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class FileParserImplTest {
    static FileParserImpl parser;
    static BufferedWriter writer;
    static String fileName = "test.txt";
    static String testText = "Here's some text to test FileParserImpl class";

    @BeforeAll
    static void setup() throws IOException {
        parser = new FileParserImpl();
        writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(testText);
        writer.newLine();
        writer.flush();
    }

    @AfterAll
    static void teardown() throws IOException {
        Files.deleteIfExists(Paths.get("test.txt"));
        writer.close();

    }

    @Test
    void testParse() throws ParserException, IOException {
        FileData fileData = parser.parse(Path.of("test.txt"));
        assertNotNull(fileData);
        assertEquals(testText, fileData.content().strip());
        var expected = new HashMap<String, String>();
        expected.put("ext", fileName.split("\\.")[1]); // im not sure if this is gonna work, so should rewrite this later
        expected.put("path", fileName);
        expected.put("size", String.valueOf(Files.size(Path.of(fileName)))); // the design is very humane
        expected.put("lines", String.valueOf(testText.split("\\n").length));

        assertEquals(expected, fileData.metadata());

    }

    @Test
    void testParse_fileIsNotReadable() {
        File file = Paths.get("test.txt").toFile();
        var _ = file.setReadable(false); // making the test file unreadable
        assertThrows(ParserException.class, () -> parser.parse(Path.of("test.txt")));
        var _ = file.setReadable(true);
    }
}