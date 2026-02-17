$ErrorActionPreference = "Stop"
$JDK_PATH = "C:\Program Files\Java\jdk-18.0.2.1"
$JPACKAGE = "$JDK_PATH\bin\jpackage.exe"
$VERSION = "3.0"
$PROJECT_DIR = $PWD.Path
$TEMP_BUILD = "C:\Temp\MyApotekBuild"
$DIST_DIR = "$PROJECT_DIR\dist_v7"

Write-Host "1. Building Project..." -ForegroundColor Cyan
.\mvnw.cmd clean package -DskipTests

Write-Host "2. Preparing Staging Area in temp (avoids OneDrive path issues)..." -ForegroundColor Cyan
if (Test-Path $TEMP_BUILD) { cmd /c "rmdir /s /q $TEMP_BUILD" }
New-Item -ItemType Directory -Force -Path "$TEMP_BUILD\staging" | Out-Null
New-Item -ItemType Directory -Force -Path "$TEMP_BUILD\staging\lib" | Out-Null
New-Item -ItemType Directory -Force -Path "$TEMP_BUILD\output" | Out-Null

Copy-Item "target\My-Apotek-0.0.1-SNAPSHOT.jar" -Destination "$TEMP_BUILD\staging\"
Copy-Item "target\lib\*" -Destination "$TEMP_BUILD\staging\lib\" -Recurse

Write-Host "3. Packaging Application (Portable EXE)..." -ForegroundColor Cyan
Write-Host "Using jpackage at: $JPACKAGE"

& $JPACKAGE `
  --type app-image `
  --name "MyApotek" `
  --input "$TEMP_BUILD\staging" `
  --main-jar "My-Apotek-0.0.1-SNAPSHOT.jar" `
  --main-class "com.example.My.Apotek.desktop.DesktopLauncher" `
  --dest "$TEMP_BUILD\output" `
  --java-options "-Dfile.encoding=UTF-8" `
  --app-version $VERSION `
  --win-console

Write-Host "4. Copying output to dist_v7..." -ForegroundColor Cyan
if (Test-Path $DIST_DIR) { cmd /c "rmdir /s /q $DIST_DIR" }
Start-Sleep -Seconds 1
Copy-Item "$TEMP_BUILD\output\MyApotek" -Destination $DIST_DIR -Recurse

Write-Host "5. Cleanup temp..." -ForegroundColor Cyan
cmd /c "rmdir /s /q $TEMP_BUILD"

Write-Host "---------------------------------------------------" -ForegroundColor Green
Write-Host "SUCCESS!" -ForegroundColor Green
Write-Host "App Location:  $DIST_DIR"
Write-Host "Executable:    $DIST_DIR\MyApotek.exe"
Write-Host "---------------------------------------------------"
