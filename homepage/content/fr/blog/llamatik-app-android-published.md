---
title: "Découvrez l'application Llamatik : un chat IA hors ligne sur votre mobile"
date: 2025-11-25T09:00:00-05:00
description: "Nous sommes ravis d'annoncer le lancement de l'application Llamatik, un chatbot IA entièrement hors ligne conçu pour mettre en valeur la puissance et la polyvalence de la bibliothèque Kotlin Multiplatform Llamatik. Que vous soyez développeur et curieux de l'IA sur l'appareil, ou utilisateur en quête d'un chatbot privé, rapide et moderne, l'application Llamatik met tout le potentiel des LLM légers directement sur votre mobile."
image: "images/blog/llamatik_new_app_hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "App"]
showDate: true
draft: false
---

🚀 Découvrez l'application Llamatik : un chat IA hors ligne sur votre mobile

Nous sommes ravis d'annoncer le lancement de l'application Llamatik, un chatbot IA entièrement hors ligne conçu pour mettre en valeur la puissance et la polyvalence de la bibliothèque Kotlin Multiplatform Llamatik.
Que vous soyez développeur et curieux de l'IA sur l'appareil, ou utilisateur en quête d'un chatbot privé, rapide et moderne, l'application Llamatik met tout le potentiel des LLM légers directement sur votre mobile.

⸻

🔍 Qu'est-ce que Llamatik ?

Llamatik est une bibliothèque Kotlin Multiplatform qui permet aux développeurs d'exécuter des grands modèles de langage (LLM) locaux et des embeddings vectoriels sur Android, iOS, Desktop et le Web.

Avec la sortie de l'application Llamatik, vous pouvez désormais découvrir tout ce que la bibliothèque sait faire, sans écrire une seule ligne de code.

L'application sert à la fois de démo en direct, de bac à sable et d'implémentation de référence pour les développeurs qui créent des expériences d'IA locale.

⸻

⚡ Ce que sait faire l'application Llamatik

🧠 Un chat IA entièrement local

Toute la génération de texte s'exécute à 100 % sur votre appareil, avec des modèles open source quantifiés.
Pas de cloud, pas de serveur, et aucune donnée envoyée où que ce soit.
Cela rend Llamatik :
•	Privé
•	Rapide
•	Toujours disponible
•	Conforme aux environnements aux exigences strictes de confidentialité

L'interface de chat est simple, épurée, et construite avec Jetpack Compose Multiplatform.

⸻

📦 Téléchargeur de modèles intégré

L'application permet de télécharger directement plusieurs modèles optimisés, dont :
•	Gemma 3 270M
•	SmolVLM 256M / 500M (vision + langage)
•	Phi-1.5
•	Qwen 2.5 5B (quantifié)
•	Llama 3.2 1B (quantifié)

Tous les modèles sont au format GGUF et compatibles avec le backend llama.cpp intégré à Llamatik.

Vous pouvez télécharger, changer et gérer vos modèles directement depuis l'application.

⸻

🎯 Suggestions intelligentes et actions rapides

Pour rendre l'expérience plus intuitive, l'application propose :
•	Des prompts suggérés
•	Des actions rapides
•	Une interface épurée et réactive
•	Une pastille de modèle sélectionnable au-dessus de la zone de saisie
•	Une présentation intégrée au premier lancement

Ces petites attentions rendent l'application agréable aussi bien pour les débutants que pour les utilisateurs avancés.

⸻

🔐 La confidentialité avant tout

Chaque génération se déroule en local.
Vos conversations :
•	Ne sont pas collectées
•	Ne sont pas stockées à l'extérieur
•	Ne sont ni journalisées ni envoyées à un serveur

Même l'inférence des modèles se fait hors ligne, avec llama.cpp compilé pour le mobile.

La seule télémétrie de l'application est Firebase Analytics et Crashlytics, utilisés strictement pour le suivi des erreurs et des statistiques d'usage anonymes.

⸻

🧩 Une vitrine des fonctionnalités de la bibliothèque Llamatik

L'application illustre plusieurs des capacités clés de la bibliothèque sous-jacente :

✔ Prise en charge de Kotlin Multiplatform

Écrivez une fois, exécutez sur Android, iOS, Desktop et le Web.

✔ Inférence de LLM sur l'appareil

Grâce à un pont C/C++ sur mesure vers llama.cpp.

✔ Génération d'embeddings

Compatible avec des modèles comme nomic-embed-text-v1.5.

✔ Prise en charge des bases vectorielles

Créez de la recherche sémantique locale ou des fonctionnalités de type RAG avec des bases vectorielles légères.

✔ Gestion des modèles

Téléchargez, chargez, déchargez, changez de modèle et générez avec plusieurs modèles quantifiés.

✔ Utilitaires ChatRunner de haut niveau

Faciles à utiliser pour le streaming, la génération et la logique de conversation avec l'IA.

Pour les développeurs qui nous lisent : l'application Llamatik repose entièrement sur la bibliothèque open source, et son code source est un exemple concret d'intégration d'IA locale avec KMP dans un projet réel.

⸻

📱 Pourquoi une application ?

La bibliothèque Llamatik est déjà puissante, mais nous voulions offrir un moyen pour :
•	Les développeurs de voir la bibliothèque en action
•	Les utilisateurs d'expérimenter avec l'IA locale
•	La communauté de tester facilement des modèles
•	Tout le monde de découvrir des assistants IA hors ligne sans aucune configuration
•	Les nouveaux venus de comprendre comment est structurée une application d'IA multiplateforme

L'application Llamatik vous offre un environnement complet et soigné pour explorer ce que les LLM locaux peuvent faire sur un téléphone.

⸻

🗺 Et ensuite ?

Nous travaillons déjà sur :
•	🔍 La recherche vectorielle intégrée pour les PDF et les documents
•	📁 L'import local de modèles depuis le stockage de fichiers
•	🌐 Davantage de fournisseurs et de tailles de modèles
•	🎛 Des réglages de chat avancés (température, top-k, etc.)
•	🧱 Des pipelines RAG sur l'appareil
•	🛠 Des exemples et des tutoriels pour les développeurs

Vos retours aideront à façonner la feuille de route, alors n'hésitez pas à nous dire ce que vous en pensez !

⸻

🐪 Essayez Llamatik dès aujourd'hui

Vous pouvez dès maintenant télécharger l'[application Llamatik sur Google Play](https://play.google.com/store/apps/details?id=com.llamatik.app.android), et bientôt sur l'App Store d'iOS.

Elle est gratuite, ouverte, hors ligne, et conçue dans le souci de la confidentialité et des performances.

Que vous soyez développeur et que vous créiez de nouvelles fonctionnalités propulsées par l'IA, ou utilisateur à la recherche d'un assistant IA local et rapide, Llamatik est là pour vous.

👉 Explorez. Apprenez. Créez.
👉 Découvrez ce qui devient possible quand l'IA s'exécute entièrement sur votre appareil.
