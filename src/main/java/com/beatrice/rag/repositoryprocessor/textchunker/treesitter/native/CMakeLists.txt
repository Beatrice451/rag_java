cmake_minimum_required(VERSION 3.16)
project(TreeSitterJNI LANGUAGES C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

set(VENDOR_DIR ${CMAKE_CURRENT_SOURCE_DIR}/vendor)
set(INCLUDE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/include)

set(NATIVE_SRC
        TreeSitterJNI.cpp
)

find_package(Java REQUIRED)
find_package(JNI REQUIRED)

include_directories(
        ${JNI_INCLUDE_DIRS}
        ${VENDOR_DIR}/tree-sitter/lib/include
        ${VENDOR_DIR}/tree-sitter-java/src
        ${VENDOR_DIR}/tree-sitter-java/bindings/c
        ${VENDOR_DIR}/tree-sitter-python/src
        ${VENDOR_DIR}/tree-sitter-python/bindings/c
        ${INCLUDE_DIR}
)

add_library(treesitterjni SHARED
        TreeSitterJNI.cpp
        ${VENDOR_DIR}/tree-sitter/lib/src/lib.c
        ${VENDOR_DIR}/tree-sitter-java/src/parser.c
        ${VENDOR_DIR}/tree-sitter-python/src/parser.c
        ${VENDOR_DIR}/tree-sitter-python/src/scanner.c

)


target_link_libraries(treesitterjni ${JNI_LIBRARIES})
