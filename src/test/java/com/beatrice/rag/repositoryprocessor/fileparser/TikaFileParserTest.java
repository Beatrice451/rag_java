package com.beatrice.rag.repositoryprocessor.fileparser;

import com.beatrice.rag.exception.ParserException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TikaFileParserTest {
    static TikaFileParser parser;
    static BufferedWriter writer;
    static String fileName = "test.txt";
    static String testText = "Here's some text to test FileParserImpl class";

    @BeforeAll
    static void setup() throws IOException {
        Set<String> mimeTypes = Set.of(
                "text/plain",
                "text/html",
                "text/xml",
                "application/xml",
                "application/json"
        );

        parser = new TikaFileParser(mimeTypes);
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


}