import { ref } from 'vue';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * WebSocket 连接 Composable
 *
 * 基于 STOMP 协议的实时通信，支持：
 * - 自动重连
 * - 心跳检测
 * - 多端消息同步
 *
 * @param {string} url - WebSocket 服务器 URL
 * @param {string} token - 认证令牌
 * @returns {object} WebSocket 相关状态和方法
 */
export function useWebSocket(url, token) {
  // 连接状态
  const isConnected = ref(false);

  // 连接状态文本
  const status = ref('未连接');

  // STOMP 客户端实例
  const client = ref(null);

  // 接收的消息列表
  const messages = ref([]);

  /**
   * 连接 WebSocket
   * @param {function} onMessageReceived - 消息接收回调
   */
  const connect = (onMessageReceived) => {
    // 如果已连接，直接返回
    if (client.value && client.value.active) return;

    status.value = '连接中...';

    // 创建 STOMP 客户端
    const stompClient = new Client({
      // WebSocket 工厂函数
      webSocketFactory: () => new SockJS(url),

      // 连接头，包含认证信息
      connectHeaders: { Authorization: `Bearer ${token}` },

      // 重连延迟（毫秒）
      reconnectDelay: 5000,

      // 心跳检测设置
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      // 连接成功回调
      onConnect: () => {
        isConnected.value = true;
        status.value = '已连接';
        console.log('WebSocket 已连接');

        // 订阅代理消息主题
        stompClient.subscribe('/topic/agent-chat', (message) => {
          try {
            const body = JSON.parse(message.body);
            messages.value.push(body);
            if (onMessageReceived) onMessageReceived(body);
          } catch (e) {
            console.error('消息解析失败:', e);
          }
        });
      },

      // STOMP 错误回调
      onStompError: (frame) => {
        console.error('Broker 报告错误: ' + frame.headers['message']);
        console.error('详细信息: ' + frame.body);
        status.value = '连接错误';
        isConnected.value = false;
      },

      // WebSocket 关闭回调
      onWebSocketClose: () => {
        status.value = '连接断开';
        isConnected.value = false;
      }
    });

    // 激活客户端
    stompClient.activate();
    client.value = stompClient;
  };

  /**
   * 断开 WebSocket 连接
   */
  const disconnect = () => {
    if (client.value) {
      client.value.deactivate();
      client.value = null;
    }
    isConnected.value = false;
    status.value = '已断开';
  };

  /**
   * 发送消息
   * @param {string} destination - 消息目标地址
   * @param {object} body - 消息体
   */
  const sendMessage = (destination, body) => {
    if (client.value && isConnected.value) {
      client.value.publish({
        destination: destination,
        body: JSON.stringify(body),
        headers: { 'content-type': 'application/json' }
      });
    } else {
      console.warn('WebSocket 未连接');
    }
  };

  return {
    isConnected,
    status,
    messages,
    connect,
    disconnect,
    sendMessage
  };
}
