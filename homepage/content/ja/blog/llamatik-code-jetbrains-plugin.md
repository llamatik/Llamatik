---
title: "Llamatik Code のご紹介:IntelliJ と Android Studio 向け、プライバシー最優先の AI コーディングアシスタント"
date: 2026-05-14T00:00:00-05:00
description: "Llamatik Code は、IntelliJ ベースの IDE 向けのローカルファーストな AI コーディングアシスタントです。推論はすべて GGUF モデルを使ってお使いのマシン上で実行されます。クラウドも API キーも不要で、データがデバイスの外に出ることもありません。"
image: "images/blog/llamatik_code_landing_image.png"
tags: ["Llamatik Code", "IntelliJ", "Android Studio", "AI", "Privacy", "llama.cpp", "GGUF", "Local AI"]
showDate: true
draft: false
---

Llamatik は、最初の日からプライベートなオンデバイス AI のプラットフォームとして開発してきました。今日、その理念をあなたの IDE にも届けます。

**Llamatik Code** が [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31304-llamatik-code) で公開されました。IntelliJ IDEA、Android Studio、そして JetBrains IDE ファミリー全体で使える、完全ローカルでプライバシー最優先の AI コーディングアシスタントです。

クラウドも、API キーも不要。データがマシンの外に出ることもありません。IDE の中で動くのは、あなたのローカル GGUF モデルだけです。

---

## なぜ IDE にローカル AI なのか

多くの AI コーディングアシスタントは同じ仕組みで動きます。コードがリモートサーバーに送られ、クラウドのモデルが処理し、結果が返ってきます。それでも役には立ちますが、トレードオフがあります。

- リクエストのたびに、コードとコンテキストがマシンの外に出ます
- ネットワーク接続とサードパーティの稼働状況に依存します
- サブスクリプションの費用は使用量に応じて増えます
- 企業や規制の厳しい環境では、クラウド AI をそもそも使えないことが少なくありません

