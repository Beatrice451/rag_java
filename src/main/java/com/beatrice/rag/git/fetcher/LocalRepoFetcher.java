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

public class LocalRepoFetcher implements RepositoryFetcher {
    private static final Logger logger = Logger.getLogger(LocalRepoFetcher.class.getName());

    @Override
    public RepositoryContext fetch(String source) {
        try (Repository repo = open(Path.of(source))) {
            return extractContext(repo);
        }
    }

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
