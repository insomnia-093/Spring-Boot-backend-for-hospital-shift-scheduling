<template>
  <header class="header">
    <h1>个人中心</h1>
    <button style="width: auto;" class="secondary" @click="$emit('navigate', 'dashboard')">返回概览</button>
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
        <button class="secondary" @click="$emit('navigate', 'shifts')">查看排班</button>
        <button class="secondary" @click="$emit('navigate', 'agent')">智能体任务</button>
        <button class="secondary" @click="$emit('navigate', 'dashboard')">返回概览</button>
      </div>
    </div>
  </div>

  <div v-if="isAdmin" class="profile-grid" style="margin-top: 1.5rem;">
    <div class="card profile-panel">
      <h3>管理员 - 重置用户密码</h3>
      <div class="form-group">
        <label>选择用户</label>
        <select v-model="passwordForm.userId">
          <option value="">请选择用户</option>
          <option v-for="u in adminUsers" :key="u.id" :value="u.id">
            {{ u.fullName }} ({{ u.email }})
          </option>
        </select>
      </div>
      <div class="form-group">
        <label>新密码</label>
        <input type="password" v-model="passwordForm.newPassword" placeholder="至少 8 位" />
      </div>
      <button style="width: auto;" @click="resetUserPassword" :disabled="loading">更新密码</button>
    </div>

    <div class="card profile-panel">
      <h3>管理员 - 修改班次</h3>
      <div class="form-group">
        <label>班次</label>
        <div class="input-row">
          <select v-model="shiftForm.shiftId">
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
        <input type="datetime-local" v-model="shiftForm.startTime" />
      </div>
      <div class="form-group">
        <label>结束时间</label>
        <input type="datetime-local" v-model="shiftForm.endTime" />
      </div>
      <!-- 其他表单项省略以节省空间，可按需补充 -->
      <button style="width: auto;" @click="updateShiftDetails" :disabled="loading">保存班次</button>
    </div>

    <div class="card profile-panel">
      <h3>科室值班统计（夜班）</h3>
      <div class="text-muted text-sm" style="margin-bottom: 1rem;">按科室统计已指派的夜班人数</div>
      <div style="display: flex; gap: 2rem; align-items: center;">
        <div style="flex: 1; text-align: center;">
          <div class="pie-chart" :style="pieData.style" style="width: 150px; height: 150px; border-radius: 50%; margin: 0 auto;"></div>
        </div>
        <div style="flex: 1;">
          <div class="pie-legend">
            <div v-for="item in pieData.items" :key="item.label" class="legend-item">
              <span class="legend-color" :style="{ backgroundColor: item.color }"></span>
              <span class="legend-label">{{ item.label }}</span>
              <span class="legend-value">{{ item.value }} 人</span>
            </div>
            <div v-if="pieData.items.length === 0" class="text-muted text-sm">暂无数据</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue';

const props = defineProps({
  user: Object,
  lastLogin: String,
  isAdmin: Boolean,
  adminUsers: Array,
  shifts: Array,
  loading: Boolean,
  pieData: Object
});

const emit = defineEmits(['navigate', 'reset-password', 'update-shift']);

const passwordForm = reactive({ userId: '', newPassword: '' });
const shiftForm = reactive({
  shiftId: '',
  startTime: '',
  endTime: '',
  requiredRole: 'DOCTOR',
  status: 'OPEN',
  departmentId: '',
  assigneeUserId: '',
  notes: ''
});

const userInitials = computed(() => {
  const name = (props.user.fullName || props.user.email || 'U').trim();
  if (!name) return 'U';
  if (name.length <= 2) return name.toUpperCase();
  return name.slice(0, 2).toUpperCase();
});

const formatTime = (isoString) => {
  if (!isoString) return '-';
  return new Date(isoString).toLocaleString();
};

const resetUserPassword = () => {
  if (!passwordForm.userId || !passwordForm.newPassword) return;
  emit('reset-password', { ...passwordForm });
  passwordForm.newPassword = '';
};

const toLocalInput = (isoString) => {
  if (!isoString) return '';
  const date = new Date(isoString);
  const pad = (val) => String(val).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const loadShiftIntoForm = () => {
  const shift = props.shifts.find(item => String(item.id) === String(shiftForm.shiftId));
  if (!shift) return;
  shiftForm.startTime = toLocalInput(shift.startTime);
  shiftForm.endTime = toLocalInput(shift.endTime);
  // ... 其他字段赋值
};

const updateShiftDetails = () => {
  if (!shiftForm.shiftId) return;
  emit('update-shift', { ...shiftForm });
};
</script>
