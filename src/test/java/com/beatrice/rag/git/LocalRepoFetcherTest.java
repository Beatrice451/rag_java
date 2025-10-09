package com.beatrice.rag.git;

import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class LocalRepoFetcherTest {

    @Test
    public void testOpenRepository() {
        LocalRepoFetcher fetcher = new LocalRepoFetcher();
        String source = "C:\\Users\\Beatrice\\IdeaProjects\\rag_java";
        System.out.println(fetcher.fetch(source));
    }
}

