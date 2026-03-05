<template>
  <AuthParams v-if="!auth.token" @login-success="handleLoginSuccess" />
  <AppLayout
    v-else
    :user="user"
    :current-view="currentView"
    :ws-connected="wsConnected"
    :ws-status="wsStatus"
    :notifications="notifications"
    @logout="handleLogout"
    @navigate="navigate"
  >
    <template #dashboard>
      <DashboardView
        :departments="departments"
        :shifts="shifts"
        :pending-task-count="pendingTaskCount"
        :summary="summary"
        :my-shifts="myShifts"
        :calendar-title="calendarTitle"
        :calendar-days="calendarDays"
        v-model:filterDeptId="calendarFilterDeptId"
        @refresh="loadDashboard"
        @changeMonth="changeMonth"
        @openDay="openCalendarDay"
      />
    </template>

    <template #shifts>
      <ShiftsView
        :shifts="shifts"
        :loading="loadingData"
        :departments="departments"
        :assignee-distribution="summary.assigneeDistribution"
        :department-distribution="summary.departmentDistribution"
        :is-admin="isAdmin"
        @refresh="loadShifts"
        @edit-shift="updateShiftDetails"
      />
    </template>

    <template #agent>
      <AgentView
        :messages="chatMessages"
        :ws-connected="wsConnected"
        :ws-status="wsStatus"
        :loading="loadingAgent"
        @send="sendChat"
        @connect="connectWs"
        @refresh="loadChatHistory"
      />
    </template>

    <template #profile>
      <ProfileView
        :user="user"
        :last-login="lastLogin"
        :is-admin="isAdmin"
        :admin-users="adminUsers"
        :shifts="shifts"
        :loading="loading"
        :pie-data="departmentShiftPie"
        @navigate="navigate"
        @reset-password="resetUserPassword"
        @update-shift="updateShiftDetails"
      />
    </template>
  </AppLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useApi } from './composables/useApi';
import { useWebSocket } from './composables/useWebSocket';

import AuthParams from './components/AuthParams.vue';
import AppLayout from './components/AppLayout.vue';
import DashboardView from './components/DashboardView.vue';
import ShiftsView from './components/ShiftsView.vue';
import AgentView from './components/AgentView.vue';
import ProfileView from './components/ProfileView.vue';

const { api, token: authToken, API_BASE } = useApi();
const WS_BASE = import.meta.env.VITE_WS_BASE || 'http://localhost:9090/ws';

// State
const loading = ref(false);
const loadingData = ref(false);
const loadingAgent = ref(false);

const auth = reactive({
  token: localStorage.getItem('jwt_token') || null
});

const { isConnected: wsConnected, status: wsStatus, connect: wsConnect, disconnect: wsDisconnect, sendMessage: wsSendMessage } = useWebSocket(WS_BASE, auth.token);

const user = reactive(JSON.parse(localStorage.getItem('user_info') || '{}'));
const currentView = ref('dashboard');
const lastLogin = ref(localStorage.getItem('last_login') || null);

const departments = ref([]);
const shifts = ref([]);
const agentTasks = ref([]);
const notifications = ref([]);
const summary = reactive({
  totalShifts: 0,
  nightShifts: 0,
  assignedShifts: 0,
  unassignedShifts: 0,
  totalAssignees: 0,
  roleDistribution: [],
  departmentDistribution: [],
  assigneeDistribution: []
});

const calendarMonth = ref(new Date());
const calendarFilterDeptId = ref('');
const calendarData = reactive({
  year: 0,
  month: 0,
  calendarShifts: {}
});
const myShiftItems = ref([]);
const chatMessages = ref([]);
const adminUsers = ref([]);

// Computed
const isAdmin = computed(() => Array.isArray(user.roles) && user.roles.includes('ADMIN'));
const pendingTaskCount = computed(() => agentTasks.value.filter(task => ['PENDING', 'IN_PROGRESS'].includes(task.status)).length);

const myShifts = computed(() => myShiftItems.value || []);

const calendarTitle = computed(() => {
  const date = calendarMonth.value;
  return `${date.getFullYear()}年${date.getMonth() + 1}月`;
});

const pad2 = (val) => String(val).padStart(2, '0');

const formatDateKey = (date) => `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;

const formatMonthParam = (input) => {
  const date = input instanceof Date ? input : new Date(input);
  if (Number.isNaN(date.getTime())) {
    const now = new Date();
    return `${now.getFullYear()}-${pad2(now.getMonth() + 1)}`;
  }
  return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}`;
};

