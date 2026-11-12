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

// ---- generation progress (stable-diffusion.cpp sd_set_generation_progress_callback) ----
//
// The callback is invoked synchronously on the thread running the generation, which is a JVM
// thread, so GetEnv() succeeds. The listener is a global ref to a Kotlin object exposing
// onNative(String?, String?, String?, int, long, long).
static JavaVM *   g_sd_jvm = nullptr;
static jobject    g_sd_progress_listener = nullptr;
static jmethodID  g_sd_progress_mid = nullptr;

static jstring new_jstring_or_null(JNIEnv * env, const char * s) {
    return s ? env->NewStringUTF(s) : nullptr;
}

static void sd_progress_trampoline(const sd_generation_progress_t * p, void * /*data*/) {
    if (!p || !g_sd_jvm || !g_sd_progress_listener || !g_sd_progress_mid) return;
    JNIEnv * env = nullptr;
    if (g_sd_jvm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || !env) return;

    jstring stage  = new_jstring_or_null(env, p->stage);
    jstring detail = new_jstring_or_null(env, p->detail);
    jstring unit   = new_jstring_or_null(env, p->unit);
    env->CallVoidMethod(g_sd_progress_listener, g_sd_progress_mid,
                        stage, detail, unit,
                        (jint) p->sample_pass, (jlong) p->completed, (jlong) p->total);
    if (env->ExceptionCheck()) env->ExceptionClear();
    if (stage)  env->DeleteLocalRef(stage);
    if (detail) env->DeleteLocalRef(detail);
    if (unit)   env->DeleteLocalRef(unit);
}

