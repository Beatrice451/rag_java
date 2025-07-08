package com.beatrice.rag.repositoryprocessor.chunkembedder;

import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;

import java.util.List;

public interface ChunkEmbedder {
    List<Embedding> embedChunks(List<Chunk> chunks);

    void addEmbeddingToChunk(List<Chunk> chunks, List<Embedding> embeddings);
}
