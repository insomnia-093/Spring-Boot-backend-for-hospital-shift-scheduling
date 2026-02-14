#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Coze AI 工作流 HTTP API 服务器
提供 RESTful 接口供后端调用 Coze 工作流
"""

import os
import json
import logging
from pathlib import Path
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs
from dotenv import load_dotenv
from cozepy import COZE_CN_BASE_URL, Coze, TokenAuth

# 加载环境变量
project_root = Path(__file__).parent
env_file = project_root / '.env'
if env_file.exists():
    load_dotenv(dotenv_path=str(env_file))
else:
    load_dotenv()

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# 初始化 Coze 客户端
COZE_API_TOKEN = os.getenv('COZE_API_KEY', '')
WORKFLOW_ID = os.getenv('COZE_WORKFLOW_ID') or os.getenv('workflow_id', '')
COZE_ENABLED = os.getenv('COZE_ENABLED', 'true').lower() == 'true'

logger.info(f"Coze 配置: API_TOKEN={'***' if COZE_API_TOKEN else '未设置'}, WORKFLOW_ID={WORKFLOW_ID or '未设置'}, ENABLED={COZE_ENABLED}")

if not COZE_API_TOKEN or not WORKFLOW_ID:
    logger.warning(f"未完整配置 Coze: TOKEN={bool(COZE_API_TOKEN)}, WORKFLOW_ID={bool(WORKFLOW_ID)}")
    if COZE_ENABLED:
        logger.warning("Coze 启用但配置不完整，将使用演示模式")
        COZE_ENABLED = False
    COZE_API_TOKEN = None
    WORKFLOW_ID = None

if COZE_ENABLED and COZE_API_TOKEN and WORKFLOW_ID:
    try:
        coze_client = Coze(auth=TokenAuth(token=COZE_API_TOKEN), base_url=COZE_CN_BASE_URL)
        logger.info("✅ Coze 客户端初始化成功，生产模式")
    except Exception as e:
        logger.error(f"❌ Coze 客户端初始化失败: {e}")
        coze_client = None
        COZE_ENABLED = False
else:
    coze_client = None
    logger.info("⚠️  演示模式启用（Coze 未配置）")


class CozeHTTPHandler(BaseHTTPRequestHandler):
    """处理 HTTP 请求的处理器"""

    def log_message(self, format, *args):
        """重写日志输出"""
        logger.info("%s - %s" % (self.client_address[0], format % args))

    def do_GET(self):
        """处理 GET 请求"""
        parsed_path = urlparse(self.path)
        path = parsed_path.path

        if path == '/health':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            response = {
                'status': 'ok',
                'service': 'coze-workflow-api',
                'mode': 'demo' if coze_client is None else 'production'
            }
            self.wfile.write(json.dumps(response).encode())
        else:
            self.send_response(404)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps({'error': '未找到该端点'}).encode())

    def do_POST(self):
        """处理 POST 请求"""
        parsed_path = urlparse(self.path)
        path = parsed_path.path

        if path == '/api/coze/chat':
            self.handle_coze_chat()
        else:
            self.send_response(404)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps({'error': '未找到该端点'}).encode())

    def handle_coze_chat(self):
        """处理 Coze 聊天请求"""
        try:
            # 读取请求体
            content_length = int(self.headers.get('Content-Length', 0))
            request_body = self.rfile.read(content_length).decode('utf-8')
            request_data = json.loads(request_body)

            user_input = request_data.get('input', '').strip()

            if not user_input:
                self.send_response(400)
                self.send_header('Content-type', 'application/json')
                self.end_headers()
                self.wfile.write(json.dumps({'error': '输入不能为空'}).encode())
                return

            logger.info(f"收到聊天请求: {user_input[:100]}")

            # 调用 Coze 或演示模式
            if coze_client and WORKFLOW_ID:
                response_text = self.call_coze_workflow(user_input)
            else:
                response_text = self.generate_demo_response(user_input)

            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            response = {
                'response': response_text,
                'status': 'success'
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))

        except Exception as e:
            logger.error(f"处理请求时出错: {e}")
            self.send_response(500)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            response = {
                'error': f'服务器错误: {str(e)}',
                'status': 'failed'
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode('utf-8'))

    def call_coze_workflow(self, user_input):
        """调用 Coze 工作流"""
        try:
            if not coze_client or not WORKFLOW_ID:
                raise Exception("Coze 客户端未初始化或 workflow_id 未配置")

            parameters = {'input': user_input}
            logger.info(f"调用 Coze 工作流: {WORKFLOW_ID}，参数: {parameters}")

            workflow = coze_client.workflows.runs.create(
                workflow_id=WORKFLOW_ID,
                parameters=parameters,
            )

            logger.info(f"Coze 工作流响应对象: {workflow}")

            # 提取响应 - 尝试多种方式
            response = None

            # 方式 1：data 属性中的 response 字段
            if hasattr(workflow, 'data') and isinstance(workflow.data, dict):
                response = workflow.data.get('response')
                if response:
                    logger.info(f"从 data.response 提取: {response[:100]}")
                    return response

            # 方式 2：output 字段
            if hasattr(workflow, 'output'):
                response = workflow.output
                if response:
                    logger.info(f"从 output 提取: {response[:100]}")
                    return response

            # 方式 3：直接转字符串
            if hasattr(workflow, '__dict__'):
                logger.info(f"Workflow 属性: {workflow.__dict__}")
                # 查找包含内容的字段
                for key, value in workflow.__dict__.items():
                    if value and isinstance(value, (str, dict)):
                        response = str(value)
                        if len(response) > 10:  # 有意义的响应
                            logger.info(f"从 {key} 提取: {response[:100]}")
                            return response

            # 方式 4：完整对象转字符串
            response = str(workflow)
            logger.info(f"Coze 返回响应: {response[:100]}")
            return response

        except Exception as e:
            logger.error(f"调用 Coze 工作流失败: {e}", exc_info=True)
            raise

    def generate_demo_response(self, user_input):
        """生成演示响应"""
        # 根据关键词返回相应的演示回复
        input_lower = user_input.lower()

        if '生成' in user_input or 'generate' in input_lower:
            return ("📋 已接收排班生成请求。我将为下周生成最优排班方案：\n\n"
                   "✓ 夜班人数均匀分配（每晚 3-4 人）\n"
                   "✓ 资深医生轮休安排\n"
                   "✓ 新入职员工避免连续夜班\n\n"
                   "预计 1-2 分钟内完成排班计算。")

        elif '校验' in user_input or 'validate' in input_lower:
            return ("🔍 开始校验当前排班冲突...\n\n"
                   "✅ 检查结果：\n"
                   "  • 总班次: 42\n"
                   "  • 冲突班次: 0\n"
                   "  • 覆盖率: 100%\n\n"
                   "✓ 排班无冲突，可以发布！")

        elif '数据' in user_input or 'sync' in input_lower:
            return ("🔄 同步医院 HIS 系统数据...\n\n"
                   "✓ 已同步内容：\n"
                   "  • 医护人员信息: 152 人\n"
                   "  • 科室部门: 18 个\n"
                   "  • 班次规则: 8 套\n\n"
                   "数据同步完成，可用于排班计算。")

        elif '帮助' in user_input or 'help' in input_lower:
            return ("🤖 我是医院排班智能助手，支持以下功能：\n\n"
                   "1️⃣ 生成排班 - \"生成下周排班\"\n"
                   "2️⃣ 校验排班 - \"校验当前排班\"\n"
                   "3️⃣ 同步数据 - \"同步 HIS 数据\"\n"
                   "4️⃣ 查询班次 - \"查看本月班次\"\n\n"
                   "输入上述关键词，我会为你处理排班相关任务！")

        else:
            return (f"💬 我收到你的消息：\"{user_input}\"\n\n"
                   "我是医院排班智能助手，可以帮你：\n"
                   "• 生成最优排班方案\n"
                   "• 检测排班冲突\n"
                   "• 同步员工和部门数据\n\n"
                   "输入 \"帮助\" 了解更多功能！")

    def do_OPTIONS(self):
        """处理 CORS 预检请求"""
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()


def run_server(port=8000):
    """启动 HTTP 服务器"""
    server_address = ('127.0.0.1', port)
    httpd = HTTPServer(server_address, CozeHTTPHandler)
    logger.info(f"Coze API 服务器启动在 http://127.0.0.1:{port}")
    logger.info("按 Ctrl+C 停止服务器")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        logger.info("服务器已停止")
        httpd.server_close()


if __name__ == '__main__':
    port = int(os.getenv('COZE_API_PORT', '8000'))
    run_server(port)
