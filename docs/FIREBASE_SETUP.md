# Firebase Setup

## Firestore Security Rules

The project requires server-side security rules deployed to Firebase. The rules file lives at `firestore.rules` in the repository root.

### Deploying Rules

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Deploy rules
firebase deploy --only firestore:rules
firebase deploy --only storage
```

### What the Rules Enforce

| Collection                        | Read                            | Write                                       |
| --------------------------------- | ------------------------------- | ------------------------------------------- |
| `customer/{uid}`                  | Owner only (`auth.uid == uid`)  | Owner only                                  |
| `customer/{uid}/privateData/role` | Owner only                      | Denied (backend only)                       |
| `product/{id}`                    | Any authenticated user          | Admin only (checked via `privateData/role`) |
| `order/{id}`                      | Owner only (`customerId` field) | Owner only                                  |

### Admin Role Setup

The `isAdmin` flag is stored in `customer/{uid}/privateData/role`. It cannot be written by clients (rules deny all writes). To grant admin access:

**Option A — Firebase Console (manual):**

1. Go to Firebase Console → Firestore Database
2. Navigate to `customer/{uid}/privateData/role`
3. Set `isAdmin: true`

**Option B — Cloud Function (recommended for production):**

```typescript
// functions/src/index.ts
import * as admin from "firebase-admin";
admin.initializeApp();

export const setAdminRole = functions.https.onCall(async (data, context) => {
  // Only allow existing admins to promote others
  const callerUid = context.auth?.uid;
  if (!callerUid) throw new functions.https.HttpsError("unauthenticated", "");

  const callerRole = await admin
    .firestore()
    .doc(`customer/${callerUid}/privateData/role`)
    .get();
  if (!callerRole.data()?.isAdmin) {
    throw new functions.https.HttpsError("permission-denied", "");
  }

  await admin
    .firestore()
    .doc(`customer/${data.targetUid}/privateData/role`)
    .set({ isAdmin: true });
});
```

## Storage Rules

Storage rules at `storage.rules` restrict image uploads to admin users only. Maximum file size: 5 MB, images only.

## Firebase Config Files

Firebase config files (`google-services.json`, `GoogleService-Info.plist`) are **not committed to git**. They are distributed via:

1. **Local development:** Stored in `~/Documents/NutriSport/`, copied into the project by a setup script
2. **CI/CD:** Injected from GitHub Secrets (`GOOGLE_SERVICES_JSON`, `GOOGLE_SERVICE_INFO_PLIST`)

### Local Development Setup

Source-of-truth files live outside the repository in `~/Documents/NutriSport/`:

```
~/Documents/NutriSport/
  debug/
    google-services.json            — Android debug (com.portfolio.nutrisport.debug)
    GoogleService-Info.plist         — iOS debug
  release/
    google-services.json            — Android release (com.portfolio.nutrisport)
    GoogleService-Info.plist         — iOS release
  benchmark/
    google-services.json            — Android benchmark
```

**First-time setup:**

1. Create the directory structure:
   ```bash
   mkdir -p ~/Documents/NutriSport/{debug,release,benchmark}
   ```
2. Download `google-services.json` from [Firebase Console](https://console.firebase.google.com/) (Project Settings > Your apps) for each build variant, or get them from a team member
3. Place files in the corresponding directories
4. Run the setup script:
   ```bash
   ./scripts/setup-firebase.sh
   ```

**After `git clean -fdx` or fresh clone** — re-run the script:

```bash
./scripts/setup-firebase.sh
```

**Check mode** — verify files are in place without copying:

```bash
./scripts/setup-firebase.sh --check
```

### Expected Project Locations

The setup script copies files to these paths:

```
androidApp/google-services.json              — default (fallback, copy of release)
androidApp/src/debug/google-services.json    — debug build type
androidApp/src/release/google-services.json  — release build type
androidApp/src/benchmark/google-services.json — benchmark build type
iosApp/iosApp/GoogleService-Info.plist       — iOS app
```

All paths are in `.gitignore` — they never appear in `git status`.

## Crashlytics

Firebase Crashlytics is integrated for automatic crash reporting in production builds. Setup is minimal — Crashlytics Gradle plugin + native SDK dependency in `androidApp`, with collection disabled in debug builds.

For full details see [CRASHLYTICS.md](CRASHLYTICS.md).

## Related

- [Crashlytics Documentation](CRASHLYTICS.md) — crash reporting setup, MCP integration, analysis skills
- [CI Documentation](CI.md) — CI/CD pipeline and secrets configuration
- [Security Documentation](SECURITY.md) — security fixes and OWASP compliance
