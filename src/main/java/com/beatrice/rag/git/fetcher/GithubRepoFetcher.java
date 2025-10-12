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

public class GithubRepoFetcher implements RepositoryFetcher {
    private static final Logger logger = Logger.getLogger(GithubRepoFetcher.class.getName());
    private static final Pattern GITHUB_URL_PATTERN =
            Pattern.compile("github\\.com[:/]([^/]+/[^/]+)(?:\\.git)?$");

    private final Path baseCloneDir;

    public GithubRepoFetcher(Path baseCloneDir) {
        this.baseCloneDir = baseCloneDir;
    }

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

    private boolean validateUrl(String url) {
        return GITHUB_URL_PATTERN.matcher(url).find();
    }

    private String extractFullName(String repoUrl) {
        Matcher m = GITHUB_URL_PATTERN.matcher(repoUrl);
        if (m.find()) {
            return m.group(1).replace(".git", "");
        }
        throw new IllegalArgumentException("Cannot extract full name from: " + repoUrl);
    }

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
