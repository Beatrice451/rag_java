package com.beatrice.rag.repositoryprocessor;

import com.beatrice.rag.database.ChunkDao;
import com.beatrice.rag.exception.ParserException;
import com.beatrice.rag.git.RepositoryService;
import com.beatrice.rag.git.dto.RepositoryContext;
import com.beatrice.rag.repositoryprocessor.chunkembedder.Embedder;
import com.beatrice.rag.repositoryprocessor.fileparser.FileData;
import com.beatrice.rag.repositoryprocessor.fileparser.FileParser;
import com.beatrice.rag.repositoryprocessor.filewalker.FileWalker;
import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.beatrice.rag.repositoryprocessor.textchunker.TextChunker;

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
    private static final RepositoryService repositoryService = new RepositoryService(Path.of("~/.cache/repos"));
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


    public List<Chunk> processRepository(String source) {
        RepositoryContext context = repositoryService.loadRepository(source);
        Path repoRoot = context.localRepoPath();
        List<Chunk> chunks = new ArrayList<>();

        Stream<Path> filteredFiles = walker.walk(repoRoot);
        filteredFiles.map(this::safeParse)
                .flatMap(Optional::stream)
                .flatMap(parsed -> chunker.chunk(parsed).stream())
                .forEach(chunks::add);
        chunks.forEach(chunk -> chunk.setSourceRepo(context.fullName()));

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

