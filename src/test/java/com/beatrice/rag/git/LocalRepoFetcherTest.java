package com.beatrice.rag.git;

import com.beatrice.rag.git.fetcher.LocalRepoFetcher;
import org.junit.jupiter.api.Test;

public class LocalRepoFetcherTest {

    @Test
    public void testOpenRepository() {
        LocalRepoFetcher fetcher = new LocalRepoFetcher();
        String source = "C:\\Users\\Beatrice\\IdeaProjects\\rag_java";
        System.out.println(fetcher.fetch(source));
    }
}

