// src/components/UserMemoryDrawer.jsx
import { useState, useEffect } from 'react';
import { Drawer, List, Button, Tag, Input, message, Popconfirm, Empty, Select, Table } from 'antd';
import { PlusOutlined, DeleteOutlined, BulbOutlined } from '@ant-design/icons';
import { userMemoryApi } from '../api/userMemory';

const categoryColors = {
  PREFERENCE: 'gold',
  PERSONAL_INFO: 'blue',
  HABIT: 'green',
  OTHER: 'default',
};

const categoryLabels = {
  PREFERENCE: '偏好',
  PERSONAL_INFO: '个人信息',
  HABIT: '习惯',
  OTHER: '其他',
};

function UserMemoryDrawer({ open, onClose }) {
  const [memories, setMemories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [newContent, setNewContent] = useState('');
  const [newCategory, setNewCategory] = useState('OTHER');

  const loadMemories = async () => {
    setLoading(true);
    try {
      const res = await userMemoryApi.getMemories();
      setMemories(res.data);
    } catch {
      message.error('加载记忆失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open) loadMemories();
  }, [open]);

  const handleAdd = async () => {
    if (!newContent.trim()) return;
    try {
      await userMemoryApi.add({ content: newContent.trim(), category: newCategory });
      message.success('添加成功');
      setNewContent('');
      loadMemories();
    } catch {
      message.error('添加失败');
    }
  };

  const handleDelete = async (id) => {
    try {
      await userMemoryApi.delete(id);
      loadMemories();
    } catch {
      message.error('删除失败');
    }
  };

  return (
    <Drawer
      title="我的记忆"
      open={open}
      onClose={onClose}
      size={400}
    >
      <div style={{ marginBottom: 16, display: 'flex', gap: 8 }}>
        <Input
          value={newContent}
          onChange={(e) => setNewContent(e.target.value)}
          placeholder="输入要记住的信息..."
          onPressEnter={handleAdd}
          style={{ flex: 1 }}
        />
        <Select
          value={newCategory}
          onChange={setNewCategory}
          style={{ width: 100 }}
          size="small"
          options={[
            { value: 'OTHER', label: '其他' },
            { value: 'PREFERENCE', label: '偏好' },
            { value: 'PERSONAL_INFO', label: '个人信息' },
            { value: 'HABIT', label: '习惯' },
          ]}
        />
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加
        </Button>
      </div>

      <Table
        dataSource={memories}
        rowKey="id"
        loading={loading}
        pagination={false}
        locale={{ emptyText: <Empty description="暂无记忆" /> }}
        showHeader={false}
        columns={[
          {
            dataIndex: 'category',
            width: 70,
            render: (cat) => (
              <Tag color={categoryColors[cat] || 'default'}>
                {categoryLabels[cat] || cat}
              </Tag>
            ),
          },
          {
            dataIndex: 'content',
            ellipsis: true,
          },
          {
            width: 50,
            render: (_, record) => (
              <Popconfirm
                title="确定删除这条记忆？"
                onConfirm={() => handleDelete(record.id)}
              >
                <Button type="text" danger icon={<DeleteOutlined />} />
              </Popconfirm>
            ),
          },
        ]}
      />
    </Drawer>
  );
}

export default UserMemoryDrawer;