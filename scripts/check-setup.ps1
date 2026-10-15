# Checks the one-time machine setup needed to build Murmel. Read-only: changes nothing.
# Usage: powershell -ExecutionPolicy Bypass -File scripts\check-setup.ps1

$fail = $false
function Ok($m)   { Write-Host "  [OK] $m" -ForegroundColor Green }
function Bad($m)  { Write-Host "  [X]  $m" -ForegroundColor Red; $script:fail = $true }
function Hint($m) { Write-Host "       -> $m" }
Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "Java (needed to start Gradle)"
$java = if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) { "$env:JAVA_HOME\bin\java.exe" } else { (Get-Command java -ErrorAction SilentlyContinue).Source }
if ($java) {
    $ver = (& $java -version 2>&1 | Where-Object { $_ -notmatch "^Picked up" } | Select-Object -First 1).ToString()
    if ($ver -match 'version "(\d+)') {
        $major = [int]$Matches[1]
        if ($major -lt 17) { Bad "Java 17-24 required, found: $ver" }
        elseif ($major -gt 24) { Bad "Java $major is too new for Gradle 8.14 (max 24): $ver"; Hint "point JAVA_HOME to a JDK 21, see docs\SETUP.md, step 2" }
        else { Ok $ver }
    }
    if ($env:JAVA_HOME) { Ok "JAVA_HOME=$env:JAVA_HOME" } else { Bad "JAVA_HOME is not set"; Hint "see docs\SETUP.md, step 2" }
} else { Bad "No Java found"; Hint "install Android Studio (bundles a JDK) and set JAVA_HOME, see docs\SETUP.md" }

Write-Host "Android SDK"
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { $env:ANDROID_SDK_ROOT }
if (-not $sdk -and (Test-Path local.properties)) {
    $line = Select-String -Path local.properties -Pattern '^sdk.dir=' | Select-Object -First 1
    if ($line) { $sdk = ($line.Line -replace '^sdk.dir=', '') -replace '\\\\', '\' -replace '\\:', ':' }
}
if ($sdk -and (Test-Path $sdk)) { Ok "SDK at $sdk" } else { Bad "Android SDK not found"; Hint "set ANDROID_HOME, see docs\SETUP.md, step 2" }

Write-Host "adb (talks to your phone)"
if (Get-Command adb -ErrorAction SilentlyContinue) {
    Ok ((adb version | Select-Object -First 1).ToString())
    $n = (adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }).Count
    if ($n -gt 0) { Ok "$n phone(s) connected" } else { Hint "no phone connected (fine for building, needed to install)" }
} else { Bad "adb not on PATH"; Hint "add %ANDROID_HOME%\platform-tools to PATH" }

Write-Host "Network (first build downloads Gradle + libraries)"
foreach ($h in "services.gradle.org", "dl.google.com", "repo.maven.apache.org") {
    try { Invoke-WebRequest "https://$h" -Method Head -TimeoutSec 10 -UseBasicParsing | Out-Null; Ok "$h reachable" }
    catch { if ($_.Exception.Response) { Ok "$h reachable" } else { Bad "$h not reachable"; Hint "check VPN / proxy / firewall" } }
}

Write-Host ""
if ($fail) { Write-Host "Fix the [X] items above, then run this again." } else { Write-Host "All good. Build with: .\gradlew.bat assembleDebug" }
