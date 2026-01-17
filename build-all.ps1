$modVersion = Read-Host "Mod version (e.g., 1.0.1)"
if ([string]::IsNullOrWhiteSpace($modVersion)) {
    Write-Host "No version provided. Aborting."
    exit 1
}

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$versionDirs = Get-ChildItem -Directory -Path $root | Where-Object { $_.Name -match '^\d+\.\d+\.\d+$' } | Sort-Object Name

if (-not $versionDirs) {
    Write-Host "No version folders found under $root"
    exit 1
}

$outDir = Join-Path $root ("builds\\" + $modVersion)
if (Test-Path $outDir) {
    Remove-Item -Recurse -Force $outDir
}
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

foreach ($dir in $versionDirs) {
    $gradleProps = Join-Path $dir.FullName "gradle.properties"
    if (Test-Path $gradleProps) {
        $lines = Get-Content -Path $gradleProps
        $lines = $lines | ForEach-Object {
            if ($_ -match '^mod_version=') { "mod_version=$modVersion" } else { $_ }
        }
        Set-Content -Path $gradleProps -Value $lines -Encoding ASCII
    }

    $libDirs = @(
        (Join-Path $dir.FullName "fabric\\build\\libs"),
        (Join-Path $dir.FullName "neoforge\\build\\libs"),
        (Join-Path $dir.FullName "forge\\build\\libs")
    )
    foreach ($libDir in $libDirs) {
        if (Test-Path $libDir) {
            Get-ChildItem -Path $libDir -Filter "*.jar" -ErrorAction SilentlyContinue |
                Remove-Item -Force -ErrorAction SilentlyContinue
        }
    }

    Push-Location $dir.FullName
    Write-Host "Building $($dir.Name)..."
    .\gradlew.bat clean build
    if ($LASTEXITCODE -ne 0) {
        Pop-Location
        Write-Host "Build failed for $($dir.Name)."
        exit 1
    }
    Pop-Location

    foreach ($libDir in $libDirs) {
        if (Test-Path $libDir) {
            Get-ChildItem -Path $libDir -Filter "*.jar" |
                Where-Object { $_.Name -notmatch 'sources|dev-shadow' } |
                Copy-Item -Destination $outDir -Force
        }
    }
}

Write-Host "Done. Output in $outDir"
