package com.beatrice.rag.database;

import com.beatrice.rag.repositoryprocessor.chunkembedder.Embedding;
import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;


/**
 * Data Access Object (DAO) for managing {@link Chunk} data and embeddings in the database.
 * <p>
 * Provides methods for persisting chunks, retrieving relevant chunks based on embedding cosine similarity, etc.
 * </p>
 *
 * <p>
 * This class abstracts all low-level database interactions, allowing other modules to remain storage-agnostic.
 * </p>
 *
 * <p>
 * <b>Note: </b> This implementation assumes PostgreSQL as the underlying database system and uses
 * <a href="https://github.com/pgvector/pgvector">pgvector</a> extension for storaging and querying vector embeddings.
 * </p>
 */
public class ChunkDao {
    private static final Logger logger = Logger.getLogger(ChunkDao.class.getName());
    private final Connection connection;

    public ChunkDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * Persists the taken chunk to a database
     *
     * @param chunk Chunk to persist
     */
    public void saveChunk(Chunk chunk) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                """
                        INSERT INTO embeddings
                            (content_hash, file_path, embedding, content, metadata, line_start, line_end, source_repo_name)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING
                       """
        )) {
            pstmt.setString(1, chunk.getContentHash());
            pstmt.setString(2, String.valueOf(chunk.getSource()));
            pstmt.setObject(3, chunk.getEmbedding().getValues());
            pstmt.setString(4, chunk.getText());
            pstmt.setObject(5, mapToJsonString(chunk.getMetadata()), Types.OTHER);
            pstmt.setInt(6, chunk.getLineStart());
            pstmt.setInt(7, chunk.getLineEnd());
            pstmt.setString(8, chunk.getSourceRepo());
            if (pstmt.executeUpdate() == 1) {
                logger.fine("Chunk %s saved to database".formatted(chunk.getContentHash()));
            } else {
                logger.fine("Chunk %s already in database. Ignoring".formatted(chunk.getContentHash()));
            }

        } catch (SQLException e) {
            logger.warning("Exception occurred while saving chunk to db: " + e);
        }


    }

    /**
     * Retrieves the most relevant chunks based on cosine similarity to the given question embedding,
     * using the <a href="https://github.com/pgvector/pgvector">pgvector</a> extension's {@code <=>} operator
     *
     * @param embeddedQuestion Embedding of the question for which cosine similarity is calculated
     * @param limit            How many relevant chunks to return
     * @return List of chunks ranked by relevance to the question embedding
     */
    public List<Chunk> retrieveRelevantChunks(Embedding embeddedQuestion, int limit, String fullRepoName) {
        List<Chunk> chunks = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM embeddings WHERE source_repo_name = ? ORDER BY embedding <=> ?::vector LIMIT ?"
        )) {
            pstmt.setString(1, fullRepoName);
            pstmt.setObject(2, embeddedQuestion.getValues());
            pstmt.setInt(3, limit);

            try (ResultSet res = pstmt.executeQuery()) {
                while (res.next()) {
                    String contentHash = res.getString("content_hash");
                    Path source = Path.of(res.getString("file_path"));
                    int lineStart = res.getInt("line_start");
                    int lineEnd = res.getInt("line_end");
                    PGobject pgEmbedding = (PGobject) res.getObject("embedding");
                    Embedding embedding = new Embedding(pgObjectToFloatArray(pgEmbedding));
                    String content = res.getString("content");
                    PGobject pgMetadata = (PGobject) res.getObject("metadata");
                    Map<String, String> metadata = jsonStringToMap(pgMetadata.getValue());
                    Chunk chunk = new Chunk();

                    chunk.setContentHash(contentHash);
                    chunk.setSource(source);
                    chunk.setMetadata(metadata);
                    chunk.setText(content);
                    chunk.setEmbedding(embedding);
                    chunk.setLineStart(lineStart);
                    chunk.setLineEnd(lineEnd);
                    chunks.add(chunk);
                }
            }
        } catch (SQLException e) {
            logger.warning("Exception occurred while selecting vectors from database: " + e);
        }
        return chunks;
    }


    /**
     * Returns a set of hashes of chunks that already exist in the database.
     *
     * @param chunks The list of chunks to check for existence in the database.
     * @return A set of hashes of the chunks that already exist in the database.
     */
    public Set<String> getExistingChunkHashes(List<Chunk> chunks) {
        String placeholders = String.join(", ", Collections.nCopies(chunks.size(), "?"));
        String sql = "SELECT content_hash FROM embeddings WHERE content_hash IN (%s)".formatted(placeholders);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < chunks.size(); i++) {
                pstmt.setString(i + 1, chunks.get(i).getContentHash());
            }
            Set<String> existing = null;
            try (ResultSet rs = pstmt.executeQuery()) {

                existing = new HashSet<>();
                while (rs.next()) {
                    existing.add(rs.getString(1));
                }
            }

            return existing;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * Converts the given Map to JSON string
     *
     * @param map Map to be converted
     * @return JSON string representation of the given map
     */
    private String mapToJsonString(Map<?, ?> map) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the given JSON string to a map
     *
     * @param json JSON string to be converted
     * @return converted map
     */
    private Map<String, String> jsonStringToMap(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the given {@link PGobject} to a float array
     * <p>
     * It is used to handle the {@code vector} data type
     * provided by the <a href="https://github.com/pgvector/pgvector">pgvector</a> extension
     * </p>
     *
     * @param pGobject {@link PGobject} to extract a float array from
     * @return float array extracted from the object
     */
    private float[] pgObjectToFloatArray(PGobject pGobject) {
        String value = pGobject.getValue();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PGobject is null or empty");
        }
        value = value.replaceAll("[\\[\\]]", "");
        String[] parts = value.split(",");
        float[] res = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            res[i] = Float.parseFloat(parts[i]);
        }

        return res;
    }
}
