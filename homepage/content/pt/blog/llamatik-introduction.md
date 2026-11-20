---
title: "Apresentamos o Llamatik: LLMs offline para Kotlin Multiplatform"
date: 2025-07-22T09:00:00-05:00
description: "Conheça o Llamatik — uma biblioteca Kotlin multiplataforma para rodar o llama.cpp localmente no Android, iOS e desktop, com servidor HTTP incluso."
image: "images/blog/a-pair-of-llamas-in-a-field-with-clouds-and-mountain.svg"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI"]
showDate: true
draft: false
---

🦙 Apresentamos o Llamatik: LLMs offline para Kotlin Multiplatform

Estamos muito felizes em apresentar o Llamatik — uma biblioteca Kotlin Multiplatform de código aberto
que leva Large Language Models (LLMs) locais para Android, iOS, desktop e além, usando o poder do
llama.cpp.

O Llamatik torna simples e eficiente integrar inferência e embeddings offline, direto no dispositivo,
aos seus apps KMP, esteja você construindo um assistente de IA, um chatbot com RAG ou uma
ferramenta de inteligência na borda.

⸻

✨ Por que o Llamatik?

Embora o ecossistema de IA seja rico em APIs, a maioria dos desenvolvedores Kotlin ainda depende de
modelos na nuvem. Isso significa latência, riscos à privacidade e custos recorrentes.

Acreditamos que o futuro da IA é:

• 🔐 Privado: seus dados ficam no dispositivo.

• 📱 Multiplataforma: uma única base de código para Android, iOS, macOS, Linux e Windows.

• ⚡ Performático: feito para modelos pequenos e quantizados que rodam rápido em hardware comum.

O Llamatik faz a ponte entre o backend em C++ do llama.cpp e as ferramentas multiplataforma modernas
do Kotlin — dando a você controle total sobre seus modelos e seus dados.

⸻

💡 O que dá para fazer com ele?

Com o Llamatik, você pode:

• 🧠 Rodar LLMs quantizados como Phi-2, Mistral ou TinyLlama totalmente offline.

• 🔍 Gerar embeddings localmente com modelos como nomic-embed-text ou bge-small.

• ⚡ Subir o seu próprio servidor de inferência HTTP com Ktor — totalmente autônomo, embutido no
Llamatik e movido pelo llama.cpp.

• 🌐 Conectar-se a endpoints remotos do llama.cpp (como llama-cpp-python, llama-server ou Ollama) com
o cliente HTTP incluso.

• 🔁 Usar a mesma API no Android, no iOS e em outras plataformas nativas — sem código específico de
plataforma.

⸻

🌐 Servidor de inferência HTTP integrado

O Llamatik inclui um servidor HTTP pronto para uso, baseado em Ktor, que encapsula os seus modelos
locais do llama.cpp. Você o inicia com uma única chamada e expõe endpoints como:

POST /v1/chat/completions

POST /v1/embeddings

GET /v1/models

Isso facilita:

• 🔗 Conectar seus próprios apps (ou outros dispositivos) a um modelo local compartilhado

• 🧪 Usar ferramentas compatíveis com a OpenAI (como LangChain e LlamaIndex) com o seu servidor local

• ⚙️ Integrar a seus próprios deployments de borda ou experimentos

Sem Python. Sem Docker. Só Kotlin + Llamatik.

⸻

📦 O que vem dentro?

• 🦙 Bindings nativos para o llama.cpp em targets Kotlin/Native (sem as limitações de JNI).

• 🧠 Gerenciador de contexto multimodelo (para geração e embeddings simultâneos).

• 🛰️ Cliente e servidor HTTP opcionais.

• 🧱 Carregamento de modelos, montagem de prompts e execução eficiente em memória, pensados para
dispositivos móveis.

• 🛠️ API simples e extensível — feita para Kotlin, não adaptada de Python.

⸻

📢 Participe

O Llamatik é de código aberto e movido pela comunidade. Seja você alguém construindo apps com IA em
primeiro lugar ou apenas explorando o que é possível offline, adoraríamos ouvir você.

🔗 [Repositório no GitHub](https://github.com/ferranpons/Llamatik)
