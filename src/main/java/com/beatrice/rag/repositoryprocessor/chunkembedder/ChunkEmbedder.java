package com.beatrice.rag.repositoryprocessor.chunkembedder;

import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;

import java.util.List;

public interface ChunkEmbedder {
    public List<List<Float>> embedChunks(List<Chunk> chunks);
}
