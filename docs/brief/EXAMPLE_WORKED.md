# 57. Worked Example (Shows the Expected Level of Quality)

**All names, numbers and results below are ILLUSTRATIVE, for format only. They are not real measurements.**

## 57.1 The idea (as the user might write it)

"InvoiceNudge: freelancers add unpaid invoices and the app sends polite, escalating payment-reminder emails to their clients. Web first. Hindi + English. Free for 5 invoices, paid after that."

## 57.2 P0 Intake output

| Item | Answer |
|---|---|
| Problem | Freelancers avoid chasing late payments because it feels awkward and is easy to forget |
| Users | Solo freelancers (buyer = user); clients receive emails but never log in |
| Primary workflow | Add invoice, choose reminder schedule, app emails the client, freelancer marks paid |
| MVP | Accounts, invoices, clients, 3-step reminder schedule, mark paid, Hindi/English UI |
| Non-goals | Invoice creation/PDF, accounting sync, SMS/WhatsApp, team accounts |
| Domain flags (Section 54) | Payments (subscription only, no client payments); personal data of clients (email, name) |
| Assumptions (stated, I can correct) | Stack A; managed auth; email via a transactional provider; monthly subscription via hosted checkout; INR + USD display |
| Questions (only high-value) | 1) Which countries first (tax and consent rules)? 2) Free-plan limit and price? 3) May reminders be sent in the freelancer's name from their own address? |

## 57.3 PRD excerpt

**Story:** As a freelancer, I want reminders to go out automatically so that I get paid without chasing.

| ID | Acceptance criterion (testable) |
|---|---|
| AC-1 | Given an unpaid invoice with a due date, when the due date passes by 1 day, then one reminder email is queued and sent once, and the send is recorded |
| AC-2 | Given a reminder already sent for step 1, when the scheduler runs again, then no duplicate is sent (idempotent) |
| AC-3 | Given a paid invoice, when the scheduler runs, then no reminder is sent |
| AC-4 | Given User A, when requesting User B's invoice by id, then the response is 404 |
| AC-5 | Given a free-plan user with 5 invoices, when adding a 6th, then the API returns a clear upgrade message |
| AC-6 | Given a client email that bounced, then reminders stop and the freelancer sees the status |

Error states: email provider down (retry with backoff, show "delayed"), invalid client email, expired session, rate limit.

## 57.4 Key decisions (ADRs, short)

| Decision | Choice | Reason | Exit plan |
|---|---|---|---|
| Stack | Next.js + TypeScript + PostgreSQL + Prisma | Default Stack A | n/a |
| Auth | Managed auth service | MVP speed, verified email, MFA later (Section 45) | Export users + hashes |
| Hosting | Serverless app + managed Postgres | Least ops (Section 50) | Container on VPS |
| Email | Transactional provider with SPF/DKIM/DMARC | Deliverability (Section 36) | Swap behind one module |
| Scheduler | Daily job + queue with idempotency keys | Avoid duplicate sends | n/a |

## 57.5 Schema excerpt (Section 25.4)

| Table | Columns | Notes |
|---|---|---|
| `clients` | id (uuid), **owner_id**, name, email, language, created_at | Index owner_id; unique (owner_id, email) |
| `invoices` | id, **owner_id**, client_id, number, amount_minor, currency, due_date, status (unpaid/paid/void), paid_at | Money as integer minor units |
| `reminder_steps` | id, **owner_id**, invoice_id, step (1-3), send_at, sent_at, provider_message_id, status | Unique (invoice_id, step) = idempotency |
| `audit_logs` | id, user_id, event, ip, metadata, created_at | No secrets or email bodies |

## 57.6 API excerpt (Section 25.5)

| Method + path | Auth | Ownership check | Validation | Rate limit |
|---|---|---|---|---|
| `POST /api/invoices` | yes | owner = token user; client must belong to owner | amount > 0, currency allowlist, due date valid | 30/min/user |
| `GET /api/invoices/:id` | yes | `WHERE id=:id AND owner_id=:user` else 404 | uuid | 100/min/user |
| `POST /api/invoices/:id/mark-paid` | yes | same | uuid | 30/min/user |
| `POST /api/webhooks/billing` | provider signature | n/a | signature + event id dedupe | provider IP/signature |

## 57.7 Implementation plan (first milestones)

| Milestone | Tasks | Gate |
|---|---|---|
| M0 | Repo, CI, `.env.example`, gitleaks hook, AGENTS.md | CI green |
| M1 | DB + migrations, health check, UI shell (Hindi/English switch) | Build passes |
| M2 | Managed auth wired, verified-email gate, rate limits | Auth tests pass |
| M3 | Ownership layer + tests for every invoice/client endpoint | Cross-user tests: all denied |
| M4 | Invoice CRUD + scheduler + email + idempotency | AC-1 to AC-6 pass |

## 57.8 Evidence format (ILLUSTRATIVE VALUES)

| Check | Command/tool | Environment | Result | Gate | Status |
|---|---|---|---|---|---|
| Secrets | `gitleaks git -v` | CI, 2026-xx-xx | 0 findings | 0 | PASS |
| IDOR | test suite `idor.spec` | staging | 14/14 endpoints deny cross-user | 100% | PASS |
| Load | `npx autocannon -c 50 -d 30 <staging>/api/health` | staging | p95 and error rate recorded here | Section 48 | PASS or FAIL with numbers |
| Lighthouse | CLI, median of 3 | staging | scores recorded here | Section 48 | PASS or FAIL |
| ZAP baseline | not available in this environment | n/a | NOT RUN, run `<command>` from your machine | 0 high | NOT RUN |

## 57.9 Final-report snippet (labels in use)

- Reminder emails: **REAL**, verified by sending to a test inbox; delivery log id recorded.
- Hindi translations: **REAL** but unreviewed by a native speaker (listed as remaining issue).
- WhatsApp reminders: **NOT IMPLEMENTED** (non-goal).
- Subscription billing webhooks: **REAL** in test mode; live mode **NOT RUN** until keys are set.
- Public URL: **UNKNOWN** until deployment is verified.

---

