---
title: "Presentiamo l'app Llamatik: una chat IA offline sul tuo dispositivo mobile"
date: 2025-11-25T09:00:00-05:00
description: "Siamo felici di annunciare il lancio dell'app Llamatik, un chatbot IA completamente offline pensato per mostrare la potenza e la versatilità della libreria Kotlin Multiplatform Llamatik. Che tu sia uno sviluppatore curioso dell'IA on-device o un utente che vuole un chatbot privato, veloce e moderno, l'app Llamatik porta tutto il potenziale degli LLM leggeri direttamente sul tuo dispositivo mobile."
image: "images/blog/llamatik_new_app_hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "App"]
showDate: true
draft: false
---

🚀 Presentiamo l'app Llamatik: chat IA offline sul tuo dispositivo mobile

Siamo felici di annunciare il lancio dell'app Llamatik, un chatbot IA completamente offline pensato per mostrare la potenza e la versatilità della libreria Kotlin Multiplatform Llamatik.
Che tu sia uno sviluppatore curioso dell'IA on-device o un utente che vuole un chatbot privato, veloce e moderno, l'app Llamatik porta tutto il potenziale degli LLM leggeri direttamente sul tuo dispositivo mobile.

⸻

🔍 Che cos'è Llamatik?

Llamatik è una libreria Kotlin Multiplatform che permette agli sviluppatori di eseguire Large Language Model (LLM) locali ed embedding vettoriali su Android, iOS, desktop e web.

Con il rilascio dell'app Llamatik, ora puoi provare tutto ciò che la libreria sa fare, senza scrivere una sola riga di codice.

L'app è allo stesso tempo una demo dal vivo, un ambiente di prova e un'implementazione di riferimento per gli sviluppatori che creano esperienze di IA locale.

⸻

⚡ Cosa sa fare l'app Llamatik

🧠 Chat IA completamente locale

Tutta la generazione di testo avviene al 100% sul tuo dispositivo, con modelli open source quantizzati.
Niente cloud, niente server, nessun dato inviato altrove.
Questo rende Llamatik:
•	Privato
•	Veloce
•	Sempre disponibile
•	Conforme agli ambienti con requisiti di privacy rigorosi

L'interfaccia della chat è semplice, pulita e realizzata con Jetpack Compose Multiplatform.

⸻

📦 Download dei modelli integrato

L'app include il download diretto di diversi modelli ottimizzati, tra cui:
•	Gemma 3 270M
•	SmolVLM 256M / 500M (visione + linguaggio)
•	Phi-1.5
•	Qwen 2.5 5B (quantizzato)
•	Llama 3.2 1B (quantizzato)

Tutti i modelli sono in formato GGUF e compatibili con il backend llama.cpp integrato in Llamatik.

Puoi scaricare, cambiare e gestire i modelli direttamente dall'app.

⸻

🎯 Suggerimenti intelligenti e azioni rapide

Per rendere l'esperienza più intuitiva, l'app offre:
•	Prompt suggeriti
•	Azioni rapide
•	Un'interfaccia pulita e reattiva
•	Un selettore del modello sopra la casella di input
•	Un onboarding integrato

Questi piccoli dettagli rendono l'app ideale sia per i principianti sia per gli utenti esperti.

⸻

🔐 La privacy prima di tutto

Ogni generazione avviene in locale.
Le tue conversazioni:
•	Non vengono raccolte
•	Non vengono archiviate all'esterno
•	Non vengono registrate né inviate a un server

Anche l'inferenza del modello avviene offline, usando llama.cpp compilato per il mobile.

L'unica telemetria presente nell'app è Firebase Analytics e Crashlytics, usati esclusivamente per il monitoraggio degli errori e per statistiche d'uso anonime.

⸻

🧩 Una vetrina delle funzionalità della libreria Llamatik

L'app mostra alcune delle capacità chiave della libreria sottostante:

✔ Supporto a Kotlin Multiplatform

Scrivi una volta, esegui su Android, iOS, desktop e web.

✔ Inferenza LLM on-device

Tramite un bridge C/C++ personalizzato verso llama.cpp.

✔ Generazione di embedding

Compatibile con modelli come nomic-embed-text-v1.5.

✔ Supporto ai vector store

Crea ricerca semantica locale o funzionalità in stile RAG con vector store leggeri.

✔ Gestione dei modelli

Scarica, carica, scarica dalla memoria, cambia e genera con più modelli quantizzati.

✔ Utility ChatRunner di alto livello

Semplici da usare per lo streaming, la generazione e la gestione della logica delle conversazioni IA.

Per gli sviluppatori che ci leggono: l'app Llamatik è costruita interamente sopra la libreria open source, e il suo codice è un esempio pratico di integrazione di IA locale con KMP in un caso reale.

⸻

📱 Perché un'app?

Anche se la libreria Llamatik è già potente, volevamo offrire un modo per:
•	Permettere agli sviluppatori di vedere la libreria in azione
•	Permettere agli utenti di sperimentare con l'IA locale
•	Permettere alla community di testare i modelli con facilità
•	Permettere a chiunque di provare gli assistenti IA offline senza alcuna configurazione
•	Permettere a chi è alle prime armi di capire come è strutturata un'app IA multipiattaforma

L'app Llamatik ti offre un ambiente completo e curato per scoprire cosa possono fare gli LLM locali su un telefono.

⸻

🗺 Cosa arriverà prossimamente?

Stiamo già lavorando a:
•	🔍 Ricerca vettoriale integrata per PDF e documenti
•	📁 Importazione di modelli locali dall'archiviazione dei file
•	🌐 Più provider e dimensioni di modelli
•	🎛 Impostazioni avanzate della chat (temperature, top-k, ecc.)
•	🧱 Pipeline RAG on-device
•	🛠 Esempi e tutorial per sviluppatori

Il tuo feedback ci aiuterà a definire la roadmap, quindi non esitare a dirci cosa ne pensi!

⸻

🐪 Prova Llamatik oggi stesso

Ora puoi scaricare l'[app Llamatik su Google Play](https://play.google.com/store/apps/details?id=com.llamatik.app.android) e presto anche su App Store per iOS.

È gratuita, aperta, offline e pensata con attenzione a privacy e prestazioni.

Che tu sia uno sviluppatore che crea nuove funzionalità basate sull'IA o un utente che vuole un assistente IA locale e veloce, Llamatik è qui per te.

👉 Esplora. Impara. Crea.
👉 Scopri cosa è possibile quando l'IA gira interamente sul tuo dispositivo.