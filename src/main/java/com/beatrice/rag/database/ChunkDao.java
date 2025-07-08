package com.beatrice.rag.database;

import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
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
                "INSERT INTO embeddings (content_hash, file_path, embedding, content, metadata) VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING"
        )) {
            pstmt.setString(1, chunk.getContentHash());
            pstmt.setString(2, String.valueOf(chunk.getSource()));
            pstmt.setObject(3, chunk.getEmbedding().getValues());
            pstmt.setString(4, chunk.getText());
            pstmt.setObject(5, mapToJsonString(chunk.getMetadata()), Types.OTHER);

            pstmt.execute();
        } catch (SQLException e) {
            logger.warning("Exception occurred while saving chunk to db: " + e);
        }


    }


    private String mapToJsonString(Map<?, ?> map) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