Llamatik Code は異なるアプローチを取ります。すべてが [llama.cpp](https://github.com/ggerganov/llama.cpp) を使ってデバイス上で動作します。Llamatik のモバイルアプリやデスクトップアプリを支えているのと同じ、実績あるランタイムです。GGUF モデルを一度読み込めば、そこから先の IDE は、外部と通信しない高機能な AI アシスタントになります。

---

## Llamatik Code でできること

### Ask Mode — ファイルを変更せず、何でもチャットで質問

質問したり、API を調べたり、解説を求めたり、スニペットを生成したりできます。Ask Mode は完全な対話型インターフェースで、複数セッションの履歴を保持し、現在のファイル、選択中のテキスト、プロジェクト構造も把握しています。

Ask Mode は**無料プランでも無制限**です。1日の上限もクレジットもありません。

### Agent Mode — コードの変更をモデルに任せる

Agent Mode はさらに一歩進んでいます。やりたいことを自然言語で伝えると、エージェントがコードの編集を提案し、差分を表示して、ファイルに手を加える前にあなたの承認を待ちます。

エージェントはプロジェクトを理解しています。ファイル構成を把握し、ファイル間を移動でき、複数のツール呼び出しをつなげて複数ステップのタスクをこなします。無料プランには、単一セッションのエージェントタスクが含まれます。Pro では、複数タスクの並列実行と **Aggressive Mode** が使えるようになります。Aggressive Mode では、エージェントが各ステップで承認を求めることなく、自律的に判断できます。

### 複数ファイルの編集(Pro)

コードの変更を複数のファイルに一度に適用できます。リファクタリングや API の移行など、複数のファイルにまたがる作業に便利です。

### MCP サーバーとの連携

Llamatik Code は [Model Context Protocol](https://modelcontextprotocol.io/) に対応しており、エージェントを外部のツールやサービスに接続できます。無料プランでは、手動でアクションを承認する形で MCP サーバーを1つ使えます。Pro では、MCP サーバーの数に制限がなくなり、自動承認も使えます。

### External Knowledge — Web 検索を内蔵

バージョン 1.5.0 から、Llamatik Code には External Knowledge の仕組みが加わりました。API キーなしで、モデルが Web を検索し、ドキュメントを読めます。

内部では DuckDuckGo を使い、開発者の意図を理解して検索先を選びます。

- Kotlin やコルーチンに関する質問 → kotlinlang.org を検索
- Android や Compose に関する質問 → developer.android.com を検索
- エラーメッセージ → GitHub Issues を検索
- ライブラリに関する質問 → パッケージレジストリと変更履歴を検索

各結果には、情報源の種類、信頼度、直接の URL といった構造化された引用が付きます。モデルは複数ステップの調査ワークフローを自動で計画し、得られた内容を整理して一貫した回答にまとめます。

無料プランでは1日5回まで検索できます(1回につき5件の結果)。Pro では、1日50回、1回につき10件の結果、最大10件のプライベートドキュメントソースまで拡張されます。

### Code Health Monitor

コード品質の問題を見つけ出す、バックグラウンド分析モードです。無料プランでは手動で実行でき、Pro では高度な自動モードが使えます。

---

## Free と Pro の比較

| 機能 | Free | Pro |
|---|---|---|
| Ask Mode(チャット) | 無制限 | 無制限 |
| エージェントセッション | 単一セッション | 複数タスク、並列 |
| Aggressive エージェントモード | — | ✓ |
| 複数ファイルへのコード適用 | — | ✓ |
| MCP サーバー | 1つ、手動承認 | 無制限、自動承認 |
| External Knowledge の検索回数/日 | 5 | 50 |
| 1回の検索あたりの結果数 | 5 | 10 |
| プライベートドキュメントソース | 1 | 10 |
| Code Health Monitor | 手動 | 高度な自動 |

Pro のサブスクリプションは [llamatik.com](https://www.llamatik.com) でご利用いただけます。

---

## はじめに

**動作要件:**
- IntelliJ IDEA 2026.1、またはプラットフォームビルド 252 以降の JetBrains IDE
- macOS、Linux、Windows(64 ビット、JDK 21 以降)
- ローカルに保存された GGUF モデルファイル

**インストール:**
1. IDE を開く → Settings → Plugins → Marketplace
2. **Llamatik Code** を検索してインストール
3. IDE を再起動

**モデルのセットアップ:**
1. Settings → Llamatik Code → Inference を開く
2. ローカルの `.gguf` ファイルを指定する
3. ハードウェアに合わせて、コンテキストサイズとスレッド数を調整する

llama.cpp 互換の GGUF モデルであれば、どれでも動作します。ほとんどの開発タスクでは、7B の Q4_K_M または Q5_K_M で量子化された instruct モデルから始めるとよいでしょう。負荷の高いエージェント作業や複雑なコードベースには、13B〜14B のモデルのほうが、推論力が目に見えて向上します。

詳しいセットアップ手順とおすすめのモデルは、[ドキュメント](https://docs.llamatik.com/llamatik-code/)をご覧ください。

---

## Llamatik エンジンの上に構築

Llamatik Code は、Llamatik のモバイルアプリやデスクトップアプリを動かしているのと同じ、Kotlin ファーストの llama.cpp 統合で動作します。つまり、スマートフォンでチャットするときも、IDE でコードを編集するときも、推論の品質、GGUF との互換性、プライバシーの保証は同じです。

外部へのネットワークアクセスが発生するのは、明示的に設定した MCP サーバーまたは External Knowledge の検索を使う場合だけです。それ以外のデータがマシンの外に出ることはありません。

---

## 今すぐインストール

Llamatik Code は、JetBrains Marketplace で今すぐ利用できます。

→ [Llamatik Code をインストール](https://plugins.jetbrains.com/plugin/31304-llamatik-code)  
→ [ドキュメントを読む](https://docs.llamatik.com/llamatik-code/)
