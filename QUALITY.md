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
2. **Plan first when it's not trivial.** If the change touches several files or a high-risk area (see tiers below), the agent writes a short plan and the builder approves it before any code is written. If the diff fits in one sentence, skip the plan.
3. **The agent builds it.** That includes tests and a local `./gradlew check`. The agent hooks in section 3a run on every edit and before the agent can finish.
4. **The agent shows evidence, not claims.** The PR contains the commands it ran and their output (tests, eval result). "Tests pass" without output doesn't count.
5. **CI must be green** (section 3).
6. **A fresh reviewer agent** goes through the checklist in section 4 and lists findings. This must be a separate agent that did not write the code and sees only the diff and the FEATURES item. It flags only problems that affect correctness, the requirements or the red flags; everything else is optional. (A reviewer asked to find gaps always finds some, and chasing every one leads to over-engineering.)
7. **The builder reviews.** **Read the changed tests first**, then the code. Run the app if the UI changed, go through the checklist, and explain back anything new (step 2 of the syllabus method).
8. **Update the ledger in the same PR.** If there's no time to explain it, mark it `seen` honestly. Never mark it higher than it is. High-tier changes wait until they reach `explained`.
9. **Fill the PR template and squash-merge.** The builder always makes the merge decision; AI reviews are input, not a verdict.

### Risk tiers: review depth follows risk, not author

| Tier | Examples | Review |
|---|---|---|
| **High** | JSON parser and validation, storage and encryption, backup/restore, migrations, manifest and permissions, anything that handles transcripts, prompts and the model pipeline | Plan approved first · reviewer agent **plus a second reviewer on a different model** · full builder review · break-it check · ledger at `explained` before merge |
| **Normal** | ViewModels, screens, time logic, notifications | Reviewer agent · builder review · break-it check where there is logic |
| **Low** | Theme, strings, previews, docs, build-script cosmetics | CI + a light builder look |

### PR template (`.github/pull_request_template.md`)

```markdown
## What and why
FEATURES item:
Risk tier: high | normal | low
## Agent decision log
Goal in one sentence:
Alternatives considered and why they were rejected:
## Evidence
<commands the agent ran and their output: tests, eval, lint>
## Test changes
<every changed or removed test or eval case, and why; "none" if none>
## Authorship
Authored-by: agent | builder | pair
Syllabus units touched:
## Checks
- [ ] CI green, no new detekt or CPD findings
- [ ] Changed tests read first; no assertion weakened to fit the code
- [ ] Reviewer agent findings triaged (fixed / won't fix + why)
- [ ] High tier only: second reviewer (different model) done
- [ ] Break-it check done (section 5)
- [ ] No red flags from section 4e
- [ ] New dependencies verified (section 4h), or none added
- [ ] Ledger updated: files and levels
## What I didn't fully understand yet
```

---

## 3. Automated checks (CI)

| Check | Tool | Fails the build when |
|---|---|---|
| Formatting | ktlint (auto-fix in a pre-commit hook) | any violation |
| Code smells, complexity | detekt + the Compose Rules plugin | new findings beyond the baseline |
| Duplication | PMD CPD (copy-paste detector, Kotlin) | a new duplicated block over about 50 tokens |
| Android issues | Android Lint (incl. HardcodedText, MissingTranslation) | errors; warnings are tracked |
| Unit tests | JUnit, Turbine | any failure |
| Coverage | Kover | below the per-area thresholds in section 5 |
| No network | script on the **merged release manifest** | `INTERNET` permission present |
| No tracking libraries | dependency deny-list (Firebase Analytics, Crashlytics, ad SDKs …) | a listed dependency appears, even transitively |
| Dependency integrity | Gradle dependency verification (`gradle/verification-metadata.xml`) + dependency locking | an artifact or checksum isn't in the verified list |
| Tests not weakened | script comparing the PR to `main` | number of tests or eval cases goes down, a test is newly `@Ignore`d, or a coverage threshold is lowered, unless the PR is labelled `test-change-approved` by the builder |
| Mutation testing (from M1) | Pitest on the parser and time logic | mutation score drops below the target in section 5 |
| Model quality (from M1) | eval set, including the held-out part | pass rate drops compared to `main` |
| Authorship | trailer check | `Authored-by` missing |
| Quality report | report script (section 6) | never fails; it only publishes |

**Baselines:** the detekt baseline may only shrink. New code must be clean. Growing the baseline needs a written reason in the PR.

**CI is never weakened to let an agent pass.** Lowering a threshold, skipping a check or growing a baseline is a builder decision in its own PR, never part of a feature PR.

### 3a. Agent hooks (deterministic checks while the agent works)

`CLAUDE.md` rules are advisory: an agent can forget them in a long session. Rules that must hold every time become Claude Code hooks in `.claude/settings.json`, checked into the repo.

