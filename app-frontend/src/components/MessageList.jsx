// src/components/MessageList.jsx
import { useEffect, useRef } from 'react';
import { Avatar, Spin } from 'antd';
import { UserOutlined, RobotOutlined } from '@ant-design/icons';

function MessageList({ messages, loading }) {
  const messagesEndRef = useRef(null);

  // 自动滚动到底部
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <div style={{ flex: 1, overflow: 'auto', padding: '20px', background: '#f5f5f5' }}>
      {messages.map((msg, index) => (
        <div
          key={index}
          style={{
            display: 'flex',
            justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
            marginBottom: '16px',
          }}
        >
          <div style={{ display: 'flex', maxWidth: '70%', alignItems: 'flex-start' }}>
            {msg.role !== 'user' && (
              <Avatar icon={<RobotOutlined />} style={{ marginRight: '8px', background: '#52c41a' }} />
            )}
            <div
              style={{
                padding: '10px 15px',
                borderRadius: '12px',
                background: msg.role === 'user' ? '#1677ff' : '#ffffff',
                color: msg.role === 'user' ? '#ffffff' : '#000000',
                wordBreak: 'break-word',
              }}
            >
              {msg.content}
            </div>
            {msg.role === 'user' && (
              <Avatar icon={<UserOutlined />} style={{ marginLeft: '8px', background: '#1677ff' }} />
            )}
          </div>
        </div>
      ))}
      {loading && (
        <div style={{ textAlign: 'center' }}>
          <Spin description="AI 正在思考..." />
        </div>
      )}
      <div ref={messagesEndRef} />
    </div>
  );
}

export default MessageList;