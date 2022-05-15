#include <stdio.h>
#include <string.h>
#include <iostream>

#include "common.h"

extern "C" {

    struct CopyMemoryRequest {
        unsigned char* from;
        unsigned char* to;
        int size;
    };

    EXPORTED void copyBuffer(CopyMemoryRequest* request) {
        memcpy(request->to, request->from, request->size);
    }

    struct ClearMemoryRequest {
        unsigned char* data;
        int size;
    };

    EXPORTED void clearBuffer(ClearMemoryRequest* request) {
        memset(request->data, 0, request->size);
    }
}