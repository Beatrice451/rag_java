package com.beatrice.rag.git;

import com.beatrice.rag.git.dto.RepositoryContext;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;


// TODO implement
public class GithubRepoFetcher implements RepositoryFetcher {
    private static final Logger logger = Logger.getLogger(GithubRepoFetcher.class.getName());



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

    private String extractFullName() {
        String[] parts = repoUrl.split("/");
        return parts[parts.length - 2] + "/" + parts[parts.length - 1].replaceAll(".git$", "");
    }

    private void cloneRepo(String branchToClone) {
        String branchRef = "refs/heads/" + branchToClone;
        logger.info("Trying to clone repository: %s, branch: %s".formatted(this.repoName, branchToClone));
        if (this.isCloned(branchToClone)) {
            logger.info("Repository %s with branch %s already cloned. Using existing copy".formatted(this.repoName, branchRef));
            return;
        }
        try (Git res = Git.cloneRepository()
                .setURI(this.repoUrl)
                .setBranch(branchRef)
                .setDirectory(this.repoLocalPath.toFile())
                .call()) {

        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }


}
