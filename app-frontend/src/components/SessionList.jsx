// src/components/SessionList.jsx
import { Button, List, Avatar, Modal } from 'antd';
import { PlusOutlined, MessageOutlined, DeleteOutlined } from '@ant-design/icons';
import { useState } from 'react';

function SessionList({ sessions, currentSessionId, onSelectSession, onNewSession, onDeleteSession }) {
  const [deleteConfirm, setDeleteConfirm] = useState(null);

  const handleDelete = (sessionId, e) => {
    e.stopPropagation();
    Modal.confirm({
      title: '确认删除',
      content: '删除后无法恢复，确定要删除这个会话吗？',
      onOk: () => onDeleteSession(sessionId),
    });
  };

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Button
        type="primary"
        icon={<PlusOutlined />}
        onClick={onNewSession}
        style={{ margin: 16, width: 'calc(100% - 32px)' }}
      >
        新会话
      </Button>

      <List
        style={{ flex: 1, overflow: 'auto' }}
        dataSource={sessions}
        renderItem={session => (
          <List.Item
            onClick={() => onSelectSession(session.id)}
            style={{
              cursor: 'pointer',
              background: currentSessionId === session.id ? '#e6f7ff' : 'transparent',
              padding: '12px 16px',
              borderBottom: '1px solid #f0f0f0'
            }}
            actions={[
              <DeleteOutlined
                key="delete"
                style={{ color: '#ff4d4f', cursor: 'pointer' }}
                onClick={(e) => handleDelete(session.id, e)}
              />
            ]}
          >
            <List.Item.Meta
              avatar={<Avatar icon={<MessageOutlined />} />}
              title={session.title || '新对话'}
              description={session.lastTime || '刚刚'}
            />
          </List.Item>
        )}
      />
    </div>
  );
}

export default SessionList;