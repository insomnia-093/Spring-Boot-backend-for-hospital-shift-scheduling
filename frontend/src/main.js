import { createApp } from 'vue';
import './style.css';
import App from './App.vue';
import { setupErrorHandling } from './utils/errorHandler.js';


// 立即初始化全局错误处理
setupErrorHandling();

createApp(App).mount('#app');
