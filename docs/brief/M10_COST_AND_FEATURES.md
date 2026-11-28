# 55. Cost and Capacity Estimate

Produce this in P2 and update in P9. **Use real prices from the providers' official pages (note the date); if not checked, write UNKNOWN. Never invent a price.**

## 55.1 Workload model

| Input | Value | Basis |
|---|---|---|
| Users (registered / daily active) at launch, 6 months, 12 months | | Assumption, state source |
| Actions per active user per day | | |
| Average requests per action | | |
| Peak factor (peak requests/sec divided by average) | start with 5-10 | Assumption |
| Average record size and growth per month | | |
| AI calls per user per day, tokens in/out per call | | Section 47 |
| Emails/SMS per user per month | | |
| Stored files per user, average size | | |

Formulas: average req/s = (DAU x actions x requests per action) / 86,400. Peak req/s = average x peak factor. Storage = rows x average size x growth + files. AI cost = calls x (input tokens x input price + output tokens x output price).

## 55.2 Cost table (monthly)

| Component | Driver | Unit price (official page, date) | Low | Expected | High |
|---|---|---|---|---|---|
| Hosting / compute | | | | | |
| Database | | | | | |
| Object storage + bandwidth/CDN | | | | | |
| Email / SMS / push | | | | | |
| AI provider | | | | | |
| Payment fees | % + fixed | | | | |
| Monitoring / error tracking | | | | | |
| Domain, TLS, backups | | | | | |
| App store fees (if mobile) | | | | | |
| **Total** | | | | | |

## 55.3 Capacity check and triggers

- Compare peak req/s and data size with the chosen hosting (for example a 2 vCPU / 8 GB server) using the Section 48 load test; record the measured limit, not a guess.
- Write upgrade triggers: for example CPU above 70% for 15 minutes, p95 above the Section 48 gate, disk above 75%, DB connections above 80%.
- Scenario table at 10, 1,000 and 100,000 users: what changes (instance size, managed DB, caching, queues, CDN) and the rough cost step.
- Set spend caps and alerts on every metered service (Section 36); per-user AI quotas (Section 47).
- State break-even: price per user vs cost per user.

---

# 56. Feature Modules: Realtime, Search, Files/Media, Offline-First, Localization

Use only the parts the product needs.

## 56.1 Realtime (live updates, chat, notifications)

| Need | Use |
|---|---|
| Occasional refresh | Polling (simplest; fine for MVP) |
| One-way server updates | Server-Sent Events |
| Two-way low latency | WebSockets |

Rules: authenticate on connect and again when the token expires; authorize **every channel subscription and every message** (ownership/tenant); check Origin; limit message size and rate per connection; heartbeat and reconnect with backoff and message ids to avoid duplicates; one server is fine at small scale, add pub/sub (for example Redis) only when running several instances; load-test connection counts; log connects/denials.

## 56.2 Search

Start with PostgreSQL full-text search or trigram indexes. Add a dedicated engine only when needed. Rules: owner/tenant filters enforced inside the query or index filter, never after retrieval; index only what users may see; never index secrets; cap page size and query length; rate limit; keep the index in sync (queue, retry, rebuild command); test that User A never sees User B's results.

## 56.3 Files and media

1. Upload directly to private object storage with short-lived signed URLs; the app server never streams large files itself.
2. Validate size, type by content (magic bytes), count and quota; random storage names; strip image metadata (EXIF); scan for malware where relevant.
3. Process asynchronously in a queue: thumbnails, transcoding, OCR. CPU-heavy video work does not belong on a small web server; use a worker or external service.
4. Serve private files through signed URLs; set lifecycle and deletion rules; serve public assets via a CDN.
5. Tests: oversized file, wrong type disguised by extension, another user's file URL, expired URL.

## 56.4 Offline-first and sync

Local store (SQLite or IndexedDB); queue mutations with client-generated ids (idempotent); defined conflict rule (last-write-wins, per-field merge, or CRDT) with a stated reason; versioning on records; sync resumes after reconnect; encrypted local storage and data wipe on logout for sensitive data; handle expired auth while offline; test with airplane mode and two devices editing the same record.

## 56.5 Localization (for example English + Hindi)

Use an i18n framework with message catalogs (no hard-coded strings); UTF-8 everywhere; fonts that cover Devanagari and other target scripts; locale formatting for dates, numbers and currency (for example INR with lakh/crore grouping); RTL support if Urdu or Arabic is targeted; plural rules; translations reviewed by a native speaker; language stored per user; test long strings and mixed-script content; legal pages in each supported language where required.

---

