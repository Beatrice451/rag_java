package com.beatrice.rag.git;

import com.beatrice.rag.git.dto.RepositoryContext;

public interface RepositoryFetcher {
    RepositoryContext fetch(String source);
}
