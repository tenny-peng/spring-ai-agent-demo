// src/components/SessionList.jsx
import { Button, Avatar, Modal, Input, Tooltip } from 'antd';
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

      <div style={{ flex: 1, overflow: 'auto' }}>
        {sessions.map(session => (
          <div
            key={session.conversationId}
            onClick={() => onSelectSession(session.conversationId)}
            style={{
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: 12,
              padding: '12px 16px',
              borderBottom: '1px solid #f0f0f0',
              background: currentSessionId === session.conversationId ? '#e6f7ff' : 'transparent'
            }}
          >
            <Avatar icon={<MessageOutlined />} />
            <div style={{ flex: 1, minWidth: 0 }}>
              {editingId === session.conversationId ? (
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
              ) : (
                <div style={{ fontWeight: 500, fontSize: 14, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {session.title || '新对话'}
                </div>
              )}
              <div style={{ fontSize: 12, color: '#999', marginTop: 2 }}>
                {session.updatedAt || '刚刚'}
              </div>
            </div>
            <div style={{ display: 'flex', gap: 8, flexShrink: 0 }} onClick={e => e.stopPropagation()}>
              <Tooltip title="重命名">
                <EditOutlined style={{ color: '#1890ff', cursor: 'pointer' }} onClick={(e) => startEdit(session, e)} />
              </Tooltip>
              <Tooltip title="删除">
                <DeleteOutlined style={{ color: '#ff4d4f', cursor: 'pointer' }} onClick={(e) => handleDelete(session.conversationId, e)} />
              </Tooltip>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default SessionList;