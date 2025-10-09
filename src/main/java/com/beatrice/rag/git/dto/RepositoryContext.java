package com.beatrice.rag.git.dto;


import java.nio.file.Path;

// TODO replace fields with actual DB columns
public record RepositoryContext(
        String fullName,
        String owner,
        Path localRepoPath,
        String remoteUrl,
        boolean isLocal
) {
}
