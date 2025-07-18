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
    public List<Chunk> embedChunks(List<Chunk> chunks) {
        List<Chunk> result = new ArrayList<>();
        if (chunks.isEmpty()) {
            logger.info("No chunks to embed — possibly all embeddings already exist in the database");
            return result;
        }

        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .inputOfArrayOfStrings(chunks.stream()
                        .map(Chunk::getText)
                        .toList())
                .model(EMBEDDING_MODEL_NAME)
                .build();

        logger.info("Received request to embed %s chunks".formatted(chunks.size()));

        CreateEmbeddingResponse response = client.embeddings().create(params);
        for (int i = 0; i < chunks.size(); i++) {
            Embedding embedding = new Embedding(response.data().get(i).embedding());
            Chunk newChunk = new Chunk(chunks.get(i));
            newChunk.setEmbedding(embedding);
            result.add(newChunk);
        }


        return result;
    }

}
