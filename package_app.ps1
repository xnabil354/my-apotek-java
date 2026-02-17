$ErrorActionPreference = "Stop"
$JDK_PATH = "C:\Program Files\Java\jdk-18.0.2.1"
$JPACKAGE = "$JDK_PATH\bin\jpackage.exe"
$VERSION = "3.0"
$DIST_DIR = "dist_v7"

Write-Host "1. Building Project..." -ForegroundColor Cyan
.\mvnw.cmd clean package -DskipTests

Write-Host "2. Preparing Staging Area..." -ForegroundColor Cyan
if (Test-Path "staging") { Remove-Item "staging" -Recurse -Force }
New-Item -ItemType Directory -Force -Path "staging" | Out-Null
New-Item -ItemType Directory -Force -Path "staging/lib" | Out-Null

Copy-Item "target/My-Apotek-0.0.1-SNAPSHOT.jar" -Destination "staging/"
Copy-Item "target/lib/*.jar" -Destination "staging/lib/"

if (Test-Path $DIST_DIR) { Remove-Item $DIST_DIR -Recurse -Force }
New-Item -ItemType Directory -Force -Path $DIST_DIR | Out-Null

Write-Host "3. Packaging Application (Portable EXE)..." -ForegroundColor Cyan
Write-Host "Using jpackage at: $JPACKAGE"

& $JPACKAGE `
  --type app-image `
  --name "MyApotek" `
  --input "staging" `
  --main-jar "My-Apotek-0.0.1-SNAPSHOT.jar" `
  --main-class "com.example.My.Apotek.desktop.DesktopLauncher" `
  --dest $DIST_DIR `
  --java-options "-Dfile.encoding=UTF-8" `
  --app-version $VERSION `
  --win-console

Write-Host "---------------------------------------------------" -ForegroundColor Green
Write-Host "SUCCESS!" -ForegroundColor Green
Write-Host "App Location:  $DIST_DIR\MyApotek"
Write-Host "Executable:    $DIST_DIR\MyApotek\MyApotek.exe"
Write-Host "---------------------------------------------------"
