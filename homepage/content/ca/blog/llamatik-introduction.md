---
title: "Presentem Llamatik: LLM sense connexió per a Kotlin Multiplatform"
date: 2025-07-22T09:00:00-05:00
description: "Descobreix Llamatik, una llibreria Kotlin multiplataforma per executar llama.cpp en local a Android, iOS i escriptori, amb servidor HTTP inclòs."
image: "images/blog/a-pair-of-llamas-in-a-field-with-clouds-and-mountain.svg"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI"]
showDate: true
draft: false
---

🦙 Presentem Llamatik: LLM sense connexió per a Kotlin Multiplatform

Estem encantats de presentar Llamatik, una llibreria de codi obert de Kotlin Multiplatform que porta
els grans models de llenguatge (LLM) locals a Android, iOS, escriptori i més enllà gràcies a la
potència de llama.cpp.

Llamatik fa que integrar inferència i embeddings sense connexió, directament al dispositiu, a les
teves apps KMP sigui senzill i eficient, tant si estàs creant un assistent d'IA, un xatbot RAG o una
eina d'intel·ligència a la vora (edge).

⸻

✨ Per què Llamatik?

Tot i que l'ecosistema de la IA és ric en APIs, la majoria de desenvolupadors Kotlin continuen
lligats a models al núvol. Això vol dir latència, riscos per a la privacitat i costos recurrents.

Creiem que el futur de la IA és:

• 🔐 Privat: les teves dades es queden al dispositiu.

• 📱 Multiplataforma: una sola base de codi per a Android, iOS, macOS, Linux i Windows.

• ⚡ Ràpid: pensat per a models petits i quantitzats que van àgils en maquinari de consum.

Llamatik uneix el backend en C++ de llama.cpp amb les modernes eines multiplataforma de Kotlin,
i et dona un control total sobre els teus models i les teves dades.

⸻

💡 Què hi pots fer?

Amb Llamatik pots:

• 🧠 Executar LLM quantitzats com Phi-2, Mistral o TinyLlama completament sense connexió.

• 🔍 Generar embeddings en local amb models com nomic-embed-text o bge-small.

• ⚡ Llançar el teu propi servidor d'inferència HTTP amb Ktor, totalment autònom, integrat a Llamatik
i impulsat per llama.cpp.

• 🌐 Connectar-te a endpoints remots de llama.cpp (com llama-cpp-python, llama-server o Ollama) amb
el client HTTP inclòs.

• 🔁 Usar la mateixa API a Android, iOS i altres plataformes natives, sense codi específic de
plataforma.

⸻

🌐 Servidor d'inferència HTTP integrat

Llamatik inclou un servidor HTTP basat en Ktor, llest per usar, que encapsula els teus models locals
de llama.cpp. Pots engegar-lo amb una sola crida i exposar endpoints com:

POST /v1/chat/completions

POST /v1/embeddings

GET /v1/models

Això facilita:

• 🔗 Connectar les teves pròpies apps (o altres dispositius) a un model local compartit

• 🧪 Usar eines compatibles amb OpenAI (p. ex. LangChain, LlamaIndex) amb el teu servidor local

• ⚙️ Integrar-ho en els teus propis desplegaments o experiments a la vora

Sense Python. Sense Docker. Només Kotlin + Llamatik.

⸻

📦 Què hi ha a dins?

• 🦙 Bindings natius a llama.cpp per als targets de Kotlin/Native (sense les limitacions de JNI o de
només JNI).

• 🧠 Gestor de contextos multimodel (per a generació i embeddings simultanis).

• 🛰️ Client i servidor HTTP opcionals.

• 🧱 Càrrega de models, construcció de prompts i execució eficient en memòria pensada per a mòbil.

• 🛠️ API senzilla i extensible, feta per a Kotlin i no adaptada des de Python.

⸻

📢 Participa-hi

Llamatik és de codi obert i està impulsat per la comunitat. Tant si estàs creant apps pensades per a
la IA com si simplement vols explorar què és possible sense connexió, ens encantarà saber de tu.

🔗 [Repositori de GitHub](https://github.com/ferranpons/Llamatik)
