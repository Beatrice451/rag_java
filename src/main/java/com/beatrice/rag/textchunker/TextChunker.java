package com.beatrice.rag.textchunker;

import com.beatrice.rag.fileparser.FileData;

import java.util.List;

public interface TextChunker {
    public List<Chunk> chunk(FileData fileData);
}
