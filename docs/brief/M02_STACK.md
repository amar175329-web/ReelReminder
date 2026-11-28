# 46. Default Stacks and Stack Selection

**Rule: choose boring, widely documented, well-maintained technology that the person can keep running. One language across frontend and backend where possible. Fewer moving parts beat clever architecture.** Verify current versions and docs before pinning anything.

## Pick one default

| Stack | Use when | Typical parts |
|---|---|---|
| **A. Full-stack TypeScript app (default for most web products)** | SaaS, dashboards, content + app, marketplaces, MVPs | Next.js (or similar full-stack framework) + TypeScript strict + PostgreSQL + ORM with migrations (Prisma or Drizzle) + managed or library auth + Zod validation + Tailwind with a small component library + Vitest + Playwright |
| **B. Separate frontend + API** | Mobile app shares the backend, heavy background jobs, or a team split | React (Vite) or any SPA + Node API (Fastify/NestJS/Express) **or** Python FastAPI + PostgreSQL + Redis only if queues/rate limits need it + Docker |
| **C. Mobile-first** | Android/iOS app is the main product | Expo (React Native) + Stack B backend; see Section 49 |
| **D. Content/marketing site** | Mostly static pages | Astro or static Next.js + headless CMS or Markdown; no custom auth unless needed |

## Selection rules

1. Start with Stack A unless a row above clearly fits better. Write the reason in an ADR.
2. Database default: **PostgreSQL** (relational, transactions, row-level security). Use others only with a stated need.
3. No microservices, no Kubernetes, no message broker, no GraphQL layer unless requirements prove the need. A well-structured monolith is the default.
4. Use an **LTS** Node.js release for production (as of Nov 2026: Node 24 and Node 26 are LTS lines, Node 22 is in maintenance, Node 20 is end-of-life; verify at nodejs.org/en/about/releases). Lighthouse CLI itself needs Node 22 or later.
5. Pin versions with a lockfile; upgrade deliberately; run CI on every upgrade.
6. Prefer official SDKs and documented APIs; avoid abandoned packages (check last release, open security issues, license).
7. Fit the hosting target (Section 50): a 2-core / 8 GB server runs one Node app + Postgres comfortably; build in CI rather than on the server.
8. AI features: call providers **from the backend only**; wrap the provider behind one internal module so models can be swapped (Section 47).
9. Any deviation from the default needs an ADR: need, alternatives, cost of maintaining it alone.

## Avoid

Home-grown auth or crypto; ORMs without migration tooling; storing files in the database; secrets in `NEXT_PUBLIC_`/`VITE_` variables; mixing several UI libraries; unmaintained templates copied without review; a different stack per feature.

---

