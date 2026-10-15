# Murmel

Voice-first, fully on-device baby log for parents in Germany. *(Working title.)*

Tap, talk, done. Speech recognition and the small language model that turns a brain dump into log entries both run on the phone. Nothing is sent anywhere.

- Feature list and roadmap: [FEATURES.md](FEATURES.md)
- Instructions for coding agents: [CLAUDE.md](CLAUDE.md)

## Getting started

1. Install [Android Studio](https://developer.android.com/studio) (latest stable).
2. **File → Open** and choose this folder. Let Gradle sync; it downloads the Android SDK pieces and dependencies on first run.
   If Studio suggests updating the Android Gradle Plugin or library versions, accepting is fine.
3. On your phone: enable **Developer options → USB debugging**, connect it, and press **Run ▶**.

Command line alternative (needs the Android SDK installed and `ANDROID_HOME` set):

```bash
./gradlew assembleDebug        # build
./gradlew installDebug         # install on a connected phone
./gradlew test                 # unit tests
```

## Tech stack

| Area | Choice |
|---|---|
| Language / UI | Kotlin, Jetpack Compose, Material 3 |
| Min. Android | 12 (API 31), required for on-device speech recognition |
| Speech → text | Android on-device `SpeechRecognizer` (whisper.cpp as fallback) |
| Text → log entries | LiteRT-LM + Gemma 3 1B / Qwen3 0.6B, constrained JSON output |
| Storage | Room + SQLCipher (planned) |
| Payments | Google Play Billing, one-time unlock (planned) |
| CI | GitHub Actions builds a debug APK on every push |

## Privacy by construction

- No `INTERNET` permission in the manifest.
- Cloud backup and device-to-device transfer are disabled.
- No accounts, no analytics, no ad SDKs.
