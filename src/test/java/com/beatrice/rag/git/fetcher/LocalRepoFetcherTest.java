package com.beatrice.rag.git.fetcher;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocalRepoFetcherTest {
    static LocalRepoFetcher localRepoFetcher;
    @BeforeAll
    static void setup() {
        localRepoFetcher = new LocalRepoFetcher();
    }

    @Test
    public void testRepoFetch_repoExists() {
        String source = "."; // using this porject repo for test
        var context = localRepoFetcher.fetch(source);
        assertNotNull(context); // only not null assertion cuz im not sure what repository will be tested in the final version
    }

    @Test
    public void testRepoFetch_repoNotExists() {
        assertThrows(IllegalArgumentException.class, () -> localRepoFetcher.fetch("this/repo/does/not/exist"));
    }
}

