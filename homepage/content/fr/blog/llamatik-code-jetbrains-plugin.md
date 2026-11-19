---
title: "Découvrez Llamatik Code : un assistant de code IA axé sur la confidentialité pour IntelliJ et Android Studio"
date: 2026-05-14T00:00:00-05:00
description: "Llamatik Code est un assistant de code IA local-first pour les IDE basés sur IntelliJ. Toute l'inférence s'exécute sur votre machine avec des modèles GGUF : pas de cloud, pas de clés d'API, aucune donnée ne quitte votre appareil."
image: "images/blog/llamatik_code_landing_image.png"
tags: ["Llamatik Code", "IntelliJ", "Android Studio", "AI", "Privacy", "llama.cpp", "GGUF", "Local AI"]
showDate: true
draft: false
---

Depuis le premier jour, nous construisons Llamatik comme une plateforme d'IA privée, directement sur l'appareil. Aujourd'hui, nous apportons cette même philosophie à votre IDE.

**Llamatik Code** est désormais disponible sur le [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31304-llamatik-code) : un assistant de code IA entièrement local, axé sur la confidentialité, pour IntelliJ IDEA, Android Studio et toute la famille des IDE JetBrains.

Pas de cloud. Pas de clés d'API. Aucune donnée ne quitte votre machine. Juste votre modèle GGUF local, qui tourne dans votre IDE.

---

## Pourquoi de l'IA locale dans votre IDE ?

La plupart des assistants de code IA fonctionnent de la même façon : votre code est envoyé à un serveur distant, un modèle dans le cloud le traite, et le résultat revient. Ça marche, mais avec des contreparties :

- Votre code et son contexte quittent votre machine à chaque requête
- Vous dépendez de la connexion réseau et de la disponibilité de services tiers
- Le coût des abonnements croît avec l'usage
- Les environnements d'entreprise et réglementés ne peuvent souvent pas du tout utiliser l'IA dans le cloud

