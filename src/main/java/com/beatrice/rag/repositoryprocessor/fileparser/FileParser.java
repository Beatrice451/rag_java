package com.beatrice.rag.repositoryprocessor.fileparser;

import com.beatrice.rag.exception.ParserException;

import java.io.IOException;
import java.nio.file.Path;

public interface FileParser {
    FileData parse(Path file) throws IOException, ParserException;
}
