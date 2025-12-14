# MCPlugin

![Plugin][badge]

Ein simples Vanish/Utility-Plugin für Paper-Server (Kotlin).

---

## Inhaltsverzeichnis
- [Features](#features)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Konfiguration & Hinweise](#konfiguration--hinweise)
- [FAQ](#faq)

---

## Features
- Vanish (ein-/ausschalten) mit Alias `/v`
- Ping-Befehl für schnelle Verbindungstest-Antwort
- Persistenz über SQLite (lokal)

---

## Installation
1. Die JAR in dein Paper `plugins/`-Verzeichnis legen.
2. Server neu starten.

---

## Commands

### /ping
```
/ping
```
- Beschreibung: Antwortet mit Pong.
- Permission: `mcplugin.command.ping` (default: true)

### /vanish
```
/vanish
```
- Beschreibung: Schaltet Vanish ein/aus.
- Alias: `/v`
- Permission: `mcplugin.command.vanish` (default: op)

---

## Permissions
| Permission | Beschreibung |
|---|---|
| `mcplugin.command.ping` | Erlaubt die Nutzung des Ping-Befehls |
| `mcplugin.command.vanish` | Erlaubt Vanish ein-/auszuschalten |
| `mcplugin.vanish.see` | Erlaubt, vanished Spieler zu sehen |
| `mcplugin.vanish.notify` | Erhält Vanish-Notify-Meldungen |

Hinweis: `mcplugin.command.ping` ist standardmäßig für alle Spieler aktiviert; Vanish-rechte sind standardmäßig nur für Operatoren (`op`) gesetzt.

---

## Konfiguration & Hinweise
- Plugin-Metadaten und Commands sind in `src/main/resources/plugin.yml` definiert.
- Datenbank: SQLite wird als lokale Persistenz genutzt (`org.xerial:sqlite-jdbc`).
- Packaging: `maven-shade-plugin` ist konfiguriert; Minimierung ist aktiviert, aber auf zentrale Bibliotheken beschränkt (z. B. kotlin-stdlib, sqlite-jdbc), um Laufzeitprobleme zu vermeiden.
- Kotlin-JVM-Target: aktuell auf JVM 17 eingestellt (`pom.xml`). Wenn du Java 21 nutzen möchtest, passe die Werte in `pom.xml` (`maven.compiler.target`, `kotlin.compiler.jvmTarget`) an.

---

## FAQ
Q: Ich sehe Spieler nicht, obwohl sie vanished sind — was tun?

A: Prüfe, ob du die Permission `mcplugin.vanish.see` besitzt.

---

[badge]: https://img.shields.io/badge/Plugin-MCPlugin-blue
