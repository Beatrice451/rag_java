package com.beatrice.rag.repositoryprocessor.chunkembedder;

import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;

import java.util.List;

public interface ChunkEmbedder {


    /**
     * Embeds the content of the given chunks
     *
     * @param chunks List of chunks to be embedded
     * @return List of chunks with their embeddings
     * @see Chunk
     */
    List<Chunk> embedChunks(List<Chunk> chunks);

}