| Hook | Runs | Does |
|---|---|---|
| PostToolUse (after Edit/Write on `*.kt`) | `ktlint --format` on the file, then detekt on the module | Problems go straight back to the agent to fix |
| Stop | `./gradlew test` (and the eval set once it exists) | The agent can't finish its turn while tests fail |
| PreToolUse guard | before Edit/Write/Bash | Blocks agent edits to protected paths (below) and blocks `git push --force`, `--no-verify` and edits to `.git/hooks` |

**Protected paths** (only the builder changes these, in their own PR):
- `app/src/main/AndroidManifest.xml` and the network-security config
- `config/detekt/baseline.xml`, the Kover and Pitest thresholds, the CI workflows
- `gradle/verification-metadata.xml`
- `evals/holdout/`, the held-out eval cases (section 5)
- `quality/understanding.yml`: only the builder sets understanding levels; the agent may suggest an entry in the PR

Test files are **not** blocked, because agents must write tests. Instead, every changed test is listed in the PR ("Test changes") and the CI guard above catches weakened suites.

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
- **Hidden errors:** an empty `catch`, one that only logs, `runCatching { … }.getOrNull()` or `?: return` that silently drops a failure, or a default value that hides a parse error. Every failure is either handled visibly or passed up on purpose.

### 4f. Tests (read these first)
- Was any existing assertion changed? If so, did the *requirement* change, or was the test bent to fit the code?
- Is new logic tested?
- Do the tests check behaviour, not implementation details?
- Is there at least one test where features work **together** (for example several items in one brain dump, an appointment with a relative date, then the review screen)? Agents often build features that pass alone but break when combined.
- No special-casing: production code must not check for test inputs, and expected outputs must not be copied from the code's own output.
- Is each test readable as given / when / then, with one reason to fail?

### 4g. Understanding
- Can I explain every line? If not, set the level to `seen` and write the gap under "What I didn't fully understand yet".
- To reach `explained`, the builder answers 3–5 questions the reviewer agent asks about the change (what breaks if X is removed, which thread this runs on, what happens on bad input) **without looking at the agent's explanation first**.

### 4h. Dependencies
AI tools sometimes suggest packages that don't exist or have known vulnerabilities; attackers register invented names ("slopsquatting").
- Is the new dependency really needed? Could the standard library or an existing dependency do it?
- Does it exist under exactly that group and name on Google Maven or Maven Central, from the expected publisher, with real usage?
- Version pinned in `gradle/libs.versions.toml`; verification metadata updated by the builder.
- Does it add the `INTERNET` permission or analytics? Check the merged manifest.

---

## 5. Reasonable testing

Test where bugs are expensive and the logic is pure. Coverage is a smoke alarm, not a goal.

| Area | Kind of test | Target |
|---|---|---|
| JSON parser and validation | unit: valid, malformed, partial output | line ≥ 90 %, branch ≥ 85 %, Pitest mutation score ≥ 75 % |
| Time logic (relative times, U-windows, DST) | parameterised unit tests | line ≥ 90 %, Pitest mutation score ≥ 75 % |
| ViewModels | unit + Turbine | line ≥ 70 % |
| Room / DAOs | in-memory DB tests | one test **per migration**, no exceptions |
| Composables | previews + one UI test per main flow (capture → review → save) | no coverage target |
| Model quality | eval set (about 20, later 50 brain dumps) | pass rate tracked; no drop allowed |

Excluded from coverage: generated code, DI modules, theme files.

**Break-it check (manual mutation test):** during review, the builder changes one line on purpose, for example flips a condition or an off-by-one. A test should turn red. If none does, the test is missing or weak. Pitest does the same automatically for the parser and time logic; the manual check stays because it is also a learning exercise.

**Coverage says a line ran; mutation score says a test would notice if that line were wrong.** For agent-written tests the second matters more.

**Tests and code from different sessions (high tier):** one agent session writes the tests from the FEATURES item, the builder reviews them, and a new session writes the code to pass them. The agent writing the code doesn't get to define what "done" means.

**Held-out eval set:** about a third of the eval brain dumps live in `evals/holdout/`. Agents never see them while tuning the prompt or model; only CI scores them. A large gap between the visible and the held-out pass rate means the prompt fits the examples rather than the task. Add new held-out cases from time to time (for example from your own real brain dumps), and never move a case from `holdout/` into the visible set.

---

## 6. Quality metrics and report

`quality/report` runs on every build of `main`. It writes a summary into the CI job summary and appends one row to `quality/history.csv`, so trends are visible.

