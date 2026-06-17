import client from './client';

export const adminApi = {
  // 用户管理
  getUsers: (params) => client.post('/admin/user/pageList', params),

  // 文档管理
  getDocuments: (params) => client.post('/admin/document/pageList', params),
  uploadDocument: (formData) => client.post('/admin/document/upload', formData),
  deleteDocument: (id) => client.delete(`/admin/document/delete/${id}`),
};