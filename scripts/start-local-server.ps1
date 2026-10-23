$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$dbDirectory = Join-Path $projectRoot 'server\data'
New-Item -ItemType Directory -Path $dbDirectory -Force | Out-Null

if (-not $env:DB_URL) {
    $dbPath = (Join-Path $dbDirectory 'game_db').Replace('\', '/')
    $env:DB_URL = "jdbc:h2:file:$dbPath;DB_CLOSE_DELAY=-1;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE"
}
if (-not $env:SERVER_ADDRESS) { $env:SERVER_ADDRESS = '127.0.0.1' }
if (-not $env:SERVER_PORT) { $env:SERVER_PORT = '8080' }
if (-not $env:GAME_DEMO_SEED_ENABLED) { $env:GAME_DEMO_SEED_ENABLED = 'false' }
if (-not (Get-Command mvn.cmd -ErrorAction SilentlyContinue)) {
    throw 'Chưa tìm thấy Maven. Cài Maven và thêm thư mục bin vào PATH.'
}
if (Get-NetTCPConnection -LocalPort ([int]$env:SERVER_PORT) -State Listen -ErrorAction SilentlyContinue) {
    throw "Cổng $($env:SERVER_PORT) đang được sử dụng. Hãy dừng Backend cũ hoặc chọn SERVER_PORT khác."
}

Set-Location -LiteralPath $projectRoot
Write-Host "Database của clone này: $dbDirectory"
Write-Host "Backend: http://$($env:SERVER_ADDRESS):$($env:SERVER_PORT)"
& mvn.cmd -B -DskipTests install
if ($LASTEXITCODE -ne 0) { throw "Build thất bại (Maven exit code $LASTEXITCODE)." }
& mvn.cmd -pl server spring-boot:run
if ($LASTEXITCODE -ne 0) { throw "Backend dừng với Maven exit code $LASTEXITCODE." }
