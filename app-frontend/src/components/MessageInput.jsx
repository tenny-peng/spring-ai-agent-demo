// src/components/MessageInput.jsx
import { useState } from 'react';
import { Input, Button, Switch } from 'antd';
import { SendOutlined } from '@ant-design/icons';

const { TextArea } = Input;

function MessageInput({ onSend, loading, webSearchEnabled, onWebSearchChange }) {
  const [message, setMessage] = useState('');

  const handleSend = () => {
    if (message.trim() && !loading) {
      onSend(message.trim());
      setMessage('');
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div style={{ padding: '16px', borderTop: '1px solid #f0f0f0', background: '#ffffff' }}>
      <div style={{ display: 'flex', gap: '8px' }}>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 8 }}>
          <Switch
            checked={webSearchEnabled}
            onChange={onWebSearchChange}
            checkedChildren="联网搜索"
            unCheckedChildren="联网搜索"
          />
        </div>
        <TextArea
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="输入消息... (Shift+Enter 换行，Enter 发送)"
          autoSize={{ minRows: 1, maxRows: 4 }}
          disabled={loading}
          style={{ flex: 1 }}
        />
        <Button
          type="primary"
          icon={<SendOutlined />}
          onClick={handleSend}
          loading={loading}
          disabled={!message.trim()}
        >
          发送
        </Button>
      </div>
    </div>
  );
}

export default MessageInput;