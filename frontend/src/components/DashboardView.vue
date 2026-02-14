<template>
  <div class="dashboard-wrapper">
    <div class="dashboard-container">
      <!-- 标题区 -->
      <div class="section-header">
        <h2>科室与概览</h2>
        <button class="btn-refresh" @click="$emit('refresh')">
          <span>🔄</span> 刷新
        </button>
      </div>

      <!-- 统计卡片 - 3列 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">科室数量</div>
          <div class="stat-value">{{ departments.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">班次数量</div>
          <div class="stat-value">{{ shifts.length }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">待处理任务</div>
          <div class="stat-value">{{ pendingTaskCount }}</div>
        </div>
      </div>

      <!-- 下方3列布局 -->
      <div class="content-grid">
        <!-- 中列：我的排班 -->
        <div class="card">
          <div class="card-header">我的排班</div>
          <div class="card-body">
            <div v-if="myShifts.length > 0" class="shift-list">
              <div v-for="shift in myShifts.slice(0, 5)" :key="shift.id" class="shift-item">
                <div class="shift-dept">{{ shift.departmentName || '科室' }}</div>
                <div class="shift-role">{{ shift.requiredRole }}</div>
                <div class="shift-time">{{ formatTime(shift.startTime) }}</div>
              </div>
            </div>
            <div v-else class="empty-state">暂无指派班次</div>
          </div>
        </div>

        <!-- 右列：值班日历 -->
        <div class="card calendar-card">
          <div class="calendar-header">
            <div class="calendar-title">值班日历</div>
            <div class="calendar-controls">
              <select :value="filterDeptId" @change="$emit('update:filterDeptId', $event.target.value)" class="dept-select">
                <option value="">全部科室</option>
                <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
              </select>
              <div class="month-buttons">
                <button class="month-btn" @click="$emit('changeMonth', -1)">上个月</button>
                <button class="month-btn" @click="$emit('changeMonth', 1)">下个月</button>
              </div>
            </div>
          </div>
          <div class="calendar-body">
            <div class="calendar-date-display">{{ calendarTitle }}</div>
            <div class="calendar-grid-header">
              <div class="weekday" v-for="day in ['日','一','二','三','四','五','六']" :key="day">{{ day }}</div>
            </div>
            <div class="calendar-grid">
              <div
                v-for="cell in calendarDays"
                :key="cell.key"
                class="calendar-cell"
                :class="{ 'is-out': !cell.inMonth, 'is-today': isToday(cell) }"
                @click="$emit('openDay', cell)"
              >
                <div class="calendar-date">{{ cell.dayNumber }}</div>
                <div class="calendar-items">
                  <span
                    v-for="(item, idx) in cell.items.slice(0, 2)"
                    :key="idx"
                    class="calendar-item"
                    :title="item.label"
                  >{{ item.label }}</span>
                  <span v-if="cell.items.length > 2" class="calendar-more">+{{ cell.items.length - 2 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  departments: Array,
  shifts: Array,
  pendingTaskCount: Number,
  summary: Object,
  myShifts: Array,
  calendarTitle: String,
  calendarDays: Array,
  filterDeptId: [String, Number]
});

defineEmits(['refresh', 'update:filterDeptId', 'changeMonth', 'openDay', 'navigate']);

const topNavItems = ['dashboard', 'shifts', 'agent'];

const formatTime = (isoString) => {
  if (!isoString) return '-';
  const d = new Date(isoString);
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
};

const isToday = (cell) => {
  const today = new Date();
  const d = new Date(cell.key);
  return d.getDate() === today.getDate() &&
         d.getMonth() === today.getMonth() &&
         d.getFullYear() === today.getFullYear();
};
</script>

<style scoped>
.dashboard-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f3ff 0%, #f0ebff 100%);
}

.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 0 28px 32px 28px;
  height: 100%;
  overflow-y: auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 0 4px;
}

.section-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #2c2c2c;
  margin: 0;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.btn-refresh {
  padding: 10px 18px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.btn-refresh:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.4);
}

.btn-refresh:active {
  transform: translateY(0);
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 8px;
}

.stat-card {
  background: white;
  padding: 28px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(99, 102, 241, 0.1);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}

