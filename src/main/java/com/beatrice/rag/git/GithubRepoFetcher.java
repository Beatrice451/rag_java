package com.beatrice.rag.git;

import com.beatrice.rag.git.dto.RepositoryContext;

import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;


// TODO implement
public class GithubRepoFetcher implements RepositoryFetcher {
    private static final Logger logger = Logger.getLogger(GithubRepoFetcher.class.getName());
    private final String apiUrl;
    private final String repoUrl;

    public GithubRepoFetcher(String apiUrl, String repoUrl) {
        this.repoUrl = repoUrl;
        this.apiUrl = extractApiUrl();
    }


    @Override
    public RepositoryContext fetch(String source) {
        return null;
    }

    private boolean validateUrl(String url) {
        Pattern p = Pattern.compile("github\\.com[:/]([^/]+/[^/]+)(?:\\.git)?$");
        return p.matcher(url).find();
    }

    private String extractApiUrl() {
        return this.repoUrl.replace("https://github.com/", "https://api.github.com/repos/");

    }


}
