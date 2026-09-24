#!/usr/bin/env bash
set -euo pipefail

echo "==> Running Air startup script..."

# Determine mode: warmup vs task
if [ "${AIR_STARTUP_MODE:-}" = "warmup" ]; then
    WARMUP=1
else
    WARMUP=
fi

# Ensure JDK 21 is installed
if [ ! -d "/usr/lib/jvm/java-21-openjdk-amd64" ]; then
    echo "==> Installing OpenJDK 21..."
    sudo apt-get update -y
    sudo apt-get install -y openjdk-21-jdk
fi

# Configure JAVA_HOME and PATH in shell environment
ENV_FILE="$HOME/.air_profile"
JAVA_HOME_PATH="/usr/lib/jvm/java-21-openjdk-amd64"
if [ ! -d "$JAVA_HOME_PATH" ]; then
    JAVA_HOME_PATH="$(dirname "$(dirname "$(readlink -f "$(which javac)")")")"
fi

cat <<EOF > "$ENV_FILE"
export JAVA_HOME="$JAVA_HOME_PATH"
export PATH="\$JAVA_HOME/bin:\$PATH"
EOF

# Append sourcing to login profile
PROFILE_TARGET=""
for f in "$HOME/.bash_profile" "$HOME/.bash_login" "$HOME/.profile"; do
    if [ -f "$f" ]; then
        PROFILE_TARGET="$f"
        break
    fi
done
if [ -z "$PROFILE_TARGET" ]; then
    PROFILE_TARGET="$HOME/.profile"
fi

MARKER="# Air environment configuration"
if ! grep -qF "$MARKER" "$PROFILE_TARGET" 2>/dev/null; then
    echo -e "\n$MARKER\n[ -f \"$ENV_FILE\" ] && . \"$ENV_FILE\"" >> "$PROFILE_TARGET"
fi

if [ -f "$HOME/.bashrc" ] && ! grep -qF "$MARKER" "$HOME/.bashrc" 2>/dev/null; then
    echo -e "\n$MARKER\n[ -f \"$ENV_FILE\" ] && . \"$ENV_FILE\"" >> "$HOME/.bashrc"
fi

# Source it for current script execution
# shellcheck disable=SC1090
. "$ENV_FILE"

# Ensure gradlew has executable permissions
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
fi

# Define healthcheck function
healthcheck() {
    echo "==> Running healthcheck..."
    echo "==> Checking Java version..."
    java -version
    javac -version

    echo "==> Building and testing IntelliJ Scilla plugin..."
    ./gradlew buildPlugin test --no-daemon

    echo "==> Healthcheck passed successfully!"
}

# Run warmup tasks if in warmup mode
if [ -n "${WARMUP:-}" ]; then
    echo "==> Running in WARMUP mode. Warming up build and caches..."
    healthcheck
fi

echo "==> Startup script completed successfully."
