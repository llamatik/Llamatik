#include "ggml.h"

#if defined(__GNUC__) || defined(__clang__)
#include "ggml-impl.h"
#endif

// Fallback for ggml_mul_mat_i8_tensorwise (SD-specific INT8 tensorwise matmul).
// Delegates to ggml_mul_mat, ignoring INT8-specific arguments.
// Standard FP16/FP32 SD models work correctly; INT8-quantized models do not.
struct ggml_tensor * ggml_mul_mat_i8_tensorwise(
        struct ggml_context * ctx,
        struct ggml_tensor  * weight,
        struct ggml_tensor  * input,
        struct ggml_tensor  * weight_scale,
        struct ggml_tensor  * bias,
        int                   convrot_group_size)
{
    (void)weight_scale;
    (void)bias;
    (void)convrot_group_size;
    return ggml_mul_mat(ctx, weight, input);
}

// Fallback for ggml_quantize_i8_convrot (packs I8 activations with convrot).
// Returns a contiguous view; convrot quantisation is a no-op.
struct ggml_tensor * ggml_quantize_i8_convrot(
        struct ggml_context * ctx,
        struct ggml_tensor  * a,
        int                   group_size)
{
    (void)group_size;
    return ggml_cont(ctx, a);
}

#if defined(__GNUC__) || defined(__clang__)
// ggml_graph_view() is an internal ggml function (declared in ggml-impl.h, not GGML_API).
// stable-diffusion.cpp's ggml_compat.cpp (ggml_backend_graph_compute_async_range) calls it.
//
// Static builds (macOS / iOS) link ggml's own definition. On Android ggml is built as shared
// libraries (libggml-base.so) that do not export it, so the link fails with
// "undefined symbol: ggml_graph_view". This weak copy (identical to ggml.c) fills that gap:
// where ggml provides the strong symbol it wins, otherwise this one is used.
extern "C" __attribute__((weak))
struct ggml_cgraph ggml_graph_view(struct ggml_cgraph * cgraph0, int i0, int i1) {
    struct ggml_cgraph cgraph = {};
    cgraph.size             = 0;
    cgraph.n_nodes          = i1 - i0;
    cgraph.n_leafs          = 0;
    cgraph.nodes            = cgraph0->nodes + i0;
    cgraph.grads            = nullptr; // gradients would need visited_hash_set
    cgraph.grad_accs        = nullptr;
    cgraph.leafs            = nullptr;
    cgraph.use_counts       = cgraph0->use_counts;
    cgraph.visited_hash_set = cgraph0->visited_hash_set;
    cgraph.order            = cgraph0->order;
    cgraph.uid              = 0;
    return cgraph;
}
#endif
