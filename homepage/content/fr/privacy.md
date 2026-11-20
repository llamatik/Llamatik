---
type: "doc"
title: "Politique de confidentialité"
date: 2026-11-20T00:00:00+00:00
---

> Cette politique n'est disponible qu'en anglais. Seule la version anglaise fait foi.

<!--
  DRAFT FOR OWNER / LEGAL REVIEW. Before publishing:
  1. Confirm the Firebase retention settings quoted in section 4 match the Firebase console.
  2. Re-read section 5: it lists the third-party requests the website makes today (only GitHub Pages hosting).
-->

Last updated: November 20, 2026

## In short

- Llamatik runs AI **on your device**. Your prompts, chats, documents, images and voice are processed locally and are **not sent to us**.
- There is **no account** and there are **no ads**.
- The Android app can send **crash reports and basic usage statistics, but only if you turn that on** in Settings. It is **off by default**.
- This website uses **no cookies and no analytics**.

## 1. Who we are

Llamatik (the apps, the Kotlin SDK, the self-hosted server, the Llamatik Code plugin and this website, llamatik.com) is developed by **Ferran Pons**, who is the controller of the personal data described in this policy.

- Email: [info@llamatik.com](mailto:info@llamatik.com)
- Postal address: Tr. Sant Pere, 60, B2, 08392 Sant Andreu de Llavaneres (Barcelona), Spain

## 2. What stays on your device

Everything you create or load in Llamatik is stored on your device only, and we have no access to it:

- prompts, chats and chat history;
- documents you add for question answering;
- voice recordings, which are transcribed on the device with Whisper;
- images and videos you analyze or generate;
- downloaded models and your settings.

If you enable **Agent Mode** and grant the corresponding Android permissions, the assistant can use your calendar, contacts, alarms, notifications and clipboard. These actions run on your device and only with the permissions you grant; you can revoke them at any time in your system settings.

## 3. When data does leave your device

Llamatik only connects to the network for the things below, and only when you use them.

- **Downloading models.** The in-app catalog downloads models from Hugging Face (huggingface.co); you can also enter another address. The server you download from receives your IP address and standard request details, and its own privacy policy applies. We do not see these requests.
- **News on the home screen.** The app fetches the Llamatik blog feed from www.llamatik.com, which is hosted as described in section 5.
- **Servers and other devices you connect to.** If you connect the app to a Llamatik Server, or to another device with **Share this device**, your prompts and the generated replies are sent to that server or device. We do not operate these servers: whoever runs one controls it. These connections currently use **plain, unencrypted HTTP**, so use them only on networks you trust.
- **The web app (llamatik.com/prompt/).** It runs entirely in your browser using WebAssembly. Models you download are stored in your browser's own storage on your device. When you use the prompt box on the home page, your text is passed to the web app in the URL fragment (the part after `#`), which browsers never send to any server.
- **Diagnostics (Android only, optional).** See section 4.

## 4. Diagnostics: optional, Android only

The Android app includes Google Firebase **Crashlytics** and **Analytics**. They are **disabled until you turn on "Share diagnostics" in Settings**. The iOS, desktop and web versions do not include them.

If you turn it on, the app sends:

- **Crash reports:** the technical details of a crash (stack trace), device model, operating system and app version, the state of the app at the time of the crash, and an installation identifier.
- **Basic usage statistics:** app sessions, screens viewed, app version, device model, operating system, language, the approximate country derived from your IP address, and a pseudonymous app-instance identifier.

We do not create custom events about what you do in the app, and the app is not designed to send your prompts, chats, documents, voice or generated content. We do not use the advertising ID, advertising features or ad personalization, and the app does not request the advertising-ID permission.

**Legal basis:** your consent (Article 6(1)(a) GDPR). **Withdrawing consent:** turn the switch off. Collection stops immediately, data that was not yet sent is discarded, and the app-instance identifier is reset, so if you turn it on again you appear as a new, unrelated installation. Data that Google has already received is deleted when its retention period ends (Firebase keeps crash reports for 90 days; Analytics event data is kept for the retention period configured in our Firebase project, two months by default).

Google acts as our processor for this data and may process it outside the European Economic Area under the EU–US Data Privacy Framework and standard contractual clauses. See [Firebase privacy and security](https://firebase.google.com/support/privacy).

Separately from Llamatik, Google Play and the App Store collect their own information about installs and crashes under their own policies.

## 5. This website

- **No cookies, no analytics, no advertising and no tracking pixels.** Nothing on this site identifies or follows you between visits.
- **Hosting.** The site is served by GitHub Pages (GitHub, Inc., a Microsoft company). When you load a page, GitHub receives your IP address and standard request data and keeps server logs under its own policy ([GitHub Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement)). We do not receive these logs.
- **Fonts.** Fonts are served from our own site, so no font provider is contacted.
- **GitHub statistics.** The star count and download links for the latest release are fetched when the site is built, not from your browser, so visiting the site does not contact the GitHub API.
- **Quotes.** The home page quotes a few public Google Play reviews, with the reviewer's first name only. If you wrote one and want it removed, email us.
- **Links.** Download buttons and other links lead to Google Play, the App Store, GitHub and other sites, which have their own privacy policies.
- **Email.** If you write to us, we use your email address and message only to reply, and keep them for as long as needed to handle your request.

## 6. Llamatik Code and the self-hosted server

**Llamatik Code**, our JetBrains IDE plugin, has its own [privacy policy](/llamatik-code/privacy.html). It runs models locally; features that search the web, such as External Knowledge, send your search query to the search provider when you use them.

The **Llamatik Server** and the **SDK** are software you run yourself. The libraries do not send data to us. If you deploy a server for other people, you are responsible for how you handle their data.

## 7. Why we process data, and who receives it

- **Diagnostics:** your consent.
- **Running and securing the website, and answering your messages:** our legitimate interest in operating the project and helping its users.

We do not sell personal data and we do not share it for advertising. The only recipients are the service providers named above: Google (Firebase), GitHub (hosting), Hugging Face and the other hosts you choose to download from, and our email provider.

## 8. Your rights

Under the GDPR and similar laws you can ask us for access to, correction, deletion, restriction or portability of your personal data, object to its processing, and withdraw consent at any time. You can also lodge a complaint with your local data protection authority.

To exercise these rights, write to [info@llamatik.com](mailto:info@llamatik.com). Because Llamatik has no accounts and collects diagnostics only in a pseudonymous form, we usually cannot identify data that belongs to you; turning the diagnostics switch off is the quickest way to stop collection and reset your identifier.

## 9. Children

Llamatik is not directed at children under 13 (or the minimum age in your country). We do not knowingly collect personal data from children.

## 10. Security

Keeping processing on your device is our main safeguard. No system is perfectly secure: protect your device, and remember that connections to servers and other devices are currently unencrypted.

## 11. Changes

We may update this policy. The date at the top shows the latest version, and we will describe material changes in the app or on our blog.

## 12. Contact

Questions about this policy: [info@llamatik.com](mailto:info@llamatik.com).
