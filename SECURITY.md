# Security Policy

## Supported Versions

Llamatik is actively developed, and security updates are provided for recent stable releases.

|                        |           | 
| ---------------------- | --------- |
| Version                | Supported |
| Latest release (v0.x)  | ✅ Yes    |
| Previous minor version | ✅ Yes    |
| Older versions         | ❌ No     |

Only the latest published version (and, when applicable, the immediately previous minor release) receives security fixes. Users are strongly encouraged to upgrade to the latest version.

⸻

## Reporting a Vulnerability

We take security and privacy issues seriously, especially given Llamatik’s focus on on-device, offline AI.

## How to report

If you discover a security vulnerability, please do not open a public GitHub issue.

Instead, report it privately by one of the following methods:

	•	GitHub Security Advisories. Use the “Report a vulnerability” button in the repository’s Security tab (preferred).
	•	Email (fallback). Send a report to: security@llamatik.com (If email is unavailable, use GitHub Security Advisories.)

## What to include

Please include as much of the following as possible:

	•	A clear description of the vulnerability
	•	Steps to reproduce (proof of concept if available)
	•	Affected versions / platforms (Android, iOS, Desktop, library)
	•	Potential impact (e.g. crash, data exposure, code execution)
	•	Any suggested mitigation or fix (optional)

## What to expect
	•	Acknowledgement within 72 hours
	•	Status updates as the issue is investigated
	•	If confirmed, a fix will be prepared and released as soon as reasonably possible
	•	Once fixed, we may coordinate responsible disclosure and credit the reporter (if desired)

## Server connections (not encrypted yet)

The Llamatik app can optionally connect to a self-hosted [Llamatik Server](https://github.com/ferranpons/Llamatik-Server). These connections currently use **plain HTTP and are not encrypted**:

	•	Prompts and responses are not 100% secure; anyone on the same network could potentially read them.
	•	Users should avoid sending passwords or sensitive personal information, and prefer trusted networks (or a TLS reverse proxy in front of the server).
	•	The app shows a warning before connecting to a server.
	•	Encrypted connections are planned; both encrypted and unencrypted modes will be supported.

Reports about this limitation are known and do not need to be submitted as vulnerabilities; reports about other weaknesses in server connectivity are welcome.

## Scope notes
	•	Llamatik runs locally and offline by default; vulnerabilities related to model behavior or prompt content are generally out of scope unless they cause crashes, memory corruption, or unintended data access.
	•	Issues in third-party dependencies (e.g. llama.cpp, platform runtimes) may be tracked upstream, but we will document mitigations when applicable.

⸻

Thank you for helping keep Llamatik safe and trustworthy 💚
