# Contributing to RPG AI Hub

Thank you for your interest in contributing to RPG AI Hub!

## Development Guidelines

1. **Deterministic Game Engine**: Ensure that world mutations are always validated by `WorldConsistencyValidator` and processed by `WorldStateEngine`. Never allow direct unchecked mutations from AI models.
2. **Security First**: Never log or expose API keys.
3. **Jetpack Compose**: Follow Material Design 3 guidelines and theme colors from `Theme.kt`.
4. **Unit Testing**: Add tests for new state engine rules in `app/src/test/java/com/example/`.

## Pull Request Checklist

- [ ] Code compiles without errors via `./gradlew assembleDebug`
- [ ] Unit tests pass via `./gradlew testDebugUnitTest`
- [ ] No hardcoded secrets or API keys
