param(
    [switch]$Run,
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvn) {
    Write-Host "找不到 mvn，请先安装 Maven 或把它加到 PATH。" -ForegroundColor Red
    exit 1
}

$mvnArgs = @('-B', 'clean', 'package')
if ($SkipTests) { $mvnArgs += '-DskipTests' }

Write-Host ">>> mvn $($mvnArgs -join ' ')" -ForegroundColor Cyan
& mvn @mvnArgs
if ($LASTEXITCODE -ne 0) {
    Write-Host "构建失败。" -ForegroundColor Red
    exit $LASTEXITCODE
}

$jar = Join-Path $PSScriptRoot 'target/plane-war-2.0.0.jar'
Write-Host ">>> 构建完成: $jar" -ForegroundColor Green

if ($Run) {
    & java -jar $jar
}
