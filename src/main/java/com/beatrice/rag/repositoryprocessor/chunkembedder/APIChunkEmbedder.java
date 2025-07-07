package com.beatrice.rag.repositoryprocessor.chunkembedder;

import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;

import java.util.ArrayList;
import java.util.List;

import static com.beatrice.rag.Config.*;

public class APIChunkEmbedder implements ChunkEmbedder {
    private static final OpenAIClient client;

    static {
        client = OpenAIOkHttpClient.builder()
                .apiKey(OPENAI_API_KEY)
                .baseUrl(OPENAI_BASE_URL)
                .build();
    }

    @Override
    public List<List<Float>> embedChunks(List<Chunk> chunks) {
        List<List<Float>> embeddings = new ArrayList<>();
        List<String> texts = chunks.stream()
                .map(Chunk::getText)
                .toList();

        for (String text : texts) {
            EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                    .model(EMBEDDING_MODEL_NAME)
                    .input(text)
                    .build();
            CreateEmbeddingResponse embedding = client.embeddings().create(params);
            embeddings.add(embedding.data().getFirst().embedding());
        }
        return embeddings;
    }


}
