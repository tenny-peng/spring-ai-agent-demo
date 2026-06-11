// src/api/client.js
import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080/api',  // 你的 Spring 后端地址
  timeout: 30000,
});

// 请求拦截器：自动添加 token
client.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：处理错误
client.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default client;