package com.beatrice.rag.repositoryprocessor.chunkembedder;

/**
 * Interface for embedding text into its numeric representation (vector)
 * <p>
 *     Implementations of this interface are responsible for converting textual data
 *     into numerical vector embeddings.
 * </p>
 *
 * @param <I> the type of input data
 * @param <R> the type of returned result
 */
public interface Embedder<I, R> {
    R embed(I input);

}
