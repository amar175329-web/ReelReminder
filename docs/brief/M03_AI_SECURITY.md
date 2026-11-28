# 47. AI / LLM / Agent Security

Applies to any feature that sends user or business data to a language model, retrieves documents for a model (RAG), or lets a model call tools.

Baseline: **OWASP Top 10 for LLM Applications** (2025 edition risks listed below; a 2026 edition has been published that keeps all ten areas with some renaming and reranking: check owasp.org for the current text) and, for tool-using agents, the **OWASP Top 10 for Agentic Applications** (December 2025).

## 47.1 Risk map

| Risk | What goes wrong | Required controls |
|---|---|---|
| Prompt injection (LLM01) | Text from a user, web page, email or file tells the model to ignore rules, leak data or misuse tools | Keep trusted instructions separate from untrusted content; mark and isolate retrieved/user text; never let model output grant permissions; least-privilege tools; human approval for risky actions |
| Sensitive information disclosure (LLM02) | Model reveals other users' data, secrets or private context | Send the minimum data; strip secrets and unnecessary personal data; never put API keys or credentials in prompts; per-user data isolation |
| Supply chain (LLM03) | Malicious or compromised models, plugins, packages, datasets | Pin and review dependencies; use trusted providers; check licenses and data policies |
| Data and model poisoning (LLM04) | Bad data steers behavior | Control what can enter knowledge bases; review sources; version datasets |
| Improper output handling (LLM05) | Model output used as code, SQL, HTML or shell commands | Treat output as untrusted: validate against a schema, encode before rendering, never `eval`/exec, never run generated SQL or shell without a sandbox and approval |
| Excessive agency (LLM06) | Model has too many tools or permissions | Allowlist tools; scope each tool to the current user; read-only by default; confirm destructive, financial or external-message actions |
| System prompt leakage (LLM07) | Users extract instructions | Assume the system prompt can leak; never store secrets or access rules in it; enforce rules in code |
| Vector/embedding weaknesses (LLM08) | RAG returns another user's documents or poisoned chunks | Filter by owner/tenant **before** retrieval; per-tenant indexes or metadata filters; validate sources |
| Misinformation (LLM09) | Confident wrong answers are acted on | Ground answers in retrieved sources with citations; allow "I don't know"; label AI output; human review for high-stakes use |
| Unbounded consumption (LLM10) | Cost blow-ups, denial of service | Per-user rate limits and daily quotas, max input/output tokens, timeouts, hard monthly spend cap with alert, authentication required |

## 47.2 Build rules

1. Provider API keys exist only on the server. The browser calls **your** API, never the provider directly.
2. Define in writing what the AI may do, may not do, refusal behavior, and when it escalates to a human.
3. Version prompts and model configuration in the repo; log prompt version with each call (without secrets or unnecessary personal data).
4. Retrieval: apply the logged-in user's permissions to every retrieved document. Test that User A cannot retrieve User B's content.
5. Structured output: request JSON, validate with a strict schema, reject or retry on failure.
6. Tools: each tool call is authorized server-side as the current user; log tool name, arguments summary, result status.
7. Uploads sent to a model follow file-validation rules (Section 31); treat file content as untrusted instructions.
8. Check the provider's data retention and training policy and disclose AI use to users where it could mislead (Section 37).
9. Provide fallback behavior when the provider is down or rate limited; show honest error states.
10. Cap cost: tokens per request, requests per minute, daily quota per user and plan, global monthly budget alert.

## 47.3 AI evaluation suite (must exist and be run)

| Category | Example checks |
|---|---|
| Accuracy | Known questions with expected facts; citation present and correct |
| Refusal | Disallowed requests are refused politely |
| Prompt injection | Instructions hidden in user text, documents and web content do not change behavior or leak data |
| Data isolation | User A prompts never surface User B data |
| Output safety | Output containing HTML, SQL or shell fragments is neutralized |
| Tool misuse | Model cannot call tools outside the user's permissions |
| Cost/latency | p95 latency and tokens per request within budget; abuse attempts hit limits |
| Regression | Re-run after any prompt, model or retrieval change |

Report real results: counts passed/failed per category, latency, cost per request. If evals were not run: **NOT RUN**.

---

