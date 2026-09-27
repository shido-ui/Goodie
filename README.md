# RPG AI Hub ⚔️🎲

An Android-first persistent AI RPG and roleplaying platform with an **authoritative deterministic game engine**.

---

## 🌟 Overview

**RPG AI Hub** bridges narrative roleplay with game simulation. Unlike naive wrappers where language models arbitrarily invent gold or teleport players, RPG AI Hub enforces a strict architectural contract:

> **The AI proposes. The game engine decides.**

All state mutations (damage, healing, gold transactions, inventory changes, location movements, quest milestones, faction standings, relationship shifts) must pass through schema validation, rule checking, and the authoritative Living World Director before atomic persistence.

---

## 🎮 Gameplay Modes

1. **Open World RPG**: Freely explore living realms (Eldoria High Fantasy, Neo-Veridia Cyberpunk, Ravenloft Gothic) where simulated NPCs follow daily schedules, factions compete for influence, and rumors spread dynamically.
2. **Character Chat**: Deep 1-on-1 dialogue and relationship simulation with specialized companions or antagonists.
3. **Open World + Companion**: Journey through the open world alongside a chosen companion who observes and reacts to events.

---

## 🏛️ Architecture & Data Flow

```
[User Input]
     ↓
[SessionManager]
     ↓
[Rerouter] ──→ (Extracts nearby NPCs, active quests, relevant memories, lore)
     ↓
[NarrativeDirector] ──→ (Builds bounded prompt with authoritative facts)
     ↓
[AI Provider (Gemini / OpenAI / Custom)]
     ↓
[Structured JSON Response]
     ↓
[StateProposalExtractor]
     ↓
[WorldConsistencyValidator] ──→ (Enforces HP/Gold bounds, inventory, geography)
     ↓
[WorldStateEngine] ──→ (Applies validated state mutations)
     ↓
[WorldPulseEngine] ──→ (Advances time, NPC schedules, pending consequences)
     ↓
[Atomic Database Transaction (Room)]
     ↓
[Reactive Compose UI (M3)]
```

---

## 🔐 API Key Security (BYOK)

- **Encrypted in Android Keystore / Encrypted Vault**: Secrets are never saved in plaintext `SharedPreferences`, never written to logcat, never included in exports or backups, and never committed to Git.
- **Masked in UI**: Keys are displayed as `••••••••` or masked preview `sk-...4a91`.
- **Zero Hardcoded Secrets**: Bring Your Own Key (BYOK).

---

## 🤖 AI Provider Setup

### 1. Google Gemini (Recommended)
- **Provider ID**: `gemini`
- **Endpoint**: `https://generativelanguage.googleapis.com/v1beta/openai/chat/completions`
- **Model**: `gemini-2.5-flash` (or `gemini-2.5-pro`, `gemini-1.5-flash`)
- **API Key**: Enter your Gemini API key securely in **AI Settings** in the app.

> ⚠️ **Warning**: Never commit your Gemini API key to GitHub or source code.

### 2. OpenAI
- **Provider ID**: `openai`
- **Endpoint**: `https://api.openai.com/v1/chat/completions`
- **Model**: `gpt-4o` or `gpt-4o-mini`

### 3. Local / Custom (Ollama, LM Studio)
- **Endpoint**: `http://10.0.2.2:11434/v1/chat/completions` (Android Emulator loopback)
- **Model**: `llama3`, `mistral`, `phi3`

---

## 🛠️ Building & Testing

### Build Debug APK
```bash
./gradlew assembleDebug
```
Output APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

---

## 📦 Backup & Restore

Campaign worlds can be exported to JSON and restored atomically:
- **JSON Validation**: Checks format and schema version (v2).
- **Integrity Validation**: Verifies entity uniqueness and rule bounds before committing.
- **Rollback Safety**: If any check fails, the existing database save remains untouched.

---

## 📜 License
Licensed under the Apache License, Version 2.0.
