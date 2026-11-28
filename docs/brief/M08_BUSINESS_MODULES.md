# 51. Payments and Subscriptions

Use only when the product charges money. Use a compliant provider (for example Stripe or Razorpay; confirm availability, fees and compliance for your country from official pages).

1. **Never touch card data.** Use the provider's hosted checkout or hosted fields so card numbers never reach your server (keeps PCI scope minimal). Never store card numbers or CVV.
2. Define plans, limits, trial, currency, tax/GST and invoice requirements in the PRD; verify tax rules with a professional.
3. **Entitlements come from server state**, updated by verified webhooks. Never grant access because the browser says "paid".
4. Webhooks: verify the provider's signature; reject unsigned/old events; process idempotently (store event ids); respond fast and handle work in a queue; handle retries and out-of-order events.
5. Handle the full lifecycle: checkout started, paid, failed, retry/dunning, cancelled, plan change/proration, refund, dispute/chargeback, expired card, grace period.
6. Test mode and live mode use **separate keys and separate environments**; live keys only in production secrets.
7. Idempotency keys on every create-charge/create-subscription call.
8. Reconcile: a scheduled job compares your records with the provider's and alerts on mismatches.
9. Store provider customer/subscription ids and status, not sensitive payment data. Log payment events with ids, not card or personal data.
10. User-facing: clear pricing, receipts/invoices, cancel and refund policy, failure messages that tell the truth.
11. Mobile in-app digital purchases may require store billing (Section 49).
12. Tests: successful payment, failed payment, duplicate webhook, forged webhook, refund, downgrade, cancel; each with real output.

---

# 52. Multi-Tenancy (Teams, Organizations, Workspaces)

Use when several organizations share the product and must never see each other's data.

| Model | Use when | Trade-off |
|---|---|---|
| **Shared database, `tenant_id` on every row + row-level security (default)** | Most SaaS | Simple and cheap; needs discipline and tests |
| Schema per tenant | Moderate isolation needs | More migration complexity |
| Database per tenant | Strict isolation/regulation, few large tenants | Highest cost and ops effort |

Rules:

1. Tenant identity comes from the **verified token/session membership**, never from a request parameter or header the client controls.
2. Every table has `tenant_id` (NOT NULL, indexed, foreign key). Every query filters by it through one central data-access layer. Enable database row-level security as a second line of defense.
3. Uniqueness is per tenant (for example unique `(tenant_id, slug)`).
4. Membership table: `memberships(user_id, tenant_id, role)`; a user may belong to many tenants; switching tenant re-authorizes.
5. Background jobs, queues, caches, search indexes, file storage and AI retrieval **carry the tenant** (key prefixes, filters). Test each.
6. Per-tenant limits and rate limiting; per-tenant usage and billing records.
7. Export and delete per tenant; offboarding removes data on schedule (Section 37).
8. Admin/support "impersonation" is explicit, time-limited, audited and visible to the tenant.
9. Mandatory tests: a user in Tenant A cannot read, write, delete, list, search, export or receive notifications about Tenant B data via any endpoint, job or AI feature.

---

# 53. Roles and Permissions (RBAC)

1. Define roles in the PRD, for example: **owner, admin, member, viewer**, plus a separate platform **super-admin** that is not a normal tenant role.
2. Store roles in the database; never decide permissions from client-side flags or JWT claims alone without server checks.
3. One central permission function: `can(user, action, resource)`; routes and queries call it. No scattered `if (role === ...)` checks.
4. Deny by default; grant the minimum; ownership check **and** role check both apply.
5. Invitation flow: signed, expiring, single-use invites; role chosen by an authorized inviter; cannot grant a role higher than your own.
6. Prevent lockout and privilege abuse: at least one owner remains; role changes are audited; admin actions logged (Section 28).
7. Hide UI the user cannot use, but **always enforce on the server**.

Permission matrix (fill this in the PRD; generate tests from it):

| Resource / action | Owner | Admin | Member | Viewer | Anonymous |
|---|---|---|---|---|---|
| View own item | yes | yes | yes | yes | no |
| View others' items in tenant | yes | yes | per rule | per rule | no |
| Create item | yes | yes | yes | no | no |
| Edit own item | yes | yes | yes | no | no |
| Edit others' items | yes | yes | no | no | no |
| Delete item | yes | yes | own only | no | no |
| Manage members | yes | limited | no | no | no |
| Billing | yes | no | no | no | no |
| Delete tenant | yes | no | no | no | no |

Automated test rule: for **every cell**, one test proves the allowed action succeeds and each forbidden action fails with `403`/`404`.

---

