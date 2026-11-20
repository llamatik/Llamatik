---
title: "Kotlin से Android पर LLM ऑफ़लाइन कैसे चलाएँ"
date: 2026-02-11T00:00:00-05:00
description: "यह लेख बताता है कि Kotlin से Android पर LLM ऑफ़लाइन कैसे चलाएँ — बिना सर्वर, बिना API key और पूरी यूज़र प्राइवेसी के साथ — Llamatik की मदद से, जो llama.cpp के ऊपर बना Kotlin-फ़र्स्ट रैपर है।"
image: "images/blog/how-to-llms-on-android-hero.png"
tags: ["Kotlin", "Multiplatform", "LLM", "llama.cpp", "Offline AI", "Android"]
showDate: true
draft: false
---

Large Language Models (LLM) चलाने का आम मतलब है यूज़र का डेटा क्लाउड पर भेजना। लेकिन आधुनिक Android डिवाइस पर LLM पूरी तरह ऑफ़लाइन, लोकली, Kotlin से चल सकते हैं।
यह लेख बताता है कि Kotlin से Android पर LLM ऑफ़लाइन कैसे चलाएँ — बिना सर्वर, बिना API key और पूरी यूज़र प्राइवेसी के साथ — Llamatik की मदद से, जो llama.cpp के ऊपर बना Kotlin-फ़र्स्ट रैपर है।

## Android पर LLM ऑफ़लाइन क्यों चलाएँ?

क्लाउड-आधारित LLM के साथ कुछ समझौते करने पड़ते हैं:
- ❌ नेटवर्क पर निर्भरता
- ❌ लेटेंसी
- ❌ उपयोग के हिसाब से खर्च
- ❌ यूज़र का डेटा डिवाइस से बाहर जाना

ऑफ़लाइन LLM से संभव होता है:
- 📴 ऑफ़लाइन-फ़र्स्ट ऐप्स
- 🔐 प्राइवेसी की रक्षा करने वाला AI
- 📱 अनुमानित प्रदर्शन और खर्च

आधुनिक Android फ़ोन काफ़ी ताकतवर हैं — कमी थी तो अच्छे Kotlin टूलिंग की।

## llama.cpp: ऑन-डिवाइस LLM की बुनियाद

llama.cpp एक उच्च-प्रदर्शन C++ रनटाइम है, जिसे CPU पर LLM कुशलता से चलाने के लिए बनाया गया है।

यह सपोर्ट करता है:
- क्वांटाइज़्ड GGUF मॉडल
- ARM CPU (Android)
- GPU या क्लाउड पर कोई निर्भरता नहीं

लेकिन C++ को सीधे Android ऐप्स में जोड़ना काफ़ी झंझट भरा है।
यहीं Llamatik काम आता है।

## Llamatik क्या है?

Llamatik एक Kotlin-फ़र्स्ट लाइब्रेरी है, जो Android डेवलपर को साफ़-सुथरे Kotlin API से llama.cpp को लोकली चलाने देती है।

मुख्य विशेषताएँ:
- पूरी तरह ऑफ़लाइन इन्फ़रेंस
- ऐप कोड में JNI नहीं
- Kotlin Multiplatform सपोर्ट
- GGUF मॉडल संगतता
- स्ट्रीमिंग और एम्बेडिंग सपोर्ट

आप सिर्फ़ Kotlin लिखते हैं — नेटिव जटिलता पर्दे के पीछे छिपी रहती है।

## अपने Android प्रोजेक्ट में Llamatik जोड़ें

Llamatik Maven Central पर उपलब्ध है।

```
dependencies {
 implementation("com.llamatik:library:0.12.0")
}
```

कोई कस्टम Gradle सेटअप नहीं।
कोई NDK कॉन्फ़िगरेशन नहीं।

## GGUF मॉडल जोड़ें

कोई क्वांटाइज़्ड GGUF मॉडल (जैसे Q4) डाउनलोड करें और उसे यहाँ रखें:
```
androidMain/assets/
└── phi-2.Q4_0.gguf
```

छोटे मॉडल जल्दी लोड होते हैं और मोबाइल डिवाइस पर बेहतर चलते हैं।

## मॉडल लोड करें

```
val modelPath = LlamaBridge.getModelPath("phi-2.Q4_0.gguf")
LlamaBridge.initGenerateModel(modelPath)
```

यह मॉडल को assets से कॉपी करके नेटिव मेमोरी में लोड कर देता है।

## ऑफ़लाइन टेक्स्ट जनरेट करें

```
val response = LlamaBridge.generate(
 "Explain Kotlin Multiplatform in one sentence."
)
```

बस इतना ही।
इंटरनेट नहीं।
API key नहीं।
क्लाउड कॉल नहीं।

## स्ट्रीमिंग जनरेशन (अनुशंसित)

चैट-स्टाइल UI के लिए:
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

यह इनके साथ बिना किसी दिक्कत के काम करता है:
- Jetpack Compose
- StateFlow
- ViewModels

## एम्बेडिंग और ऑफ़लाइन RAG

Llamatik एम्बेडिंग को भी सपोर्ट करता है, जिससे ऑफ़लाइन सर्च और RAG जैसे उपयोग संभव होते हैं।

```
LlamaBridge.initModel(modelPath)
val embedding = LlamaBridge.embed("On-device AI with Kotlin")
```

एम्बेडिंग को लोकली स्टोर करके पूरी तरह ऑफ़लाइन AI फ़ीचर बनाएँ।

## प्रदर्शन से जुड़ी बातें

ऑन-डिवाइस LLM की अपनी सीमाएँ हैं:
- क्वांटाइज़्ड मॉडल इस्तेमाल करें
- क्लाउड GPU की तुलना में जवाब धीमे मिलेंगे
- मेमोरी का ध्यान से प्रबंधन करें

लेकिन इन कामों के लिए:
- असिस्टिव फ़ीचर
- छोटे प्रॉम्प्ट
- किसी खास डोमेन के काम

प्रदर्शन ज़रूरत से कहीं बेहतर है।

## ऑफ़लाइन LLM कब इस्तेमाल करें?

Llamatik तब आदर्श है जब आपको चाहिए:
- ऑफ़लाइन सपोर्ट
- प्राइवेसी की मज़बूत गारंटी
- अनुमानित खर्च
- UI के साथ गहरा इंटीग्रेशन

यह बड़े क्लाउड मॉडल का विकल्प नहीं है — यह एज AI को सही ढंग से करने का तरीका है।

## खुद आज़माएँ
- GitHub: [https://github.com/ferranpons/llamatik](https://github.com/ferranpons/llamatik)
- वेबसाइट और डेमो ऐप: [https://llamatik.com](https://llamatik.com)
- llama.cpp: [https://github.com/ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)

## अंतिम विचार
Kotlin से Android पर LLM को ऑफ़लाइन चलाना अब प्रयोग की चीज़ नहीं रहा।
सही टूल के साथ Kotlin डेवलपर C++ को छुए बिना प्राइवेट, ऑफ़लाइन, ऑन-डिवाइस AI बना सकते हैं।
अगर आप क्लाउड से आगे मोबाइल AI के बारे में जानना चाहते हैं, तो शुरुआत के लिए यह एक बढ़िया जगह है।


मूल रूप से [Medium](https://medium.com/@ferranpons/how-to-run-llms-offline-on-android-using-kotlin-2d2fe848b300) और [Dev.to](https://dev.to/ferranpons/how-to-run-llms-offline-on-android-using-kotlin-407g) पर प्रकाशित।
