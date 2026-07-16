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

# 本地 configs/ 下需发布到 Nacos 的配置清单
$configs = @(
    @{DataId="cloud-common.yaml"; File="cloud-common.yaml"},
    @{DataId="cloud-auth.yaml"; File="cloud-auth.yaml"},
    @{DataId="cloud-admin.yaml"; File="cloud-admin.yaml"},
    @{DataId="cloud-gateway.yaml"; File="cloud-gateway.yaml"},
    @{DataId="cloud-message.yaml"; File="cloud-message.yaml"}
)

$publishUrl = "http://" + $NacosAddr + "/nacos/v1/cs/configs"
$createCount = 0      # 新建数
$updateCount = 0      # 更新数
$skipCount = 0        # 跳过数
$failCount = 0        # 失败数

foreach ($config in $configs) {
    $FilePath = Join-Path $ConfigsDir $config.File

    if (-not (Test-Path $FilePath)) {
        Write-Host "   WARNING: Local file not found: $FilePath, skipping" -ForegroundColor Yellow
        continue
    }

    $DataId = $config.DataId

    try {
        $Content = Get-Content $FilePath -Raw -Encoding UTF8
        # 去掉 UTF-8 BOM（若有），否则与 Nacos 返回内容比较时永远不一致，反复触发无谓的更新
        $Bom = [char]0xFEFF
        $Content = $Content -replace "^$Bom", ""

        # 查询 Nacos 是否已存在该配置
        $GetConfigUrl = "http://" + $NacosAddr + "/nacos/v1/cs/configs"
        $getConfigParams = @{
            dataId = $DataId
            group = $Group
            tenant = $Namespace
            accessToken = $Token
        }

        $existingConfig = $null
        try {
            $existingConfig = Invoke-RestMethod -Uri $GetConfigUrl -Method GET -Body $getConfigParams
        }
        catch {
            # 配置不存在（Nacos 返回 404），正常情况，按新建处理
            $existingConfig = $null
        }

        $exists = ($null -ne $existingConfig -and -not [string]::IsNullOrWhiteSpace($existingConfig))
        $same = ($exists -and $existingConfig.Trim() -eq $Content.Trim())

        if ($same) {
            # 本地与远端一致，无需操作
            Write-Host "==> $DataId ... " -ForegroundColor Cyan -NoNewline
            Write-Host "SKIP (local and remote are the same)" -ForegroundColor Gray
            $skipCount++
            continue
        }

        # 发布配置到 Nacos（新建或更新走同一个 POST /nacos/v1/cs/configs 接口）
        # 用 application/x-www-form-urlencoded 提交，content 经 EscapeDataString 编码以处理特殊字符（=、&、换行）
        # 鉴权：accessToken 与 username 都放 body。前提是配置的 createBy/owner 为当前用户（首次发布即写入）；
        #       若配置是无主历史数据（createBy 为空），update 会报 "user not found!"(403)，需先删除重建。
        $formFields = @(
            "dataId=" + [System.Uri]::EscapeDataString($DataId),
            "group="   + [System.Uri]::EscapeDataString($Group),
            "type=yaml",
            "accessToken=" + [System.Uri]::EscapeDataString($Token),
            "username="    + [System.Uri]::EscapeDataString($NacosUser),
            "content=" + [System.Uri]::EscapeDataString($Content)
        )
        if (-not [string]::IsNullOrEmpty($Namespace)) {
            $formFields = @("tenant=" + [System.Uri]::EscapeDataString($Namespace)) + $formFields
        }
        $Body = [string]::Join("&", $formFields)

        $publishResponse = Invoke-RestMethod -Uri $publishUrl -Method POST -Body $Body `
            -ContentType "application/x-www-form-urlencoded; charset=UTF-8"

        if ($publishResponse -eq $true) {
            if ($exists) {
                Write-Host "==> $DataId ... " -ForegroundColor Cyan -NoNewline
                Write-Host "UPDATE (remote differs from local, published local to Nacos)" -ForegroundColor Yellow
                $updateCount++
            }
            else {
                Write-Host "==> $DataId ... " -ForegroundColor Cyan -NoNewline
                Write-Host "CREATE (new config published to Nacos)" -ForegroundColor Green
                $createCount++
            }
        }
        else {
            Write-Host "==> $DataId ... " -ForegroundColor Cyan -NoNewline
            Write-Host " FAILED (Nacos rejected the publish: $publishResponse)" -ForegroundColor Red
            $failCount++
        }
    }
    catch {
        Write-Host "==> $DataId ... " -ForegroundColor Cyan -NoNewline
        Write-Host " FAILED: $($_.Exception.Message)" -ForegroundColor Red
        $failCount++
    }
}

Write-Host ""
Write-Host "DONE! Open http://${NacosAddr}/nacos ($NacosUser/$NacosPassword)" -ForegroundColor Green
Write-Host "   Created: $createCount, Updated: $updateCount, Skipped: $skipCount, Failed: $failCount (total $($configs.Count) configurations)" -ForegroundColor Green
