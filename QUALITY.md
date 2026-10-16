# Murmel – Quality Workflow

*Draft, 2026-10-16. Works with `FEATURES.md` (what to build) and `SYLLABUS.md` (what to learn).*

Most of Murmel's code is written by AI agents and reviewed by the builder. So every change has to answer three questions:

1. **Is it correct?** Tests and the eval set say so.
2. **Is it clean?** Automated checks and the review checklist say so.
3. **Do I understand it?** The understanding ledger says so.

---

## 1. Tracking authorship and understanding

These two facts are tracked separately. Who wrote a line never changes. How well the builder understands it changes over time.

### 1a. Authorship: commit trailers (set once)

Every commit that touches `src/` gets an `Authored-by` trailer:

```
feat(capture): wrap SpeechRecognizer in a callbackFlow

Authored-by: agent      # agent | builder | pair
Syllabus: S1, K3
```

- `agent`: the AI wrote it and the builder reviewed it.
- `builder`: the builder typed it. AI may have advised.
- `pair`: mixed. Split the commit when that's practical, otherwise use `pair`.
- PRs are squash-merged. The PR template fills the trailer on the squash commit.
- A `commit-msg` hook and a CI check both reject commits without the trailer.

### 1b. Understanding: the ledger (updated over time)

`quality/understanding.yml`, one entry per file. Add a `symbol:` field if a file needs to be split.

```yaml
- path: app/src/main/java/app/murmel/speech/SpeechInput.kt
  level: explained        # seen | explained | owned
  at: 3f2a9c1             # commit when the level was set
  date: 2026-10-20
  units: [S1, K3]
  note: "awaitClose stops the recognizer. Asked why startListening must run on Main."
```

| Level | Meaning |
|---|---|
| `seen` | Read the diff and have a rough idea of what it does. |
| `explained` | Explained every line back to the agent. Can answer "what breaks if we remove this?" |
| `owned` | Also made a real change to it alone, or could rewrite it from scratch. |

**"Fully understood" means `explained` or `owned`.**

**Staleness:** if any `agent` or `pair` commit touches the file after `at`, the entry becomes **stale**. The report flags it. Re-reviewing only the diff since `at` is enough to clear it.

---

## 2. The per-change loop

1. **One small branch, one purpose.** Aim for under about 300 changed lines of production code. Bigger AI diffs can't be reviewed properly.
2. **The agent builds it.** That includes tests and a local `./gradlew check`.
3. **CI must be green** (section 3).
4. **A fresh reviewer agent** goes through the checklist in section 4 and lists findings. This must be a separate agent that did not write the code.
5. **The builder reviews.** Read the diff, run the app if the UI changed, go through the checklist, and explain back anything new (step 2 of the syllabus method).
6. **Update the ledger in the same PR.** If there's no time to explain it, mark it `seen` honestly. Never mark it higher than it is.
7. **Fill the PR template and squash-merge.**

### PR template (`.github/pull_request_template.md`)

```markdown
## What and why
FEATURES item:
## Authorship
Authored-by: agent | builder | pair
Syllabus units touched:
## Checks
- [ ] CI green, no new detekt findings
- [ ] Reviewer agent findings triaged (fixed / won't fix + why)
- [ ] Break-it check done (section 5)
- [ ] No red flags from section 4e
- [ ] Ledger updated: files and levels
## What I didn't fully understand yet
```

---

## 3. Automated checks (CI)

| Check | Tool | Fails the build when |
|---|---|---|
| Formatting | ktlint (auto-fix in a pre-commit hook) | any violation |
| Code smells, complexity | detekt + the Compose Rules plugin | new findings beyond the baseline |
| Android issues | Android Lint (incl. HardcodedText, MissingTranslation) | errors; warnings are tracked |
| Unit tests | JUnit, Turbine | any failure |
| Coverage | Kover | below the per-area thresholds in section 5 |
| No network | script on the **merged release manifest** | `INTERNET` permission present |
| No tracking libraries | dependency deny-list (Firebase Analytics, Crashlytics, ad SDKs …) | a listed dependency appears, even transitively |
| Model quality (from M1) | eval set | pass rate drops compared to `main` |
| Authorship | trailer check | `Authored-by` missing |
| Quality report | report script (section 6) | never fails; it only publishes |

**Baselines:** the detekt baseline may only shrink. New code must be clean. Growing the baseline needs a written reason in the PR.

---

## 4. Review checklist (for the builder and the reviewer agent)

### 4a. Correctness
- Does it do what the FEATURES item says, and nothing more?
- Edge cases: empty transcript, garbage model output, permission denied, rotation, process death, midnight and DST changes.

### 4b. Clean code
- Names say *what*, not *how*. No private abbreviations.
- Functions do one thing. Guidelines: about 30 lines or fewer, 4 parameters or fewer.
- No dead code, no commented-out code, no TODO without a linked issue.
- Comments explain *why*, not *what*.
- Duplication: extract on the third copy, not the second.
- Kotlin idioms: `val` over `var`; sealed or data classes for state; exhaustive `when`; no `!!`; nulls handled on purpose; no Java-style getters or utility classes.

### 4c. Android and Compose best practices
- Composables are stateless where possible, with state hoisted. `modifier: Modifier = Modifier` is the first optional parameter.
- `collectAsStateWithLifecycle()`, not `collectAsState()`.
- ViewModels expose `StateFlow`, never `MutableStateFlow`.
- No business logic in composables.
- All user-facing text lives in resources, in both German and English.
- Accessibility and one-handed use: content descriptions; touch targets of at least 56 dp, the record button at least 96 dp (see `CLAUDE.md`).

