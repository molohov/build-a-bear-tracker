param(
    [string]$AvdName = "Pixel_9_API_35"
)

$SdkRoot = $env:ANDROID_HOME
if (-not $SdkRoot) {
    $SdkRoot = Join-Path $env:LOCALAPPDATA "Android\Sdk"
}

$adb = Join-Path $SdkRoot "platform-tools\adb.exe"
$emulator = Join-Path $SdkRoot "emulator\emulator.exe"

if (-not (Test-Path $adb)) {
    Write-Error "adb not found at $adb"
    exit 1
}

if (-not (Test-Path $emulator)) {
    Write-Error "emulator not found at $emulator"
    exit 1
}

$connected = & $adb devices | Select-String "\tdevice$"
if ($connected) {
    Write-Host "Device already connected."
} else {
    Write-Host "Starting emulator: $AvdName"
    # Forward host keyboard input into the emulator (AVD has hw.keyboard disabled by default).
    Start-Process -FilePath $emulator -ArgumentList "-avd", $AvdName, "-use-keycode-forwarding"
}

Write-Host "Waiting for device..."
& $adb wait-for-device | Out-Null

$timeout = 180
$elapsed = 0
while ($elapsed -lt $timeout) {
    $boot = (& $adb shell getprop sys.boot_completed 2>$null).Trim()
    if ($boot -eq "1") {
        Write-Host "Emulator ready."
        exit 0
    }
    Start-Sleep -Seconds 3
    $elapsed += 3
    Write-Host "Booting... ($elapsed s)"
}

Write-Error "Timed out waiting for emulator boot after ${timeout}s"
exit 1