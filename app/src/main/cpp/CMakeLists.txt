cmake_minimum_required(VERSION 3.22.1)
project(wildhorizon)

add_library(wildhorizon SHARED native_engine.cpp)

find_library(log-lib log)
find_library(gles-lib GLESv3)

target_link_libraries(wildhorizon ${gles-lib} ${log-lib})
