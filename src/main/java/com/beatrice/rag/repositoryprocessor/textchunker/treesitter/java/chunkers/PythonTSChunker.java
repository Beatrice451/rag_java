package com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.chunkers;

import com.beatrice.rag.repositoryprocessor.fileparser.FileData;
import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.beatrice.rag.repositoryprocessor.textchunker.TextChunker;

import java.util.List;

public class PythonTSChunker implements TextChunker, AutoCloseable {
    @Override
    public List<Chunk> chunk(FileData fileData) {
        return List.of();
    }

    @Override
    public void close() {

    }
}
