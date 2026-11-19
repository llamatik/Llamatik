---
title: "Как запустить LLM офлайн на Android с помощью Kotlin"
date: 2026-02-11T00:00:00-05:00
description: "В этой статье показано, как запускать LLM офлайн на Android с помощью Kotlin — без серверов, без API-ключей и с полной приватностью пользователей — на примере Llamatik, Kotlin-обёртки над llama.cpp."
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

Запуск больших языковых моделей (LLM) обычно означает отправку пользовательских данных в облако. Но на современных Android-устройствах LLM могут работать полностью офлайн и локально — на Kotlin.
В этой статье показано, как запускать LLM офлайн на Android с помощью Kotlin — без серверов, без API-ключей и с полной приватностью пользователей — на примере Llamatik, Kotlin-обёртки над llama.cpp.

## Зачем запускать LLM офлайн на Android?

У облачных LLM есть свои минусы:
- ❌ Зависимость от сети
- ❌ Задержки
- ❌ Оплата по объёму использования
- ❌ Пользовательские данные покидают устройство

Офлайн-LLM дают:
- 📴 Офлайн-first приложения
- 🔐 ИИ с сохранением приватности
- 📱 Предсказуемые производительность и стоимость

Современные Android-смартфоны достаточно мощные — не хватало только хороших инструментов для Kotlin.

## llama.cpp: основа для LLM на устройстве

llama.cpp — это высокопроизводительный C++-рантайм, созданный для эффективного запуска LLM на CPU.

Он поддерживает:
- Квантизованные модели GGUF
- ARM-процессоры (Android)
- Работу без GPU и без облака

Но встраивать C++ напрямую в Android-приложения — настоящая боль.
Тут на помощь приходит Llamatik.

## Что такое Llamatik?

Llamatik — это библиотека, ориентированная на Kotlin, которая позволяет Android-разработчикам запускать llama.cpp локально через чистый Kotlin API.

Ключевые возможности:
- Полностью офлайн-инференс
- Никакого JNI в коде приложения
- Поддержка Kotlin Multiplatform
- Совместимость с моделями GGUF
- Поддержка streaming и эмбеддингов

Вы пишете только на Kotlin — нативная сложность остаётся скрытой.

## Добавьте Llamatik в свой Android-проект

Llamatik опубликован на Maven Central.

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

Никакой особой настройки Gradle.
Никакой конфигурации NDK.

## Добавьте модель GGUF

Скачайте квантизованную модель GGUF (например, Q4) и поместите её в:
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

Небольшие модели загружаются быстрее и лучше подходят для мобильных устройств.

## Загрузите модель

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

Это копирует модель из assets и загружает её в нативную память.

## Генерация текста офлайн

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

Вот и всё.
Без интернета.
Без API-ключей.
Без обращений к облаку.

## Streaming-генерация (рекомендуется)

Для интерфейсов в стиле чата:
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

Это без проблем работает с:
- Jetpack Compose
- StateFlow
- ViewModel

## Эмбеддинги и офлайн-RAG

Llamatik также поддерживает эмбеддинги, что открывает путь к офлайн-поиску и сценариям RAG.

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

Храните эмбеддинги локально и создавайте полностью офлайн-функции на базе ИИ.

## Замечания о производительности

У LLM на устройстве есть ограничения:
- Используйте квантизованные модели
- Ждите более медленных ответов, чем на облачных GPU
- Внимательно управляйте памятью

Но для:
- Вспомогательных функций
- Коротких промптов
- Узкоспециализированных задач

Производительности более чем достаточно.

## Когда стоит использовать офлайн-LLM?

Llamatik идеален, когда вам нужны:
- Поддержка офлайн-режима
- Надёжные гарантии приватности
- Предсказуемые расходы
- Тесная интеграция с UI

Это не замена большим облачным моделям, а правильно сделанный edge AI.

## Попробуйте сами
- GitHub: [https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- Сайт и демо-приложение: [https://llamatik.com](https://llamatik.com)
- llama.cpp: [https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## Напоследок
Запуск LLM офлайн на Android с помощью Kotlin больше не эксперимент.
С правильными инструментами Kotlin-разработчики могут создавать приватный офлайн-ИИ прямо на устройстве — не прикасаясь к C++.
Если вам интересен мобильный ИИ за пределами облака, это отличное место для старта.


Первоначально опубликовано на [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) и [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g).
