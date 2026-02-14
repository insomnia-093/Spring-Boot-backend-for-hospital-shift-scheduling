#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Coze 配置快速验证脚本
检查 Coze API 密钥、Workflow ID 和连接状态
"""

import os
from pathlib import Path
from dotenv import load_dotenv

# 加载环境变量
project_root = Path(__file__).parent
env_file = project_root / '.env'
if env_file.exists():
    load_dotenv(dotenv_path=str(env_file))
else:
    load_dotenv()

print("\n" + "="*60)
print("🔧 Coze 配置验证工具")
print("="*60 + "\n")

# 检查环境变量
api_key = os.getenv('COZE_API_KEY', '')
workflow_id = os.getenv('COZE_WORKFLOW_ID') or os.getenv('workflow_id', '')
api_url = os.getenv('COZE_API_URL', 'http://localhost:8000')
enabled = os.getenv('COZE_ENABLED', 'true').lower() == 'true'

print("📋 当前配置：\n")
print(f"  API Key:        {'✅ 已设置 (pat_...)' if api_key and api_key.startswith('pat_') else '❌ 未设置或格式错误'}")
if api_key:
    print(f"                  {api_key[:20]}...{api_key[-10:] if len(api_key) > 30 else ''}")
print(f"  Workflow ID:    {'✅ 已设置' if workflow_id else '❌ 未设置'} ({workflow_id})")
print(f"  API URL:        {api_url}")
print(f"  Coze 启用:      {'✅ 是' if enabled else '⚠️  否（使用演示模式）'}")

print("\n" + "-"*60)

# 验证配置
all_ok = True
errors = []

if not api_key:
    errors.append("❌ COZE_API_KEY 未设置")
    all_ok = False
elif not api_key.startswith('pat_'):
    errors.append("⚠️  COZE_API_KEY 应以 'pat_' 开头")
else:
    print("✅ API Key 格式正确")

if not workflow_id:
    errors.append("❌ COZE_WORKFLOW_ID 未设置")
    all_ok = False
else:
    print("✅ Workflow ID 已设置")

print("-"*60)

if errors:
    print("\n⚠️  发现问题：\n")
    for error in errors:
        print(f"  {error}")
    print("\n📝 解决方案：\n")
    print("  1. 打开 .env 文件")
    print("  2. 设置 COZE_API_KEY=pat_your_token_here")
    print("  3. 设置 COZE_WORKFLOW_ID=your_workflow_id")
    print("  4. 保存并重启后端服务")
    print("\n获取凭证：")
    print("  - 访问 https://coze.cn")
    print("  - API Key 位置：个人中心 -> API")
    print("  - Workflow ID：在工作流设置页面")
else:
    print("\n✅ Coze 配置完整！\n")
    print("后续步骤：")
    print("  1. 启动 Python Coze API 服务器:")
    print("     python coze_api_server.py")
    print("  2. 启动后端服务:")
    print("     mvn spring-boot:run")
    print("  3. 启动前端:")
    print("     cd frontend && npm run dev")
    print("  4. 在聊天界面测试发送消息")

print("\n" + "="*60 + "\n")

# 尝试连接测试（如果配置完整）
if all_ok and api_key and workflow_id:
    print("🧪 尝试初始化 Coze 客户端...\n")
    try:
        from cozepy import COZE_CN_BASE_URL, Coze, TokenAuth
        coze = Coze(auth=TokenAuth(token=api_key), base_url=COZE_CN_BASE_URL)
        print("✅ Coze 客户端初始化成功！")
        print("\n可以进行以下测试：")
        print("  1. 启动 coze_api_server.py")
        print("  2. 访问 http://localhost:8000/health")
        print("  3. 在医院排班系统中测试聊天功能")
    except Exception as e:
        print(f"❌ 客户端初始化失败: {e}")
        print("\n可能原因：")
        print("  1. API Key 错误或已过期")
        print("  2. 网络连接问题")
        print("  3. cozepy 库未安装 (pip install cozepy)")

print()
