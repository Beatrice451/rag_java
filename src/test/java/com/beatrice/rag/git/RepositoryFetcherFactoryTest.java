package com.beatrice.rag.git;

import com.beatrice.rag.git.fetcher.GithubRepoFetcher;
import com.beatrice.rag.git.fetcher.LocalRepoFetcher;
import com.beatrice.rag.git.fetcher.RepositoryFetcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryFetcherFactoryTest {
    RepositoryFetcherFactory factory;


    @BeforeEach
    void setUp() {
        factory = new RepositoryFetcherFactory(Path.of("~/")); // stub path, not used anyway
    }

    @Test
    void testCreate_repoIsLocal() {
        RepositoryFetcher fetcher = factory.create(".");
        assertNotNull(fetcher);
        assertInstanceOf(LocalRepoFetcher.class, fetcher);
    }

    @Test
    void testCreate_repoIsRemote() {
        RepositoryFetcher fetcher = factory.create("git@github.com:Beatrice451/rag_java.git");
        assertNotNull(fetcher);
        assertInstanceOf(GithubRepoFetcher.class, fetcher);
    }

    @Test
    void testCreate_unsupportedSource() {
        assertThrows(IllegalArgumentException.class, () -> factory.create("qwerty"));
    }
}