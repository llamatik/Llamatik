---
title: "Presentamos Llamatik Code: un asistente de programación con IA que prioriza la privacidad para IntelliJ y Android Studio"
date: 2026-05-14T00:00:00-05:00
description: "Llamatik Code es un asistente de programación con IA, local por diseño, para los IDE basados en IntelliJ. Toda la inferencia se ejecuta en tu equipo con modelos GGUF: sin nube, sin claves de API y sin que tus datos salgan de tu dispositivo."
image: "images/blog/llamatik_code_landing_image.png"
tags: ["Llamatik Code", "IntelliJ", "Android Studio", "AI", "Privacy", "llama.cpp", "GGUF", "Local AI"]
showDate: true
draft: false
---

Desde el primer día hemos construido Llamatik como una plataforma de IA privada y en el dispositivo. Hoy llevamos esa misma filosofía a tu IDE.

**Llamatik Code** ya está disponible en el [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/31304-llamatik-code): un asistente de programación con IA totalmente local y que prioriza la privacidad, para IntelliJ IDEA, Android Studio y toda la familia de IDE de JetBrains.

Sin nube. Sin claves de API. Sin que tus datos salgan de tu equipo. Solo tu modelo GGUF local, ejecutándose dentro de tu IDE.

---

## ¿Por qué IA local en tu IDE?

La mayoría de los asistentes de programación con IA funcionan igual: tu código se envía a un servidor remoto, un modelo en la nube lo procesa y el resultado vuelve a ti. Funciona, pero tiene contrapartidas:

- Tu código y su contexto salen de tu equipo en cada petición
- Dependes de la conexión de red y de la disponibilidad de terceros
- El coste de las suscripciones crece con el uso
- Los entornos empresariales y regulados a menudo no pueden usar IA en la nube

