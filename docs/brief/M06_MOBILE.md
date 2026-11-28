# 49. Mobile Apps (Android, iOS, PWA)

## 49.1 Decide the delivery type

| Option | Choose when | Notes |
|---|---|---|
| Responsive web | Most products at MVP | Cheapest; test on real phones |
| **PWA** | Installable web app is enough, no store needed | No App Store presence; limited iOS capabilities |
| **Cross-platform native (default: Expo / React Native)** | Need store apps, push notifications, camera, offline, one codebase | Shares TypeScript with Stack A/B |
| Flutter | Team prefers Dart or needs custom rendering | Separate language from web stack |
| Fully native (Kotlin / Swift) | Heavy platform-specific features | Two codebases; avoid unless required |

Record the choice and reason as an ADR.

## 49.2 Mobile security rules

1. Tokens and secrets in the platform secure store (Keychain / Android Keystore, e.g. `expo-secure-store`), **never** plain AsyncStorage or files.
2. Auth: OAuth 2.0 authorization code with **PKCE** via the system browser; refresh tokens rotated; session revocable server-side.
3. The app binary is public: **no API secrets inside the app**. Anything shipped in the app is extractable. Call your backend; the backend holds secrets.
4. All traffic over HTTPS; consider certificate pinning only if you can manage pin rotation.
5. Validate deep links and intents; never trust their parameters.
6. Request the minimum permissions, at the moment of need, with a clear reason.
7. Same server-side rules as web: ownership checks, validation, rate limits.
8. Protect release signing keys and keystores: offline backup, never in git.
9. Crash and error reporting without personal data; privacy-aware analytics.

## 49.3 Store requirements (verify before every release)

| Store | Requirement (as of Nov 2026) |
|---|---|
| Google Play | New apps and app updates must target **Android 16 (API level 36)** since 31 Aug 2026 (extension to 1 Nov 2026 was possible if requested). Check the Play Console target-API page before each release. |
| Both stores | A privacy policy URL, accurate data-safety / privacy-label declarations, and an **in-app way to delete the account** and data if accounts can be created. |
| Apple App Store | Follow the current App Review Guidelines: login rules (check Guideline 4.8 if you offer third-party sign-in), in-app purchase rules for digital goods, privacy manifest and required SDK/Xcode versions (check the Apple developer site for the current minimum). |
| Payments | Digital goods/subscriptions inside apps generally must use store billing; physical goods and services may use other providers. Verify current policy. |

If a requirement cannot be verified from the official page, mark it **UNKNOWN** and tell me where to check.

## 49.4 Delivery checklist

- [ ] Real-device tests (low-end Android, recent iPhone if iOS is targeted), poor network, offline, background/foreground, permission denied.
- [ ] Adaptive layouts for phones and tablets/foldables (Android is removing large-screen opt-outs).
- [ ] Push notifications: consent flow, token storage, unsubscribe.
- [ ] Over-the-air updates only for JS changes allowed by store policy; native changes need a store release.
- [ ] Versioning and a minimum-supported-version check from the backend (force update for critical security fixes).
- [ ] Store listing assets, screenshots, privacy forms, test credentials for reviewers.
- [ ] Staged rollout (percentage) with crash monitoring and a rollback plan.
- [ ] Builds produced by CI or EAS-style cloud build, not by a hand-built laptop artifact.

---

