# TEST PLAN (Sections 15, 48). Report format 48.5; never PASS without real output.
| Area | Tool | Gate |
|---|---|---|
| Logic units: ShortcodeParser, MediaIdCodec, NextTrigger (all repeats, month-end, leap year, DST, ends), streak, grouping (Today/Coming up), form state | `./gradlew testDebugUnitTest` | 100% pass; >= 70% coverage on logic modules |
| Room migration | MigrationTestHelper (instrumented; NOT RUN if no emulator) | no row lost |
| PendingIntent uniqueness, ReelOpener by id | instrumented/unit | 3 of 3 distinct and correct |
| Lint, build | `./gradlew lintDebug assembleDebug` | 0 errors |
| Secrets | `gitleaks git -v` ; `gitleaks dir -v .` | 0 findings |
| Dependency vulnerabilities | tool chosen and recorded (UNKNOWN) | 0 critical/high in production deps |
| Website | Lighthouse mobile median of 3 | >= 70 / 90 / 90 / 90 |
| Public URLs | `curl -sIL` | 200 |
| Device | docs/DEVICE_CHECKLIST.md run by the owner | all items reported |
Results table (fill with real data):
| Check | Command/tool | Date | Environment | Result | Gate | PASS/FAIL/NOT RUN |
|---|---|---|---|---|---|---|
