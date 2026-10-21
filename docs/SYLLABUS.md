# Murmel – Learning Syllabus

Gated learning path for the builder (background: HTML/CSS/JS, Angular, Java Spring ~2022).
Rule: an agent starts a milestone only after the builder has passed the previous gate.
Method per unit: (1) chapter with Murmel examples, live demos + Angular/Spring bridge, (2) agent builds a small real piece, builder reviews the diff and explains it back, (3) gate questions.

Interactive syllabus (chapters open inside it): https://claude.ai/artifact/D5eB1UGjkfNdRm2JRVephR
Chapters are published as fragments `chapters/<code>.html` alongside the page; new chapters are added the same way.
Chapters written: K1 Kotlin · A1 Android anatomy · C1 Compose UI · C2 State · K2 Coroutines.
Next chapter to write: K3 Flow. Remaining chapters are written when the builder reaches each stage, so they match the code that exists then.
(The first standalone State page, https://claude.ai/artifact/CRAtzkpHPECbEtDyKfLPmv, is superseded by chapter C2.)

## Stage 0 — Foundations (Milestone 0, already built)
- K1 Kotlin for Java/TS devs (deep) — chapter ✅
- A1 Anatomy of an Android app: Activity lifecycle, manifest, resources, Gradle (working) — chapter ✅
- C1 Compose UI building blocks: layouts, Modifier, Material 3, previews (deep) — chapter ✅
- C2 State in Compose (deep) — chapter ✅, unit understood ✅
**Gate 0 → M1:** no-INTERNET manifest; remember vs rotation; why RecordButton is stateless. Exercise: review an agent-built "At a glance" header line by line.
**Gate 0 passed ✅ (2026-10-21).** Exercise adapted to the pregnancy app (SSW, next appointment, open questions). Gaps closed during the gate: INTERNET is not granted unless declared; padding inside vs. outside a Surface; `?.let` is safe navigation.

## Stage 1 — Technical spike (Milestone 1)
- K2 Coroutines & threads (deep) — chapter ✅
- K3 Flow: StateFlow, callbackFlow, collectAsStateWithLifecycle (deep) — next chapter
- A2 Runtime permissions: RECORD_AUDIO (working)
- S1 SpeechRecognizer, on-device German (working)
- L1 Small LLM on device: LiteRT-LM, quantization, prompting (deep)
- L2 Reliable JSON: kotlinx.serialization, validation, eval set, parser tests (deep)
- A3 Measuring on device: Logcat, profiler, thermal status (working)
**Gate 1 → M2:** model never on Main; callbackFlow/awaitClose; malformed JSON handling; StateFlow vs Flow. Exercise: builder makes the speech/model decision from the measurements.

## Stage 2 — MVP (Milestone 2)
- R1 Architecture layers, single source of truth (deep)
- R2 Hilt DI (working)
- D1 Room: entities, DAO, Flow queries, migrations (deep)
- D2 SQLCipher, Keystore, BiometricPrompt (working)
- U1 Navigation Compose (working)
- T1 java.time, relative dates, pregnancy week (SSW) from due date, DST (deep)
- B1 WorkManager + notifications (working)
- F1 Backup/restore via Storage Access Framework (working)
- L3 Shipping the model via Play Asset Delivery (working)
- Q1 Testing: JUnit, Turbine, Compose UI tests, eval in CI (deep)
- U2 Accessibility & de/en strings (working)
- R3 Release on Google Play: signing, R8, Data safety (working)
**Gate 2 → M3:** trace an entry through every layer/thread; missing migration; appointment at 02:30 on 25 Oct 2026 (DST); SSW on the day after the due date; lost-phone threat model. Exercise: restore an old-version backup on a fresh install.

## Stage 3 — One-time unlock (Milestone 3)
- P1 Play Billing, non-consumable, offline restore (working)
- P2 Appointment summary PDF, record not advice (working)
- P3 Glance widgets, quick record (working)
**Gate 3 → v1:** unlock on a new offline phone; PDF wording guardrail. Exercise: write the birth-handoff migration plan (pregnancy → baby log) before code.

## Stage 4 — Baby log (Milestone 4)
Units are planned when Milestone 3 ships. Expected: U-windows from the birth date, log-type schema migration, P4 multiple children (migration of existing data).

## Later (v2+, ungated)
LoRA fine-tuning · whisper.cpp/NDK/JNI · cloud-free two-parent sync · iOS (Swift, Foundation Models)

## Plan changes from this syllabus (applied to FEATURES.md on 2026-10-16)
1. Coroutines before Flow.
2. No first-launch model download (would need INTERNET) → Play Asset Delivery only.
3. Parser tests and the first ~20 eval brain dumps are part of Milestone 1.

## Plan changes from the pregnancy-first pivot (2026-10-16)
1. T1 covers SSW from the due date; U-windows move to Stage 4.
2. Gate 2 DST question uses an appointment instead of a feed.
3. Multiple children (P4) moves to the new Stage 4; Gate 3 exercise becomes the birth-handoff migration.
