---
title: "Llamatik Code 发布：面向 IntelliJ 和 Android Studio、隐私优先的 AI 编程助手"
date: 2026-05-14T00:00:00-05:00
description: "Llamatik Code 是面向 IntelliJ 系列 IDE、本地优先的 AI 编程助手。所有推理都使用 GGUF 模型在你的机器上运行：无需云端，无需 API 密钥，数据不会离开你的设备。"
image: "images/blog/llamatik_code_landing_image.png"
tags: ["Llamatik Code", "IntelliJ", "Android Studio", "AI", "Privacy", "llama.cpp", "GGUF", "Local AI"]
showDate: true
draft: false
---

从第一天起，我们就在把 Llamatik 打造成一个私密的端侧 AI 平台。今天，我们把同样的理念带进你的 IDE。

**Llamatik Code** 现已登陆 [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31304-llamatik-code)，这是一款完全本地、隐私优先的 AI 编程助手，适用于 IntelliJ IDEA、Android Studio 以及整个 JetBrains IDE 家族。

没有云端，没有 API 密钥，数据不会离开你的机器。只有你的本地 GGUF 模型，在你的 IDE 里运行。

---

## 为什么要在 IDE 中使用本地 AI？

大多数 AI 编程助手的工作方式都一样：你的代码被发送到远程服务器，由云端模型处理，再把结果返回。这当然可行，但也有取舍：

- 每次请求，你的代码和上下文都会离开你的机器
- 你依赖网络连接和第三方服务的可用性
- 订阅费用随用量增长
- 企业和受监管的环境往往完全无法使用云端 AI

Llamatik Code 采用了不同的思路。一切都使用 [llama.cpp](https://github.com/ggerganov/llama.cpp) 在设备上运行，它与驱动 Llamatik 移动端和桌面端应用的是同一个久经考验的运行时。你只需加载一次 GGUF 模型，从此你的 IDE 就拥有了一个功能完备、绝不会“回传数据”的 AI 助手。

---

## Llamatik Code 能做什么

### Ask Mode：随意聊，不修改文件

你可以提问、探索 API、获取解释、生成代码片段。Ask Mode 是完整的对话界面，支持持久化的多会话历史，并能感知你当前的文件、选中的文本和项目结构。

Ask Mode 在免费版中**不限次数**，没有每日上限，也没有额度限制。

### Agent Mode：让模型来修改代码

Agent Mode 更进一步。你用自然语言描述想要的结果，智能体就会提出代码修改建议、展示 diff，并在改动任何文件之前等待你的批准。

智能体具备项目感知能力：它理解你的文件结构，可以跨文件导航，并串联多次工具调用来完成多步骤任务。免费版包含单会话的智能体任务。Pro 版解锁并行多任务执行和 **Aggressive Mode**，在该模式下，智能体可以自主决策，无需在每一步都征求批准。

### 多文件编辑（Pro）

一次性对多个文件应用代码修改。适用于重构、API 迁移，以及任何涉及多个文件的改动。

### MCP 服务器集成

Llamatik Code 支持 [Model Context Protocol](https://modelcontextprotocol.io/)，让你可以把智能体连接到外部工具和服务。免费版支持一个 MCP 服务器，并需手动批准操作。Pro 版解锁不限数量的 MCP 服务器和自动批准。

### 外部知识：内置网页搜索

自 1.5.0 版本起，Llamatik Code 加入了外部知识系统，让模型无需任何 API 密钥即可搜索网页并阅读文档。

它在底层使用 DuckDuckGo，并能理解开发者的意图：

- 关于 Kotlin 或协程的问题 → 搜索 kotlinlang.org
- 关于 Android 或 Compose 的问题 → 搜索 developer.android.com
- 错误信息 → 搜索 GitHub Issues
- 关于库的问题 → 搜索包注册表和更新日志

每条结果都附带结构化的引用，包括来源类型、置信度和直接链接。模型会自动规划多步骤的调研流程，并把发现综合成连贯的答案。

免费版每天可搜索 5 次（每次 5 条结果）。Pro 版提高到每天 50 次搜索，每次 10 条结果，并支持最多 10 个私有文档来源。

### 代码健康监测

这是一种后台分析模式，用来发现代码质量问题。免费用户可以手动运行；Pro 版解锁高级自动化模式。

---

## 免费版与 Pro 版

| 功能 | 免费版 | Pro 版 |
|---|---|---|
| Ask Mode（聊天） | 不限次数 | 不限次数 |
| 智能体会话 | 单会话 | 多任务、并行 |
| Aggressive 智能体模式 | — | ✓ |
| 多文件代码应用 | — | ✓ |
| MCP 服务器 | 1 个，手动批准 | 不限数量，自动批准 |
| 每日外部知识搜索次数 | 5 | 50 |
| 每次搜索的结果数 | 5 | 10 |
| 私有文档来源 | 1 | 10 |
| 代码健康监测 | 手动 | 高级自动化 |

Pro 订阅可在 [llamatik.com](https://www.llamatik.com) 获取。

---

## 快速上手

**环境要求：**
- IntelliJ IDEA 2026.1，或任何平台构建号为 252+ 的 JetBrains IDE
- macOS、Linux 或 Windows（64 位，JDK 21+）
- 一个存放在本地的 GGUF 模型文件

**安装：**
1. 打开你的 IDE → Settings → Plugins → Marketplace
2. 搜索 **Llamatik Code** 并安装
3. 重启 IDE

**模型设置：**
1. 前往 Settings → Llamatik Code → Inference
2. 指向你本地的 `.gguf` 文件
3. 根据你的硬件调整上下文大小和线程数

任何兼容 llama.cpp 的 GGUF 模型都可以使用。对于大多数开发任务，7B 的 Q4_K_M 或 Q5_K_M 量化指令模型是不错的起点。对于更繁重的智能体工作或复杂的代码库，13B 到 14B 的模型在推理能力上会有明显提升。

完整的设置说明和模型推荐请见[文档](https://docs.llamatik.com/llamatik-code/)。

---

## 基于 Llamatik 引擎构建

Llamatik Code 由同一套以 Kotlin 优先的 llama.cpp 集成驱动，这套集成同样驱动着 Llamatik 的移动端和桌面端应用。这意味着无论你是在手机上聊天，还是在 IDE 里编写代码，推理质量、GGUF 兼容性和隐私保障都是一样的。

只有通过你明确配置的 MCP 服务器或外部知识搜索，才会发生对外的网络访问。其他任何内容都不会离开你的机器。

---

## 今天就安装

Llamatik Code 现已在 JetBrains Marketplace 上架。

→ [安装 Llamatik Code](https://plugins.jetbrains.com/plugin/31304-llamatik-code)  
→ [阅读文档](https://docs.llamatik.com/llamatik-code/)