const formatDateParam = (input) => {
  const date = input instanceof Date ? input : new Date(input);
  if (Number.isNaN(date.getTime())) {
    return formatDateKey(new Date());
  }
  return formatDateKey(date);
};

const calendarDays = computed(() => {
  const date = calendarMonth.value;
  const year = date.getFullYear();
  const month = date.getMonth();
  const firstDay = new Date(year, month, 1);
  const startOffset = firstDay.getDay();

  const startDate = new Date(year, month, 1 - startOffset);

  const map = new Map();
  const calendarShifts = calendarData.calendarShifts || {};
  Object.entries(calendarShifts).forEach(([key, items]) => {
    const filtered = (items || []).filter(item => {
      if (!calendarFilterDeptId.value) return true;
      return String(item.departmentId || '') === String(calendarFilterDeptId.value);
    });
    if (!filtered.length) return;

    const mapped = filtered.map(item => {
      const dept = item.departmentName || '科室';
      const summary = item.summary || '值班';
      const headcount = item.headcount != null ? `(${item.headcount}人)` : '';
      return { type: 'calendar', label: `${dept} ${summary}${headcount}`.trim() };
    });
    if (mapped.length) {
      map.set(key, mapped);
    }
  });

  const days = [];
  for (let i = 0; i < 42; i += 1) {
    const cellDate = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate() + i);
    const key = formatDateKey(cellDate);
    days.push({
      key,
      dayNumber: cellDate.getDate(),
      inMonth: cellDate.getMonth() === month,
      items: map.get(key) || []
    });
  }
  return days;
});

const piePalette = ['#6366f1', '#38bdf8', '#f59e0b', '#22c55e', '#a855f7', '#f97316'];
const departmentShiftPie = computed(() => {
  const map = new Map();
  (shifts.value || []).filter(s => !!s.assigneeUserId).forEach(s => {
    const name = s.departmentName || '未知';
    map.set(name, (map.get(name) || 0) + 1);
  });

  const items = Array.from(map.entries()).map(([label, value], i) => ({
    label, value, color: piePalette[i % piePalette.length]
  }));

  // Conic gradient style
  let acc = 0;
  const total = items.reduce((sum, i) => sum + i.value, 0) || 1;
  const stops = items.map(item => {
    const start = (acc / total) * 100;
    acc += item.value;
    const end = (acc / total) * 100;
    return `${item.color} ${start}% ${end}%`;
  });

  return { items, style: { background: `conic-gradient(${stops.join(', ')})` } };
});

// Actions
const handleLoginSuccess = (res) => {
  auth.token = res.token;
  authToken.value = res.token; // Update useApi token state
  localStorage.setItem('jwt_token', res.token);
  Object.assign(user, {
    id: res.userId,
    email: res.email,
    fullName: res.fullName,
    roles: res.roles
  });
  localStorage.setItem('user_info', JSON.stringify(user));
  lastLogin.value = new Date().toISOString();
  navigate('dashboard');

  // Re-initialize WebSocket connection with new token
  connectWs();
};

const handleLogout = () => {
  auth.token = null;
  authToken.value = null; // Clear useApi token state
  localStorage.removeItem('jwt_token');
  localStorage.removeItem('user_info');
  disconnectWs();
};

const navigate = (view) => {
  currentView.value = view;
  if (view === 'dashboard') loadDashboard();
  if (view === 'shifts') loadShifts();
  if (view === 'agent') {
    loadChatHistory();
  }
  if (view === 'profile' && isAdmin.value) {
    loadAdminUsers();
    loadShifts(); // Update shift stats for admin
  }
};

const changeMonth = (offset) => {
  const cur = calendarMonth.value;
  calendarMonth.value = new Date(cur.getFullYear(), cur.getMonth() + offset, 1);
  loadDashboard();
};

const openCalendarDay = (cell) => {
  console.log('Open day', cell);
};

