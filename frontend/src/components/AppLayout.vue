<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-icon">🏥</div>
        <div>
          <div class="brand-title">排班系统</div>
          <div class="text-muted text-sm">Hospital Agent</div>
        </div>
      </div>

      <nav class="mt-4">
        <div class="nav-item" :class="{ active: currentView === 'dashboard' }" @click="navigate('dashboard')">
          📊 概览面板
        </div>
        <div class="nav-item" :class="{ active: currentView === 'shifts' }" @click="navigate('shifts')">
          📅 排班管理
        </div>
        <div class="nav-item" :class="{ active: currentView === 'agent' }" @click="navigate('agent')">
          🤖 智能排班
        </div>
        <div class="nav-item" :class="{ active: currentView === 'profile' }" @click="navigate('profile')">
          🧑‍⚕️ 个人中心
        </div>
      </nav>

      <div class="profile-card">
        <div class="avatar">{{ userInitials }}</div>
        <div>
          <div class="profile-name">{{ user.fullName || '未命名用户' }}</div>
          <div class="text-muted text-sm">{{ user.roles ? user.roles.join(', ') : '' }}</div>
        </div>
      </div>
      <div class="connection-badge" :class="{ online: wsConnected }">
        <span class="dot"></span>
        {{ wsStatus }}
      </div>
      <button class="secondary" @click="logout">退出登录</button>
    </aside>

    <main class="main-content">
      <header class="topbar">
        <div>
          <div class="page-title">{{ viewTitle }}</div>
          <div class="text-muted text-sm">欢迎回来，{{ user.fullName || '用户' }}</div>
        </div>
        <div class="topbar-actions">
          <button class="ghost" @click="navigate('dashboard')">概览</button>
          <button class="ghost" @click="navigate('shifts')">班次</button>
          <button class="ghost" @click="navigate('agent')">智能体</button>
          <button class="ghost" @click="navigate('profile')">个人中心</button>
        </div>
      </header>

      <div v-if="notifications.length" class="notice-stack">
        <div v-for="note in notifications" :key="note.id" class="notice-pill">
          <span>{{ note.message }}</span>
          <span class="text-muted text-sm">{{ note.time }}</span>
        </div>
      </div>

      <!-- Dashboard View -->
      <div v-if="currentView === 'dashboard'" class="page">
        <!-- (This part would be refactored into a Dashboard component) -->
        <slot name="dashboard"></slot>
      </div>

      <!-- Shifts View -->
      <div v-if="currentView === 'shifts'" class="page">
        <!-- (This part would be refactored into a Shifts component) -->
        <slot name="shifts"></slot>
      </div>

      <!-- Agent View -->
      <div v-if="currentView === 'agent'" class="page">
        <!-- (This part would be refactored into an AgentChat component) -->
        <slot name="agent"></slot>
      </div>

      <!-- Profile View -->
      <div v-if="currentView === 'profile'" class="page">
        <!-- (This part would be refactored into a Profile component) -->
        <slot name="profile"></slot>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  user: Object,
  currentView: String,
  wsConnected: Boolean,
  wsStatus: String,
  notifications: Array
});

const emit = defineEmits(['logout', 'navigate']);

const viewTitle = computed(() => {
  if (props.currentView === 'dashboard') return '概览面板';
  if (props.currentView === 'shifts') return '班次管理';
  if (props.currentView === 'agent') return '智能体中心';
  if (props.currentView === 'profile') return '个人中心';
  return '排班系统';
});

const userInitials = computed(() => {
  const name = (props.user.fullName || props.user.email || 'U').trim();
  if (!name) return 'U';
  if (name.length <= 2) return name.toUpperCase();
  return name.slice(0, 2).toUpperCase();
});

const navigate = (view) => {
  emit('navigate', view);
};

const logout = () => {
  emit('logout');
};
</script>

<style scoped>
/* App Layout Container */
.app-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #f5f0ff 0%, #ede9ff 100%);
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 250px;
  background: white;
  border-right: 1px solid #e5e0ff;
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.brand-icon {
  font-size: 32px;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-title {
  font-size: 16px;
  font-weight: 700;
  color: #2c2c2c;
}

.text-muted {
  color: #999;
}

.text-sm {
  font-size: 12px;
}

/* Navigation */
nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.nav-item {
  padding: 12px 14px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
  color: #666;
  font-size: 13px;
  font-weight: 500;
  margin: 2px 0;
}

.nav-item:hover {
  background: #f9f7ff;
  color: #6366f1;
}

.nav-item.active {
  background: #ede9fe;
  color: #6366f1;
  font-weight: 600;
}

/* Profile Card */
.profile-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9f7ff;
  border-radius: 8px;
  margin-bottom: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.profile-name {
  font-size: 13px;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 2px;
}

/* Connection Badge */
.connection-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  background: #f0f0f0;
  font-size: 11px;
  color: #666;
  margin-bottom: 12px;
}

.connection-badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ccc;
}

.connection-badge.online {
  background: #dcfce7;
  color: #15803d;
}

.connection-badge.online .dot {
  background: #22c55e;
}

/* Buttons */
.secondary {
  padding: 8px 16px;
  border: 1px solid #e5e0ff;
  border-radius: 6px;
  background: white;
  color: #6366f1;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  width: 100%;
}

.secondary:hover {
  background: #f9f7ff;
  border-color: #6366f1;
}

/* Main Content */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Topbar */
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e5e0ff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #2c2c2c;
  margin: 0;
}

.topbar-actions {
  display: flex;
  gap: 12px;
}

.ghost {
  padding: 8px 14px;
  border: none;
  background: transparent;
  color: #666;
  cursor: pointer;
  font-size: 12px;
  border-radius: 6px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.ghost:hover {
  background: #f9f7ff;
  color: #6366f1;
}

/* Notice Stack */
.notice-stack {
  padding: 8px 24px;
  background: #fef3c7;
  border-bottom: 1px solid #fcd34d;
  display: flex;
  gap: 12px;
  overflow-x: auto;
}

.notice-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  padding: 8px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: #92400e;
  white-space: nowrap;
  border: 1px solid #fcd34d;
}

/* Page Content */
.page {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.mt-4 {
  margin-top: 16px;
}
</style>
