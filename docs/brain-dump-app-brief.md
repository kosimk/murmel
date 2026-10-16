# Voice-First Brain-Dump Journaling App — Project Brief

## The Concept
A voice-first "brain dump to notes" app. You tap, speak freely, and the app transcribes your rambling and condenses it into a clean note or a short journal entry, saved to a simple dated timeline.

## Core Loop (the whole MVP)
1. **Capture** — Tap a button, speak your brain dump. Transcription happens on-device.
2. **Condense** — A local AI model distills the transcript into a tidy one-liner or a few bullet points.
3. **Save** — Entry lands in a simple, scrollable dated timeline.

Everything runs locally. Nothing leaves the phone.

## Ruthless MVP Scope
**Include:** speak, condense, save, scroll back — plus rock-solid local storage (lost entries kill journaling apps).

**Cut for v1:** accounts, cloud sync, social features, charts, weekly "wrapped" reports, mood matrices, therapist-style analysis.

## The Technology
- **iPhone:** Apple's Foundation Models framework (~3B-parameter on-device model, Swift API) — strong at summarization and entity extraction. Speech framework for voice-to-text.
- **Android:** Gemini Nano via ML Kit — has a built-in summarization API (conversations to bulleted lists) plus proofreading. SpeechRecognizer for voice-to-text.
- **Ceiling to respect:** small on-device models are great at "tighten up this messy text," not at deep reasoning or genuine back-and-forth dialogue. Keep the AI's job to "clean up and condense what I said."

## The Wedge (why yours over the competition)
Competitors (Brain Dump, Jrnly, Audionotes, Ripple, Journee) all stumble on at least one of:
- **Pricing:** AI summaries locked behind steep subscriptions (~$3/week).
- **Privacy:** the smart summarizing runs in the cloud, quietly breaking the privacy promise.
- **Bloat:** Life Matrix scores, Spotify-style weekly wrapped, therapist reports — overwhelming.

**Your opening:** on-device AI summarization that is genuinely free AND truly private. Nobody cleanly offers that combination.

**Honest trade-off:** on-device output won't be as polished as cloud models — but "free + private" may matter more to your audience than a shinier summary.

## Pricing Model
Free core loop + a single one-time "unlock everything" purchase (no subscriptions).
- Because the AI runs on-device, ongoing costs are near zero — no recurring revenue needed to survive.
- Dodges the paywall rage seen in competitor reviews.
- Catch: revenue only grows with a steady trickle of new buyers. Manageable given near-zero running costs.
- **To nail down later:** exactly which features are free vs. behind the unlock (e.g. unlimited history, export).

## Validation Before Building
1. **Mine the complaints (free):** read 1-star reviews of Day One, Daylio, Rosebud, and journaling/productivity subreddits. Check if "I just want to talk and have it tidy up my thoughts, free and private" is a pain people voice unprompted.
2. **Fake-door landing page:** one page describing the app as if it exists, one button — "notify me when it launches." Drive a trickle of traffic; measure email signups (behavior, not opinions).
3. **Trap to avoid:** don't ask friends "would you use this?" — people are polite. Measure clicks and signups, not nice words.

## Open Questions / Next Steps
- Pin down the exact free vs. unlock feature split.
- Decide which platform to build first (iPhone or Android).
- Define the specific target person who picks this *because* it's free and private.


---

## Platform & On-Device AI Decision (updated)

**Platform first:** Android — because the builder carries an Android (Fairphone 6) and can test daily.

**Nano caveat:** On-device Gemini Nano (especially the stronger v3) is confirmed only on a narrow set of mostly-2026 flagship devices (Pixel 10, Snapdragon 8 Elite Gen 5 tier). The Fairphone 6 is a mid-range chip and may not run Nano locally, or only an older/weaker version. The "Gemini" features Fairphone advertises (e.g. Gemini Live) are cloud-based, not the on-device Nano the app would call via ML Kit. **Confirm directly** whether Google AICore + a Nano model actually run on the specific unit.

### Chosen path: bundle your own small model (Nano-independent)
Don't depend on the phone's built-in Nano. Ship a small open model inside the app and run it yourself — works on any reasonably capable phone, including the Fairphone.

