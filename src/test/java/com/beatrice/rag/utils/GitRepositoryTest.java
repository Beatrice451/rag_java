package com.beatrice.rag.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitRepositoryTest {
    private final String testRepo = "https://github.com/octocat/Hello-World.git";
    @TempDir
    Path tempDir;
    private GitRepository repo;

    @BeforeEach
    public void setUp() {
        repo = new GitRepository(testRepo, tempDir);
    }


    @Tag("unit")
    @Test
    public void testGetApiUrl() {
        assertEquals("https://api.github.com/repos/octocat/Hello-World", repo.getApiUrl());
    }

    @Tag("unit")
    @Test
    public void testExtractRepoName() {
        assertEquals("Hello-World", repo.getRepoName());
    }

    @Tag("unit")
    @Test
    public void testNameGitStripping() {
        assertEquals("https://github.com/octocat/Hello-World", repo.getRepoUrl());
    }


    /**
     * Check {@code cloneRepo} method.
     * <p>
     * In this test {@code cloneRepo} called and verifies that the repository is cloned successfully.
     * </p>
     */
    @Tag("integration")
    @Test
    public void testRepoClone() {
        repo.cloneRepo();
        assertTrue(Files.exists(repo.getRepoLocalPath()));
        assertTrue(repo.isCloned());
    }

    /**
     * Check {@code getInfo} method with valid fields.
     * <p>
     * In this test {@code getInfo} called with an array of fields {@code {"id", "name", "full_name"}}
     * * and verifies that the returned map contains the expected keys and corresponding values.
     * </p>
     */
    @Tag("integration")
    @Test
    public void testGetInfoValidFields() {
        String[] fields = {"id", "name", "full_name"};
        Set<String> fieldsSet = new HashSet<>(Set.of(fields));

        Map<String, Object> info = repo.getInfo(fields);
        Map<String, Object> expected = new HashMap<>();
        expected.put("id", 1296269);
        expected.put("name", "Hello-World");
        expected.put("full_name", "octocat/Hello-World");
        assertEquals(expected, info);
        assertEquals(fieldsSet, info.keySet());

    }

    /**
     * Same as the test above, but invalid field name is passed ({@code "invalid field name"}).
     * <p>
     * Check that field with invalid name is ignored.
     * </p>
     */
    @Tag("integration")
    @Test
    public void testGetInfoInvalidFields() {
        String[] fields = {"invalid field name", "id", "name"};
        Map<String, Object> info = repo.getInfo(fields);
        Map<String, Object> expected = new HashMap<>();
        expected.put("id", 1296269);
        expected.put("name", "Hello-World");
        assertEquals(expected, info);
    }

    @Tag("integration")
    @Test
    public void testIsRepoExistsTrue() {
        boolean result = repo.isRepoExists();
        assertTrue(result);
    }

    @Tag("integration")
    @Test
    public void isRepoExists_shouldReturnFalse_whenRepoDoesNotExist() {
        GitRepository repo = new GitRepository("https://github.com/thisRepo/doesNotExist");
        boolean result = repo.isRepoExists();
        assertFalse(result);
    }

    @Test
    public void isRepoExists_shouldThrowIllegalStateException_whenResponseIsUnexpected() throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        when(mockResponse.statusCode()).thenReturn(500);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        Field clientField = GitRepository.class.getDeclaredField("client");
        clientField.setAccessible(true);

        clientField.set(repo, mockClient);

        assertThrows(IllegalStateException.class, repo::isRepoExists);
    }


}