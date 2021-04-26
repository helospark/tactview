#include "common.h"
#include <stdlib.h>
#include <iostream>

const int ALIGNMENT = 16;

extern "C"
{
    EXPORTED void* allocateDirect(int size)  {
        if (size % ALIGNMENT != 0) {
            size = size + ALIGNMENT - (size % ALIGNMENT);
        }
        void* pointer = aligned_alloc(ALIGNMENT, size);

        std::cout << "Alignment: " << (long)pointer << " " << size << std::endl;

        return pointer;
    }

    EXPORTED void free(void* data) {
        free(data);
    }

}

