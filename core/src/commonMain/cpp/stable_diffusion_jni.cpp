#include <jni.h>

#include <cstdint>
#include <cstdlib>
#include <cstring>
#include <string>
#include <vector>

#include "stable-diffusion.h"

// -----------------------------------------------------------------------------
// Minimal JNI wrapper for leejet/stable-diffusion.cpp
//
// Exposes:
//  - StableDiffusionBridge.initModel(modelPath, threads)
//  - StableDiffusionBridge.txt2img(...)
//  - StableDiffusionBridge.release()
//
// We keep a single global context (matches how LlamaBridge/WhisperBridge are used).
// -----------------------------------------------------------------------------

static sd_ctx_t * g_sd_ctx = nullptr;

// Cached model paths — used to skip unnecessary free/reload when the same files are requested.
static std::string g_sd_model_path;
static std::string g_sd_vae_path;
static std::string g_sd_t5xxl_path;
static std::string g_sd_clip_vision_path;

static std::string jstring_to_utf8(JNIEnv * env, jstring s) {
    if (!s) return "";
    const char * c = env->GetStringUTFChars(s, nullptr);
    std::string out = c ? c : "";
    if (c) env->ReleaseStringUTFChars(s, c);
    return out;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_initModelNative(
        JNIEnv * env,
        jobject,
        jstring modelPath,
        jint threads,
        jstring vaePath,
        jstring t5xxlPath,
        jstring clipVisionPath
) {
    const std::string path       = jstring_to_utf8(env, modelPath);
    const std::string vae        = jstring_to_utf8(env, vaePath);
    const std::string t5xxl      = jstring_to_utf8(env, t5xxlPath);
    const std::string clipVision = jstring_to_utf8(env, clipVisionPath);
    if (path.empty()) return JNI_FALSE;

    // Skip free/reload if the exact same model files are already loaded.
    // Repeated init with the same model fragments the heap and exhausts Metal
    // memory, eventually causing ggml_new_tensor_impl to assert when T5 tries
    // to reserve its compute workspace.
    if (g_sd_ctx
            && path       == g_sd_model_path
            && vae        == g_sd_vae_path
            && t5xxl      == g_sd_t5xxl_path
            && clipVision == g_sd_clip_vision_path) {
        return JNI_TRUE;
    }

    if (g_sd_ctx) {
        free_sd_ctx(g_sd_ctx);
        g_sd_ctx = nullptr;
    }

    // DiT-only models (Wan, LTX-Video, HunyuanVideo) always come with companion files
    // (VAE, T5xxl, etc.) and must be loaded via diffusion_model_path so their tensors
    // receive the "model.diffusion_model." prefix that WanRunner/LTXRunner expects.
    // Full checkpoints (SD1.x, SDXL, SVD) have no companion files and use model_path.
    const bool has_companion_files = !vae.empty() || !t5xxl.empty() || !clipVision.empty();

    if (!has_companion_files) {
        sd_ctx_params_t params;
        sd_ctx_params_init(&params);
        params.model_path       = path.c_str();
        params.n_threads        = (int) threads;
        params.enable_mmap      = true;
        g_sd_ctx = new_sd_ctx(&params);
    }

    if (!g_sd_ctx) {
        // Load as a standalone diffusion model (DiT models like Wan, LTX-Video)
        sd_ctx_params_t params2;
        sd_ctx_params_init(&params2);
        params2.diffusion_model_path = path.c_str();
        params2.n_threads        = (int) threads;
        params2.enable_mmap      = true;
        params2.vae_path         = vae.empty()         ? nullptr : vae.c_str();
        params2.t5xxl_path       = t5xxl.empty()       ? nullptr : t5xxl.c_str();
        params2.clip_vision_path = clipVision.empty()   ? nullptr : clipVision.c_str();
        g_sd_ctx = new_sd_ctx(&params2);
    }

    if (g_sd_ctx) {
        g_sd_model_path       = path;
        g_sd_vae_path         = vae;
        g_sd_t5xxl_path       = t5xxl;
        g_sd_clip_vision_path = clipVision;
    }

    return g_sd_ctx ? JNI_TRUE : JNI_FALSE;
}

static inline void rgba_from_rgb(
        const uint8_t * rgb,
        int pixelCount,
        std::vector<uint8_t> & out
) {
    out.resize((size_t)pixelCount * 4);
    size_t si = 0;
    size_t di = 0;
    for (int i = 0; i < pixelCount; i++) {
        out[di++] = rgb[si++];
        out[di++] = rgb[si++];
        out[di++] = rgb[si++];
        out[di++] = 255;
    }
}

static inline void rgba_from_gray(
        const uint8_t * gray,
        int pixelCount,
        std::vector<uint8_t> & out
) {
    out.resize((size_t)pixelCount * 4);
    size_t si = 0;
    size_t di = 0;
    for (int i = 0; i < pixelCount; i++) {
        const uint8_t v = gray[si++];
        out[di++] = v;
        out[di++] = v;
        out[di++] = v;
        out[di++] = 255;
    }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_txt2img(
        JNIEnv * env,
        jobject,
        jstring prompt,
        jstring negativePrompt,
        jint width,
        jint height,
        jint steps,
        jfloat cfgScale,
        jlong seed
) {
    if (!g_sd_ctx) {
        return env->NewByteArray(0);
    }

    const std::string p = jstring_to_utf8(env, prompt);
    const std::string n = jstring_to_utf8(env, negativePrompt);
    if (p.empty()) {
        return env->NewByteArray(0);
    }

    sd_img_gen_params_t gen;
    sd_img_gen_params_init(&gen);

    gen.prompt = p.c_str();
    gen.negative_prompt = n.c_str();
    gen.width  = (int) width;
    gen.height = (int) height;
    gen.seed   = (int64_t) seed;

    gen.sample_params.sample_steps = (int) steps;
    gen.sample_params.guidance.txt_cfg = (float) cfgScale;

    sd_image_generation_result_t * img_result = nullptr;
    bool ok = generate_image(g_sd_ctx, &gen, &img_result);
    if (!ok || !img_result || img_result->count == 0 || !img_result->items[0].image.data ||
            img_result->items[0].image.width == 0 || img_result->items[0].image.height == 0) {
        free_sd_image_generation_result(img_result);
        return env->NewByteArray(0);
    }
    sd_image_t * img = &img_result->items[0].image;

    const int w = (int) img->width;
    const int h = (int) img->height;
    const int ch = (int) img->channel;
    const int pixelCount = w * h;

    std::vector<uint8_t> rgba;
    if (ch == 4) {
        rgba.assign(img->data, img->data + (size_t)pixelCount * 4);
    } else if (ch == 3) {
        rgba_from_rgb(img->data, pixelCount, rgba);
    } else if (ch == 1) {
        rgba_from_gray(img->data, pixelCount, rgba);
    } else {
        free_sd_image_generation_result(img_result);
        return env->NewByteArray(0);
    }

    free_sd_image_generation_result(img_result);

    jbyteArray out = env->NewByteArray((jsize) rgba.size());
    if (!out) return env->NewByteArray(0);
    env->SetByteArrayRegion(out, 0, (jsize) rgba.size(), (const jbyte*) rgba.data());
    return out;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_img2img(
        JNIEnv * env,
        jobject,
        jbyteArray initImageRgba,
        jint initImageW,
        jint initImageH,
        jstring prompt,
        jstring negativePrompt,
        jint width,
        jint height,
        jint steps,
        jfloat cfgScale,
        jfloat strength,
        jlong seed
) {
    if (!g_sd_ctx) {
        return env->NewByteArray(0);
    }

    const std::string p = jstring_to_utf8(env, prompt);
    const std::string n = jstring_to_utf8(env, negativePrompt);
    if (p.empty()) {
        return env->NewByteArray(0);
    }

    jsize initSize = env->GetArrayLength(initImageRgba);
    jbyte * initBytes = env->GetByteArrayElements(initImageRgba, nullptr);
    if (!initBytes || initSize == 0) {
        if (initBytes) env->ReleaseByteArrayElements(initImageRgba, initBytes, JNI_ABORT);
        return env->NewByteArray(0);
    }

    const int pixelCount = (int)initImageW * (int)initImageH;
    uint8_t * initCopy = (uint8_t *) std::malloc((size_t)pixelCount * 4);
    if (!initCopy) {
        env->ReleaseByteArrayElements(initImageRgba, initBytes, JNI_ABORT);
        return env->NewByteArray(0);
    }
    std::memcpy(initCopy, initBytes, (size_t)pixelCount * 4);
    env->ReleaseByteArrayElements(initImageRgba, initBytes, JNI_ABORT);

    sd_img_gen_params_t gen;
    sd_img_gen_params_init(&gen);

    gen.prompt = p.c_str();
    gen.negative_prompt = n.c_str();
    gen.width    = (int) width;
    gen.height   = (int) height;
    gen.seed     = (int64_t) seed;
    gen.strength = (float) strength;

    gen.sample_params.sample_steps = (int) steps;
    gen.sample_params.guidance.txt_cfg = (float) cfgScale;

    gen.init_image.data    = initCopy;
    gen.init_image.width   = (uint32_t) initImageW;
    gen.init_image.height  = (uint32_t) initImageH;
    gen.init_image.channel = 4;

    sd_image_generation_result_t * img_result = nullptr;
    bool ok = generate_image(g_sd_ctx, &gen, &img_result);
    std::free(initCopy);

    if (!ok || !img_result || img_result->count == 0 || !img_result->items[0].image.data ||
            img_result->items[0].image.width == 0 || img_result->items[0].image.height == 0) {
        free_sd_image_generation_result(img_result);
        return env->NewByteArray(0);
    }
    sd_image_t * img = &img_result->items[0].image;

    const int w = (int) img->width;
    const int h = (int) img->height;
    const int ch = (int) img->channel;
    const int outPixelCount = w * h;

    std::vector<uint8_t> rgba;
    if (ch == 4) {
        rgba.assign(img->data, img->data + (size_t)outPixelCount * 4);
    } else if (ch == 3) {
        rgba_from_rgb(img->data, outPixelCount, rgba);
    } else if (ch == 1) {
        rgba_from_gray(img->data, outPixelCount, rgba);
    } else {
        free_sd_image_generation_result(img_result);
        return env->NewByteArray(0);
    }

    free_sd_image_generation_result(img_result);

    jbyteArray out = env->NewByteArray((jsize) rgba.size());
    if (!out) return env->NewByteArray(0);
    env->SetByteArrayRegion(out, 0, (jsize) rgba.size(), (const jbyte*) rgba.data());
    return out;
}

extern "C" JNIEXPORT void JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_release(
        JNIEnv *,
        jobject
) {
    if (g_sd_ctx) {
        free_sd_ctx(g_sd_ctx);
        g_sd_ctx = nullptr;
    }
    g_sd_model_path.clear();
    g_sd_vae_path.clear();
    g_sd_t5xxl_path.clear();
    g_sd_clip_vision_path.clear();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_supportsVideoGeneration(
        JNIEnv *, jobject
) {
    if (!g_sd_ctx) return JNI_FALSE;
    return sd_ctx_supports_video_generation(g_sd_ctx) ? JNI_TRUE : JNI_FALSE;
}

// Returns a packed byte array: [frameCount int32 LE][width int32 LE][height int32 LE][fps int32 LE]
// followed by frameCount * (width * height * 4) bytes of RGBA data.
// Returns an empty array on failure.
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_txt2video(
        JNIEnv * env,
        jobject,
        jstring prompt,
        jstring negativePrompt,
        jint width,
        jint height,
        jint videoFrames,
        jint fps,
        jfloat cfgScale,
        jlong seed
) {
    if (!g_sd_ctx) return env->NewByteArray(0);

    const std::string p = jstring_to_utf8(env, prompt);
    const std::string n = jstring_to_utf8(env, negativePrompt);
    if (p.empty()) return env->NewByteArray(0);

    sd_vid_gen_params_t gen;
    sd_vid_gen_params_init(&gen);
    gen.prompt = p.c_str();
    gen.negative_prompt = n.c_str();
    gen.width = (int) width;
    gen.height = (int) height;
    gen.video_frames = (int) videoFrames;
    gen.fps = (int) fps;
    gen.seed = (int64_t) seed;
    gen.sample_params.guidance.txt_cfg = (float) cfgScale;

    sd_video_generation_result_t * vid_result = nullptr;
    bool ok = generate_video(g_sd_ctx, &gen, &vid_result);
    if (!ok || !vid_result || vid_result->frame_count == 0 || !vid_result->frames) {
        free_sd_video_generation_result(vid_result);
        return env->NewByteArray(0);
    }

    const int w = (int) vid_result->frames[0].width;
    const int h = (int) vid_result->frames[0].height;
    const int frameBytes = w * h * 4;
    const size_t totalSize = 16 + (size_t) vid_result->frame_count * frameBytes;

    jbyteArray out = env->NewByteArray((jsize) totalSize);
    if (!out) {
        free_sd_video_generation_result(vid_result);
        return env->NewByteArray(0);
    }

    auto write_le_i32 = [](uint8_t * buf, int32_t v) {
        buf[0] = (uint8_t)(v & 0xFF);
        buf[1] = (uint8_t)((v >> 8) & 0xFF);
        buf[2] = (uint8_t)((v >> 16) & 0xFF);
        buf[3] = (uint8_t)((v >> 24) & 0xFF);
    };
    uint8_t header[16];
    write_le_i32(header + 0,  (int32_t) vid_result->frame_count);
    write_le_i32(header + 4,  (int32_t) w);
    write_le_i32(header + 8,  (int32_t) h);
    write_le_i32(header + 12, (int32_t) vid_result->fps);
    env->SetByteArrayRegion(out, 0, 16, (const jbyte *) header);

    std::vector<uint8_t> rgba;
    for (int i = 0; i < vid_result->frame_count; i++) {
        sd_image_t * img = &vid_result->frames[i];
        const int ch = (int) img->channel;
        const int pixelCount = w * h;
        if (ch == 4) {
            rgba.assign(img->data, img->data + (size_t) pixelCount * 4);
        } else if (ch == 3) {
            rgba_from_rgb(img->data, pixelCount, rgba);
        } else if (ch == 1) {
            rgba_from_gray(img->data, pixelCount, rgba);
        } else {
            free_sd_video_generation_result(vid_result);
            return env->NewByteArray(0);
        }
        env->SetByteArrayRegion(out, (jsize)(16 + i * frameBytes), (jsize) frameBytes,
                                (const jbyte *) rgba.data());
    }

    free_sd_video_generation_result(vid_result);
    return out;
}
