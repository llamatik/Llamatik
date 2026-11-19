---
title: "如何使用 Kotlin 在 Android 上离线运行大模型"
date: 2026-02-11T00:00:00-05:00
description: "本文介绍如何使用 Kotlin 在 Android 上离线运行大模型：无需服务器，无需 API 密钥，充分保护用户隐私。我们将借助 Llamatik，一个以 Kotlin 优先的 llama.cpp 封装库。"
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

运行大语言模型（LLM）通常意味着把用户数据发送到云端。但在现代 Android 设备上，LLM 完全可以使用 Kotlin 在本地离线运行。
本文介绍如何使用 Kotlin 在 Android 上离线运行 LLM：无需服务器，无需 API 密钥，充分保护用户隐私。我们将借助 Llamatik，一个以 Kotlin 优先的 llama.cpp 封装库。

## 为什么要在 Android 上离线运行 LLM？

基于云端的 LLM 有一些取舍：
- ❌ 依赖网络
- ❌ 延迟
- ❌ 按用量计费
- ❌ 用户数据离开设备

离线 LLM 则可以带来：
- 📴 离线优先的应用
- 🔐 保护隐私的 AI
- 📱 可预期的性能与成本

现代 Android 手机的性能已经足够强大，缺少的只是好用的 Kotlin 工具。

## llama.cpp：端侧 LLM 的基础

llama.cpp 是一个高性能的 C++ 运行时，专为在 CPU 上高效运行 LLM 而设计。

它支持：
- 量化的 GGUF 模型
- ARM CPU（Android）
- 不依赖 GPU 或云端

但是，把 C++ 直接集成到 Android 应用中非常麻烦。
这正是 Llamatik 的用武之地。

## 什么是 Llamatik？

Llamatik 是一个 Kotlin 优先的库，让 Android 开发者能够通过简洁的 Kotlin API 在本地运行 llama.cpp。

主要特性：
- 完全离线推理
- 应用代码中无需 JNI
- 支持 Kotlin Multiplatform
- 兼容 GGUF 模型
- 支持流式输出与嵌入

你只需编写 Kotlin，原生层的复杂性都被隐藏起来了。

## 将 Llamatik 添加到你的 Android 项目

Llamatik 已发布到 Maven Central。

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

无需自定义 Gradle 配置。
无需配置 NDK。

## 添加 GGUF 模型

下载一个量化的 GGUF 模型（例如 Q4），并放到：
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

较小的模型加载更快，在移动设备上表现也更好。

## 加载模型

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

这会把模型从 assets 中复制出来，并加载到原生内存中。

## 离线生成文本

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

就这么简单。
无需联网。
无需 API 密钥。
无需调用云端。

## 流式生成（推荐）

适用于聊天类界面：
```
LlamaBridge.generateStreamWithContext(
 system = "You are a concise assistant.",
 context = "",
 user = "List three benefits of offline LLMs.",
 onDelta = { token -> /* update UI */ },
 onDone = { },
 onError = { error -> }
)
```

它可以与以下组件无缝配合：
- Jetpack Compose
- StateFlow
- ViewModel

## 嵌入与离线 RAG

Llamatik 同样支持嵌入，可用于离线搜索和 RAG 场景。

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

把嵌入存储在本地，就能构建完全离线的 AI 功能。

## 性能说明

端侧 LLM 有其局限：
- 使用量化模型
- 响应速度会比云端 GPU 慢
- 需要谨慎管理内存

但对于：
- 辅助类功能
- 简短的提示词
- 特定领域的任务

这样的性能已经绰绰有余。

## 什么时候该使用离线 LLM？

在以下情况下，Llamatik 是理想之选：
- 需要离线支持
- 对隐私有严格保障要求
- 需要可预期的成本
- 需要与界面紧密集成

它不能替代大型云端模型，而是把边缘 AI 做到位。

## 动手试试
- GitHub：[https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- 网站与演示应用：[https://llamatik.com](https://llamatik.com)
- llama.cpp：[https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## 写在最后
使用 Kotlin 在 Android 上离线运行 LLM，已不再是实验性的尝试。
有了合适的工具，Kotlin 开发者无需接触 C++，就能构建私密、离线、运行在设备端的 AI。
如果你对云端之外的移动端 AI 感兴趣，这里是个不错的起点。


本文最初发布于 [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) 和 [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g)。
