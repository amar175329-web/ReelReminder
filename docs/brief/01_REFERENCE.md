# Universal Professional App / Website: Idea-to-Production Reference (v4)

> Project-neutral. This file is the long-form reference. The short prompt to paste is `00_CORE_PROMPT.md`.
> **Core principle:** understand, research, plan, design, build, integrate, secure, test, deploy, verify.
> **Non-negotiable:** never fabricate. Unknown, missing, simulated or unverified things are labelled as such.

## Section 1. How to Use This Reference

1. Paste `00_CORE_PROMPT.md` into a new chat and add your idea (or fill `IDEA_TEMPLATE.md`).
2. Keep the other files in `docs/brief/` of your repo (or attach them). The agent reads a module when it reaches that phase.
3. Section numbers are the same in every file. Module map: see `README.md`.
4. This brief never assumes an existing project, framework or provider unless you provide one.

---

# 2. Product Discovery

Before implementation, define:

- Problem and why it matters.
- Target users.
- Customers and buyer, if different.
- Admin and other roles.
- Primary use case.
- Core user journey.
- Smallest valuable MVP.
- Non-goals to prevent scope creep.
- Competitors and alternatives when useful.
- Differentiation/opportunity.
- Measurable success criteria.
- Business/product KPIs where applicable.
- Legal, privacy, security, accessibility, platform, payment, and third-party constraints.
- Expected scale and performance requirements.
- Budget constraints.
- Launch constraints.

Do not add features simply because they are technically possible.

---

# 3. PRD Requirements

Produce a practical PRD containing:

- Executive summary.
- Product vision.
- Problem statement.
- Target users/personas.
- Jobs-to-be-done where useful.
- User stories.
- Core user journeys.
- Functional requirements.
- Non-functional requirements.
- Acceptance criteria.
- Edge cases.
- Error states.
- Roles and permissions.
- Data requirements.
- Integrations.
- Notifications.
- Analytics/events.
- Pricing/billing if applicable.
- MVP.
- Phase 2.
- Future roadmap.
- Risks.
- Assumptions.
- Open questions.
- Explicit non-goals.

Every important feature should have a clear acceptance criterion.

---

# 4. Research Before Build

Do not jump directly from idea to code.

When current external information can materially improve the result, research:

- Existing products.
- Competitors and alternatives.
- Official API documentation.
- Current SDKs.
- Current framework documentation.
- Open-source repositories.
- Useful libraries.
- Design systems.
- Component libraries.
- AI providers/models.
- Storage/database options.
- Authentication providers.
- Hosting/deployment options.
- Automation services.
- Research/data sources.
- Relevant community discussions.
- Platform policies and limitations.
- Licensing requirements.
- Free-tier limitations.
- Pricing and usage limits.
- Reliability/status where relevant.

### Research rules

1. Prefer official documentation for technical claims.
2. Prefer primary sources for platform policies and API behavior.
3. Use community sources for practical experience, not as unquestioned authority.
4. Check that a resource is current enough for the project.
5. Do not copy proprietary code, private material, or protected designs.
6. Do not assume that a popular tool is the best tool.
7. Explain why each important external resource is recommended.
8. Record uncertainty when evidence conflicts.

---

# 5. Resource Intelligence / Resource Hub

Create a dedicated **Resource Intelligence** section in the project plan.

Identify useful resources that can improve the project, including:

- GitHub repositories.
- Open-source libraries.
- Plugin/skill/tool integrations available to the agent.
- Official APIs/SDKs.
- AI models/providers.
- Research tools.
- Automation tools.
- Design systems.
- UI/component libraries.
- Database/storage services.
- Authentication services.
- Deployment/hosting services.
- Monitoring/error tracking.
- Testing tools.
- Competitor products.
- Documentation/tutorials.
- Useful datasets where legally and practically appropriate.

### Resource evaluation

| Resource | Type | Purpose | Free/Tier | Important Limits | Reliability/Status | Official Link | License | Recommendation |
|---|---|---|---|---|---|---|---|---|

Do not recommend a resource merely because it exists.

### Resource selection rules

- Prefer the simplest reliable option that satisfies requirements.
- Prefer official APIs over unofficial scraping when an official API exists.
- Check licensing before adopting open-source code.
- Check rate limits and free-tier limits.
- Check whether the service is suitable for production.
- Check vendor lock-in where relevant.
- Check data/privacy implications.
- Check whether an external dependency is actually necessary.
- Avoid unnecessary dependencies.

### Plugin / Skill discovery

If the environment provides plugins, skills, connectors, or external tools:

1. Inspect available capabilities.
2. Identify only those relevant to the current task.
3. Explain what each capability is useful for.
4. Explain required permissions/connections.
5. Explain setup steps.
6. Use the capability only when it materially helps.
7. Do not install or connect unrelated tools.
8. Never expose tokens, passwords, API keys, or private connection data.

---

# 6. UX / UI

Design the product as an experienced product designer would.

The interface must feel:

- Human-authored.
- Intentional.
- Calm.
- Premium.
- Consistent.
- Useful.
- Easy to understand.
- Appropriate to the product's audience.

It must **not** feel like:

