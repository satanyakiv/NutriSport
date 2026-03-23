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

Firebase config files (`google-services.json`, `GoogleService-Info.plist`) are **not committed to git**. They must be:

1. **Local development:** Place files manually in the expected paths
2. **CI/CD:** Injected from GitHub Secrets (`GOOGLE_SERVICES_JSON`, `GOOGLE_SERVICE_INFO_PLIST`)

### Expected File Locations

```
androidApp/google-services.json              — default (fallback)
androidApp/src/debug/google-services.json    — debug build type
androidApp/src/release/google-services.json  — release build type
iosApp/iosApp/GoogleService-Info.plist       — iOS app
```

## Related

- [CI Documentation](CI.md) — CI/CD pipeline and secrets configuration
- [Security Documentation](SECURITY.md) — security fixes and OWASP compliance
