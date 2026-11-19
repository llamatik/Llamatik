---
title: "Llamatik 应用发布：在你的手机上离线使用 AI 聊天"
date: 2025-11-25T09:00:00-05:00
description: "我们很高兴宣布 Llamatik 应用正式发布。这是一款完全离线的 AI 聊天机器人，旨在展示 Llamatik Kotlin Multiplatform 库的强大与灵活。无论你是对端侧 AI 感兴趣的开发者，还是想要私密、快速、现代化聊天体验的用户，Llamatik 应用都能把轻量级 LLM 的全部潜力直接带到你的手机上。"
image: "images/blog/llamatik_new_app_hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "App"]
showDate: true
draft: false
---

🚀 Llamatik 应用发布：在你的手机上离线使用 AI 聊天

我们很高兴宣布 Llamatik 应用正式发布。这是一款完全离线的 AI 聊天机器人，旨在展示 Llamatik Kotlin Multiplatform 库的强大与灵活。
无论你是对端侧 AI 感兴趣的开发者，还是想要私密、快速、现代化聊天体验的用户，Llamatik 应用都能把轻量级 LLM 的全部潜力直接带到你的手机上。

⸻

🔍 什么是 Llamatik？

Llamatik 是一个 Kotlin Multiplatform 库，让开发者可以在 Android、iOS、桌面端和 Web 平台上运行本地大语言模型（LLM）和向量嵌入。

有了 Llamatik 应用，你现在无需写一行代码，就能体验这个库的全部能力。

这款应用既是实时演示和试验场，也是开发者构建本地 AI 体验的参考实现。

⸻

⚡ Llamatik 应用能做什么

🧠 完全本地的 AI 聊天

所有文本生成都使用量化的开源模型，100% 在你的设备上运行。
没有云端，没有服务器，也不会把数据发送到任何地方。
这让 Llamatik：
•	私密
•	快速
•	随时可用
•	符合严格的隐私合规环境要求

聊天界面简洁清爽，使用 Jetpack Compose Multiplatform 构建。

⸻

📦 内置模型下载器

应用直接支持下载多个经过优化的模型，包括：
•	Gemma 3 270M
•	SmolVLM 256M / 500M（视觉 + 语言）
•	Phi-1.5
•	Qwen 2.5 5B（量化）
•	Llama 3.2 1B（量化）

所有模型均为 GGUF 格式，与 Llamatik 集成的 llama.cpp 后端兼容。

你可以直接在应用中下载、切换和管理模型。

⸻

🎯 智能建议与快捷操作

为了让体验更直观，应用提供了：
•	建议提示词
•	快捷操作
•	简洁、响应迅速的界面
•	输入框上方可选择的模型标签
•	内置的新手引导

这些小细节让应用对新手和高级用户都很友好。

⸻

🔐 隐私优先

每一次生成都在本地完成。
你的对话：
•	不会被收集
•	不会存储在外部
•	不会被记录，也不会发送到服务器

就连模型推理也是使用为移动端编译的 llama.cpp 离线完成的。

应用中唯一的遥测是 Firebase Analytics 和 Crashlytics，仅用于错误追踪和匿名的使用情况分析。

⸻

🧩 Llamatik 库功能一览

应用展示了底层库的几项关键能力：

✔ 支持 Kotlin Multiplatform

一次构建，即可运行在 Android、iOS、桌面端和 Web 上。

✔ 端侧 LLM 推理

通过自定义的 C/C++ 桥接层对接 llama.cpp。

✔ 嵌入生成

兼容 nomic-embed-text-v1.5 等模型。

✔ 向量存储支持

借助轻量级向量存储，构建本地语义搜索或类 RAG 功能。

✔ 模型管理

在多个量化模型之间下载、加载、卸载、切换并生成内容。

✔ 高层 ChatRunner 工具

轻松处理流式输出、生成和 AI 对话逻辑。

写给开发者：Llamatik 应用完全构建在开源库之上，它的代码库就是真实 KMP 本地 AI 集成的实用范例。

⸻

📱 为什么要做一款应用？

虽然 Llamatik 库本身已经足够强大，但我们还是希望：
•	开发者能看到这个库的实际运行效果
•	用户能体验本地 AI
•	社区能轻松测试各种模型
•	任何人无需任何配置就能体验离线 AI 助手
•	新手能了解跨平台 AI 应用是如何组织的

Llamatik 应用为你提供了一个完整、精致的环境，来探索本地 LLM 在手机上能做些什么。

⸻

🗺 接下来有什么？

我们已经在着手开发：
•	🔍 面向 PDF 和文档的集成向量搜索
•	📁 从文件存储中导入本地模型
•	🌐 更多模型来源和规格
•	🎛 高级聊天设置（temperature、top-k 等）
•	🧱 端侧 RAG 流水线
•	🛠 开发者示例与教程

你的反馈将帮助我们制定路线图，所以请随时分享你的想法！

⸻

🐪 立即体验 Llamatik

你现在可以在 [Google Play 下载 Llamatik 应用](https://play.google.com/store/apps/details?id=com.llamatik.app.android)，iOS App Store 版本也即将上线。

它免费、开放、可离线使用，在设计上兼顾隐私与性能。

无论你是正在构建 AI 新功能的开发者，还是想要一个快速、本地 AI 助手的用户，Llamatik 都为你而来。

👉 探索，学习，构建。
👉 体验 AI 完全运行在你设备上的无限可能。
