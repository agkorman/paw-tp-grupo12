#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if [ -x /opt/homebrew/opt/openjdk@21/bin/java ]; then
  export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
else
  echo "Error: .env file not found. Copy and fill in DB_* and MAIL_* variables." >&2
  exit 1
fi

mvn clean install -DskipTests
mvn -pl webapp jetty:run
