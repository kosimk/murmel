# Instructions for coding agents

Murmel is a voice-first, fully on-device companion for expecting parents in Germany: it catches questions for the doctor, Hebamme and partner, records what was said, and organizes appointments. A baby log follows later (Milestone 4). Read FEATURES.md for scope and the current milestone; background and process live in `docs/` (brief, SYLLABUS, QUALITY). Work on the milestone marked 🔜 unless told otherwise, and tick items off in FEATURES.md when they are done.

## Non-negotiable rules
1. **Nothing leaves the phone.** Do not add the `INTERNET` permission, network libraries, analytics, crash-reporting or ad SDKs. If a feature seems to need the network (e.g. model download), stop and ask first; prefer Play Asset Delivery.
2. **Record, never advise.** The app collects the user's questions and records what happened and what the doctor or Hebamme said. It never answers a question, judges urgency, or gives interpretation, diagnosis, "normal ranges", warnings or recommendations, in code, prompts or UI text. The only safety text is the one fixed, hand-written acute-symptoms notice in FEATURES.md.
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
- The model's only job is extraction: transcript → JSON matching the item schema in FEATURES.md (Milestone 1). It never writes text shown to the user as advice.
- Any prompt or model change must be checked against the eval set (`evals/`, once it exists) and the result noted in the PR.