- An AI-generated dashboard template.
- A generic SaaS starter.
- Random glassmorphism.
- Excessive gradients.
- Excessive rounded cards.
- Everything placed inside cards.
- Decorative UI with no purpose.
- Meaningless KPI statistics.
- Huge empty areas.
- Excessive animation.
- Inconsistent icons.
- Random typography.
- Fake activity used to make screens look busy.

## UX architecture

Define:

- Information architecture.
- Navigation.
- User flows.
- Screen inventory.
- Primary/secondary actions.
- Responsive desktop/tablet/mobile behavior.
- Permissions-based navigation.
- Search/filter/sort behavior.
- Confirmation flows.
- Destructive-action protection.
- Loading states.
- Empty states.
- Error states.
- Success states.
- Offline/degraded states where applicable.
- Accessibility.
- Keyboard navigation.
- Focus behavior.

## Human-quality design standard

Before considering UI complete, review:

- Typography hierarchy.
- Font selection.
- Line height.
- Spacing rhythm.
- Grid/alignment.
- Content density.
- Visual hierarchy.
- Contrast.
- Border treatment.
- Radius consistency.
- Button dimensions.
- Form behavior.
- Icon consistency.
- Table/list readability.
- Sidebar behavior.
- Mobile navigation.
- Responsive breakpoints.
- Hover/focus/active/disabled states.
- Skeleton/loading behavior.
- Empty-state writing.
- Error-message quality.
- Confirmation feedback.
- Animation restraint.

Every visual decision should improve comprehension or usability.

---

# 7. Design System

Create a small, coherent design system instead of styling screens independently.

Define:

- Color tokens.
- Typography tokens.
- Spacing scale.
- Radius scale.
- Shadows/elevation.
- Borders.
- Icon rules.
- Button variants.
- Input variants.
- Cards/panels.
- Tables.
- Modals/dialogs.
- Tabs.
- Navigation.
- Toasts/alerts.
- Status indicators.
- Empty states.
- Loading states.
- Error states.

Avoid inventing a new visual language for every page.

---

# 8. Frontend

Choose the frontend architecture based on requirements.

Handle:

- Routing.
- Protected routes.
- Reusable components.
- Consistent state management.
- Forms and validation.
- Real backend/API integration.
- Loading/error/empty states.
- Optimistic updates only where safe.
- Accessibility.
- Responsive behavior.
- Performance.
- Direct navigation to application routes.
- Browser compatibility where required.

Do not use fake API responses in production.

If a feature is not connected to a backend yet, label it honestly as incomplete or unavailable.

---

# 9. Backend

Define and implement:

- API architecture.
- Authentication.
- Authorization.
- Server-side validation.
- Permission enforcement.
- Tenant isolation where applicable.
- Rate limiting where appropriate.
- Structured logging.
- Error handling.
- Server-side secrets.
- Retries.
- Idempotency.
- Webhooks.
- Background jobs where appropriate.
- API documentation.
- Health checks.

External failures must be handled gracefully.

Never treat a client-side success message as proof that a server-side action succeeded.

---

# 10. Database and Data

Define:

- Schema.
- Relationships.
- Constraints.
- Indexes.
- Migrations.
- Seed strategy.
- Retention rules.
- Deletion rules.
- Backup strategy.
- Restore strategy.
- Development/test/staging/production separation.
- Data privacy.
- Sensitive-data handling.

### Production-data rule

Never mix demo, test, seed, fake analytics, fake customers, fake transactions, or fake activity with real production data.

If real data is unavailable, use an honest empty state.

Examples:

> No projects yet.

> Connect your account to begin.

> No activity has been recorded.

Never manufacture activity to make the interface look impressive.

---

# 11. AI / Automation

When AI is applicable:

- Define exactly what AI is allowed to do.
- Define what AI is not allowed to do.
- Use a real configured provider in production.
- Prefer current official provider documentation.
- Define model selection and fallback behavior.
- Define latency/cost considerations.
- Ground factual outputs in approved/retrieved data when required.
- Track source/citation lineage where factual claims matter.
- Define refusal behavior.
- Define human escalation.
- Provide human override where appropriate.
- Version important prompts and configuration.
- Create evaluation scenarios for accuracy, latency, consistency, safety, refusal behavior, and tool-use correctness.
- Log safe diagnostic metadata without exposing secrets.

AI must never be treated as automatically correct.

---

# 12. Third-Party Integrations

For every integration:

- Identify the official API/service.
- Explain why it is required.
- Document authentication method.
- Document required permissions.
- Store credentials securely.
- Provide connection setup.
- Provide connection testing.
- Show honest status:
  - NOT CONNECTED
  - CONNECTING
  - CONNECTED
  - ERROR
- Handle expired credentials.
- Handle API failures.
- Handle rate limits.
- Handle retries.
- Handle duplicates/idempotency.
- Handle webhooks.
- Handle provider outages.

Never show CONNECTED unless the connection has actually been verified.

---

# 13. Payments

If applicable:

- Plans.
- Limits.
- Trials.
- Entitlements.
- Payment provider.
- Checkout.
- Webhooks.
- Idempotency.
- Failed payments.
- Cancellations.
- Refunds.
- Plan changes.
- Subscription state.

Never mark payment success from client-side state alone.

