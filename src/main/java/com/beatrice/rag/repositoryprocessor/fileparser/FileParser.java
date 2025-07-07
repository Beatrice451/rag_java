package com.beatrice.rag.repositoryprocessor.fileparser;

import java.io.IOException;
import java.nio.file.Path;

public interface FileParser {
    FileData parse(Path file) throws IOException;
}
