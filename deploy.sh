#!/bin/bash

# Docker 部署启动脚本

set -e

echo "================================"
echo "医院排班系统 - Docker 部署"
echo "================================"
echo ""

# 检查 Docker
echo "✓ 检查 Docker..."
if ! command -v docker &> /dev/null; then
    echo "✗ Docker 未安装，请先安装 Docker"
    exit 1
fi
echo "  Docker 版本: $(docker --version)"

# 检查 Docker Compose
echo "✓ 检查 Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    echo "✗ Docker Compose 未安装，请先安装"
    exit 1
fi
echo "  Docker Compose 版本: $(docker-compose --version)"

# 检查 .env 文件
echo "✓ 检查环境配置..."
if [ ! -f ".env" ]; then
    echo "✗ .env 文件不存在"
    exit 1
fi
echo "  .env 文件已找到"

# 检查端口
echo "✓ 检查端口..."
PORTS=(9090 80 5432)
for port in "${PORTS[@]}"; do
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo "  ⚠ 警告: 端口 $port 已被占用"
    fi
done

# 启动服务
echo ""
echo "🚀 启动服务..."
docker-compose up -d

# 等待服务启动
echo ""
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo ""
echo "📊 服务状态检查..."
docker-compose ps

# 检查后端健康
echo ""
echo "🔍 检查后端健康状态..."
for i in {1..10}; do
    if curl -s http://localhost:9090/api/health > /dev/null 2>&1; then
        echo "✓ 后端服务已就绪"
        break
    else
        echo "  等待后端启动... ($i/10)"
        sleep 3
    fi
done

# 检查前端
echo "🔍 检查前端..."
for i in {1..10}; do
    if curl -s http://localhost/ > /dev/null 2>&1; then
        echo "✓ 前端服务已就绪"
        break
    else
        echo "  等待前端启动... ($i/10)"
        sleep 3
    fi
done

# 检查数据库
echo "🔍 检查数据库..."
if docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
    echo "✓ 数据库服务已就绪"
else
    echo "⚠ 数据库可能未完全启动"
fi

echo ""
echo "================================"
echo "✅ 部署完成！"
echo "================================"
echo ""
echo "📍 访问地址:"
echo "  前端:       http://localhost"
echo "  后端 API:   http://localhost:9090/api"
echo "  健康检查:   http://localhost:9090/api/health"
echo "  监控指标:   http://localhost:9090/actuator/prometheus"
echo ""
echo "📝 常用命令:"
echo "  查看日志:   docker-compose logs -f backend"
echo "  停止服务:   docker-compose down"
echo "  重启服务:   docker-compose restart"
echo ""
echo "📚 更多信息请查看 DOCKER_DEPLOYMENT.md"
echo ""
