---
title: "Llamatik 简介：面向 Kotlin Multiplatform 的离线大模型"
date: 2025-07-22T09:00:00-05:00
description: "认识 Llamatik：一个可在 Android、iOS 和桌面端本地运行 llama.cpp 的多平台 Kotlin 库，并自带 HTTP 服务器。"
image: "images/blog/a-pair-of-llamas-in-a-field-with-clouds-and-mountain.svg"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI"]
showDate: true
draft: false
---

🦙 Llamatik 简介：面向 Kotlin Multiplatform 的离线大模型

我们很高兴地推出 Llamatik，这是一个开源的 Kotlin Multiplatform 库，借助 llama.cpp 的强大能力，
把本地大语言模型（LLM）带到 Android、iOS、桌面端等平台。

Llamatik 让你可以简单高效地把离线、端侧的推理和嵌入集成到
KMP 应用中，无论你要构建的是 AI 助手、RAG 聊天机器人，还是边缘智能工具。

⸻

✨ 为什么选择 Llamatik？

AI 生态中的 API 虽然丰富，但大多数 Kotlin 开发者仍然离不开基于云端的
模型，这意味着延迟、隐私风险和持续的费用。

我们相信，AI 的未来应该是：

• 🔐 私密：你的数据留在设备上。

• 📱 多平台：一套代码库，覆盖 Android、iOS、macOS、Linux 和 Windows。

• ⚡ 高性能：专为小型量化模型打造，在消费级硬件上也能快速运行。

Llamatik 在 llama.cpp 的 C++ 后端与 Kotlin 现代化的多平台工具链之间架起桥梁，
让你完全掌控自己的模型和数据。

⸻

💡 你可以用它做什么？

使用 Llamatik，你可以：

• 🧠 完全离线运行 Phi-2、Mistral 或 TinyLlama 等量化 LLM。

• 🔍 在本地使用 nomic-embed-text 或 bge-small 等模型生成嵌入。

• ⚡ 使用 Ktor 启动你自己的 HTTP 推理服务器，完全自包含，内置于 Llamatik，
由 llama.cpp 驱动。

• 🌐 通过内置的 HTTP 客户端连接远程 llama.cpp 端点（如 llama-cpp-python、llama-server 或 Ollama）。

• 🔁 在 Android、iOS 和其他原生平台上使用同一套 API，无需任何平台专属代码。

⸻

🌐 内置 HTTP 推理服务器

Llamatik 自带一个开箱即用、基于 Ktor 的 HTTP 服务器，用来封装你的本地 llama.cpp 模型。
只需一次调用即可启动，并提供如下端点：

POST /v1/chat/completions

POST /v1/embeddings

GET /v1/models

这让你可以轻松地：

• 🔗 把你自己的应用（或其他设备）连接到共享的本地模型

• 🧪 在本地服务器上使用兼容 OpenAI 的工具（例如 LangChain、LlamaIndex）

• ⚙️ 集成到你自己的边缘部署或实验中

无需 Python，无需 Docker，只要 Kotlin + Llamatik。

⸻

📦 里面有什么？

• 🦙 面向 Kotlin/Native 目标的 llama.cpp 原生绑定（没有 JNI，也不受 JNI 的限制）。

• 🧠 多模型上下文管理器（可同时进行生成与嵌入）。

• 🛰️ 可选的 HTTP 客户端和服务器。

• 🧱 为移动端量身打造的模型加载、提示词构建和内存高效的执行方式。

• 🛠️ 简洁、可扩展的 API，为 Kotlin 而生，而非从 Python 改造而来。

⸻

📢 参与其中

Llamatik 是开源项目，由社区驱动。无论你是在构建 AI 优先的应用，还是只想
探索离线能做些什么，我们都期待听到你的声音。

🔗 [GitHub 仓库](https://github.com/ferranpons/Llamatik)
