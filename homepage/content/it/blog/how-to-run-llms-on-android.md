---
title: "Come eseguire LLM offline su Android con Kotlin"
date: 2026-02-11T00:00:00-05:00
description: "Questo articolo mostra come eseguire LLM offline su Android con Kotlin, senza server, senza chiavi API e con la piena privacy degli utenti, usando Llamatik, un wrapper Kotlin-first di llama.cpp."
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

Eseguire Large Language Model (LLM) di solito significa inviare i dati degli utenti al cloud. Ma sui dispositivi Android moderni gli LLM possono girare completamente offline, in locale, usando Kotlin.
Questo articolo mostra come eseguire LLM offline su Android con Kotlin, senza server, senza chiavi API e con la piena privacy degli utenti, usando Llamatik, un wrapper Kotlin-first di llama.cpp.

## Perché eseguire gli LLM offline su Android?

Gli LLM nel cloud hanno i loro compromessi:
- ❌ Dipendenza dalla rete
- ❌ Latenza
- ❌ Costi basati sull'utilizzo
- ❌ Dati degli utenti che escono dal dispositivo

Gli LLM offline permettono di avere:
- 📴 App offline-first
- 🔐 IA che rispetta la privacy
- 📱 Prestazioni e costi prevedibili

Gli smartphone Android moderni sono abbastanza potenti: quello che mancava erano buoni strumenti Kotlin.

## llama.cpp: la base degli LLM on-device

llama.cpp è un runtime C++ ad alte prestazioni progettato per eseguire gli LLM in modo efficiente sulle CPU.

Supporta:
- Modelli GGUF quantizzati
- CPU ARM (Android)
- Nessuna dipendenza da GPU o cloud

Ma integrare direttamente il C++ nelle app Android è un incubo.
È qui che entra in gioco Llamatik.

## Che cos'è Llamatik?

Llamatik è una libreria Kotlin-first che permette agli sviluppatori Android di eseguire llama.cpp in locale tramite una API Kotlin pulita.

Funzionalità principali:
- Inferenza completamente offline
- Nessun JNI nel codice dell'app
- Supporto a Kotlin Multiplatform
- Compatibilità con i modelli GGUF
- Supporto a streaming ed embedding

Scrivi solo Kotlin: la complessità nativa resta nascosta.

## Aggiungi Llamatik al tuo progetto Android

Llamatik è pubblicato su Maven Central.

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

Nessuna configurazione Gradle personalizzata.
Nessuna configurazione dell'NDK.

## Aggiungi un modello GGUF

Scarica un modello GGUF quantizzato (ad es. Q4) e posizionalo in:
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

I modelli più piccoli si caricano più velocemente e funzionano meglio sui dispositivi mobili.

## Carica il modello

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

Questo copia il modello dagli asset e lo carica nella memoria nativa.

## Genera testo offline

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

Tutto qui.
Niente internet.
Niente chiavi API.
Niente chiamate al cloud.

## Generazione in streaming (consigliata)

Per le interfacce in stile chat:
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

Funziona senza problemi con:
- Jetpack Compose
- StateFlow
- ViewModel

## Embedding e RAG offline

Llamatik supporta anche gli embedding, abilitando la ricerca offline e i casi d'uso RAG.

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

Salva gli embedding in locale per creare funzionalità di IA completamente offline.

## Note sulle prestazioni

Gli LLM on-device hanno dei limiti:
- Usa modelli quantizzati
- Aspettati risposte più lente rispetto alle GPU nel cloud
- Gestisci la memoria con attenzione

Ma per:
- Funzionalità di assistenza
- Prompt brevi
- Compiti specifici di un dominio

Le prestazioni sono più che sufficienti.

## Quando conviene usare gli LLM offline?

Llamatik è ideale quando ti servono:
- Supporto offline
- Solide garanzie di privacy
- Costi prevedibili
- Integrazione stretta con la UI

Non sostituisce i grandi modelli nel cloud: è edge AI fatta bene.

## Provalo tu stesso
- GitHub: [https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- Sito web e app demo: [https://llamatik.com](https://llamatik.com)
- llama.cpp: [https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## Considerazioni finali
Eseguire gli LLM offline su Android con Kotlin non è più sperimentale.
Con gli strumenti giusti, gli sviluppatori Kotlin possono creare IA privata, offline e on-device senza toccare il C++.
Se sei curioso di scoprire l'IA mobile oltre il cloud, questo è un ottimo punto di partenza.


Pubblicato originariamente su [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) e [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g).
