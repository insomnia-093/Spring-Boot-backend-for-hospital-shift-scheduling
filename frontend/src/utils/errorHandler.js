/**
 * 全局错误处理和日志上传
 */

const API_BASE = globalThis.API_BASE || 'http://localhost:9090/api';

/**
 * 设置全局错误处理
 */
export function setupErrorHandling() {
  // 未捕获的同步错误
  window.addEventListener('error', (event) => {
    console.error('❌ 未捕获的错误:', event.error);
    if (event.error && event.error.stack) {
      logErrorToServer({
        type: 'uncaught-error',
        message: event.error.message || '未知错误',
        stack: event.error.stack,
        timestamp: new Date().toISOString(),
        url: window.location.href
      });
    }
  });

  // Promise 未捕获的拒绝
  window.addEventListener('unhandledrejection', (event) => {
    console.error('❌ Promise 拒绝:', event.reason);
    logErrorToServer({
      type: 'unhandled-rejection',
      message: event.reason?.message || String(event.reason),
      timestamp: new Date().toISOString(),
      url: window.location.href
    });
  });
}

/**
 * 上传错误日志到服务器
 */
async function logErrorToServer(error) {
  try {
    await fetch(`${API_BASE}/logs/error`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('jwt_token') || ''}`
      },
      body: JSON.stringify(error)
    }).catch((e) => {
      console.warn('无法上传错误日志:', e.message);
    });
  } catch (e) {
    console.error('错误日志处理异常:', e);
  }
}

/**
 * 包装 API 调用，自动处理错误
 */
export async function apiCall(endpoint, options = {}) {
  try {
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    const token = localStorage.getItem('jwt_token');
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      const error = new Error(errorData.message || `HTTP ${response.status}`);
      error.status = response.status;
      error.data = errorData;
      throw error;
    }

    return await response.json();
  } catch (error) {
    console.error(`API 调用失败 [${endpoint}]:`, error);
    logErrorToServer({
      type: 'api-error',
      endpoint,
      message: error.message,
      status: error.status,
      timestamp: new Date().toISOString()
    });
    throw error;
  }
}
