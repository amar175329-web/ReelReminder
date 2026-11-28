# AGENTS.md
Project: ReelReminder | Stack: Android native (Kotlin, Jetpack Compose, Room, AlarmManager exact alarms, Coil) / no backend / local DB / APK via GitHub Releases / static site on Vercel (/site)
Commands (verified from repo build files on 2026-11-28): install: ./gradlew --version | dev: ./gradlew installDebug | test: ./gradlew testDebugUnitTest | lint: ./gradlew lintDebug | build: ./gradlew assembleDebug | secrets: gitleaks git -v && gitleaks dir -v .

READ FIRST: docs/brief/00_CORE_PROMPT.md (all rules, labels, phases, numbers, final report) -> docs/STATUS.md -> docs/DECISIONS.md -> docs/ISSUES.md. Read modules when a phase needs them: M06_MOBILE, M05_QUALITY_GATES, M04_SECURITY_CORE, M07_HOSTING, M09_REGULATED_DOMAINS (social platforms, scraping), FRESHNESS. Visual + behaviour truth: prototype/index.html. Logo: assets/logo.png (light version).

RULES
- Existing project: follow Section 18 before changing anything (inspect, tag known-good `pre-v6`, branch `v6/work`, preserve working logic and user data, no destructive step without impact + backup + rollback + my OK).
- Work ONE issue at a time in docs/ISSUES.md order. Plan briefly, test first where logic is testable, change small, run lint + unit tests + build, verify every acceptance line, record evidence in docs/EVIDENCE.md, commit, push, update ISSUES.md + STATUS.md, give ONE next step.
- Labels exactly: REAL / SIMULATED / NOT IMPLEMENTED / NOT RUN / UNKNOWN / MISSING. Never write PASS/DONE/DEPLOYED/LIVE without checked evidence (command + real output summary).
- You cannot use my physical phone. Anything that needs it = NOT RUN + an item in docs/DEVICE_CHECKLIST.md. At each milestone gate (after issues 4, 11, 16, 24) build an APK, publish a GitHub pre-release, list the device checks for me, and STOP until I reply.
- Never commit secrets (keystore, tokens, .env). Never ask me to paste secrets in chat. If a login is needed (gh, vercel) tell me the exact command to run.
- No fake data in production paths: empty states are honest. No dead buttons. Every screen: loading, empty, error, success states.
- No scraping and no unofficial Instagram endpoints (Section 54). Official: opening the saved link, Instagram's public embed (verify terms first, record source + date), optional official oEmbed via a server proxy (not in this scope).
- Validate every deep link / intent / shared text; accept only instagram.com reel/post links. Minimum permissions, requested at the moment of need.
- If 3 fix attempts fail: stop, summarise, propose a different approach.
- Talk to me in simple Hindi (Devanagari), short sentences. Code, commits and docs stay in English.
- At the end of every step: update docs/STATUS.md and give ONE clear next step.
