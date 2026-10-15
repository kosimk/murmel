#!/usr/bin/env bash
# Checks the one-time machine setup needed to build Murmel. Read-only: changes nothing.
# Usage: ./scripts/check-setup.sh   (or VS Code: Terminal → Run Task… → Check setup)

ok()   { printf "  \033[32m✔\033[0m %s\n" "$1"; }
bad()  { printf "  \033[31m✘\033[0m %s\n" "$1"; FAIL=1; }
hint() { printf "      → %s\n" "$1"; }
FAIL=0
cd "$(dirname "$0")/.." || exit 1

echo "Java (needed to start Gradle)"
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA="$(command -v java)"
fi
if [ -n "$JAVA" ]; then
  VER="$("$JAVA" -version 2>&1 | grep -v "^Picked up" | head -1)"
  MAJOR="$(echo "$VER" | sed -E 's/.*version "([0-9]+).*/\1/')"
  if [ "${MAJOR:-0}" -ge 17 ] 2>/dev/null; then ok "$VER"; else bad "Java 17+ required, found: $VER"; fi
  [ -n "$JAVA_HOME" ] && ok "JAVA_HOME=$JAVA_HOME" || { bad "JAVA_HOME is not set"; hint "see docs/SETUP.md, step 2"; }
else
  bad "No Java found"; hint "install Android Studio (bundles a JDK) and set JAVA_HOME, see docs/SETUP.md"
fi

echo "Android SDK"
SDK="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
if [ -z "$SDK" ] && [ -f local.properties ]; then
  SDK="$(grep '^sdk.dir=' local.properties | cut -d= -f2-)"
fi
if [ -n "$SDK" ] && [ -d "$SDK" ]; then ok "SDK at $SDK"; else bad "Android SDK not found"; hint "set ANDROID_HOME, see docs/SETUP.md, step 2"; fi

echo "adb (talks to your phone)"
if command -v adb >/dev/null 2>&1; then
  ok "$(adb version | head -1)"
  DEVICES="$(adb devices | sed '1d' | grep -c 'device$')"
  [ "$DEVICES" -gt 0 ] && ok "$DEVICES phone(s) connected" || hint "no phone connected (fine for building, needed to install)"
else
  bad "adb not on PATH"; hint "add \$ANDROID_HOME/platform-tools to PATH"
fi

echo "Network (first build downloads Gradle + libraries)"
if command -v curl >/dev/null 2>&1; then
  for host in services.gradle.org dl.google.com repo.maven.apache.org; do
    if curl -s -o /dev/null -m 10 "https://$host"; then ok "$host reachable"; else bad "$host not reachable"; hint "check VPN / proxy / firewall"; fi
  done
fi

echo
[ "$FAIL" -eq 0 ] && echo "All good. Build with: ./gradlew assembleDebug" || echo "Fix the ✘ items above, then run this again."
exit "$FAIL"
