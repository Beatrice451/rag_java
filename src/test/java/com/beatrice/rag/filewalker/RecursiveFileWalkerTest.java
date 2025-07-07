package com.beatrice.rag.filewalker;

import com.beatrice.rag.repositoryprocessor.filewalker.FileWalker;
import com.beatrice.rag.repositoryprocessor.filewalker.RecursiveFileWalker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecursiveFileWalkerTest {
    private static final Set<Path> ignoredDirs = Stream.of(
                    "ignored_dir"
            )
            .map(Path::of)
            .collect(Collectors.toSet());
    private static final Set<String> ignoredFileExts = Set.of(
            "exe"
    );
    @TempDir
    static Path tempDir;

    @Tag("integration")
    @Test
    public void testWalk() {
        FileWalker walker = new RecursiveFileWalker(ignoredFileExts, ignoredDirs);
        Stream<Path> stream = walker.walk(tempDir);
        Set<String> result = stream.map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toSet());
        Set<String> expected = Set.of(
                "included_file.txt",
                "nameless_included_file",
                "included_file.md"
        );

        assertEquals(result, expected);


    }

    @BeforeAll
    static void setup() {
        try {
            Path ignored_dir = Files.createDirectory(tempDir.resolve("ignored_dir"));
            Path included_dir = Files.createDirectory(tempDir.resolve("included_dir"));
            Path hidden_dir = Files.createDirectory(tempDir.resolve(".hidden_dir"));

            Files.createFile(included_dir.resolve("included_file.txt"));
            Files.createFile(included_dir.resolve("nameless_included_file"));
            Files.createFile(included_dir.resolve(".hidden_file"));
            Files.createFile(included_dir.resolve("ignored_file.exe"));
            Files.createFile(hidden_dir.resolve("should_be_ignored.txt"));
            Files.createFile(ignored_dir.resolve("should_be_ignored.txt"));
            Files.createFile(tempDir.resolve("included_file.md"));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
