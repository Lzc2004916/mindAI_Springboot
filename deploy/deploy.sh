#!/usr/bin/env bash
# ============================================================
# 生产部署脚本（带回滚能力）
# 用法:
#   ./deploy.sh deploy     # 构建并部署
#   ./deploy.sh rollback   # 回滚到上一版本
#   ./deploy.sh status     # 查看当前状态
# ============================================================
set -euo pipefail

APP_NAME="mindai-app"
IMAGE_TAG="${1:-}"
PREVIOUS_TAG_FILE="/tmp/mindai_previous_tag"
COMPOSE_FILE="docker-compose.yml"

# ---- 颜色输出 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()  { echo -e "${GREEN}[$(date +%H:%M:%S)]${NC} $*"; }
warn() { echo -e "${YELLOW}[$(date +%H:%M:%S)] WARN:${NC} $*"; }
err()  { echo -e "${RED}[$(date +%H:%M:%S)] ERROR:${NC} $*" >&2; }

# ---- 部署 ----
deploy() {
    log "开始部署..."

    # 检查 .env
    if [ ! -f .env ]; then
        err ".env 文件不存在，请先 cp .env.production.example .env 并填入值"
        exit 1
    fi

    # 记录当前运行的镜像 tag（用于回滚）
    local current_image
    current_image=$(docker inspect "${APP_NAME}" --format='{{.Config.Image}}' 2>/dev/null || echo "")
    if [ -n "${current_image}" ]; then
        echo "${current_image}" > "${PREVIOUS_TAG_FILE}"
        log "已记录上一版本: ${current_image}"
    fi

    # 构建并启动
    log "构建镜像..."
    docker compose -f "${COMPOSE_FILE}" build app

    log "停止旧容器并启动新容器..."
    docker compose -f "${COMPOSE_FILE}" up -d --no-deps app

    # 健康检查
    log "等待服务就绪..."
    local retries=12
    for i in $(seq 1 ${retries}); do
        if curl -sf http://localhost:1236/actuator/health/readiness | grep -q '"status":"UP"'; then
            log "服务已就绪"
            log "部署完成 ✅"
            return 0
        fi
        warn "等待中... (${i}/${retries})"
        sleep 5
    done

    err "健康检查失败，自动回滚..."
    rollback
    exit 1
}

# ---- 回滚 ----
rollback() {
    if [ ! -f "${PREVIOUS_TAG_FILE}" ]; then
        err "没有找到上一版本记录，无法回滚"
        exit 1
    fi

    local previous_image
    previous_image=$(cat "${PREVIOUS_TAG_FILE}")
    warn "回滚到上一版本: ${previous_image}"

    # 修改 compose 镜像 tag 并重启
    docker stop "${APP_NAME}" && docker rm "${APP_NAME}" || true
    docker run -d --name "${APP_NAME}" \
        --restart unless-stopped \
        --env-file .env \
        -p 127.0.0.1:1236:1236 \
        -v app_files:/app/files \
        -v app_files_private:/app/files_private \
        -v app_logs:/app/logs \
        "${previous_image}"

    log "回滚完成 ✅"
}

# ---- 状态 ----
status() {
    docker compose -f "${COMPOSE_FILE}" ps
    echo "---"
    curl -s http://localhost:1236/actuator/health | head -20
}

case "${2:-${1:-status}}" in
    deploy)    deploy ;;
    rollback)  rollback ;;
    status)    status ;;
    *)
        echo "用法: $0 {deploy|rollback|status}"
        exit 1
        ;;
esac
