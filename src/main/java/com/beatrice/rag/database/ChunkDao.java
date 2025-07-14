package com.beatrice.rag.database;

import com.beatrice.rag.repositoryprocessor.chunkembedder.Embedding;
import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ChunkDao {
    private final Connection connection;
    private static final Logger logger = Logger.getLogger(ChunkDao.class.getName());

    public ChunkDao(Connection connection) {
        this.connection = connection;
    }

    public void saveChunk(Chunk chunk) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO embeddings (content_hash, file_path, embedding, content, metadata, line_start, line_end) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING"
        )) {
            pstmt.setString(1, chunk.getContentHash());
            pstmt.setString(2, String.valueOf(chunk.getSource()));
            pstmt.setObject(3, chunk.getEmbedding().getValues());
            pstmt.setString(4, chunk.getText());
            pstmt.setObject(5, mapToJsonString(chunk.getMetadata()), Types.OTHER);
            pstmt.setInt(6, chunk.getLineStart());
            pstmt.setInt(7, chunk.getLineEnd());
            if (pstmt.executeUpdate() == 1) {
                logger.fine("Chunk %s saved to database".formatted(chunk.getContentHash()));
            } else {
                logger.fine("Chunk %s already in database. Ignoring".formatted(chunk.getContentHash()));
            }

        } catch (SQLException e) {
            logger.warning("Exception occurred while saving chunk to db: " + e);
        }


    }

    public List<Chunk> retrieveRelevantChunks(Embedding embeddedQuestion, int limit) {
        List<Chunk> chunks = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM embeddings ORDER BY embedding <=> ?::vector LIMIT ?"
        )) {
            pstmt.setObject(1, embeddedQuestion.getValues());
            pstmt.setInt(2, limit);
            System.out.println(pstmt);

            var res = pstmt.executeQuery();
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
        } catch (SQLException e) {
            logger.warning("Exception occurred while selecting vectors from database: " + e);
        }
        return chunks;
    }


    private String mapToJsonString(Map<?, ?> map) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> jsonStringToMap(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

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
