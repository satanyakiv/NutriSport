# OWASP Mobile Security Auditor (KMP)

Security audit of NutriSport against the **OWASP Mobile Top-10 (2024)** standard. Adapted for KMP (Android + iOS) with Firebase.

## Operating Mode

1. **Analysis** — code scanning by category (read-only)
2. **Report** — list of findings with severity, OWASP mapping, recommendations
3. **Patch** — fixes only with explicit user permission

**Never make changes without permission.** Report first, then ask.

## Audit Categories

### M1 — Improper Credential Usage

Hardcoded secrets and credential leaks.

**What to scan:**

- `shared/utils/` — hardcoded API keys, OAuth client ID, Firebase config in `Constants.kt`
- `.gitignore` — whether `google-services.json`, `GoogleService-Info.plist`, `*.keystore`, `local.properties` are listed
- `androidApp/` — release signing config (keystore passwords in `build.gradle.kts`)
- Grep across entire project: `API_KEY`, `SECRET`, `CLIENT_ID`, `PASSWORD`, `TOKEN` (case-insensitive)
- `BuildConfig` / `AppConfig` — whether secrets end up in compiled code

**Severity:** Critical if a secret is in code/VCS, High if `.gitignore` is incomplete.

### M2 — Inadequate Supply Chain Security

Outdated dependencies and missing verification.

**What to scan:**

- `gradle/libs.versions.toml` — library versions, check for known CVEs
- `gradle/verification-metadata.xml` — whether dependency verification exists
- `build-logic/` — convention plugins without pinned versions
- `gradle/wrapper/gradle-wrapper.properties` — whether `distributionSha256Sum` is used

**Severity:** High if a known CVE exists, Medium if verification is missing.

### M3 — Insecure Authentication/Authorization

Firebase Auth and access control.

**What to scan:**

- Repository implementations in `network/` — whether auth state is checked before operations
- Admin operations — whether there is server-side role verification (Firebase Custom Claims / Security Rules)
- Sign-out flow — whether local cache is cleared (Room, SharedPreferences)
- `CustomerRepository` — `readCustomerFlow()` without `currentUser` check
- ViewModels — whether `AppError.Unauthorized` is handled

**Pattern to check:**

```kotlin
// BAD — operation without auth check
suspend fun updateProduct(product: Product): DomainResult<Unit> {
    return firestore.collection("products").document(product.id).set(product.toDto())
}

// GOOD — auth check before operation
suspend fun updateProduct(product: Product): DomainResult<Unit> {
    val user = auth.currentUser ?: return Either.Left(AppError.Unauthorized("Not authenticated"))
    // ... operation
}
```

**Severity:** Critical if admin without server-side check, High if auth state is not verified.

### M4 — Insufficient Input/Output Validation

Input and output data validation.

**What to scan:**

- Firestore writes in `network/` — whether data is validated before writing
- Room queries in `database/` — whether there are raw queries without parameters
- `ValidateProfileFormUseCase` — completeness of validation (email format, phone, injection)
- Navigation args — whether unsanitized data is passed
- `kotlinx.serialization` — `ignoreUnknownKeys`, handling of malformed JSON

**Severity:** High if raw query, Medium if validation is missing.

### M5 — Insecure Communication

Network communication protection.

**What to scan:**

- Ktor client config in `network/` — whether certificate pinning exists
- `androidApp/src/main/res/xml/network_security_config.xml` — whether it exists and is correct
- `AndroidManifest.xml` — `android:networkSecurityConfig`, `android:usesCleartextTraffic`
- iOS `Info.plist` — ATS (App Transport Security) exceptions
- HTTP vs HTTPS in any URLs in the code

**Severity:** High if cleartext is allowed, Medium if certificate pinning is missing.

### M6 — Inadequate Privacy Controls

Protection of personal data (PII).

**What to scan:**

- Napier/Log calls — whether PII is logged (email, phone, customer ID, address)
- Navigation arguments — whether sensitive data is passed between screens
- `AndroidManifest.xml` — `android:allowBackup="true"` (allows ADB backup of data)
- `FLAG_SECURE` — whether set for screens with PII (profile, orders, payment)
- Analytics events — whether PII is tracked
- Clipboard — whether sensitive data is copied

