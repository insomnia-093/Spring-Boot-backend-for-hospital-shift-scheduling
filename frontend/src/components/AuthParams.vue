<template>
  <div v-if="!token" class="auth-container">
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
          <button class="tab" :class="{ active: view === 'login' }" @click="view = 'login'">登录</button>
          <button class="tab" :class="{ active: view === 'register' }" @click="view = 'register'">注册</button>
        </div>

        <div v-if="view === 'login'" class="mt-4">
          <h2 class="text-center">登录系统</h2>
          <div class="mt-4">
            <div class="form-group">
              <label>邮箱</label>
              <input type="email" v-model.trim="form.email" placeholder="admin@hospital.com" />
            </div>
            <div class="form-group">
              <label>密码</label>
              <input type="password" v-model="form.password" @keyup.enter="handleLogin" />
            </div>
            <div v-if="loginError" class="form-error">{{ loginError }}</div>
            <button @click="handleLogin" :disabled="loading || !canLogin">
              {{ loading ? '登录中...' : '登录' }}
            </button>
            <div class="mt-4 text-center text-sm">
              没有账号? <span class="text-link" @click="view = 'register'">去注册</span>
            </div>
          </div>
        </div>

        <div v-if="view === 'register'" class="mt-4">
          <h2 class="text-center">注册账号</h2>
          <div class="mt-4">
            <div class="form-group">
              <label>姓名</label>
              <input type="text" v-model.trim="registerForm.fullName" placeholder="真实姓名" />
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input type="email" v-model.trim="registerForm.email" placeholder="email@hospital.com" />
            </div>
            <div class="form-group">
              <label>密码</label>
              <input type="password" v-model="registerForm.password" placeholder="至少 8 位" />
            </div>
            <div class="form-group">
              <label>科室</label>
              <select v-model="registerForm.departmentId" :disabled="deptLoading">
                <option value="">{{ deptLoading ? '科室加载中...' : '请选择科室' }}</option>
                <option v-for="dept in departments" :key="dept.id" :value="dept.id">
                  {{ dept.name }}
                </option>
              </select>
              <div class="form-hint" v-if="deptError">{{ deptError }}</div>
              <div class="form-hint" v-else>可选：如不选择，将以默认科室注册</div>
            </div>
            <div v-if="registerError" class="form-error">{{ registerError }}</div>
            <button @click="handleRegister" :disabled="loading || !canRegister">
              {{ loading ? '注册中...' : '注册' }}
            </button>
            <div class="mt-4 text-center text-sm">
              已有账号? <span class="text-link" @click="view = 'login'">去登录</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useApi } from '../composables/useApi';

const emit = defineEmits(['login-success']);
const { api, loading, token } = useApi();

const view = ref('login');
const form = reactive({ email: '', password: '' });
const registerForm = reactive({
  email: '',
  password: '',
  fullName: '',
  departmentId: ''
});

const departments = ref([]);
const deptLoading = ref(false);
const deptError = ref('');
const loginError = ref('');
const registerError = ref('');

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const canLogin = computed(() => emailPattern.test(form.email) && form.password.length >= 6);
const canRegister = computed(() => {
  if (!registerForm.fullName.trim()) return false;
  if (!emailPattern.test(registerForm.email)) return false;
  if ((registerForm.password || '').length < 8) return false;
  return true;
});

const loadDepartments = async () => {
  deptLoading.value = true;
  deptError.value = '';
  try {
    const res = await api('/departments', { method: 'GET' });
    departments.value = Array.isArray(res) ? res : [];
  } catch (e) {
    deptError.value = '科室加载失败，请稍后重试';
    departments.value = [];
  } finally {
    deptLoading.value = false;
  }
};

const handleLogin = async () => {
  loginError.value = '';
  if (!canLogin.value) {
    loginError.value = '请输入有效邮箱与密码';
    return;
  }
  loading.value = true;
  try {
    const res = await api('/auth/login', {
      method: 'POST',
      body: JSON.stringify(form)
    });
    loginSuccess(res);
  } catch (e) {
    loginError.value = '登录失败: ' + e.message;
  } finally {
    loading.value = false;
  }
};

const handleRegister = async () => {
  registerError.value = '';
  if (!canRegister.value) {
    registerError.value = '请完善姓名、邮箱与密码（至少 8 位）';
    return;
  }
  loading.value = true;
  try {
    const departmentId = registerForm.departmentId ? parseInt(registerForm.departmentId, 10) : null;
    const body = {
      email: registerForm.email,
      password: registerForm.password,
      fullName: registerForm.fullName,
      departmentId: Number.isNaN(departmentId) ? null : departmentId
    };

    const res = await api('/auth/register', {
      method: 'POST',
      body: JSON.stringify(body)
    });
    loginSuccess(res);
  } catch (e) {
    registerError.value = '注册失败: ' + e.message;
  } finally {
    loading.value = false;
  }
};

const loginSuccess = (res) => {
  localStorage.setItem('jwt_token', res.token);
  localStorage.setItem('user_info', JSON.stringify({
    id: res.userId,
    email: res.email,
    fullName: res.fullName,
    roles: res.roles
  }));
  localStorage.setItem('last_login', new Date().toISOString());

  // 触发事件通知父组件
  emit('login-success', res);
};

onMounted(() => {
  loadDepartments();
});
</script>

