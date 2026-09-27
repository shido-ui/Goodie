# Changelog

All notable changes to **RPG AI Hub** will be documented in this file.

## [2.0.0] - 2026-09-27

### Added
- **Authoritative World State Engine**: Complete deterministic state engine for HP, gold, inventory, and geography.
- **Living World Director**: Autonomous NPC schedules, goals, and Level-of-Detail simulation.
- **WorldPulseEngine**: Advances persistent world time, NPC movements, and pending delayed consequences.
- **AI Provider Abstraction**: BYOK Google Gemini (`gemini-2.5-flash`), OpenAI (`gpt-4o`), and custom local endpoints.
- **Single Source of Truth Model Configuration**: Solved the "No model" configuration bug with unified `ModelConfigRepository`.
- **Encrypted Keystore Vault**: Secure storage of user API keys in Android Keystore.
- **Atomic Save & Backup System**: Transactional Room DB storage and multi-phase validated import/export.
- **Full Jetpack Compose & Material 3 UI**: Dark RPG theme, live HUD header, dice roller, character sheet, world codex, and debug telemetry.
