package com.beatrice.rag.repositoryprocessor.fileparser;

import com.beatrice.rag.exception.ParserException;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.beatrice.rag.Config.ALLOWED_MIME_TYPES;

public class TikaFileParser extends AbstractFileParser {
    @Override
    public FileData parse(Path file) throws IOException, ParserException {
        String fileMime = detectMime(file);
        if (!ALLOWED_MIME_TYPES.contains(fileMime)) {
            throw new ParserException("Unallowed MIME type: %s for file %s".formatted(fileMime, file));
        }
        String content = readFile(file);
        Map<String, String> metadata = extractMetadata(file, content);


        return new FileData(content, metadata);
    }

    private static String readFile(Path file) throws IOException, ParserException {
        try (BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();

            parser.parse(stream, handler, metadata);
            return handler.toString();
        } catch (TikaException | SAXException e) {
            throw new ParserException(e);
        }

    }

    private static String detectMime(Path file) throws IOException {
        DefaultDetector detector = new DefaultDetector();
        Metadata metadata = new Metadata();

        try (InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
            MediaType mediaType = detector.detect(stream, metadata);
            return mediaType.toString();
        }
    }
}