// API Loaders
const updateSummary = (shiftList) => {
  summary.totalShifts = shiftList.length;
  summary.unassignedShifts = shiftList.filter(s => !s.assigneeUserId).length;
  summary.assignedShifts = shiftList.filter(s => !!s.assigneeUserId).length;
  summary.nightShifts = shiftList.filter(s => s.shiftType === 'NIGHT').length;

  const deptMap = new Map();
  const assigneeMap = new Map();
  const roleMap = new Map();

  shiftList.forEach(s => {
    const dept = s.departmentName || s.departmentId || '未分配';
    deptMap.set(dept, (deptMap.get(dept) || 0) + 1);

    if (s.assigneeUserId) {
      const assignee = s.assigneeName || `User ${s.assigneeUserId}`;
      assigneeMap.set(assignee, (assigneeMap.get(assignee) || 0) + 1);
    }

    if (s.requiredRole) {
      roleMap.set(s.requiredRole, (roleMap.get(s.requiredRole) || 0) + 1);
    }
  });

  summary.departmentDistribution = Array.from(deptMap.entries())
    .map(([label, value]) => ({ label, value }))
    .sort((a, b) => b.value - a.value);

  summary.assigneeDistribution = Array.from(assigneeMap.entries())
    .map(([label, value]) => ({ label, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 10);

  // Also populating roleDistribution just in case
  summary.roleDistribution = Array.from(roleMap.entries())
     .map(([label, value]) => ({ label, value }));
};

const applyShiftManagement = (shiftManagement) => {
  if (!shiftManagement) return;

  const shiftList = Array.isArray(shiftManagement.shifts)
    ? shiftManagement.shifts
    : Array.isArray(shiftManagement?.data?.shifts)
      ? shiftManagement.data.shifts
      : [];

  shifts.value = shiftList;

  const computedTotal = shiftList.length;
  const computedAssigned = shiftList.filter((s) => s?.assigneeUserId != null).length;
  const computedPending = Math.max(computedTotal - computedAssigned, 0);
  const computedNight = shiftList.filter((s) => {
    const t = String(s?.startTime || '');
    return t.includes('T16:') || t.includes('T17:') || t.includes('T18:') || t.includes('T19:') ||
      t.includes('T20:') || t.includes('T21:') || t.includes('T22:') || t.includes('T23:');
  }).length;

  const stats = shiftManagement.stats || shiftManagement?.data?.stats || {};
  const statsTotal = Number(stats.totalShifts ?? NaN);
  const statsAssigned = Number(stats.assignedShifts ?? NaN);
  const statsPending = Number(stats.pendingShifts ?? stats.unassignedShifts ?? NaN);
  const statsNight = Number(stats.nightShifts ?? NaN);

  const preferComputed = computedTotal > 0 && (!Number.isFinite(statsTotal) || statsTotal === 0);

  summary.totalShifts = preferComputed ? computedTotal : (Number.isFinite(statsTotal) ? statsTotal : computedTotal);
  summary.assignedShifts = preferComputed ? computedAssigned : (Number.isFinite(statsAssigned) ? statsAssigned : computedAssigned);
  summary.unassignedShifts = preferComputed ? computedPending : (Number.isFinite(statsPending) ? statsPending : computedPending);
  summary.nightShifts = preferComputed ? computedNight : (Number.isFinite(statsNight) ? statsNight : computedNight);

  const charts = shiftManagement.charts || shiftManagement?.data?.charts || {};
  summary.assigneeDistribution = Array.isArray(charts.staffDistribution) ? charts.staffDistribution : [];
  summary.departmentDistribution = Array.isArray(charts.departmentDistribution) ? charts.departmentDistribution : [];
};

const applyOverviewPanel = (overviewPanel) => {
  if (!overviewPanel) return;
  myShiftItems.value = overviewPanel.myShifts || [];
  const calendar = overviewPanel.calendar || {};
  calendarData.year = calendar.year || calendarData.year;
  calendarData.month = calendar.month || calendarData.month;
  calendarData.calendarShifts = calendar.calendarShifts || {};
};

const DEMO_MONTH_FALLBACK = '2026-03';

const loadShifts = async () => {
  loadingData.value = true;
  try {
    let visualization = null;
    const monthParam = formatMonthParam(calendarMonth.value || new Date());

    try {
      visualization = await api(`/analytics/visualization?month=${monthParam}`);
    } catch (_) {
      visualization = null;
    }

    if (visualization) {
      applyShiftManagement(visualization.shiftManagement || visualization);
    }

    if (!Array.isArray(shifts.value) || shifts.value.length === 0) {
      const fallbackShifts = await api('/shifts');
      shifts.value = Array.isArray(fallbackShifts) ? fallbackShifts : [];
      updateSummary(shifts.value);
    } else {
      updateSummary(shifts.value);
    }
  } finally {
    loadingData.value = false;
  }
};

const loadDashboard = async () => {
  loadingData.value = true;
  try {
    const monthParam = formatMonthParam(calendarMonth.value || new Date()) || DEMO_MONTH_FALLBACK;

    const [deptRes, taskRes] = await Promise.all([
      api('/departments').catch(() => []),
      api('/agent/tasks/pending').catch(() => [])
    ]);

    departments.value = Array.isArray(deptRes) ? deptRes : [];
    agentTasks.value = Array.isArray(taskRes) ? taskRes : [];

    try {
      const visualization = await api(`/analytics/visualization?month=${monthParam}`);
      if (visualization?.shiftManagement || visualization?.overviewPanel) {
        applyShiftManagement(visualization.shiftManagement || visualization);
        applyOverviewPanel(visualization.overviewPanel || {});
      }
    } catch (_) {
      // visualization is optional, fallback below
    }

    if (!Array.isArray(shifts.value) || shifts.value.length === 0) {
      const shiftRes = await api('/shifts').catch(() => []);
      shifts.value = Array.isArray(shiftRes) ? shiftRes : [];
      updateSummary(shifts.value);
    } else {
      updateSummary(shifts.value);
    }

    if (!calendarData.calendarShifts || Object.keys(calendarData.calendarShifts).length === 0) {
      const dateParam = formatDateParam(calendarMonth.value || new Date());
      const calRes = await api(`/analytics/overview?date=${dateParam}`).catch(() => null);
      if (calRes?.overviewPanel) {
        applyOverviewPanel(calRes.overviewPanel);
      } else if (calRes) {
        applyOverviewPanel(calRes);
      }
    }
  } finally {
    loadingData.value = false;
  }
};

const loadChatHistory = async () => {
  loadingAgent.value = true;
  try {
    const res = await api('/agent/chat?limit=50').catch(() => []);
    const list = Array.isArray(res) ? res : (Array.isArray(res?.items) ? res.items : []);
    chatMessages.value = list.map((m, idx) => ({
      id: m.id ?? `msg-${idx}`,
      role: m.role || (m.sender === 'USER' ? 'user' : 'assistant'),
      content: m.content || m.message || ''
    }));
  } finally {
    loadingAgent.value = false;
  }
};

const sendChat = async (payload) => {
  const text = (payload?.message || payload || '').toString().trim();
  if (!text) return;

  chatMessages.value.push({
    id: `local-user-${Date.now()}`,
    role: 'user',
    content: text
  });

  loadingAgent.value = true;
  try {
    const res = await api('/agent/coze-chat', {
      method: 'POST',
      body: JSON.stringify({ message: text }),
      headers: { 'Content-Type': 'application/json' }
    });

    const reply = res?.reply || res?.content || res?.message || '已发送，暂无回复内容';
    chatMessages.value.push({
      id: `local-bot-${Date.now()}`,
      role: 'assistant',
      content: reply
    });
  } catch (e) {
    chatMessages.value.push({
      id: `local-err-${Date.now()}`,
      role: 'assistant',
      content: `发送失败: ${e?.message || '未知错误'}`
    });
  } finally {
    loadingAgent.value = false;
  }
};

const loadAdminUsers = async () => {
  if (!isAdmin.value) return;
  try {
    const res = await api('/admin/users').catch(() => []);
    adminUsers.value = Array.isArray(res) ? res : (Array.isArray(res?.items) ? res.items : []);
  } catch (_) {
    adminUsers.value = [];
  }
};

const resetUserPassword = async (payload) => {
  if (!isAdmin.value || !payload) return;
  await api('/admin/users/reset-password', {
    method: 'POST',
    body: JSON.stringify(payload),
    headers: { 'Content-Type': 'application/json' }
  });
  await loadAdminUsers();
};

const normalizeShiftPayload = (payload) => ({
  id: payload.id,
  departmentId: payload.departmentId ?? null,
  assigneeUserId: payload.assigneeUserId ?? null,
  requiredRole: payload.requiredRole ?? null,
  shiftType: payload.shiftType ?? null,
  status: payload.status ?? null,
  startTime: payload.startTime ?? null,
  endTime: payload.endTime ?? null
});

const updateShiftDetails = async (payload) => {
  if (!payload?.id) return;
  const body = normalizeShiftPayload(payload);

  // Prefer admin endpoint for explicit permissions; fallback to shared endpoint.
  try {
    await api(`/admin/shifts/${payload.id}`, {
      method: 'PUT',
      body: JSON.stringify(body),
      headers: { 'Content-Type': 'application/json' }
    });
  } catch (_) {
    await api(`/shifts/${payload.id}`, {
      method: 'PUT',
      body: JSON.stringify(body),
      headers: { 'Content-Type': 'application/json' }
    });
  }

  await Promise.all([loadShifts(), loadDashboard()]);
};

const connectWs = () => {
  try {
    wsConnect();
  } catch (_) {
    // no-op
  }
};

const disconnectWs = () => {
  try {
    wsDisconnect();
  } catch (_) {
    // no-op
  }
};

onMounted(async () => {
  if (!auth.token) return;
  authToken.value = auth.token;
  connectWs();
  await loadDashboard();
});
</script>