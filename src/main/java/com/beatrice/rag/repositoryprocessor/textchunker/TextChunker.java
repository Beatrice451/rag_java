package com.beatrice.rag.repositoryprocessor.textchunker;

import com.beatrice.rag.repositoryprocessor.fileparser.FileData;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface TextChunker {
    List<Chunk> chunk(FileData fileData);

    // Method to hash chunk content using md5 algorithm
    default String hash(Chunk chunk) {
        byte[] hashedText;
        String textToHash = chunk.getText();
        byte[] textBytes = textToHash.getBytes(StandardCharsets.UTF_8);
        try {
            hashedText = MessageDigest.getInstance("MD5").digest(textBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : hashedText) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
