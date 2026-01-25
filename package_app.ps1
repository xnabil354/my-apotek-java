$ErrorActionPreference = "Stop"
$JDK_PATH = "C:\Program Files\Java\jdk-18.0.2.1"
$JPACKAGE = "$JDK_PATH\bin\jpackage.exe"
$VERSION = "2.0"

# 1. Clean & Build (Produces Jar + Libs)
Write-Host "1. Building Project..." -ForegroundColor Cyan
.\mvnw.cmd clean package -DskipTests

# 2. Prepare Staging Directory (This is crucial for jpackage)
Write-Host "2. Preparing Staging Area..." -ForegroundColor Cyan
if (Test-Path "staging") { Remove-Item "staging" -Recurse -Force }
New-Item -ItemType Directory -Force -Path "staging" | Out-Null
New-Item -ItemType Directory -Force -Path "staging/lib" | Out-Null

# Copy Main Jar
Copy-Item "target/My-Apotek-0.0.1-SNAPSHOT.jar" -Destination "staging/"
# Copy Libraries
Copy-Item "target/lib/*.jar" -Destination "staging/lib/"

# 3. Clean Output Directory
if (Test-Path "dist_v6") { Remove-Item "dist_v6" -Recurse -Force }
New-Item -ItemType Directory -Force -Path "dist_v6" | Out-Null

# 4. Run jpackage
Write-Host "3. Packaging Application (Portable EXE)..." -ForegroundColor Cyan
Write-Host "Using jpackage at: $JPACKAGE"

& $JPACKAGE `
  --type app-image `
  --name "MyApotek" `
  --input "staging" `
  --main-jar "My-Apotek-0.0.1-SNAPSHOT.jar" `
  --main-class "com.example.My.Apotek.desktop.DesktopLauncher" `
  --dest "dist_v6" `
  --java-options "-Dfile.encoding=UTF-8" `
  --app-version $VERSION `
  --win-console

Write-Host "---------------------------------------------------" -ForegroundColor Green
Write-Host "SUCCESS!" -ForegroundColor Green
Write-Host "App Location:  dist_v6\MyApotek" 
Write-Host "Executable:    dist_v6\MyApotek\MyApotek.exe" 
Write-Host "---------------------------------------------------"
