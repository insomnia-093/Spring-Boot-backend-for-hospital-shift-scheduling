import { createApp, ref, reactive, onMounted, computed } from 'vue';
// SockJS in some bundlers expects a Node-style global.
if (typeof globalThis.global === 'undefined') {
  globalThis.global = globalThis;
}
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import './style.css';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:9090/api';
const WS_BASE = import.meta.env.VITE_WS_BASE || 'http://localhost:9090/ws';

createApp({
  setup() {
    const loading = ref(false);
    const loadingData = ref(false);
    const loadingAgent = ref(false);
    const wsConnected = ref(false);
    const wsStatus = ref('未连接');
    const wsClient = ref(null);

    const auth = reactive({
      token: localStorage.getItem('jwt_token') || null,
      view: 'login',
      form: { email: '', password: '' },
      registerForm: {
        email: '',
        password: '',
        fullName: '',
        roles: 'ADMIN',
        departmentId: null,
        departmentName: ''
      }
    });

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
    // 将 changeMonth 作为函数声明提前，避免在 setup 执行期间引用时产生 ReferenceError
    function changeMonth(offset) {
      if (!offset) return;
      const cur = calendarMonth.value || new Date();
      calendarMonth.value = new Date(cur.getFullYear(), cur.getMonth() + offset, 1);
      // 仅刷新必要数据
      loadDashboard();
    }
    const calendarEntries = ref([]);
    const calendarFilterDeptId = ref('');
    const calendarModal = reactive({ open: false, date: '', items: [] });
    const newCalendarEntry = reactive({ summary: '', headcount: '', departmentId: '' });

    const chatMessages = ref([]);
    const chatInput = ref('');

    const adminUsers = ref([]);
    const adminPasswordForm = reactive({ userId: '', newPassword: '' });
    const adminShiftForm = reactive({
      shiftId: '',
      startTime: '',
      endTime: '',
      requiredRole: 'DOCTOR',
      status: 'OPEN',
      departmentId: '',
      assigneeUserId: '',
      notes: ''
    });

    const newTask = reactive({
      type: 'GENERATE_SCHEDULE',
      payload: ''
    });

    const agentFilter = ref('ALL');
    const quickPrompts = [
      { label: '生成本周排班', type: 'GENERATE_SCHEDULE', payload: '{"range":"本周","rules":["夜班平衡","优先主任医师"]}' },
      { label: '校验当前排班', type: 'VALIDATE_SCHEDULE', payload: '{"focus":"冲突检测","scope":"全科"}' },
      { label: '同步科室数据', type: 'DATA_SYNC', payload: '{"source":"his","mode":"incremental"}' }
    ];

    const viewTitle = computed(() => {
      if (currentView.value === 'dashboard') return '概览面板';
      if (currentView.value === 'shifts') return '班次管理';
      if (currentView.value === 'agent') return '智能体中心';
      if (currentView.value === 'profile') return '个人中心';
      return '排班系统';
    });

    const isAdmin = computed(() => {
      return Array.isArray(user.roles) && user.roles.includes('ADMIN');
    });

    const userInitials = computed(() => {
      const name = (user.fullName || user.email || 'U').trim();
      if (!name) return 'U';
      if (name.length <= 2) return name.toUpperCase();
      return name.slice(0, 2).toUpperCase();
    });

    const pendingTaskCount = computed(() => {
      return agentTasks.value.filter(task => task.status === 'PENDING' || task.status === 'IN_PROGRESS').length;
    });

    const filteredAgentTasks = computed(() => {
      if (agentFilter.value === 'ALL') return agentTasks.value;
      return agentTasks.value.filter(task => task.status === agentFilter.value);
    });

    const isNightShift = (startTime, endTime) => {
      if (!startTime) return false;
      const start = new Date(startTime);
      const end = endTime ? new Date(endTime) : null;
      const isNightHour = (date) => {
        const hour = date.getHours();
        return hour >= 18 || hour < 6;
      };
      if (isNightHour(start)) return true;
      if (end && isNightHour(end)) return true;
      return false;
    };

    const buildPieStyle = (items) => {
      const total = items.reduce((sum, item) => sum + item.value, 0);
      if (!total) {
        return { background: '#e5e7eb' };
      }
      let acc = 0;
      const stops = items.map(item => {
        const start = (acc / total) * 100;
        acc += item.value;
        const end = (acc / total) * 100;
        return `${item.color} ${start}% ${end}%`;
      });
      return { background: `conic-gradient(${stops.join(', ')})` };
    };

    const nightShiftItems = computed(() => {
      return shifts.value.filter(shift => isNightShift(shift.startTime, shift.endTime));
    });

    const nightAssigneesByDepartment = computed(() => {
      const map = new Map();
      nightShiftItems.value.forEach(shift => {
        if (!shift.assigneeUserId) return;
        const key = shift.departmentName || `科室 ${shift.departmentId || '-'}`;
        if (!map.has(key)) map.set(key, new Set());
        map.get(key).add(shift.assigneeUserId);
      });
      return Array.from(map.entries()).map(([label, users]) => ({
        label,
        value: users.size
      }));
    });

    const nightRoleCounts = computed(() => {
      const map = new Map();
      nightShiftItems.value.forEach(shift => {
        const key = shift.requiredRole || '未指定';
        map.set(key, (map.get(key) || 0) + 1);
      });
      return Array.from(map.entries()).map(([label, value]) => ({ label, value }));
    });

    const nightAssignmentStatus = computed(() => {
      let assigned = 0;
      let unassigned = 0;
      nightShiftItems.value.forEach(shift => {
        if (shift.assigneeUserId) assigned += 1;
        else unassigned += 1;
      });
      return [
        { label: '已指派', value: assigned },
        { label: '待指派', value: unassigned }
      ];
    });

    const piePalette = ['#6366f1', '#38bdf8', '#f59e0b', '#22c55e', '#a855f7', '#f97316'];

    const buildPieData = (items) => {
      return items.map((item, index) => ({
        ...item,
        color: piePalette[index % piePalette.length]
      }));
    };

    const nightPieDept = computed(() => {
      const items = buildPieData(nightAssigneesByDepartment.value);
      return { items, style: buildPieStyle(items) };
    });

    const nightPieRole = computed(() => {
      const items = buildPieData(nightRoleCounts.value);
      return { items, style: buildPieStyle(items) };
    });

    const nightPieStatus = computed(() => {
      const items = buildPieData(nightAssignmentStatus.value);
      return { items, style: buildPieStyle(items) };
    });

    const barWidth = (value) => {
      const values = summary.assigneeDistribution.map(item => item.value || 0);
      const max = Math.max(1, ...values);
      const ratio = Math.min(1, (value || 0) / max);
      return `${Math.round(ratio * 100)}%`;
    };

    const myShifts = computed(() => {
      if (!user.id) return [];
      return shifts.value
        .filter(shift => String(shift.assigneeUserId || '') === String(user.id))
        .sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
        .slice(0, 6);
    });

    const calendarTitle = computed(() => {
      const date = calendarMonth.value;
      return `${date.getFullYear()}年${date.getMonth() + 1}月`;
    });

    const formatDateKey = (date) => {
      const pad = (val) => String(val).padStart(2, '0');
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
    };

    const getMonthRange = (date) => {
      const start = new Date(date.getFullYear(), date.getMonth(), 1);
      const end = new Date(date.getFullYear(), date.getMonth() + 1, 0);
      return { start, end };
    };

    const toIsoStart = (date) => {
      return new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0).toISOString();
    };

    const toIsoEnd = (date) => {
      return new Date(date.getFullYear(), date.getMonth(), date.getDate(), 23, 59, 59).toISOString();
    };

    const calendarItemsMap = computed(() => {
      const map = new Map();
      shifts.value.forEach(shift => {
        if (!shift.startTime) return;
        if (calendarFilterDeptId.value && String(shift.departmentId) !== String(calendarFilterDeptId.value)) return;
        const date = new Date(shift.startTime);
        const key = formatDateKey(date);
        const label = `${shift.departmentName || '科室'} ${shift.requiredRole || ''}`.trim();
        if (!map.has(key)) map.set(key, []);
        map.get(key).push({
          type: 'shift',
          label,
          status: shift.status,
          assignee: shift.assigneeName || ''
        });
      });
      calendarEntries.value.forEach(entry => {
        if (calendarFilterDeptId.value && String(entry.departmentId || '') !== String(calendarFilterDeptId.value)) return;
        const key = entry.date;
        if (!map.has(key)) map.set(key, []);
        const dept = entry.departmentName ? `（${entry.departmentName}）` : '';
        const headcount = entry.headcount ? `·${entry.headcount}人` : '';
        map.get(key).push({
          type: 'note',
          label: `${entry.summary}${dept}${headcount}`.trim(),
          status: 'NOTE'
        });
      });
      return map;
    });

    const calendarDays = computed(() => {
      const date = calendarMonth.value;
      const year = date.getFullYear();
      const month = date.getMonth();
      const firstDay = new Date(year, month, 1);
      const startOffset = firstDay.getDay();
      const startDate = new Date(year, month, 1 - startOffset);
      const days = [];
      for (let i = 0; i < 42; i += 1) {
        const cellDate = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate() + i);
        const key = formatDateKey(cellDate);
        days.push({
          key,
          dayNumber: cellDate.getDate(),
          inMonth: cellDate.getMonth() === month,
          items: calendarItemsMap.value.get(key) || []
        });
      }
      return days;
    });

    const openCalendarDay = (cell) => {
      calendarModal.open = true;
      calendarModal.date = cell.key;
      calendarModal.items = cell.items || [];
      newCalendarEntry.summary = '';
      newCalendarEntry.headcount = '';
      newCalendarEntry.departmentId = calendarFilterDeptId.value || '';
    };

    const closeCalendarModal = () => {
      calendarModal.open = false;
      calendarModal.date = '';
      calendarModal.items = [];
    };

    const createCalendarEntry = async () => {
      if (!calendarModal.date || !newCalendarEntry.summary) return;
      loadingData.value = true;
      try {
        await api('/calendar', {
          method: 'POST',
          body: JSON.stringify({
            date: calendarModal.date,
            summary: newCalendarEntry.summary,
            headcount: newCalendarEntry.headcount ? parseInt(newCalendarEntry.headcount, 10) : 0,
            departmentId: newCalendarEntry.departmentId ? parseInt(newCalendarEntry.departmentId, 10) : null
          })
        });
        await loadCalendarData();
        addNotice('值班记录已添加');
      } catch (e) {
        alert('添加失败: ' + e.message);
      } finally {
        loadingData.value = false;
      }
    };

    const loadCalendarData = async () => {
      const { start, end } = getMonthRange(calendarMonth.value);
      const startDate = formatDateKey(start);
      const endDate = formatDateKey(end);
      const startIso = toIsoStart(start);
      const endIso = toIsoEnd(end);

      const results = await Promise.allSettled([
        api(`/shifts/summary?start=${encodeURIComponent(startIso)}&end=${encodeURIComponent(endIso)}`),
        api(`/calendar?start=${startDate}&end=${endDate}`)
      ]);

      if (results[0].status === 'fulfilled') {
        Object.assign(summary, results[0].value);
      }
      if (results[1].status === 'fulfilled') {
        calendarEntries.value = results[1].value;
      }
    };

    const loadDashboard = async () => {
      loadingData.value = true;
      const { start, end } = getMonthRange(calendarMonth.value);
      const startDate = formatDateKey(start);
      const endDate = formatDateKey(end);
      const startIso = toIsoStart(start);
      const endIso = toIsoEnd(end);
      const results = await Promise.allSettled([
        api('/departments'),
        api('/shifts'),
        api('/agent/tasks/pending'),
        api(`/shifts/summary?start=${encodeURIComponent(startIso)}&end=${encodeURIComponent(endIso)}`),
        api(`/calendar?start=${startDate}&end=${endDate}`)
      ]);

      if (results[0].status === 'fulfilled') departments.value = results[0].value;
      if (results[1].status === 'fulfilled') shifts.value = results[1].value;
      if (results[2].status === 'fulfilled') agentTasks.value = results[2].value;
      if (results[3].status === 'fulfilled') Object.assign(summary, results[3].value);
      if (results[4].status === 'fulfilled') calendarEntries.value = results[4].value;

      loadingData.value = false;
    };

    const handleLogin = async () => {
      loading.value = true;
      try {
        const res = await api('/auth/login', {
          method: 'POST',
          body: JSON.stringify(auth.form)
        });
        loginSuccess(res);
      } catch (e) {
        alert('登录失败: ' + e.message);
      } finally {
        loading.value = false;
      }
    };

    const handleRegister = async () => {
      loading.value = true;
      try {
        const roles = auth.registerForm.roles.split(',').map(r => r.trim()).filter(Boolean);
        const departmentId = auth.registerForm.departmentId ? parseInt(auth.registerForm.departmentId, 10) : null;
        const departmentName = auth.registerForm.departmentName ? auth.registerForm.departmentName.trim() : '';
        const body = {
          email: auth.registerForm.email,
          password: auth.registerForm.password,
          fullName: auth.registerForm.fullName,
          roles,
          departmentId: Number.isNaN(departmentId) ? null : departmentId,
          departmentName: departmentName || null
        };

        const res = await api('/auth/register', {
          method: 'POST',
          body: JSON.stringify(body)
        });
        loginSuccess(res);
      } catch (e) {
        alert('注册失败: ' + e.message);
      } finally {
        loading.value = false;
      }
    };

    const loginSuccess = (res) => {
      auth.token = res.token;
      Object.assign(user, {
        id: res.userId,
        email: res.email,
        fullName: res.fullName,
        roles: res.roles
      });
      lastLogin.value = new Date().toISOString();
      localStorage.setItem('jwt_token', res.token);
      localStorage.setItem('user_info', JSON.stringify(user));
      localStorage.setItem('last_login', lastLogin.value);
      navigate('dashboard');
      connectWs();
    };

    const logout = () => {
      auth.token = null;
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user_info');
      disconnectWs();
      Object.keys(user).forEach(key => delete user[key]);
    };

    const navigate = (view) => {
      currentView.value = view;
      if (view === 'dashboard') loadDashboard();
      if (view === 'shifts') loadShifts();
      if (view === 'agent') {
        loadAgentTasks();
        loadChatHistory();
      }
      if (view === 'profile' && isAdmin.value) {
        loadAdminUsers();
        loadShifts();
      }
    };

    const loadShifts = async () => {
      loadingData.value = true;
      try {
        shifts.value = await api('/shifts');
      } catch (e) {
        console.error(e);
      } finally {
        loadingData.value = false;
      }
    };

    const loadAgentTasks = async () => {
      try {
        agentTasks.value = await api('/agent/tasks/pending');
      } catch (e) {
        console.error(e);
      }
    };

    const submitTask = async () => {
      if (!newTask.payload) return;
      loadingAgent.value = true;
      try {
        const res = await api('/agent/tasks', {
          method: 'POST',
          body: JSON.stringify({
            taskType: newTask.type,
            payload: newTask.payload
          })
        });
        alert('任务已创建 ID: ' + res.id);
        newTask.payload = '';
        loadAgentTasks();
      } catch (e) {
        alert('失败: ' + e.message);
      } finally {
        loadingAgent.value = false;
      }
    };

    const applyQuickPrompt = (prompt) => {
      newTask.type = prompt.type;
      newTask.payload = prompt.payload;
    };

    const addNotice = (message) => {
      notifications.value.unshift({
        id: crypto.randomUUID(),
        message,
        time: new Date().toLocaleTimeString()
      });
      notifications.value = notifications.value.slice(0, 5);
    };

    const connectWs = () => {
      if (wsClient.value) return;
      wsStatus.value = '连接中';
      const client = new Client({
        webSocketFactory: () => new SockJS(WS_BASE),
        connectHeaders: auth.token ? { Authorization: `Bearer ${auth.token}` } : {},
        reconnectDelay: 4000,
        onConnect: () => {
          wsConnected.value = true;
          wsStatus.value = '已连接';
          if (currentView.value === 'agent' && chatMessages.value.length === 0) {
            loadChatHistory();
          }
          client.subscribe('/topic/shifts', message => {
            const event = JSON.parse(message.body);
            handleShiftEvent(event);
          });
          client.subscribe('/topic/agent-tasks', message => {
            const event = JSON.parse(message.body);
            handleAgentTaskEvent(event);
          });
          client.subscribe('/topic/notifications', message => {
            const event = JSON.parse(message.body);
            addNotice(event?.payload?.message || '收到新的通知');
          });
          client.subscribe('/topic/agent-chat', message => {
            const chat = JSON.parse(message.body);
            chatMessages.value.push(chat);
            if (currentView.value !== 'agent') {
              addNotice(`${chat.sender || '匿名'}: ${chat.content}`);
            }
          });
        },
        onStompError: () => {
          wsStatus.value = '连接失败';
          wsConnected.value = false;
        },
        onWebSocketClose: () => {
          wsStatus.value = '已断开';
          wsConnected.value = false;
        }
      });

      client.activate();
      wsClient.value = client;
    };

    const disconnectWs = () => {
      if (!wsClient.value) return;
      wsClient.value.deactivate();
      wsClient.value = null;
      wsConnected.value = false;
      wsStatus.value = '已断开';
    };

    const handleShiftEvent = (event) => {
      if (!event || !event.type) return;
      if (event.type === 'SHIFT_CREATED') {
        shifts.value.unshift(event.payload);
        addNotice('新增班次已同步');
      }
      if (event.type === 'SHIFT_UPDATED') {
        const index = shifts.value.findIndex(item => item.id === event.payload.id);
        if (index >= 0) shifts.value.splice(index, 1, event.payload);
        addNotice('班次已更新');
      }
      if (event.type === 'SHIFT_DELETED') {
        shifts.value = shifts.value.filter(item => item.id !== event.payload.shiftId);
        addNotice('班次已删除');
      }
    };

    const handleAgentTaskEvent = (event) => {
      if (!event || !event.type) return;
      if (event.type === 'TASK_CREATED') {
        agentTasks.value.unshift(event.payload);
        addNotice('新增智能体任务');
      }
      if (event.type === 'TASK_UPDATED') {
        const index = agentTasks.value.findIndex(item => item.id === event.payload.id);
        if (index >= 0) agentTasks.value.splice(index, 1, event.payload);
        addNotice('智能体任务已更新');
      }
    };

    const sendChat = () => {
      const content = chatInput.value.trim();
      if (!content || !wsClient.value || !wsConnected.value) return;
      const payload = {
        sender: user.fullName || user.email || '用户',
        role: 'CLIENT',
        content
      };
      wsClient.value.publish({ destination: '/app/agent-chat', body: JSON.stringify(payload) });
      chatInput.value = '';
    };

    const api = async (endpoint, options = {}) => {
      const headers = {
        'Content-Type': 'application/json',
        ...(auth.token ? { 'Authorization': `Bearer ${auth.token}` } : {})
      };

      const res = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers: { ...headers, ...options.headers }
      });

      if (res.status === 401) {
        logout();
        throw new Error('Unauthorized');
      }
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Request failed');
      }
      return res.json();
    };

    const formatTime = (isoString) => {
      if (!isoString) return '-';
      return new Date(isoString).toLocaleString();
    };

    const loadAdminUsers = async () => {
      try {
        adminUsers.value = await api('/admin/users');
      } catch (e) {
        console.error(e);
      }
    };

    const resetUserPassword = async () => {
      if (!adminPasswordForm.userId || !adminPasswordForm.newPassword) return;
      loading.value = true;
      try {
        await api(`/admin/users/${adminPasswordForm.userId}/password`, {
          method: 'PUT',
          body: JSON.stringify({ newPassword: adminPasswordForm.newPassword })
        });
        adminPasswordForm.newPassword = '';
        addNotice('密码已更新');
      } catch (e) {
        alert('修改密码失败: ' + e.message);
      } finally {
        loading.value = false;
      }
    };

    const toLocalInput = (isoString) => {
      if (!isoString) return '';
      const date = new Date(isoString);
      const pad = (val) => String(val).padStart(2, '0');
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
    };

    const loadShiftIntoForm = () => {
      const shift = shifts.value.find(item => String(item.id) === String(adminShiftForm.shiftId));
      if (!shift) return;
      adminShiftForm.startTime = toLocalInput(shift.startTime);
      adminShiftForm.endTime = toLocalInput(shift.endTime);
      adminShiftForm.requiredRole = shift.requiredRole || 'DOCTOR';
      adminShiftForm.status = shift.status || 'OPEN';
      adminShiftForm.departmentId = shift.departmentId ? String(shift.departmentId) : '';
      adminShiftForm.assigneeUserId = shift.assigneeUserId ? String(shift.assigneeUserId) : '';
      adminShiftForm.notes = shift.notes || '';
    };

    const updateShiftDetails = async () => {
      if (!adminShiftForm.shiftId) return;
      loading.value = true;
      try {
        const body = {
          startTime: adminShiftForm.startTime,
          endTime: adminShiftForm.endTime,
          requiredRole: adminShiftForm.requiredRole,
          status: adminShiftForm.status,
          departmentId: adminShiftForm.departmentId ? parseInt(adminShiftForm.departmentId, 10) : null,
          assigneeUserId: adminShiftForm.assigneeUserId ? parseInt(adminShiftForm.assigneeUserId, 10) : null,
          notes: adminShiftForm.notes
        };
        await api(`/admin/shifts/${adminShiftForm.shiftId}`, {
          method: 'PUT',
          body: JSON.stringify(body)
        });
        addNotice('班次已更新');
        loadShifts();
      } catch (e) {
        alert('更新班次失败: ' + e.message);
      } finally {
        loading.value = false;
      }
    };

    const loadChatHistory = async () => {
      try {
        chatMessages.value = await api('/agent/chat?limit=50');
      } catch (e) {
        console.error(e);
      }
    };

    return {
      loading,
      loadingData,
      loadingAgent,
      wsConnected,
      wsStatus,
      auth,
      user,
      currentView,
      lastLogin,
      departments,
      shifts,
      agentTasks,
      notifications,
      summary,
      calendarTitle,
      calendarDays,
      calendarFilterDeptId,
      calendarModal,
      newCalendarEntry,
      myShifts,
      barWidth,
      changeMonth,
      openCalendarDay,
      closeCalendarModal,
      createCalendarEntry,
      chatMessages,
      chatInput,
      newTask,
      agentFilter,
      quickPrompts,
      adminUsers,
      adminPasswordForm,
      adminShiftForm,
      handleLogin,
      handleRegister,
      logout,
      navigate,
      loadDashboard,
      loadShifts,
      submitTask,
      applyQuickPrompt,
      sendChat,
      formatTime,
      viewTitle,
      userInitials,
      pendingTaskCount,
      filteredAgentTasks,
      nightPieDept,
      nightPieRole,
      nightPieStatus,
      isAdmin,
      loadShiftIntoForm,
      resetUserPassword,
      updateShiftDetails,
      loadChatHistory
    };
  },
  template: `
    <div>
      <div v-if="!auth.token" class="auth-container">
        <div class="auth-shell">
          <div class="auth-panel auth-hero">
            <div class="brand">
              <div class="brand-icon">🏥</div>
              <div>
                <div class="brand-title">医院排班系统</div>
                <div class="text-muted text-sm">统一管理排班、科室与智能体任务</div>
              </div>
            </div>
            <p class="text-muted">让排班、协作、智能体指令在一个控制台中完成。</p>
            <div class="auth-metrics">
              <div class="metric-card">
                <div class="metric-value">99.9%</div>
                <div class="text-sm text-muted">任务成功率</div>
              </div>
              <div class="metric-card">
                <div class="metric-value">24/7</div>
                <div class="text-sm text-muted">智能体在线</div>
              </div>
              <div class="metric-card">
                <div class="metric-value">120+</div>
                <div class="text-sm text-muted">排班模板</div>
              </div>
            </div>
            <div class="auth-badges">
              <span class="badge">权限分级</span>
              <span class="badge">排班冲突提醒</span>
              <span class="badge">数据审计</span>
            </div>
            <ul class="feature-list">
              <li>科室与班次一体化管理</li>
              <li>权限分级与安全登录</li>
              <li>智能体协作与任务追踪</li>
            </ul>
          </div>

          <div class="card auth-card">
            <div class="auth-tabs">
              <button class="tab" :class="{ active: auth.view === 'login' }" @click="auth.view = 'login'">登录</button>
              <button class="tab" :class="{ active: auth.view === 'register' }" @click="auth.view = 'register'">注册</button>
            </div>

            <div v-if="auth.view === 'login'" class="mt-4">
              <h2 class="text-center">登录系统</h2>
              <div class="mt-4">
                <div class="form-group">
                  <label>邮箱</label>
                  <input type="email" v-model="auth.form.email" placeholder="admin@hospital.com" />
                </div>
                <div class="form-group">
                  <label>密码</label>
                  <input type="password" v-model="auth.form.password" @keyup.enter="handleLogin" />
                </div>
                <button @click="handleLogin" :disabled="loading">
                  {{ loading ? '登录中...' : '登录' }}
                </button>
                <div class="mt-4 text-center text-sm">
                  没有账号? <span class="text-link" @click="auth.view = 'register'">去注册</span>
                </div>
              </div>
            </div>

            <div v-if="auth.view === 'register'" class="mt-4">
              <h2 class="text-center">注册账号</h2>
              <div class="mt-4">
                <div class="form-group">
                  <label>姓名</label>
                  <input type="text" v-model="auth.registerForm.fullName" placeholder="真实姓名" />
                </div>
                <div class="form-group">
                  <label>邮箱</label>
                  <input type="email" v-model="auth.registerForm.email" placeholder="email@hospital.com" />
                </div>
                <div class="form-group">
                  <label>密码</label>
                  <input type="password" v-model="auth.registerForm.password" />
                </div>
                <div class="form-group">
                  <label>角色 (逗号分隔)</label>
                  <input type="text" v-model="auth.registerForm.roles" placeholder="ADMIN, DOCTOR" />
                </div>
                <div class="form-group">
                  <label>科室</label>
                  <select v-model="auth.registerForm.departmentName">
                    <option value="">请选择科室</option>
                    <optgroup label="内科">
                      <option>呼吸内科</option>
                      <option>消化内科</option>
                      <option>神经内科</option>
                      <option>心血管内科</option>
                      <option>肾内科</option>
                      <option>血液内科</option>
                      <option>免疫科</option>
                      <option>内分泌科</option>
                    </optgroup>
                    <optgroup label="外科">
                      <option>普通外科</option>
                      <option>神经外科</option>
                      <option>心胸外科</option>
                      <option>泌尿外科</option>
                      <option>心血管外科</option>
                      <option>乳腺外科</option>
                      <option>肝胆外科</option>
                      <option>器官移植</option>
                      <option>肛肠外科</option>
                      <option>烧伤科</option>
                      <option>骨外科</option>
                    </optgroup>
                    <optgroup label="妇产科">
                      <option>妇科</option>
                      <option>产科</option>
                      <option>计划生育</option>
                      <option>妇幼保健</option>
                    </optgroup>
                    <optgroup label="男科">
                      <option>男科</option>
                    </optgroup>
                    <optgroup label="儿科">
                      <option>儿科综合</option>
                      <option>小儿内科</option>
                      <option>小儿外科</option>
                      <option>新生儿科</option>
                      <option>儿童营养保健科</option>
                    </optgroup>
                    <optgroup label="五官科">
                      <option>耳鼻喉科</option>
                      <option>眼科</option>
                      <option>口腔科</option>
                    </optgroup>
                    <optgroup label="肿瘤科">
                      <option>肿瘤内科</option>
                      <option>肿瘤外科</option>
                      <option>肿瘤妇科</option>
                      <option>骨肿瘤科</option>
                      <option>放疗科</option>
                      <option>肿瘤康复科</option>
                      <option>肿瘤综合科</option>
                    </optgroup>
                    <optgroup label="皮肤性病科">
                      <option>皮肤科</option>
                      <option>性病科</option>
                    </optgroup>
                    <optgroup label="中医科">
                      <option>中医全科</option>
                      <option>中医内科</option>
                      <option>中医外科</option>
                      <option>中医妇科</option>
                      <option>中医儿科</option>
                      <option>中医保健科</option>
                      <option>针灸按摩科</option>
                      <option>中医骨伤科</option>
                      <option>中医肿瘤科</option>
                    </optgroup>
                    <optgroup label="传染科">
                      <option>肝病科</option>
                      <option>艾滋病科</option>
                      <option>结核病</option>
                      <option>寄生虫</option>
                    </optgroup>
                    <optgroup label="精神心理科">
                      <option>精神科</option>
                      <option>心理咨询科</option>
                    </optgroup>
                    <optgroup label="整形美容科">
                      <option>整形美容科</option>
                    </optgroup>
                    <optgroup label="营养科">
                      <option>营养科</option>
                    </optgroup>
                    <optgroup label="生殖中心">
                      <option>生殖中心</option>
                    </optgroup>
                    <optgroup label="麻醉医学科">
                      <option>麻醉科</option>
                      <option>疼痛科</option>
                    </optgroup>
                    <optgroup label="医学影像科">
                      <option>核医学科</option>
                      <option>放射科</option>
                      <option>超声科</option>
                    </optgroup>
                    <optgroup label="其它科室">
                      <option>药剂科</option>
                      <option>护理科</option>
                      <option>体检科</option>
                      <option>检验科</option>
                      <option>急诊科</option>
                      <option>公共卫生与预防科</option>
                      <option>全科</option>
                      <option>设备科</option>
                    </optgroup>
                  </select>
                </div>
                <button @click="handleRegister" :disabled="loading">
                  {{ loading ? '注册中...' : '注册' }}
                </button>
                <div class="mt-4 text-center text-sm">
                  已有账号? <span class="text-link" @click="auth.view = 'login'">去登录</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="app-layout">
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

          <div v-if="currentView === 'dashboard'" class="page">
            <header class="header">
              <h1>科室与概览</h1>
              <button style="width: auto;" @click="loadDashboard">刷新</button>
            </header>
            <div class="stats">
              <div class="stat-card">
                <div class="stat-title">科室数量</div>
                <div class="stat-value">{{ departments.length }}</div>
              </div>
              <div class="stat-card">
                <div class="stat-title">班次数量</div>
                <div class="stat-value">{{ shifts.length }}</div>
              </div>
              <div class="stat-card">
                <div class="stat-title">待处理任务</div>
                <div class="stat-value">{{ pendingTaskCount }}</div>
              </div>
            </div>

            <div class="dashboard-grid">
              <div class="card summary-card">
                <div class="panel-title">值班统计概览</div>
                <div class="summary-grid">
                  <div>
                    <div class="text-muted text-sm">总班次</div>
                    <div class="summary-value">{{ summary.totalShifts }}</div>
                  </div>
                  <div>
                    <div class="text-muted text-sm">夜班班次</div>
                    <div class="summary-value">{{ summary.nightShifts }}</div>
                  </div>
                  <div>
                    <div class="text-muted text-sm">已指派</div>
                    <div class="summary-value">{{ summary.assignedShifts }}</div>
                  </div>
                  <div>
                    <div class="text-muted text-sm">待指派</div>
                    <div class="summary-value">{{ summary.unassignedShifts }}</div>
                  </div>
                  <div>
                    <div class="text-muted text-sm">参与人员</div>
                    <div class="summary-value">{{ summary.totalAssignees }}</div>
                  </div>
                </div>
                <div class="summary-tags">
                  <span class="tag" v-for="item in summary.roleDistribution" :key="item.label">
                    {{ item.label }} · {{ item.value }}
                  </span>
                  <span v-if="summary.roleDistribution.length === 0" class="text-muted text-sm">暂无角色统计</span>
                </div>
              </div>

              <div class="card summary-card">
                <div class="panel-title">我的排班</div>
                <div class="my-shift-list">
                  <div v-for="shift in myShifts" :key="shift.id" class="my-shift-item">
                    <div>
                      <div class="my-shift-title">{{ shift.departmentName || '科室' }} · {{ shift.requiredRole }}</div>
                      <div class="text-muted text-sm">{{ formatTime(shift.startTime) }} - {{ formatTime(shift.endTime) }}</div>
                    </div>
                    <span :class="['status-badge', 'status-' + shift.status]">{{ shift.status }}</span>
                  </div>
                  <div v-if="myShifts.length === 0" class="text-muted text-sm">暂无指派班次</div>
                </div>
              </div>

              <div class="card calendar-card">
                <div class="calendar-header">
                  <div>
                    <div class="panel-title">值班日历</div>
                    <div class="text-muted text-sm">{{ calendarTitle }}</div>
                  </div>
                  <div class="calendar-actions">
                    <select v-model="calendarFilterDeptId" class="calendar-filter">
                      <option value="">全部科室</option>
                      <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
                    </select>
                    <button class="ghost" style="width: auto;" @click="changeMonth(-1)">上个月</button>
                    <button class="ghost" style="width: auto;" @click="changeMonth(1)">下个月</button>
                  </div>
                </div>
                <div class="calendar-grid">
                  <div class="calendar-weekday" v-for="day in ['日','一','二','三','四','五','六']" :key="day">{{ day }}</div>
                  <div
                    v-for="cell in calendarDays"
                    :key="cell.key"
                    class="calendar-cell"
                    :class="{ 'is-out': !cell.inMonth }"
                    @click="openCalendarDay(cell)"
                  >
                    <div class="calendar-date">{{ cell.dayNumber }}</div>
                    <div class="calendar-items">
                      <span
                        v-for="(item, idx) in cell.items.slice(0, 2)"
                        :key="idx"
                        class="calendar-item"
                        :class="'status-' + item.status"
                      >{{ item.label }}</span>
                      <span v-if="cell.items.length > 2" class="calendar-more">+{{ cell.items.length - 2 }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="card distribution-card">
                <div class="panel-title">人员分布（本月）</div>
                <div class="text-muted text-sm">按被指派次数统计</div>
                <div class="bar-list">
                  <div v-for="item in summary.assigneeDistribution" :key="item.label" class="bar-row">
                    <div class="bar-label">{{ item.label }}</div>
                    <div class="bar-track">
                      <div class="bar-fill" :style="{ width: barWidth(item.value) }"></div>
                    </div>
                    <div class="bar-value">{{ item.value }}</div>
                  </div>
                  <div v-if="summary.assigneeDistribution.length === 0" class="text-muted text-sm">暂无人员分布</div>
                </div>
                <div class="divider"></div>
                <div class="panel-title">科室分布（本月）</div>
                <div class="dept-list">
                  <div v-for="item in summary.departmentDistribution" :key="item.label" class="dept-item">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                  </div>
                  <div v-if="summary.departmentDistribution.length === 0" class="text-muted text-sm">暂无科室分布</div>
                </div>
              </div>
            </div>

            <div v-if="calendarModal.open" class="modal-backdrop" @click.self="closeCalendarModal">
              <div class="modal">
                <div class="modal-header">
                  <div>
                    <div class="panel-title">{{ calendarModal.date }} 值班详情</div>
                    <div class="text-muted text-sm">点击空白可关闭</div>
                  </div>
                  <button class="ghost" style="width: auto;" @click="closeCalendarModal">关闭</button>
                </div>
                <div class="modal-body">
                  <div class="modal-section">
                    <div class="text-muted text-sm">当日安排</div>
                    <div class="modal-list">
                      <div v-for="(item, idx) in calendarModal.items" :key="idx" class="calendar-item" :class="'status-' + item.status">
                        {{ item.label }}
                      </div>
                      <div v-if="calendarModal.items.length === 0" class="text-muted text-sm">暂无记录</div>
                    </div>
                  </div>
                  <div class="modal-section">
                    <div class="text-muted text-sm">新增值班记录</div>
                    <div class="form-group">
                      <label>科室</label>
                      <select v-model="newCalendarEntry.departmentId">
                        <option value="">选择科室</option>
                        <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
                      </select>
                    </div>
                    <div class="form-group">
                      <label>值班摘要</label>
                      <input type="text" v-model="newCalendarEntry.summary" placeholder="例：夜班 3 人" />
                    </div>
                    <div class="form-group">
                      <label>人数</label>
                      <input type="number" min="0" v-model="newCalendarEntry.headcount" />
                    </div>
                    <button style="width: auto;" @click="createCalendarEntry">添加记录</button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="currentView === 'shifts'" class="page">
            <header class="header">
              <h1>班次管理</h1>
              <button style="width: auto;" @click="loadShifts">刷新</button>
            </header>
            <div class="card table-card">
              <table class="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>时间范围</th>
                    <th>科室</th>
                    <th>必需角色</th>
                    <th>状态</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="loadingData"><td colspan="5" class="text-center">加载中...</td></tr>
                  <tr v-else-if="shifts.length === 0"><td colspan="5" class="text-center">暂无排班</td></tr>
                  <tr v-for="shift in shifts" :key="shift.id">
                    <td>{{ shift.id }}</td>
                    <td>
                      {{ formatTime(shift.startTime) }} <br />
                      <span class="text-muted text-sm">{{ formatTime(shift.endTime) }}</span>
                    </td>
                    <td>{{ shift.departmentId }}</td>
                    <td><span class="chip">{{ shift.requiredRole }}</span></td>
                    <td>
                      <span :class="['status-badge', 'status-' + shift.status]">{{ shift.status }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div v-if="currentView === 'agent'" class="page">
            <div class="agent-layout">
              <aside class="agent-sidebar">
                <div class="card compact-card">
                  <div class="panel-title">任务概览</div>
                  <div class="info-list">
                    <div class="info-item">
                      <span>总任务数</span>
                      <strong>{{ agentTasks.length }}</strong>
                    </div>
                    <div class="info-item">
                      <span>处理中</span>
                      <strong>{{ pendingTaskCount }}</strong>
                    </div>
                  </div>
                </div>

                <div class="card compact-card">
                  <div class="panel-title">状态筛选</div>
                  <select v-model="agentFilter">
                    <option value="ALL">全部</option>
                    <option value="PENDING">待处理</option>
                    <option value="IN_PROGRESS">进行中</option>
                    <option value="COMPLETED">已完成</option>
                    <option value="FAILED">失败</option>
                  </select>
                </div>

                <div class="card compact-card">
                  <div class="panel-title">快捷指令</div>
                  <div class="quick-prompts">
                    <button
                      class="prompt-chip"
                      v-for="prompt in quickPrompts"
                      :key="prompt.label"
                      @click="applyQuickPrompt(prompt)"
                    >
                      {{ prompt.label }}
                    </button>
                  </div>
                </div>
              </aside>

              <section class="agent-thread">
                <div class="card chat-header">
                  <div>
                    <h2>智能体排班助手</h2>
                    <div class="text-sm text-muted">与排班Agent协作，实时跟踪任务状态</div>
                  </div>
                  <button class="secondary" style="width: auto;" @click="loadAgentTasks">刷新任务</button>
                </div>

                <div class="message-list">
                  <div v-if="filteredAgentTasks.length === 0" class="text-center text-muted" style="margin-top: 2rem;">
                    暂无符合条件的任务
                  </div>
                  <div v-for="task in filteredAgentTasks" :key="task.id" class="message agent">
                    <div class="message-title">
                      任务 #{{ task.id }} <span :class="['status-badge', 'status-' + task.status]">{{ task.status }}</span>
                    </div>
                    <div class="text-sm">Type: {{ task.taskType }}</div>
                    <div style="margin-top: 0.5rem;">{{ task.payload }}</div>
                  </div>
                </div>

                <div class="card form-group" style="max-width: 100%;">
                  <label>下达指令 (JSON Prompt)</label>
                  <div class="input-row">
                    <select v-model="newTask.type" style="width: 200px;">
                      <option value="GENERATE_SCHEDULE">生成排班 (GENERATE)</option>
                      <option value="VALIDATE_SCHEDULE">校验排班 (VALIDATE)</option>
                      <option value="DATA_SYNC">同步数据 (SYNC)</option>
                    </select>
                    <input type="text" v-model="newTask.payload" placeholder="输入参数或描述..." @keyup.enter="submitTask" />
                    <button style="width: auto;" @click="submitTask" :disabled="loadingAgent">发送</button>
                  </div>
                </div>

                <div class="card chat-panel">
                  <div class="panel-title">智能体沟通区</div>
                  <div class="chat-list">
                    <div v-if="chatMessages.length === 0" class="text-muted text-sm">暂无对话，输入消息开始沟通</div>
                    <div v-for="(msg, idx) in chatMessages" :key="idx" :class="['chat-bubble', msg.role === 'CLIENT' ? 'out' : 'in']">
                      <div class="chat-meta">
                        <strong>{{ msg.sender || '匿名' }}</strong>
                        <span class="text-muted text-sm">{{ formatTime(msg.timestamp) }}</span>
                      </div>
                      <div>{{ msg.content }}</div>
                    </div>
                  </div>
                  <div class="input-row chat-input">
                    <input type="text" v-model="chatInput" placeholder="输入要发送给智能体的消息..." @keyup.enter="sendChat" />
                    <button style="width: auto;" @click="sendChat" :disabled="!wsConnected">发送</button>
                  </div>
                  <div class="text-muted text-sm">实时消息通过 WebSocket 同步，多端可见。</div>
                </div>
              </section>
            </div>
          </div>

          <div v-if="currentView === 'profile'" class="page">
            <header class="header">
              <h1>个人中心</h1>
              <button style="width: auto;" class="secondary" @click="navigate('dashboard')">返回概览</button>
            </header>
            <div class="profile-grid">
              <div class="card profile-panel">
                <div class="profile-header">
                  <div class="avatar avatar-lg">{{ userInitials }}</div>
                  <div>
                    <h2>{{ user.fullName || '未命名用户' }}</h2>
                    <div class="text-muted">{{ user.email || '暂无邮箱' }}</div>
                    <div class="tag-row">
                      <span v-for="role in (user.roles || [])" :key="role" class="tag">{{ role }}</span>
                    </div>
                  </div>
                </div>
                <div class="info-list">
                  <div class="info-item">
                    <span>账号ID</span>
                    <strong>{{ user.id || '-' }}</strong>
                  </div>
                  <div class="info-item">
                    <span>上次登录</span>
                    <strong>{{ formatTime(lastLogin) }}</strong>
                  </div>
                  <div class="info-item">
                    <span>默认视图</span>
                    <strong>{{ viewTitle }}</strong>
                  </div>
                </div>
              </div>

              <div class="card profile-panel">
                <h3>账号安全</h3>
                <div class="info-list">
                  <div class="info-item">
                    <span>安全状态</span>
                    <strong>正常</strong>
                  </div>
                  <div class="info-item">
                    <span>令牌状态</span>
                    <strong>{{ auth.token ? '已登录' : '未登录' }}</strong>
                  </div>
                  <div class="info-item">
                    <span>权限摘要</span>
                    <strong>{{ (user.roles || []).length }} 项</strong>
                  </div>
                </div>
                <div class="notice">
                  如需修改密码或角色，请联系系统管理员。
                </div>
              </div>

              <div class="card profile-panel">
                <h3>快捷入口</h3>
                <div class="action-grid">
                  <button class="secondary" @click="navigate('shifts')">查看排班</button>
                  <button class="secondary" @click="navigate('agent')">智能体任务</button>
                  <button class="secondary" @click="navigate('dashboard')">返回概览</button>
                </div>
              </div>
            </div>

            <div v-if="isAdmin" class="profile-grid" style="margin-top: 1.5rem;">
              <div class="card profile-panel">
                <h3>管理员 - 重置用户密码</h3>
                <div class="form-group">
                  <label>选择用户</label>
                  <select v-model="adminPasswordForm.userId">
                    <option value="">请选择用户</option>
                    <option v-for="u in adminUsers" :key="u.id" :value="u.id">
                      {{ u.fullName }} ({{ u.email }})
                    </option>
                  </select>
                </div>
                <div class="form-group">
                  <label>新密码</label>
                  <input type="password" v-model="adminPasswordForm.newPassword" placeholder="至少 8 位" />
                </div>
                <button style="width: auto;" @click="resetUserPassword" :disabled="loading">更新密码</button>
              </div>

              <div class="card profile-panel">
                <h3>管理员 - 修改班次</h3>
                <div class="form-group">
                  <label>班次</label>
                  <div class="input-row">
                    <select v-model="adminShiftForm.shiftId">
                      <option value="">选择班次</option>
                      <option v-for="shift in shifts" :key="shift.id" :value="shift.id">
                        #{{ shift.id }} {{ formatTime(shift.startTime) }} - {{ formatTime(shift.endTime) }}
                      </option>
                    </select>
                    <button style="width: auto;" class="secondary" @click="loadShiftIntoForm">载入</button>
                  </div>
                </div>
                <div class="form-group">
                  <label>开始时间</label>
                  <input type="datetime-local" v-model="adminShiftForm.startTime" />
                </div>
                <div class="form-group">
                  <label>结束时间</label>
                  <input type="datetime-local" v-model="adminShiftForm.endTime" />
                </div>
                <div class="form-group">
                  <label>必需角色</label>
                  <select v-model="adminShiftForm.requiredRole">
                    <option value="DOCTOR">医生</option>
                    <option value="NURSE">护士</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>状态</label>
                  <select v-model="adminShiftForm.status">
                    <option value="OPEN">OPEN</option>
                    <option value="ASSIGNED">ASSIGNED</option>
                    <option value="COMPLETED">COMPLETED</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>科室ID</label>
                  <input type="number" v-model="adminShiftForm.departmentId" placeholder="例如 1" />
                </div>
                <div class="form-group">
                  <label>指派用户ID (可选)</label>
                  <input type="number" v-model="adminShiftForm.assigneeUserId" />
                </div>
                <div class="form-group">
                  <label>备注</label>
                  <textarea rows="3" v-model="adminShiftForm.notes" placeholder="班次备注"></textarea>
                </div>
                <button style="width: auto;" @click="updateShiftDetails" :disabled="loading">保存班次</button>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  `
}).mount('#app');
