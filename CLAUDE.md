# Instructions for coding agents

Murmel is a voice-first, fully on-device baby log for parents in Germany. Read FEATURES.md for scope and the current milestone. Work on the milestone marked 🔜 unless told otherwise, and tick items off in FEATURES.md when they are done.

## Non-negotiable rules
1. **Nothing leaves the phone.** Do not add the `INTERNET` permission, network libraries, analytics, crash-reporting or ad SDKs. If a feature seems to need the network (e.g. model download), stop and ask first; prefer Play Asset Delivery.
2. **Record, never advise.** The app logs what happened and what the doctor said. No interpretation, diagnosis, "normal ranges", warnings or recommendations, in code, prompts or UI text.
3. **Keep cloud backup disabled** (`allowBackup=false`, `data_extraction_rules.xml`).
4. **German first.** Every user-facing string goes in `res/values/strings.xml` (German) with an English translation in `res/values-en/strings.xml`. No hard-coded strings in Compose.
5. **One-handed, night-friendly UI.** Primary actions in the bottom half of the screen, large touch targets (≥ 56 dp, record button ≥ 96 dp), dark theme default.

## Conventions
- Kotlin, Jetpack Compose, Material 3. MVVM: `ui/<feature>/` holds `XScreen.kt` + `XViewModel.kt`.
- State via `StateFlow`; side effects in coroutines on `viewModelScope`.
- Heavy work (model loading, inference, speech) never on the main thread.
- Dependency versions only in `gradle/libs.versions.toml`.
- No dependency-injection framework yet; pass dependencies via constructors / a simple `AppContainer`.
- Small, focused commits. Each commit must build (`./gradlew assembleDebug`) and pass `./gradlew test`.

## Model / prompt work
- The model's only job is extraction: transcript → JSON matching the log schema.
- Any prompt or model change must be checked against the eval set (`evals/`, once it exists) and the result noted in the PR.
