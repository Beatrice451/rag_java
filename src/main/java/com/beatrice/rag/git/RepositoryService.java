package com.beatrice.rag.git;

import com.beatrice.rag.git.dto.RepositoryContext;
import com.beatrice.rag.git.fetcher.RepositoryFetcher;

import java.nio.file.Path;


/**
 * High-level service that provides a unified interface
 * for loading repositories from either local or remote sources.
 * <p>
 * Internally delegates repository fetching to {@link RepositoryFetcherFactory}.
 * </p>
 */
public class RepositoryService {
    private final RepositoryFetcherFactory factory;


    /**
     * Creates a new service instance using the specified base directory
     * for cloning remote repositories.
     *
     * @param baseCloneDir root directory for all cloned repositories
     */
    public RepositoryService(Path baseCloneDir) {
        this.factory = new RepositoryFetcherFactory(baseCloneDir);
    }

    /**
     * Loads a repository from the given source.
     * <p>
     * Automatically determines whether the source refers to a local path
     * or a remote GitHub repository.
     * </p>
     *
     * @param source local repository path or remote GitHub URL
     * @return {@link RepositoryContext} describing the loaded repository
     * @throws IllegalArgumentException if the source cannot be processed
     */
    public RepositoryContext loadRepository(String source) {
        RepositoryFetcher fetcher = factory.create(source);
        return fetcher.fetch(source);
    }
}