---

# 14. Security and Privacy

Perform a security review covering:

- Threat model.
- Authentication.
- Session management.
- Authorization.
- Tenant isolation.
- Input validation.
- Output encoding.
- File validation.
- Upload security.
- Rate limiting.
- Secrets management.
- Encryption where appropriate.
- Secure transport.
- Security headers.
- CSRF protection where applicable.
- Webhook verification.
- API protection.
- Audit logs.
- Privacy.
- Retention/deletion.
- Dependency vulnerabilities.
- Error leakage.
- Backup protection.

Use an established security verification framework where appropriate, such as OWASP ASVS 5.0 (with the OWASP Top 10:2025 as a risk checklist), which organizes web-application verification across architecture, authentication, sessions, access control, validation, cryptography, logging, data protection, APIs, and configuration.

Do not claim a security standard is fully satisfied unless the applicable requirements were actually assessed.

---

# 15. Testing

Actually run tests.

Use the applicable combination of:

- Unit tests.
- Integration tests.
- API tests.
- Database tests.
- End-to-end tests.
- Critical user journey tests.
- Authentication tests.
- Authorization tests.
- Tenant-isolation tests.
- Negative tests.
- Edge-case tests.
- AI evaluations.
- Webhook tests.
- Provider integration tests.
- Responsive/UI smoke tests.
- Accessibility checks.
- Production build tests.
- Regression tests.

### Test reporting rule

Never write PASS unless the test actually ran and passed.

For each important test, record:

- What was tested.
- How it was tested.
- Result.
- Failure details if any.
- Fix applied.
- Retest result.

---

# 16. Performance

Before production, check:

- Server response time.
- Database query efficiency.
- Unnecessary API calls.
- Frontend rendering.
- Bundle size.
- JavaScript usage.
- Image optimization.
- Caching.
- Data fetching.
- Large lists/tables.
- Background jobs.
- AI latency.
- External API latency.

Do not sacrifice correctness or functionality merely to improve superficial performance metrics.

---

# 17. Deployment and Publishing

Choose hosting based on:

- Traffic.
- Backend requirements.
- Database.
- Integrations.
- Cost.
- Reliability.
- Scaling.
- Operational complexity.

Prepare:

- Production environment.
- HTTPS.
- Domain/DNS.
- Database.
- Migrations.
- Secrets.
- Environment variables.
- Monitoring.
- Error tracking.
- Health checks.
- Backups.
- Rollback plan.

Verify the public deployment externally.

Never invent a public URL.

Never call something live until it has actually been checked.

---

# 18. Existing Project Protection

If the user provides an existing codebase:

### Before changing anything

Inspect:

- Repository.
- Branch.
- Current commit.
- Working tree.
- Frontend.
- Backend.
- Database.
- Environment configuration.
- Deployment configuration.
- Existing tests.
- Integrations.
- Current public deployment.

Identify the source of truth.

### Preservation rule

Do not replace working functionality merely to create a new visual style.

Preserve:

- Existing business logic.
- Working APIs.
- Database behavior.
- Authentication.
- Integrations.
- Important workflows.
- Existing data.
- Existing tests.

Improve them rather than silently deleting them.

### Destructive-change rule

Before deleting or replacing code, database, deployment, domain, environment configuration, integration, or production data, first establish:

1. What is being removed.
2. Why it is being removed.
3. What depends on it.
4. Whether a backup exists.
5. Whether rollback is possible.
6. Whether the replacement is verified.

If safety cannot be established, do not delete it. Report exactly what must be confirmed manually.

---

# 19. Build Strategy (Single Phase List)

This is the **only** phase list. It matches the Core Prompt exactly.

| # | Phase | Deliverable | Exit gate |
|---|---|---|---|
| P0 | Intake | Idea summary, users, MVP, non-goals, assumptions | Assumptions confirmed |
| P1 | Research | Resource Intelligence report | Choices justified from official sources |
| P2 | Document Pack | PRD, TRD, app flow, UI/UX brief, DB schema, API spec, implementation plan | Acceptance criteria testable |
| P3 | Architecture | Stack and auth decisions, ADRs | Written ADRs |
| P4 | Foundation | Repo, CI, env separation, DB + migrations, health check, UI shell | CI green, gitleaks clean |
| P5 | Auth + Authorization | Auth flows, roles, ownership checks | Auth + IDOR tests pass |
| P6 | Core workflow | Highest-value feature end to end | Critical-flow E2E passes |
| P7 | Integrations / AI / Payments | Real, verified integrations | Connection tests and AI evals run |
| P8 | Security hardening | Rate limits, validation, headers, secrets, logging | Security gates (Section 48) met |
| P9 | Test + quality gates | Unit, integration, E2E, load, Lighthouse, audit | Numbers in Section 48 met or gaps listed |
| P10 | Deploy | Staging then production, backups, rollback | Restore and rollback tested |
| P11 | Public verification | Real checks on the public URL | Real results recorded |
| P12 | Handover | Final report, docs, runbook, roadmap | Next steps delivered |

Polish (typography, motion, micro-interactions) happens only after P9 passes.
**Do not redesign a working product in a way that destroys its functionality.**

# 20. Human-Crafted Product Quality Gate

