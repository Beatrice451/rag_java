package com.beatrice.rag.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.beatrice.rag.Config.GITHUB_PAT;

public class GitRepository {
    private static final Logger logger = Logger.getLogger(GitRepository.class.getName());
    private final String repoUrl;
    private final String apiUrl;
    private final HttpClient client = HttpClient.newHttpClient();
    private final String repoName;
    private final Path repoLocalDir;
    private final Path repoLocalPath;


    public GitRepository(String repoUrl, Path repoLocalDir) {
        if (repoUrl.endsWith(".git")) {
            repoUrl = repoUrl.replaceFirst(".git$", "");
        }
        this.repoUrl = repoUrl;
        this.apiUrl = extractApiUrl();
        this.repoName = extractRepoName();
        this.repoLocalDir = repoLocalDir;
        this.repoLocalPath = this.repoLocalDir.resolve(repoName);
    }

    public GitRepository(String repoUrl) {
        this(repoUrl, Path.of("cloned_repos"));
    }

    public Path getRepoLocalDir() {
        return repoLocalDir;
    }

    public Path getRepoLocalPath() {
        return repoLocalPath;
    }

    public boolean isCloned() {
        return Files.isDirectory(this.repoLocalPath);
    }

    public void cloneRepo() {
        logger.info("Trying to clone repository: %s".formatted(this.repoName));
        if (this.isCloned()) {
            logger.info("Repository %s already cloned. Using existing copy".formatted(this.repoName));
            return;
        }
        try (Git res = Git.cloneRepository()
                .setURI(this.repoUrl)
                .setDirectory(this.repoLocalPath.toFile())
                .call()) {

        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getInfo(String... fields) {
        String body;
        Map<String, Object> mapResponse;
        ObjectMapper mapper = new ObjectMapper();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.apiUrl))
                    .header("Authorization", "Bearer %s".formatted(GITHUB_PAT))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                logger.warning("GitHub API returned a non-200 response code");
                return new HashMap<>();
            }

            body = response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            mapResponse = mapper.readValue(body, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        var fieldsToKeep = Set.copyOf(Arrays.asList(fields));

        return mapResponse.entrySet()
                .stream()
                .filter(entry -> fieldsToKeep.contains(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    /**
     * Checks if a GitHub repository exists by sending a HEAD request to the GitHub API.
     * <p>
     * Returns {@code true} if the repository exists (HTTP 200),
     * {@code false} if it does not exist (HTTP 404).
     * <p>
     * Throws {@code IllegalStateException} for any unexpected HTTP response status.
     *
     * @return {@code true} if the repository exists; {@code false} if it does not exist
     * @throws RuntimeException      if the request to GitHub API fails
     * @throws IllegalStateException if the response status is neither 200 nor 404
     */

    public boolean isRepoExists() {
        int code;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.apiUrl))
                    .header("Authorization", "Bearer %s".formatted(GITHUB_PAT))
                    .HEAD()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            code = response.statusCode();
            if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                return false;
            }

            if (code == HttpURLConnection.HTTP_OK) {
                return true;
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        throw new IllegalStateException("Unexpected response from Github: " + code);
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getRepoName() {
        return repoName;
    }

    @Override
    public String toString() {
        return "GitRepository{" +
                "repoUrl='" + repoUrl + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", repoName='" + repoName + '\'' +
                ", repoLocalDir='" + repoLocalDir + '\'' +
                ", repoLocalPath='" + repoLocalPath + '\'' +
                '}';
    }

    private String extractApiUrl() {
        return this.repoUrl.replace("https://github.com/", "https://api.github.com/repos/");

    }

    private String extractRepoName() {
        String[] splitUrl = this.repoUrl.split("/");
        return splitUrl[splitUrl.length - 1];
    }

}