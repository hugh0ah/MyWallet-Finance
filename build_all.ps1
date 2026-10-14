# ==============================================================================
# Script de compilación integral para MyWalletFinance
# 1. Configuración de Android SDK y local.properties
# 2. Compilación con .\gradlew.bat assembleDebug
# 3. Verificación de salida app\build\outputs\apk\debug\app-debug.apk
# ==============================================================================

$ErrorActionPreference = "Stop"

# 1. Configurar JAVA_HOME si no está en el entorno
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $fallbackJdk = "C:\Users\alvarezherrera\.gemini\antigravity-ide\scratch\tools\jdk-21.0.12.1+1"
    if (Test-Path "$fallbackJdk\bin\java.exe") {
        $env:JAVA_HOME = $fallbackJdk
        $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
        Write-Host "[OK] JAVA_HOME configurado a: $fallbackJdk"
    } else {
        Write-Error "No se encontró instalación de JDK 21 válida."
    }
}

# 2. Comprobar / Configurar Android SDK
$sdkDir = "C:\Users\alvarezherrera\AppData\Local\Android\Sdk"
if (-not (Test-Path $sdkDir)) {
    Write-Host "[*] Android SDK no encontrado en $sdkDir. Creando directorio..."
    New-Item -ItemType Directory -Force -Path $sdkDir | Out-Null
}

$localProps = Join-Path $PSScriptRoot "local.properties"
$escapedSdk = $sdkDir.Replace("\", "\\")
Set-Content -Path $localProps -Value "sdk.dir=$escapedSdk"
Write-Host "[OK] local.properties configurado con sdk.dir=$escapedSdk"

# Verificar si el SDK tiene los componentes necesarios
$hasPlatform = Test-Path (Join-Path $sdkDir "platforms\android-35")
$hasBuildTools = Test-Path (Join-Path $sdkDir "build-tools\34.0.0")

if (-not $hasPlatform -or -not $hasBuildTools) {
    Write-Host "[*] Descargando componentes del Android SDK portable..."
    $cmdlineDir = Join-Path $sdkDir "cmdline-tools\latest"
    if (-not (Test-Path (Join-Path $cmdlineDir "bin\sdkmanager.bat"))) {
        $zipUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
        $tempZip = Join-Path $env:TEMP "cmdline-tools.zip"
        Write-Host "[*] Descargando commandlinetools..."
        curl.exe -L -o $tempZip $zipUrl
        $tempExtract = Join-Path $env:TEMP "cmdline_temp"
        Expand-Archive -Path $tempZip -DestinationPath $tempExtract -Force
        New-Item -ItemType Directory -Force -Path (Join-Path $sdkDir "cmdline-tools") | Out-Null
        Move-Item -Path (Join-Path $tempExtract "cmdline-tools") -Destination $cmdlineDir -Force
        Remove-Item -Recurse -Force $tempExtract, $tempZip -ErrorAction SilentlyContinue
    }
    
    $sdkmanager = Join-Path $cmdlineDir "bin\sdkmanager.bat"
    Write-Host "[*] Aceptando licencias..."
    "y`ny`ny`ny`ny`ny`ny`n" | & $sdkmanager --sdk_root="$sdkDir" --licenses | Out-Null
    
    Write-Host "[*] Instalando platform-tools, platforms;android-35, build-tools;34.0.0..."
    & $sdkmanager --sdk_root="$sdkDir" "platform-tools" "platforms;android-35" "build-tools;34.0.0"
}

# 3. Ejecutar compilación con Gradle hasta éxito
$apkTarget = Join-Path $PSScriptRoot "app\build\outputs\apk\debug\app-debug.apk"
$maxRetries = 3
$attempt = 0
$success = $false

while (-not $success -and $attempt -lt $maxRetries) {
    $attempt++
    Write-Host "`n[>>>] Lanzando .\gradlew.bat assembleDebug (Intento $attempt de $maxRetries)..."
    
    & .\gradlew.bat assembleDebug
    
    if (Test-Path $apkTarget) {
        $apkItem = Get-Item $apkTarget
        Write-Host "`n========================================================"
        Write-Host "[COMPLETADO CON ÉXITO] APK generado correctamente:"
        Write-Host "Ruta: $($apkItem.FullName)"
        Write-Host "Tamaño: $($apkItem.Length) bytes ($([math]::Round($apkItem.Length / 1MB, 2)) MB)"
        Write-Host "Fecha: $($apkItem.LastWriteTime)"
        Write-Host "========================================================`n"
        $success = $true
    } else {
        Write-Warning "El archivo APK no fue generado en este intento. Reintentando..."
    }
}

if (-not $success) {
    Write-Error "Fallo en la generación de $apkTarget tras $maxRetries intentos."
}