Before declaring the product visually complete, ask:

### Does it look intentional?

- Does every section have a reason to exist?
- Is the hierarchy obvious?
- Are primary actions clear?
- Is the interface appropriately dense?
- Are components consistent?

### Does it look human-designed?

- Does it avoid generic AI-template patterns?
- Does the visual language fit the product?
- Does the typography feel considered?
- Does the layout respond to actual workflows?
- Are empty states useful rather than decorative?

### Does it feel premium?

- Are spacing and alignment precise?
- Are interactions predictable?
- Are details consistent?
- Are error and loading states polished?
- Does mobile feel intentionally designed rather than merely compressed?

If the answer is no, continue refining.

---

# 21. No-Fabrication Rule

These rules are absolute:

- NOT IMPLEMENTED → say NOT IMPLEMENTED.
- NOT CONNECTED → say NOT CONNECTED.
- SIMULATED → say SIMULATED.
- UNKNOWN → say UNKNOWN.
- MISSING → say MISSING.
- No data → show an honest empty state.
- Never invent credentials.
- Never invent URLs.
- Never invent customers.
- Never invent analytics.
- Never invent transactions.
- Never invent messages.
- Never invent integrations.
- Never invent API results.
- Never invent deployment status.
- Never claim tests passed unless they actually passed.
- Never claim deployment succeeded unless the public URL was actually checked.
- Never claim an integration is connected unless it was actually verified.

When uncertain, be explicit instead of guessing.

---

# 22. Documentation

Produce useful documentation for:

- Setup.
- Environment variables.
- Local development.
- Database setup.
- Migrations.
- API endpoints.
- Integrations.
- Authentication.
- Deployment.
- Backup/restore.
- Troubleshooting.
- Testing.
- Architecture.
- Important decisions.
- Known limitations.

Keep the README focused on what users/developers need to get started; longer technical documentation can be separated into dedicated documentation pages or a wiki.

---

# 23. Delivery Checklist

Before final delivery:

- [ ] PRD.
- [ ] Acceptance criteria.
- [ ] Screen map.
- [ ] User flows.
- [ ] Technical architecture.
- [ ] Database schema.
- [ ] API specification.
- [ ] Frontend.
- [ ] Backend.
- [ ] Integrations.
- [ ] Security.
- [ ] Resource Intelligence report.
- [ ] Automated tests.
- [ ] Production build.
- [ ] Deployment.
- [ ] Monitoring/backups.
- [ ] Documentation.
- [ ] Launch checklist.
- [ ] Post-launch roadmap.
- [ ] TRD, app flow, UI/UX brief.
- [ ] Auth design (JWT/session) and IDOR tests.
- [ ] Rate limiting and abuse protection.
- [ ] Secrets scan (gitleaks) and rotation list.
- [ ] Input validation and upload safety.
- [ ] Load test, Lighthouse, dependency audit results.
- [ ] Repo standards, CI/CD, AGENTS.md and context files.
- [ ] Error tracking, backups restored once, rollback tested.
- [ ] Legal pages, consent and data-deletion path.
- [ ] Independent review pass completed.
- [ ] Runbook and handover package.
- [ ] Public product verification.

---

# 24. Vibe-Coder Operating Mode (Full-Stack Engineer Quality)

The person using this brief builds with AI ("vibe coding") but expects the **output quality of a professional full-stack developer**.

The agent must therefore:

1. Do the engineering itself. Do not hand back vague advice or "you could implement X".
2. Give exact, copy-paste commands and exact file paths. State the working directory.
3. Explain decisions in plain language, briefly (what, why, risk).
4. Make safe, conventional choices by default. Avoid exotic stacks the person cannot maintain.
5. Ask only high-value questions. If a safe assumption exists, state it and continue.
6. End every phase with **one clear next step**, not a menu.
7. Verify its own work (run the build, tests, scans) instead of asking the person to check.
8. Never skip security, validation, error handling or tests because "it is only an MVP".
9. Keep the codebase maintainable: clear structure, consistent naming, `.env.example`, README, no dead code.
10. Report honestly: REAL / SIMULATED / NOT IMPLEMENTED / UNKNOWN.

**Definition of done (every feature):** works end to end against the real backend, validated input, authorization enforced server-side, loading/empty/error states, tested, no secrets exposed, documented.

---

# 25. Document Pack (produce BEFORE writing code)

Deliver these documents, in this order. Keep each practical and short enough to be used.

| # | Document | Purpose |
|---|---|---|
| 1 | PRD | What and why (see Section 3) |
| 2 | TRD | How, technically (25.1) |
| 3 | App Flow | Every user journey and screen transition (25.2) |
| 4 | UI/UX Brief | Look, feel, screens, states (25.3) |
| 5 | Backend Schema | Tables, relations, constraints (25.4) |
| 6 | API Specification | Endpoints, auth, errors (25.5) |
| 7 | Implementation Plan | Ordered milestones with gates (25.6) |
| 8 | Cost and capacity estimate | Budget and scaling triggers (Section 55) |
| 9 | Compliance requirements | Domain obligations from official sources (Section 54) |

## 25.1 TRD (Technical Requirements Document)

