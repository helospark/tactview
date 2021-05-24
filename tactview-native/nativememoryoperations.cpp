#include <stdio.h>
#include <string.h>
#include <iostream>

extern "C" {

    struct CopyMemoryRequest {
        unsigned char* from;
        unsigned char* to;
        int size;
    };

    void copyBuffer(CopyMemoryRequest* request) {
        memcpy(request->to, request->from, request->size);
    }
}