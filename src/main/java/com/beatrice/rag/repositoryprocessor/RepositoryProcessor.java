package com.beatrice.rag.repositoryprocessor;

import com.beatrice.rag.database.ChunkDao;
import com.beatrice.rag.exception.ParserException;
import com.beatrice.rag.repositoryprocessor.chunkembedder.Embedder;
import com.beatrice.rag.repositoryprocessor.fileparser.FileData;
import com.beatrice.rag.repositoryprocessor.fileparser.FileParser;
import com.beatrice.rag.repositoryprocessor.filewalker.FileWalker;
import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.beatrice.rag.repositoryprocessor.textchunker.TextChunker;
import com.beatrice.rag.git.GitRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Orchestrates a full repository processing cycle.
 * <p>
 * This class coordinates the following modules:  <br>
 * {@link FileWalker} for filtering repository files, <br>
 * {@link FileParser} for parsing file content, <br>
 * {@link TextChunker} for splitting file content into smaller pieces of code (chunks), <br>
 * {@link Embedder} for generating vector embeddings <br>
 * </p>
 *
 * @see FileWalker
 * @see FileParser
 * @see TextChunker
 * @see Embedder
 */
public class RepositoryProcessor {
    private static final Logger logger = Logger.getLogger(RepositoryProcessor.class.getName());

    private final FileWalker walker;
    private final FileParser parser;
    private final TextChunker chunker;
    private final Embedder<List<Chunk>, List<Chunk>> embedder;
    private final ChunkDao dao;

    public RepositoryProcessor(FileWalker walker,
                               FileParser parser,
                               TextChunker chunker,
                               Embedder<List<Chunk>, List<Chunk>> embedder,
                               ChunkDao dao) {
        this.walker = walker;
        this.parser = parser;
        this.chunker = chunker;
        this.embedder = embedder;
        this.dao = dao;
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
        return this.processRepository(repository, "main");
    }

    public List<Chunk> processRepository(GitRepository repository, String repoBranch) {
        repository.cloneRepo(repoBranch);
        List<Chunk> chunks = new ArrayList<>();

        Path repoRoot = repository.getRepoLocalPath();
        Stream<Path> filteredFiles = walker.walk(repoRoot);
        filteredFiles.map(this::safeParse)
                .flatMap(Optional::stream)
                .flatMap(parsed -> chunker.chunk(parsed).stream())
                .forEach(chunks::add);
        chunks.forEach(chunk -> chunk.setSourceRepo(repository.getFullName()));

        Set<String> existingHashes = getExistingChunkHashes(chunks, 500);
        List<Chunk> chunksToEmbed = chunks.stream()
                .filter(chunk -> !existingHashes.contains(chunk.getContentHash()))
                .toList();

        List<Chunk> embeddedChunks = embedder.embed(chunksToEmbed);
        embeddedChunks.forEach(chunk ->
                chunk.setEmbedding(chunk.getEmbedding().normalize())
        );
        embeddedChunks.forEach(dao::saveChunk);
        return embeddedChunks;
    }

    /**
     * Filter chunks that already have embeddings in the database.
     *
     * @param chunks    the list of chunks to filter
     * @param batchSize the number of chunks to query the database for at once
     * @return set of chunks that have embeddings in the database
     */
    private Set<String> getExistingChunkHashes(List<Chunk> chunks, int batchSize) {
        Set<String> existingHashes = new HashSet<>();
        int listSize = chunks.size();

        for (int start = 0; start < listSize; start += batchSize) {
            int end = Math.min(start + batchSize, listSize);
            List<Chunk> sublist = chunks.subList(start, end);
            existingHashes.addAll(this.dao.getExistingChunkHashes(sublist));
        }

        return existingHashes;
    }

    private Optional<FileData> safeParse(Path file) {
        try {
            return Optional.of(parser.parse(file));
        } catch (IOException e) {
            throw new UncheckedIOException("I/O error while parsing " + file, e);
        } catch (ParserException e) {
            logger.warning("Parser exception occurred while parsing %s: %s".formatted(file, e));
            return Optional.empty();
        }
    }

}

