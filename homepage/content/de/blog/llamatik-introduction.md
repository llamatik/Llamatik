---
title: "Llamatik vorgestellt: Offline-LLMs für Kotlin Multiplatform"
date: 2025-07-22T09:00:00-05:00
description: "Lerne Llamatik kennen – eine Kotlin-Multiplatform-Bibliothek, mit der du llama.cpp lokal auf Android, iOS und Desktop ausführst, inklusive HTTP-Server."
image: "images/blog/a-pair-of-llamas-in-a-field-with-clouds-and-mountain.svg"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI"]
showDate: true
draft: false
---

🦙 Llamatik vorgestellt: Offline-LLMs für Kotlin Multiplatform

Wir freuen uns riesig, Llamatik vorzustellen – eine Open-Source-Bibliothek für Kotlin Multiplatform, die dank llama.cpp lokale
Large Language Models (LLMs) auf Android, iOS, Desktop und darüber hinaus bringt.

Llamatik macht es einfach und effizient, Offline-Inferenz und Embeddings direkt auf dem Gerät in
deine KMP-Apps zu integrieren – egal, ob du einen KI-Assistenten, einen RAG-Chatbot oder ein Edge-Intelligence-Tool baust.

⸻

✨ Warum Llamatik?

Das KI-Ökosystem bietet zwar jede Menge APIs, doch die meisten Kotlin-Entwickler hängen noch immer an Cloud-basierten
Modellen. Das bedeutet Latenz, Datenschutzrisiken und laufende Kosten.

Wir glauben, dass die Zukunft der KI so aussieht:

• 🔐 Privat: Deine Daten bleiben auf dem Gerät.

• 📱 Multiplattform: Eine Codebasis für Android, iOS, macOS, Linux und Windows.

• ⚡ Performant: Gebaut für kleine, quantisierte Modelle, die auf handelsüblicher Hardware schnell laufen.

Llamatik schlägt die Brücke zwischen dem C++-Backend von llama.cpp und dem modernen Multiplatform-Tooling von Kotlin –
und gibt dir die volle Kontrolle über deine Modelle und deine Daten.

⸻

💡 Was kannst du damit machen?

Mit Llamatik kannst du:

• 🧠 Quantisierte LLMs wie Phi-2, Mistral oder TinyLlama komplett offline ausführen.

• 🔍 Embeddings mit Modellen wie nomic-embed-text oder bge-small lokal erzeugen.

• ⚡ Deinen eigenen HTTP-Inferenzserver mit Ktor starten – vollständig eigenständig, in Llamatik integriert
und von llama.cpp angetrieben.

• 🌐 Dich mit dem mitgelieferten HTTP-Client mit entfernten llama.cpp-Endpunkten verbinden (etwa llama-cpp-python, llama-server oder Ollama).

• 🔁 Dieselbe API auf Android, iOS und weiteren nativen Plattformen nutzen – ganz ohne plattformspezifischen Code.

⸻

🌐 Integrierter HTTP-Inferenzserver

Llamatik bringt einen sofort einsatzbereiten Ktor-basierten HTTP-Server mit, der deine lokalen llama.cpp-Modelle kapselt. Du
startest ihn mit einem einzigen Aufruf und stellst Endpunkte wie diese bereit:

POST /v1/chat/completions

POST /v1/embeddings

GET /v1/models

Damit kannst du ganz einfach:

• 🔗 Deine eigenen Apps (oder andere Geräte) mit einem gemeinsamen lokalen Modell verbinden

• 🧪 OpenAI-kompatibles Tooling (z. B. LangChain, LlamaIndex) mit deinem lokalen Server nutzen

• ⚙️ Alles in deine eigenen Edge-Deployments oder Experimente integrieren

Kein Python nötig. Kein Docker erforderlich. Nur Kotlin + Llamatik.

⸻

📦 Was steckt drin?

• 🦙 Native Bindings zu llama.cpp für Kotlin/Native-Targets (ohne JNI oder JNI-only-Einschränkungen).

• 🧠 Kontextmanager für mehrere Modelle (für gleichzeitige Generierung + Embeddings).

• 🛰️ Optionaler HTTP-Client und -Server.

• 🧱 Modell-Laden, Prompt-Aufbau und speichereffiziente Ausführung, zugeschnitten auf Mobilgeräte.

• 🛠️ Eine einfache, erweiterbare API – für Kotlin gebaut, nicht aus Python übernommen.

⸻

📢 Mach mit

Llamatik ist Open Source und community-getrieben. Ob du KI-first-Apps baust oder einfach
erkunden willst, was offline alles möglich ist – wir freuen uns auf dein Feedback.

🔗 [GitHub-Repository](https://github.com/ferranpons/Llamatik)