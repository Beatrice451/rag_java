package com.beatrice.rag.repositoryprocessor.chunkembedder;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import static com.beatrice.rag.Config.OPENAI_API_KEY;
import static com.beatrice.rag.Config.OPENAI_BASE_URL;

public abstract class AbstractEmbedder<I, R> implements Embedder<I, R> {
    protected static final OpenAIClient client = OpenAIOkHttpClient.builder()
            .apiKey(OPENAI_API_KEY)
            .baseUrl(OPENAI_BASE_URL)
            .build();

    @Override
    public abstract R embed(I input);

}