- Overview and goals; link to PRD requirements.
- System architecture diagram (text/Mermaid) and component list.
- Stack choices, each with a one-line reason and the alternative rejected.
- Data flow for the 3 most critical journeys.
- Authentication and authorization design (Sections 26 and 27).
- Data model summary (25.4).
- API design conventions: versioning, pagination, error format, idempotency.
- Environments: dev / staging / production, and how secrets differ.
- Non-functional targets: response time, availability, expected load, data size.
- Security design (Sections 26-31) and threat model summary.
- Observability: logs, metrics, alerts, health checks (Section 28).
- Testing strategy and quality gates (Section 32).
- Deployment, rollback and backup/restore.
- Third-party services with limits and cost.
- Risks, open questions, and Architecture Decision Records (ADRs).

## 25.2 App Flow

For each journey, document: **entry point → screens → actions → system response → success state → failure states**.

Minimum journeys: sign up, email verification, login, logout, session expiry/refresh, password reset, first-time empty state, core workflow, create/edit/delete a resource, permission denied, offline/server error, account deletion.

Format (one block per flow):

```text
FLOW: Password reset
1. User opens /forgot-password -> enters email
2. System: always shows "If the account exists, an email was sent" (no user enumeration)
3. Email contains single-use, expiring link
4. User opens link -> /reset-password?token=... -> sets new password
5. System: validates token, hashes password, invalidates token and all sessions
6. Redirect to /login with success message
FAILS: expired token, used token, weak password, rate limit exceeded
```

## 25.3 UI/UX Brief

- Product feel in 3 adjectives and 3 things to avoid (see Section 6).
- Primary users and devices (mobile-first or desktop-first, with reason).
- Screen inventory table: screen, purpose, primary action, data shown, roles allowed, states.
- Navigation model and responsive behavior.
- Design tokens (Section 7): colors, type scale, spacing, radius.
- Component list and states: default, hover, focus, active, disabled, loading, error, empty.
- Form rules: inline validation, clear errors, password visibility toggle, never reveal whether an email exists.
- Auth screens: sign up, login, verify email, forgot/reset password, session-expired.
- Accessibility: contrast, keyboard navigation, focus order, labels, reduced motion.
- Writing style for buttons, errors and empty states.

## 25.4 Backend Schema

Define every table with columns, types, constraints, indexes and relations. Use UUID (or non-guessable) primary keys. Minimum auth tables:

| Table | Key columns | Notes |
|---|---|---|
| `users` | id, email (unique, lowercase), password_hash, email_verified_at, role, status, created_at, updated_at | Never store plaintext passwords |
| `refresh_tokens` | id, user_id, token_hash, family_id, expires_at, revoked_at, replaced_by, ip, user_agent, created_at | Store only the hash; rotate on use |
| `email_verifications` | id, user_id, token_hash, expires_at, used_at | Single-use |
| `password_resets` | id, user_id, token_hash, expires_at, used_at | Single-use, short expiry |
| `audit_logs` | id, user_id (nullable), event, ip, user_agent, metadata (no secrets), created_at | Login success/failure, resets, role changes |
| `<domain tables>` | id, **owner_id (FK users.id)**, fields..., created_at, updated_at, deleted_at | Every user-owned row carries an owner/tenant id |

Also define: foreign keys with delete behavior, unique constraints, indexes on owner_id and lookup columns, migrations, seed data (dev only), retention and deletion rules, backup and restore.

## 25.5 API Specification

For every endpoint: method, path, auth required, roles, request schema, response schema, error codes, rate limit, ownership check performed. Use one consistent error format and never leak stack traces or internals.

## 25.6 Implementation Plan

Ordered milestones, each with tasks, owner, acceptance criteria and a **gate** (what must pass before moving on).

| Milestone | Scope | Gate |
|---|---|---|
| M0 Setup | Repo, environments, `.env.example`, linting, CI, secret scan, pre-commit | CI green, gitleaks clean |
| M1 Foundation | DB, migrations, base API, health check, UI shell, design tokens | Build passes, health check OK |
| M2 Auth | Auth per the Section 45 decision (managed or custom Section 26): sign up, verify email, login, refresh, logout, reset | Auth tests pass, rate limits proven |
| M3 Authorization | Ownership checks, roles, IDOR tests (Section 27) | Cross-user access tests all denied |
| M4 Core workflow | Highest-value feature end to end | Critical-flow E2E passes |
| M5 Remaining features | By priority | Each meets definition of done |
| M6 Abuse and validation | Rate limits, input validation, upload safety (Sections 29, 31) | Negative tests pass |
| M7 Observability and deploy | Logging, alerts, HTTPS, private DB (Section 28) | Staging verified |
| M8 Verification | Load, Lighthouse, secrets, dependency and security scans (Section 32) | Real results recorded |
| M9 Launch | Production deploy, backups, rollback test, public verification | Public URL checked |

---

# 34. Full-Stack Coverage Map (Everything a Professional Does)

A professional full-stack developer does far more than write screens and endpoints. The agent must cover **every row** below, or explicitly mark it NOT APPLICABLE / NOT IMPLEMENTED with a reason.

