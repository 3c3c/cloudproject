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
    @{DataId="cloud-product.yaml"; File="cloud-product.yaml"},
    @{DataId="cloud-order.yaml"; File="cloud-order.yaml"},
    @{DataId="cloud-gateway.yaml"; File="cloud-gateway.yaml"}
)

foreach ($config in $configs) {
    $FilePath = Join-Path $ConfigsDir $config.File

    if (-not (Test-Path $FilePath)) {
        Write-Host "   WARNING: File not found: $FilePath, skipping" -ForegroundColor Yellow
        continue
    }

    Write-Host "==> Publishing $($config.DataId) ..." -ForegroundColor Cyan -NoNewline

    try {
        $Content = Get-Content $FilePath -Raw -Encoding UTF8
        $ConfigUrl = "http://" + $NacosAddr + "/nacos/v1/cs/configs"

        $body = @{
            dataId = $config.DataId
            group = $Group
            tenant = $Namespace
            type = "yaml"
            accessToken = $Token
            content = $Content
        }

        $Response = Invoke-RestMethod -Uri $ConfigUrl -Method POST -Body $body

        if ($Response -eq $true) {
            Write-Host " OK" -ForegroundColor Green
        }
        else {
            Write-Host " FAILED: $Response" -ForegroundColor Red
        }
    }
    catch {
        Write-Host " FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "DONE! Open http://${NacosAddr}/nacos ($NacosUser/$NacosPassword)" -ForegroundColor Green
Write-Host "   Check Config Management -> Config List to see the 5 configurations (namespace: public)." -ForegroundColor Green
