# 27. Authorization and IDOR Prevention

Authentication proves who the user is. Authorization proves what they may touch. Review **every** endpoint and database query.

Rules:

1. **Deny by default.** Every route requires an explicit auth and permission decision.
2. The acting user comes from the verified token, **never from a request field** such as `userId` in the body or URL.
3. Every read, update and delete is scoped by owner/tenant: `WHERE id = :id AND owner_id = :currentUserId`.
4. Enforce ownership in one central layer (policy or repository function), not scattered per route.
5. For another user's resource return `404` (or a uniform denial), not data and not a revealing error.
6. Use UUIDs, not sequential IDs, but do not rely on that for security.
7. Use database row-level security where the database supports it.
8. Check ownership of related objects too (for example, an `orderId` that belongs to another user's `projectId`).
9. Protect bulk, export, search, file-download and webhook endpoints the same way.
10. Admin actions need explicit role checks and audit logs.

**Required test for every resource endpoint:** User A creates a resource; User B must fail to read, update and delete it. Record the results as real test output.

---

# 28. Secure Deployment and Observability

| Area | Requirement |
|---|---|
| HTTPS | Enforce HTTPS everywhere, redirect HTTP, enable HSTS. |
| Secrets | Store in the platform secrets manager or protected environment variables. Never in the repo, image or frontend bundle. |
| Database | Not reachable from the public internet. Private network or IP allowlist, strong credentials, least-privilege DB user for the app. |
| Server | Firewall with only needed ports, SSH key-only, no password SSH login, automatic security updates. |
| Headers | CSP, X-Content-Type-Options, frame protections, Referrer-Policy, Permissions-Policy, restrictive CORS (explicit origins only). |
| Logging | Structured logs for authentication attempts, authorization denials, API errors, validation failures, rate-limit hits, unusual traffic patterns. |
| Log hygiene | Never log passwords, tokens, API keys, full card or personal data. Include request id for tracing. |
| Alerts | Spikes in failed logins, many 401/403/429, new-IP admin logins, 5xx surges, unusual request volume from one IP or key. |
| Backups | Automated, encrypted, restore actually tested. |
| Rollback | A tested way to return to the previous version. |
| Health | `/health` endpoint and uptime monitoring. |

---

# 29. Abuse Protection

Implement rate limiting and bot protection. The numbers below are **starting points to tune**, not guarantees.

| Target | Starting limit | Keyed by |
|---|---|---|
| Login | 5 attempts / 15 min, then backoff | account + IP |
| Account creation | 3 / hour | IP (+ device signals) |
| Password reset / email verification resend | 3 / hour | account + IP |
| General API | 100 requests / min | user, or IP if anonymous |
| Expensive or search endpoints | 20 / min | user |
| AI generation | Per-minute limit plus daily quota and hard spending cap | user (and plan) |
| File upload | Small count per hour plus size cap | user |

Also:

- Return `429` with `Retry-After`. Use a shared store (for example Redis) when running multiple instances.
- Add CAPTCHA or bot challenge (for example Turnstile or reCAPTCHA) on signup and on login after repeated failures.
- Cap page size and total records per request; require pagination; limit export size.
- Detect scraping patterns (high sequential access, no browser signals) and throttle or block.
- Set request body size limits and timeouts.
- Consider a WAF/CDN layer for volumetric abuse.
- AI endpoints: per-user cost accounting, max tokens, prompt-length limits, no unauthenticated access.

---

# 30. Secrets and Credentials Scan

1. Scan the whole project **and git history**: `gitleaks git -v` (history) and `gitleaks dir -v .` (files on disk). The older `gitleaks detect` command is deprecated since v8.19.0.
2. Search code for API keys, DB URLs, service-role keys, JWT secrets, tokens, private keys.
3. Treat any secret ever committed as **compromised: rotate it**. Deleting it from the latest commit is not enough.
4. Move all secrets to server-side environment variables or a secrets manager. Provide `.env.example` with placeholders only.
5. Ensure `.env*` is in `.gitignore`.
6. Frontend rule: anything with a public prefix (for example `NEXT_PUBLIC_`, `VITE_`, `REACT_APP_`) is visible to everyone. Only genuinely public values may use it. Service keys and database credentials never.
7. Use secrets only in server code; the browser calls your API, and your API calls third parties.
8. Add gitleaks as a pre-commit hook (`gitleaks git --pre-commit --staged`, or the pre-commit framework hook) and a CI step so leaks fail the build.

---

# 31. Input Validation and Upload Safety

First create an **input inventory**: every form, API body, query parameter, path parameter, header, cookie, webhook payload and file upload.

| Threat | Required defense |
|---|---|
| SQL injection | Parameterized queries or ORM only. Never build SQL by string concatenation. |
| Command injection | Never pass user input to a shell. If unavoidable, use argument arrays with strict allowlists. |
| Script injection (XSS) | Output encoding, framework auto-escaping, sanitize any HTML input, CSP. |
| Mass assignment | Accept only an allowlist of fields; never bind the raw request body to a model. |
| Type confusion | Strict schema validation on the server (for example Zod, Pydantic, Joi) with exact types, ranges, lengths, formats. Reject invalid data with `400`/`422`. |
| SSRF / open redirect | Allowlist outbound URLs and redirect targets. |
| Unsafe uploads | Size limit, extension and **content-type check by magic bytes**, allowlist of types, random server-side filenames, store outside the web root or in object storage, never execute uploads, scan for malware where relevant, serve with safe headers. |
| Path traversal | Never use user input in file paths directly. |

Client-side validation is for usability only. **Server-side validation is the security control.**

---

# 32. Verification Toolkit (Run for Real, Report Real Results)

Pass/fail numbers are defined in Section 48 (`M05_QUALITY_GATES.md`). Commands below were checked against current tool documentation.

| Check | Tool / command | Notes |
|---|---|---|
| Secrets in code and history | `gitleaks git -v` and `gitleaks dir -v .` | Separate binary (brew, apt, Docker, GitHub release), not an npm package. `detect`/`protect` are deprecated. Rotate anything found. |
| Load / stress test | `npx autocannon -c 50 -d 30 <staging-url>` | `-c` connections, `-d` seconds, `-p` pipelining. HTTP/1.1 only. Own staging only, ideally from a different machine. |
| Performance, accessibility, SEO, best practices | `npx lighthouse <url> --only-categories=performance,accessibility,best-practices,seo --output=json --output=html --output-path=./lighthouse-report` | Needs Node 22+ and Chrome/Chromium. Run 3 times and use the median. |
| Dependency vulnerabilities | `npm audit --omit=dev` (or `pip-audit`) | Triage by severity and exploitability. |
| Web security scan | OWASP ZAP baseline scan | Staging only. |
| Rate-limit proof | Scripted repeated login/API calls; confirm `429` | Include in tests. |
| IDOR proof | Cross-user automated tests (Section 27) | Mandatory for every resource endpoint. |
| E2E | Playwright or equivalent | Critical journeys plus auth flows. |
| Security headers / TLS | securityheaders.com, SSL Labs | Public URL only. |

Rules: a tool that cannot run here is reported **NOT RUN** with the reason and exact instructions to run it. Never write PASS without actual output. Include command, date, environment and result summary.

---

# 33. Security Hardening Master Prompt (Copy/Paste for an Existing or New Project)

```text
Act as a senior security engineer and full-stack developer. I am a vibe coder, so do the work yourself,
give exact commands, and end with ONE clear next step. Never fabricate results.
First inspect the project (stack, auth, database, endpoints, deployment) and list every finding
before changing anything. Preserve working functionality and real data.

1. AUTHENTICATION
Review the authentication system and make it secure. Ensure passwords are securely hashed (Argon2id or
bcrypt), sessions expire, email verification is enabled, password reset tokens are single-use and expire,
login attempts are rate limited, and authentication secrets are never exposed to the frontend.
Use short-lived JWT access tokens with rotating refresh tokens in HttpOnly cookies. Refactor insecure logic.

2. AUTHORIZATION / IDOR
Review all API endpoints and database queries. Ensure every request verifies the logged-in user owns the
data being accessed. Prevent IDOR by enforcing ownership checks before reading, modifying, or deleting any
resource. Add automated cross-user tests.

3. SECURE DEPLOYMENT
Enforce HTTPS, store secrets securely, restrict direct database access from the public internet, and add
logging for authentication attempts, API errors, and unusual traffic patterns so suspicious behavior
can be detected.

4. ABUSE PROTECTION
Implement rate limiting for login attempts, API endpoints, account creation, and AI generation requests.
Prevent bots or scripts from repeatedly calling endpoints or scraping data.

5. SECRETS
Scan the entire project and git history for secrets and credentials (gitleaks). Ensure API keys, database
service keys, and tokens are never exposed in frontend code or committed. Move all secrets to server-side
environment variables. List anything that must be rotated.

6. INPUT VALIDATION
Identify every place user input enters the system (forms, APIs, uploads, query parameters). Add strict
server-side validation and sanitization against SQL injection, command injection, script injection, and
unsafe file uploads. Reject invalid data and enforce strict types.

7. DELIVERABLES
Produce: findings list (severity-ranked), changes made, tests actually run with real results,
remaining risks, items NOT IMPLEMENTED, and the single next step.
Also produce or update: PRD, TRD, app flow, UI/UX brief, backend schema, API spec, implementation plan.
```

---