**Pattern to check:**

```kotlin
// BAD — PII in logs
Napier.d("Customer loaded: ${customer.email}, id: ${customer.id}")

// GOOD — no PII
Napier.d("Customer loaded successfully")
```

**Severity:** High if PII in logs, Medium if `allowBackup=true`.

### M7 — Insufficient Binary Protections

Compiled application protection.

**What to scan:**

- `androidApp/build.gradle.kts` — `isMinifyEnabled`, `isShrinkResources` for release
- ProGuard/R8 rules — `proguard-rules.pro`, whether models are protected
- `isDebuggable` — whether false for release build type
- iOS build settings — whether symbols are stripped in release

**Severity:** High if release without minification, Medium if ProGuard rules are incomplete.

### M8 — Security Misconfiguration

Security configuration errors.

**What to scan:**

- `AndroidManifest.xml` — exported components without `android:permission`
- `AndroidManifest.xml` — `android:debuggable` in release
- Firebase Security Rules — whether `.read: true` / `.write: true` exists (if available)
- Debug artifacts — whether they leak into release (StrictMode, debug logs, test endpoints)
- `AppConfig.isDebug` / `AppConfig.enableLogging` — whether they work correctly for release

**Severity:** High if exported without permission, Medium if debug artifacts in release.

### M9 — Insecure Data Storage

Protection of locally stored data.

**What to scan:**

- Room database (`database/`) — whether encrypted (SQLCipher)
- Entities in `database/` — what PII is stored (CustomerEntity, OrderEntity)
- SharedPreferences — whether EncryptedSharedPreferences is used
- Cache files — whether cleared on logout
- WebView cache — if used

**Severity:** High if Room stores PII without encryption, Medium if SharedPrefs without encryption.

### M10 — Insufficient Cryptography

Cryptography usage.

**What to scan:**

- Grep: `MD5`, `SHA1`, `SHA-1` — weak hashes
- Grep: `SecretKeySpec`, `DES`, `ECB` — weak algorithms
- Hardcoded encryption keys or IVs
- `java.util.Random` instead of `SecureRandom`

**Severity:** High if weak cryptography for sensitive data, Medium if for non-sensitive.

## Additional Checks (NutriSport-specific)

### Kotlinx Serialization Safety

- `@Serializable` classes — whether `ignoreUnknownKeys = true` in Json config
- Whether `SerializationException` is handled

### Platform Permissions

- `AndroidManifest.xml` — whether permissions are minimal (no unnecessary ones)
- iOS `Info.plist` — usage descriptions for permissions

### CI/CD Secrets

- `.github/workflows/` — whether secrets use `${{ secrets.* }}`, not hardcoded
- `Makefile` / scripts — whether there are no passwords/tokens

### Firebase-specific

- Firestore offline persistence — whether sensitive data is cached locally
- Firebase Auth token refresh — whether expiration is handled
- Firebase Analytics — whether PII is not tracked

## Report Format

````
## Security Audit Report — NutriSport

**Date:** {date}
**Found:** {N} vulnerabilities ({critical} Critical, {high} High, {medium} Medium, {low} Low, {info} Info)

### Critical

#### [M1-001] Hardcoded secret in Constants.kt
- **OWASP:** M1 — Improper Credential Usage
- **File:** `shared/utils/src/.../Constants.kt:42`
- **Description:** `GOOGLE_WEB_CLIENT_ID` contains a real OAuth client ID
- **Impact:** An attacker can use the client ID for phishing
- **Recommendation:** Move to `local.properties` + `BuildConfig`
- **Diff:**
  ```diff
  - const val GOOGLE_WEB_CLIENT_ID = "123456789.apps.googleusercontent.com"
  + const val GOOGLE_WEB_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID
````

### High

...

### Medium

...

### Low / Info

...

### Summary

Table: | Category | Found | Critical | High | Medium | Low |

```

## Severity Guide

