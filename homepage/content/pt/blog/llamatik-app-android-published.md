---
title: "Apresentamos o app Llamatik: um chat de IA offline no seu dispositivo móvel"
date: 2025-11-25T09:00:00-05:00
description: "Temos o prazer de anunciar o lançamento do app Llamatik, um chatbot de IA totalmente offline criado para mostrar o poder e a versatilidade da biblioteca Llamatik Kotlin Multiplatform. Seja você um desenvolvedor curioso sobre IA no dispositivo ou alguém que quer uma experiência de chatbot privada, rápida e moderna, o app Llamatik traz todo o potencial dos LLMs leves direto para o seu dispositivo móvel."
image: "images/blog/llamatik_new_app_hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "App"]
showDate: true
draft: false
---

🚀 Apresentamos o app Llamatik: chat de IA offline no seu dispositivo móvel

Temos o prazer de anunciar o lançamento do app Llamatik, um chatbot de IA totalmente offline criado para mostrar o poder e a versatilidade da biblioteca Llamatik Kotlin Multiplatform.
Seja você um desenvolvedor curioso sobre IA no dispositivo ou alguém que quer uma experiência de chatbot privada, rápida e moderna, o app Llamatik traz todo o potencial dos LLMs leves direto para o seu dispositivo móvel.

⸻

🔍 O que é o Llamatik?

O Llamatik é uma biblioteca Kotlin Multiplatform que permite aos desenvolvedores rodar Large Language Models (LLMs) locais e embeddings vetoriais em plataformas Android, iOS, Desktop e Web.

Com o lançamento do app Llamatik, agora você pode experimentar tudo o que a biblioteca faz — sem escrever uma única linha de código.

O app funciona como demo ao vivo, ambiente de testes e implementação de referência para desenvolvedores que criam experiências de IA local.

⸻

⚡ O que o app Llamatik faz

🧠 Chat de IA totalmente local

Toda a geração de texto roda 100% no seu dispositivo, usando modelos de código aberto quantizados.
Não há nuvem, não há servidor e nenhum dado é enviado para lugar nenhum.
Isso torna o Llamatik:
•	Privado
•	Rápido
•	Sempre disponível
•	Compatível com ambientes de privacidade rigorosa

A interface de chat é simples, limpa e criada com Jetpack Compose Multiplatform.

⸻

📦 Download de modelos integrado

O app inclui download direto de vários modelos otimizados, entre eles:
•	Gemma 3 270M
•	SmolVLM 256M / 500M (visão + linguagem)
•	Phi-1.5
•	Qwen 2.5 5B (quantizado)
•	Llama 3.2 1B (quantizado)

Todos os modelos vêm no formato GGUF e são compatíveis com o backend llama.cpp integrado ao Llamatik.

Você pode baixar, alternar e gerenciar os modelos diretamente no app.

⸻

🎯 Sugestões inteligentes e ações rápidas

Para tornar a experiência mais intuitiva, o app oferece:
•	Prompts sugeridos
•	Ações rápidas
•	Interface limpa e responsiva
•	Um seletor de modelo em formato de pílula acima da caixa de texto
•	Onboarding integrado

Esses pequenos detalhes tornam o app ótimo tanto para iniciantes quanto para usuários avançados.

⸻

🔐 Privacidade em primeiro lugar

Toda geração acontece localmente.
Suas conversas:
•	Não são coletadas
•	Não são armazenadas externamente
•	Não são registradas em logs nem enviadas a um servidor

Até a inferência do modelo é feita offline, usando o llama.cpp compilado para dispositivos móveis.

A única telemetria do app é o Firebase Analytics e o Crashlytics, usados estritamente para rastreamento de erros e métricas de uso anônimas.

⸻

🧩 Uma vitrine dos recursos da biblioteca Llamatik

O app demonstra várias das principais capacidades da biblioteca por trás dele:

✔ Suporte a Kotlin Multiplatform

Crie uma vez e rode em Android, iOS, Desktop e Web.

✔ Inferência de LLM no dispositivo

Por meio de uma ponte C/C++ personalizada para o llama.cpp.

✔ Geração de embeddings

Compatível com modelos como o nomic-embed-text-v1.5.

✔ Suporte a vector store

Crie busca semântica local ou recursos no estilo RAG com vector stores leves.

✔ Gerenciamento de modelos

Baixe, carregue, descarregue, alterne e gere texto com vários modelos quantizados.

✔ Utilitários ChatRunner de alto nível

Facilitam o streaming, a geração e o tratamento da lógica de conversa com a IA.

Para os desenvolvedores que estão lendo: o app Llamatik é construído inteiramente sobre a biblioteca de código aberto — e o seu código é um exemplo prático de integração de IA local com KMP no mundo real.

⸻

📱 Por que um app?

Embora a biblioteca Llamatik já seja poderosa, queríamos oferecer um jeito para que:
•	Desenvolvedores vejam a biblioteca em ação
•	Usuários experimentem a IA local
•	A comunidade teste modelos com facilidade
•	Qualquer pessoa experimente assistentes de IA offline sem configuração
•	Quem está começando entenda como um app de IA multiplataforma é estruturado

O app Llamatik oferece um ambiente completo e polido para explorar o que os LLMs locais podem fazer em um celular.

⸻

🗺 O que vem por aí?

Já estamos trabalhando em:
•	🔍 Busca vetorial integrada para PDFs e documentos
•	📁 Importação local de modelos a partir do armazenamento de arquivos
•	🌐 Mais provedores e tamanhos de modelos
•	🎛 Configurações avançadas de chat (temperature, top-k etc.)
•	🧱 Pipelines de RAG no dispositivo
•	🛠 Exemplos e tutoriais para desenvolvedores

O seu feedback vai ajudar a moldar o roadmap — então compartilhe o que você pensa!

⸻

🐪 Experimente o Llamatik hoje

Agora você já pode baixar o [app Llamatik no Google Play](https://play.google.com/store/apps/details?id=com.llamatik.app.android) e, em breve, na App Store do iOS.

É gratuito, aberto, offline e criado com foco em privacidade e desempenho.

Seja você um desenvolvedor criando novos recursos com IA ou alguém que quer um assistente de IA local e rápido — o Llamatik está aqui para você.

👉 Explore. Aprenda. Crie.
👉 Descubra o que é possível quando a IA roda inteiramente no seu dispositivo.
