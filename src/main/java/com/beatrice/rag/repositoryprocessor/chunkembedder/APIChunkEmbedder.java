package com.beatrice.rag.repositoryprocessor.chunkembedder;

import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.beatrice.rag.Config.*;

/**
 * {@link ChunkEmbedder} that uses the OpenAI API to generate embeddings for given chunks.
 * <p>
 * This class is a concrete implementation of {@link ChunkEmbedder} that uses the OpenAI API to generate embeddings
 * for given chunks.
 * It reads the API key from the {@link com.beatrice.rag.Config}.
 * <p>
 * The base URL of the API is read from the {@link com.beatrice.rag.Config} class.
 */
public class APIChunkEmbedder implements ChunkEmbedder {
    private static final Logger logger = Logger.getLogger(APIChunkEmbedder.class.getName());
    private static final OpenAIClient client;


    static {
        client = OpenAIOkHttpClient.builder()
                .apiKey(OPENAI_API_KEY)
                .baseUrl(OPENAI_BASE_URL)
                .build();
    }


    @Override
    public List<Embedding> embedChunks(List<Chunk> chunks) {
        List<Embedding> embeddings = new ArrayList<>();
        List<String> texts = chunks.stream()
                .map(Chunk::getText)
                .toList();

        for (String text : texts) {
            logger.fine("Embedding text: %d/%d".formatted(embeddings.size() + 1, texts.size()));
            EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                    .model(EMBEDDING_MODEL_NAME)
                    .input(text)
                    .build();
            CreateEmbeddingResponse embedding = client.embeddings().create(params);
            embeddings.add(
                    new Embedding(embedding.data().getFirst().embedding())
            );
        }
        return embeddings;
    }


    /**
     * @param chunks     Chunks to add embedding vectors to
     * @param embeddings Embedding to add to chunks
     */
    @Override
    public void addEmbeddingToChunk(List<Chunk> chunks, List<Embedding> embeddings) {
        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException("Chunks and embeddings count mismatch");
        }

        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            Embedding embedding = embeddings.get(i);
            if (chunk.getEmbedding() != null) {
                throw new IllegalArgumentException("Chunk at index %d already has an embedding".formatted(i));
            }
            chunk.setEmbedding(embedding);
        }
    }

}
