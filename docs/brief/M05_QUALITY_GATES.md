# 48. Quality Gates (Numbers That Define "Pass")

**Gate = must meet or the release is blocked (or the gap is listed honestly and I approve).**
**Target = aim for it; explain misses.**
These are recommended defaults, not legal standards. Change a number only with a written reason in `docs/DECISIONS.md`.
Measure on a production-like build (staging), never against a third-party site. Record command, date, environment and real output.

## 48.1 Security gates

| Check | Gate | How |
|---|---|---|
| Secrets in code and history | **0 findings** (or all rotated and documented) | `gitleaks git -v` and `gitleaks dir -v .` |
| Dependency vulnerabilities | **0 critical and 0 high in production dependencies**; others triaged | `npm audit --omit=dev` (or `pip-audit`) |
| Cross-user access (IDOR) | **100% of resource endpoints** have an automated test where User B cannot read, update or delete User A's data | Test suite |
| Authentication tests | Signup, verify, login, refresh, reuse-detection, logout, reset (valid, expired, reused) all covered | Test suite |
| Rate limits | Login, signup, reset, API and AI endpoints return `429` when limits are exceeded | Scripted test |
| Web scan | **0 high-severity** findings on staging | OWASP ZAP baseline scan |
| TLS and headers | HTTPS only, HSTS, CSP and other headers present | securityheaders.com, SSL Labs on public URL |
| Database exposure | Not reachable from the public internet | Port scan from outside / security group review |

## 48.2 Performance gates

| Metric | Gate | Target |
|---|---|---|
| Lighthouse Performance (mobile, median of 3 runs) | >= 70 | >= 90 |
| Lighthouse Accessibility | >= 90 | >= 95 |
| Lighthouse Best Practices | >= 90 | >= 95 |
| Lighthouse SEO (public pages) | >= 90 | >= 95 |
| Core Web Vitals (75th percentile, field or lab proxy) | LCP <= 2.5 s, INP <= 200 ms, CLS <= 0.1 | Better |
| API latency under expected peak load | p95 <= 500 ms (reads <= 300 ms) | p95 <= 300 ms |
| API errors under expected peak load | **< 1% non-2xx** (excluding intended 4xx tests) | 0 |
| Initial JavaScript (gzipped) | <= 300 KB app pages, <= 200 KB public/marketing pages | Smaller |
| Health endpoint | `/health` returns 200 in <= 200 ms | Faster |
| Password hash time | 100 ms to 1 s per hash on production hardware (OWASP: under 1 s) | 200-500 ms |

"Expected peak load" must be written in the TRD (for example peak concurrent users x 2). If unknown: state the assumption.

## 48.3 Quality gates

| Check | Gate |
|---|---|
| Build, lint, typecheck | 0 errors (warnings reviewed) |
| Unit + integration tests | 100% pass; critical business logic covered |
| Coverage | >= 70% overall, >= 90% for auth, authorization, payments |
| E2E | All critical journeys pass (sign up, verify, login, core action, logout, reset) |
| Accessibility | WCAG 2.2 AA target; automated checks (axe-style) show **0 critical or serious** issues; keyboard-only pass done |
| Responsive | Verified at phone, tablet, desktop widths |
| Backup | **Restore tested once** into a clean environment; time recorded |
| Rollback | Rollback to previous release tested on staging |
| PRD traceability | Every acceptance criterion maps to a test or is listed as a gap |
| Honesty | Report contains no PASS without evidence |

## 48.4 Commands (verified against current documentation, Nov 2026)

```text
# Secrets: gitleaks has three modes: git, dir, stdin (detect/protect are deprecated since v8.19.0)
gitleaks git -v                          # scan git history of current repo
gitleaks dir -v .                        # scan files on disk
gitleaks git --pre-commit --staged -v    # pre-commit style scan of staged changes

# Load test (HTTP/1.1; -c connections, -d seconds, -p pipelining). Own staging only.
npx autocannon -c 50 -d 30 https://staging.example.com/api/health

# Lighthouse (needs Node 22+ and Chrome/Chromium installed; categories: performance, accessibility, best-practices, seo)
npx lighthouse https://staging.example.com --only-categories=performance,accessibility,best-practices,seo --output=json --output=html --output-path=./lighthouse-report

# Dependencies
npm audit --omit=dev
```

Notes: `gitleaks` is a separate binary (brew, apt, Docker or GitHub release), not an npm package. Lighthouse in a headless server needs a browser installed; run it from your own machine or CI against the staging URL if the server has none. Run load tests from a different machine than the one under test, otherwise the numbers lie.

## 48.5 Reporting format

| Check | Command/tool | Date | Environment | Result (real numbers) | Gate | PASS / FAIL / NOT RUN |
|---|---|---|---|---|---|---|

---

