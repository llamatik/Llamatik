---
title: "Llamatik のご紹介:Kotlin Multiplatform 向けオフライン LLM"
date: 2025-07-22T09:00:00-05:00
description: "Llamatik は、Android、iOS、デスクトップで llama.cpp をローカル実行できる Kotlin マルチプラットフォームライブラリです。HTTP サーバーも備えています。"
image: "images/blog/a-pair-of-llamas-in-a-field-with-clouds-and-mountain.svg"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI"]
showDate: true
draft: false
---

🦙 Llamatik のご紹介:Kotlin Multiplatform 向けオフライン LLM

オープンソースの Kotlin Multiplatform ライブラリ「Llamatik」を公開しました。llama.cpp の力で、ローカルで動く
大規模言語モデル(LLM)を Android、iOS、デスクトップなどに導入できます。

Llamatik を使えば、オフラインのオンデバイス推論と埋め込み(embeddings)を KMP アプリに手軽かつ効率的に組み込めます。
AI アシスタントでも、RAG チャットボットでも、エッジインテリジェンスのツールでも使えます。

⸻

✨ なぜ Llamatik なのか

AI のエコシステムには API が豊富にそろっていますが、多くの Kotlin 開発者は今もクラウドベースのモデルに
縛られています。その結果、レイテンシ、プライバシーのリスク、継続的なコストが生じます。

私たちは、AI の未来は次のようなものだと考えています。

• 🔐 プライベート:データはデバイスの外に出ません。

• 📱 マルチプラットフォーム:Android、iOS、macOS、Linux、Windows を1つのコードベースで。

• ⚡ 高パフォーマンス:量子化された小型モデルを、一般的なハードウェアで高速に動かせるように設計しています。

Llamatik は、llama.cpp の C++ バックエンドと Kotlin の最新のマルチプラットフォームツールをつなぎ、
モデルとデータを完全に自分の手でコントロールできるようにします。

⸻

💡 何ができるのか

Llamatik では、次のことができます。

• 🧠 Phi-2、Mistral、TinyLlama などの量子化 LLM を、完全にオフラインで実行できます。

• 🔍 nomic-embed-text や bge-small などのモデルで、埋め込みをローカルで生成できます。

• ⚡ Ktor を使った独自の HTTP 推論サーバーを起動できます。Llamatik に組み込まれていて完結しており、
llama.cpp で動作します。

• 🌐 付属の HTTP クライアントで、リモートの llama.cpp エンドポイント(llama-cpp-python、llama-server、Ollama など)に
接続できます。

• 🔁 Android、iOS、その他のネイティブプラットフォームで同じ API を使えます。プラットフォーム固有のコードは
不要です。

⸻

🌐 組み込みの HTTP 推論サーバー

Llamatik には、ローカルの llama.cpp モデルをラップする、すぐに使える Ktor ベースの HTTP サーバーが
含まれています。1回の呼び出しで起動でき、次のようなエンドポイントを公開できます。

POST /v1/chat/completions

POST /v1/embeddings

GET /v1/models

これにより、次のことが簡単になります。

• 🔗 自分のアプリ(や他のデバイス)を、共有されたローカルモデルに接続する

• 🧪 OpenAI 互換のツール(LangChain、LlamaIndex など)をローカルサーバーで使う

• ⚙️ 独自のエッジデプロイや実験に組み込む

Python は不要です。Docker も不要です。必要なのは Kotlin と Llamatik だけです。

⸻

📦 中身は?

• 🦙 Kotlin/Native ターゲット向けの llama.cpp ネイティブバインディング(JNI や JNI のみの制約はありません)。

• 🧠 複数モデルのコンテキストマネージャー(生成と埋め込みの同時実行用)。

• 🛰️ オプションの HTTP クライアントとサーバー。

• 🧱 モバイル向けに調整された、モデルの読み込み、プロンプト構築、メモリ効率のよい実行。

• 🛠️ シンプルで拡張しやすい API。Python からの移植ではなく、Kotlin のために作られています。

⸻

📢 参加しよう

Llamatik はオープンソースで、コミュニティ主導のプロジェクトです。AI ファーストのアプリを開発している方も、
オフラインで何ができるのかを試してみたい方も、ぜひご意見をお聞かせください。

🔗 [GitHub リポジトリ](https://github.com/ferranpons/Llamatik)
