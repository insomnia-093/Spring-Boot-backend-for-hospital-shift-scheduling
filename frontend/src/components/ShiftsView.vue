<template>
  <header class="header">
    <h1>班次管理</h1>
    <button class="btn-refresh" style="width: auto;" @click="$emit('refresh')">刷新</button>
  </header>

  <div class="stats">
    <div class="stat-card">
      <div class="stat-title">总班次</div>
      <div class="stat-value">{{ shifts.length }}</div>
    </div>
    <div class="stat-card">
      <div class="stat-title">已指派</div>
      <div class="stat-value" style="color: #10b981;">{{ assignedCount }}</div>
    </div>
    <div class="stat-card">
      <div class="stat-title">待指派</div>
      <div class="stat-value" style="color: #f59e0b;">{{ unassignedCount }}</div>
    </div>
    <div class="stat-card">
      <div class="stat-title">夜班</div>
      <div class="stat-value" style="color: #6366f1;">{{ nightShiftCount }}</div>
    </div>
  </div>

  <div class="shifts-layout">
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
          <tr v-if="loading"><td colspan="5" class="text-center">加载中...</td></tr>
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

    <div class="card distribution-card">
      <div class="panel-title">班次类型占比</div>
      <div class="pie-3d-wrapper">
        <div class="pie-3d-container">
          <div class="pie-3d" :style="pieChartStyle"></div>
          <div class="pie-3d-thickness" :style="pieChartThicknessStyle"></div>
        </div>
        <div class="pie-legend-list">
           <div class="legend-item">
             <span class="legend-dot" style="background: #a855f7;"></span>
             <span>白班 {{ dayShiftCount }}</span>
           </div>
           <div class="legend-item">
             <span class="legend-dot" style="background: #312e81;"></span>
             <span>夜班 {{ nightShiftCount }}</span>
           </div>
        </div>
      </div>
      <div class="divider"></div>

      <div class="panel-title">人员分布（本月）</div>
      <div class="text-muted text-sm">按被指派次数统计</div>
      <div class="bar-list">
        <div v-for="item in assigneeDistribution" :key="item.label" class="bar-row">
          <div class="bar-label">{{ item.label }}</div>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: barWidth(item.value) }"></div>
          </div>
          <div class="bar-value">{{ item.value }}</div>
        </div>
        <div v-if="assigneeDistribution.length === 0" class="text-muted text-sm">暂无人员分布</div>
      </div>
      <div class="divider"></div>
      <div class="panel-title">科室分布（本月）</div>
      <div class="dept-list">
        <div v-for="item in departmentDistribution" :key="item.label" class="dept-item">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
        <div v-if="departmentDistribution.length === 0" class="text-muted text-sm">暂无科室分布</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  shifts: Array,
  loading: Boolean,
  assigneeDistribution: Array,
  departmentDistribution: Array
});

defineEmits(['refresh']);

const assignedCount = computed(() => props.shifts.filter(s => !!s.assigneeUserId).length);
const unassignedCount = computed(() => props.shifts.filter(s => !s.assigneeUserId).length);
const nightShiftCount = computed(() => props.shifts.filter(s => s.shiftType === 'NIGHT').length);
const dayShiftCount = computed(() => props.shifts.length - nightShiftCount.value);

const pieChartStyle = computed(() => {
  const total = props.shifts.length || 1;
  const nightPercent = (nightShiftCount.value / total) * 100;
  // 夜班深紫色 #312e81，白班浅紫色 #a855f7
  return {
    background: `conic-gradient(
      #312e81 0% ${nightPercent}%, 
      #a855f7 ${nightPercent}% 100%
    )`
  };
});

// Calculate thickness color roughly darker
const pieChartThicknessStyle = computed(() => {
    const total = props.shifts.length || 1;
    const nightPercent = (nightShiftCount.value / total) * 100;
    // Darker shades for thickness
    return {
      background: `conic-gradient(
        #1e1b4b 0% ${nightPercent}%, 
        #7e22ce ${nightPercent}% 100%
      )`
    };
});

const formatTime = (isoString) => {
  if (!isoString) return '-';
  return new Date(isoString).toLocaleString();
};

const barWidth = (value) => {
  const values = props.assigneeDistribution.map(item => item.value || 0);
  const max = Math.max(1, ...values);
  const ratio = Math.min(1, (value || 0) / max);
  return `${Math.round(ratio * 100)}%`;
};
</script>