$ErrorActionPreference = "Stop"

$gitCmd = "C:\Users\alvarezherrera\AppData\Local\Programs\Git\cmd"
$ghDir = "C:\Program Files\GitHub CLI"

if (Test-Path $gitCmd) {
    $env:PATH = "$gitCmd;$env:PATH"
}
if (Test-Path $ghDir) {
    $env:PATH = "$ghDir;$env:PATH"
}

Write-Host "=== 1. Preparación de archivos y .gitignore ==="
$docsDir = Join-Path $PSScriptRoot "docs"
if (-not (Test-Path $docsDir)) {
    New-Item -ItemType Directory -Force -Path $docsDir | Out-Null
    Write-Host "Directorio docs creado."
}

$reportSource = Join-Path $PSScriptRoot "_apks_research\REVERSE_ENGINEERING_REPORT.md"
$reportDest = Join-Path $docsDir "REVERSE_ENGINEERING_REPORT.md"
if (Test-Path $reportSource) {
    Copy-Item -Path $reportSource -Destination $reportDest -Force
    Write-Host "[OK] Reporte copiado a $reportDest"
} else {
    Write-Host "No se encontró reporte en $reportSource"
}

$gitignorePath = Join-Path $PSScriptRoot ".gitignore"
$gitignoreContent = @"
# Ignorar investigación de APKs y binarios pesados
_apks_research/
*.apk
*.xapk
*.aab

# Gradle y Builds
.gradle/
build/
app/build/

# Configuración local de SDK y secretos
local.properties
*.keystore
*.jks

# IDEs
.idea/
*.iml
.vscode/
"@

Set-Content -Path $gitignorePath -Value $gitignoreContent
Write-Host "[OK] .gitignore actualizado."

Write-Host "`n=== 2. Preparación del APK ==="
$sourceApk = Join-Path $PSScriptRoot "app\build\outputs\apk\debug\app-debug.apk"
$cleanApk = Join-Path $PSScriptRoot "MyWalletFinance-v1.0.0.apk"
if (Test-Path $sourceApk) {
    Copy-Item -Path $sourceApk -Destination $cleanApk -Force
    Write-Host "[OK] APK copiado como: $cleanApk"
} else {
    Write-Error "No se encontró $sourceApk"
}

Write-Host "`n=== 3. Comprobación de gh auth status ==="
try {
    $authStatus = & gh auth status 2>&1
    Write-Host $authStatus
    $isLoggedIn = $LASTEXITCODE -eq 0
} catch {
    $isLoggedIn = $false
    Write-Host "Error verificando auth status: $_"
}

Write-Host "Logged in to GitHub: $isLoggedIn"
