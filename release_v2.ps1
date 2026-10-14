$ErrorActionPreference = "Stop"

$gitCmd = "C:\Users\alvarezherrera\AppData\Local\Programs\Git\cmd"
$ghDir = "C:\Program Files\GitHub CLI"

if (Test-Path $gitCmd) {
    $env:PATH = "$gitCmd;$env:PATH"
}
if (Test-Path $ghDir) {
    $env:PATH = "$ghDir;$env:PATH"
}

# 1. Copiar el APK con el nombre oficial v2.0.0
$apkSource = Join-Path $PSScriptRoot "app\build\outputs\apk\debug\app-debug.apk"
$apkV2 = Join-Path $PSScriptRoot "MyWalletFinance-v2.0.0.apk"

if (-not (Test-Path $apkSource)) {
    Write-Error "No se encontró el APK en $apkSource"
}

Copy-Item -Path $apkSource -Destination $apkV2 -Force
$apkItem = Get-Item $apkV2
Write-Host "[OK] APK preparado en: $($apkItem.FullName) ($([math]::Round($apkItem.Length / 1MB, 2)) MB)"

# 2. Git add y commit
Write-Host "[*] Registrando cambios en Git..."
& git status --short
& git add .
& git commit -m "feat: v2.0.0 full CRUD for real balances, accounts, transactions, budgets and categories"

# 3. Git push
Write-Host "[*] Subiendo cambios a GitHub origin/main..."
& git push origin main

# 4. Publicar la Release v2.0.0 en GitHub
Write-Host "[*] Publicando GitHub Release v2.0.0..."
$title = "MyWallet Finance v2.0.0 - Gestión Total de Saldos y Cuentas"
$notes = "Añadido ajuste de saldo real en un toque, creación/edición/borrado de cuentas, edición y borrado de transacciones, creación de presupuestos y categorías personalizadas, y botón para limpiar datos de prueba."
$asset = "$apkV2#MyWalletFinance-v2.0.0.apk"

& gh release create v2.0.0 $asset --title "$title" --notes "$notes"

Write-Host ""
Write-Host "========================================================"
Write-Host "[RELEASE v2.0.0 PUBLICADA CON ÉXITO]"
Write-Host "Repositorio: https://github.com/hugh0ah/MyWallet-Finance"
Write-Host "Página Release: https://github.com/hugh0ah/MyWallet-Finance/releases/tag/v2.0.0"
Write-Host "Descarga directa APK: https://github.com/hugh0ah/MyWallet-Finance/releases/download/v2.0.0/MyWalletFinance-v2.0.0.apk"
Write-Host "========================================================"
