package com.beatrice.rag.git.fetcher;

import com.beatrice.rag.git.dto.RepositoryContext;

public interface RepositoryFetcher {
    RepositoryContext fetch(String source);
}
