---
title: "Comment exécuter des LLM hors ligne sur Android avec Kotlin"
date: 2026-02-11T00:00:00-05:00
description: "Cet article montre comment exécuter des LLM hors ligne sur Android avec Kotlin, sans serveur, sans clé d'API et dans le plus grand respect de la vie privée, grâce à Llamatik, un wrapper Kotlin-first autour de llama.cpp."
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

Exécuter de grands modèles de langage (LLM) implique généralement d'envoyer les données des utilisateurs dans le cloud. Pourtant, sur les appareils Android récents, les LLM peuvent tourner entièrement hors ligne, en local, avec Kotlin.
Cet article montre comment exécuter des LLM hors ligne sur Android avec Kotlin, sans serveur, sans clé d'API et dans le plus grand respect de la vie privée, grâce à Llamatik, un wrapper Kotlin-first autour de llama.cpp.

## Pourquoi exécuter des LLM hors ligne sur Android ?

Les LLM dans le cloud ont leurs contreparties :
- ❌ Dépendance au réseau
- ❌ Latence
- ❌ Coût proportionnel à l'usage
- ❌ Des données utilisateur qui quittent l'appareil

Les LLM hors ligne permettent :
- 📴 Des applications pensées d'abord pour le hors ligne
- 🔐 Une IA qui préserve la vie privée
- 📱 Des performances et des coûts prévisibles

Les téléphones Android récents sont assez puissants : ce qui manquait, c'était un bon outillage Kotlin.

## llama.cpp : le socle des LLM sur l'appareil

llama.cpp est un runtime C++ très performant, conçu pour exécuter efficacement des LLM sur CPU.

Il prend en charge :
- Les modèles GGUF quantifiés
- Les CPU ARM (Android)
- Aucune dépendance à un GPU ou au cloud

Mais intégrer du C++ directement dans une application Android est fastidieux.
C'est là qu'intervient Llamatik.

## Qu'est-ce que Llamatik ?

Llamatik est une bibliothèque Kotlin-first qui permet aux développeurs Android d'exécuter llama.cpp en local grâce à une API Kotlin claire.

Principales fonctionnalités :
- Inférence entièrement hors ligne
- Pas de JNI dans le code de l'application
- Prise en charge de Kotlin Multiplatform
- Compatibilité avec les modèles GGUF
- Prise en charge du streaming et des embeddings

Vous n'écrivez que du Kotlin : la complexité native reste cachée.

## Ajouter Llamatik à votre projet Android

Llamatik est publié sur Maven Central.

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

Pas de configuration Gradle personnalisée.
Pas de configuration du NDK.

## Ajouter un modèle GGUF

Téléchargez un modèle GGUF quantifié (par ex. Q4) et placez-le dans :
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

Les modèles plus petits se chargent plus vite et fonctionnent mieux sur mobile.

## Charger le modèle

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

Le modèle est copié depuis les assets puis chargé dans la mémoire native.

## Générer du texte hors ligne

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

Et c'est tout.
Pas d'Internet.
Pas de clés d'API.
Pas d'appels au cloud.

## Génération en streaming (recommandée)

Pour les interfaces de type chat :
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

Cela s'intègre sans heurt avec :
- Jetpack Compose
- StateFlow
- ViewModels

## Embeddings et RAG hors ligne

Llamatik prend aussi en charge les embeddings, ce qui ouvre la porte à la recherche et aux cas d'usage RAG hors ligne.

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

Stockez les embeddings en local pour créer des fonctionnalités d'IA entièrement hors ligne.

## Notes sur les performances

Les LLM sur l'appareil ont leurs limites :
- Utilisez des modèles quantifiés
- Attendez-vous à des réponses plus lentes qu'avec les GPU du cloud
- Gérez la mémoire avec soin

Mais pour :
- Des fonctionnalités d'assistance
- Des prompts courts
- Des tâches propres à un domaine

Les performances sont largement suffisantes.

## Quand utiliser des LLM hors ligne ?

Llamatik est idéal lorsque vous avez besoin de :
- La prise en charge du hors ligne
- De solides garanties de confidentialité
- Des coûts prévisibles
- Une intégration étroite à l'interface

Ce n'est pas un remplaçant des grands modèles du cloud : c'est de l'edge AI bien faite.

## Essayez par vous-même
- GitHub : [https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- Site web et application de démo : [https://llamatik.com](https://llamatik.com)
- llama.cpp : [https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## Pour conclure
Exécuter des LLM hors ligne sur Android avec Kotlin n'a plus rien d'expérimental.
Avec les bons outils, les développeurs Kotlin peuvent créer une IA privée, hors ligne et sur l'appareil, sans jamais toucher au C++.
Si vous êtes curieux de l'IA mobile au-delà du cloud, c'est un excellent point de départ.


Publié initialement sur [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) et [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g).
