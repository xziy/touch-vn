#!/usr/bin/env bash
# Builds the wasm bundle and runs the Playwright suite against it. Extra args go to playwright.
set -euo pipefail
cd "$(dirname "$0")"
(cd .. && ./gradlew :composeApp:wasmJsBrowserDistribution --console=plain -q)
[ -d node_modules/@playwright/test ] || npm install --no-audit --no-fund
npx playwright test "$@"
