# 26. Authentication Design (JWT-Based, Secure by Default)

Choose the simplest secure approach. If JWT is used, follow these rules.

| Area | Requirement |
|---|---|
| Password storage | **Argon2id** with at least the OWASP minimum (m=19 MiB, t=2, p=1), raised as far as login latency allows (hash time under 1 s; RFC 9106 gives stronger profiles). Legacy only: bcrypt cost 10+ (12 common) with its 72-byte input limit; rehash to Argon2id at next login. Never plaintext, never fast hashes (SHA-256), never reversible encryption. |
| Password policy | Minimum 12 characters (NIST guidance favors 15 for password-only accounts; verify current NIST SP 800-63B), allow long passphrases (64+), check against breached-password lists, no arbitrary composition rules, no forced periodic rotation. |
| Access token | Short-lived (about 10-15 minutes). Minimal claims: user id, role, expiry. No sensitive data in payload. |
| Refresh token | Long-lived but finite (for example 7-30 days), random, **rotated on every use**, stored hashed in DB, reuse detection revokes the whole token family. |
| Token storage | Refresh token in `HttpOnly; Secure; SameSite` cookie. Never store tokens in `localStorage`. Access token in memory only. |
| JWT validation | Pin the algorithm (reject `none`), verify signature, `exp`, `iss`, `aud`. Use a strong secret or asymmetric keys (RS256/EdDSA). Support key rotation. |
| Session expiry | Idle timeout and absolute timeout. Logout revokes the refresh token server-side. |
| Email verification | Required before full access. Random single-use token, stored hashed, expires (for example 24 hours). |
| Password reset | Random single-use token (at least 256-bit), stored hashed, expires in 15-60 minutes. On success, invalidate the token and revoke all sessions. |
| User enumeration | Identical responses and timing for "email exists" and "email does not exist" on login, signup and reset. |
| Brute force | Rate limit and progressive delay per account and per IP (Section 29). Optional temporary lockout with alerting. |
| CSRF | Required when cookies carry auth: SameSite plus CSRF token for state-changing requests. |
| MFA | Recommended for admin roles; optional for users. |
| Secrets | JWT secrets and keys exist only on the server, loaded from environment or secrets manager. Never in frontend code, never in the repo. |
| Logging | Log login success/failure, resets, token reuse, role changes. Never log passwords, tokens or secrets. |

**Mobile apps:** the cookie rules above are for browsers. Native apps use OAuth 2.0 authorization code with PKCE and keep tokens in Keychain/Keystore (Section 49).

Auth flow summary:

```text
SIGNUP -> create user (unverified) -> send verification email -> verify -> active
LOGIN -> verify password (hash compare) -> issue access token + rotating refresh cookie
REQUEST -> verify access token -> load user -> authorize ownership -> respond
EXPIRED ACCESS -> call /auth/refresh -> rotate refresh token -> new access token
REUSED REFRESH TOKEN -> revoke family -> force re-login -> log security event
LOGOUT -> revoke refresh token -> clear cookie
RESET -> request (generic response) -> email link -> set new password -> revoke all sessions
```

When refactoring existing auth, audit first, list each insecure pattern found, then fix it. Do not silently rewrite working login flows without tests.

---

# 45. Authentication: Build vs Buy Decision

**Default for a vibe coder: use a managed or well-established auth solution, not hand-written auth.** Auth bugs are the most expensive bugs. Choose custom JWT (Section 26) only with a written reason.

| Option | Choose when | Watch out for |
|---|---|---|
| Managed auth service (hosted login, e.g. Supabase Auth, Clerk, Auth0, Firebase Auth) | MVP, small team, need email verification, social login, MFA fast | Vendor lock-in, pricing and free-tier limits (verify current), data location, outage dependency |
| Established auth library inside your app (e.g. Better Auth, Auth.js, Lucia-style libraries; verify each is still maintained) | Want data in your own DB, moderate control | You still own configuration mistakes; keep the library updated |
| Custom JWT/session auth (Section 26) | Special requirements, data residency, no vendor allowed, or strong reason | You own every security detail; needs the full test suite in Section 26 |
| Add MFA / passkeys | Admin roles, money, sensitive data | Recovery flow must also be secure |

Decision steps the agent must follow:

1. List requirements: roles, social login, MFA, email verification, SSO, data residency, budget, mobile app yes/no.
2. Check current pricing, limits and maintenance status of each candidate from official sources.
3. Pick the simplest option that meets requirements. Record it as an ADR in `docs/DECISIONS.md` (choice, alternatives, reason, exit plan).
4. Regardless of option, the app itself must still do: server-side verification of the session/token on every request, ownership checks (Section 27), rate limits (Section 29), audit logging (Section 28).
5. Never implement your own cryptography. Use vetted libraries (for example a maintained JOSE library for JWT, Argon2 library for hashing).
6. Managed-service keys: public/anon keys may be in the frontend only if the service is designed for it and **row-level security or equivalent is enforced**; service-role / secret keys stay server-side only.
7. Plan the exit: how users and password hashes could be exported if you leave the provider.

Mobile apps: use OAuth 2.0 authorization code with PKCE and the system browser; store tokens in Keychain/Keystore (Section 49).

---

