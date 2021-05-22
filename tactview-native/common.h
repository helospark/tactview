#include "logging.h"

#ifdef _WIN32
# define EXPORTED  __declspec( dllexport )
#else
# define EXPORTED
#endif

#ifndef COMMON_H
#define COMMON_H

  struct NativePair {
    const char* key;
    const char* value;
  };

  struct NativeMap {
    int size;
    NativePair* data;
  };

#endif
