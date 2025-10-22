package com.beatrice.rag.git.fetcher;

import com.beatrice.rag.git.dto.RepositoryContext;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches a remote GitHub repository by cloning it into a local directory.
 * <p>
 * If the repository is already cloned under the configured base directory,
 * it reuses the existing copy instead of recloning.
 * </p>
 */
public class GithubRepoFetcher implements RepositoryFetcher {
    private static final Logger logger = Logger.getLogger(GithubRepoFetcher.class.getName());
    private static final Pattern GITHUB_URL_PATTERN =
            Pattern.compile("github\\.com[:/]([^/]+/[^/]+)(?:\\.git)?$");

    private final Path baseCloneDir;


    /**
     * Creates a new fetcher that clones repositories into the specified base directory.
     *
     * @param baseCloneDir root directory for all cloned repositories
     */
    public GithubRepoFetcher(Path baseCloneDir) {
        this.baseCloneDir = baseCloneDir;
    }

    /**
     * Fetches a remote GitHub repository by cloning it if necessary,
     * then returns its metadata context.
     *
     * @param source HTTPS or SSH GitHub repository URL
     * @return {@link RepositoryContext} describing the cloned repository
     * @throws IllegalArgumentException if the URL is invalid or unsupported
     */
    @Override
    public RepositoryContext fetch(String source) {
        if (!validateUrl(source)) {
            throw new IllegalArgumentException("Invalid GitHub URL: " + source);
        }

        String fullName = extractFullName(source);
        String owner = fullName.split("/")[0];
        Path localPath = baseCloneDir.resolve(fullName.replace("/", "_"));

        clone(source, localPath);

        return new RepositoryContext(
                fullName,
                owner,
                localPath,
                source,
                false // isLocal = false
        );
    }

    /**
     * Validates the format of the GitHub repository URL.
     *
     * @param url repository URL
     * @return {@code true} if the URL matches GitHub format, otherwise {@code false}
     */
    private boolean validateUrl(String url) {
        return GITHUB_URL_PATTERN.matcher(url).find();
    }

    /**
     * Extracts the {@code owner/repo} part from the repository URL.
     *
     * @param repoUrl repository URL
     * @return full repository name in {@code owner/repo} format
     * @throws IllegalArgumentException if the URL cannot be parsed
     */
    private String extractFullName(String repoUrl) {
        Matcher m = GITHUB_URL_PATTERN.matcher(repoUrl);
        if (m.find()) {
            return m.group(1).replace(".git", "");
        }
        throw new IllegalArgumentException("Cannot extract full name from: " + repoUrl);
    }

    /**
     * Clones the remote repository into the given local directory if it is not already cloned.
     *
     * @param repoUrl   repository URL
     * @param localPath target directory for the clone
     * @throws RuntimeException if cloning fails
     */
    private void clone(String repoUrl, Path localPath) {
        if (Files.exists(localPath.resolve(".git"))) {
            logger.info("Repository already cloned: " + localPath);
            return;
        }

        try {
            Files.createDirectories(localPath);
            logger.info("Cloning repository: " + repoUrl + " into " + localPath);

            try (Git ignored = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localPath.toFile())
                    .call()) {
                logger.info("Clone completed: " + localPath);
            }
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to clone repository", e);
        }
    }
}
