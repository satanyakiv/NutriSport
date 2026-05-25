#!/usr/bin/env bash
# Send a test FCM push via the Firebase Cloud Messaging HTTP v1 API.
#
# Zero external dependencies beyond what macOS / the repo already has:
#   python3 (stdlib json only — NO google-auth needed)
#   openssl (LibreSSL is fine — RS256 sign)
#   curl, base64
#
# Auth: a Firebase service-account JSON key. Obtain it ONCE from
#   Firebase Console → Project Settings → Service accounts →
#   "Generate new private key". Save it to (default):
#       ~/.nutrisport/firebase/service-account.json
#   NEVER commit it. It lives outside the repo by design.
#
# Usage:
#   fcm-send.sh --token <REGISTRATION_TOKEN> --title "Hi" --body "Test"
#   fcm-send.sh --topic news --title "News" --body "Body" --data key=val --data k2=v2
#   fcm-send.sh --token <TOKEN> --title T --body B --dry-run     # prints payload, no send
#
# Options:
#   --token <t>     device registration token (mutually exclusive with --topic)
#   --topic <name>  send to a topic instead of a single device
#   --title <s>     notification title
#   --body <s>      notification body
#   --data k=v      optional data payload entry (repeatable)
#   --sa <path>     service-account json (default ~/.nutrisport/firebase/service-account.json)
#   --dry-run       build + print the FCM payload, skip the token mint + send
#
# Exit codes: 0 success, 1 usage/arg error, 2 auth/token error, 3 FCM API error.

set -euo pipefail

SA_PATH="${HOME}/.nutrisport/firebase/service-account.json"
TOKEN="" TOPIC="" TITLE="" BODY="" DRY_RUN=0
DATA_KEYS=() DATA_VALS=()

while [ $# -gt 0 ]; do
  case "$1" in
    --token)   TOKEN="$2"; shift 2 ;;
    --topic)   TOPIC="$2"; shift 2 ;;
    --title)   TITLE="$2"; shift 2 ;;
    --body)    BODY="$2";  shift 2 ;;
    --sa)      SA_PATH="$2"; shift 2 ;;
    --dry-run) DRY_RUN=1; shift ;;
    --data)
      k="${2%%=*}"; v="${2#*=}"
      DATA_KEYS+=("$k"); DATA_VALS+=("$v"); shift 2 ;;
    *) echo "ERROR: unknown arg '$1'" >&2; exit 1 ;;
  esac
done

if [ -z "$TOKEN" ] && [ -z "$TOPIC" ]; then
  echo "ERROR: one of --token or --topic is required" >&2; exit 1
fi
if [ -n "$TOKEN" ] && [ -n "$TOPIC" ]; then
  echo "ERROR: --token and --topic are mutually exclusive" >&2; exit 1
fi
if [ -z "$TITLE" ] || [ -z "$BODY" ]; then
  echo "ERROR: --title and --body are required" >&2; exit 1
fi

b64url() { openssl base64 -A | tr '+/' '-_' | tr -d '='; }

build_fcm_payload() {
  python3 - "$@" <<'PY'
import json, sys
token, topic, title, body = sys.argv[1:5]
data = {}
for kv in sys.argv[5:]:
    k, _, v = kv.partition("=")
    data[k] = v
msg = {"notification": {"title": title, "body": body}}
if token:
    msg["token"] = token
else:
    msg["topic"] = topic
if data:
    msg["data"] = data
print(json.dumps({"message": msg}))
PY
}

# Reassemble --data pairs for the python helper
DATA_ARGS=()
for i in "${!DATA_KEYS[@]}"; do DATA_ARGS+=("${DATA_KEYS[$i]}=${DATA_VALS[$i]}"); done
FCM_PAYLOAD="$(build_fcm_payload "$TOKEN" "$TOPIC" "$TITLE" "$BODY" "${DATA_ARGS[@]:-}")"

# --dry-run is credential-free on purpose: lets you iterate on payload
# shape without needing the service-account key at all.
if [ "$DRY_RUN" -eq 1 ]; then
  echo "DRY RUN — payload that would POST to FCM v1 messages:send:"
  echo "$FCM_PAYLOAD" | python3 -m json.tool
  exit 0
fi

if [ ! -f "$SA_PATH" ]; then
  echo "ERROR: service-account key not found at $SA_PATH" >&2
  echo "Get it: Firebase Console → Project Settings → Service accounts →" >&2
  echo "        Generate new private key. Save to $SA_PATH (chmod 600, never commit)." >&2
  exit 2
fi

# --- extract SA fields via python3 stdlib (no google-auth) ---
read -r CLIENT_EMAIL TOKEN_URI PROJECT_ID < <(python3 - "$SA_PATH" <<'PY'
import json, sys
d = json.load(open(sys.argv[1]))
print(d["client_email"], d.get("token_uri", "https://oauth2.googleapis.com/token"), d["project_id"])
PY
)

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
KEY_PEM="$WORK/sa_key.pem"
( umask 077; python3 - "$SA_PATH" > "$KEY_PEM" <<'PY'
import json, sys
print(json.load(open(sys.argv[1]))["private_key"], end="")
PY
)

# --- mint OAuth2 access token (JWT bearer grant, RS256) ---
NOW=$(date +%s); EXP=$((NOW + 3600))
HEADER='{"alg":"RS256","typ":"JWT"}'
CLAIM=$(python3 - "$CLIENT_EMAIL" "$TOKEN_URI" "$NOW" "$EXP" <<'PY'
import json, sys
iss, aud, iat, exp = sys.argv[1:5]
print(json.dumps({
    "iss": iss,
    "scope": "https://www.googleapis.com/auth/firebase.messaging",
    "aud": aud, "iat": int(iat), "exp": int(exp),
}, separators=(",", ":")))
PY
)
H_B64=$(printf '%s' "$HEADER" | b64url)
C_B64=$(printf '%s' "$CLAIM"  | b64url)
SIGNING_INPUT="${H_B64}.${C_B64}"
SIG_B64=$(printf '%s' "$SIGNING_INPUT" | openssl dgst -sha256 -sign "$KEY_PEM" | b64url)
ASSERTION="${SIGNING_INPUT}.${SIG_B64}"

TOKEN_RESP=$(curl -s -X POST "$TOKEN_URI" \
  --data-urlencode "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer" \
  --data-urlencode "assertion=${ASSERTION}")
ACCESS_TOKEN=$(printf '%s' "$TOKEN_RESP" | python3 -c 'import json,sys; print(json.load(sys.stdin).get("access_token",""))')

if [ -z "$ACCESS_TOKEN" ]; then
  echo "ERROR: failed to mint access token. Response:" >&2
  echo "$TOKEN_RESP" >&2
  exit 2
fi

# --- send the FCM message ---
FCM_URL="https://fcm.googleapis.com/v1/projects/${PROJECT_ID}/messages:send"
RESP=$(curl -s -w '\n%{http_code}' -X POST "$FCM_URL" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "$FCM_PAYLOAD")
HTTP_CODE=$(printf '%s' "$RESP" | tail -1)
BODY_RESP=$(printf '%s' "$RESP" | sed '$d')

echo "$BODY_RESP" | python3 -m json.tool 2>/dev/null || echo "$BODY_RESP"
if [ "$HTTP_CODE" = "200" ]; then
  echo "OK: FCM message accepted (HTTP 200)."
  exit 0
else
  echo "ERROR: FCM API returned HTTP $HTTP_CODE" >&2
  exit 3
fi
