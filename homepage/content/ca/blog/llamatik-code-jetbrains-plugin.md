---
title: "Presentem Llamatik Code: un assistent de programació amb IA que prioritza la privacitat per a IntelliJ i Android Studio"
date: 2026-05-14T00:00:00-05:00
description: "Llamatik Code és un assistent de programació amb IA, local per disseny, per als IDE basats en IntelliJ. Tota la inferència s'executa a la teva màquina amb models GGUF: sense núvol, sense claus d'API i sense que cap dada surti del dispositiu."
image: "images/blog/llamatik_code_landing_image.png"
tags: ["Llamatik Code", "IntelliJ", "Android Studio", "AI", "Privacy", "llama.cpp", "GGUF", "Local AI"]
showDate: true
draft: false
---

Des del primer dia hem estat construint Llamatik com una plataforma d'IA privada i al dispositiu. Avui portem aquesta mateixa filosofia al teu IDE.

**Llamatik Code** ja és disponible al [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31304-llamatik-code): un assistent de programació amb IA totalment local i que prioritza la privacitat, per a IntelliJ IDEA, Android Studio i tota la família d'IDE de JetBrains.

Sense núvol. Sense claus d'API. Sense que cap dada surti de la teva màquina. Només el teu model GGUF local, executant-se dins del teu IDE.

---

## Per què IA local al teu IDE?

La majoria d'assistents de programació amb IA funcionen igual: el teu codi s'envia a un servidor remot, un model al núvol el processa i el resultat torna. Funciona, però té contrapartides:

- El teu codi i el context surten de la teva màquina a cada petició
- Dependeixes de la connexió de xarxa i de la disponibilitat de tercers
- El cost de la subscripció creix amb l'ús
- Els entorns empresarials i regulats sovint no poden utilitzar IA al núvol de cap manera

Llamatik Code adopta un enfocament diferent. Tot s'executa al dispositiu amb [llama.cpp](https://github.com/ggerganov/llama.cpp), el mateix runtime contrastat que fa funcionar les apps de Llamatik per a mòbil i escriptori. Carregues un model GGUF una sola vegada i, a partir d'aquest moment, el teu IDE té un assistent d'IA plenament capaç que mai no es connecta enlloc.

---

## Què pot fer Llamatik Code

### Ask Mode: xateja sobre qualsevol cosa, sense canvis als fitxers

Fes preguntes, explora APIs, obtén explicacions, genera fragments de codi. Ask Mode és una interfície conversacional completa, amb historial persistent de diverses sessions i coneixement dels teus fitxers actuals, del text seleccionat i de l'estructura del projecte.

Ask Mode és **il·limitat al nivell gratuït**: sense límit diari, sense crèdits.

### Agent Mode: deixa que el model condueixi els canvis de codi

Agent Mode va més enllà. Descrius el que vols en llenguatge natural i l'agent proposa edicions de codi, et mostra un diff i espera la teva aprovació abans de tocar cap fitxer.

L'agent coneix el projecte: entén l'estructura dels teus fitxers, pot navegar entre fitxers i encadena diverses crides a eines per completar tasques de diversos passos. El nivell gratuït inclou tasques d'agent d'una sola sessió. Pro desbloqueja l'execució paral·lela de diverses tasques i **Aggressive Mode**, on l'agent pot prendre decisions de manera autònoma sense demanar aprovació a cada pas.

### Edició de diversos fitxers (Pro)

Aplica canvis de codi a diversos fitxers alhora. Útil per a refactoritzacions, migracions d'API i qualsevol cosa que afecti més d'un fitxer.

### Integració de servidors MCP

