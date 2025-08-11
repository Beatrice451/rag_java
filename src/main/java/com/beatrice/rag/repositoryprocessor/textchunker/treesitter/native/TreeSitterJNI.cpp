#include <tree_sitter/api.h>
#include <string>
#include "tree-sitter-java.h"
#include "tree-sitter-python.h"
#include <vector>
#include <functional>
#include <cstring>
#include "treesitter_jni.h"

extern "C" {
const TSLanguage *get_language_by_name(const char *name) {
    if (strcmp(name, "java") == 0) {
        return tree_sitter_java();
    }
    if (strcmp(name, "python") == 0) {
        return tree_sitter_python();
    }

    return nullptr;
}

JNIEXPORT jlong JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_parser_TreeSitterParser_createParser(
    JNIEnv *env, jclass /* obj */) {
    TSParser *parser = ts_parser_new();
    if (!parser)
        return 0;

    return reinterpret_cast<jlong>(parser);
}


JNIEXPORT void JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_parser_TreeSitterParser_setParserLanguage
(JNIEnv *env, jobject obj, jlong parserPtr, jstring lang) {
    const char *langNameC = env->GetStringUTFChars(lang, nullptr);

    const TSLanguage *language = get_language_by_name(langNameC);

    if (language == nullptr) {
        env->ReleaseStringUTFChars(lang, langNameC);
        jclass exClass = env->FindClass("java/lang/IllegalArgumentException");
        env->ThrowNew(exClass, "Unknown language name");
        return;
    }

    auto *parser = reinterpret_cast<TSParser *>(parserPtr);
    ts_parser_set_language(parser, language);

    env->ReleaseStringUTFChars(lang, langNameC);
}


JNIEXPORT jlong JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_parser_TreeSitterParser_parse(
    JNIEnv *env, jobject obj, jlong parserPtr, jstring code) {
    auto *parser = reinterpret_cast<TSParser *>(parserPtr);
    const char *source = env->GetStringUTFChars(code, nullptr);
    TSTree *tree = ts_parser_parse_string(parser, nullptr, source, strlen(source));
    env->ReleaseStringUTFChars(code, source);
    return reinterpret_cast<jlong>(tree);
}

JNIEXPORT jlongArray JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_wrappers_Node_getNodesOfType(
    JNIEnv *env, jobject obj, const jlong nodePtr, jstring typeStr) {
    const char *targetType = env->GetStringUTFChars(typeStr, nullptr);
    const TSNode *node = reinterpret_cast<TSNode *>(nodePtr);


    std::vector<TSNode> resultNodes;
    std::function<void(TSNode)> visit = [&](TSNode current) {
        if (strcmp(ts_node_type(current), targetType) == 0) {
            resultNodes.push_back(current);
        }
        uint32_t count = ts_node_child_count(current);
        for (uint32_t i = 0; i < count; ++i) {
            visit(ts_node_child(current, i));
        }
    };

    visit(*node);

    jlongArray result = env->NewLongArray(resultNodes.size());
    std::vector<jlong> nodePtrs;
    for (const TSNode n: resultNodes) {
        auto *copy = new TSNode(n);
        nodePtrs.push_back(reinterpret_cast<jlong>(copy));
    }

    env->SetLongArrayRegion(result, 0, nodePtrs.size(), nodePtrs.data());
    env->ReleaseStringUTFChars(typeStr, targetType);
    return result;
}

JNIEXPORT void JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_parser_TreeSitterParser_deleteParser(
    JNIEnv *env, jobject obj, jlong parserPtr) {
    auto *parser = reinterpret_cast<TSParser *>(parserPtr);
    ts_parser_delete(parser);
}

JNIEXPORT void JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_wrappers_Tree_deleteTree(
    JNIEnv *env, jobject obj, jlong treePtr) {
    auto *tree = reinterpret_cast<TSTree *>(treePtr);
    ts_tree_delete(tree);
}

JNIEXPORT jint JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_wrappers_Node_getStartByte(
    JNIEnv *env, jobject obj, jlong nodePtr) {
    auto *node = reinterpret_cast<TSNode *>(nodePtr);
    return static_cast<jint>(ts_node_start_byte(*node));
}

JNIEXPORT jint JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_wrappers_Node_getEndByte(
    JNIEnv *env, jobject obj, jlong nodePtr) {
    auto *node = reinterpret_cast<TSNode *>(nodePtr);
    return static_cast<jint>(ts_node_end_byte(*node));
}

JNIEXPORT void JNICALL Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_wrappers_Node_freeNode(
    JNIEnv *env, jobject obj, jlong nodePtr) {
    auto *node = reinterpret_cast<TSNode *>(nodePtr);
    delete node;
}

JNIEXPORT jlongArray JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_wrappers_Node_getNodeCoordinates
(JNIEnv *env, jobject obj, jlong nodePtr) {
    auto *node = reinterpret_cast<TSNode *>(nodePtr);
    TSPoint start = ts_node_start_point(*node);
    TSPoint end = ts_node_end_point(*node);

    jlongArray result = env->NewLongArray(4);
    if (result == nullptr) {
        return nullptr;
    }

    jlong coords[4] = {
        static_cast<jlong>(start.row),
        static_cast<jlong>(start.column),
        static_cast<jlong>(end.row),
        static_cast<jlong>(end.column),
    };

    env->SetLongArrayRegion(result, 0, 4, coords);
    return result;
}

JNIEXPORT jlong JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_wrappers_Node_getChildByFieldName
(JNIEnv *env, jobject obj, jlong nodePtr, jstring fieldName) {
    const char *fieldNameCStr = env->GetStringUTFChars(fieldName, nullptr);


    const TSNode *node = reinterpret_cast<TSNode *>(nodePtr);


    TSNode child = ts_node_child_by_field_name(
        *node,
        fieldNameCStr,
        static_cast<uint32_t>(strlen(fieldNameCStr))
    );


    env->ReleaseStringUTFChars(fieldName, fieldNameCStr);


    auto *childCopy = new TSNode(child);

    return reinterpret_cast<jlong>(childCopy);
}

JNIEXPORT jlong JNICALL
Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_java_wrappers_Tree_getRootNode(
    JNIEnv *env, jobject obj, jlong treePtr) {
    const auto *tree = reinterpret_cast<TSTree *>(treePtr);
    const TSNode root = ts_tree_root_node(tree);
    auto *copy = new TSNode(root);
    return reinterpret_cast<jlong>(copy);
}
}
