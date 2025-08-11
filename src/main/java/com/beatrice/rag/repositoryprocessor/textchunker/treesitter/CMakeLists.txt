cmake_minimum_required(VERSION 3.16)
project(treesitterjni)

set(CMAKE_CXX_STANDARD 17)

# Установи путь к JDK
find_package(Java REQUIRED)
find_package(JNI REQUIRED)

include_directories(
    ${JNI_INCLUDE_DIRS}
    vendor/tree-sitter/lib/include
    vendor/tree-sitter-java/src
    vendor/tree-sitter-java/bindings/c
    jni
)

add_library(treesitterjni SHARED
    TreeSitterJNI.cpp
    vendor/tree-sitter/lib/src/lib.c
    vendor/tree-sitter-java/src/parser.c

)

target_compile_definitions(treesitterjni PRIVATE
    TREE_SITTER_LANGUAGE=tree_sitter_java
)

target_link_libraries(treesitterjni ${JNI_LIBRARIES})