| Metric | Source | Healthy signal |
|---|---|---|
| Kotlin lines of code, prod / test (no blanks or comments) | cloc | test:prod around 0.5–1.0 in logic packages |
| Largest files and functions | detekt | no file over 400 lines, no function over 60 |
| Cyclomatic complexity, max | detekt | 15 or less |
| New detekt findings | detekt | 0 |
| Duplicated blocks | CPD | 0 new; total trending down |
| Moved / refactored lines share | `git log --stat -M` | not falling to near zero; a sign that code is copied instead of reused |
| Mutation score (parser, time) | Pitest | as in section 5 |
| Lint warnings | Android Lint | trending down |
| Coverage per area | Kover | as in section 5 |
| Authorship share of lines | `git blame` + trailers | for information only |
| **Understood share of AI-written lines** | `git blame` + ledger | ≥ 80 % during a stage; **100 % of the stage's code at its gate** |
| Stale ledger entries | report script | 0 at a gate |
| Eval pass rate, visible and held-out | eval set | no drop compared to `main`; held-out within about 10 points of visible |
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
CPD          0 new · 2 total
Mutation     parser 81 % · time 77 %
Eval         visible 13/14 (93 %) · held-out 5/6 (83 %)
Next to understand: ModelRunner.kt (stale), SpeechInput.kt (seen)
```

**Goodhart warning:** once a number becomes a target, it stops being a good measure. If a metric looks great while the code feels worse, trust the code and fix the metric.

---

## 7. Rhythm and gates

- **Every PR:** the loop in section 2.
- **When a test fails or the app misbehaves:** try to debug it yourself for about 10 minutes before handing it to the agent. Debugging is the skill that suffers most when AI does the work; asking the agent conceptual questions ("why would this run on Main?") is fine, asking it for the fix is the last step.
- **Weekly, or every ~5 PRs (20 minutes):** read the trend in `history.csv`. Pick the 1–2 "next to understand" files for the next study session. Fix one code smell or one duplicated block.
- **Monthly:** prune `CLAUDE.md`. A rule the agent already follows without being told can go; a rule it keeps breaking becomes a hook or a CI check.
- **At every SYLLABUS gate,** in addition to the gate questions:
  - All CI checks green. The detekt baseline did not grow during the stage.
  - 100 % of the stage's AI-written lines at `explained` or `owned`, none stale.
  - A fresh agent reviews **all** of the stage's code against section 4. The findings are triaged.
  - Held-out eval pass rate within about 10 points of the visible one.
  - A report snapshot is saved as `quality/reports/<milestone>.md`.

---

## 8. Setup tasks (small, do before or early in Milestone 1)

- [ ] Add ktlint, detekt + Compose Rules, CPD and Kover to Gradle. Create the detekt baseline.
- [ ] Turn on Gradle dependency verification and dependency locking.
- [ ] CI: merged-manifest guard, dependency deny-list, trailer check, "tests not weakened" guard.
- [ ] Claude Code hooks in `.claude/settings.json`: PostToolUse lint, Stop test gate, PreToolUse guard for protected paths (section 3a).
- [ ] `commit-msg` and pre-commit hooks. PR template.
- [ ] Reviewer agent definition (`.claude/agents/reviewer.md`) with the section 4 checklist and the "correctness and requirements only" instruction.
- [ ] `quality/` folder: `understanding.yml`, `history.csv`, report script.
- [ ] Seed the ledger with the Milestone 0 files at their honest levels.
- [ ] With the first parser code: Pitest, and `evals/holdout/` split from the eval set.
- [ ] Optional: add a short "reading the quality report" unit to Stage 1 of SYLLABUS.md, or fold it into Q1.

---

## 9. Background and sources (checked October 2026)

Why the rules above exist:

- **Give the agent a check it can run, and keep rules that must always hold in hooks rather than instructions; review in a fresh context; tell reviewers to flag only real gaps.** [Best practices for Claude Code](https://code.claude.com/docs/en/best-practices) (Anthropic).
- **Review by risk tier, require evidence, read test changes first, never weaken CI, use differently built reviewers for important code, the human owns the merge.** Addy Osmani, [Agentic Code Review](https://www.oreilly.com/radar/agentic-code-review/) (O'Reilly Radar, June 2026).
- **Visible tests pass while hidden ones fail, mostly because features break when combined.** Zhao et al., [SpecBench](https://arxiv.org/abs/2605.21384) (May 2026). Basis for the held-out eval set and the combined-feature tests.
- **More duplication, much less refactoring and more error-hiding code in AI-era changes.** GitClear, [The Maintainability Gap](https://www.gitclear.com/the_ai_code_quality_maintainability_gap) (2026; vendor data, not independently verified). Basis for CPD and the hidden-errors red flag.
- **AI code fails about 44 % of security tasks; Java does worst.** Veracode 2026 GenAI Code Security Report, via [The Next Web](https://thenextweb.com/news/veracode-2026-genai-code-security-56-percent-pass-rate) (July 2026).
- **Invented and vulnerable dependencies.** Endor Labs, [Hallucinated packages](https://www.endorlabs.com/learn/hallucinated-packages-how-ai-invents-dependencies-attackers-exploit). Basis for section 4h and dependency verification.
- **Delegating code lowers learning; asking conceptual questions or generating then asking for explanations keeps most of it; debugging is hit hardest.** Anthropic, [How AI assistance impacts the formation of coding skills](https://www.anthropic.com/research/AI-assistance-coding-skills) (January 2026). Basis for the ledger questions and the 10-minute debugging habit.
