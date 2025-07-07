package com.beatrice.rag.repositoryprocessor.textchunker;

import com.beatrice.rag.repositoryprocessor.fileparser.FileData;

import java.util.List;

public interface TextChunker {
    List<Chunk> chunk(FileData fileData);
}
