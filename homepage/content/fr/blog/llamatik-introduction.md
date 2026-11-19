---
title: "Présentation de Llamatik : des LLM hors ligne pour Kotlin Multiplatform"
date: 2025-07-22T09:00:00-05:00
description: "Découvrez Llamatik, une bibliothèque Kotlin multiplateforme pour exécuter llama.cpp en local sur Android, iOS et desktop, avec un serveur HTTP intégré."
image: "images/blog/a-pair-of-llamas-in-a-field-with-clouds-and-mountain.svg"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI"]
showDate: true
draft: false
---

🦙 Présentation de Llamatik : des LLM hors ligne pour Kotlin Multiplatform

Nous sommes ravis de vous présenter Llamatik, une bibliothèque Kotlin Multiplatform open source qui
apporte les grands modèles de langage (LLM) locaux à Android, iOS, au desktop et au-delà, grâce à la
puissance de llama.cpp.

Llamatik rend l'intégration de l'inférence et des embeddings hors ligne, directement sur l'appareil,
simple et efficace dans vos applications KMP, que vous construisiez un assistant IA, un chatbot RAG
ou un outil d'intelligence en périphérie (edge).

⸻

✨ Pourquoi Llamatik ?

Si l'écosystème de l'IA regorge d'API, la plupart des développeurs Kotlin restent tributaires de
modèles hébergés dans le cloud. Avec, à la clé, de la latence, des risques pour la confidentialité et
des coûts récurrents.

Nous croyons que l'avenir de l'IA sera :

• 🔐 Privé : vos données restent sur l'appareil.

• 📱 Multiplateforme : une seule base de code pour Android, iOS, macOS, Linux et Windows.

• ⚡ Performant : conçu pour des modèles compacts et quantifiés qui tournent vite sur du matériel grand public.

Llamatik comble le fossé entre le backend C++ de llama.cpp et l'outillage multiplateforme moderne de
Kotlin, pour vous donner un contrôle total sur vos modèles et vos données.

⸻

💡 Que pouvez-vous en faire ?

Avec Llamatik, vous pouvez :

• 🧠 Exécuter des LLM quantifiés comme Phi-2, Mistral ou TinyLlama entièrement hors ligne.

• 🔍 Générer des embeddings en local avec des modèles comme nomic-embed-text ou bge-small.

• ⚡ Lancer votre propre serveur d'inférence HTTP avec Ktor, entièrement autonome, intégré à Llamatik
et propulsé par llama.cpp.

• 🌐 Vous connecter à des endpoints llama.cpp distants (comme llama-cpp-python, llama-server ou
Ollama) grâce au client HTTP fourni.

• 🔁 Utiliser la même API sur Android, iOS et les autres plateformes natives, sans code spécifique à
chaque plateforme.

⸻

🌐 Serveur d'inférence HTTP intégré

Llamatik inclut un serveur HTTP prêt à l'emploi, basé sur Ktor, qui encapsule vos modèles llama.cpp
locaux. Vous le démarrez d'un seul appel et il expose des endpoints comme :

POST /v1/chat/completions

POST /v1/embeddings

GET /v1/models

Cela facilite :

• 🔗 La connexion de vos propres applications (ou d'autres appareils) à un modèle local partagé

• 🧪 L'utilisation d'outils compatibles OpenAI (par ex. LangChain, LlamaIndex) avec votre serveur local

• ⚙️ L'intégration à vos propres déploiements ou expériences en périphérie

Pas de Python. Pas de Docker. Juste Kotlin + Llamatik.

⸻

📦 Ce qu'il y a dans la boîte

• 🦙 Des bindings natifs vers llama.cpp pour les cibles Kotlin/Native (sans les limites de JNI ou du « JNI seulement »).

• 🧠 Un gestionnaire de contexte multimodèle (pour la génération et les embeddings simultanés).

• 🛰️ Un client et un serveur HTTP en option.

• 🧱 Chargement des modèles, construction des prompts et exécution économe en mémoire, pensés pour le mobile.

• 🛠️ Une API simple et extensible, conçue pour Kotlin et non adaptée depuis Python.

⸻

📢 Rejoignez l'aventure

Llamatik est open source et porté par la communauté. Que vous construisiez des applications centrées
sur l'IA ou que vous exploriez simplement ce qui est possible hors ligne, nous serions ravis d'avoir
de vos nouvelles.

🔗 [Dépôt GitHub](https://github.com/ferranpons/Llamatik)
