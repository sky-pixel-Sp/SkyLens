$ErrorActionPreference = 'Stop'

$GradleVersion = '8.13'
$GradleRoot = Join-Path $env:LOCALAPPDATA "SkyLens\gradle-$GradleVersion"
$GradleHome = Join-Path $GradleRoot "gradle-$GradleVersion"
$GradleExe = Join-Path $GradleHome 'bin\gradle.bat'
$ZipPath = Join-Path $GradleRoot "gradle-$GradleVersion-bin.zip"

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw 'Java was not found. Install JDK 17 or newer, then reopen PowerShell.'
}

$SdkPath = $env:ANDROID_SDK_ROOT
if ([string]::IsNullOrWhiteSpace($SdkPath)) { $SdkPath = $env:ANDROID_HOME }
if ([string]::IsNullOrWhiteSpace($SdkPath)) {
    $SdkPath = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
}
if (-not (Test-Path $SdkPath)) {
    throw "Android SDK was not found at '$SdkPath'. Install Android SDK Platform 36 using Android Studio > SDK Manager, or set ANDROID_SDK_ROOT."
}

$env:ANDROID_HOME = $SdkPath
$env:ANDROID_SDK_ROOT = $SdkPath

if (-not (Test-Path $GradleExe)) {
    New-Item -ItemType Directory -Force -Path $GradleRoot | Out-Null
    if (-not (Test-Path $ZipPath)) {
        $Uri = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
        Write-Host "Downloading Gradle $GradleVersion from the official Gradle distribution service..."
        Invoke-WebRequest -Uri $Uri -OutFile $ZipPath
    }
    Write-Host 'Extracting Gradle...'
    Expand-Archive -LiteralPath $ZipPath -DestinationPath $GradleRoot -Force
}

if (-not (Test-Path $GradleExe)) {
    throw "Gradle executable was not found after extraction: $GradleExe"
}

Push-Location $PSScriptRoot
try {
    & $GradleExe --no-daemon assembleDebug
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed with exit code $LASTEXITCODE" }
    $Apk = Join-Path $PSScriptRoot 'app\build\outputs\apk\debug\app-debug.apk'
    if (-not (Test-Path $Apk)) { throw 'Gradle finished without producing the expected debug APK.' }
    Write-Host "Build complete: $Apk" -ForegroundColor Green
}
finally {
    Pop-Location
}
