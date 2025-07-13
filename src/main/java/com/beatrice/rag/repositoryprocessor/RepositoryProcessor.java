package com.beatrice.rag.repositoryprocessor;

import com.beatrice.rag.database.ChunkDao;
import com.beatrice.rag.database.Database;
import com.beatrice.rag.repositoryprocessor.chunkembedder.ChunkEmbedder;
import com.beatrice.rag.repositoryprocessor.chunkembedder.Embedding;
import com.beatrice.rag.repositoryprocessor.fileparser.FileData;
import com.beatrice.rag.repositoryprocessor.fileparser.FileParser;
import com.beatrice.rag.repositoryprocessor.filewalker.FileWalker;
import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.beatrice.rag.repositoryprocessor.textchunker.TextChunker;
import com.beatrice.rag.utils.GitRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Orchestrates a full repository processing cycle.
 * <p>
 * This class coordinates the following modules:  <br>
 * {@link FileWalker} for filtering repository files, <br>
 * {@link FileParser} for parsing file content, <br>
 * {@link TextChunker} for splitting file content into smaller pieces of code (chunks), <br>
 * {@link ChunkEmbedder} for generating vector embeddings <br>
 * </p>
 *
 * @see FileWalker
 * @see FileParser
 * @see TextChunker
 * @see ChunkEmbedder
 */
public class RepositoryProcessor {
    private final FileWalker walker;
    private final FileParser parser;
    private final TextChunker chunker;
    private final ChunkEmbedder embedder;
    private final ChunkDao dao;

    public RepositoryProcessor(FileWalker walker, FileParser parser, TextChunker chunker, ChunkEmbedder embedder) {
        this.walker = walker;
        this.parser = parser;
        this.chunker = chunker;
        this.embedder = embedder;
        this.dao = new ChunkDao(Database.getConnection());
    }


    /**
     * Processes the given Git repository and returns a list of text chunks with embeddings.
     * <p>
     * This method performs the following steps:
     * <ul>
     *   <li>Clones the repository (if not already cloned)</li>
     *   <li>Walks through the files in the repository and filters them using {@code FileWalker}</li>
     *   <li>Parses each file with {@code FileParser}</li>
     *   <li>Splits the parsed content into chunks using {@code TextChunker}</li>
     *   <li>Generates embeddings for the chunks via {@code ChunkEmbedder}</li>
     *   <li>Associates the embeddings with the corresponding chunks</li>
     *   <li>Persists each chunk in the database via {@code ChunkDao}</li>
     * </ul>
     *
     * @param repository the Git repository to process
     * @return a list of {@link Chunk} objects with associated embeddings and metadata
     * @throws RuntimeException if file parsing fails
     */
    public List<Chunk> processRepository(GitRepository repository) {
        repository.cloneRepo();
        List<Chunk> chunks = new ArrayList<>();

        Path repoRoot = repository.getRepoLocalPath();
        Stream<Path> filteredFiles = walker.walk(repoRoot);
        filteredFiles.forEach(file -> {
            FileData parsed = null;
            try {
                parsed = parser.parse(file);
            } catch (IOException e) {
                throw new RuntimeException("Error occurred while parsing files: " + e);
            }
            List<Chunk> fileChunks = chunker.chunk(parsed);
            chunks.addAll(fileChunks);
        });

        List<Embedding> embeddings = embedder.embedChunks(chunks);
        embeddings = embeddings.stream().map(Embedding::normalize).toList();
        embedder.addEmbeddingToChunk(chunks, embeddings);
        chunks.forEach(dao::saveChunk);
        return chunks;
    }

}