.stat-card:hover {
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.15);
  transform: translateY(-4px);
  border-color: rgba(99, 102, 241, 0.3);
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 14px;
  display: block;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 内容网格 */
.content-grid {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) minmax(380px, 2fr);
  gap: 24px;
  flex: 1;
  grid-auto-rows: minmax(0, 1fr);
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(99, 102, 241, 0.1);
}

.card:hover {
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.15);
  transform: translateY(-4px);
}

.card-header {
  padding: 20px;
  font-size: 15px;
  font-weight: 700;
  color: #2c2c2c;
  border-bottom: 1px solid #f3f0ff;
  background: #fafafa;
}

.card-body {
  padding: 28px 20px;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 排班列表 */
.shift-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shift-item {
  padding: 12px;
  border-radius: 8px;
  background: #f9f7ff;
  font-size: 12px;
  border-left: 3px solid #6366f1;
  transition: all 0.2s ease;
}

.shift-item:hover {
  background: #f3f0ff;
  transform: translateX(4px);
}

.shift-dept {
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 4px;
}

.shift-role {
  color: #666;
  font-size: 11px;
  margin-bottom: 4px;
}

.shift-time {
  color: #6366f1;
  font-size: 11px;
  font-weight: 500;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 32px 12px;
  color: #aaa;
  font-size: 13px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 日历卡片 */
.calendar-card {
  grid-column: 2;
  grid-row: 1;
  display: flex;
  flex-direction: column;
  min-height: 520px;
}

.calendar-header {
  padding: 20px;
  border-bottom: 1px solid #f3f0ff;
  background: #fafafa;
}

.calendar-title {
  font-size: 15px;
  font-weight: 700;
  color: #2c2c2c;
  margin-bottom: 12px;
}

.calendar-controls {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.dept-select {
  flex: 1;
  min-width: 100px;
  padding: 8px 10px;
  border: 1px solid #e5e0ff;
  border-radius: 6px;
  font-size: 12px;
  background: white;
  color: #2c2c2c;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dept-select:hover {
  border-color: #6366f1;
  background: #f9f7ff;
}

.dept-select:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.month-buttons {
  display: flex;
  gap: 6px;
}

.month-btn {
  padding: 6px 12px;
  border: 1px solid #e5e0ff;
  border-radius: 6px;
  background: white;
  color: #2c2c2c;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
  white-space: nowrap;
}

.month-btn:hover {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border-color: transparent;
  transform: translateY(-1px);
}

.calendar-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.calendar-date-display {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 500;
}

.calendar-grid-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  font-size: 11px;
  font-weight: 700;
  color: #666;
  padding: 4px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  flex: 1;
  min-height: 360px;
}

.calendar-cell {
  aspect-ratio: 1;
  border: 1px solid #e5e0ff;
  border-radius: 6px;
  padding: 4px;
  font-size: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}

.calendar-cell:hover {
  border-color: #6366f1;
  background: #f9f7ff;
  transform: translateY(-1px);
}

.calendar-cell.is-out {
  opacity: 0.4;
  cursor: default;
  background: #fafafa;
}

.calendar-cell.is-out:hover {
  border-color: #e5e0ff;
  background: #fafafa;
  transform: none;
}

.calendar-cell.is-today {
  border: 2px solid #6366f1;
  font-weight: 700;
  background: #ede9fe;
}

.calendar-date {
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 1px;
}

.calendar-items {
  display: flex;
  flex-direction: column;
  gap: 1px;
  width: 100%;
  align-items: center;
  justify-content: flex-end;
}

.calendar-item {
  font-size: 8px;
  background: #6366f1;
  color: white;
  padding: 1px 2px;
  border-radius: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  font-weight: 500;
}

.calendar-more {
  font-size: 8px;
  color: #6366f1;
  font-weight: 600;
}

@media (max-width: 1400px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .calendar-card {
    grid-column: auto;
    grid-row: auto;
  }
}

@media (max-width: 900px) {
  .stats-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .calendar-card {
    grid-column: auto;
    grid-row: auto;
  }

  .dashboard-container {
    gap: 20px;
    padding: 20px;
  }
}
</style>
