# Security

Security measures applied to NutriSport, mapped to OWASP Mobile Top-10 (2024).

## Audit Summary

| OWASP | Category           | Status | Severity      |
| ----- | ------------------ | ------ | ------------- |
| M1    | Credentials        | Fixed  | Critical/High |
| M3    | Auth/Authz         | Fixed  | Critical      |
| M5    | Communication      | Fixed  | Medium        |
| M6    | Privacy            | Fixed  | Medium        |
| M7    | Binary Protections | Fixed  | Low           |
| M8    | Configuration      | Fixed  | Medium        |
| M9    | Data Storage       | Fixed  | High          |

## M3 — Server-Side Admin Authorization

**Vulnerability:** Admin operations (create/update/delete products, upload images) only checked `isAdmin` on the client side. Any authenticated user could call admin APIs directly.

**Fix:**

- Added `withAdminAuth()` helper in `FirestoreExt.kt` that queries `customer/{uid}/privateData/role` from Firestore before allowing mutations
- All admin write operations in `AdminRepositoryImpl` now use `withAdminAuth()` instead of `withAuth()`
- Deployed `firestore.rules` enforcing server-side admin verification for the `product` collection
- Deployed `storage.rules` restricting image uploads to admin users

**Files:** `firestore.rules`, `storage.rules`, `network/.../FirestoreExt.kt`, `network/.../AdminRepositoryImpl.kt`

## M1 — Credential Protection

**Vulnerability:** Firebase config files (`google-services.json`, `GoogleService-Info.plist`) and `GOOGLE_WEB_CLIENT_ID` were committed to git. Debug keystore also tracked.

**Fix:**

- Removed all Firebase config files and debug keystore from git tracking
- Added `**/google-services.json`, `**/GoogleService-Info.plist`, `*.keystore`, `*.jks` to `.gitignore`
- Moved `GOOGLE_WEB_CLIENT_ID` from hardcoded `Constants.kt` to `local.properties` + `BuildConfig`
- CI workflows inject Firebase configs from GitHub Secrets at build time
- Created `local.properties.example` for developer onboarding

**Files:** `.gitignore`, `Constants.kt`, `androidApp/build.gradle.kts`, `NutrisportApplication.kt`, CI workflows

## M9 — Data Cleanup on Sign-Out

**Vulnerability:** `signOut()` only called `Firebase.auth.signOut()` without clearing the local Room database. PII (name, email, phone, address, cart, orders) remained accessible on the device.

**Fix:**

- `signOut()` now clears customer data and cart items from Room before returning
- User ID is captured before Firebase sign-out (since `currentUserId()` returns null after sign-out)

**Files:** `network/.../CustomerRepositoryImpl.kt`

## M8 — Backup Protection

**Vulnerability:** `android:allowBackup="true"` allowed ADB backups of the app's SQLite database containing PII.

**Fix:**

- Set `android:allowBackup="false"` and `android:fullBackupContent="false"` in AndroidManifest.xml

**Files:** `androidApp/src/main/AndroidManifest.xml`

## M5 — Network Security

**Vulnerability:** No `network_security_config.xml`. Cleartext traffic not explicitly blocked.

**Fix:**

- Created `network_security_config.xml` blocking all cleartext HTTP traffic
- Referenced via `android:networkSecurityConfig` in AndroidManifest.xml

**Files:** `androidApp/src/main/res/xml/network_security_config.xml`, `AndroidManifest.xml`

## M6 — Privacy Controls

**Vulnerability:** Customer ID logged in plaintext during checkout. No `FLAG_SECURE` on sensitive screens.

**Fix:**

- Removed customer ID from Napier log in `CheckoutViewModel`
- Added `FLAG_SECURE` to `MainActivity` in release builds (prevents screenshots and task switcher preview)

**Files:** `feature/.../CheckoutViewModel.kt`, `androidApp/.../MainActivity.kt`

## M7 — Binary Protections

**Vulnerability:** `debug.keystore` committed to git.

**Fix:**

- Removed from git tracking. `.gitignore` now covers `*.keystore` and `*.jks`
- Release builds already use R8 minification and resource shrinking

**Files:** `.gitignore`

## Not Covered (and Why)

- **M9 SQLCipher** — database encryption is overkill for a portfolio project. Mitigated by disabling backups and clearing data on sign-out
- **M2 Gradle verification** — `verification-metadata.xml` adds significant maintenance overhead for a portfolio project
- **M4 Regex validation** — basic length validation is acceptable for a demo app. Firestore is not vulnerable to SQL injection

## Related

- [Firebase Setup](FIREBASE_SETUP.md) — deploying security rules, admin role setup
- [CI Documentation](CI.md) — CI/CD pipeline and secrets
