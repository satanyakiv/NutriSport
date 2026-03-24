#!/bin/bash
set -euo pipefail

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

ok()   { printf "${GREEN}OK${NC}   %s\n" "$1"; }
fail() { printf "${RED}FAIL${NC} %s\n" "$1"; }
warn() { printf "${YELLOW}WARN${NC} %s\n" "$1"; }

CHECK_ONLY=false
[[ "${1:-}" == "--check" ]] && CHECK_ONLY=true

echo ""
echo "=== Dev Environment Setup (iOS Automation) ==="
echo ""

# 1. Node.js
if command -v node &>/dev/null; then
  ok "Node.js $(node --version)"
else
  fail "Node.js not found — install via nvm or brew"
  exit 1
fi

# 2. Appium
if command -v appium &>/dev/null; then
  ok "Appium $(appium --version)"
else
  if $CHECK_ONLY; then
    fail "Appium not installed"
  else
    echo "Installing Appium..."
    npm install -g appium 2>&1 | tail -3
    ok "Appium installed"
  fi
fi

# 3. XCUITest driver
WDA_DIR="$HOME/.appium/node_modules/appium-xcuitest-driver/node_modules/appium-webdriveragent"
if [[ -d "$WDA_DIR" ]]; then
  ok "XCUITest driver installed"
else
  if $CHECK_ONLY; then
    fail "XCUITest driver not installed"
  else
    echo "Installing XCUITest driver..."
    appium driver install xcuitest 2>&1 | tail -3
    ok "XCUITest driver installed"
  fi
fi

# 4. WDA build for booted simulator
BOOTED_SIM=$(xcrun simctl list devices | grep -i booted | head -1 | grep -oE '[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}' || true)
if [[ -z "$BOOTED_SIM" ]]; then
  warn "No iOS Simulator booted — skip WDA build (boot a simulator and re-run)"
else
  # Check if WDA is already built for this simulator
  WDA_HASH_DIR=$(find ~/Library/Developer/Xcode/DerivedData -maxdepth 1 -name "WebDriverAgent-*" -type d 2>/dev/null | head -1)
  WDA_RUNNER="${WDA_HASH_DIR}/Build/Products/Debug-iphonesimulator/WebDriverAgentRunner-Runner.app"

  if [[ -n "$WDA_HASH_DIR" && -d "$WDA_RUNNER" ]]; then
    ok "WDA built for simulator ($BOOTED_SIM)"
  else
    if $CHECK_ONLY; then
      fail "WDA not built — run without --check to build"
    else
      echo "Building WDA for simulator $BOOTED_SIM (first time, ~2-3 min)..."
      cd "$WDA_DIR"
      xcodebuild build-for-testing \
        -project WebDriverAgent.xcodeproj \
        -scheme WebDriverAgentRunner \
        -destination "platform=iOS Simulator,id=$BOOTED_SIM" \
        -quiet 2>&1
      cd - >/dev/null
      ok "WDA built"
    fi
  fi
fi

echo ""
echo "=== Done ==="
