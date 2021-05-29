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
}