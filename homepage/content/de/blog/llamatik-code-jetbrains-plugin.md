---
title: "Llamatik Code vorgestellt: ein KI-Coding-Assistent mit Fokus auf Privatsphäre für IntelliJ und Android Studio"
date: 2026-05-14T00:00:00-05:00
description: "Llamatik Code ist ein Local-first-KI-Coding-Assistent für IntelliJ-basierte IDEs. Die gesamte Inferenz läuft mit GGUF-Modellen auf deinem Rechner – keine Cloud, keine API-Keys, keine Daten, die dein Gerät verlassen."
image: "images/blog/llamatik_code_landing_image.png"
tags: ["Llamatik Code", "IntelliJ", "Android Studio", "AI", "Privacy", "llama.cpp", "GGUF", "Local AI"]
showDate: true
draft: false
---

Seit dem ersten Tag bauen wir Llamatik als Plattform für private KI auf dem Gerät. Heute bringen wir dieselbe Philosophie in deine IDE.

**Llamatik Code** ist jetzt im [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31304-llamatik-code) verfügbar – ein vollständig lokaler KI-Coding-Assistent mit Fokus auf Privatsphäre für IntelliJ IDEA, Android Studio und die gesamte JetBrains-IDE-Familie.

Keine Cloud. Keine API-Keys. Keine Daten, die deinen Rechner verlassen. Nur dein lokales GGUF-Modell, das in deiner IDE läuft.

---

## Warum lokale KI in deiner IDE?

Die meisten KI-Coding-Assistenten funktionieren gleich: Dein Code wird an einen entfernten Server geschickt, ein Cloud-Modell verarbeitet ihn, und das Ergebnis kommt zurück. Das funktioniert – hat aber seinen Preis:

- Dein Code und dein Kontext verlassen bei jeder Anfrage deinen Rechner
- Du bist auf Netzwerkverbindung und die Verfügbarkeit Dritter angewiesen
- Die Abo-Kosten wachsen mit der Nutzung
- In Unternehmen und regulierten Umgebungen ist Cloud-KI oft gar nicht erlaubt

