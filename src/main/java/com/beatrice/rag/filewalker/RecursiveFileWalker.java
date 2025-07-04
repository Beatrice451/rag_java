package com.beatrice.rag.filewalker;

import com.beatrice.rag.Config;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class RecursiveFileWalker implements FileWalker {

    private final Set<String> filesToIgnore;
    private final Set<Path> dirsToIgnore;

    public RecursiveFileWalker(Set<String> filesToIgnore, Set<Path> dirsToIgnore) {
        this.filesToIgnore = filesToIgnore;
        this.dirsToIgnore = dirsToIgnore;
    }

    public RecursiveFileWalker() {
        this(Config.FILE_EXTENSIONS_TO_IGNORE, Config.DIRS_TO_IGNORE);
    }

    @Override
    public Stream<Path> walk(Path repo) {
        List<Path> collectedPaths = new ArrayList<>();
        try {
            Files.walkFileTree(repo, new SimpleFileVisitor<>() {
                @Override
                public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs) {
                    return shouldIgnoreDir(dir) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                }

                @Override
                public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
                    if (!shouldIgnoreFile(file)) {
                        collectedPaths.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return collectedPaths.stream();
    }

    private boolean shouldIgnoreDir(Path dirPath) {
        return dirsToIgnore.contains(dirPath.getFileName()) || dirPath.getFileName().toString().startsWith(".");
    }

    private boolean shouldIgnoreFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String[] parts = fileName.split("\\.");
        String ext = parts[parts.length - 1];
        return filesToIgnore.contains(ext) || filePath.getFileName().toString().startsWith(".");
    }
}
