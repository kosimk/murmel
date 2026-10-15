# Murmel – Feature List

*Working title. Voice-first, fully on-device baby log for parents in Germany.*

**Product promise:** Tap, talk, done. Everything stays on your phone.

**Guardrail for every feature:** Murmel is a faithful private *record*, never a medical advisor. It logs what happened and what the doctor said. It never interprets, diagnoses or recommends.

Legend: ✅ done · 🔜 next · ⬜ planned · 💶 behind the one-time unlock

---

## Milestone 0 — Project setup ✅
- ✅ Kotlin + Jetpack Compose project, Android 12+ (API 31)
- ✅ German as default language, English as second
- ✅ Dark theme by default (night feeds)
- ✅ Home screen with one large, thumb-reachable record button (UI only)
- ✅ Cloud backup and device transfer disabled; no INTERNET permission
- ✅ CI builds a debug APK on every push

## Milestone 1 — Technical spike on the Fairphone 6 🔜
Goal: prove the core pipeline works on a mid-range phone before building real features.
- 🔜 Request microphone permission on first tap
- 🔜 German speech → text with the on-device `SpeechRecognizer`
- 🔜 Show the raw transcript on screen
- ⬜ Run a small model (Gemma 3 1B or Qwen3 0.6B) via LiteRT-LM on the transcript
- ⬜ Return structured JSON (`type`, `time`, `amount`, `side`, `note`, `exam`)
- ⬜ Measure: transcription quality, model latency, phone temperature after 10 runs
- ⬜ Decision: keep built-in speech recognizer or switch to whisper.cpp

## Milestone 2 — MVP (free core) ⬜
### Capture
- ⬜ One-tap voice capture from the home screen
- ⬜ Free-form brain dump → one or more log entries ("fed at 3, left side, then slept an hour" → feed + sleep)
- ⬜ Review screen: confirm or quickly fix what the model understood before saving
- ⬜ Manual fallback: quick tap buttons for feed / diaper / sleep / note
- ⬜ Times understood relative to now ("vor 20 Minuten", "um drei")

### Log types
- ⬜ Feeding (breast left/right, bottle with amount)
- ⬜ Diaper (wet / dirty)
- ⬜ Sleep (start / end / duration)
- ⬜ Free note (anything else)

### Timeline
- ⬜ Chronological day view
- ⬜ "At a glance" header: last feed, last diaper, current awake/sleep time
- ⬜ Edit and delete entries

### German check-ups (U-Untersuchungen)
- ⬜ U1–U9 list with age windows based on the baby's birth date
- ⬜ Gentle reminder when a U-window is coming up
- ⬜ Voice note after a check-up, filed against the right U ("U4 lief gut, Arzt will die Hüfte beobachten")

### Privacy and trust
- ⬜ Encrypted local database (Room + SQLCipher)
- ⬜ Plain-language privacy screen: what is stored, where, and that nothing is sent
- ⬜ Backup to a file the user saves themselves (and restore from it)
- ⬜ App lock (fingerprint / PIN), optional

### Onboarding
- ⬜ Baby's name and birth date
- ⬜ One-screen explanation: tap, talk, it stays on your phone
- ⬜ First-launch model download (or Play Asset Delivery) with clear size info

## Milestone 3 — One-time unlock 💶
*Proposal for the free vs. paid line. The free core stays fully usable forever; no history limits.*
- 💶 Doctor summary export (PDF) for the Kinderarzt visit
- 💶 Multiple children / twins
- 💶 Pregnancy module (Mutterpass / Vorsorge appointments)
- 💶 Home-screen widget and lock-screen quick record
- 💶 Simple patterns view (feeds per day, sleep per day), descriptive only
- 💶 Custom log types (pumping, medication given, tummy time …)
- ⬜ Google Play Billing: one non-consumable in-app product

## Later (v2 and beyond) ⬜
- ⬜ Fine-tuned model for German baby-log extraction (LoRA, ~1,000–5,000 examples; seeded by the eval set)
- ⬜ whisper.cpp speech recognition for consistent quality on all phones
- ⬜ Two-parent sync without a cloud (local Wi-Fi / Bluetooth / QR handoff)
- ⬜ iOS version (Apple Foundation Models)
- ⬜ Other countries with structured check-up systems

## Explicitly out of scope
- ❌ Accounts, cloud sync, server backend
- ❌ Analytics or ad SDKs
- ❌ Medical interpretation, symptom checking, advice of any kind
- ❌ Social or community features
- ❌ Subscriptions

## Quality
- ⬜ Eval set: ~50 realistic German brain dumps → expected JSON; run on every prompt/model change
- ⬜ Unit tests for parsing and U-window calculations
- ⬜ UI test for the capture → review → save flow
