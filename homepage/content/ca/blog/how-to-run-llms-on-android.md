---
title: "Com executar LLM sense connexió a Android amb Kotlin"
date: 2026-02-11T00:00:00-05:00
description: "Aquest article explica com executar LLM sense connexió a Android amb Kotlin, sense servidors, sense claus d'API i amb total privacitat per a l'usuari, gràcies a Llamatik, un embolcall de llama.cpp pensat per a Kotlin."
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

Executar grans models de llenguatge (LLM) sol implicar enviar les dades de l'usuari al núvol. Però als dispositius Android moderns, els LLM es poden executar totalment sense connexió, en local, amb Kotlin.
Aquest article explica com executar LLM sense connexió a Android amb Kotlin, sense servidors, sense claus d'API i amb total privacitat per a l'usuari, gràcies a Llamatik, un embolcall de llama.cpp pensat per a Kotlin.

## Per què executar LLM sense connexió a Android?

Els LLM al núvol tenen inconvenients:
- ❌ Dependència de la xarxa
- ❌ Latència
- ❌ Cost segons l'ús
- ❌ Les dades de l'usuari surten del dispositiu

Els LLM sense connexió permeten:
- 📴 Apps pensades per funcionar sense connexió
- 🔐 IA que respecta la privacitat
- 📱 Rendiment i cost previsibles

Els telèfons Android actuals són prou potents; el que faltava eren bones eines per a Kotlin.

## llama.cpp: la base dels LLM al dispositiu

llama.cpp és un runtime en C++ d'alt rendiment dissenyat per executar LLM de manera eficient en CPU.

Admet:
- Models GGUF quantitzats
- CPU ARM (Android)
- Cap dependència de GPU ni del núvol

Però integrar C++ directament en apps Android és feixuc.
Aquí és on entra Llamatik.

## Què és Llamatik?

Llamatik és una llibreria pensada primer de tot per a Kotlin que permet als desenvolupadors Android executar llama.cpp en local mitjançant una API de Kotlin neta.

Característiques principals:
- Inferència totalment sense connexió
- Sense JNI al codi de l'app
- Suport per a Kotlin Multiplatform
- Compatibilitat amb models GGUF
- Suport de streaming i embeddings

Tu només escrius Kotlin; la complexitat nativa queda amagada.

## Afegeix Llamatik al teu projecte Android

Llamatik es publica a Maven Central.

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

Sense configuració personalitzada de Gradle.
Sense configurar l'NDK.

## Afegeix un model GGUF

Descarrega un model GGUF quantitzat (p. ex. Q4) i col·loca'l a:
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

Els models més petits es carreguen més de pressa i van millor als dispositius mòbils.

## Carrega el model

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

Això copia el model des dels assets i el carrega a la memòria nativa.

## Genera text sense connexió

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

Ja està.
Sense internet.
Sense claus d'API.
Sense crides al núvol.

## Generació en streaming (recomanada)

Per a interfícies d'estil xat:
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

Funciona sense problemes amb:
- Jetpack Compose
- StateFlow
- ViewModels

## Embeddings i RAG sense connexió

Llamatik també admet embeddings, cosa que permet cerca i casos d'ús de RAG sense connexió.

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

Desa els embeddings en local per crear funcionalitats d'IA totalment sense connexió.

## Notes sobre el rendiment

Els LLM al dispositiu tenen límits:
- Fes servir models quantitzats
- Compta amb respostes més lentes que en GPU al núvol
- Gestiona la memòria amb cura

Però per a:
- Funcionalitats d'assistència
- Prompts curts
- Tasques d'un domini concret

El rendiment és més que suficient.

## Quan val la pena fer servir LLM sense connexió?

Llamatik és ideal quan necessites:
- Suport sense connexió
- Fortes garanties de privacitat
- Costos previsibles
- Una integració estreta amb la interfície

No substitueix els grans models al núvol: és IA a la vora (edge) ben feta.

## Prova-ho tu mateix
- GitHub: [https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- Web i app de demostració: [https://llamatik.com](https://llamatik.com)
- llama.cpp: [https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## Conclusions
Executar LLM sense connexió a Android amb Kotlin ja no és experimental.
Amb les eines adequades, els desenvolupadors Kotlin poden crear IA privada, sense connexió i al dispositiu, sense haver de tocar C++.
Si tens curiositat per la IA mòbil més enllà del núvol, aquest és un molt bon punt de partida.


Publicat originalment a [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) i [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g).
