---
title: "Presentamos Llamatik: LLM sin conexión para Kotlin Multiplatform"
date: 2025-07-22T09:00:00-05:00
description: "Conoce Llamatik, una biblioteca multiplataforma de Kotlin para ejecutar llama.cpp en local en Android, iOS y escritorio, con servidor HTTP incluido."
image: "images/blog/a-pair-of-llamas-in-a-field-with-clouds-and-mountain.svg"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI"]
showDate: true
draft: false
---

🦙 Presentamos Llamatik: LLM sin conexión para Kotlin Multiplatform

Estamos encantados de presentar Llamatik, una biblioteca de código abierto de Kotlin Multiplatform que lleva los
grandes modelos de lenguaje (LLM) locales a Android, iOS, escritorio y más allá, gracias a la potencia de llama.cpp.

Llamatik hace que integrar inferencia y embeddings sin conexión, directamente en el dispositivo, sea sencillo y
eficiente en tus apps KMP, tanto si estás creando un asistente de IA, un chatbot con RAG o una herramienta de
inteligencia en el borde.

⸻

✨ ¿Por qué Llamatik?

Aunque el ecosistema de IA está lleno de APIs, la mayoría de los desarrolladores de Kotlin siguen atados a modelos
en la nube. Eso supone latencia, riesgos para la privacidad y costes continuos.

Creemos que el futuro de la IA es:

• 🔐 Privado: tus datos se quedan en el dispositivo.

• 📱 Multiplataforma: una sola base de código para Android, iOS, macOS, Linux y Windows.

• ⚡ Eficiente: pensado para modelos pequeños y cuantizados que funcionan rápido en hardware de consumo.

Llamatik tiende un puente entre el backend en C++ de llama.cpp y las modernas herramientas multiplataforma de
Kotlin, y te da control total sobre tus modelos y tus datos.

⸻

💡 ¿Qué puedes hacer con Llamatik?

Con Llamatik puedes:

• 🧠 Ejecutar LLM cuantizados como Phi-2, Mistral o TinyLlama completamente sin conexión.

• 🔍 Generar embeddings en local con modelos como nomic-embed-text o bge-small.

• ⚡ Lanzar tu propio servidor de inferencia HTTP con Ktor, totalmente autónomo, integrado en Llamatik y
con la tecnología de llama.cpp.

• 🌐 Conectarte a endpoints remotos de llama.cpp (como llama-cpp-python, llama-server u Ollama) con el
cliente HTTP incluido.

• 🔁 Usar la misma API en Android, iOS y otras plataformas nativas, sin código específico para cada
plataforma.

⸻

🌐 Servidor de inferencia HTTP integrado

Llamatik incluye un servidor HTTP basado en Ktor, listo para usar, que envuelve tus modelos locales de llama.cpp.
Puedes ponerlo en marcha con una sola llamada y exponer endpoints como:

POST /v1/chat/completions

POST /v1/embeddings

GET /v1/models

Esto facilita:

• 🔗 Conectar tus propias apps (u otros dispositivos) a un modelo local compartido

• 🧪 Usar con tu servidor local herramientas compatibles con OpenAI (por ejemplo, LangChain o LlamaIndex)

• ⚙️ Integrarlo en tus propios despliegues o experimentos en el borde

Sin Python. Sin Docker. Solo Kotlin + Llamatik.

⸻

📦 ¿Qué incluye?

• 🦙 Bindings nativos a llama.cpp para targets Kotlin/Native (sin JNI ni las limitaciones de depender solo de JNI).

• 🧠 Gestor de contexto multimodelo (para generación y embeddings simultáneos).

• 🛰️ Cliente y servidor HTTP opcionales.

• 🧱 Carga de modelos, construcción de prompts y ejecución eficiente en memoria, pensada para móviles.

• 🛠️ API sencilla y extensible, creada para Kotlin y no adaptada desde Python.

⸻

📢 Participa

Llamatik es de código abierto y está impulsado por la comunidad. Tanto si estás creando apps centradas en la IA
como si simplemente quieres explorar lo que es posible sin conexión, nos encantará saber de ti.

🔗 [Repositorio de GitHub](https://github.com/ferranpons/Llamatik)