### 4d. Threading
- Model, database and file work stays off Main. Dispatchers are injected, not hard-coded.
- No `GlobalScope`. No `runBlocking` in production code.
- Every `callbackFlow` has `awaitClose`.
- Cancellation is respected. Long loops check `isActive` or call `ensureActive()`.

### 4e. Murmel red flags (these block the merge)
- **Logging personal data.** Transcripts, notes or baby data must never reach `Log.*` or `println`, not even in debug builds.
- **Network code** of any kind.
- **Advice in the UI.** User-facing strings with words like *normal*, *zu wenig*, *zu viel*, *sollten* or *empfehlen* get a manual guardrail check. Murmel records; it never interprets.
- Model output saved **without validation and the review screen**.
- Personal data stored **outside the encrypted database**, for example in SharedPreferences.
- **Swallowed exceptions:** an empty `catch`, or one that only logs.

### 4f. Tests
- Is new logic tested?
- Do the tests check behaviour, not implementation details?
- Is each test readable as given / when / then, with one reason to fail?

### 4g. Understanding
- Can I explain every line? If not, set the level to `seen` and write the gap under "What I didn't fully understand yet".

---

## 5. Reasonable testing

Test where bugs are expensive and the logic is pure. Coverage is a smoke alarm, not a goal.

| Area | Kind of test | Target |
|---|---|---|
| JSON parser and validation | unit: valid, malformed, partial output | line ≥ 90 %, branch ≥ 85 % |
| Time logic (relative times, U-windows, DST) | parameterised unit tests | line ≥ 90 % |
| ViewModels | unit + Turbine | line ≥ 70 % |
| Room / DAOs | in-memory DB tests | one test **per migration**, no exceptions |
| Composables | previews + one UI test per main flow (capture → review → save) | no coverage target |
| Model quality | eval set (about 20, later 50 brain dumps) | pass rate tracked; no drop allowed |

Excluded from coverage: generated code, DI modules, theme files.

**Break-it check (manual mutation test):** during review, the builder changes one line on purpose, for example flips a condition or an off-by-one. A test should turn red. If none does, the test is missing or weak.

---

## 6. Quality metrics and report

`quality/report` runs on every build of `main`. It writes a summary into the CI job summary and appends one row to `quality/history.csv`, so trends are visible.

| Metric | Source | Healthy signal |
|---|---|---|
| Kotlin lines of code, prod / test (no blanks or comments) | cloc | test:prod around 0.5–1.0 in logic packages |
| Largest files and functions | detekt | no file over 400 lines, no function over 60 |
| Cyclomatic complexity, max | detekt | 15 or less |
| New detekt findings | detekt | 0 |
| Lint warnings | Android Lint | trending down |
| Coverage per area | Kover | as in section 5 |
| Authorship share of lines | `git blame` + trailers | for information only |
| **Understood share of AI-written lines** | `git blame` + ledger | ≥ 80 % during a stage; **100 % of the stage's code at its gate** |
| Stale ledger entries | report script | 0 at a gate |
| Eval pass rate | eval set | no drop compared to `main` |
| App and model size | build output | tracked |
| Number of dependencies | Gradle | tracked; each new one justified in its PR |

**How the AI and understanding numbers are computed:**
1. For each Kotlin file, `git blame --line-porcelain` gives the commit behind every line.
2. Each commit's `Authored-by` trailer gives the author type of that line.
3. The ledger gives the file's level and whether it is stale.
4. Understood share = AI-written lines (`agent` + `pair`) in files at `explained` or `owned` that are not stale, divided by all AI-written lines.

**Example report (illustrative numbers):**

```
Murmel quality · main @ 3f2a9c1 · 2026-10-20
Kotlin LOC   prod 1,240 · test 860 (0.69)
Authored     agent 78 % · pair 12 % · builder 10 %
Understood   84 % of AI lines · 2 files stale
Coverage     parser 94 % · time 91 % · viewmodels 72 %
detekt       0 new · baseline 3
Eval         18/20 (90 %) ▲ +1
Next to understand: ModelRunner.kt (stale), SpeechInput.kt (seen)
```

**Goodhart warning:** once a number becomes a target, it stops being a good measure. If a metric looks great while the code feels worse, trust the code and fix the metric.

---

## 7. Rhythm and gates

- **Every PR:** the loop in section 2.
- **Weekly, or every ~5 PRs (20 minutes):** read the trend in `history.csv`. Pick the 1–2 "next to understand" files for the next study session. Fix one code smell.
- **At every SYLLABUS gate,** in addition to the gate questions:
  - All CI checks green. The detekt baseline did not grow during the stage.
  - 100 % of the stage's AI-written lines at `explained` or `owned`, none stale.
  - A fresh agent reviews **all** of the stage's code against section 4. The findings are triaged.
  - A report snapshot is saved as `quality/reports/<milestone>.md`.

---

## 8. Setup tasks (small, do before or early in Milestone 1)

- [ ] Add ktlint, detekt + Compose Rules and Kover to Gradle. Create the detekt baseline.
- [ ] CI: merged-manifest guard, dependency deny-list, trailer check.
- [ ] `commit-msg` and pre-commit hooks. PR template.
- [ ] `quality/` folder: `understanding.yml`, `history.csv`, report script.
- [ ] Seed the ledger with the Milestone 0 files at their honest levels.
- [ ] Optional: add a short "reading the quality report" unit to Stage 1 of SYLLABUS.md, or fold it into Q1.
