---
title: "LLMs offline auf Android ausführen – mit Kotlin"
date: 2026-02-11T00:00:00-05:00
description: "Dieser Artikel zeigt, wie du LLMs offline auf Android mit Kotlin ausführst – ohne Server, ohne API-Keys und mit vollem Datenschutz für deine Nutzer. Dafür nutzt du Llamatik, einen Kotlin-first-Wrapper um llama.cpp."
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

Large Language Models (LLMs) auszuführen bedeutet meistens, Nutzerdaten in die Cloud zu schicken. Auf modernen Android-Geräten laufen LLMs aber komplett offline und lokal – mit Kotlin.
Dieser Artikel zeigt, wie du LLMs offline auf Android mit Kotlin ausführst – ohne Server, ohne API-Keys und mit vollem Datenschutz für deine Nutzer. Dafür nutzt du Llamatik, einen Kotlin-first-Wrapper um llama.cpp.

## Warum LLMs offline auf Android ausführen?

Cloud-basierte LLMs haben ihre Nachteile:
- ❌ Abhängigkeit vom Netzwerk
- ❌ Latenz
- ❌ Nutzungsabhängige Kosten
- ❌ Nutzerdaten verlassen das Gerät

Offline-LLMs ermöglichen:
- 📴 Offline-first-Apps
- 🔐 KI, die die Privatsphäre schützt
- 📱 Planbare Performance und Kosten

Moderne Android-Handys sind leistungsstark genug – was bisher fehlte, war gutes Kotlin-Tooling.

## llama.cpp: die Grundlage für LLMs auf dem Gerät

llama.cpp ist eine hochperformante C++-Runtime, die LLMs effizient auf CPUs ausführt.

Sie unterstützt:
- Quantisierte GGUF-Modelle
- ARM-CPUs (Android)
- Keine Abhängigkeit von GPU oder Cloud

Aber C++ direkt in Android-Apps einzubinden ist mühsam.
Genau hier kommt Llamatik ins Spiel.

## Was ist Llamatik?

Llamatik ist eine Kotlin-first-Bibliothek, mit der Android-Entwickler llama.cpp über eine saubere Kotlin-API lokal ausführen können.

Die wichtigsten Features:
- Vollständig offline Inferenz
- Kein JNI im App-Code
- Unterstützung für Kotlin Multiplatform
- Kompatibilität mit GGUF-Modellen
- Unterstützung für Streaming & Embeddings

Du schreibst nur Kotlin – die native Komplexität bleibt versteckt.

## Llamatik zu deinem Android-Projekt hinzufügen

Llamatik ist auf Maven Central veröffentlicht.

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

Kein eigenes Gradle-Setup.
Keine NDK-Konfiguration.

## Ein GGUF-Modell hinzufügen

Lade ein quantisiertes GGUF-Modell herunter (z. B. Q4) und lege es hier ab:
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

Kleinere Modelle laden schneller und laufen auf Mobilgeräten besser.

## Das Modell laden

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

Dadurch wird das Modell aus den Assets kopiert und in den nativen Speicher geladen.

## Text offline generieren

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

Das war's schon.
Kein Internet.
Keine API-Keys.
Keine Cloud-Aufrufe.

## Streaming-Generierung (empfohlen)

Für Chat-artige UIs:
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

Das funktioniert nahtlos mit:
- Jetpack Compose
- StateFlow
- ViewModels

## Embeddings & Offline-RAG

Llamatik unterstützt außerdem Embeddings und ermöglicht damit Offline-Suche und RAG-Anwendungsfälle.

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

Speichere Embeddings lokal, um komplett offline funktionierende KI-Features zu bauen.

## Hinweise zur Performance

LLMs auf dem Gerät haben Grenzen:
- Verwende quantisierte Modelle
- Rechne mit langsameren Antworten als bei Cloud-GPUs
- Gehe sorgfältig mit dem Speicher um

Aber für:
- Assistenzfunktionen
- Kurze Prompts
- Domänenspezifische Aufgaben

ist die Performance mehr als ausreichend.

## Wann solltest du Offline-LLMs einsetzen?

Llamatik ist ideal, wenn du Folgendes brauchst:
- Offline-Unterstützung
- Starke Datenschutzgarantien
- Planbare Kosten
- Enge UI-Integration

Es ist kein Ersatz für große Cloud-Modelle – es ist Edge-KI, richtig gemacht.

## Probier es selbst aus
- GitHub: [https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- Website & Demo-App: [https://llamatik.com](https://llamatik.com)
- llama.cpp: [https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## Schlussgedanken
LLMs offline auf Android mit Kotlin auszuführen ist längst nicht mehr experimentell.
Mit den richtigen Tools können Kotlin-Entwickler private, offline laufende KI auf dem Gerät bauen – ohne C++ anzufassen.
Wenn dich mobile KI jenseits der Cloud interessiert, ist das ein guter Einstieg.


Ursprünglich veröffentlicht auf [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) und [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g).
