package com.beatrice.rag.git.fetcher;

import com.beatrice.rag.git.dto.RepositoryContext;

/**
 * Common abstraction for fetching repository data from different sources.
 * <p>
 * Implementations provide logic for retrieving repository metadata
 * and building a {@link RepositoryContext} instance, regardless of whether
 * the source is local or remote.
 * </p>
 */
public interface RepositoryFetcher {
    RepositoryContext fetch(String source);
}
