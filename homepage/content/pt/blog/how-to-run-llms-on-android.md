---
title: "Como rodar LLMs offline no Android usando Kotlin"
date: 2026-02-11T00:00:00-05:00
description: "Este artigo mostra como rodar LLMs offline no Android usando Kotlin, sem servidores, sem chaves de API e com total privacidade para o usuário, com o Llamatik, um wrapper do llama.cpp pensado para Kotlin."
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

Rodar Large Language Models (LLMs) normalmente significa enviar os dados do usuário para a nuvem. Mas nos dispositivos Android modernos, os LLMs podem rodar totalmente offline, de forma local, usando Kotlin.
Este artigo mostra como rodar LLMs offline no Android usando Kotlin, sem servidores, sem chaves de API e com total privacidade para o usuário, com o Llamatik, um wrapper do llama.cpp pensado para Kotlin.

## Por que rodar LLMs offline no Android?

Os LLMs na nuvem têm seus contrapontos:
- ❌ Dependência de rede
- ❌ Latência
- ❌ Custo baseado em uso
- ❌ Dados do usuário saindo do dispositivo

Os LLMs offline permitem:
- 📴 Apps offline-first
- 🔐 IA que preserva a privacidade
- 📱 Desempenho e custo previsíveis

Os celulares Android modernos são potentes o bastante. O que faltava era um bom ferramental Kotlin.

## llama.cpp: a base dos LLMs no dispositivo

O llama.cpp é um runtime C++ de alto desempenho criado para rodar LLMs de forma eficiente em CPUs.

Ele oferece suporte a:
- Modelos GGUF quantizados
- CPUs ARM (Android)
- Nenhuma dependência de GPU ou nuvem

Mas integrar C++ diretamente em apps Android é trabalhoso.
É aí que entra o Llamatik.

## O que é o Llamatik?

O Llamatik é uma biblioteca pensada primeiro para Kotlin que permite aos desenvolvedores Android rodar o llama.cpp localmente por meio de uma API Kotlin limpa.

Principais recursos:
- Inferência totalmente offline
- Sem JNI no código do app
- Suporte a Kotlin Multiplatform
- Compatibilidade com modelos GGUF
- Suporte a streaming e embeddings

Você escreve apenas Kotlin, e a complexidade nativa fica escondida.

## Adicione o Llamatik ao seu projeto Android

O Llamatik é publicado no Maven Central.

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

Sem configuração personalizada do Gradle.
Sem configuração do NDK.

## Adicione um modelo GGUF

Baixe um modelo GGUF quantizado (por exemplo, Q4) e coloque-o em:
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

Modelos menores carregam mais rápido e funcionam melhor em dispositivos móveis.

## Carregue o modelo

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

Isso copia o modelo dos assets e o carrega na memória nativa.

## Gere texto offline

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

Só isso.
Sem internet.
Sem chaves de API.
Sem chamadas à nuvem.

## Geração em streaming (recomendado)

Para interfaces no estilo chat:
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

Isso funciona perfeitamente com:
- Jetpack Compose
- StateFlow
- ViewModels

## Embeddings e RAG offline

O Llamatik também oferece suporte a embeddings, o que habilita busca offline e casos de uso de RAG.

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

Armazene os embeddings localmente para criar recursos de IA totalmente offline.

## Observações sobre desempenho

Os LLMs no dispositivo têm limites:
- Use modelos quantizados
- Espere respostas mais lentas do que em GPUs na nuvem
- Gerencie a memória com cuidado

Mas para:
- Recursos de assistência
- Prompts curtos
- Tarefas de domínio específico

O desempenho é mais do que suficiente.

## Quando usar LLMs offline?

O Llamatik é ideal quando você precisa de:
- Suporte offline
- Fortes garantias de privacidade
- Custos previsíveis
- Integração estreita com a interface

Ele não substitui os grandes modelos na nuvem: é IA na borda feita do jeito certo.

## Experimente você mesmo
- GitHub: [https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- Site e app de demonstração: [https://llamatik.com](https://llamatik.com)
- llama.cpp: [https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## Considerações finais
Rodar LLMs offline no Android usando Kotlin não é mais algo experimental.
Com as ferramentas certas, desenvolvedores Kotlin podem criar IA privada, offline e no dispositivo, sem encostar em C++.
Se você tem curiosidade sobre IA móvel além da nuvem, este é um ótimo ponto de partida.


Publicado originalmente no [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) e no [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g).
