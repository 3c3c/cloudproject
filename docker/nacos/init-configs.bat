@echo off
REM =====================================================================
REM 一键把 configs/ 下的配置导入 Nacos（namespace: public, group: DEFAULT_GROUP）
REM 用法： docker\nacos\init-configs.bat
REM 前置：Nacos 已启动且可访问（见 docker-compose.yml）
REM =====================================================================

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0init-configs.ps1"