Llamatik Code geht einen anderen Weg. Alles läuft auf dem Gerät mit [llama.cpp](https://github.com/ggerganov/llama.cpp) – derselben praxiserprobten Runtime, die auch die Llamatik-Apps für Mobilgeräte und Desktop antreibt. Du lädst einmal ein GGUF-Modell, und ab da hat deine IDE einen voll leistungsfähigen KI-Assistenten, der nie nach Hause telefoniert.

---

## Das kann Llamatik Code

### Ask Mode — über alles chatten, ohne Dateiänderungen

Stell Fragen, erkunde APIs, lass dir Dinge erklären, generiere Snippets. Der Ask Mode ist eine vollwertige Chat-Oberfläche mit dauerhaftem Verlauf über mehrere Sitzungen, die deine aktuellen Dateien, markierten Text und die Projektstruktur kennt.

Der Ask Mode ist **im kostenlosen Tarif unbegrenzt** – ohne Tageslimit, ohne Credits.

### Agent Mode — das Modell steuert die Codeänderungen

Der Agent Mode geht weiter. Du beschreibst in natürlicher Sprache, was du willst, und der Agent schlägt Codeänderungen vor, zeigt dir einen Diff und wartet auf deine Freigabe, bevor er eine Datei anfasst.

Der Agent kennt dein Projekt: Er versteht deine Dateistruktur, kann dateiübergreifend navigieren und verkettet mehrere Tool-Aufrufe, um mehrstufige Aufgaben zu erledigen. Der kostenlose Tarif enthält Agent-Aufgaben in einer einzelnen Sitzung. Pro schaltet die parallele Ausführung mehrerer Aufgaben frei sowie den **Aggressive Mode** – hier kann der Agent eigenständig Entscheidungen treffen, ohne bei jedem Schritt um Freigabe zu bitten.

### Bearbeitung mehrerer Dateien (Pro)

Wende Codeänderungen auf mehrere Dateien gleichzeitig an. Praktisch für Refactorings, API-Migrationen und alles, was mehr als eine Datei betrifft.

### MCP-Server-Integration

Llamatik Code unterstützt das [Model Context Protocol](https://modelcontextprotocol.io/), mit dem du den Agenten an externe Tools und Dienste anbinden kannst. Der kostenlose Tarif unterstützt einen MCP-Server mit manueller Freigabe von Aktionen. Pro schaltet unbegrenzt viele MCP-Server und die automatische Freigabe frei.

### External Knowledge — Websuche integriert

Seit Version 1.5.0 enthält Llamatik Code ein External-Knowledge-System, mit dem das Modell im Web suchen und Dokumentation lesen kann – ganz ohne API-Key.

Es nutzt im Hintergrund DuckDuckGo und erkennt die Absicht hinter Entwicklerfragen:

- Fragen zu Kotlin oder Coroutines → Suche auf kotlinlang.org
- Fragen zu Android oder Compose → Suche auf developer.android.com
- Fehlermeldungen → Suche in GitHub Issues
- Fragen zu Bibliotheken → Suche in Paketregistern und Changelogs

Jedes Ergebnis kommt mit strukturierten Quellenangaben – Quellentyp, Konfidenzniveau und direkte URL. Das Modell plant mehrstufige Recherche-Workflows automatisch und fasst die Ergebnisse zu einer stimmigen Antwort zusammen.

Der kostenlose Tarif enthält 5 Suchen pro Tag (je 5 Ergebnisse). Pro erhöht das auf 50 Suchen pro Tag mit je 10 Ergebnissen und bis zu 10 privaten Dokumentationsquellen.

### Code Health Monitor

Ein Analysemodus im Hintergrund, der Probleme bei der Codequalität aufdeckt. Im kostenlosen Tarif startest du ihn manuell; Pro schaltet erweiterte automatisierte Modi frei.

---

## Free vs. Pro

| Funktion | Free | Pro |
|---|---|---|
| Ask Mode (Chat) | Unbegrenzt | Unbegrenzt |
| Agent-Sitzungen | Einzelne Sitzung | Mehrere Aufgaben, parallel |
| Aggressive Agent Mode | — | ✓ |
| Code in mehreren Dateien anwenden | — | ✓ |
| MCP-Server | 1, manuelle Freigabe | Unbegrenzt, automatische Freigabe |
| External-Knowledge-Suchen/Tag | 5 | 50 |
| Ergebnisse pro Suche | 5 | 10 |
| Private Dokumentationsquellen | 1 | 10 |
| Code Health Monitor | Manuell | Erweitert, automatisiert |

Pro-Abos gibt es auf [llamatik.com](https://www.llamatik.com).

---

## Los geht's

**Voraussetzungen:**
- IntelliJ IDEA 2026.1 oder eine beliebige JetBrains-IDE ab Plattform-Build 252
- macOS, Linux oder Windows (64-Bit, JDK 21+)
- Eine lokal gespeicherte GGUF-Modelldatei

**Installation:**
1. Öffne deine IDE → Settings → Plugins → Marketplace
2. Suche nach **Llamatik Code** und installiere es
3. Starte die IDE neu

**Modell einrichten:**
1. Gehe zu Settings → Llamatik Code → Inference
2. Verweise auf deine lokale `.gguf`-Datei
3. Passe Kontextgröße und Thread-Anzahl an deine Hardware an

Jedes mit llama.cpp kompatible GGUF-Modell funktioniert. Für die meisten Entwicklungsaufgaben ist ein quantisiertes 7B-Instruct-Modell in Q4_K_M oder Q5_K_M ein guter Startpunkt. Für aufwendigere Agent-Arbeit oder komplexe Codebasen bietet ein Modell mit 13B–14B spürbar besseres Reasoning.

Die vollständige Einrichtungsanleitung und Modellempfehlungen findest du in der [Dokumentation](https://docs.llamatik.com/llamatik-code/).

---

## Gebaut auf der Llamatik-Engine

Llamatik Code wird von derselben Kotlin-first-llama.cpp-Integration angetrieben wie die Llamatik-Apps für Mobilgeräte und Desktop. Das heißt: dieselbe Inferenzqualität, dieselbe GGUF-Kompatibilität und dieselben Datenschutzgarantien – ob du auf dem Handy chattest oder in deiner IDE Code bearbeitest.

Externer Netzwerkzugriff findet nur über ausdrücklich konfigurierte MCP-Server oder External-Knowledge-Suchen statt. Sonst verlässt nichts deinen Rechner.

---

## Installiere es noch heute

Llamatik Code ist ab sofort im JetBrains Marketplace verfügbar.

→ [Llamatik Code installieren](https://plugins.jetbrains.com/plugin/31304-llamatik-code)  
→ [Dokumentation lesen](https://docs.llamatik.com/llamatik-code/)
