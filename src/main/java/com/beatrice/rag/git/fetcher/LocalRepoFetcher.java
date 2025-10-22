package com.beatrice.rag.git.fetcher;

import com.beatrice.rag.git.dto.RepositoryContext;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Fetches metadata from a local Git repository located on the filesystem.
 * <p>
 * This implementation opens an existing local repository and extracts context data
 * such as the owner, full name, and remote URL (if available).
 * </p>
 */
public class LocalRepoFetcher implements RepositoryFetcher {
    private static final Logger logger = Logger.getLogger(LocalRepoFetcher.class.getName());

    /**
     * Fetches metadata for the local repository located at the given path.
     *
     * @param source absolute or relative path to a local Git repository
     * @return {@link RepositoryContext} describing the repository
     * @throws IllegalArgumentException if the provided path is not a Git repository
     */
    @Override
    public RepositoryContext fetch(String source) {
        try (Repository repo = open(Path.of(source))) {
            return extractContext(repo);
        }
    }


    /**
     * Opens a Git repository from the specified filesystem path.
     *
     * @param source path to the repository root directory
     * @return an instance of {@link Repository}
     * @throws IllegalArgumentException if the repository cannot be opened
     */
    private Repository open(Path source) throws IllegalArgumentException {
        File gitDir = source.resolve(".git").toFile();
        if (!gitDir.exists()) {
            throw new IllegalArgumentException("Not a git repository: " + source);
        }

        try {
            return new FileRepositoryBuilder()
                    .setGitDir(gitDir)
                    .readEnvironment()
                    .findGitDir()
                    .build();
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't read .git directory");
        }
    }

    /**
     * Extracts metadata from the given {@link Repository} instance.
     * Attempts to infer GitHub-specific data such as full name and owner.
     *
     * @param repo opened JGit repository instance
     * @return {@link RepositoryContext} with extracted metadata
     */
    private RepositoryContext extractContext(Repository repo) {
        String fullName = null;
        String owner = null;
        Path localRepoPath = repo.getWorkTree().toPath();
        String remoteUrl = null;
        boolean isLocal = true;


        Pattern p = Pattern.compile("github\\.com[:/]([^/]+/[^/]+)(?:\\.git)?$");
        Set<String> remoteNames = repo.getRemoteNames();
        for (String remoteName : remoteNames) {
            remoteUrl = repo.getConfig().getString("remote", remoteName, "url");
            Matcher m = p.matcher(remoteUrl);
            if (m.find()) {
                fullName = m.group(1).replace(".git", "");
                owner = fullName.split("/")[0];
                remoteUrl = repo.getConfig().getString("remote", remoteName, "url");
                break;
            }
        }

        if (fullName == null || fullName.isBlank()) {
            logger.warning("Can't extract GitHub URL from remotes");
        }
        return new RepositoryContext(
                fullName, owner, localRepoPath, remoteUrl, isLocal
        );
    }
}
