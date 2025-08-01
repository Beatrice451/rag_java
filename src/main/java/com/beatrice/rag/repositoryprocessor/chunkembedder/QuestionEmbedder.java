package com.beatrice.rag.repositoryprocessor.chunkembedder;

import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;

import java.util.logging.Logger;

import static com.beatrice.rag.Config.EMBEDDING_MODEL_NAME;

public class QuestionEmbedder extends AbstractEmbedder<String, Embedding> {


    private static final Logger logger = Logger.getLogger(QuestionEmbedder.class.getName());

    @Override
    public Embedding embed(String input) {
        long start = System.nanoTime();
        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .input(input)
                .model(EMBEDDING_MODEL_NAME)
                .build();

        logger.info("Received request to embed question");

        CreateEmbeddingResponse response = client.embeddings().create(params);
        logger.fine("Embedded question in %d ms".formatted((System.nanoTime() - start) / 1_000_000));
        return new Embedding(response.data().getFirst().embedding());

    }
}
