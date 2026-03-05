import { ref, reactive } from 'vue';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:9090/api';

export function useApi() {
  const loading = ref(false);
  const token = ref(localStorage.getItem('jwt_token') || null);

  const api = async (endpoint, options = {}) => {
    const headers = {
      'Content-Type': 'application/json',
      ...(token.value ? { 'Authorization': `Bearer ${token.value}` } : {})
    };

    try {
      const res = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers: { ...headers, ...options.headers }
      });

      if (res.status === 401) {
        console.warn('认证失败');
        token.value = null;
        localStorage.removeItem('jwt_token');
        throw new Error('认证失败，请重新登录');
      }

      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        const errorMsg = err.message || err.error || `HTTP ${res.status}`;
        throw new Error(errorMsg);
      }

      const payload = await res.json().catch(() => null);

      // 兼容两类后端返回：
      // 1) 直接返回业务对象
      // 2) 返回统一包裹 { code, message, data }
      if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'data')) {
        return payload.data;
      }

      return payload;

    } catch (error) {
      console.error(`API 调用异常 [${endpoint}]:`, error);
      throw error;
    }
  };

  return {
    loading,
    api,
    token,
    API_BASE
  };
}
