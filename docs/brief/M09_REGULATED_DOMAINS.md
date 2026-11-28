# 54. Regulated and Sensitive Domains

This is engineering guidance, **not legal advice**. Decide in P0 whether the idea touches any domain below; record it in the PRD under "Compliance requirements"; build the controls; have a qualified professional review before launch. Obligations come from **official sources checked at the time** (Section 58), never from memory.

## 54.1 Process

1. Flag domains in P0 (use the Idea Template checkboxes).
2. For each flag, list obligations from official regulator/platform pages, with link and date checked.
3. Turn each obligation into a requirement with an acceptance test (or mark UNKNOWN and escalate to the owner).
4. Record decisions in `docs/DECISIONS.md`. Re-check before each major release.

## 54.2 Domain table

| Domain | Typical risks | Engineering controls | Check with |
|---|---|---|---|
| **Finance, market data, financial news** | Redistributing exchange/vendor data without a licence; stale or wrong prices; being treated as investment advice | Use licensed or official data sources and follow their display terms; show source, timestamp and delay on every figure; "information only, not advice" notices; no personalised buy/sell recommendations unless the legal status is confirmed; label AI-generated summaries; never store brokerage credentials; keep an audit trail of data changes | Data vendor/exchange licence terms; the securities regulator for your country (in India, SEBI rules on advisers/research analysts: verify); a lawyer |
| **News and content aggregation** | Copyright, scraping against terms | Prefer licensed APIs and feeds; respect terms and robots.txt; short attributed snippets with links; takedown process | Publisher terms, a lawyer |
| **Health and wellness** | Highly sensitive data; harmful medical claims | Data minimisation, explicit consent, encryption in transit and at rest, access logs, vendor data agreements; no diagnosis or treatment claims; emergency disclaimers | Health-data law of the target country (for example HIPAA in the US), a lawyer |
| **Children and teens** | Under-18 data (India DPDP requires verifiable parental consent), COPPA-style rules elsewhere | Age gate, avoid tracking and targeted ads, parental consent flow, minimal data, strong moderation | Current DPDP text and local law |
| **Social platforms and third-party APIs (Meta, YouTube, X, etc.)** | Policy violations, app-review rejection, token expiry, account bans | Official APIs only; request minimum scopes; complete app review and business verification where required; store tokens encrypted and server-side; handle expiry/refresh and revocation; respect rate limits; implement the platform's data-deletion callback; no fake engagement or policy-violating automation | Each platform's current developer policy |
| **User-generated content and marketplaces** | Illegal or harmful content, spam, fraud | Report/flag, moderation queue, takedown process, rate limits, blocking, audit logs; terms of use; know your local intermediary obligations | A lawyer |
| **AI-generated or synthetic media (voice, avatar, video, images)** | Impersonation, missing consent, mislabelled synthetic content | Written consent for any real person's voice or likeness; disclose synthetic or altered realistic content; follow each platform's labelling rules (for example YouTube's synthetic-content disclosure: verify current policy); watermark/provenance where feasible | Platform policy, a lawyer |
| **E-commerce and payments** | Consumer-protection, tax, card data | Section 51; clear refund/cancel policy; GST/tax invoices; never store card data | Tax professional, provider docs |
| **Web scraping and data collection** | Terms violations, personal data, copyright | Official APIs first; respect terms and robots.txt; rate-limit your crawler; do not collect personal data without a lawful basis | A lawyer |
| **Education, employment, credit and other high-stakes decisions** | Unfair or wrong automated decisions | Human review, explainability, appeal path, bias testing, no sole reliance on AI (Section 47) | Sector regulator |

## 54.3 Always

Privacy policy and terms that match actual behaviour (Section 37); data inventory; retention and deletion; breach response (Section 40); honest limits stated in the UI where users could rely on the output.

---

