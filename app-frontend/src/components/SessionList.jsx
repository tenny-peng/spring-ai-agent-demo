// src/components/SessionList.jsx
import { Button, List, Avatar, Modal, Input, Tooltip } from 'antd';
import { PlusOutlined, MessageOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons';
import { useState, useRef } from 'react';

function SessionList({ sessions, currentSessionId, onSelectSession, onNewSession, onDeleteSession, onRenameSession }) {
  const [editingId, setEditingId] = useState(null);
  const [editTitle, setEditTitle] = useState('');
  const inputRef = useRef(null);

  const handleDelete = (conversationId, e) => {
    e.stopPropagation();
    Modal.confirm({
      title: '确认删除',
      content: '删除后无法恢复，确定要删除这个会话吗？',
      onOk: () => onDeleteSession(conversationId),
    });
  };

  const startEdit = (session, e) => {
    e.stopPropagation();
    setEditingId(session.conversationId);
    setEditTitle(session.title || '');
    setTimeout(() => inputRef.current?.focus(), 0);
  };

  const confirmEdit = async () => {
    const trimmed = editTitle.trim();
    if (!trimmed || !editingId) {
      setEditingId(null);
      return;
    }
    await onRenameSession(editingId, trimmed);
    setEditingId(null);
  };

  const cancelEdit = () => {
    setEditingId(null);
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
            onClick={() => onSelectSession(session.conversationId)}
            style={{
              cursor: 'pointer',
              background: currentSessionId === session.conversationId ? '#e6f7ff' : 'transparent',
              padding: '12px 16px',
              borderBottom: '1px solid #f0f0f0'
            }}
            actions={[
              <Tooltip title="重命名" key="edit">
                <EditOutlined
                  style={{ color: '#1890ff', cursor: 'pointer' }}
                  onClick={(e) => startEdit(session, e)}
                />
              </Tooltip>,
              <Tooltip title="删除" key="delete">
                <DeleteOutlined
                  style={{ color: '#ff4d4f', cursor: 'pointer' }}
                  onClick={(e) => handleDelete(session.conversationId, e)}
                />
              </Tooltip>
            ]}
          >
            <List.Item.Meta
              avatar={<Avatar icon={<MessageOutlined />} />}
              title={editingId === session.conversationId ? (
                <Input
                  ref={inputRef}
                  size="small"
                  value={editTitle}
                  onChange={e => setEditTitle(e.target.value)}
                  onPressEnter={confirmEdit}
                  onBlur={confirmEdit}
                  onKeyDown={e => e.key === 'Escape' && cancelEdit()}
                  onClick={e => e.stopPropagation()}
                  style={{ width: '100%' }}
                />
              ) : session.title || '新对话'}
              description={session.updatedAt || '刚刚'}
            />
          </List.Item>
        )}
      />
    </div>
  );
}

export default SessionList;