#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DEFAULT_SOURCE="$HOME/Documents/NutriSport"

SOURCE_DIR="$DEFAULT_SOURCE"
CHECK_ONLY=false

usage() {
  cat <<EOF
Usage: $(basename "$0") [--source <path>] [--check]

Copies Firebase config files from a local secure directory into the project.

Options:
  --source <path>   Source directory (default: ~/Documents/NutriSport)
  --check           Only verify files are in place, don't copy

Expected source structure:
  <source>/debug/google-services.json
  <source>/debug/GoogleService-Info.plist
  <source>/release/google-services.json
  <source>/release/GoogleService-Info.plist
  <source>/benchmark/google-services.json
EOF
  exit 1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --source) SOURCE_DIR="$2"; shift 2 ;;
    --check)  CHECK_ONLY=true; shift ;;
    -h|--help) usage ;;
    *) echo "Unknown option: $1"; usage ;;
  esac
done

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

ok()   { printf "${GREEN}OK${NC}   %s\n" "$1"; }
fail() { printf "${RED}MISS${NC} %s\n" "$1"; }
skip() { printf "${YELLOW}SKIP${NC} %s (source missing)\n" "$1"; }

validate_json() {
  python3 -c "import json, sys; json.load(open(sys.argv[1]))" "$1" 2>/dev/null
}

# Mapping: source_relative -> project_relative
declare -a MAPPINGS=(
  "debug/google-services.json|androidApp/src/debug/google-services.json"
  "release/google-services.json|androidApp/src/release/google-services.json"
  "release/google-services.json|androidApp/google-services.json"
  "benchmark/google-services.json|androidApp/src/benchmark/google-services.json"
  "debug/GoogleService-Info.plist|iosApp/iosApp/GoogleService-Info.plist"
)

copied=0
missing=0
skipped=0

echo ""
if $CHECK_ONLY; then
  echo "Checking Firebase config files..."
else
  echo "Setting up Firebase config files..."
  echo "Source: $SOURCE_DIR"
fi
echo ""

if [[ ! -d "$SOURCE_DIR" ]]; then
  echo -e "${RED}ERROR:${NC} Source directory not found: $SOURCE_DIR"
  echo ""
  echo "Create it and place Firebase configs there:"
  echo "  mkdir -p $SOURCE_DIR/{debug,release,benchmark}"
  exit 1
fi

for mapping in "${MAPPINGS[@]}"; do
  src_rel="${mapping%%|*}"
  dst_rel="${mapping##*|}"
  src="$SOURCE_DIR/$src_rel"
  dst="$PROJECT_ROOT/$dst_rel"

  if $CHECK_ONLY; then
    if [[ -f "$dst" ]]; then
      ok "$dst_rel"
      ((copied++))
    else
      fail "$dst_rel"
      ((missing++))
    fi
    continue
  fi

  if [[ ! -f "$src" ]]; then
    skip "$dst_rel"
    ((skipped++))
    continue
  fi

  if [[ "$src" == *.json ]] && ! validate_json "$src"; then
    echo -e "${RED}ERROR:${NC} Invalid JSON: $src"
    ((missing++))
    continue
  fi

  mkdir -p "$(dirname "$dst")"
  cp "$src" "$dst"
  ok "$dst_rel"
  ((copied++))
done

echo ""
echo "---"
echo "Copied: $copied  Skipped: $skipped  Missing: $missing"

if [[ $missing -gt 0 ]]; then
  exit 1
fi
