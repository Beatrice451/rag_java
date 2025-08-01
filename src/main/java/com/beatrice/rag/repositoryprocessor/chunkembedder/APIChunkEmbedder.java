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
 *
 */
public class APIChunkEmbedder extends AbstractEmbedder<List<Chunk>, List<Chunk>> {
    private static final Logger logger = Logger.getLogger(APIChunkEmbedder.class.getName());

    @Override
    public List<Chunk> embed(List<Chunk> chunks) {
        long start = System.nanoTime();
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

        logger.fine("Embedded %d chunks in %d ms".formatted(result.size(), (System.nanoTime() - start) / 1_000_000));
        return result;
    }

}
