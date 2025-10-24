package com.beatrice.rag.git.fetcher;

import com.beatrice.rag.git.dto.RepositoryContext;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GithubRepoFetcherTest {

    @TempDir
    Path tempDir;

    GithubRepoFetcher fetcher;

    @BeforeEach
    void setUp() {
        fetcher = new GithubRepoFetcher(tempDir);
    }

    @Test
    void fetch_validHttpsUrl_createsContextAndClones() throws Exception {
        String repoUrl = "https://github.com/user/repo.git";
        Path expectedLocalPath = tempDir.resolve("user_repo");

        // Mock static Git.cloneRepository()
        try (MockedStatic<Git> gitStatic = Mockito.mockStatic(Git.class)) {
            CloneCommand mockClone = mock(CloneCommand.class);
            Git mockGit = mock(Git.class);

            gitStatic.when(Git::cloneRepository).thenReturn(mockClone);
            when(mockClone.setURI(repoUrl)).thenReturn(mockClone);
            when(mockClone.setDirectory(any())).thenReturn(mockClone);
            when(mockClone.call()).thenReturn(mockGit);

            RepositoryContext context = fetcher.fetch(repoUrl);

            assertEquals("user/repo", context.fullName());
            assertEquals("user", context.owner());
            assertEquals(expectedLocalPath, context.localRepoPath());
            assertEquals(repoUrl, context.remoteUrl());
            assertFalse(context.isLocal());

            verify(mockClone).setURI(repoUrl);
            verify(mockClone).setDirectory(expectedLocalPath.toFile());
        }
    }

    @Test
    void fetch_invalidUrl_throwsException() {
        String invalidUrl = "https://example.com/notgithub/repo.git";
        assertThrows(IllegalArgumentException.class, () -> fetcher.fetch(invalidUrl));
    }

    @Test
    void fetch_existingClone_skipsClone() throws IOException {
        String repoUrl = "https://github.com/user/repo.git";
        Path localPath = tempDir.resolve("user_repo");
        Files.createDirectories(localPath.resolve(".git"));

        try (MockedStatic<Git> gitStatic = Mockito.mockStatic(Git.class)) {
            RepositoryContext ctx = fetcher.fetch(repoUrl);
            assertEquals("user/repo", ctx.fullName());
            gitStatic.verifyNoInteractions(); // No clone attempted
        }
    }

    @Test
    void extractFullName_validUrl_returnsOwnerRepo() {
        String url = "git@github.com:user/repo.git";
        String result = invokeExtractFullName(url);
        assertEquals("user/repo", result);
    }

    @Test
    void extractFullName_invalidUrl_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                invokeExtractFullName("https://notgithub.com/repo"));
    }

    // Helper to access private method for unit testing
    private String invokeExtractFullName(String url) {
        try {
            var method = GithubRepoFetcher.class.getDeclaredMethod("extractFullName", String.class);
            method.setAccessible(true);
            return (String) method.invoke(fetcher, url);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException re) throw re;
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
