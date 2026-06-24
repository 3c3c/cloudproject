#!/usr/bin/env bash
# =====================================================================
# 一键把 configs/ 下的配置导入 Nacos（namespace: public, group: DEFAULT_GROUP）
# 用法： bash docker/nacos/init-configs.sh
# 前置：Nacos 已启动且可访问（见 docker-compose.yml）
# =====================================================================
set -euo pipefail

NACOS_ADDR="${NACOS_ADDR:-127.0.0.1:8848}"
NACOS_USER="${NACOS_USER:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
NS="${NACOS_NS:-}"        # 空 = Nacos 的 public 命名空间
GROUP="DEFAULT_GROUP"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CONFIGS_DIR="${SCRIPT_DIR}/configs"

echo "==> 登录 Nacos (${NACOS_ADDR}) 获取 accessToken ..."
LOGIN_RESP=$(curl -s -X POST "http://${NACOS_ADDR}/nacos/v1/auth/login" \
  --data-urlencode "username=${NACOS_USER}" \
  --data-urlencode "password=${NACOS_PASSWORD}" || true)
TOKEN=$(printf '%s' "${LOGIN_RESP}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

if [ -z "${TOKEN}" ]; then
  echo "❌ 登录失败。请确认 Nacos 已启动、账号密码正确（默认 nacos/nacos）。"
  echo "   返回内容: ${LOGIN_RESP}"
  exit 1
fi
echo "    token 获取成功。"

publish() {
  local data_id="$1" file="$2"
  if [ ! -f "${file}" ]; then echo "   ⚠ 文件不存在: ${file}，跳过"; return; fi
  printf "==> 发布 %-22s ... " "${data_id}"
  local resp
  resp=$(curl -s -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "group=${GROUP}" \
    --data-urlencode "tenant=${NS}" \
    --data-urlencode "type=yaml" \
    --data-urlencode "accessToken=${TOKEN}" \
    --data-urlencode "content@${file}")
  if [ "${resp}" = "true" ]; then echo "✓"; else echo "✗ 失败: ${resp}"; fi
}

publish "cloud-common.yaml"  "${CONFIGS_DIR}/cloud-common.yaml"
publish "cloud-auth.yaml"    "${CONFIGS_DIR}/cloud-auth.yaml"
publish "cloud-product.yaml" "${CONFIGS_DIR}/cloud-product.yaml"
publish "cloud-order.yaml"   "${CONFIGS_DIR}/cloud-order.yaml"
publish "cloud-gateway.yaml" "${CONFIGS_DIR}/cloud-gateway.yaml"

echo ""
echo "✅ 完成。打开 http://${NACOS_ADDR}/nacos （${NACOS_USER}/${NACOS_PASSWORD}）"
echo "   在「配置管理 → 配置列表」可看到以上 5 个配置（命名空间：public）。"
