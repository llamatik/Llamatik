---
title: "چگونه با Kotlin مدل‌های زبانی را به‌صورت آفلاین روی Android اجرا کنیم"
date: 2026-02-11T00:00:00-05:00
description: "این مقاله نشان می‌دهد چگونه با Kotlin و بدون سرور و کلید API، مدل‌های زبانی را آفلاین روی Android اجرا کنید و حریم خصوصی کاربر را کاملاً حفظ کنید؛ با Llamatik، پوششی Kotlin-محور بر llama.cpp."
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

اجرای مدل‌های زبانی بزرگ (LLM) معمولاً یعنی ارسال داده‌های کاربر به فضای ابری. اما روی دستگاه‌های Android امروزی، مدل‌های زبانی می‌توانند کاملاً آفلاین و محلی و با Kotlin اجرا شوند.
این مقاله نشان می‌دهد چگونه با Kotlin و بدون سرور و کلید API، مدل‌های زبانی را آفلاین روی Android اجرا کنید و حریم خصوصی کاربر را کاملاً حفظ کنید؛ با Llamatik، پوششی Kotlin-محور بر llama.cpp.

## چرا مدل‌های زبانی را آفلاین روی Android اجرا کنیم؟

مدل‌های زبانی ابری با بده‌بستان‌هایی همراه‌اند:
- ❌ وابستگی به شبکه
- ❌ تأخیر
- ❌ هزینه‌ی مبتنی بر مصرف
- ❌ خروج داده‌های کاربر از دستگاه

مدل‌های زبانی آفلاین این امکان‌ها را فراهم می‌کنند:
- 📴 برنامه‌های آفلاین‌محور
- 🔐 هوش مصنوعی حافظ حریم خصوصی
- 📱 عملکرد و هزینه‌ی قابل پیش‌بینی

گوشی‌های Android امروزی به‌قدر کافی قدرتمندند؛ حلقه‌ی گمشده ابزارهای خوب Kotlin بوده است.

## llama.cpp: پایه‌ی مدل‌های زبانی روی دستگاه

llama.cpp یک رانتایم C++ با کارایی بالاست که برای اجرای کارآمد مدل‌های زبانی روی CPU طراحی شده است.

از این موارد پشتیبانی می‌کند:
- مدل‌های کوانتیزه‌ی GGUF
- CPUهای ARM (Android)
- بدون وابستگی به GPU یا فضای ابری

اما یکپارچه‌کردن مستقیم C++ در برنامه‌های Android دردسر دارد.
Llamatik همین‌جا به کار می‌آید.

## Llamatik چیست؟

Llamatik کتابخانه‌ای Kotlin-محور است که به توسعه‌دهندگان Android اجازه می‌دهد llama.cpp را با یک API تمیز Kotlin به‌صورت محلی اجرا کنند.

ویژگی‌های کلیدی:
- استنتاج کاملاً آفلاین
- بدون JNI در کد برنامه
- پشتیبانی از Kotlin Multiplatform
- سازگاری با مدل‌های GGUF
- پشتیبانی از streaming و امبدینگ

شما فقط Kotlin می‌نویسید؛ پیچیدگی نیتیو پنهان می‌ماند.

## Llamatik را به پروژه‌ی Android خود اضافه کنید

Llamatik روی Maven Central منتشر شده است.

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

بدون تنظیمات سفارشی Gradle.
بدون پیکربندی NDK.

## یک مدل GGUF اضافه کنید

یک مدل کوانتیزه‌ی GGUF (مثلاً Q4) دانلود کنید و آن را در این مسیر قرار دهید:
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

مدل‌های کوچک‌تر سریع‌تر بارگذاری می‌شوند و روی دستگاه‌های موبایل بهتر کار می‌کنند.

## مدل را بارگذاری کنید

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

این کار مدل را از assets کپی می‌کند و در حافظه‌ی نیتیو بارگذاری می‌کند.

## تولید متن آفلاین

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

همین و بس.
بدون اینترنت.
بدون کلید API.
بدون فراخوانی ابری.

## تولید به‌صورت streaming (توصیه‌شده)

برای رابط‌های کاربری چت‌مانند:
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

این روش به‌خوبی با موارد زیر کار می‌کند:
- Jetpack Compose
- StateFlow
- ViewModelها

## امبدینگ و RAG آفلاین

Llamatik از امبدینگ هم پشتیبانی می‌کند و جست‌وجوی آفلاین و کاربردهای RAG را ممکن می‌سازد.

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

امبدینگ‌ها را به‌صورت محلی ذخیره کنید تا قابلیت‌های هوش مصنوعی کاملاً آفلاین بسازید.

## نکاتی درباره‌ی عملکرد

مدل‌های زبانی روی دستگاه محدودیت‌هایی دارند:
- از مدل‌های کوانتیزه استفاده کنید
- انتظار پاسخ‌های کندتر از GPUهای ابری را داشته باشید
- حافظه را با دقت مدیریت کنید

اما برای:
- قابلیت‌های کمکی
- promptهای کوتاه
- کارهای تخصصی یک حوزه

عملکرد بیش از کافی است.

## چه زمانی از مدل‌های زبانی آفلاین استفاده کنیم؟

Llamatik زمانی ایده‌آل است که به این موارد نیاز دارید:
- پشتیبانی آفلاین
- تضمین‌های قوی حریم خصوصی
- هزینه‌های قابل پیش‌بینی
- یکپارچگی نزدیک با رابط کاربری

جایگزین مدل‌های بزرگ ابری نیست؛ هوش مصنوعی لبه است، درست و اصولی.

## خودتان امتحان کنید
- GitHub: [https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- وب‌سایت و برنامه‌ی نمایشی: [https://llamatik.com](https://llamatik.com)
- llama.cpp: [https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## سخن پایانی
اجرای آفلاین مدل‌های زبانی روی Android با Kotlin دیگر آزمایشی نیست.
با ابزارهای درست، توسعه‌دهندگان Kotlin می‌توانند هوش مصنوعی خصوصی، آفلاین و روی دستگاه بسازند، بی‌آنکه به C++ دست بزنند.
اگر درباره‌ی هوش مصنوعی موبایل فراتر از فضای ابری کنجکاوید، این نقطه‌ی شروع خوبی است.


نخستین‌بار در [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) و [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g) منتشر شده است.