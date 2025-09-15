package com.beatrice.rag.repositoryprocessor.fileparser;

import java.util.Map;

public record FileData(String content, Map<String, String> metadata) {

}
