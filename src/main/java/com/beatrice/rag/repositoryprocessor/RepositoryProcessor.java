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
        embedder.addEmbeddingToChunk(chunks, embeddings);
        chunks.forEach(dao::saveChunk);
        return chunks;
    }

}
