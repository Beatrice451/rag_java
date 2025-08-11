package com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.chunkers;

import com.beatrice.rag.repositoryprocessor.fileparser.FileData;
import com.beatrice.rag.repositoryprocessor.textchunker.Chunk;
import com.beatrice.rag.repositoryprocessor.textchunker.TextChunker;
import com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.parser.Language;
import com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.parser.TreeSitterParser;
import com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.wrappers.Node;
import com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.wrappers.Tree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaTSChunker implements TextChunker, AutoCloseable {
    private final TreeSitterParser parser = new TreeSitterParser();


    @Override
    public List<Chunk> chunk(FileData fileData) {
        String content = fileData.getContent();
        Map<String, String> metadata = fileData.getMetadata();
        List<Chunk> chunks = new ArrayList<>();

        Tree tree = parser.parse(content, Language.JAVA);
        List<Node> classNodes = tree.getNodesOfType("class_declaration");
        for (Node classNode : classNodes) {
            List<Node> methodNodes = classNode.getNodesOfType("method_declaration");
            String className = classNode.getChildByFieldName("name").getContent();
            for (Node methodNode : methodNodes) {
                String methodName = methodNode.getChildByFieldName("name").getContent();
                Chunk chunk = new Chunk();

                chunk.setText(methodNode.getContent());
                long[] nodeCoords = methodNode.getNodeCoordinates();
                int lineStart = (int) nodeCoords[0] + 1;
                int lineEnd = (int) nodeCoords[2] + 1;
                Path source = Path.of(metadata.get("path"));
                String codeSource = "%s#%s".formatted(className, methodName);

                chunk.setLineStart(lineStart);
                chunk.setLineEnd(lineEnd);
                chunk.setSource(source);
                chunk.setMetadata(metadata);
                chunk.setContentHash(hash(chunk));
                chunk.setCodeSource(codeSource);
                chunks.add(chunk);
            }
        }


        return chunks;
    }

    @Override
    public void close() {
        parser.close();
    }
}
