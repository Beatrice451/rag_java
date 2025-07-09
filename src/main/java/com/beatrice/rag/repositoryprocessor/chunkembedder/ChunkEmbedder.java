package com.beatrice.rag.repositoryprocessor.chunkembedder;

import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;

import java.util.List;

public interface ChunkEmbedder {

    /**
     * Generate embeddings for each chunk in the list of chunks
     *
     * @param chunks Chunks to generate embeddings for
     * @return List of embeddings for each chunk in the input list
     */
    List<Embedding> embedChunks(List<Chunk> chunks);


    /**
     * Take a list of embeddings and add them to a list of chunks respective
     *
     * @param chunks Chunks to add embedding vectors to
     * @param embeddings Embedding to add to chunks
     */
    void addEmbeddingToChunk(List<Chunk> chunks, List<Embedding> embeddings);
}
