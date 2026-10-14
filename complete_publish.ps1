param (
    [string]$GitHubToken = ""
)

$ErrorActionPreference = "Stop"

$gitCmd = "C:\Users\alvarezherrera\AppData\Local\Programs\Git\cmd"
$ghDir = "C:\Program Files\GitHub CLI"

if (Test-Path $gitCmd) {
    $env:PATH = "$gitCmd;$env:PATH"
}
if (Test-Path $ghDir) {
    $env:PATH = "$ghDir;$env:PATH"
}

# Si se proporciona un token directamente
if ($GitHubToken) {
    Write-Host "[*] Autenticando gh con token..."
    $GitHubToken | & gh auth login --with-token
}

# Verificar autenticación
Write-Host "[*] Verificando estado de autenticación en GitHub..."
& gh auth status
if ($LASTEXITCODE -ne 0) {
    Write-Error "GitHub CLI no está autenticado. Por favor autoriza el acceso primero."
}

# Obtener nombre de usuario de GitHub
$ghUser = (& gh api user --jq .login).Trim()
$ghEmail = (& gh api user --jq .email).Trim()
if (-not $ghEmail -or $ghEmail -eq "null") {
    $ghEmail = "$ghUser@users.noreply.github.com"
}
Write-Host "[OK] Conectado como: $ghUser ($ghEmail)"

# 1. Preparar .gitignore y docs
Write-Host "`n[*] Preparando .gitignore y reporte técnico..."
$docsDir = Join-Path $PSScriptRoot "docs"
if (-not (Test-Path $docsDir)) {
    New-Item -ItemType Directory -Force -Path $docsDir | Out-Null
}

$reportSrc = Join-Path $PSScriptRoot "_apks_research\REVERSE_ENGINEERING_REPORT.md"
$reportDst = Join-Path $docsDir "REVERSE_ENGINEERING_REPORT.md"
if (Test-Path $reportSrc) {
    Copy-Item -Path $reportSrc -Destination $reportDst -Force
    Write-Host "[OK] Reporte copiado a $reportDst"
}

$gitignorePath = Join-Path $PSScriptRoot ".gitignore"
$gitignoreContent = @"
# Binarios e investigación de ingeniería inversa
_apks_research/
*.apk
*.xapk
*.aab

# Gradle y compilación
.gradle/
build/
app/build/

# Secretos y configuración local
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

# 2. Renombrar / Copiar APK
$apkSource = Join-Path $PSScriptRoot "app\build\outputs\apk\debug\app-debug.apk"
$cleanApk = Join-Path $PSScriptRoot "MyWalletFinance-v1.0.0.apk"
if (-not (Test-Path $apkSource)) {
    Write-Error "No se encontró el APK en $apkSource. Compila el proyecto primero."
}
Copy-Item -Path $apkSource -Destination $cleanApk -Force
Write-Host "[OK] APK preparado en: $cleanApk"

# 3. Inicializar Git y configurar credenciales locales
Write-Host "`n[*] Configurando Git local..."
& git config --global user.name "$ghUser"
& git config --global user.email "$ghEmail"

if (-not (Test-Path (Join-Path $PSScriptRoot ".git"))) {
    & git init
    Write-Host "[OK] Repositorio Git inicializado."
}

& git branch -M main

# Asegurarse de que no haya restos en staging
& git add .
& git commit -m "feat: initial release of MyWallet Finance v1.0.0"

# 4. Crear Repositorio GitHub y hacer push
Write-Host "`n[*] Creando repositorio en GitHub..."
$repoName = "MyWallet-Finance"

# Comprobar si ya existe
$repoExists = $false
try {
    & gh repo view "$ghUser/$repoName" 2>$null
    if ($LASTEXITCODE -eq 0) { $repoExists = $true }
} catch {
    $repoExists = $false
}

if (-not $repoExists) {
    & gh repo create $repoName --public --source=. --remote=origin --push
    Write-Host "[OK] Repositorio público creado y código subido a origin/main."
} else {
    Write-Host "[*] El repositorio ya existe. Asegurando remote y haciendo push..."
    try { & git remote add origin "https://github.com/$ghUser/$repoName.git" 2>$null } catch {}
    & git push -u origin main --force
}

# 5. Crear Release en GitHub
Write-Host "`n[*] Creando GitHub Release oficial v1.0.0..."
$notes = "Versión inicial completa con motor contable estilo Wallet, teclado rápido estilo Cashew y lectura automática de notificaciones de pago."
$assetArg = "$cleanApk#MyWalletFinance-v1.0.0.apk"

& gh release create v1.0.0 $assetArg --title "MyWallet Finance v1.0.0" --notes "$notes"

# URLs finales
$repoUrl = "https://github.com/$ghUser/$repoName"
$releaseUrl = "https://github.com/$ghUser/$repoName/releases/tag/v1.0.0"
$downloadUrl = "https://github.com/$ghUser/$repoName/releases/download/v1.0.0/MyWalletFinance-v1.0.0.apk"

Write-Host ""
Write-Host "========================================================"
Write-Host "[PUBLICACION COMPLETADA CON EXITO]"
Write-Host "Repositorio GitHub: $repoUrl"
Write-Host "Pagina de Release:  $releaseUrl"
Write-Host "Descarga directa:   $downloadUrl"
Write-Host "========================================================"
