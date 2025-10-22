package com.beatrice.rag.git;

import com.beatrice.rag.git.fetcher.GithubRepoFetcher;
import com.beatrice.rag.git.fetcher.LocalRepoFetcher;
import com.beatrice.rag.git.fetcher.RepositoryFetcher;

import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Factory class responsible for creating the appropriate {@link RepositoryFetcher}
 * implementation based on the provided source string.
 * <p>
 * Determines whether the input refers to a local path or a remote GitHub URL
 * and returns a corresponding fetcher instance.
 * </p>
 */
public class RepositoryFetcherFactory {
    private final Path baseCloneDir;

    /**
     * Creates a new factory instance that uses the specified directory
     * as a base for cloning remote repositories.
     *
     * @param baseCloneDir root directory for all cloned repositories
     */
    public RepositoryFetcherFactory(Path baseCloneDir) {
        this.baseCloneDir = baseCloneDir;
    }


    /**
     * Creates an appropriate {@link RepositoryFetcher} instance
     * for the given source string.
     *
     * @param source repository path or URL
     * @return {@link LocalRepoFetcher} for local repositories
     *         or {@link GithubRepoFetcher} for GitHub URLs
     * @throws IllegalArgumentException if the source type is unsupported
     */
    public RepositoryFetcher create(String source) {
        if (isLocalPath(source)) {
            return new LocalRepoFetcher();
        }
        if (isGithubUrl(source)) {
            return new GithubRepoFetcher(baseCloneDir);
        }
        throw new IllegalArgumentException("Unsupported repository source: " + source);
    }

    /**
     * Checks whether the given string points to a valid local path.
     *
     * @param source possible local repository path
     * @return {@code true} if the path exists, otherwise {@code false}
     */
    private boolean isLocalPath(String source) {
        return Files.exists(Path.of(source));
    }

    /**
     * Checks whether the given string matches the format of a GitHub URL.
     *
     * @param source possible remote repository URL
     * @return {@code true} if the URL points to GitHub, otherwise {@code false}
     */
    private boolean isGithubUrl(String source) {
        return source.startsWith("https://github.com") || source.startsWith("git@github.com");
    }
}