Llamatik Code és compatible amb el [Model Context Protocol](https://modelcontextprotocol.io/), cosa que et permet connectar l'agent a eines i serveis externs. El nivell gratuït admet un servidor MCP amb aprovació manual de les accions. Pro desbloqueja servidors MCP il·limitats i l'aprovació automàtica.

### Coneixement extern: cerca web integrada

Des de la versió 1.5.0, Llamatik Code inclou un sistema de coneixement extern (External Knowledge) que permet al model cercar a la web i llegir documentació sense cap clau d'API.

Fa servir DuckDuckGo per sota i entén la intenció del desenvolupador:

- Preguntes sobre Kotlin o Coroutines → cerca a kotlinlang.org
- Preguntes sobre Android o Compose → cerca a developer.android.com
- Missatges d'error → cerca a GitHub Issues
- Preguntes sobre llibreries → cerca als registres de paquets i als registres de canvis

Cada resultat inclou cites estructurades: tipus de font, nivell de confiança i URL directa. El model planifica automàticament fluxos de recerca de diversos passos i sintetitza les troballes en una resposta coherent.

El nivell gratuït inclou 5 cerques al dia (5 resultats cadascuna). Pro ho eleva a 50 cerques al dia amb 10 resultats cadascuna i fins a 10 fonts de documentació privades.

### Code Health Monitor

Un mode d'anàlisi en segon pla que posa de manifest problemes de qualitat del codi. Els usuaris gratuïts el poden executar manualment; Pro desbloqueja modes automàtics avançats.

---

## Gratuït vs Pro

| Funcionalitat | Gratuït | Pro |
|---|---|---|
| Ask Mode (xat) | Il·limitat | Il·limitat |
| Sessions d'agent | Una sola sessió | Multitasca, en paral·lel |
| Mode d'agent agressiu | — | ✓ |
| Aplicació de codi a diversos fitxers | — | ✓ |
| Servidors MCP | 1, aprovació manual | Il·limitats, aprovació automàtica |
| Cerques de coneixement extern/dia | 5 | 50 |
| Resultats per cerca | 5 | 10 |
| Fonts de documentació privades | 1 | 10 |
| Code Health Monitor | Manual | Automàtic avançat |

Les subscripcions Pro estan disponibles a [llamatik.com](https://www.llamatik.com).

---

## Primers passos

**Requisits:**
- IntelliJ IDEA 2026.1 o qualsevol IDE de JetBrains amb la build de plataforma 252+
- macOS, Linux o Windows (64 bits, JDK 21+)
- Un fitxer de model GGUF desat en local

**Instal·lació:**
1. Obre el teu IDE → Settings → Plugins → Marketplace
2. Cerca **Llamatik Code** i instal·la'l
3. Reinicia l'IDE

**Configuració del model:**
1. Ves a Settings → Llamatik Code → Inference
2. Apunta al teu fitxer `.gguf` local
3. Ajusta la mida del context i el nombre de fils segons el teu maquinari

Qualsevol model GGUF compatible amb llama.cpp funciona. Per a la majoria de tasques de desenvolupament, un model instruct quantitzat de 7B en Q4_K_M o Q5_K_M és un bon punt de partida. Per a feina d'agent més exigent o bases de codi complexes, un model de 13B–14B ofereix un raonament notablement millor.

Les instruccions completes de configuració i les recomanacions de models són a la [documentació](https://docs.llamatik.com/llamatik-code/).

---

## Construït sobre el motor de Llamatik

Llamatik Code funciona amb la mateixa integració de llama.cpp feta primer de tot per a Kotlin que mou les apps de Llamatik per a mòbil i escriptori. Això vol dir la mateixa qualitat d'inferència, la mateixa compatibilitat amb GGUF i les mateixes garanties de privacitat, tant si xateges des del telèfon com si edites codi al teu IDE.

L'accés extern a la xarxa només es produeix mitjançant servidors MCP configurats explícitament o cerques de coneixement extern. Res més surt de la teva màquina.

---

## Instal·la'l avui mateix

Llamatik Code ja és disponible al JetBrains Marketplace.

→ [Instal·la Llamatik Code](https://plugins.jetbrains.com/plugin/31304-llamatik-code)  
→ [Llegeix la documentació](https://docs.llamatik.com/llamatik-code/)
