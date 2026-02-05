$ErrorActionPreference = "Stop"

$rootDir = Resolve-Path (Join-Path $PSScriptRoot "..")
$buildDir = Join-Path $rootDir "build"
$distDir = Join-Path $rootDir "dist"

if (Test-Path $buildDir) { Remove-Item $buildDir -Recurse -Force }
if (Test-Path $distDir) { Remove-Item $distDir -Recurse -Force }

New-Item -ItemType Directory -Path (Join-Path $buildDir "classes") | Out-Null
New-Item -ItemType Directory -Path $distDir | Out-Null

$javaFiles = Get-ChildItem -Path $rootDir -Recurse -Filter "*.java" |
  Where-Object { $_.FullName -notmatch "\\build\\|\\dist\\|\\.git\\" } |
  Select-Object -ExpandProperty FullName

if (-not $javaFiles) {
  Write-Error "No Java files found."
}

& javac --release 17 -d (Join-Path $buildDir "classes") $javaFiles

$manifestFile = Join-Path $buildDir "manifest.txt"
@" 
Main-Class: Main.GameManager
"@ | Set-Content -Path $manifestFile -Encoding ascii

& jar --create --file (Join-Path $distDir "BrickBreaker.jar") --manifest $manifestFile -C (Join-Path $buildDir "classes") .

Copy-Item -Path (Join-Path $rootDir "assets") -Destination (Join-Path $distDir "assets") -Recurse
if (Test-Path (Join-Path $distDir "assets\\Deprecated")) { Remove-Item (Join-Path $distDir "assets\\Deprecated") -Recurse -Force }
if (Test-Path (Join-Path $distDir "assets\\Future-updates")) { Remove-Item (Join-Path $distDir "assets\\Future-updates") -Recurse -Force }
Get-ChildItem -Path (Join-Path $distDir "assets") -Recurse -Force |
  Where-Object { $_.Name -eq ".DS_Store" -or $_.Name -eq "desktop.ini" } |
  Remove-Item -Force

@"
@echo off
setlocal
cd /d %~dp0
java -jar BrickBreaker.jar
endlocal
"@ | Set-Content -Path (Join-Path $distDir "run.bat") -Encoding ascii

@"
#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
cd "$SCRIPT_DIR"
java -jar BrickBreaker.jar
"@ | Set-Content -Path (Join-Path $distDir "run.sh") -Encoding ascii

if (Get-Command Compress-Archive -ErrorAction SilentlyContinue) {
  $zipPath = Join-Path $distDir "BrickBreaker.zip"
  if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
  Compress-Archive -Path (Join-Path $distDir "BrickBreaker.jar"), (Join-Path $distDir "assets"), (Join-Path $distDir "run.sh"), (Join-Path $distDir "run.bat") -DestinationPath $zipPath
}

Write-Output "Package created at: $distDir\BrickBreaker.zip"