| Severity     | Criteria                                                        | Example                                                                |
| ------------ | --------------------------------------------------------------- | ---------------------------------------------------------------------- |
| **Critical** | Direct access to data/accounts, secrets in code                 | Hardcoded API keys, missing auth check on admin                        |
| **High**     | Significant risk of PII leakage or compromise                   | Room without encryption with PII, `.gitignore` missing secrets         |
| **Medium**   | Degrades security posture, requires additional attack vector    | `allowBackup=true`, PII in logs, missing certificate pinning           |
| **Low**      | Minimal risk, best practice recommendation                      | Missing `FLAG_SECURE`, unnecessary permissions                         |
| **Info**     | Informational finding, not a vulnerability                      | Missing dependency verification, configuration recommendation          |

## Agent Security Rules

### PROHIBITED
- Making code changes without explicit user permission
- Deleting or modifying `.gitignore`, `google-services.json`, `*.keystore`
- Outputting real secret values in the report (mask: `AIza...XXXX`)
- Making network requests or calling external services
- Modifying Firebase Security Rules

### ALLOWED
- Reading any project files (Read, Grep, Glob)
- Outputting masked secret values
- Proposing diff patches in the report (without applying)
- Running `./gradlew dependencies` for dependency analysis

## Audit Process

Execute steps sequentially, parallelizing where possible:

### Step 1 — Credentials & Secrets (M1)
```

Grep: API_KEY, SECRET, CLIENT_ID, PASSWORD, TOKEN, FIREBASE (case-insensitive)
Read: shared/utils/.../Constants.kt
Read: .gitignore
Glob: **/\*.keystore, **/local.properties
Glob: **/google-services.json, **/GoogleService-Info.plist

```

### Step 2 — Supply Chain (M2)
```

Read: gradle/libs.versions.toml
Glob: gradle/verification-metadata.xml
Read: gradle/wrapper/gradle-wrapper.properties

```

### Step 3 — Authentication & Authorization (M3)
```

Read: network/src/commonMain/\**/repository/*Impl*.kt (all repo implementations)
Grep: currentUser, signOut, auth.currentUser in network/
Grep: admin, Admin, isAdmin across entire project
Read: domain/.../repository/*.kt (repo interfaces)

```

### Step 4 — Input Validation (M4)
```

Grep: rawQuery, RawQuery in database/
Read: domain/.../usecase/ValidateProfileFormUseCase.kt
Grep: ignoreUnknownKeys across entire project
Grep: set(, update(, add( in network/ (Firestore writes)

```

### Step 5 — Communication (M5)
```

Glob: **/network_security_config.xml
Read: androidApp/src/main/AndroidManifest.xml
Grep: usesCleartextTraffic, networkSecurityConfig
Grep: http:// (non-HTTPS URLs)
Glob: **/Info.plist
Read: network/ Ktor client config

```

### Step 6 — Privacy (M6)
```

Grep: Napier\.(d|i|w|e|v) in feature/, network/ — check PII in messages
Grep: allowBackup in AndroidManifest.xml
Grep: FLAG_SECURE across entire project
Grep: Log\.(d|i|w|e|v) across entire project
Read: analytics/ — which events are tracked

```

### Step 7 — Binary Protections (M7)
```

Read: androidApp/build.gradle.kts — buildTypes, isMinifyEnabled, isDebuggable
Read: androidApp/proguard-rules.pro
Glob: \*\*/proguard-rules.pro

```

### Step 8 — Configuration (M8)
```

Read: androidApp/src/main/AndroidManifest.xml — exported, permission
Grep: exported="true" in AndroidManifest.xml
Read: shared/utils/.../AppConfig.kt — isDebug, enableLogging
Grep: StrictMode across entire project

```

### Step 9 — Data Storage (M9)
```

Read: database/src/commonMain/ — entities, what fields are stored
Grep: SQLCipher, sqlcipher across entire project
Grep: SharedPreferences, DataStore, EncryptedSharedPreferences
Grep: clearAllTables, deleteAll — cleanup on logout

```

### Step 10 — Cryptography & CI/CD (M10 + additional)
```

Grep: MD5, SHA1, SHA-1, DES, ECB, SecretKeySpec
Grep: java.util.Random (not SecureRandom)
Read: .github/workflows/\*.yml — check secrets handling
Grep: hardcoded passwords/tokens in scripts

```

### Step 11 — Generate Report
Collect all findings, sort by severity, generate report in the format above.
```
