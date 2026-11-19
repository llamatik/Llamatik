---
title: "Kotlin で Android 上の LLM をオフライン実行する方法"
date: 2026-02-11T00:00:00-05:00
description: "サーバーも API キーも不要で、ユーザーのプライバシーを守りながら、Kotlin で Android 上の LLM をオフライン実行する方法を紹介します。llama.cpp を Kotlin ファーストでラップした Llamatik を使います。"
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

大規模言語モデル(LLM)を動かすには、ユーザーのデータをクラウドへ送るのが一般的です。しかし最近の Android デバイスなら、Kotlin を使って LLM を完全にオフライン、ローカルで実行できます。
この記事では、サーバーも API キーも不要で、ユーザーのプライバシーを守りながら、Kotlin で Android 上の LLM をオフライン実行する方法を紹介します。使うのは、llama.cpp を Kotlin ファーストでラップした Llamatik です。

## なぜ Android で LLM をオフライン実行するのか

クラウドベースの LLM にはトレードオフがあります。
- ❌ ネットワークへの依存
- ❌ レイテンシ
- ❌ 従量課金のコスト
- ❌ ユーザーデータがデバイスの外に出ること

オフライン LLM なら、次のことが可能になります。
- 📴 オフラインファーストのアプリ
- 🔐 プライバシーを守る AI
- 📱 予測しやすいパフォーマンスとコスト

最近の Android スマートフォンは十分に高性能です。足りなかったのは、使いやすい Kotlin のツールでした。

## llama.cpp:オンデバイス LLM の土台

llama.cpp は、CPU 上で LLM を効率よく動かすために作られた、高性能な C++ ランタイムです。

次のものに対応しています。
- 量子化された GGUF モデル
- ARM CPU(Android)
- GPU やクラウドに依存しない動作

ただ、C++ を Android アプリに直接組み込むのは大変です。
そこで Llamatik の出番です。

## Llamatik とは

Llamatik は Kotlin ファーストのライブラリです。Android 開発者は、すっきりした Kotlin API で llama.cpp をローカル実行できます。

主な特長は次のとおりです。
- 完全オフラインでの推論
- アプリのコードに JNI は不要
- Kotlin Multiplatform に対応
- GGUF モデルとの互換性
- ストリーミングと埋め込み(embeddings)に対応

書くのは Kotlin だけで、ネイティブの複雑さは表に出ません。

## Android プロジェクトに Llamatik を追加する

Llamatik は Maven Central で公開されています。

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

Gradle の特別な設定は不要です。
NDK の設定も不要です。

## GGUF モデルを追加する

量子化された GGUF モデル(Q4 など)をダウンロードし、次の場所に置きます。
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

小さいモデルほど読み込みが速く、モバイルデバイスでもよく動作します。

## モデルを読み込む

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

これでアセットからモデルをコピーし、ネイティブメモリに読み込みます。

## オフラインでテキストを生成する

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

これだけです。
インターネットは不要。
API キーも不要。
クラウド呼び出しもありません。

## ストリーミング生成(推奨)

チャット形式の UI 向けです。
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

次のものとシームレスに連携します。
- Jetpack Compose
- StateFlow
- ViewModel

## 埋め込みとオフライン RAG

Llamatik は埋め込み(embeddings)にも対応しており、オフライン検索や RAG のユースケースを実現できます。

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

埋め込みをローカルに保存すれば、完全オフラインの AI 機能を構築できます。

## パフォーマンスについて

オンデバイスの LLM には限界があります。
- 量子化されたモデルを使う
- クラウドの GPU に比べて応答は遅くなる
- メモリを慎重に管理する

それでも、次のような用途なら:
- アシスト機能
- 短いプロンプト
- 特定ドメインのタスク

パフォーマンスは十分すぎるほどです。

## オフライン LLM を使うのはどんなとき?

Llamatik は、次のようなニーズに最適です。
- オフライン対応
- 強力なプライバシーの保証
- 予測しやすいコスト
- UI との緊密な統合

大規模なクラウドモデルの代わりではなく、エッジ AI をあるべき形で実現するものです。

## 実際に試してみよう
- GitHub: [https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- Web サイトとデモアプリ: [https://llamatik.com](https://llamatik.com)
- llama.cpp: [https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## おわりに
Kotlin で Android 上の LLM をオフライン実行することは、もはや実験的なものではありません。
適切なツールがあれば、Kotlin 開発者は C++ に触れることなく、プライベートでオフラインのオンデバイス AI を構築できます。
クラウドの先にあるモバイル AI に興味があるなら、ここが始めどころです。


この記事は [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) と [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g) で先に公開したものです。
