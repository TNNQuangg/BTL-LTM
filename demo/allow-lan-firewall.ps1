$ErrorActionPreference = 'Stop'
$ruleName = 'Ve Hinh Doan Y LAN TCP 8080'
if (-not (Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue)) {
    New-NetFirewallRule -DisplayName $ruleName -Direction Inbound -Action Allow `
        -Protocol TCP -LocalPort 8080 -Profile Any -RemoteAddress LocalSubnet | Out-Null
}
Write-Host "Da cho phep TCP 8080 chi tu cac may trong mang noi bo."
