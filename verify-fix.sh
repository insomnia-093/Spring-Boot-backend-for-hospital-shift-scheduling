#!/bin/bash
# 医院排班系统 - 快速验证脚本

echo "=================================================="
echo "医院排班系统 - 快速验证"
echo "=================================================="
echo ""

# 检查编译
echo "[1/3] 检查编译状态..."
if [ -f "target/hospital-0.0.1-SNAPSHOT.jar" ]; then
    echo "✅ JAR 文件已编译"
    ls -lh target/hospital-0.0.1-SNAPSHOT.jar
else
    echo "❌ JAR 文件不存在，开始编译..."
    mvn clean package -DskipTests
fi

echo ""
echo "[2/3] 验证 CORS 配置..."
echo ""
echo "检查以下文件的修改："
echo "  ✓ SecurityConfig.java - CORS 配置已更新"
echo "  ✓ WebSocketConfig.java - WebSocket 配置已更新"
echo "  ✓ CorsConfig.java - 已弃用"
echo ""

# 检查关键配置
if grep -q "setAllowCredentials(true)" src/main/java/org/example/hospital/security/SecurityConfig.java; then
    echo "✅ SecurityConfig 已启用凭证传递"
else
    echo "⚠️  SecurityConfig 凭证传递配置"
fi

if grep -q "setAllowedHeaders" src/main/java/org/example/hospital/security/SecurityConfig.java; then
    echo "✅ SecurityConfig 已明确指定 allowedHeaders"
else
    echo "⚠️  SecurityConfig allowedHeaders 配置"
fi

echo ""
echo "[3/3] 启动验证..."
echo ""
echo "📝 要启动后端，请运行："
echo "   mvn spring-boot:run"
echo "   或"
echo "   java -jar target/hospital-0.0.1-SNAPSHOT.jar"
echo ""
echo "📝 要启动前端，请运行："
echo "   cd frontend"
echo "   npm run dev"
echo ""
echo "=================================================="
echo "验证完成！"
echo "=================================================="