Llamatik Code adopta otro enfoque. Todo se ejecuta en el dispositivo con [llama.cpp](https://github.com/ggerganov/llama.cpp), el mismo runtime probado en miles de situaciones que impulsa las apps de Llamatik para móvil y escritorio. Cargas un modelo GGUF una vez y, a partir de ese momento, tu IDE cuenta con un asistente de IA plenamente capaz que nunca se conecta a ningún servidor.

---

## Qué puede hacer Llamatik Code

### Ask Mode: chatea sobre lo que quieras, sin modificar archivos

Haz preguntas, explora APIs, obtén explicaciones y genera fragmentos de código. Ask Mode es una interfaz conversacional completa, con historial persistente de varias sesiones y conocimiento de tus archivos actuales, del texto seleccionado y de la estructura de tu proyecto.

Ask Mode es **ilimitado en el plan gratuito**: sin tope diario, sin créditos.

### Agent Mode: deja que el modelo lleve las riendas de los cambios de código

Agent Mode va más allá. Describes lo que quieres en lenguaje natural y el agente propone cambios en el código, te muestra un diff y espera tu aprobación antes de tocar ningún archivo.

El agente conoce tu proyecto: entiende la estructura de archivos, puede moverse entre ellos y encadena varias llamadas a herramientas para completar tareas de varios pasos. El plan gratuito incluye tareas de agente de una sola sesión. Pro desbloquea la ejecución paralela de varias tareas y el **Aggressive Mode**, en el que el agente puede tomar decisiones de forma autónoma sin pedir aprobación en cada paso.

### Edición de varios archivos (Pro)

Aplica cambios de código en varios archivos a la vez. Resulta útil para refactorizaciones, migraciones de APIs y cualquier cambio que afecte a más de un archivo.

### Integración con servidores MCP

Llamatik Code es compatible con el [Model Context Protocol](https://modelcontextprotocol.io/), que te permite conectar el agente con herramientas y servicios externos. El plan gratuito admite un servidor MCP con aprobación manual de las acciones. Pro desbloquea servidores MCP ilimitados y la aprobación automática.

### Conocimiento externo: búsqueda web integrada

Desde la versión 1.5.0, Llamatik Code incluye un sistema de conocimiento externo (External Knowledge) que permite al modelo buscar en la web y leer documentación sin ninguna clave de API.

Usa DuckDuckGo por debajo y entiende la intención del desarrollador:

- Preguntas sobre Kotlin o Coroutines → busca en kotlinlang.org
- Preguntas sobre Android o Compose → busca en developer.android.com
- Mensajes de error → busca en GitHub Issues
- Preguntas sobre bibliotecas → busca en registros de paquetes y changelogs

Cada resultado incluye citas estructuradas: tipo de fuente, nivel de confianza y URL directa. El modelo planifica automáticamente flujos de investigación de varios pasos y sintetiza los hallazgos en una respuesta coherente.

El plan gratuito incluye 5 búsquedas al día (5 resultados cada una). Pro lo eleva a 50 búsquedas al día con 10 resultados cada una y hasta 10 fuentes de documentación privadas.

### Code Health Monitor

Un modo de análisis en segundo plano que detecta problemas de calidad del código. Los usuarios gratuitos pueden ejecutarlo manualmente; Pro desbloquea modos automáticos avanzados.

---

## Gratis frente a Pro

| Función | Gratis | Pro |
|---|---|---|
| Ask Mode (chat) | Ilimitado | Ilimitado |
| Sesiones de agente | Una sola sesión | Multitarea, en paralelo |
| Modo de agente agresivo | — | ✓ |
| Aplicación de código en varios archivos | — | ✓ |
| Servidores MCP | 1, aprobación manual | Ilimitados, aprobación automática |
| Búsquedas de conocimiento externo/día | 5 | 50 |
| Resultados por búsqueda | 5 | 10 |
| Fuentes de documentación privadas | 1 | 10 |
| Code Health Monitor | Manual | Automático avanzado |

Las suscripciones Pro están disponibles en [llamatik.com](https://www.llamatik.com).

---

## Primeros pasos

**Requisitos:**
- IntelliJ IDEA 2026.1 o cualquier IDE de JetBrains con build de plataforma 252+
- macOS, Linux o Windows (64 bits, JDK 21+)
- Un archivo de modelo GGUF almacenado en local

**Instalación:**
1. Abre tu IDE → Settings → Plugins → Marketplace
2. Busca **Llamatik Code** e instálalo
3. Reinicia el IDE

**Configuración del modelo:**
1. Ve a Settings → Llamatik Code → Inference
2. Indica la ruta de tu archivo `.gguf` local
3. Ajusta el tamaño de contexto y el número de hilos según tu hardware

Funciona cualquier modelo GGUF compatible con llama.cpp. Para la mayoría de las tareas de desarrollo, un modelo instruct de 7B cuantizado en Q4_K_M o Q5_K_M es un buen punto de partida. Para un trabajo de agente más exigente o para bases de código complejas, un modelo de 13B–14B ofrece un razonamiento claramente mejor.

Las instrucciones completas de configuración y las recomendaciones de modelos están en la [documentación](https://docs.llamatik.com/llamatik-code/).

---

## Construido sobre el motor de Llamatik

Llamatik Code funciona con la misma integración de llama.cpp, pensada primero para Kotlin, que impulsa las apps de Llamatik para móvil y escritorio. Eso significa la misma calidad de inferencia, la misma compatibilidad con GGUF y las mismas garantías de privacidad, tanto si chateas en tu móvil como si editas código en tu IDE.

El acceso externo a la red solo se produce a través de servidores MCP configurados de forma explícita o de búsquedas de conocimiento externo. Nada más sale de tu equipo.

---

## Instálalo hoy

Llamatik Code ya está disponible en el JetBrains Marketplace.

→ [Instalar Llamatik Code](https://plugins.jetbrains.com/plugin/31304-llamatik-code)  
→ [Leer la documentación](https://docs.llamatik.com/llamatik-code/)
