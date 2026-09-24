#!/usr/bin/env bash
# Environment startup script for the intellij-scilla IntelliJ plugin project.
#
# The project is built with Gradle (wrapper 7.1.1) + the gradle-intellij-plugin and
# targets Java 11 (see .github/workflows/gradle.yml). The image ships only a very new
# JBR, which Gradle 7.1.1 cannot run on, so we provision a Temurin 11 JDK here.
#
# Everything expensive (JDK download, Gradle distribution, IntelliJ IDEA SDK,
# dependency + build caches) happens on disk so it is captured in the warm snapshot.

set -euo pipefail

REPO_DIR="${REPO_DIR:-/workspaces/intellij-scilla}"
JDK_DIR="$HOME/.jdks/temurin-11"
JDK_URL="https://api.adoptium.net/v3/binary/latest/11/ga/linux/x64/jdk/hotspot/normal/eclipse"

log() { echo "[startup] $*"; }

install_jdk11() {
  if [ -x "$JDK_DIR/bin/javac" ]; then
    log "Temurin 11 already present at $JDK_DIR"
    return
  fi
  log "Downloading Temurin 11 JDK"
  local tmp
  tmp="$(mktemp -d)"
  curl -fsSL -o "$tmp/jdk11.tar.gz" "$JDK_URL"
  mkdir -p "$tmp/jdk" "$HOME/.jdks"
  tar -xzf "$tmp/jdk11.tar.gz" -C "$tmp/jdk" --strip-components=1
  rm -rf "$JDK_DIR"
  mv "$tmp/jdk" "$JDK_DIR"
  rm -rf "$tmp"
  "$JDK_DIR/bin/java" -version
}

# Gradle / the JVM do not honour HTTP(S)_PROXY, so translate the environment proxy
# into JVM system properties. Rewritten on every boot in case the proxy changes.
configure_gradle() {
  mkdir -p "$HOME/.gradle"
  local props="$HOME/.gradle/gradle.properties"
  local proxy="${HTTPS_PROXY:-${https_proxy:-}}"
  {
    echo "org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8"
    echo "org.gradle.java.home=$JDK_DIR"
    if [ -n "$proxy" ]; then
      local hostport host port
      hostport="${proxy#*://}"
      hostport="${hostport%/}"
      host="${hostport%%:*}"
      port="${hostport##*:}"
      [ "$port" = "$host" ] && port=80
      echo "systemProp.http.proxyHost=$host"
      echo "systemProp.http.proxyPort=$port"
      echo "systemProp.https.proxyHost=$host"
      echo "systemProp.https.proxyPort=$port"
      echo "systemProp.http.nonProxyHosts=localhost|127.0.0.1"
      echo "systemProp.https.nonProxyHosts=localhost|127.0.0.1"
    fi
  } > "$props"
  log "Wrote $props"
}

export_java_home() {
  export JAVA_HOME="$JDK_DIR"
  export PATH="$JDK_DIR/bin:$PATH"
  local profile="$HOME/.bashrc"
  if [ -w "$profile" ] && ! grep -q 'jdks/temurin-11' "$profile" 2>/dev/null; then
    {
      echo ""
      echo "# Java 11 toolchain for the intellij-scilla Gradle build"
      echo "export JAVA_HOME=\"$JDK_DIR\""
      echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\""
    } >> "$profile"
  fi
}

warm_caches() {
  cd "$REPO_DIR"
  chmod +x ./gradlew
  # Downloads the Gradle distribution, the IntelliJ IDEA SDK and all dependencies,
  # and produces the plugin distribution + compiled test classes.
  log "Building plugin and compiling tests (primes Gradle/IntelliJ SDK caches)"
  ./gradlew --no-daemon buildPlugin testClasses
}

# Asserts the environment can actually do what a task needs: compile the plugin
# against the IntelliJ SDK and run the project's test suite offline-warm.
healthcheck() {
  cd "$REPO_DIR"
  export JAVA_HOME="$JDK_DIR"

  # Java 11 toolchain must be usable.
  "$JDK_DIR/bin/java" -version 2>&1 | grep -q '"11' || {
    echo "[healthcheck] Temurin 11 JDK is not usable" >&2
    return 1
  }

  # The Gradle build must resolve and the IntelliJ SDK must be present.
  log "healthcheck: running ./gradlew buildPlugin test"
  ./gradlew --no-daemon buildPlugin test || {
    echo "[healthcheck] gradle buildPlugin/test failed" >&2
    return 1
  }

  # The plugin artifact must have been produced.
  ls build/distributions/*.zip >/dev/null 2>&1 || {
    echo "[healthcheck] no plugin distribution produced in build/distributions" >&2
    return 1
  }

  log "healthcheck: OK"
}

main() {
  install_jdk11
  configure_gradle
  export_java_home
  warm_caches
  healthcheck
}

main "$@"