| Area | Agent must deliver | Where |
|---|---|---|
| Product | Discovery, PRD, scope, MVP, acceptance criteria | 2, 3 |
| Architecture | TRD, ADRs, stack choice with reasons, default stacks | 25, 5, 46 |
| UX/UI | App flow, UI/UX brief, design system, states | 6, 7, 25 |
| Frontend | Routing, state, forms, a11y, responsive, performance | 8 |
| Backend | API, validation, jobs, webhooks, health checks | 9 |
| Database | Schema, migrations, backups, retention | 10, 25.4 |
| Auth and access | Build-vs-buy decision, JWT/sessions, RBAC, ownership checks, multi-tenancy | 26, 27, 45, 52, 53 |
| Security | Threat model, headers, secrets, input safety, abuse limits, AI/LLM security | 14, 28-31, 47 |
| Repo and workflow | Structure, git flow, lint, types, hooks, CI/CD | 35 |
| Production engineering | Errors, logs, cache, queues, email, storage, flags | 36 |
| Legal and privacy | Policies, consent, data rights, accessibility | 37 |
| Collaboration with AI | AGENTS.md, context files, checkpoints, debugging protocol | 38 |
| Extra rigor | Independent review, failure drills, restore drills, budgets | 39 |
| Operations | Runbooks, incident response, key rotation, monitoring | 40 |
| Release and handover | Release checklist, changelog, docs, 7-day watch | 41 |
| Verification | Real tests, scans, load, Lighthouse, public check, numeric pass/fail gates | 15, 32, 48 |
| Mobile | Android/iOS/PWA decision, secure storage, store rules | 49 |
| Hosting | EC2/VPS, serverless, managed DB checklists | 50 |
| Money and teams | Payments, multi-tenancy, roles matrix | 51, 52, 53 |
| Regulated domains | Finance/market data, health, children, social APIs, UGC, AI media | 54 |
| Cost and capacity | Workload model, cost table, upgrade triggers | 55 |
| Feature modules | Realtime, search, files/media, offline-first, localization | 56 |
| Example and freshness | Worked example; register of expiring facts | 57, 58 |

---

# 35. Project Setup and Repository Standards

## 35.1 Structure and tooling

- One clear, conventional folder structure for the chosen stack (frontend, backend, shared types, migrations, tests, docs, scripts). Explain it in the README in five lines.
- Strict typing (for example TypeScript `strict`, or type hints plus a checker). No `any`/untyped shortcuts without a comment.
- Linter and formatter configured and enforced.
- `.env.example` with placeholders only; real `.env` ignored by git.
- Use an LTS Node.js release for production (verify the current LTS line at nodejs.org; Node 20 is end-of-life as of April 2026).
- Pinned dependency versions with a lockfile. Add a dependency only when it earns its place; check license, maintenance, size.
- Automated dependency updates (Dependabot or Renovate) with CI as the safety net.

## 35.2 Git workflow

| Rule | Detail |
|---|---|
| Main branch | Always deployable; protected |
| Work | Short-lived feature branches, one concern each |
| Commits | Small, frequent, meaningful (conventional commits such as `feat:`, `fix:`, `chore:`) |
| Checkpoint | Commit after **every working step** so any bad AI edit can be reverted |
| Before risky work | Tag or branch a known-good state |
| Pull requests | Description, what changed, how tested, risks; self-review before merge |
| Never commit | Secrets, `.env`, build output, large binaries, personal data |

## 35.3 CI/CD pipeline (minimum)

```text
on every push / PR:
  install (locked) -> lint -> typecheck -> unit tests -> integration tests
  -> build -> secret scan (gitleaks) -> dependency audit
on merge to main:
  deploy to STAGING -> run migrations -> smoke tests (health, login, core flow)
  -> manual approval -> deploy to PRODUCTION -> post-deploy health check
rollback: one documented command or button returning to the previous release
```

A failing step blocks the merge or deploy. Never disable a check to "get it through".

---

# 36. Production Engineering Essentials

Include each of these when applicable, or state why not.

| Capability | Standard |
|---|---|
| Error tracking | Capture frontend and backend errors with release version and request id (for example Sentry-class tool). Scrub secrets and personal data. |
| Structured logging | JSON logs, levels, request id, no sensitive data (see Section 28). |
| Metrics and uptime | Latency (p50/p95/p99), error rate, traffic, saturation; uptime monitor on `/health`. |
| Caching | Cache only what is safe; define key, TTL and invalidation. Never cache per-user data in shared caches. |
| Background jobs | Queue for slow or retryable work (email, AI, reports, webhooks) with retries, backoff, dead-letter handling and idempotency. |
| Transactional email | Dedicated provider, SPF, DKIM and DMARC configured, bounce and complaint handling, unsubscribe where required. |
| File storage | Object storage with private buckets and short-lived signed URLs; size limits; virus scan where relevant. |
| API conventions | Versioning, consistent error format, pagination, filtering, idempotency keys for payments and creates. |
| Database changes | Versioned migrations, backup before migrating, backward-compatible steps (expand, migrate, contract), tested on staging first. |
| Data lifecycle | Soft delete where recovery matters, hard delete for privacy requests, retention schedule. |
| Feature flags | For risky or staged releases; remove flags when done. |
| Time and locale | Store UTC, display local; i18n-ready strings if more than one language is planned. |
| SEO and sharing | Titles, meta, canonical, sitemap, robots, Open Graph for public pages; no SEO games. |
| Analytics | Privacy-respecting, consent-aware, events defined in the PRD; never log personal data into analytics. |
| Resilience | Timeouts on every outbound call, retries with backoff, circuit breaker for flaky providers, graceful degradation and graceful shutdown. |
| Config | Per-environment configuration; production differs from dev only by config, not code. |
| Cost control | Budgets and alerts on cloud, AI and email spend; per-user AI quotas; know the free-tier limits (Section 5). |
| Mobile / PWA | If a phone experience matters: responsive first, installable PWA where useful, touch targets, offline states. |

