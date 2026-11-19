---
title: "Cómo ejecutar LLM sin conexión en Android con Kotlin"
date: 2026-02-11T00:00:00-05:00
description: "Este artículo explica cómo ejecutar LLM sin conexión en Android con Kotlin, sin servidores, sin claves de API y con total privacidad para el usuario, usando Llamatik, un wrapper de llama.cpp pensado para Kotlin."
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

Ejecutar grandes modelos de lenguaje (LLM) suele implicar enviar los datos del usuario a la nube. Pero en los dispositivos Android modernos, los LLM pueden funcionar por completo sin conexión, en local, con Kotlin.
Este artículo explica cómo ejecutar LLM sin conexión en Android con Kotlin, sin servidores, sin claves de API y con total privacidad para el usuario, usando Llamatik, un wrapper de llama.cpp pensado para Kotlin.

## ¿Por qué ejecutar LLM sin conexión en Android?

Los LLM en la nube tienen sus inconvenientes:
- ❌ Dependencia de la red
- ❌ Latencia
- ❌ Coste según el uso
- ❌ Los datos del usuario salen del dispositivo

Los LLM sin conexión permiten:
- 📴 Apps que funcionan primero sin conexión
- 🔐 IA que protege la privacidad
- 📱 Rendimiento y costes predecibles

Los teléfonos Android modernos son lo bastante potentes; lo que faltaba eran buenas herramientas para Kotlin.

## llama.cpp: la base de los LLM en el dispositivo

llama.cpp es un runtime de C++ de alto rendimiento diseñado para ejecutar LLM de forma eficiente en CPU.

Es compatible con:
- Modelos GGUF cuantizados
- CPU ARM (Android)
- Sin depender de GPU ni de la nube

Pero integrar C++ directamente en apps Android es un dolor de cabeza.
Ahí es donde entra Llamatik.

## ¿Qué es Llamatik?

Llamatik es una biblioteca pensada primero para Kotlin que permite a los desarrolladores de Android ejecutar llama.cpp en local mediante una API limpia de Kotlin.

Características principales:
- Inferencia totalmente sin conexión
- Sin JNI en el código de tu app
- Compatible con Kotlin Multiplatform
- Compatibilidad con modelos GGUF
- Compatible con streaming y embeddings

Tú solo escribes Kotlin; la complejidad nativa queda oculta.

## Añade Llamatik a tu proyecto Android

Llamatik está publicado en Maven Central.

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

Sin configuración personalizada de Gradle.
Sin configurar el NDK.

## Añade un modelo GGUF

Descarga un modelo GGUF cuantizado (por ejemplo, Q4) y colócalo en:
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

Los modelos más pequeños cargan más rápido y funcionan mejor en dispositivos móviles.

## Carga el modelo

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

Esto copia el modelo desde los assets y lo carga en la memoria nativa.

## Genera texto sin conexión

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

Eso es todo.
Sin internet.
Sin claves de API.
Sin llamadas a la nube.

## Generación en streaming (recomendada)

Para interfaces de tipo chat:
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

Esto funciona sin fricciones con:
- Jetpack Compose
- StateFlow
- ViewModels

## Embeddings y RAG sin conexión

Llamatik también admite embeddings, lo que permite búsqueda y casos de uso de RAG sin conexión.

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

Guarda los embeddings en local para crear funciones de IA totalmente sin conexión.

## Notas sobre el rendimiento

Los LLM en el dispositivo tienen sus límites:
- Usa modelos cuantizados
- Espera respuestas más lentas que con las GPU de la nube
- Gestiona la memoria con cuidado

Pero para:
- Funciones de asistencia
- Prompts cortos
- Tareas de un dominio concreto

El rendimiento es más que suficiente.

## ¿Cuándo usar LLM sin conexión?

Llamatik es ideal cuando necesitas:
- Funcionamiento sin conexión
- Sólidas garantías de privacidad
- Costes predecibles
- Una integración estrecha con la interfaz

No sustituye a los grandes modelos de la nube: es IA en el borde bien hecha.

## Pruébalo tú mismo
- GitHub: [https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- Sitio web y app de demostración: [https://llamatik.com](https://llamatik.com)
- llama.cpp: [https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## Reflexiones finales
Ejecutar LLM sin conexión en Android con Kotlin ya no es algo experimental.
Con las herramientas adecuadas, los desarrolladores de Kotlin pueden crear IA privada, sin conexión y en el dispositivo, sin tocar C++.
Si te interesa la IA móvil más allá de la nube, este es un gran punto de partida.


Publicado originalmente en [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) y [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g).
