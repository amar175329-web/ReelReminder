# 50. Hosting and Deployment Playbooks

Pick the hosting target in P3, record it in an ADR, then follow its checklist in P10. Always verify current limits, pricing and docs from the provider's official pages.

## 50.1 Choosing

| Target | Good for | Trade-offs |
|---|---|---|
| **Serverless / PaaS (Vercel, Netlify, Cloudflare, Render, Railway, Fly.io)** | Next.js/frontends, small APIs, previews, least ops | No long-running processes on pure serverless; execution limits; database is external; costs grow with usage |
| **VPS / cloud VM (AWS EC2 Ubuntu or similar)** | Full control, long-running jobs, one box for app + DB at small scale | **You own patching, firewall, backups, monitoring**; single point of failure |
| **Managed database (Supabase, Neon, AWS RDS, etc.)** | Production data without running Postgres yourself | Free-tier limits; must configure access rules correctly |
| **Containers on a VM (Docker Compose + reverse proxy)** | Repeatable deploys on a VPS | Slightly more setup, much better reproducibility |

## 50.2 AWS EC2 / Ubuntu VPS checklist (example: 2 vCPU, 8 GB RAM, no GPU)

**Access and OS**
- [ ] Non-root sudo user; SSH **key-only** login; password login and root login disabled (`PasswordAuthentication no`, `PermitRootLogin no`).
- [ ] Protect the private key with a passphrase; keep it only on devices you trust. Never paste keys, tokens or `.env` contents into chats.
- [ ] Firewall: security group allows 80 and 443 from anywhere; **22 only from your IP if possible**; everything else closed. Add `ufw` as a second layer.
- [ ] `fail2ban` (or equivalent) for SSH; automatic security updates (`unattended-upgrades`).
- [ ] Use IMDSv2 and an IAM **role** for AWS access; no long-lived AWS keys on the server.
- [ ] Swap configured; disk and memory alerts set.

**App runtime**
- [ ] Node from an LTS release (Section 46) or Docker image pinned by version.
- [ ] Run the app as a non-root user under **systemd** (or Docker Compose with restart policy / PM2).
- [ ] **Do not build on the server** if it runs out of memory; build in CI and deploy the artifact.
- [ ] Reverse proxy (Caddy or Nginx) terminates HTTPS with automatic certificates (Let's Encrypt); redirect HTTP to HTTPS; HSTS; security headers; request size and timeout limits; rate limiting at the proxy as a first layer.
- [ ] App listens on localhost only; only the proxy is public.
- [ ] Environment variables in a root-owned file (mode 600) or secrets manager; not in git, not in the image.
- [ ] Elastic IP and DNS records documented in the runbook.

**Database**
- [ ] PostgreSQL bound to localhost (or a private subnet); **never exposed publicly**; strong unique credentials; app uses a least-privilege DB user, not the superuser.
- [ ] Automated `pg_dump`/snapshots **to storage off the server** (for example an S3 bucket with versioning and encryption); retention schedule; **restore tested once**.
- [ ] Migrations run as a separate deploy step with a backup taken first.

**Operations**
- [ ] Log rotation; logs shipped or at least retained with size limits.
- [ ] Uptime monitor on `/health`; error tracking (Section 36); alerts for CPU, memory, disk, 5xx, failed logins.
- [ ] Zero-downtime-ish deploy: start new version, health check, switch, keep previous release for rollback.
- [ ] Run load tests from a different machine, against staging.
- [ ] Working from a phone over SSH (for example Termius): use `tmux` or `screen` so long tasks survive disconnects; use key authentication; avoid editing production by hand: change code through git and CI.

## 50.3 Vercel-style serverless checklist

- [ ] Environment variables set per environment (Preview vs Production); secrets only in server-side variables; anything with a public prefix is visible to users.
- [ ] Understand function limits (execution time, memory, request size) from current docs; move long jobs to a queue or worker.
- [ ] No local file storage: use object storage and an external database.
- [ ] Database connection pooling configured for serverless (connection limits are a classic outage cause).
- [ ] Preview deployments protected (no real data, no production secrets); production branch protected.
- [ ] Cron/scheduled jobs verified in provider docs; secure cron endpoints with a secret or provider signature.
- [ ] Security headers set in config; custom domain with HTTPS; rollback via previous deployment.
- [ ] Spend limits and usage alerts enabled.

## 50.4 Supabase / managed Postgres checklist

- [ ] **Row-Level Security enabled on every table** exposed through the auto-generated API, with explicit policies; a table without RLS is publicly readable by anyone holding the public key.
- [ ] Public (anon) key may be in the frontend; **service-role / secret keys never** (Section 30).
- [ ] Direct database connection strings server-side only; network restrictions or allowlists where offered.
- [ ] Backups and point-in-time recovery: confirm what the plan includes; restore tested.
- [ ] Auth settings: email verification on, redirect URLs allowlisted, password rules, rate limits.
- [ ] Storage buckets private by default with signed URLs.

## 50.5 Deployment gates (all hosting types)

HTTPS live, secrets set, database private, migrations applied, backup taken, health check green, smoke tests pass, monitoring and alerts active, rollback tested, public URL verified by real requests (Section 41). **Never call it live until checked.**

---

