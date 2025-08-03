#include "TreeSitterJNI.h"
#include <tree_sitter/api.h>
#include <string>
#include "tree-sitter-java.h"


extern "C" {

JNIEXPORT jlong JNICALL Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_jni_TreeSitterJNI_createParser(JNIEnv* env, jobject obj) {
    TSParser *parser = ts_parser_new();
    ts_parser_set_language(parser, tree_sitter_java());
    return (jlong) parser;
}

JNIEXPORT jlong JNICALL Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_jni_TreeSitterJNI_parse(JNIEnv* env, jobject obj, jlong parserPtr, jstring code) {
    TSParser *parser = (TSParser*) parserPtr;
    const char* source = env->GetStringUTFChars(code, 0);
    TSTree *tree = ts_parser_parse_string(parser, nullptr, source, strlen(source));
    env->ReleaseStringUTFChars(code, source);
    return (jlong) tree;
}

JNIEXPORT jstring JNICALL Java_com_beatrice_rag_repositoryprocessor_textchunker_treesitter_jni_TreeSitterJNI_getRootNode(JNIEnv* env, jobject obj, jlong treePtr) {
    TSTree *tree = (TSTree*) treePtr;
    TSNode root = ts_tree_root_node(tree);
    std::string s = ts_node_type(root);
    return env->NewStringUTF(s.c_str());
}

}

