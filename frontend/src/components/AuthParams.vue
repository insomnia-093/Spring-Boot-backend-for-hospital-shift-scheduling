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
              <input type="email" v-model="form.email" placeholder="admin@hospital.com" />
            </div>
            <div class="form-group">
              <label>密码</label>
              <input type="password" v-model="form.password" @keyup.enter="handleLogin" />
            </div>
            <button @click="handleLogin" :disabled="loading">
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
              <input type="text" v-model="registerForm.fullName" placeholder="真实姓名" />
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input type="email" v-model="registerForm.email" placeholder="email@hospital.com" />
            </div>
            <div class="form-group">
              <label>密码</label>
              <input type="password" v-model="registerForm.password" />
            </div>
            <div class="form-group">
              <label>角色 (逗号分隔)</label>
              <input type="text" v-model="registerForm.roles" placeholder="ADMIN, DOCTOR" />
            </div>
            <div class="form-group">
              <label>科室</label>
              <select v-model="registerForm.departmentName">
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
              已有账号? <span class="text-link" @click="view = 'login'">去登录</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useApi } from '../composables/useApi';

const emit = defineEmits(['login-success']);
const { api, loading, token } = useApi();

const view = ref('login');
const form = reactive({ email: '', password: '' });
const registerForm = reactive({
  email: '',
  password: '',
  fullName: '',
  roles: 'ADMIN',
  departmentId: null,
  departmentName: ''
});

const handleLogin = async () => {
  loading.value = true;
  try {
    const res = await api('/auth/login', {
      method: 'POST',
      body: JSON.stringify(form)
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
    const roles = registerForm.roles.split(',').map(r => r.trim()).filter(Boolean);
    const departmentId = registerForm.departmentId ? parseInt(registerForm.departmentId, 10) : null;
    const departmentName = registerForm.departmentName ? registerForm.departmentName.trim() : '';
    const body = {
      email: registerForm.email,
      password: registerForm.password,
      fullName: registerForm.fullName,
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
</script>

