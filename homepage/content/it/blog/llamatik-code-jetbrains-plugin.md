---
title: "Presentiamo Llamatik Code: un assistente di programmazione IA orientato alla privacy per IntelliJ e Android Studio"
date: 2026-05-14T00:00:00-05:00
description: "Llamatik Code è un assistente di programmazione IA local-first per gli IDE basati su IntelliJ. Tutta l'inferenza gira sul tuo computer con modelli GGUF: nessun cloud, nessuna chiave API, nessun dato che lascia il tuo dispositivo."
image: "images/blog/llamatik_code_landing_image.png"
tags: ["Llamatik Code", "IntelliJ", "Android Studio", "AI", "Privacy", "llama.cpp", "GGUF", "Local AI"]
showDate: true
draft: false
---

Fin dal primo giorno costruiamo Llamatik come piattaforma per un'IA privata e on-device. Oggi portiamo la stessa filosofia nel tuo IDE.

**Llamatik Code** è ora disponibile su [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31304-llamatik-code): un assistente di programmazione IA completamente locale e orientato alla privacy per IntelliJ IDEA, Android Studio e l'intera famiglia di IDE JetBrains.

Niente cloud. Niente chiavi API. Nessun dato che lascia il tuo computer. Solo il tuo modello GGUF locale, in esecuzione dentro il tuo IDE.

---

## Perché l'IA locale nel tuo IDE?

La maggior parte degli assistenti di programmazione IA funziona allo stesso modo: il tuo codice viene inviato a un server remoto, un modello nel cloud lo elabora e il risultato torna indietro. Funziona, ma ha dei compromessi:

- Il tuo codice e il contesto lasciano il tuo computer a ogni richiesta
- Dipendi dalla connessione di rete e dalla disponibilità di terze parti
- I costi dell'abbonamento crescono con l'utilizzo
- Gli ambienti aziendali e regolamentati spesso non possono usare affatto l'IA nel cloud

