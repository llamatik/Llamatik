---
title: "Die Llamatik-App ist da: Offline-KI-Chat auf deinem Smartphone"
date: 2025-11-25T09:00:00-05:00
description: "Wir freuen uns, die Llamatik-App zu veröffentlichen – einen vollständig offline laufenden KI-Chatbot, der zeigt, was die Kotlin-Multiplatform-Bibliothek Llamatik kann. Ob du als Entwickler neugierig auf KI auf dem Gerät bist oder als Nutzer einen privaten, schnellen und modernen Chatbot suchst: Die Llamatik-App bringt das volle Potenzial schlanker LLMs direkt auf dein Smartphone."
image: "images/blog/llamatik_new_app_hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "App"]
showDate: true
draft: false
---

🚀 Die Llamatik-App ist da: Offline-KI-Chat auf deinem Smartphone

Wir freuen uns, die Llamatik-App zu veröffentlichen – einen vollständig offline laufenden KI-Chatbot, der zeigt, was die Kotlin-Multiplatform-Bibliothek Llamatik kann.
Ob du als Entwickler neugierig auf KI auf dem Gerät bist oder als Nutzer einen privaten, schnellen und modernen Chatbot suchst: Die Llamatik-App bringt das volle Potenzial schlanker LLMs direkt auf dein Smartphone.

⸻

🔍 Was ist Llamatik?

Llamatik ist eine Kotlin-Multiplatform-Bibliothek, mit der Entwickler lokale Large Language Models (LLMs) und Vektor-Embeddings auf Android, iOS, Desktop und im Web ausführen können.

Mit der Llamatik-App kannst du jetzt alles ausprobieren, was die Bibliothek kann – ohne eine einzige Zeile Code zu schreiben.

Die App dient als Live-Demo, als Sandbox und als Referenzimplementierung für Entwickler, die lokale KI-Erlebnisse bauen.

⸻

⚡ Das kann die Llamatik-App

🧠 KI-Chat, komplett lokal

Die gesamte Textgenerierung läuft zu 100 % auf deinem Gerät und nutzt quantisierte Open-Source-Modelle.
Es gibt keine Cloud, keinen Server und keine Daten, die irgendwohin gesendet werden.
Das macht Llamatik:
•	Privat
•	Schnell
•	Immer verfügbar
•	Geeignet für Umgebungen mit strengen Datenschutzanforderungen

Die Chat-Oberfläche ist schlicht, aufgeräumt und mit Jetpack Compose Multiplatform gebaut.

⸻

📦 Integrierter Modell-Downloader

Die App bietet direkten Download für mehrere optimierte Modelle, darunter:
•	Gemma 3 270M
•	SmolVLM 256M / 500M (Vision + Sprache)
•	Phi-1.5
•	Qwen 2.5 5B (quantisiert)
•	Llama 3.2 1B (quantisiert)

Alle Modelle liegen im GGUF-Format vor und sind mit dem in Llamatik integrierten llama.cpp-Backend kompatibel.

Du kannst Modelle direkt in der App herunterladen, wechseln und verwalten.

⸻

🎯 Intelligente Vorschläge & Schnellaktionen

Damit sich die App intuitiver bedienen lässt, bietet sie:
•	Vorgeschlagene Prompts
•	Schnellaktionen
•	Eine aufgeräumte, reaktionsschnelle Oberfläche
•	Eine auswählbare Modell-Pill über dem Eingabefeld
•	Ein integriertes Onboarding

Diese kleinen Details machen die App für Einsteiger und Profis gleichermaßen attraktiv.

⸻

🔐 Datenschutz zuerst

Jede Generierung findet lokal statt.
Deine Unterhaltungen:
•	werden nicht gesammelt
•	werden nicht extern gespeichert
•	werden weder protokolliert noch an einen Server gesendet

Selbst die Modell-Inferenz läuft offline mit llama.cpp, das für Mobilgeräte kompiliert wurde.

Die einzige Telemetrie in der App sind Firebase Analytics und Crashlytics – ausschließlich für die Fehlerverfolgung und anonyme Nutzungsstatistiken.

⸻

🧩 Ein Schaufenster für die Features der Llamatik-Bibliothek

Die App demonstriert mehrere zentrale Fähigkeiten der zugrunde liegenden Bibliothek:

✔ Unterstützung für Kotlin Multiplatform

Einmal bauen, auf Android, iOS, Desktop und im Web ausführen.

✔ LLM-Inferenz auf dem Gerät

Über eine eigene C/C++-Bridge zu llama.cpp.

✔ Erzeugung von Embeddings

Kompatibel mit Modellen wie nomic-embed-text-v1.5.

✔ Unterstützung für Vektor-Stores

Baue lokale semantische Suche oder RAG-ähnliche Funktionen mit schlanken Vektor-Stores.

✔ Modellverwaltung

Mehrere quantisierte Modelle herunterladen, laden, entladen, wechseln und damit generieren.

✔ ChatRunner-Utilities auf hoher Ebene

Einfaches Streaming, Generieren und Steuern der Konversationslogik.

Für alle Entwickler unter euch: Die Llamatik-App ist vollständig auf der Open-Source-Bibliothek aufgebaut – und ihr Code ist ein praxisnahes Beispiel für lokale KI-Integration mit KMP im echten Einsatz.

⸻

📱 Warum eine App?

Die Llamatik-Bibliothek ist zwar schon mächtig, aber wir wollten einen Weg schaffen, damit:
•	Entwickler die Bibliothek in Aktion sehen können
•	Nutzer mit lokaler KI experimentieren können
•	Die Community Modelle einfach testen kann
•	Jeder Offline-KI-Assistenten ohne Setup erleben kann
•	Neueinsteiger verstehen, wie eine plattformübergreifende KI-App aufgebaut ist

Die Llamatik-App bietet dir eine komplette, ausgereifte Umgebung, um zu entdecken, was lokale LLMs auf einem Smartphone leisten.

⸻

🗺 Wie geht es weiter?

Daran arbeiten wir bereits:
•	🔍 Integrierte Vektorsuche für PDFs und Dokumente
•	📁 Lokaler Modellimport aus dem Dateispeicher
•	🌐 Mehr Modellanbieter und Modellgrößen
•	🎛 Erweiterte Chat-Einstellungen (Temperature, Top-k usw.)
•	🧱 RAG-Pipelines auf dem Gerät
•	🛠 Beispielprojekte & Tutorials für Entwickler

Dein Feedback hilft uns, die Roadmap zu gestalten – teile also gern deine Gedanken mit uns!

⸻

🐪 Probier Llamatik noch heute aus

Du kannst die [Llamatik-App jetzt bei Google Play laden](https://play.google.com/store/apps/details?id=com.llamatik.app.android) – und bald auch im iOS App Store.

Sie ist kostenlos, offen, offline und mit Blick auf Datenschutz und Performance gebaut.

Ob du als Entwickler neue KI-Features baust oder als Nutzer einen schnellen, lokalen KI-Assistenten willst – Llamatik ist für dich da.

👉 Entdecken. Lernen. Bauen.
👉 Erlebe, was möglich ist, wenn KI komplett auf deinem Gerät läuft.