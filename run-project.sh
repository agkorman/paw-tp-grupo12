#!/usr/bin/env bash
# Usage: bash run-project.sh
# Purpose: repeatable steps to export env vars and start the app.

set -euo pipefail

# Always run from repo root (where this script lives)
cd "$(dirname "$0")"

# Resolve JDK 21 for build and runtime
resolve_java_home() {
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    /usr/libexec/java_home -v 21 2>/dev/null || true
  fi
}

JAVA_HOME="$(resolve_java_home)"

# Fallback to Homebrew path if java_home didn’t return anything
if [ -z "${JAVA_HOME}" ]; then
  BREW_JAVA_HOME="$(brew --prefix openjdk@21 2>/dev/null)/libexec/openjdk.jdk/Contents/Home"
  if [ -x "${BREW_JAVA_HOME}/bin/java" ]; then
    JAVA_HOME="${BREW_JAVA_HOME}"
  fi
fi

if [ -z "${JAVA_HOME}" ] || [ ! -x "${JAVA_HOME}/bin/java" ]; then
  echo "ERROR: JDK 21 not found. Install with: brew install openjdk@21" >&2
  exit 1
fi

export JAVA_HOME
export PATH="${JAVA_HOME}/bin:${PATH}"

# Verify we are really on Java 21
JAVA_VER="$("${JAVA_HOME}/bin/java" -version 2>&1 | head -n1)"
if [[ "${JAVA_VER}" != *"21"* ]]; then
  echo "ERROR: Expected Java 21 but got: ${JAVA_VER}" >&2
  exit 1
fi

# Load environment variables from .env if present
if [ -f .env ]; then
  set -a
  . ./.env
  set +a
fi

echo "JAVA_HOME=$JAVA_HOME"
echo "DB_URL=${DB_URL-}"

# Build and run
mvn clean install
mvn -pl webapp jetty:run
