package com.beatrice.rag.generator;

import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.beatrice.rag.Config.*;

public class Generator {
    private static final String PROMPT = """
    [INST] You are an AI assistant analyzing provided code chunks.
    Your task is to answer the user’s question ONLY if the answer is explicitly found in the given chunks.

    Rules:
    1. If the chunks contain enough information for a precise answer, provide a concise response, quoting relevant parts of the code.

    2. If the chunks lack the necessary information or context is insufficient, reply strictly with:
    "I cannot answer the question due to insufficient context."
    (No explanations, guesses, or assumptions allowed!).


    Examples:

    Question: "Where is the function foo() located?"
    Response (if foo is not in the chunks): "I cannot answer the question due to insufficient context."

    Question: "What parameters does bar() take?"
    Response (if bar is in the chunks): "The function bar() accepts (x: int, y: str), see line 12: def bar(x: int, y: str):"
    Answer ONLY using this context:
    %s


    Question: %s [/INST]
""";


    private static final Logger logger = Logger.getLogger(Generator.class.getName());
    private static final OpenAIClient client;

    static {
        client = OpenAIOkHttpClient.builder()
                .apiKey(OPENAI_API_KEY)
                .baseUrl(OPENAI_BASE_URL)
                .build();
    }

    public String generate(String question, List<Chunk> chunks) {
        logger.info("Sending question to the generator. Waiting for the answer");
        List<String> context = new ArrayList<>();
        for (Chunk chunk : chunks) {
            String sb = "source: %s\n".formatted(chunk.getSource()) +
                    "lines: %d:%d\n".formatted(chunk.getLineStart(), chunk.getLineEnd()) +
                    "metadata: %s\n".formatted(chunk.getMetadata()) +
                    "chunk content: %s\n".formatted(chunk.getText());
            context.add(sb);
        }

        String prompt = PROMPT.formatted(context, question);
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(GENERATIVE_MODEL_NAME)
                .addUserMessage(prompt)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);
        Optional<String> content = completion.choices().getFirst().message().content();
        return content.orElse("The answer is empty");
    }
}
