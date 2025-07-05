package com.beatrice.rag.fileparser;

import java.io.IOException;
import java.nio.file.Path;
interface FileParser {
    FileData parse(Path file) throws IOException;
}
