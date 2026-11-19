---
title: "Presentiamo Llamatik: LLM offline per Kotlin Multiplatform"
date: 2025-07-22T09:00:00-05:00
description: "Scopri Llamatik, una libreria Kotlin multipiattaforma per eseguire llama.cpp in locale su Android, iOS e desktop, con tanto di server HTTP."
image: "images/blog/a-pair-of-llamas-in-a-field-with-clouds-and-mountain.svg"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI"]
showDate: true
draft: false
---

🦙 Presentiamo Llamatik: LLM offline per Kotlin Multiplatform

Siamo entusiasti di presentare Llamatik, una libreria open source Kotlin Multiplatform che porta i Large Language Model (LLM) locali su Android, iOS, desktop e non solo, grazie alla potenza di llama.cpp.

Llamatik rende semplice ed efficiente integrare l'inferenza e gli embedding offline, direttamente sul dispositivo, nelle tue app KMP, che tu stia creando un assistente IA, un chatbot RAG o uno strumento di edge intelligence.

⸻

✨ Perché Llamatik?

Nonostante l'ecosistema dell'IA sia ricco di API, la maggior parte degli sviluppatori Kotlin dipende ancora da modelli nel cloud. Il che significa latenza, rischi per la privacy e costi continui.

Crediamo che il futuro dell'IA sia:

• 🔐 Privato: i tuoi dati restano sul dispositivo.

• 📱 Multipiattaforma: un'unica codebase per Android, iOS, macOS, Linux e Windows.

• ⚡ Performante: pensato per modelli piccoli e quantizzati che girano veloci su hardware consumer.

Llamatik colma il divario tra il backend C++ di llama.cpp e gli strumenti multipiattaforma moderni di Kotlin, dandoti il pieno controllo sui tuoi modelli e sui tuoi dati.

⸻

💡 Cosa puoi farci?

Con Llamatik puoi:

• 🧠 Eseguire LLM quantizzati come Phi-2, Mistral o TinyLlama completamente offline.

• 🔍 Generare embedding in locale con modelli come nomic-embed-text o bge-small.

• ⚡ Avviare il tuo server di inferenza HTTP con Ktor, completamente autonomo, integrato in Llamatik e basato su llama.cpp.

• 🌐 Collegarti a endpoint llama.cpp remoti (come llama-cpp-python, llama-server o Ollama) con il client HTTP incluso.

• 🔁 Usare la stessa API su Android, iOS e altre piattaforme native, senza codice specifico per piattaforma.

⸻

🌐 Server di inferenza HTTP integrato

Llamatik include un server HTTP basato su Ktor, pronto all'uso, che avvolge i tuoi modelli llama.cpp locali. Puoi avviarlo con una sola chiamata ed esporre endpoint come:

POST /v1/chat/completions

POST /v1/embeddings

GET /v1/models

Questo rende semplice:

• 🔗 Collegare le tue app (o altri dispositivi) a un modello locale condiviso

• 🧪 Usare strumenti compatibili con OpenAI (ad es. LangChain, LlamaIndex) con il tuo server locale

• ⚙️ Integrare il tutto nei tuoi deployment o esperimenti edge

Niente Python. Niente Docker. Solo Kotlin + Llamatik.

⸻

📦 Cosa c'è dentro?

• 🦙 Binding nativi a llama.cpp per i target Kotlin/Native (senza JNI né i limiti del solo JNI).

• 🧠 Gestore di contesto multi-modello (per generazione ed embedding in contemporanea).

• 🛰️ Client e server HTTP opzionali.

• 🧱 Caricamento dei modelli, costruzione dei prompt ed esecuzione efficiente in termini di memoria, su misura per il mobile.

• 🛠️ API semplice ed estensibile, pensata per Kotlin e non adattata da Python.

⸻

📢 Partecipa

Llamatik è open source e guidato dalla community. Che tu stia creando app IA-first o semplicemente esplorando cosa si può fare offline, ci farebbe piacere sentire la tua opinione.

🔗 [Repository GitHub](https://github.com/ferranpons/Llamatik)