Llamatik Code adopte une approche différente. Tout s'exécute sur l'appareil avec [llama.cpp](https://github.com/ggerganov/llama.cpp), le même runtime éprouvé qui propulse les applications mobiles et desktop Llamatik. Vous chargez un modèle GGUF une fois, et dès lors votre IDE dispose d'un assistant IA pleinement capable qui ne contacte jamais personne.

---

## Ce que sait faire Llamatik Code

### Ask Mode : discutez de tout, sans modifier de fichiers

Posez des questions, explorez des API, obtenez des explications, générez des extraits de code. Ask Mode est une interface conversationnelle complète, avec un historique multisession persistant, qui connaît vos fichiers actuels, le texte sélectionné et la structure de votre projet.

Ask Mode est **illimité dans l'offre gratuite** : pas de plafond quotidien, pas de crédits.

### Agent Mode : laissez le modèle piloter les modifications de code

Agent Mode va plus loin. Vous décrivez ce que vous voulez en langage naturel, et l'agent propose des modifications de code, vous montre un diff et attend votre approbation avant de toucher à un fichier.

L'agent connaît votre projet : il comprend l'arborescence de vos fichiers, peut naviguer d'un fichier à l'autre et enchaîne plusieurs appels d'outils pour mener à bien des tâches en plusieurs étapes. L'offre gratuite inclut les tâches d'agent à session unique. Pro débloque l'exécution parallèle de plusieurs tâches et **Aggressive Mode**, où l'agent peut prendre des décisions de façon autonome, sans demander votre approbation à chaque étape.

### Modification de plusieurs fichiers (Pro)

Appliquez des modifications de code à plusieurs fichiers à la fois. Pratique pour les refactorisations, les migrations d'API et tout ce qui touche plus d'un fichier.

### Intégration de serveurs MCP

Llamatik Code prend en charge le [Model Context Protocol](https://modelcontextprotocol.io/), ce qui vous permet de connecter l'agent à des outils et services externes. L'offre gratuite prend en charge un serveur MCP avec approbation manuelle des actions. Pro débloque un nombre illimité de serveurs MCP et l'approbation automatique.

### External Knowledge : la recherche web intégrée

Depuis la version 1.5.0, Llamatik Code inclut un système External Knowledge qui permet au modèle de rechercher sur le web et de lire de la documentation, sans aucune clé d'API.

Il s'appuie sur DuckDuckGo et comprend l'intention du développeur :

- Questions sur Kotlin ou les coroutines → recherche sur kotlinlang.org
- Questions sur Android ou Compose → recherche sur developer.android.com
- Messages d'erreur → recherche dans les issues GitHub
- Questions sur des bibliothèques → recherche dans les registres de paquets et les changelogs

Chaque résultat est accompagné de citations structurées : type de source, niveau de confiance et URL directe. Le modèle planifie automatiquement des workflows de recherche en plusieurs étapes et synthétise les résultats en une réponse cohérente.

L'offre gratuite inclut 5 recherches par jour (5 résultats chacune). Pro passe à 50 recherches par jour avec 10 résultats chacune, et jusqu'à 10 sources de documentation privées.

### Code Health Monitor

Un mode d'analyse en arrière-plan qui fait remonter les problèmes de qualité du code. Les utilisateurs gratuits peuvent le lancer manuellement ; Pro débloque des modes automatisés avancés.

---

## Gratuit ou Pro

| Fonctionnalité | Gratuit | Pro |
|---|---|---|
| Ask Mode (chat) | Illimité | Illimité |
| Sessions d'agent | Session unique | Multitâche, parallèle |
| Aggressive Mode de l'agent | — | ✓ |
| Application de code sur plusieurs fichiers | — | ✓ |
| Serveurs MCP | 1, approbation manuelle | Illimités, approbation automatique |
| Recherches External Knowledge par jour | 5 | 50 |
| Résultats par recherche | 5 | 10 |
| Sources de documentation privées | 1 | 10 |
| Code Health Monitor | Manuel | Automatisé avancé |

Les abonnements Pro sont disponibles sur [llamatik.com](https://www.llamatik.com).

---

## Pour commencer

**Prérequis :**
- IntelliJ IDEA 2026.1 ou tout IDE JetBrains basé sur la plateforme build 252+
- macOS, Linux ou Windows (64 bits, JDK 21+)
- Un fichier de modèle GGUF stocké en local

**Installation :**
1. Ouvrez votre IDE → Settings → Plugins → Marketplace
2. Recherchez **Llamatik Code** et installez-le
3. Redémarrez l'IDE

**Configuration du modèle :**
1. Allez dans Settings → Llamatik Code → Inference
2. Indiquez le chemin de votre fichier `.gguf` local
3. Ajustez la taille du contexte et le nombre de threads selon votre matériel

Tout modèle GGUF compatible avec llama.cpp fonctionne. Pour la plupart des tâches de développement, un modèle instruct quantifié 7B en Q4_K_M ou Q5_K_M est un bon point de départ. Pour un travail d'agent plus lourd ou des bases de code complexes, un modèle 13B–14B offre un raisonnement nettement meilleur.

Les instructions d'installation complètes et les recommandations de modèles se trouvent dans la [documentation](https://docs.llamatik.com/llamatik-code/).

---

## Construit sur le moteur Llamatik

Llamatik Code repose sur la même intégration Kotlin-first de llama.cpp que celle qui propulse les applications mobiles et desktop Llamatik. Vous bénéficiez donc de la même qualité d'inférence, de la même compatibilité GGUF et des mêmes garanties de confidentialité, que vous discutiez sur votre téléphone ou éditiez du code dans votre IDE.

L'accès réseau externe ne se produit que via des serveurs MCP explicitement configurés ou des recherches External Knowledge. Rien d'autre ne quitte votre machine.

---

## Installez-le dès aujourd'hui

Llamatik Code est disponible dès maintenant sur le JetBrains Marketplace.

→ [Installer Llamatik Code](https://plugins.jetbrains.com/plugin/31304-llamatik-code)  
→ [Lire la documentation](https://docs.llamatik.com/llamatik-code/)
