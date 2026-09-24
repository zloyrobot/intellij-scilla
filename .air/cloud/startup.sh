#!/usr/bin/env bash

set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

if [ "${AIR_STARTUP_MODE:-}" = warmup ]; then
    WARMUP=1
else
    WARMUP=
fi

healthcheck() {
    echo "Checking the Gradle build and packaged IntelliJ plugin..."
    ./gradlew --no-daemon test buildPlugin

    shopt -s nullglob
    local distributions=(build/distributions/*.zip)
    if [ "${#distributions[@]}" -eq 0 ] || [ ! -s "${distributions[0]}" ]; then
        echo "Plugin distribution was not produced in build/distributions." >&2
        return 1
    fi
    echo "Environment ready: ${distributions[0]}"
}

if [ -n "${WARMUP:-}" ]; then
    echo "Warming Gradle, Java toolchain, IntelliJ Platform, and build caches..."
    healthcheck
else
    echo "Task startup complete; cached build dependencies are available."
fi