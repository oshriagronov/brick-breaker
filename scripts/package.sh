#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
BUILD_DIR="$ROOT_DIR/build"
DIST_DIR="$ROOT_DIR/dist"

rm -rf "$BUILD_DIR" "$DIST_DIR"
mkdir -p "$BUILD_DIR/classes" "$DIST_DIR"

# Compile Java sources
JAVA_FILES=$(find "$ROOT_DIR" -name "*.java" -not -path "*/build/*" -not -path "*/dist/*" -not -path "*/.git/*")

if [[ -z "$JAVA_FILES" ]]; then
  echo "No Java files found."
  exit 1
fi

javac --release 17 -d "$BUILD_DIR/classes" $JAVA_FILES

# Create runnable JAR
MANIFEST_FILE="$BUILD_DIR/manifest.txt"
cat > "$MANIFEST_FILE" <<'MANIFEST'
Main-Class: Main.GameManager
MANIFEST

jar --create --file "$DIST_DIR/BrickBreaker.jar" --manifest "$MANIFEST_FILE" -C "$BUILD_DIR/classes" .

# Copy runtime assets next to the JAR
cp -R "$ROOT_DIR/assets" "$DIST_DIR/assets"
rm -rf "$DIST_DIR/assets/Deprecated" "$DIST_DIR/assets/Future-updates"
find "$DIST_DIR/assets" \( -name ".DS_Store" -o -name "desktop.ini" \) -delete

# Create launch scripts
cat > "$DIST_DIR/run.sh" <<'RUNSH'
#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR"
java -jar BrickBreaker.jar
RUNSH
chmod +x "$DIST_DIR/run.sh"

cat > "$DIST_DIR/run.bat" <<'RUNBAT'
@echo off
setlocal
cd /d %~dp0
java -jar BrickBreaker.jar
endlocal
RUNBAT

# Create a zip for distribution
( cd "$DIST_DIR" && zip -r "BrickBreaker.zip" "BrickBreaker.jar" "assets" "run.sh" "run.bat" )

echo "Package created at: $DIST_DIR/BrickBreaker.zip"