---

# 37. Legal, Privacy and Compliance Basics

This section is **engineering guidance, not legal advice**. Have a qualified professional review anything that handles money, health, children's data, or large amounts of personal data.

- Privacy Policy and Terms of Service, written for what the product actually does.
- Cookie and tracking consent where required; no non-essential tracking before consent.
- Data inventory: what personal data is collected, why, where stored, who can access it, how long kept.
- User rights: export and delete account and data; a working process, not just a promise.
- Applicable regimes to check for the target users: **India DPDP Act 2023 and DPDP Rules 2025** (notified 13-14 Nov 2025; phased: consent-manager rules from 13 Nov 2026; main duties such as notices, security safeguards, breach reporting, retention and children's data from **13 May 2027**; breach reporting to the Data Protection Board within 72 hours and verifiable parental consent for under-18s are part of the rules), GDPR, CCPA/CPRA and others. Verify current text and dates from official sources before relying on them.
- Payments: use a compliant provider; **never store card numbers or CVV**.
- Children: do not collect data from minors without a lawful basis and appropriate safeguards.
- AI: disclose AI use where users could be misled; do not present AI output as professional advice.
- Accessibility: target WCAG 2.2 AA and run automated checks (for example axe) plus a keyboard-only pass.
- Licenses: confirm every dependency and asset license allows the intended use.
- Breach readiness: know what must be reported, to whom and how fast (Section 40).

---

# 38. Vibe-Coder Workflow Playbook (How to Run AI Agents Reliably)

## 38.1 Rules for the agent

1. **Plan first, then code.** Show the plan in a few lines and wait only if the change is risky or destructive.
2. **Small tasks.** One feature or fix per cycle; no sprawling rewrites.
3. **Checkpoint commits** after every working step.
4. **Stop and ask** only when: a destructive action is needed, requirements conflict, credentials or paid services are required, or a legal or security choice has no safe default.
5. **Self-review** before every handoff (38.4).
6. **Update the context files** (38.2) at the end of every session.
7. **Never claim done** without running build, tests and relevant scans.

## 38.2 Context files kept in the repo

| File | Contains |
|---|---|
| `AGENTS.md` | Standing rules for any AI agent working in this repo |
| `docs/PRD.md`, `docs/TRD.md` | Product and technical truth |
| `docs/DECISIONS.md` | Short ADRs: what was decided and why |
| `docs/STATUS.md` | What works, what is broken, what is next |
| `docs/RUNBOOK.md` | How to deploy, roll back, rotate keys, restore backups |

## 38.3 `AGENTS.md` template

```text
# AGENTS.md
Project: <name> | Stack: <frontend / backend / DB / hosting>
Commands: install: <cmd> | dev: <cmd> | test: <cmd> | lint: <cmd> | build: <cmd>

RULES
- Read docs/STATUS.md and docs/DECISIONS.md before starting.
- Plan briefly, change small, run lint + typecheck + tests before saying done.
- Commit after each working step with a conventional commit message.
- Never commit secrets. Secrets are server-side environment variables only.
- Every endpoint: authenticate, authorize ownership, validate input, rate limit, log safely.
- Never use fake data in production code paths. Empty states must be honest.
- Never mark anything PASS, CONNECTED or DEPLOYED unless actually verified.
- Ask before: deleting data, changing the database schema destructively, adding paid services.
- At the end: update docs/STATUS.md and give ONE clear next step.
```

**Instruction file names:** keep `AGENTS.md` as the single source. Claude Code reads `CLAUDE.md` (put `@AGENTS.md` in it); Gemini CLI reads `GEMINI.md`; Codex, OpenCode, Cursor, Copilot and Antigravity read `AGENTS.md`. Verify against each tool's current docs (Section 58).

## 38.4 Self-review checklist (run before every handoff)

- Does it build, lint, typecheck and pass tests (real output)?
- Authentication and ownership enforced on the server for every new route?
- All new input validated on the server; no raw SQL concatenation?
- Any secret, token or key in code, logs or frontend bundle?
- Loading, empty, error and success states present?
- Mobile layout checked?
- Any placeholder, fake data, dead button or TODO left? List it honestly.
- Docs and STATUS updated?

## 38.5 Debugging protocol

1. Reproduce the problem and capture the exact error.
2. Isolate the smallest failing case.
3. State a hypothesis; check logs and evidence rather than guessing.
4. Fix the cause, not the symptom.
5. Add a regression test.
6. Re-run the full relevant suite and confirm.

If three fix attempts fail, stop, summarize what was tried and what is known, and propose a different approach instead of looping.

## 38.6 Ready-made task prompts

| Situation | Prompt to paste |
|---|---|
| New feature | "Read AGENTS.md and STATUS.md. Plan briefly, implement `<feature>` following the PRD acceptance criteria, add tests, run all checks, update STATUS.md, give one next step." |
| Bug | "Follow the debugging protocol in AGENTS.md for: `<error/steps>`. Add a regression test." |
| Refactor | "Refactor `<area>` without changing behavior. Tests must pass before and after. Small commits." |
| Review | "Act as a senior reviewer. Review the last changes for bugs, security, performance and maintainability. Severity-rank findings. Do not change code until I approve." |
| Security | Section 33 hardening prompt (`M04_SECURITY_CORE.md`) |
| Pre-launch | "Run the release checklist in Section 41 and report real results." |

---

# 39. Extra-Rigor Standards (Going Beyond a Typical Build)

These practices raise quality above what many solo or small-team projects do.

| Practice | What to do |
|---|---|
| Independent review | After building, run a **fresh-context review** (new chat or second agent) acting as a hostile reviewer: security, logic, performance, maintainability. Fix or document every finding. |
| Requirement traceability | Map each PRD acceptance criterion to at least one automated test. Unmapped criteria are listed as gaps. |
| Threat model per feature | For each new feature ask: who could abuse this, what could they get, how do we detect and limit it? |
| Failure drills | Deliberately test: database down, third-party API down, expired token, duplicate webhook, slow network, full disk. The app must fail gracefully and tell the truth. |
| Restore drill | Actually restore a backup into a clean environment once before launch. An untested backup is not a backup. |
| Rollback drill | Deploy a deliberately broken staging build and roll back. |
| Performance budgets | Use the numeric gates in Section 48 (Lighthouse, Core Web Vitals, API p95, bundle size) and fail CI when exceeded. |
| Automated accessibility | Add axe-style checks to E2E tests plus a manual keyboard pass. |
| Supply-chain hygiene | Dependency audit, lockfile review, license check, minimal dependencies; generate an SBOM if the product handles sensitive data. |
| Data safety | Verify that logs, error reports and analytics contain no secrets or personal data. |
| Honest limits | Publish a Known Limitations list in the README. |

---

# 40. Operations, Incident Response and Maintenance

## 40.1 Runbook (`docs/RUNBOOK.md`) must contain

- How to deploy, roll back and verify a release.
- How to run migrations safely and restore from backup.
- How to rotate each secret (JWT keys, database, API keys) with zero or minimal downtime.
- How to read logs and trace a request id.
- How to disable a feature quickly (flag or config).
- Contacts and provider status pages.

## 40.2 Incident response (minimum)

1. **Detect** (alerts, user report). 2. **Contain** (disable feature, block IP, revoke tokens, rotate keys). 3. **Assess** (what data or users are affected). 4. **Fix** and verify. 5. **Notify** affected users and authorities where required (Section 37). 6. **Review**: write a short post-incident note with cause, timeline and prevention.

If a secret or database credential may have leaked: rotate it immediately, review access logs, then investigate.

## 40.3 Ongoing maintenance

Weekly dependency and security alert review; monthly backup-restore check and cost review; quarterly secret rotation and access review; log retention and cleanup on schedule.

---

# 41. Release Checklist and Handover

## 41.1 Pre-release

- [ ] All PRD acceptance criteria met or listed as known gaps.
- [ ] CI green: lint, types, tests, build, gitleaks, dependency audit.
- [ ] Cross-user IDOR tests, auth tests, rate-limit tests pass (real output).
- [ ] Load test, Lighthouse and accessibility results recorded.
- [ ] Staging verified; migrations tested; backup taken; rollback ready.
- [ ] HTTPS, security headers, private database, monitoring and alerts live.
- [ ] Legal pages, cookie consent and data-deletion path in place.
- [ ] Production secrets set; no secrets in repo or frontend.
- [ ] Error tracking, uptime monitor and log access confirmed.

## 41.2 Launch and first week

- Deploy, then verify the **public URL** through the real critical journeys (sign up, verify email, login, core action, logout, reset).
- Watch errors, latency, failed logins, 429s and costs closely for 7 days.
- Fix urgent issues first; record everything in `docs/STATUS.md`.

## 41.3 Handover package

README (setup in minutes), `.env.example`, PRD, TRD, schema, API spec, runbook, decisions, known limitations, credentials inventory (names and locations, **never values**), and a post-launch roadmap.

---

# 44. Final Operating Principle

The agent's job is not merely to write code.

Its job is to help transform an idea into a **useful, coherent, secure, maintainable, tested, deployable, human-quality product**.

The correct sequence is:

**IDEA → DISCOVERY → RESEARCH → RESOURCES → PRD → UX → DESIGN SYSTEM → ARCHITECTURE → BUILD → INTEGRATE → SECURITY → TEST → DEPLOY → VERIFY → POLISH**

Do not skip verification to make progress look faster.

Do not sacrifice real functionality for visual polish.

Do not sacrifice usability for visual novelty.

Do not sacrifice truth for an impressive report.

**A smaller real product is better than a larger fake one.**

