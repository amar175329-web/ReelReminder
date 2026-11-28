# Idea-to-Production Brief v4.1: File Guide

**Start here:** paste `00_CORE_PROMPT.md` into a new chat, add your idea at the bottom (or fill `IDEA_TEMPLATE.md`).
Attach all the other files, or keep them in `docs/brief/` of your repo. Section numbers are identical in every file.

| File | Sections | What it covers |
|---|---|---|
| `00_CORE_PROMPT.md` | n/a | Short master prompt: rules, single 13-phase list with exit gates, always-on engineering rules, numeric gates summary, report format |
| `IDEA_TEMPLATE.md` | n/a | Form for describing your idea (domain flags, users, needs, coding tool) |
| `01_REFERENCE.md` | 1-25, 34-41, 44 | Discovery, PRD, research, UX/UI, frontend, backend, DB, integrations, security overview, testing, deployment, document pack, repo standards, production engineering, legal, AI workflow, extra rigor, operations, release |
| `M01_AUTH.md` | 26, 45 | Secure JWT/session design; build-vs-buy decision |
| `M02_STACK.md` | 46 | Default stacks and selection rules |
| `M03_AI_SECURITY.md` | 47 | OWASP LLM Top 10 controls, agent safety, AI eval suite |
| `M04_SECURITY_CORE.md` | 27-33 | Authorization/IDOR, secure deployment, abuse protection, secrets, input validation, verification toolkit, hardening prompt |
| `M05_QUALITY_GATES.md` | 48 | Numeric pass/fail thresholds and verified commands |
| `M06_MOBILE.md` | 49 | Android/iOS/PWA, mobile security, store requirements |
| `M07_HOSTING.md` | 50 | EC2/VPS, serverless, managed Postgres checklists |
| `M08_BUSINESS_MODULES.md` | 51-53 | Payments, multi-tenancy, roles and permissions |
| `M09_REGULATED_DOMAINS.md` | 54 | Finance/market data, news, health, children, social APIs, UGC, AI media, scraping |
| `M10_COST_AND_FEATURES.md` | 55-56 | Cost/capacity estimate; realtime, search, files/media, offline-first, localization |
| `EXAMPLE_WORKED.md` | 57 | Full idea-to-docs example (illustrative numbers, clearly marked) |
| `FRESHNESS.md` | 58 | Register of expiring facts with last-verified dates and re-check intervals |

## What changed in 4.1

| Gap from review | Fix |
|---|---|
| Core Prompt had no numbers | Numeric gates table now inline |
| Instruction file names differ by tool | AGENTS.md as source plus pointer files (CLAUDE.md with `@AGENTS.md`, GEMINI.md) |
| Facts expire | Freshness rule plus `FRESHNESS.md` register |
| No worked example | `EXAMPLE_WORKED.md` |
| No regulated-domain guidance | Section 54 |
| No cost/capacity template | Section 55 |
| Missing feature modules | Section 56 (realtime, search, files/media, offline-first, localization) |
| Auth plan ignored build-vs-buy | Milestone M2 now follows Section 45; mobile pointer added to Section 26 |

## Review log (honest)

A scripted cross-reference check found every "Section N" reference resolves to a defined section, and a text scan found no leftover deprecated commands. This was a **self-review in the same context, not an independent fresh-context review**. Recommended: run a fresh review with `Review` prompt in Section 38.6 on the whole pack once.

## Verified vs not verified (Nov 2026)

| Verified from current sources | Not verified (marked UNKNOWN in the files) |
|---|---|
| gitleaks `git`/`dir` commands; autocannon flags; Lighthouse Node 22+ and categories; Node LTS lines; OWASP Top 10:2025, ASVS 5.0, LLM Top 10, Agentic Top 10; Argon2id/bcrypt guidance; India DPDP dates; Google Play API 36; which tools read AGENTS.md (third-party sources) | Provider pricing and limits; Apple SDK/guideline text; NIST password length text; WCAG/Core Web Vitals re-check; tax/GST rules; SEBI and other regulator specifics; Section 48 numbers are recommended defaults, not official standards |

Not tested on a real project: run it on a small idea elsewhere and note where the agent drifts.
