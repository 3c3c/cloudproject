$ErrorActionPreference = "Stop"

if ($env:NACOS_ADDR) { $NacosAddr = $env:NACOS_ADDR } else { $NacosAddr = "127.0.0.1:8848" }
if ($env:NACOS_USER) { $NacosUser = $env:NACOS_USER } else { $NacosUser = "nacos" }
if ($env:NACOS_PASSWORD) { $NacosPassword = $env:NACOS_PASSWORD } else { $NacosPassword = "nacos" }
if ($env:NACOS_NS) { $Namespace = $env:NACOS_NS } else { $Namespace = "" }
$Group = "DEFAULT_GROUP"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ConfigsDir = Join-Path $ScriptDir "configs"

Write-Host "==> Login Nacos ($NacosAddr) to get accessToken ..." -ForegroundColor Cyan

$LoginUrl = "http://" + $NacosAddr + "/nacos/v1/auth/login"

try {
    $LoginBody = "username=" + $NacosUser + "&password=" + $NacosPassword
    $LoginResponse = Invoke-RestMethod -Uri $LoginUrl -Method POST -Body $LoginBody -ContentType "application/x-www-form-urlencoded"
    $Token = $LoginResponse.accessToken

    if ([string]::IsNullOrWhiteSpace($Token)) {
        Write-Host "ERROR: Login failed, cannot get accessToken" -ForegroundColor Red
        exit 1
    }

    Write-Host "    Token obtained successfully." -ForegroundColor Green
}
catch {
    Write-Host "ERROR: Login failed. Please ensure Nacos is running and credentials are correct (default: nacos/nacos)." -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$configs = @(
    @{DataId="cloud-common.yaml"; File="cloud-common.yaml"},
    @{DataId="cloud-auth.yaml"; File="cloud-auth.yaml"},
    @{DataId="cloud-admin.yaml"; File="cloud-admin.yaml"},
    @{DataId="cloud-gateway.yaml"; File="cloud-gateway.yaml"}
)

$skipCount = 0
$publishCount = 0

foreach ($config in $configs) {
    $FilePath = Join-Path $ConfigsDir $config.File

    if (-not (Test-Path $FilePath)) {
        Write-Host "   WARNING: File not found: $FilePath, skipping" -ForegroundColor Yellow
        continue
    }

    try {
        $Content = Get-Content $FilePath -Raw -Encoding UTF8

        # Check if config already exists in Nacos
        $GetConfigUrl = "http://" + $NacosAddr + "/nacos/v1/cs/configs"
        $getConfigParams = @{
            dataId = $config.DataId
            group = $Group
            tenant = $Namespace
            accessToken = $Token
        }

        $existingConfig = $null
        try {
            $existingConfig = Invoke-RestMethod -Uri $GetConfigUrl -Method GET -Body $getConfigParams
        }
        catch {
            # Config doesn't exist yet, that's okay
            $existingConfig = $null
        }

        # Skip if config exists and content is same
        if ($existingConfig -and $existingConfig.Trim() -eq $Content.Trim()) {
            Write-Host "==> $($config.DataId) ... " -ForegroundColor Cyan -NoNewline
            Write-Host "SKIP (local and remote are the same)" -ForegroundColor Gray
            $skipCount++
        }
        else {
            # Pull config from Nacos to update local file
            if ($existingConfig) {
                Write-Host "==> $($config.DataId) ... " -ForegroundColor Cyan -NoNewline
                Write-Host "SYNC (remote differs from local, pulling from Nacos)" -ForegroundColor Yellow

                # Update local file with config from Nacos
                $existingConfig | Out-File -FilePath $FilePath -Encoding UTF8 -NoNewline

                Write-Host "      Local file updated: $FilePath" -ForegroundColor Green
                $publishCount++
            }
            else {
                Write-Host "==> $($config.DataId) ... " -ForegroundColor Cyan -NoNewline
                Write-Host "WARNING (config not found in Nacos, skipping)" -ForegroundColor Yellow
            }
        }
    }
    catch {
        Write-Host "==> $($config.DataId) ... " -ForegroundColor Cyan -NoNewline
        Write-Host " FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "DONE! Open http://${NacosAddr}/nacos ($NacosUser/$NacosPassword)" -ForegroundColor Green
Write-Host "   Published: $publishCount, Skipped: $skipCount (total 4 configurations)" -ForegroundColor Green