extern "C" JNIEXPORT void JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_setProgressCallbackNative(
        JNIEnv * env, jobject, jobject listener) {
    if (!g_sd_jvm) env->GetJavaVM(&g_sd_jvm);
    if (g_sd_progress_listener) {
        env->DeleteGlobalRef(g_sd_progress_listener);
        g_sd_progress_listener = nullptr;
        g_sd_progress_mid = nullptr;
    }
    if (listener) {
        jclass cls = env->GetObjectClass(listener);
        jmethodID mid = env->GetMethodID(cls, "onNative",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IJJ)V");
        env->DeleteLocalRef(cls);
        if (!mid) { env->ExceptionClear(); return; }
        g_sd_progress_mid = mid;
        g_sd_progress_listener = env->NewGlobalRef(listener);
    }
    // The context-level callback itself is registered once per context in initModelNative;
    // the trampoline is a no-op while no listener is set.
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
        sd_set_generation_progress_callback(g_sd_ctx, sd_progress_trampoline, nullptr);
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


// -----------------------------------------------------------------------------
// ControlNet
// -----------------------------------------------------------------------------

extern "C" JNIEXPORT jboolean JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_loadControlNet(
        JNIEnv * env, jobject, jstring path) {
    if (!g_sd_ctx) return JNI_FALSE;
    const std::string p = jstring_to_utf8(env, path);
    if (p.empty()) return JNI_FALSE;
    return sd_ctx_load_control_net(g_sd_ctx, p.c_str()) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_unloadControlNet(
        JNIEnv *, jobject) {
    if (!g_sd_ctx) return JNI_FALSE;
    return sd_ctx_unload_control_net(g_sd_ctx) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_hasControlNet(
        JNIEnv *, jobject) {
    if (!g_sd_ctx) return JNI_FALSE;
    return sd_ctx_has_control_net(g_sd_ctx) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_llamatik_core_platform_StableDiffusionBridge_generateWithControlNative(
        JNIEnv * env,
        jobject,
        jbyteArray controlImageRgba,
        jint controlW,
        jint controlH,
        jint controlType,
        jstring prompt,
        jstring negativePrompt,
        jint width,
        jint height,
        jint steps,
        jfloat cfgScale,
        jfloat controlStrength,
        jlong seed
) {
    if (!g_sd_ctx || !sd_ctx_has_control_net(g_sd_ctx)) return env->NewByteArray(0);
    if (controlType < 0 || controlType >= (jint) SD_CONTROL_TYPE_COUNT) return env->NewByteArray(0);

    const std::string p = jstring_to_utf8(env, prompt);
    const std::string n = jstring_to_utf8(env, negativePrompt);
    if (p.empty() || controlW <= 0 || controlH <= 0) return env->NewByteArray(0);

    const size_t expected = (size_t) controlW * (size_t) controlH * 4;
    if ((size_t) env->GetArrayLength(controlImageRgba) < expected) return env->NewByteArray(0);

    std::vector<uint8_t> control(expected);
    env->GetByteArrayRegion(controlImageRgba, 0, (jsize) expected, (jbyte *) control.data());

    sd_img_gen_params_t gen;
    sd_img_gen_params_init(&gen);
    gen.prompt = p.c_str();
    gen.negative_prompt = n.c_str();
    gen.width  = (int) width;
    gen.height = (int) height;
    gen.seed   = (int64_t) seed;
    gen.sample_params.sample_steps = (int) steps;
    gen.sample_params.guidance.txt_cfg = (float) cfgScale;

    gen.control_image.data    = control.data();
    gen.control_image.width   = (uint32_t) controlW;
    gen.control_image.height  = (uint32_t) controlH;
    gen.control_image.channel = 4;
    gen.control_strength      = (float) controlStrength;
    gen.control_type          = (enum sd_control_type_t) controlType;

    sd_image_generation_result_t * img_result = nullptr;
    const bool ok = generate_image(g_sd_ctx, &gen, &img_result);
    if (!ok || !img_result || img_result->count == 0 || !img_result->items[0].image.data ||
            img_result->items[0].image.width == 0 || img_result->items[0].image.height == 0) {
        free_sd_image_generation_result(img_result);
        return env->NewByteArray(0);
    }
    sd_image_t * img = &img_result->items[0].image;
    const int pixelCount = (int) img->width * (int) img->height;

    std::vector<uint8_t> rgba;
    if (img->channel == 4) {
        rgba.assign(img->data, img->data + (size_t) pixelCount * 4);
    } else if (img->channel == 3) {
        rgba_from_rgb(img->data, pixelCount, rgba);
    } else if (img->channel == 1) {
        rgba_from_gray(img->data, pixelCount, rgba);
    } else {
        free_sd_image_generation_result(img_result);
        return env->NewByteArray(0);
    }
    free_sd_image_generation_result(img_result);

    jbyteArray out = env->NewByteArray((jsize) rgba.size());
    if (!out) return env->NewByteArray(0);
    env->SetByteArrayRegion(out, 0, (jsize) rgba.size(), (const jbyte *) rgba.data());
    return out;
}

// -----------------------------------------------------------------------------
// Video upscaler (VideoUpscalerBridge)
// -----------------------------------------------------------------------------

static video_upscaler_ctx_t * g_vu_ctx = nullptr;

static int32_t read_le_i32(const uint8_t * b) {
    return (int32_t) ((uint32_t) b[0] | ((uint32_t) b[1] << 8) | ((uint32_t) b[2] << 16) | ((uint32_t) b[3] << 24));
}

static void write_le_i32_vu(uint8_t * b, int32_t v) {
    b[0] = (uint8_t) (v & 0xFF);
    b[1] = (uint8_t) ((v >> 8) & 0xFF);
    b[2] = (uint8_t) ((v >> 16) & 0xFF);
    b[3] = (uint8_t) ((v >> 24) & 0xFF);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_llamatik_core_platform_VideoUpscalerBridge_initModelNative(
        JNIEnv * env, jobject, jstring diffusionModelPath, jstring vaePath, jint upscaleFactor, jint threads) {
    const std::string model = jstring_to_utf8(env, diffusionModelPath);
    const std::string vae   = jstring_to_utf8(env, vaePath);
    if (model.empty() || vae.empty()) return JNI_FALSE;

    if (g_vu_ctx) {
        free_video_upscaler_ctx(g_vu_ctx);
        g_vu_ctx = nullptr;
    }

    sd_video_upscaler_ctx_params_t params;
    sd_video_upscaler_ctx_params_init(&params);
    params.diffusion_model_path = model.c_str();
    params.vae_path             = vae.c_str();
    params.upscale_factor       = (int) upscaleFactor;
    if (threads > 0) params.n_threads = (int) threads;

    g_vu_ctx = new_video_upscaler_ctx(&params);
    return g_vu_ctx ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_llamatik_core_platform_VideoUpscalerBridge_isLoaded(JNIEnv *, jobject) {
    return g_vu_ctx ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_llamatik_core_platform_VideoUpscalerBridge_release(JNIEnv *, jobject) {
    if (g_vu_ctx) {
        free_video_upscaler_ctx(g_vu_ctx);
        g_vu_ctx = nullptr;
    }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_llamatik_core_platform_VideoUpscalerBridge_upscale(
        JNIEnv * env, jobject, jbyteArray packedFrames, jlong seed, jint windowSize, jint temporalChunkSize) {
    if (!g_vu_ctx || !packedFrames) return env->NewByteArray(0);

    const jsize total = env->GetArrayLength(packedFrames);
    if (total < 16) return env->NewByteArray(0);

    uint8_t header[16];
    env->GetByteArrayRegion(packedFrames, 0, 16, (jbyte *) header);
    const int32_t frameCount = read_le_i32(header + 0);
    const int32_t w          = read_le_i32(header + 4);
    const int32_t h          = read_le_i32(header + 8);
    const int32_t fps        = read_le_i32(header + 12);
    if (frameCount <= 0 || w <= 0 || h <= 0) return env->NewByteArray(0);

    const size_t inFrameBytes = (size_t) w * (size_t) h * 4;
    if ((size_t) total < 16 + (size_t) frameCount * inFrameBytes) return env->NewByteArray(0);

    // The upscaler takes RGB frames.
    std::vector<std::vector<uint8_t>> rgbFrames((size_t) frameCount);
    std::vector<sd_image_t> frames((size_t) frameCount);
    std::vector<uint8_t> rgba(inFrameBytes);
    for (int32_t i = 0; i < frameCount; i++) {
        env->GetByteArrayRegion(packedFrames, (jsize) (16 + (size_t) i * inFrameBytes),
                                (jsize) inFrameBytes, (jbyte *) rgba.data());
        auto & rgb = rgbFrames[(size_t) i];
        rgb.resize((size_t) w * (size_t) h * 3);
        for (size_t px = 0, n = (size_t) w * (size_t) h; px < n; px++) {
            rgb[px * 3 + 0] = rgba[px * 4 + 0];
            rgb[px * 3 + 1] = rgba[px * 4 + 1];
            rgb[px * 3 + 2] = rgba[px * 4 + 2];
        }
        frames[(size_t) i].width   = (uint32_t) w;
        frames[(size_t) i].height  = (uint32_t) h;
        frames[(size_t) i].channel = 3;
        frames[(size_t) i].data    = rgb.data();
    }

    sd_video_upscale_params_t params;
    sd_video_upscale_params_init(&params);
    params.frames      = frames.data();
    params.frame_count = (int) frameCount;
    params.fps         = (int) fps;
    if (seed >= 0) params.seed = (int64_t) seed;
    if (windowSize > 0)        params.window_size         = (int) windowSize;
    if (temporalChunkSize > 0) params.temporal_chunk_size = (int) temporalChunkSize;

    sd_video_generation_result_t * result = nullptr;
    const bool ok = upscale_video(g_vu_ctx, &params, &result);
    if (!ok || !result || result->frame_count <= 0 || !result->frames) {
        free_sd_video_generation_result(result);
        return env->NewByteArray(0);
    }

    const int outW = (int) result->frames[0].width;
    const int outH = (int) result->frames[0].height;
    const size_t outFrameBytes = (size_t) outW * (size_t) outH * 4;
    const size_t outTotal = 16 + (size_t) result->frame_count * outFrameBytes;

    jbyteArray out = env->NewByteArray((jsize) outTotal);
    if (!out) {
        free_sd_video_generation_result(result);
        return env->NewByteArray(0);
    }

    uint8_t outHeader[16];
    write_le_i32_vu(outHeader + 0,  (int32_t) result->frame_count);
    write_le_i32_vu(outHeader + 4,  (int32_t) outW);
    write_le_i32_vu(outHeader + 8,  (int32_t) outH);
    write_le_i32_vu(outHeader + 12, (int32_t) (result->fps > 0 ? result->fps : fps));
    env->SetByteArrayRegion(out, 0, 16, (const jbyte *) outHeader);

    std::vector<uint8_t> outRgba;
    for (int i = 0; i < result->frame_count; i++) {
        sd_image_t * img = &result->frames[i];
        const int px = outW * outH;
        if ((int) img->width != outW || (int) img->height != outH) {
            free_sd_video_generation_result(result);
            return env->NewByteArray(0);
        }
        if (img->channel == 4) {
            outRgba.assign(img->data, img->data + (size_t) px * 4);
        } else if (img->channel == 3) {
            rgba_from_rgb(img->data, px, outRgba);
        } else if (img->channel == 1) {
            rgba_from_gray(img->data, px, outRgba);
        } else {
            free_sd_video_generation_result(result);
            return env->NewByteArray(0);
        }
        env->SetByteArrayRegion(out, (jsize) (16 + (size_t) i * outFrameBytes), (jsize) outFrameBytes,
                                (const jbyte *) outRgba.data());
    }

    free_sd_video_generation_result(result);
    return out;
}
