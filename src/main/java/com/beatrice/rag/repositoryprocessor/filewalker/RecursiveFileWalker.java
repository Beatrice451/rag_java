package com.beatrice.rag.repositoryprocessor.filewalker;

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


    /**
     * Walks the directory tree starting from the specified root path.
     * <p>
     * Filters out:
     * <ul>
     *   <li>Files and directories whose names start with a dot (e.g., <code>.git</code>, <code>.env</code>)</li>
     *   <li>Directories explicitly listed in the constructor or specified in the configuration</li>
     *   <li>Files with extensions listed in the constructor or specified in the configuration</li>
     * </ul>
     * Filtering rules are taken from constructor parameters if provided; otherwise, they fall back
     * to the configuration defined in a {@code .properties} file.
     * </p>
     *
     * @param repo the root directory to traverse
     * @return a stream of paths that are not filtered out (i.e., not ignored)
     */
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


    /**
     * Checks if the walker should ignore the given directory.
     * <p>
     * The directory is ignored if it is in the set of directories to ignore
     * or if it starts with a dot.
     * </p>
     *
     * @param dirPath the path of the directory
     * @return {@code true} if the directory should be ignored, {@code false} otherwise.
     */
    private boolean shouldIgnoreDir(Path dirPath) {
        return dirsToIgnore.contains(dirPath.getFileName()) || dirPath.getFileName().toString().startsWith(".");
    }


    /**
     * Returns {@code true} if the walker should ignore the file.
     * <p>
     * The file is ignored if it has an extension in the set of ignored
     * extensions or if it starts with a dot.
     * </p>
     *
     * @param filePath the path of the file
     * @return {@code true} if a file should be ignored, {@code false} otherwise.
     */
    private boolean shouldIgnoreFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String[] parts = fileName.split("\\.");
        String ext = parts[parts.length - 1];
        return filesToIgnore.contains(ext) || filePath.getFileName().toString().startsWith(".");
    }
}
