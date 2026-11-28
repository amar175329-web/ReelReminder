# 58. Freshness Register (Facts That Expire)

**Rule:** Before relying on any fact below, check its "Last verified" date. If older than 90 days (or a deadline has passed), re-verify from the official source and update this table. If you cannot verify, mark it **UNKNOWN** and tell the user where to check. Provider pricing and limits must always be checked at decision time.

| Fact (as of last verified) | Where to re-check | Last verified | Re-check |
|---|---|---|---|
| gitleaks CLI uses `git`, `dir`, `stdin`; `detect`/`protect` deprecated since v8.19.0 | github.com/gitleaks/gitleaks | 2026-11-22 | 90 days |
| autocannon options `-c`, `-d`, `-p` (HTTP/1.1) | autocannon README | 2026-11-22 | 180 days |
| Lighthouse CLI needs Node 22+; categories: performance, accessibility, best-practices, seo | github.com/GoogleChrome/lighthouse | 2026-11-22 | 90 days |
| Node.js: 20 EOL (Apr 2026); 22 maintenance to Apr 2027; 24 and 26 LTS lines | nodejs.org/en/about/releases | 2026-11-22 | 90 days |
| OWASP Top 10:2025 (web), ASVS 5.0, LLM Top 10 (2025 list; 2026 edition published), Agentic Top 10 (Dec 2025) | owasp.org | 2026-11-22 | 180 days |
| Argon2id minimum m=19 MiB, t=2, p=1; bcrypt legacy cost 10+, 72-byte limit | OWASP Password Storage Cheat Sheet | 2026-11-22 | 180 days |
| Password length guidance (NIST SP 800-63B) | pages.nist.gov/800-63-4 | not re-verified in this pass: UNKNOWN | 180 days |
| India DPDP: consent-manager rules 13 Nov 2026; main duties 13 May 2027; breach report to Board within 72 hours; parental consent under 18 | meity.gov.in, official gazette | 2026-11-22 | 60 days (dates may shift) |
| Google Play: new apps and updates must target Android 16 (API 36) since 31 Aug 2026 | Play Console target-API page | 2026-11-22 | before each release |
| Apple minimum Xcode/SDK, guidelines 4.8, privacy manifest | developer.apple.com | NOT VERIFIED: UNKNOWN | before each release |
| AGENTS.md read natively by Codex, OpenCode, Cursor, Copilot, Antigravity; Claude Code uses CLAUDE.md (import AGENTS.md); Gemini CLI uses GEMINI.md | Each tool's documentation | 2026-11-22 (third-party sources, verify) | 90 days |
| WCAG 2.2 AA as accessibility target | w3.org/TR/WCAG22 | not re-checked in this pass (from prior knowledge): UNKNOWN | 365 days |
| Core Web Vitals "good": LCP 2.5 s, INP 200 ms, CLS 0.1 | web.dev/vitals | not re-checked in this pass (from prior knowledge): UNKNOWN | 180 days |
| Provider pricing, free-tier limits, rate limits | Provider official pricing/docs | NOT VERIFIED: UNKNOWN | at decision time |
| Section 48 thresholds are recommended defaults, not official standards | n/a | n/a | review each project |

**Brief version:** 4.1. **Facts verified:** 22 Nov 2026.

---

