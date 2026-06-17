import { useState, useEffect } from 'react';
import { Table, Input, Card } from 'antd';
import { adminApi } from '../../api/admin';

function UserManagement() {
  const [data, setData] = useState({ records: [], total: 0 });
  const [loading, setLoading] = useState(false);
  const [params, setParams] = useState({ page: 1, size: 20, username: '' });

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await adminApi.getUsers(params);
      setData(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, [params]);

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '用户名', dataIndex: 'username' },
    { title: '邮箱', dataIndex: 'email' },
    { title: '角色', dataIndex: 'role' },
    { title: '会话数', dataIndex: 'conversationCount' },
    { title: '注册时间', dataIndex: 'createdAt' },
    { title: '最后活跃', dataIndex: 'updatedAt' },
  ];

  return (
    <Card title="用户管理">
      <Input.Search
        placeholder="搜索用户名"
        allowClear
        style={{ width: 300, marginBottom: 16 }}
        onSearch={(val) => setParams(p => ({ ...p, page: 1, username: val }))}
      />
      <Table
        rowKey="id"
        columns={columns}
        dataSource={data.records}
        loading={loading}
        pagination={{
          current: params.page,
          pageSize: params.size,
          total: data.total,
          onChange: (page, size) => setParams(p => ({ ...p, page, size })),
        }}
      />
    </Card>
  );
}

export default UserManagement;