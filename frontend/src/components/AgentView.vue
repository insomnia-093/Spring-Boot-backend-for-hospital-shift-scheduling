<template>
  <div class="agent-layout single-pane">
    <section class="agent-thread full-width">
      <div class="card chat-header">
        <div>
          <h2>智能体排班助手</h2>
          <div class="text-sm text-muted">与排班Agent实时沟通</div>
        </div>
        <div style="display: flex; gap: 0.75rem; align-items: center;">
          <span :class="['connection-badge', wsConnected ? 'online' : '']">
            <span class="dot"></span>
            {{ wsStatus }}
          </span>
          <button v-if="!wsConnected" class="secondary" style="width: auto; padding: 0.4rem 0.75rem;" @click="connectWs">
            重新连接
          </button>
          <button class="ghost btn-refresh" style="width: auto;" @click="loadChatHistory">刷新对话</button>
        </div>
      </div>

      <div class="card chat-panel main-chat">
        <div class="chat-list" ref="chatListRef">
          <div v-if="messages.length === 0" class="text-muted text-sm">暂无对话，输入消息开始沟通</div>
          <div v-for="(msg, idx) in messages" :key="idx" :class="['chat-bubble', 'role-' + (msg.role || 'CLIENT')]">
            <div class="chat-bubble-header">
              <div class="chat-sender">
                <span v-if="msg.role === 'AGENT'" class="badge-agent">🤖 Agent</span>
                <span v-else-if="msg.role === 'SYSTEM'" class="badge-system">⚙️ System</span>
                <span v-else class="badge-client">👤 User</span>
                <strong>{{ msg.sender || '匿名' }}</strong>
              </div>
              <span class="chat-time">{{ formatTime(msg.timestamp) }}</span>
            </div>
            <div class="chat-bubble-content">{{ msg.content }}</div>
          </div>
        </div>
        <div class="input-row chat-input">
          <input
            type="text"
            v-model="input"
            placeholder="输入要发送给智能体的消息..."
            @keyup.enter="send"
            :disabled="!wsConnected || loading"
          />
          <button style="width: auto;" @click="send" :disabled="!wsConnected || loading">
            {{ loading ? '处理中...' : '发送' }}
          </button>
        </div>
      </div>
      <div class="text-muted text-sm text-center mt-2">实时消息通过 WebSocket 同步，多端可见。</div>
    </section>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted } from 'vue';

const props = defineProps({
  messages: Array,
  wsConnected: Boolean,
  wsStatus: String,
  loading: Boolean
});

const emit = defineEmits(['send', 'connect', 'refresh']);

const input = ref('');
const chatListRef = ref(null);

const send = () => {
  if (!input.value.trim()) return;
  emit('send', input.value);
  input.value = '';
};

const connectWs = () => {
  emit('connect');
};

const loadChatHistory = () => {
  emit('refresh');
};

const formatTime = (isoString) => {
  if (!isoString) return '-';
  return new Date(isoString).toLocaleString();
};

const scrollToBottom = () => {
  nextTick(() => {
    if (chatListRef.value) {
      chatListRef.value.scrollTop = chatListRef.value.scrollHeight;
    }
  });
};

watch(() => props.messages.length, scrollToBottom);
onMounted(scrollToBottom);
</script>

<style scoped>
.agent-layout {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f3ff 0%, #f0ebff 100%);
  padding: 0;
}

.agent-thread {
  display: flex;
  flex-direction: column;
  gap: 0;
  height: 100%;
  background: transparent;
}

.full-width {
  width: 100%;
  margin: 0;
  padding: 0;
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  margin: 16px;
  margin-bottom: 0;
}

.chat-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #2c2c2c;
}

.chat-header .text-muted {
  color: #666;
  font-size: 13px;
}

.main-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin: 0 16px 16px 16px;
  overflow: hidden;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-bubble {
  display: flex;
  flex-direction: column;
  gap: 6px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-bubble-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.chat-sender {
  display: flex;
  align-items: center;
  gap: 6px;
}

.chat-sender strong {
  color: #2c2c2c;
  font-weight: 600;
}

.badge-agent {
  display: inline-block;
  padding: 2px 8px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.badge-system {
  display: inline-block;
  padding: 2px 8px;
  background: #fbbf24;
  color: #1f2937;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.badge-client {
  display: inline-block;
  padding: 2px 8px;
  background: #e5e7eb;
  color: #1f2937;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.chat-time {
  font-size: 11px;
  color: #aaa;
}

.chat-bubble-content {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.5;
  word-break: break-word;
  background: linear-gradient(135deg, #ede9fe, #f3e8ff);
  border-left: 4px solid #6366f1;
  color: #2c2c2c;
  font-size: 14px;
}

.role-AGENT .chat-bubble-content {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-left-color: #6366f1;
}

.role-SYSTEM .chat-bubble-content {
  background: #fef3c7;
  color: #1f2937;
  border-left-color: #fbbf24;
}

.input-row {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #f3f0ff;
  background: white;
}

.input-row input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e5e0ff;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
  transition: all 0.2s ease;
}

.input-row input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.input-row input:disabled {
  background: #f9f7ff;
  color: #ccc;
  cursor: not-allowed;
}

.input-row button {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.input-row button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.input-row button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.text-muted {
  color: #999;
}

.text-sm {
  font-size: 13px;
}

.text-center {
  text-align: center;
}

.mt-2 {
  margin-top: 8px;
}

.connection-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  background: #f0f0f0;
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.connection-badge.online {
  background: #dcfce7;
  color: #166534;
}

.connection-badge .dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ccc;
  animation: pulse 2s infinite;
}

.connection-badge.online .dot {
  background: #22c55e;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.secondary {
  padding: 8px 16px;
  border: 1px solid #e5e0ff;
  border-radius: 6px;
  background: white;
  color: #6366f1;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.secondary:hover {
  background: #f9f7ff;
  border-color: #6366f1;
}

.ghost {
  background: transparent;
  border: none;
  color: #6366f1;
  padding: 8px 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ghost:hover {
  background: rgba(99, 102, 241, 0.1);
}

.btn-refresh {
  font-weight: 500;
}
</style>
