// Emscripten (wasm32) defines INT64_C(x) as `x##L` (type long) while int64_t is `long long`, so
// templates such as std::max({int64_t, INT64_C(1)}) fail to deduce a single type (seen in
// llama.cpp's deepseek41.cpp). Force-included into the llama target to make the literals match
// int64_t/uint64_t. Remove once upstream llama.cpp avoids mixing the two.
#pragma once
#include <stdint.h>

#undef INT64_C
#undef UINT64_C
#define INT64_C(c) c##LL
#define UINT64_C(c) c##ULL