Llamatik Code segue un approccio diverso. Tutto gira sul dispositivo usando [llama.cpp](https://github.com/ggerganov/llama.cpp), lo stesso runtime collaudato che alimenta le app Llamatik per mobile e desktop. Carichi un modello GGUF una volta sola e da quel momento il tuo IDE ha un assistente IA completo che non "telefona mai a casa".

---

## Cosa sa fare Llamatik Code

### Ask Mode — chatta su qualsiasi cosa, senza modificare i file

Fai domande, esplora le API, ottieni spiegazioni, genera snippet. Ask Mode è un'interfaccia conversazionale completa, con cronologia persistente su più sessioni e conoscenza dei tuoi file correnti, del testo selezionato e della struttura del progetto.

Ask Mode è **illimitato nel piano gratuito**: nessun limite giornaliero, nessun credito.

### Agent Mode — lascia che sia il modello a guidare le modifiche al codice

Agent Mode va oltre. Descrivi ciò che vuoi in linguaggio naturale e l'agente propone le modifiche al codice, ti mostra un diff e attende la tua approvazione prima di toccare qualsiasi file.

L'agente conosce il progetto: comprende la struttura dei file, può spostarsi tra i file e concatena più chiamate a strumenti per portare a termine compiti in più passaggi. Il piano gratuito include i compiti dell'agente a sessione singola. Pro sblocca l'esecuzione parallela di più compiti e **Aggressive Mode**, in cui l'agente può prendere decisioni autonome senza chiedere approvazione a ogni passaggio.

### Modifica di più file (Pro)

Applica le modifiche al codice a più file contemporaneamente. Utile per i refactoring, le migrazioni di API e tutto ciò che coinvolge più di un file.

### Integrazione con i server MCP

Llamatik Code supporta il [Model Context Protocol](https://modelcontextprotocol.io/), che ti permette di collegare l'agente a strumenti e servizi esterni. Il piano gratuito supporta un server MCP con approvazione manuale delle azioni. Pro sblocca server MCP illimitati e l'approvazione automatica.

### External Knowledge — ricerca web integrata

Dalla versione 1.5.0, Llamatik Code include un sistema External Knowledge che permette al modello di cercare sul web e leggere la documentazione senza alcuna chiave API.

Usa DuckDuckGo dietro le quinte e comprende le intenzioni dello sviluppatore:

- Domande su Kotlin o sulle Coroutines → cerca su kotlinlang.org
- Domande su Android o Compose → cerca su developer.android.com
- Messaggi di errore → cerca nelle GitHub Issues
- Domande sulle librerie → cerca nei registri dei pacchetti e nei changelog

Ogni risultato è accompagnato da citazioni strutturate: tipo di fonte, livello di affidabilità e URL diretto. Il modello pianifica automaticamente flussi di ricerca in più passaggi e sintetizza i risultati in una risposta coerente.

Il piano gratuito include 5 ricerche al giorno (5 risultati ciascuna). Pro porta il limite a 50 ricerche al giorno con 10 risultati ciascuna e fino a 10 fonti di documentazione private.

### Code Health Monitor

Una modalità di analisi in background che fa emergere i problemi di qualità del codice. Gli utenti gratuiti possono avviarla manualmente; Pro sblocca le modalità automatiche avanzate.

---

## Free vs Pro

| Funzionalità | Free | Pro |
|---|---|---|
| Ask Mode (chat) | Illimitato | Illimitato |
| Sessioni dell'agente | Sessione singola | Multi-task, in parallelo |
| Aggressive agent mode | — | ✓ |
| Applicazione del codice a più file | — | ✓ |
| Server MCP | 1, approvazione manuale | Illimitati, approvazione automatica |
| Ricerche External Knowledge al giorno | 5 | 50 |
| Risultati per ricerca | 5 | 10 |
| Fonti di documentazione private | 1 | 10 |
| Code Health Monitor | Manuale | Automatico avanzato |

Gli abbonamenti Pro sono disponibili su [llamatik.com](https://www.llamatik.com).

---

## Per iniziare

**Requisiti:**
- IntelliJ IDEA 2026.1 o qualsiasi IDE JetBrains con build di piattaforma 252+
- macOS, Linux o Windows (64 bit, JDK 21+)
- Un file di modello GGUF salvato in locale

**Installazione:**
1. Apri il tuo IDE → Settings → Plugins → Marketplace
2. Cerca **Llamatik Code** e installalo
3. Riavvia l'IDE

**Configurazione del modello:**
1. Vai su Settings → Llamatik Code → Inference
2. Indica il percorso del tuo file `.gguf` locale
3. Regola la dimensione del contesto e il numero di thread in base al tuo hardware

Funziona qualsiasi modello GGUF compatibile con llama.cpp. Per la maggior parte dei compiti di sviluppo, un modello instruct da 7B quantizzato Q4_K_M o Q5_K_M è un buon punto di partenza. Per un lavoro più impegnativo con gli agenti o per codebase complesse, un modello da 13B–14B offre un ragionamento nettamente migliore.

Le istruzioni complete di configurazione e i modelli consigliati sono nella [documentazione](https://docs.llamatik.com/llamatik-code/).

---

## Costruito sul motore Llamatik

Llamatik Code è alimentato dalla stessa integrazione di llama.cpp Kotlin-first che muove le app Llamatik per mobile e desktop. Questo significa la stessa qualità di inferenza, la stessa compatibilità con GGUF e le stesse garanzie di privacy, sia che tu stia chattando sul telefono sia che tu stia scrivendo codice nel tuo IDE.

L'accesso alla rete esterna avviene solo tramite server MCP configurati esplicitamente o ricerche External Knowledge. Nient'altro lascia il tuo computer.

---

## Installalo oggi stesso

Llamatik Code è disponibile ora su JetBrains Marketplace.

→ [Installa Llamatik Code](https://plugins.jetbrains.com/plugin/31304-llamatik-code)  
→ [Leggi la documentazione](https://docs.llamatik.com/llamatik-code/)
