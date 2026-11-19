---
title: "Presentamos la app Llamatik: un chat de IA sin conexión en tu móvil"
date: 2025-11-25T09:00:00-05:00
description: "Nos complace anunciar el lanzamiento de la app Llamatik, un chatbot de IA totalmente sin conexión creado para mostrar la potencia y versatilidad de la biblioteca Llamatik para Kotlin Multiplatform. Tanto si eres un desarrollador curioso por la IA en el dispositivo como un usuario que busca un chatbot privado, rápido y moderno, la app Llamatik lleva todo el potencial de los LLM ligeros directamente a tu móvil."
image: "images/blog/llamatik_new_app_hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "App"]
showDate: true
draft: false
---

🚀 Presentamos la app Llamatik: chat de IA sin conexión en tu móvil

Nos complace anunciar el lanzamiento de la app Llamatik, un chatbot de IA totalmente sin conexión creado para mostrar la potencia y versatilidad de la biblioteca Llamatik para Kotlin Multiplatform.
Tanto si eres un desarrollador curioso por la IA en el dispositivo como un usuario que busca un chatbot privado, rápido y moderno, la app Llamatik lleva todo el potencial de los LLM ligeros directamente a tu móvil.

⸻

🔍 ¿Qué es Llamatik?

Llamatik es una biblioteca de Kotlin Multiplatform que permite a los desarrolladores ejecutar grandes modelos de lenguaje (LLM) locales y embeddings vectoriales en Android, iOS, escritorio y la web.

Con el lanzamiento de la app Llamatik, ya puedes probar todo lo que ofrece la biblioteca sin escribir ni una sola línea de código.

La app funciona como demostración en vivo, como entorno de pruebas y como implementación de referencia para los desarrolladores que crean experiencias de IA local.

⸻

⚡ Qué puede hacer la app Llamatik

🧠 Chat de IA totalmente local

Toda la generación de texto se ejecuta al 100 % en tu dispositivo con modelos de código abierto cuantizados.
No hay nube, no hay servidor y no se envía ningún dato a ninguna parte.
Esto hace que Llamatik sea:
•	Privado
•	Rápido
•	Siempre disponible
•	Apto para entornos con requisitos estrictos de privacidad

La interfaz del chat es sencilla, limpia y está creada con Jetpack Compose Multiplatform.

⸻

📦 Descargador de modelos integrado

La app incluye descarga directa de varios modelos optimizados, entre ellos:
•	Gemma 3 270M
•	SmolVLM 256M / 500M (visión + lenguaje)
•	Phi-1.5
•	Qwen 2.5 5B (cuantizado)
•	Llama 3.2 1B (cuantizado)

Todos los modelos están en formato GGUF y son compatibles con el backend de llama.cpp integrado en Llamatik.

Puedes descargar, cambiar y gestionar los modelos directamente desde la app.

⸻

🎯 Sugerencias inteligentes y acciones rápidas

Para que la experiencia sea más intuitiva, la app ofrece:
•	Prompts sugeridos
•	Acciones rápidas
•	Una interfaz limpia y fluida
•	Una píldora para elegir el modelo sobre el cuadro de entrada
•	Introducción guiada integrada

Estos pequeños detalles hacen que la app sea ideal tanto para principiantes como para usuarios avanzados.

⸻

🔐 La privacidad, lo primero

Toda la generación se hace en local.
Tus conversaciones:
•	No se recopilan
•	No se almacenan en el exterior
•	No se registran ni se envían a ningún servidor

Incluso la inferencia del modelo se hace sin conexión, con llama.cpp compilado para móvil.

La única telemetría de la app es Firebase Analytics y Crashlytics, que se usan estrictamente para el seguimiento de errores y para obtener datos de uso anónimos.

⸻

🧩 Un escaparate de las funciones de la biblioteca Llamatik

La app muestra varias capacidades clave de la biblioteca en la que se basa:

✔ Compatibilidad con Kotlin Multiplatform

Se crea una vez y se ejecuta en Android, iOS, escritorio y la web.

✔ Inferencia de LLM en el dispositivo

A través de un puente personalizado en C/C++ hacia llama.cpp.

✔ Generación de embeddings

Compatible con modelos como nomic-embed-text-v1.5.

✔ Compatibilidad con almacenes de vectores

Crea búsqueda semántica local o funciones de tipo RAG con almacenes de vectores ligeros.

✔ Gestión de modelos

Descarga, carga, descarga de memoria, cambio y generación con varios modelos cuantizados.

✔ Utilidades de alto nivel con ChatRunner

Facilitan el streaming, la generación y la lógica de las conversaciones con la IA.

Para los desarrolladores que lean esto: la app Llamatik está construida íntegramente sobre la biblioteca de código abierto, y su código es un ejemplo práctico de integración de IA local con KMP en el mundo real.

⸻

📱 ¿Por qué una app?

Aunque la biblioteca Llamatik ya es potente, queríamos ofrecer una forma de que:
•	Los desarrolladores vean la biblioteca en acción
•	Los usuarios experimenten con la IA local
•	La comunidad pruebe modelos con facilidad
•	Cualquiera pueda probar asistentes de IA sin conexión sin configurar nada
•	Los recién llegados entiendan cómo se estructura una app de IA multiplataforma

La app Llamatik te ofrece un entorno completo y pulido para explorar lo que los LLM locales pueden hacer en un móvil.

⸻

🗺 ¿Qué viene después?

Ya estamos trabajando en:
•	🔍 Búsqueda vectorial integrada para PDF y documentos
•	📁 Importación local de modelos desde el almacenamiento de archivos
•	🌐 Más proveedores y tamaños de modelos
•	🎛 Ajustes avanzados del chat (temperatura, top-k, etc.)
•	🧱 Pipelines de RAG en el dispositivo
•	🛠 Ejemplos y tutoriales para desarrolladores

Tus comentarios ayudarán a definir la hoja de ruta, así que ¡cuéntanos qué opinas!

⸻

🐪 Prueba Llamatik hoy

Ya puedes descargar la [app Llamatik en Google Play](https://play.google.com/store/apps/details?id=com.llamatik.app.android) y, muy pronto, en la App Store de iOS.

Es gratuita, abierta, funciona sin conexión y está creada pensando en la privacidad y el rendimiento.

Tanto si eres un desarrollador que crea nuevas funciones con IA como un usuario que quiere un asistente de IA rápido y local, Llamatik está aquí para ti.

👉 Explora. Aprende. Crea.
👉 Descubre lo que es posible cuando la IA se ejecuta por completo en tu dispositivo.
