package com.beatrice.rag.textchunker.treesitter;

import com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.parsers.Language;
import com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.parsers.TreeSitterParser;
import com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.wrappers.Node;
import com.beatrice.rag.repositoryprocessor.textchunker.treesitter.java.wrappers.Tree;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TreeSitterParserTest {
    private static final String testSourceCode;

    static {
        try {
            testSourceCode = Files.readString(Path.of("src/test/java/com/beatrice/rag/textchunker/treesitter/TestCode.java"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TreeSitterParser parser;

    @BeforeEach
    public void setup() {
        parser = new TreeSitterParser();
    }

    @AfterEach
    public void cleanup() {
        if (parser != null) {
            parser.close();
        }
    }

    @Test
    public void createParserTest() {
        assertNotNull(parser);
    }

    @Test
    public void parseTest() {
        Tree tree = parser.parse(testSourceCode, Language.JAVA);
        assertNotNull(tree);
        tree.close();

    }

    @Test
    public void testTreeGetNodesOfType() {
        Tree tree = parser.parse(testSourceCode, Language.JAVA);
        List<Node> nodes = tree.getNodesOfType("method_declaration");
        assertFalse(nodes.isEmpty());
        for (Node node : nodes) {
            assertTrue(node.getContent().contains("public"));
            assertTrue(node.getContent().contains("test"));
        }
        tree.close();
    }


}