- **Runtime:** LiteRT-LM (Google's successor to the deprecated MediaPipe LLM Inference API). Clean Kotlin API; loads small open models.
- **Models:** Gemma 3 1B or Qwen (few-hundred-MB range, int4 quantized). llama.cpp (GGUF) is the max-flexibility fallback but closer to the metal.
- **Convergence win:** this is the SAME plumbing you'd use later to run a fine-tuned model. Build MVP on LiteRT-LM with stock small Gemma, then swap in a fine-tuned version down the line — one pipeline, both plans.

### Honest caveats
- **App size:** bundling a model adds a few hundred MB.
- **Threading:** load the model on a background thread or the app freezes (ANR).
- **Heat, not RAM, is the real constraint:** sustained inference causes thermal throttling; mid-range phones handle it less gracefully. Fine for your use case — short bursts to summarize a brain dump, not minutes of continuous generation.

## Fine-Tuning (version-two moat, NOT in MVP)
Skip for MVP — use the stock model to ship fast and validate demand first.
- **Technique:** LoRA / QLoRA — freeze the base model, train small adapter layers. Runs on a single consumer GPU (or free Colab) in hours.
- **Data amounts (for style/format adaptation like "messy speech in → clean bullets out"):**
  - Below ~200 examples: don't bother — use clever prompting instead.
  - Minimum viable: ~200–500 examples.
  - Sweet spot: ~1,000–5,000 examples (where LoRA reliably beats prompting). Diminishing returns beyond.
- **Each example** = a pair: rambly transcript + the tidy bullet-point version you want.
- **Bootstrap trick:** have a bigger cloud model generate first-draft clean versions, then refine by hand.
- **Key caveat:** quality of examples beats quantity. 1,000 careful pairs beat 5,000 sloppy ones. The real work is curating a dataset that reflects the exact style you want.

## "One engine, many niches" strategy
Build the voice-to-clean-note engine once, then reskin for specific audiences.
- **Caveat:** fitness voice-logging is already crowded (VoiceFitLog, GhostFit, RepCount, Sleet — a whole emerging category). Avoid it.
- **Next step:** pick an uncrowded niche lane nobody has planted a flag in yet.


---

## Chosen Niche: Voice-First + Fully Private Baby Tracker (German-localized)

### Why this niche
The parenting pain is real and validated — exhausted, one-handed parents need to *talk*, not tap ("more than three taps and you quit within a week" is literally what competitors advertise against).

**The crowding reality (honest):**
- Voice-first baby logging is contested: Betteroo (voice free in every plan), Robin Baby (voice-first by design), Nara (Siri), ParAI, plus incumbents like Huckleberry.
- Offline/on-device baby tracking is also claimed: Buggy, Bebblo, Baby Feed Tracker, "Offline Baby Tracker" (whose whole pitch is refusing the cloud).

**The actual wedge — the intersection nobody cleanly fills:**
- The offline ones are all **tap-based**.
- The voice-first ones all rely on the **cloud** for smart voice processing.
- **Voice-first AND fully on-device together** is the open gap — and it's exactly what the bundled on-device model (LiteRT-LM + small Gemma) makes possible. Until recently you basically couldn't do good voice without the cloud.
- Telling admission: "Offline Baby Tracker" deliberately skips two-phone real-time sync because syncing would mean the cloud — an honest trade-off, but a weakness to revisit later.

**Positioning story:** "The only baby tracker that's both voice-first AND truly private."

### Localization hook: Germany
Narrow from "another baby tracker" to "the one built for the German system." Privacy is cultural there (GDPR taken seriously), so on-device data is a genuine selling point, not just a feature.

- Map entries to Germany's structured check-up system: the **U-Untersuchungen** (U1–U9) and pregnancy **Vorsorge** appointments — the paper **Kinder-Untersuchungsheft** and **Mutterpass** parents already live by.
- Example flow: brain-dump after a check-up — "U4 went well, weight was good, doctor wants to watch the hip" — and it files against the right exam, on-device, private.
- Why it's defensible for a solo builder: big funded incumbents rarely localize this deeply because it doesn't scale globally — which is exactly why it's an open lane.

### Possible extensions
- Extend backwards into the **pregnancy period** (Mutterpass / Vorsorge appointments).
- Other deeply-localized markets with structured health-check systems.

### The non-negotiable guardrail
Stay a faithful **private record**, NOT a medical advisor.
- Safe: logging "the doctor said X."
- Off-limits: interpreting results or suggesting what to do — that strays into the health territory to avoid.
