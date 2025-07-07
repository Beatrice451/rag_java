package com.beatrice.rag.repositoryprocessor.filewalker;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileWalker {
    Stream<Path> walk(Path repo);
}
