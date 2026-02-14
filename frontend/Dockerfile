# 前端 Dockerfile - 多阶段构建

# 第一阶段：构建
FROM node:18-alpine AS builder

WORKDIR /build

# 复制 package 文件
COPY frontend/package.json frontend/package-lock.json ./

# 安装依赖
RUN npm ci

# 复制源代码
COPY frontend .

# 构建项目
RUN npm run build

# 第二阶段：运行
FROM nginx:alpine

# 复制 Nginx 配置
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# 复制构建好的文件
COPY --from=builder /build/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

# Nginx 启动
CMD ["nginx", "-g", "daemon off;"]
